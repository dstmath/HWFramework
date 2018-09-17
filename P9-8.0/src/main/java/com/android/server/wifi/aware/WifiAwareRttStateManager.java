package com.android.server.wifi.aware;

import android.content.Context;
import android.net.wifi.IRttManager;
import android.net.wifi.IRttManager.Stub;
import android.net.wifi.RttManager.ParcelableRttParams;
import android.net.wifi.RttManager.ParcelableRttResults;
import android.net.wifi.RttManager.RttParams;
import android.net.wifi.RttManager.RttResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WifiAwareRttStateManager {
    private static final boolean DBG = false;
    private static final String TAG = "WifiAwareRttStateMgr";
    private static final boolean VDBG = false;
    private AsyncChannel mAsyncChannel;
    private final SparseArray<WifiAwareClientState> mPendingOperations = new SparseArray();

    private class AwareRttHandler extends Handler {
        AwareRttHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 69632:
                    if (msg.arg1 == 0) {
                        WifiAwareRttStateManager.this.mAsyncChannel.sendMessage(69633);
                    } else {
                        Log.e(WifiAwareRttStateManager.TAG, "Failed to set up channel connection to RTT service");
                        WifiAwareRttStateManager.this.mAsyncChannel = null;
                    }
                    return;
                case 69634:
                    return;
                case 69636:
                    Log.e(WifiAwareRttStateManager.TAG, "Channel connection to RTT service lost");
                    WifiAwareRttStateManager.this.mAsyncChannel = null;
                    return;
                default:
                    WifiAwareClientState client = WifiAwareRttStateManager.this.getAndRemovePendingOperationClient(msg.arg2);
                    if (client == null) {
                        Log.e(WifiAwareRttStateManager.TAG, "handleMessage(): RTT message (" + msg.what + ") -- cannot find registered pending operation client for ID " + msg.arg2);
                        return;
                    }
                    switch (msg.what) {
                        case 160258:
                            client.onRangingFailure(msg.arg2, msg.arg1, ((Bundle) msg.obj).getString("android.net.wifi.RttManager.Description"));
                            break;
                        case 160259:
                            int rangingId = msg.arg2;
                            ParcelableRttResults results = msg.obj;
                            for (RttResult rttResult : results.mResults) {
                                rttResult.bssid = null;
                            }
                            client.onRangingSuccess(rangingId, results);
                            break;
                        case 160260:
                            client.onRangingAborted(msg.arg2);
                            break;
                        default:
                            Log.e(WifiAwareRttStateManager.TAG, "handleMessage(): ignoring message " + msg.what);
                            break;
                    }
                    return;
            }
        }
    }

    public void start(Context context, Looper looper) {
        IRttManager service = Stub.asInterface(ServiceManager.getService("rttmanager"));
        if (service == null) {
            Log.e(TAG, "start(): not able to get WIFI_RTT_SERVICE");
        } else {
            startWithRttService(context, looper, service);
        }
    }

    public void startWithRttService(Context context, Looper looper, IRttManager service) {
        try {
            Messenger messenger = service.getMessenger();
            this.mAsyncChannel = new AsyncChannel();
            this.mAsyncChannel.connect(context, new AwareRttHandler(looper), messenger);
        } catch (RemoteException e) {
            Log.e(TAG, "start(): not able to getMessenger() of WIFI_RTT_SERVICE");
        }
    }

    private WifiAwareClientState getAndRemovePendingOperationClient(int rangingId) {
        WifiAwareClientState client = (WifiAwareClientState) this.mPendingOperations.get(rangingId);
        this.mPendingOperations.delete(rangingId);
        return client;
    }

    public void startRanging(int rangingId, WifiAwareClientState client, RttParams[] params) {
        if (this.mAsyncChannel == null) {
            Log.d(TAG, "startRanging(): AsyncChannel to RTT service not configured - failing");
            client.onRangingFailure(rangingId, -2, "Aware service not able to configure connection to RTT service");
            return;
        }
        this.mPendingOperations.put(rangingId, client);
        this.mAsyncChannel.sendMessage(160256, 0, rangingId, new ParcelableRttParams(params));
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("WifiAwareRttStateManager:");
        pw.println("  mPendingOperations: [" + this.mPendingOperations + "]");
    }
}
