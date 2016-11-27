package openmods.codecs;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = ModSettings.Default.ID, name = ModSettings.Default.NAME, version = "@VERSION@", acceptedMinecraftVersions = ModSettings.Default.MC_VERSIONS, acceptableRemoteVersions = "*")
public class NotEnoughCodecs {

    public static final Map<String, String> KNOWN_MIME_TYPES = Maps.newHashMap();

    @SidedProxy(clientSide = "openmods.codecs.ClientProxy", serverSide = "openmods.codecs.ServerProxy")
    public static IProxy proxy;

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
