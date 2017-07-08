package android.app;

import android.Manifest.permission;
import android.R;
import android.app.PendingIntent.OnMarshaledListener;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hwtheme.HwThemeManager;
import android.media.AudioAttributes;
import android.media.session.MediaSession.Token;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.BidiFormatter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.HwNotificationColorUtil;
import com.android.internal.util.NotificationColorUtil;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Notification implements Parcelable {
    public static final AudioAttributes AUDIO_ATTRIBUTES_DEFAULT = null;
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
    private static final int COLOR_INVALID = 1;
    public static final Creator<Notification> CREATOR = null;
    public static final int DEFAULT_ALL = -1;
    public static final int DEFAULT_LIGHTS = 4;
    public static final int DEFAULT_SOUND = 1;
    public static final int DEFAULT_VIBRATE = 2;
    public static final String EXTRA_ALLOW_DURING_SETUP = "android.allowDuringSetup";
    public static final String EXTRA_APP_NAME = "android.extraAppName";
    public static final String EXTRA_BACKGROUND_IMAGE_URI = "android.backgroundImageUri";
    public static final String EXTRA_BIG_TEXT = "android.bigText";
    public static final String EXTRA_BUILDER_APPLICATION_INFO = "android.appInfo";
    public static final String EXTRA_CHRONOMETER_COUNT_DOWN = "android.chronometerCountDown";
    public static final String EXTRA_COMPACT_ACTIONS = "android.compactActions";
    public static final String EXTRA_CONTAINS_CUSTOM_VIEW = "android.contains.customView";
    public static final String EXTRA_CONVERSATION_TITLE = "android.conversationTitle";
    public static final String EXTRA_HW_IS_FROM_CLONED_PROCESS = "com.huawei.isClonedProcess";
    public static final String EXTRA_HW_IS_INTENT_PROTECTED_APP = "com.huawei.isIntentProtectedApp";
    public static final String EXTRA_INFO_TEXT = "android.infoText";
    public static final String EXTRA_LARGE_ICON = "android.largeIcon";
    public static final String EXTRA_LARGE_ICON_BIG = "android.largeIcon.big";
    public static final String EXTRA_MEDIA_SESSION = "android.mediaSession";
    public static final String EXTRA_MESSAGES = "android.messages";
    public static final String EXTRA_ORIGINATING_USERID = "android.originatingUserId";
    public static final String EXTRA_PEOPLE = "android.people";
    public static final String EXTRA_PICTURE = "android.picture";
    public static final String EXTRA_PROGRESS = "android.progress";
    public static final String EXTRA_PROGRESS_INDETERMINATE = "android.progressIndeterminate";
    public static final String EXTRA_PROGRESS_MAX = "android.progressMax";
    public static final String EXTRA_REMOTE_INPUT_HISTORY = "android.remoteInputHistory";
    public static final String EXTRA_SELF_DISPLAY_NAME = "android.selfDisplayName";
    public static final String EXTRA_SHOW_ACTION_ICON = "android.extraShowActionIcon";
    public static final String EXTRA_SHOW_CHRONOMETER = "android.showChronometer";
    public static final String EXTRA_SHOW_WHEN = "android.showWhen";
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
    public static final int FLAG_HIGH_PRIORITY = 128;
    public static final int FLAG_INSISTENT = 4;
    public static final int FLAG_LOCAL_ONLY = 256;
    public static final int FLAG_NO_CLEAR = 32;
    public static final int FLAG_ONGOING_EVENT = 2;
    public static final int FLAG_ONLY_ALERT_ONCE = 8;
    public static final int FLAG_SHOW_LIGHTS = 1;
    public static final String INTENT_CATEGORY_NOTIFICATION_PREFERENCES = "android.intent.category.NOTIFICATION_PREFERENCES";
    private static final int MAX_CHARSEQUENCE_LENGTH = 5120;
    private static final int MAX_REPLY_HISTORY = 5;
    public static final int PRIORITY_DEFAULT = 0;
    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_LOW = -1;
    public static final int PRIORITY_MAX = 2;
    public static final int PRIORITY_MIN = -2;
    @Deprecated
    public static final int STREAM_DEFAULT = -1;
    private static final String TAG = "Notification";
    public static final int VISIBILITY_PRIVATE = 0;
    public static final int VISIBILITY_PUBLIC = 1;
    public static final int VISIBILITY_SECRET = -1;
    public Action[] actions;
    public ArraySet<PendingIntent> allPendingIntents;
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
    public int ledARGB;
    public int ledOffMS;
    public int ledOnMS;
    private String mGroupKey;
    private Icon mLargeIcon;
    private Icon mSmallIcon;
    private String mSortKey;
    private Object mSyncLock;
    public int number;
    public int priority;
    public Notification publicVersion;
    public Uri sound;
    public CharSequence tickerText;
    @Deprecated
    public RemoteViews tickerView;
    public long[] vibrate;
    public int visibility;
    public long when;

    final /* synthetic */ class -void_writeToParcel_android_os_Parcel_parcel_int_flags_LambdaImpl0 implements OnMarshaledListener {
        private /* synthetic */ Parcel val$parcel;
        private /* synthetic */ Notification val$this;

        public /* synthetic */ -void_writeToParcel_android_os_Parcel_parcel_int_flags_LambdaImpl0(Notification notification, Parcel parcel) {
            this.val$this = notification;
            this.val$parcel = parcel;
        }

        public void onMarshaled(PendingIntent arg0, Parcel arg1, int arg2) {
            this.val$this.-android_app_Notification_lambda$1(this.val$parcel, arg0, arg1, arg2);
        }
    }

    public static class Action implements Parcelable {
        public static final Creator<Action> CREATOR = null;
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
                this(Icon.createWithResource(ProxyInfo.LOCAL_EXCL_LIST, icon), title, intent, new Bundle(), null);
            }

            public Builder(Icon icon, CharSequence title, PendingIntent intent) {
                this(icon, title, intent, new Bundle(), null);
            }

            public Builder(Action action) {
                this(action.getIcon(), action.title, action.actionIntent, new Bundle(action.mExtras), action.getRemoteInputs());
            }

            private Builder(Icon icon, CharSequence title, PendingIntent intent, Bundle extras, RemoteInput[] remoteInputs) {
                this.mIcon = icon;
                this.mTitle = title;
                this.mIntent = intent;
                this.mExtras = extras;
                if (remoteInputs != null) {
                    this.mRemoteInputs = new ArrayList(remoteInputs.length);
                    Collections.addAll(this.mRemoteInputs, remoteInputs);
                }
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
                return new Action(this.mTitle, this.mIntent, this.mExtras, this.mRemoteInputs != null ? (RemoteInput[]) this.mRemoteInputs.toArray(new RemoteInput[this.mRemoteInputs.size()]) : null, this.mAllowGeneratedReplies, null);
            }
        }

        public interface Extender {
            Builder extend(Builder builder);
        }

        public static final class WearableExtender implements Extender {
            private static final int DEFAULT_FLAGS = 1;
            private static final String EXTRA_WEARABLE_EXTENSIONS = "android.wearable.EXTENSIONS";
            private static final int FLAG_AVAILABLE_OFFLINE = 1;
            private static final int FLAG_HINT_LAUNCHES_ACTIVITY = 2;
            private static final String KEY_CANCEL_LABEL = "cancelLabel";
            private static final String KEY_CONFIRM_LABEL = "confirmLabel";
            private static final String KEY_FLAGS = "flags";
            private static final String KEY_IN_PROGRESS_LABEL = "inProgressLabel";
            private CharSequence mCancelLabel;
            private CharSequence mConfirmLabel;
            private int mFlags;
            private CharSequence mInProgressLabel;

            private void setFlag(int r1, boolean r2) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.Notification.Action.WearableExtender.setFlag(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 9 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.app.Notification.Action.WearableExtender.setFlag(int, boolean):void");
            }

            public WearableExtender() {
                this.mFlags = FLAG_AVAILABLE_OFFLINE;
            }

            public WearableExtender(Action action) {
                this.mFlags = FLAG_AVAILABLE_OFFLINE;
                Bundle wearableBundle = action.getExtras().getBundle(EXTRA_WEARABLE_EXTENSIONS);
                if (wearableBundle != null) {
                    this.mFlags = wearableBundle.getInt(KEY_FLAGS, FLAG_AVAILABLE_OFFLINE);
                    this.mInProgressLabel = wearableBundle.getCharSequence(KEY_IN_PROGRESS_LABEL);
                    this.mConfirmLabel = wearableBundle.getCharSequence(KEY_CONFIRM_LABEL);
                    this.mCancelLabel = wearableBundle.getCharSequence(KEY_CANCEL_LABEL);
                }
            }

            public Builder extend(Builder builder) {
                Bundle wearableBundle = new Bundle();
                if (this.mFlags != FLAG_AVAILABLE_OFFLINE) {
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
                setFlag(FLAG_AVAILABLE_OFFLINE, availableOffline);
                return this;
            }

            public boolean isAvailableOffline() {
                return (this.mFlags & FLAG_AVAILABLE_OFFLINE) != 0;
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
                setFlag(FLAG_HINT_LAUNCHES_ACTIVITY, hintLaunchesActivity);
                return this;
            }

            public boolean getHintLaunchesActivity() {
                return (this.mFlags & FLAG_HINT_LAUNCHES_ACTIVITY) != 0;
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.Notification.Action.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.Notification.Action.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.Notification.Action.<clinit>():void");
        }

        private Action(Parcel in) {
            boolean z;
            this.mAllowGeneratedReplies = false;
            if (in.readInt() != 0) {
                this.mIcon = (Icon) Icon.CREATOR.createFromParcel(in);
                if (this.mIcon.getType() == Notification.PRIORITY_MAX) {
                    this.icon = this.mIcon.getResId();
                }
            }
            this.title = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            if (in.readInt() == Notification.VISIBILITY_PUBLIC) {
                this.actionIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(in);
            }
            this.mExtras = Bundle.setDefusable(in.readBundle(), true);
            this.mRemoteInputs = (RemoteInput[]) in.createTypedArray(RemoteInput.CREATOR);
            if (in.readInt() == Notification.VISIBILITY_PUBLIC) {
                z = true;
            } else {
                z = false;
            }
            this.mAllowGeneratedReplies = z;
        }

        @Deprecated
        public Action(int icon, CharSequence title, PendingIntent intent) {
            this(Icon.createWithResource(ProxyInfo.LOCAL_EXCL_LIST, icon), title, intent, new Bundle(), null, false);
        }

        private Action(Icon icon, CharSequence title, PendingIntent intent, Bundle extras, RemoteInput[] remoteInputs, boolean allowGeneratedReplies) {
            this.mAllowGeneratedReplies = false;
            this.mIcon = icon;
            if (icon != null && icon.getType() == Notification.PRIORITY_MAX) {
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

        public Action clone() {
            return new Action(getIcon(), this.title, this.actionIntent, new Bundle(this.mExtras), getRemoteInputs(), getAllowGeneratedReplies());
        }

        public int describeContents() {
            return Notification.VISIBILITY_PRIVATE;
        }

        public void writeToParcel(Parcel out, int flags) {
            int i = Notification.VISIBILITY_PUBLIC;
            Icon ic = getIcon();
            if (ic != null) {
                out.writeInt(Notification.VISIBILITY_PUBLIC);
                ic.writeToParcel(out, Notification.VISIBILITY_PRIVATE);
            } else {
                out.writeInt(Notification.VISIBILITY_PRIVATE);
            }
            TextUtils.writeToParcel(this.title, out, flags);
            if (this.actionIntent != null) {
                out.writeInt(Notification.VISIBILITY_PUBLIC);
                this.actionIntent.writeToParcel(out, flags);
            } else {
                out.writeInt(Notification.VISIBILITY_PRIVATE);
            }
            out.writeBundle(this.mExtras);
            out.writeTypedArray(this.mRemoteInputs, flags);
            if (!this.mAllowGeneratedReplies) {
                i = Notification.VISIBILITY_PRIVATE;
            }
            out.writeInt(i);
        }
    }

    public static abstract class Style {
        private CharSequence mBigContentTitle;
        protected Builder mBuilder;
        protected CharSequence mSummaryText;
        protected boolean mSummaryTextSet;

        public Style() {
            this.mSummaryText = null;
            this.mSummaryTextSet = false;
        }

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
                contentView.setViewVisibility(16909243, Notification.VISIBILITY_PRIVATE);
            } else {
                contentView.setViewVisibility(16909243, Notification.FLAG_ONLY_ALERT_ONCE);
            }
            return contentView;
        }

        public RemoteViews makeContentView() {
            return null;
        }

        public RemoteViews makeBigContentView() {
            return null;
        }

        public RemoteViews makeHeadsUpContentView() {
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
    }

    public static class BigPictureStyle extends Style {
        public static final int MIN_ASHMEM_BITMAP_SIZE = 131072;
        private Icon mBigLargeIcon;
        private boolean mBigLargeIconSet;
        private Bitmap mPicture;

        public BigPictureStyle() {
            this.mBigLargeIconSet = false;
        }

        @Deprecated
        public BigPictureStyle(Builder builder) {
            this.mBigLargeIconSet = false;
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
            if (this.mPicture != null && this.mPicture.isMutable() && this.mPicture.getAllocationByteCount() >= MIN_ASHMEM_BITMAP_SIZE) {
                this.mPicture = this.mPicture.createAshmemBitmap();
            }
            if (this.mBigLargeIcon != null) {
                this.mBigLargeIcon.convertToAshmem();
            }
        }

        public RemoteViews makeBigContentView() {
            Icon icon = null;
            if (this.mBigLargeIconSet) {
                icon = this.mBuilder.mN.mLargeIcon;
                this.mBuilder.mN.mLargeIcon = this.mBigLargeIcon;
            }
            RemoteViews contentView = getStandardView(this.mBuilder.getBigPictureLayoutResource());
            if (this.mSummaryTextSet) {
                contentView.setTextViewText(16908413, this.mBuilder.processLegacyText(this.mSummaryText));
                contentView.setViewVisibility(16908413, Notification.VISIBILITY_PRIVATE);
            }
            this.mBuilder.setContentMinHeight(contentView, this.mBuilder.mN.hasLargeIcon());
            if (this.mBigLargeIconSet) {
                this.mBuilder.mN.mLargeIcon = icon;
            }
            contentView.setImageViewBitmap(16909233, this.mPicture);
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
        private static final int LINES_CONSUMED_BY_ACTIONS = 4;
        private static final int MAX_LINES = 13;
        private CharSequence mBigText;

        public BigTextStyle() {
        }

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
            contentView.setTextViewText(16909234, bigTextText);
            contentView.setViewVisibility(16909234, TextUtils.isEmpty(bigTextText) ? Notification.FLAG_ONLY_ALERT_ONCE : Notification.VISIBILITY_PRIVATE);
            contentView.setInt(16909234, "setMaxLines", calculateMaxLines(builder));
            contentView.setBoolean(16909234, "setHasImage", builder.mN.hasLargeIcon());
        }

        private static int calculateMaxLines(Builder builder) {
            boolean hasActions = false;
            if (builder.mActions.size() > 0) {
                hasActions = true;
            }
            if (hasActions) {
                return 9;
            }
            return MAX_LINES;
        }
    }

    public static class Builder {
        public static final String EXTRA_REBUILD_BIG_CONTENT_VIEW_ACTION_COUNT = "android.rebuild.bigViewActionCount";
        public static final String EXTRA_REBUILD_CONTENT_VIEW_ACTION_COUNT = "android.rebuild.contentViewActionCount";
        public static final String EXTRA_REBUILD_HEADS_UP_CONTENT_VIEW_ACTION_COUNT = "android.rebuild.hudViewActionCount";
        private static final String HW_SMALL_ICON_TINT_FOR_SYSTEMUI = "hw_small_icon_tint";
        private static final int MAX_ACTION_BUTTONS = 3;
        private static final int SMALL_ICON_CAN_BE_TINT = 1;
        private static final int SMALL_ICON_CAN_NOT_BE_TINT = 0;
        private static final boolean STRIP_AND_REBUILD = false;
        private ArrayList<Action> mActions;
        private int mCachedContrastColor;
        private int mCachedContrastColorIsFor;
        private NotificationColorUtil mColorUtil;
        private boolean mColorUtilInited;
        private Context mContext;
        private HwNotificationColorUtil mHWColorUtil;
        private Notification mN;
        private ArrayList<String> mPersonList;
        private Style mStyle;
        private Bundle mUserExtras;

        public android.app.Notification.Builder setFlag(int r1, boolean r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.Notification.Builder.setFlag(int, boolean):android.app.Notification$Builder
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.Notification.Builder.setFlag(int, boolean):android.app.Notification$Builder");
        }

        public Builder(Context context) {
            this(context, null);
        }

        public Builder(Context context, Notification toAdopt) {
            this.mUserExtras = new Bundle();
            this.mActions = new ArrayList(MAX_ACTION_BUTTONS);
            this.mPersonList = new ArrayList();
            this.mColorUtilInited = false;
            this.mCachedContrastColor = SMALL_ICON_CAN_BE_TINT;
            this.mCachedContrastColorIsFor = SMALL_ICON_CAN_BE_TINT;
            this.mContext = context;
            if (toAdopt == null) {
                this.mN = new Notification();
                if (context.getApplicationInfo().targetSdkVersion < 24) {
                    this.mN.extras.putBoolean(Notification.EXTRA_SHOW_WHEN, true);
                }
                this.mN.priority = Notification.VISIBILITY_PRIVATE;
                this.mN.visibility = Notification.VISIBILITY_PRIVATE;
                return;
            }
            this.mN = toAdopt;
            if (this.mN.actions != null) {
                Collections.addAll(this.mActions, this.mN.actions);
            }
            if (this.mN.extras.containsKey(Notification.EXTRA_PEOPLE)) {
                Collections.addAll(this.mPersonList, this.mN.extras.getStringArray(Notification.EXTRA_PEOPLE));
            }
            if (this.mN.getSmallIcon() == null && this.mN.icon != 0) {
                setSmallIcon(this.mN.icon);
            }
            if (this.mN.getLargeIcon() == null && this.mN.largeIcon != null) {
                setLargeIcon(this.mN.largeIcon);
            }
            String templateClass = this.mN.extras.getString(Notification.EXTRA_TEMPLATE);
            if (!TextUtils.isEmpty(templateClass)) {
                Class<? extends Style> styleClass = getNotificationStyleClass(templateClass);
                if (styleClass == null) {
                    Log.d(Notification.TAG, "Unknown style class: " + templateClass);
                    return;
                }
                try {
                    Constructor<? extends Style> ctor = styleClass.getDeclaredConstructor(new Class[Notification.VISIBILITY_PRIVATE]);
                    ctor.setAccessible(true);
                    Style style = (Style) ctor.newInstance(new Object[Notification.VISIBILITY_PRIVATE]);
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
            if (!this.mColorUtilInited) {
                this.mColorUtilInited = true;
                if (this.mContext.getApplicationInfo().targetSdkVersion < 21) {
                    this.mColorUtil = NotificationColorUtil.getInstance(this.mContext);
                }
            }
            return this.mColorUtil;
        }

        private HwNotificationColorUtil getHWColorUtil() {
            if (this.mHWColorUtil == null) {
                this.mHWColorUtil = HwNotificationColorUtil.getInstance(this.mContext);
            }
            return this.mHWColorUtil;
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
            if (icon != null && icon.getType() == Notification.PRIORITY_MAX) {
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

        public Builder setRemoteInputHistory(CharSequence[] text) {
            if (text == null) {
                this.mN.extras.putCharSequenceArray(Notification.EXTRA_REMOTE_INPUT_HISTORY, null);
            } else {
                int N = Math.min(Notification.MAX_REPLY_HISTORY, text.length);
                CharSequence[] safe = new CharSequence[N];
                for (int i = Notification.VISIBILITY_PRIVATE; i < N; i += SMALL_ICON_CAN_BE_TINT) {
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
            setFlag(Notification.FLAG_HIGH_PRIORITY, highPriority);
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

        public Builder setSound(Uri sound) {
            this.mN.sound = sound;
            this.mN.audioAttributes = Notification.AUDIO_ATTRIBUTES_DEFAULT;
            return this;
        }

        @Deprecated
        public Builder setSound(Uri sound, int streamType) {
            this.mN.sound = sound;
            this.mN.audioStreamType = streamType;
            return this;
        }

        public Builder setSound(Uri sound, AudioAttributes audioAttributes) {
            this.mN.sound = sound;
            this.mN.audioAttributes = audioAttributes;
            return this;
        }

        public Builder setVibrate(long[] pattern) {
            this.mN.vibrate = pattern;
            return this;
        }

        public Builder setLights(int argb, int onMs, int offMs) {
            this.mN.ledARGB = argb;
            this.mN.ledOnMS = onMs;
            this.mN.ledOffMS = offMs;
            if (!(onMs == 0 && offMs == 0)) {
                Notification notification = this.mN;
                notification.flags |= SMALL_ICON_CAN_BE_TINT;
            }
            return this;
        }

        public Builder setOngoing(boolean ongoing) {
            setFlag(Notification.PRIORITY_MAX, ongoing);
            return this;
        }

        public Builder setOnlyAlertOnce(boolean onlyAlertOnce) {
            setFlag(Notification.FLAG_ONLY_ALERT_ONCE, onlyAlertOnce);
            return this;
        }

        public Builder setAutoCancel(boolean autoCancel) {
            setFlag(Notification.FLAG_AUTO_CANCEL, autoCancel);
            return this;
        }

        public Builder setLocalOnly(boolean localOnly) {
            setFlag(Notification.FLAG_LOCAL_ONLY, localOnly);
            return this;
        }

        public Builder setDefaults(int defaults) {
            this.mN.defaults = defaults;
            return this;
        }

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
            setFlag(Notification.FLAG_GROUP_SUMMARY, isGroupSummary);
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
            this.mActions.add(action);
            return this;
        }

        public Builder setActions(Action... actions) {
            this.mActions.clear();
            for (int i = Notification.VISIBILITY_PRIVATE; i < actions.length; i += SMALL_ICON_CAN_BE_TINT) {
                this.mActions.add(actions[i]);
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

        public Builder setColor(int argb) {
            this.mN.color = argb;
            sanitizeColor();
            return this;
        }

        private Drawable getProfileBadgeDrawable() {
            if (this.mN.extras.getBoolean(Notification.EXTRA_HW_IS_FROM_CLONED_PROCESS)) {
                return HwThemeManager.getClonedDrawable(this.mContext, null);
            }
            Drawable badge = HwThemeManager.getHwBadgeDrawable(this.mN, this.mContext, null);
            if (badge != null) {
                return badge;
            }
            if (this.mContext.getUserId() == 0) {
                return null;
            }
            return this.mContext.getPackageManager().getUserBadgeForDensityNoBackground(new UserHandle(this.mContext.getUserId()), Notification.VISIBILITY_PRIVATE);
        }

        private Bitmap getProfileBadge() {
            Drawable badge = getProfileBadgeDrawable();
            if (badge == null) {
                return null;
            }
            int size = this.mContext.getResources().getDimensionPixelSize(17105010);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            badge.setBounds(Notification.VISIBILITY_PRIVATE, Notification.VISIBILITY_PRIVATE, size, size);
            badge.draw(canvas);
            return bitmap;
        }

        private void bindProfileBadge(RemoteViews contentView) {
            Bitmap profileBadge = getProfileBadge();
            if (profileBadge != null) {
                contentView.setImageViewBitmap(16909229, profileBadge);
                contentView.setViewVisibility(16909229, Notification.VISIBILITY_PRIVATE);
            }
        }

        private void resetStandardTemplate(RemoteViews contentView) {
            resetNotificationHeader(contentView);
            resetContentMargins(contentView);
            contentView.setViewVisibility(16908356, Notification.FLAG_ONLY_ALERT_ONCE);
            contentView.setViewVisibility(R.id.title, Notification.FLAG_ONLY_ALERT_ONCE);
            contentView.setTextViewText(R.id.title, null);
            contentView.setViewVisibility(16908413, Notification.FLAG_ONLY_ALERT_ONCE);
            contentView.setTextViewText(16908413, null);
            contentView.setViewVisibility(16909244, Notification.FLAG_ONLY_ALERT_ONCE);
            contentView.setTextViewText(16909244, null);
            contentView.setViewVisibility(R.id.progress, Notification.FLAG_ONLY_ALERT_ONCE);
        }

        private void resetNotificationHeader(RemoteViews contentView) {
            contentView.setImageViewResource(R.id.icon, Notification.VISIBILITY_PRIVATE);
            contentView.setBoolean(16909220, "setExpanded", false);
            contentView.setTextViewText(16909221, null);
            contentView.setViewVisibility(16909227, Notification.FLAG_ONLY_ALERT_ONCE);
            contentView.setViewVisibility(16909428, Notification.FLAG_ONLY_ALERT_ONCE);
            contentView.setTextViewText(16909428, null);
            contentView.setViewVisibility(16909427, Notification.FLAG_ONLY_ALERT_ONCE);
            contentView.setViewVisibility(16909226, Notification.FLAG_ONLY_ALERT_ONCE);
            contentView.setViewVisibility(16908436, Notification.FLAG_ONLY_ALERT_ONCE);
            contentView.setImageViewIcon(16909229, null);
            contentView.setViewVisibility(16909229, Notification.FLAG_ONLY_ALERT_ONCE);
        }

        private void resetContentMargins(RemoteViews contentView) {
            contentView.setViewLayoutMarginEndDimen(16909243, Notification.VISIBILITY_PRIVATE);
            contentView.setViewLayoutMarginEndDimen(16908413, Notification.VISIBILITY_PRIVATE);
        }

        private RemoteViews applyStandardTemplate(int resId) {
            return applyStandardTemplate(resId, true);
        }

        private RemoteViews applyStandardTemplate(int resId, boolean hasProgress) {
            Bundle ex = this.mN.extras;
            return applyStandardTemplate(resId, hasProgress, processLegacyText(ex.getCharSequence(Notification.EXTRA_TITLE)), processLegacyText(ex.getCharSequence(Notification.EXTRA_TEXT)));
        }

        private RemoteViews applyStandardTemplate(int resId, boolean hasProgress, CharSequence title, CharSequence text) {
            RemoteViews contentView = new BuilderRemoteViews(this.mContext.getApplicationInfo(), resId);
            resetStandardTemplate(contentView);
            Bundle ex = this.mN.extras;
            bindNotificationHeader(contentView);
            bindLargeIcon(contentView);
            boolean showProgress = handleProgressBar(hasProgress, contentView, ex);
            if (title != null) {
                int i;
                contentView.setViewVisibility(R.id.title, Notification.VISIBILITY_PRIVATE);
                contentView.setTextViewText(R.id.title, title);
                if (showProgress) {
                    i = Notification.PRIORITY_MIN;
                } else {
                    i = Notification.VISIBILITY_SECRET;
                }
                contentView.setViewLayoutWidth(R.id.title, i);
            }
            if (text != null) {
                int textId;
                if (showProgress) {
                    textId = 16909244;
                } else {
                    textId = 16908413;
                }
                contentView.setTextViewText(textId, text);
                contentView.setViewVisibility(textId, Notification.VISIBILITY_PRIVATE);
            }
            setContentMinHeight(contentView, !showProgress ? this.mN.hasLargeIcon() : true);
            return contentView;
        }

        void setContentMinHeight(RemoteViews remoteView, boolean hasMinHeight) {
            int minHeight = Notification.VISIBILITY_PRIVATE;
            if (hasMinHeight) {
                minHeight = this.mContext.getResources().getDimensionPixelSize(17104966);
            }
            remoteView.setInt(16909231, "setMinimumHeight", minHeight);
        }

        private boolean handleProgressBar(boolean hasProgress, RemoteViews contentView, Bundle ex) {
            int max = ex.getInt(Notification.EXTRA_PROGRESS_MAX, Notification.VISIBILITY_PRIVATE);
            int progress = ex.getInt(Notification.EXTRA_PROGRESS, Notification.VISIBILITY_PRIVATE);
            boolean ind = ex.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE);
            if (!hasProgress || (max == 0 && !ind)) {
                contentView.setViewVisibility(R.id.progress, Notification.FLAG_ONLY_ALERT_ONCE);
                return false;
            }
            contentView.setViewVisibility(R.id.progress, Notification.VISIBILITY_PRIVATE);
            contentView.setProgressBar(R.id.progress, max, progress, ind);
            contentView.setProgressBackgroundTintList(R.id.progress, ColorStateList.valueOf(this.mContext.getColor(17170513)));
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
                contentView.setViewVisibility(16908356, Notification.VISIBILITY_PRIVATE);
                contentView.setImageViewIcon(16908356, this.mN.mLargeIcon);
                processLargeLegacyIcon(this.mN.mLargeIcon, contentView);
                contentView.setViewLayoutMarginEndDimen(16909243, 17104961);
                contentView.setViewLayoutMarginEndDimen(16908413, 17104961);
                contentView.setViewLayoutMarginEndDimen(R.id.progress, 17104961);
            }
        }

        private void bindNotificationHeader(RemoteViews contentView) {
            bindSmallIcon(contentView);
            bindHeaderAppName(contentView);
            bindHeaderText(contentView);
            bindHeaderChronometerAndTime(contentView);
            bindExpandButton(contentView);
            bindProfileBadge(contentView);
        }

        private void bindExpandButton(RemoteViews contentView) {
            contentView.setDrawableParameters(16909228, false, Notification.VISIBILITY_SECRET, resolveContrastColor(), Mode.SRC_ATOP, Notification.VISIBILITY_SECRET);
            contentView.setInt(16909220, "setOriginalNotificationColor", resolveContrastColor());
        }

        private void bindHeaderChronometerAndTime(RemoteViews contentView) {
            if (showsTimeOrChronometer()) {
                contentView.setViewVisibility(16909226, Notification.VISIBILITY_PRIVATE);
                if (this.mN.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER)) {
                    contentView.setViewVisibility(16909227, Notification.VISIBILITY_PRIVATE);
                    contentView.setLong(16909227, "setBase", this.mN.when + (SystemClock.elapsedRealtime() - System.currentTimeMillis()));
                    contentView.setBoolean(16909227, "setStarted", true);
                    contentView.setChronometerCountDown(16909227, this.mN.extras.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN));
                    return;
                }
                contentView.setViewVisibility(16908436, Notification.VISIBILITY_PRIVATE);
                contentView.setLong(16908436, "setTime", this.mN.when);
                return;
            }
            contentView.setLong(16908436, "setTime", this.mN.when != 0 ? this.mN.when : this.mN.creationTime);
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
                contentView.setTextViewText(16909428, processLegacyText(headerText));
                contentView.setViewVisibility(16909428, Notification.VISIBILITY_PRIVATE);
                contentView.setViewVisibility(16909427, Notification.VISIBILITY_PRIVATE);
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

        private void bindHeaderAppName(RemoteViews contentView) {
            contentView.setTextViewText(16909221, loadHeaderAppName());
            contentView.setTextColor(16909221, -1291845632);
        }

        private void bindSmallIcon(RemoteViews contentView) {
            if (this.mN.mSmallIcon == null && this.mN.icon != 0) {
                this.mN.mSmallIcon = Icon.createWithResource(this.mContext, this.mN.icon);
            }
            contentView.setImageViewIcon(R.id.icon, this.mN.mSmallIcon);
            processSmallIconColor(this.mN.mSmallIcon, contentView);
        }

        private boolean showsTimeOrChronometer() {
            return !this.mN.showsTime() ? this.mN.showsChronometer() : true;
        }

        private void resetStandardTemplateWithActions(RemoteViews big) {
            big.setViewVisibility(16909210, Notification.FLAG_ONLY_ALERT_ONCE);
            big.removeAllViews(16909210);
            big.setViewVisibility(16909215, Notification.FLAG_ONLY_ALERT_ONCE);
            big.setTextViewText(16909219, null);
            big.setViewVisibility(16909218, Notification.FLAG_ONLY_ALERT_ONCE);
            big.setTextViewText(16909218, null);
            big.setViewVisibility(16909217, Notification.FLAG_ONLY_ALERT_ONCE);
            big.setTextViewText(16909217, null);
            big.setViewLayoutMarginBottomDimen(16909429, Notification.VISIBILITY_PRIVATE);
        }

        private RemoteViews applyStandardTemplateWithActions(int layoutId) {
            Bundle ex = this.mN.extras;
            return applyStandardTemplateWithActions(layoutId, true, processLegacyText(ex.getCharSequence(Notification.EXTRA_TITLE)), processLegacyText(ex.getCharSequence(Notification.EXTRA_TEXT)));
        }

        private RemoteViews applyStandardTemplateWithActions(int layoutId, boolean hasProgress, CharSequence title, CharSequence text) {
            RemoteViews big = applyStandardTemplate(layoutId, hasProgress, title, text);
            resetStandardTemplateWithActions(big);
            boolean validRemoteInput = false;
            int N = this.mActions.size();
            if (N > 0) {
                big.setViewVisibility(16909214, Notification.VISIBILITY_PRIVATE);
                big.setViewVisibility(16909210, Notification.VISIBILITY_PRIVATE);
                big.setViewLayoutMarginBottomDimen(16909429, 17105218);
                if (N > MAX_ACTION_BUTTONS) {
                    N = MAX_ACTION_BUTTONS;
                }
                for (int i = Notification.VISIBILITY_PRIVATE; i < N; i += SMALL_ICON_CAN_BE_TINT) {
                    Action action = (Action) this.mActions.get(i);
                    validRemoteInput |= hasValidRemoteInput(action);
                    big.addView(16909210, generateActionButton(action));
                }
            } else {
                big.setViewVisibility(16909214, Notification.FLAG_ONLY_ALERT_ONCE);
            }
            CharSequence[] replyText = this.mN.extras.getCharSequenceArray(Notification.EXTRA_REMOTE_INPUT_HISTORY);
            if (validRemoteInput && replyText != null && replyText.length > 0 && !TextUtils.isEmpty(replyText[Notification.VISIBILITY_PRIVATE])) {
                big.setViewVisibility(16909215, Notification.VISIBILITY_PRIVATE);
                big.setTextViewText(16909219, replyText[Notification.VISIBILITY_PRIVATE]);
                if (replyText.length > SMALL_ICON_CAN_BE_TINT && !TextUtils.isEmpty(replyText[SMALL_ICON_CAN_BE_TINT])) {
                    big.setViewVisibility(16909218, Notification.VISIBILITY_PRIVATE);
                    big.setTextViewText(16909218, replyText[SMALL_ICON_CAN_BE_TINT]);
                    if (replyText.length > Notification.PRIORITY_MAX && !TextUtils.isEmpty(replyText[Notification.PRIORITY_MAX])) {
                        big.setViewVisibility(16909217, Notification.VISIBILITY_PRIVATE);
                        big.setTextViewText(16909217, replyText[Notification.PRIORITY_MAX]);
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
            int length = remoteInputs.length;
            for (int i = Notification.VISIBILITY_PRIVATE; i < length; i += SMALL_ICON_CAN_BE_TINT) {
                RemoteInput r = remoteInputs[i];
                CharSequence[] choices = r.getChoices();
                if (r.getAllowFreeFormInput() || (choices != null && choices.length != 0)) {
                    return true;
                }
            }
            return false;
        }

        public RemoteViews createContentView() {
            if (this.mN.contentView != null && (this.mStyle == null || !this.mStyle.displayCustomViewInline())) {
                return this.mN.contentView;
            }
            if (this.mStyle != null) {
                RemoteViews styleView = this.mStyle.makeContentView();
                if (styleView != null) {
                    return styleView;
                }
            }
            return applyStandardTemplate(getBaseLayoutResource());
        }

        public RemoteViews createBigContentView() {
            RemoteViews result = null;
            if (this.mN.bigContentView != null && (this.mStyle == null || !this.mStyle.displayCustomViewInline())) {
                return this.mN.bigContentView;
            }
            if (this.mStyle != null) {
                result = this.mStyle.makeBigContentView();
                hideLine1Text(result);
            } else if (this.mActions.size() != 0) {
                result = applyStandardTemplateWithActions(getBigBaseLayoutResource());
            }
            adaptNotificationHeaderForBigContentView(result);
            return result;
        }

        public RemoteViews makeNotificationHeader() {
            RemoteViews header = new BuilderRemoteViews(this.mContext.getApplicationInfo(), 17367179);
            resetNotificationHeader(header);
            bindNotificationHeader(header);
            return header;
        }

        private void hideLine1Text(RemoteViews result) {
            if (result != null) {
                result.setViewVisibility(16909244, Notification.FLAG_ONLY_ALERT_ONCE);
            }
        }

        private void adaptNotificationHeaderForBigContentView(RemoteViews result) {
            if (result != null) {
                result.setBoolean(16909220, "setExpanded", true);
            }
        }

        public RemoteViews createHeadsUpContentView() {
            if (this.mN.headsUpContentView != null && (this.mStyle == null || !this.mStyle.displayCustomViewInline())) {
                return this.mN.headsUpContentView;
            }
            if (this.mStyle != null) {
                RemoteViews styleView = this.mStyle.makeHeadsUpContentView();
                if (styleView != null) {
                    return styleView;
                }
            } else if (this.mActions.size() == 0) {
                return null;
            }
            return applyStandardTemplateWithActions(getBigBaseLayoutResource());
        }

        public RemoteViews makePublicContentView() {
            if (this.mN.publicVersion != null) {
                return recoverBuilder(this.mContext, this.mN.publicVersion).createContentView();
            }
            Bundle savedBundle = this.mN.extras;
            Style style = this.mStyle;
            this.mStyle = null;
            Icon largeIcon = this.mN.mLargeIcon;
            this.mN.mLargeIcon = null;
            Bitmap largeIconLegacy = this.mN.largeIcon;
            this.mN.largeIcon = null;
            Bundle publicExtras = new Bundle();
            publicExtras.putBoolean(Notification.EXTRA_SHOW_WHEN, savedBundle.getBoolean(Notification.EXTRA_SHOW_WHEN));
            publicExtras.putBoolean(Notification.EXTRA_SHOW_CHRONOMETER, savedBundle.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER));
            publicExtras.putBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN, savedBundle.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN));
            publicExtras.putCharSequence(Notification.EXTRA_TITLE, this.mContext.getString(17039678));
            this.mN.extras = publicExtras;
            RemoteViews publicView = applyStandardTemplate(getBaseLayoutResource());
            savedBundle.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, this.mN.extras.getInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI));
            this.mN.extras = savedBundle;
            this.mN.mLargeIcon = largeIcon;
            this.mN.largeIcon = largeIconLegacy;
            this.mStyle = style;
            return publicView;
        }

        private RemoteViews generateActionButton(Action action) {
            int actionTombstoneLayoutResource;
            boolean tombstone = action.actionIntent == null;
            ApplicationInfo applicationInfo = this.mContext.getApplicationInfo();
            if (tombstone) {
                actionTombstoneLayoutResource = getActionTombstoneLayoutResource();
            } else {
                actionTombstoneLayoutResource = getActionLayoutResource();
            }
            RemoteViews button = new BuilderRemoteViews(applicationInfo, actionTombstoneLayoutResource);
            Icon ai = action.getIcon();
            if (this.mN.extras.getBoolean(Notification.EXTRA_SHOW_ACTION_ICON)) {
                int actionIconLength = this.mContext.getResources().getDimensionPixelSize(34472167);
                button.setTextViewCompoundDrawablesWithBounds(16909211, ai, null, null, null, actionIconLength, actionIconLength, this.mContext.getResources().getDimensionPixelSize(34472168));
            }
            button.setTextViewText(16909211, processLegacyText(action.title));
            if (!tombstone) {
                button.setOnClickPendingIntent(16909211, action.actionIntent);
            }
            button.setContentDescription(16909211, action.title);
            if (action.mRemoteInputs != null) {
                button.setRemoteInputs(16909211, action.mRemoteInputs);
            }
            if (this.mN.color != 0) {
                button.setTextColor(16909211, resolveContrastColor());
            }
            return button;
        }

        private boolean isLegacy() {
            return getColorUtil() != null;
        }

        private CharSequence processLegacyText(CharSequence charSequence) {
            if (isLegacy()) {
                return getColorUtil().invertCharSequenceColors(charSequence);
            }
            return charSequence;
        }

        private void processSmallIconColor(Icon smallIcon, RemoteViews contentView) {
            int i = Notification.VISIBILITY_SECRET;
            boolean colorable = isPureColorIcon(smallIcon);
            if (colorable) {
                if (this.mN.color == 0) {
                    contentView.setDrawableParameters(R.id.icon, false, Notification.VISIBILITY_SECRET, -9079435, Mode.SRC_ATOP, Notification.VISIBILITY_SECRET);
                    contentView.setInt(16909220, "setOriginalIconColor", Notification.VISIBILITY_SECRET);
                    return;
                }
                contentView.setDrawableParameters(R.id.icon, false, Notification.VISIBILITY_SECRET, resolveContrastColor(), Mode.SRC_ATOP, Notification.VISIBILITY_SECRET);
            }
            String str = "setOriginalIconColor";
            if (colorable) {
                i = resolveContrastColor();
            }
            contentView.setInt(16909220, str, i);
        }

        private boolean isPureColorIcon(Icon icon) {
            switch (getHWColorUtil().getSmallIconColorType(this.mContext, icon)) {
                case Notification.VISIBILITY_PRIVATE /*0*/:
                case SMALL_ICON_CAN_BE_TINT /*1*/:
                    this.mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, Notification.VISIBILITY_PRIVATE);
                    return false;
                case Notification.PRIORITY_MAX /*2*/:
                    this.mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, SMALL_ICON_CAN_BE_TINT);
                    return true;
                case Notification.FLAG_INSISTENT /*4*/:
                    this.mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, SMALL_ICON_CAN_BE_TINT);
                    return false;
                default:
                    this.mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, Notification.VISIBILITY_PRIVATE);
                    return false;
            }
        }

        private void processLargeLegacyIcon(Icon largeIcon, RemoteViews contentView) {
            if (largeIcon != null && isLegacy() && getColorUtil().isGrayscaleIcon(this.mContext, largeIcon)) {
                contentView.setDrawableParameters(16908356, false, Notification.VISIBILITY_SECRET, resolveContrastColor(), Mode.SRC_ATOP, Notification.VISIBILITY_SECRET);
            }
        }

        private void sanitizeColor() {
            if (this.mN.color != 0) {
                Notification notification = this.mN;
                notification.color |= Color.BLACK;
            }
        }

        int resolveContrastColor() {
            if (this.mCachedContrastColorIsFor == this.mN.color && this.mCachedContrastColor != SMALL_ICON_CAN_BE_TINT) {
                return this.mCachedContrastColor;
            }
            int contrasted = NotificationColorUtil.resolveContrastColor(this.mContext, this.mN.color);
            this.mCachedContrastColorIsFor = this.mN.color;
            this.mCachedContrastColor = contrasted;
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
            if (this.mN.bigContentView == null && this.mN.contentView == null) {
                if (this.mN.headsUpContentView != null) {
                }
                return this.mN;
            }
            this.mN.extras.putBoolean(Notification.EXTRA_CONTAINS_CUSTOM_VIEW, true);
            return this.mN;
        }

        public static Builder recoverBuilder(Context context, Notification n) {
            Context builderContext;
            ApplicationInfo applicationInfo = (ApplicationInfo) n.extras.getParcelable(Notification.EXTRA_BUILDER_APPLICATION_INFO);
            if (applicationInfo != null) {
                try {
                    builderContext = context.createApplicationContext(applicationInfo, Notification.FLAG_INSISTENT);
                } catch (NameNotFoundException e) {
                    Log.e(Notification.TAG, "ApplicationInfo " + applicationInfo + " not found");
                    builderContext = context;
                }
            } else {
                builderContext = context;
            }
            return new Builder(builderContext, n);
        }

        private static Class<? extends Style> getNotificationStyleClass(String templateClass) {
            int i = Notification.VISIBILITY_PRIVATE;
            Class<? extends Style>[] classes = new Class[]{BigTextStyle.class, BigPictureStyle.class, InboxStyle.class, MediaStyle.class, DecoratedCustomViewStyle.class, DecoratedMediaCustomViewStyle.class, MessagingStyle.class};
            int length = classes.length;
            while (i < length) {
                Class<? extends Style> innerClass = classes[i];
                if (templateClass.equals(innerClass.getName())) {
                    return innerClass;
                }
                i += SMALL_ICON_CAN_BE_TINT;
            }
            return null;
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
                this.mStyle.buildStyled(this.mN);
            }
            if (this.mContext.getApplicationInfo().targetSdkVersion < 24 && (this.mStyle == null || !this.mStyle.displayCustomViewInline())) {
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
            if ((this.mN.defaults & Notification.FLAG_INSISTENT) != 0) {
                Notification notification = this.mN;
                notification.flags |= SMALL_ICON_CAN_BE_TINT;
            }
            return this.mN;
        }

        public Notification buildInto(Notification n) {
            build().cloneInto(n, true);
            return n;
        }

        public static Notification maybeCloneStrippedForDelivery(Notification n) {
            String templateClass = n.extras.getString(Notification.EXTRA_TEMPLATE);
            if (!TextUtils.isEmpty(templateClass) && getNotificationStyleClass(templateClass) == null) {
                return n;
            }
            boolean stripContentView = n.contentView instanceof BuilderRemoteViews ? n.extras.getInt(EXTRA_REBUILD_CONTENT_VIEW_ACTION_COUNT, Notification.VISIBILITY_SECRET) == n.contentView.getSequenceNumber() : false;
            boolean stripBigContentView = n.bigContentView instanceof BuilderRemoteViews ? n.extras.getInt(EXTRA_REBUILD_BIG_CONTENT_VIEW_ACTION_COUNT, Notification.VISIBILITY_SECRET) == n.bigContentView.getSequenceNumber() : false;
            boolean stripHeadsUpContentView = n.headsUpContentView instanceof BuilderRemoteViews ? n.extras.getInt(EXTRA_REBUILD_HEADS_UP_CONTENT_VIEW_ACTION_COUNT, Notification.VISIBILITY_SECRET) == n.headsUpContentView.getSequenceNumber() : false;
            if (!stripContentView && !stripBigContentView && !stripHeadsUpContentView) {
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
            return 17367180;
        }

        private int getBigBaseLayoutResource() {
            return 17367181;
        }

        private int getBigPictureLayoutResource() {
            return 17367183;
        }

        private int getBigTextLayoutResource() {
            return 17367184;
        }

        private int getInboxLayoutResource() {
            return 17367185;
        }

        private int getMessagingLayoutResource() {
            return 17367315;
        }

        private int getActionLayoutResource() {
            return 17367174;
        }

        private int getActionTombstoneLayoutResource() {
            return 17367176;
        }
    }

    private static class BuilderRemoteViews extends RemoteViews {
        public BuilderRemoteViews(Parcel parcel) {
            super(parcel);
        }

        public BuilderRemoteViews(ApplicationInfo appInfo, int layoutId) {
            super(appInfo, layoutId);
        }

        public /* bridge */ /* synthetic */ RemoteViews m1clone() {
            return clone();
        }

        public BuilderRemoteViews clone() {
            Parcel p = Parcel.obtain();
            writeToParcel(p, Notification.VISIBILITY_PRIVATE);
            p.setDataPosition(Notification.VISIBILITY_PRIVATE);
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
        private int mColor;
        private Bitmap mLargeIcon;
        private UnreadConversation mUnreadConversation;

        public static class Builder {
            private long mLatestTimestamp;
            private final List<String> mMessages;
            private final String mParticipant;
            private PendingIntent mReadPendingIntent;
            private RemoteInput mRemoteInput;
            private PendingIntent mReplyPendingIntent;

            public Builder(String name) {
                this.mMessages = new ArrayList();
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
                String[] messages = (String[]) this.mMessages.toArray(new String[this.mMessages.size()]);
                String[] participants = new String[Notification.VISIBILITY_PUBLIC];
                participants[Notification.VISIBILITY_PRIVATE] = this.mParticipant;
                return new UnreadConversation(messages, this.mRemoteInput, this.mReplyPendingIntent, this.mReadPendingIntent, participants, this.mLatestTimestamp);
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
                return this.mParticipants.length > 0 ? this.mParticipants[Notification.VISIBILITY_PRIVATE] : null;
            }

            public long getLatestTimestamp() {
                return this.mLatestTimestamp;
            }

            Bundle getBundleForUnreadConversation() {
                Bundle b = new Bundle();
                String author = null;
                if (this.mParticipants != null && this.mParticipants.length > Notification.VISIBILITY_PUBLIC) {
                    author = this.mParticipants[Notification.VISIBILITY_PRIVATE];
                }
                Parcelable[] messages = new Parcelable[this.mMessages.length];
                for (int i = Notification.VISIBILITY_PRIVATE; i < messages.length; i += Notification.VISIBILITY_PUBLIC) {
                    Bundle m = new Bundle();
                    m.putString(KEY_TEXT, this.mMessages[i]);
                    m.putString(KEY_AUTHOR, author);
                    messages[i] = m;
                }
                b.putParcelableArray(KEY_MESSAGES, messages);
                if (this.mRemoteInput != null) {
                    b.putParcelable(KEY_REMOTE_INPUT, this.mRemoteInput);
                }
                b.putParcelable(KEY_ON_REPLY, this.mReplyPendingIntent);
                b.putParcelable(KEY_ON_READ, this.mReadPendingIntent);
                b.putStringArray(KEY_PARTICIPANTS, this.mParticipants);
                b.putLong(KEY_TIMESTAMP, this.mLatestTimestamp);
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
                    for (int i = Notification.VISIBILITY_PRIVATE; i < tmp.length; i += Notification.VISIBILITY_PUBLIC) {
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
                if (participants == null || participants.length != Notification.VISIBILITY_PUBLIC) {
                    return null;
                }
                return new UnreadConversation(messages, remoteInput, onReply, onRead, participants, b.getLong(KEY_TIMESTAMP));
            }
        }

        public CarExtender() {
            this.mColor = Notification.VISIBILITY_PRIVATE;
        }

        public CarExtender(Notification notif) {
            Bundle carBundle = null;
            this.mColor = Notification.VISIBILITY_PRIVATE;
            if (notif.extras != null) {
                carBundle = notif.extras.getBundle(EXTRA_CAR_EXTENDER);
            }
            if (carBundle != null) {
                this.mLargeIcon = (Bitmap) carBundle.getParcelable(EXTRA_LARGE_ICON);
                this.mColor = carBundle.getInt(EXTRA_COLOR, Notification.VISIBILITY_PRIVATE);
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
        public DecoratedCustomViewStyle() {
        }

        public boolean displayCustomViewInline() {
            return true;
        }

        public RemoteViews makeContentView() {
            return makeStandardTemplateWithCustomContent(this.mBuilder.mN.contentView);
        }

        public RemoteViews makeBigContentView() {
            return makeDecoratedBigContentView();
        }

        public RemoteViews makeHeadsUpContentView() {
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
            return remoteViews;
        }

        private RemoteViews makeStandardTemplateWithCustomContent(RemoteViews customContent) {
            RemoteViews remoteViews = this.mBuilder.applyStandardTemplate(this.mBuilder.getBaseLayoutResource());
            buildIntoRemoteViewContent(remoteViews, customContent);
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
                remoteViews.removeAllViews(16909231);
                remoteViews.addView(16909231, customContent);
            }
            int endMargin = 17104960;
            if (this.mBuilder.mN.hasLargeIcon()) {
                endMargin = 17105217;
            }
            remoteViews.setViewLayoutMarginEndDimen(16909231, endMargin);
        }
    }

    public static class MediaStyle extends Style {
        static final int MAX_MEDIA_BUTTONS = 5;
        static final int MAX_MEDIA_BUTTONS_IN_COMPACT = 3;
        private int[] mActionsToShowInCompact;
        private Token mToken;

        public MediaStyle() {
            this.mActionsToShowInCompact = null;
        }

        @Deprecated
        public MediaStyle(Builder builder) {
            this.mActionsToShowInCompact = null;
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

        public RemoteViews makeContentView() {
            return makeMediaContentView();
        }

        public RemoteViews makeBigContentView() {
            return makeMediaBigContentView();
        }

        public RemoteViews makeHeadsUpContentView() {
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
            RemoteViews button = new BuilderRemoteViews(this.mBuilder.mContext.getApplicationInfo(), 17367177);
            button.setImageViewIcon(16909211, action.getIcon());
            button.setDrawableParameters(16909211, false, Notification.VISIBILITY_SECRET, color, Mode.SRC_ATOP, Notification.VISIBILITY_SECRET);
            if (!tombstone) {
                button.setOnClickPendingIntent(16909211, action.actionIntent);
            }
            button.setContentDescription(16909211, action.title);
            return button;
        }

        private RemoteViews makeMediaContentView() {
            int N;
            RemoteViews view = this.mBuilder.applyStandardTemplate(17367186, false);
            int numActions = this.mBuilder.mActions.size();
            if (this.mActionsToShowInCompact == null) {
                N = Notification.VISIBILITY_PRIVATE;
            } else {
                N = Math.min(this.mActionsToShowInCompact.length, MAX_MEDIA_BUTTONS_IN_COMPACT);
            }
            if (N > 0) {
                view.removeAllViews(16909232);
                for (int i = Notification.VISIBILITY_PRIVATE; i < N; i += Notification.VISIBILITY_PUBLIC) {
                    if (i >= numActions) {
                        Object[] objArr = new Object[Notification.PRIORITY_MAX];
                        objArr[Notification.VISIBILITY_PRIVATE] = Integer.valueOf(i);
                        objArr[Notification.VISIBILITY_PUBLIC] = Integer.valueOf(numActions + Notification.VISIBILITY_SECRET);
                        throw new IllegalArgumentException(String.format("setShowActionsInCompactView: action %d out of bounds (max %d)", objArr));
                    }
                    view.addView(16909232, generateMediaActionButton((Action) this.mBuilder.mActions.get(this.mActionsToShowInCompact[i]), this.mBuilder.resolveContrastColor()));
                }
            }
            handleImage(view);
            int endMargin = 17104960;
            if (this.mBuilder.mN.hasLargeIcon()) {
                endMargin = 17105217;
            }
            view.setViewLayoutMarginEndDimen(16909231, endMargin);
            return view;
        }

        private RemoteViews makeMediaBigContentView() {
            int actionCount = Math.min(this.mBuilder.mActions.size(), MAX_MEDIA_BUTTONS);
            int actionsInCompact;
            if (this.mActionsToShowInCompact == null) {
                actionsInCompact = Notification.VISIBILITY_PRIVATE;
            } else {
                actionsInCompact = Math.min(this.mActionsToShowInCompact.length, MAX_MEDIA_BUTTONS_IN_COMPACT);
            }
            if (!this.mBuilder.mN.hasLargeIcon() && actionCount <= actionsInCompact) {
                return null;
            }
            RemoteViews big = this.mBuilder.applyStandardTemplate(17367182, false);
            if (actionCount > 0) {
                big.removeAllViews(16909232);
                for (int i = Notification.VISIBILITY_PRIVATE; i < actionCount; i += Notification.VISIBILITY_PUBLIC) {
                    big.addView(16909232, generateMediaActionButton((Action) this.mBuilder.mActions.get(i), this.mBuilder.resolveContrastColor()));
                }
            }
            handleImage(big);
            return big;
        }

        private void handleImage(RemoteViews contentView) {
            if (this.mBuilder.mN.hasLargeIcon()) {
                contentView.setViewLayoutMarginEndDimen(16909243, Notification.VISIBILITY_PRIVATE);
                contentView.setViewLayoutMarginEndDimen(16908413, Notification.VISIBILITY_PRIVATE);
            }
        }

        protected boolean hasProgress() {
            return false;
        }
    }

    public static class DecoratedMediaCustomViewStyle extends MediaStyle {
        public DecoratedMediaCustomViewStyle() {
        }

        public boolean displayCustomViewInline() {
            return true;
        }

        public RemoteViews makeContentView() {
            return buildIntoRemoteView(super.makeContentView(), 16909242, this.mBuilder.mN.contentView);
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
                return buildIntoRemoteView(remoteViews, 16909231, customRemoteView);
            }
            if (customRemoteView != this.mBuilder.mN.contentView) {
                return buildIntoRemoteView(super.makeContentView(), 16909242, customRemoteView);
            }
            return null;
        }

        public RemoteViews makeHeadsUpContentView() {
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
        private ArrayList<CharSequence> mTexts;

        public InboxStyle() {
            this.mTexts = new ArrayList(Notification.MAX_REPLY_HISTORY);
        }

        @Deprecated
        public InboxStyle(Builder builder) {
            this.mTexts = new ArrayList(Notification.MAX_REPLY_HISTORY);
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
            int[] rowIds = new int[]{16909235, 16909236, 16909237, 16909238, 16909239, 16909240, 16909241};
            int length = rowIds.length;
            for (int i = Notification.VISIBILITY_PRIVATE; i < length; i += Notification.VISIBILITY_PUBLIC) {
                contentView.setViewVisibility(rowIds[i], Notification.FLAG_ONLY_ALERT_ONCE);
            }
            int i2 = Notification.VISIBILITY_PRIVATE;
            int topPadding = this.mBuilder.mContext.getResources().getDimensionPixelSize(17105223);
            boolean first = true;
            int onlyViewId = Notification.VISIBILITY_PRIVATE;
            int maxRows = rowIds.length;
            if (this.mBuilder.mActions.size() > 0) {
                maxRows += Notification.VISIBILITY_SECRET;
            }
            while (i2 < this.mTexts.size() && i2 < maxRows) {
                CharSequence str = (CharSequence) this.mTexts.get(i2);
                if (!TextUtils.isEmpty(str)) {
                    contentView.setViewVisibility(rowIds[i2], Notification.VISIBILITY_PRIVATE);
                    contentView.setTextViewText(rowIds[i2], this.mBuilder.processLegacyText(str));
                    contentView.setViewPadding(rowIds[i2], Notification.VISIBILITY_PRIVATE, topPadding, Notification.VISIBILITY_PRIVATE, Notification.VISIBILITY_PRIVATE);
                    handleInboxImageMargin(contentView, rowIds[i2], first);
                    if (first) {
                        onlyViewId = rowIds[i2];
                    } else {
                        onlyViewId = Notification.VISIBILITY_PRIVATE;
                    }
                    first = false;
                }
                i2 += Notification.VISIBILITY_PUBLIC;
            }
            if (onlyViewId != 0) {
                contentView.setViewPadding(onlyViewId, Notification.VISIBILITY_PRIVATE, this.mBuilder.mContext.getResources().getDimensionPixelSize(17105222), Notification.VISIBILITY_PRIVATE, Notification.VISIBILITY_PRIVATE);
            }
            return contentView;
        }

        private void handleInboxImageMargin(RemoteViews contentView, int id, boolean first) {
            int endMargin = Notification.VISIBILITY_PRIVATE;
            if (first) {
                boolean z = this.mBuilder.mN.extras.getInt(Notification.EXTRA_PROGRESS_MAX, Notification.VISIBILITY_PRIVATE) == 0 ? this.mBuilder.mN.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE) : true;
                if (this.mBuilder.mN.hasLargeIcon() && !z) {
                    endMargin = 17104961;
                }
            }
            contentView.setViewLayoutMarginEndDimen(id, endMargin);
        }
    }

    public static class MessagingStyle extends Style {
        public static final int MAXIMUM_RETAINED_MESSAGES = 25;
        CharSequence mConversationTitle;
        List<Message> mMessages;
        CharSequence mUserDisplayName;

        public static final class Message {
            static final String KEY_DATA_MIME_TYPE = "type";
            static final String KEY_DATA_URI = "uri";
            static final String KEY_SENDER = "sender";
            static final String KEY_TEXT = "text";
            static final String KEY_TIMESTAMP = "time";
            private String mDataMimeType;
            private Uri mDataUri;
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
                bundle.putLong(KEY_TIMESTAMP, this.mTimestamp);
                if (this.mSender != null) {
                    bundle.putCharSequence(KEY_SENDER, this.mSender);
                }
                if (this.mDataMimeType != null) {
                    bundle.putString(KEY_DATA_MIME_TYPE, this.mDataMimeType);
                }
                if (this.mDataUri != null) {
                    bundle.putParcelable(KEY_DATA_URI, this.mDataUri);
                }
                return bundle;
            }

            static Bundle[] getBundleArrayForMessages(List<Message> messages) {
                Bundle[] bundles = new Bundle[messages.size()];
                int N = messages.size();
                for (int i = Notification.VISIBILITY_PRIVATE; i < N; i += Notification.VISIBILITY_PUBLIC) {
                    bundles[i] = ((Message) messages.get(i)).toBundle();
                }
                return bundles;
            }

            static List<Message> getMessagesFromBundleArray(Parcelable[] bundles) {
                List<Message> messages = new ArrayList(bundles.length);
                for (int i = Notification.VISIBILITY_PRIVATE; i < bundles.length; i += Notification.VISIBILITY_PUBLIC) {
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
                    if (!bundle.containsKey(KEY_TEXT) || !bundle.containsKey(KEY_TIMESTAMP)) {
                        return null;
                    }
                    Message message = new Message(bundle.getCharSequence(KEY_TEXT), bundle.getLong(KEY_TIMESTAMP), bundle.getCharSequence(KEY_SENDER));
                    if (bundle.containsKey(KEY_DATA_MIME_TYPE) && bundle.containsKey(KEY_DATA_URI)) {
                        message.setData(bundle.getString(KEY_DATA_MIME_TYPE), (Uri) bundle.getParcelable(KEY_DATA_URI));
                    }
                    return message;
                } catch (ClassCastException e) {
                    return null;
                }
            }
        }

        MessagingStyle() {
            this.mMessages = new ArrayList();
        }

        public MessagingStyle(CharSequence userDisplayName) {
            this.mMessages = new ArrayList();
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
            this.mMessages.add(new Message(text, timestamp, sender));
            if (this.mMessages.size() > MAXIMUM_RETAINED_MESSAGES) {
                this.mMessages.remove(Notification.VISIBILITY_PRIVATE);
            }
            return this;
        }

        public MessagingStyle addMessage(Message message) {
            this.mMessages.add(message);
            if (this.mMessages.size() > MAXIMUM_RETAINED_MESSAGES) {
                this.mMessages.remove(Notification.VISIBILITY_PRIVATE);
            }
            return this;
        }

        public List<Message> getMessages() {
            return this.mMessages;
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
            fixTitleAndTextExtras(extras);
        }

        private void fixTitleAndTextExtras(Bundle extras) {
            CharSequence title;
            Message m = findLatestIncomingMessage();
            CharSequence -get1 = m == null ? null : m.mText;
            CharSequence -get0 = m == null ? null : TextUtils.isEmpty(m.mSender) ? this.mUserDisplayName : m.mSender;
            if (TextUtils.isEmpty(this.mConversationTitle)) {
                title = -get0;
            } else if (TextUtils.isEmpty(-get0)) {
                title = this.mConversationTitle;
            } else {
                BidiFormatter bidi = BidiFormatter.getInstance();
                Context -get12 = this.mBuilder.mContext;
                Object[] objArr = new Object[Notification.PRIORITY_MAX];
                objArr[Notification.VISIBILITY_PRIVATE] = bidi.unicodeWrap(this.mConversationTitle);
                objArr[Notification.VISIBILITY_PUBLIC] = bidi.unicodeWrap(m.mSender);
                title = -get12.getString(17040891, objArr);
            }
            if (title != null) {
                extras.putCharSequence(Notification.EXTRA_TITLE, title);
            }
            if (-get1 != null) {
                extras.putCharSequence(Notification.EXTRA_TEXT, -get1);
            }
        }

        protected void restoreFromExtras(Bundle extras) {
            super.restoreFromExtras(extras);
            this.mMessages.clear();
            this.mUserDisplayName = extras.getCharSequence(Notification.EXTRA_SELF_DISPLAY_NAME);
            this.mConversationTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE);
            Parcelable[] parcelables = extras.getParcelableArray(Notification.EXTRA_MESSAGES);
            if (parcelables != null && (parcelables instanceof Parcelable[])) {
                this.mMessages = Message.getMessagesFromBundleArray(parcelables);
            }
        }

        public RemoteViews makeContentView() {
            Message m = findLatestIncomingMessage();
            CharSequence -get0 = this.mConversationTitle != null ? this.mConversationTitle : m == null ? null : m.mSender;
            CharSequence makeMessageLine = m == null ? null : this.mConversationTitle != null ? makeMessageLine(m) : m.mText;
            return this.mBuilder.applyStandardTemplate(this.mBuilder.getBaseLayoutResource(), false, -get0, makeMessageLine);
        }

        private Message findLatestIncomingMessage() {
            for (int i = this.mMessages.size() + Notification.VISIBILITY_SECRET; i >= 0; i += Notification.VISIBILITY_SECRET) {
                Message m = (Message) this.mMessages.get(i);
                if (!TextUtils.isEmpty(m.mSender)) {
                    return m;
                }
            }
            if (this.mMessages.isEmpty()) {
                return null;
            }
            return (Message) this.mMessages.get(this.mMessages.size() + Notification.VISIBILITY_SECRET);
        }

        public RemoteViews makeBigContentView() {
            CharSequence title;
            if (TextUtils.isEmpty(this.mBigContentTitle)) {
                title = this.mConversationTitle;
            } else {
                title = this.mBigContentTitle;
            }
            boolean hasTitle = !TextUtils.isEmpty(title);
            if (this.mMessages.size() == Notification.VISIBILITY_PUBLIC) {
                CharSequence bigTitle;
                CharSequence text;
                if (hasTitle) {
                    bigTitle = title;
                    text = makeMessageLine((Message) this.mMessages.get(Notification.VISIBILITY_PRIVATE));
                } else {
                    bigTitle = ((Message) this.mMessages.get(Notification.VISIBILITY_PRIVATE)).mSender;
                    text = ((Message) this.mMessages.get(Notification.VISIBILITY_PRIVATE)).mText;
                }
                RemoteViews contentView = this.mBuilder.applyStandardTemplateWithActions(this.mBuilder.getBigTextLayoutResource(), false, bigTitle, null);
                BigTextStyle.applyBigTextContentView(this.mBuilder, contentView, text);
                return contentView;
            }
            int i;
            contentView = this.mBuilder.applyStandardTemplateWithActions(this.mBuilder.getMessagingLayoutResource(), false, title, null);
            int[] rowIds = new int[]{16909235, 16909236, 16909237, 16909238, 16909239, 16909240, 16909241};
            int length = rowIds.length;
            for (i = Notification.VISIBILITY_PRIVATE; i < length; i += Notification.VISIBILITY_PUBLIC) {
                contentView.setViewVisibility(rowIds[i], Notification.FLAG_ONLY_ALERT_ONCE);
            }
            int i2 = Notification.VISIBILITY_PRIVATE;
            contentView.setViewLayoutMarginBottomDimen(16909243, hasTitle ? 17105220 : Notification.VISIBILITY_PRIVATE);
            String str = "setNumIndentLines";
            i = !this.mBuilder.mN.hasLargeIcon() ? Notification.VISIBILITY_PRIVATE : hasTitle ? Notification.VISIBILITY_PUBLIC : Notification.PRIORITY_MAX;
            contentView.setInt(16909430, str, i);
            int contractedChildId = Notification.VISIBILITY_SECRET;
            Message contractedMessage = findLatestIncomingMessage();
            int firstMessage = Math.max(Notification.VISIBILITY_PRIVATE, this.mMessages.size() - rowIds.length);
            while (firstMessage + i2 < this.mMessages.size() && i2 < rowIds.length) {
                Message m = (Message) this.mMessages.get(firstMessage + i2);
                int rowId = rowIds[i2];
                contentView.setViewVisibility(rowId, Notification.VISIBILITY_PRIVATE);
                contentView.setTextViewText(rowId, makeMessageLine(m));
                if (contractedMessage == m) {
                    contractedChildId = rowId;
                }
                i2 += Notification.VISIBILITY_PUBLIC;
            }
            contentView.setInt(16909430, "setContractedChildId", contractedChildId);
            return contentView;
        }

        private CharSequence makeMessageLine(Message m) {
            BidiFormatter bidi = BidiFormatter.getInstance();
            SpannableStringBuilder sb = new SpannableStringBuilder();
            if (TextUtils.isEmpty(m.mSender)) {
                sb.append(bidi.unicodeWrap(this.mUserDisplayName == null ? ProxyInfo.LOCAL_EXCL_LIST : this.mUserDisplayName), makeFontColorSpan(this.mBuilder.resolveContrastColor()), Notification.VISIBILITY_PRIVATE);
            } else {
                sb.append(bidi.unicodeWrap(m.mSender), makeFontColorSpan(Color.BLACK), Notification.VISIBILITY_PRIVATE);
            }
            sb.append("  ").append(bidi.unicodeWrap(m.mText == null ? ProxyInfo.LOCAL_EXCL_LIST : m.mText));
            return sb;
        }

        public RemoteViews makeHeadsUpContentView() {
            Message m = findLatestIncomingMessage();
            CharSequence -get0 = this.mConversationTitle != null ? this.mConversationTitle : m == null ? null : m.mSender;
            CharSequence makeMessageLine = m == null ? null : this.mConversationTitle != null ? makeMessageLine(m) : m.mText;
            return this.mBuilder.applyStandardTemplateWithActions(this.mBuilder.getBigBaseLayoutResource(), false, -get0, makeMessageLine);
        }

        private static TextAppearanceSpan makeFontColorSpan(int color) {
            return new TextAppearanceSpan(null, Notification.VISIBILITY_PRIVATE, Notification.VISIBILITY_PRIVATE, ColorStateList.valueOf(color), null);
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
        private ArrayList<Action> mActions;
        private Bitmap mBackground;
        private int mContentActionIndex;
        private int mContentIcon;
        private int mContentIconGravity;
        private int mCustomContentHeight;
        private int mCustomSizePreset;
        private String mDismissalId;
        private PendingIntent mDisplayIntent;
        private int mFlags;
        private int mGravity;
        private int mHintScreenTimeout;
        private ArrayList<Notification> mPages;

        private void setFlag(int r1, boolean r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.Notification.WearableExtender.setFlag(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.Notification.WearableExtender.setFlag(int, boolean):void");
        }

        public WearableExtender() {
            this.mActions = new ArrayList();
            this.mFlags = SIZE_XSMALL;
            this.mPages = new ArrayList();
            this.mContentIconGravity = DEFAULT_CONTENT_ICON_GRAVITY;
            this.mContentActionIndex = UNSET_ACTION_INDEX;
            this.mCustomSizePreset = SIZE_DEFAULT;
            this.mGravity = DEFAULT_GRAVITY;
        }

        public WearableExtender(Notification notif) {
            this.mActions = new ArrayList();
            this.mFlags = SIZE_XSMALL;
            this.mPages = new ArrayList();
            this.mContentIconGravity = DEFAULT_CONTENT_ICON_GRAVITY;
            this.mContentActionIndex = UNSET_ACTION_INDEX;
            this.mCustomSizePreset = SIZE_DEFAULT;
            this.mGravity = DEFAULT_GRAVITY;
            Bundle wearableBundle = notif.extras.getBundle(EXTRA_WEARABLE_EXTENSIONS);
            if (wearableBundle != null) {
                List<Action> actions = wearableBundle.getParcelableArrayList(KEY_ACTIONS);
                if (actions != null) {
                    this.mActions.addAll(actions);
                }
                this.mFlags = wearableBundle.getInt(KEY_FLAGS, SIZE_XSMALL);
                this.mDisplayIntent = (PendingIntent) wearableBundle.getParcelable(KEY_DISPLAY_INTENT);
                Notification[] pages = Notification.getNotificationArrayFromBundle(wearableBundle, KEY_PAGES);
                if (pages != null) {
                    Collections.addAll(this.mPages, pages);
                }
                this.mBackground = (Bitmap) wearableBundle.getParcelable(KEY_BACKGROUND);
                this.mContentIcon = wearableBundle.getInt(KEY_CONTENT_ICON);
                this.mContentIconGravity = wearableBundle.getInt(KEY_CONTENT_ICON_GRAVITY, DEFAULT_CONTENT_ICON_GRAVITY);
                this.mContentActionIndex = wearableBundle.getInt(KEY_CONTENT_ACTION_INDEX, UNSET_ACTION_INDEX);
                this.mCustomSizePreset = wearableBundle.getInt(KEY_CUSTOM_SIZE_PRESET, SIZE_DEFAULT);
                this.mCustomContentHeight = wearableBundle.getInt(KEY_CUSTOM_CONTENT_HEIGHT);
                this.mGravity = wearableBundle.getInt(KEY_GRAVITY, DEFAULT_GRAVITY);
                this.mHintScreenTimeout = wearableBundle.getInt(KEY_HINT_SCREEN_TIMEOUT);
                this.mDismissalId = wearableBundle.getString(KEY_DISMISSAL_ID);
            }
        }

        public Builder extend(Builder builder) {
            Bundle wearableBundle = new Bundle();
            if (!this.mActions.isEmpty()) {
                wearableBundle.putParcelableArrayList(KEY_ACTIONS, this.mActions);
            }
            if (this.mFlags != SIZE_XSMALL) {
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
            if (this.mContentActionIndex != UNSET_ACTION_INDEX) {
                wearableBundle.putInt(KEY_CONTENT_ACTION_INDEX, this.mContentActionIndex);
            }
            if (this.mCustomSizePreset != 0) {
                wearableBundle.putInt(KEY_CUSTOM_SIZE_PRESET, this.mCustomSizePreset);
            }
            if (this.mCustomContentHeight != 0) {
                wearableBundle.putInt(KEY_CUSTOM_CONTENT_HEIGHT, this.mCustomContentHeight);
            }
            if (this.mGravity != DEFAULT_GRAVITY) {
                wearableBundle.putInt(KEY_GRAVITY, this.mGravity);
            }
            if (this.mHintScreenTimeout != 0) {
                wearableBundle.putInt(KEY_HINT_SCREEN_TIMEOUT, this.mHintScreenTimeout);
            }
            if (this.mDismissalId != null) {
                wearableBundle.putString(KEY_DISMISSAL_ID, this.mDismissalId);
            }
            builder.getExtras().putBundle(EXTRA_WEARABLE_EXTENSIONS, wearableBundle);
            return builder;
        }

        public /* bridge */ /* synthetic */ Object m2clone() throws CloneNotSupportedException {
            return clone();
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
            setFlag(FLAG_START_SCROLL_BOTTOM, startScrollBottom);
            return this;
        }

        public boolean getStartScrollBottom() {
            return (this.mFlags & FLAG_START_SCROLL_BOTTOM) != 0;
        }

        public WearableExtender setContentIntentAvailableOffline(boolean contentIntentAvailableOffline) {
            setFlag(SIZE_XSMALL, contentIntentAvailableOffline);
            return this;
        }

        public boolean getContentIntentAvailableOffline() {
            return (this.mFlags & SIZE_XSMALL) != 0;
        }

        public WearableExtender setHintHideIcon(boolean hintHideIcon) {
            setFlag(SIZE_SMALL, hintHideIcon);
            return this;
        }

        public boolean getHintHideIcon() {
            return (this.mFlags & SIZE_SMALL) != 0;
        }

        public WearableExtender setHintShowBackgroundOnly(boolean hintShowBackgroundOnly) {
            setFlag(SIZE_LARGE, hintShowBackgroundOnly);
            return this;
        }

        public boolean getHintShowBackgroundOnly() {
            return (this.mFlags & SIZE_LARGE) != 0;
        }

        public WearableExtender setHintAvoidBackgroundClipping(boolean hintAvoidBackgroundClipping) {
            setFlag(FLAG_HINT_AVOID_BACKGROUND_CLIPPING, hintAvoidBackgroundClipping);
            return this;
        }

        public boolean getHintAvoidBackgroundClipping() {
            return (this.mFlags & FLAG_HINT_AVOID_BACKGROUND_CLIPPING) != 0;
        }

        public WearableExtender setHintScreenTimeout(int timeout) {
            this.mHintScreenTimeout = timeout;
            return this;
        }

        public int getHintScreenTimeout() {
            return this.mHintScreenTimeout;
        }

        public WearableExtender setHintAmbientBigPicture(boolean hintAmbientBigPicture) {
            setFlag(FLAG_BIG_PICTURE_AMBIENT, hintAmbientBigPicture);
            return this;
        }

        public boolean getHintAmbientBigPicture() {
            return (this.mFlags & FLAG_BIG_PICTURE_AMBIENT) != 0;
        }

        public WearableExtender setHintContentIntentLaunchesActivity(boolean hintContentIntentLaunchesActivity) {
            setFlag(FLAG_HINT_CONTENT_INTENT_LAUNCHES_ACTIVITY, hintContentIntentLaunchesActivity);
            return this;
        }

        public boolean getHintContentIntentLaunchesActivity() {
            return (this.mFlags & FLAG_HINT_CONTENT_INTENT_LAUNCHES_ACTIVITY) != 0;
        }

        public WearableExtender setDismissalId(String dismissalId) {
            this.mDismissalId = dismissalId;
            return this;
        }

        public String getDismissalId() {
            return this.mDismissalId;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.Notification.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.Notification.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.Notification.<clinit>():void");
    }

    public String getGroup() {
        return this.mGroupKey;
    }

    public String getSortKey() {
        return this.mSortKey;
    }

    public Notification() {
        this.audioStreamType = VISIBILITY_SECRET;
        this.audioAttributes = AUDIO_ATTRIBUTES_DEFAULT;
        this.color = VISIBILITY_PRIVATE;
        this.mSyncLock = new Object();
        this.extras = new Bundle();
        this.when = System.currentTimeMillis();
        this.creationTime = System.currentTimeMillis();
        this.priority = VISIBILITY_PRIVATE;
    }

    public Notification(Context context, int icon, CharSequence tickerText, long when, CharSequence contentTitle, CharSequence contentText, Intent contentIntent) {
        this.audioStreamType = VISIBILITY_SECRET;
        this.audioAttributes = AUDIO_ATTRIBUTES_DEFAULT;
        this.color = VISIBILITY_PRIVATE;
        this.mSyncLock = new Object();
        this.extras = new Bundle();
        new Builder(context).setWhen(when).setSmallIcon(icon).setTicker(tickerText).setContentTitle(contentTitle).setContentText(contentText).setContentIntent(PendingIntent.getActivity(context, VISIBILITY_PRIVATE, contentIntent, VISIBILITY_PRIVATE)).buildInto(this);
    }

    @Deprecated
    public Notification(int icon, CharSequence tickerText, long when) {
        this.audioStreamType = VISIBILITY_SECRET;
        this.audioAttributes = AUDIO_ATTRIBUTES_DEFAULT;
        this.color = VISIBILITY_PRIVATE;
        this.mSyncLock = new Object();
        this.extras = new Bundle();
        this.icon = icon;
        this.tickerText = tickerText;
        this.when = when;
        this.creationTime = System.currentTimeMillis();
    }

    public Notification(Parcel parcel) {
        this.audioStreamType = VISIBILITY_SECRET;
        this.audioAttributes = AUDIO_ATTRIBUTES_DEFAULT;
        this.color = VISIBILITY_PRIVATE;
        this.mSyncLock = new Object();
        this.extras = new Bundle();
        readFromParcelImpl(parcel);
        this.allPendingIntents = parcel.readArraySet(null);
    }

    private void readFromParcelImpl(Parcel parcel) {
        int version = parcel.readInt();
        this.when = parcel.readLong();
        this.creationTime = parcel.readLong();
        if (parcel.readInt() != 0) {
            this.mSmallIcon = (Icon) Icon.CREATOR.createFromParcel(parcel);
            if (this.mSmallIcon.getType() == PRIORITY_MAX) {
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
    }

    public /* bridge */ /* synthetic */ Object m0clone() throws CloneNotSupportedException {
        return clone();
    }

    public Notification clone() {
        Notification that = new Notification();
        cloneInto(that, true);
        return that;
    }

    public void cloneInto(Notification that, boolean heavy) {
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
            System.arraycopy(vibrate, VISIBILITY_PRIVATE, vib, VISIBILITY_PRIVATE, N);
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
            for (int i = VISIBILITY_PRIVATE; i < this.actions.length; i += VISIBILITY_PUBLIC) {
                that.actions[i] = this.actions[i].clone();
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
        if (this.extras != null && !this.extras.isEmpty()) {
            Set<String> keyset = this.extras.keySet();
            int N = keyset.size();
            String[] keys = (String[]) keyset.toArray(new String[N]);
            for (int i = VISIBILITY_PRIVATE; i < N; i += VISIBILITY_PUBLIC) {
                String key = keys[i];
                Object obj = this.extras.get(key);
                if (obj != null && ((obj instanceof Parcelable) || (obj instanceof Parcelable[]) || (obj instanceof SparseArray) || (obj instanceof ArrayList))) {
                    this.extras.remove(key);
                }
            }
        }
    }

    public static CharSequence safeCharSequence(CharSequence cs) {
        if (cs == null) {
            return cs;
        }
        if (cs.length() > MAX_CHARSEQUENCE_LENGTH) {
            cs = cs.subSequence(VISIBILITY_PRIVATE, MAX_CHARSEQUENCE_LENGTH);
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
        Object[] spans = ss.getSpans(VISIBILITY_PRIVATE, ss.length(), Object.class);
        SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
        int length = spans.length;
        for (int i = VISIBILITY_PRIVATE; i < length; i += VISIBILITY_PUBLIC) {
            Object resultSpan;
            TextAppearanceSpan span = spans[i];
            TextAppearanceSpan resultSpan2 = span;
            if (span instanceof CharacterStyle) {
                resultSpan2 = span.getUnderlying();
            }
            if (resultSpan2 instanceof TextAppearanceSpan) {
                TextAppearanceSpan originalSpan = resultSpan2;
                resultSpan = new TextAppearanceSpan(originalSpan.getFamily(), originalSpan.getTextStyle(), VISIBILITY_SECRET, originalSpan.getTextColor(), originalSpan.getLinkTextColor());
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
        return VISIBILITY_PRIVATE;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        boolean collectPendingIntents = this.allPendingIntents == null;
        if (collectPendingIntents) {
            PendingIntent.setOnMarshaledListener(new -void_writeToParcel_android_os_Parcel_parcel_int_flags_LambdaImpl0(this, parcel));
        }
        try {
            writeToParcelImpl(parcel, flags);
            parcel.writeArraySet(this.allPendingIntents);
            if (collectPendingIntents) {
                PendingIntent.setOnMarshaledListener(null);
            }
        } catch (Throwable th) {
            if (collectPendingIntents) {
                PendingIntent.setOnMarshaledListener(null);
            }
        }
    }

    /* synthetic */ void -android_app_Notification_lambda$1(Parcel parcel, PendingIntent intent, Parcel out, int outFlags) {
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
        parcel.writeInt(VISIBILITY_PUBLIC);
        parcel.writeLong(this.when);
        parcel.writeLong(this.creationTime);
        if (this.mSmallIcon == null && this.icon != 0) {
            this.mSmallIcon = Icon.createWithResource(ProxyInfo.LOCAL_EXCL_LIST, this.icon);
        }
        if (this.mSmallIcon != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.mSmallIcon.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        parcel.writeInt(this.number);
        if (this.contentIntent != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.contentIntent.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        if (this.deleteIntent != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.deleteIntent.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        if (this.tickerText != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            TextUtils.writeToParcel(this.tickerText, parcel, flags);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        if (this.tickerView != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.tickerView.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        if (this.contentView != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.contentView.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        if (this.mLargeIcon == null && this.largeIcon != null) {
            this.mLargeIcon = Icon.createWithBitmap(this.largeIcon);
        }
        if (this.mLargeIcon != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.mLargeIcon.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        parcel.writeInt(this.defaults);
        parcel.writeInt(this.flags);
        if (this.sound != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.sound.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        parcel.writeInt(this.audioStreamType);
        if (this.audioAttributes != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.audioAttributes.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        parcel.writeLongArray(this.vibrate);
        parcel.writeInt(this.ledARGB);
        parcel.writeInt(this.ledOnMS);
        parcel.writeInt(this.ledOffMS);
        parcel.writeInt(this.iconLevel);
        if (this.fullScreenIntent != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.fullScreenIntent.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        parcel.writeInt(this.priority);
        parcel.writeString(this.category);
        parcel.writeString(this.mGroupKey);
        parcel.writeString(this.mSortKey);
        parcel.writeBundle(this.extras);
        parcel.writeTypedArray(this.actions, VISIBILITY_PRIVATE);
        if (this.bigContentView != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.bigContentView.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        if (this.headsUpContentView != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.headsUpContentView.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        parcel.writeInt(this.visibility);
        if (this.publicVersion != null) {
            parcel.writeInt(VISIBILITY_PUBLIC);
            this.publicVersion.writeToParcel(parcel, VISIBILITY_PRIVATE);
        } else {
            parcel.writeInt(VISIBILITY_PRIVATE);
        }
        parcel.writeInt(this.color);
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
        addFieldsFromContext(context.getApplicationInfo(), context.getUserId(), notification);
    }

    public static void addFieldsFromContext(ApplicationInfo ai, int userId, Notification notification) {
        notification.extras.putParcelable(EXTRA_BUILDER_APPLICATION_INFO, ai);
        notification.extras.putInt(EXTRA_ORIGINATING_USERID, userId);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Notification(pri=");
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
        if ((this.defaults & PRIORITY_MAX) != 0) {
            sb.append("default");
        } else if (this.vibrate != null) {
            int N = this.vibrate.length + VISIBILITY_SECRET;
            sb.append("[");
            for (int i = VISIBILITY_PRIVATE; i < N; i += VISIBILITY_PUBLIC) {
                sb.append(this.vibrate[i]);
                sb.append(',');
            }
            if (N != VISIBILITY_SECRET) {
                sb.append(this.vibrate[N]);
            }
            sb.append("]");
        } else {
            sb.append("null");
        }
        sb.append(" sound=");
        if ((this.defaults & VISIBILITY_PUBLIC) != 0) {
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
        Object[] objArr = new Object[VISIBILITY_PUBLIC];
        objArr[VISIBILITY_PRIVATE] = Integer.valueOf(this.color);
        sb.append(String.format(" color=0x%08x", objArr));
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
            case VISIBILITY_SECRET /*-1*/:
                return "SECRET";
            case VISIBILITY_PRIVATE /*0*/:
                return "PRIVATE";
            case VISIBILITY_PUBLIC /*1*/:
                return "PUBLIC";
            default:
                return "UNKNOWN(" + String.valueOf(vis) + ")";
        }
    }

    public static String priorityToString(int pri) {
        switch (pri) {
            case PRIORITY_MIN /*-2*/:
                return "MIN";
            case VISIBILITY_SECRET /*-1*/:
                return "LOW";
            case VISIBILITY_PRIVATE /*0*/:
                return "DEFAULT";
            case VISIBILITY_PUBLIC /*1*/:
                return "HIGH";
            case PRIORITY_MAX /*2*/:
                return "MAX";
            default:
                return "UNKNOWN(" + String.valueOf(pri) + ")";
        }
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
        return (this.mGroupKey == null || (this.flags & FLAG_GROUP_SUMMARY) == 0) ? false : true;
    }

    public boolean isGroupChild() {
        return this.mGroupKey != null && (this.flags & FLAG_GROUP_SUMMARY) == 0;
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
