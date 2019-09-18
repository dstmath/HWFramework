package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Paint;
import android.net.wifi.WifiEnterpriseConfig;
import android.telecom.Logging.Session;
import android.util.AttributeSet;
import android.util.LogPrinter;
import android.util.Pair;
import android.util.Printer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RemoteViews.RemoteView
public class GridLayout extends ViewGroup {
    private static final int ALIGNMENT_MODE = 6;
    public static final int ALIGN_BOUNDS = 0;
    public static final int ALIGN_MARGINS = 1;
    public static final Alignment BASELINE = new Alignment() {
        /* access modifiers changed from: package-private */
        public int getGravityOffset(View view, int cellDelta) {
            return 0;
        }

        public int getAlignmentValue(View view, int viewSize, int mode) {
            if (view.getVisibility() == 8) {
                return 0;
            }
            int baseline = view.getBaseline();
            return baseline == -1 ? Integer.MIN_VALUE : baseline;
        }

        public Bounds getBounds() {
            return new Bounds() {
                private int size;

                /* access modifiers changed from: protected */
                public void reset() {
                    super.reset();
                    this.size = Integer.MIN_VALUE;
                }

                /* access modifiers changed from: protected */
                public void include(int before, int after) {
                    super.include(before, after);
                    this.size = Math.max(this.size, before + after);
                }

                /* access modifiers changed from: protected */
                public int size(boolean min) {
                    return Math.max(super.size(min), this.size);
                }

                /* access modifiers changed from: protected */
                public int getOffset(GridLayout gl, View c, Alignment a, int size2, boolean hrz) {
                    return Math.max(0, super.getOffset(gl, c, a, size2, hrz));
                }
            };
        }
    };
    public static final Alignment BOTTOM = TRAILING;
    private static final int CAN_STRETCH = 2;
    public static final Alignment CENTER = new Alignment() {
        /* access modifiers changed from: package-private */
        public int getGravityOffset(View view, int cellDelta) {
            return cellDelta >> 1;
        }

        public int getAlignmentValue(View view, int viewSize, int mode) {
            return viewSize >> 1;
        }
    };
    private static final int COLUMN_COUNT = 3;
    private static final int COLUMN_ORDER_PRESERVED = 4;
    private static final int DEFAULT_ALIGNMENT_MODE = 1;
    static final int DEFAULT_CONTAINER_MARGIN = 0;
    private static final int DEFAULT_COUNT = Integer.MIN_VALUE;
    private static final boolean DEFAULT_ORDER_PRESERVED = true;
    private static final int DEFAULT_ORIENTATION = 0;
    private static final boolean DEFAULT_USE_DEFAULT_MARGINS = false;
    public static final Alignment END = TRAILING;
    public static final Alignment FILL = new Alignment() {
        /* access modifiers changed from: package-private */
        public int getGravityOffset(View view, int cellDelta) {
            return 0;
        }

        public int getAlignmentValue(View view, int viewSize, int mode) {
            return Integer.MIN_VALUE;
        }

        public int getSizeInCell(View view, int viewSize, int cellSize) {
            return cellSize;
        }
    };
    public static final int HORIZONTAL = 0;
    private static final int INFLEXIBLE = 0;
    private static final Alignment LEADING = new Alignment() {
        /* access modifiers changed from: package-private */
        public int getGravityOffset(View view, int cellDelta) {
            return 0;
        }

        public int getAlignmentValue(View view, int viewSize, int mode) {
            return 0;
        }
    };
    public static final Alignment LEFT = createSwitchingAlignment(START, END);
    static final Printer LOG_PRINTER = new LogPrinter(3, GridLayout.class.getName());
    static final int MAX_SIZE = 100000;
    static final Printer NO_PRINTER = new Printer() {
        public void println(String x) {
        }
    };
    private static final int ORIENTATION = 0;
    public static final Alignment RIGHT = createSwitchingAlignment(END, START);
    private static final int ROW_COUNT = 1;
    private static final int ROW_ORDER_PRESERVED = 2;
    public static final Alignment START = LEADING;
    public static final Alignment TOP = LEADING;
    private static final Alignment TRAILING = new Alignment() {
        /* access modifiers changed from: package-private */
        public int getGravityOffset(View view, int cellDelta) {
            return cellDelta;
        }

        public int getAlignmentValue(View view, int viewSize, int mode) {
            return viewSize;
        }
    };
    public static final int UNDEFINED = Integer.MIN_VALUE;
    static final Alignment UNDEFINED_ALIGNMENT = new Alignment() {
        /* access modifiers changed from: package-private */
        public int getGravityOffset(View view, int cellDelta) {
            return Integer.MIN_VALUE;
        }

        public int getAlignmentValue(View view, int viewSize, int mode) {
            return Integer.MIN_VALUE;
        }
    };
    static final int UNINITIALIZED_HASH = 0;
    private static final int USE_DEFAULT_MARGINS = 5;
    public static final int VERTICAL = 1;
    int mAlignmentMode;
    int mDefaultGap;
    final Axis mHorizontalAxis;
    int mLastLayoutParamsHashCode;
    int mOrientation;
    Printer mPrinter;
    boolean mUseDefaultMargins;
    final Axis mVerticalAxis;

    public static abstract class Alignment {
        /* access modifiers changed from: package-private */
        public abstract int getAlignmentValue(View view, int i, int i2);

        /* access modifiers changed from: package-private */
        public abstract int getGravityOffset(View view, int i);

        Alignment() {
        }

        /* access modifiers changed from: package-private */
        public int getSizeInCell(View view, int viewSize, int cellSize) {
            return viewSize;
        }

        /* access modifiers changed from: package-private */
        public Bounds getBounds() {
            return new Bounds();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AlignmentMode {
    }

    static final class Arc {
        public final Interval span;
        public boolean valid = true;
        public final MutableInt value;

        public Arc(Interval span2, MutableInt value2) {
            this.span = span2;
            this.value = value2;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.span);
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(!this.valid ? "+>" : Session.SUBSESSION_SEPARATION_CHAR);
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(this.value);
            return sb.toString();
        }
    }

    static final class Assoc<K, V> extends ArrayList<Pair<K, V>> {
        private final Class<K> keyType;
        private final Class<V> valueType;

        private Assoc(Class<K> keyType2, Class<V> valueType2) {
            this.keyType = keyType2;
            this.valueType = valueType2;
        }

        public static <K, V> Assoc<K, V> of(Class<K> keyType2, Class<V> valueType2) {
            return new Assoc<>(keyType2, valueType2);
        }

        public void put(K key, V value) {
            add(Pair.create(key, value));
        }

        public PackedMap<K, V> pack() {
            int N = size();
            Object[] objArr = (Object[]) Array.newInstance(this.keyType, N);
            Object[] objArr2 = (Object[]) Array.newInstance(this.valueType, N);
            for (int i = 0; i < N; i++) {
                objArr[i] = ((Pair) get(i)).first;
                objArr2[i] = ((Pair) get(i)).second;
            }
            return new PackedMap<>(objArr, objArr2);
        }
    }

    final class Axis {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final int COMPLETE = 2;
        private static final int NEW = 0;
        private static final int PENDING = 1;
        public Arc[] arcs;
        public boolean arcsValid;
        PackedMap<Interval, MutableInt> backwardLinks;
        public boolean backwardLinksValid;
        public int definedCount;
        public int[] deltas;
        PackedMap<Interval, MutableInt> forwardLinks;
        public boolean forwardLinksValid;
        PackedMap<Spec, Bounds> groupBounds;
        public boolean groupBoundsValid;
        public boolean hasWeights;
        public boolean hasWeightsValid;
        public final boolean horizontal;
        public int[] leadingMargins;
        public boolean leadingMarginsValid;
        public int[] locations;
        public boolean locationsValid;
        private int maxIndex;
        boolean orderPreserved;
        private MutableInt parentMax;
        private MutableInt parentMin;
        public int[] trailingMargins;
        public boolean trailingMarginsValid;

        static {
            Class<GridLayout> cls = GridLayout.class;
        }

        private Axis(boolean horizontal2) {
            this.definedCount = Integer.MIN_VALUE;
            this.maxIndex = Integer.MIN_VALUE;
            this.groupBoundsValid = false;
            this.forwardLinksValid = false;
            this.backwardLinksValid = false;
            this.leadingMarginsValid = false;
            this.trailingMarginsValid = false;
            this.arcsValid = false;
            this.locationsValid = false;
            this.hasWeightsValid = false;
            this.orderPreserved = true;
            this.parentMin = new MutableInt(0);
            this.parentMax = new MutableInt(-100000);
            this.horizontal = horizontal2;
        }

        private int calculateMaxIndex() {
            int result = -1;
            int N = GridLayout.this.getChildCount();
            for (int i = 0; i < N; i++) {
                LayoutParams params = GridLayout.this.getLayoutParams(GridLayout.this.getChildAt(i));
                Interval span = (this.horizontal ? params.columnSpec : params.rowSpec).span;
                result = Math.max(Math.max(Math.max(result, span.min), span.max), span.size());
            }
            if (result == -1) {
                return Integer.MIN_VALUE;
            }
            return result;
        }

        private int getMaxIndex() {
            if (this.maxIndex == Integer.MIN_VALUE) {
                this.maxIndex = Math.max(0, calculateMaxIndex());
            }
            return this.maxIndex;
        }

        public int getCount() {
            return Math.max(this.definedCount, getMaxIndex());
        }

        public void setCount(int count) {
            if (count != Integer.MIN_VALUE && count < getMaxIndex()) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.horizontal ? "column" : "row");
                sb.append("Count must be greater than or equal to the maximum of all grid indices (and spans) defined in the LayoutParams of each child");
                GridLayout.handleInvalidParams(sb.toString());
            }
            this.definedCount = count;
        }

        public boolean isOrderPreserved() {
            return this.orderPreserved;
        }

        public void setOrderPreserved(boolean orderPreserved2) {
            this.orderPreserved = orderPreserved2;
            invalidateStructure();
        }

        private PackedMap<Spec, Bounds> createGroupBounds() {
            Assoc<Spec, Bounds> assoc = Assoc.of(Spec.class, Bounds.class);
            int N = GridLayout.this.getChildCount();
            for (int i = 0; i < N; i++) {
                LayoutParams lp = GridLayout.this.getLayoutParams(GridLayout.this.getChildAt(i));
                Spec spec = this.horizontal ? lp.columnSpec : lp.rowSpec;
                assoc.put(spec, spec.getAbsoluteAlignment(this.horizontal).getBounds());
            }
            return assoc.pack();
        }

        private void computeGroupBounds() {
            Bounds[] values = (Bounds[]) this.groupBounds.values;
            for (Bounds reset : values) {
                reset.reset();
            }
            int N = GridLayout.this.getChildCount();
            for (int i = 0; i < N; i++) {
                View c = GridLayout.this.getChildAt(i);
                LayoutParams lp = GridLayout.this.getLayoutParams(c);
                Spec spec = this.horizontal ? lp.columnSpec : lp.rowSpec;
                this.groupBounds.getValue(i).include(GridLayout.this, c, spec, this, GridLayout.this.getMeasurementIncludingMargin(c, this.horizontal) + (spec.weight == 0.0f ? 0 : getDeltas()[i]));
            }
        }

        public PackedMap<Spec, Bounds> getGroupBounds() {
            if (this.groupBounds == null) {
                this.groupBounds = createGroupBounds();
            }
            if (!this.groupBoundsValid) {
                computeGroupBounds();
                this.groupBoundsValid = true;
            }
            return this.groupBounds;
        }

        private PackedMap<Interval, MutableInt> createLinks(boolean min) {
            Assoc<Interval, MutableInt> result = Assoc.of(Interval.class, MutableInt.class);
            Spec[] keys = (Spec[]) getGroupBounds().keys;
            int N = keys.length;
            for (int i = 0; i < N; i++) {
                result.put(min ? keys[i].span : keys[i].span.inverse(), new MutableInt());
            }
            return result.pack();
        }

        private void computeLinks(PackedMap<Interval, MutableInt> links, boolean min) {
            MutableInt[] spans = (MutableInt[]) links.values;
            for (MutableInt reset : spans) {
                reset.reset();
            }
            Bounds[] bounds = (Bounds[]) getGroupBounds().values;
            for (int i = 0; i < bounds.length; i++) {
                int size = bounds[i].size(min);
                MutableInt valueHolder = links.getValue(i);
                valueHolder.value = Math.max(valueHolder.value, min ? size : -size);
            }
        }

        private PackedMap<Interval, MutableInt> getForwardLinks() {
            if (this.forwardLinks == null) {
                this.forwardLinks = createLinks(true);
            }
            if (!this.forwardLinksValid) {
                computeLinks(this.forwardLinks, true);
                this.forwardLinksValid = true;
            }
            return this.forwardLinks;
        }

        private PackedMap<Interval, MutableInt> getBackwardLinks() {
            if (this.backwardLinks == null) {
                this.backwardLinks = createLinks(false);
            }
            if (!this.backwardLinksValid) {
                computeLinks(this.backwardLinks, false);
                this.backwardLinksValid = true;
            }
            return this.backwardLinks;
        }

        private void include(List<Arc> arcs2, Interval key, MutableInt size, boolean ignoreIfAlreadyPresent) {
            if (key.size() != 0) {
                if (ignoreIfAlreadyPresent) {
                    for (Arc arc : arcs2) {
                        if (arc.span.equals(key)) {
                            return;
                        }
                    }
                }
                arcs2.add(new Arc(key, size));
            }
        }

        private void include(List<Arc> arcs2, Interval key, MutableInt size) {
            include(arcs2, key, size, true);
        }

        /* access modifiers changed from: package-private */
        public Arc[][] groupArcsByFirstVertex(Arc[] arcs2) {
            int N = getCount() + 1;
            Arc[][] result = new Arc[N][];
            int[] sizes = new int[N];
            for (Arc arc : arcs2) {
                int i = arc.span.min;
                sizes[i] = sizes[i] + 1;
            }
            for (int i2 = 0; i2 < sizes.length; i2++) {
                result[i2] = new Arc[sizes[i2]];
            }
            Arrays.fill(sizes, 0);
            for (Arc arc2 : arcs2) {
                int i3 = arc2.span.min;
                Arc[] arcArr = result[i3];
                int i4 = sizes[i3];
                sizes[i3] = i4 + 1;
                arcArr[i4] = arc2;
            }
            return result;
        }

        private Arc[] topologicalSort(final Arc[] arcs2) {
            return new Object() {
                static final /* synthetic */ boolean $assertionsDisabled = false;
                Arc[][] arcsByVertex = Axis.this.groupArcsByFirstVertex(arcs2);
                int cursor = (this.result.length - 1);
                Arc[] result = new Arc[arcs2.length];
                int[] visited = new int[(Axis.this.getCount() + 1)];

                static {
                    Class<GridLayout> cls = GridLayout.class;
                }

                /* access modifiers changed from: package-private */
                public void walk(int loc) {
                    int i;
                    switch (this.visited[loc]) {
                        case 0:
                            this.visited[loc] = 1;
                            for (Arc arc : this.arcsByVertex[loc]) {
                                walk(arc.span.max);
                                Arc[] arcArr = this.result;
                                this.cursor = this.cursor - 1;
                                arcArr[i] = arc;
                            }
                            this.visited[loc] = 2;
                            return;
                        default:
                            return;
                    }
                }

                /* access modifiers changed from: package-private */
                public Arc[] sort() {
                    int N = this.arcsByVertex.length;
                    for (int loc = 0; loc < N; loc++) {
                        walk(loc);
                    }
                    return this.result;
                }
            }.sort();
        }

        private Arc[] topologicalSort(List<Arc> arcs2) {
            return topologicalSort((Arc[]) arcs2.toArray(new Arc[arcs2.size()]));
        }

        private void addComponentSizes(List<Arc> result, PackedMap<Interval, MutableInt> links) {
            for (int i = 0; i < ((Interval[]) links.keys).length; i++) {
                include(result, ((Interval[]) links.keys)[i], ((MutableInt[]) links.values)[i], false);
            }
        }

        private Arc[] createArcs() {
            List<Arc> mins = new ArrayList<>();
            List<Arc> maxs = new ArrayList<>();
            addComponentSizes(mins, getForwardLinks());
            addComponentSizes(maxs, getBackwardLinks());
            if (this.orderPreserved) {
                for (int i = 0; i < getCount(); i++) {
                    include(mins, new Interval(i, i + 1), new MutableInt(0));
                }
            }
            int i2 = getCount();
            include(mins, new Interval(0, i2), this.parentMin, false);
            include(maxs, new Interval(i2, 0), this.parentMax, false);
            return (Arc[]) GridLayout.append(topologicalSort(mins), topologicalSort(maxs));
        }

        private void computeArcs() {
            getForwardLinks();
            getBackwardLinks();
        }

        public Arc[] getArcs() {
            if (this.arcs == null) {
                this.arcs = createArcs();
            }
            if (!this.arcsValid) {
                computeArcs();
                this.arcsValid = true;
            }
            return this.arcs;
        }

        private boolean relax(int[] locations2, Arc entry) {
            if (!entry.valid) {
                return false;
            }
            Interval span = entry.span;
            int u = span.min;
            int v = span.max;
            int candidate = locations2[u] + entry.value.value;
            if (candidate <= locations2[v]) {
                return false;
            }
            locations2[v] = candidate;
            return true;
        }

        private void init(int[] locations2) {
            Arrays.fill(locations2, 0);
        }

        private String arcsToString(List<Arc> arcs2) {
            String str;
            String var = this.horizontal ? "x" : "y";
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (Arc arc : arcs2) {
                if (first) {
                    first = false;
                } else {
                    result = result.append(", ");
                }
                int src = arc.span.min;
                int dst = arc.span.max;
                int value = arc.value.value;
                if (src < dst) {
                    str = var + dst + "-" + var + src + ">=" + value;
                } else {
                    str = var + src + "-" + var + dst + "<=" + (-value);
                }
                result.append(str);
            }
            return result.toString();
        }

        private void logError(String axisName, Arc[] arcs2, boolean[] culprits0) {
            List<Arc> culprits = new ArrayList<>();
            List<Arc> removed = new ArrayList<>();
            for (int c = 0; c < arcs2.length; c++) {
                Arc arc = arcs2[c];
                if (culprits0[c]) {
                    culprits.add(arc);
                }
                if (!arc.valid) {
                    removed.add(arc);
                }
            }
            Printer printer = GridLayout.this.mPrinter;
            printer.println(axisName + " constraints: " + arcsToString(culprits) + " are inconsistent; permanently removing: " + arcsToString(removed) + ". ");
        }

        private boolean solve(Arc[] arcs2, int[] locations2) {
            return solve(arcs2, locations2, true);
        }

        private boolean solve(Arc[] arcs2, int[] locations2, boolean modifyOnError) {
            String axisName = this.horizontal ? "horizontal" : "vertical";
            int N = getCount() + 1;
            boolean[] originalCulprits = null;
            for (int p = 0; p < arcs2.length; p++) {
                init(locations2);
                for (int i = 0; i < N; i++) {
                    boolean changed = false;
                    for (Arc relax : arcs2) {
                        changed |= relax(locations2, relax);
                    }
                    if (!changed) {
                        if (originalCulprits != null) {
                            logError(axisName, arcs2, originalCulprits);
                        }
                        return true;
                    }
                }
                if (!modifyOnError) {
                    return false;
                }
                boolean[] culprits = new boolean[arcs2.length];
                for (int i2 = 0; i2 < N; i2++) {
                    int length = arcs2.length;
                    for (int j = 0; j < length; j++) {
                        culprits[j] = culprits[j] | relax(locations2, arcs2[j]);
                    }
                }
                if (p == 0) {
                    originalCulprits = culprits;
                }
                int i3 = 0;
                while (true) {
                    if (i3 >= arcs2.length) {
                        break;
                    }
                    if (culprits[i3]) {
                        Arc arc = arcs2[i3];
                        if (arc.span.min >= arc.span.max) {
                            arc.valid = false;
                            break;
                        }
                    }
                    i3++;
                }
            }
            return true;
        }

        private void computeMargins(boolean leading) {
            int[] margins = leading ? this.leadingMargins : this.trailingMargins;
            int N = GridLayout.this.getChildCount();
            for (int i = 0; i < N; i++) {
                View c = GridLayout.this.getChildAt(i);
                if (c.getVisibility() != 8) {
                    LayoutParams lp = GridLayout.this.getLayoutParams(c);
                    Interval span = (this.horizontal ? lp.columnSpec : lp.rowSpec).span;
                    int index = leading ? span.min : span.max;
                    margins[index] = Math.max(margins[index], GridLayout.this.getMargin1(c, this.horizontal, leading));
                }
            }
        }

        public int[] getLeadingMargins() {
            if (this.leadingMargins == null) {
                this.leadingMargins = new int[(getCount() + 1)];
            }
            if (!this.leadingMarginsValid) {
                computeMargins(true);
                this.leadingMarginsValid = true;
            }
            return this.leadingMargins;
        }

        public int[] getTrailingMargins() {
            if (this.trailingMargins == null) {
                this.trailingMargins = new int[(getCount() + 1)];
            }
            if (!this.trailingMarginsValid) {
                computeMargins(false);
                this.trailingMarginsValid = true;
            }
            return this.trailingMargins;
        }

        private boolean solve(int[] a) {
            return solve(getArcs(), a);
        }

        private boolean computeHasWeights() {
            int N = GridLayout.this.getChildCount();
            for (int i = 0; i < N; i++) {
                View child = GridLayout.this.getChildAt(i);
                if (child.getVisibility() != 8) {
                    LayoutParams lp = GridLayout.this.getLayoutParams(child);
                    if ((this.horizontal ? lp.columnSpec : lp.rowSpec).weight != 0.0f) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean hasWeights() {
            if (!this.hasWeightsValid) {
                this.hasWeights = computeHasWeights();
                this.hasWeightsValid = true;
            }
            return this.hasWeights;
        }

        public int[] getDeltas() {
            if (this.deltas == null) {
                this.deltas = new int[GridLayout.this.getChildCount()];
            }
            return this.deltas;
        }

        private void shareOutDelta(int totalDelta, float totalWeight) {
            Arrays.fill(this.deltas, 0);
            int N = GridLayout.this.getChildCount();
            for (int i = 0; i < N; i++) {
                View c = GridLayout.this.getChildAt(i);
                if (c.getVisibility() != 8) {
                    LayoutParams lp = GridLayout.this.getLayoutParams(c);
                    float weight = (this.horizontal ? lp.columnSpec : lp.rowSpec).weight;
                    if (weight != 0.0f) {
                        int delta = Math.round((((float) totalDelta) * weight) / totalWeight);
                        this.deltas[i] = delta;
                        totalDelta -= delta;
                        totalWeight -= weight;
                    }
                }
            }
        }

        private void solveAndDistributeSpace(int[] a) {
            Arrays.fill(getDeltas(), 0);
            solve(a);
            boolean validSolution = true;
            int deltaMax = (this.parentMin.value * GridLayout.this.getChildCount()) + 1;
            if (deltaMax >= 2) {
                int deltaMin = 0;
                float totalWeight = calculateTotalWeight();
                int validDelta = -1;
                while (deltaMin < deltaMax) {
                    int delta = (int) ((((long) deltaMin) + ((long) deltaMax)) / 2);
                    invalidateValues();
                    shareOutDelta(delta, totalWeight);
                    validSolution = solve(getArcs(), a, false);
                    if (validSolution) {
                        validDelta = delta;
                        deltaMin = delta + 1;
                    } else {
                        deltaMax = delta;
                    }
                }
                if (validDelta > 0 && !validSolution) {
                    invalidateValues();
                    shareOutDelta(validDelta, totalWeight);
                    solve(a);
                }
            }
        }

        private float calculateTotalWeight() {
            float totalWeight = 0.0f;
            int N = GridLayout.this.getChildCount();
            for (int i = 0; i < N; i++) {
                View c = GridLayout.this.getChildAt(i);
                if (c.getVisibility() != 8) {
                    LayoutParams lp = GridLayout.this.getLayoutParams(c);
                    totalWeight += (this.horizontal ? lp.columnSpec : lp.rowSpec).weight;
                }
            }
            return totalWeight;
        }

        private void computeLocations(int[] a) {
            if (!hasWeights()) {
                solve(a);
            } else {
                solveAndDistributeSpace(a);
            }
            if (!this.orderPreserved) {
                int a0 = a[0];
                int N = a.length;
                for (int i = 0; i < N; i++) {
                    a[i] = a[i] - a0;
                }
            }
        }

        public int[] getLocations() {
            if (this.locations == null) {
                this.locations = new int[(getCount() + 1)];
            }
            if (this.locationsValid == 0) {
                computeLocations(this.locations);
                this.locationsValid = true;
            }
            return this.locations;
        }

        private int size(int[] locations2) {
            return locations2[getCount()];
        }

        private void setParentConstraints(int min, int max) {
            this.parentMin.value = min;
            this.parentMax.value = -max;
            this.locationsValid = false;
        }

        private int getMeasure(int min, int max) {
            setParentConstraints(min, max);
            return size(getLocations());
        }

        public int getMeasure(int measureSpec) {
            int mode = View.MeasureSpec.getMode(measureSpec);
            int size = View.MeasureSpec.getSize(measureSpec);
            if (mode == Integer.MIN_VALUE) {
                return getMeasure(0, size);
            }
            if (mode == 0) {
                return getMeasure(0, 100000);
            }
            if (mode != 1073741824) {
                return 0;
            }
            return getMeasure(size, size);
        }

        public void layout(int size) {
            setParentConstraints(size, size);
            getLocations();
        }

        public void invalidateStructure() {
            this.maxIndex = Integer.MIN_VALUE;
            this.groupBounds = null;
            this.forwardLinks = null;
            this.backwardLinks = null;
            this.leadingMargins = null;
            this.trailingMargins = null;
            this.arcs = null;
            this.locations = null;
            this.deltas = null;
            this.hasWeightsValid = false;
            invalidateValues();
        }

        public void invalidateValues() {
            this.groupBoundsValid = false;
            this.forwardLinksValid = false;
            this.backwardLinksValid = false;
            this.leadingMarginsValid = false;
            this.trailingMarginsValid = false;
            this.arcsValid = false;
            this.locationsValid = false;
        }
    }

    static class Bounds {
        public int after;
        public int before;
        public int flexibility;

        private Bounds() {
            reset();
        }

        /* access modifiers changed from: protected */
        public void reset() {
            this.before = Integer.MIN_VALUE;
            this.after = Integer.MIN_VALUE;
            this.flexibility = 2;
        }

        /* access modifiers changed from: protected */
        public void include(int before2, int after2) {
            this.before = Math.max(this.before, before2);
            this.after = Math.max(this.after, after2);
        }

        /* access modifiers changed from: protected */
        public int size(boolean min) {
            if (min || !GridLayout.canStretch(this.flexibility)) {
                return this.before + this.after;
            }
            return 100000;
        }

        /* access modifiers changed from: protected */
        public int getOffset(GridLayout gl, View c, Alignment a, int size, boolean horizontal) {
            return this.before - a.getAlignmentValue(c, size, gl.getLayoutMode());
        }

        /* access modifiers changed from: protected */
        public final void include(GridLayout gl, View c, Spec spec, Axis axis, int size) {
            this.flexibility &= spec.getFlexibility();
            boolean z = axis.horizontal;
            int before2 = spec.getAbsoluteAlignment(axis.horizontal).getAlignmentValue(c, size, gl.getLayoutMode());
            include(before2, size - before2);
        }

        public String toString() {
            return "Bounds{before=" + this.before + ", after=" + this.after + '}';
        }
    }

    static final class Interval {
        public final int max;
        public final int min;

        public Interval(int min2, int max2) {
            this.min = min2;
            this.max = max2;
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return this.max - this.min;
        }

        /* access modifiers changed from: package-private */
        public Interval inverse() {
            return new Interval(this.max, this.min);
        }

        public boolean equals(Object that) {
            if (this == that) {
                return true;
            }
            if (that == null || getClass() != that.getClass()) {
                return false;
            }
            Interval interval = (Interval) that;
            if (this.max == interval.max && this.min == interval.min) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (31 * this.min) + this.max;
        }

        public String toString() {
            return "[" + this.min + ", " + this.max + "]";
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        private static final int BOTTOM_MARGIN = 6;
        private static final int COLUMN = 1;
        private static final int COLUMN_SPAN = 4;
        private static final int COLUMN_WEIGHT = 6;
        private static final int DEFAULT_COLUMN = Integer.MIN_VALUE;
        private static final int DEFAULT_HEIGHT = -2;
        private static final int DEFAULT_MARGIN = Integer.MIN_VALUE;
        private static final int DEFAULT_ROW = Integer.MIN_VALUE;
        private static final Interval DEFAULT_SPAN = new Interval(Integer.MIN_VALUE, -2147483647);
        private static final int DEFAULT_SPAN_SIZE = DEFAULT_SPAN.size();
        private static final int DEFAULT_WIDTH = -2;
        private static final int GRAVITY = 0;
        private static final int LEFT_MARGIN = 3;
        private static final int MARGIN = 2;
        private static final int RIGHT_MARGIN = 5;
        private static final int ROW = 2;
        private static final int ROW_SPAN = 3;
        private static final int ROW_WEIGHT = 5;
        private static final int TOP_MARGIN = 4;
        public Spec columnSpec;
        public Spec rowSpec;

        private LayoutParams(int width, int height, int left, int top, int right, int bottom, Spec rowSpec2, Spec columnSpec2) {
            super(width, height);
            this.rowSpec = Spec.UNDEFINED;
            this.columnSpec = Spec.UNDEFINED;
            setMargins(left, top, right, bottom);
            this.rowSpec = rowSpec2;
            this.columnSpec = columnSpec2;
        }

        public LayoutParams(Spec rowSpec2, Spec columnSpec2) {
            this(-2, -2, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, rowSpec2, columnSpec2);
        }

        public LayoutParams() {
            this(Spec.UNDEFINED, Spec.UNDEFINED);
        }

        public LayoutParams(ViewGroup.LayoutParams params) {
            super(params);
            this.rowSpec = Spec.UNDEFINED;
            this.columnSpec = Spec.UNDEFINED;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams params) {
            super(params);
            this.rowSpec = Spec.UNDEFINED;
            this.columnSpec = Spec.UNDEFINED;
        }

        public LayoutParams(LayoutParams source) {
            super((ViewGroup.MarginLayoutParams) source);
            this.rowSpec = Spec.UNDEFINED;
            this.columnSpec = Spec.UNDEFINED;
            this.rowSpec = source.rowSpec;
            this.columnSpec = source.columnSpec;
        }

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.rowSpec = Spec.UNDEFINED;
            this.columnSpec = Spec.UNDEFINED;
            reInitSuper(context, attrs);
            init(context, attrs);
        }

        private void reInitSuper(Context context, AttributeSet attrs) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewGroup_MarginLayout);
            try {
                int margin = a.getDimensionPixelSize(2, Integer.MIN_VALUE);
                this.leftMargin = a.getDimensionPixelSize(3, margin);
                this.topMargin = a.getDimensionPixelSize(4, margin);
                this.rightMargin = a.getDimensionPixelSize(5, margin);
                this.bottomMargin = a.getDimensionPixelSize(6, margin);
            } finally {
                a.recycle();
            }
        }

        private void init(Context context, AttributeSet attrs) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridLayout_Layout);
            try {
                int gravity = a.getInt(0, 0);
                this.columnSpec = GridLayout.spec(a.getInt(1, Integer.MIN_VALUE), a.getInt(4, DEFAULT_SPAN_SIZE), GridLayout.getAlignment(gravity, true), a.getFloat(6, 0.0f));
                this.rowSpec = GridLayout.spec(a.getInt(2, Integer.MIN_VALUE), a.getInt(3, DEFAULT_SPAN_SIZE), GridLayout.getAlignment(gravity, false), a.getFloat(5, 0.0f));
            } finally {
                a.recycle();
            }
        }

        public void setGravity(int gravity) {
            this.rowSpec = this.rowSpec.copyWriteAlignment(GridLayout.getAlignment(gravity, false));
            this.columnSpec = this.columnSpec.copyWriteAlignment(GridLayout.getAlignment(gravity, true));
        }

        /* access modifiers changed from: protected */
        public void setBaseAttributes(TypedArray attributes, int widthAttr, int heightAttr) {
            this.width = attributes.getLayoutDimension(widthAttr, -2);
            this.height = attributes.getLayoutDimension(heightAttr, -2);
        }

        /* access modifiers changed from: package-private */
        public final void setRowSpecSpan(Interval span) {
            this.rowSpec = this.rowSpec.copyWriteSpan(span);
        }

        /* access modifiers changed from: package-private */
        public final void setColumnSpecSpan(Interval span) {
            this.columnSpec = this.columnSpec.copyWriteSpan(span);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LayoutParams that = (LayoutParams) o;
            if (this.columnSpec.equals(that.columnSpec) && this.rowSpec.equals(that.rowSpec)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (31 * this.rowSpec.hashCode()) + this.columnSpec.hashCode();
        }
    }

    static final class MutableInt {
        public int value;

        public MutableInt() {
            reset();
        }

        public MutableInt(int value2) {
            this.value = value2;
        }

        public void reset() {
            this.value = Integer.MIN_VALUE;
        }

        public String toString() {
            return Integer.toString(this.value);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {
    }

    static final class PackedMap<K, V> {
        public final int[] index;
        public final K[] keys;
        public final V[] values;

        private PackedMap(K[] keys2, V[] values2) {
            this.index = createIndex(keys2);
            this.keys = compact(keys2, this.index);
            this.values = compact(values2, this.index);
        }

        public V getValue(int i) {
            return this.values[this.index[i]];
        }

        private static <K> int[] createIndex(K[] keys2) {
            int size = keys2.length;
            int[] result = new int[size];
            Map<K, Integer> keyToIndex = new HashMap<>();
            for (int i = 0; i < size; i++) {
                K key = keys2[i];
                Integer index2 = keyToIndex.get(key);
                if (index2 == null) {
                    index2 = Integer.valueOf(keyToIndex.size());
                    keyToIndex.put(key, index2);
                }
                result[i] = index2.intValue();
            }
            return result;
        }

        private static <K> K[] compact(K[] a, int[] index2) {
            int size = a.length;
            K[] result = (Object[]) Array.newInstance(a.getClass().getComponentType(), GridLayout.max2(index2, -1) + 1);
            for (int i = 0; i < size; i++) {
                result[index2[i]] = a[i];
            }
            return result;
        }
    }

    public static class Spec {
        static final float DEFAULT_WEIGHT = 0.0f;
        static final Spec UNDEFINED = GridLayout.spec(Integer.MIN_VALUE);
        final Alignment alignment;
        final Interval span;
        final boolean startDefined;
        final float weight;

        private Spec(boolean startDefined2, Interval span2, Alignment alignment2, float weight2) {
            this.startDefined = startDefined2;
            this.span = span2;
            this.alignment = alignment2;
            this.weight = weight2;
        }

        private Spec(boolean startDefined2, int start, int size, Alignment alignment2, float weight2) {
            this(startDefined2, new Interval(start, start + size), alignment2, weight2);
        }

        /* access modifiers changed from: private */
        public Alignment getAbsoluteAlignment(boolean horizontal) {
            if (this.alignment != GridLayout.UNDEFINED_ALIGNMENT) {
                return this.alignment;
            }
            if (this.weight != 0.0f) {
                return GridLayout.FILL;
            }
            return horizontal ? GridLayout.START : GridLayout.BASELINE;
        }

        /* access modifiers changed from: package-private */
        public final Spec copyWriteSpan(Interval span2) {
            return new Spec(this.startDefined, span2, this.alignment, this.weight);
        }

        /* access modifiers changed from: package-private */
        public final Spec copyWriteAlignment(Alignment alignment2) {
            return new Spec(this.startDefined, this.span, alignment2, this.weight);
        }

        /* access modifiers changed from: package-private */
        public final int getFlexibility() {
            return (this.alignment == GridLayout.UNDEFINED_ALIGNMENT && this.weight == 0.0f) ? 0 : 2;
        }

        public boolean equals(Object that) {
            if (this == that) {
                return true;
            }
            if (that == null || getClass() != that.getClass()) {
                return false;
            }
            Spec spec = (Spec) that;
            if (this.alignment.equals(spec.alignment) && this.span.equals(spec.span)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (31 * this.span.hashCode()) + this.alignment.hashCode();
        }
    }

    public GridLayout(Context context) {
        this(context, null);
    }

    public GridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHorizontalAxis = new Axis(true);
        this.mVerticalAxis = new Axis(false);
        this.mOrientation = 0;
        this.mUseDefaultMargins = false;
        this.mAlignmentMode = 1;
        this.mLastLayoutParamsHashCode = 0;
        this.mPrinter = LOG_PRINTER;
        this.mDefaultGap = context.getResources().getDimensionPixelOffset(17105015);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridLayout, defStyleAttr, defStyleRes);
        try {
            setRowCount(a.getInt(1, Integer.MIN_VALUE));
            setColumnCount(a.getInt(3, Integer.MIN_VALUE));
            setOrientation(a.getInt(0, 0));
            setUseDefaultMargins(a.getBoolean(5, false));
            setAlignmentMode(a.getInt(6, 1));
            setRowOrderPreserved(a.getBoolean(2, true));
            setColumnOrderPreserved(a.getBoolean(4, true));
        } finally {
            a.recycle();
        }
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public void setOrientation(int orientation) {
        if (this.mOrientation != orientation) {
            this.mOrientation = orientation;
            invalidateStructure();
            requestLayout();
        }
    }

    public int getRowCount() {
        return this.mVerticalAxis.getCount();
    }

    public void setRowCount(int rowCount) {
        this.mVerticalAxis.setCount(rowCount);
        invalidateStructure();
        requestLayout();
    }

    public int getColumnCount() {
        return this.mHorizontalAxis.getCount();
    }

    public void setColumnCount(int columnCount) {
        this.mHorizontalAxis.setCount(columnCount);
        invalidateStructure();
        requestLayout();
    }

    public boolean getUseDefaultMargins() {
        return this.mUseDefaultMargins;
    }

    public void setUseDefaultMargins(boolean useDefaultMargins) {
        this.mUseDefaultMargins = useDefaultMargins;
        requestLayout();
    }

    public int getAlignmentMode() {
        return this.mAlignmentMode;
    }

    public void setAlignmentMode(int alignmentMode) {
        this.mAlignmentMode = alignmentMode;
        requestLayout();
    }

    public boolean isRowOrderPreserved() {
        return this.mVerticalAxis.isOrderPreserved();
    }

    public void setRowOrderPreserved(boolean rowOrderPreserved) {
        this.mVerticalAxis.setOrderPreserved(rowOrderPreserved);
        invalidateStructure();
        requestLayout();
    }

    public boolean isColumnOrderPreserved() {
        return this.mHorizontalAxis.isOrderPreserved();
    }

    public void setColumnOrderPreserved(boolean columnOrderPreserved) {
        this.mHorizontalAxis.setOrderPreserved(columnOrderPreserved);
        invalidateStructure();
        requestLayout();
    }

    public Printer getPrinter() {
        return this.mPrinter;
    }

    public void setPrinter(Printer printer) {
        this.mPrinter = printer == null ? NO_PRINTER : printer;
    }

    static int max2(int[] a, int valueIfEmpty) {
        int result = valueIfEmpty;
        for (int max : a) {
            result = Math.max(result, max);
        }
        return result;
    }

    static <T> T[] append(T[] a, T[] b) {
        T[] result = (Object[]) Array.newInstance(a.getClass().getComponentType(), a.length + b.length);
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    static Alignment getAlignment(int gravity, boolean horizontal) {
        int flags = (gravity & (horizontal ? 7 : 112)) >> (horizontal ? 0 : 4);
        if (flags == 1) {
            return CENTER;
        }
        if (flags == 3) {
            return horizontal ? LEFT : TOP;
        } else if (flags == 5) {
            return horizontal ? RIGHT : BOTTOM;
        } else if (flags == 7) {
            return FILL;
        } else {
            if (flags == 8388611) {
                return START;
            }
            if (flags != 8388613) {
                return UNDEFINED_ALIGNMENT;
            }
            return END;
        }
    }

    private int getDefaultMargin(View c, boolean horizontal, boolean leading) {
        if (c.getClass() == Space.class) {
            return 0;
        }
        return this.mDefaultGap / 2;
    }

    private int getDefaultMargin(View c, boolean isAtEdge, boolean horizontal, boolean leading) {
        return getDefaultMargin(c, horizontal, leading);
    }

    private int getDefaultMargin(View c, LayoutParams p, boolean horizontal, boolean leading) {
        boolean isAtEdge = false;
        if (!this.mUseDefaultMargins) {
            return 0;
        }
        Spec spec = horizontal ? p.columnSpec : p.rowSpec;
        Axis axis = horizontal ? this.mHorizontalAxis : this.mVerticalAxis;
        Interval span = spec.span;
        if (!((!horizontal || !isLayoutRtl()) ? leading : !leading) ? span.max == axis.getCount() : span.min == 0) {
            isAtEdge = true;
        }
        return getDefaultMargin(c, isAtEdge, horizontal, leading);
    }

    /* access modifiers changed from: package-private */
    public int getMargin1(View view, boolean horizontal, boolean leading) {
        int margin;
        LayoutParams lp = getLayoutParams(view);
        if (horizontal) {
            margin = leading ? lp.leftMargin : lp.rightMargin;
        } else {
            margin = leading ? lp.topMargin : lp.bottomMargin;
        }
        return margin == Integer.MIN_VALUE ? getDefaultMargin(view, lp, horizontal, leading) : margin;
    }

    private int getMargin(View view, boolean horizontal, boolean leading) {
        if (this.mAlignmentMode == 1) {
            return getMargin1(view, horizontal, leading);
        }
        Axis axis = horizontal ? this.mHorizontalAxis : this.mVerticalAxis;
        int[] margins = leading ? axis.getLeadingMargins() : axis.getTrailingMargins();
        LayoutParams lp = getLayoutParams(view);
        Spec spec = horizontal ? lp.columnSpec : lp.rowSpec;
        return margins[leading ? spec.span.min : spec.span.max];
    }

    private int getTotalMargin(View child, boolean horizontal) {
        return getMargin(child, horizontal, true) + getMargin(child, horizontal, false);
    }

    private static boolean fits(int[] a, int value, int start, int end) {
        if (end > a.length) {
            return false;
        }
        for (int i = start; i < end; i++) {
            if (a[i] > value) {
                return false;
            }
        }
        return true;
    }

    private static void procrusteanFill(int[] a, int start, int end, int value) {
        int length = a.length;
        Arrays.fill(a, Math.min(start, length), Math.min(end, length), value);
    }

    private static void setCellGroup(LayoutParams lp, int row, int rowSpan, int col, int colSpan) {
        lp.setRowSpecSpan(new Interval(row, row + rowSpan));
        lp.setColumnSpecSpan(new Interval(col, col + colSpan));
    }

    private static int clip(Interval minorRange, boolean minorWasDefined, int count) {
        int size = minorRange.size();
        if (count == 0) {
            return size;
        }
        return Math.min(size, count - (minorWasDefined ? Math.min(minorRange.min, count) : 0));
    }

    private void validateLayoutParams() {
        int N;
        GridLayout gridLayout = this;
        int count = 0;
        boolean horizontal = gridLayout.mOrientation == 0;
        Axis axis = horizontal ? gridLayout.mHorizontalAxis : gridLayout.mVerticalAxis;
        if (axis.definedCount != Integer.MIN_VALUE) {
            count = axis.definedCount;
        }
        int major = 0;
        int minor = 0;
        int[] maxSizes = new int[count];
        int i = 0;
        int N2 = getChildCount();
        while (i < N2) {
            LayoutParams lp = (LayoutParams) gridLayout.getChildAt(i).getLayoutParams();
            Spec majorSpec = horizontal ? lp.rowSpec : lp.columnSpec;
            Interval majorRange = majorSpec.span;
            boolean majorWasDefined = majorSpec.startDefined;
            int majorSpan = majorRange.size();
            if (majorWasDefined) {
                major = majorRange.min;
            }
            Spec minorSpec = horizontal ? lp.columnSpec : lp.rowSpec;
            Interval minorRange = minorSpec.span;
            boolean minorWasDefined = minorSpec.startDefined;
            Axis axis2 = axis;
            int minorSpan = clip(minorRange, minorWasDefined, count);
            if (minorWasDefined) {
                minor = minorRange.min;
            }
            if (count != 0) {
                if (!majorWasDefined || !minorWasDefined) {
                    while (true) {
                        N = N2;
                        if (fits(maxSizes, major, minor, minor + minorSpan) != 0) {
                            break;
                        }
                        if (minorWasDefined) {
                            major++;
                        } else if (minor + minorSpan <= count) {
                            minor++;
                        } else {
                            minor = 0;
                            major++;
                        }
                        N2 = N;
                    }
                } else {
                    N = N2;
                }
                boolean z = minorWasDefined;
                procrusteanFill(maxSizes, minor, minor + minorSpan, major + majorSpan);
            } else {
                N = N2;
            }
            if (horizontal) {
                setCellGroup(lp, major, majorSpan, minor, minorSpan);
            } else {
                setCellGroup(lp, minor, minorSpan, major, majorSpan);
            }
            minor += minorSpan;
            i++;
            axis = axis2;
            N2 = N;
            gridLayout = this;
        }
    }

    private void invalidateStructure() {
        this.mLastLayoutParamsHashCode = 0;
        this.mHorizontalAxis.invalidateStructure();
        this.mVerticalAxis.invalidateStructure();
        invalidateValues();
    }

    private void invalidateValues() {
        if (this.mHorizontalAxis != null && this.mVerticalAxis != null) {
            this.mHorizontalAxis.invalidateValues();
            this.mVerticalAxis.invalidateValues();
        }
    }

    /* access modifiers changed from: protected */
    public void onSetLayoutParams(View child, ViewGroup.LayoutParams layoutParams) {
        super.onSetLayoutParams(child, layoutParams);
        if (!checkLayoutParams(layoutParams)) {
            handleInvalidParams("supplied LayoutParams are of the wrong type");
        }
        invalidateStructure();
    }

    /* access modifiers changed from: package-private */
    public final LayoutParams getLayoutParams(View c) {
        return (LayoutParams) c.getLayoutParams();
    }

    /* access modifiers changed from: private */
    public static void handleInvalidParams(String msg) {
        throw new IllegalArgumentException(msg + ". ");
    }

    private void checkLayoutParams(LayoutParams lp, boolean horizontal) {
        String groupName = horizontal ? "column" : "row";
        Interval span = (horizontal ? lp.columnSpec : lp.rowSpec).span;
        if (span.min != Integer.MIN_VALUE && span.min < 0) {
            handleInvalidParams(groupName + " indices must be positive");
        }
        int count = (horizontal ? this.mHorizontalAxis : this.mVerticalAxis).definedCount;
        if (count != Integer.MIN_VALUE) {
            if (span.max > count) {
                handleInvalidParams(groupName + " indices (start + span) mustn't exceed the " + groupName + " count");
            }
            if (span.size() > count) {
                handleInvalidParams(groupName + " span mustn't exceed the " + groupName + " count");
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        if (!(p instanceof LayoutParams)) {
            return false;
        }
        LayoutParams lp = (LayoutParams) p;
        checkLayoutParams(lp, true);
        checkLayoutParams(lp, false);
        return true;
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (sPreserveMarginParamsInLayoutParamConversion) {
            if (lp instanceof LayoutParams) {
                return new LayoutParams((LayoutParams) lp);
            }
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
            }
        }
        return new LayoutParams(lp);
    }

    private void drawLine(Canvas graphics, int x1, int y1, int x2, int y2, Paint paint) {
        int i = x1;
        int i2 = y1;
        int i3 = x2;
        int i4 = y2;
        if (isLayoutRtl()) {
            int width = getWidth();
            graphics.drawLine((float) (width - i), (float) i2, (float) (width - i3), (float) i4, paint);
            return;
        }
        graphics.drawLine((float) i, (float) i2, (float) i3, (float) i4, paint);
    }

    /* access modifiers changed from: protected */
    public void onDebugDrawMargins(Canvas canvas, Paint paint) {
        LayoutParams lp = new LayoutParams();
        for (int i = 0; i < getChildCount(); i++) {
            View c = getChildAt(i);
            lp.setMargins(getMargin1(c, true, true), getMargin1(c, false, true), getMargin1(c, true, false), getMargin1(c, false, false));
            lp.onDebugDraw(c, canvas, paint);
        }
    }

    /* access modifiers changed from: protected */
    public void onDebugDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(50, 255, 255, 255));
        Insets insets = getOpticalInsets();
        int top = getPaddingTop() + insets.top;
        int left = getPaddingLeft() + insets.left;
        int right = (getWidth() - getPaddingRight()) - insets.right;
        int bottom = (getHeight() - getPaddingBottom()) - insets.bottom;
        int[] xs = this.mHorizontalAxis.locations;
        if (xs != null) {
            int length = xs.length;
            int i = 0;
            while (true) {
                int length2 = length;
                if (i >= length2) {
                    break;
                }
                int x = left + xs[i];
                drawLine(canvas, x, top, x, bottom, paint);
                i++;
                length = length2;
            }
        }
        int[] ys = this.mVerticalAxis.locations;
        if (ys != null) {
            int length3 = ys.length;
            int i2 = 0;
            while (true) {
                int length4 = length3;
                if (i2 >= length4) {
                    break;
                }
                int y = top + ys[i2];
                drawLine(canvas, left, y, right, y, paint);
                i2++;
                length3 = length4;
            }
        }
        super.onDebugDraw(canvas);
    }

    public void onViewAdded(View child) {
        super.onViewAdded(child);
        invalidateStructure();
    }

    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        invalidateStructure();
    }

    /* access modifiers changed from: protected */
    public void onChildVisibilityChanged(View child, int oldVisibility, int newVisibility) {
        super.onChildVisibilityChanged(child, oldVisibility, newVisibility);
        if (oldVisibility == 8 || newVisibility == 8) {
            invalidateStructure();
        }
    }

    private int computeLayoutParamsHashCode() {
        int result = 1;
        int N = getChildCount();
        for (int i = 0; i < N; i++) {
            View c = getChildAt(i);
            if (c.getVisibility() != 8) {
                result = (31 * result) + ((LayoutParams) c.getLayoutParams()).hashCode();
            }
        }
        return result;
    }

    private void consistencyCheck() {
        if (this.mLastLayoutParamsHashCode == 0) {
            validateLayoutParams();
            this.mLastLayoutParamsHashCode = computeLayoutParamsHashCode();
        } else if (this.mLastLayoutParamsHashCode != computeLayoutParamsHashCode()) {
            this.mPrinter.println("The fields of some layout parameters were modified in between layout operations. Check the javadoc for GridLayout.LayoutParams#rowSpec.");
            invalidateStructure();
            consistencyCheck();
        }
    }

    private void measureChildWithMargins2(View child, int parentWidthSpec, int parentHeightSpec, int childWidth, int childHeight) {
        child.measure(getChildMeasureSpec(parentWidthSpec, getTotalMargin(child, true), childWidth), getChildMeasureSpec(parentHeightSpec, getTotalMargin(child, false), childHeight));
    }

    private void measureChildrenWithMargins(int widthSpec, int heightSpec, boolean firstPass) {
        int N = getChildCount();
        int i = 0;
        while (true) {
            int N2 = N;
            if (i < N2) {
                View c = getChildAt(i);
                if (c.getVisibility() != 8) {
                    LayoutParams lp = getLayoutParams(c);
                    if (firstPass) {
                        measureChildWithMargins2(c, widthSpec, heightSpec, lp.width, lp.height);
                    } else {
                        boolean horizontal = this.mOrientation == 0;
                        Spec spec = horizontal ? lp.columnSpec : lp.rowSpec;
                        if (spec.getAbsoluteAlignment(horizontal) == FILL) {
                            Interval span = spec.span;
                            int[] locations = (horizontal ? this.mHorizontalAxis : this.mVerticalAxis).getLocations();
                            int viewSize = (locations[span.max] - locations[span.min]) - getTotalMargin(c, horizontal);
                            if (horizontal) {
                                measureChildWithMargins2(c, widthSpec, heightSpec, viewSize, lp.height);
                            } else {
                                measureChildWithMargins2(c, widthSpec, heightSpec, lp.width, viewSize);
                            }
                        }
                    }
                }
                i++;
                N = N2;
            } else {
                return;
            }
        }
    }

    static int adjust(int measureSpec, int delta) {
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec + delta), View.MeasureSpec.getMode(measureSpec));
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthSpec, int heightSpec) {
        int heightSansPadding;
        int widthSansPadding;
        consistencyCheck();
        invalidateValues();
        int hPadding = getPaddingLeft() + getPaddingRight();
        int vPadding = getPaddingTop() + getPaddingBottom();
        int widthSpecSansPadding = adjust(widthSpec, -hPadding);
        int heightSpecSansPadding = adjust(heightSpec, -vPadding);
        measureChildrenWithMargins(widthSpecSansPadding, heightSpecSansPadding, true);
        if (this.mOrientation == 0) {
            widthSansPadding = this.mHorizontalAxis.getMeasure(widthSpecSansPadding);
            measureChildrenWithMargins(widthSpecSansPadding, heightSpecSansPadding, false);
            heightSansPadding = this.mVerticalAxis.getMeasure(heightSpecSansPadding);
        } else {
            heightSansPadding = this.mVerticalAxis.getMeasure(heightSpecSansPadding);
            measureChildrenWithMargins(widthSpecSansPadding, heightSpecSansPadding, false);
            widthSansPadding = this.mHorizontalAxis.getMeasure(widthSpecSansPadding);
        }
        setMeasuredDimension(resolveSizeAndState(Math.max(widthSansPadding + hPadding, getSuggestedMinimumWidth()), widthSpec, 0), resolveSizeAndState(Math.max(heightSansPadding + vPadding, getSuggestedMinimumHeight()), heightSpec, 0));
    }

    private int getMeasurement(View c, boolean horizontal) {
        return horizontal ? c.getMeasuredWidth() : c.getMeasuredHeight();
    }

    /* access modifiers changed from: package-private */
    public final int getMeasurementIncludingMargin(View c, boolean horizontal) {
        if (c.getVisibility() == 8) {
            return 0;
        }
        return getMeasurement(c, horizontal) + getTotalMargin(c, horizontal);
    }

    public void requestLayout() {
        super.requestLayout();
        invalidateValues();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int N;
        int i;
        int cx;
        GridLayout gridLayout = this;
        consistencyCheck();
        int targetWidth = right - left;
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        gridLayout.mHorizontalAxis.layout((targetWidth - paddingLeft) - paddingRight);
        gridLayout.mVerticalAxis.layout(((bottom - top) - paddingTop) - paddingBottom);
        int[] hLocations = gridLayout.mHorizontalAxis.getLocations();
        int[] vLocations = gridLayout.mVerticalAxis.getLocations();
        int N2 = getChildCount();
        int cx2 = 0;
        while (true) {
            int N3 = N2;
            if (cx2 < N3) {
                View c = gridLayout.getChildAt(cx2);
                if (c.getVisibility() == 8) {
                    N = N3;
                    i = cx2;
                } else {
                    LayoutParams lp = gridLayout.getLayoutParams(c);
                    Spec columnSpec = lp.columnSpec;
                    Spec rowSpec = lp.rowSpec;
                    Interval colSpan = columnSpec.span;
                    Interval rowSpan = rowSpec.span;
                    LayoutParams layoutParams = lp;
                    int x1 = hLocations[colSpan.min];
                    int y1 = vLocations[rowSpan.min];
                    int cellWidth = hLocations[colSpan.max] - x1;
                    int cellHeight = vLocations[rowSpan.max] - y1;
                    Interval interval = colSpan;
                    int pWidth = gridLayout.getMeasurement(c, true);
                    Interval interval2 = rowSpan;
                    int pHeight = gridLayout.getMeasurement(c, false);
                    Alignment hAlign = columnSpec.getAbsoluteAlignment(true);
                    Alignment vAlign = rowSpec.getAbsoluteAlignment(false);
                    Bounds boundsX = gridLayout.mHorizontalAxis.getGroupBounds().getValue(cx2);
                    Spec spec = rowSpec;
                    Bounds boundsY = gridLayout.mVerticalAxis.getGroupBounds().getValue(cx2);
                    Spec spec2 = columnSpec;
                    int gravityOffsetX = hAlign.getGravityOffset(c, cellWidth - boundsX.size(true));
                    int gravityOffsetY = vAlign.getGravityOffset(c, cellHeight - boundsY.size(true));
                    int leftMargin = gridLayout.getMargin(c, true, true);
                    int topMargin = gridLayout.getMargin(c, false, true);
                    int rightMargin = gridLayout.getMargin(c, true, false);
                    int sumMarginsX = leftMargin + rightMargin;
                    int sumMarginsY = topMargin + gridLayout.getMargin(c, false, false);
                    GridLayout gridLayout2 = gridLayout;
                    Bounds bounds = boundsX;
                    N = N3;
                    i = cx2;
                    int alignmentOffsetX = boundsX.getOffset(gridLayout2, c, hAlign, pWidth + sumMarginsX, true);
                    View c2 = c;
                    int alignmentOffsetY = boundsY.getOffset(gridLayout2, c2, vAlign, pHeight + sumMarginsY, false);
                    int width = hAlign.getSizeInCell(c2, pWidth, cellWidth - sumMarginsX);
                    int height = vAlign.getSizeInCell(c2, pHeight, cellHeight - sumMarginsY);
                    int dx = x1 + gravityOffsetX + alignmentOffsetX;
                    if (!isLayoutRtl()) {
                        cx = paddingLeft + leftMargin + dx;
                    } else {
                        cx = (((targetWidth - width) - paddingRight) - rightMargin) - dx;
                    }
                    int i2 = alignmentOffsetY;
                    int alignmentOffsetY2 = paddingTop + y1 + gravityOffsetY + alignmentOffsetY + topMargin;
                    int i3 = dx;
                    if (!(width == c2.getMeasuredWidth() && height == c2.getMeasuredHeight())) {
                        c2.measure(View.MeasureSpec.makeMeasureSpec(width, 1073741824), View.MeasureSpec.makeMeasureSpec(height, 1073741824));
                    }
                    c2.layout(cx, alignmentOffsetY2, cx + width, alignmentOffsetY2 + height);
                }
                cx2 = i + 1;
                N2 = N;
                gridLayout = this;
            } else {
                return;
            }
        }
    }

    public CharSequence getAccessibilityClassName() {
        return GridLayout.class.getName();
    }

    public static Spec spec(int start, int size, Alignment alignment, float weight) {
        Spec spec = new Spec(start != Integer.MIN_VALUE, start, size, alignment, weight);
        return spec;
    }

    public static Spec spec(int start, Alignment alignment, float weight) {
        return spec(start, 1, alignment, weight);
    }

    public static Spec spec(int start, int size, float weight) {
        return spec(start, size, UNDEFINED_ALIGNMENT, weight);
    }

    public static Spec spec(int start, float weight) {
        return spec(start, 1, weight);
    }

    public static Spec spec(int start, int size, Alignment alignment) {
        return spec(start, size, alignment, 0.0f);
    }

    public static Spec spec(int start, Alignment alignment) {
        return spec(start, 1, alignment);
    }

    public static Spec spec(int start, int size) {
        return spec(start, size, UNDEFINED_ALIGNMENT);
    }

    public static Spec spec(int start) {
        return spec(start, 1);
    }

    private static Alignment createSwitchingAlignment(final Alignment ltr, final Alignment rtl) {
        return new Alignment() {
            /* access modifiers changed from: package-private */
            public int getGravityOffset(View view, int cellDelta) {
                return (!view.isLayoutRtl() ? Alignment.this : rtl).getGravityOffset(view, cellDelta);
            }

            public int getAlignmentValue(View view, int viewSize, int mode) {
                return (!view.isLayoutRtl() ? Alignment.this : rtl).getAlignmentValue(view, viewSize, mode);
            }
        };
    }

    static boolean canStretch(int flexibility) {
        return (flexibility & 2) != 0;
    }
}
