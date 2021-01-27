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
    @Override // com.huawei.nb.notification.RecordObserver
    public final boolean isEqual(AManagedObject aManagedObject) {
        if (this.aiModel == null || aManagedObject == null || !(aManagedObject instanceof AiModel)) {
            return false;
        }
        return Objects.equals(((AiModel) aManagedObject).getOrigin_id(), this.aiModel.getOrigin_id());
    }
}
