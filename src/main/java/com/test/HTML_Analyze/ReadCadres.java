package com.test.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ReadCadres {

    @Nullable
    public static JSONObject readCadres(final Document doc) {
        JSONObject output = new JSONObject();

        try {
            Elements tables = doc.getElementsByTag("table");

            JSONArray detailJSON = new JSONArray();
            output.put("detail", detailJSON);
            Elements detailRaw = tables.get(2).getElementsByTag("tr");
            for (int i = 1; i < detailRaw.size(); i++) {
                JSONObject tmp = new JSONObject();
                detailJSON.put(tmp);

                Elements children = detailRaw.get(i).children();

                tmp.put("semester", Integer.parseInt(children.get(0).text().trim().split("學年")[0]));
                tmp.put("semester2", children.get(1).text().trim().equals("第一學期") ? 1 : 2);
                tmp.put("name", children.get(2).text().trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }
}
