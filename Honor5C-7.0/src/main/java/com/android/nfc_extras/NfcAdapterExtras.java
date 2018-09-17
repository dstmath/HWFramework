package com.android.nfc_extras;

import android.nfc.INfcAdapterExtras;
import android.nfc.NfcAdapter;
import android.os.RemoteException;
import android.util.Log;
import java.util.HashMap;

public final class NfcAdapterExtras {
    public static final String ACTION_RF_FIELD_OFF_DETECTED = "com.android.nfc_extras.action.RF_FIELD_OFF_DETECTED";
    public static final String ACTION_RF_FIELD_ON_DETECTED = "com.android.nfc_extras.action.RF_FIELD_ON_DETECTED";
    private static final CardEmulationRoute ROUTE_OFF = null;
    private static final String TAG = "NfcAdapterExtras";
    private static final HashMap<NfcAdapter, NfcAdapterExtras> sNfcExtras = null;
    private static INfcAdapterExtras sService;
    private final NfcAdapter mAdapter;
    private final NfcExecutionEnvironment mEmbeddedEe;
    final String mPackageName;
    private final CardEmulationRoute mRouteOnWhenScreenOn;

    public static final class CardEmulationRoute {
        public static final int ROUTE_OFF = 1;
        public static final int ROUTE_ON_WHEN_SCREEN_ON = 2;
        public final NfcExecutionEnvironment nfcEe;
        public final int route;

        public CardEmulationRoute(int route, NfcExecutionEnvironment nfcEe) {
            if (route == ROUTE_OFF && nfcEe != null) {
                throw new IllegalArgumentException("must not specifiy a NFC-EE with ROUTE_OFF");
            } else if (route == ROUTE_OFF || nfcEe != null) {
                this.route = route;
                this.nfcEe = nfcEe;
            } else {
                throw new IllegalArgumentException("must specifiy a NFC-EE for this route");
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.nfc_extras.NfcAdapterExtras.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.nfc_extras.NfcAdapterExtras.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.nfc_extras.NfcAdapterExtras.<clinit>():void");
    }

    private static void initService(NfcAdapter adapter) {
        INfcAdapterExtras service = adapter.getNfcAdapterExtrasInterface();
        if (service != null) {
            sService = service;
        }
    }

    public static NfcAdapterExtras get(NfcAdapter adapter) {
        if (adapter.getContext() == null) {
            throw new UnsupportedOperationException("You must pass a context to your NfcAdapter to use the NFC extras APIs");
        }
        NfcAdapterExtras extras;
        synchronized (NfcAdapterExtras.class) {
            if (sService == null) {
                initService(adapter);
            }
            extras = (NfcAdapterExtras) sNfcExtras.get(adapter);
            if (extras == null) {
                extras = new NfcAdapterExtras(adapter);
                sNfcExtras.put(adapter, extras);
            }
        }
        return extras;
    }

    private NfcAdapterExtras(NfcAdapter adapter) {
        this.mAdapter = adapter;
        this.mPackageName = adapter.getContext().getPackageName();
        this.mEmbeddedEe = new NfcExecutionEnvironment(this);
        this.mRouteOnWhenScreenOn = new CardEmulationRoute(2, this.mEmbeddedEe);
    }

    void attemptDeadServiceRecovery(Exception e) {
        Log.e(TAG, "NFC Adapter Extras dead - attempting to recover");
        this.mAdapter.attemptDeadServiceRecovery(e);
        initService(this.mAdapter);
    }

    INfcAdapterExtras getService() {
        return sService;
    }

    public CardEmulationRoute getCardEmulationRoute() {
        try {
            CardEmulationRoute cardEmulationRoute;
            if (sService.getCardEmulationRoute(this.mPackageName) == 1) {
                cardEmulationRoute = ROUTE_OFF;
            } else {
                cardEmulationRoute = this.mRouteOnWhenScreenOn;
            }
            return cardEmulationRoute;
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return ROUTE_OFF;
        }
    }

    public void setCardEmulationRoute(CardEmulationRoute route) {
        try {
            sService.setCardEmulationRoute(this.mPackageName, route.route);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public NfcExecutionEnvironment getEmbeddedExecutionEnvironment() {
        return this.mEmbeddedEe;
    }

    public void authenticate(byte[] token) {
        try {
            sService.authenticate(this.mPackageName, token);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
        }
    }

    public String getDriverName() {
        try {
            return sService.getDriverName(this.mPackageName);
        } catch (RemoteException e) {
            attemptDeadServiceRecovery(e);
            return "";
        }
    }
}
