package cn.chloeprime.kuromusic.common.network;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

interface Packet {
    default void handle(Supplier<NetworkEvent.Context> context) {
        var ctx = context.get();
        ctx.enqueueWork(() -> handle(ctx));
        ctx.setPacketHandled(true);
    }

    void handle(NetworkEvent.Context context);
}
