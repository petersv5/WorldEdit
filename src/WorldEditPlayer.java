// $Id$
/*
 * WorldEditLibrary
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.blocks.BlockType;

/**
 *
 * @author sk89q
 */
public abstract class WorldEditPlayer {
    /**
     * Server interface.
     */
    protected ServerInterface server;

    /**
     * Construct the player.
     */
    public WorldEditPlayer() {
        server = WorldEditController.getServer();
    }

    /**
     * Get the name of the player.
     *
     * @return String
     */
    public abstract String getName();
    /**
     * Get the point of the block that is being stood upon.
     *
     * @return point
     */
    public abstract Vector getBlockOn();
    /**
     * Get the point of the block that is being stood in.
     *
     * @return point
     */
    public abstract Vector getBlockIn();
    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range
     * @return point
     */
    public abstract Vector getBlockTrace(int range);
    /**
     * Get the player's position.
     *
     * @return point
     */
    public abstract Vector getPosition();
    /**
     * Get the player's view pitch.
     *
     * @return pitch
     */
    public abstract double getPitch();
    /**
     * Get the player's view yaw.
     *
     * @return yaw
     */
    public abstract double getYaw();
    /**
     * Get the ID of the item that the player is holding.
     *
     * @return
     */
    public abstract int getItemInHand();

    /**
     * Returns true if the player is holding a pick axe.
     *
     * @return whether a pick axe is held
     */
    public boolean isHoldingPickAxe() {
        int item = getItemInHand();
        return item == 257 || item == 270 || item == 274 || item == 278
                || item == 285;
    }

    /**
     * Get the player's cardinal direction (N, W, NW, etc.).
     *
     * @return
     */
    public abstract String getCardinalDirection();

    /**
     * Print a WorldEditLibrary message.
     *
     * @param msg
     */
    public abstract void print(String msg);

    /**
     * Print a WorldEditLibrary error.
     *
     * @param msg
     */
    public abstract void printError(String msg);

    /**
     * Move the player.
     *
     * @param pos
     * @param pitch
     * @param yaw
     */
    public abstract void setPosition(Vector pos, float pitch, float yaw);

    /**
     * Move the player.
     *
     * @param pos
     */
    public void setPosition(Vector pos) {
        setPosition(pos, (float)getPitch(), (float)getYaw());
    }

    /**
     * Find a position for the player to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The player will be teleported to
     * that free position.
     *
     * @param searchPos search position
     */
    public void findFreePosition(Vector searchPos) {
        int x = searchPos.getBlockX();
        int y = searchPos.getBlockY();
        int origY = y;
        int z = searchPos.getBlockZ();

        byte free = 0;

        while (y <= 129) {
            if (etc.getServer().getBlockIdAt(x, y, z) == 0) {
                free++;
            } else {
                free = 0;
            }

            if (free == 2) {
                if (y - 1 != origY) {
                    setPosition(new Vector(x + 0.5, y - 1, z + 0.5));
                    return;
                }
            }

            y++;
        }
    }

    /**
     * Find a position for the player to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The player will be teleported to
     * that free position.
     */
    public void findFreePosition() {
        findFreePosition(getBlockIn());
    }

    /**
     * Pass through the wall that you are looking at.
     *
     * @param range
     * @return whether a wall was passed through
     */
    public abstract boolean passThroughForwardWall(int range);

    /**
     * Go up one level to the next free space above.
     *
     * @return true if a spot was found
     */
    public boolean ascendLevel() {
        Vector pos = getBlockIn();
        int x = pos.getBlockX();
        int y = pos.getBlockY();
        int z = pos.getBlockZ();

        byte free = 0;
        byte spots = 0;

        while (y <= 129) {
            if (server.getBlockType(new Vector(x, y, z)) == 0) {
                free++;
            } else {
                free = 0;
            }

            if (free == 2) {
                spots++;
                if (spots == 2) {
                    int type = server.getBlockType(new Vector(x, y - 2, z));
                    
                    // Don't get put in lava!
                    if (type == 10 || type == 11) {
                        return false;
                    }

                    setPosition(new Vector(x + 0.5, y - 1, z + 0.5));
                    return true;
                }
            }

            y++;
        }

        return false;
    }

    /**
     * Go up one level to the next free space above.
     *
     * @return true if a spot was found
     */
    public boolean descendLevel() {
        Vector pos = getBlockIn();
        int x = pos.getBlockX();
        int y = pos.getBlockY() - 1;
        int z = pos.getBlockZ();

        byte free = 0;

        while (y >= 1) {
            if (server.getBlockType(new Vector(x, y, z)) == 0) {
                free++;
            } else {
                free = 0;
            }

            if (free == 2) {
                // So we've found a spot, but we have to drop the player
                // lightly and also check to see if there's something to
                // stand upon
                while (y >= 0) {
                    int type = server.getBlockType(new Vector(x, y, z));

                    // Don't want to end up in lava
                    if (type != 0 && type != 10 && type != 11) {
                        // Found a block!
                        setPosition(new Vector(x + 0.5, y + 1, z + 0.5));
                        return true;
                    }
                    
                    y--;
                }

                return false;
            }

            y--;
        }

        return false;
    }

    /**
     * Ascend to the ceiling above.
     * 
     * @param clearance
     * @return whether the player was moved
     */
    public boolean ascendToCeiling(int clearance) {
        Vector pos = getBlockIn();
        int x = pos.getBlockX();
        int initialY = pos.getBlockY();
        int y = pos.getBlockY() + 2;
        int z = pos.getBlockZ();
        
        // No free space above
        if (server.getBlockType(new Vector(x, y, z)) != 0) {
            return false;
        }

        while (y <= 127) {
            // Found a ceiling!
            if (server.getBlockType(new Vector(x, y, z)) != 0) {
                int platformY = Math.max(initialY, y - 3 - clearance);
                server.setBlockType(new Vector(x, platformY, z),
                        BlockType.GLASS.getID());
                setPosition(new Vector(x + 0.5, platformY + 1, z + 0.5));
                return true;
            }

            y++;
        }

        return false;
    }

    /**
     * Gives the player an item.
     *
     * @param type
     * @param amt
     */
    public abstract void giveItem(int type, int amt);

    /**
     * Returns true if equal.
     *
     * @param other
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WorldEditPlayer)) {
            return false;
        }
        WorldEditPlayer other2 = (WorldEditPlayer)other;
        return other2.getName().equals(getName());
    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
