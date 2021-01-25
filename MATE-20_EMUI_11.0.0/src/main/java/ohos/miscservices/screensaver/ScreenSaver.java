package ohos.miscservices.screensaver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.sysability.samgr.SysAbilityManager;

public class ScreenSaver {
    public static final int POLICY_CHARGING = 0;
    public static final int POLICY_DOCKED = 1;
    public static final int POLICY_EITHER = 2;
    public static final int POLICY_NEVER = 3;
    private ScreenSaverProxyImpl proxy;

    @Target({ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface IntgerDef {
        String[] prefix() default {};

        int[] value() default {};
    }

    public static class ScreenSaverBean {
        public boolean isActiveElement;
        public CharSequence name;
        public ElementName screenSaverElement;
        public ElementName screenSaverSettingElement;
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface ScreenSaverPolicy {
    }

    public ScreenSaver(Context context) {
        this.proxy = ScreenSaverProxyImpl.getInstance(context, SysAbilityManager.getSysAbility(-1));
    }

    public void activeElement(ElementName elementName) {
        this.proxy.activeElement(elementName);
    }

    public ElementName getActiveElement() {
        return this.proxy.getActiveElement();
    }

    public void preview(ElementName elementName) {
        this.proxy.preview(elementName);
    }

    public List<ScreenSaverBean> getElementList() {
        return this.proxy.getElementList();
    }

    public void setScreenSaverPolicy(int i) {
        this.proxy.setScreenSaverPolicy(i);
    }

    public int getScreenSaverPolicy() {
        return this.proxy.getScreenSaverPolicy();
    }

    public void enable() {
        this.proxy.enable();
    }

    public void disable() {
        this.proxy.disable();
    }

    public boolean isEnabled() {
        return this.proxy.isEnabled();
    }
}
