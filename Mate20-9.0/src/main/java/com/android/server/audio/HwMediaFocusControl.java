package com.android.server.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Binder;
import android.os.RemoteException;
import android.pc.IHwPCManager;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.Log;
import java.util.Iterator;

public class HwMediaFocusControl extends MediaFocusControl {
    private static final boolean DEBUG = false;
    private static final String TAG = "HwMediaFocusControl";
    protected volatile boolean mInDestopMode = false;

    public HwMediaFocusControl(Context cntxt, PlayerFocusEnforcer pfe) {
        super(cntxt, pfe);
    }

    /* access modifiers changed from: protected */
    public boolean isMediaForDPExternalDisplay(AudioAttributes aa, String clientId, String pkgName, int uid) {
        long token;
        HwPCUtils.log(TAG, "isMediaForDPExternalDisplay aa = " + aa + ", clientId = " + clientId + ", pkgName = " + pkgName + ", mInDestopMode = " + this.mInDestopMode + ", uid = " + uid);
        if (!this.mInDestopMode || "AudioFocus_For_Phone_Ring_And_Calls".compareTo(clientId) == 0 || (33792 & AudioSystem.getDevicesForStream(3)) == 0) {
            return false;
        }
        boolean isMedia = true;
        if (aa == null || aa.getUsage() != 1) {
            isMedia = false;
        }
        if (isMedia && !TextUtils.isEmpty(pkgName)) {
            try {
                IHwPCManager service = HwPCUtils.getHwPCManager();
                if (service != null) {
                    token = Binder.clearCallingIdentity();
                    boolean isPackageRunningOnPCMode = service.isPackageRunningOnPCMode(pkgName, uid);
                    Binder.restoreCallingIdentity(token);
                    return isPackageRunningOnPCMode;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "isMediaForDPExternalDisplay RemoteException");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }
        return false;
    }

    public void desktopModeChanged(boolean desktopMode) {
        HwPCUtils.log(TAG, "changedToDestopMode desktopMode = " + desktopMode);
        if (desktopMode != this.mInDestopMode) {
            this.mInDestopMode = desktopMode;
        }
    }

    public boolean isPkgInExternalDisplay(String pkgName) {
        long token;
        HwPCUtils.log(TAG, "isPkgInExternalDisplay pkgName = " + pkgName);
        if (pkgName == null) {
            return false;
        }
        synchronized (mAudioFocusLock) {
            Iterator it = this.mFocusStack.iterator();
            while (it.hasNext()) {
                FocusRequester fr = (FocusRequester) it.next();
                boolean isLargeDisplayApp = false;
                try {
                    IHwPCManager service = HwPCUtils.getHwPCManager();
                    if (service != null) {
                        token = Binder.clearCallingIdentity();
                        isLargeDisplayApp = service.isPackageRunningOnPCMode(pkgName, fr.getClientUid());
                        Binder.restoreCallingIdentity(token);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "isPkgInExternalDisplay RemoteException");
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
                if (fr.hasSamePackage(pkgName) && isLargeDisplayApp) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInDesktopMode() {
        return this.mInDestopMode;
    }

    /* access modifiers changed from: protected */
    public void travelsFocusedStack() {
        Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
        while (stackIterator.hasNext()) {
            FocusRequester nextFr = stackIterator.next();
            boolean isInExternalDisplay = isMediaForDPExternalDisplay(nextFr.getAudioAttributes(), nextFr.getClientId(), nextFr.getPackageName(), nextFr.getClientUid());
            HwPCUtils.log(TAG, "travelsFocusedStack isInExternalDisplay = " + isInExternalDisplay);
            nextFr.setIsInExternal(isInExternalDisplay);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isUsageAffectDesktopMedia(int usage) {
        HwPCUtils.log(TAG, " isUsageAffectDesktopMedia usage = " + usage);
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        if (usage == 5 || usage == 1) {
            return true;
        }
        if (usage == 2 && audioManager.getMode() == 3) {
            return true;
        }
        return false;
    }
}
