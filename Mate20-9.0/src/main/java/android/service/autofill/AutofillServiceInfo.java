package android.service.autofill;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.metrics.LogMaker;
import android.os.RemoteException;
import android.telephony.SubscriptionPlan;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.PrintWriter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class AutofillServiceInfo {
    private static final String TAG = "AutofillServiceInfo";
    private static final String TAG_AUTOFILL_SERVICE = "autofill-service";
    private static final String TAG_COMPATIBILITY_PACKAGE = "compatibility-package";
    private final ArrayMap<String, Long> mCompatibilityPackages;
    private final ServiceInfo mServiceInfo;
    private final String mSettingsActivity;

    private static ServiceInfo getServiceInfoOrThrow(ComponentName comp, int userHandle) throws PackageManager.NameNotFoundException {
        try {
            ServiceInfo si = AppGlobals.getPackageManager().getServiceInfo(comp, 128, userHandle);
            if (si != null) {
                return si;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "catch a RemoteException in function getServiceInfoOrThrow");
        }
        throw new PackageManager.NameNotFoundException(comp.toString());
    }

    public AutofillServiceInfo(Context context, ComponentName comp, int userHandle) throws PackageManager.NameNotFoundException {
        this(context, getServiceInfoOrThrow(comp, userHandle));
    }

    /* JADX WARNING: type inference failed for: r1v2, types: [java.lang.String, android.util.ArrayMap<java.lang.String, java.lang.Long>, android.content.res.TypedArray] */
    public AutofillServiceInfo(Context context, ServiceInfo si) {
        if (!"android.permission.BIND_AUTOFILL_SERVICE".equals(si.permission)) {
            if ("android.permission.BIND_AUTOFILL".equals(si.permission)) {
                Log.w(TAG, "AutofillService from '" + si.packageName + "' uses unsupported permission " + "android.permission.BIND_AUTOFILL" + ". It works for now, but might not be supported on future releases");
                new MetricsLogger().write(new LogMaker(1289).setPackageName(si.packageName));
            } else {
                Log.w(TAG, "AutofillService from '" + si.packageName + "' does not require permission " + "android.permission.BIND_AUTOFILL_SERVICE");
                throw new SecurityException("Service does not require permission android.permission.BIND_AUTOFILL_SERVICE");
            }
        }
        this.mServiceInfo = si;
        XmlResourceParser parser = si.loadXmlMetaData(context.getPackageManager(), AutofillService.SERVICE_META_DATA);
        ? r1 = 0;
        if (parser == null) {
            this.mSettingsActivity = r1;
            this.mCompatibilityPackages = r1;
            return;
        }
        String settingsActivity = null;
        ArrayMap<String, Long> compatibilityPackages = r1;
        try {
            Resources resources = context.getPackageManager().getResourcesForApplication(si.applicationInfo);
            int type = 0;
            while (type != 1 && type != 2) {
                type = parser.next();
            }
            if (TAG_AUTOFILL_SERVICE.equals(parser.getName())) {
                TypedArray afsAttributes = resources.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.AutofillService);
                settingsActivity = afsAttributes.getString(0);
                if (afsAttributes != null) {
                    afsAttributes.recycle();
                }
                compatibilityPackages = parseCompatibilityPackages(parser, resources);
            } else {
                Log.e(TAG, "Meta-data does not start with autofill-service tag");
            }
        } catch (PackageManager.NameNotFoundException | IOException | XmlPullParserException e) {
            Log.e(TAG, "Error parsing auto fill service meta-data", e);
        } catch (Throwable th) {
            if (r1 != 0) {
                r1.recycle();
            }
            throw th;
        }
        this.mSettingsActivity = settingsActivity;
        this.mCompatibilityPackages = compatibilityPackages;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0058, code lost:
        if (r2 != null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005a, code lost:
        r2.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0092, code lost:
        if (r2 != null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00af, code lost:
        if (r2 == null) goto L_0x00da;
     */
    private ArrayMap<String, Long> parseCompatibilityPackages(XmlPullParser parser, Resources resources) throws IOException, XmlPullParserException {
        String maxVersionCodeStr;
        Long maxVersionCode;
        ArrayMap<String, Long> compatibilityPackages = null;
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                break;
            } else if (!(type == 3 || type == 4 || !TAG_COMPATIBILITY_PACKAGE.equals(parser.getName()))) {
                TypedArray cpAttributes = null;
                try {
                    cpAttributes = resources.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.AutofillService_CompatibilityPackage);
                    String name = cpAttributes.getString(0);
                    if (TextUtils.isEmpty(name)) {
                        Log.e(TAG, "Invalid compatibility package:" + name);
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        maxVersionCodeStr = cpAttributes.getString(1);
                        if (maxVersionCodeStr != null) {
                            maxVersionCode = Long.valueOf(Long.parseLong(maxVersionCodeStr));
                            if (maxVersionCode.longValue() < 0) {
                                Log.e(TAG, "Invalid compatibility max version code:" + maxVersionCode);
                                XmlUtils.skipCurrentTag(parser);
                            }
                        } else {
                            maxVersionCode = Long.valueOf(SubscriptionPlan.BYTES_UNLIMITED);
                        }
                        if (compatibilityPackages == null) {
                            compatibilityPackages = new ArrayMap<>();
                        }
                        compatibilityPackages.put(name, maxVersionCode);
                        XmlUtils.skipCurrentTag(parser);
                        if (cpAttributes != null) {
                            cpAttributes.recycle();
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid compatibility max version code:" + maxVersionCodeStr);
                    XmlUtils.skipCurrentTag(parser);
                } catch (Throwable th) {
                    XmlUtils.skipCurrentTag(parser);
                    if (cpAttributes != null) {
                        cpAttributes.recycle();
                    }
                    throw th;
                }
            }
        }
        return compatibilityPackages;
    }

    public ServiceInfo getServiceInfo() {
        return this.mServiceInfo;
    }

    public String getSettingsActivity() {
        return this.mSettingsActivity;
    }

    public ArrayMap<String, Long> getCompatibilityPackages() {
        return this.mCompatibilityPackages;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[");
        builder.append(this.mServiceInfo);
        builder.append(", settings:");
        builder.append(this.mSettingsActivity);
        builder.append(", hasCompatPckgs:");
        builder.append(this.mCompatibilityPackages != null && !this.mCompatibilityPackages.isEmpty());
        builder.append("]");
        return builder.toString();
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("Component: ");
        pw.println(getServiceInfo().getComponentName());
        pw.print(prefix);
        pw.print("Settings: ");
        pw.println(this.mSettingsActivity);
        pw.print(prefix);
        pw.print("Compat packages: ");
        pw.println(this.mCompatibilityPackages);
    }
}
