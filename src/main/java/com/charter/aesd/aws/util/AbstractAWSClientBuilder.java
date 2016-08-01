package com.charter.aesd.aws.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.charter.aesd.aws.enums.AWSAuthType;

/**
 * <p/>
 * Provide a base class for AWS clients to use to setup there common client
 * configurations, such as Proxy, Credentials, etc.
 * <p/>
 * User: matthewsmith Date: 7/22/14 Time: 1:13 PM
 * 
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
abstract public class AbstractAWSClientBuilder<T> {

    /**
     *
     */
    private ClientConfiguration config = null;
    private String profileName;
    private String profileConfigFilePath;
    private AWSAuthType authType;

    /**
     * Abstract ... provide the base level configuration container
     */
    protected AbstractAWSClientBuilder(final AWSAuthType authType) {

        this.authType = authType;
        this.config = new ClientConfiguration();
    }

    /**
     * @return {@code String} The profile to use for credential resolution
     */
    public String getProfileName() {

        return this.profileName;
    }

    /**
     * Sets the name of the profile specified in the profile config <br />
     * <br />
     * Default value is <code>"default"</code>
     * 
     * @param profileName
     * @return {@link AbstractAWSClientBuilder}
     */
    public AbstractAWSClientBuilder<T> setProfileName(String profileName) {

        this.profileName = profileName;
        return this;
    }

    /**
     * @return {@code String} The path of the AWS credentials file
     */
    public String getProfileConfigFilePath() {

        return this.profileConfigFilePath;
    }

    /**
     * Sets the physical location of the profile config <br />
     * <br />
     * 
     * Default behavior loads the profile config from
     * <code>~/.aws/credentials</code>
     * 
     * @param profileConfigFilePath
     * @return {@link AbstractAWSClientBuilder}
     */
    public AbstractAWSClientBuilder<T> setProfileConfigFilePath(String profileConfigFilePath) {

        this.profileConfigFilePath = profileConfigFilePath;
        return this;
    }

    /**
     * Sets the {@link ClientConfiguration} used to configure
     * 
     * @param config {@link ClientConfiguration}
     * 
     * @return {@link AbstractAWSClientBuilder}
     */
    public AbstractAWSClientBuilder<T> setConfig(ClientConfiguration config) {

        this.config = config;

        return this;
    }

    /**
     * Creates a {@code ClientConfiguration} object using the System properties
     * for {@code http.proxyHost} and {@code http.proxyPort}. To leverage this
     * both host and port must be set using the -D args (i.e.,
     * {@code -Dhttp.proxyHost=my.proxy.host.com -Dhttp.proxyPort=3128} and if
     * auth is required
     * {@code -Dhttp.proxyUser=username -Dhttp.proxyPassword=password1234}.
     * 
     * @return A {@ClientConfiguration}. Never
     *         {@code null}.
     */
    public ClientConfiguration getConfig() {

        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        String proxyUserName = System.getProperty("http.proxyUser");
        String proxyUserPasswd = System.getProperty("http.proxyPassword");

        if (proxyHost != null) {
            this.config.setProxyHost(proxyHost);
        }

        if (proxyPort != null) {
            this.config.setProxyPort(Integer.parseInt(proxyPort));
        }

        if (proxyUserName != null) {
            this.config.setProxyUsername(proxyUserName);
        }

        if (proxyUserPasswd != null) {
            this.config.setProxyPassword(proxyUserPasswd);
        }

        return this.config;
    }

    /**
     * 
     * @return
     */
    public T build() {

        if (this.authType == AWSAuthType.DEFAULT_AWS) {
            return allocateClient(new DefaultAWSCredentialsProviderChain(), getConfig());
        }

        if (this.authType == AWSAuthType.INSTANCE_ROLE) {
            return allocateClient(new InstanceProfileCredentialsProvider(), getConfig());
        }

        if (this.profileConfigFilePath == null) {
            if (this.profileName == null) {
                return allocateClient(new ProfileCredentialsProvider(), getConfig());
            }
            return allocateClient(new ProfileCredentialsProvider(this.profileName), getConfig());

        } else if (this.profileName != null) {
            // Have both a profile name and a config location
            return allocateClient(new ProfileCredentialsProvider(new ProfilesConfigFile(getProfileConfigFilePath()),
                getProfileName()), getConfig());
        }

        return allocateClient(null, getConfig());
    }

    /**
     *
     */
    abstract protected T allocateClient(final AWSCredentialsProvider provider, final ClientConfiguration config);
} // AbstractAWSClientBuilder
