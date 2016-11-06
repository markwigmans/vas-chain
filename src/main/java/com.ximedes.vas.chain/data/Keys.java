package com.ximedes.vas.chain.data;

import com.chain.api.Asset;
import com.chain.api.MockHsm;
import com.chain.exception.ChainException;
import com.chain.http.Client;
import com.chain.signing.HsmSigner;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 *
 */
@Component
public class Keys {

    @Autowired
    private Client client;

    @Getter
    private MockHsm.Key key;


    @PostConstruct
    public void init() throws ChainException {
        final String alias = "vas";
        key = findByAlias(alias);
        if (key == null) {
            key = MockHsm.Key.create(client, alias);
            HsmSigner.addKey(key, MockHsm.getSignerClient(client));
        }
    }

    MockHsm.Key findByAlias(final String alias) throws ChainException {
        final MockHsm.Key.Items items = new MockHsm.Key.QueryBuilder().addAlias(alias).execute(client);
        return items.hasNext() ? items.next() : null;
    }

}
