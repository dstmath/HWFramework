package com.android.server.policy;

public interface GlobalActionsProvider {

    public interface GlobalActionsListener {
        void onGlobalActionsAvailableChanged(boolean z);

        void onGlobalActionsDismissed();

        void onGlobalActionsShown();
    }

    boolean isGlobalActionsDisabled();

    void setGlobalActionsListener(GlobalActionsListener globalActionsListener);

    void showGlobalActions();
}
