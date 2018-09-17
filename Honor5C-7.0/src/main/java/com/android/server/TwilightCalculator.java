package com.android.server;

public class TwilightCalculator {
    private static final float ALTIDUTE_CORRECTION_CIVIL_TWILIGHT = -0.10471976f;
    private static final float C1 = 0.0334196f;
    private static final float C2 = 3.49066E-4f;
    private static final float C3 = 5.236E-6f;
    public static final int DAY = 0;
    private static final float DEGREES_TO_RADIANS = 0.017453292f;
    private static final float J0 = 9.0E-4f;
    public static final int NIGHT = 1;
    private static final float OBLIQUITY = 0.4092797f;
    private static final long UTC_2000 = 946728000000L;
    public int mState;
    public long mSunrise;
    public long mSunset;

    public void calculateTwilight(long time, double latiude, double longitude) {
        float daysSince2000 = ((float) (time - UTC_2000)) / 8.64E7f;
        float meanAnomaly = 6.24006f + (0.01720197f * daysSince2000);
        double solarLng = (1.796593063d + (((((double) meanAnomaly) + (Math.sin((double) meanAnomaly) * 0.03341960161924362d)) + (Math.sin((double) (2.0f * meanAnomaly)) * 3.4906598739326E-4d)) + (Math.sin((double) (3.0f * meanAnomaly)) * 5.236000106378924E-6d))) + 3.141592653589793d;
        double arcLongitude = (-longitude) / 360.0d;
        double solarTransitJ2000 = ((((double) (J0 + ((float) Math.round(((double) (daysSince2000 - J0)) - arcLongitude)))) + arcLongitude) + (Math.sin((double) meanAnomaly) * 0.0053d)) + (Math.sin(2.0d * solarLng) * -0.0069d);
        double solarDec = Math.asin(Math.sin(solarLng) * Math.sin(0.4092797040939331d));
        double latRad = latiude * 0.01745329238474369d;
        double cosHourAngle = (Math.sin(-0.10471975803375244d) - (Math.sin(latRad) * Math.sin(solarDec))) / (Math.cos(latRad) * Math.cos(solarDec));
        if (cosHourAngle >= 1.0d) {
            this.mState = NIGHT;
            this.mSunset = -1;
            this.mSunrise = -1;
        } else if (cosHourAngle <= -1.0d) {
            this.mState = DAY;
            this.mSunset = -1;
            this.mSunrise = -1;
        } else {
            float hourAngle = (float) (Math.acos(cosHourAngle) / 6.283185307179586d);
            this.mSunset = Math.round((((double) hourAngle) + solarTransitJ2000) * 8.64E7d) + UTC_2000;
            this.mSunrise = Math.round((solarTransitJ2000 - ((double) hourAngle)) * 8.64E7d) + UTC_2000;
            if (this.mSunrise < time) {
                if (this.mSunset > time) {
                    this.mState = DAY;
                }
            }
            this.mState = NIGHT;
        }
    }
}
