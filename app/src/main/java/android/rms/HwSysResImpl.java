package android.rms;

import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.rms.resource.ActivityResource;
import android.rms.resource.AlarmResource;
import android.rms.resource.AppOpsResource;
import android.rms.resource.AppResource;
import android.rms.resource.AppServiceResource;
import android.rms.resource.BroadcastResource;
import android.rms.resource.ContentObserverResource;
import android.rms.resource.CursorResource;
import android.rms.resource.NotificationResource;
import android.rms.resource.OrderedBroadcastObserveResource;
import android.rms.resource.PidsResource;
import android.rms.resource.ProviderResource;
import android.rms.resource.ReceiverResource;
import android.telephony.HwVSimManager;
import android.util.Log;
import com.huawei.chrfile.client.NcMetricConstant;
import huawei.android.app.admin.HwDeviceAdminInfo;
import huawei.android.telephony.wrapper.HuaweiTelephonyManagerWrapper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class HwSysResImpl implements HwSysResource {
    private static final boolean DEBUG = false;
    private static final String TAG = "RMS.HwSysResImpl";
    private static boolean enableIaware;
    private static boolean enableRms;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.HwSysResImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.HwSysResImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.HwSysResImpl.<clinit>():void");
    }

    public static HwSysResource getResource(int resourceType) {
        if (enableRms) {
            switch (resourceType) {
                case HuaweiTelephonyManagerWrapper.SINGLE_MODE_SIM_CARD /*10*/:
                    return NotificationResource.getInstance();
                case HwVSimManager.NETWORK_TYPE_EVDO_B /*12*/:
                    return ReceiverResource.getInstance();
                case HwVSimManager.NETWORK_TYPE_LTE /*13*/:
                    return AlarmResource.getInstance();
                case NcMetricConstant.GPS_METRIC_ID /*14*/:
                    return AppOpsResource.getInstance();
                case NcMetricConstant.WIFI_METRIC_ID /*15*/:
                    return ProviderResource.getInstance();
                case PduHeaders.MMS_VERSION_1_0 /*16*/:
                    return PidsResource.getInstance();
                case HwVSimManager.NETWORK_TYPE_TDS /*17*/:
                    return CursorResource.getInstance();
                case PduHeaders.MMS_VERSION_1_2 /*18*/:
                    return AppServiceResource.getInstance();
                case PduHeaders.MMS_VERSION_1_3 /*19*/:
                    return AppResource.getInstance();
                case 35:
                    return ContentObserverResource.getInstance();
                case 36:
                    return ActivityResource.getInstance();
            }
        }
        if (enableIaware) {
            switch (resourceType) {
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_APN /*11*/:
                    return BroadcastResource.getInstance();
                case 37:
                    return OrderedBroadcastObserveResource.getInstance();
            }
        }
        return null;
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        return 1;
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        return 1;
    }

    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        return 1;
    }

    public int queryPkgPolicy(int type, int value, String key) {
        return 0;
    }

    public void release(int callingUid, String pkg, int processTpye) {
    }

    public void clear(int callingUid, String pkg, int processTpye) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw) {
    }

    public Bundle query() {
        return null;
    }

    private static boolean isUidSystem(int uid, String pkg) {
        int appid = UserHandle.getAppId(uid);
        return (appid == 1000 || appid == 1001 || uid == 0) ? true : "android".equals(pkg);
    }

    private int isHuaweiApp(String pkg) {
        return pkg.contains("huawei") ? 1 : 0;
    }

    public int getTypeId(int callingUid, String pkg, int processTpye) {
        int typeID = processTpye;
        if (-1 != processTpye) {
            return typeID;
        }
        if (isUidSystem(callingUid, pkg)) {
            return 2;
        }
        if (pkg != null) {
            return isHuaweiApp(pkg);
        }
        return 0;
    }

    public long getResourceId(int callingUid, String pkg, int processTpye) {
        int uid;
        int typeID = getTypeId(callingUid, pkg, processTpye);
        if (3 == processTpye) {
            uid = -1;
        } else {
            uid = callingUid;
        }
        return (((long) typeID) << 32) + ((long) uid);
    }

    protected boolean registerResourceCallback(IUpdateWhiteListCallback updateCallback) {
        return HwSysResManager.getInstance().registerResourceCallback(updateCallback);
    }

    protected ArrayList<String> getResWhiteList(int resouceTpye, int type) {
        String[] whiteList = null;
        ArrayList<String> mList = new ArrayList();
        String configWhiteList = HwSysResManager.getInstance().getWhiteList(resouceTpye, type);
        if (configWhiteList != null) {
            whiteList = configWhiteList.split(";");
        }
        if (whiteList != null) {
            int i = 0;
            while (i < whiteList.length) {
                if (!(mList.contains(whiteList[i]) || whiteList[i].isEmpty())) {
                    mList.add(whiteList[i]);
                    if (Log.HWLog) {
                        Log.d(TAG, "getResWhiteList put the name into the list  type:" + resouceTpye + ", name:" + whiteList[i] + " , num:" + i);
                    }
                }
                i++;
            }
        }
        return mList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean isInWhiteList(String pkg, ArrayList<String> whiteList) {
        if (pkg == null || whiteList == null || !whiteList.contains(pkg)) {
            return DEBUG;
        }
        return true;
    }
}
