package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.MemoryFile;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseLongArray;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BatterySipper;
import com.android.internal.util.ArrayUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BatteryStatsHelper {
    static final boolean DEBUG = false;
    private static final String TAG = BatteryStatsHelper.class.getSimpleName();
    private static Intent sBatteryBroadcastXfer;
    private static ArrayMap<File, BatteryStats> sFileXfer = new ArrayMap<>();
    private static BatteryStats sStatsXfer;
    private Intent mBatteryBroadcast;
    @UnsupportedAppUsage
    private IBatteryStats mBatteryInfo;
    long mBatteryRealtimeUs;
    long mBatteryTimeRemainingUs;
    long mBatteryUptimeUs;
    PowerCalculator mBluetoothPowerCalculator;
    private final List<BatterySipper> mBluetoothSippers;
    PowerCalculator mCameraPowerCalculator;
    long mChargeTimeRemainingUs;
    private final boolean mCollectBatteryBroadcast;
    private double mComputedPower;
    private final Context mContext;
    PowerCalculator mCpuPowerCalculator;
    PowerCalculator mFlashlightPowerCalculator;
    boolean mHasBluetoothPowerReporting;
    boolean mHasWifiPowerReporting;
    private double mMaxDrainedPower;
    private double mMaxPower;
    private double mMaxRealPower;
    PowerCalculator mMediaPowerCalculator;
    PowerCalculator mMemoryPowerCalculator;
    private double mMinDrainedPower;
    MobileRadioPowerCalculator mMobileRadioPowerCalculator;
    private final List<BatterySipper> mMobilemsppList;
    private PackageManager mPackageManager;
    @UnsupportedAppUsage
    private PowerProfile mPowerProfile;
    long mRawRealtimeUs;
    long mRawUptimeUs;
    PowerCalculator mSensorPowerCalculator;
    private String[] mServicepackageArray;
    private BatteryStats mStats;
    private long mStatsPeriod;
    private int mStatsType;
    private String[] mSystemPackageArray;
    private double mTotalPower;
    long mTypeBatteryRealtimeUs;
    long mTypeBatteryUptimeUs;
    @UnsupportedAppUsage
    private final List<BatterySipper> mUsageList;
    private final SparseArray<List<BatterySipper>> mUserSippers;
    PowerCalculator mWakelockPowerCalculator;
    private final boolean mWifiOnly;
    PowerCalculator mWifiPowerCalculator;
    private final List<BatterySipper> mWifiSippers;

    public static boolean checkWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm == null) {
            return false;
        }
        return !cm.isNetworkSupported(0);
    }

    public static boolean checkHasWifiPowerReporting(BatteryStats stats, PowerProfile profile) {
        return (!stats.hasWifiActivityReporting() || profile.getAveragePower(PowerProfile.POWER_WIFI_CONTROLLER_IDLE) == 0.0d || profile.getAveragePower(PowerProfile.POWER_WIFI_CONTROLLER_RX) == 0.0d || profile.getAveragePower(PowerProfile.POWER_WIFI_CONTROLLER_TX) == 0.0d) ? false : true;
    }

    public static boolean checkHasBluetoothPowerReporting(BatteryStats stats, PowerProfile profile) {
        return (!stats.hasBluetoothActivityReporting() || profile.getAveragePower(PowerProfile.POWER_BLUETOOTH_CONTROLLER_IDLE) == 0.0d || profile.getAveragePower(PowerProfile.POWER_BLUETOOTH_CONTROLLER_RX) == 0.0d || profile.getAveragePower(PowerProfile.POWER_BLUETOOTH_CONTROLLER_TX) == 0.0d) ? false : true;
    }

    @UnsupportedAppUsage
    public BatteryStatsHelper(Context context) {
        this(context, true);
    }

    @UnsupportedAppUsage
    public BatteryStatsHelper(Context context, boolean collectBatteryBroadcast) {
        this(context, collectBatteryBroadcast, checkWifiOnly(context));
    }

    @UnsupportedAppUsage
    public BatteryStatsHelper(Context context, boolean collectBatteryBroadcast, boolean wifiOnly) {
        this.mUsageList = new ArrayList();
        this.mWifiSippers = new ArrayList();
        this.mBluetoothSippers = new ArrayList();
        this.mUserSippers = new SparseArray<>();
        this.mMobilemsppList = new ArrayList();
        this.mStatsType = 0;
        this.mStatsPeriod = 0;
        this.mMaxPower = 1.0d;
        this.mMaxRealPower = 1.0d;
        this.mHasWifiPowerReporting = false;
        this.mHasBluetoothPowerReporting = false;
        this.mContext = context;
        this.mCollectBatteryBroadcast = collectBatteryBroadcast;
        this.mWifiOnly = wifiOnly;
        this.mPackageManager = context.getPackageManager();
        Resources resources = context.getResources();
        this.mSystemPackageArray = resources.getStringArray(R.array.config_batteryPackageTypeSystem);
        this.mServicepackageArray = resources.getStringArray(R.array.config_batteryPackageTypeService);
    }

    public void storeStatsHistoryInFile(String fname) {
        synchronized (sFileXfer) {
            File path = makeFilePath(this.mContext, fname);
            sFileXfer.put(path, getStats());
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(path);
                Parcel hist = Parcel.obtain();
                getStats().writeToParcelWithoutUids(hist, 0);
                fout.write(hist.marshall());
                try {
                    fout.close();
                } catch (IOException e) {
                }
            } catch (IOException e2) {
                Log.w(TAG, "Unable to write history to file", e2);
                if (fout != null) {
                    fout.close();
                }
            } catch (Throwable th) {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e3) {
                    }
                }
                throw th;
            }
        }
    }

    public static BatteryStats statsFromFile(Context context, String fname) {
        synchronized (sFileXfer) {
            File path = makeFilePath(context, fname);
            BatteryStats stats = sFileXfer.get(path);
            if (stats != null) {
                return stats;
            }
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(path);
                byte[] data = readFully(fin);
                Parcel parcel = Parcel.obtain();
                parcel.unmarshall(data, 0, data.length);
                parcel.setDataPosition(0);
                BatteryStatsImpl createFromParcel = BatteryStatsImpl.CREATOR.createFromParcel(parcel);
                try {
                    fin.close();
                } catch (IOException e) {
                }
                return createFromParcel;
            } catch (IOException e2) {
                Log.w(TAG, "Unable to read history to file", e2);
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e3) {
                    }
                }
                return getStats(IBatteryStats.Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME)));
            } catch (Throwable th) {
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        }
    }

    @UnsupportedAppUsage
    public static void dropFile(Context context, String fname) {
        makeFilePath(context, fname).delete();
    }

    private static File makeFilePath(Context context, String fname) {
        return new File(context.getFilesDir(), fname);
    }

    @UnsupportedAppUsage
    public void clearStats() {
        this.mStats = null;
    }

    @UnsupportedAppUsage
    public BatteryStats getStats() {
        if (this.mStats == null) {
            load();
        }
        return this.mStats;
    }

    @UnsupportedAppUsage
    public Intent getBatteryBroadcast() {
        if (this.mBatteryBroadcast == null && this.mCollectBatteryBroadcast) {
            load();
        }
        return this.mBatteryBroadcast;
    }

    public PowerProfile getPowerProfile() {
        return this.mPowerProfile;
    }

    public void create(BatteryStats stats) {
        this.mPowerProfile = new PowerProfile(this.mContext);
        this.mStats = stats;
    }

    @UnsupportedAppUsage
    public void create(Bundle icicle) {
        if (icicle != null) {
            this.mStats = sStatsXfer;
            this.mBatteryBroadcast = sBatteryBroadcastXfer;
        }
        this.mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME));
        this.mPowerProfile = new PowerProfile(this.mContext);
    }

    @UnsupportedAppUsage
    public void storeState() {
        sStatsXfer = this.mStats;
        sBatteryBroadcastXfer = this.mBatteryBroadcast;
    }

    public static String makemAh(double power) {
        String format;
        if (power == 0.0d) {
            return WifiEnterpriseConfig.ENGINE_DISABLE;
        }
        if (power < 1.0E-5d) {
            format = "%.8f";
        } else if (power < 1.0E-4d) {
            format = "%.7f";
        } else if (power < 0.001d) {
            format = "%.6f";
        } else if (power < 0.01d) {
            format = "%.5f";
        } else if (power < 0.1d) {
            format = "%.4f";
        } else if (power < 1.0d) {
            format = "%.3f";
        } else if (power < 10.0d) {
            format = "%.2f";
        } else if (power < 100.0d) {
            format = "%.1f";
        } else {
            format = "%.0f";
        }
        return String.format(Locale.ENGLISH, format, Double.valueOf(power));
    }

    @UnsupportedAppUsage
    public void refreshStats(int statsType, int asUser) {
        SparseArray<UserHandle> users = new SparseArray<>(1);
        users.put(asUser, new UserHandle(asUser));
        refreshStats(statsType, users);
    }

    @UnsupportedAppUsage
    public void refreshStats(int statsType, List<UserHandle> asUsers) {
        int n = asUsers.size();
        SparseArray<UserHandle> users = new SparseArray<>(n);
        for (int i = 0; i < n; i++) {
            UserHandle userHandle = asUsers.get(i);
            users.put(userHandle.getIdentifier(), userHandle);
        }
        refreshStats(statsType, users);
    }

    @UnsupportedAppUsage
    public void refreshStats(int statsType, SparseArray<UserHandle> asUsers) {
        refreshStats(statsType, asUsers, SystemClock.elapsedRealtime() * 1000, SystemClock.uptimeMillis() * 1000);
    }

    public void refreshStats(int statsType, SparseArray<UserHandle> asUsers, long rawRealtimeUs, long rawUptimeUs) {
        int size;
        PowerCalculator powerCalculator;
        BatteryStatsHelper batteryStatsHelper = this;
        if (statsType != 0) {
            Log.w(TAG, "refreshStats called for statsType " + statsType + " but only STATS_SINCE_CHARGED is supported. Using STATS_SINCE_CHARGED instead.");
        }
        getStats();
        batteryStatsHelper.mMaxPower = 0.0d;
        batteryStatsHelper.mMaxRealPower = 0.0d;
        batteryStatsHelper.mComputedPower = 0.0d;
        batteryStatsHelper.mTotalPower = 0.0d;
        batteryStatsHelper.mUsageList.clear();
        batteryStatsHelper.mWifiSippers.clear();
        batteryStatsHelper.mBluetoothSippers.clear();
        batteryStatsHelper.mUserSippers.clear();
        batteryStatsHelper.mMobilemsppList.clear();
        if (batteryStatsHelper.mStats != null) {
            if (batteryStatsHelper.mCpuPowerCalculator == null) {
                batteryStatsHelper.mCpuPowerCalculator = new CpuPowerCalculator(batteryStatsHelper.mPowerProfile);
            }
            batteryStatsHelper.mCpuPowerCalculator.reset();
            if (batteryStatsHelper.mMemoryPowerCalculator == null) {
                batteryStatsHelper.mMemoryPowerCalculator = new MemoryPowerCalculator(batteryStatsHelper.mPowerProfile);
            }
            batteryStatsHelper.mMemoryPowerCalculator.reset();
            if (batteryStatsHelper.mWakelockPowerCalculator == null) {
                batteryStatsHelper.mWakelockPowerCalculator = new WakelockPowerCalculator(batteryStatsHelper.mPowerProfile);
            }
            batteryStatsHelper.mWakelockPowerCalculator.reset();
            if (batteryStatsHelper.mMobileRadioPowerCalculator == null) {
                batteryStatsHelper.mMobileRadioPowerCalculator = new MobileRadioPowerCalculator(batteryStatsHelper.mPowerProfile, batteryStatsHelper.mStats);
            }
            batteryStatsHelper.mMobileRadioPowerCalculator.reset(batteryStatsHelper.mStats);
            boolean hasWifiPowerReporting = checkHasWifiPowerReporting(batteryStatsHelper.mStats, batteryStatsHelper.mPowerProfile);
            if (batteryStatsHelper.mWifiPowerCalculator == null || hasWifiPowerReporting != batteryStatsHelper.mHasWifiPowerReporting) {
                if (hasWifiPowerReporting) {
                    powerCalculator = new WifiPowerCalculator(batteryStatsHelper.mPowerProfile);
                } else {
                    powerCalculator = new WifiPowerEstimator(batteryStatsHelper.mPowerProfile);
                }
                batteryStatsHelper.mWifiPowerCalculator = powerCalculator;
                batteryStatsHelper.mHasWifiPowerReporting = hasWifiPowerReporting;
            }
            batteryStatsHelper.mWifiPowerCalculator.reset();
            boolean hasBluetoothPowerReporting = checkHasBluetoothPowerReporting(batteryStatsHelper.mStats, batteryStatsHelper.mPowerProfile);
            if (batteryStatsHelper.mBluetoothPowerCalculator == null || hasBluetoothPowerReporting != batteryStatsHelper.mHasBluetoothPowerReporting) {
                batteryStatsHelper.mBluetoothPowerCalculator = new BluetoothPowerCalculator(batteryStatsHelper.mPowerProfile);
                batteryStatsHelper.mHasBluetoothPowerReporting = hasBluetoothPowerReporting;
            }
            batteryStatsHelper.mBluetoothPowerCalculator.reset();
            batteryStatsHelper.mSensorPowerCalculator = new SensorPowerCalculator(batteryStatsHelper.mPowerProfile, (SensorManager) batteryStatsHelper.mContext.getSystemService(Context.SENSOR_SERVICE), batteryStatsHelper.mStats, rawRealtimeUs, statsType);
            batteryStatsHelper.mSensorPowerCalculator.reset();
            if (batteryStatsHelper.mCameraPowerCalculator == null) {
                batteryStatsHelper.mCameraPowerCalculator = new CameraPowerCalculator(batteryStatsHelper.mPowerProfile);
            }
            batteryStatsHelper.mCameraPowerCalculator.reset();
            if (batteryStatsHelper.mFlashlightPowerCalculator == null) {
                batteryStatsHelper.mFlashlightPowerCalculator = new FlashlightPowerCalculator(batteryStatsHelper.mPowerProfile);
            }
            batteryStatsHelper.mFlashlightPowerCalculator.reset();
            if (batteryStatsHelper.mMediaPowerCalculator == null) {
                batteryStatsHelper.mMediaPowerCalculator = new MediaPowerCalculator(batteryStatsHelper.mPowerProfile);
            }
            batteryStatsHelper.mMediaPowerCalculator.reset();
            batteryStatsHelper.mStatsType = statsType;
            batteryStatsHelper.mRawUptimeUs = rawUptimeUs;
            batteryStatsHelper.mRawRealtimeUs = rawRealtimeUs;
            batteryStatsHelper.mBatteryUptimeUs = batteryStatsHelper.mStats.getBatteryUptime(rawUptimeUs);
            batteryStatsHelper.mBatteryRealtimeUs = batteryStatsHelper.mStats.getBatteryRealtime(rawRealtimeUs);
            batteryStatsHelper.mTypeBatteryUptimeUs = batteryStatsHelper.mStats.computeBatteryUptime(rawUptimeUs, batteryStatsHelper.mStatsType);
            batteryStatsHelper.mTypeBatteryRealtimeUs = batteryStatsHelper.mStats.computeBatteryRealtime(rawRealtimeUs, batteryStatsHelper.mStatsType);
            batteryStatsHelper.mBatteryTimeRemainingUs = batteryStatsHelper.mStats.computeBatteryTimeRemaining(rawRealtimeUs);
            batteryStatsHelper.mChargeTimeRemainingUs = batteryStatsHelper.mStats.computeChargeTimeRemaining(rawRealtimeUs);
            batteryStatsHelper.mMinDrainedPower = (((double) batteryStatsHelper.mStats.getLowDischargeAmountSinceCharge()) * batteryStatsHelper.mPowerProfile.getBatteryCapacity()) / 100.0d;
            batteryStatsHelper.mMaxDrainedPower = (((double) batteryStatsHelper.mStats.getHighDischargeAmountSinceCharge()) * batteryStatsHelper.mPowerProfile.getBatteryCapacity()) / 100.0d;
            batteryStatsHelper.processAppUsage(asUsers);
            for (int i = 0; i < batteryStatsHelper.mUsageList.size(); i++) {
                BatterySipper bs = batteryStatsHelper.mUsageList.get(i);
                bs.computeMobilemspp();
                if (bs.mobilemspp != 0.0d) {
                    batteryStatsHelper.mMobilemsppList.add(bs);
                }
            }
            for (int i2 = 0; i2 < batteryStatsHelper.mUserSippers.size(); i2++) {
                List<BatterySipper> user = batteryStatsHelper.mUserSippers.valueAt(i2);
                for (int j = 0; j < user.size(); j++) {
                    BatterySipper bs2 = user.get(j);
                    bs2.computeMobilemspp();
                    if (bs2.mobilemspp != 0.0d) {
                        batteryStatsHelper.mMobilemsppList.add(bs2);
                    }
                }
            }
            Collections.sort(batteryStatsHelper.mMobilemsppList, new Comparator<BatterySipper>() {
                /* class com.android.internal.os.BatteryStatsHelper.AnonymousClass1 */

                public int compare(BatterySipper lhs, BatterySipper rhs) {
                    return Double.compare(rhs.mobilemspp, lhs.mobilemspp);
                }
            });
            processMiscUsage();
            Collections.sort(batteryStatsHelper.mUsageList);
            if (!batteryStatsHelper.mUsageList.isEmpty()) {
                double d = batteryStatsHelper.mUsageList.get(0).totalPowerMah;
                batteryStatsHelper.mMaxPower = d;
                batteryStatsHelper.mMaxRealPower = d;
                int usageListCount = batteryStatsHelper.mUsageList.size();
                for (int i3 = 0; i3 < usageListCount; i3++) {
                    batteryStatsHelper.mComputedPower += batteryStatsHelper.mUsageList.get(i3).totalPowerMah;
                }
            }
            batteryStatsHelper.mTotalPower = batteryStatsHelper.mComputedPower;
            if (batteryStatsHelper.mStats.getLowDischargeAmountSinceCharge() > 1) {
                double d2 = batteryStatsHelper.mMinDrainedPower;
                double d3 = batteryStatsHelper.mComputedPower;
                if (d2 > d3) {
                    double amount = d2 - d3;
                    batteryStatsHelper.mTotalPower = d2;
                    BatterySipper bs3 = new BatterySipper(BatterySipper.DrainType.UNACCOUNTED, null, amount);
                    int index = Collections.binarySearch(batteryStatsHelper.mUsageList, bs3);
                    if (index < 0) {
                        index = -(index + 1);
                    }
                    batteryStatsHelper.mUsageList.add(index, bs3);
                    batteryStatsHelper.mMaxPower = Math.max(batteryStatsHelper.mMaxPower, amount);
                } else {
                    double d4 = batteryStatsHelper.mMaxDrainedPower;
                    if (d4 < d3) {
                        double amount2 = d3 - d4;
                        BatterySipper bs4 = new BatterySipper(BatterySipper.DrainType.OVERCOUNTED, null, amount2);
                        int index2 = Collections.binarySearch(batteryStatsHelper.mUsageList, bs4);
                        if (index2 < 0) {
                            index2 = -(index2 + 1);
                        }
                        batteryStatsHelper.mUsageList.add(index2, bs4);
                        batteryStatsHelper.mMaxPower = Math.max(batteryStatsHelper.mMaxPower, amount2);
                    }
                }
            }
            double hiddenPowerMah = batteryStatsHelper.removeHiddenBatterySippers(batteryStatsHelper.mUsageList);
            double totalRemainingPower = getTotalPower() - hiddenPowerMah;
            if (Math.abs(totalRemainingPower) > 0.001d) {
                int i4 = 0;
                int size2 = batteryStatsHelper.mUsageList.size();
                while (i4 < size2) {
                    BatterySipper sipper = batteryStatsHelper.mUsageList.get(i4);
                    if (!sipper.shouldHide) {
                        size = size2;
                        sipper.proportionalSmearMah = ((sipper.totalPowerMah + sipper.screenPowerMah) / totalRemainingPower) * hiddenPowerMah;
                        sipper.sumPower();
                    } else {
                        size = size2;
                    }
                    i4++;
                    batteryStatsHelper = this;
                    size2 = size;
                }
            }
        }
    }

    private void processAppUsage(SparseArray<UserHandle> asUsers) {
        BatterySipper app;
        boolean forAllUsers = asUsers.get(-1) != null;
        this.mStatsPeriod = this.mTypeBatteryRealtimeUs;
        BatterySipper osSipper = null;
        SparseArray<? extends BatteryStats.Uid> uidStats = this.mStats.getUidStats();
        int NU = uidStats.size();
        int iu = 0;
        while (iu < NU) {
            BatteryStats.Uid u = (BatteryStats.Uid) uidStats.valueAt(iu);
            BatterySipper app2 = new BatterySipper(BatterySipper.DrainType.APP, u, 0.0d);
            this.mCpuPowerCalculator.calculateApp(app2, u, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
            this.mWakelockPowerCalculator.calculateApp(app2, u, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
            this.mMobileRadioPowerCalculator.calculateApp(app2, u, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
            this.mWifiPowerCalculator.calculateApp(app2, u, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
            this.mBluetoothPowerCalculator.calculateApp(app2, u, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
            this.mSensorPowerCalculator.calculateApp(app2, u, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
            this.mCameraPowerCalculator.calculateApp(app2, u, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
            this.mFlashlightPowerCalculator.calculateApp(app2, u, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
            this.mMediaPowerCalculator.calculateApp(app2, u, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
            if (app2.sumPower() != 0.0d || u.getUid() == 0) {
                int uid = app2.getUid();
                int userId = UserHandle.getUserId(uid);
                if (uid == 1010) {
                    app = app2;
                    this.mWifiSippers.add(app);
                } else {
                    app = app2;
                    if (uid == 1002) {
                        this.mBluetoothSippers.add(app);
                    } else if (forAllUsers || asUsers.get(userId) != null || UserHandle.getAppId(uid) < 10000) {
                        this.mUsageList.add(app);
                    } else {
                        List<BatterySipper> list = this.mUserSippers.get(userId);
                        if (list == null) {
                            list = new ArrayList();
                            this.mUserSippers.put(userId, list);
                        }
                        list.add(app);
                    }
                }
                if (uid == 0) {
                    osSipper = app;
                }
            }
            iu++;
            uidStats = uidStats;
        }
        if (osSipper != null) {
            this.mWakelockPowerCalculator.calculateRemaining(osSipper, this.mStats, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
            osSipper.sumPower();
        }
    }

    private void addPhoneUsage() {
        long phoneOnTimeMs = this.mStats.getPhoneOnTime(this.mRawRealtimeUs, this.mStatsType) / 1000;
        double phoneOnPower = (this.mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) * ((double) phoneOnTimeMs)) / 3600000.0d;
        if (phoneOnPower != 0.0d) {
            addEntry(BatterySipper.DrainType.PHONE, phoneOnTimeMs, phoneOnPower);
        }
    }

    private void addScreenUsage() {
        long j = 1000;
        long screenOnTimeMs = this.mStats.getScreenOnTime(this.mRawRealtimeUs, this.mStatsType) / 1000;
        double power = 0.0d + (((double) screenOnTimeMs) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON));
        double screenFullPower = this.mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
        int i = 0;
        while (i < 5) {
            power += ((double) (this.mStats.getScreenBrightnessTime(i, this.mRawRealtimeUs, this.mStatsType) / j)) * ((((double) (((float) i) + 0.5f)) * screenFullPower) / 5.0d);
            i++;
            j = 1000;
        }
        double power2 = power / 3600000.0d;
        if (power2 != 0.0d) {
            addEntry(BatterySipper.DrainType.SCREEN, screenOnTimeMs, power2);
        }
    }

    private void addAmbientDisplayUsage() {
        long ambientDisplayMs = this.mStats.getScreenDozeTime(this.mRawRealtimeUs, this.mStatsType) / 1000;
        double power = (this.mPowerProfile.getAveragePower(PowerProfile.POWER_AMBIENT_DISPLAY) * ((double) ambientDisplayMs)) / 3600000.0d;
        if (power > 0.0d) {
            addEntry(BatterySipper.DrainType.AMBIENT_DISPLAY, ambientDisplayMs, power);
        }
    }

    private void addRadioUsage() {
        BatterySipper radio = new BatterySipper(BatterySipper.DrainType.CELL, null, 0.0d);
        this.mMobileRadioPowerCalculator.calculateRemaining(radio, this.mStats, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
        radio.sumPower();
        if (radio.totalPowerMah > 0.0d) {
            this.mUsageList.add(radio);
        }
    }

    private void aggregateSippers(BatterySipper bs, List<BatterySipper> from, String tag) {
        for (int i = 0; i < from.size(); i++) {
            bs.add(from.get(i));
        }
        bs.computeMobilemspp();
        bs.sumPower();
    }

    private void addIdleUsage() {
        double totalPowerMah = ((((double) (this.mTypeBatteryRealtimeUs / 1000)) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_SUSPEND)) + (((double) (this.mStats.getScreenOnTime(SystemClock.elapsedRealtime() * 1000, this.mStatsType) / 1000)) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_IDLE))) / 3600000.0d;
        if (totalPowerMah != 0.0d) {
            addEntry(BatterySipper.DrainType.IDLE, this.mTypeBatteryRealtimeUs / 1000, totalPowerMah);
        }
    }

    private void addWiFiUsage() {
        BatterySipper bs = new BatterySipper(BatterySipper.DrainType.WIFI, null, 0.0d);
        this.mWifiPowerCalculator.calculateRemaining(bs, this.mStats, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
        aggregateSippers(bs, this.mWifiSippers, "WIFI");
        if (bs.totalPowerMah > 0.0d) {
            this.mUsageList.add(bs);
        }
    }

    private void addBluetoothUsage() {
        BatterySipper bs = new BatterySipper(BatterySipper.DrainType.BLUETOOTH, null, 0.0d);
        this.mBluetoothPowerCalculator.calculateRemaining(bs, this.mStats, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
        aggregateSippers(bs, this.mBluetoothSippers, "Bluetooth");
        if (bs.totalPowerMah > 0.0d) {
            this.mUsageList.add(bs);
        }
    }

    private void addUserUsage() {
        for (int i = 0; i < this.mUserSippers.size(); i++) {
            int userId = this.mUserSippers.keyAt(i);
            BatterySipper bs = new BatterySipper(BatterySipper.DrainType.USER, null, 0.0d);
            bs.userId = userId;
            aggregateSippers(bs, this.mUserSippers.valueAt(i), "User");
            this.mUsageList.add(bs);
        }
    }

    private void addMemoryUsage() {
        BatterySipper memory = new BatterySipper(BatterySipper.DrainType.MEMORY, null, 0.0d);
        this.mMemoryPowerCalculator.calculateRemaining(memory, this.mStats, this.mRawRealtimeUs, this.mRawUptimeUs, this.mStatsType);
        memory.sumPower();
        if (memory.totalPowerMah > 0.0d) {
            this.mUsageList.add(memory);
        }
    }

    private void processMiscUsage() {
        addUserUsage();
        addPhoneUsage();
        addScreenUsage();
        addAmbientDisplayUsage();
        addWiFiUsage();
        addBluetoothUsage();
        addMemoryUsage();
        addIdleUsage();
        if (!this.mWifiOnly) {
            addRadioUsage();
        }
    }

    private BatterySipper addEntry(BatterySipper.DrainType drainType, long time, double power) {
        BatterySipper bs = new BatterySipper(drainType, null, 0.0d);
        bs.usagePowerMah = power;
        bs.usageTimeMs = time;
        bs.sumPower();
        this.mUsageList.add(bs);
        return bs;
    }

    @UnsupportedAppUsage
    public List<BatterySipper> getUsageList() {
        return this.mUsageList;
    }

    public List<BatterySipper> getMobilemsppList() {
        return this.mMobilemsppList;
    }

    public long getStatsPeriod() {
        return this.mStatsPeriod;
    }

    public int getStatsType() {
        return this.mStatsType;
    }

    @UnsupportedAppUsage
    public double getMaxPower() {
        return this.mMaxPower;
    }

    public double getMaxRealPower() {
        return this.mMaxRealPower;
    }

    @UnsupportedAppUsage
    public double getTotalPower() {
        return this.mTotalPower;
    }

    public double getComputedPower() {
        return this.mComputedPower;
    }

    public double getMinDrainedPower() {
        return this.mMinDrainedPower;
    }

    public double getMaxDrainedPower() {
        return this.mMaxDrainedPower;
    }

    public static byte[] readFully(FileInputStream stream) throws IOException {
        return readFully(stream, stream.available());
    }

    public static byte[] readFully(FileInputStream stream, int avail) throws IOException {
        int pos = 0;
        byte[] data = new byte[avail];
        while (true) {
            int amt = stream.read(data, pos, data.length - pos);
            if (amt <= 0) {
                return data;
            }
            pos += amt;
            int avail2 = stream.available();
            if (avail2 > data.length - pos) {
                byte[] newData = new byte[(pos + avail2)];
                System.arraycopy(data, 0, newData, 0, pos);
                data = newData;
            }
        }
    }

    public double removeHiddenBatterySippers(List<BatterySipper> sippers) {
        double proportionalSmearPowerMah = 0.0d;
        BatterySipper screenSipper = null;
        for (int i = sippers.size() - 1; i >= 0; i--) {
            BatterySipper sipper = sippers.get(i);
            sipper.shouldHide = shouldHideSipper(sipper);
            if (!(!sipper.shouldHide || sipper.drainType == BatterySipper.DrainType.OVERCOUNTED || sipper.drainType == BatterySipper.DrainType.SCREEN || sipper.drainType == BatterySipper.DrainType.AMBIENT_DISPLAY || sipper.drainType == BatterySipper.DrainType.UNACCOUNTED || sipper.drainType == BatterySipper.DrainType.BLUETOOTH || sipper.drainType == BatterySipper.DrainType.WIFI || sipper.drainType == BatterySipper.DrainType.IDLE)) {
                proportionalSmearPowerMah += sipper.totalPowerMah;
            }
            if (sipper.drainType == BatterySipper.DrainType.SCREEN) {
                screenSipper = sipper;
            }
        }
        smearScreenBatterySipper(sippers, screenSipper);
        return proportionalSmearPowerMah;
    }

    public void smearScreenBatterySipper(List<BatterySipper> sippers, BatterySipper screenSipper) {
        long totalActivityTimeMs = 0;
        SparseLongArray activityTimeArray = new SparseLongArray();
        int size = sippers.size();
        for (int i = 0; i < size; i++) {
            BatteryStats.Uid uid = sippers.get(i).uidObj;
            if (uid != null) {
                long timeMs = getProcessForegroundTimeMs(uid, 0);
                activityTimeArray.put(uid.getUid(), timeMs);
                totalActivityTimeMs += timeMs;
            }
        }
        if (screenSipper != null && totalActivityTimeMs >= 600000) {
            double screenPowerMah = screenSipper.totalPowerMah;
            int size2 = sippers.size();
            for (int i2 = 0; i2 < size2; i2++) {
                BatterySipper sipper = sippers.get(i2);
                sipper.screenPowerMah = (((double) activityTimeArray.get(sipper.getUid(), 0)) * screenPowerMah) / ((double) totalActivityTimeMs);
            }
        }
    }

    public boolean shouldHideSipper(BatterySipper sipper) {
        BatterySipper.DrainType drainType = sipper.drainType;
        return drainType == BatterySipper.DrainType.IDLE || drainType == BatterySipper.DrainType.CELL || drainType == BatterySipper.DrainType.SCREEN || drainType == BatterySipper.DrainType.AMBIENT_DISPLAY || drainType == BatterySipper.DrainType.UNACCOUNTED || drainType == BatterySipper.DrainType.OVERCOUNTED || isTypeService(sipper) || isTypeSystem(sipper);
    }

    public boolean isTypeService(BatterySipper sipper) {
        String[] packages = this.mPackageManager.getPackagesForUid(sipper.getUid());
        if (packages == null) {
            return false;
        }
        for (String packageName : packages) {
            if (ArrayUtils.contains(this.mServicepackageArray, packageName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTypeSystem(BatterySipper sipper) {
        int uid = sipper.uidObj == null ? -1 : sipper.getUid();
        sipper.mPackages = this.mPackageManager.getPackagesForUid(uid);
        if (uid >= 0 && uid < 10000) {
            return true;
        }
        if (sipper.mPackages != null) {
            for (String packageName : sipper.mPackages) {
                if (ArrayUtils.contains(this.mSystemPackageArray, packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public long convertUsToMs(long timeUs) {
        return timeUs / 1000;
    }

    public long convertMsToUs(long timeMs) {
        return 1000 * timeMs;
    }

    @VisibleForTesting
    public long getForegroundActivityTotalTimeUs(BatteryStats.Uid uid, long rawRealtimeUs) {
        BatteryStats.Timer timer = uid.getForegroundActivityTimer();
        if (timer != null) {
            return timer.getTotalTimeLocked(rawRealtimeUs, 0);
        }
        return 0;
    }

    @VisibleForTesting
    public long getProcessForegroundTimeMs(BatteryStats.Uid uid, int which) {
        long rawRealTimeUs = convertMsToUs(SystemClock.elapsedRealtime());
        long timeUs = 0;
        for (int type : new int[]{0}) {
            timeUs += uid.getProcessStateTime(type, rawRealTimeUs, which);
        }
        return convertUsToMs(Math.min(timeUs, getForegroundActivityTotalTimeUs(uid, rawRealTimeUs)));
    }

    @VisibleForTesting
    public void setPackageManager(PackageManager packageManager) {
        this.mPackageManager = packageManager;
    }

    @VisibleForTesting
    public void setSystemPackageArray(String[] array) {
        this.mSystemPackageArray = array;
    }

    @VisibleForTesting
    public void setServicePackageArray(String[] array) {
        this.mServicepackageArray = array;
    }

    @UnsupportedAppUsage
    private void load() {
        IBatteryStats iBatteryStats = this.mBatteryInfo;
        if (iBatteryStats != null) {
            this.mStats = getStats(iBatteryStats);
            if (this.mCollectBatteryBroadcast) {
                this.mBatteryBroadcast = this.mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0045, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004a, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004b, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004e, code lost:
        throw r5;
     */
    private static BatteryStatsImpl getStats(IBatteryStats service) {
        try {
            ParcelFileDescriptor pfd = service.getStatisticsStream();
            if (pfd != null) {
                try {
                    FileInputStream fis = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
                    byte[] data = readFully(fis, MemoryFile.getSize(pfd.getFileDescriptor()));
                    Parcel parcel = Parcel.obtain();
                    parcel.unmarshall(data, 0, data.length);
                    parcel.setDataPosition(0);
                    BatteryStatsImpl stats = BatteryStatsImpl.CREATOR.createFromParcel(parcel);
                    fis.close();
                    try {
                        pfd.close();
                        Log.i(TAG, "close battery-stats ashmem fd.");
                    } catch (IOException e) {
                        Log.w(TAG, "close ashmem fd error.");
                    }
                    return stats;
                } catch (IOException e2) {
                    Log.w(TAG, "Unable to read statistics stream", e2);
                    try {
                        pfd.close();
                        Log.i(TAG, "close battery-stats ashmem fd.");
                    } catch (IOException e3) {
                        Log.w(TAG, "close ashmem fd error.");
                    }
                } catch (Throwable th) {
                    try {
                        pfd.close();
                        Log.i(TAG, "close battery-stats ashmem fd.");
                    } catch (IOException e4) {
                        Log.w(TAG, "close ashmem fd error.");
                    }
                    throw th;
                }
            }
        } catch (RemoteException e5) {
            Log.w(TAG, "RemoteException:", e5);
        }
        return new BatteryStatsImpl();
    }
}
