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
    private CallBack mCallerCallback;
    private final Context mContext;
    private final Object mLock = new Object();
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
                    /* class org.simalliance.openmobileapi.SEService.AnonymousClass1 */

                    @Override // android.se.omapi.SEService.OnConnectedListener
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
        android.se.omapi.SEService sEService = this.mService;
        if (sEService != null) {
            return sEService.isConnected();
        }
        Log.v(SERVICE_TAG, "Service is null");
        throw new IllegalStateException("service not connected to system");
    }

    public String getVersion() {
        android.se.omapi.SEService sEService = this.mService;
        if (sEService != null) {
            return sEService.getVersion();
        }
        Log.v(SERVICE_TAG, "Service is null");
        throw new IllegalStateException("service not connected to system");
    }

    public Reader[] getReaders() {
        Log.d(SERVICE_TAG, "getReaders");
        android.se.omapi.SEService sEService = this.mService;
        if (sEService != null) {
            Reader[] readers = sEService.getReaders();
            for (Reader reader : readers) {
                this.mReaders.put(reader.getName(), new Reader(this, reader.getName()));
            }
            return sortReaders();
        }
        Log.v(SERVICE_TAG, "Service is null");
        throw new IllegalStateException("service not connected to system");
    }

    public void shutdown() {
        Log.d(SERVICE_TAG, "shutdown");
        android.se.omapi.SEService sEService = this.mService;
        if (sEService != null) {
            sEService.shutdown();
        } else {
            Log.v(SERVICE_TAG, "Service is null");
            throw new IllegalStateException("service not connected to system");
        }
    }

    /* access modifiers changed from: package-private */
    public Reader getReader(String name) {
        android.se.omapi.SEService sEService = this.mService;
        if (sEService != null) {
            Reader[] readers = sEService.getReaders();
            for (Reader reader : readers) {
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
        if (reader != null) {
            readersList.add(reader);
        }
        Reader reader2 = this.mReaders.get(SMARTCARD_SERVICE_ESE_TERMINAL);
        if (reader2 != null) {
            Log.d(SERVICE_TAG, "sortReaders : confirm eSE Reader be the 2nd!");
            readersList.add(reader2);
        }
        for (Reader r : this.mReaders.values()) {
            if (!readersList.contains(r)) {
                readersList.add(r);
            }
        }
        return (Reader[]) readersList.toArray(new Reader[readersList.size()]);
    }
}
