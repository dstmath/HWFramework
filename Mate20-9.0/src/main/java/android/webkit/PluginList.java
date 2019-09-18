package android.webkit;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class PluginList {
    private ArrayList<Plugin> mPlugins = new ArrayList<>();

    @Deprecated
    public synchronized List getList() {
        return this.mPlugins;
    }

    @Deprecated
    public synchronized void addPlugin(Plugin plugin) {
        if (!this.mPlugins.contains(plugin)) {
            this.mPlugins.add(plugin);
        }
    }

    @Deprecated
    public synchronized void removePlugin(Plugin plugin) {
        int location = this.mPlugins.indexOf(plugin);
        if (location != -1) {
            this.mPlugins.remove(location);
        }
    }

    @Deprecated
    public synchronized void clear() {
        this.mPlugins.clear();
    }

    /* Debug info: failed to restart local var, previous not found, register: 1 */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    @Deprecated
    public synchronized void pluginClicked(Context context, int position) {
        this.mPlugins.get(position).dispatchClickEvent(context);
    }
}
