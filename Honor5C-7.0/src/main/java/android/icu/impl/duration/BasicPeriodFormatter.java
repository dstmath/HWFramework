package android.icu.impl.duration;

import android.icu.impl.duration.impl.PeriodFormatterData;

class BasicPeriodFormatter implements PeriodFormatter {
    private Customizations customs;
    private PeriodFormatterData data;
    private BasicPeriodFormatterFactory factory;
    private String localeName;

    private java.lang.String format(int r1, boolean r2, int[] r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.BasicPeriodFormatter.format(int, boolean, int[]):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.BasicPeriodFormatter.format(int, boolean, int[]):java.lang.String");
    }

    BasicPeriodFormatter(BasicPeriodFormatterFactory factory, String localeName, PeriodFormatterData data, Customizations customs) {
        this.factory = factory;
        this.localeName = localeName;
        this.data = data;
        this.customs = customs;
    }

    public String format(Period period) {
        if (period.isSet()) {
            return format(period.timeLimit, period.inFuture, period.counts);
        }
        throw new IllegalArgumentException("period is not set");
    }

    public PeriodFormatter withLocale(String locName) {
        if (this.localeName.equals(locName)) {
            return this;
        }
        return new BasicPeriodFormatter(this.factory, locName, this.factory.getData(locName), this.customs);
    }
}
