package com.test;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HTML_Analyze {


    public static @Nullable JSONObject readHistoryAbsent(final String responseContent) {
        JSONObject output = new JSONObject();

        try {
            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");
//            System.out.println(tables);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    public static @Nullable JSONObject readHistoryRewards(final String responseContent) {
        JSONObject output = new JSONObject();

        try {
            Document doc = Jsoup.parse(responseContent);
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

                tmp.put("recording_date", getTimeFormat(children.get(0).text().trim()));
                tmp.put("occurence_date", getTimeFormat(children.get(1).text().trim()));
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
                tmp.put("recording_date", getTimeFormat(children.get(0).text().trim()));
                tmp.put("occurence_date", getTimeFormat(children.get(1).text().trim()));
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

    public static @Nullable JSONObject readPunishedCancelLog(final String responseContent) {
        JSONObject output = new JSONObject();

        try {
            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");

            JSONArray detailARY = new JSONArray();
            output.put("detail", detailARY);

            Elements detailRaw = tables.get(1).getElementsByTag("tr");
            for (int i = 2; i < detailRaw.size(); i++) {
                Elements td = detailRaw.get(i).children();
                // no log
                if (td.size() == 1) continue;

                JSONObject cur = new JSONObject();
                detailARY.put(cur);

                cur.put("cancel_time", getTimeFormat(td.get(0).text().trim()));
                cur.put("status", td.get(1).text().trim());
                cur.put("occur_time", getTimeFormat(td.get(2).text().trim()));
                cur.put("description", td.get(3).text().trim());
                cur.put("type", td.get(4).text().trim());
                cur.put("major", Integer.parseInt(td.get(5).text().trim()));
                cur.put("minor", Integer.parseInt(td.get(6).text().trim()));
                cur.put("commendation_admonition", Integer.parseInt(td.get(7).text().trim()));
                cur.put("point", Integer.parseInt(td.get(8).text().trim()));
            }


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    public static @Nullable JSONObject readClubs(final String responseContent) {
        JSONObject output = new JSONObject();

        try {
            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");

            JSONArray detailJSON = new JSONArray();
            output.put("detail", detailJSON);
            Elements detailRaw = tables.get(2).getElementsByTag("tr");
            for (int i = 1; i < detailRaw.size(); i++) {
                JSONObject tmp = new JSONObject();
                detailJSON.put(tmp);

                Elements children = detailRaw.get(i).children();
                tmp.put("semester", Integer.parseInt(children.get(0).text().trim().split("學年")[0]));
                tmp.put("semester2", children.get(1).text().trim().equals("第一學期") ? 1 : 2);
                tmp.put("name", children.get(2).text().trim());
                tmp.put("group", children.get(3).text().trim());
                tmp.put("position", children.get(5).text().trim());
                tmp.put("score", getInt(children.get(6).text().trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    public static @Nullable JSONObject readCadres(final String responseContent) {
        JSONObject output = new JSONObject();

        try {
            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");

            JSONArray detailJSON = new JSONArray();
            output.put("detail", detailJSON);
            Elements detailRaw = tables.get(2).getElementsByTag("tr");
            for (int i = 1; i < detailRaw.size(); i++) {
                JSONObject tmp = new JSONObject();
                detailJSON.put(tmp);

                Elements children = detailRaw.get(i).children();

                tmp.put("semester", Integer.parseInt(children.get(0).text().trim().split("學年")[0]));
                tmp.put("semester2", children.get(1).text().trim().equals("第一學期") ? 1 : 2);
                tmp.put("name", children.get(2).text().trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    public static @Nullable JSONObject readClassTable(final String responseContent) {
        JSONObject output = new JSONObject();
        String[] dayTitles = new String[]{"星期一", "星期二", "星期三", "星期四", "星期五"};

        try {
            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");

            JSONObject detailJSON = new JSONObject();
            output.put("detail", detailJSON);
            Elements detailRaw = tables.get(2).getElementsByTag("tr");

            for (String i : dayTitles)
                detailJSON.put(i, new JSONObject());

            for (int i = 1; i < detailRaw.size() - 1; i++) {
                Elements children = detailRaw.get(i).children();

                for (int j = 0; j < 5; j++) {
                    /* [微處理機, (必), 王彥盛,, 資訊二甲] */
                    String[] class_split = children.get(j + 2).text().split(" ");

                    JSONObject curClass = new JSONObject("{\"empty\": false}");
                    JSONObject curDay = detailJSON.getJSONObject(dayTitles[j]);
                    curDay.put(String.valueOf(i), curClass);

                    if (class_split.length <= 1) {
                        curClass.put("empty", true);
                        continue;
                    }

                    curClass.put("name", class_split[0]);
                    curClass.put("require", class_split[1].equals("(必)"));
                    curClass.put("teacher", new JSONArray(class_split[2].split(",")));
                    curClass.put("place", class_split[3]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    public static @Nullable JSONObject readHistoryScore(final String responseContent) {
        JSONObject output = new JSONObject();

        try {

            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");

            JSONArray detailJSON = new JSONArray();
            output.put("detail", detailJSON);

            Elements scoreData = tables.get(2).getElementsByIndexEquals(0).get(0).getElementsByTag("table");
            for (int i = 0; i < scoreData.size(); i += 2) {
                JSONObject curObj = new JSONObject();
                detailJSON.put(curObj);

                JSONArray subject = new JSONArray();
                curObj.put("subject", subject);

                Elements tr = scoreData.get(i).getElementsByTag("tr");
                Elements tr_sum = scoreData.get(i + 1).getElementsByTag("tr");
                Elements credits_tr1 = tr_sum.get(3).children();
                Elements credits_tr2 = tr_sum.get(5).children();
                Elements rank_tr = tr_sum.get(7).children();
                Elements teacherResponse1_tr = tr_sum.get(9).children();
                Elements teacherResponse2_tr = tr_sum.get(13).children();
                String[] rankTmp = rank_tr.get(1).text().trim().split(" ");

                String[] semesterStr = tr.get(0).text().trim().split(" "); // "110 學年度 第 1 學期"
                curObj.put("semester", Integer.parseInt(semesterStr[0]));
                curObj.put("semester2", Integer.parseInt(semesterStr[3]));
                curObj.put("try_credit", Integer.parseInt(credits_tr1.get(0).text().trim()));
                curObj.put("try_mandatory_credit", Integer.parseInt(credits_tr1.get(1).text().trim()));
                curObj.put("try_elective_credit", Integer.parseInt(credits_tr1.get(2).text().trim()));
                curObj.put("get_credit", Integer.parseInt(credits_tr2.get(0).text().trim()));
                curObj.put("get_mandatory_credit", Integer.parseInt(credits_tr2.get(1).text().trim()));
                curObj.put("get_elective_credit", Integer.parseInt(credits_tr2.get(2).text().trim()));
                curObj.put("final_grade", Double.parseDouble(rank_tr.get(0).text().trim()));
                curObj.put("rank", Integer.parseInt(rankTmp[0]));
                curObj.put("pr", 100 - Double.parseDouble(rankTmp[1].substring(1, rankTmp[1].length() - 1)));
                curObj.put("overall", teacherResponse1_tr.get(0).text().trim());
                curObj.put("recommendation", teacherResponse2_tr.get(0).text().trim());

                // add subjects
                for (int j = 2; j < tr.size(); ++j) {
                    Elements td = tr.get(j).children();
                    JSONObject curSub = new JSONObject();
                    subject.put(curSub);

                    String grade = td.get(9).text().trim();
                    if (grade.startsWith("C")) {
                        curSub.put("retry", true);
                        grade = grade.substring(1);
                    } else curSub.put("retry", false);

                    String rank = td.get(11).text().trim();
                    if (rank.equals("")) rank = "-1";

                    curSub.put("name", td.get(0).text().trim().substring(2));
                    curSub.put("mandatory", td.get(1).text().trim().equals("必修"));
                    curSub.put("attr", td.get(5).text().trim());
                    curSub.put("type", td.get(6).text().trim());
                    curSub.put("attr2", td.get(7).text().trim());
                    curSub.put("credit", Integer.parseInt(td.get(8).text().trim()));
                    curSub.put("grade", Integer.parseInt(grade));
                    curSub.put("get", td.get(10).text().trim().equals("Y"));
                    curSub.put("rank", Integer.parseInt(rank));
                    curSub.put("c_students", Integer.parseInt(td.get(12).text().trim()));
                    curSub.put("c_average", Double.parseDouble(td.get(13).text().trim()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    public static @Nullable JSONObject readScore(final String responseContent) {
        JSONObject output = new JSONObject();

        try {

            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");

            JSONArray detailJSON = new JSONArray();
            output.put("detail", detailJSON);

//            Elements scoreData = tables.get(2).getElementsByIndexEquals(0).get(0).getElementsByTag("table");

            System.out.println(tables.get(2));

//            for (int i = 0; i < scoreData.size(); i += 2) {
//                JSONObject curObj = new JSONObject();
//                detailJSON.put(curObj);
//
//                JSONArray subject = new JSONArray();
//                curObj.put("subject", subject);
//
//                Elements tr = scoreData.get(i).getElementsByTag("tr");
//                Elements tr_sum = scoreData.get(i + 1).getElementsByTag("tr");
//                Elements credits_tr1 = tr_sum.get(3).children();
//                Elements credits_tr2 = tr_sum.get(5).children();
//                Elements rank_tr = tr_sum.get(7).children();
//                Elements teacherResponse1_tr = tr_sum.get(9).children();
//                Elements teacherResponse2_tr = tr_sum.get(13).children();
//                String[] rankTmp = rank_tr.get(1).text().trim().split(" ");
//
//                String[] semesterStr = tr.get(0).text().trim().split(" "); // "110 學年度 第 1 學期"
//                curObj.put("semester", Integer.parseInt(semesterStr[0]));
//                curObj.put("semester2", Integer.parseInt(semesterStr[3]));
//                curObj.put("try_credit", Integer.parseInt(credits_tr1.get(0).text().trim()));
//                curObj.put("try_mandatory_credit", Integer.parseInt(credits_tr1.get(1).text().trim()));
//                curObj.put("try_elective_credit", Integer.parseInt(credits_tr1.get(2).text().trim()));
//                curObj.put("get_credit", Integer.parseInt(credits_tr2.get(0).text().trim()));
//                curObj.put("get_mandatory_credit", Integer.parseInt(credits_tr2.get(1).text().trim()));
//                curObj.put("get_elective_credit", Integer.parseInt(credits_tr2.get(2).text().trim()));
//                curObj.put("final_grade", Double.parseDouble(rank_tr.get(0).text().trim()));
//                curObj.put("rank", Integer.parseInt(rankTmp[0]));
//                curObj.put("pr", 100 - Double.parseDouble(rankTmp[1].substring(1, rankTmp[1].length() - 1)));
//                curObj.put("overall", teacherResponse1_tr.get(0).text().trim());
//                curObj.put("recommendation", teacherResponse2_tr.get(0).text().trim());
//
//                // add subjects
//                for (int j = 2; j < tr.size(); ++j) {
//                    Elements td = tr.get(j).children();
//                    JSONObject curSub = new JSONObject();
//                    subject.put(curSub);
//
//                    String grade = td.get(9).text().trim();
//                    if (grade.startsWith("C")) {
//                        curSub.put("retry", true);
//                        grade = grade.substring(1);
//                    } else curSub.put("retry", false);
//
//                    String rank = td.get(11).text().trim();
//                    if (rank.equals("")) rank = "-1";
//
//                    curSub.put("name", td.get(0).text().trim().substring(2));
//                    curSub.put("mandatory", td.get(1).text().trim().equals("必修"));
//                    curSub.put("attr", td.get(5).text().trim());
//                    curSub.put("type", td.get(6).text().trim());
//                    curSub.put("attr2", td.get(7).text().trim());
//                    curSub.put("credit", Integer.parseInt(td.get(8).text().trim()));
//                    curSub.put("grade", Integer.parseInt(grade));
//                    curSub.put("get", td.get(10).text().trim().equals("Y"));
//                    curSub.put("rank", Integer.parseInt(rank));
//                    curSub.put("c_students", Integer.parseInt(td.get(12).text().trim()));
//                    curSub.put("c_average", Double.parseDouble(td.get(13).text().trim()));
//                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    private static int getInt(String inp) {
        if (inp.equals(""))
            return 0;
        return Integer.parseInt(inp);
    }

    private static String getTimeFormat(String time) {
        // `111年11月04日` after format -> `2023-11-04`
        return String.join("-", time.split("[年月日]"));
    }
}
