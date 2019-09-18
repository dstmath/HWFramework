package android.support.v4.app;

import android.graphics.Rect;
import android.support.annotation.RequiresApi;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(21)
class FragmentTransitionCompat21 extends FragmentTransitionImpl {
    FragmentTransitionCompat21() {
    }

    public boolean canHandle(Object transition) {
        return transition instanceof Transition;
    }

    public Object cloneTransition(Object transition) {
        if (transition != null) {
            return ((Transition) transition).clone();
        }
        return null;
    }

    public Object wrapTransitionInSet(Object transition) {
        if (transition == null) {
            return null;
        }
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition((Transition) transition);
        return transitionSet;
    }

    public void setSharedElementTargets(Object transitionObj, View nonExistentView, ArrayList<View> sharedViews) {
        TransitionSet transition = (TransitionSet) transitionObj;
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

    public void setEpicenter(Object transitionObj, View view) {
        if (view != null) {
            final Rect epicenter = new Rect();
            getBoundsOnScreen(view, epicenter);
            ((Transition) transitionObj).setEpicenterCallback(new Transition.EpicenterCallback() {
                public Rect onGetEpicenter(Transition transition) {
                    return epicenter;
                }
            });
        }
    }

    public void addTargets(Object transitionObj, ArrayList<View> views) {
        Transition transition = (Transition) transitionObj;
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

    public Object mergeTransitionsTogether(Object transition1, Object transition2, Object transition3) {
        TransitionSet transitionSet = new TransitionSet();
        if (transition1 != null) {
            transitionSet.addTransition((Transition) transition1);
        }
        if (transition2 != null) {
            transitionSet.addTransition((Transition) transition2);
        }
        if (transition3 != null) {
            transitionSet.addTransition((Transition) transition3);
        }
        return transitionSet;
    }

    public void scheduleHideFragmentView(Object exitTransitionObj, final View fragmentView, final ArrayList<View> exitingViews) {
        ((Transition) exitTransitionObj).addListener(new Transition.TransitionListener() {
            public void onTransitionStart(Transition transition) {
            }

            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                fragmentView.setVisibility(8);
                int numViews = exitingViews.size();
                for (int i = 0; i < numViews; i++) {
                    ((View) exitingViews.get(i)).setVisibility(0);
                }
            }

            public void onTransitionCancel(Transition transition) {
            }

            public void onTransitionPause(Transition transition) {
            }

            public void onTransitionResume(Transition transition) {
            }
        });
    }

    public Object mergeTransitionsInSequence(Object exitTransitionObj, Object enterTransitionObj, Object sharedElementTransitionObj) {
        Transition staggered = null;
        Transition exitTransition = (Transition) exitTransitionObj;
        Transition enterTransition = (Transition) enterTransitionObj;
        Transition sharedElementTransition = (Transition) sharedElementTransitionObj;
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

    public void beginDelayedTransition(ViewGroup sceneRoot, Object transition) {
        TransitionManager.beginDelayedTransition(sceneRoot, (Transition) transition);
    }

    public void scheduleRemoveTargets(Object overallTransitionObj, Object enterTransition, ArrayList<View> enteringViews, Object exitTransition, ArrayList<View> exitingViews, Object sharedElementTransition, ArrayList<View> sharedElementsIn) {
        final Object obj = enterTransition;
        final ArrayList<View> arrayList = enteringViews;
        final Object obj2 = exitTransition;
        final ArrayList<View> arrayList2 = exitingViews;
        final Object obj3 = sharedElementTransition;
        final ArrayList<View> arrayList3 = sharedElementsIn;
        AnonymousClass3 r1 = new Transition.TransitionListener() {
            public void onTransitionStart(Transition transition) {
                if (obj != null) {
                    FragmentTransitionCompat21.this.replaceTargets(obj, arrayList, null);
                }
                if (obj2 != null) {
                    FragmentTransitionCompat21.this.replaceTargets(obj2, arrayList2, null);
                }
                if (obj3 != null) {
                    FragmentTransitionCompat21.this.replaceTargets(obj3, arrayList3, null);
                }
            }

            public void onTransitionEnd(Transition transition) {
            }

            public void onTransitionCancel(Transition transition) {
            }

            public void onTransitionPause(Transition transition) {
            }

            public void onTransitionResume(Transition transition) {
            }
        };
        ((Transition) overallTransitionObj).addListener(r1);
    }

    public void swapSharedElementTargets(Object sharedElementTransitionObj, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn) {
        TransitionSet sharedElementTransition = (TransitionSet) sharedElementTransitionObj;
        if (sharedElementTransition != null) {
            sharedElementTransition.getTargets().clear();
            sharedElementTransition.getTargets().addAll(sharedElementsIn);
            replaceTargets(sharedElementTransition, sharedElementsOut, sharedElementsIn);
        }
    }

    public void replaceTargets(Object transitionObj, ArrayList<View> oldTargets, ArrayList<View> newTargets) {
        Transition transition = (Transition) transitionObj;
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

    public void addTarget(Object transitionObj, View view) {
        if (transitionObj != null) {
            ((Transition) transitionObj).addTarget(view);
        }
    }

    public void removeTarget(Object transitionObj, View view) {
        if (transitionObj != null) {
            ((Transition) transitionObj).removeTarget(view);
        }
    }

    public void setEpicenter(Object transitionObj, final Rect epicenter) {
        if (transitionObj != null) {
            ((Transition) transitionObj).setEpicenterCallback(new Transition.EpicenterCallback() {
                public Rect onGetEpicenter(Transition transition) {
                    if (epicenter == null || epicenter.isEmpty()) {
                        return null;
                    }
                    return epicenter;
                }
            });
        }
    }
}
