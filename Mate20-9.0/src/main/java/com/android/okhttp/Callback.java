package com.android.okhttp;

import java.io.IOException;

public interface Callback {
    void onFailure(Request request, IOException iOException);

    void onResponse(Response response) throws IOException;
}
