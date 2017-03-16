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

    public Map<String, String> parseContent(String content) {
        Map<String, String> map = new HashMap<>();
        Annotation document = new Annotation(content);

        // run all Annotators on this text
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            map.put(sentence.toShorterString("Text"), ConnUtil.getConnStr(sentence));
        }
        return map;
    }
}
