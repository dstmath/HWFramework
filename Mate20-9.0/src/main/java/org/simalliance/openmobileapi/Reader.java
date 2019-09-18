package org.simalliance.openmobileapi;

import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Reader {
    private static final String TAG = "SIMalliance.OMAPI.Reader";
    private final ArrayList<EventCallBack> mEventCallBackList = new ArrayList<>();
    private final Object mLock = new Object();
    private final String mName;
    private android.se.omapi.Reader mReader;
    private final SEService mService;

    public static class Event {
        private int mEventType;
        private Reader mReader;

        public Event(Reader reader, int type) {
            this.mReader = reader;
            this.mEventType = type;
        }

        public Reader getReader() {
            return this.mReader;
        }

        public int getEventType() {
            return this.mEventType;
        }
    }

    public interface EventCallBack {
        void notify(Event event);
    }

    Reader(SEService service, String name) {
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
        if (this.mReader == null) {
            try {
                this.mReader = this.mService.getReader(this.mName);
            } catch (Exception e) {
                throw new IOException("service reader cannot be accessed.");
            }
        }
        return new Session(this.mService, this.mReader.openSession(), this);
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
        return this.mReader.isSecureElementPresent();
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
            this.mReader.closeSessions();
        }
    }

    public void registerReaderEventCallback(EventCallBack callBack) {
        synchronized (this.mEventCallBackList) {
            if (callBack == null) {
                try {
                    Log.v(TAG, "The callback is null");
                } catch (Throwable th) {
                    throw th;
                }
            } else if (!this.mEventCallBackList.contains(callBack)) {
                this.mEventCallBackList.add(callBack);
            } else {
                Log.v(TAG, "The callback has been already registered (" + callBack + ")");
            }
        }
    }

    public boolean unregisterReaderEventCallback(EventCallBack callBack) {
        synchronized (this.mEventCallBackList) {
            if (callBack == null) {
                try {
                    Log.v(TAG, "The callback is null");
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                boolean remove = this.mEventCallBackList.remove(callBack);
                return remove;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyEvent(int eventType) {
        Log.v(TAG, "notify eventType=" + eventType + " name=" + this.mName + " this=" + this);
        synchronized (this.mEventCallBackList) {
            Iterator<EventCallBack> it = this.mEventCallBackList.iterator();
            while (it.hasNext()) {
                final int type = eventType;
                final EventCallBack callback = it.next();
                new Thread(new Runnable() {
                    public void run() {
                        callback.notify(new Event(Reader.this, type));
                    }
                }).start();
            }
        }
    }
}
