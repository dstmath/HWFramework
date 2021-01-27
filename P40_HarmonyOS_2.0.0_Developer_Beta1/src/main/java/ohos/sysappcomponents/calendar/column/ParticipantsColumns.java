package ohos.sysappcomponents.calendar.column;

import ohos.annotation.SystemApi;

public interface ParticipantsColumns {
    public static final String EVENT_ID = "event_id";
    public static final String PARTICIPANT_EMAIL = "attendeeEmail";
    @SystemApi
    public static final String PARTICIPANT_IDENTITY = "attendeeIdentity";
    @SystemApi
    public static final String PARTICIPANT_ID_NAMESPACE = "attendeeIdNamespace";
    public static final String PARTICIPANT_NAME = "attendeeName";
    public static final String PARTICIPANT_ROLE_TYPE = "attendeeRelationship";
    public static final String PARTICIPANT_STATUS = "attendeeStatus";
    public static final int PARTICIPANT_STATUS_ACCEPTED = 1;
    public static final int PARTICIPANT_STATUS_DECLINED = 2;
    public static final int PARTICIPANT_STATUS_INVITED = 3;
    public static final int PARTICIPANT_STATUS_NONE = 0;
    public static final int PARTICIPANT_STATUS_TENTATIVE = 4;
    public static final String PARTICIPANT_TYPE = "attendeeType";
    public static final int ROLE_ATTENDEE = 1;
    public static final int ROLE_NONE = 0;
    public static final int ROLE_ORGANIZER = 2;
    public static final int ROLE_PERFORMER = 3;
    public static final int ROLE_SPEAKER = 4;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_OPTIONAL = 2;
    public static final int TYPE_REQUIRED = 1;
    public static final int TYPE_RESOURCE = 3;
}
