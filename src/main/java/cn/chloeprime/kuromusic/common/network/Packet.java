package cn.chloeprime.kuromusic.common.network;

import cn.chloeprime.kuromusic.KuroMusic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

interface Packet extends CustomPacketPayload {
    ClassValue<CustomPacketPayload.Type<? extends Packet>> PACKET_TYPES = new ClassValue<>() {
        @Override
        protected Type<? extends Packet> computeValue(@NotNull Class<?> type) {
            return new Type<>(KuroMusic.loc(type.getSimpleName().toLowerCase(Locale.ROOT)));
        }
    };

    static <P extends Packet> StreamCodec<? super RegistryFriendlyByteBuf, P> codec(
            BiConsumer<P, ? super RegistryFriendlyByteBuf> encoder,
                    Function<? super RegistryFriendlyByteBuf, P> decoder
    ) {
        return StreamCodec.of(
                (buf, packet) -> encoder.accept(packet, buf),
                decoder::apply
        );
    }

    @SuppressWarnings("unchecked")
    static <P extends Packet> CustomPacketPayload.Type<P> typeOf(Class<P> clazz) {
        return (Type<P>) PACKET_TYPES.get(clazz);
    }

    @Override
    default @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPES.get(getClass());
    }

    void encode(RegistryFriendlyByteBuf buf);
    void handle(IPayloadContext context);
}
