package android.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.icu.text.MeasureFormat;
import android.icu.text.MeasureFormat.FormatWidth;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.net.Uri;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.RemotableViewMethod;
import android.view.View;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import com.android.internal.util.Protocol;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

@RemoteView
public class Chronometer extends TextView {
    private static final int HOUR_IN_SEC = 3600;
    private static final int MIN_IN_SEC = 60;
    private static final String TAG = "Chronometer";
    private long mBase;
    private boolean mCountDown;
    private String mFormat;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private Object[] mFormatterArgs;
    private Locale mFormatterLocale;
    private boolean mLogged;
    private long mNow;
    private OnChronometerTickListener mOnChronometerTickListener;
    private StringBuilder mRecycle;
    private boolean mRunning;
    private boolean mStarted;
    private final Runnable mTickRunnable;
    private boolean mVisible;

    public interface OnChronometerTickListener {
        void onChronometerTick(Chronometer chronometer);
    }

    public Chronometer(Context context) {
        this(context, null, 0);
    }

    public Chronometer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Chronometer(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Chronometer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mFormatterArgs = new Object[1];
        this.mRecycle = new StringBuilder(8);
        this.mTickRunnable = new Runnable() {
            public void run() {
                if (Chronometer.this.mRunning) {
                    Chronometer.this.updateText(SystemClock.elapsedRealtime());
                    Chronometer.this.dispatchChronometerTick();
                    Chronometer.this.postDelayed(Chronometer.this.mTickRunnable, 1000);
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Chronometer, defStyleAttr, defStyleRes);
        setFormat(a.getString(0));
        setCountDown(a.getBoolean(1, false));
        a.recycle();
        init();
    }

    private void init() {
        this.mBase = SystemClock.elapsedRealtime();
        updateText(this.mBase);
    }

    @RemotableViewMethod
    public void setCountDown(boolean countDown) {
        this.mCountDown = countDown;
        updateText(SystemClock.elapsedRealtime());
    }

    public boolean isCountDown() {
        return this.mCountDown;
    }

    public boolean isTheFinalCountDown() {
        try {
            getContext().startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://youtu.be/9jK-NcRmVcw")).addCategory("android.intent.category.BROWSABLE").addFlags(Protocol.BASE_NETWORK_AGENT));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @RemotableViewMethod
    public void setBase(long base) {
        this.mBase = base;
        dispatchChronometerTick();
        updateText(SystemClock.elapsedRealtime());
    }

    public long getBase() {
        return this.mBase;
    }

    @RemotableViewMethod
    public void setFormat(String format) {
        this.mFormat = format;
        if (format != null && this.mFormatBuilder == null) {
            this.mFormatBuilder = new StringBuilder(format.length() * 2);
        }
    }

    public String getFormat() {
        return this.mFormat;
    }

    public void setOnChronometerTickListener(OnChronometerTickListener listener) {
        this.mOnChronometerTickListener = listener;
    }

    public OnChronometerTickListener getOnChronometerTickListener() {
        return this.mOnChronometerTickListener;
    }

    public void start() {
        this.mStarted = true;
        updateRunning();
    }

    public void stop() {
        this.mStarted = false;
        updateRunning();
    }

    @RemotableViewMethod
    public void setStarted(boolean started) {
        this.mStarted = started;
        updateRunning();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mVisible = false;
        updateRunning();
    }

    protected void onWindowVisibilityChanged(int visibility) {
        boolean z = false;
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            z = true;
        }
        this.mVisible = z;
        updateRunning();
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        updateRunning();
    }

    private synchronized void updateText(long now) {
        this.mNow = now;
        long seconds = (this.mCountDown ? this.mBase - now : now - this.mBase) / 1000;
        boolean negative = false;
        if (seconds < 0) {
            seconds = -seconds;
            negative = true;
        }
        String text = DateUtils.formatElapsedTime(this.mRecycle, seconds);
        if (negative) {
            text = getResources().getString(R.string.negative_duration, new Object[]{text});
        }
        if (this.mFormat != null) {
            Locale loc = Locale.getDefault();
            if (this.mFormatter == null || (loc.equals(this.mFormatterLocale) ^ 1) != 0) {
                this.mFormatterLocale = loc;
                this.mFormatter = new Formatter(this.mFormatBuilder, loc);
            }
            this.mFormatBuilder.setLength(0);
            this.mFormatterArgs[0] = text;
            try {
                this.mFormatter.format(this.mFormat, this.mFormatterArgs);
                text = this.mFormatBuilder.toString();
            } catch (IllegalFormatException e) {
                if (!this.mLogged) {
                    Log.w(TAG, "Illegal format string: " + this.mFormat);
                    this.mLogged = true;
                }
            }
        }
        setText((CharSequence) text);
    }

    private void updateRunning() {
        boolean running = (this.mVisible && this.mStarted) ? isShown() : false;
        if (running != this.mRunning) {
            if (running) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                postDelayed(this.mTickRunnable, 1000);
            } else {
                removeCallbacks(this.mTickRunnable);
            }
            this.mRunning = running;
        }
    }

    void dispatchChronometerTick() {
        if (this.mOnChronometerTickListener != null) {
            this.mOnChronometerTickListener.onChronometerTick(this);
        }
    }

    private static String formatDuration(long ms) {
        int duration = (int) (ms / 1000);
        if (duration < 0) {
            duration = -duration;
        }
        int h = 0;
        int m = 0;
        if (duration >= HOUR_IN_SEC) {
            h = duration / HOUR_IN_SEC;
            duration -= h * HOUR_IN_SEC;
        }
        if (duration >= 60) {
            m = duration / 60;
            duration -= m * 60;
        }
        int s = duration;
        ArrayList<Measure> measures = new ArrayList();
        if (h > 0) {
            measures.add(new Measure(Integer.valueOf(h), MeasureUnit.HOUR));
        }
        if (m > 0) {
            measures.add(new Measure(Integer.valueOf(m), MeasureUnit.MINUTE));
        }
        measures.add(new Measure(Integer.valueOf(s), MeasureUnit.SECOND));
        return MeasureFormat.getInstance(Locale.getDefault(), FormatWidth.WIDE).formatMeasures((Measure[]) measures.toArray(new Measure[measures.size()]));
    }

    public CharSequence getContentDescription() {
        return formatDuration(this.mNow - this.mBase);
    }

    public CharSequence getAccessibilityClassName() {
        return Chronometer.class.getName();
    }
}
