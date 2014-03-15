package openmods.codecs;

import com.google.common.base.Preconditions;

public class Utils {

    public static void convertEndianness(byte[] data, int size) {
        if (size == 1) {}
        else if (size == 2) {
            Preconditions.checkArgument(data.length % 2 == 0, "Invalid short buffer size");
            for (int i = 0; i < data.length; i += 2) {
                byte b0 = data[i + 0];
                byte b1 = data[i + 1];
                data[i + 0] = b1;
                data[i + 1] = b0;
            }
        } else if (size == 4) {
            Preconditions.checkArgument(data.length % 4 == 0, "Invalid int buffer size");
            for (int i = 0; i < data.length; i += 4) {
                byte b0 = data[i + 0];
                byte b1 = data[i + 1];
                byte b2 = data[i + 2];
                byte b3 = data[i + 3];
                data[i + 0] = b3;
                data[i + 1] = b2;
                data[i + 2] = b1;
                data[i + 3] = b0;
            }
        }
    }

}
