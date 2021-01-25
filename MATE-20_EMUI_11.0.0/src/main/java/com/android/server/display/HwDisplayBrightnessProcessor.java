package com.android.server.display;

import android.graphics.PointF;
import android.os.Bundle;
import android.os.HwBrightnessProcessor;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;

public class HwDisplayBrightnessProcessor extends HwBrightnessProcessor {
    private static final int FAILED_RETURN_VALUE = -1;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final int SUCCESS_RETURN_VALUE = 0;
    private static final String TAG = "HwDisplayBrightnessProcessor";
    private static ArrayMap<String, HwBrightnessProcessor> sHwBrightnessProcessor = new ArrayMap<>();

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    public HwDisplayBrightnessProcessor(AutomaticBrightnessController autoController, ManualBrightnessController manualController) {
        loadHwBrightnessProcessors(autoController, manualController);
    }

    private static void loadHwBrightnessProcessors(AutomaticBrightnessController autoController, ManualBrightnessController manualController) {
        sHwBrightnessProcessor.put("Cryogenic", new CryogenicPowerProcessor(autoController));
        sHwBrightnessProcessor.put("SceneRecognition", new SceneRecognitionProcessor(autoController));
        sHwBrightnessProcessor.put("PersonalizedBrightnessCurveLevel", new PersonalizedBrightnessCurveLevelProcessor(autoController, manualController));
        sHwBrightnessProcessor.put("PersonalizedBrightness", new PersonalizedBrightnessProcessor(autoController));
        sHwBrightnessProcessor.put("QRCodeBrighten", new QrCodeBrightenProcessor(autoController));
        sHwBrightnessProcessor.put("ThermalMaxBrightnessNit", new ThermalMaxBrightnessNitProcessor(autoController, manualController));
        sHwBrightnessProcessor.put("CurrentBrightnessNit", new CurrentBrightnessNitProcessor(autoController));
        sHwBrightnessProcessor.put("AmbientLuxBrightness", new AmbientLuxBrightnessProcessor(autoController, manualController));
        sHwBrightnessProcessor.put("FrontCameraApp", new FrontCameraAppProcessor(autoController, manualController));
        sHwBrightnessProcessor.put("GameDiableAutoBrightness", new GameDiableAutoBrightnessProcessor(autoController));
        sHwBrightnessProcessor.put("ResetCurrentBrightness", new ResetCurrentBrightnessProcessor(autoController));
        sHwBrightnessProcessor.put("ResetCurrentBrightnessFromOff", new ResetCurrentBrightnessFromOffProcessor(autoController));
        sHwBrightnessProcessor.put("SetCurrentBrightnessOff", new SetCurrentBrightnessOffProcessor(autoController));
        Slog.i(TAG, "loadHwBrightnessProcessors");
    }

    public HwBrightnessProcessor getProcessor(String processName) {
        return sHwBrightnessProcessor.get(processName);
    }

    /* access modifiers changed from: private */
    public static final class SceneRecognitionProcessor extends HwBrightnessProcessor {
        private AutomaticBrightnessController mAutomaticBrightnessController;

        SceneRecognitionProcessor(AutomaticBrightnessController controller) {
            this.mAutomaticBrightnessController = controller;
        }

        public boolean getData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
            if (automaticBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                automaticBrightnessController.getUserDragInfo(data);
                if (HwDisplayBrightnessProcessor.HWDEBUG) {
                    Slog.i(HwDisplayBrightnessProcessor.TAG, "getUserDragInfo");
                }
                retValue[0] = 0;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PersonalizedBrightnessCurveLevelProcessor extends HwBrightnessProcessor {
        private AutomaticBrightnessController mAutomaticBrightnessController;
        private ManualBrightnessController mManualBrightnessController;

        PersonalizedBrightnessCurveLevelProcessor(AutomaticBrightnessController controller, ManualBrightnessController manualController) {
            this.mAutomaticBrightnessController = controller;
            this.mManualBrightnessController = manualController;
        }

        public boolean setData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            if (this.mAutomaticBrightnessController == null || this.mManualBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                int topApkLevel = data.getInt("TopApkLevel", 0);
                this.mAutomaticBrightnessController.setPersonalizedBrightnessCurveLevel(topApkLevel);
                this.mManualBrightnessController.setPersonalizedBrightnessCurveLevel(topApkLevel);
                if (HwDisplayBrightnessProcessor.HWDEBUG) {
                    Slog.i(HwDisplayBrightnessProcessor.TAG, "setPersonalizedBrightnessCurveLevel=" + topApkLevel);
                }
                retValue[0] = 0;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PersonalizedBrightnessProcessor extends HwBrightnessProcessor {
        private static final String TAG = "PersonalizedBrightnessProcessor";
        private AutomaticBrightnessController mAutomaticBrightnessController;

        PersonalizedBrightnessProcessor(AutomaticBrightnessController controller) {
            this.mAutomaticBrightnessController = controller;
        }

        public boolean setData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            if (this.mAutomaticBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                int isCurveUpdate = data.getInt("CurveUpdateFlag", 0);
                if (HwDisplayBrightnessProcessor.HWDEBUG) {
                    Slog.i(TAG, "isCurveUpdate = " + isCurveUpdate);
                }
                if (isCurveUpdate == 1) {
                    this.mAutomaticBrightnessController.updateNewBrightnessCurveTmp();
                }
                retValue[0] = 0;
                return true;
            }
        }

        public boolean getData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
            if (automaticBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                List<PointF> defaultCurve = automaticBrightnessController.getCurrentDefaultNewCurveLine();
                if (defaultCurve == null) {
                    retValue[0] = -1;
                    return false;
                }
                if (defaultCurve instanceof ArrayList) {
                    data.putParcelableArrayList("DefaultCurve", (ArrayList) defaultCurve);
                }
                if (HwDisplayBrightnessProcessor.HWDEBUG) {
                    Slog.i(TAG, "defaultCurve ");
                }
                retValue[0] = 0;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class QrCodeBrightenProcessor extends HwBrightnessProcessor {
        private static final String TAG = "QrCodeBrightenProcessor";
        private AutomaticBrightnessController mAutomaticBrightnessController;

        QrCodeBrightenProcessor(AutomaticBrightnessController controller) {
            this.mAutomaticBrightnessController = controller;
        }

        public boolean setData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            if (this.mAutomaticBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                boolean isVideoPlay = data.getBoolean("IsVideoPlay", false);
                if (HwDisplayBrightnessProcessor.HWFLOW) {
                    Slog.i(TAG, "IsVideoPlay = " + isVideoPlay);
                }
                this.mAutomaticBrightnessController.setVideoPlayStatus(isVideoPlay);
                retValue[0] = 0;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class ThermalMaxBrightnessNitProcessor extends HwBrightnessProcessor {
        private static final String TAG = "ThermalMaxBrightnessNitProcessor";
        private AutomaticBrightnessController mAutomaticBrightnessController;
        private ManualBrightnessController mManualBrightnessController;

        ThermalMaxBrightnessNitProcessor(AutomaticBrightnessController autoController, ManualBrightnessController manualController) {
            this.mAutomaticBrightnessController = autoController;
            this.mManualBrightnessController = manualController;
        }

        public boolean setData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            if (this.mAutomaticBrightnessController == null || this.mManualBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                int maxBrightnessNit = data.getInt("MaxBrightnessNit", 0);
                if (HwDisplayBrightnessProcessor.HWFLOW) {
                    Slog.i(TAG, "MaxBrightnessNitFromThermal,maxNit = " + maxBrightnessNit);
                }
                this.mAutomaticBrightnessController.setMaxBrightnessNitFromThermal(maxBrightnessNit);
                this.mManualBrightnessController.setMaxBrightnessNitFromThermal(maxBrightnessNit);
                retValue[0] = 0;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class CurrentBrightnessNitProcessor extends HwBrightnessProcessor {
        private static final String TAG = "CurrentBrightnessNitProcessor";
        private AutomaticBrightnessController mAutomaticBrightnessController;

        CurrentBrightnessNitProcessor(AutomaticBrightnessController autoController) {
            this.mAutomaticBrightnessController = autoController;
        }

        public boolean getData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
            if (automaticBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                int currentBrightnessNit = automaticBrightnessController.getCurrentBrightnessNit();
                int thermalMaxBrightnessNit = this.mAutomaticBrightnessController.getTheramlMaxBrightnessNit();
                int maxBrightnessNit = this.mAutomaticBrightnessController.getMaxBrightnessNit();
                int minBrightnessNit = this.mAutomaticBrightnessController.getMinBrightnessNit();
                data.putInt("CurrentBrightnessNit", currentBrightnessNit);
                data.putInt("ThermalMaxBrightnessNit", thermalMaxBrightnessNit);
                data.putInt("MaxBrightnessNit", maxBrightnessNit);
                data.putInt("MinBrightnessNit", minBrightnessNit);
                if (HwDisplayBrightnessProcessor.HWDEBUG) {
                    Slog.i(TAG, "currentBrightnessNit=" + currentBrightnessNit + ",thermalMaxBrightnessNit=" + thermalMaxBrightnessNit + ",maxBrightnessNit=" + maxBrightnessNit + ",minBrightnessNit=" + minBrightnessNit);
                }
                retValue[0] = 0;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class AmbientLuxBrightnessProcessor extends HwBrightnessProcessor {
        private static final String TAG = "AmbientLuxBrightnessProcessor";
        private AutomaticBrightnessController mAutomaticBrightnessController;
        private ManualBrightnessController mManualBrightnessController;

        AmbientLuxBrightnessProcessor(AutomaticBrightnessController autoController, ManualBrightnessController manualController) {
            this.mAutomaticBrightnessController = autoController;
            this.mManualBrightnessController = manualController;
        }

        public boolean getData(Bundle data, int[] retValue) {
            int brightnessLevel;
            int ambientLux;
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
            if (automaticBrightnessController == null || this.mManualBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                boolean isAutoModeEnable = automaticBrightnessController.getAutoBrightnessEnable();
                if (isAutoModeEnable) {
                    ambientLux = this.mAutomaticBrightnessController.getAmbientLux();
                    brightnessLevel = this.mAutomaticBrightnessController.getBrightnessLevel(ambientLux);
                } else {
                    ambientLux = this.mManualBrightnessController.getAmbientLux();
                    brightnessLevel = this.mManualBrightnessController.getBrightnessLevel(ambientLux);
                }
                data.putInt("AmbientLux", ambientLux);
                data.putInt("BrightnessLevel", brightnessLevel);
                if (HwDisplayBrightnessProcessor.HWFLOW) {
                    Slog.i(TAG, "ambientLux=" + ambientLux + ",brightnessLevel= " + brightnessLevel + ",isAutoModeEnable=" + isAutoModeEnable);
                }
                retValue[0] = 0;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class FrontCameraAppProcessor extends HwBrightnessProcessor {
        private static final String TAG = "FrontCameraAppProcessor";
        private AutomaticBrightnessController mAutomaticBrightnessController;
        private ManualBrightnessController mManualBrightnessController;

        FrontCameraAppProcessor(AutomaticBrightnessController autoController, ManualBrightnessController manualController) {
            this.mAutomaticBrightnessController = autoController;
            this.mManualBrightnessController = manualController;
        }

        public boolean setData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            if (this.mAutomaticBrightnessController == null || this.mManualBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                boolean isFrontCameraAppEnable = data.getBoolean("FrontCameraAppEnableState", false);
                if (HwDisplayBrightnessProcessor.HWFLOW) {
                    Slog.i(TAG, "frontCameraAppEnableState= " + isFrontCameraAppEnable);
                }
                this.mAutomaticBrightnessController.setFrontCameraAppEnableState(isFrontCameraAppEnable);
                this.mManualBrightnessController.setFrontCameraAppEnableState(isFrontCameraAppEnable);
                retValue[0] = 0;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class GameDiableAutoBrightnessProcessor extends HwBrightnessProcessor {
        private static final String TAG = "GameDiableAutoBrightnessProcessor";
        private AutomaticBrightnessController mAutomaticBrightnessController;

        GameDiableAutoBrightnessProcessor(AutomaticBrightnessController autoController) {
            this.mAutomaticBrightnessController = autoController;
        }

        public boolean getData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
            if (automaticBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                boolean isGameDisableAutoBrightnessModeEnable = automaticBrightnessController.getGameDisableAutoBrightnessModeStatus();
                data.putBoolean("GameDisableAutoBrightnessModeEnable", isGameDisableAutoBrightnessModeEnable);
                if (HwDisplayBrightnessProcessor.HWFLOW) {
                    Slog.i(TAG, "GameDisableAutoBrightnessModeEnable=" + isGameDisableAutoBrightnessModeEnable);
                }
                retValue[0] = 0;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class ResetCurrentBrightnessProcessor extends HwBrightnessProcessor {
        private static final String TAG = "ResetCurrentBrightnessProcessor";
        private AutomaticBrightnessController mAutomaticBrightnessController;

        public ResetCurrentBrightnessProcessor(AutomaticBrightnessController autoController) {
            this.mAutomaticBrightnessController = autoController;
        }

        public boolean setData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            if (this.mAutomaticBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                boolean isUpdateBrightnessEnable = data.getBoolean("UpdateBrightnessEnable", false);
                if (HwDisplayBrightnessProcessor.HWFLOW) {
                    Slog.i(TAG, "isUpdateBrightnessEnable=" + isUpdateBrightnessEnable);
                }
                if (isUpdateBrightnessEnable) {
                    this.mAutomaticBrightnessController.resetCurrentBrightness();
                }
                retValue[0] = 0;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class ResetCurrentBrightnessFromOffProcessor extends HwBrightnessProcessor {
        private static final String TAG = "ResetCurrentBrightnessFromOffProcessor";
        private AutomaticBrightnessController mAutomaticBrightnessController;

        public ResetCurrentBrightnessFromOffProcessor(AutomaticBrightnessController autoController) {
            this.mAutomaticBrightnessController = autoController;
        }

        public boolean setData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            if (this.mAutomaticBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                boolean isUpdateBrightnessEnable = data.getBoolean("UpdateBrightnessEnable", false);
                if (HwDisplayBrightnessProcessor.HWFLOW) {
                    Slog.i(TAG, "isUpdateBrightnessEnable=" + isUpdateBrightnessEnable);
                }
                if (isUpdateBrightnessEnable) {
                    this.mAutomaticBrightnessController.resetCurrentBrightnessFromOff();
                }
                retValue[0] = 0;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class SetCurrentBrightnessOffProcessor extends HwBrightnessProcessor {
        private static final String TAG = "SetCurrentBrightnessOffProcessor";
        private AutomaticBrightnessController mAutomaticBrightnessController;

        public SetCurrentBrightnessOffProcessor(AutomaticBrightnessController autoController) {
            this.mAutomaticBrightnessController = autoController;
        }

        public boolean setData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length == 0) {
                return false;
            }
            if (this.mAutomaticBrightnessController == null) {
                retValue[0] = -1;
                return false;
            } else if (data == null) {
                retValue[0] = -1;
                return false;
            } else {
                boolean isUpdateBrightnessOffEnable = data.getBoolean("UpdateBrightnessOffEnable", false);
                if (HwDisplayBrightnessProcessor.HWFLOW) {
                    Slog.i(TAG, "isUpdateBrightnessOffEnable=" + isUpdateBrightnessOffEnable);
                }
                if (isUpdateBrightnessOffEnable) {
                    this.mAutomaticBrightnessController.setCurrentBrightnessOff();
                }
                retValue[0] = 0;
                return true;
            }
        }
    }
}
