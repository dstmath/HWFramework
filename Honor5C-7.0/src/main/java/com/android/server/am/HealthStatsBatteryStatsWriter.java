package com.android.server.am;

import android.os.BatteryStats;
import android.os.BatteryStats.ControllerActivityCounter;
import android.os.BatteryStats.Counter;
import android.os.BatteryStats.LongCounter;
import android.os.BatteryStats.Timer;
import android.os.BatteryStats.Uid;
import android.os.BatteryStats.Uid.Pid;
import android.os.BatteryStats.Uid.Pkg;
import android.os.BatteryStats.Uid.Pkg.Serv;
import android.os.BatteryStats.Uid.Proc;
import android.os.BatteryStats.Uid.Sensor;
import android.os.BatteryStats.Uid.Wakelock;
import android.os.SystemClock;
import android.os.health.HealthStatsWriter;
import android.os.health.PackageHealthStats;
import android.os.health.PidHealthStats;
import android.os.health.ProcessHealthStats;
import android.os.health.ServiceHealthStats;
import android.os.health.TimerStat;
import android.util.SparseArray;
import com.android.server.EventLogTags;
import java.util.Map.Entry;

public class HealthStatsBatteryStatsWriter {
    private final long mNowRealtimeMs;
    private final long mNowUptimeMs;

    public HealthStatsBatteryStatsWriter() {
        this.mNowRealtimeMs = SystemClock.elapsedRealtime();
        this.mNowUptimeMs = SystemClock.uptimeMillis();
    }

    public void writeUid(HealthStatsWriter uidWriter, BatteryStats bs, Uid uid) {
        int i;
        long sum;
        uidWriter.addMeasurement(10001, bs.computeBatteryRealtime(this.mNowRealtimeMs * 1000, 2) / 1000);
        uidWriter.addMeasurement(10002, bs.computeBatteryUptime(this.mNowUptimeMs * 1000, 2) / 1000);
        uidWriter.addMeasurement(10003, bs.computeBatteryScreenOffRealtime(this.mNowRealtimeMs * 1000, 2) / 1000);
        uidWriter.addMeasurement(10004, bs.computeBatteryScreenOffUptime(this.mNowUptimeMs * 1000, 2) / 1000);
        for (Entry<String, ? extends Wakelock> entry : uid.getWakelockStats().entrySet()) {
            String key = (String) entry.getKey();
            Wakelock wakelock = (Wakelock) entry.getValue();
            addTimers(uidWriter, 10005, key, wakelock.getWakeTime(1));
            addTimers(uidWriter, 10006, key, wakelock.getWakeTime(0));
            addTimers(uidWriter, 10007, key, wakelock.getWakeTime(2));
            addTimers(uidWriter, 10008, key, wakelock.getWakeTime(18));
        }
        for (Entry<String, ? extends Timer> entry2 : uid.getSyncStats().entrySet()) {
            addTimers(uidWriter, 10009, (String) entry2.getKey(), (Timer) entry2.getValue());
        }
        for (Entry<String, ? extends Timer> entry22 : uid.getJobStats().entrySet()) {
            addTimers(uidWriter, 10010, (String) entry22.getKey(), (Timer) entry22.getValue());
        }
        SparseArray<? extends Sensor> sensors = uid.getSensorStats();
        int N = sensors.size();
        for (i = 0; i < N; i++) {
            int sensorId = sensors.keyAt(i);
            if (sensorId == -10000) {
                addTimer(uidWriter, 10011, ((Sensor) sensors.valueAt(i)).getSensorTime());
            } else {
                addTimers(uidWriter, 10012, Integer.toString(sensorId), ((Sensor) sensors.valueAt(i)).getSensorTime());
            }
        }
        SparseArray<? extends Pid> pids = uid.getPidStats();
        N = pids.size();
        for (i = 0; i < N; i++) {
            HealthStatsWriter writer = new HealthStatsWriter(PidHealthStats.CONSTANTS);
            writePid(writer, (Pid) pids.valueAt(i));
            uidWriter.addStats(10013, Integer.toString(pids.keyAt(i)), writer);
        }
        for (Entry<String, ? extends Proc> entry3 : uid.getProcessStats().entrySet()) {
            writer = new HealthStatsWriter(ProcessHealthStats.CONSTANTS);
            writeProc(writer, (Proc) entry3.getValue());
            uidWriter.addStats(10014, (String) entry3.getKey(), writer);
        }
        for (Entry<String, ? extends Pkg> entry4 : uid.getPackageStats().entrySet()) {
            writer = new HealthStatsWriter(PackageHealthStats.CONSTANTS);
            writePkg(writer, (Pkg) entry4.getValue());
            uidWriter.addStats(10015, (String) entry4.getKey(), writer);
        }
        ControllerActivityCounter controller = uid.getWifiControllerActivity();
        if (controller != null) {
            uidWriter.addMeasurement(10016, controller.getIdleTimeCounter().getCountLocked(2));
            uidWriter.addMeasurement(10017, controller.getRxTimeCounter().getCountLocked(2));
            sum = 0;
            for (LongCounter counter : controller.getTxTimeCounters()) {
                sum += counter.getCountLocked(2);
            }
            uidWriter.addMeasurement(10018, sum);
            uidWriter.addMeasurement(10019, controller.getPowerCounter().getCountLocked(2));
        }
        controller = uid.getBluetoothControllerActivity();
        if (controller != null) {
            uidWriter.addMeasurement(10020, controller.getIdleTimeCounter().getCountLocked(2));
            uidWriter.addMeasurement(10021, controller.getRxTimeCounter().getCountLocked(2));
            sum = 0;
            for (LongCounter counter2 : controller.getTxTimeCounters()) {
                sum += counter2.getCountLocked(2);
            }
            uidWriter.addMeasurement(10022, sum);
            uidWriter.addMeasurement(10023, controller.getPowerCounter().getCountLocked(2));
        }
        controller = uid.getModemControllerActivity();
        if (controller != null) {
            uidWriter.addMeasurement(10024, controller.getIdleTimeCounter().getCountLocked(2));
            uidWriter.addMeasurement(10025, controller.getRxTimeCounter().getCountLocked(2));
            sum = 0;
            for (LongCounter counter22 : controller.getTxTimeCounters()) {
                sum += counter22.getCountLocked(2);
            }
            uidWriter.addMeasurement(10026, sum);
            uidWriter.addMeasurement(10027, controller.getPowerCounter().getCountLocked(2));
        }
        uidWriter.addMeasurement(10028, uid.getWifiRunningTime(this.mNowRealtimeMs * 1000, 2) / 1000);
        uidWriter.addMeasurement(10029, uid.getFullWifiLockTime(this.mNowRealtimeMs * 1000, 2) / 1000);
        uidWriter.addTimer(10030, uid.getWifiScanCount(2), uid.getWifiScanTime(this.mNowRealtimeMs * 1000, 2) / 1000);
        uidWriter.addMeasurement(10031, uid.getWifiMulticastTime(this.mNowRealtimeMs * 1000, 2) / 1000);
        addTimer(uidWriter, 10032, uid.getAudioTurnedOnTimer());
        addTimer(uidWriter, 10033, uid.getVideoTurnedOnTimer());
        addTimer(uidWriter, 10034, uid.getFlashlightTurnedOnTimer());
        addTimer(uidWriter, 10035, uid.getCameraTurnedOnTimer());
        addTimer(uidWriter, 10036, uid.getForegroundActivityTimer());
        addTimer(uidWriter, 10037, uid.getBluetoothScanTimer());
        addTimer(uidWriter, 10038, uid.getProcessStateTimer(0));
        addTimer(uidWriter, 10039, uid.getProcessStateTimer(1));
        addTimer(uidWriter, 10040, uid.getProcessStateTimer(2));
        addTimer(uidWriter, 10041, uid.getProcessStateTimer(3));
        addTimer(uidWriter, 10042, uid.getProcessStateTimer(4));
        addTimer(uidWriter, 10043, uid.getProcessStateTimer(5));
        addTimer(uidWriter, 10044, uid.getVibratorOnTimer());
        uidWriter.addMeasurement(10045, (long) uid.getUserActivityCount(0, 2));
        uidWriter.addMeasurement(10046, (long) uid.getUserActivityCount(1, 2));
        uidWriter.addMeasurement(10047, (long) uid.getUserActivityCount(2, 2));
        uidWriter.addMeasurement(10048, uid.getNetworkActivityBytes(0, 2));
        uidWriter.addMeasurement(10049, uid.getNetworkActivityBytes(1, 2));
        uidWriter.addMeasurement(10050, uid.getNetworkActivityBytes(2, 2));
        uidWriter.addMeasurement(10051, uid.getNetworkActivityBytes(3, 2));
        uidWriter.addMeasurement(10052, uid.getNetworkActivityBytes(4, 2));
        uidWriter.addMeasurement(10053, uid.getNetworkActivityBytes(5, 2));
        uidWriter.addMeasurement(10054, uid.getNetworkActivityPackets(0, 2));
        uidWriter.addMeasurement(10055, uid.getNetworkActivityPackets(1, 2));
        uidWriter.addMeasurement(10056, uid.getNetworkActivityPackets(2, 2));
        uidWriter.addMeasurement(10057, uid.getNetworkActivityPackets(3, 2));
        uidWriter.addMeasurement(10058, uid.getNetworkActivityPackets(4, 2));
        uidWriter.addMeasurement(10059, uid.getNetworkActivityPackets(5, 2));
        uidWriter.addTimer(10061, uid.getMobileRadioActiveCount(2), uid.getMobileRadioActiveTime(2));
        uidWriter.addMeasurement(10062, uid.getUserCpuTimeUs(2) / 1000);
        uidWriter.addMeasurement(10063, uid.getSystemCpuTimeUs(2) / 1000);
        uidWriter.addMeasurement(10064, uid.getCpuPowerMaUs(2) / 1000);
    }

    public void writePid(HealthStatsWriter pidWriter, Pid pid) {
        if (pid != null) {
            pidWriter.addMeasurement(20001, (long) pid.mWakeNesting);
            pidWriter.addMeasurement(20002, pid.mWakeSumMs);
            pidWriter.addMeasurement(20002, pid.mWakeStartMs);
        }
    }

    public void writeProc(HealthStatsWriter procWriter, Proc proc) {
        procWriter.addMeasurement(EventLogTags.AM_FINISH_ACTIVITY, proc.getUserTime(2));
        procWriter.addMeasurement(EventLogTags.AM_TASK_TO_FRONT, proc.getSystemTime(2));
        procWriter.addMeasurement(EventLogTags.AM_NEW_INTENT, (long) proc.getStarts(2));
        procWriter.addMeasurement(EventLogTags.AM_CREATE_TASK, (long) proc.getNumCrashes(2));
        procWriter.addMeasurement(EventLogTags.AM_CREATE_ACTIVITY, (long) proc.getNumAnrs(2));
        procWriter.addMeasurement(EventLogTags.AM_RESTART_ACTIVITY, proc.getForegroundTime(2));
    }

    public void writePkg(HealthStatsWriter pkgWriter, Pkg pkg) {
        for (Entry<String, ? extends Serv> entry : pkg.getServiceStats().entrySet()) {
            HealthStatsWriter writer = new HealthStatsWriter(ServiceHealthStats.CONSTANTS);
            writeServ(writer, (Serv) entry.getValue());
            pkgWriter.addStats(EventLogTags.STREAM_DEVICES_CHANGED, (String) entry.getKey(), writer);
        }
        for (Entry<String, ? extends Counter> entry2 : pkg.getWakeupAlarmStats().entrySet()) {
            Counter counter = (Counter) entry2.getValue();
            if (counter != null) {
                pkgWriter.addMeasurements(40002, (String) entry2.getKey(), (long) counter.getCountLocked(2));
            }
        }
    }

    public void writeServ(HealthStatsWriter servWriter, Serv serv) {
        servWriter.addMeasurement(50001, (long) serv.getStarts(2));
        servWriter.addMeasurement(50002, (long) serv.getLaunches(2));
    }

    private void addTimer(HealthStatsWriter writer, int key, Timer timer) {
        if (timer != null) {
            writer.addTimer(key, timer.getCountLocked(2), timer.getTotalTimeLocked(this.mNowRealtimeMs * 1000, 2) / 1000);
        }
    }

    private void addTimers(HealthStatsWriter writer, int key, String name, Timer timer) {
        if (timer != null) {
            writer.addTimers(key, name, new TimerStat(timer.getCountLocked(2), timer.getTotalTimeLocked(this.mNowRealtimeMs * 1000, 2) / 1000));
        }
    }
}
