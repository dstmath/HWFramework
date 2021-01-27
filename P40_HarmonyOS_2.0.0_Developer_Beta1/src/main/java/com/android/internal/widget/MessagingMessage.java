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

    static MessagingMessage createMessage(MessagingLayout layout, Notification.MessagingStyle.Message m, ImageResolver resolver) {
        if (!hasImage(m) || ActivityManager.isLowRamDeviceStatic()) {
            return MessagingTextMessage.createMessage(layout, m);
        }
        return MessagingImageMessage.createMessage(layout, m, resolver);
    }

    static void dropCache() {
        MessagingTextMessage.dropCache();
        MessagingImageMessage.dropCache();
    }

    static boolean hasImage(Notification.MessagingStyle.Message m) {
        return (m.getDataUri() == null || m.getDataMimeType() == null || !m.getDataMimeType().startsWith(IMAGE_MIME_TYPE_PREFIX)) ? false : true;
    }

    default boolean setMessage(Notification.MessagingStyle.Message message) {
        getState().setMessage(message);
        return true;
    }

    default Notification.MessagingStyle.Message getMessage() {
        return getState().getMessage();
    }

    default boolean sameAs(Notification.MessagingStyle.Message message) {
        Notification.MessagingStyle.Message ownMessage = getMessage();
        if (!Objects.equals(message.getText(), ownMessage.getText()) || !Objects.equals(message.getSender(), ownMessage.getSender())) {
            return false;
        }
        if (((message.isRemoteInputHistory() != ownMessage.isRemoteInputHistory()) || Objects.equals(Long.valueOf(message.getTimestamp()), Long.valueOf(ownMessage.getTimestamp()))) && Objects.equals(message.getDataMimeType(), ownMessage.getDataMimeType()) && Objects.equals(message.getDataUri(), ownMessage.getDataUri())) {
            return true;
        }
        return false;
    }

    default boolean sameAs(MessagingMessage message) {
        return sameAs(message.getMessage());
    }

    default void removeMessage() {
        getGroup().removeMessage(this);
    }

    default void setMessagingGroup(MessagingGroup group) {
        getState().setGroup(group);
    }

    default void setIsHistoric(boolean isHistoric) {
        getState().setIsHistoric(isHistoric);
    }

    default MessagingGroup getGroup() {
        return getState().getGroup();
    }

    default void setIsHidingAnimated(boolean isHiding) {
        getState().setIsHidingAnimated(isHiding);
    }

    @Override // com.android.internal.widget.MessagingLinearLayout.MessagingChild
    default boolean isHidingAnimated() {
        return getState().isHidingAnimated();
    }

    @Override // com.android.internal.widget.MessagingLinearLayout.MessagingChild
    default void hideAnimated() {
        setIsHidingAnimated(true);
        getGroup().performRemoveAnimation(getView(), new Runnable() {
            /* class com.android.internal.widget.$$Lambda$MessagingMessage$goi5oiwdlMBbUvfJzNl7fGbZK0 */

            @Override // java.lang.Runnable
            public final void run() {
                MessagingMessage.this.setIsHidingAnimated(false);
            }
        });
    }

    default boolean hasOverlappingRendering() {
        return false;
    }

    default void recycle() {
        getState().recycle();
    }

    default View getView() {
        return (View) this;
    }

    default void setColor(int textColor) {
    }
}
