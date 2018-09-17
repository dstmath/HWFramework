package com.android.server.display;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.Settings.System;
import com.android.server.display.HwLightSensorController.LightSensorCallbacks;
import com.huawei.displayengine.DElog;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.displayengine.IDisplayEngineServiceEx.Stub;
import com.huawei.pgmng.plug.PGSdk;
import com.huawei.pgmng.plug.PGSdk.Sink;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DisplayEngineService extends Stub implements LightSensorCallbacks {
    private static final int BINDER_REBUILD_COUNT_MAX = 10;
    private static final String COLOR_MODE_SWITCH_PERMISSION = "com.huawei.android.permission.MANAGE_USERS";
    private static final String KEY_COLOR_MODE_SWITCH = "color_mode_switch";
    private static final int LIGHT_SENSOR_RATE_MILLS = 300;
    private static final int RETURN_PARAMETER_INVALID = -2;
    private static final String TAG = "DE J DisplayEngineService";
    private int mBinderRebuildCount = 0;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                DElog.e(DisplayEngineService.TAG, "Invalid input parameter!");
                return;
            }
            DElog.d(DisplayEngineService.TAG, "onReceive step in!");
            String action = intent.getAction();
            if (action != null && action.equals("huawei.intent.action.RGBW_CONFIG_ACTION")) {
                int ret = DisplayEngineService.this.getSupported(13);
                if (ret == 1) {
                    PersistableBundle data = new PersistableBundle();
                    data.putIntArray("Buffer", new int[1]);
                    data.putInt("BufferLength", 1);
                    DisplayEngineService.this.setEffect(13, 1, data);
                    DElog.d(DisplayEngineService.TAG, "setEffect feature rgbw mode param updata");
                } else {
                    DElog.e(DisplayEngineService.TAG, "current product not support rgbw and ret:" + ret);
                }
            }
        }
    };
    private ContentObserver mColorModeObserver = null;
    private ColorModeSwitchedReceiver mColorModeSwitchedReceiver = null;
    private final Context mContext;
    private final DisplayEngineHandler mHandler;
    private final HandlerThread mHandlerThread;
    private volatile boolean mIsBinderBuilding = false;
    private final HwLightSensorController mLightSensorController;
    private boolean mLightSensorEnable = false;
    private Object mLockBinderBuilding;
    private Object mLockService;
    private volatile IDisplayEngineService mNativeService;
    private volatile boolean mNativeServiceInitialized = false;
    private PGSdk mPGSdk = null;
    private volatile boolean mScreenOn = false;
    private final ScreenStateReceiver mScreenStateReceiver;
    private Sink mStateRecognitionListener = null;

    private class ColorModeSwitchedReceiver extends BroadcastReceiver {
        /* synthetic */ ColorModeSwitchedReceiver(DisplayEngineService this$0, ColorModeSwitchedReceiver -this1) {
            this();
        }

        private ColorModeSwitchedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                DElog.e(DisplayEngineService.TAG, "Invalid input parameter!");
                return;
            }
            if (intent.getAction().equals("android.intent.action.USER_SWITCHED")) {
                if (System.getIntForUser(DisplayEngineService.this.mContext.getContentResolver(), DisplayEngineService.KEY_COLOR_MODE_SWITCH, 1, -2) == 0) {
                    DisplayEngineService.this.setScene(13, 16);
                } else {
                    DisplayEngineService.this.setScene(13, 17);
                }
            }
            DisplayEngineService.this.initColorContentObserver();
        }
    }

    private final class DisplayEngineHandler extends Handler {
        public static final int BEGIN_POSITION = 1;
        public static final int END_POSITION = -1;
        public static final int IMAGE_EXIT = 1004;
        public static final int IMAGE_FULLSCREEN_VIEW = 1002;
        public static final int IMAGE_THUMBNAIL = 1003;
        public static final int VIDEO_FULLSCREEN_EXIT = 1001;
        public static final int VIDEO_FULLSCREEN_START = 1000;
        boolean mAlreadyHandle = false;
        int mSceneState = 0;
        Stack mVideoStack = new Stack();

        public DisplayEngineHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                DElog.e(DisplayEngineService.TAG, "msg is null!");
                return;
            }
            switch (msg.what) {
                case 2:
                    Bundle B = msg.getData();
                    String name = (String) B.get("SurfaceName");
                    String attachWin = (String) B.get("AttachWinName");
                    if (attachWin == null || !WhiteList.ATTACH_IMAGE_LIST.contains(attachWin) || !name.startsWith("PopupWindow")) {
                        if (!WhiteList.IMAGE_LIST.contains(name)) {
                            if (!WhiteList.VIDEO_LIST.contains(name)) {
                                if (WhiteList.VO_LIST.contains(name)) {
                                    setVoScene(B);
                                    break;
                                }
                            }
                            setVideoScene(B);
                            break;
                        }
                        setImageScene(B);
                        break;
                    }
                    setImageScene(B);
                    break;
                    break;
                default:
                    DElog.e(DisplayEngineService.TAG, "Invalid message");
                    break;
            }
        }

        private void getTimer() {
            DElog.d(DisplayEngineService.TAG, "getTimer step in!");
            if (!this.mAlreadyHandle) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        DisplayEngineHandler.this.handleVideoStack();
                    }
                }, 100);
                this.mAlreadyHandle = true;
            }
        }

        private void pushActionToStack(int action) {
            this.mVideoStack.push(Integer.valueOf(action));
            getTimer();
        }

        private void setVideoScene(Bundle B) {
            int frameLeft = B.getInt("FrameLeft");
            int frameRight = B.getInt("FrameRight");
            int frameTop = B.getInt("FrameTop");
            int frameBottom = B.getInt("FrameBottom");
            int displayWidth = B.getInt("DisplayWidth");
            int displayHeight = B.getInt("DisplayHeight");
            int position = B.getInt("Position");
            if (position == -1) {
                pushActionToStack(1001);
            } else if (position != 1) {
            } else {
                if (WhiteList.isFullScreen(frameLeft, frameTop, frameRight, frameBottom, displayWidth, displayHeight)) {
                    pushActionToStack(1000);
                } else {
                    pushActionToStack(1001);
                }
            }
        }

        private void setImageScene(Bundle B) {
            int frameLeft = B.getInt("FrameLeft");
            int frameRight = B.getInt("FrameRight");
            int frameTop = B.getInt("FrameTop");
            int frameBottom = B.getInt("FrameBottom");
            int displayWidth = B.getInt("DisplayWidth");
            int displayHeight = B.getInt("DisplayHeight");
            int position = B.getInt("Position");
            if (position == -1) {
                pushActionToStack(1004);
            } else if (position != 1) {
            } else {
                if (WhiteList.isFullScreen(frameLeft, frameTop, frameRight, frameBottom, displayWidth, displayHeight)) {
                    pushActionToStack(1002);
                } else {
                    pushActionToStack(1003);
                }
            }
        }

        private void setVoScene(Bundle B) {
            int position = B.getInt("Position");
            if (position == -1) {
                pushActionToStack(1001);
            } else if (position == 1) {
                pushActionToStack(1000);
            } else {
                DElog.e(DisplayEngineService.TAG, "[leilei] setVoScene position ERROR !");
            }
        }

        private void handleVideoStack() {
            DElog.d(DisplayEngineService.TAG, "handleVideoStack step in!");
            if (this.mVideoStack.empty()) {
                this.mAlreadyHandle = false;
                return;
            }
            if (this.mSceneState != ((Integer) this.mVideoStack.peek()).intValue()) {
                this.mSceneState = ((Integer) this.mVideoStack.peek()).intValue();
                sendToNativeScene(this.mSceneState);
            }
            while (!this.mVideoStack.empty()) {
                this.mVideoStack.pop();
            }
            this.mAlreadyHandle = false;
        }

        private int sendToNativeScene(int sceneState) {
            int ret;
            switch (sceneState) {
                case 1000:
                    return DisplayEngineService.this.setScene(1, 4);
                case 1001:
                    ret = DisplayEngineService.this.setScene(1, 8);
                    return DisplayEngineService.this.setDetailPGscene();
                case 1002:
                    return DisplayEngineService.this.setScene(3, 12);
                case 1003:
                    return DisplayEngineService.this.setScene(3, 12);
                case 1004:
                    ret = DisplayEngineService.this.setScene(3, 13);
                    return DisplayEngineService.this.setDetailPGscene();
                default:
                    return -1;
            }
        }
    }

    private class ScreenStateReceiver extends BroadcastReceiver {
        public ScreenStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.setPriority(1000);
            DisplayEngineService.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                DElog.e(DisplayEngineService.TAG, "Invalid input parameter!");
                return;
            }
            DElog.i(DisplayEngineService.TAG, "BroadcastReceiver.onReceive() action:" + intent.getAction());
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                DisplayEngineService.this.setScene(10, 17);
                DisplayEngineService.this.mScreenOn = false;
            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                DisplayEngineService.this.setScene(10, 16);
                DisplayEngineService.this.mScreenOn = true;
            } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                DisplayEngineService.this.registerPGSdk();
                DisplayEngineService.this.initColorModeSwitch();
                DisplayEngineService.this.registerRgbwBroadcast();
                DisplayEngineService.this.setScene(18, 16);
                DisplayEngineService.this.mScreenOn = true;
            }
        }
    }

    static final class WhiteList {
        static final List<String> ATTACH_IMAGE_LIST = new ArrayList<String>() {
            {
                add("com.tencent.mm/com.tencent.mm.plugin.profile.ui.ContactInfoUI");
            }
        };
        static final List<String> IMAGE_LIST = new ArrayList<String>() {
            {
                add("com.tencent.mm/com.tencent.mm.ui.chatting.gallery.ImageGalleryUI");
                add("com.tencent.mm/com.tencent.mm.plugin.sns.ui.SnsBrowseUI");
                add("com.tencent.mm/com.tencent.mm.plugin.sns.ui.SnsGalleryUI");
                add("com.tencent.mm/com.tencent.mm.plugin.subapp.ui.gallery.GestureGalleryUI");
                add("com.tencent.mm/com.tencent.mm.plugin.gallery.ui.ImagePreviewUI");
                add("com.tencent.mm/com.tencent.mm.plugin.setting.ui.setting.PreviewHdHeadImg");
                add("com.tencent.mobileqq/com.tencent.mobileqq.activity.aio.photo.AIOGalleryActivity");
                add("com.tencent.mobileqq/cooperation.qzone.QzonePicturePluginProxyActivity");
                add("com.tencent.mobileqq/com.tencent.mobileqq.activity.photo.PhotoPreviewActivity");
                add("com.tencent.mobileqq/com.tencent.mobileqq.activity.FriendProfileImageActivity");
                add("com.baidu.tieba/com.baidu.tieba.image.ImageViewerActivity");
                add("com.sina.weibo/com.sina.weibo.imageviewer.ImageViewer");
            }
        };
        static final List<String> VIDEO_LIST = new ArrayList<String>() {
            {
                add("SurfaceView - air.tv.douyu.android/tv.douyu.view.activity.PlayerActivity");
                add("SurfaceView - air.tv.douyu.android/tv.douyu.view.activity.VideoPlayerActivity");
                add("SurfaceView - air.tv.douyu.android/tv.douyu.view.activity.MobilePlayerActivity");
                add("SurfaceView - com.panda.videoliveplatform/com.panda.videoliveplatform.activity.LiveRoomActivity");
                add("SurfaceView - com.meelive.ingkee/com.meelive.ingkee.game.activity.RoomPlayerActivity");
                add("SurfaceView - com.meelive.ingkee/com.meelive.ingkee.ui.room.activity.RoomActivity");
                add("SurfaceView - com.duowan.kiwi/com.duowan.kiwi.channelpage.ChannelPage");
                add("SurfaceView - com.duowan.kiwi/com.duowan.kiwi.mobileliving.PortraitAwesomeLivingActivity");
                add("SurfaceView - com.duowan.kiwi/com.duowan.kiwi.recordervedio.VideoShowDetailActivity");
            }
        };
        static final List<String> VO_LIST = new ArrayList<String>() {
            {
                add("SurfaceView - com.tencent.mm/com.tencent.mm.plugin.voip.ui.VideoActivity");
                add("SurfaceView - com.tencent.mobileqq/com.tencent.av.ui.AVActivity");
            }
        };

        WhiteList() {
        }

        static boolean isFullScreen(int frameLeft, int frameTop, int frameRight, int frameBottom, int displayWidth, int displayHeight) {
            int l = Math.abs(frameLeft);
            int t = Math.abs(frameTop);
            int r = Math.abs(frameRight);
            int b = Math.abs(frameBottom);
            boolean isLandscape = l + r >= displayHeight + -100 && t + b >= displayWidth - 100;
            boolean isPortrait = l + r >= displayWidth + -100 && t + b >= displayHeight - 100;
            return !isLandscape ? isPortrait : true;
        }
    }

    public DisplayEngineService(Context context) {
        this.mContext = context;
        HwLightSensorController controller = null;
        SensorManager manager = (SensorManager) this.mContext.getSystemService("sensor");
        if (manager == null) {
            DElog.e(TAG, "Failed to get SensorManager:sensor");
        } else {
            controller = new HwLightSensorController(this, manager, 300);
        }
        this.mLightSensorController = controller;
        this.mScreenStateReceiver = new ScreenStateReceiver();
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new DisplayEngineHandler(this.mHandlerThread.getLooper());
        this.mNativeService = null;
        this.mLockService = new Object();
        this.mLockBinderBuilding = new Object();
        sendUIScene();
        initColorModeValue();
    }

    private IDisplayEngineService getNativeService() throws RemoteException {
        if (this.mNativeService == null && (this.mNativeServiceInitialized ^ 1) != 0) {
            synchronized (this.mLockService) {
                if (this.mNativeService == null && (this.mNativeServiceInitialized ^ 1) != 0) {
                    buildBinder();
                    if (this.mNativeService != null) {
                        this.mNativeServiceInitialized = true;
                    }
                }
            }
        }
        if (this.mNativeService != null || !this.mNativeServiceInitialized) {
            return this.mNativeService;
        }
        if (this.mBinderRebuildCount < 10) {
            throw new RemoteException("Try to rebuild binder " + this.mBinderRebuildCount + " times.");
        }
        throw new RemoteException("binder rebuilding failed!");
    }

    private void buildBinder() {
        IBinder binder = ServiceManager.getService("DisplayEngineService");
        if (binder != null) {
            this.mNativeService = IDisplayEngineService.Stub.asInterface(binder);
            if (this.mNativeService == null) {
                DElog.w(TAG, "service is null!");
                return;
            }
            return;
        }
        this.mNativeService = null;
        DElog.w(TAG, "binder is null!");
    }

    private void rebuildBinder() {
        DElog.i(TAG, "wait 800ms to rebuild binder...");
        SystemClock.sleep(800);
        DElog.i(TAG, "rebuild binder...");
        synchronized (this.mLockService) {
            buildBinder();
            if (this.mNativeService != null) {
                DElog.i(TAG, "rebuild binder success.");
                if (this.mScreenOn) {
                    setScene(10, 16);
                }
            } else {
                DElog.i(TAG, "rebuild binder failed!");
                this.mBinderRebuildCount++;
            }
        }
        synchronized (this.mLockBinderBuilding) {
            if (this.mBinderRebuildCount < 10) {
                this.mIsBinderBuilding = false;
            }
        }
    }

    private void rebuildBinderDelayed() {
        if (!this.mIsBinderBuilding) {
            synchronized (this.mLockBinderBuilding) {
                if (!this.mIsBinderBuilding) {
                    new Thread(new Runnable() {
                        public void run() {
                            DisplayEngineService.this.rebuildBinder();
                        }
                    }).start();
                    this.mIsBinderBuilding = true;
                }
            }
        }
    }

    public int getSupported(int feature) {
        try {
            IDisplayEngineService service = getNativeService();
            if (service != null) {
                return service.getSupported(feature);
            }
            return 0;
        } catch (RemoteException e) {
            DElog.e(TAG, "getSupported(" + feature + ") has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
            return 0;
        }
    }

    public int setScene(int scene, int action) {
        try {
            IDisplayEngineService service = getNativeService();
            if (service != null) {
                return service.setScene(scene, action);
            }
            return -1;
        } catch (RemoteException e) {
            DElog.e(TAG, "setScene(" + scene + ", " + action + ") has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
            return -1;
        }
    }

    public int setData(int type, PersistableBundle data) {
        try {
            IDisplayEngineService service = getNativeService();
            if (service == null) {
                return -1;
            }
            if (data != null) {
                return service.setData(type, data);
            }
            DElog.e(TAG, "setData(" + type + ", data): data is null!");
            return -2;
        } catch (RemoteException e) {
            DElog.e(TAG, "setData(" + type + ") has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
            return -1;
        }
    }

    public int sendMessage(int messageID, Bundle data) {
        if (data != null) {
            Message msg = this.mHandler.obtainMessage(messageID);
            msg.setData(data);
            this.mHandler.sendMessage(msg);
            return 0;
        }
        DElog.e(TAG, "sendMessage(" + messageID + ", data): data is null!");
        return -2;
    }

    public int getEffect(int feature, int type, byte[] status, int length) {
        try {
            IDisplayEngineService service = getNativeService();
            if (service == null) {
                return -1;
            }
            if (status != null && status.length == length) {
                return service.getEffect(feature, type, status, length);
            }
            DElog.e(TAG, "getEffect(" + feature + ", " + type + ", status, " + length + "): data is null or status.length != length!");
            return -2;
        } catch (RemoteException e) {
            DElog.e(TAG, "getEffect(" + feature + ", " + type + ", " + length + ") has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
            return -1;
        }
    }

    public int setEffect(int feature, int mode, PersistableBundle data) {
        try {
            IDisplayEngineService service = getNativeService();
            if (service == null) {
                return -1;
            }
            if (data != null) {
                return service.setEffect(feature, mode, data);
            }
            DElog.e(TAG, "setEffect(" + feature + ", " + mode + ", data): data is null!");
            return -2;
        } catch (RemoteException e) {
            DElog.e(TAG, "setEffect(" + feature + ", " + mode + ") has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
            return -1;
        }
    }

    public void updateLightSensorState(boolean sensorEnable) {
        enableLightSensor(sensorEnable);
        DElog.i(TAG, "LightSensorEnable=" + sensorEnable);
    }

    public void processSensorData(long timeInMs, int lux, int cct) {
        int[] ambientParam = new int[]{lux, cct};
        PersistableBundle bundle = new PersistableBundle();
        bundle.putIntArray("Buffer", ambientParam);
        bundle.putInt("BufferLength", 8);
        int ret = setData(9, bundle);
        if (ret != 0) {
            DElog.i(TAG, "processSensorData set Data Error: ret =" + ret);
        }
    }

    private void enableLightSensor(boolean enable) {
        if (this.mLightSensorEnable != enable && this.mLightSensorController != null) {
            this.mLightSensorEnable = enable;
            if (this.mLightSensorEnable) {
                this.mLightSensorController.enableSensor();
            } else {
                this.mLightSensorController.disableSensor();
            }
        }
    }

    private void initColorContentObserver() {
        if (this.mColorModeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mColorModeObserver);
        }
        this.mColorModeObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                if (System.getIntForUser(DisplayEngineService.this.mContext.getContentResolver(), DisplayEngineService.KEY_COLOR_MODE_SWITCH, 1, -2) == 0) {
                    DisplayEngineService.this.setScene(13, 16);
                } else {
                    DisplayEngineService.this.setScene(13, 17);
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_COLOR_MODE_SWITCH), true, this.mColorModeObserver, -2);
    }

    private void initColorModeSwitch() {
        if (this.mColorModeSwitchedReceiver == null) {
            this.mColorModeSwitchedReceiver = new ColorModeSwitchedReceiver(this, null);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_SWITCHED");
            this.mContext.registerReceiver(this.mColorModeSwitchedReceiver, filter, COLOR_MODE_SWITCH_PERMISSION, new Handler());
        }
        initColorContentObserver();
    }

    private void initColorModeValue() {
        if (System.getIntForUser(this.mContext.getContentResolver(), KEY_COLOR_MODE_SWITCH, 1, -2) == 0) {
            setScene(13, 16);
        } else {
            setScene(13, 17);
        }
    }

    private int setDetailPGscene(String pkg) {
        String pkgName = pkg;
        int scene = 0;
        if (pkg == null) {
            DElog.i(TAG, "pkg is null");
            return -1;
        } else if (this.mPGSdk == null) {
            DElog.i(TAG, "mPGSdk is null");
            return -1;
        } else {
            try {
                DElog.d(TAG, "getPkgType, pkgName: " + pkg);
                scene = this.mPGSdk.getPkgType(this.mContext, pkg);
            } catch (RemoteException ex) {
                DElog.e(TAG, "getPkgType", ex);
            }
            DElog.d(TAG, "PGSdk getPkgType, scene result:" + scene);
            return setScene(17, scene);
        }
    }

    private int setDetailPGscene() {
        String pkgName = getCurrentTopAppName();
        int scene = 0;
        if (pkgName == null) {
            DElog.i(TAG, "getCurrentTopAppName is null");
            return -1;
        } else if (this.mPGSdk == null) {
            DElog.i(TAG, "mPGSdk is null");
            return -1;
        } else {
            try {
                DElog.d(TAG, "getPkgType,pkgName: " + pkgName);
                scene = this.mPGSdk.getPkgType(this.mContext, pkgName);
            } catch (RemoteException ex) {
                DElog.e(TAG, "getPkgType", ex);
            }
            DElog.d(TAG, "PGSdk getPkgType, scene result:" + scene);
            return setScene(17, scene);
        }
    }

    private String getCurrentTopAppName() {
        try {
            List<RunningTaskInfo> runningTasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (runningTasks == null || runningTasks.isEmpty()) {
                return null;
            }
            return ((RunningTaskInfo) runningTasks.get(0)).topActivity.getPackageName();
        } catch (SecurityException e) {
            DElog.e(TAG, "getCurrentTopAppName() failed to get topActivity PackageName " + e);
            return null;
        }
    }

    private void initPGSdkState() {
        if (this.mPGSdk == null) {
            DElog.i(TAG, "mPGSdk is null");
        } else if (this.mStateRecognitionListener == null) {
            DElog.i(TAG, "mStateRecognitionListener is null");
        } else {
            try {
                DElog.d(TAG, "enableStateEvent step in!");
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(10000).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_EBOOK_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_INPUT_START).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_INPUT_END).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_OFFICE_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_MMS_FRONT).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_VIDEO_START).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_VIDEO_END).intValue());
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, Integer.valueOf(IDisplayEngineService.DE_ACTION_PG_CAMERA_END).intValue());
            } catch (RemoteException ex) {
                DElog.e(TAG, "enableStateEvent", ex);
            }
        }
    }

    private void registerPGSdk() {
        if (this.mPGSdk == null) {
            DElog.d(TAG, "mPGSdk constructor ok");
            this.mPGSdk = PGSdk.getInstance();
        }
        if (this.mStateRecognitionListener == null) {
            DElog.d(TAG, "mStateRecognitionListener constructor ok");
            this.mStateRecognitionListener = new Sink() {
                public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
                    DElog.d(DisplayEngineService.TAG, "state type: " + stateType + " eventType:" + eventType + " pid:" + pid + " pkd:" + pkg + " uid:" + uid);
                    if (stateType == 10000) {
                        DisplayEngineService.this.setDetailPGscene(pkg);
                    } else if (stateType == IDisplayEngineService.DE_ACTION_PG_VIDEO_END || stateType == IDisplayEngineService.DE_ACTION_PG_CAMERA_END) {
                        DisplayEngineService.this.setScene(0, stateType);
                        DisplayEngineService.this.setDetailPGscene(pkg);
                    } else {
                        DisplayEngineService.this.setScene(0, stateType);
                    }
                }
            };
        }
        initPGSdkState();
    }

    private void sendUIScene() {
        setScene(10, 16);
        this.mScreenOn = true;
    }

    private void registerRgbwBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("huawei.intent.action.RGBW_CONFIG_ACTION");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, null);
    }
}
