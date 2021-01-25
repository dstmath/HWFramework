package ohos.agp.render.render3d.gltf;

import java.util.List;
import ohos.agp.render.render3d.resources.GpuResourceHandle;
import ohos.agp.render.render3d.resources.ResourceHandle;

public interface GltfImportData {
    List<ResourceHandle> getAnimations();

    String getError();

    List<ResourceHandle> getImages();

    List<ResourceHandle> getMaterials();

    List<ResourceHandle> getMeshes();

    List<GpuResourceHandle> getSamplers();

    List<ResourceHandle> getSkins();

    List<GpuResourceHandle> getTextures();

    boolean isValid();

    void release();
}
