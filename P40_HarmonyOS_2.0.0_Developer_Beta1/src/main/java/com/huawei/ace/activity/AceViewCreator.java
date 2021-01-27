package com.huawei.ace.activity;

import android.content.Context;
import com.huawei.ace.runtime.IAceView;
import com.huawei.ace.runtime.IAceViewCreator;

public class AceViewCreator implements IAceViewCreator {
    private Context context;
    private Boolean isWearable = false;
    private final int type;

    public AceViewCreator(Context context2, int i) {
        this.context = context2;
        this.type = i;
    }

    public void setIsWearable() {
        this.isWearable = true;
    }

    @Override // com.huawei.ace.runtime.IAceViewCreator
    public IAceView createView(int i, float f) {
        if (this.type == 1) {
            return new AceNativeView(this.context, i);
        }
        return new AceView(this.context, i, f, this.isWearable);
    }
}
