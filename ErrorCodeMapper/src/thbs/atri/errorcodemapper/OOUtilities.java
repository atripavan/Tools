/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thbs.atri.errorcodemapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.table.XCell;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.uno.UnoRuntime;

/**
 *
 * @author Atri
 */
public class OOUtilities {
	private static int index=0;
	private static Properties prop;
	
	static
	{
		try {
			 prop=new Properties();
			 FileInputStream fis = new FileInputStream("errormapgen-config.properties");
			 prop.load(fis);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}
	
	@SuppressWarnings("finally")
	public static ArrayList<ErrorMap> fillErrorMaps(XTextTable errorCodeTable, String operationName) 
	{
		ArrayList<ErrorMap> objErrmaps=new ArrayList<ErrorMap>();
		String cellNames[]=errorCodeTable.getCellNames();
		index=0;
		ErrorMap objErrMap = null;
		int rowNo=2;
		try {
			while (cellNames[index] != null) 
			{
				objErrMap = new ErrorMap();
				while (!((cellNames[index].substring(1)).equalsIgnoreCase(Integer.toString(rowNo + 1)))) 
				{
					String strRowNo = Integer.toString(rowNo);
					if (cellNames[index].equalsIgnoreCase(prop.getProperty(ErrorCodeMapperConstants.DEST_CODE_COLUMN) + strRowNo)) 
					{
						objErrMap.setDestErrorCode(replaceSpecChars(getCellText(cellNames[index],errorCodeTable)));
					}

					else if (cellNames[index].equalsIgnoreCase(prop.getProperty(ErrorCodeMapperConstants.DEST_CODE_DESC_COLUMN) + strRowNo)) 
					{
						objErrMap.setDestErrorCodeDesc(replaceSpecChars(getCellText(cellNames[index],errorCodeTable)));
					} 

					else if (cellNames[index].startsWith(prop.getProperty(ErrorCodeMapperConstants.FAULT_ORIGIN_COLUMN) + strRowNo ))
					{			
						if (cellNames[index].equalsIgnoreCase(prop.getProperty(ErrorCodeMapperConstants.FAULT_ORIGIN_COLUMN) + strRowNo)) 
						{
							objErrMap.setSourceName(replaceSpecChars(getCellText(cellNames[index],errorCodeTable).trim()));
						}		
						else if (!(checkCDEMerge(cellNames[index])=='0'))
						{
							List<String> srcCodesList=new ArrayList<String>();
							List<String> srcCodesDescList=new ArrayList<String>();
							while(!(checkCDEMerge(cellNames[index])=='0'))	
							{
								if (checkCDEMerge(cellNames[index])=='2')
									srcCodesList.add(replaceSpecChars(getCellText(cellNames[index], errorCodeTable)));
								else if (checkCDEMerge(cellNames[index])=='3') 
									srcCodesDescList.add(replaceSpecChars(getCellText(cellNames[index], errorCodeTable)));

								index++;
							}
							if(srcCodesList!=null)
								objErrMap.setSourceErrorCodes(srcCodesList.toArray(new String[srcCodesList.size()]));
							if(srcCodesDescList!=null)
								objErrMap.setSourceErrorCodeDesc(srcCodesDescList.toArray(new String[srcCodesDescList.size()]));
							index--;
						} 
					}

					else if (cellNames[index].startsWith(prop.getProperty(ErrorCodeMapperConstants.SRC_CODE_COLUMN) + strRowNo ))
					{					
						if (cellNames[index].equalsIgnoreCase(prop.getProperty(ErrorCodeMapperConstants.SRC_CODE_COLUMN) + strRowNo)) 
						{
							String[] sourceCodes = {replaceSpecChars(getCellText(cellNames[index],errorCodeTable))};
							objErrMap.setSourceErrorCodes(sourceCodes);
						}		
						else if (isSplit(cellNames[index])) {
							objErrMap.setSourceErrorCodes(fillStringArrays(cellNames, errorCodeTable));
							index--;
						} 
					}
					//					else if (cellNames[index].startsWith("E" + strRowNo)) 
					//					{
					//						if (cellNames[index].equalsIgnoreCase("E" + strRowNo)) {
					//							String[] sourceDesc = {replaceSpecChars(getCellText(cellNames[index],errorCodeTable))};
					//							objErrMap.setSourceErrorCodeDesc(sourceDesc);
					//						}
					//						else if (isSplit(cellNames[index])) 
					//						{
					//							objErrMap.setSourceErrorCodeDesc(fillStringArrays(cellNames, errorCodeTable));
					//							index--;
					//						}
					//					}
					index++;
				}
				rowNo++;
				System.out.println(objErrMap);
				objErrmaps.add(objErrMap);
			}
		} catch (ArrayIndexOutOfBoundsException ar) {
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			objErrmaps.add(objErrMap);
			return objErrmaps;
		}
	}

	static String[] fillStringArrays(String temp[], XTextTable errorCodeTable) {
		String[] strArray = null;
		ArrayList<String> stringArrayList = null;
		try {
			stringArrayList = new ArrayList<String>();
			while (isSplit(temp[index])) 
			{
				if(checkDEMerge(temp[index])!='2' && checkDEMerge(temp[index])!='3')
					stringArrayList.add(replaceSpecChars(getCellText(temp[index],errorCodeTable).trim()));
				index++;
			}

		} catch (ArrayIndexOutOfBoundsException ar) {
		}catch (StringIndexOutOfBoundsException sr) {
		} catch (Exception e) {
			e.printStackTrace();
		}
		strArray = new String[stringArrayList.size()];
		stringArrayList.toArray(strArray);
		return strArray;
	}

	public static String getCellText(
			String cellName, XTextTable table) {
		XCell objCell = table.getCellByName(cellName);
		XText objCellText = (XText) UnoRuntime.queryInterface(
				XText.class, objCell);
		if(objCell!=null)
			return objCellText.getString().trim();
		else
			return null;
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
					String paraContent=range.getString().trim();
					if (range != null && paraContent.endsWith("Error Mapping")) {
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
														System.out.println("*************Operation:"+operationName+"**************");
														Operation objOperation=new Operation();
														objOperation.setOperation(operationName);
														ArrayList<ErrorMap> errorMapsList=OOUtilities.fillErrorMaps(errorCodeTable, operationName);
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
					else if((paraContent.toLowerCase().contains("common") &&
							paraContent.toLowerCase().contains("error")))
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
							if(!paraStyle.equalsIgnoreCase("Heading 3"))
							{
								for (int j = 1; j < servInfoList.size(); j++)
								{
									if ((servInfoList.get(i + j).getImplementationName()).equalsIgnoreCase("SwXTextTable")) 
									{

										XTextTable errorCodeTable = (XTextTable) UnoRuntime.queryInterface(
												XTextTable.class, servInfoList.get(i + j));
										if (isErrorcodesTable(errorCodeTable)) {
											ArrayList<ErrorMap> errorMapsList=OOUtilities.fillErrorMaps(errorCodeTable, "Common");
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
		if (getCellText(  prop.getProperty(ErrorCodeMapperConstants.SRC_CODE_DESC_COLUMN)+"1"  , table)!=""
									&&
			getCellText(  prop.getProperty(ErrorCodeMapperConstants.DEST_CODE_COLUMN)+"1"  , table).equalsIgnoreCase(prop.getProperty(ErrorCodeMapperConstants.DEST_CODE_COL_HEADER)))
			return true;
		else
			return false;
	}

	public static char checkCDEMerge(String cell)
	{
		char mergeIndex='0';
		if( isSplit(cell) && cell.startsWith(prop.getProperty(ErrorCodeMapperConstants.FAULT_ORIGIN_COLUMN)))
		{
			mergeIndex=cell.charAt(cell.indexOf(".")+1);
		}
		return mergeIndex;
	}

	public static char checkDEMerge(String cell)
	{
		char mergeIndex='0';
		if( isSplit(cell) && cell.startsWith("D"))
		{
			mergeIndex=cell.charAt(cell.indexOf(".")+1);
		}
		return mergeIndex;
	}

	public static String replaceCharAt(String s, int pos, char c) {
		return s.substring(0,pos) + c + s.substring(pos+1);
	}

	public static boolean isSplit(String cellName) {
		boolean split=cellName.matches("[A-Z]\\d*\\.\\d.\\d");
		return split;
	} 

	/**
    Escape characters for text appearing in HTML markup.

    <P>This method exists as a defence against Cross Site Scripting (XSS) hacks.
    The idea is to neutralize control characters commonly used by scripts, such that
    they will not be executed by the browser. This is done by replacing the control
    characters with their escaped equivalents.  

    <P>The following characters are replaced with corresponding 
    HTML character entities :
    <table border='1' cellpadding='3' cellspacing='0'>
    <tr><th> Character </th><th>Replacement</th></tr>
    <tr><td> < </td><td> &lt; </td></tr>
    <tr><td> > </td><td> &gt; </td></tr>
    <tr><td> & </td><td> &amp; </td></tr>
    <tr><td> " </td><td> &quot;</td></tr>
    <tr><td> \t </td><td> &#009;</td></tr>
    <tr><td> ! </td><td> &#033;</td></tr>
    <tr><td> # </td><td> &#035;</td></tr>
    <tr><td> $ </td><td> &#036;</td></tr>
    <tr><td> % </td><td> &#037;</td></tr>
    <tr><td> ' </td><td> &#039;</td></tr>
    <tr><td> ( </td><td> &#040;</td></tr> 
    <tr><td> ) </td><td> &#041;</td></tr>
    <tr><td> * </td><td> &#042;</td></tr>
    <tr><td> + </td><td> &#043; </td></tr>
    <tr><td> , </td><td> &#044; </td></tr>
    <tr><td> - </td><td> &#045; </td></tr>
    <tr><td> . </td><td> &#046; </td></tr>
    <tr><td> / </td><td> &#047; </td></tr>
    <tr><td> : </td><td> &#058;</td></tr>
    <tr><td> ; </td><td> &#059;</td></tr>
    <tr><td> = </td><td> &#061;</td></tr>
    <tr><td> ? </td><td> &#063;</td></tr>
    <tr><td> @ </td><td> &#064;</td></tr>
    <tr><td> [ </td><td> &#091;</td></tr>
    <tr><td> \ </td><td> &#092;</td></tr>
    <tr><td> ] </td><td> &#093;</td></tr>
    <tr><td> ^ </td><td> &#094;</td></tr>
    <tr><td> _ </td><td> &#095;</td></tr>
    <tr><td> ` </td><td> &#096;</td></tr>
    <tr><td> { </td><td> &#123;</td></tr>
    <tr><td> | </td><td> &#124;</td></tr>
    <tr><td> } </td><td> &#125;</td></tr>
    <tr><td> ~ </td><td> &#126;</td></tr>
    </table>

    <P>Note that JSTL's {@code <c:out>} escapes <em>only the first 
    five</em> of the above characters.
	 */

	public static String replaceSpecChars(String aText)
	{
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character =  iterator.current();
		while (character != CharacterIterator.DONE ){
			if (character == '<') {
				result.append("&lt;");
			}
			else if (character == '>') {
				result.append("&gt;");
			}
			else if (character == '&') {
				result.append("&amp;");
			}
			else if (character == '\"') {
				result.append("&quot;");
			}
			else if (character == '\'') {
				result.append("&apos;");
			}
			else if (character == '\t') {
				addCharEntity(9, result);
			}
			else if (character == '!') {
				addCharEntity(33, result);
			}
			else if (character == '#') {
				addCharEntity(35, result);
			}       
			else if (character == '$') {
				addCharEntity(36, result);
			}
			else if (character == '%') {
				addCharEntity(37, result);
			}
			else if (character == '/') {
				addCharEntity(47, result);
			}       
			else if (character == '=') {
				addCharEntity(61, result);
			}
			else if (character == '\\') {
				addCharEntity(92, result);
			}
			else if (character == '`') {
				addCharEntity(96, result);
			}
			else if (character == '|') {
				addCharEntity(124, result);
			}
			else {
				//the char is not a special one
				//add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}
	public static void addCharEntity(Integer aIdx, StringBuilder aBuilder){
		String padding = "";
		if( aIdx <= 9 ){
			padding = "00";
		}
		else if( aIdx <= 99 ){
			padding = "0";
		}
		else {
			//no prefix
		}
		String number = padding + aIdx.toString();
		aBuilder.append("&#" + number + ";");
	}

}
