package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$ciVvgjk4nG5c73re_PC3wYunx6s  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$ciVvgjk4nG5c73re_PC3wYunx6s implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$ciVvgjk4nG5c73re_PC3wYunx6s INSTANCE = new $$Lambda$ModeFunctionUtil$ciVvgjk4nG5c73re_PC3wYunx6s();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$ciVvgjk4nG5c73re_PC3wYunx6s() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addFaceDetectModeCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
