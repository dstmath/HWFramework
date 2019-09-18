package com.huawei.agp;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Surface;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

public class RenderEngine {
    private static final String TAG = "RenderEngine";
    private static boolean sPreloaded = false;

    private static native int nativeAGPAllocateCommandBuffer();

    private static native long nativeAGPAllocateGPUMemory(int i);

    private static native void nativeAGPBeginRenderToTxture(int i);

    private static native void nativeAGPBindIndexBuffer(int i, int i2, String str);

    private static native void nativeAGPBindVertexBuffers(int[] iArr, int i, int i2, int[] iArr2);

    private static native void nativeAGPCommandBufferBegin(int i);

    private static native int nativeAGPCreateBuffer(int i, String str);

    private static native int nativeAGPCreateGraphicProgram(boolean z, String str, String str2, String str3, String str4, AssetManager assetManager);

    private static native int nativeAGPCreateSwapChain(Surface surface, String[] strArr, String[] strArr2);

    private static native void nativeAGPDraw(int i, int i2, int i3, int i4);

    private static native void nativeAGPDrawIndexed(int i, int i2, int i3, int i4, int i5);

    private static native void nativeAGPDrawIndirect(int i, int i2, int i3, int i4);

    private static native void nativeAGPEndRenderToTxture(int i);

    private static native void nativeAGPFreeCommandBuffer(int i);

    private static native void nativeAGPFreeGPUMemory(long j);

    private static native int nativeAGPLoad2DImage(int i, long j, int i2);

    private static native void nativeAGPLoadBufferByte(int i, int i2, byte[] bArr, int i3, int i4);

    private static native void nativeAGPLoadBufferDirectByte(int i, int i2, ByteBuffer byteBuffer, int i3, int i4);

    private static native void nativeAGPLoadBufferDirectDouble(int i, int i2, DoubleBuffer doubleBuffer, int i3, int i4);

    private static native void nativeAGPLoadBufferDirectFloat(int i, int i2, FloatBuffer floatBuffer, int i3, int i4);

    private static native void nativeAGPLoadBufferDirectInt(int i, int i2, IntBuffer intBuffer, int i3, int i4);

    private static native void nativeAGPLoadBufferDirectShort(int i, int i2, ShortBuffer shortBuffer, int i3, int i4);

    private static native void nativeAGPLoadBufferDouble(int i, int i2, double[] dArr, int i3, int i4);

    private static native void nativeAGPLoadBufferFloat(int i, int i2, float[] fArr, int i3, int i4);

    private static native void nativeAGPLoadBufferInt(int i, int i2, int[] iArr, int i3, int i4);

    private static native void nativeAGPLoadBufferShort(int i, int i2, short[] sArr, int i3, int i4);

    private static native long nativeAGPMapBuffer(int i);

    private static native void nativeAGPProvisionPipeline(int i, String[] strArr, String[] strArr2);

    private static native void nativeAGPReleaseHandle(int i);

    private static native void nativeAGPReleaseHandles();

    private static native int nativeAGPReleaseSwapChain();

    private static native void nativeAGPSetWindowColor(int i, int i2, int i3, int i4);

    private static native void nativeAGPSwap();

    private static native void nativeAGPUnmapBuffer(int i);

    private static native int nativeAGPUpdateUniform_Buffer(int i, int i2, int i3, int i4, int i5, int i6, int i7);

    private static native int nativeAGPUpdateUniform_Image(int i, int i2, int i3, int i4, int i5);

    private static native void nativeAGPUseProgram(int i);

    private static native void nativeAGPWindowResized(Surface surface);

    private static native int nativeCreateTextureArrayFromBitmaps(Bitmap[] bitmapArr, String[] strArr, String[] strArr2);

    private static native int nativeVKCreateImage(int i, int i2, String[] strArr, String[] strArr2);

    private static native int nativeVKCreateImageFromFile(int i, int i2, String str, String[] strArr, String[] strArr2);

    static {
        preload();
    }

    static void DBG() {
        if (!Thread.currentThread().getName().startsWith("VkRenderThread")) {
            Log.i("DEBUG", "ERROR: Current Thread: " + Thread.currentThread().getName());
            Thread.dumpStack();
        }
    }

    public static boolean preload() {
        if (sPreloaded) {
            return true;
        }
        Log.i(TAG, "starting preload");
        try {
            System.loadLibrary("agp10");
            sPreloaded = true;
        } catch (UnsatisfiedLinkError e) {
            sPreloaded = false;
        }
        return sPreloaded;
    }

    public static boolean isSupportAGP() {
        return sPreloaded;
    }

    public static int createSwapChain(Surface surface) {
        DBG();
        Map<String, String> configMap = new HashMap<>();
        return nativeAGPCreateSwapChain(surface, (String[]) configMap.keySet().toArray(new String[0]), (String[]) configMap.values().toArray(new String[0]));
    }

    public static int createSwapChain(Surface surface, Map<String, String> configMap) {
        DBG();
        return nativeAGPCreateSwapChain(surface, (String[]) configMap.keySet().toArray(new String[0]), (String[]) configMap.values().toArray(new String[0]));
    }

    public static void releaseSwapChain() {
        DBG();
        nativeAGPReleaseSwapChain();
    }

    public static void releaseHandles() {
        DBG();
        nativeAGPReleaseHandles();
    }

    public static void swap() {
        DBG();
        nativeAGPSwap();
    }

    public static void windowResized(Surface surface) {
        DBG();
        nativeAGPWindowResized(surface);
    }

    public static int createGraphicProgram(boolean preCompiled, String vert_shader, String frag_shader, String descriptorSetLayout, String pushConstantLayout, AssetManager assetMgr) {
        DBG();
        return nativeAGPCreateGraphicProgram(preCompiled, vert_shader, frag_shader, descriptorSetLayout, pushConstantLayout, assetMgr);
    }

    public static int createTextureArrayFromBitmaps(Bitmap[] images, Map<String, String> configMap) {
        DBG();
        return nativeCreateTextureArrayFromBitmaps(images, (String[]) configMap.keySet().toArray(new String[0]), (String[]) configMap.values().toArray(new String[0]));
    }

    public static int createImageFromFile(int width, int height, String path, Map<String, String> configMap) {
        DBG();
        return nativeVKCreateImageFromFile(width, height, path, (String[]) configMap.keySet().toArray(new String[0]), (String[]) configMap.values().toArray(new String[0]));
    }

    public static int createImage(int width, int height, Map<String, String> configMap) {
        DBG();
        return nativeVKCreateImage(width, height, (String[]) configMap.keySet().toArray(new String[0]), (String[]) configMap.values().toArray(new String[0]));
    }

    public static int load2DImage(int handle, long dataPtr, int stride) {
        DBG();
        return nativeAGPLoad2DImage(handle, dataPtr, stride);
    }

    public static void releaseHandle(int handle) {
        DBG();
        nativeAGPReleaseHandle(handle);
    }

    public static long allocateGPUMemory(int size) {
        DBG();
        if (size > 0) {
            return nativeAGPAllocateGPUMemory(size);
        }
        return 0;
    }

    public static void freeGPUMemory(long dataPtr) {
        DBG();
        if (dataPtr > 0) {
            nativeAGPFreeGPUMemory(dataPtr);
        }
    }

    public static int createBuffer(String type, int size) {
        DBG();
        if (size > 0) {
            return nativeAGPCreateBuffer(size, type);
        }
        return 0;
    }

    public static long mapBuffer(int handle) {
        DBG();
        return nativeAGPMapBuffer(handle);
    }

    public static void unMapBuffer(int handle) {
        DBG();
        nativeAGPUnmapBuffer(handle);
    }

    public static void loadBufferFloat(int handle, int offset, float[] data, int start, int count) {
        DBG();
        nativeAGPLoadBufferFloat(handle, offset, data, start, count);
    }

    public static void loadBufferInt(int handle, int offset, int[] data, int start, int count) {
        DBG();
        nativeAGPLoadBufferInt(handle, offset, data, start, count);
    }

    public static void loadBufferShort(int handle, int offset, short[] data, int start, int count) {
        DBG();
        nativeAGPLoadBufferShort(handle, offset, data, start, count);
    }

    public static void loadBufferDouble(int handle, int offset, double[] data, int start, int count) {
        DBG();
        nativeAGPLoadBufferDouble(handle, offset, data, start, count);
    }

    public static void loadBufferByte(int handle, int offset, byte[] data, int start, int count) {
        DBG();
        nativeAGPLoadBufferByte(handle, offset, data, start, count);
    }

    public static void loadBufferFloat(int handle, int offset, FloatBuffer data, int start, int count) {
        DBG();
        nativeAGPLoadBufferDirectFloat(handle, offset, data, start, count);
    }

    public static void loadBufferInt(int handle, int offset, IntBuffer data, int start, int count) {
        DBG();
        nativeAGPLoadBufferDirectInt(handle, offset, data, start, count);
    }

    public static void loadBufferShort(int handle, int offset, ShortBuffer data, int start, int count) {
        DBG();
        nativeAGPLoadBufferDirectShort(handle, offset, data, start, count);
    }

    public static void loadBufferDouble(int handle, int offset, DoubleBuffer data, int start, int count) {
        DBG();
        nativeAGPLoadBufferDirectDouble(handle, offset, data, start, count);
    }

    public static void loadBufferByte(int handle, int offset, ByteBuffer data, int start, int count) {
        DBG();
        nativeAGPLoadBufferDirectByte(handle, offset, data, start, count);
    }

    public static void provisionPipeline(int programHandle, Map<String, String> pipelineConfig) {
        DBG();
        nativeAGPProvisionPipeline(programHandle, (String[]) pipelineConfig.keySet().toArray(new String[0]), (String[]) pipelineConfig.values().toArray(new String[0]));
    }

    public static void setActiveShaderProgram(int programHandle) {
        DBG();
        nativeAGPUseProgram(programHandle);
    }

    public static void updateUniformBuffer(int programHandle, int shaderType, int setId, int bind, int bufferHandle, int offset, int range) {
        DBG();
        nativeAGPUpdateUniform_Buffer(programHandle, shaderType, setId, bind, bufferHandle, offset, range);
    }

    public static void updateUniformImage(int programHandle, int shaderType, int setId, int binding, int imageHandle) {
        DBG();
        nativeAGPUpdateUniform_Image(programHandle, shaderType, setId, binding, imageHandle);
    }

    public static void bindVertexBuffers(int[] bufferHandle, int firstBind, int bindcount, int[] offset) {
        DBG();
        nativeAGPBindVertexBuffers(bufferHandle, firstBind, bindcount, offset);
    }

    public static void bindIndexBuffer(int bufferHandle, int offset, String type) {
        DBG();
        nativeAGPBindIndexBuffer(bufferHandle, offset, type);
    }

    public static void draw(int vertexCount, int instanceCount, int firstVextex, int firstInstance) {
        DBG();
        nativeAGPDraw(vertexCount, instanceCount, firstVextex, firstInstance);
    }

    public static void drawIndexed(int indexCount, int instanceCount, int firstindex, int vertexOffset, int firstInstance) {
        DBG();
        nativeAGPDrawIndexed(indexCount, instanceCount, firstindex, vertexOffset, firstInstance);
    }

    public static void drawIndirect(int bufferHandle, int offset, int drawCount, int stride) {
        DBG();
        nativeAGPDrawIndirect(bufferHandle, offset, drawCount, stride);
    }

    public static void beginRenderToTxture(int handle) {
        DBG();
        nativeAGPBeginRenderToTxture(handle);
    }

    public static void endRenderToTxture(int handle) {
        DBG();
        nativeAGPEndRenderToTxture(handle);
    }

    public static int allocateCommandBuffer() {
        DBG();
        return nativeAGPAllocateCommandBuffer();
    }

    public static void freeCommandBuffer(int handle) {
        DBG();
        nativeAGPFreeCommandBuffer(handle);
    }

    public static void beginCommandBuffer(int handle) {
        DBG();
        nativeAGPCommandBufferBegin(handle);
    }

    public static void endCommandBuffer(int handle) {
        DBG();
        nativeAGPFreeCommandBuffer(handle);
    }

    public static void setWindowColor(int r, int g, int b, int alpha) {
        DBG();
        nativeAGPSetWindowColor(r, g, b, alpha);
    }
}
