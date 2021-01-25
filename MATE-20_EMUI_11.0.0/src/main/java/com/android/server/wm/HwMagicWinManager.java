package com.android.server.wm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.animation.Animation;
import com.huawei.android.util.SlogEx;
import com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback;
import com.huawei.onehopassistantsdk.IOnehopMagicWindowInterface;
import com.huawei.server.magicwin.DefaultHwMagicWinManager;
import com.huawei.server.magicwin.HwMagicWinStatistics;
import com.huawei.server.magicwin.HwMagicWindowConfig;
import com.huawei.server.magicwin.HwMagicWindowConfigLoader;
import com.huawei.server.magicwin.HwMagicWindowManagerService;
import com.huawei.server.magicwin.HwMagicWindowUIController;
import com.huawei.server.utils.SharedParameters;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HwMagicWinManager extends DefaultHwMagicWinManager {
    private static final String BLACK_LIST_PATH_HEAD = "magicWindowFeature_multiscreen_projection_limit";
    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 0;
    private static final String KEY_MULTI_RESUME = "android.allow_multiple_resumed_activities";
    private static final int MAX_CONTAINER_COUNT = 2;
    private static final int OHM_APP_SWITCH_CHANGE = 2;
    private static final int OHM_CONFIG_FILES = 1;
    private static final String ONE_HOP_PACKAGE = "com.huawei.pcassistant";
    private static final String ONE_HOP_SERVICE = "com.huawei.pcassistant.IOnehopFwkInterface";
    public static final int SINK = 1;
    public static final int SOURCE = 0;
    private static final String TAG = "HWMW_HwMagicWinManager";
    private static HwMagicWinManager mInstance = new HwMagicWinManager();
    private HwMagicWinAmsPolicy mAmsPolicy;
    private Map<Integer, HwMagicContainer> mContainers = new HashMap(2);
    private Context mContext;
    private boolean mIsOneHopSvcBound = false;
    private boolean mIsSink = false;
    private HwMagicWindowConfig.SystemConfig mLocalSysCfg;
    private IOnehopMagicWindowCallback mOnehopCallback = new IOnehopMagicWindowCallback.Stub() {
        /* class com.android.server.wm.HwMagicWinManager.AnonymousClass2 */

        @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback
        public void onReceive(int msgType, String msg) {
            SlogEx.d(HwMagicWinManager.TAG, "Receive string by onehop. type=" + msgType + " isSink?" + HwMagicWinManager.this.mIsSink);
            if (msgType != 2 || HwMagicWinManager.this.mIsSink) {
                SlogEx.w(HwMagicWinManager.TAG, "Msg not handled!");
            } else {
                HwMagicWinManager.this.mService.handleAppSwitchChange(msg);
            }
        }

        @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback
        public void onReceiveBundle(int msgType, Bundle data) {
            SlogEx.d(HwMagicWinManager.TAG, "Receive bundle by onehop. type=" + msgType);
        }

        @Override // com.huawei.onehopassistantsdk.IOnehopMagicWindowCallback
        public void onReceiveFile(int msgType, List<Uri> uris) {
            SlogEx.d(HwMagicWinManager.TAG, "Receive file by onehop. type=" + msgType + " isSink?" + HwMagicWinManager.this.mIsSink);
            if (msgType != 1 || HwMagicWinManager.this.mIsSink) {
                SlogEx.w(HwMagicWinManager.TAG, "Msg not handled!");
            } else {
                HwMagicWinManager.this.mService.handleConfigFiles(uris);
            }
        }
    };
    private ServiceConnection mOnehopConn = new ServiceConnection() {
        /* class com.android.server.wm.HwMagicWinManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            SlogEx.i(HwMagicWinManager.TAG, "One hop service connected.");
            HwMagicWinManager.this.mOnehopService = IOnehopMagicWindowInterface.Stub.asInterface(service);
            HwMagicWinManager.this.mService.handleOneHopSvcStateChanged(true);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            SlogEx.i(HwMagicWinManager.TAG, "One hop service lost.");
            HwMagicWinManager.this.mOnehopService = null;
        }
    };
    private IOnehopMagicWindowInterface mOnehopService = null;
    private HwMagicWindowManagerService mService;
    private SharedParameters mSharedParameters;
    private int mVirtualDisplayId = -1;
    private HwMagicWinWmsPolicy mWmsPolicy;

    private HwMagicWinManager() {
    }

    public static HwMagicWinManager getInstance() {
        return mInstance;
    }

    public void init(SharedParameters parameters, HwMagicWindowManagerService service) {
        this.mSharedParameters = parameters;
        this.mContext = parameters.getContext();
        this.mService = service;
        this.mLocalSysCfg = HwMagicWindowConfigLoader.loadSystem(null);
        if (this.mLocalSysCfg.isSupportLocalConfig()) {
            createContainer(0, null, this.mLocalSysCfg);
        }
        this.mAmsPolicy = new HwMagicWinAmsPolicy(parameters);
        this.mWmsPolicy = new HwMagicWinWmsPolicy(parameters);
    }

    public HwMagicWindowConfig.SystemConfig getLocalSysCfg() {
        return this.mLocalSysCfg;
    }

    public void createContainer(int type, List<Uri> uris, HwMagicWindowConfig.SystemConfig sysCfg) {
        if (!this.mContainers.containsKey(Integer.valueOf(type))) {
            SlogEx.e(TAG, "createContainer type = " + type);
            if (type == 0) {
                HwMagicContainer container = new HwMagicLocalContainer(this.mSharedParameters, uris, sysCfg);
                this.mContainers.put(Integer.valueOf(type), container);
                getUIController().createMwUi(container.getDisplayId(), container);
            } else if (type == 1) {
                this.mContainers.put(Integer.valueOf(type), new HwMagicVirtualContainer(this.mSharedParameters, uris, sysCfg));
                notifyPCAssistantStateChanged();
            }
        }
    }

    private void releaseContainer(int type) {
        HwMagicContainer container;
        HwMagicContainer virtualContainer = getVirtualContainer();
        if (virtualContainer != null) {
            virtualContainer.getConfig().clearAppConfigVirtual();
            virtualContainer.getCameraRotation().release();
        }
        if (type == 1 && (container = this.mContainers.remove(Integer.valueOf(type))) != null) {
            getUIController().onContainerRelease(container.getDisplayId());
        }
    }

    public HwMagicContainer getContainer(ActivityRecordEx ar) {
        if (ar == null || ar.getDisplayEx() == null) {
            return null;
        }
        return getContainerByDisplayId(ar.getDisplayEx().getDisplayId());
    }

    public HwMagicContainer getContainer(WindowStateEx state) {
        if (state == null) {
            return null;
        }
        return getContainerByDisplayId(state.getDisplayId());
    }

    public HwMagicContainer getContainer(int type) {
        return this.mContainers.get(Integer.valueOf(type));
    }

    public HwMagicContainer getLocalContainer() {
        return this.mContainers.get(0);
    }

    public HwMagicContainer getContainerByDisplayId(int displayId) {
        if (displayId > 0 && displayId == this.mVirtualDisplayId) {
            return getContainer(1);
        }
        if (displayId == 0) {
            return getContainer(0);
        }
        return null;
    }

    public HwMagicContainer getVirtualContainer() {
        return this.mContainers.get(1);
    }

    public boolean isAnyContainerExist() {
        return !this.mContainers.isEmpty();
    }

    public boolean isSupportVirtualContainer() {
        return true;
    }

    public HwMagicWinWmsPolicy getWmsPolicy() {
        return this.mWmsPolicy;
    }

    public HwMagicWinAmsPolicy getAmsPolicy() {
        return this.mAmsPolicy;
    }

    public boolean isSupportMultiResume(String packageName) {
        try {
            ApplicationInfo appInfo = this.mSharedParameters.getContext().getPackageManager().getApplicationInfo(packageName, 128);
            if (appInfo.metaData == null || appInfo.metaData.getBoolean(KEY_MULTI_RESUME, true)) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            SlogEx.e(TAG, "isSupportMultiResume:package name no found !");
            return false;
        }
    }

    public Handler getHandler() {
        return this.mService.mHandler;
    }

    public boolean isSupportOpenCapability() {
        return true;
    }

    public Rect getHwMagicWinMiddleBounds(int type) {
        HwMagicContainer container = getContainer(type);
        if (container == null) {
            SlogEx.w(TAG, "getHwMagicWinMiddleBounds container is null return empty, type = " + type);
            return new Rect(0, 0, 0, 0);
        }
        Rect rect = container.getBounds(3, false);
        SlogEx.i(TAG, "getHwMagicWinMiddleBounds  = " + rect);
        return rect;
    }

    public boolean getHwMagicWinEnabled(int type, String packageName) {
        HwMagicContainer container = getContainer(type);
        boolean isEnabled = container != null && container.getHwMagicWinEnabled(packageName);
        SlogEx.i(TAG, "getHwMagicWinEnabled  = " + isEnabled + " pkg:" + packageName);
        return isEnabled;
    }

    public void updateAppMagicWinStatusInMultiDevice(int reason, int targetDisplayId, int targetWidth, int targetHeight) {
        SlogEx.i(TAG, "updateAppMagicWinStatusInMultiDevice reason=" + reason + ",targetDisplayId=" + targetDisplayId + ", targetWidth=" + targetWidth + ",targetHeight=" + targetHeight);
        if (reason == 1) {
            setVirtualDisplayId(targetDisplayId);
            HwMagicContainer container = this.mContainers.get(1);
            if (container == null) {
                SlogEx.e(TAG, "Fatal error, create virtual container fail!!!");
                return;
            }
            container.attachDisplayId(targetDisplayId);
            container.updateDisplayMetrics(targetWidth, targetHeight);
            getUIController().createMwUi(targetDisplayId, container);
            container.registerEventListener();
            this.mAmsPolicy.mModeSwitcher.moveFocusToMagicWinInVirtualContainer();
            container.getCameraRotation().updateCameraRotation(1);
        } else if (reason == 2) {
            HwMagicWinStatistics.getInstance(1);
            HwMagicWinStatistics.stopTicks("exit_virtual_fullscreen");
            HwMagicContainer virtualContainer = getVirtualContainer();
            if (virtualContainer != null) {
                virtualContainer.getCameraRotation().updateCameraRotation(-1);
            }
        } else if (reason != 3) {
            SlogEx.e(TAG, "the input reason is error! reason = " + reason);
        } else {
            HwMagicContainer vContainer = getVirtualContainer();
            if (vContainer != null) {
                getUIController().hideMwWallpaperInNeed(vContainer.getDisplayId());
                vContainer.getCameraRotation().updateCameraRotation(-1);
            }
            HwMagicWinStatistics.getInstance(1).stopTick("virtual_display_empty");
            setVirtualDisplayId(-1);
        }
    }

    private void setVirtualDisplayId(int displayId) {
        this.mVirtualDisplayId = displayId;
    }

    public int getVirtualDisplayId() {
        return this.mVirtualDisplayId;
    }

    public boolean isMaster(ActivityRecordEx ar) {
        return checkPosition(ar, 1);
    }

    public boolean isSlave(ActivityRecordEx ar) {
        return checkPosition(ar, 2);
    }

    public boolean isMiddle(ActivityRecordEx ar) {
        return checkPosition(ar, 3);
    }

    public boolean isFull(ActivityRecordEx ar) {
        return checkPosition(ar, 5);
    }

    private boolean checkPosition(ActivityRecordEx ar, int pos) {
        HwMagicContainer container = getContainer(ar);
        if (ar == null || container == null || container.getBoundsPosition(ar.getRequestedOverrideBounds()) != pos) {
            return false;
        }
        return true;
    }

    public int getDragFullMode(ActivityRecordEx activity) {
        TaskRecordEx task = activity.getTaskRecordEx();
        if (task != null && task.inHwMagicWindowingMode()) {
            return task.getDragFullMode();
        }
        return 0;
    }

    public void setDragFullMode(ActivityRecordEx activity, int fullMode) {
        TaskRecordEx task = activity.getTaskRecordEx();
        if (task != null) {
            if ((fullMode == 5 || fullMode == 6 || fullMode == 0) && task.getDragFullMode() != fullMode) {
                task.setDragFullMode(fullMode);
            }
        }
    }

    public boolean isDragFullMode(ActivityRecordEx activity) {
        return isDragFullMode(getDragFullMode(activity));
    }

    public boolean isDragFullMode(int mode) {
        return mode == 5 || mode == 6;
    }

    public void handleMsgOneHopConnState(Message msg) {
        this.mIsSink = msg.arg1 == 1;
        if (msg.arg2 == 1) {
            Intent oneHopIntent = new Intent(ONE_HOP_SERVICE);
            oneHopIntent.setPackage(ONE_HOP_PACKAGE);
            try {
                this.mIsOneHopSvcBound = this.mContext.bindService(oneHopIntent, this.mOnehopConn, 1);
                SlogEx.i(TAG, "Bind one hop service: " + this.mIsOneHopSvcBound);
            } catch (SecurityException e) {
                SlogEx.e(TAG, "No access to bind one hop svc, or one hop svc not found.");
            }
        } else {
            if (!this.mIsSink) {
                HwMagicWinStatistics.getInstance(1).stopTick("exit_assistant");
                releaseContainer(1);
            }
            disconnectOnehop();
        }
    }

    private void disconnectOnehop() {
        if (this.mOnehopService != null) {
            SlogEx.i(TAG, "Unregister one hop callback");
            try {
                this.mOnehopService.unRegisterCallback(this.mOnehopCallback);
            } catch (RemoteException e) {
                SlogEx.e(TAG, "No one hop svc to call unRegisterCallback.");
            } catch (Throwable th) {
                this.mOnehopService = null;
                throw th;
            }
            this.mOnehopService = null;
        }
        if (this.mIsOneHopSvcBound) {
            SlogEx.i(TAG, "Unbind one hop service.");
            this.mContext.unbindService(this.mOnehopConn);
            this.mIsOneHopSvcBound = false;
        }
    }

    public void handleMsgOneHopSvcConnState(Message msg) {
        if (msg.arg1 == 1) {
            handleOnehopSvcConnected();
        } else {
            disconnectOnehop();
        }
    }

    private void handleOnehopSvcConnected() {
        if (this.mOnehopService != null) {
            SlogEx.i(TAG, "Register one hop callback");
            try {
                this.mOnehopService.registerCallback(this.mOnehopCallback);
            } catch (RemoteException e) {
                SlogEx.e(TAG, "No one hop svc to call registerCallback.");
            }
            if (this.mIsSink) {
                try {
                    this.mOnehopService.sendFile(1, getLocalContainer().getConfig().getConfigFiles());
                    SlogEx.d(TAG, "Sink?" + this.mIsSink + " send:OHM_CONFIG_FILES");
                } catch (RemoteException e2) {
                    SlogEx.e(TAG, "One hop service can not reach.");
                }
            }
        }
    }

    public void handleConfigFiles(List<Uri> uris) {
        if (this.mIsSink) {
            return;
        }
        if (uris.size() == 0) {
            SlogEx.e(TAG, "No URI in OHM_CONFIG_FILES.");
            return;
        }
        createContainer(1, uris, this.mLocalSysCfg);
        sendPadCastBlackListPath(uris);
    }

    private void sendPadCastBlackListPath(List<Uri> uris) {
        File blackListFile;
        String blackListFilePath = null;
        for (int i = 0; i < uris.size(); i++) {
            String configPath = uris.get(i).getPath();
            if (configPath != null && configPath.contains(BLACK_LIST_PATH_HEAD)) {
                blackListFilePath = configPath.replace("/root/", "");
            }
        }
        if (TextUtils.isEmpty(blackListFilePath) && (blackListFile = HwCfgFilePolicy.getCfgFile("/xml/multiscreen_projection_limit.xml", 0)) != null) {
            blackListFilePath = blackListFile.getPath();
        }
        SlogEx.d(TAG, "sendPadCastBlackListPath  blackListPath = " + blackListFilePath);
        notifyPadCastBlackListPath(blackListFilePath);
    }

    private void notifyPadCastBlackListPath(String filePath) {
        ActivityTaskManagerServiceEx atmsEx = this.mSharedParameters.getAms().getActivityTaskManagerEx();
        if (atmsEx == null) {
            SlogEx.w(TAG, "notify pad cast black list fail.");
            return;
        }
        try {
            Field atmsField = atmsEx.getClass().getDeclaredField("mATMS");
            atmsField.setAccessible(true);
            Optional<Class<?>> atmsClassOpt = Optional.ofNullable(atmsField.get(atmsEx)).map($$Lambda$HwMagicWinManager$mXd9uBJgScgmgJYtHkeAAnFDNI.INSTANCE);
            if (atmsClassOpt.isPresent()) {
                HwActivityTaskManagerServiceEx hwAtmsEx = (HwActivityTaskManagerServiceEx) atmsClassOpt.get().getField("mHwATMSEx").get(atmsField.get(atmsEx));
                hwAtmsEx.getClass().getMethod("loadPadCastBlackList", String.class).invoke(hwAtmsEx, filePath);
            }
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            SlogEx.e(TAG, "send pad cast black list exception.");
        }
    }

    public void sendAppSwitchToSource(String pkg, boolean isEnabled) {
        if (this.mOnehopService == null || !this.mIsSink) {
            SlogEx.w(TAG, "App switch not sent. Svc?" + this.mOnehopService + " isSink?" + this.mIsSink);
            return;
        }
        StringBuilder sb = new StringBuilder(pkg);
        sb.append(",");
        try {
            this.mOnehopService.send(2, sb.append(isEnabled).toString());
            SlogEx.i(TAG, "App switch sent by sink.");
        } catch (RemoteException e) {
            SlogEx.e(TAG, "One hop service can not reach.");
        }
    }

    public void syncAppSwitch(String data) {
        if (this.mIsSink || data == null || data.isEmpty()) {
            SlogEx.e(TAG, "handleMsgSyncAppSwitch not performed. isSink?" + this.mIsSink + " data?" + data);
            return;
        }
        String[] datSet = data.split(",");
        if (datSet.length != 2) {
            SlogEx.e(TAG, "Leak of data when synchronizing app switch.");
            return;
        }
        Optional.ofNullable(getVirtualContainer()).map($$Lambda$HwMagicWinManager$XvuOIsuCF4b3sdbkUWP9wWUlOc.INSTANCE).ifPresent(new Consumer(datSet[0], Boolean.parseBoolean(datSet[1])) {
            /* class com.android.server.wm.$$Lambda$HwMagicWinManager$3T5PLNjOJ5dKK3TUlsMXfUmTcAk */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                HwMagicWinManager.this.lambda$syncAppSwitch$2$HwMagicWinManager(this.f$1, this.f$2, (HwMagicWindowConfig) obj);
            }
        });
    }

    public /* synthetic */ void lambda$syncAppSwitch$2$HwMagicWinManager(String pkg, boolean isEnabled, HwMagicWindowConfig config) {
        config.onAppSwitchChanged(pkg, isEnabled);
        getAmsPolicy().removeRecentMagicWindowApp(pkg, getVirtualDisplayId());
        notifyPCAssistantStateChanged();
    }

    private void notifyPCAssistantStateChanged() {
        Optional<ActivityTaskManagerServiceEx> atmsExOpt = Optional.ofNullable(this.mSharedParameters).map($$Lambda$HwMagicWinManager$H14HHBeYd8rUwXp6BZZ7i52IwoY.INSTANCE).map($$Lambda$HwMagicWinManager$dtmjVmrQAAUiq5Bz2DnD51N9Q.INSTANCE);
        Bundle bundle = new Bundle();
        if (atmsExOpt.isPresent()) {
            bundle.putString("android.intent.extra.REASON", "magicAppStateChange");
            bundle.putInt("android.intent.extra.user_handle", atmsExOpt.get().getCurrentUserId());
            ActivityTaskManagerServiceEx atmsEx = atmsExOpt.get();
            try {
                Field atmsField = atmsEx.getClass().getDeclaredField("mATMS");
                atmsField.setAccessible(true);
                Optional<Class<?>> atmsClassOpt = Optional.ofNullable(atmsField.get(atmsEx)).map($$Lambda$HwMagicWinManager$85bLyePmy_vTZuCqiN7r4Ge_2lw.INSTANCE);
                if (atmsClassOpt.isPresent()) {
                    HwActivityTaskManagerServiceEx hwAtmsEx = (HwActivityTaskManagerServiceEx) atmsClassOpt.get().getField("mHwATMSEx").get(atmsField.get(atmsEx));
                    hwAtmsEx.getClass().getMethod("call", Bundle.class).invoke(hwAtmsEx, bundle);
                }
            } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
                SlogEx.e(TAG, "Notify PCAssistant state change exception.");
            }
        } else {
            SlogEx.w(TAG, "Notify PCAssistant state change fail.");
        }
    }

    public Animation getHwMagicWinAnimation(Animation animation, boolean enter, int transit, AppWindowTokenExt appWindowToken, Rect frame, boolean isAppLauncher) {
        if (appWindowToken == null || frame == null) {
            return animation;
        }
        return this.mWmsPolicy.getMagicAnimation(animation, appWindowToken, transit, enter, frame, isAppLauncher);
    }

    public HwMagicWindowUIController getUIController() {
        return this.mService.getUIController();
    }

    public void removeUninstallAppConfig(String pkgName, boolean isReplace) {
        this.mContainers.forEach(new BiConsumer(pkgName, isReplace) {
            /* class com.android.server.wm.$$Lambda$HwMagicWinManager$mMvMjyl2GveRsXDndoEOk_qsFag */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                Integer num = (Integer) obj;
                ((HwMagicContainer) obj2).getConfig().delAppConfigUninstall(this.f$0, this.f$1);
            }
        });
    }

    public void addInstallAppConfig(String pkgName) {
        this.mContainers.forEach(new BiConsumer(pkgName) {
            /* class com.android.server.wm.$$Lambda$HwMagicWinManager$NKzzPZ0PaoJft8h3ViwVVZ_gkGA */
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                Integer num = (Integer) obj;
                ((HwMagicContainer) obj2).getConfig().addAppConfigInstall(this.f$0);
            }
        });
    }

    public void dumpAppConfig(PrintWriter pw, String pkgName) {
        this.mContainers.forEach(new BiConsumer(pw, pkgName) {
            /* class com.android.server.wm.$$Lambda$HwMagicWinManager$Lb4y31kdzpTvJ4TeU4MfMHc4axY */
            private final /* synthetic */ PrintWriter f$0;
            private final /* synthetic */ String f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                HwMagicWinManager.lambda$dumpAppConfig$8(this.f$0, this.f$1, (Integer) obj, (HwMagicContainer) obj2);
            }
        });
    }

    static /* synthetic */ void lambda$dumpAppConfig$8(PrintWriter pw, String pkgName, Integer key, HwMagicContainer container) {
        pw.println("containerType=" + container.getType());
        container.getConfig().dump(pw, pkgName);
    }
}
