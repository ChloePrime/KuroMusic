package cn.chloeprime.kuromusic.client;

import cn.chloeprime.kuromusic.client.audio.BackgroundMusicManager;
import cn.chloeprime.kuromusic.client.audio.ExternalSound;
import cn.chloeprime.kuromusic.client.audio.VanillaMusicOverrideTracker;
import cn.chloeprime.kuromusic.common.ModSoundEvents;
import cn.chloeprime.kuromusic.common.command.SetBackgroundMusicCommand;
import cn.chloeprime.kuromusic.common.network.ClientboundPlayMusicPacket;
import cn.chloeprime.kuromusic.common.network.ClientboundSetBackgroundMusicPacket;
import cn.chloeprime.kuromusic.mixin.client.MusicManagerAccessor;
import com.google.common.base.Suppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.function.Supplier;

public class ClientNetworkHandler {
    public static final Minecraft MC = Minecraft.getInstance();
    public static final Supplier<ResourceLocation> MUSIC_EVENT_ID = Suppliers.memoize(ModSoundEvents.MUSIC::getId);

    public static void handlePlayMusicPacket(ClientboundPlayMusicPacket packet) {
        MC.getSoundManager().stop(MUSIC_EVENT_ID.get(), packet.source);

        var player = MC.player;
        var x = packet.isPositioned ? packet.x() : player != null ? player.getX() : 0;
        var y = packet.isPositioned ? packet.y() : player != null ? player.getY() : 0;
        var z = packet.isPositioned ? packet.z() : player != null ? player.getZ() : 0;
        var attenuation = packet.isPositioned ? SoundInstance.Attenuation.LINEAR : SoundInstance.Attenuation.NONE;

        var overrideVanillaMusic = !packet.isPositioned;
        var vanillaMusicSilencer = overrideVanillaMusic ? new Object() : null;
        if (overrideVanillaMusic) {
            VanillaMusicOverrideTracker.silenceForReason(vanillaMusicSilencer);
        }

        var instance = new ExternalSound(
                packet.url, MUSIC_EVENT_ID.get(),
                packet.source, packet.volume, packet.pitch,
                RandomSource.create(packet.seed),
                false, 0, attenuation,
                x, y, z, overrideVanillaMusic,
                overrideVanillaMusic
                        ? () -> VanillaMusicOverrideTracker.unsilenceForReason(vanillaMusicSilencer)
                        : () -> {}
        );
        MC.getSoundManager().play(instance);
    }

    public static void handleSetBgmPacket(ClientboundSetBackgroundMusicPacket packet) {
        var mc = Minecraft.getInstance();
        if (SetBackgroundMusicCommand.DEV_NULL.equals(packet.url)) {
            BackgroundMusicManager.clear();
            return;
        }
        BackgroundMusicManager.set(packet.priority, packet.url).thenAcceptAsync(_void -> {
            mc.getMusicManager().stopPlaying();
            ((MusicManagerAccessor) mc.getMusicManager()).setNextSongDelay(0);
        }, mc);
    }
}
