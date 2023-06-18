package tw.xserver.analyze_HTML;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static tw.xserver.Util.dateConvert;

public class ReadPunishedCancelLog {
    private static final String[] LABEL = {"銷功過日期", "功過狀態", "發生日期", "事由", "獎懲類別", "大功過", "小功過", "嘉獎警告", "優缺點"};

    @Nullable
    public static JSONObject read(final Document doc) {
        JSONArray output = new JSONArray();

        try {
            Elements tables = doc.getElementsByTag("table");

            Elements detailRaw = tables.get(1).getElementsByTag("tr");
            for (int i = 2; i < detailRaw.size(); i++) {
                Elements td = detailRaw.get(i).children();
                // no log
                if (td.size() == 1) continue;

                JSONObject cur = new JSONObject();
                output.put(cur);

                for (int j = 0; j < 9; j++) {
                    switch (j) {
                        case 0, 2 -> {
                            cur.put(LABEL[j], dateConvert(td.get(0).text().trim().split("[年月日]")));
                        }
                        case 1, 3, 4 -> {
                            cur.put(LABEL[j], td.get(j).text().trim());
                        }
                        default -> {
                            cur.put(LABEL[j], Integer.parseInt(td.get(j).text().trim()));
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return new JSONObject().put("銷過紀錄", output);
    }
}
