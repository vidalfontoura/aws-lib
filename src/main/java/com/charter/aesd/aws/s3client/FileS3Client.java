package com.charter.aesd.aws.s3client;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.charter.aesd.aws.s3client.object.S3FileObject;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FileS3Client implements IS3Client {

    public static String BUCKET_NAME_AS_PATH = "aws.lib.bucketNameAsPath";

    /**
     * BucketName is not analogise to a file system, so you can optionally use the BucketName as the base directly
     * for file operations by setting the 'aws.lib.bucketNameAsPath' property to true.
     *
     * It is left as false for backwards compatibility.
     */
    private final boolean useBucketName;

    public FileS3Client() {
        useBucketName = DynamicPropertyFactory.getInstance().getBooleanProperty(BUCKET_NAME_AS_PATH, false).get();
    }

    /**
     * Returns null if Bucket Name is not used, controlled via the <code>BUCKET_NAME_AS_PATH</code> property. Then
     * FileS3Client will not use a base directory to support backwards compatibility.
     */
    private String directory(String bucketName) {
        return useBucketName ? bucketName : null;
    }

    @Override
    public void put(String bucketName, String path, long contentLength, InputStream inputStream) throws IOException {

        final File file = new File(directory(bucketName), path);

    	if(!file.exists()){
			Files.createParentDirs(file);
		}
    	
    	Files.write(IOUtils.toByteArray(inputStream), file);
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

        return new FileInputStream(new File(directory(bucketName), path));
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

        final List<S3FileObject> s3FileObjects = new ArrayList<>();

        final File folder = new File(directory(bucketName), path);
        final File[] files = folder.listFiles();

        for (File file : files) {
            s3FileObjects.add(S3FileObject.fromFile(file, bucketName));
        }

        return s3FileObjects;
    }
    
    @Override
	public List<String> listFilesPath(String bucketName, String prefix, String delimiter) throws IOException {

    	final File folder = new File(directory(bucketName), prefix);
    	if(folder.exists()) {
            final List<String> filesPath = Lists.newArrayList();
            final Collection<File> paths = FileUtils.listFiles(folder, null, true);

            paths.forEach(file -> {
                String path = file.getAbsolutePath().replaceAll("\\\\", "/");
                if (path.endsWith(delimiter.replaceAll("\\\\", "/"))) {
                    filesPath.add(S3FileObject.getPath(file, bucketName));
                }
            });
            return filesPath;
        }

        return Collections.emptyList();
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
        (new File(directory(bucketName), path)).delete();
    }

    @Override
    public void mkdir(String bucketName, String path) {
        (new File(directory(bucketName), path)).mkdirs();
    }

    @Override
    public boolean exists(String bucketName, String path) {
        return (new File(directory(bucketName), path)).exists();
    }

}
