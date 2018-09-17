package android.print;

import java.util.function.IntConsumer;

final /* synthetic */ class PrinterCapabilitiesInfo$Builder$-android_print_PrinterCapabilitiesInfo$Builder_setDuplexModes_int_duplexModes_int_defaultDuplexMode_LambdaImpl0 implements IntConsumer {
    public void accept(int arg0) {
        PrintAttributes.enforceValidDuplexMode(arg0);
    }
}
