package com.test.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static com.test.HTML_Analyze.Util.dateConvert;
import static com.test.HTML_Analyze.Util.getInt;

public class ReadRewards {
    private static final String[] PREVIEW_LABEL = {"年度", "學期", "大功", "小功", "嘉獎", "優點", "大過", "小過", "警告", "缺點"};
    private static final String[] REWARD_DETAIL_LABEL = {"大功", "小功", "嘉獎", "優點", "加分"};
    private static final String[] PUNISH_DETAIL_LABEL = {"大過", "小過", "警告", "缺點", "扣分"};

    @Nullable
    public static JSONObject readRewards(final Document doc) {
        JSONObject output = new JSONObject();

        try {
            Elements tables = doc.getElementsByTag("table");

            Elements e_total = tables.get(2).getElementsByTag("tr").get(1).children();
            JSONObject total = new JSONObject();
            output.put("獎懲統計", total);
            for (int i = 0; i < 10; i++) {
                total.put(PREVIEW_LABEL[i], e_total.get(i).text().trim());
            }

            Elements e_reward_detail = tables.get(3).getElementsByTag("tr");
            JSONArray reward_detail = new JSONArray();
            output.put("獎勵明細", reward_detail);
            for (int i = 2; i < e_reward_detail.size(); i++) {
                Elements cur = e_reward_detail.get(i).children();
                if (cur.size() == 1) continue;
                JSONObject data = new JSONObject();
                reward_detail.put(data);
                data.put("簽呈日期", dateConvert(cur.get(0).text().trim().split("[年月日]")));
                data.put("發生日期", dateConvert(cur.get(1).text().trim().split("[年月日]")));
                data.put("事由", cur.get(2).text().trim());
                data.put("獎勵", cur.get(3).text().trim());
                data.put("銷過狀態", cur.get(9).text().trim());
                data.put("銷過日期", cur.get(10).text().trim());
                data.put("備註", cur.get(11).text().trim());

                for (int j = 4; j < 9; ++j) {
                    data.put(REWARD_DETAIL_LABEL[j - 4], getInt(cur.get(j).text().trim()));
                }
            }

            Elements e_punish_detail = tables.get(4).getElementsByTag("tr");
            JSONArray punish_detail = new JSONArray();
            output.put("懲罰明細", punish_detail);
            for (int i = 2; i < e_punish_detail.size(); i++) {
                Elements cur = e_punish_detail.get(i).children();
                if (cur.size() == 1) continue;
                JSONObject data = new JSONObject();
                punish_detail.put(data);
                data.put("簽呈日期", dateConvert(cur.get(0).text().trim().split("[年月日]")));
                data.put("發生日期", dateConvert(cur.get(1).text().trim().split("[年月日]")));
                data.put("事由", cur.get(2).text().trim());
                data.put("懲罰", cur.get(3).text().trim());
                data.put("備註", cur.get(9).text().trim());

                for (int j = 4; j < 9; ++j) {
                    data.put(PUNISH_DETAIL_LABEL[j - 4], getInt(cur.get(j).text().trim()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }
}
