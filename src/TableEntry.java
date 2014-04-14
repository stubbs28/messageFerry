

public class TableEntry {
	private final int TTL = 1000;
	
	private int netID;
	private int cost;
	private int nextHop;
	private boolean isMobile;
	private int ttl;
	
	public TableEntry(int netID, int cost, int nextHop, boolean isMobile)
	{
		this.netID = netID;
		this.cost = cost;
		this.nextHop = nextHop;
		ttl = TTL;
	}
	
	public TableEntry(String e)
	{
		extractEntry(e);
	}
	
	public boolean isAlive()
	{
		if(cost == 0)
			return true;
		if(0 >= --ttl)
			return false;
		return true;
	}
	
	public void keepAlive()
	{
		ttl = TTL;
	}
	
	
	public void setTTL(int ttl)
	{
		this.ttl = ttl;
	}
	
	public int getTTL()
	{
		return ttl;
	}
	
	public void setNetID(int netID)
	{
		this.netID = netID;
	}
	
	public void setCost(int cost)
	{
		this.cost = cost;
	}
	
	public void setNextHop(int nextHop)
	{
		this.nextHop = nextHop;
	}
	
	public void setMobile(boolean isMobile)
	{
		this.isMobile = isMobile;
	}
	
	public int getNetID()
	{
		return netID;
	}
	
	public int getCost()
	{
		return cost;
	}
	
	public int getNextHop()
	{
		return nextHop;
	}
	
	public boolean isMobile()
	{
		return isMobile;
	}
	
	public String getRIP(int number)
	{
		String str = netID + ", " + (cost + 1) + ", " + number  + ", " + ttl + ", " + isMobile + "\n";
		return str;
	}
	
	public String toString()
	{
		return  netID + ", " + cost + ", " + nextHop + ", " + ttl + "\n";
	}

	private void extractEntry(String str)
	{
		String[] strs = str.split(", ");
		netID = Integer.parseInt(strs[0]);
		cost = Integer.parseInt(strs[1]);
		nextHop = Integer.parseInt(strs[2]);
		ttl = Integer.parseInt(strs[3]);
		isMobile = Boolean.parseBoolean(strs[4]);
	}
}
