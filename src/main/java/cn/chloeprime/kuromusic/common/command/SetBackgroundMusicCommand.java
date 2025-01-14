package cn.chloeprime.kuromusic.common.command;

import cn.chloeprime.kuromusic.KuroMusic;
import cn.chloeprime.kuromusic.common.ModPermissions;
import cn.chloeprime.kuromusic.common.network.ClientboundSetBackgroundMusicPacket;
import cn.chloeprime.kuroutils.PermissionUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class SetBackgroundMusicCommand {
    public static final long COMMAND_MUSIC_PRIORITY = Integer.MAX_VALUE;
    public static final String DEV_NULL = "/dev/null";
    public static final String[] URL_EXAMPLES = Stream.concat(
            Arrays.stream(PlayMusicCommand.URL_EXAMPLES),
            Stream.of('"' + DEV_NULL + '"')
    ).toArray(String[]::new);

    public static final SuggestionProvider<CommandSourceStack> URL_EXAMPLE_SUGGESTIONS = SuggestionProviders.register(
            KuroMusic.loc("example_urls_with_dev_null"),
            (context, builder) -> SharedSuggestionProvider.suggest(URL_EXAMPLES, builder)
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var command = Commands.literal("setbackgroundmusic").requires(PermissionUtils.checker(ModPermissions.SET_BGM))
                .then(Commands.argument("url", StringArgumentType.string()).suggests(URL_EXAMPLE_SUGGESTIONS)
                        .then(Commands.argument("targets", EntityArgument.players()).executes(
                                        context -> playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), 1, 1))
                                .then(Commands.argument("volume", FloatArgumentType.floatArg(0)).executes(
                                                context -> playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), FloatArgumentType.getFloat(context, "volume"), 1))
                                        .then(Commands.argument("pitch", FloatArgumentType.floatArg(0, 2)).executes(
                                                context -> playMusic(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "url"), FloatArgumentType.getFloat(context, "volume"), FloatArgumentType.getFloat(context, "pitch")))
                                        ))));
        dispatcher.register(command);
    }

    public static int playMusic(CommandSourceStack source, Collection<ServerPlayer> targets, String url, float volume, float pitch) {
        var count = 0;
        var packet = new ClientboundSetBackgroundMusicPacket(url, COMMAND_MUSIC_PRIORITY, volume, pitch);
        for (var player : targets) {
            PacketDistributor.sendToPlayer(player, packet);
            ++count;
        }
        if (targets.size() == 1) {
            source.sendSuccess(() -> {
                return Component.translatable("kuromusic.commands.setbackgroundmusic.success.single", url, targets.iterator().next().getDisplayName());
            }, true);
        } else {
            source.sendSuccess(() -> {
                return Component.translatable("kuromusic.commands.setbackgroundmusic.success.multiple", url, targets.size());
            }, true);
        }
        return count;
    }
}
