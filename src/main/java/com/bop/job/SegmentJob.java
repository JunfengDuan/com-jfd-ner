package com.bop.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bop.service.NERService;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bop.util.NerType.*;

/**
 * Created by jfd on 5/3/17.
 */
public class SegmentJob {

    private static final Logger logger = LoggerFactory.getLogger(SegmentJob.class);

    private final NERService nerService;
    private final StanfordCoreNLP pipeline;
    private final CRFClassifier<CoreLabel> segment;
    private final AbstractSequenceClassifier<CoreLabel> ner;

    @Autowired
    public SegmentJob(NERService nerService, StanfordCoreNLP pipeline, CRFClassifier<CoreLabel> segment, AbstractSequenceClassifier<CoreLabel> ner) {
        this.nerService = nerService;
        this.pipeline = pipeline;
        this.segment = segment;
        this.ner = ner;
    }

    public void doNlp(String text) {

       String segString = doSegment(text);

        Map result = doNer(segString);

        Map<String,Object> segments = new HashMap<>();

        segments.put(text, result);

        nerService.saveResults(segments);

    }

    public String doSegment(String sent) {

        List<String> strings = segment.segmentString(sent);

        String segmentString = StringUtils.join(strings, " ");

        logger.info("segmented res :{}",segmentString);
        return segmentString;
    }

    private Map<String, Object> doNer(String tokenString){

        Map<String,Object> tags = new HashMap<>();
        JSONArray other = new JSONArray();

        String xml = ner.classifyWithInlineXML(tokenString);
        String msg = xml.replaceAll(" ","");
        logger.debug("\nxml:{}", xml);

        List<String> regex = regex(msg);
        regex.forEach(r -> parseXml(tags, other, r));
        tags.put(O, other.toJSONString());

        tags.entrySet().forEach(e -> logger.info("text:{}; tag:{}; ner:{}",
                tokenString.replaceAll(" ",""), e.getValue(), e.getKey()));
        return tags;
    }

    private List<String> regex(String xml){
        List<String> list = new ArrayList<>();
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(xml);
        while (m.find()) {
            list.add(m.group());
        }
        return list;
    }

    private void parseXml(Map<String,Object> tags, JSONArray other, String xml){
        try {
            Document document = DocumentHelper.parseText(xml);
            Element root = document.getRootElement();
            if(tags.containsKey(root.getName())){
                JSONObject obj = new JSONObject();
                obj.put(root.getName(),root.getText());
                other.add(obj);
            }else{
                tags.put(root.getName(), root.getText());
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

}
