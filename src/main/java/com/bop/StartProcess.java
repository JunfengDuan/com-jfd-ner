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

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.bop.util.NerType.CAPACITY;
import static com.bop.util.NerType.POOL_SIZE;
import static com.bop.util.NerType.TAKE_SIZE;

/**
 * Created by jfd on 5/3/17.
 */
@Component
public class StartProcess implements CommandLineRunner{

    private static final Logger logger = LoggerFactory.getLogger(StartProcess.class);

    private static CRFClassifier<CoreLabel> segment = CoreNLPUtil.getSegment();

    private static AbstractSequenceClassifier<CoreLabel> ner = CoreNLPUtil.getNer();
    private ExecutorService executorService;

    @Autowired
    private NERService nerService;

    @Override
    public void run(String... strings) throws Exception {
        logger.info("NER process run");

        executorService = Executors.newFixedThreadPool(POOL_SIZE);
        IntStream.range(0, POOL_SIZE).forEach(n -> executorService.execute(new SegmentJob(nerService, segment, ner)));

    }

    @PreDestroy
    public void shutDown(){
        logger.info("SegmentJob is topping");
        executorService.shutdown();
        logger.info("NER process stopped");
    }


}
