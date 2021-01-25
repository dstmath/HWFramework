package ohos.accessibility.adapter;

import java.util.List;
import ohos.accessibility.AccessibilityAbilityInfo;
import ohos.accessibility.AccessibilityStateObserver;
import ohos.accessibility.CaptionProperties;
import ohos.accessibility.CaptionPropertiesObserver;

public interface AdapterInterface {
    boolean addAccessibilityStateChangeListener(AccessibilityStateObserver accessibilityStateObserver);

    boolean addCaptionPropertiesObserver(CaptionPropertiesObserver captionPropertiesObserver);

    boolean addTouchExplorationStateChangeListener(AccessibilityStateObserver accessibilityStateObserver);

    List<AccessibilityAbilityInfo> getAbilityList(int i, int i2);

    CaptionProperties getCaptionProperties();

    boolean isAccessibilityCaptionEnabled();

    boolean isEnabled();

    boolean isTouchExplorationEnabled();

    boolean removeAccessibilityStateChangeListener(AccessibilityStateObserver accessibilityStateObserver);

    boolean removeCaptionPropertiesObserver(CaptionPropertiesObserver captionPropertiesObserver);

    boolean removeStateChangeListeners(AccessibilityStateObserver accessibilityStateObserver);

    boolean removeTouchExplorationStateChangeListener(AccessibilityStateObserver accessibilityStateObserver);
}
