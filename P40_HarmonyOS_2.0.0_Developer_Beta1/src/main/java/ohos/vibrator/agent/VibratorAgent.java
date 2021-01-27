package ohos.vibrator.agent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.vibrator.bean.VibrationPattern;
import ohos.vibrator.common.VibratorEffectUtil;
import ohos.vibrator.manager.VibratorManager;

public class VibratorAgent {
    public static final String COMMAND_MMI_VIBRATOR_AGEING_ON = "mmi_vibrator_ageing_on";
    public static final String COMMAND_MMI_VIBRATOR_CALIB_GET_RESULT = "mmi_vibrator_calib_get_result";
    public static final String COMMAND_MMI_VIBRATOR_CALIB_ON = "mmi_vibrator_calib_on";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113826, "VibratorAgent");
    private static final int VIBRATION_EFFECT_DURATION_MAXIMUM = 1800000;
    public static final String VIBRATOR_STOP_MODE_CUSTOMIZED = "customized";
    public static final String VIBRATOR_STOP_MODE_PRESET = "preset";
    public static final String VIBRATOR_STOP_MODE_TIME = "time";
    private final Map<Integer, String> vibratorIdToMode = new ConcurrentHashMap();

    public List<Integer> getVibratorIdList() {
        HiTraceId begin = HiTrace.begin("getVibratorIdList", 1);
        List<Integer> vibratorIdList = VibratorManager.getInstance().getVibratorIdList();
        HiTrace.end(begin);
        return vibratorIdList;
    }

    public boolean isSupport(int i) {
        HiTraceId begin = HiTrace.begin("isSupport", 1);
        boolean isSupport = VibratorManager.getInstance().isSupport(i);
        HiTrace.end(begin);
        return isSupport;
    }

    public boolean isEffectSupport(int i, String str) {
        HiTraceId begin = HiTrace.begin("isEffectSupport", 1);
        boolean isEffectSupport = VibratorManager.getInstance().isEffectSupport(i, str);
        HiTrace.end(begin);
        return isEffectSupport;
    }

    public boolean startOnce(int i, int i2) {
        if (i2 <= 0) {
            HiLog.error(LABEL, "param duration is invalid", new Object[0]);
            return false;
        } else if (i2 > VIBRATION_EFFECT_DURATION_MAXIMUM) {
            HiLog.error(LABEL, "param duration is exceed maximum", new Object[0]);
            return false;
        } else {
            this.vibratorIdToMode.put(Integer.valueOf(i), VIBRATOR_STOP_MODE_TIME);
            HiTraceId begin = HiTrace.begin("startOnce", 1);
            boolean vibrate = VibratorManager.getInstance().vibrate(i, i2);
            HiTrace.end(begin);
            return vibrate;
        }
    }

    public boolean startOnce(int i, String str) {
        this.vibratorIdToMode.put(Integer.valueOf(i), VIBRATOR_STOP_MODE_PRESET);
        HiTraceId begin = HiTrace.begin("startOnce", 1);
        boolean vibrate = VibratorManager.getInstance().vibrate(i, str);
        HiTrace.end(begin);
        return vibrate;
    }

    public boolean start(int i, VibrationPattern vibrationPattern) {
        HiLog.debug(LABEL, "vibrate with vibrationEffect", new Object[0]);
        if (vibrationPattern == null) {
            HiLog.error(LABEL, "vibrate vibrationEffect cannot be null", new Object[0]);
            return false;
        }
        VibratorEffectUtil convert2VibratorEffectUtil = vibrationPattern.convert2VibratorEffectUtil();
        if (convert2VibratorEffectUtil == null) {
            HiLog.error(LABEL, "vibrate vibratorEffectUtil cannot be null", new Object[0]);
            return false;
        }
        this.vibratorIdToMode.put(Integer.valueOf(i), VIBRATOR_STOP_MODE_CUSTOMIZED);
        HiTraceId begin = HiTrace.begin("start", 1);
        boolean vibrate = VibratorManager.getInstance().vibrate(i, convert2VibratorEffectUtil);
        HiTrace.end(begin);
        return vibrate;
    }

    public boolean startOnce(int i) {
        if (i <= 0) {
            HiLog.error(LABEL, "param duration is invalid", new Object[0]);
            return false;
        } else if (i > VIBRATION_EFFECT_DURATION_MAXIMUM) {
            HiLog.error(LABEL, "param duration is exceed maximum", new Object[0]);
            return false;
        } else if (getVibratorIdList().isEmpty()) {
            return false;
        } else {
            this.vibratorIdToMode.put(getVibratorIdList().get(0), VIBRATOR_STOP_MODE_TIME);
            HiTraceId begin = HiTrace.begin("startOnce", 1);
            boolean vibrate = VibratorManager.getInstance().vibrate(getVibratorIdList().get(0).intValue(), i);
            HiTrace.end(begin);
            return vibrate;
        }
    }

    public boolean startOnce(String str) {
        return start(str, false);
    }

    public boolean start(String str, boolean z) {
        if (getVibratorIdList().isEmpty()) {
            return false;
        }
        this.vibratorIdToMode.put(getVibratorIdList().get(0), VIBRATOR_STOP_MODE_PRESET);
        HiTraceId begin = HiTrace.begin("start", 1);
        boolean vibrate = VibratorManager.getInstance().vibrate(getVibratorIdList().get(0).intValue(), str, z);
        HiTrace.end(begin);
        return vibrate;
    }

    public boolean start(VibrationPattern vibrationPattern) {
        HiLog.debug(LABEL, "vibrate with vibrationEffect", new Object[0]);
        if (vibrationPattern == null) {
            HiLog.error(LABEL, "vibrate vibrationEffect cannot be null", new Object[0]);
            return false;
        }
        VibratorEffectUtil convert2VibratorEffectUtil = vibrationPattern.convert2VibratorEffectUtil();
        if (convert2VibratorEffectUtil == null) {
            HiLog.error(LABEL, "vibrate vibratorEffectUtil cannot be null", new Object[0]);
            return false;
        } else if (getVibratorIdList().isEmpty()) {
            return false;
        } else {
            this.vibratorIdToMode.put(getVibratorIdList().get(0), VIBRATOR_STOP_MODE_CUSTOMIZED);
            HiTraceId begin = HiTrace.begin("start", 1);
            boolean vibrate = VibratorManager.getInstance().vibrate(getVibratorIdList().get(0).intValue(), convert2VibratorEffectUtil);
            HiTrace.end(begin);
            return vibrate;
        }
    }

    public boolean stop(int i, String str) {
        HiLog.debug(LABEL, "vibrate stop", new Object[0]);
        if (str == null || str.isEmpty()) {
            return false;
        }
        if (!VIBRATOR_STOP_MODE_TIME.equals(str) && !VIBRATOR_STOP_MODE_PRESET.equals(str) && !VIBRATOR_STOP_MODE_CUSTOMIZED.equals(str)) {
            return false;
        }
        this.vibratorIdToMode.remove(Integer.valueOf(i), str);
        HiTraceId begin = HiTrace.begin("stop", 1);
        boolean stop = VibratorManager.getInstance().stop(i, str);
        HiTrace.end(begin);
        return stop;
    }

    public boolean stop() {
        HiLog.debug(LABEL, "vibrate stop", new Object[0]);
        if (getVibratorIdList().isEmpty() || this.vibratorIdToMode.isEmpty()) {
            HiLog.error(LABEL, "there is no vibrator or the vibratorIdToMode is empty", new Object[0]);
            return false;
        }
        HiTraceId begin = HiTrace.begin("stop", 1);
        boolean stop = stop(getVibratorIdList().get(0).intValue(), this.vibratorIdToMode.get(getVibratorIdList().get(0)));
        HiTrace.end(begin);
        return stop;
    }

    @SystemApi
    public boolean setVibratorParameter(int i, String str) {
        if (!COMMAND_MMI_VIBRATOR_CALIB_ON.equals(str) && !COMMAND_MMI_VIBRATOR_AGEING_ON.equals(str)) {
            return false;
        }
        HiTraceId begin = HiTrace.begin("setVibratorParameter", 1);
        boolean vibratorParameter = VibratorManager.getInstance().setVibratorParameter(i, str);
        HiTrace.end(begin);
        return vibratorParameter;
    }

    @SystemApi
    public String getVibratorParameter(int i, String str) {
        if (!COMMAND_MMI_VIBRATOR_CALIB_GET_RESULT.equals(str) && !COMMAND_MMI_VIBRATOR_AGEING_ON.equals(str)) {
            return "";
        }
        HiTraceId begin = HiTrace.begin("getVibratorParameter", 1);
        String vibratorParameter = VibratorManager.getInstance().getVibratorParameter(i, str);
        HiTrace.end(begin);
        return vibratorParameter;
    }
}
