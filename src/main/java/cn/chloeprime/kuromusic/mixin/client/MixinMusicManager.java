package cn.chloeprime.kuromusic.mixin.client;

import cn.chloeprime.kuromusic.client.audio.ExternalMusic;
import cn.chloeprime.kuromusic.client.audio.VanillaMusicOverrideTracker;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

@Mixin(MusicManager.class)
public abstract class MixinMusicManager {
    @Shadow @Nullable private SoundInstance currentMusic;
    @Shadow @Final private Minecraft minecraft;
    @Shadow private int nextSongDelay;

    private static final @Unique WeakReference<ExternalMusic> kuromusic$NULL_REF = new WeakReference<>(null);
    private @Unique WeakReference<ExternalMusic> kuromusic$currentExternalMusic = kuromusic$NULL_REF;

    @Inject(method = "tick", at = @At("HEAD"))
    private void fixDelayOverflowProblem(CallbackInfo ci) {
        nextSongDelay = Math.max(0, nextSongDelay);
    }

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

    @Inject(method = "startPlaying", at = @At("HEAD"))
    private void recordCurrentExternalMusic(Music music, CallbackInfo ci) {
        if (music instanceof ExternalMusic externalMusic) {
            kuromusic$currentExternalMusic = new WeakReference<>(externalMusic);
        }
    }

    @Inject(method = "isPlayingMusic", at = @At("HEAD"), cancellable = true)
    private void compareExternalMusicByRef(Music music, CallbackInfoReturnable<Boolean> cir) {
        if (currentMusic == null) {
            return;
        }
        if (music instanceof ExternalMusic externalMusic) {
            cir.setReturnValue(externalMusic == kuromusic$currentExternalMusic.get());
        }
    }

    private @Unique Music kuromusic$capturedNewMusic;

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/sounds/Music;getEvent()Lnet/minecraft/core/Holder;"))
    private Holder<SoundEvent> mc121fix$captureMusicInstance(Music music, Operation<Holder<SoundEvent>> original) {
        kuromusic$capturedNewMusic = music;
        return original.call(music);
    }

    @ModifyExpressionValue(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;equals(Ljava/lang/Object;)Z"))
    private boolean mc121fix$modifyExternalMusicEquality(boolean original) {
        try {
            if (kuromusic$capturedNewMusic instanceof ExternalMusic) {
                return kuromusic$capturedNewMusic == kuromusic$currentExternalMusic.get();
            }
            return original;
        } finally {
            kuromusic$capturedNewMusic = null;
        }
    }

    @Inject(method = "stopPlaying()V", at = @At("RETURN"))
    private void removeRecordedCurrentExternalMusic(CallbackInfo ci) {
        kuromusic$currentExternalMusic = kuromusic$NULL_REF;
    }
}
