/*
 * OfficeUNOClientApp.java
 *
 * Created on 2008.12.13 - 15:53:07
 *
 */
package thbs.atri.errorcodemapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import ooo.connector.BootstrapSocketConnector;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Marshaller;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sun.star.beans.PropertyValue;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;


public class ErrorCodeMapper {

	public ErrorCodeMapper() {
	}
	static Mapping objMapping = new Mapping();
	static
	{
		InputStream inputStream =  ErrorCodeMapper.class.getClassLoader().getResourceAsStream("errormap-mapper.xml");
		InputSource objIs=new InputSource(inputStream);
		objMapping.loadMapping(objIs);
	}
	/** Creates a new instance of OfficeUNOClientApp */
	public static XTextDocument xTextDocument;
	public static ErrorMapConfig objErrorMapXml=new ErrorMapConfig();

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Enter the Open office installation path\n" +
			"For e.g., C:/Program Files/OpenOffice.org 2.3/program\n");
			BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
			String ooPath = cin.readLine();
			// get the remote office component context
			XComponentContext xContext = BootstrapSocketConnector.bootstrap(ooPath);
			if (xContext == null) {
				System.err.println("Could not bootstrap default Office.\nCheck installation path.");
				System.exit(0);
			}
			XMultiComponentFactory xMCF = xContext.getServiceManager();
			// Get the root frame (i.e. desktop) of openoffice framework.
			Object oDesktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
			// Desktop has 3 interfaces. The XComponentLoader interface provides ability to load
			// components.

			System.out.println("Enter the URL of SID, for which the errormap-config.xml is to be generated\n" +
			"For e.g., C:/Program Files/36560 ViewBusinessCustomerUsage_2_0 Service Implementation Design.doc\n");
			String sidUrl =cin.readLine();
			String serviceName=null;
			try {
				sidUrl=sidUrl.replace("\\","/");
				serviceName=sidUrl.substring(sidUrl.lastIndexOf("/")+1);
				serviceName = serviceName.split("\\ ")[1];
				// URL of the component to be loaded
				sidUrl = "file:///"+sidUrl;
				XComponentLoader xCompLoader = (XComponentLoader) UnoRuntime.queryInterface
				(com.sun.star.frame.XComponentLoader.class, oDesktop);
				PropertyValue xPropArgs[] = new PropertyValue[1];
				xPropArgs[0]=new PropertyValue();
				xPropArgs[0].Name="Hidden";
				xPropArgs[0].Value=true;
				// Load the document, which will be displayed. More param info in apidoc
				XComponent xComp = xCompLoader.loadComponentFromURL(sidUrl, "_default", 0, xPropArgs);
				xTextDocument = (XTextDocument) UnoRuntime.queryInterface(
						XTextDocument.class, xComp);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("SID file name is not in convention");
				System.exit(0);
			}catch (IllegalArgumentException e) {
				System.err.println("Could not find SID.Check the entered URL.");
				System.exit(0);
			}

			System.out.println("Enter the URL where you want the errormap-config XML to be generated\n" +
			"For e.g., C:/Program Files/\n");
			String xmlLoc = cin.readLine();
			xmlLoc=xmlLoc+"/errormap-config.xml";

			OOUtilities.getErrorcodeTables();

			//marshal the person object out as a <person>
			objErrorMapXml.setServiceName(serviceName);
			File file = new File(xmlLoc);
			OutputStreamWriter osWriter = new OutputStreamWriter(new FileOutputStream(file));

			Marshaller objMarsh = new Marshaller(osWriter);
			objMarsh.setMapping(objMapping);
			objMarsh.marshal(objErrorMapXml);

			checkWellFormed(xmlLoc);

		} catch (BootstrapException e) {
			System.err.println("Could not bootstrap default Office.\nCheck installation path.");
		} catch (IOException e) {
			System.err.println("File not found\n"+e.getMessage());
		}catch (MappingException e) {
			e.printStackTrace();
			System.err.println("Error occured while Java-XML mapping\n"+e);;
		} catch (Exception e) {
			System.err.println("Unexpected error occured\n"+e);
			e.printStackTrace();
		} finally {
			BootstrapSocketConnector.disconnect();
			System.exit(0);
		}
	}
	
	public static void checkWellFormed(String str)
	{
		try{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.parse(str);
			System.out.println("XML generated successfully!!");
		}
		catch (SAXException sax){
			System.err.println("XML generated but is not well-formed\n Check if there are any special characters" +
			"in the SID ErrorCode tables");
		}
		catch (IOException io){
			System.err.println("Special characters present in XML\n" +
					"Check if there are any special characters:"+io.getMessage());
		}
	}
}
