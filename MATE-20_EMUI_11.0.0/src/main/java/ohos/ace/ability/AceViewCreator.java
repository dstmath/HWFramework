package ohos.ace.ability;

import com.huawei.ace.runtime.IAceView;
import com.huawei.ace.runtime.IAceViewCreator;
import ohos.app.Context;

public class AceViewCreator implements IAceViewCreator {
    private Context context;

    public AceViewCreator(Context context2) {
        this.context = context2;
    }

    @Override // com.huawei.ace.runtime.IAceViewCreator
    public IAceView createView(int i, float f) {
        return new AceView(this.context, i, f);
    }
}
