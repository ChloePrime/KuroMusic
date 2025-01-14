package cn.chloeprime.kuromusic.common.command;

import cn.chloeprime.kuromusic.common.ModPermissions;
import cn.chloeprime.kuromusic.common.network.ClientboundStopSelfBackgroundMusicPacket;
import cn.chloeprime.kuroutils.PermissionUtils;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;

public class StopSelfBackgroundMusicCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var command = Commands.literal("stopbackgroundmusic")
                .requires(PermissionUtils.checker(ModPermissions.STOP_SELF_BGM))
                .executes(context -> stopSelfBackgroundMusic(context.getSource(), ImmutableList.of(context.getSource().getEntityOrException())));

        dispatcher.register(command);
    }

    public static int stopSelfBackgroundMusic(CommandSourceStack source, Collection<? extends Entity> targets) {
        var packet = new ClientboundStopSelfBackgroundMusicPacket();
        int count = 0;

        for (Entity target : targets) {
            if (!(target instanceof ServerPlayer player)) {
                continue;
            }
            ++count;
            PacketDistributor.sendToPlayer(player, packet);
        }

        if (count > 0) {
            source.sendSuccess(() -> Component.translatable("kuromusic.commands.stopbackgroundmusic.success"), true);
        }
        return count;
    }
}
