package com.leisen.wallet.sdk.newhttp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public interface ResponseHandlerInterfaceX {
    URL getRequestURI();

    boolean getUseSynchronousMode();

    void sendCancelMessage();

    void sendFailureMessage(int i, byte[] bArr, Throwable th);

    void sendFinishMessage();

    void sendProgressMessage(int i, int i2);

    void sendResponseMessage(HttpURLConnection httpURLConnection) throws IOException;

    void sendRetryMessage(int i);

    void sendStartMessage();

    void sendSuccessMessage(int i, byte[] bArr);

    void setRequestURI(URL url);

    void setUseSynchronousMode(boolean z);
}
