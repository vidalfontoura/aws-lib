package com.charter.aesd.aws.s3client;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.charter.aesd.aws.s3client.object.S3FileObject;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileS3Client implements IS3Client {

    @Override
    public void put(String bucketName, String path, long contentLength, InputStream inputStream) throws IOException {
    	
    	final File file = new File(path);

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
	public List<String> listFilesPath(String bucketName, String prefix, String delimiter) throws IOException {

    	
    	final File folder = new File(prefix);
    	if(folder.exists()){
    		final List<String> filesPath = Lists.newArrayList();
	    	final Collection<File> paths = FileUtils.listFiles(folder, null, true);
	
	        for (File file : paths) {
	        	String path = file.getAbsolutePath().replaceAll("\\\\", "/");
	        	if(path.endsWith(delimiter.replaceAll("\\\\", "/"))){
	        		filesPath.add(file.getAbsolutePath());
	        	}
	        }
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
        (new File(path)).delete();
    }

    @Override
    public void mkdir(String bucketName, String path) {
        (new File(path)).mkdirs();
    }

    @Override
    public boolean exists(String bucketName, String path) {
        return (new File(path)).exists();
    }

}
