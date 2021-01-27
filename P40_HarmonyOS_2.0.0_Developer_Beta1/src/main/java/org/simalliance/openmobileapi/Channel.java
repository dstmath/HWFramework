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
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        android.se.omapi.Channel channel = this.mChannel;
        if (channel != null) {
            channel.close();
            return;
        }
        throw new IllegalStateException("channel must not be null");
    }

    public boolean isClosed() {
        Log.d(TAG, "Channel to isClosed");
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
            return true;
        }
        android.se.omapi.Channel channel = this.mChannel;
        if (channel != null) {
            return !channel.isOpen();
        }
        throw new IllegalStateException("channel must not be null");
    }

    public boolean isBasicChannel() {
        Log.d(TAG, "Channel to isBasicChannel");
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        android.se.omapi.Channel channel = this.mChannel;
        if (channel != null) {
            return channel.isBasicChannel();
        }
        throw new IllegalStateException("channel must not be null");
    }

    public byte[] transmit(byte[] command) throws IOException {
        Log.d(TAG, "Channel to transmit");
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        android.se.omapi.Channel channel = this.mChannel;
        if (channel != null) {
            return channel.transmit(command);
        }
        throw new IllegalStateException("channel must not be null");
    }

    public Session getSession() {
        return this.mSession;
    }

    public byte[] getSelectResponse() {
        Log.d(TAG, "Channel to getSelectResponse");
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        android.se.omapi.Channel channel = this.mChannel;
        if (channel == null) {
            throw new IllegalStateException("channel must not be null");
        } else if (channel.isOpen()) {
            return this.mChannel.getSelectResponse();
        } else {
            throw new IllegalStateException("channel is closed");
        }
    }

    public boolean selectNext() throws IOException {
        Log.d(TAG, "Channel to selectNext");
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        android.se.omapi.Channel channel = this.mChannel;
        if (channel == null) {
            throw new IllegalStateException("channel must not be null");
        } else if (channel.isOpen()) {
            return this.mChannel.selectNext();
        } else {
            throw new IllegalStateException("channel is closed");
        }
    }

    public void setTransmitBehaviour(boolean expectDataWithWarningSW) {
        Log.d(TAG, "setTransmitBehaviour ,expectDataWithWarningSW:" + expectDataWithWarningSW);
        SEService sEService = this.mService;
        if (sEService == null || !sEService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        } else if (this.mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        }
    }
}
