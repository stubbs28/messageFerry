
public class Message {
	

	public final static String SYN = "SYN";
	public final static String ACK = "ACK";
	public final static String RIP = "RIP";
	
	public final static int TTL = 300;
	
	private String type;
	private int ttl;
	private int src;
	private int dst;
	private int seq;
	private int ack;
	private String content;
	
	public Message(String type, int ttl, int src, int dst, int seq, int ack, String content)
	{
		this.type = type;
		this.ttl = ttl;
		this.src = src;
		this.dst = dst;
		this.seq = seq;
		this.ack = ack;
		this.content = content;
	}
	
	public Message(String m)
	{
		extractMessage(m);
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public void setTTL(int ttl)
	{
		this.ttl = ttl;		
	}
	
	public void setSrc(int src)
	{
		this.src = src;
	}
	
	public void setDst(int dst)
	{
		this.dst = dst;
	}
	
	public void setSeq(int seq)
	{
		this.seq = seq;
	}
	
	public void setAck(int ack)
	{
		this.ack = ack;
	}
	
	public void setContent(String content)
	{
		this.content = content;
	}
	
	public int getTTL()
	{
		return ttl;
	}
	
	public String getType()
	{
		return type;
	}
	
	public int getSrc()
	{
		return src;
	}
	
	public int getDst()
	{
		return dst;
	}
	
	public int getSeq()
	{
		return seq;
	}
	
	public int getAck()
	{
		return ack;
	}
	
	public String getContent()
	{
		return content;
	}
	
	public String getString()
	{
		return type + ", " + ttl + ", " + src + ", " + dst + ", " + seq + ", " + ack + ", " + content;
	}
	
	public void extractMessage(String str)
	{
		String[] strs = str.split(", ");
		
		type = strs[0];
		ttl = Integer.parseInt(strs[1]);
		src = Integer.parseInt(strs[2]);
		dst = Integer.parseInt(strs[3]);
		seq = Integer.parseInt(strs[4]);
		ack = Integer.parseInt(strs[5]);
		content = "";
		for(int i = 6; i < strs.length - 1; i++)
			content += strs[i] + ", ";
		content += strs[strs.length - 1];
		System.out.println(content);
	}
}
