package tmsdkobf;

import android.text.TextUtils;

public class qt {
    private static int Oi = 0;
    public String Oj;
    public String Ok;
    public String Ol;
    public String Om;
    public String On;
    public String Oo;
    public String Op;
    public String Oq;
    public boolean Or;
    public String mDescription;
    public String mFileName;

    public qt() {
        StringBuilder append = new StringBuilder().append("");
        int i = Oi + 1;
        Oi = i;
        this.Oj = append.append(i).toString();
    }

    public static void a(StringBuilder stringBuilder, qt qtVar) {
        stringBuilder.append('0');
        stringBuilder.append(qtVar.Oj);
        if (!TextUtils.isEmpty(qtVar.Ok)) {
            stringBuilder.append(':');
            stringBuilder.append('1');
            stringBuilder.append(qtVar.Ok);
        }
        if (!TextUtils.isEmpty(qtVar.mFileName)) {
            stringBuilder.append(':');
            stringBuilder.append('2');
            stringBuilder.append(qtVar.mFileName);
        }
        if (!TextUtils.isEmpty(qtVar.Ol)) {
            stringBuilder.append(':');
            stringBuilder.append('3');
            stringBuilder.append(qtVar.Ol);
        }
        if (!TextUtils.isEmpty(qtVar.Om)) {
            stringBuilder.append(':');
            stringBuilder.append('4');
            stringBuilder.append(qtVar.Om);
        }
        if (!TextUtils.isEmpty(qtVar.On)) {
            stringBuilder.append(':');
            stringBuilder.append('5');
            stringBuilder.append(qtVar.On);
        }
        if (!TextUtils.isEmpty(qtVar.Oo)) {
            stringBuilder.append(':');
            stringBuilder.append('6');
            stringBuilder.append(qtVar.Oo);
        }
        stringBuilder.append(':');
        stringBuilder.append('8');
        stringBuilder.append(qtVar.Op);
    }
}
