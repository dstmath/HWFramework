package ohos.msdp.devicestatus;

import java.util.function.BiConsumer;

/* renamed from: ohos.msdp.devicestatus.-$$Lambda$DeviceStatusManager$xdYLlkcQ-bFh6ndDBOtbBovxTqM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DeviceStatusManager$xdYLlkcQbFh6ndDBOtbBovxTqM implements BiConsumer {
    public static final /* synthetic */ $$Lambda$DeviceStatusManager$xdYLlkcQbFh6ndDBOtbBovxTqM INSTANCE = new $$Lambda$DeviceStatusManager$xdYLlkcQbFh6ndDBOtbBovxTqM();

    private /* synthetic */ $$Lambda$DeviceStatusManager$xdYLlkcQbFh6ndDBOtbBovxTqM() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        DeviceStatusManager.DEVICE_STATUS_A2H_TRANSFER_MAP.put((String) obj2, (Integer) obj);
    }
}
