package android.transition;

import android.transition.Transition.TransitionListenerAdapter;
import android.util.ArrayMap;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class TransitionManager {
    private static final String[] EMPTY_STRINGS = null;
    private static String LOG_TAG;
    private static Transition sDefaultTransition;
    private static ArrayList<ViewGroup> sPendingTransitions;
    private static ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>> sRunningTransitions;
    ArrayMap<Scene, ArrayMap<Scene, Transition>> mScenePairTransitions;
    ArrayMap<Scene, Transition> mSceneTransitions;

    private static class MultiListener implements OnPreDrawListener, OnAttachStateChangeListener {
        ViewGroup mSceneRoot;
        Transition mTransition;

        /* renamed from: android.transition.TransitionManager.MultiListener.1 */
        class AnonymousClass1 extends TransitionListenerAdapter {
            final /* synthetic */ ArrayMap val$runningTransitions;

            AnonymousClass1(ArrayMap val$runningTransitions) {
                this.val$runningTransitions = val$runningTransitions;
            }

            public void onTransitionEnd(Transition transition) {
                ((ArrayList) this.val$runningTransitions.get(MultiListener.this.mSceneRoot)).remove(transition);
            }
        }

        MultiListener(Transition transition, ViewGroup sceneRoot) {
            this.mTransition = transition;
            this.mSceneRoot = sceneRoot;
        }

        private void removeListeners() {
            this.mSceneRoot.getViewTreeObserver().removeOnPreDrawListener(this);
            this.mSceneRoot.removeOnAttachStateChangeListener(this);
        }

        public void onViewAttachedToWindow(View v) {
        }

        public void onViewDetachedFromWindow(View v) {
            removeListeners();
            TransitionManager.sPendingTransitions.remove(this.mSceneRoot);
            ArrayList<Transition> runningTransitions = (ArrayList) TransitionManager.getRunningTransitions().get(this.mSceneRoot);
            if (runningTransitions != null && runningTransitions.size() > 0) {
                for (Transition runningTransition : runningTransitions) {
                    runningTransition.resume(this.mSceneRoot);
                }
            }
            this.mTransition.clearValues(true);
        }

        public boolean onPreDraw() {
            removeListeners();
            if (!TransitionManager.sPendingTransitions.remove(this.mSceneRoot)) {
                return true;
            }
            ArrayMap<ViewGroup, ArrayList<Transition>> runningTransitions = TransitionManager.getRunningTransitions();
            ArrayList<Transition> currentTransitions = (ArrayList) runningTransitions.get(this.mSceneRoot);
            Iterable previousRunningTransitions = null;
            if (currentTransitions == null) {
                currentTransitions = new ArrayList();
                runningTransitions.put(this.mSceneRoot, currentTransitions);
            } else if (currentTransitions.size() > 0) {
                previousRunningTransitions = new ArrayList(currentTransitions);
            }
            currentTransitions.add(this.mTransition);
            this.mTransition.addListener(new AnonymousClass1(runningTransitions));
            this.mTransition.captureValues(this.mSceneRoot, false);
            if (r1 != null) {
                for (Transition runningTransition : r1) {
                    runningTransition.resume(this.mSceneRoot);
                }
            }
            this.mTransition.playTransition(this.mSceneRoot);
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.transition.TransitionManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.transition.TransitionManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.transition.TransitionManager.<clinit>():void");
    }

    public TransitionManager() {
        this.mSceneTransitions = new ArrayMap();
        this.mScenePairTransitions = new ArrayMap();
    }

    public void setDefaultTransition(Transition transition) {
        sDefaultTransition = transition;
    }

    public static Transition getDefaultTransition() {
        return sDefaultTransition;
    }

    public void setTransition(Scene scene, Transition transition) {
        this.mSceneTransitions.put(scene, transition);
    }

    public void setTransition(Scene fromScene, Scene toScene, Transition transition) {
        ArrayMap<Scene, Transition> sceneTransitionMap = (ArrayMap) this.mScenePairTransitions.get(toScene);
        if (sceneTransitionMap == null) {
            sceneTransitionMap = new ArrayMap();
            this.mScenePairTransitions.put(toScene, sceneTransitionMap);
        }
        sceneTransitionMap.put(fromScene, transition);
    }

    private Transition getTransition(Scene scene) {
        Transition transition;
        ViewGroup sceneRoot = scene.getSceneRoot();
        if (sceneRoot != null) {
            Scene currScene = Scene.getCurrentScene(sceneRoot);
            if (currScene != null) {
                ArrayMap<Scene, Transition> sceneTransitionMap = (ArrayMap) this.mScenePairTransitions.get(scene);
                if (sceneTransitionMap != null) {
                    transition = (Transition) sceneTransitionMap.get(currScene);
                    if (transition != null) {
                        return transition;
                    }
                }
            }
        }
        transition = (Transition) this.mSceneTransitions.get(scene);
        if (transition == null) {
            transition = sDefaultTransition;
        }
        return transition;
    }

    private static void changeScene(Scene scene, Transition transition) {
        ViewGroup sceneRoot = scene.getSceneRoot();
        if (!sPendingTransitions.contains(sceneRoot)) {
            sPendingTransitions.add(sceneRoot);
            Transition transition2 = null;
            if (transition != null) {
                transition2 = transition.clone();
                transition2.setSceneRoot(sceneRoot);
            }
            Scene oldScene = Scene.getCurrentScene(sceneRoot);
            if (!(oldScene == null || transition2 == null || !oldScene.isCreatedFromLayoutResource())) {
                transition2.setCanRemoveViews(true);
            }
            sceneChangeSetup(sceneRoot, transition2);
            scene.enter();
            sceneChangeRunTransition(sceneRoot, transition2);
        }
    }

    private static ArrayMap<ViewGroup, ArrayList<Transition>> getRunningTransitions() {
        WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>> runningTransitions = (WeakReference) sRunningTransitions.get();
        if (runningTransitions == null || runningTransitions.get() == null) {
            runningTransitions = new WeakReference(new ArrayMap());
            sRunningTransitions.set(runningTransitions);
        }
        return (ArrayMap) runningTransitions.get();
    }

    private static void sceneChangeRunTransition(ViewGroup sceneRoot, Transition transition) {
        if (transition != null && sceneRoot != null) {
            MultiListener listener = new MultiListener(transition, sceneRoot);
            sceneRoot.addOnAttachStateChangeListener(listener);
            sceneRoot.getViewTreeObserver().addOnPreDrawListener(listener);
        }
    }

    private static void sceneChangeSetup(ViewGroup sceneRoot, Transition transition) {
        ArrayList<Transition> runningTransitions = (ArrayList) getRunningTransitions().get(sceneRoot);
        if (runningTransitions != null && runningTransitions.size() > 0) {
            for (Transition runningTransition : runningTransitions) {
                runningTransition.pause(sceneRoot);
            }
        }
        if (transition != null) {
            transition.captureValues(sceneRoot, true);
        }
        Scene previousScene = Scene.getCurrentScene(sceneRoot);
        if (previousScene != null) {
            previousScene.exit();
        }
    }

    public void transitionTo(Scene scene) {
        changeScene(scene, getTransition(scene));
    }

    public static void go(Scene scene) {
        changeScene(scene, sDefaultTransition);
    }

    public static void go(Scene scene, Transition transition) {
        changeScene(scene, transition);
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot) {
        beginDelayedTransition(sceneRoot, null);
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot, Transition transition) {
        if (!sPendingTransitions.contains(sceneRoot) && sceneRoot.isLaidOut()) {
            sPendingTransitions.add(sceneRoot);
            if (transition == null) {
                transition = sDefaultTransition;
            }
            Transition transitionClone = transition.clone();
            sceneChangeSetup(sceneRoot, transitionClone);
            Scene.setCurrentScene(sceneRoot, null);
            sceneChangeRunTransition(sceneRoot, transitionClone);
        }
    }

    public static void endTransitions(ViewGroup sceneRoot) {
        sPendingTransitions.remove(sceneRoot);
        ArrayList<Transition> runningTransitions = (ArrayList) getRunningTransitions().get(sceneRoot);
        if (runningTransitions != null && !runningTransitions.isEmpty()) {
            ArrayList<Transition> copy = new ArrayList(runningTransitions);
            for (int i = copy.size() - 1; i >= 0; i--) {
                ((Transition) copy.get(i)).end();
            }
        }
    }
}
