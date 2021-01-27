package com.android.server.display;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.WifiDisplay;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.appactcontrol.AppActConstant;
import com.huawei.android.hardware.display.HwWifiDisplayParameters;
import com.huawei.android.net.wifi.p2p.WifiP2pManagerCommonEx;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.displayengine.IDisplayEngineService;
import huawei.hiview.HiTrace;
import huawei.hiview.HiTraceId;

public class HwWifiDisplayControllerEx implements IHwWifiDisplayControllerEx {
    private static final int BITRATE_2G_BASE = 6;
    private static final int BITRATE_2G_MAXIMUM = 8;
    private static final int BITRATE_2G_MINIMUM = 3;
    private static final int BITRATE_5G_BASE = 8;
    private static final int BITRATE_5G_MAXIMUM = 12;
    private static final int BITRATE_5G_MINIMUM = 5;
    private static final int BITRATE_BAND_2G = 1;
    private static final int BITRATE_BAND_5G = 2;
    private static final int BITRATE_COLOR_LIMITED = 5;
    private static final int BITRATE_NORMAL_LINKSPEED = 72;
    private static final String EXTRA_LINKSPEED = "linkSpeed";
    private static final String TAG = "HwWifiDisplayControllerEx";
    private static final String VIDEO_PLAY_STATE_FULL_SCREEN = "HWE_VIDEO_START";
    private static final String VIDEO_PLAY_STATE_IDLE = "HWE_VIDEO_STOP";
    private static final String WFD_LINKSPEED_INFO = "com.huawei.net.wifi.p2p.LINK_SPEED";
    private boolean isColorHoldNeeded = false;
    private int mBitrateMax;
    private int mBitrateMin;
    private Context mContext;
    private int mConvertRatio = 9;
    private HiTraceId mCurrentTraceId = null;
    private Handler mHandler;
    private HiTrace mHiTrace = null;
    private boolean mIsContentProtectionRequired = true;
    private boolean mIsUibcRequired = true;
    boolean mIsWorkOn2G = false;
    private int mLastBitrate;
    private int mLastScanChannelId;
    private int mMiracastConnectionErrorCode = -1;
    private PowerKit mPowerKit;
    private int mProjectionScene = 1;
    private PowerKit.Sink mStateRecognitionListener = new PowerKit.Sink() {
        /* class com.android.server.display.HwWifiDisplayControllerEx.AnonymousClass3 */

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            boolean colorNeedBk = HwWifiDisplayControllerEx.this.isColorHoldNeeded;
            if (stateType == 10016 || stateType == 10017) {
                HwWifiDisplayControllerEx.this.isColorHoldNeeded = false;
                if (stateType == 10016 && HwWifiDisplayControllerEx.this.mWfdc.getmRemoteDisplay() != null) {
                    Slog.i(HwWifiDisplayControllerEx.TAG, "video play idle.");
                    HwWifiDisplayControllerEx.this.mWfdc.getmRemoteDisplay().sendWifiDisplayAction(HwWifiDisplayControllerEx.VIDEO_PLAY_STATE_IDLE);
                }
            } else if (eventType == 1) {
                HwWifiDisplayControllerEx.this.isColorHoldNeeded = true;
                if (stateType == 10015 && HwWifiDisplayControllerEx.this.mWfdc.getmRemoteDisplay() != null) {
                    Slog.i(HwWifiDisplayControllerEx.TAG, "video play with full screen");
                    HwWifiDisplayControllerEx.this.mWfdc.getmRemoteDisplay().sendWifiDisplayAction(HwWifiDisplayControllerEx.VIDEO_PLAY_STATE_FULL_SCREEN);
                }
            } else {
                HwWifiDisplayControllerEx.this.isColorHoldNeeded = false;
            }
            if (colorNeedBk != HwWifiDisplayControllerEx.this.isColorHoldNeeded) {
                HwWifiDisplayControllerEx hwWifiDisplayControllerEx = HwWifiDisplayControllerEx.this;
                hwWifiDisplayControllerEx.changeVideoBitrate(hwWifiDisplayControllerEx.mLastBitrate * HwWifiDisplayControllerEx.this.mConvertRatio);
            }
            Slog.i(HwWifiDisplayControllerEx.TAG, "isColorHoldNeeded:" + HwWifiDisplayControllerEx.this.isColorHoldNeeded);
        }
    };
    private String mVerificaitonCode = null;
    private IWifiDisplayControllerInner mWfdc;
    private final BroadcastReceiver mWifiP2pReceiver = new BroadcastReceiver() {
        /* class com.android.server.display.HwWifiDisplayControllerEx.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (HwWifiDisplayControllerEx.WFD_LINKSPEED_INFO.equals(intent.getAction())) {
                int linkSpeed = intent.getIntExtra(HwWifiDisplayControllerEx.EXTRA_LINKSPEED, 0);
                Slog.i(HwWifiDisplayControllerEx.TAG, "receive linkSpeed: " + linkSpeed);
                HwWifiDisplayControllerEx.this.changeVideoBitrate(linkSpeed);
            }
        }
    };

    public HwWifiDisplayControllerEx(IWifiDisplayControllerInner wfdc, Context context, Handler handler) {
        this.mWfdc = wfdc;
        this.mContext = context;
        this.mHandler = handler;
        this.mLastScanChannelId = 0;
        this.mHiTrace = HwFrameworkFactory.getHiTrace();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WFD_LINKSPEED_INFO);
        this.mContext.registerReceiver(this.mWifiP2pReceiver, intentFilter, null, this.mHandler);
    }

    public void saveCurrentTraceId() {
        Slog.i(TAG, "in saveCurrentTraceId");
        this.mCurrentTraceId = this.mHiTrace.getId();
        if (!this.mCurrentTraceId.isValid()) {
            Slog.d(TAG, "mCurrentTraceId is not valid!");
        }
    }

    public void recoverTraceId() {
        Slog.i(TAG, "in recoverTraceId");
        HiTraceId hiTraceId = this.mCurrentTraceId;
        if (hiTraceId == null) {
            Slog.e(TAG, "in recoverTraceId, mCurrentTraceId == null");
            return;
        }
        this.mHiTrace.setId(hiTraceId);
        if (!this.mCurrentTraceId.isValid()) {
            Slog.i(TAG, "in recoverTraceId, mCurrentTraceId is not valid!");
        }
    }

    public void requestStartScan(int channelID) {
        this.mLastScanChannelId = channelID;
        this.mWfdc.requestStartScanInner();
        this.mLastScanChannelId = 0;
    }

    public boolean tryDiscoverPeersEx() {
        if (this.mLastScanChannelId == 0) {
            return false;
        }
        Slog.d(TAG, "used tryDiscoverPeersEx " + this.mLastScanChannelId);
        WifiP2pManagerCommonEx.discoverPeers(this.mWfdc.getWifiP2pChannelInner(), this.mLastScanChannelId, new WifiP2pManager.ActionListener() {
            /* class com.android.server.display.HwWifiDisplayControllerEx.AnonymousClass2 */

            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onSuccess() {
                if (HwWifiDisplayControllerEx.this.mWfdc.getmDiscoverPeersInProgress()) {
                    HwWifiDisplayControllerEx.this.mWfdc.requestPeersEx();
                }
            }

            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onFailure(int reason) {
                Slog.d(HwWifiDisplayControllerEx.TAG, "Discover peers failed with reason " + reason + ".");
            }
        });
        this.mWfdc.postDelayedDiscover();
        return true;
    }

    public void setConnectParameters(boolean isSupportHdcp, boolean isUibcError, HwWifiDisplayParameters parameters) {
        this.mIsContentProtectionRequired = !isSupportHdcp;
        this.mIsUibcRequired = !isUibcError;
        if (parameters != null) {
            this.mVerificaitonCode = parameters.getVerificaitonCode();
            this.mProjectionScene = parameters.getProjectionScene();
        }
    }

    public void setDisplayParameters() {
        if (this.mWfdc.getmRemoteDisplay() != null) {
            StringBuilder param = new StringBuilder();
            param.append("isCPRequired=");
            boolean z = this.mIsContentProtectionRequired;
            String str = AppActConstant.VALUE_TRUE;
            param.append(z ? str : AppActConstant.VALUE_FALSE);
            param.append(";isUibcRequired=");
            if (!this.mIsUibcRequired) {
                str = AppActConstant.VALUE_FALSE;
            }
            param.append(str);
            String str2 = this.mVerificaitonCode;
            if (str2 != null && str2.length() > 0) {
                param.append(";vcode=");
                param.append(this.mVerificaitonCode);
            }
            param.append(";projectionScene=");
            param.append(this.mProjectionScene);
            this.mWfdc.getmRemoteDisplay().setDisplayParameters(param.toString());
        }
    }

    public void resetDisplayParameters() {
        this.mIsContentProtectionRequired = true;
        this.mIsUibcRequired = true;
        this.mVerificaitonCode = "";
        this.mProjectionScene = 1;
    }

    public void checkVerificationResult(boolean isRight) {
        if (this.mWfdc.getmRemoteDisplay() != null) {
            Slog.i(TAG, "VerificationResult " + isRight);
            this.mWfdc.getmRemoteDisplay().checkVerificationResult(isRight);
            if (!isRight) {
                this.mWfdc.disconnectInner();
            }
        }
    }

    public void setVideoBitrate() {
        if (this.mWfdc.getmRemoteDisplay() != null) {
            if (this.mIsWorkOn2G) {
                this.mLastBitrate = 6;
                this.mBitrateMin = 3;
                this.mBitrateMax = 8;
                this.mConvertRatio = 12;
            } else {
                this.mLastBitrate = 8;
                this.mBitrateMin = 5;
                this.mBitrateMax = 12;
                this.mConvertRatio = 9;
            }
            this.mWfdc.getmRemoteDisplay().setVideoBitrate(this.mLastBitrate);
        }
    }

    public void setWorkFrequency(int frequency) {
        boolean z = true;
        if (convertFrequencyToBand(frequency) != 1) {
            z = false;
        }
        this.mIsWorkOn2G = z;
    }

    private int convertFrequencyToBand(int frequency) {
        Slog.i(TAG, "miracast work on frequency : " + frequency);
        return (frequency < 2412 || frequency > 2484) ? 2 : 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeVideoBitrate(int linkSpeed) {
        int usefulBandwith = linkSpeed / this.mConvertRatio;
        if (this.isColorHoldNeeded && usefulBandwith < 5) {
            usefulBandwith = 5;
        }
        if (SystemProperties.getInt("wfd.config.bitrate", 0) != 0) {
            usefulBandwith = SystemProperties.getInt("wfd.config.bitrate", 0);
        }
        Slog.i(TAG, "usefulBandwith: " + usefulBandwith + ", mLastBitrate " + this.mLastBitrate);
        int i = this.mLastBitrate;
        if (usefulBandwith < i) {
            if (usefulBandwith < this.mBitrateMin) {
                usefulBandwith = this.mBitrateMin;
            }
        } else if (usefulBandwith > i && usefulBandwith > this.mBitrateMax) {
            usefulBandwith = this.mBitrateMax;
        }
        if (usefulBandwith != this.mLastBitrate && this.mWfdc.getmRemoteDisplay() != null) {
            this.mLastBitrate = usefulBandwith;
            Slog.i(TAG, "update videoBitrate: " + this.mLastBitrate);
            this.mWfdc.getmRemoteDisplay().setVideoBitrate(usefulBandwith);
        }
    }

    public void registerPGStateEvent() {
        try {
            this.mPowerKit = PowerKit.getInstance();
            if (this.mPowerKit != null) {
                this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT);
                this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT);
                this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_START);
                this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_END);
                this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT);
                this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_CAMERA_END);
                this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT);
                return;
            }
            Slog.i(TAG, "mPowerKit is null, init failed !!!");
        } catch (RemoteException e) {
            this.mPowerKit = null;
            Slog.i(TAG, "enableStateEvent failed !!!");
        }
    }

    public void unregisterPGStateEvent() {
        PowerKit powerKit = this.mPowerKit;
        if (powerKit != null) {
            try {
                powerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT);
                this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT);
                this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_START);
                this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_END);
                this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT);
                this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_CAMERA_END);
                this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT);
            } catch (RemoteException e) {
                Slog.i(TAG, "disableStateEvent failed !!!");
            } catch (Throwable th) {
                this.mPowerKit = null;
                throw th;
            }
            this.mPowerKit = null;
        }
    }

    public void advertisDisplayCasting(final WifiDisplay display) {
        Slog.i(TAG, "advertisDisplayCasting ");
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.HwWifiDisplayControllerEx.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                HwWifiDisplayControllerEx.this.mWfdc.getmListener().onDisplayCasting(display);
            }
        });
    }

    public void sendWifiDisplayAction(String action) {
        if (this.mWfdc.getmRemoteDisplay() != null) {
            Slog.i(TAG, "sendWifiDisplayAction " + action);
            this.mWfdc.getmRemoteDisplay().sendWifiDisplayAction(action);
        }
    }

    public void updateConnectionErrorCode(int errorCode) {
        this.mMiracastConnectionErrorCode = errorCode;
    }

    public int getConnectionErrorCode() {
        return this.mMiracastConnectionErrorCode;
    }

    public void displayDataNotify(final String displayData) {
        Slog.i(TAG, "displayDataNotify ");
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.HwWifiDisplayControllerEx.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                HwWifiDisplayControllerEx.this.mWfdc.getmListener().onDisplayDataInfo(displayData);
            }
        });
    }

    public void removeSharelinkGroup(WifiP2pManager.Channel wifiP2pChannel, WifiP2pManager.ActionListener listener) {
        Slog.i(TAG, "removeSharelinkGroup in");
        WifiP2pManagerCommonEx.sharelinkRemoveGroup(wifiP2pChannel, "WifiDisplay", listener);
    }
}
