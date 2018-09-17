package com.android.server.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioSystem;
import android.os.Binder;
import android.os.RemoteException;
import android.pc.IHwPCManager;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.Log;

public class HwMediaFocusControl extends MediaFocusControl {
    private static final boolean DEBUG = false;
    private static final String TAG = "HwMediaFocusControl";
    protected volatile boolean mInDestopMode = false;

    public HwMediaFocusControl(Context cntxt, PlayerFocusEnforcer pfe) {
        super(cntxt, pfe);
    }

    protected boolean isAppInExternalDisplay(AudioAttributes aa, String clientId, String pkgName, int uid) {
        HwPCUtils.log(TAG, "isAppInExternalDisplay aa = " + aa + ", clientId = " + clientId + ", pkgName = " + pkgName + ", mInDestopMode = " + this.mInDestopMode + ", uid = " + uid);
        if (!this.mInDestopMode || "AudioFocus_For_Phone_Ring_And_Calls".compareTo(clientId) == 0 || (AudioSystem.getDevicesForStream(3) & 1024) == 0) {
            return false;
        }
        boolean isMedia = aa != null && aa.getUsage() == 1;
        if (isMedia && (TextUtils.isEmpty(pkgName) ^ 1) != 0) {
            long token;
            try {
                IHwPCManager service = HwPCUtils.getHwPCManager();
                if (service != null) {
                    token = Binder.clearCallingIdentity();
                    boolean isPackageRunningOnPCMode = service.isPackageRunningOnPCMode(pkgName, uid);
                    Binder.restoreCallingIdentity(token);
                    return isPackageRunningOnPCMode;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "isAppInExternalDisplay RemoteException");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        }
        return false;
    }

    public void desktopModeChanged(boolean desktopMode) {
        HwPCUtils.log(TAG, "changedToDestopMode desktopMode = " + desktopMode);
        if (desktopMode != this.mInDestopMode) {
            this.mInDestopMode = desktopMode;
            if (!this.mInDestopMode) {
                synchronized (mAudioFocusLock) {
                    this.mFocusStackForExternal.clear();
                }
            }
        }
    }

    public boolean isPkgInExternalStack(String pkgName) {
        HwPCUtils.log(TAG, "isPkgInExternalStack pkgName = " + pkgName);
        if (pkgName == null) {
            return false;
        }
        synchronized (mAudioFocusLock) {
            for (FocusRequester fr : this.mFocusStackForExternal) {
                if (fr.hasSamePackage(pkgName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
