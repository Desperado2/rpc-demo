package com.desperado.common;

//RPC通信的响应数据规则
public class RpcResponse {
    //响应数据Id
    private String responseId;
    //请求数据的id
    private String requestId;
    //请求成功标志
    private boolean success;
    //响应的数据结果
    private Object result;
    //异常信息
    private Throwable throwable;

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "responseId='" + responseId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", success=" + success +
                ", result=" + result +
                ", throwable=" + throwable +
                '}';
    }
}
