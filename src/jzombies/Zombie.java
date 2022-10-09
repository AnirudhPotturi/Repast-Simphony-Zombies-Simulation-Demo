/**
 * 
 */
package jzombies;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;


/**
 * @author aniru
 *
 */
public class Zombie {
	
	//Creating the space and a grid. Zombie is located in this
	// Zombies move around in the continuous space. This space allows floating point type values which will be rounded off to integer values. The int values are the values we look up in the grid.
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private boolean moved;
	
	public Zombie(ContinuousSpace<Object> space, Grid<Object> grid)
	{
		this.space = space;
		this.grid = grid;
	}
	
	
	//Step 1: Method called in every iteration of the simulation. Hence the annotation.
	// The annotation basically means that the step function will be called on all zombie objects starting at Tick 1 (a timestep -- measure of time) andthen every tick there after
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step()
	{
		// We are getting the grid location of the zombie
		GridPoint point = grid.getLocation(this);
		
		
		
		/*
		* @param grid the grid that contains the neighborhood
	    * @param point the central point of the neighborhood
	    * @param clazz the type of objects we want to get in the neighborhood. Objects not
	    * of this type will not be in the neighborhood list
	    * @param extent the extent of the neighborhood in each dimension --- x and y. So a total of 8 neigbouring cells are covered including diagonal ones.
	    * */
		// Using this class to create grid cells for surrounding neighborhood
		GridCellNgh<Human> nghCreator = new GridCellNgh<Human>(grid, point, Human.class, 1, 1);
		
		
		// We get a list of GridCells that make up the neighborhood. What is returned is the center cell where the Zombie is currently located.
		List<GridCell<Human>> gridCells = nghCreator.getNeighborhood(true);
		
		// The list passed to this is shuffled
		// With the shuffle method, we make sure the zombie moves in different directions in cases where all the cells are equal.
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		
		//Here, all we're doing is basically iterating through the list of GridCells and finding the cell with the largest size ie; cell with max number of objects in it.
		// Thus we get the cell containing the max number of Humans.
		GridPoint pointWithMostNumberOfHumans = null;
		int maxCount = -1;
		for(GridCell<Human> cell : gridCells)
		{
			if(cell.size() > maxCount)
			{
				pointWithMostNumberOfHumans = cell.getPoint();
				maxCount = cell.size();
			}
		}
		
		moveTowards(pointWithMostNumberOfHumans);
	}
	
	
	// Step 2: We determined the cell with max number of humans, now we want to move towards that cell
	public void moveTowards(GridPoint point)
	{
		// We only want to move if we're not located in the target cell
		if(!point.equals(grid.getLocation(this)))
		{
			//Getting zombie's current location. What is returned is coordinates of type double.
			NdPoint myPoint = space.getLocation(this);
			
			// Getting coordinates of the target point
			NdPoint targetPoint = new NdPoint(point.getX(), point.getY());
			
			// Calculating the angle at which the zombie should move towards the target
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, targetPoint);
			
			//Moves the zombie in the continuous space 1 unit at a time at an angle
			space.moveByVector(this, 1, angle,0);
			
			// Updating the position of the zombie in the grid.
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());
			
			moved = true;
		}
	}
	
	
	//Step 3 : A method used when infecting humans and turning them into zombies. We will be adding this with a network projection to model
	//         the Zombie infection network.
	public void infect()
	{
		GridPoint point = grid.getLocation(this);
		List<Object> humans = new ArrayList<Object>();
		for(Object obj : grid.getObjectsAt(point.getX(), point.getY()))
		{
			if(obj instanceof Human)
			{
				humans.add(obj);
			}
		}
		
		if(humans.size() > 0)
		{
			int index = RandomHelper.nextIntFromTo(0, humans.size() - 1);
			Object obj = humans.get(index);
			NdPoint spacePoint = space.getLocation(obj);
			Context<Object> context = ContextUtils.getContext(obj);
			context.remove(obj);
			Zombie zombie = new Zombie(space, grid);
			context.add(zombie);
			space.moveTo(zombie, spacePoint.getX(), spacePoint.getY());
			grid.moveTo(zombie,  point.getX(), point.getY());
			
			Network<Object> net = (Network<Object>) context.getProjection("infection network");
			net.addEdge(this, zombie);
		}
		
	}
	

}
