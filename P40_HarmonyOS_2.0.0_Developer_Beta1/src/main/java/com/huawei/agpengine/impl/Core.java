package com.huawei.agpengine.impl;

import com.huawei.agpengine.impl.CoreDeviceCreateInfo;
import java.math.BigInteger;

class Core {
    private Core() {
    }

    static BigInteger getInvalidGpuResourceHandle() {
        return CoreJni.invalidGpuResourceHandleAgpget();
    }

    static long getInvalidEntity() {
        return CoreJni.invalidEntityAgpget();
    }

    static boolean isEntityValid(int entity) {
        return CoreJni.isEntityValid(entity);
    }

    static CorePluginRegister getPluginRegister() {
        return new CorePluginRegister(CoreJni.getPluginRegister(), false);
    }

    static String getVersion() {
        return CoreJni.getVersion();
    }

    static boolean isDebugBuild() {
        return CoreJni.isDebugBuild();
    }

    static boolean isGpuResourceHandleValid(long handle) {
        return CoreJni.isGpuResourceHandleValid(handle);
    }

    static boolean isRenderHandleValid(long handle) {
        return CoreJni.isRenderHandleValid(handle);
    }

    static long getAnimationRepeatCountInfinite() {
        return CoreJni.animationRepeatCountInfiniteAgpget();
    }

    static long getMaxAnimationPathLength() {
        return CoreJni.maxAnimationPathLengthAgpget();
    }

    static long getMaxNodeNameLength() {
        return CoreJni.maxNodeNameLengthAgpget();
    }

    static long getGltfInvalidIndex() {
        return CoreJni.gltfInvalidIndexAgpget();
    }

    static CoreEnginePtr createEngine(Object androidContext, CoreVersionInfo applicationVersion) {
        return new CoreEnginePtr(CoreJni.createEngine(androidContext, CoreVersionInfo.getCptr(applicationVersion), applicationVersion), true);
    }

    static CoreNativeWindow createAndroidNativeWindow(CorePlatform platform, Object surface) {
        return new CoreNativeWindow(CoreJni.createAndroidNativeWindow(CorePlatform.getCptr(platform), platform, surface), true);
    }

    static void destroyAndroidNativeWindow(CoreNativeWindow nativeWindow) {
        CoreJni.destroyAndroidNativeWindow(CoreNativeWindow.getCptr(nativeWindow), nativeWindow);
    }

    static boolean isSrgbSurfaceSupported(CoreDevice device) {
        return CoreJni.isSrgbSurfaceSupported(CoreDevice.getCptr(device), device);
    }

    static BigInteger createAndroidSurface(CoreDevice device, CoreNativeWindow nativeWindow) {
        return CoreJni.createAndroidSurface(CoreDevice.getCptr(device), device, CoreNativeWindow.getCptr(nativeWindow), nativeWindow);
    }

    static void destroyAndroidSurface(CoreDevice device, BigInteger surfaceHandle) {
        CoreJni.destroyAndroidSurface(CoreDevice.getCptr(device), device, surfaceHandle);
    }

    static CoreDevice createDevice(CoreEngine engine, CoreDeviceCreateInfo.CoreBackend backend, long applicationContext, long sharedContext, int msaaSampleCount, int depthBufferBits, int alphaBits, int stencilBits) {
        long cptr = CoreJni.createDevice(CoreEngine.getCptr(engine), engine, backend.swigValue(), applicationContext, sharedContext, msaaSampleCount, depthBufferBits, alphaBits, stencilBits);
        if (cptr == 0) {
            return null;
        }
        return new CoreDevice(cptr, false);
    }

    static void registerApkFilesystem(CoreFileManager fileManager, String protocol, Object assetManager) {
        CoreJni.registerApkFilesystem(CoreFileManager.getCptr(fileManager), fileManager, protocol, assetManager);
    }

    static void unregisterFilesystem(CoreFileManager fileManager, String protocol) {
        CoreJni.unregisterFilesystem(CoreFileManager.getCptr(fileManager), fileManager, protocol);
    }

    static boolean registerPath(CoreFileManager fileManager, String protocol, String uri, boolean prepend) {
        return CoreJni.registerPath(CoreFileManager.getCptr(fileManager), fileManager, protocol, uri, prepend);
    }

    static void unregisterPath(CoreFileManager fileManager, String protocol, String uri) {
        CoreJni.unregisterPath(CoreFileManager.getCptr(fileManager), fileManager, protocol, uri);
    }

    static boolean getPropertyData(CorePropertyHandle propertyHandle, CoreWriteableByteArrayView dataOut) {
        return CoreJni.getPropertyData(CorePropertyHandle.getCptr(propertyHandle), propertyHandle, CoreWriteableByteArrayView.getCptr(dataOut), dataOut);
    }

    static boolean setPropertyData(CorePropertyHandle propertyHandle, CoreByteArrayView data) {
        return CoreJni.setPropertyData(CorePropertyHandle.getCptr(propertyHandle), propertyHandle, CoreByteArrayView.getCptr(data), data);
    }

    static boolean tickFrame(CoreEngine engine, CoreEcs ecs) {
        return CoreJni.tickFrame(CoreEngine.getCptr(engine), engine, CoreEcs.getCptr(ecs), ecs);
    }

    static CoreSystemGraphLoaderPtr createSystemGraphLoader(CoreFileManager fileManager) {
        return new CoreSystemGraphLoaderPtr(CoreJni.createSystemGraphLoader(CoreFileManager.getCptr(fileManager), fileManager), true);
    }

    static CoreMeshBuilderPtr createMeshBuilder(CoreShaderManager shaderManager, int primitiveCount) {
        return new CoreMeshBuilderPtr(CoreJni.createMeshBuilder(CoreShaderManager.getCptr(shaderManager), shaderManager, primitiveCount), true);
    }

    static CoreRenderDataStorePod createRenderDataStorePod(CoreEngine engine, String name) {
        long cptr = CoreJni.createRenderDataStorePod(CoreEngine.getCptr(engine), engine, name);
        if (cptr == 0) {
            return null;
        }
        return new CoreRenderDataStorePod(cptr, false);
    }

    static void destroyRenderDataStorePod(CoreEngine engine, CoreRenderDataStorePod dataStore) {
        CoreJni.destroyRenderDataStorePod(CoreEngine.getCptr(engine), engine, CoreRenderDataStorePod.getCptr(dataStore), dataStore);
    }

    static CoreRenderDataStorePod getRenderDataStorePod(CoreEngine engine, String name) {
        long cptr = CoreJni.getRenderDataStorePod(CoreEngine.getCptr(engine), engine, name);
        if (cptr == 0) {
            return null;
        }
        return new CoreRenderDataStorePod(cptr, false);
    }

    static void createPod(CoreRenderDataStorePod dataStore, String typeName, String name, CoreByteArrayView data) {
        CoreJni.createPod(CoreRenderDataStorePod.getCptr(dataStore), dataStore, typeName, name, CoreByteArrayView.getCptr(data), data);
    }

    static void createShaderSpecializationRenderPod(CoreRenderDataStorePod dataStore, String typeName, String name, CoreShaderSpecializationRenderPod shaderSpecialization) {
        CoreJni.createShaderSpecializationRenderPod(CoreRenderDataStorePod.getCptr(dataStore), dataStore, typeName, name, CoreShaderSpecializationRenderPod.getCptr(shaderSpecialization), shaderSpecialization);
    }

    static void destroyPod(CoreRenderDataStorePod dataStore, String typeName, String name) {
        CoreJni.destroyPod(CoreRenderDataStorePod.getCptr(dataStore), dataStore, typeName, name);
    }

    static void setPod(CoreRenderDataStorePod dataStore, String name, CoreByteArrayView data) {
        CoreJni.setPod(CoreRenderDataStorePod.getCptr(dataStore), dataStore, name, CoreByteArrayView.getCptr(data), data);
    }

    static void setShaderSpecializationRenderPod(CoreRenderDataStorePod dataStore, String name, CoreShaderSpecializationRenderPod shaderSpecialization) {
        CoreJni.setShaderSpecializationRenderPod(CoreRenderDataStorePod.getCptr(dataStore), dataStore, name, CoreShaderSpecializationRenderPod.getCptr(shaderSpecialization), shaderSpecialization);
    }

    static CoreByteArrayView getPod(CoreRenderDataStorePod dataStore, String name) {
        return new CoreByteArrayView(CoreJni.getPod(CoreRenderDataStorePod.getCptr(dataStore), dataStore, name), true);
    }

    static long createUniformRingBuffer(CoreEngine engine, String name, long byteSize) {
        return CoreJni.createUniformRingBuffer(CoreEngine.getCptr(engine), engine, name, byteSize);
    }

    static void copyDataToBufferOnCpu(CoreEngine engine, CoreByteArrayView data, long dstHandle) {
        CoreJni.copyDataToBufferOnCpu(CoreEngine.getCptr(engine), engine, CoreByteArrayView.getCptr(data), data, dstHandle);
    }

    static long createImage(CoreEngine engine, String name, long width, long height, CoreFormat format) {
        return CoreJni.createImage0(CoreEngine.getCptr(engine), engine, name, width, height, format.swigValue());
    }

    static long createImage(CoreEngine engine, String name, String imageUri, int flags) {
        return CoreJni.createImage1(CoreEngine.getCptr(engine), engine, name, imageUri, flags);
    }

    static void copyDataToImage(CoreEngine engine, CoreByteArrayView data, long dstHandle, long width, long height) {
        CoreJni.copyDataToImage(CoreEngine.getCptr(engine), engine, CoreByteArrayView.getCptr(data), data, dstHandle, width, height);
    }

    static void setSceneIrradianceCoefficient(CoreSceneComponent scenecomponent, CoreVec3 irradianceCoefficient, int index) {
        CoreJni.setSceneIrradianceCoefficient(CoreSceneComponent.getCptr(scenecomponent), scenecomponent, CoreVec3.getCptr(irradianceCoefficient), irradianceCoefficient, index);
    }

    static CoreVec3ArrayView getSceneIrradianceCoefficients(CoreSceneComponent scenecomponent) {
        return new CoreVec3ArrayView(CoreJni.getSceneIrradianceCoefficients(CoreSceneComponent.getCptr(scenecomponent), scenecomponent), true);
    }

    static long createTextureViewOes(CoreEngine engine, String name, long width, long height, long texId) {
        return CoreJni.createTextureViewOes(CoreEngine.getCptr(engine), engine, name, width, height, texId);
    }

    static long createImageViewHwBuffer(CoreEngine engine, String name, Object hwBuffer) {
        return CoreJni.createImageViewHwBuffer(CoreEngine.getCptr(engine), engine, name, hwBuffer);
    }

    static long getShaderHandle(CoreEngine engine, String name) {
        return CoreJni.getShaderHandle(CoreEngine.getCptr(engine), engine, name);
    }

    static long createColorTargetGpuImage(CoreEngine engine, String name, long width, long height) {
        return CoreJni.createColorTargetGpuImage(CoreEngine.getCptr(engine), engine, name, width, height);
    }

    static long createDepthTargetGpuImage(CoreEngine engine, String name, long width, long height) {
        return CoreJni.createDepthTargetGpuImage(CoreEngine.getCptr(engine), engine, name, width, height);
    }
}
