package ohos.vibrator.agent;

import java.util.List;
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
    private static final int MODE_CUSTOMIZED = 2;
    private static final int MODE_DURATION = 0;
    private static final int MODE_PRESET = 1;
    private static final String[] STOP_MODE = {VIBRATOR_STOP_MODE_TIME, VIBRATOR_STOP_MODE_PRESET, VIBRATOR_STOP_MODE_CUSTOMIZED};
    private static final int VIBRATION_EFFECT_DURATION_MAXIMUM = 1800000;
    public static final String VIBRATOR_STOP_MODE_CUSTOMIZED = "customized";
    public static final String VIBRATOR_STOP_MODE_PRESET = "preset";
    public static final String VIBRATOR_STOP_MODE_TIME = "time";
    private static boolean[] vibrationMode = {false, false, false};

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

    public boolean vibrate(int i, int i2) {
        if (i2 <= 0) {
            HiLog.error(LABEL, "param duration is invalid", new Object[0]);
            return false;
        } else if (i2 > VIBRATION_EFFECT_DURATION_MAXIMUM) {
            HiLog.error(LABEL, "param duration is exceed maximum", new Object[0]);
            return false;
        } else {
            HiTraceId begin = HiTrace.begin("vibrate", 1);
            boolean vibrate = VibratorManager.getInstance().vibrate(i, i2);
            HiTrace.end(begin);
            return vibrate;
        }
    }

    public boolean vibrate(int i, String str) {
        HiTraceId begin = HiTrace.begin("vibrate", 1);
        boolean vibrate = VibratorManager.getInstance().vibrate(i, str);
        HiTrace.end(begin);
        return vibrate;
    }

    public boolean vibrate(int i, VibrationPattern vibrationPattern) {
        HiLog.info(LABEL, "vibrate with vibrationEffect", new Object[0]);
        if (vibrationPattern == null) {
            HiLog.error(LABEL, "vibrate vibrationEffect cannot be null.", new Object[0]);
            return false;
        }
        VibratorEffectUtil convert2VibratorEffectUtil = vibrationPattern.convert2VibratorEffectUtil();
        if (convert2VibratorEffectUtil == null) {
            HiLog.error(LABEL, "vibrate vibratorEffectUtil cannot be null.", new Object[0]);
            return false;
        }
        HiTraceId begin = HiTrace.begin("vibrate", 1);
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
            HiTraceId begin = HiTrace.begin("start", 1);
            boolean vibrate = VibratorManager.getInstance().vibrate(getVibratorIdList().get(0).intValue(), i);
            if (vibrate) {
                vibrationMode[0] = true;
            }
            HiTrace.end(begin);
            return vibrate;
        }
    }

    public boolean startOnce(String str) {
        if (getVibratorIdList().isEmpty()) {
            return false;
        }
        HiTraceId begin = HiTrace.begin("start", 1);
        boolean vibrate = VibratorManager.getInstance().vibrate(getVibratorIdList().get(0).intValue(), str);
        if (vibrate) {
            vibrationMode[1] = true;
        }
        HiTrace.end(begin);
        return vibrate;
    }

    public boolean start(VibrationPattern vibrationPattern) {
        HiLog.info(LABEL, "vibrate with vibrationEffect", new Object[0]);
        if (vibrationPattern == null) {
            HiLog.error(LABEL, "vibrate vibrationEffect cannot be null.", new Object[0]);
            return false;
        }
        VibratorEffectUtil convert2VibratorEffectUtil = vibrationPattern.convert2VibratorEffectUtil();
        if (convert2VibratorEffectUtil == null) {
            HiLog.error(LABEL, "vibrate vibratorEffectUtil cannot be null.", new Object[0]);
            return false;
        } else if (getVibratorIdList().isEmpty()) {
            return false;
        } else {
            HiTraceId begin = HiTrace.begin("start", 1);
            boolean vibrate = VibratorManager.getInstance().vibrate(getVibratorIdList().get(0).intValue(), convert2VibratorEffectUtil);
            if (vibrate) {
                vibrationMode[2] = true;
            }
            HiTrace.end(begin);
            return vibrate;
        }
    }

    public boolean stop(int i, String str) {
        HiLog.info(LABEL, "vibrate stop", new Object[0]);
        if (str == null || str.isEmpty()) {
            return false;
        }
        if (!VIBRATOR_STOP_MODE_TIME.equals(str) && !VIBRATOR_STOP_MODE_PRESET.equals(str) && !VIBRATOR_STOP_MODE_CUSTOMIZED.equals(str)) {
            return false;
        }
        HiTraceId begin = HiTrace.begin("stop", 1);
        boolean stop = VibratorManager.getInstance().stop(i, str);
        HiTrace.end(begin);
        return stop;
    }

    public boolean stop() {
        HiLog.info(LABEL, "vibrate stop", new Object[0]);
        int i = 0;
        boolean z = false;
        while (true) {
            boolean[] zArr = vibrationMode;
            if (i >= zArr.length) {
                return z;
            }
            if (zArr[i]) {
                HiTraceId begin = HiTrace.begin("stop", 1);
                boolean stop = VibratorManager.getInstance().stop(getVibratorIdList().get(0).intValue(), STOP_MODE[i]);
                HiTrace.end(begin);
                z = stop;
            }
            i++;
        }
    }

    public boolean setVibratorParameter(int i, String str) {
        if (!COMMAND_MMI_VIBRATOR_CALIB_ON.equals(str) && !COMMAND_MMI_VIBRATOR_AGEING_ON.equals(str)) {
            return false;
        }
        HiTraceId begin = HiTrace.begin("setVibratorParameter", 1);
        boolean vibratorParameter = VibratorManager.getInstance().setVibratorParameter(i, str);
        HiTrace.end(begin);
        return vibratorParameter;
    }

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
