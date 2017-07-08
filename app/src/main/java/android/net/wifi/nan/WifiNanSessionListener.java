package android.net.wifi.nan;

import android.net.wifi.nan.IWifiNanSessionListener.Stub;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class WifiNanSessionListener {
    private static final boolean DBG = false;
    public static final int FAIL_REASON_INVALID_ARGS = 1;
    public static final int FAIL_REASON_NO_MATCH_SESSION = 2;
    public static final int FAIL_REASON_NO_RESOURCES = 0;
    public static final int FAIL_REASON_OTHER = 3;
    public static final int LISTEN_HIDDEN_FLAGS = 245;
    public static final int LISTEN_MATCH = 16;
    public static final int LISTEN_MESSAGE_RECEIVED = 128;
    public static final int LISTEN_MESSAGE_SEND_FAIL = 64;
    public static final int LISTEN_MESSAGE_SEND_SUCCESS = 32;
    public static final int LISTEN_PUBLISH_FAIL = 1;
    public static final int LISTEN_PUBLISH_TERMINATED = 2;
    public static final int LISTEN_SUBSCRIBE_FAIL = 4;
    public static final int LISTEN_SUBSCRIBE_TERMINATED = 8;
    private static final String MESSAGE_BUNDLE_KEY_MESSAGE = "message";
    private static final String MESSAGE_BUNDLE_KEY_MESSAGE2 = "message2";
    private static final String MESSAGE_BUNDLE_KEY_PEER_ID = "peer_id";
    private static final String TAG = "WifiNanSessionListener";
    public static final int TERMINATE_REASON_DONE = 0;
    public static final int TERMINATE_REASON_FAIL = 1;
    private static final boolean VDBG = false;
    public IWifiNanSessionListener callback;
    private final Handler mHandler;

    /* renamed from: android.net.wifi.nan.WifiNanSessionListener.2 */
    class AnonymousClass2 extends Handler {
        AnonymousClass2(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WifiNanSessionListener.TERMINATE_REASON_FAIL /*1*/:
                    WifiNanSessionListener.this.onPublishFail(msg.arg1);
                case WifiNanSessionListener.LISTEN_PUBLISH_TERMINATED /*2*/:
                    WifiNanSessionListener.this.onPublishTerminated(msg.arg1);
                case WifiNanSessionListener.LISTEN_SUBSCRIBE_FAIL /*4*/:
                    WifiNanSessionListener.this.onSubscribeFail(msg.arg1);
                case WifiNanSessionListener.LISTEN_SUBSCRIBE_TERMINATED /*8*/:
                    WifiNanSessionListener.this.onSubscribeTerminated(msg.arg1);
                case WifiNanSessionListener.LISTEN_MATCH /*16*/:
                    WifiNanSessionListener.this.onMatch(msg.getData().getInt(WifiNanSessionListener.MESSAGE_BUNDLE_KEY_PEER_ID), msg.getData().getByteArray(WifiNanSessionListener.MESSAGE_BUNDLE_KEY_MESSAGE), msg.arg1, msg.getData().getByteArray(WifiNanSessionListener.MESSAGE_BUNDLE_KEY_MESSAGE2), msg.arg2);
                case WifiNanSessionListener.LISTEN_MESSAGE_SEND_SUCCESS /*32*/:
                    WifiNanSessionListener.this.onMessageSendSuccess(msg.arg1);
                case WifiNanSessionListener.LISTEN_MESSAGE_SEND_FAIL /*64*/:
                    WifiNanSessionListener.this.onMessageSendFail(msg.arg1, msg.arg2);
                case WifiNanSessionListener.LISTEN_MESSAGE_RECEIVED /*128*/:
                    WifiNanSessionListener.this.onMessageReceived(msg.arg2, msg.getData().getByteArray(WifiNanSessionListener.MESSAGE_BUNDLE_KEY_MESSAGE), msg.arg1);
                default:
            }
        }
    }

    public WifiNanSessionListener() {
        this(Looper.myLooper());
    }

    public WifiNanSessionListener(Looper looper) {
        this.callback = new Stub() {
            public void onPublishFail(int reason) {
                Message msg = WifiNanSessionListener.this.mHandler.obtainMessage(WifiNanSessionListener.TERMINATE_REASON_FAIL);
                msg.arg1 = reason;
                WifiNanSessionListener.this.mHandler.sendMessage(msg);
            }

            public void onPublishTerminated(int reason) {
                Message msg = WifiNanSessionListener.this.mHandler.obtainMessage(WifiNanSessionListener.LISTEN_PUBLISH_TERMINATED);
                msg.arg1 = reason;
                WifiNanSessionListener.this.mHandler.sendMessage(msg);
            }

            public void onSubscribeFail(int reason) {
                Message msg = WifiNanSessionListener.this.mHandler.obtainMessage(WifiNanSessionListener.LISTEN_SUBSCRIBE_FAIL);
                msg.arg1 = reason;
                WifiNanSessionListener.this.mHandler.sendMessage(msg);
            }

            public void onSubscribeTerminated(int reason) {
                Message msg = WifiNanSessionListener.this.mHandler.obtainMessage(WifiNanSessionListener.LISTEN_SUBSCRIBE_TERMINATED);
                msg.arg1 = reason;
                WifiNanSessionListener.this.mHandler.sendMessage(msg);
            }

            public void onMatch(int peerId, byte[] serviceSpecificInfo, int serviceSpecificInfoLength, byte[] matchFilter, int matchFilterLength) {
                Bundle data = new Bundle();
                data.putInt(WifiNanSessionListener.MESSAGE_BUNDLE_KEY_PEER_ID, peerId);
                data.putByteArray(WifiNanSessionListener.MESSAGE_BUNDLE_KEY_MESSAGE, serviceSpecificInfo);
                data.putByteArray(WifiNanSessionListener.MESSAGE_BUNDLE_KEY_MESSAGE2, matchFilter);
                Message msg = WifiNanSessionListener.this.mHandler.obtainMessage(WifiNanSessionListener.LISTEN_MATCH);
                msg.arg1 = serviceSpecificInfoLength;
                msg.arg2 = matchFilterLength;
                msg.setData(data);
                WifiNanSessionListener.this.mHandler.sendMessage(msg);
            }

            public void onMessageSendSuccess(int messageId) {
                Message msg = WifiNanSessionListener.this.mHandler.obtainMessage(WifiNanSessionListener.LISTEN_MESSAGE_SEND_SUCCESS);
                msg.arg1 = messageId;
                WifiNanSessionListener.this.mHandler.sendMessage(msg);
            }

            public void onMessageSendFail(int messageId, int reason) {
                Message msg = WifiNanSessionListener.this.mHandler.obtainMessage(WifiNanSessionListener.LISTEN_MESSAGE_SEND_FAIL);
                msg.arg1 = messageId;
                msg.arg2 = reason;
                WifiNanSessionListener.this.mHandler.sendMessage(msg);
            }

            public void onMessageReceived(int peerId, byte[] message, int messageLength) {
                Bundle data = new Bundle();
                data.putByteArray(WifiNanSessionListener.MESSAGE_BUNDLE_KEY_MESSAGE, message);
                Message msg = WifiNanSessionListener.this.mHandler.obtainMessage(WifiNanSessionListener.LISTEN_MESSAGE_RECEIVED);
                msg.arg1 = messageLength;
                msg.arg2 = peerId;
                msg.setData(data);
                WifiNanSessionListener.this.mHandler.sendMessage(msg);
            }
        };
        this.mHandler = new AnonymousClass2(looper);
    }

    public void onPublishFail(int reason) {
    }

    public void onPublishTerminated(int reason) {
        Log.w(TAG, "onPublishTerminated: called in stub - override if interested or disable");
    }

    public void onSubscribeFail(int reason) {
    }

    public void onSubscribeTerminated(int reason) {
        Log.w(TAG, "onSubscribeTerminated: called in stub - override if interested or disable");
    }

    public void onMatch(int peerId, byte[] serviceSpecificInfo, int serviceSpecificInfoLength, byte[] matchFilter, int matchFilterLength) {
    }

    public void onMessageSendSuccess(int messageId) {
    }

    public void onMessageSendFail(int messageId, int reason) {
    }

    public void onMessageReceived(int peerId, byte[] message, int messageLength) {
    }
}
