package com.android.server.wifi.HwQoE;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.wifi.HwHiLog;

public class HwQoEGameCHRImpl {
    public static final int AP_TYPE_24G_BT_COEXIST = 2;
    public static final int AP_TYPE_24G_ONLY = 1;
    public static final int AP_TYPE_5G_ONLY = 3;
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final int DS_KOG_LATENCY_100 = 100;
    private static final int DS_KOG_LATENCY_200 = 200;
    private static final int DS_KOG_LATENCY_300 = 300;
    private static final int DS_KOG_LATENCY_400 = 400;
    private static final int DS_KOG_RTT_STATIC_REPORT = 2;
    private static final int GAME_BAD_MAX_TCP_RTT = 460;
    private static final int GAME_GENERAL_MAX_ARP_RTT = 100;
    private static final int GAME_GENERAL_MAX_RTT = 200;
    private static final int GAME_GENERAL_MAX_TCP_RTT = 100;
    private static final int GAME_POOR_MAX_ARP_RTT = 200;
    private static final int GAME_POOR_MAX_RTT = 460;
    private static final int GAME_POOR_MAX_TCP_RTT = 200;
    private static final int GAME_SMOOTH_MAX_ARP_RTT = 30;
    private static final int GAME_SMOOTH_MAX_RTT = 100;
    private static final int GAME_SMOOTH_MAX_TCP_RTT = 50;
    private static final int GAME_STAT_MOBILE_ENUM_234G = 0;
    private static final int GAME_STAT_MOBILE_ENUM_ENDC = 1;
    private static final int GAME_STAT_MOBILE_ENUM_NR = 2;
    private static final int GAME_STAT_MOBILE_NUM = 3;
    private static final int GAME_UNKOWN_RTT = 0;
    private static final String INTENT_DS_KOG_RTT_REPORT = "com.huawei.intent.action.kog_rtt_report";
    private static final int NSA_STATE0 = 0;
    private static final int NSA_STATE1 = 1;
    private static final int NSA_STATE2 = 2;
    private static final int NSA_STATE3 = 3;
    private static final int NSA_STATE4 = 4;
    private static final int NSA_STATE5 = 5;
    private static final int NSA_STATE6 = 6;
    private static final int SECOND_TO_MILLI_SECONDS = 1000;
    private static final String TAG = "HiDATA_GameCHRImpl";
    private boolean mAllowStatistics;
    private int mApType;
    private long mArpRttGeneralDuration;
    private long mArpRttGeneralStartTime;
    private long mArpRttPoorDuration;
    private long mArpRttPoorStartTime;
    private long mArpRttSmoothDuration;
    private long mArpRttSmoothStartTime;
    private short mBTScan24GCounter;
    private Context mContext;
    private int mCurrMobileRegTypeEnum = 0;
    private long mCurrentGameRTT;
    private final int[] mDsKogRtt100Count = new int[3];
    private final int[] mDsKogRtt200Count = new int[3];
    private final int[] mDsKogRtt300Count = new int[3];
    private final int[] mDsKogRtt400Count = new int[3];
    private final int[] mDsKogRttOver400Count = new int[3];
    private final int[] mDsKogTotalRttCount = new int[3];
    private final int[] mDsKogTotalRttSum = new int[3];
    private long mGameRTTGeneralStartTime;
    private long mGameRTTPoorStartTime;
    private long mGameRTTSmoothStartTime;
    private HwWifiGameNetChrInfo mHwWifiGameNetChrInfo = new HwWifiGameNetChrInfo();
    private int mLastArpRttLevel;
    private int mLastGameNetworkLevel;
    private long mLastGeneralStartTime;
    private long mLastPoorStartTime;
    private long mLastSmoothStartTime;
    private int mLastTcpRttLevel;
    private BluetoothAdapter mLocalBluetoothAdapter;
    private final long[] mMobileGameRttGeneralStartTime = new long[3];
    private final long[] mMobileGameRttPoorStartTime = new long[3];
    private final long[] mMobileGameRttSmoothStartTime = new long[3];
    private final long[] mMobileLastGeneralStartTime = new long[3];
    private final long[] mMobileLastPoorStartTime = new long[3];
    private final long[] mMobileLastSmoothStartTime = new long[3];
    private final long[] mMobileNetworkGeneralDuration = new long[3];
    private final long[] mMobileNetworkPoorDuration = new long[3];
    private final long[] mMobileNetworkSmoothDuration = new long[3];
    private final long[] mMobileTcpRttBadDuration = new long[3];
    private final long[] mMobileTcpRttBadStartTime = new long[3];
    private final long[] mMobileTcpRttGeneralDuration = new long[3];
    private final long[] mMobileTcpRttGeneralStartTime = new long[3];
    private final long[] mMobileTcpRttPoorDuration = new long[3];
    private final long[] mMobileTcpRttPoorStartTime = new long[3];
    private final long[] mMobileTcpRttSmoothDuration = new long[3];
    private final long[] mMobileTcpRttSmoothStartTime = new long[3];
    private final long[] mMobileTotalGameGeneralRtt = new long[3];
    private final long[] mMobileTotalGamePoorRtt = new long[3];
    private final long[] mMobileTotalGameRTT = new long[3];
    private final long[] mMobileTotalGameSmoothRtt = new long[3];
    private long mNetworkGeneralDuration;
    private long mNetworkPoorDuration;
    private long mNetworkSmoothDuration;
    private int mOldMobileRegTypeEnum = 0;
    private long mTcpRttBadDuration;
    private long mTcpRttBadStartTime;
    private long mTcpRttGeneralDuration;
    private long mTcpRttGeneralStartTime;
    private long mTcpRttPoorDuration;
    private long mTcpRttPoorStartTime;
    private long mTcpRttSmoothDuration;
    private long mTcpRttSmoothStartTime;
    private int mTencentTmgpGameType;
    private long mTotalGameGeneralRTT;
    private long mTotalGamePoorRTT;
    private long mTotalGameSmoothRTT;
    private short mWifiArpFailCounter;
    private short mWifiDisCounter;
    private short mWifiRoamingCounter;
    private short mWifiScanCounter;
    private long mlastGameWarStartTime;

    public HwQoEGameCHRImpl(Context context) {
        this.mContext = context;
    }

    private void initialGameMobileChr() {
        for (int i = 0; i < 3; i++) {
            this.mMobileGameRttSmoothStartTime[i] = 0;
            this.mMobileGameRttGeneralStartTime[i] = 0;
            this.mMobileGameRttPoorStartTime[i] = 0;
            this.mMobileNetworkSmoothDuration[i] = 0;
            this.mMobileNetworkGeneralDuration[i] = 0;
            this.mMobileNetworkPoorDuration[i] = 0;
            this.mMobileTcpRttSmoothStartTime[i] = 0;
            this.mMobileTcpRttGeneralStartTime[i] = 0;
            this.mMobileTcpRttPoorStartTime[i] = 0;
            this.mMobileTcpRttBadStartTime[i] = 0;
            this.mMobileTcpRttSmoothDuration[i] = 0;
            this.mMobileTcpRttGeneralDuration[i] = 0;
            this.mMobileTcpRttPoorDuration[i] = 0;
            this.mMobileTcpRttBadDuration[i] = 0;
            this.mDsKogRtt100Count[i] = 0;
            this.mDsKogRtt200Count[i] = 0;
            this.mDsKogRtt300Count[i] = 0;
            this.mDsKogRtt400Count[i] = 0;
            this.mDsKogRttOver400Count[i] = 0;
            this.mDsKogTotalRttSum[i] = 0;
            this.mDsKogTotalRttCount[i] = 0;
        }
    }

    private void initialGameCHR() {
        this.mGameRTTSmoothStartTime = 0;
        this.mGameRTTGeneralStartTime = 0;
        this.mGameRTTPoorStartTime = 0;
        this.mNetworkSmoothDuration = 0;
        this.mNetworkGeneralDuration = 0;
        this.mNetworkPoorDuration = 0;
        this.mArpRttSmoothStartTime = 0;
        this.mArpRttGeneralStartTime = 0;
        this.mArpRttPoorStartTime = 0;
        this.mArpRttSmoothDuration = 0;
        this.mArpRttGeneralDuration = 0;
        this.mArpRttPoorDuration = 0;
        this.mTcpRttSmoothStartTime = 0;
        this.mTcpRttGeneralStartTime = 0;
        this.mTcpRttPoorStartTime = 0;
        this.mTcpRttBadStartTime = 0;
        this.mTcpRttSmoothDuration = 0;
        this.mTcpRttGeneralDuration = 0;
        this.mTcpRttPoorDuration = 0;
        this.mTcpRttBadDuration = 0;
        initialGameMobileChr();
        this.mLastGameNetworkLevel = 0;
        this.mLastArpRttLevel = 0;
        this.mLastTcpRttLevel = 0;
        this.mWifiRoamingCounter = 0;
        this.mWifiArpFailCounter = 0;
        this.mApType = 1;
        this.mWifiDisCounter = 0;
        this.mAllowStatistics = false;
        this.mWifiScanCounter = 0;
        this.mBTScan24GCounter = 0;
        this.mHwWifiGameNetChrInfo.clean();
    }

    private void handleSmoothRtt(long rtt) {
        if (this.mLastGameNetworkLevel != 100) {
            this.mGameRTTSmoothStartTime = System.currentTimeMillis();
        }
        if (this.mLastGameNetworkLevel == 200 && this.mGameRTTGeneralStartTime != 0) {
            this.mNetworkGeneralDuration += System.currentTimeMillis() - this.mGameRTTGeneralStartTime;
            this.mTotalGameGeneralRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastGeneralStartTime);
            this.mGameRTTGeneralStartTime = 0;
        } else if (this.mLastGameNetworkLevel != 460 || this.mGameRTTPoorStartTime == 0) {
            int i = this.mLastGameNetworkLevel;
            if (i == 0) {
                this.mNetworkSmoothDuration = this.mGameRTTSmoothStartTime - this.mlastGameWarStartTime;
                this.mTotalGameSmoothRTT = this.mNetworkSmoothDuration * rtt;
            } else if (i == 100) {
                this.mTotalGameSmoothRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastSmoothStartTime);
            } else {
                logD(false, "handleSmoothRtt mLastGameNetworkLevel:%{public}d", Integer.valueOf(i));
            }
        } else {
            this.mNetworkPoorDuration += System.currentTimeMillis() - this.mGameRTTPoorStartTime;
            this.mTotalGamePoorRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastPoorStartTime);
            this.mGameRTTPoorStartTime = 0;
        }
        this.mLastGameNetworkLevel = 100;
        this.mLastSmoothStartTime = System.currentTimeMillis();
    }

    private void handleGeneralRtt(long rtt) {
        if (this.mLastGameNetworkLevel != 200) {
            this.mGameRTTGeneralStartTime = System.currentTimeMillis();
        }
        if (this.mLastGameNetworkLevel == 100 && this.mGameRTTSmoothStartTime != 0) {
            this.mNetworkSmoothDuration += System.currentTimeMillis() - this.mGameRTTSmoothStartTime;
            this.mTotalGameSmoothRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastSmoothStartTime);
            this.mGameRTTSmoothStartTime = 0;
        } else if (this.mLastGameNetworkLevel != 460 || this.mGameRTTPoorStartTime == 0) {
            int i = this.mLastGameNetworkLevel;
            if (i == 0) {
                this.mNetworkGeneralDuration = this.mGameRTTGeneralStartTime - this.mlastGameWarStartTime;
                this.mTotalGameGeneralRTT = this.mNetworkGeneralDuration * rtt;
            } else if (i == 200) {
                this.mTotalGameGeneralRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastGeneralStartTime);
            } else {
                logD(false, "handleGeneralRtt mLastGameNetworkLevel:%{public}d", Integer.valueOf(i));
            }
        } else {
            this.mNetworkPoorDuration += System.currentTimeMillis() - this.mGameRTTPoorStartTime;
            this.mTotalGamePoorRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastPoorStartTime);
            this.mGameRTTPoorStartTime = 0;
        }
        this.mLastGameNetworkLevel = HwQoEService.KOG_LATENCY_TIME_THRESHOLD;
        this.mLastGeneralStartTime = System.currentTimeMillis();
    }

    public void updateTencentTmgpGameRttChanged(long rtt) {
        if (this.mTencentTmgpGameType != 1) {
            logD(false, "Game-rtt, TencentTmgpGameType is not KOG_INWAR", new Object[0]);
            return;
        }
        logD(false, "Game-rtt:%{public}s ms, mLastGameNetworkLevel:%{public}d", String.valueOf(rtt), Integer.valueOf(this.mLastGameNetworkLevel));
        if (rtt < 100) {
            handleSmoothRtt(rtt);
        } else if (rtt < 200) {
            handleGeneralRtt(rtt);
        } else {
            if (this.mLastGameNetworkLevel != 460) {
                this.mGameRTTPoorStartTime = System.currentTimeMillis();
            }
            if (this.mLastGameNetworkLevel == 100 && this.mGameRTTSmoothStartTime != 0) {
                this.mNetworkSmoothDuration += System.currentTimeMillis() - this.mGameRTTSmoothStartTime;
                this.mTotalGameSmoothRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastSmoothStartTime);
                this.mGameRTTSmoothStartTime = 0;
            } else if (this.mLastGameNetworkLevel != 200 || this.mGameRTTGeneralStartTime == 0) {
                int i = this.mLastGameNetworkLevel;
                if (i == 0) {
                    this.mNetworkPoorDuration = this.mGameRTTPoorStartTime - this.mlastGameWarStartTime;
                    this.mTotalGamePoorRTT = this.mNetworkPoorDuration * rtt;
                } else if (i == 460) {
                    this.mTotalGamePoorRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastPoorStartTime);
                } else {
                    logD(false, "updateTencentTmgpGameRttChanged mLastGameNetworkLevel:%{public}d", Integer.valueOf(i));
                }
            } else {
                this.mNetworkGeneralDuration += System.currentTimeMillis() - this.mGameRTTGeneralStartTime;
                this.mTotalGameGeneralRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastGeneralStartTime);
                this.mGameRTTGeneralStartTime = 0;
            }
            this.mLastGameNetworkLevel = 460;
            this.mLastPoorStartTime = System.currentTimeMillis();
        }
        this.mCurrentGameRTT = rtt;
    }

    private boolean isValidRegTypeEnum(int regTypeEnum) {
        if (regTypeEnum == 0 || regTypeEnum == 1 || regTypeEnum == 2) {
            return true;
        }
        return false;
    }

    private void updateMobileTmgpGameSmothRttChanged(int regTypeEnum, long rtt) {
        if (isValidRegTypeEnum(regTypeEnum)) {
            if (this.mLastGameNetworkLevel != 100) {
                this.mMobileGameRttSmoothStartTime[regTypeEnum] = System.currentTimeMillis();
            }
            if (this.mLastGameNetworkLevel == 200 && this.mMobileGameRttGeneralStartTime[regTypeEnum] != 0) {
                long[] jArr = this.mMobileNetworkGeneralDuration;
                jArr[regTypeEnum] = jArr[regTypeEnum] + (System.currentTimeMillis() - this.mMobileGameRttGeneralStartTime[regTypeEnum]);
                long[] jArr2 = this.mMobileTotalGameGeneralRtt;
                jArr2[regTypeEnum] = jArr2[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mMobileLastGeneralStartTime[regTypeEnum]));
                this.mMobileGameRttGeneralStartTime[regTypeEnum] = 0;
            } else if (this.mLastGameNetworkLevel != 460 || this.mMobileGameRttPoorStartTime[regTypeEnum] == 0) {
                int i = this.mLastGameNetworkLevel;
                if (i == 0) {
                    long[] jArr3 = this.mMobileNetworkSmoothDuration;
                    jArr3[regTypeEnum] = this.mMobileGameRttSmoothStartTime[regTypeEnum] - this.mlastGameWarStartTime;
                    this.mMobileTotalGameSmoothRtt[regTypeEnum] = jArr3[regTypeEnum] * rtt;
                } else if (i == 100) {
                    long[] jArr4 = this.mMobileTotalGameSmoothRtt;
                    jArr4[regTypeEnum] = jArr4[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mMobileLastSmoothStartTime[regTypeEnum]));
                } else {
                    logD(false, "updateMobileTmgpGameSmothRttChanged mLastGameNetworkLevel:%{public}d", Integer.valueOf(i));
                }
            } else {
                long[] jArr5 = this.mMobileNetworkPoorDuration;
                jArr5[regTypeEnum] = jArr5[regTypeEnum] + (System.currentTimeMillis() - this.mMobileGameRttPoorStartTime[regTypeEnum]);
                long[] jArr6 = this.mMobileTotalGamePoorRtt;
                jArr6[regTypeEnum] = jArr6[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mMobileLastPoorStartTime[regTypeEnum]));
                this.mMobileGameRttPoorStartTime[regTypeEnum] = 0;
            }
            this.mLastGameNetworkLevel = 100;
            this.mMobileLastSmoothStartTime[regTypeEnum] = System.currentTimeMillis();
        }
    }

    private void updateMobileTmgpGameGeneralRttChanged(int regTypeEnum, long rtt) {
        if (isValidRegTypeEnum(regTypeEnum)) {
            if (this.mLastGameNetworkLevel != 200) {
                this.mMobileGameRttGeneralStartTime[regTypeEnum] = System.currentTimeMillis();
            }
            if (this.mLastGameNetworkLevel == 100 && this.mMobileGameRttSmoothStartTime[regTypeEnum] != 0) {
                long[] jArr = this.mMobileNetworkSmoothDuration;
                jArr[regTypeEnum] = jArr[regTypeEnum] + (System.currentTimeMillis() - this.mMobileGameRttSmoothStartTime[regTypeEnum]);
                long[] jArr2 = this.mMobileTotalGameSmoothRtt;
                jArr2[regTypeEnum] = jArr2[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mMobileLastSmoothStartTime[regTypeEnum]));
                this.mMobileGameRttSmoothStartTime[regTypeEnum] = 0;
            } else if (this.mLastGameNetworkLevel != 460 || this.mMobileGameRttPoorStartTime[regTypeEnum] == 0) {
                int i = this.mLastGameNetworkLevel;
                if (i == 0) {
                    long[] jArr3 = this.mMobileNetworkGeneralDuration;
                    jArr3[regTypeEnum] = this.mMobileGameRttGeneralStartTime[regTypeEnum] - this.mlastGameWarStartTime;
                    this.mMobileTotalGameGeneralRtt[regTypeEnum] = jArr3[regTypeEnum] * rtt;
                } else if (i == 200) {
                    long[] jArr4 = this.mMobileTotalGameGeneralRtt;
                    jArr4[regTypeEnum] = jArr4[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mMobileLastGeneralStartTime[regTypeEnum]));
                } else {
                    logD(false, "updateMobileTmgpGameGeneralRttChanged mLastGameNetworkLevel:%{public}d", Integer.valueOf(i));
                }
            } else {
                long[] jArr5 = this.mMobileNetworkPoorDuration;
                jArr5[regTypeEnum] = jArr5[regTypeEnum] + (System.currentTimeMillis() - this.mMobileGameRttPoorStartTime[regTypeEnum]);
                long[] jArr6 = this.mMobileTotalGamePoorRtt;
                jArr6[regTypeEnum] = jArr6[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mMobileLastPoorStartTime[regTypeEnum]));
                this.mMobileGameRttPoorStartTime[regTypeEnum] = 0;
            }
            this.mLastGameNetworkLevel = HwQoEService.KOG_LATENCY_TIME_THRESHOLD;
            this.mMobileLastGeneralStartTime[regTypeEnum] = System.currentTimeMillis();
        }
    }

    private void updateMobileTmgpGamePoorRttChanged(int regTypeEnum, long rtt) {
        if (isValidRegTypeEnum(regTypeEnum)) {
            if (this.mLastGameNetworkLevel != 460) {
                this.mMobileGameRttPoorStartTime[regTypeEnum] = System.currentTimeMillis();
            }
            if (this.mLastGameNetworkLevel == 100 && this.mMobileGameRttSmoothStartTime[regTypeEnum] != 0) {
                long[] jArr = this.mMobileNetworkSmoothDuration;
                jArr[regTypeEnum] = jArr[regTypeEnum] + (System.currentTimeMillis() - this.mMobileGameRttSmoothStartTime[regTypeEnum]);
                long[] jArr2 = this.mMobileTotalGameSmoothRtt;
                jArr2[regTypeEnum] = jArr2[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mMobileLastSmoothStartTime[regTypeEnum]));
                this.mMobileGameRttSmoothStartTime[regTypeEnum] = 0;
            } else if (this.mLastGameNetworkLevel != 200 || this.mMobileGameRttGeneralStartTime[regTypeEnum] == 0) {
                int i = this.mLastGameNetworkLevel;
                if (i == 0) {
                    long[] jArr3 = this.mMobileNetworkPoorDuration;
                    jArr3[regTypeEnum] = this.mMobileGameRttPoorStartTime[regTypeEnum] - this.mlastGameWarStartTime;
                    this.mMobileTotalGamePoorRtt[regTypeEnum] = jArr3[regTypeEnum] * rtt;
                } else if (i == 460) {
                    long[] jArr4 = this.mMobileTotalGamePoorRtt;
                    jArr4[regTypeEnum] = jArr4[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mMobileLastPoorStartTime[regTypeEnum]));
                } else {
                    logD(false, "updateMobileTmgpGamePoorRttChanged mLastGameNetworkLevel:%{public}d", Integer.valueOf(i));
                }
            } else {
                long[] jArr5 = this.mMobileNetworkGeneralDuration;
                jArr5[regTypeEnum] = jArr5[regTypeEnum] + (System.currentTimeMillis() - this.mMobileGameRttGeneralStartTime[regTypeEnum]);
                long[] jArr6 = this.mMobileTotalGameGeneralRtt;
                jArr6[regTypeEnum] = jArr6[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mMobileLastGeneralStartTime[regTypeEnum]));
                this.mMobileGameRttGeneralStartTime[regTypeEnum] = 0;
            }
            this.mLastGameNetworkLevel = 460;
            this.mMobileLastPoorStartTime[regTypeEnum] = System.currentTimeMillis();
        }
    }

    public void updateMobileTencentTmgpGameRttChanged(long rtt) {
        int i = this.mCurrMobileRegTypeEnum;
        if (this.mTencentTmgpGameType != 1) {
            logD(false, "Game-rtt, TencentTmgpGameType is not KOG_INWAR", new Object[0]);
            return;
        }
        logD(false, "Game-rtt:%{public}s ms, mLastGameNetworkLevel:%{public}d", String.valueOf(rtt), Integer.valueOf(this.mLastGameNetworkLevel));
        if (rtt < 100) {
            updateMobileTmgpGameSmothRttChanged(i, rtt);
        } else if (rtt < 200) {
            updateMobileTmgpGameGeneralRttChanged(i, rtt);
        } else {
            updateMobileTmgpGamePoorRttChanged(i, rtt);
        }
        this.mCurrentGameRTT = rtt;
    }

    private void totalMobileDurationAndRtt(int regTypeEnum) {
        if (isValidRegTypeEnum(regTypeEnum)) {
            if (this.mLastGameNetworkLevel == 460 && this.mMobileGameRttPoorStartTime[regTypeEnum] != 0) {
                long[] jArr = this.mMobileNetworkPoorDuration;
                jArr[regTypeEnum] = jArr[regTypeEnum] + (System.currentTimeMillis() - this.mMobileGameRttPoorStartTime[regTypeEnum]);
                long[] jArr2 = this.mMobileTotalGamePoorRtt;
                jArr2[regTypeEnum] = jArr2[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mMobileLastPoorStartTime[regTypeEnum]));
            } else if (this.mLastGameNetworkLevel == 100 && this.mMobileGameRttSmoothStartTime[regTypeEnum] != 0) {
                long[] jArr3 = this.mMobileNetworkSmoothDuration;
                jArr3[regTypeEnum] = jArr3[regTypeEnum] + (System.currentTimeMillis() - this.mMobileGameRttSmoothStartTime[regTypeEnum]);
                long[] jArr4 = this.mMobileTotalGameSmoothRtt;
                jArr4[regTypeEnum] = jArr4[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mMobileLastSmoothStartTime[regTypeEnum]));
            } else if (this.mLastGameNetworkLevel != 200 || this.mMobileGameRttGeneralStartTime[regTypeEnum] == 0) {
                logD(false, "totalMobileDurationAndRtt mLastGameNetworkLevel:%{public}d", Integer.valueOf(this.mLastGameNetworkLevel));
            } else {
                long[] jArr5 = this.mMobileNetworkGeneralDuration;
                jArr5[regTypeEnum] = jArr5[regTypeEnum] + (System.currentTimeMillis() - this.mMobileGameRttGeneralStartTime[regTypeEnum]);
                long[] jArr6 = this.mMobileTotalGameGeneralRtt;
                jArr6[regTypeEnum] = jArr6[regTypeEnum] + (this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastGeneralStartTime));
            }
        }
    }

    private void updateMobileTcpRttDuration(int regTypeEnum) {
        if (isValidRegTypeEnum(regTypeEnum)) {
            if (this.mLastTcpRttLevel == 200 && this.mMobileTcpRttPoorStartTime[regTypeEnum] != 0) {
                long[] jArr = this.mMobileTcpRttPoorDuration;
                jArr[regTypeEnum] = jArr[regTypeEnum] + (System.currentTimeMillis() - this.mMobileTcpRttPoorStartTime[regTypeEnum]);
            } else if (this.mLastTcpRttLevel == 50 && this.mMobileTcpRttSmoothStartTime[regTypeEnum] != 0) {
                long[] jArr2 = this.mMobileTcpRttSmoothDuration;
                jArr2[regTypeEnum] = jArr2[regTypeEnum] + (System.currentTimeMillis() - this.mMobileTcpRttSmoothStartTime[regTypeEnum]);
            } else if (this.mLastTcpRttLevel == 100 && this.mMobileTcpRttGeneralStartTime[regTypeEnum] != 0) {
                long[] jArr3 = this.mMobileTcpRttGeneralDuration;
                jArr3[regTypeEnum] = jArr3[regTypeEnum] + (System.currentTimeMillis() - this.mMobileTcpRttGeneralStartTime[regTypeEnum]);
            } else if (this.mLastTcpRttLevel != 460 || this.mMobileTcpRttBadStartTime[regTypeEnum] == 0) {
                logD(false, "updateMobileTcpRttDuration mLastTcpRttLevel:%{public}d", Integer.valueOf(this.mLastTcpRttLevel));
            } else {
                long[] jArr4 = this.mMobileTcpRttBadDuration;
                jArr4[regTypeEnum] = jArr4[regTypeEnum] + (System.currentTimeMillis() - this.mMobileTcpRttBadStartTime[regTypeEnum]);
            }
            long[] jArr5 = this.mMobileTcpRttPoorDuration;
            jArr5[regTypeEnum] = jArr5[regTypeEnum] / 1000;
            long[] jArr6 = this.mMobileTcpRttSmoothDuration;
            jArr6[regTypeEnum] = jArr6[regTypeEnum] / 1000;
            long[] jArr7 = this.mMobileTcpRttGeneralDuration;
            jArr7[regTypeEnum] = jArr7[regTypeEnum] / 1000;
            long[] jArr8 = this.mMobileTcpRttBadDuration;
            jArr8[regTypeEnum] = jArr8[regTypeEnum] / 1000;
        }
    }

    public void updateMobileTencentTmgpGameStateChanged(int gameState) {
        logD(false, "updateMobileTencentTmgpGameStateChanged new state: %{public}d", Integer.valueOf(gameState));
        this.mTencentTmgpGameType = gameState;
        if (this.mTencentTmgpGameType != 0 || !this.mAllowStatistics) {
            int i = this.mTencentTmgpGameType;
            if (i == 1) {
                initialGameCHR();
                this.mAllowStatistics = true;
                this.mlastGameWarStartTime = System.currentTimeMillis();
                return;
            }
            logD(false, "mTencentTmgpGameType is invalid, TencentTmgpGameType: %{public}d", Integer.valueOf(i));
            return;
        }
        this.mAllowStatistics = false;
        for (int i2 = 0; i2 < 3; i2++) {
            totalMobileDurationAndRtt(i2);
            long[] jArr = this.mMobileTotalGameRTT;
            long j = this.mMobileTotalGamePoorRtt[i2];
            long[] jArr2 = this.mMobileTotalGameSmoothRtt;
            jArr[i2] = j + jArr2[i2] + this.mMobileTotalGameGeneralRtt[i2];
            long[] jArr3 = this.mMobileNetworkPoorDuration;
            jArr3[i2] = jArr3[i2] / 1000;
            long[] jArr4 = this.mMobileNetworkSmoothDuration;
            jArr4[i2] = jArr4[i2] / 1000;
            long[] jArr5 = this.mMobileNetworkGeneralDuration;
            jArr5[i2] = jArr5[i2] / 1000;
            if (jArr4[i2] != 0) {
                jArr2[i2] = jArr2[i2] / jArr4[i2];
            }
            long[] jArr6 = this.mMobileNetworkGeneralDuration;
            if (jArr6[i2] != 0) {
                long[] jArr7 = this.mMobileTotalGameGeneralRtt;
                jArr7[i2] = jArr7[i2] / jArr6[i2];
            }
            long[] jArr8 = this.mMobileNetworkPoorDuration;
            if (jArr8[i2] != 0) {
                long[] jArr9 = this.mMobileTotalGamePoorRtt;
                jArr9[i2] = jArr9[i2] / jArr8[i2];
            }
            updateMobileTcpRttDuration(i2);
        }
        sendIntentDsKogTcpRttStatic(this.mContext);
    }

    private void resetRttStartTimer(int indexRsv) {
        if (isValidRegTypeEnum(indexRsv)) {
            this.mMobileGameRttSmoothStartTime[indexRsv] = 0;
            this.mMobileGameRttGeneralStartTime[indexRsv] = 0;
            this.mMobileGameRttPoorStartTime[indexRsv] = 0;
        }
    }

    private void startMobileGameRttTimer(int timerIndex) {
        if (isValidRegTypeEnum(timerIndex)) {
            int i = this.mLastGameNetworkLevel;
            if (i == 100) {
                this.mMobileGameRttSmoothStartTime[timerIndex] = System.currentTimeMillis();
            } else if (i == 200) {
                this.mMobileGameRttGeneralStartTime[timerIndex] = System.currentTimeMillis();
            } else if (i == 460) {
                this.mMobileGameRttPoorStartTime[timerIndex] = System.currentTimeMillis();
            } else {
                logD(false, "startMobileGameRttTimer mLastGameNetworkLevel:%{public}d", Integer.valueOf(i));
            }
        }
    }

    public void updateRegTypeAndTencentTmgpGameRttTimer(int dataRegTech, int nsaState) {
        int i = this.mCurrMobileRegTypeEnum;
        this.mOldMobileRegTypeEnum = i;
        if (dataRegTech != 0) {
            if (dataRegTech != 20) {
                this.mCurrMobileRegTypeEnum = 0;
            } else if (nsaState == 0 || nsaState == 6) {
                this.mCurrMobileRegTypeEnum = 2;
            } else if (nsaState == 5) {
                this.mCurrMobileRegTypeEnum = 1;
            } else {
                this.mCurrMobileRegTypeEnum = 0;
            }
            int i2 = this.mCurrMobileRegTypeEnum;
            int i3 = this.mOldMobileRegTypeEnum;
            if (i2 == i3) {
                logD(false, "Game-rtt, updateTencentTmgpGameRttTimer MobileRegTypeEnum not changed", new Object[0]);
                return;
            }
            totalMobileDurationAndRtt(i3);
            resetRttStartTimer(this.mOldMobileRegTypeEnum);
            startMobileGameRttTimer(this.mCurrMobileRegTypeEnum);
            return;
        }
        totalMobileDurationAndRtt(i);
        resetRttStartTimer(this.mCurrMobileRegTypeEnum);
    }

    public void handleWifiStateChanged(int wifiState) {
        if (wifiState == 109) {
            totalMobileDurationAndRtt(this.mCurrMobileRegTypeEnum);
            resetRttStartTimer(this.mCurrMobileRegTypeEnum);
        } else if (wifiState == 110) {
            startMobileGameRttTimer(this.mCurrMobileRegTypeEnum);
        }
    }

    private void handleGameSmoothArpRtt() {
        if (this.mLastArpRttLevel != 30) {
            this.mArpRttSmoothStartTime = System.currentTimeMillis();
        }
        if (this.mLastArpRttLevel == 100 && this.mArpRttGeneralStartTime != 0) {
            this.mArpRttGeneralDuration += System.currentTimeMillis() - this.mArpRttGeneralStartTime;
            this.mArpRttGeneralStartTime = 0;
        } else if (this.mLastArpRttLevel != 200 || this.mArpRttPoorStartTime == 0) {
            int i = this.mLastArpRttLevel;
            if (i == 0) {
                this.mArpRttSmoothDuration = this.mArpRttSmoothStartTime - this.mlastGameWarStartTime;
            } else {
                logD(false, "rtt < GAME_SMOOTH_MAX_ARP_RTT,mLastArpRttLevel: %{public}d", Integer.valueOf(i));
            }
        } else {
            this.mArpRttPoorDuration += System.currentTimeMillis() - this.mArpRttPoorStartTime;
            this.mArpRttPoorStartTime = 0;
        }
        this.mLastArpRttLevel = 30;
    }

    private void handleGameGeneralArpRtt() {
        if (this.mLastArpRttLevel != 100) {
            this.mArpRttGeneralStartTime = System.currentTimeMillis();
        }
        if (this.mLastArpRttLevel == 30 && this.mArpRttSmoothStartTime != 0) {
            this.mArpRttSmoothDuration += System.currentTimeMillis() - this.mArpRttSmoothStartTime;
            this.mArpRttSmoothStartTime = 0;
        } else if (this.mLastArpRttLevel != 200 || this.mArpRttPoorStartTime == 0) {
            int i = this.mLastArpRttLevel;
            if (i == 0) {
                this.mArpRttGeneralDuration = this.mArpRttGeneralStartTime - this.mlastGameWarStartTime;
            } else {
                logD(false, "rtt < GAME_GENERAL_MAX_ARP_RTT,mLastArpRttLevel: %{public}d", Integer.valueOf(i));
            }
        } else {
            this.mArpRttPoorDuration += System.currentTimeMillis() - this.mArpRttPoorStartTime;
            this.mArpRttPoorStartTime = 0;
        }
        this.mLastArpRttLevel = 100;
    }

    public void updateArpRttChanged(int rtt) {
        logD(false, "Arp-rtt:%{public}d ms, mLastArpRttLevel:%{public}d", Integer.valueOf(rtt), Integer.valueOf(this.mLastArpRttLevel));
        if (this.mTencentTmgpGameType != 1) {
            logD(false, "Arp-rtt:, TencentTmgpGameType is not KOG_INWAR", new Object[0]);
        } else if (rtt < 30) {
            handleGameSmoothArpRtt();
        } else if (rtt < 100) {
            handleGameGeneralArpRtt();
        } else {
            if (this.mLastArpRttLevel != 200) {
                this.mArpRttPoorStartTime = System.currentTimeMillis();
            }
            if (this.mLastArpRttLevel == 30 && this.mArpRttSmoothStartTime != 0) {
                this.mArpRttSmoothDuration += System.currentTimeMillis() - this.mArpRttSmoothStartTime;
                this.mArpRttSmoothStartTime = 0;
            } else if (this.mLastArpRttLevel != 100 || this.mArpRttGeneralStartTime == 0) {
                int i = this.mLastArpRttLevel;
                if (i == 0) {
                    this.mArpRttPoorDuration = this.mArpRttPoorStartTime - this.mlastGameWarStartTime;
                } else {
                    logD(false, "rtt >= GAME_GENERAL_MAX_ARP_RTT,mLastArpRttLevel: %{public}d", Integer.valueOf(i));
                }
            } else {
                this.mArpRttGeneralDuration += System.currentTimeMillis() - this.mArpRttGeneralStartTime;
                this.mArpRttGeneralStartTime = 0;
            }
            this.mLastArpRttLevel = HwQoEService.KOG_LATENCY_TIME_THRESHOLD;
        }
    }

    private void handleGameRtt() {
        if (this.mLastGameNetworkLevel == 460 && this.mGameRTTPoorStartTime != 0) {
            this.mNetworkPoorDuration += System.currentTimeMillis() - this.mGameRTTPoorStartTime;
            this.mTotalGamePoorRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastPoorStartTime);
        } else if (this.mLastGameNetworkLevel == 100 && this.mGameRTTSmoothStartTime != 0) {
            this.mNetworkSmoothDuration += System.currentTimeMillis() - this.mGameRTTSmoothStartTime;
            this.mTotalGameSmoothRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastSmoothStartTime);
        } else if (this.mLastGameNetworkLevel != 200 || this.mGameRTTGeneralStartTime == 0) {
            logD(false, "invalid mLastGameNetworkLevel,mLastGameNetworkLevel: %{public}d", Integer.valueOf(this.mLastGameNetworkLevel));
        } else {
            this.mNetworkGeneralDuration += System.currentTimeMillis() - this.mGameRTTGeneralStartTime;
            this.mTotalGameGeneralRTT += this.mCurrentGameRTT * (System.currentTimeMillis() - this.mLastGeneralStartTime);
        }
        this.mNetworkPoorDuration /= 1000;
        this.mNetworkSmoothDuration /= 1000;
        this.mNetworkGeneralDuration /= 1000;
        long j = this.mNetworkSmoothDuration;
        if (j != 0) {
            this.mTotalGameSmoothRTT /= j;
        }
        long j2 = this.mNetworkGeneralDuration;
        if (j2 != 0) {
            this.mTotalGameGeneralRTT /= j2;
        }
        long j3 = this.mNetworkPoorDuration;
        if (j3 != 0) {
            this.mTotalGamePoorRTT /= j3;
        }
    }

    private void handleArpRtt(int network) {
        if (network == 1) {
            if (this.mLastArpRttLevel == 200 && this.mArpRttPoorStartTime != 0) {
                this.mArpRttPoorDuration += System.currentTimeMillis() - this.mArpRttPoorStartTime;
            } else if (this.mLastArpRttLevel == 30 && this.mArpRttSmoothStartTime != 0) {
                this.mArpRttSmoothDuration += System.currentTimeMillis() - this.mArpRttSmoothStartTime;
            } else if (this.mLastArpRttLevel != 100 || this.mArpRttGeneralStartTime == 0) {
                logD(false, "mLastArpRttLevel: %{public}d, mArpRttGeneralStartTime: %{public}d", Integer.valueOf(this.mLastArpRttLevel), Long.valueOf(this.mArpRttGeneralStartTime));
            } else {
                this.mArpRttGeneralDuration += System.currentTimeMillis() - this.mArpRttGeneralStartTime;
            }
            this.mArpRttPoorDuration /= 1000;
            this.mArpRttSmoothDuration /= 1000;
            this.mArpRttGeneralDuration /= 1000;
            return;
        }
        logD(false, "handleArpRtt(), network: %{public}d", Integer.valueOf(network));
    }

    public void updateTencentTmgpGameStateChanged(int state, int network) {
        logD(false, "updateTencentTmgpGameStateChanged new state: %{public}d, network:%{public}d", Integer.valueOf(state), Integer.valueOf(network));
        this.mTencentTmgpGameType = state;
        if (this.mTencentTmgpGameType != 0 || !this.mAllowStatistics) {
            int i = this.mTencentTmgpGameType;
            if (i == 1) {
                initialGameCHR();
                this.mAllowStatistics = true;
                this.mlastGameWarStartTime = System.currentTimeMillis();
                return;
            }
            logD(false, "mTencentTmgpGameType: %{public}d, mAllowStatistics: %{public}d", Integer.valueOf(i), Boolean.valueOf(this.mAllowStatistics));
            return;
        }
        this.mAllowStatistics = false;
        handleGameRtt();
        handleArpRtt(network);
        if (this.mLastTcpRttLevel == 200 && this.mTcpRttPoorStartTime != 0) {
            this.mTcpRttPoorDuration += System.currentTimeMillis() - this.mTcpRttPoorStartTime;
        } else if (this.mLastTcpRttLevel == 50 && this.mTcpRttSmoothStartTime != 0) {
            this.mTcpRttSmoothDuration += System.currentTimeMillis() - this.mTcpRttSmoothStartTime;
        } else if (this.mLastTcpRttLevel == 100 && this.mTcpRttGeneralStartTime != 0) {
            this.mTcpRttGeneralDuration += System.currentTimeMillis() - this.mTcpRttGeneralStartTime;
        } else if (this.mLastTcpRttLevel != 460 || this.mTcpRttBadStartTime == 0) {
            logD(false, "mLastTcpRttLevel: %{public}d, mTcpRttGeneralStartTime: %{public}d", Integer.valueOf(this.mLastTcpRttLevel), Long.valueOf(this.mTcpRttGeneralStartTime));
        } else {
            this.mTcpRttBadDuration += System.currentTimeMillis() - this.mTcpRttBadStartTime;
        }
        this.mTcpRttPoorDuration /= 1000;
        this.mTcpRttSmoothDuration /= 1000;
        this.mTcpRttGeneralDuration /= 1000;
        this.mTcpRttBadDuration /= 1000;
        reportCHR(network);
    }

    public void updateTencentTmgpGameRttCounter(long rtt) {
        if (isValidRegTypeEnum(this.mCurrMobileRegTypeEnum) && rtt > 0) {
            if (rtt <= 100) {
                int[] iArr = this.mDsKogRtt100Count;
                int i = this.mCurrMobileRegTypeEnum;
                iArr[i] = iArr[i] + 1;
            } else if (rtt <= 200) {
                int[] iArr2 = this.mDsKogRtt200Count;
                int i2 = this.mCurrMobileRegTypeEnum;
                iArr2[i2] = iArr2[i2] + 1;
            } else if (rtt <= 300) {
                int[] iArr3 = this.mDsKogRtt300Count;
                int i3 = this.mCurrMobileRegTypeEnum;
                iArr3[i3] = iArr3[i3] + 1;
            } else if (rtt <= 400) {
                int[] iArr4 = this.mDsKogRtt400Count;
                int i4 = this.mCurrMobileRegTypeEnum;
                iArr4[i4] = iArr4[i4] + 1;
            } else {
                int[] iArr5 = this.mDsKogRttOver400Count;
                int i5 = this.mCurrMobileRegTypeEnum;
                iArr5[i5] = iArr5[i5] + 1;
            }
            int[] iArr6 = this.mDsKogTotalRttSum;
            int i6 = this.mCurrMobileRegTypeEnum;
            iArr6[i6] = (int) (((long) iArr6[i6]) + rtt);
            int[] iArr7 = this.mDsKogTotalRttCount;
            iArr7[i6] = iArr7[i6] + 1;
        }
    }

    private void sendIntentDsKogTcpRttStatic(Context context) {
        String preStr = "";
        if (context == null) {
            logD(false, "sendIntentDsKogTcpRttStatic context is null", new Object[0]);
            return;
        }
        Intent chrIntent = new Intent(INTENT_DS_KOG_RTT_REPORT);
        Bundle extras = new Bundle();
        extras.putInt("ReportType", 2);
        for (int i = 0; i < 3; i++) {
            if (i == 1) {
                preStr = "endc";
            } else if (i == 2) {
                preStr = "nr";
            } else {
                logD(false, "sendIntentDsKogTcpRttStatic", new Object[0]);
            }
            extras.putInt(preStr + "TcpRttSmoothDuration", (int) this.mMobileTcpRttSmoothDuration[i]);
            extras.putInt(preStr + "TcpRttGeneralDuration", (int) this.mMobileTcpRttGeneralDuration[i]);
            extras.putInt(preStr + "TcpRttPoorDuration", (int) this.mMobileTcpRttPoorDuration[i]);
            extras.putInt(preStr + "TcpRttBadDuration", (int) this.mMobileTcpRttBadDuration[i]);
            extras.putInt(preStr + "NetworkSmoothDuration", (int) this.mMobileNetworkSmoothDuration[i]);
            extras.putInt(preStr + "NetworkGeneralDuration", (int) this.mMobileNetworkGeneralDuration[i]);
            extras.putInt(preStr + "NetworkPoorDuration", (int) this.mMobileNetworkPoorDuration[i]);
            extras.putInt(preStr + "DsKOGRtt100Count", this.mDsKogRtt100Count[i]);
            extras.putInt(preStr + "DsKOGRtt200Count", this.mDsKogRtt200Count[i]);
            extras.putInt(preStr + "DsKOGRtt300Count", this.mDsKogRtt300Count[i]);
            extras.putInt(preStr + "DsKOGRtt400Count", this.mDsKogRtt400Count[i]);
            extras.putInt(preStr + "DsKOGRttOver400Count", this.mDsKogRttOver400Count[i]);
            extras.putInt(preStr + "DsKOGTotalRttSum", this.mDsKogTotalRttSum[i]);
            extras.putInt(preStr + "DsKOGTotalRttCount", this.mDsKogTotalRttCount[i]);
        }
        chrIntent.putExtras(extras);
        context.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
    }

    private void reportCHR(int network) {
        HwWifiGameNetChrInfo hwWifiGameNetChrInfo;
        if (network == 1 && (hwWifiGameNetChrInfo = this.mHwWifiGameNetChrInfo) != null) {
            hwWifiGameNetChrInfo.setArpRttGeneralDuration(this.mArpRttGeneralDuration);
            this.mHwWifiGameNetChrInfo.setArpRttPoorDuration(this.mArpRttPoorDuration);
            this.mHwWifiGameNetChrInfo.setArpRttSmoothDuration(this.mArpRttSmoothDuration);
            this.mHwWifiGameNetChrInfo.setNetworkGeneralDuration(this.mNetworkGeneralDuration);
            this.mHwWifiGameNetChrInfo.setNetworkPoorDuration(this.mNetworkPoorDuration);
            this.mHwWifiGameNetChrInfo.setNetworkSmoothDuration(this.mNetworkSmoothDuration);
            this.mHwWifiGameNetChrInfo.setTcpRttBadDuration(this.mTcpRttBadDuration);
            this.mHwWifiGameNetChrInfo.setTcpRttGeneralDuration(this.mTcpRttGeneralDuration);
            this.mHwWifiGameNetChrInfo.setTcpRttPoorDuration(this.mTcpRttPoorDuration);
            this.mHwWifiGameNetChrInfo.setTcpRttSmoothDuration(this.mTcpRttSmoothDuration);
            this.mHwWifiGameNetChrInfo.setWifiDisCounter(this.mWifiDisCounter);
            this.mHwWifiGameNetChrInfo.setWifiRoamingCounter(this.mWifiRoamingCounter);
            this.mHwWifiGameNetChrInfo.setWifiScanCounter(this.mWifiScanCounter);
            this.mHwWifiGameNetChrInfo.setBTScan24GCounter(this.mBTScan24GCounter);
            this.mHwWifiGameNetChrInfo.setNetworkSmoothAvgRtt(this.mTotalGameSmoothRTT);
            this.mHwWifiGameNetChrInfo.setNetworkGeneralAvgRtt(this.mTotalGameGeneralRTT);
            this.mHwWifiGameNetChrInfo.setNetworkPoorAvgRtt(this.mTotalGamePoorRTT);
            chrInfoDump();
            if (this.mApType == 2) {
                this.mHwWifiGameNetChrInfo.setAP24gBTCoexist(true);
            }
            int i = this.mApType;
            if (i == 3) {
                this.mHwWifiGameNetChrInfo.setAP5gOnly(true);
            } else if (i == 1 || i == 2) {
                this.mHwWifiGameNetChrInfo.setAP5gOnly(false);
            } else {
                logD(false, "mApType: %{public}d", Integer.valueOf(i));
            }
        } else if (network == 0) {
            sendIntentDsKogTcpRttStatic(this.mContext);
        } else {
            logD(false, "network: %{public}d, mHwWifiGameNetChrInfo: %{public}d", Integer.valueOf(network), this.mHwWifiGameNetChrInfo);
        }
    }

    public void updateWifiFrequency(boolean is5G) {
        if (is5G) {
            this.mApType = 3;
        } else if (getBluetoothConneced()) {
            this.mApType = 2;
        } else {
            this.mApType = 1;
        }
        logD(false, "updateWifiFrequency: mApType:%{public}d", Integer.valueOf(this.mApType));
    }

    public void updateWifiRoaming() {
        if (this.mTencentTmgpGameType == 1) {
            this.mWifiRoamingCounter = (short) (this.mWifiRoamingCounter + 1);
        }
    }

    public void updateWiFiScanCounter() {
        if (this.mTencentTmgpGameType == 1) {
            this.mWifiScanCounter = (short) (this.mWifiScanCounter + 1);
        }
    }

    public void updateBTScanCounter() {
        if (this.mTencentTmgpGameType == 1 && this.mApType != 3) {
            this.mBTScan24GCounter = (short) (this.mBTScan24GCounter + 1);
        }
    }

    public void updateWiFiDisCounter() {
        if (this.mTencentTmgpGameType == 1) {
            this.mWifiDisCounter = (short) (this.mWifiDisCounter + 1);
        }
    }

    public void updateArpResult(boolean success, int arpRtt) {
        if (!success) {
            this.mWifiArpFailCounter = (short) (this.mWifiArpFailCounter + 1);
        } else if (this.mTencentTmgpGameType == 1) {
            updateArpRttChanged(arpRtt);
        } else {
            logD(false, "updateArpResult() do nothing", new Object[0]);
        }
    }

    private void handleSmoothTcpRtt() {
        if (this.mLastTcpRttLevel != 50) {
            this.mTcpRttSmoothStartTime = System.currentTimeMillis();
        }
        if (this.mLastTcpRttLevel == 100 && this.mTcpRttGeneralStartTime != 0) {
            this.mTcpRttGeneralDuration += System.currentTimeMillis() - this.mTcpRttGeneralStartTime;
            this.mTcpRttGeneralStartTime = 0;
        } else if (this.mLastTcpRttLevel != 200 || this.mTcpRttPoorStartTime == 0) {
            int i = this.mLastTcpRttLevel;
            if (i == 0) {
                this.mTcpRttSmoothDuration = this.mTcpRttSmoothStartTime - this.mlastGameWarStartTime;
            } else if (i != 460 || this.mTcpRttBadStartTime == 0) {
                logD(false, "tcpRtt < GAME_SMOOTH_MAX_TCP_RTT, mLastTcpRttLevel: %{public}d", Integer.valueOf(this.mLastTcpRttLevel));
            } else {
                this.mTcpRttBadDuration += System.currentTimeMillis() - this.mTcpRttBadStartTime;
                this.mTcpRttBadStartTime = 0;
            }
        } else {
            this.mTcpRttPoorDuration += System.currentTimeMillis() - this.mTcpRttPoorStartTime;
            this.mTcpRttPoorStartTime = 0;
        }
        this.mLastTcpRttLevel = 50;
    }

    private void handleGeneralTcpRtt() {
        if (this.mLastTcpRttLevel != 100) {
            this.mTcpRttGeneralStartTime = System.currentTimeMillis();
        }
        if (this.mLastTcpRttLevel == 50 && this.mTcpRttSmoothStartTime != 0) {
            this.mTcpRttSmoothDuration += System.currentTimeMillis() - this.mTcpRttSmoothStartTime;
            this.mTcpRttSmoothStartTime = 0;
        } else if (this.mLastTcpRttLevel != 200 || this.mTcpRttPoorStartTime == 0) {
            int i = this.mLastTcpRttLevel;
            if (i == 0) {
                this.mTcpRttGeneralDuration = this.mTcpRttGeneralStartTime - this.mlastGameWarStartTime;
            } else if (i != 460 || this.mTcpRttBadStartTime == 0) {
                logD(false, "tcpRtt < GAME_GENERAL_MAX_TCP_RTT, mLastTcpRttLevel: %{public}d", Integer.valueOf(this.mLastTcpRttLevel));
            } else {
                this.mTcpRttBadDuration += System.currentTimeMillis() - this.mTcpRttBadStartTime;
                this.mTcpRttBadStartTime = 0;
            }
        } else {
            this.mTcpRttPoorDuration += System.currentTimeMillis() - this.mTcpRttPoorStartTime;
            this.mTcpRttPoorStartTime = 0;
        }
        this.mLastTcpRttLevel = 100;
    }

    public void updateWifiTcpRtt(long tcpRttSegs, long tcpRtt, long duration) {
        if (this.mTencentTmgpGameType != 1) {
            logD(false, "Tcp-rtt, TencentTmgpGameType is not KOG_INWAR", new Object[0]);
            return;
        }
        logD(false, "updateWifiTcpRtt Tcp-rtt: %{public}s ms, duration: %{public}s ms, tcpRttSegs:%{public}s Segs, mLastTcpRttLevel:%{public}d", String.valueOf(tcpRtt), String.valueOf(duration), String.valueOf(tcpRttSegs), Integer.valueOf(this.mLastTcpRttLevel));
        if (tcpRtt < 50) {
            handleSmoothTcpRtt();
        } else if (tcpRtt < 100) {
            handleGeneralTcpRtt();
        } else if (tcpRtt < 200) {
            statisticsTcpPoorRtt();
        } else {
            statisticsTcpBadRtt();
        }
    }

    private void statisticsTcpPoorRtt() {
        if (this.mLastTcpRttLevel != 200) {
            this.mTcpRttPoorStartTime = System.currentTimeMillis();
        }
        if (this.mLastTcpRttLevel == 50 && this.mTcpRttSmoothStartTime != 0) {
            this.mTcpRttSmoothDuration += System.currentTimeMillis() - this.mTcpRttSmoothStartTime;
            this.mTcpRttSmoothStartTime = 0;
        } else if (this.mLastTcpRttLevel != 100 || this.mTcpRttGeneralStartTime == 0) {
            int i = this.mLastTcpRttLevel;
            if (i == 0) {
                this.mTcpRttPoorDuration = this.mTcpRttPoorStartTime - this.mlastGameWarStartTime;
            } else if (i != 460 || this.mTcpRttBadStartTime == 0) {
                logD(false, "statisticsTcpPoorRtt(), mLastTcpRttLevel: %{public}d", Integer.valueOf(this.mLastTcpRttLevel));
            } else {
                this.mTcpRttBadDuration += System.currentTimeMillis() - this.mTcpRttBadStartTime;
                this.mTcpRttBadStartTime = 0;
            }
        } else {
            this.mTcpRttGeneralDuration += System.currentTimeMillis() - this.mTcpRttGeneralStartTime;
            this.mTcpRttGeneralStartTime = 0;
        }
        this.mLastTcpRttLevel = HwQoEService.KOG_LATENCY_TIME_THRESHOLD;
    }

    private void statisticsTcpBadRtt() {
        if (this.mLastTcpRttLevel != 460) {
            this.mTcpRttBadStartTime = System.currentTimeMillis();
        }
        if (this.mLastTcpRttLevel == 50 && this.mTcpRttSmoothStartTime != 0) {
            this.mTcpRttSmoothDuration += System.currentTimeMillis() - this.mTcpRttSmoothStartTime;
            this.mTcpRttSmoothStartTime = 0;
        } else if (this.mLastTcpRttLevel != 200 || this.mTcpRttPoorStartTime == 0) {
            int i = this.mLastTcpRttLevel;
            if (i == 0) {
                this.mTcpRttBadDuration = this.mTcpRttBadStartTime - this.mlastGameWarStartTime;
            } else if (i != 100 || this.mTcpRttGeneralStartTime == 0) {
                logD(false, "mLastTcpRttLevel: %{public}d, mTcpRttGeneralStartTime: %{public}d", Integer.valueOf(this.mLastTcpRttLevel), Long.valueOf(this.mTcpRttGeneralStartTime));
            } else {
                this.mTcpRttGeneralDuration += System.currentTimeMillis() - this.mTcpRttGeneralStartTime;
                this.mTcpRttGeneralStartTime = 0;
            }
        } else {
            this.mTcpRttPoorDuration += System.currentTimeMillis() - this.mTcpRttPoorStartTime;
            this.mTcpRttPoorStartTime = 0;
        }
        this.mLastTcpRttLevel = 460;
    }

    private boolean getBluetoothConneced() {
        if (this.mLocalBluetoothAdapter == null) {
            this.mLocalBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        BluetoothAdapter bluetoothAdapter = this.mLocalBluetoothAdapter;
        if (bluetoothAdapter == null || bluetoothAdapter.getConnectionState() != 2) {
            return false;
        }
        return true;
    }

    private void chrInfoDump() {
        StringBuffer buffer = new StringBuffer("Game CHR Info : ");
        buffer.append("ApType: ");
        buffer.append(this.mApType);
        int i = this.mApType;
        if (i == 1) {
            buffer.append("_AP_TYPE_24G_ONLY");
        } else if (i == 2) {
            buffer.append("_AP_TYPE_24G_BT_COEXIST");
        } else if (i == 3) {
            buffer.append("_AP_TYPE_5G_ONLY");
        } else {
            logD(false, "mApType is invalid, mApType: %{public}d", Integer.valueOf(i));
        }
        buffer.append(", GameRttSmoothDuration: ");
        buffer.append(this.mNetworkSmoothDuration);
        buffer.append(" s, GameRttGeneralDuration: ");
        buffer.append(this.mNetworkGeneralDuration);
        buffer.append(" s, GameRttPoorDuration: ");
        buffer.append(this.mNetworkPoorDuration);
        buffer.append(", GameRttSmoothDuration: ");
        buffer.append(this.mTotalGameSmoothRTT);
        buffer.append(" ms, GameRttGeneralAvgRTT: ");
        buffer.append(this.mTotalGameGeneralRTT);
        buffer.append(" ms, GameRttPoorAvgRTT: ");
        buffer.append(this.mTotalGamePoorRTT);
        buffer.append(" ms, ArpRttSmoothAvgRTT: ");
        buffer.append(this.mArpRttSmoothDuration);
        buffer.append(" s, ArpRttGeneralDuration: ");
        buffer.append(this.mArpRttGeneralDuration);
        buffer.append(" s, ArpRttPoorDuration: ");
        buffer.append(this.mArpRttPoorDuration);
        buffer.append(" s, TcpRttSmoothDuration: ");
        buffer.append(this.mTcpRttSmoothDuration);
        buffer.append(" s, TcpRttGeneralDuration: ");
        buffer.append(this.mTcpRttGeneralDuration);
        buffer.append(" s, TcpRttPoorDuration: ");
        buffer.append(this.mTcpRttPoorDuration);
        buffer.append(" s, TcpRttBadDuration: ");
        buffer.append(this.mTcpRttBadDuration);
        buffer.append(" s, WifiDisCounter: ");
        buffer.append((int) this.mWifiDisCounter);
        buffer.append(" , WifiScanCounter: ");
        buffer.append((int) this.mWifiScanCounter);
        buffer.append(" , WifiRoamingCounter: ");
        buffer.append((int) this.mWifiRoamingCounter);
        logD(false, "%{public}s", buffer.toString());
    }

    private void logD(boolean isFmtStrPrivate, String info, Object... args) {
        HwHiLog.d(TAG, isFmtStrPrivate, info, args);
    }
}
