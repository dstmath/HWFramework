package com.android.server;

import android.app.ActivityThread;
import android.common.HwActivityThread;
import android.common.HwFrameworkFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.wifipro.WifiProCommonUtils;

public final class HwMapleHelper {
    private static final int BETA_USER_TYPE = 3;
    private static final boolean DEBUG = false;
    private static final boolean MAPLE_PROCESS = ActivityThread.sIsMygote;
    private static final int MESSAGE_DELAY = 300000;
    private static final int MESSAGE_DURATION = 3600000;
    private static final int MSG_ID_REPORT_RT = 1;
    private static final String PROCESS_NAME = "system_server";
    private static final String TAG = "HwMapleHelper";
    private static final int USER_TYPE = SystemProperties.getInt("ro.logsystem.usertype", 0);
    private static HwMapleHelper instanceHelper = null;
    private MapleHelperHandler mMapleHelperHandler;

    private static class MapleHelperHandler extends Handler {
        private HwActivityThread mHwActivityThread;
        private long mStartTime;

        public MapleHelperHandler(Looper looper) {
            super(looper);
            this.mHwActivityThread = null;
            this.mHwActivityThread = HwFrameworkFactory.getHwActivityThread();
            this.mStartTime = System.currentTimeMillis();
        }

        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                Slog.w(HwMapleHelper.TAG, "can not handle the msg:" + msg.what);
                return;
            }
            if (!handleScheduleReportData()) {
                Slog.e(HwMapleHelper.TAG, "fail to report runtime infomation!!!");
            }
            sendEmptyMessageDelayed(1, WifiProCommonUtils.RECHECK_DELAYED_MS);
        }

        private boolean handleScheduleReportData() {
            if (this.mHwActivityThread == null) {
                return false;
            }
            return this.mHwActivityThread.doReportRuntime(HwMapleHelper.PROCESS_NAME, this.mStartTime);
        }

        private String codeToString(int code) {
            return Integer.toString(code);
        }
    }

    public static boolean isSupportMapleHelper() {
        return MAPLE_PROCESS && USER_TYPE == 3;
    }

    public static void startMapleHelper() {
        if (instanceHelper == null) {
            instanceHelper = new HwMapleHelper();
        }
    }

    private HwMapleHelper() {
        this.mMapleHelperHandler = null;
        this.mMapleHelperHandler = new MapleHelperHandler(BackgroundThread.getHandler().getLooper());
        this.mMapleHelperHandler.sendEmptyMessageDelayed(1, HwArbitrationDEFS.DelayTimeMillisB);
    }
}
