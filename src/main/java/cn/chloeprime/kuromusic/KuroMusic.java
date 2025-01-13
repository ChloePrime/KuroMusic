package cn.chloeprime.kuromusic;

import cn.chloeprime.kuromusic.common.ModSoundEvents;
import cn.chloeprime.kuromusic.common.command.PlayMusicCommand;
import cn.chloeprime.kuromusic.common.command.SetBackgroundMusicCommand;
import cn.chloeprime.kuromusic.common.command.StopSelfBackgroundMusicCommand;
import cn.chloeprime.kuromusic.common.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(KuroMusic.MODID)
public class KuroMusic {
    public static final String MODID = "kuromusic";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }

    public KuroMusic() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModSoundEvents.init(bus);
        bus.register(this);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::init);
        event.enqueueWork(() -> MinecraftForge.EVENT_BUS.addListener(KuroMusic::registerCommands));
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        PlayMusicCommand.register(dispatcher);
        SetBackgroundMusicCommand.register(dispatcher);
        StopSelfBackgroundMusicCommand.register(dispatcher);
    }
}
