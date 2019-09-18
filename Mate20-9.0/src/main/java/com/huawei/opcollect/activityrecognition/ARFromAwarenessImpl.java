package com.huawei.opcollect.activityrecognition;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.hiai.awareness.service.AwarenessManager;
import com.huawei.hiai.awareness.service.AwarenessServiceConnection;
import com.huawei.hiai.awareness.service.ExtendAwarenessFence;
import com.huawei.hiai.awareness.service.IRequestCallBack;
import com.huawei.hiai.awareness.service.RequestResult;
import com.huawei.opcollect.collector.servicecollection.ARStatusAction;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.utils.OPCollectLog;
import java.lang.ref.WeakReference;

public class ARFromAwarenessImpl extends ARStatusAction.ARProvider {
    private static final String ACTION = "com.huawei.opcollect.action.ARChange";
    private static final long CONNECT_INTERVAL = 20000;
    private static final int CONNECT_MESSAGE = 1;
    private static final int REGISTER_MESSAGE = 2;
    private static final int STORE_MESSAGE = 3;
    private static final String TAG = "ARFromAwarenessImpl";
    private ARChangeReceiver mARChangeReceiver;
    /* access modifiers changed from: private */
    public AwarenessManager mAwarenessManager;
    /* access modifiers changed from: private */
    public MyAwarenessServiceConnection mAwarenessServiceConnection;
    private Context mContext;
    private ExtendAwarenessFence mExtendAwarenessFence;
    /* access modifiers changed from: private */
    public Handler mHandler = null;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private PendingIntent mPendingIntent;
    private RequestCallBackImpl mRequestCallBackImpl;

    private static class ARChangeReceiver extends BroadcastReceiver {
        private int mLastEventType = 0;
        private int mLastMotionType = 0;
        private long mLastTimeStamp = -1;
        private final WeakReference<ARFromAwarenessImpl> service;

        ARChangeReceiver(ARFromAwarenessImpl service2) {
            this.service = new WeakReference<>(service2);
        }

        public void onReceive(Context context, Intent i) {
            if (i != null && ARFromAwarenessImpl.ACTION.equals(i.getAction())) {
                int motionType = ARStatusAction.activityName2Type(i.getStringExtra(AwarenessConstants.DATA_ACTION_STRING_TYPE));
                int eventType = i.getIntExtra(AwarenessConstants.DATA_EVENT_TYPE, 1);
                long timeStamp = i.getLongExtra(AwarenessConstants.DATA_SENSOR_TIME_STAMP, -1);
                if (this.mLastMotionType != motionType || this.mLastEventType != eventType || timeStamp != this.mLastTimeStamp) {
                    this.mLastMotionType = motionType;
                    this.mLastEventType = eventType;
                    this.mLastTimeStamp = timeStamp;
                    OPCollectLog.i(ARFromAwarenessImpl.TAG, "motionType: " + motionType + " eventType: " + eventType + " timeStamp: " + timeStamp);
                    ARFromAwarenessImpl impl = (ARFromAwarenessImpl) this.service.get();
                    if (impl != null) {
                        synchronized (impl.mLock) {
                            if (impl.mHandler != null) {
                                Message msg = new Message();
                                msg.what = 3;
                                msg.arg1 = motionType;
                                msg.arg2 = eventType;
                                impl.mHandler.sendMessage(msg);
                            }
                        }
                    }
                }
            }
        }
    }

    private static class ARHandler extends Handler {
        private final WeakReference<ARFromAwarenessImpl> service;

        ARHandler(ARFromAwarenessImpl impl) {
            this.service = new WeakReference<>(impl);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ARFromAwarenessImpl impl = (ARFromAwarenessImpl) this.service.get();
            if (impl != null && msg != null) {
                OPCollectLog.i(ARFromAwarenessImpl.TAG, "handleMessage msg: " + msg.what);
                switch (msg.what) {
                    case 1:
                        synchronized (impl.mLock) {
                            if (impl.mAwarenessManager != null && !impl.mAwarenessManager.connectMsdpService(impl.mAwarenessServiceConnection)) {
                                sendEmptyMessageDelayed(1, ARFromAwarenessImpl.CONNECT_INTERVAL);
                            }
                        }
                        return;
                    case 2:
                        impl.registerMovementFence();
                        return;
                    case 3:
                        impl.storeARStatus(msg.arg1, msg.arg2, -1);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    private static class MyAwarenessServiceConnection implements AwarenessServiceConnection {
        private final WeakReference<ARFromAwarenessImpl> service;

        MyAwarenessServiceConnection(ARFromAwarenessImpl service2) {
            this.service = new WeakReference<>(service2);
        }

        public void onServiceConnected() {
            OPCollectLog.i(ARFromAwarenessImpl.TAG, "onServiceConnected.");
            ARFromAwarenessImpl impl = (ARFromAwarenessImpl) this.service.get();
            if (impl != null) {
                synchronized (impl.mLock) {
                    if (impl.mHandler != null) {
                        impl.mHandler.removeMessages(1);
                        impl.mHandler.sendEmptyMessage(2);
                    }
                }
            }
        }

        public void onServiceDisconnected() {
            OPCollectLog.e(ARFromAwarenessImpl.TAG, "onServiceDisconnected.");
        }
    }

    private static class RequestCallBackImpl extends IRequestCallBack.Stub {
        private RequestCallBackImpl() {
        }

        public void onRequestResult(RequestResult requestResult) {
            if (requestResult != null) {
                OPCollectLog.i(ARFromAwarenessImpl.TAG, " " + requestResult.getErrorResult());
            }
        }
    }

    public ARFromAwarenessImpl(Context context, ARStatusAction arStatusAction) {
        super(arStatusAction);
        this.mContext = context;
    }

    public void enable() {
        OPCollectLog.i(TAG, "enable");
        synchronized (this.mLock) {
            if (this.mAwarenessServiceConnection == null) {
                this.mAwarenessServiceConnection = new MyAwarenessServiceConnection(this);
            }
            if (this.mAwarenessManager == null) {
                this.mAwarenessManager = new AwarenessManager(this.mContext);
            }
            if (this.mRequestCallBackImpl == null) {
                this.mRequestCallBackImpl = new RequestCallBackImpl();
            }
            if (this.mExtendAwarenessFence == null) {
                this.mExtendAwarenessFence = new ExtendAwarenessFence(1, 4, 127, null);
            }
            if (this.mARChangeReceiver == null) {
                this.mARChangeReceiver = new ARChangeReceiver(this);
                this.mContext.registerReceiver(this.mARChangeReceiver, new IntentFilter(ACTION), "com.huawei.permission.OP_COLLECT", OdmfCollectScheduler.getInstance().getCtrlHandler());
            }
            if (this.mPendingIntent == null) {
                Intent intent = new Intent(ACTION);
                intent.setPackage(this.mContext.getPackageName());
                this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
            }
            if (this.mHandler == null) {
                this.mHandler = new ARHandler(this);
            }
            this.mHandler.removeMessages(1);
            if (!this.mAwarenessManager.connectMsdpService(this.mAwarenessServiceConnection)) {
                this.mHandler.sendEmptyMessageDelayed(1, CONNECT_INTERVAL);
            }
        }
    }

    public boolean enableAREvent(int type) {
        boolean z = true;
        OPCollectLog.i(TAG, "enableAREvent type: " + type);
        synchronized (this.mLock) {
            if (this.mExtendAwarenessFence == null || this.mAwarenessManager == null) {
                OPCollectLog.e(TAG, "mExtendAwarenessFence or mAwarenessManager is null");
                z = false;
            } else {
                Bundle bundle = new Bundle();
                if (type == 1) {
                    bundle.putLong(AwarenessConstants.SENSORHUB_CONTROL_REPORT_PERIOD, ARStatusAction.REPORT_LATENCY_NS);
                } else {
                    bundle.putLong(AwarenessConstants.SENSORHUB_CONTROL_REPORT_PERIOD, 200000000000L);
                }
                bundle.putString(AwarenessConstants.HwMSDPOtherParams, null);
                this.mExtendAwarenessFence.setRegisterBundle(bundle);
                RequestResult requestResult = this.mAwarenessManager.setReportPeriod(this.mExtendAwarenessFence);
                if (requestResult != null) {
                    OPCollectLog.i(TAG, "setReportPeriod code: " + requestResult.getErrorCode() + " result: " + requestResult.getErrorResult());
                } else {
                    OPCollectLog.e(TAG, "setReportPeriod return null");
                }
            }
        }
        return z;
    }

    public void disable() {
        OPCollectLog.i(TAG, "disable");
        synchronized (this.mLock) {
            if (this.mARChangeReceiver != null) {
                this.mContext.unregisterReceiver(this.mARChangeReceiver);
                this.mARChangeReceiver = null;
            }
            if (this.mHandler != null) {
                this.mHandler.removeMessages(1);
                this.mHandler = null;
            }
            if (this.mAwarenessManager != null) {
                if (!(this.mExtendAwarenessFence == null || this.mExtendAwarenessFence.getTopKey() == null)) {
                    this.mAwarenessManager.unRegisterFence(this.mRequestCallBackImpl, this.mExtendAwarenessFence, this.mPendingIntent);
                }
                this.mAwarenessManager.disconnectService();
                this.mExtendAwarenessFence = null;
                this.mRequestCallBackImpl = null;
                this.mAwarenessServiceConnection = null;
                this.mPendingIntent = null;
                this.mAwarenessManager = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        return;
     */
    public void registerMovementFence() {
        synchronized (this.mLock) {
            if (this.mRequestCallBackImpl == null) {
                OPCollectLog.e(TAG, "registerMovementFence mRequestCallBackImpl is null");
            } else if (this.mExtendAwarenessFence == null) {
                OPCollectLog.e(TAG, "registerMovementFence mExtendAwarenessFence is null");
            } else if (this.mPendingIntent == null) {
                OPCollectLog.e(TAG, "registerMovementFence mPendingIntent is null");
            } else if (this.mAwarenessManager == null) {
                OPCollectLog.e(TAG, "registerMovementFence mAwarenessManager is null");
            } else {
                boolean screenStatus = false;
                PowerManager pm = null;
                if (this.mContext != null) {
                    pm = (PowerManager) this.mContext.getSystemService("power");
                }
                if (pm != null && Build.VERSION.SDK_INT >= 20) {
                    screenStatus = pm.isInteractive();
                }
                Bundle bundle = new Bundle();
                if (screenStatus) {
                    bundle.putLong(AwarenessConstants.SENSORHUB_CONTROL_REPORT_PERIOD, ARStatusAction.REPORT_LATENCY_NS);
                } else {
                    bundle.putLong(AwarenessConstants.SENSORHUB_CONTROL_REPORT_PERIOD, 200000000000L);
                }
                bundle.putString(AwarenessConstants.HwMSDPOtherParams, null);
                this.mExtendAwarenessFence.setRegisterBundle(bundle);
                if (this.mAwarenessManager.registerMovementFence(this.mRequestCallBackImpl, this.mExtendAwarenessFence, this.mPendingIntent)) {
                    OPCollectLog.i(TAG, "registerMovementFence success");
                } else {
                    OPCollectLog.e(TAG, "registerMovementFence failed");
                }
            }
        }
    }
}
