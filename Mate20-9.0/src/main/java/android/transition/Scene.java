package android.transition;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class Scene {
    private Context mContext;
    Runnable mEnterAction;
    Runnable mExitAction;
    private View mLayout;
    private int mLayoutId = -1;
    private ViewGroup mSceneRoot;

    public static Scene getSceneForLayout(ViewGroup sceneRoot, int layoutId, Context context) {
        SparseArray<Scene> scenes = (SparseArray) sceneRoot.getTag(16909275);
        if (scenes == null) {
            scenes = new SparseArray<>();
            sceneRoot.setTagInternal(16909275, scenes);
        }
        Scene scene = scenes.get(layoutId);
        if (scene != null) {
            return scene;
        }
        Scene scene2 = new Scene(sceneRoot, layoutId, context);
        scenes.put(layoutId, scene2);
        return scene2;
    }

    public Scene(ViewGroup sceneRoot) {
        this.mSceneRoot = sceneRoot;
    }

    private Scene(ViewGroup sceneRoot, int layoutId, Context context) {
        this.mContext = context;
        this.mSceneRoot = sceneRoot;
        this.mLayoutId = layoutId;
    }

    public Scene(ViewGroup sceneRoot, View layout) {
        this.mSceneRoot = sceneRoot;
        this.mLayout = layout;
    }

    @Deprecated
    public Scene(ViewGroup sceneRoot, ViewGroup layout) {
        this.mSceneRoot = sceneRoot;
        this.mLayout = layout;
    }

    public ViewGroup getSceneRoot() {
        return this.mSceneRoot;
    }

    public void exit() {
        if (getCurrentScene(this.mSceneRoot) == this && this.mExitAction != null) {
            this.mExitAction.run();
        }
    }

    public void enter() {
        if (this.mLayoutId > 0 || this.mLayout != null) {
            getSceneRoot().removeAllViews();
            if (this.mLayoutId > 0) {
                LayoutInflater.from(this.mContext).inflate(this.mLayoutId, this.mSceneRoot);
            } else {
                this.mSceneRoot.addView(this.mLayout);
            }
        }
        if (this.mEnterAction != null) {
            this.mEnterAction.run();
        }
        setCurrentScene(this.mSceneRoot, this);
    }

    static void setCurrentScene(View view, Scene scene) {
        view.setTagInternal(16908838, scene);
    }

    static Scene getCurrentScene(View view) {
        return (Scene) view.getTag(16908838);
    }

    public void setEnterAction(Runnable action) {
        this.mEnterAction = action;
    }

    public void setExitAction(Runnable action) {
        this.mExitAction = action;
    }

    /* access modifiers changed from: package-private */
    public boolean isCreatedFromLayoutResource() {
        return this.mLayoutId > 0;
    }
}
