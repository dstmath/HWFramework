package tmsdkobf;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.utils.d;
import tmsdk.common.utils.j;

/* compiled from: Unknown */
final class jb implements ir {
    private static Intent tt;
    private Intent mIntent;
    private ir sY;

    /* compiled from: Unknown */
    static final class a implements ir {
        private SmsMessage tu;

        a() {
        }

        public SmsEntity ca() {
            return null;
        }

        public void g(byte[] bArr) {
            try {
                this.tu = SmsMessage.createFromPdu(bArr);
            } catch (Throwable th) {
                this.tu = null;
            }
        }

        public String getAddress() {
            return this.tu == null ? null : this.tu.getOriginatingAddress();
        }

        public String getBody() {
            return this.tu == null ? null : this.tu.getMessageBody();
        }

        public String getServiceCenter() {
            return this.tu == null ? null : this.tu.getServiceCenterAddress();
        }
    }

    /* compiled from: Unknown */
    static final class b implements ir {
        private static Method tw;
        private static Constructor<android.telephony.SmsMessage> tx;
        private android.telephony.SmsMessage tv;

        b() {
        }

        public SmsEntity ca() {
            return null;
        }

        public void g(byte[] bArr) {
            Constructor constructor;
            Object[] objArr;
            try {
                String str;
                String str2;
                qz qzVar = jq.uh;
                if (qzVar == null) {
                    str = null;
                    str2 = null;
                } else {
                    str = qzVar.f(jb.tt);
                    str2 = qzVar.im();
                }
                if (str2 != null && str != null && qzVar.in() && str.equals("0")) {
                    if (tw != null) {
                        if (tx != null) {
                            constructor = tx;
                            objArr = new Object[1];
                            objArr[0] = tw.invoke(null, new Object[]{bArr});
                            this.tv = (android.telephony.SmsMessage) constructor.newInstance(objArr);
                            this.tv.getMessageBody();
                            return;
                        }
                    }
                    tw = Class.forName("com.android.internal.telephony.cdma.SmsMessage").getMethod("createFromPdu", new Class[]{byte[].class});
                    tw.setAccessible(true);
                    tx = android.telephony.SmsMessage.class.getDeclaredConstructor(new Class[]{Class.forName("com.android.internal.telephony.SmsMessageBase")});
                    tx.setAccessible(true);
                    constructor = tx;
                    objArr = new Object[1];
                    objArr[0] = tw.invoke(null, new Object[]{bArr});
                    this.tv = (android.telephony.SmsMessage) constructor.newInstance(objArr);
                    this.tv.getMessageBody();
                    return;
                }
                if (!(str2 == null || str == null)) {
                    if (qzVar.io()) {
                        int cZ = qzVar.cZ(str);
                        this.tv = android.telephony.SmsMessage.createFromPdu(bArr);
                        if (this.tv != null) {
                            this.tv.getMessageBody();
                        }
                        if (this.tv == null || this.tv.getMessageBody() == null) {
                            try {
                                if (tw != null) {
                                    if (tx != null) {
                                    }
                                }
                                tw = Class.forName("android.telephony.gemini.GeminiSmsMessage").getMethod("createFromPdu", new Class[]{byte[].class, Integer.TYPE});
                                tw.setAccessible(true);
                                this.tv = (android.telephony.SmsMessage) tw.invoke(null, new Object[]{bArr, Integer.valueOf(cZ)});
                                this.tv.getMessageBody();
                            } catch (Exception e) {
                                this.tv = null;
                            }
                        }
                        if (this.tv != null && this.tv.getMessageBody() != null) {
                            return;
                        }
                        if (cZ == 1) {
                            try {
                                if (tw != null) {
                                    if (tx != null) {
                                        return;
                                    }
                                }
                                tw = Class.forName("com.android.internal.telephony.gsm.SmsMessage").getMethod("createFromPdu", new Class[]{byte[].class});
                                tw.setAccessible(true);
                                this.tv = (android.telephony.SmsMessage) tw.invoke(null, new Object[]{bArr});
                                this.tv.getMessageBody();
                                return;
                            } catch (Exception e2) {
                                this.tv = null;
                                return;
                            }
                        }
                        return;
                    }
                }
                this.tv = android.telephony.SmsMessage.createFromPdu(bArr);
                this.tv.getMessageBody();
            } catch (Exception e3) {
                this.tv = null;
            } catch (Throwable th) {
                try {
                    if (tw != null) {
                        if (tx != null) {
                            constructor = tx;
                            objArr = new Object[1];
                            objArr[0] = tw.invoke(null, new Object[]{bArr});
                            this.tv = (android.telephony.SmsMessage) constructor.newInstance(objArr);
                            this.tv.getMessageBody();
                        }
                    }
                    tw = Class.forName("com.android.internal.telephony.gsm.SmsMessage").getMethod("createFromPdu", new Class[]{byte[].class});
                    tw.setAccessible(true);
                    tx = android.telephony.SmsMessage.class.getDeclaredConstructor(new Class[]{Class.forName("com.android.internal.telephony.SmsMessageBase")});
                    tx.setAccessible(true);
                    constructor = tx;
                    objArr = new Object[1];
                    objArr[0] = tw.invoke(null, new Object[]{bArr});
                    this.tv = (android.telephony.SmsMessage) constructor.newInstance(objArr);
                    this.tv.getMessageBody();
                } catch (Exception e4) {
                    this.tv = null;
                }
            }
        }

        public String getAddress() {
            return this.tv == null ? null : this.tv.getOriginatingAddress();
        }

        public String getBody() {
            return this.tv == null ? null : this.tv.getMessageBody();
        }

        public String getServiceCenter() {
            return this.tv == null ? null : this.tv.getServiceCenterAddress();
        }
    }

    public jb(Intent intent) {
        this.mIntent = new Intent(intent);
        tt = new Intent(intent);
    }

    public SmsEntity ca() {
        Bundle extras = this.mIntent.getExtras();
        Object[] objArr = extras == null ? null : (Object[]) extras.get("pdus");
        if (objArr == null || objArr.length == 0) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        String str = null;
        for (int i = 0; i < objArr.length; i++) {
            if (objArr[i] != null) {
                g((byte[]) objArr[i]);
                if (getBody() == null) {
                    break;
                }
                stringBuffer.append(getBody());
                if (str == null) {
                    str = getAddress();
                }
            }
        }
        if (str == null) {
            return null;
        }
        SmsEntity smsEntity = new SmsEntity();
        smsEntity.phonenum = str;
        smsEntity.body = stringBuffer.toString();
        smsEntity.serviceCenter = getServiceCenter();
        smsEntity.type = 1;
        smsEntity.protocolType = 0;
        smsEntity.raw = this.mIntent;
        qz qzVar = jq.uh;
        if (qzVar != null) {
            smsEntity.fromCard = qzVar.f(this.mIntent);
            d.d("DualSim", "SMSParser number:" + smsEntity.phonenum + " fromcard:" + smsEntity.fromCard);
        }
        return smsEntity;
    }

    public void g(byte[] bArr) {
        if (this.sY == null) {
            this.sY = j.iM() <= 3 ? new a() : new b();
        }
        this.sY.g(bArr);
    }

    public String getAddress() {
        return this.sY.getAddress();
    }

    public String getBody() {
        return this.sY.getBody();
    }

    public String getServiceCenter() {
        return this.sY.getServiceCenter();
    }
}
