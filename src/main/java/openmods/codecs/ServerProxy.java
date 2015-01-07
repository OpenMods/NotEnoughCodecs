package openmods.codecs;

public class ServerProxy implements IProxy {
    @Override
    public void registerCodecs() {
        Log.warn("This mod should only be used on client side");
    }
}
