package com.charter.aesd.aws.s3client;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.charter.aesd.aws.s3client.enums.S3AuthType;
import com.charter.aesd.aws.s3client.object.S3FileObject;
import com.google.common.base.Strings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * AWS client for retrieving and adding files to an S3 bucket<br />
 * Use {@link S3Client.Builder} to construct an instance of {@link S3Client}
 */
public class S3Client implements IS3Client {

    final private AmazonS3Client client;

    private S3Client(AmazonS3Client client) {

        this.client = client;
    }

    /**
     * Puts a file into S3
     *
     * @param bucketName name of the S3 bucket
     * @param path <code>String</code> path to put the file
     * @param contentLength <code>long</code> Byte size of the file
     * @param inputStream {@link InputStream} to upload
     */
    @Override
    public void put(String bucketName, String path, long contentLength, InputStream inputStream) {

        final ObjectMetadata objectMetaData = new ObjectMetadata();
        objectMetaData.setContentLength(contentLength);

        final PutObjectRequest putReq = new PutObjectRequest(bucketName, path, inputStream, objectMetaData);

        client.putObject(putReq);
    }

    /**
     * Puts a file into S3 with the ability to pass meta data in
     * {@link ObjectMetadata}<br />
     * {@link ObjectMetadata#setContentLength(long)} should be set to improve
     * upload performance
     *
     * @param bucketName name of the S3 bucket
     * @param path <code>String</code> path to put the file
     * @param objectMetadata {@link ObjectMetadata} contains meta data of the
     *        target file
     * @param inputStream {@link InputStream} to upload to S3
     */
    @Override
    public void put(String bucketName, String path, ObjectMetadata objectMetadata, InputStream inputStream) {

        final PutObjectRequest putReq = new PutObjectRequest(bucketName, path, inputStream, objectMetadata);

        client.putObject(putReq);
    }

    /**
     * Gets a file from S3 with a provided {@link S3FileObject}
     *
     * @param bucketName name of the S3 bucket
     * @param obj {@link S3FileObject} retrieved from
     *        {@link S3Client#listFiles(String, String, boolean)}
     * @return InputStream for object if it exists
     */
    @Override
    public InputStream get(String bucketName, S3FileObject obj) {

        return get(bucketName, obj.getAbsolutePath());
    }

    /**
     * Gets a file from S3
     *
     * @param bucketName name of the S3 bucket
     * @param path <code>String</code> path of the file
     * @return {@link InputStream} to download the file
     */
    @Override
    public InputStream get(String bucketName, String path) {

        GetObjectRequest getRequest = new GetObjectRequest(bucketName, path);
        S3Object object = client.getObject(getRequest);

        return object.getObjectContent();
    }

    /**
     * Gets a file from S3 as an {@link S3Object}
     *
     * @param bucketName name of the S3 bucket
     * @param path <code>String</code> path of the file
     * @return {@link S3Object}
     */
    @Override
    public S3Object getS3Object(String bucketName, String path) {

        GetObjectRequest getRequest = new GetObjectRequest(bucketName, path);
        return client.getObject(getRequest);
    }

    /**
     * Lists files contained within a bucket
     *
     * @param bucketName name of the S3 bucket
     * @param path Path prefix to search for files
     * @return {@link List} of {@link S3FileObject}'s
     */
    @Override
    public List<S3FileObject> listFiles(String bucketName, String path, boolean recursive) {

        final String correctedPath = path.replaceAll("/$", "").replaceAll("^/", "");

        ObjectListing listing = client.listObjects(bucketName, correctedPath);
        boolean truncated = false;

        final List<S3FileObject> files = new ArrayList<S3FileObject>();

        do {
            final List<S3ObjectSummary> objectSummaries = listing.getObjectSummaries();

            for (S3ObjectSummary objectSummary : objectSummaries) {
                final S3FileObject s3FileObject = new S3FileObject(objectSummary);
                if (!recursive) {
                    final String name = s3FileObject.getName();
                    final String absolutePath = s3FileObject.getAbsolutePath();
                    final String expectedPath =
                        Strings.isNullOrEmpty(correctedPath) ? name : correctedPath + "/" + name;
                    if (absolutePath.replaceAll("/$", "").equals(expectedPath) || name.equals(path)
                        || absolutePath.equals(path)) {
                        files.add(s3FileObject);
                    }
                } else {
                    files.add(new S3FileObject(objectSummary));
                }
            }

            truncated = listing.isTruncated();
            if (truncated) {
                listing = client.listNextBatchOfObjects(listing);
            }
        } while (truncated);

        return files;
    }

    /**
     * Lists files contained within a bucket
     *
     * @param bucketName name of the S3 bucket
     * @return {@link List} of {@link S3FileObject}'s
     */
    public List<S3FileObject> listFiles(String bucketName) {

        final ObjectListing listing = client.listObjects(bucketName);
        final List<S3ObjectSummary> objectSummaries = listing.getObjectSummaries();
        final List<S3FileObject> files = new ArrayList<S3FileObject>();

        for (S3ObjectSummary objectSummary : objectSummaries) {
            files.add(new S3FileObject(objectSummary));
        }

        return files;
    }

    /** {@inheritDoc} */
    @Override
    public S3Object rename(String bucketName, String sourcePath, String destPath) throws IOException {
        //Copy the object
        client.copyObject(new CopyObjectRequest(bucketName, sourcePath, bucketName, destPath));

        //Delete the original
        client.deleteObject(new DeleteObjectRequest(bucketName, sourcePath));

        return getS3Object(bucketName, destPath);
    }

    /** {@inheritDoc} */
    @Override
    public void delete(String bucketName, String path) {
        DeleteObjectRequest request = new DeleteObjectRequest(bucketName, path);
        client.deleteObject(request);
    }

    /**
     * Returns the {@code AmazonS3Client} for this instance.
     * 
     * @return
     *      The {@code AmazaonS3Client}.
     */
    protected AmazonS3Client getClient() {
        return client;
    }

    /**
     * Builder class for constructing an instance of {@link S3Client}
     *
     */
    public static class Builder {

        private S3AuthType authType;
        private String profileName;
        private String profileConfigFilePath;
        private ClientConfiguration config;

        /**
         * Constructor for {@link S3AuthType}
         *
         * @param authType {@link S3AuthType}
         */
        public Builder(S3AuthType authType) {
            this.authType = authType;
            this.config = new ClientConfiguration();
        }

        /**
         * Type of authentication used to talk to AWS
         *
         * @param authType
         * @return {@link Builder}
         */
        public Builder setAuthType(S3AuthType authType) {

            this.authType = authType;
            return this;
        }

        /**
         * Sets the name of the profile specified in the profile config, and used
         *  with an auth type of {@link S3AuthType#PROFILE}
         * <br /><br />
         * Default value is <code>"default"</code>
         *
         * @param profileName
         * @return {@link Builder}
         */
        public Builder setProfileName(String profileName) {

            this.profileName = profileName;
            return this;
        }

        /**
         * Sets the physical location of the profile config, and used
         *  with an auth type of {@link S3AuthType#PROFILE}
         * <br /><br />
         * 
         * Default behavior loads the profile config from <code>~/.aws/credentials</code>
         *
         * @param profileConfigFilePath
         * @return {@link Builder}
         */
        public Builder setProfileConfigFilePath(String profileConfigFilePath) {

            this.profileConfigFilePath = profileConfigFilePath;
            return this;
        }

        /**
         * Sets the {@link ClientConfiguration} used to configure the {@link AmazonS3Client}
         *
         * @param config {@link ClientConfiguration}
         * @return {@link Builder}
         */
        public Builder setConfig(ClientConfiguration config) {
            this.config = config;
            return this;
        }

        public S3Client build() {

            if (this.authType == S3AuthType.PROFILE && profileConfigFilePath == null && profileName == null) {
                return new S3Client(new AmazonS3Client(new ProfileCredentialsProvider(), config));
            }

            if (this.authType == S3AuthType.PROFILE && profileConfigFilePath == null && profileName != null) {
                return new S3Client(new AmazonS3Client(new ProfileCredentialsProvider(profileName), config));
            }

            if (this.authType == S3AuthType.PROFILE && profileConfigFilePath != null && profileName != null) {
                return new S3Client(new AmazonS3Client(
                        new ProfileCredentialsProvider(
                                new ProfilesConfigFile(profileConfigFilePath), profileName),
                                getConfiguration()));
            }

            if (this.authType == S3AuthType.INSTANCE_ROLE) {
                return new S3Client(new AmazonS3Client(new InstanceProfileCredentialsProvider(), config));
            }

            throw new IllegalStateException("Invalid S3Client configuration");
        }

        /**
         * Creates a {@code ClientConfiguration} object using the System properties
         * for {@code http.proxyHost} and {@code http.proxyPort}. To leverage this
         * both host and port must be set using the -D args (i.e.,
         * {@code -Dhttp.proxyHost=my.proxy.host.com -Dhttp.proxyPort=3128} and if
         * auth is required {@code -Dhttp.proxyUser=username -Dhttp.proxyPassword=password1234}. 
         *
         * @return
         *      A {@ClientConfiguration}. Never {@code null}.
         */
        public ClientConfiguration getConfiguration() {

            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");
            String proxyUserName = System.getProperty("http.proxyUser");
            String proxyUserPasswd = System.getProperty("http.proxyPassword");

            if(proxyHost != null) {
                config.setProxyHost(proxyHost);
            }

            if(proxyPort != null) {
                config.setProxyPort(Integer.parseInt(proxyPort));
            }

            if(proxyUserName != null) {
                config.setProxyUsername(proxyUserName);
            }

            if(proxyUserPasswd != null) {
                config.setProxyPassword(proxyUserPasswd);
            }

            return config;
        }
    }

    @Override
    public void mkdir(String bucketName, String path) {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        final String correctedPath = path.endsWith("/") ? path : path + "/";

        final ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0L);
        objectMetadata.setContentType("application/x-directory");

        final PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName, correctedPath, inputStream, objectMetadata);

        client.putObject(putObjectRequest);
    }
}
