package com.charter.aesd.aws.s3client;

import com.amazonaws.services.s3.model.S3Object;
import com.charter.aesd.aws.s3client.object.S3FileObject;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileS3ClientTest {

    protected static final String BUCKET_NAME = ".";

    @Test
    public void testListFiles() throws IOException {

        final String filePath = "src/test/resources";
        final List<String> expectedPaths =
                Lists.newArrayList(new File("src/test/resources/file1").getAbsolutePath(), new File(
                        "src/test/resources/file2").getAbsolutePath(), new File("src/test/resources/file3").getAbsolutePath());
        final List<String> expectedNames = Lists.newArrayList("file1", "file2", "file3");

        final IS3Client s3Client = new FileS3Client();
        final List<S3FileObject> files = s3Client.listFiles(BUCKET_NAME, filePath, false);

        assertEquals(files.size(), 3);
        for (S3FileObject s3FileObject : files) {
            String path = s3FileObject.getAbsolutePath();
            assertTrue("invalid path: " + path, expectedPaths.contains(path));
            assertEquals("", s3FileObject.getObjectSummary().getBucketName());
        }
    }

    @Test
    public void testPut() throws IOException {

        final String filePath = "src/test/resources/file4";
        final String fileContents = "Test 4";

        final IS3Client s3Client = new FileS3Client();

        s3Client.put(BUCKET_NAME, filePath, 0L, new ByteArrayInputStream(fileContents.getBytes()));
        try (FileInputStream stream = new FileInputStream(new File(filePath))) {
            final String newfileContents = IOUtils.toString(stream);
            assertEquals(fileContents, newfileContents);
        }

        Files.delete(Paths.get(filePath));
    }

    @Test
    public void testGet() throws IOException {

        final String filePath = "src/test/resources/file1";
        final String fileContents = "Test 1";

        final IS3Client s3Client = new FileS3Client();
        final InputStream input = s3Client.get(BUCKET_NAME, filePath);
        final String newFileContents = IOUtils.toString(input);

        assertEquals(fileContents, newFileContents);
    }

    @Test
    public void testGetObject() throws IOException {

        final String filePath = "src/test/resources/file1";
        final String fileContents = "Test 1";

        final IS3Client s3Client = new FileS3Client();
        final S3Object input = s3Client.getS3Object(BUCKET_NAME, filePath);
        final String newFileContents = IOUtils.toString(input.getObjectContent());

        assertEquals(fileContents, newFileContents);
    }


    @Test
    public void testListFilesPath() throws IOException {
        final String filePath = "src/test/resources/";
        final List<String> expectedPaths =
                Lists.newArrayList(new File("src/test/resources/file1").getAbsolutePath(), new File(
                        "src/test/resources/file2").getAbsolutePath(), new File("src/test/resources/file3").getAbsolutePath());
        final List<String> expectedNames = Lists.newArrayList("file1", "file2", "file3");

        final FileS3Client s3Client = new FileS3Client();
        final List<String> paths = s3Client.listFilesPath(BUCKET_NAME, filePath, "");

        assertEquals(paths.size(), 3);
        for (String path : paths) {
            assertTrue("invalid path: " + path, expectedPaths.contains(path));
        }
    }

}