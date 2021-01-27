package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Picture;
import android.graphics.Rect;

public class PictureDrawable extends Drawable {
    private Picture mPicture;

    public PictureDrawable(Picture picture) {
        this.mPicture = picture;
    }

    public Picture getPicture() {
        return this.mPicture;
    }

    public void setPicture(Picture picture) {
        this.mPicture = picture;
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (this.mPicture != null) {
            Rect bounds = getBounds();
            canvas.save();
            canvas.clipRect(bounds);
            canvas.translate((float) bounds.left, (float) bounds.top);
            canvas.drawPicture(this.mPicture);
            canvas.restore();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        Picture picture = this.mPicture;
        if (picture != null) {
            return picture.getWidth();
        }
        return -1;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        Picture picture = this.mPicture;
        if (picture != null) {
            return picture.getHeight();
        }
        return -1;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
    }
}
