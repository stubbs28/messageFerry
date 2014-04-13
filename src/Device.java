import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Device {
	
	public final static int RANGE = 4;
	public final static int CLEAR_TIMER = 200;
	public final static int SCAN_TIMER = 5;
	
	protected Point location;
	protected int number;
	protected int seq;
	protected boolean isScanning;
	
	protected String displayMessage;
	protected int clearCount;
	protected int scanCount;
	
	protected Map<Integer, TableEntry> routingTable;
	protected ArrayList<Message> sendQueue;
	protected ArrayList<Message> resendQueue;
	
	public Device(int number, Point location)
	{
		this.number = number;
		this.location = location;
		seq = 0;
		isScanning = false;
		displayMessage = "";
		clearCount = 0;
		sendQueue = new ArrayList<Message>();
		resendQueue = new ArrayList<Message>();
		routingTable = new HashMap<Integer, TableEntry>();
		routingTable.put(number, new TableEntry(number, 0, number));
	}
	
	
	public String getRoutingTable()
	{
		String tableInfo = "Device " + number + " Routing Table: \n";
		for(int t: routingTable.keySet())
		{
			tableInfo += routingTable.get(t).toString();
		}
		return tableInfo;
	}
	
	public boolean isMobile()
	{
		return false;
	}
	
	public void setNumber(int number)
	{
		this.number = number;
	}
	
	public void setLocation(Point location)
	{
		this.location = location;
	}
	
	public int getNumber()
	{
		return number;
	}
	
	public Color getColor()
	{
		Color c = Color.MAGENTA;
		
		Iterator<Message> ii = sendQueue.iterator();
		while(ii.hasNext())
		{
			Message m = ii.next();
			if(m.getType().equals(Message.SYN))
			{
				c = Color.GREEN;
				break;
			}else if(m.getType().equals(Message.ACK))
			{
				c = Color.YELLOW;
			}
		}
		return c;
	}
	
	public Point getLocation()
	{
		return location;
	}
	
	public boolean inRange(Point comp)
	{
		return (location.distance(comp) < RANGE);
	}
	
	public void tick(Map<Point, Device> devices)
	{
		// remove dead routing info
		Iterator<Entry<Integer, TableEntry>> ii = routingTable.entrySet().iterator();
		while(ii.hasNext())
		{
			TableEntry t =  ii.next().getValue();
			if(!t.isAlive())
				ii.remove();
		}
		
		// remove dead messages
		Iterator<Message> i = sendQueue.iterator();
		while(i.hasNext())
		{
			Message m = i.next();
			if(m.getSrc() != number)
				m.setTTL(m.getTTL() - 1);
			if(m.getTTL() <= 0)
				i.remove();
		}
	
		// queue messages to resend
		i = resendQueue.iterator();
		while(i.hasNext())
		{
			Message message = i.next();
			message.setTTL(message.getTTL() - 1);
			if(message.getTTL() <= 0)
			{
				message.setTTL(Message.TTL);
				sendQueue.add(message);
				i.remove();
			}
		}
		
		scan(devices);
	}
	
	private void solicitRIP(int dst)
	{
		Message m = new Message(Message.RIP, Message.TTL, number, dst, 0, 0, RIP());
		sendQueue.add(m);
	}
	
	private String RIP()
	{
		String rip = "";
		for(Integer netID: routingTable.keySet())
			rip += routingTable.get(netID).getRIP(number, isMobile());
		return rip;
	}
	
	private void updateRT(Message rip) 
	{	
		String[] entries = rip.getContent().split("\n");
		for(String entry: entries)
		{
			TableEntry t = new TableEntry(entry);
			int netID = t.getNetID();
			
			if(netID == number)
				continue;
			
			if(routingTable.containsKey(netID))
			{
				if(routingTable.get(netID).getCost() >= t.getCost())
				{
					routingTable.put(netID, t);
				}
			} 
			else
			{
				routingTable.put(netID, t);
			}
		}
	}
	
	private void scan(Map<Point, Device> devices)
	{
		if(0 > --scanCount)
		{
			isScanning = true;
			for(Point key: devices.keySet())
			{
				if(!devices.get(key).equals(this) && inRange(devices.get(key).getLocation()))
				{
					connect(devices.get(key));
					devices.get(key).connect(this);
				}
			}
			scanCount = SCAN_TIMER;
		} else
			isScanning = false;
	}
	
	public boolean isScanning()
	{
		return isScanning;
	}
	
	private void connect(Device d) 
	{
		if(!routingTable.containsKey(d.getNumber()))
		{
			routingTable.put(d.getNumber(), new TableEntry(d.getNumber(), 1, d.getNumber()));
		}else
		{
			routingTable.get(d.getNumber()).keepAlive();
		}
		solicitRIP(d.getNumber());
		sendMessages(d);
	}
	
	
	public void send(int dst, String text)
	{
		if (Integer.MAX_VALUE <= ++seq)
			seq = 0;
		Message m = new Message(Message.SYN, Message.TTL, number, dst, seq, 0, text);
		sendQueue.add(m);
	}
	
	private void sendMessages(Device recipiant)
	{
		Iterator<Message> ii = sendQueue.iterator();
		while(ii.hasNext())
		{
			Message m = ii.next();
			
			if(routingTable.containsKey(m.getDst()))
			{
				if(routingTable.get(m.getDst()).getNextHop() == recipiant.getNumber())
				{
					recipiant.receiveMessage(m.getString());
					if(m.getSrc() == number && m.getType() == Message.SYN)
					{
						Message mm = new Message(m.getType(), m.getTTL(), m.getSrc(), m.getDst(), m.getSeq(), m.getAck(), m.getContent());
						resendQueue.add(mm);
					}
					ii.remove();
				}
			}
		}
	}
	
	public void receiveMessage(String str)
	{
		Message message = new Message(str);
		
		if((int)(Math.random() * 100) < 5)
			return;
		if(message.getDst() != number)
		{
			sendQueue.add(message);
		} else
		{
			if(message.getType().equals(Message.ACK))
			{
				processMessage(message);
				
				Iterator<Message> i = sendQueue.iterator();
				while(i.hasNext())
				{
					Message send = i.next();
					if(send.getSeq() == message.getAck())
						i.remove();
				}
				
				i = resendQueue.iterator();
				while(i.hasNext())
				{
					Message resend = i.next();
					if(resend.getSeq() == message.getAck())
						i.remove();
				}
			} else if(message.getType().equals(Message.RIP))
			{
				updateRT(message);
			} else if(message.getType().equals(Message.SYN))
			{
				processMessage(message);
				Message ack = new Message(Message.ACK, Message.TTL, number, message.getSrc(), 0, message.getSeq(), "ACK");
				sendQueue.add(ack);
			}
		}
	}

	private void processMessage(Message message) {
		displayMessage = "Recieved: " + message.getContent();
		System.out.println("Device " + number + " recieved: " + message.getContent());
		clearCount = CLEAR_TIMER;
	}
	
	public String displayMessage()
	{
		if(--clearCount <= 0)
			displayMessage = "";
		return displayMessage;
	}
}
