package com.android.systemui.shared.system;

import android.app.Activity;
import android.view.View;
import android.view.ViewHierarchyEncoder;
import java.io.ByteArrayOutputStream;

public class ActivityCompat {
    private final Activity mWrapped;

    public ActivityCompat(Activity activity) {
        this.mWrapped = activity;
    }

    public void registerRemoteAnimations(RemoteAnimationDefinitionCompat definition) {
        this.mWrapped.registerRemoteAnimations(definition.getWrapped());
    }

    public boolean encodeViewHierarchy(ByteArrayOutputStream out) {
        View view = null;
        if (!(this.mWrapped.getWindow() == null || this.mWrapped.getWindow().peekDecorView() == null || this.mWrapped.getWindow().peekDecorView().getViewRootImpl() == null)) {
            view = this.mWrapped.getWindow().peekDecorView().getViewRootImpl().getView();
        }
        if (view == null) {
            return false;
        }
        ViewHierarchyEncoder encoder = new ViewHierarchyEncoder(out);
        int[] location = view.getLocationOnScreen();
        encoder.addProperty("window:left", location[0]);
        encoder.addProperty("window:top", location[1]);
        view.encode(encoder);
        encoder.endStream();
        return true;
    }

    public int getDisplayId() {
        return this.mWrapped.getDisplayId();
    }
}
