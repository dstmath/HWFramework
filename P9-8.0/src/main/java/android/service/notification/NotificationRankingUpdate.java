package android.service.notification;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class NotificationRankingUpdate implements Parcelable {
    public static final Creator<NotificationRankingUpdate> CREATOR = new Creator<NotificationRankingUpdate>() {
        public NotificationRankingUpdate createFromParcel(Parcel parcel) {
            return new NotificationRankingUpdate(parcel);
        }

        public NotificationRankingUpdate[] newArray(int size) {
            return new NotificationRankingUpdate[size];
        }
    };
    private final Bundle mChannels;
    private final int[] mImportance;
    private final Bundle mImportanceExplanation;
    private final String[] mInterceptedKeys;
    private final String[] mKeys;
    private final Bundle mOverrideGroupKeys;
    private final Bundle mOverridePeople;
    private final Bundle mShowBadge;
    private final Bundle mSnoozeCriteria;
    private final Bundle mSuppressedVisualEffects;
    private final Bundle mVisibilityOverrides;

    public NotificationRankingUpdate(String[] keys, String[] interceptedKeys, Bundle visibilityOverrides, Bundle suppressedVisualEffects, int[] importance, Bundle explanation, Bundle overrideGroupKeys, Bundle channels, Bundle overridePeople, Bundle snoozeCriteria, Bundle showBadge) {
        this.mKeys = keys;
        this.mInterceptedKeys = interceptedKeys;
        this.mVisibilityOverrides = visibilityOverrides;
        this.mSuppressedVisualEffects = suppressedVisualEffects;
        this.mImportance = importance;
        this.mImportanceExplanation = explanation;
        this.mOverrideGroupKeys = overrideGroupKeys;
        this.mChannels = channels;
        this.mOverridePeople = overridePeople;
        this.mSnoozeCriteria = snoozeCriteria;
        this.mShowBadge = showBadge;
    }

    public NotificationRankingUpdate(Parcel in) {
        this.mKeys = in.readStringArray();
        this.mInterceptedKeys = in.readStringArray();
        this.mVisibilityOverrides = in.readBundle();
        this.mSuppressedVisualEffects = in.readBundle();
        this.mImportance = new int[this.mKeys.length];
        in.readIntArray(this.mImportance);
        this.mImportanceExplanation = in.readBundle();
        this.mOverrideGroupKeys = in.readBundle();
        this.mChannels = in.readBundle();
        this.mOverridePeople = in.readBundle();
        this.mSnoozeCriteria = in.readBundle();
        this.mShowBadge = in.readBundle();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStringArray(this.mKeys);
        out.writeStringArray(this.mInterceptedKeys);
        out.writeBundle(this.mVisibilityOverrides);
        out.writeBundle(this.mSuppressedVisualEffects);
        out.writeIntArray(this.mImportance);
        out.writeBundle(this.mImportanceExplanation);
        out.writeBundle(this.mOverrideGroupKeys);
        out.writeBundle(this.mChannels);
        out.writeBundle(this.mOverridePeople);
        out.writeBundle(this.mSnoozeCriteria);
        out.writeBundle(this.mShowBadge);
    }

    public String[] getOrderedKeys() {
        return this.mKeys;
    }

    public String[] getInterceptedKeys() {
        return this.mInterceptedKeys;
    }

    public Bundle getVisibilityOverrides() {
        return this.mVisibilityOverrides;
    }

    public Bundle getSuppressedVisualEffects() {
        return this.mSuppressedVisualEffects;
    }

    public int[] getImportance() {
        return this.mImportance;
    }

    public Bundle getImportanceExplanation() {
        return this.mImportanceExplanation;
    }

    public Bundle getOverrideGroupKeys() {
        return this.mOverrideGroupKeys;
    }

    public Bundle getChannels() {
        return this.mChannels;
    }

    public Bundle getOverridePeople() {
        return this.mOverridePeople;
    }

    public Bundle getSnoozeCriteria() {
        return this.mSnoozeCriteria;
    }

    public Bundle getShowBadge() {
        return this.mShowBadge;
    }
}
