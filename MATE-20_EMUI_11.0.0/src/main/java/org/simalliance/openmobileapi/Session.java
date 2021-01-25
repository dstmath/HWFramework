package org.simalliance.openmobileapi;

import android.os.SystemProperties;
import android.se.omapi.Channel;
import android.util.Log;
import java.io.IOException;
import java.util.NoSuchElementException;

public class Session {
    private static final boolean NFC_FELICA = SystemProperties.getBoolean("ro.config.has_felica_feature", (boolean) NFC_FELICA);
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
        Log.d(TAG, "Session to openBasicChannel P2" + ((int) P2));
        android.se.omapi.Session session = this.mSession;
        if (session != null) {
            Channel channel = session.openBasicChannel(aid, P2);
            if (channel == null) {
                return null;
            }
            return new Channel(this.mService, this, channel);
        }
        throw new IllegalStateException("service session is null");
    }

    public Channel openBasicChannel(byte[] aid) throws IOException {
        Channel channel;
        Log.d(TAG, "Session to openBasicChannel");
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mSession == null) {
            throw new IllegalStateException("service session is null");
        } else if (getReader() == null) {
            throw new IllegalStateException("reader must not be null");
        } else if (!NFC_FELICA && (channel = this.mSession.openBasicChannel(aid)) != null) {
            return new Channel(this.mService, this, channel);
        } else {
            return null;
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
        Log.d(TAG, "Session to openLogicalChannel P2:" + ((int) p2));
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
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
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        android.se.omapi.Session session = this.mSession;
        if (session != null) {
            session.close();
        }
    }

    public byte[] getATR() {
        Log.d(TAG, "Session to getATR");
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        android.se.omapi.Session session = this.mSession;
        if (session == null) {
            throw new IllegalStateException("service session is null");
        } else if (NFC_FELICA) {
            return null;
        } else {
            return session.getATR();
        }
    }

    public boolean isClosed() {
        Log.d(TAG, "Session to isClosed");
        android.se.omapi.Session session = this.mSession;
        if (session == null) {
            return true;
        }
        return session.isClosed();
    }

    public void closeChannels() {
        Log.d(TAG, "Session to closeChannels");
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        android.se.omapi.Session session = this.mSession;
        if (session != null) {
            session.closeChannels();
        }
    }

    public Reader getReader() {
        return this.mReader;
    }
}
