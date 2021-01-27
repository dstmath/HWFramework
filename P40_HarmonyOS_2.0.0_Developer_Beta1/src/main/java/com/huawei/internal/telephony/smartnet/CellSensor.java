package com.huawei.internal.telephony.smartnet;

import com.huawei.android.telephony.SubscriptionInfoEx;

public class CellSensor {
    private static final String TAG = "CellSensor";
    private int blackType;
    private int cdmaLevelHw;
    private String dataOperatorNumeric;
    private int dataRadioTech;
    private int dataRegState;
    private long exceptionStartTime;
    private int gsmLevelHw;
    private String iccid;
    private int id;
    private int lteLevelHw;
    private long mainCellId;
    private int mainPci;
    private int mainTac;
    private long neighboringCellId;
    private int neighboringPci;
    private int neighboringTac;
    private int nrLevelHw;
    private int nrState;
    private int nsaState;
    private int routeId;
    private long samplingTime;
    private int slotId;
    private String voiceOperatorNumeric;
    private int voiceRadioTech;
    private int voiceRegState;
    private int wcdmaLevelHw;

    public int getId() {
        return this.id;
    }

    public void setId(int id2) {
        this.id = id2;
    }

    public int getSlotId() {
        return this.slotId;
    }

    public void setSlotId(int slotId2) {
        this.slotId = slotId2;
    }

    public String getIccid() {
        return this.iccid;
    }

    public void setIccid(String iccid2) {
        this.iccid = iccid2;
    }

    public long getSamplingTime() {
        return this.samplingTime;
    }

    public void setSamplingTime(long samplingTime2) {
        this.samplingTime = samplingTime2;
    }

    public long getMainCellId() {
        return this.mainCellId;
    }

    public void setMainCellId(long mainCellId2) {
        this.mainCellId = mainCellId2;
    }

    public int getMainPci() {
        return this.mainPci;
    }

    public void setMainPci(int mainPci2) {
        this.mainPci = mainPci2;
    }

    public int getMainTac() {
        return this.mainTac;
    }

    public void setMainTac(int mainTac2) {
        this.mainTac = mainTac2;
    }

    public long getNeighboringCellId() {
        return this.neighboringCellId;
    }

    public void setNeighboringCellId(long neighboringCellId2) {
        this.neighboringCellId = neighboringCellId2;
    }

    public int getNeighboringPci() {
        return this.neighboringPci;
    }

    public void setNeighboringPci(int neighboringPci2) {
        this.neighboringPci = neighboringPci2;
    }

    public int getNeighboringTac() {
        return this.neighboringTac;
    }

    public void setNeighboringTac(int neighboringTac2) {
        this.neighboringTac = neighboringTac2;
    }

    public int getVoiceRegState() {
        return this.voiceRegState;
    }

    public void setVoiceRegState(int voiceRegState2) {
        this.voiceRegState = voiceRegState2;
    }

    public int getDataRegState() {
        return this.dataRegState;
    }

    public void setDataRegState(int dataRegState2) {
        this.dataRegState = dataRegState2;
    }

    public String getVoiceOperatorNumeric() {
        return this.voiceOperatorNumeric;
    }

    public void setVoiceOperatorNumeric(String voiceOperatorNumeric2) {
        this.voiceOperatorNumeric = voiceOperatorNumeric2;
    }

    public String getDataOperatorNumeric() {
        return this.dataOperatorNumeric;
    }

    public void setDataOperatorNumeric(String dataOperatorNumeric2) {
        this.dataOperatorNumeric = dataOperatorNumeric2;
    }

    public int getVoiceRadioTech() {
        return this.voiceRadioTech;
    }

    public void setVoiceRadioTech(int voiceRadioTech2) {
        this.voiceRadioTech = voiceRadioTech2;
    }

    public int getDataRadioTech() {
        return this.dataRadioTech;
    }

    public void setDataRadioTech(int dataRadioTech2) {
        this.dataRadioTech = dataRadioTech2;
    }

    public int getNrState() {
        return this.nrState;
    }

    public void setNrState(int nrState2) {
        this.nrState = nrState2;
    }

    public int getNsaState() {
        return this.nsaState;
    }

    public void setNsaState(int nsaState2) {
        this.nsaState = nsaState2;
    }

    public int getCdmaLevelHw() {
        return this.cdmaLevelHw;
    }

    public void setCdmaLevelHw(int cdmaLevelHw2) {
        this.cdmaLevelHw = cdmaLevelHw2;
    }

    public int getGsmLevelHw() {
        return this.gsmLevelHw;
    }

    public void setGsmLevelHw(int gsmLevelHw2) {
        this.gsmLevelHw = gsmLevelHw2;
    }

    public int getWcdmaLevelHw() {
        return this.wcdmaLevelHw;
    }

    public void setWcdmaLevelHw(int wcdmaLevelHw2) {
        this.wcdmaLevelHw = wcdmaLevelHw2;
    }

    public int getLteLevelHw() {
        return this.lteLevelHw;
    }

    public void setLteLevelHw(int lteLevelHw2) {
        this.lteLevelHw = lteLevelHw2;
    }

    public int getNrLevelHw() {
        return this.nrLevelHw;
    }

    public void setNrLevelHw(int nrLevelHw2) {
        this.nrLevelHw = nrLevelHw2;
    }

    public String toString() {
        return "CellSensor{id=" + this.id + ",routeId=" + this.routeId + ", slotId=" + this.slotId + ", iccid='" + SubscriptionInfoEx.givePrintableIccid(this.iccid) + ", time=" + this.samplingTime + ", voiceRegState=" + this.voiceRegState + ", dataRegState=" + this.dataRegState + ", voiceRadioTech=" + this.voiceRadioTech + ", dataRadioTech=" + this.dataRadioTech + ", nrState=" + this.nrState + ", nsaState=" + this.nsaState + ", cdmaLevelHw=" + this.cdmaLevelHw + ", gsmLevelHw=" + this.gsmLevelHw + ", wcdmaLevelHw=" + this.wcdmaLevelHw + ", lteLevelHw=" + this.lteLevelHw + ", nrLevelHw=" + this.nrLevelHw + ", exceptionStartTime=" + this.exceptionStartTime + ", blackType=" + this.blackType + '}';
    }

    private boolean isOutOfService() {
        return (this.dataRegState == 0 || this.voiceRegState == 0) ? false : true;
    }

    private boolean isWeakSignalStrength() {
        return ((double) this.cdmaLevelHw) <= 2.0d && ((double) this.gsmLevelHw) <= 2.0d && ((double) this.wcdmaLevelHw) <= 2.0d && ((double) this.lteLevelHw) <= 2.0d && ((double) this.nrLevelHw) <= 2.0d;
    }

    private boolean isVoiceRadioTechAbove4G() {
        int i = this.voiceRadioTech;
        return i == 20 || i == 19 || i == 14;
    }

    private boolean isDataRadioTechAbove4G() {
        int i = this.dataRadioTech;
        return i == 20 || i == 19 || i == 14;
    }

    private boolean isOffTheRat() {
        return !isVoiceRadioTechAbove4G() && !isDataRadioTechAbove4G();
    }

    public long getExceptionStartTime() {
        return this.exceptionStartTime;
    }

    public void setExceptionStartTime(long time) {
        this.exceptionStartTime = time;
    }

    public void setBlackType(int type) {
        this.blackType |= type;
    }

    public int getBlackPointType() {
        return this.blackType;
    }

    public void analyseAndUpdateBlackPointType() {
        int blackPointType = 0;
        if (isOutOfService()) {
            blackPointType = 0 | 1;
        }
        if (isWeakSignalStrength()) {
            blackPointType |= 2;
        }
        if (isOffTheRat()) {
            blackPointType |= 4;
        }
        if (blackPointType != 0) {
            setExceptionStartTime(this.samplingTime);
        }
        setBlackType(blackPointType);
    }

    public int getRouteId() {
        return this.routeId;
    }

    public void setRouteId(int routeId2) {
        this.routeId = routeId2;
    }
}
