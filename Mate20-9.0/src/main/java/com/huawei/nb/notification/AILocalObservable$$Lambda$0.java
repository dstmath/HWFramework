package com.huawei.nb.notification;

import com.huawei.nb.client.callback.AISubscribeCallback;
import com.huawei.nb.client.callback.CallbackManager;
import java.util.function.Function;

final /* synthetic */ class AILocalObservable$$Lambda$0 implements Function {
    static final Function $instance = new AILocalObservable$$Lambda$0();

    private AILocalObservable$$Lambda$0() {
    }

    public Object apply(Object obj) {
        return new AISubscribeCallback((CallbackManager) obj);
    }
}
