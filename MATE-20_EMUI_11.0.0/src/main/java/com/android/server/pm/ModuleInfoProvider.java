package com.android.server.pm;

import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.ModuleInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;

@VisibleForTesting
public class ModuleInfoProvider {
    private static final String MODULE_METADATA_KEY = "android.content.pm.MODULE_METADATA";
    private static final String TAG = "PackageManager.ModuleInfoProvider";
    private final Context mContext;
    private volatile boolean mMetadataLoaded;
    private final Map<String, ModuleInfo> mModuleInfo;
    private final IPackageManager mPackageManager;
    private volatile String mPackageName;

    ModuleInfoProvider(Context context, IPackageManager packageManager) {
        this.mContext = context;
        this.mPackageManager = packageManager;
        this.mModuleInfo = new ArrayMap();
    }

    @VisibleForTesting
    public ModuleInfoProvider(XmlResourceParser metadata, Resources resources) {
        this.mContext = null;
        this.mPackageManager = null;
        this.mModuleInfo = new ArrayMap();
        loadModuleMetadata(metadata, resources);
    }

    public void systemReady() {
        this.mPackageName = this.mContext.getResources().getString(17039824);
        if (TextUtils.isEmpty(this.mPackageName)) {
            Slog.w(TAG, "No configured module metadata provider.");
            return;
        }
        try {
            PackageInfo pi = this.mPackageManager.getPackageInfo(this.mPackageName, 128, 0);
            Resources packageResources = this.mContext.createPackageContext(this.mPackageName, 0).getResources();
            loadModuleMetadata(packageResources.getXml(pi.applicationInfo.metaData.getInt(MODULE_METADATA_KEY)), packageResources);
        } catch (PackageManager.NameNotFoundException | RemoteException e) {
            Slog.w(TAG, "Unable to discover metadata package: " + this.mPackageName, e);
        }
    }

    private void loadModuleMetadata(XmlResourceParser parser, Resources packageResources) {
        XmlUtils.beginDocument(parser, "module-metadata");
        while (true) {
            XmlUtils.nextElement(parser);
            if (parser.getEventType() == 1) {
                break;
            } else if (!"module".equals(parser.getName())) {
                Slog.w(TAG, "Unexpected metadata element: " + parser.getName());
                this.mModuleInfo.clear();
                break;
            } else {
                try {
                    CharSequence moduleName = packageResources.getText(Integer.parseInt(parser.getAttributeValue(null, Settings.ATTR_NAME).substring(1)));
                    String modulePackageName = XmlUtils.readStringAttribute(parser, "packageName");
                    boolean isHidden = XmlUtils.readBooleanAttribute(parser, "isHidden");
                    ModuleInfo mi = new ModuleInfo();
                    mi.setHidden(isHidden);
                    mi.setPackageName(modulePackageName);
                    mi.setName(moduleName);
                    this.mModuleInfo.put(modulePackageName, mi);
                } catch (IOException | XmlPullParserException e) {
                    Slog.w(TAG, "Error parsing module metadata", e);
                    this.mModuleInfo.clear();
                } catch (Throwable th) {
                    parser.close();
                    this.mMetadataLoaded = true;
                    throw th;
                }
            }
        }
        parser.close();
        this.mMetadataLoaded = true;
    }

    /* access modifiers changed from: package-private */
    public List<ModuleInfo> getInstalledModules(int flags) {
        if (!this.mMetadataLoaded) {
            throw new IllegalStateException("Call to getInstalledModules before metadata loaded");
        } else if ((131072 & flags) != 0) {
            return new ArrayList(this.mModuleInfo.values());
        } else {
            try {
                List<PackageInfo> allPackages = this.mPackageManager.getInstalledPackages(1073741824 | flags, 0).getList();
                ArrayList<ModuleInfo> installedModules = new ArrayList<>(allPackages.size());
                for (PackageInfo p : allPackages) {
                    ModuleInfo m = this.mModuleInfo.get(p.packageName);
                    if (m != null) {
                        installedModules.add(m);
                    }
                }
                return installedModules;
            } catch (RemoteException e) {
                Slog.w(TAG, "Unable to retrieve all package names", e);
                return Collections.emptyList();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ModuleInfo getModuleInfo(String packageName, int flags) {
        if (this.mMetadataLoaded) {
            return this.mModuleInfo.get(packageName);
        }
        throw new IllegalStateException("Call to getModuleInfo before metadata loaded");
    }

    /* access modifiers changed from: package-private */
    public String getPackageName() {
        if (this.mMetadataLoaded) {
            return this.mPackageName;
        }
        throw new IllegalStateException("Call to getVersion before metadata loaded");
    }
}
