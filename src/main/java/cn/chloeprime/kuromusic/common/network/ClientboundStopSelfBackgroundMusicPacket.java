package cn.chloeprime.kuromusic.common.network;

import cn.chloeprime.kuromusic.client.ClientNetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClientboundStopSelfBackgroundMusicPacket implements Packet {
    public ClientboundStopSelfBackgroundMusicPacket() {
    }

    public ClientboundStopSelfBackgroundMusicPacket(FriendlyByteBuf buf) {
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ClientNetworkHandler.handleStopSelfBackgroundMusic();
    }
}
