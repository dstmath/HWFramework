package com.android.server.foldscreenview;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.ContentObserver;
import android.hardware.camera2.CameraManager;
import android.hardware.display.HwFoldScreenState;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.server.LocalServices;
import com.android.server.gesture.DefaultGestureNavConst;
import com.android.server.gesture.DefaultGestureNavManager;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.input.InputManagerServiceEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.statistical.StatisticalUtils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.server.HwBasicPlatformFactory;
import java.util.ArrayList;
import java.util.List;

public class SubScreenViewEntry extends BroadcastReceiver {
    private static final String ACTION_INCALL_SCREEN = "InCallScreenIsForegroundActivity";
    private static final String CAMERA_MAIN_ACTIVITY = "com.huawei.camera.controller.CameraActivity";
    private static final String CAMERA_PACKAGENAME = "com.huawei.camera";
    private static final int DEFAULT_SUB_DISPLAY_SIDE = 1;
    private static final int DELAYED_TIME = 100;
    private static final int DELAY_TIMEING_CAMERA_CHECK = 150;
    private static final int DELAY_TIMING_GESTURE_NAVI_UPDATE = 300;
    private static final String FOCUS_PACKAGE_ID = "focusPackageName";
    private static final String FRONT_CAMERA = "1";
    private static final int FULL_HEIGHT = HwFoldScreenState.getScreenPhysicalRect(1).height();
    private static final int ILLEGAL_PID = -1;
    private static final String INCALL_BROADCAST_PERMIS = "com.android.systemui.permission.BackgrounCallingLayout";
    private static final int INTELLIGENT_AWAKEN_TURN_ON = 1;
    private static final String KEY_STRING_VIDEO_CALL = "IsVideoCall";
    private static final int MAIN_SCREEN_DISPLAY = 2;
    private static final int MAIN_SCREEN_WIDTH = HwFoldScreenState.getScreenPhysicalRect(2).width();
    private static final int MSG_AFT_POLICY_CHANGED = 14;
    private static final int MSG_CONFIGURATION_CHANGED = 9;
    private static final int MSG_CREAT_SUB_SCREEN_VIEW = 1;
    private static final int MSG_DISPLAY_CHANGE_TO_SUB_MODE = 3;
    private static final int MSG_HANDLE_LANGUANG_CHANGED = 5;
    private static final int MSG_REMOVE_APP_VIEW = 8;
    private static final int MSG_REMOVE_SUB_SCREEN_VIEW = 4;
    private static final int MSG_ROTATION_CHANGED = 2;
    private static final int MSG_SET_VISIBILITY_SUB_SCREEN_VIEW = 6;
    private static final int MSG_UPDATA_CREATE_APP_VIEW = 7;
    private static final int MSG_UPDATE_CALL_STATE_CHANGE = 13;
    private static final int MSG_UPDATE_FSM_INTELLIGEN_STATE = 11;
    private static final int MSG_UPDATE_GESTURE_NAV_GLOBAL_STATE = 10;
    private static final String NEED_ADD_BUTTON = "needAddBtnView";
    private static final String PACKAGR_NAME = "packageName";
    private static final int PLAY_ANIMATION_FOR_CALL_AUTO_FLIP = 1;
    private static final int PLAY_ANIMATION_FOR_USER_CLICK = 2;
    private static final String REAR_CAMERA = "0";
    private static final String REMOTE_VIEW_ID = "remoteView";
    private static final int REMOTE_VIEW_LAYOUT_INDEX = 0;
    private static final String REMOTE_VIEW_PID = "remoteViewPid";
    private static final int SIDE_LEFT_BOTTOM = 2;
    private static final int SIDE_LEFT_TOP = 1;
    private static final int SIDE_NONE = 0;
    private static final int SIDE_RIGHT_BOTTOM = 3;
    private static final int SIDE_RIGHT_TOP = 4;
    private static final float SUB_DISPLAY_SCALE;
    private static final int SUB_SCREEN_WIDTH = HwFoldScreenState.getScreenPhysicalRect(3).width();
    private static final String TAG = "FoldScreen_SubScreenViewEntry";
    private static final int TYPE_SUB_DISPLAY = 3;
    private static final int VIEW_HEIGHT = ((int) ((1.0f - SUB_DISPLAY_SCALE) * ((float) FULL_HEIGHT)));
    private static final int VIEW_WIDTH;
    private final Runnable delayUpdateSubScreenView = new Runnable() {
        /* class com.android.server.foldscreenview.SubScreenViewEntry.AnonymousClass4 */

        @Override // java.lang.Runnable
        public void run() {
            if (HwFoldScreenManagerEx.getDisplayMode() == 3) {
                SubScreenViewEntry.this.mHandler.sendEmptyMessage(1);
            }
        }
    };
    private final Runnable delayUpdateVisibilityForCamaraChange = new Runnable() {
        /* class com.android.server.foldscreenview.SubScreenViewEntry.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            SubScreenViewEntry.this.mHandler.sendEmptyMessage(6);
        }
    };
    private ActivityLifeStateMonitor mActivityLifeStateMonitor;
    private AppSwitchMonitor mAppSwitchMonitor;
    private CameraManager.AvailabilityCallback mAvailabilityCallback;
    private ImageView mBackButtonView;
    private String mCameraAppName = null;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.foldscreenview.SubScreenViewEntry.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            SubScreenViewEntry.this.mHandler.sendEmptyMessage(11);
        }
    };
    private Context mContext;
    private int mCurrentRotation;
    private TextView mDisplayModeChangeButton;
    private LinearLayout mFlipButtonClockZone;
    private String mForegroundAppName = null;
    private int mForegroundPid;
    private final HwFoldScreenManagerEx.FoldFsmTipsRequestListener mFsmTipsRemoveListener = new HwFoldScreenManagerEx.FoldFsmTipsRequestListener() {
        /* class com.android.server.foldscreenview.SubScreenViewEntry.AnonymousClass2 */

        public void onRequestFsmTips(int reqTipsType, Bundle data) {
            Log.d(SubScreenViewEntry.TAG, "onRequestFsmTips, tipsType = " + reqTipsType);
            if (data.getInt("KEY_TIPS_INT_REMOVED_REASON") != 1) {
                SubScreenViewEntry.this.onFsmTipRemoved();
            }
        }
    };
    private DefaultGestureNavManager mGestureNavPolicy;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private ImageView mHomeButtonView;
    private InputManagerServiceEx.DefaultHwInputManagerLocalService mHwInputManagerInternal;
    private ImageView mImageView;
    private boolean mIsActivatedCamera = false;
    private boolean mIsCallStateOffHook = false;
    private boolean mIsCallStateRinging = false;
    private boolean mIsCloseMobileViewShowed = false;
    private boolean mIsEarpieceIncall;
    private boolean mIsFirstLockScreenCalled = true;
    private boolean mIsGestureNaviEnabled = false;
    private boolean mIsIntelligentOn = false;
    private boolean mIsLockScreenShowed = false;
    private boolean mIsOccluded = false;
    private boolean mIsStartupGuideEnable = false;
    private boolean mIsSubScreenViewShowed = false;
    private boolean mIsTelephonyCallFront = false;
    private boolean mIsVideoCall = false;
    private LinearLayout mLinearLayout;
    private PhoneStateListener mMsimPhoneStateListenerCard0 = null;
    private PhoneStateListener mMsimPhoneStateListenerCard1 = null;
    private LinearLayout mNavigationLayout;
    private View mOldAppView;
    private int mOldCallingPid = -1;
    private RemoteViews mOldRemoteViews;
    private ImageView mRecentButtonView;
    private RelativeLayout mRelativeLayout;
    private String mShowingCameraId;
    private View mSubScreenView;
    private TelephonyManager mTelephonyManager;
    private int mUseEarpiecePid = -1;
    private TextView mViewContent;
    private WindowManager mWindowManager;
    private int mWindowWidth = 0;
    private WindowManager.LayoutParams mWlParams;

    static {
        int i = SUB_SCREEN_WIDTH;
        SUB_DISPLAY_SCALE = (((float) i) * 1.0f) / (((float) MAIN_SCREEN_WIDTH) * 1.0f);
        VIEW_WIDTH = i;
    }

    public SubScreenViewEntry(Context context) {
        this.mContext = context;
        Object service = this.mContext.getSystemService("window");
        if (service instanceof WindowManager) {
            this.mWindowManager = (WindowManager) service;
        }
    }

    public void init() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new SubScreenViewHandler(this.mHandlerThread.getLooper());
        this.mGestureNavPolicy = (DefaultGestureNavManager) LocalServices.getService(DefaultGestureNavManager.class);
        this.mHwInputManagerInternal = (InputManagerServiceEx.DefaultHwInputManagerLocalService) LocalServices.getService(InputManagerServiceEx.DefaultHwInputManagerLocalService.class);
        this.mWlParams = new WindowManager.LayoutParams(VIEW_WIDTH, VIEW_HEIGHT, 0, 0, HwArbitrationDEFS.MSG_VPN_STATE_OPEN, 776, -1);
        WindowManager.LayoutParams layoutParams = this.mWlParams;
        layoutParams.gravity = 8388659;
        layoutParams.layoutInDisplayCutoutMode = 1;
        layoutParams.setTitle(TAG);
        this.mActivityLifeStateMonitor = new ActivityLifeStateMonitor(this);
        this.mAppSwitchMonitor = new AppSwitchMonitor(this);
        this.mActivityLifeStateMonitor.start();
        this.mAppSwitchMonitor.start();
        initCameraCallback();
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.LOCALE_CHANGED");
            filter.addAction(ACTION_INCALL_SCREEN);
            this.mContext.registerReceiver(this, filter, INCALL_BROADCAST_PERMIS, null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "registerReceiver fail");
        }
        this.mCurrentRotation = 0;
        initGastureNaviObserver();
        initFsmIntelligentObserver();
        registerCallStateListener();
        HwFoldScreenManagerEx.registerFsmTipsRequestListener(this.mFsmTipsRemoveListener, 4);
    }

    private void initCameraCallback() {
        this.mAvailabilityCallback = new CameraManager.AvailabilityCallback() {
            /* class com.android.server.foldscreenview.SubScreenViewEntry.AnonymousClass5 */

            @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
            public void onCameraAvailable(String cameraId) {
                if ("0".equals(cameraId) || "1".equals(cameraId)) {
                    Log.i(SubScreenViewEntry.TAG, "onCameraAvailable " + cameraId);
                    SubScreenViewEntry.this.processCameraAvailable(cameraId);
                }
            }

            @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
            public void onCameraUnavailable(String cameraId) {
                if ("0".equals(cameraId) || "1".equals(cameraId)) {
                    Log.i(SubScreenViewEntry.TAG, "onCameraUnavailable " + cameraId);
                    SubScreenViewEntry.this.processCameraUnavailable(cameraId);
                }
            }
        };
        Object service = this.mContext.getSystemService("camera");
        if (service instanceof CameraManager) {
            ((CameraManager) service).registerAvailabilityCallback(this.mAvailabilityCallback, (Handler) null);
        }
    }

    private void initGastureNaviObserver() {
        this.mIsGestureNaviEnabled = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getGestureNavConst().getGestureNavEnabled(this.mContext, -2);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(DefaultGestureNavConst.KEY_SECURE_GESTURE_NAVIGATION), false, new ContentObserver(new Handler()) {
            /* class com.android.server.foldscreenview.SubScreenViewEntry.AnonymousClass6 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                Log.i(SubScreenViewEntry.TAG, "setting gesture nav status change");
                SubScreenViewEntry.this.mHandler.sendEmptyMessage(10);
            }
        }, -1);
    }

    public void updateAppView(RemoteViews remoteViews, String focusPackageName) {
        if (remoteViews != null) {
            int remoteViewPid = Binder.getCallingPid();
            Bundle bundle = new Bundle();
            bundle.putInt(REMOTE_VIEW_PID, remoteViewPid);
            bundle.putString(FOCUS_PACKAGE_ID, focusPackageName);
            bundle.putParcelable(REMOTE_VIEW_ID, remoteViews);
            Message msg = this.mHandler.obtainMessage();
            msg.setData(bundle);
            msg.what = 7;
            Log.i(TAG, "updateAppView remoteViewPid: " + remoteViewPid);
            this.mHandler.sendMessage(msg);
        }
    }

    public void removeAppView(boolean isNeedAddBtnView) {
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        Log.i(TAG, "removeAppView packageName: " + packageName + ", isNeedAddBtnView: " + isNeedAddBtnView);
        Bundle bundle = new Bundle();
        bundle.putString("packageName", packageName);
        bundle.putInt(NEED_ADD_BUTTON, isNeedAddBtnView ? 1 : 0);
        Message msg = this.mHandler.obtainMessage();
        msg.setData(bundle);
        msg.what = 8;
        this.mHandler.sendMessage(msg);
    }

    public int getRemoteViewsPid() {
        if (this.mSubScreenView == null || this.mOldRemoteViews == null) {
            return -1;
        }
        Log.i(TAG, " RemoteViewsPid: " + this.mOldCallingPid);
        return this.mOldCallingPid;
    }

    public void handleAppDiedForRemoteViews() {
        if (this.mOldRemoteViews != null) {
            Log.i(TAG, "handleAppDiedForRemoteViews");
            Bundle bundle = new Bundle();
            bundle.putString("packageName", this.mOldRemoteViews.getPackage());
            Message msg = this.mHandler.obtainMessage();
            msg.setData(bundle);
            msg.what = 8;
            this.mHandler.sendMessage(msg);
        }
    }

    public void handleSwitchingUserForRemoteViews() {
        Log.i(TAG, "handleSwitchingUserForRemoteViews");
        this.mActivityLifeStateMonitor.start();
        this.mActivityLifeStateMonitor.setStartupGuideFlag(true);
        this.mHandler.sendEmptyMessage(10);
        this.mHandler.sendEmptyMessage(11);
        if (this.mOldRemoteViews != null) {
            Bundle bundle = new Bundle();
            bundle.putString("packageName", this.mOldRemoteViews.getPackage());
            Message msg = this.mHandler.obtainMessage();
            msg.setData(bundle);
            msg.what = 8;
            this.mHandler.sendMessage(msg);
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent != null && this.mHandler != null) {
            String action = intent.getAction();
            if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                this.mHandler.sendEmptyMessage(5);
            } else if (ACTION_INCALL_SCREEN.equals(action)) {
                this.mIsTelephonyCallFront = intent.getBooleanExtra("IsForegroundActivity", false);
                Log.i(TAG, "onReceive-->mIsTelephonyCallFront: " + this.mIsTelephonyCallFront + " mIsCallStateRinging:" + this.mIsCallStateRinging + " mIsCallStateOffHook:" + this.mIsCallStateOffHook);
                this.mIsVideoCall = intent.getBooleanExtra(KEY_STRING_VIDEO_CALL, false);
                StringBuilder sb = new StringBuilder();
                sb.append("check video call = ");
                sb.append(this.mIsVideoCall);
                Log.i(TAG, sb.toString());
                if (HwFoldScreenManagerEx.getDisplayMode() == 3 && !this.mIsVideoCall) {
                    this.mHandler.sendEmptyMessage(13);
                }
            } else {
                Log.i(TAG, "nothing need to do");
            }
        }
    }

    public void onRotationChanged(int rotation) {
        Handler handler = this.mHandler;
        if (handler != null) {
            this.mHandler.sendMessageDelayed(handler.obtainMessage(2, rotation, 0), 100);
        }
    }

    public void onConfigurationChanged() {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendEmptyMessage(9);
        }
    }

    public void handleDisplayModeChangeBefore(int displayMode) {
        Log.i(TAG, "handle before->the current display mode: " + displayMode + " mSubScreenView: " + this.mSubScreenView);
        Handler handler = this.mHandler;
        if (handler != null) {
            if (displayMode == 3) {
                handler.sendEmptyMessage(3);
            } else {
                handler.sendEmptyMessage(4);
            }
        }
    }

    public void handleStartupGuideChanged(boolean isShowStartupGuide) {
        Log.i(TAG, "handleStartupGuideChanged isShowStartupGuide: " + isShowStartupGuide);
        this.mIsStartupGuideEnable = isShowStartupGuide;
        this.mIsSubScreenViewShowed = true;
        if (HwFoldScreenManagerEx.getDisplayMode() == 3) {
            if (isShowStartupGuide) {
                this.mHandler.sendEmptyMessage(1);
            } else {
                this.mHandler.sendEmptyMessage(6);
            }
        }
    }

    public void handleLockScreenShowChanged(boolean isShowing) {
        Log.i(TAG, "handleLockScreenShowChanged isShowing: " + isShowing);
        this.mIsLockScreenShowed = isShowing;
        this.mIsSubScreenViewShowed = true;
        if (HwFoldScreenManagerEx.getDisplayMode() == 3) {
            if (this.mIsFirstLockScreenCalled) {
                this.mIsFirstLockScreenCalled = false;
                this.mHandler.sendEmptyMessage(1);
            }
            this.mHandler.sendEmptyMessage(6);
        }
    }

    public void handleCloseMobileViewChanged(boolean isCloseMobileView) {
        Log.i(TAG, "handleCloseMobileViewChanged isCloseMobileView: " + isCloseMobileView);
        this.mIsCloseMobileViewShowed = isCloseMobileView;
        this.mHandler.sendEmptyMessage(6);
    }

    public void handleActivatedCamera(boolean isActivatedCamera) {
        Log.i(TAG, "handleActivatedCamera----isActivatedCamera: " + isActivatedCamera);
        this.mIsActivatedCamera = isActivatedCamera;
        if (this.mIsActivatedCamera) {
            this.mHandler.sendEmptyMessage(6);
        } else if (this.mShowingCameraId == null) {
            this.mHandler.sendEmptyMessage(6);
        } else {
            Log.i(TAG, "nothing need to do");
        }
    }

    public void handleForegroundAppChanged(int targetPid, String targetPackageName) {
        int i;
        this.mForegroundPid = targetPid;
        this.mForegroundAppName = targetPackageName;
        Log.i(TAG, "mForegroundPid: " + this.mForegroundPid + " mForegroundAppName: " + this.mForegroundAppName + " mUseEarpiecePid: " + this.mUseEarpiecePid + ", mIsVideoCall = " + this.mIsVideoCall);
        String str = this.mForegroundAppName;
        if (str != null && !str.equals(this.mCameraAppName) && this.mUseEarpiecePid != -1 && (i = this.mForegroundPid) != 0 && getAppNameByPid(i).equals(getAppNameByPid(this.mUseEarpiecePid)) && !checkHeadSetIsConnected() && !this.mIsVideoCall) {
            requestShowFlipTipsAndFlip(1);
        }
    }

    public void handleOccludedChanged(boolean isOccluded) {
        Log.i(TAG, "handleOccludedChanged: mIsOccluded: " + this.mIsOccluded + " isOccluded: " + isOccluded);
        if (this.mIsOccluded != isOccluded) {
            this.mIsOccluded = isOccluded;
            this.mHandler.sendEmptyMessage(6);
        }
    }

    public void notifyUpdateAftPolicy(int ownerPid, int mode) {
        Handler handler = this.mHandler;
        if (handler != null) {
            this.mHandler.sendMessage(handler.obtainMessage(14, ownerPid, mode));
        }
    }

    private class DisplayModeListener implements HwFoldScreenManagerEx.FoldDisplayModeListener {
        private DisplayModeListener() {
        }

        public void onScreenDisplayModeChange(int displayMode) {
            Log.i(SubScreenViewEntry.TAG, "dispaly mode change->the current display mode: " + displayMode + " mSubScreenView: " + SubScreenViewEntry.this.mSubScreenView);
            if (SubScreenViewEntry.this.mHandler != null) {
                if (displayMode == 3) {
                    SubScreenViewEntry.this.mHandler.sendEmptyMessage(3);
                } else {
                    SubScreenViewEntry.this.mHandler.sendEmptyMessage(4);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processCameraUnavailable(String cameraId) {
        this.mShowingCameraId = cameraId;
        this.mCameraAppName = getTopPackageName();
        Log.i(TAG, "processCameraUnavailable: " + this.mCameraAppName);
        if (!CAMERA_PACKAGENAME.equals(this.mCameraAppName)) {
            this.mHandler.removeCallbacks(this.delayUpdateVisibilityForCamaraChange);
            this.mHandler.postDelayed(this.delayUpdateVisibilityForCamaraChange, 150);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processCameraAvailable(String cameraId) {
        this.mShowingCameraId = null;
        if (CAMERA_PACKAGENAME.equals(this.mCameraAppName)) {
            this.mHandler.sendEmptyMessage(6);
        } else {
            this.mHandler.removeCallbacks(this.delayUpdateVisibilityForCamaraChange);
            this.mHandler.postDelayed(this.delayUpdateVisibilityForCamaraChange, 150);
        }
        this.mCameraAppName = null;
    }

    /* access modifiers changed from: private */
    public final class SubScreenViewHandler extends Handler {
        SubScreenViewHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean isNeedAddBtnView = false;
            switch (msg.what) {
                case 1:
                case 3:
                    SubScreenViewEntry.this.createSubScreenView();
                    SubScreenViewEntry.this.createSubScreenNavView();
                    SubScreenViewEntry.this.enbaleInputScaleMode(true);
                    return;
                case 2:
                    SubScreenViewEntry.this.updateSubScreenView(msg.arg1);
                    SubScreenViewEntry.this.updateInputScaleMode(msg.arg1);
                    return;
                case 4:
                    SubScreenViewEntry.this.removeSubScreenView();
                    SubScreenViewEntry.this.destroySubScreenNavView();
                    SubScreenViewEntry.this.enbaleInputScaleMode(false);
                    return;
                case 5:
                    SubScreenViewEntry.this.handleLanguageChanged();
                    return;
                case 6:
                    SubScreenViewEntry.this.setVisibilitySubScreenView();
                    return;
                case 7:
                    Bundle bundle = msg.getData();
                    Parcelable parcelable = bundle.getParcelable(SubScreenViewEntry.REMOTE_VIEW_ID);
                    RemoteViews remoteViews = null;
                    if (parcelable instanceof RemoteViews) {
                        remoteViews = (RemoteViews) parcelable;
                    }
                    SubScreenViewEntry.this.handleUpdateAndCreateAppView(remoteViews, bundle.getString(SubScreenViewEntry.FOCUS_PACKAGE_ID), bundle.getInt(SubScreenViewEntry.REMOTE_VIEW_PID));
                    return;
                case 8:
                    Bundle bundleDate = msg.getData();
                    String packageName = bundleDate.getString("packageName");
                    if (bundleDate.getInt(SubScreenViewEntry.NEED_ADD_BUTTON, 1) == 1) {
                        isNeedAddBtnView = true;
                    }
                    SubScreenViewEntry.this.handleRemoveAppView(packageName, isNeedAddBtnView);
                    return;
                case 9:
                    SubScreenViewEntry.this.handleConfigurationChanged();
                    return;
                case 10:
                    SubScreenViewEntry.this.handleUpdateGestureNavState();
                    return;
                case 11:
                    SubScreenViewEntry.this.handleUpdateFsmIntelligentState();
                    return;
                case 12:
                default:
                    return;
                case 13:
                    SubScreenViewEntry.this.handleUpdateCallStateChange();
                    return;
                case 14:
                    SubScreenViewEntry.this.handleAftPolicyChange(msg.arg1, msg.arg2);
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateAndCreateAppView(RemoteViews remoteViews, String focusPackageName, int callingPid) {
        if (this.mSubScreenView != null && remoteViews != null) {
            RemoteViews remoteViews2 = this.mOldRemoteViews;
            if (remoteViews2 == null) {
                updateAppViewLocked(remoteViews, focusPackageName, callingPid);
                return;
            }
            String oldPkgName = remoteViews2.getPackage();
            String newPkgName = remoteViews.getPackage();
            Log.i(TAG, "oldPkgName: " + oldPkgName + " newPkgName: " + newPkgName);
            if (oldPkgName != null && oldPkgName.equals(newPkgName)) {
                updateAppViewLocked(remoteViews, focusPackageName, callingPid);
            }
        }
    }

    private void updateAppViewLocked(RemoteViews remoteViews, String focusPackageName, int callingPid) {
        Log.i(TAG, "updateAppViewLocked mIsLockScreenShowed:" + this.mIsLockScreenShowed + " mIsActivatedCamera: " + this.mIsActivatedCamera);
        View appView = remoteViews.apply(this.mContext, this.mRelativeLayout, null);
        this.mLinearLayout.setVisibility(4);
        this.mRelativeLayout.addView(appView, 0);
        View view = this.mOldAppView;
        if (view == null) {
            this.mRelativeLayout.removeView(this.mLinearLayout);
        } else {
            this.mRelativeLayout.removeView(view);
        }
        Log.i(TAG, "updateAppViewLocked---mIsStartupGuideEnable: " + this.mIsStartupGuideEnable + " mIsLockScreenShowed: " + this.mIsLockScreenShowed + " mIsCloseMobileViewShowed: " + this.mIsCloseMobileViewShowed + " mIsOccluded: " + this.mIsOccluded);
        boolean z = true;
        boolean isInvisible = this.mIsStartupGuideEnable || (!this.mIsOccluded && this.mIsLockScreenShowed) || this.mIsCloseMobileViewShowed;
        if (isInvisible) {
            appView.setVisibility(4);
        } else {
            appView.setVisibility(0);
        }
        this.mRelativeLayout.setVisibility(0);
        if (isInvisible) {
            z = false;
        }
        updateNavigationLayout(z);
        this.mOldAppView = appView;
        this.mOldCallingPid = callingPid;
        this.mOldRemoteViews = remoteViews;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRemoveAppView(String packageName, boolean isNeedAddBtnView) {
        RemoteViews remoteViews;
        if (this.mSubScreenView != null && packageName != null && (remoteViews = this.mOldRemoteViews) != null && packageName.equals(remoteViews.getPackage())) {
            if (this.mLinearLayout.getParent() == null) {
                this.mRelativeLayout.addView(this.mLinearLayout);
            }
            View view = this.mOldAppView;
            if (!(view == null || this.mOldRemoteViews == null)) {
                this.mRelativeLayout.removeView(view);
            }
            Log.i(TAG, "handleRemoveAppView---mIsStartupGuideEnable: " + this.mIsStartupGuideEnable + " mIsLockScreenShowed: " + this.mIsLockScreenShowed + " mIsCloseMobileViewShowed: " + this.mIsCloseMobileViewShowed + " isNeedAddBtnView" + isNeedAddBtnView);
            boolean isInvisible = this.mIsStartupGuideEnable || this.mIsLockScreenShowed || this.mIsCloseMobileViewShowed;
            if (!isNeedAddBtnView || isInvisible || (this.mShowingCameraId == null && this.mIsActivatedCamera)) {
                this.mLinearLayout.setVisibility(4);
            } else {
                this.mLinearLayout.setVisibility(0);
            }
            this.mRelativeLayout.setVisibility(0);
            this.mOldAppView = null;
            this.mOldCallingPid = -1;
            this.mOldRemoteViews = null;
        }
    }

    private void initSubScreenView(int rotation) {
        int windowId;
        if (this.mIsTelephonyCallFront && (this.mIsCallStateRinging || this.mIsCallStateOffHook)) {
            initSubScreenViewForCallTips(rotation);
        } else if (!this.mIsIntelligentOn) {
            initSubScreenViewForFsmIntelTurnOff(rotation);
        } else {
            View view = this.mSubScreenView;
            if (view != null) {
                this.mWindowManager.removeView(view);
                this.mSubScreenView = null;
            }
            if (rotation == 0) {
                windowId = 34013438;
            } else if (rotation == 1) {
                windowId = 34013460;
            } else if (rotation == 3) {
                windowId = 34013459;
            } else {
                windowId = 34013458;
            }
            this.mCurrentRotation = rotation;
            this.mSubScreenView = LayoutInflater.from(this.mContext).inflate(windowId, (ViewGroup) null);
            this.mRelativeLayout = (RelativeLayout) this.mSubScreenView.findViewById(34603505);
            this.mLinearLayout = (LinearLayout) this.mSubScreenView.findViewById(34603502);
            this.mViewContent = (TextView) this.mSubScreenView.findViewById(34603390);
            this.mImageView = (ImageView) this.mSubScreenView.findViewById(34603501);
            this.mImageView.setOnClickListener(new View.OnClickListener() {
                /* class com.android.server.foldscreenview.SubScreenViewEntry.AnonymousClass7 */

                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    Log.i(SubScreenViewEntry.TAG, "start camera activity");
                    StatisticalUtils.reporte(SubScreenViewEntry.this.mContext, 10102, "extreme_selfie");
                    SubScreenViewEntry.this.startCameraActivity();
                }
            });
            this.mNavigationLayout = (LinearLayout) this.mSubScreenView.findViewById(34603503);
            this.mBackButtonView = (ImageView) this.mSubScreenView.findViewById(34603498);
            this.mHomeButtonView = (ImageView) this.mSubScreenView.findViewById(34603500);
            this.mRecentButtonView = (ImageView) this.mSubScreenView.findViewById(34603504);
            new NavigationButton(this.mContext, this.mBackButtonView, 4);
            new NavigationButton(this.mContext, this.mHomeButtonView, 3);
            new NavigationButton(this.mContext, this.mRecentButtonView, 187);
        }
    }

    private void updateNavigationLayout(boolean isVisible) {
        LinearLayout linearLayout;
        if (this.mSubScreenView != null && (linearLayout = this.mNavigationLayout) != null) {
            if (!isVisible || this.mIsGestureNaviEnabled) {
                this.mNavigationLayout.setVisibility(8);
            } else {
                linearLayout.setVisibility(0);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createSubScreenView() {
        if (!this.mIsSubScreenViewShowed) {
            Log.i(TAG, "cannot create subScreen view");
        } else if (this.mSubScreenView != null) {
            setVisibilitySubScreenView();
            Log.i(TAG, "have the existed mSubScreenView: " + this.mSubScreenView);
        } else if (HwFoldScreenManagerEx.getDisplayMode() != 3) {
            Log.i(TAG, "the current is sub screen");
        } else {
            int rotation = this.mWindowManager.getDefaultDisplay().getRotation();
            updateViewParameter(rotation);
            initSubScreenView(rotation);
            Log.d(TAG, "Create Subview [ xPosition: " + this.mWlParams.x + ", yPosition: " + this.mWlParams.y + ", viewHeigh: " + this.mWlParams.height + ", viewWidth: " + this.mWlParams.width + "]");
            boolean z = false;
            boolean isInvisible = this.mIsStartupGuideEnable || this.mIsLockScreenShowed || this.mIsCloseMobileViewShowed;
            if (this.mIsTelephonyCallFront && (this.mIsCallStateOffHook || this.mIsCallStateRinging)) {
                isInvisible = false;
            }
            if (this.mShowingCameraId != null || isInvisible || this.mIsActivatedCamera) {
                this.mLinearLayout.setVisibility(4);
            }
            if (!isInvisible || this.mIsOccluded) {
                z = true;
            }
            updateNavigationLayout(z);
            this.mWindowManager.addView(this.mSubScreenView, this.mWlParams);
            this.mWindowWidth = this.mWindowManager.getDefaultDisplay().getWidth();
            bringTopSubScreenNavView();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startCameraActivity() {
        Intent intent = new Intent();
        intent.setClassName(CAMERA_PACKAGENAME, CAMERA_MAIN_ACTIVITY);
        intent.setAction("com.huawei.camera.store.show.BACK_BEAUTY");
        intent.addFlags(268468224);
        ActivityOptions options = ActivityOptions.makeBasic();
        ActivityManagerEx.setLaunchWindowingMode(options, 4, this.mContext);
        try {
            this.mContext.startActivity(intent, options.toBundle());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "startCameraActivity fail");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSubScreenView(int rotation) {
        if (this.mSubScreenView == null) {
            return;
        }
        if (this.mCurrentRotation != rotation) {
            resetSubScreenView();
            return;
        }
        updateViewParameter(rotation);
        Log.d(TAG, "Update Subview [ xPosition: " + this.mWlParams.x + ", yPosition: " + this.mWlParams.y + ", viewHeigh: " + this.mWlParams.height + ", viewWidth: " + this.mWlParams.width + "]");
        this.mWindowManager.updateViewLayout(this.mSubScreenView, this.mWlParams);
    }

    private void resetSubScreenView() {
        Log.i(TAG, "resetSubScreenView: mSubScreenView: " + this.mSubScreenView);
        View view = this.mSubScreenView;
        if (view != null) {
            this.mWindowManager.removeView(view);
            this.mSubScreenView = null;
            createSubScreenView();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConfigurationChanged() {
        Log.i(TAG, "handleConfigurationChanged mIsLockScreenShowed: " + this.mIsLockScreenShowed + " mIsActivatedCamera: " + this.mIsActivatedCamera);
        if (!this.mIsLockScreenShowed && !this.mIsActivatedCamera && this.mSubScreenView != null && this.mWindowWidth != this.mWindowManager.getDefaultDisplay().getWidth()) {
            resetSubScreenView();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeSubScreenView() {
        Log.d(TAG, "Remove Subview mSubScreenView: " + this.mSubScreenView);
        View view = this.mSubScreenView;
        if (view != null) {
            this.mWindowManager.removeView(view);
            this.mSubScreenView = null;
            this.mOldAppView = null;
            this.mOldCallingPid = -1;
            this.mOldRemoteViews = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLanguageChanged() {
        if (this.mSubScreenView != null && this.mViewContent != null) {
            resetSubScreenView();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAftPolicyChange(int ownerPid, int mode) {
        boolean useEarpieceOnly = false;
        if ((AudioSystem.getDevicesForStream(0) & 3) == 1 && isInVoipCallMode(mode)) {
            useEarpieceOnly = true;
        }
        Log.i(TAG, "ownerPid: " + ownerPid + " mForegroundPid: " + this.mForegroundPid + " mode: " + mode + " useEarpieceOnly: " + useEarpieceOnly + " mIsEarpieceIncall: " + this.mIsEarpieceIncall + ", mIsVideoCall = " + this.mIsVideoCall);
        if (checkHeadSetIsConnected()) {
            this.mUseEarpiecePid = -1;
            this.mIsEarpieceIncall = useEarpieceOnly;
            return;
        }
        if (useEarpieceOnly) {
            this.mUseEarpiecePid = ownerPid;
        } else {
            this.mUseEarpiecePid = -1;
        }
        String str = this.mForegroundAppName;
        if (str == null || str.equals(this.mCameraAppName)) {
            this.mIsEarpieceIncall = useEarpieceOnly;
            return;
        }
        if (this.mIsEarpieceIncall != useEarpieceOnly && !this.mIsVideoCall && useEarpieceOnly && getAppNameByPid(this.mForegroundPid).equals(getAppNameByPid(this.mUseEarpiecePid))) {
            requestShowFlipTipsAndFlip(1);
        }
        this.mIsEarpieceIncall = useEarpieceOnly;
    }

    private boolean checkHeadSetIsConnected() {
        Object service = this.mContext.getSystemService("audio");
        AudioManager audioManager = null;
        if (service instanceof AudioManager) {
            audioManager = (AudioManager) service;
        }
        boolean isHeadSetConnectedState = false;
        if (audioManager == null) {
            return false;
        }
        boolean isEnabledForBlueHeadSet = (audioManager.isBluetoothA2dpOn() || audioManager.isBluetoothScoOn()) && BluetoothAdapter.getDefaultAdapter().isEnabled();
        if (audioManager.isWiredHeadsetOn() || isEnabledForBlueHeadSet) {
            isHeadSetConnectedState = true;
        }
        Log.d(TAG, "checkHeadSetIsConnected : " + isHeadSetConnectedState + " isEnabledForBlueHeadSet: " + isEnabledForBlueHeadSet);
        return isHeadSetConnectedState;
    }

    private boolean isInVoipCallMode(int mode) {
        return mode == 3;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setVisibilitySubScreenView() {
        if (this.mSubScreenView != null) {
            boolean z = true;
            boolean isVisible = !this.mIsStartupGuideEnable && !this.mIsCloseMobileViewShowed;
            Log.i(TAG, "isVisible: " + isVisible + " mOldRemoteViews: " + this.mOldRemoteViews + " mShowingCameraId: " + this.mShowingCameraId + " mIsActivatedCamera: " + this.mIsActivatedCamera + " mIsOccluded: " + this.mIsOccluded);
            if (this.mOldAppView == null) {
                if (isVisible && !this.mIsLockScreenShowed && (this.mShowingCameraId == null && !this.mIsActivatedCamera && (!this.mIsOccluded || !this.mIsLockScreenShowed))) {
                    this.mLinearLayout.setVisibility(0);
                } else {
                    this.mLinearLayout.setVisibility(4);
                }
            } else if (!isVisible || (!this.mIsOccluded && this.mIsLockScreenShowed)) {
                this.mOldAppView.setVisibility(4);
            } else {
                this.mOldAppView.setVisibility(0);
            }
            if ((!isVisible || this.mIsLockScreenShowed) && !this.mIsOccluded) {
                z = false;
            }
            updateNavigationLayout(z);
        }
    }

    private void updateViewParameter(int rotation) {
        if (rotation == 0) {
            WindowManager.LayoutParams layoutParams = this.mWlParams;
            layoutParams.x = 0;
            int i = FULL_HEIGHT;
            int i2 = VIEW_HEIGHT;
            layoutParams.y = i - i2;
            layoutParams.width = VIEW_WIDTH;
            layoutParams.height = i2;
        } else if (rotation == 1) {
            WindowManager.LayoutParams layoutParams2 = this.mWlParams;
            int i3 = FULL_HEIGHT;
            int i4 = VIEW_HEIGHT;
            layoutParams2.x = i3 - i4;
            int i5 = MAIN_SCREEN_WIDTH;
            int i6 = VIEW_WIDTH;
            layoutParams2.y = i5 - i6;
            layoutParams2.width = i4;
            layoutParams2.height = i6;
        } else if (rotation == 2) {
            WindowManager.LayoutParams layoutParams3 = this.mWlParams;
            int i7 = MAIN_SCREEN_WIDTH;
            int i8 = VIEW_WIDTH;
            layoutParams3.x = i7 - i8;
            layoutParams3.y = 0;
            layoutParams3.width = i8;
            layoutParams3.height = VIEW_HEIGHT;
        } else if (rotation == 3) {
            WindowManager.LayoutParams layoutParams4 = this.mWlParams;
            layoutParams4.x = 0;
            layoutParams4.y = 0;
            layoutParams4.width = VIEW_HEIGHT;
            layoutParams4.height = VIEW_WIDTH;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateGestureNavState() {
        this.mIsGestureNaviEnabled = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getGestureNavConst().getGestureNavEnabled(this.mContext, -2);
        updateNavigationLayout(!this.mIsStartupGuideEnable && !this.mIsLockScreenShowed && !this.mIsCloseMobileViewShowed);
        if (this.mSubScreenView != null && this.mIsGestureNaviEnabled) {
            this.mHandler.sendEmptyMessage(4);
            this.mHandler.postDelayed(this.delayUpdateSubScreenView, 300);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createSubScreenNavView() {
        DefaultGestureNavManager defaultGestureNavManager = this.mGestureNavPolicy;
        if (defaultGestureNavManager != null) {
            defaultGestureNavManager.initSubScreenNavView();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void destroySubScreenNavView() {
        DefaultGestureNavManager defaultGestureNavManager = this.mGestureNavPolicy;
        if (defaultGestureNavManager != null) {
            defaultGestureNavManager.destroySubScreenNavView();
        }
    }

    private void bringTopSubScreenNavView() {
        DefaultGestureNavManager defaultGestureNavManager = this.mGestureNavPolicy;
        if (defaultGestureNavManager != null) {
            defaultGestureNavManager.bringTopSubScreenNavView();
        }
    }

    private boolean isIsFsmIntelligentOn() {
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "intelligent_awaken_enabled", 1, -2) == 1) {
            return true;
        }
        return false;
    }

    private void initFsmIntelligentObserver() {
        this.mIsIntelligentOn = isIsFsmIntelligentOn();
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("intelligent_awaken_enabled"), true, this.mContentObserver, -1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateFsmIntelligentState() {
        boolean isIntelligentOn = isIsFsmIntelligentOn();
        Log.i(TAG, "handleUpdateFsmIntelligentState--->mIsIntelligentOn: " + this.mIsIntelligentOn + " isIntelligentOn: " + isIntelligentOn);
        if (this.mIsIntelligentOn != isIntelligentOn) {
            this.mIsIntelligentOn = isIntelligentOn;
            if (HwFoldScreenManagerEx.getDisplayMode() == 3) {
                updateViewForFsmIntelStateChanged(this.mIsIntelligentOn);
            }
        }
    }

    private void updateViewForFsmIntelStateChanged(boolean isIntelligentOn) {
        int rotation = this.mWindowManager.getDefaultDisplay().getRotation();
        updateViewParameter(rotation);
        if (isIntelligentOn) {
            initSubScreenView(rotation);
        } else {
            initSubScreenViewForFsmIntelTurnOff(rotation);
        }
        updateNavigationLayout(true);
        this.mWindowManager.addView(this.mSubScreenView, this.mWlParams);
        this.mWindowWidth = this.mWindowManager.getDefaultDisplay().getWidth();
        bringTopSubScreenNavView();
    }

    private void initSubScreenViewForFsmIntelTurnOff(int rotation) {
        int windowId;
        Log.i(TAG, "initSubScreenViewForFsmIntelTurnOff: mIsTelephonyCallFront: " + this.mIsTelephonyCallFront + " mIsCallStateRinging: " + this.mIsCallStateRinging + " mIsCallStateOffHook:" + this.mIsCallStateOffHook);
        View view = this.mSubScreenView;
        if (view != null) {
            this.mWindowManager.removeView(view);
            this.mSubScreenView = null;
        }
        if (rotation == 0) {
            windowId = 34013454;
        } else if (rotation == 1) {
            windowId = 34013457;
        } else if (rotation == 3) {
            windowId = 34013456;
        } else {
            windowId = 34013455;
        }
        this.mCurrentRotation = rotation;
        this.mSubScreenView = LayoutInflater.from(this.mContext).inflate(windowId, (ViewGroup) null);
        this.mRelativeLayout = (RelativeLayout) this.mSubScreenView.findViewById(34603505);
        this.mLinearLayout = (LinearLayout) this.mSubScreenView.findViewById(34603502);
        this.mFlipButtonClockZone = (LinearLayout) this.mSubScreenView.findViewById(34603499);
        this.mFlipButtonClockZone.setOnClickListener(new View.OnClickListener() {
            /* class com.android.server.foldscreenview.SubScreenViewEntry.AnonymousClass8 */

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SubScreenViewEntry.this.requestShowFlipTipsAndFlip(2);
            }
        });
        this.mViewContent = (TextView) this.mSubScreenView.findViewById(34603390);
        if (this.mIsTelephonyCallFront && (this.mIsCallStateRinging || this.mIsCallStateOffHook)) {
            this.mViewContent.setText(this.mContext.getString(33685597));
        }
        this.mNavigationLayout = (LinearLayout) this.mSubScreenView.findViewById(34603503);
        this.mBackButtonView = (ImageView) this.mSubScreenView.findViewById(34603498);
        this.mHomeButtonView = (ImageView) this.mSubScreenView.findViewById(34603500);
        this.mRecentButtonView = (ImageView) this.mSubScreenView.findViewById(34603504);
        new NavigationButton(this.mContext, this.mBackButtonView, 4);
        new NavigationButton(this.mContext, this.mHomeButtonView, 3);
        new NavigationButton(this.mContext, this.mRecentButtonView, 187);
    }

    private void registerCallStateListener() {
        Object telephonyRelatedObject = this.mContext.getSystemService("phone");
        if (telephonyRelatedObject instanceof TelephonyManager) {
            this.mTelephonyManager = (TelephonyManager) telephonyRelatedObject;
            if (!isMultiSimEnabled()) {
                if (this.mMsimPhoneStateListenerCard0 == null) {
                    this.mMsimPhoneStateListenerCard0 = getPhoneStateListener(0);
                }
                this.mTelephonyManager.listen(this.mMsimPhoneStateListenerCard0, 32);
                return;
            }
            if (this.mMsimPhoneStateListenerCard0 == null) {
                this.mMsimPhoneStateListenerCard0 = getPhoneStateListener(0);
            }
            if (this.mMsimPhoneStateListenerCard1 == null) {
                this.mMsimPhoneStateListenerCard1 = getPhoneStateListener(1);
            }
            this.mTelephonyManager.listen(this.mMsimPhoneStateListenerCard0, 32);
            this.mTelephonyManager.listen(this.mMsimPhoneStateListenerCard1, 32);
        }
    }

    private PhoneStateListener getPhoneStateListener(int subscription) {
        return new PhoneStateListener(Integer.valueOf(subscription)) {
            /* class com.android.server.foldscreenview.SubScreenViewEntry.AnonymousClass9 */

            @Override // android.telephony.PhoneStateListener
            public void onCallStateChanged(int state, String phoneNumber) {
                if (state == 0) {
                    SubScreenViewEntry.this.mIsCallStateRinging = false;
                    SubScreenViewEntry.this.mIsCallStateOffHook = false;
                    SubScreenViewEntry.this.mIsVideoCall = false;
                } else if (state == 1) {
                    SubScreenViewEntry.this.mIsCallStateRinging = true;
                    SubScreenViewEntry.this.mIsCallStateOffHook = false;
                } else if (state == 2) {
                    SubScreenViewEntry.this.mIsCallStateRinging = false;
                    SubScreenViewEntry.this.mIsCallStateOffHook = true;
                }
                super.onCallStateChanged(state, phoneNumber);
            }
        };
    }

    private static boolean isMultiSimEnabled() {
        return TelephonyManager.getDefault().isMultiSimEnabled();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateCallStateChange() {
        if (this.mIsTelephonyCallFront && !this.mIsCallStateRinging && !this.mIsCallStateOffHook) {
            resetSubScreenView();
        } else if (!this.mIsTelephonyCallFront || (!this.mIsCallStateRinging && !this.mIsCallStateOffHook)) {
            if (this.mIsTelephonyCallFront || (!this.mIsCallStateRinging && !this.mIsCallStateOffHook)) {
                Log.i(TAG, "nothing need to do");
            } else {
                resetSubScreenView();
            }
        } else if (!this.mIsIntelligentOn) {
            requestShowFlipTipsAndFlip(1);
        } else {
            int rotation = this.mWindowManager.getDefaultDisplay().getRotation();
            updateViewParameter(rotation);
            initSubScreenViewForCallTips(rotation);
            updateNavigationLayout(true);
            this.mWindowManager.addView(this.mSubScreenView, this.mWlParams);
            this.mWindowWidth = this.mWindowManager.getDefaultDisplay().getWidth();
            bringTopSubScreenNavView();
        }
    }

    private void initSubScreenViewForCallTips(int rotation) {
        int windowId;
        if (!this.mIsIntelligentOn) {
            initSubScreenViewForFsmIntelTurnOff(rotation);
            return;
        }
        View view = this.mSubScreenView;
        if (view != null) {
            this.mWindowManager.removeView(view);
            this.mSubScreenView = null;
        }
        if (rotation == 0) {
            windowId = 34013439;
        } else if (rotation == 1) {
            windowId = 34013453;
        } else if (rotation == 3) {
            windowId = 34013452;
        } else {
            windowId = 34013451;
        }
        this.mCurrentRotation = rotation;
        this.mSubScreenView = LayoutInflater.from(this.mContext).inflate(windowId, (ViewGroup) null);
        this.mRelativeLayout = (RelativeLayout) this.mSubScreenView.findViewById(34603505);
        this.mLinearLayout = (LinearLayout) this.mSubScreenView.findViewById(34603502);
        this.mViewContent = (TextView) this.mSubScreenView.findViewById(34603390);
        this.mNavigationLayout = (LinearLayout) this.mSubScreenView.findViewById(34603503);
        this.mBackButtonView = (ImageView) this.mSubScreenView.findViewById(34603498);
        this.mHomeButtonView = (ImageView) this.mSubScreenView.findViewById(34603500);
        this.mRecentButtonView = (ImageView) this.mSubScreenView.findViewById(34603504);
        new NavigationButton(this.mContext, this.mBackButtonView, 4);
        new NavigationButton(this.mContext, this.mHomeButtonView, 3);
        new NavigationButton(this.mContext, this.mRecentButtonView, 187);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestShowFlipTipsAndFlip(int flipReason) {
        if (HwFoldScreenManagerEx.getDisplayMode() == 3) {
            Log.d(TAG, "requestShowFlipTipsAndFlip, flipReason = " + flipReason);
            Bundle data = new Bundle();
            if (flipReason == 1) {
                data.putInt("KEY_TIPS_TEXT", 1);
            } else {
                data.putInt("KEY_TIPS_TEXT", 2);
            }
            data.putString("KEY_TIPS_STR_CALLER_NAME", TAG);
            data.putInt("KEY_TIPS_INT_VIEW_TYPE", 1);
            data.putInt("KEY_TIPS_INT_DISPLAY_MODE", 2);
            HwFoldScreenManagerEx.reqShowTipsToFsm(2, data);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onFsmTipRemoved() {
        Log.d(TAG, "onFsmTipRemoved");
        if (HwFoldScreenManagerEx.getDisplayMode() == 3) {
            this.mHandler.sendEmptyMessage(3);
        }
    }

    private String getTopPackageName() {
        ActivityInfo topActivityInfo = ActivityManagerEx.getLastResumedActivity();
        if (topActivityInfo != null) {
            return topActivityInfo.packageName;
        }
        return "";
    }

    private String getAppNameByPid(int pid) {
        List<ActivityManager.RunningAppProcessInfo> processes = getRunningProcesses();
        if (processes == null || processes.size() < 1) {
            Log.d(TAG, "get app name, get running process failed");
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
            if (processInfo.pid == pid && processInfo.processName != null) {
                Log.i(TAG, "pid: " + pid + " packageName: " + processInfo.processName);
                String[] packageParts = processInfo.processName.split(AwarenessInnerConstants.COLON_KEY);
                return packageParts.length > 0 ? packageParts[0] : processInfo.processName;
            }
        }
        return "";
    }

    private List<ActivityManager.RunningAppProcessInfo> getRunningProcesses() {
        Context context = this.mContext;
        if (context == null) {
            Log.d(TAG, "getRunningProcesses, mContext is null");
            return new ArrayList(0);
        }
        Object getService = context.getSystemService("activity");
        ActivityManager activityManager = null;
        if (getService instanceof ActivityManager) {
            activityManager = (ActivityManager) getService;
        }
        if (activityManager != null) {
            return activityManager.getRunningAppProcesses();
        }
        Log.d(TAG, "get process status, get ams service failed");
        return new ArrayList(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enbaleInputScaleMode(boolean enable) {
        int side = enable ? getSubDisplayInputScaleSide(this.mCurrentRotation) : 0;
        InputManagerServiceEx.DefaultHwInputManagerLocalService defaultHwInputManagerLocalService = this.mHwInputManagerInternal;
        if (defaultHwInputManagerLocalService != null) {
            float f = SUB_DISPLAY_SCALE;
            defaultHwInputManagerLocalService.setInputScaleConfig(f, f, side, 3);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateInputScaleMode(int rotation) {
        InputManagerServiceEx.DefaultHwInputManagerLocalService defaultHwInputManagerLocalService;
        if (this.mSubScreenView != null && (defaultHwInputManagerLocalService = this.mHwInputManagerInternal) != null) {
            float f = SUB_DISPLAY_SCALE;
            defaultHwInputManagerLocalService.setInputScaleConfig(f, f, getSubDisplayInputScaleSide(rotation), 3);
        }
    }

    private int getSubDisplayInputScaleSide(int rotation) {
        if (rotation == 0) {
            return 1;
        }
        if (rotation == 1) {
            return 2;
        }
        if (rotation == 2) {
            return 3;
        }
        if (rotation != 3) {
            return 1;
        }
        return 4;
    }
}
