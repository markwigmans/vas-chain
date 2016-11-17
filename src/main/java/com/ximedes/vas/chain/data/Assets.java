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
package com.ximedes.vas.chain.data;

import com.chain.api.Asset;
import com.chain.api.MockHsm;
import com.chain.exception.ChainException;
import com.chain.http.Client;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Assets to be used by the transfers / balances.
 */
@Component
public class Assets {

    private final Client client;
    private final MockHsm.Key key;

    @Getter
    private Asset eur;

    /**
     * Auto wired constructor
     */
    @Autowired
    public Assets(Client client, Keys keys) {
        this.client = client;
        this.key = keys.getKey();
    }

    @PostConstruct
    public void init() throws ChainException {
        final String assetName = "eurocents";
        eur = findByAlias(assetName);
        if (eur == null) {
            eur = new Asset.Builder().setAlias(assetName).addRootXpub(key.xpub).setQuorum(1).create(client);
        }
    }

    Asset findByAlias(final String alias) throws ChainException {
        Asset.Items items = new Asset.QueryBuilder().setFilter("alias=$1").addFilterParameter(alias).execute(client);
        return items.hasNext() ? items.next() : null;
    }
}
