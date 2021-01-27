package ohos.form;

import android.content.Context;
import ohos.aafwk.ability.FormAdapter;

public class FormManagerAdapter {
    private static final FormManagerAdapter INSTANCE = new FormManagerAdapter();

    public static FormManagerAdapter getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        FormAdapter.getInstance().init(context);
    }

    private FormManagerAdapter() {
    }
}
