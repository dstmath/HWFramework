package com.huawei.android.os;

public class AdCleanerManagerEx {
    private static final int CODE_AD_DEBUG = 1019;
    private static final int CODE_CLEAN_AD_STRATEGY = 1018;
    private static final int CODE_SET_AD_STRATEGY = 1017;
    private static final String DESCRIPTOR_ADCLEANER_MANAGER_Ex = "android.os.AdCleanerManagerEx";
    private static final String TAG = "AdCleanerManagerEx";

    public static int cleanAdFilterRules(java.util.List<java.lang.String> r9, boolean r10) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:30:? in {5, 6, 12, 23, 24, 26, 27, 28, 29, 31} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
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
        r3 = 0;
        r0 = android.os.Parcel.obtain();
        r1 = android.os.Parcel.obtain();
        r6 = "network_management";
        r2 = android.os.ServiceManager.getService(r6);
        if (r2 == 0) goto L_0x0041;
    L_0x0012:
        r6 = "android.os.AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r0.writeInterfaceToken(r6);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r6 = "AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r7 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r7.<init>();	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r8 = "CleanAdFilterRules needRest: ";	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r7 = r7.append(r8);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r7 = r7.append(r10);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r7 = r7.toString();	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        android.util.Log.d(r6, r7);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        if (r10 == 0) goto L_0x0048;	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
    L_0x0034:
        r6 = 1;	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r0.writeInt(r6);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
    L_0x0038:
        r6 = 1018; // 0x3fa float:1.427E-42 double:5.03E-321;	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r7 = 0;	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r2.transact(r6, r0, r1, r7);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r1.readException();	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
    L_0x0041:
        r1.recycle();
        r0.recycle();
    L_0x0047:
        return r3;
    L_0x0048:
        if (r9 != 0) goto L_0x0064;
    L_0x004a:
        r6 = -1;
        r0.writeInt(r6);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        goto L_0x0038;
    L_0x004f:
        r5 = move-exception;
        r6 = "AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r7 = "------- err: CleanAdFilterRules() RemoteException ! ";	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        android.util.Log.d(r6, r7);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r5.printStackTrace();	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r3 = 1;
        r1.recycle();
        r0.recycle();
        goto L_0x0047;
    L_0x0064:
        r6 = 0;
        r0.writeInt(r6);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r0.writeStringList(r9);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r6 = "AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r7 = "CleanAdFilterRules adAppList: ";	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        android.util.Log.d(r6, r7);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r4 = 0;	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
    L_0x0075:
        r6 = r9.size();	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        if (r4 >= r6) goto L_0x0038;	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
    L_0x007b:
        r7 = "AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r6 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r6.<init>();	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r6 = r6.append(r4);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r8 = " = ";	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r8 = r6.append(r8);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r6 = r9.get(r4);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r6 = (java.lang.String) r6;	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r6 = r8.append(r6);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r6 = r6.toString();	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        android.util.Log.d(r7, r6);	 Catch:{ RemoteException -> 0x004f, all -> 0x00a2 }
        r4 = r4 + 1;
        goto L_0x0075;
    L_0x00a2:
        r6 = move-exception;
        r1.recycle();
        r0.recycle();
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.os.AdCleanerManagerEx.cleanAdFilterRules(java.util.List, boolean):int");
    }

    public static int printRuleMaps() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:14:? in {3, 9, 11, 12, 13, 15} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
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
        r3 = 0;
        r0 = android.os.Parcel.obtain();
        r1 = android.os.Parcel.obtain();
        r5 = "network_management";
        r2 = android.os.ServiceManager.getService(r5);
        if (r2 == 0) goto L_0x0025;
    L_0x0012:
        r5 = "android.os.AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x002c, all -> 0x0038 }
        r0.writeInterfaceToken(r5);	 Catch:{ RemoteException -> 0x002c, all -> 0x0038 }
        r5 = 0;	 Catch:{ RemoteException -> 0x002c, all -> 0x0038 }
        r0.writeInt(r5);	 Catch:{ RemoteException -> 0x002c, all -> 0x0038 }
        r5 = 1019; // 0x3fb float:1.428E-42 double:5.035E-321;	 Catch:{ RemoteException -> 0x002c, all -> 0x0038 }
        r6 = 0;	 Catch:{ RemoteException -> 0x002c, all -> 0x0038 }
        r2.transact(r5, r0, r1, r6);	 Catch:{ RemoteException -> 0x002c, all -> 0x0038 }
        r1.readException();	 Catch:{ RemoteException -> 0x002c, all -> 0x0038 }
    L_0x0025:
        r1.recycle();
        r0.recycle();
    L_0x002b:
        return r3;
    L_0x002c:
        r4 = move-exception;
        r4.printStackTrace();	 Catch:{ RemoteException -> 0x002c, all -> 0x0038 }
        r3 = 1;
        r1.recycle();
        r0.recycle();
        goto L_0x002b;
    L_0x0038:
        r5 = move-exception;
        r1.recycle();
        r0.recycle();
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.os.AdCleanerManagerEx.printRuleMaps():int");
    }

    public static int setAdFilterRules(java.util.Map<java.lang.String, java.util.List<java.lang.String>> r17, java.util.Map<java.lang.String, java.util.List<java.lang.String>> r18, boolean r19) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:48:? in {5, 8, 10, 11, 13, 19, 26, 31, 32, 34, 38, 40, 41, 43, 44, 45, 46, 47, 49} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
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
        r4 = 0;
        r1 = android.os.Parcel.obtain();
        r2 = android.os.Parcel.obtain();
        r14 = "network_management";
        r3 = android.os.ServiceManager.getService(r14);
        if (r3 == 0) goto L_0x004f;
    L_0x0012:
        r14 = "android.os.AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r1.writeInterfaceToken(r14);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        if (r19 == 0) goto L_0x0056;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x001a:
        r14 = 1;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x001b:
        r1.writeInt(r14);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r14 = "AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15.<init>();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r16 = "setAdFilterRules needRest: ";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r16);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r0 = r19;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r0);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.toString();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        android.util.Log.d(r14, r15);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        if (r17 != 0) goto L_0x0058;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x003c:
        r14 = -1;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r1.writeInt(r14);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x0040:
        if (r18 != 0) goto L_0x00ea;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x0042:
        r14 = -1;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r1.writeInt(r14);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x0046:
        r14 = 1017; // 0x3f9 float:1.425E-42 double:5.025E-321;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = 0;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r3.transact(r14, r1, r2, r15);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r2.readException();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x004f:
        r2.recycle();
        r1.recycle();
    L_0x0055:
        return r4;
    L_0x0056:
        r14 = 0;
        goto L_0x001b;
    L_0x0058:
        r12 = r17.size();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r5 = 0;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r1.writeInt(r12);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r14 = "AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15.<init>();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r16 = "adViewMap size = ";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r16);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r12);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.toString();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        android.util.Log.d(r14, r15);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r10 = r17.keySet();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r9 = new java.util.ArrayList;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r9.<init>();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r8 = r10.iterator();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x0087:
        r14 = r8.hasNext();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        if (r14 == 0) goto L_0x00ac;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x008d:
        r7 = r8.next();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r7 = (java.lang.String) r7;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r9.add(r7);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        goto L_0x0087;
    L_0x0097:
        r11 = move-exception;
        r14 = "AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = "------- err: setAdFilterRules() RemoteException ! ";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        android.util.Log.d(r14, r15);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r11.printStackTrace();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r4 = 1;
        r2.recycle();
        r1.recycle();
        goto L_0x0055;
    L_0x00ac:
        if (r5 >= r12) goto L_0x0040;
    L_0x00ae:
        r6 = r9.get(r5);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r6 = (java.lang.String) r6;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r0 = r17;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r13 = r0.get(r6);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r13 = (java.util.List) r13;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r1.writeString(r6);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r1.writeStringList(r13);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r14 = "AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15.<init>();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r16 = "i=";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r16);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r5);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r16 = ", send adViewMap key: ";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r16);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r6);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.toString();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        android.util.Log.d(r14, r15);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r5 = r5 + 1;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        goto L_0x00ac;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x00ea:
        r12 = r18.size();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r5 = 0;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r1.writeInt(r12);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r14 = "AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15.<init>();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r16 = " adIdMap size = ";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r16);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r12);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.toString();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        android.util.Log.d(r14, r15);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r10 = r18.keySet();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r9 = new java.util.ArrayList;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r9.<init>();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r8 = r10.iterator();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x0119:
        r14 = r8.hasNext();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        if (r14 == 0) goto L_0x0131;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
    L_0x011f:
        r7 = r8.next();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r7 = (java.lang.String) r7;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r9.add(r7);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        goto L_0x0119;
    L_0x0129:
        r14 = move-exception;
        r2.recycle();
        r1.recycle();
        throw r14;
    L_0x0131:
        if (r5 >= r12) goto L_0x0046;
    L_0x0133:
        r6 = r9.get(r5);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r6 = (java.lang.String) r6;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r0 = r18;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r13 = r0.get(r6);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r13 = (java.util.List) r13;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r1.writeString(r6);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r1.writeStringList(r13);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r14 = "AdCleanerManagerEx";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15.<init>();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r16 = "i=";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r16);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r5);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r16 = ", send adIdMap key: ";	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r16);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.append(r6);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r15 = r15.toString();	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        android.util.Log.d(r14, r15);	 Catch:{ RemoteException -> 0x0097, all -> 0x0129 }
        r5 = r5 + 1;
        goto L_0x0131;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.os.AdCleanerManagerEx.setAdFilterRules(java.util.Map, java.util.Map, boolean):int");
    }
}
