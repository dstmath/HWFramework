package android.widget;

import android.annotation.UnsupportedAppUsage;
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
import android.os.UserHandle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.ViewDebug;
import android.view.ViewHierarchyEncoder;
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import android.widget.RemoteViews;
import com.android.internal.R;
import java.util.Calendar;
import java.util.TimeZone;
import libcore.icu.LocaleData;

@RemoteViews.RemoteView
public class TextClock extends TextView {
    @Deprecated
    public static final CharSequence DEFAULT_FORMAT_12_HOUR = "h:mm a";
    @Deprecated
    public static final CharSequence DEFAULT_FORMAT_24_HOUR = "H:mm";
    private CharSequence mDescFormat;
    private CharSequence mDescFormat12;
    private CharSequence mDescFormat24;
    @ViewDebug.ExportedProperty
    private CharSequence mFormat;
    private CharSequence mFormat12;
    private CharSequence mFormat24;
    private ContentObserver mFormatChangeObserver;
    @ViewDebug.ExportedProperty
    private boolean mHasSeconds;
    private final BroadcastReceiver mIntentReceiver;
    private boolean mRegistered;
    private boolean mShouldRunTicker;
    private boolean mShowCurrentUserTime;
    private boolean mStopTicking;
    private final Runnable mTicker;
    private Calendar mTime;
    private String mTimeZone;

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<TextClock> {
        private int mFormat12HourId;
        private int mFormat24HourId;
        private int mIs24HourModeEnabledId;
        private boolean mPropertiesMapped = false;
        private int mTimeZoneId;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mFormat12HourId = propertyMapper.mapObject("format12Hour", 16843722);
            this.mFormat24HourId = propertyMapper.mapObject("format24Hour", 16843723);
            this.mIs24HourModeEnabledId = propertyMapper.mapBoolean("is24HourModeEnabled", 0);
            this.mTimeZoneId = propertyMapper.mapObject("timeZone", 16843724);
            this.mPropertiesMapped = true;
        }

        public void readProperties(TextClock node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readObject(this.mFormat12HourId, node.getFormat12Hour());
                propertyReader.readObject(this.mFormat24HourId, node.getFormat24Hour());
                propertyReader.readBoolean(this.mIs24HourModeEnabledId, node.is24HourModeEnabled());
                propertyReader.readObject(this.mTimeZoneId, node.getTimeZone());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
    }

    /* access modifiers changed from: private */
    public class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            TextClock.this.chooseFormat();
            TextClock.this.onTimeChanged();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            TextClock.this.chooseFormat();
            TextClock.this.onTimeChanged();
        }
    }

    public TextClock(Context context) {
        super(context);
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class android.widget.TextClock.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (!TextClock.this.mStopTicking) {
                    if (TextClock.this.mTimeZone == null && Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                        TextClock.this.createTime(intent.getStringExtra("time-zone"));
                    } else if (!TextClock.this.mShouldRunTicker && (Intent.ACTION_TIME_TICK.equals(intent.getAction()) || Intent.ACTION_TIME_CHANGED.equals(intent.getAction()))) {
                        return;
                    }
                    TextClock.this.onTimeChanged();
                }
            }
        };
        this.mTicker = new Runnable() {
            /* class android.widget.TextClock.AnonymousClass2 */

            public void run() {
                if (!TextClock.this.mStopTicking) {
                    TextClock.this.onTimeChanged();
                    long now = SystemClock.uptimeMillis();
                    TextClock.this.getHandler().postAtTime(TextClock.this.mTicker, (1000 - (now % 1000)) + now);
                }
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

    /* JADX INFO: finally extract failed */
    public TextClock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class android.widget.TextClock.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (!TextClock.this.mStopTicking) {
                    if (TextClock.this.mTimeZone == null && Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                        TextClock.this.createTime(intent.getStringExtra("time-zone"));
                    } else if (!TextClock.this.mShouldRunTicker && (Intent.ACTION_TIME_TICK.equals(intent.getAction()) || Intent.ACTION_TIME_CHANGED.equals(intent.getAction()))) {
                        return;
                    }
                    TextClock.this.onTimeChanged();
                }
            }
        };
        this.mTicker = new Runnable() {
            /* class android.widget.TextClock.AnonymousClass2 */

            public void run() {
                if (!TextClock.this.mStopTicking) {
                    TextClock.this.onTimeChanged();
                    long now = SystemClock.uptimeMillis();
                    TextClock.this.getHandler().postAtTime(TextClock.this.mTicker, (1000 - (now % 1000)) + now);
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextClock, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.TextClock, attrs, a, defStyleAttr, defStyleRes);
        try {
            this.mFormat12 = a.getText(0);
            this.mFormat24 = a.getText(1);
            this.mTimeZone = a.getString(2);
            a.recycle();
            init();
        } catch (Throwable th) {
            a.recycle();
            throw th;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createTime(String timeZone) {
        if (timeZone != null) {
            this.mTime = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        } else {
            this.mTime = Calendar.getInstance();
        }
    }

    @ViewDebug.ExportedProperty
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

    @ViewDebug.ExportedProperty
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

    public void refresh() {
        onTimeChanged();
        invalidate();
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

    @UnsupportedAppUsage
    public CharSequence getFormat() {
        return this.mFormat;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        }
        return a;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mRegistered) {
            this.mRegistered = true;
            registerReceiver();
            registerObserver();
            createTime(this.mTimeZone);
        }
    }

    @Override // android.view.View
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (!this.mShouldRunTicker && isVisible) {
            this.mShouldRunTicker = true;
            if (this.mHasSeconds) {
                this.mTicker.run();
            } else {
                onTimeChanged();
            }
        } else if (this.mShouldRunTicker && !isVisible) {
            this.mShouldRunTicker = false;
            getHandler().removeCallbacks(this.mTicker);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mRegistered) {
            unregisterReceiver();
            unregisterObserver();
            this.mRegistered = false;
        }
    }

    public void disableClockTick() {
        this.mStopTicking = true;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiverAsUser(this.mIntentReceiver, Process.myUserHandle(), filter, null, getHandler());
    }

    private void registerObserver() {
        if (this.mRegistered) {
            if (this.mFormatChangeObserver == null) {
                this.mFormatChangeObserver = new FormatChangeObserver(getHandler());
            }
            ContentResolver resolver = getContext().getContentResolver();
            Uri uri = Settings.System.getUriFor(Settings.System.TIME_12_24);
            if (this.mShowCurrentUserTime) {
                resolver.registerContentObserver(uri, true, this.mFormatChangeObserver, -1);
            } else {
                resolver.registerContentObserver(uri, true, this.mFormatChangeObserver, UserHandle.myUserId());
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void onTimeChanged() {
        this.mTime.setTimeInMillis(System.currentTimeMillis());
        setText(DateFormat.format(this.mFormat, this.mTime));
        setContentDescription(DateFormat.format(this.mDescFormat, this.mTime));
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void encodeProperties(ViewHierarchyEncoder stream) {
        super.encodeProperties(stream);
        CharSequence s = getFormat12Hour();
        String str = null;
        stream.addProperty("format12Hour", s == null ? null : s.toString());
        CharSequence s2 = getFormat24Hour();
        stream.addProperty("format24Hour", s2 == null ? null : s2.toString());
        CharSequence charSequence = this.mFormat;
        if (charSequence != null) {
            str = charSequence.toString();
        }
        stream.addProperty("format", str);
        stream.addProperty("hasSeconds", this.mHasSeconds);
    }
}
