package android.view.contentcapture;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.autofill.AutofillId;
import com.android.internal.util.Preconditions;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

@SystemApi
public final class ContentCaptureEvent implements Parcelable {
    public static final Parcelable.Creator<ContentCaptureEvent> CREATOR = new Parcelable.Creator<ContentCaptureEvent>() {
        /* class android.view.contentcapture.ContentCaptureEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ContentCaptureEvent createFromParcel(Parcel parcel) {
            int sessionId = parcel.readInt();
            int type = parcel.readInt();
            ContentCaptureEvent event = new ContentCaptureEvent(sessionId, type, parcel.readLong());
            AutofillId id = (AutofillId) parcel.readParcelable(null);
            if (id != null) {
                event.setAutofillId(id);
            }
            ArrayList<AutofillId> ids = parcel.createTypedArrayList(AutofillId.CREATOR);
            if (ids != null) {
                event.setAutofillIds(ids);
            }
            ViewNode node = ViewNode.readFromParcel(parcel);
            if (node != null) {
                event.setViewNode(node);
            }
            event.setText(parcel.readCharSequence());
            if (type == -1 || type == -2) {
                event.setParentSessionId(parcel.readInt());
            }
            if (type == -1 || type == 6) {
                event.setClientContext((ContentCaptureContext) parcel.readParcelable(null));
            }
            return event;
        }

        @Override // android.os.Parcelable.Creator
        public ContentCaptureEvent[] newArray(int size) {
            return new ContentCaptureEvent[size];
        }
    };
    private static final String TAG = ContentCaptureEvent.class.getSimpleName();
    public static final int TYPE_CONTEXT_UPDATED = 6;
    public static final int TYPE_SESSION_FINISHED = -2;
    public static final int TYPE_SESSION_PAUSED = 8;
    public static final int TYPE_SESSION_RESUMED = 7;
    public static final int TYPE_SESSION_STARTED = -1;
    public static final int TYPE_VIEW_APPEARED = 1;
    public static final int TYPE_VIEW_DISAPPEARED = 2;
    public static final int TYPE_VIEW_TEXT_CHANGED = 3;
    public static final int TYPE_VIEW_TREE_APPEARED = 5;
    public static final int TYPE_VIEW_TREE_APPEARING = 4;
    private ContentCaptureContext mClientContext;
    private final long mEventTime;
    private AutofillId mId;
    private ArrayList<AutofillId> mIds;
    private ViewNode mNode;
    private int mParentSessionId;
    private final int mSessionId;
    private CharSequence mText;
    private final int mType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
    }

    public ContentCaptureEvent(int sessionId, int type, long eventTime) {
        this.mParentSessionId = 0;
        this.mSessionId = sessionId;
        this.mType = type;
        this.mEventTime = eventTime;
    }

    public ContentCaptureEvent(int sessionId, int type) {
        this(sessionId, type, System.currentTimeMillis());
    }

    public ContentCaptureEvent setAutofillId(AutofillId id) {
        this.mId = (AutofillId) Preconditions.checkNotNull(id);
        return this;
    }

    public ContentCaptureEvent setAutofillIds(ArrayList<AutofillId> ids) {
        this.mIds = (ArrayList) Preconditions.checkNotNull(ids);
        return this;
    }

    public ContentCaptureEvent addAutofillId(AutofillId id) {
        Preconditions.checkNotNull(id);
        if (this.mIds == null) {
            this.mIds = new ArrayList<>();
            AutofillId autofillId = this.mId;
            if (autofillId == null) {
                String str = TAG;
                Log.w(str, "addAutofillId(" + id + ") called without an initial id");
            } else {
                this.mIds.add(autofillId);
                this.mId = null;
            }
        }
        this.mIds.add(id);
        return this;
    }

    public ContentCaptureEvent setParentSessionId(int parentSessionId) {
        this.mParentSessionId = parentSessionId;
        return this;
    }

    public ContentCaptureEvent setClientContext(ContentCaptureContext clientContext) {
        this.mClientContext = clientContext;
        return this;
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public int getParentSessionId() {
        return this.mParentSessionId;
    }

    public ContentCaptureContext getContentCaptureContext() {
        return this.mClientContext;
    }

    public ContentCaptureEvent setViewNode(ViewNode node) {
        this.mNode = (ViewNode) Preconditions.checkNotNull(node);
        return this;
    }

    public ContentCaptureEvent setText(CharSequence text) {
        this.mText = text;
        return this;
    }

    public int getType() {
        return this.mType;
    }

    public long getEventTime() {
        return this.mEventTime;
    }

    public ViewNode getViewNode() {
        return this.mNode;
    }

    public AutofillId getId() {
        return this.mId;
    }

    public List<AutofillId> getIds() {
        return this.mIds;
    }

    public CharSequence getText() {
        return this.mText;
    }

    public void mergeEvent(ContentCaptureEvent event) {
        Preconditions.checkNotNull(event);
        int eventType = event.getType();
        if (this.mType != eventType) {
            String str = TAG;
            Log.e(str, "mergeEvent(" + getTypeAsString(eventType) + ") cannot be merged with different eventType=" + getTypeAsString(this.mType));
        } else if (eventType == 2) {
            List<AutofillId> ids = event.getIds();
            AutofillId id = event.getId();
            if (ids != null) {
                if (id != null) {
                    String str2 = TAG;
                    Log.w(str2, "got TYPE_VIEW_DISAPPEARED event with both id and ids: " + event);
                }
                for (int i = 0; i < ids.size(); i++) {
                    addAutofillId(ids.get(i));
                }
            } else if (id != null) {
                addAutofillId(id);
            } else {
                throw new IllegalArgumentException("mergeEvent(): got TYPE_VIEW_DISAPPEARED event with neither id or ids: " + event);
            }
        } else if (eventType == 3) {
            setText(event.getText());
        } else {
            String str3 = TAG;
            Log.e(str3, "mergeEvent(" + getTypeAsString(eventType) + ") does not support this event type.");
        }
    }

    public void dump(PrintWriter pw) {
        pw.print("type=");
        pw.print(getTypeAsString(this.mType));
        pw.print(", time=");
        pw.print(this.mEventTime);
        if (this.mId != null) {
            pw.print(", id=");
            pw.print(this.mId);
        }
        if (this.mIds != null) {
            pw.print(", ids=");
            pw.print(this.mIds);
        }
        if (this.mNode != null) {
            pw.print(", mNode.id=");
            pw.print(this.mNode.getAutofillId());
        }
        if (this.mSessionId != 0) {
            pw.print(", sessionId=");
            pw.print(this.mSessionId);
        }
        if (this.mParentSessionId != 0) {
            pw.print(", parentSessionId=");
            pw.print(this.mParentSessionId);
        }
        if (this.mText != null) {
            pw.print(", text=");
            pw.println(ContentCaptureHelper.getSanitizedString(this.mText));
        }
        if (this.mClientContext != null) {
            pw.print(", context=");
            this.mClientContext.dump(pw);
            pw.println();
        }
    }

    public String toString() {
        StringBuilder string = new StringBuilder("ContentCaptureEvent[type=").append(getTypeAsString(this.mType));
        string.append(", session=");
        string.append(this.mSessionId);
        if (this.mType == -1 && this.mParentSessionId != 0) {
            string.append(", parent=");
            string.append(this.mParentSessionId);
        }
        if (this.mId != null) {
            string.append(", id=");
            string.append(this.mId);
        }
        if (this.mIds != null) {
            string.append(", ids=");
            string.append(this.mIds);
        }
        ViewNode viewNode = this.mNode;
        if (viewNode != null) {
            String className = viewNode.getClassName();
            if (this.mNode != null) {
                string.append(", class=");
                string.append(className);
            }
            string.append(", id=");
            string.append(this.mNode.getAutofillId());
        }
        if (this.mText != null) {
            string.append(", text=");
            string.append(ContentCaptureHelper.getSanitizedString(this.mText));
        }
        if (this.mClientContext != null) {
            string.append(", context=");
            string.append(this.mClientContext);
        }
        string.append(']');
        return string.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mSessionId);
        parcel.writeInt(this.mType);
        parcel.writeLong(this.mEventTime);
        parcel.writeParcelable(this.mId, flags);
        parcel.writeTypedList(this.mIds);
        ViewNode.writeToParcel(parcel, this.mNode, flags);
        parcel.writeCharSequence(this.mText);
        int i = this.mType;
        if (i == -1 || i == -2) {
            parcel.writeInt(this.mParentSessionId);
        }
        int i2 = this.mType;
        if (i2 == -1 || i2 == 6) {
            parcel.writeParcelable(this.mClientContext, flags);
        }
    }

    public static String getTypeAsString(int type) {
        switch (type) {
            case -2:
                return "SESSION_FINISHED";
            case -1:
                return "SESSION_STARTED";
            case 0:
            default:
                return "UKNOWN_TYPE: " + type;
            case 1:
                return "VIEW_APPEARED";
            case 2:
                return "VIEW_DISAPPEARED";
            case 3:
                return "VIEW_TEXT_CHANGED";
            case 4:
                return "VIEW_TREE_APPEARING";
            case 5:
                return "VIEW_TREE_APPEARED";
            case 6:
                return "CONTEXT_UPDATED";
            case 7:
                return "SESSION_RESUMED";
            case 8:
                return "SESSION_PAUSED";
        }
    }
}
