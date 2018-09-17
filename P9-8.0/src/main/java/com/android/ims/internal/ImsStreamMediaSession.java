package com.android.ims.internal;

public class ImsStreamMediaSession {
    private static final String TAG = "ImsStreamMediaSession";
    private Listener mListener;

    public static class Listener {
    }

    ImsStreamMediaSession(IImsStreamMediaSession mediaSession) {
    }

    ImsStreamMediaSession(IImsStreamMediaSession mediaSession, Listener listener) {
        this(mediaSession);
        setListener(listener);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }
}
