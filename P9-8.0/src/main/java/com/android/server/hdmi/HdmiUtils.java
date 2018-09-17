package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.util.Slog;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class HdmiUtils {
    private static final int[] ADDRESS_TO_TYPE = new int[]{0, 1, 1, 3, 4, 5, 3, 3, 4, 1, 3, 4, 2, 2, 0};
    private static final String[] DEFAULT_NAMES = new String[]{"TV", "Recorder_1", "Recorder_2", "Tuner_1", "Playback_1", "AudioSystem", "Tuner_2", "Tuner_3", "Playback_2", "Recorder_3", "Tuner_4", "Playback_3", "Reserved_1", "Reserved_2", "Secondary_TV"};

    private HdmiUtils() {
    }

    static boolean isValidAddress(int address) {
        return address >= 0 && address <= 14;
    }

    static int getTypeFromAddress(int address) {
        if (isValidAddress(address)) {
            return ADDRESS_TO_TYPE[address];
        }
        return -1;
    }

    static String getDefaultDeviceName(int address) {
        if (isValidAddress(address)) {
            return DEFAULT_NAMES[address];
        }
        return "";
    }

    static void verifyAddressType(int logicalAddress, int deviceType) {
        int actualDeviceType = getTypeFromAddress(logicalAddress);
        if (actualDeviceType != deviceType) {
            throw new IllegalArgumentException("Device type missmatch:[Expected:" + deviceType + ", Actual:" + actualDeviceType);
        }
    }

    static boolean checkCommandSource(HdmiCecMessage cmd, int expectedAddress, String tag) {
        int src = cmd.getSource();
        if (src == expectedAddress) {
            return true;
        }
        Slog.w(tag, "Invalid source [Expected:" + expectedAddress + ", Actual:" + src + "]");
        return false;
    }

    static boolean parseCommandParamSystemAudioStatus(HdmiCecMessage cmd) {
        return cmd.getParams()[0] == (byte) 1;
    }

    static List<Integer> asImmutableList(int[] is) {
        ArrayList<Integer> list = new ArrayList(is.length);
        for (int type : is) {
            list.add(Integer.valueOf(type));
        }
        return Collections.unmodifiableList(list);
    }

    static int twoBytesToInt(byte[] data) {
        return ((data[0] & 255) << 8) | (data[1] & 255);
    }

    static int twoBytesToInt(byte[] data, int offset) {
        return ((data[offset] & 255) << 8) | (data[offset + 1] & 255);
    }

    static int threeBytesToInt(byte[] data) {
        return (((data[0] & 255) << 16) | ((data[1] & 255) << 8)) | (data[2] & 255);
    }

    static <T> List<T> sparseArrayToList(SparseArray<T> array) {
        ArrayList<T> list = new ArrayList();
        for (int i = 0; i < array.size(); i++) {
            list.add(array.valueAt(i));
        }
        return list;
    }

    static <T> List<T> mergeToUnmodifiableList(List<T> a, List<T> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return Collections.emptyList();
        }
        if (a.isEmpty()) {
            return Collections.unmodifiableList(b);
        }
        if (b.isEmpty()) {
            return Collections.unmodifiableList(a);
        }
        List<T> newList = new ArrayList();
        newList.addAll(a);
        newList.addAll(b);
        return Collections.unmodifiableList(newList);
    }

    static boolean isAffectingActiveRoutingPath(int activePath, int newPath) {
        for (int i = 0; i <= 12; i += 4) {
            if (((newPath >> i) & 15) != 0) {
                newPath &= 65520 << i;
                break;
            }
        }
        if (newPath == 0) {
            return true;
        }
        return isInActiveRoutingPath(activePath, newPath);
    }

    static boolean isInActiveRoutingPath(int activePath, int newPath) {
        int i = 12;
        while (i >= 0) {
            int nibbleActive = (activePath >> i) & 15;
            if (nibbleActive != 0) {
                int nibbleNew = (newPath >> i) & 15;
                if (nibbleNew == 0) {
                    break;
                } else if (nibbleActive != nibbleNew) {
                    return false;
                } else {
                    i -= 4;
                }
            } else {
                break;
            }
        }
        return true;
    }

    static HdmiDeviceInfo cloneHdmiDeviceInfo(HdmiDeviceInfo info, int newPowerStatus) {
        return new HdmiDeviceInfo(info.getLogicalAddress(), info.getPhysicalAddress(), info.getPortId(), info.getDeviceType(), info.getVendorId(), info.getDisplayName(), newPowerStatus);
    }
}
