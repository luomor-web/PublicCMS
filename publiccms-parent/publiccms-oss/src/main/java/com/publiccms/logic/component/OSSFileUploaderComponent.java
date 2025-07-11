package com.publiccms.logic.component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.publiccms.common.api.FileUploader;
import com.publiccms.common.cache.CacheEntity;
import com.publiccms.common.cache.CacheEntityFactory;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.logic.component.config.ConfigDataComponent;
import com.publiccms.views.pojo.entities.ConfigableCacheEntity;
import com.publiccms.views.pojo.entities.FileUploadResult;

import jakarta.annotation.Resource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * OSSFileUploaderComponent 对象存储上传组件
 * 
 */
@Component
public class OSSFileUploaderComponent implements FileUploader {
    @Resource
    private ConfigDataComponent configDataComponent;
    private CacheEntity<Short, ConfigableCacheEntity<S3Client>> cache;
    private CacheEntity<Short, ConfigableCacheEntity<S3Presigner>> presignerCache;

    public S3Presigner getPresigner(short siteId, Map<String, String> config) {
        ConfigableCacheEntity<S3Presigner> configableCacheEntity = presignerCache.get(siteId);
        if (null == configableCacheEntity || !config.equals(configableCacheEntity.getConfig())) {
            synchronized (presignerCache) {
                configableCacheEntity = presignerCache.get(siteId);
                if (null == configableCacheEntity || !config.equals(configableCacheEntity.getConfig())) {
                    S3Presigner presigner = S3Presigner.builder()
                            .credentialsProvider(StaticCredentialsProvider
                                    .create(AwsBasicCredentials.create(config.get(OSSComponent.CONFIG_ACCESSKEYID),
                                            config.get(OSSComponent.CONFIG_ACCESSKEYSECRET))))
                            .region(Region.AWS_GLOBAL)
                            .endpointOverride(URI.create(config.get(OSSComponent.CONFIG_PRIVATE_ENDPOINT)))
                            .serviceConfiguration(
                                    S3Configuration.builder().pathStyleAccessEnabled(false).chunkedEncodingEnabled(false).build())
                            .build();
                    if (null == configableCacheEntity) {
                        configableCacheEntity = new ConfigableCacheEntity<>(presigner, config);
                    } else {
                        configableCacheEntity.setConfig(config);
                        configableCacheEntity.setEntity(presigner);
                    }
                    presignerCache.put(siteId, configableCacheEntity);
                }
            }
        }
        return configableCacheEntity.getEntity();
    }

    public S3Client getClient(short siteId, boolean privatefile, Map<String, String> config) {
        ConfigableCacheEntity<S3Client> configableCacheEntity = cache.get(siteId);
        if (null == configableCacheEntity || !config.equals(configableCacheEntity.getConfig())) {
            synchronized (cache) {
                configableCacheEntity = cache.get(siteId);
                if (null == configableCacheEntity || !config.equals(configableCacheEntity.getConfig())) {
                    S3Client client = S3Client.builder()
                            .credentialsProvider(StaticCredentialsProvider
                                    .create(AwsBasicCredentials.create(config.get(OSSComponent.CONFIG_ACCESSKEYID),
                                            config.get(OSSComponent.CONFIG_ACCESSKEYSECRET))))
                            .region(Region.AWS_GLOBAL)
                            .endpointOverride(URI.create(config
                                    .get(privatefile ? OSSComponent.CONFIG_PRIVATE_ENDPOINT : OSSComponent.CONFIG_ENDPOINT)))
                            .serviceConfiguration(
                                    S3Configuration.builder().pathStyleAccessEnabled(false).chunkedEncodingEnabled(false).build())
                            .build();
                    if (null == configableCacheEntity) {
                        configableCacheEntity = new ConfigableCacheEntity<>(client, config);
                    } else {
                        configableCacheEntity.setConfig(config);
                        configableCacheEntity.setEntity(client);
                    }
                    cache.put(siteId, configableCacheEntity);
                }
            }
        }
        return configableCacheEntity.getEntity();
    }

    @Override
    public boolean enableUpload(short siteId, boolean privatefile) {
        Map<String, String> config = configDataComponent.getConfigData(siteId, OSSComponent.CONFIG_CODE);
        return enableUpload(config, privatefile);
    }

    @Override
    public boolean enablePrefix(short siteId, boolean privatefile) {
        Map<String, String> config = configDataComponent.getConfigData(siteId, OSSComponent.CONFIG_CODE);
        return CommonUtils.notEmpty(config.get(privatefile ? OSSComponent.CONFIG_PRIVATE_CDN_URL : OSSComponent.CONFIG_CDN_URL))
                || CommonUtils.notEmpty(
                        config.get(privatefile ? OSSComponent.CONFIG_PRIVATE_BUCKET_URL : OSSComponent.CONFIG_BUCKET_URL));
    }

    public static boolean enableUpload(Map<String, String> config, boolean privatefile) {
        return CommonUtils.notEmpty(config) && CommonUtils.notEmpty(config.get(OSSComponent.CONFIG_ACCESSKEYID))
                && CommonUtils.notEmpty(config.get(OSSComponent.CONFIG_ACCESSKEYSECRET))
                && CommonUtils.notEmpty(config.get(privatefile ? OSSComponent.CONFIG_PRIVATE_REGION : OSSComponent.CONFIG_REGION))
                && CommonUtils
                        .notEmpty(config.get(privatefile ? OSSComponent.CONFIG_PRIVATE_ENDPOINT : OSSComponent.CONFIG_ENDPOINT))
                && CommonUtils.notEmpty(config.get(privatefile ? OSSComponent.CONFIG_PRIVATE_BUCKET : OSSComponent.CONFIG_BUCKET))
                && CommonUtils.notEmpty(
                        config.get(privatefile ? OSSComponent.CONFIG_PRIVATE_BUCKET_URL : OSSComponent.CONFIG_BUCKET_URL));
    }

    @Override
    public String getPrivateFileUrl(short siteId, int expiryMinutes, String filepath) {
        Map<String, String> config = configDataComponent.getConfigData(siteId, OSSComponent.CONFIG_CODE);
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .getObjectRequest(t -> t.bucket(config.get(OSSComponent.CONFIG_PRIVATE_BUCKET)).key(filepath)).build();
        S3Presigner presigner = getPresigner(siteId, config);
        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    public String getPrefix(short siteId, boolean privatefile) {
        Map<String, String> config = configDataComponent.getConfigData(siteId, OSSComponent.CONFIG_CODE);
        String prefix = config.get(privatefile ? OSSComponent.CONFIG_PRIVATE_CDN_URL : OSSComponent.CONFIG_CDN_URL);
        if (CommonUtils.empty(prefix)) {
            prefix = config.get(privatefile ? OSSComponent.CONFIG_PRIVATE_BUCKET_URL : OSSComponent.CONFIG_BUCKET_URL);
        }
        if (prefix.endsWith(Constants.SEPARATOR)) {
            return prefix;
        } else {
            return CommonUtils.joinString(prefix, Constants.SEPARATOR);
        }
    }

    @Override
    public FileUploadResult upload(short siteId, MultipartFile file, boolean privatefile, String filepath, Locale locale)
            throws IOException {
        Map<String, String> config = configDataComponent.getConfigData(siteId, OSSComponent.CONFIG_CODE);
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(config.get(privatefile ? OSSComponent.CONFIG_PRIVATE_BUCKET : OSSComponent.CONFIG_BUCKET)).key(filepath)
                .build();
        S3Client client = getClient(siteId, privatefile, config);
        try (InputStream inputStream = file.getInputStream()) {
            client.putObject(objectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        }
        FileUploadResult uploadResult = new FileUploadResult();
        uploadResult.setFilename(filepath);
        uploadResult.setFileSize(file.getSize());
        return uploadResult;
    }

    @Override
    public FileUploadResult upload(short siteId, byte[] file, boolean privatefile, String filepath, Locale locale)
            throws IOException {
        Map<String, String> config = configDataComponent.getConfigData(siteId, OSSComponent.CONFIG_CODE);
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(config.get(privatefile ? OSSComponent.CONFIG_PRIVATE_BUCKET : OSSComponent.CONFIG_BUCKET)).key(filepath)
                .build();
        S3Client client = getClient(siteId, privatefile, config);
        client.putObject(objectRequest, RequestBody.fromBytes(file));
        FileUploadResult uploadResult = new FileUploadResult();
        uploadResult.setFilename(filepath);
        uploadResult.setFileSize(file.length);
        return uploadResult;
    }

    @Override
    public void clear() {
        cache.clear(false);
        presignerCache.clear(false);
    }

    @Override
    public void clear(short siteId) {
        cache.remove(siteId);
        presignerCache.remove(siteId);
    }

    /**
     * @param cacheEntityFactory
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    @Resource
    public void initCache(CacheEntityFactory cacheEntityFactory)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        cache = cacheEntityFactory.createCacheEntity(OSSComponent.CONFIG_CODE, CacheEntityFactory.MEMORY_CACHE_ENTITY);
        presignerCache = cacheEntityFactory.createCacheEntity(OSSComponent.CONFIG_CODE + "Presigner",
                CacheEntityFactory.MEMORY_CACHE_ENTITY);
    }

    @Override
    public String getCacheCode() {
        return OSSComponent.CONFIG_CODE;
    }
}
