package tmsdkobf;

import android.content.Intent;
import android.text.TextUtils;
import java.io.ByteArrayInputStream;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.utils.n;

final class ia implements hn {
    private final String SUBJECT = "01";
    private Intent mIntent;
    private final String rs = "0c";
    private final String rt = "03";
    private final String ru = "00";
    private a rv;

    static final class a {
        String subject;
        String url;

        a() {
        }

        public String getAddress() {
            return this.url;
        }

        public String getBody() {
            return this.subject + this.url;
        }

        public String getServiceCenter() {
            return null;
        }
    }

    public ia(Intent intent) {
        this.mIntent = new Intent(intent);
    }

    private boolean a(ByteArrayInputStream byteArrayInputStream) {
        byteArrayInputStream.reset();
        while (byteArrayInputStream.available() > 0) {
            String f = f(byteArrayInputStream);
            if ("0c".equals(f)) {
                this.rv.url = "http://" + new String(aF(e(byteArrayInputStream)));
            } else if ("01".equals(f)) {
                this.rv.subject = new String(aF(e(byteArrayInputStream)));
            }
        }
        return TextUtils.isEmpty(this.rv.subject);
    }

    private byte[] aF(String str) {
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

    private boolean b(ByteArrayInputStream byteArrayInputStream) {
        byteArrayInputStream.reset();
        while (byteArrayInputStream.available() > 0) {
            this.rv.subject = new String(aF(e(byteArrayInputStream)));
        }
        return TextUtils.isEmpty(this.rv.subject);
    }

    private boolean c(ByteArrayInputStream byteArrayInputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        String str = "";
        byteArrayInputStream.reset();
        while (byteArrayInputStream.available() > 0) {
            String f = f(byteArrayInputStream);
            if (f.equals("03")) {
                String str2 = new String(aF(d(byteArrayInputStream)));
                stringBuilder.append(str2);
                if (str.equals("0c")) {
                    this.rv.url = "http://" + str2;
                } else if (str.equals("01")) {
                    this.rv.subject = str2;
                }
            } else {
                str = f;
            }
        }
        if (TextUtils.isEmpty(this.rv.subject)) {
            this.rv.subject = stringBuilder.toString();
        }
        return TextUtils.isEmpty(this.rv.subject);
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

    public SmsEntity bt() {
        byte[] byteArrayExtra = this.mIntent.getByteArrayExtra("data");
        SmsEntity smsEntity = null;
        if (byteArrayExtra != null && n.iX() > 3) {
            g(byteArrayExtra);
            smsEntity = new SmsEntity();
            smsEntity.phonenum = getAddress();
            smsEntity.body = getBody();
            smsEntity.serviceCenter = getServiceCenter();
            smsEntity.type = 1;
            smsEntity.protocolType = 2;
            smsEntity.raw = this.mIntent;
            qc qcVar = im.rE;
            if (qcVar != null) {
                smsEntity.fromCard = qcVar.f(this.mIntent);
            }
        }
        return smsEntity;
    }

    public void g(byte[] bArr) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        this.rv = new a();
        if (!a(byteArrayInputStream) && !b(byteArrayInputStream) && !c(byteArrayInputStream)) {
            this.rv = null;
        }
    }

    public String getAddress() {
        return this.rv == null ? null : this.rv.getAddress();
    }

    public String getBody() {
        return this.rv == null ? null : this.rv.getBody();
    }

    public String getServiceCenter() {
        return this.rv == null ? null : this.rv.getServiceCenter();
    }
}
