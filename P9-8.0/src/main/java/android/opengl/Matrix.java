package android.opengl;

import android.hardware.camera2.params.TonemapCurve;

public class Matrix {
    private static final float[] sTemp = new float[32];

    public static native void multiplyMM(float[] fArr, int i, float[] fArr2, int i2, float[] fArr3, int i3);

    public static native void multiplyMV(float[] fArr, int i, float[] fArr2, int i2, float[] fArr3, int i3);

    public static void transposeM(float[] mTrans, int mTransOffset, float[] m, int mOffset) {
        for (int i = 0; i < 4; i++) {
            int mBase = (i * 4) + mOffset;
            mTrans[i + mTransOffset] = m[mBase];
            mTrans[(i + 4) + mTransOffset] = m[mBase + 1];
            mTrans[(i + 8) + mTransOffset] = m[mBase + 2];
            mTrans[(i + 12) + mTransOffset] = m[mBase + 3];
        }
    }

    public static boolean invertM(float[] mInv, int mInvOffset, float[] m, int mOffset) {
        float src0 = m[mOffset + 0];
        float src4 = m[mOffset + 1];
        float src8 = m[mOffset + 2];
        float src12 = m[mOffset + 3];
        float src1 = m[mOffset + 4];
        float src5 = m[mOffset + 5];
        float src9 = m[mOffset + 6];
        float src13 = m[mOffset + 7];
        float src2 = m[mOffset + 8];
        float src6 = m[mOffset + 9];
        float src10 = m[mOffset + 10];
        float src14 = m[mOffset + 11];
        float src3 = m[mOffset + 12];
        float src7 = m[mOffset + 13];
        float src11 = m[mOffset + 14];
        float src15 = m[mOffset + 15];
        float atmp0 = src10 * src15;
        float atmp1 = src11 * src14;
        float atmp2 = src9 * src15;
        float atmp3 = src11 * src13;
        float atmp4 = src9 * src14;
        float atmp5 = src10 * src13;
        float atmp6 = src8 * src15;
        float atmp7 = src11 * src12;
        float atmp8 = src8 * src14;
        float atmp9 = src10 * src12;
        float atmp10 = src8 * src13;
        float atmp11 = src9 * src12;
        float dst0 = (((atmp0 * src5) + (atmp3 * src6)) + (atmp4 * src7)) - (((atmp1 * src5) + (atmp2 * src6)) + (atmp5 * src7));
        float dst1 = (((atmp1 * src4) + (atmp6 * src6)) + (atmp9 * src7)) - (((atmp0 * src4) + (atmp7 * src6)) + (atmp8 * src7));
        float dst2 = (((atmp2 * src4) + (atmp7 * src5)) + (atmp10 * src7)) - (((atmp3 * src4) + (atmp6 * src5)) + (atmp11 * src7));
        float dst3 = (((atmp5 * src4) + (atmp8 * src5)) + (atmp11 * src6)) - (((atmp4 * src4) + (atmp9 * src5)) + (atmp10 * src6));
        float dst4 = (((atmp1 * src1) + (atmp2 * src2)) + (atmp5 * src3)) - (((atmp0 * src1) + (atmp3 * src2)) + (atmp4 * src3));
        float dst5 = (((atmp0 * src0) + (atmp7 * src2)) + (atmp8 * src3)) - (((atmp1 * src0) + (atmp6 * src2)) + (atmp9 * src3));
        float dst6 = (((atmp3 * src0) + (atmp6 * src1)) + (atmp11 * src3)) - (((atmp2 * src0) + (atmp7 * src1)) + (atmp10 * src3));
        float dst7 = (((atmp4 * src0) + (atmp9 * src1)) + (atmp10 * src2)) - (((atmp5 * src0) + (atmp8 * src1)) + (atmp11 * src2));
        float btmp0 = src2 * src7;
        float btmp1 = src3 * src6;
        float btmp2 = src1 * src7;
        float btmp3 = src3 * src5;
        float btmp4 = src1 * src6;
        float btmp5 = src2 * src5;
        float btmp6 = src0 * src7;
        float btmp7 = src3 * src4;
        float btmp8 = src0 * src6;
        float btmp9 = src2 * src4;
        float btmp10 = src0 * src5;
        float btmp11 = src1 * src4;
        float dst8 = (((btmp0 * src13) + (btmp3 * src14)) + (btmp4 * src15)) - (((btmp1 * src13) + (btmp2 * src14)) + (btmp5 * src15));
        float dst9 = (((btmp1 * src12) + (btmp6 * src14)) + (btmp9 * src15)) - (((btmp0 * src12) + (btmp7 * src14)) + (btmp8 * src15));
        float dst10 = (((btmp2 * src12) + (btmp7 * src13)) + (btmp10 * src15)) - (((btmp3 * src12) + (btmp6 * src13)) + (btmp11 * src15));
        float dst11 = (((btmp5 * src12) + (btmp8 * src13)) + (btmp11 * src14)) - (((btmp4 * src12) + (btmp9 * src13)) + (btmp10 * src14));
        float dst12 = (((btmp2 * src10) + (btmp5 * src11)) + (btmp1 * src9)) - (((btmp4 * src11) + (btmp0 * src9)) + (btmp3 * src10));
        float dst13 = (((btmp8 * src11) + (btmp0 * src8)) + (btmp7 * src10)) - (((btmp6 * src10) + (btmp9 * src11)) + (btmp1 * src8));
        float dst14 = (((btmp6 * src9) + (btmp11 * src11)) + (btmp3 * src8)) - (((btmp10 * src11) + (btmp2 * src8)) + (btmp7 * src9));
        float dst15 = (((btmp10 * src10) + (btmp4 * src8)) + (btmp9 * src9)) - (((btmp8 * src9) + (btmp11 * src10)) + (btmp5 * src8));
        float det = (((src0 * dst0) + (src1 * dst1)) + (src2 * dst2)) + (src3 * dst3);
        if (det == TonemapCurve.LEVEL_BLACK) {
            return false;
        }
        float invdet = 1.0f / det;
        mInv[mInvOffset] = dst0 * invdet;
        mInv[mInvOffset + 1] = dst1 * invdet;
        mInv[mInvOffset + 2] = dst2 * invdet;
        mInv[mInvOffset + 3] = dst3 * invdet;
        mInv[mInvOffset + 4] = dst4 * invdet;
        mInv[mInvOffset + 5] = dst5 * invdet;
        mInv[mInvOffset + 6] = dst6 * invdet;
        mInv[mInvOffset + 7] = dst7 * invdet;
        mInv[mInvOffset + 8] = dst8 * invdet;
        mInv[mInvOffset + 9] = dst9 * invdet;
        mInv[mInvOffset + 10] = dst10 * invdet;
        mInv[mInvOffset + 11] = dst11 * invdet;
        mInv[mInvOffset + 12] = dst12 * invdet;
        mInv[mInvOffset + 13] = dst13 * invdet;
        mInv[mInvOffset + 14] = dst14 * invdet;
        mInv[mInvOffset + 15] = dst15 * invdet;
        return true;
    }

    public static void orthoM(float[] m, int mOffset, float left, float right, float bottom, float top, float near, float far) {
        if (left == right) {
            throw new IllegalArgumentException("left == right");
        } else if (bottom == top) {
            throw new IllegalArgumentException("bottom == top");
        } else if (near == far) {
            throw new IllegalArgumentException("near == far");
        } else {
            float r_width = 1.0f / (right - left);
            float r_height = 1.0f / (top - bottom);
            float r_depth = 1.0f / (far - near);
            float y = 2.0f * r_height;
            float z = -2.0f * r_depth;
            float tx = (-(right + left)) * r_width;
            float ty = (-(top + bottom)) * r_height;
            float tz = (-(far + near)) * r_depth;
            m[mOffset + 0] = 2.0f * r_width;
            m[mOffset + 5] = y;
            m[mOffset + 10] = z;
            m[mOffset + 12] = tx;
            m[mOffset + 13] = ty;
            m[mOffset + 14] = tz;
            m[mOffset + 15] = 1.0f;
            m[mOffset + 1] = TonemapCurve.LEVEL_BLACK;
            m[mOffset + 2] = TonemapCurve.LEVEL_BLACK;
            m[mOffset + 3] = TonemapCurve.LEVEL_BLACK;
            m[mOffset + 4] = TonemapCurve.LEVEL_BLACK;
            m[mOffset + 6] = TonemapCurve.LEVEL_BLACK;
            m[mOffset + 7] = TonemapCurve.LEVEL_BLACK;
            m[mOffset + 8] = TonemapCurve.LEVEL_BLACK;
            m[mOffset + 9] = TonemapCurve.LEVEL_BLACK;
            m[mOffset + 11] = TonemapCurve.LEVEL_BLACK;
        }
    }

    public static void frustumM(float[] m, int offset, float left, float right, float bottom, float top, float near, float far) {
        if (left == right) {
            throw new IllegalArgumentException("left == right");
        } else if (top == bottom) {
            throw new IllegalArgumentException("top == bottom");
        } else if (near == far) {
            throw new IllegalArgumentException("near == far");
        } else if (near <= TonemapCurve.LEVEL_BLACK) {
            throw new IllegalArgumentException("near <= 0.0f");
        } else if (far <= TonemapCurve.LEVEL_BLACK) {
            throw new IllegalArgumentException("far <= 0.0f");
        } else {
            float r_width = 1.0f / (right - left);
            float r_height = 1.0f / (top - bottom);
            float r_depth = 1.0f / (near - far);
            float y = 2.0f * (near * r_height);
            float A = (right + left) * r_width;
            float B = (top + bottom) * r_height;
            float C = (far + near) * r_depth;
            float D = 2.0f * ((far * near) * r_depth);
            m[offset + 0] = 2.0f * (near * r_width);
            m[offset + 5] = y;
            m[offset + 8] = A;
            m[offset + 9] = B;
            m[offset + 10] = C;
            m[offset + 14] = D;
            m[offset + 11] = -1.0f;
            m[offset + 1] = TonemapCurve.LEVEL_BLACK;
            m[offset + 2] = TonemapCurve.LEVEL_BLACK;
            m[offset + 3] = TonemapCurve.LEVEL_BLACK;
            m[offset + 4] = TonemapCurve.LEVEL_BLACK;
            m[offset + 6] = TonemapCurve.LEVEL_BLACK;
            m[offset + 7] = TonemapCurve.LEVEL_BLACK;
            m[offset + 12] = TonemapCurve.LEVEL_BLACK;
            m[offset + 13] = TonemapCurve.LEVEL_BLACK;
            m[offset + 15] = TonemapCurve.LEVEL_BLACK;
        }
    }

    public static void perspectiveM(float[] m, int offset, float fovy, float aspect, float zNear, float zFar) {
        float f = 1.0f / ((float) Math.tan(((double) fovy) * 0.008726646259971648d));
        float rangeReciprocal = 1.0f / (zNear - zFar);
        m[offset + 0] = f / aspect;
        m[offset + 1] = TonemapCurve.LEVEL_BLACK;
        m[offset + 2] = TonemapCurve.LEVEL_BLACK;
        m[offset + 3] = TonemapCurve.LEVEL_BLACK;
        m[offset + 4] = TonemapCurve.LEVEL_BLACK;
        m[offset + 5] = f;
        m[offset + 6] = TonemapCurve.LEVEL_BLACK;
        m[offset + 7] = TonemapCurve.LEVEL_BLACK;
        m[offset + 8] = TonemapCurve.LEVEL_BLACK;
        m[offset + 9] = TonemapCurve.LEVEL_BLACK;
        m[offset + 10] = (zFar + zNear) * rangeReciprocal;
        m[offset + 11] = -1.0f;
        m[offset + 12] = TonemapCurve.LEVEL_BLACK;
        m[offset + 13] = TonemapCurve.LEVEL_BLACK;
        m[offset + 14] = ((2.0f * zFar) * zNear) * rangeReciprocal;
        m[offset + 15] = TonemapCurve.LEVEL_BLACK;
    }

    public static float length(float x, float y, float z) {
        return (float) Math.sqrt((double) (((x * x) + (y * y)) + (z * z)));
    }

    public static void setIdentityM(float[] sm, int smOffset) {
        int i;
        for (i = 0; i < 16; i++) {
            sm[smOffset + i] = TonemapCurve.LEVEL_BLACK;
        }
        for (i = 0; i < 16; i += 5) {
            sm[smOffset + i] = 1.0f;
        }
    }

    public static void scaleM(float[] sm, int smOffset, float[] m, int mOffset, float x, float y, float z) {
        for (int i = 0; i < 4; i++) {
            int smi = smOffset + i;
            int mi = mOffset + i;
            sm[smi] = m[mi] * x;
            sm[smi + 4] = m[mi + 4] * y;
            sm[smi + 8] = m[mi + 8] * z;
            sm[smi + 12] = m[mi + 12];
        }
    }

    public static void scaleM(float[] m, int mOffset, float x, float y, float z) {
        for (int i = 0; i < 4; i++) {
            int mi = mOffset + i;
            m[mi] = m[mi] * x;
            int i2 = mi + 4;
            m[i2] = m[i2] * y;
            i2 = mi + 8;
            m[i2] = m[i2] * z;
        }
    }

    public static void translateM(float[] tm, int tmOffset, float[] m, int mOffset, float x, float y, float z) {
        int i;
        for (i = 0; i < 12; i++) {
            tm[tmOffset + i] = m[mOffset + i];
        }
        for (i = 0; i < 4; i++) {
            int mi = mOffset + i;
            tm[(tmOffset + i) + 12] = (((m[mi] * x) + (m[mi + 4] * y)) + (m[mi + 8] * z)) + m[mi + 12];
        }
    }

    public static void translateM(float[] m, int mOffset, float x, float y, float z) {
        for (int i = 0; i < 4; i++) {
            int mi = mOffset + i;
            int i2 = mi + 12;
            m[i2] = m[i2] + (((m[mi] * x) + (m[mi + 4] * y)) + (m[mi + 8] * z));
        }
    }

    public static void rotateM(float[] rm, int rmOffset, float[] m, int mOffset, float a, float x, float y, float z) {
        synchronized (sTemp) {
            setRotateM(sTemp, 0, a, x, y, z);
            multiplyMM(rm, rmOffset, m, mOffset, sTemp, 0);
        }
    }

    public static void rotateM(float[] m, int mOffset, float a, float x, float y, float z) {
        synchronized (sTemp) {
            setRotateM(sTemp, 0, a, x, y, z);
            multiplyMM(sTemp, 16, m, mOffset, sTemp, 0);
            System.arraycopy(sTemp, 16, m, mOffset, 16);
        }
    }

    public static void setRotateM(float[] rm, int rmOffset, float a, float x, float y, float z) {
        rm[rmOffset + 3] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 7] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 11] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 12] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 13] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 14] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 15] = 1.0f;
        a *= 0.017453292f;
        float s = (float) Math.sin((double) a);
        float c = (float) Math.cos((double) a);
        if (1.0f == x && TonemapCurve.LEVEL_BLACK == y && TonemapCurve.LEVEL_BLACK == z) {
            rm[rmOffset + 5] = c;
            rm[rmOffset + 10] = c;
            rm[rmOffset + 6] = s;
            rm[rmOffset + 9] = -s;
            rm[rmOffset + 1] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 2] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 4] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 8] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 0] = 1.0f;
        } else if (TonemapCurve.LEVEL_BLACK == x && 1.0f == y && TonemapCurve.LEVEL_BLACK == z) {
            rm[rmOffset + 0] = c;
            rm[rmOffset + 10] = c;
            rm[rmOffset + 8] = s;
            rm[rmOffset + 2] = -s;
            rm[rmOffset + 1] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 4] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 6] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 9] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 5] = 1.0f;
        } else if (TonemapCurve.LEVEL_BLACK == x && TonemapCurve.LEVEL_BLACK == y && 1.0f == z) {
            rm[rmOffset + 0] = c;
            rm[rmOffset + 5] = c;
            rm[rmOffset + 1] = s;
            rm[rmOffset + 4] = -s;
            rm[rmOffset + 2] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 6] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 8] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 9] = TonemapCurve.LEVEL_BLACK;
            rm[rmOffset + 10] = 1.0f;
        } else {
            float len = length(x, y, z);
            if (1.0f != len) {
                float recipLen = 1.0f / len;
                x *= recipLen;
                y *= recipLen;
                z *= recipLen;
            }
            float nc = 1.0f - c;
            float xy = x * y;
            float yz = y * z;
            float zx = z * x;
            float xs = x * s;
            float ys = y * s;
            float zs = z * s;
            rm[rmOffset + 0] = ((x * x) * nc) + c;
            rm[rmOffset + 4] = (xy * nc) - zs;
            rm[rmOffset + 8] = (zx * nc) + ys;
            rm[rmOffset + 1] = (xy * nc) + zs;
            rm[rmOffset + 5] = ((y * y) * nc) + c;
            rm[rmOffset + 9] = (yz * nc) - xs;
            rm[rmOffset + 2] = (zx * nc) - ys;
            rm[rmOffset + 6] = (yz * nc) + xs;
            rm[rmOffset + 10] = ((z * z) * nc) + c;
        }
    }

    public static void setRotateEulerM(float[] rm, int rmOffset, float x, float y, float z) {
        x *= 0.017453292f;
        y *= 0.017453292f;
        z *= 0.017453292f;
        float cx = (float) Math.cos((double) x);
        float sx = (float) Math.sin((double) x);
        float cy = (float) Math.cos((double) y);
        float sy = (float) Math.sin((double) y);
        float cz = (float) Math.cos((double) z);
        float sz = (float) Math.sin((double) z);
        float cxsy = cx * sy;
        float sxsy = sx * sy;
        rm[rmOffset + 0] = cy * cz;
        rm[rmOffset + 1] = (-cy) * sz;
        rm[rmOffset + 2] = sy;
        rm[rmOffset + 3] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 4] = (cxsy * cz) + (cx * sz);
        rm[rmOffset + 5] = ((-cxsy) * sz) + (cx * cz);
        rm[rmOffset + 6] = (-sx) * cy;
        rm[rmOffset + 7] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 8] = ((-sxsy) * cz) + (sx * sz);
        rm[rmOffset + 9] = (sxsy * sz) + (sx * cz);
        rm[rmOffset + 10] = cx * cy;
        rm[rmOffset + 11] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 12] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 13] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 14] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 15] = 1.0f;
    }

    public static void setLookAtM(float[] rm, int rmOffset, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        float fx = centerX - eyeX;
        float fy = centerY - eyeY;
        float fz = centerZ - eyeZ;
        float rlf = 1.0f / length(fx, fy, fz);
        fx *= rlf;
        fy *= rlf;
        fz *= rlf;
        float sx = (fy * upZ) - (fz * upY);
        float sy = (fz * upX) - (fx * upZ);
        float sz = (fx * upY) - (fy * upX);
        float rls = 1.0f / length(sx, sy, sz);
        sx *= rls;
        sy *= rls;
        sz *= rls;
        float ux = (sy * fz) - (sz * fy);
        float uy = (sz * fx) - (sx * fz);
        float uz = (sx * fy) - (sy * fx);
        rm[rmOffset + 0] = sx;
        rm[rmOffset + 1] = ux;
        rm[rmOffset + 2] = -fx;
        rm[rmOffset + 3] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 4] = sy;
        rm[rmOffset + 5] = uy;
        rm[rmOffset + 6] = -fy;
        rm[rmOffset + 7] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 8] = sz;
        rm[rmOffset + 9] = uz;
        rm[rmOffset + 10] = -fz;
        rm[rmOffset + 11] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 12] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 13] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 14] = TonemapCurve.LEVEL_BLACK;
        rm[rmOffset + 15] = 1.0f;
        translateM(rm, rmOffset, -eyeX, -eyeY, -eyeZ);
    }
}
