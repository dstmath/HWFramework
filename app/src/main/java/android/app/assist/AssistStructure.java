package android.app.assist;

import android.app.Activity;
import android.content.ComponentName;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PooledStringReader;
import android.os.PooledStringWriter;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.renderscript.Mesh.TriangleMeshBuilder;
import android.security.keymaster.KeymasterDefs;
import android.speech.tts.TextToSpeech.Engine;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.ViewStructure;
import android.view.WindowManagerGlobal;
import java.util.ArrayList;

public class AssistStructure implements Parcelable {
    public static final Creator<AssistStructure> CREATOR = null;
    static final boolean DEBUG_PARCEL = false;
    static final boolean DEBUG_PARCEL_CHILDREN = false;
    static final boolean DEBUG_PARCEL_TREE = false;
    static final String DESCRIPTOR = "android.app.AssistStructure";
    static final String TAG = "AssistStructure";
    static final int TRANSACTION_XFER = 2;
    static final int VALIDATE_VIEW_TOKEN = 572662306;
    static final int VALIDATE_WINDOW_TOKEN = 286331153;
    ComponentName mActivityComponent;
    boolean mHaveData;
    final ArrayList<ViewNodeBuilder> mPendingAsyncChildren;
    IBinder mReceiveChannel;
    SendChannel mSendChannel;
    Rect mTmpRect;
    final ArrayList<WindowNode> mWindowNodes;

    final class ParcelTransferReader {
        private final IBinder mChannel;
        private Parcel mCurParcel;
        int mNumReadViews;
        int mNumReadWindows;
        PooledStringReader mStringReader;
        final float[] mTmpMatrix;
        private IBinder mTransferToken;

        ParcelTransferReader(IBinder channel) {
            this.mTmpMatrix = new float[9];
            this.mChannel = channel;
        }

        void go() {
            fetchData();
            AssistStructure.this.mActivityComponent = ComponentName.readFromParcel(this.mCurParcel);
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
                this.mChannel.transact(AssistStructure.TRANSACTION_XFER, data, this.mCurParcel, 0);
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
        final float[] mTmpMatrix;
        final ArrayList<ViewStackEntry> mViewStack;
        final boolean mWriteStructure;

        ParcelTransferWriter(AssistStructure as, Parcel out) {
            this.mViewStack = new ArrayList();
            this.mTmpMatrix = new float[9];
            this.mWriteStructure = as.waitForReady();
            ComponentName.writeToParcel(as.mActivityComponent, out);
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
                return AssistStructure.DEBUG_PARCEL_TREE;
            }
            PooledStringWriter pwriter = new PooledStringWriter(out);
            while (writeNextEntryToParcel(as, out, pwriter)) {
                if (out.dataSize() > Root.FLAG_EMPTY) {
                    out.writeInt(0);
                    out.writeStrongBinder(this);
                    pwriter.finish();
                    return true;
                }
            }
            pwriter.finish();
            this.mViewStack.clear();
            return AssistStructure.DEBUG_PARCEL_TREE;
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
            int flags = child.writeSelfToParcel(out, pwriter, this.mTmpMatrix);
            this.mNumWrittenViews++;
            if ((Root.FLAG_REMOVABLE_USB & flags) != 0) {
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
                    return AssistStructure.DEBUG_PARCEL_TREE;
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
            if (code != AssistStructure.TRANSACTION_XFER) {
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
        static final int FLAGS_HAS_CHILDREN = 1048576;
        static final int FLAGS_HAS_COMPLEX_TEXT = 8388608;
        static final int FLAGS_HAS_CONTENT_DESCRIPTION = 33554432;
        static final int FLAGS_HAS_ELEVATION = 268435456;
        static final int FLAGS_HAS_EXTRAS = 4194304;
        static final int FLAGS_HAS_ID = 2097152;
        static final int FLAGS_HAS_LARGE_COORDS = 67108864;
        static final int FLAGS_HAS_MATRIX = 1073741824;
        static final int FLAGS_HAS_SCROLL = 134217728;
        static final int FLAGS_HAS_TEXT = 16777216;
        static final int FLAGS_LONG_CLICKABLE = 2048;
        static final int FLAGS_SELECTED = 64;
        static final int FLAGS_VISIBILITY_MASK = 12;
        public static final int TEXT_COLOR_UNDEFINED = 1;
        public static final int TEXT_STYLE_BOLD = 1;
        public static final int TEXT_STYLE_ITALIC = 2;
        public static final int TEXT_STYLE_STRIKE_THRU = 8;
        public static final int TEXT_STYLE_UNDERLINE = 4;
        float mAlpha;
        ViewNode[] mChildren;
        String mClassName;
        CharSequence mContentDescription;
        float mElevation;
        Bundle mExtras;
        int mFlags;
        int mHeight;
        int mId;
        String mIdEntry;
        String mIdPackage;
        String mIdType;
        Matrix mMatrix;
        int mScrollX;
        int mScrollY;
        ViewNodeText mText;
        int mWidth;
        int mX;
        int mY;

        ViewNode() {
            this.mId = -1;
            this.mAlpha = Engine.DEFAULT_VOLUME;
        }

        ViewNode(ParcelTransferReader reader, int nestingLevel) {
            this.mId = -1;
            this.mAlpha = Engine.DEFAULT_VOLUME;
            Parcel in = reader.readParcel(AssistStructure.VALIDATE_VIEW_TOKEN, nestingLevel);
            reader.mNumReadViews += TEXT_STYLE_BOLD;
            PooledStringReader preader = reader.mStringReader;
            this.mClassName = preader.readString();
            this.mFlags = in.readInt();
            int flags = this.mFlags;
            if ((FLAGS_HAS_ID & flags) != 0) {
                this.mId = in.readInt();
                if (this.mId != 0) {
                    this.mIdEntry = preader.readString();
                    if (this.mIdEntry != null) {
                        this.mIdType = preader.readString();
                        this.mIdPackage = preader.readString();
                    }
                }
            }
            if ((FLAGS_HAS_LARGE_COORDS & flags) != 0) {
                this.mX = in.readInt();
                this.mY = in.readInt();
                this.mWidth = in.readInt();
                this.mHeight = in.readInt();
            } else {
                int val = in.readInt();
                this.mX = val & 32767;
                this.mY = (val >> FLAGS_FOCUSABLE) & 32767;
                val = in.readInt();
                this.mWidth = val & 32767;
                this.mHeight = (val >> FLAGS_FOCUSABLE) & 32767;
            }
            if ((FLAGS_HAS_SCROLL & flags) != 0) {
                this.mScrollX = in.readInt();
                this.mScrollY = in.readInt();
            }
            if ((FLAGS_HAS_MATRIX & flags) != 0) {
                this.mMatrix = new Matrix();
                in.readFloatArray(reader.mTmpMatrix);
                this.mMatrix.setValues(reader.mTmpMatrix);
            }
            if ((FLAGS_HAS_ELEVATION & flags) != 0) {
                this.mElevation = in.readFloat();
            }
            if ((FLAGS_HAS_ALPHA & flags) != 0) {
                this.mAlpha = in.readFloat();
            }
            if ((FLAGS_HAS_CONTENT_DESCRIPTION & flags) != 0) {
                this.mContentDescription = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            }
            if ((FLAGS_HAS_TEXT & flags) != 0) {
                boolean z;
                if ((FLAGS_HAS_COMPLEX_TEXT & flags) == 0) {
                    z = true;
                } else {
                    z = AssistStructure.DEBUG_PARCEL_TREE;
                }
                this.mText = new ViewNodeText(in, z);
            }
            if ((FLAGS_HAS_EXTRAS & flags) != 0) {
                this.mExtras = in.readBundle();
            }
            if ((FLAGS_HAS_CHILDREN & flags) != 0) {
                int NCHILDREN = in.readInt();
                this.mChildren = new ViewNode[NCHILDREN];
                for (int i = 0; i < NCHILDREN; i += TEXT_STYLE_BOLD) {
                    this.mChildren[i] = new ViewNode(reader, nestingLevel + TEXT_STYLE_BOLD);
                }
            }
        }

        int writeSelfToParcel(Parcel out, PooledStringWriter pwriter, float[] tmpMatrix) {
            boolean z = true;
            int flags = this.mFlags & 1048575;
            if (this.mId != -1) {
                flags |= FLAGS_HAS_ID;
            }
            if ((this.mX & -32768) == 0 && (this.mY & -32768) == 0) {
                int i;
                int i2;
                if ((this.mWidth & -32768) != 0) {
                    i = TEXT_STYLE_BOLD;
                } else {
                    i = 0;
                }
                if ((this.mHeight & -32768) != 0) {
                    i2 = TEXT_STYLE_BOLD;
                } else {
                    i2 = 0;
                }
                if ((i | i2) != 0) {
                }
                if (!(this.mScrollX == 0 && this.mScrollY == 0)) {
                    flags |= FLAGS_HAS_SCROLL;
                }
                if (this.mMatrix != null) {
                    flags |= FLAGS_HAS_MATRIX;
                }
                if (this.mElevation != 0.0f) {
                    flags |= FLAGS_HAS_ELEVATION;
                }
                if (this.mAlpha != Engine.DEFAULT_VOLUME) {
                    flags |= FLAGS_HAS_ALPHA;
                }
                if (this.mContentDescription != null) {
                    flags |= FLAGS_HAS_CONTENT_DESCRIPTION;
                }
                if (this.mText != null) {
                    flags |= FLAGS_HAS_TEXT;
                    if (!this.mText.isSimple()) {
                        flags |= FLAGS_HAS_COMPLEX_TEXT;
                    }
                }
                if (this.mExtras != null) {
                    flags |= FLAGS_HAS_EXTRAS;
                }
                if (this.mChildren != null) {
                    flags |= FLAGS_HAS_CHILDREN;
                }
                pwriter.writeString(this.mClassName);
                out.writeInt(flags);
                if ((flags & FLAGS_HAS_ID) != 0) {
                    out.writeInt(this.mId);
                    if (this.mId != 0) {
                        pwriter.writeString(this.mIdEntry);
                        if (this.mIdEntry != null) {
                            pwriter.writeString(this.mIdType);
                            pwriter.writeString(this.mIdPackage);
                        }
                    }
                }
                if ((FLAGS_HAS_LARGE_COORDS & flags) == 0) {
                    out.writeInt(this.mX);
                    out.writeInt(this.mY);
                    out.writeInt(this.mWidth);
                    out.writeInt(this.mHeight);
                } else {
                    out.writeInt((this.mY << FLAGS_FOCUSABLE) | this.mX);
                    out.writeInt((this.mHeight << FLAGS_FOCUSABLE) | this.mWidth);
                }
                if ((FLAGS_HAS_SCROLL & flags) != 0) {
                    out.writeInt(this.mScrollX);
                    out.writeInt(this.mScrollY);
                }
                if ((FLAGS_HAS_MATRIX & flags) != 0) {
                    this.mMatrix.getValues(tmpMatrix);
                    out.writeFloatArray(tmpMatrix);
                }
                if ((FLAGS_HAS_ELEVATION & flags) != 0) {
                    out.writeFloat(this.mElevation);
                }
                if ((FLAGS_HAS_ALPHA & flags) != 0) {
                    out.writeFloat(this.mAlpha);
                }
                if ((FLAGS_HAS_CONTENT_DESCRIPTION & flags) != 0) {
                    TextUtils.writeToParcel(this.mContentDescription, out, 0);
                }
                if ((FLAGS_HAS_TEXT & flags) != 0) {
                    ViewNodeText viewNodeText = this.mText;
                    if ((FLAGS_HAS_COMPLEX_TEXT & flags) != 0) {
                        z = AssistStructure.DEBUG_PARCEL_TREE;
                    }
                    viewNodeText.writeToParcel(out, z);
                }
                if ((flags & FLAGS_HAS_EXTRAS) != 0) {
                    out.writeBundle(this.mExtras);
                }
                return flags;
            }
            flags |= FLAGS_HAS_LARGE_COORDS;
            flags |= FLAGS_HAS_SCROLL;
            if (this.mMatrix != null) {
                flags |= FLAGS_HAS_MATRIX;
            }
            if (this.mElevation != 0.0f) {
                flags |= FLAGS_HAS_ELEVATION;
            }
            if (this.mAlpha != Engine.DEFAULT_VOLUME) {
                flags |= FLAGS_HAS_ALPHA;
            }
            if (this.mContentDescription != null) {
                flags |= FLAGS_HAS_CONTENT_DESCRIPTION;
            }
            if (this.mText != null) {
                flags |= FLAGS_HAS_TEXT;
                if (this.mText.isSimple()) {
                    flags |= FLAGS_HAS_COMPLEX_TEXT;
                }
            }
            if (this.mExtras != null) {
                flags |= FLAGS_HAS_EXTRAS;
            }
            if (this.mChildren != null) {
                flags |= FLAGS_HAS_CHILDREN;
            }
            pwriter.writeString(this.mClassName);
            out.writeInt(flags);
            if ((flags & FLAGS_HAS_ID) != 0) {
                out.writeInt(this.mId);
                if (this.mId != 0) {
                    pwriter.writeString(this.mIdEntry);
                    if (this.mIdEntry != null) {
                        pwriter.writeString(this.mIdType);
                        pwriter.writeString(this.mIdPackage);
                    }
                }
            }
            if ((FLAGS_HAS_LARGE_COORDS & flags) == 0) {
                out.writeInt((this.mY << FLAGS_FOCUSABLE) | this.mX);
                out.writeInt((this.mHeight << FLAGS_FOCUSABLE) | this.mWidth);
            } else {
                out.writeInt(this.mX);
                out.writeInt(this.mY);
                out.writeInt(this.mWidth);
                out.writeInt(this.mHeight);
            }
            if ((FLAGS_HAS_SCROLL & flags) != 0) {
                out.writeInt(this.mScrollX);
                out.writeInt(this.mScrollY);
            }
            if ((FLAGS_HAS_MATRIX & flags) != 0) {
                this.mMatrix.getValues(tmpMatrix);
                out.writeFloatArray(tmpMatrix);
            }
            if ((FLAGS_HAS_ELEVATION & flags) != 0) {
                out.writeFloat(this.mElevation);
            }
            if ((FLAGS_HAS_ALPHA & flags) != 0) {
                out.writeFloat(this.mAlpha);
            }
            if ((FLAGS_HAS_CONTENT_DESCRIPTION & flags) != 0) {
                TextUtils.writeToParcel(this.mContentDescription, out, 0);
            }
            if ((FLAGS_HAS_TEXT & flags) != 0) {
                ViewNodeText viewNodeText2 = this.mText;
                if ((FLAGS_HAS_COMPLEX_TEXT & flags) != 0) {
                    z = AssistStructure.DEBUG_PARCEL_TREE;
                }
                viewNodeText2.writeToParcel(out, z);
            }
            if ((flags & FLAGS_HAS_EXTRAS) != 0) {
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
            return this.mFlags & FLAGS_VISIBILITY_MASK;
        }

        public boolean isAssistBlocked() {
            return (this.mFlags & FLAGS_ASSIST_BLOCKED) != 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public boolean isEnabled() {
            return (this.mFlags & TEXT_STYLE_BOLD) == 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public boolean isClickable() {
            return (this.mFlags & FLAGS_CLICKABLE) != 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public boolean isFocusable() {
            return (this.mFlags & FLAGS_FOCUSABLE) != 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public boolean isFocused() {
            return (this.mFlags & FLAGS_FOCUSED) != 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public boolean isAccessibilityFocused() {
            return (this.mFlags & FLAGS_ACCESSIBILITY_FOCUSED) != 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public boolean isCheckable() {
            return (this.mFlags & FLAGS_CHECKABLE) != 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public boolean isChecked() {
            return (this.mFlags & FLAGS_CHECKED) != 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public boolean isSelected() {
            return (this.mFlags & FLAGS_SELECTED) != 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public boolean isActivated() {
            return (this.mFlags & FLAGS_ACTIVATED) != 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public boolean isLongClickable() {
            return (this.mFlags & FLAGS_LONG_CLICKABLE) != 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public boolean isContextClickable() {
            return (this.mFlags & FLAGS_CONTEXT_CLICKABLE) != 0 ? true : AssistStructure.DEBUG_PARCEL_TREE;
        }

        public String getClassName() {
            return this.mClassName;
        }

        public CharSequence getContentDescription() {
            return this.mContentDescription;
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
            return this.mText != null ? this.mText.mTextColor : TEXT_STYLE_BOLD;
        }

        public int getTextBackgroundColor() {
            return this.mText != null ? this.mText.mTextBackgroundColor : TEXT_STYLE_BOLD;
        }

        public float getTextSize() {
            return this.mText != null ? this.mText.mTextSize : 0.0f;
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
            this.mNode.mFlags = (state ? KeymasterDefs.KM_ALGORITHM_HMAC : 0) | (this.mNode.mFlags & -129);
        }

        public void setEnabled(boolean state) {
            this.mNode.mFlags = (state ? 0 : 1) | (this.mNode.mFlags & -2);
        }

        public void setClickable(boolean state) {
            this.mNode.mFlags = (state ? Document.FLAG_SUPPORTS_REMOVE : 0) | (this.mNode.mFlags & -1025);
        }

        public void setLongClickable(boolean state) {
            this.mNode.mFlags = (state ? Process.PROC_CHAR : 0) | (this.mNode.mFlags & -2049);
        }

        public void setContextClickable(boolean state) {
            this.mNode.mFlags = (state ? Process.PROC_OUT_FLOAT : 0) | (this.mNode.mFlags & -16385);
        }

        public void setFocusable(boolean state) {
            this.mNode.mFlags = (state ? 16 : 0) | (this.mNode.mFlags & -17);
        }

        public void setFocused(boolean state) {
            this.mNode.mFlags = (state ? 32 : 0) | (this.mNode.mFlags & -33);
        }

        public void setAccessibilityFocused(boolean state) {
            this.mNode.mFlags = (state ? StrictMode.DETECT_VM_REGISTRATION_LEAKS : 0) | (this.mNode.mFlags & -4097);
        }

        public void setCheckable(boolean state) {
            this.mNode.mFlags = (state ? TriangleMeshBuilder.TEXTURE_0 : 0) | (this.mNode.mFlags & -257);
        }

        public void setChecked(boolean state) {
            this.mNode.mFlags = (state ? Document.FLAG_VIRTUAL_DOCUMENT : 0) | (this.mNode.mFlags & -513);
        }

        public void setSelected(boolean state) {
            this.mNode.mFlags = (state ? 64 : 0) | (this.mNode.mFlags & -65);
        }

        public void setActivated(boolean state) {
            this.mNode.mFlags = (state ? Process.PROC_OUT_LONG : 0) | (this.mNode.mFlags & -8193);
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
            t.mText = text;
            t.mTextSelectionEnd = -1;
            t.mTextSelectionStart = -1;
        }

        public void setText(CharSequence text, int selectionStart, int selectionEnd) {
            ViewNodeText t = getNodeText();
            t.mText = text;
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
            return this.mNode.mExtras != null ? true : AssistStructure.DEBUG_PARCEL_TREE;
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
            return new ViewNodeBuilder(this.mAssist, node, AssistStructure.DEBUG_PARCEL_TREE);
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
    }

    static final class ViewNodeText {
        String mHint;
        int[] mLineBaselines;
        int[] mLineCharOffsets;
        CharSequence mText;
        int mTextBackgroundColor;
        int mTextColor;
        int mTextSelectionEnd;
        int mTextSelectionStart;
        float mTextSize;
        int mTextStyle;

        ViewNodeText() {
            this.mTextColor = 1;
            this.mTextBackgroundColor = 1;
        }

        boolean isSimple() {
            if (this.mTextBackgroundColor == 1 && this.mTextSelectionStart == 0 && this.mTextSelectionEnd == 0 && this.mLineCharOffsets == null && this.mLineBaselines == null) {
                return this.mHint == null ? true : AssistStructure.DEBUG_PARCEL_TREE;
            } else {
                return AssistStructure.DEBUG_PARCEL_TREE;
            }
        }

        ViewNodeText(Parcel in, boolean simple) {
            this.mTextColor = 1;
            this.mTextBackgroundColor = 1;
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

        void writeToParcel(Parcel out, boolean simple) {
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

        WindowNode(AssistStructure assist, ViewRootImpl root) {
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
            ViewNodeBuilder builder = new ViewNodeBuilder(assist, this.mRoot, AssistStructure.DEBUG_PARCEL_TREE);
            if ((root.getWindowFlags() & Process.PROC_OUT_LONG) != 0) {
                view.onProvideStructure(builder);
                builder.setAssistBlocked(true);
                return;
            }
            view.dispatchProvideStructure(builder);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.assist.AssistStructure.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.assist.AssistStructure.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.assist.AssistStructure.<clinit>():void");
    }

    public AssistStructure(Activity activity) {
        this.mWindowNodes = new ArrayList();
        this.mPendingAsyncChildren = new ArrayList();
        this.mTmpRect = new Rect();
        this.mHaveData = true;
        this.mActivityComponent = activity.getComponentName();
        ArrayList<ViewRootImpl> views = WindowManagerGlobal.getInstance().getRootViews(activity.getActivityToken());
        for (int i = 0; i < views.size(); i++) {
            this.mWindowNodes.add(new WindowNode(this, (ViewRootImpl) views.get(i)));
        }
    }

    public AssistStructure() {
        this.mWindowNodes = new ArrayList();
        this.mPendingAsyncChildren = new ArrayList();
        this.mTmpRect = new Rect();
        this.mHaveData = true;
        this.mActivityComponent = null;
    }

    public AssistStructure(Parcel in) {
        this.mWindowNodes = new ArrayList();
        this.mPendingAsyncChildren = new ArrayList();
        this.mTmpRect = new Rect();
        this.mReceiveChannel = in.readStrongBinder();
    }

    public void dump() {
        Log.i(TAG, "Activity: " + this.mActivityComponent.flattenToShortString());
        int N = getWindowNodeCount();
        for (int i = 0; i < N; i++) {
            WindowNode node = getWindowNodeAt(i);
            Log.i(TAG, "Window #" + i + " [" + node.getLeft() + "," + node.getTop() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + node.getWidth() + "x" + node.getHeight() + "]" + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + node.getTitle());
            dump("  ", node.getRootViewNode());
        }
    }

    void dump(String prefix, ViewNode node) {
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
        if (elevation != 0.0f) {
            Log.i(TAG, prefix + "  Elevation: " + elevation);
        }
        if (node.getAlpha() != 0.0f) {
            Log.i(TAG, prefix + "  Alpha: " + elevation);
        }
        CharSequence contentDescription = node.getContentDescription();
        if (contentDescription != null) {
            Log.i(TAG, prefix + "  Content description: " + contentDescription);
        }
        CharSequence text = node.getText();
        if (text != null) {
            Log.i(TAG, prefix + "  Text (sel " + node.getTextSelectionStart() + "-" + node.getTextSelectionEnd() + "): " + text);
            Log.i(TAG, prefix + "  Text size: " + node.getTextSize() + " , style: #" + node.getTextStyle());
            Log.i(TAG, prefix + "  Text color fg: #" + Integer.toHexString(node.getTextColor()) + ", bg: #" + Integer.toHexString(node.getTextBackgroundColor()));
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
        int NCHILDREN = node.getChildCount();
        if (NCHILDREN > 0) {
            Log.i(TAG, prefix + "  Children:");
            String cprefix = prefix + "    ";
            for (int i = 0; i < NCHILDREN; i++) {
                dump(cprefix, node.getChildAt(i));
            }
        }
    }

    public ComponentName getActivityComponent() {
        ensureData();
        return this.mActivityComponent;
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
        boolean skipStructure = DEBUG_PARCEL_TREE;
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
        if (skipStructure) {
            return DEBUG_PARCEL_TREE;
        }
        return true;
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
