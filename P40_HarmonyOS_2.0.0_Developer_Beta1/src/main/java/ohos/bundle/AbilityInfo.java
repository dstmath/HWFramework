package ohos.bundle;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.annotation.SystemApi;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.event.notification.NotificationRequest;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.fastjson.JSONObject;

public class AbilityInfo implements Sequenceable {
    private static final String ABILITY_CA_SUBTYPE = "ability.subtype.ca";
    private static final Map<String, Integer> BACKGROUND_MODE_MAP = Collections.unmodifiableMap(new HashMap<String, Integer>() {
        /* class ohos.bundle.AbilityInfo.AnonymousClass1 */

        {
            put(AbilityInfo.KEY_DATA_TRANSFER, 1);
            put(AbilityInfo.KEY_AUDIO_PLAYBACK, 2);
            put(AbilityInfo.KEY_AUDIO_RECORDING, 2);
            put(AbilityInfo.KEY_PICTURE_IN_PICTURE, 2);
            put(AbilityInfo.KEY_VOIP, 4);
            put(AbilityInfo.KEY_LOCATION, 8);
            put(AbilityInfo.KEY_BLUETOOTH_INTERACTION, 16);
            put(AbilityInfo.KEY_WIFI_INTERACTION, 16);
            put(AbilityInfo.KEY_SCREEN_FETCH, 32);
        }
    });
    private static final String FORM_ENTITY_HOME_SCREEN = "homeScreen";
    private static final String FORM_ENTITY_SEARCHBOX = "searchbox";
    private static final String KEY_AUDIO_PLAYBACK = "audioPlayback";
    private static final String KEY_AUDIO_RECORDING = "audioRecording";
    private static final String KEY_BLUETOOTH_INTERACTION = "bluetoothInteraction";
    private static final String KEY_DATA_TRANSFER = "dataTransfer";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_PICTURE_IN_PICTURE = "pictureIntPicture";
    private static final String KEY_SCREEN_FETCH = "screenFetch";
    private static final String KEY_VOIP = "voip";
    private static final String KEY_WIFI_INTERACTION = "wifiInteraction";
    private static final int MAX_DEVICETYPE_SIZE = 50;
    private static final int MAX_LIMIT_SIZE = 1024;
    public static final Sequenceable.Producer<AbilityInfo> PRODUCER = $$Lambda$AbilityInfo$AHHFNCH9taRGuj92fOUlimhK1s.INSTANCE;
    private static final int VALUE_AUDIO_PLAYBACK = 2;
    private static final int VALUE_AUDIO_RECORDING = 2;
    private static final int VALUE_BLUETOOTH_INTERACTION = 16;
    private static final int VALUE_DATA_TRANSFER = 1;
    private static final int VALUE_HOME_SCREEN = 1;
    private static final int VALUE_LOCATION = 8;
    private static final int VALUE_PICTURE_IN_PICTURE = 2;
    private static final int VALUE_SCREEN_FETCH = 32;
    private static final int VALUE_SEARCHBOX = 2;
    private static final int VALUE_VOIP = 4;
    private static final int VALUE_WIFI_INTERACTION = 16;
    private String appName = "";
    private ApplicationInfo applicationInfo = new ApplicationInfo();
    private int backgroundModes = 0;
    public String bundleName = "";
    public String className = "";
    private int defaultFormHeight = 0;
    private int defaultFormWidth = 0;
    private String description = "";
    private int descriptionId = 0;
    private List<String> deviceCapabilities = new ArrayList();
    private String deviceId = "";
    private List<String> deviceTypes = new ArrayList();
    private boolean directLaunch = true;
    private String downloadUrl = "";
    public boolean enabled = true;
    private boolean formEnabled = false;
    private int formEntity = 0;
    private boolean grantPermission = false;
    private int iconId = 0;
    private String iconPath = "";
    private boolean isVisible = false;
    public String label = "";
    private int labelId = 0;
    private LaunchMode launchMode = LaunchMode.STANDARD;
    private int minFormHeight = 0;
    private int minFormWidth = 0;
    private String moduleName = "";
    private boolean multiUserShared = false;
    private String name = "";
    private DisplayOrientation orientation = DisplayOrientation.UNSPECIFIED;
    private String originalClassName = "";
    private String packageName = "";
    private int packageSize = 0;
    private List<String> permissions = new ArrayList();
    private String privacyName = "";
    private String privacyUrl = "";
    private String process = "";
    private String readPermission = "";
    private AbilitySubType subType = AbilitySubType.UNSPECIFIED;
    private boolean supportPipMode = false;
    private String targetAbility = "";
    private AbilityType type = AbilityType.UNKNOWN;
    private String uri = "";
    private String uriPermissionMode = "";
    private String uriPermissionPath = "";
    private String versionName = "";
    private String writePermission = "";

    public enum AbilitySubType {
        UNSPECIFIED,
        CA
    }

    public enum AbilityType {
        UNKNOWN,
        PAGE,
        SERVICE,
        DATA,
        WEB
    }

    public enum DisplayOrientation {
        UNSPECIFIED,
        LANDSCAPE,
        PORTRAIT,
        FOLLOWRECENT
    }

    public enum LaunchMode {
        SINGLETON,
        STANDARD
    }

    static /* synthetic */ AbilityInfo lambda$static$0(Parcel parcel) {
        AbilityInfo abilityInfo = new AbilityInfo();
        abilityInfo.unmarshalling(parcel);
        return abilityInfo;
    }

    public AbilityInfo() {
    }

    public AbilityInfo(AbilityInfo abilityInfo) {
        this.packageName = abilityInfo.packageName;
        this.name = abilityInfo.name;
        this.originalClassName = abilityInfo.originalClassName;
        this.label = abilityInfo.label;
        this.description = abilityInfo.description;
        this.iconPath = abilityInfo.iconPath;
        this.uri = abilityInfo.uri;
        this.moduleName = abilityInfo.moduleName;
        this.process = abilityInfo.process;
        this.targetAbility = abilityInfo.targetAbility;
        this.appName = abilityInfo.appName;
        this.privacyUrl = abilityInfo.privacyUrl;
        this.privacyName = abilityInfo.privacyName;
        this.downloadUrl = abilityInfo.downloadUrl;
        this.versionName = abilityInfo.versionName;
        this.backgroundModes = abilityInfo.backgroundModes;
        this.packageSize = abilityInfo.packageSize;
        this.isVisible = abilityInfo.isVisible;
        this.formEnabled = abilityInfo.formEnabled;
        this.multiUserShared = abilityInfo.multiUserShared;
        this.type = abilityInfo.type;
        this.subType = abilityInfo.subType;
        this.orientation = abilityInfo.orientation;
        this.launchMode = abilityInfo.launchMode;
        List<String> list = abilityInfo.permissions;
        if (list != null) {
            this.permissions.addAll(list);
        }
        List<String> list2 = abilityInfo.deviceTypes;
        if (list2 != null) {
            this.deviceTypes.addAll(list2);
        }
        List<String> list3 = abilityInfo.deviceCapabilities;
        if (list3 != null) {
            this.deviceCapabilities.addAll(list3);
        }
        this.supportPipMode = abilityInfo.supportPipMode;
        this.grantPermission = abilityInfo.grantPermission;
        this.readPermission = abilityInfo.readPermission;
        this.writePermission = abilityInfo.writePermission;
        this.uriPermissionMode = abilityInfo.uriPermissionMode;
        this.uriPermissionPath = abilityInfo.uriPermissionPath;
        this.directLaunch = abilityInfo.directLaunch;
        this.bundleName = abilityInfo.bundleName;
        this.className = abilityInfo.className;
        this.deviceId = abilityInfo.deviceId;
        this.applicationInfo = new ApplicationInfo(abilityInfo.applicationInfo);
        this.formEntity = abilityInfo.formEntity;
        this.minFormHeight = abilityInfo.minFormHeight;
        this.defaultFormHeight = abilityInfo.defaultFormHeight;
        this.minFormWidth = abilityInfo.minFormWidth;
        this.defaultFormWidth = abilityInfo.defaultFormWidth;
        this.labelId = abilityInfo.labelId;
        this.descriptionId = abilityInfo.descriptionId;
        this.iconId = abilityInfo.iconId;
        this.enabled = abilityInfo.enabled;
    }

    @Deprecated
    public String getPackageName() {
        return this.packageName;
    }

    @Deprecated
    public void setPackageName(String str) {
        this.packageName = str;
    }

    @Deprecated
    public String getName() {
        return this.name;
    }

    @Deprecated
    public void setName(String str) {
        this.name = str;
    }

    public String getLabel() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }

    public String getIconPath() {
        return this.iconPath;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public AbilityType getType() {
        return this.type;
    }

    public void setType(AbilityType abilityType) {
        this.type = abilityType;
    }

    public DisplayOrientation getOrientation() {
        return this.orientation;
    }

    public LaunchMode getLaunchMode() {
        return this.launchMode;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public void setBundleName(String str) {
        this.bundleName = str;
    }

    public String getClassName() {
        return this.className;
    }

    public String getOriginalClassName() {
        return this.originalClassName;
    }

    public void setClassName(String str) {
        this.className = str;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public List<String> getDeviceTypes() {
        return this.deviceTypes;
    }

    public void setDeviceTypes(List<String> list) {
        this.deviceTypes = list;
    }

    public List<String> getDeviceCapabilities() {
        return this.deviceCapabilities;
    }

    public String getURI() {
        return this.uri;
    }

    public boolean isDifferentName() {
        String str;
        String str2 = this.originalClassName;
        return str2 != null && !str2.isEmpty() && (str = this.name) != null && !str.equals(this.originalClassName);
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public String getProcess() {
        return this.process;
    }

    public String getTargetAbility() {
        return this.targetAbility;
    }

    public boolean getFormEnabled() {
        return this.formEnabled;
    }

    public int getBackgroundModes() {
        return this.backgroundModes;
    }

    public ApplicationInfo getApplicationInfo() {
        return this.applicationInfo;
    }

    public int getFormEntity() {
        return this.formEntity;
    }

    public int getMinFormHeight() {
        return this.minFormHeight;
    }

    public int getDefaultFormHeight() {
        return this.defaultFormHeight;
    }

    public int getMinFormWidth() {
        return this.minFormWidth;
    }

    public int getDefaultFormWidth() {
        return this.defaultFormWidth;
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

    @SystemApi
    public AbilitySubType getSubType() {
        return this.subType;
    }

    public String getReadPermission() {
        return this.readPermission;
    }

    public String getWritePermission() {
        return this.writePermission;
    }

    public boolean marshalling(Parcel parcel) {
        if (!(parcel.writeString(this.packageName) && parcel.writeString(this.name) && parcel.writeString(this.originalClassName) && parcel.writeString(this.label) && parcel.writeString(this.description) && parcel.writeString(this.iconPath) && parcel.writeString(this.uri) && parcel.writeString(this.moduleName) && parcel.writeString(this.process) && parcel.writeString(this.targetAbility) && parcel.writeString(this.appName) && parcel.writeString(this.privacyUrl) && parcel.writeString(this.privacyName) && parcel.writeString(this.downloadUrl) && parcel.writeString(this.versionName) && parcel.writeInt(this.backgroundModes) && parcel.writeInt(this.packageSize) && parcel.writeBoolean(this.isVisible) && parcel.writeBoolean(this.formEnabled) && parcel.writeBoolean(this.multiUserShared) && parcel.writeInt(this.type.ordinal()) && parcel.writeInt(this.orientation.ordinal()) && parcel.writeInt(this.launchMode.ordinal()) && parcel.writeInt(this.permissions.size()))) {
            return false;
        }
        for (String str : this.permissions) {
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
        if (this.subType == AbilitySubType.CA && !this.deviceCapabilities.contains(ABILITY_CA_SUBTYPE)) {
            this.deviceCapabilities.add(ABILITY_CA_SUBTYPE);
        }
        if (!parcel.writeInt(this.deviceCapabilities.size())) {
            return false;
        }
        for (String str3 : this.deviceCapabilities) {
            if (!parcel.writeString(str3)) {
                return false;
            }
        }
        if (!(parcel.writeBoolean(this.supportPipMode) && parcel.writeBoolean(this.grantPermission) && parcel.writeString(this.readPermission) && parcel.writeString(this.writePermission) && parcel.writeString(this.uriPermissionMode) && parcel.writeString(this.uriPermissionPath) && parcel.writeBoolean(this.directLaunch) && parcel.writeString(this.bundleName) && parcel.writeString(this.className) && parcel.writeString(this.deviceId))) {
            return false;
        }
        parcel.writeSequenceable(this.applicationInfo);
        if (parcel.writeInt(this.formEntity) && parcel.writeInt(this.minFormHeight) && parcel.writeInt(this.defaultFormHeight) && parcel.writeInt(this.minFormWidth) && parcel.writeInt(this.defaultFormWidth) && parcel.writeInt(this.iconId) && parcel.writeInt(this.descriptionId) && parcel.writeInt(this.labelId) && parcel.writeBoolean(this.enabled)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.packageName = parcel.readString();
        this.name = parcel.readString();
        this.originalClassName = parcel.readString();
        this.label = parcel.readString();
        this.description = parcel.readString();
        this.iconPath = parcel.readString();
        this.uri = parcel.readString();
        this.moduleName = parcel.readString();
        this.process = parcel.readString();
        this.targetAbility = parcel.readString();
        this.appName = parcel.readString();
        this.privacyUrl = parcel.readString();
        this.privacyName = parcel.readString();
        this.downloadUrl = parcel.readString();
        this.versionName = parcel.readString();
        this.backgroundModes = parcel.readInt();
        this.packageSize = parcel.readInt();
        this.isVisible = parcel.readBoolean();
        this.formEnabled = parcel.readBoolean();
        this.multiUserShared = parcel.readBoolean();
        this.type = AbilityType.values()[parcel.readInt()];
        this.orientation = DisplayOrientation.values()[parcel.readInt()];
        this.launchMode = LaunchMode.values()[parcel.readInt()];
        int readInt = parcel.readInt();
        if (readInt > 1024) {
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            this.permissions.add(parcel.readString());
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
            this.deviceCapabilities.add(parcel.readString());
        }
        this.supportPipMode = parcel.readBoolean();
        this.grantPermission = parcel.readBoolean();
        this.readPermission = parcel.readString();
        this.writePermission = parcel.readString();
        this.uriPermissionMode = parcel.readString();
        this.uriPermissionPath = parcel.readString();
        this.directLaunch = parcel.readBoolean();
        this.bundleName = parcel.readString();
        this.className = parcel.readString();
        this.deviceId = parcel.readString();
        if (!parcel.readSequenceable(this.applicationInfo)) {
            return false;
        }
        this.formEntity = parcel.readInt();
        this.minFormHeight = parcel.readInt();
        this.defaultFormHeight = parcel.readInt();
        this.minFormWidth = parcel.readInt();
        this.defaultFormWidth = parcel.readInt();
        this.iconId = parcel.readInt();
        this.descriptionId = parcel.readInt();
        this.labelId = parcel.readInt();
        this.enabled = parcel.readBoolean();
        if (this.deviceCapabilities.contains(ABILITY_CA_SUBTYPE)) {
            this.subType = AbilitySubType.CA;
            return true;
        }
        this.subType = AbilitySubType.UNSPECIFIED;
        return true;
    }

    public AbilityInfo parseAbility(JSONObject jSONObject, String str, String str2, HapModuleInfo hapModuleInfo) {
        this.name = ProfileConstants.getJsonString(jSONObject, "name");
        this.bundleName = str;
        this.description = ProfileConstants.getJsonString(jSONObject, ProfileConstants.DESCRIPTION);
        this.label = ProfileConstants.getJsonString(jSONObject, ProfileConstants.LABEL);
        this.iconPath = ProfileConstants.getJsonString(jSONObject, ProfileConstants.ICON);
        this.uri = ProfileConstants.getJsonString(jSONObject, Constants.ELEMNAME_URL_STRING);
        this.targetAbility = ProfileConstants.getJsonString(jSONObject, "targetAbility");
        this.appName = ProfileConstants.getJsonString(jSONObject, "appName");
        this.privacyUrl = ProfileConstants.getJsonString(jSONObject, "privacyUrl");
        this.privacyName = ProfileConstants.getJsonString(jSONObject, "privacyName");
        this.downloadUrl = ProfileConstants.getJsonString(jSONObject, "downloadUrl");
        this.versionName = ProfileConstants.getJsonString(jSONObject, "versionName");
        this.directLaunch = ProfileConstants.getJsonBoolean(jSONObject, "directLaunch", false);
        this.multiUserShared = ProfileConstants.getJsonBoolean(jSONObject, "multiUserShared", false);
        this.supportPipMode = ProfileConstants.getJsonBoolean(jSONObject, "supportPipMode", false);
        this.isVisible = ProfileConstants.getJsonBoolean(jSONObject, "visible", false);
        parseEnum(jSONObject);
        if (jSONObject.containsKey("backgroundModes")) {
            for (String str3 : JSONObject.parseArray(ProfileConstants.getJsonString(jSONObject, "backgroundModes"), String.class)) {
                if (BACKGROUND_MODE_MAP.containsKey(str3)) {
                    this.backgroundModes = BACKGROUND_MODE_MAP.get(str3).intValue() | this.backgroundModes;
                }
            }
        }
        if (jSONObject.containsKey("permissions")) {
            this.permissions = JSONObject.parseArray(ProfileConstants.getJsonString(jSONObject, "permissions"), String.class);
        }
        if (hapModuleInfo != null) {
            this.deviceTypes = hapModuleInfo.getDeviceTypes();
            this.deviceCapabilities = hapModuleInfo.getReqCapabilities();
            this.moduleName = hapModuleInfo.getModuleName();
        }
        this.className = str2 + this.name;
        parseForm(jSONObject);
        parsePermissions(jSONObject);
        return this;
    }

    private void parseEnum(JSONObject jSONObject) {
        String jsonString = ProfileConstants.getJsonString(jSONObject, "type");
        if ("page".equals(jsonString)) {
            this.type = AbilityType.PAGE;
        } else if ("provider".equals(jsonString)) {
            this.type = AbilityType.DATA;
        } else if (NotificationRequest.CLASSIFICATION_SERVICE.equals(jsonString)) {
            this.type = AbilityType.SERVICE;
        } else {
            this.type = AbilityType.UNKNOWN;
        }
        if ("standard".equals(ProfileConstants.getJsonString(jSONObject, "launchType"))) {
            this.launchMode = LaunchMode.STANDARD;
        } else {
            this.launchMode = LaunchMode.SINGLETON;
        }
        String jsonString2 = ProfileConstants.getJsonString(jSONObject, "orientation");
        if ("landscape".equals(jsonString2)) {
            this.orientation = DisplayOrientation.LANDSCAPE;
        } else if ("portrait".equals(jsonString2)) {
            this.orientation = DisplayOrientation.PORTRAIT;
        } else {
            this.orientation = DisplayOrientation.UNSPECIFIED;
        }
    }

    private void parsePermissions(JSONObject jSONObject) {
        if (jSONObject.containsKey("permissions")) {
            this.permissions = JSONObject.parseArray(ProfileConstants.getJsonString(jSONObject, "permissions"), String.class);
        }
        if (this.type == AbilityType.DATA) {
            this.grantPermission = ProfileConstants.getJsonBoolean(jSONObject, "grantPermission", false);
            this.readPermission = ProfileConstants.getJsonString(jSONObject, "readPermission");
            this.writePermission = ProfileConstants.getJsonString(jSONObject, "writePermission");
            if (jSONObject.containsKey("uriPermission")) {
                JSONObject jSONObject2 = jSONObject.getJSONObject("uriPermission");
                this.uriPermissionMode = ProfileConstants.getJsonString(jSONObject2, Constants.ATTRNAME_MODE);
                this.uriPermissionPath = ProfileConstants.getJsonString(jSONObject2, "path");
            }
        }
    }

    private void parseForm(JSONObject jSONObject) {
        this.formEnabled = ProfileConstants.getJsonBoolean(jSONObject, "formEnabled", false);
        if (this.formEnabled && jSONObject.containsKey("form")) {
            JSONObject jSONObject2 = jSONObject.getJSONObject("form");
            if (jSONObject2.containsKey("formEntity")) {
                List parseArray = JSONObject.parseArray(ProfileConstants.getJsonString(jSONObject2, "formEntity"), String.class);
                if (parseArray.contains(FORM_ENTITY_HOME_SCREEN)) {
                    this.formEntity++;
                }
                if (parseArray.contains(FORM_ENTITY_SEARCHBOX)) {
                    this.formEntity += 2;
                }
            }
            this.minFormWidth = ProfileConstants.getJsonInt(jSONObject, "minWidth");
            this.defaultFormWidth = ProfileConstants.getJsonInt(jSONObject, "defaultWidth");
            this.minFormHeight = ProfileConstants.getJsonInt(jSONObject, "minHeight");
            this.defaultFormHeight = ProfileConstants.getJsonInt(jSONObject, "defaultHeight");
        }
    }

    public void dump(String str, PrintWriter printWriter) throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException("prefix is null, dump failed");
        } else if (printWriter != null) {
            StringBuilder sb = new StringBuilder();
            dumpCommonAttributes(str, sb);
            dumpPageAbilityAttributes(str, sb);
            dumpFormAttributes(str, sb);
            dumpPermissions(str, sb);
            printWriter.println(sb.toString());
        } else {
            throw new IllegalArgumentException("writer is null, dump failed");
        }
    }

    private void dumpCommonAttributes(String str, StringBuilder sb) {
        sb.append(str);
        sb.append("bundleName=");
        sb.append(this.bundleName);
        sb.append(System.lineSeparator());
        sb.append(str);
        sb.append("name=");
        sb.append(this.name);
        sb.append(System.lineSeparator());
        sb.append(str);
        sb.append("label=");
        sb.append(this.label);
        sb.append(System.lineSeparator());
        sb.append(str);
        sb.append("description=");
        sb.append(this.description);
        sb.append(System.lineSeparator());
        sb.append(str);
        sb.append("abilityType=");
        sb.append(this.type);
        sb.append(System.lineSeparator());
        sb.append(str);
        sb.append("visible=");
        sb.append(this.isVisible);
        sb.append(System.lineSeparator());
        sb.append(str);
        sb.append("launchMode=");
        sb.append(this.launchMode);
        sb.append(System.lineSeparator());
    }

    private void dumpFormAttributes(String str, StringBuilder sb) {
        if (this.formEnabled) {
            sb.append(str);
            sb.append("formEnabled=");
            sb.append(this.formEnabled);
            sb.append(System.lineSeparator());
            sb.append(str);
            sb.append("formEntity=");
            sb.append(this.formEntity);
            sb.append(System.lineSeparator());
            sb.append(str);
            sb.append("minFormWidth=");
            sb.append(this.minFormWidth);
            sb.append(System.lineSeparator());
            sb.append(str);
            sb.append("minFormHeight=");
            sb.append(this.minFormHeight);
            sb.append(System.lineSeparator());
            sb.append(str);
            sb.append("defaultFormWidth=");
            sb.append(this.defaultFormWidth);
            sb.append(System.lineSeparator());
            sb.append(str);
            sb.append("defaultFormHeight=");
            sb.append(this.defaultFormHeight);
            sb.append(System.lineSeparator());
        }
    }

    private void dumpPageAbilityAttributes(String str, StringBuilder sb) {
        if (this.type == AbilityType.PAGE) {
            sb.append(str);
            sb.append("orientation=");
            sb.append(this.orientation);
            sb.append(System.lineSeparator());
            sb.append(str);
            sb.append("targetAbility=");
            sb.append(this.targetAbility);
            sb.append(System.lineSeparator());
            sb.append(str);
            sb.append("supportPipMode=");
            sb.append(this.supportPipMode);
            sb.append(System.lineSeparator());
        }
    }

    private void dumpPermissions(String str, StringBuilder sb) {
        List<String> list = this.permissions;
        if (list != null && !list.isEmpty()) {
            sb.append(str);
            sb.append("permissions=");
            sb.append(this.permissions);
            sb.append(System.lineSeparator());
        }
    }
}
