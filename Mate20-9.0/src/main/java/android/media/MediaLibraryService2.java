package android.media;

import android.app.PendingIntent;
import android.media.MediaSession2;
import android.media.update.ApiLoader;
import android.media.update.MediaLibraryService2Provider;
import android.media.update.MediaSessionService2Provider;
import android.os.Bundle;
import java.util.List;
import java.util.concurrent.Executor;

public abstract class MediaLibraryService2 extends MediaSessionService2 {
    public static final String SERVICE_INTERFACE = "android.media.MediaLibraryService2";

    public static final class LibraryRoot {
        public static final String EXTRA_OFFLINE = "android.media.extra.OFFLINE";
        public static final String EXTRA_RECENT = "android.media.extra.RECENT";
        public static final String EXTRA_SUGGESTED = "android.media.extra.SUGGESTED";
        private final MediaLibraryService2Provider.LibraryRootProvider mProvider;

        public LibraryRoot(String rootId, Bundle extras) {
            this.mProvider = ApiLoader.getProvider().createMediaLibraryService2LibraryRoot(this, rootId, extras);
        }

        public String getRootId() {
            return this.mProvider.getRootId_impl();
        }

        public Bundle getExtras() {
            return this.mProvider.getExtras_impl();
        }
    }

    public static final class MediaLibrarySession extends MediaSession2 {
        private final MediaLibraryService2Provider.MediaLibrarySessionProvider mProvider;

        public static final class Builder extends MediaSession2.BuilderBase<MediaLibrarySession, Builder, MediaLibrarySessionCallback> {
            public Builder(MediaLibraryService2 service, Executor callbackExecutor, MediaLibrarySessionCallback callback) {
                super(
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0005: CONSTRUCTOR  (wrap: android.media.-$$Lambda$MediaLibraryService2$MediaLibrarySession$Builder$KbvKQ6JiEvVRMpYadxywG_GUsco
                      0x0002: CONSTRUCTOR  (r0v0 android.media.-$$Lambda$MediaLibraryService2$MediaLibrarySession$Builder$KbvKQ6JiEvVRMpYadxywG_GUsco) = (r2v0 'service' android.media.MediaLibraryService2), (r3v0 'callbackExecutor' java.util.concurrent.Executor), (r4v0 'callback' android.media.MediaLibraryService2$MediaLibrarySession$MediaLibrarySessionCallback) android.media.-$$Lambda$MediaLibraryService2$MediaLibrarySession$Builder$KbvKQ6JiEvVRMpYadxywG_GUsco.<init>(android.media.MediaLibraryService2, java.util.concurrent.Executor, android.media.MediaLibraryService2$MediaLibrarySession$MediaLibrarySessionCallback):void CONSTRUCTOR) android.media.MediaSession2.BuilderBase.<init>(android.media.update.ProviderCreator):void SUPER in method: android.media.MediaLibraryService2.MediaLibrarySession.Builder.<init>(android.media.MediaLibraryService2, java.util.concurrent.Executor, android.media.MediaLibraryService2$MediaLibrarySession$MediaLibrarySessionCallback):void, dex: boot-framework_classes.dex
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
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
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
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0002: CONSTRUCTOR  (r0v0 android.media.-$$Lambda$MediaLibraryService2$MediaLibrarySession$Builder$KbvKQ6JiEvVRMpYadxywG_GUsco) = (r2v0 'service' android.media.MediaLibraryService2), (r3v0 'callbackExecutor' java.util.concurrent.Executor), (r4v0 'callback' android.media.MediaLibraryService2$MediaLibrarySession$MediaLibrarySessionCallback) android.media.-$$Lambda$MediaLibraryService2$MediaLibrarySession$Builder$KbvKQ6JiEvVRMpYadxywG_GUsco.<init>(android.media.MediaLibraryService2, java.util.concurrent.Executor, android.media.MediaLibraryService2$MediaLibrarySession$MediaLibrarySessionCallback):void CONSTRUCTOR in method: android.media.MediaLibraryService2.MediaLibrarySession.Builder.<init>(android.media.MediaLibraryService2, java.util.concurrent.Executor, android.media.MediaLibraryService2$MediaLibrarySession$MediaLibrarySessionCallback):void, dex: boot-framework_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:629)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 23 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: android.media.-$$Lambda$MediaLibraryService2$MediaLibrarySession$Builder$KbvKQ6JiEvVRMpYadxywG_GUsco, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 28 more
                    */
                /*
                    this = this;
                    android.media.-$$Lambda$MediaLibraryService2$MediaLibrarySession$Builder$KbvKQ6JiEvVRMpYadxywG_GUsco r0 = new android.media.-$$Lambda$MediaLibraryService2$MediaLibrarySession$Builder$KbvKQ6JiEvVRMpYadxywG_GUsco
                    r0.<init>(r2, r3, r4)
                    r1.<init>(r0)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.MediaLibraryService2.MediaLibrarySession.Builder.<init>(android.media.MediaLibraryService2, java.util.concurrent.Executor, android.media.MediaLibraryService2$MediaLibrarySession$MediaLibrarySessionCallback):void");
            }

            public Builder setPlayer(MediaPlayerBase player) {
                return (Builder) super.setPlayer(player);
            }

            public Builder setPlaylistAgent(MediaPlaylistAgent playlistAgent) {
                return (Builder) super.setPlaylistAgent(playlistAgent);
            }

            public Builder setVolumeProvider(VolumeProvider2 volumeProvider) {
                return (Builder) super.setVolumeProvider(volumeProvider);
            }

            public Builder setSessionActivity(PendingIntent pi) {
                return (Builder) super.setSessionActivity(pi);
            }

            public Builder setId(String id) {
                return (Builder) super.setId(id);
            }

            public Builder setSessionCallback(Executor executor, MediaLibrarySessionCallback callback) {
                return (Builder) super.setSessionCallback(executor, callback);
            }

            public MediaLibrarySession build() {
                return (MediaLibrarySession) super.build();
            }
        }

        public static class MediaLibrarySessionCallback extends MediaSession2.SessionCallback {
            public LibraryRoot onGetLibraryRoot(MediaLibrarySession session, MediaSession2.ControllerInfo controllerInfo, Bundle rootHints) {
                return null;
            }

            public MediaItem2 onGetItem(MediaLibrarySession session, MediaSession2.ControllerInfo controllerInfo, String mediaId) {
                return null;
            }

            public List<MediaItem2> onGetChildren(MediaLibrarySession session, MediaSession2.ControllerInfo controller, String parentId, int page, int pageSize, Bundle extras) {
                return null;
            }

            public void onSubscribe(MediaLibrarySession session, MediaSession2.ControllerInfo controller, String parentId, Bundle extras) {
            }

            public void onUnsubscribe(MediaLibrarySession session, MediaSession2.ControllerInfo controller, String parentId) {
            }

            public void onSearch(MediaLibrarySession session, MediaSession2.ControllerInfo controllerInfo, String query, Bundle extras) {
            }

            public List<MediaItem2> onGetSearchResult(MediaLibrarySession session, MediaSession2.ControllerInfo controllerInfo, String query, int page, int pageSize, Bundle extras) {
                return null;
            }
        }

        public MediaLibrarySession(MediaLibraryService2Provider.MediaLibrarySessionProvider provider) {
            super(provider);
            this.mProvider = provider;
        }

        public void notifyChildrenChanged(MediaSession2.ControllerInfo controller, String parentId, int itemCount, Bundle extras) {
            this.mProvider.notifyChildrenChanged_impl(controller, parentId, itemCount, extras);
        }

        public void notifyChildrenChanged(String parentId, int itemCount, Bundle extras) {
            this.mProvider.notifyChildrenChanged_impl(parentId, itemCount, extras);
        }

        public void notifySearchResultChanged(MediaSession2.ControllerInfo controller, String query, int itemCount, Bundle extras) {
            this.mProvider.notifySearchResultChanged_impl(controller, query, itemCount, extras);
        }
    }

    public abstract MediaLibrarySession onCreateSession(String str);

    /* access modifiers changed from: package-private */
    public MediaSessionService2Provider createProvider() {
        return ApiLoader.getProvider().createMediaLibraryService2(this);
    }
}
