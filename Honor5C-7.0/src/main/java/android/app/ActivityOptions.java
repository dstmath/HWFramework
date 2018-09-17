package android.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.IRemoteCallback.Stub;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Pair;
import android.util.Slog;
import android.view.AppTransitionAnimationSpec;
import android.view.View;
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
    public static final String KEY_LAUNCH_BOUNDS = "android:activity.launchBounds";
    private static final String KEY_LAUNCH_STACK_ID = "android.activity.launchStackId";
    private static final String KEY_LAUNCH_TASK_ID = "android.activity.launchTaskId";
    public static final String KEY_PACKAGE_NAME = "android:activity.packageName";
    private static final String KEY_RESULT_CODE = "android:activity.resultCode";
    private static final String KEY_RESULT_DATA = "android:activity.resultData";
    private static final String KEY_TASK_OVERLAY = "android.activity.taskOverlay";
    private static final String KEY_TRANSITION_COMPLETE_LISTENER = "android:activity.transitionCompleteListener";
    private static final String KEY_TRANSITION_IS_RETURNING = "android:activity.transitionIsReturning";
    private static final String KEY_TRANSITION_SHARED_ELEMENTS = "android:activity.sharedElementNames";
    private static final String KEY_USAGE_TIME_REPORT = "android:activity.usageTimeReport";
    private static final String TAG = "ActivityOptions";
    private AppTransitionAnimationSpec[] mAnimSpecs;
    private IRemoteCallback mAnimationFinishedListener;
    private IRemoteCallback mAnimationStartedListener;
    private int mAnimationType;
    private int mCustomEnterResId;
    private int mCustomExitResId;
    private int mCustomInPlaceResId;
    private int mDockCreateMode;
    private int mExitCoordinatorIndex;
    private int mHeight;
    private boolean mIsReturning;
    private Rect mLaunchBounds;
    private int mLaunchStackId;
    private int mLaunchTaskId;
    private String mPackageName;
    private int mResultCode;
    private Intent mResultData;
    private ArrayList<String> mSharedElementNames;
    private int mStartX;
    private int mStartY;
    private boolean mTaskOverlay;
    private Bitmap mThumbnail;
    private ResultReceiver mTransitionReceiver;
    private PendingIntent mUsageTimeReport;
    private int mWidth;

    /* renamed from: android.app.ActivityOptions.1 */
    class AnonymousClass1 extends Stub {
        final /* synthetic */ Handler val$handler;
        final /* synthetic */ OnAnimationStartedListener val$listener;

        /* renamed from: android.app.ActivityOptions.1.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ OnAnimationStartedListener val$listener;

            AnonymousClass1(OnAnimationStartedListener val$listener) {
                this.val$listener = val$listener;
            }

            public void run() {
                this.val$listener.onAnimationStarted();
            }
        }

        AnonymousClass1(Handler val$handler, OnAnimationStartedListener val$listener) {
            this.val$handler = val$handler;
            this.val$listener = val$listener;
        }

        public void sendResult(Bundle data) throws RemoteException {
            this.val$handler.post(new AnonymousClass1(this.val$listener));
        }
    }

    /* renamed from: android.app.ActivityOptions.2 */
    class AnonymousClass2 extends Stub {
        final /* synthetic */ Handler val$handler;
        final /* synthetic */ OnAnimationFinishedListener val$listener;

        /* renamed from: android.app.ActivityOptions.2.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ OnAnimationFinishedListener val$listener;

            AnonymousClass1(OnAnimationFinishedListener val$listener) {
                this.val$listener = val$listener;
            }

            public void run() {
                this.val$listener.onAnimationFinished();
            }
        }

        AnonymousClass2(Handler val$handler, OnAnimationFinishedListener val$listener) {
            this.val$handler = val$handler;
            this.val$listener = val$listener;
        }

        public void sendResult(Bundle data) throws RemoteException {
            this.val$handler.post(new AnonymousClass1(this.val$listener));
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
        opts.mAnimationType = ANIM_CUSTOM;
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
        opts.mAnimationType = ANIM_CUSTOM_IN_PLACE;
        opts.mCustomInPlaceResId = animId;
        return opts;
    }

    private void setOnAnimationStartedListener(Handler handler, OnAnimationStartedListener listener) {
        if (listener != null) {
            this.mAnimationStartedListener = new AnonymousClass1(handler, listener);
        }
    }

    private void setOnAnimationFinishedListener(Handler handler, OnAnimationFinishedListener listener) {
        if (listener != null) {
            this.mAnimationFinishedListener = new AnonymousClass2(handler, listener);
        }
    }

    public static ActivityOptions makeScaleUpAnimation(View source, int startX, int startY, int width, int height) {
        ActivityOptions opts = new ActivityOptions();
        opts.mPackageName = source.getContext().getPackageName();
        opts.mAnimationType = ANIM_SCALE_UP;
        int[] pts = new int[ANIM_SCALE_UP];
        source.getLocationOnScreen(pts);
        opts.mStartX = pts[ANIM_NONE] + startX;
        opts.mStartY = pts[ANIM_CUSTOM] + startY;
        opts.mWidth = width;
        opts.mHeight = height;
        return opts;
    }

    public static ActivityOptions makeClipRevealAnimation(View source, int startX, int startY, int width, int height) {
        ActivityOptions opts = new ActivityOptions();
        opts.mAnimationType = ANIM_CLIP_REVEAL;
        int[] pts = new int[ANIM_SCALE_UP];
        source.getLocationOnScreen(pts);
        opts.mStartX = pts[ANIM_NONE] + startX;
        opts.mStartY = pts[ANIM_CUSTOM] + startY;
        opts.mWidth = width;
        opts.mHeight = height;
        return opts;
    }

    public static ActivityOptions makeThumbnailScaleUpAnimation(View source, Bitmap thumbnail, int startX, int startY) {
        return makeThumbnailScaleUpAnimation(source, thumbnail, startX, startY, null);
    }

    public static ActivityOptions makeThumbnailScaleUpAnimation(View source, Bitmap thumbnail, int startX, int startY, OnAnimationStartedListener listener) {
        return makeThumbnailAnimation(source, thumbnail, startX, startY, listener, true);
    }

    public static ActivityOptions makeThumbnailScaleDownAnimation(View source, Bitmap thumbnail, int startX, int startY, OnAnimationStartedListener listener) {
        return makeThumbnailAnimation(source, thumbnail, startX, startY, listener, false);
    }

    private static ActivityOptions makeThumbnailAnimation(View source, Bitmap thumbnail, int startX, int startY, OnAnimationStartedListener listener, boolean scaleUp) {
        ActivityOptions opts = new ActivityOptions();
        opts.mPackageName = source.getContext().getPackageName();
        opts.mAnimationType = scaleUp ? ANIM_THUMBNAIL_SCALE_UP : ANIM_THUMBNAIL_SCALE_DOWN;
        opts.mThumbnail = thumbnail;
        int[] pts = new int[ANIM_SCALE_UP];
        source.getLocationOnScreen(pts);
        opts.mStartX = pts[ANIM_NONE] + startX;
        opts.mStartY = pts[ANIM_CUSTOM] + startY;
        opts.setOnAnimationStartedListener(source.getHandler(), listener);
        return opts;
    }

    public static ActivityOptions makeThumbnailAspectScaleUpAnimation(View source, Bitmap thumbnail, int startX, int startY, int targetWidth, int targetHeight, Handler handler, OnAnimationStartedListener listener) {
        return makeAspectScaledThumbnailAnimation(source, thumbnail, startX, startY, targetWidth, targetHeight, handler, listener, true);
    }

    public static ActivityOptions makeThumbnailAspectScaleDownAnimation(View source, Bitmap thumbnail, int startX, int startY, int targetWidth, int targetHeight, Handler handler, OnAnimationStartedListener listener) {
        return makeAspectScaledThumbnailAnimation(source, thumbnail, startX, startY, targetWidth, targetHeight, handler, listener, false);
    }

    private static ActivityOptions makeAspectScaledThumbnailAnimation(View source, Bitmap thumbnail, int startX, int startY, int targetWidth, int targetHeight, Handler handler, OnAnimationStartedListener listener, boolean scaleUp) {
        int i;
        ActivityOptions opts = new ActivityOptions();
        opts.mPackageName = source.getContext().getPackageName();
        if (scaleUp) {
            i = ANIM_THUMBNAIL_ASPECT_SCALE_UP;
        } else {
            i = ANIM_THUMBNAIL_ASPECT_SCALE_DOWN;
        }
        opts.mAnimationType = i;
        opts.mThumbnail = thumbnail;
        int[] pts = new int[ANIM_SCALE_UP];
        source.getLocationOnScreen(pts);
        opts.mStartX = pts[ANIM_NONE] + startX;
        opts.mStartY = pts[ANIM_CUSTOM] + startY;
        opts.mWidth = targetWidth;
        opts.mHeight = targetHeight;
        opts.setOnAnimationStartedListener(handler, listener);
        return opts;
    }

    public static ActivityOptions makeThumbnailAspectScaleDownAnimation(View source, AppTransitionAnimationSpec[] specs, Handler handler, OnAnimationStartedListener onAnimationStartedListener, OnAnimationFinishedListener onAnimationFinishedListener) {
        ActivityOptions opts = new ActivityOptions();
        opts.mPackageName = source.getContext().getPackageName();
        opts.mAnimationType = ANIM_THUMBNAIL_ASPECT_SCALE_DOWN;
        opts.mAnimSpecs = specs;
        opts.setOnAnimationStartedListener(handler, onAnimationStartedListener);
        opts.setOnAnimationFinishedListener(handler, onAnimationFinishedListener);
        return opts;
    }

    public static ActivityOptions makeSceneTransitionAnimation(Activity activity, View sharedElement, String sharedElementName) {
        Pair[] pairArr = new Pair[ANIM_CUSTOM];
        pairArr[ANIM_NONE] = Pair.create(sharedElement, sharedElementName);
        return makeSceneTransitionAnimation(activity, pairArr);
    }

    @SafeVarargs
    public static ActivityOptions makeSceneTransitionAnimation(Activity activity, Pair<View, String>... sharedElements) {
        ActivityOptions opts = new ActivityOptions();
        if (activity.getWindow().hasFeature(13)) {
            opts.mAnimationType = ANIM_SCENE_TRANSITION;
            ArrayList<String> names = new ArrayList();
            ArrayList<View> views = new ArrayList();
            if (sharedElements != null) {
                for (int i = ANIM_NONE; i < sharedElements.length; i += ANIM_CUSTOM) {
                    Pair<View, String> sharedElement = sharedElements[i];
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
            ExitTransitionCoordinator exit = new ExitTransitionCoordinator(activity, names, names, views, false);
            opts.mTransitionReceiver = exit;
            opts.mSharedElementNames = names;
            opts.mIsReturning = false;
            opts.mExitCoordinatorIndex = activity.mActivityTransitionState.addExitTransitionCoordinator(exit);
            return opts;
        }
        opts.mAnimationType = ANIM_DEFAULT;
        return opts;
    }

    public static ActivityOptions makeSceneTransitionAnimation(Activity activity, ExitTransitionCoordinator exitCoordinator, ArrayList<String> sharedElementNames, int resultCode, Intent resultData) {
        ActivityOptions opts = new ActivityOptions();
        opts.mAnimationType = ANIM_SCENE_TRANSITION;
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
        opts.mAnimationType = ANIM_LAUNCH_TASK_BEHIND;
        return opts;
    }

    public static ActivityOptions makeBasic() {
        return new ActivityOptions();
    }

    public boolean getLaunchTaskBehind() {
        return this.mAnimationType == ANIM_LAUNCH_TASK_BEHIND;
    }

    private ActivityOptions() {
        this.mAnimationType = ANIM_NONE;
        this.mLaunchStackId = -1;
        this.mLaunchTaskId = -1;
        this.mDockCreateMode = ANIM_NONE;
    }

    public ActivityOptions(Bundle opts) {
        this.mAnimationType = ANIM_NONE;
        this.mLaunchStackId = -1;
        this.mLaunchTaskId = -1;
        this.mDockCreateMode = ANIM_NONE;
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
            case ANIM_CUSTOM /*1*/:
                this.mCustomEnterResId = opts.getInt(KEY_ANIM_ENTER_RES_ID, ANIM_NONE);
                this.mCustomExitResId = opts.getInt(KEY_ANIM_EXIT_RES_ID, ANIM_NONE);
                this.mAnimationStartedListener = Stub.asInterface(opts.getBinder(KEY_ANIM_START_LISTENER));
                break;
            case ANIM_SCALE_UP /*2*/:
            case ANIM_CLIP_REVEAL /*11*/:
                this.mStartX = opts.getInt(KEY_ANIM_START_X, ANIM_NONE);
                this.mStartY = opts.getInt(KEY_ANIM_START_Y, ANIM_NONE);
                this.mWidth = opts.getInt(KEY_ANIM_WIDTH, ANIM_NONE);
                this.mHeight = opts.getInt(KEY_ANIM_HEIGHT, ANIM_NONE);
                break;
            case ANIM_THUMBNAIL_SCALE_UP /*3*/:
            case ANIM_THUMBNAIL_SCALE_DOWN /*4*/:
            case ANIM_THUMBNAIL_ASPECT_SCALE_UP /*8*/:
            case ANIM_THUMBNAIL_ASPECT_SCALE_DOWN /*9*/:
                this.mThumbnail = (Bitmap) opts.getParcelable(KEY_ANIM_THUMBNAIL);
                this.mStartX = opts.getInt(KEY_ANIM_START_X, ANIM_NONE);
                this.mStartY = opts.getInt(KEY_ANIM_START_Y, ANIM_NONE);
                this.mWidth = opts.getInt(KEY_ANIM_WIDTH, ANIM_NONE);
                this.mHeight = opts.getInt(KEY_ANIM_HEIGHT, ANIM_NONE);
                this.mAnimationStartedListener = Stub.asInterface(opts.getBinder(KEY_ANIM_START_LISTENER));
                break;
            case ANIM_SCENE_TRANSITION /*5*/:
                this.mTransitionReceiver = (ResultReceiver) opts.getParcelable(KEY_TRANSITION_COMPLETE_LISTENER);
                this.mIsReturning = opts.getBoolean(KEY_TRANSITION_IS_RETURNING, false);
                this.mSharedElementNames = opts.getStringArrayList(KEY_TRANSITION_SHARED_ELEMENTS);
                this.mResultData = (Intent) opts.getParcelable(KEY_RESULT_DATA);
                this.mResultCode = opts.getInt(KEY_RESULT_CODE);
                this.mExitCoordinatorIndex = opts.getInt(KEY_EXIT_COORDINATOR_INDEX);
                break;
            case ANIM_CUSTOM_IN_PLACE /*10*/:
                this.mCustomInPlaceResId = opts.getInt(KEY_ANIM_IN_PLACE_RES_ID, ANIM_NONE);
                break;
        }
        this.mLaunchStackId = opts.getInt(KEY_LAUNCH_STACK_ID, -1);
        this.mLaunchTaskId = opts.getInt(KEY_LAUNCH_TASK_ID, -1);
        this.mTaskOverlay = opts.getBoolean(KEY_TASK_OVERLAY, false);
        this.mDockCreateMode = opts.getInt(KEY_DOCK_CREATE_MODE, ANIM_NONE);
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

    public Bitmap getThumbnail() {
        return this.mThumbnail;
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

    public static ActivityOptions fromBundle(Bundle bOptions) {
        return bOptions != null ? new ActivityOptions(bOptions) : null;
    }

    public static void abort(ActivityOptions options) {
        if (options != null) {
            options.abort();
        }
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

    public void setTaskOverlay(boolean taskOverlay) {
        this.mTaskOverlay = taskOverlay;
    }

    public boolean getTaskOverlay() {
        return this.mTaskOverlay;
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
        this.mResultCode = ANIM_NONE;
        this.mExitCoordinatorIndex = ANIM_NONE;
        this.mAnimationType = otherOptions.mAnimationType;
        switch (otherOptions.mAnimationType) {
            case ANIM_CUSTOM /*1*/:
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
            case ANIM_SCALE_UP /*2*/:
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
            case ANIM_THUMBNAIL_SCALE_UP /*3*/:
            case ANIM_THUMBNAIL_SCALE_DOWN /*4*/:
            case ANIM_THUMBNAIL_ASPECT_SCALE_UP /*8*/:
            case ANIM_THUMBNAIL_ASPECT_SCALE_DOWN /*9*/:
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
            case ANIM_SCENE_TRANSITION /*5*/:
                this.mTransitionReceiver = otherOptions.mTransitionReceiver;
                this.mSharedElementNames = otherOptions.mSharedElementNames;
                this.mIsReturning = otherOptions.mIsReturning;
                this.mThumbnail = null;
                this.mAnimationStartedListener = null;
                this.mResultData = otherOptions.mResultData;
                this.mResultCode = otherOptions.mResultCode;
                this.mExitCoordinatorIndex = otherOptions.mExitCoordinatorIndex;
                break;
            case ANIM_CUSTOM_IN_PLACE /*10*/:
                this.mCustomInPlaceResId = otherOptions.mCustomInPlaceResId;
                break;
        }
        this.mAnimSpecs = otherOptions.mAnimSpecs;
        this.mAnimationFinishedListener = otherOptions.mAnimationFinishedListener;
    }

    public Bundle toBundle() {
        IBinder iBinder = null;
        if (this.mAnimationType == ANIM_DEFAULT) {
            return null;
        }
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
            case ANIM_CUSTOM /*1*/:
                b.putInt(KEY_ANIM_ENTER_RES_ID, this.mCustomEnterResId);
                b.putInt(KEY_ANIM_EXIT_RES_ID, this.mCustomExitResId);
                str = KEY_ANIM_START_LISTENER;
                if (this.mAnimationStartedListener != null) {
                    iBinder = this.mAnimationStartedListener.asBinder();
                }
                b.putBinder(str, iBinder);
                break;
            case ANIM_SCALE_UP /*2*/:
            case ANIM_CLIP_REVEAL /*11*/:
                b.putInt(KEY_ANIM_START_X, this.mStartX);
                b.putInt(KEY_ANIM_START_Y, this.mStartY);
                b.putInt(KEY_ANIM_WIDTH, this.mWidth);
                b.putInt(KEY_ANIM_HEIGHT, this.mHeight);
                break;
            case ANIM_THUMBNAIL_SCALE_UP /*3*/:
            case ANIM_THUMBNAIL_SCALE_DOWN /*4*/:
            case ANIM_THUMBNAIL_ASPECT_SCALE_UP /*8*/:
            case ANIM_THUMBNAIL_ASPECT_SCALE_DOWN /*9*/:
                b.putParcelable(KEY_ANIM_THUMBNAIL, this.mThumbnail);
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
            case ANIM_SCENE_TRANSITION /*5*/:
                if (this.mTransitionReceiver != null) {
                    b.putParcelable(KEY_TRANSITION_COMPLETE_LISTENER, this.mTransitionReceiver);
                }
                b.putBoolean(KEY_TRANSITION_IS_RETURNING, this.mIsReturning);
                b.putStringArrayList(KEY_TRANSITION_SHARED_ELEMENTS, this.mSharedElementNames);
                b.putParcelable(KEY_RESULT_DATA, this.mResultData);
                b.putInt(KEY_RESULT_CODE, this.mResultCode);
                b.putInt(KEY_EXIT_COORDINATOR_INDEX, this.mExitCoordinatorIndex);
                break;
            case ANIM_CUSTOM_IN_PLACE /*10*/:
                b.putInt(KEY_ANIM_IN_PLACE_RES_ID, this.mCustomInPlaceResId);
                break;
        }
        b.putInt(KEY_LAUNCH_STACK_ID, this.mLaunchStackId);
        b.putInt(KEY_LAUNCH_TASK_ID, this.mLaunchTaskId);
        b.putBoolean(KEY_TASK_OVERLAY, this.mTaskOverlay);
        b.putInt(KEY_DOCK_CREATE_MODE, this.mDockCreateMode);
        if (this.mAnimSpecs != null) {
            b.putParcelableArray(KEY_ANIM_SPECS, this.mAnimSpecs);
        }
        if (this.mAnimationFinishedListener != null) {
            b.putBinder(KEY_ANIMATION_FINISHED_LISTENER, this.mAnimationFinishedListener.asBinder());
        }
        return b;
    }

    public void requestUsageTimeReport(PendingIntent receiver) {
        this.mUsageTimeReport = receiver;
    }

    public ActivityOptions forTargetActivity() {
        if (this.mAnimationType != ANIM_SCENE_TRANSITION) {
            return null;
        }
        ActivityOptions result = new ActivityOptions();
        result.update(this);
        return result;
    }

    public String toString() {
        return "ActivityOptions(" + hashCode() + "), mPackageName=" + this.mPackageName + ", mAnimationType=" + this.mAnimationType + ", mStartX=" + this.mStartX + ", mStartY=" + this.mStartY + ", mWidth=" + this.mWidth + ", mHeight=" + this.mHeight;
    }
}
