package com.huawei.android.feature.install;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.feature.IDynamicInstall;
import java.util.ArrayList;
import java.util.List;

public abstract class RemoteServiceConnector {
    /* access modifiers changed from: private */
    public static final String TAG = RemoteServiceConnector.class.getSimpleName();
    /* access modifiers changed from: private */
    public static IBinder.DeathRecipient mDeathRecipient = new u();
    private List<RemoteRequest> mCommands = new ArrayList();
    /* access modifiers changed from: private */
    public Context mContext;
    private HandlerThread mHandlerThread;
    /* access modifiers changed from: private */
    public boolean mIsWaitingConnect;
    /* access modifiers changed from: private */
    public IDynamicInstall mRemoteProxy;
    /* access modifiers changed from: private */
    public ServiceConnection mServiceConnection;
    private Handler mWorkHandler;

    public RemoteServiceConnector(Context context) {
        this.mContext = context;
        this.mHandlerThread = new HandlerThread(RemoteServiceConnector.class.getSimpleName());
        this.mHandlerThread.start();
        this.mWorkHandler = new Handler(this.mHandlerThread.getLooper());
    }

    /* access modifiers changed from: private */
    public void doCommand(RemoteRequest remoteRequest) {
        if (this.mRemoteProxy == null && !this.mIsWaitingConnect) {
            Intent bindServiceIntent = getBindServiceIntent();
            this.mCommands.add(remoteRequest);
            this.mIsWaitingConnect = true;
            this.mServiceConnection = new t(this);
            if (!this.mContext.bindService(bindServiceIntent, this.mServiceConnection, 1)) {
                Log.d(TAG, "can not bind service error");
                this.mIsWaitingConnect = false;
                handleBindRemoteServiceError(this.mCommands);
                return;
            }
            Log.d(TAG, "bind return true");
        } else if (this.mIsWaitingConnect) {
            this.mCommands.add(remoteRequest);
        } else {
            Log.d(TAG, "IBinder not null");
            remoteRequest.run();
        }
    }

    /* access modifiers changed from: private */
    public void handleServiceConnected(IBinder iBinder) {
        this.mRemoteProxy = IDynamicInstall.Stub.asInterface(iBinder);
        try {
            this.mRemoteProxy.asBinder().linkToDeath(mDeathRecipient, 0);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
        this.mIsWaitingConnect = false;
        for (RemoteRequest post : this.mCommands) {
            this.mWorkHandler.post(post);
        }
        this.mCommands.clear();
    }

    /* access modifiers changed from: private */
    public void handleServiceDisconnected() {
        if (this.mRemoteProxy != null) {
            this.mRemoteProxy.asBinder().unlinkToDeath(mDeathRecipient, 0);
        }
        this.mRemoteProxy = null;
        this.mIsWaitingConnect = false;
    }

    public void doRemoteRequest(RemoteRequest remoteRequest) {
        this.mWorkHandler.post(new s(this, remoteRequest));
    }

    public abstract Intent getBindServiceIntent();

    public IDynamicInstall getRemoteProxy() {
        return this.mRemoteProxy;
    }

    public abstract void handleBindRemoteServiceError(List<RemoteRequest> list);

    public void quit() {
        this.mWorkHandler.post(new v(this));
    }
}
