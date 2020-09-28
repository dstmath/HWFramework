package android.hardware;

import java.util.GregorianCalendar;

public class GeomagneticField {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final long BASE_TIME = new GregorianCalendar(2015, 1, 1).getTimeInMillis();
    private static final float[][] DELTA_G = {new float[]{0.0f}, new float[]{10.7f, 17.9f}, new float[]{-8.6f, -3.3f, 2.4f}, new float[]{3.1f, -6.2f, -0.4f, -10.4f}, new float[]{-0.4f, 0.8f, -9.2f, 4.0f, -4.2f}, new float[]{-0.2f, 0.1f, -1.4f, 0.0f, 1.3f, 3.8f}, new float[]{-0.5f, -0.2f, -0.6f, 2.4f, -1.1f, 0.3f, 1.5f}, new float[]{0.2f, -0.2f, -0.4f, 1.3f, 0.2f, -0.4f, -0.9f, 0.3f}, new float[]{0.0f, 0.1f, -0.5f, 0.5f, -0.2f, 0.4f, 0.2f, -0.4f, 0.3f}, new float[]{0.0f, -0.1f, -0.1f, 0.4f, -0.5f, -0.2f, 0.1f, 0.0f, -0.2f, -0.1f}, new float[]{0.0f, 0.0f, -0.1f, 0.3f, -0.1f, -0.1f, -0.1f, 0.0f, -0.2f, -0.1f, -0.2f}, new float[]{0.0f, 0.0f, -0.1f, 0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1f, -0.1f}, new float[]{0.1f, 0.0f, 0.0f, 0.1f, -0.1f, 0.0f, 0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}};
    private static final float[][] DELTA_H = {new float[]{0.0f}, new float[]{0.0f, -26.8f}, new float[]{0.0f, -27.1f, -13.3f}, new float[]{0.0f, 8.4f, -0.4f, 2.3f}, new float[]{0.0f, -0.6f, 5.3f, 3.0f, -5.3f}, new float[]{0.0f, 0.4f, 1.6f, -1.1f, 3.3f, 0.1f}, new float[]{0.0f, 0.0f, -2.2f, -0.7f, 0.1f, 1.0f, 1.3f}, new float[]{0.0f, 0.7f, 0.5f, -0.2f, -0.1f, -0.7f, 0.1f, 0.1f}, new float[]{0.0f, -0.3f, 0.3f, 0.3f, 0.6f, -0.1f, -0.2f, 0.3f, 0.0f}, new float[]{0.0f, -0.2f, -0.1f, -0.2f, 0.1f, 0.1f, 0.0f, -0.2f, 0.4f, 0.3f}, new float[]{0.0f, 0.1f, -0.1f, 0.0f, 0.0f, -0.2f, 0.1f, -0.1f, -0.2f, 0.1f, -0.1f}, new float[]{0.0f, 0.0f, 0.1f, 0.0f, 0.1f, 0.0f, 0.0f, 0.1f, 0.0f, -0.1f, 0.0f, -0.1f}, new float[]{0.0f, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}};
    private static final float EARTH_REFERENCE_RADIUS_KM = 6371.2f;
    private static final float EARTH_SEMI_MAJOR_AXIS_KM = 6378.137f;
    private static final float EARTH_SEMI_MINOR_AXIS_KM = 6356.7524f;
    private static final float[][] G_COEFF = {new float[]{0.0f}, new float[]{-29438.5f, -1501.1f}, new float[]{-2445.3f, 3012.5f, 1676.6f}, new float[]{1351.1f, -2352.3f, 1225.6f, 581.9f}, new float[]{907.2f, 813.7f, 120.3f, -335.0f, 70.3f}, new float[]{-232.6f, 360.1f, 192.4f, -141.0f, -157.4f, 4.3f}, new float[]{69.5f, 67.4f, 72.8f, -129.8f, -29.0f, 13.2f, -70.9f}, new float[]{81.6f, -76.1f, -6.8f, 51.9f, 15.0f, 9.3f, -2.8f, 6.7f}, new float[]{24.0f, 8.6f, -16.9f, -3.2f, -20.6f, 13.3f, 11.7f, -16.0f, -2.0f}, new float[]{5.4f, 8.8f, 3.1f, -3.1f, 0.6f, -13.3f, -0.1f, 8.7f, -9.1f, -10.5f}, new float[]{-1.9f, -6.5f, 0.2f, 0.6f, -0.6f, 1.7f, -0.7f, 2.1f, 2.3f, -1.8f, -3.6f}, new float[]{3.1f, -1.5f, -2.3f, 2.1f, -0.9f, 0.6f, -0.7f, 0.2f, 1.7f, -0.2f, 0.4f, 3.5f}, new float[]{-2.0f, -0.3f, 0.4f, 1.3f, -0.9f, 0.9f, 0.1f, 0.5f, -0.4f, -0.4f, 0.2f, -0.9f, 0.0f}};
    private static final float[][] H_COEFF = {new float[]{0.0f}, new float[]{0.0f, 4796.2f}, new float[]{0.0f, -2845.6f, -642.0f}, new float[]{0.0f, -115.3f, 245.0f, -538.3f}, new float[]{0.0f, 283.4f, -188.6f, 180.9f, -329.5f}, new float[]{0.0f, 47.4f, 196.9f, -119.4f, 16.1f, 100.1f}, new float[]{0.0f, -20.7f, 33.2f, 58.8f, -66.5f, 7.3f, 62.5f}, new float[]{0.0f, -54.1f, -19.4f, 5.6f, 24.4f, 3.3f, -27.5f, -2.3f}, new float[]{0.0f, 10.2f, -18.1f, 13.2f, -14.6f, 16.2f, 5.7f, -9.1f, 2.2f}, new float[]{0.0f, -21.6f, 10.8f, 11.7f, -6.8f, -6.9f, 7.8f, 1.0f, -3.9f, 8.5f}, new float[]{0.0f, 3.3f, -0.3f, 4.6f, 4.4f, -7.9f, -0.6f, -4.1f, -2.8f, -1.1f, -8.7f}, new float[]{0.0f, -0.1f, 2.1f, -0.7f, -1.1f, 0.7f, -0.2f, -2.1f, -1.5f, -2.5f, -2.0f, -2.3f}, new float[]{0.0f, -1.0f, 0.5f, 1.8f, -2.2f, 0.3f, 0.7f, -0.1f, 0.3f, 0.2f, -0.9f, -0.2f, 0.7f}};
    private static final float[][] SCHMIDT_QUASI_NORM_FACTORS = computeSchmidtQuasiNormFactors(G_COEFF.length);
    private float mGcLatitudeRad;
    private float mGcLongitudeRad;
    private float mGcRadiusKm;
    private float mX;
    private float mY;
    private float mZ;

    /* JADX INFO: Multiple debug info for r9v6 float[]: [D('i' int), D('sinMLon' float[])] */
    public GeomagneticField(float gdLatitudeDeg, float gdLongitudeDeg, float altitudeMeters, long timeMillis) {
        int MAX_N = G_COEFF.length;
        float gdLatitudeDeg2 = Math.min(89.99999f, Math.max(-89.99999f, gdLatitudeDeg));
        computeGeocentricCoordinates(gdLatitudeDeg2, gdLongitudeDeg, altitudeMeters);
        LegendreTable legendre = new LegendreTable(MAX_N - 1, (float) (1.5707963267948966d - ((double) this.mGcLatitudeRad)));
        float[] relativeRadiusPower = new float[(MAX_N + 2)];
        relativeRadiusPower[0] = 1.0f;
        relativeRadiusPower[1] = EARTH_REFERENCE_RADIUS_KM / this.mGcRadiusKm;
        for (int i = 2; i < relativeRadiusPower.length; i++) {
            relativeRadiusPower[i] = relativeRadiusPower[i - 1] * relativeRadiusPower[1];
        }
        float[] sinMLon = new float[MAX_N];
        float[] cosMLon = new float[MAX_N];
        sinMLon[0] = 0.0f;
        cosMLon[0] = 1.0f;
        sinMLon[1] = (float) Math.sin((double) this.mGcLongitudeRad);
        cosMLon[1] = (float) Math.cos((double) this.mGcLongitudeRad);
        for (int m = 2; m < MAX_N; m++) {
            int x = m >> 1;
            sinMLon[m] = (sinMLon[m - x] * cosMLon[x]) + (cosMLon[m - x] * sinMLon[x]);
            cosMLon[m] = (cosMLon[m - x] * cosMLon[x]) - (sinMLon[m - x] * sinMLon[x]);
        }
        float inverseCosLatitude = 1.0f / ((float) Math.cos((double) this.mGcLatitudeRad));
        float yearsSinceBase = ((float) (timeMillis - BASE_TIME)) / 3.1536001E10f;
        float gcX = 0.0f;
        float gcY = 0.0f;
        float gcZ = 0.0f;
        for (int n = 1; n < MAX_N; n++) {
            int m2 = 0;
            while (m2 <= n) {
                float g = G_COEFF[n][m2] + (DELTA_G[n][m2] * yearsSinceBase);
                float h = H_COEFF[n][m2] + (DELTA_H[n][m2] * yearsSinceBase);
                gcX += relativeRadiusPower[n + 2] * ((cosMLon[m2] * g) + (sinMLon[m2] * h)) * legendre.mPDeriv[n][m2] * SCHMIDT_QUASI_NORM_FACTORS[n][m2];
                gcY += relativeRadiusPower[n + 2] * ((float) m2) * ((sinMLon[m2] * g) - (cosMLon[m2] * h)) * legendre.mP[n][m2] * SCHMIDT_QUASI_NORM_FACTORS[n][m2] * inverseCosLatitude;
                gcZ -= (((((float) (n + 1)) * relativeRadiusPower[n + 2]) * ((cosMLon[m2] * g) + (sinMLon[m2] * h))) * legendre.mP[n][m2]) * SCHMIDT_QUASI_NORM_FACTORS[n][m2];
                m2++;
                MAX_N = MAX_N;
            }
        }
        double latDiffRad = Math.toRadians((double) gdLatitudeDeg2) - ((double) this.mGcLatitudeRad);
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
        float f = this.mX;
        float f2 = this.mY;
        float f3 = (f * f) + (f2 * f2);
        float f4 = this.mZ;
        return (float) Math.sqrt((double) (f3 + (f4 * f4)));
    }

    private void computeGeocentricCoordinates(float gdLatitudeDeg, float gdLongitudeDeg, float altitudeMeters) {
        float altitudeKm = altitudeMeters / 1000.0f;
        double gdLatRad = Math.toRadians((double) gdLatitudeDeg);
        float clat = (float) Math.cos(gdLatRad);
        float slat = (float) Math.sin(gdLatRad);
        float latRad = (float) Math.sqrt((double) ((4.0680636E7f * clat * clat) + (4.04083E7f * slat * slat)));
        this.mGcLatitudeRad = (float) Math.atan((double) ((((latRad * altitudeKm) + 4.04083E7f) * (slat / clat)) / ((latRad * altitudeKm) + 4.0680636E7f)));
        this.mGcLongitudeRad = (float) Math.toRadians((double) gdLongitudeDeg);
        this.mGcRadiusKm = (float) Math.sqrt((double) ((altitudeKm * altitudeKm) + (2.0f * altitudeKm * ((float) Math.sqrt((double) ((4.0680636E7f * clat * clat) + (4.04083E7f * slat * slat))))) + (((((4.0680636E7f * 4.0680636E7f) * clat) * clat) + (((4.04083E7f * 4.04083E7f) * slat) * slat)) / (((4.0680636E7f * clat) * clat) + ((4.04083E7f * slat) * slat)))));
    }

    private static class LegendreTable {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        public final float[][] mP;
        public final float[][] mPDeriv;

        public LegendreTable(int maxN, float thetaRad) {
            float cos = (float) Math.cos((double) thetaRad);
            float sin = (float) Math.sin((double) thetaRad);
            this.mP = new float[(maxN + 1)][];
            this.mPDeriv = new float[(maxN + 1)][];
            this.mP[0] = new float[]{1.0f};
            this.mPDeriv[0] = new float[]{0.0f};
            for (int n = 1; n <= maxN; n++) {
                this.mP[n] = new float[(n + 1)];
                this.mPDeriv[n] = new float[(n + 1)];
                for (int m = 0; m <= n; m++) {
                    if (n == m) {
                        float[][] fArr = this.mP;
                        fArr[n][m] = fArr[n - 1][m - 1] * sin;
                        float[][] fArr2 = this.mPDeriv;
                        fArr2[n][m] = (fArr[n - 1][m - 1] * cos) + (fArr2[n - 1][m - 1] * sin);
                    } else if (n == 1 || m == n - 1) {
                        float[][] fArr3 = this.mP;
                        fArr3[n][m] = fArr3[n - 1][m] * cos;
                        float[][] fArr4 = this.mPDeriv;
                        fArr4[n][m] = ((-sin) * fArr3[n - 1][m]) + (fArr4[n - 1][m] * cos);
                    } else {
                        float k = ((float) (((n - 1) * (n - 1)) - (m * m))) / ((float) (((n * 2) - 1) * ((n * 2) - 3)));
                        float[][] fArr5 = this.mP;
                        fArr5[n][m] = (fArr5[n - 1][m] * cos) - (fArr5[n - 2][m] * k);
                        float[][] fArr6 = this.mPDeriv;
                        fArr6[n][m] = (((-sin) * fArr5[n - 1][m]) + (fArr6[n - 1][m] * cos)) - (fArr6[n - 2][m] * k);
                    }
                }
            }
        }
    }

    private static float[][] computeSchmidtQuasiNormFactors(int maxN) {
        float[][] schmidtQuasiNorm = new float[(maxN + 1)][];
        schmidtQuasiNorm[0] = new float[]{1.0f};
        for (int n = 1; n <= maxN; n++) {
            schmidtQuasiNorm[n] = new float[(n + 1)];
            schmidtQuasiNorm[n][0] = (schmidtQuasiNorm[n - 1][0] * ((float) ((n * 2) - 1))) / ((float) n);
            int m = 1;
            while (m <= n) {
                schmidtQuasiNorm[n][m] = schmidtQuasiNorm[n][m - 1] * ((float) Math.sqrt((double) (((float) (((n - m) + 1) * (m == 1 ? 2 : 1))) / ((float) (n + m)))));
                m++;
            }
        }
        return schmidtQuasiNorm;
    }
}
