package ohos.event.commonevent;

import java.util.List;
import java.util.Optional;
import ohos.aafwk.content.IntentParams;
import ohos.aafwk.content.Skills;
import ohos.event.EventConstant;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;

/* access modifiers changed from: package-private */
public class MatchingSkillsTransformation {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    private static final String TAG = "MatchingSkillsTransformation";

    static boolean writeToParcel(CommonEventSubscribeInfo commonEventSubscribeInfo, Parcel parcel) {
        if (parcel == null) {
            HiLog.warn(LABEL, "out is null.", new Object[0]);
            return false;
        }
        Skills orElse = convertMatchingSkillsToZSkills(commonEventSubscribeInfo.getMatchingSkills()).orElse(null);
        if (orElse == null) {
            HiLog.warn(LABEL, "get skills failed.", new Object[0]);
            return false;
        } else if (!parcel.writeInt(1)) {
            HiLog.warn(LABEL, "write skills failed.", new Object[0]);
            return false;
        } else {
            parcel.writeSequenceable(orElse);
            if (!parcel.writeString(commonEventSubscribeInfo.getDeviceId())) {
                HiLog.warn(LABEL, "write publisherDeviceId failed.", new Object[0]);
                return false;
            } else if (!parcel.writeString(commonEventSubscribeInfo.getPermission())) {
                HiLog.warn(LABEL, "write publisherPermission failed.", new Object[0]);
                return false;
            } else if (!parcel.writeInt(commonEventSubscribeInfo.getThreadMode().ordinal())) {
                HiLog.warn(LABEL, "write threadMode failed.", new Object[0]);
                return false;
            } else if (!parcel.writeInt(commonEventSubscribeInfo.getUserId())) {
                HiLog.warn(LABEL, "write userId failed.", new Object[0]);
                return false;
            } else if (parcel.writeInt(commonEventSubscribeInfo.getPriority())) {
                return true;
            } else {
                HiLog.warn(LABEL, "write priority failed.", new Object[0]);
                return false;
            }
        }
    }

    static Optional<CommonEventSubscribeInfo> readFromParcel(Parcel parcel) {
        if (parcel == null) {
            return Optional.empty();
        }
        if (parcel.readInt() == 1) {
            Skills skills = new Skills();
            if (!parcel.readSequenceable(skills)) {
                HiLog.warn(LABEL, "read skills failed.", new Object[0]);
                return Optional.empty();
            }
            CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo(convertZSkillsToMatchingSkills(skills).orElse(null));
            commonEventSubscribeInfo.setDeviceId(parcel.readString());
            commonEventSubscribeInfo.setPermission(parcel.readString());
            int readInt = parcel.readInt();
            if (readInt < CommonEventSubscribeInfo.ThreadMode.HANDLER.ordinal() || readInt > CommonEventSubscribeInfo.ThreadMode.BACKGROUND.ordinal()) {
                readInt = CommonEventSubscribeInfo.ThreadMode.HANDLER.ordinal();
            }
            commonEventSubscribeInfo.setThreadMode(CommonEventSubscribeInfo.ThreadMode.values()[readInt]);
            commonEventSubscribeInfo.setUserId(parcel.readInt());
            commonEventSubscribeInfo.setPriority(parcel.readInt());
            return Optional.of(commonEventSubscribeInfo);
        }
        HiLog.warn(LABEL, "read invalid parcel.", new Object[0]);
        return Optional.empty();
    }

    static Optional<Skills> convertMatchingSkillsToZSkills(MatchingSkills matchingSkills) {
        if (matchingSkills == null) {
            return Optional.empty();
        }
        Skills skills = new Skills();
        int countEvents = matchingSkills.countEvents();
        for (int i = 0; i < countEvents; i++) {
            skills.addAction(matchingSkills.getEvent(i));
        }
        List<String> entities = matchingSkills.getEntities();
        if (entities != null) {
            for (String str : entities) {
                skills.addEntity(str);
            }
        }
        int countSchemes = matchingSkills.countSchemes();
        for (int i2 = 0; i2 < countSchemes; i2++) {
            skills.addScheme(matchingSkills.getScheme(i2));
        }
        if (matchingSkills.getIntentParams() != null) {
            skills.setIntentParams(new IntentParams(matchingSkills.getIntentParams()));
        }
        return Optional.of(skills);
    }

    private static Optional<MatchingSkills> convertZSkillsToMatchingSkills(Skills skills) {
        if (skills == null) {
            return Optional.empty();
        }
        MatchingSkills matchingSkills = new MatchingSkills();
        int countActions = skills.countActions();
        for (int i = 0; i < countActions; i++) {
            matchingSkills.addEvent(skills.getAction(i));
        }
        List<String> entities = skills.getEntities();
        if (entities != null) {
            for (String str : entities) {
                matchingSkills.addEntity(str);
            }
        }
        int countSchemes = skills.countSchemes();
        for (int i2 = 0; i2 < countSchemes; i2++) {
            matchingSkills.addScheme(skills.getScheme(i2));
        }
        if (skills.getIntentParams() != null) {
            matchingSkills.setIntentParams(new IntentParams(skills.getIntentParams()));
        }
        return Optional.of(matchingSkills);
    }

    private MatchingSkillsTransformation() {
    }
}
