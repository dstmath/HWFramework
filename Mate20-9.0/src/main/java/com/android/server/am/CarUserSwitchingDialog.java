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
import android.widget.ImageView;
import android.widget.TextView;
import com.android.server.usb.UsbAudioDevice;

final class CarUserSwitchingDialog extends UserSwitchingDialog {
    private static final String TAG = "ActivityManagerCarUserSwitchingDialog";

    static class CircleFramedDrawable extends Drawable {
        private final Bitmap mBitmap = Bitmap.createBitmap(this.mSize, this.mSize, Bitmap.Config.ARGB_8888);
        private RectF mDstRect;
        private final Paint mPaint;
        private float mScale;
        private final int mSize;
        private Rect mSrcRect;

        public static CircleFramedDrawable getInstance(Bitmap icon, float iconSize) {
            return new CircleFramedDrawable(icon, (int) iconSize);
        }

        public CircleFramedDrawable(Bitmap icon, int size) {
            this.mSize = size;
            Canvas canvas = new Canvas(this.mBitmap);
            int width = icon.getWidth();
            int height = icon.getHeight();
            int square = Math.min(width, height);
            Rect cropRect = new Rect((width - square) / 2, (height - square) / 2, square, square);
            RectF circleRect = new RectF(0.0f, 0.0f, (float) this.mSize, (float) this.mSize);
            Path fillPath = new Path();
            fillPath.addArc(circleRect, 0.0f, 360.0f);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            this.mPaint = new Paint();
            this.mPaint.setAntiAlias(true);
            this.mPaint.setColor(UsbAudioDevice.kAudioDeviceMetaMask);
            this.mPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(fillPath, this.mPaint);
            this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(icon, cropRect, circleRect, this.mPaint);
            this.mPaint.setXfermode(null);
            this.mScale = 1.0f;
            this.mSrcRect = new Rect(0, 0, this.mSize, this.mSize);
            this.mDstRect = new RectF(0.0f, 0.0f, (float) this.mSize, (float) this.mSize);
        }

        public void draw(Canvas canvas) {
            float pad = (((float) this.mSize) - (this.mScale * ((float) this.mSize))) / 2.0f;
            this.mDstRect.set(pad, pad, ((float) this.mSize) - pad, ((float) this.mSize) - pad);
            canvas.drawBitmap(this.mBitmap, this.mSrcRect, this.mDstRect, null);
        }

        public int getOpacity() {
            return -3;
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }
    }

    public CarUserSwitchingDialog(ActivityManagerService service, Context context, UserInfo oldUser, UserInfo newUser, boolean aboveSystem, String switchingFromSystemUserMessage, String switchingToSystemUserMessage) {
        super(service, context, oldUser, newUser, aboveSystem, switchingFromSystemUserMessage, switchingToSystemUserMessage);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
    }

    /* access modifiers changed from: package-private */
    public void inflateContent() {
        setCancelable(false);
        Resources res = getContext().getResources();
        View view = LayoutInflater.from(getContext()).inflate(17367107, null);
        Bitmap bitmap = ((UserManager) getContext().getSystemService("user")).getUserIcon(this.mNewUser.id);
        if (bitmap != null) {
            ((ImageView) view.findViewById(16909511)).setImageDrawable(CircleFramedDrawable.getInstance(bitmap, res.getDimension(17104949)));
        }
        ((TextView) view.findViewById(16909510)).setText(res.getString(17039725));
        setView(view);
    }
}
