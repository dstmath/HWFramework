package ohos.sysappcomponents.calendar.entity;

import ohos.annotation.SystemApi;
import ohos.sysappcomponents.calendar.column.BaseColumns;
import ohos.sysappcomponents.calendar.column.EventsColumns;
import ohos.sysappcomponents.calendar.column.ParticipantsColumns;

public class Participants extends CalendarEntity implements BaseColumns, ParticipantsColumns, EventsColumns {
    @SystemApi
    private boolean deleted;
    private int eventId;
    private String participantEmail;
    @SystemApi
    private String participantIdNamespace;
    @SystemApi
    private String participantIdentity;
    private String participantName;
    private int participantRoleType;
    private int participantStatus;
    private int participantType;
    @SystemApi
    private String syncId;

    public int getEventId() {
        return this.eventId;
    }

    public void setEventId(int i) {
        this.eventId = i;
    }

    public String getParticipantName() {
        return this.participantName;
    }

    public void setParticipantName(String str) {
        this.participantName = str;
    }

    public String getParticipantEmail() {
        return this.participantEmail;
    }

    public void setParticipantEmail(String str) {
        this.participantEmail = str;
    }

    public int getParticipantRoleType() {
        return this.participantRoleType;
    }

    public void setParticipantRoleType(int i) {
        this.participantRoleType = i;
    }

    public int getParticipantType() {
        return this.participantType;
    }

    public void setParticipantType(int i) {
        this.participantType = i;
    }

    public int getParticipantStatus() {
        return this.participantStatus;
    }

    public void setParticipantStatus(int i) {
        this.participantStatus = i;
    }

    @SystemApi
    public String getParticipantIdentity() {
        return this.participantIdentity;
    }

    @SystemApi
    public void setParticipantIdentity(String str) {
        this.participantIdentity = str;
    }

    @SystemApi
    public String getParticipantIdNamespace() {
        return this.participantIdNamespace;
    }

    @SystemApi
    public void setParticipantIdNamespace(String str) {
        this.participantIdNamespace = str;
    }

    @SystemApi
    public boolean isDeleted() {
        return this.deleted;
    }

    @SystemApi
    public void setDeleted(boolean z) {
        this.deleted = z;
    }

    @SystemApi
    public String getSyncId() {
        return this.syncId;
    }

    @SystemApi
    public void setSyncId(String str) {
        this.syncId = str;
    }
}
