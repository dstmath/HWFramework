package com.android.server.zrhung.appeye;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.zrhung.ZrHungData;
import com.android.server.zrhung.IZRHungService;
import com.android.server.zrhung.ZRHungService;
import com.huawei.libcore.io.IoUtilsEx;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AppEyeSocketThread extends Thread {
    private static final String SOCKET_NAME = "ZRHungServer";
    static final String TAG = "AppEyeSocketThread";
    private List<String> msglists;

    public AppEyeSocketThread() {
        this.msglists = null;
        this.msglists = new ArrayList(16);
    }

    private void connectServer() {
        LocalSocket socket = null;
        BufferedReader in = null;
        try {
            socket = socketConnect();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                String result = in.readLine();
                if (result == null) {
                    break;
                } else if ("START".equals(result)) {
                    this.msglists.clear();
                    this.msglists.add(result);
                } else if (!"END".equals(result)) {
                    this.msglists.add(result);
                } else if (this.msglists.isEmpty()) {
                    this.msglists.clear();
                } else if (!"START".equals(this.msglists.get(0))) {
                    Log.e(TAG, "START END not match");
                    this.msglists.clear();
                } else {
                    this.msglists.remove(0);
                    AppEyeMessage message = new AppEyeMessage();
                    if (message.parseMsg(this.msglists) == 0) {
                        IZRHungService zrHungService = ZRHungService.getInstance();
                        if (zrHungService != null) {
                            ZrHungData beginData = new ZrHungData();
                            beginData.putString("eventtype", "socketrecover");
                            beginData.put("appeyemessage", message);
                            zrHungService.sendEvent(beginData);
                        }
                    }
                }
            }
            Log.w(TAG, "errors while receiving from socket: ZRHungServer");
        } catch (IOException | InterruptedException e) {
            Log.w(TAG, "errors while connecting socket");
        } catch (Throwable th) {
            IoUtilsEx.closeQuietly((AutoCloseable) null);
            IoUtilsEx.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtilsEx.closeQuietly(in);
        IoUtilsEx.closeQuietly(socket);
    }

    private LocalSocket socketConnect() throws InterruptedException {
        LocalSocket socket = new LocalSocket();
        LocalSocketAddress address = new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.RESERVED);
        while (true) {
            try {
                socket.connect(address);
                Log.i(TAG, "socket connect OK");
                break;
            } catch (IOException e) {
                Thread.sleep(30000);
                Log.i(TAG, "connecting socket: ZRHungServer");
            }
        }
        return socket;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        while (true) {
            connectServer();
        }
    }
}
