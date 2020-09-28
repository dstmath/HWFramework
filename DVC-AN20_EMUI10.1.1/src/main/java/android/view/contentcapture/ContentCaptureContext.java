package android.view.contentcapture;

import android.annotation.SystemApi;
import android.content.ComponentName;
import android.content.LocusId;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class ContentCaptureContext implements Parcelable {
    public static final Parcelable.Creator<ContentCaptureContext> CREATOR = new Parcelable.Creator<ContentCaptureContext>() {
        /* class android.view.contentcapture.ContentCaptureContext.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ContentCaptureContext createFromParcel(Parcel parcel) {
            ContentCaptureContext clientContext;
            boolean hasClientContext = true;
            if (parcel.readInt() != 1) {
                hasClientContext = false;
            }
            if (hasClientContext) {
                Bundle extras = parcel.readBundle();
                Builder builder = new Builder((LocusId) parcel.readParcelable(null));
                if (extras != null) {
                    builder.setExtras(extras);
                }
                clientContext = new ContentCaptureContext(builder);
            } else {
                clientContext = null;
            }
            ComponentName componentName = (ComponentName) parcel.readParcelable(null);
            if (componentName == null) {
                return clientContext;
            }
            return new ContentCaptureContext(clientContext, componentName, parcel.readInt(), parcel.readInt(), parcel.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public ContentCaptureContext[] newArray(int size) {
            return new ContentCaptureContext[size];
        }
    };
    @SystemApi
    public static final int FLAG_DISABLED_BY_APP = 1;
    @SystemApi
    public static final int FLAG_DISABLED_BY_FLAG_SECURE = 2;
    @SystemApi
    public static final int FLAG_RECONNECTED = 4;
    private final ComponentName mComponentName;
    private final int mDisplayId;
    private final Bundle mExtras;
    private final int mFlags;
    private final boolean mHasClientContext;
    private final LocusId mId;
    private int mParentSessionId;
    private final int mTaskId;

    @Retention(RetentionPolicy.SOURCE)
    @interface ContextCreationFlags {
    }

    public ContentCaptureContext(ContentCaptureContext clientContext, ComponentName componentName, int taskId, int displayId, int flags) {
        this.mParentSessionId = 0;
        if (clientContext != null) {
            this.mHasClientContext = true;
            this.mExtras = clientContext.mExtras;
            this.mId = clientContext.mId;
        } else {
            this.mHasClientContext = false;
            this.mExtras = null;
            this.mId = null;
        }
        this.mComponentName = (ComponentName) Preconditions.checkNotNull(componentName);
        this.mTaskId = taskId;
        this.mDisplayId = displayId;
        this.mFlags = flags;
    }

    private ContentCaptureContext(Builder builder) {
        this.mParentSessionId = 0;
        this.mHasClientContext = true;
        this.mExtras = builder.mExtras;
        this.mId = builder.mId;
        this.mComponentName = null;
        this.mFlags = 0;
        this.mTaskId = 0;
        this.mDisplayId = -1;
    }

    public ContentCaptureContext(ContentCaptureContext original, int extraFlags) {
        this.mParentSessionId = 0;
        this.mHasClientContext = original.mHasClientContext;
        this.mExtras = original.mExtras;
        this.mId = original.mId;
        this.mComponentName = original.mComponentName;
        this.mTaskId = original.mTaskId;
        this.mFlags = original.mFlags | extraFlags;
        this.mDisplayId = original.mDisplayId;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public LocusId getLocusId() {
        return this.mId;
    }

    @SystemApi
    public int getTaskId() {
        return this.mTaskId;
    }

    @SystemApi
    public ComponentName getActivityComponent() {
        return this.mComponentName;
    }

    @SystemApi
    public ContentCaptureSessionId getParentSessionId() {
        int i = this.mParentSessionId;
        if (i == 0) {
            return null;
        }
        return new ContentCaptureSessionId(i);
    }

    public void setParentSessionId(int parentSessionId) {
        this.mParentSessionId = parentSessionId;
    }

    @SystemApi
    public int getDisplayId() {
        return this.mDisplayId;
    }

    @SystemApi
    public int getFlags() {
        return this.mFlags;
    }

    public static ContentCaptureContext forLocusId(String id) {
        return new Builder(new LocusId(id)).build();
    }

    public static final class Builder {
        private boolean mDestroyed;
        private Bundle mExtras;
        private final LocusId mId;

        public Builder(LocusId id) {
            this.mId = (LocusId) Preconditions.checkNotNull(id);
        }

        public Builder setExtras(Bundle extras) {
            this.mExtras = (Bundle) Preconditions.checkNotNull(extras);
            throwIfDestroyed();
            return this;
        }

        public ContentCaptureContext build() {
            throwIfDestroyed();
            this.mDestroyed = true;
            return new ContentCaptureContext(this);
        }

        private void throwIfDestroyed() {
            Preconditions.checkState(!this.mDestroyed, "Already called #build()");
        }
    }

    public void dump(PrintWriter pw) {
        if (this.mComponentName != null) {
            pw.print("activity=");
            pw.print(this.mComponentName.flattenToShortString());
        }
        if (this.mId != null) {
            pw.print(", id=");
            this.mId.dump(pw);
        }
        pw.print(", taskId=");
        pw.print(this.mTaskId);
        pw.print(", displayId=");
        pw.print(this.mDisplayId);
        if (this.mParentSessionId != 0) {
            pw.print(", parentId=");
            pw.print(this.mParentSessionId);
        }
        if (this.mFlags > 0) {
            pw.print(", flags=");
            pw.print(this.mFlags);
        }
        if (this.mExtras != null) {
            pw.print(", hasExtras");
        }
    }

    private boolean fromServer() {
        return this.mComponentName != null;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("Context[");
        if (fromServer()) {
            builder.append("act=");
            builder.append(ComponentName.flattenToShortString(this.mComponentName));
            builder.append(", taskId=");
            builder.append(this.mTaskId);
            builder.append(", displayId=");
            builder.append(this.mDisplayId);
            builder.append(", flags=");
            builder.append(this.mFlags);
        } else {
            builder.append("id=");
            builder.append(this.mId);
            if (this.mExtras != null) {
                builder.append(", hasExtras");
            }
        }
        if (this.mParentSessionId != 0) {
            builder.append(", parentId=");
            builder.append(this.mParentSessionId);
        }
        builder.append(']');
        return builder.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mHasClientContext ? 1 : 0);
        if (this.mHasClientContext) {
            parcel.writeParcelable(this.mId, flags);
            parcel.writeBundle(this.mExtras);
        }
        parcel.writeParcelable(this.mComponentName, flags);
        if (fromServer()) {
            parcel.writeInt(this.mTaskId);
            parcel.writeInt(this.mDisplayId);
            parcel.writeInt(this.mFlags);
        }
    }
}
