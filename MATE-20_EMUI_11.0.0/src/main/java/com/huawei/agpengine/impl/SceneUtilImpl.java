package com.huawei.agpengine.impl;

import com.huawei.agpengine.Entity;
import com.huawei.agpengine.SceneNode;
import com.huawei.agpengine.components.CameraComponent;
import com.huawei.agpengine.components.LightComponent;
import com.huawei.agpengine.components.LocalMatrixComponent;
import com.huawei.agpengine.components.NodeComponent;
import com.huawei.agpengine.components.TransformComponent;
import com.huawei.agpengine.components.WorldMatrixComponent;
import com.huawei.agpengine.math.Quaternion;
import com.huawei.agpengine.math.Vector2;
import com.huawei.agpengine.math.Vector3;
import com.huawei.agpengine.resources.ResourceHandle;
import com.huawei.agpengine.util.BoundingBox;
import com.huawei.agpengine.util.SceneUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/* access modifiers changed from: package-private */
public class SceneUtilImpl implements SceneUtil {
    private final EngineImpl mEngine;
    private final CoreMeshUtil mNativeMeshUtil;
    private final CorePicking mNativePicking;
    private final CoreSceneUtil mNativeSceneUtil;
    private final SceneImpl mScene;

    /* access modifiers changed from: private */
    public static class RayCastResultImpl implements SceneUtil.RayCastResult {
        private final float mDistance;
        private final SceneNode mNode;

        RayCastResultImpl(SceneNode node, float distance) {
            this.mNode = node;
            this.mDistance = distance;
        }

        @Override // com.huawei.agpengine.util.SceneUtil.RayCastResult
        public SceneNode getNode() {
            return this.mNode;
        }

        @Override // com.huawei.agpengine.util.SceneUtil.RayCastResult
        public float getDistance() {
            return this.mDistance;
        }
    }

    SceneUtilImpl(EngineImpl engine, SceneImpl scene) {
        if (engine == null || scene == null) {
            throw new NullPointerException("Arguments must not be null.");
        }
        this.mEngine = engine;
        this.mScene = scene;
        this.mNativeSceneUtil = this.mEngine.getAgpContext().getGraphicsContext().getSceneUtil();
        this.mNativeMeshUtil = this.mEngine.getAgpContext().getGraphicsContext().getMeshUtil();
        this.mNativePicking = CorePicking.getInterface(Core.getPluginRegister());
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public ResourceHandle generateCubeMesh(String name, ResourceHandle material, float width, float height, float depth) {
        this.mEngine.requireRenderThread();
        return new ResourceHandleImpl(this.mEngine, this.mNativeMeshUtil.generateCubeMesh(name, ResourceHandleImpl.getNativeHandle(material), width, height, depth));
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public ResourceHandle generatePlaneMesh(String name, ResourceHandle material, float width, float depth) {
        this.mEngine.requireRenderThread();
        return new ResourceHandleImpl(this.mEngine, this.mNativeMeshUtil.generatePlaneMesh(name, ResourceHandleImpl.getNativeHandle(material), width, depth));
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public ResourceHandle generateSphereMesh(String name, ResourceHandle material, float radius, int rings, int sectors) {
        this.mEngine.requireRenderThread();
        return new ResourceHandleImpl(this.mEngine, this.mNativeMeshUtil.generateSphereMesh(name, ResourceHandleImpl.getNativeHandle(material), radius, (long) rings, (long) sectors));
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public ResourceHandle generateConeMesh(String name, ResourceHandle material, float radius, float length, int sectors) {
        this.mEngine.requireRenderThread();
        return new ResourceHandleImpl(this.mEngine, this.mNativeMeshUtil.generateConeMesh(name, ResourceHandleImpl.getNativeHandle(material), radius, length, (long) sectors));
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public Entity generateCube(String name, ResourceHandle material, float width, float height, float depth) {
        this.mEngine.requireRenderThread();
        return new EntityImpl(this.mScene, this.mNativeMeshUtil.generateCube(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), name, ResourceHandleImpl.getNativeHandle(material), width, height, depth));
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public Entity generatePlane(String name, ResourceHandle material, float width, float depth) {
        this.mEngine.requireRenderThread();
        return new EntityImpl(this.mScene, this.mNativeMeshUtil.generatePlane(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), name, ResourceHandleImpl.getNativeHandle(material), width, depth));
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public Entity generateSphere(String name, ResourceHandle material, float radius, int rings, int sectors) {
        this.mEngine.requireRenderThread();
        return new EntityImpl(this.mScene, this.mNativeMeshUtil.generateSphere(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), name, ResourceHandleImpl.getNativeHandle(material), radius, (long) rings, (long) sectors));
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public Entity generateCone(String name, ResourceHandle material, float radius, float length, int sectors) {
        this.mEngine.requireRenderThread();
        return new EntityImpl(this.mScene, this.mNativeMeshUtil.generateCone(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), name, ResourceHandleImpl.getNativeHandle(material), radius, length, (long) sectors));
    }

    private void checkCamera(Entity camera) {
        if (camera == null) {
            throw new NullPointerException("camera must not be null.");
        }
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public Vector3 screenToWorld(Entity camera, Vector3 screenPoint) {
        checkCamera(camera);
        if (screenPoint != null) {
            return Swig.get(this.mNativePicking.screenToWorld(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), camera.getId(), Swig.set(screenPoint)));
        }
        throw new NullPointerException("screenPoint must not be null.");
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public Vector3 worldToScreen(Entity camera, Vector3 worldPoint) {
        checkCamera(camera);
        if (worldPoint != null) {
            return Swig.get(this.mNativePicking.worldToScreen(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), camera.getId(), Swig.set(worldPoint)));
        }
        throw new NullPointerException("worldPoint must not be null.");
    }

    private List<SceneUtil.RayCastResult> convertRayCast(CoreRayCastResultArray raycastResults) {
        List<SceneUtil.RayCastResult> results = new ArrayList<>(raycastResults.size());
        for (int i = 0; i < raycastResults.size(); i++) {
            CoreRayCastResult nativeResult = raycastResults.get(i);
            Optional<? extends SceneNode> node = this.mEngine.getNode(nativeResult.getNode());
            if (node.isPresent()) {
                results.add(new RayCastResultImpl((SceneNode) node.get(), nativeResult.getDistance()));
            } else {
                throw new IllegalStateException("Internal graphics engine error");
            }
        }
        return results;
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public List<SceneUtil.RayCastResult> rayCast(Vector3 origin, Vector3 direction) {
        return convertRayCast(this.mNativePicking.rayCast(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), Swig.set(origin), Swig.set(direction)));
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public List<SceneUtil.RayCastResult> rayCastFromCamera(Entity camera, Vector2 screenPos) {
        checkCamera(camera);
        return convertRayCast(this.mNativePicking.rayCastFromCamera(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), camera.getId(), Swig.set(screenPos)));
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public BoundingBox getBoundsUsingTransform(Entity entity, boolean isRecursive) {
        CoreMinAndMax mam = this.mNativePicking.getTransformComponentAabb(entity.getId(), isRecursive, this.mEngine.getAgpContext().getGraphicsContext().getEcs());
        return new BoundingBox(Swig.get(mam.getMinAabb()), Swig.get(mam.getMaxAabb()));
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public BoundingBox getBoundsUsingWorldMatrix(Entity entity, boolean isRecursive) {
        CoreMinAndMax mam = this.mNativePicking.getWorldMatrixComponentAabb(entity.getId(), isRecursive, this.mEngine.getAgpContext().getGraphicsContext().getEcs());
        BoundingBox bounds = new BoundingBox();
        bounds.setAabbMinMax(Swig.get(mam.getMinAabb()), Swig.get(mam.getMaxAabb()));
        return bounds;
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public Entity createPerspectiveCamera(Vector3 position, Quaternion rotation, float znear, float zfar, float fovDegrees) {
        Entity cameraEntity = this.mScene.createEntity();
        cameraEntity.addComponent(LocalMatrixComponent.class);
        cameraEntity.addComponent(WorldMatrixComponent.class);
        NodeComponent nodeComponent = (NodeComponent) cameraEntity.addComponent(NodeComponent.class);
        nodeComponent.setName("Default Camera");
        cameraEntity.setComponent(nodeComponent);
        TransformComponent transformComponent = (TransformComponent) cameraEntity.addComponent(TransformComponent.class);
        transformComponent.setPosition(position);
        transformComponent.setRotation(rotation);
        cameraEntity.setComponent(transformComponent);
        CameraComponent cameraComponent = (CameraComponent) cameraEntity.addComponent(CameraComponent.class);
        cameraComponent.setCameraType(CameraComponent.CameraType.PERSPECTIVE);
        cameraComponent.setPerspectiveVerticalFov((float) Math.toRadians((double) fovDegrees));
        cameraComponent.setZnear(znear);
        cameraComponent.setZfar(zfar);
        cameraEntity.setComponent(cameraComponent);
        return cameraEntity;
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public void updateCameraViewport(Entity camera, int renderResolutionX, int renderResolutionY, boolean isAutoAspect, float fovDegrees) {
        checkCamera(camera);
        double fovRadians = Math.toRadians((double) fovDegrees);
        Optional<CameraComponent> cameraComponent = camera.getComponent(CameraComponent.class);
        if (cameraComponent.isPresent()) {
            float aspectRatio = ((float) renderResolutionX) / ((float) renderResolutionY);
            if (isAutoAspect) {
                cameraComponent.get().setPerspectiveAspectRatio(aspectRatio);
                if (aspectRatio > 1.0f) {
                    cameraComponent.get().setPerspectiveVerticalFov((float) fovRadians);
                } else {
                    cameraComponent.get().setPerspectiveVerticalFov((float) (Math.atan(Math.tan(0.5d * fovRadians) / ((double) aspectRatio)) * 2.0d));
                }
            }
            cameraComponent.get().setRenderResolution(renderResolutionX, renderResolutionY);
            camera.setComponent(cameraComponent.get());
        }
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public void updateCameraViewport(Entity camera, int renderResolutionX, int renderResolutionY) {
        checkCamera(camera);
        Optional<CameraComponent> cameraComponent = camera.getComponent(CameraComponent.class);
        if (cameraComponent.isPresent()) {
            cameraComponent.get().setPerspectiveAspectRatio(((float) renderResolutionX) / ((float) renderResolutionY));
            cameraComponent.get().setRenderResolution(renderResolutionX, renderResolutionY);
            camera.setComponent(cameraComponent.get());
        }
    }

    private Entity createLight(CoreLightComponent lightComponent, Vector3 position, Quaternion rotation) {
        return new EntityImpl(this.mScene, this.mNativeSceneUtil.createLight(this.mEngine.getAgpContext().getGraphicsContext().getEcs(), lightComponent, Swig.set(position), Swig.set(rotation)));
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public Entity createLightDirectional(Quaternion rotation, boolean isShadowEnabled, Vector3 color, float intensity) {
        CoreLightComponent nativeLightComponent = new CoreLightComponent();
        nativeLightComponent.setType(Swig.getNativeLightType(LightComponent.LightType.DIRECTIONAL));
        nativeLightComponent.setColor(Swig.set(color));
        nativeLightComponent.setIntensity(intensity);
        nativeLightComponent.setShadowEnabled(isShadowEnabled);
        return createLight(nativeLightComponent, Vector3.ZERO, rotation);
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public Entity createLightSpot(Vector3 position, Quaternion rotation, boolean isShadowEnabled, Vector3 color, float intensity) {
        CoreLightComponent nativeLightComponent = new CoreLightComponent();
        nativeLightComponent.setType(Swig.getNativeLightType(LightComponent.LightType.SPOT));
        nativeLightComponent.setColor(Swig.set(color));
        nativeLightComponent.setIntensity(intensity);
        nativeLightComponent.setShadowEnabled(isShadowEnabled);
        return createLight(nativeLightComponent, position, rotation);
    }

    @Override // com.huawei.agpengine.util.SceneUtil
    public Entity createLightPoint(Vector3 position, boolean isShadowEnabled, Vector3 color, float intensity) {
        CoreLightComponent nativeLightComponent = new CoreLightComponent();
        nativeLightComponent.setType(Swig.getNativeLightType(LightComponent.LightType.POINT));
        nativeLightComponent.setColor(Swig.set(color));
        nativeLightComponent.setIntensity(intensity);
        nativeLightComponent.setShadowEnabled(isShadowEnabled);
        return createLight(nativeLightComponent, position, Quaternion.IDENTITY);
    }
}
