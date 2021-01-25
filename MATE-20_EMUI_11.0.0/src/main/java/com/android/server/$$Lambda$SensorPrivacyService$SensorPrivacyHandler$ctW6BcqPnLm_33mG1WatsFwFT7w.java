package com.android.server;

import com.android.server.SensorPrivacyService;
import java.util.function.Consumer;

/* renamed from: com.android.server.-$$Lambda$SensorPrivacyService$SensorPrivacyHandler$ctW6BcqPnLm_33mG1WatsFwFT7w  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SensorPrivacyService$SensorPrivacyHandler$ctW6BcqPnLm_33mG1WatsFwFT7w implements Consumer {
    public static final /* synthetic */ $$Lambda$SensorPrivacyService$SensorPrivacyHandler$ctW6BcqPnLm_33mG1WatsFwFT7w INSTANCE = new $$Lambda$SensorPrivacyService$SensorPrivacyHandler$ctW6BcqPnLm_33mG1WatsFwFT7w();

    private /* synthetic */ $$Lambda$SensorPrivacyService$SensorPrivacyHandler$ctW6BcqPnLm_33mG1WatsFwFT7w() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((SensorPrivacyService.SensorPrivacyServiceImpl) obj).persistSensorPrivacyState();
    }
}
