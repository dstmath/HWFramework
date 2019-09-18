package android.support.v4.app;

import android.graphics.Rect;
import android.os.Build;
import android.support.v4.app.BackStackRecord;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewCompat;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class FragmentTransition {
    private static final int[] INVERSE_OPS = {0, 3, 0, 1, 5, 4, 7, 6, 9, 8};
    private static final FragmentTransitionImpl PLATFORM_IMPL = (Build.VERSION.SDK_INT >= 21 ? new FragmentTransitionCompat21() : null);
    private static final FragmentTransitionImpl SUPPORT_IMPL = resolveSupportImpl();

    static class FragmentContainerTransition {
        public Fragment firstOut;
        public boolean firstOutIsPop;
        public BackStackRecord firstOutTransaction;
        public Fragment lastIn;
        public boolean lastInIsPop;
        public BackStackRecord lastInTransaction;

        FragmentContainerTransition() {
        }
    }

    private static FragmentTransitionImpl resolveSupportImpl() {
        try {
            return (FragmentTransitionImpl) Class.forName("android.support.transition.FragmentTransitionSupport").getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (Exception e) {
            return null;
        }
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

    /* JADX WARNING: type inference failed for: r2v9, types: [android.view.View] */
    /* JADX WARNING: Multi-variable type inference failed */
    private static void configureTransitionsReordered(FragmentManagerImpl fragmentManager, int containerId, FragmentContainerTransition fragments, View nonExistentView, ArrayMap<String, String> nameOverrides) {
        Object exitTransition;
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
            FragmentTransitionImpl impl = chooseImpl(outFragment, inFragment);
            if (impl != null) {
                boolean inIsPop = fragmentContainerTransition.lastInIsPop;
                boolean outIsPop = fragmentContainerTransition.firstOutIsPop;
                ArrayList<View> sharedElementsIn = new ArrayList<>();
                ArrayList<View> sharedElementsOut = new ArrayList<>();
                Object enterTransition = getEnterTransition(impl, inFragment, inIsPop);
                Object exitTransition2 = getExitTransition(impl, outFragment, outIsPop);
                ArrayList<View> sharedElementsOut2 = sharedElementsOut;
                ArrayList<View> sharedElementsIn2 = sharedElementsIn;
                boolean z = outIsPop;
                boolean inIsPop2 = inIsPop;
                Object enterTransition2 = enterTransition;
                FragmentTransitionImpl impl2 = impl;
                Object sharedElementTransition = configureSharedElementsReordered(impl, sceneRoot2, view, nameOverrides, fragmentContainerTransition, sharedElementsOut2, sharedElementsIn2, enterTransition2, exitTransition2);
                if (enterTransition2 == null && sharedElementTransition == null) {
                    exitTransition = exitTransition2;
                    if (exitTransition == null) {
                        return;
                    }
                } else {
                    exitTransition = exitTransition2;
                }
                ArrayList<View> sharedElementsOut3 = sharedElementsOut2;
                ArrayList<View> exitingViews = configureEnteringExitingViews(impl2, exitTransition, outFragment, sharedElementsOut3, view);
                ArrayList<View> sharedElementsIn3 = sharedElementsIn2;
                ArrayList<View> enteringViews = configureEnteringExitingViews(impl2, enterTransition2, inFragment, sharedElementsIn3, view);
                setViewVisibility(enteringViews, 4);
                ArrayList<View> enteringViews2 = enteringViews;
                ArrayList<View> sharedElementsIn4 = sharedElementsIn3;
                ArrayList<View> exitingViews2 = exitingViews;
                ArrayList<View> sharedElementsOut4 = sharedElementsOut3;
                Object transition = mergeTransitions(impl2, enterTransition2, exitTransition, sharedElementTransition, inFragment, inIsPop2);
                if (transition != null) {
                    replaceHide(impl2, exitTransition, outFragment, exitingViews2);
                    ArrayList<String> inNames = impl2.prepareSetNameOverridesReordered(sharedElementsIn4);
                    Object obj = exitTransition;
                    Object obj2 = enterTransition2;
                    impl2.scheduleRemoveTargets(transition, enterTransition2, enteringViews2, exitTransition, exitingViews2, sharedElementTransition, sharedElementsIn4);
                    impl2.beginDelayedTransition(sceneRoot2, transition);
                    impl2.setNameOverridesReordered(sceneRoot2, sharedElementsOut4, sharedElementsIn4, inNames, nameOverrides);
                    setViewVisibility(enteringViews2, 0);
                    impl2.swapSharedElementTargets(sharedElementTransition, sharedElementsOut4, sharedElementsIn4);
                } else {
                    Object obj3 = exitTransition;
                    Object obj4 = enterTransition2;
                    ArrayList<View> arrayList = enteringViews2;
                    ArrayList<View> arrayList2 = sharedElementsOut4;
                }
            }
        }
    }

    private static void replaceHide(FragmentTransitionImpl impl, Object exitTransition, Fragment exitingFragment, final ArrayList<View> exitingViews) {
        if (exitingFragment != null && exitTransition != null && exitingFragment.mAdded && exitingFragment.mHidden && exitingFragment.mHiddenChanged) {
            exitingFragment.setHideReplaced(true);
            impl.scheduleHideFragmentView(exitTransition, exitingFragment.getView(), exitingViews);
            OneShotPreDrawListener.add(exitingFragment.mContainer, new Runnable() {
                public void run() {
                    FragmentTransition.setViewVisibility(exitingViews, 4);
                }
            });
        }
    }

    /* JADX WARNING: type inference failed for: r2v8, types: [android.view.View] */
    /* JADX WARNING: Multi-variable type inference failed */
    private static void configureTransitionsOrdered(FragmentManagerImpl fragmentManager, int containerId, FragmentContainerTransition fragments, View nonExistentView, ArrayMap<String, String> nameOverrides) {
        Object exitTransition;
        FragmentManagerImpl fragmentManagerImpl = fragmentManager;
        FragmentContainerTransition fragmentContainerTransition = fragments;
        View view = nonExistentView;
        ArrayMap<String, String> arrayMap = nameOverrides;
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
            FragmentTransitionImpl impl = chooseImpl(outFragment, inFragment);
            if (impl != null) {
                boolean inIsPop = fragmentContainerTransition.lastInIsPop;
                boolean outIsPop = fragmentContainerTransition.firstOutIsPop;
                Object enterTransition = getEnterTransition(impl, inFragment, inIsPop);
                Object exitTransition2 = getExitTransition(impl, outFragment, outIsPop);
                ArrayList<View> sharedElementsOut = new ArrayList<>();
                ArrayList<View> sharedElementsIn = new ArrayList<>();
                ArrayList<View> sharedElementsOut2 = sharedElementsOut;
                Object exitTransition3 = exitTransition2;
                Object enterTransition2 = enterTransition;
                boolean z = outIsPop;
                boolean z2 = inIsPop;
                FragmentTransitionImpl impl2 = impl;
                Fragment outFragment2 = outFragment;
                Object sharedElementTransition = configureSharedElementsOrdered(impl, sceneRoot2, view, arrayMap, fragmentContainerTransition, sharedElementsOut2, sharedElementsIn, enterTransition2, exitTransition3);
                Object enterTransition3 = enterTransition2;
                if (enterTransition3 == null && sharedElementTransition == null) {
                    exitTransition = exitTransition3;
                    if (exitTransition == null) {
                        return;
                    }
                } else {
                    exitTransition = exitTransition3;
                }
                ArrayList<View> sharedElementsOut3 = sharedElementsOut2;
                ArrayList<View> exitingViews = configureEnteringExitingViews(impl2, exitTransition, outFragment2, sharedElementsOut3, view);
                if (exitingViews == null || exitingViews.isEmpty()) {
                    exitTransition = null;
                }
                Object exitTransition4 = exitTransition;
                impl2.addTarget(enterTransition3, view);
                Object transition = mergeTransitions(impl2, enterTransition3, exitTransition4, sharedElementTransition, inFragment, fragmentContainerTransition.lastInIsPop);
                if (transition != null) {
                    ArrayList arrayList = new ArrayList();
                    impl2.scheduleRemoveTargets(transition, enterTransition3, arrayList, exitTransition4, exitingViews, sharedElementTransition, sharedElementsIn);
                    ArrayList<View> arrayList2 = sharedElementsOut3;
                    scheduleTargetChange(impl2, sceneRoot2, inFragment, view, sharedElementsIn, enterTransition3, arrayList, exitTransition4, exitingViews);
                    ArrayList<View> sharedElementsIn2 = sharedElementsIn;
                    impl2.setNameOverridesOrdered(sceneRoot2, sharedElementsIn2, arrayMap);
                    impl2.beginDelayedTransition(sceneRoot2, transition);
                    impl2.scheduleNameReset(sceneRoot2, sharedElementsIn2, arrayMap);
                } else {
                    ArrayList<View> arrayList3 = exitingViews;
                    ArrayList<View> arrayList4 = sharedElementsOut3;
                    Object obj = enterTransition3;
                    ArrayList<View> arrayList5 = sharedElementsIn;
                }
            }
        }
    }

    private static void scheduleTargetChange(FragmentTransitionImpl impl, ViewGroup sceneRoot, Fragment inFragment, View nonExistentView, ArrayList<View> sharedElementsIn, Object enterTransition, ArrayList<View> enteringViews, Object exitTransition, ArrayList<View> exitingViews) {
        final Object obj = enterTransition;
        final FragmentTransitionImpl fragmentTransitionImpl = impl;
        final View view = nonExistentView;
        final Fragment fragment = inFragment;
        final ArrayList<View> arrayList = sharedElementsIn;
        final ArrayList<View> arrayList2 = enteringViews;
        final ArrayList<View> arrayList3 = exitingViews;
        final Object obj2 = exitTransition;
        AnonymousClass2 r0 = new Runnable() {
            public void run() {
                if (obj != null) {
                    fragmentTransitionImpl.removeTarget(obj, view);
                    arrayList2.addAll(FragmentTransition.configureEnteringExitingViews(fragmentTransitionImpl, obj, fragment, arrayList, view));
                }
                if (arrayList3 != null) {
                    if (obj2 != null) {
                        ArrayList<View> tempExiting = new ArrayList<>();
                        tempExiting.add(view);
                        fragmentTransitionImpl.replaceTargets(obj2, arrayList3, tempExiting);
                    }
                    arrayList3.clear();
                    arrayList3.add(view);
                }
            }
        };
        OneShotPreDrawListener.add(sceneRoot, r0);
    }

    private static FragmentTransitionImpl chooseImpl(Fragment outFragment, Fragment inFragment) {
        ArrayList<Object> transitions = new ArrayList<>();
        if (outFragment != null) {
            Object exitTransition = outFragment.getExitTransition();
            if (exitTransition != null) {
                transitions.add(exitTransition);
            }
            Object returnTransition = outFragment.getReturnTransition();
            if (returnTransition != null) {
                transitions.add(returnTransition);
            }
            Object sharedReturnTransition = outFragment.getSharedElementReturnTransition();
            if (sharedReturnTransition != null) {
                transitions.add(sharedReturnTransition);
            }
        }
        if (inFragment != null) {
            Object enterTransition = inFragment.getEnterTransition();
            if (enterTransition != null) {
                transitions.add(enterTransition);
            }
            Object reenterTransition = inFragment.getReenterTransition();
            if (reenterTransition != null) {
                transitions.add(reenterTransition);
            }
            Object sharedEnterTransition = inFragment.getSharedElementEnterTransition();
            if (sharedEnterTransition != null) {
                transitions.add(sharedEnterTransition);
            }
        }
        if (transitions.isEmpty()) {
            return null;
        }
        if (PLATFORM_IMPL != null && canHandleAll(PLATFORM_IMPL, transitions)) {
            return PLATFORM_IMPL;
        }
        if (SUPPORT_IMPL != null && canHandleAll(SUPPORT_IMPL, transitions)) {
            return SUPPORT_IMPL;
        }
        if (PLATFORM_IMPL == null && SUPPORT_IMPL == null) {
            return null;
        }
        throw new IllegalArgumentException("Invalid Transition types");
    }

    private static boolean canHandleAll(FragmentTransitionImpl impl, List<Object> transitions) {
        int size = transitions.size();
        for (int i = 0; i < size; i++) {
            if (!impl.canHandle(transitions.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static Object getSharedElementTransition(FragmentTransitionImpl impl, Fragment inFragment, Fragment outFragment, boolean isPop) {
        Object obj;
        if (inFragment == null || outFragment == null) {
            return null;
        }
        if (isPop) {
            obj = outFragment.getSharedElementReturnTransition();
        } else {
            obj = inFragment.getSharedElementEnterTransition();
        }
        return impl.wrapTransitionInSet(impl.cloneTransition(obj));
    }

    private static Object getEnterTransition(FragmentTransitionImpl impl, Fragment inFragment, boolean isPop) {
        Object obj;
        if (inFragment == null) {
            return null;
        }
        if (isPop) {
            obj = inFragment.getReenterTransition();
        } else {
            obj = inFragment.getEnterTransition();
        }
        return impl.cloneTransition(obj);
    }

    private static Object getExitTransition(FragmentTransitionImpl impl, Fragment outFragment, boolean isPop) {
        Object obj;
        if (outFragment == null) {
            return null;
        }
        if (isPop) {
            obj = outFragment.getReturnTransition();
        } else {
            obj = outFragment.getExitTransition();
        }
        return impl.cloneTransition(obj);
    }

    private static Object configureSharedElementsReordered(FragmentTransitionImpl impl, ViewGroup sceneRoot, View nonExistentView, ArrayMap<String, String> nameOverrides, FragmentContainerTransition fragments, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn, Object enterTransition, Object exitTransition) {
        Object sharedElementTransition;
        Rect epicenter;
        ArrayMap<String, View> inSharedElements;
        final View epicenterView;
        FragmentTransitionImpl fragmentTransitionImpl = impl;
        View view = nonExistentView;
        ArrayMap<String, String> arrayMap = nameOverrides;
        FragmentContainerTransition fragmentContainerTransition = fragments;
        ArrayList<View> arrayList = sharedElementsOut;
        ArrayList<View> arrayList2 = sharedElementsIn;
        Object obj = enterTransition;
        Fragment inFragment = fragmentContainerTransition.lastIn;
        Fragment outFragment = fragmentContainerTransition.firstOut;
        if (inFragment != null) {
            inFragment.getView().setVisibility(0);
        }
        if (inFragment == null) {
            ViewGroup viewGroup = sceneRoot;
            Fragment fragment = outFragment;
        } else if (outFragment == null) {
            ViewGroup viewGroup2 = sceneRoot;
            Fragment fragment2 = outFragment;
        } else {
            boolean inIsPop = fragmentContainerTransition.lastInIsPop;
            Object sharedElementTransition2 = nameOverrides.isEmpty() ? null : getSharedElementTransition(fragmentTransitionImpl, inFragment, outFragment, inIsPop);
            ArrayMap<String, View> outSharedElements = captureOutSharedElements(fragmentTransitionImpl, arrayMap, sharedElementTransition2, fragmentContainerTransition);
            ArrayMap<String, View> inSharedElements2 = captureInSharedElements(fragmentTransitionImpl, arrayMap, sharedElementTransition2, fragmentContainerTransition);
            if (nameOverrides.isEmpty()) {
                sharedElementTransition2 = null;
                if (outSharedElements != null) {
                    outSharedElements.clear();
                }
                if (inSharedElements2 != null) {
                    inSharedElements2.clear();
                }
            } else {
                addSharedElementsWithMatchingNames(arrayList, outSharedElements, nameOverrides.keySet());
                addSharedElementsWithMatchingNames(arrayList2, inSharedElements2, nameOverrides.values());
            }
            Object sharedElementTransition3 = sharedElementTransition2;
            if (obj == null && exitTransition == null && sharedElementTransition3 == null) {
                return null;
            }
            callSharedElementStartEnd(inFragment, outFragment, inIsPop, outSharedElements, true);
            if (sharedElementTransition3 != null) {
                arrayList2.add(view);
                fragmentTransitionImpl.setSharedElementTargets(sharedElementTransition3, view, arrayList);
                sharedElementTransition = sharedElementTransition3;
                inSharedElements = inSharedElements2;
                ArrayMap<String, View> arrayMap2 = outSharedElements;
                setOutEpicenter(fragmentTransitionImpl, sharedElementTransition3, exitTransition, outSharedElements, fragmentContainerTransition.firstOutIsPop, fragmentContainerTransition.firstOutTransaction);
                Rect epicenter2 = new Rect();
                View epicenterView2 = getInEpicenterView(inSharedElements, fragmentContainerTransition, obj, inIsPop);
                if (epicenterView2 != null) {
                    fragmentTransitionImpl.setEpicenter(obj, epicenter2);
                }
                epicenter = epicenter2;
                epicenterView = epicenterView2;
            } else {
                sharedElementTransition = sharedElementTransition3;
                inSharedElements = inSharedElements2;
                ArrayMap<String, View> arrayMap3 = outSharedElements;
                epicenterView = null;
                epicenter = null;
            }
            final Fragment fragment3 = inFragment;
            final Fragment fragment4 = outFragment;
            final boolean z = inIsPop;
            AnonymousClass3 r10 = r0;
            final ArrayMap<String, View> arrayMap4 = inSharedElements;
            boolean z2 = inIsPop;
            final FragmentTransitionImpl fragmentTransitionImpl2 = fragmentTransitionImpl;
            Fragment fragment5 = outFragment;
            final Rect rect = epicenter;
            AnonymousClass3 r0 = new Runnable() {
                public void run() {
                    FragmentTransition.callSharedElementStartEnd(fragment3, fragment4, z, arrayMap4, false);
                    if (epicenterView != null) {
                        fragmentTransitionImpl2.getBoundsOnScreen(epicenterView, rect);
                    }
                }
            };
            OneShotPreDrawListener.add(sceneRoot, r10);
            return sharedElementTransition;
        }
        return null;
    }

    private static void addSharedElementsWithMatchingNames(ArrayList<View> views, ArrayMap<String, View> sharedElements, Collection<String> nameOverridesSet) {
        for (int i = sharedElements.size() - 1; i >= 0; i--) {
            View view = sharedElements.valueAt(i);
            if (nameOverridesSet.contains(ViewCompat.getTransitionName(view))) {
                views.add(view);
            }
        }
    }

    private static Object configureSharedElementsOrdered(FragmentTransitionImpl impl, ViewGroup sceneRoot, View nonExistentView, ArrayMap<String, String> nameOverrides, FragmentContainerTransition fragments, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn, Object enterTransition, Object exitTransition) {
        ArrayMap<String, View> outSharedElements;
        Rect inEpicenter;
        FragmentTransitionImpl fragmentTransitionImpl = impl;
        FragmentContainerTransition fragmentContainerTransition = fragments;
        ArrayList<View> arrayList = sharedElementsOut;
        Object obj = enterTransition;
        Fragment inFragment = fragmentContainerTransition.lastIn;
        Fragment outFragment = fragmentContainerTransition.firstOut;
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
            Object sharedElementTransition = nameOverrides.isEmpty() ? null : getSharedElementTransition(fragmentTransitionImpl, inFragment, outFragment, inIsPop);
            ArrayMap<String, String> arrayMap = nameOverrides;
            ArrayMap<String, View> outSharedElements2 = captureOutSharedElements(fragmentTransitionImpl, arrayMap, sharedElementTransition, fragmentContainerTransition);
            if (nameOverrides.isEmpty()) {
                sharedElementTransition = null;
            } else {
                arrayList.addAll(outSharedElements2.values());
            }
            Object sharedElementTransition2 = sharedElementTransition;
            if (obj == null && exitTransition == null && sharedElementTransition2 == null) {
                return null;
            }
            callSharedElementStartEnd(inFragment, outFragment, inIsPop, outSharedElements2, true);
            if (sharedElementTransition2 != null) {
                Rect inEpicenter2 = new Rect();
                fragmentTransitionImpl.setSharedElementTargets(sharedElementTransition2, nonExistentView, arrayList);
                outSharedElements = outSharedElements2;
                inEpicenter = inEpicenter2;
                setOutEpicenter(fragmentTransitionImpl, sharedElementTransition2, exitTransition, outSharedElements2, fragmentContainerTransition.firstOutIsPop, fragmentContainerTransition.firstOutTransaction);
                if (obj != null) {
                    fragmentTransitionImpl.setEpicenter(obj, inEpicenter);
                }
            } else {
                outSharedElements = outSharedElements2;
                inEpicenter = null;
            }
            Object sharedElementTransition3 = sharedElementTransition2;
            final Rect inEpicenter3 = inEpicenter;
            final Object finalSharedElementTransition = sharedElementTransition3;
            final FragmentTransitionImpl fragmentTransitionImpl2 = fragmentTransitionImpl;
            final ArrayMap<String, String> arrayMap2 = arrayMap;
            final FragmentContainerTransition fragmentContainerTransition2 = fragmentContainerTransition;
            final ArrayList<View> arrayList2 = sharedElementsIn;
            AnonymousClass4 r13 = r0;
            ArrayMap<String, View> arrayMap3 = outSharedElements;
            final View view = nonExistentView;
            final Fragment fragment5 = inFragment;
            boolean inIsPop2 = inIsPop;
            final Fragment fragment6 = outFragment;
            Fragment fragment7 = outFragment;
            final boolean z = inIsPop2;
            Fragment fragment8 = inFragment;
            final ArrayList<View> arrayList3 = arrayList;
            final Object obj2 = enterTransition;
            AnonymousClass4 r0 = new Runnable() {
                public void run() {
                    ArrayMap<String, View> inSharedElements = FragmentTransition.captureInSharedElements(fragmentTransitionImpl2, arrayMap2, finalSharedElementTransition, fragmentContainerTransition2);
                    if (inSharedElements != null) {
                        arrayList2.addAll(inSharedElements.values());
                        arrayList2.add(view);
                    }
                    FragmentTransition.callSharedElementStartEnd(fragment5, fragment6, z, inSharedElements, false);
                    if (finalSharedElementTransition != null) {
                        fragmentTransitionImpl2.swapSharedElementTargets(finalSharedElementTransition, arrayList3, arrayList2);
                        View inEpicenterView = FragmentTransition.getInEpicenterView(inSharedElements, fragmentContainerTransition2, obj2, z);
                        if (inEpicenterView != null) {
                            fragmentTransitionImpl2.getBoundsOnScreen(inEpicenterView, inEpicenter3);
                        }
                    }
                }
            };
            OneShotPreDrawListener.add(sceneRoot, r13);
            return sharedElementTransition3;
        }
        return null;
    }

    private static ArrayMap<String, View> captureOutSharedElements(FragmentTransitionImpl impl, ArrayMap<String, String> nameOverrides, Object sharedElementTransition, FragmentContainerTransition fragments) {
        ArrayList<String> names;
        SharedElementCallback sharedElementCallback;
        if (nameOverrides.isEmpty() || sharedElementTransition == null) {
            nameOverrides.clear();
            return null;
        }
        Fragment outFragment = fragments.firstOut;
        ArrayMap<String, View> outSharedElements = new ArrayMap<>();
        impl.findNamedViews(outSharedElements, outFragment.getView());
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
                } else if (!name.equals(ViewCompat.getTransitionName(view))) {
                    nameOverrides.put(ViewCompat.getTransitionName(view), nameOverrides.remove(name));
                }
            }
        } else {
            nameOverrides.retainAll(outSharedElements.keySet());
        }
        return outSharedElements;
    }

    /* access modifiers changed from: private */
    public static ArrayMap<String, View> captureInSharedElements(FragmentTransitionImpl impl, ArrayMap<String, String> nameOverrides, Object sharedElementTransition, FragmentContainerTransition fragments) {
        ArrayList<String> names;
        SharedElementCallback sharedElementCallback;
        Fragment inFragment = fragments.lastIn;
        View fragmentView = inFragment.getView();
        if (nameOverrides.isEmpty() || sharedElementTransition == null || fragmentView == null) {
            nameOverrides.clear();
            return null;
        }
        ArrayMap<String, View> inSharedElements = new ArrayMap<>();
        impl.findNamedViews(inSharedElements, fragmentView);
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
            inSharedElements.retainAll(nameOverrides.values());
        }
        if (sharedElementCallback != null) {
            sharedElementCallback.onMapSharedElements(names, inSharedElements);
            for (int i = names.size() - 1; i >= 0; i--) {
                String name = names.get(i);
                View view = inSharedElements.get(name);
                if (view == null) {
                    String key = findKeyForValue(nameOverrides, name);
                    if (key != null) {
                        nameOverrides.remove(key);
                    }
                } else if (!name.equals(ViewCompat.getTransitionName(view))) {
                    String key2 = findKeyForValue(nameOverrides, name);
                    if (key2 != null) {
                        nameOverrides.put(key2, ViewCompat.getTransitionName(view));
                    }
                }
            }
        } else {
            retainValues(nameOverrides, inSharedElements);
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

    /* access modifiers changed from: private */
    public static View getInEpicenterView(ArrayMap<String, View> inSharedElements, FragmentContainerTransition fragments, Object enterTransition, boolean inIsPop) {
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

    private static void setOutEpicenter(FragmentTransitionImpl impl, Object sharedElementTransition, Object exitTransition, ArrayMap<String, View> outSharedElements, boolean outIsPop, BackStackRecord outTransaction) {
        String sourceName;
        if (outTransaction.mSharedElementSourceNames != null && !outTransaction.mSharedElementSourceNames.isEmpty()) {
            if (outIsPop) {
                sourceName = outTransaction.mSharedElementTargetNames.get(0);
            } else {
                sourceName = outTransaction.mSharedElementSourceNames.get(0);
            }
            View outEpicenterView = outSharedElements.get(sourceName);
            impl.setEpicenter(sharedElementTransition, outEpicenterView);
            if (exitTransition != null) {
                impl.setEpicenter(exitTransition, outEpicenterView);
            }
        }
    }

    private static void retainValues(ArrayMap<String, String> nameOverrides, ArrayMap<String, View> namedViews) {
        for (int i = nameOverrides.size() - 1; i >= 0; i--) {
            if (!namedViews.containsKey(nameOverrides.valueAt(i))) {
                nameOverrides.removeAt(i);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void callSharedElementStartEnd(Fragment inFragment, Fragment outFragment, boolean isPop, ArrayMap<String, View> sharedElements, boolean isStart) {
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

    /* access modifiers changed from: private */
    public static ArrayList<View> configureEnteringExitingViews(FragmentTransitionImpl impl, Object transition, Fragment fragment, ArrayList<View> sharedElements, View nonExistentView) {
        ArrayList<View> viewList = null;
        if (transition != null) {
            viewList = new ArrayList<>();
            View root = fragment.getView();
            if (root != null) {
                impl.captureTransitioningViews(viewList, root);
            }
            if (sharedElements != null) {
                viewList.removeAll(sharedElements);
            }
            if (!viewList.isEmpty()) {
                viewList.add(nonExistentView);
                impl.addTargets(transition, viewList);
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

    private static Object mergeTransitions(FragmentTransitionImpl impl, Object enterTransition, Object exitTransition, Object sharedElementTransition, Fragment inFragment, boolean isPop) {
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
            return impl.mergeTransitionsTogether(exitTransition, enterTransition, sharedElementTransition);
        }
        return impl.mergeTransitionsInSequence(exitTransition, enterTransition, sharedElementTransition);
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

    static boolean supportsTransition() {
        return (PLATFORM_IMPL == null && SUPPORT_IMPL == null) ? false : true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:86:0x00f2  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x0106  */
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
                                if (!fragment2.mAdded && fragment2.mView != null && fragment2.mView.getVisibility() == 0 && fragment2.mPostponedAlpha >= 0.0f) {
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
                    if (fragment2.mState < 1 && manager.mCurState >= 1 && !backStackRecord.mReorderingAllowed) {
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

    private FragmentTransition() {
    }
}
