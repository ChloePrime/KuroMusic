package cn.chloeprime.kuromusic.client.audio;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;

import java.io.ByteArrayInputStream;
import java.util.Objects;

public class ExternalMusic extends Music {
    public ExternalMusic(Holder<SoundEvent> event, byte[] data) {
        this(event, data, 0, getLengthSeconds(data) > 0);
    }

    public SimpleSoundInstance createInstance() {
        return isValid
                ? ExternalSound.forExternalMusic(new ByteArrayInputStream(data), getEvent().value())
                : SimpleSoundInstance.forMusic(getEvent().value());
    }

    private final byte[] data;
    private final boolean isValid;

    private ExternalMusic(Holder<SoundEvent> event, byte[] data, int delay, boolean valid) {
        super(event, delay, delay, valid);
        this.isValid = valid;
        this.data = valid ? Objects.requireNonNull(data) : null;
    }

    public static double getLengthSeconds(byte[] data) {
        return ExternalMusicSupport.getStreamLength(data);
    }
}
