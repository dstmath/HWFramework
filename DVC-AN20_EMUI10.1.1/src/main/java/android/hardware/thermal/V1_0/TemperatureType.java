package android.hardware.thermal.V1_0;

import android.bluetooth.BluetoothHeadset;
import com.android.internal.telephony.IccCardConstants;
import java.util.ArrayList;

public final class TemperatureType {
    public static final int BATTERY = 2;
    public static final int CPU = 0;
    public static final int GPU = 1;
    public static final int SKIN = 3;
    public static final int UNKNOWN = -1;

    public static final String toString(int o) {
        if (o == -1) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        if (o == 0) {
            return "CPU";
        }
        if (o == 1) {
            return "GPU";
        }
        if (o == 2) {
            return BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_XEVENT_BATTERY_LEVEL;
        }
        if (o == 3) {
            return "SKIN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & -1) == -1) {
            list.add(IccCardConstants.INTENT_VALUE_ICC_UNKNOWN);
            flipped = 0 | -1;
        }
        list.add("CPU");
        if ((o & 1) == 1) {
            list.add("GPU");
            flipped |= 1;
        }
        if ((o & 2) == 2) {
            list.add(BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_XEVENT_BATTERY_LEVEL);
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("SKIN");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
