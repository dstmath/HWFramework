package com.android.internal.os;

import android.net.LocalSocket;
import java.io.BufferedReader;
import java.io.DataOutputStream;

public interface IHwZygoteEx {
    void handlePreloadApp(String str);

    ZygoteArguments preloadApplication(String str, BufferedReader bufferedReader, DataOutputStream dataOutputStream, LocalSocket localSocket);
}
