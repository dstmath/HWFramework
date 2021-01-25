package huawei.android.widget.loader;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;

public class PluginContextWrapper extends ContextWrapper {
    private Resources mResource;
    private Resources.Theme mTheme;

    public PluginContextWrapper(Context base) {
        super(base);
        this.mResource = ResLoader.getInstance().getResources(base);
        this.mTheme = ResLoader.getInstance().getTheme(base);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public Resources getResources() {
        return this.mResource;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public Resources.Theme getTheme() {
        return this.mTheme;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public Object getSystemService(String name) {
        if (name.equals("layout_inflater")) {
            return new PluginLayoutInflater(this);
        }
        return super.getSystemService(name);
    }
}
