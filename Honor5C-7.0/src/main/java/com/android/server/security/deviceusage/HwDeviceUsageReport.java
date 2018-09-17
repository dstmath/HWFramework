package com.android.server.security.deviceusage;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.wifipro.WifiProCommonDefs;
import huawei.android.security.IHwSecurityDiagnosePlugin;
import huawei.android.security.IHwSecurityService.Stub;
import java.util.Date;

public class HwDeviceUsageReport {
    private static final int DEVICE_SECURE_DIAGNOSE_ID = 2;
    private static final boolean HW_DEBUG = false;
    private static final int ISBN_ID = 0;
    private static final String TAG = "HwDeviceUsageReport";
    private Bundle mDeviceUseData;
    private IHwSecurityDiagnosePlugin mHwSecurityDiagnosePlugin;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.deviceusage.HwDeviceUsageReport.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.deviceusage.HwDeviceUsageReport.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.deviceusage.HwDeviceUsageReport.<clinit>():void");
    }

    public HwDeviceUsageReport(Context context) {
        this.mDeviceUseData = new Bundle();
        if (HW_DEBUG) {
            Slog.d(TAG, "HwDeviceUsageReport  create");
        }
        IBinder b = ServiceManager.getService("securityserver");
        if (b != null) {
            if (HW_DEBUG) {
                Slog.d(TAG, "getHwSecurityService");
            }
            try {
                IBinder diagnoseServiceBinder = Stub.asInterface(b).querySecurityInterface(DEVICE_SECURE_DIAGNOSE_ID);
                if (diagnoseServiceBinder != null) {
                    this.mHwSecurityDiagnosePlugin = IHwSecurityDiagnosePlugin.Stub.asInterface(diagnoseServiceBinder);
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Error in get Diagnose Service ");
            }
        }
    }

    public void reportFirstUseTime(long time) {
        if (HW_DEBUG) {
            Slog.d(TAG, "getFirstUseTime");
        }
        this.mDeviceUseData.putString(HwSecDiagnoseConstant.DEVICE_RENEW_TIME, new Date(time).toString());
        this.mDeviceUseData.putString(HwSecDiagnoseConstant.DEVICE_RENEW_SN_CODE, getISBN());
        if (this.mHwSecurityDiagnosePlugin != null) {
            try {
                this.mHwSecurityDiagnosePlugin.report(WifiProCommonDefs.TYEP_HAS_INTERNET, this.mDeviceUseData);
            } catch (RemoteException e) {
                Slog.e(TAG, "report EXCEPTION = " + e);
            }
        }
    }

    private String getISBN() {
        return HwOEMInfoAdapter.getISBNOrSN(ISBN_ID);
    }
}
