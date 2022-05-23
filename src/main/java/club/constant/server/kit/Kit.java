package club.constant.server.kit;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class Kit {

    public void giveKit(Player player) {
        player.getInventory().clear();
        player.getInventory().addItemStack(ItemStack.builder(Material.STONE_SWORD).build());
        player.getInventory().setItemInOffHand(ItemStack.builder(Material.SHIELD).build());
    }

}
