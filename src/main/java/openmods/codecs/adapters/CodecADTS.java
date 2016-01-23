package openmods.codecs.adapters;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;

import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import openmods.codecs.Log;

public class CodecADTS extends CodecAacBase {

    private ADTSDemultiplexer adts;

    @Override
    protected void initializeStream(URL url, DataInputStream in) throws IOException {
        adts = new ADTSDemultiplexer(in);
    }

    @Override
    protected byte[] getDecoderSpecificInfo() {
        return adts.getDecoderSpecificInfo();
    }

    @Override
    protected void validateFormat(URL url) {
        AudioFormat headerFormat = new AudioFormat(adts.getSampleFrequency(), 16, adts.getChannelCount(), true, true);
        if (!headerFormat.matches(format)) {
            Log.warn("Stream %s header declared different format than buffer", url);
        }
    }

    @Override
    protected byte[] readNextFrame() throws IOException {
        return adts.readNextFrame();
    }

    @Override
    protected void cleanupSpecific() {
        adts = null;
    }

}
