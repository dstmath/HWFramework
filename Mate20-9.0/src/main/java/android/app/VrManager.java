package android.app;

import android.annotation.SystemApi;
import android.content.ComponentName;
import android.os.Handler;
import android.os.RemoteException;
import android.service.vr.IPersistentVrStateCallbacks;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.ArrayMap;
import java.util.Map;

@SystemApi
public class VrManager {
    private Map<VrStateCallback, CallbackEntry> mCallbackMap = new ArrayMap();
    private final IVrManager mService;

    private static class CallbackEntry {
        final VrStateCallback mCallback;
        final Handler mHandler;
        final IPersistentVrStateCallbacks mPersistentStateCallback = new IPersistentVrStateCallbacks.Stub() {
            public void onPersistentVrStateChanged(boolean enabled) {
                CallbackEntry.this.mHandler.post(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0009: INVOKE  (wrap: android.os.Handler
                      0x0002: IGET  (r0v1 android.os.Handler) = (wrap: android.app.VrManager$CallbackEntry
                      0x0000: IGET  (r0v0 android.app.VrManager$CallbackEntry) = (r2v0 'this' android.app.VrManager$CallbackEntry$2 A[THIS]) android.app.VrManager.CallbackEntry.2.this$0 android.app.VrManager$CallbackEntry) android.app.VrManager.CallbackEntry.mHandler android.os.Handler), (wrap: android.app.-$$Lambda$VrManager$CallbackEntry$2$KvHLIXm3-7igcOqTEl46YdjhHMk
                      0x0006: CONSTRUCTOR  (r1v0 android.app.-$$Lambda$VrManager$CallbackEntry$2$KvHLIXm3-7igcOqTEl46YdjhHMk) = (r2v0 'this' android.app.VrManager$CallbackEntry$2 A[THIS]), (r3v0 'enabled' boolean) android.app.-$$Lambda$VrManager$CallbackEntry$2$KvHLIXm3-7igcOqTEl46YdjhHMk.<init>(android.app.VrManager$CallbackEntry$2, boolean):void CONSTRUCTOR) android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: android.app.VrManager.CallbackEntry.2.onPersistentVrStateChanged(boolean):void, dex: boot-framework_classes.dex
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
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0006: CONSTRUCTOR  (r1v0 android.app.-$$Lambda$VrManager$CallbackEntry$2$KvHLIXm3-7igcOqTEl46YdjhHMk) = (r2v0 'this' android.app.VrManager$CallbackEntry$2 A[THIS]), (r3v0 'enabled' boolean) android.app.-$$Lambda$VrManager$CallbackEntry$2$KvHLIXm3-7igcOqTEl46YdjhHMk.<init>(android.app.VrManager$CallbackEntry$2, boolean):void CONSTRUCTOR in method: android.app.VrManager.CallbackEntry.2.onPersistentVrStateChanged(boolean):void, dex: boot-framework_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 32 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: android.app.-$$Lambda$VrManager$CallbackEntry$2$KvHLIXm3-7igcOqTEl46YdjhHMk, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 37 more
                    */
                /*
                    this = this;
                    android.app.VrManager$CallbackEntry r0 = android.app.VrManager.CallbackEntry.this
                    android.os.Handler r0 = r0.mHandler
                    android.app.-$$Lambda$VrManager$CallbackEntry$2$KvHLIXm3-7igcOqTEl46YdjhHMk r1 = new android.app.-$$Lambda$VrManager$CallbackEntry$2$KvHLIXm3-7igcOqTEl46YdjhHMk
                    r1.<init>(r2, r3)
                    r0.post(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: android.app.VrManager.CallbackEntry.AnonymousClass2.onPersistentVrStateChanged(boolean):void");
            }
        };
        final IVrStateCallbacks mStateCallback = new IVrStateCallbacks.Stub() {
            public void onVrStateChanged(boolean enabled) {
                CallbackEntry.this.mHandler.post(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0009: INVOKE  (wrap: android.os.Handler
                      0x0002: IGET  (r0v1 android.os.Handler) = (wrap: android.app.VrManager$CallbackEntry
                      0x0000: IGET  (r0v0 android.app.VrManager$CallbackEntry) = (r2v0 'this' android.app.VrManager$CallbackEntry$1 A[THIS]) android.app.VrManager.CallbackEntry.1.this$0 android.app.VrManager$CallbackEntry) android.app.VrManager.CallbackEntry.mHandler android.os.Handler), (wrap: android.app.-$$Lambda$VrManager$CallbackEntry$1$rgUBVVG1QhelpvAp8W3UQHDHJdU
                      0x0006: CONSTRUCTOR  (r1v0 android.app.-$$Lambda$VrManager$CallbackEntry$1$rgUBVVG1QhelpvAp8W3UQHDHJdU) = (r2v0 'this' android.app.VrManager$CallbackEntry$1 A[THIS]), (r3v0 'enabled' boolean) android.app.-$$Lambda$VrManager$CallbackEntry$1$rgUBVVG1QhelpvAp8W3UQHDHJdU.<init>(android.app.VrManager$CallbackEntry$1, boolean):void CONSTRUCTOR) android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: android.app.VrManager.CallbackEntry.1.onVrStateChanged(boolean):void, dex: boot-framework_classes.dex
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
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0006: CONSTRUCTOR  (r1v0 android.app.-$$Lambda$VrManager$CallbackEntry$1$rgUBVVG1QhelpvAp8W3UQHDHJdU) = (r2v0 'this' android.app.VrManager$CallbackEntry$1 A[THIS]), (r3v0 'enabled' boolean) android.app.-$$Lambda$VrManager$CallbackEntry$1$rgUBVVG1QhelpvAp8W3UQHDHJdU.<init>(android.app.VrManager$CallbackEntry$1, boolean):void CONSTRUCTOR in method: android.app.VrManager.CallbackEntry.1.onVrStateChanged(boolean):void, dex: boot-framework_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 32 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: android.app.-$$Lambda$VrManager$CallbackEntry$1$rgUBVVG1QhelpvAp8W3UQHDHJdU, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 37 more
                    */
                /*
                    this = this;
                    android.app.VrManager$CallbackEntry r0 = android.app.VrManager.CallbackEntry.this
                    android.os.Handler r0 = r0.mHandler
                    android.app.-$$Lambda$VrManager$CallbackEntry$1$rgUBVVG1QhelpvAp8W3UQHDHJdU r1 = new android.app.-$$Lambda$VrManager$CallbackEntry$1$rgUBVVG1QhelpvAp8W3UQHDHJdU
                    r1.<init>(r2, r3)
                    r0.post(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: android.app.VrManager.CallbackEntry.AnonymousClass1.onVrStateChanged(boolean):void");
            }
        };

        CallbackEntry(VrStateCallback callback, Handler handler) {
            this.mCallback = callback;
            this.mHandler = handler;
        }
    }

    public VrManager(IVrManager service) {
        this.mService = service;
    }

    public void registerVrStateCallback(VrStateCallback callback, Handler handler) {
        if (callback != null && !this.mCallbackMap.containsKey(callback)) {
            CallbackEntry entry = new CallbackEntry(callback, handler);
            this.mCallbackMap.put(callback, entry);
            try {
                this.mService.registerListener(entry.mStateCallback);
                this.mService.registerPersistentVrStateListener(entry.mPersistentStateCallback);
            } catch (RemoteException e) {
                try {
                    unregisterVrStateCallback(callback);
                } catch (Exception e2) {
                    e.rethrowFromSystemServer();
                }
            }
        }
    }

    public void unregisterVrStateCallback(VrStateCallback callback) {
        CallbackEntry entry = this.mCallbackMap.remove(callback);
        if (entry != null) {
            try {
                this.mService.unregisterListener(entry.mStateCallback);
            } catch (RemoteException e) {
            }
            try {
                this.mService.unregisterPersistentVrStateListener(entry.mPersistentStateCallback);
            } catch (RemoteException e2) {
            }
        }
    }

    public boolean getVrModeEnabled() {
        try {
            return this.mService.getVrModeState();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return false;
        }
    }

    public boolean getPersistentVrModeEnabled() {
        try {
            return this.mService.getPersistentVrModeEnabled();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return false;
        }
    }

    public void setPersistentVrModeEnabled(boolean enabled) {
        try {
            this.mService.setPersistentVrModeEnabled(enabled);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void setVr2dDisplayProperties(Vr2dDisplayProperties vr2dDisplayProp) {
        try {
            this.mService.setVr2dDisplayProperties(vr2dDisplayProp);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void setAndBindVrCompositor(ComponentName componentName) {
        try {
            this.mService.setAndBindCompositor(componentName == null ? null : componentName.flattenToString());
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void setStandbyEnabled(boolean standby) {
        try {
            this.mService.setStandbyEnabled(standby);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void setVrInputMethod(ComponentName componentName) {
        try {
            this.mService.setVrInputMethod(componentName);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }
}
