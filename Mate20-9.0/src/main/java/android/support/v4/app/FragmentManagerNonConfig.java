package android.support.v4.app;

import android.arch.lifecycle.ViewModelStore;
import java.util.List;

public class FragmentManagerNonConfig {
    private final List<FragmentManagerNonConfig> mChildNonConfigs;
    private final List<Fragment> mFragments;
    private final List<ViewModelStore> mViewModelStores;

    FragmentManagerNonConfig(List<Fragment> fragments, List<FragmentManagerNonConfig> childNonConfigs, List<ViewModelStore> viewModelStores) {
        this.mFragments = fragments;
        this.mChildNonConfigs = childNonConfigs;
        this.mViewModelStores = viewModelStores;
    }

    /* access modifiers changed from: package-private */
    public List<Fragment> getFragments() {
        return this.mFragments;
    }

    /* access modifiers changed from: package-private */
    public List<FragmentManagerNonConfig> getChildNonConfigs() {
        return this.mChildNonConfigs;
    }

    /* access modifiers changed from: package-private */
    public List<ViewModelStore> getViewModelStores() {
        return this.mViewModelStores;
    }
}
