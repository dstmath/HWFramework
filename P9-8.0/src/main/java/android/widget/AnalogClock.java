package android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import java.util.TimeZone;

@RemoteView
@Deprecated
public class AnalogClock extends View {
    private boolean mAttached;
    private Time mCalendar;
    private boolean mChanged;
    private Drawable mDial;
    private int mDialHeight;
    private int mDialWidth;
    private float mHour;
    private Drawable mHourHand;
    private final BroadcastReceiver mIntentReceiver;
    private Drawable mMinuteHand;
    private float mMinutes;

    public AnalogClock(Context context) {
        this(context, null);
    }

    public AnalogClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClock(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AnalogClock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.TIMEZONE_CHANGED")) {
                    AnalogClock.this.mCalendar = new Time(TimeZone.getTimeZone(intent.getStringExtra("time-zone")).getID());
                }
                AnalogClock.this.onTimeChanged();
                AnalogClock.this.invalidate();
            }
        };
        Resources r = context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnalogClock, defStyleAttr, defStyleRes);
        this.mDial = a.getDrawable(0);
        if (this.mDial == null) {
            this.mDial = context.getDrawable(R.drawable.clock_dial);
        }
        this.mHourHand = a.getDrawable(1);
        if (this.mHourHand == null) {
            this.mHourHand = context.getDrawable(R.drawable.clock_hand_hour);
        }
        this.mMinuteHand = a.getDrawable(2);
        if (this.mMinuteHand == null) {
            this.mMinuteHand = context.getDrawable(R.drawable.clock_hand_minute);
        }
        this.mCalendar = new Time();
        this.mDialWidth = this.mDial.getIntrinsicWidth();
        this.mDialHeight = this.mDial.getIntrinsicHeight();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_TICK");
            filter.addAction("android.intent.action.TIME_SET");
            filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            getContext().registerReceiverAsUser(this.mIntentReceiver, Process.myUserHandle(), filter, null, getHandler());
        }
        this.mCalendar = new Time();
        onTimeChanged();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            getContext().unregisterReceiver(this.mIntentReceiver);
            this.mAttached = false;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float hScale = 1.0f;
        float vScale = 1.0f;
        if (widthMode != 0 && widthSize < this.mDialWidth) {
            hScale = ((float) widthSize) / ((float) this.mDialWidth);
        }
        if (heightMode != 0 && heightSize < this.mDialHeight) {
            vScale = ((float) heightSize) / ((float) this.mDialHeight);
        }
        float scale = Math.min(hScale, vScale);
        -wrap6(View.resolveSizeAndState((int) (((float) this.mDialWidth) * scale), widthMeasureSpec, 0), View.resolveSizeAndState((int) (((float) this.mDialHeight) * scale), heightMeasureSpec, 0));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mChanged = true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean changed = this.mChanged;
        if (changed) {
            this.mChanged = false;
        }
        int availableWidth = this.mRight - this.mLeft;
        int availableHeight = this.mBottom - this.mTop;
        int x = availableWidth / 2;
        int y = availableHeight / 2;
        Drawable dial = this.mDial;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();
        boolean scaled = false;
        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min(((float) availableWidth) / ((float) w), ((float) availableHeight) / ((float) h));
            canvas.save();
            canvas.scale(scale, scale, (float) x, (float) y);
        }
        if (changed) {
            dial.setBounds(x - (w / 2), y - (h / 2), (w / 2) + x, (h / 2) + y);
        }
        dial.draw(canvas);
        canvas.save();
        canvas.rotate((this.mHour / 12.0f) * 360.0f, (float) x, (float) y);
        Drawable hourHand = this.mHourHand;
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w / 2), y - (h / 2), (w / 2) + x, (h / 2) + y);
        }
        hourHand.draw(canvas);
        canvas.restore();
        canvas.save();
        canvas.rotate((this.mMinutes / 60.0f) * 360.0f, (float) x, (float) y);
        Drawable minuteHand = this.mMinuteHand;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h / 2), (w / 2) + x, (h / 2) + y);
        }
        minuteHand.draw(canvas);
        canvas.restore();
        if (scaled) {
            canvas.restore();
        }
    }

    private void onTimeChanged() {
        this.mCalendar.setToNow();
        int hour = this.mCalendar.hour;
        this.mMinutes = ((float) this.mCalendar.minute) + (((float) this.mCalendar.second) / 60.0f);
        this.mHour = ((float) hour) + (this.mMinutes / 60.0f);
        this.mChanged = true;
        updateContentDescription(this.mCalendar);
    }

    private void updateContentDescription(Time time) {
        setContentDescription(DateUtils.formatDateTime(this.mContext, time.toMillis(false), 129));
    }
}
