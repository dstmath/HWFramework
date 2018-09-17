package org.simalliance.openmobileapi;

import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import org.simalliance.openmobileapi.service.ISmartcardServiceChannel;
import org.simalliance.openmobileapi.service.SmartcardError;

public class Channel {
    private static final String TAG = "Channel";
    private final ISmartcardServiceChannel mChannel;
    private final Object mLock = new Object();
    private final SEService mService;
    private Session mSession;

    Channel(SEService service, Session session, ISmartcardServiceChannel channel) {
        this.mService = service;
        this.mSession = session;
        this.mChannel = channel;
    }

    public void close() {
        Log.d(TAG, "Channel to close");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else {
            SmartcardError error = new SmartcardError();
            try {
                this.mChannel.close(error);
                SEService.checkForException(error);
            } catch (RemoteException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    public boolean isClosed() {
        Log.d(TAG, "Channel to isClosed");
        if (this.mService == null || !this.mService.isConnected()) {
            return true;
        }
        if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        }
        try {
            return this.mChannel.isClosed();
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public boolean isBasicChannel() {
        Log.d(TAG, "Channel to isBasicChannel");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else {
            try {
                return this.mChannel.isBasicChannel();
            } catch (RemoteException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    public byte[] transmit(byte[] command) throws IOException {
        Log.d(TAG, "Channel to transmit");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else {
            byte[] response;
            synchronized (this.mLock) {
                SmartcardError error = new SmartcardError();
                try {
                    response = this.mChannel.transmit(command, error);
                    checkUnsupportedOperation(error);
                    SEService.checkForException(error);
                } catch (RemoteException e) {
                    throw new IllegalStateException(e.getMessage());
                } catch (Exception e2) {
                    throw new IOException(e2.getMessage());
                }
            }
            return response;
        }
    }

    public Session getSession() {
        return this.mSession;
    }

    public byte[] getSelectResponse() {
        Log.d(TAG, "Channel to getSelectResponse");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else {
            try {
                if (this.mChannel.isClosed()) {
                    throw new IllegalStateException("channel is closed");
                }
                try {
                    byte[] response = this.mChannel.getSelectResponse();
                    if (response == null || response.length != 0) {
                        return response;
                    }
                    return null;
                } catch (RemoteException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            } catch (RemoteException e1) {
                throw new IllegalStateException(e1.getMessage());
            }
        }
    }

    public boolean selectNext() throws IOException {
        Log.d(TAG, "Channel to selectNext");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else {
            try {
                if (this.mChannel.isClosed()) {
                    throw new IllegalStateException("channel is closed");
                }
                boolean response;
                synchronized (this.mLock) {
                    SmartcardError error = new SmartcardError();
                    try {
                        response = this.mChannel.selectNext(error);
                        checkUnsupportedOperation(error);
                        SEService.checkForException(error);
                    } catch (RemoteException e1) {
                        throw new IllegalStateException(e1.getMessage());
                    } catch (Exception e) {
                        throw new IOException(e.getMessage());
                    }
                }
                return response;
            } catch (RemoteException e12) {
                throw new IllegalStateException(e12.getMessage());
            }
        }
    }

    private void checkUnsupportedOperation(SmartcardError error) throws IOException {
        Exception exp = error.createException();
        if (exp != null && (exp instanceof UnsupportedOperationException)) {
            String msg = exp.getMessage();
            if (msg != null && msg.contains("IOError")) {
                Log.d(TAG, "checkUnsupportedOperation contains IOError");
                throw new IOException("IOError");
            }
        }
    }
}
