package dev.emmily.polls.test;

import dev.emmily.polls.poll.Poll;
import dev.emmily.polls.poll.PollService;
import dev.emmily.polls.util.time.TimeFormatter;
import dev.emmily.sigma.api.repository.CachedAsyncModelRepository;
import dev.emmily.sigma.platform.codec.gson.GsonModelCodec;
import dev.emmily.sigma.platform.jdk.MapModelRepository;
import dev.emmily.sigma.platform.json.JsonModelRepository;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.yushust.message.MessageHandler;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PollTest {

  private PollService pollService;
  private MessageHandler messageHandler;
  private CachedAsyncModelRepository<Poll> pollRepository;
  private List<String> capturedMessages;

  @BeforeEach
  public void setup() {
    capturedMessages = new ArrayList<>();

    messageHandler = MessageHandler.of((entity, path) -> path, config -> config
      .specify(Player.class)
      .setMessageSender((player, mode, message) -> capturedMessages.add(message)));

    pollRepository = new JsonModelRepository<>(
      new MapModelRepository<>(), new GsonModelCodec(),
      new File("src/test/resources"), Poll.class
    );

    pollService = new PollService(null, pollRepository, new TimeFormatter(messageHandler), messageHandler);
  }

  @Test
  public void testVoteSuccessful() {
    UUID playerId = UUID.randomUUID();
    Player player = mock(Player.class);
    when(player.getUniqueId()).thenReturn(playerId);

    Int2ObjectMap<Poll.Option> options = new Int2ObjectOpenHashMap<>();
    options.put(0, Poll.Option.of(0, "Option A"));

    Poll poll = Poll.of("poll1", "issuer", "question", System.currentTimeMillis() + 10000);
    poll.options().putAll(options);

    pollService.vote(poll, 0, player);

    assertEquals(1, poll.options().get(0).votes());
    assertTrue(poll.voters().contains(playerId));
  }

  @Test
  public void testVoteExpiredPoll() {
    UUID playerId = UUID.randomUUID();
    Player player = mock(Player.class);
    when(player.getUniqueId()).thenReturn(playerId);

    Poll poll = Poll.of("poll1", "issuer", "question", System.currentTimeMillis() - 10000);
    poll.setClosed(true);

    pollService.vote(poll, 0, player);

    assertEquals(1, capturedMessages.size());
    assertTrue(capturedMessages.getFirst().contains("poll.already-ended"));
    assertFalse(poll.voters().contains(playerId));
  }

  @Test
  public void testVoteAlreadyVoted() {
    UUID playerId = UUID.randomUUID();
    Player player = mock(Player.class);
    when(player.getUniqueId()).thenReturn(playerId);

    Poll poll = Poll.of("poll1", "issuer", "question", System.currentTimeMillis() + 10000);
    poll.voters().add(playerId);

    pollService.vote(poll, 0, player);

    assertEquals(1, capturedMessages.size());
    assertTrue(capturedMessages.getFirst().contains("poll.already-voted"));
  }

  @Test
  public void testExpirePollBehaviorWithoutBukkit() {
    Int2ObjectMap<Poll.Option> options = new Int2ObjectOpenHashMap<>();
    options.put(0, Poll.Option.of(0, "Option A").addVote().addVote().addVote().addVote().addVote());
    options.put(1, Poll.Option.of(1, "Option B").addVote().addVote().addVote());
    options.put(2, Poll.Option.of(2, "Option C").addVote().addVote());

    Poll poll = Poll.of("poll1", "issuer", "question", System.currentTimeMillis() + 10000);
    poll.options().putAll(options);
    poll.setClosed(true);

    boolean expired = expireLikePollService(poll);
    assertTrue(expired);
  }

  private boolean expireLikePollService(Poll poll) {
    if (!poll.closed() || poll.expireDate() < System.currentTimeMillis()) {
      return false;
    }

    Int2ObjectMap<Poll.Option> options = poll.options();
    int totalVotes = options.values().stream().mapToInt(Poll.Option::votes).sum();

    List<String> results = options
      .int2ObjectEntrySet()
      .stream()
      .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
      .map(entry -> {
        int votes = entry.getValue().votes();
        float percentage = totalVotes == 0
          ? 0f
          : (float) (votes * 100) / totalVotes;

        return entry.getValue().value() + ": " + votes + "(" + String.format("%.2f", percentage) + "%)"; // hardcoded lol
      })
      .toList();

    for (String line : results) {
      System.out.println(line);
    }

    return true;
  }
}
