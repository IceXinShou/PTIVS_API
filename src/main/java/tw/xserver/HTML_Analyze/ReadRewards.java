package tw.xserver.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static tw.xserver.Util.dateConvert;
import static tw.xserver.Util.getInt;

public class ReadRewards {
    private static final String[] PREVIEW_LABEL = {"年度", "學期", "大功", "小功", "嘉獎", "優點", "大過", "小過", "警告", "缺點"};
    private static final String[] REWARD_DETAIL_LABEL = {"簽呈日期", "發生日期", "事由", "獎勵", "大功", "小功", "嘉獎", "優點", "加分", "銷過狀態", "銷過日期", "備註"};
    private static final String[] PUNISH_DETAIL_LABEL = {"簽呈日期", "發生日期", "事由", "懲罰", "大過", "小過", "警告", "缺點", "扣分", "備註"};

    @Nullable
    public static JSONObject read(final Document doc) {
        JSONObject output = new JSONObject();

        try {
            Elements tables = doc.getElementsByTag("table");

            Elements e_total = tables.get(2).getElementsByTag("tr").get(1).children();
            JSONObject total = new JSONObject();
            output.put("獎懲統計", total);

            total.put(PREVIEW_LABEL[0], Integer.parseInt(e_total.get(0).text().trim()));
            total.put(PREVIEW_LABEL[1], Integer.parseInt(e_total.get(1).text().trim()));
            for (int i = 2; i < 10; i++) {
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

                for (int j = 0; j < 12; j++) {
                    switch (j) {
                        case 0, 1 -> {
                            data.put(REWARD_DETAIL_LABEL[j], dateConvert(cur.get(j).text().trim().split("[年月日]")));
                        }
                        case 2, 3, 9, 10, 11 -> {
                            data.put(REWARD_DETAIL_LABEL[j], cur.get(j).text().trim());
                        }
                        default -> {
                            data.put(REWARD_DETAIL_LABEL[j], getInt(cur.get(j).text().trim()));
                        }
                    }
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

                for (int j = 0; j < 10; j++) {
                    switch (j) {
                        case 0, 1 -> {
                            data.put(PUNISH_DETAIL_LABEL[j], dateConvert(cur.get(j).text().trim().split("[年月日]")));
                        }
                        case 2, 3, 9 -> {
                            data.put(PUNISH_DETAIL_LABEL[j], cur.get(j).text().trim());
                        }
                        default -> {
                            data.put(PUNISH_DETAIL_LABEL[j], getInt(cur.get(j).text().trim()));
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return new JSONObject().put("學期獎懲", output);
    }
}
