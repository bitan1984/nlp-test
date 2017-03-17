package com.hadou;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

/**
 * Created by jiajianchao on 2017/3/14.
 */
public class ClientTest {
    static String pattern = "[.。]|[!?！？\n]+";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,  pos, lemma, ner, depparse");
        StanfordCoreNLPClient pipeline = new CustomerCoreNLPClient(props, "http://123.57.23.48", 39002, 1);
        String text = "我是任正非，我创立的公司叫华为。";
        String[] strings = text.split(pattern);
        for (String string : strings) {
//            System.out.println(string);
            if (string.trim().length() == 0)
                continue;
            if (string.length() > 128)
                continue;
            Annotation document = new Annotation(string);
            // run all Annotators on this text
            pipeline.annotate(document);
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

            if (sentences != null) {
                for (CoreMap sentence : sentences) {
                    // this is the parse tree of the current sentence
//            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
//            System.out.println(tree.toString());
//            System.out.println(sentence.toShorterString("Text"));
                    String conll = ConnUtil.getConnStr(sentence);
                    if (conll == null) {
                        continue;
                    }
                    System.out.println(conll);
                    System.out.println();
                }
            }
        }
    }
}
