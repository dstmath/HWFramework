package android.se.omapi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.se.omapi.ISecureElementListener;
import android.se.omapi.ISecureElementService;
import android.util.Log;
import java.lang.annotation.RCUnownedThisRef;
import java.util.HashMap;
import java.util.concurrent.Executor;

public final class SEService {
    public static final int IO_ERROR = 1;
    public static final int NO_SUCH_ELEMENT_ERROR = 2;
    private static final String TAG = "OMAPI.SEService";
    private ServiceConnection mConnection;
    private final Context mContext;
    private final Object mLock = new Object();
    private final HashMap<String, Reader> mReaders = new HashMap<>();
    /* access modifiers changed from: private */
    public SEListener mSEListener = new SEListener();
    /* access modifiers changed from: private */
    public volatile ISecureElementService mSecureElementService;

    public interface OnConnectedListener {
        void onConnected();
    }

    private static class SEListener extends ISecureElementListener.Stub {
        public Executor mExecutor;
        public OnConnectedListener mListener;

        private SEListener() {
            this.mListener = null;
            this.mExecutor = null;
        }

        public IBinder asBinder() {
            return this;
        }

        public void onConnected() {
            if (this.mListener != null && this.mExecutor != null) {
                this.mExecutor.execute(new Runnable() {
                    public void run() {
                        SEListener.this.mListener.onConnected();
                    }
                });
            }
        }
    }

    public SEService(Context context, Executor executor, OnConnectedListener listener) {
        if (context == null || listener == null || executor == null) {
            throw new NullPointerException("Arguments must not be null");
        }
        this.mContext = context;
        this.mSEListener.mListener = listener;
        this.mSEListener.mExecutor = executor;
        this.mConnection = new ServiceConnection() {
            @RCUnownedThisRef
            public synchronized void onServiceConnected(ComponentName className, IBinder service) {
                ISecureElementService unused = SEService.this.mSecureElementService = ISecureElementService.Stub.asInterface(service);
                if (SEService.this.mSEListener != null) {
                    SEService.this.mSEListener.onConnected();
                }
                Log.i(SEService.TAG, "Service onServiceConnected");
            }

            public void onServiceDisconnected(ComponentName className) {
                ISecureElementService unused = SEService.this.mSecureElementService = null;
                Log.i(SEService.TAG, "Service onServiceDisconnected");
            }
        };
        Intent intent = new Intent(ISecureElementService.class.getName());
        intent.setClassName("com.android.se", "com.android.se.SecureElementService");
        if (this.mContext.bindService(intent, this.mConnection, 1)) {
            Log.i(TAG, "bindService successful");
        }
    }

    public boolean isConnected() {
        return this.mSecureElementService != null;
    }

    public Reader[] getReaders() {
        int i;
        if (this.mSecureElementService != null) {
            try {
                String[] readerNames = this.mSecureElementService.getReaders();
                Reader[] readers = new Reader[readerNames.length];
                int i2 = 0;
                for (String readerName : readerNames) {
                    if (this.mReaders.get(readerName) == null) {
                        try {
                            this.mReaders.put(readerName, new Reader(this, readerName, getReader(readerName)));
                            i = i2 + 1;
                            try {
                                readers[i2] = this.mReaders.get(readerName);
                            } catch (Exception e) {
                                int i3 = i;
                                e = e;
                                i2 = i3;
                            }
                        } catch (Exception e2) {
                            e = e2;
                            Log.e(TAG, "Error adding Reader: " + readerName, e);
                        }
                    } else {
                        i = i2 + 1;
                        readers[i2] = this.mReaders.get(readerName);
                    }
                    i2 = i;
                }
                return readers;
            } catch (RemoteException e3) {
                throw new RuntimeException(e3);
            }
        } else {
            throw new IllegalStateException("service not connected to system");
        }
    }

    public void shutdown() {
        synchronized (this.mLock) {
            if (this.mSecureElementService != null) {
                for (Reader reader : this.mReaders.values()) {
                    try {
                        reader.closeSessions();
                    } catch (Exception e) {
                    }
                }
            }
            try {
                this.mContext.unbindService(this.mConnection);
            } catch (IllegalArgumentException e2) {
            }
            this.mSecureElementService = null;
        }
    }

    public String getVersion() {
        return "3.3";
    }

    /* access modifiers changed from: package-private */
    public ISecureElementListener getListener() {
        return this.mSEListener;
    }

    private ISecureElementReader getReader(String name) {
        try {
            return this.mSecureElementService.getReader(name);
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
