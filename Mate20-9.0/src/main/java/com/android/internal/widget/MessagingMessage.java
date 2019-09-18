package com.android.internal.widget;

import android.app.ActivityManager;
import android.app.Notification;
import android.view.View;
import com.android.internal.widget.MessagingLinearLayout;
import java.util.Objects;

public interface MessagingMessage extends MessagingLinearLayout.MessagingChild {
    public static final String IMAGE_MIME_TYPE_PREFIX = "image/";

    MessagingMessageState getState();

    int getVisibility();

    void setVisibility(int i);

    static MessagingMessage createMessage(MessagingLayout layout, Notification.MessagingStyle.Message m) {
        if (!hasImage(m) || ActivityManager.isLowRamDeviceStatic()) {
            return MessagingTextMessage.createMessage(layout, m);
        }
        return MessagingImageMessage.createMessage(layout, m);
    }

    static void dropCache() {
        MessagingTextMessage.dropCache();
        MessagingImageMessage.dropCache();
    }

    static boolean hasImage(Notification.MessagingStyle.Message m) {
        return (m.getDataUri() == null || m.getDataMimeType() == null || !m.getDataMimeType().startsWith(IMAGE_MIME_TYPE_PREFIX)) ? false : true;
    }

    boolean setMessage(Notification.MessagingStyle.Message message) {
        getState().setMessage(message);
        return true;
    }

    Notification.MessagingStyle.Message getMessage() {
        return getState().getMessage();
    }

    boolean sameAs(Notification.MessagingStyle.Message message) {
        Notification.MessagingStyle.Message ownMessage = getMessage();
        if (!Objects.equals(message.getText(), ownMessage.getText()) || !Objects.equals(message.getSender(), ownMessage.getSender())) {
            return false;
        }
        if (((message.isRemoteInputHistory() != ownMessage.isRemoteInputHistory()) || Objects.equals(Long.valueOf(message.getTimestamp()), Long.valueOf(ownMessage.getTimestamp()))) && Objects.equals(message.getDataMimeType(), ownMessage.getDataMimeType()) && Objects.equals(message.getDataUri(), ownMessage.getDataUri())) {
            return true;
        }
        return false;
    }

    boolean sameAs(MessagingMessage message) {
        return sameAs(message.getMessage());
    }

    void removeMessage() {
        getGroup().removeMessage(this);
    }

    void setMessagingGroup(MessagingGroup group) {
        getState().setGroup(group);
    }

    void setIsHistoric(boolean isHistoric) {
        getState().setIsHistoric(isHistoric);
    }

    MessagingGroup getGroup() {
        return getState().getGroup();
    }

    void setIsHidingAnimated(boolean isHiding) {
        getState().setIsHidingAnimated(isHiding);
    }

    boolean isHidingAnimated() {
        return getState().isHidingAnimated();
    }

    void hideAnimated() {
        setIsHidingAnimated(true);
        getGroup().performRemoveAnimation(getView(), new Runnable() {
            public final void run() {
                MessagingMessage.this.setIsHidingAnimated(false);
            }
        });
    }

    boolean hasOverlappingRendering() {
        return false;
    }

    void recycle() {
        getState().recycle();
    }

    View getView() {
        return (View) this;
    }

    void setColor(int textColor) {
    }
}
