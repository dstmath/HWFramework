package android.service.voice;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;
import android.security.keymaster.KeymasterDefs;

public class VoiceInteractionServiceInfo {
    static final String TAG = "VoiceInteractionServiceInfo";
    private String mParseError;
    private String mRecognitionService;
    private ServiceInfo mServiceInfo;
    private String mSessionService;
    private String mSettingsActivity;
    private boolean mSupportsAssist;
    private boolean mSupportsLaunchFromKeyguard;
    private boolean mSupportsLocalInteraction;

    public VoiceInteractionServiceInfo(android.content.pm.PackageManager r14, android.content.pm.ServiceInfo r15) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x013d in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r13 = this;
        r12 = 2;
        r11 = 1;
        r13.<init>();
        if (r15 != 0) goto L_0x000d;
    L_0x0007:
        r9 = "Service not available";
        r13.mParseError = r9;
        return;
    L_0x000d:
        r9 = "android.permission.BIND_VOICE_INTERACTION";
        r10 = r15.permission;
        r9 = r9.equals(r10);
        if (r9 != 0) goto L_0x001e;
    L_0x0018:
        r9 = "Service does not require permission android.permission.BIND_VOICE_INTERACTION";
        r13.mParseError = r9;
        return;
    L_0x001e:
        r6 = 0;
        r9 = "android.voice_interaction";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r6 = r15.loadXmlMetaData(r14, r9);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r6 != 0) goto L_0x0046;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
    L_0x0028:
        r9 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9.<init>();	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r10 = "No android.voice_interaction meta-data for ";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.append(r10);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r10 = r15.packageName;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.append(r10);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.toString();	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mParseError = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r6 == 0) goto L_0x0045;
    L_0x0042:
        r6.close();
    L_0x0045:
        return;
    L_0x0046:
        r9 = r15.applicationInfo;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r7 = r14.getResourcesForApplication(r9);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r1 = android.util.Xml.asAttributeSet(r6);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
    L_0x0050:
        r8 = r6.next();	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r8 == r11) goto L_0x0058;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
    L_0x0056:
        if (r8 != r12) goto L_0x0050;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
    L_0x0058:
        r5 = r6.getName();	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = "voice-interaction-service";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.equals(r5);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r9 != 0) goto L_0x0070;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
    L_0x0065:
        r9 = "Meta-data does not start with voice-interaction-service tag";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mParseError = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r6 == 0) goto L_0x006f;
    L_0x006c:
        r6.close();
    L_0x006f:
        return;
    L_0x0070:
        r9 = com.android.internal.R.styleable.VoiceInteractionService;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r0 = r7.obtainAttributes(r1, r9);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = 1;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r0.getString(r9);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mSessionService = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = 2;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r0.getString(r9);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mRecognitionService = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = 0;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r0.getString(r9);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mSettingsActivity = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = 3;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r10 = 0;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r0.getBoolean(r9, r10);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mSupportsAssist = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = 4;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r10 = 0;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r0.getBoolean(r9, r10);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mSupportsLaunchFromKeyguard = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = 5;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r10 = 0;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r0.getBoolean(r9, r10);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mSupportsLocalInteraction = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r0.recycle();	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r13.mSessionService;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r9 != 0) goto L_0x00b5;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
    L_0x00aa:
        r9 = "No sessionService specified";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mParseError = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r6 == 0) goto L_0x00b4;
    L_0x00b1:
        r6.close();
    L_0x00b4:
        return;
    L_0x00b5:
        r9 = r13.mRecognitionService;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r9 != 0) goto L_0x00c4;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
    L_0x00b9:
        r9 = "No recognitionService specified";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mParseError = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r6 == 0) goto L_0x00c3;
    L_0x00c0:
        r6.close();
    L_0x00c3:
        return;
    L_0x00c4:
        if (r6 == 0) goto L_0x00c9;
    L_0x00c6:
        r6.close();
    L_0x00c9:
        r13.mServiceInfo = r15;
        return;
    L_0x00cc:
        r2 = move-exception;
        r9 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9.<init>();	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r10 = "Error parsing voice interation service meta-data: ";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.append(r10);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.append(r2);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.toString();	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mParseError = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = "VoiceInteractionServiceInfo";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r10 = "error parsing voice interaction service meta-data";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        android.util.Log.w(r9, r10, r2);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r6 == 0) goto L_0x00f1;
    L_0x00ee:
        r6.close();
    L_0x00f1:
        return;
    L_0x00f2:
        r3 = move-exception;
        r9 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9.<init>();	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r10 = "Error parsing voice interation service meta-data: ";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.append(r10);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.append(r3);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.toString();	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mParseError = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = "VoiceInteractionServiceInfo";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r10 = "error parsing voice interaction service meta-data";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        android.util.Log.w(r9, r10, r3);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r6 == 0) goto L_0x0117;
    L_0x0114:
        r6.close();
    L_0x0117:
        return;
    L_0x0118:
        r4 = move-exception;
        r9 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9.<init>();	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r10 = "Error parsing voice interation service meta-data: ";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.append(r10);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.append(r4);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = r9.toString();	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r13.mParseError = r9;	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r9 = "VoiceInteractionServiceInfo";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        r10 = "error parsing voice interaction service meta-data";	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        android.util.Log.w(r9, r10, r4);	 Catch:{ XmlPullParserException -> 0x0118, IOException -> 0x00f2, NameNotFoundException -> 0x00cc, all -> 0x013e }
        if (r6 == 0) goto L_0x013d;
    L_0x013a:
        r6.close();
    L_0x013d:
        return;
    L_0x013e:
        r9 = move-exception;
        if (r6 == 0) goto L_0x0144;
    L_0x0141:
        r6.close();
    L_0x0144:
        throw r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.voice.VoiceInteractionServiceInfo.<init>(android.content.pm.PackageManager, android.content.pm.ServiceInfo):void");
    }

    public VoiceInteractionServiceInfo(PackageManager pm, ComponentName comp) throws NameNotFoundException {
        this(pm, pm.getServiceInfo(comp, KeymasterDefs.KM_ALGORITHM_HMAC));
    }

    public VoiceInteractionServiceInfo(PackageManager pm, ComponentName comp, int userHandle) throws NameNotFoundException {
        this(pm, getServiceInfoOrThrow(comp, userHandle));
    }

    static ServiceInfo getServiceInfoOrThrow(ComponentName comp, int userHandle) throws NameNotFoundException {
        try {
            ServiceInfo si = AppGlobals.getPackageManager().getServiceInfo(comp, 269222016, userHandle);
            if (si != null) {
                return si;
            }
        } catch (RemoteException e) {
        }
        throw new NameNotFoundException(comp.toString());
    }

    public String getParseError() {
        return this.mParseError;
    }

    public ServiceInfo getServiceInfo() {
        return this.mServiceInfo;
    }

    public String getSessionService() {
        return this.mSessionService;
    }

    public String getRecognitionService() {
        return this.mRecognitionService;
    }

    public String getSettingsActivity() {
        return this.mSettingsActivity;
    }

    public boolean getSupportsAssist() {
        return this.mSupportsAssist;
    }

    public boolean getSupportsLaunchFromKeyguard() {
        return this.mSupportsLaunchFromKeyguard;
    }

    public boolean getSupportsLocalInteraction() {
        return this.mSupportsLocalInteraction;
    }
}
