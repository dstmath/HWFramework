package android.app.assist;

import android.annotation.SystemApi;
import android.app.Activity;
import android.app.slice.Slice;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PooledStringReader;
import android.os.PooledStringWriter;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.SettingsStringUtil;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.ViewStructure;
import android.view.WindowManagerGlobal;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssistStructure implements Parcelable {
    public static final Parcelable.Creator<AssistStructure> CREATOR = new Parcelable.Creator<AssistStructure>() {
        /* class android.app.assist.AssistStructure.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AssistStructure createFromParcel(Parcel in) {
            return new AssistStructure(in);
        }

        @Override // android.os.Parcelable.Creator
        public AssistStructure[] newArray(int size) {
            return new AssistStructure[size];
        }
    };
    private static final boolean DEBUG_PARCEL = false;
    private static final boolean DEBUG_PARCEL_CHILDREN = false;
    private static final boolean DEBUG_PARCEL_TREE = false;
    private static final String DESCRIPTOR = "android.app.AssistStructure";
    private static final String TAG = "AssistStructure";
    private static final int TRANSACTION_XFER = 2;
    private static final int VALIDATE_VIEW_TOKEN = 572662306;
    private static final int VALIDATE_WINDOW_TOKEN = 286331153;
    private long mAcquisitionEndTime;
    private long mAcquisitionStartTime;
    private ComponentName mActivityComponent;
    private int mAutofillFlags;
    private int mFlags;
    private boolean mHaveData;
    private boolean mIsHomeActivity;
    private final ArrayList<ViewNodeBuilder> mPendingAsyncChildren;
    private IBinder mReceiveChannel;
    private boolean mSanitizeOnWrite;
    private SendChannel mSendChannel;
    private int mTaskId;
    private Rect mTmpRect;
    private final ArrayList<WindowNode> mWindowNodes;

    public static class AutofillOverlay {
        public boolean focused;
        public AutofillValue value;
    }

    public void setAcquisitionStartTime(long acquisitionStartTime) {
        this.mAcquisitionStartTime = acquisitionStartTime;
    }

    public void setAcquisitionEndTime(long acquisitionEndTime) {
        this.mAcquisitionEndTime = acquisitionEndTime;
    }

    public void setHomeActivity(boolean isHomeActivity) {
        this.mIsHomeActivity = isHomeActivity;
    }

    public long getAcquisitionStartTime() {
        ensureData();
        return this.mAcquisitionStartTime;
    }

    public long getAcquisitionEndTime() {
        ensureData();
        return this.mAcquisitionEndTime;
    }

    /* access modifiers changed from: package-private */
    public static final class SendChannel extends Binder {
        volatile AssistStructure mAssistStructure;

        SendChannel(AssistStructure as) {
            this.mAssistStructure = as;
        }

        /* access modifiers changed from: protected */
        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 2) {
                return super.onTransact(code, data, reply, flags);
            }
            AssistStructure as = this.mAssistStructure;
            if (as == null) {
                return true;
            }
            data.enforceInterface(AssistStructure.DESCRIPTOR);
            IBinder token = data.readStrongBinder();
            if (token == null) {
                new ParcelTransferWriter(as, reply).writeToParcel(as, reply);
                return true;
            } else if (token instanceof ParcelTransferWriter) {
                ((ParcelTransferWriter) token).writeToParcel(as, reply);
                return true;
            } else {
                Log.w(AssistStructure.TAG, "Caller supplied bad token type: " + token);
                return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ViewStackEntry {
        int curChild;
        ViewNode node;
        int numChildren;

        ViewStackEntry() {
        }
    }

    static final class ParcelTransferWriter extends Binder {
        ViewStackEntry mCurViewStackEntry;
        int mCurViewStackPos;
        int mCurWindow;
        int mNumWindows;
        int mNumWrittenViews;
        int mNumWrittenWindows;
        final boolean mSanitizeOnWrite;
        final float[] mTmpMatrix = new float[9];
        final ArrayList<ViewStackEntry> mViewStack = new ArrayList<>();
        final boolean mWriteStructure;

        ParcelTransferWriter(AssistStructure as, Parcel out) {
            int i;
            this.mSanitizeOnWrite = as.mSanitizeOnWrite;
            this.mWriteStructure = as.waitForReady();
            out.writeInt(as.mFlags);
            out.writeInt(as.mAutofillFlags);
            out.writeLong(as.mAcquisitionStartTime);
            out.writeLong(as.mAcquisitionEndTime);
            this.mNumWindows = as.mWindowNodes.size();
            if (!this.mWriteStructure || (i = this.mNumWindows) <= 0) {
                out.writeInt(0);
            } else {
                out.writeInt(i);
            }
        }

        /* access modifiers changed from: package-private */
        public void writeToParcel(AssistStructure as, Parcel out) {
            int start = out.dataPosition();
            this.mNumWrittenWindows = 0;
            this.mNumWrittenViews = 0;
            boolean more = writeToParcelInner(as, out);
            StringBuilder sb = new StringBuilder();
            sb.append("Flattened ");
            sb.append(more ? Slice.HINT_PARTIAL : "final");
            sb.append(" assist data: ");
            sb.append(out.dataPosition() - start);
            sb.append(" bytes, containing ");
            sb.append(this.mNumWrittenWindows);
            sb.append(" windows, ");
            sb.append(this.mNumWrittenViews);
            sb.append(" views");
            Log.i(AssistStructure.TAG, sb.toString());
        }

        /* access modifiers changed from: package-private */
        public boolean writeToParcelInner(AssistStructure as, Parcel out) {
            if (this.mNumWindows == 0) {
                return false;
            }
            PooledStringWriter pwriter = new PooledStringWriter(out);
            while (writeNextEntryToParcel(as, out, pwriter)) {
                if (out.dataSize() > 65536) {
                    out.writeInt(0);
                    out.writeStrongBinder(this);
                    pwriter.finish();
                    return true;
                }
            }
            pwriter.finish();
            this.mViewStack.clear();
            return false;
        }

        /* access modifiers changed from: package-private */
        public void pushViewStackEntry(ViewNode node, int pos) {
            ViewStackEntry entry;
            if (pos >= this.mViewStack.size()) {
                entry = new ViewStackEntry();
                this.mViewStack.add(entry);
            } else {
                entry = this.mViewStack.get(pos);
            }
            entry.node = node;
            entry.numChildren = node.getChildCount();
            entry.curChild = 0;
            this.mCurViewStackEntry = entry;
        }

        /* access modifiers changed from: package-private */
        public void writeView(ViewNode child, Parcel out, PooledStringWriter pwriter, int levelAdj) {
            out.writeInt(AssistStructure.VALIDATE_VIEW_TOKEN);
            int flags = child.writeSelfToParcel(out, pwriter, this.mSanitizeOnWrite, this.mTmpMatrix);
            this.mNumWrittenViews++;
            if ((1048576 & flags) != 0) {
                out.writeInt(child.mChildren.length);
                int pos = this.mCurViewStackPos + 1;
                this.mCurViewStackPos = pos;
                pushViewStackEntry(child, pos);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean writeNextEntryToParcel(AssistStructure as, Parcel out, PooledStringWriter pwriter) {
            ViewStackEntry viewStackEntry = this.mCurViewStackEntry;
            if (viewStackEntry == null) {
                int pos = this.mCurWindow;
                if (pos >= this.mNumWindows) {
                    return false;
                }
                WindowNode win = (WindowNode) as.mWindowNodes.get(pos);
                this.mCurWindow++;
                out.writeInt(AssistStructure.VALIDATE_WINDOW_TOKEN);
                win.writeSelfToParcel(out, pwriter, this.mTmpMatrix);
                this.mNumWrittenWindows++;
                ViewNode root = win.mRoot;
                this.mCurViewStackPos = 0;
                writeView(root, out, pwriter, 0);
                return true;
            } else if (viewStackEntry.curChild < this.mCurViewStackEntry.numChildren) {
                ViewNode child = this.mCurViewStackEntry.node.mChildren[this.mCurViewStackEntry.curChild];
                this.mCurViewStackEntry.curChild++;
                writeView(child, out, pwriter, 1);
                return true;
            } else {
                while (true) {
                    int pos2 = this.mCurViewStackPos - 1;
                    this.mCurViewStackPos = pos2;
                    if (pos2 >= 0) {
                        this.mCurViewStackEntry = this.mViewStack.get(pos2);
                        if (this.mCurViewStackEntry.curChild < this.mCurViewStackEntry.numChildren) {
                            break;
                        }
                    } else {
                        this.mCurViewStackEntry = null;
                        break;
                    }
                }
                return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class ParcelTransferReader {
        private final IBinder mChannel;
        private Parcel mCurParcel;
        int mNumReadViews;
        int mNumReadWindows;
        PooledStringReader mStringReader;
        final float[] mTmpMatrix = new float[9];
        private IBinder mTransferToken;

        ParcelTransferReader(IBinder channel) {
            this.mChannel = channel;
        }

        /* access modifiers changed from: package-private */
        public void go() {
            fetchData();
            AssistStructure.this.mFlags = this.mCurParcel.readInt();
            AssistStructure.this.mAutofillFlags = this.mCurParcel.readInt();
            AssistStructure.this.mAcquisitionStartTime = this.mCurParcel.readLong();
            AssistStructure.this.mAcquisitionEndTime = this.mCurParcel.readLong();
            int N = this.mCurParcel.readInt();
            if (N > 0) {
                this.mStringReader = new PooledStringReader(this.mCurParcel);
                for (int i = 0; i < N; i++) {
                    AssistStructure.this.mWindowNodes.add(new WindowNode(this));
                }
            }
            this.mCurParcel.recycle();
            this.mCurParcel = null;
        }

        /* access modifiers changed from: package-private */
        public Parcel readParcel(int validateToken, int level) {
            int token = this.mCurParcel.readInt();
            if (token == 0) {
                this.mTransferToken = this.mCurParcel.readStrongBinder();
                if (this.mTransferToken != null) {
                    fetchData();
                    this.mStringReader = new PooledStringReader(this.mCurParcel);
                    this.mCurParcel.readInt();
                    return this.mCurParcel;
                }
                throw new IllegalStateException("Reached end of partial data without transfer token");
            } else if (token == validateToken) {
                return this.mCurParcel;
            } else {
                throw new BadParcelableException("Got token " + Integer.toHexString(token) + ", expected token " + Integer.toHexString(validateToken));
            }
        }

        private void fetchData() {
            Parcel data = Parcel.obtain();
            try {
                data.writeInterfaceToken(AssistStructure.DESCRIPTOR);
                data.writeStrongBinder(this.mTransferToken);
                if (this.mCurParcel != null) {
                    data = this.mCurParcel;
                }
                this.mCurParcel = Parcel.obtain();
                try {
                    this.mChannel.transact(2, data, this.mCurParcel, 0);
                    data.recycle();
                    this.mNumReadViews = 0;
                    this.mNumReadWindows = 0;
                } catch (RemoteException e) {
                    Log.w(AssistStructure.TAG, "Failure reading AssistStructure data", e);
                    throw new IllegalStateException("Failure reading AssistStructure data: " + e);
                }
            } finally {
                data.recycle();
            }
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
        public void writeToParcel(Parcel out, boolean simple, boolean writeSensitive) {
            TextUtils.writeToParcel(writeSensitive ? this.mText : "", out, 0);
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

    public static class WindowNode {
        final int mDisplayId;
        final int mHeight;
        final ViewNode mRoot;
        final CharSequence mTitle;
        final int mWidth;
        final int mX;
        final int mY;

        WindowNode(AssistStructure assist, ViewRootImpl root, boolean forAutoFill, int flags) {
            View view = root.getView();
            Rect rect = new Rect();
            view.getBoundsOnScreen(rect);
            this.mX = rect.left - view.getLeft();
            this.mY = rect.top - view.getTop();
            this.mWidth = rect.width();
            this.mHeight = rect.height();
            this.mTitle = root.getTitle();
            this.mDisplayId = root.getDisplayId();
            this.mRoot = new ViewNode();
            ViewNodeBuilder builder = new ViewNodeBuilder(assist, this.mRoot, false);
            if ((root.getWindowFlags() & 8192) != 0) {
                if (forAutoFill) {
                    view.onProvideAutofillStructure(builder, resolveViewAutofillFlags(view.getContext(), flags));
                } else {
                    view.onProvideStructure(builder);
                    builder.setAssistBlocked(true);
                    return;
                }
            }
            if (forAutoFill) {
                view.dispatchProvideAutofillStructure(builder, resolveViewAutofillFlags(view.getContext(), flags));
            } else {
                view.dispatchProvideStructure(builder);
            }
        }

        WindowNode(ParcelTransferReader reader) {
            Parcel in = reader.readParcel(AssistStructure.VALIDATE_WINDOW_TOKEN, 0);
            reader.mNumReadWindows++;
            this.mX = in.readInt();
            this.mY = in.readInt();
            this.mWidth = in.readInt();
            this.mHeight = in.readInt();
            this.mTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.mDisplayId = in.readInt();
            this.mRoot = new ViewNode(reader, 0);
        }

        /* access modifiers changed from: package-private */
        public int resolveViewAutofillFlags(Context context, int fillRequestFlags) {
            return ((fillRequestFlags & 1) != 0 || context.isAutofillCompatibilityEnabled()) ? 1 : 0;
        }

        /* access modifiers changed from: package-private */
        public void writeSelfToParcel(Parcel out, PooledStringWriter pwriter, float[] tmpMatrix) {
            out.writeInt(this.mX);
            out.writeInt(this.mY);
            out.writeInt(this.mWidth);
            out.writeInt(this.mHeight);
            TextUtils.writeToParcel(this.mTitle, out, 0);
            out.writeInt(this.mDisplayId);
        }

        public int getLeft() {
            return this.mX;
        }

        public int getTop() {
            return this.mY;
        }

        public int getWidth() {
            return this.mWidth;
        }

        public int getHeight() {
            return this.mHeight;
        }

        public CharSequence getTitle() {
            return this.mTitle;
        }

        public int getDisplayId() {
            return this.mDisplayId;
        }

        public ViewNode getRootViewNode() {
            return this.mRoot;
        }
    }

    public static class ViewNode {
        static final int AUTOFILL_FLAGS_HAS_AUTOFILL_HINTS = 16;
        static final int AUTOFILL_FLAGS_HAS_AUTOFILL_OPTIONS = 32;
        static final int AUTOFILL_FLAGS_HAS_AUTOFILL_SESSION_ID = 2048;
        static final int AUTOFILL_FLAGS_HAS_AUTOFILL_TYPE = 8;
        static final int AUTOFILL_FLAGS_HAS_AUTOFILL_VALUE = 4;
        static final int AUTOFILL_FLAGS_HAS_AUTOFILL_VIEW_ID = 1;
        static final int AUTOFILL_FLAGS_HAS_AUTOFILL_VIRTUAL_VIEW_ID = 2;
        static final int AUTOFILL_FLAGS_HAS_HTML_INFO = 64;
        static final int AUTOFILL_FLAGS_HAS_MAX_TEXT_EMS = 512;
        static final int AUTOFILL_FLAGS_HAS_MAX_TEXT_LENGTH = 1024;
        static final int AUTOFILL_FLAGS_HAS_MIN_TEXT_EMS = 256;
        static final int AUTOFILL_FLAGS_HAS_TEXT_ID_ENTRY = 128;
        static final int FLAGS_ACCESSIBILITY_FOCUSED = 4096;
        static final int FLAGS_ACTIVATED = 8192;
        static final int FLAGS_ALL_CONTROL = -1048576;
        static final int FLAGS_ASSIST_BLOCKED = 128;
        static final int FLAGS_CHECKABLE = 256;
        static final int FLAGS_CHECKED = 512;
        static final int FLAGS_CLICKABLE = 1024;
        static final int FLAGS_CONTEXT_CLICKABLE = 16384;
        static final int FLAGS_DISABLED = 1;
        static final int FLAGS_FOCUSABLE = 16;
        static final int FLAGS_FOCUSED = 32;
        static final int FLAGS_HAS_ALPHA = 536870912;
        static final int FLAGS_HAS_CHILDREN = 1048576;
        static final int FLAGS_HAS_COMPLEX_TEXT = 8388608;
        static final int FLAGS_HAS_CONTENT_DESCRIPTION = 33554432;
        static final int FLAGS_HAS_ELEVATION = 268435456;
        static final int FLAGS_HAS_EXTRAS = 4194304;
        static final int FLAGS_HAS_ID = 2097152;
        static final int FLAGS_HAS_INPUT_TYPE = 262144;
        static final int FLAGS_HAS_LARGE_COORDS = 67108864;
        static final int FLAGS_HAS_LOCALE_LIST = 65536;
        static final int FLAGS_HAS_MATRIX = 1073741824;
        static final int FLAGS_HAS_SCROLL = 134217728;
        static final int FLAGS_HAS_TEXT = 16777216;
        static final int FLAGS_HAS_URL = 524288;
        static final int FLAGS_LONG_CLICKABLE = 2048;
        static final int FLAGS_OPAQUE = 32768;
        static final int FLAGS_SELECTED = 64;
        static final int FLAGS_VISIBILITY_MASK = 12;
        public static final int TEXT_COLOR_UNDEFINED = 1;
        public static final int TEXT_STYLE_BOLD = 1;
        public static final int TEXT_STYLE_ITALIC = 2;
        public static final int TEXT_STYLE_STRIKE_THRU = 8;
        public static final int TEXT_STYLE_UNDERLINE = 4;
        float mAlpha;
        int mAutofillFlags;
        String[] mAutofillHints;
        AutofillId mAutofillId;
        CharSequence[] mAutofillOptions;
        AutofillOverlay mAutofillOverlay;
        int mAutofillType;
        AutofillValue mAutofillValue;
        ViewNode[] mChildren;
        String mClassName;
        CharSequence mContentDescription;
        float mElevation;
        Bundle mExtras;
        int mFlags;
        int mHeight;
        ViewStructure.HtmlInfo mHtmlInfo;
        int mId;
        String mIdEntry;
        String mIdPackage;
        String mIdType;
        int mImportantForAutofill;
        int mInputType;
        LocaleList mLocaleList;
        Matrix mMatrix;
        int mMaxEms;
        int mMaxLength;
        int mMinEms;
        boolean mSanitized;
        int mScrollX;
        int mScrollY;
        ViewNodeText mText;
        String mTextIdEntry;
        String mWebDomain;
        String mWebScheme;
        int mWidth;
        int mX;
        int mY;

        @SystemApi
        public ViewNode() {
            this.mId = -1;
            this.mAutofillType = 0;
            this.mMinEms = -1;
            this.mMaxEms = -1;
            this.mMaxLength = -1;
            this.mAlpha = 1.0f;
        }

        ViewNode(ParcelTransferReader reader, int nestingLevel) {
            this.mId = -1;
            boolean z = false;
            this.mAutofillType = 0;
            this.mMinEms = -1;
            this.mMaxEms = -1;
            this.mMaxLength = -1;
            this.mAlpha = 1.0f;
            Parcel in = reader.readParcel(AssistStructure.VALIDATE_VIEW_TOKEN, nestingLevel);
            reader.mNumReadViews++;
            PooledStringReader preader = reader.mStringReader;
            this.mClassName = preader.readString();
            this.mFlags = in.readInt();
            int flags = this.mFlags;
            this.mAutofillFlags = in.readInt();
            int autofillFlags = this.mAutofillFlags;
            if ((2097152 & flags) != 0) {
                this.mId = in.readInt();
                if (this.mId != -1) {
                    this.mIdEntry = preader.readString();
                    if (this.mIdEntry != null) {
                        this.mIdType = preader.readString();
                        this.mIdPackage = preader.readString();
                    }
                }
            }
            if (autofillFlags != 0) {
                this.mSanitized = in.readInt() == 1;
                this.mImportantForAutofill = in.readInt();
                if ((autofillFlags & 1) != 0) {
                    int autofillViewId = in.readInt();
                    if ((autofillFlags & 2) != 0) {
                        this.mAutofillId = new AutofillId(autofillViewId, in.readInt());
                    } else {
                        this.mAutofillId = new AutofillId(autofillViewId);
                    }
                    if ((autofillFlags & 2048) != 0) {
                        this.mAutofillId.setSessionId(in.readInt());
                    }
                }
                if ((autofillFlags & 8) != 0) {
                    this.mAutofillType = in.readInt();
                }
                if ((autofillFlags & 16) != 0) {
                    this.mAutofillHints = in.readStringArray();
                }
                if ((autofillFlags & 4) != 0) {
                    this.mAutofillValue = (AutofillValue) in.readParcelable(null);
                }
                if ((autofillFlags & 32) != 0) {
                    this.mAutofillOptions = in.readCharSequenceArray();
                }
                if ((autofillFlags & 64) != 0) {
                    this.mHtmlInfo = (ViewStructure.HtmlInfo) in.readParcelable(null);
                }
                if ((autofillFlags & 256) != 0) {
                    this.mMinEms = in.readInt();
                }
                if ((autofillFlags & 512) != 0) {
                    this.mMaxEms = in.readInt();
                }
                if ((autofillFlags & 1024) != 0) {
                    this.mMaxLength = in.readInt();
                }
                if ((autofillFlags & 128) != 0) {
                    this.mTextIdEntry = preader.readString();
                }
            }
            if ((67108864 & flags) != 0) {
                this.mX = in.readInt();
                this.mY = in.readInt();
                this.mWidth = in.readInt();
                this.mHeight = in.readInt();
            } else {
                int val = in.readInt();
                this.mX = val & 32767;
                this.mY = (val >> 16) & 32767;
                int val2 = in.readInt();
                this.mWidth = val2 & 32767;
                this.mHeight = (val2 >> 16) & 32767;
            }
            if ((134217728 & flags) != 0) {
                this.mScrollX = in.readInt();
                this.mScrollY = in.readInt();
            }
            if ((1073741824 & flags) != 0) {
                this.mMatrix = new Matrix();
                in.readFloatArray(reader.mTmpMatrix);
                this.mMatrix.setValues(reader.mTmpMatrix);
            }
            if ((268435456 & flags) != 0) {
                this.mElevation = in.readFloat();
            }
            if ((536870912 & flags) != 0) {
                this.mAlpha = in.readFloat();
            }
            if ((33554432 & flags) != 0) {
                this.mContentDescription = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            }
            if ((16777216 & flags) != 0) {
                this.mText = new ViewNodeText(in, (8388608 & flags) == 0 ? true : z);
            }
            if ((262144 & flags) != 0) {
                this.mInputType = in.readInt();
            }
            if ((524288 & flags) != 0) {
                this.mWebScheme = in.readString();
                this.mWebDomain = in.readString();
            }
            if ((65536 & flags) != 0) {
                this.mLocaleList = (LocaleList) in.readParcelable(null);
            }
            if ((4194304 & flags) != 0) {
                this.mExtras = in.readBundle();
            }
            if ((1048576 & flags) != 0) {
                int NCHILDREN = in.readInt();
                this.mChildren = new ViewNode[NCHILDREN];
                for (int i = 0; i < NCHILDREN; i++) {
                    this.mChildren[i] = new ViewNode(reader, nestingLevel + 1);
                }
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x003a, code lost:
            if ((((r20.mWidth & -32768) != 0) | ((r20.mHeight & -32768) != 0)) != false) goto L_0x003c;
         */
        public int writeSelfToParcel(Parcel out, PooledStringWriter pwriter, boolean sanitizeOnWrite, float[] tmpMatrix) {
            AutofillValue sanitizedValue;
            boolean writeSensitive = true;
            int flags = this.mFlags & 1048575;
            int autofillFlags = 0;
            if (this.mId != -1) {
                flags |= 2097152;
            }
            if ((this.mX & -32768) == 0 && (this.mY & -32768) == 0) {
            }
            flags |= 67108864;
            if (!(this.mScrollX == 0 && this.mScrollY == 0)) {
                flags |= 134217728;
            }
            if (this.mMatrix != null) {
                flags |= 1073741824;
            }
            if (this.mElevation != 0.0f) {
                flags |= 268435456;
            }
            if (this.mAlpha != 1.0f) {
                flags |= 536870912;
            }
            if (this.mContentDescription != null) {
                flags |= 33554432;
            }
            ViewNodeText viewNodeText = this.mText;
            if (viewNodeText != null) {
                flags |= 16777216;
                if (!viewNodeText.isSimple()) {
                    flags |= 8388608;
                }
            }
            if (this.mInputType != 0) {
                flags |= 262144;
            }
            if (!(this.mWebScheme == null && this.mWebDomain == null)) {
                flags |= 524288;
            }
            if (this.mLocaleList != null) {
                flags |= 65536;
            }
            if (this.mExtras != null) {
                flags |= 4194304;
            }
            if (this.mChildren != null) {
                flags |= 1048576;
            }
            AutofillId autofillId = this.mAutofillId;
            if (autofillId != null) {
                autofillFlags = 0 | 1;
                if (autofillId.isVirtualInt()) {
                    autofillFlags |= 2;
                }
                if (this.mAutofillId.hasSession()) {
                    autofillFlags |= 2048;
                }
            }
            if (this.mAutofillValue != null) {
                autofillFlags |= 4;
            }
            if (this.mAutofillType != 0) {
                autofillFlags |= 8;
            }
            if (this.mAutofillHints != null) {
                autofillFlags |= 16;
            }
            if (this.mAutofillOptions != null) {
                autofillFlags |= 32;
            }
            if (this.mHtmlInfo instanceof Parcelable) {
                autofillFlags |= 64;
            }
            if (this.mMinEms > -1) {
                autofillFlags |= 256;
            }
            if (this.mMaxEms > -1) {
                autofillFlags |= 512;
            }
            if (this.mMaxLength > -1) {
                autofillFlags |= 1024;
            }
            if (this.mTextIdEntry != null) {
                autofillFlags |= 128;
            }
            pwriter.writeString(this.mClassName);
            int writtenFlags = flags;
            if (autofillFlags != 0 && (this.mSanitized || !sanitizeOnWrite)) {
                writtenFlags = flags & -513;
            }
            AutofillOverlay autofillOverlay = this.mAutofillOverlay;
            if (autofillOverlay != null) {
                if (autofillOverlay.focused) {
                    writtenFlags |= 32;
                } else {
                    writtenFlags &= -33;
                }
            }
            out.writeInt(writtenFlags);
            out.writeInt(autofillFlags);
            if ((2097152 & flags) != 0) {
                out.writeInt(this.mId);
                if (this.mId != -1) {
                    pwriter.writeString(this.mIdEntry);
                    if (this.mIdEntry != null) {
                        pwriter.writeString(this.mIdType);
                        pwriter.writeString(this.mIdPackage);
                    }
                }
            }
            if (autofillFlags != 0) {
                out.writeInt(this.mSanitized ? 1 : 0);
                out.writeInt(this.mImportantForAutofill);
                writeSensitive = this.mSanitized || !sanitizeOnWrite;
                if ((autofillFlags & 1) != 0) {
                    out.writeInt(this.mAutofillId.getViewId());
                    if ((autofillFlags & 2) != 0) {
                        out.writeInt(this.mAutofillId.getVirtualChildIntId());
                    }
                    if ((autofillFlags & 2048) != 0) {
                        out.writeInt(this.mAutofillId.getSessionId());
                    }
                }
                if ((autofillFlags & 8) != 0) {
                    out.writeInt(this.mAutofillType);
                }
                if ((autofillFlags & 16) != 0) {
                    out.writeStringArray(this.mAutofillHints);
                }
                if ((autofillFlags & 4) != 0) {
                    if (writeSensitive) {
                        sanitizedValue = this.mAutofillValue;
                    } else {
                        AutofillOverlay autofillOverlay2 = this.mAutofillOverlay;
                        if (autofillOverlay2 == null || autofillOverlay2.value == null) {
                            sanitizedValue = null;
                        } else {
                            sanitizedValue = this.mAutofillOverlay.value;
                        }
                    }
                    out.writeParcelable(sanitizedValue, 0);
                }
                if ((autofillFlags & 32) != 0) {
                    out.writeCharSequenceArray(this.mAutofillOptions);
                }
                if ((autofillFlags & 64) != 0) {
                    out.writeParcelable((Parcelable) this.mHtmlInfo, 0);
                }
                if ((autofillFlags & 256) != 0) {
                    out.writeInt(this.mMinEms);
                }
                if ((autofillFlags & 512) != 0) {
                    out.writeInt(this.mMaxEms);
                }
                if ((autofillFlags & 1024) != 0) {
                    out.writeInt(this.mMaxLength);
                }
                if ((autofillFlags & 128) != 0) {
                    pwriter.writeString(this.mTextIdEntry);
                }
            }
            if ((flags & 67108864) != 0) {
                out.writeInt(this.mX);
                out.writeInt(this.mY);
                out.writeInt(this.mWidth);
                out.writeInt(this.mHeight);
            } else {
                out.writeInt((this.mY << 16) | this.mX);
                out.writeInt((this.mHeight << 16) | this.mWidth);
            }
            if ((flags & 134217728) != 0) {
                out.writeInt(this.mScrollX);
                out.writeInt(this.mScrollY);
            }
            if ((flags & 1073741824) != 0) {
                this.mMatrix.getValues(tmpMatrix);
                out.writeFloatArray(tmpMatrix);
            }
            if ((flags & 268435456) != 0) {
                out.writeFloat(this.mElevation);
            }
            if ((flags & 536870912) != 0) {
                out.writeFloat(this.mAlpha);
            }
            if ((flags & 33554432) != 0) {
                TextUtils.writeToParcel(this.mContentDescription, out, 0);
            }
            if ((flags & 16777216) != 0) {
                this.mText.writeToParcel(out, (8388608 & flags) == 0, writeSensitive);
            }
            if ((flags & 262144) != 0) {
                out.writeInt(this.mInputType);
            }
            if ((524288 & flags) != 0) {
                out.writeString(this.mWebScheme);
                out.writeString(this.mWebDomain);
            }
            if ((65536 & flags) != 0) {
                out.writeParcelable(this.mLocaleList, 0);
            }
            if ((4194304 & flags) != 0) {
                out.writeBundle(this.mExtras);
            }
            return flags;
        }

        public int getId() {
            return this.mId;
        }

        public String getIdPackage() {
            return this.mIdPackage;
        }

        public String getIdType() {
            return this.mIdType;
        }

        public String getIdEntry() {
            return this.mIdEntry;
        }

        public AutofillId getAutofillId() {
            return this.mAutofillId;
        }

        public int getAutofillType() {
            return this.mAutofillType;
        }

        public String[] getAutofillHints() {
            return this.mAutofillHints;
        }

        public AutofillValue getAutofillValue() {
            return this.mAutofillValue;
        }

        public void setAutofillOverlay(AutofillOverlay overlay) {
            this.mAutofillOverlay = overlay;
        }

        public CharSequence[] getAutofillOptions() {
            return this.mAutofillOptions;
        }

        public int getInputType() {
            return this.mInputType;
        }

        public boolean isSanitized() {
            return this.mSanitized;
        }

        public void updateAutofillValue(AutofillValue value) {
            this.mAutofillValue = value;
            if (value.isText()) {
                if (this.mText == null) {
                    this.mText = new ViewNodeText();
                }
                this.mText.mText = value.getTextValue();
            }
        }

        public int getLeft() {
            return this.mX;
        }

        public int getTop() {
            return this.mY;
        }

        public int getScrollX() {
            return this.mScrollX;
        }

        public int getScrollY() {
            return this.mScrollY;
        }

        public int getWidth() {
            return this.mWidth;
        }

        public int getHeight() {
            return this.mHeight;
        }

        public Matrix getTransformation() {
            return this.mMatrix;
        }

        public float getElevation() {
            return this.mElevation;
        }

        public float getAlpha() {
            return this.mAlpha;
        }

        public int getVisibility() {
            return this.mFlags & 12;
        }

        public boolean isAssistBlocked() {
            return (this.mFlags & 128) != 0;
        }

        public boolean isEnabled() {
            return (this.mFlags & 1) == 0;
        }

        public boolean isClickable() {
            return (this.mFlags & 1024) != 0;
        }

        public boolean isFocusable() {
            return (this.mFlags & 16) != 0;
        }

        public boolean isFocused() {
            return (this.mFlags & 32) != 0;
        }

        public boolean isAccessibilityFocused() {
            return (this.mFlags & 4096) != 0;
        }

        public boolean isCheckable() {
            return (this.mFlags & 256) != 0;
        }

        public boolean isChecked() {
            return (this.mFlags & 512) != 0;
        }

        public boolean isSelected() {
            return (this.mFlags & 64) != 0;
        }

        public boolean isActivated() {
            return (this.mFlags & 8192) != 0;
        }

        public boolean isOpaque() {
            return (this.mFlags & 32768) != 0;
        }

        public boolean isLongClickable() {
            return (this.mFlags & 2048) != 0;
        }

        public boolean isContextClickable() {
            return (this.mFlags & 16384) != 0;
        }

        public String getClassName() {
            return this.mClassName;
        }

        public CharSequence getContentDescription() {
            return this.mContentDescription;
        }

        public String getWebDomain() {
            return this.mWebDomain;
        }

        public void setWebDomain(String domain) {
            if (domain != null) {
                Uri uri = Uri.parse(domain);
                if (uri == null) {
                    Log.w(AssistStructure.TAG, "Failed to parse web domain");
                    return;
                }
                this.mWebScheme = uri.getScheme();
                this.mWebDomain = uri.getHost();
            }
        }

        public String getWebScheme() {
            return this.mWebScheme;
        }

        public ViewStructure.HtmlInfo getHtmlInfo() {
            return this.mHtmlInfo;
        }

        public LocaleList getLocaleList() {
            return this.mLocaleList;
        }

        public CharSequence getText() {
            ViewNodeText viewNodeText = this.mText;
            if (viewNodeText != null) {
                return viewNodeText.mText;
            }
            return null;
        }

        public int getTextSelectionStart() {
            ViewNodeText viewNodeText = this.mText;
            if (viewNodeText != null) {
                return viewNodeText.mTextSelectionStart;
            }
            return -1;
        }

        public int getTextSelectionEnd() {
            ViewNodeText viewNodeText = this.mText;
            if (viewNodeText != null) {
                return viewNodeText.mTextSelectionEnd;
            }
            return -1;
        }

        public int getTextColor() {
            ViewNodeText viewNodeText = this.mText;
            if (viewNodeText != null) {
                return viewNodeText.mTextColor;
            }
            return 1;
        }

        public int getTextBackgroundColor() {
            ViewNodeText viewNodeText = this.mText;
            if (viewNodeText != null) {
                return viewNodeText.mTextBackgroundColor;
            }
            return 1;
        }

        public float getTextSize() {
            ViewNodeText viewNodeText = this.mText;
            if (viewNodeText != null) {
                return viewNodeText.mTextSize;
            }
            return 0.0f;
        }

        public int getTextStyle() {
            ViewNodeText viewNodeText = this.mText;
            if (viewNodeText != null) {
                return viewNodeText.mTextStyle;
            }
            return 0;
        }

        public int[] getTextLineCharOffsets() {
            ViewNodeText viewNodeText = this.mText;
            if (viewNodeText != null) {
                return viewNodeText.mLineCharOffsets;
            }
            return null;
        }

        public int[] getTextLineBaselines() {
            ViewNodeText viewNodeText = this.mText;
            if (viewNodeText != null) {
                return viewNodeText.mLineBaselines;
            }
            return null;
        }

        public String getTextIdEntry() {
            return this.mTextIdEntry;
        }

        public String getHint() {
            ViewNodeText viewNodeText = this.mText;
            if (viewNodeText != null) {
                return viewNodeText.mHint;
            }
            return null;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }

        public int getChildCount() {
            ViewNode[] viewNodeArr = this.mChildren;
            if (viewNodeArr != null) {
                return viewNodeArr.length;
            }
            return 0;
        }

        public ViewNode getChildAt(int index) {
            return this.mChildren[index];
        }

        public int getMinTextEms() {
            return this.mMinEms;
        }

        public int getMaxTextEms() {
            return this.mMaxEms;
        }

        public int getMaxTextLength() {
            return this.mMaxLength;
        }

        public int getImportantForAutofill() {
            return this.mImportantForAutofill;
        }
    }

    /* access modifiers changed from: package-private */
    public static class ViewNodeBuilder extends ViewStructure {
        final AssistStructure mAssist;
        final boolean mAsync;
        final ViewNode mNode;

        ViewNodeBuilder(AssistStructure assist, ViewNode node, boolean async) {
            this.mAssist = assist;
            this.mNode = node;
            this.mAsync = async;
        }

        @Override // android.view.ViewStructure
        public void setId(int id, String packageName, String typeName, String entryName) {
            ViewNode viewNode = this.mNode;
            viewNode.mId = id;
            viewNode.mIdPackage = packageName;
            viewNode.mIdType = typeName;
            viewNode.mIdEntry = entryName;
        }

        @Override // android.view.ViewStructure
        public void setDimens(int left, int top, int scrollX, int scrollY, int width, int height) {
            ViewNode viewNode = this.mNode;
            viewNode.mX = left;
            viewNode.mY = top;
            viewNode.mScrollX = scrollX;
            viewNode.mScrollY = scrollY;
            viewNode.mWidth = width;
            viewNode.mHeight = height;
        }

        @Override // android.view.ViewStructure
        public void setTransformation(Matrix matrix) {
            if (matrix == null) {
                this.mNode.mMatrix = null;
                return;
            }
            this.mNode.mMatrix = new Matrix(matrix);
        }

        @Override // android.view.ViewStructure
        public void setElevation(float elevation) {
            this.mNode.mElevation = elevation;
        }

        @Override // android.view.ViewStructure
        public void setAlpha(float alpha) {
            this.mNode.mAlpha = alpha;
        }

        @Override // android.view.ViewStructure
        public void setVisibility(int visibility) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -13) | (visibility & 12);
        }

        @Override // android.view.ViewStructure
        public void setAssistBlocked(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -129) | (state ? 128 : 0);
        }

        @Override // android.view.ViewStructure
        public void setEnabled(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -2) | (!state ? 1 : 0);
        }

        @Override // android.view.ViewStructure
        public void setClickable(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -1025) | (state ? 1024 : 0);
        }

        @Override // android.view.ViewStructure
        public void setLongClickable(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -2049) | (state ? 2048 : 0);
        }

        @Override // android.view.ViewStructure
        public void setContextClickable(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -16385) | (state ? 16384 : 0);
        }

        @Override // android.view.ViewStructure
        public void setFocusable(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -17) | (state ? 16 : 0);
        }

        @Override // android.view.ViewStructure
        public void setFocused(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -33) | (state ? 32 : 0);
        }

        @Override // android.view.ViewStructure
        public void setAccessibilityFocused(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -4097) | (state ? 4096 : 0);
        }

        @Override // android.view.ViewStructure
        public void setCheckable(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & TrafficStats.TAG_NETWORK_STACK_RANGE_END) | (state ? 256 : 0);
        }

        @Override // android.view.ViewStructure
        public void setChecked(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -513) | (state ? 512 : 0);
        }

        @Override // android.view.ViewStructure
        public void setSelected(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -65) | (state ? 64 : 0);
        }

        @Override // android.view.ViewStructure
        public void setActivated(boolean state) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -8193) | (state ? 8192 : 0);
        }

        @Override // android.view.ViewStructure
        public void setOpaque(boolean opaque) {
            ViewNode viewNode = this.mNode;
            viewNode.mFlags = (viewNode.mFlags & -32769) | (opaque ? 32768 : 0);
        }

        @Override // android.view.ViewStructure
        public void setClassName(String className) {
            this.mNode.mClassName = className;
        }

        @Override // android.view.ViewStructure
        public void setContentDescription(CharSequence contentDescription) {
            this.mNode.mContentDescription = contentDescription;
        }

        private final ViewNodeText getNodeText() {
            if (this.mNode.mText != null) {
                return this.mNode.mText;
            }
            this.mNode.mText = new ViewNodeText();
            return this.mNode.mText;
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
            if (this.mNode.mText != null) {
                return this.mNode.mText.mText;
            }
            return null;
        }

        @Override // android.view.ViewStructure
        public int getTextSelectionStart() {
            if (this.mNode.mText != null) {
                return this.mNode.mText.mTextSelectionStart;
            }
            return -1;
        }

        @Override // android.view.ViewStructure
        public int getTextSelectionEnd() {
            if (this.mNode.mText != null) {
                return this.mNode.mText.mTextSelectionEnd;
            }
            return -1;
        }

        @Override // android.view.ViewStructure
        public CharSequence getHint() {
            if (this.mNode.mText != null) {
                return this.mNode.mText.mHint;
            }
            return null;
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
            this.mNode.mChildren = new ViewNode[num];
        }

        @Override // android.view.ViewStructure
        public int addChildCount(int num) {
            if (this.mNode.mChildren == null) {
                setChildCount(num);
                return 0;
            }
            int start = this.mNode.mChildren.length;
            ViewNode[] newArray = new ViewNode[(start + num)];
            System.arraycopy(this.mNode.mChildren, 0, newArray, 0, start);
            this.mNode.mChildren = newArray;
            return start;
        }

        @Override // android.view.ViewStructure
        public int getChildCount() {
            if (this.mNode.mChildren != null) {
                return this.mNode.mChildren.length;
            }
            return 0;
        }

        @Override // android.view.ViewStructure
        public ViewStructure newChild(int index) {
            ViewNode node = new ViewNode();
            this.mNode.mChildren[index] = node;
            return new ViewNodeBuilder(this.mAssist, node, false);
        }

        @Override // android.view.ViewStructure
        public ViewStructure asyncNewChild(int index) {
            ViewNodeBuilder builder;
            synchronized (this.mAssist) {
                ViewNode node = new ViewNode();
                this.mNode.mChildren[index] = node;
                builder = new ViewNodeBuilder(this.mAssist, node, true);
                this.mAssist.mPendingAsyncChildren.add(builder);
            }
            return builder;
        }

        @Override // android.view.ViewStructure
        public void asyncCommit() {
            synchronized (this.mAssist) {
                if (!this.mAsync) {
                    throw new IllegalStateException("Child " + this + " was not created with ViewStructure.asyncNewChild");
                } else if (this.mAssist.mPendingAsyncChildren.remove(this)) {
                    this.mAssist.notifyAll();
                } else {
                    throw new IllegalStateException("Child " + this + " already committed");
                }
            }
        }

        @Override // android.view.ViewStructure
        public Rect getTempRect() {
            return this.mAssist.mTmpRect;
        }

        @Override // android.view.ViewStructure
        public void setAutofillId(AutofillId id) {
            this.mNode.mAutofillId = id;
        }

        @Override // android.view.ViewStructure
        public void setAutofillId(AutofillId parentId, int virtualId) {
            this.mNode.mAutofillId = new AutofillId(parentId, virtualId);
        }

        @Override // android.view.ViewStructure
        public AutofillId getAutofillId() {
            return this.mNode.mAutofillId;
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
        public void setImportantForAutofill(int mode) {
            this.mNode.mImportantForAutofill = mode;
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
            this.mNode.mSanitized = !sensitive;
        }

        @Override // android.view.ViewStructure
        public void setWebDomain(String domain) {
            this.mNode.setWebDomain(domain);
        }

        @Override // android.view.ViewStructure
        public void setLocaleList(LocaleList localeList) {
            this.mNode.mLocaleList = localeList;
        }

        @Override // android.view.ViewStructure
        public ViewStructure.HtmlInfo.Builder newHtmlInfoBuilder(String tagName) {
            return new HtmlInfoNodeBuilder(tagName);
        }

        @Override // android.view.ViewStructure
        public void setHtmlInfo(ViewStructure.HtmlInfo htmlInfo) {
            this.mNode.mHtmlInfo = htmlInfo;
        }
    }

    /* access modifiers changed from: private */
    public static final class HtmlInfoNode extends ViewStructure.HtmlInfo implements Parcelable {
        public static final Parcelable.Creator<HtmlInfoNode> CREATOR = new Parcelable.Creator<HtmlInfoNode>() {
            /* class android.app.assist.AssistStructure.HtmlInfoNode.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public HtmlInfoNode createFromParcel(Parcel parcel) {
                HtmlInfoNodeBuilder builder = new HtmlInfoNodeBuilder(parcel.readString());
                String[] names = parcel.readStringArray();
                String[] values = parcel.readStringArray();
                if (!(names == null || values == null)) {
                    if (names.length != values.length) {
                        Log.w(AssistStructure.TAG, "HtmlInfo attributes mismatch: names=" + names.length + ", values=" + values.length);
                    } else {
                        for (int i = 0; i < names.length; i++) {
                            builder.addAttribute(names[i], values[i]);
                        }
                    }
                }
                return builder.build();
            }

            @Override // android.os.Parcelable.Creator
            public HtmlInfoNode[] newArray(int size) {
                return new HtmlInfoNode[size];
            }
        };
        private ArrayList<Pair<String, String>> mAttributes;
        private final String[] mNames;
        private final String mTag;
        private final String[] mValues;

        private HtmlInfoNode(HtmlInfoNodeBuilder builder) {
            this.mTag = builder.mTag;
            if (builder.mNames == null) {
                this.mNames = null;
                this.mValues = null;
                return;
            }
            this.mNames = new String[builder.mNames.size()];
            this.mValues = new String[builder.mValues.size()];
            builder.mNames.toArray(this.mNames);
            builder.mValues.toArray(this.mValues);
        }

        @Override // android.view.ViewStructure.HtmlInfo
        public String getTag() {
            return this.mTag;
        }

        @Override // android.view.ViewStructure.HtmlInfo
        public List<Pair<String, String>> getAttributes() {
            String[] strArr;
            if (this.mAttributes == null && (strArr = this.mNames) != null) {
                this.mAttributes = new ArrayList<>(strArr.length);
                int i = 0;
                while (true) {
                    String[] strArr2 = this.mNames;
                    if (i >= strArr2.length) {
                        break;
                    }
                    this.mAttributes.add(i, new Pair<>(strArr2[i], this.mValues[i]));
                    i++;
                }
            }
            return this.mAttributes;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(this.mTag);
            parcel.writeStringArray(this.mNames);
            parcel.writeStringArray(this.mValues);
        }
    }

    /* access modifiers changed from: private */
    public static final class HtmlInfoNodeBuilder extends ViewStructure.HtmlInfo.Builder {
        private ArrayList<String> mNames;
        private final String mTag;
        private ArrayList<String> mValues;

        HtmlInfoNodeBuilder(String tag) {
            this.mTag = tag;
        }

        @Override // android.view.ViewStructure.HtmlInfo.Builder
        public ViewStructure.HtmlInfo.Builder addAttribute(String name, String value) {
            if (this.mNames == null) {
                this.mNames = new ArrayList<>();
                this.mValues = new ArrayList<>();
            }
            this.mNames.add(name);
            this.mValues.add(value);
            return this;
        }

        @Override // android.view.ViewStructure.HtmlInfo.Builder
        public HtmlInfoNode build() {
            return new HtmlInfoNode(this);
        }
    }

    public AssistStructure(Activity activity, boolean forAutoFill, int flags) {
        this.mWindowNodes = new ArrayList<>();
        this.mPendingAsyncChildren = new ArrayList<>();
        this.mTmpRect = new Rect();
        this.mSanitizeOnWrite = false;
        this.mHaveData = true;
        this.mFlags = flags;
        ArrayList<ViewRootImpl> views = WindowManagerGlobal.getInstance().getRootViews(activity.getActivityToken());
        for (int i = 0; i < views.size(); i++) {
            ViewRootImpl root = views.get(i);
            if (root.getView() == null) {
                Log.w(TAG, "Skipping window with dettached view: " + ((Object) root.getTitle()));
            } else {
                this.mWindowNodes.add(new WindowNode(this, root, forAutoFill, flags));
            }
        }
    }

    public AssistStructure() {
        this.mWindowNodes = new ArrayList<>();
        this.mPendingAsyncChildren = new ArrayList<>();
        this.mTmpRect = new Rect();
        this.mSanitizeOnWrite = false;
        this.mHaveData = true;
        this.mFlags = 0;
    }

    public AssistStructure(Parcel in) {
        this.mWindowNodes = new ArrayList<>();
        this.mPendingAsyncChildren = new ArrayList<>();
        this.mTmpRect = new Rect();
        boolean z = false;
        this.mSanitizeOnWrite = false;
        this.mTaskId = in.readInt();
        this.mActivityComponent = ComponentName.readFromParcel(in);
        this.mIsHomeActivity = in.readInt() == 1 ? true : z;
        this.mReceiveChannel = in.readStrongBinder();
    }

    public void sanitizeForParceling(boolean sanitize) {
        this.mSanitizeOnWrite = sanitize;
    }

    public void dump(boolean showSensitive) {
        String str;
        if (this.mActivityComponent == null) {
            Log.i(TAG, "dump(): calling ensureData() first");
            ensureData();
        }
        Log.i(TAG, "Task id: " + this.mTaskId);
        StringBuilder sb = new StringBuilder();
        sb.append("Activity: ");
        ComponentName componentName = this.mActivityComponent;
        if (componentName != null) {
            str = componentName.flattenToShortString();
        } else {
            str = null;
        }
        sb.append(str);
        Log.i(TAG, sb.toString());
        Log.i(TAG, "Sanitize on write: " + this.mSanitizeOnWrite);
        Log.i(TAG, "Flags: " + this.mFlags);
        int N = getWindowNodeCount();
        for (int i = 0; i < N; i++) {
            WindowNode node = getWindowNodeAt(i);
            Log.i(TAG, "Window #" + i + " [" + node.getLeft() + SmsManager.REGEX_PREFIX_DELIMITER + node.getTop() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + node.getWidth() + "x" + node.getHeight() + "] " + ((Object) node.getTitle()));
            dump("  ", node.getRootViewNode(), showSensitive);
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, ViewNode node, boolean showSensitive) {
        String safeText;
        Log.i(TAG, prefix + "View [" + node.getLeft() + SmsManager.REGEX_PREFIX_DELIMITER + node.getTop() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + node.getWidth() + "x" + node.getHeight() + "] " + node.getClassName());
        int id = node.getId();
        if (id != 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            sb.append("  ID: #");
            sb.append(Integer.toHexString(id));
            String entry = node.getIdEntry();
            if (entry != null) {
                String type = node.getIdType();
                String pkg = node.getIdPackage();
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                sb.append(pkg);
                sb.append(SettingsStringUtil.DELIMITER);
                sb.append(type);
                sb.append("/");
                sb.append(entry);
            }
            Log.i(TAG, sb.toString());
        }
        int scrollX = node.getScrollX();
        int scrollY = node.getScrollY();
        if (!(scrollX == 0 && scrollY == 0)) {
            Log.i(TAG, prefix + "  Scroll: " + scrollX + SmsManager.REGEX_PREFIX_DELIMITER + scrollY);
        }
        Matrix matrix = node.getTransformation();
        if (matrix != null) {
            Log.i(TAG, prefix + "  Transformation: " + matrix);
        }
        float elevation = node.getElevation();
        if (elevation != 0.0f) {
            Log.i(TAG, prefix + "  Elevation: " + elevation);
        }
        if (node.getAlpha() != 0.0f) {
            Log.i(TAG, prefix + "  Alpha: " + elevation);
        }
        CharSequence contentDescription = node.getContentDescription();
        if (contentDescription != null) {
            Log.i(TAG, prefix + "  Content description: " + ((Object) contentDescription));
        }
        CharSequence text = node.getText();
        if (text != null) {
            if (node.isSanitized() || showSensitive) {
                safeText = text.toString();
            } else {
                safeText = "REDACTED[" + text.length() + " chars]";
            }
            Log.i(TAG, prefix + "  Text (sel " + node.getTextSelectionStart() + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + node.getTextSelectionEnd() + "): " + safeText);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(prefix);
            sb2.append("  Text size: ");
            sb2.append(node.getTextSize());
            sb2.append(" , style: #");
            sb2.append(node.getTextStyle());
            Log.i(TAG, sb2.toString());
            Log.i(TAG, prefix + "  Text color fg: #" + Integer.toHexString(node.getTextColor()) + ", bg: #" + Integer.toHexString(node.getTextBackgroundColor()));
            StringBuilder sb3 = new StringBuilder();
            sb3.append(prefix);
            sb3.append("  Input type: ");
            sb3.append(node.getInputType());
            Log.i(TAG, sb3.toString());
            Log.i(TAG, prefix + "  Resource id: " + node.getTextIdEntry());
        }
        String webDomain = node.getWebDomain();
        if (webDomain != null) {
            Log.i(TAG, prefix + "  Web domain: " + webDomain);
        }
        ViewStructure.HtmlInfo htmlInfo = node.getHtmlInfo();
        if (htmlInfo != null) {
            Log.i(TAG, prefix + "  HtmlInfo: tag=" + htmlInfo.getTag() + ", attr=" + htmlInfo.getAttributes());
        }
        LocaleList localeList = node.getLocaleList();
        if (localeList != null) {
            Log.i(TAG, prefix + "  LocaleList: " + localeList);
        }
        String hint = node.getHint();
        if (hint != null) {
            Log.i(TAG, prefix + "  Hint: " + hint);
        }
        Bundle extras = node.getExtras();
        if (extras != null) {
            Log.i(TAG, prefix + "  Extras: " + extras);
        }
        if (node.isAssistBlocked()) {
            Log.i(TAG, prefix + "  BLOCKED");
        }
        AutofillId autofillId = node.getAutofillId();
        if (autofillId == null) {
            Log.i(TAG, prefix + " NO autofill ID");
        } else {
            Log.i(TAG, prefix + "  Autofill info: id= " + autofillId + ", type=" + node.getAutofillType() + ", options=" + Arrays.toString(node.getAutofillOptions()) + ", hints=" + Arrays.toString(node.getAutofillHints()) + ", value=" + node.getAutofillValue() + ", sanitized=" + node.isSanitized() + ", important=" + node.getImportantForAutofill());
        }
        int NCHILDREN = node.getChildCount();
        if (NCHILDREN > 0) {
            Log.i(TAG, prefix + "  Children:");
            String cprefix = prefix + "    ";
            int i = 0;
            while (i < NCHILDREN) {
                dump(cprefix, node.getChildAt(i), showSensitive);
                i++;
                NCHILDREN = NCHILDREN;
            }
        }
    }

    public void setTaskId(int taskId) {
        this.mTaskId = taskId;
    }

    public int getTaskId() {
        return this.mTaskId;
    }

    public void setActivityComponent(ComponentName componentName) {
        this.mActivityComponent = componentName;
    }

    public ComponentName getActivityComponent() {
        return this.mActivityComponent;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public boolean isHomeActivity() {
        return this.mIsHomeActivity;
    }

    public int getWindowNodeCount() {
        ensureData();
        return this.mWindowNodes.size();
    }

    public WindowNode getWindowNodeAt(int index) {
        ensureData();
        return this.mWindowNodes.get(index);
    }

    public void ensureDataForAutofill() {
        if (!this.mHaveData) {
            this.mHaveData = true;
            Binder.allowBlocking(this.mReceiveChannel);
            try {
                new ParcelTransferReader(this.mReceiveChannel).go();
            } finally {
                Binder.defaultBlocking(this.mReceiveChannel);
            }
        }
    }

    public void ensureData() {
        if (!this.mHaveData) {
            this.mHaveData = true;
            new ParcelTransferReader(this.mReceiveChannel).go();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean waitForReady() {
        boolean skipStructure = false;
        synchronized (this) {
            long endTime = SystemClock.uptimeMillis() + 5000;
            while (this.mPendingAsyncChildren.size() > 0) {
                long now = SystemClock.uptimeMillis();
                if (now >= endTime) {
                    break;
                }
                try {
                    wait(endTime - now);
                } catch (InterruptedException e) {
                }
            }
            if (this.mPendingAsyncChildren.size() > 0) {
                Log.w(TAG, "Skipping assist structure, waiting too long for async children (have " + this.mPendingAsyncChildren.size() + " remaining");
                skipStructure = true;
            }
        }
        return !skipStructure;
    }

    public void clearSendChannel() {
        SendChannel sendChannel = this.mSendChannel;
        if (sendChannel != null) {
            sendChannel.mAssistStructure = null;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mTaskId);
        ComponentName.writeToParcel(this.mActivityComponent, out);
        out.writeInt(this.mIsHomeActivity ? 1 : 0);
        if (this.mHaveData) {
            if (this.mSendChannel == null) {
                this.mSendChannel = new SendChannel(this);
            }
            out.writeStrongBinder(this.mSendChannel);
            return;
        }
        out.writeStrongBinder(this.mReceiveChannel);
    }
}
