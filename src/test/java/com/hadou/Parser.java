package com.hadou;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.parser.shiftreduce.PerceptronModel;
import edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser;
import edu.stanford.nlp.parser.shiftreduce.Weight;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by jiajianchao on 2017/3/6.
 */
public class Parser {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        PrintWriter writer=new PrintWriter(new OutputStreamWriter(new FileOutputStream("D:\\workspace\\idea_workspace\\nlp\\src\\main\\parseFetures.txt"),"utf-8"));
        ShiftReduceParser parser = IOUtils.readObjectFromURLOrClasspathOrFileSystem("edu/stanford/nlp/models/srparser/chineseSR.ser.gz");
        PerceptronModel model= (PerceptronModel) parser.model;
        Map<String, Weight> featureWeights=model.featureWeights;
        for (Map.Entry<String, Weight> stringWeightEntry : featureWeights.entrySet()) {
            writer.println(stringWeightEntry.getKey()+"\t"+stringWeightEntry.getValue().toString());
        }
        writer.close();
    }
}
