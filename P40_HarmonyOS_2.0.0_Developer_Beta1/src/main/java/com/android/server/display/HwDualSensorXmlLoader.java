package com.android.server.display;

import android.util.Slog;
import com.android.server.display.HwDualSensorXmlLoader;
import com.huawei.displayengine.XmlElement;
import com.huawei.displayengine.XmlLoader;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class HwDualSensorXmlLoader extends XmlLoader<HwDualSensorData> {
    private static final String DUAL_SENSOR_XML_PATH = "/xml/lcd/DualSensorConfig.xml";
    private static final String TAG = "HwDualSensorXmlLoader";
    private static final HwDualSensorData sData = new HwDualSensorData();
    private static HwDualSensorXmlLoader sLoader;
    private static final Object sLock = new Object();

    private HwDualSensorXmlLoader() {
        load(sData, getBackSensorParametersElements());
        load(sData, getModuleSensorOptionsElements());
        load(sData, getAlgorithmParametersElements());
        sData.print();
    }

    public static HwDualSensorData getData() {
        HwDualSensorData retData = null;
        synchronized (sLock) {
            try {
                if (sLoader == null) {
                    sLoader = new HwDualSensorXmlLoader();
                }
                retData = sData;
                if (retData == null) {
                    sData.loadDefault();
                    retData = sData;
                }
            } catch (RuntimeException e) {
                Slog.e(TAG, "getData() failed! " + e);
                if (0 == 0) {
                    sData.loadDefault();
                }
            } catch (Throwable th) {
                if (0 == 0) {
                    sData.loadDefault();
                    HwDualSensorData hwDualSensorData = sData;
                }
                throw th;
            }
        }
        return retData;
    }

    /* access modifiers changed from: protected */
    public String getXmlPath() {
        File xmlFile = HwCfgFilePolicy.getCfgFile(DUAL_SENSOR_XML_PATH, 0);
        if (xmlFile == null) {
            Slog.w(TAG, "get xmlFile :/xml/lcd/DualSensorConfig.xml failed!");
            return "";
        }
        try {
            return xmlFile.getCanonicalPath();
        } catch (IOException e) {
            Slog.e(TAG, "get xmlCanonicalPath error IOException!");
            return null;
        }
    }

    private XmlElement getBackSensorParametersElements() {
        XmlElement root = new XmlElementBackSensorParameters();
        root.registerChildElement(new XmlElementBackSensorParametersGroup());
        return root;
    }

    /* access modifiers changed from: private */
    public static final class XmlElementBackSensorParameters extends XmlElement<HwDualSensorData> {
        private XmlElementBackSensorParameters() {
        }

        /* access modifiers changed from: protected */
        public String getBranchName() {
            return "BackSensorParameters";
        }
    }

    /* access modifiers changed from: private */
    public final class XmlElementBackSensorParametersGroup extends XmlElement<HwDualSensorData> {
        private static final int LUX_COEFFICIENTS_SIZE = 5;
        private static final int SPECTRUM_PARAM_NIR_SIZE = 8;
        private static final int SPECTRUM_PARAM_POLY_SIZE = 4;
        private static final int SPECTRUM_PARAM_SIZE = 10;
        private static final String SPLIT = ",";

        private XmlElementBackSensorParametersGroup() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void putSpectrumParams(HashMap<String, BiConsumer<String, HwDualSensorData>> map) {
            map.put("SpectrumLuxPram", new BiConsumer() {
                /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$mu2TimDVbF7BKzfvscm3bZyyoDA */

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.this.lambda$putSpectrumParams$0$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup((String) obj, (HwDualSensorData) obj2);
                }
            });
            map.put("SpectrumScalingPram", new BiConsumer() {
                /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$n5UM0tzpMUr0_UafcbZOGpyB4n8 */

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.this.lambda$putSpectrumParams$1$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup((String) obj, (HwDualSensorData) obj2);
                }
            });
            map.put("SpectrumNirPram", new BiConsumer() {
                /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$ZZUXJpub4LKAyiq96t3KVh6FoU */

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.this.lambda$putSpectrumParams$2$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup((String) obj, (HwDualSensorData) obj2);
                }
            });
            map.put("SpectrumPoly1Pram", new BiConsumer() {
                /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$aapEBgPdIcnkJDlFdLpGmtPqiPM */

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.this.lambda$putSpectrumParams$3$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup((String) obj, (HwDualSensorData) obj2);
                }
            });
            map.put("SpectrumPoly2Pram", new BiConsumer() {
                /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$CpyKbBoI1SfPBNnP0zNXqaiR6rQ */

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.this.lambda$putSpectrumParams$4$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup((String) obj, (HwDualSensorData) obj2);
                }
            });
            map.put("SpectrumPoly3Pram", new BiConsumer() {
                /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$t1vpDZxROadVOUN7bUiHc6yavfo */

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.this.lambda$putSpectrumParams$5$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup((String) obj, (HwDualSensorData) obj2);
                }
            });
            map.put("SpectrumPoly4Pram", new BiConsumer() {
                /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$UMWBy5E6EDm9JGcx8iD1G_MnRuA */

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.this.lambda$putSpectrumParams$6$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup((String) obj, (HwDualSensorData) obj2);
                }
            });
            map.put("SpectrumPoly5Pram", new BiConsumer() {
                /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$PGWto1FxuuJf0JSqv5bFbAUSog */

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.this.lambda$putSpectrumParams$7$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup((String) obj, (HwDualSensorData) obj2);
                }
            });
            map.put("SpectrumGain", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$TNytBktChieb7MpwuK0UhY8neI.INSTANCE);
            map.put("SpectrumTime", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$8Z4oN_GM9Aqh3F9GReYc6VFMPsI.INSTANCE);
            map.put("SpecLowLuxTh1", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$2tU3RHZGfoaSVMqfkJGQP2VRGOQ.INSTANCE);
            map.put("SpecLowLuxTh2", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$Lbkd_it70Djc_PriqeMYH2NkFFQ.INSTANCE);
            map.put("SpecLowLuxTh3", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$GvNKys_JKP7SYlsRlm5fYl0oIEI.INSTANCE);
            map.put("SpecLowLuxTh4", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$nnzgE3M5As3N5PZwsDbW4gwDG4.INSTANCE);
            map.put("NeedNirCompensation", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$V3O5wlfdrjAzBZ6o1yLdrYlD734.INSTANCE);
        }

        /* access modifiers changed from: protected */
        public Map<String, BiConsumer<String, HwDualSensorData>> getParser() {
            return new HashMap<String, BiConsumer<String, HwDualSensorData>>(40) {
                /* class com.android.server.display.HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.AnonymousClass1 */

                {
                    put("SensorVersion", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NigoHqpGJ9HjJtRVrPDaJ36UeYw.INSTANCE);
                    put("FrontSensorRateMillis", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zCHjwAvtzi0VaAYQFIkm2KZEx0.INSTANCE);
                    put("BackSensorRateMillis", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ZHpDrS75_CX8lGWBcSLCTBCeCQ.INSTANCE);
                    put("FusedSensorRateMillis", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$p5JVUTwcCnxgFqmWB44OZS85lI.INSTANCE);
                    put("FusedColorSensorRateMillis", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$NzvUmp2dwUn1Bz8QNhzWnJjDnNM.INSTANCE);
                    put("IR_BOUNDRY_RGBCW", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$Q7cEeZBNrWAe4SJ4mOaP6C_cyM.INSTANCE);
                    put("LuxrgbcwCoefficientsLow", new BiConsumer() {
                        /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$9QIkkCpF3qHNYKkU9DppSqhHVlk */

                        @Override // java.util.function.BiConsumer
                        public final void accept(Object obj, Object obj2) {
                            HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.AnonymousClass1.this.lambda$new$6$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1((String) obj, (HwDualSensorData) obj2);
                        }
                    });
                    put("LuxrgbcwCoefficientsHigh", new BiConsumer() {
                        /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$7ra2POUqClCpvZ6zWNgzOZiJas0 */

                        @Override // java.util.function.BiConsumer
                        public final void accept(Object obj, Object obj2) {
                            HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.AnonymousClass1.this.lambda$new$7$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1((String) obj, (HwDualSensorData) obj2);
                        }
                    });
                    put("ATime_rgbcw", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$4crToUdgPe8G2YJ9qFfa8HjHOoQ.INSTANCE);
                    put("AGain_rgbcw", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$cEvmXTD4x6LgQr4zhcVnEp0JKMM.INSTANCE);
                    put("ProductionCalibrationR", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$lkXzs9uaMfaIZHKk98plcb72hlQ.INSTANCE);
                    put("ProductionCalibrationG", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$spqvo5NF765uqbpaM9e1KGLTWY.INSTANCE);
                    put("ProductionCalibrationB", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$a2LtoJyx7BepPT5Dj71UbYD4Tk8.INSTANCE);
                    put("ProductionCalibrationC", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$nPAKRicNhoO8LW2xmjfCGdzKeg.INSTANCE);
                    put("ProductionCalibrationW", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$Prhr5mN7qEH96rsaJXluGemqo.INSTANCE);
                    put("IRBoundry", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$TKOd4PGCmhqebykNqcDtUuRCiLo.INSTANCE);
                    put("LuxCoefficientsLow", new BiConsumer() {
                        /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$o4SETQ3OKUdJNOuxjw4I_1ra1Es */

                        @Override // java.util.function.BiConsumer
                        public final void accept(Object obj, Object obj2) {
                            HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.AnonymousClass1.this.lambda$new$16$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1((String) obj, (HwDualSensorData) obj2);
                        }
                    });
                    put("LuxCoefficientsHigh", new BiConsumer() {
                        /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$4a9lOu7A7NkNOor7DcCMIemnmhU */

                        @Override // java.util.function.BiConsumer
                        public final void accept(Object obj, Object obj2) {
                            HwDualSensorXmlLoader.XmlElementBackSensorParametersGroup.AnonymousClass1.this.lambda$new$17$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1((String) obj, (HwDualSensorData) obj2);
                        }
                    });
                    put("ATime", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$LYZPTycG5LgFemJs4C3kvszUNl8.INSTANCE);
                    put("AGain", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$vtjeDgsY8p2ZHm5g3p4SeENGeKA.INSTANCE);
                    put("LuxScale", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$zFty8zjj_uorQG8Pmcm3AyhzkM.INSTANCE);
                    put("ProductionCalibrationX", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$8ukHil6h5GkgBOE41A9U1lEuC4.INSTANCE);
                    put("ProductionCalibrationY", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$ScQUOizbldHRi20EQ5tjcCGqiRg.INSTANCE);
                    put("ProductionCalibrationZ", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$HJx3uETRqOYz6nTJvtwH_GkwSVo.INSTANCE);
                    put("ProductionCalibrationIR1", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$1cqdvNM9Hj8AlWDl6oB49alcpiM.INSTANCE);
                    put("RatioIRGreen", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$KxEKVYx9lvNlZKEPRjkhcRKRP4.INSTANCE);
                    put("LuxCoefHighRed", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$6__Hja_gWgXehQpQ5ofRPN0D5Dw.INSTANCE);
                    put("LuxCoefHighGreen", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$k1Q0p7DXTOtdo3rJjJaYdOKDJQ.INSTANCE);
                    put("LuxCoefHighBlue", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$2d2bRcQlblxeG3cUM5xL6zgH3YY.INSTANCE);
                    put("LuxCoefLowRed", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$IhddK4ikraaykE0PcJ4DD1TXBKE.INSTANCE);
                    put("LuxCoefLowGreen", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$_Geg9Az_Wsch_s2Cva41ndXqDvk.INSTANCE);
                    put("LuxCoefLowBlue", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$KFf9i5aKyEhZZZ7ZU0cPska2qOM.INSTANCE);
                    XmlElementBackSensorParametersGroup.this.putSpectrumParams(this);
                    put("IsOutwardLightSensor", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$bjmFha6LQywQgl6keloK12o_PEw.INSTANCE);
                    put("IsInwardFoldScreen", $$Lambda$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1$2NFL0_Dtg9Hq7EX8bWklVQeLwLk.INSTANCE);
                }

                public /* synthetic */ void lambda$new$6$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1(String text, HwDualSensorData data) {
                    XmlElementBackSensorParametersGroup.this.parseLuxRgbcwCoefficientsLow(text, data);
                }

                public /* synthetic */ void lambda$new$7$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1(String text, HwDualSensorData data) {
                    XmlElementBackSensorParametersGroup.this.parseLuxRgbcwCoefficientsHigh(text, data);
                }

                public /* synthetic */ void lambda$new$16$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1(String text, HwDualSensorData data) {
                    XmlElementBackSensorParametersGroup.this.parseLuxXyzCoefficientsLow(text, data);
                }

                public /* synthetic */ void lambda$new$17$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup$1(String text, HwDualSensorData data) {
                    XmlElementBackSensorParametersGroup.this.parseLuxXyzCoefficientsHigh(text, data);
                }
            };
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void parseLuxRgbcwCoefficientsLow(String text, HwDualSensorData data) {
            double[] parsedArray = parseDoubleArray(text, 5, ",");
            data.luxCoefLow1 = (float) parsedArray[0];
            data.luxCoefLow2 = (float) parsedArray[1];
            data.luxCoefLow3 = (float) parsedArray[2];
            data.luxCoefLow4 = (float) parsedArray[3];
            data.luxCoefLow5 = (float) parsedArray[4];
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void parseLuxRgbcwCoefficientsHigh(String text, HwDualSensorData data) {
            double[] parsedArray = parseDoubleArray(text, 5, ",");
            data.luxCoefHigh1 = (float) parsedArray[0];
            data.luxCoefHigh2 = (float) parsedArray[1];
            data.luxCoefHigh3 = (float) parsedArray[2];
            data.luxCoefHigh4 = (float) parsedArray[3];
            data.luxCoefHigh5 = (float) parsedArray[4];
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void parseLuxXyzCoefficientsLow(String text, HwDualSensorData data) {
            double[] parsedArray = parseDoubleArray(text, 5, ",");
            data.luxCoefLowX = (float) parsedArray[0];
            data.luxCoefLowY = (float) parsedArray[1];
            data.luxCoefLowZ = (float) parsedArray[2];
            data.luxCoefLowIr = (float) parsedArray[3];
            data.luxCoefLowOffset = (float) parsedArray[4];
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void parseLuxXyzCoefficientsHigh(String text, HwDualSensorData data) {
            double[] parsedArray = parseDoubleArray(text, 5, ",");
            data.luxCoefHighX = (float) parsedArray[0];
            data.luxCoefHighY = (float) parsedArray[1];
            data.luxCoefHighZ = (float) parsedArray[2];
            data.luxCoefHighIr = (float) parsedArray[3];
            data.luxCoefHighOffset = (float) parsedArray[4];
        }

        /* access modifiers changed from: private */
        /* renamed from: parseSpectrumLuxParam */
        public void lambda$putSpectrumParams$0$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup(String text, HwDualSensorData data) {
            data.spectrumParam = parseDoubleArray(text, 10, ",");
        }

        /* access modifiers changed from: private */
        /* renamed from: parseSpectrumLuxScalingParam */
        public void lambda$putSpectrumParams$1$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup(String text, HwDualSensorData data) {
            data.scaling = parseDoubleArray(text, 8, ",");
        }

        /* access modifiers changed from: private */
        /* renamed from: parseSpectrumLuxNirParam */
        public void lambda$putSpectrumParams$2$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup(String text, HwDualSensorData data) {
            data.ratioNir = parseDoubleArray(text, 8, ",");
        }

        /* access modifiers changed from: private */
        /* renamed from: parseSpectrumLuxPoly1Param */
        public void lambda$putSpectrumParams$3$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup(String text, HwDualSensorData data) {
            data.polyCoefs1 = parseDoubleArray(text, 4, ",");
        }

        /* access modifiers changed from: private */
        /* renamed from: parseSpectrumLuxPoly2Param */
        public void lambda$putSpectrumParams$4$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup(String text, HwDualSensorData data) {
            data.polyCoefs2 = parseDoubleArray(text, 4, ",");
        }

        /* access modifiers changed from: private */
        /* renamed from: parseSpectrumLuxPoly3Param */
        public void lambda$putSpectrumParams$5$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup(String text, HwDualSensorData data) {
            data.polyCoefs3 = parseDoubleArray(text, 4, ",");
        }

        /* access modifiers changed from: private */
        /* renamed from: parseSpectrumLuxPoly4Param */
        public void lambda$putSpectrumParams$6$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup(String text, HwDualSensorData data) {
            data.polyCoefs4 = parseDoubleArray(text, 4, ",");
        }

        /* access modifiers changed from: private */
        /* renamed from: parseSpectrumLuxPoly5Param */
        public void lambda$putSpectrumParams$7$HwDualSensorXmlLoader$XmlElementBackSensorParametersGroup(String text, HwDualSensorData data) {
            data.polyCoefs5 = parseDoubleArray(text, 4, ",");
        }
    }

    private XmlElement getModuleSensorOptionsElements() {
        XmlElement root = new XmlElementModuleSensorOptions();
        root.registerChildElement(new XmlElementModuleSensorOptionsGroup());
        return root;
    }

    /* access modifiers changed from: private */
    public static final class XmlElementModuleSensorOptions extends XmlElement<HwDualSensorData> {
        private XmlElementModuleSensorOptions() {
        }

        /* access modifiers changed from: protected */
        public String getBranchName() {
            return "ModuleSensorOptions";
        }
    }

    /* access modifiers changed from: private */
    public final class XmlElementModuleSensorOptionsGroup extends XmlElement<HwDualSensorData> {
        private XmlElementModuleSensorOptionsGroup() {
        }

        /* access modifiers changed from: protected */
        public Map<String, BiConsumer<String, HwDualSensorData>> getParser() {
            return new HashMap<String, BiConsumer<String, HwDualSensorData>>(3) {
                /* class com.android.server.display.HwDualSensorXmlLoader.XmlElementModuleSensorOptionsGroup.AnonymousClass1 */

                {
                    put("HwLightSensorController", $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$pI4u3jpuA2mLCbUaAhbZcmD_9hU.INSTANCE);
                    put("HwNormalizedAutomaticBrightnessController", $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$6fmJBxDLkfp6J3D_3uXEzbmy8.INSTANCE);
                    put("HwNormalizedManualBrightnessController", $$Lambda$HwDualSensorXmlLoader$XmlElementModuleSensorOptionsGroup$1$chyon5x4ojaVkuw7ssq02r0wuM.INSTANCE);
                }
            };
        }
    }

    private XmlElement getAlgorithmParametersElements() {
        XmlElement root = new XmlElementAlgorithmParameters();
        root.registerChildElement(new XmlElementAlgorithmParametersGroup());
        return root;
    }

    /* access modifiers changed from: private */
    public static final class XmlElementAlgorithmParameters extends XmlElement<HwDualSensorData> {
        private XmlElementAlgorithmParameters() {
        }

        /* access modifiers changed from: protected */
        public String getBranchName() {
            return "AlgorithmParameters";
        }
    }

    /* access modifiers changed from: private */
    public final class XmlElementAlgorithmParametersGroup extends XmlElement<HwDualSensorData> {
        private static final String SPLIT = ",";
        private static final int STABILITY_PROBABILITY_LIST_SIZE = 25;
        private static final int STABILITY_THRESHOLD_LIST_SIZE = 4;

        private XmlElementAlgorithmParametersGroup() {
        }

        /* access modifiers changed from: protected */
        public Map<String, BiConsumer<String, HwDualSensorData>> getParser() {
            return new HashMap<String, BiConsumer<String, HwDualSensorData>>(18) {
                /* class com.android.server.display.HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.AnonymousClass1 */

                {
                    put("FrontSensorStabilityThresholdList", new BiConsumer() {
                        /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$2fKzZKZSgqwxSAQBM47WdOF6Q */

                        @Override // java.util.function.BiConsumer
                        public final void accept(Object obj, Object obj2) {
                            HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.AnonymousClass1.this.lambda$new$0$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1((String) obj, (HwDualSensorData) obj2);
                        }
                    });
                    put("BackSensorStabilityThresholdList", new BiConsumer() {
                        /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$8bFYc5hMz84qHFZiy7SZWdESys */

                        @Override // java.util.function.BiConsumer
                        public final void accept(Object obj, Object obj2) {
                            HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.AnonymousClass1.this.lambda$new$1$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1((String) obj, (HwDualSensorData) obj2);
                        }
                    });
                    put("StabilizedProbabilityLUT", new BiConsumer() {
                        /* class com.android.server.display.$$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$b1bPE_dvBUbu0rzfgW_6RtyeEwE */

                        @Override // java.util.function.BiConsumer
                        public final void accept(Object obj, Object obj2) {
                            HwDualSensorXmlLoader.XmlElementAlgorithmParametersGroup.AnonymousClass1.this.lambda$new$2$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1((String) obj, (HwDualSensorData) obj2);
                        }
                    });
                    put("BackRoofThresh", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$bvUSFKuiMXxEK0GEucKnZWYaSSo.INSTANCE);
                    put("BackFloorThresh", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$6sgjCbNwcaJqP6ngnkB_kJprGc.INSTANCE);
                    put("DarkRoomThresh", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$STeJQvr54oCLDUaXJ4mqBvO24I.INSTANCE);
                    put("DarkRoomThresh1", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$hyuBYmmg94SzoX7ngLcdxqFlkc.INSTANCE);
                    put("DarkRoomThresh2", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$ftqmlBHdgLlfApG5VfrSOD9KCwA.INSTANCE);
                    put("DarkRoomThresh3", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$4OIotJvsbzgXAjyBrtggjbHV36g.INSTANCE);
                    put("DarkRoomDelta", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$UcqfElTeXISxLan2CeV6bUSI8cc.INSTANCE);
                    put("DarkRoomDelta1", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$dZgco1dlNaK8L038igyBzkabotw.INSTANCE);
                    put("DarkRoomDelta2", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$tlo1CGZratNUrkmxcIwg6hqGI.INSTANCE);
                    put("FlashlightDetectionMode", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$K5BJGN2Y1FesN1Z8rNPJWwWlsc4.INSTANCE);
                    put("FlashlightOffTimeThresholdMs", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$DXPpWDlQYQfi7vglRmze2jmqysI.INSTANCE);
                    put("BackLuxDeviationThresh", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$lcF8v84eNM10iahZqhd7xGiEfIc.INSTANCE);
                    put("BackSensorTimeOutTH", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$I3LCw23iE8xipVvDxQUaalFUuo.INSTANCE);
                    put("BackSensorBypassCountMax", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$S55mRsyQi3gZHQU1GaCpCX2OzE.INSTANCE);
                    put("IsFilterOn", $$Lambda$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1$EI7aMWKfwXHqvs8VX8ngZ518nro.INSTANCE);
                }

                public /* synthetic */ void lambda$new$0$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1(String text, HwDualSensorData data) {
                    XmlElementAlgorithmParametersGroup.this.parseFrontStabilityThresholdList(text, data);
                }

                public /* synthetic */ void lambda$new$1$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1(String text, HwDualSensorData data) {
                    XmlElementAlgorithmParametersGroup.this.parseBackStabilityThresholdList(text, data);
                }

                public /* synthetic */ void lambda$new$2$HwDualSensorXmlLoader$XmlElementAlgorithmParametersGroup$1(String text, HwDualSensorData data) {
                    XmlElementAlgorithmParametersGroup.this.parseStabilizedProbabilityLutList(text, data);
                }
            };
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void parseFrontStabilityThresholdList(String text, HwDualSensorData data) {
            data.frontSensorStabilityThreshold = parseIntArray(text, 4, ",");
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void parseBackStabilityThresholdList(String text, HwDualSensorData data) {
            data.backSensorStabilityThreshold = parseIntArray(text, 4, ",");
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void parseStabilizedProbabilityLutList(String text, HwDualSensorData data) {
            data.stabilizedProbabilityLut = parseIntArray(text, 25, ",");
        }
    }
}
