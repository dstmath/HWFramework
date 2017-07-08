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
    EonsControlState mOplDataState;
    HwOplRecords mOplRecords;
    EonsControlState mPnnDataState;
    HwPnnRecords mPnnRecords;

    public enum CphsType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.HwEons.CphsType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.HwEons.CphsType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.HwEons.CphsType.<clinit>():void");
        }

        public boolean isLong() {
            return this == LONG ? HwEons.DBG : false;
        }

        public boolean isShort() {
            return this == SHORT ? HwEons.DBG : false;
        }
    }

    public enum EonsControlState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.HwEons.EonsControlState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.HwEons.EonsControlState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.HwEons.EonsControlState.<clinit>():void");
        }

        public boolean isIniting() {
            return this == INITING ? HwEons.DBG : false;
        }

        public boolean isPresent() {
            return this == PRESENT ? HwEons.DBG : false;
        }

        public boolean isAbsent() {
            return this == ABSENT ? HwEons.DBG : false;
        }
    }

    public enum EonsState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.HwEons.EonsState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.HwEons.EonsState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.HwEons.EonsState.<clinit>():void");
        }

        public boolean isIniting() {
            return this == INITING ? HwEons.DBG : false;
        }

        public boolean isDisabled() {
            return this == DISABLED ? HwEons.DBG : false;
        }

        public boolean isPnnPresent() {
            return this == PNN_PRESENT ? HwEons.DBG : false;
        }

        public boolean isPnnAndOplPresent() {
            return this == PNN_AND_OPL_PRESENT ? HwEons.DBG : false;
        }
    }

    public HwEons() {
        this.mPnnDataState = EonsControlState.INITING;
        this.mOplDataState = EonsControlState.INITING;
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
            return DBG;
        } else if (getEonsState().isPnnPresent() && this.mPnnRecords != null) {
            updateEonsIfHplmn(regOperator, hplmn);
            return DBG;
        } else if (!getEonsState().isIniting()) {
            return DBG;
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
        if (radioTechStr.equals(" 2G") || radioTechStr.equals(" 3G") || radioTechStr.equals(" 4G")) {
            return radioTechStr;
        }
        return "";
    }

    private void updateEonsFromOplAndPnn(String regOperator, int lac) {
        int pnnRecord = this.mOplRecords.getMatchingPnnRecord(regOperator, lac, DBG);
        log("[HwEons] Fetched HwEons name from EF_PNN record = " + pnnRecord + ", name = " + this.mPnnRecords.getNameFromPnnRecord(pnnRecord, DBG));
    }

    private void updateEonsIfHplmn(String regOperator, String hplmn) {
        log("[HwEons] Comparing hplmn, " + hplmn + " with registered plmn " + regOperator);
        if (hplmn != null && hplmn.equals(regOperator)) {
            log("[HwEons] Fetched HwEons name from EF_PNN's first record, name = " + this.mPnnRecords.getNameFromPnnRecord(1, DBG));
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
