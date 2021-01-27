package com.huawei.agpengine.resources;

import android.hardware.HardwareBuffer;
import com.huawei.agpengine.SceneNode;
import com.huawei.agpengine.property.PropertyData;
import com.huawei.agpengine.util.MeshBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

public interface ResourceManager {
    public static final int IMAGE_LOADER_FLIP_VERTICALLY_BIT = 16;
    public static final int IMAGE_LOADER_FORCE_GRAYSCALE_BIT = 8;
    public static final int IMAGE_LOADER_FORCE_LINEAR_RGB_BIT = 2;
    public static final int IMAGE_LOADER_FORCE_SRGB_BIT = 4;
    public static final int IMAGE_LOADER_GENERATE_MIPS_BIT = 1;
    public static final int IMAGE_LOADER_PREMULTIPLY_ALPHA = 32;

    public interface ResourceInfo {
        ResourceHandle getHandle();

        String getName();

        String getUri();
    }

    void copyDataToImage(GpuResourceHandle gpuResourceHandle, ByteBuffer byteBuffer, int i, int i2);

    GpuResourceHandle createColorTargetGpuImage(String str, int i, int i2);

    GpuResourceHandle createDepthTargetGpuImage(String str, int i, int i2);

    int createExternalTextureOes();

    GpuResourceHandle createImage(String str, int i, int i2, ImageFormat imageFormat);

    GpuResourceHandle createImage(String str, String str2, int i);

    GpuResourceHandle createImageViewHwBuffer(String str, HardwareBuffer hardwareBuffer);

    ResourceHandle createMaterial(String str, String str2);

    ResourceHandle createMesh(String str, String str2, MeshBuilder meshBuilder);

    Optional<AnimationPlayback> createPlayback(ResourceHandle resourceHandle, SceneNode sceneNode);

    RenderDataStorePod createRenderDataStorePod(String str);

    GpuResourceHandle createTexture(String str, int i, int i2, ImageFormat imageFormat);

    GpuResourceHandle createTexture(String str, String str2);

    GpuResourceHandle createTextureViewOes(String str, int i, int i2, int i3);

    GpuResourceHandle createUniformRingBuffer(String str, int i);

    void deleteExternalTextureOes(int i);

    List<ResourceInfo> getAnimations();

    GpuResourceHandle getEmptyGpuResourceHandle();

    ResourceHandle getEmptyResourceHandle();

    List<ResourceInfo> getImages();

    Optional<MaterialDesc> getMaterialDesc(ResourceHandle resourceHandle);

    List<ResourceInfo> getMaterials();

    Optional<MeshDesc> getMeshDesc(ResourceHandle resourceHandle);

    List<ResourceInfo> getMeshes();

    Optional<RenderDataStorePod> getRenderDataStorePod(String str);

    ResourceHandle getResourceHandle(String str);

    Optional<PropertyData> getResourcePropertyData(ResourceHandle resourceHandle);

    List<ResourceInfo> getResources();

    GpuResourceHandle getShaderHandle(String str);

    List<ResourceInfo> getSkins();

    void setMaterialDesc(ResourceHandle resourceHandle, MaterialDesc materialDesc);

    boolean setResourcePropertyData(ResourceHandle resourceHandle, PropertyData propertyData);

    void updateBuffer(GpuResourceHandle gpuResourceHandle, ByteBuffer byteBuffer);
}
