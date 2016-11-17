package com.ximedes.vas.chain.service;

import com.chain.http.Client;
import com.ximedes.vas.chain.data.Assets;
import com.ximedes.vas.chain.data.Keys;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class AccountServiceTest {

    private AccountService service;

    @Before
    public void setup() {
        service = new AccountService(mock(Client.class), mock(Keys.class), mock(Assets.class));
    }

    @Test
    public void splitIssuedAmount() throws Exception {
        Assert.assertThat(service.splitIssuedAmount(10, 3), containsInAnyOrder(3, 3, 4));
        Assert.assertThat(service.splitIssuedAmount(8, 4), containsInAnyOrder(2, 2, 2, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalSplitIssuedAmount() throws Exception {
        service.splitIssuedAmount(10, 0);
    }

}