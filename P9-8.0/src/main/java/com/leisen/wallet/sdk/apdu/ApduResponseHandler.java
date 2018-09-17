package com.leisen.wallet.sdk.apdu;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class ApduResponseHandler {
    private static final int MESSAGE_FAILURE = 2;
    private static final int MESSAGE_SENDNEXT = 1;
    private static final int MESSAGE_SENDNEXT_ERROR = 3;
    private static final int MESSAGE_SUCCESS = 0;
    private ResponseHandler mHandler;
    private boolean useSynchronousMode;

    private static class ResponseHandler extends Handler {
        private final ApduResponseHandler mResponder;

        public ResponseHandler(ApduResponseHandler responder) {
            this.mResponder = responder;
        }

        public void handleMessage(Message msg) {
            this.mResponder.handleMessage(msg);
        }
    }

    public abstract void OnSendNextError(int i, int i2, String str, String str2, Error error);

    public abstract void onFailure(int i, Error error);

    public abstract void onSendNext(int i, int i2, String str, String str2);

    public abstract void onSuccess(String str);

    public ApduResponseHandler() {
        setUseSynchronousMode(true);
    }

    public void setUseSynchronousMode(boolean useSynchronousMode) {
        if (!useSynchronousMode && Looper.myLooper() == null) {
            useSynchronousMode = true;
        }
        if (!useSynchronousMode && this.mHandler == null) {
            this.mHandler = new ResponseHandler(this);
        } else if (useSynchronousMode && this.mHandler != null) {
            this.mHandler = null;
        }
        this.useSynchronousMode = useSynchronousMode;
    }

    public boolean getUseSynchronousMode() {
        return this.useSynchronousMode;
    }

    private void sendMessage(Message msg) {
        if (getUseSynchronousMode() || this.mHandler == null) {
            handleMessage(msg);
        } else if (!Thread.currentThread().isInterrupted()) {
            this.mHandler.sendMessage(msg);
        }
    }

    public void handleMessage(Message msg) {
        String str = null;
        Object[] objects;
        switch (msg.what) {
            case 0:
                objects = msg.obj;
                if (objects != null && objects.length >= 1) {
                    if (objects[0] != null) {
                        str = objects[0].toString();
                    }
                    onSuccess(str);
                    return;
                }
                return;
            case 1:
                objects = (Object[]) msg.obj;
                if (objects != null && objects.length >= 4) {
                    onSendNext(Integer.parseInt(objects[0].toString()), Integer.parseInt(objects[1].toString()), objects[2].toString(), objects[3].toString());
                    return;
                }
                return;
            case 2:
                objects = (Object[]) msg.obj;
                if (objects != null && objects.length >= 2) {
                    onFailure(Integer.parseInt(objects[0].toString()), (Error) objects[1]);
                    return;
                }
                return;
            case 3:
                objects = (Object[]) msg.obj;
                if (objects != null && objects.length >= 5) {
                    OnSendNextError(Integer.parseInt(objects[0].toString()), Integer.parseInt(objects[1].toString()), objects[2].toString(), objects[3].toString(), (Error) objects[4]);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void sendSuccessMessage(String response) {
        sendMessage(obtainMessage(0, new Object[]{response}));
    }

    public void sendSendNextMessage(int result, int index, String rapdu, String sw) {
        sendMessage(obtainMessage(1, new Object[]{Integer.valueOf(result), Integer.valueOf(index), rapdu, sw}));
    }

    public void sendSendNextErrorMessage(int result, int index, String rapdu, String sw, Error e) {
        sendMessage(obtainMessage(3, new Object[]{Integer.valueOf(result), Integer.valueOf(index), rapdu, sw, e}));
    }

    public void sendFailureMessage(int result, Error e) {
        sendMessage(obtainMessage(2, new Object[]{Integer.valueOf(result), e}));
    }

    private Message obtainMessage(int responseMessageId, Object responseMessageData) {
        if (this.mHandler != null) {
            return Message.obtain(this.mHandler, responseMessageId, responseMessageData);
        }
        Message msg = Message.obtain();
        if (msg == null) {
            return msg;
        }
        msg.what = responseMessageId;
        msg.obj = responseMessageData;
        return msg;
    }
}
