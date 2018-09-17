package android.vrsystem;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.vrsystem.IVRSystemService.Stub;

public class VRSystemServiceManager implements IVRSystemServiceManager {
    private static final String SYSTEMUI = "com.android.systemui";
    private static final String TAG = "VRSystemServiceManager";
    private static final String VR_METADATA_NAME = "com.huawei.android.vr.application.mode";
    private static final String VR_METADATA_VALUE = "vr_only";
    private static final boolean VR_SWITCH = SystemProperties.getBoolean("ro.vr.surport", false);
    private static VRSystemServiceManager sInstance;
    private IVRSystemService mVRM;

    private static class Instance {
        private static VRSystemServiceManager sInstance = new VRSystemServiceManager();

        private Instance() {
        }
    }

    /* synthetic */ VRSystemServiceManager(VRSystemServiceManager -this0) {
        this();
    }

    public static VRSystemServiceManager getInstance() {
        return Instance.sInstance;
    }

    public boolean isVRMode() {
        if (!isValid()) {
            return false;
        }
        boolean isVR = false;
        try {
            isVR = this.mVRM.isVRmode();
        } catch (RemoteException ex) {
            Log.w(TAG, "vr state query exception! ", ex);
        }
        return isVR;
    }

    public boolean isVRApplication(Context context, String packageName) {
        if (!isValid() || context == null || packageName == null || packageName.equals("")) {
            return false;
        }
        return isVRApp(context, packageName);
    }

    public String getContactName(Context context, String num) {
        if (!isValid()) {
            return null;
        }
        if (checkContext(context)) {
            String name = null;
            try {
                name = this.mVRM.getContactName(num);
            } catch (RemoteException ex) {
                Log.w(TAG, "vr state query exception! ", ex);
            }
            return name;
        }
        Log.i(TAG, "Client is not vr");
        return null;
    }

    public void registerVRListener(Context context, IVRListener vrlistener) {
        if (!isValid()) {
            return;
        }
        if (checkContext(context)) {
            try {
                this.mVRM.addVRListener(vrlistener);
            } catch (RemoteException e) {
                Log.w(TAG, "add listener exception ", e);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }

    public void registerExpandListener(Context context, IVRListener vrlistener) {
        if (!isValid()) {
            return;
        }
        if (checkContext(context)) {
            try {
                this.mVRM.registerExpandListener(vrlistener);
            } catch (RemoteException e) {
                Log.w(TAG, "add listener exception ", e);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }

    public void unregisterVRListener(Context context, IVRListener vrlistener) {
        if (!isValid()) {
            return;
        }
        if (checkContext(context)) {
            try {
                this.mVRM.deleteVRListener(vrlistener);
            } catch (RemoteException e) {
                Log.w(TAG, "delete listener exception ", e);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }

    public void acceptInCall(Context context) {
        if (!isValid()) {
            return;
        }
        if (checkContext(context)) {
            try {
                this.mVRM.acceptInCall();
            } catch (RemoteException ex) {
                Log.w(TAG, "acceptInCall request exception!", ex);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }

    public void endInCall(Context context) {
        if (!isValid()) {
            return;
        }
        if (checkContext(context)) {
            try {
                this.mVRM.endInCall();
            } catch (RemoteException ex) {
                Log.w(TAG, "acceptInCall request exception!", ex);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }

    public int getHelmetBattery(Context context) {
        if (!isValid()) {
            return 0;
        }
        if (checkContext(context)) {
            int battery = 0;
            try {
                battery = this.mVRM.getHelmetBattery();
            } catch (RemoteException e) {
                Log.w(TAG, "get Helmet battery exception ", e);
            }
            return battery;
        }
        Log.i(TAG, "Client is not vr");
        return 0;
    }

    public int getHelmetBrightness(Context context) {
        if (!isValid()) {
            return 0;
        }
        if (checkContext(context)) {
            int brightness = 0;
            try {
                brightness = this.mVRM.getHelmetBrightness();
            } catch (RemoteException e) {
                Log.w(TAG, "get Helmet brightness exception ", e);
            }
            return brightness;
        }
        Log.i(TAG, "Client is not vr");
        return 0;
    }

    public void setHelmetBrightness(Context context, int brightness) {
        if (!isValid()) {
            return;
        }
        if (checkContext(context)) {
            try {
                this.mVRM.setHelmetBrightness(brightness);
            } catch (RemoteException e) {
                Log.w(TAG, "set Helmet brightness exception ", e);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }

    private VRSystemServiceManager() {
        this.mVRM = Stub.asInterface(ServiceManager.getService("vr_system"));
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
        } catch (RemoteException ex) {
            Log.w(TAG, "vr service exception, please check ", ex);
        }
        return valid;
    }

    private boolean isValid() {
        return VR_SWITCH ? checkServiceValid() : false;
    }

    private boolean checkContext(Context context) {
        return context != null ? isVRApp(context, context.getPackageName()) : false;
    }

    private boolean isVRApp(Context context, String packageName) {
        if (SYSTEMUI.equals(packageName)) {
            return true;
        }
        String vrOnly = getManifestMetadata(context, packageName, VR_METADATA_NAME);
        if (VR_METADATA_VALUE.equals(vrOnly)) {
            return true;
        }
        Log.w(TAG, "no vr app metadata " + vrOnly);
        return false;
    }

    private String getManifestMetadata(Context context, String packageName, String name) {
        ApplicationInfo appinfo = null;
        try {
            appinfo = context.getPackageManager().getApplicationInfo(packageName, 128);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getApplicationInfo exception ", e);
        }
        if (appinfo == null || appinfo.metaData == null) {
            return null;
        }
        return appinfo.metaData.getString(name);
    }
}
