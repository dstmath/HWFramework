package com.huawei.server.sidetouch;

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
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.UserSwitchObserverEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.hardware.input.HwInputManager;
import com.huawei.android.hardware.input.HwSideTouchManagerEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.hwpartsidetouchopt.BuildConfig;

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
    private static HwSideTouchManager sInstance = null;
    private IHwSideTouchCallback mCallback = null;
    private Context mContext;
    private int mCurrentSideTouchMode = INVALID_VOLUNME_MODE;
    private int mEventSource = 0;
    private int mHandStatus = INVALID_VOLUNME_MODE;
    private boolean mIsSystemReady = false;
    private String mSideConfig = null;
    private ContentObserver mSidePositionObserver;
    private ContentObserver mSingleHandObserver;
    private ContentObserver mTalkBackObserver;
    private int mVolumeCommandCalledCnt = 0;

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

    public void systemReady() {
        this.mIsSystemReady = true;
        initSidePositionObserver();
        initUserSwitchObserver();
        initTpInfo();
        initTalkBackObserver();
        initSingleHandObserver();
    }

    public int[] runSideTouchCommand(int commandType, Bundle bundle) {
        if (!this.mIsSystemReady) {
            Log.w(TAG, "system not ready, return");
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
            case HwSideStatusManager.FLAG_SIDE_FEATURE /* 8 */:
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
        HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_VOLUME_GUI_INFO, getVolumeGuiInfoParams(bundle));
    }

    private String getVolumeGuiInfoParams(Bundle bundle) {
        int guiState = bundle.getInt("guiState");
        int startPosX = bundle.getInt(HwSideTouchManagerEx.X_START_POS);
        int endPosX = bundle.getInt(HwSideTouchManagerEx.X_END_POS);
        int startPosY = bundle.getInt(HwSideTouchManagerEx.Y_START_POS);
        int endPosY = bundle.getInt(HwSideTouchManagerEx.Y_END_POS);
        int mapToX = bundle.getInt(HwSideTouchManagerEx.X_MAP_TO);
        int mapToY = bundle.getInt(HwSideTouchManagerEx.Y_MAP_TO);
        return guiState + "#" + startPosX + "#" + endPosX + "#" + startPosY + "#" + endPosY + "#" + mapToX + "#" + mapToY;
    }

    private void setSideTouchRegion(Bundle bundle) {
        String paramEnd;
        String paramStart;
        if (bundle != null) {
            int type = bundle.getInt(HwSideTouchManagerEx.TOUCH_REGION_TYPE);
            Rect rect = (Rect) bundle.getParcelable(HwSideTouchManagerEx.TOUCH_REGION_RECT);
            if (rect == null) {
                Log.e(TAG, "setSideTouchRegion rect is null");
                return;
            }
            if (type == 0) {
                paramStart = HwSideTouchManagerEx.VOLUME_GREEN_AREA_START + "#" + rect.top;
                paramEnd = HwSideTouchManagerEx.VOLUME_GREEN_AREA_END + "#" + rect.bottom;
            } else if (type == 1) {
                paramStart = HwSideTouchManagerEx.VOLUME_ACTIVE_AREA_START + "#" + rect.top;
                paramEnd = HwSideTouchManagerEx.VOLUME_ACTIVE_AREA_END + "#" + rect.bottom;
            } else {
                Log.w(TAG, "setSideTouchRegion type error");
                return;
            }
            HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_TUNE_TIME_PRMT, paramStart);
            HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_TUNE_TIME_PRMT, paramEnd);
        }
    }

    private int[] getSideTouchRegion(Bundle bundle) {
        String paramStart;
        String paramStart2;
        if (bundle == null) {
            return null;
        }
        int type = bundle.getInt(HwSideTouchManagerEx.TOUCH_REGION_TYPE);
        if (type == 0) {
            paramStart2 = HwSideTouchManagerEx.VOLUME_GREEN_AREA_START;
            paramStart = HwSideTouchManagerEx.VOLUME_GREEN_AREA_END;
        } else if (type == 1) {
            paramStart2 = HwSideTouchManagerEx.VOLUME_ACTIVE_AREA_START;
            paramStart = HwSideTouchManagerEx.VOLUME_ACTIVE_AREA_END;
        } else {
            Log.w(TAG, "setSideTouchRegion type error " + type);
            return null;
        }
        String startRegion = HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_GET_TUNE_TIME_PRMT, paramStart2);
        Log.i(TAG, "getSideTouchRegion startRegion " + startRegion);
        String endRegion = HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_GET_TUNE_TIME_PRMT, paramStart);
        Log.i(TAG, "getSideTouchRegion endRegion " + endRegion);
        return new int[]{getRegion(startRegion, true), getRegion(endRegion, false)};
    }

    private int getRegion(String region, boolean isStart) {
        String[] splitResult;
        int defaultRegion = isStart ? REGION_START_DEFAULT : REGION_END_DEFAULT;
        if (region == null || (splitResult = region.split("#")) == null || splitResult.length != 2 || !STATUS_OK.equals(splitResult[0])) {
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
        int mode = bundle.getInt(HwSideTouchManagerEx.VOLUME_GESTURE);
        if (mode < 0) {
            Log.w(TAG, "setSideTouchVolumeMode mode error " + mode);
            return;
        }
        HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_TUNE_TIME_PRMT, HwSideTouchManagerEx.VOLUME_GESTURE + "#" + mode);
    }

    private int[] getSideTouchVolumeMode() {
        String volumeMode = HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_GET_TUNE_TIME_PRMT, HwSideTouchManagerEx.VOLUME_GESTURE);
        int[] sideVolumeModes = new int[1];
        if (volumeMode != null) {
            int getMode = getModeFromTp(volumeMode);
            if (getMode < 0) {
                Log.w(TAG, "getSideTouchVolumeMode mode error " + getMode);
                sideVolumeModes[0] = HwSideTouchManagerEx.DEFAULT_VOLUME_MODE;
                return sideVolumeModes;
            }
            sideVolumeModes[0] = getMode;
        } else {
            Log.w(TAG, "getSideTouchVolumeMode mode get error , return mapping as defalut");
            sideVolumeModes[0] = HwSideTouchManagerEx.DEFAULT_VOLUME_MODE;
        }
        return sideVolumeModes;
    }

    private int[] getSideTouchStatus(Bundle bundle) {
        if (bundle != null) {
            Log.i(TAG, "getSideTouchStatus:" + this.mHandStatus + "," + this.mEventSource);
            return new int[]{this.mHandStatus, this.mEventSource};
        }
        String volumeMode = HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_VOLUME_GET_STATE, (String) null);
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
        } else if (bundle.getInt(HwSideTouchManagerEx.EXTRA_INFO) == HwSideTouchManagerEx.EXTRA_INFO_VOLUME_PANEL_CNT) {
            return new int[]{this.mVolumeCommandCalledCnt};
        } else {
            return null;
        }
    }

    private int getModeFromTp(String volumeMode) {
        String[] splitResult;
        if (volumeMode == null || (splitResult = volumeMode.split("#")) == null || !STATUS_OK.equals(splitResult[0])) {
            return INVALID_VOLUNME_MODE;
        }
        try {
            return Integer.parseInt(splitResult[1]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException");
            return INVALID_VOLUNME_MODE;
        }
    }

    private void setSideTouchMapping(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "setSideTouchMapping bundle is null");
            return;
        }
        Rect fromRect = (Rect) bundle.getParcelable(HwSideTouchManagerEx.FROM_RECT);
        Rect toRect = (Rect) bundle.getParcelable(HwSideTouchManagerEx.TO_RECT);
        if (fromRect == null || toRect == null) {
            Log.e(TAG, "setSideTouchMapping fromRect or toRect is null");
            return;
        }
        String params = fromRect.left + "#" + fromRect.right + "#" + fromRect.top + "#" + fromRect.bottom + "#" + toRect.left + "#" + toRect.right + "#" + toRect.top + "#" + toRect.bottom;
        Log.i(TAG, "setSideTouchMapping params = " + params);
        ajustSideTouchRegion(bundle, true, true);
        HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_MAP_SIDE_AREA, params);
    }

    private void enableSideTouch(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "enableSideTouch bundle is null");
            return;
        }
        boolean isEnable = bundle.getBoolean(HwSideTouchManagerEx.ENABLE);
        Rect rect = (Rect) bundle.getParcelable(HwSideTouchManagerEx.TO_RECT);
        if (rect == null) {
            Log.e(TAG, "enableSideTouch rect is null");
            return;
        }
        String params = rect.left + "#" + rect.right + "#" + rect.top + "#" + rect.bottom;
        Log.i(TAG, "enableSideTouch isEnable =" + isEnable + ", params = " + params);
        if (isEnable) {
            HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_EXPOSE_SIDE_AREA, params);
        } else {
            HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_REMOVE_SIDE_AREA, params);
        }
        ajustSideTouchRegion(bundle, isEnable, false);
    }

    private void ajustSideTouchRegion(Bundle bundle, boolean isEnable, boolean isMapping) {
        int tuneTop;
        if (bundle == null) {
            Log.e(TAG, "enableSideTouch bundle is null");
            return;
        }
        String startRegion = HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_GET_TUNE_TIME_PRMT, HwSideTouchManagerEx.VOLUME_ACTIVE_AREA_START);
        int tuneTop2 = getRegion(startRegion, true);
        Log.i(TAG, "AjustSideTouchRegion startRegion =" + startRegion + ", oldTop =" + tuneTop2);
        Rect rect = (Rect) bundle.getParcelable(isMapping ? HwSideTouchManagerEx.FROM_RECT : HwSideTouchManagerEx.TO_RECT);
        if (rect == null) {
            Log.e(TAG, "ajustSideTouchRegion rect beyond the active area.");
        } else if ((rect.bottom + rect.top) / 2 > REGION_END_DEFAULT) {
            Log.i(TAG, "enableSideTouch rect is null");
        } else {
            if (isEnable && rect.bottom > tuneTop2 && tuneTop2 >= rect.top) {
                tuneTop = rect.bottom + REGION_TUNE_TO_GAME;
                Log.i(TAG, "AjustSideTouchRegion newTop =" + tuneTop);
            } else if (!isEnable) {
                tuneTop = REGION_START_DEFAULT;
            } else {
                return;
            }
            HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_TUNE_TIME_PRMT, HwSideTouchManagerEx.VOLUME_GREEN_AREA_START + "#" + tuneTop);
            String paramNewStart = HwSideTouchManagerEx.VOLUME_ACTIVE_AREA_START + "#" + tuneTop;
            HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_TUNE_TIME_PRMT, paramNewStart);
            Log.i(TAG, "AjustSideTouchRegion paramStart = " + paramNewStart);
        }
    }

    private void setSidePosition(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "setSidePosition bundle is null");
            return;
        }
        int sidePosition = bundle.getInt(HwSideTouchManagerEx.VOLUME_SIED_ENABLED, HwSideTouchManagerEx.SIDE_TOUCH_BOTH);
        HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_TUNE_TIME_PRMT, HwSideTouchManagerEx.VOLUME_SIED_ENABLED + "#" + sidePosition);
    }

    private void notifySideConfig(int sidePosition, boolean isSelfChange) {
        if (sidePosition == HwSideTouchManagerEx.SIDE_TOUCH_DISABLE) {
            this.mSideConfig = "1,0";
        } else if (sidePosition == HwSideTouchManagerEx.SIDE_TOUCH_LEFT) {
            this.mSideConfig = "1,1";
        } else if (sidePosition == HwSideTouchManagerEx.SIDE_TOUCH_RIGHT) {
            this.mSideConfig = SIDE_CONFIG_RIGHTHAND;
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
        int triggerSensitivity = bundle.getInt(HwSideTouchManagerEx.SIDE_TOUCH_TRIGGER_SENSITIVITY);
        int volumeFlickUp = HwSideTouchManagerEx.VOLUME_FLICK_THRESHOLD_MIN;
        int volumeFlickDown = HwSideTouchManagerEx.VOLUME_FLICK_THRESHOLD_MIN;
        int volumeTouchMin = HwSideTouchManagerEx.VOLUME_TOUCH_MIN_THRESHOLD_MIN;
        if (triggerSensitivity != HwSideTouchManagerEx.SIDE_TOUCH_HIGH_TRIGGER_SENSITIVITY) {
            if (triggerSensitivity == HwSideTouchManagerEx.SIDE_TOUCH_MEDIUM_TRIGGER_SENSITIVITY) {
                volumeFlickUp = HwSideTouchManagerEx.VOLUME_FLICK_THRESHOLD_MAX;
                volumeFlickDown = HwSideTouchManagerEx.VOLUME_FLICK_THRESHOLD_MAX;
            } else if (triggerSensitivity == HwSideTouchManagerEx.SIDE_TOUCH_LOW_TRIGGER_SENSITIVITY) {
                volumeFlickUp = HwSideTouchManagerEx.VOLUME_FLICK_THRESHOLD_MAX;
                volumeFlickDown = HwSideTouchManagerEx.VOLUME_FLICK_THRESHOLD_MAX;
                volumeTouchMin = HwSideTouchManagerEx.VOLUME_TOUCH_MIN_THRESHOLD_MAX;
            } else {
                Log.w(TAG, "setSideTouchTriggerSensitivity: param error, triggerSensitivity is " + triggerSensitivity);
                return;
            }
        }
        HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_TUNE_TIME_PRMT, HwSideTouchManagerEx.VOLUME_FLICK_UP + "#" + volumeFlickUp);
        HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_TUNE_TIME_PRMT, HwSideTouchManagerEx.VOLUME_FLICK_DOWN + "#" + volumeFlickDown);
        HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_TUNE_TIME_PRMT, HwSideTouchManagerEx.VOLUME_TOUCH_MIN + "#" + volumeTouchMin);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSidePosition(boolean isSelfChange) {
        int position = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), HwSideTouchManagerEx.KEY_SIDE_POSITION, HwSideTouchManagerEx.SIDE_TOUCH_BOTH, ActivityManagerEx.getCurrentUser());
        Log.i(TAG, "side position onChange: " + position + ", isSelfChange:" + isSelfChange);
        if (position > HwSideTouchManagerEx.SIDE_TOUCH_BOTH || position < HwSideTouchManagerEx.SIDE_TOUCH_DISABLE) {
            Log.w(TAG, "side position error " + position);
            return;
        }
        notifySideConfig(position, isSelfChange);
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
        if (this.mCurrentSideTouchMode == HwSideTouchManagerEx.SIDE_TOUCH_DISABLE) {
            Log.i(TAG, "mCurrentSideTouchMode is disable, do nothing");
            return;
        }
        int newTouchMode = HwSideTouchManagerEx.DEFAULT_VOLUME_MODE;
        boolean isSingleHandMode = getSingleHandEnableState();
        boolean isTalkBackEnabled = getTalkBackEnableState();
        Log.i(TAG, "isSingleHandMode is " + isSingleHandMode + " isTalkBackEnabled is " + isTalkBackEnabled + " mCurrentSideTouchMode is " + this.mCurrentSideTouchMode);
        if (isSingleHandMode || isTalkBackEnabled) {
            newTouchMode = getSideTouchVolumeStepMode(this.mCurrentSideTouchMode);
        }
        if (newTouchMode == INVALID_VOLUNME_MODE) {
            Log.i(TAG, "newTouchMode is INVALID_VOLUNME_MODE, do nothing");
            return;
        }
        Log.i(TAG, "set newTouchMode is " + newTouchMode);
        Bundle bundle = new Bundle();
        bundle.putInt(HwSideTouchManagerEx.VOLUME_GESTURE, newTouchMode);
        runSideTouchCommand(4, bundle);
        Log.i(TAG, "updateSideTouchVolumeMode success");
    }

    public boolean getTalkBackEnableState() {
        if (SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0, -2) == 1) {
            return true;
        }
        return false;
    }

    private boolean getSingleHandEnableState() {
        String singleHandMode = Settings.Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        return singleHandMode != null && !BuildConfig.FLAVOR.equals(singleHandMode);
    }

    private int getSideTouchVolumeStepMode(int currentMode) {
        if (currentMode == HwSideTouchManagerEx.VOLUME_MODE_DOUBLE_SLIDE_MAPPING) {
            return HwSideTouchManagerEx.VOLUME_MODE_DOUBLE_SLIDE_STEP;
        }
        if (currentMode == HwSideTouchManagerEx.VOLUME_MODE_DOUBLE_CLICK_MAPPING) {
            return HwSideTouchManagerEx.VOLUME_MODE_DOUBLE_CLICK_STEP;
        }
        if (currentMode == HwSideTouchManagerEx.VOLUME_MODE_SINGLE_CLICK_MAPPING) {
            return HwSideTouchManagerEx.VOLUME_MODE_SINGLE_CLICK_STEP;
        }
        if (currentMode == HwSideTouchManagerEx.VOLUME_MODE_MAPPING) {
            return HwSideTouchManagerEx.VOLUME_MODE_STEP;
        }
        Log.e(TAG, "current volumeModes is not MAPPING mode, return INVALID_VOLUNME_MODE");
        return INVALID_VOLUNME_MODE;
    }

    private void initTpInfo() {
        Log.i(TAG, "enter initTpInfo, default is " + HwSideTouchManagerEx.DEFAULT_VOLUME_MODE);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            /* class com.huawei.server.sidetouch.HwSideTouchManager.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                boolean isTalkBackEnabled = HwSideTouchManager.this.getTalkBackEnableState();
                Log.i(HwSideTouchManager.TAG, "isTaklBackEnabled " + isTalkBackEnabled);
                if (!isTalkBackEnabled) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(HwSideTouchManagerEx.VOLUME_GESTURE, HwSideTouchManagerEx.DEFAULT_VOLUME_MODE);
                    HwSideTouchManager.this.runSideTouchCommand(4, bundle);
                }
                HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_TUNE_TIME_PRMT, HwSideTouchManagerEx.VOLUME_GREEN_AREA_START + "#" + HwSideTouchManager.REGION_START_DEFAULT);
                String paramNewStart = HwSideTouchManagerEx.VOLUME_ACTIVE_AREA_START + "#" + HwSideTouchManager.REGION_START_DEFAULT;
                HwInputManager.runSideTouchCommand(HwSideTouchManagerEx.COMMAND_SET_TUNE_TIME_PRMT, paramNewStart);
                Log.i(HwSideTouchManager.TAG, "SystemReady paramStart = " + paramNewStart);
            }
        });
    }

    private void initTalkBackObserver() {
        if (this.mContext == null) {
            Log.w(TAG, "mContext is null");
            return;
        }
        this.mTalkBackObserver = new ContentObserver(null) {
            /* class com.huawei.server.sidetouch.HwSideTouchManager.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                Log.i(HwSideTouchManager.TAG, "talkBack status onChange");
                HwSideTouchManager.this.updateSideTouchVolumeMode();
            }
        };
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.Secure.getUriFor(ACCESSIBILITY_SCREENREADER_ENABLED), false, this.mTalkBackObserver, (int) INVALID_VOLUNME_MODE);
        this.mTalkBackObserver.onChange(true);
    }

    private void initSingleHandObserver() {
        this.mSingleHandObserver = new ContentObserver(null) {
            /* class com.huawei.server.sidetouch.HwSideTouchManager.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                Log.i(HwSideTouchManager.TAG, "single hand status onChange");
                HwSideTouchManager.this.updateSideTouchVolumeMode();
            }
        };
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.Global.getUriFor("single_hand_mode"), false, this.mSingleHandObserver, (int) INVALID_VOLUNME_MODE);
        this.mSingleHandObserver.onChange(true);
    }

    private void initSidePositionObserver() {
        this.mSidePositionObserver = new ContentObserver(null) {
            /* class com.huawei.server.sidetouch.HwSideTouchManager.AnonymousClass4 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                HwSideTouchManager.this.updateSidePosition(false);
            }
        };
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.Secure.getUriFor(HwSideTouchManagerEx.KEY_SIDE_POSITION), false, this.mSidePositionObserver, (int) INVALID_VOLUNME_MODE);
        updateSidePosition(true);
    }

    private void initUserSwitchObserver() {
        try {
            ActivityManagerEx.registerUserSwitchObserver(new UserSwitchObserverEx() {
                /* class com.huawei.server.sidetouch.HwSideTouchManager.AnonymousClass5 */

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

    private void queryHandStatus() {
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
