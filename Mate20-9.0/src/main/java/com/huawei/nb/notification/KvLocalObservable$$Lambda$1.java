package com.huawei.nb.notification;

import com.huawei.nb.client.callback.CallbackManager;
import com.huawei.nb.client.callback.KvSubscribeCallback;
import java.util.function.Function;

final /* synthetic */ class KvLocalObservable$$Lambda$1 implements Function {
    static final Function $instance = new KvLocalObservable$$Lambda$1();

    private KvLocalObservable$$Lambda$1() {
    }

    public Object apply(Object obj) {
        return new KvSubscribeCallback((CallbackManager) obj);
    }
}
