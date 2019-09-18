package com.android.server.zrhung.appeye;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.zrhung.ZrHungData;
import com.android.server.zrhung.IZRHungService;
import com.android.server.zrhung.ZRHungService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import libcore.io.IoUtils;

public class AppEyeSocketThread extends Thread {
    private static final String SOCKET_NAME = "ZRHungServer";
    static final String TAG = "AppEyeSocketThread";
    private static IZRHungService mZrHungService = null;
    private ArrayList<String> list;

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b9, code lost:
        java.lang.Thread.sleep(com.android.server.hidata.arbitration.HwArbitrationDEFS.DelayTimeMillisA);
        android.util.Log.i(TAG, "connecting socket: ZRHungServer");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00c7, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00c9, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        android.util.Log.w(TAG, "errors while connecting socket: " + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0100, code lost:
        libcore.io.IoUtils.closeQuietly(null);
        libcore.io.IoUtils.closeQuietly(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0106, code lost:
        throw r2;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00c9 A[ExcHandler: InterruptedException (r2v1 'e' java.lang.InterruptedException A[CUSTOM_DECLARE]), PHI: r0 
      PHI: (r0v3 'socket' android.net.LocalSocket) = (r0v0 'socket' android.net.LocalSocket), (r0v5 'socket' android.net.LocalSocket), (r0v5 'socket' android.net.LocalSocket), (r0v5 'socket' android.net.LocalSocket), (r0v5 'socket' android.net.LocalSocket) binds: [B:1:0x0002, B:2:?, B:3:0x0011, B:4:?, B:5:0x001d] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x0002] */
    private void connectServer() {
        LocalSocket socket = null;
        BufferedReader in = null;
        try {
            socket = new LocalSocket();
            LocalSocketAddress address = new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.RESERVED);
            while (true) {
                socket.connect(address);
                Log.i(TAG, "socket connect OK");
                break;
            }
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                String result = in.readLine();
                if (result == null) {
                    break;
                } else if (result.equals("START")) {
                    this.list.clear();
                    this.list.add(result);
                } else if (!result.equals("END")) {
                    this.list.add(result);
                } else if (this.list.isEmpty()) {
                    this.list.clear();
                } else if (!this.list.get(0).equals("START")) {
                    Log.e(TAG, "START END not match");
                    this.list.clear();
                } else {
                    this.list.remove(0);
                    AppEyeMessage message = new AppEyeMessage();
                    if (message.parseMsg(this.list) == 0) {
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
        } catch (IOException e) {
            Log.w(TAG, "errors while connecting socket: " + e);
        } catch (InterruptedException e2) {
        }
        IoUtils.closeQuietly(in);
        IoUtils.closeQuietly(socket);
    }

    public void run() {
        while (true) {
            connectServer();
        }
    }

    public AppEyeSocketThread() {
        this.list = null;
        this.list = new ArrayList<>();
    }
}
