package android.view.textclassifier;

import android.app.RemoteAction;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class ConversationAction implements Parcelable {
    public static final Parcelable.Creator<ConversationAction> CREATOR = new Parcelable.Creator<ConversationAction>() {
        /* class android.view.textclassifier.ConversationAction.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConversationAction createFromParcel(Parcel in) {
            return new ConversationAction(in);
        }

        @Override // android.os.Parcelable.Creator
        public ConversationAction[] newArray(int size) {
            return new ConversationAction[size];
        }
    };
    public static final String TYPE_ADD_CONTACT = "add_contact";
    public static final String TYPE_CALL_PHONE = "call_phone";
    public static final String TYPE_COPY = "copy";
    public static final String TYPE_CREATE_REMINDER = "create_reminder";
    public static final String TYPE_OPEN_URL = "open_url";
    public static final String TYPE_SEND_EMAIL = "send_email";
    public static final String TYPE_SEND_SMS = "send_sms";
    public static final String TYPE_SHARE_LOCATION = "share_location";
    public static final String TYPE_TEXT_REPLY = "text_reply";
    public static final String TYPE_TRACK_FLIGHT = "track_flight";
    public static final String TYPE_VIEW_CALENDAR = "view_calendar";
    public static final String TYPE_VIEW_MAP = "view_map";
    private final RemoteAction mAction;
    private final Bundle mExtras;
    private final float mScore;
    private final CharSequence mTextReply;
    private final String mType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {
    }

    private ConversationAction(String type, RemoteAction action, CharSequence textReply, float score, Bundle extras) {
        this.mType = (String) Preconditions.checkNotNull(type);
        this.mAction = action;
        this.mTextReply = textReply;
        this.mScore = score;
        this.mExtras = (Bundle) Preconditions.checkNotNull(extras);
    }

    private ConversationAction(Parcel in) {
        this.mType = in.readString();
        this.mAction = (RemoteAction) in.readParcelable(null);
        this.mTextReply = in.readCharSequence();
        this.mScore = in.readFloat();
        this.mExtras = in.readBundle();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mType);
        parcel.writeParcelable(this.mAction, flags);
        parcel.writeCharSequence(this.mTextReply);
        parcel.writeFloat(this.mScore);
        parcel.writeBundle(this.mExtras);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getType() {
        return this.mType;
    }

    public RemoteAction getAction() {
        return this.mAction;
    }

    public float getConfidenceScore() {
        return this.mScore;
    }

    public CharSequence getTextReply() {
        return this.mTextReply;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public static final class Builder {
        private RemoteAction mAction;
        private Bundle mExtras;
        private float mScore;
        private CharSequence mTextReply;
        private String mType;

        public Builder(String actionType) {
            this.mType = (String) Preconditions.checkNotNull(actionType);
        }

        public Builder setAction(RemoteAction action) {
            this.mAction = action;
            return this;
        }

        public Builder setTextReply(CharSequence textReply) {
            this.mTextReply = textReply;
            return this;
        }

        public Builder setConfidenceScore(float score) {
            this.mScore = score;
            return this;
        }

        public Builder setExtras(Bundle extras) {
            this.mExtras = extras;
            return this;
        }

        public ConversationAction build() {
            String str = this.mType;
            RemoteAction remoteAction = this.mAction;
            CharSequence charSequence = this.mTextReply;
            float f = this.mScore;
            Bundle bundle = this.mExtras;
            if (bundle == null) {
                bundle = Bundle.EMPTY;
            }
            return new ConversationAction(str, remoteAction, charSequence, f, bundle);
        }
    }
}
