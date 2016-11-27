package openmods.codecs;

import openmods.codecs.adapters.CodecADTS;
import openmods.codecs.adapters.CodecFLAC;
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
            Log.warn(t, "Can't register codec %s for extension %s", cls.getName(), ext);
        }
    }

    private static void registerCodec(String clsName, String ext, String... mimeTypes) {
        try {
            Class<?> cls = Class.forName(clsName);
            Class<? extends ICodec> castedCls = cls.asSubclass(ICodec.class);
            registerCodec(castedCls, ext, mimeTypes);
        } catch (Throwable t) {
            Log.warn(t, "Can't register codec %s for extension %s", clsName, ext);
        }
    }

    @Override
    public void registerCodecs() {
        registerCodec(CodecMP3.class, "MP3", "audio/mpeg", "audio/x-mpeg", "audio/mpeg3", "audio/x-mpeg3");
        registerCodec(CodecADTS.class, "AAC", "audio/aac", "audio/aacp", "audio/mp4", "audio/mpeg4-generic");
        registerCodec(CodecMP4.class, "MP4", "audio/mp4", "audio/mpeg4-generic");
        registerCodec(CodecMP4.class, "M4A");
        registerCodec(CodecFLAC.class, "FLAC", "audio/flac");

        registerCodec("paulscode.sound.codecs.CodecIBXM", "XM", "audio/xm", "audio/x-xm"); // unconfirmed, but whatever
        registerCodec("paulscode.sound.codecs.CodecIBXM", "S3M", "audio/s3m", "audio/x-s3m");
        registerCodec("paulscode.sound.codecs.CodecIBXM", "MOD", "audio/mod", "audio/x-mod");

        registerCodec("paulscode.sound.codecs.CodecWav", "WAV", "audio/wav", "audio/x-wav", "audio/wave");
    }
}
