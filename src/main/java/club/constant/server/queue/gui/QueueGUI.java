package club.constant.server.queue.gui;

import club.constant.server.ConstantServer;
import club.constant.server.queue.MatchQueue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public class QueueGUI {

    private final static Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, "Queues");

    private static final ConstantServer server = ConstantServer.INSTANCE;

    static {
        MinecraftServer.getSchedulerManager().scheduleTask(QueueGUI::update, TaskSchedule.millis(0), TaskSchedule.millis(50));
        ItemStack createQueue1 = ItemStack.builder(Material.NETHER_STAR).displayName(Component.text("Create 1v1 Queue!", NamedTextColor.GOLD)).build();
        ItemStack createQueue2 = ItemStack.builder(Material.NETHER_STAR).displayName(Component.text("Create 2v2 Queue!", NamedTextColor.GOLD)).build();
        ItemStack createQueue3 = ItemStack.builder(Material.NETHER_STAR).displayName(Component.text("Create 3v3 Queue!", NamedTextColor.GOLD)).build();
        ItemStack createQueue4 = ItemStack.builder(Material.NETHER_STAR).displayName(Component.text("Create 4v4 Queue!", NamedTextColor.GOLD)).build();
        inventory.setItemStack(44, createQueue4);
        inventory.setItemStack(43, createQueue3);
        inventory.setItemStack(42, createQueue2);
        inventory.setItemStack(41, createQueue1);
        inventory.addInventoryCondition(((player, slot, clickType, inventoryConditionResult) -> {
            if (inventoryConditionResult.getClickedItem().material() == Material.NETHER_STAR) {
                if (server.getQueueManager().getQueue(player) != null) {
                    player.sendMessage("You are already in a queue!");
                } else {
                    switch (slot) {
                        case 41 -> {
                            if (server.getQueueManager().getFromMaxSize(2).size() >= 20) {
                                player.sendMessage("There are already 20 queues with this type!");
                            } else {
                                player.sendMessage("You started a 1v1 queue!");
                                MatchQueue queue = new MatchQueue(2);
                                queue.addPlayer(player);
                            }
                        }
                        case 42 -> {
                            if (server.getQueueManager().getFromMaxSize(4).size() >= 8) {
                                player.sendMessage("There are already 8 queues with this type!");
                            } else {
                                player.sendMessage("You started a 2v2 queue!");
                                MatchQueue queue2 = new MatchQueue(4);
                                queue2.addPlayer(player);
                            }
                        }
                        case 43 -> {
                            if (server.getQueueManager().getFromMaxSize(6).size() >= 6) {
                                player.sendMessage("There are already 6 queues with this type!");
                            } else {
                                player.sendMessage("You started a 3v3 queue!");
                                MatchQueue queue3 = new MatchQueue(6);
                                queue3.addPlayer(player);
                            }
                        }
                        case 44 -> {
                            if (server.getQueueManager().getFromMaxSize(8).size() >= 6) {
                                player.sendMessage("There are already 6 queues with this type!");
                            } else {
                                player.sendMessage("You started a 4v4 queue!");
                                MatchQueue queue4 = new MatchQueue(8);
                                queue4.addPlayer(player);
                            }
                        }
                    }
                }
            } else if (inventoryConditionResult.getClickedItem().material() == Material.PAPER) {
                MatchQueue queue = server.getQueueManager().getQueue(inventoryConditionResult.getClickedItem().getTag(Tag.UUID("queue")));
                if (!queue.getQueue().contains(player)) {
                    queue.addPlayer(player);
                }
            }
            inventoryConditionResult.setCancel(true);
        }));
    }

    public static void open(Player player) {
        player.openInventory(inventory);
    }

    private static void update() {
        List<MatchQueue> sorted = server.getQueueManager().getSorted();
        for (int i = 0; i < 40; i++) {
            try {
                MatchQueue queue = sorted.get(i);
                inventory.setItemStack(i, queue.getItemStack());
            } catch (IndexOutOfBoundsException ex) {
                inventory.setItemStack(i, ItemStack.builder(Material.AIR).build());
            }
        }
        inventory.update();
    }

}
