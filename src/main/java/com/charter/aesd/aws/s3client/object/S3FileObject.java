package com.charter.aesd.aws.s3client.object;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.charter.aesd.aws.s3client.FileS3Client;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.netflix.config.DynamicPropertyFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * File object wrapper for {@link S3ObjectSummary}
 *
 */
public class S3FileObject {

    private final S3ObjectSummary objectSummary;
    private final String name;
    private final String absolutePath;

    /**
     * Constructor for {@link S3FileObject}
     *
     * @param objectSummary
     */
    public S3FileObject(S3ObjectSummary objectSummary) {

        this.objectSummary = objectSummary;
        absolutePath = objectSummary.getKey();

        name = getNameFromPath(absolutePath);
    }

    /**
     * Constructor for {@link S3Object}
     * @param object The object to encapsulate
     */
    public S3FileObject(S3Object object) {
        objectSummary = new S3ObjectSummary();
        objectSummary.setKey(object.getKey());
        objectSummary.setBucketName(object.getBucketName());
        objectSummary.setETag(object.getObjectMetadata().getETag());
        objectSummary.setLastModified(object.getObjectMetadata().getLastModified());
        objectSummary.setSize(object.getObjectMetadata().getContentLength());

        absolutePath = object.getKey();
        name = getNameFromPath(absolutePath);
    }

    /**
     * Returns true if the {@link S3FileObject} is a directory
     *
     * @return {@link Boolean}
     */
    public boolean isDirectory() {
        return absolutePath.endsWith("/") || absolutePath.endsWith("_$folder$");
    }

    /**
     * Returns the full unmodified path from {@link S3ObjectSummary#getKey()}
     *
     * @return <code>string</code> absolute path
     */
    public String getAbsolutePath() {

        return absolutePath;
    }

    /**
     * Returns the name of the file without the full path
     *
     * @return filename
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the unmodified {@link S3ObjectSummary} from
     * {@link AmazonS3Client}
     *
     * @return {@link S3ObjectSummary}
     */
    public S3ObjectSummary getObjectSummary() {

        return objectSummary;
    }

    @Override
    public String toString() {

        return absolutePath;
    }

    /**
     * Gets the file name from a path provided from an {@link S3ObjectSummary}
     *
     * @param path
     * @return file name
     */
    public static String getNameFromPath(String path) {
    	
    	path = path.replaceAll("\\\\", "/");

        if (path.endsWith("/")) {
            path = path.replaceAll("/$", "");
        }
        else if (path.endsWith("_$folder$")) {
            path = path.substring(0, path.length() - 9);
        }

        final Pattern namePattern = Pattern.compile("^.*/(.*)$");
        final Matcher matcher = namePattern.matcher(path);

        if (matcher.matches()) {
            return matcher.group(1);
        }

        return path;
    }

    /**
     * Converts a {@link File} from the file system into an {@link S3FileObject}
     *
     * @param file
     * @return {@link S3FileObject}
     * @throws IOException
     */
    public static S3FileObject fromFile(File file) throws IOException {

        return new S3FileObject(createS3ObjectSummary(file));
    }

    public static S3FileObject fromFile(File file, String bucketName) throws IOException {

        final S3ObjectSummary objectSummary = createS3ObjectSummary(file);
        if (DynamicPropertyFactory.getInstance().getBooleanProperty(FileS3Client.BUCKET_NAME_AS_PATH, false).get()) {
            objectSummary.setBucketName(bucketName);
            objectSummary.setKey(getPath(file, bucketName));
        }
        return new S3FileObject(objectSummary);
    }

    public static String getPath(File file, String bucketName) {
        String filePath = file.getAbsolutePath();
        if (DynamicPropertyFactory.getInstance().getBooleanProperty(FileS3Client.BUCKET_NAME_AS_PATH, false).get()) {
            int idx = filePath.indexOf(bucketName);
            filePath = filePath.substring(idx + bucketName.length() + 1);
        }
        return filePath;
    }

    public static S3ObjectSummary createS3ObjectSummary(File file) throws IOException {

        final S3ObjectSummary objectSummary = new S3ObjectSummary();
        objectSummary.setKey(file.getAbsolutePath());
        objectSummary.setLastModified(new Date(file.lastModified()));
        objectSummary.setOwner(new Owner("system", "system"));
        objectSummary.setBucketName("");
        objectSummary.setSize(file.length());
        objectSummary.setStorageClass("");

        if (!file.isDirectory()) {
            final HashCode md5 = Files.hash(file, Hashing.md5());
            final byte[] bytes = md5.asBytes();
            objectSummary.setETag(bytes.toString());
        }
        return objectSummary;
    }

}
