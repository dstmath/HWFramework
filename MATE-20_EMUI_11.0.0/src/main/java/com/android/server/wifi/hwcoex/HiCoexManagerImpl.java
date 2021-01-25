package com.android.server.wifi.hwcoex;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.android.server.wifi.HiCoexManager;
import com.android.server.wifi.WifiNative;
import java.util.List;

public class HiCoexManagerImpl implements HiCoexManager {
    private static final String TAG = "HiCoexManagerImpl";
    private static HiCoexManagerImpl mInstance;
    private Context mContext;
    private Handler mHandler;
    private HiCoexArbitration mHiCoexArbitration;
    private HiCoexChrImpl mHiCoexChrImpl = null;
    private HiCoexReceiver mHiCoexReceiver;
    private boolean mIsForegroundScanOn = false;
    private boolean mIsWifiConnecting = false;
    private WifiNative mWifiNative;

    private HiCoexManagerImpl(Context context, WifiNative wifiNative) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        initCoexHandler();
    }

    public static HiCoexManager createHiCoexManager(Context context, WifiNative wifiNative) {
        if (mInstance == null) {
            mInstance = new HiCoexManagerImpl(context, wifiNative);
        }
        return mInstance;
    }

    public static HiCoexManager getInstance() {
        return mInstance;
    }

    public static HiCoexManagerImpl getHiCoexManagerImpl() {
        return mInstance;
    }

    public List<Integer> getRecommendWiFiChannel() {
        return this.mHiCoexReceiver.getRecommendWiFiChannel();
    }

    public List<Integer> getDeprecatedWiFiChannel() {
        return this.mHiCoexReceiver.getDeprecatedWiFiChannel();
    }

    public void notifyForegroundScan(boolean isOn, String packageName) {
        if (!(this.mIsForegroundScanOn ^ isOn)) {
            HiCoexUtils.logV(TAG, "skip notifyForegroundScan, now:" + this.mIsForegroundScanOn + ", param:" + isOn);
            return;
        }
        if (isOn) {
            if (HiCoexUtils.isForegroundScanInList(packageName)) {
                this.mHandler.sendEmptyMessageDelayed(11, 3000);
            } else {
                HiCoexUtils.logV(TAG, "foregroundScan not in list, packageName:" + packageName);
                return;
            }
        } else if (this.mHandler.hasMessages(11)) {
            this.mHandler.removeMessages(11);
        }
        this.mIsForegroundScanOn = isOn;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(10, HiCoexUtils.booleanToInt(isOn), 0));
    }

    public void notifyWifiConnecting(boolean isStart) {
        if (!(this.mIsWifiConnecting ^ isStart)) {
            HiCoexUtils.logV(TAG, "skip notifyWifiConnecting now:" + this.mIsWifiConnecting + ", param:" + isStart);
            return;
        }
        if (isStart) {
            this.mHandler.sendEmptyMessageDelayed(13, 10000);
        } else if (this.mHandler.hasMessages(13)) {
            this.mHandler.removeMessages(13);
        }
        this.mIsWifiConnecting = isStart;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(12, HiCoexUtils.booleanToInt(isStart), 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initCoexManagerImpl() {
        HiCoexUtils.initialDebugEnable();
        HiCoexChrImpl.createHiCoexChrImpl(this.mContext);
        this.mHiCoexChrImpl = HiCoexChrImpl.getInstance();
        this.mHiCoexReceiver = new HiCoexReceiver(this.mContext, this.mHandler);
        this.mHiCoexArbitration = new HiCoexArbitration(this.mContext, this.mHiCoexReceiver, this.mWifiNative);
        this.mHiCoexReceiver.startMonitor();
    }

    private void initCoexHandler() {
        HandlerThread handlerThread = new HandlerThread("hicoex_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.hwcoex.HiCoexManagerImpl.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        HiCoexManagerImpl.this.initCoexManagerImpl();
                        return;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 21:
                    case 22:
                    case 23:
                        HiCoexManagerImpl.this.mHiCoexArbitration.onReceiveEvent(msg);
                        return;
                    case 9:
                    case 14:
                    case 15:
                    case 20:
                    default:
                        return;
                }
            }
        };
        this.mHandler.sendEmptyMessage(1);
    }
}
