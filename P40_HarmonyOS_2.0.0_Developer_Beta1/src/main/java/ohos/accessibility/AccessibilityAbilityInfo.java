package ohos.accessibility;

import ohos.bundle.AbilityInfo;

public class AccessibilityAbilityInfo {
    public static final int ACCESSIBILITY_ABILITY_TYPE_AUDIBLE = 4;
    public static final int ACCESSIBILITY_ABILITY_TYPE_BRAILLE = 32;
    public static final int ACCESSIBILITY_ABILITY_TYPE_GENERIC = 16;
    public static final int ACCESSIBILITY_ABILITY_TYPE_HAPTIC = 2;
    public static final int ACCESSIBILITY_ABILITY_TYPE_SPOKEN = 1;
    public static final int ACCESSIBILITY_ABILITY_TYPE_VISUAL = 8;
    public static final int CAPABILITY_FILTER_KEY_EVENTS = 8;
    public static final int CAPABILITY_GESTURES = 32;
    public static final int CAPABILITY_MAGNIFICATION = 16;
    public static final int CAPABILITY_TOUCH_EXPLORATION = 2;
    public static final int CAPABILITY_WINDOW_CONTENT = 1;
    public static final int FEEDBACK_ALL_MASK = -1;
    private AbilityInfo abilityInfo;
    private int accessibilityAbilityType;
    private int capabilityValues;
    private String description;
    private int eventTypes;
    private String id;
    private String name;
    private String packageName;

    public String getName() {
        return this.name;
    }

    public int getAccessibilityAbilityType() {
        return this.accessibilityAbilityType;
    }

    public int getEventTypes() {
        return this.eventTypes;
    }

    public String getDescription() {
        return this.description;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public int getCapabilityValues() {
        return this.capabilityValues;
    }

    public String getId() {
        return this.id;
    }

    public AbilityInfo getAbilityInfo() {
        return this.abilityInfo;
    }

    public void setName(String str) {
        this.name = str;
    }

    public void setAccessibilityAbilityType(int i) {
        this.accessibilityAbilityType = i;
    }

    public void setEventTypes(int i) {
        this.eventTypes = i;
    }

    public void setDescription(String str) {
        this.description = str;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public void setId(String str) {
        this.id = str;
    }

    public void setCapabilityValues(int i) {
        this.capabilityValues = i;
    }

    public void setAbilityInfo(AbilityInfo abilityInfo2) {
        this.abilityInfo = abilityInfo2;
    }
}
