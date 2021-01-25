package com.android.ims;

import android.telecom.ConferenceParticipant;
import com.android.ims.ImsCall;
import java.util.List;

public interface IHwImsCallManagerInner {
    List<ConferenceParticipant> getConferenceParticipantsForExt();

    IHwImsCallEx getHwImsCallEx();

    ImsCall getImsCall();

    ImsCall.Listener getListener();

    void setConferenceParticipants(List<ConferenceParticipant> list);

    void setImsCallState(boolean z, boolean z2, boolean z3);
}
