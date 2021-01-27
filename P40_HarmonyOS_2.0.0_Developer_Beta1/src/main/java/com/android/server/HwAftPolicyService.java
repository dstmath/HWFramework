package com.android.server;

import android.aft.IHwAftPolicyService;
import android.app.ActivityThread;
import android.app.KeyguardManager;
import android.content.Context;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.media.AudioSystem;
import android.os.Binder;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public class HwAftPolicyService extends IHwAftPolicyService.Stub {
    private static final int AFT_HAL_DEATH_COOKIE = 1000;
    private static final boolean DEBUG;
    private static final String HARMONYOS_INCALL_WINDOW_TITLE = "com.huawei.ohos.call.base.InCallAbility";
    private static final String HW_AFT = "HwAft";
    private static final String INCALL_UI_WINDOW_TITLE = "com.android.incallui/com.android.incallui.InCallActivity";
    private static final String KEYGUARD_UI_WINDOW_TITLE = "StatusBar";
    private static final int MSG_FOCUS_CHANGED = 1;
    private static final int MSG_INCALL_MODE_CHANGED = 3;
    private static final int MSG_KEYGUARD_CHANGED = 4;
    private static final int MSG_ROTATION_CHANGED = 2;
    private static final String PERMISSION = "android.permission.CONTROL_KEYGUARD";
    private static final boolean PROXIMITY_ENABLE = SystemProperties.getBoolean("ro.product.proximityenable", true);
    private static final String PROXIMITY_UI_WINDOW_TITLE = "Emui:ProximityWnd";
    private static final String TAG = "HwAftPolicyService";
    private static final String[] VOIP_APP_WINDOW_LIST = {"com.tencent.mm/com.tencent.mm.plugin.voip.ui.VideoActivity", "com.tencent.mobileqq/com.tencent.av.ui.AVActivity"};
    private static final String WHITE_LIST = "HwAft_whitelist.xml";
    private static final String XML_NAME = "name";
    private static final String XML_WINDOW = "window";
    private int mAudioMode;
    private int mCallModeOwnerPid;
    private boolean mEarpieceIncall;
    private String mFocusWindowTitle;
    private int mFocusedAppPid;
    private final Handler mHandler;
    private boolean mIsShowing;
    private boolean mKeyguardLockState = true;
    private boolean mKeyguardStateChanged = false;
    private boolean mLastAftState = false;
    private final Object mLock = new Object();
    private int mOrientation = (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90);
    private boolean mOrientationChanged = false;
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private ITouchscreen mTpHal = null;
    private List<String> mWhiteListWindows;

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.debuggable", false) || SystemProperties.getInt("ro.logsystem.usertype", 1) == 3 || SystemProperties.getInt("ro.logsystem.usertype", 1) == 5) {
            z = true;
        }
        DEBUG = z;
    }

    final class ServiceNotification extends IServiceNotification.Stub {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            HwAftPolicyService.this.connectToHidl();
        }
    }

    /* access modifiers changed from: package-private */
    public final class DeathRecipient implements IHwBinder.DeathRecipient {
        DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1000) {
                synchronized (HwAftPolicyService.this.mLock) {
                    HwAftPolicyService.this.mTpHal = null;
                }
                HwAftPolicyService.this.mHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    final class AftHandler extends Handler {
        AftHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            boolean z = true;
            if (i == 1) {
                HwAftPolicyService.this.handleFocusChange(msg.arg1, (String) msg.obj);
            } else if (i == 2) {
                HwAftPolicyService.this.handleOrientationChange(msg.arg1);
            } else if (i == 3) {
                HwAftPolicyService.this.handleIncallModeChange(msg.arg1, msg.arg2);
            } else if (i == 4) {
                HwAftPolicyService hwAftPolicyService = HwAftPolicyService.this;
                if (msg.arg1 != 1) {
                    z = false;
                }
                hwAftPolicyService.handleKeyguardStateChange(z);
            }
        }
    }

    public HwAftPolicyService(Context context) {
        Slog.i(TAG, "HwAftPolicyService constructor");
        this.mHandler = new AftHandler(FgThread.get().getLooper());
        try {
            IServiceManager serviceManager = IServiceManager.getService();
            if (serviceManager == null || !serviceManager.registerForNotifications(ITouchscreen.kInterfaceName, "", this.mServiceNotification)) {
                Slog.e(TAG, "Failed to get serviceManager and register service start notification");
            }
            this.mWhiteListWindows = new ArrayList();
            initDeviceState(context);
            connectToHidl();
            new Thread(new Runnable() {
                /* class com.android.server.HwAftPolicyService.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwAftPolicyService.this.loadConfig();
                }
            }).start();
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to register service start notification RemoteException");
        }
    }

    public void notifyOrientationChange(int orientation) {
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            Slog.e(TAG, "you have no permission to call notifyOrientationChange from uid:" + Binder.getCallingUid());
        } else if (!isValidOrientation(orientation)) {
            Slog.e(TAG, "notifyOrientationChange param error:" + orientation);
        } else {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, orientation, 0));
        }
    }

    public void notifyIncallModeChange(int ownerPid, int mode) {
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            Slog.e(TAG, "you have no permission to call notifyIncallModeChange from uid:" + Binder.getCallingUid());
            return;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, ownerPid, mode));
    }

    public void notifyFocusChange(int focusPid, String focusWindowTitle) {
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            Slog.e(TAG, "you have no permission to call notifyFocusChange from uid:" + Binder.getCallingUid());
            return;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, focusPid, 0, focusWindowTitle));
    }

    public void notifyKeyguardStateChange(boolean isShowing) {
        if (ActivityThread.currentApplication().getApplicationContext().checkCallingOrSelfPermission(PERMISSION) != 0) {
            Slog.e(TAG, "you have no permission to call notifyKeyguardStateChange");
            return;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4, isShowing ? 1 : 0, 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToHidl() {
        synchronized (this.mLock) {
            if (this.mTpHal == null) {
                try {
                    this.mTpHal = ITouchscreen.getService();
                    if (this.mTpHal == null) {
                        Slog.e(TAG, "Failed to get ITouchscreen service");
                        return;
                    }
                    this.mTpHal.linkToDeath(new DeathRecipient(), 1000);
                    updateAftPolicyLocked(true);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to register service start notification RemoteException");
                } catch (NoSuchElementException e2) {
                    Slog.e(TAG, "Failed to register service start notification NoSuchElementException");
                }
            }
        }
    }

    private void initDeviceState(Context context) {
        Object keyguardManagerObject = context.getSystemService("keyguard");
        KeyguardManager keyguardManager = null;
        if (keyguardManagerObject instanceof KeyguardManager) {
            keyguardManager = (KeyguardManager) keyguardManagerObject;
        }
        if (keyguardManager != null) {
            this.mIsShowing = keyguardManager.isKeyguardLocked();
            if (this.mIsShowing) {
                this.mFocusWindowTitle = KEYGUARD_UI_WINDOW_TITLE;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadConfig() {
        loadConfig(HwCfgFilePolicy.getCfgFile("xml/HwAft_whitelist.xml", 0));
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0028 A[Catch:{ FileNotFoundException -> 0x001e, XmlPullParserException -> 0x001b, IOException -> 0x0018, all -> 0x0015 }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00b1 A[SYNTHETIC, Splitter:B:35:0x00b1] */
    /* JADX WARNING: Removed duplicated region for block: B:58:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    private void loadConfig(File configfile) {
        InputStream inputStream = null;
        if (configfile != null) {
            try {
                if (configfile.exists()) {
                    inputStream = new FileInputStream(configfile);
                    if (inputStream != null) {
                        XmlPullParser xmlParser = Xml.newPullParser();
                        xmlParser.setInput(inputStream, null);
                        int xmlEventType = xmlParser.next();
                        while (true) {
                            if (xmlEventType == 1) {
                                break;
                            }
                            if (DEBUG) {
                                Slog.d(TAG, "xmlName : " + xmlParser.getName());
                                Slog.d(TAG, "EventType : " + xmlEventType);
                            }
                            if (xmlEventType != 2 || !XML_WINDOW.equals(xmlParser.getName())) {
                                if (xmlEventType == 3 && HW_AFT.equals(xmlParser.getName())) {
                                    break;
                                }
                            } else {
                                String windowName = xmlParser.getAttributeValue(null, "name");
                                Slog.i(TAG, "windowName : " + windowName);
                                addWindowToWhiteList(windowName);
                            }
                            xmlEventType = xmlParser.next();
                        }
                    }
                    if (inputStream == null) {
                        try {
                            inputStream.close();
                            return;
                        } catch (IOException e) {
                            Log.e(TAG, "load config: IO Exception while closing stream");
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "loadConfig FileNotFoundException.");
                if (0 != 0) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (XmlPullParserException e3) {
                Log.e(TAG, "loadConfig XmlPullParserException");
                if (0 != 0) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (IOException e4) {
                Log.e(TAG, "loadConfig IOException");
                if (0 != 0) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "load config: IO Exception while closing stream");
                    }
                }
                throw th;
            }
        }
        Slog.w(TAG, "HwAft_whitelist.xml is not exist");
        if (inputStream != null) {
        }
        if (inputStream == null) {
        }
    }

    private void addWindowToWhiteList(String windowName) {
        if (DEBUG) {
            Slog.d(TAG, "add name:" + windowName);
        }
        if (!this.mWhiteListWindows.contains(windowName)) {
            this.mWhiteListWindows.add(windowName);
        }
    }

    private boolean isKeyguardLockWindow() {
        String str;
        if (!this.mIsShowing || (str = this.mFocusWindowTitle) == null) {
            return false;
        }
        if (str.equals(KEYGUARD_UI_WINDOW_TITLE) || (!PROXIMITY_ENABLE && this.mFocusWindowTitle.equals(PROXIMITY_UI_WINDOW_TITLE))) {
            return true;
        }
        return false;
    }

    private void scheduleUpdatePolicyLocked() {
        updateAftPolicyLocked();
    }

    private void updateAftPolicyLocked() {
        updateAftPolicyLocked(false);
    }

    private void updateAftPolicyLocked(boolean forceUpdate) {
        if (this.mTpHal != null) {
            boolean foreground = callingAppForegroundLocked();
            int i = 1;
            boolean curAftState = this.mEarpieceIncall && foreground;
            boolean curKeyguardState = isKeyguardLockWindow();
            if (DEBUG) {
                Slog.i(TAG, "updateAftPolicy, orientation=" + this.mOrientation + ", earpieceIncall=" + this.mEarpieceIncall + ", callAppforeground=" + foreground + ", curAftState=" + curAftState + ",isKeyguardShowing=" + this.mIsShowing + ", curKeyguardState=" + curKeyguardState + ", force=" + forceUpdate);
            }
            try {
                if (curAftState != this.mLastAftState || forceUpdate) {
                    this.mLastAftState = curAftState;
                    if (DEBUG) {
                        Slog.i(TAG, "call aft hidl set state=" + curAftState);
                    }
                    ITouchscreen iTouchscreen = this.mTpHal;
                    if (!curAftState) {
                        i = 0;
                    }
                    iTouchscreen.hwTsSetAftAlgoState(i);
                }
                if (this.mOrientationChanged || forceUpdate) {
                    this.mOrientationChanged = false;
                    if (DEBUG) {
                        Slog.i(TAG, "call aft hidl set orientation=" + this.mOrientation);
                    }
                    this.mTpHal.hwTsSetAftAlgoOrientation(this.mOrientation);
                }
                if (this.mKeyguardLockState != curKeyguardState || forceUpdate) {
                    this.mKeyguardLockState = curKeyguardState;
                    String config = curKeyguardState ? "version:1+state:locked" : "version:1+state:unlocked";
                    int retcode = this.mTpHal.hwTsSetAftConfig(config);
                    if (DEBUG) {
                        Slog.d(TAG, "call hwTsSetAftConfig config={" + config + "} retcode=" + retcode);
                    }
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "hidl call throw exception RemoteException");
            }
            if (DEBUG) {
                Slog.d(TAG, "updateAftPolicy complete.");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKeyguardStateChange(boolean isShowing) {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.i(TAG, "handleKeyguardStateChange isShowing:" + isShowing);
            }
            this.mIsShowing = isShowing;
            scheduleUpdatePolicyLocked();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOrientationChange(int orientation) {
        synchronized (this.mLock) {
            if (this.mOrientation != orientation) {
                if (DEBUG) {
                    Slog.i(TAG, "handleOrientationChange device orientation change to " + orientation);
                }
                this.mOrientation = orientation;
                this.mOrientationChanged = true;
                scheduleUpdatePolicyLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIncallModeChange(int ownerPid, int mode) {
        boolean useEarpieceOnly = false;
        int devices = AudioSystem.getDevicesForStream(0);
        if ((devices & 1) != 0) {
        }
        if ((devices & 3) == 1 && isInCallMode(mode)) {
            useEarpieceOnly = true;
        }
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("ownerPid:");
            sb.append(ownerPid);
            sb.append(", device:0x");
            sb.append(Integer.toHexString(devices));
            sb.append(", callingMode:");
            sb.append(mode);
            sb.append(", should turn ");
            sb.append(useEarpieceOnly ? "on" : "off");
            sb.append(" aft, mEarpieceIncall:");
            sb.append(this.mEarpieceIncall);
            sb.append(", mLastAftState:");
            sb.append(this.mLastAftState);
            Slog.i(TAG, sb.toString());
        }
        synchronized (this.mLock) {
            this.mCallModeOwnerPid = ownerPid;
            this.mAudioMode = mode;
            if (this.mEarpieceIncall != useEarpieceOnly) {
                if (DEBUG) {
                    Slog.i(TAG, "handleIncallModeChange earpiece mode change to " + useEarpieceOnly);
                }
                this.mEarpieceIncall = useEarpieceOnly;
                scheduleUpdatePolicyLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFocusChange(int focusPid, String focusWindowTitle) {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.i(TAG, "handleFocusChange focusPid=" + focusPid + ", focusWindowTitle:" + focusWindowTitle);
            }
            this.mFocusWindowTitle = focusWindowTitle;
            this.mFocusedAppPid = focusPid;
            scheduleUpdatePolicyLocked();
        }
    }

    private boolean callingAppForegroundLocked() {
        int i;
        String str;
        if (this.mCallModeOwnerPid == Process.myPid()) {
            String str2 = this.mFocusWindowTitle;
            return str2 != null && (str2.equals(INCALL_UI_WINDOW_TITLE) || this.mFocusWindowTitle.equals(HARMONYOS_INCALL_WINDOW_TITLE));
        }
        boolean inWhiteList = false;
        for (String window : VOIP_APP_WINDOW_LIST) {
            if (window.equals(this.mFocusWindowTitle)) {
                inWhiteList = true;
            }
        }
        if (!inWhiteList && (str = this.mFocusWindowTitle) != null) {
            inWhiteList = this.mWhiteListWindows.contains(str);
        }
        return inWhiteList && (i = this.mCallModeOwnerPid) != 0 && this.mFocusedAppPid == i;
    }

    private boolean isInCallMode(int mode) {
        return mode == 2 || mode == 3;
    }

    private boolean isValidOrientation(int orientation) {
        return orientation == 0 || orientation == 1 || orientation == 2 || orientation == 3;
    }
}
