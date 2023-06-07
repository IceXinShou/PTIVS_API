package com.test.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static com.test.HTML_Analyze.Util.dateConvert;
import static com.test.HTML_Analyze.Util.getInt;

public class ReadHistoryRewards {
    private static final String[] PREVIEW_LABEL = {"年度", "學期", "大功", "小功", "嘉獎", "優點", "大過", "小過", "警告", "缺點"};
    private static final String[] REWARD_DETAIL_LABEL = {"簽呈日期", "發生日期", "事由", "獎勵", "大功", "小功", "嘉獎", "優點", "加分", "功過狀態", "銷過日期", "備註"};
    private static final String[] PUNISH_DETAIL_LABEL = {"簽呈日期", "發生日期", "事由", "懲罰", "大過", "小過", "警告", "缺點", "扣分", "功過狀態", "銷過日期", "備註"};

    @Nullable
    public static JSONObject readHistoryRewards(final Document doc) {
        JSONObject output = new JSONObject();

        try {
            Elements tables = doc.getElementsByTag("table");

            // put preview data
            JSONArray previewJSON = new JSONArray();
            output.put("統計", previewJSON);

            Elements previewRaw = tables.get(2).getElementsByTag("tr");
            for (int i = 1; i < previewRaw.size() - 3; ++i) {
                JSONObject tmp = new JSONObject();
                previewJSON.put(tmp);

                Elements children = previewRaw.get(i).children();
                for (int j = 0; j < 10; ++j) {
                    tmp.put(PREVIEW_LABEL[j], Integer.parseInt(children.get(j).text().trim()));
                }
            }

            // put reward detail
            int detailPos; // TODO: detailPos is not correct from both of reward and punish detail
            JSONArray rewardDetailJSON = new JSONArray();
            output.put("獎勵明細", rewardDetailJSON);
            Elements detailRaw = tables.get(3).getElementsByTag("tr");
            for (detailPos = 2; detailPos < detailRaw.size() - 3; ++detailPos) {
                JSONObject tmp = new JSONObject();
                rewardDetailJSON.put(tmp);

                Elements children = detailRaw.get(detailPos).children();
                if (children.size() == 1) break;

                for (int i = 0; i < 12; ++i) {
                    switch (i) {
                        case 0, 1 -> {
                            tmp.put(REWARD_DETAIL_LABEL[i], dateConvert(children.get(i).text().trim().split("[年月日]")));
                        }
                        case 2, 3, 11 -> {
                            tmp.put(REWARD_DETAIL_LABEL[i], children.get(i).text().trim());
                        }
                        case 4, 5, 6, 7, 8 -> {
                            tmp.put(REWARD_DETAIL_LABEL[i], getInt(children.get(i).text().trim()));
                        }
                    }
                }
            }

            JSONArray punishDetailJSON = new JSONArray();
            output.put("懲罰明細", punishDetailJSON);
            for (detailPos += 2; detailPos < detailRaw.size(); ++detailPos) {
                JSONObject tmp = new JSONObject();
                punishDetailJSON.put(tmp);

                Elements children = detailRaw.get(detailPos).children();
                if (children.size() == 1) break;

                for (int i = 0; i < 12; ++i) {
                    switch (i) {
                        case 0, 1 -> {
                            tmp.put(PUNISH_DETAIL_LABEL[i], dateConvert(children.get(i).text().trim().split("[年月日]")));
                        }
                        case 2, 3, 11 -> {
                            tmp.put(PUNISH_DETAIL_LABEL[i], children.get(i).text().trim());
                        }
                        case 4, 5, 6, 7, 8 -> {
                            tmp.put(PUNISH_DETAIL_LABEL[i], getInt(children.get(i).text().trim()));
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return new JSONObject().put("歷年獎懲", output);
    }
}
