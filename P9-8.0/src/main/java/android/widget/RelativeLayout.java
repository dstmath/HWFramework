package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Pools.SynchronizedPool;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.IntToString;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewHierarchyEncoder;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

@RemoteView
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
    private static final int[] RULES_HORIZONTAL = new int[]{0, 1, 5, 7, 16, 17, 18, 19};
    private static final int[] RULES_VERTICAL = new int[]{2, 3, 4, 6, 8};
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
        private SparseArray<Node> mKeyNodes;
        private ArrayList<Node> mNodes;
        private ArrayDeque<Node> mRoots;

        static class Node {
            private static final int POOL_LIMIT = 100;
            private static final SynchronizedPool<Node> sPool = new SynchronizedPool(100);
            final SparseArray<Node> dependencies = new SparseArray();
            final ArrayMap<Node, DependencyGraph> dependents = new ArrayMap();
            View view;

            Node() {
            }

            static Node acquire(View view) {
                Node node = (Node) sPool.acquire();
                if (node == null) {
                    node = new Node();
                }
                node.view = view;
                return node;
            }

            void release() {
                this.view = null;
                this.dependents.clear();
                this.dependencies.clear();
                sPool.release(this);
            }
        }

        /* synthetic */ DependencyGraph(DependencyGraph -this0) {
            this();
        }

        private DependencyGraph() {
            this.mNodes = new ArrayList();
            this.mKeyNodes = new SparseArray();
            this.mRoots = new ArrayDeque();
        }

        void clear() {
            ArrayList<Node> nodes = this.mNodes;
            int count = nodes.size();
            for (int i = 0; i < count; i++) {
                ((Node) nodes.get(i)).release();
            }
            nodes.clear();
            this.mKeyNodes.clear();
            this.mRoots.clear();
        }

        void add(View view) {
            int id = view.getId();
            Node node = Node.acquire(view);
            if (id != -1) {
                this.mKeyNodes.put(id, node);
            }
            this.mNodes.add(node);
        }

        void getSortedViews(View[] sorted, int... rules) {
            ArrayDeque<Node> roots = findRoots(rules);
            int index = 0;
            while (true) {
                Node node = (Node) roots.pollLast();
                if (node == null) {
                    break;
                }
                View view = node.view;
                int key = view.getId();
                int index2 = index + 1;
                sorted[index] = view;
                ArrayMap<Node, DependencyGraph> dependents = node.dependents;
                int count = dependents.size();
                for (int i = 0; i < count; i++) {
                    Node dependent = (Node) dependents.keyAt(i);
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
            int i;
            Node node;
            SparseArray<Node> keyNodes = this.mKeyNodes;
            ArrayList<Node> nodes = this.mNodes;
            int count = nodes.size();
            for (i = 0; i < count; i++) {
                node = (Node) nodes.get(i);
                node.dependents.clear();
                node.dependencies.clear();
            }
            for (i = 0; i < count; i++) {
                node = (Node) nodes.get(i);
                int[] rules = ((LayoutParams) node.view.getLayoutParams()).mRules;
                for (int i2 : rulesFilter) {
                    int rule = rules[i2];
                    if (rule > 0) {
                        Node dependency = (Node) keyNodes.get(rule);
                        if (!(dependency == null || dependency == node)) {
                            dependency.dependents.put(node, this);
                            node.dependencies.put(rule, dependency);
                        }
                    }
                }
            }
            ArrayDeque<Node> roots = this.mRoots;
            roots.clear();
            for (i = 0; i < count; i++) {
                node = (Node) nodes.get(i);
                if (node.dependencies.size() == 0) {
                    roots.addLast(node);
                }
            }
            return roots;
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        @ExportedProperty(category = "layout")
        public boolean alignWithParent;
        private int mBottom;
        private int[] mInitialRules;
        private boolean mIsRtlCompatibilityMode;
        private int mLeft;
        private boolean mNeedsLayoutResolution;
        private int mRight;
        @ExportedProperty(category = "layout", indexMapping = {@IntToString(from = 2, to = "above"), @IntToString(from = 4, to = "alignBaseline"), @IntToString(from = 8, to = "alignBottom"), @IntToString(from = 5, to = "alignLeft"), @IntToString(from = 12, to = "alignParentBottom"), @IntToString(from = 9, to = "alignParentLeft"), @IntToString(from = 11, to = "alignParentRight"), @IntToString(from = 10, to = "alignParentTop"), @IntToString(from = 7, to = "alignRight"), @IntToString(from = 6, to = "alignTop"), @IntToString(from = 3, to = "below"), @IntToString(from = 14, to = "centerHorizontal"), @IntToString(from = 13, to = "center"), @IntToString(from = 15, to = "centerVertical"), @IntToString(from = 0, to = "leftOf"), @IntToString(from = 1, to = "rightOf"), @IntToString(from = 18, to = "alignStart"), @IntToString(from = 19, to = "alignEnd"), @IntToString(from = 20, to = "alignParentStart"), @IntToString(from = 21, to = "alignParentEnd"), @IntToString(from = 16, to = "startOf"), @IntToString(from = 17, to = "endOf")}, mapping = {@IntToString(from = -1, to = "true"), @IntToString(from = 0, to = "false/NO_ID")}, resolveId = true)
        private int[] mRules;
        private boolean mRulesChanged;
        private int mTop;

        public LayoutParams(Context c, AttributeSet attrs) {
            boolean hasRtlSupport;
            super(c, attrs);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mRulesChanged = false;
            this.mIsRtlCompatibilityMode = false;
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.RelativeLayout_Layout);
            if (c.getApplicationInfo().targetSdkVersion >= 17) {
                hasRtlSupport = c.getApplicationInfo().hasRtlSupport() ^ 1;
            } else {
                hasRtlSupport = true;
            }
            this.mIsRtlCompatibilityMode = hasRtlSupport;
            int[] rules = this.mRules;
            int[] initialRules = this.mInitialRules;
            int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
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
                        rules[9] = a.getBoolean(attr, false) ? -1 : 0;
                        break;
                    case 10:
                        rules[10] = a.getBoolean(attr, false) ? -1 : 0;
                        break;
                    case 11:
                        rules[11] = a.getBoolean(attr, false) ? -1 : 0;
                        break;
                    case 12:
                        rules[12] = a.getBoolean(attr, false) ? -1 : 0;
                        break;
                    case 13:
                        rules[13] = a.getBoolean(attr, false) ? -1 : 0;
                        break;
                    case 14:
                        rules[14] = a.getBoolean(attr, false) ? -1 : 0;
                        break;
                    case 15:
                        rules[15] = a.getBoolean(attr, false) ? -1 : 0;
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
                        rules[20] = a.getBoolean(attr, false) ? -1 : 0;
                        break;
                    case 22:
                        rules[21] = a.getBoolean(attr, false) ? -1 : 0;
                        break;
                    default:
                        break;
                }
            }
            this.mRulesChanged = true;
            System.arraycopy(rules, 0, initialRules, 0, 22);
            a.recycle();
        }

        public LayoutParams(int w, int h) {
            super(w, h);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mRulesChanged = false;
            this.mIsRtlCompatibilityMode = false;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mRulesChanged = false;
            this.mIsRtlCompatibilityMode = false;
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mRulesChanged = false;
            this.mIsRtlCompatibilityMode = false;
        }

        public LayoutParams(LayoutParams source) {
            super((MarginLayoutParams) source);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mRulesChanged = false;
            this.mIsRtlCompatibilityMode = false;
            this.mIsRtlCompatibilityMode = source.mIsRtlCompatibilityMode;
            this.mRulesChanged = source.mRulesChanged;
            this.alignWithParent = source.alignWithParent;
            System.arraycopy(source.mRules, 0, this.mRules, 0, 22);
            System.arraycopy(source.mInitialRules, 0, this.mInitialRules, 0, 22);
        }

        public String debug(String output) {
            return output + "ViewGroup.LayoutParams={ width=" + android.view.ViewGroup.LayoutParams.sizeToString(this.width) + ", height=" + android.view.ViewGroup.LayoutParams.sizeToString(this.height) + " }";
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
            if (this.mInitialRules[16] == 0 && this.mInitialRules[17] == 0 && this.mInitialRules[18] == 0 && this.mInitialRules[19] == 0 && this.mInitialRules[20] == 0 && this.mInitialRules[21] == 0) {
                return false;
            }
            return true;
        }

        private boolean isRelativeRule(int rule) {
            if (rule == 16 || rule == 17 || rule == 18 || rule == 19 || rule == 20 || rule == 21) {
                return true;
            }
            return false;
        }

        private void resolveRules(int layoutDirection) {
            int i = 1;
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
                    this.mRules[isLayoutRtl ? 7 : 5] = this.mRules[18];
                    this.mRules[18] = 0;
                }
                if (this.mRules[19] != 0) {
                    this.mRules[isLayoutRtl ? 5 : 7] = this.mRules[19];
                    this.mRules[19] = 0;
                }
                if (!((this.mRules[16] == 0 && this.mRules[17] == 0) || (this.mRules[0] == 0 && this.mRules[1] == 0))) {
                    this.mRules[0] = 0;
                    this.mRules[1] = 0;
                }
                if (this.mRules[16] != 0) {
                    this.mRules[isLayoutRtl ? 1 : 0] = this.mRules[16];
                    this.mRules[16] = 0;
                }
                if (this.mRules[17] != 0) {
                    int[] iArr = this.mRules;
                    if (isLayoutRtl) {
                        i = 0;
                    }
                    iArr[i] = this.mRules[17];
                    this.mRules[17] = 0;
                }
                if (!((this.mRules[20] == 0 && this.mRules[21] == 0) || (this.mRules[9] == 0 && this.mRules[11] == 0))) {
                    this.mRules[9] = 0;
                    this.mRules[11] = 0;
                }
                if (this.mRules[20] != 0) {
                    this.mRules[isLayoutRtl ? 11 : 9] = this.mRules[20];
                    this.mRules[20] = 0;
                }
                if (this.mRules[21] != 0) {
                    this.mRules[isLayoutRtl ? 9 : 11] = this.mRules[21];
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
            if (!this.mNeedsLayoutResolution && !hasRelativeRules()) {
                return false;
            }
            if (this.mRulesChanged || layoutDirection != getLayoutDirection()) {
                return true;
            }
            return false;
        }

        protected void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("layout:alignWithParent", this.alignWithParent);
        }
    }

    private class TopToBottomLeftToRightComparator implements Comparator<View> {
        /* synthetic */ TopToBottomLeftToRightComparator(RelativeLayout this$0, TopToBottomLeftToRightComparator -this1) {
            this();
        }

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
        boolean z;
        boolean z2 = true;
        int version = context.getApplicationInfo().targetSdkVersion;
        if (version <= 17) {
            z = true;
        } else {
            z = false;
        }
        this.mAllowBrokenMeasureSpecs = z;
        if (version < 18) {
            z2 = false;
        }
        this.mMeasureVerticalWithPaddingMargin = z2;
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
            if ((Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK & gravity) == 0) {
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
        if ((this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) != gravity) {
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

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LayoutParams params;
        int[] rules;
        if (this.mDirtyHierarchy) {
            this.mDirtyHierarchy = false;
            sortChildren();
        }
        int myWidth = -1;
        int myHeight = -1;
        int width = 0;
        int height = 0;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode != 0) {
            myWidth = widthSize;
        }
        if (heightMode != 0) {
            myHeight = heightSize;
        }
        if (widthMode == 1073741824) {
            width = myWidth;
        }
        if (heightMode == 1073741824) {
            height = myHeight;
        }
        View ignore = null;
        int gravity = this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        boolean horizontalGravity = (gravity == 8388611 || gravity == 0) ? false : true;
        gravity = this.mGravity & 112;
        boolean verticalGravity = (gravity == 48 || gravity == 0) ? false : true;
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
        boolean offsetHorizontalAxis = false;
        boolean offsetVerticalAxis = false;
        if ((horizontalGravity || verticalGravity) && this.mIgnoreGravity != -1) {
            ignore = findViewById(this.mIgnoreGravity);
        }
        boolean isWrapContentWidth = widthMode != 1073741824;
        boolean isWrapContentHeight = heightMode != 1073741824;
        int layoutDirection = getLayoutDirection();
        if (isLayoutRtl() && myWidth == -1) {
            myWidth = 65536;
        }
        for (View child : this.mSortedHorizontalChildren) {
            if (child.getVisibility() != 8) {
                params = (LayoutParams) child.getLayoutParams();
                applyHorizontalSizeRules(params, myWidth, params.getRules(layoutDirection));
                measureChildHorizontal(child, params, myWidth, myHeight);
                if (positionChildHorizontal(child, params, myWidth, isWrapContentWidth)) {
                    offsetHorizontalAxis = true;
                }
            }
        }
        View[] views = this.mSortedVerticalChildren;
        int targetSdkVersion = getContext().getApplicationInfo().targetSdkVersion;
        for (View child2 : views) {
            if (child2.getVisibility() != 8) {
                params = (LayoutParams) child2.getLayoutParams();
                applyVerticalSizeRules(params, myHeight, child2.getBaseline());
                measureChild(child2, params, myWidth, myHeight);
                if (positionChildVertical(child2, params, myHeight, isWrapContentHeight)) {
                    offsetVerticalAxis = true;
                }
                if (isWrapContentWidth) {
                    if (isLayoutRtl()) {
                        if (targetSdkVersion < 19) {
                            width = Math.max(width, myWidth - params.mLeft);
                        } else {
                            width = Math.max(width, (myWidth - params.mLeft) - params.leftMargin);
                        }
                    } else if (targetSdkVersion < 19) {
                        width = Math.max(width, params.mRight);
                    } else {
                        width = Math.max(width, params.mRight + params.rightMargin);
                    }
                }
                if (isWrapContentHeight) {
                    if (targetSdkVersion < 19) {
                        height = Math.max(height, params.mBottom);
                    } else {
                        height = Math.max(height, params.mBottom + params.bottomMargin);
                    }
                }
                if (child2 != ignore || verticalGravity) {
                    left = Math.min(left, params.mLeft - params.leftMargin);
                    top = Math.min(top, params.mTop - params.topMargin);
                }
                if (child2 != ignore || horizontalGravity) {
                    right = Math.max(right, params.mRight + params.rightMargin);
                    bottom = Math.max(bottom, params.mBottom + params.bottomMargin);
                }
            }
        }
        View baselineView = null;
        LayoutParams baselineParams = null;
        for (View child22 : views) {
            if (child22.getVisibility() != 8) {
                LayoutParams childParams = (LayoutParams) child22.getLayoutParams();
                if (baselineView == null || baselineParams == null || compareLayoutPosition(childParams, baselineParams) < 0) {
                    baselineView = child22;
                    baselineParams = childParams;
                }
            }
        }
        this.mBaselineView = baselineView;
        if (isWrapContentWidth) {
            width += this.mPaddingRight;
            if (this.mLayoutParams != null && this.mLayoutParams.width >= 0) {
                width = Math.max(width, this.mLayoutParams.width);
            }
            width = View.resolveSize(Math.max(width, getSuggestedMinimumWidth()), widthMeasureSpec);
            if (offsetHorizontalAxis) {
                for (View child222 : views) {
                    if (child222.getVisibility() != 8) {
                        params = (LayoutParams) child222.getLayoutParams();
                        rules = params.getRules(layoutDirection);
                        if (rules[13] != 0 || rules[14] != 0) {
                            centerHorizontal(child222, params, width);
                        } else if (rules[11] != 0) {
                            int childWidth = child222.getMeasuredWidth();
                            params.mLeft = (width - this.mPaddingRight) - childWidth;
                            params.mRight = params.mLeft + childWidth;
                        }
                    }
                }
            }
        }
        if (isWrapContentHeight) {
            height += this.mPaddingBottom;
            if (this.mLayoutParams != null && this.mLayoutParams.height >= 0) {
                height = Math.max(height, this.mLayoutParams.height);
            }
            height = View.resolveSize(Math.max(height, getSuggestedMinimumHeight()), heightMeasureSpec);
            if (offsetVerticalAxis) {
                for (View child2222 : views) {
                    if (child2222.getVisibility() != 8) {
                        params = (LayoutParams) child2222.getLayoutParams();
                        rules = params.getRules(layoutDirection);
                        if (rules[13] != 0 || rules[15] != 0) {
                            centerVertical(child2222, params, height);
                        } else if (rules[12] != 0) {
                            int childHeight = child2222.getMeasuredHeight();
                            params.mTop = (height - this.mPaddingBottom) - childHeight;
                            params.mBottom = params.mTop + childHeight;
                        }
                    }
                }
            }
        }
        if (horizontalGravity || verticalGravity) {
            Rect selfBounds = this.mSelfBounds;
            selfBounds.set(this.mPaddingLeft, this.mPaddingTop, width - this.mPaddingRight, height - this.mPaddingBottom);
            Rect contentBounds = this.mContentBounds;
            Gravity.apply(this.mGravity, right - left, bottom - top, selfBounds, contentBounds, layoutDirection);
            int horizontalOffset = contentBounds.left - left;
            int verticalOffset = contentBounds.top - top;
            if (!(horizontalOffset == 0 && verticalOffset == 0)) {
                for (View child22222 : views) {
                    if (!(child22222.getVisibility() == 8 || child22222 == ignore)) {
                        params = (LayoutParams) child22222.getLayoutParams();
                        if (horizontalGravity) {
                            params.mLeft = params.mLeft + horizontalOffset;
                            params.mRight = params.mRight + horizontalOffset;
                        }
                        if (verticalGravity) {
                            params.mTop = params.mTop + verticalOffset;
                            params.mBottom = params.mBottom + verticalOffset;
                        }
                    }
                }
            }
        }
        if (isLayoutRtl()) {
            int offsetWidth = myWidth - width;
            for (View child222222 : views) {
                if (child222222.getVisibility() != 8) {
                    params = (LayoutParams) child222222.getLayoutParams();
                    params.mLeft = params.mLeft - offsetWidth;
                    params.mRight = params.mRight - offsetWidth;
                }
            }
        }
        -wrap6(width, height);
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
        int childHeightMeasureSpec;
        int childWidthMeasureSpec = getChildMeasureSpec(params.mLeft, params.mRight, params.width, params.leftMargin, params.rightMargin, this.mPaddingLeft, this.mPaddingRight, myWidth);
        if (myHeight >= 0 || (this.mAllowBrokenMeasureSpecs ^ 1) == 0) {
            int maxHeight;
            int heightMode;
            if (this.mMeasureVerticalWithPaddingMargin) {
                maxHeight = Math.max(0, (((myHeight - this.mPaddingTop) - this.mPaddingBottom) - params.topMargin) - params.bottomMargin);
            } else {
                maxHeight = Math.max(0, myHeight);
            }
            if (params.height == -1) {
                heightMode = 1073741824;
            } else {
                heightMode = Integer.MIN_VALUE;
            }
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, heightMode);
        } else if (params.height >= 0) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(params.height, 1073741824);
        } else {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
        }
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    private int getChildMeasureSpec(int childStart, int childEnd, int childSize, int startMargin, int endMargin, int startPadding, int endPadding, int mySize) {
        int childSpecMode = 0;
        int childSpecSize = 0;
        boolean isUnspecified = mySize < 0;
        if (!isUnspecified || (this.mAllowBrokenMeasureSpecs ^ 1) == 0) {
            int tempStart = childStart;
            int tempEnd = childEnd;
            if (childStart == Integer.MIN_VALUE) {
                tempStart = startPadding + startMargin;
            }
            if (childEnd == Integer.MIN_VALUE) {
                tempEnd = (mySize - endPadding) - endMargin;
            }
            int maxAvailable = tempEnd - tempStart;
            if (childStart != Integer.MIN_VALUE && childEnd != Integer.MIN_VALUE) {
                childSpecMode = isUnspecified ? 0 : 1073741824;
                childSpecSize = Math.max(0, maxAvailable);
            } else if (childSize >= 0) {
                childSpecMode = 1073741824;
                if (maxAvailable >= 0) {
                    childSpecSize = Math.min(maxAvailable, childSize);
                } else {
                    childSpecSize = childSize;
                }
            } else if (childSize == -1) {
                childSpecMode = isUnspecified ? 0 : 1073741824;
                childSpecSize = Math.max(0, maxAvailable);
            } else if (childSize == -2) {
                if (maxAvailable >= 0) {
                    childSpecMode = Integer.MIN_VALUE;
                    childSpecSize = maxAvailable;
                } else {
                    childSpecMode = 0;
                    childSpecSize = 0;
                }
            }
            return MeasureSpec.makeMeasureSpec(childSpecSize, childSpecMode);
        }
        if (childStart != Integer.MIN_VALUE && childEnd != Integer.MIN_VALUE) {
            childSpecSize = Math.max(0, childEnd - childStart);
            childSpecMode = 1073741824;
        } else if (childSize >= 0) {
            childSpecSize = childSize;
            childSpecMode = 1073741824;
        } else {
            childSpecSize = 0;
            childSpecMode = 0;
        }
        return MeasureSpec.makeMeasureSpec(childSpecSize, childSpecMode);
    }

    private boolean positionChildHorizontal(View child, LayoutParams params, int myWidth, boolean wrapContent) {
        boolean z = true;
        int[] rules = params.getRules(getLayoutDirection());
        if (params.mLeft == Integer.MIN_VALUE && params.mRight != Integer.MIN_VALUE) {
            params.mLeft = params.mRight - child.getMeasuredWidth();
        } else if (params.mLeft != Integer.MIN_VALUE && params.mRight == Integer.MIN_VALUE) {
            params.mRight = params.mLeft + child.getMeasuredWidth();
        } else if (params.mLeft == Integer.MIN_VALUE && params.mRight == Integer.MIN_VALUE) {
            if (rules[13] != 0 || rules[14] != 0) {
                if (wrapContent) {
                    params.mLeft = this.mPaddingLeft + params.leftMargin;
                    params.mRight = params.mLeft + child.getMeasuredWidth();
                } else {
                    centerHorizontal(child, params, myWidth);
                }
                return true;
            } else if (isLayoutRtl()) {
                params.mRight = (myWidth - this.mPaddingRight) - params.rightMargin;
                params.mLeft = params.mRight - child.getMeasuredWidth();
            } else {
                params.mLeft = this.mPaddingLeft + params.leftMargin;
                params.mRight = params.mLeft + child.getMeasuredWidth();
            }
        }
        if (rules[21] == 0) {
            z = false;
        }
        return z;
    }

    private boolean positionChildVertical(View child, LayoutParams params, int myHeight, boolean wrapContent) {
        boolean z = true;
        int[] rules = params.getRules();
        if (params.mTop == Integer.MIN_VALUE && params.mBottom != Integer.MIN_VALUE) {
            params.mTop = params.mBottom - child.getMeasuredHeight();
        } else if (params.mTop != Integer.MIN_VALUE && params.mBottom == Integer.MIN_VALUE) {
            params.mBottom = params.mTop + child.getMeasuredHeight();
        } else if (params.mTop == Integer.MIN_VALUE && params.mBottom == Integer.MIN_VALUE) {
            if (rules[13] == 0 && rules[15] == 0) {
                params.mTop = this.mPaddingTop + params.topMargin;
                params.mBottom = params.mTop + child.getMeasuredHeight();
            } else {
                if (wrapContent) {
                    params.mTop = this.mPaddingTop + params.topMargin;
                    params.mBottom = params.mTop + child.getMeasuredHeight();
                } else {
                    centerVertical(child, params, myHeight);
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
        childParams.mLeft = Integer.MIN_VALUE;
        childParams.mRight = Integer.MIN_VALUE;
        LayoutParams anchorParams = getRelatedViewParams(rules, 0);
        if (anchorParams != null) {
            childParams.mRight = anchorParams.mLeft - (anchorParams.leftMargin + childParams.rightMargin);
        } else if (childParams.alignWithParent && rules[0] != 0 && myWidth >= 0) {
            childParams.mRight = (myWidth - this.mPaddingRight) - childParams.rightMargin;
        }
        anchorParams = getRelatedViewParams(rules, 1);
        if (anchorParams != null) {
            childParams.mLeft = anchorParams.mRight + (anchorParams.rightMargin + childParams.leftMargin);
        } else if (childParams.alignWithParent && rules[1] != 0) {
            childParams.mLeft = this.mPaddingLeft + childParams.leftMargin;
        }
        anchorParams = getRelatedViewParams(rules, 5);
        if (anchorParams != null) {
            childParams.mLeft = anchorParams.mLeft + childParams.leftMargin;
        } else if (childParams.alignWithParent && rules[5] != 0) {
            childParams.mLeft = this.mPaddingLeft + childParams.leftMargin;
        }
        anchorParams = getRelatedViewParams(rules, 7);
        if (anchorParams != null) {
            childParams.mRight = anchorParams.mRight - childParams.rightMargin;
        } else if (childParams.alignWithParent && rules[7] != 0 && myWidth >= 0) {
            childParams.mRight = (myWidth - this.mPaddingRight) - childParams.rightMargin;
        }
        if (rules[9] != 0) {
            childParams.mLeft = this.mPaddingLeft + childParams.leftMargin;
        }
        if (rules[11] != 0 && myWidth >= 0) {
            childParams.mRight = (myWidth - this.mPaddingRight) - childParams.rightMargin;
        }
    }

    private void applyVerticalSizeRules(LayoutParams childParams, int myHeight, int myBaseline) {
        int[] rules = childParams.getRules();
        int baselineOffset = getRelatedViewBaselineOffset(rules);
        if (baselineOffset != -1) {
            if (myBaseline != -1) {
                baselineOffset -= myBaseline;
            }
            childParams.mTop = baselineOffset;
            childParams.mBottom = Integer.MIN_VALUE;
            return;
        }
        childParams.mTop = Integer.MIN_VALUE;
        childParams.mBottom = Integer.MIN_VALUE;
        LayoutParams anchorParams = getRelatedViewParams(rules, 2);
        if (anchorParams != null) {
            childParams.mBottom = anchorParams.mTop - (anchorParams.topMargin + childParams.bottomMargin);
        } else if (childParams.alignWithParent && rules[2] != 0 && myHeight >= 0) {
            childParams.mBottom = (myHeight - this.mPaddingBottom) - childParams.bottomMargin;
        }
        anchorParams = getRelatedViewParams(rules, 3);
        if (anchorParams != null) {
            childParams.mTop = anchorParams.mBottom + (anchorParams.bottomMargin + childParams.topMargin);
        } else if (childParams.alignWithParent && rules[3] != 0) {
            childParams.mTop = this.mPaddingTop + childParams.topMargin;
        }
        anchorParams = getRelatedViewParams(rules, 6);
        if (anchorParams != null) {
            childParams.mTop = anchorParams.mTop + childParams.topMargin;
        } else if (childParams.alignWithParent && rules[6] != 0) {
            childParams.mTop = this.mPaddingTop + childParams.topMargin;
        }
        anchorParams = getRelatedViewParams(rules, 8);
        if (anchorParams != null) {
            childParams.mBottom = anchorParams.mBottom - childParams.bottomMargin;
        } else if (childParams.alignWithParent && rules[8] != 0 && myHeight >= 0) {
            childParams.mBottom = (myHeight - this.mPaddingBottom) - childParams.bottomMargin;
        }
        if (rules[10] != 0) {
            childParams.mTop = this.mPaddingTop + childParams.topMargin;
        }
        if (rules[12] != 0 && myHeight >= 0) {
            childParams.mBottom = (myHeight - this.mPaddingBottom) - childParams.bottomMargin;
        }
    }

    private View getRelatedView(int[] rules, int relation) {
        int id = rules[relation];
        if (id == 0) {
            return null;
        }
        Node node = (Node) this.mGraph.mKeyNodes.get(id);
        if (node == null) {
            return null;
        }
        View v = node.view;
        while (v.getVisibility() == 8) {
            node = (Node) this.mGraph.mKeyNodes.get(((LayoutParams) v.getLayoutParams()).getRules(v.getLayoutDirection())[relation]);
            if (node == null || v == node.view) {
                return null;
            }
            v = node.view;
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
        params.mLeft = left;
        params.mRight = left + childWidth;
    }

    private static void centerVertical(View child, LayoutParams params, int myHeight) {
        int childHeight = child.getMeasuredHeight();
        int top = (myHeight - childHeight) / 2;
        params.mTop = top;
        params.mBottom = top + childHeight;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
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

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams lp) {
        if (sPreserveMarginParamsInLayoutParamConversion) {
            if (lp instanceof LayoutParams) {
                return new LayoutParams((LayoutParams) lp);
            }
            if (lp instanceof MarginLayoutParams) {
                return new LayoutParams((MarginLayoutParams) lp);
            }
        }
        return new LayoutParams(lp);
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        if (this.mTopToBottomLeftToRightSet == null) {
            this.mTopToBottomLeftToRightSet = new TreeSet(new TopToBottomLeftToRightComparator(this, null));
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
