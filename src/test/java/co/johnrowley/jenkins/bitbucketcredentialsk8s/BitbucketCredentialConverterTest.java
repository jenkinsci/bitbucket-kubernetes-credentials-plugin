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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.atlassian.bitbucket.jenkins.internal.config.BitbucketTokenCredentialsImpl;
import com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.CredentialsConvertionException;
import hudson.util.HistoricalSecrets;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.io.InputStream;
import jenkins.security.ConfidentialStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Tests for {@link BitbucketCredentialConverter}.
 */
class BitbucketCredentialConverterTest {

    private static MockedStatic<ConfidentialStore> csMockStatic;
    private static MockedStatic<HistoricalSecrets> hsMockStatic;

    @BeforeAll
    static void mockConfidentialStore() {
        csMockStatic = mockStatic(ConfidentialStore.class);
        hsMockStatic = mockStatic(HistoricalSecrets.class);
    }

    @BeforeEach
    void before() {
        ConfidentialStore csMock = mock(ConfidentialStore.class);
        when(ConfidentialStore.get()).thenReturn(csMock);
        when(csMock.randomBytes(anyInt())).thenAnswer(it -> new byte[(Integer) (it.getArguments()[0])]);
    }

    @AfterAll
    static void resetMockStatic() {
        csMockStatic.close();
        hsMockStatic.close();
    }

    @Test
    void canConvert() {
        BitbucketCredentialConverter converter = new BitbucketCredentialConverter();
        assertThat("correct registration of valid type", converter.canConvert("bitbucketToken"), is(true));
        assertThat("incorrect type is rejected", converter.canConvert("something"), is(false));
    }

    @Test
    void failsToConvertASecretMissingText() throws Exception {
        BitbucketCredentialConverter converter = new BitbucketCredentialConverter();

        try (InputStream is = get("missing-text.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            assertThat("The Secret was loaded correctly from disk", secret, notNullValue());

            assertThrows(CredentialsConvertionException.class, () -> converter.convert(secret));
        }
    }

    @Test
    void failsToConvertWithNonBase64EncodedText() throws Exception {
        BitbucketCredentialConverter converter = new BitbucketCredentialConverter();

        try (InputStream is = get("text-isnt-base64.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            assertThat("The Secret was loaded correctly from disk", secret, notNullValue());

            assertThrows(CredentialsConvertionException.class, () -> converter.convert(secret));
        }
    }

    @Test
    void canConvertAValidSecret() throws Exception {
        ConfidentialStore.get();
        BitbucketCredentialConverter converter = new BitbucketCredentialConverter();

        try (InputStream is = get("valid.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            assertThat("The Secret was loaded correctly from disk", secret, notNullValue());

            BitbucketTokenCredentialsImpl credential = converter.convert(secret);
            assertThat(credential, notNullValue());

            assertThat("credential id is mapped correctly", credential.getId(), is("a-test-secret"));
            assertThat(
                    "credential description is mapped correctly",
                    credential.getDescription(),
                    is("secret bitbucket personal token credential from Kubernetes"));
            assertThat(
                    "credential text mapped to the secret",
                    credential.getSecret().getPlainText(),
                    is("someSuperDuperSecret"));
        }
    }

    private static InputStream get(String resource) {
        InputStream is = BitbucketCredentialConverterTest.class.getResourceAsStream(resource);
        assertNotNull(is, "failed to load resource " + resource);
        return is;
    }
}
