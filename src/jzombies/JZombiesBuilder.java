/**
 * 
 */
package jzombies;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

/**
 * @author aniru
 *
 */
public class JZombiesBuilder implements ContextBuilder<Object> {
	
	

	@Override
	public Context build(Context<Object> context) {
		// TODO Auto-generated method stub		
		NetworkBuilder <Object> netBuilder = new NetworkBuilder <Object>("infection network", context , true );
		netBuilder.buildNetwork();
		
		context.setId("jzombies");
		
		// Creating a continuous space projection
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(), 50, 50);
		
		// Creating a grid projection
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);		
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, 50, 50));
		
		
		int zombieCount = 5;
		for(int i = 0; i < zombieCount; i++)
		{
			context.add(new Zombie(space, grid));
		}
		
		int humanCount = 500;
		for(int i = 0; i < humanCount; i++)
		{
			int energy = RandomHelper.nextIntFromTo(4, 10);
			context.add(new Human(space, grid, energy));
		}
		
		// Iterating through each object in the context and retreiving the location of each one in the ContinuousSpace and moving it to the corresponding location in the Grid;
		for(Object obj : context)
		{
			NdPoint point = space.getLocation(obj);
			grid.moveTo(obj, (int)point.getX(), (int)point.getY());
		}
		
		return context;
	}

}
