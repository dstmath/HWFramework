package ohos.agp.render.render3d.gltf;

import java.util.List;

public interface GltfData {
    public static final int INVALID_INDEX = -1;

    String getBaseUri();

    int getDefaultSceneIndex();

    String getError();

    List<String> getExternalUris();

    String getGltfUri();

    int getSceneCount();

    int getThumbnailCount();

    boolean isValid();

    void release();

    void releaseBuffers();
}
