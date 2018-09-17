package android.opengl;

import android.hardware.camera2.params.TonemapCurve;
import javax.microedition.khronos.opengles.GL10;

public class GLU {
    private static final float[] sScratch = new float[32];

    public static String gluErrorString(int error) {
        switch (error) {
            case 0:
                return "no error";
            case 1280:
                return "invalid enum";
            case 1281:
                return "invalid value";
            case 1282:
                return "invalid operation";
            case 1283:
                return "stack overflow";
            case 1284:
                return "stack underflow";
            case 1285:
                return "out of memory";
            default:
                return null;
        }
    }

    public static void gluLookAt(GL10 gl, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        float[] scratch = sScratch;
        synchronized (scratch) {
            Matrix.setLookAtM(scratch, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
            gl.glMultMatrixf(scratch, 0);
        }
    }

    public static void gluOrtho2D(GL10 gl, float left, float right, float bottom, float top) {
        gl.glOrthof(left, right, bottom, top, -1.0f, 1.0f);
    }

    public static void gluPerspective(GL10 gl, float fovy, float aspect, float zNear, float zFar) {
        float top = zNear * ((float) Math.tan(((double) fovy) * 0.008726646259971648d));
        float bottom = -top;
        gl.glFrustumf(bottom * aspect, top * aspect, bottom, top, zNear, zFar);
    }

    public static int gluProject(float objX, float objY, float objZ, float[] model, int modelOffset, float[] project, int projectOffset, int[] view, int viewOffset, float[] win, int winOffset) {
        float[] scratch = sScratch;
        synchronized (scratch) {
            Matrix.multiplyMM(scratch, 0, project, projectOffset, model, modelOffset);
            scratch[16] = objX;
            scratch[17] = objY;
            scratch[18] = objZ;
            scratch[19] = 1.0f;
            Matrix.multiplyMV(scratch, 20, scratch, 0, scratch, 16);
            float w = scratch[23];
            if (w == TonemapCurve.LEVEL_BLACK) {
                return 0;
            }
            float rw = 1.0f / w;
            win[winOffset] = ((float) view[viewOffset]) + ((((float) view[viewOffset + 2]) * ((scratch[20] * rw) + 1.0f)) * 0.5f);
            win[winOffset + 1] = ((float) view[viewOffset + 1]) + ((((float) view[viewOffset + 3]) * ((scratch[21] * rw) + 1.0f)) * 0.5f);
            win[winOffset + 2] = ((scratch[22] * rw) + 1.0f) * 0.5f;
            return 1;
        }
    }

    public static int gluUnProject(float winX, float winY, float winZ, float[] model, int modelOffset, float[] project, int projectOffset, int[] view, int viewOffset, float[] obj, int objOffset) {
        float[] scratch = sScratch;
        synchronized (scratch) {
            Matrix.multiplyMM(scratch, 0, project, projectOffset, model, modelOffset);
            if (Matrix.invertM(scratch, 16, scratch, 0)) {
                scratch[0] = (((winX - ((float) view[viewOffset + 0])) * 2.0f) / ((float) view[viewOffset + 2])) - 1.0f;
                scratch[1] = (((winY - ((float) view[viewOffset + 1])) * 2.0f) / ((float) view[viewOffset + 3])) - 1.0f;
                scratch[2] = (2.0f * winZ) - 1.0f;
                scratch[3] = 1.0f;
                Matrix.multiplyMV(obj, objOffset, scratch, 16, scratch, 0);
                return 1;
            }
            return 0;
        }
    }
}
