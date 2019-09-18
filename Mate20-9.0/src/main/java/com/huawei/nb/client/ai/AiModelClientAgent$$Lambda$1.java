package com.huawei.nb.client.ai;

import com.huawei.nb.ai.AiModelRequest;
import java.util.function.Consumer;

final /* synthetic */ class AiModelClientAgent$$Lambda$1 implements Consumer {
    private final AiModelClientAgent arg$1;

    AiModelClientAgent$$Lambda$1(AiModelClientAgent aiModelClientAgent) {
        this.arg$1 = aiModelClientAgent;
    }

    public void accept(Object obj) {
        this.arg$1.bridge$lambda$1$AiModelClientAgent((AiModelRequest) obj);
    }
}
