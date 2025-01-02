package cn.chloeprime.kuromusic.mixin.client;

import cn.chloeprime.kuromusic.client.audio.ExternalMusic;
import cn.chloeprime.kuromusic.client.audio.VanillaMusicOverrideTracker;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
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

    @WrapOperation(
            method = "startPlaying",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forMusic(Lnet/minecraft/sounds/SoundEvent;)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;"))
    private SimpleSoundInstance createExternalInstanceForExternalMusic(SoundEvent sound, Operation<SimpleSoundInstance> original, Music music) {
        if (music instanceof ExternalMusic extMusic) {
            return extMusic.createInstance();
        } else {
            return original.call(sound);
        }
    }
}
