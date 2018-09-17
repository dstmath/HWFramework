package android.widget;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewHierarchyEncoder;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import java.util.Calendar;
import java.util.TimeZone;
import libcore.icu.LocaleData;

@RemoteView
public class TextClock extends TextView {
    @Deprecated
    public static final CharSequence DEFAULT_FORMAT_12_HOUR = "h:mm a";
    @Deprecated
    public static final CharSequence DEFAULT_FORMAT_24_HOUR = "H:mm";
    private CharSequence mDescFormat;
    private CharSequence mDescFormat12;
    private CharSequence mDescFormat24;
    @ExportedProperty
    private CharSequence mFormat;
    private CharSequence mFormat12;
    private CharSequence mFormat24;
    private ContentObserver mFormatChangeObserver;
    @ExportedProperty
    private boolean mHasSeconds;
    private final BroadcastReceiver mIntentReceiver;
    private boolean mRegistered;
    private boolean mShouldRunTicker;
    private boolean mShowCurrentUserTime;
    private final Runnable mTicker;
    private Calendar mTime;
    private String mTimeZone;

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            TextClock.this.chooseFormat();
            TextClock.this.onTimeChanged();
        }

        public void onChange(boolean selfChange, Uri uri) {
            TextClock.this.chooseFormat();
            TextClock.this.onTimeChanged();
        }
    }

    public TextClock(Context context) {
        super(context);
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (TextClock.this.mTimeZone == null && "android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                    TextClock.this.createTime(intent.getStringExtra("time-zone"));
                }
                TextClock.this.onTimeChanged();
            }
        };
        this.mTicker = new Runnable() {
            public void run() {
                TextClock.this.onTimeChanged();
                long now = SystemClock.uptimeMillis();
                TextClock.this.getHandler().postAtTime(TextClock.this.mTicker, now + (1000 - (now % 1000)));
            }
        };
        init();
    }

    public TextClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextClock(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TextClock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mIntentReceiver = /* anonymous class already generated */;
        this.mTicker = /* anonymous class already generated */;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextClock, defStyleAttr, defStyleRes);
        try {
            this.mFormat12 = a.getText(0);
            this.mFormat24 = a.getText(1);
            this.mTimeZone = a.getString(2);
            init();
        } finally {
            a.recycle();
        }
    }

    private void init() {
        if (this.mFormat12 == null || this.mFormat24 == null) {
            LocaleData ld = LocaleData.get(getContext().getResources().getConfiguration().locale);
            if (this.mFormat12 == null) {
                this.mFormat12 = ld.timeFormat_hm;
            }
            if (this.mFormat24 == null) {
                this.mFormat24 = ld.timeFormat_Hm;
            }
        }
        createTime(this.mTimeZone);
        chooseFormat();
    }

    private void createTime(String timeZone) {
        if (timeZone != null) {
            this.mTime = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        } else {
            this.mTime = Calendar.getInstance();
        }
    }

    @ExportedProperty
    public CharSequence getFormat12Hour() {
        return this.mFormat12;
    }

    @RemotableViewMethod
    public void setFormat12Hour(CharSequence format) {
        this.mFormat12 = format;
        chooseFormat();
        onTimeChanged();
    }

    public void setContentDescriptionFormat12Hour(CharSequence format) {
        this.mDescFormat12 = format;
        chooseFormat();
        onTimeChanged();
    }

    @ExportedProperty
    public CharSequence getFormat24Hour() {
        return this.mFormat24;
    }

    @RemotableViewMethod
    public void setFormat24Hour(CharSequence format) {
        this.mFormat24 = format;
        chooseFormat();
        onTimeChanged();
    }

    public void setContentDescriptionFormat24Hour(CharSequence format) {
        this.mDescFormat24 = format;
        chooseFormat();
        onTimeChanged();
    }

    public void setShowCurrentUserTime(boolean showCurrentUserTime) {
        this.mShowCurrentUserTime = showCurrentUserTime;
        chooseFormat();
        onTimeChanged();
        unregisterObserver();
        registerObserver();
    }

    public boolean is24HourModeEnabled() {
        if (this.mShowCurrentUserTime) {
            return DateFormat.is24HourFormat(getContext(), ActivityManager.getCurrentUser());
        }
        return DateFormat.is24HourFormat(getContext());
    }

    public String getTimeZone() {
        return this.mTimeZone;
    }

    @RemotableViewMethod
    public void setTimeZone(String timeZone) {
        this.mTimeZone = timeZone;
        createTime(timeZone);
        onTimeChanged();
    }

    public CharSequence getFormat() {
        return this.mFormat;
    }

    private void chooseFormat() {
        boolean format24Requested = is24HourModeEnabled();
        LocaleData ld = LocaleData.get(getContext().getResources().getConfiguration().locale);
        if (format24Requested) {
            this.mFormat = abc(this.mFormat24, this.mFormat12, ld.timeFormat_Hm);
            this.mDescFormat = abc(this.mDescFormat24, this.mDescFormat12, this.mFormat);
        } else {
            this.mFormat = abc(this.mFormat12, this.mFormat24, ld.timeFormat_hm);
            this.mDescFormat = abc(this.mDescFormat12, this.mDescFormat24, this.mFormat);
        }
        boolean hadSeconds = this.mHasSeconds;
        this.mHasSeconds = DateFormat.hasSeconds(this.mFormat);
        if (this.mShouldRunTicker && hadSeconds != this.mHasSeconds) {
            if (hadSeconds) {
                getHandler().removeCallbacks(this.mTicker);
            } else {
                this.mTicker.run();
            }
        }
    }

    private static CharSequence abc(CharSequence a, CharSequence b, CharSequence c) {
        if (a == null) {
            return b == null ? c : b;
        } else {
            return a;
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mRegistered) {
            this.mRegistered = true;
            registerReceiver();
            registerObserver();
            createTime(this.mTimeZone);
        }
    }

    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (!this.mShouldRunTicker && isVisible) {
            this.mShouldRunTicker = true;
            if (this.mHasSeconds) {
                this.mTicker.run();
            } else {
                onTimeChanged();
            }
        } else if (this.mShouldRunTicker && (isVisible ^ 1) != 0) {
            this.mShouldRunTicker = false;
            getHandler().removeCallbacks(this.mTicker);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mRegistered) {
            unregisterReceiver();
            unregisterObserver();
            this.mRegistered = false;
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        getContext().registerReceiverAsUser(this.mIntentReceiver, Process.myUserHandle(), filter, null, getHandler());
    }

    private void registerObserver() {
        if (this.mRegistered) {
            if (this.mFormatChangeObserver == null) {
                this.mFormatChangeObserver = new FormatChangeObserver(getHandler());
            }
            ContentResolver resolver = getContext().getContentResolver();
            if (this.mShowCurrentUserTime) {
                resolver.registerContentObserver(System.CONTENT_URI, true, this.mFormatChangeObserver, -1);
            } else {
                resolver.registerContentObserver(System.CONTENT_URI, true, this.mFormatChangeObserver);
            }
        }
    }

    private void unregisterReceiver() {
        getContext().unregisterReceiver(this.mIntentReceiver);
    }

    private void unregisterObserver() {
        if (this.mFormatChangeObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(this.mFormatChangeObserver);
        }
    }

    private void onTimeChanged() {
        if (this.mShouldRunTicker) {
            this.mTime.setTimeInMillis(System.currentTimeMillis());
            setText(DateFormat.format(this.mFormat, this.mTime));
            setContentDescription(DateFormat.format(this.mDescFormat, this.mTime));
        }
    }

    protected void encodeProperties(ViewHierarchyEncoder stream) {
        String str = null;
        super.encodeProperties(stream);
        CharSequence s = getFormat12Hour();
        stream.addProperty("format12Hour", s == null ? null : s.toString());
        s = getFormat24Hour();
        stream.addProperty("format24Hour", s == null ? null : s.toString());
        String str2 = "format";
        if (this.mFormat != null) {
            str = this.mFormat.toString();
        }
        stream.addProperty(str2, str);
        stream.addProperty("hasSeconds", this.mHasSeconds);
    }
}
