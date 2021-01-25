package com.huawei.agpengine.gltf;

import com.huawei.agpengine.resources.GpuResourceHandle;
import com.huawei.agpengine.resources.ResourceHandle;
import java.util.List;

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
