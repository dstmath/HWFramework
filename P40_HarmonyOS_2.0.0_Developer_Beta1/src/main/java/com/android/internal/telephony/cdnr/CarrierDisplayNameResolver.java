package com.android.internal.telephony.cdnr;

import android.content.Context;
import android.content.res.Resources;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.SparseArray;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.cdnr.CarrierDisplayNameData;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.util.IndentingPrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CarrierDisplayNameResolver {
    private static final boolean DBG = true;
    private static final CarrierDisplayNameConditionRule DEFAULT_CARRIER_DISPLAY_NAME_RULE = new CarrierDisplayNameConditionRule(0);
    private static final int DEFAULT_CARRIER_NAME_DISPLAY_CONDITION_BITMASK = 0;
    private static final List<Integer> EF_SOURCE_PRIORITY = Arrays.asList(2, 1, 10, 3, 4, 5, 6, 7, 8, 9);
    private static final int INVALID_INDEX = -1;
    private static final int LOCAL_LOG_SIZE = 10;
    private static final String TAG = "CDNR";
    private final CarrierConfigManager mCCManager;
    private CarrierDisplayNameData mCarrierDisplayNameData;
    private final Context mContext;
    private final SparseArray<EfData> mEf = new SparseArray<>();
    private final LocalLog mLocalLog = new LocalLog(10);
    private final GsmCdmaPhone mPhone;

    public CarrierDisplayNameResolver(GsmCdmaPhone phone) {
        this.mContext = phone.getContext();
        this.mPhone = phone;
        this.mCCManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
    }

    public void updateEfFromRuim(RuimRecords ruim) {
        int key = getSourcePriority(6);
        if (ruim == null) {
            this.mEf.remove(key);
        } else {
            this.mEf.put(key, new RuimEfData(ruim));
        }
    }

    public void updateEfFromUsim(SIMRecords usim) {
        int key = getSourcePriority(3);
        if (usim == null) {
            this.mEf.remove(key);
        } else {
            this.mEf.put(key, new UsimEfData(usim));
        }
    }

    public void updateEfFromCarrierConfig(PersistableBundle config) {
        int key = getSourcePriority(1);
        if (config == null) {
            this.mEf.remove(key);
        } else {
            this.mEf.put(key, new CarrierConfigEfData(config));
        }
    }

    public void updateEfForEri(String eriText) {
        PersistableBundle config = getCarrierConfig();
        int key = getSourcePriority(10);
        if (TextUtils.isEmpty(eriText) || ((!this.mPhone.isPhoneTypeCdma() && !this.mPhone.isPhoneTypeCdmaLte()) || !config.getBoolean("allow_cdma_eri_bool"))) {
            this.mEf.remove(key);
        } else {
            this.mEf.put(key, new EriEfData(eriText));
        }
    }

    public void updateEfForBrandOverride(String operatorName) {
        int key = getSourcePriority(2);
        if (TextUtils.isEmpty(operatorName)) {
            this.mEf.remove(key);
        } else {
            this.mEf.put(key, new BrandOverrideEfData(operatorName, getServiceState().getOperatorNumeric()));
        }
    }

    public CarrierDisplayNameData getCarrierDisplayNameData() {
        resolveCarrierDisplayName();
        return this.mCarrierDisplayNameData;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.mEf.size(); i++) {
            EfData p = this.mEf.valueAt(i);
            sb.append("{spnDisplayCondition = " + p.getServiceProviderNameDisplayCondition() + ", spn = " + p.getServiceProviderName() + ", spdiList = " + p.getServiceProviderDisplayInformation() + ", pnnList = " + p.getPlmnNetworkNameList() + ", oplList = " + p.getOperatorPlmnList() + ", ehplmn = " + p.getEhplmnList() + "}, ");
        }
        sb.append(", roamingFromSS = " + getServiceState().getRoaming());
        sb.append(", registeredPLMN = " + getServiceState().getOperatorNumeric());
        return sb.toString();
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("CDNR:");
        pw.increaseIndent();
        pw.println("fields = " + toString());
        pw.println("carrierDisplayNameData = " + this.mCarrierDisplayNameData);
        pw.decreaseIndent();
        pw.println("CDNR local log:");
        pw.increaseIndent();
        this.mLocalLog.dump(pw);
        pw.decreaseIndent();
    }

    private PersistableBundle getCarrierConfig() {
        PersistableBundle config = this.mCCManager.getConfigForSubId(this.mPhone.getSubId());
        if (config == null) {
            return CarrierConfigManager.getDefaultConfig();
        }
        return config;
    }

    private CarrierDisplayNameConditionRule getDisplayRule() {
        for (int i = 0; i < this.mEf.size(); i++) {
            if (this.mEf.valueAt(i).getServiceProviderNameDisplayCondition() != -1) {
                return new CarrierDisplayNameConditionRule(this.mEf.valueAt(i).getServiceProviderNameDisplayCondition());
            }
        }
        return DEFAULT_CARRIER_DISPLAY_NAME_RULE;
    }

    private List<String> getEfSpdi() {
        for (int i = 0; i < this.mEf.size(); i++) {
            if (this.mEf.valueAt(i).getServiceProviderDisplayInformation() != null) {
                return this.mEf.valueAt(i).getServiceProviderDisplayInformation();
            }
        }
        return Collections.EMPTY_LIST;
    }

    private String getEfSpn() {
        for (int i = 0; i < this.mEf.size(); i++) {
            if (!TextUtils.isEmpty(this.mEf.valueAt(i).getServiceProviderName())) {
                return this.mEf.valueAt(i).getServiceProviderName();
            }
        }
        return PhoneConfigurationManager.SSSS;
    }

    private List<IccRecords.OperatorPlmnInfo> getEfOpl() {
        for (int i = 0; i < this.mEf.size(); i++) {
            if (this.mEf.valueAt(i).getOperatorPlmnList() != null) {
                return this.mEf.valueAt(i).getOperatorPlmnList();
            }
        }
        return Collections.EMPTY_LIST;
    }

    private List<IccRecords.PlmnNetworkName> getEfPnn() {
        for (int i = 0; i < this.mEf.size(); i++) {
            if (this.mEf.valueAt(i).getPlmnNetworkNameList() != null) {
                return this.mEf.valueAt(i).getPlmnNetworkNameList();
            }
        }
        return Collections.EMPTY_LIST;
    }

    private CarrierDisplayNameData getCarrierDisplayNameFromEf() {
        CarrierDisplayNameConditionRule displayRule = getDisplayRule();
        String registeredPlmnNumeric = getServiceState().getOperatorNumeric();
        boolean isRoaming = getServiceState().getRoaming() && !getEfSpdi().contains(registeredPlmnNumeric);
        boolean showSpn = displayRule.shouldShowSpn(isRoaming);
        boolean showPlmn = displayRule.shouldShowPnn(isRoaming);
        String spn = getEfSpn();
        List<IccRecords.OperatorPlmnInfo> efOpl = getEfOpl();
        List<IccRecords.PlmnNetworkName> efPnn = getEfPnn();
        String plmn = null;
        if (!(efOpl == null || efPnn == null || !efOpl.isEmpty())) {
            plmn = efPnn.isEmpty() ? PhoneConfigurationManager.SSSS : getPlmnNetworkName(efPnn.get(0));
        }
        if (TextUtils.isEmpty(plmn)) {
            plmn = registeredPlmnNumeric;
        }
        return new CarrierDisplayNameData.Builder().setSpn(spn).setShowSpn(showSpn).setPlmn(plmn).setShowPlmn(showPlmn).build();
    }

    private CarrierDisplayNameData getCarrierDisplayNameFromWifiCallingOverride(CarrierDisplayNameData rawCarrierDisplayNameData) {
        PersistableBundle config = getCarrierConfig();
        boolean useRootLocale = config.getBoolean("wfc_spn_use_root_locale");
        Resources r = this.mContext.getResources();
        if (useRootLocale) {
            r.getConfiguration().setLocale(Locale.ROOT);
        }
        WfcCarrierNameFormatter wfcFormatter = new WfcCarrierNameFormatter(config, r.getStringArray(17236117), getServiceState().getVoiceRegState() == 3);
        String wfcSpn = wfcFormatter.formatVoiceName(rawCarrierDisplayNameData.getSpn());
        String wfcDataSpn = wfcFormatter.formatDataName(rawCarrierDisplayNameData.getSpn());
        String wfcPlmn = wfcFormatter.formatVoiceName(rawCarrierDisplayNameData.getPlmn());
        if (!TextUtils.isEmpty(wfcSpn) && !TextUtils.isEmpty(wfcDataSpn)) {
            return new CarrierDisplayNameData.Builder().setSpn(wfcSpn).setDataSpn(wfcDataSpn).setShowSpn(true).build();
        }
        if (!TextUtils.isEmpty(wfcPlmn)) {
            return new CarrierDisplayNameData.Builder().setPlmn(wfcPlmn).setShowPlmn(true).build();
        }
        return rawCarrierDisplayNameData;
    }

    private CarrierDisplayNameData getOutOfServiceDisplayName(CarrierDisplayNameData data) {
        String plmn;
        boolean forceDisplayNoService = false;
        boolean isSimReady = this.mPhone.getUiccCardApplication() != null && this.mPhone.getUiccCardApplication().getState() == IccCardApplicationStatus.AppState.APPSTATE_READY;
        if (this.mContext.getResources().getBoolean(17891415) && !isSimReady) {
            forceDisplayNoService = true;
        }
        ServiceState ss = getServiceState();
        if (ss.getVoiceRegState() == 3 || !ss.isEmergencyOnly() || forceDisplayNoService) {
            plmn = this.mContext.getResources().getString(17040422);
        } else {
            plmn = this.mContext.getResources().getString(17040034);
        }
        return new CarrierDisplayNameData.Builder().setSpn(data.getSpn()).setDataSpn(data.getDataSpn()).setShowSpn(data.shouldShowSpn()).setPlmn(plmn).setShowPlmn(true).build();
    }

    private void resolveCarrierDisplayName() {
        CarrierDisplayNameData data = getCarrierDisplayNameFromEf();
        Rlog.d(TAG, "CarrierName from EF: " + data);
        if (getCombinedRegState(getServiceState()) != 0) {
            data = getOutOfServiceDisplayName(data);
            Rlog.d(TAG, "Out of service carrierName " + data);
        } else if (this.mPhone.isWifiCallingEnabled()) {
            data = getCarrierDisplayNameFromWifiCallingOverride(data);
            Rlog.d(TAG, "CarrierName override by wifi-calling " + data);
        }
        if (!Objects.equals(this.mCarrierDisplayNameData, data)) {
            this.mLocalLog.log(String.format("ResolveCarrierDisplayName: %s", data.toString()));
        }
        this.mCarrierDisplayNameData = data;
    }

    private static String getPlmnNetworkName(IccRecords.PlmnNetworkName name) {
        if (name == null) {
            return PhoneConfigurationManager.SSSS;
        }
        if (!TextUtils.isEmpty(name.fullName)) {
            return name.fullName;
        }
        if (!TextUtils.isEmpty(name.shortName)) {
            return name.shortName;
        }
        return PhoneConfigurationManager.SSSS;
    }

    private static int getSourcePriority(int source) {
        int priority = EF_SOURCE_PRIORITY.indexOf(Integer.valueOf(source));
        if (priority == -1) {
            return KeepaliveStatus.INVALID_HANDLE;
        }
        return priority;
    }

    /* access modifiers changed from: private */
    public static final class CarrierDisplayNameConditionRule {
        private int mDisplayConditionBitmask;

        CarrierDisplayNameConditionRule(int carrierDisplayConditionBitmask) {
            this.mDisplayConditionBitmask = carrierDisplayConditionBitmask;
        }

        /* access modifiers changed from: package-private */
        public boolean shouldShowSpn(boolean isRoaming) {
            return !isRoaming || (this.mDisplayConditionBitmask & 2) == 2;
        }

        /* access modifiers changed from: package-private */
        public boolean shouldShowPnn(boolean isRoaming) {
            return isRoaming || (this.mDisplayConditionBitmask & 1) == 1;
        }

        public String toString() {
            return String.format("{ SPN_bit = %d, PLMN_bit = %d }", Integer.valueOf(2 & this.mDisplayConditionBitmask), Integer.valueOf(this.mDisplayConditionBitmask & 1));
        }
    }

    private ServiceState getServiceState() {
        return this.mPhone.getServiceStateTracker().getServiceState();
    }

    /* access modifiers changed from: private */
    public static final class WfcCarrierNameFormatter {
        final String mDataFormat;
        final String mVoiceFormat;

        WfcCarrierNameFormatter(PersistableBundle config, String[] wfcFormats, boolean inFlightMode) {
            int voiceIdx = config.getInt("wfc_spn_format_idx_int");
            int dataIdx = config.getInt("wfc_data_spn_format_idx_int");
            int flightModeIdx = config.getInt("wfc_flight_mode_spn_format_idx_int");
            if (voiceIdx < 0 || voiceIdx >= wfcFormats.length) {
                Rlog.e(CarrierDisplayNameResolver.TAG, "updateSpnDisplay: KEY_WFC_SPN_FORMAT_IDX_INT out of bounds: " + voiceIdx);
                voiceIdx = -1;
            }
            if (dataIdx < 0 || dataIdx >= wfcFormats.length) {
                Rlog.e(CarrierDisplayNameResolver.TAG, "updateSpnDisplay: KEY_WFC_DATA_SPN_FORMAT_IDX_INT out of bounds: " + dataIdx);
                dataIdx = -1;
            }
            voiceIdx = inFlightMode ? (flightModeIdx < 0 || flightModeIdx >= wfcFormats.length) ? voiceIdx : flightModeIdx : voiceIdx;
            String str = PhoneConfigurationManager.SSSS;
            this.mVoiceFormat = voiceIdx != -1 ? wfcFormats[voiceIdx] : str;
            this.mDataFormat = dataIdx != -1 ? wfcFormats[dataIdx] : str;
        }

        public String formatVoiceName(String name) {
            if (TextUtils.isEmpty(name)) {
                return name;
            }
            return String.format(this.mVoiceFormat, name.trim());
        }

        public String formatDataName(String name) {
            if (TextUtils.isEmpty(name)) {
                return name;
            }
            return String.format(this.mDataFormat, name.trim());
        }
    }

    private static int getCombinedRegState(ServiceState ss) {
        if (ss.getVoiceRegState() != 0) {
            return ss.getDataRegState();
        }
        return ss.getVoiceRegState();
    }
}
