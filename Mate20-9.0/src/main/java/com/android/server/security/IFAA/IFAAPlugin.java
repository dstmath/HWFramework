package com.android.server.security.IFAA;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import com.android.server.HwNetworkManagementService;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import huawei.android.security.IIFAAPlugin;
import huawei.android.security.IIFAAPluginCallBack;

public class IFAAPlugin extends IIFAAPlugin.Stub implements IHwSecurityPlugin {
    private static final String ACTION_TRUSTSPACE_SECURE_HINT = "com.huawei.trustspace.action.TRUSTSPACE_SECURE_HINT";
    public static final Object BINDLOCK = new Object();
    private static final String BROADCAST_PERMISSION = "android.permission.STATUS_BAR";
    private static final int CERTIFICATE_SIGN_CMD_ID = 2048;
    private static final int CERTIFICATE_SIGN_PROMPTION_VALUE = 10;
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            if (IFAAPlugin.HW_DEBUG) {
                Slog.d(IFAAPlugin.LOG_TAG, "createPlugin");
            }
            return new IFAAPlugin(context);
        }

        public String getPluginPermission() {
            return IFAAPlugin.USE_IFAA_MANAGER;
        }
    };
    private static final int FAILED = 1;
    /* access modifiers changed from: private */
    public static final boolean HW_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(LOG_TAG, 4)));
    private static final int IFAA_CMD_AUTHENTICATE = 4;
    private static final int IFAA_CMD_REGISTER = 2;
    private static final int IFAA_FAIL = -1;
    private static final int IFAA_RET_OFFSET = 0;
    private static final int IFAA_SUCCESS = 0;
    private static final String LOG_TAG = "IFAAPlugin";
    private static final int MAX_INT = 2130706432;
    private static final int PROCESS_CMD_TYPE = 1;
    private static final int SIZE_OF_INT = 4;
    private static final int SUCCESS = 0;
    private static final String SYSTEMUI_PACKAGE_NAME = "com.android.systemui";
    private static final String USE_IFAA_MANAGER = "cn.org.ifaa.permission.USE_IFAA_MANAGER";
    private final Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private static class MessageData {
        IBinder mCallBack;
        byte[] mData;

        MessageData(byte[] data, IBinder callBack) {
            this.mData = data;
            this.mCallBack = callBack;
        }
    }

    public native byte[] nativeProcessCmd(Context context, byte[] bArr);

    public native int nativeStart();

    public native void nativeStop();

    static {
        try {
            System.loadLibrary("ifaajni_ca");
        } catch (UnsatisfiedLinkError e) {
            Slog.e(LOG_TAG, "LoadLibrary is error ");
        }
    }

    private static int decodeByteToInt(byte[] ary, int offset) {
        if (ary == null || ary.length + -4 < offset) {
            return -1;
        }
        return (ary[offset] & 255) | ((ary[offset + 1] << 8) & 65280) | ((ary[offset + 2] << 16) & MemoryConstant.LARGE_CPU_MASK) | ((ary[offset + 3] << 24) & -16777216);
    }

    private static boolean isValidLen(int dataLen, int pos, int totalLen) {
        if (!(dataLen < 0 || MAX_INT < pos + dataLen || totalLen < pos + dataLen)) {
            return true;
        }
        Slog.e(LOG_TAG, "data length is out bound");
        return false;
    }

    private int getCmdId(byte[] data) {
        if (data == null) {
            Slog.e(LOG_TAG, "getCmdId bad param");
            return -1;
        }
        int dataLen = data.length;
        if (dataLen < 4) {
            return -1;
        }
        int position = 0 + 4;
        int signLen = decodeByteToInt(data, position);
        int position2 = position + 4;
        if (!isValidLen(signLen, position2, dataLen)) {
            return -1;
        }
        int position3 = position2 + signLen;
        int pkgLen = decodeByteToInt(data, position3);
        int position4 = position3 + 4;
        if (!isValidLen(pkgLen, position4, dataLen)) {
            return -1;
        }
        return decodeByteToInt(data, position4 + pkgLen);
    }

    public void processCmd(IIFAAPluginCallBack callBack, byte[] data) {
        if (HW_DEBUG) {
            Slog.d(LOG_TAG, "processCmd in IFAAPlugin");
        }
        checkPermission(USE_IFAA_MANAGER);
        if (callBack == null || data == null) {
            Slog.e(LOG_TAG, "parameters is error");
            return;
        }
        MessageData messageData = new MessageData(data, callBack.asBinder());
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = messageData;
        this.mHandler.sendMessage(msg);
    }

    public IFAAPlugin(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.security.IFAA.IFAAPlugin, android.os.IBinder] */
    public IBinder asBinder() {
        return this;
    }

    public void onStart() {
        if (HW_DEBUG) {
            Slog.d(LOG_TAG, "ifaa plugin onStart");
        }
        try {
            nativeStart();
        } catch (UnsatisfiedLinkError e) {
            Slog.e(LOG_TAG, "start error");
        }
        this.mHandlerThread = new HandlerThread("IFAAPluginThread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    IFAAPlugin.this.handleProcessCmd(msg);
                }
            }
        };
    }

    private void showHardwareSupportPrompt(byte[] reqData, byte[] resData) {
        if (reqData == null || resData == null) {
            Slog.e(LOG_TAG, "showHardwareSupportPrompt bad param");
        }
        int cmdID = getCmdId(reqData);
        int retCode = decodeByteToInt(resData, 0);
        if (2 == cmdID && retCode == 0) {
            Flog.bdReport(this.mContext, HwNetworkManagementService.NetdResponseCode.ApLinkedStaListChangeQCOM);
        }
        if (4 == cmdID && retCode == 0) {
            Flog.bdReport(this.mContext, 902);
        }
        if (2048 == cmdID && retCode == 0) {
            long identity = Binder.clearCallingIdentity();
            try {
                if (HW_DEBUG) {
                    Slog.d(LOG_TAG, "sending msg to call secure hint");
                }
                Intent intent = new Intent(ACTION_TRUSTSPACE_SECURE_HINT);
                intent.setPackage("com.android.systemui");
                intent.putExtra(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, 10);
                this.mContext.sendBroadcast(intent, BROADCAST_PERMISSION);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleProcessCmd(Message msg) {
        byte[] retData;
        if (HW_DEBUG) {
            Slog.d(LOG_TAG, "handleProcessCmd in IFAAPlugin ");
        }
        MessageData messageData = (MessageData) msg.obj;
        IBinder callBack = messageData.mCallBack;
        byte[] data = messageData.mData;
        if (HW_DEBUG) {
            Slog.d(LOG_TAG, "call JNI  processCmd ");
        }
        try {
            retData = nativeProcessCmd(this.mContext, data);
        } catch (UnsatisfiedLinkError e) {
            Slog.e(LOG_TAG, "native process cmd error");
            retData = null;
        }
        showHardwareSupportPrompt(data, retData);
        IIFAAPluginCallBack mCallBackService = IIFAAPluginCallBack.Stub.asInterface(callBack);
        if (mCallBackService == null) {
            Slog.e(LOG_TAG, "callback is invalid!!!");
        } else if (retData != null) {
            try {
                if (HW_DEBUG) {
                    Slog.d(LOG_TAG, "call processCmdResult in IIFAAPluginCallBack ");
                }
                mCallBackService.processCmdResult(0, retData);
            } catch (RemoteException e2) {
                Slog.e(LOG_TAG, "Error in send processCmdResult to callback.");
            }
        } else {
            if (HW_DEBUG) {
                Slog.d(LOG_TAG, "retData is null ");
            }
            mCallBackService.processCmdResult(1, new byte[1]);
        }
    }

    public void onStop() {
        if (this.mHandler != null) {
            this.mHandler = null;
        }
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quitSafely();
            this.mHandlerThread = null;
        }
        try {
            Slog.d(LOG_TAG, "close session");
            nativeStop();
        } catch (UnsatisfiedLinkError e) {
            Slog.e(LOG_TAG, "stop error");
        }
    }

    private void checkPermission(String permission) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }
}
