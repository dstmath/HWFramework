package android.transition;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;

public final class Scene {
    private Context mContext;
    Runnable mEnterAction;
    Runnable mExitAction;
    private View mLayout;
    private int mLayoutId = -1;
    private ViewGroup mSceneRoot;

    public static Scene getSceneForLayout(ViewGroup sceneRoot, int layoutId, Context context) {
        SparseArray<Scene> scenes = (SparseArray) sceneRoot.getTag(R.id.scene_layoutid_cache);
        if (scenes == null) {
            scenes = new SparseArray();
            sceneRoot.setTagInternal(R.id.scene_layoutid_cache, scenes);
        }
        Scene scene = (Scene) scenes.get(layoutId);
        if (scene != null) {
            return scene;
        }
        scene = new Scene(sceneRoot, layoutId, context);
        scenes.put(layoutId, scene);
        return scene;
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
        view.setTagInternal(R.id.current_scene, scene);
    }

    static Scene getCurrentScene(View view) {
        return (Scene) view.getTag(R.id.current_scene);
    }

    public void setEnterAction(Runnable action) {
        this.mEnterAction = action;
    }

    public void setExitAction(Runnable action) {
        this.mExitAction = action;
    }

    boolean isCreatedFromLayoutResource() {
        return this.mLayoutId > 0;
    }
}
