package android.app;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionSet;
import android.transition.Visibility;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.view.GhostView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import com.android.internal.view.OneShotPreDrawListener;
import java.util.ArrayList;
import java.util.Collection;

/* access modifiers changed from: package-private */
public abstract class ActivityTransitionCoordinator extends ResultReceiver {
    protected static final String KEY_ELEVATION = "shared_element:elevation";
    protected static final String KEY_IMAGE_MATRIX = "shared_element:imageMatrix";
    static final String KEY_REMOTE_RECEIVER = "android:remoteReceiver";
    protected static final String KEY_SCALE_TYPE = "shared_element:scaleType";
    protected static final String KEY_SCREEN_BOTTOM = "shared_element:screenBottom";
    protected static final String KEY_SCREEN_LEFT = "shared_element:screenLeft";
    protected static final String KEY_SCREEN_RIGHT = "shared_element:screenRight";
    protected static final String KEY_SCREEN_TOP = "shared_element:screenTop";
    protected static final String KEY_SNAPSHOT = "shared_element:bitmap";
    protected static final String KEY_TRANSLATION_Z = "shared_element:translationZ";
    public static final int MSG_ALLOW_RETURN_TRANSITION = 108;
    public static final int MSG_CANCEL = 106;
    public static final int MSG_EXIT_TRANSITION_COMPLETE = 104;
    public static final int MSG_HIDE_SHARED_ELEMENTS = 101;
    public static final int MSG_SET_REMOTE_RECEIVER = 100;
    public static final int MSG_SHARED_ELEMENT_DESTINATION = 107;
    public static final int MSG_START_EXIT_TRANSITION = 105;
    public static final int MSG_TAKE_SHARED_ELEMENTS = 103;
    protected static final ImageView.ScaleType[] SCALE_TYPE_VALUES = ImageView.ScaleType.values();
    private static final String TAG = "ActivityTransitionCoordinator";
    protected final ArrayList<String> mAllSharedElementNames;
    private boolean mBackgroundAnimatorComplete;
    private final FixedEpicenterCallback mEpicenterCallback = new FixedEpicenterCallback();
    private ArrayList<GhostViewListeners> mGhostViewListeners = new ArrayList<>();
    protected final boolean mIsReturning;
    private boolean mIsStartingTransition;
    protected SharedElementCallback mListener;
    private ArrayMap<View, Float> mOriginalAlphas = new ArrayMap<>();
    private Runnable mPendingTransition;
    protected ResultReceiver mResultReceiver;
    protected final ArrayList<String> mSharedElementNames = new ArrayList<>();
    private ArrayList<Matrix> mSharedElementParentMatrices;
    private boolean mSharedElementTransitionComplete;
    protected final ArrayList<View> mSharedElements = new ArrayList<>();
    private ArrayList<View> mStrippedTransitioningViews = new ArrayList<>();
    protected ArrayList<View> mTransitioningViews = new ArrayList<>();
    private boolean mViewsTransitionComplete;
    private Window mWindow;

    /* access modifiers changed from: protected */
    public abstract Transition getViewsTransition();

    public ActivityTransitionCoordinator(Window window, ArrayList<String> allSharedElementNames, SharedElementCallback listener, boolean isReturning) {
        super(new Handler());
        this.mWindow = window;
        this.mListener = listener;
        this.mAllSharedElementNames = allSharedElementNames;
        this.mIsReturning = isReturning;
    }

    /* access modifiers changed from: protected */
    public void viewsReady(ArrayMap<String, View> sharedElements) {
        sharedElements.retainAll(this.mAllSharedElementNames);
        SharedElementCallback sharedElementCallback = this.mListener;
        if (sharedElementCallback != null) {
            sharedElementCallback.onMapSharedElements(this.mAllSharedElementNames, sharedElements);
        }
        setSharedElements(sharedElements);
        if (!(getViewsTransition() == null || this.mTransitioningViews == null)) {
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                decorView.captureTransitioningViews(this.mTransitioningViews);
            }
            this.mTransitioningViews.removeAll(this.mSharedElements);
        }
        setEpicenter();
    }

    private void setSharedElements(ArrayMap<String, View> sharedElements) {
        boolean isFirstRun = true;
        while (!sharedElements.isEmpty()) {
            for (int i = sharedElements.size() - 1; i >= 0; i--) {
                View view = sharedElements.valueAt(i);
                String name = sharedElements.keyAt(i);
                if (isFirstRun && (view == null || !view.isAttachedToWindow() || name == null)) {
                    sharedElements.removeAt(i);
                } else if (!isNested(view, sharedElements)) {
                    this.mSharedElementNames.add(name);
                    this.mSharedElements.add(view);
                    sharedElements.removeAt(i);
                }
            }
            isFirstRun = false;
        }
    }

    private static boolean isNested(View view, ArrayMap<String, View> sharedElements) {
        ViewParent parent = view.getParent();
        while (parent instanceof View) {
            View parentView = (View) parent;
            if (sharedElements.containsValue(parentView)) {
                return true;
            }
            parent = parentView.getParent();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void stripOffscreenViews() {
        if (this.mTransitioningViews != null) {
            Rect r = new Rect();
            for (int i = this.mTransitioningViews.size() - 1; i >= 0; i--) {
                View view = this.mTransitioningViews.get(i);
                if (!view.getGlobalVisibleRect(r)) {
                    this.mTransitioningViews.remove(i);
                    this.mStrippedTransitioningViews.add(view);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public Window getWindow() {
        return this.mWindow;
    }

    public ViewGroup getDecor() {
        Window window = this.mWindow;
        if (window == null) {
            return null;
        }
        return (ViewGroup) window.getDecorView();
    }

    /* access modifiers changed from: protected */
    public void setEpicenter() {
        int index;
        View epicenter = null;
        if (!this.mAllSharedElementNames.isEmpty() && !this.mSharedElementNames.isEmpty() && (index = this.mSharedElementNames.indexOf(this.mAllSharedElementNames.get(0))) >= 0) {
            epicenter = this.mSharedElements.get(index);
        }
        setEpicenter(epicenter);
    }

    private void setEpicenter(View view) {
        if (view == null) {
            this.mEpicenterCallback.setEpicenter(null);
            return;
        }
        Rect epicenter = new Rect();
        view.getBoundsOnScreen(epicenter);
        this.mEpicenterCallback.setEpicenter(epicenter);
    }

    public ArrayList<String> getAcceptedNames() {
        return this.mSharedElementNames;
    }

    public ArrayList<String> getMappedNames() {
        ArrayList<String> names = new ArrayList<>(this.mSharedElements.size());
        for (int i = 0; i < this.mSharedElements.size(); i++) {
            names.add(this.mSharedElements.get(i).getTransitionName());
        }
        return names;
    }

    public ArrayList<View> copyMappedViews() {
        return new ArrayList<>(this.mSharedElements);
    }

    /* access modifiers changed from: protected */
    public Transition setTargets(Transition transition, boolean add) {
        ArrayList<View> arrayList;
        ArrayList<View> arrayList2;
        if (transition == null) {
            return null;
        }
        if (add && ((arrayList2 = this.mTransitioningViews) == null || arrayList2.isEmpty())) {
            return null;
        }
        TransitionSet set = new TransitionSet();
        ArrayList<View> arrayList3 = this.mTransitioningViews;
        if (arrayList3 != null) {
            for (int i = arrayList3.size() - 1; i >= 0; i--) {
                View view = this.mTransitioningViews.get(i);
                if (add) {
                    set.addTarget(view);
                } else {
                    set.excludeTarget(view, true);
                }
            }
        }
        ArrayList<View> arrayList4 = this.mStrippedTransitioningViews;
        if (arrayList4 != null) {
            for (int i2 = arrayList4.size() - 1; i2 >= 0; i2--) {
                set.excludeTarget(this.mStrippedTransitioningViews.get(i2), true);
            }
        }
        set.addTransition(transition);
        if (add || (arrayList = this.mTransitioningViews) == null || arrayList.isEmpty()) {
            return set;
        }
        return new TransitionSet().addTransition(set);
    }

    /* access modifiers changed from: protected */
    public Transition configureTransition(Transition transition, boolean includeTransitioningViews) {
        if (transition != null) {
            Transition transition2 = transition.clone();
            transition2.setEpicenterCallback(this.mEpicenterCallback);
            transition = setTargets(transition2, includeTransitioningViews);
        }
        noLayoutSuppressionForVisibilityTransitions(transition);
        return transition;
    }

    protected static void removeExcludedViews(Transition transition, ArrayList<View> views) {
        ArraySet<View> included = new ArraySet<>();
        findIncludedViews(transition, views, included);
        views.clear();
        views.addAll(included);
    }

    private static void findIncludedViews(Transition transition, ArrayList<View> views, ArraySet<View> included) {
        if (transition instanceof TransitionSet) {
            TransitionSet set = (TransitionSet) transition;
            ArrayList<View> includedViews = new ArrayList<>();
            int numViews = views.size();
            for (int i = 0; i < numViews; i++) {
                View view = views.get(i);
                if (transition.isValidTarget(view)) {
                    includedViews.add(view);
                }
            }
            int count = set.getTransitionCount();
            for (int i2 = 0; i2 < count; i2++) {
                findIncludedViews(set.getTransitionAt(i2), includedViews, included);
            }
            return;
        }
        int numViews2 = views.size();
        for (int i3 = 0; i3 < numViews2; i3++) {
            View view2 = views.get(i3);
            if (transition.isValidTarget(view2)) {
                included.add(view2);
            }
        }
    }

    protected static Transition mergeTransitions(Transition transition1, Transition transition2) {
        if (transition1 == null) {
            return transition2;
        }
        if (transition2 == null) {
            return transition1;
        }
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(transition1);
        transitionSet.addTransition(transition2);
        return transitionSet;
    }

    /* access modifiers changed from: protected */
    public ArrayMap<String, View> mapSharedElements(ArrayList<String> accepted, ArrayList<View> localViews) {
        ArrayMap<String, View> sharedElements = new ArrayMap<>();
        if (accepted != null) {
            for (int i = 0; i < accepted.size(); i++) {
                sharedElements.put(accepted.get(i), localViews.get(i));
            }
        } else {
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                decorView.findNamedViews(sharedElements);
            }
        }
        return sharedElements;
    }

    /* access modifiers changed from: protected */
    public void setResultReceiver(ResultReceiver resultReceiver) {
        this.mResultReceiver = resultReceiver;
    }

    private void setSharedElementState(View view, String name, Bundle transitionArgs, Matrix tempMatrix, RectF tempRect, int[] decorLoc) {
        float bottom;
        float right;
        float top;
        float left;
        int scaleTypeInt;
        Bundle sharedElementBundle = transitionArgs.getBundle(name);
        if (sharedElementBundle != null) {
            if ((view instanceof ImageView) && (scaleTypeInt = sharedElementBundle.getInt(KEY_SCALE_TYPE, -1)) >= 0) {
                ImageView imageView = (ImageView) view;
                ImageView.ScaleType scaleType = SCALE_TYPE_VALUES[scaleTypeInt];
                imageView.setScaleType(scaleType);
                if (scaleType == ImageView.ScaleType.MATRIX) {
                    tempMatrix.setValues(sharedElementBundle.getFloatArray(KEY_IMAGE_MATRIX));
                    imageView.setImageMatrix(tempMatrix);
                }
            }
            view.setTranslationZ(sharedElementBundle.getFloat(KEY_TRANSLATION_Z));
            view.setElevation(sharedElementBundle.getFloat(KEY_ELEVATION));
            float left2 = sharedElementBundle.getFloat(KEY_SCREEN_LEFT);
            float top2 = sharedElementBundle.getFloat(KEY_SCREEN_TOP);
            float right2 = sharedElementBundle.getFloat(KEY_SCREEN_RIGHT);
            float bottom2 = sharedElementBundle.getFloat(KEY_SCREEN_BOTTOM);
            if (decorLoc != null) {
                left = left2 - ((float) decorLoc[0]);
                top = top2 - ((float) decorLoc[1]);
                right = right2 - ((float) decorLoc[0]);
                bottom = bottom2 - ((float) decorLoc[1]);
            } else {
                getSharedElementParentMatrix(view, tempMatrix);
                tempRect.set(left2, top2, right2, bottom2);
                tempMatrix.mapRect(tempRect);
                float leftInParent = tempRect.left;
                float topInParent = tempRect.top;
                view.getInverseMatrix().mapRect(tempRect);
                float width = tempRect.width();
                float height = tempRect.height();
                view.setLeft(0);
                view.setTop(0);
                view.setRight(Math.round(width));
                view.setBottom(Math.round(height));
                tempRect.set(0.0f, 0.0f, width, height);
                view.getMatrix().mapRect(tempRect);
                left = leftInParent - tempRect.left;
                top = topInParent - tempRect.top;
                right = left + width;
                bottom = top + height;
            }
            int x = Math.round(left);
            int y = Math.round(top);
            int width2 = Math.round(right) - x;
            int height2 = Math.round(bottom) - y;
            view.measure(View.MeasureSpec.makeMeasureSpec(width2, 1073741824), View.MeasureSpec.makeMeasureSpec(height2, 1073741824));
            view.layout(x, y, x + width2, y + height2);
        }
    }

    private void setSharedElementMatrices() {
        int numSharedElements = this.mSharedElements.size();
        if (numSharedElements > 0) {
            this.mSharedElementParentMatrices = new ArrayList<>(numSharedElements);
        }
        for (int i = 0; i < numSharedElements; i++) {
            ViewGroup parent = (ViewGroup) this.mSharedElements.get(i).getParent();
            Matrix matrix = new Matrix();
            if (parent != null) {
                parent.transformMatrixToLocal(matrix);
                matrix.postTranslate((float) parent.getScrollX(), (float) parent.getScrollY());
            }
            this.mSharedElementParentMatrices.add(matrix);
        }
    }

    private void getSharedElementParentMatrix(View view, Matrix matrix) {
        int index;
        if (this.mSharedElementParentMatrices == null) {
            index = -1;
        } else {
            index = this.mSharedElements.indexOf(view);
        }
        if (index < 0) {
            matrix.reset();
            ViewParent viewParent = view.getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) viewParent;
                parent.transformMatrixToLocal(matrix);
                matrix.postTranslate((float) parent.getScrollX(), (float) parent.getScrollY());
                return;
            }
            return;
        }
        matrix.set(this.mSharedElementParentMatrices.get(index));
    }

    /* access modifiers changed from: protected */
    public ArrayList<SharedElementOriginalState> setSharedElementState(Bundle sharedElementState, ArrayList<View> snapshots) {
        ArrayList<SharedElementOriginalState> originalImageState = new ArrayList<>();
        if (sharedElementState != null) {
            Matrix tempMatrix = new Matrix();
            RectF tempRect = new RectF();
            int numSharedElements = this.mSharedElements.size();
            for (int i = 0; i < numSharedElements; i++) {
                View sharedElement = this.mSharedElements.get(i);
                String name = this.mSharedElementNames.get(i);
                originalImageState.add(getOldSharedElementState(sharedElement, name, sharedElementState));
                setSharedElementState(sharedElement, name, sharedElementState, tempMatrix, tempRect, null);
            }
        }
        SharedElementCallback sharedElementCallback = this.mListener;
        if (sharedElementCallback != null) {
            sharedElementCallback.onSharedElementStart(this.mSharedElementNames, this.mSharedElements, snapshots);
        }
        return originalImageState;
    }

    /* access modifiers changed from: protected */
    /* renamed from: notifySharedElementEnd */
    public void lambda$scheduleSetSharedElementEnd$0$ActivityTransitionCoordinator(ArrayList<View> snapshots) {
        SharedElementCallback sharedElementCallback = this.mListener;
        if (sharedElementCallback != null) {
            sharedElementCallback.onSharedElementEnd(this.mSharedElementNames, this.mSharedElements, snapshots);
        }
    }

    /* access modifiers changed from: protected */
    public void scheduleSetSharedElementEnd(ArrayList<View> snapshots) {
        View decorView = getDecor();
        if (decorView != null) {
            OneShotPreDrawListener.add(decorView, new Runnable(snapshots) {
                /* class android.app.$$Lambda$ActivityTransitionCoordinator$fkaPvc8GCghP2GMwEgS_J5m_T_4 */
                private final /* synthetic */ ArrayList f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActivityTransitionCoordinator.this.lambda$scheduleSetSharedElementEnd$0$ActivityTransitionCoordinator(this.f$1);
                }
            });
        }
    }

    private static SharedElementOriginalState getOldSharedElementState(View view, String name, Bundle transitionArgs) {
        Bundle bundle;
        SharedElementOriginalState state = new SharedElementOriginalState();
        state.mLeft = view.getLeft();
        state.mTop = view.getTop();
        state.mRight = view.getRight();
        state.mBottom = view.getBottom();
        state.mMeasuredWidth = view.getMeasuredWidth();
        state.mMeasuredHeight = view.getMeasuredHeight();
        state.mTranslationZ = view.getTranslationZ();
        state.mElevation = view.getElevation();
        if (!(view instanceof ImageView) || (bundle = transitionArgs.getBundle(name)) == null || bundle.getInt(KEY_SCALE_TYPE, -1) < 0) {
            return state;
        }
        ImageView imageView = (ImageView) view;
        state.mScaleType = imageView.getScaleType();
        if (state.mScaleType == ImageView.ScaleType.MATRIX) {
            state.mMatrix = new Matrix(imageView.getImageMatrix());
        }
        return state;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x006d  */
    public ArrayList<View> createSnapshots(Bundle state, Collection<String> names) {
        View snapshot;
        int numSharedElements = names.size();
        ArrayList<View> snapshots = new ArrayList<>(numSharedElements);
        if (numSharedElements == 0) {
            return snapshots;
        }
        Context context = getWindow().getContext();
        int[] decorLoc = new int[2];
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            decorView.getLocationOnScreen(decorLoc);
        }
        Matrix tempMatrix = new Matrix();
        for (String name : names) {
            Bundle sharedElementBundle = state.getBundle(name);
            View snapshot2 = null;
            if (sharedElementBundle != null) {
                Parcelable parcelable = sharedElementBundle.getParcelable(KEY_SNAPSHOT);
                if (parcelable != null) {
                    SharedElementCallback sharedElementCallback = this.mListener;
                    if (sharedElementCallback != null) {
                        snapshot = sharedElementCallback.onCreateSnapshotView(context, parcelable);
                        if (snapshot == null) {
                            setSharedElementState(snapshot, name, state, tempMatrix, null, decorLoc);
                        }
                        snapshot2 = snapshot;
                    }
                }
                snapshot = null;
                if (snapshot == null) {
                }
                snapshot2 = snapshot;
            }
            snapshots.add(snapshot2);
        }
        return snapshots;
    }

    protected static void setOriginalSharedElementState(ArrayList<View> sharedElements, ArrayList<SharedElementOriginalState> originalState) {
        for (int i = 0; i < originalState.size(); i++) {
            View view = sharedElements.get(i);
            SharedElementOriginalState state = originalState.get(i);
            if ((view instanceof ImageView) && state.mScaleType != null) {
                ImageView imageView = (ImageView) view;
                imageView.setScaleType(state.mScaleType);
                if (state.mScaleType == ImageView.ScaleType.MATRIX) {
                    imageView.setImageMatrix(state.mMatrix);
                }
            }
            view.setElevation(state.mElevation);
            view.setTranslationZ(state.mTranslationZ);
            view.measure(View.MeasureSpec.makeMeasureSpec(state.mMeasuredWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(state.mMeasuredHeight, 1073741824));
            view.layout(state.mLeft, state.mTop, state.mRight, state.mBottom);
        }
    }

    /* access modifiers changed from: protected */
    public Bundle captureSharedElementState() {
        Bundle bundle = new Bundle();
        RectF tempBounds = new RectF();
        Matrix tempMatrix = new Matrix();
        for (int i = 0; i < this.mSharedElements.size(); i++) {
            captureSharedElementState(this.mSharedElements.get(i), this.mSharedElementNames.get(i), bundle, tempMatrix, tempBounds);
        }
        return bundle;
    }

    /* access modifiers changed from: protected */
    public void clearState() {
        this.mWindow = null;
        this.mSharedElements.clear();
        this.mTransitioningViews = null;
        this.mStrippedTransitioningViews = null;
        this.mOriginalAlphas.clear();
        this.mResultReceiver = null;
        this.mPendingTransition = null;
        this.mListener = null;
        this.mSharedElementParentMatrices = null;
    }

    /* access modifiers changed from: protected */
    public long getFadeDuration() {
        return getWindow().getTransitionBackgroundFadeDuration();
    }

    /* access modifiers changed from: protected */
    public void hideViews(ArrayList<View> views) {
        int count = views.size();
        for (int i = 0; i < count; i++) {
            View view = views.get(i);
            if (!this.mOriginalAlphas.containsKey(view)) {
                this.mOriginalAlphas.put(view, Float.valueOf(view.getAlpha()));
            }
            view.setAlpha(0.0f);
        }
    }

    /* access modifiers changed from: protected */
    public void showViews(ArrayList<View> views, boolean setTransitionAlpha) {
        int count = views.size();
        for (int i = 0; i < count; i++) {
            showView(views.get(i), setTransitionAlpha);
        }
    }

    private void showView(View view, boolean setTransitionAlpha) {
        Float alpha = this.mOriginalAlphas.remove(view);
        if (alpha != null) {
            view.setAlpha(alpha.floatValue());
        }
        if (setTransitionAlpha) {
            view.setTransitionAlpha(1.0f);
        }
    }

    /* access modifiers changed from: protected */
    public void captureSharedElementState(View view, String name, Bundle transitionArgs, Matrix tempMatrix, RectF tempBounds) {
        Bundle sharedElementBundle = new Bundle();
        tempMatrix.reset();
        view.transformMatrixToGlobal(tempMatrix);
        tempBounds.set(0.0f, 0.0f, (float) view.getWidth(), (float) view.getHeight());
        tempMatrix.mapRect(tempBounds);
        sharedElementBundle.putFloat(KEY_SCREEN_LEFT, tempBounds.left);
        sharedElementBundle.putFloat(KEY_SCREEN_RIGHT, tempBounds.right);
        sharedElementBundle.putFloat(KEY_SCREEN_TOP, tempBounds.top);
        sharedElementBundle.putFloat(KEY_SCREEN_BOTTOM, tempBounds.bottom);
        sharedElementBundle.putFloat(KEY_TRANSLATION_Z, view.getTranslationZ());
        sharedElementBundle.putFloat(KEY_ELEVATION, view.getElevation());
        Parcelable bitmap = null;
        SharedElementCallback sharedElementCallback = this.mListener;
        if (sharedElementCallback != null) {
            bitmap = sharedElementCallback.onCaptureSharedElementSnapshot(view, tempMatrix, tempBounds);
        }
        if (bitmap != null) {
            sharedElementBundle.putParcelable(KEY_SNAPSHOT, bitmap);
        }
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            sharedElementBundle.putInt(KEY_SCALE_TYPE, scaleTypeToInt(imageView.getScaleType()));
            if (imageView.getScaleType() == ImageView.ScaleType.MATRIX) {
                float[] matrix = new float[9];
                imageView.getImageMatrix().getValues(matrix);
                sharedElementBundle.putFloatArray(KEY_IMAGE_MATRIX, matrix);
            }
        }
        transitionArgs.putBundle(name, sharedElementBundle);
    }

    /* access modifiers changed from: protected */
    public void startTransition(Runnable runnable) {
        if (this.mIsStartingTransition) {
            this.mPendingTransition = runnable;
            return;
        }
        this.mIsStartingTransition = true;
        runnable.run();
    }

    /* access modifiers changed from: protected */
    public void transitionStarted() {
        this.mIsStartingTransition = false;
    }

    /* access modifiers changed from: protected */
    public boolean cancelPendingTransitions() {
        this.mPendingTransition = null;
        return this.mIsStartingTransition;
    }

    /* access modifiers changed from: protected */
    public void moveSharedElementsToOverlay() {
        Window window = this.mWindow;
        if (window != null && window.getSharedElementsUseOverlay()) {
            setSharedElementMatrices();
            int numSharedElements = this.mSharedElements.size();
            ViewGroup decor = getDecor();
            if (decor != null) {
                boolean moveWithParent = moveSharedElementWithParent();
                Matrix tempMatrix = new Matrix();
                for (int i = 0; i < numSharedElements; i++) {
                    View view = this.mSharedElements.get(i);
                    if (view.isAttachedToWindow()) {
                        tempMatrix.reset();
                        this.mSharedElementParentMatrices.get(i).invert(tempMatrix);
                        decor.transformMatrixToLocal(tempMatrix);
                        GhostView.addGhost(view, decor, tempMatrix);
                        ViewGroup parent = (ViewGroup) view.getParent();
                        if (moveWithParent && !isInTransitionGroup(parent, decor)) {
                            GhostViewListeners listener = new GhostViewListeners(view, parent, decor);
                            parent.getViewTreeObserver().addOnPreDrawListener(listener);
                            parent.addOnAttachStateChangeListener(listener);
                            this.mGhostViewListeners.add(listener);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean moveSharedElementWithParent() {
        return true;
    }

    public static boolean isInTransitionGroup(ViewParent viewParent, ViewGroup decor) {
        if (viewParent == decor || !(viewParent instanceof ViewGroup)) {
            return false;
        }
        ViewGroup parent = (ViewGroup) viewParent;
        if (parent.isTransitionGroup()) {
            return true;
        }
        return isInTransitionGroup(parent.getParent(), decor);
    }

    /* access modifiers changed from: protected */
    public void moveSharedElementsFromOverlay() {
        ViewGroup decor;
        int numListeners = this.mGhostViewListeners.size();
        for (int i = 0; i < numListeners; i++) {
            this.mGhostViewListeners.get(i).removeListener();
        }
        this.mGhostViewListeners.clear();
        Window window = this.mWindow;
        if (!(window == null || !window.getSharedElementsUseOverlay() || (decor = getDecor()) == null)) {
            decor.getOverlay();
            int count = this.mSharedElements.size();
            for (int i2 = 0; i2 < count; i2++) {
                GhostView.removeGhost(this.mSharedElements.get(i2));
            }
        }
    }

    /* access modifiers changed from: protected */
    /* renamed from: setGhostVisibility */
    public void lambda$scheduleGhostVisibilityChange$1$ActivityTransitionCoordinator(int visibility) {
        int numSharedElements = this.mSharedElements.size();
        for (int i = 0; i < numSharedElements; i++) {
            GhostView ghostView = GhostView.getGhost(this.mSharedElements.get(i));
            if (ghostView != null) {
                ghostView.setVisibility(visibility);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void scheduleGhostVisibilityChange(int visibility) {
        View decorView = getDecor();
        if (decorView != null) {
            OneShotPreDrawListener.add(decorView, new Runnable(visibility) {
                /* class android.app.$$Lambda$ActivityTransitionCoordinator$_HMo0E15AzCK9fwQ8WHzdz8ZIw */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActivityTransitionCoordinator.this.lambda$scheduleGhostVisibilityChange$1$ActivityTransitionCoordinator(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public boolean isViewsTransitionComplete() {
        return this.mViewsTransitionComplete;
    }

    /* access modifiers changed from: protected */
    public void viewsTransitionComplete() {
        this.mViewsTransitionComplete = true;
        startInputWhenTransitionsComplete();
    }

    /* access modifiers changed from: protected */
    public void backgroundAnimatorComplete() {
        this.mBackgroundAnimatorComplete = true;
    }

    /* access modifiers changed from: protected */
    public void sharedElementTransitionComplete() {
        this.mSharedElementTransitionComplete = true;
        startInputWhenTransitionsComplete();
    }

    private void startInputWhenTransitionsComplete() {
        ViewRootImpl viewRoot;
        if (this.mViewsTransitionComplete && this.mSharedElementTransitionComplete) {
            View decor = getDecor();
            if (!(decor == null || (viewRoot = decor.getViewRootImpl()) == null)) {
                viewRoot.setPausedForTransition(false);
            }
            onTransitionsComplete();
        }
    }

    /* access modifiers changed from: protected */
    public void pauseInput() {
        View decor = getDecor();
        ViewRootImpl viewRoot = decor == null ? null : decor.getViewRootImpl();
        if (viewRoot != null) {
            viewRoot.setPausedForTransition(true);
        }
    }

    /* access modifiers changed from: protected */
    public void onTransitionsComplete() {
    }

    /* access modifiers changed from: protected */
    public class ContinueTransitionListener extends TransitionListenerAdapter {
        protected ContinueTransitionListener() {
        }

        @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
        public void onTransitionStart(Transition transition) {
            ActivityTransitionCoordinator.this.mIsStartingTransition = false;
            Runnable pending = ActivityTransitionCoordinator.this.mPendingTransition;
            ActivityTransitionCoordinator.this.mPendingTransition = null;
            if (pending != null) {
                ActivityTransitionCoordinator.this.startTransition(pending);
            }
        }

        @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
        public void onTransitionEnd(Transition transition) {
            transition.removeListener(this);
        }
    }

    private static int scaleTypeToInt(ImageView.ScaleType scaleType) {
        int i = 0;
        while (true) {
            ImageView.ScaleType[] scaleTypeArr = SCALE_TYPE_VALUES;
            if (i >= scaleTypeArr.length) {
                return -1;
            }
            if (scaleType == scaleTypeArr[i]) {
                return i;
            }
            i++;
        }
    }

    /* access modifiers changed from: protected */
    public void setTransitioningViewsVisiblity(int visiblity, boolean invalidate) {
        ArrayList<View> arrayList = this.mTransitioningViews;
        int numElements = arrayList == null ? 0 : arrayList.size();
        for (int i = 0; i < numElements; i++) {
            View view = this.mTransitioningViews.get(i);
            if (invalidate) {
                view.setVisibility(visiblity);
            } else {
                view.setTransitionVisibility(visiblity);
            }
        }
    }

    private static void noLayoutSuppressionForVisibilityTransitions(Transition transition) {
        if (transition instanceof Visibility) {
            ((Visibility) transition).setSuppressLayout(false);
        } else if (transition instanceof TransitionSet) {
            TransitionSet set = (TransitionSet) transition;
            int count = set.getTransitionCount();
            for (int i = 0; i < count; i++) {
                noLayoutSuppressionForVisibilityTransitions(set.getTransitionAt(i));
            }
        }
    }

    public boolean isTransitionRunning() {
        return !this.mViewsTransitionComplete || !this.mSharedElementTransitionComplete || !this.mBackgroundAnimatorComplete;
    }

    /* access modifiers changed from: private */
    public static class FixedEpicenterCallback extends Transition.EpicenterCallback {
        private Rect mEpicenter;

        private FixedEpicenterCallback() {
        }

        public void setEpicenter(Rect epicenter) {
            this.mEpicenter = epicenter;
        }

        @Override // android.transition.Transition.EpicenterCallback
        public Rect onGetEpicenter(Transition transition) {
            return this.mEpicenter;
        }
    }

    private static class GhostViewListeners implements ViewTreeObserver.OnPreDrawListener, View.OnAttachStateChangeListener {
        private ViewGroup mDecor;
        private Matrix mMatrix = new Matrix();
        private View mParent;
        private View mView;
        private ViewTreeObserver mViewTreeObserver;

        public GhostViewListeners(View view, View parent, ViewGroup decor) {
            this.mView = view;
            this.mParent = parent;
            this.mDecor = decor;
            this.mViewTreeObserver = parent.getViewTreeObserver();
        }

        public View getView() {
            return this.mView;
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            GhostView ghostView = GhostView.getGhost(this.mView);
            if (ghostView == null || !this.mView.isAttachedToWindow()) {
                removeListener();
                return true;
            }
            GhostView.calculateMatrix(this.mView, this.mDecor, this.mMatrix);
            ghostView.setMatrix(this.mMatrix);
            return true;
        }

        public void removeListener() {
            if (this.mViewTreeObserver.isAlive()) {
                this.mViewTreeObserver.removeOnPreDrawListener(this);
            } else {
                this.mParent.getViewTreeObserver().removeOnPreDrawListener(this);
            }
            this.mParent.removeOnAttachStateChangeListener(this);
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View v) {
            this.mViewTreeObserver = v.getViewTreeObserver();
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View v) {
            removeListener();
        }
    }

    /* access modifiers changed from: package-private */
    public static class SharedElementOriginalState {
        int mBottom;
        float mElevation;
        int mLeft;
        Matrix mMatrix;
        int mMeasuredHeight;
        int mMeasuredWidth;
        int mRight;
        ImageView.ScaleType mScaleType;
        int mTop;
        float mTranslationZ;

        SharedElementOriginalState() {
        }
    }
}
