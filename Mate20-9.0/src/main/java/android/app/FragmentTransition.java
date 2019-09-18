package android.app;

import android.app.BackStackRecord;
import android.app.FragmentTransition;
import android.graphics.Rect;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.ArrayMap;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.view.OneShotPreDrawListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class FragmentTransition {
    private static final int[] INVERSE_OPS = {0, 3, 0, 1, 5, 4, 7, 6, 9, 8};

    public static class FragmentContainerTransition {
        public Fragment firstOut;
        public boolean firstOutIsPop;
        public BackStackRecord firstOutTransaction;
        public Fragment lastIn;
        public boolean lastInIsPop;
        public BackStackRecord lastInTransaction;
    }

    FragmentTransition() {
    }

    static void startTransitions(FragmentManagerImpl fragmentManager, ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, int startIndex, int endIndex, boolean isReordered) {
        if (fragmentManager.mCurState >= 1) {
            SparseArray<FragmentContainerTransition> transitioningFragments = new SparseArray<>();
            for (int i = startIndex; i < endIndex; i++) {
                BackStackRecord record = records.get(i);
                if (isRecordPop.get(i).booleanValue()) {
                    calculatePopFragments(record, transitioningFragments, isReordered);
                } else {
                    calculateFragments(record, transitioningFragments, isReordered);
                }
            }
            if (transitioningFragments.size() != 0) {
                View nonExistentView = new View(fragmentManager.mHost.getContext());
                int numContainers = transitioningFragments.size();
                for (int i2 = 0; i2 < numContainers; i2++) {
                    int containerId = transitioningFragments.keyAt(i2);
                    ArrayMap<String, String> nameOverrides = calculateNameOverrides(containerId, records, isRecordPop, startIndex, endIndex);
                    FragmentContainerTransition containerTransition = transitioningFragments.valueAt(i2);
                    if (isReordered) {
                        configureTransitionsReordered(fragmentManager, containerId, containerTransition, nonExistentView, nameOverrides);
                    } else {
                        configureTransitionsOrdered(fragmentManager, containerId, containerTransition, nonExistentView, nameOverrides);
                    }
                }
            }
        }
    }

    private static ArrayMap<String, String> calculateNameOverrides(int containerId, ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, int startIndex, int endIndex) {
        ArrayList<String> sources;
        ArrayList<String> targets;
        ArrayMap<String, String> nameOverrides = new ArrayMap<>();
        for (int recordNum = endIndex - 1; recordNum >= startIndex; recordNum--) {
            BackStackRecord record = records.get(recordNum);
            if (record.interactsWith(containerId)) {
                boolean isPop = isRecordPop.get(recordNum).booleanValue();
                if (record.mSharedElementSourceNames != null) {
                    int numSharedElements = record.mSharedElementSourceNames.size();
                    if (isPop) {
                        targets = record.mSharedElementSourceNames;
                        sources = record.mSharedElementTargetNames;
                    } else {
                        sources = record.mSharedElementSourceNames;
                        targets = record.mSharedElementTargetNames;
                    }
                    for (int i = 0; i < numSharedElements; i++) {
                        String sourceName = sources.get(i);
                        String targetName = targets.get(i);
                        String previousTarget = nameOverrides.remove(targetName);
                        if (previousTarget != null) {
                            nameOverrides.put(sourceName, previousTarget);
                        } else {
                            nameOverrides.put(sourceName, targetName);
                        }
                    }
                }
            }
        }
        return nameOverrides;
    }

    /* JADX WARNING: type inference failed for: r2v5, types: [android.view.View] */
    /* JADX WARNING: Multi-variable type inference failed */
    private static void configureTransitionsReordered(FragmentManagerImpl fragmentManager, int containerId, FragmentContainerTransition fragments, View nonExistentView, ArrayMap<String, String> nameOverrides) {
        Transition exitTransition;
        FragmentManagerImpl fragmentManagerImpl = fragmentManager;
        FragmentContainerTransition fragmentContainerTransition = fragments;
        View view = nonExistentView;
        ViewGroup sceneRoot = null;
        if (fragmentManagerImpl.mContainer.onHasView()) {
            sceneRoot = fragmentManagerImpl.mContainer.onFindViewById(containerId);
        } else {
            int i = containerId;
        }
        ViewGroup sceneRoot2 = sceneRoot;
        if (sceneRoot2 != null) {
            Fragment inFragment = fragmentContainerTransition.lastIn;
            Fragment outFragment = fragmentContainerTransition.firstOut;
            boolean inIsPop = fragmentContainerTransition.lastInIsPop;
            boolean outIsPop = fragmentContainerTransition.firstOutIsPop;
            ArrayList<View> sharedElementsIn = new ArrayList<>();
            ArrayList<View> sharedElementsOut = new ArrayList<>();
            Transition enterTransition = getEnterTransition(inFragment, inIsPop);
            Transition exitTransition2 = getExitTransition(outFragment, outIsPop);
            Transition enterTransition2 = enterTransition;
            ArrayList<View> sharedElementsOut2 = sharedElementsOut;
            ArrayList<View> sharedElementsIn2 = sharedElementsIn;
            boolean z = outIsPop;
            TransitionSet sharedElementTransition = configureSharedElementsReordered(sceneRoot2, view, nameOverrides, fragmentContainerTransition, sharedElementsOut, sharedElementsIn, enterTransition2, exitTransition2);
            Transition enterTransition3 = enterTransition2;
            if (enterTransition3 == null && sharedElementTransition == null) {
                exitTransition = exitTransition2;
                if (exitTransition == null) {
                    return;
                }
            } else {
                exitTransition = exitTransition2;
            }
            ArrayList<View> exitingViews = configureEnteringExitingViews(exitTransition, outFragment, sharedElementsOut2, view);
            ArrayList<View> enteringViews = configureEnteringExitingViews(enterTransition3, inFragment, sharedElementsIn2, view);
            setViewVisibility(enteringViews, 4);
            Transition transition = mergeTransitions(enterTransition3, exitTransition, sharedElementTransition, inFragment, inIsPop);
            if (transition != null) {
                replaceHide(exitTransition, outFragment, exitingViews);
                transition.setNameOverrides(nameOverrides);
                scheduleRemoveTargets(transition, enterTransition3, enteringViews, exitTransition, exitingViews, sharedElementTransition, sharedElementsIn2);
                TransitionManager.beginDelayedTransition(sceneRoot2, transition);
                setViewVisibility(enteringViews, 0);
                if (sharedElementTransition != null) {
                    sharedElementTransition.getTargets().clear();
                    sharedElementTransition.getTargets().addAll(sharedElementsIn2);
                    replaceTargets(sharedElementTransition, sharedElementsOut2, sharedElementsIn2);
                }
            } else {
                ArrayMap<String, String> arrayMap = nameOverrides;
            }
        }
    }

    /* JADX WARNING: type inference failed for: r2v5, types: [android.view.View] */
    /* JADX WARNING: Multi-variable type inference failed */
    private static void configureTransitionsOrdered(FragmentManagerImpl fragmentManager, int containerId, FragmentContainerTransition fragments, View nonExistentView, ArrayMap<String, String> nameOverrides) {
        Transition exitTransition;
        FragmentManagerImpl fragmentManagerImpl = fragmentManager;
        FragmentContainerTransition fragmentContainerTransition = fragments;
        View view = nonExistentView;
        ViewGroup sceneRoot = null;
        if (fragmentManagerImpl.mContainer.onHasView()) {
            sceneRoot = fragmentManagerImpl.mContainer.onFindViewById(containerId);
        } else {
            int i = containerId;
        }
        ViewGroup sceneRoot2 = sceneRoot;
        if (sceneRoot2 != null) {
            Fragment inFragment = fragmentContainerTransition.lastIn;
            Fragment outFragment = fragmentContainerTransition.firstOut;
            boolean inIsPop = fragmentContainerTransition.lastInIsPop;
            boolean outIsPop = fragmentContainerTransition.firstOutIsPop;
            Transition enterTransition = getEnterTransition(inFragment, inIsPop);
            Transition exitTransition2 = getExitTransition(outFragment, outIsPop);
            ArrayList<View> sharedElementsOut = new ArrayList<>();
            ArrayList<View> sharedElementsIn = new ArrayList<>();
            ArrayList<View> sharedElementsOut2 = sharedElementsOut;
            Transition exitTransition3 = exitTransition2;
            boolean z = outIsPop;
            TransitionSet sharedElementTransition = configureSharedElementsOrdered(sceneRoot2, view, nameOverrides, fragmentContainerTransition, sharedElementsOut2, sharedElementsIn, enterTransition, exitTransition3);
            Transition enterTransition2 = enterTransition;
            if (enterTransition2 == null && sharedElementTransition == null) {
                exitTransition = exitTransition3;
                if (exitTransition == null) {
                    return;
                }
            } else {
                exitTransition = exitTransition3;
            }
            ArrayList<View> exitingViews = configureEnteringExitingViews(exitTransition, outFragment, sharedElementsOut2, view);
            if (exitingViews == null || exitingViews.isEmpty()) {
                exitTransition = null;
            }
            if (enterTransition2 != null) {
                enterTransition2.addTarget(view);
            }
            Transition transition = mergeTransitions(enterTransition2, exitTransition, sharedElementTransition, inFragment, fragmentContainerTransition.lastInIsPop);
            if (transition != null) {
                transition.setNameOverrides(nameOverrides);
                ArrayList arrayList = new ArrayList();
                scheduleRemoveTargets(transition, enterTransition2, arrayList, exitTransition, exitingViews, sharedElementTransition, sharedElementsIn);
                Fragment fragment = outFragment;
                Fragment fragment2 = inFragment;
                boolean z2 = inIsPop;
                scheduleTargetChange(sceneRoot2, inFragment, view, sharedElementsIn, enterTransition2, arrayList, exitTransition, exitingViews);
                TransitionManager.beginDelayedTransition(sceneRoot2, transition);
            } else {
                ArrayMap<String, String> arrayMap = nameOverrides;
                boolean z3 = inIsPop;
                Fragment fragment3 = outFragment;
                Fragment fragment4 = inFragment;
                ViewGroup viewGroup = sceneRoot2;
            }
        }
    }

    private static void replaceHide(Transition exitTransition, Fragment exitingFragment, final ArrayList<View> exitingViews) {
        if (exitingFragment != null && exitTransition != null && exitingFragment.mAdded && exitingFragment.mHidden && exitingFragment.mHiddenChanged) {
            exitingFragment.setHideReplaced(true);
            final View fragmentView = exitingFragment.getView();
            OneShotPreDrawListener.add(exitingFragment.mContainer, new Runnable(exitingViews) {
                private final /* synthetic */ ArrayList f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    FragmentTransition.setViewVisibility(this.f$0, 4);
                }
            });
            exitTransition.addListener(new TransitionListenerAdapter() {
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                    fragmentView.setVisibility(8);
                    FragmentTransition.setViewVisibility(exitingViews, 0);
                }
            });
        }
    }

    private static void scheduleTargetChange(ViewGroup sceneRoot, Fragment inFragment, View nonExistentView, ArrayList<View> sharedElementsIn, Transition enterTransition, ArrayList<View> enteringViews, Transition exitTransition, ArrayList<View> exitingViews) {
        $$Lambda$FragmentTransition$8Ei4ls5jlZcfRvuLcweFAxtFBFs r0 = new Runnable(enterTransition, nonExistentView, inFragment, sharedElementsIn, enteringViews, exitingViews, exitTransition) {
            private final /* synthetic */ Transition f$0;
            private final /* synthetic */ View f$1;
            private final /* synthetic */ Fragment f$2;
            private final /* synthetic */ ArrayList f$3;
            private final /* synthetic */ ArrayList f$4;
            private final /* synthetic */ ArrayList f$5;
            private final /* synthetic */ Transition f$6;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
            }

            public final void run() {
                FragmentTransition.lambda$scheduleTargetChange$1(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
            }
        };
        OneShotPreDrawListener.add(sceneRoot, r0);
    }

    static /* synthetic */ void lambda$scheduleTargetChange$1(Transition enterTransition, View nonExistentView, Fragment inFragment, ArrayList sharedElementsIn, ArrayList enteringViews, ArrayList exitingViews, Transition exitTransition) {
        if (enterTransition != null) {
            enterTransition.removeTarget(nonExistentView);
            enteringViews.addAll(configureEnteringExitingViews(enterTransition, inFragment, sharedElementsIn, nonExistentView));
        }
        if (exitingViews != null) {
            if (exitTransition != null) {
                ArrayList<View> tempExiting = new ArrayList<>();
                tempExiting.add(nonExistentView);
                replaceTargets(exitTransition, exitingViews, tempExiting);
            }
            exitingViews.clear();
            exitingViews.add(nonExistentView);
        }
    }

    private static TransitionSet getSharedElementTransition(Fragment inFragment, Fragment outFragment, boolean isPop) {
        Transition transition;
        if (inFragment == null || outFragment == null) {
            return null;
        }
        if (isPop) {
            transition = outFragment.getSharedElementReturnTransition();
        } else {
            transition = inFragment.getSharedElementEnterTransition();
        }
        Transition transition2 = cloneTransition(transition);
        if (transition2 == null) {
            return null;
        }
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(transition2);
        return transitionSet;
    }

    private static Transition getEnterTransition(Fragment inFragment, boolean isPop) {
        Transition transition;
        if (inFragment == null) {
            return null;
        }
        if (isPop) {
            transition = inFragment.getReenterTransition();
        } else {
            transition = inFragment.getEnterTransition();
        }
        return cloneTransition(transition);
    }

    private static Transition getExitTransition(Fragment outFragment, boolean isPop) {
        Transition transition;
        if (outFragment == null) {
            return null;
        }
        if (isPop) {
            transition = outFragment.getReturnTransition();
        } else {
            transition = outFragment.getExitTransition();
        }
        return cloneTransition(transition);
    }

    private static Transition cloneTransition(Transition transition) {
        if (transition != null) {
            return transition.clone();
        }
        return transition;
    }

    private static TransitionSet configureSharedElementsReordered(ViewGroup sceneRoot, View nonExistentView, ArrayMap<String, String> nameOverrides, FragmentContainerTransition fragments, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn, Transition enterTransition, Transition exitTransition) {
        View epicenterView;
        Rect epicenter;
        View view = nonExistentView;
        ArrayMap<String, String> arrayMap = nameOverrides;
        FragmentContainerTransition fragmentContainerTransition = fragments;
        ArrayList<View> arrayList = sharedElementsOut;
        ArrayList<View> arrayList2 = sharedElementsIn;
        Transition transition = enterTransition;
        Transition transition2 = exitTransition;
        Fragment inFragment = fragmentContainerTransition.lastIn;
        Fragment outFragment = fragmentContainerTransition.firstOut;
        if (!(inFragment == null || inFragment.getView() == null)) {
            inFragment.getView().setVisibility(0);
        }
        if (inFragment == null || outFragment == null) {
            ViewGroup viewGroup = sceneRoot;
            return null;
        }
        boolean inIsPop = fragmentContainerTransition.lastInIsPop;
        TransitionSet sharedElementTransition = nameOverrides.isEmpty() ? null : getSharedElementTransition(inFragment, outFragment, inIsPop);
        ArrayMap<String, View> outSharedElements = captureOutSharedElements(arrayMap, sharedElementTransition, fragmentContainerTransition);
        ArrayMap<String, View> inSharedElements = captureInSharedElements(arrayMap, sharedElementTransition, fragmentContainerTransition);
        if (nameOverrides.isEmpty()) {
            sharedElementTransition = null;
            if (outSharedElements != null) {
                outSharedElements.clear();
            }
            if (inSharedElements != null) {
                inSharedElements.clear();
            }
        } else {
            addSharedElementsWithMatchingNames(arrayList, outSharedElements, nameOverrides.keySet());
            addSharedElementsWithMatchingNames(arrayList2, inSharedElements, nameOverrides.values());
        }
        TransitionSet sharedElementTransition2 = sharedElementTransition;
        if (transition == null && transition2 == null && sharedElementTransition2 == null) {
            return null;
        }
        callSharedElementStartEnd(inFragment, outFragment, inIsPop, outSharedElements, true);
        if (sharedElementTransition2 != null) {
            arrayList2.add(view);
            setSharedElementTargets(sharedElementTransition2, view, arrayList);
            setOutEpicenter(sharedElementTransition2, transition2, outSharedElements, fragmentContainerTransition.firstOutIsPop, fragmentContainerTransition.firstOutTransaction);
            final Rect epicenter2 = new Rect();
            View epicenterView2 = getInEpicenterView(inSharedElements, fragmentContainerTransition, transition, inIsPop);
            if (epicenterView2 != null) {
                transition.setEpicenterCallback(new Transition.EpicenterCallback() {
                    public Rect onGetEpicenter(Transition transition) {
                        return Rect.this;
                    }
                });
            }
            epicenter = epicenter2;
            epicenterView = epicenterView2;
        } else {
            epicenter = null;
            epicenterView = null;
        }
        $$Lambda$FragmentTransition$jurn0WXuKw3bRQ_2d5zCWdeZWuI r1 = r7;
        TransitionSet sharedElementTransition3 = sharedElementTransition2;
        ArrayMap<String, View> arrayMap2 = outSharedElements;
        boolean z = inIsPop;
        $$Lambda$FragmentTransition$jurn0WXuKw3bRQ_2d5zCWdeZWuI r7 = new Runnable(outFragment, inIsPop, inSharedElements, epicenterView, epicenter) {
            private final /* synthetic */ Fragment f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ ArrayMap f$3;
            private final /* synthetic */ View f$4;
            private final /* synthetic */ Rect f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void run() {
                FragmentTransition.lambda$configureSharedElementsReordered$2(Fragment.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        };
        OneShotPreDrawListener.add(sceneRoot, r1);
        return sharedElementTransition3;
    }

    static /* synthetic */ void lambda$configureSharedElementsReordered$2(Fragment inFragment, Fragment outFragment, boolean inIsPop, ArrayMap inSharedElements, View epicenterView, Rect epicenter) {
        callSharedElementStartEnd(inFragment, outFragment, inIsPop, inSharedElements, false);
        if (epicenterView != null) {
            epicenterView.getBoundsOnScreen(epicenter);
        }
    }

    private static void addSharedElementsWithMatchingNames(ArrayList<View> views, ArrayMap<String, View> sharedElements, Collection<String> nameOverridesSet) {
        for (int i = sharedElements.size() - 1; i >= 0; i--) {
            View view = sharedElements.valueAt(i);
            if (view != null && nameOverridesSet.contains(view.getTransitionName())) {
                views.add(view);
            }
        }
    }

    private static TransitionSet configureSharedElementsOrdered(ViewGroup sceneRoot, View nonExistentView, ArrayMap<String, String> nameOverrides, FragmentContainerTransition fragments, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn, Transition enterTransition, Transition exitTransition) {
        FragmentContainerTransition fragmentContainerTransition = fragments;
        ArrayList<View> arrayList = sharedElementsOut;
        Transition transition = enterTransition;
        Transition transition2 = exitTransition;
        Fragment inFragment = fragmentContainerTransition.lastIn;
        Fragment outFragment = fragmentContainerTransition.firstOut;
        final Rect inEpicenter = null;
        if (inFragment == null) {
            ViewGroup viewGroup = sceneRoot;
            Fragment fragment = outFragment;
            Fragment fragment2 = inFragment;
        } else if (outFragment == null) {
            ViewGroup viewGroup2 = sceneRoot;
            Fragment fragment3 = outFragment;
            Fragment fragment4 = inFragment;
        } else {
            boolean inIsPop = fragmentContainerTransition.lastInIsPop;
            TransitionSet sharedElementTransition = nameOverrides.isEmpty() ? null : getSharedElementTransition(inFragment, outFragment, inIsPop);
            ArrayMap<String, String> arrayMap = nameOverrides;
            ArrayMap<String, View> outSharedElements = captureOutSharedElements(arrayMap, sharedElementTransition, fragmentContainerTransition);
            if (nameOverrides.isEmpty()) {
                sharedElementTransition = null;
            } else {
                arrayList.addAll(outSharedElements.values());
            }
            TransitionSet sharedElementTransition2 = sharedElementTransition;
            if (transition == null && transition2 == null && sharedElementTransition2 == null) {
                return null;
            }
            callSharedElementStartEnd(inFragment, outFragment, inIsPop, outSharedElements, true);
            if (sharedElementTransition2 != null) {
                inEpicenter = new Rect();
                setSharedElementTargets(sharedElementTransition2, nonExistentView, arrayList);
                setOutEpicenter(sharedElementTransition2, transition2, outSharedElements, fragmentContainerTransition.firstOutIsPop, fragmentContainerTransition.firstOutTransaction);
                if (transition != null) {
                    transition.setEpicenterCallback(new Transition.EpicenterCallback() {
                        public Rect onGetEpicenter(Transition transition) {
                            if (Rect.this.isEmpty()) {
                                return null;
                            }
                            return Rect.this;
                        }
                    });
                }
            } else {
                View view = nonExistentView;
            }
            $$Lambda$FragmentTransition$Ip0LktADPhG_3ouNBXgzufWpFfY r15 = r0;
            TransitionSet sharedElementTransition3 = sharedElementTransition2;
            ArrayMap<String, View> arrayMap2 = outSharedElements;
            boolean z = inIsPop;
            Fragment fragment5 = outFragment;
            Fragment fragment6 = inFragment;
            $$Lambda$FragmentTransition$Ip0LktADPhG_3ouNBXgzufWpFfY r0 = new Runnable(arrayMap, sharedElementTransition2, fragmentContainerTransition, sharedElementsIn, nonExistentView, inFragment, outFragment, inIsPop, arrayList, transition, inEpicenter) {
                private final /* synthetic */ ArrayMap f$0;
                private final /* synthetic */ TransitionSet f$1;
                private final /* synthetic */ Rect f$10;
                private final /* synthetic */ FragmentTransition.FragmentContainerTransition f$2;
                private final /* synthetic */ ArrayList f$3;
                private final /* synthetic */ View f$4;
                private final /* synthetic */ Fragment f$5;
                private final /* synthetic */ Fragment f$6;
                private final /* synthetic */ boolean f$7;
                private final /* synthetic */ ArrayList f$8;
                private final /* synthetic */ Transition f$9;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r7;
                    this.f$7 = r8;
                    this.f$8 = r9;
                    this.f$9 = r10;
                    this.f$10 = r11;
                }

                public final void run() {
                    FragmentTransition.lambda$configureSharedElementsOrdered$3(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10);
                }
            };
            OneShotPreDrawListener.add(sceneRoot, r15);
            return sharedElementTransition3;
        }
        return null;
    }

    static /* synthetic */ void lambda$configureSharedElementsOrdered$3(ArrayMap nameOverrides, TransitionSet finalSharedElementTransition, FragmentContainerTransition fragments, ArrayList sharedElementsIn, View nonExistentView, Fragment inFragment, Fragment outFragment, boolean inIsPop, ArrayList sharedElementsOut, Transition enterTransition, Rect inEpicenter) {
        ArrayMap<String, View> inSharedElements = captureInSharedElements(nameOverrides, finalSharedElementTransition, fragments);
        if (inSharedElements != null) {
            sharedElementsIn.addAll(inSharedElements.values());
            sharedElementsIn.add(nonExistentView);
        }
        callSharedElementStartEnd(inFragment, outFragment, inIsPop, inSharedElements, false);
        if (finalSharedElementTransition != null) {
            finalSharedElementTransition.getTargets().clear();
            finalSharedElementTransition.getTargets().addAll(sharedElementsIn);
            replaceTargets(finalSharedElementTransition, sharedElementsOut, sharedElementsIn);
            View inEpicenterView = getInEpicenterView(inSharedElements, fragments, enterTransition, inIsPop);
            if (inEpicenterView != null) {
                inEpicenterView.getBoundsOnScreen(inEpicenter);
            }
        }
    }

    private static ArrayMap<String, View> captureOutSharedElements(ArrayMap<String, String> nameOverrides, TransitionSet sharedElementTransition, FragmentContainerTransition fragments) {
        ArrayList<String> names;
        SharedElementCallback sharedElementCallback;
        if (nameOverrides.isEmpty() || sharedElementTransition == null) {
            nameOverrides.clear();
            return null;
        }
        Fragment outFragment = fragments.firstOut;
        ArrayMap<String, View> outSharedElements = new ArrayMap<>();
        outFragment.getView().findNamedViews(outSharedElements);
        BackStackRecord outTransaction = fragments.firstOutTransaction;
        if (fragments.firstOutIsPop) {
            sharedElementCallback = outFragment.getEnterTransitionCallback();
            names = outTransaction.mSharedElementTargetNames;
        } else {
            sharedElementCallback = outFragment.getExitTransitionCallback();
            names = outTransaction.mSharedElementSourceNames;
        }
        outSharedElements.retainAll(names);
        if (sharedElementCallback != null) {
            sharedElementCallback.onMapSharedElements(names, outSharedElements);
            for (int i = names.size() - 1; i >= 0; i--) {
                String name = names.get(i);
                View view = outSharedElements.get(name);
                if (view == null) {
                    nameOverrides.remove(name);
                } else if (!name.equals(view.getTransitionName())) {
                    nameOverrides.put(view.getTransitionName(), nameOverrides.remove(name));
                }
            }
        } else {
            nameOverrides.retainAll(outSharedElements.keySet());
        }
        return outSharedElements;
    }

    private static ArrayMap<String, View> captureInSharedElements(ArrayMap<String, String> nameOverrides, TransitionSet sharedElementTransition, FragmentContainerTransition fragments) {
        ArrayList<String> names;
        SharedElementCallback sharedElementCallback;
        Fragment inFragment = fragments.lastIn;
        View fragmentView = inFragment.getView();
        if (nameOverrides.isEmpty() || sharedElementTransition == null || fragmentView == null) {
            nameOverrides.clear();
            return null;
        }
        ArrayMap<String, View> inSharedElements = new ArrayMap<>();
        fragmentView.findNamedViews(inSharedElements);
        BackStackRecord inTransaction = fragments.lastInTransaction;
        if (fragments.lastInIsPop) {
            sharedElementCallback = inFragment.getExitTransitionCallback();
            names = inTransaction.mSharedElementSourceNames;
        } else {
            sharedElementCallback = inFragment.getEnterTransitionCallback();
            names = inTransaction.mSharedElementTargetNames;
        }
        if (names != null) {
            inSharedElements.retainAll(names);
        }
        if (names == null || sharedElementCallback == null) {
            retainValues(nameOverrides, inSharedElements);
        } else {
            sharedElementCallback.onMapSharedElements(names, inSharedElements);
            for (int i = names.size() - 1; i >= 0; i--) {
                String name = names.get(i);
                View view = inSharedElements.get(name);
                if (view == null) {
                    String key = findKeyForValue(nameOverrides, name);
                    if (key != null) {
                        nameOverrides.remove(key);
                    }
                } else if (!name.equals(view.getTransitionName())) {
                    String key2 = findKeyForValue(nameOverrides, name);
                    if (key2 != null) {
                        nameOverrides.put(key2, view.getTransitionName());
                    }
                }
            }
        }
        return inSharedElements;
    }

    private static String findKeyForValue(ArrayMap<String, String> map, String value) {
        int numElements = map.size();
        for (int i = 0; i < numElements; i++) {
            if (value.equals(map.valueAt(i))) {
                return map.keyAt(i);
            }
        }
        return null;
    }

    private static View getInEpicenterView(ArrayMap<String, View> inSharedElements, FragmentContainerTransition fragments, Transition enterTransition, boolean inIsPop) {
        String targetName;
        BackStackRecord inTransaction = fragments.lastInTransaction;
        if (enterTransition == null || inSharedElements == null || inTransaction.mSharedElementSourceNames == null || inTransaction.mSharedElementSourceNames.isEmpty()) {
            return null;
        }
        if (inIsPop) {
            targetName = inTransaction.mSharedElementSourceNames.get(0);
        } else {
            targetName = inTransaction.mSharedElementTargetNames.get(0);
        }
        return inSharedElements.get(targetName);
    }

    private static void setOutEpicenter(TransitionSet sharedElementTransition, Transition exitTransition, ArrayMap<String, View> outSharedElements, boolean outIsPop, BackStackRecord outTransaction) {
        String sourceName;
        if (outTransaction.mSharedElementSourceNames != null && !outTransaction.mSharedElementSourceNames.isEmpty()) {
            if (outIsPop) {
                sourceName = outTransaction.mSharedElementTargetNames.get(0);
            } else {
                sourceName = outTransaction.mSharedElementSourceNames.get(0);
            }
            View outEpicenterView = outSharedElements.get(sourceName);
            setEpicenter(sharedElementTransition, outEpicenterView);
            if (exitTransition != null) {
                setEpicenter(exitTransition, outEpicenterView);
            }
        }
    }

    private static void setEpicenter(Transition transition, View view) {
        if (view != null) {
            final Rect epicenter = new Rect();
            view.getBoundsOnScreen(epicenter);
            transition.setEpicenterCallback(new Transition.EpicenterCallback() {
                public Rect onGetEpicenter(Transition transition) {
                    return Rect.this;
                }
            });
        }
    }

    private static void retainValues(ArrayMap<String, String> nameOverrides, ArrayMap<String, View> namedViews) {
        for (int i = nameOverrides.size() - 1; i >= 0; i--) {
            if (!namedViews.containsKey(nameOverrides.valueAt(i))) {
                nameOverrides.removeAt(i);
            }
        }
    }

    private static void callSharedElementStartEnd(Fragment inFragment, Fragment outFragment, boolean isPop, ArrayMap<String, View> sharedElements, boolean isStart) {
        SharedElementCallback sharedElementCallback;
        if (isPop) {
            sharedElementCallback = outFragment.getEnterTransitionCallback();
        } else {
            sharedElementCallback = inFragment.getEnterTransitionCallback();
        }
        if (sharedElementCallback != null) {
            ArrayList<View> views = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            int count = sharedElements == null ? 0 : sharedElements.size();
            for (int i = 0; i < count; i++) {
                names.add(sharedElements.keyAt(i));
                views.add(sharedElements.valueAt(i));
            }
            if (isStart) {
                sharedElementCallback.onSharedElementStart(names, views, null);
            } else {
                sharedElementCallback.onSharedElementEnd(names, views, null);
            }
        }
    }

    private static void setSharedElementTargets(TransitionSet transition, View nonExistentView, ArrayList<View> sharedViews) {
        List<View> views = transition.getTargets();
        views.clear();
        int count = sharedViews.size();
        for (int i = 0; i < count; i++) {
            bfsAddViewChildren(views, sharedViews.get(i));
        }
        views.add(nonExistentView);
        sharedViews.add(nonExistentView);
        addTargets(transition, sharedViews);
    }

    private static void bfsAddViewChildren(List<View> views, View startView) {
        int startIndex = views.size();
        if (!containedBeforeIndex(views, startView, startIndex)) {
            views.add(startView);
            for (int index = startIndex; index < views.size(); index++) {
                View view = views.get(index);
                if (view instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) view;
                    int childCount = viewGroup.getChildCount();
                    for (int childIndex = 0; childIndex < childCount; childIndex++) {
                        View child = viewGroup.getChildAt(childIndex);
                        if (!containedBeforeIndex(views, child, startIndex)) {
                            views.add(child);
                        }
                    }
                }
            }
        }
    }

    private static boolean containedBeforeIndex(List<View> views, View view, int maxIndex) {
        for (int i = 0; i < maxIndex; i++) {
            if (views.get(i) == view) {
                return true;
            }
        }
        return false;
    }

    private static void scheduleRemoveTargets(Transition overalTransition, Transition enterTransition, ArrayList<View> enteringViews, Transition exitTransition, ArrayList<View> exitingViews, TransitionSet sharedElementTransition, ArrayList<View> sharedElementsIn) {
        final Transition transition = enterTransition;
        final ArrayList<View> arrayList = enteringViews;
        final Transition transition2 = exitTransition;
        final ArrayList<View> arrayList2 = exitingViews;
        final TransitionSet transitionSet = sharedElementTransition;
        final ArrayList<View> arrayList3 = sharedElementsIn;
        AnonymousClass5 r0 = new TransitionListenerAdapter() {
            public void onTransitionStart(Transition transition) {
                if (transition != null) {
                    FragmentTransition.replaceTargets(transition, arrayList, null);
                }
                if (transition2 != null) {
                    FragmentTransition.replaceTargets(transition2, arrayList2, null);
                }
                if (transitionSet != null) {
                    FragmentTransition.replaceTargets(transitionSet, arrayList3, null);
                }
            }
        };
        overalTransition.addListener(r0);
    }

    public static void replaceTargets(Transition transition, ArrayList<View> oldTargets, ArrayList<View> newTargets) {
        int i = 0;
        if (transition instanceof TransitionSet) {
            TransitionSet set = (TransitionSet) transition;
            int numTransitions = set.getTransitionCount();
            while (i < numTransitions) {
                replaceTargets(set.getTransitionAt(i), oldTargets, newTargets);
                i++;
            }
        } else if (!hasSimpleTarget(transition)) {
            List<View> targets = transition.getTargets();
            if (targets != null && targets.size() == oldTargets.size() && targets.containsAll(oldTargets)) {
                int targetCount = newTargets == null ? 0 : newTargets.size();
                while (i < targetCount) {
                    transition.addTarget(newTargets.get(i));
                    i++;
                }
                for (int i2 = oldTargets.size() - 1; i2 >= 0; i2--) {
                    transition.removeTarget(oldTargets.get(i2));
                }
            }
        }
    }

    public static void addTargets(Transition transition, ArrayList<View> views) {
        if (transition != null) {
            int i = 0;
            if (transition instanceof TransitionSet) {
                TransitionSet set = (TransitionSet) transition;
                int numTransitions = set.getTransitionCount();
                while (i < numTransitions) {
                    addTargets(set.getTransitionAt(i), views);
                    i++;
                }
            } else if (!hasSimpleTarget(transition) && isNullOrEmpty(transition.getTargets())) {
                int numViews = views.size();
                while (i < numViews) {
                    transition.addTarget(views.get(i));
                    i++;
                }
            }
        }
    }

    private static boolean hasSimpleTarget(Transition transition) {
        return !isNullOrEmpty(transition.getTargetIds()) || !isNullOrEmpty(transition.getTargetNames()) || !isNullOrEmpty(transition.getTargetTypes());
    }

    private static boolean isNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }

    private static ArrayList<View> configureEnteringExitingViews(Transition transition, Fragment fragment, ArrayList<View> sharedElements, View nonExistentView) {
        ArrayList<View> viewList = null;
        if (transition != null) {
            viewList = new ArrayList<>();
            View root = fragment.getView();
            if (root != null) {
                root.captureTransitioningViews(viewList);
            }
            if (sharedElements != null) {
                viewList.removeAll(sharedElements);
            }
            if (!viewList.isEmpty()) {
                viewList.add(nonExistentView);
                addTargets(transition, viewList);
            }
        }
        return viewList;
    }

    /* access modifiers changed from: private */
    public static void setViewVisibility(ArrayList<View> views, int visibility) {
        if (views != null) {
            for (int i = views.size() - 1; i >= 0; i--) {
                views.get(i).setVisibility(visibility);
            }
        }
    }

    private static Transition mergeTransitions(Transition enterTransition, Transition exitTransition, Transition sharedElementTransition, Fragment inFragment, boolean isPop) {
        boolean z;
        boolean overlap = true;
        if (!(enterTransition == null || exitTransition == null || inFragment == null)) {
            if (isPop) {
                z = inFragment.getAllowReturnTransitionOverlap();
            } else {
                z = inFragment.getAllowEnterTransitionOverlap();
            }
            overlap = z;
        }
        if (overlap) {
            TransitionSet transitionSet = new TransitionSet();
            if (enterTransition != null) {
                transitionSet.addTransition(enterTransition);
            }
            if (exitTransition != null) {
                transitionSet.addTransition(exitTransition);
            }
            if (sharedElementTransition == null) {
                return transitionSet;
            }
            transitionSet.addTransition(sharedElementTransition);
            return transitionSet;
        }
        Transition staggered = null;
        if (exitTransition != null && enterTransition != null) {
            staggered = new TransitionSet().addTransition(exitTransition).addTransition(enterTransition).setOrdering(1);
        } else if (exitTransition != null) {
            staggered = exitTransition;
        } else if (enterTransition != null) {
            staggered = enterTransition;
        }
        if (sharedElementTransition == null) {
            return staggered;
        }
        TransitionSet together = new TransitionSet();
        if (staggered != null) {
            together.addTransition(staggered);
        }
        together.addTransition(sharedElementTransition);
        return together;
    }

    public static void calculateFragments(BackStackRecord transaction, SparseArray<FragmentContainerTransition> transitioningFragments, boolean isReordered) {
        int numOps = transaction.mOps.size();
        for (int opNum = 0; opNum < numOps; opNum++) {
            addToFirstInLastOut(transaction, transaction.mOps.get(opNum), transitioningFragments, false, isReordered);
        }
    }

    public static void calculatePopFragments(BackStackRecord transaction, SparseArray<FragmentContainerTransition> transitioningFragments, boolean isReordered) {
        if (transaction.mManager.mContainer.onHasView()) {
            for (int opNum = transaction.mOps.size() - 1; opNum >= 0; opNum--) {
                addToFirstInLastOut(transaction, transaction.mOps.get(opNum), transitioningFragments, true, isReordered);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:88:0x0106  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x011a  */
    private static void addToFirstInLastOut(BackStackRecord transaction, BackStackRecord.Op op, SparseArray<FragmentContainerTransition> transitioningFragments, boolean isPop, boolean isReorderedTransaction) {
        boolean setLastIn;
        FragmentContainerTransition containerTransition;
        Fragment fragment;
        FragmentContainerTransition containerTransition2;
        FragmentContainerTransition containerTransition3;
        boolean setFirstOut;
        BackStackRecord backStackRecord = transaction;
        BackStackRecord.Op op2 = op;
        SparseArray<FragmentContainerTransition> sparseArray = transitioningFragments;
        boolean z = isPop;
        Fragment fragment2 = op2.fragment;
        if (fragment2 != null) {
            int containerId = fragment2.mContainerId;
            if (containerId != 0) {
                int command = z ? INVERSE_OPS[op2.cmd] : op2.cmd;
                boolean setLastIn2 = false;
                boolean wasRemoved = false;
                boolean setFirstOut2 = false;
                boolean wasAdded = false;
                boolean z2 = false;
                if (command != 1) {
                    switch (command) {
                        case 3:
                        case 6:
                            if (isReorderedTransaction) {
                                if (!fragment2.mAdded && fragment2.mView != null && fragment2.mView.getVisibility() == 0 && fragment2.mView.getTransitionAlpha() > 0.0f) {
                                    z2 = true;
                                }
                                setFirstOut = z2;
                            } else {
                                if (fragment2.mAdded && !fragment2.mHidden) {
                                    z2 = true;
                                }
                                setFirstOut = z2;
                            }
                            wasRemoved = true;
                            break;
                        case 4:
                            if (isReorderedTransaction) {
                                if (fragment2.mHiddenChanged && fragment2.mAdded && fragment2.mHidden) {
                                    z2 = true;
                                }
                                setFirstOut2 = z2;
                            } else {
                                if (fragment2.mAdded && !fragment2.mHidden) {
                                    z2 = true;
                                }
                                setFirstOut2 = z2;
                            }
                            wasRemoved = true;
                            break;
                        case 5:
                            if (isReorderedTransaction) {
                                if (fragment2.mHiddenChanged && !fragment2.mHidden && fragment2.mAdded) {
                                    z2 = true;
                                }
                                setLastIn2 = z2;
                            } else {
                                setLastIn2 = fragment2.mHidden;
                            }
                            wasAdded = true;
                            break;
                        case 7:
                            break;
                    }
                }
                if (isReorderedTransaction) {
                    setLastIn = fragment2.mIsNewlyAdded;
                } else {
                    if (!fragment2.mAdded && !fragment2.mHidden) {
                        z2 = true;
                    }
                    setLastIn = z2;
                }
                wasAdded = true;
                boolean setLastIn3 = setLastIn2;
                boolean wasRemoved2 = wasRemoved;
                boolean setFirstOut3 = setFirstOut2;
                boolean wasAdded2 = wasAdded;
                FragmentContainerTransition containerTransition4 = sparseArray.get(containerId);
                if (setLastIn3) {
                    containerTransition4 = ensureContainer(containerTransition4, sparseArray, containerId);
                    containerTransition4.lastIn = fragment2;
                    containerTransition4.lastInIsPop = z;
                    containerTransition4.lastInTransaction = backStackRecord;
                }
                FragmentContainerTransition containerTransition5 = containerTransition4;
                if (!isReorderedTransaction && wasAdded2) {
                    if (containerTransition5 != null && containerTransition5.firstOut == fragment2) {
                        containerTransition5.firstOut = null;
                    }
                    FragmentManagerImpl manager = backStackRecord.mManager;
                    if (fragment2.mState < 1 && manager.mCurState >= 1 && manager.mHost.getContext().getApplicationInfo().targetSdkVersion >= 24 && !backStackRecord.mReorderingAllowed) {
                        manager.makeActive(fragment2);
                        FragmentManagerImpl fragmentManagerImpl = manager;
                        containerTransition = containerTransition5;
                        fragment = null;
                        manager.moveToState(fragment2, 1, 0, 0, false);
                        if (!setFirstOut3) {
                            containerTransition3 = containerTransition;
                            if (containerTransition3 == null || containerTransition3.firstOut == null) {
                                containerTransition2 = ensureContainer(containerTransition3, sparseArray, containerId);
                                containerTransition2.firstOut = fragment2;
                                containerTransition2.firstOutIsPop = z;
                                containerTransition2.firstOutTransaction = backStackRecord;
                                if (!isReorderedTransaction && wasRemoved2 && containerTransition2 != null && containerTransition2.lastIn == fragment2) {
                                    containerTransition2.lastIn = fragment;
                                }
                            }
                        } else {
                            containerTransition3 = containerTransition;
                        }
                        containerTransition2 = containerTransition3;
                        containerTransition2.lastIn = fragment;
                    }
                }
                fragment = null;
                containerTransition = containerTransition5;
                if (!setFirstOut3) {
                }
                containerTransition2 = containerTransition3;
                containerTransition2.lastIn = fragment;
            }
        }
    }

    private static FragmentContainerTransition ensureContainer(FragmentContainerTransition containerTransition, SparseArray<FragmentContainerTransition> transitioningFragments, int containerId) {
        if (containerTransition != null) {
            return containerTransition;
        }
        FragmentContainerTransition containerTransition2 = new FragmentContainerTransition();
        transitioningFragments.put(containerId, containerTransition2);
        return containerTransition2;
    }
}
