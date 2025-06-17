package dev.emmily.polls.poll;

import com.google.common.base.Strings;
import dev.emmily.polls.PollsPlugin;
import dev.emmily.polls.message.MessageMode;
import dev.emmily.polls.message.Messages;
import dev.emmily.polls.message.provider.PollPlaceholderProvider;
import dev.emmily.polls.util.time.TimeFormatter;
import dev.emmily.sigma.api.repository.CachedAsyncModelRepository;
import me.yushust.message.MessageHandler;
import me.yushust.message.util.ReplacePack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PollService implements Iterable<Poll> {
  private final PollsPlugin plugin;
  private final CachedAsyncModelRepository<Poll> pollRepository;
  private final MessageHandler messageHandler;

  @Inject
  public PollService(
    PollsPlugin plugin,
    CachedAsyncModelRepository<Poll> pollRepository,
    TimeFormatter timeFormatter,
    MessageHandler messageHandler
  ) {
    this.plugin = plugin;
    this.pollRepository = pollRepository;
    this.messageHandler = messageHandler;

    messageHandler
      .getConfig()
      .specify(Poll.class)
      .addProvider("poll", new PollPlaceholderProvider(this, timeFormatter));
  }

  public void loadAll() {
    pollRepository.findAllAsync().whenComplete((polls, error) -> {
      if (error != null) {
        plugin.getLogger().log(Level.SEVERE, "Unable to load polls", error);
        return;
      }

      for (Poll poll : polls) {
        if (poll.hasExpired()) {
          pollRepository.delete(poll);
          continue;
        }

        registerPoll(poll, false);
      }
    });
  }

  public void saveAll() {
    pollRepository.getAllAsync().whenComplete((polls, error) -> {
      if (error != null) {
        plugin.getLogger().log(Level.SEVERE, "Unable to save polls", error);
        return;
      }

      for (Poll poll : polls) {
        if (!poll.hasExpired()) {
          pollRepository.create(poll);
        }
      }
    });
  }

  public void registerPoll(Poll poll, boolean announce) {
    if (announce) {
      messageHandler.send(Bukkit.getOnlinePlayers(), "poll.opened", poll);
    }

    pollRepository.cache(poll);
    scheduleExpiration(poll);
  }

  public void registerPoll(Poll poll) {
    registerPoll(poll, true);
  }

  public boolean vote(Poll poll, int option, Player player) {
    if (poll.hasExpired()) {
      messageHandler.send(player, "poll.already-ended", poll);
      return false;
    }

    if (!poll.canVote(player)) {
      messageHandler.send(player, "poll.already-voted", poll);
      return false;
    }

    var options = poll.options();
    if (!options.containsKey(option)) {
      return false;
    }

    options.get(option).addVote();
    poll.voters().add(player.getUniqueId());
    return true;
  }

  public boolean expirePoll(Poll poll) {
    if (poll.closed()) {
      return false;
    }

    poll.setClosed(true);
    pollRepository.deleteCached(poll);
    pollRepository.deleteAsync(poll).whenComplete(($, error) -> {
      if (error != null) {
        plugin.getLogger().log(Level.SEVERE, "Unable to delete poll", error);
      }
    });

    messageHandler.sendIn(Bukkit.getOnlinePlayers(), MessageMode.INFO, "poll.ended", poll);
    return true;
  }

  public void scheduleExpiration(Poll poll) {
    long delay = (poll.expireDate() - System.currentTimeMillis()) / 50;
    Bukkit.getScheduler().runTaskLater(plugin, () -> expirePoll(poll), delay);
  }

  public List<String> getFormattedResults(Poll poll, Player viewer) {
    if (poll.closed()) {
      return null;
    }

    var options = poll.options();
    int totalVotes = options.values().stream()
      .mapToInt(Poll.Option::votes)
      .sum();

    return options.int2ObjectEntrySet().stream()
      .sorted((a, b) -> Integer.compare(b.getIntKey(), a.getIntKey()))
      .map(Map.Entry::getValue)
      .filter(option -> !Strings.isNullOrEmpty(option.value()))
      .map(option -> {
        int votes = option.votes();
        float percentage = totalVotes == 0
          ? 0f
          : (float) (votes * 100) / totalVotes;

        return messageHandler.format(
          viewer,
          "poll.results-format",
          ReplacePack.make(
            "%option%", option.value(),
            "%votes%", votes,
            "%percentage%", percentage
          ),
          new Object[]{poll}
        );
      })
      .toList();
  }

  public Poll getPoll(String id) {
    return pollRepository.get(id);
  }

  public List<Poll> getAllPolls() {
    return pollRepository.getAll();
  }

  @Override
  public @NotNull Iterator<Poll> iterator() {
    return pollRepository.getAll().iterator();
  }
}
