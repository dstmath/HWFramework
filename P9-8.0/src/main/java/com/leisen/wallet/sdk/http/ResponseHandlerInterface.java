package com.leisen.wallet.sdk.http;

import java.io.IOException;
import java.net.URI;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

public interface ResponseHandlerInterface {
    Header[] getRequestHeaders();

    URI getRequestURI();

    boolean getUseSynchronousMode();

    void sendCancelMessage();

    void sendFailureMessage(int i, Header[] headerArr, byte[] bArr, Throwable th);

    void sendFinishMessage();

    void sendProgressMessage(int i, int i2);

    void sendResponseMessage(HttpResponse httpResponse) throws IOException;

    void sendRetryMessage(int i);

    void sendStartMessage();

    void sendSuccessMessage(int i, Header[] headerArr, byte[] bArr);

    void setRequestHeaders(Header[] headerArr);

    void setRequestURI(URI uri);

    void setUseSynchronousMode(boolean z);
}
