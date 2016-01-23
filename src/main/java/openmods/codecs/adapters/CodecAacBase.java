package openmods.codecs.adapters;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.sound.sampled.AudioFormat;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import openmods.codecs.Log;
import openmods.codecs.Utils;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;

public abstract class CodecAacBase implements ICodec {

    protected boolean initialized;
    protected AudioFormat format;
    private Decoder decoder;
    private boolean streamClosed;
    private SampleBuffer buf;
    private DataInputStream in;
    private boolean reverseBytes;
    private boolean endianessConflict;
    private int sampleInBytes;

    @Override
    public void reverseByteOrder(boolean b) {
        this.reverseBytes = b;
    }

    @Override
    public boolean initialize(URL url) {
        try {
            final URLConnection conn = url.openConnection();
            conn.connect();

            in = new DataInputStream(conn.getInputStream());
            initializeStream(url, in);
            decoder = new Decoder(getDecoderSpecificInfo());
            buf = new SampleBuffer();
            initialized = true;

            updateBuffer();
            format = new AudioFormat(buf.getSampleRate(), buf.getBitsPerSample(), buf.getChannels(), true, buf.isBigEndian());
            validateFormat(url);

            sampleInBytes = format.getSampleSizeInBits() / 8;
            endianessConflict = format.isBigEndian() && reverseBytes;

            return true;
        } catch (Throwable t) {
            Log.warn(t, "Failed to initalize codec for url '%s'", url);
        }

        return false;
    }

    protected abstract void initializeStream(URL url, DataInputStream in) throws IOException;

    protected abstract byte[] getDecoderSpecificInfo();

    protected abstract void validateFormat(URL url);

    protected abstract byte[] readNextFrame() throws IOException;

    protected abstract void cleanupSpecific();

    private static boolean formatChanged(AudioFormat af, SampleBuffer buf) {
        return af.getSampleRate() != buf.getSampleRate()
                || af.getChannels() != buf.getChannels()
                || af.getSampleSizeInBits() != buf.getBitsPerSample()
                || af.isBigEndian() != buf.isBigEndian();
    }

    @Override
    public boolean initialized() {
        return initialized;
    }

    @Override
    public boolean endOfStream() {
        return streamClosed;
    }

    @Override
    public AudioFormat getAudioFormat() {
        return format;
    }

    private void updateBuffer() throws IOException {
        byte[] b = readNextFrame();
        decoder.decodeFrame(b, buf);
    }

    private boolean appendBytes(ByteArrayOutputStream output) throws IOException {
        byte data[] = buf.getData();

        if (endianessConflict)
            Utils.convertEndianness(data, sampleInBytes);

        output.write(data);

        updateBuffer();

        if (formatChanged(format, buf)) {
            Log.warn("Stream format changed, aborting");
            streamClosed = true;
            return false;
        }

        return true;
    }

    @Override
    public SoundBuffer read() {
        if (!initialized || streamClosed)
            return null;

        final int limit = SoundSystemConfig.getStreamingBufferSize();
        ByteArrayOutputStream output = new ByteArrayOutputStream(limit);

        try {
            while (appendBytes(output) && !streamClosed && output.size() < limit) {}
        } catch (Throwable t) {
            Log.warn(t, "Error in stream decoding, aborting");
            streamClosed = true;
        }

        return new SoundBuffer(output.toByteArray(), format);
    }

    @Override
    public SoundBuffer readAll() {
        if (!initialized || streamClosed)
            return null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            while (appendBytes(output) && !streamClosed) {}
        } catch (Throwable t) {
            Log.warn(t, "Error in stream decoding, aborting");
            streamClosed = true;
        }

        return new SoundBuffer(output.toByteArray(), format);
    }

    @Override
    public void cleanup() {
        streamClosed = true;
        initialized = false;
        cleanupSpecific();
        decoder = null;
        try {
            in.close();
        } catch (IOException e) {
            Log.warn(e, "Can't close stream");
        }
    }
}
