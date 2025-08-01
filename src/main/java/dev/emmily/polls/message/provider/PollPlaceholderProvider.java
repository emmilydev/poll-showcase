package dev.emmily.polls.message.provider;

import dev.emmily.polls.poll.Poll;
import dev.emmily.polls.poll.PollService;
import dev.emmily.polls.util.time.TimeFormatter;
import me.yushust.message.format.PlaceholderProvider;
import me.yushust.message.track.ContextRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

public class PollPlaceholderProvider
  implements PlaceholderProvider<Poll> {
  private final PollService pollService;
  private final TimeFormatter timeFormatter;

  public PollPlaceholderProvider(PollService pollService,
                                 TimeFormatter timeFormatter) {
    this.pollService = pollService;
    this.timeFormatter = timeFormatter;
  }

  @Override
  public @Nullable Object replace(ContextRepository context,
                                  Poll poll, String placeholder) {
    if (!(context.getEntity() instanceof Player player)) {
      return null;
    }

    return switch (placeholder.toLowerCase()) {
      case "id" -> poll.getId();
      case "issuer" -> Bukkit.getPlayer(UUID.fromString(poll.issuer())).getName();
      case "question" -> poll.question();
      case "options" -> {
        StringJoiner joiner = new StringJoiner("\n");

        for (Poll.Option option : poll.options().values()) {
          joiner.add(option.value());
        }

        yield joiner.toString();
      }
      case "votes" -> poll.totalVoters();
      case "creation_date" -> timeFormatter.format(
        context.getEntity(),
        System.currentTimeMillis() - poll.creationDate());
      case "expire_date" -> timeFormatter.format(
        context.getEntity(),
        poll.expireDate() - System.currentTimeMillis());
      case "expired" -> context.get(player, "word." + (poll.hasExpired() ? "yes" : "no"));
      case "open" -> context.get(player, "word." + (poll.hasExpired() ? "no" : "yes"));
      case "player_voted" -> context.get(player, "word." + (poll.canVote(player) ? "no" : "yes"));
      case "results" -> {
        List<String> formattedResults = pollService.getFormattedResults(poll, player);

        if (formattedResults == null) {
          yield null;
        }

        yield "\n" + String.join("\n", formattedResults);
      }
      default -> null;
    };
  }
}