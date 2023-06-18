package tw.xserver.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ReadClassTable {

    private static final String[] dayTitles = new String[]{"星期一", "星期二", "星期三", "星期四", "星期五"};

    @Nullable
    public static JSONObject read(final Document doc) {
        JSONObject output = new JSONObject();

        try {
            Elements tables = doc.getElementsByTag("table");

            Elements detailRaw = tables.get(2).getElementsByTag("tr");

            for (String i : dayTitles)
                output.put(i, new JSONObject());

            for (int i = 1; i < detailRaw.size() - 1; i++) {
                Elements children = detailRaw.get(i).children();

                for (int j = 0; j < 5; j++) {
                    /* [微處理機, (必), 王彥盛, 資訊二甲] */
                    String[] class_split = children.get(j + 2).text().split(" ");

                    JSONObject curClass = new JSONObject("{\"空堂\": false}");
                    JSONObject curDay = output.getJSONObject(dayTitles[j]);
                    curDay.put(String.valueOf(i), curClass);

                    if (class_split.length <= 1) {
                        curClass.put("空堂", true);
                        continue;
                    }

                    curClass.put("課程名稱", class_split[0]);
                    curClass.put("必修", class_split[1].equals("(必)"));
                    curClass.put("授課教師", new JSONArray(class_split[2].split(",")));
                    curClass.put("上課教室", class_split[3]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return new JSONObject().put("課表查詢", output);
    }
}
