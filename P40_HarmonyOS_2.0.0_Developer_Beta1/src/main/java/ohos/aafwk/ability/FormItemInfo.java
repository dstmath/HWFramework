package ohos.aafwk.ability;

import java.util.HashMap;

public class FormItemInfo {
    private String abilityModuleName;
    private String abilityName;
    private String bundleName;
    private boolean eSystemFlag;
    int eSystemPreviewLayoutId;
    private int formId;
    private String formName;
    private String[] hapSourceDirs;
    private boolean isJsForm;
    private String jsComponentName;
    private String layoutIdConfig;
    private HashMap<String, String> moduleInfoMap;
    private String moduleName;
    int previewLayoutId;
    private String scheduledUpdateTime;
    private int specificationId;
    private boolean updageFlag;
    private int updateDuration;

    public int getFormId() {
        return this.formId;
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
        return this.formName;
    }

    public String getJsComponentName() {
        return this.jsComponentName;
    }

    public String getAbilityModuleName() {
        return this.abilityModuleName;
    }

    public String getLayoutIdConfig() {
        return this.layoutIdConfig;
    }

    public int getSpecificationId() {
        return this.specificationId;
    }

    public boolean isESystem() {
        return this.eSystemFlag;
    }

    public boolean isEnableUpdateFlag() {
        return this.updageFlag;
    }

    public int getUpdateDuration() {
        return this.updateDuration;
    }

    public String getScheduledUpdateTime() {
        return this.scheduledUpdateTime;
    }

    public String[] getHapSourceDirs() {
        String[] strArr = this.hapSourceDirs;
        return strArr != null ? (String[]) strArr.clone() : new String[0];
    }

    public String getHapSourceByModuleName(String str) {
        return this.moduleInfoMap.containsKey(str) ? this.moduleInfoMap.get(str) : "";
    }

    /* access modifiers changed from: package-private */
    public boolean isValidItem() {
        String str;
        String str2;
        String str3;
        String str4;
        String str5 = this.bundleName;
        if (str5 == null || str5.isEmpty() || (str = this.moduleName) == null || str.isEmpty() || (str2 = this.abilityName) == null || str2.isEmpty() || (str3 = this.formName) == null || str3.isEmpty()) {
            return false;
        }
        if (this.isJsForm || ((str4 = this.layoutIdConfig) != null && !str4.isEmpty())) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isMatch(FormRecord formRecord) {
        if (formRecord != null && isEqual(formRecord.bundleName, this.bundleName) && isEqual(formRecord.moduleName, this.moduleName) && isEqual(formRecord.abilityName, this.abilityName) && isEqual(formRecord.formName, this.formName) && formRecord.specification == this.specificationId) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isSameFormConfig(FormRecord formRecord) {
        if (formRecord != null && isEqual(formRecord.bundleName, this.bundleName) && isEqual(formRecord.moduleName, this.moduleName) && isEqual(formRecord.abilityName, this.abilityName) && isEqual(formRecord.formName, this.formName)) {
            return true;
        }
        return false;
    }

    private boolean isEqual(String str, String str2) {
        return str != null && str.equals(str2);
    }

    public boolean isJsForm() {
        return this.isJsForm;
    }
}
