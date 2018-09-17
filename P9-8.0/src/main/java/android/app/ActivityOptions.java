package android.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.IRemoteCallback.Stub;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.util.Pair;
import android.util.Slog;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import java.util.ArrayList;

public class ActivityOptions {
    public static final int ANIM_CLIP_REVEAL = 11;
    public static final int ANIM_CUSTOM = 1;
    public static final int ANIM_CUSTOM_IN_PLACE = 10;
    public static final int ANIM_DEFAULT = 6;
    public static final int ANIM_LAUNCH_TASK_BEHIND = 7;
    public static final int ANIM_NONE = 0;
    public static final int ANIM_SCALE_UP = 2;
    public static final int ANIM_SCENE_TRANSITION = 5;
    public static final int ANIM_THUMBNAIL_ASPECT_SCALE_DOWN = 9;
    public static final int ANIM_THUMBNAIL_ASPECT_SCALE_UP = 8;
    public static final int ANIM_THUMBNAIL_SCALE_DOWN = 4;
    public static final int ANIM_THUMBNAIL_SCALE_UP = 3;
    public static final String EXTRA_USAGE_TIME_REPORT = "android.activity.usage_time";
    public static final String EXTRA_USAGE_TIME_REPORT_PACKAGES = "android.usage_time_packages";
    private static final String KEY_ANIMATION_FINISHED_LISTENER = "android:activity.animationFinishedListener";
    public static final String KEY_ANIM_ENTER_RES_ID = "android:activity.animEnterRes";
    public static final String KEY_ANIM_EXIT_RES_ID = "android:activity.animExitRes";
    public static final String KEY_ANIM_HEIGHT = "android:activity.animHeight";
    public static final String KEY_ANIM_IN_PLACE_RES_ID = "android:activity.animInPlaceRes";
    private static final String KEY_ANIM_SPECS = "android:activity.animSpecs";
    public static final String KEY_ANIM_START_LISTENER = "android:activity.animStartListener";
    public static final String KEY_ANIM_START_X = "android:activity.animStartX";
    public static final String KEY_ANIM_START_Y = "android:activity.animStartY";
    public static final String KEY_ANIM_THUMBNAIL = "android:activity.animThumbnail";
    public static final String KEY_ANIM_TYPE = "android:activity.animType";
    public static final String KEY_ANIM_WIDTH = "android:activity.animWidth";
    private static final String KEY_DOCK_CREATE_MODE = "android:activity.dockCreateMode";
    private static final String KEY_EXIT_COORDINATOR_INDEX = "android:activity.exitCoordinatorIndex";
    private static final String KEY_INSTANT_APP_VERIFICATION_BUNDLE = "android:instantapps.installerbundle";
    public static final String KEY_LAUNCH_BOUNDS = "android:activity.launchBounds";
    private static final String KEY_LAUNCH_DISPLAY_ID = "android.activity.launchDisplayId";
    private static final String KEY_LAUNCH_STACK_ID = "android.activity.launchStackId";
    private static final String KEY_LAUNCH_TASK_ID = "android.activity.launchTaskId";
    public static final String KEY_PACKAGE_NAME = "android:activity.packageName";
    private static final String KEY_RESULT_CODE = "android:activity.resultCode";
    private static final String KEY_RESULT_DATA = "android:activity.resultData";
    private static final String KEY_ROTATION_ANIMATION_HINT = "android:activity.rotationAnimationHint";
    private static final String KEY_SPECS_FUTURE = "android:activity.specsFuture";
    private static final String KEY_TASK_OVERLAY = "android.activity.taskOverlay";
    private static final String KEY_TASK_OVERLAY_CAN_RESUME = "android.activity.taskOverlayCanResume";
    private static final String KEY_TRANSITION_COMPLETE_LISTENER = "android:activity.transitionCompleteListener";
    private static final String KEY_TRANSITION_IS_RETURNING = "android:activity.transitionIsReturning";
    private static final String KEY_TRANSITION_SHARED_ELEMENTS = "android:activity.sharedElementNames";
    private static final String KEY_USAGE_TIME_REPORT = "android:activity.usageTimeReport";
    private static final String TAG = "ActivityOptions";
    private AppTransitionAnimationSpec[] mAnimSpecs;
    private IRemoteCallback mAnimationFinishedListener;
    private IRemoteCallback mAnimationStartedListener;
    private int mAnimationType = 0;
    private Bundle mAppVerificationBundle;
    private int mCustomEnterResId;
    private int mCustomExitResId;
    private int mCustomInPlaceResId;
    private int mDockCreateMode = 0;
    private int mExitCoordinatorIndex;
    private int mHeight;
    private boolean mIsReturning;
    private Rect mLaunchBounds;
    private int mLaunchDisplayId = -1;
    private int mLaunchStackId = -1;
    private int mLaunchTaskId = -1;
    private String mPackageName;
    private int mResultCode;
    private Intent mResultData;
    private int mRotationAnimationHint = -1;
    private ArrayList<String> mSharedElementNames;
    private IAppTransitionAnimationSpecsFuture mSpecsFuture;
    private int mStartX;
    private int mStartY;
    private boolean mTaskOverlay;
    private boolean mTaskOverlayCanResume;
    private Bitmap mThumbnail;
    private ResultReceiver mTransitionReceiver;
    private PendingIntent mUsageTimeReport;
    private int mWidth;

    private static class HideWindowListener extends TransitionListenerAdapter implements HideSharedElementsCallback {
        private final ExitTransitionCoordinator mExit;
        private boolean mSharedElementHidden;
        private ArrayList<View> mSharedElements;
        private boolean mTransitionEnded;
        private final boolean mWaitingForTransition;
        private final Window mWindow;

        public HideWindowListener(Window window, ExitTransitionCoordinator exit) {
            this.mWindow = window;
            this.mExit = exit;
            this.mSharedElements = new ArrayList(exit.mSharedElements);
            Transition transition = this.mWindow.getExitTransition();
            if (transition != null) {
                transition.addListener(this);
                this.mWaitingForTransition = true;
            } else {
                this.mWaitingForTransition = false;
            }
            View decorView = this.mWindow.getDecorView();
            if (decorView == null) {
                return;
            }
            if (decorView.getTag(16908813) != null) {
                throw new IllegalStateException("Cannot start a transition while one is running");
            }
            decorView.setTagInternal(16908813, exit);
        }

        public void onTransitionEnd(Transition transition) {
            this.mTransitionEnded = true;
            hideWhenDone();
            transition.removeListener(this);
        }

        public void hideSharedElements() {
            this.mSharedElementHidden = true;
            hideWhenDone();
        }

        private void hideWhenDone() {
            if (!this.mSharedElementHidden) {
                return;
            }
            if (!this.mWaitingForTransition || this.mTransitionEnded) {
                this.mExit.resetViews();
                int numSharedElements = this.mSharedElements.size();
                for (int i = 0; i < numSharedElements; i++) {
                    ((View) this.mSharedElements.get(i)).requestLayout();
                }
                View decorView = this.mWindow.getDecorView();
                if (decorView != null) {
                    decorView.setTagInternal(16908813, null);
                    decorView.setVisibility(8);
                }
            }
        }
    }

    public interface OnAnimationFinishedListener {
        void onAnimationFinished();
    }

    public interface OnAnimationStartedListener {
        void onAnimationStarted();
    }

    public static ActivityOptions makeCustomAnimation(Context context, int enterResId, int exitResId) {
        return makeCustomAnimation(context, enterResId, exitResId, null, null);
    }

    public static ActivityOptions makeCustomAnimation(Context context, int enterResId, int exitResId, Handler handler, OnAnimationStartedListener listener) {
        ActivityOptions opts = new ActivityOptions();
        opts.mPackageName = context.getPackageName();
        opts.mAnimationType = 1;
        opts.mCustomEnterResId = enterResId;
        opts.mCustomExitResId = exitResId;
        opts.setOnAnimationStartedListener(handler, listener);
        return opts;
    }

    public static ActivityOptions makeCustomInPlaceAnimation(Context context, int animId) {
        if (animId == 0) {
            throw new RuntimeException("You must specify a valid animation.");
        }
        ActivityOptions opts = new ActivityOptions();
        opts.mPackageName = context.getPackageName();
        opts.mAnimationType = 10;
        opts.mCustomInPlaceResId = animId;
        return opts;
    }

    private void setOnAnimationStartedListener(final Handler handler, final OnAnimationStartedListener listener) {
        if (listener != null) {
            this.mAnimationStartedListener = new Stub() {
                public void sendResult(Bundle data) throws RemoteException {
                    Handler handler = handler;
                    final OnAnimationStartedListener onAnimationStartedListener = listener;
                    handler.post(new Runnable() {
                        public void run() {
                            onAnimationStartedListener.onAnimationStarted();
                        }
                    });
                }
            };
        }
    }

    private void setOnAnimationFinishedListener(final Handler handler, final OnAnimationFinishedListener listener) {
        if (listener != null) {
            this.mAnimationFinishedListener = new Stub() {
                public void sendResult(Bundle data) throws RemoteException {
                    Handler handler = handler;
                    final OnAnimationFinishedListener onAnimationFinishedListener = listener;
                    handler.post(new Runnable() {
                        public void run() {
                            onAnimationFinishedListener.onAnimationFinished();
                        }
                    });
                }
            };
        }
    }

    public static ActivityOptions makeScaleUpAnimation(View source, int startX, int startY, int width, int height) {
        ActivityOptions opts = new ActivityOptions();
        opts.mPackageName = source.getContext().getPackageName();
        opts.mAnimationType = 2;
        int[] pts = new int[2];
        source.getLocationOnScreen(pts);
        opts.mStartX = pts[0] + startX;
        opts.mStartY = pts[1] + startY;
        opts.mWidth = width;
        opts.mHeight = height;
        return opts;
    }

    public static ActivityOptions makeClipRevealAnimation(View source, int startX, int startY, int width, int height) {
        ActivityOptions opts = new ActivityOptions();
        opts.mAnimationType = 11;
        int[] pts = new int[2];
        source.getLocationOnScreen(pts);
        opts.mStartX = pts[0] + startX;
        opts.mStartY = pts[1] + startY;
        opts.mWidth = width;
        opts.mHeight = height;
        return opts;
    }

    public static ActivityOptions makeThumbnailScaleUpAnimation(View source, Bitmap thumbnail, int startX, int startY) {
        return makeThumbnailScaleUpAnimation(source, thumbnail, startX, startY, null);
    }

    private static ActivityOptions makeThumbnailScaleUpAnimation(View source, Bitmap thumbnail, int startX, int startY, OnAnimationStartedListener listener) {
        return makeThumbnailAnimation(source, thumbnail, startX, startY, listener, true);
    }

    private static ActivityOptions makeThumbnailAnimation(View source, Bitmap thumbnail, int startX, int startY, OnAnimationStartedListener listener, boolean scaleUp) {
        ActivityOptions opts = new ActivityOptions();
        opts.mPackageName = source.getContext().getPackageName();
        opts.mAnimationType = scaleUp ? 3 : 4;
        opts.mThumbnail = thumbnail;
        int[] pts = new int[2];
        source.getLocationOnScreen(pts);
        opts.mStartX = pts[0] + startX;
        opts.mStartY = pts[1] + startY;
        opts.setOnAnimationStartedListener(source.getHandler(), listener);
        return opts;
    }

    public static ActivityOptions makeMultiThumbFutureAspectScaleAnimation(Context context, Handler handler, IAppTransitionAnimationSpecsFuture specsFuture, OnAnimationStartedListener listener, boolean scaleUp) {
        int i;
        ActivityOptions opts = new ActivityOptions();
        opts.mPackageName = context.getPackageName();
        if (scaleUp) {
            i = 8;
        } else {
            i = 9;
        }
        opts.mAnimationType = i;
        opts.mSpecsFuture = specsFuture;
        opts.setOnAnimationStartedListener(handler, listener);
        return opts;
    }

    public static ActivityOptions makeThumbnailAspectScaleDownAnimation(View source, Bitmap thumbnail, int startX, int startY, int targetWidth, int targetHeight, Handler handler, OnAnimationStartedListener listener) {
        return makeAspectScaledThumbnailAnimation(source, thumbnail, startX, startY, targetWidth, targetHeight, handler, listener, false);
    }

    private static ActivityOptions makeAspectScaledThumbnailAnimation(View source, Bitmap thumbnail, int startX, int startY, int targetWidth, int targetHeight, Handler handler, OnAnimationStartedListener listener, boolean scaleUp) {
        int i;
        ActivityOptions opts = new ActivityOptions();
        opts.mPackageName = source.getContext().getPackageName();
        if (scaleUp) {
            i = 8;
        } else {
            i = 9;
        }
        opts.mAnimationType = i;
        opts.mThumbnail = thumbnail;
        int[] pts = new int[2];
        source.getLocationOnScreen(pts);
        opts.mStartX = pts[0] + startX;
        opts.mStartY = pts[1] + startY;
        opts.mWidth = targetWidth;
        opts.mHeight = targetHeight;
        opts.setOnAnimationStartedListener(handler, listener);
        return opts;
    }

    public static ActivityOptions makeThumbnailAspectScaleDownAnimation(View source, AppTransitionAnimationSpec[] specs, Handler handler, OnAnimationStartedListener onAnimationStartedListener, OnAnimationFinishedListener onAnimationFinishedListener) {
        ActivityOptions opts = new ActivityOptions();
        opts.mPackageName = source.getContext().getPackageName();
        opts.mAnimationType = 9;
        opts.mAnimSpecs = specs;
        opts.setOnAnimationStartedListener(handler, onAnimationStartedListener);
        opts.setOnAnimationFinishedListener(handler, onAnimationFinishedListener);
        return opts;
    }

    public static ActivityOptions makeSceneTransitionAnimation(Activity activity, View sharedElement, String sharedElementName) {
        return makeSceneTransitionAnimation(activity, Pair.create(sharedElement, sharedElementName));
    }

    @SafeVarargs
    public static ActivityOptions makeSceneTransitionAnimation(Activity activity, Pair<View, String>... sharedElements) {
        ActivityOptions opts = new ActivityOptions();
        makeSceneTransitionAnimation(activity, activity.getWindow(), opts, activity.mExitTransitionListener, (Pair[]) sharedElements);
        return opts;
    }

    @SafeVarargs
    public static ActivityOptions startSharedElementAnimation(Window window, Pair<View, String>... sharedElements) {
        ActivityOptions opts = new ActivityOptions();
        if (window.getDecorView() == null) {
            return opts;
        }
        ExitTransitionCoordinator exit = makeSceneTransitionAnimation(null, window, opts, null, (Pair[]) sharedElements);
        if (exit != null) {
            exit.setHideSharedElementsCallback(new HideWindowListener(window, exit));
            exit.startExit();
        }
        return opts;
    }

    public static void stopSharedElementAnimation(Window window) {
        View decorView = window.getDecorView();
        if (decorView != null) {
            ExitTransitionCoordinator exit = (ExitTransitionCoordinator) decorView.getTag(16908813);
            if (exit != null) {
                exit.cancelPendingTransitions();
                decorView.setTagInternal(16908813, null);
                TransitionManager.endTransitions((ViewGroup) decorView);
                exit.resetViews();
                exit.clearState();
                decorView.setVisibility(0);
            }
        }
    }

    static ExitTransitionCoordinator makeSceneTransitionAnimation(Activity activity, Window window, ActivityOptions opts, SharedElementCallback callback, Pair<View, String>[] sharedElements) {
        if (window.hasFeature(13)) {
            opts.mAnimationType = 5;
            ArrayList<String> names = new ArrayList();
            ArrayList<View> views = new ArrayList();
            if (sharedElements != null) {
                for (Pair<View, String> sharedElement : sharedElements) {
                    String sharedElementName = sharedElement.second;
                    if (sharedElementName == null) {
                        throw new IllegalArgumentException("Shared element name must not be null");
                    }
                    names.add(sharedElementName);
                    if (sharedElement.first == null) {
                        throw new IllegalArgumentException("Shared element must not be null");
                    }
                    views.add((View) sharedElement.first);
                }
            }
            ExitTransitionCoordinator exit = new ExitTransitionCoordinator(activity, window, callback, names, names, views, false);
            opts.mTransitionReceiver = exit;
            opts.mSharedElementNames = names;
            opts.mIsReturning = activity == null;
            if (activity == null) {
                opts.mExitCoordinatorIndex = -1;
            } else {
                opts.mExitCoordinatorIndex = activity.mActivityTransitionState.addExitTransitionCoordinator(exit);
            }
            return exit;
        }
        opts.mAnimationType = 6;
        return null;
    }

    static ActivityOptions makeSceneTransitionAnimation(Activity activity, ExitTransitionCoordinator exitCoordinator, ArrayList<String> sharedElementNames, int resultCode, Intent resultData) {
        ActivityOptions opts = new ActivityOptions();
        opts.mAnimationType = 5;
        opts.mSharedElementNames = sharedElementNames;
        opts.mTransitionReceiver = exitCoordinator;
        opts.mIsReturning = true;
        opts.mResultCode = resultCode;
        opts.mResultData = resultData;
        opts.mExitCoordinatorIndex = activity.mActivityTransitionState.addExitTransitionCoordinator(exitCoordinator);
        return opts;
    }

    public static ActivityOptions makeTaskLaunchBehind() {
        ActivityOptions opts = new ActivityOptions();
        opts.mAnimationType = 7;
        return opts;
    }

    public static ActivityOptions makeBasic() {
        return new ActivityOptions();
    }

    public boolean getLaunchTaskBehind() {
        return this.mAnimationType == 7;
    }

    private ActivityOptions() {
    }

    public ActivityOptions(Bundle opts) {
        opts.setDefusable(true);
        this.mPackageName = opts.getString(KEY_PACKAGE_NAME);
        try {
            this.mUsageTimeReport = (PendingIntent) opts.getParcelable(KEY_USAGE_TIME_REPORT);
        } catch (RuntimeException e) {
            Slog.w(TAG, e);
        }
        this.mLaunchBounds = (Rect) opts.getParcelable(KEY_LAUNCH_BOUNDS);
        this.mAnimationType = opts.getInt(KEY_ANIM_TYPE);
        switch (this.mAnimationType) {
            case 1:
                this.mCustomEnterResId = opts.getInt(KEY_ANIM_ENTER_RES_ID, 0);
                this.mCustomExitResId = opts.getInt(KEY_ANIM_EXIT_RES_ID, 0);
                this.mAnimationStartedListener = Stub.asInterface(opts.getBinder(KEY_ANIM_START_LISTENER));
                break;
            case 2:
            case 11:
                this.mStartX = opts.getInt(KEY_ANIM_START_X, 0);
                this.mStartY = opts.getInt(KEY_ANIM_START_Y, 0);
                this.mWidth = opts.getInt(KEY_ANIM_WIDTH, 0);
                this.mHeight = opts.getInt(KEY_ANIM_HEIGHT, 0);
                break;
            case 3:
            case 4:
            case 8:
            case 9:
                GraphicBuffer buffer = (GraphicBuffer) opts.getParcelable(KEY_ANIM_THUMBNAIL);
                if (buffer != null) {
                    this.mThumbnail = Bitmap.createHardwareBitmap(buffer);
                }
                this.mStartX = opts.getInt(KEY_ANIM_START_X, 0);
                this.mStartY = opts.getInt(KEY_ANIM_START_Y, 0);
                this.mWidth = opts.getInt(KEY_ANIM_WIDTH, 0);
                this.mHeight = opts.getInt(KEY_ANIM_HEIGHT, 0);
                this.mAnimationStartedListener = Stub.asInterface(opts.getBinder(KEY_ANIM_START_LISTENER));
                break;
            case 5:
                this.mTransitionReceiver = (ResultReceiver) opts.getParcelable(KEY_TRANSITION_COMPLETE_LISTENER);
                this.mIsReturning = opts.getBoolean(KEY_TRANSITION_IS_RETURNING, false);
                this.mSharedElementNames = opts.getStringArrayList(KEY_TRANSITION_SHARED_ELEMENTS);
                this.mResultData = (Intent) opts.getParcelable(KEY_RESULT_DATA);
                this.mResultCode = opts.getInt(KEY_RESULT_CODE);
                this.mExitCoordinatorIndex = opts.getInt(KEY_EXIT_COORDINATOR_INDEX);
                break;
            case 10:
                this.mCustomInPlaceResId = opts.getInt(KEY_ANIM_IN_PLACE_RES_ID, 0);
                break;
        }
        this.mLaunchDisplayId = opts.getInt(KEY_LAUNCH_DISPLAY_ID, -1);
        this.mLaunchStackId = opts.getInt(KEY_LAUNCH_STACK_ID, -1);
        this.mLaunchTaskId = opts.getInt(KEY_LAUNCH_TASK_ID, -1);
        this.mTaskOverlay = opts.getBoolean(KEY_TASK_OVERLAY, false);
        this.mTaskOverlayCanResume = opts.getBoolean(KEY_TASK_OVERLAY_CAN_RESUME, false);
        this.mDockCreateMode = opts.getInt(KEY_DOCK_CREATE_MODE, 0);
        if (opts.containsKey(KEY_ANIM_SPECS)) {
            Parcelable[] specs = opts.getParcelableArray(KEY_ANIM_SPECS);
            this.mAnimSpecs = new AppTransitionAnimationSpec[specs.length];
            for (int i = specs.length - 1; i >= 0; i--) {
                this.mAnimSpecs[i] = (AppTransitionAnimationSpec) specs[i];
            }
        }
        if (opts.containsKey(KEY_ANIMATION_FINISHED_LISTENER)) {
            this.mAnimationFinishedListener = Stub.asInterface(opts.getBinder(KEY_ANIMATION_FINISHED_LISTENER));
        }
        this.mRotationAnimationHint = opts.getInt(KEY_ROTATION_ANIMATION_HINT);
        this.mAppVerificationBundle = opts.getBundle(KEY_INSTANT_APP_VERIFICATION_BUNDLE);
        if (opts.containsKey(KEY_SPECS_FUTURE)) {
            this.mSpecsFuture = IAppTransitionAnimationSpecsFuture.Stub.asInterface(opts.getBinder(KEY_SPECS_FUTURE));
        }
    }

    public ActivityOptions setLaunchBounds(Rect screenSpacePixelRect) {
        Rect rect = null;
        if (screenSpacePixelRect != null) {
            rect = new Rect(screenSpacePixelRect);
        }
        this.mLaunchBounds = rect;
        return this;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public Rect getLaunchBounds() {
        return this.mLaunchBounds;
    }

    public int getAnimationType() {
        return this.mAnimationType;
    }

    public int getCustomEnterResId() {
        return this.mCustomEnterResId;
    }

    public int getCustomExitResId() {
        return this.mCustomExitResId;
    }

    public int getCustomInPlaceResId() {
        return this.mCustomInPlaceResId;
    }

    public GraphicBuffer getThumbnail() {
        return this.mThumbnail != null ? this.mThumbnail.createGraphicBufferHandle() : null;
    }

    public int getStartX() {
        return this.mStartX;
    }

    public int getStartY() {
        return this.mStartY;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public IRemoteCallback getOnAnimationStartListener() {
        return this.mAnimationStartedListener;
    }

    public IRemoteCallback getAnimationFinishedListener() {
        return this.mAnimationFinishedListener;
    }

    public int getExitCoordinatorKey() {
        return this.mExitCoordinatorIndex;
    }

    public void abort() {
        if (this.mAnimationStartedListener != null) {
            try {
                this.mAnimationStartedListener.sendResult(null);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean isReturning() {
        return this.mIsReturning;
    }

    boolean isCrossTask() {
        return this.mExitCoordinatorIndex < 0;
    }

    public ArrayList<String> getSharedElementNames() {
        return this.mSharedElementNames;
    }

    public ResultReceiver getResultReceiver() {
        return this.mTransitionReceiver;
    }

    public int getResultCode() {
        return this.mResultCode;
    }

    public Intent getResultData() {
        return this.mResultData;
    }

    public PendingIntent getUsageTimeReport() {
        return this.mUsageTimeReport;
    }

    public AppTransitionAnimationSpec[] getAnimSpecs() {
        return this.mAnimSpecs;
    }

    public IAppTransitionAnimationSpecsFuture getSpecsFuture() {
        return this.mSpecsFuture;
    }

    public static ActivityOptions fromBundle(Bundle bOptions) {
        return bOptions != null ? new ActivityOptions(bOptions) : null;
    }

    public static void abort(ActivityOptions options) {
        if (options != null) {
            options.abort();
        }
    }

    public int getLaunchDisplayId() {
        return this.mLaunchDisplayId;
    }

    public ActivityOptions setLaunchDisplayId(int launchDisplayId) {
        this.mLaunchDisplayId = launchDisplayId;
        return this;
    }

    public int getLaunchStackId() {
        return this.mLaunchStackId;
    }

    public void setLaunchStackId(int launchStackId) {
        this.mLaunchStackId = launchStackId;
    }

    public void setLaunchTaskId(int taskId) {
        this.mLaunchTaskId = taskId;
    }

    public int getLaunchTaskId() {
        return this.mLaunchTaskId;
    }

    public void setTaskOverlay(boolean taskOverlay, boolean canResume) {
        this.mTaskOverlay = taskOverlay;
        this.mTaskOverlayCanResume = canResume;
    }

    public boolean getTaskOverlay() {
        return this.mTaskOverlay;
    }

    public boolean canTaskOverlayResume() {
        return this.mTaskOverlayCanResume;
    }

    public int getDockCreateMode() {
        return this.mDockCreateMode;
    }

    public void setDockCreateMode(int dockCreateMode) {
        this.mDockCreateMode = dockCreateMode;
    }

    public void update(ActivityOptions otherOptions) {
        if (otherOptions.mPackageName != null) {
            this.mPackageName = otherOptions.mPackageName;
        }
        this.mUsageTimeReport = otherOptions.mUsageTimeReport;
        this.mTransitionReceiver = null;
        this.mSharedElementNames = null;
        this.mIsReturning = false;
        this.mResultData = null;
        this.mResultCode = 0;
        this.mExitCoordinatorIndex = 0;
        this.mAnimationType = otherOptions.mAnimationType;
        switch (otherOptions.mAnimationType) {
            case 1:
                this.mCustomEnterResId = otherOptions.mCustomEnterResId;
                this.mCustomExitResId = otherOptions.mCustomExitResId;
                this.mThumbnail = null;
                if (this.mAnimationStartedListener != null) {
                    try {
                        this.mAnimationStartedListener.sendResult(null);
                    } catch (RemoteException e) {
                    }
                }
                this.mAnimationStartedListener = otherOptions.mAnimationStartedListener;
                break;
            case 2:
                this.mStartX = otherOptions.mStartX;
                this.mStartY = otherOptions.mStartY;
                this.mWidth = otherOptions.mWidth;
                this.mHeight = otherOptions.mHeight;
                if (this.mAnimationStartedListener != null) {
                    try {
                        this.mAnimationStartedListener.sendResult(null);
                    } catch (RemoteException e2) {
                    }
                }
                this.mAnimationStartedListener = null;
                break;
            case 3:
            case 4:
            case 8:
            case 9:
                this.mThumbnail = otherOptions.mThumbnail;
                this.mStartX = otherOptions.mStartX;
                this.mStartY = otherOptions.mStartY;
                this.mWidth = otherOptions.mWidth;
                this.mHeight = otherOptions.mHeight;
                if (this.mAnimationStartedListener != null) {
                    try {
                        this.mAnimationStartedListener.sendResult(null);
                    } catch (RemoteException e3) {
                    }
                }
                this.mAnimationStartedListener = otherOptions.mAnimationStartedListener;
                break;
            case 5:
                this.mTransitionReceiver = otherOptions.mTransitionReceiver;
                this.mSharedElementNames = otherOptions.mSharedElementNames;
                this.mIsReturning = otherOptions.mIsReturning;
                this.mThumbnail = null;
                this.mAnimationStartedListener = null;
                this.mResultData = otherOptions.mResultData;
                this.mResultCode = otherOptions.mResultCode;
                this.mExitCoordinatorIndex = otherOptions.mExitCoordinatorIndex;
                break;
            case 10:
                this.mCustomInPlaceResId = otherOptions.mCustomInPlaceResId;
                break;
        }
        this.mAnimSpecs = otherOptions.mAnimSpecs;
        this.mAnimationFinishedListener = otherOptions.mAnimationFinishedListener;
        this.mSpecsFuture = otherOptions.mSpecsFuture;
    }

    public Bundle toBundle() {
        IBinder iBinder = null;
        Bundle b = new Bundle();
        if (this.mPackageName != null) {
            b.putString(KEY_PACKAGE_NAME, this.mPackageName);
        }
        if (this.mLaunchBounds != null) {
            b.putParcelable(KEY_LAUNCH_BOUNDS, this.mLaunchBounds);
        }
        b.putInt(KEY_ANIM_TYPE, this.mAnimationType);
        if (this.mUsageTimeReport != null) {
            b.putParcelable(KEY_USAGE_TIME_REPORT, this.mUsageTimeReport);
        }
        String str;
        switch (this.mAnimationType) {
            case 1:
                b.putInt(KEY_ANIM_ENTER_RES_ID, this.mCustomEnterResId);
                b.putInt(KEY_ANIM_EXIT_RES_ID, this.mCustomExitResId);
                str = KEY_ANIM_START_LISTENER;
                if (this.mAnimationStartedListener != null) {
                    iBinder = this.mAnimationStartedListener.asBinder();
                }
                b.putBinder(str, iBinder);
                break;
            case 2:
            case 11:
                b.putInt(KEY_ANIM_START_X, this.mStartX);
                b.putInt(KEY_ANIM_START_Y, this.mStartY);
                b.putInt(KEY_ANIM_WIDTH, this.mWidth);
                b.putInt(KEY_ANIM_HEIGHT, this.mHeight);
                break;
            case 3:
            case 4:
            case 8:
            case 9:
                if (this.mThumbnail != null) {
                    Bitmap hwBitmap = this.mThumbnail.copy(Config.HARDWARE, false);
                    if (hwBitmap != null) {
                        b.putParcelable(KEY_ANIM_THUMBNAIL, hwBitmap.createGraphicBufferHandle());
                    } else {
                        Slog.w(TAG, "Failed to copy thumbnail");
                    }
                }
                b.putInt(KEY_ANIM_START_X, this.mStartX);
                b.putInt(KEY_ANIM_START_Y, this.mStartY);
                b.putInt(KEY_ANIM_WIDTH, this.mWidth);
                b.putInt(KEY_ANIM_HEIGHT, this.mHeight);
                str = KEY_ANIM_START_LISTENER;
                if (this.mAnimationStartedListener != null) {
                    iBinder = this.mAnimationStartedListener.asBinder();
                }
                b.putBinder(str, iBinder);
                break;
            case 5:
                if (this.mTransitionReceiver != null) {
                    b.putParcelable(KEY_TRANSITION_COMPLETE_LISTENER, this.mTransitionReceiver);
                }
                b.putBoolean(KEY_TRANSITION_IS_RETURNING, this.mIsReturning);
                b.putStringArrayList(KEY_TRANSITION_SHARED_ELEMENTS, this.mSharedElementNames);
                b.putParcelable(KEY_RESULT_DATA, this.mResultData);
                b.putInt(KEY_RESULT_CODE, this.mResultCode);
                b.putInt(KEY_EXIT_COORDINATOR_INDEX, this.mExitCoordinatorIndex);
                break;
            case 10:
                b.putInt(KEY_ANIM_IN_PLACE_RES_ID, this.mCustomInPlaceResId);
                break;
        }
        b.putInt(KEY_LAUNCH_DISPLAY_ID, this.mLaunchDisplayId);
        b.putInt(KEY_LAUNCH_STACK_ID, this.mLaunchStackId);
        b.putInt(KEY_LAUNCH_TASK_ID, this.mLaunchTaskId);
        b.putBoolean(KEY_TASK_OVERLAY, this.mTaskOverlay);
        b.putBoolean(KEY_TASK_OVERLAY_CAN_RESUME, this.mTaskOverlayCanResume);
        b.putInt(KEY_DOCK_CREATE_MODE, this.mDockCreateMode);
        if (this.mAnimSpecs != null) {
            b.putParcelableArray(KEY_ANIM_SPECS, this.mAnimSpecs);
        }
        if (this.mAnimationFinishedListener != null) {
            b.putBinder(KEY_ANIMATION_FINISHED_LISTENER, this.mAnimationFinishedListener.asBinder());
        }
        if (this.mSpecsFuture != null) {
            b.putBinder(KEY_SPECS_FUTURE, this.mSpecsFuture.asBinder());
        }
        b.putInt(KEY_ROTATION_ANIMATION_HINT, this.mRotationAnimationHint);
        if (this.mAppVerificationBundle != null) {
            b.putBundle(KEY_INSTANT_APP_VERIFICATION_BUNDLE, this.mAppVerificationBundle);
        }
        return b;
    }

    public void requestUsageTimeReport(PendingIntent receiver) {
        this.mUsageTimeReport = receiver;
    }

    public ActivityOptions forTargetActivity() {
        if (this.mAnimationType != 5) {
            return null;
        }
        ActivityOptions result = new ActivityOptions();
        result.update(this);
        return result;
    }

    public int getRotationAnimationHint() {
        return this.mRotationAnimationHint;
    }

    public void setRotationAnimationHint(int hint) {
        this.mRotationAnimationHint = hint;
    }

    public Bundle popAppVerificationBundle() {
        Bundle out = this.mAppVerificationBundle;
        this.mAppVerificationBundle = null;
        return out;
    }

    public ActivityOptions setAppVerificationBundle(Bundle bundle) {
        this.mAppVerificationBundle = bundle;
        return this;
    }

    public String toString() {
        return "ActivityOptions(" + hashCode() + "), mPackageName=" + this.mPackageName + ", mAnimationType=" + this.mAnimationType + ", mStartX=" + this.mStartX + ", mStartY=" + this.mStartY + ", mWidth=" + this.mWidth + ", mHeight=" + this.mHeight;
    }
}
