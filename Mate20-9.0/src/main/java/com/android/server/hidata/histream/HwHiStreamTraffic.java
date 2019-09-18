package com.android.server.hidata.histream;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class HwHiStreamTraffic {
    private static HwHiStreamTraffic mHwHiStreamTraffic;
    private Context mContext;
    private NetworkStatsManager mNetworkStatsManager = ((NetworkStatsManager) this.mContext.getSystemService("netstats"));
    private TelephonyManager mTelephonyManager = TelephonyManager.from(this.mContext);

    private HwHiStreamTraffic(Context context) {
        this.mContext = context;
    }

    public static HwHiStreamTraffic createInstance(Context context) {
        if (mHwHiStreamTraffic == null) {
            mHwHiStreamTraffic = new HwHiStreamTraffic(context);
        }
        return mHwHiStreamTraffic;
    }

    public static HwHiStreamTraffic getInstance() {
        return mHwHiStreamTraffic;
    }

    public long getTotalTraffic(long startTime, long endTime, int uid, int network) {
        long[] traffic = getTraffic(startTime, endTime, uid, network);
        if (traffic == null || 2 > traffic.length) {
            return 0;
        }
        return traffic[0] + traffic[1];
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0087, code lost:
        if (r14 != null) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0089, code lost:
        r14.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00ab, code lost:
        if (r14 == null) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00ae, code lost:
        return r4;
     */
    public long[] getTraffic(long startTime, long endTime, int uid, int network) {
        NetworkStats mNetworkStats;
        int i = uid;
        int i2 = network;
        long[] traffic = new long[2];
        String imsi = this.mTelephonyManager.getSubscriberId(SubscriptionManager.getDefaultDataSubscriptionId());
        NetworkStats mNetworkStats2 = null;
        if (801 == i2 && TextUtils.isEmpty(imsi)) {
            return traffic;
        }
        try {
            NetworkStats.Bucket summaryBucket = new NetworkStats.Bucket();
            if (800 == i2) {
                mNetworkStats = this.mNetworkStatsManager.querySummary(1, imsi, startTime, endTime);
            } else if (801 == i2) {
                mNetworkStats = this.mNetworkStatsManager.querySummary(0, imsi, startTime, endTime);
            } else {
                if (mNetworkStats2 != null) {
                    mNetworkStats2.close();
                }
                return traffic;
            }
            mNetworkStats2 = mNetworkStats;
            long rxBytes = 0;
            long txBytes = 0;
            if (mNetworkStats2 != null) {
                do {
                    mNetworkStats2.getNextBucket(summaryBucket);
                    if (i == 0) {
                        rxBytes += summaryBucket.getRxBytes();
                        txBytes += summaryBucket.getTxBytes();
                    } else if (i > 0 && i == summaryBucket.getUid()) {
                        rxBytes += summaryBucket.getRxBytes();
                        txBytes += summaryBucket.getTxBytes();
                    }
                } while (mNetworkStats2.hasNextBucket());
                traffic[0] = rxBytes;
                traffic[1] = txBytes;
            } else {
                HwHiStreamUtils.logD("mNetworkStats == null");
            }
        } catch (Exception e) {
            HwHiStreamUtils.logD("getMobileTraffic Exception" + e);
        } catch (Throwable th) {
            if (mNetworkStats2 != null) {
                mNetworkStats2.close();
            }
            throw th;
        }
    }
}
