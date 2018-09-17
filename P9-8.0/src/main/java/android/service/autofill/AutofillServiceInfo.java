package android.service.autofill;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.RemoteException;
import android.util.Log;

public final class AutofillServiceInfo {
    private static final String TAG = "AutofillServiceInfo";
    private final ServiceInfo mServiceInfo;
    private final String mSettingsActivity;

    private static ServiceInfo getServiceInfoOrThrow(ComponentName comp, int userHandle) throws NameNotFoundException {
        try {
            ServiceInfo si = AppGlobals.getPackageManager().getServiceInfo(comp, 128, userHandle);
            if (si != null) {
                return si;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "catch a RemoteException in function getServiceInfoOrThrow");
        }
        throw new NameNotFoundException(comp.toString());
    }

    public AutofillServiceInfo(PackageManager pm, ComponentName comp, int userHandle) throws NameNotFoundException {
        this(pm, getServiceInfoOrThrow(comp, userHandle));
    }

    public AutofillServiceInfo(PackageManager pm, ServiceInfo si) {
        this.mServiceInfo = si;
        TypedArray metaDataArray = getMetaDataArray(pm, si);
        if (metaDataArray != null) {
            this.mSettingsActivity = metaDataArray.getString(0);
            metaDataArray.recycle();
            return;
        }
        this.mSettingsActivity = null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x007a A:{Splitter: B:9:0x0056, ExcHandler: org.xmlpull.v1.XmlPullParserException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:17:0x006b, code:
            if ("autofill-service".equals(r3.getName()) != false) goto L_0x0088;
     */
    /* JADX WARNING: Missing block: B:18:0x006d, code:
            android.util.Log.e(TAG, "Meta-data does not start with autofill-service tag");
     */
    /* JADX WARNING: Missing block: B:20:0x007a, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Log.e(TAG, "Error parsing auto fill service meta-data", r2);
     */
    /* JADX WARNING: Missing block: B:25:?, code:
            r0 = android.util.Xml.asAttributeSet(r3);
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            r6 = r9.getResourcesForApplication(r10.applicationInfo).obtainAttributes(r0, com.android.internal.R.styleable.AutofillService);
     */
    /* JADX WARNING: Missing block: B:30:0x0097, code:
            r3.close();
     */
    /* JADX WARNING: Missing block: B:31:0x009b, code:
            return r6;
     */
    /* JADX WARNING: Missing block: B:32:0x009c, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:34:?, code:
            android.util.Log.e(TAG, "Error getting application resources", r1);
     */
    /* JADX WARNING: Missing block: B:35:0x00a6, code:
            r3.close();
     */
    /* JADX WARNING: Missing block: B:36:0x00a9, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static TypedArray getMetaDataArray(PackageManager pm, ServiceInfo si) {
        if ("android.permission.BIND_AUTOFILL_SERVICE".equals(si.permission) || ("android.permission.BIND_AUTOFILL".equals(si.permission) ^ 1) == 0) {
            XmlResourceParser parser = si.loadXmlMetaData(pm, AutofillService.SERVICE_META_DATA);
            if (parser == null) {
                return null;
            }
            while (true) {
                try {
                    int type = parser.next();
                    if (type != 1) {
                    }
                    
/*
Method generation error in method: android.service.autofill.AutofillServiceInfo.getMetaDataArray(android.content.pm.PackageManager, android.content.pm.ServiceInfo):android.content.res.TypedArray, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x005e: IF  (r5_0 'type' int) == (2 int)  -> B:15:0x0060 in method: android.service.autofill.AutofillServiceInfo.getMetaDataArray(android.content.pm.PackageManager, android.content.pm.ServiceInfo):android.content.res.TypedArray, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:205)
	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:100)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:50)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:278)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:173)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:322)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:260)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:222)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: IF can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:446)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 37 more

*/

    public ServiceInfo getServiceInfo() {
        return this.mServiceInfo;
    }

    public String getSettingsActivity() {
        return this.mSettingsActivity;
    }
}
