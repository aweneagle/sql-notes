package canal_qbus;

import java.net.InetSocketAddress;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.common.utils.AddressUtils;

import java.util.List;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;

import java.io.FileInputStream;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  

public class Canal implements Session {
    final int BATCH_NUM = 1;
    
    private long currRecordId = 0;

    private Logger logger;

    private static HashMap<Integer, String> eventTypeMap;
    
    private boolean alive = false;

    private Pattern oldTablePattern ;
    
    /*
     * canal
     */
    private CanalConnector canal;

    public Canal()
    {
        alive = false;
        eventTypeMap = new HashMap<Integer, String>();
        eventTypeMap.put(EventType.INSERT.getNumber(), "insert");
        eventTypeMap.put(EventType.UPDATE.getNumber(), "update");
        eventTypeMap.put(EventType.DELETE.getNumber(), "delete");
        eventTypeMap.put(EventType.CREATE.getNumber(), "create");
        eventTypeMap.put(EventType.ALTER.getNumber(), "alter");
        eventTypeMap.put(EventType.ERASE.getNumber(), "drop");  //drop table command
        eventTypeMap.put(EventType.TRUNCATE.getNumber(), "truncate");
        eventTypeMap.put(EventType.RENAME.getNumber(), "rename");
        eventTypeMap.put(EventType.CINDEX.getNumber(), "cindex");
        eventTypeMap.put(EventType.DINDEX.getNumber(), "dindex");
        oldTablePattern = Pattern.compile("rename\\s+table\\s+([^\\s]+)\\s+to\\s+[^\\s]+");
        logger = LoggerFactory.getLogger(Canal.class);
    }
    

    public void conn() throws Exception {
        canal = CanalConnectors.newSingleConnector(
                new InetSocketAddress(
                    AddressUtils.getHostIp(), 
                    Config.get("canal").getInt("port")
                    ),
                Config.get("canal").getString("distination"), 
                Config.get("canal").getString("user", ""), 
                Config.get("canal").getString("pass", "")
                );
        canal.connect();
        logger.info("connection established");
        alive = true;
        canal.subscribe();
    }

    public void close() throws Exception
    {
        if (alive) {
            canal.disconnect();
            logger.info("connection closed");
            alive = false;
        }
    }

    public void send(Msg msg) throws Exception
    {
        throw new Exception("should not call Canal::send() method");
    }

    public Msg recv() throws Exception
    {
        long timeout = 1000;
        Message records;
        try {
            records = canal.getWithoutAck(BATCH_NUM, timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        currRecordId = records.getId();
        /* -1 是心跳包
         */
        /*
        if (currRecordId != -1) {
            logger.info("canal receive: {}", currRecordId);
        }
        */
        List<JsonObject> list = new ArrayList<JsonObject>();

        for (Entry e : records.getEntries()) {
            JsonObject rc = parseRecord(e);
            if (rc != null) {
                list.add(rc);
            }
        }
        return new Msg(list, currRecordId);
    }
    
    public void ack() throws Exception
    {
        /*
         * -1 是心跳包
         */
        /*
        if (currRecordId != -1) {
            logger.info("canal ack: {}", currRecordId);
        }
        */
        canal.ack(this.currRecordId);
    }

    private JsonObject parseRecord(Entry entry) throws Exception
    {
        JsonObject res = null;
        JsonObjectBuilder data = Json.createObjectBuilder();

        // get table name
        String tableName = entry.getHeader().getTableName();
        data.add("table", tableName);
        data.add("db", entry.getHeader().getSchemaName());

        if (entry.getEntryType() == EntryType.ROWDATA) {
            RowChange rc = null;
            try {
                rc = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("parse event has an error, error:" + e.getMessage() + ", data:" + entry.toString());
            }
            
            // 获取操作类型 event type
            EventType eventType = rc.getEventType();
            String sql = rc.getSql();
            data.add("sql", sql);
            data.add("event", eventTypeMap.get(eventType.getNumber()));
            JsonArrayBuilder rows = Json.createArrayBuilder();
            int et = eventType.getNumber();
            if (eventType == EventType.QUERY  || rc.getIsDdl()) {
                //这里不会有原始数据带过来;
                //抓取 rename 的 旧表名
                if (eventType == EventType.RENAME) {
                    Matcher m = oldTablePattern.matcher(sql);
                    String oldTableName = "unknown";
                    if (m.matches()) {
                        oldTableName = m.group(1);
                        data.add("old_table", oldTableName);
                    }
                }
            } else {
                for (RowData rowData : rc.getRowDatasList()) {
                    switch (et) {
                        case EventType.INSERT_VALUE:
                        case EventType.UPDATE_VALUE:
                            getColumns(rowData.getAfterColumnsList(), rows);
                            break;

                        case EventType.DELETE_VALUE:
                            getColumns(rowData.getBeforeColumnsList(), rows);
                            break;

                        default:
                            throw new Exception("failed to load entry data, wrong EventType");
                    }
                }
                for (RowData rowData : rc.getRowDatasList()) {
                }
            }
            data.add("rows", rows);
            
            res = data.build();
            
        }
        
        return res;
    }
    
    private void getColumns(List<Column> columns, JsonArrayBuilder rows)
    {
        JsonObjectBuilder col = Json.createObjectBuilder();
        for (Column column : columns) {
            JsonObjectBuilder field = Json.createObjectBuilder();
            StringBuilder builder = new StringBuilder();
            String fieldName = column.getName();
            field.add("value", column.getValue());
            field.add("type", column.getMysqlType());
            field.add("updated", column.getUpdated());
            col.add(fieldName, field);
        }
        rows.add(col);
    }
}
