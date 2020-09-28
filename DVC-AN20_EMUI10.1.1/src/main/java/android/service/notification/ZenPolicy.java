package android.service.notification;

import android.media.AudioSystem;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Contacts;
import android.provider.MediaStore;
import android.util.proto.ProtoOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public final class ZenPolicy implements Parcelable {
    public static final Parcelable.Creator<ZenPolicy> CREATOR = new Parcelable.Creator<ZenPolicy>() {
        /* class android.service.notification.ZenPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ZenPolicy createFromParcel(Parcel source) {
            ZenPolicy policy = new ZenPolicy();
            policy.mPriorityCategories = source.readArrayList(Integer.class.getClassLoader());
            policy.mVisualEffects = source.readArrayList(Integer.class.getClassLoader());
            policy.mPriorityCalls = source.readInt();
            policy.mPriorityMessages = source.readInt();
            return policy;
        }

        @Override // android.os.Parcelable.Creator
        public ZenPolicy[] newArray(int size) {
            return new ZenPolicy[size];
        }
    };
    public static final int PEOPLE_TYPE_ANYONE = 1;
    public static final int PEOPLE_TYPE_CONTACTS = 2;
    public static final int PEOPLE_TYPE_NONE = 4;
    public static final int PEOPLE_TYPE_STARRED = 3;
    public static final int PEOPLE_TYPE_UNSET = 0;
    public static final int PRIORITY_CATEGORY_ALARMS = 5;
    public static final int PRIORITY_CATEGORY_CALLS = 3;
    public static final int PRIORITY_CATEGORY_EVENTS = 1;
    public static final int PRIORITY_CATEGORY_MEDIA = 6;
    public static final int PRIORITY_CATEGORY_MESSAGES = 2;
    public static final int PRIORITY_CATEGORY_REMINDERS = 0;
    public static final int PRIORITY_CATEGORY_REPEAT_CALLERS = 4;
    public static final int PRIORITY_CATEGORY_SYSTEM = 7;
    public static final int STATE_ALLOW = 1;
    public static final int STATE_DISALLOW = 2;
    public static final int STATE_UNSET = 0;
    public static final int VISUAL_EFFECT_AMBIENT = 5;
    public static final int VISUAL_EFFECT_BADGE = 4;
    public static final int VISUAL_EFFECT_FULL_SCREEN_INTENT = 0;
    public static final int VISUAL_EFFECT_LIGHTS = 1;
    public static final int VISUAL_EFFECT_NOTIFICATION_LIST = 6;
    public static final int VISUAL_EFFECT_PEEK = 2;
    public static final int VISUAL_EFFECT_STATUS_BAR = 3;
    private int mPriorityCalls = 0;
    private ArrayList<Integer> mPriorityCategories = new ArrayList<>(Collections.nCopies(8, 0));
    private int mPriorityMessages = 0;
    private ArrayList<Integer> mVisualEffects = new ArrayList<>(Collections.nCopies(7, 0));

    @Retention(RetentionPolicy.SOURCE)
    public @interface PeopleType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PriorityCategory {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface VisualEffect {
    }

    public int getPriorityMessageSenders() {
        return this.mPriorityMessages;
    }

    public int getPriorityCallSenders() {
        return this.mPriorityCalls;
    }

    public int getPriorityCategoryReminders() {
        return this.mPriorityCategories.get(0).intValue();
    }

    public int getPriorityCategoryEvents() {
        return this.mPriorityCategories.get(1).intValue();
    }

    public int getPriorityCategoryMessages() {
        return this.mPriorityCategories.get(2).intValue();
    }

    public int getPriorityCategoryCalls() {
        return this.mPriorityCategories.get(3).intValue();
    }

    public int getPriorityCategoryRepeatCallers() {
        return this.mPriorityCategories.get(4).intValue();
    }

    public int getPriorityCategoryAlarms() {
        return this.mPriorityCategories.get(5).intValue();
    }

    public int getPriorityCategoryMedia() {
        return this.mPriorityCategories.get(6).intValue();
    }

    public int getPriorityCategorySystem() {
        return this.mPriorityCategories.get(7).intValue();
    }

    public int getVisualEffectFullScreenIntent() {
        return this.mVisualEffects.get(0).intValue();
    }

    public int getVisualEffectLights() {
        return this.mVisualEffects.get(1).intValue();
    }

    public int getVisualEffectPeek() {
        return this.mVisualEffects.get(2).intValue();
    }

    public int getVisualEffectStatusBar() {
        return this.mVisualEffects.get(3).intValue();
    }

    public int getVisualEffectBadge() {
        return this.mVisualEffects.get(4).intValue();
    }

    public int getVisualEffectAmbient() {
        return this.mVisualEffects.get(5).intValue();
    }

    public int getVisualEffectNotificationList() {
        return this.mVisualEffects.get(6).intValue();
    }

    public boolean shouldHideAllVisualEffects() {
        for (int i = 0; i < this.mVisualEffects.size(); i++) {
            if (this.mVisualEffects.get(i).intValue() != 2) {
                return false;
            }
        }
        return true;
    }

    public boolean shouldShowAllVisualEffects() {
        for (int i = 0; i < this.mVisualEffects.size(); i++) {
            if (this.mVisualEffects.get(i).intValue() != 1) {
                return false;
            }
        }
        return true;
    }

    public static final class Builder {
        private ZenPolicy mZenPolicy;

        public Builder() {
            this.mZenPolicy = new ZenPolicy();
        }

        public Builder(ZenPolicy policy) {
            if (policy != null) {
                this.mZenPolicy = policy.copy();
            } else {
                this.mZenPolicy = new ZenPolicy();
            }
        }

        public ZenPolicy build() {
            return this.mZenPolicy.copy();
        }

        public Builder allowAllSounds() {
            for (int i = 0; i < this.mZenPolicy.mPriorityCategories.size(); i++) {
                this.mZenPolicy.mPriorityCategories.set(i, 1);
            }
            this.mZenPolicy.mPriorityMessages = 1;
            this.mZenPolicy.mPriorityCalls = 1;
            return this;
        }

        public Builder disallowAllSounds() {
            for (int i = 0; i < this.mZenPolicy.mPriorityCategories.size(); i++) {
                this.mZenPolicy.mPriorityCategories.set(i, 2);
            }
            this.mZenPolicy.mPriorityMessages = 4;
            this.mZenPolicy.mPriorityCalls = 4;
            return this;
        }

        public Builder showAllVisualEffects() {
            for (int i = 0; i < this.mZenPolicy.mVisualEffects.size(); i++) {
                this.mZenPolicy.mVisualEffects.set(i, 1);
            }
            return this;
        }

        public Builder hideAllVisualEffects() {
            for (int i = 0; i < this.mZenPolicy.mVisualEffects.size(); i++) {
                this.mZenPolicy.mVisualEffects.set(i, 2);
            }
            return this;
        }

        public Builder unsetPriorityCategory(int category) {
            this.mZenPolicy.mPriorityCategories.set(category, 0);
            if (category == 2) {
                this.mZenPolicy.mPriorityMessages = 0;
            } else if (category == 3) {
                this.mZenPolicy.mPriorityCalls = 0;
            }
            return this;
        }

        public Builder unsetVisualEffect(int effect) {
            this.mZenPolicy.mVisualEffects.set(effect, 0);
            return this;
        }

        public Builder allowReminders(boolean allow) {
            this.mZenPolicy.mPriorityCategories.set(0, Integer.valueOf(allow ? 1 : 2));
            return this;
        }

        public Builder allowEvents(boolean allow) {
            this.mZenPolicy.mPriorityCategories.set(1, Integer.valueOf(allow ? 1 : 2));
            return this;
        }

        public Builder allowMessages(int audienceType) {
            if (audienceType == 0) {
                return unsetPriorityCategory(2);
            }
            if (audienceType == 4) {
                this.mZenPolicy.mPriorityCategories.set(2, 2);
            } else if (audienceType != 1 && audienceType != 2 && audienceType != 3) {
                return this;
            } else {
                this.mZenPolicy.mPriorityCategories.set(2, 1);
            }
            this.mZenPolicy.mPriorityMessages = audienceType;
            return this;
        }

        public Builder allowCalls(int audienceType) {
            if (audienceType == 0) {
                return unsetPriorityCategory(3);
            }
            if (audienceType == 4) {
                this.mZenPolicy.mPriorityCategories.set(3, 2);
            } else if (audienceType != 1 && audienceType != 2 && audienceType != 3) {
                return this;
            } else {
                this.mZenPolicy.mPriorityCategories.set(3, 1);
            }
            this.mZenPolicy.mPriorityCalls = audienceType;
            return this;
        }

        public Builder allowRepeatCallers(boolean allow) {
            this.mZenPolicy.mPriorityCategories.set(4, Integer.valueOf(allow ? 1 : 2));
            return this;
        }

        public Builder allowAlarms(boolean allow) {
            this.mZenPolicy.mPriorityCategories.set(5, Integer.valueOf(allow ? 1 : 2));
            return this;
        }

        public Builder allowMedia(boolean allow) {
            this.mZenPolicy.mPriorityCategories.set(6, Integer.valueOf(allow ? 1 : 2));
            return this;
        }

        public Builder allowSystem(boolean allow) {
            this.mZenPolicy.mPriorityCategories.set(7, Integer.valueOf(allow ? 1 : 2));
            return this;
        }

        public Builder allowCategory(int category, boolean allow) {
            if (category == 0) {
                allowReminders(allow);
            } else if (category == 1) {
                allowEvents(allow);
            } else if (category == 4) {
                allowRepeatCallers(allow);
            } else if (category == 5) {
                allowAlarms(allow);
            } else if (category == 6) {
                allowMedia(allow);
            } else if (category == 7) {
                allowSystem(allow);
            }
            return this;
        }

        public Builder showFullScreenIntent(boolean show) {
            this.mZenPolicy.mVisualEffects.set(0, Integer.valueOf(show ? 1 : 2));
            return this;
        }

        public Builder showLights(boolean show) {
            this.mZenPolicy.mVisualEffects.set(1, Integer.valueOf(show ? 1 : 2));
            return this;
        }

        public Builder showPeeking(boolean show) {
            this.mZenPolicy.mVisualEffects.set(2, Integer.valueOf(show ? 1 : 2));
            return this;
        }

        public Builder showStatusBarIcons(boolean show) {
            this.mZenPolicy.mVisualEffects.set(3, Integer.valueOf(show ? 1 : 2));
            return this;
        }

        public Builder showBadges(boolean show) {
            this.mZenPolicy.mVisualEffects.set(4, Integer.valueOf(show ? 1 : 2));
            return this;
        }

        public Builder showInAmbientDisplay(boolean show) {
            this.mZenPolicy.mVisualEffects.set(5, Integer.valueOf(show ? 1 : 2));
            return this;
        }

        public Builder showInNotificationList(boolean show) {
            this.mZenPolicy.mVisualEffects.set(6, Integer.valueOf(show ? 1 : 2));
            return this;
        }

        public Builder showVisualEffect(int effect, boolean show) {
            switch (effect) {
                case 0:
                    showFullScreenIntent(show);
                    break;
                case 1:
                    showLights(show);
                    break;
                case 2:
                    showPeeking(show);
                    break;
                case 3:
                    showStatusBarIcons(show);
                    break;
                case 4:
                    showBadges(show);
                    break;
                case 5:
                    showInAmbientDisplay(show);
                    break;
                case 6:
                    showInNotificationList(show);
                    break;
            }
            return this;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.mPriorityCategories);
        dest.writeList(this.mVisualEffects);
        dest.writeInt(this.mPriorityCalls);
        dest.writeInt(this.mPriorityMessages);
    }

    public String toString() {
        return ZenPolicy.class.getSimpleName() + '{' + "priorityCategories=[" + priorityCategoriesToString() + "], visualEffects=[" + visualEffectsToString() + "], priorityCalls=" + peopleTypeToString(this.mPriorityCalls) + ", priorityMessages=" + peopleTypeToString(this.mPriorityMessages) + '}';
    }

    private String priorityCategoriesToString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.mPriorityCategories.size(); i++) {
            if (this.mPriorityCategories.get(i).intValue() != 0) {
                builder.append(indexToCategory(i));
                builder.append("=");
                builder.append(stateToString(this.mPriorityCategories.get(i).intValue()));
                builder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
        }
        return builder.toString();
    }

    private String visualEffectsToString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.mVisualEffects.size(); i++) {
            if (this.mVisualEffects.get(i).intValue() != 0) {
                builder.append(indexToVisualEffect(i));
                builder.append("=");
                builder.append(stateToString(this.mVisualEffects.get(i).intValue()));
                builder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
        }
        return builder.toString();
    }

    private String indexToVisualEffect(int visualEffectIndex) {
        switch (visualEffectIndex) {
            case 0:
                return "fullScreenIntent";
            case 1:
                return "lights";
            case 2:
                return "peek";
            case 3:
                return "statusBar";
            case 4:
                return "badge";
            case 5:
                return AudioSystem.DEVICE_IN_AMBIENT_NAME;
            case 6:
                return "notificationList";
            default:
                return null;
        }
    }

    private String indexToCategory(int categoryIndex) {
        switch (categoryIndex) {
            case 0:
                return "reminders";
            case 1:
                return "events";
            case 2:
                return "messages";
            case 3:
                return "calls";
            case 4:
                return "repeatCallers";
            case 5:
                return "alarms";
            case 6:
                return MediaStore.AUTHORITY;
            case 7:
                return "system";
            default:
                return null;
        }
    }

    private String stateToString(int state) {
        if (state == 0) {
            return "unset";
        }
        if (state == 1) {
            return "allow";
        }
        if (state == 2) {
            return "disallow";
        }
        return "invalidState{" + state + "}";
    }

    private String peopleTypeToString(int peopleType) {
        if (peopleType == 0) {
            return "unset";
        }
        if (peopleType == 1) {
            return "anyone";
        }
        if (peopleType == 2) {
            return Contacts.AUTHORITY;
        }
        if (peopleType == 3) {
            return "starred_contacts";
        }
        if (peopleType == 4) {
            return "none";
        }
        return "invalidPeopleType{" + peopleType + "}";
    }

    public boolean equals(Object o) {
        if (!(o instanceof ZenPolicy)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        ZenPolicy other = (ZenPolicy) o;
        if (!Objects.equals(other.mPriorityCategories, this.mPriorityCategories) || !Objects.equals(other.mVisualEffects, this.mVisualEffects) || other.mPriorityCalls != this.mPriorityCalls || other.mPriorityMessages != this.mPriorityMessages) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mPriorityCategories, this.mVisualEffects, Integer.valueOf(this.mPriorityCalls), Integer.valueOf(this.mPriorityMessages));
    }

    private int getZenPolicyPriorityCategoryState(int category) {
        switch (category) {
            case 0:
                return getPriorityCategoryReminders();
            case 1:
                return getPriorityCategoryEvents();
            case 2:
                return getPriorityCategoryMessages();
            case 3:
                return getPriorityCategoryCalls();
            case 4:
                return getPriorityCategoryRepeatCallers();
            case 5:
                return getPriorityCategoryAlarms();
            case 6:
                return getPriorityCategoryMedia();
            case 7:
                return getPriorityCategorySystem();
            default:
                return -1;
        }
    }

    private int getZenPolicyVisualEffectState(int effect) {
        switch (effect) {
            case 0:
                return getVisualEffectFullScreenIntent();
            case 1:
                return getVisualEffectLights();
            case 2:
                return getVisualEffectPeek();
            case 3:
                return getVisualEffectStatusBar();
            case 4:
                return getVisualEffectBadge();
            case 5:
                return getVisualEffectAmbient();
            case 6:
                return getVisualEffectNotificationList();
            default:
                return -1;
        }
    }

    public boolean isCategoryAllowed(int category, boolean defaultVal) {
        int zenPolicyPriorityCategoryState = getZenPolicyPriorityCategoryState(category);
        if (zenPolicyPriorityCategoryState == 1) {
            return true;
        }
        if (zenPolicyPriorityCategoryState != 2) {
            return defaultVal;
        }
        return false;
    }

    public boolean isVisualEffectAllowed(int effect, boolean defaultVal) {
        int zenPolicyVisualEffectState = getZenPolicyVisualEffectState(effect);
        if (zenPolicyVisualEffectState == 1) {
            return true;
        }
        if (zenPolicyVisualEffectState != 2) {
            return defaultVal;
        }
        return false;
    }

    public void apply(ZenPolicy policyToApply) {
        int newState;
        int i;
        int i2;
        if (policyToApply != null) {
            for (int category = 0; category < this.mPriorityCategories.size(); category++) {
                if (!(this.mPriorityCategories.get(category).intValue() == 2 || (newState = policyToApply.mPriorityCategories.get(category).intValue()) == 0)) {
                    this.mPriorityCategories.set(category, Integer.valueOf(newState));
                    if (category == 2 && this.mPriorityMessages < (i2 = policyToApply.mPriorityMessages)) {
                        this.mPriorityMessages = i2;
                    } else if (category == 3 && this.mPriorityCalls < (i = policyToApply.mPriorityCalls)) {
                        this.mPriorityCalls = i;
                    }
                }
            }
            for (int visualEffect = 0; visualEffect < this.mVisualEffects.size(); visualEffect++) {
                if (!(this.mVisualEffects.get(visualEffect).intValue() == 2 || policyToApply.mVisualEffects.get(visualEffect).intValue() == 0)) {
                    this.mVisualEffects.set(visualEffect, policyToApply.mVisualEffects.get(visualEffect));
                }
            }
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1159641169921L, getPriorityCategoryReminders());
        proto.write(1159641169922L, getPriorityCategoryEvents());
        proto.write(1159641169923L, getPriorityCategoryMessages());
        proto.write(1159641169924L, getPriorityCategoryCalls());
        proto.write(1159641169925L, getPriorityCategoryRepeatCallers());
        proto.write(1159641169926L, getPriorityCategoryAlarms());
        proto.write(1159641169927L, getPriorityCategoryMedia());
        proto.write(1159641169928L, getPriorityCategorySystem());
        proto.write(1159641169929L, getVisualEffectFullScreenIntent());
        proto.write(1159641169930L, getVisualEffectLights());
        proto.write(1159641169931L, getVisualEffectPeek());
        proto.write(1159641169932L, getVisualEffectStatusBar());
        proto.write(1159641169933L, getVisualEffectBadge());
        proto.write(1159641169934L, getVisualEffectAmbient());
        proto.write(1159641169935L, getVisualEffectNotificationList());
        proto.write(1159641169937L, getPriorityMessageSenders());
        proto.write(1159641169936L, getPriorityCallSenders());
        proto.end(token);
    }

    public ZenPolicy copy() {
        Parcel parcel = Parcel.obtain();
        try {
            writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            return CREATOR.createFromParcel(parcel);
        } finally {
            parcel.recycle();
        }
    }
}
