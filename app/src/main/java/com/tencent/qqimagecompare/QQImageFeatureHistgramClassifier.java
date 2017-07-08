package com.tencent.qqimagecompare;

import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: Unknown */
public class QQImageFeatureHistgramClassifier {
    private int mThreshold;
    private ArrayList<QQImageFeatureHSV> mW;
    private IClassifyCallback mX;
    private Object mY;
    private int mZ;

    /* compiled from: Unknown */
    public interface IClassifyCallback {
        void OnStep(int i, Object obj);
    }

    public QQImageFeatureHistgramClassifier() {
        this.mThreshold = 80;
        this.mW = new ArrayList();
    }

    public void addFeature(QQImageFeatureHSV qQImageFeatureHSV) {
        this.mW.add(qQImageFeatureHSV);
        this.mZ = this.mW.size();
    }

    public ArrayList<ArrayList<QQImageFeatureHSV>> classify() {
        ArrayList<ArrayList<QQImageFeatureHSV>> arrayList = new ArrayList();
        if (this.mW.size() > 0) {
            while (true) {
                int size = this.mW.size();
                if (size != 1) {
                    if (size <= 1) {
                        break;
                    }
                    ArrayList arrayList2 = new ArrayList();
                    QQImageFeatureHSV qQImageFeatureHSV = (QQImageFeatureHSV) this.mW.remove(0);
                    arrayList2.add(qQImageFeatureHSV);
                    Iterator it = this.mW.iterator();
                    while (it.hasNext()) {
                        QQImageFeatureHSV qQImageFeatureHSV2 = (QQImageFeatureHSV) it.next();
                        if (qQImageFeatureHSV.compare(qQImageFeatureHSV2) >= this.mThreshold) {
                            arrayList2.add(qQImageFeatureHSV2);
                            it.remove();
                        }
                    }
                    arrayList.add(arrayList2);
                    if (this.mX != null) {
                        this.mX.OnStep(((this.mZ - this.mW.size()) * 100) / this.mZ, this.mY);
                    }
                } else {
                    ArrayList arrayList3 = new ArrayList();
                    arrayList3.add(this.mW.remove(0));
                    arrayList.add(arrayList3);
                    if (this.mX != null) {
                        this.mX.OnStep(((this.mZ - this.mW.size()) * 100) / this.mZ, this.mY);
                    }
                }
            }
        }
        return arrayList;
    }

    public void setClassifyCallback(IClassifyCallback iClassifyCallback, Object obj) {
        this.mX = iClassifyCallback;
        this.mY = obj;
    }

    public void setThreshold(int i) {
        this.mThreshold = i;
    }
}
