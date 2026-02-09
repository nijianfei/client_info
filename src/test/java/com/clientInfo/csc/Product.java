
package com.clientInfo.csc;

@SuppressWarnings("unused")
public class Product {


    private Extensions extensions;

    private String modelId;

    private Long productDeviceType;

    private String productId;

    private String productName;

    public Extensions getExtensions() {
        return extensions;
    }

    public void setExtensions(Extensions extensions) {
        this.extensions = extensions;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Long getProductDeviceType() {
        return productDeviceType;
    }

    public void setProductDeviceType(Long productDeviceType) {
        this.productDeviceType = productDeviceType;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

}
