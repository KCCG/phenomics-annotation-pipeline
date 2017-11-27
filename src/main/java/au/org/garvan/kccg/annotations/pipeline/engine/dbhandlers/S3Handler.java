package au.org.garvan.kccg.annotations.pipeline.engine.dbhandlers;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.publicational.Article;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.ByteArrayInputStream;

/**
 * Created by ahmed on 22/11/17.
 */
public class S3Handler {

    private static String bucketName = "phenomics-article-abstracts";
    private static String region = "ap-southeast-2";
    private static AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).withRegion(region).build();

    public  void storeAbstract(Article article) {
            String keyName = String.format("%d.json", article.getPubMedID());
            byte[] bytesToWrite = article.getArticleAbstract().constructJson().toString().getBytes();
            ObjectMetadata omd = new ObjectMetadata();
            omd.setContentLength(bytesToWrite.length);
            PutObjectResult result =  s3client.putObject(new PutObjectRequest(bucketName, keyName, new ByteArrayInputStream(bytesToWrite), omd));

    }
    public S3Handler(){}

}
