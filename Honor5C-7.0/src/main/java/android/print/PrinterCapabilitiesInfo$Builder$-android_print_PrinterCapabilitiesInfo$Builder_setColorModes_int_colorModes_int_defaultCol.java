package android.print;

import java.util.function.IntConsumer;

final /* synthetic */ class PrinterCapabilitiesInfo$Builder$-android_print_PrinterCapabilitiesInfo$Builder_setColorModes_int_colorModes_int_defaultColorMode_LambdaImpl0 implements IntConsumer {
    public void accept(int arg0) {
        PrintAttributes.enforceValidColorMode(arg0);
    }
}
