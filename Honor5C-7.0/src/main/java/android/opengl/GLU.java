package android.opengl;

import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import javax.microedition.khronos.opengles.GL10;

public class GLU {
    private static final float[] sScratch = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.opengl.GLU.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.opengl.GLU.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLU.<clinit>():void");
    }

    public static String gluErrorString(int error) {
        switch (error) {
            case TextToSpeech.SUCCESS /*0*/:
                return "no error";
            case GLES20.GL_INVALID_ENUM /*1280*/:
                return "invalid enum";
            case GLES20.GL_INVALID_VALUE /*1281*/:
                return "invalid value";
            case GLES20.GL_INVALID_OPERATION /*1282*/:
                return "invalid operation";
            case GLES32.GL_STACK_OVERFLOW /*1283*/:
                return "stack overflow";
            case GLES32.GL_STACK_UNDERFLOW /*1284*/:
                return "stack underflow";
            case GLES20.GL_OUT_OF_MEMORY /*1285*/:
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
        gl.glOrthof(left, right, bottom, top, ScaledLayoutParams.SCALE_UNSPECIFIED, Engine.DEFAULT_VOLUME);
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
            scratch[19] = Engine.DEFAULT_VOLUME;
            Matrix.multiplyMV(scratch, 20, scratch, 0, scratch, 16);
            float w = scratch[23];
            if (w == 0.0f) {
                return 0;
            }
            float rw = Engine.DEFAULT_VOLUME / w;
            win[winOffset] = ((float) view[viewOffset]) + ((((float) view[viewOffset + 2]) * ((scratch[20] * rw) + Engine.DEFAULT_VOLUME)) * NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            win[winOffset + 1] = ((float) view[viewOffset + 1]) + ((((float) view[viewOffset + 3]) * ((scratch[21] * rw) + Engine.DEFAULT_VOLUME)) * NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            win[winOffset + 2] = ((scratch[22] * rw) + Engine.DEFAULT_VOLUME) * NetworkHistoryUtils.RECOVERY_PERCENTAGE;
            return 1;
        }
    }

    public static int gluUnProject(float winX, float winY, float winZ, float[] model, int modelOffset, float[] project, int projectOffset, int[] view, int viewOffset, float[] obj, int objOffset) {
        float[] scratch = sScratch;
        synchronized (scratch) {
            Matrix.multiplyMM(scratch, 0, project, projectOffset, model, modelOffset);
            if (Matrix.invertM(scratch, 16, scratch, 0)) {
                scratch[0] = (((winX - ((float) view[viewOffset + 0])) * 2.0f) / ((float) view[viewOffset + 2])) - Engine.DEFAULT_VOLUME;
                scratch[1] = (((winY - ((float) view[viewOffset + 1])) * 2.0f) / ((float) view[viewOffset + 3])) - Engine.DEFAULT_VOLUME;
                scratch[2] = (2.0f * winZ) - Engine.DEFAULT_VOLUME;
                scratch[3] = Engine.DEFAULT_VOLUME;
                Matrix.multiplyMV(obj, objOffset, scratch, 16, scratch, 0);
                return 1;
            }
            return 0;
        }
    }
}
