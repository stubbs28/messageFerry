import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Board extends JPanel implements MouseListener, KeyListener
{
	private final int B_WIDTH = 1000;
	private final int B_HEIGHT = 1000;
	private final int INITIAL_DELAY = 100;
	private final int PERIOD_INTERVAL = 100;
	private final int G_WIDTH = B_WIDTH / 50;
	private final int G_HEIGHT = B_HEIGHT / 50;
	private final int TABLE_WIDTH = 400;
	private Timer timer;;
	
	private Map<Point, Device> devices;
	private Point lastDeviceAdded;
	private Point src;
	private Point dst;
	private int deviceNumber = 1;
	
	private Point mouse;
	private boolean drawRange = true;
	private boolean drawPath = false;
	private boolean drawPaths = true;
	private boolean run = false;
	private boolean getDst = false;
	private boolean drawTable = true;
	
	Scanner scan;
	
	public Board()
	{
		scan = new Scanner(System.in);
		devices = new HashMap<Point, Device>();
		initBoard();
	}
	
	private void initBoard()
	{
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(B_WIDTH + TABLE_WIDTH, B_HEIGHT));
		
		setDoubleBuffered(true);
		
		this.addMouseListener(this);
		this.addKeyListener(this);
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new ScheduleTask(), INITIAL_DELAY, PERIOD_INTERVAL);
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
		drawImages(g);
	}
	
	
	private void drawImages(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if(getDst)
		{
			g2d.setColor(Color.YELLOW);
			g2d.fillOval((src.x * G_WIDTH) - 5, (src.y * G_HEIGHT) - 5, G_WIDTH + 10, G_HEIGHT + 10);
			g2d.fillOval((dst.x * G_WIDTH) - 5, (dst.y * G_HEIGHT) - 5, G_WIDTH + 10, G_HEIGHT + 10);
		}
		
		g2d.setColor(Color.GRAY);
		for(int w = 0; w < B_WIDTH / G_WIDTH; w++)
			g2d.drawLine(w * G_WIDTH, 0, w * G_WIDTH, B_HEIGHT);
		for(int h = 0; h < B_HEIGHT / G_HEIGHT; h++)
			g2d.drawLine(0, h * G_WIDTH, B_WIDTH, h * G_WIDTH);
		
		if(drawPaths)
		{
			for(Point key: devices.keySet())
			{
				g2d.setColor(Color.BLACK);
				g2d.drawChars(devices.get(key).displayMessage().toCharArray(), 0, 
						devices.get(key).displayMessage().toCharArray().length,
						devices.get(key).getLocation().x * G_WIDTH, 
						devices.get(key).getLocation().y * G_HEIGHT);
				
				g2d.setColor(Color.DARK_GRAY);
				if(devices.get(key).isMobile())
				{
					ArrayList<Point> path = ((MobileDevice)devices.get(key)).getPath();
					for(int i = 0; i < path.size() - 1; i++)
					{
						Point p1 = path.get(i);
						Point p2 = path.get(i + 1);
						g2d.fillRect((p1.x * G_WIDTH) + 1, (p1.y * G_HEIGHT) + 1, G_WIDTH - 1, G_HEIGHT - 1);
						g2d.fillRect((p2.x * G_WIDTH) + 1, (p2.y * G_HEIGHT) + 1, G_WIDTH - 1, G_HEIGHT - 1);
						g2d.drawLine(p1.x * G_WIDTH + (G_WIDTH / 2), p1.y * G_HEIGHT + (G_HEIGHT / 2),
								p2.x * G_WIDTH + (G_WIDTH / 2), p2.y * G_HEIGHT + (G_HEIGHT / 2));
					}
				}
			}
		}
		
		if(drawPath)
		{
			g2d.setColor(Color.RED);
			
			ArrayList<Point> path = ((MobileDevice)devices.get(lastDeviceAdded)).getPath();
			for(int i = 0; i < path.size() - 1; i++)
			{
				Point p1 = path.get(i);
				Point p2 = path.get(i + 1);
				g2d.fillRect((p1.x * G_WIDTH) + 1, (p1.y * G_HEIGHT) + 1, G_WIDTH - 1, G_HEIGHT - 1);
				g2d.fillRect((p2.x * G_WIDTH) + 1, (p2.y * G_HEIGHT) + 1, G_WIDTH - 1, G_HEIGHT - 1);
				g2d.drawLine(p1.x * G_WIDTH + (G_WIDTH / 2), p1.y * G_HEIGHT + (G_HEIGHT / 2),
						p2.x * G_WIDTH + (G_WIDTH / 2), p2.y * G_HEIGHT + (G_HEIGHT / 2));
			}
			Point p1 = path.get(path.size() - 1);
			g2d.drawLine(p1.x * G_WIDTH + (G_WIDTH / 2), p1.y * G_HEIGHT + (G_HEIGHT / 2),
					mouse.x * G_WIDTH + (G_WIDTH / 2), mouse.y * G_HEIGHT + (G_HEIGHT / 2));
		}
		
		
		g2d.setColor(Color.CYAN);
		mouse = MouseInfo.getPointerInfo().getLocation();
		Point s = this.getLocationOnScreen();
		mouse.x = (mouse.x - s.x) / G_WIDTH;
		mouse.y = (mouse.y - s.y) / G_HEIGHT;
		g2d.fillRect((mouse.x * G_WIDTH) + 1, (mouse.y * G_HEIGHT) + 1, G_WIDTH - 1, G_HEIGHT - 1);
		
		for(Point key: devices.keySet())
		{
			Device d = devices.get(key);
			g2d.setColor(Color.YELLOW);
			if(d.isScanning())
				g2d.fillOval((d.getLocation().x * G_WIDTH) - (Device.RANGE * G_WIDTH) + (G_WIDTH / 2), 
						(d.getLocation().y * G_HEIGHT) - (Device.RANGE * G_HEIGHT) + (G_HEIGHT / 2) ,
						Device.RANGE * 2 * G_WIDTH, Device.RANGE * 2 * G_HEIGHT);
		}

		
		String tableInfo = "";
		for(Point key: devices.keySet())
		{
			Device d = devices.get(key);
			g2d.setColor(d.getColor());
			
			g2d.fillRect((d.getLocation().x * G_WIDTH) + 2, (d.getLocation().y * G_HEIGHT) + 2, G_WIDTH - 3, G_HEIGHT - 3);
			if(drawRange)
				g2d.drawOval((d.getLocation().x * G_WIDTH) - (Device.RANGE * G_WIDTH) + (G_WIDTH / 2), 
						(d.getLocation().y * G_HEIGHT) - (Device.RANGE * G_HEIGHT) + (G_HEIGHT / 2) ,
						Device.RANGE * 2 * G_WIDTH, Device.RANGE * 2 * G_HEIGHT);
			
			tableInfo += d.getRoutingTable() + "\n";
			
			g2d.setColor(Color.BLACK);
			String number = "" +  devices.get(key).getNumber();
			g2d.drawChars(number.toCharArray(), 0, 
					number.toCharArray().length,
					(devices.get(key).getLocation().x * G_WIDTH) + 2, 
					((devices.get(key).getLocation().y + 1) * G_HEIGHT) - 5);
		}
		
		System.out.println(tableInfo);
		
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect(B_WIDTH, 0, B_WIDTH + TABLE_WIDTH, B_HEIGHT);
		g2d.setColor(Color.BLACK);
		if(!tableInfo.equals(""))
		{
			int maxLines = (B_HEIGHT / 15);
			String[] lines = tableInfo.split("\n");
			for(int i = 0; (i < lines.length && i < (maxLines * 2)); i++)
			{
				g2d.drawChars(lines[i].toCharArray(), 0, 
						lines[i].toCharArray().length, (i < maxLines) ? B_WIDTH + 15 : B_WIDTH + 200, 
						15 + ((i % maxLines) * 15));
			}
		}
		
		Toolkit.getDefaultToolkit().sync();
	}
	
	private class ScheduleTask extends TimerTask
	{
		@Override
		public void run()
		{
			if(run)
			{
				for(Point key: devices.keySet())
				{
					devices.get(key).tick(devices);
					
					if(devices.get(key).isMobile())
					{
						((MobileDevice)devices.get(key)).move();
					}
				}
			}
			repaint();
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if(drawPath)
		{
			((MobileDevice)devices.get(lastDeviceAdded)).addPathPoint(mouse);
		} else
		{
			if(devices.containsKey(mouse))
			{
				if(getDst)
				{
					if(!src.equals(mouse))
					{
						int d = devices.get(mouse).getNumber();
						dst = mouse;
						String msg = JOptionPane.showInputDialog("Device " + devices.get(src).getNumber() + " message to device " + d + ": ");
						if(msg != null)
							devices.get(src).send(d, msg);
					}
					getDst = !getDst;
				} else
				{
					src = mouse;
					dst = src;
					getDst = !getDst;
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		char c = arg0.getKeyChar();
		if(c == 'q' && drawPath)
		{
			drawPath = !drawPath;
			((MobileDevice)devices.get(lastDeviceAdded)).setCanMove(true);
		}else if((c == 'm' || c == 'n') && !drawPath)
		{
			Device d;
			if(c == 'm')
			{
				d = new MobileDevice(deviceNumber++, mouse);
				drawPath = true;
			}
			else
				d = new Device(deviceNumber++, mouse);
			
			devices.put(mouse, d);
			lastDeviceAdded = mouse;
		} else if(c == 'z' && !drawPath)
		{
			devices.remove(mouse);
		} else if(c == 'r')
		{
			drawRange = !drawRange;
		} else if(c == 'p')
		{
			drawPaths = !drawPaths;
		} else if(c == 's')
		{
			run = !run;
		} else if(c == 't')
		{
			drawTable = !drawTable;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
