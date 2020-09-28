package huawei.android.widget.loader;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class PluginLayoutInflater extends LayoutInflater {
    private static final String[] CLASS_PREFIX_LIST = {"android.widget.", "android.webkit.", "android.app.", "huawei.android.widget."};
    private static final String TAG = "PluginLayoutInflater";

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
    @Override // android.view.LayoutInflater
    public View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        for (String prefix : CLASS_PREFIX_LIST) {
            try {
                View view = createView(name, prefix, attrs);
                if (view != null) {
                    return view;
                }
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "create view exception");
            }
        }
        return super.onCreateView(name, attrs);
    }
}
