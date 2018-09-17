package android.app;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.security.keymaster.KeymasterDefs;
import android.speech.tts.TextToSpeech.Engine;
import android.transition.Transition;
import android.transition.Transition.EpicenterCallback;
import android.transition.Transition.TransitionListenerAdapter;
import android.transition.TransitionSet;
import android.transition.Visibility;
import android.util.ArrayMap;
import android.view.GhostView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.ArrayList;
import java.util.Collection;

abstract class ActivityTransitionCoordinator extends ResultReceiver {
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
    public static final int MSG_CANCEL = 106;
    public static final int MSG_EXIT_TRANSITION_COMPLETE = 104;
    public static final int MSG_HIDE_SHARED_ELEMENTS = 101;
    public static final int MSG_SET_REMOTE_RECEIVER = 100;
    public static final int MSG_SHARED_ELEMENT_DESTINATION = 107;
    public static final int MSG_START_EXIT_TRANSITION = 105;
    public static final int MSG_TAKE_SHARED_ELEMENTS = 103;
    protected static final ScaleType[] SCALE_TYPE_VALUES = null;
    private static final String TAG = "ActivityTransitionCoordinator";
    protected final ArrayList<String> mAllSharedElementNames;
    private final FixedEpicenterCallback mEpicenterCallback;
    private ArrayList<GhostViewListeners> mGhostViewListeners;
    protected final boolean mIsReturning;
    private boolean mIsStartingTransition;
    protected SharedElementCallback mListener;
    private ArrayMap<View, Float> mOriginalAlphas;
    private Runnable mPendingTransition;
    protected ResultReceiver mResultReceiver;
    protected final ArrayList<String> mSharedElementNames;
    private ArrayList<Matrix> mSharedElementParentMatrices;
    private boolean mSharedElementTransitionComplete;
    protected final ArrayList<View> mSharedElements;
    protected ArrayList<View> mTransitioningViews;
    private boolean mViewsTransitionComplete;
    private Window mWindow;

    /* renamed from: android.app.ActivityTransitionCoordinator.1 */
    class AnonymousClass1 implements OnPreDrawListener {
        final /* synthetic */ View val$decorView;
        final /* synthetic */ ArrayList val$snapshots;

        AnonymousClass1(View val$decorView, ArrayList val$snapshots) {
            this.val$decorView = val$decorView;
            this.val$snapshots = val$snapshots;
        }

        public boolean onPreDraw() {
            this.val$decorView.getViewTreeObserver().removeOnPreDrawListener(this);
            ActivityTransitionCoordinator.this.notifySharedElementEnd(this.val$snapshots);
            return true;
        }
    }

    /* renamed from: android.app.ActivityTransitionCoordinator.2 */
    class AnonymousClass2 implements OnPreDrawListener {
        final /* synthetic */ View val$decorView;
        final /* synthetic */ int val$visibility;

        AnonymousClass2(View val$decorView, int val$visibility) {
            this.val$decorView = val$decorView;
            this.val$visibility = val$visibility;
        }

        public boolean onPreDraw() {
            this.val$decorView.getViewTreeObserver().removeOnPreDrawListener(this);
            ActivityTransitionCoordinator.this.setGhostVisibility(this.val$visibility);
            return true;
        }
    }

    protected class ContinueTransitionListener extends TransitionListenerAdapter {
        protected ContinueTransitionListener() {
        }

        public void onTransitionStart(Transition transition) {
            ActivityTransitionCoordinator.this.mIsStartingTransition = false;
            Runnable pending = ActivityTransitionCoordinator.this.mPendingTransition;
            ActivityTransitionCoordinator.this.mPendingTransition = null;
            if (pending != null) {
                ActivityTransitionCoordinator.this.startTransition(pending);
            }
        }
    }

    private static class FixedEpicenterCallback extends EpicenterCallback {
        private Rect mEpicenter;

        private FixedEpicenterCallback() {
        }

        public void setEpicenter(Rect epicenter) {
            this.mEpicenter = epicenter;
        }

        public Rect onGetEpicenter(Transition transition) {
            return this.mEpicenter;
        }
    }

    private static class GhostViewListeners implements OnPreDrawListener {
        private ViewGroup mDecor;
        private Matrix mMatrix;
        private View mParent;
        private View mView;

        public GhostViewListeners(View view, View parent, ViewGroup decor) {
            this.mMatrix = new Matrix();
            this.mView = view;
            this.mParent = parent;
            this.mDecor = decor;
        }

        public View getView() {
            return this.mView;
        }

        public boolean onPreDraw() {
            GhostView ghostView = GhostView.getGhost(this.mView);
            if (ghostView == null) {
                this.mParent.getViewTreeObserver().removeOnPreDrawListener(this);
            } else {
                GhostView.calculateMatrix(this.mView, this.mDecor, this.mMatrix);
                ghostView.setMatrix(this.mMatrix);
            }
            return true;
        }
    }

    static class SharedElementOriginalState {
        int mBottom;
        float mElevation;
        int mLeft;
        Matrix mMatrix;
        int mMeasuredHeight;
        int mMeasuredWidth;
        int mRight;
        ScaleType mScaleType;
        int mTop;
        float mTranslationZ;

        SharedElementOriginalState() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.ActivityTransitionCoordinator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.ActivityTransitionCoordinator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityTransitionCoordinator.<clinit>():void");
    }

    protected abstract Transition getViewsTransition();

    public ActivityTransitionCoordinator(Window window, ArrayList<String> allSharedElementNames, SharedElementCallback listener, boolean isReturning) {
        super(new Handler());
        this.mSharedElements = new ArrayList();
        this.mSharedElementNames = new ArrayList();
        this.mTransitioningViews = new ArrayList();
        this.mEpicenterCallback = new FixedEpicenterCallback();
        this.mGhostViewListeners = new ArrayList();
        this.mOriginalAlphas = new ArrayMap();
        this.mWindow = window;
        this.mListener = listener;
        this.mAllSharedElementNames = allSharedElementNames;
        this.mIsReturning = isReturning;
    }

    protected void viewsReady(ArrayMap<String, View> sharedElements) {
        sharedElements.retainAll(this.mAllSharedElementNames);
        if (this.mListener != null) {
            this.mListener.onMapSharedElements(this.mAllSharedElementNames, sharedElements);
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
                View view = (View) sharedElements.valueAt(i);
                String name = (String) sharedElements.keyAt(i);
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

    protected void stripOffscreenViews() {
        if (this.mTransitioningViews != null) {
            Rect r = new Rect();
            for (int i = this.mTransitioningViews.size() - 1; i >= 0; i--) {
                View view = (View) this.mTransitioningViews.get(i);
                if (!view.getGlobalVisibleRect(r)) {
                    this.mTransitioningViews.remove(i);
                    showView(view, true);
                }
            }
        }
    }

    protected Window getWindow() {
        return this.mWindow;
    }

    public ViewGroup getDecor() {
        return this.mWindow == null ? null : (ViewGroup) this.mWindow.getDecorView();
    }

    protected void setEpicenter() {
        View epicenter = null;
        if (!(this.mAllSharedElementNames.isEmpty() || this.mSharedElementNames.isEmpty())) {
            int index = this.mSharedElementNames.indexOf(this.mAllSharedElementNames.get(0));
            if (index >= 0) {
                epicenter = (View) this.mSharedElements.get(index);
            }
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
        ArrayList<String> names = new ArrayList(this.mSharedElements.size());
        for (int i = 0; i < this.mSharedElements.size(); i++) {
            names.add(((View) this.mSharedElements.get(i)).getTransitionName());
        }
        return names;
    }

    public ArrayList<View> copyMappedViews() {
        return new ArrayList(this.mSharedElements);
    }

    public ArrayList<String> getAllSharedElementNames() {
        return this.mAllSharedElementNames;
    }

    protected Transition setTargets(Transition transition, boolean add) {
        if (transition == null || (add && (this.mTransitioningViews == null || this.mTransitioningViews.isEmpty()))) {
            return null;
        }
        TransitionSet set = new TransitionSet();
        if (this.mTransitioningViews != null) {
            for (int i = this.mTransitioningViews.size() - 1; i >= 0; i--) {
                View view = (View) this.mTransitioningViews.get(i);
                if (add) {
                    set.addTarget(view);
                } else {
                    set.excludeTarget(view, true);
                }
            }
        }
        set.addTransition(transition);
        if (!(add || this.mTransitioningViews == null || this.mTransitioningViews.isEmpty())) {
            set = new TransitionSet().addTransition(set);
        }
        return set;
    }

    protected Transition configureTransition(Transition transition, boolean includeTransitioningViews) {
        if (transition != null) {
            transition = transition.clone();
            transition.setEpicenterCallback(this.mEpicenterCallback);
            transition = setTargets(transition, includeTransitioningViews);
        }
        noLayoutSuppressionForVisibilityTransitions(transition);
        return transition;
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

    protected ArrayMap<String, View> mapSharedElements(ArrayList<String> accepted, ArrayList<View> localViews) {
        ArrayMap<String, View> sharedElements = new ArrayMap();
        if (accepted != null) {
            for (int i = 0; i < accepted.size(); i++) {
                sharedElements.put((String) accepted.get(i), (View) localViews.get(i));
            }
        } else {
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                decorView.findNamedViews(sharedElements);
            }
        }
        return sharedElements;
    }

    protected void setResultReceiver(ResultReceiver resultReceiver) {
        this.mResultReceiver = resultReceiver;
    }

    private void setSharedElementState(View view, String name, Bundle transitionArgs, Matrix tempMatrix, RectF tempRect, int[] decorLoc) {
        Bundle sharedElementBundle = transitionArgs.getBundle(name);
        if (sharedElementBundle != null) {
            if (view instanceof ImageView) {
                int scaleTypeInt = sharedElementBundle.getInt(KEY_SCALE_TYPE, -1);
                if (scaleTypeInt >= 0) {
                    ImageView imageView = (ImageView) view;
                    ScaleType scaleType = SCALE_TYPE_VALUES[scaleTypeInt];
                    imageView.setScaleType(scaleType);
                    if (scaleType == ScaleType.MATRIX) {
                        tempMatrix.setValues(sharedElementBundle.getFloatArray(KEY_IMAGE_MATRIX));
                        imageView.setImageMatrix(tempMatrix);
                    }
                }
            }
            view.setTranslationZ(sharedElementBundle.getFloat(KEY_TRANSLATION_Z));
            view.setElevation(sharedElementBundle.getFloat(KEY_ELEVATION));
            float left = sharedElementBundle.getFloat(KEY_SCREEN_LEFT);
            float top = sharedElementBundle.getFloat(KEY_SCREEN_TOP);
            float right = sharedElementBundle.getFloat(KEY_SCREEN_RIGHT);
            float bottom = sharedElementBundle.getFloat(KEY_SCREEN_BOTTOM);
            if (decorLoc != null) {
                left -= (float) decorLoc[0];
                top -= (float) decorLoc[1];
                right -= (float) decorLoc[0];
                bottom -= (float) decorLoc[1];
            } else {
                getSharedElementParentMatrix(view, tempMatrix);
                tempRect.set(left, top, right, bottom);
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
            view.measure(MeasureSpec.makeMeasureSpec(width2, KeymasterDefs.KM_UINT_REP), MeasureSpec.makeMeasureSpec(height2, KeymasterDefs.KM_UINT_REP));
            view.layout(x, y, x + width2, y + height2);
        }
    }

    private void setSharedElementMatrices() {
        int numSharedElements = this.mSharedElements.size();
        if (numSharedElements > 0) {
            this.mSharedElementParentMatrices = new ArrayList(numSharedElements);
        }
        for (int i = 0; i < numSharedElements; i++) {
            ViewGroup parent = (ViewGroup) ((View) this.mSharedElements.get(i)).getParent();
            Matrix matrix = new Matrix();
            parent.transformMatrixToLocal(matrix);
            matrix.postTranslate((float) parent.getScrollX(), (float) parent.getScrollY());
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
        matrix.set((Matrix) this.mSharedElementParentMatrices.get(index));
    }

    protected ArrayList<SharedElementOriginalState> setSharedElementState(Bundle sharedElementState, ArrayList<View> snapshots) {
        ArrayList<SharedElementOriginalState> originalImageState = new ArrayList();
        if (sharedElementState != null) {
            Matrix tempMatrix = new Matrix();
            RectF tempRect = new RectF();
            int numSharedElements = this.mSharedElements.size();
            for (int i = 0; i < numSharedElements; i++) {
                View sharedElement = (View) this.mSharedElements.get(i);
                String name = (String) this.mSharedElementNames.get(i);
                originalImageState.add(getOldSharedElementState(sharedElement, name, sharedElementState));
                setSharedElementState(sharedElement, name, sharedElementState, tempMatrix, tempRect, null);
            }
        }
        if (this.mListener != null) {
            this.mListener.onSharedElementStart(this.mSharedElementNames, this.mSharedElements, snapshots);
        }
        return originalImageState;
    }

    protected void notifySharedElementEnd(ArrayList<View> snapshots) {
        if (this.mListener != null) {
            this.mListener.onSharedElementEnd(this.mSharedElementNames, this.mSharedElements, snapshots);
        }
    }

    protected void scheduleSetSharedElementEnd(ArrayList<View> snapshots) {
        View decorView = getDecor();
        if (decorView != null) {
            decorView.getViewTreeObserver().addOnPreDrawListener(new AnonymousClass1(decorView, snapshots));
        }
    }

    private static SharedElementOriginalState getOldSharedElementState(View view, String name, Bundle transitionArgs) {
        SharedElementOriginalState state = new SharedElementOriginalState();
        state.mLeft = view.getLeft();
        state.mTop = view.getTop();
        state.mRight = view.getRight();
        state.mBottom = view.getBottom();
        state.mMeasuredWidth = view.getMeasuredWidth();
        state.mMeasuredHeight = view.getMeasuredHeight();
        state.mTranslationZ = view.getTranslationZ();
        state.mElevation = view.getElevation();
        if (!(view instanceof ImageView)) {
            return state;
        }
        Bundle bundle = transitionArgs.getBundle(name);
        if (bundle == null || bundle.getInt(KEY_SCALE_TYPE, -1) < 0) {
            return state;
        }
        ImageView imageView = (ImageView) view;
        state.mScaleType = imageView.getScaleType();
        if (state.mScaleType == ScaleType.MATRIX) {
            state.mMatrix = new Matrix(imageView.getImageMatrix());
        }
        return state;
    }

    protected ArrayList<View> createSnapshots(Bundle state, Collection<String> names) {
        int numSharedElements = names.size();
        ArrayList<View> snapshots = new ArrayList(numSharedElements);
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
            Object obj = null;
            if (sharedElementBundle != null) {
                Parcelable parcelable = sharedElementBundle.getParcelable(KEY_SNAPSHOT);
                if (!(parcelable == null || this.mListener == null)) {
                    obj = this.mListener.onCreateSnapshotView(context, parcelable);
                }
                if (obj != null) {
                    setSharedElementState(obj, name, state, tempMatrix, null, decorLoc);
                }
            }
            snapshots.add(obj);
        }
        return snapshots;
    }

    protected static void setOriginalSharedElementState(ArrayList<View> sharedElements, ArrayList<SharedElementOriginalState> originalState) {
        for (int i = 0; i < originalState.size(); i++) {
            View view = (View) sharedElements.get(i);
            SharedElementOriginalState state = (SharedElementOriginalState) originalState.get(i);
            if ((view instanceof ImageView) && state.mScaleType != null) {
                ImageView imageView = (ImageView) view;
                imageView.setScaleType(state.mScaleType);
                if (state.mScaleType == ScaleType.MATRIX) {
                    imageView.setImageMatrix(state.mMatrix);
                }
            }
            view.setElevation(state.mElevation);
            view.setTranslationZ(state.mTranslationZ);
            view.measure(MeasureSpec.makeMeasureSpec(state.mMeasuredWidth, KeymasterDefs.KM_UINT_REP), MeasureSpec.makeMeasureSpec(state.mMeasuredHeight, KeymasterDefs.KM_UINT_REP));
            view.layout(state.mLeft, state.mTop, state.mRight, state.mBottom);
        }
    }

    protected Bundle captureSharedElementState() {
        Bundle bundle = new Bundle();
        RectF tempBounds = new RectF();
        Matrix tempMatrix = new Matrix();
        for (int i = 0; i < this.mSharedElements.size(); i++) {
            captureSharedElementState((View) this.mSharedElements.get(i), (String) this.mSharedElementNames.get(i), bundle, tempMatrix, tempBounds);
        }
        return bundle;
    }

    protected void clearState() {
        this.mWindow = null;
        this.mSharedElements.clear();
        this.mTransitioningViews = null;
        this.mOriginalAlphas.clear();
        this.mResultReceiver = null;
        this.mPendingTransition = null;
        this.mListener = null;
        this.mSharedElementParentMatrices = null;
    }

    protected long getFadeDuration() {
        return getWindow().getTransitionBackgroundFadeDuration();
    }

    protected void hideViews(ArrayList<View> views) {
        int count = views.size();
        for (int i = 0; i < count; i++) {
            View view = (View) views.get(i);
            if (!this.mOriginalAlphas.containsKey(view)) {
                this.mOriginalAlphas.put(view, Float.valueOf(view.getAlpha()));
            }
            view.setAlpha(0.0f);
        }
    }

    protected void showViews(ArrayList<View> views, boolean setTransitionAlpha) {
        int count = views.size();
        for (int i = 0; i < count; i++) {
            showView((View) views.get(i), setTransitionAlpha);
        }
    }

    private void showView(View view, boolean setTransitionAlpha) {
        Float alpha = (Float) this.mOriginalAlphas.remove(view);
        if (alpha != null) {
            view.setAlpha(alpha.floatValue());
        }
        if (setTransitionAlpha) {
            view.setTransitionAlpha(Engine.DEFAULT_VOLUME);
        }
    }

    protected void captureSharedElementState(View view, String name, Bundle transitionArgs, Matrix tempMatrix, RectF tempBounds) {
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
        if (this.mListener != null) {
            bitmap = this.mListener.onCaptureSharedElementSnapshot(view, tempMatrix, tempBounds);
        }
        if (bitmap != null) {
            sharedElementBundle.putParcelable(KEY_SNAPSHOT, bitmap);
        }
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            sharedElementBundle.putInt(KEY_SCALE_TYPE, scaleTypeToInt(imageView.getScaleType()));
            if (imageView.getScaleType() == ScaleType.MATRIX) {
                float[] matrix = new float[9];
                imageView.getImageMatrix().getValues(matrix);
                sharedElementBundle.putFloatArray(KEY_IMAGE_MATRIX, matrix);
            }
        }
        transitionArgs.putBundle(name, sharedElementBundle);
    }

    protected void startTransition(Runnable runnable) {
        if (this.mIsStartingTransition) {
            this.mPendingTransition = runnable;
            return;
        }
        this.mIsStartingTransition = true;
        runnable.run();
    }

    protected void transitionStarted() {
        this.mIsStartingTransition = false;
    }

    protected boolean cancelPendingTransitions() {
        this.mPendingTransition = null;
        return this.mIsStartingTransition;
    }

    protected void moveSharedElementsToOverlay() {
        if (this.mWindow != null && this.mWindow.getSharedElementsUseOverlay()) {
            setSharedElementMatrices();
            int numSharedElements = this.mSharedElements.size();
            ViewGroup decor = getDecor();
            if (decor != null) {
                boolean moveWithParent = moveSharedElementWithParent();
                Matrix tempMatrix = new Matrix();
                for (int i = 0; i < numSharedElements; i++) {
                    View view = (View) this.mSharedElements.get(i);
                    tempMatrix.reset();
                    ((Matrix) this.mSharedElementParentMatrices.get(i)).invert(tempMatrix);
                    GhostView.addGhost(view, decor, tempMatrix);
                    ViewGroup parent = (ViewGroup) view.getParent();
                    if (moveWithParent && !isInTransitionGroup(parent, decor)) {
                        GhostViewListeners listener = new GhostViewListeners(view, parent, decor);
                        parent.getViewTreeObserver().addOnPreDrawListener(listener);
                        this.mGhostViewListeners.add(listener);
                    }
                }
            }
        }
    }

    protected boolean moveSharedElementWithParent() {
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

    protected void moveSharedElementsFromOverlay() {
        int i;
        int numListeners = this.mGhostViewListeners.size();
        for (i = 0; i < numListeners; i++) {
            GhostViewListeners listener = (GhostViewListeners) this.mGhostViewListeners.get(i);
            ((ViewGroup) listener.getView().getParent()).getViewTreeObserver().removeOnPreDrawListener(listener);
        }
        this.mGhostViewListeners.clear();
        if (this.mWindow != null && this.mWindow.getSharedElementsUseOverlay()) {
            ViewGroup decor = getDecor();
            if (decor != null) {
                ViewGroupOverlay overlay = decor.getOverlay();
                int count = this.mSharedElements.size();
                for (i = 0; i < count; i++) {
                    GhostView.removeGhost((View) this.mSharedElements.get(i));
                }
            }
        }
    }

    protected void setGhostVisibility(int visibility) {
        int numSharedElements = this.mSharedElements.size();
        for (int i = 0; i < numSharedElements; i++) {
            GhostView ghostView = GhostView.getGhost((View) this.mSharedElements.get(i));
            if (ghostView != null) {
                ghostView.setVisibility(visibility);
            }
        }
    }

    protected void scheduleGhostVisibilityChange(int visibility) {
        View decorView = getDecor();
        if (decorView != null) {
            decorView.getViewTreeObserver().addOnPreDrawListener(new AnonymousClass2(decorView, visibility));
        }
    }

    protected boolean isViewsTransitionComplete() {
        return this.mViewsTransitionComplete;
    }

    protected void viewsTransitionComplete() {
        this.mViewsTransitionComplete = true;
        startInputWhenTransitionsComplete();
    }

    protected void sharedElementTransitionComplete() {
        this.mSharedElementTransitionComplete = true;
        startInputWhenTransitionsComplete();
    }

    private void startInputWhenTransitionsComplete() {
        if (this.mViewsTransitionComplete && this.mSharedElementTransitionComplete) {
            View decor = getDecor();
            if (decor != null) {
                ViewRootImpl viewRoot = decor.getViewRootImpl();
                if (viewRoot != null) {
                    viewRoot.setPausedForTransition(false);
                }
            }
            onTransitionsComplete();
        }
    }

    protected void pauseInput() {
        ViewRootImpl viewRoot = null;
        View decor = getDecor();
        if (decor != null) {
            viewRoot = decor.getViewRootImpl();
        }
        if (viewRoot != null) {
            viewRoot.setPausedForTransition(true);
        }
    }

    protected void onTransitionsComplete() {
    }

    private static int scaleTypeToInt(ScaleType scaleType) {
        for (int i = 0; i < SCALE_TYPE_VALUES.length; i++) {
            if (scaleType == SCALE_TYPE_VALUES[i]) {
                return i;
            }
        }
        return -1;
    }

    protected void setTransitioningViewsVisiblity(int visiblity, boolean invalidate) {
        int numElements = this.mTransitioningViews == null ? 0 : this.mTransitioningViews.size();
        for (int i = 0; i < numElements; i++) {
            View view = (View) this.mTransitioningViews.get(i);
            view.setTransitionVisibility(visiblity);
            if (invalidate) {
                view.invalidate();
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
}
