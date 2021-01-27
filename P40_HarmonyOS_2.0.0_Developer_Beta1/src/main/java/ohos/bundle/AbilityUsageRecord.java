package ohos.bundle;

import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public class AbilityUsageRecord implements Sequenceable {
    public static final Sequenceable.Producer<AbilityUsageRecord> PRODUCER = $$Lambda$AbilityUsageRecord$64duQThYK1S2VAzf7HfKawGOmk.INSTANCE;
    private int appLabelId;
    private String bundleName = "";
    private int descriptionId;
    private int iconId;
    private boolean isDeleted;
    private int labelId;
    private long lastLaunchTime;
    private int launchedCount;
    private int moduleDescriptionId;
    private int moduleLabelId;
    private String moduleName = "";
    private String name;

    public int getLabelId() {
        return 0;
    }

    public String getName() {
        return "";
    }

    static /* synthetic */ AbilityUsageRecord lambda$static$0(Parcel parcel) {
        AbilityUsageRecord abilityUsageRecord = new AbilityUsageRecord();
        abilityUsageRecord.unmarshalling(parcel);
        return abilityUsageRecord;
    }

    public static Sequenceable.Producer<AbilityUsageRecord> getPRODUCER() {
        return PRODUCER;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public int getAppLabelId() {
        return this.appLabelId;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public int getModuleLabelId() {
        return this.moduleLabelId;
    }

    public int getModuleDescriptionId() {
        return this.descriptionId;
    }

    public int getDescriptionId() {
        return this.descriptionId;
    }

    public int getIconId() {
        return this.iconId;
    }

    public int getLaunchedCount() {
        return this.launchedCount;
    }

    public long getLastLaunchTime() {
        return this.lastLaunchTime;
    }

    public boolean isDeleted() {
        return this.isDeleted;
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel.writeString(this.bundleName) && parcel.writeInt(this.appLabelId) && parcel.writeString(this.moduleName) && parcel.writeInt(this.moduleLabelId) && parcel.writeInt(this.moduleDescriptionId) && parcel.writeString(this.name) && parcel.writeInt(this.labelId) && parcel.writeInt(this.descriptionId) && parcel.writeInt(this.iconId) && parcel.writeInt(this.launchedCount) && parcel.writeLong(this.lastLaunchTime) && parcel.writeBoolean(this.isDeleted)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.bundleName = parcel.readString();
        this.appLabelId = parcel.readInt();
        this.moduleName = parcel.readString();
        this.moduleLabelId = parcel.readInt();
        this.moduleDescriptionId = parcel.readInt();
        this.name = parcel.readString();
        this.labelId = parcel.readInt();
        this.descriptionId = parcel.readInt();
        this.iconId = parcel.readInt();
        this.launchedCount = parcel.readInt();
        this.lastLaunchTime = parcel.readLong();
        this.isDeleted = parcel.readBoolean();
        return true;
    }
}
