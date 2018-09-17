package android.app.assist;

import android.app.Activity;
import android.content.ComponentName;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.camera2.params.TonemapCurve;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PooledStringReader;
import android.os.PooledStringWriter;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.ViewStructure;
import android.view.ViewStructure.HtmlInfo;
import android.view.ViewStructure.HtmlInfo.Builder;
import android.view.WindowManagerGlobal;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssistStructure implements Parcelable {
    public static final Creator<AssistStructure> CREATOR = new Creator<AssistStructure>() {
        public AssistStructure createFromParcel(Parcel in) {
            return new AssistStructure(in);
        }

        public AssistStructure[] newArray(int size) {
            return new AssistStructure[size];
        }
    };
    static final boolean DEBUG_PARCEL = false;
    static final boolean DEBUG_PARCEL_CHILDREN = false;
    static final boolean DEBUG_PARCEL_TREE = false;
    static final String DESCRIPTOR = "android.app.AssistStructure";
    static final String TAG = "AssistStructure";
    static final int TRANSACTION_XFER = 2;
    static final int VALIDATE_VIEW_TOKEN = 572662306;
    static final int VALIDATE_WINDOW_TOKEN = 286331153;
    private long mAcquisitionEndTime;
    private long mAcquisitionStartTime;
    ComponentName mActivityComponent;
    private int mFlags;
    boolean mHaveData;
    private boolean mIsHomeActivity;
    final ArrayList<ViewNodeBuilder> mPendingAsyncChildren;
    IBinder mReceiveChannel;
    boolean mSanitizeOnWrite;
    SendChannel mSendChannel;
    Rect mTmpRect;
    final ArrayList<WindowNode> mWindowNodes;

    public static class AutofillOverlay {
        public boolean focused;
        public AutofillValue value;
    }

    private static final class HtmlInfoNode extends HtmlInfo implements Parcelable {
        public static final Creator<HtmlInfoNode> CREATOR = new Creator<HtmlInfoNode>() {
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

            public HtmlInfoNode[] newArray(int size) {
                return new HtmlInfoNode[size];
            }
        };
        private ArrayList<Pair<String, String>> mAttributes;
        private final String[] mNames;
        private final String mTag;
        private final String[] mValues;

        /* synthetic */ HtmlInfoNode(HtmlInfoNodeBuilder builder, HtmlInfoNode -this1) {
            this(builder);
        }

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

        public String getTag() {
            return this.mTag;
        }

        public List<Pair<String, String>> getAttributes() {
            if (this.mAttributes == null && this.mNames != null) {
                this.mAttributes = new ArrayList(this.mNames.length);
                for (int i = 0; i < this.mNames.length; i++) {
                    this.mAttributes.add(i, new Pair(this.mNames[i], this.mValues[i]));
                }
            }
            return this.mAttributes;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(this.mTag);
            parcel.writeStringArray(this.mNames);
            parcel.writeStringArray(this.mValues);
        }
    }

    private static final class HtmlInfoNodeBuilder extends Builder {
        private ArrayList<String> mNames;
        private final String mTag;
        private ArrayList<String> mValues;

        HtmlInfoNodeBuilder(String tag) {
            this.mTag = tag;
        }

        public Builder addAttribute(String name, String value) {
            if (this.mNames == null) {
                this.mNames = new ArrayList();
                this.mValues = new ArrayList();
            }
            this.mNames.add(name);
            this.mValues.add(value);
            return this;
        }

        public HtmlInfoNode build() {
            return new HtmlInfoNode(this, null);
        }
    }

    final class ParcelTransferReader {
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

        void go() {
            fetchData();
            AssistStructure.this.mActivityComponent = ComponentName.readFromParcel(this.mCurParcel);
            AssistStructure.this.mFlags = this.mCurParcel.readInt();
            AssistStructure.this.mAcquisitionStartTime = this.mCurParcel.readLong();
            AssistStructure.this.mAcquisitionEndTime = this.mCurParcel.readLong();
            int N = this.mCurParcel.readInt();
            if (N > 0) {
                this.mStringReader = new PooledStringReader(this.mCurParcel);
                for (int i = 0; i < N; i++) {
                    AssistStructure.this.mWindowNodes.add(new WindowNode(this));
                }
            }
        }

        Parcel readParcel(int validateToken, int level) {
            int token = this.mCurParcel.readInt();
            if (token == 0) {
                this.mTransferToken = this.mCurParcel.readStrongBinder();
                if (this.mTransferToken == null) {
                    throw new IllegalStateException("Reached end of partial data without transfer token");
                }
                fetchData();
                this.mStringReader = new PooledStringReader(this.mCurParcel);
                this.mCurParcel.readInt();
                return this.mCurParcel;
            } else if (token == validateToken) {
                return this.mCurParcel;
            } else {
                throw new BadParcelableException("Got token " + Integer.toHexString(token) + ", expected token " + Integer.toHexString(validateToken));
            }
        }

        private void fetchData() {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(AssistStructure.DESCRIPTOR);
            data.writeStrongBinder(this.mTransferToken);
            if (this.mCurParcel != null) {
                this.mCurParcel.recycle();
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
        final ArrayList<ViewStackEntry> mViewStack = new ArrayList();
        final boolean mWriteStructure;

        ParcelTransferWriter(AssistStructure as, Parcel out) {
            this.mSanitizeOnWrite = as.mSanitizeOnWrite;
            this.mWriteStructure = as.waitForReady();
            ComponentName.writeToParcel(as.mActivityComponent, out);
            out.writeInt(as.mFlags);
            out.writeLong(as.mAcquisitionStartTime);
            out.writeLong(as.mAcquisitionEndTime);
            this.mNumWindows = as.mWindowNodes.size();
            if (!this.mWriteStructure || this.mNumWindows <= 0) {
                out.writeInt(0);
            } else {
                out.writeInt(this.mNumWindows);
            }
        }

        void writeToParcel(AssistStructure as, Parcel out) {
            int start = out.dataPosition();
            this.mNumWrittenWindows = 0;
            this.mNumWrittenViews = 0;
            Log.i(AssistStructure.TAG, "Flattened " + (writeToParcelInner(as, out) ? "partial" : "final") + " assist data: " + (out.dataPosition() - start) + " bytes, containing " + this.mNumWrittenWindows + " windows, " + this.mNumWrittenViews + " views");
        }

        boolean writeToParcelInner(AssistStructure as, Parcel out) {
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

        void pushViewStackEntry(ViewNode node, int pos) {
            ViewStackEntry entry;
            if (pos >= this.mViewStack.size()) {
                entry = new ViewStackEntry();
                this.mViewStack.add(entry);
            } else {
                entry = (ViewStackEntry) this.mViewStack.get(pos);
            }
            entry.node = node;
            entry.numChildren = node.getChildCount();
            entry.curChild = 0;
            this.mCurViewStackEntry = entry;
        }

        void writeView(ViewNode child, Parcel out, PooledStringWriter pwriter, int levelAdj) {
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

        boolean writeNextEntryToParcel(AssistStructure as, Parcel out, PooledStringWriter pwriter) {
            int pos;
            if (this.mCurViewStackEntry == null) {
                pos = this.mCurWindow;
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
            } else if (this.mCurViewStackEntry.curChild < this.mCurViewStackEntry.numChildren) {
                ViewNode child = this.mCurViewStackEntry.node.mChildren[this.mCurViewStackEntry.curChild];
                ViewStackEntry viewStackEntry = this.mCurViewStackEntry;
                viewStackEntry.curChild++;
                writeView(child, out, pwriter, 1);
                return true;
            } else {
                do {
                    pos = this.mCurViewStackPos - 1;
                    this.mCurViewStackPos = pos;
                    if (pos < 0) {
                        this.mCurViewStackEntry = null;
                        break;
                    }
                    this.mCurViewStackEntry = (ViewStackEntry) this.mViewStack.get(pos);
                } while (this.mCurViewStackEntry.curChild >= this.mCurViewStackEntry.numChildren);
                return true;
            }
        }
    }

    static final class SendChannel extends Binder {
        volatile AssistStructure mAssistStructure;

        SendChannel(AssistStructure as) {
            this.mAssistStructure = as;
        }

        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
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

    public static class ViewNode {
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
        static final int FLAGS_HAS_AUTOFILL_DATA = Integer.MIN_VALUE;
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
        float mAlpha = 1.0f;
        String[] mAutofillHints;
        AutofillId mAutofillId;
        CharSequence[] mAutofillOptions;
        AutofillOverlay mAutofillOverlay;
        int mAutofillType = 0;
        AutofillValue mAutofillValue;
        ViewNode[] mChildren;
        String mClassName;
        CharSequence mContentDescription;
        float mElevation;
        Bundle mExtras;
        int mFlags;
        int mHeight;
        HtmlInfo mHtmlInfo;
        int mId = -1;
        String mIdEntry;
        String mIdPackage;
        String mIdType;
        int mInputType;
        LocaleList mLocaleList;
        Matrix mMatrix;
        boolean mSanitized;
        int mScrollX;
        int mScrollY;
        ViewNodeText mText;
        String mWebDomain;
        int mWidth;
        int mX;
        int mY;

        ViewNode() {
        }

        ViewNode(ParcelTransferReader reader, int nestingLevel) {
            boolean z = true;
            Parcel in = reader.readParcel(AssistStructure.VALIDATE_VIEW_TOKEN, nestingLevel);
            reader.mNumReadViews++;
            PooledStringReader preader = reader.mStringReader;
            this.mClassName = preader.readString();
            this.mFlags = in.readInt();
            int flags = this.mFlags;
            if ((2097152 & flags) != 0) {
                this.mId = in.readInt();
                if (this.mId != 0) {
                    this.mIdEntry = preader.readString();
                    if (this.mIdEntry != null) {
                        this.mIdType = preader.readString();
                        this.mIdPackage = preader.readString();
                    }
                }
            }
            if ((Integer.MIN_VALUE & flags) != 0) {
                boolean z2;
                if (in.readInt() == 1) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                this.mSanitized = z2;
                this.mAutofillId = (AutofillId) in.readParcelable(null);
                this.mAutofillType = in.readInt();
                this.mAutofillHints = in.readStringArray();
                this.mAutofillValue = (AutofillValue) in.readParcelable(null);
                this.mAutofillOptions = in.readCharSequenceArray();
                Parcelable p = in.readParcelable(null);
                if (p instanceof HtmlInfo) {
                    this.mHtmlInfo = (HtmlInfo) p;
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
                val = in.readInt();
                this.mWidth = val & 32767;
                this.mHeight = (val >> 16) & 32767;
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
                this.mContentDescription = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            }
            if ((16777216 & flags) != 0) {
                if ((8388608 & flags) != 0) {
                    z = false;
                }
                this.mText = new ViewNodeText(in, z);
            }
            if ((262144 & flags) != 0) {
                this.mInputType = in.readInt();
            }
            if ((524288 & flags) != 0) {
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

        /* JADX WARNING: Missing block: B:124:0x01a3, code:
            if ((r4 | r7) != 0) goto L_0x0028;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        int writeSelfToParcel(Parcel out, PooledStringWriter pwriter, boolean sanitizeOnWrite, float[] tmpMatrix) {
            int i;
            boolean z = true;
            boolean writeSensitive = true;
            int flags = this.mFlags & 1048575;
            if (this.mId != -1) {
                flags |= 2097152;
            }
            if (this.mAutofillId != null) {
                flags |= Integer.MIN_VALUE;
            }
            if ((this.mX & -32768) == 0 && (this.mY & -32768) == 0) {
                if ((this.mWidth & -32768) != 0) {
                    i = 1;
                } else {
                    i = 0;
                }
                int i2;
                if ((this.mHeight & -32768) != 0) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
            }
            flags |= 67108864;
            if (!(this.mScrollX == 0 && this.mScrollY == 0)) {
                flags |= 134217728;
            }
            if (this.mMatrix != null) {
                flags |= 1073741824;
            }
            if (this.mElevation != TonemapCurve.LEVEL_BLACK) {
                flags |= 268435456;
            }
            if (this.mAlpha != 1.0f) {
                flags |= 536870912;
            }
            if (this.mContentDescription != null) {
                flags |= 33554432;
            }
            if (this.mText != null) {
                flags |= 16777216;
                if (!this.mText.isSimple()) {
                    flags |= 8388608;
                }
            }
            if (this.mInputType != 0) {
                flags |= 262144;
            }
            if (this.mWebDomain != null) {
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
            pwriter.writeString(this.mClassName);
            int writtenFlags = flags;
            if ((flags & Integer.MIN_VALUE) != 0 && (this.mSanitized || (sanitizeOnWrite ^ 1) != 0)) {
                writtenFlags = flags & -513;
            }
            if (this.mAutofillOverlay != null) {
                if (this.mAutofillOverlay.focused) {
                    writtenFlags |= 32;
                } else {
                    writtenFlags &= -33;
                }
            }
            out.writeInt(writtenFlags);
            if ((2097152 & flags) != 0) {
                out.writeInt(this.mId);
                if (this.mId != 0) {
                    pwriter.writeString(this.mIdEntry);
                    if (this.mIdEntry != null) {
                        pwriter.writeString(this.mIdType);
                        pwriter.writeString(this.mIdPackage);
                    }
                }
            }
            if ((flags & Integer.MIN_VALUE) != 0) {
                Parcelable sanitizedValue;
                writeSensitive = !this.mSanitized ? sanitizeOnWrite ^ 1 : true;
                if (this.mSanitized) {
                    i = 1;
                } else {
                    i = 0;
                }
                out.writeInt(i);
                out.writeParcelable(this.mAutofillId, 0);
                out.writeInt(this.mAutofillType);
                out.writeStringArray(this.mAutofillHints);
                if (writeSensitive) {
                    sanitizedValue = this.mAutofillValue;
                } else if (this.mAutofillOverlay == null || this.mAutofillOverlay.value == null) {
                    sanitizedValue = null;
                } else {
                    sanitizedValue = this.mAutofillOverlay.value;
                }
                out.writeParcelable(sanitizedValue, 0);
                out.writeCharSequenceArray(this.mAutofillOptions);
                if (this.mHtmlInfo instanceof Parcelable) {
                    out.writeParcelable((Parcelable) this.mHtmlInfo, 0);
                } else {
                    out.writeParcelable(null, 0);
                }
            }
            if ((67108864 & flags) != 0) {
                out.writeInt(this.mX);
                out.writeInt(this.mY);
                out.writeInt(this.mWidth);
                out.writeInt(this.mHeight);
            } else {
                out.writeInt((this.mY << 16) | this.mX);
                out.writeInt((this.mHeight << 16) | this.mWidth);
            }
            if ((134217728 & flags) != 0) {
                out.writeInt(this.mScrollX);
                out.writeInt(this.mScrollY);
            }
            if ((1073741824 & flags) != 0) {
                this.mMatrix.getValues(tmpMatrix);
                out.writeFloatArray(tmpMatrix);
            }
            if ((268435456 & flags) != 0) {
                out.writeFloat(this.mElevation);
            }
            if ((536870912 & flags) != 0) {
                out.writeFloat(this.mAlpha);
            }
            if ((33554432 & flags) != 0) {
                TextUtils.writeToParcel(this.mContentDescription, out, 0);
            }
            if ((16777216 & flags) != 0) {
                ViewNodeText viewNodeText = this.mText;
                if ((8388608 & flags) != 0) {
                    z = false;
                }
                viewNodeText.writeToParcel(out, z, writeSensitive);
            }
            if ((262144 & flags) != 0) {
                out.writeInt(this.mInputType);
            }
            if ((524288 & flags) != 0) {
                out.writeString(this.mWebDomain);
            }
            if ((flags & 65536) != 0) {
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

        public HtmlInfo getHtmlInfo() {
            return this.mHtmlInfo;
        }

        public LocaleList getLocaleList() {
            return this.mLocaleList;
        }

        public CharSequence getText() {
            return this.mText != null ? this.mText.mText : null;
        }

        public int getTextSelectionStart() {
            return this.mText != null ? this.mText.mTextSelectionStart : -1;
        }

        public int getTextSelectionEnd() {
            return this.mText != null ? this.mText.mTextSelectionEnd : -1;
        }

        public int getTextColor() {
            return this.mText != null ? this.mText.mTextColor : 1;
        }

        public int getTextBackgroundColor() {
            return this.mText != null ? this.mText.mTextBackgroundColor : 1;
        }

        public float getTextSize() {
            return this.mText != null ? this.mText.mTextSize : TonemapCurve.LEVEL_BLACK;
        }

        public int getTextStyle() {
            return this.mText != null ? this.mText.mTextStyle : 0;
        }

        public int[] getTextLineCharOffsets() {
            return this.mText != null ? this.mText.mLineCharOffsets : null;
        }

        public int[] getTextLineBaselines() {
            return this.mText != null ? this.mText.mLineBaselines : null;
        }

        public String getHint() {
            return this.mText != null ? this.mText.mHint : null;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }

        public int getChildCount() {
            return this.mChildren != null ? this.mChildren.length : 0;
        }

        public ViewNode getChildAt(int index) {
            return this.mChildren[index];
        }
    }

    static class ViewNodeBuilder extends ViewStructure {
        final AssistStructure mAssist;
        final boolean mAsync;
        final ViewNode mNode;

        ViewNodeBuilder(AssistStructure assist, ViewNode node, boolean async) {
            this.mAssist = assist;
            this.mNode = node;
            this.mAsync = async;
        }

        public void setId(int id, String packageName, String typeName, String entryName) {
            this.mNode.mId = id;
            this.mNode.mIdPackage = packageName;
            this.mNode.mIdType = typeName;
            this.mNode.mIdEntry = entryName;
        }

        public void setDimens(int left, int top, int scrollX, int scrollY, int width, int height) {
            this.mNode.mX = left;
            this.mNode.mY = top;
            this.mNode.mScrollX = scrollX;
            this.mNode.mScrollY = scrollY;
            this.mNode.mWidth = width;
            this.mNode.mHeight = height;
        }

        public void setTransformation(Matrix matrix) {
            if (matrix == null) {
                this.mNode.mMatrix = null;
                return;
            }
            this.mNode.mMatrix = new Matrix(matrix);
        }

        public void setElevation(float elevation) {
            this.mNode.mElevation = elevation;
        }

        public void setAlpha(float alpha) {
            this.mNode.mAlpha = alpha;
        }

        public void setVisibility(int visibility) {
            this.mNode.mFlags = (this.mNode.mFlags & -13) | visibility;
        }

        public void setAssistBlocked(boolean state) {
            this.mNode.mFlags = (state ? 128 : 0) | (this.mNode.mFlags & -129);
        }

        public void setEnabled(boolean state) {
            this.mNode.mFlags = (state ? 0 : 1) | (this.mNode.mFlags & -2);
        }

        public void setClickable(boolean state) {
            this.mNode.mFlags = (state ? 1024 : 0) | (this.mNode.mFlags & -1025);
        }

        public void setLongClickable(boolean state) {
            this.mNode.mFlags = (state ? 2048 : 0) | (this.mNode.mFlags & -2049);
        }

        public void setContextClickable(boolean state) {
            this.mNode.mFlags = (state ? 16384 : 0) | (this.mNode.mFlags & -16385);
        }

        public void setFocusable(boolean state) {
            this.mNode.mFlags = (state ? 16 : 0) | (this.mNode.mFlags & -17);
        }

        public void setFocused(boolean state) {
            this.mNode.mFlags = (state ? 32 : 0) | (this.mNode.mFlags & -33);
        }

        public void setAccessibilityFocused(boolean state) {
            this.mNode.mFlags = (state ? 4096 : 0) | (this.mNode.mFlags & -4097);
        }

        public void setCheckable(boolean state) {
            this.mNode.mFlags = (state ? 256 : 0) | (this.mNode.mFlags & -257);
        }

        public void setChecked(boolean state) {
            this.mNode.mFlags = (state ? 512 : 0) | (this.mNode.mFlags & -513);
        }

        public void setSelected(boolean state) {
            this.mNode.mFlags = (state ? 64 : 0) | (this.mNode.mFlags & -65);
        }

        public void setActivated(boolean state) {
            this.mNode.mFlags = (state ? 8192 : 0) | (this.mNode.mFlags & -8193);
        }

        public void setOpaque(boolean opaque) {
            this.mNode.mFlags = (opaque ? 32768 : 0) | (-32769 & this.mNode.mFlags);
        }

        public void setClassName(String className) {
            this.mNode.mClassName = className;
        }

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

        public void setText(CharSequence text) {
            ViewNodeText t = getNodeText();
            t.mText = TextUtils.trimNoCopySpans(text);
            t.mTextSelectionEnd = -1;
            t.mTextSelectionStart = -1;
        }

        public void setText(CharSequence text, int selectionStart, int selectionEnd) {
            ViewNodeText t = getNodeText();
            t.mText = TextUtils.trimNoCopySpans(text);
            t.mTextSelectionStart = selectionStart;
            t.mTextSelectionEnd = selectionEnd;
        }

        public void setTextStyle(float size, int fgColor, int bgColor, int style) {
            ViewNodeText t = getNodeText();
            t.mTextColor = fgColor;
            t.mTextBackgroundColor = bgColor;
            t.mTextSize = size;
            t.mTextStyle = style;
        }

        public void setTextLines(int[] charOffsets, int[] baselines) {
            ViewNodeText t = getNodeText();
            t.mLineCharOffsets = charOffsets;
            t.mLineBaselines = baselines;
        }

        public void setHint(CharSequence hint) {
            String str = null;
            ViewNodeText nodeText = getNodeText();
            if (hint != null) {
                str = hint.toString();
            }
            nodeText.mHint = str;
        }

        public CharSequence getText() {
            return this.mNode.mText != null ? this.mNode.mText.mText : null;
        }

        public int getTextSelectionStart() {
            return this.mNode.mText != null ? this.mNode.mText.mTextSelectionStart : -1;
        }

        public int getTextSelectionEnd() {
            return this.mNode.mText != null ? this.mNode.mText.mTextSelectionEnd : -1;
        }

        public CharSequence getHint() {
            return this.mNode.mText != null ? this.mNode.mText.mHint : null;
        }

        public Bundle getExtras() {
            if (this.mNode.mExtras != null) {
                return this.mNode.mExtras;
            }
            this.mNode.mExtras = new Bundle();
            return this.mNode.mExtras;
        }

        public boolean hasExtras() {
            return this.mNode.mExtras != null;
        }

        public void setChildCount(int num) {
            this.mNode.mChildren = new ViewNode[num];
        }

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

        public int getChildCount() {
            return this.mNode.mChildren != null ? this.mNode.mChildren.length : 0;
        }

        public ViewStructure newChild(int index) {
            ViewNode node = new ViewNode();
            this.mNode.mChildren[index] = node;
            return new ViewNodeBuilder(this.mAssist, node, false);
        }

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

        public Rect getTempRect() {
            return this.mAssist.mTmpRect;
        }

        public void setAutofillId(AutofillId id) {
            this.mNode.mAutofillId = id;
        }

        public void setAutofillId(AutofillId parentId, int virtualId) {
            this.mNode.mAutofillId = new AutofillId(parentId, virtualId);
        }

        public AutofillId getAutofillId() {
            return this.mNode.mAutofillId;
        }

        public void setAutofillType(int type) {
            this.mNode.mAutofillType = type;
        }

        public void setAutofillHints(String[] hints) {
            this.mNode.mAutofillHints = hints;
        }

        public void setAutofillValue(AutofillValue value) {
            this.mNode.mAutofillValue = value;
        }

        public void setAutofillOptions(CharSequence[] options) {
            this.mNode.mAutofillOptions = options;
        }

        public void setInputType(int inputType) {
            this.mNode.mInputType = inputType;
        }

        public void setDataIsSensitive(boolean sensitive) {
            this.mNode.mSanitized = sensitive ^ 1;
        }

        public void setWebDomain(String domain) {
            if (domain == null) {
                this.mNode.mWebDomain = null;
                return;
            }
            this.mNode.mWebDomain = Uri.parse(domain).getHost();
        }

        public void setLocaleList(LocaleList localeList) {
            this.mNode.mLocaleList = localeList;
        }

        public Builder newHtmlInfoBuilder(String tagName) {
            return new HtmlInfoNodeBuilder(tagName);
        }

        public void setHtmlInfo(HtmlInfo htmlInfo) {
            this.mNode.mHtmlInfo = htmlInfo;
        }
    }

    static final class ViewNodeText {
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

        boolean isSimple() {
            if (this.mTextBackgroundColor == 1 && this.mTextSelectionStart == 0 && this.mTextSelectionEnd == 0 && this.mLineCharOffsets == null && this.mLineBaselines == null) {
                return this.mHint == null;
            } else {
                return false;
            }
        }

        ViewNodeText(Parcel in, boolean simple) {
            this.mText = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
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

        void writeToParcel(Parcel out, boolean simple, boolean writeSensitive) {
            TextUtils.writeToParcel(writeSensitive ? this.mText : ProxyInfo.LOCAL_EXCL_LIST, out, 0);
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

    static final class ViewStackEntry {
        int curChild;
        ViewNode node;
        int numChildren;

        ViewStackEntry() {
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
                    view.onProvideAutofillStructure(builder, (flags & 1) != 0 ? 1 : 0);
                } else {
                    view.onProvideStructure(builder);
                    builder.setAssistBlocked(true);
                    return;
                }
            }
            if (forAutoFill) {
                view.dispatchProvideAutofillStructure(builder, (flags & 1) != 0 ? 1 : 0);
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
            this.mTitle = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.mDisplayId = in.readInt();
            this.mRoot = new ViewNode(reader, 0);
        }

        void writeSelfToParcel(Parcel out, PooledStringWriter pwriter, float[] tmpMatrix) {
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

    public AssistStructure(Activity activity, boolean forAutoFill, int flags) {
        this.mWindowNodes = new ArrayList();
        this.mPendingAsyncChildren = new ArrayList();
        this.mTmpRect = new Rect();
        this.mSanitizeOnWrite = false;
        this.mHaveData = true;
        this.mActivityComponent = activity.getComponentName();
        this.mFlags = flags;
        ArrayList<ViewRootImpl> views = WindowManagerGlobal.getInstance().getRootViews(activity.getActivityToken());
        for (int i = 0; i < views.size(); i++) {
            this.mWindowNodes.add(new WindowNode(this, (ViewRootImpl) views.get(i), forAutoFill, flags));
        }
    }

    public AssistStructure() {
        this.mWindowNodes = new ArrayList();
        this.mPendingAsyncChildren = new ArrayList();
        this.mTmpRect = new Rect();
        this.mSanitizeOnWrite = false;
        this.mHaveData = true;
        this.mActivityComponent = null;
        this.mFlags = 0;
    }

    public AssistStructure(Parcel in) {
        boolean z = true;
        this.mWindowNodes = new ArrayList();
        this.mPendingAsyncChildren = new ArrayList();
        this.mTmpRect = new Rect();
        this.mSanitizeOnWrite = false;
        if (in.readInt() != 1) {
            z = false;
        }
        this.mIsHomeActivity = z;
        this.mReceiveChannel = in.readStrongBinder();
    }

    public void sanitizeForParceling(boolean sanitize) {
        this.mSanitizeOnWrite = sanitize;
    }

    public void dump(boolean showSensitive) {
        if (this.mActivityComponent == null) {
            Log.i(TAG, "dump(): calling ensureData() first");
            ensureData();
        }
        Log.i(TAG, "Activity: " + this.mActivityComponent.flattenToShortString());
        Log.i(TAG, "Sanitize on write: " + this.mSanitizeOnWrite);
        Log.i(TAG, "Flags: " + this.mFlags);
        int N = getWindowNodeCount();
        for (int i = 0; i < N; i++) {
            WindowNode node = getWindowNodeAt(i);
            Log.i(TAG, "Window #" + i + " [" + node.getLeft() + "," + node.getTop() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + node.getWidth() + "x" + node.getHeight() + "]" + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + node.getTitle());
            dump("  ", node.getRootViewNode(), showSensitive);
        }
    }

    void dump(String prefix, ViewNode node, boolean showSensitive) {
        Log.i(TAG, prefix + "View [" + node.getLeft() + "," + node.getTop() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + node.getWidth() + "x" + node.getHeight() + "]" + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + node.getClassName());
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
                sb.append(":");
                sb.append(type);
                sb.append("/");
                sb.append(entry);
            }
            Log.i(TAG, sb.toString());
        }
        int scrollX = node.getScrollX();
        int scrollY = node.getScrollY();
        if (!(scrollX == 0 && scrollY == 0)) {
            Log.i(TAG, prefix + "  Scroll: " + scrollX + "," + scrollY);
        }
        Matrix matrix = node.getTransformation();
        if (matrix != null) {
            Log.i(TAG, prefix + "  Transformation: " + matrix);
        }
        float elevation = node.getElevation();
        if (elevation != TonemapCurve.LEVEL_BLACK) {
            Log.i(TAG, prefix + "  Elevation: " + elevation);
        }
        if (node.getAlpha() != TonemapCurve.LEVEL_BLACK) {
            Log.i(TAG, prefix + "  Alpha: " + elevation);
        }
        CharSequence contentDescription = node.getContentDescription();
        if (contentDescription != null) {
            Log.i(TAG, prefix + "  Content description: " + contentDescription);
        }
        CharSequence text = node.getText();
        if (text != null) {
            String safeText;
            if (node.isSanitized() || showSensitive) {
                safeText = text.toString();
            } else {
                safeText = "REDACTED[" + text.length() + " chars]";
            }
            Log.i(TAG, prefix + "  Text (sel " + node.getTextSelectionStart() + "-" + node.getTextSelectionEnd() + "): " + safeText);
            Log.i(TAG, prefix + "  Text size: " + node.getTextSize() + " , style: #" + node.getTextStyle());
            Log.i(TAG, prefix + "  Text color fg: #" + Integer.toHexString(node.getTextColor()) + ", bg: #" + Integer.toHexString(node.getTextBackgroundColor()));
            Log.i(TAG, prefix + "  Input type: " + node.getInputType());
        }
        String webDomain = node.getWebDomain();
        if (webDomain != null) {
            Log.i(TAG, prefix + "  Web domain: " + webDomain);
        }
        HtmlInfo htmlInfo = node.getHtmlInfo();
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
            Log.i(TAG, prefix + "Autofill info: id= " + autofillId + ", type=" + node.getAutofillType() + ", options=" + Arrays.toString(node.getAutofillOptions()) + ", hints=" + Arrays.toString(node.getAutofillHints()) + ", value=" + node.getAutofillValue() + ", sanitized=" + node.isSanitized());
        }
        int NCHILDREN = node.getChildCount();
        if (NCHILDREN > 0) {
            Log.i(TAG, prefix + "  Children:");
            String cprefix = prefix + "    ";
            for (int i = 0; i < NCHILDREN; i++) {
                dump(cprefix, node.getChildAt(i), showSensitive);
            }
        }
    }

    public ComponentName getActivityComponent() {
        ensureData();
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
        return (WindowNode) this.mWindowNodes.get(index);
    }

    public void ensureData() {
        if (!this.mHaveData) {
            this.mHaveData = true;
            new ParcelTransferReader(this.mReceiveChannel).go();
        }
    }

    boolean waitForReady() {
        boolean skipStructure = false;
        synchronized (this) {
            long endTime = SystemClock.uptimeMillis() + 5000;
            while (this.mPendingAsyncChildren.size() > 0) {
                long now = SystemClock.uptimeMillis();
                if (now < endTime) {
                    try {
                        wait(endTime - now);
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (this.mPendingAsyncChildren.size() > 0) {
                Log.w(TAG, "Skipping assist structure, waiting too long for async children (have " + this.mPendingAsyncChildren.size() + " remaining");
                skipStructure = true;
            }
        }
        return skipStructure ^ 1;
    }

    public void clearSendChannel() {
        if (this.mSendChannel != null) {
            this.mSendChannel.mAssistStructure = null;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
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
