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
    static String pattern = "[.。]|[!?！？\n]+";

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
        AbstractParser pcfgParser = new PCFGParser();
        long lineCount = 0L;
        try {
            srParser.init();
            pcfgParser.init();
            reader = IOUtils.readerFromString(input, "utf-8");
            writer = IOUtils.getPrintWriter(output, "utf-8");
            String line = null;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount % 100 == 0)
                    logger.info("已经处理" + lineCount + "行记录");
                if (StringUtils.isEmpty(line)) {
                    continue;
                }
                line = line.trim();
                if (line.length() == 0)
                    continue;
                if (line.length() > 40)
                    continue;
                long time1 = System.currentTimeMillis();
                String result1 = srParser.parseContent(line);
                long time2 = System.currentTimeMillis();
                String result2 = pcfgParser.parseContent(line);
                long time3 = System.currentTimeMillis();
//                    System.out.println((time2 - time1) + " vs " + (time3 - time2));
                if (result1 != null && result1.equals(result2)) {
                    writer.println(result1);
                    writer.println();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeIgnoringExceptions(reader);
            IOUtils.closeIgnoringExceptions(writer);
            srParser.close();
            pcfgParser.close();
        }
    }

}
