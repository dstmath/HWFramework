package com.huawei.agpengine.gltf;

import com.huawei.agpengine.Entity;
import com.huawei.agpengine.Task;
import java.nio.ByteBuffer;

public interface GltfLoader {
    public static final int RESOURCE_IMPORT_BIT_ANIMATION = 64;
    public static final int RESOURCE_IMPORT_BIT_IMAGE = 2;
    public static final int RESOURCE_IMPORT_BIT_MATERIAL = 8;
    public static final int RESOURCE_IMPORT_BIT_MESH = 16;
    public static final int RESOURCE_IMPORT_BIT_SAMPLER = 1;
    public static final int RESOURCE_IMPORT_BIT_SKIN = 32;
    public static final int RESOURCE_IMPORT_BIT_TEXTURE = 4;
    public static final int RESOURCE_IMPORT_FLAG_BITS_ALL = Integer.MAX_VALUE;
    public static final int SCENE_IMPORT_BIT_CAMERA = 4;
    public static final int SCENE_IMPORT_BIT_LIGHT = 16;
    public static final int SCENE_IMPORT_BIT_MESH = 2;
    public static final int SCENE_IMPORT_BIT_MORPH = 32;
    public static final int SCENE_IMPORT_BIT_SCENE = 1;
    public static final int SCENE_IMPORT_BIT_SKIN = 8;
    public static final int SCENE_IMPORT_FLAG_BITS_ALL = Integer.MAX_VALUE;

    public interface ImportListener {
        void onGltfImportFinished(String str, GltfImportData gltfImportData);
    }

    int getDefaultResourceImportFlags();

    int getDefaultSceneImportFlags();

    GltfImportData importGltf(GltfData gltfData, int i);

    Task importGltfAsync(GltfData gltfData, int i, ImportListener importListener);

    Entity importScene(int i, GltfData gltfData, GltfImportData gltfImportData, Entity entity, int i2);

    GltfData loadGltf(String str);

    GltfData loadGltf(ByteBuffer byteBuffer);
}
