package com.qunar.barrier_free_qunar.java.sdk.model;

public class OssUploadResult {
    
    /**
     * 文件访问URL
     */
    private String url;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String contentType;
    
    /**
     * OSS存储路径
     */
    private String ossPath;
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public String getOssPath() {
        return ossPath;
    }
    
    public void setOssPath(String ossPath) {
        this.ossPath = ossPath;
    }

    @Override
    public String toString() {
        return "OssUploadResult{" +
                "url='" + url + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", contentType='" + contentType + '\'' +
                ", ossPath='" + ossPath + '\'' +
                '}';
    }
}