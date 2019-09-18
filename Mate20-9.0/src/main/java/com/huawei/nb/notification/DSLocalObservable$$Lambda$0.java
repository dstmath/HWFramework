package com.huawei.nb.notification;

import com.huawei.nb.client.callback.CallbackManager;
import com.huawei.nb.client.callback.SubscribeCallback;
import java.util.function.Function;

final /* synthetic */ class DSLocalObservable$$Lambda$0 implements Function {
    static final Function $instance = new DSLocalObservable$$Lambda$0();

    private DSLocalObservable$$Lambda$0() {
    }

    public Object apply(Object obj) {
        return new SubscribeCallback((CallbackManager) obj);
    }
}
