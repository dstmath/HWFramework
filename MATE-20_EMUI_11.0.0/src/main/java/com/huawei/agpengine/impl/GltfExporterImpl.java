package com.huawei.agpengine.impl;

import com.huawei.agpengine.gltf.GltfExporter;

/* access modifiers changed from: package-private */
public final class GltfExporterImpl implements GltfExporter {
    private static final String TAG = "core: GltfExporterImpl";
    private EngineImpl mEngine;

    GltfExporterImpl(EngineImpl engine) {
        this.mEngine = engine;
    }

    @Override // com.huawei.agpengine.gltf.GltfExporter
    public boolean exportGltf(String uri) {
        CoreGraphicsContext nativeContext = this.mEngine.getAgpContext().getGraphicsContext();
        return nativeContext.getGltf().saveGltf(nativeContext.getEcs(), uri);
    }
}
