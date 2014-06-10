package com.charter.aesd.aws.s3client;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.charter.aesd.aws.s3client.object.S3FileObject;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileS3Client implements IS3Client {

    @Override
    public void put(String bucketName, String path, long contentLength, InputStream inputStream) throws IOException {

        ByteStreams.copy(inputStream, new FileOutputStream(new File(path)));
    }

    @Override
    public void put(String bucketName, String path, ObjectMetadata objectMetadata, InputStream inputStream)
        throws IOException {

        put(bucketName, path, 0, inputStream);
    }

    @Override
    public InputStream get(String bucketName, S3FileObject obj) throws IOException {

        return new FileInputStream(new File(obj.getAbsolutePath()));
    }

    @Override
    public InputStream get(String bucketName, String path) throws IOException {

        return new FileInputStream(new File(path));
    }

    @Override
    public S3Object getS3Object(String bucketName, String path) throws IOException {

        S3Object s3Object = new S3Object();
        s3Object.setBucketName(bucketName);
        s3Object.setKey(path);
        s3Object.setObjectContent(get(bucketName, path));
        s3Object.setRedirectLocation(path);
        return s3Object;
    }

    @Override
    public List<S3FileObject> listFiles(String bucketName, String path, boolean recursive) throws IOException {

        final List<S3FileObject> s3FileObjects = new ArrayList<S3FileObject>();

        final File folder = new File(path);
        final File[] files = folder.listFiles();

        for (File file : files) {
            s3FileObjects.add(S3FileObject.fromFile(file));
        }

        return s3FileObjects;
    }

    @Override
    public S3Object rename(String bucketName, String sourcePath, String destPath) throws IOException {
        File source = new File(sourcePath);
        File dest = new File(destPath);
        source.renameTo(dest);

        return getS3Object(bucketName, destPath);
    }

    @Override
    public void delete(String bucketName, String path) throws IOException {
        (new File(path)).delete();
    }
}
