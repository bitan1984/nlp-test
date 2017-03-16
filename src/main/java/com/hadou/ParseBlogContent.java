package com.hadou;

import edu.stanford.nlp.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Map;

/**
 * Created by jiajianchao on 2017/3/15.
 */
public class ParseBlogContent {
    static Logger logger = Logger.getLogger(ParseBlogContent.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            logger.warn("请输入要处理的源文件和目标文件");
            return;
        }
        String input = args[0];
        String output = args[1];
        BufferedReader reader = null;
        PrintWriter writer = null;
        AbstractParser srParser = new SRParser();
        AbstractParser factoredParser = new FactoredParser();
        try {
            srParser.init();
            factoredParser.init();
            reader = IOUtils.readerFromString(input, "utf-8");
            writer = IOUtils.getPrintWriter(output, "utf-8");
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isEmpty(line)) {
                    continue;
                }
                String[] values = StringUtils.splitPreserveAllTokens(line, "\t");
                if (values == null || values.length < 2) {
                    continue;
                }
                JsonReader jsonReader = Json.createReader(new StringReader(values[1]));
                JsonObject jsonObject = jsonReader.readObject();
                String content = jsonObject.getString("Content");
                Map<String, String> result1 = srParser.parseContent(content);
                Map<String, String> result2 = factoredParser.parseContent(content);
                for (Map.Entry<String, String> stringStringEntry : result1.entrySet()) {
                    String text = stringStringEntry.getKey();
                    String parseTree = stringStringEntry.getValue();
                    if (parseTree.startsWith("(X")) {
                        continue;
                    }
                    String otherParseTree = result2.get(text);
                    if (otherParseTree != null && otherParseTree.equals(parseTree)) {
                        writer.println(text + "\t" + parseTree);
                    }
                }
                jsonReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeIgnoringExceptions(reader);
            IOUtils.closeIgnoringExceptions(writer);
            srParser.close();
            factoredParser.close();
        }
    }
}
