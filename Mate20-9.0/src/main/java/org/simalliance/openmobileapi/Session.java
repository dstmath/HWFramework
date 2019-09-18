package org.simalliance.openmobileapi;

import android.os.SystemProperties;
import android.se.omapi.Channel;
import android.util.Log;
import java.io.IOException;
import java.util.NoSuchElementException;

public class Session {
    private static final boolean NFC_FELICA = SystemProperties.getBoolean("ro.config.has_felica_feature", NFC_FELICA);
    private static final byte P2_00 = 0;
    private static final byte P2_04 = 4;
    private static final byte P2_08 = 8;
    private static final byte P2_0C = 12;
    private static final String TAG = "SIMalliance.OMAPI.Session";
    private final Object mLock = new Object();
    private final Reader mReader;
    private final SEService mService;
    private final android.se.omapi.Session mSession;

    Session(SEService service, android.se.omapi.Session session, Reader reader) {
        this.mService = service;
        this.mReader = reader;
        this.mSession = session;
    }

    public Channel openBasicChannel(byte[] aid, byte P2) throws IOException {
        Log.d(TAG, "Session to openBasicChannel P2" + P2);
        if (this.mSession != null) {
            Channel channel = this.mSession.openBasicChannel(aid, P2);
            if (channel == null) {
                return null;
            }
            return new Channel(this.mService, this, channel);
        }
        throw new IllegalStateException("service session is null");
    }

    public Channel openBasicChannel(byte[] aid) throws IOException {
        Log.d(TAG, "Session to openBasicChannel");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession == null) {
            throw new IllegalStateException("service session is null");
        } else if (getReader() == null) {
            throw new IllegalStateException("reader must not be null");
        } else if (NFC_FELICA) {
            return null;
        } else {
            Channel channel = this.mSession.openBasicChannel(aid);
            if (channel == null) {
                return null;
            }
            return new Channel(this.mService, this, channel);
        }
    }

    public Channel openLogicalChannel(byte[] aid) throws IOException, IllegalStateException, IllegalArgumentException, SecurityException, NoSuchElementException, UnsupportedOperationException {
        Channel channel = this.mSession.openLogicalChannel(aid);
        if (channel == null) {
            return null;
        }
        return new Channel(this.mService, this, channel);
    }

    public Channel openLogicalChannel(byte[] aid, byte p2) throws IOException, IllegalStateException, IllegalArgumentException, SecurityException, NoSuchElementException, UnsupportedOperationException {
        Log.d(TAG, "Session to openLogicalChannel P2:" + p2);
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession == null) {
            throw new IllegalStateException("service session is null");
        } else if (getReader() == null) {
            throw new IllegalStateException("reader must not be null");
        } else if (p2 == 0 || p2 == 4 || p2 == 8 || p2 == 12) {
            Channel channel = this.mSession.openLogicalChannel(aid, p2);
            if (channel == null) {
                return null;
            }
            return new Channel(this.mService, this, channel);
        } else {
            throw new IllegalStateException("P2 Error");
        }
    }

    public void close() {
        Log.d(TAG, "Session to close");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession != null) {
            this.mSession.close();
        }
    }

    public byte[] getATR() {
        Log.d(TAG, "Session to getATR");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession == null) {
            throw new IllegalStateException("service session is null");
        } else if (NFC_FELICA) {
            return null;
        } else {
            return this.mSession.getATR();
        }
    }

    public boolean isClosed() {
        Log.d(TAG, "Session to isClosed");
        if (this.mSession == null) {
            return true;
        }
        return this.mSession.isClosed();
    }

    public void closeChannels() {
        Log.d(TAG, "Session to closeChannels");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession != null) {
            this.mSession.closeChannels();
        }
    }

    public Reader getReader() {
        return this.mReader;
    }
}
