package com.huawei.nearbysdk;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.nearbysdk.IAuthListener.Stub;

public class AuthListenerTransport extends Stub {
    private static final String BUNDLE_AUTH_ID = "AUTH_ID";
    private static final String BUNDLE_RESULT = "RESULT";
    private static final String BUNDLE_RSA_BYTES = "BUNDLE_RSA_BYTES";
    private static final String BUNDLE_SESSION_KEY = "SESSION_KEY";
    private static final String BUNDLE_SESSION_KEY_IV = "SESSION_KEY_IV";
    static final String TAG = "AuthListenerTransport";
    private static final int TYPE_ON_AUTH_RESULT = 1;
    private AuthListener mListener;
    private final Handler mListenerHandler;
    private NearbyAdapter mNearbyAdapter;

    public void onAuthentificationResult(long authId, boolean result, byte[] sessionKey, byte[] sessionKeyIV, byte[] rsa_bytes, NearbyDevice device) {
        HwLog.d(TAG, "onAuthentificationResult authId = " + authId + "; result = " + result + ";device = " + device);
        sendMessage(1, authId, result, sessionKey, sessionKeyIV, rsa_bytes, device);
    }

    private void sendMessage(int msgWhat, long authId, boolean result, byte[] sessionKey, byte[] sessionKeyIV, byte[] rsa_bytes, NearbyDevice device) {
        Message msg = this.mListenerHandler.obtainMessage(msgWhat, device);
        Bundle bundle = new Bundle();
        bundle.putByteArray(BUNDLE_SESSION_KEY, sessionKey);
        bundle.putByteArray(BUNDLE_SESSION_KEY_IV, sessionKeyIV);
        bundle.putByteArray(BUNDLE_RSA_BYTES, rsa_bytes);
        bundle.putBoolean(BUNDLE_RESULT, result);
        bundle.putLong(BUNDLE_AUTH_ID, authId);
        msg.setData(bundle);
        this.mListenerHandler.sendMessage(msg);
    }

    AuthListenerTransport(NearbyAdapter nearbyAdapter, AuthListener listener, Looper looper) {
        this.mListener = listener;
        this.mNearbyAdapter = nearbyAdapter;
        this.mListenerHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                AuthListenerTransport.this._handleMessage(msg);
            }
        };
    }

    private void _handleMessage(Message msg) {
        NearbyDevice device = msg.obj;
        byte[] sessionKey = msg.getData().getByteArray(BUNDLE_SESSION_KEY);
        byte[] sessionKeyIV = msg.getData().getByteArray(BUNDLE_SESSION_KEY_IV);
        byte[] rsa_bytes = msg.getData().getByteArray(BUNDLE_RSA_BYTES);
        boolean result = msg.getData().getBoolean(BUNDLE_RESULT);
        long authId = msg.getData().getLong(BUNDLE_AUTH_ID);
        HwLog.d(TAG, "_handleMessage: " + msg.toString());
        switch (msg.what) {
            case 1:
                HwLog.d(TAG, "Listener.onAuthentificationResult authId = " + authId + "; result = " + result);
                if (this.mNearbyAdapter != null) {
                    if (device == null) {
                        device = this.mNearbyAdapter.getDevice(authId);
                    }
                    this.mNearbyAdapter.setSessionKey(authId, sessionKey, sessionKeyIV, rsa_bytes, device);
                    this.mListener.onAuthentificationResult(authId, result, sessionKey, device);
                    return;
                }
                return;
            default:
                HwLog.e(TAG, "_handleMessage: unknown message " + msg.what);
                return;
        }
    }
}
