package com.charter.aesd.aws.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;

/**
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
    private ClientConfiguration _config = null;
    private String _profileName;
    private String _profileConfigFilePath;

    /**
     *
     */
    protected AbstractAWSClientBuilder() {

        _config = new ClientConfiguration();
    }

    /**
     *
     * @return {@code String} The profile to use for credential resolution
     */
    public String getProfileName() {

        return _profileName;
    }

    /**
     * Sets the name of the profile specified in the profile config
     * <br /><br />
     * Default value is <code>"default"</code>
     *
     * @param profileName
     * @return {@link AbstractAWSClientBuilder}
     */
    public AbstractAWSClientBuilder<T> setProfileName(String profileName) {

        _profileName = profileName;
        return this;
    }

    /**
     *
     * @return {@code String} The path of the AWS credentials file
     */
    public String getProfileConfigFilePath() {

        return _profileConfigFilePath;
    }

    /**
     * Sets the physical location of the profile config
     * <br /><br />
     *
     * Default behavior loads the profile config from <code>~/.aws/credentials</code>
     *
     * @param profileConfigFilePath
     * @return {@link AbstractAWSClientBuilder}
     */
    public AbstractAWSClientBuilder<T> setProfileConfigFilePath(String profileConfigFilePath) {

        _profileConfigFilePath = profileConfigFilePath;
        return this;
    }

    /**
     * Sets the {@link ClientConfiguration} used to configure
     *
     * @param config
     *                 {@link ClientConfiguration}
     *
     * @return {@link AbstractAWSClientBuilder}
     */
    public AbstractAWSClientBuilder<T> setConfig(ClientConfiguration config) {

        _config = config;

        return this;
    }

    /**
     * Creates a {@code ClientConfiguration} object using the System properties for {@code http.proxyHost} and
     * {@code http.proxyPort}. To leverage this both host and port must be set using the -D args (i.e., {@code
     * -Dhttp.proxyHost=my.proxy.host.com -Dhttp.proxyPort=3128} and if auth is required {@code
     * -Dhttp.proxyUser=username -Dhttp.proxyPassword=password1234}.
     *
     * @return A {@ClientConfiguration}. Never {@code null}.
     */
    public ClientConfiguration getConfig() {

        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        String proxyUserName = System.getProperty("http.proxyUser");
        String proxyUserPasswd = System.getProperty("http.proxyPassword");

        if (proxyHost != null) {
            _config.setProxyHost(proxyHost);
        }

        if (proxyPort != null) {
            _config.setProxyPort(Integer.parseInt(proxyPort));
        }

        if (proxyUserName != null) {
            _config.setProxyUsername(proxyUserName);
        }

        if (proxyUserPasswd != null) {
            _config.setProxyPassword(proxyUserPasswd);
        }

        return _config;
    }

    /**
     *
     * @return
     */
    public T build() {

        if (_profileConfigFilePath == null) {
            if (_profileName == null) {
                return allocateClient(new ProfileCredentialsProvider(),
                                      getConfig());
            } else {
                return allocateClient(new ProfileCredentialsProvider(_profileName),
                                      getConfig());
            }
        } else if (_profileName != null) {
            // Have both a profile name and a config location
            return allocateClient(new ProfileCredentialsProvider(new ProfilesConfigFile(getProfileConfigFilePath()),
                                                                                               getProfileName()),
                                             getConfig());
        }

        return allocateClient(null,
                        getConfig());
    }

    /**
     *
     */
    abstract protected T allocateClient(final ProfileCredentialsProvider provider,
                                        final ClientConfiguration config);
} // AbstractAWSClientBuilder
