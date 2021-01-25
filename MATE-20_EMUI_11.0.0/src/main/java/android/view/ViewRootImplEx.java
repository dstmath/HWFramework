package android.view;

public class ViewRootImplEx {
    public static void setIsAmbientMode(ViewRootImpl viewRoot, boolean isAmbient) {
        if (viewRoot != null) {
            viewRoot.setIsAmbientMode(isAmbient);
        }
    }
}
