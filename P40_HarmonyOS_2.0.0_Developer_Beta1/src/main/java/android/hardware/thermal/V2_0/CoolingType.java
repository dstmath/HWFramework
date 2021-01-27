package android.hardware.thermal.V2_0;

import android.bluetooth.BluetoothHeadset;
import java.util.ArrayList;

public final class CoolingType {
    public static final int BATTERY = 1;
    public static final int COMPONENT = 6;
    public static final int CPU = 2;
    public static final int FAN = 0;
    public static final int GPU = 3;
    public static final int MODEM = 4;
    public static final int NPU = 5;

    public static final String toString(int o) {
        if (o == 0) {
            return "FAN";
        }
        if (o == 1) {
            return BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_XEVENT_BATTERY_LEVEL;
        }
        if (o == 2) {
            return "CPU";
        }
        if (o == 3) {
            return "GPU";
        }
        if (o == 4) {
            return "MODEM";
        }
        if (o == 5) {
            return "NPU";
        }
        if (o == 6) {
            return "COMPONENT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("FAN");
        if ((o & 1) == 1) {
            list.add(BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_XEVENT_BATTERY_LEVEL);
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("CPU");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("GPU");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("MODEM");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("NPU");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("COMPONENT");
            flipped |= 6;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
