package android.nfc;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.app.ActivityThread;
import android.app.OnActivityPausedListener;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.INfcAdapter;
import android.nfc.INfcUnlockHandler;
import android.nfc.ITagRemovedCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class NfcAdapter {
    public static final String ACTION_ADAPTER_STATE_CHANGED = "android.nfc.action.ADAPTER_STATE_CHANGED";
    public static final String ACTION_HANDOVER_TRANSFER_DONE = "android.nfc.action.HANDOVER_TRANSFER_DONE";
    public static final String ACTION_HANDOVER_TRANSFER_STARTED = "android.nfc.action.HANDOVER_TRANSFER_STARTED";
    public static final String ACTION_NDEF_DISCOVERED = "android.nfc.action.NDEF_DISCOVERED";
    public static final String ACTION_TAG_DISCOVERED = "android.nfc.action.TAG_DISCOVERED";
    public static final String ACTION_TAG_LEFT_FIELD = "android.nfc.action.TAG_LOST";
    public static final String ACTION_TECH_DISCOVERED = "android.nfc.action.TECH_DISCOVERED";
    public static final String ACTION_TRANSACTION_DETECTED = "android.nfc.action.TRANSACTION_DETECTED";
    public static final String ALL_SE_ID = "com.nxp.all_se.ID";
    public static final int ALL_SE_ID_TYPE = 3;
    public static final int CARD_EMULATION_ALL_SUPPORT = 3;
    public static final int CARD_EMULATION_NO_SUPPORT = 0;
    public static final int CARD_EMULATION_SIM1_SUPPORT = 1;
    public static final int CARD_EMULATION_SIM2_SUPPORT = 2;
    public static final int CARD_EMULATION_SUB1 = 1;
    public static final int CARD_EMULATION_SUB2 = 2;
    public static final int CARD_EMULATION_UNKNOW = -1;
    public static final String EXTRA_ADAPTER_STATE = "android.nfc.extra.ADAPTER_STATE";
    public static final String EXTRA_AID = "android.nfc.extra.AID";
    public static final String EXTRA_DATA = "android.nfc.extra.DATA";
    public static final String EXTRA_HANDOVER_TRANSFER_STATUS = "android.nfc.extra.HANDOVER_TRANSFER_STATUS";
    public static final String EXTRA_HANDOVER_TRANSFER_URI = "android.nfc.extra.HANDOVER_TRANSFER_URI";
    public static final String EXTRA_ID = "android.nfc.extra.ID";
    public static final String EXTRA_NDEF_MESSAGES = "android.nfc.extra.NDEF_MESSAGES";
    public static final String EXTRA_READER_PRESENCE_CHECK_DELAY = "presence";
    public static final String EXTRA_SECURE_ELEMENT_NAME = "android.nfc.extra.SECURE_ELEMENT_NAME";
    public static final String EXTRA_TAG = "android.nfc.extra.TAG";
    @SystemApi
    public static final int FLAG_NDEF_PUSH_NO_CONFIRM = 1;
    public static final int FLAG_READER_NFC_A = 1;
    public static final int FLAG_READER_NFC_B = 2;
    public static final int FLAG_READER_NFC_BARCODE = 16;
    public static final int FLAG_READER_NFC_F = 4;
    public static final int FLAG_READER_NFC_V = 8;
    public static final int FLAG_READER_NO_PLATFORM_SOUNDS = 256;
    public static final int FLAG_READER_SKIP_NDEF_CHECK = 128;
    public static final int HANDOVER_TRANSFER_STATUS_FAILURE = 1;
    public static final int HANDOVER_TRANSFER_STATUS_SUCCESS = 0;
    public static final String HOST_ID = "com.nxp.host.ID";
    public static final int HOST_ID_TYPE = 0;
    private static final boolean NFC_FELICA = SystemProperties.getBoolean("ro.config.has_felica_feature", false);
    public static final String SMART_MX_ID = "com.nxp.smart_mx.ID";
    public static final int SMART_MX_ID_TYPE = 1;
    public static final int STATE_OFF = 1;
    public static final int STATE_ON = 3;
    public static final int STATE_TURNING_OFF = 4;
    public static final int STATE_TURNING_ON = 2;
    public static final int SWITCH_CE_FAILED = -1;
    public static final int SWITCH_CE_SUCCESS = 0;
    public static final String SWITH_CE_SWITCH_ACTION = "com.huawei.android.nfc.SWITCH_CE_STATE";
    public static final String SWITH_CE_SWITCH_STATUS = "com.huawei.android.nfc.CE_SELECTED_STATE";
    static final String TAG = "NFC";
    public static final String UICC_ID = "com.nxp.uicc.ID";
    public static final int UICC_ID_TYPE = 2;
    static INfcCardEmulation sCardEmulationService;
    static boolean sHasBeamFeature;
    static boolean sHasNfcFeature;
    static boolean sIsInitialized = false;
    static HashMap<Context, NfcAdapter> sNfcAdapters = new HashMap<>();
    static INfcFCardEmulation sNfcFCardEmulationService;
    static NfcAdapter sNullContextNfcAdapter;
    @UnsupportedAppUsage
    static INfcAdapter sService;
    static INfcTag sTagService;
    final Context mContext;
    OnActivityPausedListener mForegroundDispatchListener = new OnActivityPausedListener() {
        /* class android.nfc.NfcAdapter.AnonymousClass1 */

        @Override // android.app.OnActivityPausedListener
        public void onPaused(Activity activity) {
            NfcAdapter.this.disableForegroundDispatchInternal(activity, true);
        }
    };
    final Object mLock;
    final NfcActivityManager mNfcActivityManager;
    final HashMap<NfcUnlockHandler, INfcUnlockHandler> mNfcUnlockHandlers;
    ITagRemovedCallback mTagRemovedListener;

    @Deprecated
    public interface CreateBeamUrisCallback {
        Uri[] createBeamUris(NfcEvent nfcEvent);
    }

    @Deprecated
    public interface CreateNdefMessageCallback {
        NdefMessage createNdefMessage(NfcEvent nfcEvent);
    }

    @SystemApi
    public interface NfcUnlockHandler {
        boolean onUnlockAttempted(Tag tag);
    }

    @Deprecated
    public interface OnNdefPushCompleteCallback {
        void onNdefPushComplete(NfcEvent nfcEvent);
    }

    public interface OnTagRemovedListener {
        void onTagRemoved();
    }

    public interface ReaderCallback {
        void onTagDiscovered(Tag tag);
    }

    private static boolean hasBeamFeature() {
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "Cannot get package manager, assuming no Android Beam feature");
            return false;
        }
        try {
            return pm.hasSystemFeature(PackageManager.FEATURE_NFC_BEAM, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "Package manager query failed, assuming no Android Beam feature", e);
            return false;
        }
    }

    private static boolean hasNfcFeature() {
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "Cannot get package manager, assuming no NFC feature");
            return false;
        }
        try {
            return pm.hasSystemFeature(PackageManager.FEATURE_NFC, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "Package manager query failed, assuming no NFC feature", e);
            return false;
        }
    }

    private static boolean hasNfcHceFeature() {
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "Cannot get package manager, assuming no NFC feature");
            return false;
        }
        try {
            if (pm.hasSystemFeature("android.hardware.nfc.hce", 0) || pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF, 0)) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Package manager query failed, assuming no NFC feature", e);
            return false;
        }
    }

    public List<String> getSupportedOffHostSecureElements() {
        List<String> offHostSE = new ArrayList<>();
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "Cannot get package manager, assuming no off-host CE feature");
            return offHostSE;
        }
        try {
            if (pm.hasSystemFeature(PackageManager.FEATURE_NFC_OFF_HOST_CARD_EMULATION_UICC, 0)) {
                offHostSE.add("SIM");
            }
            if (pm.hasSystemFeature(PackageManager.FEATURE_NFC_OFF_HOST_CARD_EMULATION_ESE, 0)) {
                offHostSE.add("eSE");
            }
            return offHostSE;
        } catch (RemoteException e) {
            Log.e(TAG, "Package manager query failed, assuming no off-host CE feature", e);
            offHostSE.clear();
            return offHostSE;
        }
    }

    @UnsupportedAppUsage
    public static synchronized NfcAdapter getNfcAdapter(Context context) {
        synchronized (NfcAdapter.class) {
            if (!sIsInitialized) {
                sHasNfcFeature = hasNfcFeature();
                sHasBeamFeature = hasBeamFeature();
                boolean hasHceFeature = hasNfcHceFeature();
                if (!sHasNfcFeature) {
                    if (!hasHceFeature) {
                        Log.v(TAG, "this device does not have NFC support");
                        throw new UnsupportedOperationException();
                    }
                }
                sService = getServiceInterface();
                if (sService != null) {
                    if (sHasNfcFeature) {
                        try {
                            sTagService = sService.getNfcTagInterface();
                        } catch (RemoteException e) {
                            Log.e(TAG, "could not retrieve NFC Tag service");
                            throw new UnsupportedOperationException();
                        }
                    }
                    if (hasHceFeature) {
                        try {
                            sNfcFCardEmulationService = sService.getNfcFCardEmulationInterface();
                            try {
                                sCardEmulationService = sService.getNfcCardEmulationInterface();
                            } catch (RemoteException e2) {
                                Log.e(TAG, "could not retrieve card emulation service");
                                throw new UnsupportedOperationException();
                            }
                        } catch (RemoteException e3) {
                            Log.e(TAG, "could not retrieve NFC-F card emulation service");
                            throw new UnsupportedOperationException();
                        }
                    }
                    sIsInitialized = true;
                } else {
                    Log.e(TAG, "could not retrieve NFC service");
                    throw new UnsupportedOperationException();
                }
            }
            if (context == null) {
                if (sNullContextNfcAdapter == null) {
                    sNullContextNfcAdapter = new NfcAdapter(null);
                }
                return sNullContextNfcAdapter;
            }
            NfcAdapter adapter = sNfcAdapters.get(context);
            if (adapter == null) {
                adapter = new NfcAdapter(context);
                sNfcAdapters.put(context, adapter);
            }
            return adapter;
        }
    }

    private static INfcAdapter getServiceInterface() {
        IBinder b = ServiceManager.getService("nfc");
        if (b == null) {
            return null;
        }
        return INfcAdapter.Stub.asInterface(b);
    }

    public static NfcAdapter getDefaultAdapter(Context context) {
        if (context != null) {
            Context context2 = context.getApplicationContext();
            if (context2 != null) {
                NfcManager manager = (NfcManager) context2.getSystemService("nfc");
                if (manager == null) {
                    return null;
                }
                return manager.getDefaultAdapter();
            }
            throw new IllegalArgumentException("context not associated with any application (using a mock context?)");
        }
        throw new IllegalArgumentException("context cannot be null");
    }

    @UnsupportedAppUsage
    @Deprecated
    public static NfcAdapter getDefaultAdapter() {
        Log.w(TAG, "WARNING: NfcAdapter.getDefaultAdapter() is deprecated, use NfcAdapter.getDefaultAdapter(Context) instead", new Exception());
        return getNfcAdapter(null);
    }

    NfcAdapter(Context context) {
        this.mContext = context;
        this.mNfcActivityManager = new NfcActivityManager(this);
        this.mNfcUnlockHandlers = new HashMap<>();
        this.mTagRemovedListener = null;
        this.mLock = new Object();
    }

    @UnsupportedAppUsage
    public Context getContext() {
        return this.mContext;
    }

    @UnsupportedAppUsage
    public INfcAdapter getService() {
        isEnabled();
        return sService;
    }

    public INfcTag getTagService() {
        isEnabled();
        return sTagService;
    }

    public INfcCardEmulation getCardEmulationService() {
        isEnabled();
        return sCardEmulationService;
    }

    public INfcFCardEmulation getNfcFCardEmulationService() {
        isEnabled();
        return sNfcFCardEmulationService;
    }

    public INfcDta getNfcDtaInterface() {
        Context context = this.mContext;
        if (context != null) {
            try {
                return sService.getNfcDtaInterface(context.getPackageName());
            } catch (RemoteException e) {
                attemptDeadServiceRecovery(e);
                return null;
            }
        } else {
            throw new UnsupportedOperationException("You need a context on NfcAdapter to use the  NFC extras APIs");
        }
    }

    @UnsupportedAppUsage
    public void attemptDeadServiceRecovery(Exception e) {
        Log.e(TAG, "NFC service dead - attempting to recover", e);
        INfcAdapter service = getServiceInterface();
        if (service == null) {
            Log.e(TAG, "could not retrieve NFC service during service recovery");
            return;
        }
        sService = service;
        try {
            sTagService = service.getNfcTagInterface();
            try {
                sCardEmulationService = service.getNfcCardEmulationInterface();
            } catch (RemoteException e2) {
                Log.e(TAG, "could not retrieve NFC card emulation service during service recovery");
            }
            try {
                sNfcFCardEmulationService = service.getNfcFCardEmulationInterface();
            } catch (RemoteException e3) {
                Log.e(TAG, "could not retrieve NFC-F card emulation service during service recovery");
            }
        } catch (RemoteException e4) {
            Log.e(TAG, "could not retrieve NFC tag service during service recovery");
        }
    }

    public boolean isEnabled() {
        try {
            if (!NFC_FELICA) {
                return sService.getState() == 3;
            }
            if (sService.getState() != 3) {
                return false;
            }
            return sService.isRwP2pOn();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            try {
                if (!NFC_FELICA) {
                    return sService.getState() == 3;
                }
                if (sService.getState() != 3) {
                    return false;
                }
                return sService.isRwP2pOn();
            } catch (RemoteException e2) {
                return false;
            }
        }
    }

    @UnsupportedAppUsage
    public int getAdapterState() {
        try {
            return sService.getState();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return 1;
        }
    }

    @SystemApi
    public boolean enable() {
        try {
            return sService.enable();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            try {
                Log.d(TAG, "retry enable");
                return sService.enable();
            } catch (RemoteException re) {
                Log.e(TAG, "retry enable failure", re);
                return false;
            }
        }
    }

    @SystemApi
    public boolean disable() {
        try {
            return sService.disable(true);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            try {
                Log.d(TAG, "retry disable");
                return sService.disable(true);
            } catch (RemoteException re) {
                Log.e(TAG, "retry disable failure", re);
                return false;
            }
        }
    }

    @SystemApi
    public boolean disable(boolean persist) {
        try {
            return sService.disable(persist);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            try {
                Log.d(TAG, "retry disable");
                return sService.disable(persist);
            } catch (RemoteException re) {
                Log.e(TAG, "retry disbale failure", re);
                return false;
            }
        }
    }

    public void pausePolling(int timeoutInMs) {
        try {
            sService.pausePolling(timeoutInMs);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void resumePolling() {
        try {
            sService.resumePolling();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
        if (r7 == null) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0010, code lost:
        if (r6 == null) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0012, code lost:
        r0 = r6.length;
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0014, code lost:
        if (r1 >= r0) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0016, code lost:
        r2 = r6[r1];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0018, code lost:
        if (r2 == null) goto L_0x003b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x001a, code lost:
        r3 = r2.getScheme();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x001e, code lost:
        if (r3 == null) goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0026, code lost:
        if (r3.equalsIgnoreCase(android.content.ContentResolver.SCHEME_FILE) != false) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002e, code lost:
        if (r3.equalsIgnoreCase("content") == false) goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0030, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003a, code lost:
        throw new java.lang.IllegalArgumentException("URI needs to have either scheme file or scheme content");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0042, code lost:
        throw new java.lang.NullPointerException("Uri not allowed to be null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0043, code lost:
        r5.mNfcActivityManager.setNdefPushContentUri(r7, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0048, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0050, code lost:
        throw new java.lang.NullPointerException("activity cannot be null");
     */
    @Deprecated
    public void setBeamPushUris(Uri[] uris, Activity activity) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            } else if (!sHasBeamFeature) {
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
        if (r4 == null) goto L_0x0016;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0010, code lost:
        r2.mNfcActivityManager.setNdefPushContentUriCallback(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0015, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001d, code lost:
        throw new java.lang.NullPointerException("activity cannot be null");
     */
    @Deprecated
    public void setBeamPushUrisCallback(CreateBeamUrisCallback callback, Activity activity) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            } else if (!sHasBeamFeature) {
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
        r0 = getSdkVersion();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0012, code lost:
        if (r8 == null) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r6.mNfcActivityManager.setNdefPushMessage(r8, r7, 0);
        r1 = r9.length;
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        if (r3 >= r1) goto L_0x0032;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001e, code lost:
        r4 = r9[r3];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0020, code lost:
        if (r4 == null) goto L_0x002a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0022, code lost:
        r6.mNfcActivityManager.setNdefPushMessage(r4, r7, 0);
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0031, code lost:
        throw new java.lang.NullPointerException("activities cannot contain null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0033, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003c, code lost:
        throw new java.lang.NullPointerException("activity cannot be null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003f, code lost:
        if (r0 < 16) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0041, code lost:
        android.util.Log.e(android.nfc.NfcAdapter.TAG, "Cannot call API with Activity that has already been destroyed", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0049, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        return;
     */
    @Deprecated
    public void setNdefPushMessage(NdefMessage message, Activity activity, Activity... activities) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            } else if (!sHasBeamFeature) {
            }
        }
    }

    @SystemApi
    public void setNdefPushMessage(NdefMessage message, Activity activity, int flags) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            }
        }
        if (activity != null) {
            this.mNfcActivityManager.setNdefPushMessage(activity, message, flags);
            return;
        }
        throw new NullPointerException("activity cannot be null");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
        r0 = getSdkVersion();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0012, code lost:
        if (r8 == null) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r6.mNfcActivityManager.setNdefPushMessageCallback(r8, r7, 0);
        r1 = r9.length;
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        if (r3 >= r1) goto L_0x0032;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001e, code lost:
        r4 = r9[r3];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0020, code lost:
        if (r4 == null) goto L_0x002a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0022, code lost:
        r6.mNfcActivityManager.setNdefPushMessageCallback(r4, r7, 0);
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0031, code lost:
        throw new java.lang.NullPointerException("activities cannot contain null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0033, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003c, code lost:
        throw new java.lang.NullPointerException("activity cannot be null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003f, code lost:
        if (r0 < 16) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0041, code lost:
        android.util.Log.e(android.nfc.NfcAdapter.TAG, "Cannot call API with Activity that has already been destroyed", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0049, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        return;
     */
    @Deprecated
    public void setNdefPushMessageCallback(CreateNdefMessageCallback callback, Activity activity, Activity... activities) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            } else if (!sHasBeamFeature) {
            }
        }
    }

    @UnsupportedAppUsage
    public void setNdefPushMessageCallback(CreateNdefMessageCallback callback, Activity activity, int flags) {
        if (activity != null) {
            this.mNfcActivityManager.setNdefPushMessageCallback(activity, callback, flags);
            return;
        }
        throw new NullPointerException("activity cannot be null");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
        r0 = getSdkVersion();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0012, code lost:
        if (r7 == null) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r5.mNfcActivityManager.setOnNdefPushCompleteCallback(r7, r6);
        r1 = r8.length;
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001b, code lost:
        if (r2 >= r1) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001d, code lost:
        r3 = r8[r2];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x001f, code lost:
        if (r3 == null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0021, code lost:
        r5.mNfcActivityManager.setOnNdefPushCompleteCallback(r3, r6);
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0030, code lost:
        throw new java.lang.NullPointerException("activities cannot contain null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0032, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003b, code lost:
        throw new java.lang.NullPointerException("activity cannot be null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003e, code lost:
        if (r0 < 16) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0040, code lost:
        android.util.Log.e(android.nfc.NfcAdapter.TAG, "Cannot call API with Activity that has already been destroyed", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0048, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        return;
     */
    @Deprecated
    public void setOnNdefPushCompleteCallback(OnNdefPushCompleteCallback callback, Activity activity, Activity... activities) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            } else if (!sHasBeamFeature) {
            }
        }
    }

    public void enableForegroundDispatch(Activity activity, PendingIntent intent, IntentFilter[] filters, String[][] techLists) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            }
        }
        if (activity == null || intent == null) {
            throw new NullPointerException();
        } else if (activity.isResumed()) {
            TechListParcel parcel = null;
            if (techLists != null) {
                try {
                    if (techLists.length > 0) {
                        parcel = new TechListParcel(techLists);
                    }
                } catch (RemoteException e) {
                    attemptDeadServiceRecovery(e);
                    try {
                        Log.d(TAG, "retry enableForegroundDispatch");
                        TechListParcel parcel2 = null;
                        if (techLists != null && techLists.length > 0) {
                            parcel2 = new TechListParcel(techLists);
                        }
                        ActivityThread.currentActivityThread().registerOnActivityPausedListener(activity, this.mForegroundDispatchListener);
                        sService.setForegroundDispatch(intent, filters, parcel2);
                        return;
                    } catch (RemoteException re) {
                        Log.e(TAG, "retry setForegroundDispatch failure", re);
                        return;
                    }
                }
            }
            ActivityThread.currentActivityThread().registerOnActivityPausedListener(activity, this.mForegroundDispatchListener);
            sService.setForegroundDispatch(intent, filters, parcel);
        } else {
            throw new IllegalStateException("Foreground dispatch can only be enabled when your activity is resumed");
        }
    }

    public void disableForegroundDispatch(Activity activity) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            }
        }
        ActivityThread.currentActivityThread().unregisterOnActivityPausedListener(activity, this.mForegroundDispatchListener);
        disableForegroundDispatchInternal(activity, false);
    }

    /* access modifiers changed from: package-private */
    public void disableForegroundDispatchInternal(Activity activity, boolean force) {
        try {
            sService.setForegroundDispatch(null, null, null);
            if (force) {
                return;
            }
            if (!activity.isResumed()) {
                throw new IllegalStateException("You must disable foreground dispatching while your activity is still resumed");
            }
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            Log.d(TAG, "retry disableForegroundDispatchInternal");
            sService.setForegroundDispatch(null, null, null);
            if (force) {
                return;
            }
            if (!activity.isResumed()) {
                throw new IllegalStateException("You must disable foreground dispatching while your activity is still resumed");
            }
        } catch (RemoteException re) {
            Log.e(TAG, "retry disableForegroundDispatchInternal failure", re);
        }
    }

    public void enableReaderMode(Activity activity, ReaderCallback callback, int flags, Bundle extras) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            }
        }
        this.mNfcActivityManager.enableReaderMode(activity, callback, flags, extras);
    }

    public void disableReaderMode(Activity activity) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            }
        }
        this.mNfcActivityManager.disableReaderMode(activity);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000f, code lost:
        if (r5 == null) goto L_0x0028;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0011, code lost:
        enforceResumed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        android.nfc.NfcAdapter.sService.invokeBeam();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0019, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x001c, code lost:
        android.util.Log.e(android.nfc.NfcAdapter.TAG, "invokeBeam: NFC process has died.");
        attemptDeadServiceRecovery(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0027, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002f, code lost:
        throw new java.lang.NullPointerException("activity may not be null.");
     */
    @Deprecated
    public boolean invokeBeam(Activity activity) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            } else if (!sHasBeamFeature) {
                return false;
            }
        }
    }

    public boolean invokeBeam(BeamShareData shareData) {
        try {
            Log.e(TAG, "invokeBeamInternal()");
            sService.invokeBeamInternal(shareData);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "invokeBeam: NFC process has died.");
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
        if (r3 == null) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0010, code lost:
        if (r4 == null) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0012, code lost:
        enforceResumed(r3);
        r2.mNfcActivityManager.setNdefPushMessage(r3, r4, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        throw new java.lang.NullPointerException();
     */
    @Deprecated
    public void enableForegroundNdefPush(Activity activity, NdefMessage message) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            } else if (!sHasBeamFeature) {
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
        if (r4 == null) goto L_0x0025;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0010, code lost:
        enforceResumed(r4);
        r3.mNfcActivityManager.setNdefPushMessage(r4, null, 0);
        r3.mNfcActivityManager.setNdefPushMessageCallback(r4, null, 0);
        r3.mNfcActivityManager.setOnNdefPushCompleteCallback(r4, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
        throw new java.lang.NullPointerException();
     */
    @Deprecated
    public void disableForegroundNdefPush(Activity activity) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            } else if (!sHasBeamFeature) {
            }
        }
    }

    @SystemApi
    public boolean enableSecureNfc(boolean enable) {
        if (sHasNfcFeature) {
            try {
                return sService.setNfcSecure(enable);
            } catch (RemoteException e) {
                attemptDeadServiceRecovery(e);
                return false;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean isSecureNfcSupported() {
        if (sHasNfcFeature) {
            try {
                return sService.deviceSupportsNfcSecure();
            } catch (RemoteException e) {
                attemptDeadServiceRecovery(e);
                return false;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean isSecureNfcEnabled() {
        if (sHasNfcFeature) {
            try {
                return sService.isNfcSecureEnabled();
            } catch (RemoteException e) {
                attemptDeadServiceRecovery(e);
                return false;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @SystemApi
    public boolean enableNdefPush() {
        if (sHasNfcFeature) {
            try {
                return sService.enableNdefPush();
            } catch (RemoteException e) {
                attemptDeadServiceRecovery(e);
                return false;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @SystemApi
    public boolean disableNdefPush() {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            }
        }
        try {
            return sService.disableNdefPush();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    @Deprecated
    public boolean isNdefPushEnabled() {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            } else if (!sHasBeamFeature) {
                return false;
            } else {
                try {
                    return sService.isNdefPushEnabled();
                } catch (RemoteException e) {
                    attemptDeadServiceRecovery(e);
                    return false;
                }
            }
        }
    }

    public boolean ignore(Tag tag, int debounceMs, final OnTagRemovedListener tagRemovedListener, final Handler handler) {
        ITagRemovedCallback.Stub iListener = null;
        if (tagRemovedListener != null) {
            iListener = new ITagRemovedCallback.Stub() {
                /* class android.nfc.NfcAdapter.AnonymousClass2 */

                @Override // android.nfc.ITagRemovedCallback
                public void onTagRemoved() throws RemoteException {
                    Handler handler = handler;
                    if (handler != null) {
                        handler.post(new Runnable() {
                            /* class android.nfc.NfcAdapter.AnonymousClass2.AnonymousClass1 */

                            public void run() {
                                tagRemovedListener.onTagRemoved();
                            }
                        });
                    } else {
                        tagRemovedListener.onTagRemoved();
                    }
                    synchronized (NfcAdapter.this.mLock) {
                        NfcAdapter.this.mTagRemovedListener = null;
                    }
                }
            };
        }
        synchronized (this.mLock) {
            this.mTagRemovedListener = iListener;
        }
        try {
            return sService.ignore(tag.getServiceHandle(), debounceMs, iListener);
        } catch (RemoteException e) {
            return false;
        }
    }

    public void dispatch(Tag tag) {
        if (tag != null) {
            try {
                sService.dispatch(tag);
            } catch (RemoteException e) {
                attemptDeadServiceRecovery(e);
            }
        } else {
            throw new NullPointerException("tag cannot be null");
        }
    }

    public void setP2pModes(int initiatorModes, int targetModes) {
        try {
            sService.setP2pModes(initiatorModes, targetModes);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    @SystemApi
    public boolean addNfcUnlockHandler(final NfcUnlockHandler unlockHandler, String[] tagTechnologies) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            }
        }
        if (tagTechnologies.length == 0) {
            return false;
        }
        try {
            synchronized (this.mLock) {
                if (this.mNfcUnlockHandlers.containsKey(unlockHandler)) {
                    sService.removeNfcUnlockHandler(this.mNfcUnlockHandlers.get(unlockHandler));
                    this.mNfcUnlockHandlers.remove(unlockHandler);
                }
                INfcUnlockHandler.Stub iHandler = new INfcUnlockHandler.Stub() {
                    /* class android.nfc.NfcAdapter.AnonymousClass3 */

                    @Override // android.nfc.INfcUnlockHandler
                    public boolean onUnlockAttempted(Tag tag) throws RemoteException {
                        return unlockHandler.onUnlockAttempted(tag);
                    }
                };
                sService.addNfcUnlockHandler(iHandler, Tag.getTechCodesFromStrings(tagTechnologies));
                this.mNfcUnlockHandlers.put(unlockHandler, iHandler);
            }
            return true;
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "Unable to register LockscreenDispatch", e2);
            return false;
        }
    }

    @SystemApi
    public boolean removeNfcUnlockHandler(NfcUnlockHandler unlockHandler) {
        synchronized (NfcAdapter.class) {
            if (!sHasNfcFeature) {
                throw new UnsupportedOperationException();
            }
        }
        try {
            synchronized (this.mLock) {
                if (this.mNfcUnlockHandlers.containsKey(unlockHandler)) {
                    sService.removeNfcUnlockHandler(this.mNfcUnlockHandlers.remove(unlockHandler));
                }
            }
            return true;
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public INfcAdapterExtras getNfcAdapterExtrasInterface() {
        Context context = this.mContext;
        if (context != null) {
            try {
                return sService.getNfcAdapterExtrasInterface(context.getPackageName());
            } catch (RemoteException e) {
                attemptDeadServiceRecovery(e);
                return null;
            }
        } else {
            throw new UnsupportedOperationException("You need a context on NfcAdapter to use the  NFC extras APIs");
        }
    }

    /* access modifiers changed from: package-private */
    public void enforceResumed(Activity activity) {
        if (!activity.isResumed()) {
            throw new IllegalStateException("API cannot be called while activity is paused");
        }
    }

    /* access modifiers changed from: package-private */
    public int getSdkVersion() {
        Context context = this.mContext;
        if (context == null) {
            return 9;
        }
        return context.getApplicationInfo().targetSdkVersion;
    }

    public int getSelectedCardEmulation() {
        try {
            return sService.getSelectedCardEmulation();
        } catch (RemoteException e) {
            Log.e(TAG, "get selected ce failed!");
            return -1;
        }
    }

    public void selectCardEmulation(int sub) {
        try {
            sService.selectCardEmulation(sub);
        } catch (RemoteException e) {
            Log.e(TAG, "select ce failed!");
        }
    }

    public int getSupportCardEmulation() {
        try {
            return sService.getSupportCardEmulation();
        } catch (RemoteException e) {
            Log.e(TAG, "get support ce failed!");
            return -1;
        }
    }

    public String getFirmwareVersion() {
        try {
            return sService.getFirmwareVersion();
        } catch (RemoteException e) {
            Log.e(TAG, "get Firmware Version failed!");
            return null;
        }
    }

    public boolean isTagRwEnabled() {
        try {
            return sService.is2ndLevelMenuOn();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public void enableTagRw() {
        try {
            sService.set2ndLevelMenu(true);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void disableTagRw() {
        try {
            sService.set2ndLevelMenu(false);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }
}
