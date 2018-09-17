package com.android.internal.telephony;

import android.os.ResultReceiver;

public interface MmiCode {

    public enum State {
        PENDING,
        CANCELLED,
        COMPLETE,
        FAILED
    }

    void cancel();

    String getDialString();

    CharSequence getMessage();

    Phone getPhone();

    State getState();

    ResultReceiver getUssdCallbackReceiver();

    boolean isCancelable();

    boolean isPinPukCommand();

    boolean isUssdRequest();

    void processCode() throws CallStateException;
}
