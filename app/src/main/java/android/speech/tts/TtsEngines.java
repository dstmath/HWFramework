package android.speech.tts;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.ProxyInfo;
import android.provider.DocumentsContract.Root;
import android.provider.Settings.Secure;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

public class TtsEngines {
    private static final boolean DBG = false;
    private static final String LOCALE_DELIMITER_NEW = "_";
    private static final String LOCALE_DELIMITER_OLD = "-";
    private static final String TAG = "TtsEngines";
    private static final String XML_TAG_NAME = "tts-engine";
    private static final Map<String, String> sNormalizeCountry = null;
    private static final Map<String, String> sNormalizeLanguage = null;
    private final Context mContext;

    private static class EngineInfoComparator implements Comparator<EngineInfo> {
        static EngineInfoComparator INSTANCE;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.speech.tts.TtsEngines.EngineInfoComparator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.speech.tts.TtsEngines.EngineInfoComparator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TtsEngines.EngineInfoComparator.<clinit>():void");
        }

        private EngineInfoComparator() {
        }

        public int compare(EngineInfo lhs, EngineInfo rhs) {
            if (lhs.system && !rhs.system) {
                return -1;
            }
            if (!rhs.system || lhs.system) {
                return rhs.priority - lhs.priority;
            }
            return 1;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.speech.tts.TtsEngines.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.speech.tts.TtsEngines.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TtsEngines.<clinit>():void");
    }

    private java.lang.String settingsActivityFromServiceInfo(android.content.pm.ServiceInfo r14, android.content.pm.PackageManager r15) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x010d in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r13 = this;
        r12 = 0;
        r5 = 0;
        r9 = "android.speech.tts";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r5 = r14.loadXmlMetaData(r15, r9);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        if (r5 != 0) goto L_0x002b;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
    L_0x000b:
        r9 = "TtsEngines";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10.<init>();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r11 = "No meta-data found for :";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r11);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r14);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.toString();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        android.util.Log.w(r9, r10);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        if (r5 == 0) goto L_0x002a;
    L_0x0027:
        r5.close();
    L_0x002a:
        return r12;
    L_0x002b:
        r9 = r14.applicationInfo;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r6 = r15.getResourcesForApplication(r9);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
    L_0x0031:
        r8 = r5.next();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r9 = 1;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        if (r8 == r9) goto L_0x008f;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
    L_0x0038:
        r9 = 2;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        if (r8 != r9) goto L_0x0031;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
    L_0x003b:
        r9 = "tts-engine";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r5.getName();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r9 = r9.equals(r10);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        if (r9 != 0) goto L_0x0077;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
    L_0x0048:
        r9 = "TtsEngines";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10.<init>();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r11 = "Package ";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r11);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r14);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r11 = " uses unknown tag :";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r11);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r11 = r5.getName();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r11);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.toString();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        android.util.Log.w(r9, r10);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        if (r5 == 0) goto L_0x0076;
    L_0x0073:
        r5.close();
    L_0x0076:
        return r12;
    L_0x0077:
        r1 = android.util.Xml.asAttributeSet(r5);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r9 = com.android.internal.R.styleable.TextToSpeechEngine;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r0 = r6.obtainAttributes(r1, r9);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r9 = 0;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r7 = r0.getString(r9);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r0.recycle();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        if (r5 == 0) goto L_0x008e;
    L_0x008b:
        r5.close();
    L_0x008e:
        return r7;
    L_0x008f:
        if (r5 == 0) goto L_0x0094;
    L_0x0091:
        r5.close();
    L_0x0094:
        return r12;
    L_0x0095:
        r3 = move-exception;
        r9 = "TtsEngines";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10.<init>();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r11 = "Error parsing metadata for ";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r11);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r14);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r11 = ":";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r11);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r3);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.toString();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        android.util.Log.w(r9, r10);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        if (r5 == 0) goto L_0x00c0;
    L_0x00bd:
        r5.close();
    L_0x00c0:
        return r12;
    L_0x00c1:
        r4 = move-exception;
        r9 = "TtsEngines";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10.<init>();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r11 = "Error parsing metadata for ";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r11);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r14);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r11 = ":";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r11);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r4);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.toString();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        android.util.Log.w(r9, r10);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        if (r5 == 0) goto L_0x00ec;
    L_0x00e9:
        r5.close();
    L_0x00ec:
        return r12;
    L_0x00ed:
        r2 = move-exception;
        r9 = "TtsEngines";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10.<init>();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r11 = "Could not load resources for : ";	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r11);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.append(r14);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        r10 = r10.toString();	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        android.util.Log.w(r9, r10);	 Catch:{ NameNotFoundException -> 0x00ed, XmlPullParserException -> 0x00c1, IOException -> 0x0095, all -> 0x010e }
        if (r5 == 0) goto L_0x010d;
    L_0x010a:
        r5.close();
    L_0x010d:
        return r12;
    L_0x010e:
        r9 = move-exception;
        if (r5 == 0) goto L_0x0114;
    L_0x0111:
        r5.close();
    L_0x0114:
        throw r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.TtsEngines.settingsActivityFromServiceInfo(android.content.pm.ServiceInfo, android.content.pm.PackageManager):java.lang.String");
    }

    public TtsEngines(Context ctx) {
        this.mContext = ctx;
    }

    public String getDefaultEngine() {
        String engine = Secure.getString(this.mContext.getContentResolver(), Secure.TTS_DEFAULT_SYNTH);
        return isEngineInstalled(engine) ? engine : getHighestRankedEngineName();
    }

    public String getHighestRankedEngineName() {
        List<EngineInfo> engines = getEngines();
        if (engines.size() <= 0 || !((EngineInfo) engines.get(0)).system) {
            return null;
        }
        return ((EngineInfo) engines.get(0)).name;
    }

    public EngineInfo getEngineInfo(String packageName) {
        PackageManager pm = this.mContext.getPackageManager();
        Intent intent = new Intent(Engine.INTENT_ACTION_TTS_SERVICE);
        intent.setPackage(packageName);
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, Root.FLAG_EMPTY);
        if (resolveInfos == null || resolveInfos.size() != 1) {
            return null;
        }
        return getEngineInfo((ResolveInfo) resolveInfos.get(0), pm);
    }

    public List<EngineInfo> getEngines() {
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(new Intent(Engine.INTENT_ACTION_TTS_SERVICE), Root.FLAG_EMPTY);
        if (resolveInfos == null) {
            return Collections.emptyList();
        }
        List<EngineInfo> engines = new ArrayList(resolveInfos.size());
        for (ResolveInfo resolveInfo : resolveInfos) {
            EngineInfo engine = getEngineInfo(resolveInfo, pm);
            if (engine != null) {
                engines.add(engine);
            }
        }
        Collections.sort(engines, EngineInfoComparator.INSTANCE);
        return engines;
    }

    private boolean isSystemEngine(ServiceInfo info) {
        ApplicationInfo appInfo = info.applicationInfo;
        if (appInfo == null || (appInfo.flags & 1) == 0) {
            return DBG;
        }
        return true;
    }

    public boolean isEngineInstalled(String engine) {
        boolean z = DBG;
        if (engine == null) {
            return DBG;
        }
        if (getEngineInfo(engine) != null) {
            z = true;
        }
        return z;
    }

    public Intent getSettingsIntent(String engine) {
        PackageManager pm = this.mContext.getPackageManager();
        Intent intent = new Intent(Engine.INTENT_ACTION_TTS_SERVICE);
        intent.setPackage(engine);
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, 65664);
        if (resolveInfos != null && resolveInfos.size() == 1) {
            ServiceInfo service = ((ResolveInfo) resolveInfos.get(0)).serviceInfo;
            if (service != null) {
                String settings = settingsActivityFromServiceInfo(service, pm);
                if (settings != null) {
                    Intent i = new Intent();
                    i.setClassName(engine, settings);
                    return i;
                }
            }
        }
        return null;
    }

    private EngineInfo getEngineInfo(ResolveInfo resolve, PackageManager pm) {
        ServiceInfo service = resolve.serviceInfo;
        if (service == null) {
            return null;
        }
        EngineInfo engine = new EngineInfo();
        engine.name = service.packageName;
        CharSequence label = service.loadLabel(pm);
        engine.label = TextUtils.isEmpty(label) ? engine.name : label.toString();
        engine.icon = service.getIconResource();
        engine.priority = resolve.priority;
        engine.system = isSystemEngine(service);
        return engine;
    }

    public Locale getLocalePrefForEngine(String engineName) {
        return getLocalePrefForEngine(engineName, Secure.getString(this.mContext.getContentResolver(), Secure.TTS_DEFAULT_LOCALE));
    }

    public Locale getLocalePrefForEngine(String engineName, String prefValue) {
        String localeString = parseEnginePrefFromList(prefValue, engineName);
        if (TextUtils.isEmpty(localeString)) {
            return Locale.getDefault();
        }
        Locale result = parseLocaleString(localeString);
        if (result == null) {
            Log.w(TAG, "Failed to parse locale " + localeString + ", returning en_US instead");
            result = Locale.US;
        }
        return result;
    }

    public boolean isLocaleSetToDefaultForEngine(String engineName) {
        return TextUtils.isEmpty(parseEnginePrefFromList(Secure.getString(this.mContext.getContentResolver(), Secure.TTS_DEFAULT_LOCALE), engineName));
    }

    public Locale parseLocaleString(String localeString) {
        String language = ProxyInfo.LOCAL_EXCL_LIST;
        String country = ProxyInfo.LOCAL_EXCL_LIST;
        String variant = ProxyInfo.LOCAL_EXCL_LIST;
        if (!TextUtils.isEmpty(localeString)) {
            String[] split = localeString.split("[-_]");
            language = split[0].toLowerCase();
            if (split.length == 0) {
                Log.w(TAG, "Failed to convert " + localeString + " to a valid Locale object. Only" + " separators");
                return null;
            } else if (split.length > 3) {
                Log.w(TAG, "Failed to convert " + localeString + " to a valid Locale object. Too" + " many separators");
                return null;
            } else {
                if (split.length >= 2) {
                    country = split[1].toUpperCase();
                }
                if (split.length >= 3) {
                    variant = split[2];
                }
            }
        }
        String normalizedLanguage = (String) sNormalizeLanguage.get(language);
        if (normalizedLanguage != null) {
            language = normalizedLanguage;
        }
        String normalizedCountry = (String) sNormalizeCountry.get(country);
        if (normalizedCountry != null) {
            country = normalizedCountry;
        }
        Locale result = new Locale(language, country, variant);
        try {
            result.getISO3Language();
            result.getISO3Country();
            return result;
        } catch (MissingResourceException e) {
            Log.w(TAG, "Failed to convert " + localeString + " to a valid Locale object.");
            return null;
        }
    }

    public static Locale normalizeTTSLocale(Locale ttsLocale) {
        String language = ttsLocale.getLanguage();
        if (!TextUtils.isEmpty(language)) {
            String normalizedLanguage = (String) sNormalizeLanguage.get(language);
            if (normalizedLanguage != null) {
                language = normalizedLanguage;
            }
        }
        String country = ttsLocale.getCountry();
        if (!TextUtils.isEmpty(country)) {
            String normalizedCountry = (String) sNormalizeCountry.get(country);
            if (normalizedCountry != null) {
                country = normalizedCountry;
            }
        }
        return new Locale(language, country, ttsLocale.getVariant());
    }

    public static String[] toOldLocaleStringFormat(Locale locale) {
        String[] ret = new String[]{ProxyInfo.LOCAL_EXCL_LIST, ProxyInfo.LOCAL_EXCL_LIST, ProxyInfo.LOCAL_EXCL_LIST};
        try {
            ret[0] = locale.getISO3Language();
            ret[1] = locale.getISO3Country();
            ret[2] = locale.getVariant();
            return ret;
        } catch (MissingResourceException e) {
            return new String[]{"eng", "USA", ProxyInfo.LOCAL_EXCL_LIST};
        }
    }

    private static String parseEnginePrefFromList(String prefValue, String engineName) {
        if (TextUtils.isEmpty(prefValue)) {
            return null;
        }
        for (String value : prefValue.split(",")) {
            int delimiter = value.indexOf(58);
            if (delimiter > 0 && engineName.equals(value.substring(0, delimiter))) {
                return value.substring(delimiter + 1);
            }
        }
        return null;
    }

    public synchronized void updateLocalePrefForEngine(String engineName, Locale newLocale) {
        Secure.putString(this.mContext.getContentResolver(), Secure.TTS_DEFAULT_LOCALE, updateValueInCommaSeparatedList(Secure.getString(this.mContext.getContentResolver(), Secure.TTS_DEFAULT_LOCALE), engineName, newLocale != null ? newLocale.toString() : ProxyInfo.LOCAL_EXCL_LIST).toString());
    }

    private String updateValueInCommaSeparatedList(String list, String key, String newValue) {
        StringBuilder newPrefList = new StringBuilder();
        if (TextUtils.isEmpty(list)) {
            newPrefList.append(key).append(':').append(newValue);
        } else {
            String[] prefValues = list.split(",");
            boolean first = true;
            boolean found = DBG;
            for (String value : prefValues) {
                int delimiter = value.indexOf(58);
                if (delimiter > 0) {
                    if (key.equals(value.substring(0, delimiter))) {
                        if (first) {
                            first = DBG;
                        } else {
                            newPrefList.append(',');
                        }
                        found = true;
                        newPrefList.append(key).append(':').append(newValue);
                    } else {
                        if (first) {
                            first = DBG;
                        } else {
                            newPrefList.append(',');
                        }
                        newPrefList.append(value);
                    }
                }
            }
            if (!found) {
                newPrefList.append(',');
                newPrefList.append(key).append(':').append(newValue);
            }
        }
        return newPrefList.toString();
    }
}
