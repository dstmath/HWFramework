package com.huawei.nb.notification;

import com.huawei.nb.client.callback.CallbackManager;
import com.huawei.nb.client.callback.SubscribeCallback;
import java.util.function.Function;

final /* synthetic */ class DSLocalObservable$$Lambda$1 implements Function {
    static final Function $instance = new DSLocalObservable$$Lambda$1();

    private DSLocalObservable$$Lambda$1() {
    }

    public Object apply(Object obj) {
        return new SubscribeCallback((CallbackManager) obj);
    }
}
