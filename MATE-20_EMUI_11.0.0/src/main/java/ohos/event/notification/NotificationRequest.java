package ohos.event.notification;

import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.content.IntentParams;
import ohos.agp.components.ComponentProvider;
import ohos.app.Context;
import ohos.event.EventConstant;
import ohos.event.intentagent.IntentAgent;
import ohos.event.notification.NotificationActionButton;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.common.sessioncore.AVToken;
import ohos.media.image.PixelMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public final class NotificationRequest implements Sequenceable {
    public static final String CLASSIFICATION_ALARM = "alarm";
    public static final String CLASSIFICATION_CALL = "call";
    public static final String CLASSIFICATION_EMAIL = "email";
    public static final String CLASSIFICATION_ERROR = "err";
    public static final String CLASSIFICATION_EVENT = "event";
    public static final String CLASSIFICATION_MESSAGE = "msg";
    public static final String CLASSIFICATION_NAVIGATION = "navigation";
    public static final String CLASSIFICATION_PROGRESS = "progress";
    public static final String CLASSIFICATION_PROMO = "promo";
    public static final String CLASSIFICATION_RECOMMENDATION = "recommendation";
    public static final String CLASSIFICATION_REMINDER = "reminder";
    public static final String CLASSIFICATION_SERVICE = "service";
    public static final String CLASSIFICATION_SOCIAL = "social";
    public static final String CLASSIFICATION_STATUS = "status";
    public static final String CLASSIFICATION_SYSTEM = "sys";
    public static final String CLASSIFICATION_TRANSPORT = "transport";
    public static final int COLOR_DEFAULT = 0;
    public static final String EXTRA_USER_INPUT_HISTORY = "harmony_user_input_history";
    public static final int GROUP_ALERT_TYPE_ALL = 0;
    public static final int GROUP_ALERT_TYPE_CHILD = 2;
    public static final int GROUP_ALERT_TYPE_OVERVIEW = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final int MAX_ACTION_BUTTONS = 3;
    private static final int MAX_REPLY_HISTORY = 5;
    public static final Sequenceable.Producer<NotificationRequest> PRODUCER = $$Lambda$NotificationRequest$CZXDOPUqfPudAhbmxs9EVRcibXg.INSTANCE;
    private static final String TAG = "NotificationRequest";
    public static final int VISIBLENESS_TYPE_NO_OVERRIDE = -1000;
    public static final int VISIBLENESS_TYPE_PRIVATE = 2;
    public static final int VISIBLENESS_TYPE_PUBLIC = 1;
    public static final int VISIBLENESS_TYPE_SECRET = 3;
    private List<NotificationActionButton> actionButtons;
    private IntentAgent agent;
    private long autoDeletedTime;
    private PixelMap bigIcon;
    private String classification;
    private int color;
    private boolean colorEnabled;
    private NotificationContent content;
    private Context context;
    private long createTime;
    private String creatorBundleName;
    private int creatorPid;
    private int creatorUid;
    private ComponentProvider customBigView;
    private ComponentProvider customFloatView;
    private ComponentProvider customView;
    private long deliveryTime;
    private IntentParams extras;
    private int groupAlertType;
    private boolean groupOverview;
    private String groupValue;
    private boolean isAlertOnce;
    private boolean isCountDown;
    private boolean isFloatingIcon;
    private boolean isIndeterminate;
    private boolean isOngoing;
    private boolean isStopwatch;
    private boolean isUnremovable;
    private PixelMap littleIcon;
    private IntentAgent maxScreenIntentAgent;
    private List<MessageUser> messageUsers;
    private String notificationHashCode;
    private int notificationId;
    private int number;
    private boolean onlyLocal;
    private String ownerBundleName;
    private boolean permitSystemGeneratedContextualActionButtons;
    private int progressMaxValue;
    private int progressValue;
    private NotificationRequest publicNotification;
    private IntentAgent removalIntentAgent;
    private String settingsText;
    private boolean showCreateTime;
    private String slotId;
    private String sortingKey;
    private String statusBarText;
    private boolean tapDismissed;
    private int visibleness;

    static /* synthetic */ NotificationRequest lambda$static$0(Parcel parcel) {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.unmarshalling(parcel);
        return notificationRequest;
    }

    public NotificationRequest(NotificationRequest notificationRequest) {
        this.groupAlertType = 0;
        this.color = 0;
        this.number = 0;
        this.visibleness = -1000;
        this.actionButtons = new ArrayList(3);
        this.permitSystemGeneratedContextualActionButtons = true;
        this.messageUsers = new ArrayList();
        copyFrom(notificationRequest);
    }

    public NotificationRequest() {
        this(0);
    }

    public NotificationRequest(int i) {
        this((Context) null, i);
    }

    public NotificationRequest(Context context2, int i) {
        this.groupAlertType = 0;
        this.color = 0;
        this.number = 0;
        this.visibleness = -1000;
        this.actionButtons = new ArrayList(3);
        this.permitSystemGeneratedContextualActionButtons = true;
        this.messageUsers = new ArrayList();
        this.context = context2;
        this.notificationId = i;
        this.createTime = System.currentTimeMillis();
        this.deliveryTime = System.currentTimeMillis();
        this.notificationHashCode = generateNotificationHashCode();
    }

    NotificationRequest(int i, String str) {
        this.groupAlertType = 0;
        this.color = 0;
        this.number = 0;
        this.visibleness = -1000;
        this.actionButtons = new ArrayList(3);
        this.permitSystemGeneratedContextualActionButtons = true;
        this.messageUsers = new ArrayList();
        this.notificationId = i;
        this.notificationHashCode = str;
    }

    /* access modifiers changed from: package-private */
    public Context getContext() {
        return this.context;
    }

    public NotificationRequest setCustomView(ComponentProvider componentProvider) {
        this.customView = componentProvider;
        return this;
    }

    public ComponentProvider getCustomView() {
        return this.customView;
    }

    public NotificationRequest setCustomBigView(ComponentProvider componentProvider) {
        this.customBigView = componentProvider;
        return this;
    }

    public ComponentProvider getCustomBigView() {
        return this.customBigView;
    }

    public NotificationRequest setCustomFloatView(ComponentProvider componentProvider) {
        this.customFloatView = componentProvider;
        return this;
    }

    public ComponentProvider getCustomFloatView() {
        return this.customFloatView;
    }

    public NotificationRequest setOnlyLocal(boolean z) {
        this.onlyLocal = z;
        return this;
    }

    public boolean isOnlyLocal() {
        return this.onlyLocal;
    }

    public NotificationRequest setSortingKey(String str) {
        this.sortingKey = str;
        return this;
    }

    public String getSortingKey() {
        return this.sortingKey;
    }

    public long getAutoDeletedTime() {
        return this.autoDeletedTime;
    }

    public String getNotificationHashCode() {
        return this.notificationHashCode;
    }

    public NotificationRequest setLittleIcon(PixelMap pixelMap) {
        this.littleIcon = pixelMap;
        return this;
    }

    public PixelMap getLittleIcon() {
        return this.littleIcon;
    }

    public NotificationRequest setBigIcon(PixelMap pixelMap) {
        this.bigIcon = pixelMap;
        return this;
    }

    public PixelMap getBigIcon() {
        return this.bigIcon;
    }

    public NotificationRequest setAutoDeletedTime(long j) {
        this.autoDeletedTime = j;
        return this;
    }

    public boolean isTapDismissed() {
        return this.tapDismissed;
    }

    public NotificationRequest setTapDismissed(boolean z) {
        this.tapDismissed = z;
        return this;
    }

    public long getCreateTime() {
        return this.createTime;
    }

    public NotificationRequest setCreateTime(long j) {
        this.createTime = j;
        return this;
    }

    public boolean isShowCreateTime() {
        return this.showCreateTime;
    }

    public NotificationRequest setShowCreateTime(boolean z) {
        this.showCreateTime = z;
        return this;
    }

    public long getDeliveryTime() {
        return this.deliveryTime;
    }

    public NotificationRequest setDeliveryTime(long j) {
        this.deliveryTime = j;
        return this;
    }

    public IntentAgent getIntentAgent() {
        return this.agent;
    }

    public NotificationRequest setIntentAgent(IntentAgent intentAgent) {
        this.agent = intentAgent;
        return this;
    }

    public int getNotificationId() {
        return this.notificationId;
    }

    public String getSlotId() {
        return this.slotId;
    }

    public NotificationRequest setNotificationId(int i) {
        this.notificationId = i;
        return this;
    }

    public NotificationRequest setSlotId(String str) {
        this.slotId = str;
        return this;
    }

    public NotificationContent getContent() {
        return this.content;
    }

    public NotificationRequest setContent(NotificationContent notificationContent) {
        this.content = notificationContent;
        return this;
    }

    public NotificationRequest setAdditionalData(IntentParams intentParams) {
        this.extras = intentParams;
        return this;
    }

    public IntentParams getAdditionalData() {
        return this.extras;
    }

    public NotificationRequest setSettingsText(String str) {
        this.settingsText = str;
        return this;
    }

    public String getSettingsText() {
        return this.settingsText;
    }

    public int getNotificationType() {
        NotificationContent notificationContent = this.content;
        if (notificationContent != null) {
            return notificationContent.getContentType();
        }
        return 0;
    }

    public String getCreatorBundleName() {
        return this.creatorBundleName;
    }

    /* access modifiers changed from: package-private */
    public NotificationRequest setCreatorBundleName(String str) {
        this.creatorBundleName = str;
        return this;
    }

    public String getOwnerBundleName() {
        return this.ownerBundleName;
    }

    /* access modifiers changed from: package-private */
    public NotificationRequest setOwnerBundleName(String str) {
        this.ownerBundleName = str;
        return this;
    }

    public int getCreatorPid() {
        return this.creatorPid;
    }

    /* access modifiers changed from: package-private */
    public NotificationRequest setCreatorPid(int i) {
        this.creatorPid = i;
        return this;
    }

    public int getCreatorUid() {
        return this.creatorUid;
    }

    /* access modifiers changed from: package-private */
    public NotificationRequest setCreatorUid(int i) {
        this.creatorUid = i;
        return this;
    }

    public String getGroupValue() {
        return this.groupValue;
    }

    public NotificationRequest setGroupValue(String str) {
        this.groupValue = str;
        return this;
    }

    public int getGroupAlertType() {
        return this.groupAlertType;
    }

    public NotificationRequest setGroupAlertType(int i) {
        this.groupAlertType = i;
        return this;
    }

    public boolean isGroupOverview() {
        return this.groupOverview;
    }

    public NotificationRequest setGroupOverview(boolean z) {
        this.groupOverview = z;
        return this;
    }

    public IntentAgent getRemovalIntentAgent() {
        return this.removalIntentAgent;
    }

    public NotificationRequest setRemovalIntentAgent(IntentAgent intentAgent) {
        this.removalIntentAgent = intentAgent;
        return this;
    }

    public String getClassification() {
        return this.classification;
    }

    public NotificationRequest setClassification(String str) {
        this.classification = str;
        return this;
    }

    public IntentAgent getMaxScreenIntentAgent() {
        return this.maxScreenIntentAgent;
    }

    public NotificationRequest setMaxScreenIntentAgent(IntentAgent intentAgent) {
        this.maxScreenIntentAgent = intentAgent;
        return this;
    }

    public int getColor() {
        return this.color;
    }

    public NotificationRequest setColor(int i) {
        this.color = i;
        return this;
    }

    public boolean isColorEnabled() {
        return this.colorEnabled;
    }

    public NotificationRequest setColorEnabled(boolean z) {
        this.colorEnabled = z;
        return this;
    }

    public int getBadgeNumber() {
        return this.number;
    }

    public NotificationRequest setBadgeNumber(int i) {
        this.number = i;
        return this;
    }

    public boolean isAlertOneTime() {
        return this.isAlertOnce;
    }

    public NotificationRequest setAlertOneTime(boolean z) {
        this.isAlertOnce = z;
        return this;
    }

    public boolean isShowStopwatch() {
        return this.isStopwatch;
    }

    public NotificationRequest setShowStopwatch(boolean z) {
        this.isStopwatch = z;
        return this;
    }

    public boolean isCountdownTimer() {
        return this.isCountDown;
    }

    public NotificationRequest setCountdownTimer(boolean z) {
        this.isCountDown = z;
        return this;
    }

    public boolean isInProgress() {
        return this.isOngoing;
    }

    public NotificationRequest setInProgress(boolean z) {
        this.isOngoing = z;
        return this;
    }

    public int getVisibleness() {
        return this.visibleness;
    }

    public NotificationRequest setVisibleness(int i) {
        this.visibleness = i;
        return this;
    }

    public int getProgressValue() {
        return this.progressValue;
    }

    public int getProgressMax() {
        return this.progressMaxValue;
    }

    public boolean isProgressIndeterminate() {
        return this.isIndeterminate;
    }

    public NotificationRequest setProgressBar(int i, int i2, boolean z) {
        this.progressValue = i;
        this.progressMaxValue = i2;
        this.isIndeterminate = z;
        return this;
    }

    public String getStatusBarText() {
        return this.statusBarText;
    }

    public NotificationRequest setStatusBarText(String str) {
        this.statusBarText = str;
        return this;
    }

    public boolean isUnremovable() {
        return this.isUnremovable;
    }

    public NotificationRequest setUnremovable(boolean z) {
        this.isUnremovable = z;
        return this;
    }

    public boolean isFloatingIcon() {
        return this.isFloatingIcon;
    }

    public NotificationRequest addActionButton(NotificationActionButton notificationActionButton) {
        if (notificationActionButton != null && this.actionButtons.size() < 3) {
            this.actionButtons.add(notificationActionButton);
        }
        return this;
    }

    public List<NotificationActionButton> getActionButtons() {
        return this.actionButtons;
    }

    /* access modifiers changed from: package-private */
    public NotificationRequest setFloatingIcon(boolean z) {
        this.isFloatingIcon = z;
        return this;
    }

    public NotificationRequest setPublicNotification(NotificationRequest notificationRequest) {
        if (notificationRequest == null) {
            this.publicNotification = null;
        } else {
            this.publicNotification = new NotificationRequest();
            this.publicNotification.copyFrom(notificationRequest);
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    public NotificationRequest getPublicNotification() {
        return this.publicNotification;
    }

    public NotificationRequest setPermitSystemGeneratedContextualActionButtons(boolean z) {
        this.permitSystemGeneratedContextualActionButtons = z;
        return this;
    }

    public boolean isPermitSystemGeneratedContextualActionButtons() {
        return this.permitSystemGeneratedContextualActionButtons;
    }

    public NotificationRequest setNotificationUserInputHistory(List<String> list) {
        this.extras = new IntentParams();
        if (list == null || list.isEmpty()) {
            this.extras.setParam(EXTRA_USER_INPUT_HISTORY, null);
        } else {
            int min = Math.min(5, list.size());
            if (min == list.size()) {
                this.extras.setParam(EXTRA_USER_INPUT_HISTORY, list);
            } else {
                ArrayList arrayList = new ArrayList();
                for (int i = 0; i < min; i++) {
                    arrayList.add(list.get(i));
                }
                this.extras.setParam(EXTRA_USER_INPUT_HISTORY, arrayList);
            }
        }
        return this;
    }

    public List<MessageUser> getMessageUsers() {
        return this.messageUsers;
    }

    public NotificationRequest addMessageUser(MessageUser messageUser) {
        this.messageUsers.add(messageUser);
        return this;
    }

    public static final class NotificationContent implements Sequenceable {
        public static final int NOTIFICATION_CONTENT_BASIC_TEXT = 0;
        public static final int NOTIFICATION_CONTENT_CONVERSATION = 3;
        public static final int NOTIFICATION_CONTENT_LONG_TEXT = 1;
        public static final int NOTIFICATION_CONTENT_MEDIA = 5;
        public static final int NOTIFICATION_CONTENT_MULTILINE = 4;
        public static final int NOTIFICATION_CONTENT_PICTURE = 2;
        public static final Sequenceable.Producer<NotificationContent> PRODUCER = $$Lambda$NotificationRequest$NotificationContent$_P2Dc3P6UaGq3K_nBm0u7iABLWg.INSTANCE;
        private NotificationBasicContent content;
        private int contentType;

        static /* synthetic */ NotificationContent lambda$static$0(Parcel parcel) {
            NotificationContent notificationContent = new NotificationContent();
            notificationContent.unmarshalling(parcel);
            return notificationContent;
        }

        private NotificationContent() {
            this.content = null;
        }

        NotificationContent(NotificationContent notificationContent) {
            this.content = null;
            this.contentType = notificationContent.contentType;
            this.content = notificationContent.content.replicate();
        }

        public NotificationContent(NotificationNormalContent notificationNormalContent) {
            this.content = null;
            this.contentType = 0;
            this.content = notificationNormalContent.replicate();
        }

        public NotificationContent(NotificationLongTextContent notificationLongTextContent) {
            this.content = null;
            this.contentType = 1;
            this.content = notificationLongTextContent.replicate();
        }

        public NotificationContent(NotificationPictureContent notificationPictureContent) {
            this.content = null;
            this.contentType = 2;
            this.content = notificationPictureContent.replicate();
        }

        public NotificationContent(NotificationConversationalContent notificationConversationalContent) {
            this.content = null;
            this.contentType = 3;
            this.content = notificationConversationalContent.replicate();
        }

        public NotificationContent(NotificationMultiLineContent notificationMultiLineContent) {
            this.content = null;
            this.contentType = 4;
            this.content = notificationMultiLineContent.replicate();
        }

        public NotificationContent(NotificationMediaContent notificationMediaContent) {
            this.content = null;
            this.contentType = 5;
            this.content = notificationMediaContent.replicate();
        }

        public int getContentType() {
            return this.contentType;
        }

        public Object getNotificationContent() {
            return this.content;
        }

        public boolean marshalling(Parcel parcel) {
            if (!parcel.writeInt(this.contentType)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationRequest.NotificationContent::marshalling write contentType failed.", new Object[0]);
                return false;
            }
            parcel.writeSequenceable(this.content);
            return true;
        }

        public boolean unmarshalling(Parcel parcel) {
            this.contentType = parcel.readInt();
            int i = this.contentType;
            if (i == 0) {
                this.content = new NotificationNormalContent();
            } else if (i == 1) {
                this.content = new NotificationLongTextContent();
            } else if (i == 2) {
                this.content = new NotificationPictureContent();
            } else if (i == 3) {
                this.content = new NotificationConversationalContent();
            } else if (i == 4) {
                this.content = new NotificationMultiLineContent();
            } else if (i != 5) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationContent: unmarshalling read contentType failed.", new Object[0]);
                return false;
            } else {
                this.content = new NotificationMediaContent();
            }
            if (parcel.readSequenceable(this.content)) {
                return true;
            }
            HiLog.warn(NotificationRequest.LABEL, "NotificationContent: unmarshalling read content failed.", new Object[0]);
            this.content = null;
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public static abstract class NotificationBasicContent implements Sequenceable {
        String additionalText;
        String text;
        String title;

        public abstract String getAdditionalText();

        public abstract String getText();

        public abstract String getTitle();

        /* access modifiers changed from: package-private */
        public abstract NotificationBasicContent replicate();

        public abstract NotificationBasicContent setAdditionalText(String str);

        public abstract NotificationBasicContent setText(String str);

        public abstract NotificationBasicContent setTitle(String str);

        private NotificationBasicContent() {
        }

        private NotificationBasicContent(NotificationBasicContent notificationBasicContent) {
            this.title = notificationBasicContent.title;
            this.text = notificationBasicContent.text;
            this.additionalText = notificationBasicContent.additionalText;
        }

        public boolean marshalling(Parcel parcel) {
            if (!parcel.writeString(this.title)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationContent: marshalling write title failed.", new Object[0]);
                return false;
            } else if (!parcel.writeString(this.text)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationContent: marshalling write text failed.", new Object[0]);
                return false;
            } else if (parcel.writeString(this.additionalText)) {
                return true;
            } else {
                HiLog.warn(NotificationRequest.LABEL, "NotificationContent: marshalling write additionalText failed.", new Object[0]);
                return false;
            }
        }

        public boolean unmarshalling(Parcel parcel) {
            this.title = parcel.readString();
            this.text = parcel.readString();
            this.additionalText = parcel.readString();
            return true;
        }
    }

    public static final class NotificationNormalContent extends NotificationBasicContent {
        public NotificationNormalContent() {
            super();
        }

        NotificationNormalContent(NotificationNormalContent notificationNormalContent) {
            super(notificationNormalContent);
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationNormalContent replicate() {
            return new NotificationNormalContent(this);
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getTitle() {
            return this.title;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationNormalContent setTitle(String str) {
            this.title = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getText() {
            return this.text;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationNormalContent setText(String str) {
            this.text = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getAdditionalText() {
            return this.additionalText;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationNormalContent setAdditionalText(String str) {
            this.additionalText = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean marshalling(Parcel parcel) {
            return super.marshalling(parcel);
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean unmarshalling(Parcel parcel) {
            return super.unmarshalling(parcel);
        }
    }

    public static final class NotificationLongTextContent extends NotificationBasicContent {
        private String briefText;
        private String expandedTitle;
        private String longText;

        public NotificationLongTextContent() {
            super();
        }

        public NotificationLongTextContent(String str) {
            super();
            this.longText = str;
        }

        NotificationLongTextContent(NotificationLongTextContent notificationLongTextContent) {
            super(notificationLongTextContent);
            this.longText = notificationLongTextContent.longText;
            this.briefText = notificationLongTextContent.briefText;
            this.expandedTitle = notificationLongTextContent.expandedTitle;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getTitle() {
            return this.title;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationLongTextContent setTitle(String str) {
            this.title = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getText() {
            return this.text;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationLongTextContent setText(String str) {
            this.text = str;
            return this;
        }

        public String getLongText() {
            return this.longText;
        }

        public NotificationLongTextContent setLongText(String str) {
            this.longText = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getAdditionalText() {
            return this.additionalText;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationLongTextContent setAdditionalText(String str) {
            this.additionalText = str;
            return this;
        }

        public String getBriefText() {
            return this.briefText;
        }

        public NotificationLongTextContent setBriefText(String str) {
            this.briefText = str;
            return this;
        }

        public String getExpandedTitle() {
            return this.expandedTitle;
        }

        public NotificationLongTextContent setExpandedTitle(String str) {
            this.expandedTitle = str;
            return this;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationLongTextContent replicate() {
            return new NotificationLongTextContent(this);
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean marshalling(Parcel parcel) {
            if (!super.marshalling(parcel)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationLongContent: marshalling write basic content information failed.", new Object[0]);
                return false;
            } else if (!parcel.writeString(this.longText)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationLongTextContent: marshalling write longText failed.", new Object[0]);
                return false;
            } else if (parcel.writeString(this.briefText)) {
                return true;
            } else {
                HiLog.warn(NotificationRequest.LABEL, "NotificationLongTextContent: marshalling write briefText failed.", new Object[0]);
                return false;
            }
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean unmarshalling(Parcel parcel) {
            if (!super.unmarshalling(parcel)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationLongContent: unmarshalling read basic content information failed.", new Object[0]);
                return false;
            }
            this.longText = parcel.readString();
            this.briefText = parcel.readString();
            return true;
        }
    }

    public static final class NotificationPictureContent extends NotificationBasicContent {
        private PixelMap bigPicture;
        private String briefText;
        private String expandedTitle;
        private int pictureId;

        public NotificationPictureContent() {
            super();
            this.pictureId = 0;
        }

        public NotificationPictureContent(int i) {
            super();
            this.pictureId = i;
        }

        NotificationPictureContent(NotificationPictureContent notificationPictureContent) {
            super(notificationPictureContent);
            this.pictureId = notificationPictureContent.pictureId;
            this.briefText = notificationPictureContent.briefText;
            this.expandedTitle = notificationPictureContent.expandedTitle;
            this.bigPicture = notificationPictureContent.bigPicture;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getTitle() {
            return this.title;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationPictureContent setTitle(String str) {
            this.title = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getText() {
            return this.text;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationPictureContent setText(String str) {
            this.text = str;
            return this;
        }

        public int getPictureId() {
            return this.pictureId;
        }

        public NotificationPictureContent setPicture(int i) {
            this.pictureId = i;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getAdditionalText() {
            return this.additionalText;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationPictureContent setAdditionalText(String str) {
            this.additionalText = str;
            return this;
        }

        public String getBriefText() {
            return this.briefText;
        }

        public NotificationPictureContent setBriefText(String str) {
            this.briefText = str;
            return this;
        }

        public String getExpandedTitle() {
            return this.expandedTitle;
        }

        public NotificationPictureContent setExpandedTitle(String str) {
            this.expandedTitle = str;
            return this;
        }

        public NotificationPictureContent setBigPicture(PixelMap pixelMap) {
            this.bigPicture = pixelMap;
            return this;
        }

        public PixelMap getBigPicture() {
            return this.bigPicture;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationPictureContent replicate() {
            return new NotificationPictureContent(this);
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean marshalling(Parcel parcel) {
            if (!super.marshalling(parcel)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationPictureContent: marshalling write basic content information failed.", new Object[0]);
                return false;
            } else if (!parcel.writeInt(this.pictureId)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationPictureContent: marshalling write pictureId failed.", new Object[0]);
                return false;
            } else if (parcel.writeString(this.briefText)) {
                return true;
            } else {
                HiLog.warn(NotificationRequest.LABEL, "NotificationPictureContent: marshalling write briefText failed.", new Object[0]);
                return false;
            }
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean unmarshalling(Parcel parcel) {
            if (!super.unmarshalling(parcel)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationPictureContent: unmarshalling read basic content information failed.", new Object[0]);
                return false;
            }
            this.pictureId = parcel.readInt();
            this.briefText = parcel.readString();
            return true;
        }
    }

    public static final class NotificationConversationalContent extends NotificationBasicContent {
        private static final int MAX_MESSAGES = 1024;
        private boolean conversationGroup;
        private String conversationTitle;
        private List<ConversationalMessage> messages = new ArrayList();
        private MessageUser user;

        NotificationConversationalContent() {
            super();
        }

        public NotificationConversationalContent(MessageUser messageUser) throws IllegalArgumentException {
            super();
            if (messageUser != null) {
                this.user = messageUser;
                return;
            }
            throw new IllegalArgumentException("user can not be null.");
        }

        NotificationConversationalContent(NotificationConversationalContent notificationConversationalContent) {
            super(notificationConversationalContent);
            this.conversationTitle = notificationConversationalContent.conversationTitle;
            this.conversationGroup = notificationConversationalContent.conversationGroup;
            this.user = notificationConversationalContent.user;
            this.messages = notificationConversationalContent.messages;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationConversationalContent replicate() {
            return new NotificationConversationalContent(this);
        }

        public MessageUser getMessageUser() {
            return this.user;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getTitle() {
            return this.title;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationConversationalContent setTitle(String str) {
            this.title = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getText() {
            return this.text;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationConversationalContent setText(String str) {
            this.text = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getAdditionalText() {
            return this.additionalText;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationConversationalContent setAdditionalText(String str) {
            this.additionalText = str;
            return this;
        }

        public String getConversationTitle() {
            return this.conversationTitle;
        }

        public NotificationConversationalContent setConversationTitle(String str) {
            this.conversationTitle = str;
            return this;
        }

        public boolean isConversationGroup() {
            return this.conversationGroup;
        }

        public NotificationConversationalContent setConversationGroup(boolean z) {
            this.conversationGroup = z;
            return this;
        }

        public List<ConversationalMessage> getAllConversationalMessages() {
            return this.messages;
        }

        public NotificationConversationalContent addConversationalMessage(ConversationalMessage conversationalMessage) {
            this.messages.add(conversationalMessage);
            return this;
        }

        public NotificationConversationalContent addConversationalMessage(String str, long j, MessageUser messageUser) throws IllegalArgumentException {
            return addConversationalMessage(new ConversationalMessage(str, j, messageUser));
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean marshalling(Parcel parcel) {
            if (!super.marshalling(parcel)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationConversationalContent: marshalling write basic content information failed.", new Object[0]);
                return false;
            } else if (!parcel.writeString(this.conversationTitle)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationConversationalContent: marshalling write conversationTitle failed.", new Object[0]);
                return false;
            } else if (!parcel.writeBoolean(this.conversationGroup)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationConversationalContent: marshalling write conversationGroup failed.", new Object[0]);
                return false;
            } else {
                parcel.writeSequenceable(this.user);
                int size = this.messages.size();
                if (!parcel.writeInt(size)) {
                    HiLog.warn(NotificationRequest.LABEL, "NotificationConversationalContent: marshalling write size of messages failed.", new Object[0]);
                    return false;
                } else if (size <= 0) {
                    return true;
                } else {
                    for (ConversationalMessage conversationalMessage : this.messages) {
                        parcel.writeSequenceable(conversationalMessage);
                    }
                    return true;
                }
            }
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean unmarshalling(Parcel parcel) {
            if (!super.unmarshalling(parcel)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationConversationalContent: unmarshalling read basic content information failed.", new Object[0]);
                return false;
            }
            this.conversationTitle = parcel.readString();
            this.conversationGroup = parcel.readBoolean();
            this.user = new MessageUser();
            if (!parcel.readSequenceable(this.user)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationConversationalContent: unmarshalling read user failed.", new Object[0]);
                return false;
            }
            this.messages.clear();
            int readInt = parcel.readInt();
            if (readInt > 1024) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationConversationalContent: unmarshalling messages oversize.", new Object[0]);
                return false;
            }
            for (int i = 0; i < readInt; i++) {
                ConversationalMessage conversationalMessage = new ConversationalMessage();
                if (!parcel.readSequenceable(conversationalMessage)) {
                    HiLog.warn(NotificationRequest.LABEL, "NotificationConversationalContent: unmarshalling read messages fail.", new Object[0]);
                    return false;
                }
                this.messages.add(conversationalMessage);
            }
            return true;
        }

        public static final class ConversationalMessage implements Sequenceable {
            private static final byte HAS_VALUE = 1;
            private static final byte NO_VALUE = 0;
            public static final Sequenceable.Producer<ConversationalMessage> PRODUCER = $$Lambda$NotificationRequest$NotificationConversationalContent$ConversationalMessage$ufr8j8_bt2EbNtM7wl8KA6Kbiw.INSTANCE;
            private String mimeType;
            private MessageUser sender;
            private String text;
            private long timestamp;
            private Uri uri;

            static /* synthetic */ ConversationalMessage lambda$static$0(Parcel parcel) {
                ConversationalMessage conversationalMessage = new ConversationalMessage();
                conversationalMessage.unmarshalling(parcel);
                return conversationalMessage;
            }

            ConversationalMessage() {
            }

            public ConversationalMessage(String str, long j, MessageUser messageUser) throws IllegalArgumentException {
                if (str != null) {
                    this.text = str;
                    this.timestamp = j;
                    this.sender = messageUser;
                    return;
                }
                throw new IllegalArgumentException("text can not be null.");
            }

            public String getText() {
                return this.text;
            }

            public long getArrivedTime() {
                return this.timestamp;
            }

            public MessageUser getSender() {
                return this.sender;
            }

            public ConversationalMessage setData(String str, Uri uri2) {
                this.mimeType = str;
                this.uri = uri2;
                return this;
            }

            public String getMimeType() {
                return this.mimeType;
            }

            public Uri getUri() {
                return this.uri;
            }

            public boolean marshalling(Parcel parcel) {
                if (!parcel.writeString(this.text)) {
                    HiLog.warn(NotificationRequest.LABEL, "ConversationalMessage: marshalling write text failed.", new Object[0]);
                    return false;
                } else if (!parcel.writeLong(this.timestamp)) {
                    HiLog.warn(NotificationRequest.LABEL, "ConversationalMessage: marshalling write timestamp failed.", new Object[0]);
                    return false;
                } else if (!parcel.writeString(this.mimeType)) {
                    HiLog.warn(NotificationRequest.LABEL, "ConversationalMessage: marshalling write mimeType failed.", new Object[0]);
                    return false;
                } else {
                    if (this.uri != null) {
                        if (!parcel.writeByte((byte) 1)) {
                            return false;
                        }
                        parcel.writeSequenceable(this.uri);
                    } else if (!parcel.writeByte((byte) 0)) {
                        return false;
                    }
                    parcel.writeSequenceable(this.sender);
                    return true;
                }
            }

            public boolean unmarshalling(Parcel parcel) {
                this.text = parcel.readString();
                this.timestamp = parcel.readLong();
                this.mimeType = parcel.readString();
                byte readByte = parcel.readByte();
                if (readByte == 1) {
                    try {
                        this.uri = Uri.readFromParcel(parcel);
                    } catch (IllegalArgumentException unused) {
                        this.uri = null;
                    }
                } else if (readByte == 0) {
                    this.uri = null;
                } else {
                    HiLog.warn(NotificationRequest.LABEL, "ConversationalMessage: readFromParcel read uri parcel fail.", new Object[0]);
                    return false;
                }
                this.sender = new MessageUser();
                if (parcel.readSequenceable(this.sender)) {
                    return true;
                }
                HiLog.warn(NotificationRequest.LABEL, "ConversationalMessage: unmarshalling read sender failed.", new Object[0]);
                return false;
            }
        }
    }

    public static final class NotificationMultiLineContent extends NotificationBasicContent {
        private String briefText;
        private List<String> lines = new ArrayList();
        private String longTitle;

        public NotificationMultiLineContent() {
            super();
        }

        NotificationMultiLineContent(NotificationMultiLineContent notificationMultiLineContent) {
            super(notificationMultiLineContent);
            this.briefText = notificationMultiLineContent.briefText;
            this.longTitle = notificationMultiLineContent.longTitle;
            this.lines = notificationMultiLineContent.lines;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationMultiLineContent replicate() {
            return new NotificationMultiLineContent(this);
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getTitle() {
            return this.title;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationMultiLineContent setTitle(String str) {
            this.title = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getText() {
            return this.text;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationMultiLineContent setText(String str) {
            this.text = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getAdditionalText() {
            return this.additionalText;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationMultiLineContent setAdditionalText(String str) {
            this.additionalText = str;
            return this;
        }

        public String getBriefText() {
            return this.briefText;
        }

        public NotificationMultiLineContent setBriefText(String str) {
            this.briefText = str;
            return this;
        }

        public String getExpandedTitle() {
            return this.longTitle;
        }

        public NotificationMultiLineContent setExpandedTitle(String str) {
            this.longTitle = str;
            return this;
        }

        public List<String> getAllLines() {
            return this.lines;
        }

        public NotificationMultiLineContent addSingleLine(String str) {
            this.lines.add(str);
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean marshalling(Parcel parcel) {
            if (!super.marshalling(parcel)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationMultiLineContent: marshalling write basic content information failed.", new Object[0]);
                return false;
            } else if (!parcel.writeString(this.briefText)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationMultiLineContent: marshalling write briefText failed.", new Object[0]);
                return false;
            } else if (!parcel.writeString(this.longTitle)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationMultiLineContent: marshalling write longTitle failed.", new Object[0]);
                return false;
            } else if (parcel.writeStringList(this.lines)) {
                return true;
            } else {
                HiLog.warn(NotificationRequest.LABEL, "NotificationMultiLineContent: marshalling write lines failed.", new Object[0]);
                return false;
            }
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean unmarshalling(Parcel parcel) {
            if (!super.unmarshalling(parcel)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationMultiLineContent: unmarshalling read basic content information failed.", new Object[0]);
                return false;
            }
            this.briefText = parcel.readString();
            this.longTitle = parcel.readString();
            this.lines.clear();
            this.lines = parcel.readStringList();
            return true;
        }
    }

    public static final class NotificationMediaContent extends NotificationBasicContent {
        private int[] actionsToShow = null;
        private AVToken avToken;

        public NotificationMediaContent() {
            super();
        }

        NotificationMediaContent(NotificationMediaContent notificationMediaContent) {
            super(notificationMediaContent);
            this.avToken = notificationMediaContent.avToken;
            this.actionsToShow = notificationMediaContent.actionsToShow;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationMediaContent replicate() {
            return new NotificationMediaContent(this);
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getTitle() {
            return this.title;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationMediaContent setTitle(String str) {
            this.title = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getText() {
            return this.text;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationMediaContent setText(String str) {
            this.text = str;
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public String getAdditionalText() {
            return this.additionalText;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public NotificationMediaContent setAdditionalText(String str) {
            this.additionalText = str;
            return this;
        }

        public AVToken getAVToken() {
            return this.avToken;
        }

        public NotificationMediaContent setAVToken(AVToken aVToken) {
            this.avToken = aVToken;
            return this;
        }

        public int[] getShownActions() {
            int[] iArr = this.actionsToShow;
            return iArr != null ? (int[]) iArr.clone() : new int[0];
        }

        public NotificationMediaContent setShownActions(int[] iArr) {
            if (iArr != null) {
                this.actionsToShow = (int[]) iArr.clone();
            }
            return this;
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean marshalling(Parcel parcel) {
            if (!super.marshalling(parcel)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationMediaContent: marshalling write basic content information failed.", new Object[0]);
                return false;
            } else if (parcel.writeIntArray(this.actionsToShow)) {
                return true;
            } else {
                HiLog.warn(NotificationRequest.LABEL, "NotificationMediaContent: marshalling write actionsToShow failed.", new Object[0]);
                return false;
            }
        }

        @Override // ohos.event.notification.NotificationRequest.NotificationBasicContent
        public boolean unmarshalling(Parcel parcel) {
            if (!super.unmarshalling(parcel)) {
                HiLog.warn(NotificationRequest.LABEL, "NotificationMediaContent: unmarshalling read basic content information failed.", new Object[0]);
                return false;
            }
            this.actionsToShow = parcel.readIntArray();
            return true;
        }
    }

    public boolean marshalling(Parcel parcel) {
        if (!writeToParcel(parcel)) {
            return false;
        }
        parcel.writeSequenceable(this.content);
        parcel.writeSequenceable(this.extras);
        parcel.writeSequenceable(this.removalIntentAgent);
        parcel.writeSequenceable(this.maxScreenIntentAgent);
        parcel.writeSequenceable(this.agent);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        if (!readFromParcel(parcel)) {
            return false;
        }
        this.content = new NotificationContent();
        if (!parcel.readSequenceable(this.content)) {
            HiLog.warn(LABEL, "NotificationRequest: unmarshalling read content failed.", new Object[0]);
            this.content = null;
        }
        this.extras = new IntentParams();
        if (!parcel.readSequenceable(this.extras)) {
            HiLog.warn(LABEL, "NotificationRequest unmarshalling read: extras is null.", new Object[0]);
            this.extras = null;
        }
        this.removalIntentAgent = new IntentAgent(null);
        if (!parcel.readSequenceable(this.removalIntentAgent)) {
            HiLog.warn(LABEL, "NotificationRequest: unmarshalling read removalIntentAgent failed.", new Object[0]);
            this.removalIntentAgent = null;
        }
        this.maxScreenIntentAgent = new IntentAgent(null);
        if (!parcel.readSequenceable(this.maxScreenIntentAgent)) {
            HiLog.warn(LABEL, "NotificationRequest: unmarshalling read maxScreenIntentAgent failed.", new Object[0]);
            this.maxScreenIntentAgent = null;
        }
        this.agent = new IntentAgent(null);
        if (parcel.readSequenceable(this.agent)) {
            return true;
        }
        HiLog.warn(LABEL, "NotificationRequest: unmarshalling read agent failed.", new Object[0]);
        this.agent = null;
        return true;
    }

    private boolean writeToParcel(Parcel parcel) {
        if (!parcel.writeInt(this.notificationId)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write notificationId failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.slotId)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write slotId failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.notificationHashCode)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write notificationHashCode failed.", new Object[0]);
            return false;
        } else if (!parcel.writeLong(this.createTime)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write createTime failed.", new Object[0]);
            return false;
        } else if (!parcel.writeLong(this.deliveryTime)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write deliveryTime failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.tapDismissed)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write tapDismissed failed.", new Object[0]);
            return false;
        } else if (!parcel.writeLong(this.autoDeletedTime)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write autoDeletedTime failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.settingsText)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write settingsText failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.creatorBundleName)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write creatorBundleName failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.ownerBundleName)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write ownerBundleName failed.", new Object[0]);
            return false;
        } else if (!writeToParcelSecond(parcel)) {
            return false;
        } else {
            return writeToParcelThird(parcel);
        }
    }

    private boolean writeToParcelSecond(Parcel parcel) {
        if (!parcel.writeInt(this.creatorPid)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write creatorPid failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.creatorUid)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write creatorUid failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.groupValue)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write groupValue failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.groupAlertType)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write groupAlertType failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.groupOverview)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write groupOverview failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.classification)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write classification failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.color)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write color failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.colorEnabled)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write colorEnabled failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.number)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write number failed.", new Object[0]);
            return false;
        } else if (parcel.writeBoolean(this.isAlertOnce)) {
            return true;
        } else {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write isAlertOnce failed.", new Object[0]);
            return false;
        }
    }

    private boolean writeToParcelThird(Parcel parcel) {
        if (!parcel.writeBoolean(this.isStopwatch)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write isStopwatch failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.isCountDown)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write isCountDown failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.isOngoing)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write isOngoing failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.showCreateTime)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write showCreateTime failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.visibleness)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write visibleness failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.progressValue)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write progressValue failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.progressMaxValue)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write progressMaxValue failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.isIndeterminate)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write isIndeterminate failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.statusBarText)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write statusBarText failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.isUnremovable)) {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write isUnremovable failed.", new Object[0]);
            return false;
        } else if (parcel.writeBoolean(this.isFloatingIcon)) {
            return true;
        } else {
            HiLog.warn(LABEL, "NotificationRequest: marshalling write isFloatingIcon failed.", new Object[0]);
            return false;
        }
    }

    private boolean readFromParcel(Parcel parcel) {
        this.notificationId = parcel.readInt();
        this.slotId = parcel.readString();
        this.notificationHashCode = parcel.readString();
        this.createTime = parcel.readLong();
        this.deliveryTime = parcel.readLong();
        this.tapDismissed = parcel.readBoolean();
        this.autoDeletedTime = parcel.readLong();
        this.settingsText = parcel.readString();
        this.creatorBundleName = parcel.readString();
        this.ownerBundleName = parcel.readString();
        this.creatorPid = parcel.readInt();
        this.creatorUid = parcel.readInt();
        this.groupValue = parcel.readString();
        this.groupAlertType = parcel.readInt();
        this.groupOverview = parcel.readBoolean();
        this.classification = parcel.readString();
        this.color = parcel.readInt();
        this.colorEnabled = parcel.readBoolean();
        this.number = parcel.readInt();
        this.isAlertOnce = parcel.readBoolean();
        this.isStopwatch = parcel.readBoolean();
        this.isCountDown = parcel.readBoolean();
        this.isOngoing = parcel.readBoolean();
        this.showCreateTime = parcel.readBoolean();
        this.visibleness = parcel.readInt();
        this.progressValue = parcel.readInt();
        this.progressMaxValue = parcel.readInt();
        this.isIndeterminate = parcel.readBoolean();
        this.statusBarText = parcel.readString();
        this.isUnremovable = parcel.readBoolean();
        this.isFloatingIcon = parcel.readBoolean();
        return true;
    }

    private String generateNotificationHashCode() {
        return String.valueOf(this.notificationId);
    }

    private void copyFrom(NotificationRequest notificationRequest) {
        copyLighty(notificationRequest);
        copyHeavy(notificationRequest);
    }

    private void copyLighty(NotificationRequest notificationRequest) {
        this.createTime = notificationRequest.createTime;
        this.deliveryTime = notificationRequest.deliveryTime;
        this.notificationId = notificationRequest.notificationId;
        this.slotId = notificationRequest.slotId;
        this.notificationHashCode = notificationRequest.notificationHashCode;
        this.autoDeletedTime = notificationRequest.autoDeletedTime;
        this.tapDismissed = notificationRequest.tapDismissed;
        this.settingsText = notificationRequest.settingsText;
        this.creatorBundleName = notificationRequest.creatorBundleName;
        this.ownerBundleName = notificationRequest.ownerBundleName;
        this.creatorPid = notificationRequest.creatorPid;
        this.creatorUid = notificationRequest.creatorUid;
        this.groupValue = notificationRequest.groupValue;
        this.groupAlertType = notificationRequest.groupAlertType;
        this.groupOverview = notificationRequest.groupOverview;
        this.classification = notificationRequest.classification;
        this.color = notificationRequest.color;
        this.colorEnabled = notificationRequest.colorEnabled;
        this.number = notificationRequest.number;
        this.isAlertOnce = notificationRequest.isAlertOnce;
        this.isStopwatch = notificationRequest.isStopwatch;
        this.isCountDown = notificationRequest.isCountDown;
        this.isOngoing = notificationRequest.isOngoing;
        this.showCreateTime = notificationRequest.showCreateTime;
        this.visibleness = notificationRequest.visibleness;
        this.progressValue = notificationRequest.progressValue;
        this.progressMaxValue = notificationRequest.progressMaxValue;
        this.isIndeterminate = notificationRequest.isIndeterminate;
        this.statusBarText = notificationRequest.statusBarText;
        this.isUnremovable = notificationRequest.isUnremovable;
        this.isFloatingIcon = notificationRequest.isFloatingIcon;
        this.onlyLocal = notificationRequest.onlyLocal;
        this.sortingKey = notificationRequest.sortingKey;
        this.context = notificationRequest.context;
        this.customView = notificationRequest.customView;
        this.customBigView = notificationRequest.customBigView;
        this.customFloatView = notificationRequest.customFloatView;
        this.permitSystemGeneratedContextualActionButtons = notificationRequest.permitSystemGeneratedContextualActionButtons;
    }

    private void copyHeavy(NotificationRequest notificationRequest) {
        NotificationContent notificationContent = notificationRequest.content;
        if (notificationContent != null) {
            this.content = new NotificationContent(notificationContent);
        }
        IntentParams intentParams = notificationRequest.extras;
        if (intentParams != null) {
            this.extras = new IntentParams(intentParams);
        }
        IntentAgent intentAgent = notificationRequest.agent;
        if (intentAgent != null) {
            this.agent = new IntentAgent(intentAgent.getObject());
        }
        IntentAgent intentAgent2 = notificationRequest.removalIntentAgent;
        if (intentAgent2 != null) {
            this.removalIntentAgent = new IntentAgent(intentAgent2.getObject());
        }
        IntentAgent intentAgent3 = notificationRequest.maxScreenIntentAgent;
        if (intentAgent3 != null) {
            this.maxScreenIntentAgent = new IntentAgent(intentAgent3.getObject());
        }
        if (notificationRequest.publicNotification != null) {
            this.publicNotification = new NotificationRequest();
            this.publicNotification.copyFrom(notificationRequest.publicNotification);
        }
        List<NotificationActionButton> list = notificationRequest.actionButtons;
        if (list != null) {
            for (NotificationActionButton notificationActionButton : list) {
                if (notificationActionButton != null) {
                    this.actionButtons.add(new NotificationActionButton.Builder(notificationActionButton).build());
                }
            }
        }
        List<MessageUser> list2 = notificationRequest.messageUsers;
        if (list2 != null) {
            for (MessageUser messageUser : list2) {
                if (messageUser != null) {
                    this.messageUsers.add(messageUser);
                }
            }
        }
    }
}
