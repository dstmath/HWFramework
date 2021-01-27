package com.huawei.agpengine.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.huawei.agpengine.BuildConfig;
import com.huawei.agpengine.Entity;
import com.huawei.agpengine.Task;
import com.huawei.agpengine.gltf.GltfData;
import com.huawei.agpengine.gltf.GltfImportData;
import com.huawei.agpengine.gltf.GltfLoader;
import com.huawei.agpengine.resources.GpuResourceHandle;
import com.huawei.agpengine.resources.ResourceHandle;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public final class GltfLoaderImpl implements GltfLoader {
    private static final String TAG = "core: GltfLoaderImpl";
    private EngineImpl mEngine;

    GltfLoaderImpl(EngineImpl engine) {
        this.mEngine = engine;
    }

    @Override // com.huawei.agpengine.gltf.GltfLoader
    public GltfData loadGltf(String gltfUri) {
        if (gltfUri != null) {
            if (gltfUri.length() == 0 || "file://".equals(gltfUri)) {
                Log.w(TAG, "Warning: Empty uri as gltf content.");
            }
            return new GltfDataImpl(this.mEngine.getAgpContext(), gltfUri);
        }
        throw new NullPointerException("gltfUri must not be null.");
    }

    @Override // com.huawei.agpengine.gltf.GltfLoader
    public GltfData loadGltf(ByteBuffer gltfBytes) {
        if (gltfBytes == null) {
            throw new NullPointerException("gltfBytes must not be null.");
        } else if (gltfBytes.isDirect()) {
            if (gltfBytes.capacity() == 0) {
                Log.w(TAG, "Warning: Empty buffer as gltf content.");
            }
            CoreByteArrayView loadingBufferView = new CoreByteArrayView(gltfBytes);
            GltfData gltf = new GltfDataImpl(this.mEngine.getAgpContext(), loadingBufferView);
            loadingBufferView.delete();
            return gltf;
        } else {
            throw new IllegalArgumentException("A direct buffer is required.");
        }
    }

    @Override // com.huawei.agpengine.gltf.GltfLoader
    public int getDefaultResourceImportFlags() {
        return CoreGltfResourceImportFlagBits.CORE_GLTF_IMPORT_RESOURCE_FLAG_BITS_ALL.swigValue();
    }

    @Override // com.huawei.agpengine.gltf.GltfLoader
    public int getDefaultSceneImportFlags() {
        return CoreGltfSceneImportFlagBits.CORE_GLTF_IMPORT_COMPONENT_FLAG_BITS_ALL.swigValue();
    }

    private GltfDataImpl checkData(GltfData gltfData) {
        if (gltfData == null) {
            throw new NullPointerException("GltfData must not be null.");
        } else if (!gltfData.isValid()) {
            throw new IllegalStateException("GltfData is not valid.");
        } else if (gltfData instanceof GltfDataImpl) {
            return (GltfDataImpl) gltfData;
        } else {
            throw new IllegalArgumentException("Unsupported GltfData.");
        }
    }

    private GltfImportDataImpl checkImportData(GltfImportData importData) {
        if (importData == null) {
            throw new NullPointerException("GltfImportData must not be null.");
        } else if (!importData.isValid()) {
            throw new IllegalStateException("GltfImportData is not valid.");
        } else if (importData instanceof GltfImportDataImpl) {
            return (GltfImportDataImpl) importData;
        } else {
            throw new IllegalArgumentException("Unsupported GltfImportData.");
        }
    }

    @Override // com.huawei.agpengine.gltf.GltfLoader
    public GltfImportData importGltf(GltfData gltfData, int flags) {
        this.mEngine.requireRenderThread();
        GltfDataImpl dataImpl = checkData(gltfData);
        CoreGltf2ImporterPtr gltfImporter = this.mEngine.getAgpContext().getGraphicsContext().getGltf().createGltf2Importer();
        if (gltfImporter == null || gltfImporter.get() == null) {
            throw new IllegalStateException("Cannot create importer.");
        }
        gltfImporter.get().importGltf(dataImpl.mResult.getData(), (long) flags);
        return new GltfImportDataImpl(this.mEngine, gltfImporter);
    }

    @Override // com.huawei.agpengine.gltf.GltfLoader
    public Task importGltfAsync(GltfData gltfData, int flags, GltfLoader.ImportListener listener) {
        return new GltfImportTaskImpl(this.mEngine, checkData(gltfData), (long) flags, listener);
    }

    @Override // com.huawei.agpengine.gltf.GltfLoader
    public Entity importScene(int sceneIndex, GltfData data, GltfImportData importData, Entity rootEntity, int flags) {
        this.mEngine.requireRenderThread();
        GltfDataImpl dataImpl = checkData(data);
        GltfImportDataImpl importDataImpl = checkImportData(importData);
        CoreGltfData nativeData = dataImpl.mResult.getData();
        CoreGltfResourceData nativeResourceData = importDataImpl.getImporter().getResult().getData();
        int root = rootEntity != null ? rootEntity.getId() : -1;
        CoreGraphicsContext graphicsContext = this.mEngine.getAgpContext().getGraphicsContext();
        return this.mEngine.getEntity(graphicsContext.getGltf().importGltfScene((long) sceneIndex, nativeData, nativeResourceData, graphicsContext.getEcs(), root, (long) flags));
    }

    /* access modifiers changed from: package-private */
    public static class GltfDataImpl implements GltfData {
        private AgpContextImpl mAgpContext;
        private String mGltfUri;
        private CoreGltfLoadResult mResult;

        GltfDataImpl(AgpContextImpl agpContext, String gltfUri) {
            this.mGltfUri = gltfUri;
            this.mAgpContext = agpContext;
            this.mResult = agpContext.getGraphicsContext().getGltf().loadGltf(gltfUri);
        }

        GltfDataImpl(AgpContextImpl agpContext, CoreByteArrayView gltfBytes) {
            this.mGltfUri = BuildConfig.FLAVOR;
            this.mAgpContext = agpContext;
            this.mResult = agpContext.getGraphicsContext().getGltf().loadGltf(gltfBytes);
        }

        /* access modifiers changed from: package-private */
        public CoreGltfLoadResult getResult() {
            return this.mResult;
        }

        @Override // com.huawei.agpengine.gltf.GltfData
        public boolean isValid() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            return coreGltfLoadResult != null && coreGltfLoadResult.getSuccess();
        }

        @Override // com.huawei.agpengine.gltf.GltfData
        public String getError() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult == null) {
                return BuildConfig.FLAVOR;
            }
            return coreGltfLoadResult.getError();
        }

        @Override // com.huawei.agpengine.gltf.GltfData
        public void releaseBuffers() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult != null) {
                coreGltfLoadResult.getData().releaseBuffers();
            }
        }

        @Override // com.huawei.agpengine.gltf.GltfData
        public int getThumbnailCount() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult == null) {
                return 0;
            }
            return (int) coreGltfLoadResult.getData().getThumbnailImageCount();
        }

        @Override // com.huawei.agpengine.gltf.GltfData
        public Bitmap getThumbnail(int index) {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult != null) {
                CoreByteArrayView imageData = coreGltfLoadResult.getData().getThumbnailImage((long) index).getData();
                if (imageData == null) {
                    return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                }
                byte[] datas = new byte[((int) imageData.size())];
                for (int byteIdx = 0; ((long) byteIdx) < imageData.size(); byteIdx++) {
                    datas[byteIdx] = (byte) imageData.get((long) byteIdx);
                }
                return BitmapFactory.decodeByteArray(datas, 0, datas.length);
            }
            throw new IllegalStateException("Gltf not available.");
        }

        @Override // com.huawei.agpengine.gltf.GltfData
        public void release() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult != null) {
                coreGltfLoadResult.delete();
                this.mResult = null;
            }
        }

        @Override // com.huawei.agpengine.gltf.GltfData
        public int getSceneCount() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult == null) {
                return 0;
            }
            return (int) coreGltfLoadResult.getData().getSceneCount();
        }

        @Override // com.huawei.agpengine.gltf.GltfData
        public int getDefaultSceneIndex() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult == null) {
                return -1;
            }
            long defaultScene = coreGltfLoadResult.getData().getDefaultSceneIndex();
            if (defaultScene == Core.getGltfInvalidIndex()) {
                return -1;
            }
            return (int) defaultScene;
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            if (this.mResult != null) {
                Log.w(GltfLoaderImpl.TAG, "GltfData not released explicitly");
            }
            release();
            super.finalize();
        }

        @Override // com.huawei.agpengine.gltf.GltfData
        public List<String> getExternalUris() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult == null) {
                return new ArrayList(0);
            }
            CoreStringArray uris = coreGltfLoadResult.getData().getExternalFileUris();
            List<String> resultUris = new ArrayList<>(uris.size());
            for (int i = 0; i < uris.size(); i++) {
                resultUris.add(uris.get(i));
            }
            return resultUris;
        }

        @Override // com.huawei.agpengine.gltf.GltfData
        public String getGltfUri() {
            return this.mGltfUri;
        }

        @Override // com.huawei.agpengine.gltf.GltfData
        public String getBaseUri() {
            int slash = this.mGltfUri.lastIndexOf(92);
            if (slash == -1) {
                slash = this.mGltfUri.lastIndexOf(47);
            }
            if (slash == -1) {
                return BuildConfig.FLAVOR;
            }
            return this.mGltfUri.substring(0, slash + 1);
        }
    }

    /* access modifiers changed from: package-private */
    public static class GltfImportDataImpl implements GltfImportData {
        private EngineImpl mEngine;
        private CoreGltf2ImporterPtr mGltfImporter;

        GltfImportDataImpl(EngineImpl engine, CoreGltf2ImporterPtr gltfImporter) {
            if (engine == null) {
                throw new NullPointerException("Internal graphics engine error");
            } else if (gltfImporter != null) {
                this.mEngine = engine;
                this.mGltfImporter = gltfImporter;
            } else {
                throw new NullPointerException("Internal graphics engine error");
            }
        }

        /* access modifiers changed from: package-private */
        public CoreGltf2Importer getImporter() {
            return this.mGltfImporter.get();
        }

        @Override // com.huawei.agpengine.gltf.GltfImportData
        public boolean isValid() {
            return getImporter().getResult().getSuccess();
        }

        @Override // com.huawei.agpengine.gltf.GltfImportData
        public String getError() {
            return getImporter().getResult().getError();
        }

        @Override // com.huawei.agpengine.gltf.GltfImportData
        public void release() {
            this.mEngine.requireRenderThread();
            CoreGltf2Importer importer = getImporter();
            if (importer != null) {
                if (!importer.isCompleted()) {
                    importer.cancel();
                }
                this.mEngine.getAgpContext().getGraphicsContext().getGltf().releaseGltfResources(importer.getResult().getData());
                this.mGltfImporter.delete();
                this.mGltfImporter = null;
            }
            this.mEngine = null;
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            if (this.mEngine != null) {
                Log.w(GltfLoaderImpl.TAG, "GltfImportData not released explicitly.");
                this.mEngine.runInRenderThread(new Runnable() {
                    /* class com.huawei.agpengine.impl.GltfLoaderImpl.GltfImportDataImpl.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        GltfImportDataImpl.this.release();
                    }
                });
            }
            super.finalize();
        }

        private List<GpuResourceHandle> toList(CoreGpuResourceArray nativeArray) {
            if (nativeArray == null) {
                return new ArrayList(0);
            }
            List<GpuResourceHandle> resultHandles = new ArrayList<>(nativeArray.size());
            for (int i = 0; i < nativeArray.size(); i++) {
                resultHandles.add(new GpuResourceHandleImpl(this.mEngine, nativeArray.get(i).longValue()));
            }
            return resultHandles;
        }

        private List<ResourceHandle> toList(CoreResourceArray nativeArray) {
            if (nativeArray == null) {
                return new ArrayList(0);
            }
            List<ResourceHandle> resultHandles = new ArrayList<>(nativeArray.size());
            for (int i = 0; i < nativeArray.size(); i++) {
                resultHandles.add(new ResourceHandleImpl(this.mEngine, nativeArray.get(i)));
            }
            return resultHandles;
        }

        @Override // com.huawei.agpengine.gltf.GltfImportData
        public List<GpuResourceHandle> getSamplers() {
            if (this.mGltfImporter == null) {
                return new ArrayList(0);
            }
            return toList(getImporter().getResult().getData().getSamplers());
        }

        @Override // com.huawei.agpengine.gltf.GltfImportData
        public List<ResourceHandle> getImages() {
            if (this.mGltfImporter == null) {
                return new ArrayList(0);
            }
            return toList(getImporter().getResult().getData().getImages());
        }

        @Override // com.huawei.agpengine.gltf.GltfImportData
        public List<GpuResourceHandle> getTextures() {
            if (this.mGltfImporter == null) {
                return new ArrayList(0);
            }
            return toList(getImporter().getResult().getData().getTextures());
        }

        @Override // com.huawei.agpengine.gltf.GltfImportData
        public List<ResourceHandle> getMaterials() {
            if (this.mGltfImporter == null) {
                return new ArrayList(0);
            }
            return toList(getImporter().getResult().getData().getMaterials());
        }

        @Override // com.huawei.agpengine.gltf.GltfImportData
        public List<ResourceHandle> getMeshes() {
            if (this.mGltfImporter == null) {
                return new ArrayList(0);
            }
            return toList(getImporter().getResult().getData().getMeshes());
        }

        @Override // com.huawei.agpengine.gltf.GltfImportData
        public List<ResourceHandle> getSkins() {
            if (this.mGltfImporter == null) {
                return new ArrayList(0);
            }
            return toList(getImporter().getResult().getData().getSkins());
        }

        @Override // com.huawei.agpengine.gltf.GltfImportData
        public List<ResourceHandle> getAnimations() {
            if (this.mGltfImporter == null) {
                return new ArrayList(0);
            }
            return toList(getImporter().getResult().getData().getAnimations());
        }
    }
}
