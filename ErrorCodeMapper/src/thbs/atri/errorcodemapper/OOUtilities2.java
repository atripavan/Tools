/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thbs.atri.errorcodemapper;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.table.XCell;
import com.sun.star.table.XTableColumns;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.uno.UnoRuntime;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Atri
 */
public class OOUtilities2 {

	private static Properties prop;
	
	static
	{
		try {
			 prop=new Properties();
			 FileInputStream fis = new FileInputStream("errormapgen-config.properties");
//			 int x = fis.available();
//			 byte b[] = new byte[x];
//			 fis.read(b);
			 prop.load(fis);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}

	static boolean isSplit(String cellName) {
		return cellName.contains(".1.1");
	}

	public static String getCellText(
			String cellName, XTextTable table) {
		XCell objCell = table.getCellByName(cellName);
		XText objCellText = (XText) UnoRuntime.queryInterface(
				XText.class, objCell);
		return objCellText.getString();
	}

	static String[] fillStringArrays(String temp[], int index, XTextTable errorCodeTable) {
		String[] strArray = null;
		ArrayList<String> stringArrayList = null;
		try {
			stringArrayList = new ArrayList<String>();
			int count = 0;
			for (int i = 0; i < temp[index].length(); i++) {
				if (temp[index].charAt(i) == '.') {
					count++;
				}
			}
			while ((temp[index].length()) > 4 && temp[index].contains(".")) {
				//System.out.println(getCellText(temp[index]) + " " + temp[index]);
				stringArrayList.add(getCellText(temp[index],errorCodeTable).trim());
				index++;
				//System.out.println("inside while: "+count);
			}

		} catch (StringIndexOutOfBoundsException sr) {
			System.out.println("Bypassed array out of bounds exception..");
		} catch (Exception e) {
			e.printStackTrace();
		}
		strArray = new String[stringArrayList.size()];
		stringArrayList.toArray(strArray);
		return strArray;
	}

	public static void getErrorcodeTables()
	{
		try{
			ArrayList<Operation> operationsList=new ArrayList<Operation>();
			XText xText = ErrorCodeMapper.xTextDocument.getText();
			XEnumerationAccess xParaAccess = (XEnumerationAccess) UnoRuntime.queryInterface(
					XEnumerationAccess.class, xText);
			// Call the XEnumerationAccess's only method to access the actual Enumeration
			XEnumeration xParaEnum = xParaAccess.createEnumeration();
			// While there are paragraphs, do things to them
			ArrayList<XServiceInfo> servInfoList = new ArrayList<XServiceInfo>();

			/*Get a reference to the next paragraphs XServiceInfo interface. TextTables are also part of this
		 enumeration access, so we ask the element if it is a TextTable, if it doesn't 
		 support the com.sun.star.text.TextTable service, then it is safe to assume that it really is a paragraph*/

			while (xParaEnum.hasMoreElements()) 
			{
				XServiceInfo xInfo = (XServiceInfo) UnoRuntime.queryInterface(
						XServiceInfo.class, xParaEnum.nextElement());
				servInfoList.add(xInfo);
			}
			for (int i = 0; i < servInfoList.size(); i++) {
				if ((servInfoList.get(i).getImplementationName()).equalsIgnoreCase("SwXParagraph")) {
					XTextRange range = (XTextRange) UnoRuntime.queryInterface(
							XTextRange.class, servInfoList.get(i));
					if (range != null && (range.getString().trim()).endsWith("Error Mapping")) {
						XTextCursor tempCur = xText.createTextCursorByRange(range.getEnd());
						XParagraphCursor paraCur = (XParagraphCursor) UnoRuntime.queryInterface(
								XParagraphCursor.class, tempCur);
						XPropertySet paraCurProps = (XPropertySet) UnoRuntime.queryInterface(
								XPropertySet.class, paraCur);
						if (paraCurProps != null) 
						{
							String paraStyle=(String) paraCurProps.getPropertyValue("ParaStyleName");
							if(paraStyle.equalsIgnoreCase("Heading 3"))
							{
								for (int j = 1; j < servInfoList.size(); j++) {
									if ((servInfoList.get(i + j).getImplementationName()).equalsIgnoreCase("SwXTextTable")) {

										XTextTable errorCodeTable = (XTextTable) UnoRuntime.queryInterface(
												XTextTable.class, servInfoList.get(i + j));
										if (isErrorcodesTable(errorCodeTable)) {
											for (int k = 0; (i + j) - k > 1; k++) {
												if (servInfoList.get((i + j) - k).getImplementationName().equalsIgnoreCase("SwXParagraph")) {
													range = (XTextRange) UnoRuntime.queryInterface(
															XTextRange.class, servInfoList.get((i + j) - k));
													tempCur = xText.createTextCursorByRange(range.getEnd());
													paraCur = (XParagraphCursor) UnoRuntime.queryInterface(
															XParagraphCursor.class, tempCur);
													paraCurProps = (XPropertySet) UnoRuntime.queryInterface(
															XPropertySet.class, paraCur);
													paraStyle=(String) paraCurProps.getPropertyValue("ParaStyleName");
													if(paraStyle.equalsIgnoreCase("Heading 2"))
													{
														String operationName=(range.getString().trim()).substring(3);
														System.out.println("Op name:"+operationName);
														Operation objOperation=new Operation();
														objOperation.setOperation(operationName);
														ArrayList<ErrorMap> errorMapsList=OOUtilities2.fillErrorMaps(errorCodeTable, operationName);
														ErrorMap[] objErrmaps=new ErrorMap[errorMapsList.size()];
														objErrmaps = (ErrorMap[]) errorMapsList.toArray(objErrmaps);
														objOperation.setErrorMaps(objErrmaps);
														operationsList.add(objOperation);														
														break;
													}
												}
											}
										}
										break;
									}
								}
							}

						}
					}
					else if((range.getString().trim()).endsWith("Common Errors"))
					{
						Common objComnMap=new Common();
						XTextCursor tempCur = xText.createTextCursorByRange(range.getEnd());
						XParagraphCursor paraCur = (XParagraphCursor) UnoRuntime.queryInterface(
								XParagraphCursor.class, tempCur);
						XPropertySet paraCurProps = (XPropertySet) UnoRuntime.queryInterface(
								XPropertySet.class, paraCur);
						if (paraCurProps != null) 
						{
							String paraStyle=(String) paraCurProps.getPropertyValue("ParaStyleName");
							if(paraStyle.equalsIgnoreCase("Heading 2"))
							{
								for (int j = 1; j < servInfoList.size(); j++)
								{
									if ((servInfoList.get(i + j).getImplementationName()).equalsIgnoreCase("SwXTextTable")) 
									{

										XTextTable errorCodeTable = (XTextTable) UnoRuntime.queryInterface(
												XTextTable.class, servInfoList.get(i + j));
										if (isErrorcodesTable(errorCodeTable)) {
											ArrayList<ErrorMap> errorMapsList=OOUtilities2.fillErrorMaps(errorCodeTable, "Common");
											ErrorMap[] objErrmaps=new ErrorMap[errorMapsList.size()];
											objErrmaps = (ErrorMap[]) errorMapsList.toArray(objErrmaps);
											objComnMap.setErrorMaps(objErrmaps);
											objComnMap.setOperation("Common");
											break;
										}
									}
								}
							}

						}
						ErrorCodeMapper.objErrorMapXml.setCommonMap(objComnMap);
					}
				}
			}
			Operation[] objOpErrmaps=new Operation[operationsList.size()];
			objOpErrmaps=operationsList.toArray(objOpErrmaps);
			ErrorCodeMapper.objErrorMapXml.setOperationNames(objOpErrmaps);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public static boolean isErrorcodesTable(XTextTable table) {
		XTableColumns cols = table.getColumns();
		String destCodeColHeader=getCellText( prop.getProperty(ErrorCodeMapperConstants.DEST_CODE_COLUMN) + "1", table);
		System.out.println("DEst code col header:"+destCodeColHeader);
		int colCount= Integer.parseInt(prop.getProperty(ErrorCodeMapperConstants.NUM_ERR_CODE_TABLE_COL)); 
		if (cols.getCount() == colCount && destCodeColHeader.equalsIgnoreCase( prop.getProperty(ErrorCodeMapperConstants.DEST_CODE_COL_HEADER) )) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("finally")
	public static ArrayList<ErrorMap> fillErrorMaps(XTextTable errorCodeTable, String operationName) 
	{
		ArrayList<ErrorMap> objErrmaps=new ArrayList<ErrorMap>();
		String cellNames[]=errorCodeTable.getCellNames();
		ErrorMap objErrMap = null;
		int index=0;
		int rowNo=2;
		try {
			while (cellNames[index] != null) 
			{
				//Row objRow = new Row();
				objErrMap = new ErrorMap();
				while (!((cellNames[index].substring(1)).equalsIgnoreCase(Integer.toString(rowNo + 1)))) 
				{
					String strRowNo = Integer.toString(rowNo);
					System.out.println("Inside second while" + " " + cellNames[index]);
					if (cellNames[index].equalsIgnoreCase( prop.getProperty(ErrorCodeMapperConstants.DEST_CODE_COLUMN)  + strRowNo)) {
						objErrMap.setDestErrorCode(getCellText(cellNames[index],errorCodeTable).trim());
						// objRow.setDestErrorCode(getCellText(cellNames[index]));
						System.out.println(getCellText(cellNames[index],errorCodeTable) + " " + cellNames[index]);

					} 

					else if (cellNames[index].equalsIgnoreCase( prop.getProperty(ErrorCodeMapperConstants.DEST_CODE_DESC_COLUMN)  + strRowNo)) {
						objErrMap.setDestErrorCodeDesc(getCellText(cellNames[index],errorCodeTable).trim());
						//objRow.setDestErrorCodeDesc(getCellText(cellNames[index]));
						System.out.println(getCellText(cellNames[index],errorCodeTable) + " " + cellNames[index]);
					} 

					else if (cellNames[index].equalsIgnoreCase( prop.getProperty(ErrorCodeMapperConstants.FAULT_ORIGIN_COLUMN)  + strRowNo)) {
						objErrMap.setOperationName((getCellText(cellNames[index],errorCodeTable)));
						System.out.println(getCellText(cellNames[index],errorCodeTable) + " " + cellNames[index]);
					} 

					else if (cellNames[index].startsWith( prop.getProperty(ErrorCodeMapperConstants.SRC_CODE_COLUMN)  + strRowNo)) {
						if (isSplit(cellNames[index])) {
							objErrMap.setSourceErrorCodes(fillStringArrays(cellNames, index, errorCodeTable));
							//objRow.setSourceErrorCode(fillStringArrays(cellNames, index));
							index--;
							// System.out.println("source code: "+fillStringArrays(cellNames, index)[0]);
							System.out.println("for E first if" + getCellText(cellNames[index],errorCodeTable) + " " + cellNames[index]);

						} else if (cellNames[index].equalsIgnoreCase( prop.getProperty(ErrorCodeMapperConstants.SRC_CODE_COLUMN)  + strRowNo)) {
							String[] sourceCodes = {getCellText(cellNames[index],errorCodeTable).trim()};
							//objRow.setSourceErrorCode(sourceCodes);
							objErrMap.setSourceErrorCodes(sourceCodes);
							System.out.println("for E second if" + getCellText(cellNames[index],errorCodeTable) + " " + cellNames[index]);

						}
					} 

					else if (cellNames[index].equalsIgnoreCase( prop.getProperty(ErrorCodeMapperConstants.SRC_CODE_DESC_COLUMN)  + strRowNo)) {
						if (isSplit(cellNames[index])) {
							// objRow.setSourceErrorDesc(fillStringArrays(cellNames, index));
							index--;
							System.out.println("for F first if" + getCellText(cellNames[index],errorCodeTable) + " " + cellNames[index]);

						} else if (cellNames[index].equalsIgnoreCase( prop.getProperty(ErrorCodeMapperConstants.SRC_CODE_DESC_COLUMN)  + strRowNo)) {
							String[] sourceDesc = {getCellText(cellNames[index],errorCodeTable).trim()};
							objErrMap.setSourceErrorCodeDesc(sourceDesc);
							System.out.println("for F second if" + getCellText(cellNames[index],errorCodeTable) + " " + cellNames[index]);

						}
					}
					index++;
				}
				rowNo++;
				System.out.println(objErrMap);
				objErrmaps.add(objErrMap);
			}
		} catch (ArrayIndexOutOfBoundsException ar) {
			System.err.println("Bypassing array out of bounds exception..");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			objErrmaps.add(objErrMap);
			return objErrmaps;
		}
	} 
	
	public static String getFileAsString(String fileName) throws Exception
	{
		 FileInputStream fis = new FileInputStream(fileName);
		 int x = fis.available();
		 byte b[] = new byte[x];
		 fis.read(b);
		 return new String(b);		
	}
	
}




class Row {

	private String destErrorCode;
	private String errorCategory;
	private String destErrorCodeDesc;
	private String[] operations;
	private String[] sourceErrorCode;
	private String[] sourceErrorDesc;

	public String getDestErrorCode() {
		return destErrorCode;
	}

	public void setDestErrorCode(String destErrorCode) {
		this.destErrorCode = destErrorCode;
	}

	public String getErrorCategory() {
		return errorCategory;
	}

	public void setErrorCategory(String errorCategory) {
		this.errorCategory = errorCategory;
	}

	public String getDestErrorCodeDesc() {
		return destErrorCodeDesc;
	}

	public void setDestErrorCodeDesc(String destErrorCodeDesc) {
		this.destErrorCodeDesc = destErrorCodeDesc;
	}

	public String[] getOperations() {
		return operations;
	}

	public void setOperations(String[] operations) {
		this.operations = operations;
	}

	public String[] getSourceErrorCode() {
		return sourceErrorCode;
	}

	public void setSourceErrorCode(String[] sourceErrorCode) {
		this.sourceErrorCode = sourceErrorCode;
	}

	public String[] getSourceErrorDesc() {
		return sourceErrorDesc;
	}

	public void setSourceErrorDesc(String[] sourceErrorDesc) {
		this.sourceErrorDesc = sourceErrorDesc;
	}
}
