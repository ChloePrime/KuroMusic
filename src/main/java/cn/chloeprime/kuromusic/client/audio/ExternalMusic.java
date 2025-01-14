package cn.chloeprime.kuromusic.client.audio;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;

import java.io.ByteArrayInputStream;
import java.util.Objects;

public class ExternalMusic extends Music {
    public ExternalMusic(Holder<SoundEvent> event, byte[] data, @Deprecated Runnable onStop) {
        this(event, data, 0, getLengthSeconds(data) > 0, onStop);
    }

    public SimpleSoundInstance createInstance() {
        return isValid
                ? ExternalSound.forExternalMusic(new ByteArrayInputStream(data), getEvent().value(), onStopCallback)
                : SimpleSoundInstance.forMusic(getEvent().value());
    }

    private final byte[] data;
    private final boolean isValid;
    private final Runnable onStopCallback;

    private ExternalMusic(Holder<SoundEvent> event, byte[] data, int delay, boolean valid, Runnable onStop) {
        super(event, delay, delay, valid);
        this.isValid = valid;
        this.data = valid ? Objects.requireNonNull(data) : null;
        this.onStopCallback = onStop;
    }

    public static double getLengthSeconds(byte[] data) {
        return ExternalMusicSupport.getStreamLength(data);
    }
}
