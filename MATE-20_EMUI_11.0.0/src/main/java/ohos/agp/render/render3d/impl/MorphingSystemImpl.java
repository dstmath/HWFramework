package ohos.agp.render.render3d.impl;

import java.util.Optional;
import ohos.agp.render.render3d.systems.MorphingSystem;

/* access modifiers changed from: package-private */
public class MorphingSystemImpl implements MorphingSystem {
    private final CoreMorphingSystem mMorphingSystem;

    private MorphingSystemImpl(CoreMorphingSystem coreMorphingSystem) {
        this.mMorphingSystem = coreMorphingSystem;
    }

    static Optional<MorphingSystemImpl> getMorpingSystem(EngineImpl engineImpl) {
        CoreSystem system = engineImpl.getAgpContext().getGraphicsContext().getEcs().getSystem("MorphingSystem");
        if (system instanceof CoreMorphingSystem) {
            return Optional.of(new MorphingSystemImpl((CoreMorphingSystem) system));
        }
        return Optional.empty();
    }

    @Override // ohos.agp.render.render3d.systems.MorphingSystem
    public int getTargetCount(long j) {
        return (int) this.mMorphingSystem.getTargetCount(j);
    }

    @Override // ohos.agp.render.render3d.systems.MorphingSystem
    public String[] getTargetNames(long j) {
        String[] strArr = new String[getTargetCount(j)];
        CoreStringViewArrayView targetNames = this.mMorphingSystem.getTargetNames(j);
        if (targetNames.size() == ((long) strArr.length)) {
            for (int i = 0; i < strArr.length; i++) {
                strArr[i] = targetNames.get((long) i);
            }
            return strArr;
        }
        throw new IllegalStateException();
    }

    @Override // ohos.agp.render.render3d.systems.MorphingSystem
    public float[] getTargetWeights(long j) {
        float[] fArr = new float[getTargetCount(j)];
        CoreFloatArrayView targetWeights = this.mMorphingSystem.getTargetWeights(j);
        if (targetWeights.size() == ((long) fArr.length)) {
            for (int i = 0; i < fArr.length; i++) {
                fArr[i] = targetWeights.get((long) i);
            }
            return fArr;
        }
        throw new IllegalStateException();
    }

    @Override // ohos.agp.render.render3d.systems.MorphingSystem
    public void setTargetNames(long j, String[] strArr) {
        this.mMorphingSystem.setTargetNamesArray(j, strArr, strArr.length);
    }

    @Override // ohos.agp.render.render3d.systems.MorphingSystem
    public void setTargetWeights(long j, float[] fArr) {
        this.mMorphingSystem.setTargetWeightsArray(j, fArr, fArr.length);
    }
}
