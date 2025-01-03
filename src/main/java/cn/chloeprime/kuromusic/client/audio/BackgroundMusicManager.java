package cn.chloeprime.kuromusic.client.audio;

import cn.chloeprime.kuromusic.common.ModSoundEvents;
import cn.chloeprime.kuromusic.common.command.SetBackgroundMusicCommand;
import cn.chloeprime.kuromusic.mixin.client.MusicManagerAccessor;
import cn.chloeprime.kuromusic.platform.MusicUrlTransformer;
import cn.chloeprime.kuromusic.util.BuggySupplier;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.LongComparators;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundMusicManager {
    public static Optional<Music> current() {
        return MUSIC_TABLE.values().stream().findFirst();
    }

    public static CompletableFuture<Void> set(long priority, String url) {
        if (SetBackgroundMusicCommand.DEV_NULL.equals(url)) {
            return clear(priority);
        }
        var factory = ExternalSound.createStreamFactory(MusicUrlTransformer.doTransform(url));
        if (factory == null) {
            return CompletableFuture.completedFuture(null);
        }
        var newCanceller = new AtomicBoolean();
        var oldCanceller = CANCELLER_TABLE.put(priority, newCanceller);
        if (oldCanceller != null) {
            oldCanceller.set(true);
        }
        return CompletableFuture
                .supplyAsync(factory::getSilently, Util.backgroundExecutor())
                .thenApply(input -> BuggySupplier.getSilently(input::readAllBytes))
                .thenApply(data -> {
                    var se = ModSoundEvents.BACKGROUND_MUSIC.getHolder().orElseThrow();
                    var ref = new Music[1];
                    return ref[0] = new ExternalMusic(se, data, () -> stopPlaying(ref[0]));
                })
                .thenAcceptAsync(music -> {
                    if (newCanceller.get()) {
                        throw new CancellationException();
                    }
                    var old = MUSIC_TABLE.put(priority, music);
                    stopPlaying(old);
                }, MC);
    }

    public static CompletableFuture<Void> clear(long priority) {
        return CompletableFuture.runAsync(() -> {
            var old = MUSIC_TABLE.remove(priority);
            if (old != null) {
                stopPlaying(old);
            }
        }, MC);
    }

    public static CompletableFuture<Void> clear() {
        return CompletableFuture.runAsync(() -> {
            MUSIC_TABLE.values().forEach(BackgroundMusicManager::stopPlaying);
            MUSIC_TABLE.clear();
        }, MC);
    }

    public static void stopPlaying(Music music) {
        MC.getMusicManager().stopPlaying(music);
        ((MusicManagerAccessor) MC.getMusicManager()).setNextSongDelay(0);
    }

    private static final ConcurrentMap<Long, AtomicBoolean> CANCELLER_TABLE = new ConcurrentHashMap<>();
    private static final Long2ObjectSortedMap<Music> MUSIC_TABLE = new Long2ObjectRBTreeMap<>(LongComparators.OPPOSITE_COMPARATOR);
    private static final Minecraft MC = Minecraft.getInstance();
}
