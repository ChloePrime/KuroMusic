package cn.chloeprime.kuromusic.client.audio;

import cn.chloeprime.kuromusic.client.ClientNetworkHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

@MethodsReturnNonnullByDefault
public class ExternalAudioStream implements AudioStream {
    private final AudioInputStream stream;
    private final int frameSize;
    private final byte[] frame;
    private final Runnable onFinishedHook;

    public ExternalAudioStream(AudioInputStream originalStream) {
        this(originalStream, null, () -> {}, 0);
    }

    public ExternalAudioStream(AudioInputStream originalStream, Runnable onFinished) {
        this(originalStream, null, onFinished, 0);
    }

    public ExternalAudioStream(AudioInputStream originalStream, Vec3 pos) {
        this(originalStream, Objects.requireNonNull(pos), () -> {}, 0);
    }

    public ExternalAudioStream(AudioInputStream originalStream, Vec3 pos, Runnable onFinished) {
        this(originalStream, Objects.requireNonNull(pos), onFinished, 0);
    }

    private ExternalAudioStream(AudioInputStream originalStream, @Nullable Vec3 pos, Runnable onFinishedHook, int ignoredNonsense) {
        this.onFinishedHook = onFinishedHook;
        var oldFormat = originalStream.getFormat();
        var sampleRate = oldFormat.getSampleRate();
        int newFormatBits = 16;
        var newFormatChannels = oldFormat.getChannels();
        var newFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate, newFormatBits, newFormatChannels, 2 * newFormatChannels,
                sampleRate, false);
        var stereoPcmStream = AudioSystem.getAudioInputStream(newFormat, originalStream);
        if (pos == null) {
            this.stream = stereoPcmStream;
        } else {
            var monoFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sampleRate, newFormatBits, 1, 2,
                    sampleRate, false);
            this.stream = AudioSystem.getAudioInputStream(monoFormat, stereoPcmStream);
        }
        this.frameSize = stream.getFormat().getFrameSize();
        this.frame = new byte[frameSize];
    }

    @Override
    public AudioFormat getFormat() {
        return stream.getFormat();
    }

    @Override
    public ByteBuffer read(int length) throws IOException {
        // 创建指定大小的ByteBuffer
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(length);
        int bytesRead = 0, count;
        // 循环读取数据直到达到指定大小或输入流结束
        do {
            // 读取下一部分数据
            count = this.stream.read(frame);
            // 将读取的数据写入ByteBuffer
            if (count != -1) {
                byteBuffer.put(frame);
            }
        } while (count != -1 && (bytesRead += frameSize) < length);
        // 翻转ByteBuffer，准备进行读取操作
        byteBuffer.flip();
        // 返回包含读取数据的ByteBuffer
        return byteBuffer;
    }

    @Override
    public void close() throws IOException {
        try {
            Optional.ofNullable(onFinishedHook).ifPresent(ClientNetworkHandler.MC::execute);
        } finally {
            stream.close();
        }
    }
}
