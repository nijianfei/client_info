package com.clientInfo.csc.vo;

public class ResultVo {
    private String resultCls;
    private String resultDetail;

    public ResultVo() {
    }

    public ResultVo(String resultCls) {
        this.resultCls = resultCls;
    }

    public ResultVo(String resultCls, String resultDetail) {
        this.resultCls = resultCls;
        this.resultDetail = resultDetail;
    }

    public String getResultCls() {
        return resultCls;
    }

    public void setResultCls(String resultCls) {
        this.resultCls = resultCls;
    }

    public String getResultDetail() {
        return resultDetail;
    }

    public void setResultDetail(String resultDetail) {
        this.resultDetail = resultDetail;
    }
}
