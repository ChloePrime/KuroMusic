package cn.chloeprime.kuromusic.client.audio;

import cn.chloeprime.kuromusic.common.ModSoundEvents;
import cn.chloeprime.kuromusic.common.command.SetBackgroundMusicCommand;
import cn.chloeprime.kuromusic.platform.MusicUrlTransformer;
import cn.chloeprime.kuromusic.util.BuggySupplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BackgroundMusicManager {
    public static Optional<Music> current() {
        return Optional.ofNullable(current);
    }

    public static CompletableFuture<Void> set(String url) {
        if (SetBackgroundMusicCommand.DEV_NULL.equals(url)) {
            return clear();
        }
        var factory = ExternalSound.createStreamFactory(MusicUrlTransformer.doTransform(url));
        if (factory == null) {
            return CompletableFuture.completedFuture(null);
        }
        var newCanceller = new AtomicBoolean();
        var oldCanceller = CANCELLER.getAndSet(newCanceller);
        if (oldCanceller != null) {
            oldCanceller.set(true);
        }
        return CompletableFuture
                .supplyAsync(factory::getSilently, Util.backgroundExecutor())
                .thenApply(input -> BuggySupplier.getSilently(input::readAllBytes))
                .thenApply(data -> {
                    var se = ModSoundEvents.BACKGROUND_MUSIC.getHolder().orElseThrow();
                    return new ExternalMusic(se, data);
                })
                .thenAcceptAsync(music -> {
                    if (newCanceller.get()) {
                        throw new CancellationException();
                    }
                    current = music;
                }, CLIENT_MAIN_THREAD);
    }

    public static CompletableFuture<Void> clear() {
        return CompletableFuture.runAsync(() -> {
            current = null;
            CLIENT_MAIN_THREAD.getMusicManager().stopPlaying();
        }, CLIENT_MAIN_THREAD);
    }

    private static Music current;
    private static final AtomicReference<AtomicBoolean> CANCELLER = new AtomicReference<>();
    private static final Minecraft CLIENT_MAIN_THREAD = Minecraft.getInstance();
}
