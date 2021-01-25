package com.android.server.display.whitebalance;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.TypedValue;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.display.whitebalance.AmbientFilter;
import com.android.server.display.whitebalance.AmbientSensor;

public class DisplayWhiteBalanceFactory {
    private static final String BRIGHTNESS_FILTER_TAG = "AmbientBrightnessFilter";
    private static final String COLOR_TEMPERATURE_FILTER_TAG = "AmbientColorTemperatureFilter";

    public static DisplayWhiteBalanceController create(Handler handler, SensorManager sensorManager, Resources resources) {
        AmbientSensor.AmbientBrightnessSensor brightnessSensor = createBrightnessSensor(handler, sensorManager, resources);
        AmbientFilter brightnessFilter = createBrightnessFilter(resources);
        AmbientSensor.AmbientColorTemperatureSensor colorTemperatureSensor = createColorTemperatureSensor(handler, sensorManager, resources);
        DisplayWhiteBalanceController controller = new DisplayWhiteBalanceController(brightnessSensor, brightnessFilter, colorTemperatureSensor, createColorTemperatureFilter(resources), createThrottler(resources), getFloat(resources, 17105056), getFloat(resources, 17105057), getFloatArray(resources, 17236011), getFloatArray(resources, 17236014));
        brightnessSensor.setCallbacks(controller);
        colorTemperatureSensor.setCallbacks(controller);
        return controller;
    }

    private DisplayWhiteBalanceFactory() {
    }

    @VisibleForTesting
    public static AmbientSensor.AmbientBrightnessSensor createBrightnessSensor(Handler handler, SensorManager sensorManager, Resources resources) {
        return new AmbientSensor.AmbientBrightnessSensor(handler, sensorManager, resources.getInteger(17694789));
    }

    @VisibleForTesting
    static AmbientFilter createBrightnessFilter(Resources resources) {
        int horizon = resources.getInteger(17694788);
        float intercept = getFloat(resources, 17105054);
        if (!Float.isNaN(intercept)) {
            return new AmbientFilter.WeightedMovingAverageAmbientFilter(BRIGHTNESS_FILTER_TAG, horizon, intercept);
        }
        throw new IllegalArgumentException("missing configurations: expected config_displayWhiteBalanceBrightnessFilterIntercept");
    }

    @VisibleForTesting
    public static AmbientSensor.AmbientColorTemperatureSensor createColorTemperatureSensor(Handler handler, SensorManager sensorManager, Resources resources) {
        return new AmbientSensor.AmbientColorTemperatureSensor(handler, sensorManager, resources.getString(17039840), resources.getInteger(17694794));
    }

    private static AmbientFilter createColorTemperatureFilter(Resources resources) {
        int horizon = resources.getInteger(17694791);
        float intercept = getFloat(resources, 17105055);
        if (!Float.isNaN(intercept)) {
            return new AmbientFilter.WeightedMovingAverageAmbientFilter(COLOR_TEMPERATURE_FILTER_TAG, horizon, intercept);
        }
        throw new IllegalArgumentException("missing configurations: expected config_displayWhiteBalanceColorTemperatureFilterIntercept");
    }

    private static DisplayWhiteBalanceThrottler createThrottler(Resources resources) {
        return new DisplayWhiteBalanceThrottler(resources.getInteger(17694795), resources.getInteger(17694796), getFloatArray(resources, 17236012), getFloatArray(resources, 17236017), getFloatArray(resources, 17236013));
    }

    private static float getFloat(Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.getValue(id, value, true);
        if (value.type != 4) {
            return Float.NaN;
        }
        return value.getFloat();
    }

    private static float[] getFloatArray(Resources resources, int id) {
        TypedArray array = resources.obtainTypedArray(id);
        try {
            if (array.length() == 0) {
                return null;
            }
            float[] values = new float[array.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = array.getFloat(i, Float.NaN);
                if (Float.isNaN(values[i])) {
                    array.recycle();
                    return null;
                }
            }
            array.recycle();
            return values;
        } finally {
            array.recycle();
        }
    }
}
