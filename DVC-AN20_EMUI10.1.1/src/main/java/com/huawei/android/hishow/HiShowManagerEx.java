package com.huawei.android.hishow;

import android.hishow.AlarmInfo;
import android.hishow.HwHiShowManager;
import android.hishow.IHwHiShowManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class HiShowManagerEx implements IHwHiShowManagerEx {
    private static final String TAG = "HiShowManagerEx";
    private static volatile HiShowManagerEx sInstance;
    private IHwHiShowManager remote = null;

    private HiShowManagerEx(IHwHiShowManager ins) {
        StringBuilder sb = new StringBuilder();
        sb.append("Check if remote inst is null ? ");
        sb.append(Boolean.toString(ins == null));
        Log.d(TAG, sb.toString());
        this.remote = ins;
    }

    public static IHwHiShowManagerEx getService() {
        if (sInstance == null) {
            synchronized (HiShowManagerEx.class) {
                if (sInstance == null) {
                    sInstance = new HiShowManagerEx(HwHiShowManager.getService());
                }
            }
        }
        return sInstance;
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void lockScreen() {
        try {
            this.remote.lockScreen();
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void controlStatusBar(boolean enable) {
        try {
            this.remote.controlStatusBar(enable);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void controlHomeButton(boolean enable) {
        try {
            this.remote.controlHomeButton(enable);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void controlRecentButton(boolean enable) {
        try {
            this.remote.controlRecentButton(enable);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void controlFloatButton(boolean enable) {
        try {
            this.remote.controlFloatButton(enable);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void switchDisturb(boolean enable) {
        try {
            this.remote.switchDisturb(enable);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void startToCharge() {
        try {
            this.remote.startToCharge();
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void stopCharging() {
        try {
            this.remote.stopCharging();
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void lightOffScreen(int brightness) {
        try {
            this.remote.lightOffScreen(brightness);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public List<AlarmInfoEx> queryAllAlarmInfo() {
        List<AlarmInfoEx> result = new ArrayList<>();
        try {
            List<AlarmInfo> r = this.remote.queryAllAlarmInfo();
            if (r != null) {
                for (AlarmInfo ai : r) {
                    result.add(new AlarmInfoEx(ai));
                }
            }
            return result;
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
            return result;
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public int addNewAlarm(AlarmInfoEx alarmInfo) {
        try {
            return this.remote.addNewAlarm(copy(alarmInfo));
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
            return -1;
        }
    }

    private AlarmInfo copy(AlarmInfoEx aie) {
        AlarmInfo ai = new AlarmInfo();
        ai.setId(aie.getId());
        ai.setEnabled(aie.isEnabled());
        ai.setHour(aie.getHour());
        ai.setMinutes(aie.getMinutes());
        ai.setDaysOfWeek(aie.getDaysOfWeek());
        ai.setDaysOfWeekType(aie.getDaysOfWeekType());
        ai.setDaysOfWeekShow(aie.getDaysOfWeekShow());
        ai.setTime(aie.getTime());
        ai.setVibrate(aie.isVibrate());
        ai.setLabel(aie.getLabel());
        ai.setAlert(aie.getAlert());
        ai.setVolume(aie.getVolume());
        ai.setAlarmType(aie.getAlarmType());
        return ai;
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public boolean deleteAlarm(int alarmId) {
        try {
            return this.remote.deleteAlarm(alarmId);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public boolean controlAlarm(boolean enable) {
        try {
            return this.remote.controlAlarm(enable);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void closeCurrentAlarm() {
        try {
            this.remote.closeCurrentAlarm();
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public boolean setAsDefaultLauncher(String pkgName, String activityName) {
        try {
            return this.remote.setAsDefaultLauncher(pkgName, activityName);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public boolean restorePreLauncher() {
        try {
            return this.remote.restorePreLauncher();
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void setActivityController(List<String> pkgWhitelist, List<String> actWhitelist, List<String> pkgBlacklist, List<String> actBlackList) {
        try {
            this.remote.setActivityController(pkgWhitelist, actWhitelist, pkgBlacklist, actBlackList);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public void cancelActivityController() {
        try {
            this.remote.cancelActivityController();
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override // com.huawei.android.hishow.IHwHiShowManagerEx
    public String requestSpecialInfo(String key) {
        try {
            return this.remote.requestSpecialInfo(key);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
            return null;
        }
    }
}
