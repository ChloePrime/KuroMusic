package cn.chloeprime.kuromusic.client.audio;

import cn.chloeprime.kuromusic.KuroMusic;
import cn.chloeprime.kuromusic.platform.MusicUrlContext;
import cn.chloeprime.kuromusic.platform.MusicUrlTransformResult;
import cn.chloeprime.kuromusic.platform.MusicUrlTransformer;
import cn.chloeprime.kuromusic.util.BuggySupplier;
import cn.chloeprime.kuromusic.util.RequestUtil;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class ExternalSound extends SimpleSoundInstance {
    private static boolean downloadUrlMusics() {
        return true;
    }

    private final BuggySupplier<AudioInputStream> streamFactory;
    private final Runnable onFinishHook;

    public ExternalSound(String url, ResourceLocation id, SoundSource category, float volume, float pitch, RandomSource random, boolean looping, int pDelay, Attenuation pAttenuation, double x, double y, double z, boolean relative, Runnable onFinishHook) {
        super(id, category, volume, pitch, random, looping, pDelay, pAttenuation, x, y, z, relative);
        this.streamFactory = createStreamFactory(MusicUrlTransformer.doTransform(url));
        this.onFinishHook = onFinishHook;
    }

    @Nullable
    private static BuggySupplier<AudioInputStream> createStreamFactory(MusicUrlTransformResult context) {
        var input = context.url();
        var xRealIp = context.useRealIpInHeader();
        // URL
        try {
            var url = new URL(input);
            if (url.getProtocol().equals("file")) {
                var asFile = new File(url.toURI());
                if (asFile.isFile()) {
                    return () -> AudioSystem.getAudioInputStream(asFile);
                } else {
                    return handleUnrecognizedInput(input);
                }
            } else {
                return () -> {
                    URL realUrl = resolveHttpRedirects(url, xRealIp);
                    return downloadUrlMusics()
                            ? AudioSystem.getAudioInputStream(new ByteArrayInputStream(downloadMusic(realUrl, xRealIp)))
                            : AudioSystem.getAudioInputStream(realUrl);
                };
            }
        } catch (MalformedURLException | URISyntaxException ignored) {
        }

        // File / Path
        try {
            var asFile = Paths.get(input);
            if (Files.isRegularFile(asFile)) {
                return () -> AudioSystem.getAudioInputStream(asFile.toFile());
            } else {
                return handleUnrecognizedInput(input);
            }
        } catch (InvalidPathException ignored) {
            // continue;
        }

        KuroMusic.LOGGER.warn("Unrecognized music url: {}", input);
        return null;
    }

    public static URL resolveHttpRedirects(URL url, boolean xRealIp) throws IOException {
        var maxRetries = 5;
        URL finalUrl = url;
        for (int i = 0; i < maxRetries; i++) {
            var conn = RequestUtil.openConnection(url, xRealIp);
            try {
                if (conn instanceof HttpURLConnection httpConnection) {
                    var code = httpConnection.getResponseCode();
                    if (code == HttpURLConnection.HTTP_MOVED_TEMP || code == HttpURLConnection.HTTP_MOVED_PERM) {
                        finalUrl = new URL(conn.getHeaderField("Location"));
                        continue;
                    }
                }
                break;
            } finally {
                conn.getInputStream().close();
            }
        }
        return finalUrl;
    }

    public static byte[] downloadMusic(URL url, boolean xRealIp) throws IOException {
        try (var stream = RequestUtil.openConnection(url, xRealIp).getInputStream()) {
            return stream.readAllBytes();
        }
    }

    private static <T> T handleUnrecognizedInput(String input) {
        KuroMusic.LOGGER.warn("Trying to play a non-existing music file: {}", input);
        return null;
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        if (streamFactory == null) {
            return super.getStream(soundBuffers, sound, looping);
        }
        var isRelative = this.relative;
        return CompletableFuture.supplyAsync(() -> {
            try {
                return isRelative
                        ? new ExternalAudioStream(streamFactory.get(), onFinishHook)
                        : new ExternalAudioStream(streamFactory.get(), new Vec3(x, y, z), onFinishHook);
            } catch (Exception ex) {
                KuroMusic.LOGGER.error("Error creating music stream", ex);
                return (AudioStream) null;
            }
        }, Util.backgroundExecutor()).thenComposeAsync(stream -> {
            if (stream != null) {
                return CompletableFuture.completedFuture(stream);
            } else {
                return super.getStream(soundBuffers, sound, looping);
            }
        }, Util.backgroundExecutor());
    }
}
