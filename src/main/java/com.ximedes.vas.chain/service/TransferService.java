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
import com.chain.api.MockHsm;
import com.chain.api.Transaction;
import com.chain.exception.ChainException;
import com.chain.http.Client;
import com.chain.signing.HsmSigner;
import com.ximedes.vas.chain.data.Assets;
import com.ximedes.vas.chain.data.Keys;
import com.ximedes.vas.chain.message.Transfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class TransferService {

    private final Client client;
    private final MockHsm.Key key;
    private final Asset eur;
    private final AtomicInteger counter = new AtomicInteger();

    /**
     * Auto wired constructor
     */
    @Autowired
    public TransferService(Client client, Keys keys, Assets assets) {
        this.client = client;
        this.key = keys.getKey();
        this.eur = assets.getEur();
    }

    public Transfer createTransfer(final Transfer request) throws ChainException {
        final int transferId = counter.incrementAndGet();
        Transaction.Template spending = new Transaction.Builder()
                .addAction(new Transaction.Action.SpendFromAccount()
                        .setAccountAlias(request.getFrom())
                        .setAssetAlias(eur.alias)
                        .setAmount(request.getAmount()))
                .addAction(new Transaction.Action.ControlWithAccount().setAccountAlias(request.getTo())
                        .setAssetAlias(eur.alias)
                        .setAmount(request.getAmount()))
                .build(client);

        Transaction.submit(client, HsmSigner.sign(spending));

        return Transfer.builder().transferId(Integer.toString(transferId)).build();
    }

    public Optional<Transfer> queryTransfer(final String transferId) throws ChainException {
        Transaction.Items transactions = new Transaction.QueryBuilder()
                .setFilter("reference_data.transferId=$1")
                .addFilterParameter(transferId)
                .execute(client);
        if (transactions.hasNext()) {
            final Transaction transaction = transactions.next();
            final String from = transaction.inputs.get(0).accountAlias;
            final String to = transaction.outputs.get(0).accountAlias;
            final int amount = transaction.inputs.stream().map(i -> i.amount).reduce(0L, Long::sum).intValue();
            return Optional.of(Transfer.builder().from(from).to(to).amount(amount).build());
        } else {
            return Optional.empty();
        }
    }
}
