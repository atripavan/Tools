/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thbs.atri.errorcodemapper;

/**
 *
 * @author Pavan
 */
public class ErrorMapConfig {

    public ErrorMapConfig() {
    }
    public String serviceName;
    public Operation[] operationNames;
    public Common commonMap;
    public ErrorMap unknownErrorCode;
  //  public ErrorMap[] errorMaps;
  //  private String[] operations;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Operation[] getOperationNames() {
        return operationNames;
    }

    public void setOperationNames(Operation[] operationNames) {
        this.operationNames = operationNames;
    }

    public Common getCommonMap() {
        return commonMap;
    }

    public void setCommonMap(Common commonMap) {
        this.commonMap = commonMap;
    }

    public ErrorMap getUnknownErrorCode() {
        return unknownErrorCode;
    }

    public void setUnknownErrorCode(ErrorMap unknownErrorCode) {
        this.unknownErrorCode = unknownErrorCode;
    }
}