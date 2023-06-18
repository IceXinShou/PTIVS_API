package tw.xserver.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static tw.xserver.Util.getInt;

public class ReadClubs {
    @Nullable
    public static JSONObject read(final Document doc) {
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
                tmp.put("社團名稱", children.get(2).text().trim());
                tmp.put("社團組別", children.get(3).text().trim());
                tmp.put("擔任職位", children.get(5).text().trim());
                tmp.put("社團成績", getInt(children.get(6).text().trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return new JSONObject().put("參與社團", output);
    }
}
