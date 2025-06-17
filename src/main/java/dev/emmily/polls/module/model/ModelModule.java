package dev.emmily.polls.module.model;

import com.google.gson.GsonBuilder;
import com.mongodb.*;
import com.mongodb.client.*;
import dev.emmily.polls.config.Configuration;
import dev.emmily.polls.model.codec.MongoModelCodecProvider;
import dev.emmily.polls.poll.Poll;
import dev.emmily.sigma.api.codec.ModelCodec;
import dev.emmily.sigma.api.repository.CachedAsyncModelRepository;
import dev.emmily.sigma.api.repository.ModelRepository;
import dev.emmily.sigma.platform.codec.gson.GsonModelCodec;
import dev.emmily.sigma.platform.jdk.MapModelRepository;
import dev.emmily.sigma.platform.mongo.MongoModelRepository;
import me.yushust.inject.AbstractModule;
import me.yushust.inject.Provides;
import me.yushust.inject.key.TypeReference;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import javax.inject.Singleton;
import java.util.List;

public class ModelModule extends AbstractModule {
  private final Configuration config;

  public ModelModule(Configuration config) {
    this.config = config;
  }

  @Override
  public void configure() {
    bind(TypeReference.of(ModelRepository.class, Poll.class))
      .toInstance(new MapModelRepository<>());
  }

  @Provides
  @Singleton
  public ModelCodec provideModelCodec() {
    return new GsonModelCodec(
      new GsonBuilder().setPrettyPrinting().create()
    );
  }

  @Provides
  @Singleton
  public CodecRegistry provideCodecRegistry(ModelCodec codec) {
    return CodecRegistries.fromRegistries(
      MongoClientSettings.getDefaultCodecRegistry(),
      CodecRegistries.fromProviders(new MongoModelCodecProvider(codec))
    );
  }

  @Provides
  @Singleton
  public MongoClient provideMongoClient(CodecRegistry codecRegistry) {
    MongoClientSettings.Builder builder = MongoClientSettings.builder()
      .codecRegistry(codecRegistry);

    if (config.contains("storage.credentials.uri")) {
      builder.applyConnectionString(
        new ConnectionString(config.getString("storage.credentials.uri"))
      );
    } else {
      ServerAddress address = new ServerAddress(
        config.getString("storage.credentials.host"),
        config.getInt("storage.credentials.port")
      );

      builder.applyToClusterSettings(settings ->
        settings.hosts(List.of(address))
      ).credential(
        MongoCredential.createCredential(
          config.getString("storage.credentials.username"),
          config.getString("storage.credentials.database"),
          config.getString("storage.credentials.password").toCharArray()
        )
      );
    }

    return MongoClients.create(builder.build());
  }

  @Provides
  @Singleton
  public MongoDatabase provideMongoDatabase(MongoClient client) {
    return client.getDatabase(config.getString("storage.credentials.database"));
  }

  @Provides
  @Singleton
  public MongoCollection<Poll> providePollCollection(
    MongoDatabase db, CodecRegistry codecRegistry
  ) {
    return db.getCollection("polls", Poll.class)
      .withCodecRegistry(codecRegistry);
  }

  @Provides
  @Singleton
  private CachedAsyncModelRepository<Poll> providePollRepository(
    ModelRepository<Poll> cache,
    MongoCollection<Poll> collection
  ) {
    return new MongoModelRepository<>(cache, collection);
  }
}
