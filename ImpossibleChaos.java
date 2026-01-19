import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

public class ImpossibleChaos extends JavaPlugin implements Listener {

    Set<Location> usedTables = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ImpossibleChaos enabled 😈");
    }

    /* 1 Crafting table explode ONCE */
    @EventHandler
    public void onCraft(InventoryOpenEvent e) {
        if (e.getInventory().getType() != InventoryType.WORKBENCH) return;
        Player p = (Player) e.getPlayer();
        Location l = p.getLocation().getBlock().getLocation();

        if (usedTables.contains(l)) return;
        usedTables.add(l);

        l.getWorld().createExplosion(l, 4F, false, false);
    }

    /* 2 Zombie buff + armor random */
    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Zombie z) {
            z.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
            z.setHealth(40);
            z.getEquipment().setHelmet(randomArmor());
        }

        if (e.getEntity() instanceof Skeleton s) {
            s.getEquipment().setHelmet(randomArmor());
        }

        if (e.getEntity() instanceof Creeper c) {
            c.setPowered(true);
        }
    }

    /* 6 Stone drops random */
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.STONE) {
            e.setDropItems(false);
            e.getBlock().getWorld().dropItemNaturally(
                e.getBlock().getLocation(),
                new ItemStack(Material.values()[new Random().nextInt(Material.values().length)])
            );
        }

        if (e.getBlock().getType().toString().contains("LOG")) {
            for (int i = 0; i < 6; i++) {
                Arrow a = e.getBlock().getWorld().spawn(e.getBlock().getLocation(), Arrow.class);
                a.setVelocity(e.getPlayer().getDirection().multiply(2));
            }
        }

        if (e.getBlock().getType() == Material.COPPER_ORE) {
            for (int i = 0; i < 3; i++)
                e.getBlock().getWorld().strikeLightning(e.getBlock().getLocation());
        }
    }

    /* 11 Step on bed = YEET */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Block b = e.getPlayer().getLocation().subtract(0,1,0).getBlock();
        if (b.getType().toString().contains("BED")) {
            e.getPlayer().setVelocity(new Vector(0, 5, 0));
        }
    }

    /* 16 Raw meat bad */
    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        if (e.getItem().getType().toString().contains("RAW")) {
            e.getPlayer().addPotionEffect(
                new PotionEffect(PotionEffectType.HUNGER, 400, 2));
            e.getPlayer().addPotionEffect(
                new PotionEffect(PotionEffectType.POISON, 200, 1));
        }
    }

    ItemStack randomArmor() {
        Material[] armor = {
            Material.IRON_HELMET,
            Material.DIAMOND_HELMET,
            Material.NETHERITE_HELMET
        };
        return new ItemStack(armor[new Random().nextInt(armor.length)]);
    }
  }
