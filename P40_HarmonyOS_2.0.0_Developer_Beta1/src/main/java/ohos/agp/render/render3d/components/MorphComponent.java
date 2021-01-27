package ohos.agp.render.render3d.components;

import ohos.agp.render.render3d.Component;

public class MorphComponent implements Component {
    private long mMorphTargets;

    public long getMorphTargets() {
        return this.mMorphTargets;
    }

    public void setMorphTargets(long j) {
        this.mMorphTargets = j;
    }
}
