package openmods.codecs.adapters;

import openmods.codecs.Log;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.frame.Frame;
import org.kc7bfi.jflac.util.ByteData;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class CodecFLAC implements ICodec {

    private boolean initialized;
    private boolean streamClosed;

    private ByteData buffer;
    private FLACDecoder decoder;
    private AudioFormat audioFormat;

    @Override
    public void reverseByteOrder(boolean b) {}

    @Override
    public boolean initialize(URL url) {
        try {
            final URLConnection conn = url.openConnection();
            conn.connect();

            decoder = new FLACDecoder(conn.getInputStream());
            decoder.readStreamInfo();
            initialized = true;

            audioFormat = new AudioFormat(decoder.getStreamInfo().getSampleRate(), decoder.getStreamInfo().getBitsPerSample(), decoder.getStreamInfo().getChannels(), true, false);
            updateBuffer();
            return true;
        } catch (Throwable t) {
            Log.warn(t, "Failed to initalize codec for url '%s'", url);
        }

        return false;
    }

    private boolean updateBuffer() throws Exception {
        Frame frame = decoder.readNextFrame();
        if (decoder.isEOF() || frame == null) {
            streamClosed = true;
            return false;
        } else {
            buffer = decoder.decodeFrame(frame, null);
            return true;
        }
    }

    @Override
    public boolean initialized() {
        return initialized;
    }

    @Override
    public SoundBuffer read() {
        if (!initialized || streamClosed)
            return null;

        final int limit = SoundSystemConfig.getStreamingBufferSize();
        ByteArrayOutputStream output = new ByteArrayOutputStream(limit);

        try {
            do {
                output.write(buffer.getData(), 0, buffer.getLen());
                if (!updateBuffer())
                    break;
            } while (!streamClosed && output.size() < limit);
        } catch (Throwable t) {
            Log.warn(t, "Error in stream decoding, aborting");
            streamClosed = true;
        }

        return new SoundBuffer(output.toByteArray(), audioFormat);
    }

    @Override
    public SoundBuffer readAll() {
        if (!initialized || streamClosed)
            return null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            do {
                output.write(buffer.getData(), 0, buffer.getLen());
                if (!updateBuffer())
                    break;
            } while (!streamClosed);
        } catch (Throwable t) {
            Log.warn(t, "Error in stream decoding, aborting");
            streamClosed = true;
        }

        return new SoundBuffer(output.toByteArray(), audioFormat);
    }

    @Override
    public boolean endOfStream() {
        return streamClosed;
    }

    @Override
    public void cleanup() {
        streamClosed = true;
        initialized = false;
        decoder = null;
    }

    @Override
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

}
