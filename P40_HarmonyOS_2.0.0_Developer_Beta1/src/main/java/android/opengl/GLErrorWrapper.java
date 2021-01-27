package android.opengl;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL;

class GLErrorWrapper extends GLWrapperBase {
    boolean mCheckError;
    boolean mCheckThread;
    Thread mOurThread;

    public GLErrorWrapper(GL gl, int configFlags) {
        super(gl);
        boolean z = false;
        this.mCheckError = (configFlags & 1) != 0;
        this.mCheckThread = (configFlags & 2) != 0 ? true : z;
    }

    private void checkThread() {
        if (this.mCheckThread) {
            Thread currentThread = Thread.currentThread();
            Thread thread = this.mOurThread;
            if (thread == null) {
                this.mOurThread = currentThread;
            } else if (!thread.equals(currentThread)) {
                throw new GLException(28672, "OpenGL method called from wrong thread.");
            }
        }
    }

    private void checkError() {
        int glError;
        if (this.mCheckError && (glError = this.mgl.glGetError()) != 0) {
            throw new GLException(glError);
        }
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glActiveTexture(int texture) {
        checkThread();
        this.mgl.glActiveTexture(texture);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glAlphaFunc(int func, float ref) {
        checkThread();
        this.mgl.glAlphaFunc(func, ref);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glAlphaFuncx(int func, int ref) {
        checkThread();
        this.mgl.glAlphaFuncx(func, ref);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBindTexture(int target, int texture) {
        checkThread();
        this.mgl.glBindTexture(target, texture);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glBlendFunc(int sfactor, int dfactor) {
        checkThread();
        this.mgl.glBlendFunc(sfactor, dfactor);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClear(int mask) {
        checkThread();
        this.mgl.glClear(mask);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClearColor(float red, float green, float blue, float alpha) {
        checkThread();
        this.mgl.glClearColor(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClearColorx(int red, int green, int blue, int alpha) {
        checkThread();
        this.mgl.glClearColorx(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClearDepthf(float depth) {
        checkThread();
        this.mgl.glClearDepthf(depth);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClearDepthx(int depth) {
        checkThread();
        this.mgl.glClearDepthx(depth);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClearStencil(int s) {
        checkThread();
        this.mgl.glClearStencil(s);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClientActiveTexture(int texture) {
        checkThread();
        this.mgl.glClientActiveTexture(texture);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glColor4f(float red, float green, float blue, float alpha) {
        checkThread();
        this.mgl.glColor4f(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glColor4x(int red, int green, int blue, int alpha) {
        checkThread();
        this.mgl.glColor4x(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        checkThread();
        this.mgl.glColorMask(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glColorPointer(int size, int type, int stride, Buffer pointer) {
        checkThread();
        this.mgl.glColorPointer(size, type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data) {
        checkThread();
        this.mgl.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data) {
        checkThread();
        this.mgl.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border) {
        checkThread();
        this.mgl.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
        checkThread();
        this.mgl.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glCullFace(int mode) {
        checkThread();
        this.mgl.glCullFace(mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDeleteTextures(int n, int[] textures, int offset) {
        checkThread();
        this.mgl.glDeleteTextures(n, textures, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDeleteTextures(int n, IntBuffer textures) {
        checkThread();
        this.mgl.glDeleteTextures(n, textures);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDepthFunc(int func) {
        checkThread();
        this.mgl.glDepthFunc(func);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDepthMask(boolean flag) {
        checkThread();
        this.mgl.glDepthMask(flag);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDepthRangef(float near, float far) {
        checkThread();
        this.mgl.glDepthRangef(near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDepthRangex(int near, int far) {
        checkThread();
        this.mgl.glDepthRangex(near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDisable(int cap) {
        checkThread();
        this.mgl.glDisable(cap);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDisableClientState(int array) {
        checkThread();
        this.mgl.glDisableClientState(array);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDrawArrays(int mode, int first, int count) {
        checkThread();
        this.mgl.glDrawArrays(mode, first, count);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        checkThread();
        this.mgl.glDrawElements(mode, count, type, indices);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11Ext, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glEnable(int cap) {
        checkThread();
        this.mgl.glEnable(cap);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11Ext
    public void glEnableClientState(int array) {
        checkThread();
        this.mgl.glEnableClientState(array);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFinish() {
        checkThread();
        this.mgl.glFinish();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFlush() {
        checkThread();
        this.mgl.glFlush();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogf(int pname, float param) {
        checkThread();
        this.mgl.glFogf(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogfv(int pname, float[] params, int offset) {
        checkThread();
        this.mgl.glFogfv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogfv(int pname, FloatBuffer params) {
        checkThread();
        this.mgl.glFogfv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogx(int pname, int param) {
        checkThread();
        this.mgl.glFogx(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogxv(int pname, int[] params, int offset) {
        checkThread();
        this.mgl.glFogxv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogxv(int pname, IntBuffer params) {
        checkThread();
        this.mgl.glFogxv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFrontFace(int mode) {
        checkThread();
        this.mgl.glFrontFace(mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFrustumf(float left, float right, float bottom, float top, float near, float far) {
        checkThread();
        this.mgl.glFrustumf(left, right, bottom, top, near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFrustumx(int left, int right, int bottom, int top, int near, int far) {
        checkThread();
        this.mgl.glFrustumx(left, right, bottom, top, near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glGenTextures(int n, int[] textures, int offset) {
        checkThread();
        this.mgl.glGenTextures(n, textures, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glGenTextures(int n, IntBuffer textures) {
        checkThread();
        this.mgl.glGenTextures(n, textures);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public int glGetError() {
        checkThread();
        return this.mgl.glGetError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetIntegerv(int pname, int[] params, int offset) {
        checkThread();
        this.mgl.glGetIntegerv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetIntegerv(int pname, IntBuffer params) {
        checkThread();
        this.mgl.glGetIntegerv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public String glGetString(int name) {
        checkThread();
        String result = this.mgl.glGetString(name);
        checkError();
        return result;
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glHint(int target, int mode) {
        checkThread();
        this.mgl.glHint(target, mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelf(int pname, float param) {
        checkThread();
        this.mgl.glLightModelf(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelfv(int pname, float[] params, int offset) {
        checkThread();
        this.mgl.glLightModelfv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelfv(int pname, FloatBuffer params) {
        checkThread();
        this.mgl.glLightModelfv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelx(int pname, int param) {
        checkThread();
        this.mgl.glLightModelx(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelxv(int pname, int[] params, int offset) {
        checkThread();
        this.mgl.glLightModelxv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelxv(int pname, IntBuffer params) {
        checkThread();
        this.mgl.glLightModelxv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightf(int light, int pname, float param) {
        checkThread();
        this.mgl.glLightf(light, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightfv(int light, int pname, float[] params, int offset) {
        checkThread();
        this.mgl.glLightfv(light, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightfv(int light, int pname, FloatBuffer params) {
        checkThread();
        this.mgl.glLightfv(light, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightx(int light, int pname, int param) {
        checkThread();
        this.mgl.glLightx(light, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightxv(int light, int pname, int[] params, int offset) {
        checkThread();
        this.mgl.glLightxv(light, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightxv(int light, int pname, IntBuffer params) {
        checkThread();
        this.mgl.glLightxv(light, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLineWidth(float width) {
        checkThread();
        this.mgl.glLineWidth(width);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLineWidthx(int width) {
        checkThread();
        this.mgl.glLineWidthx(width);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLoadIdentity() {
        checkThread();
        this.mgl.glLoadIdentity();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLoadMatrixf(float[] m, int offset) {
        checkThread();
        this.mgl.glLoadMatrixf(m, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLoadMatrixf(FloatBuffer m) {
        checkThread();
        this.mgl.glLoadMatrixf(m);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLoadMatrixx(int[] m, int offset) {
        checkThread();
        this.mgl.glLoadMatrixx(m, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLoadMatrixx(IntBuffer m) {
        checkThread();
        this.mgl.glLoadMatrixx(m);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLogicOp(int opcode) {
        checkThread();
        this.mgl.glLogicOp(opcode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialf(int face, int pname, float param) {
        checkThread();
        this.mgl.glMaterialf(face, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialfv(int face, int pname, float[] params, int offset) {
        checkThread();
        this.mgl.glMaterialfv(face, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialfv(int face, int pname, FloatBuffer params) {
        checkThread();
        this.mgl.glMaterialfv(face, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialx(int face, int pname, int param) {
        checkThread();
        this.mgl.glMaterialx(face, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialxv(int face, int pname, int[] params, int offset) {
        checkThread();
        this.mgl.glMaterialxv(face, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialxv(int face, int pname, IntBuffer params) {
        checkThread();
        this.mgl.glMaterialxv(face, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMatrixMode(int mode) {
        checkThread();
        this.mgl.glMatrixMode(mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultMatrixf(float[] m, int offset) {
        checkThread();
        this.mgl.glMultMatrixf(m, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultMatrixf(FloatBuffer m) {
        checkThread();
        this.mgl.glMultMatrixf(m);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultMatrixx(int[] m, int offset) {
        checkThread();
        this.mgl.glMultMatrixx(m, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultMatrixx(IntBuffer m) {
        checkThread();
        this.mgl.glMultMatrixx(m);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultiTexCoord4f(int target, float s, float t, float r, float q) {
        checkThread();
        this.mgl.glMultiTexCoord4f(target, s, t, r, q);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultiTexCoord4x(int target, int s, int t, int r, int q) {
        checkThread();
        this.mgl.glMultiTexCoord4x(target, s, t, r, q);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glNormal3f(float nx, float ny, float nz) {
        checkThread();
        this.mgl.glNormal3f(nx, ny, nz);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glNormal3x(int nx, int ny, int nz) {
        checkThread();
        this.mgl.glNormal3x(nx, ny, nz);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glNormalPointer(int type, int stride, Buffer pointer) {
        checkThread();
        this.mgl.glNormalPointer(type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glOrthof(float left, float right, float bottom, float top, float near, float far) {
        checkThread();
        this.mgl.glOrthof(left, right, bottom, top, near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glOrthox(int left, int right, int bottom, int top, int near, int far) {
        checkThread();
        this.mgl.glOrthox(left, right, bottom, top, near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPixelStorei(int pname, int param) {
        checkThread();
        this.mgl.glPixelStorei(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPointSize(float size) {
        checkThread();
        this.mgl.glPointSize(size);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPointSizex(int size) {
        checkThread();
        this.mgl.glPointSizex(size);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPolygonOffset(float factor, float units) {
        checkThread();
        this.mgl.glPolygonOffset(factor, units);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPolygonOffsetx(int factor, int units) {
        checkThread();
        this.mgl.glPolygonOffsetx(factor, units);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPopMatrix() {
        checkThread();
        this.mgl.glPopMatrix();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPushMatrix() {
        checkThread();
        this.mgl.glPushMatrix();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
        checkThread();
        this.mgl.glReadPixels(x, y, width, height, format, type, pixels);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glRotatef(float angle, float x, float y, float z) {
        checkThread();
        this.mgl.glRotatef(angle, x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glRotatex(int angle, int x, int y, int z) {
        checkThread();
        this.mgl.glRotatex(angle, x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glSampleCoverage(float value, boolean invert) {
        checkThread();
        this.mgl.glSampleCoverage(value, invert);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glSampleCoveragex(int value, boolean invert) {
        checkThread();
        this.mgl.glSampleCoveragex(value, invert);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glScalef(float x, float y, float z) {
        checkThread();
        this.mgl.glScalef(x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glScalex(int x, int y, int z) {
        checkThread();
        this.mgl.glScalex(x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glScissor(int x, int y, int width, int height) {
        checkThread();
        this.mgl.glScissor(x, y, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glShadeModel(int mode) {
        checkThread();
        this.mgl.glShadeModel(mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glStencilFunc(int func, int ref, int mask) {
        checkThread();
        this.mgl.glStencilFunc(func, ref, mask);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glStencilMask(int mask) {
        checkThread();
        this.mgl.glStencilMask(mask);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glStencilOp(int fail, int zfail, int zpass) {
        checkThread();
        this.mgl.glStencilOp(fail, zfail, zpass);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
        checkThread();
        this.mgl.glTexCoordPointer(size, type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvf(int target, int pname, float param) {
        checkThread();
        this.mgl.glTexEnvf(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvfv(int target, int pname, float[] params, int offset) {
        checkThread();
        this.mgl.glTexEnvfv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvfv(int target, int pname, FloatBuffer params) {
        checkThread();
        this.mgl.glTexEnvfv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvx(int target, int pname, int param) {
        checkThread();
        this.mgl.glTexEnvx(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvxv(int target, int pname, int[] params, int offset) {
        checkThread();
        this.mgl.glTexEnvxv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvxv(int target, int pname, IntBuffer params) {
        checkThread();
        this.mgl.glTexEnvxv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels) {
        checkThread();
        this.mgl.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexParameterf(int target, int pname, float param) {
        checkThread();
        this.mgl.glTexParameterf(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTexParameterx(int target, int pname, int param) {
        checkThread();
        this.mgl.glTexParameterx(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameteriv(int target, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glTexParameteriv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameteriv(int target, int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glTexParameteriv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels) {
        checkThread();
        this.mgl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTranslatef(float x, float y, float z) {
        checkThread();
        this.mgl.glTranslatef(x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTranslatex(int x, int y, int z) {
        checkThread();
        this.mgl.glTranslatex(x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
        checkThread();
        this.mgl.glVertexPointer(size, type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glViewport(int x, int y, int width, int height) {
        checkThread();
        this.mgl.glViewport(x, y, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glClipPlanef(int plane, float[] equation, int offset) {
        checkThread();
        this.mgl11.glClipPlanef(plane, equation, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glClipPlanef(int plane, FloatBuffer equation) {
        checkThread();
        this.mgl11.glClipPlanef(plane, equation);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glClipPlanex(int plane, int[] equation, int offset) {
        checkThread();
        this.mgl11.glClipPlanex(plane, equation, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glClipPlanex(int plane, IntBuffer equation) {
        checkThread();
        this.mgl11.glClipPlanex(plane, equation);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexfOES(float x, float y, float z, float width, float height) {
        checkThread();
        this.mgl11Ext.glDrawTexfOES(x, y, z, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexfvOES(float[] coords, int offset) {
        checkThread();
        this.mgl11Ext.glDrawTexfvOES(coords, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexfvOES(FloatBuffer coords) {
        checkThread();
        this.mgl11Ext.glDrawTexfvOES(coords);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexiOES(int x, int y, int z, int width, int height) {
        checkThread();
        this.mgl11Ext.glDrawTexiOES(x, y, z, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexivOES(int[] coords, int offset) {
        checkThread();
        this.mgl11Ext.glDrawTexivOES(coords, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexivOES(IntBuffer coords) {
        checkThread();
        this.mgl11Ext.glDrawTexivOES(coords);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexsOES(short x, short y, short z, short width, short height) {
        checkThread();
        this.mgl11Ext.glDrawTexsOES(x, y, z, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexsvOES(short[] coords, int offset) {
        checkThread();
        this.mgl11Ext.glDrawTexsvOES(coords, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexsvOES(ShortBuffer coords) {
        checkThread();
        this.mgl11Ext.glDrawTexsvOES(coords);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexxOES(int x, int y, int z, int width, int height) {
        checkThread();
        this.mgl11Ext.glDrawTexxOES(x, y, z, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexxvOES(int[] coords, int offset) {
        checkThread();
        this.mgl11Ext.glDrawTexxvOES(coords, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexxvOES(IntBuffer coords) {
        checkThread();
        this.mgl11Ext.glDrawTexxvOES(coords);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10Ext
    public int glQueryMatrixxOES(int[] mantissa, int mantissaOffset, int[] exponent, int exponentOffset) {
        checkThread();
        int valid = this.mgl10Ext.glQueryMatrixxOES(mantissa, mantissaOffset, exponent, exponentOffset);
        checkError();
        return valid;
    }

    @Override // javax.microedition.khronos.opengles.GL10Ext
    public int glQueryMatrixxOES(IntBuffer mantissa, IntBuffer exponent) {
        checkThread();
        int valid = this.mgl10Ext.glQueryMatrixxOES(mantissa, exponent);
        checkError();
        return valid;
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glBindBuffer(int target, int buffer) {
        checkThread();
        this.mgl11.glBindBuffer(target, buffer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glBufferData(int target, int size, Buffer data, int usage) {
        checkThread();
        this.mgl11.glBufferData(target, size, data, usage);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glBufferSubData(int target, int offset, int size, Buffer data) {
        checkThread();
        this.mgl11.glBufferSubData(target, offset, size, data);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glColor4ub(byte red, byte green, byte blue, byte alpha) {
        checkThread();
        this.mgl11.glColor4ub(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glColorPointer(int size, int type, int stride, int offset) {
        checkThread();
        this.mgl11.glColorPointer(size, type, stride, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        checkThread();
        this.mgl11.glDeleteBuffers(n, buffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glDeleteBuffers(int n, IntBuffer buffers) {
        checkThread();
        this.mgl11.glDeleteBuffers(n, buffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glDrawElements(int mode, int count, int type, int offset) {
        checkThread();
        this.mgl11.glDrawElements(mode, count, type, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGenBuffers(int n, int[] buffers, int offset) {
        checkThread();
        this.mgl11.glGenBuffers(n, buffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGenBuffers(int n, IntBuffer buffers) {
        checkThread();
        this.mgl11.glGenBuffers(n, buffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetBooleanv(int pname, boolean[] params, int offset) {
        checkThread();
        this.mgl11.glGetBooleanv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetBooleanv(int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glGetBooleanv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetBufferParameteriv(int target, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glGetBufferParameteriv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glGetBufferParameteriv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetClipPlanef(int pname, float[] eqn, int offset) {
        checkThread();
        this.mgl11.glGetClipPlanef(pname, eqn, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetClipPlanef(int pname, FloatBuffer eqn) {
        checkThread();
        this.mgl11.glGetClipPlanef(pname, eqn);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetClipPlanex(int pname, int[] eqn, int offset) {
        checkThread();
        this.mgl11.glGetClipPlanex(pname, eqn, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetClipPlanex(int pname, IntBuffer eqn) {
        checkThread();
        this.mgl11.glGetClipPlanex(pname, eqn);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetFixedv(int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glGetFixedv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetFixedv(int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glGetFixedv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetFloatv(int pname, float[] params, int offset) {
        checkThread();
        this.mgl11.glGetFloatv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetFloatv(int pname, FloatBuffer params) {
        checkThread();
        this.mgl11.glGetFloatv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetLightfv(int light, int pname, float[] params, int offset) {
        checkThread();
        this.mgl11.glGetLightfv(light, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetLightfv(int light, int pname, FloatBuffer params) {
        checkThread();
        this.mgl11.glGetLightfv(light, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetLightxv(int light, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glGetLightxv(light, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetLightxv(int light, int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glGetLightxv(light, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetMaterialfv(int face, int pname, float[] params, int offset) {
        checkThread();
        this.mgl11.glGetMaterialfv(face, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetMaterialfv(int face, int pname, FloatBuffer params) {
        checkThread();
        this.mgl11.glGetMaterialfv(face, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetMaterialxv(int face, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glGetMaterialxv(face, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetMaterialxv(int face, int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glGetMaterialxv(face, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetPointerv(int pname, Buffer[] params) {
        checkThread();
        this.mgl11.glGetPointerv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexEnviv(int env, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glGetTexEnviv(env, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexEnviv(int env, int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glGetTexEnviv(env, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexEnvxv(int env, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glGetTexEnvxv(env, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexEnvxv(int env, int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glGetTexEnvxv(env, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameterfv(int target, int pname, float[] params, int offset) {
        checkThread();
        this.mgl11.glGetTexParameterfv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
        checkThread();
        this.mgl11.glGetTexParameterfv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameteriv(int target, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glGetTexParameteriv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glGetTexParameteriv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameterxv(int target, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glGetTexParameterxv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameterxv(int target, int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glGetTexParameterxv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public boolean glIsBuffer(int buffer) {
        checkThread();
        boolean valid = this.mgl11.glIsBuffer(buffer);
        checkError();
        return valid;
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public boolean glIsEnabled(int cap) {
        checkThread();
        boolean valid = this.mgl11.glIsEnabled(cap);
        checkError();
        return valid;
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public boolean glIsTexture(int texture) {
        checkThread();
        boolean valid = this.mgl11.glIsTexture(texture);
        checkError();
        return valid;
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glNormalPointer(int type, int stride, int offset) {
        checkThread();
        this.mgl11.glNormalPointer(type, stride, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterf(int pname, float param) {
        checkThread();
        this.mgl11.glPointParameterf(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterfv(int pname, float[] params, int offset) {
        checkThread();
        this.mgl11.glPointParameterfv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterfv(int pname, FloatBuffer params) {
        checkThread();
        this.mgl11.glPointParameterfv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterx(int pname, int param) {
        checkThread();
        this.mgl11.glPointParameterx(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterxv(int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glPointParameterxv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterxv(int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glPointParameterxv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointSizePointerOES(int type, int stride, Buffer pointer) {
        checkThread();
        this.mgl11.glPointSizePointerOES(type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexCoordPointer(int size, int type, int stride, int offset) {
        checkThread();
        this.mgl11.glTexCoordPointer(size, type, stride, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexEnvi(int target, int pname, int param) {
        checkThread();
        this.mgl11.glTexEnvi(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexEnviv(int target, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glTexEnviv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexEnviv(int target, int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glTexEnviv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11, javax.microedition.khronos.opengles.GL11Ext
    public void glTexParameterfv(int target, int pname, float[] params, int offset) {
        checkThread();
        this.mgl11.glTexParameterfv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameterfv(int target, int pname, FloatBuffer params) {
        checkThread();
        this.mgl11.glTexParameterfv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameteri(int target, int pname, int param) {
        checkThread();
        this.mgl11.glTexParameteri(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameterxv(int target, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11.glTexParameterxv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameterxv(int target, int pname, IntBuffer params) {
        checkThread();
        this.mgl11.glTexParameterxv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glVertexPointer(int size, int type, int stride, int offset) {
        checkThread();
        this.mgl11.glVertexPointer(size, type, stride, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glCurrentPaletteMatrixOES(int matrixpaletteindex) {
        checkThread();
        this.mgl11Ext.glCurrentPaletteMatrixOES(matrixpaletteindex);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glLoadPaletteFromModelViewMatrixOES() {
        checkThread();
        this.mgl11Ext.glLoadPaletteFromModelViewMatrixOES();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glMatrixIndexPointerOES(int size, int type, int stride, Buffer pointer) {
        checkThread();
        this.mgl11Ext.glMatrixIndexPointerOES(size, type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glMatrixIndexPointerOES(int size, int type, int stride, int offset) {
        checkThread();
        this.mgl11Ext.glMatrixIndexPointerOES(size, type, stride, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glWeightPointerOES(int size, int type, int stride, Buffer pointer) {
        checkThread();
        this.mgl11Ext.glWeightPointerOES(size, type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glWeightPointerOES(int size, int type, int stride, int offset) {
        checkThread();
        this.mgl11Ext.glWeightPointerOES(size, type, stride, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBindFramebufferOES(int target, int framebuffer) {
        checkThread();
        this.mgl11ExtensionPack.glBindFramebufferOES(target, framebuffer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBindRenderbufferOES(int target, int renderbuffer) {
        checkThread();
        this.mgl11ExtensionPack.glBindRenderbufferOES(target, renderbuffer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBlendEquation(int mode) {
        checkThread();
        this.mgl11ExtensionPack.glBlendEquation(mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        checkThread();
        this.mgl11ExtensionPack.glBlendEquationSeparate(modeRGB, modeAlpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        checkThread();
        this.mgl11ExtensionPack.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public int glCheckFramebufferStatusOES(int target) {
        checkThread();
        int result = this.mgl11ExtensionPack.glCheckFramebufferStatusOES(target);
        checkError();
        return result;
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glDeleteFramebuffersOES(int n, int[] framebuffers, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glDeleteFramebuffersOES(n, framebuffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glDeleteFramebuffersOES(int n, IntBuffer framebuffers) {
        checkThread();
        this.mgl11ExtensionPack.glDeleteFramebuffersOES(n, framebuffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glDeleteRenderbuffersOES(int n, int[] renderbuffers, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glDeleteRenderbuffersOES(n, renderbuffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glDeleteRenderbuffersOES(int n, IntBuffer renderbuffers) {
        checkThread();
        this.mgl11ExtensionPack.glDeleteRenderbuffersOES(n, renderbuffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glFramebufferRenderbufferOES(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        checkThread();
        this.mgl11ExtensionPack.glFramebufferRenderbufferOES(target, attachment, renderbuffertarget, renderbuffer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glFramebufferTexture2DOES(int target, int attachment, int textarget, int texture, int level) {
        checkThread();
        this.mgl11ExtensionPack.glFramebufferTexture2DOES(target, attachment, textarget, texture, level);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGenerateMipmapOES(int target) {
        checkThread();
        this.mgl11ExtensionPack.glGenerateMipmapOES(target);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGenFramebuffersOES(int n, int[] framebuffers, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glGenFramebuffersOES(n, framebuffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGenFramebuffersOES(int n, IntBuffer framebuffers) {
        checkThread();
        this.mgl11ExtensionPack.glGenFramebuffersOES(n, framebuffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGenRenderbuffersOES(int n, int[] renderbuffers, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glGenRenderbuffersOES(n, renderbuffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGenRenderbuffersOES(int n, IntBuffer renderbuffers) {
        checkThread();
        this.mgl11ExtensionPack.glGenRenderbuffersOES(n, renderbuffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetFramebufferAttachmentParameterivOES(int target, int attachment, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glGetFramebufferAttachmentParameterivOES(target, attachment, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetFramebufferAttachmentParameterivOES(int target, int attachment, int pname, IntBuffer params) {
        checkThread();
        this.mgl11ExtensionPack.glGetFramebufferAttachmentParameterivOES(target, attachment, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetRenderbufferParameterivOES(int target, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glGetRenderbufferParameterivOES(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetRenderbufferParameterivOES(int target, int pname, IntBuffer params) {
        checkThread();
        this.mgl11ExtensionPack.glGetRenderbufferParameterivOES(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGenfv(int coord, int pname, float[] params, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glGetTexGenfv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGenfv(int coord, int pname, FloatBuffer params) {
        checkThread();
        this.mgl11ExtensionPack.glGetTexGenfv(coord, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGeniv(int coord, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glGetTexGeniv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGeniv(int coord, int pname, IntBuffer params) {
        checkThread();
        this.mgl11ExtensionPack.glGetTexGeniv(coord, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGenxv(int coord, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glGetTexGenxv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGenxv(int coord, int pname, IntBuffer params) {
        checkThread();
        this.mgl11ExtensionPack.glGetTexGenxv(coord, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public boolean glIsFramebufferOES(int framebuffer) {
        checkThread();
        boolean result = this.mgl11ExtensionPack.glIsFramebufferOES(framebuffer);
        checkError();
        return result;
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public boolean glIsRenderbufferOES(int renderbuffer) {
        checkThread();
        this.mgl11ExtensionPack.glIsRenderbufferOES(renderbuffer);
        checkError();
        return false;
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glRenderbufferStorageOES(int target, int internalformat, int width, int height) {
        checkThread();
        this.mgl11ExtensionPack.glRenderbufferStorageOES(target, internalformat, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenf(int coord, int pname, float param) {
        checkThread();
        this.mgl11ExtensionPack.glTexGenf(coord, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenfv(int coord, int pname, float[] params, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glTexGenfv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenfv(int coord, int pname, FloatBuffer params) {
        checkThread();
        this.mgl11ExtensionPack.glTexGenfv(coord, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGeni(int coord, int pname, int param) {
        checkThread();
        this.mgl11ExtensionPack.glTexGeni(coord, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGeniv(int coord, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glTexGeniv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGeniv(int coord, int pname, IntBuffer params) {
        checkThread();
        this.mgl11ExtensionPack.glTexGeniv(coord, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenx(int coord, int pname, int param) {
        checkThread();
        this.mgl11ExtensionPack.glTexGenx(coord, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenxv(int coord, int pname, int[] params, int offset) {
        checkThread();
        this.mgl11ExtensionPack.glTexGenxv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenxv(int coord, int pname, IntBuffer params) {
        checkThread();
        this.mgl11ExtensionPack.glTexGenxv(coord, pname, params);
        checkError();
    }
}
