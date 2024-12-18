package cn.chloeprime.kuromusic.common;

import cn.chloeprime.kuromusic.KuroMusic;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus;

public class ModSoundEvents {
    private static final DeferredRegister<SoundEvent> DFR = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, KuroMusic.MODID);
    public static final RegistryObject<SoundEvent> MUSIC = DFR.register("music", () -> SoundEvent.createFixedRangeEvent(KuroMusic.loc("music"), 64));

    @ApiStatus.Internal
    public static void init(IEventBus bus) {
        DFR.register(bus);
    }
}
