package com.android.internal.telephony.gsm;

import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.ArrayList;

public final class HwEons {
    private static final boolean DBG = true;
    static final String TAG = "HwEons";
    String mCphsOnsName;
    String mCphsOnsShortName;
    EonsControlState mOplDataState = EonsControlState.INITING;
    HwOplRecords mOplRecords;
    EonsControlState mPnnDataState = EonsControlState.INITING;
    HwPnnRecords mPnnRecords;

    public enum CphsType {
        LONG,
        SHORT;

        public boolean isLong() {
            return this == LONG;
        }

        public boolean isShort() {
            return this == SHORT;
        }
    }

    public enum EonsControlState {
        INITING,
        PRESENT,
        ABSENT;

        public boolean isIniting() {
            return this == INITING;
        }

        public boolean isPresent() {
            return this == PRESENT;
        }

        public boolean isAbsent() {
            return this == ABSENT;
        }
    }

    public enum EonsState {
        INITING,
        DISABLED,
        PNN_PRESENT,
        PNN_AND_OPL_PRESENT;

        public boolean isIniting() {
            return this == INITING;
        }

        public boolean isDisabled() {
            return this == DISABLED;
        }

        public boolean isPnnPresent() {
            return this == PNN_PRESENT;
        }

        public boolean isPnnAndOplPresent() {
            return this == PNN_AND_OPL_PRESENT;
        }
    }

    public HwEons() {
        Rlog.d(TAG, "Constructor init!");
        reset();
    }

    public void reset() {
        this.mPnnDataState = EonsControlState.INITING;
        this.mOplDataState = EonsControlState.INITING;
        this.mOplRecords = null;
        this.mPnnRecords = null;
        this.mCphsOnsName = null;
        this.mCphsOnsShortName = null;
    }

    public void setOplData(ArrayList<byte[]> records) {
        this.mOplDataState = EonsControlState.PRESENT;
        this.mOplRecords = new HwOplRecords(records);
    }

    public void resetOplData() {
        this.mOplDataState = EonsControlState.ABSENT;
        this.mOplRecords = null;
    }

    public void setPnnData(ArrayList<byte[]> records) {
        this.mPnnDataState = EonsControlState.PRESENT;
        this.mPnnRecords = new HwPnnRecords(records);
    }

    public void resetPnnData() {
        this.mPnnDataState = EonsControlState.ABSENT;
        this.mPnnRecords = null;
    }

    public void resetCphsData(CphsType type) {
        if (type.isLong()) {
            this.mCphsOnsName = null;
        } else if (type.isShort()) {
            this.mCphsOnsShortName = null;
        } else {
            this.mCphsOnsName = null;
            this.mCphsOnsShortName = null;
        }
    }

    public void setCphsData(CphsType type, byte[] data) {
        if (type.isLong()) {
            this.mCphsOnsName = IccUtils.adnStringFieldToString(data, 0, data.length);
            log("setCphsData():mCphsOnsName is :" + this.mCphsOnsName);
        } else if (type.isShort()) {
            this.mCphsOnsShortName = IccUtils.adnStringFieldToString(data, 0, data.length);
            log("setCphsData():mCphsOnsShortName is :" + this.mCphsOnsShortName);
        }
    }

    public String getEons() {
        String name = null;
        if (this.mPnnRecords != null) {
            log("getEons():mPnnRecords is not null!");
            name = this.mPnnRecords.getCurrentEons();
            log("getEons():name is :" + name);
        }
        if (!SystemProperties.get("ro.config.CphsOnsEnabled", "true").equals("true") || name != null) {
            return name;
        }
        if (TextUtils.isEmpty(this.mCphsOnsName)) {
            name = this.mCphsOnsShortName;
            log("get name from mCphsOnsShortName :" + name);
            return name;
        }
        name = this.mCphsOnsName;
        log("mCphsOnsName is not null!----get name is :" + name);
        return name;
    }

    public boolean updateEons(String regOperator, int lac, String hplmn) {
        if (getEonsState().isPnnAndOplPresent() && this.mOplRecords != null && this.mPnnRecords != null) {
            updateEonsFromOplAndPnn(regOperator, lac);
            return true;
        } else if (getEonsState().isPnnPresent() && this.mPnnRecords != null) {
            updateEonsIfHplmn(regOperator, hplmn);
            return true;
        } else if (!getEonsState().isIniting()) {
            return true;
        } else {
            log("[HwEons] Reading data from EF_OPL or EF_PNN is not complete. Suppress operator name display until all EF_OPL/EF_PNN data is read.");
            return false;
        }
    }

    public ArrayList<OperatorInfo> getEonsForAvailableNetworks(ArrayList<OperatorInfo> avlNetworks) {
        ArrayList<OperatorInfo> eonsNetworkNames = null;
        if (!getEonsState().isPnnAndOplPresent() || this.mPnnRecords == null || this.mOplRecords == null) {
            loge("[HwEons] OPL/PNN data is not available. Use the network names from Ril.");
            return null;
        }
        if (avlNetworks == null || avlNetworks.size() <= 0) {
            loge("[HwEons] Available Networks List is empty");
        } else {
            int size = avlNetworks.size();
            eonsNetworkNames = new ArrayList(size);
            log("[HwEons] Available Networks List Size = " + size);
            for (int i = 0; i < size; i++) {
                OperatorInfo oi = (OperatorInfo) avlNetworks.get(i);
                String pnnName = this.mPnnRecords.getNameFromPnnRecord(this.mOplRecords.getMatchingPnnRecord(oi.getOperatorNumericWithoutAct(), -1, false), false);
                log("[HwEons] PLMN = " + oi.getOperatorNumeric() + ", ME Name = " + oi.getOperatorAlphaLong() + ", PNN Name = " + pnnName);
                if (pnnName == null) {
                    pnnName = oi.getOperatorAlphaLong();
                } else {
                    pnnName = pnnName.concat(getRadioTechString(oi));
                }
                if (("334050".equals(oi.getOperatorNumericWithoutAct()) || "334090".equals(oi.getOperatorNumericWithoutAct())) && pnnName != null && pnnName.startsWith("AT&T")) {
                    pnnName = "AT&T" + getRadioTechString(oi);
                }
                eonsNetworkNames.add(new OperatorInfo(pnnName, oi.getOperatorAlphaShort(), oi.getOperatorNumeric(), oi.getState()));
            }
        }
        return eonsNetworkNames;
    }

    private String getRadioTechString(OperatorInfo info) {
        String radioTechStr = "";
        String operatorName = info.getOperatorAlphaLong();
        if (operatorName == null) {
            operatorName = info.getOperatorAlphaShort();
        }
        if (operatorName == null) {
            return radioTechStr;
        }
        int longNameIndex = operatorName.lastIndexOf(32);
        if (-1 == longNameIndex) {
            return radioTechStr;
        }
        radioTechStr = operatorName.substring(longNameIndex);
        if (radioTechStr.equals(" 2G") || (radioTechStr.equals(" 3G") ^ 1) == 0 || (radioTechStr.equals(" 4G") ^ 1) == 0) {
            return radioTechStr;
        }
        return "";
    }

    private void updateEonsFromOplAndPnn(String regOperator, int lac) {
        int pnnRecord = this.mOplRecords.getMatchingPnnRecord(regOperator, lac, true);
        log("[HwEons] Fetched HwEons name from EF_PNN record = " + pnnRecord + ", name = " + this.mPnnRecords.getNameFromPnnRecord(pnnRecord, true));
    }

    private void updateEonsIfHplmn(String regOperator, String hplmn) {
        log("[HwEons] Comparing hplmn, " + hplmn + " with registered plmn " + regOperator);
        if (hplmn != null && hplmn.equals(regOperator)) {
            log("[HwEons] Fetched HwEons name from EF_PNN's first record, name = " + this.mPnnRecords.getNameFromPnnRecord(1, true));
        }
    }

    private EonsState getEonsState() {
        if (this.mPnnDataState.isIniting() || this.mOplDataState.isIniting()) {
            return EonsState.INITING;
        }
        if (!this.mPnnDataState.isPresent()) {
            return EonsState.DISABLED;
        }
        if (this.mOplDataState.isPresent()) {
            return EonsState.PNN_AND_OPL_PRESENT;
        }
        return EonsState.PNN_PRESENT;
    }

    public boolean isEonsDisabled() {
        return getEonsState().isDisabled();
    }

    private void log(String s) {
        Rlog.d(TAG, "[HwEons] " + s);
    }

    private void loge(String s) {
        Rlog.e(TAG, "[HwEons] " + s);
    }
}
