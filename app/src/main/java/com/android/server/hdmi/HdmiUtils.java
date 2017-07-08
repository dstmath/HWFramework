package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.display.RampAnimator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class HdmiUtils {
    private static final int[] ADDRESS_TO_TYPE = null;
    private static final String[] DEFAULT_NAMES = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.hdmi.HdmiUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.hdmi.HdmiUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.hdmi.HdmiUtils.<clinit>():void");
    }

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
        return ((data[0] & RampAnimator.DEFAULT_MAX_BRIGHTNESS) << 8) | (data[1] & RampAnimator.DEFAULT_MAX_BRIGHTNESS);
    }

    static int twoBytesToInt(byte[] data, int offset) {
        return ((data[offset] & RampAnimator.DEFAULT_MAX_BRIGHTNESS) << 8) | (data[offset + 1] & RampAnimator.DEFAULT_MAX_BRIGHTNESS);
    }

    static int threeBytesToInt(byte[] data) {
        return (((data[0] & RampAnimator.DEFAULT_MAX_BRIGHTNESS) << 16) | ((data[1] & RampAnimator.DEFAULT_MAX_BRIGHTNESS) << 8)) | (data[2] & RampAnimator.DEFAULT_MAX_BRIGHTNESS);
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

    static int languageToInt(String language) {
        String normalized = language.toLowerCase();
        return (((normalized.charAt(0) & RampAnimator.DEFAULT_MAX_BRIGHTNESS) << 16) | ((normalized.charAt(1) & RampAnimator.DEFAULT_MAX_BRIGHTNESS) << 8)) | (normalized.charAt(2) & RampAnimator.DEFAULT_MAX_BRIGHTNESS);
    }
}
