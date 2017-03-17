package com.hadou;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jiajianchao on 2017/3/15.
 */
public abstract class AbstractParser {
    StanfordCoreNLPClient pipeline = null;

    public abstract void init();

    public void close() {
        try {
            if (pipeline != null)
                pipeline.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String parseContent(String content) {
        Annotation document = new Annotation(content);
        String result = null;
        // run all Annotators on this text
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences != null) {
            for (CoreMap sentence : sentences) {
                // this is the parse tree of the current sentence
                result = ConnUtil.getConnStr(sentence);
            }
        }
        return result;
    }
}
