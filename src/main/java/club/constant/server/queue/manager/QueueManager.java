package club.constant.server.queue.manager;

import club.constant.server.queue.MatchQueue;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class QueueManager {

    private final List<MatchQueue> queues = new ArrayList<>();

    public void addQueue(MatchQueue queue) {
        queues.add(queue);
    }

    public MatchQueue getQueue(UUID uuid) {
        return queues.stream().filter(queue -> queue.getUUID().equals(uuid)).findFirst().orElse(null);
    }

    public MatchQueue getQueue(Player player) {
        return queues.stream().filter(queue -> queue.getQueue().contains(player)).findFirst().orElse(null);
    }

    public List<MatchQueue> getQueues() {
        return queues;
    }

    public List<MatchQueue> getSorted() {
        return getQueues().stream().sorted().collect(Collectors.toList());
    }

    public List<MatchQueue> getFromMaxSize(int maxSize) {
        return getQueues().stream().filter(queue -> queue.getMaxSize() == maxSize).collect(Collectors.toList());
    }

}
