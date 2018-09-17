package org.simalliance.openmobileapi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import org.simalliance.openmobileapi.service.CardException;
import org.simalliance.openmobileapi.service.ISmartcardService;
import org.simalliance.openmobileapi.service.ISmartcardServiceCallback;
import org.simalliance.openmobileapi.service.ISmartcardServiceCallback.Stub;
import org.simalliance.openmobileapi.service.ISmartcardServiceReader;
import org.simalliance.openmobileapi.service.SmartcardError;

public class SEService {
    private static final String API_VERSION = "2.05";
    private static final String SERVICE_TAG = "SEService";
    private static final String SMARTCARD_SERVICE_UICC_TERMINAL = "SIM";
    private final ISmartcardServiceCallback mCallback = new Stub() {
    };
    private CallBack mCallerCallback;
    private ServiceConnection mConnection;
    private final Context mContext;
    private final Object mLock = new Object();
    private final HashMap<String, Reader> mReaders = new HashMap();
    private volatile ISmartcardService mSmartcardService;

    public interface CallBack {
        void serviceConnected(SEService sEService);
    }

    public SEService(Context context, CallBack listener) {
        Log.d(SERVICE_TAG, "Enter SEService");
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        this.mContext = context;
        this.mCallerCallback = listener;
        this.mConnection = new ServiceConnection() {
            public synchronized void onServiceConnected(ComponentName className, IBinder service) {
                SEService.this.mSmartcardService = ISmartcardService.Stub.asInterface(service);
                if (SEService.this.mCallerCallback != null) {
                    SEService.this.mCallerCallback.serviceConnected(SEService.this);
                }
                Log.v(SEService.SERVICE_TAG, "Service onServiceConnected");
            }

            public void onServiceDisconnected(ComponentName className) {
                SEService.this.mSmartcardService = null;
                Log.v(SEService.SERVICE_TAG, "Service onServiceDisconnected");
            }
        };
        Intent startIntent = new Intent(ISmartcardService.class.getName());
        startIntent.setComponent(startIntent.resolveSystemService(context.getPackageManager(), 0));
        if (this.mContext.bindService(startIntent, this.mConnection, 1)) {
            Log.v(SERVICE_TAG, "bindService successful");
        }
    }

    public boolean isConnected() {
        if (this.mSmartcardService != null) {
            return true;
        }
        Log.v(SERVICE_TAG, "Service is not connected");
        return false;
    }

    public String getVersion() {
        return API_VERSION;
    }

    public Reader[] getReaders() {
        if (this.mSmartcardService == null) {
            throw new IllegalStateException("service not connected to system");
        }
        try {
            String[] readerNames = this.mSmartcardService.getReaders(new SmartcardError());
            this.mReaders.clear();
            for (String readerName : readerNames) {
                this.mReaders.put(readerName, new Reader(this, readerName));
            }
            return sortReaders();
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    public void shutdown() {
        Log.d(SERVICE_TAG, "shutdown");
        synchronized (this.mLock) {
            if (this.mSmartcardService != null) {
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
            this.mSmartcardService = null;
        }
    }

    ISmartcardServiceReader getReader(String name) {
        SmartcardError error = new SmartcardError();
        try {
            ISmartcardServiceReader reader = this.mSmartcardService.getReader(name, error);
            checkForException(error);
            return reader;
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    static void checkForException(SmartcardError error) {
        try {
            error.throwException();
        } catch (CardException exp) {
            throw new IllegalStateException(exp.getMessage());
        } catch (AccessControlException exp2) {
            throw new SecurityException(exp2.getMessage());
        }
    }

    ISmartcardServiceCallback getCallback() {
        return this.mCallback;
    }

    private Reader[] sortReaders() {
        ArrayList<Reader> readersList = new ArrayList();
        Reader reader = (Reader) this.mReaders.get(SMARTCARD_SERVICE_UICC_TERMINAL);
        if (reader != null) {
            readersList.add(reader);
        }
        for (Reader r : this.mReaders.values()) {
            if (!readersList.contains(r)) {
                readersList.add(r);
            }
        }
        return (Reader[]) readersList.toArray(new Reader[readersList.size()]);
    }
}
