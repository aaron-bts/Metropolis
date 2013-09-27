package ch.k42.metropolis.model;

import ch.k42.metropolis.generator.MetropolisGenerator;
import ch.k42.metropolis.minions.ByteChunk;
import ch.k42.metropolis.minions.GridRandom;
import com.google.common.base.Optional;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 16.09.13
 * Time: 00:46
 * To change this template use File | Settings | File Templates.
 */
public class GridProvider {

    public static final int GRID_SIZE = 64;

    public static class GridKey{
        protected int gridX;
        protected int gridZ;
        public GridKey(int chunkX, int chunkZ) {
            gridX = getGridOrigin(chunkX); // (chunkX/GRID_SIZE) *GRID_SIZE; // FIXME bitops would be faster....
            gridZ = getGridOrigin(chunkZ); // (chunkZ/GRID_SIZE) *GRID_SIZE; // TODO FIXME EVERYTHING
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GridKey gridKey = (GridKey) o;

            if (gridX != gridKey.gridX) return false;
            if (gridZ != gridKey.gridZ) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = gridX;
            result = 31 * result + gridZ;
            return result;
        }
    }

    private Map<GridKey,Optional<Grid>> grids = new HashMap();
    private World world;

    public GridProvider(MetropolisGenerator generator) {
        this.world = generator.getWorld();
    }

    /**
     * Evaluates the Parcel at the given absolute chunk coordinates
     * @param chunkX x coordinate
     * @param chunkZ z coordinate
     * @return parcel at the given coordinate
     */
    public Parcel getParcel(int chunkX, int chunkZ){
        int x = getChunkOffset(chunkX);
        int z = getChunkOffset(chunkZ);
        return getGrid(chunkX,chunkZ).getParcel(x,z);
    }

    /**
     * Returns the grid at the given relative coordinates in the interval [0,Grid.GRIDSIZE)
     * @param chunkX chunk coordinates x
     * @param chunkZ chunk coordinates z
     * @return the grid at the given coordinate or null if there is none
     */
    public Grid getGrid(int chunkX, int chunkZ){
        GridKey key = new GridKey(chunkX,chunkZ);
        Optional<Grid> ogrid = grids.get(key);

        if(ogrid==null){ // does it exsist? or do we need to create one?
            ogrid = getNewGrid(chunkX,chunkZ); //TODO not always same grid
            grids.put(key,ogrid);
        }

        return ogrid.get();
    }
    /**
     * Places the Parcel at the given absolute chunk coordinates
     * @param chunkX x coordinate
     * @param chunkZ z coordinate
     * @param parcel at the given coordinate
     */
    public void setParcel(int chunkX, int chunkZ, Parcel parcel){
        int x = getChunkOffset(chunkX);
        int z = getChunkOffset(chunkZ);
        getGrid(chunkX,chunkZ).setParcel(x,z,parcel);
    }

    /**
     * Evaluates the Parcel at the given absolute chunk coordinates
     * @param chunkX x coordinate
     * @param chunkZ z coordinate
     * @return parcel at the given coordinate
     */
    public GridRandom getRandom(int chunkX, int chunkZ){
        return getGrid(chunkX,chunkZ).getRandom();
    }

    public void populate(MetropolisGenerator generator, Chunk chunk){
        Parcel p = getParcel(chunk.getX(),chunk.getZ());
        if(p!=null){
            p.populate(generator,chunk);
        }else {
            generator.reportDebug("found empty Parcel: ["+chunk.getX()+"]["+chunk.getZ()+"]");
        }
    }

    private Optional<Grid> getNewGrid(int chunkX,int chunkZ){

        int originX = getGridOrigin(chunkX);
        int originZ = getGridOrigin(chunkZ);

        long seed = world.getSeed();
        return Optional.of((Grid) new UrbanGrid(this,new GridRandom(seed,originX,originZ),originX,originZ)); // FIXME mooaaar grids
    }

    private static int getGridOrigin(int chunk){
        int ret=0;
        if(chunk<0){
            chunk += 1;
            ret = (chunk) - ((chunk)%GRID_SIZE);
            ret -= GRID_SIZE;
        }else {
            ret =  chunk-(chunk%GRID_SIZE);
        }
        return ret;
    }

    private static int getChunkOffset(int chunk){
        int ret = chunk % GRID_SIZE;
        if(ret<0)
            ret+=GRID_SIZE;
        return ret;
    }

}
