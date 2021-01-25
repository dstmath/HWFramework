package com.android.server.display;

import android.hardware.SensorEvent;
import android.util.Slog;

public class HwDualSensorUtils {
    private static final float ROUND_VALUE = 0.5f;
    private static final String TAG = "HwDualSensorEventListenerImpl";

    private static double[] getNirCompensationIfNeeded(SensorEvent raw, HwDualSensorData data) {
        if (!data.needNirCompensation) {
            return new double[]{(double) raw.values[0], (double) raw.values[1], (double) raw.values[2], (double) raw.values[3], (double) raw.values[4], (double) raw.values[5], (double) raw.values[6], (double) raw.values[7], (double) raw.values[8], (double) raw.values[9]};
        }
        double sum = 0.0d;
        for (int i = 0; i < 8; i++) {
            sum += ((double) raw.values[i]) / data.scaling[i];
        }
        double nirClear = ((double) raw.values[9]) - sum;
        double[] out = new double[10];
        for (int i2 = 0; i2 < 8; i2++) {
            out[i2] = ((double) raw.values[i2]) - ((((double) ((raw.values[8] * 0.15f) / raw.values[9])) * data.ratioNir[i2]) * nirClear);
        }
        out[8] = (double) raw.values[8];
        out[9] = (double) raw.values[9];
        return out;
    }

    private static double getPolyVal(double[] coef, double lux) {
        return (coef[0] * lux * lux * lux) + (coef[1] * lux * lux) + (coef[2] * lux) + coef[3];
    }

    static int[] convertSpectrumToLux(SensorEvent event, HwDualSensorData data) {
        double coefImp;
        int[] retArr = {0, 0};
        if (event == null || data == null || event.values.length < 15) {
            Slog.e(TAG, "convertSpectrumToLux: event || data is invalid!");
            return retArr;
        } else if (event.values[9] == 0.0f) {
            return retArr;
        } else {
            double[] raw = getNirCompensationIfNeeded(event, data);
            double luxValue = 0.0d;
            for (int i = 0; i < 10; i++) {
                luxValue += data.spectrumParam[i] * raw[i];
            }
            double luxValue2 = ((luxValue / data.spectrumGain) / data.spectrumTime) * ((double) data.luxScale);
            if (luxValue2 < ((double) data.specLowLuxTh1)) {
                coefImp = getPolyVal(data.polyCoefs1, luxValue2);
            } else if (luxValue2 >= ((double) data.specLowLuxTh1) && luxValue2 < ((double) data.specLowLuxTh2)) {
                coefImp = getPolyVal(data.polyCoefs2, luxValue2);
            } else if (luxValue2 >= ((double) data.specLowLuxTh2) && luxValue2 < ((double) data.specLowLuxTh3)) {
                coefImp = getPolyVal(data.polyCoefs3, luxValue2);
            } else if (luxValue2 < ((double) data.specLowLuxTh3) || luxValue2 >= ((double) data.specLowLuxTh4)) {
                coefImp = getPolyVal(data.polyCoefs5, luxValue2);
            } else {
                coefImp = getPolyVal(data.polyCoefs4, luxValue2);
            }
            int lux = (int) (0.5d + (luxValue2 * coefImp));
            if (lux < 0) {
                lux = 0;
            }
            retArr[0] = lux;
            return retArr;
        }
    }

    static int[] convertRgbcw2LuxAndCct(SensorEvent event, HwDualSensorData data) {
        float luxF;
        int[] retArr = {0, 0};
        if (event == null || data == null || event.values.length < 15) {
            Slog.e(TAG, "convertRgbcw2LuxAndCct: event || data is invalid!");
            return retArr;
        }
        float cRaw = event.values[0];
        float rRaw = event.values[1];
        float gRaw = event.values[2];
        float bRaw = event.values[3];
        float wRaw = event.values[4];
        float temp = data.aTimeRgbcw * data.aGainRgbcw;
        if (Float.compare(temp, 0.0f) == 0) {
            Slog.d(TAG, " data.aTimeRgbcw=" + data.aTimeRgbcw + " data.aGainRgbcw=" + data.aGainRgbcw);
            return retArr;
        }
        float cNorm = ((data.productionCalibrationC * 12800.0f) * cRaw) / temp;
        float rNorm = ((data.productionCalibrationR * 12800.0f) * rRaw) / temp;
        float gNorm = ((data.productionCalibrationG * 12800.0f) * gRaw) / temp;
        float bNorm = ((data.productionCalibrationB * 12800.0f) * bRaw) / temp;
        float wNorm = ((data.productionCalibrationW * 12800.0f) * wRaw) / temp;
        int lux = 0;
        if (Float.compare(wNorm, 0.0f) != 0) {
            if (((wNorm * 2.6f) - cNorm) / (2.6f * wNorm) > data.irBoundryRgbcw) {
                luxF = (data.luxCoefHigh1 * cNorm) + (data.luxCoefHigh2 * rNorm) + (data.luxCoefHigh3 * gNorm) + (data.luxCoefHigh4 * bNorm) + (data.luxCoefHigh5 * wNorm);
            } else {
                luxF = (data.luxCoefLow5 * wNorm) + (data.luxCoefLow1 * cNorm) + (data.luxCoefLow2 * rNorm) + (data.luxCoefLow3 * gNorm) + (data.luxCoefLow4 * bNorm);
            }
            lux = (int) (0.5f + luxF);
            if (lux < 0) {
                lux = 0;
            }
        }
        retArr[0] = lux;
        return retArr;
    }

    static int[] convertXyzToLuxAndCct(SensorEvent event, HwDualSensorData data) {
        float luxF;
        int[] retArr = {0, 0};
        if (event == null || data == null || event.values.length < 15) {
            Slog.e(TAG, "convertXyzToLuxAndCct: event || data is invalid!");
            return retArr;
        }
        float xRaw = event.values[0];
        float yRaw = event.values[1];
        float zRaw = event.values[2];
        float irRaw = event.values[3];
        float temp = data.aTime * data.aGain;
        if (Float.compare(temp, 0.0f) == 0) {
            Slog.d(TAG, " data.aTime=" + data.aTime + " data.aGain=" + data.aGain);
            return retArr;
        }
        float xNorm = ((data.productionCalibrationX * 1600.128f) * xRaw) / temp;
        float yNorm = ((data.productionCalibrationY * 1600.128f) * yRaw) / temp;
        float zNorm = ((data.productionCalibrationZ * 1600.128f) * zRaw) / temp;
        float ir1Norm = ((data.productionCalibrationIr1 * 1600.128f) * irRaw) / temp;
        int lux = 0;
        if (Float.compare(yNorm, 0.0f) != 0) {
            if (ir1Norm / yNorm > data.irBoundry) {
                luxF = (data.luxCoefHighX * xNorm) + (data.luxCoefHighY * yNorm) + (data.luxCoefHighZ * zNorm) + (data.luxCoefHighIr * ir1Norm) + data.luxCoefHighOffset;
            } else {
                luxF = data.luxCoefLowOffset + (data.luxCoefLowX * xNorm) + (data.luxCoefLowY * yNorm) + (data.luxCoefLowZ * zNorm) + (data.luxCoefLowIr * ir1Norm);
            }
            lux = (int) (0.5f + luxF);
        }
        retArr[0] = lux;
        return retArr;
    }

    static int[] convertRgbToLuxAndCct(SensorEvent event, HwDualSensorData data) {
        float luxF;
        int[] retArr = {0, 0};
        if (event == null || data == null || event.values.length < 15) {
            Slog.e(TAG, "convertRgbToLuxAndCct: event || data is invalid!");
            return retArr;
        }
        float red = event.values[0];
        float green = event.values[1];
        float blue = event.values[2];
        float iR = event.values[3];
        if (Float.compare(green, 0.0f) <= 0) {
            return retArr;
        }
        if (Float.compare(iR / green, 0.5f) > 0) {
            luxF = (data.luxCoefHighRed * red) + (data.luxCoefHighGreen * green) + (data.luxCoefHighBlue * blue);
        } else {
            luxF = (data.luxCoefLowRed * red) + (data.luxCoefLowGreen * green) + (data.luxCoefLowBlue * blue);
        }
        retArr[0] = (int) (0.5f + luxF);
        return retArr;
    }

    static int[] convertNormalizedTypeToLuxAndCct(float rawLux, float rawCct, HwDualSensorData data) {
        int[] retArr = {0, 0};
        if (data == null) {
            Slog.e(TAG, "convertNormalizedTypeToLuxAndCct: data || data is invalid!");
            return retArr;
        }
        retArr[0] = (int) ((rawLux / 100.0f) + 0.5f);
        retArr[1] = (int) (0.5f + rawCct);
        if (retArr[0] < 0) {
            retArr[0] = 0;
        }
        if (retArr[1] < 0) {
            retArr[1] = 0;
        }
        return retArr;
    }

    static int[] convertOutwardSensorToLux(SensorEvent event, HwDualSensorData data) {
        int[] retArr = {0, 0};
        if (event == null || data == null || event.values == null || event.values.length < 15) {
            Slog.e(TAG, "convertOutwardSensorToLux: event || data is invalid!");
            return retArr;
        }
        int lux = (int) (0.5f + event.values[0]);
        if (lux < 0) {
            lux = 0;
        }
        retArr[0] = lux;
        return retArr;
    }
}
