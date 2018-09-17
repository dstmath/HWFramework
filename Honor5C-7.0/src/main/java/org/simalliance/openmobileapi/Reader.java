package org.simalliance.openmobileapi;

import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import org.simalliance.openmobileapi.service.ISmartcardServiceReader;
import org.simalliance.openmobileapi.service.ISmartcardServiceSession;
import org.simalliance.openmobileapi.service.SmartcardError;

public class Reader {
    private static final String TAG = "Reader";
    private final Object mLock;
    private final String mName;
    private ISmartcardServiceReader mReader;
    private final SEService mService;

    Reader(SEService service, String name) {
        this.mLock = new Object();
        this.mName = name;
        this.mService = service;
        this.mReader = null;
    }

    public String getName() {
        return this.mName;
    }

    public Session openSession() throws IOException {
        Log.d(TAG, "Reader to openSession");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service is not connected");
        }
        Session session;
        if (this.mReader == null) {
            try {
                this.mReader = this.mService.getReader(this.mName);
            } catch (Exception e) {
                throw new IOException("service reader cannot be accessed.");
            }
        }
        synchronized (this.mLock) {
            SmartcardError error = new SmartcardError();
            ISmartcardServiceSession iSmartcardServiceSession = null;
            try {
                if (this.mReader != null) {
                    if (this.mReader.isSecureElementPresent(error)) {
                        iSmartcardServiceSession = this.mReader.openSession(error);
                    } else {
                        throw new IOException("Secure Element is not presented.");
                    }
                }
                SEService.checkForException(error);
                if (iSmartcardServiceSession == null) {
                    throw new IOException("service session is null.");
                }
                session = new Session(this.mService, iSmartcardServiceSession, this);
            } catch (RemoteException e2) {
                throw new IOException(e2.getMessage());
            }
        }
        return session;
    }

    public boolean isSecureElementPresent() {
        Log.d(TAG, "Reader to isSecureElementPresent");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service is not connected");
        }
        if (this.mReader == null) {
            try {
                this.mReader = this.mService.getReader(this.mName);
            } catch (Exception e) {
                throw new IllegalStateException("service reader cannot be accessed. " + e.getLocalizedMessage());
            }
        }
        SmartcardError error = new SmartcardError();
        boolean flag = false;
        try {
            if (this.mReader != null) {
                flag = this.mReader.isSecureElementPresent(error);
            }
            SEService.checkForException(error);
            return flag;
        } catch (RemoteException e2) {
            throw new IllegalStateException(e2.getMessage());
        }
    }

    public SEService getSEService() {
        Log.d(TAG, "Reader to getSEService");
        return this.mService;
    }

    public void closeSessions() {
        Log.d(TAG, "Reader to closeSessions");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service is not connected");
        } else if (this.mReader != null) {
            synchronized (this.mLock) {
                SmartcardError error = new SmartcardError();
                try {
                    this.mReader.closeSessions(error);
                    SEService.checkForException(error);
                } catch (RemoteException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
        }
    }
}
