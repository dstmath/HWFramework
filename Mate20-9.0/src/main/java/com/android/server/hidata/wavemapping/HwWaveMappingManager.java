package com.android.server.hidata.wavemapping;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.android.server.hidata.arbitration.HwArbitrationHistoryQoeManager;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ContextManager;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dataprovider.BootBroadcastReceiver;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.HashMap;

public class HwWaveMappingManager {
    private static final String RECEIVE_BOOT_COMPLETED = "android.permission.RECEIVE_BOOT_COMPLETED";
    private static final String TAG = ("WMapping." + HwWaveMappingManager.class.getSimpleName());
    private static HwWaveMappingManager instance = null;
    private BootBroadcastReceiver bootBroadcastReceiver;
    private HandlerThread handlerThread;
    /* access modifiers changed from: private */
    public HwWMStateMachine hwWMStateMachine;
    private IWaveMappingCallback mAppQoeCallback = null;
    /* access modifiers changed from: private */
    public Context mContext;
    private Handler mHandler;
    private IWaveMappingCallback mHiStreamCallback = null;
    private HwArbitrationHistoryQoeManager mHistoryQoE;
    private IWaveMappingCallback mIWaveMappingCallback;
    /* access modifiers changed from: private */
    public ParameterInfo param = null;
    /* access modifiers changed from: private */
    public boolean smInitFinish = false;

    private HwWaveMappingManager(Context mContext2) {
        this.mContext = mContext2;
        ContextManager.getInstance().setContext(mContext2);
        this.hwWMStateMachine = HwWMStateMachine.getInstance(mContext2);
        initControllerHandler();
        this.mHistoryQoE = HwArbitrationHistoryQoeManager.getInstance(this.hwWMStateMachine.getHandler());
        this.bootBroadcastReceiver = new BootBroadcastReceiver(this.mHandler);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");
        this.mContext.registerReceiver(this.bootBroadcastReceiver, filter, RECEIVE_BOOT_COMPLETED, null);
        LogUtil.d("HwWaveMappingManager init completed");
    }

    public static HwWaveMappingManager getInstance(Context mContext2) {
        if (instance == null) {
            instance = new HwWaveMappingManager(mContext2);
        }
        return instance;
    }

    public static HwWaveMappingManager getInstance() {
        return instance;
    }

    private void initControllerHandler() {
        this.handlerThread = new HandlerThread("HwWaveMappingManager_thread");
        this.handlerThread.start();
        this.mHandler = new Handler(this.handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        LogUtil.d("Constant.MSG_REV_SYSBOOT  begin.");
                        if (WifiProCommonUtils.isWifiProLitePropertyEnabled(HwWaveMappingManager.this.mContext)) {
                            LogUtil.d("wavemapping meet wifiprolite,isWifiProLitePropertyEnabled == true");
                            return;
                        } else if (!Constant.checkPath(HwWaveMappingManager.this.mContext)) {
                            LogUtil.e("init failure.");
                            return;
                        } else {
                            if (!HwWaveMappingManager.this.smInitFinish) {
                                HwWaveMappingManager.this.hwWMStateMachine.init();
                                ParameterInfo unused = HwWaveMappingManager.this.param = ParamManager.getInstance().getParameterInfo();
                            }
                            boolean unused2 = HwWaveMappingManager.this.smInitFinish = true;
                            return;
                        }
                    case 2:
                        LogUtil.d("Constant.MSG_REV_SYSSHUTDOWN  begin.");
                        HwWaveMappingManager.this.hwWMStateMachine.handleShutDown();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void registerWaveMappingCallback(IWaveMappingCallback callback, int module) {
        if (callback != null) {
            LogUtil.d("registerWaveMappingCallback, module = " + module);
            if (module == 0) {
                this.mAppQoeCallback = callback;
            } else if (module == 1) {
                this.mHiStreamCallback = callback;
            }
        }
    }

    public void registerWaveMappingCallback(IWaveMappingCallback callback) {
        if (callback != null) {
            LogUtil.d("registerWaveMappingCallback");
            this.mIWaveMappingCallback = callback;
        }
    }

    public void queryWaveMappingInfo(int UID, int appId, int sense, int network) {
        if (this.mIWaveMappingCallback != null) {
            LogUtil.d("queryWaveMappingInfo");
            if (this.smInitFinish) {
                this.mHistoryQoE.queryHistoryQoE(UID, appId, sense, network, this.mIWaveMappingCallback, 1);
                return;
            }
            this.mIWaveMappingCallback.onWaveMappingRespondCallback(UID, 0, network, true, false);
            return;
        }
        LogUtil.w("mIWaveMappingCallback is none");
    }

    public void queryWaveMappingInfo4Back(int UID, int appId, int sense, int network) {
        if (this.mIWaveMappingCallback != null) {
            LogUtil.d("queryWaveMappingInfo for switch back");
            if (this.smInitFinish) {
                this.mHistoryQoE.queryHistoryQoE(UID, appId, sense, network, this.mIWaveMappingCallback, 2);
                return;
            }
            this.mIWaveMappingCallback.onWaveMappingRespond4BackCallback(UID, 0, network, true, false);
            return;
        }
        LogUtil.w("mIWaveMappingCallback is none");
    }

    public HashMap<Integer, String> queryNetPreference(int network) {
        HashMap<Integer, String> netPrefer = new HashMap<>();
        if (this.mIWaveMappingCallback == null || this.param == null) {
            LogUtil.w("mIWaveMappingCallback is none");
            return netPrefer;
        }
        LogUtil.d("queryNetPreference");
        if (!this.smInitFinish || !this.param.getUserPreferEnabled()) {
            return netPrefer;
        }
        return this.mHistoryQoE.queryHistoryPreference(network, this.mIWaveMappingCallback);
    }

    public IWaveMappingCallback getWaveMappingCallback() {
        return this.mIWaveMappingCallback;
    }

    public IWaveMappingCallback getAppQoeCallback() {
        return this.mAppQoeCallback;
    }

    public IWaveMappingCallback getHiStreamCallback() {
        return this.mHiStreamCallback;
    }
}
