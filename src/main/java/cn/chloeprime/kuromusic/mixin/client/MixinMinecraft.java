package cn.chloeprime.kuromusic.mixin.client;

import cn.chloeprime.kuromusic.client.audio.BackgroundMusicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.Music;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow @Nullable public LocalPlayer player;

    @Inject(
            method = "getSituationalMusic",
            at = @At("RETURN"),
            cancellable = true,
            slice = @Slice(
                    from = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;")
            ))
    private void modifyBackgroundMusic(CallbackInfoReturnable<Music> cir) {
        if (player == null) {
            return;
        }
        BackgroundMusicManager.current().ifPresent(cir::setReturnValue);
    }
}
