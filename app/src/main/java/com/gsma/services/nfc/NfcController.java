package com.gsma.services.nfc;

import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.cardemulation.AidGroup;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.gsma.services.utils.InsufficientResourcesException;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import com.nxp.nfc.gsma.internal.NxpNfcController.NxpCallbacks;
import com.nxp.nfc.gsma.internal.NxpOffHostService;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NfcController {
    public static final int BATTERY_ALL_STATES = 2;
    public static final int BATTERY_OPERATIONAL_STATE = 1;
    public static final int PROTOCOL_ISO_DEP = 16;
    public static final int SCREEN_ALL_MODES = 2;
    public static final int SCREEN_ON_AND_LOCKED_MODE = 1;
    static final String TAG = "NfcController";
    public static final int TECHNOLOGY_NFC_A = 1;
    public static final int TECHNOLOGY_NFC_B = 2;
    public static final int TECHNOLOGY_NFC_F = 4;
    private static HashMap<Context, NfcController> sNfcController;
    private Callbacks mCb;
    private Context mContext;
    private boolean mIsHceCapable;
    NxpNfcControllerCallback mNxpCallback;
    private NxpNfcController mNxpNfcController;
    private ArrayList<OffHostService> mOffHostServiceList;
    private HashMap<String, OffHostService> mOffhostService;
    private int mUserId;

    public interface Callbacks {
        public static final int CARD_EMULATION_DISABLED = 0;
        public static final int CARD_EMULATION_ENABLED = 1;
        public static final int CARD_EMULATION_ERROR = 256;

        void onCardEmulationMode(int i);

        void onEnableNfcController(boolean z);

        void onGetDefaultController(NfcController nfcController);
    }

    public class NxpNfcControllerCallback implements NxpCallbacks {
        public void onNxpEnableNfcController(boolean success) {
            if (success) {
                NfcController.this.mCb.onEnableNfcController(true);
                Log.d(NfcController.TAG, "NFC Enabled");
                return;
            }
            NfcController.this.mCb.onEnableNfcController(false);
            Log.d(NfcController.TAG, "NFC Not Enabled");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.gsma.services.nfc.NfcController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.gsma.services.nfc.NfcController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.gsma.services.nfc.NfcController.<clinit>():void");
    }

    NfcController() {
        this.mOffHostServiceList = new ArrayList();
        this.mNxpNfcController = null;
        this.mOffhostService = new HashMap();
        this.mContext = null;
        this.mNxpCallback = null;
        this.mIsHceCapable = false;
        this.mNxpNfcController = new NxpNfcController();
        this.mUserId = UserHandle.myUserId();
        this.mNxpCallback = new NxpNfcControllerCallback();
    }

    NfcController(Context context) {
        boolean z;
        this.mOffHostServiceList = new ArrayList();
        this.mNxpNfcController = null;
        this.mOffhostService = new HashMap();
        this.mContext = null;
        this.mNxpCallback = null;
        this.mIsHceCapable = false;
        this.mNxpNfcController = new NxpNfcController(context);
        this.mContext = context;
        this.mUserId = UserHandle.myUserId();
        this.mNxpCallback = new NxpNfcControllerCallback();
        PackageManager pm = this.mContext.getPackageManager();
        if (pm.hasSystemFeature("android.hardware.nfc.hce")) {
            z = true;
        } else {
            z = pm.hasSystemFeature("android.hardware.nfc.hcef");
        }
        this.mIsHceCapable = z;
    }

    public static void getDefaultController(Context context, Callbacks callbacks) {
        if (context == null || callbacks == null) {
            throw new IllegalArgumentException("context or NfcController.Callbacks cannot be null");
        }
        callbacks.onGetDefaultController(new NfcController(context));
    }

    public boolean isEnabled() {
        return this.mNxpNfcController.isNxpNfcEnabled();
    }

    public void enableNfcController(Callbacks cb) {
        this.mCb = cb;
        if (isEnabled()) {
            this.mCb.onEnableNfcController(true);
        } else {
            this.mNxpNfcController.enableNxpNfcController(this.mNxpCallback);
        }
    }

    @Deprecated
    public boolean isCardEmulationEnabled() throws Exception {
        throw new InsufficientResourcesException("Host Card Emulation (HCE) is supported");
    }

    @Deprecated
    public void enableCardEmulationMode(Callbacks cb) throws IllegalStateException, SecurityException, InsufficientResourcesException {
        if (!isEnabled()) {
            throw new IllegalStateException("Nfc is not enabled");
        } else if (this.mIsHceCapable) {
            throw new InsufficientResourcesException("Host Card Emulation (HCE) is supported");
        } else {
            throw new SecurityException("Can not use this API");
        }
    }

    @Deprecated
    public void disableCardEmulationMode(Callbacks cb) throws SecurityException, InsufficientResourcesException {
        if (this.mIsHceCapable) {
            throw new InsufficientResourcesException("Host Card Emulation (HCE) is supported");
        }
        throw new SecurityException("Can not use this API");
    }

    private String getRandomString() {
        return new String("service" + Integer.toString(new SecureRandom().nextInt(10000) + 10000));
    }

    public OffHostService defineOffHostService(String description, String SEName) throws UnsupportedOperationException, IllegalArgumentException {
        if (this.mIsHceCapable) {
            Log.d(TAG, "defineOffHostService description=" + description + " SEName=" + SEName);
            if (description == null) {
                throw new IllegalArgumentException("description cannot be null");
            } else if (SEName == null || !(SEName.startsWith("SIM") || SEName.startsWith("eSE"))) {
                throw new IllegalArgumentException("SEName error");
            } else {
                NxpOffHostService offHostService = new NxpOffHostService(this.mUserId, description, SEName, this.mContext.getPackageName(), TextUtils.isEmpty(description) ? getRandomString() : description, true);
                Log.d(TAG, "defineOffHostService, new offHostService is " + offHostService.toString());
                offHostService.setContext(this.mContext);
                offHostService.setNxpNfcController(this.mNxpNfcController);
                return new OffHostService(offHostService);
            }
        }
        throw new UnsupportedOperationException("HCE is not supported");
    }

    public void deleteOffHostService(OffHostService service) throws IllegalArgumentException, UnsupportedOperationException {
        if (service == null) {
            throw new IllegalArgumentException("service null");
        }
        String packageName = this.mContext.getPackageName();
        Log.d(TAG, "deleteOffHostService service:" + service.toString() + ",packageName:" + packageName);
        if (service.getModifiable()) {
            this.mNxpNfcController.deleteOffHostService(this.mUserId, packageName, convertToNxpOffhostService(service));
            return;
        }
        throw new UnsupportedOperationException("service static");
    }

    public OffHostService[] getOffHostServices() {
        String packageName = this.mContext.getPackageName();
        Log.d(TAG, "getOffHostServices packageName=" + packageName);
        ArrayList<NxpOffHostService> mNxpOffhost = this.mNxpNfcController.getOffHostServices(this.mUserId, packageName);
        ArrayList<OffHostService> mOffHostList = new ArrayList();
        for (NxpOffHostService mHost : mNxpOffhost) {
            mOffHostList.add(new OffHostService(mHost));
        }
        if (mOffHostList.size() == 0) {
            return null;
        }
        return (OffHostService[]) mOffHostList.toArray(new OffHostService[mOffHostList.size()]);
    }

    public OffHostService getDefaultOffHostService() {
        Log.d(TAG, "getDefaultOffHostService begin");
        NxpOffHostService service = this.mNxpNfcController.getDefaultOffHostService(this.mUserId, this.mContext.getPackageName());
        if (service != null) {
            Log.d(TAG, "service != null getDefaultOffHostService end");
            return new OffHostService(service);
        }
        Log.d(TAG, "getDefaultOffHostService service is null");
        return null;
    }

    private ArrayList<AidGroup> convertToCeAidGroupList(List<AidGroup> mAidGroups) {
        ArrayList<AidGroup> mApduAidGroupList = new ArrayList();
        List<String> aidList = new ArrayList();
        for (AidGroup mGroup : mAidGroups) {
            AidGroup mCeAidGroup = new AidGroup(mGroup.getCategory(), mGroup.getDescription());
            aidList = mCeAidGroup.getAids();
            for (String aid : mGroup.getAidList()) {
                aidList.add(aid);
            }
            mApduAidGroupList.add(mCeAidGroup);
        }
        return mApduAidGroupList;
    }

    private NxpOffHostService convertToNxpOffhostService(OffHostService service) {
        ArrayList<AidGroup> mAidGroupList = convertToCeAidGroupList(service.mAidGroupList);
        NxpOffHostService mNxpOffHostService = new NxpOffHostService(service.mUserId, service.mDescription, service.mSEName, service.mPackageName, service.mServiceName, service.mModifiable);
        mNxpOffHostService.setContext(this.mContext);
        mNxpOffHostService.setBannerId(service.mBannerResId);
        mNxpOffHostService.mAidGroupList.addAll(mAidGroupList);
        return mNxpOffHostService;
    }
}
