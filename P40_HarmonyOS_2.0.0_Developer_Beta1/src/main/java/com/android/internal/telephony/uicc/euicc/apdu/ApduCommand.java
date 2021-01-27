package com.android.internal.telephony.uicc.euicc.apdu;

/* access modifiers changed from: package-private */
public class ApduCommand {
    public final int channel;
    public final int cla;
    public final String cmdHex;
    public final int ins;
    public final int p1;
    public final int p2;
    public final int p3;

    ApduCommand(int channel2, int cla2, int ins2, int p12, int p22, int p32, String cmdHex2) {
        this.channel = channel2;
        this.cla = cla2;
        this.ins = ins2;
        this.p1 = p12;
        this.p2 = p22;
        this.p3 = p32;
        this.cmdHex = cmdHex2;
    }

    public String toString() {
        return "ApduCommand(channel=" + this.channel + ", cla=" + this.cla + ", ins=" + this.ins + ", p1=" + this.p1 + ", p2=" + this.p2 + ", p3=" + this.p3 + ", cmd=" + this.cmdHex + ")";
    }
}
