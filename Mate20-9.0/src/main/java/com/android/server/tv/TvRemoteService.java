package com.android.server.tv;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.tv.TvRemoteProviderProxy;
import com.android.server.tv.TvRemoteProviderWatcher;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class TvRemoteService extends SystemService implements Watchdog.Monitor {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_KEYS = false;
    private static final String TAG = "TvRemoteService";
    private Map<IBinder, UinputBridge> mBridgeMap = new ArrayMap();
    public final UserHandler mHandler;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public ArrayList<TvRemoteProviderProxy> mProviderList = new ArrayList<>();
    /* access modifiers changed from: private */
    public Map<IBinder, TvRemoteProviderProxy> mProviderMap = new ArrayMap();

    private final class UserHandler extends Handler {
        public static final int MSG_INPUT_BRIDGE_CONNECTED = 2;
        public static final int MSG_START = 1;
        private boolean mRunning;
        private final TvRemoteProviderWatcher mWatcher;

        public UserHandler(UserProvider provider, Context context) {
            super(Looper.getMainLooper(), null, true);
            this.mWatcher = new TvRemoteProviderWatcher(context, provider, this);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    start();
                    return;
                case 2:
                    IBinder token = (IBinder) msg.obj;
                    TvRemoteProviderProxy provider = (TvRemoteProviderProxy) TvRemoteService.this.mProviderMap.get(token);
                    if (provider != null) {
                        provider.inputBridgeConnected(token);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private void start() {
            if (!this.mRunning) {
                this.mRunning = true;
                this.mWatcher.start();
            }
        }
    }

    private final class UserProvider implements TvRemoteProviderWatcher.ProviderMethods, TvRemoteProviderProxy.ProviderMethods {
        private final TvRemoteService mService;

        public UserProvider(TvRemoteService service) {
            this.mService = service;
        }

        public void openInputBridge(TvRemoteProviderProxy provider, IBinder token, String name, int width, int height, int maxPointers) {
            synchronized (TvRemoteService.this.mLock) {
                if (TvRemoteService.this.mProviderList.contains(provider)) {
                    this.mService.openInputBridgeInternalLocked(provider, token, name, width, height, maxPointers);
                }
            }
        }

        public void closeInputBridge(TvRemoteProviderProxy provider, IBinder token) {
            synchronized (TvRemoteService.this.mLock) {
                if (TvRemoteService.this.mProviderList.contains(provider)) {
                    this.mService.closeInputBridgeInternalLocked(token);
                    TvRemoteService.this.mProviderMap.remove(token);
                }
            }
        }

        public void clearInputBridge(TvRemoteProviderProxy provider, IBinder token) {
            synchronized (TvRemoteService.this.mLock) {
                if (TvRemoteService.this.mProviderList.contains(provider)) {
                    this.mService.clearInputBridgeInternalLocked(token);
                }
            }
        }

        public void sendTimeStamp(TvRemoteProviderProxy provider, IBinder token, long timestamp) {
            synchronized (TvRemoteService.this.mLock) {
                if (TvRemoteService.this.mProviderList.contains(provider)) {
                    this.mService.sendTimeStampInternalLocked(token, timestamp);
                }
            }
        }

        public void sendKeyDown(TvRemoteProviderProxy provider, IBinder token, int keyCode) {
            synchronized (TvRemoteService.this.mLock) {
                if (TvRemoteService.this.mProviderList.contains(provider)) {
                    this.mService.sendKeyDownInternalLocked(token, keyCode);
                }
            }
        }

        public void sendKeyUp(TvRemoteProviderProxy provider, IBinder token, int keyCode) {
            synchronized (TvRemoteService.this.mLock) {
                if (TvRemoteService.this.mProviderList.contains(provider)) {
                    this.mService.sendKeyUpInternalLocked(token, keyCode);
                }
            }
        }

        public void sendPointerDown(TvRemoteProviderProxy provider, IBinder token, int pointerId, int x, int y) {
            synchronized (TvRemoteService.this.mLock) {
                if (TvRemoteService.this.mProviderList.contains(provider)) {
                    this.mService.sendPointerDownInternalLocked(token, pointerId, x, y);
                }
            }
        }

        public void sendPointerUp(TvRemoteProviderProxy provider, IBinder token, int pointerId) {
            synchronized (TvRemoteService.this.mLock) {
                if (TvRemoteService.this.mProviderList.contains(provider)) {
                    this.mService.sendPointerUpInternalLocked(token, pointerId);
                }
            }
        }

        public void sendPointerSync(TvRemoteProviderProxy provider, IBinder token) {
            synchronized (TvRemoteService.this.mLock) {
                if (TvRemoteService.this.mProviderList.contains(provider)) {
                    this.mService.sendPointerSyncInternalLocked(token);
                }
            }
        }

        public void addProvider(TvRemoteProviderProxy provider) {
            synchronized (TvRemoteService.this.mLock) {
                provider.setProviderSink(this);
                TvRemoteService.this.mProviderList.add(provider);
                Slog.d(TvRemoteService.TAG, "provider: " + provider.toString());
            }
        }

        public void removeProvider(TvRemoteProviderProxy provider) {
            synchronized (TvRemoteService.this.mLock) {
                if (!TvRemoteService.this.mProviderList.remove(provider)) {
                    Slog.e(TvRemoteService.TAG, "Unknown provider " + provider);
                }
            }
        }
    }

    public TvRemoteService(Context context) {
        super(context);
        this.mHandler = new UserHandler(new UserProvider(this), context);
        Watchdog.getInstance().addMonitor(this);
    }

    public void onStart() {
    }

    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    public void onBootPhase(int phase) {
        if (phase == 600) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    private void informInputBridgeConnected(IBinder token) {
        this.mHandler.obtainMessage(2, 0, 0, token).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void openInputBridgeInternalLocked(TvRemoteProviderProxy provider, IBinder token, String name, int width, int height, int maxPointers) {
        try {
            if (this.mBridgeMap.containsKey(token)) {
                informInputBridgeConnected(token);
                return;
            }
            UinputBridge uinputBridge = new UinputBridge(token, name, width, height, maxPointers);
            this.mBridgeMap.put(token, uinputBridge);
            this.mProviderMap.put(token, provider);
            informInputBridgeConnected(token);
        } catch (IOException e) {
            Slog.e(TAG, "Cannot create device for " + name);
        }
    }

    /* access modifiers changed from: private */
    public void closeInputBridgeInternalLocked(IBinder token) {
        UinputBridge inputBridge = this.mBridgeMap.get(token);
        if (inputBridge != null) {
            inputBridge.close(token);
        }
        this.mBridgeMap.remove(token);
    }

    /* access modifiers changed from: private */
    public void clearInputBridgeInternalLocked(IBinder token) {
        UinputBridge inputBridge = this.mBridgeMap.get(token);
        if (inputBridge != null) {
            inputBridge.clear(token);
        }
    }

    /* access modifiers changed from: private */
    public void sendTimeStampInternalLocked(IBinder token, long timestamp) {
        UinputBridge inputBridge = this.mBridgeMap.get(token);
        if (inputBridge != null) {
            inputBridge.sendTimestamp(token, timestamp);
        }
    }

    /* access modifiers changed from: private */
    public void sendKeyDownInternalLocked(IBinder token, int keyCode) {
        UinputBridge inputBridge = this.mBridgeMap.get(token);
        if (inputBridge != null) {
            inputBridge.sendKeyDown(token, keyCode);
        }
    }

    /* access modifiers changed from: private */
    public void sendKeyUpInternalLocked(IBinder token, int keyCode) {
        UinputBridge inputBridge = this.mBridgeMap.get(token);
        if (inputBridge != null) {
            inputBridge.sendKeyUp(token, keyCode);
        }
    }

    /* access modifiers changed from: private */
    public void sendPointerDownInternalLocked(IBinder token, int pointerId, int x, int y) {
        UinputBridge inputBridge = this.mBridgeMap.get(token);
        if (inputBridge != null) {
            inputBridge.sendPointerDown(token, pointerId, x, y);
        }
    }

    /* access modifiers changed from: private */
    public void sendPointerUpInternalLocked(IBinder token, int pointerId) {
        UinputBridge inputBridge = this.mBridgeMap.get(token);
        if (inputBridge != null) {
            inputBridge.sendPointerUp(token, pointerId);
        }
    }

    /* access modifiers changed from: private */
    public void sendPointerSyncInternalLocked(IBinder token) {
        UinputBridge inputBridge = this.mBridgeMap.get(token);
        if (inputBridge != null) {
            inputBridge.sendPointerSync(token);
        }
    }
}
