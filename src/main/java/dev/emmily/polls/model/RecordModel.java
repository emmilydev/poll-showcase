package dev.emmily.polls.model;

import dev.emmily.sigma.api.Model;

/**
 * An implementation of {@link Model} whose only purpose
 * is overriding the {@code getId()} method to return an
 * {@code id()} method, in order to keep records clean of
 * getters.
 */
public interface RecordModel extends Model {
  String id();

  @Override
  default String getId() {
    return id();
  }
}
