package ohos.aafwk.ability;

import ohos.agp.components.ComponentProvider;

/* access modifiers changed from: package-private */
public class FormRecord {
    String abilityName;
    String bundleName;
    int eSystemPreviewLayoutId;
    String formName;
    ComponentProvider formView;
    String[] hapSourceDirs;
    InstantProvider instantProvider;
    boolean isEnableUpdate;
    boolean isJsForm = true;
    String moduleName;
    int previewLayoutId;
    int specification;
    int updateAtHour;
    int updateAtMin;
    long updateDuration;

    FormRecord() {
    }
}
