package tw.xserver.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ReadScore {

    @Nullable
    public static JSONObject read(final Document doc) {
        JSONObject output = new JSONObject();

        try {
            Elements tables = doc.getElementsByTag("table");

            JSONArray detailJSON = new JSONArray();
            output.put("detail", detailJSON);

            //  Elements scoreData = tables.get(2).getElementsByIndexEquals(0).get(0).getElementsByTag("table");

            System.out.println(tables.get(2));

            //  for (int i = 0; i < scoreData.size(); i += 2) {
            //  JSONObject curObj = new JSONObject();
            //  detailJSON.put(curObj);
            //
            //  JSONArray subject = new JSONArray();
            //  curObj.put("subject", subject);
            //
            //  Elements tr = scoreData.get(i).getElementsByTag("tr");
            //  Elements tr_sum = scoreData.get(i + 1).getElementsByTag("tr");
            //  Elements credits_tr1 = tr_sum.get(3).children();
            //  Elements credits_tr2 = tr_sum.get(5).children();
            //  Elements rank_tr = tr_sum.get(7).children();
            //  Elements teacherResponse1_tr = tr_sum.get(9).children();
            //  Elements teacherResponse2_tr = tr_sum.get(13).children();
            //  String[] rankTmp = rank_tr.get(1).text().trim().split(" ");
            //
            //  String[] semesterStr = tr.get(0).text().trim().split(" "); // "110 學年度 第 1 學期"
            //  curObj.put("semester", Integer.parseInt(semesterStr[0]));
            //  curObj.put("semester2", Integer.parseInt(semesterStr[3]));
            //  curObj.put("try_credit", Integer.parseInt(credits_tr1.get(0).text().trim()));
            //  curObj.put("try_mandatory_credit", Integer.parseInt(credits_tr1.get(1).text().trim()));
            //  curObj.put("try_elective_credit", Integer.parseInt(credits_tr1.get(2).text().trim()));
            //  curObj.put("get_credit", Integer.parseInt(credits_tr2.get(0).text().trim()));
            //  curObj.put("get_mandatory_credit", Integer.parseInt(credits_tr2.get(1).text().trim()));
            //  curObj.put("get_elective_credit", Integer.parseInt(credits_tr2.get(2).text().trim()));
            //  curObj.put("final_grade", Double.parseDouble(rank_tr.get(0).text().trim()));
            //  curObj.put("rank", Integer.parseInt(rankTmp[0]));
            //  curObj.put("pr", 100 - Double.parseDouble(rankTmp[1].substring(1, rankTmp[1].length() - 1)));
            //  curObj.put("overall", teacherResponse1_tr.get(0).text().trim());
            //  curObj.put("recommendation", teacherResponse2_tr.get(0).text().trim());
            //
            //  add subjects
            //  for (int j = 2; j < tr.size(); ++j) {
            //      Elements td = tr.get(j).children();
            //      JSONObject curSub = new JSONObject();
            //      subject.put(curSub);
            //
            //   String grade = td.get(9).text().trim();
            //   if (grade.startsWith("C")) {
            //       curSub.put("retry", true);
            //       grade = grade.substring(1);
            //   } else curSub.put("retry", false);
            //
            //   String rank = td.get(11).text().trim();
            //   if (rank.equals("")) rank = "-1";
            //
            //   curSub.put("name", td.get(0).text().trim().substring(2));
            //   curSub.put("mandatory", td.get(1).text().trim().equals("必修"));
            //   curSub.put("attr", td.get(5).text().trim());
            //   curSub.put("type", td.get(6).text().trim());
            //   curSub.put("attr2", td.get(7).text().trim());
            //   curSub.put("credit", Integer.parseInt(td.get(8).text().trim()));
            //   curSub.put("grade", Integer.parseInt(grade));
            //   curSub.put("get", td.get(10).text().trim().equals("Y"));
            //    curSub.put("rank", Integer.parseInt(rank));
            //    curSub.put("c_students", Integer.parseInt(td.get(12).text().trim()));
            //    curSub.put("c_average", Double.parseDouble(td.get(13).text().trim()));
            //        }
            //    }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }
}
