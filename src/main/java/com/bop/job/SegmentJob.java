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
import org.springframework.boot.SpringApplication;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.bop.util.NerType.*;

/**
 * Created by jfd on 5/3/17.
 */
public class SegmentJob implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(SegmentJob.class);
    private final NERService nerService;
    private final CRFClassifier<CoreLabel> segment;
    private final AbstractSequenceClassifier<CoreLabel> ner;
    private AtomicInteger offset = new AtomicInteger(0);
    private BlockingDeque<Map> blockingDeque;
    private int capacity;
    private int takeSize;

    public SegmentJob(NERService nerService, CRFClassifier<CoreLabel> segment, AbstractSequenceClassifier<CoreLabel> ner, 
                      BlockingDeque<Map> blockingDeque, int capacity, int takeSize) {
        this.nerService = nerService;
        this.segment = segment;
        this.ner = ner;
        this.blockingDeque = blockingDeque;
        this.capacity = capacity;
        this.takeSize = takeSize;
    }

    @Override
    public void run() {
        try {
            while (true){
                logger.info("BlockingDeque init size:{}",blockingDeque.size());
                if(blockingDeque.size() < takeSize) {
                    nerService.queryFromDB(offset, blockingDeque, capacity);
                    offset.set(offset.get()+capacity);
                }
                logger.info("BlockingDeque size:{}",blockingDeque.size());
                if(blockingDeque.isEmpty()){
                    logger.warn("Nothing grabbed from database,thread is sleeping for 5 seconds");
                    break;
                }
                startJob();
            }
        } catch (Exception e) {
            logger.error("SegmentJob exception: {}",e.getMessage());
        }finally {
            logger.warn("SegmentJob is exiting...");
            return;
        }
    }

    private void startJob() {
        logger.info("Start a new job");
        List<Map<String, Object>> list = new ArrayList<>();

        IntStream.range(0, takeSize).mapToObj(n -> nerService.takeFromDeque(blockingDeque)).forEach(list :: add);

        list.forEach(map -> map.entrySet().forEach(entry ->logger.info("\n{} : {}",entry.getKey(),entry.getValue())));

        List<Map<String,String>> eventNames = getEventName(list);
        eventNames.forEach(text -> text.entrySet().forEach(entry ->doNlp(entry.getKey(),entry.getValue())));
    }

    private List<Map<String,String>> getEventName(List<Map<String, Object>> list){

        List<Map<String,String>> events = new ArrayList<>();

        list.forEach(l -> {
            Map<String,String> newMap = new HashMap<>();
            Object[] values = l.values().toArray();
            newMap.put(String.valueOf(values[0]),String.valueOf(values[1]));
            events.add(newMap);
        });

        return events;
    }

    public void doNlp(String id, String text) {

       String segString = doSegment(text);
        Map result = doNer(segString);

        Map<String,Object> segments = new HashMap<>();
        segments.put(text, result);

        nerService.saveResults(id, segments);
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
