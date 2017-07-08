package tmsdkobf;

import android.content.Intent;
import android.text.TextUtils;
import java.io.ByteArrayInputStream;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.utils.j;

/* compiled from: Unknown */
final class je implements ir {
    private final String SUBJECT;
    private Intent mIntent;
    private final String tT;
    private final String tU;
    private final String tV;
    private a tW;

    /* compiled from: Unknown */
    static final class a {
        String address;
        String serviceCenter;
        String subject;
        String url;

        a() {
            this.address = null;
        }

        public String getAddress() {
            return this.address != null ? this.address : this.url;
        }

        public String getBody() {
            return this.subject + this.url;
        }

        public String getServiceCenter() {
            return this.serviceCenter;
        }
    }

    public je(Intent intent) {
        this.tT = "0c";
        this.SUBJECT = "01";
        this.tU = "03";
        this.tV = "00";
        this.mIntent = new Intent(intent);
    }

    private boolean a(ByteArrayInputStream byteArrayInputStream) {
        byteArrayInputStream.reset();
        while (byteArrayInputStream.available() > 0) {
            String f = f(byteArrayInputStream);
            if ("0c".equals(f)) {
                this.tW.url = "http://" + new String(bD(e(byteArrayInputStream)));
            } else if ("01".equals(f)) {
                this.tW.subject = new String(bD(e(byteArrayInputStream)));
            }
        }
        return TextUtils.isEmpty(this.tW.subject);
    }

    private boolean b(ByteArrayInputStream byteArrayInputStream) {
        byteArrayInputStream.reset();
        while (byteArrayInputStream.available() > 0) {
            this.tW.subject = new String(bD(e(byteArrayInputStream)));
        }
        return TextUtils.isEmpty(this.tW.subject);
    }

    private byte[] bD(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        byte[] bArr = new byte[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((byte) ((hexCharToInt(str.charAt(i)) << 4) | hexCharToInt(str.charAt(i + 1))));
        }
        return bArr;
    }

    private boolean c(ByteArrayInputStream byteArrayInputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        String str = "";
        byteArrayInputStream.reset();
        while (byteArrayInputStream.available() > 0) {
            String f = f(byteArrayInputStream);
            if (f.equals("03")) {
                f = new String(bD(d(byteArrayInputStream)));
                stringBuilder.append(f);
                if (str.equals("0c")) {
                    this.tW.url = "http://" + f;
                } else if (str.equals("01")) {
                    this.tW.subject = f;
                }
            } else {
                str = f;
            }
        }
        if (TextUtils.isEmpty(this.tW.subject)) {
            this.tW.subject = stringBuilder.toString();
        }
        return TextUtils.isEmpty(this.tW.subject);
    }

    private String d(ByteArrayInputStream byteArrayInputStream) {
        if (byteArrayInputStream == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (byteArrayInputStream.available() > 0) {
            String f = f(byteArrayInputStream);
            if (f.equals("00")) {
                break;
            }
            stringBuilder.append(f);
        }
        return stringBuilder.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String e(ByteArrayInputStream byteArrayInputStream) {
        if (byteArrayInputStream == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (!f(byteArrayInputStream).equals("03")) {
            if (byteArrayInputStream.available() <= 0) {
                break;
            }
        }
        while (true) {
            String f = f(byteArrayInputStream);
            if (!f.equals("00") && byteArrayInputStream.available() > 0) {
                stringBuilder.append(f);
            }
        }
        return stringBuilder.toString();
    }

    private String f(ByteArrayInputStream byteArrayInputStream) {
        if (byteArrayInputStream == null) {
            return null;
        }
        int read = byteArrayInputStream.read();
        StringBuilder stringBuilder = new StringBuilder(2);
        stringBuilder.append("0123456789abcdef".charAt((read >> 4) & 15));
        stringBuilder.append("0123456789abcdef".charAt(read & 15));
        return stringBuilder.toString().toLowerCase();
    }

    private int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 65) + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 97) + 10;
        }
        throw new RuntimeException("invalid hex char '" + c + "'");
    }

    public SmsEntity ca() {
        SmsEntity smsEntity = null;
        byte[] byteArrayExtra = this.mIntent.getByteArrayExtra("data");
        if (byteArrayExtra != null && j.iM() > 3) {
            g(byteArrayExtra);
            smsEntity = new SmsEntity();
            smsEntity.phonenum = getAddress();
            smsEntity.body = getBody();
            smsEntity.serviceCenter = getServiceCenter();
            smsEntity.type = 1;
            smsEntity.protocolType = 2;
            smsEntity.raw = this.mIntent;
            qz qzVar = jq.uh;
            if (qzVar != null) {
                smsEntity.fromCard = qzVar.f(this.mIntent);
            }
        }
        return smsEntity;
    }

    public void g(byte[] bArr) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        this.tW = new a();
        if (!a(byteArrayInputStream) && !b(byteArrayInputStream) && !c(byteArrayInputStream)) {
            this.tW = null;
        }
    }

    public String getAddress() {
        return this.tW == null ? null : this.tW.getAddress();
    }

    public String getBody() {
        return this.tW == null ? null : this.tW.getBody();
    }

    public String getServiceCenter() {
        return this.tW == null ? null : this.tW.getServiceCenter();
    }
}
