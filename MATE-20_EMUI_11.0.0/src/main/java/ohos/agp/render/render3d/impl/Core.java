package ohos.agp.render.render3d.impl;

import java.math.BigInteger;
import ohos.agp.render.render3d.impl.CoreDeviceCreateInfo;

/* access modifiers changed from: package-private */
public class Core {
    private Core() {
    }

    static BigInteger getInvalidGpuResourceHandle() {
        return CoreJni.invalidGpuResourceHandleAgpget();
    }

    static long getInvalidEntity() {
        return CoreJni.invalidEntityAgpget();
    }

    static boolean isValid(CoreEntity coreEntity) {
        return CoreJni.isValid(CoreEntity.getCptr(coreEntity), coreEntity);
    }

    static CoreEcs getEcsInstance(CoreResourceManager coreResourceManager, CoreDevGui coreDevGui) {
        long ecsInstance = CoreJni.getEcsInstance(CoreResourceManager.getCptr(coreResourceManager), coreResourceManager, CoreDevGui.getCptr(coreDevGui), coreDevGui);
        if (ecsInstance == 0) {
            return null;
        }
        return new CoreEcs(ecsInstance, false);
    }

    static void destroyEcsInstance(CoreEcs coreEcs) {
        CoreJni.destroyEcsInstance(CoreEcs.getCptr(coreEcs), coreEcs);
    }

    static CoreEngine createEngine(CoreEngineCreateInfo coreEngineCreateInfo) {
        long createEngine = CoreJni.createEngine(CoreEngineCreateInfo.getCptr(coreEngineCreateInfo), coreEngineCreateInfo);
        if (createEngine == 0) {
            return null;
        }
        return new CoreEngine(createEngine, true);
    }

    static String getName(CoreCameraComponentManager coreCameraComponentManager) {
        return CoreJni.getName0(CoreCameraComponentManager.getCptr(coreCameraComponentManager), coreCameraComponentManager);
    }

    static String getName(CoreLightComponentManager coreLightComponentManager) {
        return CoreJni.getName1(CoreLightComponentManager.getCptr(coreLightComponentManager), coreLightComponentManager);
    }

    static String getName(CoreLocalMatrixComponentManager coreLocalMatrixComponentManager) {
        return CoreJni.getName2(CoreLocalMatrixComponentManager.getCptr(coreLocalMatrixComponentManager), coreLocalMatrixComponentManager);
    }

    static String getName(CoreNodeComponentManager coreNodeComponentManager) {
        return CoreJni.getName3(CoreNodeComponentManager.getCptr(coreNodeComponentManager), coreNodeComponentManager);
    }

    static String getName(CoreRenderMeshComponentManager coreRenderMeshComponentManager) {
        return CoreJni.getName4(CoreRenderMeshComponentManager.getCptr(coreRenderMeshComponentManager), coreRenderMeshComponentManager);
    }

    static String getName(CoreSceneComponentManager coreSceneComponentManager) {
        return CoreJni.getName5(CoreSceneComponentManager.getCptr(coreSceneComponentManager), coreSceneComponentManager);
    }

    static String getName(CoreTransformComponentManager coreTransformComponentManager) {
        return CoreJni.getName6(CoreTransformComponentManager.getCptr(coreTransformComponentManager), coreTransformComponentManager);
    }

    static String getName(CoreWorldMatrixComponentManager coreWorldMatrixComponentManager) {
        return CoreJni.getName7(CoreWorldMatrixComponentManager.getCptr(coreWorldMatrixComponentManager), coreWorldMatrixComponentManager);
    }

    static String getName(CoreMorphComponentManager coreMorphComponentManager) {
        return CoreJni.getName8(CoreMorphComponentManager.getCptr(coreMorphComponentManager), coreMorphComponentManager);
    }

    static String getName(CoreNodeSystem coreNodeSystem) {
        return CoreJni.getName9(CoreNodeSystem.getCptr(coreNodeSystem), coreNodeSystem);
    }

    static long getAnimationRepeatCountInfinite() {
        return CoreJni.animationRepeatCountInfiniteAgpget();
    }

    static String getName(CoreAnimationSystem coreAnimationSystem) {
        return CoreJni.getName10(CoreAnimationSystem.getCptr(coreAnimationSystem), coreAnimationSystem);
    }

    static String getName(CoreMorphingSystem coreMorphingSystem) {
        return CoreJni.getName11(CoreMorphingSystem.getCptr(coreMorphingSystem), coreMorphingSystem);
    }

    static long getMaxAnimationPathLength() {
        return CoreJni.maxAnimationPathLengthAgpget();
    }

    static long getMaxNodeNameLength() {
        return CoreJni.maxNodeNameLengthAgpget();
    }

    static CoreResourceCreator createResourceCreator(CoreEngine coreEngine, CoreEcs coreEcs) {
        long createResourceCreator = CoreJni.createResourceCreator(CoreEngine.getCptr(coreEngine), coreEngine, CoreEcs.getCptr(coreEcs), coreEcs);
        if (createResourceCreator == 0) {
            return null;
        }
        return new CoreResourceCreator(createResourceCreator, false);
    }

    static long getGltfInvalidIndex() {
        return CoreJni.gltfInvalidIndexAgpget();
    }

    static CoreGltfLoadResult loadGltf(CoreFileManager coreFileManager, String str) {
        return new CoreGltfLoadResult(CoreJni.loadGltf0(CoreFileManager.getCptr(coreFileManager), coreFileManager, str), true);
    }

    static CoreGltfLoadResult loadGltf(CoreFileManager coreFileManager, CoreByteArrayView coreByteArrayView) {
        return new CoreGltfLoadResult(CoreJni.loadGltf1(CoreFileManager.getCptr(coreFileManager), coreFileManager, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView), true);
    }

    static boolean saveGltf(CoreEngine coreEngine, CoreEcs coreEcs, CoreResourceCreator coreResourceCreator, String str) {
        return CoreJni.saveGltf(CoreEngine.getCptr(coreEngine), coreEngine, CoreEcs.getCptr(coreEcs), coreEcs, CoreResourceCreator.getCptr(coreResourceCreator), coreResourceCreator, str);
    }

    static CoreEntity importGltfScene(long j, CoreGltfData coreGltfData, CoreGltfResourceData coreGltfResourceData, CoreEcs coreEcs, CoreEntity coreEntity, long j2) {
        return new CoreEntity(CoreJni.importGltfScene0(j, CoreGltfData.getCptr(coreGltfData), coreGltfData, CoreGltfResourceData.getCptr(coreGltfResourceData), coreGltfResourceData, CoreEcs.getCptr(coreEcs), coreEcs, CoreEntity.getCptr(coreEntity), coreEntity, j2), true);
    }

    static CoreEntity importGltfScene(long j, CoreGltfData coreGltfData, CoreGltfResourceData coreGltfResourceData, CoreEcs coreEcs, CoreEntity coreEntity) {
        return new CoreEntity(CoreJni.importGltfScene1(j, CoreGltfData.getCptr(coreGltfData), coreGltfData, CoreGltfResourceData.getCptr(coreGltfResourceData), coreGltfResourceData, CoreEcs.getCptr(coreEcs), coreEcs, CoreEntity.getCptr(coreEntity), coreEntity), true);
    }

    static CoreEntity importGltfScene(long j, CoreGltfData coreGltfData, CoreGltfResourceData coreGltfResourceData, CoreEcs coreEcs) {
        return new CoreEntity(CoreJni.importGltfScene2(j, CoreGltfData.getCptr(coreGltfData), coreGltfData, CoreGltfResourceData.getCptr(coreGltfResourceData), coreGltfResourceData, CoreEcs.getCptr(coreEcs), coreEcs), true);
    }

    static void releaseGltfResources(CoreGltfResourceData coreGltfResourceData, CoreEngine coreEngine, CoreResourceCreator coreResourceCreator) {
        CoreJni.releaseGltfResources(CoreGltfResourceData.getCptr(coreGltfResourceData), coreGltfResourceData, CoreEngine.getCptr(coreEngine), coreEngine, CoreResourceCreator.getCptr(coreResourceCreator), coreResourceCreator);
    }

    static CoreVec3 screenToWorld(CoreEcs coreEcs, CoreEntity coreEntity, CoreVec3 coreVec3) {
        return new CoreVec3(CoreJni.screenToWorld(CoreEcs.getCptr(coreEcs), coreEcs, CoreEntity.getCptr(coreEntity), coreEntity, CoreVec3.getCptr(coreVec3), coreVec3), true);
    }

    static CoreVec3 worldToScreen(CoreEcs coreEcs, CoreEntity coreEntity, CoreVec3 coreVec3) {
        return new CoreVec3(CoreJni.worldToScreen(CoreEcs.getCptr(coreEcs), coreEcs, CoreEntity.getCptr(coreEntity), coreEntity, CoreVec3.getCptr(coreVec3), coreVec3), true);
    }

    static CoreMinAndMax getWorldAabb(CoreMat4X4 coreMat4X4, CoreVec3 coreVec3, CoreVec3 coreVec32) {
        return new CoreMinAndMax(CoreJni.getWorldAabb(CoreMat4X4.getCptr(coreMat4X4), coreMat4X4, CoreVec3.getCptr(coreVec3), coreVec3, CoreVec3.getCptr(coreVec32), coreVec32), true);
    }

    static CoreMinAndMax getWorldMatrixComponentAabb(CoreEntity coreEntity, boolean z, CoreEcs coreEcs) {
        return new CoreMinAndMax(CoreJni.getWorldMatrixComponentAabb(CoreEntity.getCptr(coreEntity), coreEntity, z, CoreEcs.getCptr(coreEcs), coreEcs), true);
    }

    static CoreMinAndMax getTransformComponentAabb(CoreEntity coreEntity, boolean z, CoreEcs coreEcs) {
        return new CoreMinAndMax(CoreJni.getTransformComponentAabb(CoreEntity.getCptr(coreEntity), coreEntity, z, CoreEcs.getCptr(coreEcs), coreEcs), true);
    }

    static CoreRayCastResultArray rayCast(CoreEcs coreEcs, CoreVec3 coreVec3, CoreVec3 coreVec32) {
        return new CoreRayCastResultArray(CoreJni.rayCast(CoreEcs.getCptr(coreEcs), coreEcs, CoreVec3.getCptr(coreVec3), coreVec3, CoreVec3.getCptr(coreVec32), coreVec32), true);
    }

    static CoreRayCastResultArray rayCastFromCamera(CoreEcs coreEcs, CoreEntity coreEntity, CoreVec2 coreVec2) {
        return new CoreRayCastResultArray(CoreJni.rayCastFromCamera(CoreEcs.getCptr(coreEcs), coreEcs, CoreEntity.getCptr(coreEntity), coreEntity, CoreVec2.getCptr(coreVec2), coreVec2), true);
    }

    static CoreResourceHandle generateCubeMesh(CoreResourceCreator coreResourceCreator, String str, CoreResourceHandle coreResourceHandle, float f, float f2, float f3) {
        return new CoreResourceHandle(CoreJni.generateCubeMesh(CoreResourceCreator.getCptr(coreResourceCreator), coreResourceCreator, str, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle, f, f2, f3), true);
    }

    static CoreResourceHandle generatePlaneMesh(CoreResourceCreator coreResourceCreator, String str, CoreResourceHandle coreResourceHandle, float f, float f2) {
        return new CoreResourceHandle(CoreJni.generatePlaneMesh(CoreResourceCreator.getCptr(coreResourceCreator), coreResourceCreator, str, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle, f, f2), true);
    }

    static CoreResourceHandle generateSphereMesh(CoreResourceCreator coreResourceCreator, String str, CoreResourceHandle coreResourceHandle, float f, long j, long j2) {
        return new CoreResourceHandle(CoreJni.generateSphereMesh(CoreResourceCreator.getCptr(coreResourceCreator), coreResourceCreator, str, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle, f, j, j2), true);
    }

    static CoreResourceHandle generateConeMesh(CoreResourceCreator coreResourceCreator, String str, CoreResourceHandle coreResourceHandle, float f, float f2, long j) {
        return new CoreResourceHandle(CoreJni.generateConeMesh(CoreResourceCreator.getCptr(coreResourceCreator), coreResourceCreator, str, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle, f, f2, j), true);
    }

    static CoreEntity generateCube(CoreEcs coreEcs, CoreResourceCreator coreResourceCreator, String str, CoreResourceHandle coreResourceHandle, float f, float f2, float f3) {
        return new CoreEntity(CoreJni.generateCube(CoreEcs.getCptr(coreEcs), coreEcs, CoreResourceCreator.getCptr(coreResourceCreator), coreResourceCreator, str, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle, f, f2, f3), true);
    }

    static CoreEntity generatePlane(CoreEcs coreEcs, CoreResourceCreator coreResourceCreator, String str, CoreResourceHandle coreResourceHandle, float f, float f2) {
        return new CoreEntity(CoreJni.generatePlane(CoreEcs.getCptr(coreEcs), coreEcs, CoreResourceCreator.getCptr(coreResourceCreator), coreResourceCreator, str, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle, f, f2), true);
    }

    static CoreEntity generateSphere(CoreEcs coreEcs, CoreResourceCreator coreResourceCreator, String str, CoreResourceHandle coreResourceHandle, float f, long j, long j2) {
        return new CoreEntity(CoreJni.generateSphere(CoreEcs.getCptr(coreEcs), coreEcs, CoreResourceCreator.getCptr(coreResourceCreator), coreResourceCreator, str, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle, f, j, j2), true);
    }

    static CoreEntity generateCone(CoreEcs coreEcs, CoreResourceCreator coreResourceCreator, String str, CoreResourceHandle coreResourceHandle, float f, float f2, long j) {
        return new CoreEntity(CoreJni.generateCone(CoreEcs.getCptr(coreEcs), coreEcs, CoreResourceCreator.getCptr(coreResourceCreator), coreResourceCreator, str, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle, f, f2, j), true);
    }

    static CorePlatform createAndroidPlatform(Object obj) {
        long createAndroidPlatform = CoreJni.createAndroidPlatform(obj);
        if (createAndroidPlatform == 0) {
            return null;
        }
        return new CorePlatform(createAndroidPlatform, false);
    }

    static CoreNativeWindow createAndroidNativeWindow(CorePlatform corePlatform, long j) {
        return new CoreNativeWindow(CoreJni.createAndroidNativeWindow(CorePlatform.getCptr(corePlatform), corePlatform, j), true);
    }

    static void destroyAndroidNativeWindow(CoreNativeWindow coreNativeWindow) {
        CoreJni.destroyAndroidNativeWindow(CoreNativeWindow.getCptr(coreNativeWindow), coreNativeWindow);
    }

    static BigInteger createAndroidSurface(CoreDevice coreDevice, CoreNativeWindow coreNativeWindow) {
        return CoreJni.createAndroidSurface(CoreDevice.getCptr(coreDevice), coreDevice, CoreNativeWindow.getCptr(coreNativeWindow), coreNativeWindow);
    }

    static void destroyAndroidSurface(CoreDevice coreDevice, BigInteger bigInteger) {
        CoreJni.destroyAndroidSurface(CoreDevice.getCptr(coreDevice), coreDevice, bigInteger);
    }

    static CoreDevice createDevice(CoreEngine coreEngine, CoreDeviceCreateInfo.CoreBackend coreBackend, long j, long j2, int i, int i2, int i3, int i4) {
        long createDevice = CoreJni.createDevice(CoreEngine.getCptr(coreEngine), coreEngine, coreBackend.swigValue(), j, j2, i, i2, i3, i4);
        if (createDevice == 0) {
            return null;
        }
        return new CoreDevice(createDevice, false);
    }

    static void registerAssetManager(CoreFileManager coreFileManager, String str, Object obj) {
        CoreJni.registerAssetManager(CoreFileManager.getCptr(coreFileManager), coreFileManager, str, obj);
    }

    static void unregisterAssetManager(CoreFileManager coreFileManager, String str) {
        CoreJni.unregisterAssetManager(CoreFileManager.getCptr(coreFileManager), coreFileManager, str);
    }

    static boolean tickFrame(CoreEngine coreEngine, CoreEcs coreEcs) {
        return CoreJni.tickFrame(CoreEngine.getCptr(coreEngine), coreEngine, CoreEcs.getCptr(coreEcs), coreEcs);
    }

    static CoreSystemGraphLoader createSystemGraphLoader() {
        long createSystemGraphLoader = CoreJni.createSystemGraphLoader();
        if (createSystemGraphLoader == 0) {
            return null;
        }
        return new CoreSystemGraphLoader(createSystemGraphLoader, false);
    }

    static CoreGltf2Importer createGltf2Importer(CoreEngine coreEngine, CoreResourceCreator coreResourceCreator) {
        long createGltf2Importer = CoreJni.createGltf2Importer(CoreEngine.getCptr(coreEngine), coreEngine, CoreResourceCreator.getCptr(coreResourceCreator), coreResourceCreator);
        if (createGltf2Importer == 0) {
            return null;
        }
        return new CoreGltf2Importer(createGltf2Importer, false);
    }

    static CoreMeshBuilder createMeshBuilder(CoreShaderManager coreShaderManager, int i) {
        long createMeshBuilder = CoreJni.createMeshBuilder(CoreShaderManager.getCptr(coreShaderManager), coreShaderManager, i);
        if (createMeshBuilder == 0) {
            return null;
        }
        return new CoreMeshBuilder(createMeshBuilder, false);
    }

    static CoreRenderDataStorePod createRenderDataStorePod(CoreEngine coreEngine, String str) {
        long createRenderDataStorePod = CoreJni.createRenderDataStorePod(CoreEngine.getCptr(coreEngine), coreEngine, str);
        if (createRenderDataStorePod == 0) {
            return null;
        }
        return new CoreRenderDataStorePod(createRenderDataStorePod, false);
    }

    static void destroyRenderDataStorePod(CoreEngine coreEngine, CoreRenderDataStorePod coreRenderDataStorePod) {
        CoreJni.destroyRenderDataStorePod(CoreEngine.getCptr(coreEngine), coreEngine, CoreRenderDataStorePod.getCptr(coreRenderDataStorePod), coreRenderDataStorePod);
    }

    static CoreRenderDataStorePod getRenderDataStorePod(CoreEngine coreEngine, String str) {
        long renderDataStorePod = CoreJni.getRenderDataStorePod(CoreEngine.getCptr(coreEngine), coreEngine, str);
        if (renderDataStorePod == 0) {
            return null;
        }
        return new CoreRenderDataStorePod(renderDataStorePod, false);
    }

    static void createPod(CoreRenderDataStorePod coreRenderDataStorePod, String str, String str2, CoreByteArrayView coreByteArrayView) {
        CoreJni.createPod(CoreRenderDataStorePod.getCptr(coreRenderDataStorePod), coreRenderDataStorePod, str, str2, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView);
    }

    static void destroyPod(CoreRenderDataStorePod coreRenderDataStorePod, String str, String str2) {
        CoreJni.destroyPod(CoreRenderDataStorePod.getCptr(coreRenderDataStorePod), coreRenderDataStorePod, str, str2);
    }

    static void setPod(CoreRenderDataStorePod coreRenderDataStorePod, String str, CoreByteArrayView coreByteArrayView) {
        CoreJni.setPod(CoreRenderDataStorePod.getCptr(coreRenderDataStorePod), coreRenderDataStorePod, str, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView);
    }

    static CoreByteArrayView getPod(CoreRenderDataStorePod coreRenderDataStorePod, String str) {
        return new CoreByteArrayView(CoreJni.getPod(CoreRenderDataStorePod.getCptr(coreRenderDataStorePod), coreRenderDataStorePod, str), true);
    }

    static CoreGpuResourceHandle createUniformRingBuffer(CoreEngine coreEngine, String str, long j) {
        return new CoreGpuResourceHandle(CoreJni.createUniformRingBuffer(CoreEngine.getCptr(coreEngine), coreEngine, str, j), true);
    }

    static void copyDataToBufferOnCpu(CoreEngine coreEngine, CoreByteArrayView coreByteArrayView, CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.copyDataToBufferOnCpu(CoreEngine.getCptr(coreEngine), coreEngine, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    static CoreGpuResourceHandle createImage(CoreEngine coreEngine, String str, long j, long j2, CoreFormat coreFormat) {
        return new CoreGpuResourceHandle(CoreJni.createImage0(CoreEngine.getCptr(coreEngine), coreEngine, str, j, j2, coreFormat.swigValue()), true);
    }

    static CoreGpuResourceHandle createImage(CoreEngine coreEngine, String str, String str2) {
        return new CoreGpuResourceHandle(CoreJni.createImage1(CoreEngine.getCptr(coreEngine), coreEngine, str, str2), true);
    }

    static void copyDataToImage(CoreEngine coreEngine, CoreByteArrayView coreByteArrayView, CoreGpuResourceHandle coreGpuResourceHandle, long j, long j2) {
        CoreJni.copyDataToImage(CoreEngine.getCptr(coreEngine), coreEngine, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle, j, j2);
    }

    static void setSceneIrradianceCoefficient(CoreSceneComponent coreSceneComponent, CoreVec3 coreVec3, int i) {
        CoreJni.setSceneIrradianceCoefficient(CoreSceneComponent.getCptr(coreSceneComponent), coreSceneComponent, CoreVec3.getCptr(coreVec3), coreVec3, i);
    }

    static CoreVec3ArrayView getSceneIrradianceCoefficients(CoreSceneComponent coreSceneComponent) {
        return new CoreVec3ArrayView(CoreJni.getSceneIrradianceCoefficients(CoreSceneComponent.getCptr(coreSceneComponent), coreSceneComponent), true);
    }

    static CoreRenderNodeGraphLoader createRenderNodeGraphLoader() {
        long createRenderNodeGraphLoader = CoreJni.createRenderNodeGraphLoader();
        if (createRenderNodeGraphLoader == 0) {
            return null;
        }
        return new CoreRenderNodeGraphLoader(createRenderNodeGraphLoader, false);
    }

    static CoreGpuResourceHandle createTextureViewOes(CoreEngine coreEngine, String str, long j, long j2, long j3) {
        return new CoreGpuResourceHandle(CoreJni.createTextureViewOes(CoreEngine.getCptr(coreEngine), coreEngine, str, j, j2, j3), true);
    }
}
