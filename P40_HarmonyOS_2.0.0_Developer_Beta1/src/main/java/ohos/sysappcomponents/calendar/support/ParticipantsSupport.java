package ohos.sysappcomponents.calendar.support;

import java.util.ArrayList;
import java.util.Optional;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.calendar.LogUtil;
import ohos.sysappcomponents.calendar.entity.CalendarEntity;
import ohos.sysappcomponents.calendar.entity.Participants;
import ohos.utils.net.Uri;

public class ParticipantsSupport extends TableSupport {
    private static final String EVENT_ID_RAW_SELECTION = "event_id=?";
    private static final String TAG = ParticipantsSupport.class.getSimpleName();

    private ParticipantsSupport(Class<? extends CalendarEntity> cls, Uri uri) {
        super(cls, uri);
    }

    static ParticipantsSupport getInstance() {
        return InnerSingleton.INSTANCE;
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public Rule getRule() {
        return new Rule() {
            /* class ohos.sysappcomponents.calendar.support.$$Lambda$ParticipantsSupport$ktLML5aAYxlZrYlauCmNmjN1eGI */

            @Override // ohos.sysappcomponents.calendar.support.Rule
            public final Optional getEntity(ResultSet resultSet, String[] strArr) {
                return ParticipantsSupport.this.lambda$getRule$0$ParticipantsSupport(resultSet, strArr);
            }
        };
    }

    public /* synthetic */ Optional lambda$getRule$0$ParticipantsSupport(ResultSet resultSet, String[] strArr) {
        Participants participants = new Participants();
        for (String str : strArr) {
            setEntityValue(participants, resultSet, str);
        }
        return Optional.of(participants);
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public ValuesBucket getValueBucket(CalendarEntity calendarEntity) {
        ValuesBucket valuesBucket = new ValuesBucket();
        if (calendarEntity == null) {
            return valuesBucket;
        }
        if (calendarEntity instanceof Participants) {
            setValuesBucket(valuesBucket, (Participants) calendarEntity);
        } else {
            LogUtil.warn(TAG, "Invalid input. Input must be participants entity objects.");
        }
        return valuesBucket;
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public DataAbilityPredicates getPredicate(int i) {
        DataAbilityPredicates dataAbilityPredicates = new DataAbilityPredicates(EVENT_ID_RAW_SELECTION);
        ArrayList arrayList = new ArrayList();
        arrayList.add(String.valueOf(i));
        dataAbilityPredicates.setWhereArgs(arrayList);
        return dataAbilityPredicates;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public void initColumnMap() {
        initBaseColumnsMap();
        initParticipantsColumnsMap();
    }

    /* access modifiers changed from: private */
    public static class InnerSingleton {
        private static final ParticipantsSupport INSTANCE = new ParticipantsSupport(Participants.class, TableUri.PARTICIPANT_DATA_ABILITY_URI);

        private InnerSingleton() {
        }
    }

    private void setValuesBucket(ValuesBucket valuesBucket, Participants participants) {
        setValuesBucketSyncColumnsField(valuesBucket, participants);
        setValuesBucketParticipantsColumnsField(valuesBucket, participants);
    }

    private void setValuesBucketSyncColumnsField(ValuesBucket valuesBucket, Participants participants) {
        valuesBucket.putBoolean("deleted", Boolean.valueOf(participants.isDeleted()));
        valuesBucket.putString("_sync_id", participants.getSyncId());
    }

    private void setValuesBucketParticipantsColumnsField(ValuesBucket valuesBucket, Participants participants) {
        valuesBucket.putInteger("event_id", Integer.valueOf(participants.getEventId()));
        valuesBucket.putString("attendeeName", participants.getParticipantName());
        valuesBucket.putString("attendeeEmail", participants.getParticipantEmail());
        valuesBucket.putInteger("attendeeRelationship", Integer.valueOf(participants.getParticipantRoleType()));
        valuesBucket.putInteger("attendeeType", Integer.valueOf(participants.getParticipantType()));
        valuesBucket.putInteger("attendeeStatus", Integer.valueOf(participants.getParticipantStatus()));
        valuesBucket.putString("attendeeIdentity", participants.getParticipantIdentity());
        valuesBucket.putString("attendeeIdNamespace", participants.getParticipantIdNamespace());
    }
}
