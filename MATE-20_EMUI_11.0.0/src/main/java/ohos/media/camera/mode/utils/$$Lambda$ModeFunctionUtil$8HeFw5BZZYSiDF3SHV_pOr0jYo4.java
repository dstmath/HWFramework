package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$8HeFw5BZZYSiDF3SHV_pOr0jYo4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$8HeFw5BZZYSiDF3SHV_pOr0jYo4 implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$8HeFw5BZZYSiDF3SHV_pOr0jYo4 INSTANCE = new $$Lambda$ModeFunctionUtil$8HeFw5BZZYSiDF3SHV_pOr0jYo4();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$8HeFw5BZZYSiDF3SHV_pOr0jYo4() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addLocationCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
