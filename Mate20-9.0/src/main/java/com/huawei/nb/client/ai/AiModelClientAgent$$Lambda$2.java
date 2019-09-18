package com.huawei.nb.client.ai;

import com.huawei.nb.client.callback.AIFetchCallback;
import com.huawei.nb.client.callback.CallbackManager;
import java.util.function.Function;

final /* synthetic */ class AiModelClientAgent$$Lambda$2 implements Function {
    static final Function $instance = new AiModelClientAgent$$Lambda$2();

    private AiModelClientAgent$$Lambda$2() {
    }

    public Object apply(Object obj) {
        return new AIFetchCallback((CallbackManager) obj);
    }
}
