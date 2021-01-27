package ohos.agp.render.render3d.resources;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import ohos.agp.render.render3d.SceneNode;
import ohos.agp.render.render3d.util.MeshBuilder;

public interface ResourceManager {

    public interface ResourceInfo {
        ResourceHandle getHandle();

        String getName();

        String getUri();
    }

    void copyDataToImage(GpuResourceHandle gpuResourceHandle, ByteBuffer byteBuffer, int i, int i2);

    int createExternalTextureOes();

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

    List<ResourceInfo> getResources();

    List<ResourceInfo> getSkins();

    void setMaterialDesc(ResourceHandle resourceHandle, MaterialDesc materialDesc);

    void updateBuffer(GpuResourceHandle gpuResourceHandle, ByteBuffer byteBuffer);
}
