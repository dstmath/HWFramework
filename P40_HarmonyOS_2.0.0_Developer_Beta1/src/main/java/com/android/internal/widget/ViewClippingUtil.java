package com.android.internal.widget;

import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class ViewClippingUtil {
    private static final int CLIP_CHILDREN_TAG = 16908850;
    private static final int CLIP_CLIPPING_SET = 16908849;
    private static final int CLIP_TO_PADDING = 16908852;

    public static void setClippingDeactivated(View transformedView, boolean deactivated, ClippingParameters clippingParameters) {
        if ((deactivated || clippingParameters.isClippingEnablingAllowed(transformedView)) && (transformedView.getParent() instanceof ViewGroup)) {
            ViewGroup parent = (ViewGroup) transformedView.getParent();
            while (true) {
                if (deactivated || clippingParameters.isClippingEnablingAllowed(transformedView)) {
                    ArraySet<View> clipSet = (ArraySet) parent.getTag(16908849);
                    if (clipSet == null) {
                        clipSet = new ArraySet<>();
                        parent.setTagInternal(16908849, clipSet);
                    }
                    Boolean clipChildren = (Boolean) parent.getTag(16908850);
                    if (clipChildren == null) {
                        clipChildren = Boolean.valueOf(parent.getClipChildren());
                        parent.setTagInternal(16908850, clipChildren);
                    }
                    Boolean clipToPadding = (Boolean) parent.getTag(16908852);
                    if (clipToPadding == null) {
                        clipToPadding = Boolean.valueOf(parent.getClipToPadding());
                        parent.setTagInternal(16908852, clipToPadding);
                    }
                    if (!deactivated) {
                        clipSet.remove(transformedView);
                        if (clipSet.isEmpty()) {
                            parent.setClipChildren(clipChildren.booleanValue());
                            parent.setClipToPadding(clipToPadding.booleanValue());
                            parent.setTagInternal(16908849, null);
                            clippingParameters.onClippingStateChanged(parent, true);
                        }
                    } else {
                        clipSet.add(transformedView);
                        parent.setClipChildren(false);
                        parent.setClipToPadding(false);
                        clippingParameters.onClippingStateChanged(parent, false);
                    }
                    if (!clippingParameters.shouldFinish(parent)) {
                        ViewParent viewParent = parent.getParent();
                        if (viewParent instanceof ViewGroup) {
                            parent = (ViewGroup) viewParent;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    public interface ClippingParameters {
        boolean shouldFinish(View view);

        default boolean isClippingEnablingAllowed(View view) {
            return !MessagingPropertyAnimator.isAnimatingTranslation(view);
        }

        default void onClippingStateChanged(View view, boolean isClipping) {
        }
    }
}
