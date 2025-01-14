package cn.chloeprime.kuromusic.common.network;

import cn.chloeprime.kuromusic.KuroMusic;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {
    @SubscribeEvent
    public static void onNetworkRegister(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(KuroMusic.MODID).versioned("1.0.0").optional();
        registrar.playToClient(Packet.typeOf(ClientboundPlayMusicPacket.class), Packet.codec(ClientboundPlayMusicPacket::encode, ClientboundPlayMusicPacket::new), Packet::handle);
        registrar.playToClient(Packet.typeOf(ClientboundSetBackgroundMusicPacket.class), Packet.codec(ClientboundSetBackgroundMusicPacket::encode, ClientboundSetBackgroundMusicPacket::new), Packet::handle);
        registrar.playToClient(Packet.typeOf(ClientboundStopSelfBackgroundMusicPacket.class), Packet.codec(ClientboundStopSelfBackgroundMusicPacket::encode, ClientboundStopSelfBackgroundMusicPacket::new), Packet::handle);
    }
}
