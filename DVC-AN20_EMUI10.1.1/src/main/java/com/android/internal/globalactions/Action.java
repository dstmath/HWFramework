package com.android.internal.globalactions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface Action {
    View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater);

    CharSequence getLabelForAccessibility(Context context);

    boolean isEnabled();

    void onPress();

    boolean showBeforeProvisioning();

    boolean showDuringKeyguard();
}
