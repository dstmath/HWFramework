package android.view;

import android.content.Context;

public class DefaultHwRio implements IHwRio {
    private static DefaultHwRio sInstance;

    public static synchronized IHwRio getDefault() {
        DefaultHwRio defaultHwRio;
        synchronized (DefaultHwRio.class) {
            if (sInstance == null) {
                sInstance = new DefaultHwRio();
            }
            defaultHwRio = sInstance;
        }
        return defaultHwRio;
    }

    @Override // android.view.IHwRio
    public void attachRio(Context context, View rootView, CharSequence title, Display display) {
    }

    @Override // android.view.IHwRio
    public void detachRio() {
    }

    @Override // android.view.IHwRio
    public void hookAttribute() {
    }
}
