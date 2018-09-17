package android.widget;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import java.util.Calendar;

@Deprecated
public class DigitalClock extends TextView {
    Calendar mCalendar;
    String mFormat;
    private FormatChangeObserver mFormatChangeObserver;
    private Handler mHandler;
    private Runnable mTicker;
    private boolean mTickerStopped = false;

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            DigitalClock.this.setFormat();
        }
    }

    public DigitalClock(Context context) {
        super(context);
        initClock();
    }

    public DigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock();
    }

    private void initClock() {
        if (this.mCalendar == null) {
            this.mCalendar = Calendar.getInstance();
        }
    }

    protected void onAttachedToWindow() {
        this.mTickerStopped = false;
        super.onAttachedToWindow();
        this.mFormatChangeObserver = new FormatChangeObserver();
        getContext().getContentResolver().registerContentObserver(System.CONTENT_URI, true, this.mFormatChangeObserver);
        setFormat();
        this.mHandler = new Handler();
        this.mTicker = new Runnable() {
            public void run() {
                if (!DigitalClock.this.mTickerStopped) {
                    DigitalClock.this.mCalendar.setTimeInMillis(System.currentTimeMillis());
                    DigitalClock.this.setText(DateFormat.format(DigitalClock.this.mFormat, DigitalClock.this.mCalendar));
                    DigitalClock.this.invalidate();
                    long now = SystemClock.uptimeMillis();
                    DigitalClock.this.mHandler.postAtTime(DigitalClock.this.mTicker, now + (1000 - (now % 1000)));
                }
            }
        };
        this.mTicker.run();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mTickerStopped = true;
        getContext().getContentResolver().unregisterContentObserver(this.mFormatChangeObserver);
    }

    private void setFormat() {
        this.mFormat = DateFormat.getTimeFormatString(getContext());
    }

    public CharSequence getAccessibilityClassName() {
        return DigitalClock.class.getName();
    }
}
