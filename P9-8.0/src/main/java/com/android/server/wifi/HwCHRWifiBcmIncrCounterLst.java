package com.android.server.wifi;

import android.os.SystemClock;
import com.huawei.device.connectivitychrlog.CSubWL_COUNTERS;
import com.huawei.device.connectivitychrlog.LogInt;
import java.util.ArrayList;
import java.util.List;

public class HwCHRWifiBcmIncrCounterLst {
    private static final String TAG = "HwCHRWifiBcmIncrCounterLst";
    private List<HwCHRWifiLinkCounter> incrCounters;
    private long mStartTimeStamp;
    private long monitorDuration;

    static class HwCHRWifiLinkCounter extends HwCHRWifiCounterInfo {
        public HwCHRWifiLinkCounter(String name) {
            super(name);
        }

        public void parserValue(String Line, String cols) {
        }

        public void parseValue(HwCHRWifiBCMCounter obj) {
        }
    }

    static class HwCHRWifiBcmIncrCounter extends HwCHRWifiLinkCounter {
        private long mOrgValue = 0;

        public HwCHRWifiBcmIncrCounter(String name) {
            super(name);
        }

        public void parseValue(HwCHRWifiBCMCounter obj) {
            if (obj.getValue() - this.mOrgValue < 0) {
                this.mDelta = (obj.getValue() - this.mOrgValue) + 2147483647L;
            } else {
                this.mDelta = obj.getValue() - this.mOrgValue;
            }
            this.mOrgValue = obj.getValue();
        }
    }

    static class HwCHRWifiBcmStatus extends HwCHRWifiLinkCounter {
        public HwCHRWifiBcmStatus(String name) {
            super(name);
        }

        public void parseValue(HwCHRWifiBCMCounter obj) {
            this.mDelta = obj.getValue();
        }
    }

    public HwCHRWifiBcmIncrCounterLst() {
        this.monitorDuration = 0;
        this.mStartTimeStamp = 0;
        this.incrCounters = null;
        this.incrCounters = new ArrayList();
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_TXBYTE));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_TXFRAME));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_D11_TXNOASSOC));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_D11_TXPHYERROR));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_D11_TXCTL));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_TXFAIL));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_TXPHYERR));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_D11_TXFRMSNT));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_D11_TXNOACK));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_D11_TXFRAG));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_D11_TXNOCTS));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_D11_TXRTS));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_TXNOBUF));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_TXALLFRM));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_TXERROR));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_TXRETRANS));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXBYTE));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXFRAME));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXBADFCS));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXBADPLCP));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXMULTI));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_D11_RXFRAG));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXCTL));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXBADCM));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXNOBUF));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXCRSGLITCH));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXBADDS));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXNODATA));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXMULTI));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RESET));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXDFRMOCAST));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXMFRMOCAST));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXDFRMUCASTMBSS));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXMFRMUCASTMBSS));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXBEACONMBSS));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXWDSFRAME));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXBEACONOBSS));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXSTRT));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXCFRMOCAST));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXCFRMUCAST));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXDFRMMCAST));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXMFRMMCAST));
        this.incrCounters.add(new HwCHRWifiBcmIncrCounter(HwWifiCHRConstImpl.BCM_RXCFRMMCAST));
    }

    public void updateIncrCounters(HwCHRWifiBCMCounterReader reader) {
        int listSize = this.incrCounters.size();
        for (int i = 0; i < listSize; i++) {
            HwCHRWifiLinkCounter counter = (HwCHRWifiLinkCounter) this.incrCounters.get(i);
            HwCHRWifiBCMCounter item = reader.getBcmCounter(counter.getTag());
            if (item != null) {
                counter.parseValue(item);
            }
        }
        this.monitorDuration = SystemClock.elapsedRealtime() - this.mStartTimeStamp;
        this.mStartTimeStamp = SystemClock.elapsedRealtime();
    }

    public String getSpeedInfo() {
        StringBuffer buffer = new StringBuffer();
        int listSize = this.incrCounters.size();
        for (int i = 0; i < listSize; i++) {
            buffer.append(((HwCHRWifiLinkCounter) this.incrCounters.get(i)).toString() + HwCHRWifiCPUUsage.COL_SEP);
        }
        return buffer.toString();
    }

    public String toString() {
        return "HwCHRWifiBcmIncrCounterLst [" + getSpeedInfo() + "]";
    }

    public CSubWL_COUNTERS getWLCountersCHR() {
        CSubWL_COUNTERS chrWlCounters = new CSubWL_COUNTERS();
        int listSize = this.incrCounters.size();
        for (int i = 0; i < listSize; i++) {
            HwCHRWifiLinkCounter counter = (HwCHRWifiLinkCounter) this.incrCounters.get(i);
            LogInt obj = chrWlCounters.getfieldMap().get("i" + counter.getTag());
            if (obj != null && (obj instanceof LogInt)) {
                obj.setValue((int) counter.getDelta());
            }
        }
        chrWlCounters.imonitorDuration.setValue((int) this.monitorDuration);
        return chrWlCounters;
    }
}
