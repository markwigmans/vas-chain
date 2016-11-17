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
import com.chain.http.BatchResponse;
import com.chain.http.Client;
import com.chain.signing.HsmSigner;
import com.ximedes.vas.chain.data.Assets;
import com.ximedes.vas.chain.data.Keys;
import com.ximedes.vas.chain.message.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

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
    void init() {
        reset();
    }

    public Account createAccount(final Account request) throws ChainException {
        final String alias = Integer.toString(counter.incrementAndGet());

        log.info("createAccount({})", alias);

        com.chain.api.Account account = findByAlias(alias);
        if (account == null) {
            account = new com.chain.api.Account.Builder().setAlias(alias).addRootXpub(key.xpub).setQuorum(1).create(client);
        }

        // create issue transactions : split into multiple transactions to create some 'change'.
        if (request.getOverdraft() != null && request.getOverdraft() > 0) {
            final List<Integer> splits = splitIssuedAmount(request.getOverdraft(), 4);
            final List<Transaction.Builder> txBuilders = new ArrayList<>();
            for (int split : splits) {
                txBuilders.add(new Transaction.Builder()
                        .addAction(new Transaction.Action.Issue()
                                .setAssetId(eur.id)
                                .setAmount(split))
                        .addAction(new Transaction.Action.ControlWithAccount().setAccountId(account.id)
                                .setAssetId(eur.id)
                                .setAmount(split)));
            }
            final BatchResponse<Transaction.Template> buildTxBatch = Transaction.buildBatch(client, txBuilders);
            Assert.isTrue(buildTxBatch.errors().isEmpty(), "Errors in build template");
            final BatchResponse<Transaction.Template> signTxBatch = HsmSigner.signBatch(buildTxBatch.successes());
            Assert.isTrue(signTxBatch.errors().isEmpty(), "Errors in sign template");
            final BatchResponse<Transaction.SubmitResponse> submitTxBatch = Transaction.submitBatch(client, signTxBatch.successes());
            Assert.isTrue(submitTxBatch.errors().isEmpty(), "Errors in submit template");
        }

        return Account.builder().accountId(alias).build();
    }

    public void reset() {
        counter.set(0);
    }

    com.chain.api.Account findByAlias(final String alias) {
        try {
            final com.chain.api.Account.Items items = new com.chain.api.Account.QueryBuilder().setFilter("alias=$1").addFilterParameter(alias).execute(client);
            return items.hasNext() ? items.next() : null;
        } catch (ChainException e) {
            log.warn("findByAlias() : Exception: {}", e.toString());
        }
        return null;
    }

    public Long getBalance(final String accountId) {
        try {
            Balance.Items balances = new Balance.QueryBuilder().setFilter("account_alias=$1").addFilterParameter(accountId).execute(client);
            return balances.list.stream().map(b -> b.amount).reduce(0L, Long::sum);
        } catch (ChainException e) {
            log.warn("getBalance() : Exception: {}", e.toString());
        }
        return 0L;
    }

    public Optional<Account> queryAccount(final String accountId) throws ChainException {
        com.chain.api.Account account = findByAlias(accountId);
        if (account != null) {
            return Optional.of(Account.builder().accountId(accountId).balance(getBalance(accountId).intValue()).build());
        } else {
            return Optional.empty();
        }
    }

    /**
     * split the given amount in the given number of splits.
     */
    List<Integer> splitIssuedAmount(final int issuedAmount, final int splits) {
        Assert.isTrue(splits > 0, "split must be > 0");
        final List<Integer> returnValue = new ArrayList<>();

        IntStream.range(1, splits).forEach(i -> returnValue.add(issuedAmount / splits));
        returnValue.add(issuedAmount - returnValue.stream().reduce(0, Integer::sum));

        return returnValue;
    }
}
