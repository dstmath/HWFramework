package org.simalliance.openmobileapi;

import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import org.simalliance.openmobileapi.service.ISmartcardServiceChannel;
import org.simalliance.openmobileapi.service.ISmartcardServiceSession;
import org.simalliance.openmobileapi.service.SmartcardError;

public class Session {
    private static final byte P2_00 = (byte) 0;
    private static final byte P2_04 = (byte) 4;
    private static final byte P2_08 = (byte) 8;
    private static final byte P2_0C = (byte) 12;
    private static final String TAG = "Session";
    private final Object mLock = new Object();
    private final Reader mReader;
    private final SEService mService;
    private final ISmartcardServiceSession mSession;

    Session(SEService service, ISmartcardServiceSession session, Reader reader) {
        this.mService = service;
        this.mReader = reader;
        this.mSession = session;
    }

    public Channel openBasicChannel(byte[] aid, byte P2) throws IOException {
        Log.d(TAG, "Session to openBasicChannel P2" + P2);
        return openBasicChannel(aid);
    }

    /* JADX WARNING: Missing block: B:43:0x008a, code:
            if (r10.length == 0) goto L_0x008c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Channel openBasicChannel(byte[] aid) throws IOException {
        Log.d(TAG, "Session to openBasicChannel");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession == null) {
            throw new IllegalStateException("service session is null");
        } else if (getReader() == null) {
            throw new IllegalStateException("reader must not be null");
        } else {
            synchronized (this.mLock) {
                SmartcardError error = new SmartcardError();
                try {
                    ISmartcardServiceChannel channel = this.mSession.openBasicChannelAid(aid, this.mService.getCallback(), error);
                    SEService.checkForException(error);
                    error.clear();
                    boolean b = basicChannelInUse(error);
                    SEService.checkForException(error);
                    if (b) {
                        return null;
                    }
                    error.clear();
                    b = channelCannotBeEstablished(error);
                    SEService.checkForException(error);
                    if (b) {
                        return null;
                    }
                    if (aid != null) {
                    }
                    error.clear();
                    b = isDefaultApplicationSelected(error);
                    SEService.checkForException(error);
                    if (!b) {
                        return null;
                    }
                    error.clear();
                    checkIfAppletAvailable(error);
                    SEService.checkForException(error);
                    if (channel == null) {
                        return null;
                    }
                    Channel channel2 = new Channel(this.mService, this, channel);
                    return channel2;
                } catch (RemoteException e) {
                    throw new IllegalStateException(e.getMessage());
                } catch (Exception e2) {
                    throw new IOException(e2.getMessage());
                }
            }
        }
    }

    public Channel openLogicalChannel(byte[] aid) throws IOException, IllegalStateException, IllegalArgumentException, SecurityException, NoSuchElementException, UnsupportedOperationException {
        return openLogicalChannel(aid, P2_00);
    }

    /* JADX WARNING: Missing block: B:32:0x0079, code:
            if (r10.length == 0) goto L_0x007b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Channel openLogicalChannel(byte[] aid, byte p2) throws IOException, IllegalStateException, IllegalArgumentException, SecurityException, NoSuchElementException, UnsupportedOperationException {
        Log.d(TAG, "Session to openLogicalChannel P2:" + p2);
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession == null) {
            throw new IllegalStateException("service session is null");
        } else if (getReader() == null) {
            throw new IllegalStateException("reader must not be null");
        } else if (p2 == (byte) 0 || p2 == P2_04 || p2 == P2_08 || p2 == P2_0C) {
            synchronized (this.mLock) {
                SmartcardError error = new SmartcardError();
                try {
                    ISmartcardServiceChannel channel = this.mSession.openLogicalChannelWithP2(aid, p2, this.mService.getCallback(), error);
                    if (aid != null) {
                    }
                    if (checkUnsupportedOperation(error)) {
                        return null;
                    }
                    if (checkMissingResourceException(error)) {
                        return null;
                    }
                    SEService.checkForException(error);
                    error.clear();
                    boolean b = channelCannotBeEstablished(error);
                    SEService.checkForException(error);
                    if (b) {
                        return null;
                    }
                    error.clear();
                    checkIfAppletAvailable(error);
                    SEService.checkForException(error);
                    if (channel == null) {
                        return null;
                    }
                    Channel channel2 = new Channel(this.mService, this, channel);
                    return channel2;
                } catch (RemoteException e) {
                    throw new IllegalStateException(e.getMessage());
                } catch (Exception e2) {
                    throw new IOException(e2.getMessage());
                }
            }
        } else {
            throw new IllegalStateException("P2 Error");
        }
    }

    private boolean checkUnsupportedOperation(SmartcardError error) {
        Exception exp = error.createException();
        if (exp != null && (exp instanceof UnsupportedOperationException)) {
            String msg = exp.getMessage();
            if (msg != null && msg.contains("open channel without select AID is not supported by UICC")) {
                Log.d(TAG, "checkUnsupportedOperation open channel without select AID is not supported by UICC");
                return true;
            }
        }
        return false;
    }

    private boolean checkMissingResourceException(SmartcardError error) {
        Exception exp = error.createException();
        if (exp != null && ((exp instanceof MissingResourceException) || (exp instanceof AccessControlException))) {
            String msg = exp.getMessage();
            if (msg != null && (msg.contains("out of channels") || msg.contains("all channels are used"))) {
                Log.d(TAG, "out of channels");
                return true;
            }
        }
        return false;
    }

    public void close() {
        Log.d(TAG, "Session to close");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession != null) {
            synchronized (this.mLock) {
                SmartcardError error = new SmartcardError();
                try {
                    this.mSession.close(error);
                    SEService.checkForException(error);
                } catch (RemoteException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
        }
    }

    public byte[] getATR() {
        Log.d(TAG, "Session to getATR");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession == null) {
            throw new IllegalStateException("service session is null");
        } else {
            try {
                return this.mSession.getAtr();
            } catch (RemoteException e) {
                throw new IllegalStateException(e.getMessage());
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public boolean isClosed() {
        Log.d(TAG, "Session to isClosed");
        try {
            if (this.mSession == null) {
                return true;
            }
            return this.mSession.isClosed();
        } catch (RemoteException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void closeChannels() {
        Log.d(TAG, "Session to closeChannels");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession != null) {
            synchronized (this.mLock) {
                SmartcardError error = new SmartcardError();
                try {
                    this.mSession.closeChannels(error);
                    SEService.checkForException(error);
                } catch (RemoteException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
        }
    }

    public Reader getReader() {
        return this.mReader;
    }

    private boolean isDefaultApplicationSelected(SmartcardError error) {
        Exception exp = error.createException();
        if (exp != null) {
            String msg = exp.getMessage();
            if (msg != null && msg.contains("default application is not selected")) {
                return false;
            }
        }
        return true;
    }

    private boolean basicChannelInUse(SmartcardError error) {
        Exception exp = error.createException();
        if (exp != null) {
            String msg = exp.getMessage();
            if (msg != null && msg.contains("basic channel in use")) {
                return true;
            }
        }
        return false;
    }

    private boolean channelCannotBeEstablished(SmartcardError error) {
        Exception exp = error.createException();
        if (exp != null) {
            if (exp instanceof MissingResourceException) {
                return true;
            }
            String msg = exp.getMessage();
            if (msg != null && (msg.contains("channel in use") || msg.contains("open channel failed") || msg.contains("out of channels") || msg.contains("MANAGE CHANNEL"))) {
                return true;
            }
        }
        return false;
    }

    private void checkIfAppletAvailable(SmartcardError error) throws NoSuchElementException {
        Exception exp = error.createException();
        if (exp != null && (exp instanceof NoSuchElementException)) {
            throw new NoSuchElementException("Applet with the defined aid does not exist in the SE");
        }
    }
}
