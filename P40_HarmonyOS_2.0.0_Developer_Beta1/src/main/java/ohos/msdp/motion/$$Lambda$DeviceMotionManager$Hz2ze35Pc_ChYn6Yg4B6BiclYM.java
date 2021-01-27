package ohos.msdp.motion;

import java.util.function.BiConsumer;

/* renamed from: ohos.msdp.motion.-$$Lambda$DeviceMotionManager$Hz2ze35Pc_ChY-n6Yg4B6BiclYM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DeviceMotionManager$Hz2ze35Pc_ChYn6Yg4B6BiclYM implements BiConsumer {
    public static final /* synthetic */ $$Lambda$DeviceMotionManager$Hz2ze35Pc_ChYn6Yg4B6BiclYM INSTANCE = new $$Lambda$DeviceMotionManager$Hz2ze35Pc_ChYn6Yg4B6BiclYM();

    private /* synthetic */ $$Lambda$DeviceMotionManager$Hz2ze35Pc_ChYn6Yg4B6BiclYM() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        DeviceMotionManager.MOTION_TYPE_A2H_MAP.put((Integer) obj2, (Integer) obj);
    }
}
