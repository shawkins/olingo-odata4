/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.server.core.serializer;

import java.io.InputStream;

import org.apache.olingo.server.api.ODataContent;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.serializer.SerializerStreamResult;

public class SerializerStreamResultImpl implements SerializerStreamResult {
  private InputStream content;
  private ODataContent oDataContent;

  @Override
  public ODataContent getODataContent() {
    return oDataContent;
  }

  //  @Override
//  public ReadableByteChannel getChannel() {
//    return Channels.newChannel(getContent());
//  }
//
//  @Override
//  public void write(WritableByteChannel channel) {
//    ResultHelper.copy(Channels.newChannel(content), channel);
//  }
//
//  @Override
//  public boolean isWriteSupported() {
//    return false;
//  }

  public static SerializerResultBuilder with() {
    return new SerializerResultBuilder();
  }

  public static class SerializerResultBuilder {
    private SerializerStreamResultImpl result = new SerializerStreamResultImpl();

    public SerializerResultBuilder content(final ODataContent content) {
      result.oDataContent = content;
      return this;
    }

    public SerializerStreamResult build() {
      return result;
    }
  }
}