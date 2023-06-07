package com.test.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


/*
    {
      "detail": [
        {
          "name": "無擔任",
          "semester": 110,
          "semester2": 1
        },
        {
          "name": "無擔任",
          "semester": 110,
          "semester2": 2
        },
        {
          "name": "無擔任",
          "semester": 111,
          "semester2": 1
        },
        {
          "name": "資訊股長",
          "semester": 111,
          "semester2": 2
        },
        {
          "name": "",
          "semester": 112,
          "semester2": 1
        }
      ]
    }
 */

public class ReadCadres {

    @Nullable
    public static JSONObject readCadres(final Document doc) {
        JSONArray output = new JSONArray();

        try {
            Elements tables = doc.getElementsByTag("table");

            Elements detailRaw = tables.get(2).getElementsByTag("tr");
            for (int i = 1; i < detailRaw.size(); i++) {
                JSONObject tmp = new JSONObject();
                output.put(tmp);

                Elements children = detailRaw.get(i).children();

                tmp.put("年度", Integer.parseInt(children.get(0).text().trim().split("學年")[0]));
                tmp.put("學期", children.get(1).text().trim().equals("第一學期") ? 1 : 2);
                tmp.put("擔任幹部", children.get(2).text().trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return new JSONObject().put("擔任幹部", output);
    }
}
