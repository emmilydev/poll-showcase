package dev.emmily.polls.mongo;

import com.mongodb.client.MongoClient;
import dev.emmily.polls.config.Configuration;
import me.yushust.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MongoClientService {
  private final Configuration configuration;
  private final Injector injector;

  @Inject
  public MongoClientService(Configuration configuration, Injector injector) {
    this.configuration = configuration;
    this.injector = injector;
  }

  public void close() {
    if (!configuration.getString("storage.type").equalsIgnoreCase("mongo")) {
      return;
    }

    MongoClient client = injector.getInstance(MongoClient.class);

    client.close();
  }
}
