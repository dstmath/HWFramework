package com.huawei.nb.client.ai;

import com.huawei.nb.model.aimodel.AiModel;
import com.huawei.nb.notification.RecordObserver;
import com.huawei.odmf.core.AManagedObject;
import java.util.Objects;

public abstract class AiModelObserver extends RecordObserver {
    private AiModel aiModel;

    public AiModelObserver(AiModel aiModel2) {
        this.aiModel = aiModel2;
    }

    /* access modifiers changed from: protected */
    public final boolean isEqual(AManagedObject target) {
        if (this.aiModel == null || target == null || !(target instanceof AiModel)) {
            return false;
        }
        return Objects.equals(((AiModel) target).getOrigin_id(), this.aiModel.getOrigin_id());
    }
}
