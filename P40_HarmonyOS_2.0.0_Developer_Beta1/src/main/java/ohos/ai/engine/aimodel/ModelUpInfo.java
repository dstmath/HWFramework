package ohos.ai.engine.aimodel;

import java.util.ArrayList;
import java.util.List;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ModelUpInfo implements Sequenceable {
    public static final String ASR_BUSIDOMAIN_TYPE = "asr";
    public static final String ASR_SERVICE_TYPE = "asrservice";
    public static final String CV_BUSIDOMAIN_TYPE = "cv";
    public static final String CV_FACE_COMPARE_TYPE = "facecompare";
    public static final String CV_HEADPOSE_TYPE = "headpose";
    public static final String NLU_BUSIDOMAIN_TYPE = "nlu";
    public static final String NLU_ENGINE_TYPE = "nluengine";
    public static final Sequenceable.Producer<ModelUpInfo> PRODUCER = $$Lambda$NssaWIeHz9drjgwnfhZ9rg4cM7s.INSTANCE;
    public static final String TRANSLATIONINTERFACESTUB_TYPE = "translationinterfacestub";
    public static final String TRANSLATION_BUSIDOMAIN_TYPE = "translation";
    private List<AiModelBean> aiModelList;
    private String busiDomain;
    private String engineType;

    public ModelUpInfo() {
        this(null, null);
    }

    public ModelUpInfo(String str, String str2) {
        this.aiModelList = new ArrayList();
        this.busiDomain = str;
        this.engineType = str2;
    }

    protected ModelUpInfo(Parcel parcel) {
        this.aiModelList = new ArrayList();
        unmarshalling(parcel);
    }

    public List<AiModelBean> getAiModelList() {
        return this.aiModelList;
    }

    public void setAiModelList(List<AiModelBean> list) {
        this.aiModelList = list;
    }

    public String getBusiDomain() {
        return this.busiDomain;
    }

    public void setBusiDomain(String str) {
        this.busiDomain = str;
    }

    public String getEngineType() {
        return this.engineType;
    }

    public void setEngineType(String str) {
        this.engineType = str;
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.engineType);
        parcel.writeString(this.busiDomain);
        parcel.writeSequenceableList(this.aiModelList);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.engineType = parcel.readString();
        this.busiDomain = parcel.readString();
        this.aiModelList = parcel.readSequenceableList(AiModelBean.class);
        return true;
    }
}
