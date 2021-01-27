package com.huawei.agpengine.impl;

import android.util.Log;
import com.huawei.agpengine.Task;
import com.huawei.agpengine.gltf.GltfLoader;
import com.huawei.agpengine.impl.GltfLoaderImpl;

final class GltfImportTaskImpl extends Task {
    private static final int DEFAULT_TIME_BUDGET_MICROS = 16666;
    private static final String TAG = "core: GltfImportTaskImpl";
    private EngineImpl mEngine;
    private long mFlags;
    private GltfLoaderImpl.GltfDataImpl mGltfData;
    private GltfLoaderImpl.GltfImportDataImpl mImportData;
    private boolean mIsReady = false;
    private GltfLoader.ImportListener mListener;

    GltfImportTaskImpl(EngineImpl engine, GltfLoaderImpl.GltfDataImpl gltfData, long flags, GltfLoader.ImportListener listener) {
        this.mEngine = engine;
        this.mGltfData = gltfData;
        this.mFlags = flags;
        this.mListener = listener;
    }

    @Override // com.huawei.agpengine.Task
    public void onInitialize() {
        CoreGltfData nativeData = this.mGltfData.getResult().getData();
        CoreGltf2ImporterPtr gltfImporter = this.mEngine.getAgpContext().getGraphicsContext().getGltf().createGltf2Importer();
        if (gltfImporter != null && gltfImporter.get() != null) {
            this.mImportData = new GltfLoaderImpl.GltfImportDataImpl(this.mEngine, gltfImporter);
            gltfImporter.get().importGltfAsync(nativeData, this.mFlags, null);
        }
    }

    @Override // com.huawei.agpengine.Task
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
                Log.e(TAG, "Importing glTF failed.");
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

    @Override // com.huawei.agpengine.Task
    public void onFinish() {
    }

    @Override // com.huawei.agpengine.Task
    public void onCancel() {
        GltfLoaderImpl.GltfImportDataImpl gltfImportDataImpl = this.mImportData;
        if (gltfImportDataImpl != null) {
            gltfImportDataImpl.release();
            this.mImportData = null;
        }
    }
}
