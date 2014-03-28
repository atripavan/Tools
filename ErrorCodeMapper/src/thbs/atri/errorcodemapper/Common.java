/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thbs.atri.errorcodemapper;

/**
 *
 * @author Pavan
 */
public class Common {
    public String operation;
    public ErrorMap[] errorMaps;

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operationNames) {
        this.operation = operationNames;
    } 

    public ErrorMap[] getErrorMaps() {
        return errorMaps;
    }

    public void setErrorMaps(ErrorMap[] errorMaps) {
        this.errorMaps = errorMaps;
    }

}
