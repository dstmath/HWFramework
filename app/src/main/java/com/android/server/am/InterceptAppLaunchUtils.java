package com.android.server.am;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.ServiceManager;
import android.util.Slog;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.pm.PackageManagerService;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;

public class InterceptAppLaunchUtils {
    private static final String[] GMS_PACKAGE_NAMES = null;
    private static final String[] GOOGLE_APP_PKGS = null;
    private static final String TAG = "InterceptAppLaunchUtils";

    public static class InterceptAppDialog extends AlertDialog implements OnClickListener {
        public InterceptAppDialog(Context context, String pkgName) {
            super(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog", null, null));
            createInterceptContent(context, pkgName);
        }

        public void createInterceptContent(Context context, String pkgName) {
            String appName = InterceptAppLaunchUtils.getAppName(context, pkgName);
            getWindow().setType(2008);
            setTitle(33685902);
            setMessage(context.getString(33685903, new Object[]{appName}));
            setButton(-1, context.getString(33685718), this);
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                dismiss();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.InterceptAppLaunchUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.InterceptAppLaunchUtils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.InterceptAppLaunchUtils.<clinit>():void");
    }

    private static boolean isGmsCoreInstalled(String[] packages, int userId) {
        PackageManagerService pms = (PackageManagerService) ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY);
        for (String pkgName : packages) {
            PackageInfo pkgInfo = pms.getPackageInfo(pkgName, 0, userId);
            if (pkgInfo == null || pkgInfo.applicationInfo == null || (pkgInfo.applicationInfo.flags & 1) == 0 || (pkgInfo.applicationInfo.privateFlags & 8) == 0) {
                Slog.i(TAG, "is not installed: " + pkgName);
                return false;
            }
        }
        return true;
    }

    public static boolean isNeedInterceptGoogleApp(String pkgName, int userId) {
        if (!isInInterceptPackages(pkgName, GOOGLE_APP_PKGS) || isGmsCoreInstalled(GMS_PACKAGE_NAMES, userId)) {
            return false;
        }
        return true;
    }

    private static boolean isInInterceptPackages(String pkgName, String[] packages) {
        for (String pkg : packages) {
            if (pkg.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    private static String getAppName(Context context, String pkgName) {
        PackageManager packageManager = context.getPackageManager();
        String appName = AppHibernateCst.INVALID_PKG;
        try {
            appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName, HwSecDiagnoseConstant.BIT_VERIFYBOOT)).toString();
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "getAppName failure");
        }
        return appName;
    }
}
