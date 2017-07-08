package com.android.server.location;

import android.content.Context;
import android.location.Country;
import android.location.CountryListener;
import android.os.Handler;

public abstract class CountryDetectorBase {
    protected final Context mContext;
    protected Country mDetectedCountry;
    protected final Handler mHandler;
    protected CountryListener mListener;

    public abstract Country detectCountry();

    public abstract void stop();

    public CountryDetectorBase(Context ctx) {
        this.mContext = ctx;
        this.mHandler = new Handler();
    }

    public void setCountryListener(CountryListener listener) {
        this.mListener = listener;
    }

    protected void notifyListener(Country country) {
        if (this.mListener != null) {
            this.mListener.onCountryDetected(country);
        }
    }
}
