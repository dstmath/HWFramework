package com.android.server.display;

import android.content.Context;
import android.os.Handler;

public interface IHwUibcReceiver {
    public static final int DESTORY_TIMEOUT = 16000;

    int createReceiver(Context context, Handler handler);

    void destroyReceiver();

    Runnable getAcceptCheck();

    void resumeReceiver();

    void setRemoteMacAddress(String str);

    void setRemoteScreenSize(int i, int i2);

    void startReceiver();

    void stopReceiver();

    void suspendReceiver();
}
