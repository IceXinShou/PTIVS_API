package com.test.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static com.test.HTML_Analyze.Util.dateConvert;
import static com.test.HTML_Analyze.Util.getInt;

public class ReadHistoryRewards {

    @Nullable
    public static JSONObject readHistoryRewards(final Document doc) {
        JSONObject output = new JSONObject();

        try {
            Elements tables = doc.getElementsByTag("table");

            // put preview data
            JSONArray previewJSON = new JSONArray();
            output.put("preview", previewJSON);

            Elements previewRaw = tables.get(2).getElementsByTag("tr");
            for (int i = 1; i < previewRaw.size() - 3; ++i) {
                JSONObject tmp = new JSONObject();
                Elements children = previewRaw.get(i).children();

                tmp.put("semester1", Integer.parseInt(children.get(0).text().trim()));
                tmp.put("semester2", Integer.parseInt(children.get(1).text().trim()));
                tmp.put("major_merit", getInt(children.get(2).text().trim()));
                tmp.put("minor_merit", getInt(children.get(3).text().trim()));
                tmp.put("commendation", getInt(children.get(4).text().trim()));
                tmp.put("pros", getInt(children.get(5).text().trim()));
                tmp.put("major_demerit", getInt(children.get(6).text().trim()));
                tmp.put("minor_demerit", getInt(children.get(7).text().trim()));
                tmp.put("admonition", getInt(children.get(8).text().trim()));
                tmp.put("cons", getInt(children.get(9).text().trim()));

                previewJSON.put(tmp);
            }

            // put reward detail
            int detailPos;
            JSONArray rewardDetailJSON = new JSONArray();
            output.put("reward_detail", rewardDetailJSON);
            Elements detailRaw = tables.get(3).getElementsByTag("tr");
            for (detailPos = 2; detailPos < detailRaw.size() - 3; ++detailPos) {
                JSONObject tmp = new JSONObject();
                rewardDetailJSON.put(tmp);

                Elements children = detailRaw.get(detailPos).children();
                if (children.size() == 1) break;

                tmp.put("recording_date", dateConvert(children.get(0).text().trim().split("[年月日]")));
                tmp.put("occurence_date", dateConvert(children.get(1).text().trim().split("[年月日]")));
                tmp.put("content", children.get(2).text().trim());
                tmp.put("award", children.get(3).text().trim());
                tmp.put("major_merit", getInt(children.get(4).text().trim()));
                tmp.put("minor_merit", getInt(children.get(5).text().trim()));
                tmp.put("commendation", getInt(children.get(6).text().trim()));
                tmp.put("pros", getInt(children.get(7).text().trim()));
                tmp.put("extra_grade", getInt(children.get(8).text().trim()));
                tmp.put("remark", children.get(11).text().trim());
            }

            JSONArray punishDetailJSON = new JSONArray();
            output.put("punishment_detail", punishDetailJSON);
            for (detailPos += 2; detailPos < detailRaw.size(); ++detailPos) {
                JSONObject tmp = new JSONObject();
                punishDetailJSON.put(tmp);
                Elements children = detailRaw.get(detailPos).children();
                if (children.size() == 1) {
                    // no punished
                    break;
                }
                tmp.put("recording_date", dateConvert(children.get(0).text().trim().split("[年月日]")));
                tmp.put("occurence_date", dateConvert(children.get(1).text().trim().split("[年月日]")));
                tmp.put("content", children.get(2).text().trim());
                tmp.put("penalty", children.get(3).text().trim());
                tmp.put("major_demerit", getInt(children.get(4).text().trim()));
                tmp.put("minor_demerit", getInt(children.get(5).text().trim()));
                tmp.put("admonition", getInt(children.get(6).text().trim()));
                tmp.put("cons", getInt(children.get(7).text().trim()));
                tmp.put("deduct_grade", getInt(children.get(8).text().trim()));
                tmp.put("remark", children.get(11).text().trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }
}
