package com.test.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ReadHistoryAbsent {

    @Nullable
    public static JSONObject readHistoryAbsent(final Document doc) {
        JSONObject output = new JSONObject();

        try {
            Elements tables = doc.getElementsByTag("table");
            System.out.println(tables);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return new JSONObject().put("歷年缺曠", output);
    }
}
