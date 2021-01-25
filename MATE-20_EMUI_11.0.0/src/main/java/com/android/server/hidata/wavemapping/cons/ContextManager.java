package com.android.server.hidata.wavemapping.cons;

import android.content.Context;

public class ContextManager {
    private static final String TAG = ("WMapping." + ContextManager.class.getSimpleName());
    private static ContextManager instance = null;
    private Context context = null;

    public static synchronized ContextManager getInstance() {
        ContextManager contextManager;
        synchronized (ContextManager.class) {
            if (instance == null) {
                instance = new ContextManager();
            }
            contextManager = instance;
        }
        return contextManager;
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context2) {
        if (this.context == null) {
            this.context = context2;
        }
    }
}
