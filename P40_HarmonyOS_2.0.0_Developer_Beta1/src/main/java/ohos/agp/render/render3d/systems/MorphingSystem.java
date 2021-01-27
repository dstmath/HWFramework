package ohos.agp.render.render3d.systems;

public interface MorphingSystem {
    int getTargetCount(long j);

    String[] getTargetNames(long j);

    float[] getTargetWeights(long j);

    void setTargetNames(long j, String[] strArr);

    void setTargetWeights(long j, float[] fArr);
}
