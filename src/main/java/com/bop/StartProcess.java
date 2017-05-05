package com.bop;

import com.bop.job.SegmentJob;
import com.bop.service.NERService;
import com.bop.util.CoreNLPUtil;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by jfd on 5/3/17.
 */
@Component
public class StartProcess implements CommandLineRunner{

    private static final Logger logger = LoggerFactory.getLogger(StartProcess.class);

    private static CRFClassifier<CoreLabel> segment = CoreNLPUtil.getSegment();

    private static AbstractSequenceClassifier<CoreLabel> ner = CoreNLPUtil.getNer();

    @Autowired
    private NERService nerService;

    @Override
    public void run(String... strings) throws Exception {
        logger.info("StartProcess run");

        SegmentJob segmentJob = new SegmentJob(nerService, null, segment, ner);

        List<Map<String, Object>> list = nerService.queryList();

        list.forEach(map -> map.entrySet().forEach(entry ->logger.info("\n{} : {}",entry.getKey(),entry.getValue())));

        List<String> eventNames = getEventName(list);

        eventNames.forEach(text ->segmentJob.doNlp(text));

//        segmentJob.doNlp("北京酷宝汽车装饰有限公司的董事长刘洋的出售的装饰材料不符合国家标准");

        logger.info("StartProcess end");

    }

    private List<String> getEventName(List<Map<String, Object>> list){

        List<String> names = new ArrayList<>();

        list.forEach(map -> map.values().forEach(value -> names.add((String) value)));

        return names;
    }
}
