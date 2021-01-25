package com.huawei.hwwifiproservice;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WifiProChrSsidStatistics {
    private static final String AP_TYPE_EVENT = "ApType";
    private static final int SELF_CURE_TYPES = 4;
    private static final String TAG = "WifiProChrSsidStatistics";
    private static final int WIFI_TO_CELL_TYPES = 2;
    private String mApType = "";
    public ArrayList<Integer> mChipCureCnt = new ArrayList<>();
    public ArrayList<Integer> mChipCureSuccCnt = new ArrayList<>();
    public int mDelayConnCnt;
    public int mDelayDuration;
    public Map<String, Integer> mDetectApType = new HashMap();
    public ArrayList<Integer> mDhcpOfferCnt = new ArrayList<>();
    public ArrayList<Integer> mDhcpOfferSuccCnt = new ArrayList<>();
    public int mMultiDhcpCure;
    public Map<String, Integer> mNotConnType = new HashMap();
    public int mReDhcpCnt;
    public int mReDhcpSuccCnt;
    public int mReassocCnt;
    public int mReassocSuccCnt;
    public int mReplaceDnsCnt;
    public int mReplaceDnsSuccCnt;
    public ArrayList<Integer> mSelfCureCnt = new ArrayList<>();
    public ArrayList<Integer> mSelfCureSuccCnt = new ArrayList<>();
    public Map<String, Integer> mUnexpSwitchType = new HashMap();
    public ArrayList<Integer> mWifiToCellCnt = new ArrayList<>();
    public ArrayList<Integer> mWifiToCellDuation = new ArrayList<>();
    public ArrayList<Integer> mWifiToCellFlow = new ArrayList<>();
    public ArrayList<Integer> mWifiToCellSuccCnt = new ArrayList<>();

    public WifiProChrSsidStatistics() {
        resetSsidStatistics();
    }

    public Bundle getSsidStatBundle() {
        Bundle activeDetecEvent = new Bundle();
        activeDetecEvent.putInt("HasInternet", this.mDetectApType.get("HasInternet").intValue());
        activeDetecEvent.putInt("NoInternet", this.mDetectApType.get("NoInternet").intValue());
        activeDetecEvent.putInt("Portal", this.mDetectApType.get("Portal").intValue());
        Bundle delayConnEvent = new Bundle();
        delayConnEvent.putInt("DelayConnCnt", this.mDelayConnCnt);
        delayConnEvent.putInt("DelayDuration", this.mDelayDuration);
        Bundle notConnEvent = new Bundle();
        notConnEvent.putInt("CommonAp", this.mNotConnType.get("CommonAp").intValue());
        notConnEvent.putInt("PortalAp", this.mNotConnType.get("PortalAp").intValue());
        Bundle selfCureEvent = new Bundle();
        selfCureEvent.putInt("StopUse", this.mSelfCureCnt.get(0).intValue());
        selfCureEvent.putInt("Rejected", this.mSelfCureCnt.get(1).intValue());
        selfCureEvent.putInt("WrongPassword", this.mSelfCureCnt.get(2).intValue());
        selfCureEvent.putInt("InternetError", this.mSelfCureCnt.get(3).intValue());
        Bundle selfCureSuccEvent = new Bundle();
        selfCureSuccEvent.putInt("StopUse", this.mSelfCureSuccCnt.get(0).intValue());
        selfCureSuccEvent.putInt("Rejected", this.mSelfCureSuccCnt.get(1).intValue());
        selfCureSuccEvent.putInt("WrongPassword", this.mSelfCureSuccCnt.get(2).intValue());
        selfCureSuccEvent.putInt("InternetError", this.mSelfCureSuccCnt.get(3).intValue());
        Bundle noInterToCellEvent = new Bundle();
        noInterToCellEvent.putInt("WifiToCellCnt", this.mWifiToCellCnt.get(0).intValue());
        noInterToCellEvent.putInt("WifiToCellSuccCnt", this.mWifiToCellSuccCnt.get(0).intValue());
        noInterToCellEvent.putInt("WifiToCellDura", this.mWifiToCellDuation.get(0).intValue());
        noInterToCellEvent.putInt("WifiToCellFlow", this.mWifiToCellFlow.get(0).intValue());
        Bundle slowInterToCellEvent = new Bundle();
        slowInterToCellEvent.putInt("WifiToCellCnt", this.mWifiToCellCnt.get(1).intValue());
        slowInterToCellEvent.putInt("WifiToCellSuccCnt", this.mWifiToCellSuccCnt.get(1).intValue());
        slowInterToCellEvent.putInt("WifiToCellDura", this.mWifiToCellDuation.get(1).intValue());
        slowInterToCellEvent.putInt("WifiToCellFlow", this.mWifiToCellFlow.get(1).intValue());
        Bundle unExpectSwitchEvent = new Bundle();
        unExpectSwitchEvent.putInt("CloseWifi", this.mUnexpSwitchType.get("CloseWifi").intValue());
        unExpectSwitchEvent.putInt("CloseWifiPro", this.mUnexpSwitchType.get("CloseWifiPro").intValue());
        unExpectSwitchEvent.putInt("UserSelectOld", this.mUnexpSwitchType.get("UserSelectOld").intValue());
        unExpectSwitchEvent.putInt("ForgetAp", this.mUnexpSwitchType.get("ForgetAp").intValue());
        unExpectSwitchEvent.putInt("PingPong", this.mUnexpSwitchType.get("PingPong").intValue());
        unExpectSwitchEvent.putInt("UserRejectSwitch", this.mUnexpSwitchType.get("UserRejectSwitch").intValue());
        Bundle dnsCureRecoveryEvent = new Bundle();
        dnsCureRecoveryEvent.putInt("DhcpOfferCnt", this.mDhcpOfferCnt.get(0).intValue());
        dnsCureRecoveryEvent.putInt("DhcpOfferSuccCnt", this.mDhcpOfferSuccCnt.get(0).intValue());
        dnsCureRecoveryEvent.putInt("ChipCureCnt", this.mChipCureCnt.get(0).intValue());
        dnsCureRecoveryEvent.putInt("ChipCureSuccCnt", this.mChipCureSuccCnt.get(0).intValue());
        Bundle tcpCureRecoveryEvent = new Bundle();
        tcpCureRecoveryEvent.putInt("DhcpOfferCnt", this.mDhcpOfferCnt.get(1).intValue());
        tcpCureRecoveryEvent.putInt("DhcpOfferSuccCnt", this.mDhcpOfferSuccCnt.get(1).intValue());
        tcpCureRecoveryEvent.putInt("ChipCureCnt", this.mChipCureCnt.get(1).intValue());
        tcpCureRecoveryEvent.putInt("ChipCureSuccCnt", this.mChipCureSuccCnt.get(1).intValue());
        Bundle roamCureRecoveryEvent = new Bundle();
        roamCureRecoveryEvent.putInt("DhcpOfferCnt", this.mDhcpOfferCnt.get(2).intValue());
        roamCureRecoveryEvent.putInt("DhcpOfferSuccCnt", this.mDhcpOfferSuccCnt.get(2).intValue());
        roamCureRecoveryEvent.putInt("ChipCureCnt", this.mChipCureCnt.get(2).intValue());
        roamCureRecoveryEvent.putInt("ChipCureSuccCnt", this.mChipCureSuccCnt.get(2).intValue());
        Bundle multiCureRecoveryEvent = new Bundle();
        multiCureRecoveryEvent.putInt("DhcpOfferCnt", this.mDhcpOfferCnt.get(3).intValue());
        multiCureRecoveryEvent.putInt("DhcpOfferSuccCnt", this.mDhcpOfferSuccCnt.get(3).intValue());
        multiCureRecoveryEvent.putInt("ChipCureCnt", this.mChipCureCnt.get(3).intValue());
        multiCureRecoveryEvent.putInt("ChipCureSuccCnt", this.mChipCureSuccCnt.get(3).intValue());
        Bundle uniqueCureEvent = new Bundle();
        uniqueCureEvent.putInt("ReplaceDnsCnt", this.mReplaceDnsCnt);
        uniqueCureEvent.putInt("ReplaceDnsSuccCnt", this.mReplaceDnsSuccCnt);
        uniqueCureEvent.putInt("ReassocCnt", this.mReassocCnt);
        uniqueCureEvent.putInt("ReassocSuccCnt", this.mReassocSuccCnt);
        uniqueCureEvent.putInt("ReDhcpCnt", this.mReDhcpCnt);
        uniqueCureEvent.putInt("ReDhcpSuccCnt", this.mReDhcpSuccCnt);
        uniqueCureEvent.putInt("MultiDhcpCure", this.mMultiDhcpCure);
        Bundle allSsidEvent = new Bundle();
        allSsidEvent.putString(AP_TYPE_EVENT, this.mApType);
        allSsidEvent.putBundle("DelayConnEvent", delayConnEvent);
        allSsidEvent.putBundle("NotConnEvent", notConnEvent);
        allSsidEvent.putBundle("SelfCureEvent", selfCureEvent);
        allSsidEvent.putBundle("SelfCureSuccEvent", selfCureSuccEvent);
        allSsidEvent.putBundle("NoInterToCellEvent", noInterToCellEvent);
        allSsidEvent.putBundle("SlowInterToCellEvent", slowInterToCellEvent);
        allSsidEvent.putBundle("UnExpectSwitchEvent", unExpectSwitchEvent);
        allSsidEvent.putBundle("DnsCureRecoveryEvent", dnsCureRecoveryEvent);
        allSsidEvent.putBundle("TcpCureRecoveryEvent", tcpCureRecoveryEvent);
        allSsidEvent.putBundle("RoamCureRecoveryEvent", roamCureRecoveryEvent);
        allSsidEvent.putBundle("MultiCureRecoveryEvent", multiCureRecoveryEvent);
        allSsidEvent.putBundle("UniqueCureEvent", uniqueCureEvent);
        allSsidEvent.putBundle("ActiveDetecEvent", activeDetecEvent);
        return allSsidEvent;
    }

    public final void resetSsidStatistics() {
        this.mDetectApType.put("HasInternet", 0);
        this.mDetectApType.put("NoInternet", 0);
        this.mDetectApType.put("Portal", 0);
        this.mDelayConnCnt = 0;
        this.mDelayDuration = 0;
        this.mNotConnType.put("CommonAp", 0);
        this.mNotConnType.put("PortalAp", 0);
        this.mSelfCureCnt.clear();
        this.mSelfCureSuccCnt.clear();
        for (int i = 0; i < 4; i++) {
            this.mSelfCureCnt.add(0);
            this.mSelfCureSuccCnt.add(0);
        }
        this.mWifiToCellCnt.clear();
        this.mWifiToCellSuccCnt.clear();
        this.mWifiToCellDuation.clear();
        this.mWifiToCellFlow.clear();
        for (int i2 = 0; i2 < 2; i2++) {
            this.mWifiToCellCnt.add(0);
            this.mWifiToCellSuccCnt.add(0);
            this.mWifiToCellDuation.add(0);
            this.mWifiToCellFlow.add(0);
        }
        this.mUnexpSwitchType.put("CloseWifi", 0);
        this.mUnexpSwitchType.put("CloseWifiPro", 0);
        this.mUnexpSwitchType.put("UserSelectOld", 0);
        this.mUnexpSwitchType.put("ForgetAp", 0);
        this.mUnexpSwitchType.put("PingPong", 0);
        this.mUnexpSwitchType.put("UserRejectSwitch", 0);
        this.mDhcpOfferCnt.clear();
        this.mDhcpOfferSuccCnt.clear();
        this.mChipCureCnt.clear();
        this.mChipCureSuccCnt.clear();
        for (int i3 = 0; i3 < 4; i3++) {
            this.mDhcpOfferCnt.add(0);
            this.mDhcpOfferSuccCnt.add(0);
            this.mChipCureCnt.add(0);
            this.mChipCureSuccCnt.add(0);
        }
        this.mReplaceDnsCnt = 0;
        this.mReplaceDnsSuccCnt = 0;
        this.mReassocCnt = 0;
        this.mReassocSuccCnt = 0;
        this.mReDhcpCnt = 0;
        this.mReDhcpSuccCnt = 0;
        this.mMultiDhcpCure = 0;
    }

    public String getApType() {
        return this.mApType;
    }

    public void setApType(String apType) {
        this.mApType = apType;
    }
}
