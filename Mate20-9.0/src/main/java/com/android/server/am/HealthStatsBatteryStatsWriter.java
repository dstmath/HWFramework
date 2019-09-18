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
        HealthStatsWriter healthStatsWriter = uidWriter;
        BatteryStats batteryStats = bs;
        BatteryStats.Uid uid2 = uid;
        healthStatsWriter.addMeasurement(10001, batteryStats.computeBatteryRealtime(this.mNowRealtimeMs * 1000, 2) / 1000);
        healthStatsWriter.addMeasurement(10002, batteryStats.computeBatteryUptime(this.mNowUptimeMs * 1000, 2) / 1000);
        healthStatsWriter.addMeasurement(10003, batteryStats.computeBatteryScreenOffRealtime(this.mNowRealtimeMs * 1000, 2) / 1000);
        healthStatsWriter.addMeasurement(10004, batteryStats.computeBatteryScreenOffUptime(this.mNowUptimeMs * 1000, 2) / 1000);
        for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> entry : uid.getWakelockStats().entrySet()) {
            String key = entry.getKey();
            BatteryStats.Uid.Wakelock wakelock = (BatteryStats.Uid.Wakelock) entry.getValue();
            addTimers(healthStatsWriter, 10005, key, wakelock.getWakeTime(1));
            addTimers(healthStatsWriter, 10006, key, wakelock.getWakeTime(0));
            addTimers(healthStatsWriter, 10007, key, wakelock.getWakeTime(2));
            addTimers(healthStatsWriter, 10008, key, wakelock.getWakeTime(18));
        }
        for (Map.Entry<String, ? extends BatteryStats.Timer> entry2 : uid.getSyncStats().entrySet()) {
            addTimers(healthStatsWriter, 10009, entry2.getKey(), (BatteryStats.Timer) entry2.getValue());
        }
        for (Map.Entry<String, ? extends BatteryStats.Timer> entry3 : uid.getJobStats().entrySet()) {
            addTimers(healthStatsWriter, 10010, entry3.getKey(), (BatteryStats.Timer) entry3.getValue());
        }
        SparseArray<? extends BatteryStats.Uid.Sensor> sensors = uid.getSensorStats();
        int N = sensors.size();
        for (int i = 0; i < N; i++) {
            int sensorId = sensors.keyAt(i);
            if (sensorId == -10000) {
                addTimer(healthStatsWriter, 10011, ((BatteryStats.Uid.Sensor) sensors.valueAt(i)).getSensorTime());
            } else {
                addTimers(healthStatsWriter, 10012, Integer.toString(sensorId), ((BatteryStats.Uid.Sensor) sensors.valueAt(i)).getSensorTime());
            }
        }
        SparseArray<? extends BatteryStats.Uid.Pid> pids = uid.getPidStats();
        int N2 = pids.size();
        for (int i2 = 0; i2 < N2; i2++) {
            HealthStatsWriter writer = new HealthStatsWriter(PidHealthStats.CONSTANTS);
            writePid(writer, (BatteryStats.Uid.Pid) pids.valueAt(i2));
            healthStatsWriter.addStats(10013, Integer.toString(pids.keyAt(i2)), writer);
        }
        for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> entry4 : uid.getProcessStats().entrySet()) {
            HealthStatsWriter writer2 = new HealthStatsWriter(ProcessHealthStats.CONSTANTS);
            writeProc(writer2, (BatteryStats.Uid.Proc) entry4.getValue());
            healthStatsWriter.addStats(10014, entry4.getKey(), writer2);
        }
        for (Map.Entry<String, ? extends BatteryStats.Uid.Pkg> entry5 : uid.getPackageStats().entrySet()) {
            HealthStatsWriter writer3 = new HealthStatsWriter(PackageHealthStats.CONSTANTS);
            writePkg(writer3, (BatteryStats.Uid.Pkg) entry5.getValue());
            healthStatsWriter.addStats(10015, entry5.getKey(), writer3);
        }
        BatteryStats.ControllerActivityCounter controller = uid.getWifiControllerActivity();
        if (controller != null) {
            healthStatsWriter.addMeasurement(10016, controller.getIdleTimeCounter().getCountLocked(2));
            healthStatsWriter.addMeasurement(10017, controller.getRxTimeCounter().getCountLocked(2));
            long sum = 0;
            for (BatteryStats.LongCounter counter : controller.getTxTimeCounters()) {
                sum += counter.getCountLocked(2);
            }
            healthStatsWriter.addMeasurement(10018, sum);
            healthStatsWriter.addMeasurement(10019, controller.getPowerCounter().getCountLocked(2));
        }
        BatteryStats.ControllerActivityCounter controller2 = uid.getBluetoothControllerActivity();
        if (controller2 != null) {
            healthStatsWriter.addMeasurement(10020, controller2.getIdleTimeCounter().getCountLocked(2));
            healthStatsWriter.addMeasurement(10021, controller2.getRxTimeCounter().getCountLocked(2));
            long sum2 = 0;
            for (BatteryStats.LongCounter counter2 : controller2.getTxTimeCounters()) {
                sum2 += counter2.getCountLocked(2);
            }
            healthStatsWriter.addMeasurement(10022, sum2);
            healthStatsWriter.addMeasurement(10023, controller2.getPowerCounter().getCountLocked(2));
        }
        BatteryStats.ControllerActivityCounter controller3 = uid.getModemControllerActivity();
        if (controller3 != null) {
            healthStatsWriter.addMeasurement(10024, controller3.getIdleTimeCounter().getCountLocked(2));
            healthStatsWriter.addMeasurement(10025, controller3.getRxTimeCounter().getCountLocked(2));
            long sum3 = 0;
            for (BatteryStats.LongCounter counter3 : controller3.getTxTimeCounters()) {
                sum3 += counter3.getCountLocked(2);
            }
            healthStatsWriter.addMeasurement(10026, sum3);
            healthStatsWriter.addMeasurement(10027, controller3.getPowerCounter().getCountLocked(2));
        }
        healthStatsWriter.addMeasurement(10028, uid2.getWifiRunningTime(this.mNowRealtimeMs * 1000, 2) / 1000);
        healthStatsWriter.addMeasurement(10029, uid2.getFullWifiLockTime(this.mNowRealtimeMs * 1000, 2) / 1000);
        healthStatsWriter.addTimer(10030, uid2.getWifiScanCount(2), uid2.getWifiScanTime(this.mNowRealtimeMs * 1000, 2) / 1000);
        healthStatsWriter.addMeasurement(10031, uid2.getWifiMulticastTime(this.mNowRealtimeMs * 1000, 2) / 1000);
        addTimer(healthStatsWriter, 10032, uid.getAudioTurnedOnTimer());
        addTimer(healthStatsWriter, 10033, uid.getVideoTurnedOnTimer());
        addTimer(healthStatsWriter, 10034, uid.getFlashlightTurnedOnTimer());
        addTimer(healthStatsWriter, 10035, uid.getCameraTurnedOnTimer());
        addTimer(healthStatsWriter, 10036, uid.getForegroundActivityTimer());
        addTimer(healthStatsWriter, 10037, uid.getBluetoothScanTimer());
        addTimer(healthStatsWriter, 10038, uid2.getProcessStateTimer(0));
        addTimer(healthStatsWriter, 10039, uid2.getProcessStateTimer(1));
        addTimer(healthStatsWriter, 10040, uid2.getProcessStateTimer(4));
        addTimer(healthStatsWriter, 10041, uid2.getProcessStateTimer(2));
        addTimer(healthStatsWriter, 10042, uid2.getProcessStateTimer(3));
        addTimer(healthStatsWriter, 10043, uid2.getProcessStateTimer(6));
        addTimer(healthStatsWriter, 10044, uid.getVibratorOnTimer());
        healthStatsWriter.addMeasurement(10045, (long) uid2.getUserActivityCount(0, 2));
        healthStatsWriter.addMeasurement(10046, (long) uid2.getUserActivityCount(1, 2));
        healthStatsWriter.addMeasurement(10047, (long) uid2.getUserActivityCount(2, 2));
        healthStatsWriter.addMeasurement(10048, uid2.getNetworkActivityBytes(0, 2));
        healthStatsWriter.addMeasurement(10049, uid2.getNetworkActivityBytes(1, 2));
        healthStatsWriter.addMeasurement(10050, uid2.getNetworkActivityBytes(2, 2));
        healthStatsWriter.addMeasurement(10051, uid2.getNetworkActivityBytes(3, 2));
        healthStatsWriter.addMeasurement(10052, uid2.getNetworkActivityBytes(4, 2));
        healthStatsWriter.addMeasurement(10053, uid2.getNetworkActivityBytes(5, 2));
        healthStatsWriter.addMeasurement(10054, uid2.getNetworkActivityPackets(0, 2));
        healthStatsWriter.addMeasurement(10055, uid2.getNetworkActivityPackets(1, 2));
        healthStatsWriter.addMeasurement(10056, uid2.getNetworkActivityPackets(2, 2));
        healthStatsWriter.addMeasurement(10057, uid2.getNetworkActivityPackets(3, 2));
        healthStatsWriter.addMeasurement(10058, uid2.getNetworkActivityPackets(4, 2));
        healthStatsWriter.addMeasurement(10059, uid2.getNetworkActivityPackets(5, 2));
        healthStatsWriter.addTimer(10061, uid2.getMobileRadioActiveCount(2), uid2.getMobileRadioActiveTime(2));
        healthStatsWriter.addMeasurement(10062, uid2.getUserCpuTimeUs(2) / 1000);
        healthStatsWriter.addMeasurement(10063, uid2.getSystemCpuTimeUs(2) / 1000);
        healthStatsWriter.addMeasurement(10064, 0);
    }

    public void writePid(HealthStatsWriter pidWriter, BatteryStats.Uid.Pid pid) {
        if (pid != null) {
            pidWriter.addMeasurement(20001, (long) pid.mWakeNesting);
            pidWriter.addMeasurement(20002, pid.mWakeSumMs);
            pidWriter.addMeasurement(20002, pid.mWakeStartMs);
        }
    }

    public void writeProc(HealthStatsWriter procWriter, BatteryStats.Uid.Proc proc) {
        procWriter.addMeasurement(EventLogTags.AM_FINISH_ACTIVITY, proc.getUserTime(2));
        procWriter.addMeasurement(EventLogTags.AM_TASK_TO_FRONT, proc.getSystemTime(2));
        procWriter.addMeasurement(EventLogTags.AM_NEW_INTENT, (long) proc.getStarts(2));
        procWriter.addMeasurement(EventLogTags.AM_CREATE_TASK, (long) proc.getNumCrashes(2));
        procWriter.addMeasurement(EventLogTags.AM_CREATE_ACTIVITY, (long) proc.getNumAnrs(2));
        procWriter.addMeasurement(EventLogTags.AM_RESTART_ACTIVITY, proc.getForegroundTime(2));
    }

    public void writePkg(HealthStatsWriter pkgWriter, BatteryStats.Uid.Pkg pkg) {
        for (Map.Entry<String, ? extends BatteryStats.Uid.Pkg.Serv> entry : pkg.getServiceStats().entrySet()) {
            HealthStatsWriter writer = new HealthStatsWriter(ServiceHealthStats.CONSTANTS);
            writeServ(writer, (BatteryStats.Uid.Pkg.Serv) entry.getValue());
            pkgWriter.addStats(EventLogTags.STREAM_DEVICES_CHANGED, entry.getKey(), writer);
        }
        for (Map.Entry<String, ? extends BatteryStats.Counter> entry2 : pkg.getWakeupAlarmStats().entrySet()) {
            BatteryStats.Counter counter = (BatteryStats.Counter) entry2.getValue();
            if (counter != null) {
                pkgWriter.addMeasurements(40002, entry2.getKey(), (long) counter.getCountLocked(2));
            }
        }
    }

    public void writeServ(HealthStatsWriter servWriter, BatteryStats.Uid.Pkg.Serv serv) {
        servWriter.addMeasurement(50001, (long) serv.getStarts(2));
        servWriter.addMeasurement(50002, (long) serv.getLaunches(2));
    }

    private void addTimer(HealthStatsWriter writer, int key, BatteryStats.Timer timer) {
        if (timer != null) {
            writer.addTimer(key, timer.getCountLocked(2), timer.getTotalTimeLocked(this.mNowRealtimeMs * 1000, 2) / 1000);
        }
    }

    private void addTimers(HealthStatsWriter writer, int key, String name, BatteryStats.Timer timer) {
        if (timer != null) {
            writer.addTimers(key, name, new TimerStat(timer.getCountLocked(2), timer.getTotalTimeLocked(this.mNowRealtimeMs * 1000, 2) / 1000));
        }
    }
}
