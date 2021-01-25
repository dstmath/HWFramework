package ohos.miscservices.screensaver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.util.AttributeSet;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.abilityshell.utils.AbilityContextUtils;
import ohos.bundle.ElementName;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.screensaver.IScreenSaverSysAbility;
import ohos.miscservices.screensaver.ScreenSaver;

public class ScreenSaverDefaultImpl implements IScreenSaverSysAbility.IScreenSaver {
    private static final String LOG_TAG = "ScreenSaverDefault";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, LOG_TAG);
    private static final int XMLPULLPARSER_END_DOCUMENT = 1;
    private static final int XMLPULLPARSER_START_TAG = 2;
    private final IDreamManager dreamManager;
    private Context droidContext;
    private ohos.app.Context harmContext;

    public ScreenSaverDefaultImpl(ohos.app.Context context) {
        this.harmContext = context;
        Object androidContext = AbilityContextUtils.getAndroidContext(this.harmContext);
        if (androidContext instanceof Context) {
            this.droidContext = (Context) androidContext;
        }
        this.dreamManager = IDreamManager.Stub.asInterface(ServiceManager.getService("dreams"));
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public void activeElement(ElementName elementName) {
        if (this.dreamManager != null) {
            ComponentName convertElementToCpn = Utils.convertElementToCpn(elementName);
            try {
                ComponentName[] componentNameArr = {convertElementToCpn};
                IDreamManager iDreamManager = this.dreamManager;
                if (convertElementToCpn == null) {
                    componentNameArr = null;
                }
                iDreamManager.setDreamComponents(componentNameArr);
            } catch (RemoteException unused) {
                HiLog.info(TAG, "activeElement failed.", new Object[0]);
            }
        }
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public void preview(ElementName elementName) {
        IDreamManager iDreamManager = this.dreamManager;
        if (iDreamManager != null && elementName != null) {
            try {
                iDreamManager.testDream(Utils.convertElementToCpn(elementName));
            } catch (RemoteException unused) {
                HiLog.warn(TAG, " Failed to preview().", new Object[0]);
            }
        }
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public ElementName getActiveElement() {
        IDreamManager iDreamManager = this.dreamManager;
        if (iDreamManager == null) {
            return null;
        }
        try {
            ComponentName[] dreamComponents = iDreamManager.getDreamComponents();
            if (dreamComponents == null || dreamComponents.length <= 0) {
                return null;
            }
            return Utils.convertCpnToElement(dreamComponents[0]);
        } catch (RemoteException unused) {
            HiLog.warn(TAG, "getActiveElement failed.", new Object[0]);
            return null;
        }
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public List<ScreenSaver.ScreenSaverBean> getElementList() {
        Context context = this.droidContext;
        if (context == null) {
            return null;
        }
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> queryIntentServices = packageManager.queryIntentServices(new Intent("android.service.dreams.DreamService"), 128);
        ArrayList arrayList = new ArrayList(queryIntentServices.size());
        ElementName activeElement = getActiveElement();
        for (ResolveInfo resolveInfo : queryIntentServices) {
            if (resolveInfo.serviceInfo != null) {
                ScreenSaver.ScreenSaverBean screenSaverBean = new ScreenSaver.ScreenSaverBean();
                screenSaverBean.name = resolveInfo.loadLabel(packageManager);
                Optional<ElementName> screenSaverElementName = getScreenSaverElementName(resolveInfo);
                if (screenSaverElementName.isPresent()) {
                    screenSaverBean.screenSaverElement = screenSaverElementName.get();
                }
                screenSaverBean.isActiveElement = isActiveScreenSaver(activeElement, screenSaverBean.screenSaverElement);
                Optional<ComponentName> settingsComponent = getSettingsComponent(packageManager, resolveInfo);
                if (settingsComponent.isPresent()) {
                    screenSaverBean.screenSaverSettingElement = Utils.convertCpnToElement(settingsComponent.get());
                }
                arrayList.add(screenSaverBean);
            }
        }
        return arrayList;
    }

    private boolean isActiveScreenSaver(ElementName elementName, ElementName elementName2) {
        if (elementName == null || elementName2 == null) {
            return false;
        }
        String deviceId = elementName.getDeviceId();
        String bundleName = elementName.getBundleName();
        String abilityName = elementName.getAbilityName();
        if (bundleName == null || abilityName == null || deviceId == null || !bundleName.equals(elementName2.getBundleName()) || !abilityName.equals(elementName2.getAbilityName()) || !deviceId.equals(elementName2.getDeviceId())) {
            return false;
        }
        return true;
    }

    private static Optional<ElementName> getScreenSaverElementName(ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            return Optional.empty();
        }
        return Optional.of(new ElementName("", resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0076, code lost:
        r6 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0078, code lost:
        r6 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x007a, code lost:
        r6 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x008e, code lost:
        if (r2 == null) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0095, code lost:
        if (r2 == null) goto L_0x0098;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:9:0x001a, B:27:0x006f] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x007a A[ExcHandler: all (th java.lang.Throwable), Splitter:B:9:0x001a] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0086  */
    private static Optional<ComponentName> getSettingsComponent(PackageManager packageManager, ResolveInfo resolveInfo) {
        String str;
        Exception e;
        XmlResourceParser xmlResourceParser;
        Exception e2;
        if (resolveInfo == null || resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.metaData == null) {
            return Optional.empty();
        }
        Exception exc = null;
        try {
            xmlResourceParser = resolveInfo.serviceInfo.loadXmlMetaData(packageManager, "android.service.dream");
            if (xmlResourceParser == null) {
                try {
                    HiLog.info(TAG, "getSettingsComponentName failed, get null parser.", new Object[0]);
                    Optional<ComponentName> empty = Optional.empty();
                    if (xmlResourceParser != null) {
                        xmlResourceParser.close();
                    }
                    return empty;
                } catch (Exception e3) {
                    e2 = e3;
                    str = null;
                    exc = e2;
                } catch (Throwable th) {
                }
            } else {
                try {
                    Resources resourcesForApplication = packageManager.getResourcesForApplication(resolveInfo.serviceInfo.applicationInfo);
                    AttributeSet asAttributeSet = Xml.asAttributeSet(xmlResourceParser);
                    int next = xmlResourceParser.next();
                    while (next != 1 && next != 2) {
                        next = xmlResourceParser.next();
                    }
                    if (!"dream".equals(xmlResourceParser.getName())) {
                        HiLog.info(TAG, "getSettingsComponent failed, the tag not start with dream.", new Object[0]);
                        Optional<ComponentName> empty2 = Optional.empty();
                        xmlResourceParser.close();
                        return empty2;
                    }
                    TypedArray obtainAttributes = resourcesForApplication.obtainAttributes(asAttributeSet, R.styleable.Dream);
                    str = obtainAttributes.getString(0);
                    obtainAttributes.recycle();
                    xmlResourceParser.close();
                    if (exc != null) {
                        HiLog.info(TAG, "getSettingsComponent get exception.", new Object[0]);
                        return Optional.empty();
                    }
                    if (str != null && str.indexOf(47) < 0) {
                        str = resolveInfo.serviceInfo.packageName + "/" + str;
                    }
                    if (str == null) {
                        return Optional.empty();
                    }
                    return Optional.ofNullable(ComponentName.unflattenFromString(str));
                } catch (PackageManager.NameNotFoundException | IOException e4) {
                    e = e4;
                    str = null;
                    exc = e;
                }
            }
        } catch (PackageManager.NameNotFoundException | IOException e5) {
            e = e5;
            xmlResourceParser = null;
            str = null;
            exc = e;
        } catch (Exception e6) {
            e2 = e6;
            xmlResourceParser = null;
            str = null;
            exc = e2;
        } catch (Throwable th2) {
            Throwable th3 = th2;
            xmlResourceParser = null;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            throw th3;
        }
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public void setScreenSaverPolicy(int i) {
        setEnableScreenSaver(i != 3);
        if (i == 0) {
            setActivatedOnDock(false);
            setActivatedOnSleep(true);
        } else if (i == 1) {
            setActivatedOnDock(true);
            setActivatedOnSleep(false);
        } else if (i == 2) {
            setActivatedOnDock(true);
            setActivatedOnSleep(true);
        }
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public int getScreenSaverPolicy() {
        if (!isEnabled()) {
            return 3;
        }
        if (isActivatedOnDock() && isActivatedOnSleep()) {
            return 2;
        }
        if (isActivatedOnDock()) {
            return 1;
        }
        if (isActivatedOnSleep()) {
            return 0;
        }
        return 3;
    }

    private boolean isActivatedOnDock() {
        return getBoolean("screensaver_activate_on_dock", false);
    }

    private boolean isActivatedOnSleep() {
        return getBoolean("screensaver_activate_on_sleep", false);
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public void enable() {
        setEnableScreenSaver(true);
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public void disable() {
        setEnableScreenSaver(false);
    }

    private void setEnableScreenSaver(boolean z) {
        setBoolean("screensaver_enabled", z);
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public boolean isEnabled() {
        return getBoolean("screensaver_enabled", false);
    }

    private void setBoolean(String str, boolean z) {
        Context context = this.droidContext;
        if (context != null) {
            Settings.Secure.putInt(context.getContentResolver(), str, z ? 1 : 0);
        }
    }

    private boolean getBoolean(String str, boolean z) {
        Context context = this.droidContext;
        if (context != null) {
            return Settings.Secure.getInt(context.getContentResolver(), str, 0) != 0;
        }
        return z;
    }

    private void setActivatedOnDock(boolean z) {
        setBoolean("screensaver_activate_on_dock", z);
    }

    private void setActivatedOnSleep(boolean z) {
        setBoolean("screensaver_activate_on_sleep", z);
    }
}
