package ohos.miscservices.screensaver;

import android.content.ComponentName;
import ohos.bundle.ElementName;

public class Utils {
    private Utils() {
    }

    public static ComponentName convertElementToCpn(ElementName elementName) {
        if (elementName == null) {
            return null;
        }
        return new ComponentName(elementName.getBundleName(), elementName.getAbilityName());
    }

    public static ElementName convertCpnToElement(ComponentName componentName) {
        if (componentName == null) {
            return null;
        }
        return new ElementName("", componentName.getPackageName(), componentName.getClassName());
    }
}
