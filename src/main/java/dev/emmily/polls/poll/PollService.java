package dev.emmily.polls.poll;

import dev.emmily.polls.PollsPlugin;
import dev.emmily.polls.message.Messages;
import dev.emmily.sigma.api.repository.CachedAsyncModelRepository;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.yushust.message.MessageHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;

public class PollService
  implements Iterable<Poll> {
  private final PollsPlugin plugin;
  private final MessageHandler messageHandler;
  private final CachedAsyncModelRepository<Poll> pollRepository;

  @Inject
  public PollService(MessageHandler messageHandler, PollsPlugin plugin,
                     CachedAsyncModelRepository<Poll> pollRepository) {
    this.messageHandler = messageHandler;
    this.plugin = plugin;
    this.pollRepository = pollRepository;
  }

  public boolean expire(Poll poll) {
    if (!poll.closed() || poll.expireDate() < System.currentTimeMillis()) {
      return false;
    }

    Object2IntMap<String> options = poll.options();
    int totalVotes = options.values().intStream().sum();

    List<String> results = options
      .object2IntEntrySet()
      .stream()
      .sorted((e1, e2) -> Integer.compare(e2.getIntValue(), e1.getIntValue()))
      .map(entry -> {
        int votes = entry.getIntValue();
        float percentage = totalVotes == 0
          ? 0f
          : (float) (votes * 100) / totalVotes;

        return entry.getKey() + ": " + votes + "(" + String.format("%.2f", percentage) + "%)"; // hardcoded lol
      })
      .toList();

    for (Player player : Bukkit.getOnlinePlayers()) {
      List<Component> message = Messages.fromList(Messages.replace(messageHandler.getMany(
        player, "poll.ended"
      ), "%results%", () -> results));

      message.forEach(player::sendMessage);
    }

    return true;
  }

  public void scheduleExpiration(Poll poll) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> expire(poll), poll.expireDate());
  }

  public void vote(Poll poll, String option, Player player) {
    if (poll.hasExpired()) {
      messageHandler.send(player, "poll.already-ended");

      return;
    }

    if (!poll.canVote(player)) {
      messageHandler.send(player, "poll.already-voted");

      return;
    }

    poll.options().computeInt(option, (k, v) -> v + 1);
    poll.voters().add(player.getUniqueId());
  }

  public void register(Poll poll) {
    scheduleExpiration(poll);
  }

  public Poll get(String id) {
    return pollRepository.get(id);
  }

  public List<Poll> polls() {
    return pollRepository.getAll();
  }

  @Override
  public @NotNull Iterator<Poll> iterator() {
    return pollRepository.getAll().iterator();
  }

  private record IntAndPercentage(int value,
                                  float percentage) {
  }
}
