package com.lol768.LiteKits.extensions.cooldowns;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import com.lol768.LiteKits.LiteKits;
import com.lol768.LiteKits.API.KitCheckEvent;

public class Cooldowns extends JavaPlugin implements Listener {
    private LiteKits lk;
    
    public void onEnable() {
        Object obj = Bukkit.getServer().getPluginManager().getPlugin("LiteKits");
        if (obj != null) {
            lk = (LiteKits) obj;
            if (lk.getDescription().getVersion().equals("1.0") || lk.getDescription().getVersion().equals("1.1") || lk.getDescription().getVersion().equals("1.2")) {
                getLogger().severe("LiteKits version is too old to use this extension. Disabling self...");
                setEnabled(false);
            } else {
                Bukkit.getPluginManager().registerEvents(this, this);
            }
        } else {
            getLogger().severe("Couldn't find LiteKits. Disabling self...");
            setEnabled(false);
        }
        
        if (getConfig().getLong("cooldown-default", -1) < 0) {
            getConfig().set("cooldown-default", 60);
            saveConfig();
        }
        
        if (!getConfig().contains("once-per-life")) {
            getConfig().set("once-per-life", false);
            saveConfig();
        }
        
        if (!getConfig().contains("once-per-world")) {
           getConfig().set("once-per-world", false);
           saveConfig();
        }
        
        if (!getConfig().contains("clear-cooldown-on-death")) {
            getConfig().set("clear-cooldown-on-death", true);
            saveConfig();
        }
        
        
    }
    
    @EventHandler(ignoreCancelled=true)
    public void onKitAttempt(KitCheckEvent e) {
        if(getConfig().getBoolean("once-per-world", false) && getMetadata(e.getPlayer(), "usedInWorld-" + e.getPlayer().getWorld().getName()) != null){
            e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.RED + "You can only recieve this kit once per world");
            e.setCancelled(true);
            return;
        } 
        if (getConfig().getBoolean("once-per-life", false) && getMetadata(e.getPlayer(), "gotKitThisLife-" + e.getKitName()) != null && ((Boolean)getMetadata(e.getPlayer(), "gotKitThisLife"))) {
            e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.RED + "You can only receieve one kit per life");
            e.setCancelled(true);
            return;
        }
        Long amount = (Long) getMetadata(e.getPlayer(), "lastKitTime-" + e.getKitName());
        long now = System.currentTimeMillis() / 1000l;
        long cda = (getConfig().contains("cooldown-" + e.getKitName())) ? getConfig().getLong("cooldown-" + e.getKitName()) : getConfig().getLong("cooldown-default", 0);
        if (amount != null) {
           
            if ((now - amount) < cda) {
                String word = ((cda - (now - amount)) == 1) ? "second" : "seconds";
                e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.RED + "You must wait " + (cda - (now - amount)) + " more " + word);
                e.setCancelled(true);
                return;
            }
        }
        setMetadata(e.getPlayer(), "usedInWorld-" + e.getPlayer().getWorld().getName(), true);
        setMetadata(e.getPlayer(), "lastKitTime-" + e.getKitName(), now);
        setMetadata(e.getPlayer(), "gotKitThisLife", true);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        setMetadata(e.getEntity(), "gotKitThisLife", false);
        if ((boolean) getConfig().get("clear-cooldown-on-death", true)) {
            for (String k : lk.getConfig().getConfigurationSection("kits").getKeys(false)) {
                if (e.getEntity().hasMetadata("lastKitTime-" + k)) {
                    e.getEntity().removeMetadata("lastKitTime-" + k, this);
                }
            }
        }
    }
    
    public void setMetadata(Player player, String key, Object value) {
        player.setMetadata(key,new FixedMetadataValue(this,value));
      }
      public Object getMetadata(Player player, String key) {
        List<MetadataValue> values = player.getMetadata(key);  
        for(MetadataValue value : values){
           if(value.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName())){
              return value.value();
           }
        }
        return null;
      }
    
   

}
