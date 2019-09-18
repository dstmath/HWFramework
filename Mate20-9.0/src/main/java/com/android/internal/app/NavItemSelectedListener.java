package com.android.internal.app;

import android.app.ActionBar;
import android.view.View;
import android.widget.AdapterView;

class NavItemSelectedListener implements AdapterView.OnItemSelectedListener {
    private final ActionBar.OnNavigationListener mListener;

    public NavItemSelectedListener(ActionBar.OnNavigationListener listener) {
        this.mListener = listener;
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (this.mListener != null) {
            this.mListener.onNavigationItemSelected(position, id);
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
