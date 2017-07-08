package android.nfc;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.OnActivityPausedListener;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.ITagRemovedCallback.Stub;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.System;
import android.service.voice.VoiceInteractionSession;
import android.util.Log;
import java.util.HashMap;

public final class NfcAdapter {
    public static final String ACTION_ADAPTER_STATE_CHANGED = "android.nfc.action.ADAPTER_STATE_CHANGED";
    public static final String ACTION_HANDOVER_TRANSFER_DONE = "android.nfc.action.HANDOVER_TRANSFER_DONE";
    public static final String ACTION_HANDOVER_TRANSFER_STARTED = "android.nfc.action.HANDOVER_TRANSFER_STARTED";
    public static final String ACTION_NDEF_DISCOVERED = "android.nfc.action.NDEF_DISCOVERED";
    public static final String ACTION_TAG_DISCOVERED = "android.nfc.action.TAG_DISCOVERED";
    public static final String ACTION_TAG_LEFT_FIELD = "android.nfc.action.TAG_LOST";
    public static final String ACTION_TECH_DISCOVERED = "android.nfc.action.TECH_DISCOVERED";
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
    public static final String EXTRA_HANDOVER_TRANSFER_STATUS = "android.nfc.extra.HANDOVER_TRANSFER_STATUS";
    public static final String EXTRA_HANDOVER_TRANSFER_URI = "android.nfc.extra.HANDOVER_TRANSFER_URI";
    public static final String EXTRA_ID = "android.nfc.extra.ID";
    public static final String EXTRA_NDEF_MESSAGES = "android.nfc.extra.NDEF_MESSAGES";
    public static final String EXTRA_READER_PRESENCE_CHECK_DELAY = "presence";
    public static final String EXTRA_TAG = "android.nfc.extra.TAG";
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
    static boolean sIsInitialized;
    static HashMap<Context, NfcAdapter> sNfcAdapters;
    static INfcFCardEmulation sNfcFCardEmulationService;
    static NfcAdapter sNullContextNfcAdapter;
    static INfcAdapter sService;
    static INfcTag sTagService;
    final Context mContext;
    OnActivityPausedListener mForegroundDispatchListener;
    final Object mLock;
    final NfcActivityManager mNfcActivityManager;
    final HashMap<NfcUnlockHandler, INfcUnlockHandler> mNfcUnlockHandlers;
    ITagRemovedCallback mTagRemovedListener;

    /* renamed from: android.nfc.NfcAdapter.2 */
    class AnonymousClass2 extends Stub {
        final /* synthetic */ Handler val$handler;
        final /* synthetic */ OnTagRemovedListener val$tagRemovedListener;

        /* renamed from: android.nfc.NfcAdapter.2.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ OnTagRemovedListener val$tagRemovedListener;

            AnonymousClass1(OnTagRemovedListener val$tagRemovedListener) {
                this.val$tagRemovedListener = val$tagRemovedListener;
            }

            public void run() {
                this.val$tagRemovedListener.onTagRemoved();
            }
        }

        AnonymousClass2(Handler val$handler, OnTagRemovedListener val$tagRemovedListener) {
            this.val$handler = val$handler;
            this.val$tagRemovedListener = val$tagRemovedListener;
        }

        public void onTagRemoved() throws RemoteException {
            if (this.val$handler != null) {
                this.val$handler.post(new AnonymousClass1(this.val$tagRemovedListener));
            } else {
                this.val$tagRemovedListener.onTagRemoved();
            }
            synchronized (NfcAdapter.this.mLock) {
                NfcAdapter.this.mTagRemovedListener = null;
            }
        }
    }

    /* renamed from: android.nfc.NfcAdapter.3 */
    class AnonymousClass3 extends INfcUnlockHandler.Stub {
        final /* synthetic */ NfcUnlockHandler val$unlockHandler;

        AnonymousClass3(NfcUnlockHandler val$unlockHandler) {
            this.val$unlockHandler = val$unlockHandler;
        }

        public boolean onUnlockAttempted(Tag tag) throws RemoteException {
            return this.val$unlockHandler.onUnlockAttempted(tag);
        }
    }

    public interface CreateBeamUrisCallback {
        Uri[] createBeamUris(NfcEvent nfcEvent);
    }

    public interface CreateNdefMessageCallback {
        NdefMessage createNdefMessage(NfcEvent nfcEvent);
    }

    public interface NfcUnlockHandler {
        boolean onUnlockAttempted(Tag tag);
    }

    public interface OnNdefPushCompleteCallback {
        void onNdefPushComplete(NfcEvent nfcEvent);
    }

    public interface OnTagRemovedListener {
        void onTagRemoved();
    }

    public interface ReaderCallback {
        void onTagDiscovered(Tag tag);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.nfc.NfcAdapter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.nfc.NfcAdapter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcAdapter.<clinit>():void");
    }

    private static boolean hasNfcFeature() {
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "Cannot get package manager, assuming no NFC feature");
            return false;
        }
        try {
            return pm.hasSystemFeature(PackageManager.FEATURE_NFC, SWITCH_CE_SUCCESS);
        } catch (RemoteException e) {
            Log.e(TAG, "Package manager query failed, assuming no NFC feature", e);
            return false;
        }
    }

    public static synchronized NfcAdapter getNfcAdapter(Context context) {
        synchronized (NfcAdapter.class) {
            if (!sIsInitialized) {
                if (hasNfcFeature()) {
                    sService = getServiceInterface();
                    if (sService == null) {
                        Log.e(TAG, "could not retrieve NFC service");
                        throw new UnsupportedOperationException();
                    }
                    try {
                        sTagService = sService.getNfcTagInterface();
                        sCardEmulationService = sService.getNfcCardEmulationInterface();
                        sNfcFCardEmulationService = sService.getNfcFCardEmulationInterface();
                        sIsInitialized = true;
                    } catch (RemoteException e) {
                        Log.e(TAG, "could not retrieve NFC-F card emulation service");
                        throw new UnsupportedOperationException();
                    } catch (RemoteException e2) {
                        Log.e(TAG, "could not retrieve card emulation service");
                        throw new UnsupportedOperationException();
                    } catch (RemoteException e3) {
                        Log.e(TAG, "could not retrieve NFC Tag service");
                        throw new UnsupportedOperationException();
                    }
                }
                Log.v(TAG, "this device does not have NFC support");
                throw new UnsupportedOperationException();
            }
            if (context == null) {
                if (sNullContextNfcAdapter == null) {
                    sNullContextNfcAdapter = new NfcAdapter(null);
                }
                NfcAdapter nfcAdapter = sNullContextNfcAdapter;
                return nfcAdapter;
            }
            NfcAdapter adapter = (NfcAdapter) sNfcAdapters.get(context);
            if (adapter == null) {
                adapter = new NfcAdapter(context);
                sNfcAdapters.put(context, adapter);
            }
            return adapter;
        }
    }

    private static INfcAdapter getServiceInterface() {
        IBinder b = ServiceManager.getService(System.RADIO_NFC);
        if (b == null) {
            return null;
        }
        return INfcAdapter.Stub.asInterface(b);
    }

    public static NfcAdapter getDefaultAdapter(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }
        context = context.getApplicationContext();
        if (context == null) {
            throw new IllegalArgumentException("context not associated with any application (using a mock context?)");
        }
        NfcManager manager = (NfcManager) context.getSystemService(System.RADIO_NFC);
        if (manager == null) {
            return null;
        }
        return manager.getDefaultAdapter();
    }

    @Deprecated
    public static NfcAdapter getDefaultAdapter() {
        Log.w(TAG, "WARNING: NfcAdapter.getDefaultAdapter() is deprecated, use NfcAdapter.getDefaultAdapter(Context) instead", new Exception());
        return getNfcAdapter(null);
    }

    NfcAdapter(Context context) {
        this.mForegroundDispatchListener = new OnActivityPausedListener() {
            public void onPaused(Activity activity) {
                NfcAdapter.this.disableForegroundDispatchInternal(activity, true);
            }
        };
        this.mContext = context;
        this.mNfcActivityManager = new NfcActivityManager(this);
        this.mNfcUnlockHandlers = new HashMap();
        this.mTagRemovedListener = null;
        this.mLock = new Object();
    }

    public Context getContext() {
        return this.mContext;
    }

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
        boolean z = true;
        try {
            if (sService.getState() != STATE_ON) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            try {
                if (sService.getState() != STATE_ON) {
                    z = false;
                }
                return z;
            } catch (RemoteException e2) {
                return false;
            }
        }
    }

    public int getAdapterState() {
        try {
            return sService.getState();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return STATE_OFF;
        }
    }

    public boolean enable() {
        try {
            return sService.enable();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean disable() {
        try {
            return sService.disable(true);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean disable(boolean persist) {
        try {
            return sService.disable(persist);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
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

    public void setBeamPushUris(Uri[] uris, Activity activity) {
        if (activity == null) {
            throw new NullPointerException("activity cannot be null");
        }
        if (uris != null) {
            int length = uris.length;
            for (int i = SWITCH_CE_SUCCESS; i < length; i += STATE_OFF) {
                Uri uri = uris[i];
                if (uri == null) {
                    throw new NullPointerException("Uri not allowed to be null");
                }
                String scheme = uri.getScheme();
                if (scheme == null || !(scheme.equalsIgnoreCase(WifiManager.EXTRA_PASSPOINT_ICON_FILE) || scheme.equalsIgnoreCase(VoiceInteractionSession.KEY_CONTENT))) {
                    throw new IllegalArgumentException("URI needs to have either scheme file or scheme content");
                }
            }
        }
        this.mNfcActivityManager.setNdefPushContentUri(activity, uris);
    }

    public void setBeamPushUrisCallback(CreateBeamUrisCallback callback, Activity activity) {
        if (activity == null) {
            throw new NullPointerException("activity cannot be null");
        }
        this.mNfcActivityManager.setNdefPushContentUriCallback(activity, callback);
    }

    public void setNdefPushMessage(NdefMessage message, Activity activity, Activity... activities) {
        int targetSdkVersion = getSdkVersion();
        if (activity == null) {
            try {
                throw new NullPointerException("activity cannot be null");
            } catch (IllegalStateException e) {
                if (targetSdkVersion < FLAG_READER_NFC_BARCODE) {
                    Log.e(TAG, "Cannot call API with Activity that has already been destroyed", e);
                    return;
                }
                throw e;
            }
        }
        this.mNfcActivityManager.setNdefPushMessage(activity, message, SWITCH_CE_SUCCESS);
        int length = activities.length;
        for (int i = SWITCH_CE_SUCCESS; i < length; i += STATE_OFF) {
            Activity a = activities[i];
            if (a == null) {
                throw new NullPointerException("activities cannot contain null");
            }
            this.mNfcActivityManager.setNdefPushMessage(a, message, SWITCH_CE_SUCCESS);
        }
    }

    public void setNdefPushMessage(NdefMessage message, Activity activity, int flags) {
        if (activity == null) {
            throw new NullPointerException("activity cannot be null");
        }
        this.mNfcActivityManager.setNdefPushMessage(activity, message, flags);
    }

    public void setNdefPushMessageCallback(CreateNdefMessageCallback callback, Activity activity, Activity... activities) {
        int targetSdkVersion = getSdkVersion();
        if (activity == null) {
            try {
                throw new NullPointerException("activity cannot be null");
            } catch (IllegalStateException e) {
                if (targetSdkVersion < FLAG_READER_NFC_BARCODE) {
                    Log.e(TAG, "Cannot call API with Activity that has already been destroyed", e);
                    return;
                }
                throw e;
            }
        }
        this.mNfcActivityManager.setNdefPushMessageCallback(activity, callback, SWITCH_CE_SUCCESS);
        int length = activities.length;
        for (int i = SWITCH_CE_SUCCESS; i < length; i += STATE_OFF) {
            Activity a = activities[i];
            if (a == null) {
                throw new NullPointerException("activities cannot contain null");
            }
            this.mNfcActivityManager.setNdefPushMessageCallback(a, callback, SWITCH_CE_SUCCESS);
        }
    }

    public void setNdefPushMessageCallback(CreateNdefMessageCallback callback, Activity activity, int flags) {
        if (activity == null) {
            throw new NullPointerException("activity cannot be null");
        }
        this.mNfcActivityManager.setNdefPushMessageCallback(activity, callback, flags);
    }

    public void setOnNdefPushCompleteCallback(OnNdefPushCompleteCallback callback, Activity activity, Activity... activities) {
        int targetSdkVersion = getSdkVersion();
        if (activity == null) {
            try {
                throw new NullPointerException("activity cannot be null");
            } catch (IllegalStateException e) {
                if (targetSdkVersion < FLAG_READER_NFC_BARCODE) {
                    Log.e(TAG, "Cannot call API with Activity that has already been destroyed", e);
                    return;
                }
                throw e;
            }
        }
        this.mNfcActivityManager.setOnNdefPushCompleteCallback(activity, callback);
        int length = activities.length;
        for (int i = SWITCH_CE_SUCCESS; i < length; i += STATE_OFF) {
            Activity a = activities[i];
            if (a == null) {
                throw new NullPointerException("activities cannot contain null");
            }
            this.mNfcActivityManager.setOnNdefPushCompleteCallback(a, callback);
        }
    }

    public void enableForegroundDispatch(Activity activity, PendingIntent intent, IntentFilter[] filters, String[][] techLists) {
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
                    return;
                }
            }
            ActivityThread.currentActivityThread().registerOnActivityPausedListener(activity, this.mForegroundDispatchListener);
            sService.setForegroundDispatch(intent, filters, parcel);
        } else {
            throw new IllegalStateException("Foreground dispatch can only be enabled when your activity is resumed");
        }
    }

    public void disableForegroundDispatch(Activity activity) {
        ActivityThread.currentActivityThread().unregisterOnActivityPausedListener(activity, this.mForegroundDispatchListener);
        disableForegroundDispatchInternal(activity, false);
    }

    void disableForegroundDispatchInternal(Activity activity, boolean force) {
        try {
            sService.setForegroundDispatch(null, null, null);
            if (!force && !activity.isResumed()) {
                throw new IllegalStateException("You must disable foreground dispatching while your activity is still resumed");
            }
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void enableReaderMode(Activity activity, ReaderCallback callback, int flags, Bundle extras) {
        this.mNfcActivityManager.enableReaderMode(activity, callback, flags, extras);
    }

    public void disableReaderMode(Activity activity) {
        this.mNfcActivityManager.disableReaderMode(activity);
    }

    public boolean invokeBeam(Activity activity) {
        if (activity == null) {
            throw new NullPointerException("activity may not be null.");
        }
        enforceResumed(activity);
        try {
            sService.invokeBeam();
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "invokeBeam: NFC process has died.");
            attemptDeadServiceRecovery(e);
            return false;
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

    @Deprecated
    public void enableForegroundNdefPush(Activity activity, NdefMessage message) {
        if (activity == null || message == null) {
            throw new NullPointerException();
        }
        enforceResumed(activity);
        this.mNfcActivityManager.setNdefPushMessage(activity, message, SWITCH_CE_SUCCESS);
    }

    @Deprecated
    public void disableForegroundNdefPush(Activity activity) {
        if (activity == null) {
            throw new NullPointerException();
        }
        enforceResumed(activity);
        this.mNfcActivityManager.setNdefPushMessage(activity, null, SWITCH_CE_SUCCESS);
        this.mNfcActivityManager.setNdefPushMessageCallback(activity, null, SWITCH_CE_SUCCESS);
        this.mNfcActivityManager.setOnNdefPushCompleteCallback(activity, null);
    }

    public boolean enableNdefPush() {
        try {
            return sService.enableNdefPush();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean disableNdefPush() {
        try {
            return sService.disableNdefPush();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean isNdefPushEnabled() {
        try {
            return sService.isNdefPushEnabled();
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public boolean ignore(Tag tag, int debounceMs, OnTagRemovedListener tagRemovedListener, Handler handler) {
        ITagRemovedCallback iTagRemovedCallback = null;
        if (tagRemovedListener != null) {
            iTagRemovedCallback = new AnonymousClass2(handler, tagRemovedListener);
        }
        synchronized (this.mLock) {
            this.mTagRemovedListener = iTagRemovedCallback;
        }
        try {
            return sService.ignore(tag.getServiceHandle(), debounceMs, iTagRemovedCallback);
        } catch (RemoteException e) {
            return false;
        }
    }

    public void dispatch(Tag tag) {
        if (tag == null) {
            throw new NullPointerException("tag cannot be null");
        }
        try {
            sService.dispatch(tag);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public void setP2pModes(int initiatorModes, int targetModes) {
        try {
            sService.setP2pModes(initiatorModes, targetModes);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public boolean addNfcUnlockHandler(NfcUnlockHandler unlockHandler, String[] tagTechnologies) {
        if (tagTechnologies.length == 0) {
            return false;
        }
        try {
            synchronized (this.mLock) {
                if (this.mNfcUnlockHandlers.containsKey(unlockHandler)) {
                    sService.removeNfcUnlockHandler((INfcUnlockHandler) this.mNfcUnlockHandlers.get(unlockHandler));
                    this.mNfcUnlockHandlers.remove(unlockHandler);
                }
                INfcUnlockHandler.Stub iHandler = new AnonymousClass3(unlockHandler);
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

    public boolean removeNfcUnlockHandler(NfcUnlockHandler unlockHandler) {
        try {
            synchronized (this.mLock) {
                if (this.mNfcUnlockHandlers.containsKey(unlockHandler)) {
                    sService.removeNfcUnlockHandler((INfcUnlockHandler) this.mNfcUnlockHandlers.remove(unlockHandler));
                }
            }
            return true;
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return false;
        }
    }

    public INfcAdapterExtras getNfcAdapterExtrasInterface() {
        if (this.mContext == null) {
            throw new UnsupportedOperationException("You need a context on NfcAdapter to use the  NFC extras APIs");
        }
        try {
            return sService.getNfcAdapterExtrasInterface(this.mContext.getPackageName());
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    void enforceResumed(Activity activity) {
        if (!activity.isResumed()) {
            throw new IllegalStateException("API cannot be called while activity is paused");
        }
    }

    int getSdkVersion() {
        if (this.mContext == null) {
            return 9;
        }
        return this.mContext.getApplicationInfo().targetSdkVersion;
    }

    public int getSelectedCardEmulation() {
        int ret = SWITCH_CE_FAILED;
        try {
            ret = sService.getSelectedCardEmulation();
        } catch (RemoteException e) {
            Log.e(TAG, "get selected ce failed!");
        }
        return ret;
    }

    public void selectCardEmulation(int sub) {
        try {
            sService.selectCardEmulation(sub);
        } catch (RemoteException e) {
            Log.e(TAG, "select ce failed!");
        }
    }

    public int getSupportCardEmulation() {
        int ret = SWITCH_CE_FAILED;
        try {
            ret = sService.getSupportCardEmulation();
        } catch (RemoteException e) {
            Log.e(TAG, "get support ce failed!");
        }
        return ret;
    }

    public String getFirmwareVersion() {
        String version = null;
        try {
            version = sService.getFirmwareVersion();
        } catch (RemoteException e) {
            Log.e(TAG, "get Firmware Version failed!");
        }
        return version;
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
