package huawei.android.widget.loader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

public class PluginLayoutInflater extends LayoutInflater {
    private static final String[] sClassPrefixList = {"android.widget.", "android.webkit.", "android.app.", "huawei.android.widget."};

    public PluginLayoutInflater(Context context) {
        super(context);
    }

    protected PluginLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
    }

    public LayoutInflater cloneInContext(Context newContext) {
        return new PluginLayoutInflater(this, newContext);
    }

    /* access modifiers changed from: protected */
    public View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        String[] strArr = sClassPrefixList;
        int length = strArr.length;
        int i = 0;
        while (i < length) {
            try {
                View view = createView(name, strArr[i], attrs);
                if (view != null) {
                    return view;
                }
                i++;
            } catch (ClassNotFoundException e) {
            }
        }
        return super.onCreateView(name, attrs);
    }
}
