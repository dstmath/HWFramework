package android.nfc;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.nfc.IAppCallback.Stub;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcAdapter.ReaderCallback;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class NfcActivityManager extends Stub implements ActivityLifecycleCallbacks {
    static final Boolean DBG = Boolean.valueOf(false);
    static final String TAG = "NFC";
    final List<NfcActivityState> mActivities = new LinkedList();
    final NfcAdapter mAdapter;
    final List<NfcApplicationState> mApps = new ArrayList(1);

    class NfcActivityState {
        Activity activity;
        int flags = 0;
        NdefMessage ndefMessage = null;
        CreateNdefMessageCallback ndefMessageCallback = null;
        OnNdefPushCompleteCallback onNdefPushCompleteCallback = null;
        ReaderCallback readerCallback = null;
        Bundle readerModeExtras = null;
        int readerModeFlags = 0;
        boolean resumed = false;
        Binder token;
        CreateBeamUrisCallback uriCallback = null;
        Uri[] uris = null;

        public NfcActivityState(Activity activity) {
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
        int refCount = 0;

        public NfcApplicationState(Application app) {
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
    }

    public void enableReaderMode(Activity activity, ReaderCallback callback, int flags, Bundle extras) {
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

    public void setNdefPushContentUriCallback(Activity activity, CreateBeamUrisCallback callback) {
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

    public void setNdefPushMessageCallback(Activity activity, CreateNdefMessageCallback callback, int flags) {
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

    public void setOnNdefPushCompleteCallback(Activity activity, OnNdefPushCompleteCallback callback) {
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

    /* JADX WARNING: Missing block: B:11:0x0022, code:
            r6 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Missing block: B:12:0x0026, code:
            if (r8 == null) goto L_0x002c;
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            r5 = r8.createNdefMessage(r3);
     */
    /* JADX WARNING: Missing block: B:15:0x002c, code:
            if (r13 == null) goto L_0x009d;
     */
    /* JADX WARNING: Missing block: B:16:0x002e, code:
            r12 = r13.createBeamUris(r3);
     */
    /* JADX WARNING: Missing block: B:17:0x0032, code:
            if (r12 == null) goto L_0x009d;
     */
    /* JADX WARNING: Missing block: B:18:0x0034, code:
            r14 = new java.util.ArrayList();
            r15 = 0;
            r16 = r12.length;
     */
    /* JADX WARNING: Missing block: B:20:0x003f, code:
            if (r15 >= r16) goto L_0x008f;
     */
    /* JADX WARNING: Missing block: B:21:0x0041, code:
            r11 = r12[r15];
     */
    /* JADX WARNING: Missing block: B:22:0x0043, code:
            if (r11 != null) goto L_0x0054;
     */
    /* JADX WARNING: Missing block: B:23:0x0045, code:
            android.util.Log.e(TAG, "Uri not allowed to be null.");
     */
    /* JADX WARNING: Missing block: B:24:0x004e, code:
            r15 = r15 + 1;
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            r9 = r11.getScheme();
     */
    /* JADX WARNING: Missing block: B:30:0x0058, code:
            if (r9 == null) goto L_0x0072;
     */
    /* JADX WARNING: Missing block: B:32:0x0063, code:
            if (r9.equalsIgnoreCase(android.content.ContentResolver.SCHEME_FILE) != false) goto L_0x0081;
     */
    /* JADX WARNING: Missing block: B:34:0x0070, code:
            if ((r9.equalsIgnoreCase("content") ^ 1) == 0) goto L_0x0081;
     */
    /* JADX WARNING: Missing block: B:35:0x0072, code:
            android.util.Log.e(TAG, "Uri needs to have either scheme file or scheme content");
     */
    /* JADX WARNING: Missing block: B:37:0x007d, code:
            android.os.Binder.restoreCallingIdentity(r6);
     */
    /* JADX WARNING: Missing block: B:40:?, code:
            r14.add(android.content.ContentProvider.maybeAddUserId(r11, android.os.UserHandle.myUserId()));
     */
    /* JADX WARNING: Missing block: B:41:0x008f, code:
            r12 = (android.net.Uri[]) r14.toArray(new android.net.Uri[r14.size()]);
     */
    /* JADX WARNING: Missing block: B:42:0x009d, code:
            if (r12 == null) goto L_0x00bb;
     */
    /* JADX WARNING: Missing block: B:44:0x00a0, code:
            if (r12.length <= 0) goto L_0x00bb;
     */
    /* JADX WARNING: Missing block: B:45:0x00a2, code:
            r15 = 0;
            r16 = r12.length;
     */
    /* JADX WARNING: Missing block: B:47:0x00a8, code:
            if (r15 >= r16) goto L_0x00bb;
     */
    /* JADX WARNING: Missing block: B:48:0x00aa, code:
            r2.grantUriPermission("com.android.nfc", r12[r15], 1);
     */
    /* JADX WARNING: Missing block: B:49:0x00b8, code:
            r15 = r15 + 1;
     */
    /* JADX WARNING: Missing block: B:50:0x00bb, code:
            android.os.Binder.restoreCallingIdentity(r6);
     */
    /* JADX WARNING: Missing block: B:51:0x00ce, code:
            return new android.nfc.BeamShareData(r5, r12, new android.os.UserHandle(android.os.UserHandle.myUserId()), r4);
     */
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
        }
    }

    /* JADX WARNING: Missing block: B:9:0x000c, code:
            r1 = new android.nfc.NfcEvent(r4.mAdapter, r5);
     */
    /* JADX WARNING: Missing block: B:10:0x0013, code:
            if (r0 == null) goto L_0x0018;
     */
    /* JADX WARNING: Missing block: B:11:0x0015, code:
            r0.onNdefPushComplete(r1);
     */
    /* JADX WARNING: Missing block: B:12:0x0018, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onNdefPushComplete(byte peerLlcpVersion) {
        synchronized (this) {
            NfcActivityState state = findResumedActivityState();
            if (state == null) {
                return;
            }
            OnNdefPushCompleteCallback callback = state.onNdefPushCompleteCallback;
        }
    }

    /* JADX WARNING: Missing block: B:9:0x000c, code:
            if (r0 == null) goto L_0x0011;
     */
    /* JADX WARNING: Missing block: B:10:0x000e, code:
            r0.onTagDiscovered(r4);
     */
    /* JADX WARNING: Missing block: B:11:0x0011, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onTagDiscovered(Tag tag) throws RemoteException {
        synchronized (this) {
            NfcActivityState state = findResumedActivityState();
            if (state == null) {
                return;
            }
            ReaderCallback callback = state.readerCallback;
        }
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityStarted(Activity activity) {
    }

    /* JADX WARNING: Missing block: B:13:0x0042, code:
            if (r1 == 0) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:14:0x0044, code:
            setReaderMode(r3, r1, r0);
     */
    /* JADX WARNING: Missing block: B:15:0x0047, code:
            requestNfcServiceCallback();
     */
    /* JADX WARNING: Missing block: B:16:0x004a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0043, code:
            if (r0 == false) goto L_0x0048;
     */
    /* JADX WARNING: Missing block: B:16:0x0045, code:
            setReaderMode(r2, 0, null);
     */
    /* JADX WARNING: Missing block: B:17:0x0048, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
