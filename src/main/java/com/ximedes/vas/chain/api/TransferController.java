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
import com.ximedes.vas.chain.message.Transfer;
import com.ximedes.vas.chain.service.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@Slf4j
class TransferController {

    private final TransferService transferService;

    @Autowired
    public TransferController(final TransferService transferService) {
        this.transferService = transferService;
    }

    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public ResponseEntity createTransfer(@RequestBody Transfer request) throws ChainException {
        log.info("createTransfer({})", request);

        final Transfer transfer = transferService.createTransfer(request);
        final URI location = UriComponentsBuilder.newInstance().pathSegment("/transfer", transfer.getTransferId()).build().toUri();
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity(responseHeaders, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/transfer/{transferId}", method = RequestMethod.GET)
    public ResponseEntity queryTransfer(@PathVariable String transferId) throws ChainException {
        log.debug("queryTransfer({})", transferId);

        final Optional<Transfer> transfer = transferService.queryTransfer(transferId);
        if (transfer.isPresent())
            return new ResponseEntity(transfer, HttpStatus.OK);
        else
            return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
}

