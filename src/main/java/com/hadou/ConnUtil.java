package com.hadou;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by jiajianchao on 2017/3/16.
 */
public class ConnUtil {
    private static final String NULL_PLACEHOLDER = "_";

    public static String getConnStr(CoreMap sentence) {
        StringBuffer stringBuffer = new StringBuffer();
        if (sentence.get(CoreAnnotations.TokensAnnotation.class) != null) {
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            if(tokens.size()>40)
                return null;
            SemanticGraph depTree = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            for (int i = 0; i < tokens.size(); ++i) {
                // ^^ end nonsense to get tokens ^^

                // Newline if applicable
                if (i > 0) {
                    stringBuffer.append("\n");
                }

                // Try to get the incoming dependency edge
                int head = -1;
                String deprel = null;
                if (depTree != null) {
                    Set<Integer> rootSet = depTree.getRoots().stream().map(IndexedWord::index).collect(Collectors.toSet());
                    IndexedWord node = depTree.getNodeByIndexSafe(i + 1);
                    if (node != null) {
                        List<SemanticGraphEdge> edgeList = depTree.getIncomingEdgesSorted(node);
                        if (!edgeList.isEmpty()) {
                            assert edgeList.size() == 1;
                            head = edgeList.get(0).getGovernor().index();
                            deprel = edgeList.get(0).getRelation().toString();
                        } else if (rootSet.contains(i + 1)) {
                            head = 0;
                            deprel = "ROOT";
                        }
                    }
                }

                stringBuffer.append(line(i + 1, tokens.get(i), head, deprel));
            }
        }
        return stringBuffer.toString();
    }

    private static String line(int index,
                               CoreLabel token,
                               int head, String deprel) {
        ArrayList<String> fields = new ArrayList<>(16);

        fields.add(Integer.toString(index)); // 1
        fields.add(orNull(token.word()));    // 2
        fields.add(orNull(token.lemma()));   // 3
        fields.add(orNull(token.tag()));     // 4
        fields.add(orNull(token.tag()));     // 5
        fields.add(NULL_PLACEHOLDER);       //6
        if (head >= 0) {
            fields.add(Integer.toString(head));  // 7
            fields.add(deprel);                  // 8
        } else {
            fields.add(NULL_PLACEHOLDER);
            fields.add(NULL_PLACEHOLDER);
        }
        fields.add(NULL_PLACEHOLDER);   //9
        fields.add(NULL_PLACEHOLDER);   //10

        return StringUtils.join(fields, "\t");
    }

    private static String orNull(String in) {
        if (in == null) {
            return NULL_PLACEHOLDER;
        } else {
            return in;
        }
    }
}
