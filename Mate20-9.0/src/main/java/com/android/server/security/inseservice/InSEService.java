package com.android.server.security.inseservice;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.security.core.IHwSecurityPlugin;
import huawei.android.security.inse.IInSEService;
import vendor.huawei.hardware.huawei_security_vnode.V1_0.IHwSecurityVNode;

public class InSEService extends IInSEService.Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = new Object();
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            if (InSEService.HW_DEBUG) {
                Log.d(InSEService.TAG, "create InSEService");
            }
            return new InSEService(context);
        }

        public String getPluginPermission() {
            return InSEService.INSE_MANAGER_PERMISSION;
        }
    };
    /* access modifiers changed from: private */
    public static final boolean HW_DEBUG = (SystemProperties.get("ro.secure", "1").equals("0") || Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String INSE_HIDL_SERVICE_NAME = "HwInSExtNode";
    private static final String INSE_MANAGER_PERMISSION = "huawei.android.permission.INSE_PERMISSION";
    private static final int MAX_INSE_HIDL_DEAMON_REGISTER_TIMES = 10;
    private static final int MSG_INSE_HIDL_DEAMON_SERVIE_REGISTER = 1;
    private static final int RET_DEFAULT_ERROR_VALUE = -1001;
    private static final int RET_EXCEPTION_WHEN_INSE_CALL = -1002;
    private static final int RET_INSE_HIDL_DEAMON_IS_NOT_READY = -1000;
    private static final String TAG = "InSEServicePlugin";
    private static final int TRY_GET_HIDL_DEAMON_DEALY_MILLIS = 1000;
    /* access modifiers changed from: private */
    public static HwInSEHidlHandler mHwInSEHidlHandler;
    private static HandlerThread mHwInSEThread;
    private Context mContext = null;
    /* access modifiers changed from: private */
    public IHwBinder.DeathRecipient mInSEHidlDeamonDeathRecipient = new IHwBinder.DeathRecipient() {
        public void serviceDied(long cookie) {
            if (InSEService.mHwInSEHidlHandler != null) {
                Log.e(InSEService.TAG, "inse hidl deamon service has died, try to reconnect it later.");
                IHwSecurityVNode unused = InSEService.this.vNode = null;
                InSEService.mHwInSEHidlHandler.sendEmptyMessageDelayed(1, 1000);
            }
        }
    };
    /* access modifiers changed from: private */
    public int mInSEHidlDeamonRegisterTryTimes = 0;
    /* access modifiers changed from: private */
    public IHwSecurityVNode vNode;

    private final class HwInSEHidlHandler extends Handler {
        public HwInSEHidlHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                Log.e(InSEService.TAG, "handler thread received unknown message : " + msg.what);
                return;
            }
            try {
                IHwSecurityVNode unused = InSEService.this.vNode = IHwSecurityVNode.getService(InSEService.INSE_HIDL_SERVICE_NAME, true);
            } catch (Exception e) {
                Log.e(InSEService.TAG, "Try get inse hidl deamon servcie failed in handler message.");
            }
            if (InSEService.this.vNode != null) {
                int unused2 = InSEService.this.mInSEHidlDeamonRegisterTryTimes = 0;
                try {
                    InSEService.this.vNode.linkToDeath(InSEService.this.mInSEHidlDeamonDeathRecipient, 0);
                } catch (Exception e2) {
                    Log.e(InSEService.TAG, "Exception occured when linkToDeath in handle message");
                }
            } else {
                int unused3 = InSEService.this.mInSEHidlDeamonRegisterTryTimes = InSEService.this.mInSEHidlDeamonRegisterTryTimes + 1;
                if (InSEService.this.mInSEHidlDeamonRegisterTryTimes < 10) {
                    Log.e(InSEService.TAG, "inse hidl daemon service is not ready, try times : " + InSEService.this.mInSEHidlDeamonRegisterTryTimes);
                    InSEService.mHwInSEHidlHandler.sendEmptyMessageDelayed(1, 1000);
                } else {
                    Log.e(InSEService.TAG, "inse hidl daemon service connection failed.");
                }
            }
            if (InSEService.HW_DEBUG) {
                Log.d(InSEService.TAG, "handler thread received request inse hidl deamon message.");
            }
        }
    }

    public InSEService(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.os.IBinder, com.android.server.security.inseservice.InSEService] */
    public IBinder asBinder() {
        checkPermission(INSE_MANAGER_PERMISSION);
        return this;
    }

    public void onStart() {
        if (HW_DEBUG) {
            Log.d(TAG, "InSEService start");
        }
        mHwInSEThread = new HandlerThread(TAG);
        mHwInSEThread.start();
        mHwInSEHidlHandler = new HwInSEHidlHandler(mHwInSEThread.getLooper());
    }

    public void onStop() {
        if (mHwInSEHidlHandler != null) {
            mHwInSEHidlHandler = null;
        }
        if (mHwInSEThread != null) {
            mHwInSEThread.quitSafely();
            mHwInSEThread = null;
        }
        try {
            if (HW_DEBUG) {
                Log.d(TAG, "close InSEService");
            }
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "stop error" + e.toString());
        }
    }

    private void checkPermission(String permission) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    public int inSE_PowerOnDelayed(int time, int id) {
        checkPermission(INSE_MANAGER_PERMISSION);
        if (HW_DEBUG) {
            Log.d(TAG, "current inse feature is a stub function now");
        }
        return id < 0 ? -1 : 1;
    }
}
