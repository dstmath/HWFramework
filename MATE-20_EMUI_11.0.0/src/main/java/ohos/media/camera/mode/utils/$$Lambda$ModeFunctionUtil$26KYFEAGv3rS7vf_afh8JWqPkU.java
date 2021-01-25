package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$26KYFEAGv3rS-7vf_afh8JWqPkU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$26KYFEAGv3rS7vf_afh8JWqPkU implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$26KYFEAGv3rS7vf_afh8JWqPkU INSTANCE = new $$Lambda$ModeFunctionUtil$26KYFEAGv3rS7vf_afh8JWqPkU();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$26KYFEAGv3rS7vf_afh8JWqPkU() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addSmartCaptureCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
