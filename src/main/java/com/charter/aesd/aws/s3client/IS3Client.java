package com.charter.aesd.aws.s3client;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.charter.aesd.aws.s3client.object.S3FileObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface IS3Client {

    /**
     * Puts a file into S3
     *
     * @param bucketName name of the S3 bucket
     * @param path {@code String} path to put the file
     * @param contentLength <code>long</code> Byte size of the file
     * @param inputStream {@link InputStream} to upload
     */
    void put(String bucketName, String path, long contentLength, InputStream inputStream) throws IOException;

    /**
     * Puts a file into S3 with the provided metadata
     *
     * @param bucketName is the name o S3 bucket
     * @param path {@code String} path to put the file
     * @param objectMetadata is the metadata the s3 will keep for the provided
     *        file
     * @param inputStream {@link InputStream} to upload
     * @throws IOException
     */
    void put(String bucketName, String path, ObjectMetadata objectMetadata, InputStream inputStream) throws IOException;

    /**
     * Gets a file from S3 with a provided {@link S3FileObject}
     *
     * @param bucketName name of the S3 bucket
     * @param obj {@link S3FileObject} retrieved from
     *        {@link S3Client#listFiles(String, String, boolean)}
     * @return InputStream for object if it exists
     */
    InputStream get(String bucketName, S3FileObject obj) throws IOException;

    /**
     * Gets a file from S3
     *
     * @param bucketName name of the S3 bucket
     * @param path <code>String</code> path of the file
     * @return {@link InputStream} to download the file
     */
    InputStream get(String bucketName, String path) throws IOException;

    /**
     * Gets a file from S3 as an {@link S3Object}
     *
     * @param bucketName name of the S3 bucket
     * @param path <code>String</code> path of the file
     * @return {@link S3Object}
     * @throws IOException
     */
    S3Object getS3Object(String bucketName, String path) throws IOException;

    /**
     * Lists files path contained within a bucket, searching by prefix and delimiter 
     *
     * @param bucketName name of the S3 bucket
     * @param prefix Path prefix to search for files
     * @param delimiter Path delimiter to search for files
     * @return {@link List} of {@link String}'s
     * @throws IOException
     */
    List<String> listFilesPath(String bucketName, String prefix, String delimiter) throws IOException;
    
    /**
     * Lists files contained within a bucket
     *
     * @param bucketName name of the S3 bucket
     * @param path Path prefix to search for files
     * @return {@link List} of {@link S3FileObject}'s
     */
    List<S3FileObject> listFiles(String bucketName, String path, boolean recursive) throws IOException;

    /**
     * Renames a file.
     * NOTE: in S3 there is no rename, so it copies the file then deletes the original.
     * @param bucketName name of the S3 bucket
     * @param sourcePath name of the current file
     * @param destPath name of the destination file
     * @return The new object after renaming
     */
    S3Object rename(String bucketName, String sourcePath, String destPath) throws IOException;

    /**
     * Removes a file from the file system.
     * NOTE: There is no confirmation at this point, so be sure this is what you want before you get here.
     *
     * @param bucketName name of the S3 bucket
     * @param path <code>String</code> path of object
     */
    void delete(String bucketName, String path) throws IOException;

    /**
     * Creates a directory for the specified path
     *
     * @param bucketName name of the S3 bucket
     * @param path <code>String</code> path of directory
     */
    void mkdir(String bucketName, String path);

    /**
     * Returns boolean value of whether or not an S3 object exists
     *
     * @param bucketName name of the S3 bucket
     * @param path <code>String</code> path of object
     * @return {@link Boolean}
     */
    boolean exists(String bucketName, String path);
}
