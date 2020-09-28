package android.view;

import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseIntArray;
import android.view.WindowInsets;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class InsetsState implements Parcelable {
    public static final Parcelable.Creator<InsetsState> CREATOR = new Parcelable.Creator<InsetsState>() {
        /* class android.view.InsetsState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InsetsState createFromParcel(Parcel in) {
            return new InsetsState(in);
        }

        @Override // android.os.Parcelable.Creator
        public InsetsState[] newArray(int size) {
            return new InsetsState[size];
        }
    };
    static final int FIRST_TYPE = 0;
    static final int INSET_SIDE_BOTTOM = 3;
    static final int INSET_SIDE_LEFT = 0;
    static final int INSET_SIDE_RIGHT = 2;
    static final int INSET_SIDE_TOP = 1;
    static final int INSET_SIDE_UNKNWON = 4;
    static final int LAST_TYPE = 10;
    public static final int TYPE_BOTTOM_GESTURES = 5;
    public static final int TYPE_BOTTOM_TAPPABLE_ELEMENT = 9;
    public static final int TYPE_IME = 10;
    public static final int TYPE_LEFT_GESTURES = 6;
    public static final int TYPE_NAVIGATION_BAR = 1;
    public static final int TYPE_RIGHT_GESTURES = 7;
    public static final int TYPE_SHELF = 1;
    public static final int TYPE_SIDE_BAR_1 = 1;
    public static final int TYPE_SIDE_BAR_2 = 2;
    public static final int TYPE_SIDE_BAR_3 = 3;
    public static final int TYPE_TOP_BAR = 0;
    public static final int TYPE_TOP_GESTURES = 4;
    public static final int TYPE_TOP_TAPPABLE_ELEMENT = 8;
    private final Rect mDisplayFrame = new Rect();
    private final ArrayMap<Integer, InsetsSource> mSources = new ArrayMap<>();

    @Retention(RetentionPolicy.SOURCE)
    public @interface InsetSide {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface InternalInsetType {
    }

    public InsetsState() {
    }

    public InsetsState(InsetsState copy) {
        set(copy);
    }

    public InsetsState(InsetsState copy, boolean copySources) {
        set(copy, copySources);
    }

    public WindowInsets calculateInsets(Rect frame, boolean isScreenRound, boolean alwaysConsumeSystemBars, DisplayCutout cutout, Rect legacyContentInsets, Rect legacyStableInsets, int legacySoftInputMode, SparseIntArray typeSideMap) {
        Insets[] typeInsetsMap = new Insets[7];
        Insets[] typeMaxInsetsMap = new Insets[7];
        boolean[] typeVisibilityMap = new boolean[7];
        Rect relativeFrame = new Rect(frame);
        Rect relativeFrameMax = new Rect(frame);
        if (!(ViewRootImpl.sNewInsetsMode == 2 || legacyContentInsets == null || legacyStableInsets == null)) {
            WindowInsets.assignCompatInsets(typeInsetsMap, legacyContentInsets);
            WindowInsets.assignCompatInsets(typeMaxInsetsMap, legacyStableInsets);
        }
        int type = 0;
        while (type <= 10) {
            InsetsSource source = this.mSources.get(Integer.valueOf(type));
            if (source != null) {
                boolean skipLegacyTypes = true;
                boolean skipSystemBars = ViewRootImpl.sNewInsetsMode != 2 && (type == 0 || type == 1);
                boolean skipIme = source.getType() == 10 && (legacySoftInputMode & 16) == 0;
                if (ViewRootImpl.sNewInsetsMode != 0 || (toPublicType(type) & WindowInsets.Type.compatSystemInsets()) == 0) {
                    skipLegacyTypes = false;
                }
                if (skipSystemBars || skipIme || skipLegacyTypes) {
                    typeVisibilityMap[WindowInsets.Type.indexOf(toPublicType(type))] = source.isVisible();
                } else {
                    processSource(source, relativeFrame, false, typeInsetsMap, typeSideMap, typeVisibilityMap);
                    if (source.getType() != 10) {
                        processSource(source, relativeFrameMax, true, typeMaxInsetsMap, null, null);
                    }
                }
            }
            type++;
        }
        return new WindowInsets(typeInsetsMap, typeMaxInsetsMap, typeVisibilityMap, isScreenRound, alwaysConsumeSystemBars, cutout);
    }

    private void processSource(InsetsSource source, Rect relativeFrame, boolean ignoreVisibility, Insets[] typeInsetsMap, SparseIntArray typeSideMap, boolean[] typeVisibilityMap) {
        Insets insets = source.calculateInsets(relativeFrame, ignoreVisibility);
        int type = toPublicType(source.getType());
        processSourceAsPublicType(source, typeInsetsMap, typeSideMap, typeVisibilityMap, insets, type);
        if (type == 16) {
            processSourceAsPublicType(source, typeInsetsMap, typeSideMap, typeVisibilityMap, insets, 8);
        }
    }

    private void processSourceAsPublicType(InsetsSource source, Insets[] typeInsetsMap, SparseIntArray typeSideMap, boolean[] typeVisibilityMap, Insets insets, int type) {
        int index = WindowInsets.Type.indexOf(type);
        Insets existing = typeInsetsMap[index];
        if (existing == null) {
            typeInsetsMap[index] = insets;
        } else {
            typeInsetsMap[index] = Insets.max(existing, insets);
        }
        if (typeVisibilityMap != null) {
            typeVisibilityMap[index] = source.isVisible();
        }
        if (typeSideMap != null && !Insets.NONE.equals(insets) && getInsetSide(insets) != 4) {
            typeSideMap.put(source.getType(), getInsetSide(insets));
        }
    }

    private int getInsetSide(Insets insets) {
        if (insets.left != 0) {
            return 0;
        }
        if (insets.top != 0) {
            return 1;
        }
        if (insets.right != 0) {
            return 2;
        }
        if (insets.bottom != 0) {
            return 3;
        }
        return 4;
    }

    public InsetsSource getSource(int type) {
        return this.mSources.computeIfAbsent(Integer.valueOf(type), $$Lambda$cZhmLzK8aetUdx4VlP9w5jR7En0.INSTANCE);
    }

    public void setDisplayFrame(Rect frame) {
        this.mDisplayFrame.set(frame);
    }

    public Rect getDisplayFrame() {
        return this.mDisplayFrame;
    }

    public void removeSource(int type) {
        this.mSources.remove(Integer.valueOf(type));
    }

    public void set(InsetsState other) {
        set(other, false);
    }

    public void set(InsetsState other, boolean copySources) {
        this.mDisplayFrame.set(other.mDisplayFrame);
        this.mSources.clear();
        if (copySources) {
            for (int i = 0; i < other.mSources.size(); i++) {
                InsetsSource source = other.mSources.valueAt(i);
                this.mSources.put(Integer.valueOf(source.getType()), new InsetsSource(source));
            }
            return;
        }
        this.mSources.putAll((ArrayMap<? extends Integer, ? extends InsetsSource>) other.mSources);
    }

    public void addSource(InsetsSource source) {
        this.mSources.put(Integer.valueOf(source.getType()), source);
    }

    public int getSourcesCount() {
        return this.mSources.size();
    }

    public InsetsSource sourceAt(int index) {
        return this.mSources.valueAt(index);
    }

    public static ArraySet<Integer> toInternalType(int insetTypes) {
        ArraySet<Integer> result = new ArraySet<>();
        if ((insetTypes & 1) != 0) {
            result.add(0);
        }
        if ((insetTypes & 4) != 0) {
            result.add(1);
            result.add(2);
            result.add(3);
        }
        if ((insetTypes & 2) != 0) {
            result.add(10);
        }
        return result;
    }

    static int toPublicType(int type) {
        switch (type) {
            case 0:
                return 1;
            case 1:
            case 2:
            case 3:
                return 4;
            case 4:
            case 5:
                return 16;
            case 6:
            case 7:
                return 8;
            case 8:
            case 9:
                return 32;
            case 10:
                return 2;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    public static boolean getDefaultVisibility(int type) {
        if (type == 0 || type == 1 || type == 2 || type == 3 || type != 10) {
            return true;
        }
        return false;
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + "InsetsState");
        for (int i = this.mSources.size() + -1; i >= 0; i += -1) {
            this.mSources.valueAt(i).dump(prefix + "  ", pw);
        }
    }

    public static String typeToString(int type) {
        switch (type) {
            case 0:
                return "TYPE_TOP_BAR";
            case 1:
                return "TYPE_SIDE_BAR_1";
            case 2:
                return "TYPE_SIDE_BAR_2";
            case 3:
                return "TYPE_SIDE_BAR_3";
            case 4:
                return "TYPE_TOP_GESTURES";
            case 5:
                return "TYPE_BOTTOM_GESTURES";
            case 6:
                return "TYPE_LEFT_GESTURES";
            case 7:
                return "TYPE_RIGHT_GESTURES";
            case 8:
                return "TYPE_TOP_TAPPABLE_ELEMENT";
            case 9:
                return "TYPE_BOTTOM_TAPPABLE_ELEMENT";
            default:
                return "TYPE_UNKNOWN_" + type;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InsetsState state = (InsetsState) o;
        if (!(this.mDisplayFrame.equals(state.mDisplayFrame) && this.mSources.size() == state.mSources.size())) {
            return false;
        }
        for (int i = this.mSources.size() - 1; i >= 0; i--) {
            InsetsSource source = this.mSources.valueAt(i);
            InsetsSource otherSource = state.mSources.get(Integer.valueOf(source.getType()));
            if (otherSource == null || !otherSource.equals(source)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mDisplayFrame, this.mSources);
    }

    public InsetsState(Parcel in) {
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mDisplayFrame, flags);
        dest.writeInt(this.mSources.size());
        for (int i = 0; i < this.mSources.size(); i++) {
            dest.writeParcelable(this.mSources.valueAt(i), flags);
        }
    }

    public void readFromParcel(Parcel in) {
        this.mSources.clear();
        this.mDisplayFrame.set((Rect) in.readParcelable(null));
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            InsetsSource source = (InsetsSource) in.readParcelable(null);
            this.mSources.put(Integer.valueOf(source.getType()), source);
        }
    }
}
