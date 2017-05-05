package com.bop.util;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

/**
 * Created by jfd on 5/3/17.
 */
public class CoreNLPUtil {

    private static CRFClassifier<CoreLabel> segment;
    private static AbstractSequenceClassifier<CoreLabel> ner;

    public static CRFClassifier<CoreLabel> getSegment(){

            // 设置一些初始化参数
            Properties props = new Properties();
            props.setProperty("sighanCorporaDict", "data");
            props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
            props.setProperty("inputEncoding", "UTF-8");
            props.setProperty("sighanPostProcessing", "true");
            segment = new CRFClassifier<CoreLabel>(props);
            segment.loadClassifierNoExceptions("data/ctb.gz", props);
            segment.flags.setProperties(props);

            return segment;
    }

    public static AbstractSequenceClassifier<CoreLabel> getNer() {

        String serializedClassifier = "classifiers/chinese.misc.distsim.crf.ser.gz";

        if (ner == null) {
            ner = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
        }

        return ner;
    }
}
