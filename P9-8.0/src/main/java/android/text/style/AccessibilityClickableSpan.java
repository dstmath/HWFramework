package android.text.style;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.text.ParcelableSpan;
import android.text.Spanned;
import android.view.View;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.R;

public class AccessibilityClickableSpan extends ClickableSpan implements ParcelableSpan {
    public static final Creator<AccessibilityClickableSpan> CREATOR = new Creator<AccessibilityClickableSpan>() {
        public AccessibilityClickableSpan createFromParcel(Parcel parcel) {
            return new AccessibilityClickableSpan(parcel);
        }

        public AccessibilityClickableSpan[] newArray(int size) {
            return new AccessibilityClickableSpan[size];
        }
    };
    private int mConnectionId = -1;
    private final int mOriginalClickableSpanId;
    private long mSourceNodeId = AccessibilityNodeInfo.UNDEFINED_NODE_ID;
    private int mWindowId = -1;

    public AccessibilityClickableSpan(int originalClickableSpanId) {
        this.mOriginalClickableSpanId = originalClickableSpanId;
    }

    public AccessibilityClickableSpan(Parcel p) {
        this.mOriginalClickableSpanId = p.readInt();
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 25;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeInt(this.mOriginalClickableSpanId);
    }

    public ClickableSpan findClickableSpan(CharSequence text) {
        if (!(text instanceof Spanned)) {
            return null;
        }
        ClickableSpan[] os = (ClickableSpan[]) ((Spanned) text).getSpans(0, text.length(), ClickableSpan.class);
        for (int i = 0; i < os.length; i++) {
            if (os[i].getId() == this.mOriginalClickableSpanId) {
                return os[i];
            }
        }
        return null;
    }

    public void copyConnectionDataFrom(AccessibilityNodeInfo accessibilityNodeInfo) {
        this.mConnectionId = accessibilityNodeInfo.getConnectionId();
        this.mWindowId = accessibilityNodeInfo.getWindowId();
        this.mSourceNodeId = accessibilityNodeInfo.getSourceNodeId();
    }

    public void onClick(View unused) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(AccessibilityNodeInfo.ACTION_ARGUMENT_ACCESSIBLE_CLICKABLE_SPAN, this);
        if (this.mWindowId == -1 || this.mSourceNodeId == AccessibilityNodeInfo.UNDEFINED_NODE_ID || this.mConnectionId == -1) {
            throw new RuntimeException("ClickableSpan for accessibility service not properly initialized");
        }
        AccessibilityInteractionClient.getInstance().performAccessibilityAction(this.mConnectionId, this.mWindowId, this.mSourceNodeId, R.id.accessibilityActionClickOnClickableSpan, arguments);
    }
}
