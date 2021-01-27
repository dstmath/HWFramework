package android.nfc;

import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.nfc.IAppCallback;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class NfcActivityManager extends IAppCallback.Stub implements Application.ActivityLifecycleCallbacks {
    static final Boolean DBG = false;
    static final String TAG = "NFC";
    final List<NfcActivityState> mActivities = new LinkedList();
    @UnsupportedAppUsage
    final NfcAdapter mAdapter;
    final List<NfcApplicationState> mApps = new ArrayList(1);

    /* access modifiers changed from: package-private */
    public class NfcApplicationState {
        final Application app;
        int refCount = 0;

        public NfcApplicationState(Application app2) {
            this.app = app2;
        }

        public void register() {
            this.refCount++;
            if (this.refCount == 1) {
                this.app.registerActivityLifecycleCallbacks(NfcActivityManager.this);
            }
        }

        public void unregister() {
            this.refCount--;
            int i = this.refCount;
            if (i == 0) {
                this.app.unregisterActivityLifecycleCallbacks(NfcActivityManager.this);
            } else if (i < 0) {
                Log.e(NfcActivityManager.TAG, "-ve refcount for " + this.app);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public NfcApplicationState findAppState(Application app) {
        for (NfcApplicationState appState : this.mApps) {
            if (appState.app == app) {
                return appState;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void registerApplication(Application app) {
        NfcApplicationState appState = findAppState(app);
        if (appState == null) {
            appState = new NfcApplicationState(app);
            this.mApps.add(appState);
        }
        appState.register();
    }

    /* access modifiers changed from: package-private */
    public void unregisterApplication(Application app) {
        NfcApplicationState appState = findAppState(app);
        if (appState == null) {
            Log.e(TAG, "app was not registered " + app);
            return;
        }
        appState.unregister();
    }

    /* access modifiers changed from: package-private */
    public class NfcActivityState {
        Activity activity;
        int flags = 0;
        NdefMessage ndefMessage = null;
        NfcAdapter.CreateNdefMessageCallback ndefMessageCallback = null;
        NfcAdapter.OnNdefPushCompleteCallback onNdefPushCompleteCallback = null;
        NfcAdapter.ReaderCallback readerCallback = null;
        Bundle readerModeExtras = null;
        int readerModeFlags = 0;
        boolean resumed = false;
        Binder token;
        NfcAdapter.CreateBeamUrisCallback uriCallback = null;
        Uri[] uris = null;

        public NfcActivityState(Activity activity2) {
            if (!activity2.getWindow().isDestroyed()) {
                this.resumed = activity2.isResumed();
                this.activity = activity2;
                this.token = new Binder();
                NfcActivityManager.this.registerApplication(activity2.getApplication());
                return;
            }
            throw new IllegalStateException("activity is already destroyed");
        }

        public void destroy() {
            NfcActivityManager.this.unregisterApplication(this.activity.getApplication());
            this.resumed = false;
            this.activity = null;
            this.ndefMessage = null;
            this.ndefMessageCallback = null;
            this.onNdefPushCompleteCallback = null;
            this.uriCallback = null;
            this.uris = null;
            this.readerModeFlags = 0;
            this.token = null;
        }

        public String toString() {
            StringBuilder s = new StringBuilder("[").append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            s.append(this.ndefMessage);
            s.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            s.append(this.ndefMessageCallback);
            s.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            s.append(this.uriCallback);
            s.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            Uri[] uriArr = this.uris;
            if (uriArr != null) {
                for (Uri uri : uriArr) {
                    s.append(this.onNdefPushCompleteCallback);
                    s.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                    s.append(uri);
                    s.append("]");
                }
            }
            return s.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized NfcActivityState findActivityState(Activity activity) {
        for (NfcActivityState state : this.mActivities) {
            if (state.activity == activity) {
                return state;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public synchronized NfcActivityState getActivityState(Activity activity) {
        NfcActivityState state;
        state = findActivityState(activity);
        if (state == null) {
            state = new NfcActivityState(activity);
            this.mActivities.add(state);
        }
        return state;
    }

    /* access modifiers changed from: package-private */
    public synchronized NfcActivityState findResumedActivityState() {
        for (NfcActivityState state : this.mActivities) {
            if (state.resumed) {
                return state;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public synchronized void destroyActivityState(Activity activity) {
        NfcActivityState activityState = findActivityState(activity);
        if (activityState != null) {
            activityState.destroy();
            this.mActivities.remove(activityState);
        }
    }

    public NfcActivityManager(NfcAdapter adapter) {
        this.mAdapter = adapter;
    }

    public void enableReaderMode(Activity activity, NfcAdapter.ReaderCallback callback, int flags, Bundle extras) {
        Binder token;
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.readerCallback = callback;
            state.readerModeFlags = flags;
            state.readerModeExtras = extras;
            token = state.token;
            isResumed = state.resumed;
        }
        if (isResumed) {
            setReaderMode(token, flags, extras);
        }
    }

    public void disableReaderMode(Activity activity) {
        Binder token;
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.readerCallback = null;
            state.readerModeFlags = 0;
            state.readerModeExtras = null;
            token = state.token;
            isResumed = state.resumed;
        }
        if (isResumed) {
            setReaderMode(token, 0, null);
        }
    }

    public void setReaderMode(Binder token, int flags, Bundle extras) {
        if (DBG.booleanValue()) {
            Log.d(TAG, "Setting reader mode");
        }
        try {
            NfcAdapter.sService.setReaderMode(token, this, flags, extras);
        } catch (RemoteException e) {
            this.mAdapter.attemptDeadServiceRecovery(e);
        }
    }

    public void setNdefPushContentUri(Activity activity, Uri[] uris) {
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.uris = uris;
            isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        } else {
            verifyNfcPermission();
        }
    }

    public void setNdefPushContentUriCallback(Activity activity, NfcAdapter.CreateBeamUrisCallback callback) {
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.uriCallback = callback;
            isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        } else {
            verifyNfcPermission();
        }
    }

    public void setNdefPushMessage(Activity activity, NdefMessage message, int flags) {
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.ndefMessage = message;
            state.flags = flags;
            isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        } else {
            verifyNfcPermission();
        }
    }

    public void setNdefPushMessageCallback(Activity activity, NfcAdapter.CreateNdefMessageCallback callback, int flags) {
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.ndefMessageCallback = callback;
            state.flags = flags;
            isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        } else {
            verifyNfcPermission();
        }
    }

    public void setOnNdefPushCompleteCallback(Activity activity, NfcAdapter.OnNdefPushCompleteCallback callback) {
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.onNdefPushCompleteCallback = callback;
            isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        } else {
            verifyNfcPermission();
        }
    }

    /* access modifiers changed from: package-private */
    public void requestNfcServiceCallback() {
        try {
            NfcAdapter.sService.setAppCallback(this);
        } catch (RemoteException e) {
            this.mAdapter.attemptDeadServiceRecovery(e);
        }
    }

    /* access modifiers changed from: package-private */
    public void verifyNfcPermission() {
        try {
            NfcAdapter.sService.verifyNfcPermission();
        } catch (RemoteException e) {
            this.mAdapter.attemptDeadServiceRecovery(e);
        }
    }

    @Override // android.nfc.IAppCallback
    public BeamShareData createBeamShareData(byte peerLlcpVersion) {
        Throwable th;
        NfcAdapter.CreateNdefMessageCallback ndefCallback;
        NfcAdapter.CreateBeamUrisCallback urisCallback;
        NdefMessage message;
        Uri[] uris;
        int flags;
        Activity activity;
        Throwable th2;
        NfcEvent event;
        NfcEvent event2 = new NfcEvent(this.mAdapter, peerLlcpVersion);
        synchronized (this) {
            try {
                NfcActivityState state = findResumedActivityState();
                if (state == null) {
                    try {
                        return null;
                    } catch (Throwable th3) {
                        th = th3;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th4) {
                                th = th4;
                            }
                        }
                        throw th;
                    }
                } else {
                    ndefCallback = state.ndefMessageCallback;
                    urisCallback = state.uriCallback;
                    message = state.ndefMessage;
                    uris = state.uris;
                    flags = state.flags;
                    activity = state.activity;
                }
            } catch (Throwable th5) {
                th = th5;
                while (true) {
                    break;
                }
                throw th;
            }
        }
        long ident = Binder.clearCallingIdentity();
        if (ndefCallback != null) {
            try {
                message = ndefCallback.createNdefMessage(event2);
            } catch (Throwable th6) {
                th2 = th6;
                Binder.restoreCallingIdentity(ident);
                throw th2;
            }
        }
        if (urisCallback != null) {
            try {
                uris = urisCallback.createBeamUris(event2);
                if (uris != null) {
                    ArrayList<Uri> validUris = new ArrayList<>();
                    int length = uris.length;
                    int i = 0;
                    while (i < length) {
                        Uri uri = uris[i];
                        if (uri == null) {
                            event = event2;
                            Log.e(TAG, "Uri not allowed to be null.");
                        } else {
                            event = event2;
                            String scheme = uri.getScheme();
                            if (scheme != null) {
                                if (scheme.equalsIgnoreCase(ContentResolver.SCHEME_FILE) || scheme.equalsIgnoreCase("content")) {
                                    validUris.add(ContentProvider.maybeAddUserId(uri, activity.getUserId()));
                                }
                            }
                            Log.e(TAG, "Uri needs to have either scheme file or scheme content");
                        }
                        i++;
                        event2 = event;
                    }
                    uris = (Uri[]) validUris.toArray(new Uri[validUris.size()]);
                }
            } catch (Throwable th7) {
                th2 = th7;
                Binder.restoreCallingIdentity(ident);
                throw th2;
            }
        }
        if (uris != null && uris.length > 0) {
            for (Uri uri2 : uris) {
                activity.grantUriPermission("com.android.nfc", uri2, 1);
            }
        }
        Binder.restoreCallingIdentity(ident);
        return new BeamShareData(message, uris, activity.getUser(), flags);
    }

    @Override // android.nfc.IAppCallback
    public void onNdefPushComplete(byte peerLlcpVersion) {
        NfcAdapter.OnNdefPushCompleteCallback callback;
        synchronized (this) {
            NfcActivityState state = findResumedActivityState();
            if (state != null) {
                callback = state.onNdefPushCompleteCallback;
            } else {
                return;
            }
        }
        NfcEvent event = new NfcEvent(this.mAdapter, peerLlcpVersion);
        if (callback != null) {
            callback.onNdefPushComplete(event);
        }
    }

    @Override // android.nfc.IAppCallback
    public void onTagDiscovered(Tag tag) throws RemoteException {
        NfcAdapter.ReaderCallback callback;
        synchronized (this) {
            NfcActivityState state = findResumedActivityState();
            if (state != null) {
                callback = state.readerCallback;
            } else {
                return;
            }
        }
        if (callback != null) {
            callback.onTagDiscovered(tag);
        }
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStarted(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityResumed(Activity activity) {
        Binder token;
        int readerModeFlags;
        Bundle readerModeExtras;
        synchronized (this) {
            NfcActivityState state = findActivityState(activity);
            if (DBG.booleanValue()) {
                Log.d(TAG, "onResume() for " + activity + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + state);
            }
            if (state != null) {
                state.resumed = true;
                token = state.token;
                readerModeFlags = state.readerModeFlags;
                readerModeExtras = state.readerModeExtras;
            } else {
                return;
            }
        }
        if (readerModeFlags != 0) {
            setReaderMode(token, readerModeFlags, readerModeExtras);
        }
        requestNfcServiceCallback();
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityPaused(Activity activity) {
        Binder token;
        boolean readerModeFlagsSet;
        synchronized (this) {
            NfcActivityState state = findActivityState(activity);
            if (DBG.booleanValue()) {
                Log.d(TAG, "onPause() for " + activity + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + state);
            }
            if (state != null) {
                state.resumed = false;
                token = state.token;
                readerModeFlagsSet = state.readerModeFlags != 0;
            } else {
                return;
            }
        }
        if (readerModeFlagsSet) {
            setReaderMode(token, 0, null);
        }
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStopped(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityDestroyed(Activity activity) {
        synchronized (this) {
            NfcActivityState state = findActivityState(activity);
            if (DBG.booleanValue()) {
                Log.d(TAG, "onDestroy() for " + activity + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + state);
            }
            if (state != null) {
                destroyActivityState(activity);
            }
        }
    }
}
