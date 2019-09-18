package android.nfc;

import android.app.Activity;
import android.app.Application;
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
    final NfcAdapter mAdapter;
    final List<NfcApplicationState> mApps = new ArrayList(1);

    class NfcActivityState {
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
            if (this.uris != null) {
                for (Uri uri : this.uris) {
                    s.append(this.onNdefPushCompleteCallback);
                    s.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                    s.append(uri);
                    s.append("]");
                }
            }
            return s.toString();
        }
    }

    class NfcApplicationState {
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
            if (this.refCount == 0) {
                this.app.unregisterActivityLifecycleCallbacks(NfcActivityManager.this);
            } else if (this.refCount < 0) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0028, code lost:
        r10 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002c, code lost:
        if (r4 == null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0032, code lost:
        r6 = r4.createNdefMessage(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0034, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0035, code lost:
        r16 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0039, code lost:
        if (r5 == null) goto L_0x00a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r7 = r5.createBeamUris(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0040, code lost:
        if (r7 == null) goto L_0x00a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0042, code lost:
        r12 = new java.util.ArrayList<>();
        r13 = r7.length;
        r14 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0049, code lost:
        if (r14 >= r13) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004b, code lost:
        r15 = r7[r14];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004d, code lost:
        if (r15 != null) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0051, code lost:
        r16 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        android.util.Log.e(TAG, "Uri not allowed to be null.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0059, code lost:
        r16 = r2;
        r0 = r15.getScheme();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005f, code lost:
        if (r0 == null) goto L_0x007e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0067, code lost:
        if (r0.equalsIgnoreCase("file") != false) goto L_0x0072;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006f, code lost:
        if (r0.equalsIgnoreCase("content") != false) goto L_0x0072;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0072, code lost:
        r12.add(android.content.ContentProvider.maybeAddUserId(r15, r9.getUserId()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x007e, code lost:
        r17 = r0;
        android.util.Log.e(TAG, "Uri needs to have either scheme file or scheme content");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0088, code lost:
        r14 = r14 + 1;
        r2 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x008d, code lost:
        r16 = r2;
        r7 = (android.net.Uri[]) r12.toArray(new android.net.Uri[r12.size()]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x009d, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x009e, code lost:
        r16 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00a1, code lost:
        r16 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00a3, code lost:
        if (r7 == null) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00a6, code lost:
        if (r7.length <= 0) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a8, code lost:
        r0 = r7.length;
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00aa, code lost:
        if (r2 >= r0) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00ac, code lost:
        r9.grantUriPermission("com.android.nfc", r7[r2], 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00b4, code lost:
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00b7, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00b8, code lost:
        android.os.Binder.restoreCallingIdentity(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00bb, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00bc, code lost:
        android.os.Binder.restoreCallingIdentity(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00c9, code lost:
        return new android.nfc.BeamShareData(r6, r7, r9.getUser(), r8);
     */
    public BeamShareData createBeamShareData(byte peerLlcpVersion) {
        NfcEvent event = new NfcEvent(this.mAdapter, peerLlcpVersion);
        synchronized (this) {
            try {
                NfcActivityState state = findResumedActivityState();
                if (state == null) {
                    try {
                        return null;
                    } catch (Throwable th) {
                        th = th;
                        NfcEvent nfcEvent = event;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        throw th;
                    }
                } else {
                    NfcAdapter.CreateNdefMessageCallback ndefCallback = state.ndefMessageCallback;
                    NfcAdapter.CreateBeamUrisCallback urisCallback = state.uriCallback;
                    NdefMessage message = state.ndefMessage;
                    Uri[] uris = state.uris;
                    int flags = state.flags;
                    Activity activity = state.activity;
                }
            } catch (Throwable th3) {
                th = th3;
                NfcEvent nfcEvent2 = event;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
        r0.onNdefPushComplete(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0019, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000d, code lost:
        r1 = new android.nfc.NfcEvent(r3.mAdapter, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0014, code lost:
        if (r0 == null) goto L_0x0019;
     */
    public void onNdefPushComplete(byte peerLlcpVersion) {
        synchronized (this) {
            NfcActivityState state = findResumedActivityState();
            if (state != null) {
                NfcAdapter.OnNdefPushCompleteCallback callback = state.onNdefPushCompleteCallback;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0012, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000d, code lost:
        if (r0 == null) goto L_0x0012;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000f, code lost:
        r0.onTagDiscovered(r3);
     */
    public void onTagDiscovered(Tag tag) throws RemoteException {
        synchronized (this) {
            NfcActivityState state = findResumedActivityState();
            if (state != null) {
                NfcAdapter.ReaderCallback callback = state.readerCallback;
            }
        }
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityStarted(Activity activity) {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x003e, code lost:
        if (r0 == 0) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0040, code lost:
        setReaderMode(r3, r0, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0043, code lost:
        requestNfcServiceCallback();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0046, code lost:
        return;
     */
    public void onActivityResumed(Activity activity) {
        synchronized (this) {
            NfcActivityState state = findActivityState(activity);
            if (DBG.booleanValue()) {
                Log.d(TAG, "onResume() for " + activity + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + state);
            }
            if (state != null) {
                state.resumed = true;
                Binder token = state.token;
                int readerModeFlags = state.readerModeFlags;
                Bundle readerModeExtras = state.readerModeExtras;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003e, code lost:
        if (r0 == false) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0040, code lost:
        setReaderMode(r2, 0, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0044, code lost:
        return;
     */
    public void onActivityPaused(Activity activity) {
        synchronized (this) {
            NfcActivityState state = findActivityState(activity);
            if (DBG.booleanValue()) {
                Log.d(TAG, "onPause() for " + activity + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + state);
            }
            if (state != null) {
                state.resumed = false;
                Binder token = state.token;
                boolean readerModeFlagsSet = state.readerModeFlags != 0;
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
