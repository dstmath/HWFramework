package ohos.media.camera.device;

import java.util.function.Predicate;

/* renamed from: ohos.media.camera.device.-$$Lambda$CameraManager$CameraInfoAssistant$pXQXtKAgYNZmNaRw_xa3pXRKyvU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$CameraManager$CameraInfoAssistant$pXQXtKAgYNZmNaRw_xa3pXRKyvU implements Predicate {
    public static final /* synthetic */ $$Lambda$CameraManager$CameraInfoAssistant$pXQXtKAgYNZmNaRw_xa3pXRKyvU INSTANCE = new $$Lambda$CameraManager$CameraInfoAssistant$pXQXtKAgYNZmNaRw_xa3pXRKyvU();

    private /* synthetic */ $$Lambda$CameraManager$CameraInfoAssistant$pXQXtKAgYNZmNaRw_xa3pXRKyvU() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return CameraManager.cameraAbilityAssistant.isLogicalCamera((String) obj);
    }
}
