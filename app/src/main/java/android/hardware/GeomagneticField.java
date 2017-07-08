package android.hardware;

import android.speech.tts.TextToSpeech.Engine;

public class GeomagneticField {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final long BASE_TIME = 0;
    private static final float[][] DELTA_G = null;
    private static final float[][] DELTA_H = null;
    private static final float EARTH_REFERENCE_RADIUS_KM = 6371.2f;
    private static final float EARTH_SEMI_MAJOR_AXIS_KM = 6378.137f;
    private static final float EARTH_SEMI_MINOR_AXIS_KM = 6356.7524f;
    private static final float[][] G_COEFF = null;
    private static final float[][] H_COEFF = null;
    private static final float[][] SCHMIDT_QUASI_NORM_FACTORS = null;
    private float mGcLatitudeRad;
    private float mGcLongitudeRad;
    private float mGcRadiusKm;
    private float mX;
    private float mY;
    private float mZ;

    private static class LegendreTable {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        public final float[][] mP;
        public final float[][] mPDeriv;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.GeomagneticField.LegendreTable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.GeomagneticField.LegendreTable.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.GeomagneticField.LegendreTable.<clinit>():void");
        }

        public LegendreTable(int maxN, float thetaRad) {
            float cos = (float) Math.cos((double) thetaRad);
            float sin = (float) Math.sin((double) thetaRad);
            this.mP = new float[(maxN + 1)][];
            this.mPDeriv = new float[(maxN + 1)][];
            this.mP[0] = new float[]{Engine.DEFAULT_VOLUME};
            this.mPDeriv[0] = new float[]{0.0f};
            int n = 1;
            while (n <= maxN) {
                this.mP[n] = new float[(n + 1)];
                this.mPDeriv[n] = new float[(n + 1)];
                int m = 0;
                while (m <= n) {
                    if (n == m) {
                        this.mP[n][m] = this.mP[n - 1][m - 1] * sin;
                        this.mPDeriv[n][m] = (this.mP[n - 1][m - 1] * cos) + (this.mPDeriv[n - 1][m - 1] * sin);
                    } else if (n == 1 || m == n - 1) {
                        this.mP[n][m] = this.mP[n - 1][m] * cos;
                        this.mPDeriv[n][m] = ((-sin) * this.mP[n - 1][m]) + (this.mPDeriv[n - 1][m] * cos);
                    } else {
                        if (!-assertionsDisabled) {
                            int i = (n <= 1 || m >= n - 1) ? 0 : 1;
                            if (i == 0) {
                                throw new AssertionError();
                            }
                        }
                        float k = ((float) (((n - 1) * (n - 1)) - (m * m))) / ((float) (((n * 2) - 1) * ((n * 2) - 3)));
                        this.mP[n][m] = (this.mP[n - 1][m] * cos) - (this.mP[n - 2][m] * k);
                        this.mPDeriv[n][m] = (((-sin) * this.mP[n - 1][m]) + (this.mPDeriv[n - 1][m] * cos)) - (this.mPDeriv[n - 2][m] * k);
                    }
                    m++;
                }
                n++;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.GeomagneticField.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.GeomagneticField.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.GeomagneticField.<clinit>():void");
    }

    public GeomagneticField(float gdLatitudeDeg, float gdLongitudeDeg, float altitudeMeters, long timeMillis) {
        int m;
        int MAX_N = G_COEFF.length;
        gdLatitudeDeg = Math.min(89.99999f, Math.max(-89.99999f, gdLatitudeDeg));
        computeGeocentricCoordinates(gdLatitudeDeg, gdLongitudeDeg, altitudeMeters);
        if (!-assertionsDisabled) {
            if ((G_COEFF.length == H_COEFF.length ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        LegendreTable legendre = new LegendreTable(MAX_N - 1, (float) (1.5707963267948966d - ((double) this.mGcLatitudeRad)));
        float[] relativeRadiusPower = new float[(MAX_N + 2)];
        relativeRadiusPower[0] = Engine.DEFAULT_VOLUME;
        relativeRadiusPower[1] = EARTH_REFERENCE_RADIUS_KM / this.mGcRadiusKm;
        int i = 2;
        while (true) {
            int length = relativeRadiusPower.length;
            if (i >= r0) {
                break;
            }
            relativeRadiusPower[i] = relativeRadiusPower[i - 1] * relativeRadiusPower[1];
            i++;
        }
        float[] sinMLon = new float[MAX_N];
        float[] cosMLon = new float[MAX_N];
        sinMLon[0] = 0.0f;
        cosMLon[0] = Engine.DEFAULT_VOLUME;
        sinMLon[1] = (float) Math.sin((double) this.mGcLongitudeRad);
        cosMLon[1] = (float) Math.cos((double) this.mGcLongitudeRad);
        for (m = 2; m < MAX_N; m++) {
            int x = m >> 1;
            sinMLon[m] = (sinMLon[m - x] * cosMLon[x]) + (cosMLon[m - x] * sinMLon[x]);
            cosMLon[m] = (cosMLon[m - x] * cosMLon[x]) - (sinMLon[m - x] * sinMLon[x]);
        }
        float inverseCosLatitude = Engine.DEFAULT_VOLUME / ((float) Math.cos((double) this.mGcLatitudeRad));
        float yearsSinceBase = ((float) (timeMillis - BASE_TIME)) / 3.1536001E10f;
        float gcX = 0.0f;
        float gcY = 0.0f;
        float gcZ = 0.0f;
        for (int n = 1; n < MAX_N; n++) {
            for (m = 0; m <= n; m++) {
                float g = G_COEFF[n][m] + (DELTA_G[n][m] * yearsSinceBase);
                float h = H_COEFF[n][m] + (DELTA_H[n][m] * yearsSinceBase);
                gcX += ((relativeRadiusPower[n + 2] * ((cosMLon[m] * g) + (sinMLon[m] * h))) * legendre.mPDeriv[n][m]) * SCHMIDT_QUASI_NORM_FACTORS[n][m];
                gcY += ((((relativeRadiusPower[n + 2] * ((float) m)) * ((sinMLon[m] * g) - (cosMLon[m] * h))) * legendre.mP[n][m]) * SCHMIDT_QUASI_NORM_FACTORS[n][m]) * inverseCosLatitude;
                gcZ -= (((((float) (n + 1)) * relativeRadiusPower[n + 2]) * ((cosMLon[m] * g) + (sinMLon[m] * h))) * legendre.mP[n][m]) * SCHMIDT_QUASI_NORM_FACTORS[n][m];
            }
        }
        double latDiffRad = Math.toRadians((double) gdLatitudeDeg) - ((double) this.mGcLatitudeRad);
        this.mX = (float) ((((double) gcX) * Math.cos(latDiffRad)) + (((double) gcZ) * Math.sin(latDiffRad)));
        this.mY = gcY;
        this.mZ = (float) ((((double) (-gcX)) * Math.sin(latDiffRad)) + (((double) gcZ) * Math.cos(latDiffRad)));
    }

    public float getX() {
        return this.mX;
    }

    public float getY() {
        return this.mY;
    }

    public float getZ() {
        return this.mZ;
    }

    public float getDeclination() {
        return (float) Math.toDegrees(Math.atan2((double) this.mY, (double) this.mX));
    }

    public float getInclination() {
        return (float) Math.toDegrees(Math.atan2((double) this.mZ, (double) getHorizontalStrength()));
    }

    public float getHorizontalStrength() {
        return (float) Math.hypot((double) this.mX, (double) this.mY);
    }

    public float getFieldStrength() {
        return (float) Math.sqrt((double) (((this.mX * this.mX) + (this.mY * this.mY)) + (this.mZ * this.mZ)));
    }

    private void computeGeocentricCoordinates(float gdLatitudeDeg, float gdLongitudeDeg, float altitudeMeters) {
        float altitudeKm = altitudeMeters / 1000.0f;
        double gdLatRad = Math.toRadians((double) gdLatitudeDeg);
        float clat = (float) Math.cos(gdLatRad);
        float slat = (float) Math.sin(gdLatRad);
        float latRad = (float) Math.sqrt((double) (((4.0680636E7f * clat) * clat) + ((4.04083E7f * slat) * slat)));
        this.mGcLatitudeRad = (float) Math.atan((double) ((((latRad * altitudeKm) + 4.04083E7f) * (slat / clat)) / ((latRad * altitudeKm) + 4.0680636E7f)));
        this.mGcLongitudeRad = (float) Math.toRadians((double) gdLongitudeDeg);
        this.mGcRadiusKm = (float) Math.sqrt((double) (((altitudeKm * altitudeKm) + ((2.0f * altitudeKm) * ((float) Math.sqrt((double) (((4.0680636E7f * clat) * clat) + ((4.04083E7f * slat) * slat)))))) + ((((1.65491412E15f * clat) * clat) + ((1.63283074E15f * slat) * slat)) / (((4.0680636E7f * clat) * clat) + ((4.04083E7f * slat) * slat)))));
    }

    private static float[][] computeSchmidtQuasiNormFactors(int maxN) {
        float[][] schmidtQuasiNorm = new float[(maxN + 1)][];
        schmidtQuasiNorm[0] = new float[]{Engine.DEFAULT_VOLUME};
        for (int n = 1; n <= maxN; n++) {
            schmidtQuasiNorm[n] = new float[(n + 1)];
            schmidtQuasiNorm[n][0] = (schmidtQuasiNorm[n - 1][0] * ((float) ((n * 2) - 1))) / ((float) n);
            for (int m = 1; m <= n; m++) {
                int i;
                float[] fArr = schmidtQuasiNorm[n];
                float f = schmidtQuasiNorm[n][m - 1];
                int i2 = (n - m) + 1;
                if (m == 1) {
                    i = 2;
                } else {
                    i = 1;
                }
                fArr[m] = ((float) Math.sqrt((double) (((float) (i * i2)) / ((float) (n + m))))) * f;
            }
        }
        return schmidtQuasiNorm;
    }
}
