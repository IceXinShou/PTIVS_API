package tw.xserver.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static tw.xserver.HTML_Analyze.Util.dateConvert;

public class ReadAbsent {

    private static final String[] PREVIEW_LABEL = {"年度", "學期", "曠課", "午曠", "環曠", "事假", "病假", "公假", "喪假", "防疫假"};
    private static final String[] CLASS_LABEL = {"1", "2", "3", "4", "午休", "5", "6", "7", "環境打掃", "輔導課"};


    @Nullable
    public static JSONObject readAbsent(final Document doc) {
        JSONObject output = new JSONObject();

        try {
            Elements tables = doc.getElementsByTag("table");
            Elements e_preview = tables.get(2).getElementsByTag("tr").get(1).children();
            JSONObject preview = new JSONObject();
            output.put("統計", preview);

            preview.put(PREVIEW_LABEL[0], Integer.parseInt(e_preview.get(0).text().trim()));
            preview.put(PREVIEW_LABEL[1], Integer.parseInt(e_preview.get(1).text().trim()));
            for (int i = 2; i < 10; i++) {
                preview.put(PREVIEW_LABEL[i], e_preview.get(i).text().trim());
            }

            Elements e_detail = tables.get(4).getElementsByTag("tr");
            JSONArray detail = new JSONArray();
            output.put("資料表", detail);
            for (int i = 1; i < e_detail.size(); ++i) {
                Elements children = e_detail.get(i).children();
                JSONObject data = new JSONObject();
                detail.put(data);

                data.put("日期", dateConvert(children.get(2).text().trim().split("/")));

                for (int j = 3; j < 13; ++j) {
                    if (children.get(j).text().trim().equals("0")) continue;
                    data.put(CLASS_LABEL[j - 3], children.get(j).text().trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return new JSONObject().put("學期缺曠", output);
    }


}
