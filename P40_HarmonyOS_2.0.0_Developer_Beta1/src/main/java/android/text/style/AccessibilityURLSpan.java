package android.text.style;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

public class AccessibilityURLSpan extends URLSpan implements Parcelable {
    final AccessibilityClickableSpan mAccessibilityClickableSpan;

    public AccessibilityURLSpan(URLSpan spanToReplace) {
        super(spanToReplace.getURL());
        this.mAccessibilityClickableSpan = new AccessibilityClickableSpan(spanToReplace.getId());
    }

    public AccessibilityURLSpan(Parcel p) {
        super(p);
        this.mAccessibilityClickableSpan = new AccessibilityClickableSpan(p);
    }

    @Override // android.text.style.URLSpan, android.text.ParcelableSpan
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    @Override // android.text.style.URLSpan, android.text.ParcelableSpan
    public int getSpanTypeIdInternal() {
        return 26;
    }

    @Override // android.text.style.URLSpan, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    @Override // android.text.style.URLSpan, android.text.ParcelableSpan
    public void writeToParcelInternal(Parcel dest, int flags) {
        super.writeToParcelInternal(dest, flags);
        this.mAccessibilityClickableSpan.writeToParcel(dest, flags);
    }

    @Override // android.text.style.URLSpan, android.text.style.ClickableSpan
    public void onClick(View unused) {
        this.mAccessibilityClickableSpan.onClick(unused);
    }

    public void copyConnectionDataFrom(AccessibilityNodeInfo accessibilityNodeInfo) {
        this.mAccessibilityClickableSpan.copyConnectionDataFrom(accessibilityNodeInfo);
    }
}
