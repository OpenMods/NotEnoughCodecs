package openmods.codecs;

import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.google.common.collect.Maps;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "NotEnoughCodecs", name = "NotEnoughCodecs", version = "@VERSION@")
public class NotEnoughCodecs {

    public static final Map<String, String> KNOWN_MIME_TYPES = Maps.newHashMap();

    @SidedProxy(clientSide = "openmods.codecs.ClientProxy", serverSide = "openmods.codecs.ServerProxy")
    public static IProxy proxy;

    public static Logger logger;

    @SideOnly(Side.CLIENT)
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        proxy.registerCodecs();
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
