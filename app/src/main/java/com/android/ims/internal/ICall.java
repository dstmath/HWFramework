package com.android.ims.internal;

public interface ICall {
    boolean checkIfRemoteUserIsSame(String str);

    void close();

    boolean equalsTo(ICall iCall);
}
