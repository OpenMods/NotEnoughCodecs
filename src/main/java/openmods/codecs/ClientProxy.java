package openmods.codecs;

import openmods.codecs.adapters.CodecADTS;
import openmods.codecs.adapters.CodecMP3;
import openmods.codecs.adapters.CodecMP4;
import paulscode.sound.ICodec;
import paulscode.sound.SoundSystemConfig;

public class ClientProxy implements IProxy {
    private static void registerCodec(Class<? extends ICodec> cls, String ext, String... mimeTypes) {
        try {
            SoundSystemConfig.setCodec(ext, cls);
            for (String type : mimeTypes)
                NotEnoughCodecs.KNOWN_MIME_TYPES.put(type, ext);
        } catch (Throwable t) {
            Log.warn(t, "Can't register codec for extension %s", ext);
        }
    }

    @Override
    public void registerCodecs() {
        registerCodec(CodecMP3.class, "MP3", "audio/mpeg", "audio/x-mpeg", "audio/mpeg3", "audio/x-mpeg3");
        registerCodec(CodecADTS.class, "AAC", "audio/aac", "audio/aacp", "audio/mp4", "audio/mpeg4-generic");
        registerCodec(CodecMP4.class, "MP4", "audio/mp4", "audio/mpeg4-generic");
        registerCodec(CodecMP4.class, "M4A");
    }
}
