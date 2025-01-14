package cn.chloeprime.kuromusic.common.network;

import cn.chloeprime.kuromusic.client.ClientNetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientboundStopSelfBackgroundMusicPacket implements Packet {
    public ClientboundStopSelfBackgroundMusicPacket() {
    }

    public ClientboundStopSelfBackgroundMusicPacket(FriendlyByteBuf buf) {
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public void handle(IPayloadContext context) {
        ClientNetworkHandler.handleStopSelfBackgroundMusic();
    }
}
