package ohos.event.commonevent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ohos.aafwk.content.IntentParams;
import ohos.aafwk.content.PatternsMatcher;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class MatchingSkills implements Sequenceable {
    private static final int LIST_LENGTH = 1024;
    public static final Sequenceable.Producer<MatchingSkills> PRODUCER = $$Lambda$MatchingSkills$rtXf0Yd_XHL2Y1AVycyAMRgn8.INSTANCE;
    private static final int VALUE_NULL = -1;
    private static final int VALUE_OBJECT = 1;
    private List<String> entities;
    private List<String> events;
    private IntentParams intentParams;
    private List<String> schemes;
    private List<PatternsMatcher> typesMatcher;

    static /* synthetic */ MatchingSkills lambda$static$0(Parcel parcel) {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.unmarshalling(parcel);
        return matchingSkills;
    }

    public MatchingSkills() {
    }

    public MatchingSkills(MatchingSkills matchingSkills) {
        if (matchingSkills != null) {
            List<String> list = matchingSkills.entities;
            if (list != null) {
                this.entities = new ArrayList(list);
            }
            List<String> list2 = matchingSkills.events;
            if (list2 != null) {
                this.events = new ArrayList(list2);
            }
            List<String> list3 = matchingSkills.schemes;
            if (list3 != null) {
                this.schemes = new ArrayList(list3);
            }
            if (matchingSkills.getIntentParams() != null) {
                setIntentParams(matchingSkills.getIntentParams());
            }
        }
    }

    public void addEntity(String str) {
        if (str != null && !str.isEmpty()) {
            if (this.entities == null) {
                this.entities = new ArrayList();
            }
            if (!this.entities.contains(str)) {
                this.entities.add(str);
            }
        }
    }

    public List<String> getEntities() {
        return this.entities;
    }

    public void addEvent(String str) {
        if (str != null && !str.isEmpty()) {
            if (this.events == null) {
                this.events = new ArrayList();
            }
            if (!this.events.contains(str)) {
                this.events.add(str);
            }
        }
    }

    public String getEvent(int i) {
        List<String> list = this.events;
        if (list != null) {
            return list.get(i);
        }
        return null;
    }

    public final int countEvents() {
        List<String> list = this.events;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public void addScheme(String str) {
        if (str != null && !str.isEmpty()) {
            if (this.schemes == null) {
                this.schemes = new ArrayList();
            }
            if (!this.schemes.contains(str)) {
                this.schemes.add(str);
            }
        }
    }

    public String getScheme(int i) {
        List<String> list = this.schemes;
        if (list != null) {
            return list.get(i);
        }
        return null;
    }

    public final int countSchemes() {
        List<String> list = this.schemes;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public boolean hasScheme(String str) {
        List<String> list = this.schemes;
        return list != null && list.contains(str);
    }

    public void setIntentParams(IntentParams intentParams2) {
        if (intentParams2 == null) {
            this.intentParams = null;
        } else {
            this.intentParams = new IntentParams(intentParams2);
        }
    }

    public IntentParams getIntentParams() {
        return this.intentParams;
    }

    public void addType(String str) {
        if (str != null && !str.isEmpty()) {
            if (this.typesMatcher == null) {
                this.typesMatcher = new ArrayList();
            }
            PatternsMatcher patternsMatcher = new PatternsMatcher(str);
            if (containsType(patternsMatcher) < 0) {
                this.typesMatcher.add(patternsMatcher);
            }
        }
    }

    public boolean hasType(String str) {
        return (str == null || this.typesMatcher == null || containsType(new PatternsMatcher(str)) < 0) ? false : true;
    }

    private int containsType(PatternsMatcher patternsMatcher) {
        List<PatternsMatcher> list = this.typesMatcher;
        if (list == null) {
            return -1;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (this.typesMatcher.get(i).equals(patternsMatcher)) {
                return i;
            }
        }
        return -1;
    }

    public boolean marshalling(Parcel parcel) {
        return writeList(parcel, this.entities) && writeList(parcel, this.events) && writeList(parcel, this.schemes) && writeIntentParams(parcel, this.intentParams) && writeObjList(parcel, this.typesMatcher);
    }

    private boolean writeList(Parcel parcel, List<String> list) {
        if (list == null || list.isEmpty()) {
            if (!parcel.writeInt(-1)) {
                return false;
            }
        } else if (!parcel.writeInt(1)) {
            return false;
        } else {
            String[] strArr = new String[list.size()];
            list.toArray(strArr);
            if (!parcel.writeStringArray(strArr)) {
                return false;
            }
        }
        return true;
    }

    private boolean writeIntentParams(Parcel parcel, IntentParams intentParams2) {
        if (intentParams2 == null) {
            return parcel.writeInt(-1);
        }
        if (!parcel.writeInt(1)) {
            return false;
        }
        parcel.writeSequenceable(intentParams2);
        return true;
    }

    private boolean writeObjList(Parcel parcel, List<PatternsMatcher> list) {
        if (list == null || list.isEmpty()) {
            if (!parcel.writeInt(-1)) {
                return false;
            }
        } else if (!parcel.writeInt(1)) {
            return false;
        } else {
            parcel.writeSequenceableList(list);
        }
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.entities = null;
        if (parcel.readInt() == 1) {
            this.entities = Arrays.asList(parcel.readStringArray());
        }
        this.events = null;
        if (parcel.readInt() == 1) {
            this.events = Arrays.asList(parcel.readStringArray());
        }
        this.schemes = null;
        if (parcel.readInt() == 1) {
            this.schemes = Arrays.asList(parcel.readStringArray());
        }
        this.intentParams = null;
        if (parcel.readInt() == 1) {
            this.intentParams = new IntentParams();
            if (!parcel.readSequenceable(this.intentParams)) {
                this.intentParams = null;
                return false;
            }
        }
        this.typesMatcher = null;
        if (parcel.readInt() == 1) {
            List readSequenceableList = parcel.readSequenceableList(PatternsMatcher.class);
            this.typesMatcher = new ArrayList();
            int size = readSequenceableList.size();
            if (size > 1024) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (!(readSequenceableList.get(i) instanceof PatternsMatcher)) {
                    return false;
                }
                this.typesMatcher.add((PatternsMatcher) readSequenceableList.get(i));
            }
        }
        return true;
    }
}
