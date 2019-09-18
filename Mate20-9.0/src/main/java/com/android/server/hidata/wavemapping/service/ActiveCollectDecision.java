package com.android.server.hidata.wavemapping.service;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dataprovider.BehaviorReceiver;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;

public class ActiveCollectDecision {
    private static final int INTERVAL_24HR = 86400000;
    public static final String TAG = ("WMapping." + ActiveCollectDecision.class.getSimpleName());
    private static final int TYPE_BACKGROUND_SCAN = 1;
    private static final int TYPE_OUT4G_REC_SCAN = 3;
    private static final int TYPE_STALL_SCAN = 2;
    /* access modifiers changed from: private */
    public Handler activeCltHandler = new Handler();
    /* access modifiers changed from: private */
    public Runnable activeOut4gScanTimer = new Runnable() {
        public void run() {
            long timeCurr = System.currentTimeMillis();
            long offsetScanDuration = timeCurr - ActiveCollectDecision.this.timeLastScan;
            if (ActiveCollectDecision.this.iOut4gScanCnt <= 0 || ActiveCollectDecision.this.iOut4gScanCnt > ActiveCollectDecision.this.paraPeriodOut4gScan.length) {
                LogUtil.d(" end OUT-OF-4G perodical scan, Out4gScanCnt=" + ActiveCollectDecision.this.iOut4gScanCnt);
                ActiveCollectDecision.this.activeCltHandler.removeCallbacks(ActiveCollectDecision.this.activeOut4gScanTimer);
                return;
            }
            LogUtil.i(" run OUT-OF-4G perodical scan, Out4gScanCnt=" + ActiveCollectDecision.this.iOut4gScanCnt + ", curTime:" + timeCurr);
            if (ActiveCollectDecision.this.iOut4gScanCnt < ActiveCollectDecision.this.paraPeriodOut4gScan.length) {
                LogUtil.i("  start timer, OUT-OF-4G next period=" + ActiveCollectDecision.this.paraPeriodOut4gScan[ActiveCollectDecision.this.iOut4gScanCnt]);
                ActiveCollectDecision.this.activeCltHandler.postDelayed(this, (long) ActiveCollectDecision.this.paraPeriodOut4gScan[ActiveCollectDecision.this.iOut4gScanCnt]);
                int unused = ActiveCollectDecision.this.iOut4gScanCnt = ActiveCollectDecision.this.iOut4gScanCnt + 1;
            } else {
                LogUtil.d("  final OUT-OF-4G scan, no timer");
            }
            if (offsetScanDuration > ((long) ActiveCollectDecision.this.param.getActScanLimit_interval()) && ActiveCollectDecision.this.mwifimanager != null) {
                ActiveCollectDecision.this.mwifimanager.startScan();
                int unused2 = ActiveCollectDecision.this.iOut4gScanDailyCnt = ActiveCollectDecision.this.iOut4gScanDailyCnt + 1;
            }
        }
    };
    /* access modifiers changed from: private */
    public Runnable activeScanTimer = new Runnable() {
        public void run() {
            long timeCurr = System.currentTimeMillis();
            long offsetScanDuration = timeCurr - ActiveCollectDecision.this.timeLastScan;
            if (ActiveCollectDecision.this.iStallScanCnt > 0 && ActiveCollectDecision.this.iStallScanCnt <= ActiveCollectDecision.this.paraPeriodStallScan.length) {
                LogUtil.i(" run STALL perodical scan, StallScanCnt=" + ActiveCollectDecision.this.iStallScanCnt + ", curTime:" + timeCurr);
                if (ActiveCollectDecision.this.iStallScanCnt < ActiveCollectDecision.this.paraPeriodStallScan.length) {
                    LogUtil.i("  start timer, Stall next period=" + ActiveCollectDecision.this.paraPeriodStallScan[ActiveCollectDecision.this.iStallScanCnt]);
                    ActiveCollectDecision.this.activeCltHandler.postDelayed(this, (long) ActiveCollectDecision.this.paraPeriodStallScan[ActiveCollectDecision.this.iStallScanCnt]);
                    int unused = ActiveCollectDecision.this.iStallScanCnt = ActiveCollectDecision.this.iStallScanCnt + 1;
                } else {
                    LogUtil.d("  final STALL scan, no timer");
                }
                if (offsetScanDuration > ((long) ActiveCollectDecision.this.param.getActScanLimit_interval()) && ActiveCollectDecision.this.mwifimanager != null) {
                    ActiveCollectDecision.this.mwifimanager.startScan();
                    int unused2 = ActiveCollectDecision.this.iStallScanDailyCnt = ActiveCollectDecision.this.iStallScanDailyCnt + 1;
                }
            } else if (ActiveCollectDecision.this.iBgScanCnt <= 0 || ActiveCollectDecision.this.iBgScanCnt > ActiveCollectDecision.this.paraPeriodBgScan.length) {
                LogUtil.d(" end perodical scan, StallScanCnt=" + ActiveCollectDecision.this.iStallScanCnt + ", BgScanCnt=" + ActiveCollectDecision.this.iBgScanCnt);
                ActiveCollectDecision.this.activeCltHandler.removeCallbacks(ActiveCollectDecision.this.activeScanTimer);
            } else {
                LogUtil.i(" run BACKGROUND perodical scan, BgScanCnt=" + ActiveCollectDecision.this.iBgScanCnt + ", curTime:" + timeCurr);
                if (ActiveCollectDecision.this.iBgScanCnt < ActiveCollectDecision.this.paraPeriodBgScan.length) {
                    LogUtil.i("  start timer, BACKGROUND next period=" + ActiveCollectDecision.this.paraPeriodBgScan[ActiveCollectDecision.this.iBgScanCnt]);
                    ActiveCollectDecision.this.activeCltHandler.postDelayed(this, (long) ActiveCollectDecision.this.paraPeriodBgScan[ActiveCollectDecision.this.iBgScanCnt]);
                    int unused3 = ActiveCollectDecision.this.iBgScanCnt = ActiveCollectDecision.this.iBgScanCnt + 1;
                } else {
                    LogUtil.d("  final BACKGROUND scan");
                }
                if (offsetScanDuration > ((long) ActiveCollectDecision.this.param.getActScanLimit_interval()) && ActiveCollectDecision.this.mwifimanager != null) {
                    ActiveCollectDecision.this.mwifimanager.startScan();
                    int unused4 = ActiveCollectDecision.this.iBgScanDailyCnt = ActiveCollectDecision.this.iBgScanDailyCnt + 1;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int iBgScanCnt = 0;
    /* access modifiers changed from: private */
    public int iBgScanDailyCnt = 0;
    /* access modifiers changed from: private */
    public int iOut4gScanCnt = 0;
    /* access modifiers changed from: private */
    public int iOut4gScanDailyCnt = 0;
    /* access modifiers changed from: private */
    public int iStallScanCnt = 0;
    /* access modifiers changed from: private */
    public int iStallScanDailyCnt = 0;
    private BehaviorReceiver mBehaviorHandler = null;
    Context mCtx;
    /* access modifiers changed from: private */
    public WifiManager mwifimanager;
    /* access modifiers changed from: private */
    public int[] paraPeriodBgScan;
    /* access modifiers changed from: private */
    public int[] paraPeriodOut4gScan;
    /* access modifiers changed from: private */
    public int[] paraPeriodStallScan;
    /* access modifiers changed from: private */
    public ParameterInfo param = null;
    private long time1stCollect = 0;
    /* access modifiers changed from: private */
    public long timeLastScan = 0;

    public ActiveCollectDecision(Context ctx, BehaviorReceiver behaviorReceiver) {
        LogUtil.i("ActiveCollectDecision");
        this.mCtx = ctx;
        try {
            this.mBehaviorHandler = behaviorReceiver;
            this.mwifimanager = (WifiManager) this.mCtx.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
            LogUtil.i("ParamManager init begin.");
            this.param = ParamManager.getInstance().getParameterInfo();
            this.paraPeriodBgScan = this.param.getActBgScanPeriods();
            this.paraPeriodStallScan = this.param.getActStallScanPeriods();
            this.paraPeriodOut4gScan = this.param.getActOut4gScanPeriods();
        } catch (Exception e) {
            LogUtil.e("ActiveCollectDecision:" + e.getMessage());
        }
    }

    public boolean startBgScan() {
        LogUtil.i("startBgScan:");
        if (this.iBgScanDailyCnt > this.param.getActScanLimit_bg() || this.iBgScanDailyCnt + this.iStallScanDailyCnt > this.param.getActScanLimit_total()) {
            LogUtil.d(" active scan reach limitation, BgScanDailyCnt=" + this.iBgScanDailyCnt + ", StallScanDailyCnt=" + this.iStallScanDailyCnt);
        } else {
            BehaviorReceiver behaviorReceiver = this.mBehaviorHandler;
            boolean bScreenState = BehaviorReceiver.getScrnState();
            BehaviorReceiver behaviorReceiver2 = this.mBehaviorHandler;
            boolean bArState = BehaviorReceiver.getArState();
            if (bScreenState && bArState) {
                LogUtil.i(" screen on & stable, start active BACKGROUND scan");
                startPerodicScan(1);
                return true;
            }
        }
        this.timeLastScan = System.currentTimeMillis();
        return false;
    }

    public void stopBgScan() {
        LogUtil.i("stopBgScan:");
        this.iBgScanCnt = 0;
        this.activeCltHandler.removeCallbacks(this.activeScanTimer);
    }

    public boolean startStallScan() {
        LogUtil.i("startStallScan:");
        if (this.iStallScanDailyCnt > this.param.getActScanLimit_stall() || this.iBgScanDailyCnt + this.iStallScanDailyCnt > this.param.getActScanLimit_total()) {
            LogUtil.d(" active scan reach limitation, StallScanDailyCnt=" + this.iStallScanDailyCnt + ", BgScanDailyCnt=" + this.iBgScanDailyCnt);
        } else {
            BehaviorReceiver behaviorReceiver = this.mBehaviorHandler;
            boolean bScreenState = BehaviorReceiver.getScrnState();
            BehaviorReceiver behaviorReceiver2 = this.mBehaviorHandler;
            boolean bArState = BehaviorReceiver.getArState();
            if (bScreenState && bArState) {
                LogUtil.i(" screen on & stable, start active STALL scan");
                startPerodicScan(2);
                return true;
            }
        }
        this.timeLastScan = System.currentTimeMillis();
        return false;
    }

    public boolean startFurtherSpaceScan() {
        LogUtil.i("startAddtionalScan:");
        BehaviorReceiver behaviorReceiver = this.mBehaviorHandler;
        boolean bScreenState = BehaviorReceiver.getScrnState();
        BehaviorReceiver behaviorReceiver2 = this.mBehaviorHandler;
        boolean bArState = BehaviorReceiver.getArState();
        if (!bScreenState || !bArState) {
            return false;
        }
        LogUtil.i(" screen on & stable, start further space scan");
        if (this.mwifimanager != null) {
            this.mwifimanager.startScan();
        }
        return true;
    }

    public void stopStallScan() {
        LogUtil.i("stopStallScan:");
        this.iStallScanCnt = 0;
        this.activeCltHandler.removeCallbacks(this.activeScanTimer);
    }

    public void triggerRecogScan() {
        LogUtil.i("triggerRecogScan:");
        if (this.mwifimanager != null) {
            this.mwifimanager.startScan();
        }
    }

    private boolean startPerodicScan(int type) {
        LogUtil.i("startPerodicScan, type=" + type);
        long offsetScanDuration = System.currentTimeMillis() - this.timeLastScan;
        if (0 == this.time1stCollect) {
            this.time1stCollect = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - this.time1stCollect > 86400000) {
            this.time1stCollect = 0;
            this.iStallScanDailyCnt = 0;
            this.iBgScanDailyCnt = 0;
            this.iOut4gScanDailyCnt = 0;
        }
        if (2 == type) {
            if (this.iStallScanCnt == 0) {
                if (offsetScanDuration > ((long) this.param.getActScanLimit_interval()) && this.mwifimanager != null) {
                    this.mwifimanager.startScan();
                    this.iStallScanDailyCnt++;
                }
                if (this.iBgScanCnt > 0) {
                    this.iStallScanCnt = this.iBgScanCnt;
                    this.activeCltHandler.removeCallbacks(this.activeScanTimer);
                    LogUtil.d(" BACKGROUND scan was already started, BgScanCnt=" + this.iBgScanCnt);
                }
                if (this.iStallScanCnt < this.paraPeriodStallScan.length) {
                    LogUtil.i(" run STALL active scan timer, StallScanCnt=" + this.iStallScanCnt + ", next period=" + this.paraPeriodStallScan[this.iStallScanCnt]);
                    this.activeCltHandler.postDelayed(this.activeScanTimer, (long) this.paraPeriodStallScan[this.iStallScanCnt]);
                }
                this.iStallScanCnt++;
            } else {
                LogUtil.d(" periodical STALL scan was already started, StallScanCnt=" + this.iStallScanCnt + ", BgScanCnt=" + this.iBgScanCnt);
            }
        } else if (1 == type) {
            if (this.iStallScanCnt + this.iBgScanCnt == 0) {
                LogUtil.i(" run BACKGROUND active scan timer, BgScanCnt=" + this.iBgScanCnt + ", next period=" + this.paraPeriodBgScan[this.iBgScanCnt]);
                this.activeCltHandler.postDelayed(this.activeScanTimer, (long) this.paraPeriodBgScan[this.iBgScanCnt]);
                this.iBgScanCnt = this.iBgScanCnt + 1;
            } else {
                LogUtil.d(" periodical scan was already started, StallScanCnt=" + this.iStallScanCnt + ", BgScanCnt=" + this.iBgScanCnt);
            }
        } else if (3 == type) {
            if (this.iOut4gScanCnt == 0) {
                if (offsetScanDuration > ((long) this.param.getActScanLimit_interval()) && this.mwifimanager != null) {
                    this.mwifimanager.startScan();
                    this.iOut4gScanDailyCnt++;
                }
                if (this.iOut4gScanCnt < this.paraPeriodOut4gScan.length) {
                    LogUtil.i(" run STALL active scan timer, Out4gScanCnt=" + this.iOut4gScanCnt + ", next period=" + this.paraPeriodOut4gScan[this.iOut4gScanCnt]);
                    this.activeCltHandler.postDelayed(this.activeOut4gScanTimer, (long) this.paraPeriodOut4gScan[this.iOut4gScanCnt]);
                }
                this.iOut4gScanCnt++;
            } else {
                LogUtil.d(" periodical STALL scan was already started, StallScanCnt=" + this.iOut4gScanCnt);
            }
        }
        return true;
    }

    public boolean startOut4gRecgScan() {
        LogUtil.i("startOut4gRecgScan:");
        if (this.iOut4gScanDailyCnt > this.param.getActScanLimit_stall()) {
            LogUtil.d(" active scan reach limitation, Out4gScanDailyCnt=" + this.iOut4gScanDailyCnt);
        } else {
            BehaviorReceiver behaviorReceiver = this.mBehaviorHandler;
            if (BehaviorReceiver.getScrnState()) {
                LogUtil.i(" screen on, start active OUT-OF-4G scan");
                startPerodicScan(3);
                return true;
            }
        }
        this.timeLastScan = System.currentTimeMillis();
        return false;
    }

    public void stopOut4gRecgScan() {
        LogUtil.i("stopOut4gRecgScan:");
        this.iOut4gScanCnt = 0;
        this.activeCltHandler.removeCallbacks(this.activeOut4gScanTimer);
    }
}
