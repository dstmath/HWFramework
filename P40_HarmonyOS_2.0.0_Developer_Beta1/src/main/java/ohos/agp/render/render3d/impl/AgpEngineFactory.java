package ohos.agp.render.render3d.impl;

import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.render.render3d.Engine;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class AgpEngineFactory {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "core: AgpEngineFactory");

    private AgpEngineFactory() {
    }

    public static Optional<Engine> createEngine() {
        try {
            System.loadLibrary("agpjavaapi.z");
            return Optional.of(new EngineImpl());
        } catch (UnsatisfiedLinkError e) {
            HiLog.error(LABEL, "Loading native AGPEngine failed: %{public}s.", new Object[]{e.getMessage()});
            return Optional.empty();
        }
    }
}
