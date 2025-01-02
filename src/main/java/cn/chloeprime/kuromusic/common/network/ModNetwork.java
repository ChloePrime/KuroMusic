package cn.chloeprime.kuromusic.common.network;

import cn.chloeprime.kuromusic.KuroMusic;
import com.google.common.base.Predicates;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ModNetwork {
    public static final String PROTOCOL_VERSION = "1.0.0";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            KuroMusic.loc("play_channel"),
            () -> PROTOCOL_VERSION, Predicates.alwaysTrue(), PROTOCOL_VERSION::equals);

    private static final AtomicInteger ID_COUNT = new AtomicInteger(1);

    public static void init() {
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientboundPlayMusicPacket.class, Packet::encode, ClientboundPlayMusicPacket::new, Packet::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientboundSetBackgroundMusicPacket.class, Packet::encode, ClientboundSetBackgroundMusicPacket::new, Packet::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
