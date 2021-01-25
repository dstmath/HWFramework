package com.android.server.am;

import android.content.Context;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/* access modifiers changed from: package-private */
public final class CarUserSwitchingDialog extends UserSwitchingDialog {
    private static final String TAG = "ActivityManagerCarUserSwitchingDialog";

    public CarUserSwitchingDialog(ActivityManagerService service, Context context, UserInfo oldUser, UserInfo newUser, boolean aboveSystem, String switchingFromSystemUserMessage, String switchingToSystemUserMessage) {
        super(service, context, oldUser, newUser, aboveSystem, switchingFromSystemUserMessage, switchingToSystemUserMessage);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.UserSwitchingDialog
    public void inflateContent() {
        setCancelable(false);
        Resources res = getContext().getResources();
        getContext().setTheme(16974824);
        View view = LayoutInflater.from(getContext()).inflate(17367112, (ViewGroup) null);
        Bitmap bitmap = ((UserManager) getContext().getSystemService("user")).getUserIcon(this.mNewUser.id);
        if (bitmap != null) {
            ((ImageView) view.findViewById(16909580)).setImageDrawable(CircleFramedDrawable.getInstance(bitmap, res.getDimension(17104985)));
        }
        ((TextView) view.findViewById(16909579)).setText(res.getString(17039750));
        setView(view);
    }

    static class CircleFramedDrawable extends Drawable {
        private final Bitmap mBitmap;
        private RectF mDstRect;
        private final Paint mPaint = new Paint();
        private float mScale;
        private final int mSize;
        private Rect mSrcRect;

        public static CircleFramedDrawable getInstance(Bitmap icon, float iconSize) {
            return new CircleFramedDrawable(icon, (int) iconSize);
        }

        public CircleFramedDrawable(Bitmap icon, int size) {
            this.mSize = size;
            int i = this.mSize;
            this.mBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(this.mBitmap);
            int width = icon.getWidth();
            int height = icon.getHeight();
            int square = Math.min(width, height);
            Rect cropRect = new Rect((width - square) / 2, (height - square) / 2, square, square);
            int i2 = this.mSize;
            RectF circleRect = new RectF(0.0f, 0.0f, (float) i2, (float) i2);
            Path fillPath = new Path();
            fillPath.addArc(circleRect, 0.0f, 360.0f);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            this.mPaint.setAntiAlias(true);
            this.mPaint.setColor(-16777216);
            this.mPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(fillPath, this.mPaint);
            this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(icon, cropRect, circleRect, this.mPaint);
            this.mPaint.setXfermode(null);
            this.mScale = 1.0f;
            int i3 = this.mSize;
            this.mSrcRect = new Rect(0, 0, i3, i3);
            int i4 = this.mSize;
            this.mDstRect = new RectF(0.0f, 0.0f, (float) i4, (float) i4);
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            float f = this.mScale;
            int i = this.mSize;
            float pad = (((float) i) - (f * ((float) i))) / 2.0f;
            this.mDstRect.set(pad, pad, ((float) i) - pad, ((float) i) - pad);
            canvas.drawBitmap(this.mBitmap, this.mSrcRect, this.mDstRect, (Paint) null);
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }
    }
}
