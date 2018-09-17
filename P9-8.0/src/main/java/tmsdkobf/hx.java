package tmsdkobf;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.utils.f;
import tmsdk.common.utils.n;

final class hx implements hn {
    private static Intent qS;
    private Intent mIntent;
    private hn qx;

    static final class a implements hn {
        private SmsMessage qT;

        a() {
        }

        public SmsEntity bt() {
            return null;
        }

        public void g(byte[] bArr) {
            try {
                this.qT = SmsMessage.createFromPdu(bArr);
            } catch (Throwable th) {
                this.qT = null;
            }
        }

        public String getAddress() {
            return this.qT == null ? null : this.qT.getOriginatingAddress();
        }

        public String getBody() {
            return this.qT == null ? null : this.qT.getMessageBody();
        }

        public String getServiceCenter() {
            return this.qT == null ? null : this.qT.getServiceCenterAddress();
        }
    }

    static final class b implements hn {
        private static Method qV;
        private static Constructor<android.telephony.SmsMessage> qW;
        private android.telephony.SmsMessage qU;

        b() {
        }

        public SmsEntity bt() {
            return null;
        }

        /* JADX WARNING: Missing block: B:32:0x0105, code:
            if (qW != null) goto L_0x0073;
     */
        /* JADX WARNING: Missing block: B:62:0x01bf, code:
            if (qW != null) goto L_0x0167;
     */
        /* JADX WARNING: Missing block: B:74:0x01d9, code:
            if (qW != null) goto L_0x00da;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void g(byte[] bArr) {
            Constructor constructor;
            Object[] objArr;
            try {
                qc qcVar = im.rE;
                String str = null;
                String str2 = null;
                if (qcVar != null) {
                    str2 = qcVar.f(hx.qS);
                    str = qcVar.iv();
                }
                if (str != null && str2 != null && qcVar.iw() && str2.equals("0")) {
                    if (qV != null) {
                    }
                    qV = Class.forName("com.android.internal.telephony.cdma.SmsMessage").getMethod("createFromPdu", new Class[]{byte[].class});
                    qV.setAccessible(true);
                    Class cls = Class.forName("com.android.internal.telephony.SmsMessageBase");
                    qW = android.telephony.SmsMessage.class.getDeclaredConstructor(new Class[]{cls});
                    qW.setAccessible(true);
                    constructor = qW;
                    objArr = new Object[1];
                    objArr[0] = qV.invoke(null, new Object[]{bArr});
                    this.qU = (android.telephony.SmsMessage) constructor.newInstance(objArr);
                    this.qU.getMessageBody();
                    return;
                }
                if (!(str == null || str2 == null)) {
                    if (qcVar.ix()) {
                        int cu = qcVar.cu(str2);
                        this.qU = android.telephony.SmsMessage.createFromPdu(bArr);
                        if (this.qU != null) {
                            this.qU.getMessageBody();
                        }
                        if (this.qU == null || this.qU.getMessageBody() == null) {
                            try {
                                if (qV != null) {
                                }
                                qV = Class.forName("android.telephony.gemini.GeminiSmsMessage").getMethod("createFromPdu", new Class[]{byte[].class, Integer.TYPE});
                                qV.setAccessible(true);
                                this.qU = (android.telephony.SmsMessage) qV.invoke(null, new Object[]{bArr, Integer.valueOf(cu)});
                                this.qU.getMessageBody();
                            } catch (Exception e) {
                                this.qU = null;
                            }
                        }
                        if ((this.qU == null || this.qU.getMessageBody() == null) && cu == 1) {
                            try {
                                if (qV != null) {
                                    if (qW != null) {
                                        return;
                                    }
                                }
                                qV = Class.forName("com.android.internal.telephony.gsm.SmsMessage").getMethod("createFromPdu", new Class[]{byte[].class});
                                qV.setAccessible(true);
                                this.qU = (android.telephony.SmsMessage) qV.invoke(null, new Object[]{bArr});
                                this.qU.getMessageBody();
                                return;
                            } catch (Exception e2) {
                                this.qU = null;
                                return;
                            }
                        }
                        return;
                    }
                }
                this.qU = android.telephony.SmsMessage.createFromPdu(bArr);
                this.qU.getMessageBody();
            } catch (Exception e3) {
                this.qU = null;
            } catch (Throwable th) {
                try {
                    if (qV != null) {
                    }
                    qV = Class.forName("com.android.internal.telephony.gsm.SmsMessage").getMethod("createFromPdu", new Class[]{byte[].class});
                    qV.setAccessible(true);
                    Class cls2 = Class.forName("com.android.internal.telephony.SmsMessageBase");
                    qW = android.telephony.SmsMessage.class.getDeclaredConstructor(new Class[]{cls2});
                    qW.setAccessible(true);
                    constructor = qW;
                    objArr = new Object[1];
                    objArr[0] = qV.invoke(null, new Object[]{bArr});
                    this.qU = (android.telephony.SmsMessage) constructor.newInstance(objArr);
                    this.qU.getMessageBody();
                } catch (Exception e4) {
                    this.qU = null;
                }
            }
        }

        public String getAddress() {
            return this.qU == null ? null : this.qU.getOriginatingAddress();
        }

        public String getBody() {
            return this.qU == null ? null : this.qU.getMessageBody();
        }

        public String getServiceCenter() {
            return this.qU == null ? null : this.qU.getServiceCenterAddress();
        }
    }

    public hx(Intent intent) {
        this.mIntent = new Intent(intent);
        qS = new Intent(intent);
    }

    public SmsEntity bt() {
        Bundle extras = this.mIntent.getExtras();
        Object[] objArr = null;
        if (extras != null) {
            objArr = (Object[]) extras.get("pdus");
        }
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
        qc qcVar = im.rE;
        if (qcVar != null) {
            smsEntity.fromCard = qcVar.f(this.mIntent);
            f.f("DualSim", "SMSParser number:" + smsEntity.phonenum + " fromcard:" + smsEntity.fromCard);
        }
        return smsEntity;
    }

    public void g(byte[] bArr) {
        if (this.qx == null) {
            this.qx = n.iX() <= 3 ? new a() : new b();
        }
        this.qx.g(bArr);
    }

    public String getAddress() {
        return this.qx.getAddress();
    }

    public String getBody() {
        return this.qx.getBody();
    }

    public String getServiceCenter() {
        return this.qx.getServiceCenter();
    }
}
