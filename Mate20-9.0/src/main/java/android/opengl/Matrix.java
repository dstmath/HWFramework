package android.opengl;

public class Matrix {
    private static final float[] sTemp = new float[32];

    public static native void multiplyMM(float[] fArr, int i, float[] fArr2, int i2, float[] fArr3, int i3);

    public static native void multiplyMV(float[] fArr, int i, float[] fArr2, int i2, float[] fArr3, int i3);

    public static void transposeM(float[] mTrans, int mTransOffset, float[] m, int mOffset) {
        for (int i = 0; i < 4; i++) {
            int mBase = (i * 4) + mOffset;
            mTrans[i + mTransOffset] = m[mBase];
            mTrans[i + 4 + mTransOffset] = m[mBase + 1];
            mTrans[i + 8 + mTransOffset] = m[mBase + 2];
            mTrans[i + 12 + mTransOffset] = m[mBase + 3];
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
        float det = (src0 * dst0) + (src1 * dst1) + (src2 * dst2) + (src3 * dst3);
        if (det == 0.0f) {
            return false;
        }
        float invdet = 1.0f / det;
        mInv[mInvOffset] = dst0 * invdet;
        mInv[1 + mInvOffset] = dst1 * invdet;
        mInv[2 + mInvOffset] = dst2 * invdet;
        mInv[3 + mInvOffset] = dst3 * invdet;
        mInv[4 + mInvOffset] = dst4 * invdet;
        mInv[5 + mInvOffset] = dst5 * invdet;
        mInv[6 + mInvOffset] = dst6 * invdet;
        mInv[7 + mInvOffset] = dst7 * invdet;
        mInv[8 + mInvOffset] = dst8 * invdet;
        mInv[9 + mInvOffset] = dst9 * invdet;
        mInv[10 + mInvOffset] = dst10 * invdet;
        mInv[11 + mInvOffset] = dst11 * invdet;
        mInv[12 + mInvOffset] = dst12 * invdet;
        mInv[13 + mInvOffset] = dst13 * invdet;
        mInv[14 + mInvOffset] = dst14 * invdet;
        mInv[15 + mInvOffset] = dst15 * invdet;
        return true;
    }

    public static void orthoM(float[] m, int mOffset, float left, float right, float bottom, float top, float near, float far) {
        if (left == right) {
            throw new IllegalArgumentException("left == right");
        } else if (bottom == top) {
            throw new IllegalArgumentException("bottom == top");
        } else if (near != far) {
            float r_width = 1.0f / (right - left);
            float r_height = 1.0f / (top - bottom);
            float r_depth = 1.0f / (far - near);
            m[mOffset + 0] = 2.0f * r_width;
            m[mOffset + 5] = 2.0f * r_height;
            m[mOffset + 10] = -2.0f * r_depth;
            m[mOffset + 12] = (-(right + left)) * r_width;
            m[mOffset + 13] = (-(top + bottom)) * r_height;
            m[mOffset + 14] = (-(far + near)) * r_depth;
            m[mOffset + 15] = 1.0f;
            m[mOffset + 1] = 0.0f;
            m[mOffset + 2] = 0.0f;
            m[mOffset + 3] = 0.0f;
            m[mOffset + 4] = 0.0f;
            m[mOffset + 6] = 0.0f;
            m[mOffset + 7] = 0.0f;
            m[mOffset + 8] = 0.0f;
            m[mOffset + 9] = 0.0f;
            m[mOffset + 11] = 0.0f;
        } else {
            throw new IllegalArgumentException("near == far");
        }
    }

    public static void frustumM(float[] m, int offset, float left, float right, float bottom, float top, float near, float far) {
        if (left == right) {
            throw new IllegalArgumentException("left == right");
        } else if (top == bottom) {
            throw new IllegalArgumentException("top == bottom");
        } else if (near == far) {
            throw new IllegalArgumentException("near == far");
        } else if (near <= 0.0f) {
            throw new IllegalArgumentException("near <= 0.0f");
        } else if (far > 0.0f) {
            float r_width = 1.0f / (right - left);
            float r_height = 1.0f / (top - bottom);
            float r_depth = 1.0f / (near - far);
            m[offset + 0] = near * r_width * 2.0f;
            m[offset + 5] = near * r_height * 2.0f;
            m[offset + 8] = (right + left) * r_width;
            m[offset + 9] = (top + bottom) * r_height;
            m[offset + 10] = (far + near) * r_depth;
            m[offset + 14] = 2.0f * far * near * r_depth;
            m[offset + 11] = -1.0f;
            m[offset + 1] = 0.0f;
            m[offset + 2] = 0.0f;
            m[offset + 3] = 0.0f;
            m[offset + 4] = 0.0f;
            m[offset + 6] = 0.0f;
            m[offset + 7] = 0.0f;
            m[offset + 12] = 0.0f;
            m[offset + 13] = 0.0f;
            m[offset + 15] = 0.0f;
        } else {
            throw new IllegalArgumentException("far <= 0.0f");
        }
    }

    public static void perspectiveM(float[] m, int offset, float fovy, float aspect, float zNear, float zFar) {
        float f = 1.0f / ((float) Math.tan(((double) fovy) * 0.008726646259971648d));
        float rangeReciprocal = 1.0f / (zNear - zFar);
        m[offset + 0] = f / aspect;
        m[offset + 1] = 0.0f;
        m[offset + 2] = 0.0f;
        m[offset + 3] = 0.0f;
        m[offset + 4] = 0.0f;
        m[offset + 5] = f;
        m[offset + 6] = 0.0f;
        m[offset + 7] = 0.0f;
        m[offset + 8] = 0.0f;
        m[offset + 9] = 0.0f;
        m[offset + 10] = (zFar + zNear) * rangeReciprocal;
        m[offset + 11] = -1.0f;
        m[offset + 12] = 0.0f;
        m[offset + 13] = 0.0f;
        m[offset + 14] = 2.0f * zFar * zNear * rangeReciprocal;
        m[offset + 15] = 0.0f;
    }

    public static float length(float x, float y, float z) {
        return (float) Math.sqrt((double) ((x * x) + (y * y) + (z * z)));
    }

    public static void setIdentityM(float[] sm, int smOffset) {
        for (int i = 0; i < 16; i++) {
            sm[smOffset + i] = 0.0f;
        }
        for (int i2 = 0; i2 < 16; i2 += 5) {
            sm[smOffset + i2] = 1.0f;
        }
    }

    public static void scaleM(float[] sm, int smOffset, float[] m, int mOffset, float x, float y, float z) {
        for (int i = 0; i < 4; i++) {
            int smi = smOffset + i;
            int mi = mOffset + i;
            sm[smi] = m[mi] * x;
            sm[4 + smi] = m[4 + mi] * y;
            sm[8 + smi] = m[8 + mi] * z;
            sm[12 + smi] = m[12 + mi];
        }
    }

    public static void scaleM(float[] m, int mOffset, float x, float y, float z) {
        for (int i = 0; i < 4; i++) {
            int mi = mOffset + i;
            m[mi] = m[mi] * x;
            int i2 = 4 + mi;
            m[i2] = m[i2] * y;
            int i3 = 8 + mi;
            m[i3] = m[i3] * z;
        }
    }

    public static void translateM(float[] tm, int tmOffset, float[] m, int mOffset, float x, float y, float z) {
        for (int i = 0; i < 12; i++) {
            tm[tmOffset + i] = m[mOffset + i];
        }
        for (int i2 = 0; i2 < 4; i2++) {
            int mi = mOffset + i2;
            tm[12 + tmOffset + i2] = (m[mi] * x) + (m[4 + mi] * y) + (m[8 + mi] * z) + m[12 + mi];
        }
    }

    public static void translateM(float[] m, int mOffset, float x, float y, float z) {
        for (int i = 0; i < 4; i++) {
            int mi = mOffset + i;
            int i2 = 12 + mi;
            m[i2] = m[i2] + (m[mi] * x) + (m[4 + mi] * y) + (m[8 + mi] * z);
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
        float z2;
        float y2;
        float x2;
        rm[rmOffset + 3] = 0.0f;
        rm[rmOffset + 7] = 0.0f;
        rm[rmOffset + 11] = 0.0f;
        rm[rmOffset + 12] = 0.0f;
        rm[rmOffset + 13] = 0.0f;
        rm[rmOffset + 14] = 0.0f;
        rm[rmOffset + 15] = 1.0f;
        float a2 = 0.017453292f * a;
        float s = (float) Math.sin((double) a2);
        float c = (float) Math.cos((double) a2);
        if (1.0f == x && 0.0f == y && 0.0f == z) {
            rm[rmOffset + 5] = c;
            rm[rmOffset + 10] = c;
            rm[rmOffset + 6] = s;
            rm[rmOffset + 9] = -s;
            rm[rmOffset + 1] = 0.0f;
            rm[rmOffset + 2] = 0.0f;
            rm[rmOffset + 4] = 0.0f;
            rm[rmOffset + 8] = 0.0f;
            rm[rmOffset + 0] = 1.0f;
        } else if (0.0f == x && 1.0f == y && 0.0f == z) {
            rm[rmOffset + 0] = c;
            rm[rmOffset + 10] = c;
            rm[rmOffset + 8] = s;
            rm[rmOffset + 2] = -s;
            rm[rmOffset + 1] = 0.0f;
            rm[rmOffset + 4] = 0.0f;
            rm[rmOffset + 6] = 0.0f;
            rm[rmOffset + 9] = 0.0f;
            rm[rmOffset + 5] = 1.0f;
        } else if (0.0f == x && 0.0f == y && 1.0f == z) {
            rm[rmOffset + 0] = c;
            rm[rmOffset + 5] = c;
            rm[rmOffset + 1] = s;
            rm[rmOffset + 4] = -s;
            rm[rmOffset + 2] = 0.0f;
            rm[rmOffset + 6] = 0.0f;
            rm[rmOffset + 8] = 0.0f;
            rm[rmOffset + 9] = 0.0f;
            rm[rmOffset + 10] = 1.0f;
        } else {
            float len = length(x, y, z);
            if (1.0f != len) {
                float recipLen = 1.0f / len;
                x2 = x * recipLen;
                y2 = y * recipLen;
                z2 = z * recipLen;
            } else {
                x2 = x;
                y2 = y;
                z2 = z;
            }
            float nc = 1.0f - c;
            float xy = x2 * y2;
            float yz = y2 * z2;
            float zx = z2 * x2;
            float xs = x2 * s;
            float ys = y2 * s;
            float zs = z2 * s;
            rm[rmOffset + 0] = (x2 * x2 * nc) + c;
            rm[rmOffset + 4] = (xy * nc) - zs;
            rm[rmOffset + 8] = (zx * nc) + ys;
            rm[rmOffset + 1] = (xy * nc) + zs;
            rm[rmOffset + 5] = (y2 * y2 * nc) + c;
            rm[rmOffset + 9] = (yz * nc) - xs;
            rm[rmOffset + 2] = (zx * nc) - ys;
            rm[rmOffset + 6] = (yz * nc) + xs;
            rm[rmOffset + 10] = (z2 * z2 * nc) + c;
            return;
        }
        float f = x;
        float f2 = y;
        float f3 = z;
    }

    public static void setRotateEulerM(float[] rm, int rmOffset, float x, float y, float z) {
        float x2 = x * 0.017453292f;
        float y2 = y * 0.017453292f;
        float z2 = 0.017453292f * z;
        float cx = (float) Math.cos((double) x2);
        float sx = (float) Math.sin((double) x2);
        float cy = (float) Math.cos((double) y2);
        float sy = (float) Math.sin((double) y2);
        float cz = (float) Math.cos((double) z2);
        float sz = (float) Math.sin((double) z2);
        float cxsy = cx * sy;
        float sxsy = sx * sy;
        rm[rmOffset + 0] = cy * cz;
        rm[rmOffset + 1] = (-cy) * sz;
        rm[rmOffset + 2] = sy;
        rm[rmOffset + 3] = 0.0f;
        rm[rmOffset + 4] = (cxsy * cz) + (cx * sz);
        rm[rmOffset + 5] = ((-cxsy) * sz) + (cx * cz);
        rm[rmOffset + 6] = (-sx) * cy;
        rm[rmOffset + 7] = 0.0f;
        rm[rmOffset + 8] = ((-sxsy) * cz) + (sx * sz);
        rm[rmOffset + 9] = (sxsy * sz) + (sx * cz);
        rm[rmOffset + 10] = cx * cy;
        rm[rmOffset + 11] = 0.0f;
        rm[rmOffset + 12] = 0.0f;
        rm[rmOffset + 13] = 0.0f;
        rm[rmOffset + 14] = 0.0f;
        rm[rmOffset + 15] = 1.0f;
    }

    public static void setLookAtM(float[] rm, int rmOffset, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        float[] fArr = rm;
        int i = rmOffset;
        float f = eyeX;
        float f2 = eyeY;
        float f3 = eyeZ;
        float fx = centerX - f;
        float fy = centerY - f2;
        float fz = centerZ - f3;
        float rlf = 1.0f / length(fx, fy, fz);
        float fx2 = fx * rlf;
        float fy2 = fy * rlf;
        float fz2 = fz * rlf;
        float sx = (fy2 * upZ) - (fz2 * upY);
        float sy = (fz2 * upX) - (fx2 * upZ);
        float sz = (fx2 * upY) - (fy2 * upX);
        float rls = 1.0f / length(sx, sy, sz);
        float sx2 = sx * rls;
        float sy2 = sy * rls;
        float sz2 = sz * rls;
        fArr[i + 0] = sx2;
        fArr[i + 1] = (sy2 * fz2) - (sz2 * fy2);
        fArr[i + 2] = -fx2;
        fArr[i + 3] = 0.0f;
        fArr[i + 4] = sy2;
        fArr[i + 5] = (sz2 * fx2) - (sx2 * fz2);
        float f4 = sy2;
        fArr[i + 6] = -fy2;
        fArr[i + 7] = 0.0f;
        fArr[i + 8] = sz2;
        fArr[i + 9] = (sx2 * fy2) - (sy2 * fx2);
        fArr[i + 10] = -fz2;
        fArr[i + 11] = 0.0f;
        fArr[i + 12] = 0.0f;
        fArr[i + 13] = 0.0f;
        fArr[i + 14] = 0.0f;
        fArr[i + 15] = 1.0f;
        translateM(fArr, i, -f, -f2, -f3);
    }
}
