package com.android.server.pc;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Message;
import android.os.UEventObserver;
import android.provider.Settings;
import android.util.HwPCUtils;
import android.view.Display;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import java.util.ArrayList;

public final class HwPCMultiDisplaysManager {
    private static final String DP_STATE_DEVPATH = "DEVPATH=/devices/virtual/hw_typec/typec";
    public static final int FIRST_DISPLAY = 0;
    public static final int SECOND_DISPLAY = 1;
    private static final String TAG = "HwPCMultiDisplaysManager";
    private ArrayList<CastingDisplay> mCastingDisplays = new ArrayList<>();
    private Context mContext;
    private DisplayManager mDisplayManager;
    private final UEventObserver mDpObserver = new UEventObserver() {
        public void onUEvent(UEventObserver.UEvent event) {
            HwPCUtils.log(HwPCMultiDisplaysManager.TAG, "DP state event = " + event);
            if (event != null) {
                String DP_STATE = event.get("DP_STATE");
                HwPCUtils.log(HwPCMultiDisplaysManager.TAG, "DP state DP_STATE: " + DP_STATE + ", mDpState = " + HwPCMultiDisplaysManager.this.mDpState);
                if (AwareJobSchedulerConstants.BAR_STATUS_ON.equals(DP_STATE) && !HwPCMultiDisplaysManager.this.mDpState) {
                    HwPCMultiDisplaysManager.this.mService.notifyDpState(true);
                } else if (!AwareJobSchedulerConstants.BAR_STATUS_OFF.equals(DP_STATE) || !HwPCMultiDisplaysManager.this.mDpState) {
                    HwPCUtils.log(HwPCMultiDisplaysManager.TAG, "onUEvent wrong state");
                } else {
                    HwPCMultiDisplaysManager.this.mService.notifyDpState(false);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mDpState = false;
    private Handler mHandler;
    private boolean mInitialDpConnectAfterBoot = true;
    /* access modifiers changed from: private */
    public HwPCManagerService mService;

    public static class CastingDisplay {
        public int mDisplayId;
        public int mType;

        public CastingDisplay(int displayId, int type) {
            this.mDisplayId = displayId;
            this.mType = type;
        }
    }

    public HwPCMultiDisplaysManager(Context context, Handler handler, HwPCManagerService service) {
        this.mContext = context;
        this.mHandler = handler;
        this.mService = service;
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        this.mCastingDisplays.add(new CastingDisplay(-1, 0));
        this.mCastingDisplays.add(new CastingDisplay(-1, 0));
        this.mDpObserver.startObserving(DP_STATE_DEVPATH);
        HwPCUtils.log(TAG, "HwPCMultiDisplaysManager, mDpState = " + this.mDpState);
    }

    public boolean isExternalDisplay(int displayId) {
        return (displayId == -1 || displayId == 0) ? false : true;
    }

    public int getDisplayType(int displayid) {
        if (displayid == -1 || displayid == 0) {
            return 0;
        }
        if (this.mDisplayManager == null) {
            this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        }
        if (this.mDisplayManager != null) {
            Display display = this.mDisplayManager.getDisplay(displayid);
            if (display != null) {
                return display.getType();
            }
        }
        return 0;
    }

    public CastingDisplay get1stDisplay() {
        return this.mCastingDisplays.get(0);
    }

    public CastingDisplay get2ndDisplay() {
        return this.mCastingDisplays.get(1);
    }

    public void notifyDpState(boolean dpState) {
        HwPCUtils.log(TAG, "notifyDpState dpState = " + dpState + ", mDpState = " + this.mDpState);
        if (this.mDpState != dpState) {
            this.mDpState = dpState;
            if (!dpState) {
                if (this.mCastingDisplays.get(0).mType == 2) {
                    this.mCastingDisplays.get(0).mType = 0;
                }
                if (this.mCastingDisplays.get(1).mType == 2) {
                    this.mCastingDisplays.get(1).mType = 0;
                }
            }
        }
    }

    public void checkInitialDpConnectAfterBoot(int displayid) {
        HwPCUtils.log(TAG, "checkInitialDpConnectAfterBoot mInitialDpConnectAfterBoot = " + this.mInitialDpConnectAfterBoot);
        if (this.mInitialDpConnectAfterBoot && getDisplayType(displayid) == 2) {
            if (!this.mDpState) {
                this.mService.notifyDpState(true);
            }
            this.mInitialDpConnectAfterBoot = false;
        }
    }

    public boolean handleTwoDisplaysInDisplayAdded(int displayId) {
        int type = getDisplayType(displayId);
        HwPCUtils.log(TAG, "handleTwoDisplaysInDisplayAdded type = " + type);
        if (displayId == this.mCastingDisplays.get(0).mDisplayId && type == this.mCastingDisplays.get(0).mType) {
            HwPCUtils.log(TAG, "handleTwoDisplaysInDisplayAdded add the same display as 1st display");
            return true;
        } else if (displayId == this.mCastingDisplays.get(1).mDisplayId && type == this.mCastingDisplays.get(1).mType) {
            HwPCUtils.log(TAG, "handleTwoDisplaysInDisplayAdded add the same display as 2nd display");
            return false;
        } else {
            boolean needContinueToHandleProjMode = false;
            if (type == 2) {
                if (isExternalDisplay(this.mCastingDisplays.get(0).mDisplayId)) {
                    if (isExternalDisplay(this.mCastingDisplays.get(1).mDisplayId)) {
                        HwPCUtils.log(TAG, "unkown error during HDMI display added.");
                    } else if (this.mCastingDisplays.get(0).mType == 3 || this.mCastingDisplays.get(0).mType == 5) {
                        this.mCastingDisplays.get(1).mDisplayId = displayId;
                        this.mCastingDisplays.get(1).mType = type;
                        HwPCUtils.log(TAG, "adding HDMI display when WIFI display added.");
                    } else {
                        HwPCUtils.log(TAG, "adding HDMI display when HDMI display added, is onDisplayRemoved call lost ?");
                        needContinueToHandleProjMode = true;
                    }
                    if (!needContinueToHandleProjMode) {
                        return false;
                    }
                }
            } else if (type == 3 || type == 5) {
                if (!isExternalDisplay(this.mCastingDisplays.get(0).mDisplayId) && this.mDpState && this.mCastingDisplays.get(0).mType == 2) {
                    if (!isExternalDisplay(this.mCastingDisplays.get(1).mDisplayId)) {
                        this.mCastingDisplays.get(1).mDisplayId = displayId;
                        this.mCastingDisplays.get(1).mType = type;
                        HwPCUtils.log(TAG, "adding WIFI display when HDMI display removed but DP still connected.");
                    } else {
                        HwPCUtils.log(TAG, "unkown error during WIFI display added.");
                    }
                    return false;
                } else if (isExternalDisplay(this.mCastingDisplays.get(0).mDisplayId) && !isExternalDisplay(this.mCastingDisplays.get(1).mDisplayId)) {
                    if (this.mCastingDisplays.get(0).mType == 2) {
                        this.mCastingDisplays.get(1).mDisplayId = displayId;
                        this.mCastingDisplays.get(1).mType = type;
                        HwPCUtils.log(TAG, "adding WIFI display when HDMI display added.");
                    } else if (!this.mDpState) {
                        HwPCUtils.log(TAG, "adding WIFI display when WIFI display added, is onDisplayRemoved call lost ?");
                        needContinueToHandleProjMode = true;
                    }
                    if (!needContinueToHandleProjMode) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public boolean is4KHdmi1stDisplayRemoved(int displayId) {
        if (this.mDpState && displayId == this.mCastingDisplays.get(0).mDisplayId && this.mCastingDisplays.get(0).mType == 2) {
            return true;
        }
        return false;
    }

    public void handlelstDisplayInDisplayRemoved() {
        if (isExternalDisplay(this.mCastingDisplays.get(1).mDisplayId)) {
            HwPCUtils.log(TAG, "handlelstDisplayInDisplayRemoved SECOND_DISPLAY.mDisplayId = " + this.mCastingDisplays.get(1).mDisplayId);
            boolean z = false;
            this.mCastingDisplays.get(0).mDisplayId = this.mCastingDisplays.get(1).mDisplayId;
            this.mCastingDisplays.get(1).mDisplayId = -1;
            this.mCastingDisplays.get(0).mType = this.mCastingDisplays.get(1).mType;
            this.mCastingDisplays.get(1).mType = 0;
            HwPCUtils.setPhoneDisplayID(this.mCastingDisplays.get(0).mDisplayId);
            Settings.Secure.putInt(this.mContext.getContentResolver(), "selected-proj-mode", 0);
            Settings.Global.putInt(this.mContext.getContentResolver(), "is_display_device_connected", 0);
            if (this.mCastingDisplays.get(0).mType == 3) {
                z = true;
            }
            HwPCUtils.setIsWifiMode(z);
            this.mService.mProjMode = HwPCUtils.ProjectionMode.PHONE_MODE;
            this.mHandler.removeMessages(1);
            Message msg = this.mHandler.obtainMessage(1);
            msg.obj = this.mService.mProjMode;
            this.mHandler.sendMessage(msg);
        }
    }
}
