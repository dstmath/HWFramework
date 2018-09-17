package android.filterfw.core;

import android.filterfw.geometry.Quad;
import android.opengl.GLES20;

public class ShaderProgram extends Program {
    private GLEnvironment mGLEnvironment;
    private int mMaxTileSize = 0;
    private StopWatchMap mTimer = null;
    private int shaderProgramId;

    private native boolean allocate(GLEnvironment gLEnvironment, String str, String str2);

    private native boolean beginShaderDrawing();

    private native boolean compileAndLink();

    private native boolean deallocate();

    private native Object getUniformValue(String str);

    private static native ShaderProgram nativeCreateIdentity(GLEnvironment gLEnvironment);

    private native boolean setShaderAttributeValues(String str, float[] fArr, int i);

    private native boolean setShaderAttributeVertexFrame(String str, VertexFrame vertexFrame, int i, int i2, int i3, int i4, boolean z);

    private native boolean setShaderBlendEnabled(boolean z);

    private native boolean setShaderBlendFunc(int i, int i2);

    private native boolean setShaderClearColor(float f, float f2, float f3);

    private native boolean setShaderClearsOutput(boolean z);

    private native boolean setShaderDrawMode(int i);

    private native boolean setShaderTileCounts(int i, int i2);

    private native boolean setShaderVertexCount(int i);

    private native boolean setTargetRegion(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8);

    private native boolean setUniformValue(String str, Object obj);

    private native boolean shaderProcess(GLFrame[] gLFrameArr, GLFrame gLFrame);

    public native boolean setSourceRegion(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8);

    private void setTimer() {
        this.mTimer = new StopWatchMap();
    }

    private ShaderProgram() {
    }

    private ShaderProgram(NativeAllocatorTag tag) {
    }

    public ShaderProgram(FilterContext context, String fragmentShader) {
        this.mGLEnvironment = getGLEnvironment(context);
        allocate(this.mGLEnvironment, null, fragmentShader);
        if (compileAndLink()) {
            setTimer();
            return;
        }
        throw new RuntimeException("Could not compile and link shader!");
    }

    public ShaderProgram(FilterContext context, String vertexShader, String fragmentShader) {
        this.mGLEnvironment = getGLEnvironment(context);
        allocate(this.mGLEnvironment, vertexShader, fragmentShader);
        if (compileAndLink()) {
            setTimer();
            return;
        }
        throw new RuntimeException("Could not compile and link shader!");
    }

    public static ShaderProgram createIdentity(FilterContext context) {
        ShaderProgram program = nativeCreateIdentity(getGLEnvironment(context));
        program.setTimer();
        return program;
    }

    protected void finalize() throws Throwable {
        deallocate();
    }

    public GLEnvironment getGLEnvironment() {
        return this.mGLEnvironment;
    }

    public void process(Frame[] inputs, Frame output) {
        if (this.mTimer.LOG_MFF_RUNNING_TIMES) {
            this.mTimer.start("glFinish");
            GLES20.glFinish();
            this.mTimer.stop("glFinish");
        }
        GLFrame[] glInputs = new GLFrame[inputs.length];
        int i = 0;
        while (i < inputs.length) {
            if (inputs[i] instanceof GLFrame) {
                glInputs[i] = (GLFrame) inputs[i];
                i++;
            } else {
                throw new RuntimeException("ShaderProgram got non-GL frame as input " + i + "!");
            }
        }
        if (output instanceof GLFrame) {
            GLFrame glOutput = (GLFrame) output;
            if (this.mMaxTileSize > 0) {
                setShaderTileCounts(((output.getFormat().getWidth() + this.mMaxTileSize) - 1) / this.mMaxTileSize, ((output.getFormat().getHeight() + this.mMaxTileSize) - 1) / this.mMaxTileSize);
            }
            if (!shaderProcess(glInputs, glOutput)) {
                throw new RuntimeException("Error executing ShaderProgram!");
            } else if (this.mTimer.LOG_MFF_RUNNING_TIMES) {
                GLES20.glFinish();
                return;
            } else {
                return;
            }
        }
        throw new RuntimeException("ShaderProgram got non-GL output frame!");
    }

    public void setHostValue(String variableName, Object value) {
        if (!setUniformValue(variableName, value)) {
            throw new RuntimeException("Error setting uniform value for variable '" + variableName + "'!");
        }
    }

    public Object getHostValue(String variableName) {
        return getUniformValue(variableName);
    }

    public void setAttributeValues(String attributeName, float[] data, int componentCount) {
        if (!setShaderAttributeValues(attributeName, data, componentCount)) {
            throw new RuntimeException("Error setting attribute value for attribute '" + attributeName + "'!");
        }
    }

    public void setAttributeValues(String attributeName, VertexFrame vertexData, int type, int componentCount, int strideInBytes, int offsetInBytes, boolean normalize) {
        if (!setShaderAttributeVertexFrame(attributeName, vertexData, type, componentCount, strideInBytes, offsetInBytes, normalize)) {
            throw new RuntimeException("Error setting attribute value for attribute '" + attributeName + "'!");
        }
    }

    public void setSourceRegion(Quad region) {
        setSourceRegion(region.p0.x, region.p0.y, region.p1.x, region.p1.y, region.p2.x, region.p2.y, region.p3.x, region.p3.y);
    }

    public void setTargetRegion(Quad region) {
        setTargetRegion(region.p0.x, region.p0.y, region.p1.x, region.p1.y, region.p2.x, region.p2.y, region.p3.x, region.p3.y);
    }

    public void setSourceRect(float x, float y, float width, float height) {
        setSourceRegion(x, y, x + width, y, x, y + height, x + width, y + height);
    }

    public void setTargetRect(float x, float y, float width, float height) {
        setTargetRegion(x, y, x + width, y, x, y + height, x + width, y + height);
    }

    public void setClearsOutput(boolean clears) {
        if (!setShaderClearsOutput(clears)) {
            throw new RuntimeException("Could not set clears-output flag to " + clears + "!");
        }
    }

    public void setClearColor(float r, float g, float b) {
        if (!setShaderClearColor(r, g, b)) {
            throw new RuntimeException("Could not set clear color to " + r + "," + g + "," + b + "!");
        }
    }

    public void setBlendEnabled(boolean enable) {
        if (!setShaderBlendEnabled(enable)) {
            throw new RuntimeException("Could not set Blending " + enable + "!");
        }
    }

    public void setBlendFunc(int sfactor, int dfactor) {
        if (!setShaderBlendFunc(sfactor, dfactor)) {
            throw new RuntimeException("Could not set BlendFunc " + sfactor + "," + dfactor + "!");
        }
    }

    public void setDrawMode(int drawMode) {
        if (!setShaderDrawMode(drawMode)) {
            throw new RuntimeException("Could not set GL draw-mode to " + drawMode + "!");
        }
    }

    public void setVertexCount(int count) {
        if (!setShaderVertexCount(count)) {
            throw new RuntimeException("Could not set GL vertex count to " + count + "!");
        }
    }

    public void setMaximumTileSize(int size) {
        this.mMaxTileSize = size;
    }

    public void beginDrawing() {
        if (!beginShaderDrawing()) {
            throw new RuntimeException("Could not prepare shader-program for drawing!");
        }
    }

    private static GLEnvironment getGLEnvironment(FilterContext context) {
        GLEnvironment result = context != null ? context.getGLEnvironment() : null;
        if (result != null) {
            return result;
        }
        throw new NullPointerException("Attempting to create ShaderProgram with no GL environment in place!");
    }

    static {
        System.loadLibrary("filterfw");
    }
}
