package ohos.agp.render.render3d.impl;

import ohos.agp.render.render3d.gltf.GltfExporter;

/* access modifiers changed from: package-private */
public final class GltfExporterImpl implements GltfExporter {
    private static final String TAG = "core: GltfExporterImpl";
    private final EngineImpl mEngine;

    GltfExporterImpl(EngineImpl engineImpl) {
        this.mEngine = engineImpl;
    }

    @Override // ohos.agp.render.render3d.gltf.GltfExporter
    public boolean exportGltf(String str) {
        CoreEngine engine = this.mEngine.getAgpContext().getEngine();
        CoreGraphicsContext graphicsContext = this.mEngine.getAgpContext().getGraphicsContext();
        return Core.saveGltf(engine, graphicsContext.getEcs(), graphicsContext.getResourceCreator(), str);
    }
}
