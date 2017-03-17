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

/**
 * Created by jiajianchao on 2017/3/17.
 */
public class PreSolver {
    static Logger logger = Logger.getLogger(PreSolver.class);
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
        long allCount = 0l;
        long newsCount = 0l;
        try {
            reader = IOUtils.readerFromString(input, "utf-8");
            writer = IOUtils.getPrintWriter(output, "utf-8");
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (allCount % 1000 == 0) {
                    logger.info("共处理" + allCount + "行，其中新闻记数为：" + newsCount);
                }
                allCount++;
                if (StringUtils.isEmpty(line)) {
                    continue;
                }
                String[] values = StringUtils.splitPreserveAllTokens(line, "\t");
                if (values == null || values.length < 2) {
                    continue;
                }
                try {
                    JsonReader jsonReader = Json.createReader(new StringReader(values[1]));
                    JsonObject jsonObject = jsonReader.readObject();
                    String content = jsonObject.getString("Content");
                    String host = jsonObject.getString("SiteHost");
                    String url = jsonObject.getString("URL");
                    jsonReader.close();
                    if (host == null || url == null || host.contains("blog") || url.contains("blog")) {
                        continue;
                    }
                    newsCount++;
                    String[] strings = content.split(pattern);
                    for (String string : strings) {
                        writer.println(string);
                    }
                } catch (Exception e1) {
                    logger.error(e1.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeIgnoringExceptions(reader);
            IOUtils.closeIgnoringExceptions(writer);
        }
    }
}
