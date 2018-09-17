package com.android.internal.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Slog;
import java.util.Stack;

public class AsyncChannel {
    private static final int BASE = 69632;
    public static final int CMD_CHANNEL_DISCONNECT = 69635;
    public static final int CMD_CHANNEL_DISCONNECTED = 69636;
    public static final int CMD_CHANNEL_FULLY_CONNECTED = 69634;
    public static final int CMD_CHANNEL_FULL_CONNECTION = 69633;
    public static final int CMD_CHANNEL_HALF_CONNECTED = 69632;
    private static final int CMD_TO_STRING_COUNT = 5;
    private static final boolean DBG = false;
    public static final int STATUS_BINDING_UNSUCCESSFUL = 1;
    public static final int STATUS_FULL_CONNECTION_REFUSED_ALREADY_CONNECTED = 3;
    public static final int STATUS_REMOTE_DISCONNECTION = 4;
    public static final int STATUS_SEND_UNSUCCESSFUL = 2;
    public static final int STATUS_SUCCESSFUL = 0;
    private static final String TAG = "AsyncChannel";
    private static String[] sCmdToString;
    private AsyncChannelConnection mConnection;
    private DeathMonitor mDeathMonitor;
    private Messenger mDstMessenger;
    private Context mSrcContext;
    private Handler mSrcHandler;
    private Messenger mSrcMessenger;

    /* renamed from: com.android.internal.util.AsyncChannel.1ConnectAsync */
    final class AnonymousClass1ConnectAsync implements Runnable {
        String mDstClassName;
        String mDstPackageName;
        Context mSrcCtx;
        Handler mSrcHdlr;

        AnonymousClass1ConnectAsync(Context srcContext, Handler srcHandler, String dstPackageName, String dstClassName) {
            this.mSrcCtx = srcContext;
            this.mSrcHdlr = srcHandler;
            this.mDstPackageName = dstPackageName;
            this.mDstClassName = dstClassName;
        }

        public void run() {
            AsyncChannel.this.replyHalfConnected(AsyncChannel.this.connectSrcHandlerToPackageSync(this.mSrcCtx, this.mSrcHdlr, this.mDstPackageName, this.mDstClassName));
        }
    }

    class AsyncChannelConnection implements ServiceConnection {
        AsyncChannelConnection() {
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            AsyncChannel.this.mDstMessenger = new Messenger(service);
            AsyncChannel.this.replyHalfConnected(AsyncChannel.STATUS_SUCCESSFUL);
        }

        public void onServiceDisconnected(ComponentName className) {
            AsyncChannel.this.replyDisconnected(AsyncChannel.STATUS_SUCCESSFUL);
        }
    }

    private final class DeathMonitor implements DeathRecipient {
        DeathMonitor() {
        }

        public void binderDied() {
            AsyncChannel.this.replyDisconnected(AsyncChannel.STATUS_REMOTE_DISCONNECTION);
        }
    }

    private static class SyncMessenger {
        private static int sCount;
        private static Stack<SyncMessenger> sStack;
        private SyncHandler mHandler;
        private HandlerThread mHandlerThread;
        private Messenger mMessenger;

        private class SyncHandler extends Handler {
            private Object mLockObject;
            private Message mResultMsg;

            private SyncHandler(Looper looper) {
                super(looper);
                this.mLockObject = new Object();
            }

            public void handleMessage(Message msg) {
                this.mResultMsg = Message.obtain();
                this.mResultMsg.copyFrom(msg);
                synchronized (this.mLockObject) {
                    this.mLockObject.notify();
                }
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.util.AsyncChannel.SyncMessenger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.util.AsyncChannel.SyncMessenger.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.AsyncChannel.SyncMessenger.<clinit>():void");
        }

        private SyncMessenger() {
        }

        private static SyncMessenger obtain() {
            SyncMessenger sm;
            synchronized (sStack) {
                if (sStack.isEmpty()) {
                    sm = new SyncMessenger();
                    StringBuilder append = new StringBuilder().append("SyncHandler-");
                    int i = sCount;
                    sCount = i + AsyncChannel.STATUS_BINDING_UNSUCCESSFUL;
                    sm.mHandlerThread = new HandlerThread(append.append(i).toString());
                    sm.mHandlerThread.start();
                    sm.getClass();
                    sm.mHandler = new SyncHandler(sm.mHandlerThread.getLooper(), null);
                    sm.mMessenger = new Messenger(sm.mHandler);
                } else {
                    sm = (SyncMessenger) sStack.pop();
                }
            }
            return sm;
        }

        private void recycle() {
            synchronized (sStack) {
                sStack.push(this);
            }
        }

        private static Message sendMessageSynchronously(Messenger dstMessenger, Message msg) {
            SyncMessenger sm = obtain();
            if (dstMessenger == null || msg == null) {
                sm.mHandler.mResultMsg = null;
            } else {
                try {
                    msg.replyTo = sm.mMessenger;
                    synchronized (sm.mHandler.mLockObject) {
                        dstMessenger.send(msg);
                        sm.mHandler.mLockObject.wait();
                    }
                } catch (InterruptedException e) {
                    sm.mHandler.mResultMsg = null;
                } catch (RemoteException e2) {
                    sm.mHandler.mResultMsg = null;
                }
            }
            Message resultMsg = sm.mHandler.mResultMsg;
            sm.recycle();
            return resultMsg;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.util.AsyncChannel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.util.AsyncChannel.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.AsyncChannel.<clinit>():void");
    }

    protected static String cmdToString(int cmd) {
        cmd -= CMD_CHANNEL_HALF_CONNECTED;
        if (cmd < 0 || cmd >= sCmdToString.length) {
            return null;
        }
        return sCmdToString[cmd];
    }

    public AsyncChannel() {
    }

    public int connectSrcHandlerToPackageSync(Context srcContext, Handler srcHandler, String dstPackageName, String dstClassName) {
        this.mConnection = new AsyncChannelConnection();
        this.mSrcContext = srcContext;
        this.mSrcHandler = srcHandler;
        this.mSrcMessenger = new Messenger(srcHandler);
        this.mDstMessenger = null;
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName(dstPackageName, dstClassName);
        if (srcContext.bindService(intent, this.mConnection, STATUS_BINDING_UNSUCCESSFUL)) {
            return STATUS_SUCCESSFUL;
        }
        return STATUS_BINDING_UNSUCCESSFUL;
    }

    public int connectSync(Context srcContext, Handler srcHandler, Messenger dstMessenger) {
        connected(srcContext, srcHandler, dstMessenger);
        return STATUS_SUCCESSFUL;
    }

    public int connectSync(Context srcContext, Handler srcHandler, Handler dstHandler) {
        return connectSync(srcContext, srcHandler, new Messenger(dstHandler));
    }

    public int fullyConnectSync(Context srcContext, Handler srcHandler, Handler dstHandler) {
        int status = connectSync(srcContext, srcHandler, dstHandler);
        if (status != 0) {
            return status;
        }
        Message response = sendMessageSynchronously((int) CMD_CHANNEL_FULL_CONNECTION);
        if (response != null) {
            return response.arg1;
        }
        return STATUS_SEND_UNSUCCESSFUL;
    }

    public void connect(Context srcContext, Handler srcHandler, String dstPackageName, String dstClassName) {
        new Thread(new AnonymousClass1ConnectAsync(srcContext, srcHandler, dstPackageName, dstClassName)).start();
    }

    public void connect(Context srcContext, Handler srcHandler, Class<?> klass) {
        connect(srcContext, srcHandler, klass.getPackage().getName(), klass.getName());
    }

    public void connect(Context srcContext, Handler srcHandler, Messenger dstMessenger) {
        connected(srcContext, srcHandler, dstMessenger);
        replyHalfConnected(STATUS_SUCCESSFUL);
    }

    public void connected(Context srcContext, Handler srcHandler, Messenger dstMessenger) {
        this.mSrcContext = srcContext;
        this.mSrcHandler = srcHandler;
        this.mSrcMessenger = new Messenger(this.mSrcHandler);
        this.mDstMessenger = dstMessenger;
        linkToDeathMonitor();
    }

    public void connect(Context srcContext, Handler srcHandler, Handler dstHandler) {
        connect(srcContext, srcHandler, new Messenger(dstHandler));
    }

    public void connect(AsyncService srcAsyncService, Messenger dstMessenger) {
        connect((Context) srcAsyncService, srcAsyncService.getHandler(), dstMessenger);
    }

    public void disconnected() {
        this.mSrcContext = null;
        this.mSrcHandler = null;
        this.mSrcMessenger = null;
        this.mDstMessenger = null;
        this.mDeathMonitor = null;
        this.mConnection = null;
    }

    public void disconnect() {
        if (!(this.mConnection == null || this.mSrcContext == null)) {
            this.mSrcContext.unbindService(this.mConnection);
            this.mConnection = null;
        }
        try {
            Message msg = Message.obtain();
            msg.what = CMD_CHANNEL_DISCONNECTED;
            msg.replyTo = this.mSrcMessenger;
            this.mDstMessenger.send(msg);
        } catch (Exception e) {
        }
        replyDisconnected(STATUS_SUCCESSFUL);
        this.mSrcHandler = null;
        if (this.mConnection == null && this.mDstMessenger != null && this.mDeathMonitor != null) {
            this.mDstMessenger.getBinder().unlinkToDeath(this.mDeathMonitor, STATUS_SUCCESSFUL);
            this.mDeathMonitor = null;
        }
    }

    public void sendMessage(Message msg) {
        msg.replyTo = this.mSrcMessenger;
        try {
            this.mDstMessenger.send(msg);
        } catch (RemoteException e) {
            replyDisconnected(STATUS_SEND_UNSUCCESSFUL);
        }
    }

    public void sendMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        sendMessage(msg);
    }

    public void sendMessage(int what, int arg1) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        sendMessage(msg);
    }

    public void sendMessage(int what, int arg1, int arg2) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        sendMessage(msg);
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        sendMessage(msg);
    }

    public void sendMessage(int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        sendMessage(msg);
    }

    public void replyToMessage(Message srcMsg, Message dstMsg) {
        try {
            dstMsg.replyTo = this.mSrcMessenger;
            srcMsg.replyTo.send(dstMsg);
        } catch (RemoteException e) {
            log("TODO: handle replyToMessage RemoteException" + e);
            e.printStackTrace();
        }
    }

    public void replyToMessage(Message srcMsg, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        replyToMessage(srcMsg, msg);
    }

    public void replyToMessage(Message srcMsg, int what, int arg1) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        replyToMessage(srcMsg, msg);
    }

    public void replyToMessage(Message srcMsg, int what, int arg1, int arg2) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        replyToMessage(srcMsg, msg);
    }

    public void replyToMessage(Message srcMsg, int what, int arg1, int arg2, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        replyToMessage(srcMsg, msg);
    }

    public void replyToMessage(Message srcMsg, int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        replyToMessage(srcMsg, msg);
    }

    public Message sendMessageSynchronously(Message msg) {
        return SyncMessenger.sendMessageSynchronously(this.mDstMessenger, msg);
    }

    public Message sendMessageSynchronously(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageSynchronously(msg);
    }

    public Message sendMessageSynchronously(int what, int arg1) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        return sendMessageSynchronously(msg);
    }

    public Message sendMessageSynchronously(int what, int arg1, int arg2) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        return sendMessageSynchronously(msg);
    }

    public Message sendMessageSynchronously(int what, int arg1, int arg2, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        return sendMessageSynchronously(msg);
    }

    public Message sendMessageSynchronously(int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        return sendMessageSynchronously(msg);
    }

    private void replyHalfConnected(int status) {
        Message msg = this.mSrcHandler.obtainMessage(CMD_CHANNEL_HALF_CONNECTED);
        msg.arg1 = status;
        msg.obj = this;
        msg.replyTo = this.mDstMessenger;
        if (!linkToDeathMonitor()) {
            msg.arg1 = STATUS_BINDING_UNSUCCESSFUL;
        }
        this.mSrcHandler.sendMessage(msg);
    }

    private boolean linkToDeathMonitor() {
        if (this.mConnection == null && this.mDeathMonitor == null) {
            this.mDeathMonitor = new DeathMonitor();
            try {
                this.mDstMessenger.getBinder().linkToDeath(this.mDeathMonitor, STATUS_SUCCESSFUL);
            } catch (RemoteException e) {
                this.mDeathMonitor = null;
                return DBG;
            }
        }
        return true;
    }

    private void replyDisconnected(int status) {
        if (this.mSrcHandler != null) {
            Message msg = this.mSrcHandler.obtainMessage(CMD_CHANNEL_DISCONNECTED);
            msg.arg1 = status;
            msg.obj = this;
            msg.replyTo = this.mDstMessenger;
            this.mSrcHandler.sendMessage(msg);
        }
    }

    private static void log(String s) {
        Slog.d(TAG, s);
    }
}
