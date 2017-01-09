package com.charter.aesd.aws.s3client;

import com.charter.aesd.aws.s3client.object.S3FileObject;
import com.google.common.collect.Lists;
import com.netflix.config.ConfigurationManager;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Runs all the test in FileS3ClientTest but with the BUCKET_NAME_AS_PATH property set to true.
 */
public class FileS3ClientWithBucketNameAsPathTest extends FileS3ClientTest {

    private static List<String> expectedPaths =
            Lists.newArrayList("src/test/resources/file1", "src/test/resources/file2", "src/test/resources/file3");

    @BeforeClass
    public static void beforeClass() {
        ConfigurationManager.getConfigInstance().setProperty(FileS3Client.BUCKET_NAME_AS_PATH, true);
    }

    @Test
    @Override
    public void testListFiles() throws IOException {

        final String filePath = "src/test/resources";

        final IS3Client s3Client = new FileS3Client();
        final List<S3FileObject> files = s3Client.listFiles(BUCKET_NAME, filePath, false);

        assertEquals(3, files.size());
        for (S3FileObject s3FileObject : files) {
            String path = s3FileObject.getAbsolutePath();
            assertTrue("invalid path: " + path, expectedPaths.contains(path));
            assertEquals(BUCKET_NAME, s3FileObject.getObjectSummary().getBucketName());
        }
    }

    @Test
    @Override
    public void testListFilesPath() throws IOException {
        final String filePath = "src/test/resources/";

        final FileS3Client s3Client = new FileS3Client();
        final List<String> paths = s3Client.listFilesPath(BUCKET_NAME, filePath, "");

        assertEquals(3, paths.size());
        for (String path : paths) {
            assertTrue("invalid path: " + path, expectedPaths.contains(path));
        }
    }
}