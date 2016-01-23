package openmods.codecs.adapters;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;
import openmods.codecs.Log;

public class CodecMP4 extends CodecAacBase {

    private Track track;

    @Override
    protected void initializeStream(URL url, DataInputStream in) throws IOException {
        MP4Container container = new MP4Container(in);
        Movie movie = container.getMovie();
        List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
        if (tracks.isEmpty()) {
            Log.warn("No sound tracks in %s", url);
            initialized = false;
        } else {
            this.track = tracks.get(0);
        }
    }

    @Override
    protected byte[] getDecoderSpecificInfo() {
        return track.getDecoderSpecificInfo();
    }

    @Override
    protected byte[] readNextFrame() throws IOException {
        return track.readNextFrame().getData();
    }

    @Override
    protected void cleanupSpecific() {
        track = null;
    }

    @Override
    protected void validateFormat(URL url) {
        // NO-OP
    }

}
