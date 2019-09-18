package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Pools;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;
import com.android.internal.R;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

@RemoteViews.RemoteView
public class RelativeLayout extends ViewGroup {
    public static final int ABOVE = 2;
    public static final int ALIGN_BASELINE = 4;
    public static final int ALIGN_BOTTOM = 8;
    public static final int ALIGN_END = 19;
    public static final int ALIGN_LEFT = 5;
    public static final int ALIGN_PARENT_BOTTOM = 12;
    public static final int ALIGN_PARENT_END = 21;
    public static final int ALIGN_PARENT_LEFT = 9;
    public static final int ALIGN_PARENT_RIGHT = 11;
    public static final int ALIGN_PARENT_START = 20;
    public static final int ALIGN_PARENT_TOP = 10;
    public static final int ALIGN_RIGHT = 7;
    public static final int ALIGN_START = 18;
    public static final int ALIGN_TOP = 6;
    public static final int BELOW = 3;
    public static final int CENTER_HORIZONTAL = 14;
    public static final int CENTER_IN_PARENT = 13;
    public static final int CENTER_VERTICAL = 15;
    private static final int DEFAULT_WIDTH = 65536;
    public static final int END_OF = 17;
    public static final int LEFT_OF = 0;
    public static final int RIGHT_OF = 1;
    private static final int[] RULES_HORIZONTAL = {0, 1, 5, 7, 16, 17, 18, 19};
    private static final int[] RULES_VERTICAL = {2, 3, 4, 6, 8};
    public static final int START_OF = 16;
    public static final int TRUE = -1;
    private static final int VALUE_NOT_SET = Integer.MIN_VALUE;
    private static final int VERB_COUNT = 22;
    private boolean mAllowBrokenMeasureSpecs;
    private View mBaselineView;
    private final Rect mContentBounds;
    private boolean mDirtyHierarchy;
    private final DependencyGraph mGraph;
    private int mGravity;
    private int mIgnoreGravity;
    private boolean mMeasureVerticalWithPaddingMargin;
    private final Rect mSelfBounds;
    private View[] mSortedHorizontalChildren;
    private View[] mSortedVerticalChildren;
    private SortedSet<View> mTopToBottomLeftToRightSet;

    private static class DependencyGraph {
        /* access modifiers changed from: private */
        public SparseArray<Node> mKeyNodes;
        private ArrayList<Node> mNodes;
        private ArrayDeque<Node> mRoots;

        static class Node {
            private static final int POOL_LIMIT = 100;
            private static final Pools.SynchronizedPool<Node> sPool = new Pools.SynchronizedPool<>(100);
            final SparseArray<Node> dependencies = new SparseArray<>();
            final ArrayMap<Node, DependencyGraph> dependents = new ArrayMap<>();
            View view;

            Node() {
            }

            static Node acquire(View view2) {
                Node node = sPool.acquire();
                if (node == null) {
                    node = new Node();
                }
                node.view = view2;
                return node;
            }

            /* access modifiers changed from: package-private */
            public void release() {
                this.view = null;
                this.dependents.clear();
                this.dependencies.clear();
                sPool.release(this);
            }
        }

        private DependencyGraph() {
            this.mNodes = new ArrayList<>();
            this.mKeyNodes = new SparseArray<>();
            this.mRoots = new ArrayDeque<>();
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            ArrayList<Node> nodes = this.mNodes;
            int count = nodes.size();
            for (int i = 0; i < count; i++) {
                nodes.get(i).release();
            }
            nodes.clear();
            this.mKeyNodes.clear();
            this.mRoots.clear();
        }

        /* access modifiers changed from: package-private */
        public void add(View view) {
            int id = view.getId();
            Node node = Node.acquire(view);
            if (id != -1) {
                this.mKeyNodes.put(id, node);
            }
            this.mNodes.add(node);
        }

        /* access modifiers changed from: package-private */
        public void getSortedViews(View[] sorted, int... rules) {
            ArrayDeque<Node> roots = findRoots(rules);
            int index = 0;
            while (true) {
                Node pollLast = roots.pollLast();
                Node node = pollLast;
                if (pollLast == null) {
                    break;
                }
                View view = node.view;
                int key = view.getId();
                int index2 = index + 1;
                sorted[index] = view;
                ArrayMap<Node, DependencyGraph> dependents = node.dependents;
                int count = dependents.size();
                for (int i = 0; i < count; i++) {
                    Node dependent = dependents.keyAt(i);
                    SparseArray<Node> dependencies = dependent.dependencies;
                    dependencies.remove(key);
                    if (dependencies.size() == 0) {
                        roots.add(dependent);
                    }
                }
                index = index2;
            }
            if (index < sorted.length) {
                throw new IllegalStateException("Circular dependencies cannot exist in RelativeLayout");
            }
        }

        private ArrayDeque<Node> findRoots(int[] rulesFilter) {
            SparseArray<Node> keyNodes = this.mKeyNodes;
            ArrayList<Node> nodes = this.mNodes;
            int count = nodes.size();
            for (int i = 0; i < count; i++) {
                Node node = nodes.get(i);
                node.dependents.clear();
                node.dependencies.clear();
            }
            for (int i2 = 0; i2 < count; i2++) {
                Node node2 = nodes.get(i2);
                int[] rules = ((LayoutParams) node2.view.getLayoutParams()).mRules;
                for (int i3 : rulesFilter) {
                    int rule = rules[i3];
                    if (rule > 0) {
                        Node dependency = keyNodes.get(rule);
                        if (!(dependency == null || dependency == node2)) {
                            dependency.dependents.put(node2, this);
                            node2.dependencies.put(rule, dependency);
                        }
                    }
                }
            }
            ArrayDeque<Node> roots = this.mRoots;
            roots.clear();
            for (int i4 = 0; i4 < count; i4++) {
                Node node3 = nodes.get(i4);
                if (node3.dependencies.size() == 0) {
                    roots.addLast(node3);
                }
            }
            return roots;
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        @ViewDebug.ExportedProperty(category = "layout")
        public boolean alignWithParent;
        /* access modifiers changed from: private */
        public int mBottom;
        private int[] mInitialRules = new int[22];
        private boolean mIsRtlCompatibilityMode = false;
        /* access modifiers changed from: private */
        public int mLeft;
        private boolean mNeedsLayoutResolution;
        /* access modifiers changed from: private */
        public int mRight;
        /* access modifiers changed from: private */
        @ViewDebug.ExportedProperty(category = "layout", indexMapping = {@ViewDebug.IntToString(from = 2, to = "above"), @ViewDebug.IntToString(from = 4, to = "alignBaseline"), @ViewDebug.IntToString(from = 8, to = "alignBottom"), @ViewDebug.IntToString(from = 5, to = "alignLeft"), @ViewDebug.IntToString(from = 12, to = "alignParentBottom"), @ViewDebug.IntToString(from = 9, to = "alignParentLeft"), @ViewDebug.IntToString(from = 11, to = "alignParentRight"), @ViewDebug.IntToString(from = 10, to = "alignParentTop"), @ViewDebug.IntToString(from = 7, to = "alignRight"), @ViewDebug.IntToString(from = 6, to = "alignTop"), @ViewDebug.IntToString(from = 3, to = "below"), @ViewDebug.IntToString(from = 14, to = "centerHorizontal"), @ViewDebug.IntToString(from = 13, to = "center"), @ViewDebug.IntToString(from = 15, to = "centerVertical"), @ViewDebug.IntToString(from = 0, to = "leftOf"), @ViewDebug.IntToString(from = 1, to = "rightOf"), @ViewDebug.IntToString(from = 18, to = "alignStart"), @ViewDebug.IntToString(from = 19, to = "alignEnd"), @ViewDebug.IntToString(from = 20, to = "alignParentStart"), @ViewDebug.IntToString(from = 21, to = "alignParentEnd"), @ViewDebug.IntToString(from = 16, to = "startOf"), @ViewDebug.IntToString(from = 17, to = "endOf")}, mapping = {@ViewDebug.IntToString(from = -1, to = "true"), @ViewDebug.IntToString(from = 0, to = "false/NO_ID")}, resolveId = true)
        public int[] mRules = new int[22];
        private boolean mRulesChanged = false;
        /* access modifiers changed from: private */
        public int mTop;

        static /* synthetic */ int access$112(LayoutParams x0, int x1) {
            int i = x0.mLeft + x1;
            x0.mLeft = i;
            return i;
        }

        static /* synthetic */ int access$120(LayoutParams x0, int x1) {
            int i = x0.mLeft - x1;
            x0.mLeft = i;
            return i;
        }

        static /* synthetic */ int access$212(LayoutParams x0, int x1) {
            int i = x0.mRight + x1;
            x0.mRight = i;
            return i;
        }

        static /* synthetic */ int access$220(LayoutParams x0, int x1) {
            int i = x0.mRight - x1;
            x0.mRight = i;
            return i;
        }

        static /* synthetic */ int access$312(LayoutParams x0, int x1) {
            int i = x0.mBottom + x1;
            x0.mBottom = i;
            return i;
        }

        static /* synthetic */ int access$412(LayoutParams x0, int x1) {
            int i = x0.mTop + x1;
            x0.mTop = i;
            return i;
        }

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.RelativeLayout_Layout);
            this.mIsRtlCompatibilityMode = c.getApplicationInfo().targetSdkVersion < 17 || !c.getApplicationInfo().hasRtlSupport();
            int[] rules = this.mRules;
            int[] initialRules = this.mInitialRules;
            int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                int i2 = -1;
                switch (attr) {
                    case 0:
                        rules[0] = a.getResourceId(attr, 0);
                        break;
                    case 1:
                        rules[1] = a.getResourceId(attr, 0);
                        break;
                    case 2:
                        rules[2] = a.getResourceId(attr, 0);
                        break;
                    case 3:
                        rules[3] = a.getResourceId(attr, 0);
                        break;
                    case 4:
                        rules[4] = a.getResourceId(attr, 0);
                        break;
                    case 5:
                        rules[5] = a.getResourceId(attr, 0);
                        break;
                    case 6:
                        rules[6] = a.getResourceId(attr, 0);
                        break;
                    case 7:
                        rules[7] = a.getResourceId(attr, 0);
                        break;
                    case 8:
                        rules[8] = a.getResourceId(attr, 0);
                        break;
                    case 9:
                        rules[9] = !a.getBoolean(attr, false) ? 0 : i2;
                        break;
                    case 10:
                        rules[10] = !a.getBoolean(attr, false) ? 0 : i2;
                        break;
                    case 11:
                        rules[11] = !a.getBoolean(attr, false) ? 0 : i2;
                        break;
                    case 12:
                        rules[12] = !a.getBoolean(attr, false) ? 0 : i2;
                        break;
                    case 13:
                        rules[13] = !a.getBoolean(attr, false) ? 0 : i2;
                        break;
                    case 14:
                        rules[14] = !a.getBoolean(attr, false) ? 0 : i2;
                        break;
                    case 15:
                        rules[15] = !a.getBoolean(attr, false) ? 0 : i2;
                        break;
                    case 16:
                        this.alignWithParent = a.getBoolean(attr, false);
                        break;
                    case 17:
                        rules[16] = a.getResourceId(attr, 0);
                        break;
                    case 18:
                        rules[17] = a.getResourceId(attr, 0);
                        break;
                    case 19:
                        rules[18] = a.getResourceId(attr, 0);
                        break;
                    case 20:
                        rules[19] = a.getResourceId(attr, 0);
                        break;
                    case 21:
                        rules[20] = !a.getBoolean(attr, false) ? 0 : i2;
                        break;
                    case 22:
                        rules[21] = !a.getBoolean(attr, false) ? 0 : i2;
                        break;
                }
            }
            this.mRulesChanged = true;
            System.arraycopy(rules, 0, initialRules, 0, 22);
            a.recycle();
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super((ViewGroup.MarginLayoutParams) source);
            this.mIsRtlCompatibilityMode = source.mIsRtlCompatibilityMode;
            this.mRulesChanged = source.mRulesChanged;
            this.alignWithParent = source.alignWithParent;
            System.arraycopy(source.mRules, 0, this.mRules, 0, 22);
            System.arraycopy(source.mInitialRules, 0, this.mInitialRules, 0, 22);
        }

        public String debug(String output) {
            return output + "ViewGroup.LayoutParams={ width=" + sizeToString(this.width) + ", height=" + sizeToString(this.height) + " }";
        }

        public void addRule(int verb) {
            addRule(verb, -1);
        }

        public void addRule(int verb, int subject) {
            if (!this.mNeedsLayoutResolution && isRelativeRule(verb) && this.mInitialRules[verb] != 0 && subject == 0) {
                this.mNeedsLayoutResolution = true;
            }
            this.mRules[verb] = subject;
            this.mInitialRules[verb] = subject;
            this.mRulesChanged = true;
        }

        public void removeRule(int verb) {
            addRule(verb, 0);
        }

        public int getRule(int verb) {
            return this.mRules[verb];
        }

        private boolean hasRelativeRules() {
            return (this.mInitialRules[16] == 0 && this.mInitialRules[17] == 0 && this.mInitialRules[18] == 0 && this.mInitialRules[19] == 0 && this.mInitialRules[20] == 0 && this.mInitialRules[21] == 0) ? false : true;
        }

        private boolean isRelativeRule(int rule) {
            return rule == 16 || rule == 17 || rule == 18 || rule == 19 || rule == 20 || rule == 21;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:95:0x0175, code lost:
            if (r0.mRules[11] != 0) goto L_0x017a;
         */
        private void resolveRules(int layoutDirection) {
            char c;
            char c2 = 1;
            boolean isLayoutRtl = layoutDirection == 1;
            System.arraycopy(this.mInitialRules, 0, this.mRules, 0, 22);
            if (this.mIsRtlCompatibilityMode) {
                if (this.mRules[18] != 0) {
                    if (this.mRules[5] == 0) {
                        this.mRules[5] = this.mRules[18];
                    }
                    this.mRules[18] = 0;
                }
                if (this.mRules[19] != 0) {
                    if (this.mRules[7] == 0) {
                        this.mRules[7] = this.mRules[19];
                    }
                    this.mRules[19] = 0;
                }
                if (this.mRules[16] != 0) {
                    if (this.mRules[0] == 0) {
                        this.mRules[0] = this.mRules[16];
                    }
                    this.mRules[16] = 0;
                }
                if (this.mRules[17] != 0) {
                    if (this.mRules[1] == 0) {
                        this.mRules[1] = this.mRules[17];
                    }
                    this.mRules[17] = 0;
                }
                if (this.mRules[20] != 0) {
                    if (this.mRules[9] == 0) {
                        this.mRules[9] = this.mRules[20];
                    }
                    this.mRules[20] = 0;
                }
                if (this.mRules[21] != 0) {
                    if (this.mRules[11] == 0) {
                        this.mRules[11] = this.mRules[21];
                    }
                    this.mRules[21] = 0;
                }
            } else {
                if (!((this.mRules[18] == 0 && this.mRules[19] == 0) || (this.mRules[5] == 0 && this.mRules[7] == 0))) {
                    this.mRules[5] = 0;
                    this.mRules[7] = 0;
                }
                if (this.mRules[18] != 0) {
                    this.mRules[isLayoutRtl ? (char) 7 : 5] = this.mRules[18];
                    this.mRules[18] = 0;
                }
                if (this.mRules[19] != 0) {
                    this.mRules[isLayoutRtl ? (char) 5 : 7] = this.mRules[19];
                    this.mRules[19] = 0;
                }
                if (!((this.mRules[16] == 0 && this.mRules[17] == 0) || (this.mRules[0] == 0 && this.mRules[1] == 0))) {
                    this.mRules[0] = 0;
                    this.mRules[1] = 0;
                }
                if (this.mRules[16] != 0) {
                    this.mRules[isLayoutRtl ? (char) 1 : 0] = this.mRules[16];
                    this.mRules[16] = 0;
                }
                if (this.mRules[17] != 0) {
                    int[] iArr = this.mRules;
                    if (isLayoutRtl) {
                        c2 = 0;
                    }
                    iArr[c2] = this.mRules[17];
                    this.mRules[17] = 0;
                }
                if (this.mRules[20] == 0 && this.mRules[21] == 0) {
                    c = 11;
                } else {
                    if (this.mRules[9] == 0) {
                        c = 11;
                    } else {
                        c = 11;
                    }
                    this.mRules[9] = 0;
                    this.mRules[c] = 0;
                }
                if (this.mRules[20] != 0) {
                    this.mRules[isLayoutRtl ? c : 9] = this.mRules[20];
                    this.mRules[20] = 0;
                }
                if (this.mRules[21] != 0) {
                    int[] iArr2 = this.mRules;
                    if (isLayoutRtl) {
                        c = 9;
                    }
                    iArr2[c] = this.mRules[21];
                    this.mRules[21] = 0;
                }
            }
            this.mRulesChanged = false;
            this.mNeedsLayoutResolution = false;
        }

        public int[] getRules(int layoutDirection) {
            resolveLayoutDirection(layoutDirection);
            return this.mRules;
        }

        public int[] getRules() {
            return this.mRules;
        }

        public void resolveLayoutDirection(int layoutDirection) {
            if (shouldResolveLayoutDirection(layoutDirection)) {
                resolveRules(layoutDirection);
            }
            super.resolveLayoutDirection(layoutDirection);
        }

        private boolean shouldResolveLayoutDirection(int layoutDirection) {
            return (this.mNeedsLayoutResolution || hasRelativeRules()) && (this.mRulesChanged || layoutDirection != getLayoutDirection());
        }

        /* access modifiers changed from: protected */
        public void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("layout:alignWithParent", this.alignWithParent);
        }
    }

    private class TopToBottomLeftToRightComparator implements Comparator<View> {
        private TopToBottomLeftToRightComparator() {
        }

        public int compare(View first, View second) {
            int topDifference = first.getTop() - second.getTop();
            if (topDifference != 0) {
                return topDifference;
            }
            int leftDifference = first.getLeft() - second.getLeft();
            if (leftDifference != 0) {
                return leftDifference;
            }
            int heightDiference = first.getHeight() - second.getHeight();
            if (heightDiference != 0) {
                return heightDiference;
            }
            int widthDiference = first.getWidth() - second.getWidth();
            if (widthDiference != 0) {
                return widthDiference;
            }
            return 0;
        }
    }

    public RelativeLayout(Context context) {
        this(context, null);
    }

    public RelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mBaselineView = null;
        this.mGravity = 8388659;
        this.mContentBounds = new Rect();
        this.mSelfBounds = new Rect();
        this.mTopToBottomLeftToRightSet = null;
        this.mGraph = new DependencyGraph();
        this.mAllowBrokenMeasureSpecs = false;
        this.mMeasureVerticalWithPaddingMargin = false;
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
        queryCompatibilityModes(context);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RelativeLayout, defStyleAttr, defStyleRes);
        this.mIgnoreGravity = a.getResourceId(1, -1);
        this.mGravity = a.getInt(0, this.mGravity);
        a.recycle();
    }

    private void queryCompatibilityModes(Context context) {
        int version = context.getApplicationInfo().targetSdkVersion;
        boolean z = false;
        this.mAllowBrokenMeasureSpecs = version <= 17;
        if (version >= 18) {
            z = true;
        }
        this.mMeasureVerticalWithPaddingMargin = z;
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @RemotableViewMethod
    public void setIgnoreGravity(int viewId) {
        this.mIgnoreGravity = viewId;
    }

    public int getGravity() {
        return this.mGravity;
    }

    @RemotableViewMethod
    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            if ((8388615 & gravity) == 0) {
                gravity |= Gravity.START;
            }
            if ((gravity & 112) == 0) {
                gravity |= 48;
            }
            this.mGravity = gravity;
            requestLayout();
        }
    }

    @RemotableViewMethod
    public void setHorizontalGravity(int horizontalGravity) {
        int gravity = horizontalGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if ((8388615 & this.mGravity) != gravity) {
            this.mGravity = (this.mGravity & -8388616) | gravity;
            requestLayout();
        }
    }

    @RemotableViewMethod
    public void setVerticalGravity(int verticalGravity) {
        int gravity = verticalGravity & 112;
        if ((this.mGravity & 112) != gravity) {
            this.mGravity = (this.mGravity & -113) | gravity;
            requestLayout();
        }
    }

    public int getBaseline() {
        return this.mBaselineView != null ? this.mBaselineView.getBaseline() : super.getBaseline();
    }

    public void requestLayout() {
        super.requestLayout();
        this.mDirtyHierarchy = true;
    }

    private void sortChildren() {
        int count = getChildCount();
        if (this.mSortedVerticalChildren == null || this.mSortedVerticalChildren.length != count) {
            this.mSortedVerticalChildren = new View[count];
        }
        if (this.mSortedHorizontalChildren == null || this.mSortedHorizontalChildren.length != count) {
            this.mSortedHorizontalChildren = new View[count];
        }
        DependencyGraph graph = this.mGraph;
        graph.clear();
        for (int i = 0; i < count; i++) {
            graph.add(getChildAt(i));
        }
        graph.getSortedViews(this.mSortedVerticalChildren, RULES_VERTICAL);
        graph.getSortedViews(this.mSortedHorizontalChildren, RULES_HORIZONTAL);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x03a9  */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int count;
        View ignore;
        LayoutParams baselineParams;
        View baselineView;
        int layoutDirection;
        int myHeight;
        if (this.mDirtyHierarchy) {
            this.mDirtyHierarchy = false;
            sortChildren();
        }
        int myWidth = -1;
        int myHeight2 = -1;
        int width2 = 0;
        int height = 0;
        int i = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        if (i != 0) {
            myWidth = widthSize;
        }
        if (heightMode != 0) {
            myHeight2 = heightSize;
        }
        if (i == 1073741824) {
            width2 = myWidth;
        }
        if (heightMode == 1073741824) {
            height = myHeight2;
        }
        View ignore2 = null;
        int gravity = this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        boolean horizontalGravity = (gravity == 8388611 || gravity == 0) ? false : true;
        int gravity2 = this.mGravity & 112;
        boolean verticalGravity = (gravity2 == 48 || gravity2 == 0) ? false : true;
        boolean offsetVerticalAxis = false;
        if ((horizontalGravity || verticalGravity) && this.mIgnoreGravity != -1) {
            ignore2 = findViewById(this.mIgnoreGravity);
        }
        boolean isWrapContentWidth = i != 1073741824;
        boolean isWrapContentHeight = heightMode != 1073741824;
        int layoutDirection2 = getLayoutDirection();
        if (isLayoutRtl()) {
            width = width2;
            if (myWidth == -1) {
                myWidth = 65536;
            }
        } else {
            width = width2;
        }
        View[] views = this.mSortedHorizontalChildren;
        int height2 = height;
        int count2 = views.length;
        boolean offsetHorizontalAxis = false;
        int i2 = 0;
        while (true) {
            int widthMode = i;
            int i3 = i2;
            if (i3 >= count2) {
                break;
            }
            int count3 = count2;
            View child = views[i3];
            View[] views2 = views;
            int heightMode2 = heightMode;
            if (child.getVisibility() != 8) {
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                applyHorizontalSizeRules(params, myWidth, params.getRules(layoutDirection2));
                measureChildHorizontal(child, params, myWidth, myHeight2);
                if (positionChildHorizontal(child, params, myWidth, isWrapContentWidth)) {
                    offsetHorizontalAxis = true;
                }
            }
            i2 = i3 + 1;
            i = widthMode;
            count2 = count3;
            views = views2;
            heightMode = heightMode2;
        }
        int i4 = count2;
        int i5 = heightMode;
        View[] views3 = this.mSortedVerticalChildren;
        int count4 = views3.length;
        int count5 = getContext().getApplicationInfo().targetSdkVersion;
        int layoutDirection3 = layoutDirection2;
        int i6 = widthSize;
        int i7 = heightSize;
        int i8 = gravity2;
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
        int width3 = width;
        int height3 = height2;
        int i9 = 0;
        while (i9 < count4) {
            int count6 = count4;
            View child2 = views3[i9];
            View[] views4 = views3;
            int i10 = i9;
            if (child2.getVisibility() != 8) {
                LayoutParams params2 = (LayoutParams) child2.getLayoutParams();
                applyVerticalSizeRules(params2, myHeight2, child2.getBaseline());
                measureChild(child2, params2, myWidth, myHeight2);
                if (positionChildVertical(child2, params2, myHeight2, isWrapContentHeight)) {
                    offsetVerticalAxis = true;
                }
                if (!isWrapContentWidth) {
                    myHeight = myHeight2;
                } else if (!isLayoutRtl()) {
                    myHeight = myHeight2;
                    if (count5 < 19) {
                        width3 = Math.max(width3, params2.mRight);
                    } else {
                        width3 = Math.max(width3, params2.mRight + params2.rightMargin);
                    }
                } else if (count5 < 19) {
                    width3 = Math.max(width3, myWidth - params2.mLeft);
                    myHeight = myHeight2;
                } else {
                    myHeight = myHeight2;
                    width3 = Math.max(width3, (myWidth - params2.mLeft) + params2.leftMargin);
                }
                if (isWrapContentHeight) {
                    if (count5 < 19) {
                        height3 = Math.max(height3, params2.mBottom);
                    } else {
                        height3 = Math.max(height3, params2.mBottom + params2.bottomMargin);
                    }
                }
                if (child2 != ignore2 || verticalGravity) {
                    left = Math.min(left, params2.mLeft - params2.leftMargin);
                    top = Math.min(top, params2.mTop - params2.topMargin);
                }
                if (child2 != ignore2 || horizontalGravity) {
                    int right2 = Math.max(right, params2.mRight + params2.rightMargin);
                    bottom = Math.max(bottom, params2.mBottom + params2.bottomMargin);
                    right = right2;
                    top = top;
                }
            } else {
                myHeight = myHeight2;
                int i11 = right;
                int i12 = bottom;
            }
            i9 = i10 + 1;
            count4 = count6;
            views3 = views4;
            myHeight2 = myHeight;
        }
        View[] views5 = views3;
        int count7 = count4;
        int right3 = right;
        int bottom2 = bottom;
        LayoutParams baselineParams2 = null;
        View baselineView2 = null;
        int i13 = 0;
        while (true) {
            int targetSdkVersion = count5;
            count = count7;
            if (i13 >= count) {
                break;
            }
            int myWidth2 = myWidth;
            View child3 = views5[i13];
            View ignore3 = ignore2;
            int top2 = top;
            if (child3.getVisibility() != 8) {
                LayoutParams childParams = (LayoutParams) child3.getLayoutParams();
                if (baselineView2 == null || baselineParams2 == null || compareLayoutPosition(childParams, baselineParams2) < 0) {
                    baselineView2 = child3;
                    baselineParams2 = childParams;
                }
            }
            i13++;
            count7 = count;
            count5 = targetSdkVersion;
            myWidth = myWidth2;
            ignore2 = ignore3;
            top = top2;
        }
        int myWidth3 = myWidth;
        int top3 = top;
        View ignore4 = ignore2;
        this.mBaselineView = baselineView2;
        if (isWrapContentWidth) {
            int width4 = width3 + this.mPaddingRight;
            if (this.mLayoutParams != null && this.mLayoutParams.width >= 0) {
                width4 = Math.max(width4, this.mLayoutParams.width);
            }
            width3 = resolveSize(Math.max(width4, getSuggestedMinimumWidth()), widthMeasureSpec);
            if (offsetHorizontalAxis) {
                int i14 = 0;
                while (i14 < count) {
                    View child4 = views5[i14];
                    if (child4.getVisibility() != 8) {
                        LayoutParams params3 = (LayoutParams) child4.getLayoutParams();
                        layoutDirection = layoutDirection3;
                        int[] rules = params3.getRules(layoutDirection);
                        if (rules[13] != 0) {
                            baselineView = baselineView2;
                        } else if (rules[14] != 0) {
                            baselineView = baselineView2;
                        } else if (rules[11] != 0) {
                            int childWidth = child4.getMeasuredWidth();
                            baselineView = baselineView2;
                            int unused = params3.mLeft = (width3 - this.mPaddingRight) - childWidth;
                            int unused2 = params3.mRight = params3.mLeft + childWidth;
                        } else {
                            baselineView = baselineView2;
                        }
                        centerHorizontal(child4, params3, width3);
                    } else {
                        baselineView = baselineView2;
                        layoutDirection = layoutDirection3;
                    }
                    i14++;
                    layoutDirection3 = layoutDirection;
                    baselineView2 = baselineView;
                    int layoutDirection4 = widthMeasureSpec;
                }
            }
        }
        int layoutDirection5 = layoutDirection3;
        if (isWrapContentHeight) {
            int height4 = height3 + this.mPaddingBottom;
            if (this.mLayoutParams != null && this.mLayoutParams.height >= 0) {
                height4 = Math.max(height4, this.mLayoutParams.height);
            }
            height3 = resolveSize(Math.max(height4, getSuggestedMinimumHeight()), heightMeasureSpec);
            if (offsetVerticalAxis) {
                int i15 = 0;
                while (i15 < count) {
                    View child5 = views5[i15];
                    if (child5.getVisibility() != 8) {
                        LayoutParams params4 = (LayoutParams) child5.getLayoutParams();
                        int[] rules2 = params4.getRules(layoutDirection5);
                        if (rules2[13] != 0) {
                            baselineParams = baselineParams2;
                        } else if (rules2[15] != 0) {
                            baselineParams = baselineParams2;
                        } else if (rules2[12] != 0) {
                            int childHeight = child5.getMeasuredHeight();
                            baselineParams = baselineParams2;
                            int unused3 = params4.mTop = (height3 - this.mPaddingBottom) - childHeight;
                            int unused4 = params4.mBottom = params4.mTop + childHeight;
                        } else {
                            baselineParams = baselineParams2;
                        }
                        centerVertical(child5, params4, height3);
                    } else {
                        baselineParams = baselineParams2;
                    }
                    i15++;
                    baselineParams2 = baselineParams;
                    int i16 = heightMeasureSpec;
                }
            }
        }
        if (horizontalGravity || verticalGravity) {
            Rect selfBounds = this.mSelfBounds;
            selfBounds.set(this.mPaddingLeft, this.mPaddingTop, width3 - this.mPaddingRight, height3 - this.mPaddingBottom);
            Rect contentBounds = this.mContentBounds;
            Gravity.apply(this.mGravity, right3 - left, bottom2 - top3, selfBounds, contentBounds, layoutDirection5);
            int horizontalOffset = contentBounds.left - left;
            int verticalOffset = contentBounds.top - top3;
            if (!(horizontalOffset == 0 && verticalOffset == 0)) {
                int i17 = 0;
                while (i17 < count) {
                    Rect selfBounds2 = selfBounds;
                    View child6 = views5[i17];
                    Rect contentBounds2 = contentBounds;
                    int layoutDirection6 = layoutDirection5;
                    if (child6.getVisibility() != 8) {
                        ignore = ignore4;
                        if (child6 != ignore) {
                            LayoutParams params5 = (LayoutParams) child6.getLayoutParams();
                            if (horizontalGravity) {
                                LayoutParams.access$112(params5, horizontalOffset);
                                LayoutParams.access$212(params5, horizontalOffset);
                            }
                            if (verticalGravity) {
                                LayoutParams.access$412(params5, verticalOffset);
                                LayoutParams.access$312(params5, verticalOffset);
                            }
                        }
                    } else {
                        ignore = ignore4;
                    }
                    i17++;
                    ignore4 = ignore;
                    selfBounds = selfBounds2;
                    contentBounds = contentBounds2;
                    layoutDirection5 = layoutDirection6;
                }
                View view = ignore4;
                if (isLayoutRtl()) {
                    int offsetWidth = myWidth3 - width3;
                    int i18 = 0;
                    while (true) {
                        int i19 = i18;
                        if (i19 >= count) {
                            break;
                        }
                        View child7 = views5[i19];
                        if (child7.getVisibility() != 8) {
                            LayoutParams params6 = (LayoutParams) child7.getLayoutParams();
                            LayoutParams.access$120(params6, offsetWidth);
                            LayoutParams.access$220(params6, offsetWidth);
                        }
                        i18 = i19 + 1;
                    }
                }
                setMeasuredDimension(width3, height3);
            }
        }
        int i20 = layoutDirection5;
        View view2 = ignore4;
        if (isLayoutRtl()) {
        }
        setMeasuredDimension(width3, height3);
    }

    private int compareLayoutPosition(LayoutParams p1, LayoutParams p2) {
        int topDiff = p1.mTop - p2.mTop;
        if (topDiff != 0) {
            return topDiff;
        }
        return p1.mLeft - p2.mLeft;
    }

    private void measureChild(View child, LayoutParams params, int myWidth, int myHeight) {
        child.measure(getChildMeasureSpec(params.mLeft, params.mRight, params.width, params.leftMargin, params.rightMargin, this.mPaddingLeft, this.mPaddingRight, myWidth), getChildMeasureSpec(params.mTop, params.mBottom, params.height, params.topMargin, params.bottomMargin, this.mPaddingTop, this.mPaddingBottom, myHeight));
    }

    private void measureChildHorizontal(View child, LayoutParams params, int myWidth, int myHeight) {
        int maxHeight;
        int maxHeight2;
        int heightMode;
        int childWidthMeasureSpec = getChildMeasureSpec(params.mLeft, params.mRight, params.width, params.leftMargin, params.rightMargin, this.mPaddingLeft, this.mPaddingRight, myWidth);
        if (myHeight >= 0 || this.mAllowBrokenMeasureSpecs) {
            if (this.mMeasureVerticalWithPaddingMargin) {
                maxHeight2 = Math.max(0, (((myHeight - this.mPaddingTop) - this.mPaddingBottom) - params.topMargin) - params.bottomMargin);
            } else {
                maxHeight2 = Math.max(0, myHeight);
            }
            if (params.height == -1) {
                heightMode = 1073741824;
            } else {
                heightMode = Integer.MIN_VALUE;
            }
            maxHeight = View.MeasureSpec.makeMeasureSpec(maxHeight2, heightMode);
        } else if (params.height >= 0) {
            maxHeight = View.MeasureSpec.makeMeasureSpec(params.height, 1073741824);
        } else {
            maxHeight = View.MeasureSpec.makeMeasureSpec(0, 0);
        }
        child.measure(childWidthMeasureSpec, maxHeight);
    }

    private int getChildMeasureSpec(int childStart, int childEnd, int childSize, int startMargin, int endMargin, int startPadding, int endPadding, int mySize) {
        int childSpecSize;
        int childSpecMode;
        int i = childStart;
        int i2 = childEnd;
        int i3 = childSize;
        int childSpecMode2 = 0;
        int childSpecSize2 = 0;
        boolean isUnspecified = mySize < 0;
        if (isUnspecified) {
            if (!this.mAllowBrokenMeasureSpecs) {
                if (i != Integer.MIN_VALUE && i2 != Integer.MIN_VALUE) {
                    childSpecSize = Math.max(0, i2 - i);
                    childSpecMode = 1073741824;
                } else if (i3 >= 0) {
                    childSpecSize = i3;
                    childSpecMode = 1073741824;
                } else {
                    childSpecSize = 0;
                    childSpecMode = 0;
                }
                return View.MeasureSpec.makeMeasureSpec(childSpecSize, childSpecMode);
            }
        }
        int tempStart = i;
        int tempEnd = i2;
        if (tempStart == Integer.MIN_VALUE) {
            tempStart = startPadding + startMargin;
        }
        if (tempEnd == Integer.MIN_VALUE) {
            tempEnd = (mySize - endPadding) - endMargin;
        }
        int maxAvailable = tempEnd - tempStart;
        int i4 = 1073741824;
        if (i != Integer.MIN_VALUE && i2 != Integer.MIN_VALUE) {
            if (isUnspecified) {
                i4 = 0;
            }
            childSpecMode2 = i4;
            childSpecSize2 = Math.max(0, maxAvailable);
        } else if (i3 >= 0) {
            childSpecMode2 = 1073741824;
            if (maxAvailable >= 0) {
                childSpecSize2 = Math.min(maxAvailable, i3);
            } else {
                childSpecSize2 = i3;
            }
        } else if (i3 == -1) {
            if (isUnspecified) {
                i4 = 0;
            }
            childSpecMode2 = i4;
            childSpecSize2 = Math.max(0, maxAvailable);
        } else if (i3 == -2) {
            if (maxAvailable >= 0) {
                childSpecMode2 = Integer.MIN_VALUE;
                childSpecSize2 = maxAvailable;
            } else {
                childSpecMode2 = 0;
                childSpecSize2 = 0;
            }
        }
        return View.MeasureSpec.makeMeasureSpec(childSpecSize2, childSpecMode2);
    }

    private boolean positionChildHorizontal(View child, LayoutParams params, int myWidth, boolean wrapContent) {
        int[] rules = params.getRules(getLayoutDirection());
        boolean z = true;
        if (params.mLeft == Integer.MIN_VALUE && params.mRight != Integer.MIN_VALUE) {
            int unused = params.mLeft = params.mRight - child.getMeasuredWidth();
        } else if (params.mLeft != Integer.MIN_VALUE && params.mRight == Integer.MIN_VALUE) {
            int unused2 = params.mRight = params.mLeft + child.getMeasuredWidth();
        } else if (params.mLeft == Integer.MIN_VALUE && params.mRight == Integer.MIN_VALUE) {
            if (rules[13] == 0 && rules[14] == 0) {
                positionAtEdge(child, params, myWidth);
            } else {
                if (!wrapContent) {
                    centerHorizontal(child, params, myWidth);
                } else {
                    positionAtEdge(child, params, myWidth);
                }
                return true;
            }
        }
        if (rules[21] == 0) {
            z = false;
        }
        return z;
    }

    private void positionAtEdge(View child, LayoutParams params, int myWidth) {
        if (isLayoutRtl()) {
            int unused = params.mRight = (myWidth - this.mPaddingRight) - params.rightMargin;
            int unused2 = params.mLeft = params.mRight - child.getMeasuredWidth();
            return;
        }
        int unused3 = params.mLeft = this.mPaddingLeft + params.leftMargin;
        int unused4 = params.mRight = params.mLeft + child.getMeasuredWidth();
    }

    private boolean positionChildVertical(View child, LayoutParams params, int myHeight, boolean wrapContent) {
        int[] rules = params.getRules();
        boolean z = true;
        if (params.mTop == Integer.MIN_VALUE && params.mBottom != Integer.MIN_VALUE) {
            int unused = params.mTop = params.mBottom - child.getMeasuredHeight();
        } else if (params.mTop != Integer.MIN_VALUE && params.mBottom == Integer.MIN_VALUE) {
            int unused2 = params.mBottom = params.mTop + child.getMeasuredHeight();
        } else if (params.mTop == Integer.MIN_VALUE && params.mBottom == Integer.MIN_VALUE) {
            if (rules[13] == 0 && rules[15] == 0) {
                int unused3 = params.mTop = this.mPaddingTop + params.topMargin;
                int unused4 = params.mBottom = params.mTop + child.getMeasuredHeight();
            } else {
                if (!wrapContent) {
                    centerVertical(child, params, myHeight);
                } else {
                    int unused5 = params.mTop = this.mPaddingTop + params.topMargin;
                    int unused6 = params.mBottom = params.mTop + child.getMeasuredHeight();
                }
                return true;
            }
        }
        if (rules[12] == 0) {
            z = false;
        }
        return z;
    }

    private void applyHorizontalSizeRules(LayoutParams childParams, int myWidth, int[] rules) {
        int unused = childParams.mLeft = Integer.MIN_VALUE;
        int unused2 = childParams.mRight = Integer.MIN_VALUE;
        LayoutParams anchorParams = getRelatedViewParams(rules, 0);
        if (anchorParams != null) {
            int unused3 = childParams.mRight = anchorParams.mLeft - (anchorParams.leftMargin + childParams.rightMargin);
        } else if (childParams.alignWithParent && rules[0] != 0 && myWidth >= 0) {
            int unused4 = childParams.mRight = (myWidth - this.mPaddingRight) - childParams.rightMargin;
        }
        LayoutParams anchorParams2 = getRelatedViewParams(rules, 1);
        if (anchorParams2 != null) {
            int unused5 = childParams.mLeft = anchorParams2.mRight + anchorParams2.rightMargin + childParams.leftMargin;
        } else if (childParams.alignWithParent && rules[1] != 0) {
            int unused6 = childParams.mLeft = this.mPaddingLeft + childParams.leftMargin;
        }
        LayoutParams anchorParams3 = getRelatedViewParams(rules, 5);
        if (anchorParams3 != null) {
            int unused7 = childParams.mLeft = anchorParams3.mLeft + childParams.leftMargin;
        } else if (childParams.alignWithParent && rules[5] != 0) {
            int unused8 = childParams.mLeft = this.mPaddingLeft + childParams.leftMargin;
        }
        LayoutParams anchorParams4 = getRelatedViewParams(rules, 7);
        if (anchorParams4 != null) {
            int unused9 = childParams.mRight = anchorParams4.mRight - childParams.rightMargin;
        } else if (childParams.alignWithParent && rules[7] != 0 && myWidth >= 0) {
            int unused10 = childParams.mRight = (myWidth - this.mPaddingRight) - childParams.rightMargin;
        }
        if (rules[9] != 0) {
            int unused11 = childParams.mLeft = this.mPaddingLeft + childParams.leftMargin;
        }
        if (rules[11] != 0 && myWidth >= 0) {
            int unused12 = childParams.mRight = (myWidth - this.mPaddingRight) - childParams.rightMargin;
        }
    }

    private void applyVerticalSizeRules(LayoutParams childParams, int myHeight, int myBaseline) {
        int[] rules = childParams.getRules();
        int baselineOffset = getRelatedViewBaselineOffset(rules);
        if (baselineOffset != -1) {
            if (myBaseline != -1) {
                baselineOffset -= myBaseline;
            }
            int unused = childParams.mTop = baselineOffset;
            int unused2 = childParams.mBottom = Integer.MIN_VALUE;
            return;
        }
        int unused3 = childParams.mTop = Integer.MIN_VALUE;
        int unused4 = childParams.mBottom = Integer.MIN_VALUE;
        LayoutParams anchorParams = getRelatedViewParams(rules, 2);
        if (anchorParams != null) {
            int unused5 = childParams.mBottom = anchorParams.mTop - (anchorParams.topMargin + childParams.bottomMargin);
        } else if (childParams.alignWithParent && rules[2] != 0 && myHeight >= 0) {
            int unused6 = childParams.mBottom = (myHeight - this.mPaddingBottom) - childParams.bottomMargin;
        }
        LayoutParams anchorParams2 = getRelatedViewParams(rules, 3);
        if (anchorParams2 != null) {
            int unused7 = childParams.mTop = anchorParams2.mBottom + anchorParams2.bottomMargin + childParams.topMargin;
        } else if (childParams.alignWithParent && rules[3] != 0) {
            int unused8 = childParams.mTop = this.mPaddingTop + childParams.topMargin;
        }
        LayoutParams anchorParams3 = getRelatedViewParams(rules, 6);
        if (anchorParams3 != null) {
            int unused9 = childParams.mTop = anchorParams3.mTop + childParams.topMargin;
        } else if (childParams.alignWithParent && rules[6] != 0) {
            int unused10 = childParams.mTop = this.mPaddingTop + childParams.topMargin;
        }
        LayoutParams anchorParams4 = getRelatedViewParams(rules, 8);
        if (anchorParams4 != null) {
            int unused11 = childParams.mBottom = anchorParams4.mBottom - childParams.bottomMargin;
        } else if (childParams.alignWithParent && rules[8] != 0 && myHeight >= 0) {
            int unused12 = childParams.mBottom = (myHeight - this.mPaddingBottom) - childParams.bottomMargin;
        }
        if (rules[10] != 0) {
            int unused13 = childParams.mTop = this.mPaddingTop + childParams.topMargin;
        }
        if (rules[12] != 0 && myHeight >= 0) {
            int unused14 = childParams.mBottom = (myHeight - this.mPaddingBottom) - childParams.bottomMargin;
        }
    }

    private View getRelatedView(int[] rules, int relation) {
        int id = rules[relation];
        if (id == 0) {
            return null;
        }
        DependencyGraph.Node node = (DependencyGraph.Node) this.mGraph.mKeyNodes.get(id);
        if (node == null) {
            return null;
        }
        View v = node.view;
        while (v.getVisibility() == 8) {
            DependencyGraph.Node node2 = (DependencyGraph.Node) this.mGraph.mKeyNodes.get(((LayoutParams) v.getLayoutParams()).getRules(v.getLayoutDirection())[relation]);
            if (node2 == null || v == node2.view) {
                return null;
            }
            v = node2.view;
        }
        return v;
    }

    private LayoutParams getRelatedViewParams(int[] rules, int relation) {
        View v = getRelatedView(rules, relation);
        if (v == null || !(v.getLayoutParams() instanceof LayoutParams)) {
            return null;
        }
        return (LayoutParams) v.getLayoutParams();
    }

    private int getRelatedViewBaselineOffset(int[] rules) {
        View v = getRelatedView(rules, 4);
        if (v != null) {
            int baseline = v.getBaseline();
            if (baseline != -1 && (v.getLayoutParams() instanceof LayoutParams)) {
                return ((LayoutParams) v.getLayoutParams()).mTop + baseline;
            }
        }
        return -1;
    }

    private static void centerHorizontal(View child, LayoutParams params, int myWidth) {
        int childWidth = child.getMeasuredWidth();
        int left = (myWidth - childWidth) / 2;
        int unused = params.mLeft = left;
        int unused2 = params.mRight = left + childWidth;
    }

    private static void centerVertical(View child, LayoutParams params, int myHeight) {
        int childHeight = child.getMeasuredHeight();
        int top = (myHeight - childHeight) / 2;
        int unused = params.mTop = top;
        int unused2 = params.mBottom = top + childHeight;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams st = (LayoutParams) child.getLayoutParams();
                child.layout(st.mLeft, st.mTop, st.mRight, st.mBottom);
            }
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /* access modifiers changed from: protected */
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
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

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        if (this.mTopToBottomLeftToRightSet == null) {
            this.mTopToBottomLeftToRightSet = new TreeSet(new TopToBottomLeftToRightComparator());
        }
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            this.mTopToBottomLeftToRightSet.add(getChildAt(i));
        }
        for (View view : this.mTopToBottomLeftToRightSet) {
            if (view.getVisibility() == 0 && view.dispatchPopulateAccessibilityEvent(event)) {
                this.mTopToBottomLeftToRightSet.clear();
                return true;
            }
        }
        this.mTopToBottomLeftToRightSet.clear();
        return false;
    }

    public CharSequence getAccessibilityClassName() {
        return RelativeLayout.class.getName();
    }
}
