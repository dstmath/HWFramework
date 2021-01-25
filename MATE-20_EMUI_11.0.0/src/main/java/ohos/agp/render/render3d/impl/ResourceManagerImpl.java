package ohos.agp.render.render3d.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.agp.render.render3d.SceneNode;
import ohos.agp.render.render3d.impl.CoreResourceCreator;
import ohos.agp.render.render3d.impl.CoreResourceManager;
import ohos.agp.render.render3d.resources.AnimationPlayback;
import ohos.agp.render.render3d.resources.GpuResourceHandle;
import ohos.agp.render.render3d.resources.ImageFormat;
import ohos.agp.render.render3d.resources.MaterialDesc;
import ohos.agp.render.render3d.resources.MeshDesc;
import ohos.agp.render.render3d.resources.RenderDataStorePod;
import ohos.agp.render.render3d.resources.ResourceHandle;
import ohos.agp.render.render3d.resources.ResourceManager;
import ohos.agp.render.render3d.util.BoundingBox;
import ohos.agp.render.render3d.util.MeshBuilder;

final class ResourceManagerImpl implements ResourceManager {
    private final EngineImpl mEngine;
    private final CoreResourceCreator mResourceCreator = this.mEngine.getAgpContext().getGraphicsContext().getResourceCreator();
    private final CoreResourceManager mResourceManager = this.mEngine.getAgpContext().getEngine().getResourceManager();

    ResourceManagerImpl(EngineImpl engineImpl) {
        this.mEngine = engineImpl;
    }

    private List<ResourceManager.ResourceInfo> convertInfos(CoreResourceInfoArray coreResourceInfoArray) {
        int size = coreResourceInfoArray.size();
        ArrayList arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            final CoreResourceManager.CoreResourceInfo coreResourceInfo = coreResourceInfoArray.get(i);
            arrayList.add(new ResourceManager.ResourceInfo() {
                /* class ohos.agp.render.render3d.impl.ResourceManagerImpl.AnonymousClass1 */
                private ResourceHandle mHandle = new ResourceHandleImpl(ResourceManagerImpl.this.mEngine, coreResourceInfo.getHandle());
                private String mName = coreResourceInfo.getName();
                private String mUri = coreResourceInfo.getUri();

                @Override // ohos.agp.render.render3d.resources.ResourceManager.ResourceInfo
                public String getUri() {
                    return this.mUri;
                }

                @Override // ohos.agp.render.render3d.resources.ResourceManager.ResourceInfo
                public String getName() {
                    return this.mName;
                }

                @Override // ohos.agp.render.render3d.resources.ResourceManager.ResourceInfo
                public ResourceHandle getHandle() {
                    return this.mHandle;
                }
            });
        }
        return arrayList;
    }

    private List<ResourceManager.ResourceInfo> convertCreatorInfos(CoreResourceCreatorInfoArray coreResourceCreatorInfoArray) {
        int size = coreResourceCreatorInfoArray.size();
        ArrayList arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            final CoreResourceCreator.CoreInfo coreInfo = coreResourceCreatorInfoArray.get(i);
            arrayList.add(new ResourceManager.ResourceInfo() {
                /* class ohos.agp.render.render3d.impl.ResourceManagerImpl.AnonymousClass2 */
                private ResourceHandle mHandle = new ResourceHandleImpl(ResourceManagerImpl.this.mEngine, coreInfo.getHandle());
                private String mName = coreInfo.getName();
                private String mUri = coreInfo.getUri();

                @Override // ohos.agp.render.render3d.resources.ResourceManager.ResourceInfo
                public String getUri() {
                    return this.mUri;
                }

                @Override // ohos.agp.render.render3d.resources.ResourceManager.ResourceInfo
                public String getName() {
                    return this.mName;
                }

                @Override // ohos.agp.render.render3d.resources.ResourceManager.ResourceInfo
                public ResourceHandle getHandle() {
                    return this.mHandle;
                }
            });
        }
        return arrayList;
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getResources() {
        return convertInfos(this.mResourceManager.getResources());
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getMaterials() {
        return convertCreatorInfos(this.mResourceCreator.getMaterials());
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getMeshes() {
        return convertCreatorInfos(this.mResourceCreator.getMeshes());
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getAnimations() {
        return convertCreatorInfos(this.mResourceCreator.getAnimations());
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getSkins() {
        return convertCreatorInfos(this.mResourceCreator.getSkins());
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getImages() {
        return convertCreatorInfos(this.mResourceCreator.getImages());
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public ResourceHandle getEmptyResourceHandle() {
        return new ResourceHandleImpl(this.mEngine, new CoreResourceHandle());
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public ResourceHandle getResourceHandle(String str) {
        CoreResourceHandle handle = this.mResourceCreator.getHandle(str);
        if (this.mResourceManager.isValid(handle)) {
            return new ResourceHandleImpl(this.mEngine, handle);
        }
        return getEmptyResourceHandle();
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public ResourceHandle createMaterial(String str, String str2) {
        if (str == null) {
            throw new NullPointerException();
        } else if (str2 != null) {
            this.mEngine.requireRenderThread();
            CoreResourceHandle create = this.mResourceCreator.create(str, str2, new CoreMaterialCreateInfo());
            if (this.mResourceManager.isValid(create)) {
                return new ResourceHandleImpl(this.mEngine, create);
            }
            return getEmptyResourceHandle();
        } else {
            throw new NullPointerException();
        }
    }

    private MaterialDesc.MaterialType getMaterialDescType(CoreMaterialType coreMaterialType) {
        int i = AnonymousClass3.$SwitchMap$ohos$agp$render$render3d$impl$CoreMaterialType[coreMaterialType.ordinal()];
        if (i == 1) {
            return MaterialDesc.MaterialType.METALLIC_ROUGHNESS;
        }
        if (i == 2) {
            return MaterialDesc.MaterialType.SPECULAR_GLOSSINESS;
        }
        if (i == 3) {
            return MaterialDesc.MaterialType.UNLIT;
        }
        if (i != 4) {
            return MaterialDesc.MaterialType.UNKNOWN;
        }
        return MaterialDesc.MaterialType.UNLIT_SHADOW_ALPHA;
    }

    private MaterialDesc.MaterialAlphaMode getMaterialAlphaMode(CoreMaterialAlphaMode coreMaterialAlphaMode) {
        int i = AnonymousClass3.$SwitchMap$ohos$agp$render$render3d$impl$CoreMaterialAlphaMode[coreMaterialAlphaMode.ordinal()];
        if (i == 1) {
            return MaterialDesc.MaterialAlphaMode.OPAQUE;
        }
        if (i == 2) {
            return MaterialDesc.MaterialAlphaMode.MASK;
        }
        if (i != 3) {
            return MaterialDesc.MaterialAlphaMode.UNKNOWN;
        }
        return MaterialDesc.MaterialAlphaMode.BLEND;
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public Optional<MaterialDesc> getMaterialDesc(ResourceHandle resourceHandle) {
        if (resourceHandle != null) {
            this.mEngine.requireRenderThread();
            if (!resourceHandle.isValid()) {
                return Optional.empty();
            }
            MaterialDesc materialDesc = null;
            CoreMaterial materialFromHandle = this.mResourceCreator.getMaterialFromHandle(ResourceHandleImpl.getNativeHandle(resourceHandle));
            if (materialFromHandle != null) {
                CoreMaterialDesc desc = materialFromHandle.getDesc();
                materialDesc = new MaterialDesc();
                materialDesc.setType(getMaterialDescType(desc.getType()));
                materialDesc.setBaseColor(new GpuResourceHandleImpl(this.mEngine, desc.getBaseColor()));
                materialDesc.setNormal(new GpuResourceHandleImpl(this.mEngine, desc.getNormal()));
                materialDesc.setEmissive(new GpuResourceHandleImpl(this.mEngine, desc.getEmissive()));
                materialDesc.setAmbientOcclusion(new GpuResourceHandleImpl(this.mEngine, desc.getAo()));
                materialDesc.setMaterial(new GpuResourceHandleImpl(this.mEngine, desc.getMaterial()));
                materialDesc.setSampler(new GpuResourceHandleImpl(this.mEngine, desc.getSampler()));
                materialDesc.setBaseColorFactor(Swig.get(desc.getBaseColorFactor()));
                materialDesc.setEmissiveFactor(Swig.get(desc.getEmissiveFactor()));
                materialDesc.setAmbientOcclusionFactor(desc.getAmbientOcclusionFactor());
                materialDesc.setRoughnessFactor(desc.getRoughnessFactor());
                materialDesc.setMetallicFactor(desc.getMetallicFactor());
                materialDesc.setReflectance(desc.getReflectance());
                materialDesc.setNormalScale(desc.getNormalScale());
                materialDesc.setSpecularFactor(Swig.get(desc.getSpecularFactor()));
                materialDesc.setGlossinessFactor(desc.getNormalScale());
                materialDesc.setClearCoatFactor(desc.getClearCoatFactor());
                materialDesc.setClearCoatRoughness(desc.getClearCoatRoughness());
                materialDesc.setAlphaMode(getMaterialAlphaMode(desc.getAlphaMode()));
                materialDesc.setAlphaCutoff(desc.getAlphaCutoff());
                materialDesc.setMaterialFlags((int) desc.getMaterialFlags());
            }
            return Optional.ofNullable(materialDesc);
        }
        throw new NullPointerException();
    }

    private CoreMaterialType getNativeMaterialDescType(MaterialDesc.MaterialType materialType) {
        int i = AnonymousClass3.$SwitchMap$ohos$agp$render$render3d$resources$MaterialDesc$MaterialType[materialType.ordinal()];
        if (i == 1) {
            return CoreMaterialType.CORE_MATERIAL_METALLIC_ROUGHNESS;
        }
        if (i == 2) {
            return CoreMaterialType.CORE_MATERIAL_SPECULAR_GLOSSINESS;
        }
        if (i == 3) {
            return CoreMaterialType.CORE_MATERIAL_UNLIT;
        }
        if (i != 4) {
            return CoreMaterialType.CORE_MATERIAL_UNLIT;
        }
        return CoreMaterialType.CORE_MATERIAL_UNLIT_SHADOW_ALPHA;
    }

    private CoreMaterialAlphaMode getNativeMaterialAlphaMode(MaterialDesc.MaterialAlphaMode materialAlphaMode) {
        int i = AnonymousClass3.$SwitchMap$ohos$agp$render$render3d$resources$MaterialDesc$MaterialAlphaMode[materialAlphaMode.ordinal()];
        if (i == 1) {
            return CoreMaterialAlphaMode.CORE_ALPHA_MODE_OPAQUE;
        }
        if (i == 2) {
            return CoreMaterialAlphaMode.CORE_ALPHA_MODE_MASK;
        }
        if (i != 3) {
            return CoreMaterialAlphaMode.CORE_ALPHA_MODE_OPAQUE;
        }
        return CoreMaterialAlphaMode.CORE_ALPHA_MODE_BLEND;
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public void setMaterialDesc(ResourceHandle resourceHandle, MaterialDesc materialDesc) {
        if (resourceHandle == null) {
            throw new NullPointerException();
        } else if (materialDesc != null) {
            this.mEngine.requireRenderThread();
            CoreMaterial materialFromHandle = this.mResourceCreator.getMaterialFromHandle(ResourceHandleImpl.getNativeHandle(resourceHandle));
            if (materialFromHandle != null) {
                CoreMaterialDesc coreMaterialDesc = new CoreMaterialDesc();
                coreMaterialDesc.setType(getNativeMaterialDescType(materialDesc.getType()));
                coreMaterialDesc.setBaseColor(GpuResourceHandleImpl.getNativeHandle(materialDesc.getBaseColor()));
                coreMaterialDesc.setNormal(GpuResourceHandleImpl.getNativeHandle(materialDesc.getNormal()));
                coreMaterialDesc.setEmissive(GpuResourceHandleImpl.getNativeHandle(materialDesc.getEmissive()));
                coreMaterialDesc.setAo(GpuResourceHandleImpl.getNativeHandle(materialDesc.getAmbientOcclusion()));
                coreMaterialDesc.setMaterial(GpuResourceHandleImpl.getNativeHandle(materialDesc.getMaterial()));
                coreMaterialDesc.setSampler(GpuResourceHandleImpl.getNativeHandle(materialDesc.getSampler()));
                coreMaterialDesc.setBaseColorFactor(Swig.set(materialDesc.getBaseColorFactor()));
                coreMaterialDesc.setEmissiveFactor(Swig.set(materialDesc.getEmissiveFactor()));
                coreMaterialDesc.setAmbientOcclusionFactor(materialDesc.getAmbientOcclusionFactor());
                coreMaterialDesc.setRoughnessFactor(materialDesc.getRoughnessFactor());
                coreMaterialDesc.setMetallicFactor(materialDesc.getMetallicFactor());
                coreMaterialDesc.setReflectance(materialDesc.getReflectance());
                coreMaterialDesc.setNormalScale(materialDesc.getNormalScale());
                coreMaterialDesc.setSpecularFactor(Swig.set(materialDesc.getSpecularFactor()));
                coreMaterialDesc.setNormalScale(materialDesc.getGlossinessFactor());
                coreMaterialDesc.setClearCoatFactor(materialDesc.getClearCoatFactor());
                coreMaterialDesc.setClearCoatRoughness(materialDesc.getClearCoatRoughness());
                coreMaterialDesc.setAlphaMode(getNativeMaterialAlphaMode(materialDesc.getAlphaMode()));
                coreMaterialDesc.setAlphaCutoff(materialDesc.getAlphaCutoff());
                coreMaterialDesc.setMaterialFlags((long) materialDesc.getMaterialFlags());
                materialFromHandle.setDesc(coreMaterialDesc);
            }
        } else {
            throw new NullPointerException();
        }
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public ResourceHandle createMesh(String str, String str2, MeshBuilder meshBuilder) {
        if (str == null) {
            throw new NullPointerException();
        } else if (str2 == null) {
            throw new NullPointerException();
        } else if (meshBuilder instanceof MeshBuilderImpl) {
            this.mEngine.requireRenderThread();
            CoreMeshBuilder nativeMeshBuilder = ((MeshBuilderImpl) meshBuilder).getNativeMeshBuilder();
            CoreMeshCreateInfo coreMeshCreateInfo = new CoreMeshCreateInfo();
            if (nativeMeshBuilder != null) {
                coreMeshCreateInfo.setVertexCount(nativeMeshBuilder.getVertexCount());
                coreMeshCreateInfo.setIndexCount(nativeMeshBuilder.getIndexCount());
                coreMeshCreateInfo.setVertexData(nativeMeshBuilder.getVertexData());
                coreMeshCreateInfo.setIndexData(nativeMeshBuilder.getIndexData());
                coreMeshCreateInfo.setPrimitives(nativeMeshBuilder.getPrimitives());
            }
            CoreResourceHandle create = this.mResourceCreator.create(str, str2, coreMeshCreateInfo);
            if (this.mResourceManager.isValid(create)) {
                return new ResourceHandleImpl(this.mEngine, create);
            }
            return getEmptyResourceHandle();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public Optional<MeshDesc> getMeshDesc(ResourceHandle resourceHandle) {
        if (resourceHandle != null) {
            MeshDesc meshDesc = null;
            CoreMesh meshFromHandle = this.mResourceCreator.getMeshFromHandle(ResourceHandleImpl.getNativeHandle(resourceHandle));
            if (meshFromHandle != null) {
                CoreMeshDesc desc = meshFromHandle.getDesc();
                MeshDesc meshDesc2 = new MeshDesc();
                CoreMeshPrimitiveDescArrayView primitives = meshFromHandle.getPrimitives();
                MeshDesc.PrimitiveDesc[] primitiveDescArr = new MeshDesc.PrimitiveDesc[((int) primitives.size())];
                for (int i = 0; i < primitiveDescArr.length; i++) {
                    CoreMeshPrimitiveDesc coreMeshPrimitiveDesc = primitives.get((long) i);
                    primitiveDescArr[i] = new MeshDesc.PrimitiveDesc();
                    primitiveDescArr[i].setMaterial(new ResourceHandleImpl(this.mEngine, coreMeshPrimitiveDesc.getMaterial()));
                }
                meshDesc2.setPrimitives(primitiveDescArr);
                meshDesc2.setBounds(new BoundingBox(Swig.get(desc.getAabbMin()), Swig.get(desc.getAabbMax())));
                meshDesc = meshDesc2;
            }
            return Optional.ofNullable(meshDesc);
        }
        throw new NullPointerException();
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public GpuResourceHandle getEmptyGpuResourceHandle() {
        return new GpuResourceHandleImpl(this.mEngine, new CoreGpuResourceHandle());
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public GpuResourceHandle createTexture(String str, String str2) {
        this.mEngine.requireRenderThread();
        CoreGpuResourceHandle createImage = Core.createImage(this.mEngine.getAgpContext().getEngine(), str, str2);
        if (!CoreGpuResourceHandleUtil.isValid(createImage)) {
            return getEmptyGpuResourceHandle();
        }
        return new GpuResourceHandleImpl(this.mEngine, createImage);
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public GpuResourceHandle createTexture(String str, int i, int i2, ImageFormat imageFormat) {
        CoreFormat coreFormat;
        this.mEngine.requireRenderThread();
        int i3 = AnonymousClass3.$SwitchMap$ohos$agp$render$render3d$resources$ImageFormat[imageFormat.ordinal()];
        if (i3 == 1) {
            coreFormat = CoreFormat.CORE_FORMAT_R8_UNORM;
        } else if (i3 == 2) {
            coreFormat = CoreFormat.CORE_FORMAT_R8_SRGB;
        } else if (i3 == 3) {
            coreFormat = CoreFormat.CORE_FORMAT_R8G8B8A8_UNORM;
        } else if (i3 != 4) {
            coreFormat = CoreFormat.CORE_FORMAT_UNDEFINED;
        } else {
            coreFormat = CoreFormat.CORE_FORMAT_R8G8B8A8_SRGB;
        }
        EngineImpl engineImpl = this.mEngine;
        return new GpuResourceHandleImpl(engineImpl, Core.createImage(engineImpl.getAgpContext().getEngine(), str, (long) i, (long) i2, coreFormat));
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.agp.render.render3d.impl.ResourceManagerImpl$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$impl$CoreMaterialAlphaMode = new int[CoreMaterialAlphaMode.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$impl$CoreMaterialType = new int[CoreMaterialType.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$resources$ImageFormat = new int[ImageFormat.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$resources$MaterialDesc$MaterialAlphaMode = new int[MaterialDesc.MaterialAlphaMode.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$resources$MaterialDesc$MaterialType = new int[MaterialDesc.MaterialType.values().length];

        static {
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$ImageFormat[ImageFormat.R8_UNORM.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$ImageFormat[ImageFormat.R8_SRGB.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$ImageFormat[ImageFormat.R8G8B8A8_UNORM.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$ImageFormat[ImageFormat.R8G8B8A8_SRGB.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$MaterialDesc$MaterialAlphaMode[MaterialDesc.MaterialAlphaMode.OPAQUE.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$MaterialDesc$MaterialAlphaMode[MaterialDesc.MaterialAlphaMode.MASK.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$MaterialDesc$MaterialAlphaMode[MaterialDesc.MaterialAlphaMode.BLEND.ordinal()] = 3;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$MaterialDesc$MaterialType[MaterialDesc.MaterialType.METALLIC_ROUGHNESS.ordinal()] = 1;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$MaterialDesc$MaterialType[MaterialDesc.MaterialType.SPECULAR_GLOSSINESS.ordinal()] = 2;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$MaterialDesc$MaterialType[MaterialDesc.MaterialType.UNLIT.ordinal()] = 3;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$MaterialDesc$MaterialType[MaterialDesc.MaterialType.UNLIT_SHADOW_ALPHA.ordinal()] = 4;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreMaterialAlphaMode[CoreMaterialAlphaMode.CORE_ALPHA_MODE_OPAQUE.ordinal()] = 1;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreMaterialAlphaMode[CoreMaterialAlphaMode.CORE_ALPHA_MODE_MASK.ordinal()] = 2;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreMaterialAlphaMode[CoreMaterialAlphaMode.CORE_ALPHA_MODE_BLEND.ordinal()] = 3;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreMaterialType[CoreMaterialType.CORE_MATERIAL_METALLIC_ROUGHNESS.ordinal()] = 1;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreMaterialType[CoreMaterialType.CORE_MATERIAL_SPECULAR_GLOSSINESS.ordinal()] = 2;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreMaterialType[CoreMaterialType.CORE_MATERIAL_UNLIT.ordinal()] = 3;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreMaterialType[CoreMaterialType.CORE_MATERIAL_UNLIT_SHADOW_ALPHA.ordinal()] = 4;
            } catch (NoSuchFieldError unused18) {
            }
        }
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public void copyDataToImage(GpuResourceHandle gpuResourceHandle, ByteBuffer byteBuffer, int i, int i2) {
        this.mEngine.requireRenderThread();
        CoreGpuResourceHandle nativeHandle = GpuResourceHandleImpl.getNativeHandle(gpuResourceHandle);
        if (nativeHandle != null) {
            Core.copyDataToImage(this.mEngine.getAgpContext().getEngine(), new CoreByteArrayView(byteBuffer), nativeHandle, (long) i, (long) i2);
            return;
        }
        throw new NullPointerException();
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public GpuResourceHandle createTextureViewOes(String str, int i, int i2, int i3) {
        this.mEngine.requireRenderThread();
        CoreGpuResourceHandle createTextureViewOes = Core.createTextureViewOes(this.mEngine.getAgpContext().getEngine(), str, (long) i, (long) i2, (long) i3);
        if (!CoreGpuResourceHandleUtil.isValid(createTextureViewOes)) {
            return getEmptyGpuResourceHandle();
        }
        return new GpuResourceHandleImpl(this.mEngine, createTextureViewOes);
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public GpuResourceHandle createUniformRingBuffer(String str, int i) {
        CoreGpuResourceHandle createUniformRingBuffer = Core.createUniformRingBuffer(this.mEngine.getAgpContext().getEngine(), str, (long) i);
        if (!CoreGpuResourceHandleUtil.isValid(createUniformRingBuffer)) {
            return getEmptyGpuResourceHandle();
        }
        return new GpuResourceHandleImpl(this.mEngine, createUniformRingBuffer);
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public void updateBuffer(GpuResourceHandle gpuResourceHandle, ByteBuffer byteBuffer) {
        CoreGpuResourceHandle nativeHandle = GpuResourceHandleImpl.getNativeHandle(gpuResourceHandle);
        if (nativeHandle != null) {
            Core.copyDataToBufferOnCpu(this.mEngine.getAgpContext().getEngine(), new CoreByteArrayView(byteBuffer), nativeHandle);
            return;
        }
        throw new NullPointerException();
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public int createExternalTextureOes() {
        this.mEngine.requireRenderThread();
        return this.mEngine.getAgpContext().createTextureOes();
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public void deleteExternalTextureOes(int i) {
        this.mEngine.requireRenderThread();
        this.mEngine.getAgpContext().deleteTextureOes(i);
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public RenderDataStorePod createRenderDataStorePod(String str) {
        this.mEngine.requireRenderThread();
        return new RenderDataStorePodImpl(this.mEngine, Core.createRenderDataStorePod(this.mEngine.getAgpContext().getEngine(), str));
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public Optional<RenderDataStorePod> getRenderDataStorePod(String str) {
        this.mEngine.requireRenderThread();
        CoreRenderDataStorePod renderDataStorePod = Core.getRenderDataStorePod(this.mEngine.getAgpContext().getEngine(), str);
        if (renderDataStorePod == null) {
            return Optional.empty();
        }
        return Optional.of(new RenderDataStorePodImpl(this.mEngine, renderDataStorePod));
    }

    @Override // ohos.agp.render.render3d.resources.ResourceManager
    public Optional<AnimationPlayback> createPlayback(ResourceHandle resourceHandle, SceneNode sceneNode) {
        this.mEngine.requireRenderThread();
        CoreSystem system = this.mEngine.getAgpContext().getGraphicsContext().getEcs().getSystem("AnimationSystem");
        if (system instanceof CoreAnimationSystem) {
            return AnimationPlaybackImpl.createPlayback(this.mEngine, (CoreAnimationSystem) system, resourceHandle, sceneNode);
        }
        throw new ExceptionInInitializerError("AnimationSystem not found");
    }
}
