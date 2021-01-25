package android.support.v4.app;

import android.graphics.Rect;
import android.support.annotation.RestrictTo;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewGroupCompat;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public abstract class FragmentTransitionImpl {
    public abstract void addTarget(Object obj, View view);

    public abstract void addTargets(Object obj, ArrayList<View> arrayList);

    public abstract void beginDelayedTransition(ViewGroup viewGroup, Object obj);

    public abstract boolean canHandle(Object obj);

    public abstract Object cloneTransition(Object obj);

    public abstract Object mergeTransitionsInSequence(Object obj, Object obj2, Object obj3);

    public abstract Object mergeTransitionsTogether(Object obj, Object obj2, Object obj3);

    public abstract void removeTarget(Object obj, View view);

    public abstract void replaceTargets(Object obj, ArrayList<View> arrayList, ArrayList<View> arrayList2);

    public abstract void scheduleHideFragmentView(Object obj, View view, ArrayList<View> arrayList);

    public abstract void scheduleRemoveTargets(Object obj, Object obj2, ArrayList<View> arrayList, Object obj3, ArrayList<View> arrayList2, Object obj4, ArrayList<View> arrayList3);

    public abstract void setEpicenter(Object obj, Rect rect);

    public abstract void setEpicenter(Object obj, View view);

    public abstract void setSharedElementTargets(Object obj, View view, ArrayList<View> arrayList);

    public abstract void swapSharedElementTargets(Object obj, ArrayList<View> arrayList, ArrayList<View> arrayList2);

    public abstract Object wrapTransitionInSet(Object obj);

    /* access modifiers changed from: protected */
    public void getBoundsOnScreen(View view, Rect epicenter) {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        epicenter.set(loc[0], loc[1], loc[0] + view.getWidth(), loc[1] + view.getHeight());
    }

    /* access modifiers changed from: package-private */
    public ArrayList<String> prepareSetNameOverridesReordered(ArrayList<View> sharedElementsIn) {
        ArrayList<String> names = new ArrayList<>();
        int numSharedElements = sharedElementsIn.size();
        for (int i = 0; i < numSharedElements; i++) {
            View view = sharedElementsIn.get(i);
            names.add(ViewCompat.getTransitionName(view));
            ViewCompat.setTransitionName(view, null);
        }
        return names;
    }

    /* access modifiers changed from: package-private */
    public void setNameOverridesReordered(View sceneRoot, final ArrayList<View> sharedElementsOut, final ArrayList<View> sharedElementsIn, final ArrayList<String> inNames, Map<String, String> nameOverrides) {
        final int numSharedElements = sharedElementsIn.size();
        final ArrayList<String> outNames = new ArrayList<>();
        for (int i = 0; i < numSharedElements; i++) {
            View view = sharedElementsOut.get(i);
            String name = ViewCompat.getTransitionName(view);
            outNames.add(name);
            if (name != null) {
                ViewCompat.setTransitionName(view, null);
                String inName = nameOverrides.get(name);
                int j = 0;
                while (true) {
                    if (j >= numSharedElements) {
                        break;
                    } else if (inName.equals(inNames.get(j))) {
                        ViewCompat.setTransitionName(sharedElementsIn.get(j), name);
                        break;
                    } else {
                        j++;
                    }
                }
            }
        }
        OneShotPreDrawListener.add(sceneRoot, new Runnable() {
            /* class android.support.v4.app.FragmentTransitionImpl.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                for (int i = 0; i < numSharedElements; i++) {
                    ViewCompat.setTransitionName((View) sharedElementsIn.get(i), (String) inNames.get(i));
                    ViewCompat.setTransitionName((View) sharedElementsOut.get(i), (String) outNames.get(i));
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void captureTransitioningViews(ArrayList<View> transitioningViews, View view) {
        if (view.getVisibility() != 0) {
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (ViewGroupCompat.isTransitionGroup(viewGroup)) {
                transitioningViews.add(viewGroup);
                return;
            }
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                captureTransitioningViews(transitioningViews, viewGroup.getChildAt(i));
            }
            return;
        }
        transitioningViews.add(view);
    }

    /* access modifiers changed from: package-private */
    public void findNamedViews(Map<String, View> namedViews, View view) {
        if (view.getVisibility() == 0) {
            String transitionName = ViewCompat.getTransitionName(view);
            if (transitionName != null) {
                namedViews.put(transitionName, view);
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int count = viewGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    findNamedViews(namedViews, viewGroup.getChildAt(i));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setNameOverridesOrdered(View sceneRoot, final ArrayList<View> sharedElementsIn, final Map<String, String> nameOverrides) {
        OneShotPreDrawListener.add(sceneRoot, new Runnable() {
            /* class android.support.v4.app.FragmentTransitionImpl.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                int numSharedElements = sharedElementsIn.size();
                for (int i = 0; i < numSharedElements; i++) {
                    View view = (View) sharedElementsIn.get(i);
                    String name = ViewCompat.getTransitionName(view);
                    if (name != null) {
                        ViewCompat.setTransitionName(view, FragmentTransitionImpl.findKeyForValue(nameOverrides, name));
                    }
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void scheduleNameReset(ViewGroup sceneRoot, final ArrayList<View> sharedElementsIn, final Map<String, String> nameOverrides) {
        OneShotPreDrawListener.add(sceneRoot, new Runnable() {
            /* class android.support.v4.app.FragmentTransitionImpl.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                int numSharedElements = sharedElementsIn.size();
                for (int i = 0; i < numSharedElements; i++) {
                    View view = (View) sharedElementsIn.get(i);
                    ViewCompat.setTransitionName(view, (String) nameOverrides.get(ViewCompat.getTransitionName(view)));
                }
            }
        });
    }

    protected static void bfsAddViewChildren(List<View> views, View startView) {
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

    protected static boolean isNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }

    static String findKeyForValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
