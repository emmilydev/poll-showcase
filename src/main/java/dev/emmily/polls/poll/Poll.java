package dev.emmily.polls.poll;

import dev.emmily.sigma.api.Model;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Represents an active poll of any topic, in which players can vote
 * only once, until {@code creationDate} is reached.
 */
public final class Poll implements Model {
  public static Poll of(String id,
                        String issuer,
                        String question,
                        long expireDate) {
    Poll poll = new Poll(id, issuer, question, expireDate);

    for (int i = 0; i < 6; i++) {
      poll.options.put(i, Poll.Option.empty(i));
    }

    return poll;
  }

  private final String id;
  private final String issuer;
  private final String question;
  private final Int2ObjectMap<Option> options;
  private final Set<UUID> voters;
  private final long creationDate;
  private final long expireDate;
  private boolean closed;

  /**
   * @param id           The unique identifier of this poll
   * @param issuer       The administrator who issued this poll
   * @param options      The option to votes
   * @param voters       Mapping of player {@code UUID id} to poll option
   * @param creationDate The creation timestamp of this poll.
   * @param expireDate   The exact instant this poll ends.
   */
  private Poll(String id,
               String issuer,
               String question,
               Int2ObjectMap<Option> options,
               Set<UUID> voters,
               long creationDate,
               long expireDate) {
    this.id = id;
    this.issuer = issuer;
    this.question = question;
    this.options = options;
    this.voters = voters;
    this.creationDate = creationDate;
    this.expireDate = expireDate;
  }

  private Poll(String id,
               String issuer,
               String question,
               long expireDate) {
    this(
      id, issuer, question, new Int2ObjectOpenHashMap<>(6),
      new HashSet<>(), System.currentTimeMillis(), expireDate
    );
  }

  @Override
  public String getId() {
    return id;
  }

  public int totalVoters() {
    return voters.size();
  }

  public boolean canVote(Player player) {
    return !voters.contains(player.getUniqueId());
  }

  public boolean hasExpired() {
    return closed || System.currentTimeMillis() >= expireDate;
  }

  public String issuer() {
    return issuer;
  }

  public String question() {
    return question;
  }

  public Int2ObjectMap<Option> options() {
    return options;
  }

  public Set<UUID> voters() {
    return voters;
  }

  public long creationDate() {
    return creationDate;
  }

  public long expireDate() {
    return expireDate;
  }

  public boolean closed() {
    return closed;
  }

  public void setClosed(boolean closed) {
    this.closed = closed;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (Poll) obj;
    return Objects.equals(this.id, that.id) &&
      Objects.equals(this.issuer, that.issuer) &&
      Objects.equals(this.question, that.question) &&
      Objects.equals(this.options, that.options) &&
      Objects.equals(this.voters, that.voters) &&
      this.creationDate == that.creationDate &&
      this.expireDate == that.expireDate;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, issuer, question, options, voters, creationDate, expireDate);
  }

  @Override
  public String toString() {
    return "Poll[" +
      "id=" + id + ", " +
      "issuer=" + issuer + ", " +
      "question=" + question + ", " +
      "options=" + options + ", " +
      "voters=" + voters + ", " +
      "creationDate=" + creationDate + ", " +
      "expireDate=" + expireDate + ']';
  }

  public static class Option {
    public static Option of(int index, String value) {
      return new Option(index, value);
    }

    public static Option empty(int index) {
      return of(index, null);
    }

    private final int index;
    private int votes;
    private String value;

    public Option(int index,
                  String value) {
      this.index = index;
      this.value = value;
    }

    public int index() {
      return index;
    }

    public int votes() {
      return votes;
    }

    public Option addVote() {
      ++votes;

      return this;
    }

    public String value() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      Option option = (Option) o;
      return index == option.index;
    }

    @Override
    public int hashCode() {
      return index;
    }
  }
}
