package huawei.com.android.server.util;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.lcagent.client.LogCollectManager;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Method;

public class ReportTool {
    protected static final boolean HWLOGW_E = true;
    private static final String REPORTERINTERFACE_LIB_PATH = "/system/framework/com.huawei.report.jar";
    private static final String REPORT_CLS = "com.huawei.report.ReporterInterface";
    private static final String REPORT_METHOD_E = "e";
    private static final String TAG = "ReportTools";
    private static final String TAG_FLOW = "ReportTools_FLOW";
    private static LogCollectManager mClient;
    private static Context mContext;
    private static Method sReportMethod;
    private static Class<?> sReporterClazz;
    private static ReportTool sSingleInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.server.util.ReportTool.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.server.util.ReportTool.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.server.util.ReportTool.<clinit>():void");
    }

    public static ReportTool getInstance(Context context) {
        if (sSingleInstance == null) {
            sSingleInstance = new ReportTool(context.getApplicationContext());
        }
        return sSingleInstance;
    }

    private ReportTool(Context context) {
        initReporter(context);
    }

    private void initReporter(Context context) {
        try {
            sReporterClazz = new PathClassLoader(REPORTERINTERFACE_LIB_PATH, context.getClassLoader()).loadClass(REPORT_CLS);
            sReportMethod = sReporterClazz.getDeclaredMethod(REPORT_METHOD_E, new Class[]{Context.class, Integer.TYPE, String.class});
            mClient = new LogCollectManager(context);
            mContext = context;
        } catch (ClassNotFoundException e) {
            Log.e(TAG_FLOW, "Can't find sReporterClazz");
            sReporterClazz = null;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG_FLOW, "Can't find sReportMethod");
            sReportMethod = null;
        }
    }

    public boolean report(int eventID, String eventMsg) {
        if (isBetaUser()) {
            try {
                if (!(sReportMethod == null || sReporterClazz == null)) {
                    return ((Boolean) sReportMethod.invoke(sReporterClazz, new Object[]{mContext, Integer.valueOf(eventID), eventMsg})).booleanValue();
                }
            } catch (Exception e) {
                Log.e(TAG_FLOW, "got exception" + e.getMessage(), e);
            }
        } else {
            Log.e(TAG_FLOW, "This is not beta user build");
        }
        return false;
    }

    private boolean isBetaUser() {
        return 3 == getUserType() ? HWLOGW_E : false;
    }

    private int getUserType() {
        int userType = -1;
        try {
            userType = mClient.getUserType();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        return userType;
    }
}
