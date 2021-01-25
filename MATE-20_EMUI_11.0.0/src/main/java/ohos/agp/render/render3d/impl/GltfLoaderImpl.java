package ohos.agp.render.render3d.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.render.render3d.BuildConfig;
import ohos.agp.render.render3d.Entity;
import ohos.agp.render.render3d.Task;
import ohos.agp.render.render3d.gltf.GltfData;
import ohos.agp.render.render3d.gltf.GltfImportData;
import ohos.agp.render.render3d.gltf.GltfLoader;
import ohos.agp.render.render3d.resources.GpuResourceHandle;
import ohos.agp.render.render3d.resources.ResourceHandle;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public final class GltfLoaderImpl implements GltfLoader {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "core: GltfLoaderImpl");
    private EngineImpl mEngine;

    GltfLoaderImpl(EngineImpl engineImpl) {
        this.mEngine = engineImpl;
    }

    @Override // ohos.agp.render.render3d.gltf.GltfLoader
    public GltfData loadGltf(String str) {
        if (BuildConfig.DEBUG) {
            HiLog.debug(LABEL, "loadGltf from uri.", new Object[0]);
        }
        if (str != null) {
            if (str.length() == 0 || "file://".equals(str)) {
                HiLog.warn(LABEL, "Warning: Empty uri as gltf content.", new Object[0]);
            }
            return new GltfDataImpl(this.mEngine.getAgpContext(), str);
        }
        throw new NullPointerException();
    }

    @Override // ohos.agp.render.render3d.gltf.GltfLoader
    public GltfData loadGltf(ByteBuffer byteBuffer) {
        if (BuildConfig.DEBUG) {
            HiLog.debug(LABEL, "loadGltf from memory.", new Object[0]);
        }
        if (byteBuffer == null) {
            throw new NullPointerException();
        } else if (byteBuffer.isDirect()) {
            if (byteBuffer.capacity() == 0) {
                HiLog.warn(LABEL, "Warning: Empty buffer as gltf content.", new Object[0]);
            }
            CoreByteArrayView coreByteArrayView = new CoreByteArrayView(byteBuffer);
            GltfDataImpl gltfDataImpl = new GltfDataImpl(this.mEngine.getAgpContext(), coreByteArrayView);
            coreByteArrayView.delete();
            return gltfDataImpl;
        } else {
            throw new IllegalArgumentException("A direct buffer is required.");
        }
    }

    @Override // ohos.agp.render.render3d.gltf.GltfLoader
    public int getDefaultResourceImportFlags() {
        return CoreGltfResourceImportFlagBits.CORE_GLTF_IMPORT_RESOURCE_FLAG_BITS_ALL.swigValue();
    }

    @Override // ohos.agp.render.render3d.gltf.GltfLoader
    public int getDefaultSceneImportFlags() {
        return CoreGltfSceneImportFlagBits.CORE_GLTF_IMPORT_COMPONENT_FLAG_BITS_ALL.swigValue();
    }

    @Override // ohos.agp.render.render3d.gltf.GltfLoader
    public GltfImportData importGltf(GltfData gltfData, int i) {
        if (gltfData == null) {
            throw new NullPointerException();
        } else if (gltfData.isValid()) {
            this.mEngine.requireRenderThread();
            CoreGltf2Importer createGltf2Importer = Core.createGltf2Importer(this.mEngine.getAgpContext().getEngine(), this.mEngine.getAgpContext().getGraphicsContext().getResourceCreator());
            if (createGltf2Importer == null) {
                throw new IllegalStateException("Cannot create importer.");
            } else if (gltfData instanceof GltfDataImpl) {
                createGltf2Importer.importGltf(((GltfDataImpl) gltfData).mResult.getData(), (long) i);
                return new GltfImportDataImpl(this.mEngine, createGltf2Importer);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalStateException("GltfData is not valid.");
        }
    }

    @Override // ohos.agp.render.render3d.gltf.GltfLoader
    public Task importGltfAsync(GltfData gltfData, int i, GltfLoader.ImportListener importListener) {
        if (gltfData == null) {
            throw new NullPointerException();
        } else if (gltfData.isValid()) {
            return new GltfImportTaskImpl(this.mEngine, gltfData, (long) i, importListener);
        } else {
            throw new IllegalStateException("GltfData is not valid.");
        }
    }

    @Override // ohos.agp.render.render3d.gltf.GltfLoader
    public Entity importScene(int i, GltfData gltfData, GltfImportData gltfImportData, Entity entity, int i2) {
        CoreEntity coreEntity;
        this.mEngine.requireRenderThread();
        if (gltfData instanceof GltfDataImpl) {
            CoreGltfData data = ((GltfDataImpl) gltfData).mResult.getData();
            if (gltfImportData instanceof GltfImportDataImpl) {
                CoreGltfResourceData data2 = ((GltfImportDataImpl) gltfImportData).getImporter().getResult().getData();
                if (entity != null) {
                    coreEntity = EntityImpl.getNativeEntity(entity);
                } else {
                    coreEntity = new CoreEntity();
                }
                EngineImpl engineImpl = this.mEngine;
                return engineImpl.getEntity(Core.importGltfScene((long) i, data, data2, engineImpl.getAgpContext().getGraphicsContext().getEcs(), coreEntity, (long) i2));
            }
            throw new IllegalArgumentException();
        }
        throw new IllegalArgumentException();
    }

    static class GltfDataImpl implements GltfData {
        private AgpContextImpl mAgpContext;
        private String mGltfUri;
        private CoreGltfLoadResult mResult;

        GltfDataImpl(AgpContextImpl agpContextImpl, String str) {
            this.mGltfUri = str;
            this.mAgpContext = agpContextImpl;
            this.mResult = Core.loadGltf(agpContextImpl.getEngine().getFileManager(), str);
        }

        GltfDataImpl(AgpContextImpl agpContextImpl, CoreByteArrayView coreByteArrayView) {
            this.mGltfUri = "";
            this.mAgpContext = agpContextImpl;
            this.mResult = Core.loadGltf(agpContextImpl.getEngine().getFileManager(), coreByteArrayView);
        }

        /* access modifiers changed from: package-private */
        public CoreGltfLoadResult getResult() {
            return this.mResult;
        }

        @Override // ohos.agp.render.render3d.gltf.GltfData
        public boolean isValid() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            return coreGltfLoadResult != null && coreGltfLoadResult.getSuccess();
        }

        @Override // ohos.agp.render.render3d.gltf.GltfData
        public String getError() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult == null) {
                return "";
            }
            return coreGltfLoadResult.getError();
        }

        @Override // ohos.agp.render.render3d.gltf.GltfData
        public void releaseBuffers() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult != null) {
                coreGltfLoadResult.getData().releaseBuffers();
            }
        }

        @Override // ohos.agp.render.render3d.gltf.GltfData
        public int getThumbnailCount() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult == null) {
                return 0;
            }
            return (int) coreGltfLoadResult.getData().getThumbnailImageCount();
        }

        @Override // ohos.agp.render.render3d.gltf.GltfData
        public void release() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult != null) {
                coreGltfLoadResult.delete();
                this.mResult = null;
            }
        }

        @Override // ohos.agp.render.render3d.gltf.GltfData
        public int getSceneCount() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult == null) {
                return 0;
            }
            return (int) coreGltfLoadResult.getData().getSceneCount();
        }

        @Override // ohos.agp.render.render3d.gltf.GltfData
        public int getDefaultSceneIndex() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult == null) {
                return -1;
            }
            long defaultSceneIndex = coreGltfLoadResult.getData().getDefaultSceneIndex();
            if (defaultSceneIndex == Core.getGltfInvalidIndex()) {
                return -1;
            }
            return (int) defaultSceneIndex;
        }

        @Override // ohos.agp.render.render3d.gltf.GltfData
        public List<String> getExternalUris() {
            CoreGltfLoadResult coreGltfLoadResult = this.mResult;
            if (coreGltfLoadResult == null) {
                return new ArrayList(0);
            }
            CoreStringArray externalFileUris = coreGltfLoadResult.getData().getExternalFileUris();
            ArrayList arrayList = new ArrayList(externalFileUris.size());
            for (int i = 0; i < externalFileUris.size(); i++) {
                arrayList.add(externalFileUris.get(i));
            }
            return arrayList;
        }

        @Override // ohos.agp.render.render3d.gltf.GltfData
        public String getGltfUri() {
            return this.mGltfUri;
        }

        @Override // ohos.agp.render.render3d.gltf.GltfData
        public String getBaseUri() {
            throw new UnsupportedOperationException();
        }
    }

    static class GltfImportDataImpl implements GltfImportData {
        private EngineImpl mEngine;
        private CoreGltf2Importer mGltfImporter;

        GltfImportDataImpl(EngineImpl engineImpl, CoreGltf2Importer coreGltf2Importer) {
            if (engineImpl == null) {
                throw new NullPointerException();
            } else if (coreGltf2Importer != null) {
                this.mEngine = engineImpl;
                this.mGltfImporter = coreGltf2Importer;
            } else {
                throw new NullPointerException();
            }
        }

        /* access modifiers changed from: package-private */
        public CoreGltf2Importer getImporter() {
            return this.mGltfImporter;
        }

        @Override // ohos.agp.render.render3d.gltf.GltfImportData
        public boolean isValid() {
            return this.mGltfImporter.getResult().getSuccess();
        }

        @Override // ohos.agp.render.render3d.gltf.GltfImportData
        public String getError() {
            return this.mGltfImporter.getResult().getError();
        }

        @Override // ohos.agp.render.render3d.gltf.GltfImportData
        public void release() {
            this.mEngine.requireRenderThread();
            CoreGltf2Importer coreGltf2Importer = this.mGltfImporter;
            if (coreGltf2Importer != null) {
                if (!coreGltf2Importer.isCompleted()) {
                    this.mGltfImporter.cancel();
                }
                CoreEngine engine = this.mEngine.getAgpContext().getEngine();
                CoreResourceManager resourceManager = engine.getResourceManager();
                CoreGltfResourceData data = this.mGltfImporter.getResult().getData();
                release(resourceManager, data.getMeshes());
                release(resourceManager, data.getMaterials());
                release(resourceManager, data.getSkins());
                release(resourceManager, data.getAnimations());
                release(resourceManager, data.getImages());
                release(engine.getGpuResourceManager(), data.getSamplers());
                this.mGltfImporter.delete();
                this.mGltfImporter = null;
            }
            this.mEngine = null;
        }

        private void release(CoreResourceManager coreResourceManager, CoreResourceArray coreResourceArray) {
            if (coreResourceArray != null) {
                long size = (long) coreResourceArray.size();
                for (int i = 0; ((long) i) < size; i++) {
                    CoreResourceHandle coreResourceHandle = coreResourceArray.get(i);
                    if (coreResourceManager.isValid(coreResourceHandle)) {
                        coreResourceManager.erase(coreResourceHandle);
                    }
                }
            }
        }

        private void release(CoreGpuResourceManager coreGpuResourceManager, CoreGpuResourceArray coreGpuResourceArray) {
            if (coreGpuResourceArray != null) {
                long size = (long) coreGpuResourceArray.size();
                for (int i = 0; ((long) i) < size; i++) {
                    CoreGpuResourceHandle coreGpuResourceHandle = coreGpuResourceArray.get(i);
                    if (CoreGpuResourceHandleUtil.isValid(coreGpuResourceHandle)) {
                        coreGpuResourceManager.destroy(coreGpuResourceHandle);
                    }
                }
            }
        }

        private List<GpuResourceHandle> toList(CoreGpuResourceArray coreGpuResourceArray) {
            if (coreGpuResourceArray == null) {
                return new ArrayList(0);
            }
            ArrayList arrayList = new ArrayList(coreGpuResourceArray.size());
            for (int i = 0; i < coreGpuResourceArray.size(); i++) {
                arrayList.add(new GpuResourceHandleImpl(this.mEngine, coreGpuResourceArray.get(i)));
            }
            return arrayList;
        }

        private List<ResourceHandle> toList(CoreResourceArray coreResourceArray) {
            if (coreResourceArray == null) {
                return new ArrayList(0);
            }
            ArrayList arrayList = new ArrayList(coreResourceArray.size());
            for (int i = 0; i < coreResourceArray.size(); i++) {
                arrayList.add(new ResourceHandleImpl(this.mEngine, coreResourceArray.get(i)));
            }
            return arrayList;
        }

        @Override // ohos.agp.render.render3d.gltf.GltfImportData
        public List<GpuResourceHandle> getSamplers() {
            CoreGltf2Importer coreGltf2Importer = this.mGltfImporter;
            if (coreGltf2Importer == null) {
                return new ArrayList(0);
            }
            return toList(coreGltf2Importer.getResult().getData().getSamplers());
        }

        @Override // ohos.agp.render.render3d.gltf.GltfImportData
        public List<ResourceHandle> getImages() {
            CoreGltf2Importer coreGltf2Importer = this.mGltfImporter;
            if (coreGltf2Importer == null) {
                return new ArrayList(0);
            }
            return toList(coreGltf2Importer.getResult().getData().getImages());
        }

        @Override // ohos.agp.render.render3d.gltf.GltfImportData
        public List<GpuResourceHandle> getTextures() {
            CoreGltf2Importer coreGltf2Importer = this.mGltfImporter;
            if (coreGltf2Importer == null) {
                return new ArrayList(0);
            }
            return toList(coreGltf2Importer.getResult().getData().getTextures());
        }

        @Override // ohos.agp.render.render3d.gltf.GltfImportData
        public List<ResourceHandle> getMaterials() {
            CoreGltf2Importer coreGltf2Importer = this.mGltfImporter;
            if (coreGltf2Importer == null) {
                return new ArrayList(0);
            }
            return toList(coreGltf2Importer.getResult().getData().getMaterials());
        }

        @Override // ohos.agp.render.render3d.gltf.GltfImportData
        public List<ResourceHandle> getMeshes() {
            CoreGltf2Importer coreGltf2Importer = this.mGltfImporter;
            if (coreGltf2Importer == null) {
                return new ArrayList(0);
            }
            return toList(coreGltf2Importer.getResult().getData().getMeshes());
        }

        @Override // ohos.agp.render.render3d.gltf.GltfImportData
        public List<ResourceHandle> getSkins() {
            CoreGltf2Importer coreGltf2Importer = this.mGltfImporter;
            if (coreGltf2Importer == null) {
                return new ArrayList(0);
            }
            return toList(coreGltf2Importer.getResult().getData().getSkins());
        }

        @Override // ohos.agp.render.render3d.gltf.GltfImportData
        public List<ResourceHandle> getAnimations() {
            CoreGltf2Importer coreGltf2Importer = this.mGltfImporter;
            if (coreGltf2Importer == null) {
                return new ArrayList(0);
            }
            return toList(coreGltf2Importer.getResult().getData().getAnimations());
        }
    }
}
