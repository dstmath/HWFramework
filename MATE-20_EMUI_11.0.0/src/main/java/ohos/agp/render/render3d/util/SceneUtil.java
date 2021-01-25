package ohos.agp.render.render3d.util;

import java.util.List;
import ohos.agp.render.render3d.Entity;
import ohos.agp.render.render3d.SceneNode;
import ohos.agp.render.render3d.math.Quaternion;
import ohos.agp.render.render3d.math.Vector2;
import ohos.agp.render.render3d.math.Vector3;
import ohos.agp.render.render3d.resources.ResourceHandle;

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
