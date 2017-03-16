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
        props.setProperty("annotators", "tokenize,  pos, lemma, ner, parse");
        StanfordCoreNLPClient pipeline = new CustomerCoreNLPClient(props, "http://123.57.23.48", 39000, 1);
        String text = "城建成为外商投资青海新热点\n" +
                "新华社西宁十二月二十一日电\n" +
                "制约吸引外资的城市基础设施建设，如今却被外商看好，成为继资源开发之后青海集中利用外资的新领域。\n" +
                "西宁市城市基础设施建设长期投入不足，从新中国成立到一九九五年的四十六年间，全部投入仅四亿元左右，城市建设滞后制约了经济的发展。\n" +
                "近两年一批外商先后表示了涉足西宁城建的愿望。\n" +
                "青海省政府因势利导，提出基础设施商品化的城建思路，并于今年初批准了《西宁市鼓励引导外商投资的若干规定》。\n" +
                "西宁市东出口道路经营权实行有偿转让的决定出台后，立即有十多家外商前来洽谈，最后以五千万元的标价敲定。\n" +
                "按现代城市功能要求设计的莫家街旧城改造工程，由港商投资五千万元独家承建。\n" +
                "第六水源新建工程利用外资近两千万元，供水能力可达十五万吨日，将极大地缓解西宁市的供水紧张状况。\n" +
                "城市北出口道路建设工程已与香港泰华公司达成建设协议，投资约需一点八亿元，南绕城快速路工程也有数家外商前来洽谈投资。";
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
                    System.out.println(ConnUtil.getConnStr(sentence));
                    System.out.println();
                }
            }
        }
    }
}
