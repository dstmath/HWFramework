package com.huawei.android.server;

import android.os.Handler;
import com.android.server.Watchdog;

public class WatchdogEx {

    /* access modifiers changed from: private */
    public static class MonitorBridge implements Watchdog.Monitor {
        private MonitorEx mMonitorEx;

        private MonitorBridge() {
        }

        public void setMonitorEx(MonitorEx monitorEx) {
            this.mMonitorEx = monitorEx;
        }

        public void monitor() {
            MonitorEx monitorEx = this.mMonitorEx;
            if (monitorEx != null) {
                monitorEx.monitor();
            }
        }
    }

    public static class MonitorEx {
        private MonitorBridge mBridge = new MonitorBridge();

        public MonitorEx() {
            this.mBridge.setMonitorEx(this);
        }

        public Watchdog.Monitor getMonitor() {
            return this.mBridge;
        }

        public void monitor() {
        }
    }

    public static void addMonitor(MonitorEx monitorEx) {
        Watchdog.getInstance().addMonitor(monitorEx.getMonitor());
    }

    public static void addThread(Handler thread) {
        Watchdog.getInstance().addThread(thread);
    }
}
