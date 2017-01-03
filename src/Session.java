package canal_qbus;
import javax.json.JsonObject;
import java.util.List;

public interface Session {
	public void conn()throws Exception ;
	public void close()throws Exception ;
	public void send(Msg msg) throws Exception ;
	public Msg recv() throws Exception ;
	public void ack() throws Exception ;
}
