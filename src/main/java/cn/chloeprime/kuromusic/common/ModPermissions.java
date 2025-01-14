package cn.chloeprime.kuromusic.common;

import cn.chloeprime.kuromusic.KuroMusic;
import cn.chloeprime.kuroutils.PermissionUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

@EventBusSubscriber
public class ModPermissions {
    public static final PermissionNode<Boolean> PLAY_MUSIC = PermissionUtils.createSimple(KuroMusic.loc("music.play"));
    public static final PermissionNode<Boolean> SET_BGM = PermissionUtils.createSimple(KuroMusic.loc("background_music.set"));
    public static final PermissionNode<Boolean> STOP_SELF_BGM = PermissionUtils.createSimpleForEveryone(KuroMusic.loc("background_music.stop_self"));

    @SubscribeEvent
    public static void onRegisterPermissions(PermissionGatherEvent.Nodes event) {
        event.addNodes(PLAY_MUSIC);
        event.addNodes(SET_BGM);
        event.addNodes(STOP_SELF_BGM);
    }
}
