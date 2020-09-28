package android.rms.iaware;

import android.os.SystemClock;
import android.rms.iaware.AwareConstant;

public final class DataContract {

    protected interface AppAttr {
        public static final String CALLED_APP = "calledApp";
    }

    protected interface AppProperty {
        public static final String ACTIVITY_NAME = "activityName";
        public static final String DISPLAYED_TIME = "displayedTime";
        public static final String EXIT_MODE = "exitMode";
        public static final String LAUNCH_MODE = "launchMode";
        public static final String LAUNCH_REASON = "launchReason";
        public static final String REQUEST_MEM = "requestMem";
    }

    public interface BaseAttr {
        public static final String EVENT = "event";
    }

    public interface BaseProperty {
        public static final String EVENT = "event";
        public static final String PACKAGE_NAME = "packageName";
        public static final String PROCESS_ID = "pid";
        public static final String PROCESS_NAME = "processName";
        public static final String UID = "uid";
    }

    protected interface InputAttr {
    }

    protected interface InputProperty {
        public static final String ACTION = "action";
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

    public static class Apps implements BaseAttr, AppAttr, BaseProperty, AppProperty {

        public static final class ExitMode {
            public static final String ANR = "anr";
            public static final String CRASH = "crash";
            public static final String DIED = "died";
            public static final String KILL = "kill";
        }

        public static final class LaunchMode {
            public static final String ACTIVITY = "activity";
            public static final String BROADCAST = "broadcast";
            public static final String PROVIDER = "provider";
            public static final String RESTART = "restart";
            public static final String SERVICE = "servivce";
        }

        public static final Builder builder() {
            return new Builder();
        }

        public static final class Builder extends BaseBuilder {
            private DataNormalizer mNormalizer = new DataNormalizer();

            public CollectData build() {
                return super.build(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RES_APP));
            }

            public Builder addCalledApp(String packageName, String processName, String activityName, int pid, int uid) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition(BaseProperty.PACKAGE_NAME, packageName);
                normalizer.appendCondition("processName", processName);
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
                normalizer.appendCondition(BaseProperty.PACKAGE_NAME, packageName);
                normalizer.appendCondition("processName", processName);
                normalizer.appendCondition(AppProperty.LAUNCH_MODE, launchMode);
                normalizer.appendCondition(AppProperty.LAUNCH_REASON, launchReason);
                normalizer.appendCondition("pid", String.valueOf(pid));
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                this.mCollects.appendCollect(AppAttr.CALLED_APP, normalizer.toString());
                return this;
            }

            public Builder addActivityDisplayedInfoWithUid(String activityName, int uid, int pid, long displayedTime) {
                DataNormalizer normalizer = new DataNormalizer();
                normalizer.appendCondition(AppProperty.ACTIVITY_NAME, activityName);
                normalizer.appendCondition(BaseProperty.UID, String.valueOf(uid));
                normalizer.appendCondition("pid", String.valueOf(pid));
                normalizer.appendCondition(AppProperty.DISPLAYED_TIME, String.valueOf(displayedTime));
                this.mCollects.appendCollect(AppAttr.CALLED_APP, normalizer.toString());
                return this;
            }
        }
    }

    public static class Input implements BaseAttr, InputAttr, BaseProperty, InputProperty {
        public static final Builder builder() {
            return new Builder();
        }

        public static final class Builder extends BaseBuilder {
            public CollectData build() {
                return super.build(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RES_INPUT));
            }
        }
    }
}
