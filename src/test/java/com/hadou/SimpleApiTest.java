package com.hadou;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.simple.Sentence;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by jiajianchao on 2017/3/3.
 */
public class SimpleApiTest {
    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        BufferedReader reader = null;
        reader= IOUtils.readerFromString("StanfordCoreNLP-chinese.properties");
        props.load(reader);
        Sentence sent = new Sentence("国务院总理李克强调研上海外高桥时提出，支持上海积极探索新机制。 ");
        List<String> nerTags = sent.nerTags(props);  // [PERSON, O, O, O, O, O, O, O]
        String firstPOSTag = sent.posTags(props).get(0);   // NNP
        System.out.println(firstPOSTag);
    }
}
