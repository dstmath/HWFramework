package com.android.server.ethernet;

import android.content.Context;
import android.net.IpConfiguration;
import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.StringNetworkSpecifier;
import android.net.ip.IpClient;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.ethernet.EthernetNetworkFactory;
import java.io.FileDescriptor;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class EthernetNetworkFactory extends NetworkFactory {
    static final boolean DBG = true;
    private static final int NETWORK_SCORE = 70;
    private static final String NETWORK_TYPE = "Ethernet";
    /* access modifiers changed from: private */
    public static final String TAG = EthernetNetworkFactory.class.getSimpleName();
    private final Context mContext;
    private final Handler mHandler;
    private final ConcurrentHashMap<String, NetworkInterfaceState> mTrackingInterfaces = new ConcurrentHashMap<>();

    private static class NetworkInterfaceState {
        private static String sTcpBufferSizes = null;
        /* access modifiers changed from: private */
        public final NetworkCapabilities mCapabilities;
        private final Context mContext;
        /* access modifiers changed from: private */
        public final Handler mHandler;
        private final String mHwAddress;
        /* access modifiers changed from: private */
        public IpClient mIpClient;
        private final IpClient.Callback mIpClientCallback = new IpClient.Callback() {
            public void onProvisioningSuccess(LinkProperties newLp) {
                NetworkInterfaceState.this.mHandler.post(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000b: INVOKE  (wrap: android.os.Handler
                      0x0002: INVOKE  (r0v1 android.os.Handler) = (wrap: com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState
                      0x0000: IGET  (r0v0 com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState) = (r2v0 'this' com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1 A[THIS]) com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.1.this$0 com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState) com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.access$200(com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState):android.os.Handler type: STATIC), (wrap: com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$9XedDO1NtZ_RFArLiXxHcePnujQ
                      0x0008: CONSTRUCTOR  (r1v0 com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$9XedDO1NtZ_RFArLiXxHcePnujQ) = (r2v0 'this' com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1 A[THIS]), (r3v0 'newLp' android.net.LinkProperties) com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$9XedDO1NtZ_RFArLiXxHcePnujQ.<init>(com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1, android.net.LinkProperties):void CONSTRUCTOR) android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.1.onProvisioningSuccess(android.net.LinkProperties):void, dex: ethernet-service_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:98)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:469)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	at jadx.core.codegen.ClassGen.addInsnBody(ClassGen.java:436)
                    	at jadx.core.codegen.ClassGen.addField(ClassGen.java:377)
                    	at jadx.core.codegen.ClassGen.addFields(ClassGen.java:347)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:224)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r1v0 com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$9XedDO1NtZ_RFArLiXxHcePnujQ) = (r2v0 'this' com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1 A[THIS]), (r3v0 'newLp' android.net.LinkProperties) com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$9XedDO1NtZ_RFArLiXxHcePnujQ.<init>(com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1, android.net.LinkProperties):void CONSTRUCTOR in method: com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.1.onProvisioningSuccess(android.net.LinkProperties):void, dex: ethernet-service_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 32 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$9XedDO1NtZ_RFArLiXxHcePnujQ, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 37 more
                    */
                /*
                    this = this;
                    com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState r0 = com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.this
                    android.os.Handler r0 = r0.mHandler
                    com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$9XedDO1NtZ_RFArLiXxHcePnujQ r1 = new com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$9XedDO1NtZ_RFArLiXxHcePnujQ
                    r1.<init>(r2, r3)
                    r0.post(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.AnonymousClass1.onProvisioningSuccess(android.net.LinkProperties):void");
            }

            public void onProvisioningFailure(LinkProperties newLp) {
                NetworkInterfaceState.this.mHandler.post(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000b: INVOKE  (wrap: android.os.Handler
                      0x0002: INVOKE  (r0v1 android.os.Handler) = (wrap: com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState
                      0x0000: IGET  (r0v0 com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState) = (r2v0 'this' com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1 A[THIS]) com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.1.this$0 com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState) com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.access$200(com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState):android.os.Handler type: STATIC), (wrap: com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$f_QeN95E84S9ECYxfdEhjw7SAFc
                      0x0008: CONSTRUCTOR  (r1v0 com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$f_QeN95E84S9ECYxfdEhjw7SAFc) = (r2v0 'this' com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1 A[THIS]), (r3v0 'newLp' android.net.LinkProperties) com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$f_QeN95E84S9ECYxfdEhjw7SAFc.<init>(com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1, android.net.LinkProperties):void CONSTRUCTOR) android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.1.onProvisioningFailure(android.net.LinkProperties):void, dex: ethernet-service_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:98)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:469)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	at jadx.core.codegen.ClassGen.addInsnBody(ClassGen.java:436)
                    	at jadx.core.codegen.ClassGen.addField(ClassGen.java:377)
                    	at jadx.core.codegen.ClassGen.addFields(ClassGen.java:347)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:224)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r1v0 com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$f_QeN95E84S9ECYxfdEhjw7SAFc) = (r2v0 'this' com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1 A[THIS]), (r3v0 'newLp' android.net.LinkProperties) com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$f_QeN95E84S9ECYxfdEhjw7SAFc.<init>(com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1, android.net.LinkProperties):void CONSTRUCTOR in method: com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.1.onProvisioningFailure(android.net.LinkProperties):void, dex: ethernet-service_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 32 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$f_QeN95E84S9ECYxfdEhjw7SAFc, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 37 more
                    */
                /*
                    this = this;
                    com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState r0 = com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.this
                    android.os.Handler r0 = r0.mHandler
                    com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$f_QeN95E84S9ECYxfdEhjw7SAFc r1 = new com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$f_QeN95E84S9ECYxfdEhjw7SAFc
                    r1.<init>(r2, r3)
                    r0.post(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.AnonymousClass1.onProvisioningFailure(android.net.LinkProperties):void");
            }

            public void onLinkPropertiesChange(LinkProperties newLp) {
                NetworkInterfaceState.this.mHandler.post(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000b: INVOKE  (wrap: android.os.Handler
                      0x0002: INVOKE  (r0v1 android.os.Handler) = (wrap: com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState
                      0x0000: IGET  (r0v0 com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState) = (r2v0 'this' com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1 A[THIS]) com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.1.this$0 com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState) com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.access$200(com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState):android.os.Handler type: STATIC), (wrap: com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$a0284YqWC7oRla-yedW8TwdmRO0
                      0x0008: CONSTRUCTOR  (r1v0 com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$a0284YqWC7oRla-yedW8TwdmRO0) = (r2v0 'this' com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1 A[THIS]), (r3v0 'newLp' android.net.LinkProperties) com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$a0284YqWC7oRla-yedW8TwdmRO0.<init>(com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1, android.net.LinkProperties):void CONSTRUCTOR) android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.1.onLinkPropertiesChange(android.net.LinkProperties):void, dex: ethernet-service_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:98)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:469)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	at jadx.core.codegen.ClassGen.addInsnBody(ClassGen.java:436)
                    	at jadx.core.codegen.ClassGen.addField(ClassGen.java:377)
                    	at jadx.core.codegen.ClassGen.addFields(ClassGen.java:347)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:224)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r1v0 com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$a0284YqWC7oRla-yedW8TwdmRO0) = (r2v0 'this' com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1 A[THIS]), (r3v0 'newLp' android.net.LinkProperties) com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$a0284YqWC7oRla-yedW8TwdmRO0.<init>(com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState$1, android.net.LinkProperties):void CONSTRUCTOR in method: com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.1.onLinkPropertiesChange(android.net.LinkProperties):void, dex: ethernet-service_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 32 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$a0284YqWC7oRla-yedW8TwdmRO0, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 37 more
                    */
                /*
                    this = this;
                    com.android.server.ethernet.EthernetNetworkFactory$NetworkInterfaceState r0 = com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.this
                    android.os.Handler r0 = r0.mHandler
                    com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$a0284YqWC7oRla-yedW8TwdmRO0 r1 = new com.android.server.ethernet.-$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$1$a0284YqWC7oRla-yedW8TwdmRO0
                    r1.<init>(r2, r3)
                    r0.post(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.AnonymousClass1.onLinkPropertiesChange(android.net.LinkProperties):void");
            }
        };
        private IpConfiguration mIpConfig;
        private LinkProperties mLinkProperties = new LinkProperties();
        private boolean mLinkUp;
        /* access modifiers changed from: private */
        public NetworkAgent mNetworkAgent;
        private final NetworkInfo mNetworkInfo;
        /* access modifiers changed from: package-private */
        public final String name;
        long refCount = 0;

        NetworkInterfaceState(String ifaceName, String hwAddress, Handler handler, Context context, NetworkCapabilities capabilities) {
            this.name = ifaceName;
            this.mCapabilities = capabilities;
            this.mHandler = handler;
            this.mContext = context;
            this.mHwAddress = hwAddress;
            this.mNetworkInfo = new NetworkInfo(9, 0, EthernetNetworkFactory.NETWORK_TYPE, "");
            this.mNetworkInfo.setExtraInfo(this.mHwAddress);
            this.mNetworkInfo.setIsAvailable(EthernetNetworkFactory.DBG);
        }

        /* access modifiers changed from: package-private */
        public void setIpConfig(IpConfiguration ipConfig) {
            this.mIpConfig = ipConfig;
        }

        /* access modifiers changed from: package-private */
        public boolean statisified(NetworkCapabilities requestedCapabilities) {
            return requestedCapabilities.satisfiedByNetworkCapabilities(this.mCapabilities);
        }

        /* access modifiers changed from: package-private */
        public boolean isRestricted() {
            return this.mCapabilities.hasCapability(13);
        }

        /* access modifiers changed from: private */
        public void start() {
            if (this.mIpClient != null) {
                Log.d(EthernetNetworkFactory.TAG, "IpClient already started");
                return;
            }
            Log.d(EthernetNetworkFactory.TAG, String.format("starting IpClient(%s): mNetworkInfo=%s", new Object[]{this.name, this.mNetworkInfo}));
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.OBTAINING_IPADDR, null, this.mHwAddress);
            this.mIpClient = new IpClient(this.mContext, this.name, this.mIpClientCallback);
            if (sTcpBufferSizes == null) {
                sTcpBufferSizes = this.mContext.getResources().getString(17039804);
            }
            provisionIpClient(this.mIpClient, this.mIpConfig, sTcpBufferSizes);
        }

        /* access modifiers changed from: package-private */
        public void onIpLayerStarted(LinkProperties linkProperties) {
            if (this.mNetworkAgent != null) {
                Log.e(EthernetNetworkFactory.TAG, "Already have a NetworkAgent - aborting new request");
                stop();
                return;
            }
            this.mLinkProperties = linkProperties;
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, this.mHwAddress);
            this.mNetworkInfo.setIsAvailable(EthernetNetworkFactory.DBG);
            AnonymousClass2 r2 = new NetworkAgent(this, this.mHandler.getLooper(), this.mContext, EthernetNetworkFactory.NETWORK_TYPE, this.mNetworkInfo, this.mCapabilities, this.mLinkProperties, EthernetNetworkFactory.NETWORK_SCORE) {
                final /* synthetic */ NetworkInterfaceState this$0;

                {
                    this.this$0 = this$0;
                }

                public void unwanted() {
                    if (this == this.this$0.mNetworkAgent) {
                        this.this$0.stop();
                    } else if (this.this$0.mNetworkAgent != null) {
                        Log.d(EthernetNetworkFactory.TAG, "Ignoring unwanted as we have a more modern instance");
                    }
                }
            };
            this.mNetworkAgent = r2;
        }

        /* access modifiers changed from: package-private */
        public void onIpLayerStopped(LinkProperties linkProperties) {
            stop();
            start();
        }

        /* access modifiers changed from: package-private */
        public void updateLinkProperties(LinkProperties linkProperties) {
            this.mLinkProperties = linkProperties;
            if (this.mNetworkAgent != null) {
                this.mNetworkAgent.sendLinkProperties(linkProperties);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean updateLinkState(boolean up) {
            if (this.mLinkUp == up) {
                return false;
            }
            this.mLinkUp = up;
            stop();
            if (up) {
                start();
            }
            return EthernetNetworkFactory.DBG;
        }

        /* access modifiers changed from: package-private */
        public void stop() {
            if (this.mIpClient != null) {
                this.mIpClient.shutdown();
                this.mIpClient.awaitShutdown();
                this.mIpClient = null;
            }
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, this.mHwAddress);
            if (this.mNetworkAgent != null) {
                updateAgent();
                this.mNetworkAgent = null;
            }
            clear();
        }

        private void updateAgent() {
            if (this.mNetworkAgent != null) {
                String access$300 = EthernetNetworkFactory.TAG;
                Log.i(access$300, "Updating mNetworkAgent with: " + this.mCapabilities + ", " + this.mNetworkInfo + ", " + this.mLinkProperties);
                this.mNetworkAgent.sendNetworkCapabilities(this.mCapabilities);
                this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
                this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
                this.mNetworkAgent.sendNetworkScore(this.mLinkUp ? EthernetNetworkFactory.NETWORK_SCORE : 0);
            }
        }

        private void clear() {
            this.mLinkProperties.clear();
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.IDLE, null, null);
            this.mNetworkInfo.setIsAvailable(false);
        }

        private static void provisionIpClient(IpClient ipClient, IpConfiguration config, String tcpBufferSizes) {
            IpClient.ProvisioningConfiguration provisioningConfiguration;
            if (config.getProxySettings() == IpConfiguration.ProxySettings.STATIC || config.getProxySettings() == IpConfiguration.ProxySettings.PAC) {
                ipClient.setHttpProxy(config.getHttpProxy());
            }
            if (!TextUtils.isEmpty(tcpBufferSizes)) {
                ipClient.setTcpBufferSizes(tcpBufferSizes);
            }
            if (config.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
                provisioningConfiguration = IpClient.buildProvisioningConfiguration().withStaticConfiguration(config.getStaticIpConfiguration()).build();
            } else {
                provisioningConfiguration = IpClient.buildProvisioningConfiguration().withProvisioningTimeoutMs(0).build();
            }
            ipClient.startProvisioning(provisioningConfiguration);
        }

        public String toString() {
            return getClass().getSimpleName() + "{ iface: " + this.name + ", up: " + this.mLinkUp + ", hwAddress: " + this.mHwAddress + ", networkInfo: " + this.mNetworkInfo + ", networkAgent: " + this.mNetworkAgent + ", ipClient: " + this.mIpClient + ",linkProperties: " + this.mLinkProperties + "}";
        }
    }

    public EthernetNetworkFactory(Handler handler, Context context, NetworkCapabilities filter) {
        super(handler.getLooper(), context, NETWORK_TYPE, filter);
        this.mHandler = handler;
        this.mContext = context;
        setScoreFilter(NETWORK_SCORE);
    }

    public boolean acceptRequest(NetworkRequest request, int score) {
        String str = TAG;
        Log.d(str, "acceptRequest, request: " + request + ", score: " + score);
        if (networkForRequest(request) != null) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        NetworkInterfaceState network = networkForRequest(networkRequest);
        if (network == null) {
            String str = TAG;
            Log.e(str, "needNetworkFor, failed to get a network for " + networkRequest);
            return;
        }
        long j = network.refCount + 1;
        network.refCount = j;
        if (j == 1) {
            network.start();
        }
    }

    /* access modifiers changed from: protected */
    public void releaseNetworkFor(NetworkRequest networkRequest) {
        NetworkInterfaceState network = networkForRequest(networkRequest);
        if (network == null) {
            String str = TAG;
            Log.e(str, "needNetworkFor, failed to get a network for " + networkRequest);
            return;
        }
        long j = network.refCount - 1;
        network.refCount = j;
        if (j == 1) {
            network.stop();
        }
    }

    /* access modifiers changed from: package-private */
    public String[] getAvailableInterfaces(boolean includeRestricted) {
        return (String[]) this.mTrackingInterfaces.values().stream().filter(new Predicate(includeRestricted) {
            private final /* synthetic */ boolean f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return EthernetNetworkFactory.lambda$getAvailableInterfaces$0(this.f$0, (EthernetNetworkFactory.NetworkInterfaceState) obj);
            }
        }).sorted($$Lambda$EthernetNetworkFactory$EmftAjIay22czoGb8k_mrRGmnzg.INSTANCE).map($$Lambda$EthernetNetworkFactory$KXwxO15KBNVyyYSUjDFlm1vQ0.INSTANCE).toArray($$Lambda$EthernetNetworkFactory$TVQUJVMLGgbguTOK63vgn0fV1JA.INSTANCE);
    }

    static /* synthetic */ boolean lambda$getAvailableInterfaces$0(boolean includeRestricted, NetworkInterfaceState iface) {
        if (!iface.isRestricted() || includeRestricted) {
            return DBG;
        }
        return false;
    }

    static /* synthetic */ int lambda$getAvailableInterfaces$1(NetworkInterfaceState iface1, NetworkInterfaceState iface2) {
        int r = Boolean.compare(iface1.isRestricted(), iface2.isRestricted());
        return r == 0 ? iface1.name.compareTo(iface2.name) : r;
    }

    static /* synthetic */ String[] lambda$getAvailableInterfaces$3(int x$0) {
        return new String[x$0];
    }

    /* access modifiers changed from: package-private */
    public void addInterface(String ifaceName, String hwAddress, NetworkCapabilities capabilities, IpConfiguration ipConfiguration) {
        if (this.mTrackingInterfaces.containsKey(ifaceName)) {
            String str = TAG;
            Log.e(str, "Interface with name " + ifaceName + " already exists.");
            return;
        }
        String str2 = TAG;
        Log.d(str2, "addInterface, iface: " + ifaceName + ", capabilities: " + capabilities);
        NetworkInterfaceState networkInterfaceState = new NetworkInterfaceState(ifaceName, hwAddress, this.mHandler, this.mContext, capabilities);
        networkInterfaceState.setIpConfig(ipConfiguration);
        this.mTrackingInterfaces.put(ifaceName, networkInterfaceState);
        updateCapabilityFilter();
    }

    private void updateCapabilityFilter() {
        NetworkCapabilities capabilitiesFilter = new NetworkCapabilities();
        capabilitiesFilter.clearAll();
        for (NetworkInterfaceState iface : this.mTrackingInterfaces.values()) {
            capabilitiesFilter.combineCapabilities(iface.mCapabilities);
        }
        String str = TAG;
        Log.d(str, "updateCapabilityFilter: " + capabilitiesFilter);
        setCapabilityFilter(capabilitiesFilter);
    }

    /* access modifiers changed from: package-private */
    public void removeInterface(String interfaceName) {
        NetworkInterfaceState iface = this.mTrackingInterfaces.remove(interfaceName);
        if (iface != null) {
            iface.stop();
        }
        updateCapabilityFilter();
    }

    /* access modifiers changed from: package-private */
    public boolean updateInterfaceLinkState(String ifaceName, boolean up) {
        if (!this.mTrackingInterfaces.containsKey(ifaceName)) {
            return false;
        }
        String str = TAG;
        Log.d(str, "updateInterfaceLinkState, iface: " + ifaceName + ", up: " + up);
        return this.mTrackingInterfaces.get(ifaceName).updateLinkState(up);
    }

    /* access modifiers changed from: package-private */
    public boolean hasInterface(String interfacName) {
        return this.mTrackingInterfaces.containsKey(interfacName);
    }

    /* access modifiers changed from: package-private */
    public void updateIpConfiguration(String iface, IpConfiguration ipConfiguration) {
        NetworkInterfaceState network = this.mTrackingInterfaces.get(iface);
        if (network != null) {
            network.setIpConfig(ipConfiguration);
        }
    }

    private NetworkInterfaceState networkForRequest(NetworkRequest request) {
        String requestedIface = null;
        StringNetworkSpecifier networkSpecifier = request.networkCapabilities.getNetworkSpecifier();
        if (networkSpecifier instanceof StringNetworkSpecifier) {
            requestedIface = networkSpecifier.specifier;
        }
        NetworkInterfaceState network = null;
        if (TextUtils.isEmpty(requestedIface)) {
            Iterator<NetworkInterfaceState> it = this.mTrackingInterfaces.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                NetworkInterfaceState n = it.next();
                if (n.statisified(request.networkCapabilities)) {
                    network = n;
                    break;
                }
            }
        } else {
            NetworkInterfaceState n2 = this.mTrackingInterfaces.get(requestedIface);
            if (n2 != null && n2.statisified(request.networkCapabilities)) {
                network = n2;
            }
        }
        String str = TAG;
        Log.i(str, "networkForRequest, request: " + request + ", network: " + network);
        return network;
    }

    /* access modifiers changed from: package-private */
    public void dump(FileDescriptor fd, IndentingPrintWriter pw, String[] args) {
        EthernetNetworkFactory.super.dump(fd, pw, args);
        pw.println(getClass().getSimpleName());
        pw.println("Tracking interfaces:");
        pw.increaseIndent();
        for (String iface : this.mTrackingInterfaces.keySet()) {
            NetworkInterfaceState ifaceState = this.mTrackingInterfaces.get(iface);
            pw.println(iface + ":" + ifaceState);
            pw.increaseIndent();
            IpClient ipClient = ifaceState.mIpClient;
            if (ipClient != null) {
                ipClient.dump(fd, pw, args);
            } else {
                pw.println("IpClient is null");
            }
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }
}
