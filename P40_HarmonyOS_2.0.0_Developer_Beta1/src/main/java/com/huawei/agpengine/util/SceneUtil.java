package com.huawei.agpengine.util;

import com.huawei.agpengine.Entity;
import com.huawei.agpengine.SceneNode;
import com.huawei.agpengine.math.Quaternion;
import com.huawei.agpengine.math.Vector2;
import com.huawei.agpengine.math.Vector3;
import com.huawei.agpengine.resources.ResourceHandle;
import java.util.List;

public interface SceneUtil {

    public interface RayCastResult {
        float getDistance();

        SceneNode getNode();
    }

    Entity createLightDirectional(Quaternion quaternion, boolean z, Vector3 vector3, float f);

    Entity createLightPoint(Vector3 vector3, boolean z, Vector3 vector32, float f);

    Entity createLightSpot(Vector3 vector3, Quaternion quaternion, boolean z, Vector3 vector32, float f);

    Entity createPerspectiveCamera(Vector3 vector3, Quaternion quaternion, float f, float f2, float f3);

    Entity generateCone(String str, ResourceHandle resourceHandle, float f, float f2, int i);

    ResourceHandle generateConeMesh(String str, ResourceHandle resourceHandle, float f, float f2, int i);

    Entity generateCube(String str, ResourceHandle resourceHandle, float f, float f2, float f3);

    ResourceHandle generateCubeMesh(String str, ResourceHandle resourceHandle, float f, float f2, float f3);

    Entity generatePlane(String str, ResourceHandle resourceHandle, float f, float f2);

    ResourceHandle generatePlaneMesh(String str, ResourceHandle resourceHandle, float f, float f2);

    Entity generateSphere(String str, ResourceHandle resourceHandle, float f, int i, int i2);

    ResourceHandle generateSphereMesh(String str, ResourceHandle resourceHandle, float f, int i, int i2);

    BoundingBox getBoundsUsingTransform(Entity entity, boolean z);

    BoundingBox getBoundsUsingWorldMatrix(Entity entity, boolean z);

    List<RayCastResult> rayCast(Vector3 vector3, Vector3 vector32);

    List<RayCastResult> rayCastFromCamera(Entity entity, Vector2 vector2);

    Vector3 screenToWorld(Entity entity, Vector3 vector3);

    void updateCameraViewport(Entity entity, int i, int i2);

    void updateCameraViewport(Entity entity, int i, int i2, boolean z, float f);

    Vector3 worldToScreen(Entity entity, Vector3 vector3);
}
