package com.huawei.agpengine.components;

import com.huawei.agpengine.Component;

public class MorphComponent implements Component {
    private long mMorphTargets;

    public MorphComponent(long morphTargets) {
        this.mMorphTargets = morphTargets;
    }

    public long getMorphTargets() {
        return this.mMorphTargets;
    }
}
