package android.nfc.cardemulation;

import android.app.Activity;
import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.nfc.INfcCardEmulation;
import android.nfc.NfcAdapter;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import java.util.HashMap;
import java.util.List;

public final class CardEmulation {
    public static final String ACTION_CHANGE_DEFAULT = "android.nfc.cardemulation.action.ACTION_CHANGE_DEFAULT";
    public static final String CATEGORY_OTHER = "other";
    public static final String CATEGORY_PAYMENT = "payment";
    public static final String DHCE_ACTION_AID_TABLE_UPDATED = "org.mobilenfcassociation.CardEmulation.action.AID_TABLE_UPDATED";
    public static final int DHCE_PAYMENT_MISMATCH_CLEAR = 1;
    public static final int DHCE_PAYMENT_MISMATCH_NO_CLEAR = 0;
    public static final int DHCE_REQUESTER_ACD = 1;
    public static final int DHCE_REQUESTER_TP = 0;
    public static final int DHCE_STS_ATS_INVALID_PARAM = -2;
    public static final int DHCE_STS_ATS_NO_AID_TBL_MAX = -3;
    public static final int DHCE_STS_ATS_OTHER_ERROR = -1;
    public static final int DHCE_STS_B_INVALID_PARAM = 9157;
    public static final int DHCE_STS_B_NO_OFF_HOST_AID = 8102;
    public static final int DHCE_STS_B_OK = 65;
    public static final int DHCE_STS_B_OTHER_ERROR = 9159;
    public static final int DHCE_STS_B_UIM_LOCKED = 9153;
    public static final int DHCE_STS_B_UIM_NONE = 9151;
    public static final int DHCE_STS_B_UIM_TIMEOUT = 9158;
    public static final int DHCE_STS_B_UIM_UNSUPPORTED = 9152;
    public static final int DHCE_STS_F_AID_TABLE_OVERFLOW = 9292;
    public static final int DHCE_STS_F_INVALID_PARAM = 9297;
    public static final int DHCE_STS_F_NFC_LOCKED = 9291;
    public static final int DHCE_STS_F_OK = 129;
    public static final int DHCE_STS_F_OTHER_ERROR = 9299;
    public static final int DHCE_STS_F_TIMEOUT = 9298;
    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_SERVICE_COMPONENT = "component";
    public static final int SELECTION_MODE_ALWAYS_ASK = 1;
    public static final int SELECTION_MODE_ASK_IF_CONFLICT = 2;
    public static final int SELECTION_MODE_PREFER_DEFAULT = 0;
    static final String TAG = "CardEmulation";
    static HashMap<Context, CardEmulation> sCardEmus = new HashMap<>();
    static boolean sIsInitialized = false;
    static INfcCardEmulation sService;
    final Context mContext;

    private CardEmulation(Context context, INfcCardEmulation service) {
        this.mContext = context.getApplicationContext();
        sService = service;
    }

    public static synchronized CardEmulation getInstance(NfcAdapter adapter) {
        CardEmulation manager;
        synchronized (CardEmulation.class) {
            if (adapter != null) {
                Context context = adapter.getContext();
                if (context != null) {
                    if (!sIsInitialized) {
                        IPackageManager pm = ActivityThread.getPackageManager();
                        if (pm != null) {
                            try {
                                if (pm.hasSystemFeature("android.hardware.nfc.hce", 0)) {
                                    sIsInitialized = true;
                                } else {
                                    Log.e(TAG, "This device does not support card emulation");
                                    throw new UnsupportedOperationException();
                                }
                            } catch (RemoteException e) {
                                Log.e(TAG, "PackageManager query failed.");
                                throw new UnsupportedOperationException();
                            }
                        } else {
                            Log.e(TAG, "Cannot get PackageManager");
                            throw new UnsupportedOperationException();
                        }
                    }
                    manager = sCardEmus.get(context);
                    if (manager == null) {
                        INfcCardEmulation service = adapter.getCardEmulationService();
                        if (service != null) {
                            manager = new CardEmulation(context, service);
                            sCardEmus.put(context, manager);
                        } else {
                            Log.e(TAG, "This device does not implement the INfcCardEmulation interface.");
                            throw new UnsupportedOperationException();
                        }
                    }
                } else {
                    Log.e(TAG, "NfcAdapter context is null.");
                    throw new UnsupportedOperationException();
                }
            } else {
                throw new NullPointerException("NfcAdapter is null");
            }
        }
        return manager;
    }

    public boolean isDefaultServiceForCategory(ComponentName service, String category) {
        try {
            return sService.isDefaultServiceForCategory(this.mContext.getUserId(), service, category);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
            try {
                return sService.isDefaultServiceForCategory(this.mContext.getUserId(), service, category);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
        }
    }

    public boolean isDefaultServiceForAid(ComponentName service, String aid) {
        try {
            return sService.isDefaultServiceForAid(this.mContext.getUserId(), service, aid);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
            try {
                return sService.isDefaultServiceForAid(this.mContext.getUserId(), service, aid);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return false;
            }
        }
    }

    public boolean categoryAllowsForegroundPreference(String category) {
        if (!CATEGORY_PAYMENT.equals(category)) {
            return true;
        }
        boolean z = false;
        boolean preferForeground = false;
        try {
            if (Settings.Secure.getInt(this.mContext.getContentResolver(), Settings.Secure.NFC_PAYMENT_FOREGROUND) != 0) {
                z = true;
            }
            preferForeground = z;
        } catch (Settings.SettingNotFoundException e) {
        }
        return preferForeground;
    }

    public int getSelectionModeForCategory(String category) {
        if (!CATEGORY_PAYMENT.equals(category)) {
            return 2;
        }
        if (Settings.Secure.getString(this.mContext.getContentResolver(), Settings.Secure.NFC_PAYMENT_DEFAULT_COMPONENT) != null) {
            return 0;
        }
        return 1;
    }

    public boolean registerAidsForService(ComponentName service, String category, List<String> aids) {
        AidGroup aidGroup = new AidGroup(aids, category);
        try {
            return sService.registerAidGroupForService(this.mContext.getUserId(), service, aidGroup);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
            try {
                return sService.registerAidGroupForService(this.mContext.getUserId(), service, aidGroup);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return false;
            }
        }
    }

    public List<String> getAidsForService(ComponentName service, String category) {
        List<String> list = null;
        try {
            AidGroup group = sService.getAidGroupForService(this.mContext.getUserId(), service, category);
            if (group != null) {
                list = group.getAids();
            }
            return list;
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return null;
            }
            try {
                AidGroup group2 = sService.getAidGroupForService(this.mContext.getUserId(), service, category);
                if (group2 != null) {
                    list = group2.getAids();
                }
                return list;
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return null;
            }
        }
    }

    public boolean removeAidsForService(ComponentName service, String category) {
        try {
            return sService.removeAidGroupForService(this.mContext.getUserId(), service, category);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
            try {
                return sService.removeAidGroupForService(this.mContext.getUserId(), service, category);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return false;
            }
        }
    }

    public boolean setPreferredService(Activity activity, ComponentName service) {
        if (activity == null || service == null) {
            throw new NullPointerException("activity or service or category is null");
        } else if (activity.isResumed()) {
            try {
                return sService.setPreferredService(service);
            } catch (RemoteException e) {
                recoverService();
                if (sService == null) {
                    Log.e(TAG, "Failed to recover CardEmulationService.");
                    return false;
                }
                try {
                    return sService.setPreferredService(service);
                } catch (RemoteException e2) {
                    Log.e(TAG, "Failed to reach CardEmulationService.");
                    return false;
                }
            }
        } else {
            throw new IllegalArgumentException("Activity must be resumed.");
        }
    }

    public boolean unsetPreferredService(Activity activity) {
        if (activity == null) {
            throw new NullPointerException("activity is null");
        } else if (activity.isResumed()) {
            try {
                return sService.unsetPreferredService();
            } catch (RemoteException e) {
                recoverService();
                if (sService == null) {
                    Log.e(TAG, "Failed to recover CardEmulationService.");
                    return false;
                }
                try {
                    return sService.unsetPreferredService();
                } catch (RemoteException e2) {
                    Log.e(TAG, "Failed to reach CardEmulationService.");
                    return false;
                }
            }
        } else {
            throw new IllegalArgumentException("Activity must be resumed.");
        }
    }

    public boolean supportsAidPrefixRegistration() {
        try {
            return sService.supportsAidPrefixRegistration();
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
            try {
                return sService.supportsAidPrefixRegistration();
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return false;
            }
        }
    }

    public boolean setDefaultServiceForCategory(ComponentName service, String category) {
        try {
            return sService.setDefaultServiceForCategory(this.mContext.getUserId(), service, category);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
            try {
                return sService.setDefaultServiceForCategory(this.mContext.getUserId(), service, category);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return false;
            }
        }
    }

    public boolean setDefaultForNextTap(ComponentName service) {
        try {
            return sService.setDefaultForNextTap(this.mContext.getUserId(), service);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
            try {
                return sService.setDefaultForNextTap(this.mContext.getUserId(), service);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return false;
            }
        }
    }

    public List<ApduServiceInfo> getServices(String category) {
        try {
            return sService.getServices(this.mContext.getUserId(), category);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return null;
            }
            try {
                return sService.getServices(this.mContext.getUserId(), category);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return null;
            }
        }
    }

    public static boolean isValidAid(String aid) {
        if (aid == null) {
            return false;
        }
        if ((aid.endsWith("*") || aid.endsWith("#")) && aid.length() % 2 == 0) {
            Log.e(TAG, "AID " + aid + " is not a valid AID.");
            return false;
        } else if (!aid.endsWith("*") && !aid.endsWith("#") && aid.length() % 2 != 0) {
            Log.e(TAG, "AID " + aid + " is not a valid AID.");
            return false;
        } else if (aid.matches("[0-9A-Fa-f]{10,32}\\*?\\#?")) {
            return true;
        } else {
            Log.e(TAG, "AID " + aid + " is not a valid AID.");
            return false;
        }
    }

    public static boolean isValidApduString(String apdu) {
        if (apdu == null) {
            return false;
        }
        if (apdu.length() < 2 || apdu.length() > 248 || apdu.length() % 2 != 0) {
            Log.e(TAG, "APDU " + apdu + " is not a valid apdu pattern.");
            return false;
        } else if (apdu.matches("[0-9A-Fa-f]{10,32}")) {
            return true;
        } else {
            Log.e(TAG, "APDU " + apdu + " is not a valid apdu pattern.");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void recoverService() {
        sService = NfcAdapter.getDefaultAdapter(this.mContext).getCardEmulationService();
    }

    public static final boolean dhce_isStsOk(int status) {
        if (status >= 64 && status <= 71) {
            return true;
        }
        if (status >= 128 && status <= 135) {
            return true;
        }
        if (status < 192 || status > 199) {
            return false;
        }
        return true;
    }

    public int registerService2015(ComponentName app, String category) {
        registerService(app, category, 0);
        return 129;
    }

    public int unregisterOtherService2015(ComponentName app) {
        unregisterOtherService(app);
        return 129;
    }

    public void registerService(ComponentName app, String category, int requester) {
        try {
            sService.registerService(UserHandle.myUserId(), app, category, requester);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return;
            }
            try {
                sService.registerService(UserHandle.myUserId(), app, category, requester);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
            }
        }
    }

    public void unregisterOtherService(ComponentName app) {
        try {
            sService.unregisterOtherService(UserHandle.myUserId(), app);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return;
            }
            try {
                sService.unregisterOtherService(UserHandle.myUserId(), app);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
            }
        }
    }

    public boolean isRegisteredService(ComponentName app, String category) {
        try {
            return sService.isRegisteredService(UserHandle.myUserId(), app, category);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return false;
            }
            try {
                return sService.isRegisteredService(UserHandle.myUserId(), app, category);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return false;
            }
        }
    }

    public int getUsedAidTableSizeInPercent(String category) {
        if (category == null) {
            return -2;
        }
        if (!category.equals(CATEGORY_PAYMENT) && !category.equals("other")) {
            return -2;
        }
        try {
            return sService.getUsedAidTableSizeInPercent(UserHandle.myUserId(), category);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return -1;
            }
            try {
                return sService.getUsedAidTableSizeInPercent(UserHandle.myUserId(), category);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return -1;
            }
        }
    }

    public void initializePaymentDefault(int necessity) {
        try {
            sService.initializePaymentDefault(UserHandle.myUserId(), necessity);
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return;
            }
            try {
                sService.initializePaymentDefault(UserHandle.myUserId(), necessity);
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
            }
        }
    }

    public ComponentName getPaymentPriority() {
        try {
            return sService.getPaymentPriority(UserHandle.myUserId());
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return null;
            }
            try {
                return sService.getPaymentPriority(UserHandle.myUserId());
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return null;
            }
        }
    }

    public ApduServiceInfo getPaymentDefaultServiceInfo() {
        try {
            return sService.getPaymentDefaultServiceInfo(UserHandle.myUserId());
        } catch (RemoteException e) {
            recoverService();
            if (sService == null) {
                Log.e(TAG, "Failed to recover CardEmulationService.");
                return null;
            }
            try {
                return sService.getPaymentDefaultServiceInfo(UserHandle.myUserId());
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to reach CardEmulationService.");
                return null;
            }
        }
    }
}
