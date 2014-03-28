/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thbs.atri.errorcodemapper;

/**
 *
 * @author Pavan
 */
public class Operation {
    public String operation;
    public ErrorMap[] errorMaps;

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operationName) {
        this.operation = operationName;
    } 

    public ErrorMap[] getErrorMaps() {
        return errorMaps;
    }

    public void setErrorMaps(ErrorMap[] errorMaps) {
        this.errorMaps = errorMaps;
    }

}
