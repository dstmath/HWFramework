package com.huawei.agpengine.impl;

import android.util.Log;
import com.huawei.agpengine.Engine;
import java.util.Optional;

public final class AgpEngineFactory {
    private static final String TAG = "core: AgpEngineFactory";

    private AgpEngineFactory() {
    }

    public static Optional<Engine> createEngine() {
        try {
            System.loadLibrary("AGPJavaApi");
            return Optional.of(new EngineImpl());
        } catch (UnsatisfiedLinkError ex) {
            Log.e(TAG, "Loading native AGPEngine failed: ", ex);
            return Optional.empty();
        }
    }
}
