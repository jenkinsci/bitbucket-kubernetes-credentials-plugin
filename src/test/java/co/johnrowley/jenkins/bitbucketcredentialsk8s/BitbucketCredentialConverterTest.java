/*
 * The MIT License
 *
 * Copyright 2020 John Rowley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package co.johnrowley.jenkins.bitbucketcredentialsk8s;

import com.atlassian.bitbucket.jenkins.internal.config.BitbucketTokenCredentialsImpl;
import com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.CredentialsConvertionException;
import hudson.Extension;
import hudson.util.HistoricalSecrets;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.utils.Serialization;
import jenkins.security.ConfidentialStore;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import java.io.InputStream;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for {@link BitbucketCredentialConverter}.
 */
@Extension
public class BitbucketCredentialConverterTest {

    @BeforeClass
    public static void mockConfidentialStore() {
        Mockito.mockStatic(ConfidentialStore.class);
        Mockito.mockStatic(HistoricalSecrets.class);
    }

    @Before
    public void before() {
        ConfidentialStore csMock = Mockito.mock(ConfidentialStore.class);
        Mockito.when(ConfidentialStore.get()).thenReturn(csMock);
        Mockito.when(csMock.randomBytes(ArgumentMatchers.anyInt())).thenAnswer( it -> new byte[ (Integer)(it.getArguments()[0])] );
    }

    @Test
    public void canConvert() throws Exception {
        BitbucketCredentialConverter converter = new BitbucketCredentialConverter();
        assertThat("correct registration of valid type", converter.canConvert("bitbucketToken"), is(true));
        assertThat("incorrect type is rejected", converter.canConvert("something"), is(false));
    }

    @Test(expected = CredentialsConvertionException.class)
    public void failsToConvertASecretMissingText() throws Exception {
        BitbucketCredentialConverter converter = new BitbucketCredentialConverter();

        try (InputStream is = get("missing-text.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            assertThat("The Secret was loaded correctly from disk", notNullValue());

            BitbucketTokenCredentialsImpl credential = converter.convert(secret);
        }
    }

    @Test(expected = CredentialsConvertionException.class)
    public void failsToConvertWithNonBase64EncodedText() throws Exception {
        BitbucketCredentialConverter converter = new BitbucketCredentialConverter();

        try (InputStream is = get("text-isnt-base64.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            assertThat("The Secret was loaded correctly from disk", notNullValue());

            BitbucketTokenCredentialsImpl credential = converter.convert(secret);
        }
    }

    @Test
    public void canConvertAValidSecret() throws Exception {
        ConfidentialStore.get();
        BitbucketCredentialConverter converter = new BitbucketCredentialConverter();

        try (InputStream is = get("valid.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            assertThat("The Secret was loaded correctly from disk", notNullValue());

            BitbucketTokenCredentialsImpl credential = converter.convert(secret);
            assertThat(credential, notNullValue());

            assertThat("credential id is mapped correctly", credential.getId(), is("a-test-secret"));
            assertThat("credential description is mapped correctly", credential.getDescription(), is("secret bitbucket personal token credential from Kubernetes"));
            assertThat("credential text mapped to the secret", credential.getSecret().getPlainText(), is("someSuperDuperSecret"));
        }
    }

    private static InputStream get(String resource) {
        InputStream is = BitbucketCredentialConverterTest.class.getResourceAsStream(resource);
        if (is == null) {
            fail("failed to load resource " + resource);
        }
        return is;
    }
}