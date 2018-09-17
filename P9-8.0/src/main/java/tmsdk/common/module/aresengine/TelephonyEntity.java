package tmsdk.common.module.aresengine;

import tmsdkobf.ib;

public abstract class TelephonyEntity extends ib {
    public String name;
    public String phonenum;

    public TelephonyEntity(TelephonyEntity telephonyEntity) {
        this.id = telephonyEntity.id;
        this.phonenum = telephonyEntity.phonenum;
    }
}
