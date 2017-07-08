package android.app;

import android.os.Binder;
import android.os.IBinder;

public abstract class ApplicationThreadNative extends Binder implements IApplicationThread {
    public boolean onTransact(int r129, android.os.Parcel r130, android.os.Parcel r131, int r132) throws android.os.RemoteException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:262:0x0788
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r128 = this;
        switch(r129) {
            case 1: goto L_0x0008;
            case 2: goto L_0x0003;
            case 3: goto L_0x003a;
            case 4: goto L_0x005e;
            case 5: goto L_0x009e;
            case 6: goto L_0x00ca;
            case 7: goto L_0x00e7;
            case 8: goto L_0x01f2;
            case 9: goto L_0x020f;
            case 10: goto L_0x0233;
            case 11: goto L_0x0285;
            case 12: goto L_0x0342;
            case 13: goto L_0x0357;
            case 14: goto L_0x040a;
            case 15: goto L_0x0003;
            case 16: goto L_0x0424;
            case 17: goto L_0x0309;
            case 18: goto L_0x043f;
            case 19: goto L_0x0486;
            case 20: goto L_0x02b8;
            case 21: goto L_0x02ea;
            case 22: goto L_0x0493;
            case 23: goto L_0x04e7;
            case 24: goto L_0x053a;
            case 25: goto L_0x0547;
            case 26: goto L_0x018c;
            case 27: goto L_0x007e;
            case 28: goto L_0x059b;
            case 29: goto L_0x05d4;
            case 30: goto L_0x05e9;
            case 31: goto L_0x0616;
            case 32: goto L_0x0852;
            case 33: goto L_0x0417;
            case 34: goto L_0x063d;
            case 35: goto L_0x0658;
            case 36: goto L_0x066d;
            case 37: goto L_0x06a6;
            case 38: goto L_0x044c;
            case 39: goto L_0x0459;
            case 40: goto L_0x06d6;
            case 41: goto L_0x06eb;
            case 42: goto L_0x070c;
            case 43: goto L_0x0721;
            case 44: goto L_0x078e;
            case 45: goto L_0x04bd;
            case 46: goto L_0x07bc;
            case 47: goto L_0x07ea;
            case 48: goto L_0x0802;
            case 49: goto L_0x082c;
            case 50: goto L_0x0877;
            case 51: goto L_0x088f;
            case 52: goto L_0x08ad;
            case 53: goto L_0x08cb;
            case 54: goto L_0x08e3;
            case 55: goto L_0x0908;
            case 56: goto L_0x0920;
            case 57: goto L_0x0938;
            case 58: goto L_0x0945;
            case 59: goto L_0x0963;
            case 60: goto L_0x0983;
            case 61: goto L_0x057c;
            default: goto L_0x0003;
        };
    L_0x0003:
        r5 = super.onTransact(r129, r130, r131, r132);
        return r5;
    L_0x0008:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r6 = r130.readStrongBinder();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0034;
    L_0x001a:
        r7 = 1;
    L_0x001b:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0036;
    L_0x0021:
        r8 = 1;
    L_0x0022:
        r9 = r130.readInt();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0038;
    L_0x002c:
        r10 = 1;
    L_0x002d:
        r5 = r128;
        r5.schedulePauseActivity(r6, r7, r8, r9, r10);
        r5 = 1;
        return r5;
    L_0x0034:
        r7 = 0;
        goto L_0x001b;
    L_0x0036:
        r8 = 0;
        goto L_0x0022;
    L_0x0038:
        r10 = 0;
        goto L_0x002d;
    L_0x003a:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r6 = r130.readStrongBinder();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x005b;
    L_0x004c:
        r123 = 1;
    L_0x004e:
        r9 = r130.readInt();
        r0 = r128;
        r1 = r123;
        r0.scheduleStopActivity(r6, r1, r9);
        r5 = 1;
        return r5;
    L_0x005b:
        r123 = 0;
        goto L_0x004e;
    L_0x005e:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r6 = r130.readStrongBinder();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x007b;
    L_0x0070:
        r123 = 1;
    L_0x0072:
        r0 = r128;
        r1 = r123;
        r0.scheduleWindowVisibility(r6, r1);
        r5 = 1;
        return r5;
    L_0x007b:
        r123 = 0;
        goto L_0x0072;
    L_0x007e:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r6 = r130.readStrongBinder();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x009b;
    L_0x0090:
        r124 = 1;
    L_0x0092:
        r0 = r128;
        r1 = r124;
        r0.scheduleSleeping(r6, r1);
        r5 = 1;
        return r5;
    L_0x009b:
        r124 = 0;
        goto L_0x0092;
    L_0x009e:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r6 = r130.readStrongBinder();
        r21 = r130.readInt();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x00c7;
    L_0x00b4:
        r27 = 1;
    L_0x00b6:
        r119 = r130.readBundle();
        r0 = r128;
        r1 = r21;
        r2 = r27;
        r3 = r119;
        r0.scheduleResumeActivity(r6, r1, r2, r3);
        r5 = 1;
        return r5;
    L_0x00c7:
        r27 = 0;
        goto L_0x00b6;
    L_0x00ca:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r6 = r130.readStrongBinder();
        r5 = android.app.ResultInfo.CREATOR;
        r0 = r130;
        r24 = r0.createTypedArrayList(r5);
        r0 = r128;
        r1 = r24;
        r0.scheduleSendResult(r6, r1);
        r5 = 1;
        return r5;
    L_0x00e7:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r5 = android.content.Intent.CREATOR;
        r0 = r130;
        r12 = r5.createFromParcel(r0);
        r12 = (android.content.Intent) r12;
        r6 = r130.readStrongBinder();
        r14 = r130.readInt();
        r5 = android.content.pm.ActivityInfo.CREATOR;
        r0 = r130;
        r15 = r5.createFromParcel(r0);
        r15 = (android.content.pm.ActivityInfo) r15;
        r5 = android.content.res.Configuration.CREATOR;
        r0 = r130;
        r16 = r5.createFromParcel(r0);
        r16 = (android.content.res.Configuration) r16;
        r17 = 0;
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0127;
    L_0x011d:
        r5 = android.content.res.Configuration.CREATOR;
        r0 = r130;
        r17 = r5.createFromParcel(r0);
        r17 = (android.content.res.Configuration) r17;
    L_0x0127:
        r5 = android.content.res.CompatibilityInfo.CREATOR;
        r0 = r130;
        r18 = r5.createFromParcel(r0);
        r18 = (android.content.res.CompatibilityInfo) r18;
        r19 = r130.readString();
        r5 = r130.readStrongBinder();
        r20 = com.android.internal.app.IVoiceInteractor.Stub.asInterface(r5);
        r21 = r130.readInt();
        r22 = r130.readBundle();
        r23 = r130.readPersistableBundle();
        r5 = android.app.ResultInfo.CREATOR;
        r0 = r130;
        r24 = r0.createTypedArrayList(r5);
        r5 = com.android.internal.content.ReferrerIntent.CREATOR;
        r0 = r130;
        r25 = r0.createTypedArrayList(r5);
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0183;
    L_0x015f:
        r26 = 1;
    L_0x0161:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0186;
    L_0x0167:
        r27 = 1;
    L_0x0169:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0189;
    L_0x016f:
        r5 = android.app.ProfilerInfo.CREATOR;
        r0 = r130;
        r5 = r5.createFromParcel(r0);
        r5 = (android.app.ProfilerInfo) r5;
        r28 = r5;
    L_0x017b:
        r11 = r128;
        r13 = r6;
        r11.scheduleLaunchActivity(r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28);
        r5 = 1;
        return r5;
    L_0x0183:
        r26 = 0;
        goto L_0x0161;
    L_0x0186:
        r27 = 0;
        goto L_0x0169;
    L_0x0189:
        r28 = 0;
        goto L_0x017b;
    L_0x018c:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r6 = r130.readStrongBinder();
        r5 = android.app.ResultInfo.CREATOR;
        r0 = r130;
        r24 = r0.createTypedArrayList(r5);
        r5 = com.android.internal.content.ReferrerIntent.CREATOR;
        r0 = r130;
        r25 = r0.createTypedArrayList(r5);
        r9 = r130.readInt();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x01ec;
    L_0x01b2:
        r26 = 1;
    L_0x01b4:
        r5 = android.content.res.Configuration.CREATOR;
        r0 = r130;
        r35 = r5.createFromParcel(r0);
        r35 = (android.content.res.Configuration) r35;
        r17 = 0;
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x01d0;
    L_0x01c6:
        r5 = android.content.res.Configuration.CREATOR;
        r0 = r130;
        r17 = r5.createFromParcel(r0);
        r17 = (android.content.res.Configuration) r17;
    L_0x01d0:
        r5 = r130.readInt();
        r11 = 1;
        if (r5 != r11) goto L_0x01ef;
    L_0x01d7:
        r37 = 1;
    L_0x01d9:
        r29 = r128;
        r30 = r6;
        r31 = r24;
        r32 = r25;
        r33 = r9;
        r34 = r26;
        r36 = r17;
        r29.scheduleRelaunchActivity(r30, r31, r32, r33, r34, r35, r36, r37);
        r5 = 1;
        return r5;
    L_0x01ec:
        r26 = 0;
        goto L_0x01b4;
    L_0x01ef:
        r37 = 0;
        goto L_0x01d9;
    L_0x01f2:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r5 = com.android.internal.content.ReferrerIntent.CREATOR;
        r0 = r130;
        r25 = r0.createTypedArrayList(r5);
        r6 = r130.readStrongBinder();
        r0 = r128;
        r1 = r25;
        r0.scheduleNewIntent(r1, r6);
        r5 = 1;
        return r5;
    L_0x020f:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r6 = r130.readStrongBinder();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0230;
    L_0x0221:
        r94 = 1;
    L_0x0223:
        r9 = r130.readInt();
        r0 = r128;
        r1 = r94;
        r0.scheduleDestroyActivity(r6, r1, r9);
        r5 = 1;
        return r5;
    L_0x0230:
        r94 = 0;
        goto L_0x0223;
    L_0x0233:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r5 = android.content.Intent.CREATOR;
        r0 = r130;
        r12 = r5.createFromParcel(r0);
        r12 = (android.content.Intent) r12;
        r5 = android.content.pm.ActivityInfo.CREATOR;
        r0 = r130;
        r15 = r5.createFromParcel(r0);
        r15 = (android.content.pm.ActivityInfo) r15;
        r5 = android.content.res.CompatibilityInfo.CREATOR;
        r0 = r130;
        r18 = r5.createFromParcel(r0);
        r18 = (android.content.res.CompatibilityInfo) r18;
        r42 = r130.readInt();
        r43 = r130.readString();
        r44 = r130.readBundle();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0282;
    L_0x026b:
        r45 = 1;
    L_0x026d:
        r46 = r130.readInt();
        r47 = r130.readInt();
        r38 = r128;
        r39 = r12;
        r40 = r15;
        r41 = r18;
        r38.scheduleReceiver(r39, r40, r41, r42, r43, r44, r45, r46, r47);
        r5 = 1;
        return r5;
    L_0x0282:
        r45 = 0;
        goto L_0x026d;
    L_0x0285:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r30 = r130.readStrongBinder();
        r5 = android.content.pm.ServiceInfo.CREATOR;
        r0 = r130;
        r99 = r5.createFromParcel(r0);
        r99 = (android.content.pm.ServiceInfo) r99;
        r5 = android.content.res.CompatibilityInfo.CREATOR;
        r0 = r130;
        r18 = r5.createFromParcel(r0);
        r18 = (android.content.res.CompatibilityInfo) r18;
        r47 = r130.readInt();
        r0 = r128;
        r1 = r30;
        r2 = r99;
        r3 = r18;
        r4 = r47;
        r0.scheduleCreateService(r1, r2, r3, r4);
        r5 = 1;
        return r5;
    L_0x02b8:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r30 = r130.readStrongBinder();
        r5 = android.content.Intent.CREATOR;
        r0 = r130;
        r12 = r5.createFromParcel(r0);
        r12 = (android.content.Intent) r12;
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x02e7;
    L_0x02d4:
        r115 = 1;
    L_0x02d6:
        r47 = r130.readInt();
        r0 = r128;
        r1 = r30;
        r2 = r115;
        r3 = r47;
        r0.scheduleBindService(r1, r12, r2, r3);
        r5 = 1;
        return r5;
    L_0x02e7:
        r115 = 0;
        goto L_0x02d6;
    L_0x02ea:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r30 = r130.readStrongBinder();
        r5 = android.content.Intent.CREATOR;
        r0 = r130;
        r12 = r5.createFromParcel(r0);
        r12 = (android.content.Intent) r12;
        r0 = r128;
        r1 = r30;
        r0.scheduleUnbindService(r1, r12);
        r5 = 1;
        return r5;
    L_0x0309:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r30 = r130.readStrongBinder();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x033c;
    L_0x031b:
        r31 = 1;
    L_0x031d:
        r32 = r130.readInt();
        r33 = r130.readInt();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x033f;
    L_0x032b:
        r5 = android.content.Intent.CREATOR;
        r0 = r130;
        r34 = r5.createFromParcel(r0);
        r34 = (android.content.Intent) r34;
    L_0x0335:
        r29 = r128;
        r29.scheduleServiceArgs(r30, r31, r32, r33, r34);
        r5 = 1;
        return r5;
    L_0x033c:
        r31 = 0;
        goto L_0x031d;
    L_0x033f:
        r34 = 0;
        goto L_0x0335;
    L_0x0342:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r30 = r130.readStrongBinder();
        r0 = r128;
        r1 = r30;
        r0.scheduleStopService(r1);
        r5 = 1;
        return r5;
    L_0x0357:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r49 = r130.readString();
        r5 = android.content.pm.ApplicationInfo.CREATOR;
        r0 = r130;
        r50 = r5.createFromParcel(r0);
        r50 = (android.content.pm.ApplicationInfo) r50;
        r5 = android.content.pm.ProviderInfo.CREATOR;
        r0 = r130;
        r51 = r0.createTypedArrayList(r5);
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x03f8;
    L_0x037b:
        r52 = new android.content.ComponentName;
        r0 = r52;
        r1 = r130;
        r0.<init>(r1);
    L_0x0384:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x03fb;
    L_0x038a:
        r5 = android.app.ProfilerInfo.CREATOR;
        r0 = r130;
        r28 = r5.createFromParcel(r0);
        r28 = (android.app.ProfilerInfo) r28;
    L_0x0394:
        r54 = r130.readBundle();
        r87 = r130.readStrongBinder();
        r55 = android.app.IInstrumentationWatcher.Stub.asInterface(r87);
        r87 = r130.readStrongBinder();
        r56 = android.app.IUiAutomationConnection.Stub.asInterface(r87);
        r57 = r130.readInt();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x03fe;
    L_0x03b2:
        r58 = 1;
    L_0x03b4:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0401;
    L_0x03ba:
        r59 = 1;
    L_0x03bc:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0404;
    L_0x03c2:
        r60 = 1;
    L_0x03c4:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0407;
    L_0x03ca:
        r61 = 1;
    L_0x03cc:
        r5 = android.content.res.Configuration.CREATOR;
        r0 = r130;
        r35 = r5.createFromParcel(r0);
        r35 = (android.content.res.Configuration) r35;
        r5 = android.content.res.CompatibilityInfo.CREATOR;
        r0 = r130;
        r18 = r5.createFromParcel(r0);
        r18 = (android.content.res.CompatibilityInfo) r18;
        r5 = 0;
        r0 = r130;
        r64 = r0.readHashMap(r5);
        r65 = r130.readBundle();
        r48 = r128;
        r53 = r28;
        r62 = r35;
        r63 = r18;
        r48.bindApplication(r49, r50, r51, r52, r53, r54, r55, r56, r57, r58, r59, r60, r61, r62, r63, r64, r65);
        r5 = 1;
        return r5;
    L_0x03f8:
        r52 = 0;
        goto L_0x0384;
    L_0x03fb:
        r28 = 0;
        goto L_0x0394;
    L_0x03fe:
        r58 = 0;
        goto L_0x03b4;
    L_0x0401:
        r59 = 0;
        goto L_0x03bc;
    L_0x0404:
        r60 = 0;
        goto L_0x03c4;
    L_0x0407:
        r61 = 0;
        goto L_0x03cc;
    L_0x040a:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r128.scheduleExit();
        r5 = 1;
        return r5;
    L_0x0417:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r128.scheduleSuicide();
        r5 = 1;
        return r5;
    L_0x0424:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r5 = android.content.res.Configuration.CREATOR;
        r0 = r130;
        r35 = r5.createFromParcel(r0);
        r35 = (android.content.res.Configuration) r35;
        r0 = r128;
        r1 = r35;
        r0.scheduleConfigurationChanged(r1);
        r5 = 1;
        return r5;
    L_0x043f:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r128.updateTimeZone();
        r5 = 1;
        return r5;
    L_0x044c:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r128.clearDnsCache();
        r5 = 1;
        return r5;
    L_0x0459:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r114 = r130.readString();
        r109 = r130.readString();
        r92 = r130.readString();
        r5 = android.net.Uri.CREATOR;
        r0 = r130;
        r105 = r5.createFromParcel(r0);
        r105 = (android.net.Uri) r105;
        r0 = r128;
        r1 = r114;
        r2 = r109;
        r3 = r92;
        r4 = r105;
        r0.setHttpProxy(r1, r2, r3, r4);
        r5 = 1;
        return r5;
    L_0x0486:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r128.processInBackground();
        r5 = 1;
        return r5;
    L_0x0493:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r93 = r130.readFileDescriptor();
        r120 = r130.readStrongBinder();
        r82 = r130.readStringArray();
        if (r93 == 0) goto L_0x04b9;
    L_0x04a9:
        r5 = r93.getFileDescriptor();
        r0 = r128;
        r1 = r120;
        r2 = r82;
        r0.dumpService(r5, r1, r2);
        r93.close();	 Catch:{ IOException -> 0x04bb }
    L_0x04b9:
        r5 = 1;
        return r5;
    L_0x04bb:
        r90 = move-exception;
        goto L_0x04b9;
    L_0x04bd:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r93 = r130.readFileDescriptor();
        r120 = r130.readStrongBinder();
        r82 = r130.readStringArray();
        if (r93 == 0) goto L_0x04e3;
    L_0x04d3:
        r5 = r93.getFileDescriptor();
        r0 = r128;
        r1 = r120;
        r2 = r82;
        r0.dumpProvider(r5, r1, r2);
        r93.close();	 Catch:{ IOException -> 0x04e5 }
    L_0x04e3:
        r5 = 1;
        return r5;
    L_0x04e5:
        r90 = move-exception;
        goto L_0x04e3;
    L_0x04e7:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r5 = r130.readStrongBinder();
        r67 = android.content.IIntentReceiver.Stub.asInterface(r5);
        r5 = android.content.Intent.CREATOR;
        r0 = r130;
        r12 = r5.createFromParcel(r0);
        r12 = (android.content.Intent) r12;
        r42 = r130.readInt();
        r70 = r130.readString();
        r71 = r130.readBundle();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0534;
    L_0x0513:
        r72 = 1;
    L_0x0515:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0537;
    L_0x051b:
        r73 = 1;
    L_0x051d:
        r46 = r130.readInt();
        r47 = r130.readInt();
        r66 = r128;
        r68 = r12;
        r69 = r42;
        r74 = r46;
        r75 = r47;
        r66.scheduleRegisteredReceiver(r67, r68, r69, r70, r71, r72, r73, r74, r75);
        r5 = 1;
        return r5;
    L_0x0534:
        r72 = 0;
        goto L_0x0515;
    L_0x0537:
        r73 = 0;
        goto L_0x051d;
    L_0x053a:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r128.scheduleLowMemory();
        r5 = 1;
        return r5;
    L_0x0547:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r6 = r130.readStrongBinder();
        r17 = 0;
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0565;
    L_0x055b:
        r5 = android.content.res.Configuration.CREATOR;
        r0 = r130;
        r17 = r5.createFromParcel(r0);
        r17 = (android.content.res.Configuration) r17;
    L_0x0565:
        r5 = r130.readInt();
        r11 = 1;
        if (r5 != r11) goto L_0x0579;
    L_0x056c:
        r116 = 1;
    L_0x056e:
        r0 = r128;
        r1 = r17;
        r2 = r116;
        r0.scheduleActivityConfigurationChanged(r6, r1, r2);
        r5 = 1;
        return r5;
    L_0x0579:
        r116 = 0;
        goto L_0x056e;
    L_0x057c:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r30 = r130.readStrongBinder();
        r5 = r130.readStrongBinder();
        r20 = com.android.internal.app.IVoiceInteractor.Stub.asInterface(r5);
        r0 = r128;
        r1 = r30;
        r2 = r20;
        r0.scheduleLocalVoiceInteractionStarted(r1, r2);
        r5 = 1;
        return r5;
    L_0x059b:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x05ce;
    L_0x05a9:
        r125 = 1;
    L_0x05ab:
        r111 = r130.readInt();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x05d1;
    L_0x05b5:
        r5 = android.app.ProfilerInfo.CREATOR;
        r0 = r130;
        r5 = r5.createFromParcel(r0);
        r5 = (android.app.ProfilerInfo) r5;
        r28 = r5;
    L_0x05c1:
        r0 = r128;
        r1 = r125;
        r2 = r28;
        r3 = r111;
        r0.profilerControl(r1, r2, r3);
        r5 = 1;
        return r5;
    L_0x05ce:
        r125 = 0;
        goto L_0x05ab;
    L_0x05d1:
        r28 = 0;
        goto L_0x05c1;
    L_0x05d4:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r96 = r130.readInt();
        r0 = r128;
        r1 = r96;
        r0.setSchedulingGroup(r1);
        r5 = 1;
        return r5;
    L_0x05e9:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r5 = android.content.pm.ApplicationInfo.CREATOR;
        r0 = r130;
        r85 = r5.createFromParcel(r0);
        r85 = (android.content.pm.ApplicationInfo) r85;
        r5 = android.content.res.CompatibilityInfo.CREATOR;
        r0 = r130;
        r18 = r5.createFromParcel(r0);
        r18 = (android.content.res.CompatibilityInfo) r18;
        r86 = r130.readInt();
        r0 = r128;
        r1 = r85;
        r2 = r18;
        r3 = r86;
        r0.scheduleCreateBackupAgent(r1, r2, r3);
        r5 = 1;
        return r5;
    L_0x0616:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r5 = android.content.pm.ApplicationInfo.CREATOR;
        r0 = r130;
        r85 = r5.createFromParcel(r0);
        r85 = (android.content.pm.ApplicationInfo) r85;
        r5 = android.content.res.CompatibilityInfo.CREATOR;
        r0 = r130;
        r18 = r5.createFromParcel(r0);
        r18 = (android.content.res.CompatibilityInfo) r18;
        r0 = r128;
        r1 = r85;
        r2 = r18;
        r0.scheduleDestroyBackupAgent(r1, r2);
        r5 = 1;
        return r5;
    L_0x063d:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r88 = r130.readInt();
        r106 = r130.readStringArray();
        r0 = r128;
        r1 = r88;
        r2 = r106;
        r0.dispatchPackageBroadcast(r1, r2);
        r5 = 1;
        return r5;
    L_0x0658:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r103 = r130.readString();
        r0 = r128;
        r1 = r103;
        r0.scheduleCrash(r1);
        r5 = 1;
        return r5;
    L_0x066d:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x06a0;
    L_0x067b:
        r102 = 1;
    L_0x067d:
        r107 = r130.readString();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x06a3;
    L_0x0687:
        r5 = android.os.ParcelFileDescriptor.CREATOR;
        r0 = r130;
        r5 = r5.createFromParcel(r0);
        r5 = (android.os.ParcelFileDescriptor) r5;
        r93 = r5;
    L_0x0693:
        r0 = r128;
        r1 = r102;
        r2 = r107;
        r3 = r93;
        r0.dumpHeap(r1, r2, r3);
        r5 = 1;
        return r5;
    L_0x06a0:
        r102 = 0;
        goto L_0x067d;
    L_0x06a3:
        r93 = 0;
        goto L_0x0693;
    L_0x06a6:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r93 = r130.readFileDescriptor();
        r83 = r130.readStrongBinder();
        r110 = r130.readString();
        r82 = r130.readStringArray();
        if (r93 == 0) goto L_0x06d2;
    L_0x06c0:
        r5 = r93.getFileDescriptor();
        r0 = r128;
        r1 = r83;
        r2 = r110;
        r3 = r82;
        r0.dumpActivity(r5, r1, r2, r3);
        r93.close();	 Catch:{ IOException -> 0x06d4 }
    L_0x06d2:
        r5 = 1;
        return r5;
    L_0x06d4:
        r90 = move-exception;
        goto L_0x06d2;
    L_0x06d6:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r122 = r130.readBundle();
        r0 = r128;
        r1 = r122;
        r0.setCoreSettings(r1);
        r5 = 1;
        return r5;
    L_0x06eb:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r108 = r130.readString();
        r5 = android.content.res.CompatibilityInfo.CREATOR;
        r0 = r130;
        r89 = r5.createFromParcel(r0);
        r89 = (android.content.res.CompatibilityInfo) r89;
        r0 = r128;
        r1 = r108;
        r2 = r89;
        r0.updatePackageCompatibilityInfo(r1, r2);
        r5 = 1;
        return r5;
    L_0x070c:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r101 = r130.readInt();
        r0 = r128;
        r1 = r101;
        r0.scheduleTrimMemory(r1);
        r5 = 1;
        return r5;
    L_0x0721:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r93 = r130.readFileDescriptor();
        r5 = android.os.Debug.MemoryInfo.CREATOR;
        r0 = r130;
        r76 = r5.createFromParcel(r0);
        r76 = (android.os.Debug.MemoryInfo) r76;
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0776;
    L_0x073d:
        r77 = 1;
    L_0x073f:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0779;
    L_0x0745:
        r78 = 1;
    L_0x0747:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x077c;
    L_0x074d:
        r79 = 1;
    L_0x074f:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x077f;
    L_0x0755:
        r80 = 1;
    L_0x0757:
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0782;
    L_0x075d:
        r81 = 1;
    L_0x075f:
        r82 = r130.readStringArray();
        if (r93 == 0) goto L_0x0771;
    L_0x0765:
        r75 = r93.getFileDescriptor();	 Catch:{ all -> 0x0787 }
        r74 = r128;	 Catch:{ all -> 0x0787 }
        r74.dumpMemInfo(r75, r76, r77, r78, r79, r80, r81, r82);	 Catch:{ all -> 0x0787 }
        r93.close();
    L_0x0771:
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x0776:
        r77 = 0;
        goto L_0x073f;
    L_0x0779:
        r78 = 0;
        goto L_0x0747;
    L_0x077c:
        r79 = 0;
        goto L_0x074f;
    L_0x077f:
        r80 = 0;
        goto L_0x0757;
    L_0x0782:
        r81 = 0;
        goto L_0x075f;
    L_0x0785:
        r90 = move-exception;
        goto L_0x0771;
    L_0x0787:
        r5 = move-exception;
        r93.close();	 Catch:{ IOException -> 0x078c }
    L_0x078b:
        throw r5;
    L_0x078c:
        r90 = move-exception;
        goto L_0x078b;
    L_0x078e:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r93 = r130.readFileDescriptor();
        r82 = r130.readStringArray();
        if (r93 == 0) goto L_0x07ae;
    L_0x07a0:
        r5 = r93.getFileDescriptor();	 Catch:{ all -> 0x07b5 }
        r0 = r128;	 Catch:{ all -> 0x07b5 }
        r1 = r82;	 Catch:{ all -> 0x07b5 }
        r0.dumpGfxInfo(r5, r1);	 Catch:{ all -> 0x07b5 }
        r93.close();
    L_0x07ae:
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x07b3:
        r90 = move-exception;
        goto L_0x07ae;
    L_0x07b5:
        r5 = move-exception;
        r93.close();	 Catch:{ IOException -> 0x07ba }
    L_0x07b9:
        throw r5;
    L_0x07ba:
        r90 = move-exception;
        goto L_0x07b9;
    L_0x07bc:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r93 = r130.readFileDescriptor();
        r82 = r130.readStringArray();
        if (r93 == 0) goto L_0x07dc;
    L_0x07ce:
        r5 = r93.getFileDescriptor();	 Catch:{ all -> 0x07e3 }
        r0 = r128;	 Catch:{ all -> 0x07e3 }
        r1 = r82;	 Catch:{ all -> 0x07e3 }
        r0.dumpDbInfo(r5, r1);	 Catch:{ all -> 0x07e3 }
        r93.close();
    L_0x07dc:
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x07e1:
        r90 = move-exception;
        goto L_0x07dc;
    L_0x07e3:
        r5 = move-exception;
        r93.close();	 Catch:{ IOException -> 0x07e8 }
    L_0x07e7:
        throw r5;
    L_0x07e8:
        r90 = move-exception;
        goto L_0x07e7;
    L_0x07ea:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r113 = r130.readStrongBinder();
        r0 = r128;
        r1 = r113;
        r0.unstableProviderDied(r1);
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x0802:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r84 = r130.readStrongBinder();
        r117 = r130.readStrongBinder();
        r118 = r130.readInt();
        r121 = r130.readInt();
        r0 = r128;
        r1 = r84;
        r2 = r117;
        r3 = r118;
        r4 = r121;
        r0.requestAssistContextExtras(r1, r2, r3, r4);
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x082c:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r30 = r130.readStrongBinder();
        r5 = r130.readInt();
        r11 = 1;
        if (r5 != r11) goto L_0x084f;
    L_0x083f:
        r127 = 1;
    L_0x0841:
        r0 = r128;
        r1 = r30;
        r2 = r127;
        r0.scheduleTranslucentConversionComplete(r1, r2);
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x084f:
        r127 = 0;
        goto L_0x0841;
    L_0x0852:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r30 = r130.readStrongBinder();
        r104 = new android.app.ActivityOptions;
        r5 = r130.readBundle();
        r0 = r104;
        r0.<init>(r5);
        r0 = r128;
        r1 = r30;
        r2 = r104;
        r0.scheduleOnNewActivityOptions(r1, r2);
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x0877:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r126 = r130.readInt();
        r0 = r128;
        r1 = r126;
        r0.setProcessState(r1);
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x088f:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r5 = android.content.pm.ProviderInfo.CREATOR;
        r0 = r130;
        r112 = r5.createFromParcel(r0);
        r112 = (android.content.pm.ProviderInfo) r112;
        r0 = r128;
        r1 = r112;
        r0.scheduleInstallProvider(r1);
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x08ad:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r100 = r130.readByte();
        r5 = 1;
        r0 = r100;
        if (r0 != r5) goto L_0x08c9;
    L_0x08be:
        r5 = 1;
    L_0x08bf:
        r0 = r128;
        r0.updateTimePrefs(r5);
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x08c9:
        r5 = 0;
        goto L_0x08bf;
    L_0x08cb:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r30 = r130.readStrongBinder();
        r0 = r128;
        r1 = r30;
        r0.scheduleCancelVisibleBehind(r1);
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x08e3:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r30 = r130.readStrongBinder();
        r5 = r130.readInt();
        if (r5 <= 0) goto L_0x0905;
    L_0x08f5:
        r91 = 1;
    L_0x08f7:
        r0 = r128;
        r1 = r30;
        r2 = r91;
        r0.scheduleBackgroundVisibleBehindChanged(r1, r2);
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x0905:
        r91 = 0;
        goto L_0x08f7;
    L_0x0908:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r30 = r130.readStrongBinder();
        r0 = r128;
        r1 = r30;
        r0.scheduleEnterAnimationComplete(r1);
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x0920:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r95 = r130.createByteArray();
        r0 = r128;
        r1 = r95;
        r0.notifyCleartextNetwork(r1);
        r131.writeNoException();
        r5 = 1;
        return r5;
    L_0x0938:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r128.startBinderTracking();
        r5 = 1;
        return r5;
    L_0x0945:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r93 = r130.readFileDescriptor();
        if (r93 == 0) goto L_0x095f;
    L_0x0953:
        r5 = r93.getFileDescriptor();
        r0 = r128;
        r0.stopBinderTrackingAndDump(r5);
        r93.close();	 Catch:{ IOException -> 0x0961 }
    L_0x095f:
        r5 = 1;
        return r5;
    L_0x0961:
        r90 = move-exception;
        goto L_0x095f;
    L_0x0963:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r6 = r130.readStrongBinder();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x0980;
    L_0x0975:
        r97 = 1;
    L_0x0977:
        r0 = r128;
        r1 = r97;
        r0.scheduleMultiWindowModeChanged(r6, r1);
        r5 = 1;
        return r5;
    L_0x0980:
        r97 = 0;
        goto L_0x0977;
    L_0x0983:
        r5 = "android.app.IApplicationThread";
        r0 = r130;
        r0.enforceInterface(r5);
        r6 = r130.readStrongBinder();
        r5 = r130.readInt();
        if (r5 == 0) goto L_0x09a0;
    L_0x0995:
        r98 = 1;
    L_0x0997:
        r0 = r128;
        r1 = r98;
        r0.schedulePictureInPictureModeChanged(r6, r1);
        r5 = 1;
        return r5;
    L_0x09a0:
        r98 = 0;
        goto L_0x0997;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ApplicationThreadNative.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
    }

    public static IApplicationThread asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IApplicationThread in = (IApplicationThread) obj.queryLocalInterface(IApplicationThread.descriptor);
        if (in != null) {
            return in;
        }
        return new ApplicationThreadProxy(obj);
    }

    public ApplicationThreadNative() {
        attachInterface(this, IApplicationThread.descriptor);
    }

    public IBinder asBinder() {
        return this;
    }
}
