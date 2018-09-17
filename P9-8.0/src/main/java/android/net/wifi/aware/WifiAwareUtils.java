package android.net.wifi.aware;

public class WifiAwareUtils {
    public static void validateServiceName(byte[] serviceNameData) throws IllegalArgumentException {
        if (serviceNameData == null) {
            throw new IllegalArgumentException("Invalid service name - null");
        } else if (serviceNameData.length < 1 || serviceNameData.length > 255) {
            throw new IllegalArgumentException("Invalid service name length - must be between 1 and 255 bytes (UTF-8 encoding)");
        } else {
            int index = 0;
            while (index < serviceNameData.length) {
                byte b = serviceNameData[index];
                if ((b & 128) != 0 || ((b >= (byte) 48 && b <= (byte) 57) || ((b >= (byte) 97 && b <= (byte) 122) || ((b >= (byte) 65 && b <= (byte) 90) || b == (byte) 45 || b == (byte) 46)))) {
                    index++;
                } else {
                    throw new IllegalArgumentException("Invalid service name - illegal characters, allowed = (0-9, a-z,A-Z, -, .)");
                }
            }
        }
    }
}
