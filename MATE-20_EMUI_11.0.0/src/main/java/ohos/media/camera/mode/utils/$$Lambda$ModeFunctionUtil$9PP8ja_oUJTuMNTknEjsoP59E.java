package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$9PP8ja_oUJ-TuMNTkn-EjsoP59E  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$9PP8ja_oUJTuMNTknEjsoP59E implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$9PP8ja_oUJTuMNTknEjsoP59E INSTANCE = new $$Lambda$ModeFunctionUtil$9PP8ja_oUJTuMNTknEjsoP59E();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$9PP8ja_oUJTuMNTknEjsoP59E() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addVideoAiMovieCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
