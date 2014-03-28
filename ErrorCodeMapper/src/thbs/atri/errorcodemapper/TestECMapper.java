package thbs.atri.errorcodemapper;

import java.io.*;
import java.util.*;

import ooo.connector.BootstrapSocketConnector;

import org.exolab.castor.mapping.*;
import org.exolab.castor.xml.Marshaller;

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
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.XComponentContext;

public class TestECMapper {

	public static void main(String[] args) {
		Mapping objMapping = new Mapping();
		try {
			// URL of the component to be loaded
			String sUrl = "file:///C:/Documents and Settings/pavan_a/My Documents/36880 ViewMobileDataSessionInfo_1_0 Service Implementation Design.doc";


			// get the remote office component context
			XComponentContext xContext = BootstrapSocketConnector.bootstrap("C:/Program Files/OpenOffice.org 3/program/");
			if (xContext == null) {
				System.err.println("ERROR: Could not bootstrap default Office.");
			}
			XMultiComponentFactory xMCF = xContext.getServiceManager();
			// Get the root frame (i.e. desktop) of openoffice framework.
			Object oDesktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
			// Desktop has 3 interfaces. The XComponentLoader interface provides ability to load
			// components.
			XComponentLoader xCompLoader = (XComponentLoader) UnoRuntime.queryInterface(com.sun.star.frame.XComponentLoader.class, oDesktop);
			PropertyValue xPropArgs[] = new PropertyValue[1];
			xPropArgs[0]=new PropertyValue();
			xPropArgs[0].Name="Hidden";
			xPropArgs[0].Value=true;

			// Load the document, which will be displayed. More param info in apidoc
			XComponent xComp = xCompLoader.loadComponentFromURL(sUrl, "_default", 0, xPropArgs);
			ErrorCodeMapper.xTextDocument = (XTextDocument) UnoRuntime.queryInterface(
					XTextDocument.class, xComp);
			//System.out.println(ErrorCodeMapper.xTextDocument.getText().getString());

			getErrorcodeTables();

			// -- marshal the person object out as a <person>
			ErrorCodeMapper.objErrorMapXml.setServiceName("ManageQuota");
			File file = new File("C:/Documents and Settings/pavan_a/Desktop/errormap.xml");
			OutputStreamWriter osWriter = new OutputStreamWriter(new FileOutputStream(file));

			Marshaller objMarsh = new Marshaller(osWriter);

			objMarsh.setMapping(objMapping);
			//Marshaller.marshal(serv, file);
			objMarsh.marshal(ErrorCodeMapper.objErrorMapXml);
			 objMarsh.setNamespaceMapping("ec", "http://soa.o2.co.uk/config/ErrorMapConfig");

		} catch (java.lang.Exception e) {
			e.printStackTrace();
		} 

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
										if (OOUtilities.isErrorcodesTable(errorCodeTable)) {
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
										if (OOUtilities2.isErrorcodesTable(errorCodeTable)) {
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
			for(Operation operation:operationsList)
			{
				System.out.println("*********"+operation.getOperation()+"**********");
				for(ErrorMap obj:operation.getErrorMaps())
					System.out.println(obj);
			}
			Operation[] objOpErrmaps=new Operation[operationsList.size()];
			objOpErrmaps=(Operation[])operationsList.toArray(objOpErrmaps);
			ErrorCodeMapper.objErrorMapXml.setOperationNames(objOpErrmaps);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

}
