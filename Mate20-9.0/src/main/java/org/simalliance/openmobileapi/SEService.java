package org.simalliance.openmobileapi;

import android.content.Context;
import android.se.omapi.Reader;
import android.se.omapi.SEService;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

public class SEService {
    private static final String SERVICE_TAG = "SIMalliance.OMAPI.SEService";
    private static final String SMARTCARD_SERVICE_ESE_TERMINAL = "eSE";
    private static final String SMARTCARD_SERVICE_UICC_TERMINAL = "SIM1";
    /* access modifiers changed from: private */
    public CallBack mCallerCallback;
    private final Context mContext;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final HashMap<String, Reader> mReaders = new HashMap<>();
    private android.se.omapi.SEService mService;

    public interface CallBack {
        void serviceConnected(SEService sEService);
    }

    public SEService(Context context, CallBack listener) {
        Log.d(SERVICE_TAG, "Enter SEService");
        if (context != null) {
            this.mContext = context;
            this.mCallerCallback = listener;
            synchronized (this.mLock) {
                this.mService = new android.se.omapi.SEService(this.mContext, Executors.newSingleThreadExecutor(), new SEService.OnConnectedListener() {
                    public void onConnected() {
                        synchronized (SEService.this.mLock) {
                            if (SEService.this.mCallerCallback != null) {
                                SEService.this.mCallerCallback.serviceConnected(SEService.this);
                            }
                        }
                    }
                });
            }
            return;
        }
        throw new NullPointerException("context must not be null");
    }

    public boolean isConnected() {
        if (this.mService != null) {
            return this.mService.isConnected();
        }
        Log.v(SERVICE_TAG, "Service is null");
        throw new IllegalStateException("service not connected to system");
    }

    public String getVersion() {
        if (this.mService != null) {
            return this.mService.getVersion();
        }
        Log.v(SERVICE_TAG, "Service is null");
        throw new IllegalStateException("service not connected to system");
    }

    public Reader[] getReaders() {
        Log.d(SERVICE_TAG, "getReaders");
        if (this.mService != null) {
            for (Reader reader : this.mService.getReaders()) {
                this.mReaders.put(reader.getName(), new Reader(this, reader.getName()));
            }
            return sortReaders();
        }
        Log.v(SERVICE_TAG, "Service is null");
        throw new IllegalStateException("service not connected to system");
    }

    public void shutdown() {
        Log.d(SERVICE_TAG, "shutdown");
        if (this.mService != null) {
            this.mService.shutdown();
        } else {
            Log.v(SERVICE_TAG, "Service is null");
            throw new IllegalStateException("service not connected to system");
        }
    }

    /* access modifiers changed from: package-private */
    public Reader getReader(String name) {
        if (this.mService != null) {
            for (Reader reader : this.mService.getReaders()) {
                if (reader.getName().equals(name)) {
                    return reader;
                }
            }
            return null;
        }
        Log.v(SERVICE_TAG, "Service is null");
        throw new IllegalStateException("service not connected to system");
    }

    private Reader[] sortReaders() {
        Log.d(SERVICE_TAG, "sortReaders");
        ArrayList<Reader> readersList = new ArrayList<>();
        Reader reader = this.mReaders.get(SMARTCARD_SERVICE_UICC_TERMINAL);
        Reader reader2 = reader;
        if (reader != null) {
            readersList.add(reader2);
        }
        Reader reader3 = this.mReaders.get(SMARTCARD_SERVICE_ESE_TERMINAL);
        Reader reader4 = reader3;
        if (reader3 != null) {
            Log.d(SERVICE_TAG, "sortReaders : confirm eSE Reader be the 2nd!");
            readersList.add(reader4);
        }
        for (Reader r : this.mReaders.values()) {
            if (!readersList.contains(r)) {
                readersList.add(r);
            }
        }
        return (Reader[]) readersList.toArray(new Reader[readersList.size()]);
    }
}
