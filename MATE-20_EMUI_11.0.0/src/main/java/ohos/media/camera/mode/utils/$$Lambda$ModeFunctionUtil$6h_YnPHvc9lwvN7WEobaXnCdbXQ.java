package ohos.media.camera.mode.utils;

import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.utils.ModeFunctionUtil;

/* renamed from: ohos.media.camera.mode.utils.-$$Lambda$ModeFunctionUtil$6h_YnPHvc9lwvN7WEobaXnCdbXQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ModeFunctionUtil$6h_YnPHvc9lwvN7WEobaXnCdbXQ implements ModeFunctionUtil.Processor {
    public static final /* synthetic */ $$Lambda$ModeFunctionUtil$6h_YnPHvc9lwvN7WEobaXnCdbXQ INSTANCE = new $$Lambda$ModeFunctionUtil$6h_YnPHvc9lwvN7WEobaXnCdbXQ();

    private /* synthetic */ $$Lambda$ModeFunctionUtil$6h_YnPHvc9lwvN7WEobaXnCdbXQ() {
    }

    @Override // ohos.media.camera.mode.utils.ModeFunctionUtil.Processor
    public final void process(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        ModeFunctionUtil.addSensorHdrCapability(modeAbilityImpl, cameraAbilityImpl);
    }
}
