package com.huawei.sidetouch;

import android.app.ActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import com.huawei.android.hardware.input.HwInputManager;
import com.huawei.android.hardware.input.IHwInputManager;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.android.view.HwExtDisplaySizeUtil;

public class HwSideTouchManager {
    private static final String ACCESSIBILITY_SCREENREADER_ENABLED = "accessibility_screenreader_enabled";
    private static final int ACTIVE_AREA = 1;
    private static final int AREA_SIZE = 2;
    private static final int EVENT_SOURCE_SIDE_TOUCH = 1;
    private static final int EVENT_SOURCE_SOLID_BUTTON = 0;
    private static final int GREEN_AREA = 0;
    private static final int INVALID_VOLUNME_MODE = -1;
    private static final int REGION_END_DEFAULT = 1010;
    private static final int REGION_START_DEFAULT = 190;
    private static final int REGION_TUNE_TO_GAME = 24;
    private static final String SIDE_CONFIG_BOTHHAND = "1,3";
    private static final String SIDE_CONFIG_DISABLED = "1,0";
    private static final String SIDE_CONFIG_LEFTHAND = "1,1";
    private static final String SIDE_CONFIG_RIGHTHAND = "1,2";
    private static final String STATUS_OK = "OK";
    private static final String TAG = "HwSideTouchManager";
    private static final int TALKBACK_SERVICE_DISABLE = 0;
    private static final int TALKBACK_SERVICE_ENABLE = 1;
    private static final int VOLUME_MODE_MAPPING = 0;
    private static final int VOLUME_MODE_STEP = 1;
    private static HwSideVibrationManager sHwSideViberateManager = null;
    private static HwSideTouchManager sInstance = null;
    private IHwSideTouchCallback mCallback = null;
    private Context mContext;
    private int mCurrentSideTouchMode = -1;
    private int mEventSource = 0;
    private int mHandStatus = -1;
    private boolean mIsSystemReady = false;
    private String mSideConfig = null;
    private ContentObserver mSidePositionObserver;
    private ContentObserver mTalkBackObserver;
    private int mVolumeCommandCalledCnt = 0;
    private ContentObserver mVolumeModeObserver;

    private HwSideTouchManager(Context context) {
        this.mContext = context;
    }

    public static synchronized HwSideTouchManager getInstance(Context context) {
        HwSideTouchManager hwSideTouchManager;
        synchronized (HwSideTouchManager.class) {
            if (sInstance == null) {
                sInstance = new HwSideTouchManager(context);
            }
            hwSideTouchManager = sInstance;
        }
        return hwSideTouchManager;
    }

    public int[] runSideTouchCommand(int commandType, Bundle bundle) {
        if (!HwExtDisplaySizeUtil.getInstance().hasSideInScreen() || !this.mIsSystemReady) {
            Log.w(TAG, "do not support side screen, return");
            return null;
        }
        switch (commandType) {
            case 1:
                setVolumePanelInfo(bundle);
                return null;
            case 2:
                setSideTouchRegion(bundle);
                return null;
            case 3:
                return getSideTouchRegion(bundle);
            case 4:
                setSideTouchVolumeMode(bundle);
                return null;
            case 5:
                setSideTouchMapping(bundle);
                return null;
            case 6:
                enableSideTouch(bundle);
                return null;
            case 7:
            default:
                return null;
            case 8:
                setSidePosition(bundle);
                return null;
            case 9:
                return getSideTouchVolumeMode();
            case 10:
                setSideTouchTriggerSensitivity(bundle);
                return null;
            case 11:
                return getSideTouchStatus(bundle);
            case 12:
                return getExtraInfo(bundle);
        }
    }

    private void setVolumePanelInfo(Bundle bundle) {
        if (bundle == null) {
            Log.w(TAG, "parameter is null, return");
            return;
        }
        try {
            HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_SET_VOLUME_GUI_INFO, getVolumeGuiInfoParams(bundle));
        } catch (RemoteException e) {
            Log.e(TAG, "setVolumeGUIInfo failed RemoteException");
        }
    }

    private String getVolumeGuiInfoParams(Bundle bundle) {
        int guiState = bundle.getInt(TpCommandConstant.GUI_STATE);
        int startPosX = bundle.getInt(TpCommandConstant.X_START_POS);
        int endPosX = bundle.getInt(TpCommandConstant.X_END_POS);
        int startPosY = bundle.getInt(TpCommandConstant.Y_START_POS);
        int endPosY = bundle.getInt(TpCommandConstant.Y_END_POS);
        int mapToX = bundle.getInt(TpCommandConstant.X_MAP_TO);
        int mapToY = bundle.getInt(TpCommandConstant.Y_MAP_TO);
        return guiState + TpCommandConstant.SEPARATE + startPosX + TpCommandConstant.SEPARATE + endPosX + TpCommandConstant.SEPARATE + startPosY + TpCommandConstant.SEPARATE + endPosY + TpCommandConstant.SEPARATE + mapToX + TpCommandConstant.SEPARATE + mapToY;
    }

    private void setSideTouchRegion(Bundle bundle) {
        String paramEnd;
        String paramStart;
        if (bundle != null) {
            int type = bundle.getInt(TpCommandConstant.TOUCH_REGION_TYPE);
            Rect rect = (Rect) bundle.getParcelable(TpCommandConstant.TOUCH_REGION_RECT);
            if (rect == null) {
                Log.e(TAG, "setSideTouchRegion rect is null");
                return;
            }
            if (type == 0) {
                paramStart = "volumeGreenAreaStart#" + rect.top;
                paramEnd = "volumeGreenAreaEnd#" + rect.bottom;
            } else if (type == 1) {
                paramStart = "volumeActiveAreaStart#" + rect.top;
                paramEnd = "volumeActiveAreaEnd#" + rect.bottom;
            } else {
                Log.w(TAG, "setSideTouchRegion type error");
                return;
            }
            try {
                HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_SET_TUNE_TIME_PRMT, paramStart);
                HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_SET_TUNE_TIME_PRMT, paramEnd);
            } catch (RemoteException e) {
                Log.e(TAG, "setSideTouchRegion failed RemoteException");
            }
        }
    }

    private int[] getSideTouchRegion(Bundle bundle) {
        String paramEnd;
        String paramStart;
        if (bundle == null) {
            return null;
        }
        int type = bundle.getInt(TpCommandConstant.TOUCH_REGION_TYPE);
        if (type == 0) {
            paramStart = TpCommandConstant.VOLUME_GREEN_AREA_START;
            paramEnd = TpCommandConstant.VOLUME_GREEN_AREA_END;
        } else if (type == 1) {
            paramStart = TpCommandConstant.VOLUME_ACTIVE_AREA_START;
            paramEnd = TpCommandConstant.VOLUME_ACTIVE_AREA_END;
        } else {
            Log.w(TAG, "setSideTouchRegion type error " + type);
            return null;
        }
        try {
            String startRegion = HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_GET_TUNE_TIME_PRMT, paramStart);
            Log.i(TAG, "getSideTouchRegion startRegion " + startRegion);
            String endRegion = HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_GET_TUNE_TIME_PRMT, paramEnd);
            Log.i(TAG, "getSideTouchRegion endRegion " + endRegion);
            return new int[]{getRegion(startRegion, true), getRegion(endRegion, false)};
        } catch (RemoteException e) {
            Log.e(TAG, "getSideTouchRegion failed RemoteException");
            return null;
        }
    }

    private int getRegion(String region, boolean isStart) {
        String[] splitResult;
        int defaultRegion = isStart ? REGION_START_DEFAULT : REGION_END_DEFAULT;
        if (region == null || (splitResult = region.split(TpCommandConstant.SEPARATE)) == null || splitResult.length != 2 || !STATUS_OK.equals(splitResult[0])) {
            return defaultRegion;
        }
        try {
            return Integer.parseInt(splitResult[1]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException");
            return defaultRegion;
        }
    }

    private void setSideTouchVolumeMode(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "setSideTouchVolumeMode bundle is null");
            return;
        }
        int mode = bundle.getInt(TpCommandConstant.VOLUME_GESTURE);
        if (mode < 0) {
            Log.w(TAG, "setSideTouchVolumeMode mode error " + mode);
            return;
        }
        try {
            HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_SET_TUNE_TIME_PRMT, "volumeGesture#" + mode);
        } catch (RemoteException e) {
            Log.e(TAG, "setSideTouchVolumeMode failed RemoteException");
        }
    }

    private int[] getSideTouchVolumeMode() {
        String volumeMode = null;
        try {
            volumeMode = HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_GET_TUNE_TIME_PRMT, TpCommandConstant.VOLUME_GESTURE);
        } catch (RemoteException e) {
            Log.e(TAG, "getSideTouchVolumeMode failed RemoteException");
        }
        int[] sideVolumeModes = new int[1];
        if (volumeMode != null) {
            int getMode = getModeFromTp(volumeMode);
            if (getMode < 0) {
                Log.w(TAG, "getSideTouchVolumeMode mode error " + getMode);
                sideVolumeModes[0] = 2248;
                return sideVolumeModes;
            }
            sideVolumeModes[0] = getMode;
        } else {
            Log.w(TAG, "getSideTouchVolumeMode mode get error , return mapping as defalut");
            sideVolumeModes[0] = 2248;
        }
        return sideVolumeModes;
    }

    private int[] getSideTouchStatus(Bundle bundle) {
        if (bundle != null) {
            Log.i(TAG, "getSideTouchStatus:" + this.mHandStatus + "," + this.mEventSource);
            return new int[]{this.mHandStatus, this.mEventSource};
        }
        String volumeMode = null;
        try {
            volumeMode = HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_VOLUME_GET_STATE, (String) null);
        } catch (RemoteException e) {
            Log.e(TAG, "getSideTouchVolumeMode failed RemoteException");
        }
        int[] sideVolumeModes = new int[2];
        if (volumeMode != null) {
            sideVolumeModes[0] = getModeFromTp(volumeMode);
        } else {
            Log.w(TAG, "getSideTouchVolumeMode mode get error , return mapping as defalut");
            sideVolumeModes[0] = 2048;
        }
        sideVolumeModes[1] = this.mEventSource;
        return sideVolumeModes;
    }

    private int[] getExtraInfo(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "bundle is null");
            return null;
        }
        int type = bundle.getInt(TpCommandConstant.EXTRA_INFO);
        if (type == 1) {
            return new int[]{this.mVolumeCommandCalledCnt};
        }
        Log.e(TAG, "type is invalid for getExtraInfo, it is " + type);
        return null;
    }

    private int getModeFromTp(String volumeMode) {
        String[] splitResult;
        if (volumeMode == null || (splitResult = volumeMode.split(TpCommandConstant.SEPARATE)) == null || !STATUS_OK.equals(splitResult[0])) {
            return -1;
        }
        try {
            return Integer.parseInt(splitResult[1]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException");
            return -1;
        }
    }

    private void setSideTouchMapping(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "setSideTouchMapping bundle is null");
            return;
        }
        Rect fromRect = (Rect) bundle.getParcelable(TpCommandConstant.FROM_RECT);
        Rect toRect = (Rect) bundle.getParcelable(TpCommandConstant.TO_RECT);
        if (fromRect == null || toRect == null) {
            Log.e(TAG, "setSideTouchMapping fromRect or toRect is null");
            return;
        }
        String params = fromRect.left + TpCommandConstant.SEPARATE + fromRect.right + TpCommandConstant.SEPARATE + fromRect.top + TpCommandConstant.SEPARATE + fromRect.bottom + TpCommandConstant.SEPARATE + toRect.left + TpCommandConstant.SEPARATE + toRect.right + TpCommandConstant.SEPARATE + toRect.top + TpCommandConstant.SEPARATE + toRect.bottom;
        Log.i(TAG, "setSideTouchMapping params = " + params);
        ajustSideTouchRegion(bundle, true, true);
        try {
            HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_MAP_SIDE_AREA, params);
        } catch (RemoteException e) {
            Log.e(TAG, "setSideTouchMapping failed RemoteException");
        }
    }

    private void enableSideTouch(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "enableSideTouch bundle is null");
            return;
        }
        boolean isEnable = bundle.getBoolean(TpCommandConstant.ENABLE);
        Rect rect = (Rect) bundle.getParcelable(TpCommandConstant.TO_RECT);
        if (rect == null) {
            Log.e(TAG, "enableSideTouch rect is null");
            return;
        }
        String params = rect.left + TpCommandConstant.SEPARATE + rect.right + TpCommandConstant.SEPARATE + rect.top + TpCommandConstant.SEPARATE + rect.bottom;
        Log.i(TAG, "enableSideTouch isEnable =" + isEnable + ", params = " + params);
        if (isEnable) {
            try {
                HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_EXPOSE_SIDE_AREA, params);
            } catch (RemoteException e) {
                Log.e(TAG, "enableSideTouch failed RemoteException");
            }
        } else {
            HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_REMOVE_SIDE_AREA, params);
        }
        ajustSideTouchRegion(bundle, isEnable, false);
    }

    private void ajustSideTouchRegion(Bundle bundle, boolean isEnable, boolean isMapping) {
        int tuneTop;
        if (bundle == null) {
            Log.e(TAG, "enableSideTouch bundle is null");
            return;
        }
        try {
            String startRegion = HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_GET_TUNE_TIME_PRMT, TpCommandConstant.VOLUME_ACTIVE_AREA_START);
            int tuneTop2 = getRegion(startRegion, true);
            Log.i(TAG, "AjustSideTouchRegion startRegion =" + startRegion + ", oldTop =" + tuneTop2);
            Rect rect = (Rect) bundle.getParcelable(isMapping ? TpCommandConstant.FROM_RECT : TpCommandConstant.TO_RECT);
            if (rect == null) {
                Log.e(TAG, "ajustSideTouchRegion rect beyond the active area.");
            } else if ((rect.bottom + rect.top) / 2 > REGION_END_DEFAULT) {
                Log.i(TAG, "enableSideTouch rect is null");
            } else {
                if (isEnable && rect.bottom > tuneTop2 && tuneTop2 >= rect.top) {
                    tuneTop = rect.bottom + 24;
                    Log.i(TAG, "AjustSideTouchRegion newTop =" + tuneTop);
                } else if (!isEnable) {
                    tuneTop = REGION_START_DEFAULT;
                } else {
                    return;
                }
                try {
                    HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_SET_TUNE_TIME_PRMT, "volumeGreenAreaStart#" + tuneTop);
                    String paramNewStart = "volumeActiveAreaStart#" + tuneTop;
                    HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_SET_TUNE_TIME_PRMT, paramNewStart);
                    Log.i(TAG, "AjustSideTouchRegion paramStart = " + paramNewStart);
                } catch (RemoteException e) {
                    Log.e(TAG, "setSideTouchRegion failed RemoteException");
                }
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "getSideTouchRegion failed RemoteException");
        }
    }

    private void setSidePosition(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "setSidePosition bundle is null");
            return;
        }
        int sidePosition = bundle.getInt(TpCommandConstant.VOLUME_SIED_ENABLED, 3);
        try {
            HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_SET_TUNE_TIME_PRMT, "volumeSideEnabled#" + sidePosition);
        } catch (RemoteException e) {
            Log.e(TAG, "setSidePosition failed RemoteException");
        }
    }

    private void notifySideConfig(int sidePosition, boolean isSelfChange) {
        if (sidePosition == 0) {
            this.mSideConfig = "1,0";
        } else if (sidePosition == 1) {
            this.mSideConfig = "1,1";
        } else if (sidePosition == 2) {
            this.mSideConfig = SIDE_CONFIG_RIGHTHAND;
        } else if (sidePosition != 3) {
            this.mSideConfig = SIDE_CONFIG_BOTHHAND;
        } else {
            this.mSideConfig = SIDE_CONFIG_BOTHHAND;
        }
        if (!isSideConfigEnabled()) {
            this.mEventSource = 0;
        }
        IHwSideTouchCallback iHwSideTouchCallback = this.mCallback;
        if (iHwSideTouchCallback != null) {
            iHwSideTouchCallback.notifySideConfig(this.mSideConfig, isSelfChange);
        }
    }

    private void setSideTouchTriggerSensitivity(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "setSideTouchTriggerSensitivity bundle is null");
            return;
        }
        int triggerSensitivity = bundle.getInt(TpCommandConstant.SIDE_TOUCH_TRIGGER_SENSITIVITY);
        int volumeFlickUp = 75;
        int volumeFlickDown = 75;
        int volumeTouchMin = TpCommandConstant.VOLUME_TOUCH_MIN_THRESHOLD_MIN;
        if (triggerSensitivity != 1) {
            if (triggerSensitivity == 2) {
                volumeFlickUp = TpCommandConstant.VOLUME_FLICK_THRESHOLD_MAX;
                volumeFlickDown = TpCommandConstant.VOLUME_FLICK_THRESHOLD_MAX;
            } else if (triggerSensitivity != 3) {
                Log.w(TAG, "setSideTouchTriggerSensitivity: param error, triggerSensitivity is " + triggerSensitivity);
                return;
            } else {
                volumeFlickUp = TpCommandConstant.VOLUME_FLICK_THRESHOLD_MAX;
                volumeFlickDown = TpCommandConstant.VOLUME_FLICK_THRESHOLD_MAX;
                volumeTouchMin = 0;
            }
        }
        String volumeFlickUpParam = "volumeFlickUp#" + volumeFlickUp;
        String volumeFlickDownParam = "volumeFlickDown#" + volumeFlickDown;
        String volumeTouchMinParam = "volumeTouchMin#" + volumeTouchMin;
        try {
            IHwInputManager hwInputManager = HwInputManager.getService();
            if (hwInputManager == null) {
                Log.w(TAG, "setSideTouchTriggerSensitivity: fail to get hwInputManager, return.");
                return;
            }
            hwInputManager.runSideTouchCommand(TpCommandConstant.COMMAND_SET_TUNE_TIME_PRMT, volumeFlickUpParam);
            hwInputManager.runSideTouchCommand(TpCommandConstant.COMMAND_SET_TUNE_TIME_PRMT, volumeFlickDownParam);
            hwInputManager.runSideTouchCommand(TpCommandConstant.COMMAND_SET_TUNE_TIME_PRMT, volumeTouchMinParam);
        } catch (RemoteException e) {
            Log.e(TAG, "setSideTouchTriggerSensitivity failed RemoteException");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVolumeMode(int volumeMode) {
        int value;
        if (volumeMode == 0) {
            value = 202;
        } else if (volumeMode == 1) {
            value = TpCommandConstant.VOLUME_MODE_STEP;
        } else {
            Log.w(TAG, "volumeMode error " + volumeMode);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(TpCommandConstant.VOLUME_GESTURE, value);
        runSideTouchCommand(4, bundle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSidePosition(boolean isSelfChange) {
        int position = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), TpCommandConstant.KEY_SIDE_POSITION, 3, ActivityManager.getCurrentUser());
        Log.i(TAG, "side position onChange: " + position + ", isSelfChange:" + isSelfChange);
        if (position > 3 || position < 0) {
            Log.w(TAG, "side position error " + position);
            return;
        }
        notifySideConfig(position, isSelfChange);
    }

    public void systemReady(boolean isSystemReady) {
        Log.i(TAG, "systemReady: " + isSystemReady);
        this.mIsSystemReady = isSystemReady;
        initSidePositionObserver();
        initUserSwtichObserver();
        initTpInfo();
        initTalkBackObserver();
    }

    private void initTpInfo() {
        Log.i(TAG, "enter initTpInfo, default is 2248");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            /* class com.huawei.sidetouch.HwSideTouchManager.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                boolean isTalkBackEnabled = HwSideTouchManager.this.getTalkBackEnableState();
                Log.i(HwSideTouchManager.TAG, "isTaklBackEnabled " + isTalkBackEnabled);
                if (!isTalkBackEnabled) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(TpCommandConstant.VOLUME_GESTURE, 2248);
                    HwSideTouchManager.this.runSideTouchCommand(4, bundle);
                }
                try {
                    HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_SET_TUNE_TIME_PRMT, "volumeGreenAreaStart#190");
                    HwInputManager.getService().runSideTouchCommand(TpCommandConstant.COMMAND_SET_TUNE_TIME_PRMT, "volumeActiveAreaStart#190");
                    Log.i(HwSideTouchManager.TAG, "SystemReady paramStart = volumeActiveAreaStart#190");
                } catch (RemoteException e) {
                    Log.e(HwSideTouchManager.TAG, "setSideTouchRegion failed RemoteException");
                }
            }
        });
    }

    private void initTalkBackObserver() {
        if (this.mContext == null) {
            Log.w(TAG, "mContext is null");
            return;
        }
        this.mTalkBackObserver = new ContentObserver(null) {
            /* class com.huawei.sidetouch.HwSideTouchManager.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                Log.i(HwSideTouchManager.TAG, "talkBack status onChange");
                HwSideTouchManager.this.updateSideTouchVolumeMode();
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(ACCESSIBILITY_SCREENREADER_ENABLED), false, this.mTalkBackObserver, -1);
        this.mTalkBackObserver.onChange(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSideTouchVolumeMode() {
        Log.i(TAG, "enter updateSideTouchVolumeMode");
        int[] volumeModes = runSideTouchCommand(9, null);
        if (volumeModes == null || volumeModes.length == 0) {
            Log.e(TAG, "volumeModes is null, get mode fail");
            return;
        }
        this.mCurrentSideTouchMode = volumeModes[0];
        if (this.mCurrentSideTouchMode == 0) {
            Log.i(TAG, "mCurrentSideTouchMode is disable, do nothing");
            return;
        }
        int newTouchMode = 2248;
        boolean isSingleHandMode = getSingleHandEnableState();
        boolean isTalkBackEnabled = getTalkBackEnableState();
        Log.i(TAG, "isSingleHandMode is " + isSingleHandMode + " isTalkBackEnabled is " + isTalkBackEnabled + " mCurrentSideTouchMode is " + this.mCurrentSideTouchMode);
        if (isSingleHandMode || isTalkBackEnabled) {
            newTouchMode = getSideTouchVolumeStepMode(this.mCurrentSideTouchMode);
        }
        if (newTouchMode == -1) {
            Log.i(TAG, "newTouchMode is INVALID_VOLUNME_MODE, do nothing");
            return;
        }
        Log.i(TAG, "set newTouchMode is " + newTouchMode);
        Bundle bundle = new Bundle();
        bundle.putInt(TpCommandConstant.VOLUME_GESTURE, newTouchMode);
        runSideTouchCommand(4, bundle);
        Log.i(TAG, "updateSideTouchVolumeMode success");
    }

    public boolean getTalkBackEnableState() {
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0, -2) == 1) {
            return true;
        }
        return false;
    }

    private boolean getSingleHandEnableState() {
        String singleHandMode = Settings.Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        return singleHandMode != null && !StorageManagerExt.INVALID_KEY_DESC.equals(singleHandMode);
    }

    private int getSideTouchVolumeStepMode(int currentMode) {
        if (currentMode == 202) {
            return TpCommandConstant.VOLUME_MODE_STEP;
        }
        if (currentMode == 204) {
            return TpCommandConstant.VOLUME_MODE_DOUBLE_SLIDE_STEP;
        }
        if (currentMode == 2248) {
            return TpCommandConstant.VOLUME_MODE_DOUBLE_CLICK_STEP;
        }
        if (currentMode == 4296) {
            return TpCommandConstant.VOLUME_MODE_SINGLE_CLICK_STEP;
        }
        Log.e(TAG, "current volumeModes is not MAPPING mode, return INVALID_VOLUNME_MODE");
        return -1;
    }

    private void initVolumeModeObserver() {
        if (this.mContext == null) {
            Log.w(TAG, "mContext is null");
            return;
        }
        this.mVolumeModeObserver = new ContentObserver(null) {
            /* class com.huawei.sidetouch.HwSideTouchManager.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                int volumeMode = Settings.Secure.getIntForUser(HwSideTouchManager.this.mContext.getContentResolver(), TpCommandConstant.KEY_VOLUME_MODE, 0, ActivityManager.getCurrentUser());
                Log.i(HwSideTouchManager.TAG, "volume mode onChange: " + volumeMode);
                HwSideTouchManager.this.updateVolumeMode(volumeMode);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(TpCommandConstant.KEY_VOLUME_MODE), false, this.mVolumeModeObserver, -1);
        this.mVolumeModeObserver.onChange(true);
    }

    private void initSidePositionObserver() {
        if (this.mContext == null) {
            Log.w(TAG, "mContext is null");
            return;
        }
        this.mSidePositionObserver = new ContentObserver(null) {
            /* class com.huawei.sidetouch.HwSideTouchManager.AnonymousClass4 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                HwSideTouchManager.this.updateSidePosition(false);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(TpCommandConstant.KEY_SIDE_POSITION), false, this.mSidePositionObserver, -1);
        updateSidePosition(true);
    }

    private void initUserSwtichObserver() {
        try {
            ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                /* class com.huawei.sidetouch.HwSideTouchManager.AnonymousClass5 */

                public void onUserSwitching(int newUserId) {
                    Log.i(HwSideTouchManager.TAG, "onUserSwitching: " + newUserId);
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    Log.i(HwSideTouchManager.TAG, "onUserSwitchComplete: " + newUserId);
                    if (HwSideTouchManager.this.mSidePositionObserver != null) {
                        HwSideTouchManager.this.mSidePositionObserver.onChange(true);
                    }
                }
            }, TAG);
        } catch (RemoteException e) {
            Log.e(TAG, "registerUserSwitchObserver fail");
        }
    }

    public void notifySideTouchManager(int event) {
        Context context;
        Log.i(TAG, "notifySideTouchManager : " + event);
        boolean isSupportSideScreen = false;
        HwExtDisplaySizeUtil extdiaplyInstance = HwExtDisplaySizeUtil.getInstance();
        if (extdiaplyInstance != null && extdiaplyInstance.hasSideInScreen()) {
            isSupportSideScreen = true;
        }
        if (!isSupportSideScreen) {
            Log.d(TAG, "not support side screen");
            return;
        }
        if (sHwSideViberateManager == null && (context = this.mContext) != null) {
            sHwSideViberateManager = HwSideVibrationManager.getInstance(context);
        }
        HwSideVibrationManager hwSideVibrationManager = sHwSideViberateManager;
        if (hwSideVibrationManager != null) {
            hwSideVibrationManager.notifyThpEvent(event);
        }
    }

    public void updateTPModeForSingleHandeMode(String value) {
        if (value != null) {
            Log.i(TAG, "enter updateTPModeForSingleHandeMode");
            updateSideTouchVolumeMode();
        }
    }

    public void queryHandStatus() {
        int[] result = getSideTouchStatus(null);
        if (result != null) {
            Log.i(TAG, "queryHandStatue is: " + result[0]);
            this.mHandStatus = result[0];
            return;
        }
        Log.e(TAG, "queryHandStatue error");
    }

    public void onVolumeEvent(KeyEvent event, boolean isSideEvent) {
        if (!isSideEvent) {
            this.mEventSource = 0;
        } else if (isSideTriggerEvent(event)) {
            this.mEventSource = 1;
            queryHandStatus();
        }
    }

    public String getSideConfig() {
        return this.mSideConfig;
    }

    public boolean isSideConfigEnabled() {
        return isSideConfigEnabled(this.mSideConfig);
    }

    public boolean isSideConfigEnabled(String config) {
        return config != null && !"1,0".equals(config);
    }

    public void registerCallback(IHwSideTouchCallback callback) {
        this.mCallback = callback;
    }

    public void unregisterCallback() {
        this.mCallback = null;
    }

    public void notifySendVolumeKeyToSystem(KeyEvent event) {
        if (event == null) {
            Log.e(TAG, "event is null");
            return;
        }
        if ((event.getAction() == 0) && isSideTriggerEvent(event)) {
            this.mVolumeCommandCalledCnt++;
            Log.d(TAG, "triggered(not keygard) times:" + this.mVolumeCommandCalledCnt);
        }
    }

    private boolean isSideTriggerEvent(KeyEvent event) {
        if (event.getKeyCode() != 25 || (event.getFlags() & 4096) == 0) {
            return false;
        }
        return true;
    }
}
