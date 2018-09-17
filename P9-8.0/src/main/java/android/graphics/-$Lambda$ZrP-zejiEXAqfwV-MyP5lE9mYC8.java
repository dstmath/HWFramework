package android.graphics;

import android.graphics.ColorSpace.Rgb;
import android.graphics.ColorSpace.Rgb.TransferParameters;
import java.util.function.DoubleUnaryOperator;

final /* synthetic */ class -$Lambda$ZrP-zejiEXAqfwV-MyP5lE9mYC8 implements DoubleUnaryOperator {

    /* renamed from: android.graphics.-$Lambda$ZrP-zejiEXAqfwV-MyP5lE9mYC8$2 */
    final /* synthetic */ class AnonymousClass2 implements DoubleUnaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ double $m$0(double arg0) {
            return ColorSpace.rcpResponse(arg0, ((TransferParameters) this.-$f0).a, ((TransferParameters) this.-$f0).b, ((TransferParameters) this.-$f0).c, ((TransferParameters) this.-$f0).d, ((TransferParameters) this.-$f0).g);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final double applyAsDouble(double d) {
            return $m$0(d);
        }
    }

    /* renamed from: android.graphics.-$Lambda$ZrP-zejiEXAqfwV-MyP5lE9mYC8$3 */
    final /* synthetic */ class AnonymousClass3 implements DoubleUnaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ double $m$0(double arg0) {
            return ColorSpace.rcpResponse(arg0, ((TransferParameters) this.-$f0).a, ((TransferParameters) this.-$f0).b, ((TransferParameters) this.-$f0).c, ((TransferParameters) this.-$f0).d, ((TransferParameters) this.-$f0).e, ((TransferParameters) this.-$f0).f, ((TransferParameters) this.-$f0).g);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final double applyAsDouble(double d) {
            return $m$0(d);
        }
    }

    /* renamed from: android.graphics.-$Lambda$ZrP-zejiEXAqfwV-MyP5lE9mYC8$4 */
    final /* synthetic */ class AnonymousClass4 implements DoubleUnaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ double $m$0(double arg0) {
            return ColorSpace.response(arg0, ((TransferParameters) this.-$f0).a, ((TransferParameters) this.-$f0).b, ((TransferParameters) this.-$f0).c, ((TransferParameters) this.-$f0).d, ((TransferParameters) this.-$f0).g);
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.-$f0 = obj;
        }

        public final double applyAsDouble(double d) {
            return $m$0(d);
        }
    }

    /* renamed from: android.graphics.-$Lambda$ZrP-zejiEXAqfwV-MyP5lE9mYC8$5 */
    final /* synthetic */ class AnonymousClass5 implements DoubleUnaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ double $m$0(double arg0) {
            return ColorSpace.response(arg0, ((TransferParameters) this.-$f0).a, ((TransferParameters) this.-$f0).b, ((TransferParameters) this.-$f0).c, ((TransferParameters) this.-$f0).d, ((TransferParameters) this.-$f0).e, ((TransferParameters) this.-$f0).f, ((TransferParameters) this.-$f0).g);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.-$f0 = obj;
        }

        public final double applyAsDouble(double d) {
            return $m$0(d);
        }
    }

    /* renamed from: android.graphics.-$Lambda$ZrP-zejiEXAqfwV-MyP5lE9mYC8$6 */
    final /* synthetic */ class AnonymousClass6 implements DoubleUnaryOperator {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ double $m$0(double arg0) {
            return ((Rgb) this.-$f0).-android_graphics_ColorSpace$Rgb-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass6(Object obj) {
            this.-$f0 = obj;
        }

        public final double applyAsDouble(double d) {
            return $m$0(d);
        }
    }

    /* renamed from: android.graphics.-$Lambda$ZrP-zejiEXAqfwV-MyP5lE9mYC8$7 */
    final /* synthetic */ class AnonymousClass7 implements DoubleUnaryOperator {
        private final /* synthetic */ double -$f0;

        private final /* synthetic */ double $m$0(double arg0) {
            return Rgb.lambda$-android_graphics_ColorSpace$Rgb_113213(this.-$f0, arg0);
        }

        public /* synthetic */ AnonymousClass7(double d) {
            this.-$f0 = d;
        }

        public final double applyAsDouble(double d) {
            return $m$0(d);
        }
    }

    /* renamed from: android.graphics.-$Lambda$ZrP-zejiEXAqfwV-MyP5lE9mYC8$8 */
    final /* synthetic */ class AnonymousClass8 implements DoubleUnaryOperator {
        private final /* synthetic */ double -$f0;

        private final /* synthetic */ double $m$0(double arg0) {
            return Rgb.lambda$-android_graphics_ColorSpace$Rgb_113354(this.-$f0, arg0);
        }

        public /* synthetic */ AnonymousClass8(double d) {
            this.-$f0 = d;
        }

        public final double applyAsDouble(double d) {
            return $m$0(d);
        }
    }

    public final double applyAsDouble(double d) {
        return $m$0(d);
    }
}
