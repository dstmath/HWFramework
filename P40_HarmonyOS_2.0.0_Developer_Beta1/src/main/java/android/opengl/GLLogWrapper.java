package android.opengl;

import android.app.backup.FullBackup;
import android.content.Context;
import android.media.MediaFormat;
import android.net.TrafficStats;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.IncidentManager;
import android.provider.CallLog;
import android.provider.Telephony;
import android.rms.AppAssociate;
import android.rms.iaware.AwareConstant;
import com.android.internal.app.DumpHeapActivity;
import java.io.IOException;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import javax.microedition.khronos.opengles.GL;

/* access modifiers changed from: package-private */
public class GLLogWrapper extends GLWrapperBase {
    private static final int FORMAT_FIXED = 2;
    private static final int FORMAT_FLOAT = 1;
    private static final int FORMAT_INT = 0;
    private int mArgCount;
    boolean mColorArrayEnabled;
    private PointerInfo mColorPointer = new PointerInfo();
    private Writer mLog;
    private boolean mLogArgumentNames;
    boolean mNormalArrayEnabled;
    private PointerInfo mNormalPointer = new PointerInfo();
    StringBuilder mStringBuilder;
    private PointerInfo mTexCoordPointer = new PointerInfo();
    boolean mTextureCoordArrayEnabled;
    boolean mVertexArrayEnabled;
    private PointerInfo mVertexPointer = new PointerInfo();

    public GLLogWrapper(GL gl, Writer log, boolean logArgumentNames) {
        super(gl);
        this.mLog = log;
        this.mLogArgumentNames = logArgumentNames;
    }

    private void checkError() {
        int glError = this.mgl.glGetError();
        if (glError != 0) {
            logLine("glError: " + Integer.toString(glError));
        }
    }

    private void logLine(String message) {
        log(message + '\n');
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

    private void arg(String name, boolean value) {
        arg(name, Boolean.toString(value));
    }

    private void arg(String name, int value) {
        arg(name, Integer.toString(value));
    }

    private void arg(String name, float value) {
        arg(name, Float.toString(value));
    }

    private void returns(String result) {
        log(") returns " + result + ";\n");
        flush();
    }

    private void returns(int result) {
        returns(Integer.toString(result));
    }

    private void arg(String name, int n, int[] arr, int offset) {
        arg(name, toString(n, 0, arr, offset));
    }

    private void arg(String name, int n, short[] arr, int offset) {
        arg(name, toString(n, arr, offset));
    }

    private void arg(String name, int n, float[] arr, int offset) {
        arg(name, toString(n, arr, offset));
    }

    private void formattedAppend(StringBuilder buf, int value, int format) {
        if (format == 0) {
            buf.append(value);
        } else if (format == 1) {
            buf.append(Float.intBitsToFloat(value));
        } else if (format == 2) {
            buf.append(((float) value) / 65536.0f);
        }
    }

    private String toString(int n, int format, int[] arr, int offset) {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        int arrLen = arr.length;
        for (int i = 0; i < n; i++) {
            int index = offset + i;
            buf.append(" [" + index + "] = ");
            if (index < 0 || index >= arrLen) {
                buf.append("out of bounds");
            } else {
                formattedAppend(buf, arr[index], format);
            }
            buf.append('\n');
        }
        buf.append("}");
        return buf.toString();
    }

    private String toString(int n, short[] arr, int offset) {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        int arrLen = arr.length;
        for (int i = 0; i < n; i++) {
            int index = offset + i;
            buf.append(" [" + index + "] = ");
            if (index < 0 || index >= arrLen) {
                buf.append("out of bounds");
            } else {
                buf.append((int) arr[index]);
            }
            buf.append('\n');
        }
        buf.append("}");
        return buf.toString();
    }

    private String toString(int n, float[] arr, int offset) {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        int arrLen = arr.length;
        for (int i = 0; i < n; i++) {
            int index = offset + i;
            buf.append("[" + index + "] = ");
            if (index < 0 || index >= arrLen) {
                buf.append("out of bounds");
            } else {
                buf.append(arr[index]);
            }
            buf.append('\n');
        }
        buf.append("}");
        return buf.toString();
    }

    private String toString(int n, FloatBuffer buf) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < n; i++) {
            builder.append(" [" + i + "] = " + buf.get(i) + '\n');
        }
        builder.append("}");
        return builder.toString();
    }

    private String toString(int n, int format, IntBuffer buf) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < n; i++) {
            builder.append(" [" + i + "] = ");
            formattedAppend(builder, buf.get(i), format);
            builder.append('\n');
        }
        builder.append("}");
        return builder.toString();
    }

    private String toString(int n, ShortBuffer buf) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < n; i++) {
            builder.append(" [" + i + "] = " + ((int) buf.get(i)) + '\n');
        }
        builder.append("}");
        return builder.toString();
    }

    private void arg(String name, int n, FloatBuffer buf) {
        arg(name, toString(n, buf));
    }

    private void arg(String name, int n, IntBuffer buf) {
        arg(name, toString(n, 0, buf));
    }

    private void arg(String name, int n, ShortBuffer buf) {
        arg(name, toString(n, buf));
    }

    private void argPointer(int size, int type, int stride, Buffer pointer) {
        arg(DumpHeapActivity.KEY_SIZE, size);
        arg("type", getPointerTypeName(type));
        arg(MediaFormat.KEY_STRIDE, stride);
        arg("pointer", pointer.toString());
    }

    private static String getHex(int value) {
        return "0x" + Integer.toHexString(value);
    }

    public static String getErrorString(int error) {
        if (error == 0) {
            return "GL_NO_ERROR";
        }
        switch (error) {
            case 1280:
                return "GL_INVALID_ENUM";
            case 1281:
                return "GL_INVALID_VALUE";
            case 1282:
                return "GL_INVALID_OPERATION";
            case 1283:
                return "GL_STACK_OVERFLOW";
            case 1284:
                return "GL_STACK_UNDERFLOW";
            case 1285:
                return "GL_OUT_OF_MEMORY";
            default:
                return getHex(error);
        }
    }

    private String getClearBufferMask(int mask) {
        StringBuilder b = new StringBuilder();
        if ((mask & 256) != 0) {
            b.append("GL_DEPTH_BUFFER_BIT");
            mask &= TrafficStats.TAG_NETWORK_STACK_RANGE_END;
        }
        if ((mask & 1024) != 0) {
            if (b.length() > 0) {
                b.append(" | ");
            }
            b.append("GL_STENCIL_BUFFER_BIT");
            mask &= -1025;
        }
        if ((mask & 16384) != 0) {
            if (b.length() > 0) {
                b.append(" | ");
            }
            b.append("GL_COLOR_BUFFER_BIT");
            mask &= -16385;
        }
        if (mask != 0) {
            if (b.length() > 0) {
                b.append(" | ");
            }
            b.append(getHex(mask));
        }
        return b.toString();
    }

    private String getFactor(int factor) {
        if (factor == 0) {
            return "GL_ZERO";
        }
        if (factor == 1) {
            return "GL_ONE";
        }
        switch (factor) {
            case 768:
                return "GL_SRC_COLOR";
            case 769:
                return "GL_ONE_MINUS_SRC_COLOR";
            case 770:
                return "GL_SRC_ALPHA";
            case 771:
                return "GL_ONE_MINUS_SRC_ALPHA";
            case 772:
                return "GL_DST_ALPHA";
            case 773:
                return "GL_ONE_MINUS_DST_ALPHA";
            case 774:
                return "GL_DST_COLOR";
            case 775:
                return "GL_ONE_MINUS_DST_COLOR";
            case 776:
                return "GL_SRC_ALPHA_SATURATE";
            default:
                return getHex(factor);
        }
    }

    private String getShadeModel(int model) {
        if (model == 7424) {
            return "GL_FLAT";
        }
        if (model != 7425) {
            return getHex(model);
        }
        return "GL_SMOOTH";
    }

    private String getTextureTarget(int target) {
        if (target != 3553) {
            return getHex(target);
        }
        return "GL_TEXTURE_2D";
    }

    private String getTextureEnvTarget(int target) {
        if (target != 8960) {
            return getHex(target);
        }
        return "GL_TEXTURE_ENV";
    }

    private String getTextureEnvPName(int pname) {
        if (pname == 8704) {
            return "GL_TEXTURE_ENV_MODE";
        }
        if (pname != 8705) {
            return getHex(pname);
        }
        return "GL_TEXTURE_ENV_COLOR";
    }

    private int getTextureEnvParamCount(int pname) {
        if (pname == 8704) {
            return 1;
        }
        if (pname != 8705) {
            return 0;
        }
        return 4;
    }

    private String getTextureEnvParamName(float param) {
        int iparam = (int) param;
        if (param != ((float) iparam)) {
            return Float.toString(param);
        }
        if (iparam == 260) {
            return "GL_ADD";
        }
        if (iparam == 3042) {
            return "GL_BLEND";
        }
        if (iparam == 7681) {
            return "GL_REPLACE";
        }
        if (iparam == 34160) {
            return "GL_COMBINE";
        }
        if (iparam == 8448) {
            return "GL_MODULATE";
        }
        if (iparam != 8449) {
            return getHex(iparam);
        }
        return "GL_DECAL";
    }

    private String getMatrixMode(int matrixMode) {
        switch (matrixMode) {
            case 5888:
                return "GL_MODELVIEW";
            case 5889:
                return "GL_PROJECTION";
            case 5890:
                return "GL_TEXTURE";
            default:
                return getHex(matrixMode);
        }
    }

    private String getClientState(int clientState) {
        switch (clientState) {
            case 32884:
                return "GL_VERTEX_ARRAY";
            case 32885:
                return "GL_NORMAL_ARRAY";
            case 32886:
                return "GL_COLOR_ARRAY";
            case 32887:
            default:
                return getHex(clientState);
            case 32888:
                return "GL_TEXTURE_COORD_ARRAY";
        }
    }

    private String getCap(int cap) {
        switch (cap) {
            case 2832:
                return "GL_POINT_SMOOTH";
            case 2848:
                return "GL_LINE_SMOOTH";
            case 2884:
                return "GL_CULL_FACE";
            case 2896:
                return "GL_LIGHTING";
            case 2903:
                return "GL_COLOR_MATERIAL";
            case 2912:
                return "GL_FOG";
            case 2929:
                return "GL_DEPTH_TEST";
            case 2960:
                return "GL_STENCIL_TEST";
            case 2977:
                return "GL_NORMALIZE";
            case 3008:
                return "GL_ALPHA_TEST";
            case 3024:
                return "GL_DITHER";
            case 3042:
                return "GL_BLEND";
            case 3058:
                return "GL_COLOR_LOGIC_OP";
            case 3089:
                return "GL_SCISSOR_TEST";
            case 3553:
                return "GL_TEXTURE_2D";
            case 32826:
                return "GL_RESCALE_NORMAL";
            case 32888:
                return "GL_TEXTURE_COORD_ARRAY";
            default:
                switch (cap) {
                    case 16384:
                        return "GL_LIGHT0";
                    case 16385:
                        return "GL_LIGHT1";
                    case 16386:
                        return "GL_LIGHT2";
                    case 16387:
                        return "GL_LIGHT3";
                    case 16388:
                        return "GL_LIGHT4";
                    case 16389:
                        return "GL_LIGHT5";
                    case 16390:
                        return "GL_LIGHT6";
                    case 16391:
                        return "GL_LIGHT7";
                    default:
                        switch (cap) {
                            case 32884:
                                return "GL_VERTEX_ARRAY";
                            case 32885:
                                return "GL_NORMAL_ARRAY";
                            case 32886:
                                return "GL_COLOR_ARRAY";
                            default:
                                switch (cap) {
                                    case 32925:
                                        return "GL_MULTISAMPLE";
                                    case 32926:
                                        return "GL_SAMPLE_ALPHA_TO_COVERAGE";
                                    case 32927:
                                        return "GL_SAMPLE_ALPHA_TO_ONE";
                                    case 32928:
                                        return "GL_SAMPLE_COVERAGE";
                                    default:
                                        return getHex(cap);
                                }
                        }
                }
        }
    }

    private String getTexturePName(int pname) {
        if (pname == 33169) {
            return "GL_GENERATE_MIPMAP";
        }
        if (pname == 35741) {
            return "GL_TEXTURE_CROP_RECT_OES";
        }
        switch (pname) {
            case 10240:
                return "GL_TEXTURE_MAG_FILTER";
            case 10241:
                return "GL_TEXTURE_MIN_FILTER";
            case 10242:
                return "GL_TEXTURE_WRAP_S";
            case 10243:
                return "GL_TEXTURE_WRAP_T";
            default:
                return getHex(pname);
        }
    }

    private String getTextureParamName(float param) {
        int iparam = (int) param;
        if (param != ((float) iparam)) {
            return Float.toString(param);
        }
        if (iparam == 9728) {
            return "GL_NEAREST";
        }
        if (iparam == 9729) {
            return "GL_LINEAR";
        }
        if (iparam == 10497) {
            return "GL_REPEAT";
        }
        if (iparam == 33071) {
            return "GL_CLAMP_TO_EDGE";
        }
        switch (iparam) {
            case 9984:
                return "GL_NEAREST_MIPMAP_NEAREST";
            case 9985:
                return "GL_LINEAR_MIPMAP_NEAREST";
            case 9986:
                return "GL_NEAREST_MIPMAP_LINEAR";
            case 9987:
                return "GL_LINEAR_MIPMAP_LINEAR";
            default:
                return getHex(iparam);
        }
    }

    private String getFogPName(int pname) {
        switch (pname) {
            case 2914:
                return "GL_FOG_DENSITY";
            case 2915:
                return "GL_FOG_START";
            case 2916:
                return "GL_FOG_END";
            case 2917:
                return "GL_FOG_MODE";
            case 2918:
                return "GL_FOG_COLOR";
            default:
                return getHex(pname);
        }
    }

    private int getFogParamCount(int pname) {
        switch (pname) {
            case 2914:
                return 1;
            case 2915:
                return 1;
            case 2916:
                return 1;
            case 2917:
                return 1;
            case 2918:
                return 4;
            default:
                return 0;
        }
    }

    private String getBeginMode(int mode) {
        switch (mode) {
            case 0:
                return "GL_POINTS";
            case 1:
                return "GL_LINES";
            case 2:
                return "GL_LINE_LOOP";
            case 3:
                return "GL_LINE_STRIP";
            case 4:
                return "GL_TRIANGLES";
            case 5:
                return "GL_TRIANGLE_STRIP";
            case 6:
                return "GL_TRIANGLE_FAN";
            default:
                return getHex(mode);
        }
    }

    private String getIndexType(int type) {
        if (type == 5121) {
            return "GL_UNSIGNED_BYTE";
        }
        if (type != 5123) {
            return getHex(type);
        }
        return "GL_UNSIGNED_SHORT";
    }

    private String getIntegerStateName(int pname) {
        switch (pname) {
            case 2834:
                return "GL_SMOOTH_POINT_SIZE_RANGE";
            case 2850:
                return "GL_SMOOTH_LINE_WIDTH_RANGE";
            case 3377:
                return "GL_MAX_LIGHTS";
            case 3379:
                return "GL_MAX_TEXTURE_SIZE";
            case 3382:
                return "GL_MAX_MODELVIEW_STACK_DEPTH";
            case 3384:
                return "GL_MAX_PROJECTION_STACK_DEPTH";
            case 3385:
                return "GL_MAX_TEXTURE_STACK_DEPTH";
            case 3386:
                return "GL_MAX_VIEWPORT_DIMS";
            case 3408:
                return "GL_SUBPIXEL_BITS";
            case 3410:
                return "GL_RED_BITS";
            case 3411:
                return "GL_GREEN_BITS";
            case 3412:
                return "GL_BLUE_BITS";
            case 3413:
                return "GL_ALPHA_BITS";
            case 3414:
                return "GL_DEPTH_BITS";
            case 3415:
                return "GL_STENCIL_BITS";
            case 33000:
                return "GL_MAX_ELEMENTS_VERTICES";
            case 33001:
                return "GL_MAX_ELEMENTS_INDICES";
            case 33901:
                return "GL_ALIASED_POINT_SIZE_RANGE";
            case 33902:
                return "GL_ALIASED_LINE_WIDTH_RANGE";
            case 34018:
                return "GL_MAX_TEXTURE_UNITS";
            case 34466:
                return "GL_NUM_COMPRESSED_TEXTURE_FORMATS";
            case 34467:
                return "GL_COMPRESSED_TEXTURE_FORMATS";
            case 35213:
                return "GL_MODELVIEW_MATRIX_FLOAT_AS_INT_BITS_OES";
            case 35214:
                return "GL_PROJECTION_MATRIX_FLOAT_AS_INT_BITS_OES";
            case 35215:
                return "GL_TEXTURE_MATRIX_FLOAT_AS_INT_BITS_OES";
            default:
                return getHex(pname);
        }
    }

    private int getIntegerStateSize(int pname) {
        switch (pname) {
            case 2834:
                return 2;
            case 2850:
                return 2;
            case 3377:
                return 1;
            case 3379:
                return 1;
            case 3382:
                return 1;
            case 3384:
                return 1;
            case 3385:
                return 1;
            case 3386:
                return 2;
            case 3408:
                return 1;
            case 3410:
                return 1;
            case 3411:
                return 1;
            case 3412:
                return 1;
            case 3413:
                return 1;
            case 3414:
                return 1;
            case 3415:
                return 1;
            case 33000:
                return 1;
            case 33001:
                return 1;
            case 33901:
                return 2;
            case 33902:
                return 2;
            case 34018:
                return 1;
            case 34466:
                return 1;
            case 34467:
                int[] buffer = new int[1];
                this.mgl.glGetIntegerv(34466, buffer, 0);
                return buffer[0];
            case 35213:
            case 35214:
            case 35215:
                return 16;
            default:
                return 0;
        }
    }

    private int getIntegerStateFormat(int pname) {
        switch (pname) {
            case 35213:
            case 35214:
            case 35215:
                return 1;
            default:
                return 0;
        }
    }

    private String getHintTarget(int target) {
        if (target == 33170) {
            return "GL_GENERATE_MIPMAP_HINT";
        }
        switch (target) {
            case 3152:
                return "GL_PERSPECTIVE_CORRECTION_HINT";
            case 3153:
                return "GL_POINT_SMOOTH_HINT";
            case 3154:
                return "GL_LINE_SMOOTH_HINT";
            case 3155:
                return "GL_POLYGON_SMOOTH_HINT";
            case 3156:
                return "GL_FOG_HINT";
            default:
                return getHex(target);
        }
    }

    private String getHintMode(int mode) {
        switch (mode) {
            case 4352:
                return "GL_DONT_CARE";
            case 4353:
                return "GL_FASTEST";
            case 4354:
                return "GL_NICEST";
            default:
                return getHex(mode);
        }
    }

    private String getFaceName(int face) {
        if (face != 1032) {
            return getHex(face);
        }
        return "GL_FRONT_AND_BACK";
    }

    private String getMaterialPName(int pname) {
        switch (pname) {
            case 4608:
                return "GL_AMBIENT";
            case 4609:
                return "GL_DIFFUSE";
            case 4610:
                return "GL_SPECULAR";
            default:
                switch (pname) {
                    case 5632:
                        return "GL_EMISSION";
                    case 5633:
                        return "GL_SHININESS";
                    case 5634:
                        return "GL_AMBIENT_AND_DIFFUSE";
                    default:
                        return getHex(pname);
                }
        }
    }

    private int getMaterialParamCount(int pname) {
        switch (pname) {
            case 4608:
                return 4;
            case 4609:
                return 4;
            case 4610:
                return 4;
            default:
                switch (pname) {
                    case 5632:
                        return 4;
                    case 5633:
                        return 1;
                    case 5634:
                        return 4;
                    default:
                        return 0;
                }
        }
    }

    private String getLightName(int light) {
        if (light < 16384 || light > 16391) {
            return getHex(light);
        }
        return "GL_LIGHT" + Integer.toString(light);
    }

    private String getLightPName(int pname) {
        switch (pname) {
            case 4608:
                return "GL_AMBIENT";
            case 4609:
                return "GL_DIFFUSE";
            case 4610:
                return "GL_SPECULAR";
            case 4611:
                return "GL_POSITION";
            case 4612:
                return "GL_SPOT_DIRECTION";
            case 4613:
                return "GL_SPOT_EXPONENT";
            case 4614:
                return "GL_SPOT_CUTOFF";
            case 4615:
                return "GL_CONSTANT_ATTENUATION";
            case 4616:
                return "GL_LINEAR_ATTENUATION";
            case 4617:
                return "GL_QUADRATIC_ATTENUATION";
            default:
                return getHex(pname);
        }
    }

    private int getLightParamCount(int pname) {
        switch (pname) {
            case 4608:
                return 4;
            case 4609:
                return 4;
            case 4610:
                return 4;
            case 4611:
                return 4;
            case 4612:
                return 3;
            case 4613:
                return 1;
            case 4614:
                return 1;
            case 4615:
                return 1;
            case 4616:
                return 1;
            case 4617:
                return 1;
            default:
                return 0;
        }
    }

    private String getLightModelPName(int pname) {
        if (pname == 2898) {
            return "GL_LIGHT_MODEL_TWO_SIDE";
        }
        if (pname != 2899) {
            return getHex(pname);
        }
        return "GL_LIGHT_MODEL_AMBIENT";
    }

    private int getLightModelParamCount(int pname) {
        if (pname == 2898) {
            return 1;
        }
        if (pname != 2899) {
            return 0;
        }
        return 4;
    }

    private String getPointerTypeName(int type) {
        if (type == 5126) {
            return "GL_FLOAT";
        }
        if (type == 5132) {
            return "GL_FIXED";
        }
        switch (type) {
            case 5120:
                return "GL_BYTE";
            case 5121:
                return "GL_UNSIGNED_BYTE";
            case 5122:
                return "GL_SHORT";
            default:
                return getHex(type);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ByteBuffer toByteBuffer(int byteCount, Buffer input) {
        ByteBuffer result;
        boolean convertWholeBuffer = byteCount < 0;
        if (input instanceof ByteBuffer) {
            ByteBuffer input2 = (ByteBuffer) input;
            int position = input2.position();
            if (convertWholeBuffer) {
                byteCount = input2.limit() - position;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            for (int i = 0; i < byteCount; i++) {
                result.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof CharBuffer) {
            CharBuffer input22 = (CharBuffer) input;
            int position2 = input22.position();
            if (convertWholeBuffer) {
                byteCount = (input22.limit() - position2) * 2;
            }
            result = ByteBuffer.allocate(byteCount).order(input22.order());
            CharBuffer result2 = result.asCharBuffer();
            for (int i2 = 0; i2 < byteCount / 2; i2++) {
                result2.put(input22.get());
            }
            input22.position(position2);
        } else if (input instanceof ShortBuffer) {
            ShortBuffer input23 = (ShortBuffer) input;
            int position3 = input23.position();
            if (convertWholeBuffer) {
                byteCount = (input23.limit() - position3) * 2;
            }
            result = ByteBuffer.allocate(byteCount).order(input23.order());
            ShortBuffer result22 = result.asShortBuffer();
            for (int i3 = 0; i3 < byteCount / 2; i3++) {
                result22.put(input23.get());
            }
            input23.position(position3);
        } else if (input instanceof IntBuffer) {
            IntBuffer input24 = (IntBuffer) input;
            int position4 = input24.position();
            if (convertWholeBuffer) {
                byteCount = (input24.limit() - position4) * 4;
            }
            result = ByteBuffer.allocate(byteCount).order(input24.order());
            IntBuffer result23 = result.asIntBuffer();
            for (int i4 = 0; i4 < byteCount / 4; i4++) {
                result23.put(input24.get());
            }
            input24.position(position4);
        } else if (input instanceof FloatBuffer) {
            FloatBuffer input25 = (FloatBuffer) input;
            int position5 = input25.position();
            if (convertWholeBuffer) {
                byteCount = (input25.limit() - position5) * 4;
            }
            result = ByteBuffer.allocate(byteCount).order(input25.order());
            FloatBuffer result24 = result.asFloatBuffer();
            for (int i5 = 0; i5 < byteCount / 4; i5++) {
                result24.put(input25.get());
            }
            input25.position(position5);
        } else if (input instanceof DoubleBuffer) {
            DoubleBuffer input26 = (DoubleBuffer) input;
            int position6 = input26.position();
            if (convertWholeBuffer) {
                byteCount = (input26.limit() - position6) * 8;
            }
            result = ByteBuffer.allocate(byteCount).order(input26.order());
            DoubleBuffer result25 = result.asDoubleBuffer();
            for (int i6 = 0; i6 < byteCount / 8; i6++) {
                result25.put(input26.get());
            }
            input26.position(position6);
        } else if (input instanceof LongBuffer) {
            LongBuffer input27 = (LongBuffer) input;
            int position7 = input27.position();
            if (convertWholeBuffer) {
                byteCount = (input27.limit() - position7) * 8;
            }
            result = ByteBuffer.allocate(byteCount).order(input27.order());
            LongBuffer result26 = result.asLongBuffer();
            for (int i7 = 0; i7 < byteCount / 8; i7++) {
                result26.put(input27.get());
            }
            input27.position(position7);
        } else {
            throw new RuntimeException("Unimplemented Buffer subclass.");
        }
        result.rewind();
        result.order(ByteOrder.nativeOrder());
        return result;
    }

    private char[] toCharIndices(int count, int type, Buffer indices) {
        CharBuffer charBuffer;
        char[] result = new char[count];
        if (type == 5121) {
            ByteBuffer byteBuffer = toByteBuffer(count, indices);
            byte[] array = byteBuffer.array();
            int offset = byteBuffer.arrayOffset();
            for (int i = 0; i < count; i++) {
                result[i] = (char) (array[offset + i] & 255);
            }
        } else if (type == 5123) {
            if (indices instanceof CharBuffer) {
                charBuffer = (CharBuffer) indices;
            } else {
                charBuffer = toByteBuffer(count * 2, indices).asCharBuffer();
            }
            int oldPosition = charBuffer.position();
            charBuffer.position(0);
            charBuffer.get(result);
            charBuffer.position(oldPosition);
        }
        return result;
    }

    private void doArrayElement(StringBuilder builder, boolean enabled, String name, PointerInfo pointer, int index) {
        if (enabled) {
            builder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            builder.append(name + ":{");
            if (pointer == null || pointer.mTempByteBuffer == null) {
                builder.append("undefined }");
            } else if (pointer.mStride < 0) {
                builder.append("invalid stride");
            } else {
                int stride = pointer.getStride();
                ByteBuffer byteBuffer = pointer.mTempByteBuffer;
                int size = pointer.mSize;
                int type = pointer.mType;
                int sizeofType = pointer.sizeof(type);
                int byteOffset = stride * index;
                for (int i = 0; i < size; i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    if (type == 5126) {
                        builder.append(Float.toString(byteBuffer.asFloatBuffer().get(byteOffset / 4)));
                    } else if (type != 5132) {
                        switch (type) {
                            case 5120:
                                builder.append(Integer.toString(byteBuffer.get(byteOffset)));
                                continue;
                            case 5121:
                                builder.append(Integer.toString(byteBuffer.get(byteOffset) & 255));
                                continue;
                            case 5122:
                                builder.append(Integer.toString(byteBuffer.asShortBuffer().get(byteOffset / 2)));
                                continue;
                            default:
                                builder.append("?");
                                continue;
                        }
                    } else {
                        builder.append(Integer.toString(byteBuffer.asIntBuffer().get(byteOffset / 4)));
                    }
                    byteOffset += sizeofType;
                }
                builder.append("}");
            }
        }
    }

    private void doElement(StringBuilder builder, int ordinal, int vertexIndex) {
        builder.append(" [" + ordinal + " : " + vertexIndex + "] =");
        doArrayElement(builder, this.mVertexArrayEnabled, Telephony.BaseMmsColumns.MMS_VERSION, this.mVertexPointer, vertexIndex);
        doArrayElement(builder, this.mNormalArrayEnabled, "n", this.mNormalPointer, vertexIndex);
        doArrayElement(builder, this.mColorArrayEnabled, FullBackup.CACHE_TREE_TOKEN, this.mColorPointer, vertexIndex);
        doArrayElement(builder, this.mTextureCoordArrayEnabled, IncidentManager.URI_PARAM_TIMESTAMP, this.mTexCoordPointer, vertexIndex);
        builder.append("\n");
    }

    private void bindArrays() {
        if (this.mColorArrayEnabled) {
            this.mColorPointer.bindByteBuffer();
        }
        if (this.mNormalArrayEnabled) {
            this.mNormalPointer.bindByteBuffer();
        }
        if (this.mTextureCoordArrayEnabled) {
            this.mTexCoordPointer.bindByteBuffer();
        }
        if (this.mVertexArrayEnabled) {
            this.mVertexPointer.bindByteBuffer();
        }
    }

    private void unbindArrays() {
        if (this.mColorArrayEnabled) {
            this.mColorPointer.unbindByteBuffer();
        }
        if (this.mNormalArrayEnabled) {
            this.mNormalPointer.unbindByteBuffer();
        }
        if (this.mTextureCoordArrayEnabled) {
            this.mTexCoordPointer.unbindByteBuffer();
        }
        if (this.mVertexArrayEnabled) {
            this.mVertexPointer.unbindByteBuffer();
        }
    }

    private void startLogIndices() {
        this.mStringBuilder = new StringBuilder();
        this.mStringBuilder.append("\n");
        bindArrays();
    }

    private void endLogIndices() {
        log(this.mStringBuilder.toString());
        unbindArrays();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glActiveTexture(int texture) {
        begin("glActiveTexture");
        arg("texture", texture);
        end();
        this.mgl.glActiveTexture(texture);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glAlphaFunc(int func, float ref) {
        begin("glAlphaFunc");
        arg("func", func);
        arg("ref", ref);
        end();
        this.mgl.glAlphaFunc(func, ref);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glAlphaFuncx(int func, int ref) {
        begin("glAlphaFuncx");
        arg("func", func);
        arg("ref", ref);
        end();
        this.mgl.glAlphaFuncx(func, ref);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBindTexture(int target, int texture) {
        begin("glBindTexture");
        arg("target", getTextureTarget(target));
        arg("texture", texture);
        end();
        this.mgl.glBindTexture(target, texture);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glBlendFunc(int sfactor, int dfactor) {
        begin("glBlendFunc");
        arg("sfactor", getFactor(sfactor));
        arg("dfactor", getFactor(dfactor));
        end();
        this.mgl.glBlendFunc(sfactor, dfactor);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClear(int mask) {
        begin("glClear");
        arg("mask", getClearBufferMask(mask));
        end();
        this.mgl.glClear(mask);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClearColor(float red, float green, float blue, float alpha) {
        begin("glClearColor");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg(AppAssociate.ASSOC_WINDOW_ALPHA, alpha);
        end();
        this.mgl.glClearColor(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClearColorx(int red, int green, int blue, int alpha) {
        begin("glClearColor");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg(AppAssociate.ASSOC_WINDOW_ALPHA, alpha);
        end();
        this.mgl.glClearColorx(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClearDepthf(float depth) {
        begin("glClearDepthf");
        arg("depth", depth);
        end();
        this.mgl.glClearDepthf(depth);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClearDepthx(int depth) {
        begin("glClearDepthx");
        arg("depth", depth);
        end();
        this.mgl.glClearDepthx(depth);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClearStencil(int s) {
        begin("glClearStencil");
        arg("s", s);
        end();
        this.mgl.glClearStencil(s);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glClientActiveTexture(int texture) {
        begin("glClientActiveTexture");
        arg("texture", texture);
        end();
        this.mgl.glClientActiveTexture(texture);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glColor4f(float red, float green, float blue, float alpha) {
        begin("glColor4f");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg(AppAssociate.ASSOC_WINDOW_ALPHA, alpha);
        end();
        this.mgl.glColor4f(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glColor4x(int red, int green, int blue, int alpha) {
        begin("glColor4x");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg(AppAssociate.ASSOC_WINDOW_ALPHA, alpha);
        end();
        this.mgl.glColor4x(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        begin("glColorMask");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg(AppAssociate.ASSOC_WINDOW_ALPHA, alpha);
        end();
        this.mgl.glColorMask(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glColorPointer(int size, int type, int stride, Buffer pointer) {
        begin("glColorPointer");
        argPointer(size, type, stride, pointer);
        end();
        this.mColorPointer = new PointerInfo(size, type, stride, pointer);
        this.mgl.glColorPointer(size, type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data) {
        begin("glCompressedTexImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("internalformat", internalformat);
        arg("width", width);
        arg("height", height);
        arg("border", border);
        arg("imageSize", imageSize);
        arg("data", data.toString());
        end();
        this.mgl.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data) {
        begin("glCompressedTexSubImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("xoffset", xoffset);
        arg("yoffset", yoffset);
        arg("width", width);
        arg("height", height);
        arg("format", format);
        arg("imageSize", imageSize);
        arg("data", data.toString());
        end();
        this.mgl.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border) {
        begin("glCopyTexImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("internalformat", internalformat);
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        arg("border", border);
        end();
        this.mgl.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
        begin("glCopyTexSubImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("xoffset", xoffset);
        arg("yoffset", yoffset);
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glCullFace(int mode) {
        begin("glCullFace");
        arg("mode", mode);
        end();
        this.mgl.glCullFace(mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDeleteTextures(int n, int[] textures, int offset) {
        begin("glDeleteTextures");
        arg("n", n);
        arg("textures", n, textures, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glDeleteTextures(n, textures, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDeleteTextures(int n, IntBuffer textures) {
        begin("glDeleteTextures");
        arg("n", n);
        arg("textures", n, textures);
        end();
        this.mgl.glDeleteTextures(n, textures);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDepthFunc(int func) {
        begin("glDepthFunc");
        arg("func", func);
        end();
        this.mgl.glDepthFunc(func);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDepthMask(boolean flag) {
        begin("glDepthMask");
        arg(AwareConstant.Database.HwPkgRecord.FLAG, flag);
        end();
        this.mgl.glDepthMask(flag);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDepthRangef(float near, float far) {
        begin("glDepthRangef");
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glDepthRangef(near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDepthRangex(int near, int far) {
        begin("glDepthRangex");
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glDepthRangex(near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDisable(int cap) {
        begin("glDisable");
        arg("cap", getCap(cap));
        end();
        this.mgl.glDisable(cap);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDisableClientState(int array) {
        begin("glDisableClientState");
        arg("array", getClientState(array));
        end();
        switch (array) {
            case 32884:
                this.mVertexArrayEnabled = false;
                break;
            case 32885:
                this.mNormalArrayEnabled = false;
                break;
            case 32886:
                this.mColorArrayEnabled = false;
                break;
            case 32888:
                this.mTextureCoordArrayEnabled = false;
                break;
        }
        this.mgl.glDisableClientState(array);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDrawArrays(int mode, int first, int count) {
        begin("glDrawArrays");
        arg("mode", mode);
        arg("first", first);
        arg("count", count);
        startLogIndices();
        for (int i = 0; i < count; i++) {
            doElement(this.mStringBuilder, i, first + i);
        }
        endLogIndices();
        end();
        this.mgl.glDrawArrays(mode, first, count);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        begin("glDrawElements");
        arg("mode", getBeginMode(mode));
        arg("count", count);
        arg("type", getIndexType(type));
        char[] indexArray = toCharIndices(count, type, indices);
        int indexArrayLength = indexArray.length;
        startLogIndices();
        for (int i = 0; i < indexArrayLength; i++) {
            doElement(this.mStringBuilder, i, indexArray[i]);
        }
        endLogIndices();
        end();
        this.mgl.glDrawElements(mode, count, type, indices);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11Ext, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glEnable(int cap) {
        begin("glEnable");
        arg("cap", getCap(cap));
        end();
        this.mgl.glEnable(cap);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11Ext
    public void glEnableClientState(int array) {
        begin("glEnableClientState");
        arg("array", getClientState(array));
        end();
        switch (array) {
            case 32884:
                this.mVertexArrayEnabled = true;
                break;
            case 32885:
                this.mNormalArrayEnabled = true;
                break;
            case 32886:
                this.mColorArrayEnabled = true;
                break;
            case 32888:
                this.mTextureCoordArrayEnabled = true;
                break;
        }
        this.mgl.glEnableClientState(array);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFinish() {
        begin("glFinish");
        end();
        this.mgl.glFinish();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFlush() {
        begin("glFlush");
        end();
        this.mgl.glFlush();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogf(int pname, float param) {
        begin("glFogf");
        arg("pname", pname);
        arg("param", param);
        end();
        this.mgl.glFogf(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogfv(int pname, float[] params, int offset) {
        begin("glFogfv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glFogfv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogfv(int pname, FloatBuffer params) {
        begin("glFogfv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params);
        end();
        this.mgl.glFogfv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogx(int pname, int param) {
        begin("glFogx");
        arg("pname", getFogPName(pname));
        arg("param", param);
        end();
        this.mgl.glFogx(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogxv(int pname, int[] params, int offset) {
        begin("glFogxv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glFogxv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFogxv(int pname, IntBuffer params) {
        begin("glFogxv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params);
        end();
        this.mgl.glFogxv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFrontFace(int mode) {
        begin("glFrontFace");
        arg("mode", mode);
        end();
        this.mgl.glFrontFace(mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFrustumf(float left, float right, float bottom, float top, float near, float far) {
        begin("glFrustumf");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glFrustumf(left, right, bottom, top, near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glFrustumx(int left, int right, int bottom, int top, int near, int far) {
        begin("glFrustumx");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glFrustumx(left, right, bottom, top, near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glGenTextures(int n, int[] textures, int offset) {
        begin("glGenTextures");
        arg("n", n);
        arg("textures", Arrays.toString(textures));
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        this.mgl.glGenTextures(n, textures, offset);
        returns(toString(n, 0, textures, offset));
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glGenTextures(int n, IntBuffer textures) {
        begin("glGenTextures");
        arg("n", n);
        arg("textures", textures.toString());
        this.mgl.glGenTextures(n, textures);
        returns(toString(n, 0, textures));
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public int glGetError() {
        begin("glGetError");
        int result = this.mgl.glGetError();
        returns(result);
        return result;
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetIntegerv(int pname, int[] params, int offset) {
        begin("glGetIntegerv");
        arg("pname", getIntegerStateName(pname));
        arg("params", Arrays.toString(params));
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        this.mgl.glGetIntegerv(pname, params, offset);
        returns(toString(getIntegerStateSize(pname), getIntegerStateFormat(pname), params, offset));
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetIntegerv(int pname, IntBuffer params) {
        begin("glGetIntegerv");
        arg("pname", getIntegerStateName(pname));
        arg("params", params.toString());
        this.mgl.glGetIntegerv(pname, params);
        returns(toString(getIntegerStateSize(pname), getIntegerStateFormat(pname), params));
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public String glGetString(int name) {
        begin("glGetString");
        arg("name", name);
        String result = this.mgl.glGetString(name);
        returns(result);
        checkError();
        return result;
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glHint(int target, int mode) {
        begin("glHint");
        arg("target", getHintTarget(target));
        arg("mode", getHintMode(mode));
        end();
        this.mgl.glHint(target, mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelf(int pname, float param) {
        begin("glLightModelf");
        arg("pname", getLightModelPName(pname));
        arg("param", param);
        end();
        this.mgl.glLightModelf(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelfv(int pname, float[] params, int offset) {
        begin("glLightModelfv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glLightModelfv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelfv(int pname, FloatBuffer params) {
        begin("glLightModelfv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params);
        end();
        this.mgl.glLightModelfv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelx(int pname, int param) {
        begin("glLightModelx");
        arg("pname", getLightModelPName(pname));
        arg("param", param);
        end();
        this.mgl.glLightModelx(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelxv(int pname, int[] params, int offset) {
        begin("glLightModelxv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glLightModelxv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightModelxv(int pname, IntBuffer params) {
        begin("glLightModelfv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params);
        end();
        this.mgl.glLightModelxv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightf(int light, int pname, float param) {
        begin("glLightf");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("param", param);
        end();
        this.mgl.glLightf(light, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightfv(int light, int pname, float[] params, int offset) {
        begin("glLightfv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glLightfv(light, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightfv(int light, int pname, FloatBuffer params) {
        begin("glLightfv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params);
        end();
        this.mgl.glLightfv(light, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightx(int light, int pname, int param) {
        begin("glLightx");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("param", param);
        end();
        this.mgl.glLightx(light, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightxv(int light, int pname, int[] params, int offset) {
        begin("glLightxv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glLightxv(light, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLightxv(int light, int pname, IntBuffer params) {
        begin("glLightxv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params);
        end();
        this.mgl.glLightxv(light, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLineWidth(float width) {
        begin("glLineWidth");
        arg("width", width);
        end();
        this.mgl.glLineWidth(width);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLineWidthx(int width) {
        begin("glLineWidthx");
        arg("width", width);
        end();
        this.mgl.glLineWidthx(width);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLoadIdentity() {
        begin("glLoadIdentity");
        end();
        this.mgl.glLoadIdentity();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLoadMatrixf(float[] m, int offset) {
        begin("glLoadMatrixf");
        arg("m", 16, m, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glLoadMatrixf(m, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLoadMatrixf(FloatBuffer m) {
        begin("glLoadMatrixf");
        arg("m", 16, m);
        end();
        this.mgl.glLoadMatrixf(m);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLoadMatrixx(int[] m, int offset) {
        begin("glLoadMatrixx");
        arg("m", 16, m, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glLoadMatrixx(m, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLoadMatrixx(IntBuffer m) {
        begin("glLoadMatrixx");
        arg("m", 16, m);
        end();
        this.mgl.glLoadMatrixx(m);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glLogicOp(int opcode) {
        begin("glLogicOp");
        arg("opcode", opcode);
        end();
        this.mgl.glLogicOp(opcode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialf(int face, int pname, float param) {
        begin("glMaterialf");
        arg(Context.FACE_SERVICE, getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("param", param);
        end();
        this.mgl.glMaterialf(face, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialfv(int face, int pname, float[] params, int offset) {
        begin("glMaterialfv");
        arg(Context.FACE_SERVICE, getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glMaterialfv(face, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialfv(int face, int pname, FloatBuffer params) {
        begin("glMaterialfv");
        arg(Context.FACE_SERVICE, getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params);
        end();
        this.mgl.glMaterialfv(face, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialx(int face, int pname, int param) {
        begin("glMaterialx");
        arg(Context.FACE_SERVICE, getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("param", param);
        end();
        this.mgl.glMaterialx(face, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialxv(int face, int pname, int[] params, int offset) {
        begin("glMaterialxv");
        arg(Context.FACE_SERVICE, getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glMaterialxv(face, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMaterialxv(int face, int pname, IntBuffer params) {
        begin("glMaterialxv");
        arg(Context.FACE_SERVICE, getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params);
        end();
        this.mgl.glMaterialxv(face, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMatrixMode(int mode) {
        begin("glMatrixMode");
        arg("mode", getMatrixMode(mode));
        end();
        this.mgl.glMatrixMode(mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultMatrixf(float[] m, int offset) {
        begin("glMultMatrixf");
        arg("m", 16, m, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glMultMatrixf(m, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultMatrixf(FloatBuffer m) {
        begin("glMultMatrixf");
        arg("m", 16, m);
        end();
        this.mgl.glMultMatrixf(m);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultMatrixx(int[] m, int offset) {
        begin("glMultMatrixx");
        arg("m", 16, m, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glMultMatrixx(m, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultMatrixx(IntBuffer m) {
        begin("glMultMatrixx");
        arg("m", 16, m);
        end();
        this.mgl.glMultMatrixx(m);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultiTexCoord4f(int target, float s, float t, float r, float q) {
        begin("glMultiTexCoord4f");
        arg("target", target);
        arg("s", s);
        arg(IncidentManager.URI_PARAM_TIMESTAMP, t);
        arg("r", r);
        arg("q", q);
        end();
        this.mgl.glMultiTexCoord4f(target, s, t, r, q);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glMultiTexCoord4x(int target, int s, int t, int r, int q) {
        begin("glMultiTexCoord4x");
        arg("target", target);
        arg("s", s);
        arg(IncidentManager.URI_PARAM_TIMESTAMP, t);
        arg("r", r);
        arg("q", q);
        end();
        this.mgl.glMultiTexCoord4x(target, s, t, r, q);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glNormal3f(float nx, float ny, float nz) {
        begin("glNormal3f");
        arg("nx", nx);
        arg("ny", ny);
        arg("nz", nz);
        end();
        this.mgl.glNormal3f(nx, ny, nz);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glNormal3x(int nx, int ny, int nz) {
        begin("glNormal3x");
        arg("nx", nx);
        arg("ny", ny);
        arg("nz", nz);
        end();
        this.mgl.glNormal3x(nx, ny, nz);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glNormalPointer(int type, int stride, Buffer pointer) {
        begin("glNormalPointer");
        arg("type", type);
        arg(MediaFormat.KEY_STRIDE, stride);
        arg("pointer", pointer.toString());
        end();
        this.mNormalPointer = new PointerInfo(3, type, stride, pointer);
        this.mgl.glNormalPointer(type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glOrthof(float left, float right, float bottom, float top, float near, float far) {
        begin("glOrthof");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glOrthof(left, right, bottom, top, near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glOrthox(int left, int right, int bottom, int top, int near, int far) {
        begin("glOrthox");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glOrthox(left, right, bottom, top, near, far);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPixelStorei(int pname, int param) {
        begin("glPixelStorei");
        arg("pname", pname);
        arg("param", param);
        end();
        this.mgl.glPixelStorei(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPointSize(float size) {
        begin("glPointSize");
        arg(DumpHeapActivity.KEY_SIZE, size);
        end();
        this.mgl.glPointSize(size);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPointSizex(int size) {
        begin("glPointSizex");
        arg(DumpHeapActivity.KEY_SIZE, size);
        end();
        this.mgl.glPointSizex(size);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPolygonOffset(float factor, float units) {
        begin("glPolygonOffset");
        arg("factor", factor);
        arg("units", units);
        end();
        this.mgl.glPolygonOffset(factor, units);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPolygonOffsetx(int factor, int units) {
        begin("glPolygonOffsetx");
        arg("factor", factor);
        arg("units", units);
        end();
        this.mgl.glPolygonOffsetx(factor, units);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPopMatrix() {
        begin("glPopMatrix");
        end();
        this.mgl.glPopMatrix();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glPushMatrix() {
        begin("glPushMatrix");
        end();
        this.mgl.glPushMatrix();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
        begin("glReadPixels");
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        arg("format", format);
        arg("type", type);
        arg("pixels", pixels.toString());
        end();
        this.mgl.glReadPixels(x, y, width, height, format, type, pixels);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glRotatef(float angle, float x, float y, float z) {
        begin("glRotatef");
        arg("angle", angle);
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glRotatef(angle, x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glRotatex(int angle, int x, int y, int z) {
        begin("glRotatex");
        arg("angle", angle);
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glRotatex(angle, x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glSampleCoverage(float value, boolean invert) {
        begin("glSampleCoveragex");
        arg("value", value);
        arg("invert", invert);
        end();
        this.mgl.glSampleCoverage(value, invert);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glSampleCoveragex(int value, boolean invert) {
        begin("glSampleCoveragex");
        arg("value", value);
        arg("invert", invert);
        end();
        this.mgl.glSampleCoveragex(value, invert);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glScalef(float x, float y, float z) {
        begin("glScalef");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glScalef(x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glScalex(int x, int y, int z) {
        begin("glScalex");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glScalex(x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glScissor(int x, int y, int width, int height) {
        begin("glScissor");
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl.glScissor(x, y, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glShadeModel(int mode) {
        begin("glShadeModel");
        arg("mode", getShadeModel(mode));
        end();
        this.mgl.glShadeModel(mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glStencilFunc(int func, int ref, int mask) {
        begin("glStencilFunc");
        arg("func", func);
        arg("ref", ref);
        arg("mask", mask);
        end();
        this.mgl.glStencilFunc(func, ref, mask);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glStencilMask(int mask) {
        begin("glStencilMask");
        arg("mask", mask);
        end();
        this.mgl.glStencilMask(mask);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glStencilOp(int fail, int zfail, int zpass) {
        begin("glStencilOp");
        arg("fail", fail);
        arg("zfail", zfail);
        arg("zpass", zpass);
        end();
        this.mgl.glStencilOp(fail, zfail, zpass);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
        begin("glTexCoordPointer");
        argPointer(size, type, stride, pointer);
        end();
        this.mTexCoordPointer = new PointerInfo(size, type, stride, pointer);
        this.mgl.glTexCoordPointer(size, type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvf(int target, int pname, float param) {
        begin("glTexEnvf");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("param", getTextureEnvParamName(param));
        end();
        this.mgl.glTexEnvf(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvfv(int target, int pname, float[] params, int offset) {
        begin("glTexEnvfv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glTexEnvfv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvfv(int target, int pname, FloatBuffer params) {
        begin("glTexEnvfv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params);
        end();
        this.mgl.glTexEnvfv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvx(int target, int pname, int param) {
        begin("glTexEnvx");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("param", param);
        end();
        this.mgl.glTexEnvx(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvxv(int target, int pname, int[] params, int offset) {
        begin("glTexEnvxv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl.glTexEnvxv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexEnvxv(int target, int pname, IntBuffer params) {
        begin("glTexEnvxv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params);
        end();
        this.mgl.glTexEnvxv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels) {
        begin("glTexImage2D");
        arg("target", target);
        arg("level", level);
        arg("internalformat", internalformat);
        arg("width", width);
        arg("height", height);
        arg("border", border);
        arg("format", format);
        arg("type", type);
        arg("pixels", pixels.toString());
        end();
        this.mgl.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexParameterf(int target, int pname, float param) {
        begin("glTexParameterf");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("param", getTextureParamName(param));
        end();
        this.mgl.glTexParameterf(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTexParameterx(int target, int pname, int param) {
        begin("glTexParameterx");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("param", param);
        end();
        this.mgl.glTexParameterx(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameteriv(int target, int pname, int[] params, int offset) {
        begin("glTexParameteriv");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("params", 4, params, offset);
        end();
        this.mgl11.glTexParameteriv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameteriv(int target, int pname, IntBuffer params) {
        begin("glTexParameteriv");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("params", 4, params);
        end();
        this.mgl11.glTexParameteriv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels) {
        begin("glTexSubImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("xoffset", xoffset);
        arg("yoffset", yoffset);
        arg("width", width);
        arg("height", height);
        arg("format", format);
        arg("type", type);
        arg("pixels", pixels.toString());
        end();
        this.mgl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTranslatef(float x, float y, float z) {
        begin("glTranslatef");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glTranslatef(x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glTranslatex(int x, int y, int z) {
        begin("glTranslatex");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glTranslatex(x, y, z);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
        begin("glVertexPointer");
        argPointer(size, type, stride, pointer);
        end();
        this.mVertexPointer = new PointerInfo(size, type, stride, pointer);
        this.mgl.glVertexPointer(size, type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10
    public void glViewport(int x, int y, int width, int height) {
        begin("glViewport");
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl.glViewport(x, y, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glClipPlanef(int plane, float[] equation, int offset) {
        begin("glClipPlanef");
        arg("plane", plane);
        arg("equation", 4, equation, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glClipPlanef(plane, equation, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glClipPlanef(int plane, FloatBuffer equation) {
        begin("glClipPlanef");
        arg("plane", plane);
        arg("equation", 4, equation);
        end();
        this.mgl11.glClipPlanef(plane, equation);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glClipPlanex(int plane, int[] equation, int offset) {
        begin("glClipPlanex");
        arg("plane", plane);
        arg("equation", 4, equation, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glClipPlanex(plane, equation, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glClipPlanex(int plane, IntBuffer equation) {
        begin("glClipPlanef");
        arg("plane", plane);
        arg("equation", 4, equation);
        end();
        this.mgl11.glClipPlanex(plane, equation);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexfOES(float x, float y, float z, float width, float height) {
        begin("glDrawTexfOES");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl11Ext.glDrawTexfOES(x, y, z, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexfvOES(float[] coords, int offset) {
        begin("glDrawTexfvOES");
        arg("coords", 5, coords, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11Ext.glDrawTexfvOES(coords, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexfvOES(FloatBuffer coords) {
        begin("glDrawTexfvOES");
        arg("coords", 5, coords);
        end();
        this.mgl11Ext.glDrawTexfvOES(coords);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexiOES(int x, int y, int z, int width, int height) {
        begin("glDrawTexiOES");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl11Ext.glDrawTexiOES(x, y, z, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexivOES(int[] coords, int offset) {
        begin("glDrawTexivOES");
        arg("coords", 5, coords, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11Ext.glDrawTexivOES(coords, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexivOES(IntBuffer coords) {
        begin("glDrawTexivOES");
        arg("coords", 5, coords);
        end();
        this.mgl11Ext.glDrawTexivOES(coords);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexsOES(short x, short y, short z, short width, short height) {
        begin("glDrawTexsOES");
        arg("x", (int) x);
        arg("y", (int) y);
        arg("z", (int) z);
        arg("width", (int) width);
        arg("height", (int) height);
        end();
        this.mgl11Ext.glDrawTexsOES(x, y, z, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexsvOES(short[] coords, int offset) {
        begin("glDrawTexsvOES");
        arg("coords", 5, coords, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11Ext.glDrawTexsvOES(coords, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexsvOES(ShortBuffer coords) {
        begin("glDrawTexsvOES");
        arg("coords", 5, coords);
        end();
        this.mgl11Ext.glDrawTexsvOES(coords);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexxOES(int x, int y, int z, int width, int height) {
        begin("glDrawTexxOES");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl11Ext.glDrawTexxOES(x, y, z, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexxvOES(int[] coords, int offset) {
        begin("glDrawTexxvOES");
        arg("coords", 5, coords, offset);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11Ext.glDrawTexxvOES(coords, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glDrawTexxvOES(IntBuffer coords) {
        begin("glDrawTexxvOES");
        arg("coords", 5, coords);
        end();
        this.mgl11Ext.glDrawTexxvOES(coords);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL10Ext
    public int glQueryMatrixxOES(int[] mantissa, int mantissaOffset, int[] exponent, int exponentOffset) {
        begin("glQueryMatrixxOES");
        arg("mantissa", Arrays.toString(mantissa));
        arg("exponent", Arrays.toString(exponent));
        end();
        int valid = this.mgl10Ext.glQueryMatrixxOES(mantissa, mantissaOffset, exponent, exponentOffset);
        returns(toString(16, 2, mantissa, mantissaOffset));
        returns(toString(16, 0, exponent, exponentOffset));
        checkError();
        return valid;
    }

    @Override // javax.microedition.khronos.opengles.GL10Ext
    public int glQueryMatrixxOES(IntBuffer mantissa, IntBuffer exponent) {
        begin("glQueryMatrixxOES");
        arg("mantissa", mantissa.toString());
        arg("exponent", exponent.toString());
        end();
        int valid = this.mgl10Ext.glQueryMatrixxOES(mantissa, exponent);
        returns(toString(16, 2, mantissa));
        returns(toString(16, 0, exponent));
        checkError();
        return valid;
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glBindBuffer(int target, int buffer) {
        begin("glBindBuffer");
        arg("target", target);
        arg("buffer", buffer);
        end();
        this.mgl11.glBindBuffer(target, buffer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glBufferData(int target, int size, Buffer data, int usage) {
        begin("glBufferData");
        arg("target", target);
        arg(DumpHeapActivity.KEY_SIZE, size);
        arg("data", data.toString());
        arg("usage", usage);
        end();
        this.mgl11.glBufferData(target, size, data, usage);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glBufferSubData(int target, int offset, int size, Buffer data) {
        begin("glBufferSubData");
        arg("target", target);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        arg(DumpHeapActivity.KEY_SIZE, size);
        arg("data", data.toString());
        end();
        this.mgl11.glBufferSubData(target, offset, size, data);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glColor4ub(byte red, byte green, byte blue, byte alpha) {
        begin("glColor4ub");
        arg("red", (int) red);
        arg("green", (int) green);
        arg("blue", (int) blue);
        arg(AppAssociate.ASSOC_WINDOW_ALPHA, (int) alpha);
        end();
        this.mgl11.glColor4ub(red, green, blue, alpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        begin("glDeleteBuffers");
        arg("n", n);
        arg("buffers", buffers.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glDeleteBuffers(n, buffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glDeleteBuffers(int n, IntBuffer buffers) {
        begin("glDeleteBuffers");
        arg("n", n);
        arg("buffers", buffers.toString());
        end();
        this.mgl11.glDeleteBuffers(n, buffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGenBuffers(int n, int[] buffers, int offset) {
        begin("glGenBuffers");
        arg("n", n);
        arg("buffers", buffers.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGenBuffers(n, buffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGenBuffers(int n, IntBuffer buffers) {
        begin("glGenBuffers");
        arg("n", n);
        arg("buffers", buffers.toString());
        end();
        this.mgl11.glGenBuffers(n, buffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetBooleanv(int pname, boolean[] params, int offset) {
        begin("glGetBooleanv");
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetBooleanv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetBooleanv(int pname, IntBuffer params) {
        begin("glGetBooleanv");
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetBooleanv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetBufferParameteriv(int target, int pname, int[] params, int offset) {
        begin("glGetBufferParameteriv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetBufferParameteriv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
        begin("glGetBufferParameteriv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetBufferParameteriv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetClipPlanef(int pname, float[] eqn, int offset) {
        begin("glGetClipPlanef");
        arg("pname", pname);
        arg("eqn", eqn.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetClipPlanef(pname, eqn, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetClipPlanef(int pname, FloatBuffer eqn) {
        begin("glGetClipPlanef");
        arg("pname", pname);
        arg("eqn", eqn.toString());
        end();
        this.mgl11.glGetClipPlanef(pname, eqn);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetClipPlanex(int pname, int[] eqn, int offset) {
        begin("glGetClipPlanex");
        arg("pname", pname);
        arg("eqn", eqn.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetClipPlanex(pname, eqn, offset);
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetClipPlanex(int pname, IntBuffer eqn) {
        begin("glGetClipPlanex");
        arg("pname", pname);
        arg("eqn", eqn.toString());
        end();
        this.mgl11.glGetClipPlanex(pname, eqn);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetFixedv(int pname, int[] params, int offset) {
        begin("glGetFixedv");
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetFixedv(pname, params, offset);
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetFixedv(int pname, IntBuffer params) {
        begin("glGetFixedv");
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetFixedv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetFloatv(int pname, float[] params, int offset) {
        begin("glGetFloatv");
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetFloatv(pname, params, offset);
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetFloatv(int pname, FloatBuffer params) {
        begin("glGetFloatv");
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetFloatv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetLightfv(int light, int pname, float[] params, int offset) {
        begin("glGetLightfv");
        arg("light", light);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetLightfv(light, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetLightfv(int light, int pname, FloatBuffer params) {
        begin("glGetLightfv");
        arg("light", light);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetLightfv(light, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetLightxv(int light, int pname, int[] params, int offset) {
        begin("glGetLightxv");
        arg("light", light);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetLightxv(light, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetLightxv(int light, int pname, IntBuffer params) {
        begin("glGetLightxv");
        arg("light", light);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetLightxv(light, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetMaterialfv(int face, int pname, float[] params, int offset) {
        begin("glGetMaterialfv");
        arg(Context.FACE_SERVICE, face);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetMaterialfv(face, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetMaterialfv(int face, int pname, FloatBuffer params) {
        begin("glGetMaterialfv");
        arg(Context.FACE_SERVICE, face);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetMaterialfv(face, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetMaterialxv(int face, int pname, int[] params, int offset) {
        begin("glGetMaterialxv");
        arg(Context.FACE_SERVICE, face);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetMaterialxv(face, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetMaterialxv(int face, int pname, IntBuffer params) {
        begin("glGetMaterialxv");
        arg(Context.FACE_SERVICE, face);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetMaterialxv(face, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexEnviv(int env, int pname, int[] params, int offset) {
        begin("glGetTexEnviv");
        arg("env", env);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetTexEnviv(env, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexEnviv(int env, int pname, IntBuffer params) {
        begin("glGetTexEnviv");
        arg("env", env);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetTexEnviv(env, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexEnvxv(int env, int pname, int[] params, int offset) {
        begin("glGetTexEnviv");
        arg("env", env);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetTexEnviv(env, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexEnvxv(int env, int pname, IntBuffer params) {
        begin("glGetTexEnviv");
        arg("env", env);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetTexEnvxv(env, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameterfv(int target, int pname, float[] params, int offset) {
        begin("glGetTexParameterfv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetTexParameterfv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
        begin("glGetTexParameterfv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetTexParameterfv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameteriv(int target, int pname, int[] params, int offset) {
        begin("glGetTexParameteriv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetTexEnviv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
        begin("glGetTexParameteriv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetTexParameteriv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameterxv(int target, int pname, int[] params, int offset) {
        begin("glGetTexParameterxv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glGetTexParameterxv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetTexParameterxv(int target, int pname, IntBuffer params) {
        begin("glGetTexParameterxv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetTexParameterxv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public boolean glIsBuffer(int buffer) {
        begin("glIsBuffer");
        arg("buffer", buffer);
        end();
        boolean result = this.mgl11.glIsBuffer(buffer);
        checkError();
        return result;
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public boolean glIsEnabled(int cap) {
        begin("glIsEnabled");
        arg("cap", cap);
        end();
        boolean result = this.mgl11.glIsEnabled(cap);
        checkError();
        return result;
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public boolean glIsTexture(int texture) {
        begin("glIsTexture");
        arg("texture", texture);
        end();
        boolean result = this.mgl11.glIsTexture(texture);
        checkError();
        return result;
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterf(int pname, float param) {
        begin("glPointParameterf");
        arg("pname", pname);
        arg("param", param);
        end();
        this.mgl11.glPointParameterf(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterfv(int pname, float[] params, int offset) {
        begin("glPointParameterfv");
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glPointParameterfv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterfv(int pname, FloatBuffer params) {
        begin("glPointParameterfv");
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glPointParameterfv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterx(int pname, int param) {
        begin("glPointParameterfv");
        arg("pname", pname);
        arg("param", param);
        end();
        this.mgl11.glPointParameterx(pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterxv(int pname, int[] params, int offset) {
        begin("glPointParameterxv");
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glPointParameterxv(pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointParameterxv(int pname, IntBuffer params) {
        begin("glPointParameterxv");
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glPointParameterxv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glPointSizePointerOES(int type, int stride, Buffer pointer) {
        begin("glPointSizePointerOES");
        arg("type", type);
        arg(MediaFormat.KEY_STRIDE, stride);
        arg("params", pointer.toString());
        end();
        this.mgl11.glPointSizePointerOES(type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexEnvi(int target, int pname, int param) {
        begin("glTexEnvi");
        arg("target", target);
        arg("pname", pname);
        arg("param", param);
        end();
        this.mgl11.glTexEnvi(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexEnviv(int target, int pname, int[] params, int offset) {
        begin("glTexEnviv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glTexEnviv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexEnviv(int target, int pname, IntBuffer params) {
        begin("glTexEnviv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glTexEnviv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11, javax.microedition.khronos.opengles.GL11Ext
    public void glTexParameterfv(int target, int pname, float[] params, int offset) {
        begin("glTexParameterfv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glTexParameterfv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameterfv(int target, int pname, FloatBuffer params) {
        begin("glTexParameterfv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glTexParameterfv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameteri(int target, int pname, int param) {
        begin("glTexParameterxv");
        arg("target", target);
        arg("pname", pname);
        arg("param", param);
        end();
        this.mgl11.glTexParameteri(target, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameterxv(int target, int pname, int[] params, int offset) {
        begin("glTexParameterxv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glTexParameterxv(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexParameterxv(int target, int pname, IntBuffer params) {
        begin("glTexParameterxv");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glTexParameterxv(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glColorPointer(int size, int type, int stride, int offset) {
        begin("glColorPointer");
        arg(DumpHeapActivity.KEY_SIZE, size);
        arg("type", type);
        arg(MediaFormat.KEY_STRIDE, stride);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glColorPointer(size, type, stride, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glDrawElements(int mode, int count, int type, int offset) {
        begin("glDrawElements");
        arg("mode", mode);
        arg("count", count);
        arg("type", type);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glDrawElements(mode, count, type, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glGetPointerv(int pname, Buffer[] params) {
        begin("glGetPointerv");
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11.glGetPointerv(pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glNormalPointer(int type, int stride, int offset) {
        begin("glNormalPointer");
        arg("type", type);
        arg(MediaFormat.KEY_STRIDE, stride);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glNormalPointer(type, stride, offset);
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glTexCoordPointer(int size, int type, int stride, int offset) {
        begin("glTexCoordPointer");
        arg(DumpHeapActivity.KEY_SIZE, size);
        arg("type", type);
        arg(MediaFormat.KEY_STRIDE, stride);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glTexCoordPointer(size, type, stride, offset);
    }

    @Override // javax.microedition.khronos.opengles.GL11
    public void glVertexPointer(int size, int type, int stride, int offset) {
        begin("glVertexPointer");
        arg(DumpHeapActivity.KEY_SIZE, size);
        arg("type", type);
        arg(MediaFormat.KEY_STRIDE, stride);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11.glVertexPointer(size, type, stride, offset);
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glCurrentPaletteMatrixOES(int matrixpaletteindex) {
        begin("glCurrentPaletteMatrixOES");
        arg("matrixpaletteindex", matrixpaletteindex);
        end();
        this.mgl11Ext.glCurrentPaletteMatrixOES(matrixpaletteindex);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glLoadPaletteFromModelViewMatrixOES() {
        begin("glLoadPaletteFromModelViewMatrixOES");
        end();
        this.mgl11Ext.glLoadPaletteFromModelViewMatrixOES();
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glMatrixIndexPointerOES(int size, int type, int stride, Buffer pointer) {
        begin("glMatrixIndexPointerOES");
        argPointer(size, type, stride, pointer);
        end();
        this.mgl11Ext.glMatrixIndexPointerOES(size, type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glMatrixIndexPointerOES(int size, int type, int stride, int offset) {
        begin("glMatrixIndexPointerOES");
        arg(DumpHeapActivity.KEY_SIZE, size);
        arg("type", type);
        arg(MediaFormat.KEY_STRIDE, stride);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11Ext.glMatrixIndexPointerOES(size, type, stride, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glWeightPointerOES(int size, int type, int stride, Buffer pointer) {
        begin("glWeightPointerOES");
        argPointer(size, type, stride, pointer);
        end();
        this.mgl11Ext.glWeightPointerOES(size, type, stride, pointer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11Ext
    public void glWeightPointerOES(int size, int type, int stride, int offset) {
        begin("glWeightPointerOES");
        arg(DumpHeapActivity.KEY_SIZE, size);
        arg("type", type);
        arg(MediaFormat.KEY_STRIDE, stride);
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11Ext.glWeightPointerOES(size, type, stride, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBindFramebufferOES(int target, int framebuffer) {
        begin("glBindFramebufferOES");
        arg("target", target);
        arg("framebuffer", framebuffer);
        end();
        this.mgl11ExtensionPack.glBindFramebufferOES(target, framebuffer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBindRenderbufferOES(int target, int renderbuffer) {
        begin("glBindRenderbufferOES");
        arg("target", target);
        arg("renderbuffer", renderbuffer);
        end();
        this.mgl11ExtensionPack.glBindRenderbufferOES(target, renderbuffer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBlendEquation(int mode) {
        begin("glBlendEquation");
        arg("mode", mode);
        end();
        this.mgl11ExtensionPack.glBlendEquation(mode);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        begin("glBlendEquationSeparate");
        arg("modeRGB", modeRGB);
        arg("modeAlpha", modeAlpha);
        end();
        this.mgl11ExtensionPack.glBlendEquationSeparate(modeRGB, modeAlpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        begin("glBlendFuncSeparate");
        arg("srcRGB", srcRGB);
        arg("dstRGB", dstRGB);
        arg("srcAlpha", srcAlpha);
        arg("dstAlpha", dstAlpha);
        end();
        this.mgl11ExtensionPack.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public int glCheckFramebufferStatusOES(int target) {
        begin("glCheckFramebufferStatusOES");
        arg("target", target);
        end();
        int result = this.mgl11ExtensionPack.glCheckFramebufferStatusOES(target);
        checkError();
        return result;
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glDeleteFramebuffersOES(int n, int[] framebuffers, int offset) {
        begin("glDeleteFramebuffersOES");
        arg("n", n);
        arg("framebuffers", framebuffers.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glDeleteFramebuffersOES(n, framebuffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glDeleteFramebuffersOES(int n, IntBuffer framebuffers) {
        begin("glDeleteFramebuffersOES");
        arg("n", n);
        arg("framebuffers", framebuffers.toString());
        end();
        this.mgl11ExtensionPack.glDeleteFramebuffersOES(n, framebuffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glDeleteRenderbuffersOES(int n, int[] renderbuffers, int offset) {
        begin("glDeleteRenderbuffersOES");
        arg("n", n);
        arg("renderbuffers", renderbuffers.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glDeleteRenderbuffersOES(n, renderbuffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glDeleteRenderbuffersOES(int n, IntBuffer renderbuffers) {
        begin("glDeleteRenderbuffersOES");
        arg("n", n);
        arg("renderbuffers", renderbuffers.toString());
        end();
        this.mgl11ExtensionPack.glDeleteRenderbuffersOES(n, renderbuffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glFramebufferRenderbufferOES(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        begin("glFramebufferRenderbufferOES");
        arg("target", target);
        arg("attachment", attachment);
        arg("renderbuffertarget", renderbuffertarget);
        arg("renderbuffer", renderbuffer);
        end();
        this.mgl11ExtensionPack.glFramebufferRenderbufferOES(target, attachment, renderbuffertarget, renderbuffer);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glFramebufferTexture2DOES(int target, int attachment, int textarget, int texture, int level) {
        begin("glFramebufferTexture2DOES");
        arg("target", target);
        arg("attachment", attachment);
        arg("textarget", textarget);
        arg("texture", texture);
        arg("level", level);
        end();
        this.mgl11ExtensionPack.glFramebufferTexture2DOES(target, attachment, textarget, texture, level);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGenerateMipmapOES(int target) {
        begin("glGenerateMipmapOES");
        arg("target", target);
        end();
        this.mgl11ExtensionPack.glGenerateMipmapOES(target);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGenFramebuffersOES(int n, int[] framebuffers, int offset) {
        begin("glGenFramebuffersOES");
        arg("n", n);
        arg("framebuffers", framebuffers.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glGenFramebuffersOES(n, framebuffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGenFramebuffersOES(int n, IntBuffer framebuffers) {
        begin("glGenFramebuffersOES");
        arg("n", n);
        arg("framebuffers", framebuffers.toString());
        end();
        this.mgl11ExtensionPack.glGenFramebuffersOES(n, framebuffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGenRenderbuffersOES(int n, int[] renderbuffers, int offset) {
        begin("glGenRenderbuffersOES");
        arg("n", n);
        arg("renderbuffers", renderbuffers.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glGenRenderbuffersOES(n, renderbuffers, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGenRenderbuffersOES(int n, IntBuffer renderbuffers) {
        begin("glGenRenderbuffersOES");
        arg("n", n);
        arg("renderbuffers", renderbuffers.toString());
        end();
        this.mgl11ExtensionPack.glGenRenderbuffersOES(n, renderbuffers);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetFramebufferAttachmentParameterivOES(int target, int attachment, int pname, int[] params, int offset) {
        begin("glGetFramebufferAttachmentParameterivOES");
        arg("target", target);
        arg("attachment", attachment);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glGetFramebufferAttachmentParameterivOES(target, attachment, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetFramebufferAttachmentParameterivOES(int target, int attachment, int pname, IntBuffer params) {
        begin("glGetFramebufferAttachmentParameterivOES");
        arg("target", target);
        arg("attachment", attachment);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11ExtensionPack.glGetFramebufferAttachmentParameterivOES(target, attachment, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetRenderbufferParameterivOES(int target, int pname, int[] params, int offset) {
        begin("glGetRenderbufferParameterivOES");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glGetRenderbufferParameterivOES(target, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetRenderbufferParameterivOES(int target, int pname, IntBuffer params) {
        begin("glGetRenderbufferParameterivOES");
        arg("target", target);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11ExtensionPack.glGetRenderbufferParameterivOES(target, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGenfv(int coord, int pname, float[] params, int offset) {
        begin("glGetTexGenfv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glGetTexGenfv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGenfv(int coord, int pname, FloatBuffer params) {
        begin("glGetTexGenfv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11ExtensionPack.glGetTexGenfv(coord, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGeniv(int coord, int pname, int[] params, int offset) {
        begin("glGetTexGeniv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glGetTexGeniv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGeniv(int coord, int pname, IntBuffer params) {
        begin("glGetTexGeniv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11ExtensionPack.glGetTexGeniv(coord, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGenxv(int coord, int pname, int[] params, int offset) {
        begin("glGetTexGenxv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glGetTexGenxv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glGetTexGenxv(int coord, int pname, IntBuffer params) {
        begin("glGetTexGenxv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11ExtensionPack.glGetTexGenxv(coord, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public boolean glIsFramebufferOES(int framebuffer) {
        begin("glIsFramebufferOES");
        arg("framebuffer", framebuffer);
        end();
        boolean result = this.mgl11ExtensionPack.glIsFramebufferOES(framebuffer);
        checkError();
        return result;
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public boolean glIsRenderbufferOES(int renderbuffer) {
        begin("glIsRenderbufferOES");
        arg("renderbuffer", renderbuffer);
        end();
        this.mgl11ExtensionPack.glIsRenderbufferOES(renderbuffer);
        checkError();
        return false;
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glRenderbufferStorageOES(int target, int internalformat, int width, int height) {
        begin("glRenderbufferStorageOES");
        arg("target", target);
        arg("internalformat", internalformat);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl11ExtensionPack.glRenderbufferStorageOES(target, internalformat, width, height);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenf(int coord, int pname, float param) {
        begin("glTexGenf");
        arg("coord", coord);
        arg("pname", pname);
        arg("param", param);
        end();
        this.mgl11ExtensionPack.glTexGenf(coord, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenfv(int coord, int pname, float[] params, int offset) {
        begin("glTexGenfv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glTexGenfv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenfv(int coord, int pname, FloatBuffer params) {
        begin("glTexGenfv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11ExtensionPack.glTexGenfv(coord, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGeni(int coord, int pname, int param) {
        begin("glTexGeni");
        arg("coord", coord);
        arg("pname", pname);
        arg("param", param);
        end();
        this.mgl11ExtensionPack.glTexGeni(coord, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGeniv(int coord, int pname, int[] params, int offset) {
        begin("glTexGeniv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glTexGeniv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGeniv(int coord, int pname, IntBuffer params) {
        begin("glTexGeniv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11ExtensionPack.glTexGeniv(coord, pname, params);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenx(int coord, int pname, int param) {
        begin("glTexGenx");
        arg("coord", coord);
        arg("pname", pname);
        arg("param", param);
        end();
        this.mgl11ExtensionPack.glTexGenx(coord, pname, param);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenxv(int coord, int pname, int[] params, int offset) {
        begin("glTexGenxv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        arg(CallLog.Calls.OFFSET_PARAM_KEY, offset);
        end();
        this.mgl11ExtensionPack.glTexGenxv(coord, pname, params, offset);
        checkError();
    }

    @Override // javax.microedition.khronos.opengles.GL11ExtensionPack
    public void glTexGenxv(int coord, int pname, IntBuffer params) {
        begin("glTexGenxv");
        arg("coord", coord);
        arg("pname", pname);
        arg("params", params.toString());
        end();
        this.mgl11ExtensionPack.glTexGenxv(coord, pname, params);
        checkError();
    }

    /* access modifiers changed from: private */
    public class PointerInfo {
        public Buffer mPointer;
        public int mSize;
        public int mStride;
        public ByteBuffer mTempByteBuffer;
        public int mType;

        public PointerInfo() {
        }

        public PointerInfo(int size, int type, int stride, Buffer pointer) {
            this.mSize = size;
            this.mType = type;
            this.mStride = stride;
            this.mPointer = pointer;
        }

        public int sizeof(int type) {
            if (type == 5126 || type == 5132) {
                return 4;
            }
            switch (type) {
                case 5120:
                    return 1;
                case 5121:
                    return 1;
                case 5122:
                    return 2;
                default:
                    return 0;
            }
        }

        public int getStride() {
            int i = this.mStride;
            return i > 0 ? i : sizeof(this.mType) * this.mSize;
        }

        public void bindByteBuffer() {
            Buffer buffer = this.mPointer;
            this.mTempByteBuffer = buffer == null ? null : GLLogWrapper.this.toByteBuffer(-1, buffer);
        }

        public void unbindByteBuffer() {
            this.mTempByteBuffer = null;
        }
    }
}
