package com.hadou;

import java.util.Properties;

/**
 * Created by jiajianchao on 2017/3/17.
 */
public class PCFGParser extends AbstractParser {
    @Override
    public void init() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,   pos, lemma, ner, parse");
        pipeline = new CustomerCoreNLPClient(props, "http://127.0.0.1", 39004, 4);
    }
}
