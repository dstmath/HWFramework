package com.huawei.nb.client.ai;

import com.huawei.nb.ai.AiModelResponse;
import java.util.function.Consumer;

final /* synthetic */ class AiModelClientAgent$$Lambda$0 implements Consumer {
    private final AiModelClientAgent arg$1;

    AiModelClientAgent$$Lambda$0(AiModelClientAgent aiModelClientAgent) {
        this.arg$1 = aiModelClientAgent;
    }

    public void accept(Object obj) {
        this.arg$1.bridge$lambda$0$AiModelClientAgent((AiModelResponse) obj);
    }
}
