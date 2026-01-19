import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ImpossibleChaos extends JavaPlugin implements Listener {

    Random random = new Random();
    Set<Location> usedTables = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    randomChaos(p);
                }
            }
        }.runTaskTimer(this, 200, 400);

        getLogger().info("ImpossibleChaos ENABLED 😈");
    }

    /* ================= COMMAND ================= */

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("chaos")) {

            if (!sender.hasPermission("chaos.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "ImpossibleChaos config reloaded 😈");
                return true;
            }

            sender.sendMessage(ChatColor.YELLOW + "Usage: /chaos reload");
            return true;
        }

        return false;
    }

    /* ================= CRAFTING ================= */

    @EventHandler
    public void onCraft(InventoryOpenEvent e) {
        if (!getConfig().getBoolean("crafting_table_explode")) return;
        if (e.getInventory().getType() != InventoryType.WORKBENCH) return;

        Player p = (Player) e.getPlayer();
        Location l = p.getLocation().getBlock().getLocation();
        if (usedTables.contains(l)) return;

        usedTables.add(l);
        l.getWorld().createExplosion(l, 4f, false, false);
    }

    /* ================= MOB SPAWN ================= */

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        LivingEntity ent = e.getEntity();

        if (ent instanceof Zombie z) {
            if (getConfig().getBoolean("zombie_double_health")) {
                z.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
                z.setHealth(40);
            }
            if (getConfig().getBoolean("zombie_double_damage")) {
                z.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(8);
            }
            if (getConfig().getBoolean("zombie_random_armor")) {
                z.getEquipment().setHelmet(randomArmor());
            }
        }

        if (ent instanceof Skeleton s) {
            if (getConfig().getBoolean("skeleton_aimbot")) {
                s.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(100);
            }
            if (getConfig().getBoolean("skeleton_rapid_fire")) {
                new BukkitRunnable() {
                    public void run() {
                        if (s.isDead()) cancel();
                        s.launchProjectile(Arrow.class);
                    }
                }.runTaskTimer(this, 0, 5);
            }
        }

        if (ent instanceof Creeper c && getConfig().getBoolean("creeper_always_charged")) {
            c.setPowered(true);
        }

        if (ent instanceof Enderman em && getConfig().getBoolean("enderman_always_angry")) {
            for (Player p : Bukkit.getOnlinePlayers()) em.setTarget(p);
        }

        if (ent instanceof IronGolem g && getConfig().getBoolean("iron_golem_long_reach")) {
            g.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(100);
            g.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(40);
        }

        if (ent instanceof EnderDragon d && getConfig().getBoolean("ender_dragon_double_health")) {
            d.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(400);
            d.setHealth(400);
        }
    }

    /* ================= BLOCK BREAK ================= */

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        World w = b.getWorld();

        if (b.getType() == Material.STONE && getConfig().getBoolean("stone_random_drops")) {
            e.setDropItems(false);
            w.dropItemNaturally(b.getLocation(), randomItem());
        }

        if (b.getType().toString().contains("LOG") && getConfig().getBoolean("wood_shoots_sticks")) {
            for (int i = 0; i < 6; i++) {
                Arrow a = w.spawn(b.getLocation(), Arrow.class);
                a.setVelocity(e.getPlayer().getDirection().multiply(2));
            }
        }

        if (b.getType() == Material.DIAMOND_ORE && getConfig().getBoolean("diamond_spawn_creeper")) {
            w.spawnEntity(b.getLocation(), EntityType.CREEPER);
        }

        if (b.getType() == Material.OBSIDIAN && getConfig().getBoolean("obsidian_spawn_enderman")) {
            w.spawnEntity(b.getLocation(), EntityType.ENDERMAN);
        }

        if (b.getType() == Material.COPPER_ORE && getConfig().getBoolean("copper_lightning")) {
            for (int i = 0; i < 3; i++) w.strikeLightning(b.getLocation());
        }
    }

    /* ================= PLAYER ================= */

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Block below = p.getLocation().subtract(0,1,0).getBlock();

        if (below.getType().toString().contains("BED") &&
            getConfig().getBoolean("bed_launch_player")) {
            p.setVelocity(new Vector(0, 5, 0));
        }
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        if (getConfig().getBoolean("raw_meat_poison") &&
            e.getItem().getType().toString().contains("RAW")) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 400, 2));
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
        }
    }

    /* ================= RANDOM CHAOS ================= */

    void randomChaos(Player p) {
        int r = random.nextInt(6);
        World w = p.getWorld();

        switch (r) {
            case 0 -> w.strikeLightning(p.getLocation());
            case 1 -> w.createExplosion(p.getLocation(), 2f);
            case 2 -> p.damage(4);
            case 3 -> w.spawnEntity(p.getLocation(), EntityType.ZOMBIE);
            case 4 -> p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
            case 5 -> p.teleport(w.getSpawnLocation());
        }
    }

    /* ================= UTILS ================= */

    ItemStack randomArmor() {
        Material[] armor = {
            Material.IRON_HELMET,
            Material.DIAMOND_HELMET,
            Material.NETHERITE_HELMET
        };
        return new ItemStack(armor[random.nextInt(armor.length)]);
    }

    ItemStack randomItem() {
        Material[] m = Material.values();
        return new ItemStack(m[random.nextInt(m.length)]);
    }
    }
