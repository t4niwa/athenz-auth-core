package com.yahoo.athenz.auth.impl;

import com.yahoo.athenz.auth.Principal;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.*;

public class AuthHeaderAuthorityTest {

    @Test
    public void testGetID() {
    	AuthHeaderAuthority aha = new AuthHeaderAuthority();
        assertEquals(aha.getID(), "Auth-Header");
    }

    @Test
    public void testGetDomain() {
        AuthHeaderAuthority aha = new AuthHeaderAuthority();
        assertEquals(aha.getDomain(), "user");
    }

    @Test
    public void testGetHeader() {
        AuthHeaderAuthority aha = new AuthHeaderAuthority();
        assertEquals(aha.getHeader(), "X-Auth-User");
    }

    @Test
    public void testGetAuthenticateChallenge() {
        AuthHeaderAuthority aha = new AuthHeaderAuthority();
        assertEquals(aha.getAuthenticateChallenge(), null);
    }

    @Test
    public void testAllowAuthorization() {
        AuthHeaderAuthority aha = new AuthHeaderAuthority();
        assertFalse(aha.allowAuthorization());
    }

    @Test
    public void testAuthenticate() {
        AuthHeaderAuthority aha = new AuthHeaderAuthority();
        StringBuilder errMsg = new StringBuilder();

        try {
        	System.setProperty(AuthHeaderAuthority.ATHENZ_PROP_AUTH_HEADER_TRUSTED_CIDR, "127.0.0.1,192.168.0.1");
            aha.initialize();
        }catch (Exception ex) {
            fail();
        }

        // happy path
        String testUser = "athenz-admin";
        Principal principal = aha.authenticate(testUser, "127.0.0.1", "GET", errMsg);
        assertNotNull(principal);
        principal = aha.authenticate(testUser, "192.168.0.1", "GET", errMsg);
        assertNotNull(principal);

        // untrusted remote ip
        testUser = "athenz-admin";
        principal = aha.authenticate(testUser, "10.72.118.45", "GET", errMsg);
        assertNull(principal);
        principal = aha.authenticate(testUser, "127.0.0.", "GET", errMsg);
        assertNull(principal);

        // Failed to create principal
        try (MockedStatic<SimplePrincipal> theMock = Mockito.mockStatic(SimplePrincipal.class)) {
            theMock.when((MockedStatic.Verification) SimplePrincipal.create(anyString(), anyString(), anyString(), anyLong(), any())).thenReturn(null);
            testUser = "athenz-admin";
            principal = aha.authenticate(testUser, "127.0.0.1", "GET", errMsg);
            assertNull(principal);
        }

    }

    @Test
    public void testGetSimplePrincipal() {
        AuthHeaderAuthority aha = new AuthHeaderAuthority();
        long issueTime = System.currentTimeMillis();
        SimplePrincipal sp = aha.getSimplePrincipal("abc", "xyz", issueTime);
        assertNotNull(sp);
        assertEquals(sp.getAuthority().getClass(), AuthHeaderAuthority.class);
    }
}