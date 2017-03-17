package com.hadou;

import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;

import java.util.Map;
import java.util.Properties;

/**
 * Created by jiajianchao on 2017/3/15.
 */
public class SRParser extends AbstractParser {
    @Override
    public void init() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,  pos, lemma, ner, parse");
        pipeline = new CustomerCoreNLPClient(props, "http://127.0.0.1", 39002, 4);
    }

}
