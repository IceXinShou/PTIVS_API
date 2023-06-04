package com.test.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static com.test.HTML_Analyze.Util.dateConvert;

public class ReadPunishedCancelLog {

    @Nullable
    public static JSONObject readPunishedCancelLog(final Document doc) {
        JSONObject output = new JSONObject();

        try {
            Elements tables = doc.getElementsByTag("table");

            JSONArray detailARY = new JSONArray();
            output.put("detail", detailARY);

            Elements detailRaw = tables.get(1).getElementsByTag("tr");
            for (int i = 2; i < detailRaw.size(); i++) {
                Elements td = detailRaw.get(i).children();
                // no log
                if (td.size() == 1) continue;

                JSONObject cur = new JSONObject();
                detailARY.put(cur);

                cur.put("cancel_time", dateConvert(td.get(0).text().trim().split("[年月日]")));
                cur.put("status", td.get(1).text().trim());
                cur.put("occur_time", dateConvert(td.get(2).text().trim().split("[年月日]")));
                cur.put("description", td.get(3).text().trim());
                cur.put("type", td.get(4).text().trim());
                cur.put("major", Integer.parseInt(td.get(5).text().trim()));
                cur.put("minor", Integer.parseInt(td.get(6).text().trim()));
                cur.put("commendation_admonition", Integer.parseInt(td.get(7).text().trim()));
                cur.put("point", Integer.parseInt(td.get(8).text().trim()));
            }


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }
}
