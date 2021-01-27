package com.android.server.connectivity;

public class DefaultHwTetheringEx implements IHwTetheringEx {

    /* access modifiers changed from: private */
    public static final class Singleton {
        private static final IHwTetheringEx INSTANCE = new DefaultHwTetheringEx();

        private Singleton() {
        }
    }

    private DefaultHwTetheringEx() {
    }

    public static IHwTetheringEx getDefault() {
        return Singleton.INSTANCE;
    }
}
