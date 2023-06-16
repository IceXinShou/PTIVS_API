package tw.xserver.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ReadHistoryScore {

    @Nullable
    public static JSONObject readHistoryScore(final Document doc) {
        JSONArray output = new JSONArray();

        try {
            Elements tables = doc.getElementsByTag("table");

            Elements scoreData = tables.get(2).getElementsByIndexEquals(0).get(0).getElementsByTag("table");
            for (int i = 0; i < scoreData.size(); i += 2) {
                JSONObject curObj = new JSONObject();
                output.put(curObj);

                JSONArray subject = new JSONArray();
                curObj.put("科目名稱", subject);

                Elements tr = scoreData.get(i).getElementsByTag("tr");
                Elements tr_sum = scoreData.get(i + 1).getElementsByTag("tr");
                Elements credits_tr1 = tr_sum.get(3).children();
                Elements credits_tr2 = tr_sum.get(5).children();
                Elements rank_tr = tr_sum.get(7).children();
                Elements teacherResponse1_tr = tr_sum.get(9).children();
                Elements teacherResponse2_tr = tr_sum.get(13).children();
                String[] rankTmp = rank_tr.get(1).text().trim().split(" ");
                String[] semesterStr = tr.get(0).text().trim().split(" "); // "110 學年度 第 1 學期"

                // add subjects
                for (int j = 2; j < tr.size(); ++j) {
                    Elements td = tr.get(j).children();
                    JSONObject curSub = new JSONObject();
                    subject.put(curSub);

                    String grade = td.get(9).text().trim();
                    if (grade.startsWith("C")) {
                        curSub.put("重修", true);
                        grade = grade.substring(1);
                    } else curSub.put("重修", false);

                    String rank = td.get(11).text().trim();
                    if (rank.equals("")) rank = "-1";

                    curSub.put("科目名稱", td.get(0).text().trim().substring(2));
                    curSub.put("必修", td.get(1).text().trim().equals("必修"));
                    curSub.put("屬性", td.get(5).text().trim());
                    curSub.put("課程類別", td.get(6).text().trim());
                    curSub.put("科目屬性", td.get(7).text().trim());
                    curSub.put("學分數", Integer.parseInt(td.get(8).text().trim()));
                    curSub.put("學期成績", Integer.parseInt(grade));
                    curSub.put("學分取得", td.get(10).text().trim().equals("Y"));
                    curSub.put("班級排名", Integer.parseInt(rank));
                    curSub.put("排名人數", Integer.parseInt(td.get(12).text().trim()));
                    curSub.put("班級平均", Double.parseDouble(td.get(13).text().trim()));
                }

                curObj.put("年度", Integer.parseInt(semesterStr[0]));
                curObj.put("學期", Integer.parseInt(semesterStr[3]));
                curObj.put("修習學分數", Integer.parseInt(credits_tr1.get(0).text().trim()));
                curObj.put("必修學分數", Integer.parseInt(credits_tr1.get(1).text().trim()));
                curObj.put("選修學分數", Integer.parseInt(credits_tr1.get(2).text().trim()));
                curObj.put("實得修習學分數", Integer.parseInt(credits_tr2.get(0).text().trim()));
                curObj.put("實得必修學分數", Integer.parseInt(credits_tr2.get(1).text().trim()));
                curObj.put("實得選修學分數", Integer.parseInt(credits_tr2.get(2).text().trim()));
                curObj.put("學業成績", Double.parseDouble(rank_tr.get(0).text().trim()));
                curObj.put("學業班級排名", Integer.parseInt(rankTmp[0]));
                curObj.put("PR", 100 - Double.parseDouble(rankTmp[1].substring(1, rankTmp[1].length() - 1)));
                curObj.put("綜合表現", teacherResponse1_tr.get(0).text().trim());
                curObj.put("具體建議", teacherResponse2_tr.get(0).text().trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return new JSONObject().put("歷年學期成績", output);
    }
}
