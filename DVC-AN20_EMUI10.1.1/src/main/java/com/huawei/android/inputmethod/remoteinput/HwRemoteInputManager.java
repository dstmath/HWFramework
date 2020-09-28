package com.huawei.android.inputmethod.remoteinput;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.inputmethod.remoteinput.IRemoteInputService;

public class HwRemoteInputManager {
    private static final String DEVICE_ID = "0";
    private static final boolean IS_DEBUG_TV = SystemProperties.getBoolean("ro.config.remoteinput.debugtv", false);
    private static final String REMOTE_INPUT_SERVICE_ACTION = "com.huawei.aa.action.RemoteInputService";
    private static final String REMOTE_INPUT_SERVICE_PACKAGE = "com.huawei.systemserver";
    private static final String TAG = "HwRemoteInputManager";
    private static HwRemoteInputManager sRemoteInputManager;
    private String mCacheText;
    private Context mContext;
    private boolean mIsFocused = false;
    private boolean mIsNotified = false;
    private IRemoteInputCallback mRemoteInputCallback;
    private IRemoteInputService mRemoteInputService;
    private MyServiceConnection mServiceConnection = new MyServiceConnection();

    public static synchronized HwRemoteInputManager getInstance() {
        HwRemoteInputManager hwRemoteInputManager;
        synchronized (HwRemoteInputManager.class) {
            if (sRemoteInputManager == null) {
                sRemoteInputManager = new HwRemoteInputManager();
            }
            hwRemoteInputManager = sRemoteInputManager;
        }
        return hwRemoteInputManager;
    }

    public void setContext(Context context) {
        if (context == null) {
            Log.e(TAG, "context should not be null");
            return;
        }
        this.mContext = context;
        if (this.mRemoteInputService == null) {
            bindRemoteInputService();
        }
    }

    public void setText(String text, Bundle style) {
        debugLog("set text from mini keyboard with text: " + text);
        IRemoteInputService iRemoteInputService = this.mRemoteInputService;
        if (iRemoteInputService == null) {
            Log.i(TAG, "service not bind, mRemoteInputService is null");
            this.mCacheText = text;
            return;
        }
        try {
            iRemoteInputService.setTextFromApp(text, style);
        } catch (RemoteException e) {
            Log.e(TAG, "setTextFromApp with RemoteException");
        }
    }

    public void notifyFocus(boolean isFocused) {
        Log.i(TAG, "notify focus from mini keyboard");
        IRemoteInputService iRemoteInputService = this.mRemoteInputService;
        if (iRemoteInputService == null) {
            this.mIsFocused = isFocused;
            return;
        }
        try {
            iRemoteInputService.notifyFocus(isFocused);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyFocus with RemoteException");
        }
        this.mIsNotified = true;
        if (!isFocused && this.mRemoteInputService != null) {
            this.mCacheText = null;
        }
    }

    public void setCallBack(IRemoteInputCallback remoteInputCallback) {
        Log.i(TAG, "setCallBack from mini keyboard");
        if (this.mRemoteInputService != null) {
            Log.i(TAG, "setCallBack to remote input service");
            try {
                this.mRemoteInputService.setCallBack("0", remoteInputCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "setCallBack with RemoteException");
            }
        } else {
            Log.i(TAG, "service not bind yet");
            this.mRemoteInputCallback = remoteInputCallback;
        }
    }

    /* access modifiers changed from: private */
    public class MyServiceConnection implements ServiceConnection {
        private MyServiceConnection() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.i(HwRemoteInputManager.TAG, "onServiceConnected to " + name);
            if (binder == null) {
                Log.e(HwRemoteInputManager.TAG, "can not bind remote input service, please check permission");
                HwRemoteInputManager.this.mCacheText = null;
                return;
            }
            HwRemoteInputManager.this.mRemoteInputService = IRemoteInputService.Stub.asInterface(binder);
            if (HwRemoteInputManager.this.mCacheText != null) {
                try {
                    HwRemoteInputManager hwRemoteInputManager = HwRemoteInputManager.this;
                    hwRemoteInputManager.debugLog("setTextFromApp to remote input service on service connected " + HwRemoteInputManager.this.mCacheText);
                    HwRemoteInputManager.this.mRemoteInputService.setTextFromApp(HwRemoteInputManager.this.mCacheText, null);
                } catch (RemoteException e) {
                    Log.e(HwRemoteInputManager.TAG, "setTextFromApp with RemoteException");
                }
                HwRemoteInputManager.this.mCacheText = null;
            }
            if (!HwRemoteInputManager.this.mIsNotified) {
                try {
                    Log.i(HwRemoteInputManager.TAG, "notifyFocus to remote input service on service connected " + HwRemoteInputManager.this.mIsFocused);
                    HwRemoteInputManager.this.mRemoteInputService.notifyFocus(HwRemoteInputManager.this.mIsFocused);
                } catch (RemoteException e2) {
                    Log.e(HwRemoteInputManager.TAG, "notifyFocus with RemoteException");
                }
                HwRemoteInputManager.this.mIsNotified = true;
            }
            if (HwRemoteInputManager.this.mRemoteInputCallback != null) {
                Log.i(HwRemoteInputManager.TAG, "setCallBack to remote input service on service connected");
                try {
                    HwRemoteInputManager.this.mRemoteInputService.setCallBack("0", HwRemoteInputManager.this.mRemoteInputCallback);
                } catch (RemoteException e3) {
                    Log.e(HwRemoteInputManager.TAG, "setCallBack with RemoteException");
                }
                HwRemoteInputManager.this.mRemoteInputCallback = null;
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(HwRemoteInputManager.TAG, "onServiceDisconnected from " + componentName);
        }
    }

    private void bindRemoteInputService() {
        Log.i(TAG, "try to bind remote input service");
        Intent intent = new Intent(REMOTE_INPUT_SERVICE_ACTION);
        intent.setPackage("com.huawei.systemserver");
        try {
            this.mContext.bindService(intent, this.mServiceConnection, 1);
        } catch (IllegalStateException e) {
            Log.e(TAG, "bind remote aa, exception happens");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void debugLog(String msg) {
        if (IS_DEBUG_TV) {
            Log.d(TAG, msg);
        }
    }
}
