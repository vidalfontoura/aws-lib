package com.charter.aesd.aws.s3client;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.S3Object;
import com.charter.aesd.aws.ec2.client.api.impl.EC2ClientImpl;
import com.charter.aesd.aws.enums.AWSAuthType;
import com.charter.aesd.aws.s3client.enums.S3AuthType;
import com.charter.aesd.aws.s3client.object.S3FileObject;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * XXX NOTE: Tests do not delete files, so may not be accurate over time
 * 
 */
public class S3EncryptionClientTest {

    private static final String KEY = "teststring";
    private static final String BUCKET_NAME = "s3client-test";
    private static final String KMS_CMK_ID = "insert kms cmk id here";
    
    private S3Client client;

    @Before
    public void setUp() {

       client =
            Boolean.getBoolean("use.iam.role") ? new S3Client.Builder(S3AuthType.ENCRYPT_INSTANCE_ROLE).build()
                : new S3Client.Builder(S3AuthType.ENCRYPT_PROFILE)
                              .setKmsCmkId(KMS_CMK_ID)
                              .setKmsRegion(Regions.US_WEST_2)
                              .setRegion(Region.getRegion(Regions.US_WEST_2))
                              .build();
    }

    @Test
    public void testPutAndGet() throws IOException {

        String expected = "hello world";

        byte[] bytes = expected.getBytes();
        client.put(BUCKET_NAME, KEY, bytes.length, new ByteArrayInputStream(bytes));

        final InputStream inputStream = client.get(BUCKET_NAME, KEY);
        String actual = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));

        System.out.println("actual: " + actual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPutAndGetObject() throws IOException {

        String expected = "hello world test";

        byte[] bytes = expected.getBytes();
        final String path = KEY + "2";
        client.put(BUCKET_NAME, path, bytes.length, new ByteArrayInputStream(bytes));

        final S3Object s3Object = client.getS3Object(BUCKET_NAME, path);
        String actual = CharStreams.toString(new InputStreamReader(s3Object.getObjectContent(), Charsets.UTF_8));

        System.out.println("actual: " + actual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testListFiles() throws IOException {

        String expected = "hello world";

        byte[] bytes = expected.getBytes();
        client.put(BUCKET_NAME, "testfiles/" + KEY + "1", bytes.length, new ByteArrayInputStream(bytes));
        client.put(BUCKET_NAME, "testfiles/" + KEY + "2", bytes.length, new ByteArrayInputStream(bytes));
        client.put(BUCKET_NAME, "testfiles/" + KEY + "3", bytes.length, new ByteArrayInputStream(bytes));

        final List<S3FileObject> files = client.listFiles(BUCKET_NAME, "testfiles/", false);

        int index = 1;
        S3FileObject lastObj = null;
        for (S3FileObject file : files) {
            System.out.println(file.getAbsolutePath());
            Assert.assertEquals("testfiles/" + KEY + String.valueOf(index), file.getAbsolutePath());
            index++;
            lastObj = file;
        }

        Assert.assertNotNull(lastObj);

        // Test get(S3FileObject)
        Assert.assertEquals("testfiles/teststring3", lastObj.getAbsolutePath());
        final InputStream is = client.get(BUCKET_NAME, lastObj);
        String actual = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testListFilesNonRecursive() {

        String expected = "hello world";
        byte[] bytes = expected.getBytes();

        // Test non-recursive ls
        client.put(BUCKET_NAME, "testfiles2/subfolder/" + KEY + "1", bytes.length, new ByteArrayInputStream(bytes));
        client.put(BUCKET_NAME, "testfiles2/" + KEY + "2", bytes.length, new ByteArrayInputStream(bytes));

        final List<S3FileObject> files = client.listFiles(BUCKET_NAME, "testfiles2/", false);
        Assert.assertEquals(1, files.size());
        Assert.assertEquals("testfiles2/teststring2", files.get(0).getAbsolutePath());

        final List<S3FileObject> files2 = client.listFiles(BUCKET_NAME, "testfiles2/", true);
        Assert.assertEquals(2, files2.size());
        Assert.assertEquals("testfiles2/subfolder/teststring1", files2.get(0).getAbsolutePath());
        Assert.assertEquals("testfiles2/teststring2", files2.get(1).getAbsolutePath());
    }

    /** 
     * TODO: This test fails inconsistently and needs to be updated/fixed.
     */
    @Test
    @Ignore
    public void testClientConfigurationNoProxy() {

        // if you have a proxy configured in your network settings on a mac this will fail.
       S3Client.Builder builder = new S3Client.Builder(S3AuthType.ENCRYPT_PROFILE);
       ClientConfiguration config = builder.getConfiguration();
       Assert.assertNull(config.getProxyHost());
       Assert.assertEquals(config.getProxyPort(), -1);
       Assert.assertNull(config.getProxyUsername());
       Assert.assertNull(config.getProxyPassword());
    }

    @Test
    public void testClientConfigurationWithHostAndPort() {

        System.setProperty("http.proxyHost", "keaulcgwp01.corp.chartercom.com");
        System.setProperty("http.proxyPort", "8080");
        // if you have a proxy configured in your network settings on a mac this will fail.
       S3Client.Builder builder = new S3Client.Builder(S3AuthType.ENCRYPT_PROFILE);
       ClientConfiguration config = builder.getConfiguration();
       Assert.assertNotNull(config.getProxyHost());
       Assert.assertEquals("keaulcgwp01.corp.chartercom.com", config.getProxyHost());
       Assert.assertNotNull(config.getProxyPort());
       Assert.assertEquals(8080, config.getProxyPort());
       // docs say I should be able to set null but this seems to throw an NPE, see section 2.1:
       // http://docs.oracle.com/javase/7/docs/technotes/guides/net/proxies.html
       System.setProperty("http.proxyHost", "");
       System.setProperty("http.proxyPort", "-1");
    }

    @Test
    public void testClientConfigurationWithFullParams() {

        System.setProperty("http.proxyHost", "keaulcgwp01.corp.chartercom.com");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("http.proxyUser", "proxyUser");
        System.setProperty("http.proxyPassword", "proxyPassword");
        // if you have a proxy configured in your network settings on a mac this will fail.
       S3Client.Builder builder = new S3Client.Builder(S3AuthType.ENCRYPT_PROFILE);
       ClientConfiguration config = builder.getConfiguration();
       Assert.assertNotNull(config.getProxyHost());
       Assert.assertEquals("keaulcgwp01.corp.chartercom.com", config.getProxyHost());
       Assert.assertNotNull(config.getProxyPort());
       Assert.assertEquals(8080, config.getProxyPort());
       Assert.assertNotNull(config.getProxyUsername());
       Assert.assertEquals(config.getProxyUsername(), "proxyUser");
       Assert.assertNotNull(config.getProxyPassword());
       Assert.assertEquals(config.getProxyPassword(), "proxyPassword");
       // docs say I should be able to set null but this seems to throw an NPE, see section 2.1:
       // http://docs.oracle.com/javase/7/docs/technotes/guides/net/proxies.html
       System.setProperty("http.proxyHost", "");
       System.setProperty("http.proxyPort", "-1");
       System.setProperty("http.proxyUser", "");
       System.setProperty("http.proxyPassword", "");
       
    }
    
    @Test
    public void testExists() {

        String expected = "this exists";
        byte[] bytes = expected.getBytes();
        client.put(BUCKET_NAME, "exists", bytes.length, new ByteArrayInputStream(bytes));

        boolean doesNotExist = client.exists(BUCKET_NAME, "does_not_exist");
        boolean exists = client.exists(BUCKET_NAME, "exists");

        Assert.assertFalse(doesNotExist);
        Assert.assertTrue(exists);
    }
}