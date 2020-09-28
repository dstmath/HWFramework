package android.app;

import android.app.BackStackRecord;
import android.app.FragmentTransition;
import android.graphics.Rect;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.view.OneShotPreDrawListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/* access modifiers changed from: package-private */
public class FragmentTransition {
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

    private static void configureTransitionsReordered(FragmentManagerImpl fragmentManager, int containerId, FragmentContainerTransition fragments, View nonExistentView, ArrayMap<String, String> nameOverrides) {
        ViewGroup sceneRoot;
        Transition exitTransition;
        if (fragmentManager.mContainer.onHasView()) {
            sceneRoot = (ViewGroup) fragmentManager.mContainer.onFindViewById(containerId);
        } else {
            sceneRoot = null;
        }
        if (sceneRoot != null) {
            Fragment inFragment = fragments.lastIn;
            Fragment outFragment = fragments.firstOut;
            boolean inIsPop = fragments.lastInIsPop;
            boolean outIsPop = fragments.firstOutIsPop;
            ArrayList<View> sharedElementsIn = new ArrayList<>();
            ArrayList<View> sharedElementsOut = new ArrayList<>();
            Transition enterTransition = getEnterTransition(inFragment, inIsPop);
            Transition exitTransition2 = getExitTransition(outFragment, outIsPop);
            TransitionSet sharedElementTransition = configureSharedElementsReordered(sceneRoot, nonExistentView, nameOverrides, fragments, sharedElementsOut, sharedElementsIn, enterTransition, exitTransition2);
            if (enterTransition == null && sharedElementTransition == null) {
                exitTransition = exitTransition2;
                if (exitTransition == null) {
                    return;
                }
            } else {
                exitTransition = exitTransition2;
            }
            ArrayList<View> exitingViews = configureEnteringExitingViews(exitTransition, outFragment, sharedElementsOut, nonExistentView);
            ArrayList<View> enteringViews = configureEnteringExitingViews(enterTransition, inFragment, sharedElementsIn, nonExistentView);
            setViewVisibility(enteringViews, 4);
            Transition transition = mergeTransitions(enterTransition, exitTransition, sharedElementTransition, inFragment, inIsPop);
            if (transition != null) {
                replaceHide(exitTransition, outFragment, exitingViews);
                transition.setNameOverrides(nameOverrides);
                scheduleRemoveTargets(transition, enterTransition, enteringViews, exitTransition, exitingViews, sharedElementTransition, sharedElementsIn);
                TransitionManager.beginDelayedTransition(sceneRoot, transition);
                setViewVisibility(enteringViews, 0);
                if (sharedElementTransition != null) {
                    sharedElementTransition.getTargets().clear();
                    sharedElementTransition.getTargets().addAll(sharedElementsIn);
                    replaceTargets(sharedElementTransition, sharedElementsOut, sharedElementsIn);
                }
            }
        }
    }

    private static void configureTransitionsOrdered(FragmentManagerImpl fragmentManager, int containerId, FragmentContainerTransition fragments, View nonExistentView, ArrayMap<String, String> nameOverrides) {
        ViewGroup sceneRoot;
        Transition exitTransition;
        if (fragmentManager.mContainer.onHasView()) {
            sceneRoot = (ViewGroup) fragmentManager.mContainer.onFindViewById(containerId);
        } else {
            sceneRoot = null;
        }
        if (sceneRoot != null) {
            Fragment inFragment = fragments.lastIn;
            Fragment outFragment = fragments.firstOut;
            boolean inIsPop = fragments.lastInIsPop;
            boolean outIsPop = fragments.firstOutIsPop;
            Transition enterTransition = getEnterTransition(inFragment, inIsPop);
            Transition exitTransition2 = getExitTransition(outFragment, outIsPop);
            ArrayList<View> sharedElementsOut = new ArrayList<>();
            ArrayList<View> sharedElementsIn = new ArrayList<>();
            TransitionSet sharedElementTransition = configureSharedElementsOrdered(sceneRoot, nonExistentView, nameOverrides, fragments, sharedElementsOut, sharedElementsIn, enterTransition, exitTransition2);
            if (enterTransition == null && sharedElementTransition == null) {
                exitTransition = exitTransition2;
                if (exitTransition == null) {
                    return;
                }
            } else {
                exitTransition = exitTransition2;
            }
            ArrayList<View> exitingViews = configureEnteringExitingViews(exitTransition, outFragment, sharedElementsOut, nonExistentView);
            if (exitingViews == null || exitingViews.isEmpty()) {
                exitTransition = null;
            }
            if (enterTransition != null) {
                enterTransition.addTarget(nonExistentView);
            }
            Transition transition = mergeTransitions(enterTransition, exitTransition, sharedElementTransition, inFragment, fragments.lastInIsPop);
            if (transition != null) {
                transition.setNameOverrides(nameOverrides);
                ArrayList<View> enteringViews = new ArrayList<>();
                scheduleRemoveTargets(transition, enterTransition, enteringViews, exitTransition, exitingViews, sharedElementTransition, sharedElementsIn);
                scheduleTargetChange(sceneRoot, inFragment, nonExistentView, sharedElementsIn, enterTransition, enteringViews, exitTransition, exitingViews);
                TransitionManager.beginDelayedTransition(sceneRoot, transition);
            }
        }
    }

    private static void replaceHide(Transition exitTransition, Fragment exitingFragment, final ArrayList<View> exitingViews) {
        if (exitingFragment != null && exitTransition != null && exitingFragment.mAdded && exitingFragment.mHidden && exitingFragment.mHiddenChanged) {
            exitingFragment.setHideReplaced(true);
            final View fragmentView = exitingFragment.getView();
            OneShotPreDrawListener.add(exitingFragment.mContainer, new Runnable(exitingViews) {
                /* class android.app.$$Lambda$FragmentTransition$PZ32bJ_FSMpbzYzBl8x73NJPidQ */
                private final /* synthetic */ ArrayList f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    FragmentTransition.lambda$replaceHide$0(this.f$0);
                }
            });
            exitTransition.addListener(new TransitionListenerAdapter() {
                /* class android.app.FragmentTransition.AnonymousClass1 */

                @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                    View.this.setVisibility(8);
                    FragmentTransition.setViewVisibility(exitingViews, 0);
                }
            });
        }
    }

    private static void scheduleTargetChange(ViewGroup sceneRoot, Fragment inFragment, View nonExistentView, ArrayList<View> sharedElementsIn, Transition enterTransition, ArrayList<View> enteringViews, Transition exitTransition, ArrayList<View> exitingViews) {
        OneShotPreDrawListener.add(sceneRoot, new Runnable(nonExistentView, inFragment, sharedElementsIn, enteringViews, exitingViews, exitTransition) {
            /* class android.app.$$Lambda$FragmentTransition$8Ei4ls5jlZcfRvuLcweFAxtFBFs */
            private final /* synthetic */ View f$1;
            private final /* synthetic */ Fragment f$2;
            private final /* synthetic */ ArrayList f$3;
            private final /* synthetic */ ArrayList f$4;
            private final /* synthetic */ ArrayList f$5;
            private final /* synthetic */ Transition f$6;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
            }

            public final void run() {
                FragmentTransition.lambda$scheduleTargetChange$1(Transition.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
            }
        });
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
        TransitionSet sharedElementTransition;
        TransitionSet sharedElementTransition2;
        View epicenterView;
        Rect epicenter;
        Fragment inFragment = fragments.lastIn;
        Fragment outFragment = fragments.firstOut;
        if (inFragment != null) {
            if (inFragment.getView() != null) {
                inFragment.getView().setVisibility(0);
            } else {
                Slog.v("FragmentManager", inFragment + " get empty view");
            }
        }
        if (inFragment != null) {
            if (outFragment != null) {
                boolean inIsPop = fragments.lastInIsPop;
                if (nameOverrides.isEmpty()) {
                    sharedElementTransition = null;
                } else {
                    sharedElementTransition = getSharedElementTransition(inFragment, outFragment, inIsPop);
                }
                ArrayMap<String, View> outSharedElements = captureOutSharedElements(nameOverrides, sharedElementTransition, fragments);
                ArrayMap<String, View> inSharedElements = captureInSharedElements(nameOverrides, sharedElementTransition, fragments);
                if (nameOverrides.isEmpty()) {
                    if (outSharedElements != null) {
                        outSharedElements.clear();
                    }
                    if (inSharedElements != null) {
                        inSharedElements.clear();
                    }
                    sharedElementTransition2 = null;
                } else {
                    addSharedElementsWithMatchingNames(sharedElementsOut, outSharedElements, nameOverrides.keySet());
                    addSharedElementsWithMatchingNames(sharedElementsIn, inSharedElements, nameOverrides.values());
                    sharedElementTransition2 = sharedElementTransition;
                }
                if (enterTransition == null && exitTransition == null && sharedElementTransition2 == null) {
                    return null;
                }
                callSharedElementStartEnd(inFragment, outFragment, inIsPop, outSharedElements, true);
                if (sharedElementTransition2 != null) {
                    sharedElementsIn.add(nonExistentView);
                    setSharedElementTargets(sharedElementTransition2, nonExistentView, sharedElementsOut);
                    setOutEpicenter(sharedElementTransition2, exitTransition, outSharedElements, fragments.firstOutIsPop, fragments.firstOutTransaction);
                    final Rect epicenter2 = new Rect();
                    epicenterView = getInEpicenterView(inSharedElements, fragments, enterTransition, inIsPop);
                    if (epicenterView != null) {
                        enterTransition.setEpicenterCallback(new Transition.EpicenterCallback() {
                            /* class android.app.FragmentTransition.AnonymousClass2 */

                            @Override // android.transition.Transition.EpicenterCallback
                            public Rect onGetEpicenter(Transition transition) {
                                return Rect.this;
                            }
                        });
                    }
                    epicenter = epicenter2;
                } else {
                    epicenter = null;
                    epicenterView = null;
                }
                OneShotPreDrawListener.add(sceneRoot, new Runnable(outFragment, inIsPop, inSharedElements, epicenterView, epicenter) {
                    /* class android.app.$$Lambda$FragmentTransition$jurn0WXuKw3bRQ_2d5zCWdeZWuI */
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
                });
                return sharedElementTransition2;
            }
        }
        return null;
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
        TransitionSet sharedElementTransition;
        TransitionSet sharedElementTransition2;
        Rect inEpicenter;
        Fragment inFragment = fragments.lastIn;
        Fragment outFragment = fragments.firstOut;
        if (inFragment != null) {
            if (outFragment != null) {
                boolean inIsPop = fragments.lastInIsPop;
                if (nameOverrides.isEmpty()) {
                    sharedElementTransition = null;
                } else {
                    sharedElementTransition = getSharedElementTransition(inFragment, outFragment, inIsPop);
                }
                ArrayMap<String, View> outSharedElements = captureOutSharedElements(nameOverrides, sharedElementTransition, fragments);
                if (nameOverrides.isEmpty()) {
                    sharedElementTransition2 = null;
                } else {
                    sharedElementsOut.addAll(outSharedElements.values());
                    sharedElementTransition2 = sharedElementTransition;
                }
                if (enterTransition == null && exitTransition == null && sharedElementTransition2 == null) {
                    return null;
                }
                callSharedElementStartEnd(inFragment, outFragment, inIsPop, outSharedElements, true);
                if (sharedElementTransition2 != null) {
                    final Rect inEpicenter2 = new Rect();
                    setSharedElementTargets(sharedElementTransition2, nonExistentView, sharedElementsOut);
                    setOutEpicenter(sharedElementTransition2, exitTransition, outSharedElements, fragments.firstOutIsPop, fragments.firstOutTransaction);
                    if (enterTransition != null) {
                        enterTransition.setEpicenterCallback(new Transition.EpicenterCallback() {
                            /* class android.app.FragmentTransition.AnonymousClass3 */

                            @Override // android.transition.Transition.EpicenterCallback
                            public Rect onGetEpicenter(Transition transition) {
                                if (Rect.this.isEmpty()) {
                                    return null;
                                }
                                return Rect.this;
                            }
                        });
                    }
                    inEpicenter = inEpicenter2;
                } else {
                    inEpicenter = null;
                }
                OneShotPreDrawListener.add(sceneRoot, new Runnable(sharedElementTransition2, fragments, sharedElementsIn, nonExistentView, inFragment, outFragment, inIsPop, sharedElementsOut, enterTransition, inEpicenter) {
                    /* class android.app.$$Lambda$FragmentTransition$Ip0LktADPhG_3ouNBXgzufWpFfY */
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
                        FragmentTransition.lambda$configureSharedElementsOrdered$3(ArrayMap.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10);
                    }
                });
                return sharedElementTransition2;
            }
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
        String key;
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
                    String key2 = findKeyForValue(nameOverrides, name);
                    if (key2 != null) {
                        nameOverrides.remove(key2);
                    }
                } else if (!name.equals(view.getTransitionName()) && (key = findKeyForValue(nameOverrides, name)) != null) {
                    nameOverrides.put(key, view.getTransitionName());
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
                /* class android.app.FragmentTransition.AnonymousClass4 */

                @Override // android.transition.Transition.EpicenterCallback
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

    private static void scheduleRemoveTargets(Transition overalTransition, final Transition enterTransition, final ArrayList<View> enteringViews, final Transition exitTransition, final ArrayList<View> exitingViews, final TransitionSet sharedElementTransition, final ArrayList<View> sharedElementsIn) {
        overalTransition.addListener(new TransitionListenerAdapter() {
            /* class android.app.FragmentTransition.AnonymousClass5 */

            @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
            public void onTransitionStart(Transition transition) {
                Transition transition2 = Transition.this;
                if (transition2 != null) {
                    FragmentTransition.replaceTargets(transition2, enteringViews, null);
                }
                Transition transition3 = exitTransition;
                if (transition3 != null) {
                    FragmentTransition.replaceTargets(transition3, exitingViews, null);
                }
                TransitionSet transitionSet = sharedElementTransition;
                if (transitionSet != null) {
                    FragmentTransition.replaceTargets(transitionSet, sharedElementsIn, null);
                }
            }

            @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
            }
        });
    }

    public static void replaceTargets(Transition transition, ArrayList<View> oldTargets, ArrayList<View> newTargets) {
        List<View> targets;
        if (transition instanceof TransitionSet) {
            TransitionSet set = (TransitionSet) transition;
            int numTransitions = set.getTransitionCount();
            for (int i = 0; i < numTransitions; i++) {
                replaceTargets(set.getTransitionAt(i), oldTargets, newTargets);
            }
        } else if (!hasSimpleTarget(transition) && (targets = transition.getTargets()) != null && targets.size() == oldTargets.size() && targets.containsAll(oldTargets)) {
            int targetCount = newTargets == null ? 0 : newTargets.size();
            for (int i2 = 0; i2 < targetCount; i2++) {
                transition.addTarget(newTargets.get(i2));
            }
            for (int i3 = oldTargets.size() - 1; i3 >= 0; i3--) {
                transition.removeTarget(oldTargets.get(i3));
            }
        }
    }

    public static void addTargets(Transition transition, ArrayList<View> views) {
        if (transition != null) {
            if (transition instanceof TransitionSet) {
                TransitionSet set = (TransitionSet) transition;
                int numTransitions = set.getTransitionCount();
                for (int i = 0; i < numTransitions; i++) {
                    addTargets(set.getTransitionAt(i), views);
                }
            } else if (!hasSimpleTarget(transition) && isNullOrEmpty(transition.getTargets())) {
                int numViews = views.size();
                for (int i2 = 0; i2 < numViews; i2++) {
                    transition.addTarget(views.get(i2));
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

    /* JADX INFO: Multiple debug info for r1v1 'transition'  android.transition.Transition: [D('staggered' android.transition.Transition), D('transition' android.transition.Transition)] */
    /* JADX INFO: Multiple debug info for r2v0 android.transition.TransitionSet: [D('together' android.transition.TransitionSet), D('transition' android.transition.Transition)] */
    /* JADX INFO: Multiple debug info for r1v6 android.transition.TransitionSet: [D('transition' android.transition.Transition), D('transitionSet' android.transition.TransitionSet)] */
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

    /* JADX WARNING: Removed duplicated region for block: B:100:0x0130  */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0145  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x0149 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:113:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x00da A[ADDED_TO_REGION] */
    private static void addToFirstInLastOut(BackStackRecord transaction, BackStackRecord.Op op, SparseArray<FragmentContainerTransition> transitioningFragments, boolean isPop, boolean isReorderedTransaction) {
        int containerId;
        boolean setFirstOut;
        boolean wasRemoved;
        boolean wasAdded;
        boolean setLastIn;
        FragmentContainerTransition containerTransition;
        FragmentContainerTransition containerTransition2;
        Fragment fragment;
        FragmentContainerTransition containerTransition3;
        boolean setLastIn2;
        boolean setFirstOut2;
        boolean setFirstOut3;
        boolean setLastIn3;
        Fragment fragment2 = op.fragment;
        if (fragment2 != null && (containerId = fragment2.mContainerId) != 0) {
            int command = isPop ? INVERSE_OPS[op.cmd] : op.cmd;
            boolean z = false;
            if (command != 1) {
                if (command != 3) {
                    if (command == 4) {
                        if (isReorderedTransaction) {
                            if (fragment2.mHiddenChanged && fragment2.mAdded && fragment2.mHidden) {
                                z = true;
                            }
                            setFirstOut3 = z;
                        } else {
                            if (fragment2.mAdded && !fragment2.mHidden) {
                                z = true;
                            }
                            setFirstOut3 = z;
                        }
                        setLastIn = false;
                        wasRemoved = true;
                        setFirstOut = setFirstOut3;
                        wasAdded = false;
                    } else if (command == 5) {
                        if (isReorderedTransaction) {
                            if (fragment2.mHiddenChanged && !fragment2.mHidden && fragment2.mAdded) {
                                z = true;
                            }
                            setLastIn3 = z;
                        } else {
                            setLastIn3 = fragment2.mHidden;
                        }
                        setLastIn = setLastIn3;
                        wasRemoved = false;
                        setFirstOut = false;
                        wasAdded = true;
                    } else if (command != 6) {
                        if (command != 7) {
                            setLastIn = false;
                            wasRemoved = false;
                            setFirstOut = false;
                            wasAdded = false;
                        }
                    }
                    FragmentContainerTransition containerTransition4 = transitioningFragments.get(containerId);
                    if (setLastIn) {
                        FragmentContainerTransition containerTransition5 = ensureContainer(containerTransition4, transitioningFragments, containerId);
                        containerTransition5.lastIn = fragment2;
                        containerTransition5.lastInIsPop = isPop;
                        containerTransition5.lastInTransaction = transaction;
                        containerTransition = containerTransition5;
                    } else {
                        containerTransition = containerTransition4;
                    }
                    if (!isReorderedTransaction || !wasAdded) {
                        fragment = null;
                        containerTransition2 = containerTransition;
                    } else {
                        if (containerTransition != null && containerTransition.firstOut == fragment2) {
                            containerTransition.firstOut = null;
                        }
                        FragmentManagerImpl manager = transaction.mManager;
                        if (fragment2.mState >= 1 || manager.mCurState < 1) {
                            fragment = null;
                            containerTransition2 = containerTransition;
                        } else if (manager.mHost.getContext().getApplicationInfo().targetSdkVersion < 24 || transaction.mReorderingAllowed) {
                            fragment = null;
                            containerTransition2 = containerTransition;
                        } else {
                            manager.makeActive(fragment2);
                            containerTransition2 = containerTransition;
                            fragment = null;
                            manager.moveToState(fragment2, 1, 0, 0, false);
                        }
                    }
                    if (setFirstOut) {
                        containerTransition3 = containerTransition2;
                        if (containerTransition3 == null || containerTransition3.firstOut == null) {
                            FragmentContainerTransition containerTransition6 = ensureContainer(containerTransition3, transitioningFragments, containerId);
                            containerTransition6.firstOut = fragment2;
                            containerTransition6.firstOutIsPop = isPop;
                            containerTransition6.firstOutTransaction = transaction;
                            containerTransition3 = containerTransition6;
                        }
                    } else {
                        containerTransition3 = containerTransition2;
                    }
                    if (isReorderedTransaction && wasRemoved && containerTransition3 != null && containerTransition3.lastIn == fragment2) {
                        containerTransition3.lastIn = fragment;
                        return;
                    }
                    return;
                }
                if (isReorderedTransaction) {
                    if (!fragment2.mAdded && fragment2.mView != null && fragment2.mView.getVisibility() == 0 && fragment2.mView.getTransitionAlpha() > 0.0f) {
                        z = true;
                    }
                    setFirstOut2 = z;
                } else {
                    if (fragment2.mAdded && !fragment2.mHidden) {
                        z = true;
                    }
                    setFirstOut2 = z;
                }
                setLastIn = false;
                wasRemoved = true;
                setFirstOut = setFirstOut2;
                wasAdded = false;
                FragmentContainerTransition containerTransition42 = transitioningFragments.get(containerId);
                if (setLastIn) {
                }
                if (!isReorderedTransaction) {
                }
                fragment = null;
                containerTransition2 = containerTransition;
                if (setFirstOut) {
                }
                if (isReorderedTransaction) {
                    return;
                }
                return;
            }
            if (isReorderedTransaction) {
                setLastIn2 = fragment2.mIsNewlyAdded;
            } else {
                if (!fragment2.mAdded && !fragment2.mHidden) {
                    z = true;
                }
                setLastIn2 = z;
            }
            setLastIn = setLastIn2;
            wasRemoved = false;
            setFirstOut = false;
            wasAdded = true;
            FragmentContainerTransition containerTransition422 = transitioningFragments.get(containerId);
            if (setLastIn) {
            }
            if (!isReorderedTransaction) {
            }
            fragment = null;
            containerTransition2 = containerTransition;
            if (setFirstOut) {
            }
            if (isReorderedTransaction) {
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
