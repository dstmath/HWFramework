package android.view;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.widget.FrameLayout;
import java.util.ArrayList;

public class GhostView extends View {
    private boolean mBeingMoved;
    private int mReferences;
    private final View mView;

    private GhostView(View view) {
        super(view.getContext());
        this.mView = view;
        ViewGroup parent = (ViewGroup) this.mView.getParent();
        this.mView.setTransitionVisibility(4);
        parent.invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if (canvas instanceof DisplayListCanvas) {
            DisplayListCanvas dlCanvas = (DisplayListCanvas) canvas;
            this.mView.mRecreateDisplayList = true;
            RenderNode renderNode = this.mView.updateDisplayListIfDirty();
            if (renderNode.isValid()) {
                dlCanvas.insertReorderBarrier();
                dlCanvas.drawRenderNode(renderNode);
                dlCanvas.insertInorderBarrier();
            }
        }
    }

    public void setMatrix(Matrix matrix) {
        this.mRenderNode.setAnimationMatrix(matrix);
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (this.mView.mGhostView == this) {
            this.mView.setTransitionVisibility(visibility == 0 ? 4 : 0);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!this.mBeingMoved) {
            this.mView.setTransitionVisibility(0);
            this.mView.mGhostView = null;
            ViewGroup parent = (ViewGroup) this.mView.getParent();
            if (parent != null) {
                parent.invalidate();
            }
        }
    }

    public static void calculateMatrix(View view, ViewGroup host, Matrix matrix) {
        ViewGroup parent = (ViewGroup) view.getParent();
        matrix.reset();
        parent.transformMatrixToGlobal(matrix);
        matrix.preTranslate((float) (-parent.getScrollX()), (float) (-parent.getScrollY()));
        host.transformMatrixToLocal(matrix);
    }

    public static GhostView addGhost(View view, ViewGroup viewGroup, Matrix matrix) {
        if (view.getParent() instanceof ViewGroup) {
            ViewGroupOverlay overlay = viewGroup.getOverlay();
            ViewGroup overlayViewGroup = overlay.mOverlayViewGroup;
            GhostView ghostView = view.mGhostView;
            int previousRefCount = 0;
            if (ghostView != null) {
                View oldParent = (View) ghostView.getParent();
                ViewGroup oldGrandParent = (ViewGroup) oldParent.getParent();
                if (oldGrandParent != overlayViewGroup) {
                    previousRefCount = ghostView.mReferences;
                    oldGrandParent.removeView(oldParent);
                    ghostView = null;
                }
            }
            if (ghostView == null) {
                if (matrix == null) {
                    matrix = new Matrix();
                    calculateMatrix(view, viewGroup, matrix);
                }
                ghostView = new GhostView(view);
                ghostView.setMatrix(matrix);
                FrameLayout parent = new FrameLayout(view.getContext());
                parent.setClipChildren(false);
                copySize(viewGroup, parent);
                copySize(viewGroup, ghostView);
                parent.addView(ghostView);
                ArrayList<View> tempViews = new ArrayList();
                insertIntoOverlay(overlay.mOverlayViewGroup, parent, ghostView, tempViews, moveGhostViewsToTop(overlay.mOverlayViewGroup, tempViews));
                ghostView.mReferences = previousRefCount;
            } else if (matrix != null) {
                ghostView.setMatrix(matrix);
            }
            ghostView.mReferences++;
            return ghostView;
        }
        throw new IllegalArgumentException("Ghosted views must be parented by a ViewGroup");
    }

    public static GhostView addGhost(View view, ViewGroup viewGroup) {
        return addGhost(view, viewGroup, null);
    }

    public static void removeGhost(View view) {
        GhostView ghostView = view.mGhostView;
        if (ghostView != null) {
            ghostView.mReferences--;
            if (ghostView.mReferences == 0) {
                ViewGroup parent = (ViewGroup) ghostView.getParent();
                ((ViewGroup) parent.getParent()).removeView(parent);
            }
        }
    }

    public static GhostView getGhost(View view) {
        return view.mGhostView;
    }

    private static void copySize(View from, View to) {
        to.setLeft(0);
        to.setTop(0);
        to.setRight(from.getWidth());
        to.setBottom(from.getHeight());
    }

    private static int moveGhostViewsToTop(ViewGroup viewGroup, ArrayList<View> tempViews) {
        int numChildren = viewGroup.getChildCount();
        if (numChildren == 0) {
            return -1;
        }
        int firstGhost;
        int i;
        if (isGhostWrapper(viewGroup.getChildAt(numChildren - 1))) {
            firstGhost = numChildren - 1;
            i = numChildren - 2;
            while (i >= 0 && isGhostWrapper(viewGroup.getChildAt(i))) {
                firstGhost = i;
                i--;
            }
            return firstGhost;
        }
        for (i = numChildren - 2; i >= 0; i--) {
            View child = viewGroup.getChildAt(i);
            if (isGhostWrapper(child)) {
                tempViews.add(child);
                GhostView ghostView = (GhostView) ((ViewGroup) child).getChildAt(0);
                ghostView.mBeingMoved = true;
                viewGroup.removeViewAt(i);
                ghostView.mBeingMoved = false;
            }
        }
        if (tempViews.isEmpty()) {
            firstGhost = -1;
        } else {
            firstGhost = viewGroup.getChildCount();
            for (i = tempViews.size() - 1; i >= 0; i--) {
                viewGroup.addView((View) tempViews.get(i));
            }
            tempViews.clear();
        }
        return firstGhost;
    }

    private static void insertIntoOverlay(ViewGroup viewGroup, ViewGroup wrapper, GhostView ghostView, ArrayList<View> tempParents, int firstGhost) {
        if (firstGhost == -1) {
            viewGroup.addView(wrapper);
            return;
        }
        ArrayList<View> viewParents = new ArrayList();
        getParents(ghostView.mView, viewParents);
        int index = getInsertIndex(viewGroup, viewParents, tempParents, firstGhost);
        if (index < 0 || index >= viewGroup.getChildCount()) {
            viewGroup.addView(wrapper);
        } else {
            viewGroup.addView((View) wrapper, index);
        }
    }

    private static int getInsertIndex(ViewGroup overlayViewGroup, ArrayList<View> viewParents, ArrayList<View> tempParents, int firstGhost) {
        int low = firstGhost;
        int high = overlayViewGroup.getChildCount() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            getParents(((GhostView) ((ViewGroup) overlayViewGroup.getChildAt(mid)).getChildAt(0)).mView, tempParents);
            if (isOnTop((ArrayList) viewParents, (ArrayList) tempParents)) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
            tempParents.clear();
        }
        return low;
    }

    private static boolean isGhostWrapper(View view) {
        if (view instanceof FrameLayout) {
            FrameLayout frameLayout = (FrameLayout) view;
            if (frameLayout.getChildCount() == 1) {
                return frameLayout.getChildAt(0) instanceof GhostView;
            }
        }
        return false;
    }

    private static boolean isOnTop(ArrayList<View> viewParents, ArrayList<View> comparedWith) {
        if (viewParents.isEmpty() || comparedWith.isEmpty() || viewParents.get(0) != comparedWith.get(0)) {
            return true;
        }
        int depth = Math.min(viewParents.size(), comparedWith.size());
        for (int i = 1; i < depth; i++) {
            View viewParent = (View) viewParents.get(i);
            View comparedWithParent = (View) comparedWith.get(i);
            if (viewParent != comparedWithParent) {
                return isOnTop(viewParent, comparedWithParent);
            }
        }
        return comparedWith.size() == depth;
    }

    private static void getParents(View view, ArrayList<View> parents) {
        ViewParent parent = view.getParent();
        if (parent != null && (parent instanceof ViewGroup)) {
            getParents((View) parent, parents);
        }
        parents.add(view);
    }

    private static boolean isOnTop(View view, View comparedWith) {
        boolean customOrder;
        ViewGroup parent = (ViewGroup) view.getParent();
        int childrenCount = parent.getChildCount();
        ArrayList<View> preorderedList = parent.buildOrderedChildList();
        if (preorderedList == null) {
            customOrder = parent.isChildrenDrawingOrderEnabled();
        } else {
            customOrder = false;
        }
        boolean isOnTop = true;
        int i = 0;
        while (i < childrenCount) {
            int childIndex = customOrder ? parent.getChildDrawingOrder(childrenCount, i) : i;
            View child = preorderedList == null ? parent.getChildAt(childIndex) : (View) preorderedList.get(childIndex);
            if (child == view) {
                isOnTop = false;
                break;
            } else if (child == comparedWith) {
                isOnTop = true;
                break;
            } else {
                i++;
            }
        }
        if (preorderedList != null) {
            preorderedList.clear();
        }
        return isOnTop;
    }
}
