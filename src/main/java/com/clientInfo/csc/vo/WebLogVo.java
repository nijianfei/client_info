package com.clientInfo.csc.vo;

import java.util.List;

public class WebLogVo {
    private List<String> lastLineContent; // 最后N行内容
    public String fileState;        // 文件状态(length+lastModified)
    public String linesMD5;        // 最后N行MD5

    public WebLogVo() {
    }

    public WebLogVo(List<String> lastLineContent, String fileState, String linesMD5) {
        this.lastLineContent = lastLineContent;
        this.fileState = fileState;
        this.linesMD5 = linesMD5;
    }

    public List<String> getLastLineContent() {
        return lastLineContent;
    }

    public void setLastLineContent(List<String> lastLineContent) {
        this.lastLineContent = lastLineContent;
    }

    public String getFileState() {
        return fileState;
    }

    public void setFileState(String fileState) {
        this.fileState = fileState;
    }

    public String getLinesMD5() {
        return linesMD5;
    }

    public void setLinesMD5(String linesMD5) {
        this.linesMD5 = linesMD5;
    }
}
