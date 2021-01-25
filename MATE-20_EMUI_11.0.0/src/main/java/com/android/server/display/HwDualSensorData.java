package com.android.server.display;

import android.util.Log;
import android.util.Slog;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.huawei.displayengine.XmlData;
import java.util.Arrays;
import java.util.HashMap;

public class HwDualSensorData implements XmlData {
    static final int BACK_MSG_TIMER = 2;
    static final int BACK_VALUE_SIZE = 15;
    static final int CCT_MIN_VALUE = 0;
    static final float CCT_PARAM_1 = -449.0f;
    static final float CCT_PARAM_2 = 3525.0f;
    static final float CCT_PARAM_3 = 6823.3f;
    static final float CCT_PARAM_4 = 5520.33f;
    static final int FLASHLIGHT_DETECTION_MODE_CALLBACK = 1;
    static final int FLASHLIGHT_DETECTION_MODE_THRESHOLD = 0;
    static final int FRONT_MSG_TIMER = 1;
    static final int FRONT_VALUE_SIZE = 2;
    static final int FUSED_MSG_TIMER = 3;
    private static final boolean HW_FLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    static final int LUX_COEF = 100;
    static final int LUX_MIN_VALUE = 0;
    static final float MAX_CCT_VALUE = 13000.0f;
    static final int MSG_TIMER = 0;
    static final int NORM_COEFFICIENT = 12800;
    static final int OBSERVER_DATA_SIZE = 4;
    static final float RGBCW_MULTIPLY_VALUE = 2.6f;
    static final int SPECTRUM_NIR_INDEX_1 = 8;
    static final int SPECTRUM_NIR_INDEX_2 = 9;
    static final int SPECTRUM_PARAM_NIR_NUM = 8;
    static final int SPECTRUM_PARAM_NUM = 10;
    static final int SPECTRUM_PARAM_POLY_NUM = 3;
    static final float SPEC_NIR_WRIGHT = 0.15f;
    private static final String TAG = "HwDualSensorData";
    static final int TIMER_DISABLE = -1;
    static final int VERSION_NORMALIZED_TYPE = 4;
    static final int VERSION_NORMALIZED_TYPE_INDEX_CCT = 10;
    static final int VERSION_NORMALIZED_TYPE_INDEX_LUX = 6;
    static final int VERSION_NORMALIZED_TYPE_INDEX_MAX = 11;
    static final int VERSION_OUTWARD_SENSOR_TYPE = 13;
    static final int VERSION_RGBCW_TYPE = 3;
    static final int VERSION_RGB_TYPE = 2;
    static final int VERSION_SPECTRUM_TYPE = 12;
    static final int VERSION_XYZ_TYPE = 1;
    static final int WARM_UP_FLAG = 2;
    static final float XYZ_MULTIPLY_VALUE = 1600.128f;
    static final float X_NORM_CONST = 0.332f;
    static final float Y_NORM_CONST = 0.1858f;
    float aGain;
    float aGainRgbcw;
    float aTime;
    float aTimeRgbcw;
    float backFloorThresh;
    long backLuxDeviationThresh;
    int backRateMillis;
    float backRoofThresh;
    int backSensorBypassCount;
    int backSensorBypassCountMax;
    int[] backSensorStabilityThreshold;
    int backSensorTimeOutTh;
    int backTimeOutCount;
    float darkRoomDelta;
    float darkRoomDelta1;
    float darkRoomDelta2;
    float darkRoomThresh;
    float darkRoomThresh1;
    float darkRoomThresh2;
    float darkRoomThresh3;
    int flashlightDetectionMode;
    int flashlightOffTimeThMs;
    int frontRateMillis;
    int[] frontSensorStabilityThreshold;
    int fusedRateMillis;
    float irBoundry;
    float irBoundryRgbcw;
    boolean isFilterOn;
    boolean isInwardFoldScreen;
    boolean isOutwardLightSensor;
    float luxCoefHigh1;
    float luxCoefHigh2;
    float luxCoefHigh3;
    float luxCoefHigh4;
    float luxCoefHigh5;
    float luxCoefHighBlue;
    float luxCoefHighGreen;
    float luxCoefHighIr;
    float luxCoefHighOffset;
    float luxCoefHighRed;
    float luxCoefHighX;
    float luxCoefHighY;
    float luxCoefHighZ;
    float luxCoefLow1;
    float luxCoefLow2;
    float luxCoefLow3;
    float luxCoefLow4;
    float luxCoefLow5;
    float luxCoefLowBlue;
    float luxCoefLowGreen;
    float luxCoefLowIr;
    float luxCoefLowOffset;
    float luxCoefLowRed;
    float luxCoefLowX;
    float luxCoefLowY;
    float luxCoefLowZ;
    float luxScale;
    HashMap<String, Integer> moduleSensorMap;
    boolean needNirCompensation;
    double[] polyCoefs1;
    double[] polyCoefs2;
    double[] polyCoefs3;
    double[] polyCoefs4;
    double[] polyCoefs5;
    float productionCalibrationB;
    float productionCalibrationC;
    float productionCalibrationG;
    float productionCalibrationIr1;
    float productionCalibrationR;
    float productionCalibrationW;
    float productionCalibrationX;
    float productionCalibrationY;
    float productionCalibrationZ;
    float ratioIrGreen;
    double[] ratioNir;
    double[] scaling;
    int sensorVersion;
    float specLowLuxTh1;
    float specLowLuxTh2;
    float specLowLuxTh3;
    float specLowLuxTh4;
    double spectrumGain;
    double[] spectrumParam;
    double spectrumTime;
    int[] stabilizedProbabilityLut;

    /* access modifiers changed from: package-private */
    public enum FlashlightMode {
        OFF,
        TORCH_MODE,
        CAMERA_MODE
    }

    HwDualSensorData() {
        setDefault();
    }

    private void setDefaultRgbcwParams() {
        this.luxCoefHigh1 = -0.004354524f;
        this.luxCoefHigh2 = -0.06864114f;
        this.luxCoefHigh3 = 0.24784172f;
        this.luxCoefHigh4 = -0.15217727f;
        this.luxCoefHigh5 = -6.927572E-4f;
        this.luxCoefLow1 = 0.058521573f;
        this.luxCoefLow2 = -0.028247027f;
        this.luxCoefLow3 = -0.0530197f;
        this.luxCoefLow4 = -0.006297543f;
        this.luxCoefLow5 = 0.005755076f;
        this.productionCalibrationC = 1.0f;
        this.productionCalibrationR = 1.0f;
        this.productionCalibrationG = 1.0f;
        this.productionCalibrationB = 1.0f;
        this.productionCalibrationW = 1.0f;
        this.irBoundryRgbcw = 0.3f;
        this.aTimeRgbcw = 100.0f;
        this.aGainRgbcw = 128.0f;
    }

    private void setDefaultXyzParams() {
        this.irBoundry = 1.0f;
        this.luxCoefHighX = -11.499684f;
        this.luxCoefHighY = 16.098469f;
        this.luxCoefHighZ = -3.7601638f;
        this.luxCoefHighIr = 0.5164043f;
        this.luxCoefHighOffset = 0.0f;
        this.luxCoefLowX = -3.0825195f;
        this.luxCoefLowY = 7.662419f;
        this.luxCoefLowZ = -6.400353f;
        this.luxCoefLowIr = -3.1549554f;
        this.luxCoefLowOffset = 0.0f;
        this.productionCalibrationX = 1.0f;
        this.productionCalibrationY = 1.0f;
        this.productionCalibrationZ = 1.0f;
        this.productionCalibrationIr1 = 1.0f;
        this.aTime = 100.008f;
        this.aGain = 16.0f;
    }

    private void setDefaultRgbParams() {
        this.ratioIrGreen = 0.5f;
        this.luxCoefHighRed = 0.02673f;
        this.luxCoefHighGreen = 0.19239f;
        this.luxCoefHighBlue = -0.06468f;
        this.luxCoefLowRed = 0.02442f;
        this.luxCoefLowGreen = 0.02112f;
        this.luxCoefLowBlue = -0.09306f;
    }

    private void setDefaultControlParams() {
        this.frontSensorStabilityThreshold = new int[]{-30, -4, 4, 30};
        this.backSensorStabilityThreshold = new int[]{-30, -4, 4, 30};
        this.stabilizedProbabilityLut = new int[25];
        this.backRoofThresh = 400000.0f;
        this.backFloorThresh = 5.0f;
        this.darkRoomThresh = 3.0f;
        this.darkRoomThresh1 = 15.0f;
        this.darkRoomThresh2 = 35.0f;
        this.darkRoomThresh3 = 75.0f;
        this.darkRoomDelta = 5.0f;
        this.darkRoomDelta1 = 10.0f;
        this.darkRoomDelta2 = 30.0f;
        this.backLuxDeviationThresh = HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM;
        this.flashlightDetectionMode = 0;
        this.flashlightOffTimeThMs = 600;
        this.frontRateMillis = 300;
        this.backRateMillis = 300;
        this.fusedRateMillis = 300;
        this.backSensorTimeOutTh = 5;
        this.backTimeOutCount = 0;
        this.backSensorBypassCount = 0;
        this.backSensorBypassCountMax = 3;
        this.isFilterOn = true;
        this.moduleSensorMap = new HashMap<>();
    }

    private void setDefaultSpectrumParams() {
        this.luxScale = 1.016667f;
        this.spectrumGain = 256.0d;
        this.spectrumTime = 50.0d;
        this.specLowLuxTh1 = 15.0f;
        this.specLowLuxTh2 = 75.0f;
        this.specLowLuxTh3 = 110.0f;
        this.specLowLuxTh4 = 110.0f;
        this.spectrumParam = new double[]{130.2202d, 241.6954d, 731.8015d, 2453.8471d, 4208.88d, 2730.6698d, 618.0548d, 30.2623d, -5.9318d, -513.5437d};
        this.scaling = new double[]{1.4838d, 1.8705d, 1.8507d, 1.9243d, 1.7956d, 2.0007d, 1.923d, 2.3181d};
        this.ratioNir = new double[]{0.0447d, 0.0551d, 0.0531d, 0.0615d, 0.0391d, 0.0701d, 0.0531d, 0.0428d};
        this.polyCoefs1 = new double[]{0.0d, 0.00189304246550637d, -0.0537671503168156d, 1.46102639307804d};
        this.polyCoefs2 = new double[]{0.0d, -1.05424017372123E-4d, 0.00618684416953924d, 1.06133309650234d};
        this.polyCoefs3 = new double[]{0.0d, -9.29273077531602E-7d, 1.63340136216729E-4d, 1.10127143743199d};
        this.polyCoefs4 = new double[]{0.0d, 0.0d, 0.0d, 1.0d};
        this.polyCoefs5 = new double[]{0.0d, 0.0d, 0.0d, 1.0d};
        this.needNirCompensation = false;
    }

    private void setDefaultFoldScreenSensorParams() {
        this.isOutwardLightSensor = false;
        this.isInwardFoldScreen = false;
    }

    private void setDefault() {
        this.sensorVersion = 1;
        setDefaultRgbcwParams();
        setDefaultXyzParams();
        setDefaultRgbParams();
        setDefaultControlParams();
        setDefaultSpectrumParams();
        setDefaultFoldScreenSensorParams();
    }

    public void loadDefault() {
        if (HW_FLOW) {
            Slog.i(TAG, "loadDefault()");
        }
        setDefault();
    }

    public void print() {
        if (HW_FLOW) {
            Slog.i(TAG, "sensorVersion = " + this.sensorVersion);
            printXyzParam();
            printRgbParam();
            printRgbcwParam();
            printControlParam();
            printSpectrumParam();
            printFoldScreenSensorParam();
        }
    }

    private void printXyzParam() {
        if (this.sensorVersion == 1) {
            Slog.i(TAG, "luxCoefHighX=" + this.luxCoefHighX + " luxCoefHighY=" + this.luxCoefHighY + " luxCoefHighZ=" + this.luxCoefHighZ + " luxCoefHighIr=" + this.luxCoefHighIr + " luxCoefHighOffset=" + this.luxCoefHighOffset);
            Slog.i(TAG, "luxCoefLowX=" + this.luxCoefLowX + " luxCoefLowY=" + this.luxCoefLowY + " luxCoefLowZ=" + this.luxCoefLowZ + " luxCoefLowIr=" + this.luxCoefLowIr + " luxCoefLowOffset=" + this.luxCoefLowOffset);
            StringBuilder sb = new StringBuilder();
            sb.append("productionCalibrationX:");
            sb.append(this.productionCalibrationY);
            sb.append(" ");
            sb.append(this.productionCalibrationY);
            sb.append(" ");
            sb.append(this.productionCalibrationZ);
            sb.append(" ");
            sb.append(this.productionCalibrationIr1);
            Slog.i(TAG, sb.toString());
            Slog.i(TAG, "aTime=" + this.aTime + " aGain=" + this.aGain + " irBoundry=" + this.irBoundry);
        }
    }

    private void printRgbParam() {
        if (this.sensorVersion == 2) {
            Slog.i(TAG, "ratioIrGreen=" + this.ratioIrGreen);
            Slog.i(TAG, "luxCoefHighRed=" + this.luxCoefHighRed + " luxCoefHighGreen=" + this.luxCoefHighGreen + " luxCoefHighBlue=" + this.luxCoefHighBlue);
            Slog.i(TAG, "luxCoefLowRed=" + this.luxCoefLowRed + " luxCoefLowGreen=" + this.luxCoefLowGreen + " luxCoefLowBlue=" + this.luxCoefLowBlue);
        }
    }

    private void printRgbcwParam() {
        if (this.sensorVersion == 3) {
            Slog.i(TAG, "luxCoefHigh1=" + this.luxCoefHigh1 + " luxCoefHigh2=" + this.luxCoefHigh2 + " luxCoefHigh3=" + this.luxCoefHigh3 + " luxCoefHigh4=" + this.luxCoefHigh4 + " luxCoefHigh5=" + this.luxCoefHigh5);
            Slog.i(TAG, "luxCoefLow1=" + this.luxCoefLow1 + " luxCoefLow2=" + this.luxCoefLow2 + " luxCoefLow3=" + this.luxCoefLow3 + " luxCoefLow4=" + this.luxCoefLow4 + " luxCoefLow5=" + this.luxCoefLow5);
            Slog.i(TAG, "productionCalibration CRGBW=" + this.productionCalibrationC + " " + this.productionCalibrationR + " " + this.productionCalibrationG + " " + this.productionCalibrationB + " " + this.productionCalibrationW);
            StringBuilder sb = new StringBuilder();
            sb.append("aTimeRgbcw=");
            sb.append(this.aTimeRgbcw);
            sb.append(" aGainRgbcw=");
            sb.append(this.aGainRgbcw);
            Slog.i(TAG, sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("irBoundryRgbcw=");
            sb2.append(this.irBoundryRgbcw);
            Slog.i(TAG, sb2.toString());
        }
    }

    private void printControlParam() {
        Slog.i(TAG, "frontRateMillis=" + this.frontRateMillis + " backRateMillis=" + this.backRateMillis + " fusedRateMillis=" + this.fusedRateMillis);
        StringBuilder sb = new StringBuilder();
        sb.append("frontSensorStabilityThreshold:");
        sb.append(Arrays.toString(this.frontSensorStabilityThreshold));
        Slog.i(TAG, sb.toString());
        Slog.i(TAG, "backSensorStabilityThreshold:" + Arrays.toString(this.backSensorStabilityThreshold));
        Slog.i(TAG, "stabilizedProbabilityLut:" + Arrays.toString(this.stabilizedProbabilityLut));
        Slog.i(TAG, "backRoofThresh=" + this.backRoofThresh + " backFloorThresh=" + this.backFloorThresh + " darkRoomThresh=" + this.darkRoomThresh + " darkRoomDelta=" + this.darkRoomDelta + " backLuxDeviationThresh=" + this.backLuxDeviationThresh);
        Slog.i(TAG, "darkRoomThresh1=" + this.darkRoomThresh1 + " darkRoomThresh2=" + this.darkRoomThresh2 + " darkRoomThresh3=" + this.darkRoomThresh3 + " darkRoomDelta1=" + this.darkRoomDelta1 + " darkRoomDelta2=" + this.darkRoomDelta2);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("flashlightDetectionMode=");
        sb2.append(this.flashlightDetectionMode);
        sb2.append(" flashlightOffTimeThMs=");
        sb2.append(this.flashlightOffTimeThMs);
        Slog.i(TAG, sb2.toString());
        Slog.i(TAG, "backSensorTimeOutTh=" + this.backSensorTimeOutTh + " backSensorBypassCountMax=" + this.backSensorBypassCountMax);
        StringBuilder sb3 = new StringBuilder();
        sb3.append("isFilterOn=");
        sb3.append(this.isFilterOn);
        Slog.i(TAG, sb3.toString());
    }

    private void printSpectrumParam() {
        if (this.sensorVersion == 12) {
            Slog.i(TAG, "luxScale=" + this.luxScale + ", spectrumGain=" + this.spectrumGain + ", spectrumTime=" + this.spectrumTime + ", needNirCompensation=" + this.needNirCompensation);
            Slog.i(TAG, "specLowLuxThs=" + this.specLowLuxTh1 + ", " + this.specLowLuxTh2 + ", " + this.specLowLuxTh3 + ", " + this.specLowLuxTh4);
            StringBuilder sb = new StringBuilder();
            sb.append("spectrumParam=");
            sb.append(Arrays.toString(this.spectrumParam));
            Slog.i(TAG, sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("scaling=");
            sb2.append(Arrays.toString(this.scaling));
            Slog.i(TAG, sb2.toString());
            Slog.i(TAG, "ratioNir=" + Arrays.toString(this.ratioNir));
            Slog.i(TAG, "polyCoefs1=" + Arrays.toString(this.polyCoefs1));
            Slog.i(TAG, "polyCoefs2=" + Arrays.toString(this.polyCoefs2));
            Slog.i(TAG, "polyCoefs3=" + Arrays.toString(this.polyCoefs3));
            Slog.i(TAG, "polyCoefs4=" + Arrays.toString(this.polyCoefs4));
            Slog.i(TAG, "polyCoefs5=" + Arrays.toString(this.polyCoefs5));
        }
    }

    private void printFoldScreenSensorParam() {
        if (this.sensorVersion == 13) {
            Slog.i(TAG, "isOutwardLightSensor=" + this.isOutwardLightSensor);
            Slog.i(TAG, "isInwardFoldScreen=" + this.isInwardFoldScreen);
        }
    }
}
