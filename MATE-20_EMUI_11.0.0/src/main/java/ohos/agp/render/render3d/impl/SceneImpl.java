package ohos.agp.render.render3d.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.Engine;
import ohos.agp.render.render3d.Entity;
import ohos.agp.render.render3d.Scene;
import ohos.agp.render.render3d.SceneNode;
import ohos.agp.render.render3d.components.CameraComponent;
import ohos.agp.render.render3d.components.LightComponent;
import ohos.agp.render.render3d.components.LocalMatrixComponent;
import ohos.agp.render.render3d.components.MorphComponent;
import ohos.agp.render.render3d.components.NodeComponent;
import ohos.agp.render.render3d.components.RenderMeshComponent;
import ohos.agp.render.render3d.components.SceneComponent;
import ohos.agp.render.render3d.components.TransformComponent;
import ohos.agp.render.render3d.components.WorldMatrixComponent;
import ohos.agp.render.render3d.math.Matrix4x4;
import ohos.agp.render.render3d.math.Vector3;
import ohos.agp.render.render3d.systems.MorphingSystem;
import ohos.agp.render.render3d.util.SceneUtil;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public final class SceneImpl implements Scene {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "core: SceneImpl");
    private static final int MANAGER_MAP_INITIAL_SIZE = 10;
    private Map<String, ComponentManager<? extends Component, ? extends CoreComponentManager>> mComponentManagerMap = new HashMap(10);
    private final EngineImpl mEngine;
    private final Optional<MorphingSystemImpl> mMorphingSystem;
    private final CoreEcs mNativeEcs;
    private final CoreEntityManager mNativeEntityManager;
    private CoreNodeSystem mNativeNodeSystem;
    private final SceneUtilImpl mSceneUtil;

    SceneImpl(EngineImpl engineImpl) {
        this.mEngine = engineImpl;
        this.mSceneUtil = new SceneUtilImpl(this.mEngine, this);
        this.mNativeEcs = engineImpl.getAgpContext().getGraphicsContext().getEcs();
        this.mNativeEntityManager = this.mNativeEcs.getEntityManager();
        this.mMorphingSystem = MorphingSystemImpl.getMorpingSystem(engineImpl);
        CoreSystem system = this.mNativeEcs.getSystem("NodeSystem");
        if (system instanceof CoreNodeSystem) {
            this.mNativeNodeSystem = (CoreNodeSystem) system;
        }
        initManagers();
    }

    static Optional<CoreSceneNode> getNativeSceneNode(SceneNode sceneNode) {
        if (sceneNode == null) {
            return Optional.empty();
        }
        if (sceneNode instanceof SceneNodeImpl) {
            return Optional.of(((SceneNodeImpl) sceneNode).getNativeSceneNode());
        }
        throw new IllegalArgumentException();
    }

    /* access modifiers changed from: package-private */
    public static abstract class ComponentManager<ComponentType extends Component, NativeManagerType extends CoreComponentManager> {
        final Class<ComponentType> mComponentType;
        final NativeManagerType mManager;
        final CoreComponentManager mManagerGeneric;

        /* access modifiers changed from: package-private */
        public abstract ComponentType doGet(CoreEntity coreEntity);

        /* access modifiers changed from: package-private */
        public abstract void doSet(CoreEntity coreEntity, ComponentType componenttype);

        ComponentManager(CoreComponentManager coreComponentManager, Class<NativeManagerType> cls, Class<ComponentType> cls2) {
            this.mManagerGeneric = coreComponentManager;
            this.mComponentType = cls2;
            if (coreComponentManager == null) {
                throw new NullPointerException("ComponentManager must not be null.");
            } else if (cls != null) {
                Class<?>[] clsArr = {Long.TYPE, Boolean.TYPE};
                try {
                    long cptr = CoreComponentManager.getCptr(coreComponentManager);
                    if (cptr != 0) {
                        Constructor<NativeManagerType> declaredConstructor = cls.getDeclaredConstructor(clsArr);
                        declaredConstructor.setAccessible(true);
                        this.mManager = declaredConstructor.newInstance(Long.valueOf(cptr), false);
                        return;
                    }
                    throw new RuntimeException("ComponentManager not found: " + coreComponentManager.getClass().getSimpleName());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e2) {
                    throw new RuntimeException("CoreComponentManager constructor not found: " + cls.getSimpleName(), e2);
                }
            } else {
                throw new NullPointerException("NativeManagerType must not be null.");
            }
        }

        /* access modifiers changed from: package-private */
        public void create(CoreEntity coreEntity) {
            this.mManager.create(coreEntity);
        }

        /* access modifiers changed from: package-private */
        public final Optional<ComponentType> get(CoreEntity coreEntity) {
            if (coreEntity == null) {
                throw new NullPointerException("Entity must not be null.");
            } else if (this.mManagerGeneric.hasComponent(coreEntity)) {
                return Optional.of(doGet(coreEntity));
            } else {
                return Optional.empty();
            }
        }

        /* access modifiers changed from: package-private */
        public final void set(CoreEntity coreEntity, Component component) {
            if (coreEntity != null) {
                if (!this.mManagerGeneric.hasComponent(coreEntity)) {
                    this.mManager.create(coreEntity);
                }
                if (this.mComponentType.isInstance(component)) {
                    doSet(coreEntity, this.mComponentType.cast(component));
                    return;
                }
                throw new IllegalArgumentException();
            }
            throw new NullPointerException("Entity must not be null.");
        }
    }

    @Override // ohos.agp.render.render3d.Scene
    public Engine getEngine() {
        return this.mEngine;
    }

    @Override // ohos.agp.render.render3d.Scene
    public SceneUtil getSceneUtil() {
        return this.mSceneUtil;
    }

    @Override // ohos.agp.render.render3d.Scene
    public Entity createEntity() {
        return EntityImpl.getEntity(this, this.mNativeEntityManager.create());
    }

    @Override // ohos.agp.render.render3d.Scene
    public void destroyEntity(Entity entity) {
        this.mNativeEntityManager.destroy(EntityImpl.getNativeEntity(entity));
    }

    private CoreNodeSystem getNativeNodeSystem() {
        CoreNodeSystem coreNodeSystem = this.mNativeNodeSystem;
        if (coreNodeSystem != null) {
            return coreNodeSystem;
        }
        throw new IllegalStateException("NodeSystem not found.");
    }

    @Override // ohos.agp.render.render3d.Scene
    public SceneNode getRootNode() {
        return new SceneNodeImpl(this, getNativeNodeSystem().getRootNode());
    }

    /* access modifiers changed from: package-private */
    public SceneNode getNodeNotNull(CoreSceneNode coreSceneNode) {
        if (coreSceneNode != null) {
            return new SceneNodeImpl(this, coreSceneNode);
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public Optional<SceneNode> getNode(CoreSceneNode coreSceneNode) {
        if (coreSceneNode == null) {
            return Optional.empty();
        }
        return Optional.of(new SceneNodeImpl(this, coreSceneNode));
    }

    @Override // ohos.agp.render.render3d.Scene
    public Optional<SceneNode> getNode(Entity entity) {
        CoreSceneNode node = getNativeNodeSystem().getNode(EntityImpl.getNativeEntity(entity));
        if (node == null) {
            return Optional.empty();
        }
        return Optional.of(new SceneNodeImpl(this, node));
    }

    @Override // ohos.agp.render.render3d.Scene
    public SceneNode createNode() {
        return new SceneNodeImpl(this, getNativeNodeSystem().createNode());
    }

    @Override // ohos.agp.render.render3d.Scene
    public SceneNode cloneNode(SceneNode sceneNode, boolean z) {
        Optional<CoreSceneNode> nativeSceneNode = getNativeSceneNode(sceneNode);
        if (nativeSceneNode.isPresent()) {
            return new SceneNodeImpl(this, getNativeNodeSystem().cloneNode(nativeSceneNode.get(), z));
        }
        throw new IllegalArgumentException();
    }

    @Override // ohos.agp.render.render3d.Scene
    public void destroyNode(SceneNode sceneNode) {
        Optional<CoreSceneNode> nativeSceneNode = getNativeSceneNode(sceneNode);
        if (nativeSceneNode.isPresent()) {
            this.mNativeNodeSystem.destroyNode(nativeSceneNode.get());
        }
    }

    @Override // ohos.agp.render.render3d.Scene
    public MorphingSystem getMorphingSystem() {
        if (this.mMorphingSystem.isPresent()) {
            return this.mMorphingSystem.get();
        }
        throw new IllegalStateException();
    }

    /* access modifiers changed from: package-private */
    public ComponentManager<?, ? extends CoreComponentManager> getComponentManager(Class<?> cls) {
        ComponentManager<? extends Component, ? extends CoreComponentManager> componentManager = this.mComponentManagerMap.get(cls.getSimpleName());
        if (componentManager != null) {
            return componentManager;
        }
        HiLog.warn(LABEL, "class: %{public}s = %{public}s", new Object[]{cls.getSimpleName(), cls.getName()});
        HiLog.warn(LABEL, "componentManagers: %{public}s.", new Object[]{String.join(",", this.mComponentManagerMap.keySet())});
        throw new IllegalStateException();
    }

    private void initManagers() {
        initCameraComponentManager();
        initLightComponentManager();
        initLocalMatrixComponentManager();
        initMorphComponentManager();
        initNodeComponentManager();
        initRenderMeshComponentManager();
        initSceneComponentManager();
        initTransformComponentManager();
        initWorldMatrixComponentManager();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void nativeCameraToJava(CoreCameraComponent coreCameraComponent, CameraComponent cameraComponent) {
        cameraComponent.setName(coreCameraComponent.getName());
        cameraComponent.setCameraType(Swig.getCameraType(coreCameraComponent.getType()));
        cameraComponent.setAdditionalFlags(coreCameraComponent.getAdditionalFlags());
        cameraComponent.setPerspectiveAspectRatio(coreCameraComponent.getAspect());
        cameraComponent.setPerspectiveVerticalFov(coreCameraComponent.getVerticalFov());
        cameraComponent.setOrthoWidth(coreCameraComponent.getOrthoWidth());
        cameraComponent.setOrthoHeight(coreCameraComponent.getOrthoHeight());
        CoreMat4X4 projection = coreCameraComponent.getProjection();
        if (projection != null) {
            cameraComponent.getCustomProjection().set(projection.getData(), 0);
        }
        cameraComponent.setZnear(coreCameraComponent.getZnear());
        cameraComponent.setZfar(coreCameraComponent.getZfar());
        cameraComponent.setViewportParams(coreCameraComponent.getViewport());
        long[] renderResolution = coreCameraComponent.getRenderResolution();
        cameraComponent.setRenderResolution((int) renderResolution[0], (int) renderResolution[1]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void javaCameraToNative(CameraComponent cameraComponent, CoreCameraComponent coreCameraComponent) {
        coreCameraComponent.setName(cameraComponent.getName());
        coreCameraComponent.setType(Swig.getNativeCameraType(cameraComponent.getCameraType()));
        coreCameraComponent.setAdditionalFlags((short) cameraComponent.getAdditionalFlags());
        coreCameraComponent.setAspect(cameraComponent.getPerspectiveAspectRatio());
        coreCameraComponent.setVerticalFov(cameraComponent.getPerspectiveVerticalFov());
        coreCameraComponent.setOrthoWidth(cameraComponent.getOrthoWidth());
        coreCameraComponent.setOrthoHeight(cameraComponent.getOrthoHeight());
        CoreMat4X4 coreMat4X4 = new CoreMat4X4();
        coreMat4X4.setData(cameraComponent.getCustomProjection().getData());
        coreCameraComponent.setProjection(coreMat4X4);
        coreCameraComponent.setZnear(cameraComponent.getZnear());
        coreCameraComponent.setZfar(cameraComponent.getZfar());
        coreCameraComponent.setViewport(cameraComponent.getViewportParams());
        coreCameraComponent.setRenderResolution(new long[]{(long) cameraComponent.getRenderResolutionX(), (long) cameraComponent.getRenderResolutionY()});
    }

    private void initCameraComponentManager() {
        CoreComponentManager componentManager = this.mNativeEcs.getComponentManager("CameraComponent");
        if (componentManager != null) {
            this.mComponentManagerMap.put(CameraComponent.class.getSimpleName(), new ComponentManager<CameraComponent, CoreCameraComponentManager>(componentManager, CoreCameraComponentManager.class, CameraComponent.class) {
                /* class ohos.agp.render.render3d.impl.SceneImpl.AnonymousClass1 */

                /* access modifiers changed from: package-private */
                @Override // ohos.agp.render.render3d.impl.SceneImpl.ComponentManager
                public CameraComponent doGet(CoreEntity coreEntity) {
                    CoreCameraComponent coreCameraComponent = ((CoreCameraComponentManager) this.mManager).get(coreEntity);
                    CameraComponent cameraComponent = new CameraComponent();
                    SceneImpl.this.nativeCameraToJava(coreCameraComponent, cameraComponent);
                    return cameraComponent;
                }

                /* access modifiers changed from: package-private */
                public void doSet(CoreEntity coreEntity, CameraComponent cameraComponent) {
                    CoreCameraComponent coreCameraComponent = ((CoreCameraComponentManager) this.mManager).get(coreEntity);
                    SceneImpl.this.javaCameraToNative(cameraComponent, coreCameraComponent);
                    ((CoreCameraComponentManager) this.mManager).set(coreEntity, coreCameraComponent);
                }
            });
        }
    }

    private void initLightComponentManager() {
        CoreComponentManager componentManager = this.mNativeEcs.getComponentManager("LightComponent");
        if (componentManager != null) {
            this.mComponentManagerMap.put(LightComponent.class.getSimpleName(), new ComponentManager<LightComponent, CoreLightComponentManager>(componentManager, CoreLightComponentManager.class, LightComponent.class) {
                /* class ohos.agp.render.render3d.impl.SceneImpl.AnonymousClass2 */

                /* access modifiers changed from: package-private */
                @Override // ohos.agp.render.render3d.impl.SceneImpl.ComponentManager
                public LightComponent doGet(CoreEntity coreEntity) {
                    CoreLightComponent coreLightComponent = ((CoreLightComponentManager) this.mManager).get(coreEntity);
                    LightComponent lightComponent = new LightComponent();
                    lightComponent.setLightType(Swig.getLightType(coreLightComponent.getType()));
                    lightComponent.setColor(Swig.get(coreLightComponent.getColor()));
                    lightComponent.setIntensity(coreLightComponent.getIntensity());
                    lightComponent.setRange(coreLightComponent.getRange());
                    lightComponent.setSpotInnerAngle(coreLightComponent.getSpotInnerAngle());
                    lightComponent.setSpotOuterAngle(coreLightComponent.getSpotOuterAngle());
                    lightComponent.setShadowEnabled(coreLightComponent.getShadowEnabled());
                    return lightComponent;
                }

                /* access modifiers changed from: package-private */
                public void doSet(CoreEntity coreEntity, LightComponent lightComponent) {
                    CoreLightComponent coreLightComponent = ((CoreLightComponentManager) this.mManager).get(coreEntity);
                    coreLightComponent.setType(Swig.getNativeLightType(lightComponent.getLightType()));
                    coreLightComponent.setColor(Swig.set(lightComponent.getColor()));
                    coreLightComponent.setIntensity(lightComponent.getIntensity());
                    coreLightComponent.setRange(lightComponent.getRange());
                    coreLightComponent.setSpotInnerAngle(lightComponent.getSpotInnerAngle());
                    coreLightComponent.setSpotOuterAngle(lightComponent.getSpotOuterAngle());
                    coreLightComponent.setShadowEnabled(lightComponent.isShadowEnabled());
                    ((CoreLightComponentManager) this.mManager).set(coreEntity, coreLightComponent);
                }
            });
        }
    }

    private void initLocalMatrixComponentManager() {
        CoreComponentManager componentManager = this.mNativeEcs.getComponentManager("LocalMatrixComponent");
        if (componentManager != null) {
            this.mComponentManagerMap.put(LocalMatrixComponent.class.getSimpleName(), new ComponentManager<LocalMatrixComponent, CoreLocalMatrixComponentManager>(componentManager, CoreLocalMatrixComponentManager.class, LocalMatrixComponent.class) {
                /* class ohos.agp.render.render3d.impl.SceneImpl.AnonymousClass3 */

                /* access modifiers changed from: package-private */
                @Override // ohos.agp.render.render3d.impl.SceneImpl.ComponentManager
                public LocalMatrixComponent doGet(CoreEntity coreEntity) {
                    CoreLocalMatrixComponent coreLocalMatrixComponent = ((CoreLocalMatrixComponentManager) this.mManager).get(coreEntity);
                    LocalMatrixComponent localMatrixComponent = new LocalMatrixComponent();
                    Matrix4x4 matrix4x4 = new Matrix4x4();
                    CoreMat4X4 matrix = coreLocalMatrixComponent.getMatrix();
                    if (matrix != null) {
                        matrix4x4.set(matrix.getData(), 0);
                    }
                    localMatrixComponent.setLocalMatrix(matrix4x4);
                    return localMatrixComponent;
                }

                /* access modifiers changed from: package-private */
                public void doSet(CoreEntity coreEntity, LocalMatrixComponent localMatrixComponent) {
                    CoreLocalMatrixComponent coreLocalMatrixComponent = ((CoreLocalMatrixComponentManager) this.mManager).get(coreEntity);
                    CoreMat4X4 coreMat4X4 = new CoreMat4X4();
                    coreMat4X4.setData(localMatrixComponent.getLocalMatrix().getData());
                    coreLocalMatrixComponent.setMatrix(coreMat4X4);
                    ((CoreLocalMatrixComponentManager) this.mManager).set(coreEntity, coreLocalMatrixComponent);
                }
            });
        }
    }

    private void initMorphComponentManager() {
        CoreComponentManager componentManager = this.mNativeEcs.getComponentManager("MorphComponent");
        if (componentManager != null) {
            this.mComponentManagerMap.put(MorphComponent.class.getSimpleName(), new ComponentManager<MorphComponent, CoreMorphComponentManager>(componentManager, CoreMorphComponentManager.class, MorphComponent.class) {
                /* class ohos.agp.render.render3d.impl.SceneImpl.AnonymousClass4 */

                /* access modifiers changed from: package-private */
                @Override // ohos.agp.render.render3d.impl.SceneImpl.ComponentManager
                public MorphComponent doGet(CoreEntity coreEntity) {
                    CoreMorphComponent coreMorphComponent = ((CoreMorphComponentManager) this.mManager).get(coreEntity);
                    MorphComponent morphComponent = new MorphComponent();
                    morphComponent.setMorphTargets(coreMorphComponent.getMorphTargets());
                    return morphComponent;
                }

                /* access modifiers changed from: package-private */
                public void doSet(CoreEntity coreEntity, MorphComponent morphComponent) {
                    CoreMorphComponent coreMorphComponent = ((CoreMorphComponentManager) this.mManager).get(coreEntity);
                    coreMorphComponent.setMorphTargets(morphComponent.getMorphTargets());
                    ((CoreMorphComponentManager) this.mManager).set(coreEntity, coreMorphComponent);
                }
            });
        }
    }

    private void initNodeComponentManager() {
        CoreComponentManager componentManager = this.mNativeEcs.getComponentManager("NodeComponent");
        if (componentManager != null) {
            this.mComponentManagerMap.put(NodeComponent.class.getSimpleName(), new ComponentManager<NodeComponent, CoreNodeComponentManager>(componentManager, CoreNodeComponentManager.class, NodeComponent.class) {
                /* class ohos.agp.render.render3d.impl.SceneImpl.AnonymousClass5 */

                /* access modifiers changed from: package-private */
                @Override // ohos.agp.render.render3d.impl.SceneImpl.ComponentManager
                public NodeComponent doGet(CoreEntity coreEntity) {
                    CoreNodeComponent coreNodeComponent = ((CoreNodeComponentManager) this.mManager).get(coreEntity);
                    NodeComponent nodeComponent = new NodeComponent();
                    nodeComponent.setName(coreNodeComponent.getName());
                    nodeComponent.setParent(EntityImpl.getEntity(SceneImpl.this, coreNodeComponent.getParent()));
                    nodeComponent.setEnabled(coreNodeComponent.getEnabled());
                    nodeComponent.setExported(coreNodeComponent.getExported());
                    return nodeComponent;
                }

                /* access modifiers changed from: package-private */
                public void doSet(CoreEntity coreEntity, NodeComponent nodeComponent) {
                    CoreNodeComponent coreNodeComponent = ((CoreNodeComponentManager) this.mManager).get(coreEntity);
                    coreNodeComponent.setName(nodeComponent.getName());
                    coreNodeComponent.setParent(EntityImpl.getNativeEntity(nodeComponent.getParent()));
                    coreNodeComponent.setEnabled(nodeComponent.isEnabled());
                    coreNodeComponent.setExported(nodeComponent.isExported());
                    ((CoreNodeComponentManager) this.mManager).set(coreEntity, coreNodeComponent);
                }
            });
        }
    }

    private void initRenderMeshComponentManager() {
        CoreComponentManager componentManager = this.mNativeEcs.getComponentManager("RenderMeshComponent");
        if (componentManager != null) {
            this.mComponentManagerMap.put(RenderMeshComponent.class.getSimpleName(), new ComponentManager<RenderMeshComponent, CoreRenderMeshComponentManager>(componentManager, CoreRenderMeshComponentManager.class, RenderMeshComponent.class) {
                /* class ohos.agp.render.render3d.impl.SceneImpl.AnonymousClass6 */

                /* access modifiers changed from: package-private */
                @Override // ohos.agp.render.render3d.impl.SceneImpl.ComponentManager
                public RenderMeshComponent doGet(CoreEntity coreEntity) {
                    CoreRenderMeshComponent coreRenderMeshComponent = ((CoreRenderMeshComponentManager) this.mManager).get(coreEntity);
                    RenderMeshComponent renderMeshComponent = new RenderMeshComponent();
                    renderMeshComponent.setMesh(new ResourceHandleImpl(SceneImpl.this.mEngine, coreRenderMeshComponent.getMesh()));
                    renderMeshComponent.setMaterial(new ResourceHandleImpl(SceneImpl.this.mEngine, coreRenderMeshComponent.getMaterial()));
                    renderMeshComponent.setCastShadowsEnabled(coreRenderMeshComponent.getCastShadows());
                    return renderMeshComponent;
                }

                /* access modifiers changed from: package-private */
                public void doSet(CoreEntity coreEntity, RenderMeshComponent renderMeshComponent) {
                    CoreRenderMeshComponent coreRenderMeshComponent = ((CoreRenderMeshComponentManager) this.mManager).get(coreEntity);
                    coreRenderMeshComponent.setMesh(ResourceHandleImpl.getNativeHandle(renderMeshComponent.getMesh()));
                    coreRenderMeshComponent.setMaterial(ResourceHandleImpl.getNativeHandle(renderMeshComponent.getMaterial()));
                    coreRenderMeshComponent.setCastShadows(renderMeshComponent.isCastShadowsEnabled());
                    ((CoreRenderMeshComponentManager) this.mManager).set(coreEntity, coreRenderMeshComponent);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public Vector3[] readVector3Values(CoreVec3ArrayView coreVec3ArrayView) {
        int size = (int) coreVec3ArrayView.size();
        Vector3[] vector3Arr = new Vector3[size];
        for (int i = 0; i < size; i++) {
            vector3Arr[i] = Swig.get(coreVec3ArrayView.get((long) i));
        }
        return vector3Arr;
    }

    private void initSceneComponentManager() {
        CoreComponentManager componentManager = this.mNativeEcs.getComponentManager("SceneComponent");
        if (componentManager != null) {
            this.mComponentManagerMap.put(SceneComponent.class.getSimpleName(), new ComponentManager<SceneComponent, CoreSceneComponentManager>(componentManager, CoreSceneComponentManager.class, SceneComponent.class) {
                /* class ohos.agp.render.render3d.impl.SceneImpl.AnonymousClass7 */

                /* access modifiers changed from: package-private */
                @Override // ohos.agp.render.render3d.impl.SceneImpl.ComponentManager
                public SceneComponent doGet(CoreEntity coreEntity) {
                    CoreSceneComponent coreSceneComponent = ((CoreSceneComponentManager) this.mManager).get(coreEntity);
                    SceneComponent sceneComponent = new SceneComponent();
                    sceneComponent.setEnvironmentDiffuseColor(Swig.get(coreSceneComponent.getEnvironmentDiffuseColor()));
                    sceneComponent.setEnvironmentSpecularColor(Swig.get(coreSceneComponent.getEnvironmentSpecularColor()));
                    sceneComponent.setEnvironmentDiffuseIntensity(coreSceneComponent.getEnvironmentDiffuseIntensity());
                    sceneComponent.setEnvironmentSpecularIntensity(coreSceneComponent.getEnvironmentSpecularIntensity());
                    sceneComponent.setCamera(EntityImpl.getEntity(SceneImpl.this, coreSceneComponent.getCamera()));
                    sceneComponent.setRadianceCubemap(new GpuResourceHandleImpl(SceneImpl.this.mEngine, coreSceneComponent.getRadianceCubemap()));
                    sceneComponent.setEnvMap(new GpuResourceHandleImpl(SceneImpl.this.mEngine, coreSceneComponent.getEnvMap()));
                    sceneComponent.setBackgroundType(Swig.getEnvBgType(coreSceneComponent.getBackgroundType()));
                    sceneComponent.setIrradianceCoefficients(SceneImpl.this.readVector3Values(Core.getSceneIrradianceCoefficients(coreSceneComponent)));
                    sceneComponent.setEnvironmentRotation(Swig.get(coreSceneComponent.getEnvironmentRotation()));
                    return sceneComponent;
                }

                /* access modifiers changed from: package-private */
                public void doSet(CoreEntity coreEntity, SceneComponent sceneComponent) {
                    CoreSceneComponent coreSceneComponent = ((CoreSceneComponentManager) this.mManager).get(coreEntity);
                    coreSceneComponent.setEnvironmentDiffuseColor(Swig.set(sceneComponent.getEnvironmentDiffuseColor()));
                    coreSceneComponent.setEnvironmentSpecularColor(Swig.set(sceneComponent.getEnvironmentSpecularColor()));
                    coreSceneComponent.setEnvironmentDiffuseIntensity(sceneComponent.getEnvironmentDiffuseIntensity());
                    coreSceneComponent.setEnvironmentSpecularIntensity(sceneComponent.getEnvironmentSpecularIntensity());
                    coreSceneComponent.setCamera(EntityImpl.getNativeEntity(sceneComponent.getCamera()));
                    coreSceneComponent.setRadianceCubemap(GpuResourceHandleImpl.getNativeHandle(sceneComponent.getRadianceCubemap()));
                    coreSceneComponent.setEnvMap(GpuResourceHandleImpl.getNativeHandle(sceneComponent.getEnvMap()));
                    coreSceneComponent.setBackgroundType(Swig.getNativeEnvBgType(sceneComponent.getBackgroundType()));
                    Vector3[] irradianceCoefficients = sceneComponent.getIrradianceCoefficients();
                    if (irradianceCoefficients != null) {
                        for (int i = 0; i < irradianceCoefficients.length; i++) {
                            Core.setSceneIrradianceCoefficient(coreSceneComponent, Swig.set(irradianceCoefficients[i]), i);
                        }
                    }
                    coreSceneComponent.setEnvironmentRotation(Swig.set(sceneComponent.getEnvironmentRotation()));
                    ((CoreSceneComponentManager) this.mManager).set(coreEntity, coreSceneComponent);
                }
            });
        }
    }

    private void initTransformComponentManager() {
        CoreComponentManager componentManager = this.mNativeEcs.getComponentManager("TransformComponent");
        if (componentManager != null) {
            this.mComponentManagerMap.put(TransformComponent.class.getSimpleName(), new ComponentManager<TransformComponent, CoreTransformComponentManager>(componentManager, CoreTransformComponentManager.class, TransformComponent.class) {
                /* class ohos.agp.render.render3d.impl.SceneImpl.AnonymousClass8 */

                /* access modifiers changed from: package-private */
                @Override // ohos.agp.render.render3d.impl.SceneImpl.ComponentManager
                public TransformComponent doGet(CoreEntity coreEntity) {
                    CoreTransformComponent coreTransformComponent = ((CoreTransformComponentManager) this.mManager).get(coreEntity);
                    TransformComponent transformComponent = new TransformComponent();
                    transformComponent.setPosition(Swig.get(coreTransformComponent.getPosition()));
                    transformComponent.setRotation(Swig.get(coreTransformComponent.getRotation()));
                    transformComponent.setScale(Swig.get(coreTransformComponent.getScale()));
                    return transformComponent;
                }

                /* access modifiers changed from: package-private */
                public void doSet(CoreEntity coreEntity, TransformComponent transformComponent) {
                    CoreTransformComponent coreTransformComponent = ((CoreTransformComponentManager) this.mManager).get(coreEntity);
                    coreTransformComponent.setPosition(Swig.set(transformComponent.getPosition()));
                    coreTransformComponent.setRotation(Swig.set(transformComponent.getRotation()));
                    coreTransformComponent.setScale(Swig.set(transformComponent.getScale()));
                    ((CoreTransformComponentManager) this.mManager).set(coreEntity, coreTransformComponent);
                }
            });
        }
    }

    private void initWorldMatrixComponentManager() {
        CoreComponentManager componentManager = this.mNativeEcs.getComponentManager("WorldMatrixComponent");
        if (componentManager != null) {
            this.mComponentManagerMap.put(WorldMatrixComponent.class.getSimpleName(), new ComponentManager<WorldMatrixComponent, CoreWorldMatrixComponentManager>(componentManager, CoreWorldMatrixComponentManager.class, WorldMatrixComponent.class) {
                /* class ohos.agp.render.render3d.impl.SceneImpl.AnonymousClass9 */

                /* access modifiers changed from: package-private */
                @Override // ohos.agp.render.render3d.impl.SceneImpl.ComponentManager
                public WorldMatrixComponent doGet(CoreEntity coreEntity) {
                    CoreWorldMatrixComponent coreWorldMatrixComponent = ((CoreWorldMatrixComponentManager) this.mManager).get(coreEntity);
                    WorldMatrixComponent worldMatrixComponent = new WorldMatrixComponent();
                    worldMatrixComponent.setWorldMatrix(new Matrix4x4());
                    CoreMat4X4 matrix = coreWorldMatrixComponent.getMatrix();
                    if (matrix != null) {
                        worldMatrixComponent.getWorldMatrix().set(matrix.getData(), 0);
                    }
                    return worldMatrixComponent;
                }

                /* access modifiers changed from: package-private */
                public void doSet(CoreEntity coreEntity, WorldMatrixComponent worldMatrixComponent) {
                    CoreWorldMatrixComponent coreWorldMatrixComponent = ((CoreWorldMatrixComponentManager) this.mManager).get(coreEntity);
                    CoreMat4X4 coreMat4X4 = new CoreMat4X4();
                    coreMat4X4.setData(worldMatrixComponent.getWorldMatrix().getData());
                    coreWorldMatrixComponent.setMatrix(coreMat4X4);
                    ((CoreWorldMatrixComponentManager) this.mManager).set(coreEntity, coreWorldMatrixComponent);
                }
            });
        }
    }
}
