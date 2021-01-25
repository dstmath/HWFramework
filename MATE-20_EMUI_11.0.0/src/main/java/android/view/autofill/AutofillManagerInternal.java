package android.view.autofill;

import android.content.AutofillOptions;

public abstract class AutofillManagerInternal {
    public abstract AutofillOptions getAutofillOptions(String str, long j, int i);

    public abstract boolean isAugmentedAutofillServiceForUser(int i, int i2);

    public abstract void onBackKeyPressed();
}
