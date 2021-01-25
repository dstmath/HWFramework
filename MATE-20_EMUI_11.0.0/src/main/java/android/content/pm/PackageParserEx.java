package android.content.pm;

import android.content.pm.PackageParser;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Iterator;

public class PackageParserEx {

    public static class ActivityEx {
        private PackageParser.Activity mActivity;

        public ActivityEx(Object object) {
            this.mActivity = (PackageParser.Activity) object;
        }

        public void setNavigationHide(boolean isNavigationHide) {
            this.mActivity.info.navigationHide = isNavigationHide;
        }

        public Bundle getMetaData() {
            return this.mActivity.metaData;
        }

        public ActivityInfo getActivityInfo() {
            return this.mActivity.info;
        }
    }

    public static class PackageEx {
        public final ArrayList<ProviderEx> providers;
        public final String[] splitNames;

        public PackageEx(Object object) {
            PackageParser.Package pkg = (PackageParser.Package) object;
            this.splitNames = pkg.splitNames;
            if (pkg.providers != null) {
                this.providers = new ArrayList<>();
                Iterator<PackageParser.Provider> it = pkg.providers.iterator();
                while (it.hasNext()) {
                    this.providers.add(new ProviderEx(it.next()));
                }
                return;
            }
            this.providers = null;
        }
    }

    public static class ProviderEx {
        private PackageParser.Provider mProvider;

        private ProviderEx(Object provider) {
            this.mProvider = (PackageParser.Provider) provider;
        }

        public ProviderInfo getProviderInfo() {
            return this.mProvider.info;
        }
    }
}
