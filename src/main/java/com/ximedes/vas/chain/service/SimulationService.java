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

import com.chain.api.Account;
import com.chain.api.Asset;
import com.chain.api.Transaction;
import com.chain.exception.ChainException;
import com.chain.http.BatchResponse;
import com.chain.http.Client;
import com.chain.signing.HsmSigner;
import com.ximedes.vas.chain.data.Assets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
@Slf4j
public class SimulationService {

    private final Client client;
    private final AccountService accountService;
    private final Asset eur;

    @Autowired
    public SimulationService(Client client, AccountService accountService, Assets assets) {
        this.client = client;
        this.accountService = accountService;
        this.eur = assets.getEur();
    }

    public void reset() throws Exception {
        log.info("Reset simulation");
        accountService.reset();
    }
}
