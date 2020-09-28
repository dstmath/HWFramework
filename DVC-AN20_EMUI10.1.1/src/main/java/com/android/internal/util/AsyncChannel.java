package com.android.internal.util;

import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Slog;
import java.util.Objects;
import java.util.Stack;

public class AsyncChannel {
    private static final int BASE = 69632;
    public static final int CMD_CHANNEL_DISCONNECT = 69635;
    public static final int CMD_CHANNEL_DISCONNECTED = 69636;
    public static final int CMD_CHANNEL_FULLY_CONNECTED = 69634;
    @UnsupportedAppUsage
    public static final int CMD_CHANNEL_FULL_CONNECTION = 69633;
    @UnsupportedAppUsage
    public static final int CMD_CHANNEL_HALF_CONNECTED = 69632;
    private static final int CMD_TO_STRING_COUNT = 5;
    private static final boolean DBG = false;
    public static final int STATUS_BINDING_UNSUCCESSFUL = 1;
    public static final int STATUS_FULL_CONNECTION_REFUSED_ALREADY_CONNECTED = 3;
    public static final int STATUS_REMOTE_DISCONNECTION = 4;
    public static final int STATUS_SEND_UNSUCCESSFUL = 2;
    @UnsupportedAppUsage
    public static final int STATUS_SUCCESSFUL = 0;
    private static final String TAG = "AsyncChannel";
    private static String[] sCmdToString = new String[5];
    private AsyncChannelConnection mConnection;
    private DeathMonitor mDeathMonitor;
    private Messenger mDstMessenger;
    private Context mSrcContext;
    private Handler mSrcHandler;
    private Messenger mSrcMessenger;

    static {
        String[] strArr = sCmdToString;
        strArr[0] = "CMD_CHANNEL_HALF_CONNECTED";
        strArr[1] = "CMD_CHANNEL_FULL_CONNECTION";
        strArr[2] = "CMD_CHANNEL_FULLY_CONNECTED";
        strArr[3] = "CMD_CHANNEL_DISCONNECT";
        strArr[4] = "CMD_CHANNEL_DISCONNECTED";
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    protected static String cmdToString(int cmd) {
        int cmd2 = cmd - 69632;
        if (cmd2 < 0) {
            return null;
        }
        String[] strArr = sCmdToString;
        if (cmd2 < strArr.length) {
            return strArr[cmd2];
        }
        return null;
    }

    public int connectSrcHandlerToPackageSync(Context srcContext, Handler srcHandler, String dstPackageName, String dstClassName) {
        this.mConnection = new AsyncChannelConnection();
        this.mSrcContext = srcContext;
        this.mSrcHandler = srcHandler;
        this.mSrcMessenger = new Messenger(srcHandler);
        this.mDstMessenger = null;
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(dstPackageName, dstClassName);
        return !srcContext.bindService(intent, this.mConnection, 1);
    }

    @UnsupportedAppUsage
    public int connectSync(Context srcContext, Handler srcHandler, Messenger dstMessenger) {
        connected(srcContext, srcHandler, dstMessenger);
        return 0;
    }

    public int connectSync(Context srcContext, Handler srcHandler, Handler dstHandler) {
        return connectSync(srcContext, srcHandler, new Messenger(dstHandler));
    }

    public int fullyConnectSync(Context srcContext, Handler srcHandler, Handler dstHandler) {
        int status = connectSync(srcContext, srcHandler, dstHandler);
        if (status == 0) {
            return sendMessageSynchronously(CMD_CHANNEL_FULL_CONNECTION).arg1;
        }
        return status;
    }

    public void connect(Context srcContext, Handler srcHandler, String dstPackageName, String dstClassName) {
        new Thread(new Runnable(srcContext, srcHandler, dstPackageName, dstClassName) {
            /* class com.android.internal.util.AsyncChannel.AnonymousClass1ConnectAsync */
            String mDstClassName;
            String mDstPackageName;
            Context mSrcCtx;
            Handler mSrcHdlr;

            {
                this.mSrcCtx = srcContext;
                this.mSrcHdlr = srcHandler;
                this.mDstPackageName = dstPackageName;
                this.mDstClassName = dstClassName;
            }

            public void run() {
                AsyncChannel.this.replyHalfConnected(AsyncChannel.this.connectSrcHandlerToPackageSync(this.mSrcCtx, this.mSrcHdlr, this.mDstPackageName, this.mDstClassName));
            }
        }).start();
    }

    public void connect(Context srcContext, Handler srcHandler, Class<?> klass) {
        connect(srcContext, srcHandler, klass.getPackage().getName(), klass.getName());
    }

    @UnsupportedAppUsage
    public void connect(Context srcContext, Handler srcHandler, Messenger dstMessenger) {
        connected(srcContext, srcHandler, dstMessenger);
        replyHalfConnected(0);
    }

    @UnsupportedAppUsage
    public void connected(Context srcContext, Handler srcHandler, Messenger dstMessenger) {
        this.mSrcContext = srcContext;
        this.mSrcHandler = srcHandler;
        this.mSrcMessenger = new Messenger(this.mSrcHandler);
        this.mDstMessenger = dstMessenger;
    }

    public void connect(Context srcContext, Handler srcHandler, Handler dstHandler) {
        connect(srcContext, srcHandler, new Messenger(dstHandler));
    }

    public void connect(AsyncService srcAsyncService, Messenger dstMessenger) {
        connect(srcAsyncService, srcAsyncService.getHandler(), dstMessenger);
    }

    public void disconnected() {
        this.mSrcContext = null;
        this.mSrcHandler = null;
        this.mSrcMessenger = null;
        this.mDstMessenger = null;
        this.mDeathMonitor = null;
        this.mConnection = null;
    }

    @UnsupportedAppUsage
    public void disconnect() {
        Messenger messenger;
        Context context;
        AsyncChannelConnection asyncChannelConnection = this.mConnection;
        if (!(asyncChannelConnection == null || (context = this.mSrcContext) == null)) {
            context.unbindService(asyncChannelConnection);
            this.mConnection = null;
        }
        try {
            Message msg = Message.obtain();
            msg.what = CMD_CHANNEL_DISCONNECTED;
            msg.replyTo = this.mSrcMessenger;
            if (this.mDstMessenger != null) {
                this.mDstMessenger.send(msg);
            }
        } catch (Exception e) {
        }
        replyDisconnected(0);
        this.mSrcHandler = null;
        if (this.mConnection == null && (messenger = this.mDstMessenger) != null && this.mDeathMonitor != null) {
            messenger.getBinder().unlinkToDeath(this.mDeathMonitor, 0);
            this.mDeathMonitor = null;
        }
    }

    @UnsupportedAppUsage
    public void sendMessage(Message msg) {
        msg.replyTo = this.mSrcMessenger;
        try {
            if (this.mDstMessenger != null) {
                this.mDstMessenger.send(msg);
            }
        } catch (RemoteException e) {
            replyDisconnected(2);
        }
    }

    @UnsupportedAppUsage
    public void sendMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        sendMessage(msg);
    }

    @UnsupportedAppUsage
    public void sendMessage(int what, int arg1) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        sendMessage(msg);
    }

    @UnsupportedAppUsage
    public void sendMessage(int what, int arg1, int arg2) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        sendMessage(msg);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public void replyToMessage(Message srcMsg, Message dstMsg) {
        try {
            dstMsg.replyTo = this.mSrcMessenger;
            srcMsg.replyTo.send(dstMsg);
        } catch (RemoteException e) {
            log("TODO: handle replyToMessage RemoteException" + e);
            e.printStackTrace();
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void replyToMessage(Message srcMsg, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        replyToMessage(srcMsg, msg);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public void replyToMessage(Message srcMsg, int what, int arg1, int arg2, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        replyToMessage(srcMsg, msg);
    }

    @UnsupportedAppUsage
    public void replyToMessage(Message srcMsg, int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        replyToMessage(srcMsg, msg);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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

    /* access modifiers changed from: private */
    public static class SyncMessenger {
        private static int sCount = 0;
        private static Stack<SyncMessenger> sStack = new Stack<>();
        private SyncHandler mHandler;
        private HandlerThread mHandlerThread;
        private Messenger mMessenger;

        private SyncMessenger() {
        }

        /* access modifiers changed from: private */
        public class SyncHandler extends Handler {
            private Object mLockObject;
            private Message mResultMsg;

            private SyncHandler(Looper looper) {
                super(looper);
                this.mLockObject = new Object();
            }

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                Message msgCopy = Message.obtain();
                msgCopy.copyFrom(msg);
                synchronized (this.mLockObject) {
                    this.mResultMsg = msgCopy;
                    this.mLockObject.notify();
                }
            }
        }

        private static SyncMessenger obtain() {
            SyncMessenger sm;
            synchronized (sStack) {
                if (sStack.isEmpty()) {
                    sm = new SyncMessenger();
                    StringBuilder sb = new StringBuilder();
                    sb.append("SyncHandler-");
                    int i = sCount;
                    sCount = i + 1;
                    sb.append(i);
                    sm.mHandlerThread = new HandlerThread(sb.toString());
                    sm.mHandlerThread.start();
                    Objects.requireNonNull(sm);
                    sm.mHandler = new SyncHandler(sm.mHandlerThread.getLooper());
                    sm.mMessenger = new Messenger(sm.mHandler);
                } else {
                    sm = sStack.pop();
                }
            }
            return sm;
        }

        private void recycle() {
            synchronized (sStack) {
                sStack.push(this);
            }
        }

        /* access modifiers changed from: private */
        public static Message sendMessageSynchronously(Messenger dstMessenger, Message msg) {
            SyncMessenger sm = obtain();
            Message resultMsg = null;
            if (!(dstMessenger == null || msg == null)) {
                try {
                    msg.replyTo = sm.mMessenger;
                    synchronized (sm.mHandler.mLockObject) {
                        if (sm.mHandler.mResultMsg != null) {
                            Slog.wtf(AsyncChannel.TAG, "mResultMsg should be null here");
                            sm.mHandler.mResultMsg = null;
                        }
                        dstMessenger.send(msg);
                        sm.mHandler.mLockObject.wait();
                        resultMsg = sm.mHandler.mResultMsg;
                        sm.mHandler.mResultMsg = null;
                    }
                } catch (InterruptedException e) {
                    Slog.e(AsyncChannel.TAG, "error in sendMessageSynchronously", e);
                } catch (RemoteException e2) {
                    Slog.e(AsyncChannel.TAG, "error in sendMessageSynchronously", e2);
                }
            }
            sm.recycle();
            return resultMsg;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyHalfConnected(int status) {
        Message msg = this.mSrcHandler.obtainMessage(69632);
        msg.arg1 = status;
        msg.obj = this;
        msg.replyTo = this.mDstMessenger;
        if (!linkToDeathMonitor()) {
            msg.arg1 = 1;
        }
        this.mSrcHandler.sendMessage(msg);
    }

    private boolean linkToDeathMonitor() {
        if (this.mConnection != null || this.mDeathMonitor != null || this.mDstMessenger == null) {
            return true;
        }
        this.mDeathMonitor = new DeathMonitor();
        try {
            this.mDstMessenger.getBinder().linkToDeath(this.mDeathMonitor, 0);
            return true;
        } catch (RemoteException e) {
            this.mDeathMonitor = null;
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyDisconnected(int status) {
        Handler handler = this.mSrcHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage(CMD_CHANNEL_DISCONNECTED);
            msg.arg1 = status;
            msg.obj = this;
            msg.replyTo = this.mDstMessenger;
            this.mSrcHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: package-private */
    public class AsyncChannelConnection implements ServiceConnection {
        AsyncChannelConnection() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName className, IBinder service) {
            AsyncChannel.this.mDstMessenger = new Messenger(service);
            AsyncChannel.this.replyHalfConnected(0);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName className) {
            AsyncChannel.this.replyDisconnected(0);
        }
    }

    private static void log(String s) {
        Slog.d(TAG, s);
    }

    /* access modifiers changed from: private */
    public final class DeathMonitor implements IBinder.DeathRecipient {
        DeathMonitor() {
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            AsyncChannel.this.replyDisconnected(4);
        }
    }
}
