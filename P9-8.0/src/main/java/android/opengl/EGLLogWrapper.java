package android.opengl;

import android.content.Context;
import android.media.midi.MidiDeviceInfo;
import android.net.wifi.WifiEnterpriseConfig;
import java.io.IOException;
import java.io.Writer;
import javax.microedition.khronos.egl.EGL;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

class EGLLogWrapper implements EGL11 {
    private int mArgCount;
    boolean mCheckError;
    private EGL10 mEgl10;
    Writer mLog;
    boolean mLogArgumentNames;

    public EGLLogWrapper(EGL egl, int configFlags, Writer log) {
        boolean z;
        boolean z2 = true;
        this.mEgl10 = (EGL10) egl;
        this.mLog = log;
        if ((configFlags & 4) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mLogArgumentNames = z;
        if ((configFlags & 1) == 0) {
            z2 = false;
        }
        this.mCheckError = z2;
    }

    public boolean eglChooseConfig(EGLDisplay display, int[] attrib_list, EGLConfig[] configs, int config_size, int[] num_config) {
        begin("eglChooseConfig");
        arg(Context.DISPLAY_SERVICE, display);
        arg("attrib_list", attrib_list);
        arg("config_size", config_size);
        end();
        boolean result = this.mEgl10.eglChooseConfig(display, attrib_list, configs, config_size, num_config);
        arg("configs", (Object[]) configs);
        arg("num_config", num_config);
        returns(result);
        checkError();
        return result;
    }

    public boolean eglCopyBuffers(EGLDisplay display, EGLSurface surface, Object native_pixmap) {
        begin("eglCopyBuffers");
        arg(Context.DISPLAY_SERVICE, display);
        arg("surface", surface);
        arg("native_pixmap", native_pixmap);
        end();
        boolean result = this.mEgl10.eglCopyBuffers(display, surface, native_pixmap);
        returns(result);
        checkError();
        return result;
    }

    public EGLContext eglCreateContext(EGLDisplay display, EGLConfig config, EGLContext share_context, int[] attrib_list) {
        begin("eglCreateContext");
        arg(Context.DISPLAY_SERVICE, display);
        arg("config", (Object) config);
        arg("share_context", share_context);
        arg("attrib_list", attrib_list);
        end();
        Object result = this.mEgl10.eglCreateContext(display, config, share_context, attrib_list);
        returns(result);
        checkError();
        return result;
    }

    public EGLSurface eglCreatePbufferSurface(EGLDisplay display, EGLConfig config, int[] attrib_list) {
        begin("eglCreatePbufferSurface");
        arg(Context.DISPLAY_SERVICE, display);
        arg("config", (Object) config);
        arg("attrib_list", attrib_list);
        end();
        Object result = this.mEgl10.eglCreatePbufferSurface(display, config, attrib_list);
        returns(result);
        checkError();
        return result;
    }

    public EGLSurface eglCreatePixmapSurface(EGLDisplay display, EGLConfig config, Object native_pixmap, int[] attrib_list) {
        begin("eglCreatePixmapSurface");
        arg(Context.DISPLAY_SERVICE, display);
        arg("config", (Object) config);
        arg("native_pixmap", native_pixmap);
        arg("attrib_list", attrib_list);
        end();
        Object result = this.mEgl10.eglCreatePixmapSurface(display, config, native_pixmap, attrib_list);
        returns(result);
        checkError();
        return result;
    }

    public EGLSurface eglCreateWindowSurface(EGLDisplay display, EGLConfig config, Object native_window, int[] attrib_list) {
        begin("eglCreateWindowSurface");
        arg(Context.DISPLAY_SERVICE, display);
        arg("config", (Object) config);
        arg("native_window", native_window);
        arg("attrib_list", attrib_list);
        end();
        Object result = this.mEgl10.eglCreateWindowSurface(display, config, native_window, attrib_list);
        returns(result);
        checkError();
        return result;
    }

    public boolean eglDestroyContext(EGLDisplay display, EGLContext context) {
        begin("eglDestroyContext");
        arg(Context.DISPLAY_SERVICE, display);
        arg("context", context);
        end();
        boolean result = this.mEgl10.eglDestroyContext(display, context);
        returns(result);
        checkError();
        return result;
    }

    public boolean eglDestroySurface(EGLDisplay display, EGLSurface surface) {
        begin("eglDestroySurface");
        arg(Context.DISPLAY_SERVICE, display);
        arg("surface", surface);
        end();
        boolean result = this.mEgl10.eglDestroySurface(display, surface);
        returns(result);
        checkError();
        return result;
    }

    public boolean eglGetConfigAttrib(EGLDisplay display, EGLConfig config, int attribute, int[] value) {
        begin("eglGetConfigAttrib");
        arg(Context.DISPLAY_SERVICE, display);
        arg("config", (Object) config);
        arg("attribute", attribute);
        end();
        boolean result = this.mEgl10.eglGetConfigAttrib(display, config, attribute, value);
        arg("value", value);
        returns(result);
        checkError();
        return false;
    }

    public boolean eglGetConfigs(EGLDisplay display, EGLConfig[] configs, int config_size, int[] num_config) {
        begin("eglGetConfigs");
        arg(Context.DISPLAY_SERVICE, display);
        arg("config_size", config_size);
        end();
        boolean result = this.mEgl10.eglGetConfigs(display, configs, config_size, num_config);
        arg("configs", (Object[]) configs);
        arg("num_config", num_config);
        returns(result);
        checkError();
        return result;
    }

    public EGLContext eglGetCurrentContext() {
        begin("eglGetCurrentContext");
        end();
        Object result = this.mEgl10.eglGetCurrentContext();
        returns(result);
        checkError();
        return result;
    }

    public EGLDisplay eglGetCurrentDisplay() {
        begin("eglGetCurrentDisplay");
        end();
        Object result = this.mEgl10.eglGetCurrentDisplay();
        returns(result);
        checkError();
        return result;
    }

    public EGLSurface eglGetCurrentSurface(int readdraw) {
        begin("eglGetCurrentSurface");
        arg("readdraw", readdraw);
        end();
        Object result = this.mEgl10.eglGetCurrentSurface(readdraw);
        returns(result);
        checkError();
        return result;
    }

    public EGLDisplay eglGetDisplay(Object native_display) {
        begin("eglGetDisplay");
        arg("native_display", native_display);
        end();
        Object result = this.mEgl10.eglGetDisplay(native_display);
        returns(result);
        checkError();
        return result;
    }

    public int eglGetError() {
        begin("eglGetError");
        end();
        int result = this.mEgl10.eglGetError();
        returns(getErrorString(result));
        return result;
    }

    public boolean eglInitialize(EGLDisplay display, int[] major_minor) {
        begin("eglInitialize");
        arg(Context.DISPLAY_SERVICE, display);
        end();
        boolean result = this.mEgl10.eglInitialize(display, major_minor);
        returns(result);
        arg("major_minor", major_minor);
        checkError();
        return result;
    }

    public boolean eglMakeCurrent(EGLDisplay display, EGLSurface draw, EGLSurface read, EGLContext context) {
        begin("eglMakeCurrent");
        arg(Context.DISPLAY_SERVICE, display);
        arg("draw", draw);
        arg("read", read);
        arg("context", context);
        end();
        boolean result = this.mEgl10.eglMakeCurrent(display, draw, read, context);
        returns(result);
        checkError();
        return result;
    }

    public boolean eglQueryContext(EGLDisplay display, EGLContext context, int attribute, int[] value) {
        begin("eglQueryContext");
        arg(Context.DISPLAY_SERVICE, display);
        arg("context", context);
        arg("attribute", attribute);
        end();
        boolean result = this.mEgl10.eglQueryContext(display, context, attribute, value);
        returns(value[0]);
        returns(result);
        checkError();
        return result;
    }

    public String eglQueryString(EGLDisplay display, int name) {
        begin("eglQueryString");
        arg(Context.DISPLAY_SERVICE, display);
        arg(MidiDeviceInfo.PROPERTY_NAME, name);
        end();
        String result = this.mEgl10.eglQueryString(display, name);
        returns(result);
        checkError();
        return result;
    }

    public boolean eglQuerySurface(EGLDisplay display, EGLSurface surface, int attribute, int[] value) {
        begin("eglQuerySurface");
        arg(Context.DISPLAY_SERVICE, display);
        arg("surface", surface);
        arg("attribute", attribute);
        end();
        boolean result = this.mEgl10.eglQuerySurface(display, surface, attribute, value);
        returns(value[0]);
        returns(result);
        checkError();
        return result;
    }

    public boolean eglReleaseThread() {
        begin("eglReleaseThread");
        end();
        boolean result = this.mEgl10.eglReleaseThread();
        returns(result);
        checkError();
        return result;
    }

    public boolean eglSwapBuffers(EGLDisplay display, EGLSurface surface) {
        begin("eglSwapBuffers");
        arg(Context.DISPLAY_SERVICE, display);
        arg("surface", surface);
        end();
        boolean result = this.mEgl10.eglSwapBuffers(display, surface);
        returns(result);
        checkError();
        return result;
    }

    public boolean eglTerminate(EGLDisplay display) {
        begin("eglTerminate");
        arg(Context.DISPLAY_SERVICE, display);
        end();
        boolean result = this.mEgl10.eglTerminate(display);
        returns(result);
        checkError();
        return result;
    }

    public boolean eglWaitGL() {
        begin("eglWaitGL");
        end();
        boolean result = this.mEgl10.eglWaitGL();
        returns(result);
        checkError();
        return result;
    }

    public boolean eglWaitNative(int engine, Object bindTarget) {
        begin("eglWaitNative");
        arg(WifiEnterpriseConfig.ENGINE_KEY, engine);
        arg("bindTarget", bindTarget);
        end();
        boolean result = this.mEgl10.eglWaitNative(engine, bindTarget);
        returns(result);
        checkError();
        return result;
    }

    private void checkError() {
        int eglError = this.mEgl10.eglGetError();
        if (eglError != 12288) {
            String errorMessage = "eglError: " + getErrorString(eglError);
            logLine(errorMessage);
            if (this.mCheckError) {
                throw new GLException(eglError, errorMessage);
            }
        }
    }

    private void logLine(String message) {
        log(message + 10);
    }

    private void log(String message) {
        try {
            this.mLog.write(message);
        } catch (IOException e) {
        }
    }

    private void begin(String name) {
        log(name + '(');
        this.mArgCount = 0;
    }

    private void arg(String name, String value) {
        int i = this.mArgCount;
        this.mArgCount = i + 1;
        if (i > 0) {
            log(", ");
        }
        if (this.mLogArgumentNames) {
            log(name + "=");
        }
        log(value);
    }

    private void end() {
        log(");\n");
        flush();
    }

    private void flush() {
        try {
            this.mLog.flush();
        } catch (IOException e) {
            this.mLog = null;
        }
    }

    private void arg(String name, int value) {
        arg(name, Integer.toString(value));
    }

    private void arg(String name, Object object) {
        arg(name, toString(object));
    }

    private void arg(String name, EGLDisplay object) {
        if (object == EGL10.EGL_DEFAULT_DISPLAY) {
            arg(name, "EGL10.EGL_DEFAULT_DISPLAY");
        } else if (object == EGL_NO_DISPLAY) {
            arg(name, "EGL10.EGL_NO_DISPLAY");
        } else {
            arg(name, toString(object));
        }
    }

    private void arg(String name, EGLContext object) {
        if (object == EGL10.EGL_NO_CONTEXT) {
            arg(name, "EGL10.EGL_NO_CONTEXT");
        } else {
            arg(name, toString(object));
        }
    }

    private void arg(String name, EGLSurface object) {
        if (object == EGL10.EGL_NO_SURFACE) {
            arg(name, "EGL10.EGL_NO_SURFACE");
        } else {
            arg(name, toString(object));
        }
    }

    private void returns(String result) {
        log(" returns " + result + ";\n");
        flush();
    }

    private void returns(int result) {
        returns(Integer.toString(result));
    }

    private void returns(boolean result) {
        returns(Boolean.toString(result));
    }

    private void returns(Object result) {
        returns(toString(result));
    }

    private String toString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString();
    }

    private void arg(String name, int[] arr) {
        if (arr == null) {
            arg(name, "null");
        } else {
            arg(name, toString(arr.length, arr, 0));
        }
    }

    private void arg(String name, Object[] arr) {
        if (arr == null) {
            arg(name, "null");
        } else {
            arg(name, toString(arr.length, arr, 0));
        }
    }

    private String toString(int n, int[] arr, int offset) {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        int arrLen = arr.length;
        for (int i = 0; i < n; i++) {
            int index = offset + i;
            buf.append(" [").append(index).append("] = ");
            if (index < 0 || index >= arrLen) {
                buf.append("out of bounds");
            } else {
                buf.append(arr[index]);
            }
            buf.append(10);
        }
        buf.append("}");
        return buf.toString();
    }

    private String toString(int n, Object[] arr, int offset) {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        int arrLen = arr.length;
        for (int i = 0; i < n; i++) {
            int index = offset + i;
            buf.append(" [").append(index).append("] = ");
            if (index < 0 || index >= arrLen) {
                buf.append("out of bounds");
            } else {
                buf.append(arr[index]);
            }
            buf.append(10);
        }
        buf.append("}");
        return buf.toString();
    }

    private static String getHex(int value) {
        return "0x" + Integer.toHexString(value);
    }

    public static String getErrorString(int error) {
        switch (error) {
            case 12288:
                return "EGL_SUCCESS";
            case 12289:
                return "EGL_NOT_INITIALIZED";
            case 12290:
                return "EGL_BAD_ACCESS";
            case 12291:
                return "EGL_BAD_ALLOC";
            case 12292:
                return "EGL_BAD_ATTRIBUTE";
            case 12293:
                return "EGL_BAD_CONFIG";
            case 12294:
                return "EGL_BAD_CONTEXT";
            case 12295:
                return "EGL_BAD_CURRENT_SURFACE";
            case 12296:
                return "EGL_BAD_DISPLAY";
            case 12297:
                return "EGL_BAD_MATCH";
            case 12298:
                return "EGL_BAD_NATIVE_PIXMAP";
            case 12299:
                return "EGL_BAD_NATIVE_WINDOW";
            case 12300:
                return "EGL_BAD_PARAMETER";
            case EGL14.EGL_BAD_SURFACE /*12301*/:
                return "EGL_BAD_SURFACE";
            case EGL14.EGL_CONTEXT_LOST /*12302*/:
                return "EGL_CONTEXT_LOST";
            default:
                return getHex(error);
        }
    }
}
