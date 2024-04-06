package betterbox.mine.game.betterelo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;


import java.util.UUID;

public class CustomMobs {
    private final PluginLogger pluginLogger;
    public CustomMobs(PluginLogger pluginLogger){
        this.pluginLogger = pluginLogger;
    }
    public void spawnModifiedZombie(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "CustomMobs.spawnModifiedZombie called, player: "+player.getName());
        Location playerLocation = player.getLocation();
        World world = player.getWorld();

        // Tworzenie zombiaka
        Zombie zombie = (Zombie) world.spawnEntity(playerLocation, EntityType.ZOMBIE);

        // Ubieranie zombiaka w zestaw
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);

        zombie.getEquipment().setHelmet(helmet);
        zombie.getEquipment().setChestplate(chestplate);
        zombie.getEquipment().setLeggings(leggings);
        zombie.getEquipment().setBoots(boots);

        // Modyfikowanie parametrów zombiaka
        // Przykładowe zmiany - możesz dostosować według własnych preferencji
        zombie.setMaxHealth(40); // Zwiększenie maksymalnego zdrowia zombiaka
        zombie.setHealth(40); // Ustawienie aktualnego zdrowia na maksymalne
        zombie.setBaby(false); // Upewnienie się, że zombiak nie jest dzieckiem
        zombie.setCustomName("Modyfikowany Zombiak"); // Ustawienie niestandardowego nazwy
        zombie.setCustomNameVisible(true); // Wyświetlanie niestandardowej nazwy nad zombiakiem

        // Dodanie modyfikatora do siły ataku zombiaka
        double attackDamage = 8.0; // Przykładowa wartość siły ataku
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "attack_damage", attackDamage, AttributeModifier.Operation.ADD_NUMBER);
        zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(modifier);
    }
}
