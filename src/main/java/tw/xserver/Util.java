package tw.xserver;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Util {
    public static int getInt(String inp) {
        if (inp.equals(""))
            return 0;
        return Integer.parseInt(inp);
    }

    public static JSONObject dateConvert(String[] ymd) {
        return new JSONObject()
                .put("年", Integer.valueOf(ymd[0]))
                .put("月", Integer.valueOf(ymd[1]))
                .put("日", Integer.valueOf(ymd[2]));
    }

    public static String getTime() {
        return "[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "]";
    }
}
