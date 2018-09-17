package android.rms.iaware;

import android.os.SystemClock;
import android.rms.iaware.AwareConstant.ResourceType;

public final class DataContract {

    protected interface AppAttr {
        public static final String CALLED_APP = "calledApp";
        public static final String CALLER_APP = "callerApp";
    }

    protected interface AppProperty {
        public static final String ACTIVITY_NAME = "activityName";
        public static final String CRASH_FREQ = "crashFreq";
        public static final String CRASH_REASON = "crashReason";
        public static final String DISPLAYED_TIME = "displayedTime";
        public static final String EXIT_MODE = "exitMode";
        public static final String LAUNCH_MODE = "launchMode";
        public static final String LAUNCH_REASON = "launchReason";
        public static final String REQUEST_MEM = "requestMem";
    }

    public static class BaseBuilder {
        protected final DataNormalizer mCollectEvent = new DataNormalizer();
        protected final DataNormalizer mCollects = new DataNormalizer();

        public BaseBuilder addEvent(int event) {
            DataNormalizer normalizer = new DataNormalizer();
            normalizer.appendCondition("event", String.valueOf(event));
            this.mCollectEvent.appendCollect("event", normalizer.toString());
            return this;
        }

        public CollectData build(int resId) {
            this.mCollectEvent.appendCollect(this.mCollects.toString());
            return new CollectData(resId, SystemClock.uptimeMillis(), this.mCollectEvent.toString());
        }
    }

    public interface BaseAttr {
        public static final String EVENT = "event";
        public static final String INFO = "info";
        public static final String INTENT = "intent";
        public static final String TIMESTAMP = "timestamp";
    }

    public interface BaseProperty {
        public static final String EVENT = "event";
        public static final String PACKAGE_NAME = "packageName";
        public static final String PROCESS_ID = "pid";
        public static final String PROCESS_NAME = "processName";
        public static final String UID = "uid";
    }

    public static class Apps implements BaseAttr, AppAttr, BaseProperty, AppProperty {

        public static final class Builder extends BaseBuilder {
            public CollectData build() {
                return super.build(ResourceType.getReousrceId(ResourceType.RES_APP));
            }

            public Builder addCallerApp(String packageName, String processName, int pid, int uid) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition("packageName", packageName);
                normalizer.appendCondition(BaseProperty.PROCESS_NAME, processName);
                normalizer.appendCondition("pid", String.valueOf(pid));
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                this.mCollects.appendCollect(AppAttr.CALLER_APP, normalizer.toString());
                return this;
            }

            public Builder addCalledApp(String packageName, String processName, String activityName, int pid, int uid) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition("packageName", packageName);
                normalizer.appendCondition(BaseProperty.PROCESS_NAME, processName);
                normalizer.appendCondition(AppProperty.ACTIVITY_NAME, activityName);
                normalizer.appendCondition("pid", String.valueOf(pid));
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                this.mCollects.appendCollect(AppAttr.CALLED_APP, normalizer.toString());
                return this;
            }

            public Builder addRequestMemApp(int pid, int uid, int memkb) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition("pid", String.valueOf(pid));
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                normalizer.appendCondition(AppProperty.REQUEST_MEM, String.valueOf(memkb));
                this.mCollects.appendCollect(AppAttr.CALLED_APP, normalizer.toString());
                return this;
            }

            public Builder addLaunchCalledApp(String packageName, String processName, String launchMode, String launchReason, int pid, int uid) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition("packageName", packageName);
                normalizer.appendCondition(BaseProperty.PROCESS_NAME, processName);
                normalizer.appendCondition(AppProperty.LAUNCH_MODE, launchMode);
                normalizer.appendCondition(AppProperty.LAUNCH_REASON, launchReason);
                normalizer.appendCondition("pid", String.valueOf(pid));
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                this.mCollects.appendCollect(AppAttr.CALLED_APP, normalizer.toString());
                return this;
            }

            public Builder addExitCalledApp(String packageName, String processName, String exitMode, int pid, int uid) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition("packageName", packageName);
                normalizer.appendCondition(BaseProperty.PROCESS_NAME, processName);
                normalizer.appendCondition(AppProperty.EXIT_MODE, exitMode);
                normalizer.appendCondition("pid", String.valueOf(pid));
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                this.mCollects.appendCollect(AppAttr.CALLED_APP, normalizer.toString());
                return this;
            }

            public Builder addCrashCalledApp(String packageName, String processName, String crashReason, int crashFreq, int pid, int uid) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition("packageName", packageName);
                normalizer.appendCondition(BaseProperty.PROCESS_NAME, processName);
                normalizer.appendCondition(AppProperty.CRASH_REASON, crashReason);
                normalizer.appendCondition(AppProperty.CRASH_FREQ, String.valueOf(crashFreq));
                normalizer.appendCondition("pid", String.valueOf(pid));
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                this.mCollects.appendCollect(AppAttr.CALLED_APP, normalizer.toString());
                return this;
            }

            public Builder addActivityDisplayedInfo(String activityName, int pid, long displayedTime) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition(AppProperty.ACTIVITY_NAME, activityName);
                normalizer.appendCondition("pid", String.valueOf(pid));
                normalizer.appendCondition(AppProperty.DISPLAYED_TIME, String.valueOf(displayedTime));
                this.mCollects.appendCollect(AppAttr.CALLED_APP, normalizer.toString());
                return this;
            }
        }

        public static final class CrashReason {
            public static final String BIND_EXCEPT = "bind exception";
            public static final String RUNTIMEINIT_EXCEPT = "runtimeinit exception";
        }

        public static final class ExitMode {
            public static final String ANR = "anr";
            public static final String CLEAN_BG = "clean bg";
            public static final String CRASH = "crash";
            public static final String DIED = "died";
            public static final String FORCE_STOP = "force stop";
            public static final String INTALL_EXCEPT = "install exception";
            public static final String KILL = "kill";
            public static final String OOM_ADJ = "oomadj";
            public static final String REINSTALL = "reinstall";
            public static final String SYSTEM_MANAGER = "system manager";
            public static final String SYSTEM_READY = "system ready";
            public static final String TIMEOUT = "timeout";
        }

        public static final class LaunchMode {
            public static final String ACTIVITY = "activity";
            public static final String BACKUP = "backup";
            public static final String BINDFAIL = "bind fail";
            public static final String BROADCAST = "broadcast";
            public static final String HOLD = "hold";
            public static final String LINKFAIL = "link fail";
            public static final String ONCE = "once";
            public static final String PERSIST = "persist";
            public static final String PROVIDER = "provider";
            public static final String RESTART = "restart";
            public static final String SERVICE = "servivce";
        }

        private Apps() {
        }

        public static final Builder builder() {
            return new Builder();
        }
    }

    protected interface DevStatusAttr {
    }

    protected interface DevStatusProperty {
        public static final String ALARM_TAG = "alarmTag";
        public static final String BATTERY_PLUG_TYPE = "PlugType";
        public static final String BATTERY_STATE_CHARGING = "batterCharging";
        public static final String BATTERY_STATE_PLUGGED = "batterPlugged";
        public static final String DEVICEIDLE_ACTIVERESAON = "deviceIdleActiveReason";
        public static final String JOB_NAME = "jobName";
        public static final String NETWORK_STATE = "networkState";
        public static final String NETWORK_TYPE = "networkType";
        public static final String SCREEN_STATE = "screenState";
        public static final String SENSOR_ID = "sensorId";
        public static final String VIBRATOR_DURATION = "duration";
        public static final String WAKELOCK_TAG = "wakeLockTag";
        public static final String WAKELOCK_TYPE = "wakeLockType";
    }

    public static class DevStatus implements BaseAttr, DevStatusAttr, BaseProperty, DevStatusProperty {

        public static final class Builder extends BaseBuilder {
            public CollectData build() {
                return super.build(ResourceType.getReousrceId(ResourceType.RES_DEV_STATUS));
            }

            public Builder addUid(int uid) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                this.mCollects.appendCollect("info", normalizer.toString());
                return this;
            }

            public Builder addNetworkType(int networkType, String state) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition("networkType", String.valueOf(networkType));
                normalizer.appendCondition(DevStatusProperty.NETWORK_STATE, state);
                this.mCollects.appendCollect("info", normalizer.toString());
                return this;
            }

            public Builder addBatterystate(int plugType, boolean isBatteryPlugged, boolean isBatterCharging) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition(DevStatusProperty.BATTERY_PLUG_TYPE, String.valueOf(plugType));
                normalizer.appendCondition(DevStatusProperty.BATTERY_STATE_PLUGGED, isBatteryPlugged ? "1" : "0");
                normalizer.appendCondition(DevStatusProperty.BATTERY_STATE_CHARGING, isBatterCharging ? "1" : "0");
                this.mCollects.appendCollect("info", normalizer.toString());
                return this;
            }

            public Builder addDeviceIdle(int uid, String activeReason) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                normalizer.appendCondition(DevStatusProperty.DEVICEIDLE_ACTIVERESAON, activeReason);
                this.mCollects.appendCollect("info", normalizer.toString());
                return this;
            }

            public Builder addSensor(int uid, int sensorId) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                normalizer.appendCondition(DevStatusProperty.SENSOR_ID, String.valueOf(sensorId));
                this.mCollects.appendCollect("info", normalizer.toString());
                return this;
            }

            public Builder addVibrator(int uid, long durationMillis) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                normalizer.appendCondition("duration", String.valueOf(durationMillis));
                this.mCollects.appendCollect("info", normalizer.toString());
                return this;
            }

            public Builder addAlarm(int uid, String name) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                normalizer.appendCondition(DevStatusProperty.ALARM_TAG, name);
                this.mCollects.appendCollect("info", normalizer.toString());
                return this;
            }

            public Builder addWakeLock(int uid, int pid, String name, int wakeLockType) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                normalizer.appendCondition("pid", String.valueOf(pid));
                normalizer.appendCondition(DevStatusProperty.WAKELOCK_TAG, name);
                normalizer.appendCondition(DevStatusProperty.WAKELOCK_TYPE, String.valueOf(wakeLockType));
                this.mCollects.appendCollect("info", normalizer.toString());
                return this;
            }

            public Builder addJob(int uid, String name) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                normalizer.appendCondition(DevStatusProperty.JOB_NAME, name);
                this.mCollects.appendCollect("info", normalizer.toString());
                return this;
            }
        }

        private DevStatus() {
        }

        public static final Builder builder() {
            return new Builder();
        }
    }

    protected interface InputAttr {
    }

    protected interface InputProperty {
        public static final String ACTION = "action";
        public static final String KEY_CODE = "keyCode";
        public static final String SCAN_CODE = "scanCode";
    }

    public static class Input implements BaseAttr, InputAttr, BaseProperty, InputProperty {

        public static final class Builder extends BaseBuilder {
            public CollectData build() {
                return super.build(ResourceType.getReousrceId(ResourceType.RES_INPUT));
            }
        }

        private Input() {
        }

        public static final Builder builder() {
            return new Builder();
        }
    }
}
