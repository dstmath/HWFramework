package android.app;

import android.Manifest.permission;
import android.R;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hwtheme.HwThemeManager;
import android.media.AudioAttributes;
import android.media.PlayerBase;
import android.media.session.MediaSession.Token;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.BidiFormatter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.HwNotificationColorUtil;
import com.android.internal.util.NotificationColorUtil;
import com.android.internal.util.Preconditions;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Notification implements Parcelable {
    public static final AudioAttributes AUDIO_ATTRIBUTES_DEFAULT = new android.media.AudioAttributes.Builder().setContentType(4).setUsage(5).build();
    public static final int BADGE_ICON_LARGE = 2;
    public static final int BADGE_ICON_NONE = 0;
    public static final int BADGE_ICON_SMALL = 1;
    public static final String CATEGORY_ALARM = "alarm";
    public static final String CATEGORY_CALL = "call";
    public static final String CATEGORY_EMAIL = "email";
    public static final String CATEGORY_ERROR = "err";
    public static final String CATEGORY_EVENT = "event";
    public static final String CATEGORY_MESSAGE = "msg";
    public static final String CATEGORY_PROGRESS = "progress";
    public static final String CATEGORY_PROMO = "promo";
    public static final String CATEGORY_RECOMMENDATION = "recommendation";
    public static final String CATEGORY_REMINDER = "reminder";
    public static final String CATEGORY_SERVICE = "service";
    public static final String CATEGORY_SOCIAL = "social";
    public static final String CATEGORY_STATUS = "status";
    public static final String CATEGORY_SYSTEM = "sys";
    public static final String CATEGORY_TRANSPORT = "transport";
    public static final int COLOR_DEFAULT = 0;
    public static final int COLOR_INVALID = 1;
    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        public Notification createFromParcel(Parcel parcel) {
            return new Notification(parcel);
        }

        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };
    public static final int DEFAULT_ALL = -1;
    public static final int DEFAULT_LIGHTS = 4;
    public static final int DEFAULT_SOUND = 1;
    public static final int DEFAULT_VIBRATE = 2;
    public static final String EXTRA_ALLOW_DURING_SETUP = "android.allowDuringSetup";
    public static final String EXTRA_APP_NAME = "android.extraAppName";
    public static final String EXTRA_AUDIO_CONTENTS_URI = "android.audioContents";
    public static final String EXTRA_BACKGROUND_IMAGE_URI = "android.backgroundImageUri";
    public static final String EXTRA_BIG_TEXT = "android.bigText";
    public static final String EXTRA_BUILDER_APPLICATION_INFO = "android.appInfo";
    public static final String EXTRA_CHANNEL_ID = "android.intent.extra.CHANNEL_ID";
    public static final String EXTRA_CHRONOMETER_COUNT_DOWN = "android.chronometerCountDown";
    public static final String EXTRA_COLORIZED = "android.colorized";
    public static final String EXTRA_COMPACT_ACTIONS = "android.compactActions";
    public static final String EXTRA_CONTAINS_CUSTOM_VIEW = "android.contains.customView";
    public static final String EXTRA_CONVERSATION_TITLE = "android.conversationTitle";
    public static final String EXTRA_FOREGROUND_APPS = "android.foregroundApps";
    public static final String EXTRA_HISTORIC_MESSAGES = "android.messages.historic";
    public static final String EXTRA_HW_IS_INTENT_PROTECTED_APP = "com.huawei.isIntentProtectedApp";
    public static final String EXTRA_INFO_TEXT = "android.infoText";
    @Deprecated
    public static final String EXTRA_LARGE_ICON = "android.largeIcon";
    public static final String EXTRA_LARGE_ICON_BIG = "android.largeIcon.big";
    public static final String EXTRA_MEDIA_SESSION = "android.mediaSession";
    public static final String EXTRA_MESSAGES = "android.messages";
    public static final String EXTRA_NOTIFICATION_ID = "android.intent.extra.NOTIFICATION_ID";
    public static final String EXTRA_NOTIFICATION_TAG = "android.intent.extra.NOTIFICATION_TAG";
    public static final String EXTRA_PEOPLE = "android.people";
    public static final String EXTRA_PICTURE = "android.picture";
    public static final String EXTRA_PROGRESS = "android.progress";
    public static final String EXTRA_PROGRESS_INDETERMINATE = "android.progressIndeterminate";
    public static final String EXTRA_PROGRESS_MAX = "android.progressMax";
    public static final String EXTRA_REDUCED_IMAGES = "android.reduced.images";
    public static final String EXTRA_REMOTE_INPUT_HISTORY = "android.remoteInputHistory";
    public static final String EXTRA_SELF_DISPLAY_NAME = "android.selfDisplayName";
    public static final String EXTRA_SHOW_ACTION_ICON = "android.extraShowActionIcon";
    public static final String EXTRA_SHOW_CHRONOMETER = "android.showChronometer";
    public static final String EXTRA_SHOW_WHEN = "android.showWhen";
    @Deprecated
    public static final String EXTRA_SMALL_ICON = "android.icon";
    public static final String EXTRA_SUBSTITUTE_APP_NAME = "android.substName";
    public static final String EXTRA_SUB_TEXT = "android.subText";
    public static final String EXTRA_SUMMARY_TEXT = "android.summaryText";
    public static final String EXTRA_TEMPLATE = "android.template";
    public static final String EXTRA_TEXT = "android.text";
    public static final String EXTRA_TEXT_LINES = "android.textLines";
    public static final String EXTRA_TITLE = "android.title";
    public static final String EXTRA_TITLE_BIG = "android.title.big";
    public static final int FLAG_AUTOGROUP_SUMMARY = 1024;
    public static final int FLAG_AUTO_CANCEL = 16;
    public static final int FLAG_FOREGROUND_SERVICE = 64;
    public static final int FLAG_GROUP_SUMMARY = 512;
    @Deprecated
    public static final int FLAG_HIGH_PRIORITY = 128;
    public static final int FLAG_INSISTENT = 4;
    public static final int FLAG_LOCAL_ONLY = 256;
    public static final int FLAG_NO_CLEAR = 32;
    public static final int FLAG_ONGOING_EVENT = 2;
    public static final int FLAG_ONLY_ALERT_ONCE = 8;
    @Deprecated
    public static final int FLAG_SHOW_LIGHTS = 1;
    public static final int GROUP_ALERT_ALL = 0;
    public static final int GROUP_ALERT_CHILDREN = 2;
    public static final int GROUP_ALERT_SUMMARY = 1;
    public static final String INTENT_CATEGORY_NOTIFICATION_PREFERENCES = "android.intent.category.NOTIFICATION_PREFERENCES";
    private static final int MAX_CHARSEQUENCE_LENGTH = 5120;
    private static final int MAX_REPLY_HISTORY = 5;
    @Deprecated
    public static final int PRIORITY_DEFAULT = 0;
    @Deprecated
    public static final int PRIORITY_HIGH = 1;
    @Deprecated
    public static final int PRIORITY_LOW = -1;
    @Deprecated
    public static final int PRIORITY_MAX = 2;
    @Deprecated
    public static final int PRIORITY_MIN = -2;
    @Deprecated
    public static final int STREAM_DEFAULT = -1;
    private static final String TAG = "Notification";
    public static final int VISIBILITY_PRIVATE = 0;
    public static final int VISIBILITY_PUBLIC = 1;
    public static final int VISIBILITY_SECRET = -1;
    public static IBinder processWhitelistToken;
    public static IBinder whitelistToken;
    public Action[] actions;
    public ArraySet<PendingIntent> allPendingIntents;
    @Deprecated
    public AudioAttributes audioAttributes;
    @Deprecated
    public int audioStreamType;
    @Deprecated
    public RemoteViews bigContentView;
    public String category;
    public int color;
    public PendingIntent contentIntent;
    @Deprecated
    public RemoteViews contentView;
    private long creationTime;
    @Deprecated
    public int defaults;
    public PendingIntent deleteIntent;
    public Bundle extras;
    public int flags;
    public PendingIntent fullScreenIntent;
    @Deprecated
    public RemoteViews headsUpContentView;
    @Deprecated
    public int icon;
    public int iconLevel;
    @Deprecated
    public Bitmap largeIcon;
    @Deprecated
    public int ledARGB;
    @Deprecated
    public int ledOffMS;
    @Deprecated
    public int ledOnMS;
    private int mBadgeIcon;
    private String mChannelId;
    private int mGroupAlertBehavior;
    private String mGroupKey;
    private Icon mLargeIcon;
    private CharSequence mSettingsText;
    private String mShortcutId;
    private Icon mSmallIcon;
    private String mSortKey;
    private Object mSyncLock;
    private long mTimeout;
    public int number;
    @Deprecated
    public int priority;
    public Notification publicVersion;
    @Deprecated
    public Uri sound;
    public CharSequence tickerText;
    @Deprecated
    public RemoteViews tickerView;
    @Deprecated
    public long[] vibrate;
    public int visibility;
    public long when;

    public static class Action implements Parcelable {
        public static final Creator<Action> CREATOR = new Creator<Action>() {
            public Action createFromParcel(Parcel in) {
                return new Action(in, null);
            }

            public Action[] newArray(int size) {
                return new Action[size];
            }
        };
        private static final String EXTRA_DATA_ONLY_INPUTS = "android.extra.DATA_ONLY_INPUTS";
        public PendingIntent actionIntent;
        @Deprecated
        public int icon;
        private boolean mAllowGeneratedReplies;
        private final Bundle mExtras;
        private Icon mIcon;
        private final RemoteInput[] mRemoteInputs;
        public CharSequence title;

        public static final class Builder {
            private boolean mAllowGeneratedReplies;
            private final Bundle mExtras;
            private final Icon mIcon;
            private final PendingIntent mIntent;
            private ArrayList<RemoteInput> mRemoteInputs;
            private final CharSequence mTitle;

            @Deprecated
            public Builder(int icon, CharSequence title, PendingIntent intent) {
                this(Icon.createWithResource(ProxyInfo.LOCAL_EXCL_LIST, icon), title, intent);
            }

            public Builder(Icon icon, CharSequence title, PendingIntent intent) {
                this(icon, title, intent, new Bundle(), null, true);
            }

            public Builder(Action action) {
                this(action.getIcon(), action.title, action.actionIntent, new Bundle(action.mExtras), action.getRemoteInputs(), action.getAllowGeneratedReplies());
            }

            private Builder(Icon icon, CharSequence title, PendingIntent intent, Bundle extras, RemoteInput[] remoteInputs, boolean allowGeneratedReplies) {
                this.mAllowGeneratedReplies = true;
                this.mIcon = icon;
                this.mTitle = title;
                this.mIntent = intent;
                this.mExtras = extras;
                if (remoteInputs != null) {
                    this.mRemoteInputs = new ArrayList(remoteInputs.length);
                    Collections.addAll(this.mRemoteInputs, remoteInputs);
                }
                this.mAllowGeneratedReplies = allowGeneratedReplies;
            }

            public Builder addExtras(Bundle extras) {
                if (extras != null) {
                    this.mExtras.putAll(extras);
                }
                return this;
            }

            public Bundle getExtras() {
                return this.mExtras;
            }

            public Builder addRemoteInput(RemoteInput remoteInput) {
                if (this.mRemoteInputs == null) {
                    this.mRemoteInputs = new ArrayList();
                }
                this.mRemoteInputs.add(remoteInput);
                return this;
            }

            public Builder setAllowGeneratedReplies(boolean allowGeneratedReplies) {
                this.mAllowGeneratedReplies = allowGeneratedReplies;
                return this;
            }

            public Builder extend(Extender extender) {
                extender.extend(this);
                return this;
            }

            public Action build() {
                ArrayList<RemoteInput> dataOnlyInputs = new ArrayList();
                RemoteInput[] previousDataInputs = (RemoteInput[]) this.mExtras.getParcelableArray(Action.EXTRA_DATA_ONLY_INPUTS);
                if (previousDataInputs != null) {
                    for (RemoteInput input : previousDataInputs) {
                        dataOnlyInputs.add(input);
                    }
                }
                List<RemoteInput> textInputs = new ArrayList();
                if (this.mRemoteInputs != null) {
                    for (RemoteInput input2 : this.mRemoteInputs) {
                        if (input2.isDataOnly()) {
                            dataOnlyInputs.add(input2);
                        } else {
                            textInputs.add(input2);
                        }
                    }
                }
                if (!dataOnlyInputs.isEmpty()) {
                    this.mExtras.putParcelableArray(Action.EXTRA_DATA_ONLY_INPUTS, (RemoteInput[]) dataOnlyInputs.toArray(new RemoteInput[dataOnlyInputs.size()]));
                }
                return new Action(this.mIcon, this.mTitle, this.mIntent, this.mExtras, textInputs.isEmpty() ? null : (RemoteInput[]) textInputs.toArray(new RemoteInput[textInputs.size()]), this.mAllowGeneratedReplies, null);
            }
        }

        public interface Extender {
            Builder extend(Builder builder);
        }

        public static final class WearableExtender implements Extender {
            private static final int DEFAULT_FLAGS = 1;
            private static final String EXTRA_WEARABLE_EXTENSIONS = "android.wearable.EXTENSIONS";
            private static final int FLAG_AVAILABLE_OFFLINE = 1;
            private static final int FLAG_HINT_DISPLAY_INLINE = 4;
            private static final int FLAG_HINT_LAUNCHES_ACTIVITY = 2;
            private static final String KEY_CANCEL_LABEL = "cancelLabel";
            private static final String KEY_CONFIRM_LABEL = "confirmLabel";
            private static final String KEY_FLAGS = "flags";
            private static final String KEY_IN_PROGRESS_LABEL = "inProgressLabel";
            private CharSequence mCancelLabel;
            private CharSequence mConfirmLabel;
            private int mFlags = 1;
            private CharSequence mInProgressLabel;

            public WearableExtender(Action action) {
                Bundle wearableBundle = action.getExtras().getBundle(EXTRA_WEARABLE_EXTENSIONS);
                if (wearableBundle != null) {
                    this.mFlags = wearableBundle.getInt(KEY_FLAGS, 1);
                    this.mInProgressLabel = wearableBundle.getCharSequence(KEY_IN_PROGRESS_LABEL);
                    this.mConfirmLabel = wearableBundle.getCharSequence(KEY_CONFIRM_LABEL);
                    this.mCancelLabel = wearableBundle.getCharSequence(KEY_CANCEL_LABEL);
                }
            }

            public Builder extend(Builder builder) {
                Bundle wearableBundle = new Bundle();
                if (this.mFlags != 1) {
                    wearableBundle.putInt(KEY_FLAGS, this.mFlags);
                }
                if (this.mInProgressLabel != null) {
                    wearableBundle.putCharSequence(KEY_IN_PROGRESS_LABEL, this.mInProgressLabel);
                }
                if (this.mConfirmLabel != null) {
                    wearableBundle.putCharSequence(KEY_CONFIRM_LABEL, this.mConfirmLabel);
                }
                if (this.mCancelLabel != null) {
                    wearableBundle.putCharSequence(KEY_CANCEL_LABEL, this.mCancelLabel);
                }
                builder.getExtras().putBundle(EXTRA_WEARABLE_EXTENSIONS, wearableBundle);
                return builder;
            }

            public WearableExtender clone() {
                WearableExtender that = new WearableExtender();
                that.mFlags = this.mFlags;
                that.mInProgressLabel = this.mInProgressLabel;
                that.mConfirmLabel = this.mConfirmLabel;
                that.mCancelLabel = this.mCancelLabel;
                return that;
            }

            public WearableExtender setAvailableOffline(boolean availableOffline) {
                setFlag(1, availableOffline);
                return this;
            }

            public boolean isAvailableOffline() {
                return (this.mFlags & 1) != 0;
            }

            private void setFlag(int mask, boolean value) {
                if (value) {
                    this.mFlags |= mask;
                } else {
                    this.mFlags &= ~mask;
                }
            }

            public WearableExtender setInProgressLabel(CharSequence label) {
                this.mInProgressLabel = label;
                return this;
            }

            public CharSequence getInProgressLabel() {
                return this.mInProgressLabel;
            }

            public WearableExtender setConfirmLabel(CharSequence label) {
                this.mConfirmLabel = label;
                return this;
            }

            public CharSequence getConfirmLabel() {
                return this.mConfirmLabel;
            }

            public WearableExtender setCancelLabel(CharSequence label) {
                this.mCancelLabel = label;
                return this;
            }

            public CharSequence getCancelLabel() {
                return this.mCancelLabel;
            }

            public WearableExtender setHintLaunchesActivity(boolean hintLaunchesActivity) {
                setFlag(2, hintLaunchesActivity);
                return this;
            }

            public boolean getHintLaunchesActivity() {
                return (this.mFlags & 2) != 0;
            }

            public WearableExtender setHintDisplayActionInline(boolean hintDisplayInline) {
                setFlag(4, hintDisplayInline);
                return this;
            }

            public boolean getHintDisplayActionInline() {
                return (this.mFlags & 4) != 0;
            }
        }

        /* synthetic */ Action(Icon icon, CharSequence title, PendingIntent intent, Bundle extras, RemoteInput[] remoteInputs, boolean allowGeneratedReplies, Action -this6) {
            this(icon, title, intent, extras, remoteInputs, allowGeneratedReplies);
        }

        /* synthetic */ Action(Parcel in, Action -this1) {
            this(in);
        }

        private Action(Parcel in) {
            boolean z;
            this.mAllowGeneratedReplies = true;
            if (in.readInt() != 0) {
                this.mIcon = (Icon) Icon.CREATOR.createFromParcel(in);
                if (this.mIcon.getType() == 2) {
                    this.icon = this.mIcon.getResId();
                }
            }
            this.title = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            if (in.readInt() == 1) {
                this.actionIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(in);
            }
            this.mExtras = Bundle.setDefusable(in.readBundle(), true);
            this.mRemoteInputs = (RemoteInput[]) in.createTypedArray(RemoteInput.CREATOR);
            if (in.readInt() == 1) {
                z = true;
            } else {
                z = false;
            }
            this.mAllowGeneratedReplies = z;
        }

        @Deprecated
        public Action(int icon, CharSequence title, PendingIntent intent) {
            this(Icon.createWithResource(ProxyInfo.LOCAL_EXCL_LIST, icon), title, intent, new Bundle(), null, true);
        }

        private Action(Icon icon, CharSequence title, PendingIntent intent, Bundle extras, RemoteInput[] remoteInputs, boolean allowGeneratedReplies) {
            this.mAllowGeneratedReplies = true;
            this.mIcon = icon;
            if (icon != null && icon.getType() == 2) {
                this.icon = icon.getResId();
            }
            this.title = title;
            this.actionIntent = intent;
            if (extras == null) {
                extras = new Bundle();
            }
            this.mExtras = extras;
            this.mRemoteInputs = remoteInputs;
            this.mAllowGeneratedReplies = allowGeneratedReplies;
        }

        public Icon getIcon() {
            if (this.mIcon == null && this.icon != 0) {
                this.mIcon = Icon.createWithResource(ProxyInfo.LOCAL_EXCL_LIST, this.icon);
            }
            return this.mIcon;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }

        public boolean getAllowGeneratedReplies() {
            return this.mAllowGeneratedReplies;
        }

        public RemoteInput[] getRemoteInputs() {
            return this.mRemoteInputs;
        }

        public RemoteInput[] getDataOnlyRemoteInputs() {
            return (RemoteInput[]) this.mExtras.getParcelableArray(EXTRA_DATA_ONLY_INPUTS);
        }

        public Action clone() {
            return new Action(getIcon(), this.title, this.actionIntent, this.mExtras == null ? new Bundle() : new Bundle(this.mExtras), getRemoteInputs(), getAllowGeneratedReplies());
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            int i = 1;
            Icon ic = getIcon();
            if (ic != null) {
                out.writeInt(1);
                ic.writeToParcel(out, 0);
            } else {
                out.writeInt(0);
            }
            TextUtils.writeToParcel(this.title, out, flags);
            if (this.actionIntent != null) {
                out.writeInt(1);
                this.actionIntent.writeToParcel(out, flags);
            } else {
                out.writeInt(0);
            }
            out.writeBundle(this.mExtras);
            out.writeTypedArray(this.mRemoteInputs, flags);
            if (!this.mAllowGeneratedReplies) {
                i = 0;
            }
            out.writeInt(i);
        }
    }

    public static abstract class Style {
        private CharSequence mBigContentTitle;
        protected Builder mBuilder;
        protected CharSequence mSummaryText = null;
        protected boolean mSummaryTextSet = false;

        protected void internalSetBigContentTitle(CharSequence title) {
            this.mBigContentTitle = title;
        }

        protected void internalSetSummaryText(CharSequence cs) {
            this.mSummaryText = cs;
            this.mSummaryTextSet = true;
        }

        public void setBuilder(Builder builder) {
            if (this.mBuilder != builder) {
                this.mBuilder = builder;
                if (this.mBuilder != null) {
                    this.mBuilder.setStyle(this);
                }
            }
        }

        protected void checkBuilder() {
            if (this.mBuilder == null) {
                throw new IllegalArgumentException("Style requires a valid Builder object");
            }
        }

        protected RemoteViews getStandardView(int layoutId) {
            checkBuilder();
            CharSequence oldBuilderContentTitle = this.mBuilder.getAllExtras().getCharSequence(Notification.EXTRA_TITLE);
            if (this.mBigContentTitle != null) {
                this.mBuilder.setContentTitle(this.mBigContentTitle);
            }
            RemoteViews contentView = this.mBuilder.applyStandardTemplateWithActions(layoutId);
            this.mBuilder.getAllExtras().putCharSequence(Notification.EXTRA_TITLE, oldBuilderContentTitle);
            if (this.mBigContentTitle == null || !this.mBigContentTitle.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
                contentView.setViewVisibility(16909021, 0);
            } else {
                contentView.setViewVisibility(16909021, 8);
            }
            return contentView;
        }

        public RemoteViews makeContentView(boolean increasedHeight) {
            return null;
        }

        public RemoteViews makeBigContentView() {
            return null;
        }

        public RemoteViews makeHeadsUpContentView(boolean increasedHeight) {
            return null;
        }

        public void addExtras(Bundle extras) {
            if (this.mSummaryTextSet) {
                extras.putCharSequence(Notification.EXTRA_SUMMARY_TEXT, this.mSummaryText);
            }
            if (this.mBigContentTitle != null) {
                extras.putCharSequence(Notification.EXTRA_TITLE_BIG, this.mBigContentTitle);
            }
            extras.putString(Notification.EXTRA_TEMPLATE, getClass().getName());
        }

        protected void restoreFromExtras(Bundle extras) {
            if (extras.containsKey(Notification.EXTRA_SUMMARY_TEXT)) {
                this.mSummaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
                this.mSummaryTextSet = true;
            }
            if (extras.containsKey(Notification.EXTRA_TITLE_BIG)) {
                this.mBigContentTitle = extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
            }
        }

        public Notification buildStyled(Notification wip) {
            addExtras(wip.extras);
            return wip;
        }

        public void purgeResources() {
        }

        public Notification build() {
            checkBuilder();
            return this.mBuilder.build();
        }

        protected boolean hasProgress() {
            return true;
        }

        public boolean hasSummaryInHeader() {
            return true;
        }

        public boolean displayCustomViewInline() {
            return false;
        }

        public void reduceImageSizes(Context context) {
        }
    }

    public static class BigPictureStyle extends Style {
        public static final int MIN_ASHMEM_BITMAP_SIZE = 131072;
        private Icon mBigLargeIcon;
        private boolean mBigLargeIconSet = false;
        private Bitmap mPicture;

        @Deprecated
        public BigPictureStyle(Builder builder) {
            setBuilder(builder);
        }

        public BigPictureStyle setBigContentTitle(CharSequence title) {
            internalSetBigContentTitle(Notification.safeCharSequence(title));
            return this;
        }

        public BigPictureStyle setSummaryText(CharSequence cs) {
            internalSetSummaryText(Notification.safeCharSequence(cs));
            return this;
        }

        public BigPictureStyle bigPicture(Bitmap b) {
            this.mPicture = b;
            return this;
        }

        public BigPictureStyle bigLargeIcon(Bitmap b) {
            Icon icon = null;
            if (b != null) {
                icon = Icon.createWithBitmap(b);
            }
            return bigLargeIcon(icon);
        }

        public BigPictureStyle bigLargeIcon(Icon icon) {
            this.mBigLargeIconSet = true;
            this.mBigLargeIcon = icon;
            return this;
        }

        public void purgeResources() {
            super.purgeResources();
            if (this.mPicture != null && this.mPicture.isMutable() && this.mPicture.getAllocationByteCount() >= 131072) {
                this.mPicture = this.mPicture.createAshmemBitmap();
            }
            if (this.mBigLargeIcon != null) {
                this.mBigLargeIcon.convertToAshmem();
            }
        }

        public void reduceImageSizes(Context context) {
            super.reduceImageSizes(context);
            Resources resources = context.getResources();
            if (this.mPicture != null) {
                this.mPicture = Icon.scaleDownIfNecessary(this.mPicture, resources.getDimensionPixelSize(17105293), resources.getDimensionPixelSize(17105294));
            }
            if (this.mBigLargeIcon != null) {
                int rightIconSize = resources.getDimensionPixelSize(17105299);
                this.mBigLargeIcon.scaleDownIfNecessary(rightIconSize, rightIconSize);
            }
        }

        public RemoteViews makeBigContentView() {
            Icon oldLargeIcon = null;
            Bitmap largeIconLegacy = null;
            if (this.mBigLargeIconSet) {
                oldLargeIcon = this.mBuilder.mN.mLargeIcon;
                this.mBuilder.mN.mLargeIcon = this.mBigLargeIcon;
                largeIconLegacy = this.mBuilder.mN.largeIcon;
                this.mBuilder.mN.largeIcon = null;
            }
            RemoteViews contentView = getStandardView(this.mBuilder.getBigPictureLayoutResource());
            if (this.mSummaryTextSet) {
                contentView.setTextViewText(16909348, this.mBuilder.processLegacyText(this.mSummaryText));
                this.mBuilder.setTextViewColorSecondary(contentView, 16909348);
                contentView.setViewVisibility(16909348, 0);
            }
            this.mBuilder.setContentMinHeight(contentView, this.mBuilder.mN.hasLargeIcon());
            if (this.mBigLargeIconSet) {
                this.mBuilder.mN.mLargeIcon = oldLargeIcon;
                this.mBuilder.mN.largeIcon = largeIconLegacy;
            }
            contentView.setImageViewBitmap(16908750, this.mPicture);
            return contentView;
        }

        public void addExtras(Bundle extras) {
            super.addExtras(extras);
            if (this.mBigLargeIconSet) {
                extras.putParcelable(Notification.EXTRA_LARGE_ICON_BIG, this.mBigLargeIcon);
            }
            extras.putParcelable(Notification.EXTRA_PICTURE, this.mPicture);
        }

        protected void restoreFromExtras(Bundle extras) {
            super.restoreFromExtras(extras);
            if (extras.containsKey(Notification.EXTRA_LARGE_ICON_BIG)) {
                this.mBigLargeIconSet = true;
                this.mBigLargeIcon = (Icon) extras.getParcelable(Notification.EXTRA_LARGE_ICON_BIG);
            }
            this.mPicture = (Bitmap) extras.getParcelable(Notification.EXTRA_PICTURE);
        }

        public boolean hasSummaryInHeader() {
            return false;
        }
    }

    public static class BigTextStyle extends Style {
        private CharSequence mBigText;

        @Deprecated
        public BigTextStyle(Builder builder) {
            setBuilder(builder);
        }

        public BigTextStyle setBigContentTitle(CharSequence title) {
            internalSetBigContentTitle(Notification.safeCharSequence(title));
            return this;
        }

        public BigTextStyle setSummaryText(CharSequence cs) {
            internalSetSummaryText(Notification.safeCharSequence(cs));
            return this;
        }

        public BigTextStyle bigText(CharSequence cs) {
            this.mBigText = Notification.safeCharSequence(cs);
            return this;
        }

        public void addExtras(Bundle extras) {
            super.addExtras(extras);
            extras.putCharSequence(Notification.EXTRA_BIG_TEXT, this.mBigText);
        }

        protected void restoreFromExtras(Bundle extras) {
            super.restoreFromExtras(extras);
            this.mBigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        }

        public RemoteViews makeContentView(boolean increasedHeight) {
            if (!increasedHeight) {
                return super.makeContentView(increasedHeight);
            }
            ArrayList<Action> actions = this.mBuilder.mActions;
            this.mBuilder.mActions = new ArrayList();
            RemoteViews remoteViews = makeBigContentView();
            this.mBuilder.mActions = actions;
            return remoteViews;
        }

        public RemoteViews makeHeadsUpContentView(boolean increasedHeight) {
            if (!increasedHeight || this.mBuilder.mActions.size() <= 0) {
                return super.makeHeadsUpContentView(increasedHeight);
            }
            return makeBigContentView();
        }

        public RemoteViews makeBigContentView() {
            CharSequence text = this.mBuilder.getAllExtras().getCharSequence(Notification.EXTRA_TEXT);
            this.mBuilder.getAllExtras().putCharSequence(Notification.EXTRA_TEXT, null);
            RemoteViews contentView = getStandardView(this.mBuilder.getBigTextLayoutResource());
            this.mBuilder.getAllExtras().putCharSequence(Notification.EXTRA_TEXT, text);
            CharSequence bigTextText = this.mBuilder.processLegacyText(this.mBigText);
            if (TextUtils.isEmpty(bigTextText)) {
                bigTextText = this.mBuilder.processLegacyText(text);
            }
            applyBigTextContentView(this.mBuilder, contentView, bigTextText);
            return contentView;
        }

        static void applyBigTextContentView(Builder builder, RemoteViews contentView, CharSequence bigTextText) {
            contentView.setTextViewText(16908751, bigTextText);
            builder.setTextViewColorSecondary(contentView, 16908751);
            contentView.setViewVisibility(16908751, TextUtils.isEmpty(bigTextText) ? 8 : 0);
            contentView.setBoolean(16908751, "setHasImage", builder.mN.hasLargeIcon());
        }
    }

    public static class Builder {
        public static final String EXTRA_REBUILD_BIG_CONTENT_VIEW_ACTION_COUNT = "android.rebuild.bigViewActionCount";
        public static final String EXTRA_REBUILD_CONTENT_VIEW_ACTION_COUNT = "android.rebuild.contentViewActionCount";
        public static final String EXTRA_REBUILD_HEADS_UP_CONTENT_VIEW_ACTION_COUNT = "android.rebuild.hudViewActionCount";
        private static final String HW_SMALL_ICON_TINT_FOR_SYSTEMUI = "hw_small_icon_tint";
        private static final boolean IS_HONOR_PRODUCT = "HONOR".equals(SystemProperties.get("ro.product.brand"));
        private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", ProxyInfo.LOCAL_EXCL_LIST));
        private static final int LIGHTNESS_TEXT_DIFFERENCE_DARK = -10;
        private static final int LIGHTNESS_TEXT_DIFFERENCE_LIGHT = 20;
        private static final int MAX_ACTION_BUTTONS = 3;
        private static final int SMALL_ICON_CAN_BE_TINT = 1;
        private static final int SMALL_ICON_CAN_NOT_BE_TINT = 0;
        private static final boolean STRIP_AND_REBUILD = false;
        private static final boolean USE_ONLY_TITLE_IN_LOW_PRIORITY_SUMMARY = SystemProperties.getBoolean("notifications.only_title", true);
        private int mActionBarColor;
        private ArrayList<Action> mActions;
        private int mBackgroundColor;
        private int mBackgroundColorHint;
        private int mCachedAmbientColor;
        private int mCachedAmbientColorIsFor;
        private int mCachedContrastColor;
        private int mCachedContrastColorIsFor;
        private NotificationColorUtil mColorUtil;
        private Context mContext;
        private int mForegroundColor;
        private HwNotificationColorUtil mHWColorUtil;
        private boolean mIsLegacy;
        private boolean mIsLegacyInitialized;
        private Notification mN;
        StandardTemplateParams mParams;
        private ArrayList<String> mPersonList;
        private int mPrimaryTextColor;
        private boolean mRebuildStyledRemoteViews;
        private int mSecondaryTextColor;
        private Style mStyle;
        private int mTextColorsAreForBackground;
        private Bundle mUserExtras;

        public Builder(Context context, String channelId) {
            this(context, (Notification) null);
            this.mN.mChannelId = channelId;
        }

        @Deprecated
        public Builder(Context context) {
            this(context, (Notification) null);
        }

        public Builder(Context context, Notification toAdopt) {
            this.mUserExtras = new Bundle();
            this.mActions = new ArrayList(3);
            this.mPersonList = new ArrayList();
            this.mCachedContrastColor = 1;
            this.mCachedContrastColorIsFor = 1;
            this.mCachedAmbientColor = 1;
            this.mCachedAmbientColorIsFor = 1;
            this.mParams = new StandardTemplateParams();
            this.mTextColorsAreForBackground = 1;
            this.mPrimaryTextColor = 1;
            this.mSecondaryTextColor = 1;
            this.mActionBarColor = 1;
            this.mBackgroundColor = 1;
            this.mForegroundColor = 1;
            this.mBackgroundColorHint = 1;
            this.mContext = context;
            if (toAdopt == null) {
                this.mN = new Notification();
                if (context.getApplicationInfo().targetSdkVersion < 24) {
                    this.mN.extras.putBoolean(Notification.EXTRA_SHOW_WHEN, true);
                }
                this.mN.priority = 0;
                this.mN.visibility = 0;
                return;
            }
            this.mN = toAdopt;
            if (this.mN.actions != null) {
                Collections.addAll(this.mActions, this.mN.actions);
            }
            if (this.mN.extras.containsKey(Notification.EXTRA_PEOPLE)) {
                String[] extra_people_array = this.mN.extras.getStringArray(Notification.EXTRA_PEOPLE);
                if (extra_people_array != null) {
                    Collections.addAll(this.mPersonList, extra_people_array);
                }
            }
            if (this.mN.getSmallIcon() == null && this.mN.icon != 0) {
                setSmallIcon(this.mN.icon);
            }
            if (this.mN.getLargeIcon() == null && this.mN.largeIcon != null) {
                setLargeIcon(this.mN.largeIcon);
            }
            String templateClass = this.mN.extras.getString(Notification.EXTRA_TEMPLATE);
            if (!TextUtils.isEmpty(templateClass)) {
                Class<? extends Style> styleClass = Notification.getNotificationStyleClass(templateClass);
                if (styleClass == null) {
                    Log.d(Notification.TAG, "Unknown style class: " + templateClass);
                    return;
                }
                try {
                    Constructor<? extends Style> ctor = styleClass.getDeclaredConstructor(new Class[0]);
                    ctor.setAccessible(true);
                    Style style = (Style) ctor.newInstance(new Object[0]);
                    style.restoreFromExtras(this.mN.extras);
                    if (style != null) {
                        setStyle(style);
                    }
                } catch (Throwable t) {
                    Log.e(Notification.TAG, "Could not create Style", t);
                }
            }
        }

        private NotificationColorUtil getColorUtil() {
            if (this.mColorUtil == null) {
                this.mColorUtil = NotificationColorUtil.getInstance(this.mContext);
            }
            return this.mColorUtil;
        }

        private HwNotificationColorUtil getHWColorUtil() {
            if (this.mHWColorUtil == null) {
                this.mHWColorUtil = HwNotificationColorUtil.getInstance(this.mContext);
            }
            return this.mHWColorUtil;
        }

        public Builder setShortcutId(String shortcutId) {
            this.mN.mShortcutId = shortcutId;
            return this;
        }

        public Builder setBadgeIconType(int icon) {
            this.mN.mBadgeIcon = icon;
            return this;
        }

        public Builder setGroupAlertBehavior(int groupAlertBehavior) {
            this.mN.mGroupAlertBehavior = groupAlertBehavior;
            return this;
        }

        @Deprecated
        public Builder setChannel(String channelId) {
            this.mN.mChannelId = channelId;
            return this;
        }

        public Builder setChannelId(String channelId) {
            this.mN.mChannelId = channelId;
            return this;
        }

        @Deprecated
        public Builder setTimeout(long durationMs) {
            this.mN.mTimeout = durationMs;
            return this;
        }

        public Builder setTimeoutAfter(long durationMs) {
            this.mN.mTimeout = durationMs;
            return this;
        }

        public Builder setWhen(long when) {
            this.mN.when = when;
            return this;
        }

        public Builder setShowWhen(boolean show) {
            this.mN.extras.putBoolean(Notification.EXTRA_SHOW_WHEN, show);
            return this;
        }

        public Builder setUsesChronometer(boolean b) {
            this.mN.extras.putBoolean(Notification.EXTRA_SHOW_CHRONOMETER, b);
            return this;
        }

        public Builder setChronometerCountDown(boolean countDown) {
            this.mN.extras.putBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN, countDown);
            return this;
        }

        public Builder setSmallIcon(int icon) {
            Icon createWithResource;
            if (icon != 0) {
                createWithResource = Icon.createWithResource(this.mContext, icon);
            } else {
                createWithResource = null;
            }
            return setSmallIcon(createWithResource);
        }

        public Builder setSmallIcon(int icon, int level) {
            this.mN.iconLevel = level;
            return setSmallIcon(icon);
        }

        public Builder setSmallIcon(Icon icon) {
            this.mN.setSmallIcon(icon);
            if (icon != null && icon.getType() == 2) {
                this.mN.icon = icon.getResId();
            }
            return this;
        }

        public Builder setContentTitle(CharSequence title) {
            this.mN.extras.putCharSequence(Notification.EXTRA_TITLE, Notification.safeCharSequence(title));
            return this;
        }

        public Builder setContentText(CharSequence text) {
            this.mN.extras.putCharSequence(Notification.EXTRA_TEXT, Notification.safeCharSequence(text));
            return this;
        }

        public Builder setSubText(CharSequence text) {
            this.mN.extras.putCharSequence(Notification.EXTRA_SUB_TEXT, Notification.safeCharSequence(text));
            return this;
        }

        public Builder setSettingsText(CharSequence text) {
            this.mN.mSettingsText = Notification.safeCharSequence(text);
            return this;
        }

        public Builder setRemoteInputHistory(CharSequence[] text) {
            if (text == null) {
                this.mN.extras.putCharSequenceArray(Notification.EXTRA_REMOTE_INPUT_HISTORY, null);
            } else {
                int N = Math.min(5, text.length);
                CharSequence[] safe = new CharSequence[N];
                for (int i = 0; i < N; i++) {
                    safe[i] = Notification.safeCharSequence(text[i]);
                }
                this.mN.extras.putCharSequenceArray(Notification.EXTRA_REMOTE_INPUT_HISTORY, safe);
            }
            return this;
        }

        public Builder setNumber(int number) {
            this.mN.number = number;
            return this;
        }

        @Deprecated
        public Builder setContentInfo(CharSequence info) {
            this.mN.extras.putCharSequence(Notification.EXTRA_INFO_TEXT, Notification.safeCharSequence(info));
            return this;
        }

        public Builder setProgress(int max, int progress, boolean indeterminate) {
            this.mN.extras.putInt(Notification.EXTRA_PROGRESS, progress);
            this.mN.extras.putInt(Notification.EXTRA_PROGRESS_MAX, max);
            this.mN.extras.putBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, indeterminate);
            return this;
        }

        public Builder setAppName(CharSequence appName) {
            this.mN.extras.putCharSequence(Notification.EXTRA_APP_NAME, Notification.safeCharSequence(appName));
            return this;
        }

        public Builder setShowActionIcon(boolean showActionIcon) {
            this.mN.extras.putBoolean(Notification.EXTRA_SHOW_ACTION_ICON, showActionIcon);
            return this;
        }

        @Deprecated
        public Builder setContent(RemoteViews views) {
            return setCustomContentView(views);
        }

        public Builder setCustomContentView(RemoteViews contentView) {
            this.mN.contentView = contentView;
            return this;
        }

        public Builder setCustomBigContentView(RemoteViews contentView) {
            this.mN.bigContentView = contentView;
            return this;
        }

        public Builder setCustomHeadsUpContentView(RemoteViews contentView) {
            this.mN.headsUpContentView = contentView;
            return this;
        }

        public Builder setContentIntent(PendingIntent intent) {
            this.mN.contentIntent = intent;
            return this;
        }

        public Builder setDeleteIntent(PendingIntent intent) {
            this.mN.deleteIntent = intent;
            return this;
        }

        public Builder setFullScreenIntent(PendingIntent intent, boolean highPriority) {
            this.mN.fullScreenIntent = intent;
            setFlag(128, highPriority);
            return this;
        }

        public Builder setTicker(CharSequence tickerText) {
            this.mN.tickerText = Notification.safeCharSequence(tickerText);
            return this;
        }

        @Deprecated
        public Builder setTicker(CharSequence tickerText, RemoteViews views) {
            setTicker(tickerText);
            return this;
        }

        public Builder setLargeIcon(Bitmap b) {
            Icon icon = null;
            if (b != null) {
                icon = Icon.createWithBitmap(b);
            }
            return setLargeIcon(icon);
        }

        public Builder setLargeIcon(Icon icon) {
            this.mN.mLargeIcon = icon;
            this.mN.extras.putParcelable(Notification.EXTRA_LARGE_ICON, icon);
            return this;
        }

        @Deprecated
        public Builder setSound(Uri sound) {
            this.mN.sound = sound;
            this.mN.audioAttributes = Notification.AUDIO_ATTRIBUTES_DEFAULT;
            return this;
        }

        @Deprecated
        public Builder setSound(Uri sound, int streamType) {
            PlayerBase.deprecateStreamTypeForPlayback(streamType, Notification.TAG, "setSound()");
            this.mN.sound = sound;
            this.mN.audioStreamType = streamType;
            return this;
        }

        @Deprecated
        public Builder setSound(Uri sound, AudioAttributes audioAttributes) {
            this.mN.sound = sound;
            this.mN.audioAttributes = audioAttributes;
            return this;
        }

        @Deprecated
        public Builder setVibrate(long[] pattern) {
            this.mN.vibrate = pattern;
            return this;
        }

        @Deprecated
        public Builder setLights(int argb, int onMs, int offMs) {
            this.mN.ledARGB = argb;
            this.mN.ledOnMS = onMs;
            this.mN.ledOffMS = offMs;
            if (!(onMs == 0 && offMs == 0)) {
                Notification notification = this.mN;
                notification.flags |= 1;
            }
            return this;
        }

        public Builder setOngoing(boolean ongoing) {
            setFlag(2, ongoing);
            return this;
        }

        public Builder setColorized(boolean colorize) {
            this.mN.extras.putBoolean(Notification.EXTRA_COLORIZED, colorize);
            return this;
        }

        public Builder setOnlyAlertOnce(boolean onlyAlertOnce) {
            setFlag(8, onlyAlertOnce);
            return this;
        }

        public Builder setAutoCancel(boolean autoCancel) {
            setFlag(16, autoCancel);
            return this;
        }

        public Builder setLocalOnly(boolean localOnly) {
            setFlag(256, localOnly);
            return this;
        }

        @Deprecated
        public Builder setDefaults(int defaults) {
            this.mN.defaults = defaults;
            return this;
        }

        @Deprecated
        public Builder setPriority(int pri) {
            this.mN.priority = pri;
            return this;
        }

        public Builder setCategory(String category) {
            this.mN.category = category;
            return this;
        }

        public Builder addPerson(String uri) {
            this.mPersonList.add(uri);
            return this;
        }

        public Builder setGroup(String groupKey) {
            this.mN.mGroupKey = groupKey;
            return this;
        }

        public Builder setGroupSummary(boolean isGroupSummary) {
            setFlag(512, isGroupSummary);
            return this;
        }

        public Builder setSortKey(String sortKey) {
            this.mN.mSortKey = sortKey;
            return this;
        }

        public Builder addExtras(Bundle extras) {
            if (extras != null) {
                this.mUserExtras.putAll(extras);
            }
            return this;
        }

        public Builder setExtras(Bundle extras) {
            if (extras != null) {
                this.mUserExtras = extras;
            }
            return this;
        }

        public Bundle getExtras() {
            return this.mUserExtras;
        }

        private Bundle getAllExtras() {
            Bundle saveExtras = (Bundle) this.mUserExtras.clone();
            saveExtras.putAll(this.mN.extras);
            return saveExtras;
        }

        @Deprecated
        public Builder addAction(int icon, CharSequence title, PendingIntent intent) {
            this.mActions.add(new Action(icon, Notification.safeCharSequence(title), intent));
            return this;
        }

        public Builder addAction(Action action) {
            if (action != null) {
                this.mActions.add(action);
            }
            return this;
        }

        public Builder setActions(Action... actions) {
            this.mActions.clear();
            for (int i = 0; i < actions.length; i++) {
                if (actions[i] != null) {
                    this.mActions.add(actions[i]);
                }
            }
            return this;
        }

        public Builder setStyle(Style style) {
            if (this.mStyle != style) {
                this.mStyle = style;
                if (this.mStyle != null) {
                    this.mStyle.setBuilder(this);
                    this.mN.extras.putString(Notification.EXTRA_TEMPLATE, style.getClass().getName());
                } else {
                    this.mN.extras.remove(Notification.EXTRA_TEMPLATE);
                }
            }
            return this;
        }

        public Builder setVisibility(int visibility) {
            this.mN.visibility = visibility;
            return this;
        }

        public Builder setPublicVersion(Notification n) {
            if (n != null) {
                this.mN.publicVersion = new Notification();
                n.cloneInto(this.mN.publicVersion, true);
            } else {
                this.mN.publicVersion = null;
            }
            return this;
        }

        public Builder extend(Extender extender) {
            extender.extend(this);
            return this;
        }

        public Builder setFlag(int mask, boolean value) {
            Notification notification;
            if (value) {
                notification = this.mN;
                notification.flags |= mask;
            } else {
                notification = this.mN;
                notification.flags &= ~mask;
            }
            return this;
        }

        public Builder setColor(int argb) {
            this.mN.color = argb;
            sanitizeColor();
            return this;
        }

        private Drawable getProfileBadgeDrawable() {
            Drawable badge = HwThemeManager.getHwBadgeDrawable(this.mN, this.mContext, null);
            if (badge != null) {
                return badge;
            }
            if (this.mContext.getUserId() == 0) {
                return null;
            }
            return this.mContext.getPackageManager().getUserBadgeForDensityNoBackground(new UserHandle(this.mContext.getUserId()), 0);
        }

        private Bitmap getProfileBadge() {
            Drawable badge = getProfileBadgeDrawable();
            if (badge == null) {
                return null;
            }
            int size = this.mContext.getResources().getDimensionPixelSize(17105149);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            badge.setBounds(0, 0, size, size);
            badge.draw(canvas);
            return bitmap;
        }

        private void bindProfileBadge(RemoteViews contentView) {
            Bitmap profileBadge = getProfileBadge();
            if (profileBadge != null) {
                contentView.setImageViewBitmap(16909186, profileBadge);
                contentView.setViewVisibility(16909186, 0);
                if (isColorized()) {
                    contentView.setDrawableParameters(16909186, false, -1, getPrimaryTextColor(), Mode.SRC_ATOP, -1);
                }
            }
        }

        private void resetStandardTemplate(RemoteViews contentView) {
            resetNotificationHeader(contentView);
            resetContentMargins(contentView);
            contentView.setViewVisibility(16909218, 8);
            contentView.setViewVisibility(R.id.title, 8);
            contentView.setTextViewText(R.id.title, null);
            contentView.setViewVisibility(16909348, 8);
            contentView.setTextViewText(16909348, null);
            contentView.setViewVisibility(16909375, 8);
            contentView.setTextViewText(16909375, null);
        }

        private void resetNotificationHeader(RemoteViews contentView) {
            contentView.setBoolean(16909105, "setExpanded", false);
            contentView.setTextViewText(16908726, null);
            contentView.setViewVisibility(16908794, 8);
            contentView.setViewVisibility(16908935, 8);
            contentView.setTextViewText(16908935, null);
            contentView.setViewVisibility(16908936, 8);
            contentView.setViewVisibility(16909381, 8);
            contentView.setViewVisibility(16909377, 8);
            contentView.setImageViewIcon(16909186, null);
            contentView.setViewVisibility(16909186, 8);
        }

        private void resetContentMargins(RemoteViews contentView) {
            contentView.setViewLayoutMarginEndDimen(16909021, 0);
            contentView.setViewLayoutMarginEndDimen(16909348, 0);
        }

        private RemoteViews applyStandardTemplate(int resId) {
            return applyStandardTemplate(resId, this.mParams.reset().fillTextsFrom(this));
        }

        private RemoteViews applyStandardTemplate(int resId, boolean hasProgress) {
            return applyStandardTemplate(resId, this.mParams.reset().hasProgress(hasProgress).fillTextsFrom(this));
        }

        private RemoteViews applyStandardTemplate(int resId, StandardTemplateParams p) {
            RemoteViews contentView = new BuilderRemoteViews(this.mContext.getApplicationInfo(), resId);
            resetStandardTemplate(contentView);
            Bundle ex = this.mN.extras;
            updateBackgroundColor(contentView);
            bindNotificationHeader(contentView, p.ambient);
            bindLargeIcon(contentView);
            boolean showProgress = handleProgressBar(p.hasProgress, contentView, ex);
            if (p.title != null) {
                int i;
                contentView.setViewVisibility(R.id.title, 0);
                contentView.setTextViewText(R.id.title, p.title);
                if (!p.ambient) {
                    setTextViewColorPrimary(contentView, R.id.title);
                }
                if (showProgress) {
                    i = -2;
                } else {
                    i = -1;
                }
                contentView.setViewLayoutWidth(R.id.title, i);
            }
            if (p.text != null) {
                int textId;
                if (showProgress) {
                    textId = 16909375;
                } else {
                    textId = 16909348;
                }
                contentView.setTextViewText(textId, p.text);
                if (!p.ambient) {
                    setTextViewColorSecondary(contentView, textId);
                }
                contentView.setViewVisibility(textId, 0);
            }
            setContentMinHeight(contentView, !showProgress ? this.mN.hasLargeIcon() : true);
            return contentView;
        }

        private void setTextViewColorPrimary(RemoteViews contentView, int id) {
            ensureColors();
            contentView.setTextColor(id, this.mPrimaryTextColor);
        }

        public int getPrimaryTextColor() {
            ensureColors();
            return this.mPrimaryTextColor;
        }

        public int getSecondaryTextColor() {
            ensureColors();
            return this.mSecondaryTextColor;
        }

        private int getActionBarColor() {
            ensureColors();
            return this.mActionBarColor;
        }

        private int getActionBarColorDeEmphasized() {
            return NotificationColorUtil.getShiftedColor(getBackgroundColor(), 12);
        }

        private void setTextViewColorSecondary(RemoteViews contentView, int id) {
            ensureColors();
            contentView.setTextColor(id, this.mSecondaryTextColor);
        }

        private void ensureColors() {
            int backgroundColor = getBackgroundColor();
            if (this.mPrimaryTextColor == 1 || this.mSecondaryTextColor == 1 || this.mActionBarColor == 1 || this.mTextColorsAreForBackground != backgroundColor) {
                this.mTextColorsAreForBackground = backgroundColor;
                if (this.mForegroundColor == 1 || (isColorized() ^ 1) != 0) {
                    this.mPrimaryTextColor = NotificationColorUtil.resolvePrimaryColor(this.mContext, backgroundColor);
                    this.mSecondaryTextColor = NotificationColorUtil.resolveSecondaryColor(this.mContext, backgroundColor);
                    if (backgroundColor != 0 && (this.mBackgroundColorHint != 1 || isColorized())) {
                        this.mPrimaryTextColor = NotificationColorUtil.findAlphaToMeetContrast(this.mPrimaryTextColor, backgroundColor, 4.5d);
                        this.mSecondaryTextColor = NotificationColorUtil.findAlphaToMeetContrast(this.mSecondaryTextColor, backgroundColor, 4.5d);
                    }
                } else {
                    boolean backgroundLight;
                    double backLum = NotificationColorUtil.calculateLuminance(backgroundColor);
                    double textLum = NotificationColorUtil.calculateLuminance(this.mForegroundColor);
                    double contrast = NotificationColorUtil.calculateContrast(this.mForegroundColor, backgroundColor);
                    if (backLum > textLum && NotificationColorUtil.satisfiesTextContrast(backgroundColor, -16777216)) {
                        backgroundLight = true;
                    } else if (backLum <= textLum) {
                        backgroundLight = NotificationColorUtil.satisfiesTextContrast(backgroundColor, -1) ^ 1;
                    } else {
                        backgroundLight = false;
                    }
                    if (contrast >= 4.5d) {
                        int i;
                        this.mPrimaryTextColor = this.mForegroundColor;
                        int i2 = this.mPrimaryTextColor;
                        if (backgroundLight) {
                            i = 20;
                        } else {
                            i = -10;
                        }
                        this.mSecondaryTextColor = NotificationColorUtil.changeColorLightness(i2, i);
                        if (NotificationColorUtil.calculateContrast(this.mSecondaryTextColor, backgroundColor) < 4.5d) {
                            if (backgroundLight) {
                                this.mSecondaryTextColor = NotificationColorUtil.findContrastColor(this.mSecondaryTextColor, backgroundColor, true, 4.5d);
                            } else {
                                this.mSecondaryTextColor = NotificationColorUtil.findContrastColorAgainstDark(this.mSecondaryTextColor, backgroundColor, true, 4.5d);
                            }
                            i2 = this.mSecondaryTextColor;
                            if (backgroundLight) {
                                i = -20;
                            } else {
                                i = 10;
                            }
                            this.mPrimaryTextColor = NotificationColorUtil.changeColorLightness(i2, i);
                        }
                    } else if (backgroundLight) {
                        this.mSecondaryTextColor = NotificationColorUtil.findContrastColor(this.mForegroundColor, backgroundColor, true, 4.5d);
                        this.mPrimaryTextColor = NotificationColorUtil.changeColorLightness(this.mSecondaryTextColor, -20);
                    } else {
                        this.mSecondaryTextColor = NotificationColorUtil.findContrastColorAgainstDark(this.mForegroundColor, backgroundColor, true, 4.5d);
                        this.mPrimaryTextColor = NotificationColorUtil.changeColorLightness(this.mSecondaryTextColor, 10);
                    }
                }
                this.mActionBarColor = NotificationColorUtil.resolveActionBarColor(this.mContext, backgroundColor);
            }
        }

        private void updateBackgroundColor(RemoteViews contentView) {
            if (isColorized()) {
                contentView.setInt(16909329, "setBackgroundColor", getBackgroundColor());
            } else {
                contentView.setInt(16909329, "setBackgroundResource", 0);
            }
        }

        void setContentMinHeight(RemoteViews remoteView, boolean hasMinHeight) {
            int minHeight = 0;
            if (hasMinHeight) {
                minHeight = this.mContext.getResources().getDimensionPixelSize(17105169);
            }
            remoteView.setInt(16909106, "setMinimumHeight", minHeight);
        }

        private boolean handleProgressBar(boolean hasProgress, RemoteViews contentView, Bundle ex) {
            int max = ex.getInt(Notification.EXTRA_PROGRESS_MAX, 0);
            int progress = ex.getInt(Notification.EXTRA_PROGRESS, 0);
            boolean ind = ex.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE);
            if (!hasProgress || (max == 0 && !ind)) {
                contentView.setViewVisibility(R.id.progress, 8);
                return false;
            }
            contentView.setViewVisibility(R.id.progress, 0);
            contentView.setProgressBar(R.id.progress, max, progress, ind);
            contentView.setProgressBackgroundTintList(R.id.progress, ColorStateList.valueOf(this.mContext.getColor(17170674)));
            if (this.mN.color != 0) {
                ColorStateList colorStateList = ColorStateList.valueOf(resolveContrastColor());
                contentView.setProgressTintList(R.id.progress, colorStateList);
                contentView.setProgressIndeterminateTintList(R.id.progress, colorStateList);
            }
            return true;
        }

        private void bindLargeIcon(RemoteViews contentView) {
            if (this.mN.mLargeIcon == null && this.mN.largeIcon != null) {
                this.mN.mLargeIcon = Icon.createWithBitmap(this.mN.largeIcon);
            }
            if (this.mN.mLargeIcon != null) {
                contentView.setViewVisibility(16909218, 0);
                contentView.setImageViewIcon(16909218, this.mN.mLargeIcon);
                processLargeLegacyIcon(this.mN.mLargeIcon, contentView);
                contentView.setViewLayoutMarginEndDimen(16909021, 17105156);
                contentView.setViewLayoutMarginEndDimen(16909348, 17105156);
                contentView.setViewLayoutMarginEndDimen(R.id.progress, 17105156);
            }
        }

        private void bindNotificationHeader(RemoteViews contentView, boolean ambient) {
            bindSmallIcon(contentView, ambient);
            bindHeaderAppName(contentView, ambient);
            if (!ambient) {
                bindHeaderText(contentView);
                bindHeaderChronometerAndTime(contentView);
                bindProfileBadge(contentView);
            }
            bindExpandButton(contentView);
        }

        private void bindExpandButton(RemoteViews contentView) {
            int color;
            if (this.mN.color != 0) {
                color = getPrimaryHighlightColor();
            } else if (!IS_HONOR_PRODUCT || (IS_TABLET ^ 1) == 0) {
                color = this.mContext.getColor(33882452);
            } else {
                color = this.mContext.getColor(33882451);
            }
            contentView.setDrawableParameters(16908860, false, -1, color, Mode.SRC_ATOP, -1);
            contentView.setInt(16909105, "setOriginalNotificationColor", color);
        }

        private int getPrimaryHighlightColor() {
            return isColorized() ? getPrimaryTextColor() : resolveContrastColor();
        }

        private void bindHeaderChronometerAndTime(RemoteViews contentView) {
            if (showsTimeOrChronometer()) {
                contentView.setViewVisibility(16909381, 0);
                setTextViewColorSecondary(contentView, 16909381);
                if (this.mN.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER)) {
                    contentView.setViewVisibility(16908794, 0);
                    contentView.setLong(16908794, "setBase", this.mN.when + (SystemClock.elapsedRealtime() - System.currentTimeMillis()));
                    contentView.setBoolean(16908794, "setStarted", true);
                    contentView.setChronometerCountDown(16908794, this.mN.extras.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN));
                    setTextViewColorSecondary(contentView, 16908794);
                    return;
                }
                contentView.setViewVisibility(16909377, 0);
                contentView.setLong(16909377, "setTime", this.mN.creationTime);
                setTextViewColorSecondary(contentView, 16909377);
                return;
            }
            contentView.setLong(16909377, "setTime", this.mN.creationTime);
        }

        private void bindHeaderText(RemoteViews contentView) {
            CharSequence headerText = this.mN.extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
            if (headerText == null && this.mStyle != null && this.mStyle.mSummaryTextSet && this.mStyle.hasSummaryInHeader()) {
                headerText = this.mStyle.mSummaryText;
            }
            if (headerText == null && this.mContext.getApplicationInfo().targetSdkVersion < 24 && this.mN.extras.getCharSequence(Notification.EXTRA_INFO_TEXT) != null) {
                headerText = this.mN.extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
            }
            if (headerText != null) {
                contentView.setTextViewText(16908935, processLegacyText(headerText));
                setTextViewColorSecondary(contentView, 16908935);
                contentView.setViewVisibility(16908935, 0);
                contentView.setViewVisibility(16908936, 0);
                setTextViewColorSecondary(contentView, 16908936);
            }
        }

        public String loadHeaderAppName() {
            CharSequence name = null;
            CharSequence appName = this.mN.extras.getCharSequence(Notification.EXTRA_APP_NAME);
            if (appName != null) {
                return String.valueOf(appName);
            }
            PackageManager pm = this.mContext.getPackageManager();
            if (this.mN.extras.containsKey(Notification.EXTRA_SUBSTITUTE_APP_NAME)) {
                String pkg = this.mContext.getPackageName();
                String subName = this.mN.extras.getString(Notification.EXTRA_SUBSTITUTE_APP_NAME);
                if (pm.checkPermission(permission.SUBSTITUTE_NOTIFICATION_APP_NAME, pkg) == 0) {
                    name = subName;
                } else {
                    Log.w(Notification.TAG, "warning: pkg " + pkg + " attempting to substitute app name '" + subName + "' without holding perm " + permission.SUBSTITUTE_NOTIFICATION_APP_NAME);
                }
            }
            if (TextUtils.isEmpty(name)) {
                name = pm.getApplicationLabel(this.mContext.getApplicationInfo());
            }
            if (TextUtils.isEmpty(name)) {
                return null;
            }
            return String.valueOf(name);
        }

        private void bindHeaderAppName(RemoteViews contentView, boolean ambient) {
            contentView.setTextViewText(16908726, loadHeaderAppName());
            if (!isColorized() || (ambient ^ 1) == 0) {
                contentView.setTextColor(16908726, ambient ? resolveAmbientColor() : resolveContrastColor());
            } else {
                setTextViewColorPrimary(contentView, 16908726);
            }
            String style = this.mN.extras.getString(Notification.EXTRA_TEMPLATE);
            if (!(style != null ? style.contains("MediaStyle") : false)) {
                contentView.setTextColor(16908726, -1291845632);
            }
        }

        private void bindSmallIcon(RemoteViews contentView, boolean ambient) {
            if (this.mN.mSmallIcon == null && this.mN.icon != 0) {
                this.mN.mSmallIcon = Icon.createWithResource(this.mContext, this.mN.icon);
            }
            contentView.setImageViewIcon(R.id.icon, this.mN.mSmallIcon);
            contentView.setDrawableParameters(R.id.icon, false, -1, -1, null, this.mN.iconLevel);
            processSmallIconColor(this.mN.mSmallIcon, contentView, ambient);
        }

        private boolean showsTimeOrChronometer() {
            return !this.mN.showsTime() ? this.mN.showsChronometer() : true;
        }

        private void resetStandardTemplateWithActions(RemoteViews big) {
            big.setViewVisibility(16908692, 8);
            big.removeAllViews(16908692);
            big.setViewVisibility(16909107, 8);
            big.setTextViewText(16909108, null);
            big.setViewVisibility(16909109, 8);
            big.setTextViewText(16909109, null);
            big.setViewVisibility(16909110, 8);
            big.setTextViewText(16909110, null);
            big.setViewLayoutMarginBottomDimen(16909103, 0);
        }

        private RemoteViews applyStandardTemplateWithActions(int layoutId) {
            return applyStandardTemplateWithActions(layoutId, this.mParams.reset().fillTextsFrom(this));
        }

        private RemoteViews applyStandardTemplateWithActions(int layoutId, StandardTemplateParams p) {
            RemoteViews big = applyStandardTemplate(layoutId, p);
            resetStandardTemplateWithActions(big);
            boolean validRemoteInput = false;
            int N = this.mActions.size();
            boolean emphazisedMode = this.mN.fullScreenIntent != null ? p.ambient ^ 1 : false;
            big.setBoolean(16908692, "setEmphasizedMode", emphazisedMode);
            if (N > 0) {
                big.setViewVisibility(16908693, 0);
                big.setViewVisibility(16908692, 0);
                if (p.ambient) {
                    big.setInt(16908692, "setBackgroundColor", 0);
                } else if (isColorized()) {
                    big.setInt(16908692, "setBackgroundColor", getActionBarColor());
                } else {
                    big.setInt(16908692, "setBackgroundColor", this.mContext.getColor(17170665));
                }
                big.setViewLayoutMarginBottomDimen(16909103, 17105148);
                if (N > 3) {
                    N = 3;
                }
                for (int i = 0; i < N; i++) {
                    Action action = (Action) this.mActions.get(i);
                    if (action != null) {
                        validRemoteInput |= hasValidRemoteInput(action);
                        big.addView(16908692, generateActionButton(action, emphazisedMode, i % 2 != 0, p.ambient));
                    }
                }
            } else {
                big.setViewVisibility(16908693, 8);
            }
            CharSequence[] replyText = this.mN.extras.getCharSequenceArray(Notification.EXTRA_REMOTE_INPUT_HISTORY);
            if (!(p.ambient || !validRemoteInput || replyText == null || replyText.length <= 0 || (TextUtils.isEmpty(replyText[0]) ^ 1) == 0)) {
                big.setViewVisibility(16909107, 0);
                big.setTextViewText(16909108, replyText[0]);
                setTextViewColorSecondary(big, 16909108);
                if (replyText.length > 1 && (TextUtils.isEmpty(replyText[1]) ^ 1) != 0) {
                    big.setViewVisibility(16909109, 0);
                    big.setTextViewText(16909109, replyText[1]);
                    setTextViewColorSecondary(big, 16909109);
                    if (replyText.length > 2 && (TextUtils.isEmpty(replyText[2]) ^ 1) != 0) {
                        big.setViewVisibility(16909110, 0);
                        big.setTextViewText(16909110, replyText[2]);
                        setTextViewColorSecondary(big, 16909110);
                    }
                }
            }
            return big;
        }

        private boolean hasValidRemoteInput(Action action) {
            if (TextUtils.isEmpty(action.title) || action.actionIntent == null) {
                return false;
            }
            RemoteInput[] remoteInputs = action.getRemoteInputs();
            if (remoteInputs == null) {
                return false;
            }
            for (RemoteInput r : remoteInputs) {
                CharSequence[] choices = r.getChoices();
                if (r.getAllowFreeFormInput() || (choices != null && choices.length != 0)) {
                    return true;
                }
            }
            return false;
        }

        public RemoteViews createContentView() {
            return createContentView(false);
        }

        public RemoteViews createContentView(boolean increasedHeight) {
            if (this.mN.contentView != null && useExistingRemoteView()) {
                return this.mN.contentView;
            }
            if (this.mStyle != null) {
                RemoteViews styleView = this.mStyle.makeContentView(increasedHeight);
                if (styleView != null) {
                    return styleView;
                }
            }
            return applyStandardTemplate(getBaseLayoutResource());
        }

        private boolean useExistingRemoteView() {
            if (this.mStyle == null) {
                return true;
            }
            if (this.mStyle.displayCustomViewInline()) {
                return false;
            }
            return this.mRebuildStyledRemoteViews ^ 1;
        }

        public RemoteViews createLineView() {
            RemoteViews contentView = new BuilderRemoteViews(this.mContext.getApplicationInfo(), 34013287);
            contentView.setOnClickPendingIntent(34603045, this.mN.contentIntent != null ? this.mN.contentIntent : this.mN.fullScreenIntent);
            contentView.setTextViewText(R.id.title, null);
            contentView.setTextViewText(16909348, null);
            contentView.setTextViewText(16908726, null);
            contentView.setImageViewResource(R.id.icon, 0);
            bindSmallIcon(contentView, false);
            bindHeaderAppName(contentView, false);
            CharSequence title = processLegacyText(this.mN.extras.getCharSequence(Notification.EXTRA_TITLE));
            CharSequence text = processLegacyText(this.mN.extras.getCharSequence(Notification.EXTRA_TEXT));
            contentView.setTextViewText(R.id.title, title);
            contentView.setTextViewText(16909348, text);
            return contentView;
        }

        public RemoteViews createBigContentView() {
            RemoteViews result = null;
            if (this.mN.bigContentView != null && useExistingRemoteView()) {
                return this.mN.bigContentView;
            }
            if (this.mStyle != null) {
                result = this.mStyle.makeBigContentView();
                hideLine1Text(result);
            } else if (this.mActions.size() != 0) {
                result = applyStandardTemplateWithActions(getBigBaseLayoutResource());
            }
            makeHeaderExpanded(result);
            return result;
        }

        public RemoteViews makeNotificationHeader(boolean ambient) {
            int i;
            Boolean colorized = (Boolean) this.mN.extras.get(Notification.EXTRA_COLORIZED);
            this.mN.extras.putBoolean(Notification.EXTRA_COLORIZED, false);
            ApplicationInfo applicationInfo = this.mContext.getApplicationInfo();
            if (ambient) {
                i = 17367181;
            } else {
                i = 17367182;
            }
            RemoteViews header = new BuilderRemoteViews(applicationInfo, i);
            resetNotificationHeader(header);
            bindNotificationHeader(header, ambient);
            if (colorized != null) {
                this.mN.extras.putBoolean(Notification.EXTRA_COLORIZED, colorized.booleanValue());
            } else {
                this.mN.extras.remove(Notification.EXTRA_COLORIZED);
            }
            return header;
        }

        public RemoteViews makeAmbientNotification() {
            return applyStandardTemplateWithActions(17367183, this.mParams.reset().ambient(true).fillTextsFrom(this).hasProgress(false));
        }

        private void hideLine1Text(RemoteViews result) {
            if (result != null) {
                result.setViewVisibility(16909375, 8);
            }
        }

        public static void makeHeaderExpanded(RemoteViews result) {
            if (result != null) {
                result.setBoolean(16909105, "setExpanded", true);
            }
        }

        public RemoteViews createHeadsUpContentView(boolean increasedHeight) {
            if (this.mN.headsUpContentView != null && useExistingRemoteView()) {
                return this.mN.headsUpContentView;
            }
            if (this.mStyle != null) {
                RemoteViews styleView = this.mStyle.makeHeadsUpContentView(increasedHeight);
                if (styleView != null) {
                    return styleView;
                }
            } else if (this.mActions.size() == 0) {
                return null;
            }
            return applyStandardTemplateWithActions(getBigBaseLayoutResource());
        }

        public RemoteViews createHeadsUpContentView() {
            return createHeadsUpContentView(false);
        }

        public RemoteViews makePublicContentView() {
            return makePublicView(false);
        }

        public RemoteViews makePublicAmbientNotification() {
            return makePublicView(true);
        }

        private RemoteViews makePublicView(boolean ambient) {
            if (this.mN.publicVersion != null) {
                Builder builder = recoverBuilder(this.mContext, this.mN.publicVersion);
                return ambient ? builder.makeAmbientNotification() : builder.createContentView();
            }
            RemoteViews view;
            Bundle savedBundle = this.mN.extras;
            Style style = this.mStyle;
            this.mStyle = null;
            Icon largeIcon = this.mN.mLargeIcon;
            this.mN.mLargeIcon = null;
            Bitmap largeIconLegacy = this.mN.largeIcon;
            this.mN.largeIcon = null;
            ArrayList<Action> actions = this.mActions;
            this.mActions = new ArrayList();
            Bundle publicExtras = new Bundle();
            publicExtras.putBoolean(Notification.EXTRA_SHOW_WHEN, savedBundle.getBoolean(Notification.EXTRA_SHOW_WHEN));
            publicExtras.putBoolean(Notification.EXTRA_SHOW_CHRONOMETER, savedBundle.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER));
            publicExtras.putBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN, savedBundle.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN));
            publicExtras.putCharSequence(Notification.EXTRA_TITLE, this.mContext.getString(17040509));
            this.mN.extras = publicExtras;
            if (ambient) {
                view = makeAmbientNotification();
            } else {
                view = applyStandardTemplate(getBaseLayoutResource());
            }
            savedBundle.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, this.mN.extras.getInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI));
            this.mN.extras = savedBundle;
            this.mN.mLargeIcon = largeIcon;
            this.mN.largeIcon = largeIconLegacy;
            this.mActions = actions;
            this.mStyle = style;
            return view;
        }

        public RemoteViews makeLowPriorityContentView(boolean useRegularSubtext) {
            int color = this.mN.color;
            this.mN.color = 0;
            CharSequence summary = this.mN.extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
            if (!useRegularSubtext || TextUtils.isEmpty(summary)) {
                CharSequence newSummary = createSummaryText();
                if (!TextUtils.isEmpty(newSummary)) {
                    this.mN.extras.putCharSequence(Notification.EXTRA_SUB_TEXT, newSummary);
                }
            }
            RemoteViews header = makeNotificationHeader(false);
            header.setBoolean(16909105, "setAcceptAllTouches", true);
            if (summary != null) {
                this.mN.extras.putCharSequence(Notification.EXTRA_SUB_TEXT, summary);
            } else {
                this.mN.extras.remove(Notification.EXTRA_SUB_TEXT);
            }
            this.mN.color = color;
            return header;
        }

        private CharSequence createSummaryText() {
            CharSequence titleText = this.mN.extras.getCharSequence(Notification.EXTRA_TITLE);
            if (USE_ONLY_TITLE_IN_LOW_PRIORITY_SUMMARY) {
                return titleText;
            }
            SpannableStringBuilder summary = new SpannableStringBuilder();
            if (titleText == null) {
                titleText = this.mN.extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
            }
            BidiFormatter bidi = BidiFormatter.getInstance();
            if (titleText != null) {
                summary.append(bidi.unicodeWrap(titleText));
            }
            CharSequence contentText = this.mN.extras.getCharSequence(Notification.EXTRA_TEXT);
            if (!(titleText == null || contentText == null)) {
                summary.append(bidi.unicodeWrap(this.mContext.getText(17040507)));
            }
            if (contentText != null) {
                summary.append(bidi.unicodeWrap(contentText));
            }
            return summary;
        }

        private RemoteViews generateActionButton(Action action, boolean emphazisedMode, boolean oddAction, boolean ambient) {
            int emphasizedActionLayoutResource;
            boolean tombstone = action.actionIntent == null;
            ApplicationInfo applicationInfo = this.mContext.getApplicationInfo();
            if (emphazisedMode) {
                emphasizedActionLayoutResource = getEmphasizedActionLayoutResource();
            } else if (tombstone) {
                emphasizedActionLayoutResource = getActionTombstoneLayoutResource();
            } else {
                emphasizedActionLayoutResource = getActionLayoutResource();
            }
            RemoteViews button = new BuilderRemoteViews(applicationInfo, emphasizedActionLayoutResource);
            if (this.mN.extras.getBoolean(Notification.EXTRA_SHOW_ACTION_ICON)) {
                int actionIconLength = this.mContext.getResources().getDimensionPixelSize(34472164);
                button.setTextViewCompoundDrawablesWithBounds(16908669, action.getIcon(), null, null, null, actionIconLength, actionIconLength, this.mContext.getResources().getDimensionPixelSize(34472165));
            }
            if (IS_HONOR_PRODUCT) {
                button.setTextColor(16908669, this.mContext.getResources().getColorStateList(33882454, null));
            }
            if (!tombstone) {
                button.setOnClickPendingIntent(16908669, action.actionIntent);
            }
            button.setContentDescription(16908669, action.title);
            if (action.mRemoteInputs != null) {
                button.setRemoteInputs(16908669, action.mRemoteInputs);
            }
            if (emphazisedMode) {
                int bgColor;
                if (isColorized()) {
                    bgColor = oddAction ? getActionBarColor() : getActionBarColorDeEmphasized();
                } else {
                    Context context = this.mContext;
                    if (oddAction) {
                        emphasizedActionLayoutResource = 17170665;
                    } else {
                        emphasizedActionLayoutResource = 17170666;
                    }
                    bgColor = context.getColor(emphasizedActionLayoutResource);
                }
                button.setDrawableParameters(16908769, true, -1, bgColor, Mode.SRC_ATOP, -1);
                CharSequence title = action.title;
                ColorStateList[] outResultColor = null;
                if (isLegacy()) {
                    title = clearColorSpans(title);
                } else {
                    outResultColor = new ColorStateList[1];
                    title = ensureColorSpanContrast(title, bgColor, outResultColor);
                }
                button.setTextViewText(16908669, title);
                setTextViewColorPrimary(button, 16908669);
                if (outResultColor != null && outResultColor[0] != null) {
                    button.setTextColor(16908669, outResultColor[0]);
                } else if (!(this.mN.color == 0 || (isColorized() ^ 1) == 0)) {
                    button.setTextColor(16908669, resolveContrastColor());
                }
            } else {
                button.setTextViewText(16908669, processLegacyText(action.title));
                if (isColorized() && (ambient ^ 1) != 0) {
                    setTextViewColorPrimary(button, 16908669);
                } else if (this.mN.color != 0) {
                    button.setTextColor(16908669, ambient ? resolveAmbientColor() : resolveContrastColor());
                }
            }
            return button;
        }

        private CharSequence clearColorSpans(CharSequence charSequence) {
            if (!(charSequence instanceof Spanned)) {
                return charSequence;
            }
            Spanned ss = (Spanned) charSequence;
            Object[] spans = ss.getSpans(0, ss.length(), Object.class);
            SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
            for (Object span : spans) {
                Object resultSpan = span;
                if (span instanceof CharacterStyle) {
                    resultSpan = ((CharacterStyle) span).getUnderlying();
                }
                if (resultSpan instanceof TextAppearanceSpan) {
                    TextAppearanceSpan originalSpan = (TextAppearanceSpan) resultSpan;
                    if (originalSpan.getTextColor() != null) {
                        resultSpan = new TextAppearanceSpan(originalSpan.getFamily(), originalSpan.getTextStyle(), originalSpan.getTextSize(), null, originalSpan.getLinkTextColor());
                    }
                } else {
                    if (!((resultSpan instanceof ForegroundColorSpan) || (resultSpan instanceof BackgroundColorSpan))) {
                        resultSpan = span;
                    }
                }
                builder.setSpan(resultSpan, ss.getSpanStart(span), ss.getSpanEnd(span), ss.getSpanFlags(span));
            }
            return builder;
        }

        private CharSequence ensureColorSpanContrast(CharSequence charSequence, int background, ColorStateList[] outResultColor) {
            if (!(charSequence instanceof Spanned)) {
                return charSequence;
            }
            Spanned ss = (Spanned) charSequence;
            Object[] spans = ss.getSpans(0, ss.length(), Object.class);
            SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
            int i = 0;
            int length = spans.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    return builder;
                }
                Object resultSpan;
                ForegroundColorSpan span = spans[i2];
                ForegroundColorSpan resultSpan2 = span;
                int spanStart = ss.getSpanStart(span);
                int spanEnd = ss.getSpanEnd(span);
                boolean fullLength = spanEnd - spanStart == charSequence.length();
                if (span instanceof CharacterStyle) {
                    resultSpan2 = span.getUnderlying();
                }
                if (resultSpan2 instanceof TextAppearanceSpan) {
                    TextAppearanceSpan originalSpan = (TextAppearanceSpan) resultSpan2;
                    ColorStateList textColor = originalSpan.getTextColor();
                    if (textColor != null) {
                        int[] colors = textColor.getColors();
                        int[] newColors = new int[colors.length];
                        for (int i3 = 0; i3 < newColors.length; i3++) {
                            newColors[i3] = NotificationColorUtil.ensureLargeTextContrast(colors[i3], background);
                        }
                        ColorStateList textColor2 = new ColorStateList((int[][]) textColor.getStates().clone(), newColors);
                        resultSpan = new TextAppearanceSpan(originalSpan.getFamily(), originalSpan.getTextStyle(), originalSpan.getTextSize(), textColor2, originalSpan.getLinkTextColor());
                        if (fullLength) {
                            outResultColor[0] = new ColorStateList((int[][]) textColor2.getStates().clone(), newColors);
                        }
                    }
                } else if (resultSpan2 instanceof ForegroundColorSpan) {
                    int foregroundColor = NotificationColorUtil.ensureLargeTextContrast(resultSpan2.getForegroundColor(), background);
                    resultSpan = new ForegroundColorSpan(foregroundColor);
                    if (fullLength) {
                        outResultColor[0] = ColorStateList.valueOf(foregroundColor);
                    }
                } else {
                    resultSpan2 = span;
                }
                builder.setSpan(resultSpan, spanStart, spanEnd, ss.getSpanFlags(span));
                i = i2 + 1;
            }
        }

        private boolean isLegacy() {
            if (!this.mIsLegacyInitialized) {
                this.mIsLegacy = this.mContext.getApplicationInfo().targetSdkVersion < 21;
                this.mIsLegacyInitialized = true;
            }
            return this.mIsLegacy;
        }

        private CharSequence processLegacyText(CharSequence charSequence) {
            return processLegacyText(charSequence, false);
        }

        private CharSequence processLegacyText(CharSequence charSequence, boolean ambient) {
            boolean wantLightText = ambient;
            if ((!isLegacy() ? textColorsNeedInversion() : true) != ambient) {
                return getColorUtil().invertCharSequenceColors(charSequence);
            }
            return charSequence;
        }

        private void processSmallIconColor(Icon smallIcon, RemoteViews contentView, boolean ambient) {
            boolean colorable = isPureColorIcon(smallIcon);
            int color = ambient ? resolveAmbientColor() : getPrimaryHighlightColor();
            if (colorable) {
                if (this.mN.color == 0) {
                    contentView.setDrawableParameters(R.id.icon, false, -1, -9079435, Mode.SRC_ATOP, -1);
                    contentView.setInt(16909105, "setOriginalIconColor", 1);
                    return;
                }
                contentView.setDrawableParameters(R.id.icon, false, -1, color, Mode.SRC_ATOP, -1);
            }
            String str = "setOriginalIconColor";
            if (!colorable) {
                color = 1;
            }
            contentView.setInt(16909105, str, color);
        }

        private boolean isPureColorIcon(Icon icon) {
            switch (getHWColorUtil().getSmallIconColorType(this.mContext, icon)) {
                case 0:
                case 1:
                    this.mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 0);
                    return false;
                case 2:
                    this.mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 1);
                    return true;
                case 4:
                    this.mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 1);
                    return false;
                default:
                    this.mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 0);
                    return false;
            }
        }

        private void processLargeLegacyIcon(Icon largeIcon, RemoteViews contentView) {
            if (largeIcon != null && isLegacy() && isPureColorIcon(largeIcon)) {
                contentView.setDrawableParameters(16909218, false, -1, resolveContrastColor(), Mode.SRC_ATOP, -1);
            }
        }

        private void sanitizeColor() {
            if (this.mN.color != 0) {
                Notification notification = this.mN;
                notification.color |= -16777216;
            }
        }

        int resolveContrastColor() {
            if (this.mCachedContrastColorIsFor == this.mN.color && this.mCachedContrastColor != 1) {
                return this.mCachedContrastColor;
            }
            int color;
            int background = this.mBackgroundColorHint;
            if (this.mBackgroundColorHint == 1) {
                background = this.mContext.getColor(17170670);
            }
            if (this.mN.color == 0) {
                ensureColors();
                color = this.mSecondaryTextColor;
            } else {
                color = NotificationColorUtil.resolveContrastColor(this.mContext, this.mN.color, background);
            }
            if (Color.alpha(color) < 255) {
                color = NotificationColorUtil.compositeColors(color, background);
            }
            this.mCachedContrastColorIsFor = this.mN.color;
            this.mCachedContrastColor = color;
            return color;
        }

        int resolveAmbientColor() {
            if (this.mCachedAmbientColorIsFor == this.mN.color && this.mCachedAmbientColorIsFor != 1) {
                return this.mCachedAmbientColor;
            }
            int contrasted = NotificationColorUtil.resolveAmbientColor(this.mContext, this.mN.color);
            this.mCachedAmbientColorIsFor = this.mN.color;
            this.mCachedAmbientColor = contrasted;
            return contrasted;
        }

        public Notification buildUnstyled() {
            if (this.mActions.size() > 0) {
                this.mN.actions = new Action[this.mActions.size()];
                this.mActions.toArray(this.mN.actions);
            }
            if (!this.mPersonList.isEmpty()) {
                this.mN.extras.putStringArray(Notification.EXTRA_PEOPLE, (String[]) this.mPersonList.toArray(new String[this.mPersonList.size()]));
            }
            if (!(this.mN.bigContentView == null && this.mN.contentView == null && this.mN.headsUpContentView == null)) {
                this.mN.extras.putBoolean(Notification.EXTRA_CONTAINS_CUSTOM_VIEW, true);
            }
            return this.mN;
        }

        public static Builder recoverBuilder(Context context, Notification n) {
            Context builderContext;
            ApplicationInfo applicationInfo = (ApplicationInfo) n.extras.getParcelable(Notification.EXTRA_BUILDER_APPLICATION_INFO);
            if (applicationInfo != null) {
                try {
                    builderContext = context.createApplicationContext(applicationInfo, 4);
                } catch (NameNotFoundException e) {
                    Log.e(Notification.TAG, "ApplicationInfo " + applicationInfo + " not found");
                    builderContext = context;
                }
            } else {
                builderContext = context;
            }
            return new Builder(builderContext, n);
        }

        @Deprecated
        public Notification getNotification() {
            return build();
        }

        public Notification build() {
            if (this.mUserExtras != null) {
                this.mN.extras = getAllExtras();
            }
            this.mN.creationTime = System.currentTimeMillis();
            Notification.addFieldsFromContext(this.mContext, this.mN);
            buildUnstyled();
            if (this.mStyle != null) {
                this.mStyle.reduceImageSizes(this.mContext);
                this.mStyle.purgeResources();
                this.mStyle.buildStyled(this.mN);
            }
            this.mN.reduceImageSizes(this.mContext);
            if (this.mContext.getApplicationInfo().targetSdkVersion < 24 && useExistingRemoteView()) {
                if (this.mN.contentView == null) {
                    this.mN.contentView = createContentView();
                    this.mN.extras.putInt(EXTRA_REBUILD_CONTENT_VIEW_ACTION_COUNT, this.mN.contentView.getSequenceNumber());
                }
                if (this.mN.bigContentView == null) {
                    this.mN.bigContentView = createBigContentView();
                    if (this.mN.bigContentView != null) {
                        this.mN.extras.putInt(EXTRA_REBUILD_BIG_CONTENT_VIEW_ACTION_COUNT, this.mN.bigContentView.getSequenceNumber());
                    }
                }
                if (this.mN.headsUpContentView == null) {
                    this.mN.headsUpContentView = createHeadsUpContentView();
                    if (this.mN.headsUpContentView != null) {
                        this.mN.extras.putInt(EXTRA_REBUILD_HEADS_UP_CONTENT_VIEW_ACTION_COUNT, this.mN.headsUpContentView.getSequenceNumber());
                    }
                }
            }
            if ((this.mN.defaults & 4) != 0) {
                Notification notification = this.mN;
                notification.flags |= 1;
            }
            return this.mN;
        }

        public Notification buildInto(Notification n) {
            build().cloneInto(n, true);
            return n;
        }

        public static Notification maybeCloneStrippedForDelivery(Notification n) {
            String templateClass = n.extras.getString(Notification.EXTRA_TEMPLATE);
            if (!TextUtils.isEmpty(templateClass) && Notification.getNotificationStyleClass(templateClass) == null) {
                return n;
            }
            boolean stripContentView = n.contentView instanceof BuilderRemoteViews ? n.extras.getInt(EXTRA_REBUILD_CONTENT_VIEW_ACTION_COUNT, -1) == n.contentView.getSequenceNumber() : false;
            boolean stripBigContentView = n.bigContentView instanceof BuilderRemoteViews ? n.extras.getInt(EXTRA_REBUILD_BIG_CONTENT_VIEW_ACTION_COUNT, -1) == n.bigContentView.getSequenceNumber() : false;
            boolean stripHeadsUpContentView = n.headsUpContentView instanceof BuilderRemoteViews ? n.extras.getInt(EXTRA_REBUILD_HEADS_UP_CONTENT_VIEW_ACTION_COUNT, -1) == n.headsUpContentView.getSequenceNumber() : false;
            if (!stripContentView && (stripBigContentView ^ 1) != 0 && (stripHeadsUpContentView ^ 1) != 0) {
                return n;
            }
            Notification clone = n.clone();
            if (stripContentView) {
                clone.contentView = null;
                clone.extras.remove(EXTRA_REBUILD_CONTENT_VIEW_ACTION_COUNT);
            }
            if (stripBigContentView) {
                clone.bigContentView = null;
                clone.extras.remove(EXTRA_REBUILD_BIG_CONTENT_VIEW_ACTION_COUNT);
            }
            if (stripHeadsUpContentView) {
                clone.headsUpContentView = null;
                clone.extras.remove(EXTRA_REBUILD_HEADS_UP_CONTENT_VIEW_ACTION_COUNT);
            }
            return clone;
        }

        private int getBaseLayoutResource() {
            return 17367184;
        }

        private int getBigBaseLayoutResource() {
            return 17367185;
        }

        private int getBigPictureLayoutResource() {
            return 17367187;
        }

        private int getBigTextLayoutResource() {
            return 17367188;
        }

        private int getInboxLayoutResource() {
            return 17367189;
        }

        private int getMessagingLayoutResource() {
            return 17367191;
        }

        private int getActionLayoutResource() {
            return 17367175;
        }

        private int getEmphasizedActionLayoutResource() {
            return 17367176;
        }

        private int getActionTombstoneLayoutResource() {
            return 17367178;
        }

        private int getBackgroundColor() {
            if (isColorized()) {
                return this.mBackgroundColor != 1 ? this.mBackgroundColor : this.mN.color;
            }
            int i;
            if (this.mBackgroundColorHint != 1) {
                i = this.mBackgroundColorHint;
            } else {
                i = 0;
            }
            return i;
        }

        private boolean isColorized() {
            return this.mN.isColorized();
        }

        private boolean textColorsNeedInversion() {
            boolean z = false;
            if (this.mStyle == null || (MediaStyle.class.equals(this.mStyle.getClass()) ^ 1) != 0) {
                return false;
            }
            int targetSdkVersion = this.mContext.getApplicationInfo().targetSdkVersion;
            if (targetSdkVersion > 23 && targetSdkVersion < 26) {
                z = true;
            }
            return z;
        }

        public void setColorPalette(int backgroundColor, int foregroundColor) {
            this.mBackgroundColor = backgroundColor;
            this.mForegroundColor = foregroundColor;
            this.mTextColorsAreForBackground = 1;
            ensureColors();
        }

        public void setBackgroundColorHint(int backgroundColor) {
            this.mBackgroundColorHint = backgroundColor;
        }

        public void setRebuildStyledRemoteViews(boolean rebuild) {
            this.mRebuildStyledRemoteViews = rebuild;
        }
    }

    private static class BuilderRemoteViews extends RemoteViews {
        public BuilderRemoteViews(Parcel parcel) {
            super(parcel);
        }

        public BuilderRemoteViews(ApplicationInfo appInfo, int layoutId) {
            super(appInfo, layoutId);
        }

        public BuilderRemoteViews clone() {
            Parcel p = Parcel.obtain();
            writeToParcel(p, 0);
            p.setDataPosition(0);
            BuilderRemoteViews brv = new BuilderRemoteViews(p);
            p.recycle();
            return brv;
        }
    }

    public interface Extender {
        Builder extend(Builder builder);
    }

    public static final class CarExtender implements Extender {
        private static final String EXTRA_CAR_EXTENDER = "android.car.EXTENSIONS";
        private static final String EXTRA_COLOR = "app_color";
        private static final String EXTRA_CONVERSATION = "car_conversation";
        private static final String EXTRA_LARGE_ICON = "large_icon";
        private static final String TAG = "CarExtender";
        private int mColor = 0;
        private Bitmap mLargeIcon;
        private UnreadConversation mUnreadConversation;

        public static class Builder {
            private long mLatestTimestamp;
            private final List<String> mMessages = new ArrayList();
            private final String mParticipant;
            private PendingIntent mReadPendingIntent;
            private RemoteInput mRemoteInput;
            private PendingIntent mReplyPendingIntent;

            public Builder(String name) {
                this.mParticipant = name;
            }

            public Builder addMessage(String message) {
                this.mMessages.add(message);
                return this;
            }

            public Builder setReplyAction(PendingIntent pendingIntent, RemoteInput remoteInput) {
                this.mRemoteInput = remoteInput;
                this.mReplyPendingIntent = pendingIntent;
                return this;
            }

            public Builder setReadPendingIntent(PendingIntent pendingIntent) {
                this.mReadPendingIntent = pendingIntent;
                return this;
            }

            public Builder setLatestTimestamp(long timestamp) {
                this.mLatestTimestamp = timestamp;
                return this;
            }

            public UnreadConversation build() {
                return new UnreadConversation((String[]) this.mMessages.toArray(new String[this.mMessages.size()]), this.mRemoteInput, this.mReplyPendingIntent, this.mReadPendingIntent, new String[]{this.mParticipant}, this.mLatestTimestamp);
            }
        }

        public static class UnreadConversation {
            private static final String KEY_AUTHOR = "author";
            private static final String KEY_MESSAGES = "messages";
            private static final String KEY_ON_READ = "on_read";
            private static final String KEY_ON_REPLY = "on_reply";
            private static final String KEY_PARTICIPANTS = "participants";
            private static final String KEY_REMOTE_INPUT = "remote_input";
            private static final String KEY_TEXT = "text";
            private static final String KEY_TIMESTAMP = "timestamp";
            private final long mLatestTimestamp;
            private final String[] mMessages;
            private final String[] mParticipants;
            private final PendingIntent mReadPendingIntent;
            private final RemoteInput mRemoteInput;
            private final PendingIntent mReplyPendingIntent;

            UnreadConversation(String[] messages, RemoteInput remoteInput, PendingIntent replyPendingIntent, PendingIntent readPendingIntent, String[] participants, long latestTimestamp) {
                this.mMessages = messages;
                this.mRemoteInput = remoteInput;
                this.mReadPendingIntent = readPendingIntent;
                this.mReplyPendingIntent = replyPendingIntent;
                this.mParticipants = participants;
                this.mLatestTimestamp = latestTimestamp;
            }

            public String[] getMessages() {
                return this.mMessages;
            }

            public RemoteInput getRemoteInput() {
                return this.mRemoteInput;
            }

            public PendingIntent getReplyPendingIntent() {
                return this.mReplyPendingIntent;
            }

            public PendingIntent getReadPendingIntent() {
                return this.mReadPendingIntent;
            }

            public String[] getParticipants() {
                return this.mParticipants;
            }

            public String getParticipant() {
                return this.mParticipants.length > 0 ? this.mParticipants[0] : null;
            }

            public long getLatestTimestamp() {
                return this.mLatestTimestamp;
            }

            Bundle getBundleForUnreadConversation() {
                Bundle b = new Bundle();
                String author = null;
                if (this.mParticipants != null && this.mParticipants.length > 1) {
                    author = this.mParticipants[0];
                }
                Parcelable[] messages = new Parcelable[this.mMessages.length];
                for (int i = 0; i < messages.length; i++) {
                    Bundle m = new Bundle();
                    m.putString(KEY_TEXT, this.mMessages[i]);
                    m.putString("author", author);
                    messages[i] = m;
                }
                b.putParcelableArray(KEY_MESSAGES, messages);
                if (this.mRemoteInput != null) {
                    b.putParcelable(KEY_REMOTE_INPUT, this.mRemoteInput);
                }
                b.putParcelable(KEY_ON_REPLY, this.mReplyPendingIntent);
                b.putParcelable(KEY_ON_READ, this.mReadPendingIntent);
                b.putStringArray(KEY_PARTICIPANTS, this.mParticipants);
                b.putLong("timestamp", this.mLatestTimestamp);
                return b;
            }

            static UnreadConversation getUnreadConversationFromBundle(Bundle b) {
                if (b == null) {
                    return null;
                }
                Parcelable[] parcelableMessages = b.getParcelableArray(KEY_MESSAGES);
                String[] messages = null;
                if (parcelableMessages != null) {
                    String[] tmp = new String[parcelableMessages.length];
                    boolean success = true;
                    for (int i = 0; i < tmp.length; i++) {
                        if (!(parcelableMessages[i] instanceof Bundle)) {
                            success = false;
                            break;
                        }
                        tmp[i] = ((Bundle) parcelableMessages[i]).getString(KEY_TEXT);
                        if (tmp[i] == null) {
                            success = false;
                            break;
                        }
                    }
                    if (!success) {
                        return null;
                    }
                    messages = tmp;
                }
                PendingIntent onRead = (PendingIntent) b.getParcelable(KEY_ON_READ);
                PendingIntent onReply = (PendingIntent) b.getParcelable(KEY_ON_REPLY);
                RemoteInput remoteInput = (RemoteInput) b.getParcelable(KEY_REMOTE_INPUT);
                String[] participants = b.getStringArray(KEY_PARTICIPANTS);
                if (participants == null || participants.length != 1) {
                    return null;
                }
                return new UnreadConversation(messages, remoteInput, onReply, onRead, participants, b.getLong("timestamp"));
            }
        }

        public CarExtender(Notification notif) {
            Bundle carBundle = notif.extras == null ? null : notif.extras.getBundle(EXTRA_CAR_EXTENDER);
            if (carBundle != null) {
                this.mLargeIcon = (Bitmap) carBundle.getParcelable(EXTRA_LARGE_ICON);
                this.mColor = carBundle.getInt(EXTRA_COLOR, 0);
                this.mUnreadConversation = UnreadConversation.getUnreadConversationFromBundle(carBundle.getBundle(EXTRA_CONVERSATION));
            }
        }

        public Builder extend(Builder builder) {
            Bundle carExtensions = new Bundle();
            if (this.mLargeIcon != null) {
                carExtensions.putParcelable(EXTRA_LARGE_ICON, this.mLargeIcon);
            }
            if (this.mColor != 0) {
                carExtensions.putInt(EXTRA_COLOR, this.mColor);
            }
            if (this.mUnreadConversation != null) {
                carExtensions.putBundle(EXTRA_CONVERSATION, this.mUnreadConversation.getBundleForUnreadConversation());
            }
            builder.getExtras().putBundle(EXTRA_CAR_EXTENDER, carExtensions);
            return builder;
        }

        public CarExtender setColor(int color) {
            this.mColor = color;
            return this;
        }

        public int getColor() {
            return this.mColor;
        }

        public CarExtender setLargeIcon(Bitmap largeIcon) {
            this.mLargeIcon = largeIcon;
            return this;
        }

        public Bitmap getLargeIcon() {
            return this.mLargeIcon;
        }

        public CarExtender setUnreadConversation(UnreadConversation unreadConversation) {
            this.mUnreadConversation = unreadConversation;
            return this;
        }

        public UnreadConversation getUnreadConversation() {
            return this.mUnreadConversation;
        }
    }

    public static class DecoratedCustomViewStyle extends Style {
        public boolean displayCustomViewInline() {
            return true;
        }

        public RemoteViews makeContentView(boolean increasedHeight) {
            return makeStandardTemplateWithCustomContent(this.mBuilder.mN.contentView);
        }

        public RemoteViews makeBigContentView() {
            return makeDecoratedBigContentView();
        }

        public RemoteViews makeHeadsUpContentView(boolean increasedHeight) {
            return makeDecoratedHeadsUpContentView();
        }

        private RemoteViews makeDecoratedHeadsUpContentView() {
            RemoteViews headsUpContentView;
            if (this.mBuilder.mN.headsUpContentView == null) {
                headsUpContentView = this.mBuilder.mN.contentView;
            } else {
                headsUpContentView = this.mBuilder.mN.headsUpContentView;
            }
            if (this.mBuilder.mActions.size() == 0) {
                return makeStandardTemplateWithCustomContent(headsUpContentView);
            }
            RemoteViews remoteViews = this.mBuilder.applyStandardTemplateWithActions(this.mBuilder.getBigBaseLayoutResource());
            buildIntoRemoteViewContent(remoteViews, headsUpContentView);
            remoteViews.setUseAppContext(true);
            return remoteViews;
        }

        private RemoteViews makeStandardTemplateWithCustomContent(RemoteViews customContent) {
            RemoteViews remoteViews = this.mBuilder.applyStandardTemplate(this.mBuilder.getBaseLayoutResource());
            buildIntoRemoteViewContent(remoteViews, customContent);
            remoteViews.setUseAppContext(true);
            return remoteViews;
        }

        private RemoteViews makeDecoratedBigContentView() {
            RemoteViews bigContentView;
            if (this.mBuilder.mN.bigContentView == null) {
                bigContentView = this.mBuilder.mN.contentView;
            } else {
                bigContentView = this.mBuilder.mN.bigContentView;
            }
            if (this.mBuilder.mActions.size() == 0) {
                return makeStandardTemplateWithCustomContent(bigContentView);
            }
            RemoteViews remoteViews = this.mBuilder.applyStandardTemplateWithActions(this.mBuilder.getBigBaseLayoutResource());
            buildIntoRemoteViewContent(remoteViews, bigContentView);
            return remoteViews;
        }

        private void buildIntoRemoteViewContent(RemoteViews remoteViews, RemoteViews customContent) {
            if (customContent != null) {
                customContent = customContent.clone();
                remoteViews.removeAllViews(16909106);
                remoteViews.addView(16909106, customContent);
            }
            int endMargin = 17105153;
            if (this.mBuilder.mN.hasLargeIcon()) {
                endMargin = 17105157;
            }
            remoteViews.setViewLayoutMarginEndDimen(16909106, endMargin);
            remoteViews.setViewLayoutMarginBottomDimen(16909106, 34472112);
            remoteViews.setViewLayoutMarginTopDimen(16909106, 34472117);
            remoteViews.setUseAppContext(true);
        }
    }

    public static class MediaStyle extends Style {
        static final int MAX_MEDIA_BUTTONS = 5;
        static final int MAX_MEDIA_BUTTONS_IN_COMPACT = 3;
        private int[] mActionsToShowInCompact = null;
        private Token mToken;

        @Deprecated
        public MediaStyle(Builder builder) {
            setBuilder(builder);
        }

        public MediaStyle setShowActionsInCompactView(int... actions) {
            this.mActionsToShowInCompact = actions;
            return this;
        }

        public MediaStyle setMediaSession(Token token) {
            this.mToken = token;
            return this;
        }

        public Notification buildStyled(Notification wip) {
            super.buildStyled(wip);
            if (wip.category == null) {
                wip.category = Notification.CATEGORY_TRANSPORT;
            }
            return wip;
        }

        public RemoteViews makeContentView(boolean increasedHeight) {
            return makeMediaContentView();
        }

        public RemoteViews makeBigContentView() {
            return makeMediaBigContentView();
        }

        public RemoteViews makeHeadsUpContentView(boolean increasedHeight) {
            RemoteViews expanded = makeMediaBigContentView();
            return expanded != null ? expanded : makeMediaContentView();
        }

        public void addExtras(Bundle extras) {
            super.addExtras(extras);
            if (this.mToken != null) {
                extras.putParcelable(Notification.EXTRA_MEDIA_SESSION, this.mToken);
            }
            if (this.mActionsToShowInCompact != null) {
                extras.putIntArray(Notification.EXTRA_COMPACT_ACTIONS, this.mActionsToShowInCompact);
            }
        }

        protected void restoreFromExtras(Bundle extras) {
            super.restoreFromExtras(extras);
            if (extras.containsKey(Notification.EXTRA_MEDIA_SESSION)) {
                this.mToken = (Token) extras.getParcelable(Notification.EXTRA_MEDIA_SESSION);
            }
            if (extras.containsKey(Notification.EXTRA_COMPACT_ACTIONS)) {
                this.mActionsToShowInCompact = extras.getIntArray(Notification.EXTRA_COMPACT_ACTIONS);
            }
        }

        private RemoteViews generateMediaActionButton(Action action, int color) {
            boolean tombstone = action.actionIntent == null;
            RemoteViews button = new BuilderRemoteViews(this.mBuilder.mContext.getApplicationInfo(), 17367179);
            button.setImageViewIcon(16908669, action.getIcon());
            button.setDrawableParameters(16908669, false, -1, color, Mode.SRC_ATOP, -1);
            if (!tombstone) {
                button.setOnClickPendingIntent(16908669, action.actionIntent);
            }
            button.setContentDescription(16908669, action.title);
            return button;
        }

        private RemoteViews makeMediaContentView() {
            int N;
            RemoteViews view = this.mBuilder.applyStandardTemplate(17367190, false);
            int numActions = this.mBuilder.mActions.size();
            if (this.mActionsToShowInCompact == null) {
                N = 0;
            } else {
                N = Math.min(this.mActionsToShowInCompact.length, 3);
            }
            if (N > 0) {
                view.removeAllViews(16909046);
                for (int i = 0; i < N; i++) {
                    if (i >= numActions) {
                        throw new IllegalArgumentException(String.format("setShowActionsInCompactView: action %d out of bounds (max %d)", new Object[]{Integer.valueOf(i), Integer.valueOf(numActions - 1)}));
                    }
                    view.addView(16909046, generateMediaActionButton((Action) this.mBuilder.mActions.get(this.mActionsToShowInCompact[i]), getPrimaryHighlightColor()));
                }
            }
            handleImage(view);
            int endMargin = 17105153;
            if (this.mBuilder.mN.hasLargeIcon()) {
                endMargin = 17105157;
            }
            view.setViewLayoutMarginEndDimen(16909106, endMargin);
            return view;
        }

        private int getPrimaryHighlightColor() {
            return this.mBuilder.getPrimaryHighlightColor();
        }

        private RemoteViews makeMediaBigContentView() {
            int actionCount = Math.min(this.mBuilder.mActions.size(), 5);
            int actionsInCompact;
            if (this.mActionsToShowInCompact == null) {
                actionsInCompact = 0;
            } else {
                actionsInCompact = Math.min(this.mActionsToShowInCompact.length, 3);
            }
            if (!this.mBuilder.mN.hasLargeIcon() && actionCount <= actionsInCompact) {
                return null;
            }
            RemoteViews big = this.mBuilder.applyStandardTemplate(17367186, false);
            if (actionCount > 0) {
                big.removeAllViews(16909046);
                for (int i = 0; i < actionCount; i++) {
                    big.addView(16909046, generateMediaActionButton((Action) this.mBuilder.mActions.get(i), getPrimaryHighlightColor()));
                }
            }
            handleImage(big);
            return big;
        }

        private void handleImage(RemoteViews contentView) {
            if (this.mBuilder.mN.hasLargeIcon()) {
                contentView.setViewLayoutMarginEndDimen(16909021, 0);
                contentView.setViewLayoutMarginEndDimen(16909348, 0);
            }
        }

        protected boolean hasProgress() {
            return false;
        }
    }

    public static class DecoratedMediaCustomViewStyle extends MediaStyle {
        public boolean displayCustomViewInline() {
            return true;
        }

        public RemoteViews makeContentView(boolean increasedHeight) {
            return buildIntoRemoteView(super.makeContentView(false), 16909104, this.mBuilder.mN.contentView);
        }

        public RemoteViews makeBigContentView() {
            RemoteViews customRemoteView;
            if (this.mBuilder.mN.bigContentView != null) {
                customRemoteView = this.mBuilder.mN.bigContentView;
            } else {
                customRemoteView = this.mBuilder.mN.contentView;
            }
            return makeBigContentViewWithCustomContent(customRemoteView);
        }

        private RemoteViews makeBigContentViewWithCustomContent(RemoteViews customRemoteView) {
            RemoteViews remoteViews = super.makeBigContentView();
            if (remoteViews != null) {
                return buildIntoRemoteView(remoteViews, 16909106, customRemoteView);
            }
            if (customRemoteView != this.mBuilder.mN.contentView) {
                return buildIntoRemoteView(super.makeContentView(false), 16909104, customRemoteView);
            }
            return null;
        }

        public RemoteViews makeHeadsUpContentView(boolean increasedHeight) {
            RemoteViews customRemoteView;
            if (this.mBuilder.mN.headsUpContentView != null) {
                customRemoteView = this.mBuilder.mN.headsUpContentView;
            } else {
                customRemoteView = this.mBuilder.mN.contentView;
            }
            return makeBigContentViewWithCustomContent(customRemoteView);
        }

        private RemoteViews buildIntoRemoteView(RemoteViews remoteViews, int id, RemoteViews customContent) {
            if (customContent != null) {
                customContent = customContent.clone();
                remoteViews.removeAllViews(id);
                remoteViews.addView(id, customContent);
            }
            return remoteViews;
        }
    }

    public static class InboxStyle extends Style {
        private ArrayList<CharSequence> mTexts = new ArrayList(5);

        @Deprecated
        public InboxStyle(Builder builder) {
            setBuilder(builder);
        }

        public InboxStyle setBigContentTitle(CharSequence title) {
            internalSetBigContentTitle(Notification.safeCharSequence(title));
            return this;
        }

        public InboxStyle setSummaryText(CharSequence cs) {
            internalSetSummaryText(Notification.safeCharSequence(cs));
            return this;
        }

        public InboxStyle addLine(CharSequence cs) {
            this.mTexts.add(Notification.safeCharSequence(cs));
            return this;
        }

        public void addExtras(Bundle extras) {
            super.addExtras(extras);
            extras.putCharSequenceArray(Notification.EXTRA_TEXT_LINES, (CharSequence[]) this.mTexts.toArray(new CharSequence[this.mTexts.size()]));
        }

        protected void restoreFromExtras(Bundle extras) {
            super.restoreFromExtras(extras);
            this.mTexts.clear();
            if (extras.containsKey(Notification.EXTRA_TEXT_LINES)) {
                Collections.addAll(this.mTexts, extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES));
            }
        }

        public RemoteViews makeBigContentView() {
            CharSequence oldBuilderContentText = this.mBuilder.mN.extras.getCharSequence(Notification.EXTRA_TEXT);
            this.mBuilder.getAllExtras().putCharSequence(Notification.EXTRA_TEXT, null);
            RemoteViews contentView = getStandardView(this.mBuilder.getInboxLayoutResource());
            this.mBuilder.getAllExtras().putCharSequence(Notification.EXTRA_TEXT, oldBuilderContentText);
            int[] rowIds = new int[]{16908962, 16908963, 16908964, 16908965, 16908966, 16908967, 16908968};
            for (int rowId : rowIds) {
                contentView.setViewVisibility(rowId, 8);
            }
            int i = 0;
            int topPadding = this.mBuilder.mContext.getResources().getDimensionPixelSize(17105166);
            boolean first = true;
            int onlyViewId = 0;
            int maxRows = rowIds.length;
            if (this.mBuilder.mActions.size() > 0) {
                maxRows--;
            }
            while (i < this.mTexts.size() && i < maxRows) {
                CharSequence str = (CharSequence) this.mTexts.get(i);
                if (!TextUtils.isEmpty(str)) {
                    contentView.setViewVisibility(rowIds[i], 0);
                    contentView.setTextViewText(rowIds[i], this.mBuilder.processLegacyText(str));
                    this.mBuilder.setTextViewColorSecondary(contentView, rowIds[i]);
                    contentView.setViewPadding(rowIds[i], 0, topPadding, 0, 0);
                    handleInboxImageMargin(contentView, rowIds[i], first);
                    if (first) {
                        onlyViewId = rowIds[i];
                    } else {
                        onlyViewId = 0;
                    }
                    first = false;
                }
                i++;
            }
            if (onlyViewId != 0) {
                contentView.setViewPadding(onlyViewId, 0, this.mBuilder.mContext.getResources().getDimensionPixelSize(17105172), 0, 0);
            }
            return contentView;
        }

        private void handleInboxImageMargin(RemoteViews contentView, int id, boolean first) {
            int endMargin = 0;
            if (first) {
                int hasProgress = this.mBuilder.mN.extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0) == 0 ? this.mBuilder.mN.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE) : 1;
                if (this.mBuilder.mN.hasLargeIcon() && (hasProgress ^ 1) != 0) {
                    endMargin = 17105156;
                }
            }
            contentView.setViewLayoutMarginEndDimen(id, endMargin);
        }
    }

    public static class MessagingStyle extends Style {
        public static final int MAXIMUM_RETAINED_MESSAGES = 25;
        CharSequence mConversationTitle;
        List<Message> mHistoricMessages = new ArrayList();
        List<Message> mMessages = new ArrayList();
        CharSequence mUserDisplayName;

        public static final class Message {
            static final String KEY_DATA_MIME_TYPE = "type";
            static final String KEY_DATA_URI = "uri";
            static final String KEY_EXTRAS_BUNDLE = "extras";
            static final String KEY_SENDER = "sender";
            static final String KEY_TEXT = "text";
            static final String KEY_TIMESTAMP = "time";
            private String mDataMimeType;
            private Uri mDataUri;
            private Bundle mExtras = new Bundle();
            private final CharSequence mSender;
            private final CharSequence mText;
            private final long mTimestamp;

            public Message(CharSequence text, long timestamp, CharSequence sender) {
                this.mText = text;
                this.mTimestamp = timestamp;
                this.mSender = sender;
            }

            public Message setData(String dataMimeType, Uri dataUri) {
                this.mDataMimeType = dataMimeType;
                this.mDataUri = dataUri;
                return this;
            }

            public CharSequence getText() {
                return this.mText;
            }

            public long getTimestamp() {
                return this.mTimestamp;
            }

            public Bundle getExtras() {
                return this.mExtras;
            }

            public CharSequence getSender() {
                return this.mSender;
            }

            public String getDataMimeType() {
                return this.mDataMimeType;
            }

            public Uri getDataUri() {
                return this.mDataUri;
            }

            private Bundle toBundle() {
                Bundle bundle = new Bundle();
                if (this.mText != null) {
                    bundle.putCharSequence(KEY_TEXT, this.mText);
                }
                bundle.putLong("time", this.mTimestamp);
                if (this.mSender != null) {
                    bundle.putCharSequence(KEY_SENDER, this.mSender);
                }
                if (this.mDataMimeType != null) {
                    bundle.putString("type", this.mDataMimeType);
                }
                if (this.mDataUri != null) {
                    bundle.putParcelable("uri", this.mDataUri);
                }
                if (this.mExtras != null) {
                    bundle.putBundle(KEY_EXTRAS_BUNDLE, this.mExtras);
                }
                return bundle;
            }

            static Bundle[] getBundleArrayForMessages(List<Message> messages) {
                Bundle[] bundles = new Bundle[messages.size()];
                int N = messages.size();
                for (int i = 0; i < N; i++) {
                    bundles[i] = ((Message) messages.get(i)).toBundle();
                }
                return bundles;
            }

            static List<Message> getMessagesFromBundleArray(Parcelable[] bundles) {
                List<Message> messages = new ArrayList(bundles.length);
                for (int i = 0; i < bundles.length; i++) {
                    if (bundles[i] instanceof Bundle) {
                        Message message = getMessageFromBundle((Bundle) bundles[i]);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                }
                return messages;
            }

            static Message getMessageFromBundle(Bundle bundle) {
                try {
                    if (!bundle.containsKey(KEY_TEXT) || (bundle.containsKey("time") ^ 1) != 0) {
                        return null;
                    }
                    Message message = new Message(bundle.getCharSequence(KEY_TEXT), bundle.getLong("time"), bundle.getCharSequence(KEY_SENDER));
                    if (bundle.containsKey("type") && bundle.containsKey("uri")) {
                        message.setData(bundle.getString("type"), (Uri) bundle.getParcelable("uri"));
                    }
                    if (bundle.containsKey(KEY_EXTRAS_BUNDLE)) {
                        message.getExtras().putAll(bundle.getBundle(KEY_EXTRAS_BUNDLE));
                    }
                    return message;
                } catch (ClassCastException e) {
                    return null;
                }
            }
        }

        MessagingStyle() {
        }

        public MessagingStyle(CharSequence userDisplayName) {
            this.mUserDisplayName = userDisplayName;
        }

        public CharSequence getUserDisplayName() {
            return this.mUserDisplayName;
        }

        public MessagingStyle setConversationTitle(CharSequence conversationTitle) {
            this.mConversationTitle = conversationTitle;
            return this;
        }

        public CharSequence getConversationTitle() {
            return this.mConversationTitle;
        }

        public MessagingStyle addMessage(CharSequence text, long timestamp, CharSequence sender) {
            return addMessage(new Message(text, timestamp, sender));
        }

        public MessagingStyle addMessage(Message message) {
            this.mMessages.add(message);
            if (this.mMessages.size() > 25) {
                this.mMessages.remove(0);
            }
            return this;
        }

        public MessagingStyle addHistoricMessage(Message message) {
            this.mHistoricMessages.add(message);
            if (this.mHistoricMessages.size() > 25) {
                this.mHistoricMessages.remove(0);
            }
            return this;
        }

        public List<Message> getMessages() {
            return this.mMessages;
        }

        public List<Message> getHistoricMessages() {
            return this.mHistoricMessages;
        }

        public void addExtras(Bundle extras) {
            super.addExtras(extras);
            if (this.mUserDisplayName != null) {
                extras.putCharSequence(Notification.EXTRA_SELF_DISPLAY_NAME, this.mUserDisplayName);
            }
            if (this.mConversationTitle != null) {
                extras.putCharSequence(Notification.EXTRA_CONVERSATION_TITLE, this.mConversationTitle);
            }
            if (!this.mMessages.isEmpty()) {
                extras.putParcelableArray(Notification.EXTRA_MESSAGES, Message.getBundleArrayForMessages(this.mMessages));
            }
            if (!this.mHistoricMessages.isEmpty()) {
                extras.putParcelableArray(Notification.EXTRA_HISTORIC_MESSAGES, Message.getBundleArrayForMessages(this.mHistoricMessages));
            }
            fixTitleAndTextExtras(extras);
        }

        private void fixTitleAndTextExtras(Bundle extras) {
            CharSequence title;
            Message m = findLatestIncomingMessage();
            CharSequence text = m == null ? null : m.mText;
            CharSequence sender = m == null ? null : TextUtils.isEmpty(m.mSender) ? this.mUserDisplayName : m.mSender;
            if (TextUtils.isEmpty(this.mConversationTitle)) {
                title = sender;
            } else if (TextUtils.isEmpty(sender)) {
                title = this.mConversationTitle;
            } else {
                BidiFormatter bidi = BidiFormatter.getInstance();
                title = this.mBuilder.mContext.getString(17040512, bidi.unicodeWrap(this.mConversationTitle), bidi.unicodeWrap(m.mSender));
            }
            if (title != null) {
                extras.putCharSequence(Notification.EXTRA_TITLE, title);
            }
            if (text != null) {
                extras.putCharSequence(Notification.EXTRA_TEXT, text);
            }
        }

        protected void restoreFromExtras(Bundle extras) {
            super.restoreFromExtras(extras);
            this.mMessages.clear();
            this.mHistoricMessages.clear();
            this.mUserDisplayName = extras.getCharSequence(Notification.EXTRA_SELF_DISPLAY_NAME);
            this.mConversationTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE);
            Parcelable[] messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES);
            if (messages != null && (messages instanceof Parcelable[])) {
                this.mMessages = Message.getMessagesFromBundleArray(messages);
            }
            Parcelable[] histMessages = extras.getParcelableArray(Notification.EXTRA_HISTORIC_MESSAGES);
            if (histMessages != null && (histMessages instanceof Parcelable[])) {
                this.mHistoricMessages = Message.getMessagesFromBundleArray(histMessages);
            }
        }

        public RemoteViews makeContentView(boolean increasedHeight) {
            if (increasedHeight) {
                ArrayList<Action> actions = this.mBuilder.mActions;
                this.mBuilder.mActions = new ArrayList();
                RemoteViews remoteViews = makeBigContentView();
                this.mBuilder.mActions = actions;
                return remoteViews;
            }
            Message m = findLatestIncomingMessage();
            CharSequence title = this.mConversationTitle != null ? this.mConversationTitle : m == null ? null : m.mSender;
            CharSequence text = m == null ? null : this.mConversationTitle != null ? makeMessageLine(m, this.mBuilder) : m.mText;
            return this.mBuilder.applyStandardTemplate(this.mBuilder.getBaseLayoutResource(), this.mBuilder.mParams.reset().hasProgress(false).title(title).text(text));
        }

        private Message findLatestIncomingMessage() {
            for (int i = this.mMessages.size() - 1; i >= 0; i--) {
                Message m = (Message) this.mMessages.get(i);
                if (!TextUtils.isEmpty(m.mSender)) {
                    return m;
                }
            }
            if (this.mMessages.isEmpty()) {
                return null;
            }
            return (Message) this.mMessages.get(this.mMessages.size() - 1);
        }

        public RemoteViews makeBigContentView() {
            CharSequence title;
            if (TextUtils.isEmpty(this.mBigContentTitle)) {
                title = this.mConversationTitle;
            } else {
                title = this.mBigContentTitle;
            }
            boolean hasTitle = TextUtils.isEmpty(title) ^ 1;
            RemoteViews contentView;
            if (this.mMessages.size() == 1) {
                CharSequence bigTitle;
                CharSequence text;
                if (hasTitle) {
                    bigTitle = title;
                    text = makeMessageLine((Message) this.mMessages.get(0), this.mBuilder);
                } else {
                    bigTitle = ((Message) this.mMessages.get(0)).mSender;
                    text = ((Message) this.mMessages.get(0)).mText;
                }
                contentView = this.mBuilder.applyStandardTemplateWithActions(this.mBuilder.getBigTextLayoutResource(), this.mBuilder.mParams.reset().hasProgress(false).title(bigTitle).text(null));
                BigTextStyle.applyBigTextContentView(this.mBuilder, contentView, text);
                return contentView;
            }
            int rowId;
            Message m;
            contentView = this.mBuilder.applyStandardTemplateWithActions(this.mBuilder.getMessagingLayoutResource(), this.mBuilder.mParams.reset().hasProgress(false).title(title).text(null));
            int[] rowIds = new int[]{16908962, 16908963, 16908964, 16908965, 16908966, 16908967, 16908968};
            for (int rowId2 : rowIds) {
                contentView.setViewVisibility(rowId2, 8);
            }
            int i = 0;
            contentView.setViewLayoutMarginBottomDimen(16909021, hasTitle ? 17105168 : 0);
            String str = "setNumIndentLines";
            int i2 = !this.mBuilder.mN.hasLargeIcon() ? 0 : hasTitle ? 1 : 2;
            contentView.setInt(16909111, str, i2);
            int contractedChildId = -1;
            Message contractedMessage = findLatestIncomingMessage();
            int firstHistoricMessage = Math.max(0, this.mHistoricMessages.size() - (rowIds.length - this.mMessages.size()));
            while (firstHistoricMessage + i < this.mHistoricMessages.size() && i < rowIds.length) {
                m = (Message) this.mHistoricMessages.get(firstHistoricMessage + i);
                rowId2 = rowIds[i];
                contentView.setTextViewText(rowId2, makeMessageLine(m, this.mBuilder));
                if (contractedMessage == m) {
                    contractedChildId = rowId2;
                }
                i++;
            }
            for (int currentMessage = Math.max(0, this.mMessages.size() - rowIds.length); currentMessage < this.mMessages.size() && i < rowIds.length; currentMessage++) {
                m = (Message) this.mMessages.get(currentMessage);
                rowId2 = rowIds[i];
                contentView.setViewVisibility(rowId2, 0);
                contentView.setTextViewText(rowId2, makeMessageLine(m, this.mBuilder));
                this.mBuilder.setTextViewColorSecondary(contentView, rowId2);
                if (contractedMessage == m) {
                    contractedChildId = rowId2;
                }
                i++;
            }
            while (i < rowIds.length) {
                contentView.setTextViewText(rowIds[i], null);
                i++;
            }
            contentView.setInt(16909111, "setContractedChildId", contractedChildId);
            return contentView;
        }

        private CharSequence makeMessageLine(Message m, Builder builder) {
            BidiFormatter bidi = BidiFormatter.getInstance();
            SpannableStringBuilder sb = new SpannableStringBuilder();
            boolean colorize = builder.isColorized();
            CharSequence unicodeWrap;
            int primaryTextColor;
            if (TextUtils.isEmpty(m.mSender)) {
                unicodeWrap = bidi.unicodeWrap(this.mUserDisplayName == null ? ProxyInfo.LOCAL_EXCL_LIST : this.mUserDisplayName);
                if (colorize) {
                    primaryTextColor = builder.getPrimaryTextColor();
                } else {
                    primaryTextColor = this.mBuilder.resolveContrastColor();
                }
                sb.append(unicodeWrap, makeFontColorSpan(primaryTextColor), 0);
            } else {
                unicodeWrap = bidi.unicodeWrap(m.mSender);
                if (colorize) {
                    primaryTextColor = builder.getPrimaryTextColor();
                } else {
                    primaryTextColor = -16777216;
                }
                sb.append(unicodeWrap, makeFontColorSpan(primaryTextColor), 0);
            }
            sb.append("  ").append(bidi.unicodeWrap(m.mText == null ? ProxyInfo.LOCAL_EXCL_LIST : m.mText));
            return sb;
        }

        public RemoteViews makeHeadsUpContentView(boolean increasedHeight) {
            if (increasedHeight) {
                return makeBigContentView();
            }
            Message m = findLatestIncomingMessage();
            CharSequence title = this.mConversationTitle != null ? this.mConversationTitle : m == null ? null : m.mSender;
            CharSequence text = m == null ? null : this.mConversationTitle != null ? makeMessageLine(m, this.mBuilder) : m.mText;
            return this.mBuilder.applyStandardTemplateWithActions(this.mBuilder.getBigBaseLayoutResource(), this.mBuilder.mParams.reset().hasProgress(false).title(title).text(text));
        }

        private static TextAppearanceSpan makeFontColorSpan(int color) {
            return new TextAppearanceSpan(null, 0, 0, ColorStateList.valueOf(color), null);
        }
    }

    private static class StandardTemplateParams {
        boolean ambient;
        boolean hasProgress;
        CharSequence text;
        CharSequence title;

        /* synthetic */ StandardTemplateParams(StandardTemplateParams -this0) {
            this();
        }

        private StandardTemplateParams() {
            this.hasProgress = true;
            this.ambient = false;
        }

        final StandardTemplateParams reset() {
            this.hasProgress = true;
            this.ambient = false;
            this.title = null;
            this.text = null;
            return this;
        }

        final StandardTemplateParams hasProgress(boolean hasProgress) {
            this.hasProgress = hasProgress;
            return this;
        }

        final StandardTemplateParams title(CharSequence title) {
            this.title = title;
            return this;
        }

        final StandardTemplateParams text(CharSequence text) {
            this.text = text;
            return this;
        }

        final StandardTemplateParams ambient(boolean ambient) {
            boolean z = false;
            if (this.title == null && this.text == null) {
                z = true;
            }
            Preconditions.checkState(z, "must set ambient before text");
            this.ambient = ambient;
            return this;
        }

        final StandardTemplateParams fillTextsFrom(Builder b) {
            Bundle extras = b.mN.extras;
            this.title = b.processLegacyText(extras.getCharSequence(Notification.EXTRA_TITLE), this.ambient);
            this.text = b.processLegacyText(extras.getCharSequence(Notification.EXTRA_TEXT), this.ambient);
            return this;
        }
    }

    public static final class TvExtender implements Extender {
        private static final String EXTRA_CHANNEL_ID = "channel_id";
        private static final String EXTRA_CONTENT_INTENT = "content_intent";
        private static final String EXTRA_DELETE_INTENT = "delete_intent";
        private static final String EXTRA_FLAGS = "flags";
        private static final String EXTRA_TV_EXTENDER = "android.tv.EXTENSIONS";
        private static final int FLAG_AVAILABLE_ON_TV = 1;
        private static final String TAG = "TvExtender";
        private String mChannelId;
        private PendingIntent mContentIntent;
        private PendingIntent mDeleteIntent;
        private int mFlags;

        public TvExtender() {
            this.mFlags = 1;
        }

        public TvExtender(Notification notif) {
            Bundle bundle = notif.extras == null ? null : notif.extras.getBundle(EXTRA_TV_EXTENDER);
            if (bundle != null) {
                this.mFlags = bundle.getInt(EXTRA_FLAGS);
                this.mChannelId = bundle.getString("channel_id");
                this.mContentIntent = (PendingIntent) bundle.getParcelable(EXTRA_CONTENT_INTENT);
                this.mDeleteIntent = (PendingIntent) bundle.getParcelable(EXTRA_DELETE_INTENT);
            }
        }

        public Builder extend(Builder builder) {
            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_FLAGS, this.mFlags);
            bundle.putString("channel_id", this.mChannelId);
            if (this.mContentIntent != null) {
                bundle.putParcelable(EXTRA_CONTENT_INTENT, this.mContentIntent);
            }
            if (this.mDeleteIntent != null) {
                bundle.putParcelable(EXTRA_DELETE_INTENT, this.mDeleteIntent);
            }
            builder.getExtras().putBundle(EXTRA_TV_EXTENDER, bundle);
            return builder;
        }

        public boolean isAvailableOnTv() {
            return (this.mFlags & 1) != 0;
        }

        public TvExtender setChannel(String channelId) {
            this.mChannelId = channelId;
            return this;
        }

        public TvExtender setChannelId(String channelId) {
            this.mChannelId = channelId;
            return this;
        }

        @Deprecated
        public String getChannel() {
            return this.mChannelId;
        }

        public String getChannelId() {
            return this.mChannelId;
        }

        public TvExtender setContentIntent(PendingIntent intent) {
            this.mContentIntent = intent;
            return this;
        }

        public PendingIntent getContentIntent() {
            return this.mContentIntent;
        }

        public TvExtender setDeleteIntent(PendingIntent intent) {
            this.mDeleteIntent = intent;
            return this;
        }

        public PendingIntent getDeleteIntent() {
            return this.mDeleteIntent;
        }
    }

    public static final class WearableExtender implements Extender {
        private static final int DEFAULT_CONTENT_ICON_GRAVITY = 8388613;
        private static final int DEFAULT_FLAGS = 1;
        private static final int DEFAULT_GRAVITY = 80;
        private static final String EXTRA_WEARABLE_EXTENSIONS = "android.wearable.EXTENSIONS";
        private static final int FLAG_BIG_PICTURE_AMBIENT = 32;
        private static final int FLAG_CONTENT_INTENT_AVAILABLE_OFFLINE = 1;
        private static final int FLAG_HINT_AVOID_BACKGROUND_CLIPPING = 16;
        private static final int FLAG_HINT_CONTENT_INTENT_LAUNCHES_ACTIVITY = 64;
        private static final int FLAG_HINT_HIDE_ICON = 2;
        private static final int FLAG_HINT_SHOW_BACKGROUND_ONLY = 4;
        private static final int FLAG_START_SCROLL_BOTTOM = 8;
        private static final String KEY_ACTIONS = "actions";
        private static final String KEY_BACKGROUND = "background";
        private static final String KEY_BRIDGE_TAG = "bridgeTag";
        private static final String KEY_CONTENT_ACTION_INDEX = "contentActionIndex";
        private static final String KEY_CONTENT_ICON = "contentIcon";
        private static final String KEY_CONTENT_ICON_GRAVITY = "contentIconGravity";
        private static final String KEY_CUSTOM_CONTENT_HEIGHT = "customContentHeight";
        private static final String KEY_CUSTOM_SIZE_PRESET = "customSizePreset";
        private static final String KEY_DISMISSAL_ID = "dismissalId";
        private static final String KEY_DISPLAY_INTENT = "displayIntent";
        private static final String KEY_FLAGS = "flags";
        private static final String KEY_GRAVITY = "gravity";
        private static final String KEY_HINT_SCREEN_TIMEOUT = "hintScreenTimeout";
        private static final String KEY_PAGES = "pages";
        public static final int SCREEN_TIMEOUT_LONG = -1;
        public static final int SCREEN_TIMEOUT_SHORT = 0;
        public static final int SIZE_DEFAULT = 0;
        public static final int SIZE_FULL_SCREEN = 5;
        public static final int SIZE_LARGE = 4;
        public static final int SIZE_MEDIUM = 3;
        public static final int SIZE_SMALL = 2;
        public static final int SIZE_XSMALL = 1;
        public static final int UNSET_ACTION_INDEX = -1;
        private ArrayList<Action> mActions = new ArrayList();
        private Bitmap mBackground;
        private String mBridgeTag;
        private int mContentActionIndex = -1;
        private int mContentIcon;
        private int mContentIconGravity = DEFAULT_CONTENT_ICON_GRAVITY;
        private int mCustomContentHeight;
        private int mCustomSizePreset = 0;
        private String mDismissalId;
        private PendingIntent mDisplayIntent;
        private int mFlags = 1;
        private int mGravity = 80;
        private int mHintScreenTimeout;
        private ArrayList<Notification> mPages = new ArrayList();

        public WearableExtender(Notification notif) {
            Bundle wearableBundle = notif.extras.getBundle(EXTRA_WEARABLE_EXTENSIONS);
            if (wearableBundle != null) {
                List<Action> actions = wearableBundle.getParcelableArrayList(KEY_ACTIONS);
                if (actions != null) {
                    this.mActions.addAll(actions);
                }
                this.mFlags = wearableBundle.getInt(KEY_FLAGS, 1);
                this.mDisplayIntent = (PendingIntent) wearableBundle.getParcelable(KEY_DISPLAY_INTENT);
                Notification[] pages = Notification.getNotificationArrayFromBundle(wearableBundle, KEY_PAGES);
                if (pages != null) {
                    Collections.addAll(this.mPages, pages);
                }
                this.mBackground = (Bitmap) wearableBundle.getParcelable(KEY_BACKGROUND);
                this.mContentIcon = wearableBundle.getInt(KEY_CONTENT_ICON);
                this.mContentIconGravity = wearableBundle.getInt(KEY_CONTENT_ICON_GRAVITY, DEFAULT_CONTENT_ICON_GRAVITY);
                this.mContentActionIndex = wearableBundle.getInt(KEY_CONTENT_ACTION_INDEX, -1);
                this.mCustomSizePreset = wearableBundle.getInt(KEY_CUSTOM_SIZE_PRESET, 0);
                this.mCustomContentHeight = wearableBundle.getInt(KEY_CUSTOM_CONTENT_HEIGHT);
                this.mGravity = wearableBundle.getInt(KEY_GRAVITY, 80);
                this.mHintScreenTimeout = wearableBundle.getInt(KEY_HINT_SCREEN_TIMEOUT);
                this.mDismissalId = wearableBundle.getString(KEY_DISMISSAL_ID);
                this.mBridgeTag = wearableBundle.getString(KEY_BRIDGE_TAG);
            }
        }

        public Builder extend(Builder builder) {
            Bundle wearableBundle = new Bundle();
            if (!this.mActions.isEmpty()) {
                wearableBundle.putParcelableArrayList(KEY_ACTIONS, this.mActions);
            }
            if (this.mFlags != 1) {
                wearableBundle.putInt(KEY_FLAGS, this.mFlags);
            }
            if (this.mDisplayIntent != null) {
                wearableBundle.putParcelable(KEY_DISPLAY_INTENT, this.mDisplayIntent);
            }
            if (!this.mPages.isEmpty()) {
                wearableBundle.putParcelableArray(KEY_PAGES, (Parcelable[]) this.mPages.toArray(new Notification[this.mPages.size()]));
            }
            if (this.mBackground != null) {
                wearableBundle.putParcelable(KEY_BACKGROUND, this.mBackground);
            }
            if (this.mContentIcon != 0) {
                wearableBundle.putInt(KEY_CONTENT_ICON, this.mContentIcon);
            }
            if (this.mContentIconGravity != DEFAULT_CONTENT_ICON_GRAVITY) {
                wearableBundle.putInt(KEY_CONTENT_ICON_GRAVITY, this.mContentIconGravity);
            }
            if (this.mContentActionIndex != -1) {
                wearableBundle.putInt(KEY_CONTENT_ACTION_INDEX, this.mContentActionIndex);
            }
            if (this.mCustomSizePreset != 0) {
                wearableBundle.putInt(KEY_CUSTOM_SIZE_PRESET, this.mCustomSizePreset);
            }
            if (this.mCustomContentHeight != 0) {
                wearableBundle.putInt(KEY_CUSTOM_CONTENT_HEIGHT, this.mCustomContentHeight);
            }
            if (this.mGravity != 80) {
                wearableBundle.putInt(KEY_GRAVITY, this.mGravity);
            }
            if (this.mHintScreenTimeout != 0) {
                wearableBundle.putInt(KEY_HINT_SCREEN_TIMEOUT, this.mHintScreenTimeout);
            }
            if (this.mDismissalId != null) {
                wearableBundle.putString(KEY_DISMISSAL_ID, this.mDismissalId);
            }
            if (this.mBridgeTag != null) {
                wearableBundle.putString(KEY_BRIDGE_TAG, this.mBridgeTag);
            }
            builder.getExtras().putBundle(EXTRA_WEARABLE_EXTENSIONS, wearableBundle);
            return builder;
        }

        public WearableExtender clone() {
            WearableExtender that = new WearableExtender();
            that.mActions = new ArrayList(this.mActions);
            that.mFlags = this.mFlags;
            that.mDisplayIntent = this.mDisplayIntent;
            that.mPages = new ArrayList(this.mPages);
            that.mBackground = this.mBackground;
            that.mContentIcon = this.mContentIcon;
            that.mContentIconGravity = this.mContentIconGravity;
            that.mContentActionIndex = this.mContentActionIndex;
            that.mCustomSizePreset = this.mCustomSizePreset;
            that.mCustomContentHeight = this.mCustomContentHeight;
            that.mGravity = this.mGravity;
            that.mHintScreenTimeout = this.mHintScreenTimeout;
            that.mDismissalId = this.mDismissalId;
            that.mBridgeTag = this.mBridgeTag;
            return that;
        }

        public WearableExtender addAction(Action action) {
            this.mActions.add(action);
            return this;
        }

        public WearableExtender addActions(List<Action> actions) {
            this.mActions.addAll(actions);
            return this;
        }

        public WearableExtender clearActions() {
            this.mActions.clear();
            return this;
        }

        public List<Action> getActions() {
            return this.mActions;
        }

        public WearableExtender setDisplayIntent(PendingIntent intent) {
            this.mDisplayIntent = intent;
            return this;
        }

        public PendingIntent getDisplayIntent() {
            return this.mDisplayIntent;
        }

        public WearableExtender addPage(Notification page) {
            this.mPages.add(page);
            return this;
        }

        public WearableExtender addPages(List<Notification> pages) {
            this.mPages.addAll(pages);
            return this;
        }

        public WearableExtender clearPages() {
            this.mPages.clear();
            return this;
        }

        public List<Notification> getPages() {
            return this.mPages;
        }

        public WearableExtender setBackground(Bitmap background) {
            this.mBackground = background;
            return this;
        }

        public Bitmap getBackground() {
            return this.mBackground;
        }

        public WearableExtender setContentIcon(int icon) {
            this.mContentIcon = icon;
            return this;
        }

        public int getContentIcon() {
            return this.mContentIcon;
        }

        public WearableExtender setContentIconGravity(int contentIconGravity) {
            this.mContentIconGravity = contentIconGravity;
            return this;
        }

        public int getContentIconGravity() {
            return this.mContentIconGravity;
        }

        public WearableExtender setContentAction(int actionIndex) {
            this.mContentActionIndex = actionIndex;
            return this;
        }

        public int getContentAction() {
            return this.mContentActionIndex;
        }

        public WearableExtender setGravity(int gravity) {
            this.mGravity = gravity;
            return this;
        }

        public int getGravity() {
            return this.mGravity;
        }

        public WearableExtender setCustomSizePreset(int sizePreset) {
            this.mCustomSizePreset = sizePreset;
            return this;
        }

        public int getCustomSizePreset() {
            return this.mCustomSizePreset;
        }

        public WearableExtender setCustomContentHeight(int height) {
            this.mCustomContentHeight = height;
            return this;
        }

        public int getCustomContentHeight() {
            return this.mCustomContentHeight;
        }

        public WearableExtender setStartScrollBottom(boolean startScrollBottom) {
            setFlag(8, startScrollBottom);
            return this;
        }

        public boolean getStartScrollBottom() {
            return (this.mFlags & 8) != 0;
        }

        public WearableExtender setContentIntentAvailableOffline(boolean contentIntentAvailableOffline) {
            setFlag(1, contentIntentAvailableOffline);
            return this;
        }

        public boolean getContentIntentAvailableOffline() {
            return (this.mFlags & 1) != 0;
        }

        public WearableExtender setHintHideIcon(boolean hintHideIcon) {
            setFlag(2, hintHideIcon);
            return this;
        }

        public boolean getHintHideIcon() {
            return (this.mFlags & 2) != 0;
        }

        public WearableExtender setHintShowBackgroundOnly(boolean hintShowBackgroundOnly) {
            setFlag(4, hintShowBackgroundOnly);
            return this;
        }

        public boolean getHintShowBackgroundOnly() {
            return (this.mFlags & 4) != 0;
        }

        public WearableExtender setHintAvoidBackgroundClipping(boolean hintAvoidBackgroundClipping) {
            setFlag(16, hintAvoidBackgroundClipping);
            return this;
        }

        public boolean getHintAvoidBackgroundClipping() {
            return (this.mFlags & 16) != 0;
        }

        public WearableExtender setHintScreenTimeout(int timeout) {
            this.mHintScreenTimeout = timeout;
            return this;
        }

        public int getHintScreenTimeout() {
            return this.mHintScreenTimeout;
        }

        public WearableExtender setHintAmbientBigPicture(boolean hintAmbientBigPicture) {
            setFlag(32, hintAmbientBigPicture);
            return this;
        }

        public boolean getHintAmbientBigPicture() {
            return (this.mFlags & 32) != 0;
        }

        public WearableExtender setHintContentIntentLaunchesActivity(boolean hintContentIntentLaunchesActivity) {
            setFlag(64, hintContentIntentLaunchesActivity);
            return this;
        }

        public boolean getHintContentIntentLaunchesActivity() {
            return (this.mFlags & 64) != 0;
        }

        public WearableExtender setDismissalId(String dismissalId) {
            this.mDismissalId = dismissalId;
            return this;
        }

        public String getDismissalId() {
            return this.mDismissalId;
        }

        public WearableExtender setBridgeTag(String bridgeTag) {
            this.mBridgeTag = bridgeTag;
            return this;
        }

        public String getBridgeTag() {
            return this.mBridgeTag;
        }

        private void setFlag(int mask, boolean value) {
            if (value) {
                this.mFlags |= mask;
            } else {
                this.mFlags &= ~mask;
            }
        }
    }

    public String getGroup() {
        return this.mGroupKey;
    }

    public void setGroup(String groupKey) {
        this.mGroupKey = groupKey;
    }

    public String getSortKey() {
        return this.mSortKey;
    }

    public Notification() {
        this.number = 0;
        this.audioStreamType = -1;
        this.audioAttributes = AUDIO_ATTRIBUTES_DEFAULT;
        this.color = 0;
        this.mSyncLock = new Object();
        this.extras = new Bundle();
        this.mGroupAlertBehavior = 0;
        this.mBadgeIcon = 0;
        this.when = System.currentTimeMillis();
        this.creationTime = System.currentTimeMillis();
        this.priority = 0;
    }

    public Notification(Context context, int icon, CharSequence tickerText, long when, CharSequence contentTitle, CharSequence contentText, Intent contentIntent) {
        this.number = 0;
        this.audioStreamType = -1;
        this.audioAttributes = AUDIO_ATTRIBUTES_DEFAULT;
        this.color = 0;
        this.mSyncLock = new Object();
        this.extras = new Bundle();
        this.mGroupAlertBehavior = 0;
        this.mBadgeIcon = 0;
        new Builder(context).setWhen(when).setSmallIcon(icon).setTicker(tickerText).setContentTitle(contentTitle).setContentText(contentText).setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, 0)).buildInto(this);
    }

    @Deprecated
    public Notification(int icon, CharSequence tickerText, long when) {
        this.number = 0;
        this.audioStreamType = -1;
        this.audioAttributes = AUDIO_ATTRIBUTES_DEFAULT;
        this.color = 0;
        this.mSyncLock = new Object();
        this.extras = new Bundle();
        this.mGroupAlertBehavior = 0;
        this.mBadgeIcon = 0;
        this.icon = icon;
        this.tickerText = tickerText;
        this.when = when;
        this.creationTime = System.currentTimeMillis();
    }

    public Notification(Parcel parcel) {
        this.number = 0;
        this.audioStreamType = -1;
        this.audioAttributes = AUDIO_ATTRIBUTES_DEFAULT;
        this.color = 0;
        this.mSyncLock = new Object();
        this.extras = new Bundle();
        this.mGroupAlertBehavior = 0;
        this.mBadgeIcon = 0;
        readFromParcelImpl(parcel);
        this.allPendingIntents = parcel.readArraySet(null);
    }

    private void readFromParcelImpl(Parcel parcel) {
        int version = parcel.readInt();
        whitelistToken = parcel.readStrongBinder();
        if (whitelistToken == null) {
            whitelistToken = processWhitelistToken;
        }
        parcel.setClassCookie(PendingIntent.class, whitelistToken);
        this.when = parcel.readLong();
        this.creationTime = parcel.readLong();
        if (parcel.readInt() != 0) {
            this.mSmallIcon = (Icon) Icon.CREATOR.createFromParcel(parcel);
            if (this.mSmallIcon.getType() == 2) {
                this.icon = this.mSmallIcon.getResId();
            }
        }
        this.number = parcel.readInt();
        if (parcel.readInt() != 0) {
            this.contentIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(parcel);
        }
        if (parcel.readInt() != 0) {
            this.deleteIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(parcel);
        }
        if (parcel.readInt() != 0) {
            this.tickerText = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        }
        if (parcel.readInt() != 0) {
            this.tickerView = (RemoteViews) RemoteViews.CREATOR.createFromParcel(parcel);
        }
        if (parcel.readInt() != 0) {
            this.contentView = (RemoteViews) RemoteViews.CREATOR.createFromParcel(parcel);
        }
        if (parcel.readInt() != 0) {
            this.mLargeIcon = (Icon) Icon.CREATOR.createFromParcel(parcel);
        }
        this.defaults = parcel.readInt();
        this.flags = parcel.readInt();
        if (parcel.readInt() != 0) {
            this.sound = (Uri) Uri.CREATOR.createFromParcel(parcel);
        }
        this.audioStreamType = parcel.readInt();
        if (parcel.readInt() != 0) {
            this.audioAttributes = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(parcel);
        }
        this.vibrate = parcel.createLongArray();
        this.ledARGB = parcel.readInt();
        this.ledOnMS = parcel.readInt();
        this.ledOffMS = parcel.readInt();
        this.iconLevel = parcel.readInt();
        if (parcel.readInt() != 0) {
            this.fullScreenIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(parcel);
        }
        this.priority = parcel.readInt();
        this.category = parcel.readString();
        this.mGroupKey = parcel.readString();
        this.mSortKey = parcel.readString();
        this.extras = Bundle.setDefusable(parcel.readBundle(), true);
        this.actions = (Action[]) parcel.createTypedArray(Action.CREATOR);
        if (parcel.readInt() != 0) {
            this.bigContentView = (RemoteViews) RemoteViews.CREATOR.createFromParcel(parcel);
        }
        if (parcel.readInt() != 0) {
            this.headsUpContentView = (RemoteViews) RemoteViews.CREATOR.createFromParcel(parcel);
        }
        this.visibility = parcel.readInt();
        if (parcel.readInt() != 0) {
            this.publicVersion = (Notification) CREATOR.createFromParcel(parcel);
        }
        this.color = parcel.readInt();
        if (parcel.readInt() != 0) {
            this.mChannelId = parcel.readString();
        }
        this.mTimeout = parcel.readLong();
        if (parcel.readInt() != 0) {
            this.mShortcutId = parcel.readString();
        }
        this.mBadgeIcon = parcel.readInt();
        if (parcel.readInt() != 0) {
            this.mSettingsText = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        }
        this.mGroupAlertBehavior = parcel.readInt();
    }

    public Notification clone() {
        Notification that = new Notification();
        cloneInto(that, true);
        return that;
    }

    public void cloneInto(Notification that, boolean heavy) {
        whitelistToken = whitelistToken;
        that.when = this.when;
        that.creationTime = this.creationTime;
        that.mSmallIcon = this.mSmallIcon;
        that.number = this.number;
        that.contentIntent = this.contentIntent;
        that.deleteIntent = this.deleteIntent;
        that.fullScreenIntent = this.fullScreenIntent;
        if (this.tickerText != null) {
            that.tickerText = this.tickerText.toString();
        }
        if (heavy && this.tickerView != null) {
            that.tickerView = this.tickerView.clone();
        }
        if (heavy && this.contentView != null) {
            that.contentView = this.contentView.clone();
        }
        if (heavy && this.mLargeIcon != null) {
            that.mLargeIcon = this.mLargeIcon;
        }
        that.iconLevel = this.iconLevel;
        that.sound = this.sound;
        that.audioStreamType = this.audioStreamType;
        if (this.audioAttributes != null) {
            that.audioAttributes = new android.media.AudioAttributes.Builder(this.audioAttributes).build();
        }
        long[] vibrate = this.vibrate;
        if (vibrate != null) {
            int N = vibrate.length;
            long[] vib = new long[N];
            that.vibrate = vib;
            System.arraycopy(vibrate, 0, vib, 0, N);
        }
        that.ledARGB = this.ledARGB;
        that.ledOnMS = this.ledOnMS;
        that.ledOffMS = this.ledOffMS;
        that.defaults = this.defaults;
        that.flags = this.flags;
        that.priority = this.priority;
        that.category = this.category;
        that.mGroupKey = this.mGroupKey;
        that.mSortKey = this.mSortKey;
        if (this.extras != null) {
            try {
                that.extras = new Bundle(this.extras);
                that.extras.size();
            } catch (BadParcelableException e) {
                Log.e(TAG, "could not unparcel extras from notification: " + this, e);
                that.extras = null;
            }
        }
        if (!ArrayUtils.isEmpty(this.allPendingIntents)) {
            that.allPendingIntents = new ArraySet(this.allPendingIntents);
        }
        if (this.actions != null) {
            that.actions = new Action[this.actions.length];
            for (int i = 0; i < this.actions.length; i++) {
                if (this.actions[i] != null) {
                    that.actions[i] = this.actions[i].clone();
                }
            }
        }
        if (heavy && this.bigContentView != null) {
            that.bigContentView = this.bigContentView.clone();
        }
        if (heavy && this.headsUpContentView != null) {
            that.headsUpContentView = this.headsUpContentView.clone();
        }
        that.visibility = this.visibility;
        if (this.publicVersion != null) {
            that.publicVersion = new Notification();
            this.publicVersion.cloneInto(that.publicVersion, heavy);
        }
        that.color = this.color;
        that.mChannelId = this.mChannelId;
        that.mTimeout = this.mTimeout;
        that.mShortcutId = this.mShortcutId;
        that.mBadgeIcon = this.mBadgeIcon;
        that.mSettingsText = this.mSettingsText;
        that.mGroupAlertBehavior = this.mGroupAlertBehavior;
        if (!heavy) {
            that.lightenPayload();
        }
    }

    public final void lightenPayload() {
        this.tickerView = null;
        this.contentView = null;
        this.bigContentView = null;
        this.headsUpContentView = null;
        this.mLargeIcon = null;
        if (this.extras != null && (this.extras.isEmpty() ^ 1) != 0) {
            Set<String> keyset = this.extras.keySet();
            int N = keyset.size();
            String[] keys = (String[]) keyset.toArray(new String[N]);
            for (int i = 0; i < N; i++) {
                String key = keys[i];
                if (!"android.tv.EXTENSIONS".equals(key)) {
                    Object obj = this.extras.get(key);
                    if (obj != null && ((obj instanceof Parcelable) || (obj instanceof Parcelable[]) || (obj instanceof SparseArray) || (obj instanceof ArrayList))) {
                        this.extras.remove(key);
                    }
                }
            }
        }
    }

    public static CharSequence safeCharSequence(CharSequence cs) {
        if (cs == null) {
            return cs;
        }
        if (cs.length() > 5120) {
            cs = cs.subSequence(0, 5120);
        }
        if (!(cs instanceof Parcelable)) {
            return removeTextSizeSpans(cs);
        }
        Log.e(TAG, "warning: " + cs.getClass().getCanonicalName() + " instance is a custom Parcelable and not allowed in Notification");
        return cs.toString();
    }

    private static CharSequence removeTextSizeSpans(CharSequence charSequence) {
        if (!(charSequence instanceof Spanned)) {
            return charSequence;
        }
        Spanned ss = (Spanned) charSequence;
        Object[] spans = ss.getSpans(0, ss.length(), Object.class);
        SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
        for (TextAppearanceSpan span : spans) {
            Object resultSpan;
            TextAppearanceSpan resultSpan2 = span;
            if (span instanceof CharacterStyle) {
                resultSpan2 = span.getUnderlying();
            }
            if (resultSpan2 instanceof TextAppearanceSpan) {
                TextAppearanceSpan originalSpan = resultSpan2;
                resultSpan = new TextAppearanceSpan(originalSpan.getFamily(), originalSpan.getTextStyle(), -1, originalSpan.getTextColor(), originalSpan.getLinkTextColor());
            } else {
                if (!((resultSpan2 instanceof RelativeSizeSpan) || (resultSpan2 instanceof AbsoluteSizeSpan))) {
                    resultSpan2 = span;
                }
            }
            builder.setSpan(resultSpan, ss.getSpanStart(span), ss.getSpanEnd(span), ss.getSpanFlags(span));
        }
        return builder;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        boolean collectPendingIntents = this.allPendingIntents == null;
        if (collectPendingIntents) {
            PendingIntent.setOnMarshaledListener(new -$Lambda$vZ1qb742P9hE4drBY-TrOZB_qKo(this, parcel));
        }
        try {
            writeToParcelImpl(parcel, flags);
            parcel.writeArraySet(this.allPendingIntents);
        } finally {
            if (collectPendingIntents) {
                PendingIntent.setOnMarshaledListener(null);
            }
        }
    }

    /* synthetic */ void lambda$-android_app_Notification_86154(Parcel parcel, PendingIntent intent, Parcel out, int outFlags) {
        synchronized (this.mSyncLock) {
            if (parcel == out) {
                if (this.allPendingIntents == null) {
                    this.allPendingIntents = new ArraySet();
                }
                this.allPendingIntents.add(intent);
            }
        }
    }

    private void writeToParcelImpl(Parcel parcel, int flags) {
        parcel.writeInt(1);
        parcel.writeStrongBinder(whitelistToken);
        parcel.writeLong(this.when);
        parcel.writeLong(this.creationTime);
        if (this.mSmallIcon == null && this.icon != 0) {
            this.mSmallIcon = Icon.createWithResource(ProxyInfo.LOCAL_EXCL_LIST, this.icon);
        }
        if (this.mSmallIcon != null) {
            parcel.writeInt(1);
            this.mSmallIcon.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.number);
        if (this.contentIntent != null) {
            parcel.writeInt(1);
            this.contentIntent.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        if (this.deleteIntent != null) {
            parcel.writeInt(1);
            this.deleteIntent.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        if (this.tickerText != null) {
            parcel.writeInt(1);
            TextUtils.writeToParcel(this.tickerText, parcel, flags);
        } else {
            parcel.writeInt(0);
        }
        if (this.tickerView != null) {
            parcel.writeInt(1);
            this.tickerView.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        if (this.contentView != null) {
            parcel.writeInt(1);
            this.contentView.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        if (this.mLargeIcon == null && this.largeIcon != null) {
            this.mLargeIcon = Icon.createWithBitmap(this.largeIcon);
        }
        if (this.mLargeIcon != null) {
            parcel.writeInt(1);
            this.mLargeIcon.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.defaults);
        parcel.writeInt(this.flags);
        if (this.sound != null) {
            parcel.writeInt(1);
            this.sound.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.audioStreamType);
        if (this.audioAttributes != null) {
            parcel.writeInt(1);
            this.audioAttributes.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeLongArray(this.vibrate);
        parcel.writeInt(this.ledARGB);
        parcel.writeInt(this.ledOnMS);
        parcel.writeInt(this.ledOffMS);
        parcel.writeInt(this.iconLevel);
        if (this.fullScreenIntent != null) {
            parcel.writeInt(1);
            this.fullScreenIntent.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.priority);
        parcel.writeString(this.category);
        parcel.writeString(this.mGroupKey);
        parcel.writeString(this.mSortKey);
        parcel.writeBundle(this.extras);
        parcel.writeTypedArray(this.actions, 0);
        if (this.bigContentView != null) {
            parcel.writeInt(1);
            this.bigContentView.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        if (this.headsUpContentView != null) {
            parcel.writeInt(1);
            this.headsUpContentView.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.visibility);
        if (this.publicVersion != null) {
            parcel.writeInt(1);
            this.publicVersion.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.color);
        if (this.mChannelId != null) {
            parcel.writeInt(1);
            parcel.writeString(this.mChannelId);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeLong(this.mTimeout);
        if (this.mShortcutId != null) {
            parcel.writeInt(1);
            parcel.writeString(this.mShortcutId);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.mBadgeIcon);
        if (this.mSettingsText != null) {
            parcel.writeInt(1);
            TextUtils.writeToParcel(this.mSettingsText, parcel, flags);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.mGroupAlertBehavior);
    }

    @Deprecated
    public void setLatestEventInfo(Context context, CharSequence contentTitle, CharSequence contentText, PendingIntent contentIntent) {
        if (context.getApplicationInfo().targetSdkVersion > 22) {
            Log.e(TAG, "setLatestEventInfo() is deprecated and you should feel deprecated.", new Throwable());
        }
        if (context.getApplicationInfo().targetSdkVersion < 24) {
            this.extras.putBoolean(EXTRA_SHOW_WHEN, true);
        }
        Builder builder = new Builder(context, this);
        if (contentTitle != null) {
            builder.setContentTitle(contentTitle);
        }
        if (contentText != null) {
            builder.setContentText(contentText);
        }
        builder.setContentIntent(contentIntent);
        builder.build();
    }

    public static void addFieldsFromContext(Context context, Notification notification) {
        addFieldsFromContext(context.getApplicationInfo(), notification);
    }

    public static void addFieldsFromContext(ApplicationInfo ai, Notification notification) {
        notification.extras.putParcelable(EXTRA_BUILDER_APPLICATION_INFO, ai);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Notification(channel=");
        sb.append(getChannelId());
        sb.append(" pri=");
        sb.append(this.priority);
        sb.append(" contentView=");
        if (this.contentView != null) {
            sb.append(this.contentView.getPackage());
            sb.append("/0x");
            sb.append(Integer.toHexString(this.contentView.getLayoutId()));
        } else {
            sb.append("null");
        }
        sb.append(" vibrate=");
        if ((this.defaults & 2) != 0) {
            sb.append("default");
        } else if (this.vibrate != null) {
            int N = this.vibrate.length - 1;
            sb.append("[");
            for (int i = 0; i < N; i++) {
                sb.append(this.vibrate[i]);
                sb.append(',');
            }
            if (N != -1) {
                sb.append(this.vibrate[N]);
            }
            sb.append("]");
        } else {
            sb.append("null");
        }
        sb.append(" sound=");
        if ((this.defaults & 1) != 0) {
            sb.append("default");
        } else if (this.sound != null) {
            sb.append(this.sound.toString());
        } else {
            sb.append("null");
        }
        if (this.tickerText != null) {
            sb.append(" tick");
        }
        sb.append(" defaults=0x");
        sb.append(Integer.toHexString(this.defaults));
        sb.append(" flags=0x");
        sb.append(Integer.toHexString(this.flags));
        sb.append(String.format(" color=0x%08x", new Object[]{Integer.valueOf(this.color)}));
        if (this.category != null) {
            sb.append(" category=");
            sb.append(this.category);
        }
        if (this.mGroupKey != null) {
            sb.append(" groupKey=");
            sb.append(this.mGroupKey);
        }
        if (this.mSortKey != null) {
            sb.append(" sortKey=");
            sb.append(this.mSortKey);
        }
        if (this.actions != null) {
            sb.append(" actions=");
            sb.append(this.actions.length);
        }
        sb.append(" vis=");
        sb.append(visibilityToString(this.visibility));
        if (this.publicVersion != null) {
            sb.append(" publicVersion=");
            sb.append(this.publicVersion.toString());
        }
        sb.append(")");
        return sb.toString();
    }

    public static String visibilityToString(int vis) {
        switch (vis) {
            case -1:
                return "SECRET";
            case 0:
                return "PRIVATE";
            case 1:
                return "PUBLIC";
            default:
                return "UNKNOWN(" + String.valueOf(vis) + ")";
        }
    }

    public static String priorityToString(int pri) {
        switch (pri) {
            case -2:
                return "MIN";
            case -1:
                return "LOW";
            case 0:
                return "DEFAULT";
            case 1:
                return "HIGH";
            case 2:
                return "MAX";
            default:
                return "UNKNOWN(" + String.valueOf(pri) + ")";
        }
    }

    @Deprecated
    public String getChannel() {
        return this.mChannelId;
    }

    public String getChannelId() {
        return this.mChannelId;
    }

    @Deprecated
    public long getTimeout() {
        return this.mTimeout;
    }

    public long getTimeoutAfter() {
        return this.mTimeout;
    }

    public int getBadgeIconType() {
        return this.mBadgeIcon;
    }

    public String getShortcutId() {
        return this.mShortcutId;
    }

    public CharSequence getSettingsText() {
        return this.mSettingsText;
    }

    public int getGroupAlertBehavior() {
        return this.mGroupAlertBehavior;
    }

    public Icon getSmallIcon() {
        return this.mSmallIcon;
    }

    public void setSmallIcon(Icon icon) {
        this.mSmallIcon = icon;
    }

    public Icon getLargeIcon() {
        return this.mLargeIcon;
    }

    public void setLargeIcon(Icon icon) {
        this.mLargeIcon = icon;
    }

    public boolean isGroupSummary() {
        return (this.mGroupKey == null || (this.flags & 512) == 0) ? false : true;
    }

    public boolean isGroupChild() {
        return this.mGroupKey != null && (this.flags & 512) == 0;
    }

    public boolean suppressAlertingDueToGrouping() {
        if (isGroupSummary() && getGroupAlertBehavior() == 2) {
            return true;
        }
        if (isGroupChild() && getGroupAlertBehavior() == 1) {
            return true;
        }
        return false;
    }

    void reduceImageSizes(Context context) {
        if (!this.extras.getBoolean(EXTRA_REDUCED_IMAGES)) {
            if (!(this.mLargeIcon == null && this.largeIcon == null)) {
                Resources resources = context.getResources();
                Class<? extends Style> style = getNotificationStyle();
                int maxWidth = resources.getDimensionPixelSize(17105299);
                int maxHeight = maxWidth;
                if (MediaStyle.class.equals(style) || DecoratedMediaCustomViewStyle.class.equals(style)) {
                    maxHeight = resources.getDimensionPixelSize(17105297);
                    maxWidth = resources.getDimensionPixelSize(17105298);
                }
                if (this.mLargeIcon != null) {
                    this.mLargeIcon.scaleDownIfNecessary(maxWidth, maxHeight);
                }
                if (this.largeIcon != null) {
                    this.largeIcon = Icon.scaleDownIfNecessary(this.largeIcon, maxWidth, maxHeight);
                }
            }
            reduceImageSizesForRemoteView(this.contentView, context);
            reduceImageSizesForRemoteView(this.headsUpContentView, context);
            reduceImageSizesForRemoteView(this.bigContentView, context);
            this.extras.putBoolean(EXTRA_REDUCED_IMAGES, true);
        }
    }

    private void reduceImageSizesForRemoteView(RemoteViews remoteView, Context context) {
        if (remoteView != null) {
            Resources resources = context.getResources();
            remoteView.reduceImageSizes(resources.getDimensionPixelSize(17105296), resources.getDimensionPixelSize(17105295));
        }
    }

    private boolean isForegroundService() {
        return (this.flags & 64) != 0;
    }

    public boolean hasMediaSession() {
        return this.extras.getParcelable(EXTRA_MEDIA_SESSION) != null;
    }

    public Class<? extends Style> getNotificationStyle() {
        String templateClass = this.extras.getString(EXTRA_TEMPLATE);
        if (TextUtils.isEmpty(templateClass)) {
            return null;
        }
        return getNotificationStyleClass(templateClass);
    }

    public boolean isColorized() {
        if (isColorizedMedia()) {
            return true;
        }
        return this.extras.getBoolean(EXTRA_COLORIZED) ? isForegroundService() : false;
    }

    public boolean isColorizedMedia() {
        Class<? extends Style> style = getNotificationStyle();
        if (MediaStyle.class.equals(style)) {
            Boolean colorized = (Boolean) this.extras.get(EXTRA_COLORIZED);
            if ((colorized == null || colorized.booleanValue()) && hasMediaSession()) {
                return true;
            }
        } else if (DecoratedMediaCustomViewStyle.class.equals(style) && this.extras.getBoolean(EXTRA_COLORIZED) && hasMediaSession()) {
            return true;
        }
        return false;
    }

    public boolean isMediaNotification() {
        Class<? extends Style> style = getNotificationStyle();
        if (MediaStyle.class.equals(style) || DecoratedMediaCustomViewStyle.class.equals(style)) {
            return true;
        }
        return false;
    }

    private boolean hasLargeIcon() {
        return (this.mLargeIcon == null && this.largeIcon == null) ? false : true;
    }

    public boolean showsTime() {
        return this.when != 0 ? this.extras.getBoolean(EXTRA_SHOW_WHEN) : false;
    }

    public boolean showsChronometer() {
        return this.when != 0 ? this.extras.getBoolean(EXTRA_SHOW_CHRONOMETER) : false;
    }

    public static Class<? extends Style> getNotificationStyleClass(String templateClass) {
        int i = 0;
        Class<? extends Style>[] classes = new Class[]{BigTextStyle.class, BigPictureStyle.class, InboxStyle.class, MediaStyle.class, DecoratedCustomViewStyle.class, DecoratedMediaCustomViewStyle.class, MessagingStyle.class};
        int length = classes.length;
        while (i < length) {
            Class<? extends Style> innerClass = classes[i];
            if (templateClass.equals(innerClass.getName())) {
                return innerClass;
            }
            i++;
        }
        return null;
    }

    private static Notification[] getNotificationArrayFromBundle(Bundle bundle, String key) {
        Parcelable[] array = bundle.getParcelableArray(key);
        if ((array instanceof Notification[]) || array == null) {
            return (Notification[]) array;
        }
        Notification[] typedArray = (Notification[]) Arrays.copyOf(array, array.length, Notification[].class);
        bundle.putParcelableArray(key, typedArray);
        return typedArray;
    }
}
