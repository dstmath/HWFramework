package tmsdk.common.module.aresengine;

public interface IEntityConverter {
    <T extends CallLogEntity> T convert(CallLogEntity callLogEntity);

    <T extends SmsEntity> T convert(SmsEntity smsEntity);
}
