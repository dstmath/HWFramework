package ohos.light.agent;

import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.light.bean.LightBrightness;
import ohos.light.bean.LightEffect;
import ohos.light.common.LightColor;
import ohos.light.common.LightEffectUtil;
import ohos.light.manager.LightManager;

public class LightAgent {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113825, "LightAgent");
    private static final int LIGHT_DURATION_MAXIMUM = 600000;
    private static final int LIGHT_DURATION_MINIMUM = 0;
    private static final int LIGHT_RANGE_MAXIMUM = 255;
    private static final int LIGHT_RANGE_MINIMUM = 0;

    private boolean isBrightnessRangeValid(int i) {
        return i < 0 || i > 255;
    }

    private boolean isDurationRangeValid(int i) {
        return i < 0 || i > LIGHT_DURATION_MAXIMUM;
    }

    public List<Integer> getLightIdList() {
        HiTraceId begin = HiTrace.begin("getLightIdList", 1);
        List<Integer> lightIdList = LightManager.getInstance().getLightIdList();
        HiTrace.end(begin);
        return lightIdList;
    }

    public boolean isSupport(int i) {
        HiTraceId begin = HiTrace.begin("isSupport", 1);
        boolean isSupport = LightManager.getInstance().isSupport(i);
        HiTrace.end(begin);
        return isSupport;
    }

    public boolean isEffectSupport(int i, String str) {
        HiTraceId begin = HiTrace.begin("isEffectSupport", 1);
        boolean isEffectSupport = LightManager.getInstance().isEffectSupport(i, str);
        HiTrace.end(begin);
        return isEffectSupport;
    }

    public boolean turnOn(int i, String str) {
        return LightManager.getInstance().turnOn(i, str);
    }

    public boolean turnOn(int i, LightEffect lightEffect) {
        HiLog.info(LABEL, "turnOn the light with lightEffect", new Object[0]);
        LightEffectUtil convert2coreLightEffect = convert2coreLightEffect(lightEffect);
        if (convert2coreLightEffect == null) {
            return false;
        }
        HiTraceId begin = HiTrace.begin("turnOn", 1);
        boolean turnOn = LightManager.getInstance().turnOn(i, convert2coreLightEffect);
        HiTrace.end(begin);
        return turnOn;
    }

    public boolean turnOn(String str) {
        if (getLightIdList().isEmpty()) {
            return false;
        }
        return LightManager.getInstance().turnOn(getLightIdList().get(0).intValue(), str);
    }

    public boolean turnOn(LightEffect lightEffect) {
        LightEffectUtil convert2coreLightEffect;
        HiLog.info(LABEL, "turnOn the light with lightEffect", new Object[0]);
        if (getLightIdList().isEmpty() || (convert2coreLightEffect = convert2coreLightEffect(lightEffect)) == null) {
            return false;
        }
        HiTraceId begin = HiTrace.begin("turnOn", 1);
        boolean turnOn = LightManager.getInstance().turnOn(getLightIdList().get(0).intValue(), convert2coreLightEffect);
        HiTrace.end(begin);
        return turnOn;
    }

    public boolean turnOff(int i) {
        HiLog.info(LABEL, "turnOff the light", new Object[0]);
        HiTraceId begin = HiTrace.begin("turnOff", 1);
        boolean turnOff = LightManager.getInstance().turnOff(i);
        HiTrace.end(begin);
        return turnOff;
    }

    public boolean turnOff() {
        HiLog.info(LABEL, "turnOff the light", new Object[0]);
        if (getLightIdList().isEmpty()) {
            return false;
        }
        HiTraceId begin = HiTrace.begin("turnOff", 1);
        boolean turnOff = LightManager.getInstance().turnOff(getLightIdList().get(0).intValue());
        HiTrace.end(begin);
        return turnOff;
    }

    private LightEffectUtil convert2coreLightEffect(LightEffect lightEffect) {
        LightColor convert2coreLightColor;
        if (lightEffect != null && (convert2coreLightColor = convert2coreLightColor(lightEffect.getLightBrightness())) != null && !isDurationRangeValid(lightEffect.getOnDuration()) && !isDurationRangeValid(lightEffect.getOffDuration())) {
            return new LightEffectUtil(convert2coreLightColor, lightEffect.getOnDuration(), lightEffect.getOffDuration());
        }
        return null;
    }

    private LightColor convert2coreLightColor(LightBrightness lightBrightness) {
        if (lightBrightness != null && !isBrightnessRangeValid(lightBrightness.getRedBrightness()) && !isBrightnessRangeValid(lightBrightness.getGreenBrightness()) && !isBrightnessRangeValid(lightBrightness.getBlueBrightness())) {
            return new LightColor(lightBrightness.getRedBrightness(), lightBrightness.getGreenBrightness(), lightBrightness.getBlueBrightness());
        }
        return null;
    }
}
