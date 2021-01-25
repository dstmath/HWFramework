package com.huawei.agpengine.impl;

import com.huawei.agpengine.systems.MorphingSystem;
import java.util.Optional;

/* access modifiers changed from: package-private */
public class MorphingSystemImpl implements MorphingSystem {
    private final CoreMorphingSystem mMorphingSystem;

    private MorphingSystemImpl(CoreMorphingSystem nativeSystem) {
        this.mMorphingSystem = nativeSystem;
    }

    static Optional<MorphingSystemImpl> getMorpingSystem(EngineImpl engine) {
        CoreSystem system = engine.getAgpContext().getGraphicsContext().getEcs().getSystem("MorphingSystem");
        if (system instanceof CoreMorphingSystem) {
            return Optional.of(new MorphingSystemImpl((CoreMorphingSystem) system));
        }
        return Optional.empty();
    }

    @Override // com.huawei.agpengine.systems.MorphingSystem
    public int getTargetCount(long handle) {
        long data = this.mMorphingSystem.rlock(handle);
        if (data == 0) {
            return 0;
        }
        int result = (int) this.mMorphingSystem.getTargetCount(data);
        this.mMorphingSystem.runlock(handle);
        return result;
    }

    @Override // com.huawei.agpengine.systems.MorphingSystem
    public String[] getTargetNames(long handle) {
        long data = this.mMorphingSystem.rlock(handle);
        if (data == 0) {
            return new String[0];
        }
        int count = (int) this.mMorphingSystem.getTargetCount(data);
        String[] resultNames = new String[count];
        for (int i = 0; i < count; i++) {
            resultNames[i] = this.mMorphingSystem.getTargetName(data, i);
        }
        this.mMorphingSystem.runlock(handle);
        return resultNames;
    }

    @Override // com.huawei.agpengine.systems.MorphingSystem
    public float[] getTargetWeights(long handle) {
        long data = this.mMorphingSystem.rlock(handle);
        if (data == 0) {
            return new float[0];
        }
        int count = (int) this.mMorphingSystem.getTargetCount(data);
        float[] resultWeights = new float[count];
        for (int i = 0; i < count; i++) {
            resultWeights[i] = this.mMorphingSystem.getTargetWeight(data, i);
        }
        this.mMorphingSystem.runlock(handle);
        return resultWeights;
    }

    @Override // com.huawei.agpengine.systems.MorphingSystem
    public void setTargetNames(long handle, String[] names) {
        int length = names.length;
        long data = this.mMorphingSystem.wlock(handle);
        if (data != 0) {
            this.mMorphingSystem.setTargetNamesArray(data, names, length);
            this.mMorphingSystem.wunlock(handle);
        }
    }

    @Override // com.huawei.agpengine.systems.MorphingSystem
    public void setTargetWeights(long handle, float[] weights) {
        int length = weights.length;
        long data = this.mMorphingSystem.wlock(handle);
        if (data != 0) {
            this.mMorphingSystem.setTargetWeightsArray(data, weights, length);
            this.mMorphingSystem.wunlock(handle);
        }
    }
}
