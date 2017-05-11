package com.bop;

import com.bop.job.SegmentJob;
import com.bop.service.NERService;
import com.bop.util.CoreNLPUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static com.bop.util.NerType.*;

/**
 * Created by jfd on 5/3/17.
 */
@Component
public class StartProcess {

    private static final Logger logger = LoggerFactory.getLogger(StartProcess.class);

    private static CRFClassifier<CoreLabel> segment = CoreNLPUtil.getSegment();

    private static AbstractSequenceClassifier<CoreLabel> ner = CoreNLPUtil.getNer();
    private ExecutorService executorService;
    private ThreadFactory threadFactory;
    @Value("${pool.size:1}")
    private int poolSize;
    @Value("${deque.capacity:100}")
    private int capacity;
    @Value("${take.size:10}")
    private int takeSize;
    private BlockingDeque<Map> blockingDeque;

    @Autowired
    private NERService nerService;

    @PostConstruct
    public void init() throws Exception {
        logger.info("NER process run");

        blockingDeque = new LinkedBlockingDeque<>(poolSize*capacity);
        threadFactory = new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_FORMAT).build();
        executorService = Executors.newFixedThreadPool(poolSize, threadFactory);

        IntStream.range(0, poolSize).forEach(n -> executorService.submit(
                new SegmentJob(nerService, segment, ner, blockingDeque, capacity, takeSize)));

    }

    @PreDestroy
    public void shutDown(){
        logger.info("SegmentJob is topping");
        if (executorService != null) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn("Got InterruptedException while shutting down, aborting");
            }
        }

        logger.info("NER process stopped");
    }


}
