package com.huawei.ace.activity;

import android.content.Context;
import com.huawei.ace.runtime.IAceView;
import com.huawei.ace.runtime.IAceViewCreator;

public class AceViewCreator implements IAceViewCreator {
    private Context context;
    private Boolean isWearable = false;

    public AceViewCreator(Context context2) {
        this.context = context2;
    }

    public void setIsWearable() {
        this.isWearable = true;
    }

    @Override // com.huawei.ace.runtime.IAceViewCreator
    public IAceView createView(int i, float f) {
        return new AceView(this.context, i, f, this.isWearable);
    }
}
