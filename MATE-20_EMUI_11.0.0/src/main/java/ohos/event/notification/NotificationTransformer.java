package ohos.event.notification;

import android.app.ActivityThread;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.app.RemoteInput;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.widget.RemoteViews;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import ohos.aafwk.content.IntentParams;
import ohos.agp.components.ComponentProvider;
import ohos.agp.components.surfaceview.adapter.RemoteViewUtils;
import ohos.event.EventConstant;
import ohos.event.commonevent.IntentConverter;
import ohos.event.intentagent.IntentAgent;
import ohos.event.intentagent.IntentAgentAdapterUtils;
import ohos.event.notification.NotificationActionButton;
import ohos.event.notification.NotificationConstant;
import ohos.event.notification.NotificationRequest;
import ohos.event.notification.NotificationUserInput;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.common.sessioncore.AVToken;
import ohos.media.image.PixelMap;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.net.UriConverter;
import ohos.utils.adapter.PacMapUtils;
import ohos.utils.net.Uri;

public class NotificationTransformer {
    private static final String BUNDLE_KEY_MIME_TYPE = "type";
    private static final String BUNDLE_KEY_SENDER = "sender";
    private static final String BUNDLE_KEY_SENDER_PERSON = "sender_person";
    private static final String BUNDLE_KEY_TEXT = "text";
    private static final String BUNDLE_KEY_TIMESTAMP = "time";
    private static final String BUNDLE_KEY_URI = "uri";
    private static final String DEFAULT_CHANNEL_ID = "channel_default";
    private static final String DEFAULT_CHANNEL_NAME = "defaultChannelName";
    private static final String DELIMITER = "_";
    private static final int INDEX_ID = 2;
    private static final int INDEX_IDENTIFIER = 0;
    private static final int INDEX_OVERRIDEGROUPKEY = 5;
    private static final int INDEX_PKG_NAME = 1;
    private static final int INDEX_TAG = 3;
    private static final int INDEX_UID = 4;
    private static final int KEY_LENGTH = 6;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final int MIN_LENGTH = 5;
    private static final String SPLIT_CHAR = "\\|";
    private static final String TAG = "NotificationTransformer";
    private Context aospContext;
    private Icon appIcon;
    private String currentPkg;
    private Bitmap defaultBitmap;
    private NotificationChannel defaultChannel;

    private int getImportance(int i) {
        if (i == -1000) {
            return -1000;
        }
        if (i == 0) {
            return 0;
        }
        int i2 = 1;
        if (i != 1) {
            i2 = 2;
            if (i != 2) {
                i2 = 3;
                if (i != 3) {
                    return i != 4 ? 3 : 4;
                }
            }
        }
        return i2;
    }

    private int getLockscreenVisibility(int i) {
        if (i == 1) {
            return 1;
        }
        if (i != 2) {
            return i != 3 ? -1000 : -1;
        }
        return 0;
    }

    private int getZidaneImportance(int i) {
        if (i == -1000) {
            return -1000;
        }
        if (i == 0) {
            return 0;
        }
        int i2 = 1;
        if (i != 1) {
            i2 = 2;
            if (i != 2) {
                i2 = 3;
                if (i != 3) {
                    return i != 4 ? 3 : 4;
                }
            }
        }
        return i2;
    }

    private int getZidaneLockscreenVisibility(int i) {
        if (i == -1) {
            return 3;
        }
        if (i != 0) {
            return i != 1 ? -1000 : 1;
        }
        return 2;
    }

    /* access modifiers changed from: private */
    public static class Holder {
        private static final NotificationTransformer INSTANCE = new NotificationTransformer();

        private Holder() {
        }
    }

    private NotificationTransformer() {
        this.appIcon = null;
        this.defaultBitmap = null;
        this.defaultChannel = null;
        this.aospContext = null;
        this.currentPkg = null;
    }

    public static NotificationTransformer getInstance() {
        return Holder.INSTANCE;
    }

    public int getNotificationId(String str) throws IllegalArgumentException {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("notificationHashCode is invalid");
        }
        String[] split = str.split("_");
        if (split.length >= 1) {
            try {
                return Integer.parseInt(split[0]);
            } catch (NumberFormatException unused) {
                throw new IllegalArgumentException("notificationHashCode is not correct");
            }
        } else {
            throw new IllegalArgumentException("notificationHashCode formate is invalid");
        }
    }

    public Optional<Notification> transform(Context context, NotificationManager notificationManager, NotificationRequest notificationRequest) {
        return transform(context, notificationManager, notificationRequest, new Bundle());
    }

    public Optional<Notification> transform(Context context, NotificationManager notificationManager, NotificationRequest notificationRequest, Bundle bundle) {
        if (context == null || notificationManager == null || notificationRequest == null) {
            HiLog.error(LABEL, "NotificationTransformer::transform param is invalid", new Object[0]);
            return Optional.empty();
        }
        NotificationRequest.NotificationContent content = notificationRequest.getContent();
        if (content == null) {
            HiLog.debug(LABEL, "NotificationTransformer::transform NotificationContent is null", new Object[0]);
            return Optional.empty();
        }
        int contentType = content.getContentType();
        if (contentType == 0) {
            return transformNormalContent(context, notificationManager, notificationRequest, content, bundle);
        }
        if (contentType == 1) {
            return transformLongTextContent(context, notificationManager, notificationRequest, content, bundle);
        }
        if (contentType == 2) {
            return transformPictureContent(context, notificationManager, notificationRequest, content, bundle);
        }
        if (contentType == 3) {
            return transformConversationalContent(context, notificationManager, notificationRequest, content, bundle);
        }
        if (contentType == 4) {
            return transformMultiLineContent(context, notificationManager, notificationRequest, content, bundle);
        }
        if (contentType != 5) {
            return Optional.empty();
        }
        return transformMediaContent(context, notificationManager, notificationRequest, content, bundle);
    }

    public Optional<Notification> transformForegroundServiceNotification(Context context, NotificationManager notificationManager, NotificationRequest notificationRequest) {
        return transform(context, notificationManager, notificationRequest, null);
    }

    public Optional<NotificationRequest> transformToNotificationRequest(StatusBarNotification statusBarNotification) {
        return transformToNotificationRequest(statusBarNotification, false, 0, "");
    }

    public Optional<NotificationRequest> transformToNotificationRequest(StatusBarNotification statusBarNotification, boolean z, int i, String str) {
        if (statusBarNotification == null) {
            return Optional.empty();
        }
        Notification notification = statusBarNotification.getNotification();
        if (notification == null) {
            return Optional.empty();
        }
        Bundle bundle = notification.extras;
        if (bundle == null) {
            return Optional.empty();
        }
        if (z && !isValidNativeNotification(bundle, i, str)) {
            return Optional.empty();
        }
        NotificationRequest createNotificationRequest = createNotificationRequest(statusBarNotification, notification);
        if (createNotificationRequest == null) {
            return Optional.empty();
        }
        Optional<NotificationRequest.NotificationContent> content = getContent(bundle);
        if (!content.isPresent()) {
            return Optional.empty();
        }
        fillNotificationRequest(createNotificationRequest, content.get(), notification, bundle);
        return Optional.of(createNotificationRequest);
    }

    private boolean isValidNativeNotification(Bundle bundle, int i, String str) {
        int i2 = bundle.getInt("com.huawei.ohos.foundation.pid", -1);
        String string = bundle.getString("com.huawei.ohos.foundation.extends", "");
        if (i2 != i) {
            return false;
        }
        if (str == null || str.equals(string)) {
            return true;
        }
        return false;
    }

    private void fillPublicNotification(NotificationRequest notificationRequest, Notification notification) {
        Bundle bundle = notification.extras;
        if (bundle != null) {
            Optional<NotificationRequest.NotificationContent> content = getContent(bundle);
            if (content.isPresent()) {
                NotificationRequest notificationRequest2 = new NotificationRequest(notificationRequest.getNotificationId(), notificationRequest.getNotificationHashCode());
                notificationRequest2.setCreatorBundleName(notificationRequest.getCreatorBundleName()).setLittleIcon(notificationRequest.getLittleIcon()).setOwnerBundleName(notificationRequest.getOwnerBundleName()).setCreatorPid(notificationRequest.getCreatorPid()).setCreatorUid(notificationRequest.getCreatorUid());
                fillNotificationRequest(notificationRequest2, content.get(), notification, bundle);
                notificationRequest.setPublicNotification(notificationRequest2);
            }
        }
    }

    private NotificationRequest createNotificationRequest(StatusBarNotification statusBarNotification, Notification notification) {
        Context aospContext2 = getAospContext();
        Optional<Bitmap> iconToBitmap = iconToBitmap(notification.getSmallIcon(), aospContext2);
        if (!iconToBitmap.isPresent()) {
            HiLog.info(LABEL, "NotificationTransformer::createNotificationRequest smallPixelMap is null,", new Object[0]);
            return null;
        }
        Bitmap copy = iconToBitmap.get().copy(Bitmap.Config.ARGB_8888, true);
        if (copy == null) {
            HiLog.info(LABEL, "NotificationTransformer::createNotificationRequest smallIconBitmap is null.", new Object[0]);
            return null;
        }
        PixelMap createShellPixelMap = ImageDoubleFwConverter.createShellPixelMap(copy);
        if (createShellPixelMap == null) {
            return null;
        }
        String key = statusBarNotification.getKey();
        if (key == null || key.isEmpty()) {
            HiLog.info(LABEL, "NotificationTransformer::createNotificationRequest hash code is null.", new Object[0]);
            return null;
        }
        String[] split = key.split(SPLIT_CHAR);
        if (split.length < 5) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String currentPkg2 = getCurrentPkg(aospContext2);
        sb.append(split[2]);
        sb.append("_");
        sb.append(split[1]);
        sb.append("_");
        sb.append(split[4]);
        sb.append("_");
        sb.append(currentPkg2);
        sb.append("_");
        sb.append(split[0]);
        sb.append("_");
        sb.append(split[3]);
        if (split.length == 6) {
            sb.append("_");
            sb.append(split[5]);
        }
        NotificationRequest notificationRequest = new NotificationRequest(statusBarNotification.getId(), sb.toString());
        notificationRequest.setCreatorBundleName(split[1]).setLittleIcon(createShellPixelMap).setOwnerBundleName(currentPkg2).setCreatorPid(statusBarNotification.getInitialPid()).setCreatorUid(statusBarNotification.getUid());
        return notificationRequest;
    }

    private void fillNotificationRequest(NotificationRequest notificationRequest, NotificationRequest.NotificationContent notificationContent, Notification notification, Bundle bundle) {
        notificationRequest.setContent(notificationContent);
        IntentConverter.convertBundleToIntentParams(bundle).ifPresent(new Consumer() {
            /* class ohos.event.notification.$$Lambda$7zIvz4jE1o8ySTxnUFGyzjtCxDc */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NotificationRequest.this.setAdditionalData((IntentParams) obj);
            }
        });
        fillLargeIcon(notificationRequest, notification);
        fillBasicNotificationRequest(notificationRequest, notification, bundle);
        convertActionToActionButton(notificationRequest, notification);
    }

    private void fillLargeIcon(NotificationRequest notificationRequest, Notification notification) {
        Bitmap copy;
        Optional<Bitmap> iconToBitmap = iconToBitmap(notification.getLargeIcon(), getAospContext());
        if (iconToBitmap.isPresent() && (copy = iconToBitmap.get().copy(Bitmap.Config.ARGB_8888, true)) != null) {
            notificationRequest.setBigIcon(ImageDoubleFwConverter.createShellPixelMap(copy));
        }
    }

    private void fillBasicNotificationRequest(NotificationRequest notificationRequest, Notification notification, Bundle bundle) {
        int i;
        boolean z;
        boolean z2 = (notification.flags & 16) == 16;
        boolean z3 = (notification.flags & 512) == 512;
        boolean z4 = (notification.flags & 8) == 8;
        boolean z5 = bundle.getBoolean("android.showChronometer");
        boolean z6 = bundle.getBoolean("android.chronometerCountDown");
        boolean z7 = (notification.flags & 2) == 2;
        int i2 = bundle.getInt("android.progress", 0);
        int i3 = bundle.getInt("android.progressMax", 0);
        boolean z8 = bundle.getBoolean("android.progressIndeterminate");
        String charSeqToString = charSeqToString(notification.getSettingsText());
        String charSeqToString2 = charSeqToString(notification.tickerText);
        boolean z9 = (notification.flags & 32) == 32;
        boolean z10 = (notification.flags & 256) == 256;
        CharSequence[] charSequenceArray = bundle.getCharSequenceArray("android.remoteInputHistory");
        if (charSequenceArray == null || charSequenceArray.length <= 0) {
            z = z10;
            i = i2;
        } else {
            z = z10;
            ArrayList arrayList = new ArrayList(charSequenceArray.length);
            int length = charSequenceArray.length;
            i = i2;
            int i4 = 0;
            while (i4 < length) {
                CharSequence charSequence = charSequenceArray[i4];
                if (charSequence instanceof String) {
                    arrayList.add((String) charSequence);
                }
                i4++;
                length = length;
                charSequenceArray = charSequenceArray;
            }
            notificationRequest.setNotificationUserInputHistory(arrayList);
        }
        notificationRequest.setCreateTime(notification.when).setDeliveryTime(notification.when).setAutoDeletedTime(notification.getTimeoutAfter()).setTapDismissed(z2).setSlotId(notification.getChannelId()).setSettingsText(charSeqToString).setGroupValue(notification.getGroup()).setGroupAlertType(notification.getGroupAlertBehavior()).setGroupOverview(z3).setClassification(notification.category).setColor(notification.color).setColorEnabled(notification.isColorized()).setBadgeNumber(notification.number).setAlertOneTime(z4).setShowStopwatch(z5).setCountdownTimer(z6).setInProgress(z7).setVisibleness(getZidaneLockscreenVisibility(notification.visibility)).setProgressBar(i, i3, z8).setShowCreateTime(notification.showsTime()).setStatusBarText(charSeqToString2).setUnremovable(z9).setOnlyLocal(z).setFloatingIcon(notification.isBubbleNotification()).setSortingKey(notification.getSortKey()).setPermitSystemGeneratedContextualActionButtons(notification.getAllowSystemGeneratedContextualActions());
        fillNotificationMessageUsers(notificationRequest, bundle.getParcelableArrayList("android.people.list"));
        fillNotificationRequestIntentAgent(notificationRequest, notification);
        if (notification.publicVersion != null) {
            fillPublicNotification(notificationRequest, notification.publicVersion);
        }
    }

    private void fillNotificationRequestIntentAgent(NotificationRequest notificationRequest, Notification notification) {
        if (notification.contentIntent != null) {
            notificationRequest.setIntentAgent(new IntentAgent(notification.contentIntent));
        }
        if (notification.deleteIntent != null) {
            notificationRequest.setRemovalIntentAgent(new IntentAgent(notification.deleteIntent));
        }
        if (notification.fullScreenIntent != null) {
            notificationRequest.setMaxScreenIntentAgent(new IntentAgent(notification.fullScreenIntent));
        }
    }

    private void fillNotificationMessageUsers(NotificationRequest notificationRequest, List<Person> list) {
        if (list != null) {
            for (Person person : list) {
                MessageUser transFromPerson = transFromPerson(person);
                if (transFromPerson != null) {
                    notificationRequest.addMessageUser(transFromPerson);
                }
            }
        }
    }

    private void convertActionToActionButton(NotificationRequest notificationRequest, Notification notification) {
        Bitmap copy;
        if (!(notification.actions == null || notification.actions.length == 0)) {
            Notification.Action[] actionArr = notification.actions;
            for (Notification.Action action : actionArr) {
                if (action != null) {
                    Optional<Bitmap> iconToBitmap = iconToBitmap(action.getIcon(), getAospContext());
                    IntentAgent intentAgent = null;
                    PixelMap createShellPixelMap = (!iconToBitmap.isPresent() || (copy = iconToBitmap.get().copy(Bitmap.Config.ARGB_8888, true)) == null) ? null : ImageDoubleFwConverter.createShellPixelMap(copy);
                    if (action.actionIntent != null) {
                        intentAgent = new IntentAgent(action.actionIntent);
                    }
                    NotificationActionButton.Builder semanticActionButton = new NotificationActionButton.Builder(createShellPixelMap, action.title.toString(), intentAgent).setContextDependent(action.isContextual()).setAutoCreatedReplies(action.getAllowGeneratedReplies()).setSemanticActionButton(getSemantic(action.getSemanticAction()));
                    semanticActionButton.addAdditionalData(PacMapUtils.convertFromBundle(action.getExtras()));
                    RemoteInput[] remoteInputs = action.getRemoteInputs();
                    if (remoteInputs != null) {
                        for (RemoteInput remoteInput : remoteInputs) {
                            if (remoteInput != null) {
                                convertRemoteInputToUserInput(remoteInput).ifPresent(new Consumer() {
                                    /* class ohos.event.notification.$$Lambda$ExePE1YreDGKOIIVhX6bUyHOQZM */

                                    @Override // java.util.function.Consumer
                                    public final void accept(Object obj) {
                                        NotificationActionButton.Builder.this.addNotificationUserInput((NotificationUserInput) obj);
                                    }
                                });
                            }
                        }
                    }
                    notificationRequest.addActionButton(semanticActionButton.build());
                }
            }
        }
    }

    private Optional<NotificationUserInput> convertRemoteInputToUserInput(RemoteInput remoteInput) {
        ArrayList arrayList;
        NotificationUserInput.Builder builder = new NotificationUserInput.Builder(remoteInput.getResultKey());
        CharSequence[] choices = remoteInput.getChoices();
        if (choices != null) {
            arrayList = new ArrayList();
            for (CharSequence charSequence : choices) {
                if (charSequence != null) {
                    arrayList.add(charSequence.toString());
                }
            }
        } else {
            arrayList = null;
        }
        builder.setOptions(arrayList).setTag(remoteInput.getLabel().toString()).setPermitFreeFormInput(remoteInput.getAllowFreeFormInput());
        Set<String> allowedDataTypes = remoteInput.getAllowedDataTypes();
        if (allowedDataTypes != null) {
            for (String str : allowedDataTypes) {
                if (str != null) {
                    builder.setPermitMimeTypes(str, true);
                }
            }
        }
        return Optional.of(builder.build());
    }

    private NotificationConstant.SemanticActionButton getSemantic(int i) {
        if (i == NotificationConstant.SemanticActionButton.NONE_ACTION_BUTTON.ordinal()) {
            return NotificationConstant.SemanticActionButton.NONE_ACTION_BUTTON;
        }
        if (i == NotificationConstant.SemanticActionButton.REPLY_ACTION_BUTTON.ordinal()) {
            return NotificationConstant.SemanticActionButton.REPLY_ACTION_BUTTON;
        }
        if (i == NotificationConstant.SemanticActionButton.READ_ACTION_BUTTON.ordinal()) {
            return NotificationConstant.SemanticActionButton.READ_ACTION_BUTTON;
        }
        if (i == NotificationConstant.SemanticActionButton.UNREAD_ACTION_BUTTON.ordinal()) {
            return NotificationConstant.SemanticActionButton.UNREAD_ACTION_BUTTON;
        }
        if (i == NotificationConstant.SemanticActionButton.DELETE_ACTION_BUTTON.ordinal()) {
            return NotificationConstant.SemanticActionButton.DELETE_ACTION_BUTTON;
        }
        if (i == NotificationConstant.SemanticActionButton.ARCHIVE_ACTION_BUTTON.ordinal()) {
            return NotificationConstant.SemanticActionButton.ARCHIVE_ACTION_BUTTON;
        }
        if (i == NotificationConstant.SemanticActionButton.MUTE_ACTION_BUTTON.ordinal()) {
            return NotificationConstant.SemanticActionButton.MUTE_ACTION_BUTTON;
        }
        if (i == NotificationConstant.SemanticActionButton.UNMUTE_ACTION_BUTTON.ordinal()) {
            return NotificationConstant.SemanticActionButton.UNMUTE_ACTION_BUTTON;
        }
        if (i == NotificationConstant.SemanticActionButton.THUMBS_UP_ACTION_BUTTON.ordinal()) {
            return NotificationConstant.SemanticActionButton.THUMBS_UP_ACTION_BUTTON;
        }
        if (i == NotificationConstant.SemanticActionButton.THUMBS_DOWN_ACTION_BUTTON.ordinal()) {
            return NotificationConstant.SemanticActionButton.THUMBS_DOWN_ACTION_BUTTON;
        }
        return NotificationConstant.SemanticActionButton.CALL_ACTION_BUTTON;
    }

    public Optional<NotificationChannel> getNotificationChannel(NotificationSlot notificationSlot) {
        if (notificationSlot == null) {
            return Optional.empty();
        }
        NotificationChannel notificationChannel = new NotificationChannel(notificationSlot.getId(), notificationSlot.getName(), getImportance(notificationSlot.getLevel()));
        notificationChannel.setDescription(notificationSlot.getDescription());
        notificationChannel.setShowBadge(notificationSlot.isShowBadge());
        notificationChannel.setBypassDnd(notificationSlot.isEnableBypassDnd());
        notificationChannel.setLockscreenVisibility(getLockscreenVisibility(notificationSlot.getLockscreenVisibleness()));
        notificationChannel.enableVibration(notificationSlot.canVibrate());
        notificationChannel.enableLights(notificationSlot.canEnableLight());
        notificationChannel.setLightColor(notificationSlot.getLedLightColor());
        notificationChannel.setGroup(notificationSlot.getSlotGroup());
        Uri sound = notificationSlot.getSound();
        if (sound != null) {
            try {
                android.net.Uri convertToAndroidUri = UriConverter.convertToAndroidUri(sound);
                if (convertToAndroidUri != null && !"".equals(convertToAndroidUri.toString())) {
                    notificationChannel.setSound(convertToAndroidUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
                }
            } catch (IllegalArgumentException | NullPointerException unused) {
                HiLog.warn(LABEL, "NotificationTransformer::getNotificationChannel sound is invalid", new Object[0]);
            }
        } else {
            notificationChannel.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        }
        return Optional.of(notificationChannel);
    }

    public Optional<NotificationSlot> getNotificationSlot(NotificationChannel notificationChannel) {
        if (notificationChannel == null) {
            return Optional.empty();
        }
        NotificationSlot notificationSlot = new NotificationSlot(notificationChannel.getId(), charSeqToString(notificationChannel.getName()), getZidaneImportance(notificationChannel.getImportance()));
        notificationSlot.setDescription(notificationChannel.getDescription());
        notificationSlot.enableBadge(notificationChannel.canShowBadge());
        notificationSlot.enableBypassDnd(notificationChannel.canBypassDnd());
        notificationSlot.setLockscreenVisibleness(getZidaneLockscreenVisibility(notificationChannel.getLockscreenVisibility()));
        notificationSlot.setEnableVibration(notificationChannel.shouldVibrate());
        notificationSlot.setEnableLight(notificationChannel.shouldShowLights());
        notificationSlot.setLedLightColor(notificationChannel.getLightColor());
        notificationSlot.setSlotGroup(notificationChannel.getGroup());
        try {
            Uri convertToZidaneUri = UriConverter.convertToZidaneUri(notificationChannel.getSound());
            if (convertToZidaneUri != null) {
                notificationSlot.setSound(convertToZidaneUri);
            }
        } catch (IllegalArgumentException | NullPointerException unused) {
            HiLog.warn(LABEL, "NotificationTransformer::getNotificationSlot sound is invalid", new Object[0]);
        }
        return Optional.of(notificationSlot);
    }

    public Optional<NotificationChannelGroup> getNotificationChannelGroup(NotificationSlotGroup notificationSlotGroup) {
        if (notificationSlotGroup == null) {
            return Optional.empty();
        }
        NotificationChannelGroup notificationChannelGroup = new NotificationChannelGroup(notificationSlotGroup.getId(), notificationSlotGroup.getName());
        notificationChannelGroup.setDescription(notificationSlotGroup.getDescription());
        return Optional.of(notificationChannelGroup);
    }

    public Optional<NotificationSlotGroup> getNotificationSlotGroup(NotificationChannelGroup notificationChannelGroup) {
        if (notificationChannelGroup == null) {
            return Optional.empty();
        }
        NotificationSlotGroup notificationSlotGroup = new NotificationSlotGroup(notificationChannelGroup.getId(), charSeqToString(notificationChannelGroup.getName()));
        notificationSlotGroup.setDescription(notificationChannelGroup.getDescription());
        notificationSlotGroup.setDisabled(notificationChannelGroup.isBlocked());
        List<NotificationChannel> channels = notificationChannelGroup.getChannels();
        if (channels == null || channels.isEmpty()) {
            return Optional.of(notificationSlotGroup);
        }
        ArrayList arrayList = new ArrayList();
        for (NotificationChannel notificationChannel : channels) {
            getNotificationSlot(notificationChannel).ifPresent(new Consumer(arrayList) {
                /* class ohos.event.notification.$$Lambda$LUDFAXRqkVHgeBTVTUtW48YqbBk */
                private final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.add((NotificationSlot) obj);
                }
            });
        }
        if (!arrayList.isEmpty()) {
            notificationSlotGroup.setSlots(arrayList);
        }
        return Optional.of(notificationSlotGroup);
    }

    private Optional<NotificationRequest.NotificationContent> getContent(Bundle bundle) {
        String str = "";
        String string = bundle.getString("android.template", str);
        if (string != null) {
            str = string;
        }
        if (str.isEmpty()) {
            return getNormalContent(bundle);
        }
        if (str.equals(Notification.BigTextStyle.class.getName())) {
            return getLongTextContent(bundle);
        }
        if (str.equals(Notification.BigPictureStyle.class.getName())) {
            return getPictureContent(bundle);
        }
        if (str.equals(Notification.MessagingStyle.class.getName())) {
            return getConversationalContent(bundle);
        }
        if (str.equals(Notification.InboxStyle.class.getName())) {
            return getMultiLineContent(bundle);
        }
        if (str.equals(Notification.MediaStyle.class.getName())) {
            return getMediaContent(bundle);
        }
        return getNormalContent(bundle);
    }

    private Optional<NotificationRequest.NotificationContent> getNormalContent(Bundle bundle) {
        NotificationRequest.NotificationNormalContent notificationNormalContent = new NotificationRequest.NotificationNormalContent();
        String string = bundle.getString("android.title", "");
        String string2 = bundle.getString("android.text", "");
        notificationNormalContent.setTitle(string).setText(string2).setAdditionalText(bundle.getString("android.subText", ""));
        return Optional.of(new NotificationRequest.NotificationContent(notificationNormalContent));
    }

    private Optional<NotificationRequest.NotificationContent> getLongTextContent(Bundle bundle) {
        String string = bundle.getString("android.title", "");
        String string2 = bundle.getString("android.text", "");
        String string3 = bundle.getString("android.subText", "");
        String string4 = bundle.getString("android.summaryText", "");
        String string5 = bundle.getString("android.bigText", "");
        String string6 = bundle.getString("android.title.big", "");
        NotificationRequest.NotificationLongTextContent notificationLongTextContent = new NotificationRequest.NotificationLongTextContent();
        notificationLongTextContent.setLongText(string5).setTitle(string).setText(string2).setAdditionalText(string3).setBriefText(string4).setExpandedTitle(string6);
        return Optional.of(new NotificationRequest.NotificationContent(notificationLongTextContent));
    }

    private Optional<NotificationRequest.NotificationContent> getPictureContent(Bundle bundle) {
        String string = bundle.getString("android.title", "");
        String string2 = bundle.getString("android.text", "");
        String string3 = bundle.getString("android.subText", "");
        String string4 = bundle.getString("android.summaryText", "");
        String string5 = bundle.getString("android.title.big", "");
        Optional<PixelMap> pixelMapFromAosp = getPixelMapFromAosp((Bitmap) bundle.getParcelable("android.picture"));
        NotificationRequest.NotificationPictureContent notificationPictureContent = new NotificationRequest.NotificationPictureContent();
        notificationPictureContent.setTitle(string).setText(string2).setAdditionalText(string3).setBriefText(string4).setExpandedTitle(string5).setBigPicture(pixelMapFromAosp.orElse(null));
        return Optional.of(new NotificationRequest.NotificationContent(notificationPictureContent));
    }

    private Optional<NotificationRequest.NotificationContent> getConversationalContent(Bundle bundle) {
        String string = bundle.getString("android.title", "");
        String string2 = bundle.getString("android.text", "");
        String string3 = bundle.getString("android.subText", "");
        boolean z = bundle.getBoolean("android.isGroupConversation");
        String charSeqToString = charSeqToString(bundle.getCharSequence("android.conversationTitle"));
        MessageUser transFromPerson = transFromPerson((Person) bundle.getParcelable("android.messagingUser"));
        if (transFromPerson == null) {
            HiLog.debug(LABEL, "NotificationTransformer::getConversationalContent get sender failed", new Object[0]);
            return Optional.empty();
        }
        NotificationRequest.NotificationConversationalContent notificationConversationalContent = new NotificationRequest.NotificationConversationalContent(transFromPerson);
        notificationConversationalContent.setTitle(string).setText(string2).setAdditionalText(string3).setConversationTitle(charSeqToString).setConversationGroup(z);
        List<Notification.MessagingStyle.Message> messagesFromBundles = getMessagesFromBundles(bundle.getParcelableArray("android.messages"));
        if (!messagesFromBundles.isEmpty()) {
            for (Notification.MessagingStyle.Message message : messagesFromBundles) {
                getsMessage(message).ifPresent(new Consumer() {
                    /* class ohos.event.notification.$$Lambda$FHPmYwHXWg7um3u5xXZBtMbdCQ */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        NotificationRequest.NotificationConversationalContent.this.addConversationalMessage((NotificationRequest.NotificationConversationalContent.ConversationalMessage) obj);
                    }
                });
            }
        }
        return Optional.of(new NotificationRequest.NotificationContent(notificationConversationalContent));
    }

    private Optional<NotificationRequest.NotificationContent> getMultiLineContent(Bundle bundle) {
        String string = bundle.getString("android.title", "");
        String string2 = bundle.getString("android.text", "");
        String string3 = bundle.getString("android.subText", "");
        String string4 = bundle.getString("android.summaryText", "");
        String string5 = bundle.getString("android.title.big", "");
        NotificationRequest.NotificationMultiLineContent notificationMultiLineContent = new NotificationRequest.NotificationMultiLineContent();
        notificationMultiLineContent.setTitle(string).setText(string2).setAdditionalText(string3).setBriefText(string4).setExpandedTitle(string5);
        CharSequence[] charSequenceArray = bundle.getCharSequenceArray("android.textLines");
        if (charSequenceArray != null && charSequenceArray.length > 0) {
            for (CharSequence charSequence : charSequenceArray) {
                notificationMultiLineContent.addSingleLine(charSeqToString(charSequence));
            }
        }
        return Optional.of(new NotificationRequest.NotificationContent(notificationMultiLineContent));
    }

    private Optional<NotificationRequest.NotificationContent> getMediaContent(Bundle bundle) {
        String string = bundle.getString("android.title", "");
        String string2 = bundle.getString("android.text", "");
        String string3 = bundle.getString("android.subText", "");
        int[] intArray = bundle.getIntArray("android.compactActions");
        NotificationRequest.NotificationMediaContent notificationMediaContent = new NotificationRequest.NotificationMediaContent();
        notificationMediaContent.setTitle(string).setText(string2).setAdditionalText(string3).setShownActions(intArray);
        MediaSession.Token token = (MediaSession.Token) bundle.getParcelable("android.mediaSession");
        if (token != null) {
            notificationMediaContent.setAVToken(new AVToken(token));
        }
        return Optional.of(new NotificationRequest.NotificationContent(notificationMediaContent));
    }

    private Optional<Notification> transformNormalContent(Context context, NotificationManager notificationManager, NotificationRequest notificationRequest, NotificationRequest.NotificationContent notificationContent, Bundle bundle) {
        Object notificationContent2 = notificationContent.getNotificationContent();
        if (!(notificationContent2 instanceof NotificationRequest.NotificationNormalContent)) {
            HiLog.debug(LABEL, "NotificationTransformer::transformNormalContent invalid NotificationNormalContent", new Object[0]);
            return Optional.empty();
        }
        Notification.Builder basicBuilder = getBasicBuilder(context, notificationManager, notificationRequest, (NotificationRequest.NotificationNormalContent) notificationContent2, bundle);
        if (basicBuilder != null) {
            return Optional.of(basicBuilder.build());
        }
        HiLog.debug(LABEL, "NotificationTransformer::transformNormalContent get builder failed", new Object[0]);
        return Optional.empty();
    }

    private Optional<Notification> transformLongTextContent(Context context, NotificationManager notificationManager, NotificationRequest notificationRequest, NotificationRequest.NotificationContent notificationContent, Bundle bundle) {
        Object notificationContent2 = notificationContent.getNotificationContent();
        if (!(notificationContent2 instanceof NotificationRequest.NotificationLongTextContent)) {
            HiLog.debug(LABEL, "NotificationTransformer::transformLongTextContent invalid NotificationLongTextContent", new Object[0]);
            return Optional.empty();
        }
        NotificationRequest.NotificationLongTextContent notificationLongTextContent = (NotificationRequest.NotificationLongTextContent) notificationContent2;
        Notification.Builder basicBuilder = getBasicBuilder(context, notificationManager, notificationRequest, notificationLongTextContent, bundle);
        if (basicBuilder == null) {
            HiLog.debug(LABEL, "NotificationTransformer::transformLongTextContent get builder failed", new Object[0]);
            return Optional.empty();
        }
        basicBuilder.setStyle(new Notification.BigTextStyle().bigText(notificationLongTextContent.getLongText()).setSummaryText(notificationLongTextContent.getBriefText()).setBigContentTitle(notificationLongTextContent.getExpandedTitle()));
        return Optional.of(basicBuilder.build());
    }

    private Optional<Notification> transformPictureContent(Context context, NotificationManager notificationManager, NotificationRequest notificationRequest, NotificationRequest.NotificationContent notificationContent, Bundle bundle) {
        Object notificationContent2 = notificationContent.getNotificationContent();
        if (!(notificationContent2 instanceof NotificationRequest.NotificationPictureContent)) {
            HiLog.debug(LABEL, "NotificationTransformer::transformPictureContent invalid NotificationPictureContent", new Object[0]);
            return Optional.empty();
        }
        NotificationRequest.NotificationPictureContent notificationPictureContent = (NotificationRequest.NotificationPictureContent) notificationContent2;
        Notification.Builder basicBuilder = getBasicBuilder(context, notificationManager, notificationRequest, notificationPictureContent, bundle);
        if (basicBuilder == null) {
            HiLog.debug(LABEL, "NotificationTransformer::transformPictureContent get builder failed", new Object[0]);
            return Optional.empty();
        }
        Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(notificationPictureContent.getBigPicture());
        if (createShadowBitmap == null) {
            Optional<Bitmap> defaultBitmap2 = getDefaultBitmap(context);
            if (!defaultBitmap2.isPresent()) {
                return Optional.empty();
            }
            createShadowBitmap = defaultBitmap2.get();
        }
        basicBuilder.setStyle(new Notification.BigPictureStyle().bigPicture(createShadowBitmap).setSummaryText(notificationPictureContent.getBriefText()).setBigContentTitle(notificationPictureContent.getExpandedTitle()));
        return Optional.of(basicBuilder.build());
    }

    private Optional<Notification> transformConversationalContent(Context context, NotificationManager notificationManager, NotificationRequest notificationRequest, NotificationRequest.NotificationContent notificationContent, Bundle bundle) {
        Object notificationContent2 = notificationContent.getNotificationContent();
        if (!(notificationContent2 instanceof NotificationRequest.NotificationConversationalContent)) {
            HiLog.debug(LABEL, "NotificationTransformer::transformConversationalContent invalid NotificationConversationalContent", new Object[0]);
            return Optional.empty();
        }
        NotificationRequest.NotificationConversationalContent notificationConversationalContent = (NotificationRequest.NotificationConversationalContent) notificationContent2;
        Notification.Builder basicBuilder = getBasicBuilder(context, notificationManager, notificationRequest, notificationConversationalContent, bundle);
        if (basicBuilder == null) {
            HiLog.debug(LABEL, "NotificationTransformer::transformConversationalContent get builder failed", new Object[0]);
            return Optional.empty();
        }
        Person transFromMessageUser = transFromMessageUser(notificationConversationalContent.getMessageUser());
        if (transFromMessageUser == null) {
            HiLog.debug(LABEL, "NotificationTransformer::transformConversationalContent get sender failed", new Object[0]);
            return Optional.empty();
        }
        Notification.MessagingStyle messagingStyle = new Notification.MessagingStyle(transFromMessageUser);
        List<NotificationRequest.NotificationConversationalContent.ConversationalMessage> allConversationalMessages = notificationConversationalContent.getAllConversationalMessages();
        if (allConversationalMessages != null && !allConversationalMessages.isEmpty()) {
            for (NotificationRequest.NotificationConversationalContent.ConversationalMessage conversationalMessage : allConversationalMessages) {
                transMessage(conversationalMessage).ifPresent(new Consumer(messagingStyle) {
                    /* class ohos.event.notification.$$Lambda$NotificationTransformer$pW8kFuR5jrSjVdbgVY6TZ0Hd0c */
                    private final /* synthetic */ Notification.MessagingStyle f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        Notification.MessagingStyle unused = this.f$0.addMessage((Notification.MessagingStyle.Message) obj);
                    }
                });
            }
        }
        basicBuilder.setStyle(messagingStyle.setConversationTitle(notificationConversationalContent.getConversationTitle()).setGroupConversation(notificationConversationalContent.isConversationGroup()));
        return Optional.of(basicBuilder.build());
    }

    private Optional<Notification> transformMultiLineContent(Context context, NotificationManager notificationManager, NotificationRequest notificationRequest, NotificationRequest.NotificationContent notificationContent, Bundle bundle) {
        Object notificationContent2 = notificationContent.getNotificationContent();
        if (!(notificationContent2 instanceof NotificationRequest.NotificationMultiLineContent)) {
            HiLog.debug(LABEL, "NotificationTransformer::transformMultiLineContent invalid NotificationMultiLineContent", new Object[0]);
            return Optional.empty();
        }
        NotificationRequest.NotificationMultiLineContent notificationMultiLineContent = (NotificationRequest.NotificationMultiLineContent) notificationContent2;
        Notification.Builder basicBuilder = getBasicBuilder(context, notificationManager, notificationRequest, notificationMultiLineContent, bundle);
        if (basicBuilder == null) {
            HiLog.debug(LABEL, "NotificationTransformer::transformMultiLineContent get builder failed", new Object[0]);
            return Optional.empty();
        }
        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
        List<String> allLines = notificationMultiLineContent.getAllLines();
        if (allLines != null && !allLines.isEmpty()) {
            for (String str : allLines) {
                inboxStyle.addLine(str);
            }
        }
        basicBuilder.setStyle(inboxStyle.setSummaryText(notificationMultiLineContent.getBriefText()).setBigContentTitle(notificationMultiLineContent.getExpandedTitle()));
        return Optional.of(basicBuilder.build());
    }

    private Optional<Notification> transformMediaContent(Context context, NotificationManager notificationManager, NotificationRequest notificationRequest, NotificationRequest.NotificationContent notificationContent, Bundle bundle) {
        Object notificationContent2 = notificationContent.getNotificationContent();
        if (!(notificationContent2 instanceof NotificationRequest.NotificationMediaContent)) {
            HiLog.debug(LABEL, "NotificationTransformer::transformMediaContent invalid NotificationMediaContent", new Object[0]);
            return Optional.empty();
        }
        NotificationRequest.NotificationMediaContent notificationMediaContent = (NotificationRequest.NotificationMediaContent) notificationContent2;
        Notification.Builder basicBuilder = getBasicBuilder(context, notificationManager, notificationRequest, notificationMediaContent, bundle);
        if (basicBuilder == null) {
            HiLog.debug(LABEL, "NotificationTransformer::transformMediaContent get builder failed", new Object[0]);
            return Optional.empty();
        }
        Notification.MediaStyle mediaStyle = new Notification.MediaStyle();
        AVToken aVToken = notificationMediaContent.getAVToken();
        if (aVToken != null) {
            Object hostAVToken = aVToken.getHostAVToken();
            if (hostAVToken instanceof MediaSession.Token) {
                mediaStyle.setMediaSession((MediaSession.Token) hostAVToken);
            }
        }
        basicBuilder.setStyle(mediaStyle.setShowActionsInCompactView(notificationMediaContent.getShownActions()));
        return Optional.of(basicBuilder.build());
    }

    private Notification.Builder getBasicBuilder(Context context, NotificationManager notificationManager, NotificationRequest notificationRequest, NotificationRequest.NotificationBasicContent notificationBasicContent, Bundle bundle) {
        ArrayList arrayList;
        Optional<Icon> icon = getIcon(context, notificationRequest.getLittleIcon());
        Bundle bundle2 = null;
        if (!icon.isPresent()) {
            HiLog.debug(LABEL, "NotificationTransformer::getBasicBuilder get app icon failed", new Object[0]);
            return null;
        }
        Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(notificationRequest.getBigIcon());
        String slotId = notificationRequest.getSlotId();
        if (slotId == null || slotId.isEmpty()) {
            createDefaultChannel(notificationManager);
            slotId = DEFAULT_CHANNEL_ID;
        }
        Notification.Builder builder = new Notification.Builder(context, slotId);
        IntentParams additionalData = notificationRequest.getAdditionalData();
        if (additionalData != null) {
            arrayList = new ArrayList();
            Object param = additionalData.getParam(NotificationRequest.EXTRA_USER_INPUT_HISTORY);
            if (param instanceof ArrayList) {
                for (Object obj : (List) param) {
                    if (obj instanceof String) {
                        arrayList.add((String) obj);
                    }
                }
            }
            additionalData.remove(NotificationRequest.EXTRA_USER_INPUT_HISTORY);
            Optional<Bundle> convertIntentParamsToBundle = IntentConverter.convertIntentParamsToBundle(additionalData.getParams());
            if (convertIntentParamsToBundle.isPresent()) {
                bundle2 = convertIntentParamsToBundle.get();
            }
        } else {
            arrayList = null;
        }
        if (arrayList != null) {
            builder.setRemoteInputHistory((CharSequence[]) arrayList.toArray(new CharSequence[0]));
        }
        builder.setSmallIcon(icon.get()).setContentTitle(notificationBasicContent.getTitle()).setContentText(notificationBasicContent.getText()).setExtras(bundle2).addExtras(bundle).setSubText(notificationBasicContent.getAdditionalText()).setLargeIcon(createShadowBitmap).setFlag(32, notificationRequest.isUnremovable()).setAllowSystemGeneratedContextualActions(notificationRequest.isPermitSystemGeneratedContextualActionButtons());
        fillIntentAgent(builder, notificationRequest);
        fillCustomView(builder, notificationRequest);
        fillBasicBuilder(builder, notificationRequest);
        fillPublicVersion(context, notificationManager, builder, notificationRequest);
        transformActionButton(builder, notificationRequest);
        return builder;
    }

    private void fillIntentAgent(Notification.Builder builder, NotificationRequest notificationRequest) {
        PendingIntent pendingIntent = IntentAgentAdapterUtils.getPendingIntent(notificationRequest.getIntentAgent());
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        PendingIntent pendingIntent2 = IntentAgentAdapterUtils.getPendingIntent(notificationRequest.getRemovalIntentAgent());
        if (pendingIntent2 != null) {
            builder.setDeleteIntent(pendingIntent2);
        }
        PendingIntent pendingIntent3 = IntentAgentAdapterUtils.getPendingIntent(notificationRequest.getMaxScreenIntentAgent());
        if (pendingIntent3 != null) {
            builder.setFullScreenIntent(pendingIntent3, true);
        }
    }

    private void fillCustomView(Notification.Builder builder, NotificationRequest notificationRequest) {
        ohos.app.Context context = notificationRequest.getContext();
        if (context != null) {
            RemoteViews aRemoteViews = getARemoteViews(context, notificationRequest.getCustomView());
            if (aRemoteViews != null) {
                builder.setCustomContentView(aRemoteViews);
            }
            RemoteViews aRemoteViews2 = getARemoteViews(context, notificationRequest.getCustomBigView());
            if (aRemoteViews2 != null) {
                builder.setCustomBigContentView(aRemoteViews2);
            }
            RemoteViews aRemoteViews3 = getARemoteViews(context, notificationRequest.getCustomFloatView());
            if (aRemoteViews3 != null) {
                builder.setCustomHeadsUpContentView(aRemoteViews3);
            }
        }
    }

    private RemoteViews getARemoteViews(ohos.app.Context context, ComponentProvider componentProvider) {
        if (componentProvider != null) {
            return new RemoteViewUtils(context).getARemoteViews(componentProvider).orElse(null);
        }
        return null;
    }

    private void fillBasicBuilder(Notification.Builder builder, NotificationRequest notificationRequest) {
        builder.setAutoCancel(notificationRequest.isTapDismissed()).setWhen(notificationRequest.getDeliveryTime()).setTimeoutAfter(notificationRequest.getAutoDeletedTime()).setSettingsText(notificationRequest.getSettingsText()).setGroup(notificationRequest.getGroupValue()).setGroupAlertBehavior(notificationRequest.getGroupAlertType()).setGroupSummary(notificationRequest.isGroupOverview()).setCategory(notificationRequest.getClassification()).setColor(notificationRequest.getColor()).setColorized(notificationRequest.isColorEnabled()).setNumber(notificationRequest.getBadgeNumber()).setOnlyAlertOnce(notificationRequest.isAlertOneTime()).setUsesChronometer(notificationRequest.isShowStopwatch()).setChronometerCountDown(notificationRequest.isCountdownTimer()).setOngoing(notificationRequest.isInProgress()).setShowWhen(notificationRequest.isShowCreateTime()).setVisibility(getLockscreenVisibility(notificationRequest.getVisibleness())).setProgress(notificationRequest.getProgressMax(), notificationRequest.getProgressValue(), notificationRequest.isProgressIndeterminate()).setTicker(notificationRequest.getStatusBarText()).setLocalOnly(notificationRequest.isOnlyLocal()).setSortKey(notificationRequest.getSortingKey());
        for (MessageUser messageUser : notificationRequest.getMessageUsers()) {
            Person transFromMessageUser = transFromMessageUser(messageUser);
            if (transFromMessageUser != null) {
                builder.addPerson(transFromMessageUser);
            }
        }
    }

    private void fillPublicVersion(Context context, NotificationManager notificationManager, Notification.Builder builder, NotificationRequest notificationRequest) {
        NotificationRequest publicNotification = notificationRequest.getPublicNotification();
        if (publicNotification != null) {
            Optional<Notification> transform = transform(context, notificationManager, publicNotification);
            Objects.requireNonNull(builder);
            transform.ifPresent(new Consumer(builder) {
                /* class ohos.event.notification.$$Lambda$NotificationTransformer$Zy4cz90KBmprAQ6itQ7gD3EeQQ */
                private final /* synthetic */ Notification.Builder f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Notification.Builder unused = this.f$0.setPublicVersion((Notification) obj);
                }
            });
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x004f  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0008 A[SYNTHETIC] */
    private void transformActionButton(Notification.Builder builder, NotificationRequest notificationRequest) {
        PendingIntent pendingIntent;
        List<NotificationUserInput> userInputs;
        for (NotificationActionButton notificationActionButton : notificationRequest.getActionButtons()) {
            if (notificationActionButton != null) {
                Optional<Icon> iconByPixelMap = getIconByPixelMap(notificationActionButton.getIcon());
                Notification.Action action = null;
                Icon icon = iconByPixelMap.isPresent() ? iconByPixelMap.get() : null;
                IntentAgent intentAgent = notificationActionButton.getIntentAgent();
                if (intentAgent != null) {
                    Object object = intentAgent.getObject();
                    if (object instanceof PendingIntent) {
                        pendingIntent = (PendingIntent) object;
                        Notification.Action.Builder builder2 = new Notification.Action.Builder(icon, notificationActionButton.getTitle(), pendingIntent);
                        userInputs = notificationActionButton.getUserInputs();
                        if (userInputs != null) {
                            for (NotificationUserInput notificationUserInput : userInputs) {
                                if (notificationUserInput != null) {
                                    convertUserInputToRemoteInput(notificationUserInput).ifPresent(new Consumer(builder2) {
                                        /* class ohos.event.notification.$$Lambda$NotificationTransformer$UnEJiQu2oClhwe5Doh2NVi0q1JE */
                                        private final /* synthetic */ Notification.Action.Builder f$0;

                                        {
                                            this.f$0 = r1;
                                        }

                                        @Override // java.util.function.Consumer
                                        public final void accept(Object obj) {
                                            Notification.Action.Builder unused = this.f$0.addRemoteInput((RemoteInput) obj);
                                        }
                                    });
                                }
                            }
                        }
                        builder2.addExtras(PacMapUtils.convertIntoBundle(notificationActionButton.getAdditionalData())).setContextual(notificationActionButton.isContextDependent()).setSemanticAction(notificationActionButton.getSemanticActionButton()).setAllowGeneratedReplies(notificationActionButton.isAutoCreatedReplies());
                        action = builder2.build();
                        if (action == null) {
                            builder.addAction(builder2.build());
                        }
                    }
                }
                pendingIntent = null;
                Notification.Action.Builder builder22 = new Notification.Action.Builder(icon, notificationActionButton.getTitle(), pendingIntent);
                userInputs = notificationActionButton.getUserInputs();
                if (userInputs != null) {
                }
                builder22.addExtras(PacMapUtils.convertIntoBundle(notificationActionButton.getAdditionalData())).setContextual(notificationActionButton.isContextDependent()).setSemanticAction(notificationActionButton.getSemanticActionButton()).setAllowGeneratedReplies(notificationActionButton.isAutoCreatedReplies());
                try {
                    action = builder22.build();
                } catch (NullPointerException unused) {
                }
                if (action == null) {
                }
            }
        }
    }

    private Optional<RemoteInput> convertUserInputToRemoteInput(NotificationUserInput notificationUserInput) {
        String[] strArr;
        List<String> options = notificationUserInput.getOptions();
        if (options != null) {
            strArr = new String[options.size()];
            options.toArray(strArr);
        } else {
            strArr = null;
        }
        RemoteInput.Builder allowFreeFormInput = new RemoteInput.Builder(notificationUserInput.getInputKey()).setLabel(notificationUserInput.getTag()).setChoices(strArr).setAllowFreeFormInput(notificationUserInput.isPermitFreeFormInput());
        for (String str : notificationUserInput.getPermitMimeTypes()) {
            allowFreeFormInput.setAllowDataType(str, true);
        }
        return Optional.of(allowFreeFormInput.build());
    }

    private Optional<Icon> getIcon(Context context, PixelMap pixelMap) {
        Optional<Icon> iconByPixelMap = getIconByPixelMap(pixelMap);
        if (iconByPixelMap.isPresent()) {
            return iconByPixelMap;
        }
        Icon icon = this.appIcon;
        if (icon != null) {
            return Optional.of(icon);
        }
        Optional<Bitmap> defaultBitmap2 = getDefaultBitmap(context);
        if (!defaultBitmap2.isPresent()) {
            return Optional.empty();
        }
        this.appIcon = Icon.createWithBitmap(defaultBitmap2.get());
        Icon icon2 = this.appIcon;
        if (icon2 == null) {
            return Optional.empty();
        }
        return Optional.of(icon2);
    }

    private Optional<Bitmap> getDefaultBitmap(Context context) {
        Bitmap bitmap;
        Bitmap bitmap2 = this.defaultBitmap;
        if (bitmap2 != null) {
            return Optional.of(bitmap2);
        }
        Context applicationContext = context.getApplicationContext();
        if (applicationContext == null) {
            return Optional.empty();
        }
        PackageManager packageManager = applicationContext.getPackageManager();
        if (packageManager == null) {
            return Optional.empty();
        }
        try {
            Drawable applicationIcon = packageManager.getApplicationIcon(packageManager.getApplicationInfo(context.getPackageName(), 0));
            if (applicationIcon == null) {
                return Optional.empty();
            }
            int intrinsicWidth = applicationIcon.getIntrinsicWidth();
            int intrinsicHeight = applicationIcon.getIntrinsicHeight();
            if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
            }
            Canvas canvas = new Canvas(bitmap);
            applicationIcon.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
            applicationIcon.draw(canvas);
            this.defaultBitmap = bitmap;
            return Optional.of(this.defaultBitmap);
        } catch (PackageManager.NameNotFoundException unused) {
            return Optional.empty();
        }
    }

    private Optional<Icon> getIconByPixelMap(PixelMap pixelMap) {
        if (pixelMap == null) {
            return Optional.empty();
        }
        Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(pixelMap);
        if (createShadowBitmap == null) {
            return Optional.empty();
        }
        Icon createWithBitmap = Icon.createWithBitmap(createShadowBitmap);
        if (createWithBitmap == null) {
            return Optional.empty();
        }
        return Optional.of(createWithBitmap);
    }

    private void createDefaultChannel(NotificationManager notificationManager) {
        if (notificationManager != null && this.defaultChannel == null && Build.VERSION.SDK_INT >= 26) {
            this.defaultChannel = new NotificationChannel(DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, 3);
            notificationManager.createNotificationChannel(this.defaultChannel);
        }
    }

    private String charSeqToString(CharSequence charSequence) {
        return charSequence != null ? charSequence.toString() : "";
    }

    private Context getAospContext() {
        Application currentApplication;
        if (this.aospContext == null && (currentApplication = ActivityThread.currentApplication()) != null) {
            this.aospContext = currentApplication.getApplicationContext();
        }
        return this.aospContext;
    }

    private String getCurrentPkg(Context context) {
        if (this.currentPkg == null && context != null) {
            this.currentPkg = context.getPackageName();
        }
        return this.currentPkg;
    }

    private static Optional<Bitmap> iconToBitmap(Icon icon, Context context) {
        Bitmap bitmap;
        if (icon == null || context == null) {
            return Optional.empty();
        }
        Drawable loadDrawable = icon.loadDrawable(context);
        if (loadDrawable == null) {
            return Optional.empty();
        }
        if (loadDrawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) loadDrawable;
            if (bitmapDrawable.getBitmap() != null) {
                return Optional.of(bitmapDrawable.getBitmap());
            }
        }
        int intrinsicWidth = loadDrawable.getIntrinsicWidth();
        int intrinsicHeight = loadDrawable.getIntrinsicHeight();
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        loadDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        loadDrawable.draw(canvas);
        return Optional.of(bitmap);
    }

    private static Optional<PixelMap> getPixelMapFromAosp(Bitmap bitmap) {
        if (bitmap == null) {
            return Optional.empty();
        }
        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        if (copy == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ImageDoubleFwConverter.createShellPixelMap(copy));
    }

    private Optional<Notification.MessagingStyle.Message> transMessage(NotificationRequest.NotificationConversationalContent.ConversationalMessage conversationalMessage) {
        if (conversationalMessage == null) {
            HiLog.debug(LABEL, "NotificationTransformer::transMessage param is invalid", new Object[0]);
            return Optional.empty();
        }
        String text = conversationalMessage.getText();
        if (text == null) {
            return Optional.empty();
        }
        Notification.MessagingStyle.Message message = new Notification.MessagingStyle.Message(text, conversationalMessage.getArrivedTime(), transFromMessageUser(conversationalMessage.getSender()));
        try {
            android.net.Uri convertToAndroidUri = UriConverter.convertToAndroidUri(conversationalMessage.getUri());
            if (convertToAndroidUri != null) {
                message.setData(conversationalMessage.getMimeType(), convertToAndroidUri);
            }
        } catch (IllegalArgumentException | NullPointerException unused) {
            HiLog.warn(LABEL, "NotificationTransformer::transMessage uri is invalid", new Object[0]);
        }
        return Optional.of(message);
    }

    private Person transFromMessageUser(MessageUser messageUser) {
        if (messageUser == null) {
            HiLog.debug(LABEL, "NotificationTransformer::transFromMessageUser param is invalid", new Object[0]);
            return null;
        }
        Person.Builder builder = new Person.Builder();
        builder.setImportant(messageUser.isUserImportant()).setName(messageUser.getName()).setUri(messageUser.getUri()).setKey(messageUser.getKey()).setBot(messageUser.isMachine());
        getIconByPixelMap(messageUser.getPixelMap()).ifPresent(new Consumer(builder) {
            /* class ohos.event.notification.$$Lambda$NotificationTransformer$j41lznhIK2yH7BFjdaw7atyxViQ */
            private final /* synthetic */ Person.Builder f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                Person.Builder unused = this.f$0.setIcon((Icon) obj);
            }
        });
        return builder.build();
    }

    private Optional<NotificationRequest.NotificationConversationalContent.ConversationalMessage> getsMessage(Notification.MessagingStyle.Message message) {
        if (message == null) {
            HiLog.debug(LABEL, "NotificationTransformer::getsMessage param is invalid", new Object[0]);
            return Optional.empty();
        }
        CharSequence text = message.getText();
        if (text == null) {
            return Optional.empty();
        }
        NotificationRequest.NotificationConversationalContent.ConversationalMessage conversationalMessage = new NotificationRequest.NotificationConversationalContent.ConversationalMessage(charSeqToString(text), message.getTimestamp(), transFromPerson(message.getSenderPerson()));
        try {
            Uri convertToZidaneUri = UriConverter.convertToZidaneUri(message.getDataUri());
            if (convertToZidaneUri != null) {
                conversationalMessage.setData(message.getDataMimeType(), convertToZidaneUri);
            }
        } catch (IllegalArgumentException | NullPointerException unused) {
            HiLog.warn(LABEL, "NotificationTransformer::getsMessage uri is invalid", new Object[0]);
        }
        return Optional.of(conversationalMessage);
    }

    private MessageUser transFromPerson(Person person) {
        Bitmap copy;
        if (person == null) {
            HiLog.debug(LABEL, "NotificationTransformer::transFromPerson param is invalid", new Object[0]);
            return null;
        }
        MessageUser messageUser = new MessageUser();
        messageUser.setName(charSeqToString(person.getName())).setUri(person.getUri()).setKey(person.getKey()).setUserAsImportant(person.isImportant()).setMachine(person.isBot());
        Optional<Bitmap> iconToBitmap = iconToBitmap(person.getIcon(), getAospContext());
        if (iconToBitmap.isPresent() && (copy = iconToBitmap.get().copy(Bitmap.Config.ARGB_8888, true)) != null) {
            messageUser.setPixelMap(ImageDoubleFwConverter.createShellPixelMap(copy));
        }
        return messageUser;
    }

    private static List<Notification.MessagingStyle.Message> getMessagesFromBundles(Parcelable[] parcelableArr) {
        Notification.MessagingStyle.Message messageFromBundle;
        if (parcelableArr == null) {
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList();
        for (Parcelable parcelable : parcelableArr) {
            if ((parcelable instanceof Bundle) && (messageFromBundle = getMessageFromBundle((Bundle) parcelable)) != null) {
                arrayList.add(messageFromBundle);
            }
        }
        return arrayList;
    }

    private static Notification.MessagingStyle.Message getMessageFromBundle(Bundle bundle) {
        CharSequence charSequence;
        try {
            if (bundle.containsKey("text") && bundle.getCharSequence("text") != null) {
                Person person = (Person) bundle.getParcelable(BUNDLE_KEY_SENDER_PERSON);
                if (person == null && (charSequence = bundle.getCharSequence(BUNDLE_KEY_SENDER)) != null) {
                    person = new Person.Builder().setName(charSequence).build();
                }
                Notification.MessagingStyle.Message message = new Notification.MessagingStyle.Message(bundle.getCharSequence("text"), bundle.getLong("time"), person);
                if (bundle.containsKey("type") && bundle.containsKey("uri") && (bundle.getParcelable("uri") instanceof android.net.Uri)) {
                    message.setData(bundle.getString("type"), (android.net.Uri) bundle.getParcelable("uri"));
                }
                return message;
            }
        } catch (ClassCastException unused) {
        }
        return null;
    }
}
