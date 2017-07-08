package com.android.server.pm;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.security.IHwSecurityDiagnosePlugin;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.IHwSecurityService.Stub;

public class AntiMalPreInstallReport {
    private static final int DEVICE_SECURE_DIAGNOSE_ID = 2;
    private static final boolean HW_DEBUG = false;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "AntiMalPreInstallReport";
    private AntiMalDataManager mDataManager;
    private ReportListener mListener;
    private Thread mReportTask;

    interface ReportListener {
        void onReported();
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.AntiMalPreInstallReport.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.AntiMalPreInstallReport.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.AntiMalPreInstallReport.<clinit>():void");
    }

    public AntiMalPreInstallReport(AntiMalDataManager dataManager) {
        this.mReportTask = new Thread() {
            public void run() {
                AntiMalPreInstallReport.this.sendAntimaComponentInfo();
                AntiMalPreInstallReport.this.sendAntiMalData();
            }
        };
        this.mDataManager = dataManager;
    }

    public void report(ReportListener listener) {
        this.mListener = listener;
        this.mReportTask.start();
    }

    private void sendAntimaComponentInfo() {
        Bundle bundle = this.mDataManager.getAntimalComponentInfo();
        try {
            IHwSecurityService secServie = Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
            if (secServie != null) {
                IHwSecurityDiagnosePlugin secDgnService = IHwSecurityDiagnosePlugin.Stub.asInterface(secServie.querySecurityInterface(DEVICE_SECURE_DIAGNOSE_ID));
                if (secDgnService != null) {
                    secDgnService.sendComponentInfo(bundle);
                }
            }
        } catch (RemoteException re) {
            Log.e(TAG, "sendAntiMalData re:" + re);
        }
    }

    private void sendAntiMalData() {
        if (HW_DEBUG) {
            Log.d(TAG, "sendAntiMalData begin!");
        }
        if (this.mDataManager.needReport()) {
            if (HW_DEBUG) {
                Log.d(TAG, "sendAntiMalData Need send!");
            }
            Bundle bundle = this.mDataManager.collectData();
            try {
                IHwSecurityService secServie = Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
                if (secServie != null) {
                    IHwSecurityDiagnosePlugin secDgnService = IHwSecurityDiagnosePlugin.Stub.asInterface(secServie.querySecurityInterface(DEVICE_SECURE_DIAGNOSE_ID));
                    if (secDgnService != null) {
                        secDgnService.report(100, bundle);
                    }
                }
            } catch (RemoteException re) {
                Log.e(TAG, "sendAntiMalData re:" + re);
            }
        }
        if (this.mDataManager.needScanIllegalApks()) {
            this.mDataManager.writeAntiMalData();
        }
        onReported();
    }

    private void onReported() {
        if (this.mListener != null) {
            this.mListener.onReported();
        }
    }
}
