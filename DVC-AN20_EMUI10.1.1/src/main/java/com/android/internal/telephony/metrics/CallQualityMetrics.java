package com.android.internal.telephony.metrics;

import android.os.Build;
import android.telephony.CallQuality;
import android.telephony.CellSignalStrengthLte;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.util.Pair;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import com.android.internal.telephony.nano.TelephonyProto;
import java.util.ArrayList;
import java.util.Iterator;

public class CallQualityMetrics {
    private static final int BAD_QUALITY = 1;
    private static final int GOOD_QUALITY = 0;
    private static final int MAX_SNAPSHOTS = 5;
    private static final String TAG = CallQualityMetrics.class.getSimpleName();
    private static final boolean USERDEBUG_MODE = Build.IS_USERDEBUG;
    private Pair<CallQuality, Integer> mBestSsWithBadDlQuality;
    private Pair<CallQuality, Integer> mBestSsWithBadUlQuality;
    private Pair<CallQuality, Integer> mBestSsWithGoodDlQuality;
    private Pair<CallQuality, Integer> mBestSsWithGoodUlQuality;
    private int mDlCallQualityState = 0;
    private ArrayList<Pair<CallQuality, Integer>> mDlSnapshots = new ArrayList<>();
    private CallQuality mLastCallQuality;
    private Phone mPhone;
    private int mTotalDlBadQualityTimeMs = 0;
    private int mTotalDlGoodQualityTimeMs = 0;
    private int mTotalUlBadQualityTimeMs = 0;
    private int mTotalUlGoodQualityTimeMs = 0;
    private int mUlCallQualityState = 0;
    private ArrayList<Pair<CallQuality, Integer>> mUlSnapshots = new ArrayList<>();
    private Pair<CallQuality, Integer> mWorstSsWithBadDlQuality;
    private Pair<CallQuality, Integer> mWorstSsWithBadUlQuality;
    private Pair<CallQuality, Integer> mWorstSsWithGoodDlQuality;
    private Pair<CallQuality, Integer> mWorstSsWithGoodUlQuality;

    public CallQualityMetrics(Phone phone) {
        this.mPhone = phone;
        this.mLastCallQuality = new CallQuality();
    }

    public void saveCallQuality(CallQuality cq) {
        if (cq.getUplinkCallQualityLevel() != 5 && cq.getDownlinkCallQualityLevel() != 5) {
            int newUlCallQualityState = 1;
            int newDlCallQualityState = 1;
            if (isGoodQuality(cq.getUplinkCallQualityLevel())) {
                newUlCallQualityState = 0;
            }
            if (isGoodQuality(cq.getDownlinkCallQualityLevel())) {
                newDlCallQualityState = 0;
            }
            if (USERDEBUG_MODE) {
                if (newUlCallQualityState != this.mUlCallQualityState) {
                    this.mUlSnapshots = addSnapshot(cq, this.mUlSnapshots);
                }
                if (newDlCallQualityState != this.mDlCallQualityState) {
                    this.mDlSnapshots = addSnapshot(cq, this.mDlSnapshots);
                }
            }
            updateTotalDurations(newDlCallQualityState, newUlCallQualityState, cq);
            updateMinAndMaxSignalStrengthSnapshots(newDlCallQualityState, newUlCallQualityState, cq);
            this.mUlCallQualityState = newUlCallQualityState;
            this.mDlCallQualityState = newDlCallQualityState;
            this.mLastCallQuality = cq;
        }
    }

    private static boolean isGoodQuality(int callQualityLevel) {
        return callQualityLevel < 4;
    }

    private ArrayList<Pair<CallQuality, Integer>> addSnapshot(CallQuality cq, ArrayList<Pair<CallQuality, Integer>> snapshots) {
        if (snapshots.size() < 5) {
            snapshots.add(Pair.create(cq, getLteSnr()));
        }
        return snapshots;
    }

    private void updateTotalDurations(int newDlCallQualityState, int newUlCallQualityState, CallQuality cq) {
        int timePassed = cq.getCallDuration() - this.mLastCallQuality.getCallDuration();
        if (newDlCallQualityState == 0) {
            this.mTotalDlGoodQualityTimeMs += timePassed;
        } else {
            this.mTotalDlBadQualityTimeMs += timePassed;
        }
        if (newUlCallQualityState == 0) {
            this.mTotalUlGoodQualityTimeMs += timePassed;
        } else {
            this.mTotalUlBadQualityTimeMs += timePassed;
        }
    }

    private void updateMinAndMaxSignalStrengthSnapshots(int newDlCallQualityState, int newUlCallQualityState, CallQuality cq) {
        Integer ss = getLteSnr();
        if (!ss.equals(Integer.valueOf((int) KeepaliveStatus.INVALID_HANDLE))) {
            if (newDlCallQualityState == 0) {
                if (this.mWorstSsWithGoodDlQuality == null || ss.intValue() < ((Integer) this.mWorstSsWithGoodDlQuality.second).intValue()) {
                    this.mWorstSsWithGoodDlQuality = Pair.create(cq, ss);
                }
                if (this.mBestSsWithGoodDlQuality == null || ss.intValue() > ((Integer) this.mBestSsWithGoodDlQuality.second).intValue()) {
                    this.mBestSsWithGoodDlQuality = Pair.create(cq, ss);
                }
            } else {
                if (this.mWorstSsWithBadDlQuality == null || ss.intValue() < ((Integer) this.mWorstSsWithBadDlQuality.second).intValue()) {
                    this.mWorstSsWithBadDlQuality = Pair.create(cq, ss);
                }
                if (this.mBestSsWithBadDlQuality == null || ss.intValue() > ((Integer) this.mBestSsWithBadDlQuality.second).intValue()) {
                    this.mBestSsWithBadDlQuality = Pair.create(cq, ss);
                }
            }
            if (newUlCallQualityState == 0) {
                if (this.mWorstSsWithGoodUlQuality == null || ss.intValue() < ((Integer) this.mWorstSsWithGoodUlQuality.second).intValue()) {
                    this.mWorstSsWithGoodUlQuality = Pair.create(cq, ss);
                }
                if (this.mBestSsWithGoodUlQuality == null || ss.intValue() > ((Integer) this.mBestSsWithGoodUlQuality.second).intValue()) {
                    this.mBestSsWithGoodUlQuality = Pair.create(cq, ss);
                    return;
                }
                return;
            }
            if (this.mWorstSsWithBadUlQuality == null || ss.intValue() < ((Integer) this.mWorstSsWithBadUlQuality.second).intValue()) {
                this.mWorstSsWithBadUlQuality = Pair.create(cq, ss);
            }
            if (this.mBestSsWithBadUlQuality == null || ss.intValue() > ((Integer) this.mBestSsWithBadUlQuality.second).intValue()) {
                this.mBestSsWithBadUlQuality = Pair.create(cq, ss);
            }
        }
    }

    private Integer getLteSnr() {
        ServiceStateTracker sst = this.mPhone.getDefaultPhone().getServiceStateTracker();
        Integer valueOf = Integer.valueOf((int) KeepaliveStatus.INVALID_HANDLE);
        if (sst == null) {
            String str = TAG;
            Rlog.e(str, "getLteSnr: unable to get SST for phone " + this.mPhone.getPhoneId());
            return valueOf;
        }
        SignalStrength ss = sst.getSignalStrength();
        if (ss == null) {
            String str2 = TAG;
            Rlog.e(str2, "getLteSnr: unable to get SignalStrength for phone " + this.mPhone.getPhoneId());
            return valueOf;
        }
        for (CellSignalStrengthLte lteSs : ss.getCellSignalStrengths(CellSignalStrengthLte.class)) {
            int snr = lteSs.getRssnr();
            if (snr != Integer.MAX_VALUE) {
                return Integer.valueOf(snr);
            }
        }
        return valueOf;
    }

    private static TelephonyProto.TelephonyCallSession.Event.SignalStrength toProto(int ss) {
        TelephonyProto.TelephonyCallSession.Event.SignalStrength ret = new TelephonyProto.TelephonyCallSession.Event.SignalStrength();
        ret.lteSnr = ss;
        return ret;
    }

    public TelephonyProto.TelephonyCallSession.Event.CallQualitySummary getCallQualitySummaryDl() {
        TelephonyProto.TelephonyCallSession.Event.CallQualitySummary summary = new TelephonyProto.TelephonyCallSession.Event.CallQualitySummary();
        summary.totalGoodQualityDurationInSeconds = this.mTotalDlGoodQualityTimeMs / 1000;
        summary.totalBadQualityDurationInSeconds = this.mTotalDlBadQualityTimeMs / 1000;
        summary.totalDurationWithQualityInformationInSeconds = this.mLastCallQuality.getCallDuration() / 1000;
        Pair<CallQuality, Integer> pair = this.mWorstSsWithGoodDlQuality;
        if (pair != null) {
            summary.snapshotOfWorstSsWithGoodQuality = TelephonyMetrics.toCallQualityProto((CallQuality) pair.first);
            summary.worstSsWithGoodQuality = toProto(((Integer) this.mWorstSsWithGoodDlQuality.second).intValue());
        }
        Pair<CallQuality, Integer> pair2 = this.mBestSsWithGoodDlQuality;
        if (pair2 != null) {
            summary.snapshotOfBestSsWithGoodQuality = TelephonyMetrics.toCallQualityProto((CallQuality) pair2.first);
            summary.bestSsWithGoodQuality = toProto(((Integer) this.mBestSsWithGoodDlQuality.second).intValue());
        }
        Pair<CallQuality, Integer> pair3 = this.mWorstSsWithBadDlQuality;
        if (pair3 != null) {
            summary.snapshotOfWorstSsWithBadQuality = TelephonyMetrics.toCallQualityProto((CallQuality) pair3.first);
            summary.worstSsWithBadQuality = toProto(((Integer) this.mWorstSsWithBadDlQuality.second).intValue());
        }
        Pair<CallQuality, Integer> pair4 = this.mBestSsWithBadDlQuality;
        if (pair4 != null) {
            summary.snapshotOfBestSsWithBadQuality = TelephonyMetrics.toCallQualityProto((CallQuality) pair4.first);
            summary.bestSsWithBadQuality = toProto(((Integer) this.mBestSsWithBadDlQuality.second).intValue());
        }
        summary.snapshotOfEnd = TelephonyMetrics.toCallQualityProto(this.mLastCallQuality);
        return summary;
    }

    public TelephonyProto.TelephonyCallSession.Event.CallQualitySummary getCallQualitySummaryUl() {
        TelephonyProto.TelephonyCallSession.Event.CallQualitySummary summary = new TelephonyProto.TelephonyCallSession.Event.CallQualitySummary();
        summary.totalGoodQualityDurationInSeconds = this.mTotalUlGoodQualityTimeMs / 1000;
        summary.totalBadQualityDurationInSeconds = this.mTotalUlBadQualityTimeMs / 1000;
        summary.totalDurationWithQualityInformationInSeconds = this.mLastCallQuality.getCallDuration() / 1000;
        Pair<CallQuality, Integer> pair = this.mWorstSsWithGoodUlQuality;
        if (pair != null) {
            summary.snapshotOfWorstSsWithGoodQuality = TelephonyMetrics.toCallQualityProto((CallQuality) pair.first);
            summary.worstSsWithGoodQuality = toProto(((Integer) this.mWorstSsWithGoodUlQuality.second).intValue());
        }
        Pair<CallQuality, Integer> pair2 = this.mBestSsWithGoodUlQuality;
        if (pair2 != null) {
            summary.snapshotOfBestSsWithGoodQuality = TelephonyMetrics.toCallQualityProto((CallQuality) pair2.first);
            summary.bestSsWithGoodQuality = toProto(((Integer) this.mBestSsWithGoodUlQuality.second).intValue());
        }
        Pair<CallQuality, Integer> pair3 = this.mWorstSsWithBadUlQuality;
        if (pair3 != null) {
            summary.snapshotOfWorstSsWithBadQuality = TelephonyMetrics.toCallQualityProto((CallQuality) pair3.first);
            summary.worstSsWithBadQuality = toProto(((Integer) this.mWorstSsWithBadUlQuality.second).intValue());
        }
        Pair<CallQuality, Integer> pair4 = this.mBestSsWithBadUlQuality;
        if (pair4 != null) {
            summary.snapshotOfBestSsWithBadQuality = TelephonyMetrics.toCallQualityProto((CallQuality) pair4.first);
            summary.bestSsWithBadQuality = toProto(((Integer) this.mBestSsWithBadUlQuality.second).intValue());
        }
        summary.snapshotOfEnd = TelephonyMetrics.toCallQualityProto(this.mLastCallQuality);
        return summary;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[CallQualityMetrics phone ");
        sb.append(this.mPhone.getPhoneId());
        sb.append(" mUlSnapshots: {");
        Iterator<Pair<CallQuality, Integer>> it = this.mUlSnapshots.iterator();
        while (it.hasNext()) {
            Pair<CallQuality, Integer> snapshot = it.next();
            sb.append(" {cq=");
            sb.append(snapshot.first);
            sb.append(" ss=");
            sb.append(snapshot.second);
            sb.append("}");
        }
        sb.append("}");
        sb.append(" mDlSnapshots:{");
        Iterator<Pair<CallQuality, Integer>> it2 = this.mDlSnapshots.iterator();
        while (it2.hasNext()) {
            Pair<CallQuality, Integer> snapshot2 = it2.next();
            sb.append(" {cq=");
            sb.append(snapshot2.first);
            sb.append(" ss=");
            sb.append(snapshot2.second);
            sb.append("}");
        }
        sb.append("}");
        sb.append(" ");
        sb.append(" mTotalDlGoodQualityTimeMs: ");
        sb.append(this.mTotalDlGoodQualityTimeMs);
        sb.append(" mTotalDlBadQualityTimeMs: ");
        sb.append(this.mTotalDlBadQualityTimeMs);
        sb.append(" mTotalUlGoodQualityTimeMs: ");
        sb.append(this.mTotalUlGoodQualityTimeMs);
        sb.append(" mTotalUlBadQualityTimeMs: ");
        sb.append(this.mTotalUlBadQualityTimeMs);
        sb.append("]");
        return sb.toString();
    }
}
