package ohos.agp.render.render3d.impl;

import java.math.BigInteger;
import java.nio.Buffer;
import ohos.agp.render.render3d.impl.CoreEcs;
import ohos.agp.render.render3d.impl.CoreGltf2Importer;
import ohos.agp.render.render3d.impl.CoreGltfData;
import ohos.agp.render.render3d.impl.CoreMeshBuilder;
import ohos.agp.render.render3d.impl.CoreMorphingSystem;
import ohos.agp.render.render3d.impl.CoreRenderNodeGraphLoader;
import ohos.agp.render.render3d.impl.CoreResourceCreator;
import ohos.agp.render.render3d.impl.CoreResourceManager;
import ohos.agp.render.render3d.impl.CoreSystemGraphLoader;

/* access modifiers changed from: package-private */
public class CoreJni {
    static native void addInCoreComponentManagerArray(long j, CoreComponentManagerArray coreComponentManagerArray, long j2, CoreComponentManager coreComponentManager);

    static native void addInCoreEntityArray(long j, CoreEntityArray coreEntityArray, long j2, CoreEntity coreEntity);

    static native void addInCoreFloatArray(long j, CoreFloatArray coreFloatArray, float f);

    static native void addInCoreMeshPrimitiveDescArray(long j, CoreMeshPrimitiveDescArray coreMeshPrimitiveDescArray, long j2, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native void addInCoreSystemArray(long j, CoreSystemArray coreSystemArray, long j2, CoreSystem coreSystem);

    static native void addInCoreVec3Array(long j, CoreVec3Array coreVec3Array, long j2, CoreVec3 coreVec3);

    static native void addInCoreVec4Array(long j, CoreVec4Array coreVec4Array, long j2, CoreVec4 coreVec4);

    static native void addListenerInCoreEcs(long j, CoreEcs coreEcs, long j2, CoreEcs.CoreListener coreListener);

    static native void addPrimitiveInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder, long j2, CoreMeshBuilder.CorePrimitive corePrimitive);

    static native boolean aliveInCoreEntityManager(long j, CoreEntityManager coreEntityManager, long j2, CoreEntity coreEntity);

    static native void allocateInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder);

    static native long animationRepeatCountInfiniteAgpget();

    static native void calculateAabbInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder, long j2, long j3, CoreVec3ArrayView coreVec3ArrayView);

    static native void cancelInCoreGltf2Importer(long j, CoreGltf2Importer coreGltf2Importer);

    static native long capacityInCoreComponentManagerArray(long j, CoreComponentManagerArray coreComponentManagerArray);

    static native long capacityInCoreEntityArray(long j, CoreEntityArray coreEntityArray);

    static native long capacityInCoreFloatArray(long j, CoreFloatArray coreFloatArray);

    static native long capacityInCoreGpuResourceArray(long j, CoreGpuResourceArray coreGpuResourceArray);

    static native long capacityInCoreMeshPrimitiveDescArray(long j, CoreMeshPrimitiveDescArray coreMeshPrimitiveDescArray);

    static native long capacityInCoreRayCastResultArray(long j, CoreRayCastResultArray coreRayCastResultArray);

    static native long capacityInCoreResourceArray(long j, CoreResourceArray coreResourceArray);

    static native long capacityInCoreResourceCreatorInfoArray(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray);

    static native long capacityInCoreResourceInfoArray(long j, CoreResourceInfoArray coreResourceInfoArray);

    static native long capacityInCoreSceneNodeArray(long j, CoreSceneNodeArray coreSceneNodeArray);

    static native long capacityInCoreStringArray(long j, CoreStringArray coreStringArray);

    static native long capacityInCoreSystemArray(long j, CoreSystemArray coreSystemArray);

    static native long capacityInCoreVec3Array(long j, CoreVec3Array coreVec3Array);

    static native long capacityInCoreVec4Array(long j, CoreVec4Array coreVec4Array);

    static native long classUpcastCoreAnimation(long j);

    static native long classUpcastCoreAnimationSystem(long j);

    static native long classUpcastCoreCameraComponentManager(long j);

    static native long classUpcastCoreLightComponentManager(long j);

    static native long classUpcastCoreLocalMatrixComponentManager(long j);

    static native long classUpcastCoreMaterial(long j);

    static native long classUpcastCoreMesh(long j);

    static native long classUpcastCoreMorphComponentManager(long j);

    static native long classUpcastCoreMorphingSystem(long j);

    static native long classUpcastCoreNodeComponentManager(long j);

    static native long classUpcastCoreNodeSystem(long j);

    static native long classUpcastCoreRenderMeshComponentManager(long j);

    static native long classUpcastCoreSceneComponentManager(long j);

    static native long classUpcastCoreTransformComponentManager(long j);

    static native long classUpcastCoreWorldMatrixComponentManager(long j);

    static native void clearInCoreComponentManagerArray(long j, CoreComponentManagerArray coreComponentManagerArray);

    static native void clearInCoreEntityArray(long j, CoreEntityArray coreEntityArray);

    static native void clearInCoreFloatArray(long j, CoreFloatArray coreFloatArray);

    static native void clearInCoreGpuResourceArray(long j, CoreGpuResourceArray coreGpuResourceArray);

    static native void clearInCoreMeshPrimitiveDescArray(long j, CoreMeshPrimitiveDescArray coreMeshPrimitiveDescArray);

    static native void clearInCoreRayCastResultArray(long j, CoreRayCastResultArray coreRayCastResultArray);

    static native void clearInCoreResourceArray(long j, CoreResourceArray coreResourceArray);

    static native void clearInCoreResourceCreatorInfoArray(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray);

    static native void clearInCoreResourceInfoArray(long j, CoreResourceInfoArray coreResourceInfoArray);

    static native void clearInCoreSceneNodeArray(long j, CoreSceneNodeArray coreSceneNodeArray);

    static native void clearInCoreStringArray(long j, CoreStringArray coreStringArray);

    static native void clearInCoreSystemArray(long j, CoreSystemArray coreSystemArray);

    static native void clearInCoreVec3Array(long j, CoreVec3Array coreVec3Array);

    static native void clearInCoreVec4Array(long j, CoreVec4Array coreVec4Array);

    static native void clearModifiedFlagsInCoreComponentManager(long j, CoreComponentManager coreComponentManager);

    static native long cloneEntityInCoreEcs(long j, CoreEcs coreEcs, long j2, CoreEntity coreEntity);

    static native long cloneNodeInCoreNodeSystem(long j, CoreNodeSystem coreNodeSystem, long j2, CoreSceneNode coreSceneNode, boolean z);

    static native long componentCountInCoreComponentManager(long j, CoreComponentManager coreComponentManager);

    static native void copyDataToBufferOnCpu(long j, CoreEngine coreEngine, long j2, CoreByteArrayView coreByteArrayView, long j3, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void copyDataToImage(long j, CoreEngine coreEngine, long j2, CoreByteArrayView coreByteArrayView, long j3, CoreGpuResourceHandle coreGpuResourceHandle, long j4, long j5);

    static native long createAndroidNativeWindow(long j, CorePlatform corePlatform, long j2);

    static native long createAndroidPlatform(Object obj);

    static native BigInteger createAndroidSurface(long j, CoreDevice coreDevice, long j2, CoreNativeWindow coreNativeWindow);

    static native long createDevice(long j, CoreEngine coreEngine, int i, long j2, long j3, int i2, int i3, int i4, int i5);

    static native long createDeviceInCoreEngine(long j, CoreEngine coreEngine, long j2, CoreDeviceCreateInfo coreDeviceCreateInfo);

    static native long createEngine(long j, CoreEngineCreateInfo coreEngineCreateInfo);

    static native long createGltf2Importer(long j, CoreEngine coreEngine, long j2, CoreResourceCreator coreResourceCreator);

    static native long createHandleInCoreMorphingSystem(long j, CoreMorphingSystem coreMorphingSystem);

    static native long createImage0(long j, CoreEngine coreEngine, String str, long j2, long j3, int i);

    static native long createImage1(long j, CoreEngine coreEngine, String str, String str2);

    static native void createInCoreComponentManager(long j, CoreComponentManager coreComponentManager, long j2, CoreEntity coreEntity);

    static native long createInCoreEntityManager(long j, CoreEntityManager coreEntityManager);

    static native long createInCoreGpuResourceManager0(long j, CoreGpuResourceManager coreGpuResourceManager, String str, long j2, CoreGpuBufferDesc coreGpuBufferDesc);

    static native long createInCoreGpuResourceManager1(long j, CoreGpuResourceManager coreGpuResourceManager, String str, long j2, CoreGpuBufferDesc coreGpuBufferDesc, long j3, CoreByteArrayView coreByteArrayView);

    static native long createInCoreGpuResourceManager10(long j, CoreGpuResourceManager coreGpuResourceManager, String str, long j2, CoreGpuSamplerDesc coreGpuSamplerDesc);

    static native long createInCoreGpuResourceManager2(long j, CoreGpuResourceManager coreGpuResourceManager, long j2, CoreGpuBufferDesc coreGpuBufferDesc, long j3, CoreByteArrayView coreByteArrayView);

    static native long createInCoreGpuResourceManager3(long j, CoreGpuResourceManager coreGpuResourceManager, long j2, CoreGpuBufferDesc coreGpuBufferDesc);

    static native long createInCoreGpuResourceManager4(long j, CoreGpuResourceManager coreGpuResourceManager, String str, long j2, CoreGpuImageDesc coreGpuImageDesc);

    static native long createInCoreGpuResourceManager5(long j, CoreGpuResourceManager coreGpuResourceManager, String str, long j2, CoreGpuImageDesc coreGpuImageDesc, long j3, CoreByteArrayView coreByteArrayView);

    static native long createInCoreGpuResourceManager6(long j, CoreGpuResourceManager coreGpuResourceManager, String str, long j2, CoreGpuImageDesc coreGpuImageDesc, long j3, CoreByteArrayView coreByteArrayView, long j4, CoreBufferImageCopyArrayView coreBufferImageCopyArrayView);

    static native long createInCoreGpuResourceManager7(long j, CoreGpuResourceManager coreGpuResourceManager, long j2, CoreGpuImageDesc coreGpuImageDesc);

    static native long createInCoreGpuResourceManager8(long j, CoreGpuResourceManager coreGpuResourceManager, long j2, CoreGpuImageDesc coreGpuImageDesc, long j3, CoreByteArrayView coreByteArrayView);

    static native long createInCoreGpuResourceManager9(long j, CoreGpuResourceManager coreGpuResourceManager, long j2, CoreGpuImageDesc coreGpuImageDesc, long j3, CoreByteArrayView coreByteArrayView, long j4, CoreBufferImageCopyArrayView coreBufferImageCopyArrayView);

    static native long createInCoreGraphicsContext0(long j, CoreEngine coreEngine);

    static native long createInCoreGraphicsContext1(long j, CoreEngineCreateInfo coreEngineCreateInfo, long j2, CoreDeviceCreateInfo coreDeviceCreateInfo);

    static native long createInCoreRenderNodeGraphManager0(long j, CoreRenderNodeGraphManager coreRenderNodeGraphManager, int i, long j2, CoreRenderNodeGraphDesc coreRenderNodeGraphDesc, String str);

    static native long createInCoreRenderNodeGraphManager1(long j, CoreRenderNodeGraphManager coreRenderNodeGraphManager, int i, long j2, CoreRenderNodeGraphDesc coreRenderNodeGraphDesc);

    static native long createInCoreResourceCreator0(long j, CoreResourceCreator coreResourceCreator, String str, String str2, long j2, CoreImageCreateInfo coreImageCreateInfo);

    static native long createInCoreResourceCreator1(long j, CoreResourceCreator coreResourceCreator, String str, String str2, long j2, CoreMaterialCreateInfo coreMaterialCreateInfo);

    static native long createInCoreResourceCreator2(long j, CoreResourceCreator coreResourceCreator, String str, String str2, long j2, CoreMeshCreateInfo coreMeshCreateInfo);

    static native long createInCoreResourceCreator3(long j, CoreResourceCreator coreResourceCreator, String str, String str2, long j2, CoreSkinCreateInfo coreSkinCreateInfo);

    static native long createMeshBuilder(long j, CoreShaderManager coreShaderManager, int i);

    static native long createNodeInCoreNodeSystem(long j, CoreNodeSystem coreNodeSystem);

    static native long createPlaybackInCoreAnimationSystem(long j, CoreAnimationSystem coreAnimationSystem, long j2, CoreResourceHandle coreResourceHandle, long j3, CoreSceneNode coreSceneNode);

    static native void createPod(long j, CoreRenderDataStorePod coreRenderDataStorePod, String str, String str2, long j2, CoreByteArrayView coreByteArrayView);

    static native long createRenderDataStorePod(long j, CoreEngine coreEngine, String str);

    static native long createRenderNodeGraphLoader();

    static native long createResourceCreator(long j, CoreEngine coreEngine, long j2, CoreEcs coreEcs);

    static native void createSwapchainInCoreEngine(long j, CoreEngine coreEngine, long j2, CoreSwapchainCreateInfo coreSwapchainCreateInfo);

    static native long createSystemGraphLoader();

    static native long createTextureViewOes(long j, CoreEngine coreEngine, String str, long j2, long j3, long j4);

    static native long createUniformRingBuffer(long j, CoreEngine coreEngine, String str, long j2);

    static native void deleteCoreAnimationDesc(long j);

    static native void deleteCoreAnimationSystem(long j);

    static native void deleteCoreAnimationTrackDesc(long j);

    static native void deleteCoreAnimationTrackDescArrayView(long j);

    static native void deleteCoreBackendExtra(long j);

    static native void deleteCoreBufferImageCopyArray(long j);

    static native void deleteCoreBufferImageCopyArrayView(long j);

    static native void deleteCoreByteArrayView(long j);

    static native void deleteCoreCameraComponent(long j);

    static native void deleteCoreComponentManager(long j);

    static native void deleteCoreComponentManagerArray(long j);

    static native void deleteCoreContextInfo(long j);

    static native void deleteCoreDeviceConfiguration(long j);

    static native void deleteCoreDeviceCreateInfo(long j);

    static native void deleteCoreDevicePlatformData(long j);

    static native void deleteCoreEcs(long j);

    static native void deleteCoreEcsCoreListener(long j);

    static native void deleteCoreEngine(long j);

    static native void deleteCoreEngineCreateInfo(long j);

    static native void deleteCoreEngineTime(long j);

    static native void deleteCoreEntity(long j);

    static native void deleteCoreEntityArray(long j);

    static native void deleteCoreEntityArrayView(long j);

    static native void deleteCoreEntityManager(long j);

    static native void deleteCoreFloatArray(long j);

    static native void deleteCoreFloatArrayView(long j);

    static native void deleteCoreGltf2ImporterCoreDeleter(long j);

    static native void deleteCoreGltf2ImporterCoreListener(long j);

    static native void deleteCoreGltfDataCoreDeleter(long j);

    static native void deleteCoreGltfDataCoreThumbnailImage(long j);

    static native void deleteCoreGltfImportResult(long j);

    static native void deleteCoreGltfLoadResult(long j);

    static native void deleteCoreGltfResourceData(long j);

    static native void deleteCoreGpuResourceArray(long j);

    static native void deleteCoreGpuResourceHandle(long j);

    static native void deleteCoreGpuResourceHandleUtil(long j);

    static native void deleteCoreLightComponent(long j);

    static native void deleteCoreLocalMatrixComponent(long j);

    static native void deleteCoreMat4X4(long j);

    static native void deleteCoreMaterialCreateInfo(long j);

    static native void deleteCoreMaterialDesc(long j);

    static native void deleteCoreMeshBuilderCoreDeleter(long j);

    static native void deleteCoreMeshBuilderCorePrimitive(long j);

    static native void deleteCoreMeshCreateInfo(long j);

    static native void deleteCoreMeshDesc(long j);

    static native void deleteCoreMeshPrimitiveDesc(long j);

    static native void deleteCoreMeshPrimitiveDescArray(long j);

    static native void deleteCoreMeshPrimitiveDescArrayView(long j);

    static native void deleteCoreMinAndMax(long j);

    static native void deleteCoreMorphComponent(long j);

    static native void deleteCoreMorphingSystem(long j);

    static native void deleteCoreMorphingSystemCoreProperties(long j);

    static native void deleteCoreNativeWindow(long j);

    static native void deleteCoreNodeComponent(long j);

    static native void deleteCoreNodeSystem(long j);

    static native void deleteCorePlatform(long j);

    static native void deleteCorePluginRegister(long j);

    static native void deleteCoreQuat(long j);

    static native void deleteCoreRayCastResult(long j);

    static native void deleteCoreRayCastResultArray(long j);

    static native void deleteCoreRenderHandle(long j);

    static native void deleteCoreRenderHandleUtil(long j);

    static native void deleteCoreRenderMeshComponent(long j);

    static native void deleteCoreRenderNodeDesc(long j);

    static native void deleteCoreRenderNodeDescArrayView(long j);

    static native void deleteCoreRenderNodeGraphDesc(long j);

    static native void deleteCoreRenderNodeGraphInput(long j);

    static native void deleteCoreRenderNodeGraphInputArrayView(long j);

    static native void deleteCoreRenderNodeGraphLoaderCoreDeleter(long j);

    static native void deleteCoreRenderNodeGraphLoaderCoreLoadResult(long j);

    static native void deleteCoreRenderer(long j);

    static native void deleteCoreResourceArray(long j);

    static native void deleteCoreResourceCreator(long j);

    static native void deleteCoreResourceCreatorCoreInfo(long j);

    static native void deleteCoreResourceCreatorInfoArray(long j);

    static native void deleteCoreResourceDataHandle(long j);

    static native void deleteCoreResourceHandle(long j);

    static native void deleteCoreResourceInfoArray(long j);

    static native void deleteCoreResourceManager(long j);

    static native void deleteCoreResourceManagerCoreResourceInfo(long j);

    static native void deleteCoreSceneComponent(long j);

    static native void deleteCoreSceneNode(long j);

    static native void deleteCoreSceneNodeArray(long j);

    static native void deleteCoreSceneNodeArrayView(long j);

    static native void deleteCoreStringArray(long j);

    static native void deleteCoreStringArrayView(long j);

    static native void deleteCoreStringViewArrayView(long j);

    static native void deleteCoreSwapchainCreateInfo(long j);

    static native void deleteCoreSystem(long j);

    static native void deleteCoreSystemArray(long j);

    static native void deleteCoreSystemGraphLoaderCoreDeleter(long j);

    static native void deleteCoreSystemGraphLoaderCoreLoadResult(long j);

    static native void deleteCoreTransformComponent(long j);

    static native void deleteCoreVec2(long j);

    static native void deleteCoreVec2ArrayView(long j);

    static native void deleteCoreVec3(long j);

    static native void deleteCoreVec3Array(long j);

    static native void deleteCoreVec3ArrayView(long j);

    static native void deleteCoreVec4(long j);

    static native void deleteCoreVec4Array(long j);

    static native void deleteCoreVec4ArrayView(long j);

    static native void deleteCoreVersionInfo(long j);

    static native void deleteCoreWorldMatrixComponent(long j);

    static native void destroyAllEntitiesInCoreEntityManager(long j, CoreEntityManager coreEntityManager);

    static native void destroyAndroidNativeWindow(long j, CoreNativeWindow coreNativeWindow);

    static native void destroyAndroidSurface(long j, CoreDevice coreDevice, BigInteger bigInteger);

    static native void destroyEcsInstance(long j, CoreEcs coreEcs);

    static native boolean destroyInCoreComponentManager0(long j, CoreComponentManager coreComponentManager, long j2, CoreEntity coreEntity);

    static native void destroyInCoreComponentManager1(long j, CoreComponentManager coreComponentManager, long j2, CoreEntityArrayView coreEntityArrayView);

    static native void destroyInCoreEntityManager(long j, CoreEntityManager coreEntityManager, long j2, CoreEntity coreEntity);

    static native void destroyInCoreGpuResourceManager(long j, CoreGpuResourceManager coreGpuResourceManager, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void destroyInCoreGraphicsContext(long j, CoreGraphicsContext coreGraphicsContext);

    static native void destroyInCoreRenderNodeGraphManager(long j, CoreRenderNodeGraphManager coreRenderNodeGraphManager, long j2, CoreRenderHandle coreRenderHandle);

    static native void destroyInCoreResource(long j, CoreResource coreResource);

    static native void destroyNodeInCoreNodeSystem(long j, CoreNodeSystem coreNodeSystem, long j2, CoreSceneNode coreSceneNode);

    static native void destroyPlaybackInCoreAnimationSystem(long j, CoreAnimationSystem coreAnimationSystem, long j2, CoreAnimationPlayback coreAnimationPlayback);

    static native void destroyPod(long j, CoreRenderDataStorePod coreRenderDataStorePod, String str, String str2);

    static native void destroyRenderDataStorePod(long j, CoreEngine coreEngine, long j2, CoreRenderDataStorePod coreRenderDataStorePod);

    static native void destroySwapchainInCoreEngine(long j, CoreEngine coreEngine);

    static native void doAddInCoreGpuResourceArray0(long j, CoreGpuResourceArray coreGpuResourceArray, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void doAddInCoreGpuResourceArray1(long j, CoreGpuResourceArray coreGpuResourceArray, int i, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void doAddInCoreRayCastResultArray0(long j, CoreRayCastResultArray coreRayCastResultArray, long j2, CoreRayCastResult coreRayCastResult);

    static native void doAddInCoreRayCastResultArray1(long j, CoreRayCastResultArray coreRayCastResultArray, int i, long j2, CoreRayCastResult coreRayCastResult);

    static native void doAddInCoreResourceArray0(long j, CoreResourceArray coreResourceArray, long j2, CoreResourceHandle coreResourceHandle);

    static native void doAddInCoreResourceArray1(long j, CoreResourceArray coreResourceArray, int i, long j2, CoreResourceHandle coreResourceHandle);

    static native void doAddInCoreResourceCreatorInfoArray0(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray, long j2, CoreResourceCreator.CoreInfo coreInfo);

    static native void doAddInCoreResourceCreatorInfoArray1(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray, int i, long j2, CoreResourceCreator.CoreInfo coreInfo);

    static native void doAddInCoreResourceInfoArray0(long j, CoreResourceInfoArray coreResourceInfoArray, long j2, CoreResourceManager.CoreResourceInfo coreResourceInfo);

    static native void doAddInCoreResourceInfoArray1(long j, CoreResourceInfoArray coreResourceInfoArray, int i, long j2, CoreResourceManager.CoreResourceInfo coreResourceInfo);

    static native void doAddInCoreSceneNodeArray0(long j, CoreSceneNodeArray coreSceneNodeArray, long j2, CoreSceneNode coreSceneNode);

    static native void doAddInCoreSceneNodeArray1(long j, CoreSceneNodeArray coreSceneNodeArray, int i, long j2, CoreSceneNode coreSceneNode);

    static native void doAddInCoreStringArray0(long j, CoreStringArray coreStringArray, String str);

    static native void doAddInCoreStringArray1(long j, CoreStringArray coreStringArray, int i, String str);

    static native long doGetInCoreGpuResourceArray(long j, CoreGpuResourceArray coreGpuResourceArray, int i);

    static native long doGetInCoreRayCastResultArray(long j, CoreRayCastResultArray coreRayCastResultArray, int i);

    static native long doGetInCoreResourceArray(long j, CoreResourceArray coreResourceArray, int i);

    static native long doGetInCoreResourceCreatorInfoArray(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray, int i);

    static native long doGetInCoreResourceInfoArray(long j, CoreResourceInfoArray coreResourceInfoArray, int i);

    static native long doGetInCoreSceneNodeArray(long j, CoreSceneNodeArray coreSceneNodeArray, int i);

    static native String doGetInCoreStringArray(long j, CoreStringArray coreStringArray, int i);

    static native long doRemoveInCoreGpuResourceArray(long j, CoreGpuResourceArray coreGpuResourceArray, int i);

    static native long doRemoveInCoreRayCastResultArray(long j, CoreRayCastResultArray coreRayCastResultArray, int i);

    static native long doRemoveInCoreResourceArray(long j, CoreResourceArray coreResourceArray, int i);

    static native long doRemoveInCoreResourceCreatorInfoArray(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray, int i);

    static native long doRemoveInCoreResourceInfoArray(long j, CoreResourceInfoArray coreResourceInfoArray, int i);

    static native long doRemoveInCoreSceneNodeArray(long j, CoreSceneNodeArray coreSceneNodeArray, int i);

    static native String doRemoveInCoreStringArray(long j, CoreStringArray coreStringArray, int i);

    static native void doRemoveRangeInCoreGpuResourceArray(long j, CoreGpuResourceArray coreGpuResourceArray, int i, int i2);

    static native void doRemoveRangeInCoreRayCastResultArray(long j, CoreRayCastResultArray coreRayCastResultArray, int i, int i2);

    static native void doRemoveRangeInCoreResourceArray(long j, CoreResourceArray coreResourceArray, int i, int i2);

    static native void doRemoveRangeInCoreResourceCreatorInfoArray(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray, int i, int i2);

    static native void doRemoveRangeInCoreResourceInfoArray(long j, CoreResourceInfoArray coreResourceInfoArray, int i, int i2);

    static native void doRemoveRangeInCoreSceneNodeArray(long j, CoreSceneNodeArray coreSceneNodeArray, int i, int i2);

    static native void doRemoveRangeInCoreStringArray(long j, CoreStringArray coreStringArray, int i, int i2);

    static native long doSetInCoreGpuResourceArray(long j, CoreGpuResourceArray coreGpuResourceArray, int i, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native long doSetInCoreRayCastResultArray(long j, CoreRayCastResultArray coreRayCastResultArray, int i, long j2, CoreRayCastResult coreRayCastResult);

    static native long doSetInCoreResourceArray(long j, CoreResourceArray coreResourceArray, int i, long j2, CoreResourceHandle coreResourceHandle);

    static native long doSetInCoreResourceCreatorInfoArray(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray, int i, long j2, CoreResourceCreator.CoreInfo coreInfo);

    static native long doSetInCoreResourceInfoArray(long j, CoreResourceInfoArray coreResourceInfoArray, int i, long j2, CoreResourceManager.CoreResourceInfo coreResourceInfo);

    static native long doSetInCoreSceneNodeArray(long j, CoreSceneNodeArray coreSceneNodeArray, int i, long j2, CoreSceneNode coreSceneNode);

    static native String doSetInCoreStringArray(long j, CoreStringArray coreStringArray, int i, String str);

    static native int doSizeInCoreGpuResourceArray(long j, CoreGpuResourceArray coreGpuResourceArray);

    static native int doSizeInCoreRayCastResultArray(long j, CoreRayCastResultArray coreRayCastResultArray);

    static native int doSizeInCoreResourceArray(long j, CoreResourceArray coreResourceArray);

    static native int doSizeInCoreResourceCreatorInfoArray(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray);

    static native int doSizeInCoreResourceInfoArray(long j, CoreResourceInfoArray coreResourceInfoArray);

    static native int doSizeInCoreSceneNodeArray(long j, CoreSceneNodeArray coreSceneNodeArray);

    static native int doSizeInCoreStringArray(long j, CoreStringArray coreStringArray);

    static native long entityInCoreComponentManager(long j, CoreComponentManager coreComponentManager, long j2);

    static native void eraseInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native void eraseInCoreResourceManager(long j, CoreResourceManager coreResourceManager, long j2, CoreResourceHandle coreResourceHandle);

    static native void eraseRenderNodeInCoreRenderNodeGraphManager(long j, CoreRenderNodeGraphManager coreRenderNodeGraphManager, long j2, CoreRenderHandle coreRenderHandle, long j3);

    static native boolean executeInCoreGltf2Importer(long j, CoreGltf2Importer coreGltf2Importer, long j2);

    static native void frameStartInCoreDevGui(long j, CoreDevGui coreDevGui);

    static native void gcInCoreComponentManager(long j, CoreComponentManager coreComponentManager);

    static native long generateCone(long j, CoreEcs coreEcs, long j2, CoreResourceCreator coreResourceCreator, String str, long j3, CoreResourceHandle coreResourceHandle, float f, float f2, long j4);

    static native long generateConeMesh(long j, CoreResourceCreator coreResourceCreator, String str, long j2, CoreResourceHandle coreResourceHandle, float f, float f2, long j3);

    static native long generateCube(long j, CoreEcs coreEcs, long j2, CoreResourceCreator coreResourceCreator, String str, long j3, CoreResourceHandle coreResourceHandle, float f, float f2, float f3);

    static native long generateCubeMesh(long j, CoreResourceCreator coreResourceCreator, String str, long j2, CoreResourceHandle coreResourceHandle, float f, float f2, float f3);

    static native long generatePlane(long j, CoreEcs coreEcs, long j2, CoreResourceCreator coreResourceCreator, String str, long j3, CoreResourceHandle coreResourceHandle, float f, float f2);

    static native long generatePlaneMesh(long j, CoreResourceCreator coreResourceCreator, String str, long j2, CoreResourceHandle coreResourceHandle, float f, float f2);

    static native long generateSphere(long j, CoreEcs coreEcs, long j2, CoreResourceCreator coreResourceCreator, String str, long j3, CoreResourceHandle coreResourceHandle, float f, long j4, long j5);

    static native long generateSphereMesh(long j, CoreResourceCreator coreResourceCreator, String str, long j2, CoreResourceHandle coreResourceHandle, float f, long j3, long j4);

    static native long getAnimationFromHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native long getAnimationHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, String str);

    static native long getAnimationsInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator);

    static native int getBackendTypeInCoreDevice(long j, CoreDevice coreDevice);

    static native long getBufferDescriptorInCoreGpuResourceManager(long j, CoreGpuResourceManager coreGpuResourceManager, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native long getBufferHandleInCoreGpuResourceManager(long j, CoreGpuResourceManager coreGpuResourceManager, String str);

    static native long getChildInCoreSceneNode(long j, CoreSceneNode coreSceneNode, String str);

    static native long getChildrenInCoreSceneNode(long j, CoreSceneNode coreSceneNode);

    static native long getComponentGenerationInCoreComponentManager(long j, CoreComponentManager coreComponentManager, long j2);

    static native long getComponentIdInCoreComponentManager(long j, CoreComponentManager coreComponentManager, long j2, CoreEntity coreEntity);

    static native long getComponentManagerInCoreEcs(long j, CoreEcs coreEcs, String str);

    static native long getComponentManagerMetadataInCorePluginRegister(long j, CorePluginRegister corePluginRegister);

    static native long getCoreMorphingSystemMaxCharTableSize();

    static native long getDataInCoreGltfLoadResult(long j, CoreGltfLoadResult coreGltfLoadResult);

    static native long getDefaultSceneIndexInCoreGltfData(long j, CoreGltfData coreGltfData);

    static native long getDescInCoreAnimation(long j, CoreAnimation coreAnimation);

    static native long getDescInCoreMaterial(long j, CoreMaterial coreMaterial);

    static native long getDescInCoreMesh(long j, CoreMesh coreMesh);

    static native long getDevGuiInCoreEcs(long j, CoreEcs coreEcs);

    static native long getDevGuiInCoreEngine(long j, CoreEngine coreEngine);

    static native long getDeviceInCoreEngine(long j, CoreEngine coreEngine);

    static native float getDurationInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback);

    static native long getEcsInCoreGraphicsContext(long j, CoreGraphicsContext coreGraphicsContext);

    static native long getEcsInCoreSystem(long j, CoreSystem coreSystem);

    static native long getEcsInstance(long j, CoreResourceManager coreResourceManager, long j2, CoreDevGui coreDevGui);

    static native boolean getEffectivelyEnabledInCoreSceneNode(long j, CoreSceneNode coreSceneNode);

    static native boolean getEnabledInCoreSceneNode(long j, CoreSceneNode coreSceneNode);

    static native long getEngineInCoreGraphicsContext(long j, CoreGraphicsContext coreGraphicsContext);

    static native long getEngineTimeInCoreEngine(long j, CoreEngine coreEngine);

    static native long getEntityInCoreSceneNode(long j, CoreSceneNode coreSceneNode);

    static native long getEntityManagerInCoreEcs(long j, CoreEcs coreEcs);

    static native long getExternalFileUrisInCoreGltfData(long j, CoreGltfData coreGltfData);

    static native long getFileManagerInCoreEngine(long j, CoreEngine coreEngine);

    static native long getGenerationCounterInCoreComponentManager(long j, CoreComponentManager coreComponentManager);

    static native long getGenerationCounterInCoreEntityManager(long j, CoreEntityManager coreEntityManager);

    static native long getGpuResourceManagerInCoreEngine(long j, CoreEngine coreEngine);

    static native long getHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, String str);

    static native long getHandleInCoreResourceManager(long j, CoreResourceManager coreResourceManager, String str);

    static native long getImGuiContextInCoreDevGui(long j, CoreDevGui coreDevGui);

    static native long getImageDescriptorInCoreGpuResourceManager(long j, CoreGpuResourceManager coreGpuResourceManager, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native long getImageFromHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native long getImageHandleInCoreGpuResourceManager(long j, CoreGpuResourceManager coreGpuResourceManager, String str);

    static native long getImageHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, String str);

    static native long getImagesInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator);

    static native long getInCoreAnimationTrackDescArrayView(long j, CoreAnimationTrackDescArrayView coreAnimationTrackDescArrayView, long j2);

    static native long getInCoreBufferImageCopyArray(long j, CoreBufferImageCopyArray coreBufferImageCopyArray, long j2);

    static native long getInCoreBufferImageCopyArrayView(long j, CoreBufferImageCopyArrayView coreBufferImageCopyArrayView, long j2);

    static native short getInCoreByteArrayView(long j, CoreByteArrayView coreByteArrayView, long j2);

    static native long getInCoreCameraComponentManager0(long j, CoreCameraComponentManager coreCameraComponentManager, long j2);

    static native long getInCoreCameraComponentManager1(long j, CoreCameraComponentManager coreCameraComponentManager, long j2, CoreEntity coreEntity);

    static native long getInCoreComponentManagerArray(long j, CoreComponentManagerArray coreComponentManagerArray, int i);

    static native long getInCoreEntityArray(long j, CoreEntityArray coreEntityArray, int i);

    static native long getInCoreEntityArrayView(long j, CoreEntityArrayView coreEntityArrayView, long j2);

    static native float getInCoreFloatArray(long j, CoreFloatArray coreFloatArray, int i);

    static native float getInCoreFloatArrayView(long j, CoreFloatArrayView coreFloatArrayView, long j2);

    static native long getInCoreLightComponentManager0(long j, CoreLightComponentManager coreLightComponentManager, long j2);

    static native long getInCoreLightComponentManager1(long j, CoreLightComponentManager coreLightComponentManager, long j2, CoreEntity coreEntity);

    static native long getInCoreLocalMatrixComponentManager0(long j, CoreLocalMatrixComponentManager coreLocalMatrixComponentManager, long j2);

    static native long getInCoreLocalMatrixComponentManager1(long j, CoreLocalMatrixComponentManager coreLocalMatrixComponentManager, long j2, CoreEntity coreEntity);

    static native long getInCoreMeshPrimitiveDescArray(long j, CoreMeshPrimitiveDescArray coreMeshPrimitiveDescArray, int i);

    static native long getInCoreMeshPrimitiveDescArrayView(long j, CoreMeshPrimitiveDescArrayView coreMeshPrimitiveDescArrayView, long j2);

    static native long getInCoreMorphComponentManager0(long j, CoreMorphComponentManager coreMorphComponentManager, long j2);

    static native long getInCoreMorphComponentManager1(long j, CoreMorphComponentManager coreMorphComponentManager, long j2, CoreEntity coreEntity);

    static native long getInCoreNodeComponentManager0(long j, CoreNodeComponentManager coreNodeComponentManager, long j2);

    static native long getInCoreNodeComponentManager1(long j, CoreNodeComponentManager coreNodeComponentManager, long j2, CoreEntity coreEntity);

    static native long getInCoreRenderMeshComponentManager0(long j, CoreRenderMeshComponentManager coreRenderMeshComponentManager, long j2);

    static native long getInCoreRenderMeshComponentManager1(long j, CoreRenderMeshComponentManager coreRenderMeshComponentManager, long j2, CoreEntity coreEntity);

    static native long getInCoreRenderNodeDescArrayView(long j, CoreRenderNodeDescArrayView coreRenderNodeDescArrayView, long j2);

    static native long getInCoreRenderNodeGraphInputArrayView(long j, CoreRenderNodeGraphInputArrayView coreRenderNodeGraphInputArrayView, long j2);

    static native long getInCoreSceneComponentManager0(long j, CoreSceneComponentManager coreSceneComponentManager, long j2);

    static native long getInCoreSceneComponentManager1(long j, CoreSceneComponentManager coreSceneComponentManager, long j2, CoreEntity coreEntity);

    static native long getInCoreSceneNodeArrayView(long j, CoreSceneNodeArrayView coreSceneNodeArrayView, long j2);

    static native String getInCoreStringArrayView(long j, CoreStringArrayView coreStringArrayView, long j2);

    static native String getInCoreStringViewArrayView(long j, CoreStringViewArrayView coreStringViewArrayView, long j2);

    static native long getInCoreSystemArray(long j, CoreSystemArray coreSystemArray, int i);

    static native long getInCoreTransformComponentManager0(long j, CoreTransformComponentManager coreTransformComponentManager, long j2);

    static native long getInCoreTransformComponentManager1(long j, CoreTransformComponentManager coreTransformComponentManager, long j2, CoreEntity coreEntity);

    static native long getInCoreVec2ArrayView(long j, CoreVec2ArrayView coreVec2ArrayView, long j2);

    static native long getInCoreVec3Array(long j, CoreVec3Array coreVec3Array, int i);

    static native long getInCoreVec3ArrayView(long j, CoreVec3ArrayView coreVec3ArrayView, long j2);

    static native long getInCoreVec4Array(long j, CoreVec4Array coreVec4Array, int i);

    static native long getInCoreVec4ArrayView(long j, CoreVec4ArrayView coreVec4ArrayView, long j2);

    static native long getInCoreWorldMatrixComponentManager0(long j, CoreWorldMatrixComponentManager coreWorldMatrixComponentManager, long j2);

    static native long getInCoreWorldMatrixComponentManager1(long j, CoreWorldMatrixComponentManager coreWorldMatrixComponentManager, long j2, CoreEntity coreEntity);

    static native long getIndexCountInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder);

    static native long getIndexDataInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder);

    static native long getInstanceInCoreDevice(long j, CoreDevice coreDevice);

    static native long getJointBoundsDataInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder);

    static native long getJointBoundsInCoreMesh(long j, CoreMesh coreMesh);

    static native long getJointDataInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder);

    static native long getKeyframesFloatInCoreAnimation(long j, CoreAnimation coreAnimation, long j2, CoreAnimationTrackDesc coreAnimationTrackDesc);

    static native long getKeyframesVec3InCoreAnimation(long j, CoreAnimation coreAnimation, long j2, CoreAnimationTrackDesc coreAnimationTrackDesc);

    static native long getKeyframesVec4InCoreAnimation(long j, CoreAnimation coreAnimation, long j2, CoreAnimationTrackDesc coreAnimationTrackDesc);

    static native long getMaterialFromHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native long getMaterialHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, String str);

    static native long getMaterialsInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator);

    static native long getMeshFromHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native long getMeshHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, String str);

    static native long getMeshesInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator);

    static native long getModifiedFlagsInCoreComponentManager(long j, CoreComponentManager coreComponentManager);

    static native long getMorphTargetDataInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder);

    static native int getMouseCursorStateInCoreDevGui(long j, CoreDevGui coreDevGui);

    static native String getName0(long j, CoreCameraComponentManager coreCameraComponentManager);

    static native String getName1(long j, CoreLightComponentManager coreLightComponentManager);

    static native String getName10(long j, CoreAnimationSystem coreAnimationSystem);

    static native String getName11(long j, CoreMorphingSystem coreMorphingSystem);

    static native String getName2(long j, CoreLocalMatrixComponentManager coreLocalMatrixComponentManager);

    static native String getName3(long j, CoreNodeComponentManager coreNodeComponentManager);

    static native String getName4(long j, CoreRenderMeshComponentManager coreRenderMeshComponentManager);

    static native String getName5(long j, CoreSceneComponentManager coreSceneComponentManager);

    static native String getName6(long j, CoreTransformComponentManager coreTransformComponentManager);

    static native String getName7(long j, CoreWorldMatrixComponentManager coreWorldMatrixComponentManager);

    static native String getName8(long j, CoreMorphComponentManager coreMorphComponentManager);

    static native String getName9(long j, CoreNodeSystem coreNodeSystem);

    static native String getNameInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback);

    static native String getNameInCoreSceneNode(long j, CoreSceneNode coreSceneNode);

    static native long getNodeGraphDescriptionInCoreRenderNodeGraphLoader(long j, CoreRenderNodeGraphLoader coreRenderNodeGraphLoader);

    static native long getNodeInCoreNodeSystem(long j, CoreNodeSystem coreNodeSystem, long j2, CoreEntity coreEntity);

    static native long getParentInCoreSceneNode(long j, CoreSceneNode coreSceneNode);

    static native long getPlatformDataInCoreDevice(long j, CoreDevice coreDevice);

    static native long getPlaybackCountInCoreAnimationSystem(long j, CoreAnimationSystem coreAnimationSystem);

    static native long getPlaybackInCoreAnimationSystem(long j, CoreAnimationSystem coreAnimationSystem, long j2);

    static native int getPlaybackStateInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback);

    static native long getPluginRegisterInCoreEngine(long j, CoreEngine coreEngine);

    static native long getPod(long j, CoreRenderDataStorePod coreRenderDataStorePod, String str);

    static native long getPositionInCoreSceneNode(long j, CoreSceneNode coreSceneNode);

    static native long getPrimitiveInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder, long j2);

    static native long getPrimitivesInCoreMesh(long j, CoreMesh coreMesh);

    static native long getPrimitivesInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder);

    static native long getPropertiesInCoreResource(long j, CoreResource coreResource);

    static native long getRenderDataStoreManagerInCoreEngine(long j, CoreEngine coreEngine);

    static native long getRenderDataStoreManagerInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator);

    static native long getRenderDataStoreManagerInCoreResourceManager(long j, CoreResourceManager coreResourceManager);

    static native long getRenderDataStorePod(long j, CoreEngine coreEngine, String str);

    static native int getRenderModeInCoreEcs(long j, CoreEcs coreEcs);

    static native long getRenderNodeCountInCoreRenderNodeGraphManager(long j, CoreRenderNodeGraphManager coreRenderNodeGraphManager, long j2, CoreRenderHandle coreRenderHandle);

    static native long getRenderNodeGraphManagerInCoreEngine(long j, CoreEngine coreEngine);

    static native long getRenderNodeManagerInCoreEngine(long j, CoreEngine coreEngine);

    static native long getRenderNodeNamesInCoreRenderNodeGraphManager(long j, CoreRenderNodeGraphManager coreRenderNodeGraphManager, long j2, CoreRenderHandle coreRenderHandle);

    static native long getRendererInCoreEngine(long j, CoreEngine coreEngine);

    static native long getRepeatCountInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback);

    static native long getResourceCreatorInCoreGraphicsContext(long j, CoreGraphicsContext coreGraphicsContext);

    static native long getResourceFromHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native long getResourceFromHandleInCoreResourceManager(long j, CoreResourceManager coreResourceManager, long j2, CoreResourceHandle coreResourceHandle);

    static native long getResourceManagerInCoreEcs(long j, CoreEcs coreEcs);

    static native long getResourceManagerInCoreEngine(long j, CoreEngine coreEngine);

    static native long getResourcesInCoreResourceManager0(long j, CoreResourceManager coreResourceManager);

    static native long getResourcesInCoreResourceManager1(long j, CoreResourceManager coreResourceManager, long j2);

    static native long getResultInCoreGltf2Importer(long j, CoreGltf2Importer coreGltf2Importer);

    static native long getRootNodeInCoreNodeSystem(long j, CoreNodeSystem coreNodeSystem);

    static native long getRotationInCoreSceneNode(long j, CoreSceneNode coreSceneNode);

    static native long getSamplerDescriptorInCoreGpuResourceManager(long j, CoreGpuResourceManager coreGpuResourceManager, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native long getSamplerHandleInCoreGpuResourceManager(long j, CoreGpuResourceManager coreGpuResourceManager, String str);

    static native long getScaleInCoreSceneNode(long j, CoreSceneNode coreSceneNode);

    static native long getSceneCountInCoreGltfData(long j, CoreGltfData coreGltfData);

    static native long getSceneIrradianceCoefficients(long j, CoreSceneComponent coreSceneComponent);

    static native long getShaderManagerInCoreEngine(long j, CoreEngine coreEngine);

    static native long getShaderManagerInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator);

    static native long getShaderManagerInCoreResourceManager(long j, CoreResourceManager coreResourceManager);

    static native long getSkinFromHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native long getSkinHandleInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, String str);

    static native long getSkinsInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator);

    static native float getSpeedInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback);

    static native long getSystemInCoreEcs(long j, CoreEcs coreEcs, String str);

    static native long getSystemMetadataInCorePluginRegister(long j, CorePluginRegister corePluginRegister);

    static native long getTargetCountInCoreMorphingSystem(long j, CoreMorphingSystem coreMorphingSystem, long j2);

    static native long getTargetNamesInCoreMorphingSystem(long j, CoreMorphingSystem coreMorphingSystem, long j2);

    static native long getTargetWeightsInCoreMorphingSystem(long j, CoreMorphingSystem coreMorphingSystem, long j2);

    static native long getThumbnailImageCountInCoreGltfData(long j, CoreGltfData coreGltfData);

    static native long getThumbnailImageInCoreGltfData(long j, CoreGltfData coreGltfData, long j2, long j3, CoreFileManager coreFileManager);

    static native float getTimePositionInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback);

    static native long getTimestampsInCoreAnimation(long j, CoreAnimation coreAnimation, long j2, CoreAnimationTrackDesc coreAnimationTrackDesc);

    static native long getTracksInCoreAnimation(long j, CoreAnimation coreAnimation);

    static native long getTransformComponentAabb(long j, CoreEntity coreEntity, boolean z, long j2, CoreEcs coreEcs);

    static native BigInteger getTypeInCoreResource(long j, CoreResource coreResource);

    static native long getUniqueIdInCoreGpuResourceHandleUtil(long j, CoreGpuResourceHandle coreGpuResourceHandle);

    static native long getUniqueIdInCoreRenderHandleUtil(long j, CoreRenderHandle coreRenderHandle);

    static native long getVaraabbMaxCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc);

    static native long getVaraabbMaxCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVaraabbMinCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc);

    static native long getVaraabbMinCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native short getVaradditionalFlagsCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native float getVaralphaCutoffCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native int getVaralphaModeCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native float getVarambientOcclusionFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVaranimationsCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData);

    static native long getVaraoCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native float getVaraspectCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native long getVarbackendConfigurationCoreDeviceCreateInfo(long j, CoreDeviceCreateInfo coreDeviceCreateInfo);

    static native short getVarbackgroundTypeCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native long getVarbaseColorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarbaseColorFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarbaseCoreMat4X4(long j, CoreMat4X4 coreMat4X4);

    static native long getVarbufferingCountCoreDeviceConfiguration(long j, CoreDeviceConfiguration coreDeviceConfiguration);

    static native long getVarcameraCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native boolean getVarcastShadowsCoreRenderMeshComponent(long j, CoreRenderMeshComponent coreRenderMeshComponent);

    static native float getVarclearCoatFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native float getVarclearCoatRoughnessCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarcolorCoreLightComponent(long j, CoreLightComponent coreLightComponent);

    static native boolean getVarcolorsCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive);

    static native long getVardataCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc);

    static native long getVardataCoreGltfDataCoreThumbnailImage(long j, CoreGltfData.CoreThumbnailImage coreThumbnailImage);

    static native long getVardataCoreGltfImportResult(long j, CoreGltfImportResult coreGltfImportResult);

    static native float[] getVardataCoreMat4X4(long j, CoreMat4X4 coreMat4X4);

    static native float[] getVardataCoreQuat(long j, CoreQuat coreQuat);

    static native float[] getVardataCoreVec2(long j, CoreVec2 coreVec2);

    static native float[] getVardataCoreVec3(long j, CoreVec3 coreVec3);

    static native float[] getVardataCoreVec4(long j, CoreVec4 coreVec4);

    static native long getVardataElementCountCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc);

    static native long getVardataStoreManagerCoreMorphingSystemCoreProperties(long j, CoreMorphingSystem.CoreProperties coreProperties);

    static native String getVardataStoreNameCoreMorphingSystemCoreProperties(long j, CoreMorphingSystem.CoreProperties coreProperties);

    static native BigInteger getVardeltaTimeCoreEngineTime(long j, CoreEngineTime coreEngineTime);

    static native long getVardescCoreMaterialCreateInfo(long j, CoreMaterialCreateInfo coreMaterialCreateInfo);

    static native long getVardeviceConfigurationCoreDeviceCreateInfo(long j, CoreDeviceCreateInfo coreDeviceCreateInfo);

    static native float getVardistanceCoreRayCastResult(long j, CoreRayCastResult coreRayCastResult);

    static native boolean getVareffectivelyEnabledCoreNodeComponent(long j, CoreNodeComponent coreNodeComponent);

    static native long getVaremissiveCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVaremissiveFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native boolean getVarenabledCoreNodeComponent(long j, CoreNodeComponent coreNodeComponent);

    static native long getVarenvMapCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native float getVarenvMapLodLevelCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native long getVarenvironmentDiffuseColorCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native float getVarenvironmentDiffuseIntensityCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native long getVarenvironmentRotationCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native long getVarenvironmentSpecularColorCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native float getVarenvironmentSpecularIntensityCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native String getVarerrorCoreGltfImportResult(long j, CoreGltfImportResult coreGltfImportResult);

    static native String getVarerrorCoreGltfLoadResult(long j, CoreGltfLoadResult coreGltfLoadResult);

    static native String getVarerrorCoreRenderNodeGraphLoaderCoreLoadResult(long j, CoreRenderNodeGraphLoader.CoreLoadResult coreLoadResult);

    static native String getVarerrorCoreSystemGraphLoaderCoreLoadResult(long j, CoreSystemGraphLoader.CoreLoadResult coreLoadResult);

    static native boolean getVarexportedCoreNodeComponent(long j, CoreNodeComponent coreNodeComponent);

    static native String getVarextensionCoreGltfDataCoreThumbnailImage(long j, CoreGltfData.CoreThumbnailImage coreThumbnailImage);

    static native long getVarextraMaterialRenderingFlagsCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarflagsCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarframeCountCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc);

    static native float getVarglossinessFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarhandleCoreResourceCreatorCoreInfo(long j, CoreResourceCreator.CoreInfo coreInfo);

    static native long getVarhandleCoreResourceManagerCoreResourceInfo(long j, CoreResourceManager.CoreResourceInfo coreResourceInfo);

    static native long getVaridCoreEntity(long j, CoreEntity coreEntity);

    static native BigInteger getVaridCoreGpuResourceHandle(long j, CoreGpuResourceHandle coreGpuResourceHandle);

    static native BigInteger getVaridCoreRenderHandle(long j, CoreRenderHandle coreRenderHandle);

    static native long getVaridCoreResourceDataHandle(long j, CoreResourceDataHandle coreResourceDataHandle);

    static native long getVaridCoreResourceHandle(long j, CoreResourceHandle coreResourceHandle);

    static native long getVarimagesCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData);

    static native long getVarindexBufferCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc);

    static native long getVarindexByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarindexCountCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive);

    static native long getVarindexCountCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo);

    static native long getVarindexCountCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarindexDataCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo);

    static native long getVarindexOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native int getVarindexTypeCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive);

    static native int getVarindexTypeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native float getVarintensityCoreLightComponent(long j, CoreLightComponent coreLightComponent);

    static native int getVarinterpolationModeCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc);

    static native long getVarirradianceCoefficientsCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native long getVarjointAttributeBufferCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc);

    static native long getVarjointBoundsCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc);

    static native long getVarjointBoundsCountCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc);

    static native long getVarjointBoundsDataCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo);

    static native long getVarjointDataCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo);

    static native boolean getVarjointsCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive);

    static native long getVarmaterialCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarmaterialCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive);

    static native long getVarmaterialCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarmaterialCoreRenderMeshComponent(long j, CoreRenderMeshComponent coreRenderMeshComponent);

    static native long getVarmaterialFlagsCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarmaterialsCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData);

    static native long getVarmatrixCoreLocalMatrixComponent(long j, CoreLocalMatrixComponent coreLocalMatrixComponent);

    static native long getVarmatrixCoreWorldMatrixComponent(long j, CoreWorldMatrixComponent coreWorldMatrixComponent);

    static native long getVarmaxAabbCoreMinAndMax(long j, CoreMinAndMax coreMinAndMax);

    static native long getVarmeshCoreRenderMeshComponent(long j, CoreRenderMeshComponent coreRenderMeshComponent);

    static native long getVarmeshesCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData);

    static native float getVarmetallicFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarminAabbCoreMinAndMax(long j, CoreMinAndMax coreMinAndMax);

    static native long getVarmorphTargetBufferCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc);

    static native long getVarmorphTargetByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarmorphTargetCountCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive);

    static native long getVarmorphTargetCountCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarmorphTargetOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarmorphTargetsCoreMorphComponent(long j, CoreMorphComponent coreMorphComponent);

    static native String getVarnameCoreAnimationDesc(long j, CoreAnimationDesc coreAnimationDesc);

    static native String getVarnameCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native String getVarnameCoreNodeComponent(long j, CoreNodeComponent coreNodeComponent);

    static native String getVarnameCoreResourceCreatorCoreInfo(long j, CoreResourceCreator.CoreInfo coreInfo);

    static native String getVarnameCoreResourceManagerCoreResourceInfo(long j, CoreResourceManager.CoreResourceInfo coreResourceInfo);

    static native long getVarnodeCoreRayCastResult(long j, CoreRayCastResult coreRayCastResult);

    static native long getVarnodesCoreRenderNodeGraphDesc(long j, CoreRenderNodeGraphDesc coreRenderNodeGraphDesc);

    static native long getVarnormalCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native float getVarnormalScaleCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native float getVarorthoHeightCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native float getVarorthoWidthCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native long getVarparentCoreNodeComponent(long j, CoreNodeComponent coreNodeComponent);

    static native String getVarpathCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc);

    static native long getVarpositionCoreTransformComponent(long j, CoreTransformComponent coreTransformComponent);

    static native boolean getVarpreferSrgbFormatCoreSwapchainCreateInfo(long j, CoreSwapchainCreateInfo coreSwapchainCreateInfo);

    static native long getVarprimitiveCountCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc);

    static native long getVarprimitivesCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo);

    static native long getVarprimitivesCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc);

    static native long getVarprojectionCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native long getVarradianceCubemapCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native long getVarradianceCubemapMipCountCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent);

    static native float getVarrangeCoreLightComponent(long j, CoreLightComponent coreLightComponent);

    static native float getVarreflectanceCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarrenderNodeGraphHandleCoreRenderNodeGraphInput(long j, CoreRenderNodeGraphInput coreRenderNodeGraphInput);

    static native String getVarrenderNodeGraphNameCoreRenderNodeGraphDesc(long j, CoreRenderNodeGraphDesc coreRenderNodeGraphDesc);

    static native long[] getVarrenderResolutionCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native long getVarrequiredIntegratedMemoryFlagsCoreDeviceConfiguration(long j, CoreDeviceConfiguration coreDeviceConfiguration);

    static native long getVarrotationCoreTransformComponent(long j, CoreTransformComponent coreTransformComponent);

    static native float getVarroughnessFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarsamplerCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarsamplersCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData);

    static native long getVarscaleCoreTransformComponent(long j, CoreTransformComponent coreTransformComponent);

    static native boolean getVarshadowEnabledCoreLightComponent(long j, CoreLightComponent coreLightComponent);

    static native long getVarskinsCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData);

    static native long getVarspecularFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVarspecularRadianceCubemapsCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData);

    static native float getVarspotInnerAngleCoreLightComponent(long j, CoreLightComponent coreLightComponent);

    static native float getVarspotOuterAngleCoreLightComponent(long j, CoreLightComponent coreLightComponent);

    static native boolean getVarsuccessCoreGltfImportResult(long j, CoreGltfImportResult coreGltfImportResult);

    static native boolean getVarsuccessCoreGltfLoadResult(long j, CoreGltfLoadResult coreGltfLoadResult);

    static native boolean getVarsuccessCoreRenderNodeGraphLoaderCoreLoadResult(long j, CoreRenderNodeGraphLoader.CoreLoadResult coreLoadResult);

    static native boolean getVarsuccessCoreSystemGraphLoaderCoreLoadResult(long j, CoreSystemGraphLoader.CoreLoadResult coreLoadResult);

    static native long getVarswapchainFlagsCoreSwapchainCreateInfo(long j, CoreSwapchainCreateInfo coreSwapchainCreateInfo);

    static native boolean getVartangentsCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive);

    static native long getVartargetDataCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo);

    static native long getVartexturesCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData);

    static native long getVartimestampsCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc);

    static native BigInteger getVartotalTimeCoreEngineTime(long j, CoreEngineTime coreEngineTime);

    static native long getVartrackCountCoreAnimationDesc(long j, CoreAnimationDesc coreAnimationDesc);

    static native long getVartracksCoreAnimationDesc(long j, CoreAnimationDesc coreAnimationDesc);

    static native int getVartypeCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc);

    static native short getVartypeCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native short getVartypeCoreLightComponent(long j, CoreLightComponent coreLightComponent);

    static native int getVartypeCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc);

    static native long getVartypeCoreResourceHandle(long j, CoreResourceHandle coreResourceHandle);

    static native String getVaruriCoreResourceCreatorCoreInfo(long j, CoreResourceCreator.CoreInfo coreInfo);

    static native String getVaruriCoreResourceManagerCoreResourceInfo(long j, CoreResourceManager.CoreResourceInfo coreResourceInfo);

    static native long getVarvertexBufferCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc);

    static native long getVarvertexColorByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexColorOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexCountCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive);

    static native long getVarvertexCountCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo);

    static native long getVarvertexCountCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexDataCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo);

    static native long getVarvertexJointIndexByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexJointIndexOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexJointWeightByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexJointWeightOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexNormalByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexNormalOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexPositionByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexPositionOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexTangentByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexTangentOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexUvByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native long getVarvertexUvOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native float getVarverticalFovCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native float[] getVarviewportCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native long getVarwCoreMat4X4(long j, CoreMat4X4 coreMat4X4);

    static native float getVarwCoreQuat(long j, CoreQuat coreQuat);

    static native float getVarwCoreVec4(long j, CoreVec4 coreVec4);

    static native long getVarxCoreMat4X4(long j, CoreMat4X4 coreMat4X4);

    static native float getVarxCoreQuat(long j, CoreQuat coreQuat);

    static native float getVarxCoreVec2(long j, CoreVec2 coreVec2);

    static native float getVarxCoreVec3(long j, CoreVec3 coreVec3);

    static native float getVarxCoreVec4(long j, CoreVec4 coreVec4);

    static native long getVaryCoreMat4X4(long j, CoreMat4X4 coreMat4X4);

    static native float getVaryCoreQuat(long j, CoreQuat coreQuat);

    static native float getVaryCoreVec2(long j, CoreVec2 coreVec2);

    static native float getVaryCoreVec3(long j, CoreVec3 coreVec3);

    static native float getVaryCoreVec4(long j, CoreVec4 coreVec4);

    static native long getVarzCoreMat4X4(long j, CoreMat4X4 coreMat4X4);

    static native float getVarzCoreQuat(long j, CoreQuat coreQuat);

    static native float getVarzCoreVec3(long j, CoreVec3 coreVec3);

    static native float getVarzCoreVec4(long j, CoreVec4 coreVec4);

    static native float getVarzfarCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native float getVarznearCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent);

    static native String getVersionInCoreEngine();

    static native String getVersionInCoreGraphicsContext();

    static native long getVertexCountInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder);

    static native long getVertexDataInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder);

    static native float getWeightInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback);

    static native long getWorldAabb(long j, CoreMat4X4 coreMat4X4, long j2, CoreVec3 coreVec3, long j3, CoreVec3 coreVec32);

    static native long getWorldMatrixComponentAabb(long j, CoreEntity coreEntity, boolean z, long j2, CoreEcs coreEcs);

    static native long gltfInvalidIndexAgpget();

    static native boolean hasComponentInCoreComponentManager(long j, CoreComponentManager coreComponentManager, long j2, CoreEntity coreEntity);

    static native void importGltfAsyncInCoreGltf2Importer(long j, CoreGltf2Importer coreGltf2Importer, long j2, CoreGltfData coreGltfData, long j3, long j4, CoreGltf2Importer.CoreListener coreListener);

    static native void importGltfInCoreGltf2Importer(long j, CoreGltf2Importer coreGltf2Importer, long j2, CoreGltfData coreGltfData, long j3);

    static native long importGltfScene0(long j, long j2, CoreGltfData coreGltfData, long j3, CoreGltfResourceData coreGltfResourceData, long j4, CoreEcs coreEcs, long j5, CoreEntity coreEntity, long j6);

    static native long importGltfScene1(long j, long j2, CoreGltfData coreGltfData, long j3, CoreGltfResourceData coreGltfResourceData, long j4, CoreEcs coreEcs, long j5, CoreEntity coreEntity);

    static native long importGltfScene2(long j, long j2, CoreGltfData coreGltfData, long j3, CoreGltfResourceData coreGltfResourceData, long j4, CoreEcs coreEcs);

    static native void initInCoreEngine(long j, CoreEngine coreEngine);

    static native void initInCoreGraphicsContext(long j, CoreGraphicsContext coreGraphicsContext);

    static native void initializeInCoreEcs(long j, CoreEcs coreEcs);

    static native void initializeInCoreSystem(long j, CoreSystem coreSystem);

    static native void inputCharacterInCoreDevGui(long j, CoreDevGui coreDevGui, long j2);

    static native void insertRenderNodeInCoreRenderNodeGraphManager(long j, CoreRenderNodeGraphManager coreRenderNodeGraphManager, long j2, CoreRenderHandle coreRenderHandle, long j3, CoreRenderNodeDesc coreRenderNodeDesc, long j4);

    static native long invalidEntityAgpget();

    static native BigInteger invalidGpuResourceHandleAgpget();

    static native boolean isActiveInCoreSystem(long j, CoreSystem coreSystem);

    static native boolean isAncestorOfInCoreSceneNode(long j, CoreSceneNode coreSceneNode, long j2, CoreSceneNode coreSceneNode2);

    static native boolean isAnimationInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native boolean isCompletedInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback);

    static native boolean isCompletedInCoreGltf2Importer(long j, CoreGltf2Importer coreGltf2Importer);

    static native boolean isComputeShaderInCoreGpuResourceHandleUtil(long j, CoreGpuResourceHandle coreGpuResourceHandle);

    static native boolean isDebugBuildInCoreEngine();

    static native boolean isEmptyInCoreComponentManagerArray(long j, CoreComponentManagerArray coreComponentManagerArray);

    static native boolean isEmptyInCoreEntityArray(long j, CoreEntityArray coreEntityArray);

    static native boolean isEmptyInCoreFloatArray(long j, CoreFloatArray coreFloatArray);

    static native boolean isEmptyInCoreGpuResourceArray(long j, CoreGpuResourceArray coreGpuResourceArray);

    static native boolean isEmptyInCoreMeshPrimitiveDescArray(long j, CoreMeshPrimitiveDescArray coreMeshPrimitiveDescArray);

    static native boolean isEmptyInCoreRayCastResultArray(long j, CoreRayCastResultArray coreRayCastResultArray);

    static native boolean isEmptyInCoreResourceArray(long j, CoreResourceArray coreResourceArray);

    static native boolean isEmptyInCoreResourceCreatorInfoArray(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray);

    static native boolean isEmptyInCoreResourceInfoArray(long j, CoreResourceInfoArray coreResourceInfoArray);

    static native boolean isEmptyInCoreSceneNodeArray(long j, CoreSceneNodeArray coreSceneNodeArray);

    static native boolean isEmptyInCoreStringArray(long j, CoreStringArray coreStringArray);

    static native boolean isEmptyInCoreSystemArray(long j, CoreSystemArray coreSystemArray);

    static native boolean isEmptyInCoreVec3Array(long j, CoreVec3Array coreVec3Array);

    static native boolean isEmptyInCoreVec4Array(long j, CoreVec4Array coreVec4Array);

    static native boolean isEnabledInCoreDevGui(long j, CoreDevGui coreDevGui);

    static native boolean isGpuBufferInCoreGpuResourceHandleUtil(long j, CoreGpuResourceHandle coreGpuResourceHandle);

    static native boolean isGpuImageInCoreGpuResourceHandleUtil(long j, CoreGpuResourceHandle coreGpuResourceHandle);

    static native boolean isMaterialInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native boolean isMeshInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native boolean isShaderInCoreGpuResourceHandleUtil(long j, CoreGpuResourceHandle coreGpuResourceHandle);

    static native boolean isSkinInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native boolean isValid(long j, CoreEntity coreEntity);

    static native boolean isValidInCoreGpuResourceHandleUtil(long j, CoreGpuResourceHandle coreGpuResourceHandle);

    static native boolean isValidInCoreNativeWindow(long j, CoreNativeWindow coreNativeWindow);

    static native boolean isValidInCoreRenderHandleUtil(long j, CoreRenderHandle coreRenderHandle);

    static native boolean isValidInCoreResourceCreator(long j, CoreResourceCreator coreResourceCreator, long j2, CoreResourceHandle coreResourceHandle);

    static native boolean isValidInCoreResourceManager(long j, CoreResourceManager coreResourceManager, long j2, CoreResourceHandle coreResourceHandle);

    static native boolean loadBuffersInCoreGltfData(long j, CoreGltfData coreGltfData, long j2, CoreFileManager coreFileManager);

    static native long loadGltf0(long j, CoreFileManager coreFileManager, String str);

    static native long loadGltf1(long j, CoreFileManager coreFileManager, long j2, CoreByteArrayView coreByteArrayView);

    static native long loadInCoreRenderNodeGraphLoader0(long j, CoreRenderNodeGraphLoader coreRenderNodeGraphLoader, long j2, CoreFileManager coreFileManager, String str);

    static native long loadInCoreRenderNodeGraphLoader1(long j, CoreRenderNodeGraphLoader coreRenderNodeGraphLoader, String str);

    static native long loadInCoreSystemGraphLoader0(long j, CoreSystemGraphLoader coreSystemGraphLoader, long j2, CoreFileManager coreFileManager, String str, long j3, CoreEcs coreEcs, long j4, CoreComponentManagerTypeInfoArray coreComponentManagerTypeInfoArray, long j5, CoreSystemTypeInfoArray coreSystemTypeInfoArray);

    static native long loadInCoreSystemGraphLoader1(long j, CoreSystemGraphLoader coreSystemGraphLoader, String str, long j2, CoreEcs coreEcs, long j3, CoreComponentManagerTypeInfoArray coreComponentManagerTypeInfoArray, long j4, CoreSystemTypeInfoArray coreSystemTypeInfoArray);

    static native long lookupNodeByComponentInCoreSceneNode(long j, CoreSceneNode coreSceneNode, long j2, CoreComponentManager coreComponentManager);

    static native long lookupNodeByNameInCoreSceneNode(long j, CoreSceneNode coreSceneNode, String str);

    static native long lookupNodeByPathInCoreSceneNode(long j, CoreSceneNode coreSceneNode, String str);

    static native long lookupNodesByComponentInCoreSceneNode(long j, CoreSceneNode coreSceneNode, long j2, CoreComponentManager coreComponentManager);

    static native long maxAnimationPathLengthAgpget();

    static native long maxEntitiesInCoreEntityManager(long j, CoreEntityManager coreEntityManager);

    static native long maxNodeNameLengthAgpget();

    static native String nameInCoreComponentManager(long j, CoreComponentManager coreComponentManager);

    static native String nameInCoreSystem(long j, CoreSystem coreSystem);

    static native boolean needRenderInCoreEcs(long j, CoreEcs coreEcs);

    static native long newCoreAnimationDesc();

    static native long newCoreAnimationTrackDesc();

    static native long newCoreAnimationTrackDescArrayView(Buffer buffer);

    static native long newCoreBackendExtra();

    static native long newCoreBufferImageCopyArray(Buffer buffer);

    static native long newCoreBufferImageCopyArrayView(Buffer buffer);

    static native long newCoreByteArrayView(Buffer buffer);

    static native long newCoreCameraComponent();

    static native long newCoreComponentManagerArray0();

    static native long newCoreComponentManagerArray1(long j);

    static native long newCoreContextInfo();

    static native long newCoreDeviceConfiguration();

    static native long newCoreDevicePlatformData();

    static native long newCoreEngineCreateInfo(long j, CorePlatform corePlatform, long j2, CoreVersionInfo coreVersionInfo, long j3, CoreContextInfo coreContextInfo);

    static native long newCoreEngineTime();

    static native long newCoreEntity();

    static native long newCoreEntityArray0();

    static native long newCoreEntityArray1(long j);

    static native long newCoreEntityArrayView(Buffer buffer);

    static native long newCoreFloatArray0();

    static native long newCoreFloatArray1(long j);

    static native long newCoreFloatArrayView(Buffer buffer);

    static native long newCoreGltf2ImporterCoreDeleter();

    static native long newCoreGltfDataCoreDeleter();

    static native long newCoreGltfResourceData();

    static native long newCoreGpuResourceArray0();

    static native long newCoreGpuResourceArray1(long j, CoreGpuResourceArray coreGpuResourceArray);

    static native long newCoreGpuResourceArray2(int i, long j, CoreGpuResourceHandle coreGpuResourceHandle);

    static native long newCoreGpuResourceHandle();

    static native long newCoreGpuResourceHandleUtil();

    static native long newCoreInfo();

    static native long newCoreLightComponent();

    static native long newCoreLocalMatrixComponent();

    static native long newCoreMat4X4();

    static native long newCoreMaterialCreateInfo();

    static native long newCoreMaterialDesc();

    static native long newCoreMeshBuilderCoreDeleter();

    static native long newCoreMeshCreateInfo();

    static native long newCoreMeshDesc();

    static native long newCoreMeshPrimitiveDesc();

    static native long newCoreMeshPrimitiveDescArray0();

    static native long newCoreMeshPrimitiveDescArray1(long j);

    static native long newCoreMeshPrimitiveDescArrayView(Buffer buffer);

    static native long newCoreMinAndMax();

    static native long newCoreMorphComponent();

    static native long newCoreNativeWindow();

    static native long newCoreNodeComponent();

    static native long newCorePrimitive();

    static native long newCoreQuat0();

    static native long newCoreQuat1(float f, float f2, float f3, float f4);

    static native long newCoreRayCastResult();

    static native long newCoreRayCastResultArray0();

    static native long newCoreRayCastResultArray1(long j, CoreRayCastResultArray coreRayCastResultArray);

    static native long newCoreRayCastResultArray2(int i, long j, CoreRayCastResult coreRayCastResult);

    static native long newCoreRenderHandle();

    static native long newCoreRenderHandleUtil();

    static native long newCoreRenderMeshComponent();

    static native long newCoreRenderNodeDesc();

    static native long newCoreRenderNodeDescArrayView(Buffer buffer);

    static native long newCoreRenderNodeGraphDesc();

    static native long newCoreRenderNodeGraphInput();

    static native long newCoreRenderNodeGraphInputArrayView(Buffer buffer);

    static native long newCoreRenderNodeGraphLoaderCoreDeleter();

    static native long newCoreRenderNodeGraphLoaderCoreLoadResult0();

    static native long newCoreRenderNodeGraphLoaderCoreLoadResult1(String str);

    static native long newCoreResourceArray0();

    static native long newCoreResourceArray1(long j, CoreResourceArray coreResourceArray);

    static native long newCoreResourceArray2(int i, long j, CoreResourceHandle coreResourceHandle);

    static native long newCoreResourceCreatorInfoArray0();

    static native long newCoreResourceCreatorInfoArray1(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray);

    static native long newCoreResourceCreatorInfoArray2(int i, long j, CoreResourceCreator.CoreInfo coreInfo);

    static native long newCoreResourceDataHandle();

    static native long newCoreResourceHandle();

    static native long newCoreResourceInfo();

    static native long newCoreResourceInfoArray0();

    static native long newCoreResourceInfoArray1(long j, CoreResourceInfoArray coreResourceInfoArray);

    static native long newCoreResourceInfoArray2(int i, long j, CoreResourceManager.CoreResourceInfo coreResourceInfo);

    static native long newCoreSceneComponent();

    static native long newCoreSceneNodeArray0();

    static native long newCoreSceneNodeArray1(long j, CoreSceneNodeArray coreSceneNodeArray);

    static native long newCoreSceneNodeArray2(int i, long j, CoreSceneNode coreSceneNode);

    static native long newCoreSceneNodeArrayView(Buffer buffer);

    static native long newCoreStringArray0();

    static native long newCoreStringArray1(long j, CoreStringArray coreStringArray);

    static native long newCoreStringArray2(int i, String str);

    static native long newCoreStringViewArrayView(Buffer buffer);

    static native long newCoreSwapchainCreateInfo(BigInteger bigInteger, boolean z);

    static native long newCoreSystemArray0();

    static native long newCoreSystemArray1(long j);

    static native long newCoreSystemGraphLoaderCoreDeleter();

    static native long newCoreSystemGraphLoaderCoreLoadResult0();

    static native long newCoreSystemGraphLoaderCoreLoadResult1(long j);

    static native long newCoreThumbnailImage();

    static native long newCoreTransformComponent();

    static native long newCoreVec20();

    static native long newCoreVec21(float f, float f2);

    static native long newCoreVec2ArrayView(Buffer buffer);

    static native long newCoreVec30();

    static native long newCoreVec31(float f, float f2, float f3);

    static native long newCoreVec3Array0();

    static native long newCoreVec3Array1(long j);

    static native long newCoreVec3ArrayView(Buffer buffer);

    static native long newCoreVec40();

    static native long newCoreVec41(float f, float f2, float f3, float f4);

    static native long newCoreVec4Array0();

    static native long newCoreVec4Array1(long j);

    static native long newCoreVec4ArrayView(Buffer buffer);

    static native long newCoreVersionInfo(String str, int i, int i2, int i3);

    static native long newCoreWorldMatrixComponent();

    static native void onComponentsAddedInCoreEcsCoreListener(long j, CoreEcs.CoreListener coreListener, long j2, CoreComponentManager coreComponentManager, long j3, CoreEntityArrayView coreEntityArrayView);

    static native void onComponentsRemovedInCoreEcsCoreListener(long j, CoreEcs.CoreListener coreListener, long j2, CoreComponentManager coreComponentManager, long j3, CoreEntityArrayView coreEntityArrayView);

    static native void onEntitiesAddedInCoreEcsCoreListener(long j, CoreEcs.CoreListener coreListener, long j2, CoreEntityArrayView coreEntityArrayView);

    static native void onEntitiesRemovedInCoreEcsCoreListener(long j, CoreEcs.CoreListener coreListener, long j2, CoreEntityArrayView coreEntityArrayView);

    static native void onImportFinishedInCoreGltf2ImporterCoreListener(long j, CoreGltf2Importer.CoreListener coreListener);

    static native void onImportProgressedInCoreGltf2ImporterCoreListener(long j, CoreGltf2Importer.CoreListener coreListener, long j2, long j3);

    static native void onImportStartedInCoreGltf2ImporterCoreListener(long j, CoreGltf2Importer.CoreListener coreListener);

    static native void processEventsInCoreEcs(long j, CoreEcs coreEcs);

    static native long rayCast(long j, CoreEcs coreEcs, long j2, CoreVec3 coreVec3, long j3, CoreVec3 coreVec32);

    static native long rayCastFromCamera(long j, CoreEcs coreEcs, long j2, CoreEntity coreEntity, long j3, CoreVec2 coreVec2);

    static native void registerAssetManager(long j, CoreFileManager coreFileManager, String str, Object obj);

    static native void releaseBuffersInCoreGltfData(long j, CoreGltfData coreGltfData);

    static native void releaseGltfResources(long j, CoreGltfResourceData coreGltfResourceData, long j2, CoreEngine coreEngine, long j3, CoreResourceCreator coreResourceCreator);

    static native void releaseHandleInCoreMorphingSystem(long j, CoreMorphingSystem coreMorphingSystem, long j2);

    static native void removeListenerInCoreEcs(long j, CoreEcs coreEcs, long j2, CoreEcs.CoreListener coreListener);

    static native void renderFrameInCoreEngine0(long j, CoreEngine coreEngine, long j2, CoreRenderNodeGraphInput coreRenderNodeGraphInput);

    static native void renderFrameInCoreEngine1(long j, CoreEngine coreEngine, int i);

    static native void renderFrameInCoreEngine2(long j, CoreEngine coreEngine, long j2, CoreRenderNodeGraphInputArrayView coreRenderNodeGraphInputArrayView);

    static native void renderFrameInCoreGraphicsContext(long j, CoreGraphicsContext coreGraphicsContext, int i);

    static native void renderFrameInCoreRenderer0(long j, CoreRenderer coreRenderer, long j2, CoreRenderNodeGraphInput coreRenderNodeGraphInput);

    static native void renderFrameInCoreRenderer1(long j, CoreRenderer coreRenderer, int i);

    static native void renderFrameInCoreRenderer2(long j, CoreRenderer coreRenderer, long j2, CoreRenderNodeGraphInputArrayView coreRenderNodeGraphInputArrayView);

    static native void requestRenderInCoreEcs(long j, CoreEcs coreEcs);

    static native void reserveInCoreComponentManagerArray(long j, CoreComponentManagerArray coreComponentManagerArray, long j2);

    static native void reserveInCoreEntityArray(long j, CoreEntityArray coreEntityArray, long j2);

    static native void reserveInCoreFloatArray(long j, CoreFloatArray coreFloatArray, long j2);

    static native void reserveInCoreGpuResourceArray(long j, CoreGpuResourceArray coreGpuResourceArray, long j2);

    static native void reserveInCoreMeshPrimitiveDescArray(long j, CoreMeshPrimitiveDescArray coreMeshPrimitiveDescArray, long j2);

    static native void reserveInCoreRayCastResultArray(long j, CoreRayCastResultArray coreRayCastResultArray, long j2);

    static native void reserveInCoreResourceArray(long j, CoreResourceArray coreResourceArray, long j2);

    static native void reserveInCoreResourceCreatorInfoArray(long j, CoreResourceCreatorInfoArray coreResourceCreatorInfoArray, long j2);

    static native void reserveInCoreResourceInfoArray(long j, CoreResourceInfoArray coreResourceInfoArray, long j2);

    static native void reserveInCoreSceneNodeArray(long j, CoreSceneNodeArray coreSceneNodeArray, long j2);

    static native void reserveInCoreStringArray(long j, CoreStringArray coreStringArray, long j2);

    static native void reserveInCoreSystemArray(long j, CoreSystemArray coreSystemArray, long j2);

    static native void reserveInCoreVec3Array(long j, CoreVec3Array coreVec3Array, long j2);

    static native void reserveInCoreVec4Array(long j, CoreVec4Array coreVec4Array, long j2);

    static native boolean saveGltf(long j, CoreEngine coreEngine, long j2, CoreEcs coreEcs, long j3, CoreResourceCreator coreResourceCreator, String str);

    static native long screenToWorld(long j, CoreEcs coreEcs, long j2, CoreEntity coreEntity, long j3, CoreVec3 coreVec3);

    static native void setAabbInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder, long j2, long j3, CoreVec3 coreVec3, long j4, CoreVec3 coreVec32);

    static native void setActiveInCoreSystem(long j, CoreSystem coreSystem, boolean z);

    static native void setDataStoreManagerInCoreMorphingSystem(long j, CoreMorphingSystem coreMorphingSystem, long j2, CoreRenderDataStoreManager coreRenderDataStoreManager);

    static native void setDataStoreNameInCoreMorphingSystem(long j, CoreMorphingSystem coreMorphingSystem, String str);

    static native void setDescInCoreMaterial(long j, CoreMaterial coreMaterial, long j2, CoreMaterialDesc coreMaterialDesc);

    static native void setEnabledInCoreDevGui(long j, CoreDevGui coreDevGui, boolean z);

    static native void setEnabledInCoreSceneNode(long j, CoreSceneNode coreSceneNode, boolean z);

    static native void setInCoreCameraComponentManager0(long j, CoreCameraComponentManager coreCameraComponentManager, long j2, long j3, CoreCameraComponent coreCameraComponent);

    static native void setInCoreCameraComponentManager1(long j, CoreCameraComponentManager coreCameraComponentManager, long j2, CoreEntity coreEntity, long j3, CoreCameraComponent coreCameraComponent);

    static native void setInCoreComponentManagerArray(long j, CoreComponentManagerArray coreComponentManagerArray, int i, long j2, CoreComponentManager coreComponentManager);

    static native void setInCoreEntityArray(long j, CoreEntityArray coreEntityArray, int i, long j2, CoreEntity coreEntity);

    static native void setInCoreFloatArray(long j, CoreFloatArray coreFloatArray, int i, float f);

    static native void setInCoreLightComponentManager0(long j, CoreLightComponentManager coreLightComponentManager, long j2, long j3, CoreLightComponent coreLightComponent);

    static native void setInCoreLightComponentManager1(long j, CoreLightComponentManager coreLightComponentManager, long j2, CoreEntity coreEntity, long j3, CoreLightComponent coreLightComponent);

    static native void setInCoreLocalMatrixComponentManager0(long j, CoreLocalMatrixComponentManager coreLocalMatrixComponentManager, long j2, long j3, CoreLocalMatrixComponent coreLocalMatrixComponent);

    static native void setInCoreLocalMatrixComponentManager1(long j, CoreLocalMatrixComponentManager coreLocalMatrixComponentManager, long j2, CoreEntity coreEntity, long j3, CoreLocalMatrixComponent coreLocalMatrixComponent);

    static native void setInCoreMeshPrimitiveDescArray(long j, CoreMeshPrimitiveDescArray coreMeshPrimitiveDescArray, int i, long j2, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc);

    static native void setInCoreMorphComponentManager0(long j, CoreMorphComponentManager coreMorphComponentManager, long j2, long j3, CoreMorphComponent coreMorphComponent);

    static native void setInCoreMorphComponentManager1(long j, CoreMorphComponentManager coreMorphComponentManager, long j2, CoreEntity coreEntity, long j3, CoreMorphComponent coreMorphComponent);

    static native void setInCoreNodeComponentManager0(long j, CoreNodeComponentManager coreNodeComponentManager, long j2, long j3, CoreNodeComponent coreNodeComponent);

    static native void setInCoreNodeComponentManager1(long j, CoreNodeComponentManager coreNodeComponentManager, long j2, CoreEntity coreEntity, long j3, CoreNodeComponent coreNodeComponent);

    static native void setInCoreRenderMeshComponentManager0(long j, CoreRenderMeshComponentManager coreRenderMeshComponentManager, long j2, long j3, CoreRenderMeshComponent coreRenderMeshComponent);

    static native void setInCoreRenderMeshComponentManager1(long j, CoreRenderMeshComponentManager coreRenderMeshComponentManager, long j2, CoreEntity coreEntity, long j3, CoreRenderMeshComponent coreRenderMeshComponent);

    static native void setInCoreSceneComponentManager0(long j, CoreSceneComponentManager coreSceneComponentManager, long j2, long j3, CoreSceneComponent coreSceneComponent);

    static native void setInCoreSceneComponentManager1(long j, CoreSceneComponentManager coreSceneComponentManager, long j2, CoreEntity coreEntity, long j3, CoreSceneComponent coreSceneComponent);

    static native void setInCoreSystemArray(long j, CoreSystemArray coreSystemArray, int i, long j2, CoreSystem coreSystem);

    static native void setInCoreTransformComponentManager0(long j, CoreTransformComponentManager coreTransformComponentManager, long j2, long j3, CoreTransformComponent coreTransformComponent);

    static native void setInCoreTransformComponentManager1(long j, CoreTransformComponentManager coreTransformComponentManager, long j2, CoreEntity coreEntity, long j3, CoreTransformComponent coreTransformComponent);

    static native void setInCoreVec3Array(long j, CoreVec3Array coreVec3Array, int i, long j2, CoreVec3 coreVec3);

    static native void setInCoreVec4Array(long j, CoreVec4Array coreVec4Array, int i, long j2, CoreVec4 coreVec4);

    static native void setInCoreWorldMatrixComponentManager0(long j, CoreWorldMatrixComponentManager coreWorldMatrixComponentManager, long j2, long j3, CoreWorldMatrixComponent coreWorldMatrixComponent);

    static native void setInCoreWorldMatrixComponentManager1(long j, CoreWorldMatrixComponentManager coreWorldMatrixComponentManager, long j2, CoreEntity coreEntity, long j3, CoreWorldMatrixComponent coreWorldMatrixComponent);

    static native void setIndexDataInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder, long j2, long j3, CoreByteArrayView coreByteArrayView);

    static native void setJointDataInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder, long j2, long j3, CoreByteArrayView coreByteArrayView, long j4, CoreVec4ArrayView coreVec4ArrayView, long j5, CoreVec3ArrayView coreVec3ArrayView);

    static native void setKeyInCoreDevGui(long j, CoreDevGui coreDevGui, int i, boolean z);

    static native void setMorphTargetDataInCoreMeshBuilder(long j, CoreMeshBuilder coreMeshBuilder, long j2, long j3, CoreVec3ArrayView coreVec3ArrayView, long j4, CoreVec3ArrayView coreVec3ArrayView2, long j5, CoreVec4ArrayView coreVec4ArrayView, long j6, CoreVec3ArrayView coreVec3ArrayView3, long j7, CoreVec3ArrayView coreVec3ArrayView4, long j8, CoreVec4ArrayView coreVec4ArrayView2);

    static native void setMouseButtonStateInCoreDevGui(long j, CoreDevGui coreDevGui, int i, boolean z);

    static native void setMousePosInCoreDevGui(long j, CoreDevGui coreDevGui, float f, float f2);

    static native void setNameInCoreSceneNode(long j, CoreSceneNode coreSceneNode, String str);

    static native void setParentInCoreSceneNode(long j, CoreSceneNode coreSceneNode, long j2, CoreSceneNode coreSceneNode2);

    static native void setPlaybackStateInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback, int i);

    static native void setPod(long j, CoreRenderDataStorePod coreRenderDataStorePod, String str, long j2, CoreByteArrayView coreByteArrayView);

    static native void setPositionInCoreSceneNode(long j, CoreSceneNode coreSceneNode, long j2, CoreVec3 coreVec3);

    static native void setPropertiesInCoreResource(long j, CoreResource coreResource, long j2, CorePropertyHandle corePropertyHandle);

    static native void setPropsInCoreSystem(long j, CoreSystem coreSystem, long j2, CorePropertyHandle corePropertyHandle);

    static native void setRenderModeInCoreEcs(long j, CoreEcs coreEcs, int i);

    static native void setRepeatCountInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback, long j2);

    static native void setRotationInCoreSceneNode(long j, CoreSceneNode coreSceneNode, long j2, CoreQuat coreQuat);

    static native void setScaleInCoreSceneNode(long j, CoreSceneNode coreSceneNode, long j2, CoreVec3 coreVec3);

    static native void setSceneIrradianceCoefficient(long j, CoreSceneComponent coreSceneComponent, long j2, CoreVec3 coreVec3, int i);

    static native void setScrollInCoreDevGui(long j, CoreDevGui coreDevGui, float f, float f2);

    static native void setSpeedInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback, float f);

    static native void setTargetNamesArrayInCoreMorphingSystem(long j, CoreMorphingSystem coreMorphingSystem, long j2, String[] strArr, int i);

    static native void setTargetWeightsArrayInCoreMorphingSystem(long j, CoreMorphingSystem coreMorphingSystem, long j2, float[] fArr, int i);

    static native void setTimePositionInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback, float f);

    static native void setVaraabbMaxCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc, long j2, CoreVec3 coreVec3);

    static native void setVaraabbMaxCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2, CoreVec3 coreVec3);

    static native void setVaraabbMinCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc, long j2, CoreVec3 coreVec3);

    static native void setVaraabbMinCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2, CoreVec3 coreVec3);

    static native void setVaradditionalFlagsCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, short s);

    static native void setVaralphaCutoffCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, float f);

    static native void setVaralphaModeCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, int i);

    static native void setVarambientOcclusionFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, float f);

    static native void setVaranimationsCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData, long j2, CoreResourceArray coreResourceArray);

    static native void setVaraoCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVaraspectCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, float f);

    static native void setVarbackendConfigurationCoreDeviceCreateInfo(long j, CoreDeviceCreateInfo coreDeviceCreateInfo, long j2, CoreBackendExtra coreBackendExtra);

    static native void setVarbackgroundTypeCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, short s);

    static native void setVarbaseColorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVarbaseColorFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, long j2, CoreVec4 coreVec4);

    static native void setVarbaseCoreMat4X4(long j, CoreMat4X4 coreMat4X4, long j2, CoreVec4 coreVec4);

    static native void setVarbufferingCountCoreDeviceConfiguration(long j, CoreDeviceConfiguration coreDeviceConfiguration, long j2);

    static native void setVarcameraCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, long j2, CoreEntity coreEntity);

    static native void setVarcastShadowsCoreRenderMeshComponent(long j, CoreRenderMeshComponent coreRenderMeshComponent, boolean z);

    static native void setVarclearCoatFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, float f);

    static native void setVarclearCoatRoughnessCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, float f);

    static native void setVarcolorCoreLightComponent(long j, CoreLightComponent coreLightComponent, long j2, CoreVec3 coreVec3);

    static native void setVarcolorsCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive, boolean z);

    static native void setVardataCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc, long j2, CoreResourceDataHandle coreResourceDataHandle);

    static native void setVardataCoreGltfDataCoreThumbnailImage(long j, CoreGltfData.CoreThumbnailImage coreThumbnailImage, long j2, CoreByteArrayView coreByteArrayView);

    static native void setVardataCoreMat4X4(long j, CoreMat4X4 coreMat4X4, float[] fArr);

    static native void setVardataCoreQuat(long j, CoreQuat coreQuat, float[] fArr);

    static native void setVardataCoreVec2(long j, CoreVec2 coreVec2, float[] fArr);

    static native void setVardataCoreVec3(long j, CoreVec3 coreVec3, float[] fArr);

    static native void setVardataCoreVec4(long j, CoreVec4 coreVec4, float[] fArr);

    static native void setVardataElementCountCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc, long j2);

    static native void setVardataStoreManagerCoreMorphingSystemCoreProperties(long j, CoreMorphingSystem.CoreProperties coreProperties, long j2, CoreRenderDataStoreManager coreRenderDataStoreManager);

    static native void setVardataStoreNameCoreMorphingSystemCoreProperties(long j, CoreMorphingSystem.CoreProperties coreProperties, String str);

    static native void setVardeltaTimeCoreEngineTime(long j, CoreEngineTime coreEngineTime, BigInteger bigInteger);

    static native void setVardescCoreMaterialCreateInfo(long j, CoreMaterialCreateInfo coreMaterialCreateInfo, long j2, CoreMaterialDesc coreMaterialDesc);

    static native void setVardeviceConfigurationCoreDeviceCreateInfo(long j, CoreDeviceCreateInfo coreDeviceCreateInfo, long j2, CoreDeviceConfiguration coreDeviceConfiguration);

    static native void setVardistanceCoreRayCastResult(long j, CoreRayCastResult coreRayCastResult, float f);

    static native void setVareffectivelyEnabledCoreNodeComponent(long j, CoreNodeComponent coreNodeComponent, boolean z);

    static native void setVaremissiveCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVaremissiveFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, long j2, CoreVec3 coreVec3);

    static native void setVarenabledCoreNodeComponent(long j, CoreNodeComponent coreNodeComponent, boolean z);

    static native void setVarenvMapCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVarenvMapLodLevelCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, float f);

    static native void setVarenvironmentDiffuseColorCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, long j2, CoreVec3 coreVec3);

    static native void setVarenvironmentDiffuseIntensityCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, float f);

    static native void setVarenvironmentRotationCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, long j2, CoreQuat coreQuat);

    static native void setVarenvironmentSpecularColorCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, long j2, CoreVec3 coreVec3);

    static native void setVarenvironmentSpecularIntensityCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, float f);

    static native void setVarexportedCoreNodeComponent(long j, CoreNodeComponent coreNodeComponent, boolean z);

    static native void setVarextensionCoreGltfDataCoreThumbnailImage(long j, CoreGltfData.CoreThumbnailImage coreThumbnailImage, String str);

    static native void setVarextraMaterialRenderingFlagsCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, long j2);

    static native void setVarflagsCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarframeCountCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc, long j2);

    static native void setVarglossinessFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, float f);

    static native void setVarhandleCoreResourceCreatorCoreInfo(long j, CoreResourceCreator.CoreInfo coreInfo, long j2, CoreResourceHandle coreResourceHandle);

    static native void setVarhandleCoreResourceManagerCoreResourceInfo(long j, CoreResourceManager.CoreResourceInfo coreResourceInfo, long j2, CoreResourceHandle coreResourceHandle);

    static native void setVaridCoreEntity(long j, CoreEntity coreEntity, long j2);

    static native void setVaridCoreGpuResourceHandle(long j, CoreGpuResourceHandle coreGpuResourceHandle, BigInteger bigInteger);

    static native void setVaridCoreRenderHandle(long j, CoreRenderHandle coreRenderHandle, BigInteger bigInteger);

    static native void setVaridCoreResourceDataHandle(long j, CoreResourceDataHandle coreResourceDataHandle, long j2);

    static native void setVaridCoreResourceHandle(long j, CoreResourceHandle coreResourceHandle, long j2);

    static native void setVarimagesCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData, long j2, CoreResourceArray coreResourceArray);

    static native void setVarindexBufferCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVarindexByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarindexCountCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive, long j2);

    static native void setVarindexCountCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo, long j2);

    static native void setVarindexCountCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarindexDataCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo, long j2, CoreByteArrayView coreByteArrayView);

    static native void setVarindexOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarindexTypeCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive, int i);

    static native void setVarindexTypeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, int i);

    static native void setVarintensityCoreLightComponent(long j, CoreLightComponent coreLightComponent, float f);

    static native void setVarinterpolationModeCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc, int i);

    static native void setVarirradianceCoefficientsCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, long j2, CoreVec3 coreVec3);

    static native void setVarjointAttributeBufferCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVarjointBoundsCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc, long j2, CoreResourceDataHandle coreResourceDataHandle);

    static native void setVarjointBoundsCountCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc, long j2);

    static native void setVarjointBoundsDataCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo, long j2, CoreFloatArrayView coreFloatArrayView);

    static native void setVarjointDataCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo, long j2, CoreByteArrayView coreByteArrayView);

    static native void setVarjointsCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive, boolean z);

    static native void setVarmaterialCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVarmaterialCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive, long j2, CoreResourceHandle coreResourceHandle);

    static native void setVarmaterialCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2, CoreResourceHandle coreResourceHandle);

    static native void setVarmaterialCoreRenderMeshComponent(long j, CoreRenderMeshComponent coreRenderMeshComponent, long j2, CoreResourceHandle coreResourceHandle);

    static native void setVarmaterialFlagsCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, long j2);

    static native void setVarmaterialsCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData, long j2, CoreResourceArray coreResourceArray);

    static native void setVarmatrixCoreLocalMatrixComponent(long j, CoreLocalMatrixComponent coreLocalMatrixComponent, long j2, CoreMat4X4 coreMat4X4);

    static native void setVarmatrixCoreWorldMatrixComponent(long j, CoreWorldMatrixComponent coreWorldMatrixComponent, long j2, CoreMat4X4 coreMat4X4);

    static native void setVarmaxAabbCoreMinAndMax(long j, CoreMinAndMax coreMinAndMax, long j2, CoreVec3 coreVec3);

    static native void setVarmeshCoreRenderMeshComponent(long j, CoreRenderMeshComponent coreRenderMeshComponent, long j2, CoreResourceHandle coreResourceHandle);

    static native void setVarmeshesCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData, long j2, CoreResourceArray coreResourceArray);

    static native void setVarmetallicFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, float f);

    static native void setVarminAabbCoreMinAndMax(long j, CoreMinAndMax coreMinAndMax, long j2, CoreVec3 coreVec3);

    static native void setVarmorphTargetBufferCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVarmorphTargetByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarmorphTargetCountCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive, long j2);

    static native void setVarmorphTargetCountCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarmorphTargetOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarmorphTargetsCoreMorphComponent(long j, CoreMorphComponent coreMorphComponent, long j2);

    static native void setVarnameCoreAnimationDesc(long j, CoreAnimationDesc coreAnimationDesc, String str);

    static native void setVarnameCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, String str);

    static native void setVarnameCoreNodeComponent(long j, CoreNodeComponent coreNodeComponent, String str);

    static native void setVarnameCoreResourceCreatorCoreInfo(long j, CoreResourceCreator.CoreInfo coreInfo, String str);

    static native void setVarnameCoreResourceManagerCoreResourceInfo(long j, CoreResourceManager.CoreResourceInfo coreResourceInfo, String str);

    static native void setVarnodeCoreRayCastResult(long j, CoreRayCastResult coreRayCastResult, long j2, CoreSceneNode coreSceneNode);

    static native void setVarnodesCoreRenderNodeGraphDesc(long j, CoreRenderNodeGraphDesc coreRenderNodeGraphDesc, long j2, CoreRenderNodeDescArrayView coreRenderNodeDescArrayView);

    static native void setVarnormalCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVarnormalScaleCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, float f);

    static native void setVarorthoHeightCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, float f);

    static native void setVarorthoWidthCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, float f);

    static native void setVarparentCoreNodeComponent(long j, CoreNodeComponent coreNodeComponent, long j2, CoreEntity coreEntity);

    static native void setVarpathCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc, String str);

    static native void setVarpositionCoreTransformComponent(long j, CoreTransformComponent coreTransformComponent, long j2, CoreVec3 coreVec3);

    static native void setVarpreferSrgbFormatCoreSwapchainCreateInfo(long j, CoreSwapchainCreateInfo coreSwapchainCreateInfo, boolean z);

    static native void setVarprimitiveCountCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc, long j2);

    static native void setVarprimitivesCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo, long j2, CoreMeshPrimitiveDescArrayView coreMeshPrimitiveDescArrayView);

    static native void setVarprimitivesCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc, long j2, CoreResourceDataHandle coreResourceDataHandle);

    static native void setVarprojectionCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, long j2, CoreMat4X4 coreMat4X4);

    static native void setVarradianceCubemapCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVarradianceCubemapMipCountCoreSceneComponent(long j, CoreSceneComponent coreSceneComponent, long j2);

    static native void setVarrangeCoreLightComponent(long j, CoreLightComponent coreLightComponent, float f);

    static native void setVarreflectanceCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, float f);

    static native void setVarrenderNodeGraphHandleCoreRenderNodeGraphInput(long j, CoreRenderNodeGraphInput coreRenderNodeGraphInput, long j2, CoreRenderHandle coreRenderHandle);

    static native void setVarrenderNodeGraphNameCoreRenderNodeGraphDesc(long j, CoreRenderNodeGraphDesc coreRenderNodeGraphDesc, String str);

    static native void setVarrenderResolutionCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, long[] jArr);

    static native void setVarrequiredIntegratedMemoryFlagsCoreDeviceConfiguration(long j, CoreDeviceConfiguration coreDeviceConfiguration, long j2);

    static native void setVarrotationCoreTransformComponent(long j, CoreTransformComponent coreTransformComponent, long j2, CoreQuat coreQuat);

    static native void setVarroughnessFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, float f);

    static native void setVarsamplerCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVarsamplersCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData, long j2, CoreGpuResourceArray coreGpuResourceArray);

    static native void setVarscaleCoreTransformComponent(long j, CoreTransformComponent coreTransformComponent, long j2, CoreVec3 coreVec3);

    static native void setVarshadowEnabledCoreLightComponent(long j, CoreLightComponent coreLightComponent, boolean z);

    static native void setVarskinsCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData, long j2, CoreResourceArray coreResourceArray);

    static native void setVarspecularFactorCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, long j2, CoreVec3 coreVec3);

    static native void setVarspecularRadianceCubemapsCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData, long j2, CoreGpuResourceArray coreGpuResourceArray);

    static native void setVarspotInnerAngleCoreLightComponent(long j, CoreLightComponent coreLightComponent, float f);

    static native void setVarspotOuterAngleCoreLightComponent(long j, CoreLightComponent coreLightComponent, float f);

    static native void setVarswapchainFlagsCoreSwapchainCreateInfo(long j, CoreSwapchainCreateInfo coreSwapchainCreateInfo, long j2);

    static native void setVartangentsCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive, boolean z);

    static native void setVartargetDataCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo, long j2, CoreByteArrayView coreByteArrayView);

    static native void setVartexturesCoreGltfResourceData(long j, CoreGltfResourceData coreGltfResourceData, long j2, CoreGpuResourceArray coreGpuResourceArray);

    static native void setVartimestampsCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc, long j2, CoreResourceDataHandle coreResourceDataHandle);

    static native void setVartotalTimeCoreEngineTime(long j, CoreEngineTime coreEngineTime, BigInteger bigInteger);

    static native void setVartrackCountCoreAnimationDesc(long j, CoreAnimationDesc coreAnimationDesc, long j2);

    static native void setVartracksCoreAnimationDesc(long j, CoreAnimationDesc coreAnimationDesc, long j2, CoreResourceDataHandle coreResourceDataHandle);

    static native void setVartypeCoreAnimationTrackDesc(long j, CoreAnimationTrackDesc coreAnimationTrackDesc, int i);

    static native void setVartypeCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, short s);

    static native void setVartypeCoreLightComponent(long j, CoreLightComponent coreLightComponent, short s);

    static native void setVartypeCoreMaterialDesc(long j, CoreMaterialDesc coreMaterialDesc, int i);

    static native void setVartypeCoreResourceHandle(long j, CoreResourceHandle coreResourceHandle, long j2);

    static native void setVaruriCoreResourceCreatorCoreInfo(long j, CoreResourceCreator.CoreInfo coreInfo, String str);

    static native void setVaruriCoreResourceManagerCoreResourceInfo(long j, CoreResourceManager.CoreResourceInfo coreResourceInfo, String str);

    static native void setVarvertexBufferCoreMeshDesc(long j, CoreMeshDesc coreMeshDesc, long j2, CoreGpuResourceHandle coreGpuResourceHandle);

    static native void setVarvertexColorByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexColorOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexCountCoreMeshBuilderCorePrimitive(long j, CoreMeshBuilder.CorePrimitive corePrimitive, long j2);

    static native void setVarvertexCountCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo, long j2);

    static native void setVarvertexCountCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexDataCoreMeshCreateInfo(long j, CoreMeshCreateInfo coreMeshCreateInfo, long j2, CoreByteArrayView coreByteArrayView);

    static native void setVarvertexJointIndexByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexJointIndexOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexJointWeightByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexJointWeightOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexNormalByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexNormalOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexPositionByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexPositionOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexTangentByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexTangentOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexUvByteSizeCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarvertexUvOffsetCoreMeshPrimitiveDesc(long j, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, long j2);

    static native void setVarverticalFovCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, float f);

    static native void setVarviewportCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, float[] fArr);

    static native void setVarwCoreMat4X4(long j, CoreMat4X4 coreMat4X4, long j2, CoreVec4 coreVec4);

    static native void setVarwCoreQuat(long j, CoreQuat coreQuat, float f);

    static native void setVarwCoreVec4(long j, CoreVec4 coreVec4, float f);

    static native void setVarxCoreMat4X4(long j, CoreMat4X4 coreMat4X4, long j2, CoreVec4 coreVec4);

    static native void setVarxCoreQuat(long j, CoreQuat coreQuat, float f);

    static native void setVarxCoreVec2(long j, CoreVec2 coreVec2, float f);

    static native void setVarxCoreVec3(long j, CoreVec3 coreVec3, float f);

    static native void setVarxCoreVec4(long j, CoreVec4 coreVec4, float f);

    static native void setVaryCoreMat4X4(long j, CoreMat4X4 coreMat4X4, long j2, CoreVec4 coreVec4);

    static native void setVaryCoreQuat(long j, CoreQuat coreQuat, float f);

    static native void setVaryCoreVec2(long j, CoreVec2 coreVec2, float f);

    static native void setVaryCoreVec3(long j, CoreVec3 coreVec3, float f);

    static native void setVaryCoreVec4(long j, CoreVec4 coreVec4, float f);

    static native void setVarzCoreMat4X4(long j, CoreMat4X4 coreMat4X4, long j2, CoreVec4 coreVec4);

    static native void setVarzCoreQuat(long j, CoreQuat coreQuat, float f);

    static native void setVarzCoreVec3(long j, CoreVec3 coreVec3, float f);

    static native void setVarzCoreVec4(long j, CoreVec4 coreVec4, float f);

    static native void setVarzfarCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, float f);

    static native void setVarznearCoreCameraComponent(long j, CoreCameraComponent coreCameraComponent, float f);

    static native void setVertexDataInCoreMeshBuilder0(long j, CoreMeshBuilder coreMeshBuilder, long j2, long j3, CoreVec3ArrayView coreVec3ArrayView, long j4, CoreVec3ArrayView coreVec3ArrayView2, long j5, CoreVec2ArrayView coreVec2ArrayView, long j6, CoreVec4ArrayView coreVec4ArrayView, long j7, CoreVec4ArrayView coreVec4ArrayView2);

    static native void setVertexDataInCoreMeshBuilder1(long j, CoreMeshBuilder coreMeshBuilder, long j2, long j3, CoreFloatArrayView coreFloatArrayView, long j4, CoreFloatArrayView coreFloatArrayView2, long j5, CoreFloatArrayView coreFloatArrayView3, long j6, CoreFloatArrayView coreFloatArrayView4, long j7, CoreFloatArrayView coreFloatArrayView5);

    static native void setWeightInCoreAnimationPlayback(long j, CoreAnimationPlayback coreAnimationPlayback, float f);

    static native long sizeInCoreAnimationTrackDescArrayView(long j, CoreAnimationTrackDescArrayView coreAnimationTrackDescArrayView);

    static native long sizeInCoreBufferImageCopyArray(long j, CoreBufferImageCopyArray coreBufferImageCopyArray);

    static native long sizeInCoreBufferImageCopyArrayView(long j, CoreBufferImageCopyArrayView coreBufferImageCopyArrayView);

    static native long sizeInCoreByteArrayView(long j, CoreByteArrayView coreByteArrayView);

    static native long sizeInCoreComponentManagerArray(long j, CoreComponentManagerArray coreComponentManagerArray);

    static native long sizeInCoreEntityArray(long j, CoreEntityArray coreEntityArray);

    static native long sizeInCoreEntityArrayView(long j, CoreEntityArrayView coreEntityArrayView);

    static native long sizeInCoreFloatArray(long j, CoreFloatArray coreFloatArray);

    static native long sizeInCoreFloatArrayView(long j, CoreFloatArrayView coreFloatArrayView);

    static native long sizeInCoreMeshPrimitiveDescArray(long j, CoreMeshPrimitiveDescArray coreMeshPrimitiveDescArray);

    static native long sizeInCoreMeshPrimitiveDescArrayView(long j, CoreMeshPrimitiveDescArrayView coreMeshPrimitiveDescArrayView);

    static native long sizeInCoreRenderNodeDescArrayView(long j, CoreRenderNodeDescArrayView coreRenderNodeDescArrayView);

    static native long sizeInCoreRenderNodeGraphInputArrayView(long j, CoreRenderNodeGraphInputArrayView coreRenderNodeGraphInputArrayView);

    static native long sizeInCoreSceneNodeArrayView(long j, CoreSceneNodeArrayView coreSceneNodeArrayView);

    static native long sizeInCoreStringArrayView(long j, CoreStringArrayView coreStringArrayView);

    static native long sizeInCoreStringViewArrayView(long j, CoreStringViewArrayView coreStringViewArrayView);

    static native long sizeInCoreSystemArray(long j, CoreSystemArray coreSystemArray);

    static native long sizeInCoreVec2ArrayView(long j, CoreVec2ArrayView coreVec2ArrayView);

    static native long sizeInCoreVec3Array(long j, CoreVec3Array coreVec3Array);

    static native long sizeInCoreVec3ArrayView(long j, CoreVec3ArrayView coreVec3ArrayView);

    static native long sizeInCoreVec4Array(long j, CoreVec4Array coreVec4Array);

    static native long sizeInCoreVec4ArrayView(long j, CoreVec4ArrayView coreVec4ArrayView);

    static native boolean tickFrame(long j, CoreEngine coreEngine, long j2, CoreEcs coreEcs);

    static native void uninitializeInCoreEcs(long j, CoreEcs coreEcs);

    static native void uninitializeInCoreSystem(long j, CoreSystem coreSystem);

    static native void unregisterAssetManager(long j, CoreFileManager coreFileManager, String str);

    static native boolean updateInCoreEcs(long j, CoreEcs coreEcs, BigInteger bigInteger, BigInteger bigInteger2);

    static native boolean updateInCoreSystem(long j, CoreSystem coreSystem, boolean z, BigInteger bigInteger, BigInteger bigInteger2);

    static native void waitForIdleAndDestroyGpuResourcesInCoreGpuResourceManager(long j, CoreGpuResourceManager coreGpuResourceManager);

    static native boolean wantCaptureMouseInCoreDevGui(long j, CoreDevGui coreDevGui);

    static native long worldToScreen(long j, CoreEcs coreEcs, long j2, CoreEntity coreEntity, long j3, CoreVec3 coreVec3);

    private CoreJni() {
    }
}
