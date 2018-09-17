package tmsdk.common.module.aresengine;

import tmsdkobf.jf;

/* compiled from: Unknown */
public abstract class TelephonyEntity extends jf {
    public String name;
    public String phonenum;

    public TelephonyEntity(TelephonyEntity telephonyEntity) {
        this.id = telephonyEntity.id;
        this.phonenum = telephonyEntity.phonenum;
    }
}
