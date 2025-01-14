package cn.chloeprime.kuromusic;

import cn.chloeprime.kuromusic.common.ModSoundEvents;
import cn.chloeprime.kuromusic.common.command.PlayMusicCommand;
import cn.chloeprime.kuromusic.common.command.SetBackgroundMusicCommand;
import cn.chloeprime.kuromusic.common.command.StopSelfBackgroundMusicCommand;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import java.util.Objects;

@Mod(KuroMusic.MODID)
public class KuroMusic {
    public static final String MODID = "kuromusic";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public KuroMusic() {
        var bus = Objects.requireNonNull(ModLoadingContext.get().getActiveContainer().getEventBus());
        ModSoundEvents.init(bus);
        bus.register(this);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> NeoForge.EVENT_BUS.addListener(KuroMusic::registerCommands));
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        PlayMusicCommand.register(dispatcher);
        SetBackgroundMusicCommand.register(dispatcher);
        StopSelfBackgroundMusicCommand.register(dispatcher);
    }
}
