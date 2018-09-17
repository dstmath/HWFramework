package android.hardware;

import android.hardware.camera2.params.TonemapCurve;
import java.util.GregorianCalendar;

public class GeomagneticField {
    static final /* synthetic */ boolean -assertionsDisabled = (GeomagneticField.class.desiredAssertionStatus() ^ 1);
    private static final long BASE_TIME = new GregorianCalendar(2010, 1, 1).getTimeInMillis();
    private static final float[][] DELTA_G;
    private static final float[][] DELTA_H;
    private static final float EARTH_REFERENCE_RADIUS_KM = 6371.2f;
    private static final float EARTH_SEMI_MAJOR_AXIS_KM = 6378.137f;
    private static final float EARTH_SEMI_MINOR_AXIS_KM = 6356.7524f;
    private static final float[][] G_COEFF;
    private static final float[][] H_COEFF;
    private static final float[][] SCHMIDT_QUASI_NORM_FACTORS = computeSchmidtQuasiNormFactors(G_COEFF.length);
    private float mGcLatitudeRad;
    private float mGcLongitudeRad;
    private float mGcRadiusKm;
    private float mX;
    private float mY;
    private float mZ;

    private static class LegendreTable {
        static final /* synthetic */ boolean -assertionsDisabled = (LegendreTable.class.desiredAssertionStatus() ^ 1);
        public final float[][] mP;
        public final float[][] mPDeriv;

        public LegendreTable(int maxN, float thetaRad) {
            float cos = (float) Math.cos((double) thetaRad);
            float sin = (float) Math.sin((double) thetaRad);
            this.mP = new float[(maxN + 1)][];
            this.mPDeriv = new float[(maxN + 1)][];
            this.mP[0] = new float[]{1.0f};
            this.mPDeriv[0] = new float[]{TonemapCurve.LEVEL_BLACK};
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
                    } else if (-assertionsDisabled || (n > 1 && m < n - 1)) {
                        float k = ((float) (((n - 1) * (n - 1)) - (m * m))) / ((float) (((n * 2) - 1) * ((n * 2) - 3)));
                        this.mP[n][m] = (this.mP[n - 1][m] * cos) - (this.mP[n - 2][m] * k);
                        this.mPDeriv[n][m] = (((-sin) * this.mP[n - 1][m]) + (this.mPDeriv[n - 1][m] * cos)) - (this.mPDeriv[n - 2][m] * k);
                    } else {
                        throw new AssertionError();
                    }
                    m++;
                }
                n++;
            }
        }
    }

    static {
        r0 = new float[13][];
        r0[0] = new float[]{TonemapCurve.LEVEL_BLACK};
        r0[1] = new float[]{-29496.6f, -1586.3f};
        r0[2] = new float[]{-2396.6f, 3026.1f, 1668.6f};
        r0[3] = new float[]{1340.1f, -2326.2f, 1231.9f, 634.0f};
        r0[4] = new float[]{912.6f, 808.9f, 166.7f, -357.1f, 89.4f};
        r0[5] = new float[]{-230.9f, 357.2f, 200.3f, -141.1f, -163.0f, -7.8f};
        r0[6] = new float[]{72.8f, 68.6f, 76.0f, -141.4f, -22.8f, 13.2f, -77.9f};
        r0[7] = new float[]{80.5f, -75.1f, -4.7f, 45.3f, 13.9f, 10.4f, 1.7f, 4.9f};
        r0[8] = new float[]{24.4f, 8.1f, -14.5f, -5.6f, -19.3f, 11.5f, 10.9f, -14.1f, -3.7f};
        r0[9] = new float[]{5.4f, 9.4f, 3.4f, -5.2f, 3.1f, -12.4f, -0.7f, 8.4f, -8.5f, -10.1f};
        r0[10] = new float[]{-2.0f, -6.3f, 0.9f, -1.1f, -0.2f, 2.5f, -0.3f, 2.2f, 3.1f, -1.0f, -2.8f};
        r0[11] = new float[]{3.0f, -1.5f, -2.1f, 1.7f, -0.5f, 0.5f, -0.8f, 0.4f, 1.8f, 0.1f, 0.7f, 3.8f};
        r0[12] = new float[]{-2.2f, -0.2f, 0.3f, 1.0f, -0.6f, 0.9f, -0.1f, 0.5f, -0.4f, -0.4f, 0.2f, -0.8f, TonemapCurve.LEVEL_BLACK};
        G_COEFF = r0;
        r0 = new float[13][];
        r0[0] = new float[]{TonemapCurve.LEVEL_BLACK};
        r0[1] = new float[]{TonemapCurve.LEVEL_BLACK, 4944.4f};
        r0[2] = new float[]{TonemapCurve.LEVEL_BLACK, -2707.7f, -576.1f};
        r0[3] = new float[]{TonemapCurve.LEVEL_BLACK, -160.2f, 251.9f, -536.6f};
        r0[4] = new float[]{TonemapCurve.LEVEL_BLACK, 286.4f, -211.2f, 164.3f, -309.1f};
        r0[5] = new float[]{TonemapCurve.LEVEL_BLACK, 44.6f, 188.9f, -118.2f, TonemapCurve.LEVEL_BLACK, 100.9f};
        r0[6] = new float[]{TonemapCurve.LEVEL_BLACK, -20.8f, 44.1f, 61.5f, -66.3f, 3.1f, 55.0f};
        r0[7] = new float[]{TonemapCurve.LEVEL_BLACK, -57.9f, -21.1f, 6.5f, 24.9f, 7.0f, -27.7f, -3.3f};
        r0[8] = new float[]{TonemapCurve.LEVEL_BLACK, 11.0f, -20.0f, 11.9f, -17.4f, 16.7f, 7.0f, -10.8f, 1.7f};
        r0[9] = new float[]{TonemapCurve.LEVEL_BLACK, -20.5f, 11.5f, 12.8f, -7.2f, -7.4f, 8.0f, 2.1f, -6.1f, 7.0f};
        r0[10] = new float[]{TonemapCurve.LEVEL_BLACK, 2.8f, -0.1f, 4.7f, 4.4f, -7.2f, -1.0f, -3.9f, -2.0f, -2.0f, -8.3f};
        r0[11] = new float[]{TonemapCurve.LEVEL_BLACK, 0.2f, 1.7f, -0.6f, -1.8f, 0.9f, -0.4f, -2.5f, -1.3f, -2.1f, -1.9f, -1.8f};
        r0[12] = new float[]{TonemapCurve.LEVEL_BLACK, -0.9f, 0.3f, 2.1f, -2.5f, 0.5f, 0.6f, TonemapCurve.LEVEL_BLACK, 0.1f, 0.3f, -0.9f, -0.2f, 0.9f};
        H_COEFF = r0;
        r0 = new float[13][];
        r0[0] = new float[]{TonemapCurve.LEVEL_BLACK};
        r0[1] = new float[]{11.6f, 16.5f};
        r0[2] = new float[]{-12.1f, -4.4f, 1.9f};
        r0[3] = new float[]{0.4f, -4.1f, -2.9f, -7.7f};
        r0[4] = new float[]{-1.8f, 2.3f, -8.7f, 4.6f, -2.1f};
        r0[5] = new float[]{-1.0f, 0.6f, -1.8f, -1.0f, 0.9f, 1.0f};
        r0[6] = new float[]{-0.2f, -0.2f, -0.1f, 2.0f, -1.7f, -0.3f, 1.7f};
        r0[7] = new float[]{0.1f, -0.1f, -0.6f, 1.3f, 0.4f, 0.3f, -0.7f, 0.6f};
        r0[8] = new float[]{-0.1f, 0.1f, -0.6f, 0.2f, -0.2f, 0.3f, 0.3f, -0.6f, 0.2f};
        r0[9] = new float[]{TonemapCurve.LEVEL_BLACK, -0.1f, TonemapCurve.LEVEL_BLACK, 0.3f, -0.4f, -0.3f, 0.1f, -0.1f, -0.4f, -0.2f};
        r0[10] = new float[]{TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, -0.1f, 0.2f, TonemapCurve.LEVEL_BLACK, -0.1f, -0.2f, TonemapCurve.LEVEL_BLACK, -0.1f, -0.2f, -0.2f};
        r0[11] = new float[]{TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 0.1f, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, -0.1f, TonemapCurve.LEVEL_BLACK};
        r0[12] = new float[]{TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 0.1f, 0.1f, -0.1f, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, -0.1f, 0.1f};
        DELTA_G = r0;
        r0 = new float[13][];
        r0[0] = new float[]{TonemapCurve.LEVEL_BLACK};
        r0[1] = new float[]{TonemapCurve.LEVEL_BLACK, -25.9f};
        r0[2] = new float[]{TonemapCurve.LEVEL_BLACK, -22.5f, -11.8f};
        r0[3] = new float[]{TonemapCurve.LEVEL_BLACK, 7.3f, -3.9f, -2.6f};
        r0[4] = new float[]{TonemapCurve.LEVEL_BLACK, 1.1f, 2.7f, 3.9f, -0.8f};
        r0[5] = new float[]{TonemapCurve.LEVEL_BLACK, 0.4f, 1.8f, 1.2f, 4.0f, -0.6f};
        r0[6] = new float[]{TonemapCurve.LEVEL_BLACK, -0.2f, -2.1f, -0.4f, -0.6f, 0.5f, 0.9f};
        r0[7] = new float[]{TonemapCurve.LEVEL_BLACK, 0.7f, 0.3f, -0.1f, -0.1f, -0.8f, -0.3f, 0.3f};
        r0[8] = new float[]{TonemapCurve.LEVEL_BLACK, -0.1f, 0.2f, 0.4f, 0.4f, 0.1f, -0.1f, 0.4f, 0.3f};
        r0[9] = new float[]{TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, -0.2f, TonemapCurve.LEVEL_BLACK, -0.1f, 0.1f, TonemapCurve.LEVEL_BLACK, -0.2f, 0.3f, 0.2f};
        r0[10] = new float[]{TonemapCurve.LEVEL_BLACK, 0.1f, -0.1f, TonemapCurve.LEVEL_BLACK, -0.1f, -0.1f, TonemapCurve.LEVEL_BLACK, -0.1f, -0.2f, TonemapCurve.LEVEL_BLACK, -0.1f};
        r0[11] = new float[]{TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 0.1f, TonemapCurve.LEVEL_BLACK, 0.1f, TonemapCurve.LEVEL_BLACK, 0.1f, TonemapCurve.LEVEL_BLACK, -0.1f, -0.1f, TonemapCurve.LEVEL_BLACK, -0.1f};
        r0[12] = new float[]{TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 0.1f, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK};
        DELTA_H = r0;
    }

    public GeomagneticField(float gdLatitudeDeg, float gdLongitudeDeg, float altitudeMeters, long timeMillis) {
        int MAX_N = G_COEFF.length;
        gdLatitudeDeg = Math.min(89.99999f, Math.max(-89.99999f, gdLatitudeDeg));
        computeGeocentricCoordinates(gdLatitudeDeg, gdLongitudeDeg, altitudeMeters);
        if (-assertionsDisabled || G_COEFF.length == H_COEFF.length) {
            int m;
            LegendreTable legendre = new LegendreTable(MAX_N - 1, (float) (1.5707963267948966d - ((double) this.mGcLatitudeRad)));
            float[] relativeRadiusPower = new float[(MAX_N + 2)];
            relativeRadiusPower[0] = 1.0f;
            relativeRadiusPower[1] = EARTH_REFERENCE_RADIUS_KM / this.mGcRadiusKm;
            for (int i = 2; i < relativeRadiusPower.length; i++) {
                relativeRadiusPower[i] = relativeRadiusPower[i - 1] * relativeRadiusPower[1];
            }
            float[] sinMLon = new float[MAX_N];
            float[] cosMLon = new float[MAX_N];
            sinMLon[0] = TonemapCurve.LEVEL_BLACK;
            cosMLon[0] = 1.0f;
            sinMLon[1] = (float) Math.sin((double) this.mGcLongitudeRad);
            cosMLon[1] = (float) Math.cos((double) this.mGcLongitudeRad);
            for (m = 2; m < MAX_N; m++) {
                int x = m >> 1;
                sinMLon[m] = (sinMLon[m - x] * cosMLon[x]) + (cosMLon[m - x] * sinMLon[x]);
                cosMLon[m] = (cosMLon[m - x] * cosMLon[x]) - (sinMLon[m - x] * sinMLon[x]);
            }
            float inverseCosLatitude = 1.0f / ((float) Math.cos((double) this.mGcLatitudeRad));
            float yearsSinceBase = ((float) (timeMillis - BASE_TIME)) / 3.1536001E10f;
            float gcX = TonemapCurve.LEVEL_BLACK;
            float gcY = TonemapCurve.LEVEL_BLACK;
            float gcZ = TonemapCurve.LEVEL_BLACK;
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
            return;
        }
        throw new AssertionError();
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
        schmidtQuasiNorm[0] = new float[]{1.0f};
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
