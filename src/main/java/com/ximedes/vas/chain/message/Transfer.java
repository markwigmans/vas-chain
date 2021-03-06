/*
 * Copyright (C) 2016 Mark Wigmans (mark.wigmans@ximedes.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ximedes.vas.chain.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

/**
 * Created by mawi on 19/07/2016.
 */
@JsonDeserialize(builder = Transfer.TransferBuilder.class)
@Builder
@Value
public class Transfer {

    public enum Status {
        PENDING,
        CONFIRMED,
        INSUFFICIENT_FUNDS,
        ACCOUNT_NOT_FOUND,
        TRANSFER_NOT_FOUND
    }

    String transferId;
    String from;
    String to;
    Integer amount;
    Status status;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class TransferBuilder {
    }
}
