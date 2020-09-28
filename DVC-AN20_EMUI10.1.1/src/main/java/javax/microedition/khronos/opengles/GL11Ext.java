package javax.microedition.khronos.opengles;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public interface GL11Ext extends GL {
    public static final int GL_MATRIX_INDEX_ARRAY_BUFFER_BINDING_OES = 35742;
    public static final int GL_MATRIX_INDEX_ARRAY_OES = 34884;
    public static final int GL_MATRIX_INDEX_ARRAY_POINTER_OES = 34889;
    public static final int GL_MATRIX_INDEX_ARRAY_SIZE_OES = 34886;
    public static final int GL_MATRIX_INDEX_ARRAY_STRIDE_OES = 34888;
    public static final int GL_MATRIX_INDEX_ARRAY_TYPE_OES = 34887;
    public static final int GL_MATRIX_PALETTE_OES = 34880;
    public static final int GL_MAX_PALETTE_MATRICES_OES = 34882;
    public static final int GL_MAX_VERTEX_UNITS_OES = 34468;
    public static final int GL_TEXTURE_CROP_RECT_OES = 35741;
    public static final int GL_WEIGHT_ARRAY_BUFFER_BINDING_OES = 34974;
    public static final int GL_WEIGHT_ARRAY_OES = 34477;
    public static final int GL_WEIGHT_ARRAY_POINTER_OES = 34476;
    public static final int GL_WEIGHT_ARRAY_SIZE_OES = 34475;
    public static final int GL_WEIGHT_ARRAY_STRIDE_OES = 34474;
    public static final int GL_WEIGHT_ARRAY_TYPE_OES = 34473;

    void glCurrentPaletteMatrixOES(int i);

    void glDrawTexfOES(float f, float f2, float f3, float f4, float f5);

    void glDrawTexfvOES(FloatBuffer floatBuffer);

    void glDrawTexfvOES(float[] fArr, int i);

    void glDrawTexiOES(int i, int i2, int i3, int i4, int i5);

    void glDrawTexivOES(IntBuffer intBuffer);

    void glDrawTexivOES(int[] iArr, int i);

    void glDrawTexsOES(short s, short s2, short s3, short s4, short s5);

    void glDrawTexsvOES(ShortBuffer shortBuffer);

    void glDrawTexsvOES(short[] sArr, int i);

    void glDrawTexxOES(int i, int i2, int i3, int i4, int i5);

    void glDrawTexxvOES(IntBuffer intBuffer);

    void glDrawTexxvOES(int[] iArr, int i);

    void glEnable(int i);

    void glEnableClientState(int i);

    void glLoadPaletteFromModelViewMatrixOES();

    void glMatrixIndexPointerOES(int i, int i2, int i3, int i4);

    void glMatrixIndexPointerOES(int i, int i2, int i3, Buffer buffer);

    void glTexParameterfv(int i, int i2, float[] fArr, int i3);

    void glWeightPointerOES(int i, int i2, int i3, int i4);

    void glWeightPointerOES(int i, int i2, int i3, Buffer buffer);
}
