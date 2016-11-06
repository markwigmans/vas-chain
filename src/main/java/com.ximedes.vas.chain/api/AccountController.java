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
package com.ximedes.vas.chain.api;

import com.chain.exception.ChainException;
import com.ximedes.vas.chain.message.Account;
import com.ximedes.vas.chain.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@Slf4j
class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(final AccountService accountService) {
        this.accountService = accountService;
    }

    @RequestMapping(value = "/account", method = RequestMethod.POST)
    public ResponseEntity createAccount(@RequestBody Account request) throws ChainException {
        log.info("createAccount({})", request);

        final Account account = accountService.createAccount(request);
        final URI location = UriComponentsBuilder.newInstance().pathSegment("/account", account.getAccountId()).build().toUri();
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity(responseHeaders, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/account/{accountId}", method = RequestMethod.GET)
    public ResponseEntity queryAccount(@PathVariable String accountId) throws ChainException {
        log.debug("queryAccount({})", accountId);

        final Account account = accountService.queryAccount(accountId);
        if (account != null) {
            return new ResponseEntity(account, HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}
