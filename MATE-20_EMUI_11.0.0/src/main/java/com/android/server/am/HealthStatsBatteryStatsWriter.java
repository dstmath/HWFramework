package com.android.server.am;

import android.os.BatteryStats;
import android.os.SystemClock;
import android.os.health.HealthStatsWriter;
import android.os.health.PackageHealthStats;
import android.os.health.PidHealthStats;
import android.os.health.ProcessHealthStats;
import android.os.health.ServiceHealthStats;
import android.os.health.TimerStat;
import android.util.SparseArray;
import com.android.server.EventLogTags;
import java.util.Map;

public class HealthStatsBatteryStatsWriter {
    private final long mNowRealtimeMs = SystemClock.elapsedRealtime();
    private final long mNowUptimeMs = SystemClock.uptimeMillis();

    public void writeUid(HealthStatsWriter uidWriter, BatteryStats bs, BatteryStats.Uid uid) {
        uidWriter.addMeasurement(10001, bs.computeBatteryRealtime(this.mNowRealtimeMs * 1000, 0) / 1000);
        uidWriter.addMeasurement(10002, bs.computeBatteryUptime(this.mNowUptimeMs * 1000, 0) / 1000);
        uidWriter.addMeasurement(10003, bs.computeBatteryScreenOffRealtime(this.mNowRealtimeMs * 1000, 0) / 1000);
        uidWriter.addMeasurement(10004, bs.computeBatteryScreenOffUptime(this.mNowUptimeMs * 1000, 0) / 1000);
        for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> entry : uid.getWakelockStats().entrySet()) {
            String key = entry.getKey();
            BatteryStats.Uid.Wakelock wakelock = (BatteryStats.Uid.Wakelock) entry.getValue();
            addTimers(uidWriter, 10005, key, wakelock.getWakeTime(1));
            addTimers(uidWriter, 10006, key, wakelock.getWakeTime(0));
            addTimers(uidWriter, 10007, key, wakelock.getWakeTime(2));
            addTimers(uidWriter, 10008, key, wakelock.getWakeTime(18));
        }
        for (Map.Entry<String, ? extends BatteryStats.Timer> entry2 : uid.getSyncStats().entrySet()) {
            addTimers(uidWriter, 10009, entry2.getKey(), (BatteryStats.Timer) entry2.getValue());
        }
        for (Map.Entry<String, ? extends BatteryStats.Timer> entry3 : uid.getJobStats().entrySet()) {
            addTimers(uidWriter, 10010, entry3.getKey(), (BatteryStats.Timer) entry3.getValue());
        }
        SparseArray<? extends BatteryStats.Uid.Sensor> sensors = uid.getSensorStats();
        int N = sensors.size();
        for (int i = 0; i < N; i++) {
            int sensorId = sensors.keyAt(i);
            if (sensorId == -10000) {
                addTimer(uidWriter, 10011, ((BatteryStats.Uid.Sensor) sensors.valueAt(i)).getSensorTime());
            } else {
                addTimers(uidWriter, 10012, Integer.toString(sensorId), ((BatteryStats.Uid.Sensor) sensors.valueAt(i)).getSensorTime());
            }
        }
        SparseArray<? extends BatteryStats.Uid.Pid> pids = uid.getPidStats();
        int N2 = pids.size();
        for (int i2 = 0; i2 < N2; i2++) {
            HealthStatsWriter writer = new HealthStatsWriter(PidHealthStats.CONSTANTS);
            writePid(writer, (BatteryStats.Uid.Pid) pids.valueAt(i2));
            uidWriter.addStats(10013, Integer.toString(pids.keyAt(i2)), writer);
        }
        for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> entry4 : uid.getProcessStats().entrySet()) {
            HealthStatsWriter writer2 = new HealthStatsWriter(ProcessHealthStats.CONSTANTS);
            writeProc(writer2, (BatteryStats.Uid.Proc) entry4.getValue());
            uidWriter.addStats(10014, entry4.getKey(), writer2);
        }
        for (Map.Entry<String, ? extends BatteryStats.Uid.Pkg> entry5 : uid.getPackageStats().entrySet()) {
            HealthStatsWriter writer3 = new HealthStatsWriter(PackageHealthStats.CONSTANTS);
            writePkg(writer3, (BatteryStats.Uid.Pkg) entry5.getValue());
            uidWriter.addStats(10015, entry5.getKey(), writer3);
        }
        BatteryStats.ControllerActivityCounter controller = uid.getWifiControllerActivity();
        if (controller != null) {
            uidWriter.addMeasurement(10016, controller.getIdleTimeCounter().getCountLocked(0));
            uidWriter.addMeasurement(10017, controller.getRxTimeCounter().getCountLocked(0));
            long sum = 0;
            for (BatteryStats.LongCounter counter : controller.getTxTimeCounters()) {
                sum += counter.getCountLocked(0);
            }
            uidWriter.addMeasurement(10018, sum);
            uidWriter.addMeasurement(10019, controller.getPowerCounter().getCountLocked(0));
        }
        BatteryStats.ControllerActivityCounter controller2 = uid.getBluetoothControllerActivity();
        if (controller2 != null) {
            uidWriter.addMeasurement(10020, controller2.getIdleTimeCounter().getCountLocked(0));
            uidWriter.addMeasurement(10021, controller2.getRxTimeCounter().getCountLocked(0));
            long sum2 = 0;
            for (BatteryStats.LongCounter counter2 : controller2.getTxTimeCounters()) {
                sum2 += counter2.getCountLocked(0);
            }
            uidWriter.addMeasurement(10022, sum2);
            uidWriter.addMeasurement(10023, controller2.getPowerCounter().getCountLocked(0));
        }
        BatteryStats.ControllerActivityCounter controller3 = uid.getModemControllerActivity();
        if (controller3 != null) {
            uidWriter.addMeasurement(10024, controller3.getIdleTimeCounter().getCountLocked(0));
            uidWriter.addMeasurement(10025, controller3.getRxTimeCounter().getCountLocked(0));
            long sum3 = 0;
            for (BatteryStats.LongCounter counter3 : controller3.getTxTimeCounters()) {
                sum3 += counter3.getCountLocked(0);
            }
            uidWriter.addMeasurement(10026, sum3);
            uidWriter.addMeasurement(10027, controller3.getPowerCounter().getCountLocked(0));
        }
        uidWriter.addMeasurement(10028, uid.getWifiRunningTime(this.mNowRealtimeMs * 1000, 0) / 1000);
        uidWriter.addMeasurement(10029, uid.getFullWifiLockTime(this.mNowRealtimeMs * 1000, 0) / 1000);
        uidWriter.addTimer(10030, uid.getWifiScanCount(0), uid.getWifiScanTime(this.mNowRealtimeMs * 1000, 0) / 1000);
        uidWriter.addMeasurement(10031, uid.getWifiMulticastTime(this.mNowRealtimeMs * 1000, 0) / 1000);
        addTimer(uidWriter, 10032, uid.getAudioTurnedOnTimer());
        addTimer(uidWriter, 10033, uid.getVideoTurnedOnTimer());
        addTimer(uidWriter, 10034, uid.getFlashlightTurnedOnTimer());
        addTimer(uidWriter, 10035, uid.getCameraTurnedOnTimer());
        addTimer(uidWriter, 10036, uid.getForegroundActivityTimer());
        addTimer(uidWriter, 10037, uid.getBluetoothScanTimer());
        addTimer(uidWriter, 10038, uid.getProcessStateTimer(0));
        addTimer(uidWriter, 10039, uid.getProcessStateTimer(1));
        addTimer(uidWriter, 10040, uid.getProcessStateTimer(4));
        addTimer(uidWriter, 10041, uid.getProcessStateTimer(2));
        addTimer(uidWriter, 10042, uid.getProcessStateTimer(3));
        addTimer(uidWriter, 10043, uid.getProcessStateTimer(6));
        addTimer(uidWriter, 10044, uid.getVibratorOnTimer());
        uidWriter.addMeasurement(10045, (long) uid.getUserActivityCount(0, 0));
        uidWriter.addMeasurement(10046, (long) uid.getUserActivityCount(1, 0));
        uidWriter.addMeasurement(10047, (long) uid.getUserActivityCount(2, 0));
        uidWriter.addMeasurement(10048, uid.getNetworkActivityBytes(0, 0));
        uidWriter.addMeasurement(10049, uid.getNetworkActivityBytes(1, 0));
        uidWriter.addMeasurement(10050, uid.getNetworkActivityBytes(2, 0));
        uidWriter.addMeasurement(10051, uid.getNetworkActivityBytes(3, 0));
        uidWriter.addMeasurement(10052, uid.getNetworkActivityBytes(4, 0));
        uidWriter.addMeasurement(10053, uid.getNetworkActivityBytes(5, 0));
        uidWriter.addMeasurement(10054, uid.getNetworkActivityPackets(0, 0));
        uidWriter.addMeasurement(10055, uid.getNetworkActivityPackets(1, 0));
        uidWriter.addMeasurement(10056, uid.getNetworkActivityPackets(2, 0));
        uidWriter.addMeasurement(10057, uid.getNetworkActivityPackets(3, 0));
        uidWriter.addMeasurement(10058, uid.getNetworkActivityPackets(4, 0));
        uidWriter.addMeasurement(10059, uid.getNetworkActivityPackets(5, 0));
        uidWriter.addTimer(10061, uid.getMobileRadioActiveCount(0), uid.getMobileRadioActiveTime(0));
        uidWriter.addMeasurement(10062, uid.getUserCpuTimeUs(0) / 1000);
        uidWriter.addMeasurement(10063, uid.getSystemCpuTimeUs(0) / 1000);
        uidWriter.addMeasurement(10064, 0);
    }

    public void writePid(HealthStatsWriter pidWriter, BatteryStats.Uid.Pid pid) {
        if (pid != null) {
            pidWriter.addMeasurement(20001, (long) pid.mWakeNesting);
            pidWriter.addMeasurement(20002, pid.mWakeSumMs);
            pidWriter.addMeasurement(20002, pid.mWakeStartMs);
        }
    }

    public void writeProc(HealthStatsWriter procWriter, BatteryStats.Uid.Proc proc) {
        procWriter.addMeasurement((int) EventLogTags.AM_FINISH_ACTIVITY, proc.getUserTime(0));
        procWriter.addMeasurement((int) EventLogTags.AM_TASK_TO_FRONT, proc.getSystemTime(0));
        procWriter.addMeasurement((int) EventLogTags.AM_NEW_INTENT, (long) proc.getStarts(0));
        procWriter.addMeasurement((int) EventLogTags.AM_CREATE_TASK, (long) proc.getNumCrashes(0));
        procWriter.addMeasurement((int) EventLogTags.AM_CREATE_ACTIVITY, (long) proc.getNumAnrs(0));
        procWriter.addMeasurement((int) EventLogTags.AM_RESTART_ACTIVITY, proc.getForegroundTime(0));
    }

    public void writePkg(HealthStatsWriter pkgWriter, BatteryStats.Uid.Pkg pkg) {
        for (Map.Entry<String, ? extends BatteryStats.Uid.Pkg.Serv> entry : pkg.getServiceStats().entrySet()) {
            HealthStatsWriter writer = new HealthStatsWriter(ServiceHealthStats.CONSTANTS);
            writeServ(writer, (BatteryStats.Uid.Pkg.Serv) entry.getValue());
            pkgWriter.addStats((int) EventLogTags.STREAM_DEVICES_CHANGED, entry.getKey(), writer);
        }
        for (Map.Entry<String, ? extends BatteryStats.Counter> entry2 : pkg.getWakeupAlarmStats().entrySet()) {
            BatteryStats.Counter counter = (BatteryStats.Counter) entry2.getValue();
            if (counter != null) {
                pkgWriter.addMeasurements(40002, entry2.getKey(), (long) counter.getCountLocked(0));
            }
        }
    }

    public void writeServ(HealthStatsWriter servWriter, BatteryStats.Uid.Pkg.Serv serv) {
        servWriter.addMeasurement(50001, (long) serv.getStarts(0));
        servWriter.addMeasurement(50002, (long) serv.getLaunches(0));
    }

    private void addTimer(HealthStatsWriter writer, int key, BatteryStats.Timer timer) {
        if (timer != null) {
            writer.addTimer(key, timer.getCountLocked(0), timer.getTotalTimeLocked(this.mNowRealtimeMs * 1000, 0) / 1000);
        }
    }

    private void addTimers(HealthStatsWriter writer, int key, String name, BatteryStats.Timer timer) {
        if (timer != null) {
            writer.addTimers(key, name, new TimerStat(timer.getCountLocked(0), timer.getTotalTimeLocked(this.mNowRealtimeMs * 1000, 0) / 1000));
        }
    }
}
