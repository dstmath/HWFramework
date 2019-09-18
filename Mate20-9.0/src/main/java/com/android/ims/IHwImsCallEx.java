package com.android.ims;

import android.telecom.ConferenceParticipant;

public interface IHwImsCallEx {
    void addQcomConferenceParticipant(ConferenceParticipant conferenceParticipant);

    Object getQcomLockObj();

    void hangupForegroundResumeBackground(int i, IHwImsCallManagerInner iHwImsCallManagerInner) throws ImsException;

    void hangupWaitingOrBackground(int i, IHwImsCallManagerInner iHwImsCallManagerInner) throws ImsException;

    void removeQcomParticipantsFromList(String[] strArr);

    void replaceQcomConferenceParticipantsList();

    void setIsQcomCEPPresent(boolean z);

    boolean shouldSaveQcomParticipantList();

    void updateQcomConferenceParticipantsList(IHwImsCallManagerInner iHwImsCallManagerInner);
}
