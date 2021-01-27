package ohos.bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.fastjson.JSONArray;
import ohos.utils.fastjson.JSONObject;

public class HapModuleInfo implements Sequenceable {
    private static final String DRIVE_MODE = "drive";
    private static final int MAX_DEVICETYPE_SIZE = 50;
    private static final int MAX_LIMIT_SIZE = 1024;
    public static final Sequenceable.Producer<HapModuleInfo> PRODUCER = $$Lambda$HapModuleInfo$LeUbJfPlG7M0kKQgX29UNvGEChc.INSTANCE;
    private List<AbilityInfo> abilityInfos = new ArrayList();
    private boolean allowClassMap = false;
    private String backgroundImg = "";
    private int backgroundImgId = 0;
    private String description = "";
    private int descriptionId = 0;
    private List<String> deviceTypes = new ArrayList();
    private int iconId = 0;
    private String iconPath = "";
    private String label = "";
    private int labelId = 0;
    private String moduleName = "";
    private String name = "";
    private List<String> reqCapabilities = new ArrayList();
    private int supportedModes = 0;

    static /* synthetic */ HapModuleInfo lambda$static$0(Parcel parcel) {
        HapModuleInfo hapModuleInfo = new HapModuleInfo();
        hapModuleInfo.unmarshalling(parcel);
        return hapModuleInfo;
    }

    public HapModuleInfo() {
    }

    public HapModuleInfo(HapModuleInfo hapModuleInfo) {
        this.name = hapModuleInfo.name;
        this.description = hapModuleInfo.description;
        this.descriptionId = hapModuleInfo.descriptionId;
        this.iconPath = hapModuleInfo.iconPath;
        this.iconId = hapModuleInfo.iconId;
        this.label = hapModuleInfo.label;
        this.labelId = hapModuleInfo.labelId;
        this.backgroundImg = hapModuleInfo.backgroundImg;
        this.backgroundImgId = hapModuleInfo.backgroundImgId;
        this.supportedModes = hapModuleInfo.supportedModes;
        this.reqCapabilities = hapModuleInfo.reqCapabilities;
        this.deviceTypes = hapModuleInfo.deviceTypes;
        this.abilityInfos = hapModuleInfo.abilityInfos;
        this.moduleName = hapModuleInfo.moduleName;
        this.allowClassMap = hapModuleInfo.allowClassMap;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public int getDescriptionId() {
        return this.descriptionId;
    }

    public String getIconPath() {
        return this.iconPath;
    }

    public int getIconId() {
        return this.iconId;
    }

    public String getLabel() {
        return this.label;
    }

    public int getLabelId() {
        return this.labelId;
    }

    public String getBackgroundImg() {
        return this.backgroundImg;
    }

    public int getBackgroundImgId() {
        return this.backgroundImgId;
    }

    public int getSupportedModes() {
        return this.supportedModes;
    }

    public List<String> getReqCapabilities() {
        return this.reqCapabilities;
    }

    public List<String> getDeviceTypes() {
        return this.deviceTypes;
    }

    public List<AbilityInfo> getAbilityInfos() {
        return this.abilityInfos;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public boolean marshalling(Parcel parcel) {
        if (!(parcel.writeString(this.name) && parcel.writeString(this.description) && parcel.writeInt(this.descriptionId) && parcel.writeString(this.iconPath) && parcel.writeInt(this.iconId) && parcel.writeString(this.label) && parcel.writeInt(this.labelId) && parcel.writeString(this.backgroundImg) && parcel.writeInt(this.backgroundImgId) && parcel.writeInt(this.supportedModes) && parcel.writeInt(this.reqCapabilities.size()))) {
            return false;
        }
        for (String str : this.reqCapabilities) {
            if (!parcel.writeString(str)) {
                return false;
            }
        }
        if (!parcel.writeInt(this.deviceTypes.size())) {
            return false;
        }
        for (String str2 : this.deviceTypes) {
            if (!parcel.writeString(str2)) {
                return false;
            }
        }
        if (!parcel.writeInt(this.abilityInfos.size())) {
            return false;
        }
        for (AbilityInfo abilityInfo : this.abilityInfos) {
            parcel.writeSequenceable(abilityInfo);
        }
        if (parcel.writeString(this.moduleName) && parcel.writeBoolean(this.allowClassMap)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.name = parcel.readString();
        this.description = parcel.readString();
        this.descriptionId = parcel.readInt();
        this.iconPath = parcel.readString();
        this.iconId = parcel.readInt();
        this.label = parcel.readString();
        this.labelId = parcel.readInt();
        this.backgroundImg = parcel.readString();
        this.backgroundImgId = parcel.readInt();
        this.supportedModes = parcel.readInt();
        int readInt = parcel.readInt();
        if (readInt > 1024) {
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            this.reqCapabilities.add(parcel.readString());
        }
        int readInt2 = parcel.readInt();
        if (readInt2 > 50) {
            return false;
        }
        for (int i2 = 0; i2 < readInt2; i2++) {
            this.deviceTypes.add(parcel.readString());
        }
        int readInt3 = parcel.readInt();
        if (readInt3 > 1024) {
            return false;
        }
        for (int i3 = 0; i3 < readInt3; i3++) {
            AbilityInfo abilityInfo = new AbilityInfo();
            if (!parcel.readSequenceable(abilityInfo)) {
                return false;
            }
            this.abilityInfos.add(abilityInfo);
        }
        this.moduleName = parcel.readString();
        this.allowClassMap = parcel.readBoolean();
        return true;
    }

    public HapModuleInfo parseHapModuleInfo(JSONObject jSONObject) {
        this.name = ProfileConstants.getJsonString(jSONObject, "name");
        this.description = ProfileConstants.getJsonString(jSONObject, ProfileConstants.DESCRIPTION);
        this.iconPath = ProfileConstants.getJsonString(jSONObject, "iconPath");
        this.label = ProfileConstants.getJsonString(jSONObject, ProfileConstants.LABEL);
        if (jSONObject.containsKey("distro")) {
            this.moduleName = ProfileConstants.getJsonString(jSONObject.getJSONObject("distro"), "moduleName");
        }
        if (jSONObject.containsKey("supportedModes")) {
            Iterator it = JSONObject.parseArray(ProfileConstants.getJsonString(jSONObject, "supportedModes"), String.class).iterator();
            while (true) {
                if (it.hasNext()) {
                    if (DRIVE_MODE.equals((String) it.next())) {
                        this.supportedModes = 1;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        if (jSONObject.containsKey("reqCapabilities")) {
            this.reqCapabilities = JSONArray.parseArray(ProfileConstants.getJsonString(jSONObject, "reqCapabilities"), String.class);
        }
        if (jSONObject.containsKey("deviceType")) {
            this.deviceTypes = JSONArray.parseArray(ProfileConstants.getJsonString(jSONObject, "deviceType"), String.class);
            for (String str : this.deviceTypes) {
                if ("phone".equals(str)) {
                    Collections.replaceAll(this.deviceTypes, "phone", "default");
                }
            }
        }
        return this;
    }

    public String getPackageNameForModule(JSONObject jSONObject) {
        return jSONObject.containsKey("package") ? ProfileConstants.getJsonString(jSONObject, "package") : "";
    }

    public boolean isAllowClassMap() {
        return this.allowClassMap;
    }
}
