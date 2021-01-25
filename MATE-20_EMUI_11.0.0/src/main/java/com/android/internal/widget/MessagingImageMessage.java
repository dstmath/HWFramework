package com.android.internal.widget;

import android.app.Notification;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pools;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;
import com.android.internal.R;
import java.io.IOException;

@RemoteViews.RemoteView
public class MessagingImageMessage extends ImageView implements MessagingMessage {
    private static final String TAG = "MessagingImageMessage";
    private static Pools.SimplePool<MessagingImageMessage> sInstancePool = new Pools.SynchronizedPool(10);
    private int mActualHeight;
    private int mActualWidth;
    private float mAspectRatio;
    private Drawable mDrawable;
    private final int mExtraSpacing;
    private ImageResolver mImageResolver;
    private final int mImageRounding;
    private boolean mIsIsolated;
    private final int mIsolatedSize;
    private final int mMaxImageHeight;
    private final int mMinImageHeight;
    private final Path mPath;
    private final MessagingMessageState mState;

    public MessagingImageMessage(Context context) {
        this(context, null);
    }

    public MessagingImageMessage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessagingImageMessage(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MessagingImageMessage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mState = new MessagingMessageState(this);
        this.mPath = new Path();
        this.mMinImageHeight = context.getResources().getDimensionPixelSize(R.dimen.messaging_image_min_size);
        this.mMaxImageHeight = context.getResources().getDimensionPixelSize(R.dimen.messaging_image_max_height);
        this.mImageRounding = context.getResources().getDimensionPixelSize(R.dimen.messaging_image_rounding);
        this.mExtraSpacing = context.getResources().getDimensionPixelSize(R.dimen.messaging_image_extra_spacing);
        setMaxHeight(this.mMaxImageHeight);
        this.mIsolatedSize = getResources().getDimensionPixelSize(R.dimen.messaging_avatar_size);
    }

    @Override // com.android.internal.widget.MessagingMessage
    public MessagingMessageState getState() {
        return this.mState;
    }

    @Override // com.android.internal.widget.MessagingMessage
    public boolean setMessage(Notification.MessagingStyle.Message message) {
        Drawable drawable;
        super.setMessage(message);
        try {
            Uri uri = message.getDataUri();
            if (this.mImageResolver != null) {
                drawable = this.mImageResolver.loadImage(uri);
            } else {
                drawable = LocalImageResolver.resolveImage(uri, getContext());
            }
            if (drawable == null) {
                return false;
            }
            int intrinsicHeight = drawable.getIntrinsicHeight();
            if (intrinsicHeight == 0) {
                Log.w(TAG, "Drawable with 0 intrinsic height was returned");
                return false;
            }
            this.mDrawable = drawable;
            this.mAspectRatio = ((float) this.mDrawable.getIntrinsicWidth()) / ((float) intrinsicHeight);
            setImageDrawable(drawable);
            setContentDescription(message.getText());
            return true;
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    static MessagingMessage createMessage(MessagingLayout layout, Notification.MessagingStyle.Message m, ImageResolver resolver) {
        MessagingLinearLayout messagingLinearLayout = layout.getMessagingLinearLayout();
        MessagingImageMessage createdMessage = sInstancePool.acquire();
        if (createdMessage == null) {
            createdMessage = (MessagingImageMessage) LayoutInflater.from(layout.getContext()).inflate(R.layout.notification_template_messaging_image_message, (ViewGroup) messagingLinearLayout, false);
            createdMessage.addOnLayoutChangeListener(MessagingLayout.MESSAGING_PROPERTY_ANIMATOR);
        }
        createdMessage.setImageResolver(resolver);
        if (createdMessage.setMessage(m)) {
            return createdMessage;
        }
        createdMessage.recycle();
        return MessagingTextMessage.createMessage(layout, m);
    }

    private void setImageResolver(ImageResolver resolver) {
        this.mImageResolver = resolver;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(getRoundedRectPath());
        int width = (int) Math.max((float) getActualWidth(), ((float) getActualHeight()) * this.mAspectRatio);
        int left = (int) (((float) (getActualWidth() - width)) / 2.0f);
        this.mDrawable.setBounds(left, 0, left + width, (int) (((float) width) / this.mAspectRatio));
        this.mDrawable.draw(canvas);
        canvas.restore();
    }

    public Path getRoundedRectPath() {
        int right = getActualWidth();
        int bottom = getActualHeight();
        this.mPath.reset();
        int i = this.mImageRounding;
        float roundnessX = Math.min((float) ((right - 0) / 2), (float) i);
        float roundnessY = Math.min((float) ((bottom - 0) / 2), (float) i);
        this.mPath.moveTo((float) 0, ((float) 0) + roundnessY);
        this.mPath.quadTo((float) 0, (float) 0, ((float) 0) + roundnessX, (float) 0);
        this.mPath.lineTo(((float) right) - roundnessX, (float) 0);
        this.mPath.quadTo((float) right, (float) 0, (float) right, ((float) 0) + roundnessY);
        this.mPath.lineTo((float) right, ((float) bottom) - roundnessY);
        this.mPath.quadTo((float) right, (float) bottom, ((float) right) - roundnessX, (float) bottom);
        this.mPath.lineTo(((float) 0) + roundnessX, (float) bottom);
        this.mPath.quadTo((float) 0, (float) bottom, (float) 0, ((float) bottom) - roundnessY);
        this.mPath.close();
        return this.mPath;
    }

    @Override // com.android.internal.widget.MessagingMessage
    public void recycle() {
        super.recycle();
        setImageBitmap(null);
        this.mDrawable = null;
        sInstancePool.release(this);
    }

    public static void dropCache() {
        sInstancePool = new Pools.SynchronizedPool(10);
    }

    @Override // com.android.internal.widget.MessagingLinearLayout.MessagingChild
    public int getMeasuredType() {
        int minImageHeight;
        int measuredHeight = getMeasuredHeight();
        if (this.mIsIsolated) {
            minImageHeight = this.mIsolatedSize;
        } else {
            minImageHeight = this.mMinImageHeight;
        }
        if (measuredHeight < minImageHeight && measuredHeight != this.mDrawable.getIntrinsicHeight()) {
            return 2;
        }
        return (this.mIsIsolated || measuredHeight == this.mDrawable.getIntrinsicHeight()) ? 0 : 1;
    }

    @Override // com.android.internal.widget.MessagingLinearLayout.MessagingChild
    public void setMaxDisplayedLines(int lines) {
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mIsIsolated) {
            setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setActualWidth(getStaticWidth());
        setActualHeight(getHeight());
    }

    @Override // com.android.internal.widget.MessagingLinearLayout.MessagingChild
    public int getConsumedLines() {
        return 3;
    }

    public void setActualWidth(int actualWidth) {
        this.mActualWidth = actualWidth;
        invalidate();
    }

    public int getActualWidth() {
        return this.mActualWidth;
    }

    public void setActualHeight(int actualHeight) {
        this.mActualHeight = actualHeight;
        invalidate();
    }

    public int getActualHeight() {
        return this.mActualHeight;
    }

    public int getStaticWidth() {
        if (this.mIsIsolated) {
            return getWidth();
        }
        return (int) (((float) getHeight()) * this.mAspectRatio);
    }

    public void setIsolated(boolean isolated) {
        if (this.mIsIsolated != isolated) {
            this.mIsIsolated = isolated;
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
            layoutParams.topMargin = isolated ? 0 : this.mExtraSpacing;
            setLayoutParams(layoutParams);
        }
    }

    @Override // com.android.internal.widget.MessagingLinearLayout.MessagingChild
    public int getExtraSpacing() {
        return this.mExtraSpacing;
    }
}
