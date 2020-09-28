package com.android.internal.widget;

import android.app.Notification;
import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Pools;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import com.android.internal.R;

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

    @Override // com.android.internal.widget.MessagingMessage
    public MessagingMessageState getState() {
        return this.mState;
    }

    @Override // com.android.internal.widget.MessagingMessage
    public boolean setMessage(Notification.MessagingStyle.Message message) {
        super.setMessage(message);
        setText(message.getText());
        return true;
    }

    static MessagingMessage createMessage(MessagingLayout layout, Notification.MessagingStyle.Message m) {
        MessagingLinearLayout messagingLinearLayout = layout.getMessagingLinearLayout();
        MessagingTextMessage createdMessage = sInstancePool.acquire();
        if (createdMessage == null) {
            createdMessage = (MessagingTextMessage) LayoutInflater.from(layout.getContext()).inflate(R.layout.notification_template_messaging_text_message, (ViewGroup) messagingLinearLayout, false);
            createdMessage.addOnLayoutChangeListener(MessagingLayout.MESSAGING_PROPERTY_ANIMATOR);
        }
        createdMessage.setMessage(m);
        return createdMessage;
    }

    @Override // com.android.internal.widget.MessagingMessage
    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    public static void dropCache() {
        sInstancePool = new Pools.SynchronizedPool(10);
    }

    @Override // com.android.internal.widget.MessagingLinearLayout.MessagingChild
    public int getMeasuredType() {
        Layout layout;
        if ((!(getMeasuredHeight() < (getLayoutHeight() + getPaddingTop()) + getPaddingBottom()) || getLineCount() > 1) && (layout = getLayout()) != null) {
            return layout.getEllipsisCount(layout.getLineCount() - 1) > 0 ? 1 : 0;
        }
        return 2;
    }

    @Override // com.android.internal.widget.MessagingLinearLayout.MessagingChild
    public void setMaxDisplayedLines(int lines) {
        setMaxLines(lines);
    }

    @Override // com.android.internal.widget.MessagingLinearLayout.MessagingChild
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

    @Override // com.android.internal.widget.MessagingMessage
    public void setColor(int color) {
        setTextColor(color);
    }
}
