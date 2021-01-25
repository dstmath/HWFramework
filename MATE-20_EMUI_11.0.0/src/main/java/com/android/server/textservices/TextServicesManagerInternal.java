package com.android.server.textservices;

import android.view.textservice.SpellCheckerInfo;
import com.android.server.LocalServices;

public abstract class TextServicesManagerInternal {
    private static final TextServicesManagerInternal NOP = new TextServicesManagerInternal() {
        /* class com.android.server.textservices.TextServicesManagerInternal.AnonymousClass1 */

        @Override // com.android.server.textservices.TextServicesManagerInternal
        public SpellCheckerInfo getCurrentSpellCheckerForUser(int userId) {
            return null;
        }
    };

    public abstract SpellCheckerInfo getCurrentSpellCheckerForUser(int i);

    public static TextServicesManagerInternal get() {
        TextServicesManagerInternal instance = (TextServicesManagerInternal) LocalServices.getService(TextServicesManagerInternal.class);
        return instance != null ? instance : NOP;
    }
}
