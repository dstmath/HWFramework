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
import com.android.server.intellicom.common.SmartDualCardConsts;
import java.util.HashMap;

public class HwWaveMappingManager {
    private static final int DEFAULT_CAPACITY = 16;
    private static final String TAG = ("WMapping." + HwWaveMappingManager.class.getSimpleName());
    private static HwWaveMappingManager instance = null;
    private BootBroadcastReceiver bootBroadcastReceiver;
    private HandlerThread handlerThread;
    private boolean isInitFinished = false;
    private IWaveMappingCallback mAppQoeCallback = null;
    private Context mContext;
    private Handler mHandler;
    private IWaveMappingCallback mHiStreamCallback = null;
    private HwArbitrationHistoryQoeManager mHistoryQoE;
    private IWaveMappingCallback mIWaveMappingCallback;
    private ParameterInfo param = null;
    private HwWMStateMachine stateMachine;

    private HwWaveMappingManager(Context mContext2) {
        this.mContext = mContext2;
        ContextManager.getInstance().setContext(mContext2);
        this.stateMachine = HwWMStateMachine.getInstance(mContext2);
        initControllerHandler();
        this.mHistoryQoE = HwArbitrationHistoryQoeManager.getInstance(this.stateMachine.getHandler());
        this.bootBroadcastReceiver = new BootBroadcastReceiver(this.mHandler);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_ACTION_SHUTDOWN);
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");
        this.mContext.registerReceiver(this.bootBroadcastReceiver, filter);
        LogUtil.d(false, "HwWaveMappingManager init completed", new Object[0]);
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
            /* class com.android.server.hidata.wavemapping.HwWaveMappingManager.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    LogUtil.d(false, "Constant.MSG_REV_SYSBOOT begin.", new Object[0]);
                    if (!Constant.checkPath(HwWaveMappingManager.this.mContext)) {
                        LogUtil.e(false, "init failure.", new Object[0]);
                        return;
                    }
                    if (!HwWaveMappingManager.this.isInitFinished) {
                        HwWaveMappingManager.this.stateMachine.init();
                        HwWaveMappingManager.this.param = ParamManager.getInstance().getParameterInfo();
                    }
                    HwWaveMappingManager.this.isInitFinished = true;
                } else if (i == 2) {
                    LogUtil.d(false, "Constant.MSG_REV_SYSSHUTDOWN begin.", new Object[0]);
                    HwWaveMappingManager.this.stateMachine.handleShutDown();
                }
            }
        };
    }

    public void registerWaveMappingCallback(IWaveMappingCallback callback, int module) {
        if (callback != null) {
            LogUtil.d(false, "registerWaveMappingCallback, module = %{public}d", Integer.valueOf(module));
            if (module == 0) {
                this.mAppQoeCallback = callback;
            } else if (module == 1) {
                this.mHiStreamCallback = callback;
            }
        }
    }

    public void registerWaveMappingCallback(IWaveMappingCallback callback) {
        if (callback != null) {
            LogUtil.d(false, "registerWaveMappingCallback", new Object[0]);
            this.mIWaveMappingCallback = callback;
        }
    }

    public void queryWaveMappingInfo(int uid, int appId, int sense, int network) {
        if (this.mIWaveMappingCallback != null) {
            LogUtil.d(false, "queryWaveMappingInfo", new Object[0]);
            if (this.isInitFinished) {
                this.mHistoryQoE.queryHistoryQoE(uid, appId, sense, network, this.mIWaveMappingCallback, 1);
            } else {
                this.mIWaveMappingCallback.onWaveMappingRespondCallback(uid, 0, network, true, false);
            }
        } else {
            LogUtil.w(false, "mIWaveMappingCallback is none", new Object[0]);
        }
    }

    public void queryWaveMappingInfo4Back(int uid, int appId, int sense, int network) {
        if (this.mIWaveMappingCallback != null) {
            LogUtil.d(false, "queryWaveMappingInfo for switch back", new Object[0]);
            if (this.isInitFinished) {
                this.mHistoryQoE.queryHistoryQoE(uid, appId, sense, network, this.mIWaveMappingCallback, 2);
            } else {
                this.mIWaveMappingCallback.onWaveMappingRespond4BackCallback(uid, 0, network, true, false);
            }
        } else {
            LogUtil.w(false, "mIWaveMappingCallback is none", new Object[0]);
        }
    }

    public HashMap<Integer, String> queryNetPreference(int network) {
        HashMap<Integer, String> netPrefer = new HashMap<>(16);
        if (this.mIWaveMappingCallback == null || this.param == null) {
            LogUtil.w(false, "mIWaveMappingCallback is none", new Object[0]);
            return netPrefer;
        }
        LogUtil.d(false, "queryNetPreference", new Object[0]);
        if (!this.isInitFinished || !this.param.getUserPreferEnabled()) {
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
