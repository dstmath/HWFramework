package com.android.server.security.IFAA;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import huawei.android.security.IIFAAPlugin.Stub;
import huawei.android.security.IIFAAPluginCallBack;

public class IFAAPlugin extends Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = null;
    public static final Creator CREATOR = null;
    private static final int FAILED = 1;
    private static final boolean HW_DEBUG = false;
    private static final int PROCESS_CMD_TYPE = 1;
    private static final int SUCCESS = 0;
    private static final String TAG = "IFAAPlugin";
    private static final String USE_IFAA_MANAGER = "cn.org.ifaa.permission.USE_IFAA_MANAGER";
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    /* renamed from: com.android.server.security.IFAA.IFAAPlugin.2 */
    class AnonymousClass2 extends Handler {
        AnonymousClass2(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IFAAPlugin.PROCESS_CMD_TYPE /*1*/:
                    IFAAPlugin.this.handleProcessCmd(msg);
                default:
            }
        }
    }

    private static class MessageData {
        IBinder mCallBack;
        byte[] mData;

        MessageData(byte[] data, IBinder callBack) {
            this.mData = data;
            this.mCallBack = callBack;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.IFAA.IFAAPlugin.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.IFAA.IFAAPlugin.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.IFAA.IFAAPlugin.<clinit>():void");
    }

    public native byte[] nativeProcessCmd(Context context, byte[] bArr);

    public native int nativeStart();

    public native void nativeStop();

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
        msg.what = PROCESS_CMD_TYPE;
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
        this.mHandler = new AnonymousClass2(this.mHandlerThread.getLooper());
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
        IIFAAPluginCallBack mCallBackService = IIFAAPluginCallBack.Stub.asInterface(callBack);
        if (mCallBackService == null) {
            Slog.e(TAG, "callback is invalid!!!");
        } else if (retData != null) {
            try {
                if (HW_DEBUG) {
                    Slog.d(TAG, "call processCmdResult in IIFAAPluginCallBack ");
                }
                mCallBackService.processCmdResult(SUCCESS, retData);
            } catch (RemoteException e2) {
                Slog.e(TAG, "Error in send processCmdResult to callback.");
            }
        } else {
            if (HW_DEBUG) {
                Slog.d(TAG, "retData is null ");
            }
            mCallBackService.processCmdResult(PROCESS_CMD_TYPE, new byte[SUCCESS]);
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
