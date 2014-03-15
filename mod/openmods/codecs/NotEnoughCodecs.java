package openmods.codecs;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import paulscode.sound.ICodec;
import paulscode.sound.SoundSystemConfig;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "NotEnoughCodecs", name = "NotEnoughCodecs", version = "@VERSION@")
public class NotEnoughCodecs {

    public static Logger logger;

    private final Map<String, String> KNOWN_MIME_TYPES = Maps.newHashMap();

    private static class CodecMeta {
        public final Class<? extends ICodec> cls;
        public final String ext;
        public final Set<String> mimeTypes;

        public CodecMeta(Class<? extends ICodec> cls, String ext, String... mimeTypes) {
            this.cls = cls;
            this.ext = ext;
            this.mimeTypes = ImmutableSet.copyOf(mimeTypes);
        }
    }

    private static final List<CodecMeta> CODECS = ImmutableList.of(
            new CodecMeta(CodecMp3.class, "MP3", "audio/mpeg", "audio/x-mpeg", "audio/mpeg3", "audio/x-mpeg3")
            );

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        if (Side.CLIENT.equals(evt.getSide()))
            registerCodecs();
        else
            Log.warn("This mod should only be used on client side");
    }

    private void registerCodecs() {
        for (CodecMeta codec : CODECS) {
            try {
                SoundSystemConfig.setCodec(codec.ext, codec.cls);
                for (String type : codec.mimeTypes)
                    KNOWN_MIME_TYPES.put(type, codec.ext);
            } catch (Throwable t) {
                Log.warn(t, "Can't register codec for extension %s", codec.ext);
            }
        }
    }

    @EventHandler
    public void handleIMC(IMCEvent evt) {
        NBTTagCompound extensions = new NBTTagCompound();
        NBTTagList data = new NBTTagList();
        for (Map.Entry<String, String> e : KNOWN_MIME_TYPES.entrySet()) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("mime", e.getKey());
            entry.setString("ext", e.getValue());
            data.appendTag(entry);
        }
        extensions.setTag("data", data);

        for (IMCMessage msg : evt.getMessages())
            if ("listCodecs".equalsIgnoreCase(msg.key)) {
                String sender = msg.getSender();
                Log.info("Received codec list request from %s, responding", sender);
                FMLInterModComms.sendMessage(sender, "knownCodecs", extensions);
            }
    }
}
