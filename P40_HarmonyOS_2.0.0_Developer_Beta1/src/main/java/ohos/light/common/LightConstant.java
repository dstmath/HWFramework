package ohos.light.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import ohos.light.bean.LightEffect;

public class LightConstant {
    public static final String LIGHT_EFFECT_ID_BELT = "3";
    public static final String LIGHT_EFFECT_ID_BUTTONS = "2";
    public static final String LIGHT_EFFECT_ID_KEYBOARD = "1";
    public static final List<String> LIGHT_EFFECT_ID_LIST = Collections.unmodifiableList(Arrays.asList(LightEffect.LIGHT_ID_LED));
    public static final int LIGHT_ID_BELT = 3;
    public static final int LIGHT_ID_BUTTONS = 2;
    public static final int LIGHT_ID_KEYBOARD = 1;
    public static final int LIGHT_ID_LED = 0;
}
