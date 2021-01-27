package com.android.server.gesture.anim;

import android.opengl.GLES30;

public class GLHelper {
    private static final String TAG = "GLHelper";

    private GLHelper() {
    }

    private static int compileShader(int type, String shaderCode) {
        int shaderObjectId = GLES30.glCreateShader(type);
        for (int error = GLES30.glGetError(); error != 0; error = GLES30.glGetError()) {
            GLLogUtils.logE(TAG, "error " + error);
        }
        if (shaderObjectId == 0) {
            GLLogUtils.logW(TAG, "Could not create new shader.");
            return 0;
        }
        GLES30.glShaderSource(shaderObjectId, shaderCode);
        GLES30.glCompileShader(shaderObjectId);
        int[] compileStatus = new int[1];
        GLES30.glGetShaderiv(shaderObjectId, 35713, compileStatus, 0);
        GLLogUtils.logD(TAG, "Results of compiling source:" + System.lineSeparator() + shaderCode + System.lineSeparator() + ":" + GLES30.glGetShaderInfoLog(shaderObjectId));
        if (compileStatus[0] != 0) {
            return shaderObjectId;
        }
        GLES30.glDeleteShader(shaderObjectId);
        GLLogUtils.logW(TAG, "Compilation of shader failed.");
        return 0;
    }

    private static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        int programObjectId = GLES30.glCreateProgram();
        if (programObjectId == 0) {
            GLLogUtils.logW(TAG, "Could not create new program");
            return 0;
        }
        GLES30.glAttachShader(programObjectId, vertexShaderId);
        GLES30.glAttachShader(programObjectId, fragmentShaderId);
        GLES30.glLinkProgram(programObjectId);
        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(programObjectId, 35714, linkStatus, 0);
        GLLogUtils.logV(TAG, "Results of linking program:" + System.lineSeparator() + GLES30.glGetProgramInfoLog(programObjectId));
        if (linkStatus[0] != 0) {
            return programObjectId;
        }
        GLES30.glDeleteProgram(programObjectId);
        GLLogUtils.logW(TAG, "Linking of program failed.");
        return 0;
    }

    private static void validateProgram(int programObjectId) {
        GLES30.glValidateProgram(programObjectId);
        int[] validateStatus = new int[1];
        GLES30.glGetProgramiv(programObjectId, 35715, validateStatus, 0);
        GLLogUtils.logV(TAG, "Results of validating program: " + validateStatus[0] + System.lineSeparator() + "Log:" + GLES30.glGetProgramInfoLog(programObjectId));
    }

    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        int program = linkProgram(compileShader(35633, vertexShaderSource), compileShader(35632, fragmentShaderSource));
        validateProgram(program);
        return program;
    }
}
