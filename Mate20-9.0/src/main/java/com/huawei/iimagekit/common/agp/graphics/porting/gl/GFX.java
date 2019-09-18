package com.huawei.iimagekit.common.agp.graphics.porting.gl;

import com.huawei.iimagekit.blur.util.DebugUtil;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GFX {
    public static final int COLOR_BUFFER_BIT = 16384;
    public static final int FLOAT_SIZE_BYTES = 4;
    public static final int PRIMARY_FRAMEBUFFER_ID = 0;

    public enum BlendFactor {
        SRC_ALPHA(770),
        ONE_MINUS_SRC_ALPHA(771),
        CONST_ALPHA(32771),
        ONE_MINUS_CONST_ALPHA(32772),
        DST_ALPHA(772),
        ONE_MINUS_DST_ALPHA(773),
        ZERO(0),
        ONE(1);
        
        public final int v;

        private BlendFactor(int v2) {
            this.v = v2;
        }
    }

    public enum CommandBufferLevel {
        PRIMARY(0),
        SECONDARY(1);
        
        public final int v;

        private CommandBufferLevel(int v2) {
            this.v = v2;
        }
    }

    public enum CompareOp {
        NEVER(512),
        LESS(513),
        EQUAL(514),
        LESS_OR_EQUAL(515),
        GREATER(516),
        NOT_EQUAL(517),
        GREATER_OR_EQUAL(518),
        ALWAYS(519);
        
        public final int v;

        private CompareOp(int v2) {
            this.v = v2;
        }
    }

    public enum FilterType {
        NEAREST(9728),
        LINEAR(9729);
        
        public final int v;

        private FilterType(int v2) {
            this.v = v2;
        }
    }

    public enum FramebufferTarget {
        FRAMEBUFFER(36160),
        READ_FRAMEBUFFER(36008),
        DRAW_FRAMEBUFFER(36009);
        
        public final int v;

        private FramebufferTarget(int v2) {
            this.v = v2;
        }
    }

    public enum PrimitiveType {
        TRIANGLE_STRIP(5);
        
        public final int v;

        private PrimitiveType(int v2) {
            this.v = v2;
        }
    }

    public enum State {
        BLEND(3042);
        
        public final int v;

        private State(int v2) {
            this.v = v2;
        }
    }

    public enum StencilOp {
        KEEP(7680),
        ZERO(0),
        REPLACE(7681),
        INCREMENT(7682),
        INCREMENT_AND_WRAP(34055),
        DECREMENT(34056),
        INVERT(5386);
        
        public final int v;

        private StencilOp(int v2) {
            this.v = v2;
        }
    }

    public static native void beginCommandBuffer(int i);

    public static native void bindCommandBuffer(int i);

    public static native void bindExternalTexture(int i);

    public static native void bindFramebuffer(int i);

    public static native void bindFramebufferToTexture(int i, int i2);

    private static native void bindFramebufferToTexture(int i, int i2, int i3);

    public static native void bindIndexBuffer(int i, int i2, FloatBuffer floatBuffer, int i3, int i4, int i5);

    public static native void bindProgram(int i);

    public static native void bindShaderStorageBuffer(int i, int i2, FloatBuffer floatBuffer, int i3, int i4, int i5);

    public static native void bindTexture(int i);

    public static native void bindTexture(int i, int i2);

    public static native void bindUniformBuffer(int i, int i2);

    public static native void bindUniformBuffer(int i, int i2, FloatBuffer floatBuffer, int i3, int i4, int i5);

    public static native void bindVertexBuffer(int i, int i2, FloatBuffer floatBuffer, int i3);

    public static native void bindVertexBuffer(int i, int i2, FloatBuffer floatBuffer, int i3, int i4);

    public static native void bindVertexBuffer(int i, FloatBuffer floatBuffer, int i2);

    public static native void clear(int i);

    public static native void clearColor(float f, float f2, float f3, float f4);

    public static native void destroyBuffer(int i);

    public static native void destroyCommandBuffer(int i);

    public static native void destroyFramebuffer(int i);

    public static native void destroyProgram(int i);

    public static native void destroyTexture(int i);

    public static native void destroyUniformBuffer(int i);

    public static native void disable(State state);

    public static native void drawArrays(PrimitiveType primitiveType, int i, int i2);

    public static native void drawBegin();

    public static native void drawBegin(int i, int i2, int i3);

    public static native void drawEnd();

    public static native void enable(State state);

    public static native void endCommandBuffer(int i);

    public static native int getCurrentFramebuffer();

    public static native void getExternalTextureSize(int i, int[] iArr);

    public static native void getTextureSize(int i, int[] iArr, int i2);

    public static native int getWindowHeight();

    public static native int getWindowWidth();

    public static native int initBuffer(int i, int i2, Buffer buffer, int i3);

    private static native int initCommandBuffer(int i);

    public static native int initExternalTexture();

    public static native int initExternalTexture(int i, int i2);

    public static native int initFramebuffer();

    public static native int initFramebuffer(int i);

    public static native int initProgram(String str, String str2);

    public static native int initTexture(int i, int i2);

    public static native int initTexture(int i, int i2, int i3, int i4, int i5);

    public static native int initTextureArray(int i, int i2, int i3, int i4, FloatBuffer floatBuffer, int i5);

    public static native int initUniformBuffer(int i);

    public static native int initUniformBuffer(int i, Buffer buffer);

    public static native int initUniformBuffer(int i, Buffer buffer, int i2);

    public static native void setBlendColor(float f, float f2, float f3, float f4);

    public static native void setBlendFunc(BlendFactor blendFactor, BlendFactor blendFactor2);

    public static native void setColorMask(boolean z, boolean z2, boolean z3, boolean z4);

    public static native void setDepthMask(boolean z);

    public static native void setStencilFunc(CompareOp compareOp, int i, int i2);

    public static native void setStencilMask(int i);

    public static native void setStencilOp(StencilOp stencilOp, StencilOp stencilOp2, StencilOp stencilOp3);

    private static native void setTextureFilter1(int i, int i2, int i3);

    private static native void setTextureFilter2(int i, int i2, int i3, int i4);

    public static native void setTextureWrap(int i, int i2, int i3);

    public static native void setViewport(int i, int i2);

    public static native void submitCommandBuffer(int i);

    public static native void updateBuffer(int i, Buffer buffer, int i2, int i3, int i4);

    public static native void updateExternalTexture(int i, long j);

    public static native void updateIndexBuffer(int i, FloatBuffer floatBuffer, int i2, int i3);

    public static native void updateShaderStorageBuffer(int i, FloatBuffer floatBuffer, int i2, int i3);

    public static native void updateTexture(int i, FloatBuffer floatBuffer, int i2);

    public static native void updateTexture(int i, FloatBuffer floatBuffer, int i2, int i3, int i4, int i5, int i6);

    public static native void updateUniformBuffer(int i, FloatBuffer floatBuffer, int i2, int i3);

    public static native void updateVertexBuffer(int i, FloatBuffer floatBuffer, int i2, int i3);

    public static int initCommandBuffer(CommandBufferLevel level) {
        return initCommandBuffer(level.v);
    }

    public static int initTexture(int format, int width, int height, FloatBuffer data, int size) {
        DebugUtil.log("TODO");
        return 0;
    }

    public static void getTextureSize(int id, int[] size) {
        getTextureSize(id, size, 3553);
    }

    public static void setTextureFilter(int id, FilterType minFilter, FilterType magFilter) {
        setTextureFilter1(id, minFilter.v, magFilter.v);
    }

    public static void setTextureFilter(int id, FilterType minFilter, FilterType magFilter, int target) {
        setTextureFilter2(id, minFilter.v, magFilter.v, target);
    }

    public static void bindFramebufferToTexture(int fbId, int texId, FramebufferTarget target) {
        bindFramebufferToTexture(fbId, texId, target.v);
    }

    public static FloatBuffer initFloatBuffer(float[] data) {
        return initFloatBuffer(data, 0);
    }

    public static FloatBuffer initFloatBuffer(float[] data, int offset) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        updateFloatBuffer(buffer, data, offset);
        return buffer;
    }

    public static void updateFloatBuffer(FloatBuffer buffer, float[] data) {
        updateFloatBuffer(buffer, data, 0);
    }

    public static void updateFloatBuffer(FloatBuffer buffer, float[] data, int offset) {
        buffer.put(data, offset, data.length);
        buffer.position(0);
    }
}
