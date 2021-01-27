package com.huawei.nb.ai;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nb.model.aimodel.AiModel;
import com.huawei.nb.security.RSAEncryptUtils;
import com.huawei.nb.utils.logger.DSLog;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AiModelResponse implements Parcelable {
    public static final Parcelable.Creator<AiModelResponse> CREATOR = new Parcelable.Creator<AiModelResponse>() {
        /* class com.huawei.nb.ai.AiModelResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AiModelResponse createFromParcel(Parcel parcel) {
            return new AiModelResponse(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AiModelResponse[] newArray(int i) {
            return new AiModelResponse[i];
        }
    };
    private List<AiModel> mAiModelList;
    private Map<Long, List<Long>> mAiModelWeightMap;
    private Map<Long, List<Long>> mAiModelWeightMeanMap;
    private boolean mIsNeedDecrypt;
    private String mModelFileKey;
    private String mPrivateKey;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public AiModelResponse() {
        this.mModelFileKey = null;
        this.mPrivateKey = null;
        this.mIsNeedDecrypt = false;
        this.mAiModelList = new ArrayList();
        this.mAiModelWeightMap = new HashMap();
        this.mAiModelWeightMeanMap = new HashMap();
    }

    public AiModelResponse(Parcel parcel) {
        this.mModelFileKey = null;
        this.mPrivateKey = null;
        this.mIsNeedDecrypt = false;
        if (parcel.readByte() == 0) {
            this.mAiModelList = null;
        } else {
            this.mAiModelList = parcel.readArrayList(AiModel.class.getClassLoader());
            this.mAiModelList = Collections.unmodifiableList(this.mAiModelList);
        }
        if (parcel.readByte() == 0) {
            this.mAiModelWeightMap = null;
        } else {
            this.mAiModelWeightMap = parcel.readHashMap(ArrayList.class.getClassLoader());
        }
        if (parcel.readByte() == 0) {
            this.mAiModelWeightMeanMap = null;
        } else {
            this.mAiModelWeightMeanMap = parcel.readHashMap(ArrayList.class.getClassLoader());
        }
        if (parcel.readByte() == 0) {
            this.mModelFileKey = null;
        } else {
            this.mModelFileKey = parcel.readString();
        }
        if (parcel.readByte() == 0) {
            this.mPrivateKey = null;
        } else {
            this.mPrivateKey = parcel.readString();
        }
        this.mIsNeedDecrypt = parcel.readByte() != 1 ? false : true;
    }

    /* access modifiers changed from: package-private */
    public void addAiModel(AiModel aiModel) {
        List<AiModel> list = this.mAiModelList;
        if (list != null && aiModel != null) {
            list.add(aiModel);
        }
    }

    /* access modifiers changed from: package-private */
    public AiModelResponse setNeedDecrypt() {
        this.mIsNeedDecrypt = true;
        return this;
    }

    /* access modifiers changed from: package-private */
    public AiModelResponse setModelFileKey(String str) {
        this.mModelFileKey = str;
        return this;
    }

    public List<AiModel> getAiModelList() {
        return this.mAiModelList;
    }

    public void setPrivateKey(String str) {
        this.mPrivateKey = str;
    }

    private boolean isModelEncrypted(AiModel aiModel) {
        return aiModel.getIs_encrypt() != null && aiModel.getIs_encrypt().intValue() == 1;
    }

    private AiModel decryptMetaData(AiModel aiModel, String str) {
        AiModel copyAiModel = AiModelAttributes.copyAiModel(aiModel);
        List<String> encryptedAttributes = AiModelAttributes.getEncryptedAttributes();
        Map<String, Supplier<Object>> attributes = AiModelAttributes.getAttributes(aiModel);
        Map<String, Consumer<String>> attributes2 = AiModelAttributes.setAttributes(copyAiModel);
        for (String str2 : encryptedAttributes) {
            String str3 = (String) attributes.get(str2).get();
            String decryptString = RSAEncryptUtils.decryptString(str3, str);
            if (str3 == null || decryptString != null) {
                attributes2.get(str2).accept(decryptString);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to decrypt meta data for AI Model [");
                sb.append(aiModel.getId() != null ? aiModel.getId() : "Unknown ID");
                sb.append("].");
                DSLog.w(sb.toString(), new Object[0]);
            }
        }
        return copyAiModel;
    }

    private AiModel findModelById(Long l, List<AiModel> list) {
        for (AiModel aiModel : list) {
            if (aiModel.getId().equals(l)) {
                return aiModel;
            }
        }
        return null;
    }

    private List<AiModel> findModelsByIds(List<Long> list, List<AiModel> list2) {
        ArrayList arrayList = new ArrayList();
        for (Long l : list) {
            arrayList.add(findModelById(l, list2));
        }
        return arrayList;
    }

    private Map<Long, List<Long>> getAiModelMap(int i) {
        initModelMapping();
        if (i == 3) {
            return this.mAiModelWeightMap;
        }
        if (i == 4) {
            return this.mAiModelWeightMeanMap;
        }
        return null;
    }

    private List<AiModel> getRelatedModels(Long l, int i) {
        List<Long> list;
        Map<Long, List<Long>> aiModelMap = getAiModelMap(i);
        if (aiModelMap == null || this.mAiModelList == null || l == null || (list = aiModelMap.get(l)) == null) {
            return null;
        }
        return findModelsByIds(list, this.mAiModelList);
    }

    private void putKVRelation(Long l, Long l2, Map<Long, List<Long>> map) {
        List<Long> list = map.get(l);
        if (list == null) {
            list = new ArrayList<>(0);
        }
        list.add(l2);
        map.put(l, list);
    }

    private void initModelMapping() {
        List<AiModel> list = this.mAiModelList;
        if (list == null || list.size() <= 0) {
            DSLog.w("No AI Models found response.", new Object[0]);
            return;
        }
        if (this.mAiModelWeightMap == null) {
            this.mAiModelWeightMap = new HashMap(0);
        }
        if (this.mAiModelWeightMeanMap == null) {
            this.mAiModelWeightMeanMap = new HashMap(0);
        }
        if (this.mAiModelWeightMap.size() == 0 && this.mAiModelWeightMeanMap.size() == 0) {
            for (AiModel aiModel : this.mAiModelList) {
                if (aiModel.getModel_type() == null) {
                    DSLog.w("The AI Model type is null, Please confirm that you have the authority to access this data.", new Object[0]);
                } else {
                    int intValue = aiModel.getModel_type().intValue();
                    if (intValue != 2) {
                        if (intValue == 3) {
                            putKVRelation(aiModel.getParent_id(), aiModel.getId(), this.mAiModelWeightMap);
                        } else if (intValue == 4) {
                            putKVRelation(aiModel.getParent_id(), aiModel.getId(), this.mAiModelWeightMeanMap);
                        }
                    }
                }
            }
            this.mAiModelWeightMap = Collections.unmodifiableMap(this.mAiModelWeightMap);
            this.mAiModelWeightMeanMap = Collections.unmodifiableMap(this.mAiModelWeightMeanMap);
        }
    }

    public AiModelByteBuffer loadAiModel(AiModel aiModel) {
        if (aiModel.getIs_none().intValue() != 0) {
            DSLog.e("Failed to load ai model, error: model is none.", new Object[0]);
            return null;
        }
        if (this.mIsNeedDecrypt) {
            aiModel = decryptMetaData(aiModel, this.mPrivateKey);
        }
        ByteBuffer readAiModel = AiModelReader.readAiModel(aiModel.getFile_path(), isModelEncrypted(aiModel) ? RSAEncryptUtils.decryptStringToBytes(this.mModelFileKey, this.mPrivateKey) : null);
        if (readAiModel == null) {
            return null;
        }
        AiModelByteBuffer aiModelByteBuffer = new AiModelByteBuffer(this);
        aiModelByteBuffer.setByteBuffer(readAiModel);
        return aiModelByteBuffer;
    }

    public List<AiModelByteBuffer> loadAiModel(List<AiModel> list) {
        if (list == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList(list.size());
        for (AiModel aiModel : list) {
            arrayList.add(loadAiModel(aiModel));
        }
        return arrayList;
    }

    public List<AiModel> getRelatedWeightModels(Long l) {
        return getRelatedModels(l, 3);
    }

    public List<AiModel> getRelatedWeightModels(AiModel aiModel) {
        if (aiModel != null) {
            return getRelatedWeightModels(aiModel.getId());
        }
        return null;
    }

    public List<AiModel> getRelatedMeanModels(Long l) {
        return getRelatedModels(l, 4);
    }

    public List<AiModel> getRelatedMeanModels(AiModel aiModel) {
        if (aiModel != null) {
            return getRelatedMeanModels(aiModel.getId());
        }
        return null;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        if (this.mAiModelList != null) {
            parcel.writeByte((byte) 1);
            parcel.writeList(this.mAiModelList);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mAiModelWeightMap != null) {
            parcel.writeByte((byte) 1);
            parcel.writeMap(this.mAiModelWeightMap);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mAiModelWeightMeanMap != null) {
            parcel.writeByte((byte) 1);
            parcel.writeMap(this.mAiModelWeightMeanMap);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mModelFileKey != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mModelFileKey);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mPrivateKey != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mPrivateKey);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mIsNeedDecrypt) {
            parcel.writeByte((byte) 1);
        } else {
            parcel.writeByte((byte) 0);
        }
    }
}
