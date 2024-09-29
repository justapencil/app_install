package com.miko.appinstall.codec;

import com.miko.appinstall.model.entity.InstallationQueueEntity;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

public class InstallationQueueEntityCodec implements MessageCodec<InstallationQueueEntity, InstallationQueueEntity> {

  @Override
  public void encodeToWire(Buffer buffer, InstallationQueueEntity entity) {
    JsonObject jsonToEncode = JsonObject.mapFrom(entity);
    String jsonString = jsonToEncode.encode();

    int length = jsonString.getBytes().length;

    buffer.appendInt(length);
    buffer.appendString(jsonString);
  }

  @Override
  public InstallationQueueEntity decodeFromWire(int position, Buffer buffer) {
    int length = buffer.getInt(position);
    position += 4;

    String jsonString = buffer.getString(position, position + length);

    return new JsonObject(jsonString).mapTo(InstallationQueueEntity.class);
  }

  @Override
  public InstallationQueueEntity transform(InstallationQueueEntity entity) {

    return entity;
  }

  @Override
  public String name() {
    return this.getClass().getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
