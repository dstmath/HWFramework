package com.android.server.location;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.lcagent.client.LogCollectManager;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Method;

public class HwReportTool {
    private static final boolean DEBUG = false;
    private static final String REPORTERINTERFACE_LIB_PATH = "/system/framework/com.huawei.report.jar";
    private static final String REPORT_CLS = "com.huawei.report.ReporterInterface";
    private static final String REPORT_METHOD_E = "e";
    private static final String TAG = "HwReportTool";
    private static volatile HwReportTool sSingleInstance;
    private LogCollectManager mClient;
    private Context mContext;
    private Method sReportMethod;
    private Class<?> sReporterClazz;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.HwReportTool.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.HwReportTool.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwReportTool.<clinit>():void");
    }

    public static HwReportTool getInstance(Context context) {
        if (sSingleInstance == null) {
            sSingleInstance = new HwReportTool(context.getApplicationContext());
        }
        return sSingleInstance;
    }

    private HwReportTool(Context context) {
        this.sReporterClazz = null;
        this.sReportMethod = null;
        this.mClient = null;
        this.mContext = null;
        initReporter(context);
    }

    private void initReporter(Context context) {
        try {
            this.sReporterClazz = new PathClassLoader(REPORTERINTERFACE_LIB_PATH, context.getClassLoader()).loadClass(REPORT_CLS);
            this.sReportMethod = this.sReporterClazz.getDeclaredMethod(REPORT_METHOD_E, new Class[]{Context.class, Integer.TYPE, String.class});
            this.mClient = new LogCollectManager(context);
            this.mContext = context;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Can't find sReporterClazz");
            this.sReporterClazz = null;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "Can't find sReportMethod");
            this.sReportMethod = null;
        } catch (NullPointerException e3) {
            e3.printStackTrace();
        }
    }

    public boolean report(int eventID, String eventMsg) {
        if (!isBetaUser()) {
            Log.e(TAG, "This is not beta user build");
        } else if (!(this.sReportMethod == null || this.sReporterClazz == null)) {
            try {
                return ((Boolean) this.sReportMethod.invoke(this.sReporterClazz, new Object[]{this.mContext, Integer.valueOf(eventID), eventMsg})).booleanValue();
            } catch (Exception e) {
                Log.e(TAG, "got exception" + e.getMessage(), e);
            }
        }
        return DEBUG;
    }

    private boolean isBetaUser() {
        return 3 == getUserType() ? true : DEBUG;
    }

    private int getUserType() {
        int userType = -1;
        if (this.mClient == null) {
            return userType;
        }
        try {
            userType = this.mClient.getUserType();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        if (DEBUG) {
            Log.d(TAG, "userType is: " + userType);
        }
        return userType;
    }
}
