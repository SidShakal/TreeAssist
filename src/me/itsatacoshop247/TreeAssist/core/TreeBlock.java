package me.itsatacoshop247.TreeAssist.core;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class TreeBlock implements ConfigurationSerializable {
    private final int x, y, z;
    public final String world;
    public final long time;

    public TreeBlock(final Block b, final long timestamp) {
        this(b.getX(),
             b.getY(),
             b.getZ(),
             b.getWorld().getName(),
             timestamp);
    }

    public TreeBlock(final Map<String, Object> map) {
        this((Integer) map.get("x"),
             (Integer) map.get("y"),
             (Integer) map.get("z"),
             map.get("w").toString(),
             (Long) map.get("t"));
    }

    public TreeBlock(final int x, final int y, final int z, final String world, final long timestamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.time = timestamp;
    }

    public Block getBukkitBlock() {
        return Bukkit.getWorld(world).getBlockAt(x, y, z);
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("w", world);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("t", time);
        return map;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        final TreeBlock theOther = (TreeBlock) other;
        if (this.x != theOther.x) {
            return false;
        }
        if (this.y != theOther.y) {
            return false;
        }
        if (this.z != theOther.z) {
            return false;
        }
        return this.world.equals(theOther.world);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (world == null ? 0 : world.hashCode());
        result = prime * result + (x ^ x >>> 32);
        result = prime * result + (y ^ y >>> 32);
        result = prime * result + (z ^ z >>> 32);
        return result;
    }

    public String getId() {
        return x + ";" + y + ";" + z + ";" + world;
    }
}
