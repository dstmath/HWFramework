package ohos.aafwk.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import ohos.aafwk.content.PatternsMatcher;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

@SystemApi
public class Skills implements Sequenceable {
    private static final int LIST_LENGTH = 1024;
    public static final Sequenceable.Producer<Skills> PRODUCER = $$Lambda$Skills$iRFZwZxuLS6XDlIZfcKhQgxzXE.INSTANCE;
    private static final String SCHEME_HARMONY = "dataability";
    private static final int VALUE_NULL = -1;
    private static final int VALUE_OBJECT = 1;
    private List<String> actions;
    private List<String> authorities;
    private List<String> entities;
    private IntentParams parameters;
    private List<PatternsMatcher> pathsMatcher;
    private List<String> schemeSpecificParts;
    private List<String> schemes;
    private List<PatternsMatcher> typesMatcher;

    static /* synthetic */ Skills lambda$static$0(Parcel parcel) {
        Skills skills = new Skills();
        skills.unmarshalling(parcel);
        return skills;
    }

    public Skills() {
    }

    public Skills(Skills skills) {
        if (skills != null) {
            List<String> list = skills.entities;
            if (list != null) {
                this.entities = new ArrayList(list);
            }
            List<String> list2 = skills.actions;
            if (list2 != null) {
                this.actions = new ArrayList(list2);
            }
            List<String> list3 = skills.schemes;
            if (list3 != null) {
                this.schemes = new ArrayList(list3);
            }
            List<String> list4 = skills.schemeSpecificParts;
            if (list4 != null) {
                this.schemeSpecificParts = new ArrayList(list4);
            }
            List<String> list5 = skills.authorities;
            if (list5 != null) {
                this.authorities = new ArrayList(list5);
            }
            List<PatternsMatcher> list6 = skills.pathsMatcher;
            if (list6 != null) {
                this.pathsMatcher = new ArrayList(list6);
            }
            List<PatternsMatcher> list7 = skills.typesMatcher;
            if (list7 != null) {
                this.typesMatcher = new ArrayList(list7);
            }
            if (skills.getIntentParams() != null) {
                setIntentParams(skills.getIntentParams());
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

    public void removeEntity(String str) {
        List<String> list = this.entities;
        if (list != null) {
            list.remove(str);
            if (this.entities.size() == 0) {
                this.entities = null;
            }
        }
    }

    public boolean hasEntity(String str) {
        List<String> list = this.entities;
        return list != null && list.contains(str);
    }

    public String getEntity(int i) {
        List<String> list = this.entities;
        if (list != null) {
            return list.get(i);
        }
        return null;
    }

    public int countEntities() {
        List<String> list = this.entities;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public List<String> getEntities() {
        return this.entities;
    }

    public Iterator<String> entitiesIterator() {
        List<String> list = this.entities;
        if (list != null) {
            return list.iterator();
        }
        return null;
    }

    public void addAction(String str) {
        if (str != null && !str.isEmpty()) {
            if (this.actions == null) {
                this.actions = new ArrayList();
            }
            if (!this.actions.contains(str)) {
                this.actions.add(str);
            }
        }
    }

    public void removeAction(String str) {
        List<String> list = this.actions;
        if (list != null) {
            list.remove(str);
            if (this.actions.size() == 0) {
                this.actions = null;
            }
        }
    }

    public String getAction(int i) {
        List<String> list = this.actions;
        if (list != null) {
            return list.get(i);
        }
        return null;
    }

    public final int countActions() {
        List<String> list = this.actions;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public boolean hasAction(String str) {
        List<String> list = this.actions;
        return list != null && list.contains(str);
    }

    public Iterator<String> actionsIterator() {
        List<String> list = this.actions;
        if (list != null) {
            return list.iterator();
        }
        return null;
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

    public void removeScheme(String str) {
        List<String> list = this.schemes;
        if (list != null) {
            list.remove(str);
            if (this.schemes.size() == 0) {
                this.schemes = null;
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

    public Iterator<String> schemesIterator() {
        List<String> list = this.schemes;
        if (list != null) {
            return list.iterator();
        }
        return null;
    }

    public void addSchemeSpecificPart(String str) {
        if (str != null && !str.isEmpty()) {
            if (this.schemeSpecificParts == null) {
                this.schemeSpecificParts = new ArrayList();
            }
            if (!this.schemeSpecificParts.contains(str)) {
                this.schemeSpecificParts.add(str);
            }
        }
    }

    public void removeSchemeSpecificPart(String str) {
        List<String> list = this.schemeSpecificParts;
        if (list != null) {
            list.remove(str);
            if (this.schemeSpecificParts.size() == 0) {
                this.schemeSpecificParts = null;
            }
        }
    }

    public String getSchemeSpecificPart(int i) {
        List<String> list = this.schemeSpecificParts;
        if (list != null) {
            return list.get(i);
        }
        return null;
    }

    public final int countSchemeSpecificParts() {
        List<String> list = this.schemeSpecificParts;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public boolean hasSchemeSpecificPart(String str) {
        List<String> list = this.schemeSpecificParts;
        return list != null && list.contains(str);
    }

    public Iterator<String> schemeSpecificPartsIterator() {
        List<String> list = this.schemeSpecificParts;
        if (list != null) {
            return list.iterator();
        }
        return null;
    }

    public void addAuthority(String str) {
        if (str != null && !str.isEmpty()) {
            if (this.authorities == null) {
                this.authorities = new ArrayList();
            }
            if (!this.authorities.contains(str)) {
                this.authorities.add(str);
            }
        }
    }

    public void removeAuthority(String str) {
        List<String> list = this.authorities;
        if (list != null) {
            list.remove(str);
            if (this.authorities.size() == 0) {
                this.authorities = null;
            }
        }
    }

    public String getAuthority(int i) {
        List<String> list = this.authorities;
        if (list != null) {
            return list.get(i);
        }
        return null;
    }

    public final int countAuthorities() {
        List<String> list = this.authorities;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public boolean hasAuthority(String str) {
        List<String> list = this.authorities;
        return list != null && list.contains(str);
    }

    public Iterator<String> authoritiesIterator() {
        List<String> list = this.authorities;
        if (list != null) {
            return list.iterator();
        }
        return null;
    }

    public void addPath(String str) {
        if (str != null && !str.isEmpty()) {
            addPath(new PatternsMatcher(str));
        }
    }

    public void addPath(String str, PatternsMatcher.MatchType matchType) {
        if (str != null && !str.isEmpty()) {
            addPath(new PatternsMatcher(str, matchType));
        }
    }

    public void addPath(PatternsMatcher patternsMatcher) {
        if (this.pathsMatcher == null) {
            this.pathsMatcher = new ArrayList();
        }
        if (containsPath(patternsMatcher) < 0) {
            this.pathsMatcher.add(patternsMatcher);
        }
    }

    public void removePath(String str) {
        removePath(new PatternsMatcher(str));
    }

    public void removePath(String str, PatternsMatcher.MatchType matchType) {
        removePath(new PatternsMatcher(str, matchType));
    }

    public void removePath(PatternsMatcher patternsMatcher) {
        int containsPath = containsPath(patternsMatcher);
        List<PatternsMatcher> list = this.pathsMatcher;
        if (list != null && containsPath >= 0) {
            list.remove(containsPath);
            if (this.pathsMatcher.size() == 0) {
                this.pathsMatcher = null;
            }
        }
    }

    public String getPath(int i) {
        List<PatternsMatcher> list = this.pathsMatcher;
        if (list != null) {
            return list.get(i).getPattern();
        }
        return null;
    }

    public final int countPaths() {
        List<PatternsMatcher> list = this.pathsMatcher;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public boolean hasPath(String str) {
        return this.pathsMatcher != null && containsPath(new PatternsMatcher(str)) >= 0;
    }

    public Iterator<String> pathsIterator() {
        List<String> generatePaths = generatePaths();
        if (generatePaths != null) {
            return generatePaths.iterator();
        }
        return null;
    }

    public void addType(String str) {
        if (str != null && !str.isEmpty()) {
            addType(new PatternsMatcher(str));
        }
    }

    public void addType(String str, PatternsMatcher.MatchType matchType) {
        if (str != null && !str.isEmpty()) {
            addType(new PatternsMatcher(str, matchType));
        }
    }

    public void addType(PatternsMatcher patternsMatcher) {
        if (this.typesMatcher == null) {
            this.typesMatcher = new ArrayList();
        }
        if (containsType(patternsMatcher) < 0) {
            this.typesMatcher.add(patternsMatcher);
        }
    }

    public void removeType(String str) {
        removeType(new PatternsMatcher(str));
    }

    public void removeType(String str, PatternsMatcher.MatchType matchType) {
        removeType(new PatternsMatcher(str, matchType));
    }

    public void removeType(PatternsMatcher patternsMatcher) {
        int containsType = containsType(patternsMatcher);
        List<PatternsMatcher> list = this.typesMatcher;
        if (list != null && containsType >= 0) {
            list.remove(containsType);
            if (this.typesMatcher.size() == 0) {
                this.typesMatcher = null;
            }
        }
    }

    public String getType(int i) {
        List<PatternsMatcher> list = this.typesMatcher;
        if (list != null) {
            return list.get(i).getPattern();
        }
        return null;
    }

    public final int countTypes() {
        List<PatternsMatcher> list = this.typesMatcher;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public boolean hasType(String str) {
        return (str == null || this.typesMatcher == null || containsType(new PatternsMatcher(str)) < 0) ? false : true;
    }

    public Iterator<String> typesIterator() {
        List<String> generateTypes = generateTypes();
        if (generateTypes != null) {
            return generateTypes.iterator();
        }
        return null;
    }

    public void setIntentParams(IntentParams intentParams) {
        if (intentParams == null) {
            this.parameters = null;
        } else {
            this.parameters = new IntentParams(intentParams);
        }
    }

    public IntentParams getIntentParams() {
        return this.parameters;
    }

    public boolean marshalling(Parcel parcel) {
        return writeList(parcel, this.entities) && writeList(parcel, this.actions) && writeList(parcel, this.schemes) && writeList(parcel, this.schemeSpecificParts) && writeList(parcel, this.authorities) && writeObjList(parcel, this.pathsMatcher) && writeObjList(parcel, this.typesMatcher) && writeIntentParams(parcel, this.parameters);
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

    private boolean writeIntentParams(Parcel parcel, IntentParams intentParams) {
        if (intentParams == null) {
            return parcel.writeInt(-1);
        }
        if (!parcel.writeInt(1)) {
            return false;
        }
        parcel.writeSequenceable(intentParams);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.entities = null;
        if (parcel.readInt() == 1) {
            this.entities = Arrays.asList(parcel.readStringArray());
        }
        this.actions = null;
        if (parcel.readInt() == 1) {
            this.actions = Arrays.asList(parcel.readStringArray());
        }
        this.schemes = null;
        if (parcel.readInt() == 1) {
            this.schemes = Arrays.asList(parcel.readStringArray());
        }
        this.schemeSpecificParts = null;
        if (parcel.readInt() == 1) {
            this.schemeSpecificParts = Arrays.asList(parcel.readStringArray());
        }
        this.authorities = null;
        if (parcel.readInt() == 1) {
            this.authorities = Arrays.asList(parcel.readStringArray());
        }
        this.pathsMatcher = null;
        if (parcel.readInt() == 1) {
            List readSequenceableList = parcel.readSequenceableList(PatternsMatcher.class);
            this.pathsMatcher = new ArrayList();
            int size = readSequenceableList.size();
            for (int i = 0; i < size; i++) {
                this.pathsMatcher.add((PatternsMatcher) readSequenceableList.get(i));
            }
        }
        this.typesMatcher = null;
        if (parcel.readInt() == 1) {
            List readSequenceableList2 = parcel.readSequenceableList(PatternsMatcher.class);
            this.typesMatcher = new ArrayList();
            int size2 = readSequenceableList2.size();
            if (size2 > 1024) {
                return false;
            }
            for (int i2 = 0; i2 < size2; i2++) {
                if (!(readSequenceableList2.get(i2) instanceof PatternsMatcher)) {
                    return false;
                }
                this.typesMatcher.add((PatternsMatcher) readSequenceableList2.get(i2));
            }
        }
        this.parameters = null;
        if (parcel.readInt() == 1) {
            this.parameters = new IntentParams();
            if (!parcel.readSequenceable(this.parameters)) {
                this.parameters = null;
                return false;
            }
        }
        return true;
    }

    private boolean matchAction(String str) {
        List<String> list = this.actions;
        if ((list == null || list.isEmpty()) && (str == null || str.isEmpty())) {
            return true;
        }
        return hasAction(str);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x007f, code lost:
        if (r3 == false) goto L_0x0081;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b1, code lost:
        if (r8 == false) goto L_0x00b3;
     */
    private boolean matchUriAndType(Uri uri, String str) {
        boolean z;
        boolean z2;
        if (this.schemes == null && this.typesMatcher == null) {
            return uri == null && str == null;
        }
        String str2 = null;
        String scheme = uri == null ? null : uri.getScheme();
        if (this.schemes != null) {
            if (scheme == null) {
                scheme = "";
            }
            if (!hasScheme(scheme)) {
                return false;
            }
            if (!hasSchemeSpecificPart(uri == null ? null : uri.getEncodedSchemeSpecificPart())) {
                String encodedAuthority = uri == null ? null : uri.getEncodedAuthority();
                if (this.authorities != null) {
                    if (encodedAuthority != null && !encodedAuthority.isEmpty()) {
                        Iterator<String> it = this.authorities.iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                z2 = false;
                                break;
                            }
                            String next = it.next();
                            if (next.equals(encodedAuthority)) {
                                break;
                            }
                            if (encodedAuthority.startsWith(next + ":")) {
                                break;
                            }
                        }
                        z2 = true;
                    }
                    return false;
                }
                if (this.pathsMatcher != null) {
                    if (uri != null) {
                        str2 = uri.getEncodedPath();
                    }
                    if (str2 != null && !str2.isEmpty()) {
                        Iterator<PatternsMatcher> it2 = this.pathsMatcher.iterator();
                        while (true) {
                            if (it2.hasNext()) {
                                if (it2.next().match(str2)) {
                                    z = true;
                                    break;
                                }
                            } else {
                                z = false;
                                break;
                            }
                        }
                    }
                    return false;
                }
            }
        } else if (scheme != null && !scheme.equals(SCHEME_HARMONY)) {
            return false;
        }
        List<PatternsMatcher> list = this.typesMatcher;
        if (list == null) {
            return str == null;
        }
        for (PatternsMatcher patternsMatcher : list) {
            if (patternsMatcher.match(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchEntities(Set<String> set) {
        if (!(set == null || set.size() == 0)) {
            for (String str : set) {
                if (!hasEntity(str)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean match(Intent intent) {
        if (intent != null && matchAction(intent.getAction()) && matchUriAndType(intent.getUri(), intent.getStringParam("mime-type")) && matchEntities(intent.getEntities())) {
            return true;
        }
        return false;
    }

    private int containsPath(PatternsMatcher patternsMatcher) {
        List<PatternsMatcher> list = this.pathsMatcher;
        if (list == null) {
            return -1;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (this.pathsMatcher.get(i).equals(patternsMatcher)) {
                return i;
            }
        }
        return -1;
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

    private List<String> generatePaths() {
        if (this.pathsMatcher == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (PatternsMatcher patternsMatcher : this.pathsMatcher) {
            arrayList.add(patternsMatcher.getPattern());
        }
        return arrayList;
    }

    private List<String> generateTypes() {
        if (this.typesMatcher == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (PatternsMatcher patternsMatcher : this.typesMatcher) {
            arrayList.add(patternsMatcher.getPattern());
        }
        return arrayList;
    }
}
