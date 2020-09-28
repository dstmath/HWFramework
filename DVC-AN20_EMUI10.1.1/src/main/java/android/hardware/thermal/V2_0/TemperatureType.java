package android.hardware.thermal.V2_0;

import android.bluetooth.BluetoothHeadset;
import com.android.internal.telephony.IccCardConstants;
import java.util.ArrayList;

public final class TemperatureType {
    public static final int BATTERY = 2;
    public static final int BCL_CURRENT = 7;
    public static final int BCL_PERCENTAGE = 8;
    public static final int BCL_VOLTAGE = 6;
    public static final int CPU = 0;
    public static final int GPU = 1;
    public static final int NPU = 9;
    public static final int POWER_AMPLIFIER = 5;
    public static final int SKIN = 3;
    public static final int UNKNOWN = -1;
    public static final int USB_PORT = 4;

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
        if (o == 4) {
            return "USB_PORT";
        }
        if (o == 5) {
            return "POWER_AMPLIFIER";
        }
        if (o == 6) {
            return "BCL_VOLTAGE";
        }
        if (o == 7) {
            return "BCL_CURRENT";
        }
        if (o == 8) {
            return "BCL_PERCENTAGE";
        }
        if (o == 9) {
            return "NPU";
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
        if ((o & 4) == 4) {
            list.add("USB_PORT");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("POWER_AMPLIFIER");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("BCL_VOLTAGE");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("BCL_CURRENT");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("BCL_PERCENTAGE");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("NPU");
            flipped |= 9;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
