package canal_qbus;

import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import java.io.PrintStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class Log
{
    public static Logger w;

    public static void init()
    {
		w = LoggerFactory.getLogger(Log.class);
        redirect("error_log");
        redirect("info_log");
    }

    private static boolean redirect(String logtype)
    {
        String log_file = Config.get("global").getString(logtype, "");
        if (log_file.equals("")) {
            return false;
        }
        try {  
            PrintStream p = new PrintStream(new BufferedOutputStream(new FileOutputStream(log_file)));
            if (logtype.equals("info_log")) {
                System.setOut(p);
            } else if (logtype.equals("error_log")) {
                System.setErr(p);
            } else {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

