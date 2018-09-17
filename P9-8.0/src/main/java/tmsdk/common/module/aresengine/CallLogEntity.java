package tmsdk.common.module.aresengine;

public class CallLogEntity extends TelephonyEntity {
    public long date;
    public long duration;
    public String fromCard;
    public int type;

    public CallLogEntity(CallLogEntity callLogEntity) {
        super(callLogEntity);
        this.date = callLogEntity.date;
        this.type = callLogEntity.type;
        this.duration = callLogEntity.duration;
        this.fromCard = callLogEntity.fromCard;
    }
}
