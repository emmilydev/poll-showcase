package dev.emmily.polls.model.codec;

import dev.emmily.sigma.api.Model;
import dev.emmily.sigma.api.codec.ModelCodec;
import org.bson.BsonDocument;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class MongoModelCodec<T extends Model>
  implements Codec<T> {
  private final ModelCodec modelCodec;
  private final Codec<BsonDocument> bsonDocumentCodec;
  private final Class<T> clazz;

  public MongoModelCodec(ModelCodec modelCodec,
                         Codec<BsonDocument> bsonDocumentCodec,
                         Class<T> clazz) {
    this.modelCodec = modelCodec;
    this.bsonDocumentCodec = bsonDocumentCodec;
    this.clazz = clazz;
  }

  @Override
  public T decode(BsonReader reader,
                  DecoderContext context) {
    BsonDocument bson = bsonDocumentCodec.decode(
      reader,
      context
    );

    return modelCodec.deserializeFromString(bson.toJson(), clazz);
  }

  @Override
  public void encode(BsonWriter writer,
                     T model,
                     EncoderContext context) {
    String json = modelCodec.serializeAsString(model);

    bsonDocumentCodec.encode(
      writer,
      BsonDocument.parse(json),
      context
    );
  }

  @Override
  public Class<T> getEncoderClass() {
    return clazz;
  }
}
