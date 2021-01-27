package ohos.bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class FormInfo implements Sequenceable {
    private static final int MAX_LIMIT_SIZE = 4;
    public static final Sequenceable.Producer<FormInfo> PRODUCER = $$Lambda$FormInfo$6f4gc11tik8WEY3YufPM4Vr75Wk.INSTANCE;
    private String abilityName = "";
    private String bundleName = "";
    private String colorMode = "auto";
    private Map<String, String> customizeDatas = new HashMap();
    private String deepLink = "";
    private int defaultDimension = 1;
    private boolean defaultFlag = false;
    private String description = "";
    private String jsComponentName = "";
    private List<String> landscapeLayouts = new ArrayList();
    private String moduleName = "";
    private String name = "";
    private List<String> portraitLayouts = new ArrayList();
    private String scheduledUpdateTime = "00:00";
    private List<Integer> supportDimensions = new ArrayList();
    private FormType type = FormType.JAVA;
    private int updateDuration = 1;
    private boolean updateEnabled = false;

    public enum FormType {
        JAVA,
        JS
    }

    static /* synthetic */ FormInfo lambda$static$0(Parcel parcel) {
        FormInfo formInfo = new FormInfo();
        formInfo.unmarshalling(parcel);
        return formInfo;
    }

    public class CustomizeData {
        String name;
        String value;

        public CustomizeData() {
        }
    }

    public FormInfo() {
    }

    public FormInfo(FormInfo formInfo) {
        this.bundleName = formInfo.bundleName;
        this.moduleName = formInfo.moduleName;
        this.abilityName = formInfo.abilityName;
        this.name = formInfo.name;
        this.description = formInfo.description;
        this.type = formInfo.type;
        this.colorMode = formInfo.colorMode;
        this.defaultFlag = formInfo.defaultFlag;
        this.updateEnabled = formInfo.updateEnabled;
        this.updateDuration = formInfo.updateDuration;
        this.scheduledUpdateTime = formInfo.scheduledUpdateTime;
        this.deepLink = formInfo.deepLink;
        this.defaultDimension = formInfo.defaultDimension;
        this.supportDimensions = formInfo.supportDimensions;
        this.customizeDatas = formInfo.customizeDatas;
        if (this.type == FormType.JAVA) {
            this.landscapeLayouts = formInfo.landscapeLayouts;
            this.portraitLayouts = formInfo.portraitLayouts;
            return;
        }
        this.jsComponentName = formInfo.jsComponentName;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public String getAbilityName() {
        return this.abilityName;
    }

    public String getFormName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public FormType getType() {
        return this.type;
    }

    public String getColorMode() {
        return this.colorMode;
    }

    public String getJsComponentName() {
        return this.jsComponentName;
    }

    public String getDeepLink() {
        return this.deepLink;
    }

    public boolean isDefaultForm() {
        return this.defaultFlag;
    }

    public int getDefaultDimension() {
        return this.defaultDimension;
    }

    public List<Integer> getSupportDimensions() {
        return this.supportDimensions;
    }

    public Map<String, String> getCustomizeDatas() {
        return this.customizeDatas;
    }

    public boolean marshalling(Parcel parcel) {
        if (!(marshallingFirst(parcel) && parcel.writeInt(this.customizeDatas.size()))) {
            return false;
        }
        for (Map.Entry<String, String> entry : this.customizeDatas.entrySet()) {
            parcel.writeString(entry.getKey());
            parcel.writeString(entry.getValue());
        }
        if (this.type != FormType.JAVA) {
            return parcel.writeString(this.jsComponentName);
        }
        if (writeInfoToParcel(this.landscapeLayouts, parcel) && writeInfoToParcel(this.portraitLayouts, parcel)) {
            return true;
        }
        return false;
    }

    private boolean marshallingFirst(Parcel parcel) {
        if (!(parcel.writeString(this.bundleName) && parcel.writeString(this.moduleName) && parcel.writeString(this.abilityName) && parcel.writeString(this.name) && parcel.writeString(this.description) && parcel.writeString(this.type.toString()) && parcel.writeString(this.colorMode) && parcel.writeBoolean(this.defaultFlag) && parcel.writeBoolean(this.updateEnabled) && parcel.writeInt(this.updateDuration) && parcel.writeString(this.scheduledUpdateTime) && parcel.writeString(this.deepLink) && parcel.writeInt(this.defaultDimension) && parcel.writeInt(this.supportDimensions.size()))) {
            return false;
        }
        for (Integer num : this.supportDimensions) {
            parcel.writeInt(num.intValue());
        }
        return true;
    }

    private boolean writeInfoToParcel(List<String> list, Parcel parcel) {
        if (!parcel.writeInt(list.size())) {
            return false;
        }
        for (String str : list) {
            parcel.writeString(str);
        }
        return true;
    }

    private boolean readInfoToList(Parcel parcel, List<String> list) {
        int readInt = parcel.readInt();
        if (readInt > 4) {
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            list.add(parcel.readString());
        }
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.bundleName = parcel.readString();
        this.moduleName = parcel.readString();
        this.abilityName = parcel.readString();
        this.name = parcel.readString();
        this.description = parcel.readString();
        if ("JS".equalsIgnoreCase(parcel.readString())) {
            this.type = FormType.JS;
        }
        this.colorMode = parcel.readString();
        this.defaultFlag = parcel.readBoolean();
        this.updateEnabled = parcel.readBoolean();
        this.updateDuration = parcel.readInt();
        this.scheduledUpdateTime = parcel.readString();
        this.deepLink = parcel.readString();
        this.defaultDimension = parcel.readInt();
        int readInt = parcel.readInt();
        if (readInt > 4) {
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            this.supportDimensions.add(Integer.valueOf(parcel.readInt()));
        }
        int readInt2 = parcel.readInt();
        for (int i2 = 0; i2 < readInt2; i2++) {
            this.customizeDatas.put(parcel.readString(), parcel.readString());
        }
        if (this.type != FormType.JAVA) {
            this.jsComponentName = parcel.readString();
            return true;
        } else if (readInfoToList(parcel, this.landscapeLayouts) && readInfoToList(parcel, this.portraitLayouts)) {
            return true;
        } else {
            return false;
        }
    }
}
