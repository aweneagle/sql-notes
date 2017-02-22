package canal_qbus;
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import java.lang.Thread;
import java.util.List;
import javax.json.JsonObject;
import javax.json.JsonArray;

public class SafeService {
    final int TRY_TIMES = 4;
    private int sleep = 500;

    final int ACT_CONN = 0;
    final int ACT_CLOSE = 1;
    final int ACT_SEND = 2;
    final int ACT_RECV = 3;
    final int ACT_ACK = 4;

    private boolean connAlive = false;

    private Session session;
    private Msg msg;
    private Logger log;

    private String name;

    public SafeService(Session s, String name)
    {
        session = s;
        connAlive = false;
        this.name = name;
        log = LoggerFactory.getLogger(SafeService.class);
    }

    public boolean conn()
    {
        connAlive = tryAct(ACT_CONN, TRY_TIMES);
        return connAlive;
    }

    public boolean close()
    {
        if (connAlive) {
            boolean succClose = tryAct(ACT_CLOSE, 1);
            connAlive = false;
            return succClose;
        }
        return true;
    }

    public boolean send(Msg msg)
    {
        this.msg = msg;
        //return tryAct(ACT_SEND, TRY_TIMES);
        return tryByAutoReconn(ACT_SEND);
    }

    public boolean ack()
    {
        //return tryAct(ACT_ACK, 1);
        return tryByAutoReconn(ACT_ACK);
    }

    public boolean reconn()
    {
        close();
        return conn();
    }

    public Msg recv() throws Exception
    {

        /*
           if (!tryAct(ACT_RECV, TRY_TIMES)) {
           throw new Exception("failed to get data from server");
           }
           */
        tryByAutoReconn(ACT_RECV);
        return this.msg;
    }

    private boolean tryByAutoReconn(int action)
    {
        while (true) {
            if (tryAct(action, 1)) {
                return true;
            }
            tryAct(ACT_CONN, 2);
        }
    }

    private boolean tryAct(int action, int try_times)
    {
        while (true) {
            try {
                this.act(action);
                return true;
            } catch (Exception e) {
                error(e);
                try_times --;
                if (try_times <= 0) {
                    break;
                }
                try {
                    Thread.sleep(sleep);
                    /* 下一次延长一倍时间 */
                    sleep += sleep;

                } catch (Exception exp) {
                    sleep = 500;
                    error(exp);
                    return false;
                }
            }
        }
        sleep = 500;
        return false;
    }


    private void act(int action) throws Exception
    {
        switch (action) {
            case ACT_CONN:
                session.conn();
                break;

            case ACT_CLOSE:
                session.close();
                break;

            case ACT_SEND:
                session.send(msg);
                break;

            case ACT_RECV:
                this.msg = session.recv();
                break;

            case ACT_ACK:
                session.ack();
                break;
        }
    }

    private void error(Exception e)
    {
        e.printStackTrace();
    }

}
