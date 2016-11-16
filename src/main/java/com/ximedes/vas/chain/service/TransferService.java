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

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class TransferService {

    private static final String KEY = "transferId";

    private final Client client;
    private final Asset eur;
    private final AtomicInteger counter = new AtomicInteger();

    /**
     * Auto wired constructor
     */
    @Autowired
    public TransferService(Client client, Assets assets) {
        this.client = client;
        this.eur = assets.getEur();
    }

    @PostConstruct
    void init() {
        reset();
    }

    public Transfer createTransfer(final Transfer request) {
        final String alias = Integer.toString(counter.incrementAndGet());
        Transfer.Status status = Transfer.Status.CONFIRMED;
        try {
            Transaction.Template spending = new Transaction.Builder()
                    .addAction(new Transaction.Action.SpendFromAccount()
                            .setAccountAlias(request.getFrom())
                            .setAssetAlias(eur.alias)
                            .setAmount(request.getAmount())
                            .addReferenceDataField(KEY, alias))
                    .addAction(new Transaction.Action.ControlWithAccount().setAccountAlias(request.getTo())
                            .setAssetAlias(eur.alias)
                            .setAmount(request.getAmount())
                            .addReferenceDataField(KEY, alias))
                    .build(client);

            Transaction.submit(client, HsmSigner.sign(spending));
        } catch (ChainException e) {
            log.warn("Exception", e);
            status = Transfer.Status.INSUFFICIENT_FUNDS;
        }

        return Transfer.builder().transferId(alias).status(status).build();
    }

    public void reset() {
        counter.set(0);
    }

    public Optional<Transfer> queryTransfer(final String transferId) throws ChainException {
        Transaction.Items transactions = new Transaction.QueryBuilder()
                .setFilter(String.format("outputs(reference_data.%s=$1)", KEY)).addFilterParameter(transferId)
                .execute(client);
        if (transactions.hasNext()) {
            final Transaction transaction = transactions.next();

            final String from = transaction.inputs.stream().filter(i -> keyFound(i.referenceData)).findFirst().get().accountAlias;
            final Transaction.Output output = transaction.outputs.stream().filter(i -> keyFound(i.referenceData)).findFirst().get();
            final String to = output.accountAlias;
            final int amount = new Long(output.amount).intValue();

            return Optional.of(Transfer.builder().transferId(transferId).from(from).to(to).amount(amount).build());
        } else {
            return Optional.empty();
        }
    }

    boolean keyFound(final Map<String, Object> data) {
        return data.keySet().stream().filter(k -> KEY.equals(k)).count() != 0;
    }
}
