package org.simalliance.openmobileapi;

import android.util.Log;
import java.io.IOException;

public class Channel {
    private static final String TAG = "SIMalliance.OMAPI.Channel";
    private final android.se.omapi.Channel mChannel;
    private final Object mLock = new Object();
    private final SEService mService;
    private Session mSession;

    Channel(SEService service, Session session, android.se.omapi.Channel channel) {
        this.mService = service;
        this.mSession = session;
        this.mChannel = channel;
    }

    public void close() {
        Log.d(TAG, "Channel to close");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel != null) {
            this.mChannel.close();
        } else {
            throw new IllegalStateException("channel must not be null");
        }
    }

    public boolean isClosed() {
        Log.d(TAG, "Channel to isClosed");
        if (this.mService == null || !this.mService.isConnected()) {
            return true;
        }
        if (this.mChannel != null) {
            return !this.mChannel.isOpen();
        }
        throw new IllegalStateException("channel must not be null");
    }

    public boolean isBasicChannel() {
        Log.d(TAG, "Channel to isBasicChannel");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel != null) {
            return this.mChannel.isBasicChannel();
        } else {
            throw new IllegalStateException("channel must not be null");
        }
    }

    public byte[] transmit(byte[] command) throws IOException {
        Log.d(TAG, "Channel to transmit");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel != null) {
            return this.mChannel.transmit(command);
        } else {
            throw new IllegalStateException("channel must not be null");
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
        } else if (this.mChannel.isOpen()) {
            return this.mChannel.getSelectResponse();
        } else {
            throw new IllegalStateException("channel is closed");
        }
    }

    public boolean selectNext() throws IOException {
        Log.d(TAG, "Channel to selectNext");
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        } else if (this.mChannel.isOpen()) {
            return this.mChannel.selectNext();
        } else {
            throw new IllegalStateException("channel is closed");
        }
    }

    public void setTransmitBehaviour(boolean expectDataWithWarningSW) {
        Log.d(TAG, "setTransmitBehaviour ,expectDataWithWarningSW:" + expectDataWithWarningSW);
        if (this.mService == null || !this.mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        }
    }
}
