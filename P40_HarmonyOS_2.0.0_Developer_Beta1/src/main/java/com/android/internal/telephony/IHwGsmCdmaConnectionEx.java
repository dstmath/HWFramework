package com.android.internal.telephony;

public interface IHwGsmCdmaConnectionEx {
    default void onLineControlInfo() {
    }

    default boolean hasRevFWIM() {
        return false;
    }

    default boolean isEncryptCall() {
        return false;
    }

    default void setEncryptCall(boolean isEncryptCall) {
    }

    default boolean compareToNumber(String number) {
        return false;
    }
}
