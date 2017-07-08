package android.vrsystem;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.vrsystem.IVRSystemService.Stub;

public class VRSystemServiceManager implements IVRSystemServiceManager {
    private static final int MODE_NO_VR = 2;
    private static final int MODE_VR = 1;
    private static final String SYSTEMUI = "com.android.systemui";
    private static final String TAG = "VRSystemServiceManager";
    private static final String VR_METADATA_NAME = "com.huawei.android.vr.application.mode";
    private static final String VR_METADATA_VALUE = "vr_only";
    private static final boolean VR_SWITCH = false;
    private static VRSystemServiceManager sInstance;
    private IVRSystemService mVRM;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.vrsystem.VRSystemServiceManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.vrsystem.VRSystemServiceManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.vrsystem.VRSystemServiceManager.<clinit>():void");
    }

    private VRSystemServiceManager(IVRSystemService ivrm) {
        this.mVRM = ivrm;
    }

    public static VRSystemServiceManager getInstance() {
        VRSystemServiceManager vRSystemServiceManager;
        synchronized (VRSystemServiceManager.class) {
            if (sInstance == null || !sInstance.isValid()) {
                sInstance = new VRSystemServiceManager(Stub.asInterface(ServiceManager.getService("vr_system")));
            }
            vRSystemServiceManager = sInstance;
        }
        return vRSystemServiceManager;
    }

    private boolean checkServiceValid() {
        this.mVRM = Stub.asInterface(ServiceManager.getService("vr_system"));
        if (this.mVRM == null) {
            Log.w(TAG, "vr service is not alive");
            return false;
        }
        boolean valid = false;
        try {
            this.mVRM.isVRmode();
            valid = true;
        } catch (Exception ex) {
            Log.w(TAG, "vr service exception, please check", ex);
        }
        return valid;
    }

    private boolean isValid() {
        return VR_SWITCH ? checkServiceValid() : false;
    }

    public boolean isVRMode() {
        if (!isValid()) {
            return false;
        }
        boolean isVR = false;
        try {
            isVR = this.mVRM.isVRmode();
        } catch (Exception ex) {
            Log.w(TAG, "vr state query exception!", ex);
        }
        return isVR;
    }

    public boolean isVRApplication(Context context, String packageName) {
        if (!isValid() || context == null || packageName == null || packageName.equals("")) {
            return false;
        }
        if (SYSTEMUI.equals(packageName)) {
            return true;
        }
        boolean allowStart;
        ApplicationInfo appinfo = null;
        try {
            appinfo = context.getPackageManager().getApplicationInfo(packageName, PduHeaders.VALUE_YES);
        } catch (Exception e) {
            Log.e(TAG, "getApplicationInfo exception", e);
        }
        if (appinfo == null || appinfo.metaData == null) {
            allowStart = false;
        } else {
            allowStart = VR_METADATA_VALUE.equals(appinfo.metaData.getString(VR_METADATA_NAME));
        }
        if (!allowStart) {
            Log.i(TAG, "no vr metaData");
        }
        return allowStart;
    }

    public String getContactName(Context context, String num) {
        if (!isValid() || context == null) {
            return null;
        }
        if (isVRApplication(context, context.getPackageName())) {
            String name = null;
            try {
                name = this.mVRM.getContactName(num);
            } catch (Exception ex) {
                Log.w(TAG, "vr state query exception!", ex);
            }
            return name;
        }
        Log.i(TAG, "Client is not vr");
        return null;
    }

    public void registerVRListener(Context context, IVRListener vrlistener) {
        if (!isValid() || context == null) {
            return;
        }
        if (isVRApplication(context, context.getPackageName())) {
            try {
                this.mVRM.addVRListener(vrlistener);
            } catch (RemoteException e) {
                Log.w(TAG, "add listener exception", e);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }

    public void unregisterVRListener(Context context, IVRListener vrlistener) {
        if (isValid() && context != null) {
            try {
                this.mVRM.deleteVRListener(vrlistener);
            } catch (RemoteException e) {
                Log.w(TAG, "delete listener exception", e);
            }
        }
    }

    public void acceptInCall(Context context) {
        if (!isValid() || context == null) {
            return;
        }
        if (isVRApplication(context, context.getPackageName())) {
            try {
                this.mVRM.acceptInCall();
            } catch (Exception ex) {
                Log.w(TAG, "acceptInCall request exception!", ex);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }

    public void endInCall(Context context) {
        if (!isValid() || context == null) {
            return;
        }
        if (isVRApplication(context, context.getPackageName())) {
            try {
                this.mVRM.endInCall();
            } catch (Exception ex) {
                Log.w(TAG, "endInCall request exception!", ex);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }
}
