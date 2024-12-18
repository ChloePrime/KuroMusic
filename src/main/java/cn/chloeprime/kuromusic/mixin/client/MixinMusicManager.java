package cn.chloeprime.kuromusic.mixin.client;

import cn.chloeprime.kuromusic.client.audio.VanillaMusicOverrideTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(MusicManager.class)
public abstract class MixinMusicManager {
    @Shadow @Nullable private SoundInstance currentMusic;
    @Shadow @Final private Minecraft minecraft;
    @Shadow private int nextSongDelay;

    @Inject(method = "tick", at = @At("HEAD"))
    private void silenceWhenNeeded(CallbackInfo ci) {
        if (!VanillaMusicOverrideTracker.shouldSilenceVanillaMusic()) {
            return;
        }
        if (currentMusic != null) {
            minecraft.getSoundManager().stop(currentMusic);
            currentMusic = null;
        }
        nextSongDelay = Math.max(nextSongDelay, 20);
    }
}
