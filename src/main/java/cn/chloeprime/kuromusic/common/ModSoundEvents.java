package cn.chloeprime.kuromusic.common;

import cn.chloeprime.kuromusic.KuroMusic;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

public class ModSoundEvents {
    private static final DeferredRegister<SoundEvent> DFR = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, KuroMusic.MODID);
    public static final Holder<SoundEvent> MUSIC = DFR.register("music", () -> SoundEvent.createFixedRangeEvent(KuroMusic.loc("music"), 64));
    public static final Holder<SoundEvent> BACKGROUND_MUSIC = DFR.register("background_music", () -> SoundEvent.createFixedRangeEvent(KuroMusic.loc("background_music"), Integer.MAX_VALUE));

    @ApiStatus.Internal
    public static void init(IEventBus bus) {
        DFR.register(bus);
    }
}
