import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;


public class MobileDevice extends Device{

	private final int STEP_WAIT = 2;
	
	private boolean canMove;
	private ArrayList<Point> path;
	private int nextPointIndex = 0;
	private boolean moveForward = true;
	private int stepCounter;
	
	public MobileDevice(int number, Point location) {
		super(number, location);
		
		path = new ArrayList<Point>();
		path.add(location);
		stepCounter = STEP_WAIT;
	}
	
	public Color getColor()
	{
		Color c = super.getColor();
		return (c.equals(Color.MAGENTA)) ? Color.BLUE : c;
	}

	public boolean isMobile()
	{
		return true;
	}
	
	public void addPathPoint(Point loc)
	{
		if(path.isEmpty())
			path.add(loc);
		for(Point p: path)
		{
			if(p == loc)
				return;
		}
		path.add(loc);
	}
	
	public void setCanMove(boolean canMove)
	{
		this.canMove = canMove;
	}
	
	public ArrayList<Point> getPath()
	{
		return path;
	}
	
	public void move()
	{
		if(!path.isEmpty() && canMove && 0 >= --stepCounter)
		{
			stepCounter = STEP_WAIT;
			Point nextMove = location;
			for(int x = -1; x <= 1; x++)
			{
				for(int y = -1; y <= 1; y++)
				{
					Point p = new Point(location.x + x, location.y + y);
					if(p.distance(path.get(nextPointIndex)) < nextMove.distance(path.get(nextPointIndex)))
					{
						nextMove = p;
					}
				}
			}
			location = nextMove;
			if(location.distance(path.get(nextPointIndex)) == 0)
			{
				if(moveForward)
				{
				
					if(!(path.size() > ++nextPointIndex))
					{
						if(path.get(0).distance(path.get(--nextPointIndex)) == 0)
						{
							nextPointIndex = 1;
						}
						else
						{
							nextPointIndex--;
							moveForward = !moveForward;
						}
					}
				}else
				{
					if(0 >= nextPointIndex)
					{
						moveForward = !moveForward;
					} else
						nextPointIndex--;
				}
			}
		}
	}

}
