package canal_qbus;

import javax.json.JsonObject;
import javax.json.JsonArray;
import java.util.List;
import java.util.ArrayList;


public class Msg
{


	public List<Record> records;

    public class Record
    {
        public JsonObject data;
        public long ackId;
        public List<String> receiver;

        public Record(JsonObject data, long ackId)
        {
            this.ackId = ackId;
            this.data = data;
        }

        public String debugStr()
        {
            if (data == null) {
                return "empty json, id=" + ackId;

            } else {
                String ids = "";
                JsonArray rows = data.getJsonArray("rows");
                if (rows == null) {
                    ids = "";
                } else {
                    ids = fetchIds(rows.getValuesAs(JsonObject.class));
                }
                String receiver = "";
                if (this.receiver != null) {
                    for (String rc : this.receiver) {
                        receiver += "," + rc;
                    }
                }

                return  "], [canalId:" + ackId 
                    + "], [db:" + data.getString("db","") 
                    + "], [table:" + data.getString("table", "") 
                    + "], [event:" + data.getString("event", "") 
                    + "], [ids: " + ids + " ]"
                    + "], [receiver:" + receiver;
            }
        }

        private String fetchIds(List<JsonObject> rows)
        {
            String ids = "";
            for (JsonObject r : rows) {
                ids += r.getJsonObject("id").getString("value", "");
            }
            return ids;
        }
    }

    public Msg(List<JsonObject> data, long id)
    {
        records = new ArrayList<Record>();
        for (JsonObject r : data) {
            records.add(new Record(r, id));
        }
    }

    public String toString()
    {
        String str = "";
        for (Record r : records) {
            str += r.debugStr();
        }
        return str;
    }

    /*
    public String debug()
    {
        if (data.size()) {
            return null;
        }
        for (JsonObject r : data) {
            String ids = "";
            JsonArray rows = r.getJsonArray("rows");
            if (rows == null) {
                ids = "";
            } else {
                ids = fetchIds(rows.getValuesAs(JsonObject.class));
            }
            return "[recordNum:" + data.size() 
                + "], [canalId:" + msgId 
                + "], [db:" + r.getString("db","") 
                + "], [table:" + r.getString("table", "") 
                + "], [event:" + r.getString("event", "") 
                + "], [id: " + ids + " ]";
        }
    }

    private String fetchIds(List<JsonObject> rows)
    {
        if (rows == null) {
            return "";
        }
        String ids = "";
        for (JsonObject r : rows) {
            ids += r.getJsonObject("id").getString("value", "");
        }
        return ids;
    }
    */
}

