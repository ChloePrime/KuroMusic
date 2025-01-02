package cn.chloeprime.kuromusic.common.network;

import cn.chloeprime.kuromusic.client.ClientNetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

public class ClientboundPlayMusicPacket implements Packet {
    public static final float LOCATION_ACCURACY = 8.0F;
    public final String url;
    public final SoundSource source;
    public final boolean isPositioned;
    private final int packedX;
    private final int packedY;
    private final int packedZ;
    public final int range;
    public final float volume;
    public final float pitch;
    public final long seed;

    public ClientboundPlayMusicPacket(String url, SoundSource pSource, boolean isPositioned, double x, double y, double z, float range, float pVolume, float pPitch, long pSeed) {
        this.url = url;
        this.source = pSource;
        this.isPositioned = isPositioned;
        this.packedX = (int)(x * LOCATION_ACCURACY);
        this.packedY = (int)(y * LOCATION_ACCURACY);
        this.packedZ = (int)(z * LOCATION_ACCURACY);
        this.range = (int)(range * LOCATION_ACCURACY);
        this.volume = pVolume;
        this.pitch = pPitch;
        this.seed = pSeed;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(url);
        buf.writeEnum(source);

        buf.writeBoolean(isPositioned);
        if (isPositioned) {
            buf.writeVarInt(packedX);
            buf.writeVarInt(packedY);
            buf.writeVarInt(packedZ);
            buf.writeVarInt(range);
        }

        buf.writeFloat(volume);
        buf.writeFloat(pitch);
        buf.writeLong(seed);
    }

    public double x() {
        return packedX / (double) LOCATION_ACCURACY;
    }

    public double y() {
        return packedY / (double) LOCATION_ACCURACY;
    }

    public double z() {
        return packedZ / (double) LOCATION_ACCURACY;
    }

    public ClientboundPlayMusicPacket(FriendlyByteBuf buf) {
        this.url = buf.readUtf();
        this.source = buf.readEnum(SoundSource.class);

        this.isPositioned = buf.readBoolean();
        this.packedX = isPositioned ? buf.readVarInt() : 0;
        this.packedY = isPositioned ? buf.readVarInt() : 0;
        this.packedZ = isPositioned ? buf.readVarInt() : 0;
        this.range = isPositioned ? buf.readVarInt() : 0;

        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
        this.seed = buf.readLong();
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ClientNetworkHandler.handlePlayMusicPacket(this);
    }
}
