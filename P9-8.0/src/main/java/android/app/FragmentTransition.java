package android.app;

import android.graphics.Rect;
import android.hardware.camera2.params.TonemapCurve;
import android.transition.Transition;
import android.transition.Transition.EpicenterCallback;
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
    private static final int[] INVERSE_OPS = new int[]{0, 3, 0, 1, 5, 4, 7, 6, 9, 8};

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
            int i;
            SparseArray<FragmentContainerTransition> transitioningFragments = new SparseArray();
            for (i = startIndex; i < endIndex; i++) {
                BackStackRecord record = (BackStackRecord) records.get(i);
                if (((Boolean) isRecordPop.get(i)).booleanValue()) {
                    calculatePopFragments(record, transitioningFragments, isReordered);
                } else {
                    calculateFragments(record, transitioningFragments, isReordered);
                }
            }
            if (transitioningFragments.size() != 0) {
                View nonExistentView = new View(fragmentManager.mHost.getContext());
                int numContainers = transitioningFragments.size();
                for (i = 0; i < numContainers; i++) {
                    int containerId = transitioningFragments.keyAt(i);
                    ArrayMap<String, String> nameOverrides = calculateNameOverrides(containerId, records, isRecordPop, startIndex, endIndex);
                    FragmentContainerTransition containerTransition = (FragmentContainerTransition) transitioningFragments.valueAt(i);
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
        ArrayMap<String, String> nameOverrides = new ArrayMap();
        for (int recordNum = endIndex - 1; recordNum >= startIndex; recordNum--) {
            BackStackRecord record = (BackStackRecord) records.get(recordNum);
            if (record.interactsWith(containerId)) {
                boolean isPop = ((Boolean) isRecordPop.get(recordNum)).booleanValue();
                if (record.mSharedElementSourceNames != null) {
                    ArrayList<String> targets;
                    ArrayList<String> sources;
                    int numSharedElements = record.mSharedElementSourceNames.size();
                    if (isPop) {
                        targets = record.mSharedElementSourceNames;
                        sources = record.mSharedElementTargetNames;
                    } else {
                        sources = record.mSharedElementSourceNames;
                        targets = record.mSharedElementTargetNames;
                    }
                    for (int i = 0; i < numSharedElements; i++) {
                        String sourceName = (String) sources.get(i);
                        String targetName = (String) targets.get(i);
                        String previousTarget = (String) nameOverrides.remove(targetName);
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
        ViewGroup viewGroup = null;
        if (fragmentManager.mContainer.onHasView()) {
            viewGroup = (ViewGroup) fragmentManager.mContainer.onFindViewById(containerId);
        }
        if (viewGroup != null) {
            Fragment inFragment = fragments.lastIn;
            Fragment outFragment = fragments.firstOut;
            boolean inIsPop = fragments.lastInIsPop;
            boolean outIsPop = fragments.firstOutIsPop;
            ArrayList<View> sharedElementsIn = new ArrayList();
            ArrayList<View> sharedElementsOut = new ArrayList();
            Transition enterTransition = getEnterTransition(inFragment, inIsPop);
            Transition exitTransition = getExitTransition(outFragment, outIsPop);
            TransitionSet sharedElementTransition = configureSharedElementsReordered(viewGroup, nonExistentView, nameOverrides, fragments, sharedElementsOut, sharedElementsIn, enterTransition, exitTransition);
            if (enterTransition != null || sharedElementTransition != null || exitTransition != null) {
                ArrayList<View> exitingViews = configureEnteringExitingViews(exitTransition, outFragment, sharedElementsOut, nonExistentView);
                ArrayList<View> enteringViews = configureEnteringExitingViews(enterTransition, inFragment, sharedElementsIn, nonExistentView);
                setViewVisibility(enteringViews, 4);
                Transition transition = mergeTransitions(enterTransition, exitTransition, sharedElementTransition, inFragment, inIsPop);
                if (transition != null) {
                    replaceHide(exitTransition, outFragment, exitingViews);
                    transition.setNameOverrides(nameOverrides);
                    scheduleRemoveTargets(transition, enterTransition, enteringViews, exitTransition, exitingViews, sharedElementTransition, sharedElementsIn);
                    TransitionManager.beginDelayedTransition(viewGroup, transition);
                    setViewVisibility(enteringViews, 0);
                    if (sharedElementTransition != null) {
                        sharedElementTransition.getTargets().clear();
                        sharedElementTransition.getTargets().addAll(sharedElementsIn);
                        replaceTargets(sharedElementTransition, sharedElementsOut, sharedElementsIn);
                    }
                }
            }
        }
    }

    private static void configureTransitionsOrdered(FragmentManagerImpl fragmentManager, int containerId, FragmentContainerTransition fragments, View nonExistentView, ArrayMap<String, String> nameOverrides) {
        ViewGroup viewGroup = null;
        if (fragmentManager.mContainer.onHasView()) {
            viewGroup = (ViewGroup) fragmentManager.mContainer.onFindViewById(containerId);
        }
        if (viewGroup != null) {
            Fragment inFragment = fragments.lastIn;
            Fragment outFragment = fragments.firstOut;
            boolean inIsPop = fragments.lastInIsPop;
            boolean outIsPop = fragments.firstOutIsPop;
            Transition enterTransition = getEnterTransition(inFragment, inIsPop);
            Transition exitTransition = getExitTransition(outFragment, outIsPop);
            ArrayList<View> sharedElementsOut = new ArrayList();
            ArrayList<View> sharedElementsIn = new ArrayList();
            TransitionSet sharedElementTransition = configureSharedElementsOrdered(viewGroup, nonExistentView, nameOverrides, fragments, sharedElementsOut, sharedElementsIn, enterTransition, exitTransition);
            if (enterTransition != null || sharedElementTransition != null || exitTransition != null) {
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
                    ArrayList<View> enteringViews = new ArrayList();
                    scheduleRemoveTargets(transition, enterTransition, enteringViews, exitTransition, exitingViews, sharedElementTransition, sharedElementsIn);
                    scheduleTargetChange(viewGroup, inFragment, nonExistentView, sharedElementsIn, enterTransition, enteringViews, exitTransition, exitingViews);
                    TransitionManager.beginDelayedTransition(viewGroup, transition);
                }
            }
        }
    }

    private static void replaceHide(Transition exitTransition, Fragment exitingFragment, final ArrayList<View> exitingViews) {
        if (exitingFragment != null && exitTransition != null && exitingFragment.mAdded && exitingFragment.mHidden && exitingFragment.mHiddenChanged) {
            exitingFragment.setHideReplaced(true);
            final View fragmentView = exitingFragment.getView();
            OneShotPreDrawListener.add(exitingFragment.mContainer, new -$Lambda$3eJ3p8XnIxdVOnT82Ns3R0V5ZQE(exitingViews));
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
        OneShotPreDrawListener.add(sceneRoot, new android.app.-$Lambda$3eJ3p8XnIxdVOnT82Ns3R0V5ZQE.AnonymousClass1(enterTransition, nonExistentView, inFragment, sharedElementsIn, enteringViews, exitingViews, exitTransition));
    }

    static /* synthetic */ void lambda$-android_app_FragmentTransition_18686(Transition enterTransition, View nonExistentView, Fragment inFragment, ArrayList sharedElementsIn, ArrayList enteringViews, ArrayList exitingViews, Transition exitTransition) {
        if (enterTransition != null) {
            enterTransition.removeTarget(nonExistentView);
            enteringViews.addAll(configureEnteringExitingViews(enterTransition, inFragment, sharedElementsIn, nonExistentView));
        }
        if (exitingViews != null) {
            if (exitTransition != null) {
                ArrayList<View> tempExiting = new ArrayList();
                tempExiting.add(nonExistentView);
                replaceTargets(exitTransition, exitingViews, tempExiting);
            }
            exitingViews.clear();
            exitingViews.add(nonExistentView);
        }
    }

    private static TransitionSet getSharedElementTransition(Fragment inFragment, Fragment outFragment, boolean isPop) {
        if (inFragment == null || outFragment == null) {
            return null;
        }
        Transition sharedElementReturnTransition;
        if (isPop) {
            sharedElementReturnTransition = outFragment.getSharedElementReturnTransition();
        } else {
            sharedElementReturnTransition = inFragment.getSharedElementEnterTransition();
        }
        Transition transition = cloneTransition(sharedElementReturnTransition);
        if (transition == null) {
            return null;
        }
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(transition);
        return transitionSet;
    }

    private static Transition getEnterTransition(Fragment inFragment, boolean isPop) {
        if (inFragment == null) {
            return null;
        }
        Transition reenterTransition;
        if (isPop) {
            reenterTransition = inFragment.getReenterTransition();
        } else {
            reenterTransition = inFragment.getEnterTransition();
        }
        return cloneTransition(reenterTransition);
    }

    private static Transition getExitTransition(Fragment outFragment, boolean isPop) {
        if (outFragment == null) {
            return null;
        }
        Transition returnTransition;
        if (isPop) {
            returnTransition = outFragment.getReturnTransition();
        } else {
            returnTransition = outFragment.getExitTransition();
        }
        return cloneTransition(returnTransition);
    }

    private static Transition cloneTransition(Transition transition) {
        if (transition != null) {
            return transition.clone();
        }
        return transition;
    }

    private static TransitionSet configureSharedElementsReordered(ViewGroup sceneRoot, View nonExistentView, ArrayMap<String, String> nameOverrides, FragmentContainerTransition fragments, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn, Transition enterTransition, Transition exitTransition) {
        Fragment inFragment = fragments.lastIn;
        Fragment outFragment = fragments.firstOut;
        if (inFragment != null) {
            inFragment.getView().setVisibility(0);
        }
        if (inFragment == null || outFragment == null) {
            return null;
        }
        TransitionSet sharedElementTransition;
        boolean inIsPop = fragments.lastInIsPop;
        if (nameOverrides.isEmpty()) {
            sharedElementTransition = null;
        } else {
            sharedElementTransition = getSharedElementTransition(inFragment, outFragment, inIsPop);
        }
        ArrayMap<String, View> outSharedElements = captureOutSharedElements(nameOverrides, sharedElementTransition, fragments);
        ArrayMap<String, View> inSharedElements = captureInSharedElements(nameOverrides, sharedElementTransition, fragments);
        if (nameOverrides.isEmpty()) {
            sharedElementTransition = null;
            if (outSharedElements != null) {
                outSharedElements.clear();
            }
            if (inSharedElements != null) {
                inSharedElements.clear();
            }
        } else {
            addSharedElementsWithMatchingNames(sharedElementsOut, outSharedElements, nameOverrides.keySet());
            addSharedElementsWithMatchingNames(sharedElementsIn, inSharedElements, nameOverrides.values());
        }
        if (enterTransition == null && exitTransition == null && sharedElementTransition == null) {
            return null;
        }
        final Object epicenter;
        Object epicenterView;
        callSharedElementStartEnd(inFragment, outFragment, inIsPop, outSharedElements, true);
        if (sharedElementTransition != null) {
            sharedElementsIn.add(nonExistentView);
            setSharedElementTargets(sharedElementTransition, nonExistentView, sharedElementsOut);
            setOutEpicenter(sharedElementTransition, exitTransition, outSharedElements, fragments.firstOutIsPop, fragments.firstOutTransaction);
            epicenter = new Rect();
            epicenterView = getInEpicenterView(inSharedElements, fragments, enterTransition, inIsPop);
            if (epicenterView != null) {
                enterTransition.setEpicenterCallback(new EpicenterCallback() {
                    public Rect onGetEpicenter(Transition transition) {
                        return epicenter;
                    }
                });
            }
        } else {
            epicenter = null;
            epicenterView = null;
        }
        OneShotPreDrawListener.add(sceneRoot, new android.app.-$Lambda$3eJ3p8XnIxdVOnT82Ns3R0V5ZQE.AnonymousClass2(inIsPop, inFragment, outFragment, inSharedElements, epicenterView, epicenter));
        return sharedElementTransition;
    }

    static /* synthetic */ void lambda$-android_app_FragmentTransition_26843(Fragment inFragment, Fragment outFragment, boolean inIsPop, ArrayMap inSharedElements, View epicenterView, Rect epicenter) {
        callSharedElementStartEnd(inFragment, outFragment, inIsPop, inSharedElements, false);
        if (epicenterView != null) {
            epicenterView.getBoundsOnScreen(epicenter);
        }
    }

    private static void addSharedElementsWithMatchingNames(ArrayList<View> views, ArrayMap<String, View> sharedElements, Collection<String> nameOverridesSet) {
        for (int i = sharedElements.size() - 1; i >= 0; i--) {
            View view = (View) sharedElements.valueAt(i);
            if (view != null && nameOverridesSet.contains(view.getTransitionName())) {
                views.add(view);
            }
        }
    }

    private static TransitionSet configureSharedElementsOrdered(ViewGroup sceneRoot, View nonExistentView, ArrayMap<String, String> nameOverrides, FragmentContainerTransition fragments, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn, Transition enterTransition, Transition exitTransition) {
        Fragment inFragment = fragments.lastIn;
        Fragment outFragment = fragments.firstOut;
        if (inFragment == null || outFragment == null) {
            return null;
        }
        TransitionSet sharedElementTransition;
        boolean inIsPop = fragments.lastInIsPop;
        if (nameOverrides.isEmpty()) {
            sharedElementTransition = null;
        } else {
            sharedElementTransition = getSharedElementTransition(inFragment, outFragment, inIsPop);
        }
        ArrayMap<String, View> outSharedElements = captureOutSharedElements(nameOverrides, sharedElementTransition, fragments);
        if (nameOverrides.isEmpty()) {
            sharedElementTransition = null;
        } else {
            sharedElementsOut.addAll(outSharedElements.values());
        }
        if (enterTransition == null && exitTransition == null && sharedElementTransition == null) {
            return null;
        }
        Object inEpicenter;
        callSharedElementStartEnd(inFragment, outFragment, inIsPop, outSharedElements, true);
        if (sharedElementTransition != null) {
            inEpicenter = new Rect();
            setSharedElementTargets(sharedElementTransition, nonExistentView, sharedElementsOut);
            setOutEpicenter(sharedElementTransition, exitTransition, outSharedElements, fragments.firstOutIsPop, fragments.firstOutTransaction);
            if (enterTransition != null) {
                final Rect rect = inEpicenter;
                enterTransition.setEpicenterCallback(new EpicenterCallback() {
                    public Rect onGetEpicenter(Transition transition) {
                        if (rect.isEmpty()) {
                            return null;
                        }
                        return rect;
                    }
                });
            }
        } else {
            inEpicenter = null;
        }
        View view = sceneRoot;
        OneShotPreDrawListener.add(view, new android.app.-$Lambda$3eJ3p8XnIxdVOnT82Ns3R0V5ZQE.AnonymousClass3(inIsPop, nameOverrides, sharedElementTransition, fragments, sharedElementsIn, nonExistentView, inFragment, outFragment, sharedElementsOut, enterTransition, inEpicenter));
        return sharedElementTransition;
    }

    static /* synthetic */ void lambda$-android_app_FragmentTransition_32404(ArrayMap nameOverrides, TransitionSet finalSharedElementTransition, FragmentContainerTransition fragments, ArrayList sharedElementsIn, View nonExistentView, Fragment inFragment, Fragment outFragment, boolean inIsPop, ArrayList sharedElementsOut, Transition enterTransition, Rect inEpicenter) {
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
        if (nameOverrides.isEmpty() || sharedElementTransition == null) {
            nameOverrides.clear();
            return null;
        }
        SharedElementCallback sharedElementCallback;
        ArrayList<String> names;
        Fragment outFragment = fragments.firstOut;
        ArrayMap<String, View> outSharedElements = new ArrayMap();
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
                String name = (String) names.get(i);
                View view = (View) outSharedElements.get(name);
                if (view == null) {
                    nameOverrides.remove(name);
                } else if (!name.equals(view.getTransitionName())) {
                    nameOverrides.put(view.getTransitionName(), (String) nameOverrides.remove(name));
                }
            }
        } else {
            nameOverrides.retainAll(outSharedElements.keySet());
        }
        return outSharedElements;
    }

    private static ArrayMap<String, View> captureInSharedElements(ArrayMap<String, String> nameOverrides, TransitionSet sharedElementTransition, FragmentContainerTransition fragments) {
        Fragment inFragment = fragments.lastIn;
        View fragmentView = inFragment.getView();
        if (nameOverrides.isEmpty() || sharedElementTransition == null || fragmentView == null) {
            nameOverrides.clear();
            return null;
        }
        SharedElementCallback sharedElementCallback;
        ArrayList<String> names;
        ArrayMap<String, View> inSharedElements = new ArrayMap();
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
                String name = (String) names.get(i);
                View view = (View) inSharedElements.get(name);
                String key;
                if (view == null) {
                    key = findKeyForValue(nameOverrides, name);
                    if (key != null) {
                        nameOverrides.remove(key);
                    }
                } else if (!name.equals(view.getTransitionName())) {
                    key = findKeyForValue(nameOverrides, name);
                    if (key != null) {
                        nameOverrides.put(key, view.getTransitionName());
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
                return (String) map.keyAt(i);
            }
        }
        return null;
    }

    private static View getInEpicenterView(ArrayMap<String, View> inSharedElements, FragmentContainerTransition fragments, Transition enterTransition, boolean inIsPop) {
        BackStackRecord inTransaction = fragments.lastInTransaction;
        if (enterTransition == null || inSharedElements == null || inTransaction.mSharedElementSourceNames == null || (inTransaction.mSharedElementSourceNames.isEmpty() ^ 1) == 0) {
            return null;
        }
        String targetName;
        if (inIsPop) {
            targetName = (String) inTransaction.mSharedElementSourceNames.get(0);
        } else {
            targetName = (String) inTransaction.mSharedElementTargetNames.get(0);
        }
        return (View) inSharedElements.get(targetName);
    }

    private static void setOutEpicenter(TransitionSet sharedElementTransition, Transition exitTransition, ArrayMap<String, View> outSharedElements, boolean outIsPop, BackStackRecord outTransaction) {
        if (outTransaction.mSharedElementSourceNames != null && (outTransaction.mSharedElementSourceNames.isEmpty() ^ 1) != 0) {
            String sourceName;
            if (outIsPop) {
                sourceName = (String) outTransaction.mSharedElementTargetNames.get(0);
            } else {
                sourceName = (String) outTransaction.mSharedElementSourceNames.get(0);
            }
            View outEpicenterView = (View) outSharedElements.get(sourceName);
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
            transition.setEpicenterCallback(new EpicenterCallback() {
                public Rect onGetEpicenter(Transition transition) {
                    return epicenter;
                }
            });
        }
    }

    private static void retainValues(ArrayMap<String, String> nameOverrides, ArrayMap<String, View> namedViews) {
        for (int i = nameOverrides.size() - 1; i >= 0; i--) {
            if (!namedViews.containsKey((String) nameOverrides.valueAt(i))) {
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
            ArrayList<View> views = new ArrayList();
            ArrayList<String> names = new ArrayList();
            int count = sharedElements == null ? 0 : sharedElements.size();
            for (int i = 0; i < count; i++) {
                names.add((String) sharedElements.keyAt(i));
                views.add((View) sharedElements.valueAt(i));
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
            bfsAddViewChildren(views, (View) sharedViews.get(i));
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
                View view = (View) views.get(index);
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
        overalTransition.addListener(new TransitionListenerAdapter() {
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
        });
    }

    public static void replaceTargets(Transition transition, ArrayList<View> oldTargets, ArrayList<View> newTargets) {
        int i;
        if (transition instanceof TransitionSet) {
            TransitionSet set = (TransitionSet) transition;
            int numTransitions = set.getTransitionCount();
            for (i = 0; i < numTransitions; i++) {
                replaceTargets(set.getTransitionAt(i), oldTargets, newTargets);
            }
        } else if (!hasSimpleTarget(transition)) {
            List<View> targets = transition.getTargets();
            if (targets != null && targets.size() == oldTargets.size() && targets.containsAll(oldTargets)) {
                int targetCount = newTargets == null ? 0 : newTargets.size();
                for (i = 0; i < targetCount; i++) {
                    transition.addTarget((View) newTargets.get(i));
                }
                for (i = oldTargets.size() - 1; i >= 0; i--) {
                    transition.removeTarget((View) oldTargets.get(i));
                }
            }
        }
    }

    public static void addTargets(Transition transition, ArrayList<View> views) {
        if (transition != null) {
            int i;
            if (transition instanceof TransitionSet) {
                TransitionSet set = (TransitionSet) transition;
                int numTransitions = set.getTransitionCount();
                for (i = 0; i < numTransitions; i++) {
                    addTargets(set.getTransitionAt(i), views);
                }
            } else if (!hasSimpleTarget(transition) && isNullOrEmpty(transition.getTargets())) {
                int numViews = views.size();
                for (i = 0; i < numViews; i++) {
                    transition.addTarget((View) views.get(i));
                }
            }
        }
    }

    private static boolean hasSimpleTarget(Transition transition) {
        if (isNullOrEmpty(transition.getTargetIds()) && (isNullOrEmpty(transition.getTargetNames()) ^ 1) == 0) {
            return isNullOrEmpty(transition.getTargetTypes()) ^ 1;
        }
        return true;
    }

    private static boolean isNullOrEmpty(List list) {
        return list != null ? list.isEmpty() : true;
    }

    private static ArrayList<View> configureEnteringExitingViews(Transition transition, Fragment fragment, ArrayList<View> sharedElements, View nonExistentView) {
        ArrayList<View> viewList = null;
        if (transition != null) {
            viewList = new ArrayList();
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

    private static void setViewVisibility(ArrayList<View> views, int visibility) {
        if (views != null) {
            for (int i = views.size() - 1; i >= 0; i--) {
                ((View) views.get(i)).setVisibility(visibility);
            }
        }
    }

    private static Transition mergeTransitions(Transition enterTransition, Transition exitTransition, Transition sharedElementTransition, Fragment inFragment, boolean isPop) {
        boolean overlap = true;
        if (!(enterTransition == null || exitTransition == null || inFragment == null)) {
            overlap = isPop ? inFragment.getAllowReturnTransitionOverlap() : inFragment.getAllowEnterTransitionOverlap();
        }
        if (overlap) {
            Transition transitionSet = new TransitionSet();
            if (enterTransition != null) {
                transitionSet.addTransition(enterTransition);
            }
            if (exitTransition != null) {
                transitionSet.addTransition(exitTransition);
            }
            if (sharedElementTransition != null) {
                transitionSet.addTransition(sharedElementTransition);
            }
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
        Transition together = new TransitionSet();
        if (staggered != null) {
            together.addTransition(staggered);
        }
        together.addTransition(sharedElementTransition);
        return together;
    }

    public static void calculateFragments(BackStackRecord transaction, SparseArray<FragmentContainerTransition> transitioningFragments, boolean isReordered) {
        int numOps = transaction.mOps.size();
        for (int opNum = 0; opNum < numOps; opNum++) {
            addToFirstInLastOut(transaction, (Op) transaction.mOps.get(opNum), transitioningFragments, false, isReordered);
        }
    }

    public static void calculatePopFragments(BackStackRecord transaction, SparseArray<FragmentContainerTransition> transitioningFragments, boolean isReordered) {
        if (transaction.mManager.mContainer.onHasView()) {
            for (int opNum = transaction.mOps.size() - 1; opNum >= 0; opNum--) {
                addToFirstInLastOut(transaction, (Op) transaction.mOps.get(opNum), transitioningFragments, true, isReordered);
            }
        }
    }

    private static void addToFirstInLastOut(BackStackRecord transaction, Op op, SparseArray<FragmentContainerTransition> transitioningFragments, boolean isPop, boolean isReorderedTransaction) {
        Fragment fragment = op.fragment;
        if (fragment != null) {
            int containerId = fragment.mContainerId;
            if (containerId != 0) {
                boolean setLastIn = false;
                boolean wasRemoved = false;
                boolean setFirstOut = false;
                boolean wasAdded = false;
                switch (isPop ? INVERSE_OPS[op.cmd] : op.cmd) {
                    case 1:
                    case 7:
                        setLastIn = isReorderedTransaction ? fragment.mIsNewlyAdded : !fragment.mAdded ? fragment.mHidden ^ 1 : false;
                        wasAdded = true;
                        break;
                    case 3:
                    case 6:
                        setFirstOut = isReorderedTransaction ? (fragment.mAdded || fragment.mView == null || fragment.mView.getVisibility() != 0) ? false : fragment.mView.getTransitionAlpha() > TonemapCurve.LEVEL_BLACK : fragment.mAdded ? fragment.mHidden ^ 1 : false;
                        wasRemoved = true;
                        break;
                    case 4:
                        setFirstOut = isReorderedTransaction ? (fragment.mHiddenChanged && fragment.mAdded) ? fragment.mHidden : false : fragment.mAdded ? fragment.mHidden ^ 1 : false;
                        wasRemoved = true;
                        break;
                    case 5:
                        if (!isReorderedTransaction) {
                            setLastIn = fragment.mHidden;
                        } else if (!fragment.mHiddenChanged || (fragment.mHidden ^ 1) == 0) {
                            setLastIn = false;
                        } else {
                            setLastIn = fragment.mAdded;
                        }
                        wasAdded = true;
                        break;
                }
                FragmentContainerTransition containerTransition = (FragmentContainerTransition) transitioningFragments.get(containerId);
                if (setLastIn) {
                    containerTransition = ensureContainer(containerTransition, transitioningFragments, containerId);
                    containerTransition.lastIn = fragment;
                    containerTransition.lastInIsPop = isPop;
                    containerTransition.lastInTransaction = transaction;
                }
                if (!isReorderedTransaction && wasAdded) {
                    if (containerTransition != null && containerTransition.firstOut == fragment) {
                        containerTransition.firstOut = null;
                    }
                    FragmentManagerImpl manager = transaction.mManager;
                    if (fragment.mState < 1 && manager.mCurState >= 1 && manager.mHost.getContext().getApplicationInfo().targetSdkVersion >= 24 && (transaction.mReorderingAllowed ^ 1) != 0) {
                        manager.makeActive(fragment);
                        manager.moveToState(fragment, 1, 0, 0, false);
                    }
                }
                if (setFirstOut && (containerTransition == null || containerTransition.firstOut == null)) {
                    containerTransition = ensureContainer(containerTransition, transitioningFragments, containerId);
                    containerTransition.firstOut = fragment;
                    containerTransition.firstOutIsPop = isPop;
                    containerTransition.firstOutTransaction = transaction;
                }
                if (!isReorderedTransaction && wasRemoved && containerTransition != null && containerTransition.lastIn == fragment) {
                    containerTransition.lastIn = null;
                }
            }
        }
    }

    private static FragmentContainerTransition ensureContainer(FragmentContainerTransition containerTransition, SparseArray<FragmentContainerTransition> transitioningFragments, int containerId) {
        if (containerTransition != null) {
            return containerTransition;
        }
        containerTransition = new FragmentContainerTransition();
        transitioningFragments.put(containerId, containerTransition);
        return containerTransition;
    }
}
