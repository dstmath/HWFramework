package ohos.nfc.cardemulation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import ohos.aafwk.ability.Ability;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.nfc.NfcController;
import ohos.nfc.NfcKitsUtils;
import ohos.rpc.RemoteException;

public final class CardEmulation {
    private static final Pattern AID_PATTERN = Pattern.compile("[0-9A-Fa-f]{10,32}\\*?\\#?");
    public static final String CATEGORY_OTHER = "other";
    public static final String CATEGORY_PAYMENT = "payment";
    public static final int DISABLE_MODE_A_B = 0;
    public static final String ENABLED_SE_TYPE_ESE = "11";
    public static final String ENABLED_SE_TYPE_HISEE = "12";
    public static final int ENABLE_MODE_ALL = 3;
    public static final int FEATURE_ESE = 2;
    public static final int FEATURE_HCE = 0;
    public static final int FEATURE_UICC = 1;
    public static final String HISEE_READY = "true";
    public static final String HISEE_UNREADY = "false";
    public static final String KEY_ENABLED_SE_TYPE = "ese_type";
    public static final String KEY_HISEE_READY = "hisee_ready";
    public static final String KEY_RSSI_SUPPORTED = "rssi_support";
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "CardEmulation");
    public static final int RELEASE_PRIORITY = 0;
    public static final String RSSI_QUERY_SUPPORTED = "true";
    public static final String RSSI_QUERY_UNSUPPORTED = "false";
    public static final int SELECTION_TYPE_ASK_IF_CONFLICT = 1;
    public static final int SELECTION_TYPE_PREFER_DEFAULT = 0;
    public static final int SET_PRIORITY = 1;
    private static CardEmulation sCardEmulation;
    private static Context sContext;
    private CardEmulationProxy mCardEmulationProxy;
    private NfcController mNfcController;

    private CardEmulation(NfcController nfcController) {
        this.mNfcController = nfcController;
        if (this.mNfcController == null) {
            HiLog.warn(LABEL, "mNfcController is null.", new Object[0]);
        }
        this.mCardEmulationProxy = CardEmulationProxy.getInstance();
    }

    public static synchronized CardEmulation getInstance(NfcController nfcController) {
        CardEmulation cardEmulation;
        synchronized (CardEmulation.class) {
            if (sCardEmulation == null && nfcController != null) {
                sCardEmulation = new CardEmulation(nfcController);
                sContext = nfcController.getContext();
            }
            cardEmulation = sCardEmulation;
        }
        return cardEmulation;
    }

    public boolean isSupported(int i) {
        if (i == 2) {
            try {
                return this.mCardEmulationProxy.isSupported(i);
            } catch (RemoteException unused) {
                HiLog.warn(LABEL, "isSupported RemoteException", new Object[0]);
            }
        }
        return false;
    }

    public void setListenMode(int i) {
        try {
            this.mCardEmulationProxy.setListenMode(i);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "setListenMode RemoteException", new Object[0]);
        }
    }

    public boolean isListenModeEnabled() {
        try {
            return this.mCardEmulationProxy.isListenModeEnabled();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "isListenModeEnabled RemoteException", new Object[0]);
            return false;
        }
    }

    public int getSelectionType(String str) {
        return CATEGORY_PAYMENT.equals(str) ? 0 : 1;
    }

    public String getNfcInfo(String str) {
        try {
            if (KEY_ENABLED_SE_TYPE.equals(str) || KEY_HISEE_READY.equals(str) || KEY_RSSI_SUPPORTED.equals(str)) {
                return this.mCardEmulationProxy.getNfcInfo(str);
            }
            return "";
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "getNfcInfo RemoteException", new Object[0]);
            return "";
        }
    }

    public int setRfConfig(String str) {
        String str2 = "";
        try {
            if (!(sContext == null || sContext.getAbilityInfo() == null)) {
                str2 = sContext.getAbilityInfo().getBundleName();
            }
            return this.mCardEmulationProxy.setRfConfig(str, str2);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "setRfConfig RemoteException", new Object[0]);
            return -1;
        }
    }

    public boolean registerForegroundPreferred(Ability ability, ElementName elementName) {
        if (ability == null || elementName == null) {
            throw new NullPointerException("appAbility or appName is null");
        }
        try {
            return this.mCardEmulationProxy.registerForegroundPreferred(elementName);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "registerForegroundPreferred RemoteException", new Object[0]);
            return false;
        }
    }

    public boolean unregisterForegroundPreferred(Ability ability) {
        if (ability != null) {
            try {
                return this.mCardEmulationProxy.unregisterForegroundPreferred();
            } catch (RemoteException unused) {
                HiLog.warn(LABEL, "unregisterForegroundPreferred RemoteException", new Object[0]);
                return false;
            }
        } else {
            throw new NullPointerException("appAbility is null");
        }
    }

    public boolean isDefaultForAid(ElementName elementName, String str) {
        try {
            return this.mCardEmulationProxy.isDefaultForAid(0, elementName, str);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "isDefaultForAid RemoteException", new Object[0]);
            return false;
        }
    }

    public boolean registerAids(ElementName elementName, String str, List<String> list) {
        try {
            return this.mCardEmulationProxy.registerAids(0, elementName, new AidGroup(list, str));
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "registerAids RemoteException", new Object[0]);
            return false;
        }
    }

    public boolean removeAids(ElementName elementName, String str) {
        try {
            return this.mCardEmulationProxy.removeAids(0, elementName, str);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "removeAids RemoteException", new Object[0]);
            return false;
        }
    }

    public List<String> getAids(ElementName elementName, String str) {
        try {
            AidGroup aids = this.mCardEmulationProxy.getAids(0, elementName, str);
            return aids != null ? aids.getAids() : new ArrayList();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "removeAids RemoteException", new Object[0]);
            return new ArrayList();
        }
    }

    public static boolean isAidValid(String str) {
        if (str == null) {
            return false;
        }
        if ((str.endsWith("*") || str.endsWith("#")) && str.length() % 2 == 0) {
            HiLog.error(LABEL, "aid %{public}s is not valid.", str);
            return false;
        } else if (!str.endsWith("*") && !str.endsWith("#") && str.length() % 2 != 0) {
            HiLog.error(LABEL, "aid %{public}s is not valid.", str);
            return false;
        } else if (AID_PATTERN.matcher(str).matches()) {
            return true;
        } else {
            HiLog.error(LABEL, "aid %{public}s is not valid.", str);
            return false;
        }
    }

    public boolean isDefaultForType(ElementName elementName, String str) {
        try {
            return this.mCardEmulationProxy.isDefaultForType(0, elementName, str);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "isDefaultForType RemoteException", new Object[0]);
            return false;
        }
    }
}
