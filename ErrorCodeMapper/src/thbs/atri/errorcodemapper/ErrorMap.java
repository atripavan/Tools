/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thbs.atri.errorcodemapper;

/**
 * 
 * @author Pavan
 */
public class ErrorMap {

	public ErrorMap() {

	}

	public String[] sourceErrorCodes;
	public String operationName;
	public String sourceName;
	public String destErrorCode;
	public String destErrorCodeDesc;
	public String[] sourceErrorCodeDesc;

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String[] getSourceErrorCodeDesc() {
		return sourceErrorCodeDesc;
	}

	public void setSourceErrorCodeDesc(String[] sourceErrorCodeDesc) {
		this.sourceErrorCodeDesc = sourceErrorCodeDesc;
	}

	public String[] getSourceErrorCodes() {
		return sourceErrorCodes;
	}

	public void setSourceErrorCodes(String[] sourceErrorCodes) {
		this.sourceErrorCodes = sourceErrorCodes;
	}

	public String getDestErrorCode() {
		return destErrorCode;
	}

	public void setDestErrorCode(String destErrorCode) {
		this.destErrorCode = destErrorCode;
	}

	public String getDestErrorCodeDesc() {
		return destErrorCodeDesc;
	}

	public void setDestErrorCodeDesc(String destErrorCodeDesc) {
		this.destErrorCodeDesc = destErrorCodeDesc;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	@Override
	public String toString() {
		System.out.println("******************\ndest error code: "
				+ this.destErrorCode + " " + this.destErrorCodeDesc);
		System.out.println("dest error code Desc: " + this.destErrorCodeDesc);
		if (this.sourceErrorCodes != null) {
			for (int i = 0; i < this.sourceErrorCodes.length; i++) {
				System.out.println("Source error code: "
						+ this.sourceErrorCodes[i]);
			}
		}
		if (this.sourceErrorCodeDesc != null) {
			{
				for (int i = 0; i < this.sourceErrorCodeDesc.length; i++) {
					System.out.println("Source error code Desc: "
							+ this.sourceErrorCodeDesc[i]);
				}
			}
		}
		System.out.println("\n*************");

		return "";
	}
}
