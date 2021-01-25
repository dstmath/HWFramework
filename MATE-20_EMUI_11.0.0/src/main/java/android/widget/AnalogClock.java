package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RemoteViews;
import com.android.internal.R;
import java.util.TimeZone;

@RemoteViews.RemoteView
@Deprecated
public class AnalogClock extends View {
    private boolean mAttached;
    private Time mCalendar;
    private boolean mChanged;
    @UnsupportedAppUsage
    private Drawable mDial;
    private int mDialHeight;
    private int mDialWidth;
    private float mHour;
    @UnsupportedAppUsage
    private Drawable mHourHand;
    private final BroadcastReceiver mIntentReceiver;
    @UnsupportedAppUsage
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
            /* class android.widget.AnalogClock.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                    String tz = intent.getStringExtra("time-zone");
                    AnalogClock.this.mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
                }
                AnalogClock.this.onTimeChanged();
                AnalogClock.this.invalidate();
            }
        };
        context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnalogClock, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.AnalogClock, attrs, a, defStyleAttr, defStyleRes);
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

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            getContext().registerReceiverAsUser(this.mIntentReceiver, Process.myUserHandle(), filter, null, getHandler());
        }
        this.mCalendar = new Time();
        onTimeChanged();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            getContext().unregisterReceiver(this.mIntentReceiver);
            this.mAttached = false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int i2;
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        float hScale = 1.0f;
        float vScale = 1.0f;
        if (widthMode != 0 && widthSize < (i2 = this.mDialWidth)) {
            hScale = ((float) widthSize) / ((float) i2);
        }
        if (heightMode != 0 && heightSize < (i = this.mDialHeight)) {
            vScale = ((float) heightSize) / ((float) i);
        }
        float scale = Math.min(hScale, vScale);
        setMeasuredDimension(resolveSizeAndState((int) (((float) this.mDialWidth) * scale), widthMeasureSpec, 0), resolveSizeAndState((int) (((float) this.mDialHeight) * scale), heightMeasureSpec, 0));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mChanged = true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
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
            int w2 = hourHand.getIntrinsicWidth();
            int h2 = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w2 / 2), y - (h2 / 2), (w2 / 2) + x, y + (h2 / 2));
        }
        hourHand.draw(canvas);
        canvas.restore();
        canvas.save();
        canvas.rotate((this.mMinutes / 60.0f) * 360.0f, (float) x, (float) y);
        Drawable minuteHand = this.mMinuteHand;
        if (changed) {
            int w3 = minuteHand.getIntrinsicWidth();
            int h3 = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w3 / 2), y - (h3 / 2), (w3 / 2) + x, y + (h3 / 2));
        }
        minuteHand.draw(canvas);
        canvas.restore();
        if (scaled) {
            canvas.restore();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
