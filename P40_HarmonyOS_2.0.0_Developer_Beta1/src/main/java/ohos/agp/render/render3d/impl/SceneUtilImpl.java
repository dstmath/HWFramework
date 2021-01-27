package ohos.agp.render.render3d.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.agp.render.render3d.Entity;
import ohos.agp.render.render3d.SceneNode;
import ohos.agp.render.render3d.components.CameraComponent;
import ohos.agp.render.render3d.components.LightComponent;
import ohos.agp.render.render3d.components.LocalMatrixComponent;
import ohos.agp.render.render3d.components.NodeComponent;
import ohos.agp.render.render3d.components.TransformComponent;
import ohos.agp.render.render3d.components.WorldMatrixComponent;
import ohos.agp.render.render3d.math.Quaternion;
import ohos.agp.render.render3d.math.Vector2;
import ohos.agp.render.render3d.math.Vector3;
import ohos.agp.render.render3d.resources.ResourceHandle;
import ohos.agp.render.render3d.util.BoundingBox;
import ohos.agp.render.render3d.util.SceneUtil;

/* access modifiers changed from: package-private */
public class SceneUtilImpl implements SceneUtil {
    private static final int DEFAULT_SHADOW_MAP_SIZE = 1024;
    private final EngineImpl mEngine;
    private final SceneImpl mScene;

    /* access modifiers changed from: private */
    public static class RayCastResultImpl implements SceneUtil.RayCastResult {
        private final float mDistance;
        private final SceneNode mNode;

        RayCastResultImpl(SceneNode sceneNode, float f) {
            this.mNode = sceneNode;
            this.mDistance = f;
        }

        @Override // ohos.agp.render.render3d.util.SceneUtil.RayCastResult
        public SceneNode getNode() {
            return this.mNode;
        }

        @Override // ohos.agp.render.render3d.util.SceneUtil.RayCastResult
        public float getDistance() {
            return this.mDistance;
        }
    }

    SceneUtilImpl(EngineImpl engineImpl, SceneImpl sceneImpl) {
        if (engineImpl == null || sceneImpl == null) {
            throw new NullPointerException();
        }
        this.mEngine = engineImpl;
        this.mScene = sceneImpl;
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public ResourceHandle generateCubeMesh(String str, ResourceHandle resourceHandle, float f, float f2, float f3) {
        this.mEngine.requireRenderThread();
        return new ResourceHandleImpl(this.mEngine, Core.generateCubeMesh(this.mEngine.getAgpContext().getGraphicsContext().getResourceCreator(), str, ResourceHandleImpl.getNativeHandle(resourceHandle), f, f2, f3));
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public ResourceHandle generatePlaneMesh(String str, ResourceHandle resourceHandle, float f, float f2) {
        this.mEngine.requireRenderThread();
        return new ResourceHandleImpl(this.mEngine, Core.generatePlaneMesh(this.mEngine.getAgpContext().getGraphicsContext().getResourceCreator(), str, ResourceHandleImpl.getNativeHandle(resourceHandle), f, f2));
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public ResourceHandle generateSphereMesh(String str, ResourceHandle resourceHandle, float f, int i, int i2) {
        this.mEngine.requireRenderThread();
        return new ResourceHandleImpl(this.mEngine, Core.generateSphereMesh(this.mEngine.getAgpContext().getGraphicsContext().getResourceCreator(), str, ResourceHandleImpl.getNativeHandle(resourceHandle), f, (long) i, (long) i2));
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public ResourceHandle generateConeMesh(String str, ResourceHandle resourceHandle, float f, float f2, int i) {
        this.mEngine.requireRenderThread();
        return new ResourceHandleImpl(this.mEngine, Core.generateConeMesh(this.mEngine.getAgpContext().getGraphicsContext().getResourceCreator(), str, ResourceHandleImpl.getNativeHandle(resourceHandle), f, f2, (long) i));
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public Entity generateCube(String str, ResourceHandle resourceHandle, float f, float f2, float f3) {
        this.mEngine.requireRenderThread();
        CoreGraphicsContext graphicsContext = this.mEngine.getAgpContext().getGraphicsContext();
        return this.mEngine.getEntity(Core.generateCube(graphicsContext.getEcs(), graphicsContext.getResourceCreator(), str, ResourceHandleImpl.getNativeHandle(resourceHandle), f, f2, f3));
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public Entity generatePlane(String str, ResourceHandle resourceHandle, float f, float f2) {
        this.mEngine.requireRenderThread();
        CoreGraphicsContext graphicsContext = this.mEngine.getAgpContext().getGraphicsContext();
        return this.mEngine.getEntity(Core.generatePlane(graphicsContext.getEcs(), graphicsContext.getResourceCreator(), str, ResourceHandleImpl.getNativeHandle(resourceHandle), f, f2));
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public Entity generateSphere(String str, ResourceHandle resourceHandle, float f, int i, int i2) {
        this.mEngine.requireRenderThread();
        CoreGraphicsContext graphicsContext = this.mEngine.getAgpContext().getGraphicsContext();
        return this.mEngine.getEntity(Core.generateSphere(graphicsContext.getEcs(), graphicsContext.getResourceCreator(), str, ResourceHandleImpl.getNativeHandle(resourceHandle), f, (long) i, (long) i2));
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public Entity generateCone(String str, ResourceHandle resourceHandle, float f, float f2, int i) {
        this.mEngine.requireRenderThread();
        CoreGraphicsContext graphicsContext = this.mEngine.getAgpContext().getGraphicsContext();
        return this.mEngine.getEntity(Core.generateCone(graphicsContext.getEcs(), graphicsContext.getResourceCreator(), str, ResourceHandleImpl.getNativeHandle(resourceHandle), f, f2, (long) i));
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public Vector3 screenToWorld(Entity entity, Vector3 vector3) {
        if (entity == null) {
            throw new NullPointerException();
        } else if (vector3 != null) {
            return Swig.get(Core.screenToWorld(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), this.mEngine.getNativeEntity(entity), Swig.set(vector3)));
        } else {
            throw new NullPointerException();
        }
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public Vector3 worldToScreen(Entity entity, Vector3 vector3) {
        if (entity == null) {
            throw new NullPointerException();
        } else if (vector3 != null) {
            return Swig.get(Core.worldToScreen(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), this.mEngine.getNativeEntity(entity), Swig.set(vector3)));
        } else {
            throw new NullPointerException();
        }
    }

    private List<SceneUtil.RayCastResult> convertRayCast(CoreRayCastResultArray coreRayCastResultArray) {
        ArrayList arrayList = new ArrayList(coreRayCastResultArray.size());
        for (int i = 0; i < coreRayCastResultArray.size(); i++) {
            CoreRayCastResult coreRayCastResult = coreRayCastResultArray.get(i);
            Optional<? extends SceneNode> node = this.mEngine.getNode(coreRayCastResult.getNode());
            if (node.isPresent()) {
                arrayList.add(new RayCastResultImpl((SceneNode) node.get(), coreRayCastResult.getDistance()));
            } else {
                throw new IllegalStateException();
            }
        }
        return arrayList;
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public List<SceneUtil.RayCastResult> rayCast(Vector3 vector3, Vector3 vector32) {
        return convertRayCast(Core.rayCast(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), Swig.set(vector3), Swig.set(vector32)));
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public List<SceneUtil.RayCastResult> rayCastFromCamera(Entity entity, Vector2 vector2) {
        return convertRayCast(Core.rayCastFromCamera(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), this.mEngine.getNativeEntity(entity), Swig.set(vector2)));
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public BoundingBox getBoundsUsingTransform(Entity entity, boolean z) {
        CoreMinAndMax transformComponentAabb = Core.getTransformComponentAabb(this.mEngine.getNativeEntity(entity), z, this.mEngine.getAgpContext().getGraphicsContext().getEcs());
        return new BoundingBox(Swig.get(transformComponentAabb.getMinAabb()), Swig.get(transformComponentAabb.getMaxAabb()));
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public BoundingBox getBoundsUsingWorldMatrix(Entity entity, boolean z) {
        CoreMinAndMax worldMatrixComponentAabb = Core.getWorldMatrixComponentAabb(this.mEngine.getNativeEntity(entity), z, this.mEngine.getAgpContext().getGraphicsContext().getEcs());
        BoundingBox boundingBox = new BoundingBox();
        boundingBox.setAabbMinMax(Swig.get(worldMatrixComponentAabb.getMinAabb()), Swig.get(worldMatrixComponentAabb.getMaxAabb()));
        return boundingBox;
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public Entity createPerspectiveCamera(Vector3 vector3, Quaternion quaternion, float f, float f2, float f3) {
        Entity createEntity = this.mScene.createEntity();
        createEntity.addComponent(LocalMatrixComponent.class);
        createEntity.addComponent(WorldMatrixComponent.class);
        NodeComponent nodeComponent = (NodeComponent) createEntity.addComponent(NodeComponent.class);
        nodeComponent.setName("Default Camera");
        createEntity.setComponent(nodeComponent);
        TransformComponent transformComponent = (TransformComponent) createEntity.addComponent(TransformComponent.class);
        transformComponent.setPosition(vector3);
        transformComponent.setRotation(quaternion);
        createEntity.setComponent(transformComponent);
        CameraComponent cameraComponent = (CameraComponent) createEntity.addComponent(CameraComponent.class);
        cameraComponent.setCameraType(CameraComponent.CameraType.PERSPECTIVE);
        cameraComponent.setPerspectiveVerticalFov((float) Math.toRadians((double) f3));
        cameraComponent.setZnear(f);
        cameraComponent.setZfar(f2);
        createEntity.setComponent(cameraComponent);
        return createEntity;
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public void updateCameraViewport(Entity entity, int i, int i2, boolean z, float f) {
        double radians = Math.toRadians((double) f);
        Optional component = entity.getComponent(CameraComponent.class);
        if (component.isPresent()) {
            float f2 = ((float) i) / ((float) i2);
            if (z) {
                ((CameraComponent) component.get()).setPerspectiveAspectRatio(f2);
                if (f2 > 1.0f) {
                    ((CameraComponent) component.get()).setPerspectiveVerticalFov((float) radians);
                } else {
                    ((CameraComponent) component.get()).setPerspectiveVerticalFov((float) (Math.atan(Math.tan(radians * 0.5d) / ((double) f2)) * 2.0d));
                }
            }
            ((CameraComponent) component.get()).setRenderResolution(i, i2);
            entity.setComponent((CameraComponent) component.get());
        }
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public void updateCameraViewport(Entity entity, int i, int i2) {
        Optional component = entity.getComponent(CameraComponent.class);
        if (component.isPresent()) {
            ((CameraComponent) component.get()).setPerspectiveAspectRatio(((float) i) / ((float) i2));
            ((CameraComponent) component.get()).setRenderResolution(i, i2);
            entity.setComponent((CameraComponent) component.get());
        }
    }

    private void setLightShadowEnabled(Entity entity, LightComponent lightComponent, boolean z) {
        lightComponent.setShadowEnabled(z);
        if (z) {
            CameraComponent cameraComponent = (CameraComponent) entity.addComponent(CameraComponent.class);
            if (lightComponent.getLightType() == LightComponent.LightType.DIRECTIONAL) {
                cameraComponent.setCameraType(CameraComponent.CameraType.ORTHOGRAPHIC);
            } else if (lightComponent.getLightType() == LightComponent.LightType.SPOT) {
                cameraComponent.setCameraType(CameraComponent.CameraType.PERSPECTIVE);
            } else {
                throw new UnsupportedOperationException();
            }
            cameraComponent.setCameraType(CameraComponent.CameraType.ORTHOGRAPHIC);
            cameraComponent.setAdditionalFlags(1);
            cameraComponent.setRenderResolution(1024, 1024);
            entity.setComponent(cameraComponent);
        }
    }

    private Entity createLight(LightComponent.LightType lightType, boolean z, Vector3 vector3, float f) {
        Entity createEntity = this.mScene.createEntity();
        createEntity.addComponent(LocalMatrixComponent.class);
        createEntity.addComponent(WorldMatrixComponent.class);
        NodeComponent nodeComponent = (NodeComponent) createEntity.addComponent(NodeComponent.class);
        nodeComponent.setName("Default Light");
        createEntity.setComponent(nodeComponent);
        LightComponent lightComponent = (LightComponent) createEntity.addComponent(LightComponent.class);
        lightComponent.setLightType(lightType);
        lightComponent.setColor(vector3);
        lightComponent.setIntensity(f);
        setLightShadowEnabled(createEntity, lightComponent, z);
        createEntity.setComponent(lightComponent);
        return createEntity;
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public Entity createLightDirectional(Quaternion quaternion, boolean z, Vector3 vector3, float f) {
        Entity createLight = createLight(LightComponent.LightType.DIRECTIONAL, z, vector3, f);
        TransformComponent transformComponent = (TransformComponent) createLight.addComponent(TransformComponent.class);
        transformComponent.setRotation(quaternion);
        createLight.setComponent(transformComponent);
        return createLight;
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public Entity createLightSpot(Vector3 vector3, Quaternion quaternion, boolean z, Vector3 vector32, float f) {
        Entity createLight = createLight(LightComponent.LightType.SPOT, z, vector32, f);
        TransformComponent transformComponent = (TransformComponent) createLight.addComponent(TransformComponent.class);
        transformComponent.setPosition(vector3);
        transformComponent.setRotation(quaternion);
        createLight.setComponent(transformComponent);
        return createLight;
    }

    @Override // ohos.agp.render.render3d.util.SceneUtil
    public Entity createLightPoint(Vector3 vector3, boolean z, Vector3 vector32, float f) {
        Entity createLight = createLight(LightComponent.LightType.POINT, z, vector32, f);
        TransformComponent transformComponent = (TransformComponent) createLight.addComponent(TransformComponent.class);
        transformComponent.setPosition(vector3);
        createLight.setComponent(transformComponent);
        return createLight;
    }
}
