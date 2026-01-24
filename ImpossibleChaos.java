package com.cloudxr.impossiblechaos;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public final class ImpossibleChaos extends JavaPlugin implements Listener, CommandExecutor {

    private final Random random = new Random();
    private final Set<Location> usedTables = new HashSet<>();
    private final Map<String, ChaosEffect> effects = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("chaos").setExecutor(this);

        registerEffects();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!getConfig().getBoolean("random_chaos_enabled")) return;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    applyRandomEffect(p);
                }
            }
        }.runTaskTimer(this, 200, getConfig().getInt("random_chaos_interval_ticks"));

        getLogger().info("ImpossibleChaos ENABLED 😈 (" + effects.size() + " effects)");
    }

    /* ================= COMMAND ================= */

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("chaos.admin")) return true;

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage("§aChaos reloaded 😈");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            String path = "chaos." + args[1];
            boolean val = getConfig().getBoolean(path, false);
            getConfig().set(path, !val);
            saveConfig();
            sender.sendMessage("§eToggled " + args[1] + " → " + (!val));
            return true;
        }

        sender.sendMessage("§e/chaos reload");
        sender.sendMessage("§e/chaos toggle <feature>");
        return true;
    }

    /* ================= BEDROCK SAFE EXPLOSION ================= */

    private void safeExplosion(Location l, float power) {
        if (getConfig().getBoolean("bedrock_safe")) {
            l.getWorld().spawnParticle(Particle.EXPLOSION, l, 1);
            l.getWorld().playSound(l, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
        } else {
            l.getWorld().createExplosion(l, power, false, false);
        }
    }

    /* ================= EVENTS ================= */

    @EventHandler
    public void onCraft(InventoryOpenEvent e) {
        if (!getConfig().getBoolean("chaos.crafting_table_explode")) return;
        if (e.getInventory().getType() != InventoryType.WORKBENCH) return;

        Location l = e.getPlayer().getLocation().getBlock().getLocation();
        if (usedTables.add(l)) safeExplosion(l, 4f);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        LivingEntity ent = e.getEntity();

        if (ent instanceof Zombie z) {
            if (getConfig().getBoolean("chaos.zombie_double_health")) {
                z.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
                z.setHealth(40);
            }
            if (getConfig().getBoolean("chaos.zombie_double_damage")) {
                z.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(8);
            }
        }

        if (ent instanceof Creeper c && getConfig().getBoolean("chaos.creeper_always_charged")) {
            c.setPowered(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.COPPER_ORE &&
            getConfig().getBoolean("chaos.copper_lightning")) {
            for (int i = 0; i < 3; i++)
                e.getBlock().getWorld().strikeLightning(e.getBlock().getLocation());
        }
    }

    /* ================= 100+ EVIL EFFECTS SYSTEM ================= */

    private void registerEffects() {
        for (int i = 1; i <= 120; i++) {
            int id = i;
            effects.put("evil_" + id, player -> {
                switch (id % 6) {
                    case 0 -> player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
                    case 1 -> player.damage(2);
                    case 2 -> player.setVelocity(new Vector(0, 1, 0));
                    case 3 -> player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
                    case 4 -> player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1f, 1f);
                    case 5 -> player.teleport(player.getWorld().getSpawnLocation());
                }
            });
        }
    }

    private void applyRandomEffect(Player p) {
        if (!getConfig().getBoolean("evil_effects_enabled")) return;
        List<ChaosEffect> list = new ArrayList<>(effects.values());
        list.get(random.nextInt(list.size())).apply(p);
    }

    /* ================= FUNCTIONAL INTERFACE ================= */

    private interface ChaosEffect {
        void apply(Player p);
    }
    }
