package android.view;

import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.-$Lambda$6k_RnLLpNi5zg27ubDxN4lDdBbk.AnonymousClass2;
import android.view.-$Lambda$6k_RnLLpNi5zg27ubDxN4lDdBbk.AnonymousClass3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class FocusFinder {
    public static final String TAG = "FocusFinder";
    private static final ThreadLocal<FocusFinder> tlFocusFinder = new ThreadLocal<FocusFinder>() {
        protected FocusFinder initialValue() {
            return new FocusFinder();
        }
    };
    final Rect mBestCandidateRect;
    private final FocusSorter mFocusSorter;
    final Rect mFocusedRect;
    final Rect mOtherRect;
    private final ArrayList<View> mTempList;
    private final UserSpecifiedFocusComparator mUserSpecifiedClusterComparator;
    private final UserSpecifiedFocusComparator mUserSpecifiedFocusComparator;

    static final class FocusSorter {
        private int mLastPoolRect;
        private HashMap<View, Rect> mRectByView = null;
        private ArrayList<Rect> mRectPool = new ArrayList();
        private int mRtlMult;
        private Comparator<View> mSidesComparator = new AnonymousClass3(this);
        private Comparator<View> mTopsComparator = new AnonymousClass2(this);

        FocusSorter() {
        }

        /* synthetic */ int lambda$-android_view_FocusFinder$FocusSorter_31467(View first, View second) {
            if (first == second) {
                return 0;
            }
            Rect firstRect = (Rect) this.mRectByView.get(first);
            Rect secondRect = (Rect) this.mRectByView.get(second);
            int result = firstRect.top - secondRect.top;
            if (result == 0) {
                return firstRect.bottom - secondRect.bottom;
            }
            return result;
        }

        /* synthetic */ int lambda$-android_view_FocusFinder$FocusSorter_31927(View first, View second) {
            if (first == second) {
                return 0;
            }
            Rect firstRect = (Rect) this.mRectByView.get(first);
            Rect secondRect = (Rect) this.mRectByView.get(second);
            int result = firstRect.left - secondRect.left;
            if (result == 0) {
                return firstRect.right - secondRect.right;
            }
            return this.mRtlMult * result;
        }

        public void sort(View[] views, int start, int end, ViewGroup root, boolean isRtl) {
            int count = end - start;
            if (count >= 2) {
                int i;
                if (this.mRectByView == null) {
                    this.mRectByView = new HashMap();
                }
                this.mRtlMult = isRtl ? -1 : 1;
                for (i = this.mRectPool.size(); i < count; i++) {
                    this.mRectPool.add(new Rect());
                }
                for (i = start; i < end; i++) {
                    ArrayList arrayList = this.mRectPool;
                    int i2 = this.mLastPoolRect;
                    this.mLastPoolRect = i2 + 1;
                    Rect next = (Rect) arrayList.get(i2);
                    views[i].getDrawingRect(next);
                    root.offsetDescendantRectToMyCoords(views[i], next);
                    this.mRectByView.put(views[i], next);
                }
                Arrays.sort(views, start, count, this.mTopsComparator);
                int sweepBottom = ((Rect) this.mRectByView.get(views[start])).bottom;
                int rowStart = start;
                int sweepIdx = start + 1;
                while (sweepIdx < end) {
                    Rect currRect = (Rect) this.mRectByView.get(views[sweepIdx]);
                    if (currRect.top >= sweepBottom) {
                        if (sweepIdx - rowStart > 1) {
                            Arrays.sort(views, rowStart, sweepIdx, this.mSidesComparator);
                        }
                        sweepBottom = currRect.bottom;
                        rowStart = sweepIdx;
                    } else {
                        sweepBottom = Math.max(sweepBottom, currRect.bottom);
                    }
                    sweepIdx++;
                }
                if (sweepIdx - rowStart > 1) {
                    Arrays.sort(views, rowStart, sweepIdx, this.mSidesComparator);
                }
                this.mLastPoolRect = 0;
                this.mRectByView.clear();
            }
        }
    }

    private static final class UserSpecifiedFocusComparator implements Comparator<View> {
        private final ArrayMap<View, View> mHeadsOfChains = new ArrayMap();
        private final ArraySet<View> mIsConnectedTo = new ArraySet();
        private final ArrayMap<View, View> mNextFoci = new ArrayMap();
        private final NextFocusGetter mNextFocusGetter;
        private final ArrayMap<View, Integer> mOriginalOrdinal = new ArrayMap();
        private View mRoot;

        public interface NextFocusGetter {
            View get(View view, View view2);
        }

        UserSpecifiedFocusComparator(NextFocusGetter nextFocusGetter) {
            this.mNextFocusGetter = nextFocusGetter;
        }

        public void recycle() {
            this.mRoot = null;
            this.mHeadsOfChains.clear();
            this.mIsConnectedTo.clear();
            this.mOriginalOrdinal.clear();
            this.mNextFoci.clear();
        }

        public void setFocusables(List<View> focusables, View root) {
            int i;
            View view;
            this.mRoot = root;
            for (i = 0; i < focusables.size(); i++) {
                this.mOriginalOrdinal.put((View) focusables.get(i), Integer.valueOf(i));
            }
            for (i = focusables.size() - 1; i >= 0; i--) {
                view = (View) focusables.get(i);
                View next = this.mNextFocusGetter.get(this.mRoot, view);
                if (next != null && this.mOriginalOrdinal.containsKey(next)) {
                    this.mNextFoci.put(view, next);
                    this.mIsConnectedTo.add(next);
                }
            }
            for (i = focusables.size() - 1; i >= 0; i--) {
                view = (View) focusables.get(i);
                if (!(((View) this.mNextFoci.get(view)) == null || (this.mIsConnectedTo.contains(view) ^ 1) == 0)) {
                    setHeadOfChain(view);
                }
            }
        }

        private void setHeadOfChain(View head) {
            View view = head;
            while (view != null) {
                View otherHead = (View) this.mHeadsOfChains.get(view);
                if (otherHead != null) {
                    if (otherHead != head) {
                        view = head;
                        head = otherHead;
                    } else {
                        return;
                    }
                }
                this.mHeadsOfChains.put(view, head);
                view = (View) this.mNextFoci.get(view);
            }
        }

        public int compare(View first, View second) {
            if (first == second) {
                return 0;
            }
            View firstHead = (View) this.mHeadsOfChains.get(first);
            View secondHead = (View) this.mHeadsOfChains.get(second);
            if (firstHead != secondHead || firstHead == null) {
                boolean involvesChain = false;
                if (firstHead != null) {
                    first = firstHead;
                    involvesChain = true;
                }
                if (secondHead != null) {
                    second = secondHead;
                    involvesChain = true;
                }
                if (!involvesChain) {
                    return 0;
                }
                return ((Integer) this.mOriginalOrdinal.get(first)).intValue() < ((Integer) this.mOriginalOrdinal.get(second)).intValue() ? -1 : 1;
            } else if (first == firstHead) {
                return -1;
            } else {
                return (second == firstHead || this.mNextFoci.get(first) == null) ? 1 : -1;
            }
        }
    }

    /* synthetic */ FocusFinder(FocusFinder -this0) {
        this();
    }

    public static FocusFinder getInstance() {
        return (FocusFinder) tlFocusFinder.get();
    }

    static /* synthetic */ View lambda$-android_view_FocusFinder_2148(View r, View v) {
        return isValidId(v.getNextFocusForwardId()) ? v.findUserSetNextFocus(r, 2) : null;
    }

    static /* synthetic */ View lambda$-android_view_FocusFinder_2406(View r, View v) {
        return isValidId(v.getNextClusterForwardId()) ? v.findUserSetNextKeyboardNavigationCluster(r, 2) : null;
    }

    private FocusFinder() {
        this.mFocusedRect = new Rect();
        this.mOtherRect = new Rect();
        this.mBestCandidateRect = new Rect();
        this.mUserSpecifiedFocusComparator = new UserSpecifiedFocusComparator(new -$Lambda$6k_RnLLpNi5zg27ubDxN4lDdBbk());
        this.mUserSpecifiedClusterComparator = new UserSpecifiedFocusComparator(new NextFocusGetter() {
            public final View get(View view, View view2) {
                return $m$0(view, view2);
            }
        });
        this.mFocusSorter = new FocusSorter();
        this.mTempList = new ArrayList();
    }

    public final View findNextFocus(ViewGroup root, View focused, int direction) {
        return findNextFocus(root, focused, null, direction);
    }

    public View findNextFocusFromRect(ViewGroup root, Rect focusedRect, int direction) {
        this.mFocusedRect.set(focusedRect);
        return findNextFocus(root, null, this.mFocusedRect, direction);
    }

    private View findNextFocus(ViewGroup root, View focused, Rect focusedRect, int direction) {
        View next = null;
        ViewGroup effectiveRoot = getEffectiveRoot(root, focused);
        if (focused != null) {
            next = findNextUserSpecifiedFocus(effectiveRoot, focused, direction);
        }
        if (next != null) {
            return next;
        }
        ArrayList<View> focusables = this.mTempList;
        try {
            focusables.clear();
            effectiveRoot.addFocusables(focusables, direction);
            if (!focusables.isEmpty()) {
                next = findNextFocus(effectiveRoot, focused, focusedRect, direction, focusables);
            }
            focusables.clear();
            return next;
        } catch (Throwable th) {
            focusables.clear();
        }
    }

    private ViewGroup getEffectiveRoot(ViewGroup root, View focused) {
        if (focused == null || focused == root) {
            return root;
        }
        ViewGroup effective = null;
        ViewParent nextParent = focused.getParent();
        while (nextParent != root) {
            if (nextParent != null) {
                ViewGroup vg = (ViewGroup) nextParent;
                if (vg.getTouchscreenBlocksFocus() && focused.getContext().getPackageManager().hasSystemFeature("android.hardware.touchscreen") && vg.isKeyboardNavigationCluster()) {
                    effective = vg;
                }
                nextParent = nextParent.getParent();
            } else {
                Log.e(TAG, "error FocusFinder.getEffectiveRoot nextParent is null current thread = " + Thread.currentThread());
            }
            if (!(nextParent instanceof ViewGroup)) {
                return root;
            }
        }
        if (effective == null) {
            effective = root;
        }
        return effective;
    }

    public View findNextKeyboardNavigationCluster(View root, View currentCluster, int direction) {
        View next = null;
        if (currentCluster != null) {
            next = findNextUserSpecifiedKeyboardNavigationCluster(root, currentCluster, direction);
            if (next != null) {
                return next;
            }
        }
        ArrayList<View> clusters = this.mTempList;
        try {
            clusters.clear();
            root.addKeyboardNavigationClusters(clusters, direction);
            if (!clusters.isEmpty()) {
                next = findNextKeyboardNavigationCluster(root, currentCluster, clusters, direction);
            }
            clusters.clear();
            return next;
        } catch (Throwable th) {
            clusters.clear();
        }
    }

    private View findNextUserSpecifiedKeyboardNavigationCluster(View root, View currentCluster, int direction) {
        View userSetNextCluster = currentCluster.findUserSetNextKeyboardNavigationCluster(root, direction);
        if (userSetNextCluster == null || !userSetNextCluster.hasFocusable()) {
            return null;
        }
        return userSetNextCluster;
    }

    private View findNextUserSpecifiedFocus(ViewGroup root, View focused, int direction) {
        View userSetNextFocus = focused.findUserSetNextFocus(root, direction);
        while (userSetNextFocus != null) {
            if (userSetNextFocus.isFocusable() && userSetNextFocus.getVisibility() == 0 && (!userSetNextFocus.isInTouchMode() || userSetNextFocus.isFocusableInTouchMode())) {
                return userSetNextFocus;
            }
            userSetNextFocus = userSetNextFocus.findUserSetNextFocus(root, direction);
        }
        return null;
    }

    private View findNextFocus(ViewGroup root, View focused, Rect focusedRect, int direction, ArrayList<View> focusables) {
        if (focused == null) {
            if (focusedRect == null) {
                focusedRect = this.mFocusedRect;
                switch (direction) {
                    case 1:
                        if (!root.isLayoutRtl()) {
                            setFocusBottomRight(root, focusedRect);
                            break;
                        }
                        setFocusTopLeft(root, focusedRect);
                        break;
                    case 2:
                        if (!root.isLayoutRtl()) {
                            setFocusTopLeft(root, focusedRect);
                            break;
                        }
                        setFocusBottomRight(root, focusedRect);
                        break;
                    case 17:
                    case 33:
                        setFocusBottomRight(root, focusedRect);
                        break;
                    case 66:
                    case 130:
                        setFocusTopLeft(root, focusedRect);
                        break;
                }
            }
        }
        if (focusedRect == null) {
            focusedRect = this.mFocusedRect;
        }
        focused.getFocusedRect(focusedRect);
        root.offsetDescendantRectToMyCoords(focused, focusedRect);
        switch (direction) {
            case 1:
            case 2:
                return findNextFocusInRelativeDirection(focusables, root, focused, focusedRect, direction);
            case 17:
            case 33:
            case 66:
            case 130:
                return findNextFocusInAbsoluteDirection(focusables, root, focused, focusedRect, direction);
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }
    }

    private View findNextKeyboardNavigationCluster(View root, View currentCluster, List<View> clusters, int direction) {
        try {
            this.mUserSpecifiedClusterComparator.setFocusables(clusters, root);
            Collections.sort(clusters, this.mUserSpecifiedClusterComparator);
            int count = clusters.size();
            switch (direction) {
                case 1:
                case 17:
                case 33:
                    return getPreviousKeyboardNavigationCluster(root, currentCluster, clusters, count);
                case 2:
                case 66:
                case 130:
                    return getNextKeyboardNavigationCluster(root, currentCluster, clusters, count);
                default:
                    throw new IllegalArgumentException("Unknown direction: " + direction);
            }
        } finally {
            this.mUserSpecifiedClusterComparator.recycle();
        }
    }

    private View findNextFocusInRelativeDirection(ArrayList<View> focusables, ViewGroup root, View focused, Rect focusedRect, int direction) {
        try {
            this.mUserSpecifiedFocusComparator.setFocusables(focusables, root);
            Collections.sort(focusables, this.mUserSpecifiedFocusComparator);
            int count = focusables.size();
            switch (direction) {
                case 1:
                    return getPreviousFocusable(focused, focusables, count);
                case 2:
                    return getNextFocusable(focused, focusables, count);
                default:
                    return (View) focusables.get(count - 1);
            }
        } finally {
            this.mUserSpecifiedFocusComparator.recycle();
        }
    }

    private void setFocusBottomRight(ViewGroup root, Rect focusedRect) {
        int rootBottom = root.getScrollY() + root.getHeight();
        int rootRight = root.getScrollX() + root.getWidth();
        focusedRect.set(rootRight, rootBottom, rootRight, rootBottom);
    }

    private void setFocusTopLeft(ViewGroup root, Rect focusedRect) {
        int rootTop = root.getScrollY();
        int rootLeft = root.getScrollX();
        focusedRect.set(rootLeft, rootTop, rootLeft, rootTop);
    }

    View findNextFocusInAbsoluteDirection(ArrayList<View> focusables, ViewGroup root, View focused, Rect focusedRect, int direction) {
        this.mBestCandidateRect.set(focusedRect);
        switch (direction) {
            case 17:
                this.mBestCandidateRect.offset(focusedRect.width() + 1, 0);
                break;
            case 33:
                this.mBestCandidateRect.offset(0, focusedRect.height() + 1);
                break;
            case 66:
                this.mBestCandidateRect.offset(-(focusedRect.width() + 1), 0);
                break;
            case 130:
                this.mBestCandidateRect.offset(0, -(focusedRect.height() + 1));
                break;
        }
        View closest = null;
        int numFocusables = focusables.size();
        for (int i = 0; i < numFocusables; i++) {
            View focusable = (View) focusables.get(i);
            if (!(focusable == focused || focusable == root)) {
                focusable.getFocusedRect(this.mOtherRect);
                root.offsetDescendantRectToMyCoords(focusable, this.mOtherRect);
                if (isBetterCandidate(direction, focusedRect, this.mOtherRect, this.mBestCandidateRect)) {
                    this.mBestCandidateRect.set(this.mOtherRect);
                    closest = focusable;
                }
            }
        }
        return closest;
    }

    private static View getNextFocusable(View focused, ArrayList<View> focusables, int count) {
        if (focused != null) {
            int position = focusables.lastIndexOf(focused);
            if (position >= 0 && position + 1 < count) {
                return (View) focusables.get(position + 1);
            }
        }
        if (focusables.isEmpty()) {
            return null;
        }
        return (View) focusables.get(0);
    }

    private static View getPreviousFocusable(View focused, ArrayList<View> focusables, int count) {
        if (focused != null) {
            int position = focusables.indexOf(focused);
            if (position > 0) {
                return (View) focusables.get(position - 1);
            }
        }
        if (focusables.isEmpty()) {
            return null;
        }
        return (View) focusables.get(count - 1);
    }

    private static View getNextKeyboardNavigationCluster(View root, View currentCluster, List<View> clusters, int count) {
        if (currentCluster == null) {
            return (View) clusters.get(0);
        }
        int position = clusters.lastIndexOf(currentCluster);
        if (position < 0 || position + 1 >= count) {
            return root;
        }
        return (View) clusters.get(position + 1);
    }

    private static View getPreviousKeyboardNavigationCluster(View root, View currentCluster, List<View> clusters, int count) {
        if (currentCluster == null) {
            return (View) clusters.get(count - 1);
        }
        int position = clusters.indexOf(currentCluster);
        if (position > 0) {
            return (View) clusters.get(position - 1);
        }
        return root;
    }

    boolean isBetterCandidate(int direction, Rect source, Rect rect1, Rect rect2) {
        boolean z = true;
        if (!isCandidate(source, rect1, direction)) {
            return false;
        }
        if (!isCandidate(source, rect2, direction) || beamBeats(direction, source, rect1, rect2)) {
            return true;
        }
        if (beamBeats(direction, source, rect2, rect1)) {
            return false;
        }
        if (getWeightedDistanceFor(majorAxisDistance(direction, source, rect1), minorAxisDistance(direction, source, rect1)) >= getWeightedDistanceFor(majorAxisDistance(direction, source, rect2), minorAxisDistance(direction, source, rect2))) {
            z = false;
        }
        return z;
    }

    boolean beamBeats(int direction, Rect source, Rect rect1, Rect rect2) {
        boolean z = true;
        boolean rect1InSrcBeam = beamsOverlap(direction, source, rect1);
        if (beamsOverlap(direction, source, rect2) || (rect1InSrcBeam ^ 1) != 0) {
            return false;
        }
        if (!isToDirectionOf(direction, source, rect2) || direction == 17 || direction == 66) {
            return true;
        }
        if (majorAxisDistance(direction, source, rect1) >= majorAxisDistanceToFarEdge(direction, source, rect2)) {
            z = false;
        }
        return z;
    }

    int getWeightedDistanceFor(int majorAxisDistance, int minorAxisDistance) {
        return ((majorAxisDistance * 13) * majorAxisDistance) + (minorAxisDistance * minorAxisDistance);
    }

    boolean isCandidate(Rect srcRect, Rect destRect, int direction) {
        boolean z = true;
        boolean z2 = false;
        switch (direction) {
            case 17:
                if (srcRect.right > destRect.right || srcRect.left >= destRect.right) {
                    if (srcRect.left <= destRect.left) {
                        z = false;
                    }
                    z2 = z;
                }
                return z2;
            case 33:
                if (srcRect.bottom > destRect.bottom || srcRect.top >= destRect.bottom) {
                    if (srcRect.top <= destRect.top) {
                        z = false;
                    }
                    z2 = z;
                }
                return z2;
            case 66:
                if (srcRect.left < destRect.left || srcRect.right <= destRect.left) {
                    if (srcRect.right >= destRect.right) {
                        z = false;
                    }
                    z2 = z;
                }
                return z2;
            case 130:
                if (srcRect.top < destRect.top || srcRect.bottom <= destRect.top) {
                    if (srcRect.bottom >= destRect.bottom) {
                        z = false;
                    }
                    z2 = z;
                }
                return z2;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
        }
    }

    boolean beamsOverlap(int direction, Rect rect1, Rect rect2) {
        boolean z = true;
        boolean z2 = false;
        switch (direction) {
            case 17:
            case 66:
                if (rect2.bottom < rect1.top || rect2.top > rect1.bottom) {
                    z = false;
                }
                return z;
            case 33:
            case 130:
                if (rect2.right >= rect1.left && rect2.left <= rect1.right) {
                    z2 = true;
                }
                return z2;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
        }
    }

    boolean isToDirectionOf(int direction, Rect src, Rect dest) {
        boolean z = true;
        switch (direction) {
            case 17:
                if (src.left < dest.right) {
                    z = false;
                }
                return z;
            case 33:
                if (src.top < dest.bottom) {
                    z = false;
                }
                return z;
            case 66:
                if (src.right > dest.left) {
                    z = false;
                }
                return z;
            case 130:
                if (src.bottom > dest.top) {
                    z = false;
                }
                return z;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
        }
    }

    static int majorAxisDistance(int direction, Rect source, Rect dest) {
        return Math.max(0, majorAxisDistanceRaw(direction, source, dest));
    }

    static int majorAxisDistanceRaw(int direction, Rect source, Rect dest) {
        switch (direction) {
            case 17:
                return source.left - dest.right;
            case 33:
                return source.top - dest.bottom;
            case 66:
                return dest.left - source.right;
            case 130:
                return dest.top - source.bottom;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
        }
    }

    static int majorAxisDistanceToFarEdge(int direction, Rect source, Rect dest) {
        return Math.max(1, majorAxisDistanceToFarEdgeRaw(direction, source, dest));
    }

    static int majorAxisDistanceToFarEdgeRaw(int direction, Rect source, Rect dest) {
        switch (direction) {
            case 17:
                return source.left - dest.left;
            case 33:
                return source.top - dest.top;
            case 66:
                return dest.right - source.right;
            case 130:
                return dest.bottom - source.bottom;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
        }
    }

    static int minorAxisDistance(int direction, Rect source, Rect dest) {
        switch (direction) {
            case 17:
            case 66:
                return Math.abs((source.top + (source.height() / 2)) - (dest.top + (dest.height() / 2)));
            case 33:
            case 130:
                return Math.abs((source.left + (source.width() / 2)) - (dest.left + (dest.width() / 2)));
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
        }
    }

    public View findNearestTouchable(ViewGroup root, int x, int y, int direction, int[] deltas) {
        ArrayList<View> touchables = root.getTouchables();
        int minDistance = Integer.MAX_VALUE;
        View closest = null;
        int numTouchables = touchables.size();
        int edgeSlop = ViewConfiguration.get(root.mContext).getScaledEdgeSlop();
        Rect closestBounds = new Rect();
        Rect touchableBounds = this.mOtherRect;
        for (int i = 0; i < numTouchables; i++) {
            View touchable = (View) touchables.get(i);
            touchable.getDrawingRect(touchableBounds);
            root.offsetRectBetweenParentAndChild(touchable, touchableBounds, true, true);
            if (isTouchCandidate(x, y, touchableBounds, direction)) {
                int distance = Integer.MAX_VALUE;
                switch (direction) {
                    case 17:
                        distance = (x - touchableBounds.right) + 1;
                        break;
                    case 33:
                        distance = (y - touchableBounds.bottom) + 1;
                        break;
                    case 66:
                        distance = touchableBounds.left;
                        break;
                    case 130:
                        distance = touchableBounds.top;
                        break;
                }
                if (distance < edgeSlop && (closest == null || closestBounds.contains(touchableBounds) || (!touchableBounds.contains(closestBounds) && distance < minDistance))) {
                    minDistance = distance;
                    closest = touchable;
                    closestBounds.set(touchableBounds);
                    switch (direction) {
                        case 17:
                            deltas[0] = -distance;
                            break;
                        case 33:
                            deltas[1] = -distance;
                            break;
                        case 66:
                            deltas[0] = distance;
                            break;
                        case 130:
                            deltas[1] = distance;
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return closest;
    }

    private boolean isTouchCandidate(int x, int y, Rect destRect, int direction) {
        boolean z = true;
        switch (direction) {
            case 17:
                if (destRect.left > x || destRect.top > y || y > destRect.bottom) {
                    z = false;
                }
                return z;
            case 33:
                if (destRect.top > y || destRect.left > x || x > destRect.right) {
                    z = false;
                }
                return z;
            case 66:
                if (destRect.left < x || destRect.top > y || y > destRect.bottom) {
                    z = false;
                }
                return z;
            case 130:
                if (destRect.top < y || destRect.left > x || x > destRect.right) {
                    z = false;
                }
                return z;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
        }
    }

    private static final boolean isValidId(int id) {
        return (id == 0 || id == -1) ? false : true;
    }

    public static void sort(View[] views, int start, int end, ViewGroup root, boolean isRtl) {
        getInstance().mFocusSorter.sort(views, start, end, root, isRtl);
    }
}
