package com.android.systemui.plugins;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

public abstract class PluginFragment extends Fragment implements Plugin {
    private Context mPluginContext;

    @Override // com.android.systemui.plugins.Plugin
    public void onCreate(Context sysuiContext, Context pluginContext) {
        this.mPluginContext = pluginContext;
    }

    @Override // android.app.Fragment
    public LayoutInflater onGetLayoutInflater(Bundle savedInstanceState) {
        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(getContext());
    }

    @Override // android.app.Fragment
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override // android.app.Fragment
    public Context getContext() {
        return this.mPluginContext;
    }
}
