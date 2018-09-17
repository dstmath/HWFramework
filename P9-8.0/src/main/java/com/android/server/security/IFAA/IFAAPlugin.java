package com.android.server.security.IFAA;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import huawei.android.security.IIFAAPlugin.Stub;
import huawei.android.security.IIFAAPluginCallBack;

public class IFAAPlugin extends Stub implements IHwSecurityPlugin {
    private static final String ACTION_TRUSTSPACE_APP_SECURE_HINT = "com.huawei.trustspace.action.TRUSTSPACE_SECURE_HINT";
    public static final Object BINDLOCK = new Object();
    private static final int CERTIFICATE_SIGN_CMD_ID = 2048;
    private static final int CERTIFICATE_SIGN_PROMPTION_VALUE = 10;
    public static final Creator CREATOR = new Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            if (IFAAPlugin.HW_DEBUG) {
                Slog.d(IFAAPlugin.TAG, "createPlugin");
            }
            return new IFAAPlugin(context);
        }

        public String getPluginPermission() {
            return IFAAPlugin.USE_IFAA_MANAGER;
        }
    };
    private static final int FAILED = 1;
    private static final boolean HW_DEBUG;
    private static final int IFAA_FAIL = -1;
    private static final int IFAA_RET_OFFSET = 0;
    private static final int IFAA_SUCCESS = 0;
    private static final int MAX_INT = 2130706432;
    private static final int PROCESS_CMD_TYPE = 1;
    private static final int SIZE_OF_INT = 4;
    private static final int SUCCESS = 0;
    private static final String SYSTEMUI_PACKAGE_NAME = "com.android.systemui";
    private static final String TAG = "IFAAPlugin";
    private static final String USE_IFAA_MANAGER = "cn.org.ifaa.permission.USE_IFAA_MANAGER";
    private Context mContext;
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
        boolean z;
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 4);
        } else {
            z = false;
        }
        HW_DEBUG = z;
        try {
            System.loadLibrary("ifaajni_ca");
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "LoadLibrary is error " + e.toString());
        }
    }

    private int decodeByteToInt(byte[] ary, int offset) {
        if (ary == null || ary.length - 4 < offset) {
            return -1;
        }
        return (((ary[offset] & 255) | ((ary[offset + 1] << 8) & 65280)) | ((ary[offset + 2] << 16) & MemoryConstant.LARGE_CPU_MASK)) | ((ary[offset + 3] << 24) & -16777216);
    }

    private int getCmdId(byte[] data) {
        if (data == null) {
            Slog.e(TAG, "getCmdId bad param");
            return -1;
        }
        int dataLen = data.length;
        if (dataLen < 4) {
            return -1;
        }
        int signLen = decodeByteToInt(data, 4);
        int position = 4 + 4;
        if (signLen < 0 || 2130706424 < signLen || dataLen < signLen + 8) {
            return -1;
        }
        position = signLen + 8;
        int pkgLen = decodeByteToInt(data, position);
        position += 4;
        if (pkgLen < 0 || MAX_INT - position < pkgLen || dataLen < position + pkgLen) {
            return -1;
        }
        return decodeByteToInt(data, position + pkgLen);
    }

    public void processCmd(IIFAAPluginCallBack callBack, byte[] data) {
        if (HW_DEBUG) {
            Slog.d(TAG, "processCmd in IFAAPlugin");
        }
        checkPermission(USE_IFAA_MANAGER);
        if (callBack == null || data == null) {
            Slog.e(TAG, "parameters is error");
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

    public IBinder asBinder() {
        return this;
    }

    public void onStart() {
        if (HW_DEBUG) {
            Slog.d(TAG, "is start");
        }
        try {
            if (HW_DEBUG) {
                Slog.d(TAG, "close session");
            }
            nativeStart();
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "start error" + e.toString());
        }
        this.mHandlerThread = new HandlerThread("IFAAPluginThread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        IFAAPlugin.this.handleProcessCmd(msg);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void showHardwareSupportPrompt(byte[] reqData, byte[] resData) {
        if (reqData == null || resData == null) {
            Slog.e(TAG, "showHardwareSupportPrompt bad param");
        }
        int cmdID = getCmdId(reqData);
        int retCode = decodeByteToInt(resData, 0);
        if (2048 == cmdID && retCode == 0) {
            long identity = Binder.clearCallingIdentity();
            try {
                if (HW_DEBUG) {
                    Slog.d(TAG, "sending msg to call secure hint");
                }
                Intent intent = new Intent(ACTION_TRUSTSPACE_APP_SECURE_HINT);
                intent.setPackage(SYSTEMUI_PACKAGE_NAME);
                intent.putExtra(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, 10);
                this.mContext.sendBroadcast(intent);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private void handleProcessCmd(Message msg) {
        byte[] retData;
        if (HW_DEBUG) {
            Slog.d(TAG, "handleProcessCmd in IFAAPlugin ");
        }
        MessageData messageData = msg.obj;
        IBinder callBack = messageData.mCallBack;
        byte[] data = messageData.mData;
        if (HW_DEBUG) {
            Slog.d(TAG, "call JNI  processCmd ");
        }
        try {
            retData = nativeProcessCmd(this.mContext, data);
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, e.toString());
            retData = null;
        }
        showHardwareSupportPrompt(data, retData);
        IIFAAPluginCallBack mCallBackService = IIFAAPluginCallBack.Stub.asInterface(callBack);
        if (mCallBackService == null) {
            Slog.e(TAG, "callback is invalid!!!");
        } else if (retData != null) {
            try {
                if (HW_DEBUG) {
                    Slog.d(TAG, "call processCmdResult in IIFAAPluginCallBack ");
                }
                mCallBackService.processCmdResult(0, retData);
            } catch (RemoteException e2) {
                Slog.e(TAG, "Error in send processCmdResult to callback.");
            }
        } else {
            if (HW_DEBUG) {
                Slog.d(TAG, "retData is null ");
            }
            mCallBackService.processCmdResult(1, new byte[0]);
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
            Slog.d(TAG, "close session");
            nativeStop();
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "stop error" + e.toString());
        }
    }

    private void checkPermission(String permission) {
        this.mContext.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }
}
