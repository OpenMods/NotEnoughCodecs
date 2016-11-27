package openmods.codecs;

// Proper class will be selected during build
public class ModSettings {

    public static class Default {
        public static final String ID = "NotEnoughCodecs";
        public static final String NAME = "NotEnoughCodecs";
        public static final String MC_VERSIONS = "";
    }

    public class V1 {
        public static final String ID = "NotEnoughCodecs";
        public static final String NAME = "NotEnoughCodecs";
        public static final String MC_VERSIONS = "[1.8,1.11)";
    }

    // Change: MC 1.11 requires lowercase modid.
    // Including 1.10.2 in range so there is version that does not produce warning
    public class V2 {
        public static final String ID = "notenoughcodecs";
        public static final String NAME = "NotEnoughCodecs";
        public static final String MC_VERSIONS = "[1.10.2,1.12)";
    }

}
