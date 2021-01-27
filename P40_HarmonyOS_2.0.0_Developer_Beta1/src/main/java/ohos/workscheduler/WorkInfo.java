package ohos.workscheduler;

import ohos.bundle.ElementName;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class WorkInfo implements Sequenceable {
    public static final int BATTERY_LEVEL_LOW = 0;
    public static final int BATTERY_LEVEL_LOW_OR_OKAY = 2;
    public static final int BATTERY_LEVEL_OKAY = 1;
    public static final int CHARGING_PLUGGED_AC = 1;
    public static final int CHARGING_PLUGGED_ANY = 0;
    public static final int CHARGING_PLUGGED_USB = 2;
    public static final int CHARGING_PLUGGED_WIRELESS = 3;
    private static final boolean DEFAULT_BOOLEAN_VALUE = false;
    private static final int DEFAULT_INT_VALUE = 0;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkInfo");
    public static final int MAX_BATTERY_LEVEL = 100;
    public static final int MAX_IDLE_WAIT_TIME_MS = 1200000;
    public static final int MAX_REPEAT_DELAY_TIME_MS = 1200000;
    public static final int MIN_BATTERY_LEVEL = 10;
    public static final int MIN_IDLE_WAIT_TIME_MS = 60000;
    public static final int MIN_REPEAT_COUNTER = 1;
    public static final int MIN_REPEAT_CYCLE_TIME_MS = 1200000;
    public static final int NETWORK_TYPE_ANY = 0;
    public static final int NETWORK_TYPE_BLUETOOTH = 3;
    public static final int NETWORK_TYPE_ETHERNET = 5;
    public static final int NETWORK_TYPE_MOBILE = 1;
    public static final int NETWORK_TYPE_WIFI = 2;
    public static final int NETWORK_TYPE_WIFI_P2P = 4;
    public static final int STORAGE_LEVEL_LOW = 0;
    public static final int STORAGE_LEVEL_LOW_OR_OKAY = 2;
    public static final int STORAGE_LEVEL_OKAY = 1;
    private static final int WORKID_BEGIN = 0;
    private String abilityName;
    private int batteryLevel;
    private int batteryStatus;
    private String bundleName;
    private int chargerType;
    private ElementName element;
    private int idleWaitTimes;
    private boolean isCharging;
    private boolean isDeepIdle;
    private boolean isPersisted;
    private boolean isRepeat;
    private int networkTypes;
    private PacMap pacMaps;
    private int repeatCounter;
    private long repeatCycleTime;
    private int storageRequest;
    private int workId;

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        boolean z = false;
        if (parcel == null) {
            return false;
        }
        parcel.writeSequenceable(this.element);
        if (((((((((((((((parcel.writeString(this.bundleName)) && parcel.writeString(this.abilityName)) && parcel.writeInt(this.workId)) && parcel.writeInt(this.networkTypes)) && parcel.writeBoolean(this.isDeepIdle)) && parcel.writeInt(this.idleWaitTimes)) && parcel.writeBoolean(this.isCharging)) && parcel.writeInt(this.chargerType)) && parcel.writeInt(this.batteryLevel)) && parcel.writeInt(this.batteryStatus)) && parcel.writeInt(this.storageRequest)) && parcel.writeBoolean(this.isRepeat)) && parcel.writeBoolean(this.isPersisted)) && parcel.writeLong(this.repeatCycleTime)) && parcel.writeInt(this.repeatCounter)) {
            z = true;
        }
        if (z) {
            parcel.writeSequenceable(this.pacMaps);
        }
        return z;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (this.element == null) {
            this.element = new ElementName();
        }
        if (!parcel.readSequenceable(this.element)) {
            HiLog.error(LOG_LABEL, "unmarshalling error!", new Object[0]);
            return false;
        }
        this.bundleName = parcel.readString();
        this.abilityName = parcel.readString();
        this.workId = parcel.readInt();
        this.networkTypes = parcel.readInt();
        this.isDeepIdle = parcel.readBoolean();
        this.idleWaitTimes = parcel.readInt();
        this.isCharging = parcel.readBoolean();
        this.chargerType = parcel.readInt();
        this.batteryLevel = parcel.readInt();
        this.batteryStatus = parcel.readInt();
        this.storageRequest = parcel.readInt();
        this.isRepeat = parcel.readBoolean();
        this.isPersisted = parcel.readBoolean();
        this.repeatCycleTime = parcel.readLong();
        this.repeatCounter = parcel.readInt();
        if (this.pacMaps == null) {
            this.pacMaps = new PacMap();
        }
        if (parcel.readSequenceable(this.pacMaps)) {
            return true;
        }
        this.pacMaps = null;
        return true;
    }

    public boolean isWorkInfoValid() {
        return this.element != null;
    }

    public ElementName getElementInfo() {
        return this.element;
    }

    public int getCurrentWorkID() {
        return this.workId;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public String getAbilityName() {
        return this.abilityName;
    }

    public boolean isRequestNetwork() {
        return this.networkTypes != 0;
    }

    public int getRequestNetworkType() {
        return this.networkTypes;
    }

    public boolean isRequestBattery() {
        return this.batteryStatus != 0;
    }

    public int getRequestBatteryStatus() {
        return this.batteryStatus;
    }

    public int getRequestBatteryLevel() {
        return this.batteryLevel;
    }

    public boolean isRequestCharging() {
        return this.isCharging;
    }

    public int getRequestChargeType() {
        return this.chargerType;
    }

    public boolean isRequestStorage() {
        int i = this.storageRequest;
        return (i == 0 || i == 4) ? false : true;
    }

    public int getRequestStorageType() {
        return this.storageRequest;
    }

    public boolean isRequestDeepIdle() {
        return this.isDeepIdle;
    }

    public int getRequestIdleWaitTime() {
        return this.idleWaitTimes;
    }

    public boolean isRequestPersisted() {
        return this.isPersisted;
    }

    public boolean isRequestRepeat() {
        return this.isRepeat;
    }

    public int getRepeatCounter() {
        return this.repeatCounter;
    }

    public long getRepeatCycleTime() {
        return this.repeatCycleTime;
    }

    public boolean isRequestDelay() {
        return this.repeatCounter == 1;
    }

    public PacMap getPacMaps() {
        return this.pacMaps;
    }

    private WorkInfo(Builder builder) {
        this.bundleName = builder.bundleName;
        this.abilityName = builder.abilityName;
        this.element = builder.element;
        this.workId = builder.workId;
        this.networkTypes = builder.networkTypes;
        this.isDeepIdle = builder.isDeepIdle;
        this.idleWaitTimes = builder.idleWaitTimes;
        this.isCharging = builder.isCharging;
        this.chargerType = builder.chargerType;
        this.batteryLevel = builder.batteryLevel;
        this.batteryStatus = builder.batteryStatus;
        this.storageRequest = builder.storageRequest;
        this.isRepeat = builder.isRepeat;
        this.isPersisted = builder.isPersisted;
        this.repeatCycleTime = builder.repeatCycleTime;
        this.repeatCounter = builder.repeatCounter;
        this.pacMaps = builder.pacMaps;
    }

    public static final class Builder {
        private String abilityName;
        private int batteryLevel = 0;
        private int batteryStatus = 0;
        private String bundleName;
        private int chargerType = 0;
        private ElementName element;
        private int idleWaitTimes = 0;
        private boolean isCharging = false;
        private boolean isDeepIdle = false;
        private boolean isPersisted = false;
        private boolean isRepeat = false;
        private int networkTypes = 0;
        private PacMap pacMaps;
        private int repeatCounter = 0;
        private long repeatCycleTime = 0;
        private int storageRequest = 0;
        private int workId = 0;

        public WorkInfo build() {
            return new WorkInfo(this);
        }

        public Builder setAbilityInfo(int i, String str, String str2) {
            if (str == null || str2 == null || str.isEmpty() || str2.isEmpty()) {
                throw new NullPointerException("WorkInfo: setAbilityInfo Error! bundleName or class invalid");
            } else if (i >= 0) {
                HiLog.info(WorkInfo.LOG_LABEL, "Create AbilityInfo: ID %{public}d, %{public}s.%{public}s !!", Integer.valueOf(i), str, str2);
                this.workId = i;
                this.element = new ElementName("", str, str2);
                this.bundleName = str;
                this.abilityName = str2;
                return this;
            } else {
                throw new IllegalArgumentException("WorkInfo: setWorkID Error! illegality workid");
            }
        }

        public Builder requestNetworkType(int i) {
            if (i < 0 || i > 5) {
                throw new IllegalArgumentException("WorkInfo: illegality network type, request failed!!");
            }
            this.networkTypes = (1 << i) | this.networkTypes;
            return this;
        }

        public Builder requestDeviceIdleType(boolean z, int i) {
            if (i < 60000 || i > 1200000) {
                throw new IllegalArgumentException("WorkInfo: illegality idle wait time, request failed!!");
            }
            this.idleWaitTimes = i;
            this.isDeepIdle = z;
            return this;
        }

        public Builder requestChargingType(boolean z, int i) {
            if (!z) {
                this.isCharging = false;
                this.chargerType = 0;
            } else if (i < 0 || i > 3) {
                this.isCharging = false;
                throw new IllegalArgumentException("WorkInfo: illegality charger type, request failed!!");
            } else {
                this.isCharging = true;
                this.chargerType = (1 << i) | this.chargerType;
            }
            return this;
        }

        public Builder requestBatteryStatus(int i) {
            if (i < 0 || i > 2) {
                throw new IllegalArgumentException("WorkInfo: illegality battery status, request failed!!");
            }
            this.batteryStatus = 1 << i;
            return this;
        }

        public Builder requestStorageStatus(int i) {
            if (i < 0 || i > 2) {
                throw new IllegalArgumentException("WorkInfo: illegality storage status, request failed!");
            }
            this.storageRequest = 1 << i;
            return this;
        }

        public Builder requestRepeatCycle(long j) {
            requestRepeatCycle(j, 0);
            return this;
        }

        public Builder requestRepeatCycle(long j, int i) {
            if (j < 1200000 || i < 0) {
                throw new IllegalArgumentException("WorkInfo: illegality cycle time, request failed!");
            }
            this.repeatCycleTime = j;
            this.repeatCounter = i;
            this.isRepeat = true;
            return this;
        }

        public Builder requestPersisted(boolean z) {
            this.isPersisted = z;
            return this;
        }

        public Builder setPacMaps(PacMap pacMap) {
            this.pacMaps = pacMap;
            return this;
        }
    }
}
