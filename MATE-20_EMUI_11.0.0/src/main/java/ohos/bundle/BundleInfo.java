package ohos.bundle;

import java.util.ArrayList;
import java.util.List;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.fastjson.JSONArray;
import ohos.utils.fastjson.JSONObject;

public class BundleInfo implements Sequenceable {
    private static final int GET_BUNDLE_WITH_ABILITIES = 1;
    private static final String INVALID_VERSION = "-1";
    private static final int MAX_LIMIT_SIZE = 1024;
    public static final Sequenceable.Producer<BundleInfo> PRODUCER = $$Lambda$BundleInfo$xVR71dMQry20SoSA5q2p0al0wOc.INSTANCE;
    public List<AbilityInfo> abilityInfos = new ArrayList();
    public String appId = "";
    public ApplicationInfo appInfo = new ApplicationInfo();
    private String cpuAbi = "";
    public boolean debug = false;
    private boolean directLaunch = false;
    private String entryModuleName = "";
    private List<HapModuleInfo> hapModuleInfos = new ArrayList(0);
    private boolean isCompressNativeLibs = true;
    private boolean isSilentInstallation = false;
    private boolean keepAlive = false;
    private int maxSdkVersion = 0;
    private int minSdkVersion = 0;
    private List<ModuleInfo> moduleInfos = new ArrayList(0);
    public String name = "";
    public String originalName = "";
    private String process = "";
    public String sharedUserId = "";
    private boolean supportBackUp = false;
    private boolean systemApp = false;
    public int uid = -1;
    private String vendor = "";
    private int versionCode = 0;
    private String versionName = "";

    public BundleInfo() {
    }

    public BundleInfo(BundleInfo bundleInfo) {
        this.name = bundleInfo.name;
        this.originalName = bundleInfo.originalName;
        this.vendor = bundleInfo.vendor;
        this.versionCode = bundleInfo.versionCode;
        this.versionName = bundleInfo.versionName;
        this.minSdkVersion = bundleInfo.minSdkVersion;
        this.maxSdkVersion = bundleInfo.maxSdkVersion;
        this.process = bundleInfo.process;
        this.keepAlive = bundleInfo.keepAlive;
        this.directLaunch = bundleInfo.directLaunch;
        this.supportBackUp = bundleInfo.supportBackUp;
        this.isCompressNativeLibs = bundleInfo.isCompressNativeLibs;
        this.systemApp = bundleInfo.systemApp;
        this.cpuAbi = bundleInfo.cpuAbi;
        this.appId = bundleInfo.appId;
        this.uid = bundleInfo.uid;
        this.sharedUserId = bundleInfo.sharedUserId;
        this.appInfo = new ApplicationInfo(bundleInfo.appInfo);
        List<HapModuleInfo> list = bundleInfo.hapModuleInfos;
        if (list != null) {
            this.hapModuleInfos.addAll(list);
        }
        List<AbilityInfo> list2 = bundleInfo.abilityInfos;
        if (list2 != null) {
            this.abilityInfos.addAll(list2);
        }
        List<ModuleInfo> list3 = bundleInfo.moduleInfos;
        if (list3 != null) {
            this.moduleInfos.addAll(list3);
        }
        this.debug = bundleInfo.debug;
        this.entryModuleName = bundleInfo.entryModuleName;
        this.isSilentInstallation = bundleInfo.isSilentInstallation;
    }

    static /* synthetic */ BundleInfo lambda$static$0(Parcel parcel) {
        BundleInfo bundleInfo = new BundleInfo();
        bundleInfo.unmarshalling(parcel);
        return bundleInfo;
    }

    public String getName() {
        return this.name;
    }

    public String getOriginalName() {
        return this.originalName;
    }

    public String getVendor() {
        return this.vendor;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public int getMinSdkVersion() {
        return this.minSdkVersion;
    }

    public int getMaxSdkVersion() {
        return this.maxSdkVersion;
    }

    public ApplicationInfo getAppInfo() {
        return this.appInfo;
    }

    public List<AbilityInfo> getAbilityInfos() {
        return this.abilityInfos;
    }

    public AbilityInfo getAbilityInfoByName(String str) {
        for (AbilityInfo abilityInfo : this.abilityInfos) {
            if (abilityInfo.getClassName().equals(str)) {
                return abilityInfo;
            }
        }
        return null;
    }

    public AbilityInfo getAbilityInfoByOriginalName(String str) {
        for (AbilityInfo abilityInfo : this.abilityInfos) {
            if (abilityInfo.getOriginalClassName().equals(str)) {
                return abilityInfo;
            }
        }
        return null;
    }

    public String getAppId() {
        return this.appId;
    }

    public int getUid() {
        return this.uid;
    }

    public String getSharedUserId() {
        return this.sharedUserId;
    }

    public boolean isDifferentName() {
        return !this.originalName.isEmpty() && !this.name.equals(this.originalName);
    }

    public String getCpuAbi() {
        return this.cpuAbi;
    }

    public boolean getCompressNativeLibs() {
        return this.isCompressNativeLibs;
    }

    public String getEntryModuleName() {
        return this.entryModuleName;
    }

    /* JADX WARNING: Removed duplicated region for block: B:71:0x00fe  */
    public boolean marshalling(Parcel parcel) {
        if (!(parcel.writeString(this.name) && parcel.writeString(this.originalName) && parcel.writeString(this.vendor) && parcel.writeString(this.versionName) && parcel.writeInt(this.versionCode) && parcel.writeInt(this.minSdkVersion) && parcel.writeInt(this.maxSdkVersion) && parcel.writeString(this.process) && parcel.writeBoolean(this.keepAlive) && parcel.writeBoolean(this.directLaunch) && parcel.writeBoolean(this.supportBackUp) && parcel.writeBoolean(this.isCompressNativeLibs) && parcel.writeBoolean(this.systemApp) && parcel.writeString(this.cpuAbi) && parcel.writeString(this.appId) && parcel.writeInt(this.uid) && parcel.writeString(this.sharedUserId))) {
            return false;
        }
        parcel.writeSequenceable(this.appInfo);
        if (!parcel.writeInt(this.hapModuleInfos.size())) {
            return false;
        }
        for (HapModuleInfo hapModuleInfo : this.hapModuleInfos) {
            parcel.writeSequenceable(hapModuleInfo);
        }
        if (!parcel.writeInt(this.abilityInfos.size())) {
            return false;
        }
        for (AbilityInfo abilityInfo : this.abilityInfos) {
            parcel.writeSequenceable(abilityInfo);
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
        if (parcel.writeBoolean(this.debug) && parcel.writeString(this.entryModuleName) && parcel.writeBoolean(this.isSilentInstallation)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        int readInt;
        this.name = parcel.readString();
        this.originalName = parcel.readString();
        this.vendor = parcel.readString();
        this.versionName = parcel.readString();
        this.versionCode = parcel.readInt();
        this.minSdkVersion = parcel.readInt();
        this.maxSdkVersion = parcel.readInt();
        this.process = parcel.readString();
        this.keepAlive = parcel.readBoolean();
        this.directLaunch = parcel.readBoolean();
        this.supportBackUp = parcel.readBoolean();
        this.isCompressNativeLibs = parcel.readBoolean();
        this.systemApp = parcel.readBoolean();
        this.cpuAbi = parcel.readString();
        this.appId = parcel.readString();
        this.uid = parcel.readInt();
        this.sharedUserId = parcel.readString();
        if (!parcel.readSequenceable(this.appInfo) || (readInt = parcel.readInt()) > 1024) {
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            HapModuleInfo hapModuleInfo = new HapModuleInfo();
            if (!parcel.readSequenceable(hapModuleInfo)) {
                return false;
            }
            this.hapModuleInfos.add(hapModuleInfo);
        }
        int readInt2 = parcel.readInt();
        if (readInt2 > 1024) {
            return false;
        }
        for (int i2 = 0; i2 < readInt2; i2++) {
            AbilityInfo abilityInfo = new AbilityInfo();
            if (!parcel.readSequenceable(abilityInfo)) {
                return false;
            }
            this.abilityInfos.add(abilityInfo);
        }
        int readInt3 = parcel.readInt();
        if (readInt3 > 1024) {
            return false;
        }
        for (int i3 = 0; i3 < readInt3; i3++) {
            this.moduleInfos.add(new ModuleInfo(parcel.readString(), parcel.readString()));
        }
        this.debug = parcel.readBoolean();
        this.entryModuleName = parcel.readString();
        this.isSilentInstallation = parcel.readBoolean();
        return true;
    }

    public void parseBundle(String str, int i) {
        JSONObject parseObject = JSONObject.parseObject(str);
        if (parseObject != null && parseObject.containsKey(ProfileConstants.APP) && parseObject.containsKey(ProfileConstants.DEVICE_CONFIG) && parseObject.containsKey(ProfileConstants.MODULE)) {
            JSONObject jSONObject = parseObject.getJSONObject(ProfileConstants.APP);
            this.name = ProfileConstants.getJsonString(jSONObject, ProfileConstants.BUNDLE_NAME);
            this.vendor = ProfileConstants.getJsonString(jSONObject, ProfileConstants.VENDOR);
            JSONObject jSONObject2 = jSONObject.getJSONObject("version");
            this.versionName = ProfileConstants.getJsonString(jSONObject2, "name");
            this.versionCode = ProfileConstants.getJsonInt(jSONObject2, "code");
            JSONObject jSONObject3 = jSONObject.getJSONObject(ProfileConstants.API_VERSION);
            this.minSdkVersion = ProfileConstants.getJsonInt(jSONObject3, ProfileConstants.SDK_COMPATIBLE);
            this.maxSdkVersion = ProfileConstants.getJsonInt(jSONObject3, ProfileConstants.SDK_TARGET);
            JSONObject jSONObject4 = parseObject.getJSONObject(ProfileConstants.DEVICE_CONFIG);
            if (jSONObject4.containsKey("default")) {
                JSONObject jSONObject5 = jSONObject4.getJSONObject("default");
                this.sharedUserId = ProfileConstants.getJsonString(jSONObject5, ProfileConstants.JOINT_USER_ID);
                this.directLaunch = ProfileConstants.getJsonBoolean(jSONObject5, "directLaunch", false);
                this.keepAlive = ProfileConstants.getJsonBoolean(jSONObject5, "keepAlive", false);
                this.process = ProfileConstants.getJsonString(jSONObject5, "process");
                this.supportBackUp = ProfileConstants.getJsonBoolean(jSONObject5, "supportBackup", false);
            }
            JSONObject jSONObject6 = parseObject.getJSONObject(ProfileConstants.MODULE);
            HapModuleInfo parseHapModuleInfo = new HapModuleInfo().parseHapModuleInfo(jSONObject6);
            String packageNameForModule = parseHapModuleInfo.getPackageNameForModule(jSONObject6);
            if ((i & 1) == 1) {
                this.abilityInfos = parseAbility(jSONObject6, this.name, packageNameForModule, parseHapModuleInfo);
            }
            this.appInfo = new ApplicationInfo().parseApplication(parseHapModuleInfo, packageNameForModule);
            this.hapModuleInfos.add(parseHapModuleInfo);
        }
    }

    private List<AbilityInfo> parseAbility(JSONObject jSONObject, String str, String str2, HapModuleInfo hapModuleInfo) {
        ArrayList arrayList = new ArrayList();
        if (jSONObject.containsKey(ProfileConstants.ABILITIES)) {
            JSONArray jSONArray = jSONObject.getJSONArray(ProfileConstants.ABILITIES);
            int size = jSONArray.size();
            for (int i = 0; i < size; i++) {
                arrayList.add(new AbilityInfo().parseAbility(jSONArray.getJSONObject(i), str, str2, hapModuleInfo));
            }
        }
        return arrayList;
    }

    public String getModuleDir(String str) {
        for (ModuleInfo moduleInfo : this.moduleInfos) {
            if (moduleInfo.getModuleName().equals(str)) {
                return moduleInfo.getModuleSourceDir();
            }
        }
        return "";
    }

    public HapModuleInfo getHapModuleInfo(String str) {
        for (HapModuleInfo hapModuleInfo : this.hapModuleInfos) {
            if (hapModuleInfo.getModuleName().equals(str)) {
                return hapModuleInfo;
            }
        }
        return null;
    }
}
