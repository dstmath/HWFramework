package com.huawei.agpengine.impl;

import android.util.Log;
import com.huawei.agpengine.Component;
import com.huawei.agpengine.Engine;
import com.huawei.agpengine.Entity;
import com.huawei.agpengine.Scene;
import com.huawei.agpengine.SceneNode;
import com.huawei.agpengine.components.CameraComponent;
import com.huawei.agpengine.components.LightComponent;
import com.huawei.agpengine.components.LocalMatrixComponent;
import com.huawei.agpengine.components.MorphComponent;
import com.huawei.agpengine.components.NodeComponent;
import com.huawei.agpengine.components.RenderMeshComponent;
import com.huawei.agpengine.components.SceneComponent;
import com.huawei.agpengine.components.TransformComponent;
import com.huawei.agpengine.components.WorldMatrixComponent;
import com.huawei.agpengine.math.Matrix4x4;
import com.huawei.agpengine.math.Vector3;
import com.huawei.agpengine.property.PropertyData;
import com.huawei.agpengine.systems.MorphingSystem;
import com.huawei.agpengine.util.SceneUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/* access modifiers changed from: package-private */
public final class SceneImpl implements Scene {
    private static final int MANAGER_MAP_INITIAL_SIZE = 10;
    private static final String TAG = "core: SceneImpl";
    private Map<String, ComponentManager<? extends Component, ? extends CoreComponentManager>> mComponentManagerMap = new HashMap((int) MANAGER_MAP_INITIAL_SIZE);
    private Map<String, ComponentManager<?, ? extends CoreComponentManager>> mComponentManagerMap2 = this.mComponentManagerMap;
    private final EngineImpl mEngine;
    private final Optional<MorphingSystemImpl> mMorphingSystem;
    private final CoreEcs mNativeEcs;
    private final CoreEntityManager mNativeEntityManager;
    private CoreNodeSystem mNativeNodeSystem;
    private final SceneUtilImpl mSceneUtil;

    SceneImpl(EngineImpl engine) {
        this.mEngine = engine;
        this.mSceneUtil = new SceneUtilImpl(this.mEngine, this);
        this.mNativeEcs = engine.getAgpContext().getGraphicsContext().getEcs();
        this.mNativeEntityManager = this.mNativeEcs.getEntityManager();
        this.mMorphingSystem = MorphingSystemImpl.getMorpingSystem(engine);
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
        throw new IllegalArgumentException("Invalid SceneNode.");
    }

    /* access modifiers changed from: package-private */
    public static abstract class ComponentManager<ComponentType extends Component, NativeManagerType extends CoreComponentManager> {
        final Class<ComponentType> mComponentType;
        final NativeManagerType mManager;
        final CoreComponentManager mManagerGeneric;

        /* access modifiers changed from: package-private */
        public abstract ComponentType doGet(int i);

        /* access modifiers changed from: package-private */
        public abstract void doSet(int i, ComponentType componenttype);

        ComponentManager(CoreComponentManager manager, Class<NativeManagerType> nativeManagerClass, Class<ComponentType> componentType) {
            this.mManagerGeneric = manager;
            this.mComponentType = componentType;
            if (manager == null) {
                throw new NullPointerException("ComponentManager must not be null.");
            } else if (nativeManagerClass != null) {
                Class<?>[] argTypes = {Long.TYPE, Boolean.TYPE};
                try {
                    long cptr = CoreComponentManager.getCptr(manager);
                    if (cptr != 0) {
                        this.mManager = nativeManagerClass.getDeclaredConstructor(argTypes).newInstance(Long.valueOf(cptr), false);
                        return;
                    }
                    throw new IllegalStateException("ComponentManager not found: " + manager.getClass().getSimpleName());
                } catch (IllegalAccessException iae) {
                    throw new IllegalStateException(iae);
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException exception) {
                    throw new IllegalStateException("CoreComponentManager constructor not found: " + nativeManagerClass.getSimpleName(), exception);
                }
            } else {
                throw new NullPointerException("NativeManagerType must not be null.");
            }
        }

        /* access modifiers changed from: package-private */
        public void create(int entity) {
            this.mManager.create(entity);
        }

        /* access modifiers changed from: package-private */
        public final Optional<ComponentType> get(int entity) {
            if (this.mManagerGeneric.hasComponent(entity)) {
                return Optional.of(doGet(entity));
            }
            return Optional.empty();
        }

        /* access modifiers changed from: package-private */
        public final void set(int entity, Component component) {
            if (!this.mManagerGeneric.hasComponent(entity)) {
                this.mManager.create(entity);
            }
            if (this.mComponentType.isInstance(component)) {
                doSet(entity, this.mComponentType.cast(component));
                return;
            }
            throw new IllegalArgumentException("Internal graphics engine error");
        }
    }

    @Override // com.huawei.agpengine.Scene
    public Engine getEngine() {
        return this.mEngine;
    }

    @Override // com.huawei.agpengine.Scene
    public SceneUtil getSceneUtil() {
        return this.mSceneUtil;
    }

    @Override // com.huawei.agpengine.Scene
    public Entity createEntity() {
        return new EntityImpl(this, this.mNativeEntityManager.create());
    }

    @Override // com.huawei.agpengine.Scene
    public void destroyEntity(Entity entity) {
        this.mNativeEntityManager.destroy(entity.getId());
    }

    private CoreNodeSystem getNativeNodeSystem() {
        CoreNodeSystem coreNodeSystem = this.mNativeNodeSystem;
        if (coreNodeSystem != null) {
            return coreNodeSystem;
        }
        throw new IllegalStateException("NodeSystem not found.");
    }

    /* access modifiers changed from: package-private */
    public boolean isAlive(Entity entity) {
        return this.mNativeEntityManager.alive(entity.getId());
    }

    @Override // com.huawei.agpengine.Scene
    public SceneNode getRootNode() {
        return new SceneNodeImpl(this, getNativeNodeSystem().getRootNode());
    }

    /* access modifiers changed from: package-private */
    public SceneNode getNodeNotNull(CoreSceneNode nativeSceneNode) {
        if (nativeSceneNode != null) {
            return new SceneNodeImpl(this, nativeSceneNode);
        }
        throw new IllegalStateException("Internal graphics engine error");
    }

    /* access modifiers changed from: package-private */
    public Optional<SceneNode> getNode(CoreSceneNode nativeSceneNode) {
        if (nativeSceneNode == null) {
            return Optional.empty();
        }
        return Optional.of(new SceneNodeImpl(this, nativeSceneNode));
    }

    @Override // com.huawei.agpengine.Scene
    public Optional<SceneNode> getNode(Entity entity) {
        CoreSceneNode node = getNativeNodeSystem().getNode(entity.getId());
        if (node == null) {
            return Optional.empty();
        }
        return Optional.of(new SceneNodeImpl(this, node));
    }

    @Override // com.huawei.agpengine.Scene
    public SceneNode createNode() {
        return new SceneNodeImpl(this, getNativeNodeSystem().createNode());
    }

    @Override // com.huawei.agpengine.Scene
    public SceneNode cloneNode(SceneNode node, boolean isRecursive) {
        Optional<CoreSceneNode> nativeNodeOpt = getNativeSceneNode(node);
        if (nativeNodeOpt.isPresent()) {
            return new SceneNodeImpl(this, getNativeNodeSystem().cloneNode(nativeNodeOpt.get(), isRecursive));
        }
        throw new IllegalArgumentException("Invalid node.");
    }

    @Override // com.huawei.agpengine.Scene
    public void destroyNode(SceneNode node) {
        Optional<CoreSceneNode> nativeNodeOpt = getNativeSceneNode(node);
        if (nativeNodeOpt.isPresent()) {
            this.mNativeNodeSystem.destroyNode(nativeNodeOpt.get());
        }
    }

    @Override // com.huawei.agpengine.Scene
    public MorphingSystem getMorphingSystem() {
        if (this.mMorphingSystem.isPresent()) {
            return this.mMorphingSystem.get();
        }
        throw new IllegalStateException("Morphing system not found.");
    }

    @Override // com.huawei.agpengine.Scene
    public Optional<PropertyData> getSystemPropertyData(String systemName) {
        CorePropertyHandle handle;
        CoreSystem system = this.mNativeEcs.getSystem(systemName);
        if (system == null || (handle = system.getProps()) == null) {
            return Optional.empty();
        }
        PropertyData props = new PropertyDataImpl(handle);
        PropertyDataImpl.readFromPropertyHandle(handle, props.getData());
        return Optional.of(props);
    }

    @Override // com.huawei.agpengine.Scene
    public boolean setSystemPropertyData(String systemName, PropertyData data) {
        CoreSystem system = this.mNativeEcs.getSystem(systemName);
        if (system == null) {
            return false;
        }
        CorePropertyHandle propertyHandle = system.getProps();
        if (propertyHandle == null) {
            throw new IllegalStateException("Internal graphics engine error");
        } else if (PropertyApiImpl.nativeEquals(data.getOwner(), propertyHandle.owner())) {
            PropertyDataImpl.writeToPropertyHandle(propertyHandle, data.getData());
            return true;
        } else {
            throw new IllegalArgumentException("PropertyApi mismatch.");
        }
    }

    /* access modifiers changed from: package-private */
    public CoreComponentManager getNativeComponentManager(String componentName) {
        return this.mNativeEcs.getComponentManager(componentName);
    }

    /* access modifiers changed from: package-private */
    public String[] getComponents(int nativeEntityId) {
        List<String> componentNames = new ArrayList<>();
        CoreComponentManagerArray managers = this.mNativeEcs.componentManagers();
        int managerCount = managers.size();
        for (int i = 0; i < managerCount; i++) {
            CoreComponentManager manager = managers.get(i);
            if (!(manager == null || ((int) manager.getComponentId(nativeEntityId)) == -1)) {
                componentNames.add(manager.name());
            }
        }
        return (String[]) componentNames.toArray(new String[0]);
    }

    /* access modifiers changed from: package-private */
    public ComponentManager<?, ? extends CoreComponentManager> getComponentManager(Class<?> componentType) {
        ComponentManager<? extends Component, ? extends CoreComponentManager> manager = this.mComponentManagerMap.get(componentType.getSimpleName());
        if (manager != null) {
            return manager;
        }
        Log.w(TAG, "class: " + componentType.getSimpleName() + " = " + componentType.getName());
        StringBuilder sb = new StringBuilder();
        sb.append("componenManagers: ");
        sb.append(this.mComponentManagerMap);
        Log.w(TAG, sb.toString());
        throw new IllegalArgumentException("Unknown Component type: " + componentType.getSimpleName());
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
    private <NativeComponentType> NativeComponentType checkNull(NativeComponentType component) {
        if (component != null) {
            return component;
        }
        throw new NullPointerException("component must not be null.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void nativeCameraToJava(CoreCameraComponent cc, CameraComponent component) {
        component.setName(cc.getName());
        component.setCameraType(Swig.getCameraType(cc.getType()));
        component.setAdditionalFlags(cc.getAdditionalFlags());
        component.setPerspectiveAspectRatio(cc.getAspect());
        component.setPerspectiveVerticalFov(cc.getVerticalFov());
        component.setOrthoWidth(cc.getOrthoWidth());
        component.setOrthoHeight(cc.getOrthoHeight());
        CoreMat4X4 projection = cc.getProjection();
        if (projection != null) {
            component.getCustomProjection().set(projection.getData(), 0);
        }
        component.setZnear(cc.getZnear());
        component.setZfar(cc.getZfar());
        component.setViewportParams(cc.getViewport());
        long[] renderRes = cc.getRenderResolution();
        component.setRenderResolution((int) renderRes[0], (int) renderRes[1]);
        component.setCameraTargetType(Swig.getCameraTargetType(cc.getTargetType()));
        component.setCustomColorTarget(new GpuResourceHandleImpl(this.mEngine, cc.getCustomColorTarget()));
        component.setCustomDepthTarget(new GpuResourceHandleImpl(this.mEngine, cc.getCustomDepthTarget()));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void javaCameraToNative(CameraComponent component, CoreCameraComponent cc) {
        cc.setName(component.getName());
        cc.setType(Swig.getNativeCameraType(component.getCameraType()));
        cc.setAdditionalFlags((short) component.getAdditionalFlags());
        cc.setAspect(component.getPerspectiveAspectRatio());
        cc.setVerticalFov(component.getPerspectiveVerticalFov());
        cc.setOrthoWidth(component.getOrthoWidth());
        cc.setOrthoHeight(component.getOrthoHeight());
        CoreMat4X4 projection = new CoreMat4X4();
        projection.setData(component.getCustomProjection().getData());
        cc.setProjection(projection);
        cc.setZnear(component.getZnear());
        cc.setZfar(component.getZfar());
        cc.setViewport(component.getViewportParams());
        cc.setRenderResolution(new long[]{(long) component.getRenderResolutionX(), (long) component.getRenderResolutionY()});
        cc.setTargetType(Swig.getNativeCameraTargetType(component.getCameraTargetType()));
        if (component.getCustomColorTarget() != null) {
            cc.setCustomColorTarget(component.getCustomColorTarget().getNativeHandle());
        }
        if (component.getCustomDepthTarget() != null) {
            cc.setCustomDepthTarget(component.getCustomDepthTarget().getNativeHandle());
        }
    }

    private void initCameraComponentManager() {
        CoreComponentManager icm = this.mNativeEcs.getComponentManager("CameraComponent");
        if (icm != null) {
            this.mComponentManagerMap.put(CameraComponent.class.getSimpleName(), new ComponentManager<CameraComponent, CoreCameraComponentManager>(icm, CoreCameraComponentManager.class, CameraComponent.class) {
                /* class com.huawei.agpengine.impl.SceneImpl.AnonymousClass1 */

                /* access modifiers changed from: package-private */
                @Override // com.huawei.agpengine.impl.SceneImpl.ComponentManager
                public CameraComponent doGet(int entity) {
                    CameraComponent component = new CameraComponent();
                    SceneImpl.this.nativeCameraToJava((CoreCameraComponent) SceneImpl.this.checkNull(((CoreCameraComponentManager) this.mManager).get(entity)), component);
                    return component;
                }

                /* access modifiers changed from: package-private */
                public void doSet(int entity, CameraComponent component) {
                    CoreCameraComponent nativeCamera = (CoreCameraComponent) SceneImpl.this.checkNull(((CoreCameraComponentManager) this.mManager).get(entity));
                    SceneImpl.this.javaCameraToNative(component, nativeCamera);
                    ((CoreCameraComponentManager) this.mManager).set(entity, nativeCamera);
                }
            });
        }
    }

    private void initLightComponentManager() {
        CoreComponentManager icm = this.mNativeEcs.getComponentManager("LightComponent");
        if (icm != null) {
            this.mComponentManagerMap.put(LightComponent.class.getSimpleName(), new ComponentManager<LightComponent, CoreLightComponentManager>(icm, CoreLightComponentManager.class, LightComponent.class) {
                /* class com.huawei.agpengine.impl.SceneImpl.AnonymousClass2 */

                /* access modifiers changed from: package-private */
                @Override // com.huawei.agpengine.impl.SceneImpl.ComponentManager
                public LightComponent doGet(int entity) {
                    CoreLightComponent cc = (CoreLightComponent) SceneImpl.this.checkNull(((CoreLightComponentManager) this.mManager).get(entity));
                    LightComponent component = new LightComponent();
                    component.setLightType(Swig.getLightType(cc.getType()));
                    component.setColor(Swig.get(cc.getColor()));
                    component.setIntensity(cc.getIntensity());
                    component.setRange(cc.getRange());
                    component.setSpotInnerAngle(cc.getSpotInnerAngle());
                    component.setSpotOuterAngle(cc.getSpotOuterAngle());
                    component.setShadowEnabled(cc.getShadowEnabled());
                    return component;
                }

                /* access modifiers changed from: package-private */
                public void doSet(int entity, LightComponent component) {
                    CoreLightComponent cc = (CoreLightComponent) SceneImpl.this.checkNull(((CoreLightComponentManager) this.mManager).get(entity));
                    cc.setType(Swig.getNativeLightType(component.getLightType()));
                    cc.setColor(Swig.set(component.getColor()));
                    cc.setIntensity(component.getIntensity());
                    cc.setRange(component.getRange());
                    cc.setSpotInnerAngle(component.getSpotInnerAngle());
                    cc.setSpotOuterAngle(component.getSpotOuterAngle());
                    cc.setShadowEnabled(component.isShadowEnabled());
                    ((CoreLightComponentManager) this.mManager).set(entity, cc);
                }
            });
        }
    }

    private void initLocalMatrixComponentManager() {
        CoreComponentManager icm = this.mNativeEcs.getComponentManager("LocalMatrixComponent");
        if (icm != null) {
            this.mComponentManagerMap.put(LocalMatrixComponent.class.getSimpleName(), new ComponentManager<LocalMatrixComponent, CoreLocalMatrixComponentManager>(icm, CoreLocalMatrixComponentManager.class, LocalMatrixComponent.class) {
                /* class com.huawei.agpengine.impl.SceneImpl.AnonymousClass3 */

                /* access modifiers changed from: package-private */
                @Override // com.huawei.agpengine.impl.SceneImpl.ComponentManager
                public LocalMatrixComponent doGet(int entity) {
                    LocalMatrixComponent component = new LocalMatrixComponent();
                    Matrix4x4 localMatrix = new Matrix4x4();
                    CoreMat4X4 matrix = ((CoreLocalMatrixComponent) SceneImpl.this.checkNull(((CoreLocalMatrixComponentManager) this.mManager).get(entity))).getMatrix();
                    if (matrix != null) {
                        localMatrix.set(matrix.getData(), 0);
                    }
                    component.setLocalMatrix(localMatrix);
                    return component;
                }

                /* access modifiers changed from: package-private */
                public void doSet(int entity, LocalMatrixComponent component) {
                    CoreLocalMatrixComponent cc = (CoreLocalMatrixComponent) SceneImpl.this.checkNull(((CoreLocalMatrixComponentManager) this.mManager).get(entity));
                    CoreMat4X4 mat = new CoreMat4X4();
                    mat.setData(component.getLocalMatrix().getData());
                    cc.setMatrix(mat);
                    ((CoreLocalMatrixComponentManager) this.mManager).set(entity, cc);
                }
            });
        }
    }

    private void initMorphComponentManager() {
        CoreComponentManager icm = this.mNativeEcs.getComponentManager("MorphComponent");
        if (icm != null) {
            this.mComponentManagerMap.put(MorphComponent.class.getSimpleName(), new ComponentManager<MorphComponent, CoreMorphComponentManager>(icm, CoreMorphComponentManager.class, MorphComponent.class) {
                /* class com.huawei.agpengine.impl.SceneImpl.AnonymousClass4 */

                /* access modifiers changed from: package-private */
                @Override // com.huawei.agpengine.impl.SceneImpl.ComponentManager
                public MorphComponent doGet(int entity) {
                    CoreMorphComponent coreMorphComponent = (CoreMorphComponent) SceneImpl.this.checkNull(((CoreMorphComponentManager) this.mManager).get(entity));
                    return new MorphComponent((long) entity);
                }

                /* access modifiers changed from: package-private */
                public void doSet(int entity, MorphComponent component) {
                }
            });
        }
    }

    private void initNodeComponentManager() {
        CoreComponentManager icm = this.mNativeEcs.getComponentManager("NodeComponent");
        if (icm != null) {
            this.mComponentManagerMap.put(NodeComponent.class.getSimpleName(), new ComponentManager<NodeComponent, CoreNodeComponentManager>(icm, CoreNodeComponentManager.class, NodeComponent.class) {
                /* class com.huawei.agpengine.impl.SceneImpl.AnonymousClass5 */

                /* access modifiers changed from: package-private */
                @Override // com.huawei.agpengine.impl.SceneImpl.ComponentManager
                public NodeComponent doGet(int entity) {
                    CoreNodeComponent cc = (CoreNodeComponent) SceneImpl.this.checkNull(((CoreNodeComponentManager) this.mManager).get(entity));
                    NodeComponent component = new NodeComponent();
                    component.setName(cc.getName());
                    component.setParent(new EntityImpl(SceneImpl.this, cc.getParent()));
                    component.setEnabled(cc.getEnabled());
                    component.setExported(cc.getExported());
                    return component;
                }

                /* access modifiers changed from: package-private */
                public void doSet(int entity, NodeComponent component) {
                    CoreNodeComponent cc = (CoreNodeComponent) SceneImpl.this.checkNull(((CoreNodeComponentManager) this.mManager).get(entity));
                    cc.setName(component.getName());
                    cc.setParent(component.getParent().getId());
                    cc.setEnabled(component.isEnabled());
                    cc.setExported(component.isExported());
                    ((CoreNodeComponentManager) this.mManager).set(entity, cc);
                }
            });
        }
    }

    private void initRenderMeshComponentManager() {
        CoreComponentManager icm = this.mNativeEcs.getComponentManager("RenderMeshComponent");
        if (icm != null) {
            this.mComponentManagerMap.put(RenderMeshComponent.class.getSimpleName(), new ComponentManager<RenderMeshComponent, CoreRenderMeshComponentManager>(icm, CoreRenderMeshComponentManager.class, RenderMeshComponent.class) {
                /* class com.huawei.agpengine.impl.SceneImpl.AnonymousClass6 */

                /* access modifiers changed from: package-private */
                @Override // com.huawei.agpengine.impl.SceneImpl.ComponentManager
                public RenderMeshComponent doGet(int entity) {
                    CoreRenderMeshComponent cc = (CoreRenderMeshComponent) SceneImpl.this.checkNull(((CoreRenderMeshComponentManager) this.mManager).get(entity));
                    RenderMeshComponent component = new RenderMeshComponent();
                    component.setMesh(new ResourceHandleImpl(SceneImpl.this.mEngine, cc.getMesh()));
                    component.setMaterial(new ResourceHandleImpl(SceneImpl.this.mEngine, cc.getMaterial()));
                    component.setCastShadowsEnabled(cc.getCastShadows());
                    return component;
                }

                /* access modifiers changed from: package-private */
                public void doSet(int entity, RenderMeshComponent component) {
                    CoreRenderMeshComponent cc = (CoreRenderMeshComponent) SceneImpl.this.checkNull(((CoreRenderMeshComponentManager) this.mManager).get(entity));
                    cc.setMesh(ResourceHandleImpl.getNativeHandle(component.getMesh()));
                    cc.setMaterial(ResourceHandleImpl.getNativeHandle(component.getMaterial()));
                    cc.setCastShadows(component.isCastShadowsEnabled());
                    ((CoreRenderMeshComponentManager) this.mManager).set(entity, cc);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public Vector3[] readVector3Values(CoreVec3ArrayView values) {
        int valueCount = (int) values.size();
        Vector3[] vectorValues = new Vector3[valueCount];
        for (int i = 0; i < valueCount; i++) {
            vectorValues[i] = Swig.get(values.get((long) i));
        }
        return vectorValues;
    }

    private void initSceneComponentManager() {
        CoreComponentManager icm = this.mNativeEcs.getComponentManager("SceneComponent");
        if (icm != null) {
            this.mComponentManagerMap.put(SceneComponent.class.getSimpleName(), new ComponentManager<SceneComponent, CoreSceneComponentManager>(icm, CoreSceneComponentManager.class, SceneComponent.class) {
                /* class com.huawei.agpengine.impl.SceneImpl.AnonymousClass7 */

                /* access modifiers changed from: package-private */
                @Override // com.huawei.agpengine.impl.SceneImpl.ComponentManager
                public SceneComponent doGet(int entity) {
                    CoreSceneComponent cc = (CoreSceneComponent) SceneImpl.this.checkNull(((CoreSceneComponentManager) this.mManager).get(entity));
                    SceneComponent component = new SceneComponent();
                    component.setEnvironmentDiffuseColor(Swig.get(cc.getEnvironmentDiffuseColor()));
                    component.setEnvironmentSpecularColor(Swig.get(cc.getEnvironmentSpecularColor()));
                    component.setEnvironmentDiffuseIntensity(cc.getEnvironmentDiffuseIntensity());
                    component.setEnvironmentSpecularIntensity(cc.getEnvironmentSpecularIntensity());
                    component.setCamera(new EntityImpl(SceneImpl.this, cc.getCamera()));
                    component.setRadianceCubemap(new GpuResourceHandleImpl(SceneImpl.this.mEngine, cc.getRadianceCubemap()));
                    component.setEnvMap(new GpuResourceHandleImpl(SceneImpl.this.mEngine, cc.getEnvMap()));
                    component.setBackgroundType(Swig.getEnvBgType(cc.getBackgroundType()));
                    component.setIrradianceCoefficients(SceneImpl.this.readVector3Values(Core.getSceneIrradianceCoefficients(cc)));
                    component.setEnvironmentRotation(Swig.get(cc.getEnvironmentRotation()));
                    return component;
                }

                /* access modifiers changed from: package-private */
                public void doSet(int entity, SceneComponent component) {
                    CoreSceneComponent cc = (CoreSceneComponent) SceneImpl.this.checkNull(((CoreSceneComponentManager) this.mManager).get(entity));
                    cc.setEnvironmentDiffuseColor(Swig.set(component.getEnvironmentDiffuseColor()));
                    cc.setEnvironmentSpecularColor(Swig.set(component.getEnvironmentSpecularColor()));
                    cc.setEnvironmentDiffuseIntensity(component.getEnvironmentDiffuseIntensity());
                    cc.setEnvironmentSpecularIntensity(component.getEnvironmentSpecularIntensity());
                    cc.setCamera(component.getCamera().getId());
                    cc.setRadianceCubemap(component.getRadianceCubemap().getNativeHandle());
                    cc.setEnvMap(component.getEnvMap().getNativeHandle());
                    cc.setBackgroundType(Swig.getNativeEnvBgType(component.getBackgroundType()));
                    Vector3[] irradianceCoefficients = component.getIrradianceCoefficients();
                    if (irradianceCoefficients != null) {
                        for (int i = 0; i < irradianceCoefficients.length; i++) {
                            Core.setSceneIrradianceCoefficient(cc, Swig.set(irradianceCoefficients[i]), i);
                        }
                    }
                    cc.setEnvironmentRotation(Swig.set(component.getEnvironmentRotation()));
                    ((CoreSceneComponentManager) this.mManager).set(entity, cc);
                }
            });
        }
    }

    private void initTransformComponentManager() {
        CoreComponentManager icm = this.mNativeEcs.getComponentManager("TransformComponent");
        if (icm != null) {
            this.mComponentManagerMap.put(TransformComponent.class.getSimpleName(), new ComponentManager<TransformComponent, CoreTransformComponentManager>(icm, CoreTransformComponentManager.class, TransformComponent.class) {
                /* class com.huawei.agpengine.impl.SceneImpl.AnonymousClass8 */

                /* access modifiers changed from: package-private */
                @Override // com.huawei.agpengine.impl.SceneImpl.ComponentManager
                public TransformComponent doGet(int entity) {
                    CoreTransformComponent cc = (CoreTransformComponent) SceneImpl.this.checkNull(((CoreTransformComponentManager) this.mManager).get(entity));
                    TransformComponent component = new TransformComponent();
                    component.setPosition(Swig.get(cc.getPosition()));
                    component.setRotation(Swig.get(cc.getRotation()));
                    component.setScale(Swig.get(cc.getScale()));
                    return component;
                }

                /* access modifiers changed from: package-private */
                public void doSet(int entity, TransformComponent component) {
                    CoreTransformComponent cc = (CoreTransformComponent) SceneImpl.this.checkNull(((CoreTransformComponentManager) this.mManager).get(entity));
                    cc.setPosition(Swig.set(component.getPosition()));
                    cc.setRotation(Swig.set(component.getRotation()));
                    cc.setScale(Swig.set(component.getScale()));
                    ((CoreTransformComponentManager) this.mManager).set(entity, cc);
                }
            });
        }
    }

    private void initWorldMatrixComponentManager() {
        CoreComponentManager icm = this.mNativeEcs.getComponentManager("WorldMatrixComponent");
        if (icm != null) {
            this.mComponentManagerMap.put(WorldMatrixComponent.class.getSimpleName(), new ComponentManager<WorldMatrixComponent, CoreWorldMatrixComponentManager>(icm, CoreWorldMatrixComponentManager.class, WorldMatrixComponent.class) {
                /* class com.huawei.agpengine.impl.SceneImpl.AnonymousClass9 */

                /* access modifiers changed from: package-private */
                @Override // com.huawei.agpengine.impl.SceneImpl.ComponentManager
                public WorldMatrixComponent doGet(int entity) {
                    WorldMatrixComponent component = new WorldMatrixComponent();
                    component.setWorldMatrix(new Matrix4x4());
                    CoreMat4X4 worldMatrix = ((CoreWorldMatrixComponent) SceneImpl.this.checkNull(((CoreWorldMatrixComponentManager) this.mManager).get(entity))).getMatrix();
                    if (worldMatrix != null) {
                        component.getWorldMatrix().set(worldMatrix.getData(), 0);
                    }
                    return component;
                }

                /* access modifiers changed from: package-private */
                public void doSet(int entity, WorldMatrixComponent component) {
                    CoreWorldMatrixComponent cc = (CoreWorldMatrixComponent) SceneImpl.this.checkNull(((CoreWorldMatrixComponentManager) this.mManager).get(entity));
                    CoreMat4X4 mat = new CoreMat4X4();
                    mat.setData(component.getWorldMatrix().getData());
                    cc.setMatrix(mat);
                    ((CoreWorldMatrixComponentManager) this.mManager).set(entity, cc);
                }
            });
        }
    }
}
