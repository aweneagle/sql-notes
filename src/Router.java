package canal_qbus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;

import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonString;

public class Router
{
	private static List<String> topics;

    /*
     * {
     *   "topic_1" => [
     *      "table_1",
     *      "table_2",
     *   ]
     * }
     */
    private static HashMap<String, List<Filter>> table_filters;
    private static HashMap<String, List<Filter>> db_filters;
    private static HashMap<String, List<Filter>> event_filters;

    private static class Filter
    {
        private String check;
        protected Pattern pattern;
        protected Filter(String check)
        {
            this.check = check;
            if (check.equals("*")) {
                pattern = Pattern.compile(".*");
            } else if (check.indexOf("*") >= 0) {
                pattern = Pattern.compile(check.replace("*", "[^\\s]+"));
            } else {
                pattern = null;
            }
        }

        protected boolean checkPattern(String tocheck)
        {
            if (pattern == null) {
                return tocheck.equals(check);
            } else {
                return pattern.matcher(tocheck).matches();
            }
        }
    }

    public static void init()
    {
        topics = new ArrayList<String>();
        table_filters = new HashMap<String, List<Filter>>();
        db_filters = new HashMap<String, List<Filter>>();
        event_filters = new HashMap<String, List<Filter>>();

        for (JsonObject tp : Config.getTopics()) {

            String tpName = tp.getString("topic");

            table_filters.put(
                    tpName,
                    buildFilters(
                        tp.getJsonArray("tables").getValuesAs(JsonString.class),
                        false
                        )
                    );

            db_filters.put(
                    tpName,
                    buildFilters(
                        tp.getJsonArray("dbs").getValuesAs(JsonString.class),
                        false
                        )
                    );

            event_filters.put(
                    tpName,
                    buildFilters(
                        tp.getJsonArray("events").getValuesAs(JsonString.class),
                        true
                        )
                    );
            topics.add(tpName);
        }
    }

    private static List<Filter> buildFilters(List<JsonString> patterns, boolean tolowercase)
    {
        List<Filter> filters = new ArrayList<Filter>();
        Filter f;
        for (JsonString p : patterns) {
            if (tolowercase) {
                f = new Filter(p.getString().toLowerCase());
            } else {
                f = new Filter(p.getString());
            }
            filters.add(f);
        }
        return filters;
    }

    private static boolean topicOk(String topic, JsonObject record)
    {
        boolean tableOk = false;
        if (record.getString("event").equals("rename")) {
            tableOk = check(table_filters.get(topic), record.getString("table"))
                || check(table_filters.get(topic), record.getString("old_table"));
        } else {
            tableOk = check(table_filters.get(topic), record.getString("table"));
        }
        return check(db_filters.get(topic), record.getString("db"))
            && tableOk
            && check(event_filters.get(topic), record.getString("event"));
    }

    private static boolean check(List<Filter> filter, String name)
    {
        for (Filter f : filter) {
            if (f.checkPattern(name)) {
                return true;  
            }
        }
        return false;
    }

    public static List<String> getTopics(JsonObject record)
    {
        List<String> list = new ArrayList<String>();
        for (String tp : topics) {
            if (topicOk(tp, record)) {
                list.add(tp);
            }
        }
        return list;
    }
}
