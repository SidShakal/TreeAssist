package me.itsatacoshop247.TreeAssist.blocklists;

import me.itsatacoshop247.TreeAssist.core.TreeBlock;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlatFileBlockList implements BlockList {
    final FileConfiguration config = new YamlConfiguration();
    ConfigurationSection treeBlocks;
    File configFile;

    @Override
    public void addBlock(Block block) {
        if (treeBlocks == null || block == null) {
            return;
        }
        final long time = System.currentTimeMillis();
        TreeBlock treeBlock = new TreeBlock(block, time);
        treeBlocks.set(treeBlock.getId(), treeBlock);
    }

    @Override
    public void initiate() {
        configFile = new File(Utils.plugin.getDataFolder(), "data.yml");

        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            config.load(configFile);
            if (config.contains("Blocks")) {
                File backupFile = new File(Utils.plugin.getDataFolder(), "data_backup-" + System.currentTimeMillis() + ".yml");
                if (!backupFile.exists()) {
                    config.save(backupFile);
                }
                final List<String> list = config.getStringList("Blocks");
                treeBlocks = config.createSection("TreeBlocks");
                for (String entry : list) {
                    String[] split = entry.split(";");
                    if (split.length == 4) {
                        // legacy²
                        // X;Y;Z;W
                        try {
                            long time = System.currentTimeMillis();
                            int x = Integer.parseInt(split[0]);
                            int y = Integer.parseInt(split[1]);
                            int z = Integer.parseInt(split[2]);
                            String world = split[3];
                            TreeBlock treeBlock = new TreeBlock(x, y, z, world, time);
                            treeBlocks.set(treeBlock.getId(), treeBlock);
                        } catch (Exception e) {
                        }
                    } else if (split.length == 5) {
                        // legacy
                        // X;Y;Z;T;W
                        try {
                            long time = Long.parseLong(split[3]);
                            int x = Integer.parseInt(split[0]);
                            int y = Integer.parseInt(split[1]);
                            int z = Integer.parseInt(split[2]);
                            String world = split[4];
                            TreeBlock treeBlock = new TreeBlock(x, y, z, world, time);
                            treeBlocks.set(treeBlock.getId(), treeBlock);
                        } catch (Exception e) {
                        }
                    } else {
                        continue;
                    }
                }
                config.set("Blocks", null);
            } else if (config.isList("TreeBlocks")) {
                // Config is the briefly used in development TreeBlocks list format.
                List<?> treeBlockList = config.getList("TreeBlocks");
                treeBlocks = config.createSection("TreeBlocks");
                for (Object o : treeBlockList) {
                    TreeBlock treeBlock = (TreeBlock) o;
                    treeBlocks.set(treeBlock.getId(), treeBlock);
                }
            } else if (!config.isConfigurationSection("TreeBlocks")) {
                treeBlocks = config.createSection("TreeBlocks");
            } else {
                treeBlocks = config.getConfigurationSection("TreeBlocks");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(Utils.plugin, new Runnable() {
            @Override
            public void run() {
                FlatFileBlockList.this.save(true);
            }
        }, 1200, 1200);
    }


    @Override
    public boolean isPlayerPlaced(final Block block) {
        if (treeBlocks == null || block == null) {
            return false;
        }
        TreeBlock check = new TreeBlock(block, 0);
        return treeBlocks.contains(check.getId());
    }

    @Override
    public void logBreak(Block block, Player player) {
        removeBlock(block);
    }

    @Override
    public void removeBlock(final Block block) {
        if (treeBlocks == null || block == null) {
            return;
        }
        TreeBlock check = new TreeBlock(block, 0);
        treeBlocks.set(check.getId(), null);
    }

    public int purge(final CommandSender sender) {
        final List<TreeBlock> removals = new ArrayList<TreeBlock>();
        for (String treeBlockId : treeBlocks.getKeys(false)) {
            TreeBlock block = (TreeBlock) treeBlocks.get(treeBlockId);
            if (Bukkit.getWorld(block.world) == null) {
                removals.add(block);
                continue;
            }
            Block bukkitBlock = block.getBukkitBlock();
            if (bukkitBlock.getType() != Material.LOG &&
                    !bukkitBlock.getType().name().equals(Material.LOG_2)) {
                removals.add(block);
            }
        }
        for (TreeBlock block : removals) {
            treeBlocks.set(block.getId(), null);
        }
        save(true);
        return removals.size();
    }

    public int purge(final String worldname) {
        final List<TreeBlock> removals = new ArrayList<TreeBlock>();
        for (String treeBlockId : treeBlocks.getKeys(false)) {
            TreeBlock block = (TreeBlock) treeBlocks.get(treeBlockId);
            if (block.world.toLowerCase().endsWith(worldname.toLowerCase())) {
                removals.add(block);
            }
        }
        for (TreeBlock block : removals) {
            treeBlocks.set(block.getId(), null);
        }
        save(true);
        return removals.size();
    }

    public int purge(final int days) {
        final List<TreeBlock> removals = new ArrayList<TreeBlock>();
        for (String treeBlockId : treeBlocks.getKeys(false)) {
            TreeBlock block = (TreeBlock) treeBlocks.get(treeBlockId);
            long time = block.time;
            if (time < (System.currentTimeMillis() - days * 24 * 60 * 60 * 1000)) {
                removals.add(block);
            }
        }
        for (TreeBlock block : removals) {
            treeBlocks.set(block.getId(), null);
        }
        save(true);
        return removals.size();
    }

    @Override
    public void save() {
    }

    @Override
    public void save(boolean force) {
        this.saveData();
    }

    private void saveData() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
