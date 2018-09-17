package com.android.server.emcom.daemon;

import android.os.Message;
import com.android.server.emcom.daemon.DaemonCommand.DaemonReportCallback;

public interface CommandsInterface {
    public static final int EMCOM_DS_APP_LIST = 518;
    public static final int EMCOM_DS_BROWSER_INFO = 513;
    public static final int EMCOM_DS_FEATURE_CONFIG = 1;
    public static final int EMCOM_DS_HTTP_INFO = 514;
    public static final int EMCOM_DS_PAGE_ID = 517;
    public static final int EMCOM_DS_SAMPLE_WIN_STATS = 516;
    public static final int EMCOM_DS_TCP_STATS = 515;
    public static final int EMCOM_DS_XENGINE_DEV_FAIL = 2;
    public static final int EMCOM_SD_APP_FOREGROUND = 2;
    public static final int EMCOM_SD_BOOT_COMPLETE = 1;
    public static final int EMCOM_SD_CONFIG_UPDATE = 5;
    public static final int EMCOM_SD_PACKAGE_CHANGE = 4;
    public static final int EMCOM_SD_SAMPLE_WIN_CLOSE = 513;
    public static final int EMCOM_SD_SCREEN_STATUS = 3;
    public static final int EMCOM_SD_START_UDP_RETRAN = 260;
    public static final int EMCOM_SD_STOP_UDP_RETRAN = 261;
    public static final int EMCOM_SD_XENGINE_CONFIG_MPIP = 262;
    public static final int EMCOM_SD_XENGINE_SPEED_CTRL = 259;
    public static final int EMCOM_SD_XENGINE_START_ACC = 257;
    public static final int EMCOM_SD_XENGINE_START_MPIP = 263;
    public static final int EMCOM_SD_XENGINE_STOP_ACC = 258;
    public static final int EMCOM_SD_XENGINE_STOP_MPIP = 264;
    public static final int EMCOM_SUB_MOD_COMMON = 0;
    public static final int EMCOM_SUB_MOD_COMMON_BASE = 0;
    public static final int EMCOM_SUB_MOD_PARAMANAGER = 3;
    public static final int EMCOM_SUB_MOD_SMARTCARE = 2;
    public static final int EMCOM_SUB_MOD_SMARTCARE_BASE = 512;
    public static final int EMCOM_SUB_MOD_XENGINE = 1;
    public static final int EMCOM_SUB_MOD_XENGINE_BASE = 256;

    void exeAppForeground(int i, int i2, Message message);

    void exeBootComplete(Message message);

    void exeConfigMpip(int[] iArr, Message message);

    void exeConfigUpdate(Message message);

    void exePackageChanged(int i, String str, Message message);

    void exeScreenStatus(int i, Message message);

    void exeSpeedCtrl(int i, int i2, Message message);

    void exeStartAccelerate(int i, int i2, int i3, Message message);

    void exeStartMpip(String str, Message message);

    void exeStopAccelerate(int i, Message message);

    void exeStopMpip(Message message);

    void exeUdpAcc(int i, Message message);

    void exeUdpStop(int i, Message message);

    void execCloseSampleWin(Message message);

    void registerDaemonCallback(DaemonReportCallback daemonReportCallback);

    void unRegisterDaemonCallback(DaemonReportCallback daemonReportCallback);
}
