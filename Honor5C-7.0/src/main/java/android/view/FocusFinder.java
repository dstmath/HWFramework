package android.view;

import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.RILConstants;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.pgmng.log.LogPower;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FocusFinder {
    private static final ThreadLocal<FocusFinder> tlFocusFinder = null;
    final Rect mBestCandidateRect;
    final Rect mFocusedRect;
    final Rect mOtherRect;
    final SequentialFocusComparator mSequentialFocusComparator;
    private final ArrayList<View> mTempList;

    private static final class SequentialFocusComparator implements Comparator<View> {
        private final Rect mFirstRect;
        private final SparseArray<View> mFocusables;
        private final ArrayMap<View, View> mHeadsOfChains;
        private final SparseBooleanArray mIsConnectedTo;
        private boolean mIsLayoutRtl;
        private ViewGroup mRoot;
        private final Rect mSecondRect;

        private SequentialFocusComparator() {
            this.mFirstRect = new Rect();
            this.mSecondRect = new Rect();
            this.mFocusables = new SparseArray();
            this.mIsConnectedTo = new SparseBooleanArray();
            this.mHeadsOfChains = new ArrayMap();
        }

        public void recycle() {
            this.mRoot = null;
            this.mFocusables.clear();
            this.mHeadsOfChains.clear();
            this.mIsConnectedTo.clear();
        }

        public void setRoot(ViewGroup root) {
            this.mRoot = root;
        }

        public void setIsLayoutRtl(boolean b) {
            this.mIsLayoutRtl = b;
        }

        public void setFocusables(ArrayList<View> focusables) {
            int i;
            for (i = focusables.size() - 1; i >= 0; i--) {
                View view = (View) focusables.get(i);
                int id = view.getId();
                if (FocusFinder.isValidId(id)) {
                    this.mFocusables.put(id, view);
                }
                int nextId = view.getNextFocusForwardId();
                if (FocusFinder.isValidId(nextId)) {
                    this.mIsConnectedTo.put(nextId, true);
                }
            }
            for (i = focusables.size() - 1; i >= 0; i--) {
                view = (View) focusables.get(i);
                if (FocusFinder.isValidId(view.getNextFocusForwardId()) && !this.mIsConnectedTo.get(view.getId())) {
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
                view = (View) this.mFocusables.get(view.getNextFocusForwardId());
            }
        }

        public int compare(View first, View second) {
            int i = 1;
            int i2 = -1;
            if (first == second) {
                return 0;
            }
            View firstHead = (View) this.mHeadsOfChains.get(first);
            View secondHead = (View) this.mHeadsOfChains.get(second);
            if (firstHead != secondHead || firstHead == null) {
                if (firstHead != null) {
                    first = firstHead;
                }
                if (secondHead != null) {
                    second = secondHead;
                }
                getRect(first, this.mFirstRect);
                getRect(second, this.mSecondRect);
                if (this.mFirstRect.top < this.mSecondRect.top) {
                    return -1;
                }
                if (this.mFirstRect.top > this.mSecondRect.top) {
                    return 1;
                }
                if (this.mFirstRect.left < this.mSecondRect.left) {
                    if (!this.mIsLayoutRtl) {
                        i = -1;
                    }
                    return i;
                } else if (this.mFirstRect.left > this.mSecondRect.left) {
                    if (!this.mIsLayoutRtl) {
                        i2 = 1;
                    }
                    return i2;
                } else if (this.mFirstRect.bottom < this.mSecondRect.bottom) {
                    return -1;
                } else {
                    if (this.mFirstRect.bottom > this.mSecondRect.bottom) {
                        return 1;
                    }
                    if (this.mFirstRect.right < this.mSecondRect.right) {
                        if (!this.mIsLayoutRtl) {
                            i = -1;
                        }
                        return i;
                    } else if (this.mFirstRect.right <= this.mSecondRect.right) {
                        return 0;
                    } else {
                        if (!this.mIsLayoutRtl) {
                            i2 = 1;
                        }
                        return i2;
                    }
                }
            } else if (first == firstHead) {
                return -1;
            } else {
                return (second != firstHead && FocusFinder.isValidId(first.getNextFocusForwardId())) ? -1 : 1;
            }
        }

        private void getRect(View view, Rect rect) {
            view.getDrawingRect(rect);
            this.mRoot.offsetDescendantRectToMyCoords(view, rect);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.FocusFinder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.FocusFinder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.FocusFinder.<clinit>():void");
    }

    public static FocusFinder getInstance() {
        return (FocusFinder) tlFocusFinder.get();
    }

    private FocusFinder() {
        this.mFocusedRect = new Rect();
        this.mOtherRect = new Rect();
        this.mBestCandidateRect = new Rect();
        this.mSequentialFocusComparator = new SequentialFocusComparator();
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
        if (focused != null) {
            next = findNextUserSpecifiedFocus(root, focused, direction);
        }
        if (next != null) {
            return next;
        }
        ArrayList<View> focusables = this.mTempList;
        try {
            focusables.clear();
            root.addFocusables(focusables, direction);
            if (!focusables.isEmpty()) {
                next = findNextFocus(root, focused, focusedRect, direction, focusables);
            }
            focusables.clear();
            return next;
        } catch (Throwable th) {
            focusables.clear();
        }
    }

    private View findNextUserSpecifiedFocus(ViewGroup root, View focused, int direction) {
        View userSetNextFocus = focused.findUserSetNextFocus(root, direction);
        if (userSetNextFocus == null || !userSetNextFocus.isFocusable() || (userSetNextFocus.isInTouchMode() && !userSetNextFocus.isFocusableInTouchMode())) {
            return null;
        }
        return userSetNextFocus;
    }

    private View findNextFocus(ViewGroup root, View focused, Rect focusedRect, int direction, ArrayList<View> focusables) {
        if (focused == null) {
            if (focusedRect == null) {
                focusedRect = this.mFocusedRect;
                switch (direction) {
                    case HwCfgFilePolicy.EMUI /*1*/:
                        if (!root.isLayoutRtl()) {
                            setFocusBottomRight(root, focusedRect);
                            break;
                        }
                        setFocusTopLeft(root, focusedRect);
                        break;
                    case HwCfgFilePolicy.PC /*2*/:
                        if (!root.isLayoutRtl()) {
                            setFocusTopLeft(root, focusedRect);
                            break;
                        }
                        setFocusBottomRight(root, focusedRect);
                        break;
                    case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                    case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                        setFocusBottomRight(root, focusedRect);
                        break;
                    case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                    case LogPower.END_CHG_ROTATION /*130*/:
                        setFocusTopLeft(root, focusedRect);
                        break;
                    default:
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
            case HwCfgFilePolicy.EMUI /*1*/:
            case HwCfgFilePolicy.PC /*2*/:
                return findNextFocusInRelativeDirection(focusables, root, focused, focusedRect, direction);
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
            case LogPower.END_CHG_ROTATION /*130*/:
                return findNextFocusInAbsoluteDirection(focusables, root, focused, focusedRect, direction);
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }
    }

    private View findNextFocusInRelativeDirection(ArrayList<View> focusables, ViewGroup root, View focused, Rect focusedRect, int direction) {
        try {
            this.mSequentialFocusComparator.setRoot(root);
            this.mSequentialFocusComparator.setIsLayoutRtl(root.isLayoutRtl());
            this.mSequentialFocusComparator.setFocusables(focusables);
            Collections.sort(focusables, this.mSequentialFocusComparator);
            int count = focusables.size();
            switch (direction) {
                case HwCfgFilePolicy.EMUI /*1*/:
                    return getPreviousFocusable(focused, focusables, count);
                case HwCfgFilePolicy.PC /*2*/:
                    return getNextFocusable(focused, focusables, count);
                default:
                    return (View) focusables.get(count - 1);
            }
        } finally {
            this.mSequentialFocusComparator.recycle();
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
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                this.mBestCandidateRect.offset(focusedRect.width() + 1, 0);
                break;
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                this.mBestCandidateRect.offset(0, focusedRect.height() + 1);
                break;
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                this.mBestCandidateRect.offset(-(focusedRect.width() + 1), 0);
                break;
            case LogPower.END_CHG_ROTATION /*130*/:
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
        if (beamsOverlap(direction, source, rect2) || !rect1InSrcBeam) {
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
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                if (srcRect.right > destRect.right || srcRect.left >= destRect.right) {
                    if (srcRect.left <= destRect.left) {
                        z = false;
                    }
                    z2 = z;
                }
                return z2;
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                if (srcRect.bottom > destRect.bottom || srcRect.top >= destRect.bottom) {
                    if (srcRect.top <= destRect.top) {
                        z = false;
                    }
                    z2 = z;
                }
                return z2;
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                if (srcRect.left < destRect.left || srcRect.right <= destRect.left) {
                    if (srcRect.right >= destRect.right) {
                        z = false;
                    }
                    z2 = z;
                }
                return z2;
            case LogPower.END_CHG_ROTATION /*130*/:
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
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                if (rect2.bottom < rect1.top || rect2.top > rect1.bottom) {
                    z = false;
                }
                return z;
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
            case LogPower.END_CHG_ROTATION /*130*/:
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
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                if (src.left < dest.right) {
                    z = false;
                }
                return z;
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                if (src.top < dest.bottom) {
                    z = false;
                }
                return z;
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                if (src.right > dest.left) {
                    z = false;
                }
                return z;
            case LogPower.END_CHG_ROTATION /*130*/:
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
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                return source.left - dest.right;
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                return source.top - dest.bottom;
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                return dest.left - source.right;
            case LogPower.END_CHG_ROTATION /*130*/:
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
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                return source.left - dest.left;
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                return source.top - dest.top;
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                return dest.right - source.right;
            case LogPower.END_CHG_ROTATION /*130*/:
                return dest.bottom - source.bottom;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
        }
    }

    static int minorAxisDistance(int direction, Rect source, Rect dest) {
        switch (direction) {
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                return Math.abs((source.top + (source.height() / 2)) - (dest.top + (dest.height() / 2)));
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
            case LogPower.END_CHG_ROTATION /*130*/:
                return Math.abs((source.left + (source.width() / 2)) - (dest.left + (dest.width() / 2)));
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
        }
    }

    public View findNearestTouchable(ViewGroup root, int x, int y, int direction, int[] deltas) {
        ArrayList<View> touchables = root.getTouchables();
        int minDistance = HwBootFail.STAGE_BOOT_SUCCESS;
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
                int distance = HwBootFail.STAGE_BOOT_SUCCESS;
                switch (direction) {
                    case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                        distance = (x - touchableBounds.right) + 1;
                        break;
                    case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                        distance = (y - touchableBounds.bottom) + 1;
                        break;
                    case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                        distance = touchableBounds.left;
                        break;
                    case LogPower.END_CHG_ROTATION /*130*/:
                        distance = touchableBounds.top;
                        break;
                }
                if (distance < edgeSlop && (closest == null || closestBounds.contains(touchableBounds) || (!touchableBounds.contains(closestBounds) && distance < minDistance))) {
                    minDistance = distance;
                    closest = touchable;
                    closestBounds.set(touchableBounds);
                    switch (direction) {
                        case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                            deltas[0] = -distance;
                            break;
                        case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                            deltas[1] = -distance;
                            break;
                        case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                            deltas[0] = distance;
                            break;
                        case LogPower.END_CHG_ROTATION /*130*/:
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
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                if (destRect.left > x || destRect.top > y || y > destRect.bottom) {
                    z = false;
                }
                return z;
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                if (destRect.top > y || destRect.left > x || x > destRect.right) {
                    z = false;
                }
                return z;
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                if (destRect.left < x || destRect.top > y || y > destRect.bottom) {
                    z = false;
                }
                return z;
            case LogPower.END_CHG_ROTATION /*130*/:
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
}
