package dev.emmily.polls.model.codec;

import com.mongodb.MongoClientSettings;
import dev.emmily.sigma.api.Model;
import dev.emmily.sigma.api.codec.ModelCodec;
import org.bson.BsonDocument;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class MongoModelCodecProvider
  implements CodecProvider {
  private final ModelCodec modelCodec;
  private final Codec<BsonDocument> bsonDocumentCodec;

  public MongoModelCodecProvider(ModelCodec modelCodec,
                                 Codec<BsonDocument> bsonDocumentCodec) {
    this.modelCodec = modelCodec;
    this.bsonDocumentCodec = bsonDocumentCodec;
  }

  public MongoModelCodecProvider(ModelCodec modelCodec) {
    this(modelCodec, new BsonDocumentCodec(
      MongoClientSettings.getDefaultCodecRegistry()
    ));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Codec<T> get(Class<T> clazz, CodecRegistry codecRegistry) {
    if (!Model.class.isAssignableFrom(clazz)) {
      return null;
    }

    return (Codec<T>) new MongoModelCodec<>(
      modelCodec,
      bsonDocumentCodec,
      (Class<? extends Model>) clazz
    );
  }

}
