package com.huawei.android.hishow;

import java.util.List;

public interface IHwHiShowManagerEx {
    int addNewAlarm(AlarmInfoEx alarmInfoEx);

    void cancelActivityController();

    void closeCurrentAlarm();

    boolean controlAlarm(boolean z);

    void controlFloatButton(boolean z);

    void controlHomeButton(boolean z);

    void controlRecentButton(boolean z);

    void controlStatusBar(boolean z);

    boolean deleteAlarm(int i);

    void lightOffScreen(int i);

    void lockScreen();

    List<AlarmInfoEx> queryAllAlarmInfo();

    String requestSpecialInfo(String str);

    boolean restorePreLauncher();

    void setActivityController(List<String> list, List<String> list2, List<String> list3, List<String> list4);

    boolean setAsDefaultLauncher(String str, String str2);

    void startToCharge();

    void stopCharging();

    void switchDisturb(boolean z);
}
