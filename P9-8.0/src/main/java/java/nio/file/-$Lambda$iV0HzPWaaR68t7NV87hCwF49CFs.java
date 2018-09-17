package java.nio.file;

import java.io.Closeable;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$iV0HzPWaaR68t7NV87hCwF49CFs implements Function {

    /* renamed from: java.nio.file.-$Lambda$iV0HzPWaaR68t7NV87hCwF49CFs$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            Files.lambda$-java_nio_file_Files_3831((Closeable) this.-$f0);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: java.nio.file.-$Lambda$iV0HzPWaaR68t7NV87hCwF49CFs$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((FileTreeIterator) this.-$f0).-java_nio_file_Files-mthref-1();
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: java.nio.file.-$Lambda$iV0HzPWaaR68t7NV87hCwF49CFs$4 */
    final /* synthetic */ class AnonymousClass4 implements Runnable {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0() {
            ((FileTreeIterator) this.-$f0).-java_nio_file_Files-mthref-1();
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: java.nio.file.-$Lambda$iV0HzPWaaR68t7NV87hCwF49CFs$5 */
    final /* synthetic */ class AnonymousClass5 implements Predicate {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((BiPredicate) this.-$f0).test(((Event) arg0).lambda$-java_nio_file_Files_166757(), ((Event) arg0).attributes());
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    public final Object apply(Object obj) {
        return $m$0(obj);
    }
}
