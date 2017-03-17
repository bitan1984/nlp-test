package com.hadou;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.JSONOutputter;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by jiajianchao on 2017/2/28.
 */
public class PipelineTest {
    public static void main(String[] args) throws IOException {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        BufferedReader reader = null;
        reader=IOUtils.readerFromString("StanfordCoreNLP-chinese.properties");
        props.load(reader);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // read some text in the text variable
        String text = "国务院总理李克强调研上海外高桥时提出，支持上海积极探索新机制。";
//        text="我44岁的时候，在经营中被骗了200万，被国企南油集团除名，曾求留任遭拒绝，还背负还清200万债。妻子又和我离了婚，我带着老爹老娘弟弟妹妹在深住棚屋，创立公司。我没有资本、没有人脉、没有资源、没有技术、没有市场经验，我唯有勇敢向前，我用了27年把公司带到世界500强，行业世界第一的位置。我不觉得跌倒可怕，可怕的是再也站不起来！ 1944年，我出生于贵州安顺地区镇宁县一个贫困山区的小村庄，靠近黄果树瀑布。我的父母是乡村中学教师，家中还有兄妹6人，中、小学就读于贵州边远山区的少数民族县城。因为父母对知识的重视和追求，即使在三年自然灾害时期，我的父母仍然坚持让孩子读书。 1963年，我就读于重庆建筑工程学院（已并入重庆大学），还差一年毕业的时候，“文化大革命”开始了。父亲被关进了牛棚，因挂念挨批斗的父亲，我扒火车回家看望父亲。父亲嘱咐我要不断学习。回到重庆后，我把电子计算机、数字技术、自动控制等专业技术自学完，把樊映川的高等数学习题集从头到尾做了两遍，接着学习了许多逻辑、哲学。自学了三门外语，当时已到可以阅读大学课本的程度。 大学毕业后我去当兵了，当的是建筑兵。当兵的第一个工程就是法国公司的工程。那时法国德布尼斯.斯贝西姆公司向中国出售了一个化纤成套设备，在中国的东北辽阳市。我在那里从这个工程开始一直到建完生产，然后才离开。 1983年，随国家整建制撤销基建工程兵，我复员转业至深圳南海石油后勤服务基地。 1987年，因工作不顺利，我转而集资21000元人民币创立公司。创立初期，靠代理香港某公司的程控交换机获得了第一桶金。那年，我44岁。 1991年9月，我们租下了深圳宝安县蚝业村工业大厦三楼作为研制程控交换机的场所，五十多名年轻员工跟随我来到这栋破旧的厂房中，开始了他们充满艰险和未知的创业之路，他们把整层楼分隔为单板、电源、总测、准备四个工段，外加库房和厨房。人们在机器的高温下挥汗如雨夜以继日地作业，设计制作电路板、话务台、焊接的电路板，编写软件，调试、修改、再调试。在这样的情况下，我几乎每天都到现场检查生产及开发进度，开会研究面临的困难，分工协调解决各式各样的问题。遇到吃饭时间，我们就在大排档同大家聚餐，由其中职位最高的人自掏腰包请大家吃饭。后来，我们总部搬到了深圳龙岗坂田工业园。我们熬过了创业的艰苦岁月。 1992年我们孤注一掷投入C&C08机的研发。 1993年年末，C&C08交换机终于研发成功。其价格比国外同类产品低三分之二，为我们占领了市场。 1996年3月，为了和南斯拉夫洽谈合资项目，我率领一个十多人的团队入住贝尔格莱德的香格里拉。他们订了一间总统套房，每天房费约2000美元。不过，房间并非我独享，而是大家一起打地铺休息。 2007年我们合同销售额160亿美元，其中海外销售额115亿美元，并且是当年中国国内电子行业营利和纳税第一。截至到2008年底，我们在国际市场上覆盖100多个国家和地区，全球排名前50名的电信运营商中，已有45家使用我们的产品和服务。我们的产品和解决方案已经应用于全球170多个国家，服务全球运营商50强中的45家及全球1/3的人口。 2014年《财富》世界500强中我们排行全球第285位，与上年相比上升三十位。 2014年10月9日，Interbrand在纽约发布的“最佳全球品牌”排行榜中，我们以排名94的成绩出现在榜单之中，这也是中国大陆首个进入Interbrand top100榜单的企业公司。 27年来，曾经山寨公司变成了震惊世界的科技王国。 企业繁花似锦的时候却说这很可能是企业的“寒冬”，企业寒冬之时又可能孕育新的希望。 我是任正非，我创立的公司叫华为。 作者：任正非 来源：电商报";

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);
//        JSONOutputter.jsonPrint(document,System.out);
        // these are all the sentences in this document
// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
//            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
//                // this is the text of the token
//                String word = token.get(TextAnnotation.class);
//                // this is the POS tag of the token
//                String pos = token.get(PartOfSpeechAnnotation.class);
//                // this is the NER label of the token
//                String ne = token.get(NamedEntityTagAnnotation.class);
//                System.out.println("word:" + word + " pos:" + pos + " ne:" + ne);
//            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            System.out.println(tree.toString());

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            System.out.println(dependencies.toString());
        }

// This is the coreference link graph
// Each chain stores a set of mentions that link to each other,
// along with a method for getting the most representative mention
// Both sentence and token offsets start at 1!
//        Map<Integer, CorefChain> graph =
//                document.get(CorefChainAnnotation.class);
    }
}
