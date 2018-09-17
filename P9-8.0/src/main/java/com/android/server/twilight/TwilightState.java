package com.android.server.twilight;

import android.text.format.DateFormat;
import java.util.Calendar;

public final class TwilightState {
    private final long mSunriseTimeMillis;
    private final long mSunsetTimeMillis;

    public TwilightState(long sunriseTimeMillis, long sunsetTimeMillis) {
        this.mSunriseTimeMillis = sunriseTimeMillis;
        this.mSunsetTimeMillis = sunsetTimeMillis;
    }

    public long sunriseTimeMillis() {
        return this.mSunriseTimeMillis;
    }

    public Calendar sunrise() {
        Calendar sunrise = Calendar.getInstance();
        sunrise.setTimeInMillis(this.mSunriseTimeMillis);
        return sunrise;
    }

    public long sunsetTimeMillis() {
        return this.mSunsetTimeMillis;
    }

    public Calendar sunset() {
        Calendar sunset = Calendar.getInstance();
        sunset.setTimeInMillis(this.mSunsetTimeMillis);
        return sunset;
    }

    public boolean isNight() {
        long now = System.currentTimeMillis();
        if (now < this.mSunsetTimeMillis || now >= this.mSunriseTimeMillis) {
            return false;
        }
        return true;
    }

    public boolean equals(Object o) {
        return o instanceof TwilightState ? equals((TwilightState) o) : false;
    }

    public boolean equals(TwilightState other) {
        if (other != null && this.mSunriseTimeMillis == other.mSunriseTimeMillis && this.mSunsetTimeMillis == other.mSunsetTimeMillis) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Long.hashCode(this.mSunriseTimeMillis) ^ Long.hashCode(this.mSunsetTimeMillis);
    }

    public String toString() {
        return "TwilightState { sunrise=" + DateFormat.format("MM-dd HH:mm", this.mSunriseTimeMillis) + " sunset=" + DateFormat.format("MM-dd HH:mm", this.mSunsetTimeMillis) + " }";
    }
}
