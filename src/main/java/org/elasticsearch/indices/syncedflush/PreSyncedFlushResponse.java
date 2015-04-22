/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.indices.syncedflush;

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastOperationResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * A response to pre synced flush action.
 */
public class PreSyncedFlushResponse extends BroadcastOperationResponse {

    Map<String, byte[]> commitIds = new HashMap<>();

    PreSyncedFlushResponse() {
    }

    public PreSyncedFlushResponse(int totalShards, int successfulShards, int failedShards, List<ShardOperationFailedException> shardFailures, AtomicReferenceArray shardsResponses) {
        super(totalShards, successfulShards, failedShards, shardFailures);
        for (int i = 0; i < shardsResponses.length(); i++) {
            PreSyncedShardFlushResponse preSyncedShardFlushResponse = (PreSyncedShardFlushResponse) shardsResponses.get(i);
            commitIds.put(preSyncedShardFlushResponse.shardRouting().currentNodeId(), preSyncedShardFlushResponse.id());
        }
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        int numCommitIds = in.readVInt();
        for (int i = 0; i < numCommitIds; i++) {
            commitIds.put(in.readString(), in.readByteArray());
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeVInt(commitIds.size());
        for (Map.Entry<String, byte[]> entry : commitIds.entrySet()) {
            out.writeString(entry.getKey());
            out.writeByteArray(entry.getValue());
        }
    }

    public Map<String, byte[]> commitIds() {
        return commitIds;
    }
}
