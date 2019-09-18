package com.android.internal.widget;

import android.app.Notification;
import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Pools;
import android.view.LayoutInflater;
import android.widget.RemoteViews;

@RemoteViews.RemoteView
public class MessagingTextMessage extends ImageFloatingTextView implements MessagingMessage {
    private static Pools.SimplePool<MessagingTextMessage> sInstancePool = new Pools.SynchronizedPool(20);
    private final MessagingMessageState mState = new MessagingMessageState(this);

    public MessagingTextMessage(Context context) {
        super(context);
    }

    public MessagingTextMessage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessagingTextMessage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MessagingTextMessage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MessagingMessageState getState() {
        return this.mState;
    }

    public boolean setMessage(Notification.MessagingStyle.Message message) {
        super.setMessage(message);
        setText(message.getText());
        return true;
    }

    /* JADX WARNING: type inference failed for: r2v2, types: [android.view.View] */
    /* JADX WARNING: Multi-variable type inference failed */
    static MessagingMessage createMessage(MessagingLayout layout, Notification.MessagingStyle.Message m) {
        MessagingLinearLayout messagingLinearLayout = layout.getMessagingLinearLayout();
        MessagingTextMessage createdMessage = (MessagingTextMessage) sInstancePool.acquire();
        if (createdMessage == null) {
            createdMessage = LayoutInflater.from(layout.getContext()).inflate(17367200, messagingLinearLayout, false);
            createdMessage.addOnLayoutChangeListener(MessagingLayout.MESSAGING_PROPERTY_ANIMATOR);
        }
        createdMessage.setMessage(m);
        return createdMessage;
    }

    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    public static void dropCache() {
        sInstancePool = new Pools.SynchronizedPool(10);
    }

    public int getMeasuredType() {
        if ((getMeasuredHeight() < (getLayoutHeight() + getPaddingTop()) + getPaddingBottom()) && getLineCount() <= 1) {
            return 2;
        }
        Layout layout = getLayout();
        if (layout == null) {
            return 2;
        }
        return layout.getEllipsisCount(layout.getLineCount() - 1) > 0 ? 1 : 0;
    }

    public void setMaxDisplayedLines(int lines) {
        setMaxLines(lines);
    }

    public int getConsumedLines() {
        return getLineCount();
    }

    public int getLayoutHeight() {
        Layout layout = getLayout();
        if (layout == null) {
            return 0;
        }
        return layout.getHeight();
    }

    public void setColor(int color) {
        setTextColor(color);
    }
}
