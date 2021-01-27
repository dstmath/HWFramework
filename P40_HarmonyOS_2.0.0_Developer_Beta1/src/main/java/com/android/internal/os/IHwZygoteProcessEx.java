package com.android.internal.os;

import android.net.LocalSocket;
import java.io.IOException;

public interface IHwZygoteProcessEx {
    LocalSocket getOrCreateUsapSessionSocket(String str);

    LocalSocket getUsapSessionSocketFromArray(String str, int i);

    boolean isAppStartForMaple(String str);

    void putUsapSessionSocket(String str, int i, LocalSocket localSocket) throws IOException;

    void removeUsapSessionSocket(String str, int i);
}
