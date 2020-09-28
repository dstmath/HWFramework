package android.se.omapi;

import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Log;
import java.io.IOException;
import java.util.NoSuchElementException;

public final class Session {
    private static final String TAG = "OMAPI.Session";
    private final Object mLock = new Object();
    private final Reader mReader;
    private final SEService mService;
    private final ISecureElementSession mSession;

    Session(SEService service, ISecureElementSession session, Reader reader) {
        if (service == null || reader == null || session == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        this.mService = service;
        this.mReader = reader;
        this.mSession = session;
    }

    public Reader getReader() {
        return this.mReader;
    }

    public byte[] getATR() {
        if (this.mService.isConnected()) {
            try {
                return this.mSession.getAtr();
            } catch (RemoteException e) {
                throw new IllegalStateException(e.getMessage());
            }
        } else {
            throw new IllegalStateException("service not connected to system");
        }
    }

    public void close() {
        if (!this.mService.isConnected()) {
            Log.e(TAG, "service not connected to system");
            return;
        }
        synchronized (this.mLock) {
            try {
                this.mSession.close();
            } catch (RemoteException e) {
                Log.e(TAG, "Error closing session", e);
            }
        }
    }

    public boolean isClosed() {
        try {
            return this.mSession.isClosed();
        } catch (RemoteException e) {
            return true;
        }
    }

    public void closeChannels() {
        if (!this.mService.isConnected()) {
            Log.e(TAG, "service not connected to system");
            return;
        }
        synchronized (this.mLock) {
            try {
                this.mSession.closeChannels();
            } catch (RemoteException e) {
                Log.e(TAG, "Error closing channels", e);
            }
        }
    }

    public Channel openBasicChannel(byte[] aid, byte p2) throws IOException {
        if (this.mService.isConnected()) {
            synchronized (this.mLock) {
                try {
                    ISecureElementChannel channel = this.mSession.openBasicChannel(aid, p2, this.mReader.getSEService().getListener());
                    if (channel == null) {
                        return null;
                    }
                    return new Channel(this.mService, this, channel);
                } catch (ServiceSpecificException e) {
                    if (e.errorCode == 1) {
                        throw new IOException(e.getMessage());
                    } else if (e.errorCode == 2) {
                        throw new NoSuchElementException(e.getMessage());
                    } else {
                        throw new IllegalStateException(e.getMessage());
                    }
                } catch (RemoteException e2) {
                    throw new IllegalStateException(e2.getMessage());
                }
            }
        } else {
            throw new IllegalStateException("service not connected to system");
        }
    }

    public Channel openBasicChannel(byte[] aid) throws IOException {
        return openBasicChannel(aid, (byte) 0);
    }

    public Channel openLogicalChannel(byte[] aid, byte p2) throws IOException {
        if (this.mService.isConnected()) {
            synchronized (this.mLock) {
                try {
                    ISecureElementChannel channel = this.mSession.openLogicalChannel(aid, p2, this.mReader.getSEService().getListener());
                    if (channel == null) {
                        return null;
                    }
                    return new Channel(this.mService, this, channel);
                } catch (ServiceSpecificException e) {
                    if (e.errorCode == 1) {
                        throw new IOException(e.getMessage());
                    } else if (e.errorCode == 2) {
                        throw new NoSuchElementException(e.getMessage());
                    } else {
                        throw new IllegalStateException(e.getMessage());
                    }
                } catch (RemoteException e2) {
                    throw new IllegalStateException(e2.getMessage());
                }
            }
        } else {
            throw new IllegalStateException("service not connected to system");
        }
    }

    public Channel openLogicalChannel(byte[] aid) throws IOException {
        return openLogicalChannel(aid, Byte.MAX_VALUE);
    }
}
