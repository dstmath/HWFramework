package android.nfc;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ContentProvider;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.nfc.IAppCallback.Stub;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcAdapter.ReaderCallback;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class NfcActivityManager extends Stub implements ActivityLifecycleCallbacks {
    static final Boolean DBG = null;
    static final String TAG = "NFC";
    final List<NfcActivityState> mActivities;
    final NfcAdapter mAdapter;
    final List<NfcApplicationState> mApps;

    class NfcActivityState {
        Activity activity;
        int flags;
        NdefMessage ndefMessage;
        CreateNdefMessageCallback ndefMessageCallback;
        OnNdefPushCompleteCallback onNdefPushCompleteCallback;
        ReaderCallback readerCallback;
        Bundle readerModeExtras;
        int readerModeFlags;
        boolean resumed;
        Binder token;
        CreateBeamUrisCallback uriCallback;
        Uri[] uris;

        public NfcActivityState(Activity activity) {
            this.resumed = false;
            this.ndefMessage = null;
            this.ndefMessageCallback = null;
            this.onNdefPushCompleteCallback = null;
            this.uriCallback = null;
            this.uris = null;
            this.flags = 0;
            this.readerModeFlags = 0;
            this.readerCallback = null;
            this.readerModeExtras = null;
            if (activity.getWindow().isDestroyed()) {
                throw new IllegalStateException("activity is already destroyed");
            }
            this.resumed = activity.isResumed();
            this.activity = activity;
            this.token = new Binder();
            NfcActivityManager.this.registerApplication(activity.getApplication());
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
            s.append(this.ndefMessage).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER).append(this.ndefMessageCallback).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            s.append(this.uriCallback).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            if (this.uris != null) {
                for (Uri uri : this.uris) {
                    s.append(this.onNdefPushCompleteCallback).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER).append(uri).append("]");
                }
            }
            return s.toString();
        }
    }

    class NfcApplicationState {
        final Application app;
        int refCount;

        public NfcApplicationState(Application app) {
            this.refCount = 0;
            this.app = app;
        }

        public void register() {
            this.refCount++;
            if (this.refCount == 1) {
                this.app.registerActivityLifecycleCallbacks(NfcActivityManager.this);
            }
        }

        public void unregister() {
            this.refCount--;
            if (this.refCount == 0) {
                this.app.unregisterActivityLifecycleCallbacks(NfcActivityManager.this);
            } else if (this.refCount < 0) {
                Log.e(NfcActivityManager.TAG, "-ve refcount for " + this.app);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.nfc.NfcActivityManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.nfc.NfcActivityManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcActivityManager.<clinit>():void");
    }

    NfcApplicationState findAppState(Application app) {
        for (NfcApplicationState appState : this.mApps) {
            if (appState.app == app) {
                return appState;
            }
        }
        return null;
    }

    void registerApplication(Application app) {
        NfcApplicationState appState = findAppState(app);
        if (appState == null) {
            appState = new NfcApplicationState(app);
            this.mApps.add(appState);
        }
        appState.register();
    }

    void unregisterApplication(Application app) {
        NfcApplicationState appState = findAppState(app);
        if (appState == null) {
            Log.e(TAG, "app was not registered " + app);
        } else {
            appState.unregister();
        }
    }

    synchronized NfcActivityState findActivityState(Activity activity) {
        for (NfcActivityState state : this.mActivities) {
            if (state.activity == activity) {
                return state;
            }
        }
        return null;
    }

    synchronized NfcActivityState getActivityState(Activity activity) {
        NfcActivityState state;
        state = findActivityState(activity);
        if (state == null) {
            state = new NfcActivityState(activity);
            this.mActivities.add(state);
        }
        return state;
    }

    synchronized NfcActivityState findResumedActivityState() {
        for (NfcActivityState state : this.mActivities) {
            if (state.resumed) {
                return state;
            }
        }
        return null;
    }

    synchronized void destroyActivityState(Activity activity) {
        NfcActivityState activityState = findActivityState(activity);
        if (activityState != null) {
            activityState.destroy();
            this.mActivities.remove(activityState);
        }
    }

    public NfcActivityManager(NfcAdapter adapter) {
        this.mAdapter = adapter;
        this.mActivities = new LinkedList();
        this.mApps = new ArrayList(1);
    }

    public void enableReaderMode(Activity activity, ReaderCallback callback, int flags, Bundle extras) {
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.readerCallback = callback;
            state.readerModeFlags = flags;
            state.readerModeExtras = extras;
            Binder token = state.token;
            boolean isResumed = state.resumed;
        }
        if (isResumed) {
            setReaderMode(token, flags, extras);
        }
    }

    public void disableReaderMode(Activity activity) {
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.readerCallback = null;
            state.readerModeFlags = 0;
            state.readerModeExtras = null;
            Binder token = state.token;
            boolean isResumed = state.resumed;
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
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.uris = uris;
            boolean isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        } else {
            verifyNfcPermission();
        }
    }

    public void setNdefPushContentUriCallback(Activity activity, CreateBeamUrisCallback callback) {
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.uriCallback = callback;
            boolean isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        } else {
            verifyNfcPermission();
        }
    }

    public void setNdefPushMessage(Activity activity, NdefMessage message, int flags) {
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.ndefMessage = message;
            state.flags = flags;
            boolean isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        } else {
            verifyNfcPermission();
        }
    }

    public void setNdefPushMessageCallback(Activity activity, CreateNdefMessageCallback callback, int flags) {
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.ndefMessageCallback = callback;
            state.flags = flags;
            boolean isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        } else {
            verifyNfcPermission();
        }
    }

    public void setOnNdefPushCompleteCallback(Activity activity, OnNdefPushCompleteCallback callback) {
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.onNdefPushCompleteCallback = callback;
            boolean isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        } else {
            verifyNfcPermission();
        }
    }

    void requestNfcServiceCallback() {
        try {
            NfcAdapter.sService.setAppCallback(this);
        } catch (RemoteException e) {
            this.mAdapter.attemptDeadServiceRecovery(e);
        }
    }

    void verifyNfcPermission() {
        try {
            NfcAdapter.sService.verifyNfcPermission();
        } catch (RemoteException e) {
            this.mAdapter.attemptDeadServiceRecovery(e);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public BeamShareData createBeamShareData(byte peerLlcpVersion) {
        NfcEvent event = new NfcEvent(this.mAdapter, peerLlcpVersion);
        synchronized (this) {
            NfcActivityState state = findResumedActivityState();
            if (state == null) {
                return null;
            }
            CreateNdefMessageCallback ndefCallback = state.ndefMessageCallback;
            CreateBeamUrisCallback urisCallback = state.uriCallback;
            NdefMessage message = state.ndefMessage;
            Uri[] uris = state.uris;
            int flags = state.flags;
            Activity activity = state.activity;
            long ident = Binder.clearCallingIdentity();
            if (ndefCallback != null) {
                message = ndefCallback.createNdefMessage(event);
            }
            if (urisCallback != null) {
                uris = urisCallback.createBeamUris(event);
                if (uris != null) {
                    ArrayList<Uri> validUris = new ArrayList();
                    for (Uri uri : uris) {
                        if (uri == null) {
                            Log.e(TAG, "Uri not allowed to be null.");
                        } else {
                            String scheme = uri.getScheme();
                            if (scheme != null) {
                                if (!scheme.equalsIgnoreCase(WifiManager.EXTRA_PASSPOINT_ICON_FILE)) {
                                }
                                validUris.add(ContentProvider.maybeAddUserId(uri, UserHandle.myUserId()));
                            }
                            try {
                                Log.e(TAG, "Uri needs to have either scheme file or scheme content");
                            } catch (Throwable th) {
                                Binder.restoreCallingIdentity(ident);
                            }
                        }
                    }
                    uris = (Uri[]) validUris.toArray(new Uri[validUris.size()]);
                }
            }
            if (uris != null && uris.length > 0) {
                for (Uri uri2 : uris) {
                    activity.grantUriPermission("com.android.nfc", uri2, 1);
                }
            }
            Binder.restoreCallingIdentity(ident);
            return new BeamShareData(message, uris, new UserHandle(UserHandle.myUserId()), flags);
        }
    }

    public void onNdefPushComplete(byte peerLlcpVersion) {
        synchronized (this) {
            NfcActivityState state = findResumedActivityState();
            if (state == null) {
                return;
            }
            OnNdefPushCompleteCallback callback = state.onNdefPushCompleteCallback;
            NfcEvent event = new NfcEvent(this.mAdapter, peerLlcpVersion);
            if (callback != null) {
                callback.onNdefPushComplete(event);
            }
        }
    }

    public void onTagDiscovered(Tag tag) throws RemoteException {
        synchronized (this) {
            NfcActivityState state = findResumedActivityState();
            if (state == null) {
                return;
            }
            ReaderCallback callback = state.readerCallback;
            if (callback != null) {
                callback.onTagDiscovered(tag);
            }
        }
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityStarted(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
        synchronized (this) {
            NfcActivityState state = findActivityState(activity);
            if (DBG.booleanValue()) {
                Log.d(TAG, "onResume() for " + activity + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + state);
            }
            if (state == null) {
                return;
            }
            state.resumed = true;
            Binder token = state.token;
            int readerModeFlags = state.readerModeFlags;
            Bundle readerModeExtras = state.readerModeExtras;
            if (readerModeFlags != 0) {
                setReaderMode(token, readerModeFlags, readerModeExtras);
            }
            requestNfcServiceCallback();
        }
    }

    public void onActivityPaused(Activity activity) {
        synchronized (this) {
            NfcActivityState state = findActivityState(activity);
            if (DBG.booleanValue()) {
                Log.d(TAG, "onPause() for " + activity + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + state);
            }
            if (state == null) {
                return;
            }
            state.resumed = false;
            Binder token = state.token;
            boolean readerModeFlagsSet = state.readerModeFlags != 0;
            if (readerModeFlagsSet) {
                setReaderMode(token, 0, null);
            }
        }
    }

    public void onActivityStopped(Activity activity) {
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

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
