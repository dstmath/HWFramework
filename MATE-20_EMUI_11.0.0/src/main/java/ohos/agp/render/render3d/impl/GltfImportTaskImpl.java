package ohos.agp.render.render3d.impl;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.render.render3d.BuildConfig;
import ohos.agp.render.render3d.Task;
import ohos.agp.render.render3d.gltf.GltfData;
import ohos.agp.render.render3d.gltf.GltfLoader;
import ohos.agp.render.render3d.impl.GltfLoaderImpl;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

final class GltfImportTaskImpl extends Task {
    private static final int DEFAULT_TIME_BUDGET_MICROS = 16666;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "core: GltfImportTaskImpl");
    private EngineImpl mEngine;
    private long mFlags;
    private GltfData mGltfData;
    private GltfLoaderImpl.GltfImportDataImpl mImportData;
    private boolean mIsReady = false;
    private GltfLoader.ImportListener mListener;

    GltfImportTaskImpl(EngineImpl engineImpl, GltfData gltfData, long j, GltfLoader.ImportListener importListener) {
        this.mEngine = engineImpl;
        this.mGltfData = gltfData;
        this.mFlags = j;
        this.mListener = importListener;
    }

    @Override // ohos.agp.render.render3d.Task
    public void onInitialize() {
        GltfData gltfData = this.mGltfData;
        if (gltfData instanceof GltfLoaderImpl.GltfDataImpl) {
            CoreGltfData data = ((GltfLoaderImpl.GltfDataImpl) gltfData).getResult().getData();
            AgpContextImpl agpContext = this.mEngine.getAgpContext();
            CoreGltf2Importer createGltf2Importer = Core.createGltf2Importer(agpContext.getEngine(), agpContext.getGraphicsContext().getResourceCreator());
            if (createGltf2Importer != null) {
                this.mImportData = new GltfLoaderImpl.GltfImportDataImpl(this.mEngine, createGltf2Importer);
                createGltf2Importer.importGltfAsync(data, this.mFlags, null);
                return;
            }
            return;
        }
        throw new IllegalArgumentException();
    }

    @Override // ohos.agp.render.render3d.Task
    public boolean onExecute() {
        GltfLoaderImpl.GltfImportDataImpl gltfImportDataImpl = this.mImportData;
        if (gltfImportDataImpl == null || gltfImportDataImpl.getImporter() == null) {
            return true;
        }
        this.mEngine.requireRenderThread();
        CoreGltf2Importer importer = this.mImportData.getImporter();
        importer.execute(16666);
        if (importer.isCompleted()) {
            this.mIsReady = true;
            if (!importer.getResult().getSuccess()) {
                HiLog.error(LABEL, "Importing glTF failed.", new Object[0]);
            } else if (BuildConfig.DEBUG) {
                HiLog.debug(LABEL, "glTF imported.", new Object[0]);
            }
            GltfLoader.ImportListener importListener = this.mListener;
            if (importListener != null) {
                importListener.onGltfImportFinished(this.mGltfData.getGltfUri(), this.mImportData);
            }
            this.mGltfData = null;
            this.mImportData = null;
        }
        return this.mIsReady;
    }

    @Override // ohos.agp.render.render3d.Task
    public void onFinish() {
        if (BuildConfig.DEBUG) {
            HiLog.debug(LABEL, "onFinish()", new Object[0]);
        }
    }

    @Override // ohos.agp.render.render3d.Task
    public void onCancel() {
        if (BuildConfig.DEBUG) {
            HiLog.debug(LABEL, "onCancel()", new Object[0]);
        }
        GltfLoaderImpl.GltfImportDataImpl gltfImportDataImpl = this.mImportData;
        if (gltfImportDataImpl != null) {
            gltfImportDataImpl.release();
            this.mImportData = null;
        }
    }
}
