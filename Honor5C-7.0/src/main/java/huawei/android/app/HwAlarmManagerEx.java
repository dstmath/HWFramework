package huawei.android.app;

import android.os.RemoteException;
import android.os.ServiceManager;
import huawei.android.app.IHwAlarmManagerEx.Stub;
import huawei.android.content.HwContextEx;
import java.util.List;

public class HwAlarmManagerEx {
    private static final String TAG = "HwAlarmManagerEx";
    private static volatile HwAlarmManagerEx mInstance;
    IHwAlarmManagerEx mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.app.HwAlarmManagerEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.app.HwAlarmManagerEx.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.app.HwAlarmManagerEx.<clinit>():void");
    }

    public static synchronized HwAlarmManagerEx getInstance() {
        HwAlarmManagerEx hwAlarmManagerEx;
        synchronized (HwAlarmManagerEx.class) {
            if (mInstance == null) {
                mInstance = new HwAlarmManagerEx();
            }
            hwAlarmManagerEx = mInstance;
        }
        return hwAlarmManagerEx;
    }

    private HwAlarmManagerEx() {
        this.mService = null;
        this.mService = Stub.asInterface(ServiceManager.getService(HwContextEx.HW_ALARM_SERVICE));
    }

    public void setAlarmsPending(List<String> pkgList, List<String> actionList, boolean pending, int type) {
        try {
            this.mService.setAlarmsPending(pkgList, actionList, pending, type);
        } catch (RemoteException e) {
        }
    }

    public void removeAllPendingAlarms() {
        try {
            this.mService.removeAllPendingAlarms();
        } catch (RemoteException e) {
        }
    }

    public void setAlarmsAdjust(List<String> pkgList, List<String> actionList, boolean adjust, int type, long interval, int mode) {
        try {
            this.mService.setAlarmsAdjust(pkgList, actionList, adjust, type, interval, mode);
        } catch (RemoteException e) {
        }
    }

    public void removeAllAdjustAlarms() {
        try {
            this.mService.removeAllAdjustAlarms();
        } catch (RemoteException e) {
        }
    }
}
