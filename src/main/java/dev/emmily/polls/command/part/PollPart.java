package dev.emmily.polls.command.part;

import dev.emmily.polls.poll.Poll;
import dev.emmily.polls.poll.PollService;
import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.annotated.part.PartFactory;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.ArgumentPart;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PollPart
  implements PartFactory {
  private final PollService pollService;

  public PollPart(PollService pollService) {
    this.pollService = pollService;
  }

  @Override
  public CommandPart createPart(String name,
                                List<? extends Annotation> modifiers) {
    return new ArgumentPart() {
      @Override
      public List<?> parseValue(CommandContext context,
                                ArgumentStack stack,
                                CommandPart caller) throws ArgumentParseException {
        String next = stack.next();
        Poll poll = pollService.get(next);

        if (poll == null) {
          return Collections.emptyList();
        }

        return Collections.singletonList(poll);
      }

      @Override
      public List<String> getSuggestions(CommandContext context,
                                         ArgumentStack stack) {
        String next = stack.next();

        List<String> suggestions = new ArrayList<>();

        for (Poll poll : pollService) {
          String id = poll.getId();

          if (id.startsWith(next)) {
            suggestions.add(id);
          }
        }

        return suggestions;
      }

      @Override
      public String getName() {
        return name;
      }
    };
  }
}
