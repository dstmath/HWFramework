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

/* access modifiers changed from: package-private */
public class FragmentTransition {
    private static final int[] INVERSE_OPS = {0, 3, 0, 1, 5, 4, 7, 6, 9, 8};
    private static final FragmentTransitionImpl PLATFORM_IMPL = (Build.VERSION.SDK_INT >= 21 ? new FragmentTransitionCompat21() : null);
    private static final FragmentTransitionImpl SUPPORT_IMPL = resolveSupportImpl();

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

    private static void configureTransitionsReordered(FragmentManagerImpl fragmentManager, int containerId, FragmentContainerTransition fragments, View nonExistentView, ArrayMap<String, String> nameOverrides) {
        Fragment inFragment;
        Fragment outFragment;
        FragmentTransitionImpl impl;
        Object exitTransition;
        ViewGroup sceneRoot = null;
        if (fragmentManager.mContainer.onHasView()) {
            sceneRoot = (ViewGroup) fragmentManager.mContainer.onFindViewById(containerId);
        }
        if (sceneRoot != null && (impl = chooseImpl((outFragment = fragments.firstOut), (inFragment = fragments.lastIn))) != null) {
            boolean inIsPop = fragments.lastInIsPop;
            boolean outIsPop = fragments.firstOutIsPop;
            ArrayList<View> sharedElementsIn = new ArrayList<>();
            ArrayList<View> sharedElementsOut = new ArrayList<>();
            Object enterTransition = getEnterTransition(impl, inFragment, inIsPop);
            Object exitTransition2 = getExitTransition(impl, outFragment, outIsPop);
            Object sharedElementTransition = configureSharedElementsReordered(impl, sceneRoot, nonExistentView, nameOverrides, fragments, sharedElementsOut, sharedElementsIn, enterTransition, exitTransition2);
            if (enterTransition == null && sharedElementTransition == null) {
                exitTransition = exitTransition2;
                if (exitTransition == null) {
                    return;
                }
            } else {
                exitTransition = exitTransition2;
            }
            ArrayList<View> exitingViews = configureEnteringExitingViews(impl, exitTransition, outFragment, sharedElementsOut, nonExistentView);
            ArrayList<View> enteringViews = configureEnteringExitingViews(impl, enterTransition, inFragment, sharedElementsIn, nonExistentView);
            setViewVisibility(enteringViews, 4);
            Object transition = mergeTransitions(impl, enterTransition, exitTransition, sharedElementTransition, inFragment, inIsPop);
            if (transition != null) {
                replaceHide(impl, exitTransition, outFragment, exitingViews);
                ArrayList<String> inNames = impl.prepareSetNameOverridesReordered(sharedElementsIn);
                impl.scheduleRemoveTargets(transition, enterTransition, enteringViews, exitTransition, exitingViews, sharedElementTransition, sharedElementsIn);
                impl.beginDelayedTransition(sceneRoot, transition);
                impl.setNameOverridesReordered(sceneRoot, sharedElementsOut, sharedElementsIn, inNames, nameOverrides);
                setViewVisibility(enteringViews, 0);
                impl.swapSharedElementTargets(sharedElementTransition, sharedElementsOut, sharedElementsIn);
            }
        }
    }

    private static void replaceHide(FragmentTransitionImpl impl, Object exitTransition, Fragment exitingFragment, final ArrayList<View> exitingViews) {
        if (exitingFragment != null && exitTransition != null && exitingFragment.mAdded && exitingFragment.mHidden && exitingFragment.mHiddenChanged) {
            exitingFragment.setHideReplaced(true);
            impl.scheduleHideFragmentView(exitTransition, exitingFragment.getView(), exitingViews);
            OneShotPreDrawListener.add(exitingFragment.mContainer, new Runnable() {
                /* class android.support.v4.app.FragmentTransition.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    FragmentTransition.setViewVisibility(exitingViews, 4);
                }
            });
        }
    }

    private static void configureTransitionsOrdered(FragmentManagerImpl fragmentManager, int containerId, FragmentContainerTransition fragments, View nonExistentView, ArrayMap<String, String> nameOverrides) {
        Fragment inFragment;
        Fragment outFragment;
        FragmentTransitionImpl impl;
        Object exitTransition;
        ViewGroup sceneRoot = null;
        if (fragmentManager.mContainer.onHasView()) {
            sceneRoot = (ViewGroup) fragmentManager.mContainer.onFindViewById(containerId);
        }
        if (sceneRoot != null && (impl = chooseImpl((outFragment = fragments.firstOut), (inFragment = fragments.lastIn))) != null) {
            boolean inIsPop = fragments.lastInIsPop;
            boolean outIsPop = fragments.firstOutIsPop;
            Object enterTransition = getEnterTransition(impl, inFragment, inIsPop);
            Object exitTransition2 = getExitTransition(impl, outFragment, outIsPop);
            ArrayList<View> sharedElementsOut = new ArrayList<>();
            ArrayList<View> sharedElementsIn = new ArrayList<>();
            Object sharedElementTransition = configureSharedElementsOrdered(impl, sceneRoot, nonExistentView, nameOverrides, fragments, sharedElementsOut, sharedElementsIn, enterTransition, exitTransition2);
            if (enterTransition == null && sharedElementTransition == null) {
                exitTransition = exitTransition2;
                if (exitTransition == null) {
                    return;
                }
            } else {
                exitTransition = exitTransition2;
            }
            ArrayList<View> exitingViews = configureEnteringExitingViews(impl, exitTransition, outFragment, sharedElementsOut, nonExistentView);
            if (exitingViews == null || exitingViews.isEmpty()) {
                exitTransition = null;
            }
            impl.addTarget(enterTransition, nonExistentView);
            Object transition = mergeTransitions(impl, enterTransition, exitTransition, sharedElementTransition, inFragment, fragments.lastInIsPop);
            if (transition != null) {
                ArrayList<View> enteringViews = new ArrayList<>();
                impl.scheduleRemoveTargets(transition, enterTransition, enteringViews, exitTransition, exitingViews, sharedElementTransition, sharedElementsIn);
                scheduleTargetChange(impl, sceneRoot, inFragment, nonExistentView, sharedElementsIn, enterTransition, enteringViews, exitTransition, exitingViews);
                impl.setNameOverridesOrdered(sceneRoot, sharedElementsIn, nameOverrides);
                impl.beginDelayedTransition(sceneRoot, transition);
                impl.scheduleNameReset(sceneRoot, sharedElementsIn, nameOverrides);
            }
        }
    }

    private static void scheduleTargetChange(final FragmentTransitionImpl impl, ViewGroup sceneRoot, final Fragment inFragment, final View nonExistentView, final ArrayList<View> sharedElementsIn, final Object enterTransition, final ArrayList<View> enteringViews, final Object exitTransition, final ArrayList<View> exitingViews) {
        OneShotPreDrawListener.add(sceneRoot, new Runnable() {
            /* class android.support.v4.app.FragmentTransition.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                if (enterTransition != null) {
                    impl.removeTarget(enterTransition, nonExistentView);
                    enteringViews.addAll(FragmentTransition.configureEnteringExitingViews(impl, enterTransition, inFragment, sharedElementsIn, nonExistentView));
                }
                if (exitingViews != null) {
                    if (exitTransition != null) {
                        ArrayList<View> tempExiting = new ArrayList<>();
                        tempExiting.add(nonExistentView);
                        impl.replaceTargets(exitTransition, exitingViews, tempExiting);
                    }
                    exitingViews.clear();
                    exitingViews.add(nonExistentView);
                }
            }
        });
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

    private static Object configureSharedElementsReordered(final FragmentTransitionImpl impl, ViewGroup sceneRoot, View nonExistentView, ArrayMap<String, String> nameOverrides, FragmentContainerTransition fragments, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn, Object enterTransition, Object exitTransition) {
        Object sharedElementTransition;
        final Rect epicenter;
        final ArrayMap<String, View> inSharedElements;
        final View epicenterView;
        final Fragment inFragment = fragments.lastIn;
        final Fragment outFragment = fragments.firstOut;
        if (inFragment != null) {
            inFragment.getView().setVisibility(0);
        }
        if (inFragment != null) {
            if (outFragment != null) {
                final boolean inIsPop = fragments.lastInIsPop;
                Object sharedElementTransition2 = nameOverrides.isEmpty() ? null : getSharedElementTransition(impl, inFragment, outFragment, inIsPop);
                ArrayMap<String, View> outSharedElements = captureOutSharedElements(impl, nameOverrides, sharedElementTransition2, fragments);
                ArrayMap<String, View> inSharedElements2 = captureInSharedElements(impl, nameOverrides, sharedElementTransition2, fragments);
                if (nameOverrides.isEmpty()) {
                    sharedElementTransition2 = null;
                    if (outSharedElements != null) {
                        outSharedElements.clear();
                    }
                    if (inSharedElements2 != null) {
                        inSharedElements2.clear();
                    }
                } else {
                    addSharedElementsWithMatchingNames(sharedElementsOut, outSharedElements, nameOverrides.keySet());
                    addSharedElementsWithMatchingNames(sharedElementsIn, inSharedElements2, nameOverrides.values());
                }
                if (enterTransition == null && exitTransition == null && sharedElementTransition2 == null) {
                    return null;
                }
                callSharedElementStartEnd(inFragment, outFragment, inIsPop, outSharedElements, true);
                if (sharedElementTransition2 != null) {
                    sharedElementsIn.add(nonExistentView);
                    impl.setSharedElementTargets(sharedElementTransition2, nonExistentView, sharedElementsOut);
                    sharedElementTransition = sharedElementTransition2;
                    inSharedElements = inSharedElements2;
                    setOutEpicenter(impl, sharedElementTransition2, exitTransition, outSharedElements, fragments.firstOutIsPop, fragments.firstOutTransaction);
                    Rect epicenter2 = new Rect();
                    View epicenterView2 = getInEpicenterView(inSharedElements, fragments, enterTransition, inIsPop);
                    if (epicenterView2 != null) {
                        impl.setEpicenter(enterTransition, epicenter2);
                    }
                    epicenter = epicenter2;
                    epicenterView = epicenterView2;
                } else {
                    sharedElementTransition = sharedElementTransition2;
                    inSharedElements = inSharedElements2;
                    epicenterView = null;
                    epicenter = null;
                }
                OneShotPreDrawListener.add(sceneRoot, new Runnable() {
                    /* class android.support.v4.app.FragmentTransition.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        FragmentTransition.callSharedElementStartEnd(inFragment, outFragment, inIsPop, inSharedElements, false);
                        if (epicenterView != null) {
                            impl.getBoundsOnScreen(epicenterView, epicenter);
                        }
                    }
                });
                return sharedElementTransition;
            }
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

    /* JADX INFO: Multiple debug info for r6v5 'inEpicenter'  android.graphics.Rect: [D('inEpicenter' android.graphics.Rect), D('outSharedElements' android.support.v4.util.ArrayMap<java.lang.String, android.view.View>)] */
    private static Object configureSharedElementsOrdered(final FragmentTransitionImpl impl, ViewGroup sceneRoot, final View nonExistentView, final ArrayMap<String, String> nameOverrides, final FragmentContainerTransition fragments, final ArrayList<View> sharedElementsOut, final ArrayList<View> sharedElementsIn, final Object enterTransition, Object exitTransition) {
        ArrayMap<String, View> outSharedElements;
        final Rect inEpicenter;
        final Fragment inFragment = fragments.lastIn;
        final Fragment outFragment = fragments.firstOut;
        if (inFragment != null) {
            if (outFragment != null) {
                final boolean inIsPop = fragments.lastInIsPop;
                final Object sharedElementTransition = nameOverrides.isEmpty() ? null : getSharedElementTransition(impl, inFragment, outFragment, inIsPop);
                ArrayMap<String, View> outSharedElements2 = captureOutSharedElements(impl, nameOverrides, sharedElementTransition, fragments);
                if (nameOverrides.isEmpty()) {
                    sharedElementTransition = null;
                } else {
                    sharedElementsOut.addAll(outSharedElements2.values());
                }
                if (enterTransition == null && exitTransition == null && sharedElementTransition == null) {
                    return null;
                }
                callSharedElementStartEnd(inFragment, outFragment, inIsPop, outSharedElements2, true);
                if (sharedElementTransition != null) {
                    Rect inEpicenter2 = new Rect();
                    impl.setSharedElementTargets(sharedElementTransition, nonExistentView, sharedElementsOut);
                    outSharedElements = outSharedElements2;
                    inEpicenter = inEpicenter2;
                    setOutEpicenter(impl, sharedElementTransition, exitTransition, outSharedElements2, fragments.firstOutIsPop, fragments.firstOutTransaction);
                    if (enterTransition != null) {
                        impl.setEpicenter(enterTransition, inEpicenter);
                    }
                } else {
                    outSharedElements = outSharedElements2;
                    inEpicenter = null;
                }
                OneShotPreDrawListener.add(sceneRoot, new Runnable() {
                    /* class android.support.v4.app.FragmentTransition.AnonymousClass4 */

                    @Override // java.lang.Runnable
                    public void run() {
                        ArrayMap<String, View> inSharedElements = FragmentTransition.captureInSharedElements(impl, nameOverrides, sharedElementTransition, fragments);
                        if (inSharedElements != null) {
                            sharedElementsIn.addAll(inSharedElements.values());
                            sharedElementsIn.add(nonExistentView);
                        }
                        FragmentTransition.callSharedElementStartEnd(inFragment, outFragment, inIsPop, inSharedElements, false);
                        if (sharedElementTransition != null) {
                            impl.swapSharedElementTargets(sharedElementTransition, sharedElementsOut, sharedElementsIn);
                            View inEpicenterView = FragmentTransition.getInEpicenterView(inSharedElements, fragments, enterTransition, inIsPop);
                            if (inEpicenterView != null) {
                                impl.getBoundsOnScreen(inEpicenterView, inEpicenter);
                            }
                        }
                    }
                });
                return sharedElementTransition;
            }
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
        String key;
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
                    String key2 = findKeyForValue(nameOverrides, name);
                    if (key2 != null) {
                        nameOverrides.remove(key2);
                    }
                } else if (!name.equals(ViewCompat.getTransitionName(view)) && (key = findKeyForValue(nameOverrides, name)) != null) {
                    nameOverrides.put(key, ViewCompat.getTransitionName(view));
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

    /* JADX WARNING: Removed duplicated region for block: B:100:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00aa  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x00f2  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x0106  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x010b A[ADDED_TO_REGION] */
    private static void addToFirstInLastOut(BackStackRecord transaction, BackStackRecord.Op op, SparseArray<FragmentContainerTransition> transitioningFragments, boolean isPop, boolean isReorderedTransaction) {
        int containerId;
        FragmentContainerTransition containerTransition;
        FragmentContainerTransition containerTransition2;
        Fragment fragment;
        FragmentContainerTransition containerTransition3;
        FragmentContainerTransition containerTransition4;
        FragmentManagerImpl manager;
        Fragment fragment2 = op.fragment;
        if (fragment2 != null && (containerId = fragment2.mContainerId) != 0) {
            int command = isPop ? INVERSE_OPS[op.cmd] : op.cmd;
            boolean setLastIn = false;
            boolean wasRemoved = false;
            boolean setFirstOut = false;
            boolean wasAdded = false;
            boolean z = false;
            if (command != 1) {
                switch (command) {
                    case 3:
                    case 6:
                        if (isReorderedTransaction) {
                            if (!fragment2.mAdded && fragment2.mView != null && fragment2.mView.getVisibility() == 0 && fragment2.mPostponedAlpha >= 0.0f) {
                                z = true;
                            }
                            setFirstOut = z;
                        } else {
                            if (fragment2.mAdded && !fragment2.mHidden) {
                                z = true;
                            }
                            setFirstOut = z;
                        }
                        wasRemoved = true;
                        break;
                    case 4:
                        if (isReorderedTransaction) {
                            if (fragment2.mHiddenChanged && fragment2.mAdded && fragment2.mHidden) {
                                z = true;
                            }
                            setFirstOut = z;
                        } else {
                            if (fragment2.mAdded && !fragment2.mHidden) {
                                z = true;
                            }
                            setFirstOut = z;
                        }
                        wasRemoved = true;
                        break;
                    case 5:
                        if (isReorderedTransaction) {
                            if (fragment2.mHiddenChanged && !fragment2.mHidden && fragment2.mAdded) {
                                z = true;
                            }
                            setLastIn = z;
                        } else {
                            setLastIn = fragment2.mHidden;
                        }
                        wasAdded = true;
                        break;
                }
                containerTransition = transitioningFragments.get(containerId);
                if (setLastIn) {
                    containerTransition = ensureContainer(containerTransition, transitioningFragments, containerId);
                    containerTransition.lastIn = fragment2;
                    containerTransition.lastInIsPop = isPop;
                    containerTransition.lastInTransaction = transaction;
                }
                if (!isReorderedTransaction && wasAdded) {
                    if (containerTransition != null && containerTransition.firstOut == fragment2) {
                        containerTransition.firstOut = null;
                    }
                    manager = transaction.mManager;
                    if (fragment2.mState < 1 && manager.mCurState >= 1 && !transaction.mReorderingAllowed) {
                        manager.makeActive(fragment2);
                        containerTransition2 = containerTransition;
                        fragment = null;
                        manager.moveToState(fragment2, 1, 0, 0, false);
                        if (setFirstOut) {
                            containerTransition4 = containerTransition2;
                            if (containerTransition4 == null || containerTransition4.firstOut == null) {
                                containerTransition3 = ensureContainer(containerTransition4, transitioningFragments, containerId);
                                containerTransition3.firstOut = fragment2;
                                containerTransition3.firstOutIsPop = isPop;
                                containerTransition3.firstOutTransaction = transaction;
                                if (!isReorderedTransaction && wasRemoved && containerTransition3 != null && containerTransition3.lastIn == fragment2) {
                                    containerTransition3.lastIn = fragment;
                                    return;
                                }
                                return;
                            }
                        } else {
                            containerTransition4 = containerTransition2;
                        }
                        containerTransition3 = containerTransition4;
                        if (!isReorderedTransaction) {
                            return;
                        }
                        return;
                    }
                }
                fragment = null;
                containerTransition2 = containerTransition;
                if (setFirstOut) {
                }
                containerTransition3 = containerTransition4;
                if (!isReorderedTransaction) {
                }
            }
            if (isReorderedTransaction) {
                setLastIn = fragment2.mIsNewlyAdded;
            } else {
                if (!fragment2.mAdded && !fragment2.mHidden) {
                    z = true;
                }
                setLastIn = z;
            }
            wasAdded = true;
            containerTransition = transitioningFragments.get(containerId);
            if (setLastIn) {
            }
            containerTransition.firstOut = null;
            manager = transaction.mManager;
            manager.makeActive(fragment2);
            containerTransition2 = containerTransition;
            fragment = null;
            manager.moveToState(fragment2, 1, 0, 0, false);
            if (setFirstOut) {
            }
            containerTransition3 = containerTransition4;
            if (!isReorderedTransaction) {
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

    /* access modifiers changed from: package-private */
    public static class FragmentContainerTransition {
        public Fragment firstOut;
        public boolean firstOutIsPop;
        public BackStackRecord firstOutTransaction;
        public Fragment lastIn;
        public boolean lastInIsPop;
        public BackStackRecord lastInTransaction;

        FragmentContainerTransition() {
        }
    }

    private FragmentTransition() {
    }
}
