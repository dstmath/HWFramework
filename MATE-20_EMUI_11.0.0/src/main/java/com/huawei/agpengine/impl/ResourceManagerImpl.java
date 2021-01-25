package com.huawei.agpengine.impl;

import android.hardware.HardwareBuffer;
import com.huawei.agpengine.SceneNode;
import com.huawei.agpengine.impl.CoreResourceCreator;
import com.huawei.agpengine.impl.CoreResourceManager;
import com.huawei.agpengine.property.PropertyData;
import com.huawei.agpengine.resources.AnimationPlayback;
import com.huawei.agpengine.resources.GpuResourceHandle;
import com.huawei.agpengine.resources.ImageFormat;
import com.huawei.agpengine.resources.MaterialDesc;
import com.huawei.agpengine.resources.MeshDesc;
import com.huawei.agpengine.resources.RenderDataStorePod;
import com.huawei.agpengine.resources.ResourceHandle;
import com.huawei.agpengine.resources.ResourceManager;
import com.huawei.agpengine.util.BoundingBox;
import com.huawei.agpengine.util.MeshBuilder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class ResourceManagerImpl implements ResourceManager {
    private static final String PARAM_DATA = "data";
    private static final String PARAM_DESTINATION = "destination";
    private static final String PARAM_HANDLE = "handle";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_NODE = "node";
    private static final String PARAM_RESOURCE = "resource";
    private static final String PARAM_URI = "uri";
    private final EngineImpl mEngine;
    private final CoreResourceCreator mResourceCreator = this.mEngine.getAgpContext().getGraphicsContext().getResourceCreator();
    private final CoreResourceManager mResourceManager = this.mEngine.getAgpContext().getEngine().getResourceManager();

    private static void notNull(Object param, String paramName) {
        if (param == null) {
            throw new NullPointerException(paramName + " must not be null");
        }
    }

    ResourceManagerImpl(EngineImpl engine) {
        this.mEngine = engine;
        if (this.mResourceManager == null || this.mResourceCreator == null) {
            throw new IllegalStateException("Internal graphics engine error");
        }
    }

    private List<ResourceManager.ResourceInfo> convertInfos(CoreResourceInfoArray infoArray) {
        int infoCount = infoArray.size();
        List<ResourceManager.ResourceInfo> results = new ArrayList<>(infoCount);
        for (int i = 0; i < infoCount; i++) {
            final CoreResourceManager.CoreResourceInfo nativeInfo = infoArray.get(i);
            final CoreResourceHandle nativeHandleCopy = new CoreResourceHandle();
            CoreResourceHandle nativeHandle = nativeInfo.getHandle();
            if (nativeHandle != null) {
                nativeHandleCopy.setId(nativeHandle.getId());
                nativeHandleCopy.setType(nativeHandle.getType());
                results.add(new ResourceManager.ResourceInfo() {
                    /* class com.huawei.agpengine.impl.ResourceManagerImpl.AnonymousClass1 */
                    private ResourceHandle mHandle = new ResourceHandleImpl(ResourceManagerImpl.this.mEngine, nativeHandleCopy);
                    private String mName = nativeInfo.getName();
                    private String mUri = nativeInfo.getUri();

                    @Override // com.huawei.agpengine.resources.ResourceManager.ResourceInfo
                    public String getUri() {
                        return this.mUri;
                    }

                    @Override // com.huawei.agpengine.resources.ResourceManager.ResourceInfo
                    public String getName() {
                        return this.mName;
                    }

                    @Override // com.huawei.agpengine.resources.ResourceManager.ResourceInfo
                    public ResourceHandle getHandle() {
                        return this.mHandle;
                    }
                });
            } else {
                throw new IllegalStateException("Internal graphics engine error");
            }
        }
        return results;
    }

    private List<ResourceManager.ResourceInfo> convertCreatorInfos(CoreResourceCreatorInfoArray infoArray) {
        int infoCount = infoArray.size();
        List<ResourceManager.ResourceInfo> results = new ArrayList<>(infoCount);
        for (int i = 0; i < infoCount; i++) {
            final CoreResourceCreator.CoreInfo nativeInfo = infoArray.get(i);
            final CoreResourceHandle nativeHandleCopy = new CoreResourceHandle();
            CoreResourceHandle nativeHandle = nativeInfo.getHandle();
            if (nativeHandle != null) {
                nativeHandleCopy.setId(nativeHandle.getId());
                nativeHandleCopy.setType(nativeHandle.getType());
                results.add(new ResourceManager.ResourceInfo() {
                    /* class com.huawei.agpengine.impl.ResourceManagerImpl.AnonymousClass2 */
                    private ResourceHandle mHandle = new ResourceHandleImpl(ResourceManagerImpl.this.mEngine, nativeHandleCopy);
                    private String mName = nativeInfo.getName();
                    private String mUri = nativeInfo.getUri();

                    @Override // com.huawei.agpengine.resources.ResourceManager.ResourceInfo
                    public String getUri() {
                        return this.mUri;
                    }

                    @Override // com.huawei.agpengine.resources.ResourceManager.ResourceInfo
                    public String getName() {
                        return this.mName;
                    }

                    @Override // com.huawei.agpengine.resources.ResourceManager.ResourceInfo
                    public ResourceHandle getHandle() {
                        return this.mHandle;
                    }
                });
            } else {
                throw new IllegalStateException("Internal graphics engine error");
            }
        }
        return results;
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getResources() {
        return convertInfos(this.mResourceManager.getResources());
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getMaterials() {
        return convertCreatorInfos(this.mResourceCreator.getMaterials());
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getMeshes() {
        return convertCreatorInfos(this.mResourceCreator.getMeshes());
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getAnimations() {
        return convertCreatorInfos(this.mResourceCreator.getAnimations());
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getSkins() {
        return convertCreatorInfos(this.mResourceCreator.getSkins());
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public List<ResourceManager.ResourceInfo> getImages() {
        return convertCreatorInfos(this.mResourceCreator.getImages());
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public ResourceHandle getEmptyResourceHandle() {
        return new ResourceHandleImpl(this.mEngine, new CoreResourceHandle());
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public ResourceHandle getResourceHandle(String uri) {
        notNull(uri, PARAM_URI);
        CoreResourceHandle handle = this.mResourceCreator.getHandle(uri);
        if (this.mResourceManager.isValid(handle)) {
            return new ResourceHandleImpl(this.mEngine, handle);
        }
        return getEmptyResourceHandle();
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public Optional<PropertyData> getResourcePropertyData(ResourceHandle resource) {
        CorePropertyHandle propertyHandle;
        notNull(resource, PARAM_RESOURCE);
        CoreResource nativeResource = this.mResourceManager.getResourceFromHandle(ResourceHandleImpl.getNativeHandle(resource));
        if (!(nativeResource == null || (propertyHandle = nativeResource.getProperties()) == null)) {
            PropertyDataImpl data = new PropertyDataImpl(propertyHandle);
            if (PropertyDataImpl.readFromPropertyHandle(propertyHandle, data.getData())) {
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public boolean setResourcePropertyData(ResourceHandle resource, PropertyData data) {
        CorePropertyHandle propertyHandle;
        notNull(resource, PARAM_RESOURCE);
        notNull(resource, PARAM_DATA);
        CoreResource nativeResource = this.mResourceManager.getResourceFromHandle(ResourceHandleImpl.getNativeHandle(resource));
        if (nativeResource == null || (propertyHandle = nativeResource.getProperties()) == null) {
            return false;
        }
        return PropertyDataImpl.writeToPropertyHandle(propertyHandle, data.getData());
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public ResourceHandle createMaterial(String uri, String name) {
        notNull(uri, PARAM_URI);
        notNull(name, PARAM_NAME);
        this.mEngine.requireRenderThread();
        CoreResourceHandle handle = this.mResourceCreator.create(uri, name, new CoreMaterialCreateInfo());
        if (this.mResourceManager.isValid(handle)) {
            return new ResourceHandleImpl(this.mEngine, handle);
        }
        return getEmptyResourceHandle();
    }

    private MaterialDesc.MaterialType getMaterialDescType(CoreMaterialType nativeMaterialType) {
        int i = AnonymousClass3.$SwitchMap$com$huawei$agpengine$impl$CoreMaterialType[nativeMaterialType.ordinal()];
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

    private MaterialDesc.MaterialAlphaMode getMaterialAlphaMode(CoreMaterialAlphaMode nativeAlphaMode) {
        int i = AnonymousClass3.$SwitchMap$com$huawei$agpengine$impl$CoreMaterialAlphaMode[nativeAlphaMode.ordinal()];
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

    @Override // com.huawei.agpengine.resources.ResourceManager
    public Optional<MaterialDesc> getMaterialDesc(ResourceHandle resource) {
        notNull(resource, PARAM_RESOURCE);
        this.mEngine.requireRenderThread();
        if (!resource.isValid()) {
            return Optional.empty();
        }
        MaterialDesc materialDesc = null;
        CoreMaterial material = this.mResourceCreator.getMaterialFromHandle(ResourceHandleImpl.getNativeHandle(resource));
        if (material != null) {
            CoreMaterialDesc desc = material.getDesc();
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
            materialDesc.setMaterialExtraFlags((int) desc.getExtraMaterialRenderingFlags());
            materialDesc.setCustomMaterialShader(new GpuResourceHandleImpl(this.mEngine, desc.getCustomMaterialShader()));
        }
        return Optional.ofNullable(materialDesc);
    }

    private CoreMaterialType getNativeMaterialDescType(MaterialDesc.MaterialType materialType) {
        int i = AnonymousClass3.$SwitchMap$com$huawei$agpengine$resources$MaterialDesc$MaterialType[materialType.ordinal()];
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

    private CoreMaterialAlphaMode getNativeMaterialAlphaMode(MaterialDesc.MaterialAlphaMode alphaMode) {
        int i = AnonymousClass3.$SwitchMap$com$huawei$agpengine$resources$MaterialDesc$MaterialAlphaMode[alphaMode.ordinal()];
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

    @Override // com.huawei.agpengine.resources.ResourceManager
    public void setMaterialDesc(ResourceHandle resource, MaterialDesc materialDesc) {
        notNull(resource, PARAM_RESOURCE);
        notNull(materialDesc, "materialDesc");
        this.mEngine.requireRenderThread();
        CoreMaterial material = this.mResourceCreator.getMaterialFromHandle(ResourceHandleImpl.getNativeHandle(resource));
        if (material != null) {
            CoreMaterialDesc desc = new CoreMaterialDesc();
            desc.setType(getNativeMaterialDescType(materialDesc.getType()));
            desc.setBaseColor(materialDesc.getBaseColor().getNativeHandle());
            desc.setNormal(materialDesc.getNormal().getNativeHandle());
            desc.setEmissive(materialDesc.getEmissive().getNativeHandle());
            desc.setAo(materialDesc.getAmbientOcclusion().getNativeHandle());
            desc.setMaterial(materialDesc.getMaterial().getNativeHandle());
            desc.setSampler(materialDesc.getSampler().getNativeHandle());
            desc.setBaseColorFactor(Swig.set(materialDesc.getBaseColorFactor()));
            desc.setEmissiveFactor(Swig.set(materialDesc.getEmissiveFactor()));
            desc.setAmbientOcclusionFactor(materialDesc.getAmbientOcclusionFactor());
            desc.setRoughnessFactor(materialDesc.getRoughnessFactor());
            desc.setMetallicFactor(materialDesc.getMetallicFactor());
            desc.setReflectance(materialDesc.getReflectance());
            desc.setNormalScale(materialDesc.getNormalScale());
            desc.setSpecularFactor(Swig.set(materialDesc.getSpecularFactor()));
            desc.setNormalScale(materialDesc.getGlossinessFactor());
            desc.setClearCoatFactor(materialDesc.getClearCoatFactor());
            desc.setClearCoatRoughness(materialDesc.getClearCoatRoughness());
            desc.setAlphaMode(getNativeMaterialAlphaMode(materialDesc.getAlphaMode()));
            desc.setAlphaCutoff(materialDesc.getAlphaCutoff());
            desc.setMaterialFlags((long) materialDesc.getMaterialFlags());
            desc.setExtraMaterialRenderingFlags((long) materialDesc.getMaterialExtraFlags());
            desc.setCustomMaterialShader(materialDesc.getCustomMaterialShader().getNativeHandle());
            material.setDesc(desc);
        }
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public ResourceHandle createMesh(String uri, String name, MeshBuilder meshBuilder) {
        notNull(uri, PARAM_URI);
        notNull(name, PARAM_NAME);
        notNull(meshBuilder, "meshBuilder");
        if (meshBuilder instanceof MeshBuilderImpl) {
            this.mEngine.requireRenderThread();
            CoreMeshBuilder nativeMeshBuilder = ((MeshBuilderImpl) meshBuilder).getNativeMeshBuilder();
            CoreMeshCreateInfo meshInfo = new CoreMeshCreateInfo();
            if (nativeMeshBuilder != null) {
                meshInfo.setVertexCount(nativeMeshBuilder.getVertexCount());
                meshInfo.setIndexCount(nativeMeshBuilder.getIndexCount());
                meshInfo.setVertexData(nativeMeshBuilder.getVertexData());
                meshInfo.setIndexData(nativeMeshBuilder.getIndexData());
                meshInfo.setPrimitives(nativeMeshBuilder.getPrimitives());
            }
            CoreResourceHandle handle = this.mResourceCreator.create(uri, name, meshInfo);
            if (this.mResourceManager.isValid(handle)) {
                return new ResourceHandleImpl(this.mEngine, handle);
            }
            return getEmptyResourceHandle();
        }
        throw new IllegalArgumentException("Unsupported MeshBuilder.");
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public Optional<MeshDesc> getMeshDesc(ResourceHandle resource) {
        notNull(resource, PARAM_RESOURCE);
        MeshDesc meshDesc = null;
        CoreMesh mesh = this.mResourceCreator.getMeshFromHandle(ResourceHandleImpl.getNativeHandle(resource));
        if (mesh != null) {
            CoreMeshDesc desc = mesh.getDesc();
            meshDesc = new MeshDesc();
            CoreMeshPrimitiveDescArrayView primitives = mesh.getPrimitives();
            MeshDesc.PrimitiveDesc[] primitiveDescs = new MeshDesc.PrimitiveDesc[((int) primitives.size())];
            for (int i = 0; i < primitiveDescs.length; i++) {
                CoreMeshPrimitiveDesc primitiveDesc = primitives.get((long) i);
                if (primitiveDesc != null) {
                    primitiveDescs[i] = new MeshDesc.PrimitiveDesc();
                    primitiveDescs[i].setMaterial(new ResourceHandleImpl(this.mEngine, primitiveDesc.getMaterial()));
                } else {
                    throw new IllegalStateException("Internal graphics engine error");
                }
            }
            meshDesc.setPrimitives(primitiveDescs);
            meshDesc.setBounds(new BoundingBox(Swig.get(desc.getAabbMin()), Swig.get(desc.getAabbMax())));
        }
        return Optional.ofNullable(meshDesc);
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public GpuResourceHandle getEmptyGpuResourceHandle() {
        return new GpuResourceHandleImpl(this.mEngine);
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public GpuResourceHandle createTexture(String name, String imageUri) {
        return createImage(name, imageUri, 0);
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public GpuResourceHandle createTexture(String name, int width, int height, ImageFormat format) {
        return createImage(name, width, height, format);
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public GpuResourceHandle createImage(String name, int width, int height, ImageFormat format) {
        CoreFormat nativeFormat;
        notNull(name, PARAM_RESOURCE);
        notNull(format, "format");
        this.mEngine.requireRenderThread();
        int i = AnonymousClass3.$SwitchMap$com$huawei$agpengine$resources$ImageFormat[format.ordinal()];
        if (i == 1) {
            nativeFormat = CoreFormat.CORE_FORMAT_R8_UNORM;
        } else if (i == 2) {
            nativeFormat = CoreFormat.CORE_FORMAT_R8_SRGB;
        } else if (i == 3) {
            nativeFormat = CoreFormat.CORE_FORMAT_R8G8B8A8_UNORM;
        } else if (i != 4) {
            nativeFormat = CoreFormat.CORE_FORMAT_UNDEFINED;
        } else {
            nativeFormat = CoreFormat.CORE_FORMAT_R8G8B8A8_SRGB;
        }
        EngineImpl engineImpl = this.mEngine;
        return new GpuResourceHandleImpl(engineImpl, Core.createImage(engineImpl.getAgpContext().getEngine(), name, (long) width, (long) height, nativeFormat));
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.agpengine.impl.ResourceManagerImpl$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$impl$CoreMaterialAlphaMode = new int[CoreMaterialAlphaMode.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$impl$CoreMaterialType = new int[CoreMaterialType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$resources$ImageFormat = new int[ImageFormat.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$resources$MaterialDesc$MaterialAlphaMode = new int[MaterialDesc.MaterialAlphaMode.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$resources$MaterialDesc$MaterialType = new int[MaterialDesc.MaterialType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$agpengine$resources$ImageFormat[ImageFormat.R8_UNORM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$ImageFormat[ImageFormat.R8_SRGB.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$ImageFormat[ImageFormat.R8G8B8A8_UNORM.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$ImageFormat[ImageFormat.R8G8B8A8_SRGB.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$MaterialDesc$MaterialAlphaMode[MaterialDesc.MaterialAlphaMode.OPAQUE.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$MaterialDesc$MaterialAlphaMode[MaterialDesc.MaterialAlphaMode.MASK.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$MaterialDesc$MaterialAlphaMode[MaterialDesc.MaterialAlphaMode.BLEND.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$MaterialDesc$MaterialType[MaterialDesc.MaterialType.METALLIC_ROUGHNESS.ordinal()] = 1;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$MaterialDesc$MaterialType[MaterialDesc.MaterialType.SPECULAR_GLOSSINESS.ordinal()] = 2;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$MaterialDesc$MaterialType[MaterialDesc.MaterialType.UNLIT.ordinal()] = 3;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$MaterialDesc$MaterialType[MaterialDesc.MaterialType.UNLIT_SHADOW_ALPHA.ordinal()] = 4;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreMaterialAlphaMode[CoreMaterialAlphaMode.CORE_ALPHA_MODE_OPAQUE.ordinal()] = 1;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreMaterialAlphaMode[CoreMaterialAlphaMode.CORE_ALPHA_MODE_MASK.ordinal()] = 2;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreMaterialAlphaMode[CoreMaterialAlphaMode.CORE_ALPHA_MODE_BLEND.ordinal()] = 3;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreMaterialType[CoreMaterialType.CORE_MATERIAL_METALLIC_ROUGHNESS.ordinal()] = 1;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreMaterialType[CoreMaterialType.CORE_MATERIAL_SPECULAR_GLOSSINESS.ordinal()] = 2;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreMaterialType[CoreMaterialType.CORE_MATERIAL_UNLIT.ordinal()] = 3;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreMaterialType[CoreMaterialType.CORE_MATERIAL_UNLIT_SHADOW_ALPHA.ordinal()] = 4;
            } catch (NoSuchFieldError e18) {
            }
        }
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public GpuResourceHandle createImage(String name, String imageUri, int imageLoaderFlags) {
        notNull(name, PARAM_RESOURCE);
        notNull(imageUri, PARAM_URI);
        this.mEngine.requireRenderThread();
        long nativeHandle = Core.createImage(this.mEngine.getAgpContext().getEngine(), name, imageUri, imageLoaderFlags);
        if (!Core.isGpuResourceHandleValid(nativeHandle)) {
            return getEmptyGpuResourceHandle();
        }
        return new GpuResourceHandleImpl(this.mEngine, nativeHandle);
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public void copyDataToImage(GpuResourceHandle destination, ByteBuffer data, int width, int height) {
        notNull(destination, PARAM_DESTINATION);
        notNull(data, PARAM_DATA);
        this.mEngine.requireRenderThread();
        long nativeHandle = destination.getNativeHandle();
        CoreEngine nativeEngine = this.mEngine.getAgpContext().getEngine();
        if (nativeEngine.getGpuResourceManager().isGpuImage(nativeHandle)) {
            Core.copyDataToImage(nativeEngine, new CoreByteArrayView(data), nativeHandle, (long) width, (long) height);
            return;
        }
        throw new IllegalArgumentException("destination must be a valid GpuImage resource.");
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public GpuResourceHandle createTextureViewOes(String name, int width, int height, int textureId) {
        notNull(name, PARAM_NAME);
        this.mEngine.requireRenderThread();
        long nativeHandle = Core.createTextureViewOes(this.mEngine.getAgpContext().getEngine(), name, (long) width, (long) height, (long) textureId);
        if (!Core.isGpuResourceHandleValid(nativeHandle)) {
            return getEmptyGpuResourceHandle();
        }
        return new GpuResourceHandleImpl(this.mEngine, nativeHandle);
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public GpuResourceHandle createImageViewHwBuffer(String name, HardwareBuffer hwBuffer) {
        notNull(name, PARAM_NAME);
        notNull(hwBuffer, "hwBuffer");
        this.mEngine.requireRenderThread();
        long nativeHandle = Core.createImageViewHwBuffer(this.mEngine.getAgpContext().getEngine(), name, hwBuffer);
        if (!Core.isGpuResourceHandleValid(nativeHandle)) {
            return getEmptyGpuResourceHandle();
        }
        return new GpuResourceHandleImpl(this.mEngine, nativeHandle);
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public GpuResourceHandle createUniformRingBuffer(String name, int byteSize) {
        notNull(name, PARAM_NAME);
        long nativeHandle = Core.createUniformRingBuffer(this.mEngine.getAgpContext().getEngine(), name, (long) byteSize);
        if (!Core.isGpuResourceHandleValid(nativeHandle)) {
            return getEmptyGpuResourceHandle();
        }
        return new GpuResourceHandleImpl(this.mEngine, nativeHandle);
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public void updateBuffer(GpuResourceHandle destination, ByteBuffer data) {
        notNull(destination, PARAM_DESTINATION);
        notNull(data, PARAM_DATA);
        if (destination.isValid()) {
            Core.copyDataToBufferOnCpu(this.mEngine.getAgpContext().getEngine(), new CoreByteArrayView(data), destination.getNativeHandle());
            return;
        }
        throw new IllegalArgumentException("destination buffer must be valid.");
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public int createExternalTextureOes() {
        return this.mEngine.getAgpContext().createTextureOes();
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public void deleteExternalTextureOes(int textureId) {
        this.mEngine.getAgpContext().deleteTextureOes(textureId);
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public RenderDataStorePod createRenderDataStorePod(String name) {
        notNull(name, PARAM_NAME);
        this.mEngine.requireRenderThread();
        return new RenderDataStorePodImpl(this.mEngine, Core.createRenderDataStorePod(this.mEngine.getAgpContext().getEngine(), name));
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public Optional<RenderDataStorePod> getRenderDataStorePod(String name) {
        notNull(name, PARAM_NAME);
        this.mEngine.requireRenderThread();
        CoreRenderDataStorePod pod = Core.getRenderDataStorePod(this.mEngine.getAgpContext().getEngine(), name);
        if (pod == null) {
            return Optional.empty();
        }
        return Optional.of(new RenderDataStorePodImpl(this.mEngine, pod));
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public Optional<AnimationPlayback> createPlayback(ResourceHandle animationHandle, SceneNode node) {
        notNull(animationHandle, PARAM_HANDLE);
        notNull(node, PARAM_NODE);
        this.mEngine.requireRenderThread();
        CoreSystem system = this.mEngine.getAgpContext().getGraphicsContext().getEcs().getSystem("AnimationSystem");
        if (system instanceof CoreAnimationSystem) {
            return AnimationPlaybackImpl.createPlayback(this.mEngine, (CoreAnimationSystem) system, animationHandle, node);
        }
        throw new ExceptionInInitializerError("AnimationSystem not found");
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public GpuResourceHandle getShaderHandle(String name) {
        notNull(name, PARAM_NAME);
        this.mEngine.requireRenderThread();
        EngineImpl engineImpl = this.mEngine;
        return new GpuResourceHandleImpl(engineImpl, Core.getShaderHandle(engineImpl.getAgpContext().getEngine(), name));
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public GpuResourceHandle createColorTargetGpuImage(String name, int width, int height) {
        notNull(name, PARAM_NAME);
        this.mEngine.requireRenderThread();
        EngineImpl engineImpl = this.mEngine;
        return new GpuResourceHandleImpl(engineImpl, Core.createColorTargetGpuImage(engineImpl.getAgpContext().getEngine(), name, (long) width, (long) height));
    }

    @Override // com.huawei.agpengine.resources.ResourceManager
    public GpuResourceHandle createDepthTargetGpuImage(String name, int width, int height) {
        notNull(name, PARAM_NAME);
        this.mEngine.requireRenderThread();
        EngineImpl engineImpl = this.mEngine;
        return new GpuResourceHandleImpl(engineImpl, Core.createDepthTargetGpuImage(engineImpl.getAgpContext().getEngine(), name, (long) width, (long) height));
    }
}
