package android.renderscript;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class ScriptIntrinsicBLAS extends ScriptIntrinsic {
    public static final int CONJ_TRANSPOSE = 113;
    public static final int LEFT = 141;
    public static final int LOWER = 122;
    public static final int NON_UNIT = 131;
    public static final int NO_TRANSPOSE = 111;
    public static final int RIGHT = 142;
    private static final int RsBlas_bnnm = 1000;
    private static final int RsBlas_caxpy = 29;
    private static final int RsBlas_ccopy = 28;
    private static final int RsBlas_cdotc_sub = 6;
    private static final int RsBlas_cdotu_sub = 5;
    private static final int RsBlas_cgbmv = 64;
    private static final int RsBlas_cgemm = 125;
    private static final int RsBlas_cgemv = 63;
    private static final int RsBlas_cgerc = 99;
    private static final int RsBlas_cgeru = 98;
    private static final int RsBlas_chbmv = 96;
    private static final int RsBlas_chemm = 137;
    private static final int RsBlas_chemv = 95;
    private static final int RsBlas_cher = 100;
    private static final int RsBlas_cher2 = 102;
    private static final int RsBlas_cher2k = 139;
    private static final int RsBlas_cherk = 138;
    private static final int RsBlas_chpmv = 97;
    private static final int RsBlas_chpr = 101;
    private static final int RsBlas_chpr2 = 103;
    private static final int RsBlas_cscal = 43;
    private static final int RsBlas_csscal = 45;
    private static final int RsBlas_cswap = 27;
    private static final int RsBlas_csymm = 126;
    private static final int RsBlas_csyr2k = 128;
    private static final int RsBlas_csyrk = 127;
    private static final int RsBlas_ctbmv = 66;
    private static final int RsBlas_ctbsv = 69;
    private static final int RsBlas_ctpmv = 67;
    private static final int RsBlas_ctpsv = 70;
    private static final int RsBlas_ctrmm = 129;
    private static final int RsBlas_ctrmv = 65;
    private static final int RsBlas_ctrsm = 130;
    private static final int RsBlas_ctrsv = 68;
    private static final int RsBlas_dasum = 12;
    private static final int RsBlas_daxpy = 26;
    private static final int RsBlas_dcopy = 25;
    private static final int RsBlas_ddot = 4;
    private static final int RsBlas_dgbmv = 56;
    private static final int RsBlas_dgemm = 119;
    private static final int RsBlas_dgemv = 55;
    private static final int RsBlas_dger = 90;
    private static final int RsBlas_dnrm2 = 11;
    private static final int RsBlas_drot = 39;
    private static final int RsBlas_drotg = 37;
    private static final int RsBlas_drotm = 40;
    private static final int RsBlas_drotmg = 38;
    private static final int RsBlas_dsbmv = 88;
    private static final int RsBlas_dscal = 42;
    private static final int RsBlas_dsdot = 2;
    private static final int RsBlas_dspmv = 89;
    private static final int RsBlas_dspr = 92;
    private static final int RsBlas_dspr2 = 94;
    private static final int RsBlas_dswap = 24;
    private static final int RsBlas_dsymm = 120;
    private static final int RsBlas_dsymv = 87;
    private static final int RsBlas_dsyr = 91;
    private static final int RsBlas_dsyr2 = 93;
    private static final int RsBlas_dsyr2k = 122;
    private static final int RsBlas_dsyrk = 121;
    private static final int RsBlas_dtbmv = 58;
    private static final int RsBlas_dtbsv = 61;
    private static final int RsBlas_dtpmv = 59;
    private static final int RsBlas_dtpsv = 62;
    private static final int RsBlas_dtrmm = 123;
    private static final int RsBlas_dtrmv = 57;
    private static final int RsBlas_dtrsm = 124;
    private static final int RsBlas_dtrsv = 60;
    private static final int RsBlas_dzasum = 16;
    private static final int RsBlas_dznrm2 = 15;
    private static final int RsBlas_icamax = 19;
    private static final int RsBlas_idamax = 18;
    private static final int RsBlas_isamax = 17;
    private static final int RsBlas_izamax = 20;
    private static final int RsBlas_sasum = 10;
    private static final int RsBlas_saxpy = 23;
    private static final int RsBlas_scasum = 14;
    private static final int RsBlas_scnrm2 = 13;
    private static final int RsBlas_scopy = 22;
    private static final int RsBlas_sdot = 3;
    private static final int RsBlas_sdsdot = 1;
    private static final int RsBlas_sgbmv = 48;
    private static final int RsBlas_sgemm = 113;
    private static final int RsBlas_sgemv = 47;
    private static final int RsBlas_sger = 82;
    private static final int RsBlas_snrm2 = 9;
    private static final int RsBlas_srot = 35;
    private static final int RsBlas_srotg = 33;
    private static final int RsBlas_srotm = 36;
    private static final int RsBlas_srotmg = 34;
    private static final int RsBlas_ssbmv = 80;
    private static final int RsBlas_sscal = 41;
    private static final int RsBlas_sspmv = 81;
    private static final int RsBlas_sspr = 84;
    private static final int RsBlas_sspr2 = 86;
    private static final int RsBlas_sswap = 21;
    private static final int RsBlas_ssymm = 114;
    private static final int RsBlas_ssymv = 79;
    private static final int RsBlas_ssyr = 83;
    private static final int RsBlas_ssyr2 = 85;
    private static final int RsBlas_ssyr2k = 116;
    private static final int RsBlas_ssyrk = 115;
    private static final int RsBlas_stbmv = 50;
    private static final int RsBlas_stbsv = 53;
    private static final int RsBlas_stpmv = 51;
    private static final int RsBlas_stpsv = 54;
    private static final int RsBlas_strmm = 117;
    private static final int RsBlas_strmv = 49;
    private static final int RsBlas_strsm = 118;
    private static final int RsBlas_strsv = 52;
    private static final int RsBlas_zaxpy = 32;
    private static final int RsBlas_zcopy = 31;
    private static final int RsBlas_zdotc_sub = 8;
    private static final int RsBlas_zdotu_sub = 7;
    private static final int RsBlas_zdscal = 46;
    private static final int RsBlas_zgbmv = 72;
    private static final int RsBlas_zgemm = 131;
    private static final int RsBlas_zgemv = 71;
    private static final int RsBlas_zgerc = 108;
    private static final int RsBlas_zgeru = 107;
    private static final int RsBlas_zhbmv = 105;
    private static final int RsBlas_zhemm = 140;
    private static final int RsBlas_zhemv = 104;
    private static final int RsBlas_zher = 109;
    private static final int RsBlas_zher2 = 111;
    private static final int RsBlas_zher2k = 142;
    private static final int RsBlas_zherk = 141;
    private static final int RsBlas_zhpmv = 106;
    private static final int RsBlas_zhpr = 110;
    private static final int RsBlas_zhpr2 = 112;
    private static final int RsBlas_zscal = 44;
    private static final int RsBlas_zswap = 30;
    private static final int RsBlas_zsymm = 132;
    private static final int RsBlas_zsyr2k = 134;
    private static final int RsBlas_zsyrk = 133;
    private static final int RsBlas_ztbmv = 74;
    private static final int RsBlas_ztbsv = 77;
    private static final int RsBlas_ztpmv = 75;
    private static final int RsBlas_ztpsv = 78;
    private static final int RsBlas_ztrmm = 135;
    private static final int RsBlas_ztrmv = 73;
    private static final int RsBlas_ztrsm = 136;
    private static final int RsBlas_ztrsv = 76;
    public static final int TRANSPOSE = 112;
    public static final int UNIT = 132;
    public static final int UPPER = 121;
    private Allocation mLUT;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Diag {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Side {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Transpose {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Uplo {
    }

    private ScriptIntrinsicBLAS(long id, RenderScript rs) {
        super(id, rs);
    }

    public static ScriptIntrinsicBLAS create(RenderScript rs) {
        return new ScriptIntrinsicBLAS(rs.nScriptIntrinsicCreate(13, Element.U32(rs).getID(rs)), rs);
    }

    static void validateSide(int Side2) {
        if (Side2 != 141 && Side2 != 142) {
            throw new RSRuntimeException("Invalid side passed to BLAS");
        }
    }

    static void validateTranspose(int Trans) {
        if (Trans != 111 && Trans != 112 && Trans != 113) {
            throw new RSRuntimeException("Invalid transpose passed to BLAS");
        }
    }

    static void validateConjTranspose(int Trans) {
        if (Trans != 111 && Trans != 113) {
            throw new RSRuntimeException("Invalid transpose passed to BLAS");
        }
    }

    static void validateDiag(int Diag2) {
        if (Diag2 != 131 && Diag2 != 132) {
            throw new RSRuntimeException("Invalid diag passed to BLAS");
        }
    }

    static void validateUplo(int Uplo2) {
        if (Uplo2 != 121 && Uplo2 != 122) {
            throw new RSRuntimeException("Invalid uplo passed to BLAS");
        }
    }

    static void validateGEMV(Element e, int TransA, Allocation A, Allocation X, int incX, Allocation Y, int incY) {
        int expectedXDim;
        int expectedYDim;
        validateTranspose(TransA);
        int M = A.getType().getY();
        int N = A.getType().getX();
        if (!A.getType().getElement().isCompatible(e) || !X.getType().getElement().isCompatible(e) || !Y.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        } else if (X.getType().getY() > 1 || Y.getType().getY() > 1) {
            throw new RSRuntimeException("BLAS vectors must have Y dimension of 0 or 1");
        } else if (incX <= 0 || incY <= 0) {
            throw new RSRuntimeException("Vector increments must be greater than 0");
        } else {
            if (TransA == 111) {
                expectedXDim = ((N - 1) * incX) + 1;
                expectedYDim = ((M - 1) * incY) + 1;
            } else {
                expectedXDim = ((M - 1) * incX) + 1;
                expectedYDim = ((N - 1) * incY) + 1;
            }
            if (X.getType().getX() != expectedXDim || Y.getType().getX() != expectedYDim) {
                throw new RSRuntimeException("Incorrect vector dimensions for GEMV");
            }
        }
    }

    public void SGEMV(int TransA, float alpha, Allocation A, Allocation X, int incX, float beta, Allocation Y, int incY) {
        validateGEMV(Element.F32(this.mRS), TransA, A, X, incX, Y, incY);
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 47, TransA, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha, A.getID(this.mRS), X.getID(this.mRS), beta, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void DGEMV(int TransA, double alpha, Allocation A, Allocation X, int incX, double beta, Allocation Y, int incY) {
        validateGEMV(Element.F64(this.mRS), TransA, A, X, incX, Y, incY);
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 55, TransA, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha, A.getID(this.mRS), X.getID(this.mRS), beta, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void CGEMV(int TransA, Float2 alpha, Allocation A, Allocation X, int incX, Float2 beta, Allocation Y, int incY) {
        validateGEMV(Element.F32_2(this.mRS), TransA, A, X, incX, Y, incY);
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 63, TransA, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), X.getID(this.mRS), beta.x, beta.y, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void ZGEMV(int TransA, Double2 alpha, Allocation A, Allocation X, int incX, Double2 beta, Allocation Y, int incY) {
        validateGEMV(Element.F64_2(this.mRS), TransA, A, X, incX, Y, incY);
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 71, TransA, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), X.getID(this.mRS), beta.x, beta.y, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void SGBMV(int TransA, int KL, int KU, float alpha, Allocation A, Allocation X, int incX, float beta, Allocation Y, int incY) {
        validateGEMV(Element.F32(this.mRS), TransA, A, X, incX, Y, incY);
        if (KL < 0 || KU < 0) {
            throw new RSRuntimeException("KL and KU must be greater than or equal to 0");
        }
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 48, TransA, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha, A.getID(this.mRS), X.getID(this.mRS), beta, Y.getID(this.mRS), incX, incY, KL, KU);
    }

    public void DGBMV(int TransA, int KL, int KU, double alpha, Allocation A, Allocation X, int incX, double beta, Allocation Y, int incY) {
        validateGEMV(Element.F64(this.mRS), TransA, A, X, incX, Y, incY);
        if (KL < 0 || KU < 0) {
            throw new RSRuntimeException("KL and KU must be greater than or equal to 0");
        }
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 56, TransA, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha, A.getID(this.mRS), X.getID(this.mRS), beta, Y.getID(this.mRS), incX, incY, KL, KU);
    }

    public void CGBMV(int TransA, int KL, int KU, Float2 alpha, Allocation A, Allocation X, int incX, Float2 beta, Allocation Y, int incY) {
        validateGEMV(Element.F32_2(this.mRS), TransA, A, X, incX, Y, incY);
        if (KL < 0 || KU < 0) {
            throw new RSRuntimeException("KL and KU must be greater than or equal to 0");
        }
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 64, TransA, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), X.getID(this.mRS), beta.x, beta.y, Y.getID(this.mRS), incX, incY, KL, KU);
    }

    public void ZGBMV(int TransA, int KL, int KU, Double2 alpha, Allocation A, Allocation X, int incX, Double2 beta, Allocation Y, int incY) {
        validateGEMV(Element.F64_2(this.mRS), TransA, A, X, incX, Y, incY);
        if (KL < 0 || KU < 0) {
            throw new RSRuntimeException("KL and KU must be greater than or equal to 0");
        }
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 72, TransA, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), X.getID(this.mRS), beta.x, beta.y, Y.getID(this.mRS), incX, incY, KL, KU);
    }

    static void validateTRMV(Element e, int Uplo2, int TransA, int Diag2, Allocation A, Allocation X, int incX) {
        validateTranspose(TransA);
        validateUplo(Uplo2);
        validateDiag(Diag2);
        int N = A.getType().getY();
        if (A.getType().getX() != N) {
            throw new RSRuntimeException("A must be a square matrix for TRMV");
        } else if (!A.getType().getElement().isCompatible(e) || !X.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        } else if (X.getType().getY() > 1) {
            throw new RSRuntimeException("BLAS vectors must have Y dimension of 0 or 1");
        } else if (incX > 0) {
            if (X.getType().getX() != ((N - 1) * incX) + 1) {
                throw new RSRuntimeException("Incorrect vector dimensions for TRMV");
            }
        } else {
            throw new RSRuntimeException("Vector increments must be greater than 0");
        }
    }

    static int validateTPMV(Element e, int Uplo2, int TransA, int Diag2, Allocation Ap, Allocation X, int incX) {
        validateTranspose(TransA);
        validateUplo(Uplo2);
        validateDiag(Diag2);
        if (!Ap.getType().getElement().isCompatible(e) || !X.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        } else if (X.getType().getY() > 1) {
            throw new RSRuntimeException("BLAS vectors must have Y dimension of 0 or 1");
        } else if (Ap.getType().getY() <= 1) {
            int N = (int) Math.sqrt(((double) Ap.getType().getX()) * 2.0d);
            if (Ap.getType().getX() != ((N + 1) * N) / 2) {
                throw new RSRuntimeException("Invalid dimension for Ap");
            } else if (incX > 0) {
                if (X.getType().getX() == ((N - 1) * incX) + 1) {
                    return N;
                }
                throw new RSRuntimeException("Incorrect vector dimensions for TPMV");
            } else {
                throw new RSRuntimeException("Vector increments must be greater than 0");
            }
        } else {
            throw new RSRuntimeException("Ap must have a Y dimension of 0 or 1");
        }
    }

    public void STRMV(int Uplo2, int TransA, int Diag2, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F32(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 49, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), 0, 0.0f, A.getID(this.mRS), X.getID(this.mRS), 0.0f, 0, incX, 0, 0, 0);
    }

    public void DTRMV(int Uplo2, int TransA, int Diag2, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F64(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 57, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), 0, 0.0d, A.getID(this.mRS), X.getID(this.mRS), 0.0d, 0, incX, 0, 0, 0);
    }

    public void CTRMV(int Uplo2, int TransA, int Diag2, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F32_2(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 65, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), 0, 0.0f, 0.0f, A.getID(this.mRS), X.getID(this.mRS), 0.0f, 0.0f, 0, incX, 0, 0, 0);
    }

    public void ZTRMV(int Uplo2, int TransA, int Diag2, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F64_2(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 73, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), 0, 0.0d, 0.0d, A.getID(this.mRS), X.getID(this.mRS), 0.0d, 0.0d, 0, incX, 0, 0, 0);
    }

    public void STBMV(int Uplo2, int TransA, int Diag2, int K, Allocation A, Allocation X, int incX) {
        if (K >= 0) {
            validateTRMV(Element.F32(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
            this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 50, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), K, 0.0f, A.getID(this.mRS), X.getID(this.mRS), 0.0f, 0, incX, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("K must be greater than or equal to 0");
    }

    public void DTBMV(int Uplo2, int TransA, int Diag2, int K, Allocation A, Allocation X, int incX) {
        if (K >= 0) {
            validateTRMV(Element.F64(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
            this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 58, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), K, 0.0d, A.getID(this.mRS), X.getID(this.mRS), 0.0d, 0, incX, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("K must be greater than or equal to 0");
    }

    public void CTBMV(int Uplo2, int TransA, int Diag2, int K, Allocation A, Allocation X, int incX) {
        if (K >= 0) {
            validateTRMV(Element.F32_2(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
            this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 66, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), K, 0.0f, 0.0f, A.getID(this.mRS), X.getID(this.mRS), 0.0f, 0.0f, 0, incX, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("K must be greater than or equal to 0");
    }

    public void ZTBMV(int Uplo2, int TransA, int Diag2, int K, Allocation A, Allocation X, int incX) {
        if (K >= 0) {
            validateTRMV(Element.F64_2(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
            this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 74, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), K, 0.0d, 0.0d, A.getID(this.mRS), X.getID(this.mRS), 0.0d, 0.0d, 0, incX, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("K must be greater than or equal to 0");
    }

    public void STPMV(int Uplo2, int TransA, int Diag2, Allocation Ap, Allocation X, int incX) {
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 51, TransA, 0, 0, Uplo2, Diag2, 0, validateTPMV(Element.F32(this.mRS), Uplo2, TransA, Diag2, Ap, X, incX), 0, 0.0f, Ap.getID(this.mRS), X.getID(this.mRS), 0.0f, 0, incX, 0, 0, 0);
    }

    public void DTPMV(int Uplo2, int TransA, int Diag2, Allocation Ap, Allocation X, int incX) {
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 59, TransA, 0, 0, Uplo2, Diag2, 0, validateTPMV(Element.F64(this.mRS), Uplo2, TransA, Diag2, Ap, X, incX), 0, 0.0d, Ap.getID(this.mRS), X.getID(this.mRS), 0.0d, 0, incX, 0, 0, 0);
    }

    public void CTPMV(int Uplo2, int TransA, int Diag2, Allocation Ap, Allocation X, int incX) {
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 67, TransA, 0, 0, Uplo2, Diag2, 0, validateTPMV(Element.F32_2(this.mRS), Uplo2, TransA, Diag2, Ap, X, incX), 0, 0.0f, 0.0f, Ap.getID(this.mRS), X.getID(this.mRS), 0.0f, 0.0f, 0, incX, 0, 0, 0);
    }

    public void ZTPMV(int Uplo2, int TransA, int Diag2, Allocation Ap, Allocation X, int incX) {
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 75, TransA, 0, 0, Uplo2, Diag2, 0, validateTPMV(Element.F64_2(this.mRS), Uplo2, TransA, Diag2, Ap, X, incX), 0, 0.0d, 0.0d, Ap.getID(this.mRS), X.getID(this.mRS), 0.0d, 0.0d, 0, incX, 0, 0, 0);
    }

    public void STRSV(int Uplo2, int TransA, int Diag2, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F32(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 52, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), 0, 0.0f, A.getID(this.mRS), X.getID(this.mRS), 0.0f, 0, incX, 0, 0, 0);
    }

    public void DTRSV(int Uplo2, int TransA, int Diag2, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F64(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 60, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), 0, 0.0d, A.getID(this.mRS), X.getID(this.mRS), 0.0d, 0, incX, 0, 0, 0);
    }

    public void CTRSV(int Uplo2, int TransA, int Diag2, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F32_2(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 68, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), 0, 0.0f, 0.0f, A.getID(this.mRS), X.getID(this.mRS), 0.0f, 0.0f, 0, incX, 0, 0, 0);
    }

    public void ZTRSV(int Uplo2, int TransA, int Diag2, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F64_2(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 76, TransA, 0, 0, Uplo2, Diag2, 0, A.getType().getY(), 0, 0.0d, 0.0d, A.getID(this.mRS), X.getID(this.mRS), 0.0d, 0.0d, 0, incX, 0, 0, 0);
    }

    public void STBSV(int Uplo2, int TransA, int Diag2, int K, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F32(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        int N = A.getType().getY();
        if (K >= 0) {
            this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 53, TransA, 0, 0, Uplo2, Diag2, 0, N, K, 0.0f, A.getID(this.mRS), X.getID(this.mRS), 0.0f, 0, incX, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("Number of diagonals must be positive");
    }

    public void DTBSV(int Uplo2, int TransA, int Diag2, int K, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F64(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        int N = A.getType().getY();
        if (K >= 0) {
            this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 61, TransA, 0, 0, Uplo2, Diag2, 0, N, K, 0.0d, A.getID(this.mRS), X.getID(this.mRS), 0.0d, 0, incX, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("Number of diagonals must be positive");
    }

    public void CTBSV(int Uplo2, int TransA, int Diag2, int K, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F32_2(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        int N = A.getType().getY();
        if (K >= 0) {
            this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 69, TransA, 0, 0, Uplo2, Diag2, 0, N, K, 0.0f, 0.0f, A.getID(this.mRS), X.getID(this.mRS), 0.0f, 0.0f, 0, incX, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("Number of diagonals must be positive");
    }

    public void ZTBSV(int Uplo2, int TransA, int Diag2, int K, Allocation A, Allocation X, int incX) {
        validateTRMV(Element.F64_2(this.mRS), Uplo2, TransA, Diag2, A, X, incX);
        int N = A.getType().getY();
        if (K >= 0) {
            this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 77, TransA, 0, 0, Uplo2, Diag2, 0, N, K, 0.0d, 0.0d, A.getID(this.mRS), X.getID(this.mRS), 0.0d, 0.0d, 0, incX, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("Number of diagonals must be positive");
    }

    public void STPSV(int Uplo2, int TransA, int Diag2, Allocation Ap, Allocation X, int incX) {
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 54, TransA, 0, 0, Uplo2, Diag2, 0, validateTPMV(Element.F32(this.mRS), Uplo2, TransA, Diag2, Ap, X, incX), 0, 0.0f, Ap.getID(this.mRS), X.getID(this.mRS), 0.0f, 0, incX, 0, 0, 0);
    }

    public void DTPSV(int Uplo2, int TransA, int Diag2, Allocation Ap, Allocation X, int incX) {
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 62, TransA, 0, 0, Uplo2, Diag2, 0, validateTPMV(Element.F64(this.mRS), Uplo2, TransA, Diag2, Ap, X, incX), 0, 0.0d, Ap.getID(this.mRS), X.getID(this.mRS), 0.0d, 0, incX, 0, 0, 0);
    }

    public void CTPSV(int Uplo2, int TransA, int Diag2, Allocation Ap, Allocation X, int incX) {
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 70, TransA, 0, 0, Uplo2, Diag2, 0, validateTPMV(Element.F32_2(this.mRS), Uplo2, TransA, Diag2, Ap, X, incX), 0, 0.0f, 0.0f, Ap.getID(this.mRS), X.getID(this.mRS), 0.0f, 0.0f, 0, incX, 0, 0, 0);
    }

    public void ZTPSV(int Uplo2, int TransA, int Diag2, Allocation Ap, Allocation X, int incX) {
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 78, TransA, 0, 0, Uplo2, Diag2, 0, validateTPMV(Element.F64_2(this.mRS), Uplo2, TransA, Diag2, Ap, X, incX), 0, 0.0d, 0.0d, Ap.getID(this.mRS), X.getID(this.mRS), 0.0d, 0.0d, 0, incX, 0, 0, 0);
    }

    static int validateSYMV(Element e, int Uplo2, Allocation A, Allocation X, Allocation Y, int incX, int incY) {
        validateUplo(Uplo2);
        int N = A.getType().getY();
        if (A.getType().getX() != N) {
            throw new RSRuntimeException("A must be a square matrix for SYMV");
        } else if (!A.getType().getElement().isCompatible(e) || !X.getType().getElement().isCompatible(e) || !Y.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        } else if (X.getType().getY() > 1 || Y.getType().getY() > 1) {
            throw new RSRuntimeException("BLAS vectors must have Y dimension of 0 or 1");
        } else if (incX <= 0 || incY <= 0) {
            throw new RSRuntimeException("Vector increments must be greater than 0");
        } else {
            if (X.getType().getX() == ((N - 1) * incX) + 1) {
                if (Y.getType().getX() == ((N - 1) * incY) + 1) {
                    return N;
                }
                throw new RSRuntimeException("Incorrect vector dimensions for SYMV");
            }
            throw new RSRuntimeException("Incorrect vector dimensions for SYMV");
        }
    }

    static int validateSPMV(Element e, int Uplo2, Allocation Ap, Allocation X, int incX, Allocation Y, int incY) {
        validateUplo(Uplo2);
        if (!Ap.getType().getElement().isCompatible(e) || !X.getType().getElement().isCompatible(e) || !Y.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        } else if (X.getType().getY() > 1 || Y.getType().getY() > 1) {
            throw new RSRuntimeException("BLAS vectors must have Y dimension of 0 or 1");
        } else if (Ap.getType().getY() <= 1) {
            int N = (int) Math.sqrt(((double) Ap.getType().getX()) * 2.0d);
            if (Ap.getType().getX() != ((N + 1) * N) / 2) {
                throw new RSRuntimeException("Invalid dimension for Ap");
            } else if (incX <= 0 || incY <= 0) {
                throw new RSRuntimeException("Vector increments must be greater than 0");
            } else {
                if (X.getType().getX() == ((N - 1) * incX) + 1) {
                    if (Y.getType().getX() == ((N - 1) * incY) + 1) {
                        return N;
                    }
                    throw new RSRuntimeException("Incorrect vector dimensions for SPMV");
                }
                throw new RSRuntimeException("Incorrect vector dimensions for SPMV");
            }
        } else {
            throw new RSRuntimeException("Ap must have a Y dimension of 0 or 1");
        }
    }

    static void validateGER(Element e, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        if (!A.getType().getElement().isCompatible(e) || !X.getType().getElement().isCompatible(e) || !Y.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        } else if (X.getType().getY() > 1 || Y.getType().getY() > 1) {
            throw new RSRuntimeException("BLAS vectors must have Y dimension of 0 or 1");
        } else {
            int M = A.getType().getY();
            int N = A.getType().getX();
            if (N < 1 || M < 1) {
                throw new RSRuntimeException("M and N must be 1 or greater for GER");
            } else if (incX <= 0 || incY <= 0) {
                throw new RSRuntimeException("Vector increments must be greater than 0");
            } else {
                if (X.getType().getX() == ((M - 1) * incX) + 1) {
                    if (Y.getType().getX() != ((N - 1) * incY) + 1) {
                        throw new RSRuntimeException("Incorrect vector dimensions for GER");
                    }
                    return;
                }
                throw new RSRuntimeException("Incorrect vector dimensions for GER");
            }
        }
    }

    static int validateSYR(Element e, int Uplo2, Allocation X, int incX, Allocation A) {
        validateUplo(Uplo2);
        if (!A.getType().getElement().isCompatible(e) || !X.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        }
        int N = A.getType().getX();
        if (X.getType().getY() > 1) {
            throw new RSRuntimeException("BLAS vectors must have Y dimension of 0 or 1");
        } else if (N != A.getType().getY()) {
            throw new RSRuntimeException("A must be a symmetric matrix");
        } else if (incX > 0) {
            if (X.getType().getX() == ((N - 1) * incX) + 1) {
                return N;
            }
            throw new RSRuntimeException("Incorrect vector dimensions for SYR");
        } else {
            throw new RSRuntimeException("Vector increments must be greater than 0");
        }
    }

    static int validateSPR(Element e, int Uplo2, Allocation X, int incX, Allocation Ap) {
        validateUplo(Uplo2);
        if (!Ap.getType().getElement().isCompatible(e) || !X.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        } else if (X.getType().getY() > 1) {
            throw new RSRuntimeException("BLAS vectors must have Y dimension of 0 or 1");
        } else if (Ap.getType().getY() <= 1) {
            int N = (int) Math.sqrt(((double) Ap.getType().getX()) * 2.0d);
            if (Ap.getType().getX() != ((N + 1) * N) / 2) {
                throw new RSRuntimeException("Invalid dimension for Ap");
            } else if (incX > 0) {
                if (X.getType().getX() == ((N - 1) * incX) + 1) {
                    return N;
                }
                throw new RSRuntimeException("Incorrect vector dimensions for SPR");
            } else {
                throw new RSRuntimeException("Vector increments must be greater than 0");
            }
        } else {
            throw new RSRuntimeException("Ap must have a Y dimension of 0 or 1");
        }
    }

    static int validateSYR2(Element e, int Uplo2, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        validateUplo(Uplo2);
        if (!A.getType().getElement().isCompatible(e) || !X.getType().getElement().isCompatible(e) || !Y.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        } else if (X.getType().getY() > 1 || Y.getType().getY() > 1) {
            throw new RSRuntimeException("BLAS vectors must have Y dimension of 0 or 1");
        } else {
            int N = A.getType().getX();
            if (N != A.getType().getY()) {
                throw new RSRuntimeException("A must be a symmetric matrix");
            } else if (incX <= 0 || incY <= 0) {
                throw new RSRuntimeException("Vector increments must be greater than 0");
            } else {
                int expectedYDim = ((N - 1) * incY) + 1;
                if (X.getType().getX() == ((N - 1) * incX) + 1 && Y.getType().getX() == expectedYDim) {
                    return N;
                }
                throw new RSRuntimeException("Incorrect vector dimensions for SYR");
            }
        }
    }

    static int validateSPR2(Element e, int Uplo2, Allocation X, int incX, Allocation Y, int incY, Allocation Ap) {
        validateUplo(Uplo2);
        if (!Ap.getType().getElement().isCompatible(e) || !X.getType().getElement().isCompatible(e) || !Y.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        } else if (X.getType().getY() > 1 || Y.getType().getY() > 1) {
            throw new RSRuntimeException("BLAS vectors must have Y dimension of 0 or 1");
        } else if (Ap.getType().getY() <= 1) {
            int N = (int) Math.sqrt(((double) Ap.getType().getX()) * 2.0d);
            if (Ap.getType().getX() != ((N + 1) * N) / 2) {
                throw new RSRuntimeException("Invalid dimension for Ap");
            } else if (incX <= 0 || incY <= 0) {
                throw new RSRuntimeException("Vector increments must be greater than 0");
            } else {
                int expectedYDim = ((N - 1) * incY) + 1;
                if (X.getType().getX() == ((N - 1) * incX) + 1 && Y.getType().getX() == expectedYDim) {
                    return N;
                }
                throw new RSRuntimeException("Incorrect vector dimensions for SPR2");
            }
        } else {
            throw new RSRuntimeException("Ap must have a Y dimension of 0 or 1");
        }
    }

    public void SSYMV(int Uplo2, float alpha, Allocation A, Allocation X, int incX, float beta, Allocation Y, int incY) {
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 79, 0, 0, 0, Uplo2, 0, 0, validateSYMV(Element.F32(this.mRS), Uplo2, A, X, Y, incX, incY), 0, alpha, A.getID(this.mRS), X.getID(this.mRS), beta, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void SSBMV(int Uplo2, int K, float alpha, Allocation A, Allocation X, int incX, float beta, Allocation Y, int incY) {
        if (K >= 0) {
            this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 80, 0, 0, 0, Uplo2, 0, 0, validateSYMV(Element.F32(this.mRS), Uplo2, A, X, Y, incX, incY), K, alpha, A.getID(this.mRS), X.getID(this.mRS), beta, Y.getID(this.mRS), incX, incY, 0, 0);
            return;
        }
        throw new RSRuntimeException("K must be greater than or equal to 0");
    }

    public void SSPMV(int Uplo2, float alpha, Allocation Ap, Allocation X, int incX, float beta, Allocation Y, int incY) {
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 81, 0, 0, 0, Uplo2, 0, 0, validateSPMV(Element.F32(this.mRS), Uplo2, Ap, X, incX, Y, incY), 0, alpha, Ap.getID(this.mRS), X.getID(this.mRS), beta, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void SGER(float alpha, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        int M = A.getType().getY();
        int N = A.getType().getX();
        validateGER(Element.F32(this.mRS), X, incX, Y, incY, A);
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 82, 0, 0, 0, 0, 0, M, N, 0, alpha, X.getID(this.mRS), Y.getID(this.mRS), 0.0f, A.getID(this.mRS), incX, incY, 0, 0);
    }

    public void SSYR(int Uplo2, float alpha, Allocation X, int incX, Allocation A) {
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 83, 0, 0, 0, Uplo2, 0, 0, validateSYR(Element.F32(this.mRS), Uplo2, X, incX, A), 0, alpha, X.getID(this.mRS), A.getID(this.mRS), 0.0f, 0, incX, 0, 0, 0);
    }

    public void SSPR(int Uplo2, float alpha, Allocation X, int incX, Allocation Ap) {
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 84, 0, 0, 0, Uplo2, 0, 0, validateSPR(Element.F32(this.mRS), Uplo2, X, incX, Ap), 0, alpha, X.getID(this.mRS), Ap.getID(this.mRS), 0.0f, 0, incX, 0, 0, 0);
    }

    public void SSYR2(int Uplo2, float alpha, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 85, 0, 0, 0, Uplo2, 0, 0, validateSYR2(Element.F32(this.mRS), Uplo2, X, incX, Y, incY, A), 0, alpha, X.getID(this.mRS), Y.getID(this.mRS), 0.0f, A.getID(this.mRS), incX, incY, 0, 0);
    }

    public void SSPR2(int Uplo2, float alpha, Allocation X, int incX, Allocation Y, int incY, Allocation Ap) {
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 86, 0, 0, 0, Uplo2, 0, 0, validateSPR2(Element.F32(this.mRS), Uplo2, X, incX, Y, incY, Ap), 0, alpha, X.getID(this.mRS), Y.getID(this.mRS), 0.0f, Ap.getID(this.mRS), incX, incY, 0, 0);
    }

    public void DSYMV(int Uplo2, double alpha, Allocation A, Allocation X, int incX, double beta, Allocation Y, int incY) {
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 87, 0, 0, 0, Uplo2, 0, 0, validateSYMV(Element.F64(this.mRS), Uplo2, A, X, Y, incX, incY), 0, alpha, A.getID(this.mRS), X.getID(this.mRS), beta, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void DSBMV(int Uplo2, int K, double alpha, Allocation A, Allocation X, int incX, double beta, Allocation Y, int incY) {
        if (K >= 0) {
            this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 88, 0, 0, 0, Uplo2, 0, 0, validateSYMV(Element.F64(this.mRS), Uplo2, A, X, Y, incX, incY), K, alpha, A.getID(this.mRS), X.getID(this.mRS), beta, Y.getID(this.mRS), incX, incY, 0, 0);
            return;
        }
        throw new RSRuntimeException("K must be greater than or equal to 0");
    }

    public void DSPMV(int Uplo2, double alpha, Allocation Ap, Allocation X, int incX, double beta, Allocation Y, int incY) {
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 89, 0, 0, 0, Uplo2, 0, 0, validateSPMV(Element.F64(this.mRS), Uplo2, Ap, X, incX, Y, incY), 0, alpha, Ap.getID(this.mRS), X.getID(this.mRS), beta, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void DGER(double alpha, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        int M = A.getType().getY();
        int N = A.getType().getX();
        validateGER(Element.F64(this.mRS), X, incX, Y, incY, A);
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 90, 0, 0, 0, 0, 0, M, N, 0, alpha, X.getID(this.mRS), Y.getID(this.mRS), 0.0d, A.getID(this.mRS), incX, incY, 0, 0);
    }

    public void DSYR(int Uplo2, double alpha, Allocation X, int incX, Allocation A) {
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 91, 0, 0, 0, Uplo2, 0, 0, validateSYR(Element.F64(this.mRS), Uplo2, X, incX, A), 0, alpha, X.getID(this.mRS), A.getID(this.mRS), 0.0d, 0, incX, 0, 0, 0);
    }

    public void DSPR(int Uplo2, double alpha, Allocation X, int incX, Allocation Ap) {
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 92, 0, 0, 0, Uplo2, 0, 0, validateSPR(Element.F64(this.mRS), Uplo2, X, incX, Ap), 0, alpha, X.getID(this.mRS), Ap.getID(this.mRS), 0.0d, 0, incX, 0, 0, 0);
    }

    public void DSYR2(int Uplo2, double alpha, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 93, 0, 0, 0, Uplo2, 0, 0, validateSYR2(Element.F64(this.mRS), Uplo2, X, incX, Y, incY, A), 0, alpha, X.getID(this.mRS), Y.getID(this.mRS), 0.0d, A.getID(this.mRS), incX, incY, 0, 0);
    }

    public void DSPR2(int Uplo2, double alpha, Allocation X, int incX, Allocation Y, int incY, Allocation Ap) {
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 94, 0, 0, 0, Uplo2, 0, 0, validateSPR2(Element.F64(this.mRS), Uplo2, X, incX, Y, incY, Ap), 0, alpha, X.getID(this.mRS), Y.getID(this.mRS), 0.0d, Ap.getID(this.mRS), incX, incY, 0, 0);
    }

    static void validateGERU(Element e, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        if (!A.getType().getElement().isCompatible(e) || !X.getType().getElement().isCompatible(e) || !Y.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        } else if (X.getType().getY() > 1 || Y.getType().getY() > 1) {
            throw new RSRuntimeException("BLAS vectors must have Y dimension of 0 or 1");
        } else {
            int M = A.getType().getY();
            int N = A.getType().getX();
            if (incX <= 0 || incY <= 0) {
                throw new RSRuntimeException("Vector increments must be greater than 0");
            }
            if (X.getType().getX() == ((M - 1) * incX) + 1) {
                if (Y.getType().getX() != ((N - 1) * incY) + 1) {
                    throw new RSRuntimeException("Incorrect vector dimensions for GERU");
                }
                return;
            }
            throw new RSRuntimeException("Incorrect vector dimensions for GERU");
        }
    }

    public void CHEMV(int Uplo2, Float2 alpha, Allocation A, Allocation X, int incX, Float2 beta, Allocation Y, int incY) {
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 95, 0, 0, 0, Uplo2, 0, 0, validateSYR2(Element.F32_2(this.mRS), Uplo2, X, incX, Y, incY, A), 0, alpha.x, alpha.y, A.getID(this.mRS), X.getID(this.mRS), beta.x, beta.y, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void CHBMV(int Uplo2, int K, Float2 alpha, Allocation A, Allocation X, int incX, Float2 beta, Allocation Y, int incY) {
        int N = validateSYR2(Element.F32_2(this.mRS), Uplo2, X, incX, Y, incY, A);
        if (K >= 0) {
            this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 96, 0, 0, 0, Uplo2, 0, 0, N, K, alpha.x, alpha.y, A.getID(this.mRS), X.getID(this.mRS), beta.x, beta.y, Y.getID(this.mRS), incX, incY, 0, 0);
            return;
        }
        throw new RSRuntimeException("K must be 0 or greater for HBMV");
    }

    public void CHPMV(int Uplo2, Float2 alpha, Allocation Ap, Allocation X, int incX, Float2 beta, Allocation Y, int incY) {
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 97, 0, 0, 0, Uplo2, 0, 0, validateSPR2(Element.F32_2(this.mRS), Uplo2, X, incX, Y, incY, Ap), 0, alpha.x, alpha.y, Ap.getID(this.mRS), X.getID(this.mRS), beta.x, beta.y, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void CGERU(Float2 alpha, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        validateGERU(Element.F32_2(this.mRS), X, incX, Y, incY, A);
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 98, 0, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha.x, alpha.y, X.getID(this.mRS), Y.getID(this.mRS), 0.0f, 0.0f, A.getID(this.mRS), incX, incY, 0, 0);
    }

    public void CGERC(Float2 alpha, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        validateGERU(Element.F32_2(this.mRS), X, incX, Y, incY, A);
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 99, 0, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha.x, alpha.y, X.getID(this.mRS), Y.getID(this.mRS), 0.0f, 0.0f, A.getID(this.mRS), incX, incY, 0, 0);
    }

    public void CHER(int Uplo2, float alpha, Allocation X, int incX, Allocation A) {
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 100, 0, 0, 0, Uplo2, 0, 0, validateSYR(Element.F32_2(this.mRS), Uplo2, X, incX, A), 0, alpha, 0.0f, X.getID(this.mRS), 0, 0.0f, 0.0f, A.getID(this.mRS), incX, 0, 0, 0);
    }

    public void CHPR(int Uplo2, float alpha, Allocation X, int incX, Allocation Ap) {
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 101, 0, 0, 0, Uplo2, 0, 0, validateSPR(Element.F32_2(this.mRS), Uplo2, X, incX, Ap), 0, alpha, 0.0f, X.getID(this.mRS), 0, 0.0f, 0.0f, Ap.getID(this.mRS), incX, 0, 0, 0);
    }

    public void CHER2(int Uplo2, Float2 alpha, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 102, 0, 0, 0, Uplo2, 0, 0, validateSYR2(Element.F32_2(this.mRS), Uplo2, X, incX, Y, incY, A), 0, alpha.x, alpha.y, X.getID(this.mRS), Y.getID(this.mRS), 0.0f, 0.0f, A.getID(this.mRS), incX, incY, 0, 0);
    }

    public void CHPR2(int Uplo2, Float2 alpha, Allocation X, int incX, Allocation Y, int incY, Allocation Ap) {
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 103, 0, 0, 0, Uplo2, 0, 0, validateSPR2(Element.F32_2(this.mRS), Uplo2, X, incX, Y, incY, Ap), 0, alpha.x, alpha.y, X.getID(this.mRS), Y.getID(this.mRS), 0.0f, 0.0f, Ap.getID(this.mRS), incX, incY, 0, 0);
    }

    public void ZHEMV(int Uplo2, Double2 alpha, Allocation A, Allocation X, int incX, Double2 beta, Allocation Y, int incY) {
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 104, 0, 0, 0, Uplo2, 0, 0, validateSYR2(Element.F64_2(this.mRS), Uplo2, X, incX, Y, incY, A), 0, alpha.x, alpha.y, A.getID(this.mRS), X.getID(this.mRS), beta.x, beta.y, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void ZHBMV(int Uplo2, int K, Double2 alpha, Allocation A, Allocation X, int incX, Double2 beta, Allocation Y, int incY) {
        int N = validateSYR2(Element.F64_2(this.mRS), Uplo2, X, incX, Y, incY, A);
        if (K >= 0) {
            this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 105, 0, 0, 0, Uplo2, 0, 0, N, K, alpha.x, alpha.y, A.getID(this.mRS), X.getID(this.mRS), beta.x, beta.y, Y.getID(this.mRS), incX, incY, 0, 0);
            return;
        }
        throw new RSRuntimeException("K must be 0 or greater for HBMV");
    }

    public void ZHPMV(int Uplo2, Double2 alpha, Allocation Ap, Allocation X, int incX, Double2 beta, Allocation Y, int incY) {
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 106, 0, 0, 0, Uplo2, 0, 0, validateSPR2(Element.F64_2(this.mRS), Uplo2, X, incX, Y, incY, Ap), 0, alpha.x, alpha.y, Ap.getID(this.mRS), X.getID(this.mRS), beta.x, beta.y, Y.getID(this.mRS), incX, incY, 0, 0);
    }

    public void ZGERU(Double2 alpha, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        validateGERU(Element.F64_2(this.mRS), X, incX, Y, incY, A);
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 107, 0, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha.x, alpha.y, X.getID(this.mRS), Y.getID(this.mRS), 0.0d, 0.0d, A.getID(this.mRS), incX, incY, 0, 0);
    }

    public void ZGERC(Double2 alpha, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        validateGERU(Element.F64_2(this.mRS), X, incX, Y, incY, A);
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 108, 0, 0, 0, 0, 0, A.getType().getY(), A.getType().getX(), 0, alpha.x, alpha.y, X.getID(this.mRS), Y.getID(this.mRS), 0.0d, 0.0d, A.getID(this.mRS), incX, incY, 0, 0);
    }

    public void ZHER(int Uplo2, double alpha, Allocation X, int incX, Allocation A) {
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 109, 0, 0, 0, Uplo2, 0, 0, validateSYR(Element.F64_2(this.mRS), Uplo2, X, incX, A), 0, alpha, 0.0d, X.getID(this.mRS), 0, 0.0d, 0.0d, A.getID(this.mRS), incX, 0, 0, 0);
    }

    public void ZHPR(int Uplo2, double alpha, Allocation X, int incX, Allocation Ap) {
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 110, 0, 0, 0, Uplo2, 0, 0, validateSPR(Element.F64_2(this.mRS), Uplo2, X, incX, Ap), 0, alpha, 0.0d, X.getID(this.mRS), 0, 0.0d, 0.0d, Ap.getID(this.mRS), incX, 0, 0, 0);
    }

    public void ZHER2(int Uplo2, Double2 alpha, Allocation X, int incX, Allocation Y, int incY, Allocation A) {
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 111, 0, 0, 0, Uplo2, 0, 0, validateSYR2(Element.F64_2(this.mRS), Uplo2, X, incX, Y, incY, A), 0, alpha.x, alpha.y, X.getID(this.mRS), Y.getID(this.mRS), 0.0d, 0.0d, A.getID(this.mRS), incX, incY, 0, 0);
    }

    public void ZHPR2(int Uplo2, Double2 alpha, Allocation X, int incX, Allocation Y, int incY, Allocation Ap) {
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 112, 0, 0, 0, Uplo2, 0, 0, validateSPR2(Element.F64_2(this.mRS), Uplo2, X, incX, Y, incY, Ap), 0, alpha.x, alpha.y, X.getID(this.mRS), Y.getID(this.mRS), 0.0d, 0.0d, Ap.getID(this.mRS), incX, incY, 0, 0);
    }

    static void validateL3(Element e, int TransA, int TransB, int Side2, Allocation A, Allocation B, Allocation C) {
        int aM = -1;
        int aN = -1;
        int bM = -1;
        int bN = -1;
        if ((A != null && !A.getType().getElement().isCompatible(e)) || ((B != null && !B.getType().getElement().isCompatible(e)) || (C != null && !C.getType().getElement().isCompatible(e)))) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        } else if (C != null) {
            int cM = C.getType().getY();
            int cN = C.getType().getX();
            if (Side2 != 142) {
                if (A != null) {
                    if (TransA == 112 || TransA == 113) {
                        aN = A.getType().getY();
                        aM = A.getType().getX();
                    } else {
                        aM = A.getType().getY();
                        aN = A.getType().getX();
                    }
                }
                if (B != null) {
                    if (TransB == 112 || TransB == 113) {
                        bN = B.getType().getY();
                        bM = B.getType().getX();
                    } else {
                        bM = B.getType().getY();
                        bN = B.getType().getX();
                    }
                }
            } else if ((A != null || B == null) && (A == null || B != null)) {
                if (B != null) {
                    bM = A.getType().getY();
                    bN = A.getType().getX();
                }
                if (A != null) {
                    aM = B.getType().getY();
                    aN = B.getType().getX();
                }
            } else {
                throw new RSRuntimeException("Provided Matrix A without Matrix B, or vice versa");
            }
            if (A == null || B == null) {
                if (A != null) {
                    if (cM != cN) {
                        throw new RSRuntimeException("Matrix C is not symmetric");
                    } else if (aM != cM) {
                        throw new RSRuntimeException("Called BLAS with invalid dimensions");
                    }
                } else if (A != null && B != null && aN != bM) {
                    throw new RSRuntimeException("Called BLAS with invalid dimensions");
                }
            } else if (aN != bM || aM != cM || bN != cN) {
                throw new RSRuntimeException("Called BLAS with invalid dimensions");
            }
        } else {
            throw new RSRuntimeException("Allocation C cannot be null");
        }
    }

    public void SGEMM(int TransA, int TransB, float alpha, Allocation A, Allocation B, float beta, Allocation C) {
        int K;
        int M;
        int N;
        validateTranspose(TransA);
        validateTranspose(TransB);
        validateL3(Element.F32(this.mRS), TransA, TransB, 0, A, B, C);
        if (TransA != 111) {
            M = A.getType().getX();
            K = A.getType().getY();
        } else {
            M = A.getType().getY();
            K = A.getType().getX();
        }
        if (TransB != 111) {
            N = B.getType().getY();
        } else {
            N = B.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 113, TransA, TransB, 0, 0, 0, M, N, K, alpha, A.getID(this.mRS), B.getID(this.mRS), beta, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void DGEMM(int TransA, int TransB, double alpha, Allocation A, Allocation B, double beta, Allocation C) {
        int K;
        int M;
        int N;
        validateTranspose(TransA);
        validateTranspose(TransB);
        validateL3(Element.F64(this.mRS), TransA, TransB, 0, A, B, C);
        if (TransA != 111) {
            M = A.getType().getX();
            K = A.getType().getY();
        } else {
            M = A.getType().getY();
            K = A.getType().getX();
        }
        if (TransB != 111) {
            N = B.getType().getY();
        } else {
            N = B.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 119, TransA, TransB, 0, 0, 0, M, N, K, alpha, A.getID(this.mRS), B.getID(this.mRS), beta, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void CGEMM(int TransA, int TransB, Float2 alpha, Allocation A, Allocation B, Float2 beta, Allocation C) {
        int K;
        int M;
        int N;
        validateTranspose(TransA);
        validateTranspose(TransB);
        validateL3(Element.F32_2(this.mRS), TransA, TransB, 0, A, B, C);
        if (TransA != 111) {
            M = A.getType().getX();
            K = A.getType().getY();
        } else {
            M = A.getType().getY();
            K = A.getType().getX();
        }
        if (TransB != 111) {
            N = B.getType().getY();
        } else {
            N = B.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 125, TransA, TransB, 0, 0, 0, M, N, K, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), beta.x, beta.y, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void ZGEMM(int TransA, int TransB, Double2 alpha, Allocation A, Allocation B, Double2 beta, Allocation C) {
        int K;
        int M;
        int N;
        validateTranspose(TransA);
        validateTranspose(TransB);
        validateL3(Element.F64_2(this.mRS), TransA, TransB, 0, A, B, C);
        if (TransA != 111) {
            M = A.getType().getX();
            K = A.getType().getY();
        } else {
            M = A.getType().getY();
            K = A.getType().getX();
        }
        if (TransB != 111) {
            N = B.getType().getY();
        } else {
            N = B.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 131, TransA, TransB, 0, 0, 0, M, N, K, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), beta.x, beta.y, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void SSYMM(int Side2, int Uplo2, float alpha, Allocation A, Allocation B, float beta, Allocation C) {
        validateSide(Side2);
        validateUplo(Uplo2);
        if (A.getType().getX() == A.getType().getY()) {
            validateL3(Element.F32(this.mRS), 0, 0, Side2, A, B, C);
            this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 114, 0, 0, Side2, Uplo2, 0, C.getType().getY(), C.getType().getX(), 0, alpha, A.getID(this.mRS), B.getID(this.mRS), beta, C.getID(this.mRS), 0, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("Matrix A is not symmetric");
    }

    public void DSYMM(int Side2, int Uplo2, double alpha, Allocation A, Allocation B, double beta, Allocation C) {
        validateSide(Side2);
        validateUplo(Uplo2);
        if (A.getType().getX() == A.getType().getY()) {
            validateL3(Element.F64(this.mRS), 0, 0, Side2, A, B, C);
            this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 120, 0, 0, Side2, Uplo2, 0, C.getType().getY(), C.getType().getX(), 0, alpha, A.getID(this.mRS), B.getID(this.mRS), beta, C.getID(this.mRS), 0, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("Matrix A is not symmetric");
    }

    public void CSYMM(int Side2, int Uplo2, Float2 alpha, Allocation A, Allocation B, Float2 beta, Allocation C) {
        validateSide(Side2);
        validateUplo(Uplo2);
        if (A.getType().getX() == A.getType().getY()) {
            validateL3(Element.F32_2(this.mRS), 0, 0, Side2, A, B, C);
            this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 126, 0, 0, Side2, Uplo2, 0, C.getType().getY(), C.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), beta.x, beta.y, C.getID(this.mRS), 0, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("Matrix A is not symmetric");
    }

    public void ZSYMM(int Side2, int Uplo2, Double2 alpha, Allocation A, Allocation B, Double2 beta, Allocation C) {
        validateSide(Side2);
        validateUplo(Uplo2);
        if (A.getType().getX() == A.getType().getY()) {
            validateL3(Element.F64_2(this.mRS), 0, 0, Side2, A, B, C);
            this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 132, 0, 0, Side2, Uplo2, 0, C.getType().getY(), C.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), beta.x, beta.y, C.getID(this.mRS), 0, 0, 0, 0);
            return;
        }
        throw new RSRuntimeException("Matrix A is not symmetric");
    }

    public void SSYRK(int Uplo2, int Trans, float alpha, Allocation A, float beta, Allocation C) {
        int K;
        validateTranspose(Trans);
        validateUplo(Uplo2);
        validateL3(Element.F32(this.mRS), Trans, 0, 0, A, null, C);
        if (Trans != 111) {
            K = A.getType().getY();
        } else {
            K = A.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 115, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), K, alpha, A.getID(this.mRS), 0, beta, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void DSYRK(int Uplo2, int Trans, double alpha, Allocation A, double beta, Allocation C) {
        int K;
        validateTranspose(Trans);
        validateUplo(Uplo2);
        validateL3(Element.F64(this.mRS), Trans, 0, 0, A, null, C);
        if (Trans != 111) {
            K = A.getType().getY();
        } else {
            K = A.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 121, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), K, alpha, A.getID(this.mRS), 0, beta, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void CSYRK(int Uplo2, int Trans, Float2 alpha, Allocation A, Float2 beta, Allocation C) {
        int K;
        validateTranspose(Trans);
        validateUplo(Uplo2);
        validateL3(Element.F32_2(this.mRS), Trans, 0, 0, A, null, C);
        if (Trans != 111) {
            K = A.getType().getY();
        } else {
            K = A.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 127, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), K, alpha.x, alpha.y, A.getID(this.mRS), 0, beta.x, beta.y, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void ZSYRK(int Uplo2, int Trans, Double2 alpha, Allocation A, Double2 beta, Allocation C) {
        int K;
        validateTranspose(Trans);
        validateUplo(Uplo2);
        validateL3(Element.F64_2(this.mRS), Trans, 0, 0, A, null, C);
        if (Trans != 111) {
            K = A.getType().getY();
        } else {
            K = A.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 133, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), K, alpha.x, alpha.y, A.getID(this.mRS), 0, beta.x, beta.y, C.getID(this.mRS), 0, 0, 0, 0);
    }

    static void validateSYR2K(Element e, int Trans, Allocation A, Allocation B, Allocation C) {
        int Cdim;
        validateTranspose(Trans);
        if (!A.getType().getElement().isCompatible(e) || !B.getType().getElement().isCompatible(e) || !C.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        }
        if (Trans == 112) {
            Cdim = A.getType().getX();
        } else {
            Cdim = A.getType().getY();
        }
        if (C.getType().getX() != Cdim || C.getType().getY() != Cdim) {
            throw new RSRuntimeException("Invalid symmetric matrix in SYR2K");
        } else if (A.getType().getX() != B.getType().getX() || A.getType().getY() != B.getType().getY()) {
            throw new RSRuntimeException("Invalid A and B in SYR2K");
        }
    }

    public void SSYR2K(int Uplo2, int Trans, float alpha, Allocation A, Allocation B, float beta, Allocation C) {
        int K;
        validateUplo(Uplo2);
        validateSYR2K(Element.F32(this.mRS), Trans, A, B, C);
        if (Trans != 111) {
            K = A.getType().getY();
        } else {
            K = A.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 116, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), K, alpha, A.getID(this.mRS), B.getID(this.mRS), beta, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void DSYR2K(int Uplo2, int Trans, double alpha, Allocation A, Allocation B, double beta, Allocation C) {
        int K;
        validateUplo(Uplo2);
        validateSYR2K(Element.F64(this.mRS), Trans, A, B, C);
        if (Trans != 111) {
            K = A.getType().getY();
        } else {
            K = A.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 122, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), K, alpha, A.getID(this.mRS), B.getID(this.mRS), beta, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void CSYR2K(int Uplo2, int Trans, Float2 alpha, Allocation A, Allocation B, Float2 beta, Allocation C) {
        int K;
        validateUplo(Uplo2);
        validateSYR2K(Element.F32_2(this.mRS), Trans, A, B, C);
        if (Trans != 111) {
            K = A.getType().getY();
        } else {
            K = A.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 128, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), K, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), beta.x, beta.y, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void ZSYR2K(int Uplo2, int Trans, Double2 alpha, Allocation A, Allocation B, Double2 beta, Allocation C) {
        int K;
        validateUplo(Uplo2);
        validateSYR2K(Element.F64_2(this.mRS), Trans, A, B, C);
        if (Trans != 111) {
            K = A.getType().getY();
        } else {
            K = A.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 134, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), K, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), beta.x, beta.y, C.getID(this.mRS), 0, 0, 0, 0);
    }

    static void validateTRMM(Element e, int Side2, int TransA, Allocation A, Allocation B) {
        validateSide(Side2);
        validateTranspose(TransA);
        if (!A.getType().getElement().isCompatible(e) || !B.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        }
        int aM = A.getType().getY();
        int aN = A.getType().getX();
        if (aM == aN) {
            int bM = B.getType().getY();
            int bN = B.getType().getX();
            if (Side2 == 141) {
                if (aN != bM) {
                    throw new RSRuntimeException("Called TRMM with invalid matrices");
                }
            } else if (bN != aM) {
                throw new RSRuntimeException("Called TRMM with invalid matrices");
            }
        } else {
            throw new RSRuntimeException("Called TRMM with a non-symmetric matrix A");
        }
    }

    public void STRMM(int Side2, int Uplo2, int TransA, int Diag2, float alpha, Allocation A, Allocation B) {
        validateUplo(Uplo2);
        validateDiag(Diag2);
        validateTRMM(Element.F32(this.mRS), Side2, TransA, A, B);
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 117, TransA, 0, Side2, Uplo2, Diag2, B.getType().getY(), B.getType().getX(), 0, alpha, A.getID(this.mRS), B.getID(this.mRS), 0.0f, 0, 0, 0, 0, 0);
    }

    public void DTRMM(int Side2, int Uplo2, int TransA, int Diag2, double alpha, Allocation A, Allocation B) {
        validateUplo(Uplo2);
        validateDiag(Diag2);
        validateTRMM(Element.F64(this.mRS), Side2, TransA, A, B);
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 123, TransA, 0, Side2, Uplo2, Diag2, B.getType().getY(), B.getType().getX(), 0, alpha, A.getID(this.mRS), B.getID(this.mRS), 0.0d, 0, 0, 0, 0, 0);
    }

    public void CTRMM(int Side2, int Uplo2, int TransA, int Diag2, Float2 alpha, Allocation A, Allocation B) {
        validateUplo(Uplo2);
        validateDiag(Diag2);
        validateTRMM(Element.F32_2(this.mRS), Side2, TransA, A, B);
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 129, TransA, 0, Side2, Uplo2, Diag2, B.getType().getY(), B.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), 0.0f, 0.0f, 0, 0, 0, 0, 0);
    }

    public void ZTRMM(int Side2, int Uplo2, int TransA, int Diag2, Double2 alpha, Allocation A, Allocation B) {
        validateUplo(Uplo2);
        validateDiag(Diag2);
        validateTRMM(Element.F64_2(this.mRS), Side2, TransA, A, B);
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 135, TransA, 0, Side2, Uplo2, Diag2, B.getType().getY(), B.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), 0.0d, 0.0d, 0, 0, 0, 0, 0);
    }

    static void validateTRSM(Element e, int Side2, int TransA, Allocation A, Allocation B) {
        validateSide(Side2);
        validateTranspose(TransA);
        if (!A.getType().getElement().isCompatible(e) || !B.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        }
        int adim = A.getType().getX();
        if (adim == A.getType().getY()) {
            int bM = B.getType().getY();
            int bN = B.getType().getX();
            if (Side2 == 141) {
                if (adim != bM) {
                    throw new RSRuntimeException("Called TRSM with invalid matrix dimensions");
                }
            } else if (adim != bN) {
                throw new RSRuntimeException("Called TRSM with invalid matrix dimensions");
            }
        } else {
            throw new RSRuntimeException("Called TRSM with a non-symmetric matrix A");
        }
    }

    public void STRSM(int Side2, int Uplo2, int TransA, int Diag2, float alpha, Allocation A, Allocation B) {
        validateUplo(Uplo2);
        validateDiag(Diag2);
        validateTRSM(Element.F32(this.mRS), Side2, TransA, A, B);
        this.mRS.nScriptIntrinsicBLAS_Single(getID(this.mRS), 118, TransA, 0, Side2, Uplo2, Diag2, B.getType().getY(), B.getType().getX(), 0, alpha, A.getID(this.mRS), B.getID(this.mRS), 0.0f, 0, 0, 0, 0, 0);
    }

    public void DTRSM(int Side2, int Uplo2, int TransA, int Diag2, double alpha, Allocation A, Allocation B) {
        validateUplo(Uplo2);
        validateDiag(Diag2);
        validateTRSM(Element.F64(this.mRS), Side2, TransA, A, B);
        this.mRS.nScriptIntrinsicBLAS_Double(getID(this.mRS), 124, TransA, 0, Side2, Uplo2, Diag2, B.getType().getY(), B.getType().getX(), 0, alpha, A.getID(this.mRS), B.getID(this.mRS), 0.0d, 0, 0, 0, 0, 0);
    }

    public void CTRSM(int Side2, int Uplo2, int TransA, int Diag2, Float2 alpha, Allocation A, Allocation B) {
        validateUplo(Uplo2);
        validateDiag(Diag2);
        validateTRSM(Element.F32_2(this.mRS), Side2, TransA, A, B);
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 130, TransA, 0, Side2, Uplo2, Diag2, B.getType().getY(), B.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), 0.0f, 0.0f, 0, 0, 0, 0, 0);
    }

    public void ZTRSM(int Side2, int Uplo2, int TransA, int Diag2, Double2 alpha, Allocation A, Allocation B) {
        validateUplo(Uplo2);
        validateDiag(Diag2);
        validateTRSM(Element.F64_2(this.mRS), Side2, TransA, A, B);
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 136, TransA, 0, Side2, Uplo2, Diag2, B.getType().getY(), B.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), 0.0d, 0.0d, 0, 0, 0, 0, 0);
    }

    static void validateHEMM(Element e, int Side2, Allocation A, Allocation B, Allocation C) {
        validateSide(Side2);
        if (!A.getType().getElement().isCompatible(e) || !B.getType().getElement().isCompatible(e) || !C.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        }
        int adim = A.getType().getX();
        if (adim != A.getType().getY()) {
            throw new RSRuntimeException("Called HEMM with non-square A");
        } else if ((Side2 == 141 && adim != B.getType().getY()) || (Side2 == 142 && adim != B.getType().getX())) {
            throw new RSRuntimeException("Called HEMM with invalid B");
        } else if (B.getType().getX() != C.getType().getX() || B.getType().getY() != C.getType().getY()) {
            throw new RSRuntimeException("Called HEMM with mismatched B and C");
        }
    }

    public void CHEMM(int Side2, int Uplo2, Float2 alpha, Allocation A, Allocation B, Float2 beta, Allocation C) {
        validateUplo(Uplo2);
        validateHEMM(Element.F32_2(this.mRS), Side2, A, B, C);
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 137, 0, 0, Side2, Uplo2, 0, C.getType().getY(), C.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), beta.x, beta.y, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void ZHEMM(int Side2, int Uplo2, Double2 alpha, Allocation A, Allocation B, Double2 beta, Allocation C) {
        validateUplo(Uplo2);
        validateHEMM(Element.F64_2(this.mRS), Side2, A, B, C);
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 140, 0, 0, Side2, Uplo2, 0, C.getType().getY(), C.getType().getX(), 0, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), beta.x, beta.y, C.getID(this.mRS), 0, 0, 0, 0);
    }

    static void validateHERK(Element e, int Trans, Allocation A, Allocation C) {
        if (!A.getType().getElement().isCompatible(e) || !C.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        }
        validateConjTranspose(Trans);
        int cdim = C.getType().getX();
        if (cdim != C.getType().getY()) {
            throw new RSRuntimeException("Called HERK with non-square C");
        } else if (Trans == 111) {
            if (cdim != A.getType().getY()) {
                throw new RSRuntimeException("Called HERK with invalid A");
            }
        } else if (cdim != A.getType().getX()) {
            throw new RSRuntimeException("Called HERK with invalid A");
        }
    }

    public void CHERK(int Uplo2, int Trans, float alpha, Allocation A, float beta, Allocation C) {
        int k;
        validateUplo(Uplo2);
        validateHERK(Element.F32_2(this.mRS), Trans, A, C);
        if (Trans == 113) {
            k = A.getType().getY();
        } else {
            k = A.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 138, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), k, alpha, 0.0f, A.getID(this.mRS), 0, beta, 0.0f, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void ZHERK(int Uplo2, int Trans, double alpha, Allocation A, double beta, Allocation C) {
        int k;
        validateUplo(Uplo2);
        validateHERK(Element.F64_2(this.mRS), Trans, A, C);
        if (Trans == 113) {
            k = A.getType().getY();
        } else {
            k = A.getType().getX();
        }
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 141, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), k, alpha, 0.0d, A.getID(this.mRS), 0, beta, 0.0d, C.getID(this.mRS), 0, 0, 0, 0);
    }

    static void validateHER2K(Element e, int Trans, Allocation A, Allocation B, Allocation C) {
        if (!A.getType().getElement().isCompatible(e) || !B.getType().getElement().isCompatible(e) || !C.getType().getElement().isCompatible(e)) {
            throw new RSRuntimeException("Called BLAS with wrong Element type");
        }
        validateConjTranspose(Trans);
        int cdim = C.getType().getX();
        if (cdim == C.getType().getY()) {
            if (Trans == 111) {
                if (A.getType().getY() != cdim) {
                    throw new RSRuntimeException("Called HER2K with invalid matrices");
                }
            } else if (A.getType().getX() != cdim) {
                throw new RSRuntimeException("Called HER2K with invalid matrices");
            }
            if (A.getType().getX() != B.getType().getX() || A.getType().getY() != B.getType().getY()) {
                throw new RSRuntimeException("Called HER2K with invalid A and B matrices");
            }
            return;
        }
        throw new RSRuntimeException("Called HER2K with non-square C");
    }

    public void CHER2K(int Uplo2, int Trans, Float2 alpha, Allocation A, Allocation B, float beta, Allocation C) {
        int k;
        validateUplo(Uplo2);
        validateHER2K(Element.F32_2(this.mRS), Trans, A, B, C);
        if (Trans == 111) {
            k = A.getType().getX();
        } else {
            k = A.getType().getY();
        }
        this.mRS.nScriptIntrinsicBLAS_Complex(getID(this.mRS), 139, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), k, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), beta, 0.0f, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void ZHER2K(int Uplo2, int Trans, Double2 alpha, Allocation A, Allocation B, double beta, Allocation C) {
        int k;
        validateUplo(Uplo2);
        validateHER2K(Element.F64_2(this.mRS), Trans, A, B, C);
        if (Trans == 111) {
            k = A.getType().getX();
        } else {
            k = A.getType().getY();
        }
        this.mRS.nScriptIntrinsicBLAS_Z(getID(this.mRS), 142, Trans, 0, 0, Uplo2, 0, 0, C.getType().getX(), k, alpha.x, alpha.y, A.getID(this.mRS), B.getID(this.mRS), beta, 0.0d, C.getID(this.mRS), 0, 0, 0, 0);
    }

    public void BNNM(Allocation A, int a_offset, Allocation B, int b_offset, Allocation C, int c_offset, int c_mult) {
        validateL3(Element.U8(this.mRS), 111, 112, 0, A, B, C);
        if (a_offset < 0 || a_offset > 255) {
            throw new RSRuntimeException("Invalid a_offset passed to BNNM");
        } else if (b_offset < 0 || b_offset > 255) {
            throw new RSRuntimeException("Invalid b_offset passed to BNNM");
        } else {
            this.mRS.nScriptIntrinsicBLAS_BNNM(getID(this.mRS), A.getType().getY(), B.getType().getY(), A.getType().getX(), A.getID(this.mRS), a_offset, B.getID(this.mRS), b_offset, C.getID(this.mRS), c_offset, c_mult);
        }
    }
}
