package ohos.bundle;

import java.util.ArrayList;
import java.util.List;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ApplicationInfo implements Sequenceable {
    private static final int MAX_LIMIT_SIZE = 1024;
    public static final Sequenceable.Producer<ApplicationInfo> PRODUCER = $$Lambda$ApplicationInfo$CuJaZGiCuIOjPg7WAZJrncFY0.INSTANCE;
    private String cpuAbi = "";
    public boolean debug = false;
    private String description = "";
    private int descriptionId = 0;
    public boolean enabled = true;
    private String icon = "";
    private int iconId = 0;
    private boolean isCompressNativeLibs = true;
    private String label = "";
    private int labelId = 0;
    private List<ModuleInfo> moduleInfos = new ArrayList();
    public String name = "";
    private List<String> permissions = new ArrayList();
    private String process = "";
    private int supportedModes = 0;
    public boolean systemApp = false;

    public ApplicationInfo() {
    }

    public ApplicationInfo(ApplicationInfo applicationInfo) {
        this.name = applicationInfo.name;
        this.icon = applicationInfo.icon;
        this.label = applicationInfo.label;
        this.description = applicationInfo.description;
        List<String> list = applicationInfo.permissions;
        if (list != null) {
            this.permissions.addAll(list);
        }
        this.process = applicationInfo.process;
        this.systemApp = applicationInfo.systemApp;
        List<ModuleInfo> list2 = applicationInfo.moduleInfos;
        if (list2 != null) {
            this.moduleInfos.addAll(list2);
        }
        this.supportedModes = applicationInfo.supportedModes;
        this.labelId = applicationInfo.labelId;
        this.iconId = applicationInfo.iconId;
        this.descriptionId = applicationInfo.descriptionId;
        this.cpuAbi = applicationInfo.cpuAbi;
        this.isCompressNativeLibs = applicationInfo.isCompressNativeLibs;
        this.enabled = applicationInfo.enabled;
        this.debug = applicationInfo.debug;
    }

    static /* synthetic */ ApplicationInfo lambda$static$0(Parcel parcel) {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.unmarshalling(parcel);
        return applicationInfo;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getLabel() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }

    public String getProcess() {
        return this.process;
    }

    public int getSupportedModes() {
        return this.supportedModes;
    }

    public List<String> getModuleSourceDirs() {
        ArrayList arrayList = new ArrayList();
        for (ModuleInfo moduleInfo : this.moduleInfos) {
            arrayList.add(moduleInfo.getModuleSourceDir());
        }
        return arrayList;
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public List<ModuleInfo> getModuleInfos() {
        return this.moduleInfos;
    }

    public boolean getSystemApp() {
        return this.systemApp;
    }

    public int getIconId() {
        return this.iconId;
    }

    public int getDescriptionId() {
        return this.descriptionId;
    }

    public int getLabelId() {
        return this.labelId;
    }

    public String getCpuAbi() {
        return this.cpuAbi;
    }

    public boolean isCompressNativeLibs() {
        return this.isCompressNativeLibs;
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x00ac  */
    public boolean marshalling(Parcel parcel) {
        if (!(parcel.writeString(this.name) && parcel.writeString(this.icon) && parcel.writeString(this.label) && parcel.writeString(this.description) && parcel.writeString(this.cpuAbi) && parcel.writeString(this.process) && parcel.writeBoolean(this.systemApp) && parcel.writeInt(this.supportedModes) && parcel.writeInt(this.iconId) && parcel.writeInt(this.descriptionId) && parcel.writeInt(this.labelId) && parcel.writeBoolean(this.isCompressNativeLibs) && parcel.writeInt(this.permissions.size()))) {
            return false;
        }
        for (String str : this.permissions) {
            if (!parcel.writeString(str)) {
                return false;
            }
        }
        if (!parcel.writeInt(this.moduleInfos.size())) {
            return false;
        }
        for (ModuleInfo moduleInfo : this.moduleInfos) {
            if (!parcel.writeString(moduleInfo.getModuleName()) || !parcel.writeString(moduleInfo.getModuleSourceDir())) {
                return false;
            }
            while (r0.hasNext()) {
            }
        }
        if (parcel.writeBoolean(this.enabled) && parcel.writeBoolean(this.debug)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.name = parcel.readString();
        this.icon = parcel.readString();
        this.label = parcel.readString();
        this.description = parcel.readString();
        this.cpuAbi = parcel.readString();
        this.process = parcel.readString();
        this.systemApp = parcel.readBoolean();
        this.supportedModes = parcel.readInt();
        this.iconId = parcel.readInt();
        this.descriptionId = parcel.readInt();
        this.labelId = parcel.readInt();
        this.isCompressNativeLibs = parcel.readBoolean();
        int readInt = parcel.readInt();
        if (readInt > 1024) {
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            this.permissions.add(parcel.readString());
        }
        int readInt2 = parcel.readInt();
        if (readInt2 > 1024) {
            return false;
        }
        for (int i2 = 0; i2 < readInt2; i2++) {
            this.moduleInfos.add(new ModuleInfo(parcel.readString(), parcel.readString()));
        }
        this.enabled = parcel.readBoolean();
        this.debug = parcel.readBoolean();
        return true;
    }

    public ApplicationInfo parseApplication(HapModuleInfo hapModuleInfo, String str) {
        if (hapModuleInfo != null) {
            this.name = str + hapModuleInfo.getName();
            this.description = hapModuleInfo.getDescription();
            this.icon = hapModuleInfo.getIconPath();
            this.label = hapModuleInfo.getLabel();
            this.supportedModes = hapModuleInfo.getSupportedModes();
        }
        return this;
    }
}
