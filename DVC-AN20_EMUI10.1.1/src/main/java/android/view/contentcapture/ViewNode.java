package android.view.contentcapture;

import android.annotation.SystemApi;
import android.app.assist.AssistStructure;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewStructure;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import com.android.internal.util.Preconditions;

@SystemApi
public final class ViewNode extends AssistStructure.ViewNode {
    private static final long FLAGS_ACCESSIBILITY_FOCUSED = 131072;
    private static final long FLAGS_ACTIVATED = 2097152;
    private static final long FLAGS_ASSIST_BLOCKED = 1024;
    private static final long FLAGS_CHECKABLE = 262144;
    private static final long FLAGS_CHECKED = 524288;
    private static final long FLAGS_CLICKABLE = 4096;
    private static final long FLAGS_CONTEXT_CLICKABLE = 16384;
    private static final long FLAGS_DISABLED = 2048;
    private static final long FLAGS_FOCUSABLE = 32768;
    private static final long FLAGS_FOCUSED = 65536;
    private static final long FLAGS_HAS_AUTOFILL_HINTS = 8589934592L;
    private static final long FLAGS_HAS_AUTOFILL_ID = 32;
    private static final long FLAGS_HAS_AUTOFILL_OPTIONS = 17179869184L;
    private static final long FLAGS_HAS_AUTOFILL_PARENT_ID = 64;
    private static final long FLAGS_HAS_AUTOFILL_TYPE = 2147483648L;
    private static final long FLAGS_HAS_AUTOFILL_VALUE = 4294967296L;
    private static final long FLAGS_HAS_CLASSNAME = 16;
    private static final long FLAGS_HAS_COMPLEX_TEXT = 2;
    private static final long FLAGS_HAS_CONTENT_DESCRIPTION = 8388608;
    private static final long FLAGS_HAS_EXTRAS = 16777216;
    private static final long FLAGS_HAS_ID = 128;
    private static final long FLAGS_HAS_INPUT_TYPE = 67108864;
    private static final long FLAGS_HAS_LARGE_COORDS = 256;
    private static final long FLAGS_HAS_LOCALE_LIST = 33554432;
    private static final long FLAGS_HAS_MAX_TEXT_EMS = 268435456;
    private static final long FLAGS_HAS_MAX_TEXT_LENGTH = 536870912;
    private static final long FLAGS_HAS_MIN_TEXT_EMS = 134217728;
    private static final long FLAGS_HAS_SCROLL = 512;
    private static final long FLAGS_HAS_TEXT = 1;
    private static final long FLAGS_HAS_TEXT_ID_ENTRY = 1073741824;
    private static final long FLAGS_LONG_CLICKABLE = 8192;
    private static final long FLAGS_OPAQUE = 4194304;
    private static final long FLAGS_SELECTED = 1048576;
    private static final long FLAGS_VISIBILITY_MASK = 12;
    private static final String TAG = ViewNode.class.getSimpleName();
    private String[] mAutofillHints;
    private AutofillId mAutofillId;
    private CharSequence[] mAutofillOptions;
    private int mAutofillType;
    private AutofillValue mAutofillValue;
    private String mClassName;
    private CharSequence mContentDescription;
    private Bundle mExtras;
    private long mFlags;
    private int mHeight;
    private int mId;
    private String mIdEntry;
    private String mIdPackage;
    private String mIdType;
    private int mInputType;
    private LocaleList mLocaleList;
    private int mMaxEms;
    private int mMaxLength;
    private int mMinEms;
    private AutofillId mParentAutofillId;
    private int mScrollX;
    private int mScrollY;
    private ViewNodeText mText;
    private String mTextIdEntry;
    private int mWidth;
    private int mX;
    private int mY;

    public ViewNode() {
        this.mId = -1;
        this.mMinEms = -1;
        this.mMaxEms = -1;
        this.mMaxLength = -1;
        this.mAutofillType = 0;
    }

    private ViewNode(long nodeFlags, Parcel parcel) {
        this.mId = -1;
        this.mMinEms = -1;
        this.mMaxEms = -1;
        this.mMaxLength = -1;
        boolean z = false;
        this.mAutofillType = 0;
        this.mFlags = nodeFlags;
        if ((32 & nodeFlags) != 0) {
            this.mAutofillId = (AutofillId) parcel.readParcelable(null);
        }
        if ((64 & nodeFlags) != 0) {
            this.mParentAutofillId = (AutofillId) parcel.readParcelable(null);
        }
        if ((1 & nodeFlags) != 0) {
            this.mText = new ViewNodeText(parcel, (2 & nodeFlags) == 0 ? true : z);
        }
        if ((16 & nodeFlags) != 0) {
            this.mClassName = parcel.readString();
        }
        if ((128 & nodeFlags) != 0) {
            this.mId = parcel.readInt();
            if (this.mId != -1) {
                this.mIdEntry = parcel.readString();
                if (this.mIdEntry != null) {
                    this.mIdType = parcel.readString();
                    this.mIdPackage = parcel.readString();
                }
            }
        }
        if ((256 & nodeFlags) != 0) {
            this.mX = parcel.readInt();
            this.mY = parcel.readInt();
            this.mWidth = parcel.readInt();
            this.mHeight = parcel.readInt();
        } else {
            int val = parcel.readInt();
            this.mX = val & 32767;
            this.mY = (val >> 16) & 32767;
            int val2 = parcel.readInt();
            this.mWidth = val2 & 32767;
            this.mHeight = (val2 >> 16) & 32767;
        }
        if ((512 & nodeFlags) != 0) {
            this.mScrollX = parcel.readInt();
            this.mScrollY = parcel.readInt();
        }
        if ((8388608 & nodeFlags) != 0) {
            this.mContentDescription = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        }
        if ((16777216 & nodeFlags) != 0) {
            this.mExtras = parcel.readBundle();
        }
        if ((33554432 & nodeFlags) != 0) {
            this.mLocaleList = (LocaleList) parcel.readParcelable(null);
        }
        if ((67108864 & nodeFlags) != 0) {
            this.mInputType = parcel.readInt();
        }
        if ((FLAGS_HAS_MIN_TEXT_EMS & nodeFlags) != 0) {
            this.mMinEms = parcel.readInt();
        }
        if ((FLAGS_HAS_MAX_TEXT_EMS & nodeFlags) != 0) {
            this.mMaxEms = parcel.readInt();
        }
        if ((FLAGS_HAS_MAX_TEXT_LENGTH & nodeFlags) != 0) {
            this.mMaxLength = parcel.readInt();
        }
        if ((1073741824 & nodeFlags) != 0) {
            this.mTextIdEntry = parcel.readString();
        }
        if ((FLAGS_HAS_AUTOFILL_TYPE & nodeFlags) != 0) {
            this.mAutofillType = parcel.readInt();
        }
        if ((8589934592L & nodeFlags) != 0) {
            this.mAutofillHints = parcel.readStringArray();
        }
        if ((4294967296L & nodeFlags) != 0) {
            this.mAutofillValue = (AutofillValue) parcel.readParcelable(null);
        }
        if ((17179869184L & nodeFlags) != 0) {
            this.mAutofillOptions = parcel.readCharSequenceArray();
        }
    }

    public AutofillId getParentAutofillId() {
        return this.mParentAutofillId;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public AutofillId getAutofillId() {
        return this.mAutofillId;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public CharSequence getText() {
        ViewNodeText viewNodeText = this.mText;
        if (viewNodeText != null) {
            return viewNodeText.mText;
        }
        return null;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public String getClassName() {
        return this.mClassName;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getId() {
        return this.mId;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public String getIdPackage() {
        return this.mIdPackage;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public String getIdType() {
        return this.mIdType;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public String getIdEntry() {
        return this.mIdEntry;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getLeft() {
        return this.mX;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getTop() {
        return this.mY;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getScrollX() {
        return this.mScrollX;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getScrollY() {
        return this.mScrollY;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getWidth() {
        return this.mWidth;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getHeight() {
        return this.mHeight;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isAssistBlocked() {
        return (this.mFlags & 1024) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isEnabled() {
        return (this.mFlags & 2048) == 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isClickable() {
        return (this.mFlags & 4096) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isLongClickable() {
        return (this.mFlags & 8192) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isContextClickable() {
        return (this.mFlags & 16384) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isFocusable() {
        return (this.mFlags & 32768) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isFocused() {
        return (this.mFlags & 65536) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isAccessibilityFocused() {
        return (this.mFlags & 131072) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isCheckable() {
        return (this.mFlags & 262144) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isChecked() {
        return (this.mFlags & 524288) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isSelected() {
        return (this.mFlags & 1048576) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isActivated() {
        return (this.mFlags & 2097152) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public boolean isOpaque() {
        return (this.mFlags & 4194304) != 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public Bundle getExtras() {
        return this.mExtras;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public String getHint() {
        ViewNodeText viewNodeText = this.mText;
        if (viewNodeText != null) {
            return viewNodeText.mHint;
        }
        return null;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getTextSelectionStart() {
        ViewNodeText viewNodeText = this.mText;
        if (viewNodeText != null) {
            return viewNodeText.mTextSelectionStart;
        }
        return -1;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getTextSelectionEnd() {
        ViewNodeText viewNodeText = this.mText;
        if (viewNodeText != null) {
            return viewNodeText.mTextSelectionEnd;
        }
        return -1;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getTextColor() {
        ViewNodeText viewNodeText = this.mText;
        if (viewNodeText != null) {
            return viewNodeText.mTextColor;
        }
        return 1;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getTextBackgroundColor() {
        ViewNodeText viewNodeText = this.mText;
        if (viewNodeText != null) {
            return viewNodeText.mTextBackgroundColor;
        }
        return 1;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public float getTextSize() {
        ViewNodeText viewNodeText = this.mText;
        if (viewNodeText != null) {
            return viewNodeText.mTextSize;
        }
        return 0.0f;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getTextStyle() {
        ViewNodeText viewNodeText = this.mText;
        if (viewNodeText != null) {
            return viewNodeText.mTextStyle;
        }
        return 0;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int[] getTextLineCharOffsets() {
        ViewNodeText viewNodeText = this.mText;
        if (viewNodeText != null) {
            return viewNodeText.mLineCharOffsets;
        }
        return null;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int[] getTextLineBaselines() {
        ViewNodeText viewNodeText = this.mText;
        if (viewNodeText != null) {
            return viewNodeText.mLineBaselines;
        }
        return null;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getVisibility() {
        return (int) (this.mFlags & FLAGS_VISIBILITY_MASK);
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getInputType() {
        return this.mInputType;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getMinTextEms() {
        return this.mMinEms;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getMaxTextEms() {
        return this.mMaxEms;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getMaxTextLength() {
        return this.mMaxLength;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public String getTextIdEntry() {
        return this.mTextIdEntry;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public int getAutofillType() {
        return this.mAutofillType;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public String[] getAutofillHints() {
        return this.mAutofillHints;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public AutofillValue getAutofillValue() {
        return this.mAutofillValue;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public CharSequence[] getAutofillOptions() {
        return this.mAutofillOptions;
    }

    @Override // android.app.assist.AssistStructure.ViewNode
    public LocaleList getLocaleList() {
        return this.mLocaleList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0057, code lost:
        if ((((r28.mWidth & -32768) != 0) | ((r28.mHeight & -32768) != 0)) != false) goto L_0x0059;
     */
    private void writeSelfToParcel(Parcel parcel, int parcelFlags) {
        long nodeFlags = this.mFlags;
        if (this.mAutofillId != null) {
            nodeFlags |= 32;
        }
        if (this.mParentAutofillId != null) {
            nodeFlags |= 64;
        }
        ViewNodeText viewNodeText = this.mText;
        if (viewNodeText != null) {
            nodeFlags |= 1;
            if (!viewNodeText.isSimple()) {
                nodeFlags |= 2;
            }
        }
        if (this.mClassName != null) {
            nodeFlags |= 16;
        }
        if (this.mId != -1) {
            nodeFlags |= 128;
        }
        if ((this.mX & -32768) == 0 && (this.mY & -32768) == 0) {
        }
        nodeFlags |= 256;
        if (!(this.mScrollX == 0 && this.mScrollY == 0)) {
            nodeFlags |= 512;
        }
        if (this.mContentDescription != null) {
            nodeFlags |= 8388608;
        }
        if (this.mExtras != null) {
            nodeFlags |= 16777216;
        }
        if (this.mLocaleList != null) {
            nodeFlags |= 33554432;
        }
        if (this.mInputType != 0) {
            nodeFlags |= 67108864;
        }
        if (this.mMinEms > -1) {
            nodeFlags |= FLAGS_HAS_MIN_TEXT_EMS;
        }
        if (this.mMaxEms > -1) {
            nodeFlags |= FLAGS_HAS_MAX_TEXT_EMS;
        }
        if (this.mMaxLength > -1) {
            nodeFlags |= FLAGS_HAS_MAX_TEXT_LENGTH;
        }
        if (this.mTextIdEntry != null) {
            nodeFlags |= 1073741824;
        }
        if (this.mAutofillValue != null) {
            nodeFlags |= 4294967296L;
        }
        if (this.mAutofillType != 0) {
            nodeFlags |= FLAGS_HAS_AUTOFILL_TYPE;
        }
        if (this.mAutofillHints != null) {
            nodeFlags |= 8589934592L;
        }
        if (this.mAutofillOptions != null) {
            nodeFlags |= 17179869184L;
        }
        parcel.writeLong(nodeFlags);
        if ((nodeFlags & 32) != 0) {
            parcel.writeParcelable(this.mAutofillId, parcelFlags);
        }
        if ((nodeFlags & 64) != 0) {
            parcel.writeParcelable(this.mParentAutofillId, parcelFlags);
        }
        if ((nodeFlags & 1) != 0) {
            this.mText.writeToParcel(parcel, (nodeFlags & 2) == 0);
        }
        if ((16 & nodeFlags) != 0) {
            parcel.writeString(this.mClassName);
        }
        if ((nodeFlags & 128) != 0) {
            parcel.writeInt(this.mId);
            if (this.mId != -1) {
                parcel.writeString(this.mIdEntry);
                if (this.mIdEntry != null) {
                    parcel.writeString(this.mIdType);
                    parcel.writeString(this.mIdPackage);
                }
            }
        }
        if ((nodeFlags & 256) != 0) {
            parcel.writeInt(this.mX);
            parcel.writeInt(this.mY);
            parcel.writeInt(this.mWidth);
            parcel.writeInt(this.mHeight);
        } else {
            parcel.writeInt((this.mY << 16) | this.mX);
            parcel.writeInt((this.mHeight << 16) | this.mWidth);
        }
        if ((nodeFlags & 512) != 0) {
            parcel.writeInt(this.mScrollX);
            parcel.writeInt(this.mScrollY);
        }
        if ((nodeFlags & 8388608) != 0) {
            TextUtils.writeToParcel(this.mContentDescription, parcel, 0);
        }
        if ((nodeFlags & 16777216) != 0) {
            parcel.writeBundle(this.mExtras);
        }
        if ((33554432 & nodeFlags) != 0) {
            parcel.writeParcelable(this.mLocaleList, 0);
        }
        if ((67108864 & nodeFlags) != 0) {
            parcel.writeInt(this.mInputType);
        }
        if ((FLAGS_HAS_MIN_TEXT_EMS & nodeFlags) != 0) {
            parcel.writeInt(this.mMinEms);
        }
        if ((FLAGS_HAS_MAX_TEXT_EMS & nodeFlags) != 0) {
            parcel.writeInt(this.mMaxEms);
        }
        if ((FLAGS_HAS_MAX_TEXT_LENGTH & nodeFlags) != 0) {
            parcel.writeInt(this.mMaxLength);
        }
        if ((1073741824 & nodeFlags) != 0) {
            parcel.writeString(this.mTextIdEntry);
        }
        if ((FLAGS_HAS_AUTOFILL_TYPE & nodeFlags) != 0) {
            parcel.writeInt(this.mAutofillType);
        }
        if ((8589934592L & nodeFlags) != 0) {
            parcel.writeStringArray(this.mAutofillHints);
        }
        if ((4294967296L & nodeFlags) != 0) {
            parcel.writeParcelable(this.mAutofillValue, 0);
        }
        if ((17179869184L & nodeFlags) != 0) {
            parcel.writeCharSequenceArray(this.mAutofillOptions);
        }
    }

    public static void writeToParcel(Parcel parcel, ViewNode node, int flags) {
        if (node == null) {
            parcel.writeLong(0);
        } else {
            node.writeSelfToParcel(parcel, flags);
        }
    }

    public static ViewNode readFromParcel(Parcel parcel) {
        long nodeFlags = parcel.readLong();
        if (nodeFlags == 0) {
            return null;
        }
        return new ViewNode(nodeFlags, parcel);
    }

    public static final class ViewStructureImpl extends ViewStructure {
        final ViewNode mNode = new ViewNode();

        public ViewStructureImpl(View view) {
            this.mNode.mAutofillId = ((View) Preconditions.checkNotNull(view)).getAutofillId();
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                this.mNode.mParentAutofillId = ((View) parent).getAutofillId();
            }
        }

        public ViewStructureImpl(AutofillId parentId, long virtualId, int sessionId) {
            this.mNode.mParentAutofillId = (AutofillId) Preconditions.checkNotNull(parentId);
            this.mNode.mAutofillId = new AutofillId(parentId, virtualId, sessionId);
        }

        public ViewNode getNode() {
            return this.mNode;
        }

        @Override // android.view.ViewStructure
        public void setId(int id, String packageName, String typeName, String entryName) {
            this.mNode.mId = id;
            this.mNode.mIdPackage = packageName;
            this.mNode.mIdType = typeName;
            this.mNode.mIdEntry = entryName;
        }

        @Override // android.view.ViewStructure
        public void setDimens(int left, int top, int scrollX, int scrollY, int width, int height) {
            this.mNode.mX = left;
            this.mNode.mY = top;
            this.mNode.mScrollX = scrollX;
            this.mNode.mScrollY = scrollY;
            this.mNode.mWidth = width;
            this.mNode.mHeight = height;
        }

        @Override // android.view.ViewStructure
        public void setTransformation(Matrix matrix) {
            Log.w(ViewNode.TAG, "setTransformation() is not supported");
        }

        @Override // android.view.ViewStructure
        public void setElevation(float elevation) {
            Log.w(ViewNode.TAG, "setElevation() is not supported");
        }

        @Override // android.view.ViewStructure
        public void setAlpha(float alpha) {
            Log.w(ViewNode.TAG, "setAlpha() is not supported");
        }

        @Override // android.view.ViewStructure
        public void setVisibility(int visibility) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -13) | (((long) visibility) & ViewNode.FLAGS_VISIBILITY_MASK);
        }

        @Override // android.view.ViewStructure
        public void setAssistBlocked(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -1025) | (state ? 1024 : 0);
        }

        @Override // android.view.ViewStructure
        public void setEnabled(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -2049) | (state ? 0 : 2048);
        }

        @Override // android.view.ViewStructure
        public void setClickable(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -4097) | (state ? 4096 : 0);
        }

        @Override // android.view.ViewStructure
        public void setLongClickable(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -8193) | (state ? 8192 : 0);
        }

        @Override // android.view.ViewStructure
        public void setContextClickable(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -16385) | (state ? 16384 : 0);
        }

        @Override // android.view.ViewStructure
        public void setFocusable(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -32769) | (state ? 32768 : 0);
        }

        @Override // android.view.ViewStructure
        public void setFocused(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -65537) | (state ? 65536 : 0);
        }

        @Override // android.view.ViewStructure
        public void setAccessibilityFocused(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -131073) | (state ? 131072 : 0);
        }

        @Override // android.view.ViewStructure
        public void setCheckable(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -262145) | (state ? 262144 : 0);
        }

        @Override // android.view.ViewStructure
        public void setChecked(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -524289) | (state ? 524288 : 0);
        }

        @Override // android.view.ViewStructure
        public void setSelected(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -1048577) | (state ? 1048576 : 0);
        }

        @Override // android.view.ViewStructure
        public void setActivated(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -2097153) | (state ? 2097152 : 0);
        }

        @Override // android.view.ViewStructure
        public void setOpaque(boolean opaque) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -4194305) | (opaque ? 4194304 : 0);
        }

        @Override // android.view.ViewStructure
        public void setClassName(String className) {
            this.mNode.mClassName = className;
        }

        @Override // android.view.ViewStructure
        public void setContentDescription(CharSequence contentDescription) {
            this.mNode.mContentDescription = contentDescription;
        }

        @Override // android.view.ViewStructure
        public void setText(CharSequence text) {
            ViewNodeText t = getNodeText();
            t.mText = TextUtils.trimNoCopySpans(text);
            t.mTextSelectionEnd = -1;
            t.mTextSelectionStart = -1;
        }

        @Override // android.view.ViewStructure
        public void setText(CharSequence text, int selectionStart, int selectionEnd) {
            ViewNodeText t = getNodeText();
            t.mText = TextUtils.trimNoCopySpans(text);
            t.mTextSelectionStart = selectionStart;
            t.mTextSelectionEnd = selectionEnd;
        }

        @Override // android.view.ViewStructure
        public void setTextStyle(float size, int fgColor, int bgColor, int style) {
            ViewNodeText t = getNodeText();
            t.mTextColor = fgColor;
            t.mTextBackgroundColor = bgColor;
            t.mTextSize = size;
            t.mTextStyle = style;
        }

        @Override // android.view.ViewStructure
        public void setTextLines(int[] charOffsets, int[] baselines) {
            ViewNodeText t = getNodeText();
            t.mLineCharOffsets = charOffsets;
            t.mLineBaselines = baselines;
        }

        @Override // android.view.ViewStructure
        public void setTextIdEntry(String entryName) {
            this.mNode.mTextIdEntry = (String) Preconditions.checkNotNull(entryName);
        }

        @Override // android.view.ViewStructure
        public void setHint(CharSequence hint) {
            getNodeText().mHint = hint != null ? hint.toString() : null;
        }

        @Override // android.view.ViewStructure
        public CharSequence getText() {
            return this.mNode.getText();
        }

        @Override // android.view.ViewStructure
        public int getTextSelectionStart() {
            return this.mNode.getTextSelectionStart();
        }

        @Override // android.view.ViewStructure
        public int getTextSelectionEnd() {
            return this.mNode.getTextSelectionEnd();
        }

        @Override // android.view.ViewStructure
        public CharSequence getHint() {
            return this.mNode.getHint();
        }

        @Override // android.view.ViewStructure
        public Bundle getExtras() {
            if (this.mNode.mExtras != null) {
                return this.mNode.mExtras;
            }
            this.mNode.mExtras = new Bundle();
            return this.mNode.mExtras;
        }

        @Override // android.view.ViewStructure
        public boolean hasExtras() {
            return this.mNode.mExtras != null;
        }

        @Override // android.view.ViewStructure
        public void setChildCount(int num) {
            Log.w(ViewNode.TAG, "setChildCount() is not supported");
        }

        @Override // android.view.ViewStructure
        public int addChildCount(int num) {
            Log.w(ViewNode.TAG, "addChildCount() is not supported");
            return 0;
        }

        @Override // android.view.ViewStructure
        public int getChildCount() {
            Log.w(ViewNode.TAG, "getChildCount() is not supported");
            return 0;
        }

        @Override // android.view.ViewStructure
        public ViewStructure newChild(int index) {
            Log.w(ViewNode.TAG, "newChild() is not supported");
            return null;
        }

        @Override // android.view.ViewStructure
        public ViewStructure asyncNewChild(int index) {
            Log.w(ViewNode.TAG, "asyncNewChild() is not supported");
            return null;
        }

        @Override // android.view.ViewStructure
        public AutofillId getAutofillId() {
            return this.mNode.mAutofillId;
        }

        @Override // android.view.ViewStructure
        public void setAutofillId(AutofillId id) {
            this.mNode.mAutofillId = (AutofillId) Preconditions.checkNotNull(id);
        }

        @Override // android.view.ViewStructure
        public void setAutofillId(AutofillId parentId, int virtualId) {
            this.mNode.mParentAutofillId = (AutofillId) Preconditions.checkNotNull(parentId);
            this.mNode.mAutofillId = new AutofillId(parentId, virtualId);
        }

        @Override // android.view.ViewStructure
        public void setAutofillType(int type) {
            this.mNode.mAutofillType = type;
        }

        @Override // android.view.ViewStructure
        public void setAutofillHints(String[] hints) {
            this.mNode.mAutofillHints = hints;
        }

        @Override // android.view.ViewStructure
        public void setAutofillValue(AutofillValue value) {
            this.mNode.mAutofillValue = value;
        }

        @Override // android.view.ViewStructure
        public void setAutofillOptions(CharSequence[] options) {
            this.mNode.mAutofillOptions = options;
        }

        @Override // android.view.ViewStructure
        public void setInputType(int inputType) {
            this.mNode.mInputType = inputType;
        }

        @Override // android.view.ViewStructure
        public void setMinTextEms(int minEms) {
            this.mNode.mMinEms = minEms;
        }

        @Override // android.view.ViewStructure
        public void setMaxTextEms(int maxEms) {
            this.mNode.mMaxEms = maxEms;
        }

        @Override // android.view.ViewStructure
        public void setMaxTextLength(int maxLength) {
            this.mNode.mMaxLength = maxLength;
        }

        @Override // android.view.ViewStructure
        public void setDataIsSensitive(boolean sensitive) {
            Log.w(ViewNode.TAG, "setDataIsSensitive() is not supported");
        }

        @Override // android.view.ViewStructure
        public void asyncCommit() {
            Log.w(ViewNode.TAG, "asyncCommit() is not supported");
        }

        @Override // android.view.ViewStructure
        public Rect getTempRect() {
            Log.w(ViewNode.TAG, "getTempRect() is not supported");
            return null;
        }

        @Override // android.view.ViewStructure
        public void setWebDomain(String domain) {
            Log.w(ViewNode.TAG, "setWebDomain() is not supported");
        }

        @Override // android.view.ViewStructure
        public void setLocaleList(LocaleList localeList) {
            this.mNode.mLocaleList = localeList;
        }

        @Override // android.view.ViewStructure
        public ViewStructure.HtmlInfo.Builder newHtmlInfoBuilder(String tagName) {
            Log.w(ViewNode.TAG, "newHtmlInfoBuilder() is not supported");
            return null;
        }

        @Override // android.view.ViewStructure
        public void setHtmlInfo(ViewStructure.HtmlInfo htmlInfo) {
            Log.w(ViewNode.TAG, "setHtmlInfo() is not supported");
        }

        private ViewNodeText getNodeText() {
            if (this.mNode.mText != null) {
                return this.mNode.mText;
            }
            this.mNode.mText = new ViewNodeText();
            return this.mNode.mText;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ViewNodeText {
        String mHint;
        int[] mLineBaselines;
        int[] mLineCharOffsets;
        CharSequence mText;
        int mTextBackgroundColor = 1;
        int mTextColor = 1;
        int mTextSelectionEnd;
        int mTextSelectionStart;
        float mTextSize;
        int mTextStyle;

        ViewNodeText() {
        }

        /* access modifiers changed from: package-private */
        public boolean isSimple() {
            return this.mTextBackgroundColor == 1 && this.mTextSelectionStart == 0 && this.mTextSelectionEnd == 0 && this.mLineCharOffsets == null && this.mLineBaselines == null && this.mHint == null;
        }

        ViewNodeText(Parcel in, boolean simple) {
            this.mText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.mTextSize = in.readFloat();
            this.mTextStyle = in.readInt();
            this.mTextColor = in.readInt();
            if (!simple) {
                this.mTextBackgroundColor = in.readInt();
                this.mTextSelectionStart = in.readInt();
                this.mTextSelectionEnd = in.readInt();
                this.mLineCharOffsets = in.createIntArray();
                this.mLineBaselines = in.createIntArray();
                this.mHint = in.readString();
            }
        }

        /* access modifiers changed from: package-private */
        public void writeToParcel(Parcel out, boolean simple) {
            TextUtils.writeToParcel(this.mText, out, 0);
            out.writeFloat(this.mTextSize);
            out.writeInt(this.mTextStyle);
            out.writeInt(this.mTextColor);
            if (!simple) {
                out.writeInt(this.mTextBackgroundColor);
                out.writeInt(this.mTextSelectionStart);
                out.writeInt(this.mTextSelectionEnd);
                out.writeIntArray(this.mLineCharOffsets);
                out.writeIntArray(this.mLineBaselines);
                out.writeString(this.mHint);
            }
        }
    }
}
