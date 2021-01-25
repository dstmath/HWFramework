package com.android.server.hidata.wavemapping.modelservice;

import android.util.wifi.HwHiLog;
import com.android.server.hidata.HwHidataJniAdapter;
import com.android.server.hidata.wavemapping.chr.entity.ApChrStatInfo;
import com.android.server.hidata.wavemapping.chr.entity.BuildModelChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dao.EnterpriseApDao;
import com.android.server.hidata.wavemapping.dao.MobileApDao;
import com.android.server.hidata.wavemapping.entity.ApInfo;
import com.android.server.hidata.wavemapping.entity.CoreTrainData;
import com.android.server.hidata.wavemapping.entity.MobileApCheckParamInfo;
import com.android.server.hidata.wavemapping.entity.ModelInfo;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.entity.StdDataSet;
import com.android.server.hidata.wavemapping.entity.StdRecord;
import com.android.server.hidata.wavemapping.entity.TMapList;
import com.android.server.hidata.wavemapping.entity.TMapSet;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.GetStd;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.lang.reflect.Array;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TrainModelService extends ModelBaseService {
    private static final String COLON = ":";
    private static final String COMMA = ",";
    private static final String DASH = "-";
    private static final int DEFAULT_AVG_NUM = -100;
    private static final int DEFAULT_DATA_LENGTH = 2;
    private static final int DEFAULT_INFO_LENGTH = 4;
    private static final int DEFAULT_INIT_VALUE = -1;
    private static final int DEFAULT_TIME_UNIT = 1000;
    private static final String DEFAULT_VALUE = "0";
    private static final String DOT = ".";
    private static final String DOT_ESCAPE = "\\.";
    private static final int LIST_DEFAULT_CAPACITY = 10;
    private static final int MAP_DEFAULT_CAPACITY = 16;
    public static final String TAG = ("WMapping." + TrainModelService.class.getSimpleName());
    private static final String UNDER_LINE = "_";
    private EnterpriseApDao enterpriseApDao = new EnterpriseApDao();
    private MobileApDao mobileApDao = new MobileApDao();

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00ed: APUT  
      (r7v8 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (r11v2 'filePath' java.lang.String A[D('filePath' java.lang.String)])
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0183: APUT  
      (r10v8 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (r8v0 'place' java.lang.String A[D('place' java.lang.String)])
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x01c4: APUT  
      (r4v5 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x01bf: INVOKE  (r2v14 java.lang.String) = (r0v33 'e' java.lang.NumberFormatException A[D('e' java.lang.NumberFormatException)]) type: VIRTUAL call: java.lang.NumberFormatException.getMessage():java.lang.String)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x01ef: APUT  
      (r2v8 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (r8v0 'place' java.lang.String A[D('place' java.lang.String)])
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0146, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0147, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01e6, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01e7, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x020b, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x020c, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x020e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x020f, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0212, code lost:
        com.android.server.hidata.wavemapping.util.LogUtil.e(false, "LocatingState failed by Exception", new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x021b, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x021c, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x021d, code lost:
        r1 = false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0211 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:16:0x0088] */
    public ModelInfo loadModel(ParameterInfo param, RegularPlaceInfo placeInfo) {
        String place;
        String modelName;
        ModelInfo modelInfo;
        char c;
        boolean z;
        RuntimeException e;
        int bssidsLen;
        int headerLen;
        String str;
        NumberFormatException e2;
        String str2 = ",";
        if (param == null) {
            LogUtil.d(false, "param == null", new Object[0]);
            return null;
        } else if (placeInfo == null) {
            LogUtil.d(false, "placeInfo == null", new Object[0]);
            return null;
        } else if (placeInfo.getPlace() == null) {
            LogUtil.d(false, "place == null", new Object[0]);
            return null;
        } else {
            place = placeInfo.getPlace();
            modelName = place.replace(":", "").replace("-", "") + DOT + placeInfo.getModelName();
            LogUtil.i(false, " loadModel begin:%{public}s", modelName);
            String filePath = getModelFilePath(placeInfo, param);
            String fileContent = FileUtils.getFileContent(filePath);
            if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
                LogUtil.d(false, "loadModel ,file content is too bigger than max_file_size.", new Object[0]);
                return null;
            }
            modelInfo = new ModelInfo(place, placeInfo.getModelName());
            try {
                String[] lines = fileContent.split(Constant.getLineSeparator());
                if (lines.length < 1) {
                    LogUtil.e(false, "Failure loadModel %{public}s,lines length is zero.", modelName);
                    return null;
                }
                String[] headers = lines[0].split(str2);
                HashSet<String> setBssids = new HashSet<>(16);
                int bssidsLen2 = headers.length - param.getBssidStart();
                if (bssidsLen2 < 0) {
                    LogUtil.i(false, "loadModel,bssidsLen:%{public}d,headers.length:%{public}d,parameterInfo.getBssidStart(): %{public}d %{public}s", Integer.valueOf(bssidsLen2), Integer.valueOf(headers.length), Integer.valueOf(param.getBssidStart()), Constant.getLineSeparator());
                    Object[] objArr = new Object[2];
                    objArr[0] = filePath;
                    objArr[1] = Constant.getLineSeparator();
                    LogUtil.i(false, "loadModel,fPath:%{public}s %{public}s", objArr);
                    LogUtil.i(false, "loadModel,fileContent:%{public}s %{public}s", fileContent, Constant.getLineSeparator());
                    LogUtil.i(false, "loadModel,lines[0]:%{public}s", lines[0]);
                    return null;
                }
                int bssidStart = param.getBssidStart();
                String[] bssids = new String[bssidsLen2];
                int headerLen2 = headers.length;
                if (bssidStart > headerLen2) {
                    return null;
                }
                processBssids(setBssids, bssids, headers, bssidStart);
                if (setBssids.size() != bssidsLen2) {
                    LogUtil.e(false, "loadModel,Failure loadModel %{public}s,has duplicate bssid.", modelName);
                    return null;
                }
                modelInfo.setSetBssids(setBssids);
                modelInfo.setBssidList(bssids);
                int lineLen = lines.length;
                int[][] datas = (int[][]) Array.newInstance(int.class, lineLen - 1, bssids.length);
                int i = 1;
                while (i < lines.length) {
                    try {
                        String[] arrWords = lines[i].split(str2);
                        str = str2;
                        try {
                            if (arrWords.length < headerLen2) {
                                headerLen = headerLen2;
                                bssidsLen = bssidsLen2;
                                try {
                                    Object[] objArr2 = new Object[3];
                                    objArr2[0] = place;
                                    objArr2[1] = modelName;
                                    objArr2[2] = Integer.valueOf(i);
                                    LogUtil.e(false, "loadModel,Load Model failure,place :%{public}s,modelName:%{public}s, line num:%{public}d", objArr2);
                                } catch (NumberFormatException e3) {
                                    e2 = e3;
                                    Object[] objArr3 = new Object[1];
                                    objArr3[0] = e2.getMessage();
                                    LogUtil.e(false, "LocatingState,e %{public}s", objArr3);
                                    i++;
                                    str2 = str;
                                    headerLen2 = headerLen;
                                    bssidsLen2 = bssidsLen;
                                }
                            } else {
                                headerLen = headerLen2;
                                bssidsLen = bssidsLen2;
                                for (int k = bssidStart; k < arrWords.length; k++) {
                                    datas[i - 1][k - bssidStart] = Integer.parseInt(arrWords[k]);
                                }
                            }
                        } catch (NumberFormatException e4) {
                            e2 = e4;
                            headerLen = headerLen2;
                            bssidsLen = bssidsLen2;
                            Object[] objArr32 = new Object[1];
                            objArr32[0] = e2.getMessage();
                            LogUtil.e(false, "LocatingState,e %{public}s", objArr32);
                            i++;
                            str2 = str;
                            headerLen2 = headerLen;
                            bssidsLen2 = bssidsLen;
                        }
                    } catch (NumberFormatException e5) {
                        e2 = e5;
                        headerLen = headerLen2;
                        str = str2;
                        bssidsLen = bssidsLen2;
                        Object[] objArr322 = new Object[1];
                        objArr322[0] = e2.getMessage();
                        LogUtil.e(false, "LocatingState,e %{public}s", objArr322);
                        i++;
                        str2 = str;
                        headerLen2 = headerLen;
                        bssidsLen2 = bssidsLen;
                    }
                    i++;
                    str2 = str;
                    headerLen2 = headerLen;
                    bssidsLen2 = bssidsLen;
                }
                if (datas.length == 0) {
                    LogUtil.d(false, " loadModel failure:datas.length = 0", new Object[0]);
                    return null;
                }
                Object[] objArr4 = new Object[3];
                objArr4[0] = place;
                objArr4[1] = modelName;
                objArr4[2] = Integer.valueOf(datas.length);
                LogUtil.d(false, " loadModel,place :%{public}s,modelName:%{public}s,datas.size : %{public}d", objArr4);
                modelInfo.setDatas(datas);
                modelInfo.setDataLen(lineLen - 1);
                z = false;
                c = 1;
                Object[] objArr5 = new Object[2];
                char c2 = z ? 1 : 0;
                char c3 = z ? 1 : 0;
                objArr5[c2] = place;
                objArr5[c] = modelName;
                LogUtil.d(z, " loadModel success, place :%{public}s,modelName:%{public}s", objArr5);
                return modelInfo;
            } catch (RuntimeException e6) {
                e = e6;
                z = false;
            } catch (Exception e7) {
            }
        }
        c = 1;
        Object[] objArr6 = new Object[1];
        String message = e.getMessage();
        char c4 = z ? 1 : 0;
        char c5 = z ? 1 : 0;
        char c6 = z ? 1 : 0;
        char c7 = z ? 1 : 0;
        char c8 = z ? 1 : 0;
        char c9 = z ? 1 : 0;
        char c10 = z ? 1 : 0;
        objArr6[c4] = message;
        LogUtil.e(z, "LocatingState,e %{public}s", objArr6);
        Object[] objArr52 = new Object[2];
        char c22 = z ? 1 : 0;
        char c32 = z ? 1 : 0;
        objArr52[c22] = place;
        objArr52[c] = modelName;
        LogUtil.d(z, " loadModel success, place :%{public}s,modelName:%{public}s", objArr52);
        return modelInfo;
    }

    private void processBssids(HashSet<String> setBssids, String[] bssids, String[] headers, int bssidStart) {
        for (int i = bssidStart; i < headers.length; i++) {
            try {
                setBssids.add(headers[i]);
                bssids[i - bssidStart] = headers[i];
            } catch (RuntimeException e) {
                LogUtil.e(false, "loadModel,e %{public}s", e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(false, "loadModel failed by Exception", new Object[0]);
            }
        }
    }

    public boolean saveModel(String place, String[] reLst, ParameterInfo param, RegularPlaceInfo placeInfo) {
        StringBuilder builder = new StringBuilder(16);
        if (place == null) {
            LogUtil.d(false, " saveModel place == null", new Object[0]);
            return false;
        } else if (reLst == null) {
            LogUtil.d(false, " saveModel reLst == null", new Object[0]);
            return false;
        } else if (reLst.length == 0) {
            LogUtil.d(false, " saveModel reLst.length == 0", new Object[0]);
            return false;
        } else {
            String fileName = "";
            try {
                String str = place.replace(":", "").replace("-", "") + DOT + placeInfo.getModelName();
                String filePath = getModelFilePath(placeInfo, param);
                if (!FileUtils.delFile(filePath)) {
                    LogUtil.i(false, " saveModel ,FileUtils.delFile(filePath),filePath:%{public}s", filePath);
                }
                placeInfo.setModelName(new TimeUtil().getTimePattern02());
                fileName = place.replace(":", "").replace("-", "") + DOT + placeInfo.getModelName();
                LogUtil.i(false, " saveModel begin:%{public}s", fileName);
                String filePath2 = getModelFilePath(placeInfo, param);
                for (String line : reLst) {
                    builder.append(line);
                    builder.append(Constant.getLineSeparator());
                }
                if (!FileUtils.saveFile(filePath2, builder.toString())) {
                    LogUtil.d(false, "Failure save model %{public}s", place);
                    return false;
                }
            } catch (RuntimeException e) {
                LogUtil.e(false, "LocatingState,e %{public}s", e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(false, "saveModel failed by Exception", new Object[0]);
                return false;
            }
            LogUtil.d(false, "Success save model %{public}s", fileName);
            return true;
        }
    }

    public CoreTrainData getWmpCoreTrainData(String place, ParameterInfo param, RegularPlaceInfo placeInfo, BuildModelChrInfo buildModelChrInfo) {
        CoreTrainData coreTrainData = new CoreTrainData();
        if (place == null || "".equals(place)) {
            return coreTrainData;
        }
        LogUtil.i(false, " trainModelRe begin,place:%{public}s,isMain:%{public}s", place, String.valueOf(param.isMainAp()));
        try {
            int splitRet = splitTrainTestFiles(place, param, buildModelChrInfo);
            if (splitRet < 0) {
                LogUtil.d(false, "splitTrainTestFiles failure.", new Object[0]);
                coreTrainData.setResult(splitRet);
                return coreTrainData;
            }
            int result = transformRawData(place, param, buildModelChrInfo);
            coreTrainData.setResult(result);
            if (result < 0) {
                LogUtil.d(false, "getWmpCoreTrainData transformRawData failure", new Object[0]);
                LogUtil.d(false, " getWmpCoreTrainData end,place:%{public}s,result=%{public}d,isMain:%{public}s", place, Integer.valueOf(coreTrainData.getResult()), String.valueOf(param.isMainAp()));
                return coreTrainData;
            }
            if (getWmpCoreTrainDataByStdFile(place, param, coreTrainData)) {
                return coreTrainData;
            }
            LogUtil.d(false, " getWmpCoreTrainData end,place:%{public}s,result=%{public}d,isMain:%{public}s", place, Integer.valueOf(coreTrainData.getResult()), String.valueOf(param.isMainAp()));
            return coreTrainData;
        } catch (RuntimeException e) {
            LogUtil.e(false, "getWmpCoreTrainData,e %{public}s", e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(false, "getWmpCoreTrainData failed by Exception", new Object[0]);
        }
    }

    public boolean getWmpCoreTrainDataByStdFile(String place, ParameterInfo param, CoreTrainData coreTrainData) {
        String fileContent = FileUtils.getFileContent(getStdFilePath(place, param));
        if (fileContent.length() == 0) {
            coreTrainData.setResult(-15);
            return true;
        } else if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
            LogUtil.d(false, "getWmpCoreTrainDataByStdFile ,file content is too bigger than max_file_size.", new Object[0]);
            coreTrainData.setResult(-16);
            return true;
        } else {
            String[] fingerDatas = fileContent.split(Constant.getLineSeparator());
            LogUtil.d(false, " getWmpCoreTrainDataByStdFile fingerDatas.length:%{public}d,isMain:%{public}s", Integer.valueOf(fingerDatas.length), String.valueOf(param.isMainAp()));
            if (fingerDatas.length < param.getTrainDatasSize()) {
                LogUtil.d(false, " getWmpCoreTrainDataByStdFile fingerDatas.load train file file line length less than MIN VAL.%{public}d,isMain:%{public}s", Integer.valueOf(fingerDatas.length), String.valueOf(param.isMainAp()));
                coreTrainData.setResult(-17);
                return true;
            }
            String[] lineParams = param.toLineStr();
            LogUtil.i(false, "getWmpCoreTrainDataByStdFile:%{public}s,fingerDatas.size:%{public}d,isMain:%{public}s", place, Integer.valueOf(fingerDatas.length), String.valueOf(param.isMainAp()));
            try {
                coreTrainData.setDatas(HwHidataJniAdapter.getInstance().getWmpCoreTrainData(fingerDatas, lineParams).split(AwarenessInnerConstants.SEMI_COLON_KEY));
            } catch (RuntimeException e) {
                LogUtil.e(false, "getWmpCoreTrainDataByStdFile,e %{public}s", e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(false, "failed by Exception isMain:%{public}s", String.valueOf(param.isMainAp()));
            }
            return false;
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0262: APUT  
      (r9v4 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x025d: INVOKE  (r7v7 java.lang.Integer) = 
      (wrap: int : 0x025b: ARITH  (r7v6 int) = (r8v2 'totalRdCnt' int A[D('totalRdCnt' int)]) - (r4v11 'testDataCount' int A[D('testDataCount' int)]))
     type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x029c: APUT  
      (r13v6 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (r31v0 'place' java.lang.String A[D('place' java.lang.String)])
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x02a8, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x02a9, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x02df, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x02e0, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x02e6, code lost:
        com.android.server.hidata.wavemapping.util.LogUtil.e(false, "splitTrainTestFiles failed by Exception", new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x02f1, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x02f2, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:?, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0240, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0241, code lost:
        r2 = false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x02e5 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:1:0x0004] */
    private int splitTrainTestFiles(String place, ParameterInfo param, BuildModelChrInfo buildModelChrInfo) {
        int testDataCnt;
        SecureRandom secureRandom;
        int begin;
        TimeUtil timeUtil;
        try {
            String randomKey = getRawFilePath(place, param);
            String fileContent = FileUtils.getFileContent(randomKey);
            if (fileContent.length() == 0) {
                return -7;
            }
            if (fileContent.equals(FileUtils.ERROR_RET)) {
                return -20;
            }
            if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
                LogUtil.d(false, "splitTrainTestFiles ,raw data file content is too bigger than max_file_size.file lenght:%{public}d", Integer.valueOf(fileContent.length()));
                return -8;
            }
            String[] lines = fileContent.split(Constant.getLineSeparator());
            int len = lines.length;
            if (len < param.getTestDataSize() + param.getTrainDatasSize()) {
                LogUtil.d(false, "splitTrainTestFiles ,raw data file lines length(%{public}d) is less than train(%{public}d) and test(%{public}d) data size.", Integer.valueOf(len), Integer.valueOf(param.getTrainDatasSize()), Integer.valueOf(param.getTestDataSize()));
                return -9;
            }
            HashMap<String, AtomicInteger> hpBatchStat = new HashMap<>(16);
            List<String> batchList = new ArrayList<>(10);
            int begin2 = len > param.getMaxRawDataCount() ? len - param.getMaxRawDataCount() : 0;
            for (int i = begin2; i < len; i++) {
                String[] wds = lines[i].split(",");
                if (wds.length >= param.getScanWifiStart() && wds[0] != null && !wds[0].equals("")) {
                    String strBatch = wds[param.getBatchId()];
                    if (hpBatchStat.containsKey(strBatch)) {
                        hpBatchStat.get(strBatch).incrementAndGet();
                    } else {
                        batchList.add(strBatch);
                        hpBatchStat.put(strBatch, new AtomicInteger(1));
                    }
                }
            }
            int totalRdCnt = lines.length;
            int testDataCntbyRatio = (int) (((float) totalRdCnt) * param.getTestDataRatio());
            if (testDataCntbyRatio > param.getTestDataSize()) {
                testDataCnt = testDataCntbyRatio;
            } else {
                testDataCnt = param.getTestDataSize();
            }
            if (totalRdCnt <= testDataCnt) {
                LogUtil.d(false, "splitTrainTestFiles ,total data size is less than test data size.", new Object[0]);
                return -10;
            }
            SecureRandom secureRandom2 = new SecureRandom();
            Set<String> testBatchs = new HashSet<>(16);
            int size = hpBatchStat.size();
            int testFetchDataCnt = 0;
            int i2 = 0;
            while (true) {
                if (i2 >= size) {
                    secureRandom = secureRandom2;
                    break;
                } else if (batchList.isEmpty()) {
                    secureRandom = secureRandom2;
                    break;
                } else if (testFetchDataCnt >= testDataCnt) {
                    secureRandom = secureRandom2;
                    break;
                } else {
                    size = size;
                    String randomKey2 = batchList.get(secureRandom2.nextInt(batchList.size()));
                    AtomicInteger randomValue = hpBatchStat.get(randomKey2);
                    if (randomValue != null) {
                        testBatchs.add(randomKey2);
                        testFetchDataCnt += randomValue.intValue();
                        batchList.remove(randomKey2);
                    }
                    i2++;
                    secureRandom2 = secureRandom2;
                    randomKey = randomKey;
                    fileContent = fileContent;
                }
            }
            StringBuffer trainDataSb = new StringBuffer(16);
            StringBuffer testDataSb = new StringBuffer(16);
            TimeUtil timeUtil2 = new TimeUtil();
            int i3 = lines.length > param.getMaxRawDataCount() ? lines.length - param.getMaxRawDataCount() : 0;
            int testDataCount = 0;
            boolean isGetFirstDataTime = false;
            while (i3 < totalRdCnt) {
                String[] wds2 = lines[i3].split(",");
                if (wds2.length < param.getScanWifiStart()) {
                    begin = begin2;
                    timeUtil = timeUtil2;
                } else if (wds2[0] == null) {
                    begin = begin2;
                    timeUtil = timeUtil2;
                } else if (wds2[0].equals("")) {
                    begin = begin2;
                    timeUtil = timeUtil2;
                } else {
                    String strBatch2 = wds2[param.getBatchId()];
                    if (!isGetFirstDataTime) {
                        begin = begin2;
                        if (wds2.length > param.getTimestampId()) {
                            timeUtil = timeUtil2;
                            if (timeUtil.time2IntDate(wds2[param.getTimestampId()]) != 0) {
                                buildModelChrInfo.setFirstTimeAll(timeUtil.time2IntDate(wds2[param.getTimestampId()]));
                                isGetFirstDataTime = true;
                            }
                        } else {
                            timeUtil = timeUtil2;
                        }
                    } else {
                        begin = begin2;
                        timeUtil = timeUtil2;
                    }
                    if (testBatchs.contains(strBatch2)) {
                        testDataSb.append(lines[i3]);
                        testDataSb.append(Constant.getLineSeparator());
                        testDataCount++;
                    } else {
                        trainDataSb.append(lines[i3]);
                        trainDataSb.append(Constant.getLineSeparator());
                    }
                }
                i3++;
                timeUtil2 = timeUtil;
                hpBatchStat = hpBatchStat;
                testFetchDataCnt = testFetchDataCnt;
                batchList = batchList;
                begin2 = begin;
            }
            buildModelChrInfo.setTestDataAll(testDataCount);
            buildModelChrInfo.setTrainDataAll(totalRdCnt - testDataCount);
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(totalRdCnt - testDataCount);
            objArr[1] = Integer.valueOf(testDataCount);
            LogUtil.d(false, "chr trainData len:%{public}d,testData len:%{public}d", objArr);
            String trainDataFilePath = getTrainDataFilePath(place, param);
            String testDataFilePath = getTestDataFilePath(place, param);
            if (!FileUtils.delFile(trainDataFilePath)) {
                LogUtil.d(false, " splitTrainTestFiles failure ,FileUtils.delFile(trainDataFilePath),dataFilePath:%{public}s", trainDataFilePath);
                return -11;
            } else if (!FileUtils.saveFile(trainDataFilePath, trainDataSb.toString())) {
                String str = TAG;
                Object[] objArr2 = new Object[2];
                objArr2[0] = place;
                objArr2[1] = trainDataFilePath;
                HwHiLog.d(str, false, " splitTrainTestFiles save failure:%{public}s,trainDataFilePath:%{public}s", objArr2);
                return -12;
            } else if (!FileUtils.delFile(testDataFilePath)) {
                LogUtil.d(false, " splitTrainTestFiles failure ,FileUtils.delFile(testDataFilePath),testDataFilePath:%{public}s", testDataFilePath);
                return -13;
            } else if (FileUtils.saveFile(testDataFilePath, testDataSb.toString())) {
                return 1;
            } else {
                HwHiLog.d(TAG, false, " splitTrainTestFiles save failure:%{private}s,testDataFilePath:%{public}s", new Object[]{place, testDataFilePath});
                return -14;
            }
        } catch (RuntimeException e) {
            RuntimeException e2 = e;
            boolean z = false;
            Object[] objArr3 = new Object[1];
            String message = e2.getMessage();
            char c = z ? 1 : 0;
            char c2 = z ? 1 : 0;
            char c3 = z ? 1 : 0;
            char c4 = z ? 1 : 0;
            char c5 = z ? 1 : 0;
            char c6 = z ? 1 : 0;
            objArr3[c] = message;
            LogUtil.e(z, "splitTrainTestFiles,e %{public}s", objArr3);
            return 1;
        } catch (Exception e3) {
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v1, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v2 */
    /* JADX WARN: Type inference failed for: r3v13 */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00e4: APUT  
      (r4v9 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x00dd: INVOKE  (r7v11 java.lang.Integer) = (r10v2 'macCnt' int A[D('macCnt' int)]) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0137: APUT  
      (r2v19 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x0132: INVOKE  (r7v13 java.lang.String) = 
      (wrap: long : 0x012a: ARITH  (r4v16 'runTime2' long) = (wrap: long : 0x0129: ARITH  (r4v15 long) = (wrap: long : 0x0125: INVOKE  (r4v14 long) =  type: STATIC call: java.lang.System.currentTimeMillis():long) - (r11v0 'startTime' long A[D('startTime' long)])) / (1000 long))
     type: STATIC call: java.lang.String.valueOf(long):java.lang.String)
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0104, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0105, code lost:
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0123, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0143, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x014e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0151, code lost:
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x015f, code lost:
        r4 = false;
        com.android.server.hidata.wavemapping.util.LogUtil.e(false, "failed by Exception,isMain:%{public}s", java.lang.String.valueOf(r27.isMainAp()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0173, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0174, code lost:
        r4 = false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0141 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:47:0x0111] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x014a A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:34:0x00db] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x015a A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:22:0x0081] */
    public int wmpCoreTrainData(CoreTrainData coreTrainData, String place, ParameterInfo param, RegularPlaceInfo placeInfo, BuildModelChrInfo buildModelChrInfo) {
        int i;
        boolean z;
        boolean z2;
        RuntimeException e;
        if (coreTrainData == null) {
            HwHiLog.d(TAG, false, "wmpCoreTrainData coreTrainData == null", new Object[0]);
            return -52;
        }
        if (coreTrainData.getDatas() == null) {
            i = 0;
        } else if (coreTrainData.getDatas().length == 0) {
            i = 0;
        } else if (param == null) {
            HwHiLog.d(TAG, false, "wmpCoreTrainData parameterInfo == null", new Object[0]);
            return -1;
        } else if (placeInfo == null) {
            HwHiLog.d(TAG, false, "wmpCoreTrainData placeInfo == null", new Object[0]);
            return -1;
        } else {
            if (place == null) {
                z = false;
            } else if ("".equals(place)) {
                z = false;
            } else {
                String[] lineParams = param.toLineStr();
                LogUtil.i(false, "wmpCoreTrainData:%{public}s,coreTrainData.size:%{public}d,isMain:%{public}s", place, Integer.valueOf(coreTrainData.getDatas().length), String.valueOf(param.isMainAp()));
                Set<String> setPreLabels = new HashSet<>(16);
                try {
                    long startTime = System.currentTimeMillis();
                    String[] trainDatas = HwHidataJniAdapter.getInstance().wmpCoreTrainData(coreTrainData.getDatas(), lineParams).split(AwarenessInnerConstants.SEMI_COLON_KEY);
                    int size = trainDatas.length;
                    LogUtil.i(false, "wmpCoreTrainData, size=%{public}d,spend(seconds) run time(s):%{public}s", Integer.valueOf(size), String.valueOf((System.currentTimeMillis() - startTime) / 1000));
                    if (size < 2) {
                        return -17;
                    }
                    int macCnt = -1;
                    for (int i2 = param.getBssidStart(); i2 < trainDatas[0].split(",").length; i2++) {
                        macCnt++;
                    }
                    try {
                        Object[] objArr = new Object[1];
                        objArr[0] = Integer.valueOf(macCnt);
                        LogUtil.i(false, "macCnt=%{public}d", objArr);
                        buildModelChrInfo.getApType().setFinalUsed(macCnt);
                        for (String str : trainDatas) {
                            String[] datas = str.split(",");
                            setPreLabels.add(datas[datas.length - 1]);
                        }
                        setPreLabels.remove("prelabel");
                    } catch (RuntimeException e2) {
                        e = e2;
                        z2 = false;
                        Object[] objArr2 = new Object[1];
                        String message = e.getMessage();
                        char c = z2 ? 1 : 0;
                        char c2 = z2 ? 1 : 0;
                        char c3 = z2 ? 1 : 0;
                        char c4 = z2 ? 1 : 0;
                        char c5 = z2 ? 1 : 0;
                        char c6 = z2 ? 1 : 0;
                        char c7 = z2 ? 1 : 0;
                        char c8 = z2 ? 1 : 0;
                        objArr2[c] = message;
                        LogUtil.e(z2, "wmpCoreTrainData,e %{public}s", objArr2);
                        Object[] objArr3 = new Object[4];
                        char c9 = z2 ? 1 : 0;
                        char c10 = z2 ? 1 : 0;
                        char c11 = z2 ? 1 : 0;
                        objArr3[c9] = place;
                        objArr3[1] = Integer.valueOf(coreTrainData.getDatas().length);
                        objArr3[2] = Integer.valueOf(setPreLabels.size());
                        objArr3[3] = String.valueOf(param.isMainAp());
                        LogUtil.d(false, "wMappingTrainData end:%{public}s,coreTrainData.size:%{public}d,setPreLabels.size():%{public}d,isMain:%{public}s", objArr3);
                        return setPreLabels.size();
                    } catch (Exception e3) {
                    }
                    try {
                        if (!saveModel(place, trainDatas, param, placeInfo)) {
                            z2 = false;
                            LogUtil.i(false, "wmpCoreTrainData save model failure.", new Object[0]);
                            return -18;
                        }
                        Object[] objArr4 = new Object[1];
                        objArr4[0] = String.valueOf((System.currentTimeMillis() - startTime) / 1000);
                        LogUtil.d(false, "train models wmpCoreTrainData spend(seconds) : %{public}s", objArr4);
                        z2 = false;
                        Object[] objArr32 = new Object[4];
                        char c92 = z2 ? 1 : 0;
                        char c102 = z2 ? 1 : 0;
                        char c112 = z2 ? 1 : 0;
                        objArr32[c92] = place;
                        objArr32[1] = Integer.valueOf(coreTrainData.getDatas().length);
                        objArr32[2] = Integer.valueOf(setPreLabels.size());
                        objArr32[3] = String.valueOf(param.isMainAp());
                        LogUtil.d(false, "wMappingTrainData end:%{public}s,coreTrainData.size:%{public}d,setPreLabels.size():%{public}d,isMain:%{public}s", objArr32);
                        return setPreLabels.size();
                    } catch (RuntimeException e4) {
                        e = e4;
                        z2 = false;
                        Object[] objArr22 = new Object[1];
                        String message2 = e.getMessage();
                        char c12 = z2 ? 1 : 0;
                        char c22 = z2 ? 1 : 0;
                        char c32 = z2 ? 1 : 0;
                        char c42 = z2 ? 1 : 0;
                        char c52 = z2 ? 1 : 0;
                        char c62 = z2 ? 1 : 0;
                        char c72 = z2 ? 1 : 0;
                        char c82 = z2 ? 1 : 0;
                        objArr22[c12] = message2;
                        LogUtil.e(z2, "wmpCoreTrainData,e %{public}s", objArr22);
                        Object[] objArr322 = new Object[4];
                        char c922 = z2 ? 1 : 0;
                        char c1022 = z2 ? 1 : 0;
                        char c1122 = z2 ? 1 : 0;
                        objArr322[c922] = place;
                        objArr322[1] = Integer.valueOf(coreTrainData.getDatas().length);
                        objArr322[2] = Integer.valueOf(setPreLabels.size());
                        objArr322[3] = String.valueOf(param.isMainAp());
                        LogUtil.d(false, "wMappingTrainData end:%{public}s,coreTrainData.size:%{public}d,setPreLabels.size():%{public}d,isMain:%{public}s", objArr322);
                        return setPreLabels.size();
                    } catch (Exception e5) {
                    }
                } catch (RuntimeException e6) {
                    e = e6;
                    z2 = false;
                    Object[] objArr222 = new Object[1];
                    String message22 = e.getMessage();
                    char c122 = z2 ? 1 : 0;
                    char c222 = z2 ? 1 : 0;
                    char c322 = z2 ? 1 : 0;
                    char c422 = z2 ? 1 : 0;
                    char c522 = z2 ? 1 : 0;
                    char c622 = z2 ? 1 : 0;
                    char c722 = z2 ? 1 : 0;
                    char c822 = z2 ? 1 : 0;
                    objArr222[c122] = message22;
                    LogUtil.e(z2, "wmpCoreTrainData,e %{public}s", objArr222);
                    Object[] objArr3222 = new Object[4];
                    char c9222 = z2 ? 1 : 0;
                    char c10222 = z2 ? 1 : 0;
                    char c11222 = z2 ? 1 : 0;
                    objArr3222[c9222] = place;
                    objArr3222[1] = Integer.valueOf(coreTrainData.getDatas().length);
                    objArr3222[2] = Integer.valueOf(setPreLabels.size());
                    objArr3222[3] = String.valueOf(param.isMainAp());
                    LogUtil.d(false, "wMappingTrainData end:%{public}s,coreTrainData.size:%{public}d,setPreLabels.size():%{public}d,isMain:%{public}s", objArr3222);
                    return setPreLabels.size();
                } catch (Exception e7) {
                }
            }
            String str2 = TAG;
            Object[] objArr5 = new Object[1];
            char c13 = z ? 1 : 0;
            char c14 = z ? 1 : 0;
            char c15 = z ? 1 : 0;
            objArr5[c13] = place;
            HwHiLog.d(str2, z, "wmpCoreTrainData place == null || place.equals(\"\") .%{public}s", objArr5);
            return -1;
        }
        HwHiLog.d(TAG, i, "wmpCoreTrainData coreTrainData == null", new Object[i]);
        return -52;
    }

    public String filterMobileAp(String place, ParameterInfo param) {
        String filePath = getRawFilePath(place, param);
        HwHiLog.d(TAG, false, " filterMobileAp begin:%{public}s,filePath:%{public}s", new Object[]{place, filePath});
        ApChrStatInfo apChrStatInfo = new ApChrStatInfo();
        try {
            String fileContent = FileUtils.getFileContent(filePath);
            if ("".equals(fileContent)) {
                LogUtil.d(false, "filterMobileAp ,fileContent == null.", new Object[0]);
                apChrStatInfo.setResult(-57);
                return apChrStatInfo.toString();
            } else if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
                LogUtil.d(false, "filterMobileAp ,file content is too bigger than max_file_size.", new Object[0]);
                apChrStatInfo.setResult(-58);
                return apChrStatInfo.toString();
            } else {
                String[] lines = fileContent.split(Constant.getLineSeparator());
                if (lines.length < 2) {
                    LogUtil.d(false, " filterMobileAp read data,lines == null || lines.length < 2", new Object[0]);
                    apChrStatInfo.setResult(-59);
                    return apChrStatInfo.toString();
                }
                List<String> macs = new ArrayList<>(10);
                filterMacs(apChrStatInfo, macs, lines, param);
                if (macs.size() > param.getMaxBssidNum()) {
                    apChrStatInfo.setResult(-60);
                    return apChrStatInfo.toString();
                }
                StdDataSet stdDataSet = getFilterMobileAps2StdDataSet(place, param, lines, macs);
                apChrStatInfo.setMobileApSrc2(stdDataSet.getFilter2MobileApCnt());
                apChrStatInfo.setTotalFound(stdDataSet.getValidMacCnt());
                return apChrStatInfo.toString();
            }
        } catch (RuntimeException e) {
            LogUtil.e(false, "filterMobileAp, %{public}s,isFilterMobileAp", e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(false, "filterMobileAp failed by Exception", new Object[0]);
        }
    }

    private void filterMacs(ApChrStatInfo apChrStatInfo, List<String> macs, String[] lines, ParameterInfo param) {
        RuntimeException e;
        Exception e2;
        TrainModelService trainModelService = this;
        Set<String> tempSetMacs = new HashSet<>(16);
        String tempMac = "";
        int size = lines.length;
        Set<String> apInfoSet = trainModelService.enterpriseApDao.findAll();
        TMapSet<String, String> tmap = new TMapSet<>();
        char c = 0;
        int i = size > param.getMaxRawDataCount() ? size - param.getMaxRawDataCount() : 0;
        while (i < size) {
            String[] wds = lines[i].split(",");
            if (wds.length >= param.getScanWifiStart() && wds[c] != null && !"".equals(wds[c])) {
                int tempSize = wds.length;
                for (int k = param.getScanWifiStart(); k < tempSize; k++) {
                    String[] tempScanWifiInfos = wds[k].split(param.getWifiSeperator());
                    if (tempScanWifiInfos.length >= 4) {
                        tempMac = tempScanWifiInfos[param.getScanMac()];
                        tempSetMacs.add(tempMac);
                        String tempSsid = tempScanWifiInfos[param.getScanSsid()];
                        if (!apInfoSet.contains(tempSsid)) {
                            tmap.add((TMapSet<String, String>) tempSsid, tempMac);
                        }
                    }
                }
            }
            i++;
            c = 0;
        }
        LogUtil.i(false, "all macs:%{private}s", tempSetMacs.toString());
        if (!param.isMainAp() && !param.isTest01()) {
            apChrStatInfo.setMobileApSrc1(trainModelService.filterMobileAps1(param, tempSetMacs, tmap));
        }
        LogUtil.i(false, " filterMobileAp tempSetMacs.length:%{public}d", Integer.valueOf(tempSetMacs.size()));
        for (String tempMac2 : tempSetMacs) {
            try {
                if (trainModelService.checkMacFormat(tempMac2)) {
                    try {
                        macs.add(tempMac2);
                    } catch (RuntimeException e3) {
                        e = e3;
                    } catch (Exception e4) {
                        e2 = e4;
                        LogUtil.e(false, "updateModel macs add failed by Exception", new Object[0]);
                    }
                }
            } catch (RuntimeException e5) {
                e = e5;
                LogUtil.e(false, "updateModel:%{public}s", e.getMessage());
                trainModelService = this;
            } catch (Exception e6) {
                e2 = e6;
                LogUtil.e(false, "updateModel macs add failed by Exception", new Object[0]);
            }
        }
        LogUtil.d(false, " filterMobileAp setMacs.length:%{public}d,isFilterMobileAp", Integer.valueOf(macs.size()));
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:179:0x01ea */
    /* JADX DEBUG: Multi-variable search result rejected for r0v59, resolved type: java.lang.Object[] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r10v6 */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x01c8: APUT  
      (r5v4 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x01c3: INVOKE  (r1v7 java.lang.String) = (r0v11 'rawApSetMacs' java.util.Set<java.lang.String> A[D('rawApSetMacs' java.util.Set<java.lang.String>)]) type: VIRTUAL call: java.lang.Object.toString():java.lang.String)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x027d: APUT  
      (r3v16 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x0276: INVOKE  (r5v10 java.lang.String) = (r0v29 'e' java.lang.RuntimeException A[D('e' java.lang.RuntimeException)]) type: VIRTUAL call: java.lang.RuntimeException.getMessage():java.lang.String)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x02e1: APUT  
      (r5v6 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x02dc: INVOKE  (r4v9 java.lang.Integer) = (wrap: int : 0x02db: ARRAY_LENGTH  (r4v8 int) = (r0v9 'lines' java.lang.String[] A[D('lines' java.lang.String[])])) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x0269, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x026a, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0303, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x0304, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x0306, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x0307, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x0309, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x030a, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x030e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x030f, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x0311, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x0312, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x0319, code lost:
        com.android.server.hidata.wavemapping.util.LogUtil.e(false, "transformRawData failed by Exception", new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:188:?, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0095, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0096, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0208, code lost:
        if (r3.contains(r0) != false) goto L_0x020b;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:5:0x002a, B:24:0x008e] */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x030c A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:48:0x011b] */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x0316 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:1:0x001c] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0032 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:5:0x002a] */
    public int transformRawData(String place, ParameterInfo param, BuildModelChrInfo buildModelChrInfo) {
        boolean z;
        RuntimeException e;
        StdDataSet stdDataSet;
        RuntimeException e2;
        Exception e3;
        String str = "";
        String filePath = getTrainDataFilePath(place, param);
        HwHiLog.d(TAG, false, " transformRawData begin:%{public}s,filePath:%{public}s", new Object[]{place, filePath});
        try {
            String fileContent = FileUtils.getFileContent(filePath);
            if (str.equals(fileContent)) {
                try {
                    LogUtil.d(false, "transformRawData ,fileContent == null.", new Object[0]);
                    return -52;
                } catch (RuntimeException e4) {
                    e = e4;
                    z = false;
                    Object[] objArr = new Object[1];
                    String message = e.getMessage();
                    char c = z ? 1 : 0;
                    char c2 = z ? 1 : 0;
                    char c3 = z ? 1 : 0;
                    char c4 = z ? 1 : 0;
                    char c5 = z ? 1 : 0;
                    char c6 = z ? 1 : 0;
                    char c7 = z ? 1 : 0;
                    char c8 = z ? 1 : 0;
                    char c9 = z ? 1 : 0;
                    char c10 = z ? 1 : 0;
                    char c11 = z ? 1 : 0;
                    objArr[c] = message;
                    LogUtil.e(z, "transformRawData, %{public}s", objArr);
                    return 1;
                } catch (Exception e5) {
                }
            } else if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
                LogUtil.d(false, "transformRawData ,file content is too bigger than max_file_size.", new Object[0]);
                return -53;
            } else {
                String[] lines = fileContent.split(Constant.getLineSeparator());
                if (lines.length < 2) {
                    LogUtil.d(false, " transformRawData read data,lines == null || lines.length < 2", new Object[0]);
                    return -54;
                }
                Set<String> rawApSetMacs = new HashSet<>(16);
                List<String> macs = new ArrayList<>(10);
                String tempMac = str;
                int size = lines.length;
                MobileApDao mobileApDao2 = new MobileApDao();
                List<ApInfo> mobileAps = new ArrayList<>(10);
                if (!param.isMainAp()) {
                    mobileAps = mobileApDao2.findAllAps();
                }
                List<String> mobileApStrs = new ArrayList<>(10);
                String tempSsid = "_";
                if (!param.isMainAp() && mobileAps != null && mobileAps.size() != 0) {
                    Iterator<ApInfo> it = mobileAps.iterator();
                    while (it.hasNext()) {
                        ApInfo mAp = it.next();
                        if (mAp.getMac() != null) {
                            if (mAp.getSsid() != null) {
                                mobileApStrs.add(mAp.getSsid() + tempSsid + mAp.getMac());
                                tempMac = tempMac;
                                it = it;
                            }
                        }
                    }
                }
                new TMapSet();
                Set<Integer> batchSet = new HashSet<>(16);
                int i = 0;
                while (i < size) {
                    try {
                        String[] wds = lines[i].split(",");
                        if (wds.length >= param.getScanWifiStart()) {
                            if (wds[0] != 0 && !wds[0].equals(str)) {
                                try {
                                    batchSet.add(Integer.valueOf(wds[0]));
                                    int tempSize = wds.length;
                                    int k = param.getScanWifiStart();
                                    while (k < tempSize) {
                                        String[] tempScanWifiInfos = wds[k].split(param.getWifiSeperator());
                                        if (tempScanWifiInfos.length >= 4) {
                                            String tempMac2 = tempScanWifiInfos[param.getScanMac()];
                                            rawApSetMacs.add(tempScanWifiInfos[param.getScanSsid()] + tempSsid + tempMac2);
                                        }
                                        k++;
                                        tempSize = tempSize;
                                        str = str;
                                        wds = wds;
                                    }
                                } catch (NumberFormatException e6) {
                                }
                            }
                        }
                        i++;
                        filePath = filePath;
                        mobileAps = mobileAps;
                        str = str;
                    } catch (RuntimeException e7) {
                        e = e7;
                        z = false;
                    } catch (Exception e8) {
                    }
                }
                Object[] objArr2 = new Object[1];
                objArr2[0] = rawApSetMacs.toString();
                LogUtil.i(false, "all macs:%{private}s", objArr2);
                ApChrStatInfo apChrStatInfo = buildModelChrInfo.getApType();
                LogUtil.i(false, " transformRawData tempSetMacs.length:%{public}d", Integer.valueOf(rawApSetMacs.size()));
                Iterator iterator = rawApSetMacs.iterator();
                while (true) {
                    if (!iterator.hasNext()) {
                        break;
                    }
                    try {
                        String tempSsidMac = iterator.next();
                        try {
                            if (tempSsidMac.contains(tempSsid)) {
                                if (!param.isMainAp()) {
                                    try {
                                    } catch (RuntimeException e9) {
                                        e2 = e9;
                                        Object[] objArr3 = new Object[1];
                                        objArr3[0] = e2.getMessage();
                                        LogUtil.e(false, "updateModel:%{public}s", objArr3);
                                        mobileApStrs = mobileApStrs;
                                        tempSsid = tempSsid;
                                    } catch (Exception e10) {
                                        e3 = e10;
                                        LogUtil.e(false, "updateModel failed by Exception", new Object[0]);
                                    }
                                }
                                String tempMac3 = tempSsidMac.split(tempSsid)[1];
                                try {
                                    if (checkMacFormat(tempMac3)) {
                                        try {
                                            if (macs.size() >= param.getMaxBssidNum()) {
                                                break;
                                            }
                                            macs.add(tempMac3);
                                        } catch (RuntimeException e11) {
                                            e2 = e11;
                                            Object[] objArr32 = new Object[1];
                                            objArr32[0] = e2.getMessage();
                                            LogUtil.e(false, "updateModel:%{public}s", objArr32);
                                            mobileApStrs = mobileApStrs;
                                            tempSsid = tempSsid;
                                        } catch (Exception e12) {
                                            e3 = e12;
                                            LogUtil.e(false, "updateModel failed by Exception", new Object[0]);
                                        }
                                    }
                                } catch (RuntimeException e13) {
                                    e2 = e13;
                                    Object[] objArr322 = new Object[1];
                                    objArr322[0] = e2.getMessage();
                                    LogUtil.e(false, "updateModel:%{public}s", objArr322);
                                    mobileApStrs = mobileApStrs;
                                    tempSsid = tempSsid;
                                } catch (Exception e14) {
                                    e3 = e14;
                                    LogUtil.e(false, "updateModel failed by Exception", new Object[0]);
                                }
                            }
                        } catch (RuntimeException e15) {
                            e2 = e15;
                            Object[] objArr3222 = new Object[1];
                            objArr3222[0] = e2.getMessage();
                            LogUtil.e(false, "updateModel:%{public}s", objArr3222);
                            mobileApStrs = mobileApStrs;
                            tempSsid = tempSsid;
                        } catch (Exception e16) {
                            e3 = e16;
                            LogUtil.e(false, "updateModel failed by Exception", new Object[0]);
                        }
                    } catch (RuntimeException e17) {
                        e2 = e17;
                        Object[] objArr32222 = new Object[1];
                        objArr32222[0] = e2.getMessage();
                        LogUtil.e(false, "updateModel:%{public}s", objArr32222);
                        mobileApStrs = mobileApStrs;
                        tempSsid = tempSsid;
                    } catch (Exception e18) {
                        e3 = e18;
                        LogUtil.e(false, "updateModel failed by Exception", new Object[0]);
                    }
                }
                LogUtil.d(false, " transformRawData setMacs.length:%{public}d", Integer.valueOf(macs.size()));
                if (macs.size() == 0) {
                    return -56;
                }
                int minBatch = getMinBatch(param, batchSet);
                if (param.isMainAp()) {
                    stdDataSet = getMainApStdDataSet(param, lines, macs, minBatch);
                } else {
                    stdDataSet = getCommStdDataSet(place, param, lines, macs, minBatch);
                }
                apChrStatInfo.setUpdate(new TimeUtil().getTimeIntPattern02());
                Object[] objArr4 = new Object[1];
                objArr4[0] = Integer.valueOf(lines.length);
                LogUtil.d(false, " transformRawData datas.length:%{public}d", objArr4);
                if (!saveStdDataSetToFile(stdDataSet, place, param)) {
                    LogUtil.d(false, " saveStdDataSetToFile save failure:%{public}s", place);
                    return -55;
                }
                apChrStatInfo.setTotalFound(stdDataSet.getValidMacCnt());
                return 1;
            }
        } catch (RuntimeException e19) {
            e = e19;
            z = false;
            Object[] objArr5 = new Object[1];
            String message2 = e.getMessage();
            char c12 = z ? 1 : 0;
            char c22 = z ? 1 : 0;
            char c32 = z ? 1 : 0;
            char c42 = z ? 1 : 0;
            char c52 = z ? 1 : 0;
            char c62 = z ? 1 : 0;
            char c72 = z ? 1 : 0;
            char c82 = z ? 1 : 0;
            char c92 = z ? 1 : 0;
            char c102 = z ? 1 : 0;
            char c112 = z ? 1 : 0;
            objArr5[c12] = message2;
            LogUtil.e(z, "transformRawData, %{public}s", objArr5);
            return 1;
        } catch (Exception e20) {
        }
    }

    private int getMinBatch(ParameterInfo param, Set<Integer> batchSet) {
        if (param == null || batchSet == null || param.getMaxTrainBatchCount() <= 0 || batchSet.size() <= param.getMaxTrainBatchCount()) {
            return 0;
        }
        LinkedList<Integer> batchSetList = new LinkedList<>(batchSet);
        Collections.sort(batchSetList, Collections.reverseOrder());
        return batchSetList.get(param.getMaxTrainBatchCount() - 1).intValue();
    }

    private int filterMobileAps1(ParameterInfo param, Set<String> tempSetMacs, TMapSet<String, String> tmap) {
        int mobileApCnt = 0;
        if (tempSetMacs == null || tempSetMacs.size() == 0 || tmap == null || tmap.size() == 0) {
            return 0;
        }
        try {
            for (Map.Entry<String, Set<String>> entry : tmap.entrySet()) {
                String tempSsid = entry.getKey();
                Set<String> tempMacs = entry.getValue();
                if (tempSsid != null) {
                    if (!"".equals(tempSsid)) {
                        if (tempMacs.size() > param.getMobileApCheckLimit()) {
                            mobileApCnt += tempMacs.size();
                            if (addMobileAp(tempSsid, tempMacs.iterator(), 1).size() > 0) {
                                tempSetMacs.removeAll(tempMacs);
                                LogUtil.d(false, "filterMobileAps1 remove mobileAps:%{private}s", tempMacs.toString());
                            }
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            LogUtil.e(false, "filterMobileAps1, %{public}s", e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(false, "filterMobileAps1 failed by Exception", new Object[0]);
        }
        return mobileApCnt;
    }

    private List<String> addMobileAp(String ssid, Iterator<String> iterator, int srcType) {
        List<String> mobileMacs = new ArrayList<>(10);
        if (this.enterpriseApDao.findBySsid(ssid) == null) {
            while (iterator.hasNext()) {
                try {
                    String tempMac = iterator.next();
                    mobileMacs.add(tempMac);
                    ApInfo mobileAp = new ApInfo(ssid, tempMac, TimeUtil.getTime(), srcType);
                    if (!this.mobileApDao.insert(mobileAp)) {
                        LogUtil.d(false, "addMobileAp,add mobile ap failure,:%{private}s", mobileAp.toString());
                    }
                } catch (RuntimeException e) {
                    LogUtil.e(false, "addMobileAp, %{public}s", e.getMessage());
                } catch (Exception e2) {
                    LogUtil.e(false, "addMobileAp failed by Exception", new Object[0]);
                }
            }
        }
        return mobileMacs;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v1, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r8v0 */
    /* JADX WARN: Type inference failed for: r8v3 */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x01a2: APUT  
      (r14v2 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x019b: INVOKE  (r2v16 java.lang.String) = (r0v36 'e' java.lang.RuntimeException A[D('e' java.lang.RuntimeException)]) type: VIRTUAL call: java.lang.RuntimeException.getMessage():java.lang.String)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x01ec: APUT  
      (r2v9 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x01e7: INVOKE  (r1v11 java.lang.String) = (r0v14 'e' java.lang.RuntimeException A[D('e' java.lang.RuntimeException)]) type: VIRTUAL call: java.lang.RuntimeException.getMessage():java.lang.String)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0227: APUT  
      (r7v0 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x0222: INVOKE  (r6v4 java.lang.Integer) = (r5v3 'rdCount' int A[D('rdCount' int)]) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x01dd, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x01de, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x022d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x022e, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x008f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00a9, code lost:
        r0 = e;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x0230 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:102:0x01d7] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x008f A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:27:0x0066] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00a9 A[ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:27:0x0066] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00c7  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00f3 A[Catch:{ NumberFormatException -> 0x0141 }] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00f7 A[Catch:{ NumberFormatException -> 0x0141 }] */
    private StdDataSet getMainApStdDataSet(ParameterInfo param, String[] lines, List<String> macLst, int minBatch) {
        boolean z;
        RuntimeException e;
        HashMap<String, Integer> tempHp;
        RuntimeException e2;
        Iterator<String> it;
        String[] tempScanWifiInfos;
        String[] strArr = lines;
        StdDataSet stdDataSet = new StdDataSet();
        if (param == null || strArr == null || macLst == null || strArr.length == 0 || macLst.size() == 0) {
            return stdDataSet;
        }
        HashMap<String, Integer> tempHp2 = new HashMap<>(16);
        int i = 0;
        try {
            stdDataSet.setMacLst(macLst);
            String lastBatch = "";
            int standardBatch = 0;
            int i2 = 0;
            String tempMac = null;
            int rdCount = 0;
            while (i2 < strArr.length) {
                try {
                    try {
                        String[] wds = strArr[i2].split(",");
                        if (wds.length < param.getScanWifiStart()) {
                            tempHp = tempHp2;
                        } else if (wds[i] == null) {
                            tempHp = tempHp2;
                        } else if (wds[i].equals("")) {
                            tempHp = tempHp2;
                        } else if (wds.length < param.getServingWiFiMac()) {
                            tempHp = tempHp2;
                        } else {
                            try {
                                String curBatchs = wds[i];
                                if (Integer.valueOf(curBatchs).intValue() < minBatch) {
                                    tempHp = tempHp2;
                                } else {
                                    try {
                                        if (!curBatchs.equals(lastBatch)) {
                                            standardBatch++;
                                            lastBatch = curBatchs;
                                        }
                                    } catch (NumberFormatException e3) {
                                        tempHp = tempHp2;
                                        i2++;
                                        strArr = lines;
                                        tempHp2 = tempHp;
                                        i = 0;
                                    } catch (RuntimeException e4) {
                                        RuntimeException e5 = e4;
                                        LogUtil.e(false, "getMainApStdDataSet, %{public}s", e5.getMessage());
                                        StdRecord tempStdRecord = new StdRecord(standardBatch);
                                        if (param.getTimestamp() < wds.length) {
                                        }
                                        List<Integer> tempScanRssis = new ArrayList<>(10);
                                        tempHp2.clear();
                                        tempScanWifiInfos = wds[param.getScanWifiStart()].split(param.getWifiSeperator());
                                        if (tempScanWifiInfos.length < 4) {
                                        }
                                        i2++;
                                        strArr = lines;
                                        tempHp2 = tempHp;
                                        i = 0;
                                    } catch (Exception e6) {
                                        Exception e7 = e6;
                                        try {
                                            LogUtil.e(i, "getMainApStdDataSet batch deal failed by Exception", new Object[i]);
                                            tempHp = tempHp2;
                                        } catch (RuntimeException e8) {
                                            e2 = e8;
                                            tempHp = tempHp2;
                                            Object[] objArr = new Object[1];
                                            objArr[0] = e2.getMessage();
                                            LogUtil.e(false, "getMainApStdDataSet, %{public}s", objArr);
                                            i2++;
                                            strArr = lines;
                                            tempHp2 = tempHp;
                                            i = 0;
                                        } catch (Exception e9) {
                                            tempHp = tempHp2;
                                            try {
                                                LogUtil.e(false, "getMainApStdDataSet rssi set failed by Exception", new Object[0]);
                                                i2++;
                                                strArr = lines;
                                                tempHp2 = tempHp;
                                                i = 0;
                                            } catch (RuntimeException e10) {
                                                e = e10;
                                                z = false;
                                            } catch (Exception e11) {
                                            }
                                        }
                                        i2++;
                                        strArr = lines;
                                        tempHp2 = tempHp;
                                        i = 0;
                                    }
                                    StdRecord tempStdRecord2 = new StdRecord(standardBatch);
                                    if (param.getTimestamp() < wds.length) {
                                        tempStdRecord2.setTimeStamp(wds[param.getTimestamp()]);
                                    } else {
                                        tempStdRecord2.setTimeStamp("0");
                                    }
                                    List<Integer> tempScanRssis2 = new ArrayList<>(10);
                                    tempHp2.clear();
                                    try {
                                        tempScanWifiInfos = wds[param.getScanWifiStart()].split(param.getWifiSeperator());
                                        if (tempScanWifiInfos.length < 4) {
                                            tempHp = tempHp2;
                                        } else {
                                            tempMac = tempScanWifiInfos[param.getScanMac()];
                                            if (!stdDataSet.getMacRecords().containsKey(tempMac)) {
                                                if (!checkMacFormat(tempMac)) {
                                                    tempHp = tempHp2;
                                                } else {
                                                    stdDataSet.getMacRecords().put(tempMac, new TMapList<>());
                                                }
                                            }
                                            tempHp2.put(tempScanWifiInfos[param.getScanMac()], Integer.valueOf(Integer.parseInt(tempScanWifiInfos[param.getScanRssi()].split(DOT_ESCAPE)[0])));
                                            Iterator<String> it2 = macLst.iterator();
                                            while (it2.hasNext()) {
                                                try {
                                                    if (tempHp2.containsKey(it2.next())) {
                                                        tempScanRssis2.add(Integer.valueOf(tempHp2.get(tempMac).intValue()));
                                                    } else {
                                                        tempScanRssis2.add(0);
                                                    }
                                                    it = it2;
                                                    tempHp = tempHp2;
                                                } catch (RuntimeException e12) {
                                                    it = it2;
                                                    Object[] objArr2 = new Object[1];
                                                    tempHp = tempHp2;
                                                    try {
                                                        objArr2[0] = e12.getMessage();
                                                        LogUtil.e(false, "getMainApStdDataSet, %{public}s", objArr2);
                                                    } catch (RuntimeException e13) {
                                                        e2 = e13;
                                                        Object[] objArr3 = new Object[1];
                                                        objArr3[0] = e2.getMessage();
                                                        LogUtil.e(false, "getMainApStdDataSet, %{public}s", objArr3);
                                                        i2++;
                                                        strArr = lines;
                                                        tempHp2 = tempHp;
                                                        i = 0;
                                                    } catch (Exception e14) {
                                                        LogUtil.e(false, "getMainApStdDataSet rssi set failed by Exception", new Object[0]);
                                                        i2++;
                                                        strArr = lines;
                                                        tempHp2 = tempHp;
                                                        i = 0;
                                                    }
                                                } catch (Exception e15) {
                                                    it = it2;
                                                    LogUtil.e(false, "getMainApStdDataSet rssi add failed by Exception", new Object[0]);
                                                    tempHp = tempHp2;
                                                }
                                                it2 = it;
                                                tempHp2 = tempHp;
                                            }
                                            tempHp = tempHp2;
                                            tempStdRecord2.setScanRssis(tempScanRssis2);
                                            stdDataSet.getMacRecords().get(tempMac).add((TMapList<Integer, StdRecord>) Integer.valueOf(standardBatch), (Integer) tempStdRecord2);
                                            rdCount++;
                                        }
                                    } catch (NumberFormatException e16) {
                                        LogUtil.e(false, "getMainApStdDataSet, %{public}s", e16.getMessage());
                                    }
                                }
                            } catch (NumberFormatException e17) {
                                tempHp = tempHp2;
                            } catch (RuntimeException e18) {
                            } catch (Exception e19) {
                            }
                        }
                    } catch (RuntimeException e20) {
                        e2 = e20;
                        tempHp = tempHp2;
                        Object[] objArr32 = new Object[1];
                        objArr32[0] = e2.getMessage();
                        LogUtil.e(false, "getMainApStdDataSet, %{public}s", objArr32);
                        i2++;
                        strArr = lines;
                        tempHp2 = tempHp;
                        i = 0;
                    } catch (Exception e21) {
                        tempHp = tempHp2;
                        LogUtil.e(false, "getMainApStdDataSet rssi set failed by Exception", new Object[0]);
                        i2++;
                        strArr = lines;
                        tempHp2 = tempHp;
                        i = 0;
                    }
                    i2++;
                    strArr = lines;
                    tempHp2 = tempHp;
                    i = 0;
                } catch (RuntimeException e22) {
                    e = e22;
                    z = i;
                    Object[] objArr4 = new Object[1];
                    String message = e.getMessage();
                    char c = z ? 1 : 0;
                    char c2 = z ? 1 : 0;
                    char c3 = z ? 1 : 0;
                    char c4 = z ? 1 : 0;
                    char c5 = z ? 1 : 0;
                    char c6 = z ? 1 : 0;
                    objArr4[c] = message;
                    LogUtil.e(z, "getMainApStdDataSet, %{public}s", objArr4);
                    return stdDataSet;
                } catch (Exception e23) {
                    LogUtil.e(false, "getMainApStdDataSet failed by Exception", new Object[0]);
                    return stdDataSet;
                }
            }
            List<Integer> macIndexs = new ArrayList<>(10);
            int size = macLst.size();
            for (int i3 = 0; i3 < size; i3++) {
                macIndexs.add(Integer.valueOf(i3));
            }
            stdDataSet.setMacIndexLst(macIndexs);
            Object[] objArr5 = new Object[1];
            objArr5[0] = Integer.valueOf(rdCount);
            LogUtil.d(false, "getMainApStdDataSet rdCount:%{public}d", objArr5);
        } catch (RuntimeException e24) {
            e = e24;
            z = false;
            Object[] objArr42 = new Object[1];
            String message2 = e.getMessage();
            char c7 = z ? 1 : 0;
            char c22 = z ? 1 : 0;
            char c32 = z ? 1 : 0;
            char c42 = z ? 1 : 0;
            char c52 = z ? 1 : 0;
            char c62 = z ? 1 : 0;
            objArr42[c7] = message2;
            LogUtil.e(z, "getMainApStdDataSet, %{public}s", objArr42);
            return stdDataSet;
        } catch (Exception e25) {
            LogUtil.e(false, "getMainApStdDataSet failed by Exception", new Object[0]);
            return stdDataSet;
        }
        return stdDataSet;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0109, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x010a, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0148, code lost:
        com.android.server.hidata.wavemapping.util.LogUtil.e(false, "getCommStdDataSet failed by Exception", new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0151, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0152, code lost:
        r3 = false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0147 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:9:0x002c] */
    private StdDataSet getCommStdDataSet(String place, ParameterInfo param, String[] lines, List<String> macLst, int minBatch) {
        boolean z;
        RuntimeException e;
        String tempServeMac;
        RuntimeException e2;
        Exception e3;
        String[] strArr = lines;
        StdDataSet stdDataSet = new StdDataSet();
        if (param == null || strArr == null || macLst == null || strArr.length == 0 || macLst.size() == 0) {
            return stdDataSet;
        }
        HashMap<String, Integer> tempHp = new HashMap<>(16);
        char c = 0;
        try {
            stdDataSet.setMacLst(macLst);
            int i = 0;
            int standardBatch = 0;
            String lastBatch = "";
            int rdCount = 0;
            while (i < strArr.length) {
                try {
                    String[] wds = strArr[i].split(",");
                    if (wds.length >= param.getScanWifiStart()) {
                        if (wds[c] != null) {
                            if (!wds[c].equals("")) {
                                if (wds.length > param.getServingWiFiMac()) {
                                    try {
                                        String curBatch = wds[c];
                                        if (Integer.valueOf(curBatch).intValue() >= minBatch) {
                                            tempServeMac = wds[param.getServingWiFiMac()];
                                            try {
                                                if (!stdDataSet.getMacRecords().containsKey(tempServeMac)) {
                                                    stdDataSet.getMacRecords().put(tempServeMac, new TMapList<>());
                                                }
                                                if (!curBatch.equals(lastBatch)) {
                                                    standardBatch++;
                                                    lastBatch = curBatch;
                                                }
                                            } catch (NumberFormatException e4) {
                                            } catch (RuntimeException e5) {
                                                e2 = e5;
                                                LogUtil.e(false, "getCommStdDataSet, %{public}s", e2.getMessage());
                                                tempServeMac = tempServeMac;
                                                StdRecord tempStdRecord = new StdRecord(standardBatch);
                                                if (param.getTimestamp() < wds.length) {
                                                }
                                                processTempHp(tempHp, param, wds);
                                                judgeMacParam(macLst, tempHp, tempStdRecord);
                                                stdDataSet.getMacRecords().get(tempServeMac).add((TMapList<Integer, StdRecord>) Integer.valueOf(standardBatch), (Integer) tempStdRecord);
                                                rdCount++;
                                                i++;
                                                strArr = lines;
                                                c = 0;
                                            } catch (Exception e6) {
                                                e3 = e6;
                                                LogUtil.e(false, "getCommStdDataSet batch deal failed by Exception", new Object[0]);
                                                i++;
                                                strArr = lines;
                                                c = 0;
                                            }
                                            StdRecord tempStdRecord2 = new StdRecord(standardBatch);
                                            if (param.getTimestamp() < wds.length) {
                                                tempStdRecord2.setTimeStamp(wds[param.getTimestamp()]);
                                            } else {
                                                tempStdRecord2.setTimeStamp("0");
                                            }
                                            processTempHp(tempHp, param, wds);
                                            judgeMacParam(macLst, tempHp, tempStdRecord2);
                                            stdDataSet.getMacRecords().get(tempServeMac).add((TMapList<Integer, StdRecord>) Integer.valueOf(standardBatch), (Integer) tempStdRecord2);
                                            rdCount++;
                                        }
                                    } catch (NumberFormatException e7) {
                                    } catch (RuntimeException e8) {
                                        e2 = e8;
                                        tempServeMac = null;
                                        LogUtil.e(false, "getCommStdDataSet, %{public}s", e2.getMessage());
                                        tempServeMac = tempServeMac;
                                        StdRecord tempStdRecord22 = new StdRecord(standardBatch);
                                        if (param.getTimestamp() < wds.length) {
                                        }
                                        processTempHp(tempHp, param, wds);
                                        judgeMacParam(macLst, tempHp, tempStdRecord22);
                                        stdDataSet.getMacRecords().get(tempServeMac).add((TMapList<Integer, StdRecord>) Integer.valueOf(standardBatch), (Integer) tempStdRecord22);
                                        rdCount++;
                                    } catch (Exception e9) {
                                        e3 = e9;
                                        LogUtil.e(false, "getCommStdDataSet batch deal failed by Exception", new Object[0]);
                                    }
                                }
                            }
                        }
                    }
                } catch (RuntimeException e10) {
                    LogUtil.e(false, "getCommStdDataSet, %{public}s", e10.getMessage());
                } catch (Exception e11) {
                    LogUtil.e(false, "getCommStdDataSet rssi set failed by Exception", new Object[0]);
                }
                i++;
                strArr = lines;
                c = 0;
            }
            stdDataSet.setTotalCnt(rdCount);
            macLst.add(Constant.MAINAP_TAG);
            z = false;
            LogUtil.d(false, "rdCount:" + rdCount, new Object[0]);
        } catch (RuntimeException e12) {
            e = e12;
        } catch (Exception e13) {
        }
        return stdDataSet;
        Object[] objArr = new Object[1];
        String message = e.getMessage();
        char c2 = z ? 1 : 0;
        char c3 = z ? 1 : 0;
        char c4 = z ? 1 : 0;
        char c5 = z ? 1 : 0;
        objArr[c2] = message;
        LogUtil.e(z, "getCommStdDataSet, %{public}s", objArr);
        return stdDataSet;
    }

    private void processTempHp(HashMap<String, Integer> tempHp, ParameterInfo param, String[] wds) {
        tempHp.clear();
        int tempSize = wds.length;
        for (int k = param.getScanWifiStart(); k < tempSize; k++) {
            try {
                String[] tempScanWifiInfos = wds[k].split(param.getWifiSeperator());
                if (tempScanWifiInfos.length >= 4) {
                    if (checkMacFormat(tempScanWifiInfos[param.getScanMac()])) {
                        tempHp.put(tempScanWifiInfos[param.getScanMac()], Integer.valueOf(Integer.parseInt(tempScanWifiInfos[param.getScanRssi()].split(DOT_ESCAPE)[0])));
                    }
                }
            } catch (NumberFormatException e) {
                LogUtil.e(false, "getCommStdDataSet, %{public}s", e.getMessage());
            }
        }
    }

    private void judgeMacParam(List<String> macLst, HashMap<String, Integer> tempHp, StdRecord tempStdRecord) {
        List<Integer> tempScanRssis = new ArrayList<>(10);
        for (String mac : macLst) {
            try {
                if (tempHp.containsKey(mac)) {
                    tempScanRssis.add(tempHp.get(mac));
                } else {
                    tempScanRssis.add(0);
                }
            } catch (RuntimeException e) {
                LogUtil.e(false, "getCommStdDataSet, %{public}s", e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(false, "getCommStdDataSet rssi add failed by Exception", new Object[0]);
            }
        }
        tempStdRecord.setScanRssis(tempScanRssis);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0186: APUT  
      (r7v17 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x017f: INVOKE  (r8v17 java.lang.String) = (r0v61 'e' java.lang.NumberFormatException A[D('e' java.lang.NumberFormatException)]) type: VIRTUAL call: java.lang.NumberFormatException.getMessage():java.lang.String)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x02a2: APUT  
      (r3v5 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x029d: INVOKE  (r2v9 java.lang.String) = (r0v15 'e' java.lang.RuntimeException A[D('e' java.lang.RuntimeException)]) type: VIRTUAL call: java.lang.RuntimeException.getMessage():java.lang.String)
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x028d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x028e, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x02b6, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x02b7, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x02e9, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x02ef, code lost:
        com.android.server.hidata.wavemapping.util.LogUtil.e(false, "getFilterMobileAps2StdDataSet failed by Exception", new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:186:?, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0169, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0172, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0175, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0176, code lost:
        r20 = r1;
        r17 = r17;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x02ee A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:9:0x002e] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x016f A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:63:0x010f] */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0172 A[ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:63:0x010f] */
    private StdDataSet getFilterMobileAps2StdDataSet(String place, ParameterInfo param, String[] lines, List<String> macLst) {
        char c;
        boolean z;
        String lastBatch;
        int i;
        String lastBatch2;
        int tempSize;
        String str;
        String lastBatch3;
        int standardBatch;
        RuntimeException e;
        RuntimeException e2;
        String lastBatch4;
        StdRecord tempStdRecord;
        List<Integer> tempScanRssis;
        int tempServeRssi;
        int size2;
        String[] wds;
        TrainModelService trainModelService = this;
        String[] strArr = lines;
        StdDataSet stdDataSet = new StdDataSet();
        if (param == null || strArr == null || macLst == null || strArr.length == 0 || macLst.size() == 0) {
            return stdDataSet;
        }
        HashMap<String, Integer> tempHp = new HashMap<>(16);
        HashMap<String, String> macSsidHp = new HashMap<>(16);
        c = 0;
        try {
            stdDataSet.setMacLst(macLst);
            lastBatch = "";
            i = 0;
            int rdCount = 0;
            lastBatch2 = lastBatch;
            tempSize = 0;
            while (i < strArr.length) {
                try {
                    String[] wds2 = strArr[i].split(",");
                    if (wds2.length < param.getScanWifiStart()) {
                        str = lastBatch;
                    } else if (wds2[c] == null) {
                        str = lastBatch;
                    } else if (wds2[c].equals(lastBatch)) {
                        str = lastBatch;
                    } else if (wds2.length <= param.getServingWiFiMac()) {
                        str = lastBatch;
                    } else {
                        try {
                            String curBatch = wds2[c];
                            String tempServeMac = wds2[param.getServingWiFiMac()];
                            if (tempServeMac == null) {
                                str = lastBatch;
                            } else {
                                if (!stdDataSet.getMacRecords().containsKey(tempServeMac)) {
                                    try {
                                        if (!trainModelService.checkMacFormat(tempServeMac)) {
                                            str = lastBatch;
                                        } else {
                                            stdDataSet.getMacRecords().put(tempServeMac, new TMapList<>());
                                        }
                                    } catch (RuntimeException e3) {
                                        e2 = e3;
                                        str = lastBatch;
                                        LogUtil.e(false, "getFilterMobileAps2StdDataSet 1 , %{public}s", e2.getMessage());
                                        i++;
                                        trainModelService = this;
                                        strArr = lines;
                                        lastBatch = str;
                                        c = 0;
                                    } catch (Exception e4) {
                                        str = lastBatch;
                                        try {
                                            LogUtil.e(false, "getFilterMobileAps2StdDataSet batch deal failed by Exception", new Object[0]);
                                        } catch (RuntimeException e5) {
                                            e = e5;
                                            lastBatch3 = lastBatch2;
                                            standardBatch = tempSize;
                                            Object[] objArr = new Object[1];
                                            objArr[0] = e.getMessage();
                                            LogUtil.e(false, "getFilterMobileAps2StdDataSet 6, %{public}s", objArr);
                                            tempSize = standardBatch;
                                            lastBatch2 = lastBatch3;
                                            i++;
                                            trainModelService = this;
                                            strArr = lines;
                                            lastBatch = str;
                                            c = 0;
                                        } catch (Exception e6) {
                                            lastBatch3 = lastBatch2;
                                            standardBatch = tempSize;
                                            LogUtil.e(false, "getFilterMobileAps2StdDataSet rssi set failed by Exception", new Object[0]);
                                            tempSize = standardBatch;
                                            lastBatch2 = lastBatch3;
                                            i++;
                                            trainModelService = this;
                                            strArr = lines;
                                            lastBatch = str;
                                            c = 0;
                                        }
                                        i++;
                                        trainModelService = this;
                                        strArr = lines;
                                        lastBatch = str;
                                        c = 0;
                                    }
                                }
                                if (!curBatch.equals(lastBatch2)) {
                                    lastBatch4 = curBatch;
                                    standardBatch = tempSize + 1;
                                } else {
                                    lastBatch4 = lastBatch2;
                                    standardBatch = tempSize;
                                }
                                try {
                                    StdRecord tempStdRecord2 = new StdRecord(standardBatch);
                                    if (param.getTimestamp() < wds2.length) {
                                        try {
                                            tempStdRecord = tempStdRecord2;
                                            tempStdRecord.setTimeStamp(wds2[param.getTimestamp()]);
                                        } catch (RuntimeException e7) {
                                            e = e7;
                                            str = lastBatch;
                                        } catch (Exception e8) {
                                            str = lastBatch;
                                            lastBatch3 = lastBatch4;
                                            LogUtil.e(false, "getFilterMobileAps2StdDataSet rssi set failed by Exception", new Object[0]);
                                            tempSize = standardBatch;
                                            lastBatch2 = lastBatch3;
                                            i++;
                                            trainModelService = this;
                                            strArr = lines;
                                            lastBatch = str;
                                            c = 0;
                                        }
                                    } else {
                                        tempStdRecord = tempStdRecord2;
                                        tempStdRecord.setTimeStamp("0");
                                    }
                                    str = lastBatch;
                                    try {
                                        tempScanRssis = new ArrayList<>(10);
                                        int tempSize2 = wds2.length;
                                        tempHp.clear();
                                        int k = param.getScanWifiStart();
                                        while (k < tempSize2) {
                                            try {
                                                wds = wds2;
                                                String[] tempScanWifiInfos = wds2[k].split(param.getWifiSeperator());
                                                tempSize2 = tempSize2;
                                                if (tempScanWifiInfos.length < 4) {
                                                    lastBatch3 = lastBatch4;
                                                } else if (!trainModelService.checkMacFormat(tempScanWifiInfos[param.getScanMac()])) {
                                                    lastBatch3 = lastBatch4;
                                                } else {
                                                    tempHp.put(tempScanWifiInfos[param.getScanMac()], Integer.valueOf(Integer.parseInt(tempScanWifiInfos[param.getScanRssi()].split(DOT_ESCAPE)[0])));
                                                    macSsidHp.put(tempScanWifiInfos[param.getScanMac()], tempScanWifiInfos[param.getScanSsid()]);
                                                    lastBatch3 = lastBatch4;
                                                }
                                            } catch (NumberFormatException e9) {
                                                NumberFormatException e10 = e9;
                                                tempSize2 = tempSize2;
                                                Object[] objArr2 = new Object[1];
                                                lastBatch3 = lastBatch4;
                                                try {
                                                    objArr2[0] = e10.getMessage();
                                                    LogUtil.e(false, "getFilterMobileAps2StdDataSet 3, %{public}s", objArr2);
                                                    k++;
                                                    trainModelService = this;
                                                    wds2 = wds;
                                                    lastBatch4 = lastBatch3;
                                                } catch (RuntimeException e11) {
                                                    e = e11;
                                                    Object[] objArr3 = new Object[1];
                                                    objArr3[0] = e.getMessage();
                                                    LogUtil.e(false, "getFilterMobileAps2StdDataSet 6, %{public}s", objArr3);
                                                    tempSize = standardBatch;
                                                    lastBatch2 = lastBatch3;
                                                    i++;
                                                    trainModelService = this;
                                                    strArr = lines;
                                                    lastBatch = str;
                                                    c = 0;
                                                } catch (Exception e12) {
                                                    LogUtil.e(false, "getFilterMobileAps2StdDataSet rssi set failed by Exception", new Object[0]);
                                                    tempSize = standardBatch;
                                                    lastBatch2 = lastBatch3;
                                                    i++;
                                                    trainModelService = this;
                                                    strArr = lines;
                                                    lastBatch = str;
                                                    c = 0;
                                                }
                                            } catch (RuntimeException e13) {
                                            } catch (Exception e14) {
                                            }
                                            k++;
                                            trainModelService = this;
                                            wds2 = wds;
                                            lastBatch4 = lastBatch3;
                                        }
                                        lastBatch3 = lastBatch4;
                                        int size22 = macLst.size();
                                        int ik = 0;
                                        tempServeRssi = 0;
                                        while (ik < size22) {
                                            try {
                                                String mac = macLst.get(ik);
                                                if (tempHp.containsKey(mac)) {
                                                    if (mac.equals(tempServeMac)) {
                                                        tempServeRssi = tempHp.get(mac).intValue();
                                                    }
                                                    tempScanRssis.add(tempHp.get(mac));
                                                } else {
                                                    tempScanRssis.add(0);
                                                }
                                                size2 = size22;
                                            } catch (RuntimeException e15) {
                                                size2 = size22;
                                                LogUtil.e(false, "getFilterMobileAps2StdDataSet 4, %{public}s", e15.getMessage());
                                                tempServeRssi = tempServeRssi;
                                            } catch (Exception e16) {
                                                LogUtil.e(false, "getFilterMobileAps2StdDataSet rssi add failed by Exception", new Object[0]);
                                                size2 = size22;
                                                tempServeRssi = tempServeRssi;
                                            }
                                            ik++;
                                            size22 = size2;
                                        }
                                    } catch (RuntimeException e17) {
                                        e = e17;
                                        lastBatch3 = lastBatch4;
                                        Object[] objArr32 = new Object[1];
                                        objArr32[0] = e.getMessage();
                                        LogUtil.e(false, "getFilterMobileAps2StdDataSet 6, %{public}s", objArr32);
                                        tempSize = standardBatch;
                                        lastBatch2 = lastBatch3;
                                        i++;
                                        trainModelService = this;
                                        strArr = lines;
                                        lastBatch = str;
                                        c = 0;
                                    } catch (Exception e18) {
                                        lastBatch3 = lastBatch4;
                                        LogUtil.e(false, "getFilterMobileAps2StdDataSet rssi set failed by Exception", new Object[0]);
                                        tempSize = standardBatch;
                                        lastBatch2 = lastBatch3;
                                        i++;
                                        trainModelService = this;
                                        strArr = lines;
                                        lastBatch = str;
                                        c = 0;
                                    }
                                    try {
                                        tempScanRssis.add(Integer.valueOf(tempServeRssi));
                                        tempStdRecord.setScanRssis(tempScanRssis);
                                        tempStdRecord.setServeRssi(tempServeRssi);
                                        stdDataSet.getMacRecords().get(tempServeMac).add((TMapList<Integer, StdRecord>) Integer.valueOf(standardBatch), (Integer) tempStdRecord);
                                        rdCount++;
                                        tempSize = standardBatch;
                                        lastBatch2 = lastBatch3;
                                    } catch (RuntimeException e19) {
                                        e = e19;
                                        Object[] objArr322 = new Object[1];
                                        objArr322[0] = e.getMessage();
                                        LogUtil.e(false, "getFilterMobileAps2StdDataSet 6, %{public}s", objArr322);
                                        tempSize = standardBatch;
                                        lastBatch2 = lastBatch3;
                                        i++;
                                        trainModelService = this;
                                        strArr = lines;
                                        lastBatch = str;
                                        c = 0;
                                    } catch (Exception e20) {
                                        LogUtil.e(false, "getFilterMobileAps2StdDataSet rssi set failed by Exception", new Object[0]);
                                        tempSize = standardBatch;
                                        lastBatch2 = lastBatch3;
                                        i++;
                                        trainModelService = this;
                                        strArr = lines;
                                        lastBatch = str;
                                        c = 0;
                                    }
                                } catch (RuntimeException e21) {
                                    e = e21;
                                    str = lastBatch;
                                    lastBatch3 = lastBatch4;
                                    Object[] objArr3222 = new Object[1];
                                    objArr3222[0] = e.getMessage();
                                    LogUtil.e(false, "getFilterMobileAps2StdDataSet 6, %{public}s", objArr3222);
                                    tempSize = standardBatch;
                                    lastBatch2 = lastBatch3;
                                    i++;
                                    trainModelService = this;
                                    strArr = lines;
                                    lastBatch = str;
                                    c = 0;
                                } catch (Exception e22) {
                                    str = lastBatch;
                                    lastBatch3 = lastBatch4;
                                    LogUtil.e(false, "getFilterMobileAps2StdDataSet rssi set failed by Exception", new Object[0]);
                                    tempSize = standardBatch;
                                    lastBatch2 = lastBatch3;
                                    i++;
                                    trainModelService = this;
                                    strArr = lines;
                                    lastBatch = str;
                                    c = 0;
                                }
                            }
                        } catch (RuntimeException e23) {
                            e2 = e23;
                            str = lastBatch;
                            LogUtil.e(false, "getFilterMobileAps2StdDataSet 1 , %{public}s", e2.getMessage());
                            i++;
                            trainModelService = this;
                            strArr = lines;
                            lastBatch = str;
                            c = 0;
                        } catch (Exception e24) {
                            str = lastBatch;
                            LogUtil.e(false, "getFilterMobileAps2StdDataSet batch deal failed by Exception", new Object[0]);
                            i++;
                            trainModelService = this;
                            strArr = lines;
                            lastBatch = str;
                            c = 0;
                        }
                    }
                } catch (RuntimeException e25) {
                    e = e25;
                    str = lastBatch;
                    lastBatch3 = lastBatch2;
                    standardBatch = tempSize;
                    Object[] objArr32222 = new Object[1];
                    objArr32222[0] = e.getMessage();
                    LogUtil.e(false, "getFilterMobileAps2StdDataSet 6, %{public}s", objArr32222);
                    tempSize = standardBatch;
                    lastBatch2 = lastBatch3;
                    i++;
                    trainModelService = this;
                    strArr = lines;
                    lastBatch = str;
                    c = 0;
                } catch (Exception e26) {
                    str = lastBatch;
                    lastBatch3 = lastBatch2;
                    standardBatch = tempSize;
                    LogUtil.e(false, "getFilterMobileAps2StdDataSet rssi set failed by Exception", new Object[0]);
                    tempSize = standardBatch;
                    lastBatch2 = lastBatch3;
                    i++;
                    trainModelService = this;
                    strArr = lines;
                    lastBatch = str;
                    c = 0;
                }
                i++;
                trainModelService = this;
                strArr = lines;
                lastBatch = str;
                c = 0;
            }
            stdDataSet.setTotalCnt(rdCount);
            macLst.add(Constant.MAINAP_TAG);
            z = false;
            LogUtil.d(false, "rdCount:" + rdCount, new Object[0]);
            return filterMobileAps2(place, stdDataSet, macLst, param, macSsidHp);
        } catch (RuntimeException e27) {
            RuntimeException e28 = e27;
            z = false;
            Object[] objArr4 = new Object[1];
            String message = e28.getMessage();
            char c2 = z ? 1 : 0;
            char c3 = z ? 1 : 0;
            char c4 = z ? 1 : 0;
            char c5 = z ? 1 : 0;
            char c6 = z ? 1 : 0;
            objArr4[c2] = message;
            LogUtil.e(z, "getFilterMobileAps2StdDataSet 8, %{public}s", objArr4);
            return stdDataSet;
        } catch (Exception e29) {
        }
        lastBatch3 = lastBatch4;
        Object[] objArr322222 = new Object[1];
        objArr322222[0] = e.getMessage();
        LogUtil.e(false, "getFilterMobileAps2StdDataSet 6, %{public}s", objArr322222);
        tempSize = standardBatch;
        lastBatch2 = lastBatch3;
        i++;
        trainModelService = this;
        strArr = lines;
        lastBatch = str;
        c = 0;
    }

    private boolean saveStdDataSetToFile(StdDataSet stdDataSet, String place, ParameterInfo param) {
        boolean z;
        RuntimeException e;
        if (!isParamCorrect(stdDataSet, place, param)) {
            return false;
        }
        String dataFilePath = null;
        try {
            dataFilePath = getStdFilePath(place, param);
            if (!isFilePathCorrect(dataFilePath, place)) {
                return false;
            }
            StringBuilder trainDataSb = new StringBuilder(16);
            int size = param.isMainAp() ? stdDataSet.getMacLst().size() : stdDataSet.getMacLst().size() - 1;
            trainDataSb.append("batch,label,timestamp,link_speed,");
            try {
                processStdData(stdDataSet, trainDataSb, size);
                List<Integer> macIndexs = stdDataSet.getMacIndexLst();
                if (macIndexs == null) {
                    macIndexs = new ArrayList(10);
                    for (int i = 0; i < size; i++) {
                        macIndexs.add(Integer.valueOf(i));
                    }
                }
                trainDataSb.deleteCharAt(trainDataSb.length() - 1);
                trainDataSb.append(Constant.getLineSeparator());
                ArrayList<StdRecord> records = new ArrayList<>(10);
                for (Map.Entry<String, TMapList<Integer, StdRecord>> entry : stdDataSet.getMacRecords().entrySet()) {
                    Iterator<Map.Entry<K, StdRecord>> it = entry.getValue().entrySet().iterator();
                    while (it.hasNext()) {
                        for (StdRecord tempStdRecord : it.next().getValue()) {
                            records.add(tempStdRecord);
                        }
                    }
                }
                processTrainData(trainDataSb, records, macIndexs);
                if (!FileUtils.saveFile(dataFilePath, trainDataSb.toString())) {
                    LogUtil.d(false, " saveStdDataSetToFile save failure:%{public}s,dataFilePath:%{public}s", place, dataFilePath);
                    return false;
                }
                z = true;
                Object[] objArr = new Object[2];
                objArr[0] = place;
                char c = z ? 1 : 0;
                char c2 = z ? 1 : 0;
                char c3 = z ? 1 : 0;
                char c4 = z ? 1 : 0;
                objArr[c] = dataFilePath;
                LogUtil.i(false, " saveStdDataSetToFile save success:%{public}s,dataFilePath:%{public}s", objArr);
                return z;
            } catch (RuntimeException e2) {
                e = e2;
                z = true;
                LogUtil.e(false, "saveStdDataSetToFile, %{public}s", e.getMessage());
                Object[] objArr2 = new Object[2];
                objArr2[0] = place;
                char c5 = z ? 1 : 0;
                char c22 = z ? 1 : 0;
                char c32 = z ? 1 : 0;
                char c42 = z ? 1 : 0;
                objArr2[c5] = dataFilePath;
                LogUtil.i(false, " saveStdDataSetToFile save success:%{public}s,dataFilePath:%{public}s", objArr2);
                return z;
            } catch (Exception e3) {
                LogUtil.e(false, "saveStdDataSetToFile save file failed by Exception", new Object[0]);
                z = true;
                Object[] objArr22 = new Object[2];
                objArr22[0] = place;
                char c52 = z ? 1 : 0;
                char c222 = z ? 1 : 0;
                char c322 = z ? 1 : 0;
                char c422 = z ? 1 : 0;
                objArr22[c52] = dataFilePath;
                LogUtil.i(false, " saveStdDataSetToFile save success:%{public}s,dataFilePath:%{public}s", objArr22);
                return z;
            }
        } catch (RuntimeException e4) {
            e = e4;
            z = true;
            LogUtil.e(false, "saveStdDataSetToFile, %{public}s", e.getMessage());
            Object[] objArr222 = new Object[2];
            objArr222[0] = place;
            char c522 = z ? 1 : 0;
            char c2222 = z ? 1 : 0;
            char c3222 = z ? 1 : 0;
            char c4222 = z ? 1 : 0;
            objArr222[c522] = dataFilePath;
            LogUtil.i(false, " saveStdDataSetToFile save success:%{public}s,dataFilePath:%{public}s", objArr222);
            return z;
        } catch (Exception e5) {
            LogUtil.e(false, "saveStdDataSetToFile save file failed by Exception", new Object[0]);
            z = true;
            Object[] objArr2222 = new Object[2];
            objArr2222[0] = place;
            char c5222 = z ? 1 : 0;
            char c22222 = z ? 1 : 0;
            char c32222 = z ? 1 : 0;
            char c42222 = z ? 1 : 0;
            objArr2222[c5222] = dataFilePath;
            LogUtil.i(false, " saveStdDataSetToFile save success:%{public}s,dataFilePath:%{public}s", objArr2222);
            return z;
        }
    }

    private boolean isFilePathCorrect(String dataFilePath, String place) {
        LogUtil.i(false, " saveStdDataSetToFile save begin:%{public}s,dataFilePath:%{public}s", place, dataFilePath);
        if (FileUtils.delFile(dataFilePath)) {
            return true;
        }
        LogUtil.d(false, "saveStdDataSetToFile failure,FileUtils.delFile(dataFilePath),dataFilePath:%{public}s", dataFilePath);
        return false;
    }

    private void processStdData(StdDataSet stdDataSet, StringBuilder trainDataSb, int size) {
        int validMacCnt = 0;
        for (int i = 0; i < size; i++) {
            try {
                String tempMac = stdDataSet.getMacLst().get(i);
                if (!"0".equals(tempMac)) {
                    validMacCnt++;
                    trainDataSb.append(tempMac);
                    trainDataSb.append(",");
                }
            } catch (RuntimeException e) {
                LogUtil.e(false, "saveStdDataSetToFile, %{public}s", e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(false, "saveStdDataSetToFile trainDataSb append failed by Exception", new Object[0]);
            }
        }
        stdDataSet.setValidMacCnt(validMacCnt);
    }

    private void processTrainData(StringBuilder trainDataSb, ArrayList<StdRecord> records, List<Integer> macIndexs) {
        Collections.sort(records, new Comparator<StdRecord>() {
            /* class com.android.server.hidata.wavemapping.modelservice.TrainModelService.AnonymousClass1 */

            public int compare(StdRecord o1, StdRecord o2) {
                return o1.getBatch() - o2.getBatch();
            }
        });
        Iterator<StdRecord> it = records.iterator();
        while (it.hasNext()) {
            StdRecord record = it.next();
            try {
                trainDataSb.append(String.valueOf(record.getBatch()));
                trainDataSb.append(",0,");
                trainDataSb.append(record.getTimeStamp());
                trainDataSb.append(",0");
                for (Integer index : macIndexs) {
                    trainDataSb.append(",");
                    trainDataSb.append(record.getScanRssis().get(index.intValue()));
                }
                trainDataSb.append(Constant.getLineSeparator());
            } catch (RuntimeException e) {
                LogUtil.e(false, "saveStdDataSetToFile, %{public}s", e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(false, "saveStdDataSetToFile failed by Exception", new Object[0]);
            }
        }
    }

    private boolean isParamCorrect(StdDataSet stdDataSet, String place, ParameterInfo param) {
        if (stdDataSet == null) {
            LogUtil.d(false, "saveStdDataSetToFile,stdDataSet == null", new Object[0]);
            return false;
        } else if (stdDataSet.getMacLst() == null || stdDataSet.getMacLst().size() == 0) {
            LogUtil.d(false, "saveStdDataSetToFile,getMacLst == null or getMacLst = 0", new Object[0]);
            return false;
        } else if (place == null) {
            LogUtil.d(false, "saveStdDataSetToFile,place == null", new Object[0]);
            return false;
        } else if (param != null) {
            return true;
        } else {
            LogUtil.d(false, "saveStdDataSetToFile,param == null", new Object[0]);
            return false;
        }
    }

    public StdDataSet filterMobileAps2(String place, StdDataSet stdDataSet, List<String> macLst, ParameterInfo param, HashMap<String, String> macSsidHp) {
        if (stdDataSet == null) {
            LogUtil.d(false, "filterMobileAps2,stdDataSet == null", new Object[0]);
            return stdDataSet;
        } else if (macLst == null || macLst.size() == 0) {
            LogUtil.d(false, "filterMobileAps2,macLst == null or macLst.size == 0", new Object[0]);
            return stdDataSet;
        } else if (macSsidHp == null || macSsidHp.size() == 0) {
            LogUtil.d(false, "filterMobileAps2,macSsidHp == null or macSsidHp.size == 0", new Object[0]);
            return stdDataSet;
        } else {
            List<Integer> macIndexs = new ArrayList<>(10);
            try {
                Set<String> setMobileMacs = new HashSet<>(getAllMobileMacs(place, stdDataSet, macLst, param, macSsidHp));
                LogUtil.d(false, "filterMobileAps2 mobiles Ap is :%{private}s", setMobileMacs.toString());
                int size = macLst.size() - 1;
                for (int i = 0; i < size; i++) {
                    if (!setMobileMacs.contains(macLst.get(i))) {
                        macIndexs.add(Integer.valueOf(i));
                    } else {
                        macLst.set(i, "0");
                    }
                }
                stdDataSet.setFilter2MobileApCnt(setMobileMacs.size());
            } catch (RuntimeException e) {
                LogUtil.e(false, "filterMobileAps2, %{public}s", e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(false, "filterMobileAps2 failed by Exception", new Object[0]);
            }
            stdDataSet.setMacIndexLst(macIndexs);
            return stdDataSet;
        }
    }

    private List<String> getAllMobileMacs(String place, StdDataSet stdDataSet, List<String> macLst, ParameterInfo param, HashMap<String, String> macSsidHp) {
        RuntimeException e;
        HashMap<String, ArrayList<Float>> colsRssis = new HashMap<>(16);
        TMapSet<String, String> tmap = new TMapSet<>();
        List<String> allMobileMacs = new ArrayList<>(10);
        for (Map.Entry<String, TMapList<Integer, StdRecord>> entry : stdDataSet.getMacRecords().entrySet()) {
            try {
                colsRssis.clear();
                for (Map.Entry<Integer, List<StdRecord>> entry2 : entry.getValue().entrySet()) {
                    computeBatchFp(colsRssis, entry2.getKey(), entry2.getValue(), macLst, param);
                }
                try {
                    for (String tempMobileMac : filterBatchFp(colsRssis, macLst)) {
                        try {
                            String tempSsid = macSsidHp.get(tempMobileMac);
                            if (tempSsid != null) {
                                tmap.add((TMapSet<String, String>) tempSsid, tempMobileMac);
                            }
                        } catch (RuntimeException e2) {
                            e = e2;
                            LogUtil.e(false, "filterMobileAps2, %{public}s", e.getMessage());
                        } catch (Exception e3) {
                            LogUtil.e(false, "filterMobileAps2 add ssid failed by Exception", new Object[0]);
                        }
                    }
                } catch (RuntimeException e4) {
                    e = e4;
                    LogUtil.e(false, "filterMobileAps2, %{public}s", e.getMessage());
                } catch (Exception e5) {
                    LogUtil.e(false, "filterMobileAps2 add ssid failed by Exception", new Object[0]);
                }
            } catch (RuntimeException e6) {
                e = e6;
                LogUtil.e(false, "filterMobileAps2, %{public}s", e.getMessage());
            } catch (Exception e7) {
                LogUtil.e(false, "filterMobileAps2 add ssid failed by Exception", new Object[0]);
            }
        }
        Set<String> testMobileMacs = new HashSet<>(16);
        for (Map.Entry<String, Set<String>> entry3 : tmap.entrySet()) {
            List<String> tempFilterMacs = addMobileAp(entry3.getKey(), entry3.getValue().iterator(), 2);
            testMobileMacs.addAll(tempFilterMacs);
            if (tempFilterMacs.size() > 0) {
                allMobileMacs.addAll(tempFilterMacs);
                LogUtil.d(false, "filterMobileAps,add mobileAp success.%{public}d", Integer.valueOf(tempFilterMacs.size()));
            }
        }
        LogUtil.wtLogFile("isMainAp:" + String.valueOf(param.isMainAp()) + ",place:" + place + ",filterMobileAps2 mobiles Ap is,mac:" + testMobileMacs.toString() + Constant.getLineSeparator());
        return allMobileMacs;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r6v2, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v1 */
    /* JADX WARN: Type inference failed for: r6v3 */
    /* JADX WARN: Type inference failed for: r6v16 */
    /* JADX WARN: Type inference failed for: r6v17 */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x009d: APUT  
      (r7v5 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x0098: INVOKE  (r6v8 java.lang.String) = (r0v19 'e' java.lang.RuntimeException A[D('e' java.lang.RuntimeException)]) type: VIRTUAL call: java.lang.RuntimeException.getMessage():java.lang.String)
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0091, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0092, code lost:
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00ad, code lost:
        com.android.server.hidata.wavemapping.util.LogUtil.e(false, "filterBatchFp failed by Exception", new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00b6, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00b7, code lost:
        r6 = false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ac A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:10:0x0027] */
    public List<String> filterBatchFp(HashMap<String, ArrayList<Float>> colsRssis, List<String> macLst) {
        int i;
        int i2;
        boolean z;
        RuntimeException e;
        List<String> mobileSsids = new ArrayList<>(10);
        if (colsRssis == null) {
            i = 0;
        } else if (colsRssis.size() == 0) {
            i = 0;
        } else {
            if (macLst == null) {
                i2 = 0;
            } else if (macLst.size() == 0) {
                i2 = 0;
            } else {
                try {
                    MobileApCheckParamInfo param = ParamManager.getInstance().getMobileApCheckParamInfo();
                    List<Integer> topkMainApRssiIndexs = getTopkRssiIndexs(colsRssis.get(Constant.MAINAP_TAG), param);
                    if (topkMainApRssiIndexs != null) {
                        if (topkMainApRssiIndexs.size() != 0) {
                            GetStd getStd = new GetStd();
                            for (int i3 = macLst.size() - 1; i3 > -1; i3--) {
                                try {
                                    ArrayList<Float> tempFitkColRssis = getFitkColRssiList(topkMainApRssiIndexs, colsRssis.get(macLst.get(i3)));
                                    if (tempFitkColRssis.size() != 0) {
                                        if (((double) getStd.getStandardDevition(tempFitkColRssis)) > ((double) param.getMobileApMinStd())) {
                                            mobileSsids.add(macLst.get(i3));
                                        }
                                    }
                                } catch (RuntimeException e2) {
                                    Object[] objArr = new Object[1];
                                    objArr[0] = e2.getMessage();
                                    LogUtil.e(false, "filterBatchFp, %{public}s", objArr);
                                } catch (Exception e3) {
                                    LogUtil.e(false, "filterBatchFp ssid add failed by Exception", new Object[0]);
                                }
                            }
                            return mobileSsids;
                        }
                    }
                    return mobileSsids;
                } catch (RuntimeException e4) {
                    e = e4;
                    z = false;
                } catch (Exception e5) {
                }
            }
            LogUtil.d(i2, "filterBatchFp,macLst == null or size == 0", new Object[i2]);
            return mobileSsids;
        }
        LogUtil.d(i, "filterBatchFp,colsRssis == null", new Object[i]);
        return mobileSsids;
        Object[] objArr2 = new Object[1];
        String message = e.getMessage();
        char c = z ? 1 : 0;
        char c2 = z ? 1 : 0;
        char c3 = z ? 1 : 0;
        char c4 = z ? 1 : 0;
        objArr2[c] = message;
        LogUtil.e(z, "filterBatchFp, %{public}s", objArr2);
        return mobileSsids;
    }

    private ArrayList<Float> getFitkColRssiList(List<Integer> topkIndexs, ArrayList<Float> colRssis) {
        ArrayList<Float> tempFitkColRssis = new ArrayList<>(10);
        if (topkIndexs == null || topkIndexs.size() == 0 || colRssis == null || colRssis.size() == 0) {
            return tempFitkColRssis;
        }
        try {
            int size = colRssis.size() - 1;
            for (Integer index : topkIndexs) {
                if (index.intValue() <= size) {
                    tempFitkColRssis.add(colRssis.get(index.intValue()));
                }
            }
        } catch (RuntimeException e) {
            LogUtil.e(false, "getFitkColRssiList, %{public}s", e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(false, "getFitkColRssiList failed by Exception", new Object[0]);
        }
        return tempFitkColRssis;
    }

    private List<Integer> getTopkRssiIndexs(ArrayList<Float> mainApRssiLst, MobileApCheckParamInfo param) {
        List<Integer> results = new ArrayList<>(10);
        if (mainApRssiLst == null || mainApRssiLst.size() == 0) {
            LogUtil.d(false, "getTopkRssiIndexs,mainApRssiLst == null", new Object[0]);
            return results;
        } else if (param == null) {
            LogUtil.d(false, "getTopkRssiIndexs,param == null", new Object[0]);
            return results;
        } else {
            try {
                Float maxVal = (Float) Collections.max(mainApRssiLst);
                int size = mainApRssiLst.size();
                for (int i = 0; i < size; i++) {
                    if (maxVal.floatValue() - mainApRssiLst.get(i).floatValue() <= ((float) param.getMobileApMinRange())) {
                        results.add(Integer.valueOf(i));
                    }
                }
            } catch (RuntimeException e) {
                LogUtil.e(false, "getTopkRssiIndexs, %{public}s", e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(false, "getTopkRssiIndexs failed by Exception", new Object[0]);
            }
            return results;
        }
    }

    public void computeBatchFp(HashMap<String, ArrayList<Float>> colsRssis, Integer batch, List<StdRecord> stdRecordLst, List<String> macLst, ParameterInfo param) {
        int batchNum = stdRecordLst.size();
        if (batchNum != 0) {
            int macsCnt = macLst.size();
            for (int i = 0; i < macsCnt; i++) {
                int nonZeroSum = 0;
                int nonZeroNum = 0;
                for (int k = 0; k < batchNum; k++) {
                    try {
                        int tempVal = stdRecordLst.get(k).getScanRssis().get(i).intValue();
                        if (tempVal != 0) {
                            nonZeroNum++;
                            nonZeroSum += tempVal;
                        }
                    } catch (RuntimeException e) {
                        LogUtil.e(false, "computeBatchFp, %{public}s", e.getMessage());
                    } catch (Exception e2) {
                        try {
                            LogUtil.e(false, "computeBatchFp failed by Exception", new Object[0]);
                        } catch (RuntimeException e3) {
                            e = e3;
                            LogUtil.e(false, "computeBatchFp, %{public}s", e.getMessage());
                        } catch (Exception e4) {
                            LogUtil.e(false, "computeBatchFp rssi add failed by Exception", new Object[0]);
                        }
                    }
                }
                float avg = -100.0f;
                if (((float) nonZeroNum) / ((float) batchNum) > param.getWeightParam() && nonZeroNum != 0) {
                    avg = ((float) nonZeroSum) / ((float) nonZeroNum);
                }
                try {
                    String tempMac = macLst.get(i);
                    if (!colsRssis.containsKey(tempMac)) {
                        colsRssis.put(tempMac, new ArrayList<>(10));
                    }
                    colsRssis.get(tempMac).add(Float.valueOf(avg));
                } catch (RuntimeException e5) {
                    e = e5;
                    LogUtil.e(false, "computeBatchFp, %{public}s", e.getMessage());
                } catch (Exception e6) {
                    LogUtil.e(false, "computeBatchFp rssi add failed by Exception", new Object[0]);
                }
            }
        }
    }
}
