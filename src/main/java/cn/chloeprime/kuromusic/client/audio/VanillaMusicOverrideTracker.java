package cn.chloeprime.kuromusic.client.audio;

import java.util.LinkedHashSet;
import java.util.Set;

public class VanillaMusicOverrideTracker {
    public static boolean shouldSilenceVanillaMusic() {
        return !REASONS.isEmpty();
    }

    public static void silenceForReason(Object reason) {
        REASONS.add(reason);
    }

    public static void unsilenceForReason(Object reason) {
        REASONS.remove(reason);
    }

    private static final Set<Object> REASONS = new LinkedHashSet<>();
}
