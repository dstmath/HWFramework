package com.huawei.android.pushselfshow.richpush.html;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.huawei.android.pushselfshow.utils.c;
import com.huawei.android.pushselfshow.utils.c.a;

public class PageProgressView extends ImageView implements a {
    private int a;
    private int b;
    private int c;
    private Rect d;
    private c e;

    public PageProgressView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        a(context);
    }

    public PageProgressView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        a(context);
    }

    private void a(Context context) {
        this.d = new Rect(0, 0, 0, 0);
        this.a = 0;
        this.b = 0;
        this.e = new c(this);
    }

    void a(int i) {
        this.a = this.b;
        this.b = i;
        this.c = (this.b - this.a) / 10;
        this.e.removeMessages(42);
        this.e.sendEmptyMessage(42);
    }

    public void handleMessage(Message message) {
        if (message.what == 42) {
            this.a = Math.min(this.b, this.a + this.c);
            this.d.right = (getWidth() * this.a) / 10000;
            invalidate();
            if (this.a < this.b) {
                this.e.sendMessageDelayed(this.e.obtainMessage(42), 40);
            }
        }
    }

    public void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        drawable.setBounds(this.d);
        drawable.draw(canvas);
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.d.left = 0;
        this.d.right = ((i3 - i) * this.a) / 10000;
        this.d.top = 0;
        this.d.bottom = i4 - i2;
    }
}
