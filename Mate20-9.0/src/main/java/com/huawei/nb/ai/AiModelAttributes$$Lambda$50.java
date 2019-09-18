package com.huawei.nb.ai;

import com.huawei.nb.model.aimodel.AiModel;
import java.util.function.Supplier;

final /* synthetic */ class AiModelAttributes$$Lambda$50 implements Supplier {
    private final AiModel arg$1;

    private AiModelAttributes$$Lambda$50(AiModel aiModel) {
        this.arg$1 = aiModel;
    }

    static Supplier get$Lambda(AiModel aiModel) {
        return new AiModelAttributes$$Lambda$50(aiModel);
    }

    public Object get() {
        return this.arg$1.getCloud_update_policy();
    }
}
