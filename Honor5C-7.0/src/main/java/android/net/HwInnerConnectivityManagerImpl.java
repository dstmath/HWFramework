package android.net;

import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.internal.telephony.HuaweiTelephonyConfigs;

public class HwInnerConnectivityManagerImpl implements HwInnerConnectivityManager {
    private static final String TAG = "HwInnerConnectivityManagerImpl";
    private static HwInnerConnectivityManagerImpl mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.HwInnerConnectivityManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.HwInnerConnectivityManagerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.HwInnerConnectivityManagerImpl.<clinit>():void");
    }

    public static HwInnerConnectivityManagerImpl getDefault() {
        return mInstance;
    }

    public boolean isHwFeature(String feature) {
        Log.d(TAG, "isHwFeature: for feature = " + feature);
        if (getFeature(feature)[1] != null) {
            return true;
        }
        return false;
    }

    public String[] getFeature(String str) {
        if (str == null) {
            throw new IllegalArgumentException("getFeature() received null string");
        }
        String[] result = new String[2];
        String reqSub = null;
        if (str.equals("enableMMS_sub1")) {
            str = "enableMMS";
            reqSub = String.valueOf(0);
        } else if (str.equals("enableMMS_sub2")) {
            str = "enableMMS";
            reqSub = String.valueOf(1);
        } else if (HuaweiTelephonyConfigs.isChinaTelecom() && str.equals("enableSUPL")) {
            reqSub = String.valueOf(HwTelephonyManagerInner.getDefault().getDefault4GSlotId());
        } else if ("enableHIPRI_sub1".equals(str)) {
            str = "enableHIPRI";
            reqSub = String.valueOf(0);
        } else if ("enableHIPRI_sub2".equals(str)) {
            str = "enableHIPRI";
            reqSub = String.valueOf(1);
        }
        result[0] = str;
        result[1] = reqSub;
        return result;
    }

    public boolean checkHwFeature(String feature, NetworkCapabilities networkCapabilities, int networkType) {
        Log.d(TAG, "startUsingNetworkFeature: for feature = " + feature);
        String[] result = getFeature(feature);
        feature = result[0];
        String reqSubId = result[1];
        if (reqSubId == null) {
            return false;
        }
        Log.d(TAG, "networkCapabilities setNetworkSpecifier reqSubId = " + reqSubId);
        networkCapabilities.setNetworkSpecifier(reqSubId);
        if (isDualCellDataForHipri(networkType, feature, reqSubId)) {
            networkCapabilities.setDualCellData("true");
        }
        return true;
    }

    private boolean isDualCellDataForHipri(int networkType, String feature, String subId) {
        return SystemProperties.getBoolean("ro.hwpp.dual_cell_data", false) && "enableHIPRI".equals(feature) && networkType == 0 && subId != null && ((subId.equals(String.valueOf(0)) || subId.equals(String.valueOf(1))) && !subId.equals(String.valueOf(SubscriptionManager.getDefaultDataSubscriptionId())));
    }
}
