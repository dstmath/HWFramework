package com.android.server.hidata.wavemapping.service;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dataprovider.BehaviorReceiver;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class ActiveCollectDecision {
    private static final int INTERVAL_24HR = 86400000;
    public static final String TAG = ("WMapping." + ActiveCollectDecision.class.getSimpleName());
    private static final int TYPE_BACKGROUND_SCAN = 1;
    private static final int TYPE_OUT4G_REC_SCAN = 3;
    private static final int TYPE_STALL_SCAN = 2;
    private Handler activeCltHandler = new Handler();
    private Runnable activeOut4gScanTimer = new Runnable() {
        /* class com.android.server.hidata.wavemapping.service.ActiveCollectDecision.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            long timeCurr = System.currentTimeMillis();
            long offsetScanDuration = timeCurr - ActiveCollectDecision.this.timeLastScan;
            if (ActiveCollectDecision.this.out4gScanCnt <= 0 || ActiveCollectDecision.this.out4gScanCnt > ActiveCollectDecision.this.paraPeriodOut4gScan.length) {
                LogUtil.d(false, " end OUT-OF-4G periodical scan, Out4gScanCnt=%{public}d", Integer.valueOf(ActiveCollectDecision.this.out4gScanCnt));
                ActiveCollectDecision.this.activeCltHandler.removeCallbacks(ActiveCollectDecision.this.activeOut4gScanTimer);
                return;
            }
            LogUtil.i(false, " run OUT-OF-4G periodical scan, Out4gScanCnt=%{public}d, curTime:%{public}s", Integer.valueOf(ActiveCollectDecision.this.out4gScanCnt), String.valueOf(timeCurr));
            if (ActiveCollectDecision.this.out4gScanCnt < ActiveCollectDecision.this.paraPeriodOut4gScan.length) {
                LogUtil.i(false, " start timer, OUT-OF-4G next period=%{public}d", Integer.valueOf(ActiveCollectDecision.this.paraPeriodOut4gScan[ActiveCollectDecision.this.out4gScanCnt]));
                ActiveCollectDecision.this.activeCltHandler.postDelayed(this, (long) ActiveCollectDecision.this.paraPeriodOut4gScan[ActiveCollectDecision.this.out4gScanCnt]);
                ActiveCollectDecision.access$1108(ActiveCollectDecision.this);
            } else {
                LogUtil.d(false, "  final OUT-OF-4G scan, no timer", new Object[0]);
            }
            if (offsetScanDuration > ((long) ActiveCollectDecision.this.param.getActScanLimitInterval()) && ActiveCollectDecision.this.mWifiManager != null) {
                ActiveCollectDecision.this.mWifiManager.startScan();
                ActiveCollectDecision.access$1308(ActiveCollectDecision.this);
            }
        }
    };
    private Runnable activeScanTimer = new Runnable() {
        /* class com.android.server.hidata.wavemapping.service.ActiveCollectDecision.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            long timeCurr = System.currentTimeMillis();
            long offsetScanDuration = timeCurr - ActiveCollectDecision.this.timeLastScan;
            if (ActiveCollectDecision.this.stallScanCnt > 0 && ActiveCollectDecision.this.stallScanCnt <= ActiveCollectDecision.this.paraPeriodStallScan.length) {
                LogUtil.i(false, " run STALL periodical scan, StallScanCnt=%{public}d, curTime:%{public}s", Integer.valueOf(ActiveCollectDecision.this.stallScanCnt), String.valueOf(timeCurr));
                if (ActiveCollectDecision.this.stallScanCnt < ActiveCollectDecision.this.paraPeriodStallScan.length) {
                    LogUtil.i(false, "  start timer, Stall next period=%{public}d", Integer.valueOf(ActiveCollectDecision.this.paraPeriodStallScan[ActiveCollectDecision.this.stallScanCnt]));
                    ActiveCollectDecision.this.activeCltHandler.postDelayed(this, (long) ActiveCollectDecision.this.paraPeriodStallScan[ActiveCollectDecision.this.stallScanCnt]);
                    ActiveCollectDecision.access$108(ActiveCollectDecision.this);
                } else {
                    LogUtil.d(false, "  final STALL scan, no timer", new Object[0]);
                }
                if (offsetScanDuration > ((long) ActiveCollectDecision.this.param.getActScanLimitInterval()) && ActiveCollectDecision.this.mWifiManager != null) {
                    ActiveCollectDecision.this.mWifiManager.startScan();
                    ActiveCollectDecision.access$608(ActiveCollectDecision.this);
                }
            } else if (ActiveCollectDecision.this.bgScanCnt <= 0 || ActiveCollectDecision.this.bgScanCnt > ActiveCollectDecision.this.paraPeriodBgScan.length) {
                LogUtil.d(false, " end perodical scan, StallScanCnt=%{public}d, BgScanCnt=%{public}d", Integer.valueOf(ActiveCollectDecision.this.stallScanCnt), Integer.valueOf(ActiveCollectDecision.this.bgScanCnt));
                ActiveCollectDecision.this.activeCltHandler.removeCallbacks(ActiveCollectDecision.this.activeScanTimer);
            } else {
                LogUtil.i(false, " run BACKGROUND perodical scan, BgScanCnt=%{public}d, curTime:%{public}s", Integer.valueOf(ActiveCollectDecision.this.bgScanCnt), String.valueOf(timeCurr));
                if (ActiveCollectDecision.this.bgScanCnt < ActiveCollectDecision.this.paraPeriodBgScan.length) {
                    LogUtil.i(false, "  start timer, BACKGROUND next period=%{public}d", Integer.valueOf(ActiveCollectDecision.this.paraPeriodBgScan[ActiveCollectDecision.this.bgScanCnt]));
                    ActiveCollectDecision.this.activeCltHandler.postDelayed(this, (long) ActiveCollectDecision.this.paraPeriodBgScan[ActiveCollectDecision.this.bgScanCnt]);
                    ActiveCollectDecision.access$708(ActiveCollectDecision.this);
                } else {
                    LogUtil.d(false, "  final BACKGROUND scan", new Object[0]);
                }
                if (offsetScanDuration > ((long) ActiveCollectDecision.this.param.getActScanLimitInterval()) && ActiveCollectDecision.this.mWifiManager != null) {
                    ActiveCollectDecision.this.mWifiManager.startScan();
                    ActiveCollectDecision.access$908(ActiveCollectDecision.this);
                }
            }
        }
    };
    private int bgScanCnt = 0;
    private int bgScanDailyCnt = 0;
    private BehaviorReceiver mBehaviorHandler = null;
    Context mCtx;
    private WifiManager mWifiManager;
    private int out4gScanCnt = 0;
    private int out4gScanDailyCnt = 0;
    private int[] paraPeriodBgScan;
    private int[] paraPeriodOut4gScan;
    private int[] paraPeriodStallScan;
    private ParameterInfo param = null;
    private int stallScanCnt = 0;
    private int stallScanDailyCnt = 0;
    private long time1stCollect = 0;
    private long timeLastScan = 0;

    static /* synthetic */ int access$108(ActiveCollectDecision x0) {
        int i = x0.stallScanCnt;
        x0.stallScanCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$1108(ActiveCollectDecision x0) {
        int i = x0.out4gScanCnt;
        x0.out4gScanCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$1308(ActiveCollectDecision x0) {
        int i = x0.out4gScanDailyCnt;
        x0.out4gScanDailyCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$608(ActiveCollectDecision x0) {
        int i = x0.stallScanDailyCnt;
        x0.stallScanDailyCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$708(ActiveCollectDecision x0) {
        int i = x0.bgScanCnt;
        x0.bgScanCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$908(ActiveCollectDecision x0) {
        int i = x0.bgScanDailyCnt;
        x0.bgScanDailyCnt = i + 1;
        return i;
    }

    public ActiveCollectDecision(Context ctx, BehaviorReceiver behaviorReceiver) {
        LogUtil.i(false, "ActiveCollectDecision", new Object[0]);
        this.mCtx = ctx;
        try {
            this.mBehaviorHandler = behaviorReceiver;
            this.mWifiManager = (WifiManager) this.mCtx.getSystemService("wifi");
            LogUtil.i(false, "ParamManager init begin.", new Object[0]);
            this.param = ParamManager.getInstance().getParameterInfo();
            this.paraPeriodBgScan = this.param.getActBgScanPeriods();
            this.paraPeriodStallScan = this.param.getActStallScanPeriods();
            this.paraPeriodOut4gScan = this.param.getActOut4gScanPeriods();
        } catch (Exception e) {
            LogUtil.e(false, "ActiveCollectDecision failed by Exception", new Object[0]);
        }
    }

    public boolean isStartBgScan() {
        LogUtil.i(false, "isStartBgScan:", new Object[0]);
        if (this.bgScanDailyCnt > this.param.getActScanLimitBg() || this.bgScanDailyCnt + this.stallScanDailyCnt > this.param.getActScanLimitTotal()) {
            LogUtil.d(false, " active scan reach limitation, BgScanDailyCnt=%{public}d, StallScanDailyCnt=%{public}d", Integer.valueOf(this.bgScanDailyCnt), Integer.valueOf(this.stallScanDailyCnt));
        } else {
            BehaviorReceiver behaviorReceiver = this.mBehaviorHandler;
            boolean isScreenOn = BehaviorReceiver.getScreenState();
            BehaviorReceiver behaviorReceiver2 = this.mBehaviorHandler;
            boolean isArOn = BehaviorReceiver.getArState();
            if (isScreenOn && isArOn) {
                LogUtil.i(false, " screen on & stable, start active BACKGROUND scan", new Object[0]);
                isStartPeriodicScan(1);
                return true;
            }
        }
        this.timeLastScan = System.currentTimeMillis();
        return false;
    }

    public void stopBgScan() {
        LogUtil.i(false, "stopBgScan:", new Object[0]);
        this.bgScanCnt = 0;
        this.activeCltHandler.removeCallbacks(this.activeScanTimer);
    }

    public boolean isStartStallScan() {
        LogUtil.i(false, "isStartStallScan:", new Object[0]);
        if (this.stallScanDailyCnt > this.param.getActScanLimitStall() || this.bgScanDailyCnt + this.stallScanDailyCnt > this.param.getActScanLimitTotal()) {
            LogUtil.d(false, " active scan reach limitation, StallScanDailyCnt=%{public}d, BgScanDailyCnt=%{public}d", Integer.valueOf(this.stallScanDailyCnt), Integer.valueOf(this.bgScanDailyCnt));
        } else {
            BehaviorReceiver behaviorReceiver = this.mBehaviorHandler;
            boolean isScreenOn = BehaviorReceiver.getScreenState();
            BehaviorReceiver behaviorReceiver2 = this.mBehaviorHandler;
            boolean isArOn = BehaviorReceiver.getArState();
            if (isScreenOn && isArOn) {
                LogUtil.i(false, " screen on & stable, start active STALL scan", new Object[0]);
                isStartPeriodicScan(2);
                return true;
            }
        }
        this.timeLastScan = System.currentTimeMillis();
        return false;
    }

    public boolean isStartFurtherSpaceScan() {
        LogUtil.i(false, "startAddtionalScan:", new Object[0]);
        BehaviorReceiver behaviorReceiver = this.mBehaviorHandler;
        boolean isScreenOn = BehaviorReceiver.getScreenState();
        BehaviorReceiver behaviorReceiver2 = this.mBehaviorHandler;
        boolean isArOn = BehaviorReceiver.getArState();
        if (!isScreenOn || !isArOn) {
            return false;
        }
        LogUtil.i(false, " screen on & stable, start further space scan", new Object[0]);
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            return true;
        }
        wifiManager.startScan();
        return true;
    }

    public void stopStallScan() {
        LogUtil.i(false, "stopStallScan:", new Object[0]);
        this.stallScanCnt = 0;
        this.activeCltHandler.removeCallbacks(this.activeScanTimer);
    }

    public void triggerRecogScan() {
        LogUtil.i(false, "triggerRecogScan:", new Object[0]);
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager != null) {
            wifiManager.startScan();
        }
    }

    private boolean isStartPeriodicScan(int type) {
        LogUtil.i(false, "isStartPeriodicScan, type=%{public}d", Integer.valueOf(type));
        long offsetScanDuration = System.currentTimeMillis() - this.timeLastScan;
        if (this.time1stCollect == 0) {
            this.time1stCollect = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - this.time1stCollect > Constant.MILLISEC_ONE_DAY) {
            this.time1stCollect = 0;
            this.stallScanDailyCnt = 0;
            this.bgScanDailyCnt = 0;
            this.out4gScanDailyCnt = 0;
        }
        if (type == 2) {
            stallScan(offsetScanDuration);
        } else if (type == 1) {
            backgroundScan();
        } else if (type == 3) {
            recognitionScan(offsetScanDuration);
        }
        return true;
    }

    private void stallScan(long scanDuration) {
        WifiManager wifiManager;
        int i = this.stallScanCnt;
        if (i == 0) {
            if (scanDuration > ((long) this.param.getActScanLimitInterval()) && (wifiManager = this.mWifiManager) != null) {
                wifiManager.startScan();
                this.stallScanDailyCnt++;
            }
            int i2 = this.bgScanCnt;
            if (i2 > 0) {
                this.stallScanCnt = i2;
                this.activeCltHandler.removeCallbacks(this.activeScanTimer);
                LogUtil.d(false, " BACKGROUND scan was already started, BgScanCnt=%{public}d", Integer.valueOf(this.bgScanCnt));
            }
            int i3 = this.stallScanCnt;
            if (i3 < this.paraPeriodStallScan.length) {
                LogUtil.i(false, " run STALL active scan timer, StallScanCnt=%{public}d, next period=%{public}d", Integer.valueOf(i3), Integer.valueOf(this.paraPeriodStallScan[this.stallScanCnt]));
                this.activeCltHandler.postDelayed(this.activeScanTimer, (long) this.paraPeriodStallScan[this.stallScanCnt]);
            }
            this.stallScanCnt++;
            return;
        }
        LogUtil.d(false, " periodical STALL scan was already started, StallScanCnt=%{public}d, BgScanCnt=%{public}d", Integer.valueOf(i), Integer.valueOf(this.bgScanCnt));
    }

    private void backgroundScan() {
        int i = this.stallScanCnt;
        int i2 = this.bgScanCnt;
        if (i + i2 == 0) {
            LogUtil.i(false, " run BACKGROUND active scan timer, BgScanCnt=%{public}d, next period=%{public}d", Integer.valueOf(i2), Integer.valueOf(this.paraPeriodBgScan[this.bgScanCnt]));
            this.activeCltHandler.postDelayed(this.activeScanTimer, (long) this.paraPeriodBgScan[this.bgScanCnt]);
            this.bgScanCnt++;
            return;
        }
        LogUtil.d(false, " periodical scan was already started, StallScanCnt=%{public}d, BgScanCnt=%{public}d", Integer.valueOf(i), Integer.valueOf(this.bgScanCnt));
    }

    private void recognitionScan(long scanDuration) {
        WifiManager wifiManager;
        int i = this.out4gScanCnt;
        if (i == 0) {
            if (scanDuration > ((long) this.param.getActScanLimitInterval()) && (wifiManager = this.mWifiManager) != null) {
                wifiManager.startScan();
                this.out4gScanDailyCnt++;
            }
            int i2 = this.out4gScanCnt;
            if (i2 < this.paraPeriodOut4gScan.length) {
                LogUtil.i(false, " run STALL active scan timer, Out4gScanCnt=%{public}d, next period=%{public}d", Integer.valueOf(i2), Integer.valueOf(this.paraPeriodOut4gScan[this.out4gScanCnt]));
                this.activeCltHandler.postDelayed(this.activeOut4gScanTimer, (long) this.paraPeriodOut4gScan[this.out4gScanCnt]);
            }
            this.out4gScanCnt++;
            return;
        }
        LogUtil.d(false, " periodical STALL scan was already started, StallScanCnt=%{public}d", Integer.valueOf(i));
    }

    public boolean isStartOut4gRecgScan() {
        LogUtil.i(false, "isStartOut4gRecgScan:", new Object[0]);
        if (this.out4gScanDailyCnt > this.param.getActScanLimitStall()) {
            LogUtil.d(false, " active scan reach limitation, Out4gScanDailyCnt=%{public}d", Integer.valueOf(this.out4gScanDailyCnt));
        } else {
            BehaviorReceiver behaviorReceiver = this.mBehaviorHandler;
            if (BehaviorReceiver.getScreenState()) {
                LogUtil.i(false, " screen on, start active OUT-OF-4G scan", new Object[0]);
                isStartPeriodicScan(3);
                return true;
            }
        }
        this.timeLastScan = System.currentTimeMillis();
        return false;
    }

    public void stopOut4gRecgScan() {
        LogUtil.i(false, "stopOut4gRecgScan:", new Object[0]);
        this.out4gScanCnt = 0;
        this.activeCltHandler.removeCallbacks(this.activeOut4gScanTimer);
    }
}
