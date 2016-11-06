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
package com.ximedes.vas.chain.service;

import com.chain.api.Asset;
import com.chain.api.Balance;
import com.chain.api.MockHsm;
import com.chain.api.Transaction;
import com.chain.exception.ChainException;
import com.chain.http.Client;
import com.chain.signing.HsmSigner;
import com.ximedes.vas.chain.data.Assets;
import com.ximedes.vas.chain.data.Keys;
import com.ximedes.vas.chain.message.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class AccountService {

    private final Client client;
    private final MockHsm.Key key;
    private final Asset eur;
    private final AtomicInteger counter = new AtomicInteger();

    /**
     * Auto wired constructor
     */
    @Autowired
    public AccountService(Client client, Keys keys, Assets assets) {
        this.client = client;
        this.key = keys.getKey();
        this.eur = assets.getEur();
    }

    @PostConstruct
    void init() throws ChainException {
        counter.set(countAccounts());
    }

    public Account createAccount(final Account request) throws ChainException {
        final int id = counter.incrementAndGet();
        final com.chain.api.Account account = new com.chain.api.Account.Builder().setAlias(Integer.toString(id))
                .addRootXpub(key.xpub).setQuorum(1).create(client);

        // create issue transaction
        if (request.getOverdraft() != null && request.getOverdraft() > 0) {
            Transaction.Template issuance = new Transaction.Builder()
                    .addAction(new Transaction.Action.Issue()
                            .setAssetAlias(eur.alias)
                            .setAmount(request.getOverdraft()))
                    .addAction(new Transaction.Action.ControlWithAccount().setAccountAlias(account.alias)
                            .setAssetAlias(eur.alias)
                            .setAmount(request.getOverdraft()))
                    .build(client);

            Transaction.submit(client, HsmSigner.sign(issuance));
        }

        return Account.builder().accountId(Integer.toString(id)).build();
    }

    public Account queryAccount(final String accountId) throws ChainException {
        Balance.Items balances = new Balance.QueryBuilder().setFilter("account_alias=$1").addFilterParameter(accountId).execute(client);
        final int balance = balances.list.stream().map(b -> b.amount).reduce(0L, Long::sum).intValue();
        return Account.builder().accountId(accountId).balance(balance).build();
    }

    int countAccounts() throws ChainException {
        return new com.chain.api.Account.QueryBuilder().execute(client).list.size();
    }
}
