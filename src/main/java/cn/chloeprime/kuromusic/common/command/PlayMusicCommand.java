package cn.chloeprime.kuromusic.common.command;

import cn.chloeprime.kuromusic.KuroMusic;
import cn.chloeprime.kuromusic.common.ModPermissions;
import cn.chloeprime.kuromusic.common.ModSoundEvents;
import cn.chloeprime.kuromusic.common.network.ClientboundPlayMusicPacket;
import cn.chloeprime.kuromusic.common.network.ModNetwork;
import cn.chloeprime.kuroutils.PermissionUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class PlayMusicCommand {
    private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType(Component.translatable("commands.playsound.failed"));
    private static final String[] URL_EXAMPLES = {
            "\"https://www.example.com/foo.mp3\"",
            "\"C:\\Users\\Administrator\\Music\\Your Favourite Music.ogg\"",
    };

    public static final SuggestionProvider<CommandSourceStack> EXAMPLE_URLS = SuggestionProviders.register(
            KuroMusic.loc("example_urls"),
            (context, builder) -> SharedSuggestionProvider.suggest(URL_EXAMPLES, builder)
    );

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        var head = Commands.argument("url", StringArgumentType.string()).suggests(EXAMPLE_URLS);

        for(SoundSource soundsource : SoundSource.values()) {
            head.then(source(soundsource));
        }

        pDispatcher.register(Commands.literal("playmusic").requires(
                PermissionUtils.checker(ModPermissions.PLAY_MUSIC)
        ).then(head));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> source(SoundSource category) {
        return Commands.literal(category.getName()).then(Commands.argument("targets", EntityArgument.players()).executes((context) -> {
            return playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), category, null, 0, 1, 1, 0);
        }).then(Commands.argument("pos", Vec3Argument.vec3()).executes((context) -> {
            return playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), category, Vec3Argument.getVec3(context, "pos"), ModSoundEvents.MUSIC.get().getRange(1), 1, 1, 0);
        }).then(Commands.argument("range", FloatArgumentType.floatArg()).executes((context) -> {
            return playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), category, Vec3Argument.getVec3(context, "pos"), FloatArgumentType.getFloat(context, "range"), 1, 1, 0);
        }).then(Commands.argument("volume", FloatArgumentType.floatArg(0)).executes((context) -> {
            return playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), category, Vec3Argument.getVec3(context, "pos"), FloatArgumentType.getFloat(context, "range"), context.getArgument("volume", Float.class), 1, 0);
        }).then(Commands.argument("pitch", FloatArgumentType.floatArg(0, 2)).executes((context) -> {
            return playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), category, Vec3Argument.getVec3(context, "pos"), FloatArgumentType.getFloat(context, "range"), context.getArgument("volume", Float.class), context.getArgument("pitch", Float.class), 0);
        }).then(Commands.argument("minVolume", FloatArgumentType.floatArg(0, 1)).executes((context) -> {
            return playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), category, Vec3Argument.getVec3(context, "pos"), FloatArgumentType.getFloat(context, "range"), context.getArgument("volume", Float.class), context.getArgument("pitch", Float.class), context.getArgument("minVolume", Float.class));
        })))))).then(Commands.literal("global").executes((context) -> {
            return playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), category, null, 0, 1, 1, 0);
        }).then(Commands.argument("volume", FloatArgumentType.floatArg(0)).executes((context) -> {
            return playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), category, null, 0, context.getArgument("volume", Float.class), 1, 0);
        }).then(Commands.argument("pitch", FloatArgumentType.floatArg(0, 2)).executes((context) -> {
            return playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), category, null, 0, context.getArgument("volume", Float.class), context.getArgument("pitch", Float.class), 0);
        }).then(Commands.argument("minVolume", FloatArgumentType.floatArg(0, 1)).executes((context) -> {
            return playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), category, null, 0, context.getArgument("volume", Float.class), context.getArgument("pitch", Float.class), context.getArgument("minVolume", Float.class));
        }))))));
    }

    private static int playMusic(CommandSourceStack source, Collection<ServerPlayer> targets, String url, SoundSource category, @Nullable Vec3 pos, float range, float volume, float pitch, float pMinVolume) throws CommandSyntaxException {
        var sqrRange = Mth.square(range);
        var count = 0;
        var seed = source.getLevel().getRandom().nextLong();

        if (pos == null) {
            for (var player : targets) {
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundPlayMusicPacket(url, category, false, 0, 0, 0, 0, volume, pitch, seed)
                );
                ++count;
            }
        } else {
            for (var player : targets) {
                double dx = pos.x - player.getX();
                double dy = pos.y - player.getY();
                double dz = pos.z - player.getZ();
                double sqrDistance = dx * dx + dy * dy + dz * dz;
                Vec3 vec3 = pos;
                float realVolume = volume;
                if (sqrDistance > sqrRange) {
                    if (pMinVolume <= 0) {
                        continue;
                    }

                    double d5 = Math.sqrt(sqrDistance);
                    vec3 = new Vec3(player.getX() + dx / d5 * 2.0D, player.getY() + dy / d5 * 2.0D, player.getZ() + dz / d5 * 2.0D);
                    realVolume = pMinVolume;
                }

                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundPlayMusicPacket(url, category, true, vec3.x(), vec3.y(), vec3.z(), range, realVolume, pitch, seed)
                );
                ++count;
            }
        }

        if (count == 0) {
            throw ERROR_TOO_FAR.create();
        } else {
            if (targets.size() == 1) {
                source.sendSuccess(() -> {
                    return Component.translatable("commands.playsound.success.single", url, targets.iterator().next().getDisplayName());
                }, true);
            } else {
                source.sendSuccess(() -> {
                    return Component.translatable("commands.playsound.success.multiple", url, targets.size());
                }, true);
            }

            return count;
        }
    }
}
