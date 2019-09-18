package android.hardware.camera2.impl;

import android.hardware.camera2.impl.CameraDeviceImpl;
import java.util.function.BiConsumer;

/* renamed from: android.hardware.camera2.impl.-$$Lambda$CameraDeviceImpl$CameraDeviceCallbacks$Sm85frAzwGZVMAK-NE_gwckYXVQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$CameraDeviceImpl$CameraDeviceCallbacks$Sm85frAzwGZVMAKNE_gwckYXVQ implements BiConsumer {
    public static final /* synthetic */ $$Lambda$CameraDeviceImpl$CameraDeviceCallbacks$Sm85frAzwGZVMAKNE_gwckYXVQ INSTANCE = new $$Lambda$CameraDeviceImpl$CameraDeviceCallbacks$Sm85frAzwGZVMAKNE_gwckYXVQ();

    private /* synthetic */ $$Lambda$CameraDeviceImpl$CameraDeviceCallbacks$Sm85frAzwGZVMAKNE_gwckYXVQ() {
    }

    public final void accept(Object obj, Object obj2) {
        ((CameraDeviceImpl.CameraDeviceCallbacks) obj).notifyError(((Integer) obj2).intValue());
    }
}
