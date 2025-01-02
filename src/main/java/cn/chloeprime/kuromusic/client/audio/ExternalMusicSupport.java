package cn.chloeprime.kuromusic.client.audio;

import cn.chloeprime.kuromusic.KuroMusic;
import cn.chloeprime.kuromusic.util.ObjectPool;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.OptionalDouble;

class ExternalMusicSupport {
    static double getStreamLength(byte[] data) {
        try {
            var fast = getStreamLengthFastPath(data);
            if (fast.isPresent()) {
                return fast.getAsDouble();
            }
            return getStreamLengthSlowAccuratePath(data);
        } catch (UnsupportedAudioFileException | IOException ex) {
            KuroMusic.LOGGER.warn("Unable to obtain length of background music", ex);
            return -1;
        }
    }

    static OptionalDouble getStreamLengthFastPath(byte[] data) throws UnsupportedAudioFileException, IOException {
        var properties = AudioSystem.getAudioFileFormat(new ByteArrayInputStream(data)).properties();
        if (properties.get("duration") instanceof Number duration) {
            return OptionalDouble.of(duration.doubleValue() / 1000);
        }
        if (properties.get("mp3.bitrate.nominal.bps") instanceof Number bps) {
            return OptionalDouble.of(data.length * 8.0 / bps.doubleValue());
        }
        return OptionalDouble.empty();
    }

    static double getStreamLengthSlowAccuratePath(byte[] data) throws UnsupportedAudioFileException, IOException {
        var stream = ExternalAudioStream.convert(AudioSystem.getAudioInputStream(new ByteArrayInputStream(data)), false);
        return (double) countStreamLength(stream) / stream.getFormat().getFrameSize() / stream.getFormat().getFrameRate();
    }

    private static long countStreamLength(InputStream stream) throws IOException {
        var count = 0L;
        var buffer = LENGTH_TEST_BUFFER.poll();
        try {
            while (true) {
                var read = stream.read(buffer);
                if (read < 0) {
                    break;
                }
                count += read;
            }
            return count;
        } finally {
            LENGTH_TEST_BUFFER.offer(buffer);
        }
    }

    private static final int LENGTH_TEST_BUFFER_SIZE = 4096;
    private static final ObjectPool<byte[]> LENGTH_TEST_BUFFER = new ObjectPool<>(() -> new byte[LENGTH_TEST_BUFFER_SIZE]);
}
