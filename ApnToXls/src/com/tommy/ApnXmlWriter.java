package com.tommy;

import static util.MyUtil.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import util.Log;

public class ApnXmlWriter implements IApnWriter {
	private static final String TAG = "ApnXmlWriter";

	@Override
	public boolean writeApns(ArrayList<ApnInfo> apnInfoList,
			String apnDirPath, int fileType) {
		if (isEmptyList(apnInfoList) || !isDirectory(apnDirPath)) {
			Log.d(TAG, "apnInfoList is empty or apnDirPath is not dir : " + apnDirPath);
			return false;
		}
		Document document = DocumentHelper.createDocument();
		addHeadComments(document);		
		Element rootElement = createApnRootElement(document);
		addApnInfoListToElement(apnInfoList, rootElement);
		try {
			writeApnToXml(document, apnDirPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public boolean writeApnsForGroup(ArrayList<ApnGroup> apnGroupList,
			String apnDirPath, int fileType) {
		if (isEmptyList(apnGroupList) || !isDirectory(apnDirPath)) {
			Log.d(TAG, "apnGroupList is empty or apnDirPath is not dir : " + apnDirPath);
			return false;
		}
		Log.d(TAG, "apngroupList: " + apnGroupList);
		Document document = DocumentHelper.createDocument();
		addHeadComments(document);		
		Element rootElement = createApnRootElement(document);
		addApnGroupListToElement(apnGroupList, rootElement);
		try {
			writeApnToXml(document, apnDirPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private void addApnInfoListToElement(ArrayList<ApnInfo> apnInfoList, Element element) {
		for (ApnInfo apnInfo : apnInfoList) {
			Element apnElement = element.addElement("apn");
			apnInfo.addApnElementAttribute(apnElement);
		}
	}

	private void addApnGroupListToElement(ArrayList<ApnGroup> apnGroupList,
			Element rootElement) {
		for (ApnGroup apnGroup : apnGroupList) {
			rootElement.addComment(" " + apnGroup.getName() + "");
			addApnInfoListToElement(apnGroup.getApnInfoList(), rootElement);
			rootElement.addComment(" " + apnGroup.getName() + " end ");
		}		
	}
	
	private void addHeadComments(Document document) {
		document.addComment(ApnWriter.COMMENT_HEAD1);
		document.addComment(ApnWriter.COMMENT_HEAD2);
		document.addComment(ApnWriter.COMMENT_HEAD3);
	}
	
	private Element createApnRootElement(Document document) {
		return document.addElement("apns").addAttribute("version", "8");
	}
	
	@Override
	public boolean addApnNodesToExistFlie(ArrayList<ApnInfo> apnInfoList,
			String apnFilePath) {
		if (isEmptyList(apnInfoList) || !isLegalXMLFile(apnFilePath)) {
			Log.d(TAG, "addApnNodesToExistXml():apnInfoList is null : " + (apnInfoList == null));
			Log.d(TAG, "addApnNodesToExistXml():apnFilePath" + apnFilePath);
			return false;
		}
		Document document = getDocument(apnFilePath);
		if (document == null) return false;
		Element rootElement = document.getRootElement();
		if(!rootElement.getName().equals("apns")) {
			Log.d(TAG, "root name is not apns! root name: " + rootElement.getName());
			return false;
		}
		Log.d(TAG, "root size" + rootElement.elements().size());
		Log.d(TAG, "apnInfoList size" + apnInfoList.size());
		addApnInfoListToElement(apnInfoList, rootElement);
		Log.d(TAG, "root size" + rootElement.elements().size());
		try {
			writeApnToXml(document, apnFilePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}		
		return true;
	}
	
	@Override
	public boolean addApnGroupToExistFile(ArrayList<ApnGroup> apnGroupList,
			String apnFilePath) {
		if (isEmptyList(apnGroupList) || !isLegalXMLFile(apnFilePath)) {
			Log.d(TAG, "addApnNodesToExistXml():apnGroupList is null : " + (apnGroupList == null));
			Log.d(TAG, "addApnNodesToExistXml():apnFilePath" + apnFilePath);
			return false;
		}
		ApnReader apnReader = new ApnReader();
		Document document = getDocument(apnFilePath);
		if (document == null) return false;
		Element rootElement = document.getRootElement();
		if(!rootElement.getName().equals("apns")) {
			Log.d(TAG, "root name is not apns! root name: " + rootElement.getName());
			return false;
		}
		addApnGroupListToElement(apnGroupList, rootElement);
		try {
			writeApnToXml(document, apnFilePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}		
		return true;
	}
	
	public void writeApnToXml(Document document, String apnFilePath) throws Exception {  			
        // 紧凑的格式  
//		OutputFormat format = OutputFormat.createCompactFormat();  
        OutputFormat format = OutputFormat.createPrettyPrint();  
        format.setIndent(true); 		//设置是否缩进
        format.setIndentSize(4);		//将缩进设置为四个空格
        format.setEncoding("utf-8");  
        //format.setIndent(" "); 		//以空格方式实现缩进
         
        format.setNewlines(true); //设置是否换行
        // 创建XMLWriter对象,指定了写出文件及编码格式  
        // XMLWriter writer = new XMLWriter(new FileWriter(new  
        // File("src//a.xml")),format);  
        String apnFileDir = null;
        if (isFile(apnFilePath) && isExists(apnFilePath)) {
        	apnFileDir = apnFilePath.substring(0, apnFilePath.lastIndexOf('/') + 1);
        } else if (isDirectory(apnFilePath)) {
        	apnFileDir = apnFilePath;
        }
        Log.d(TAG, "apnFileDir: " + apnFileDir);
        if (apnFileDir == null) {
        	return;
        }
        XMLWriter writer = new XMLWriter(new OutputStreamWriter(
        		 new FileOutputStream(new File(apnFileDir + "apn-conf-new.xml")), "UTF-8"), format);  
        writer.write(document);			// 写入
        writer.flush();  				// 立即写入 
        writer.close();  				// 关闭操作 
    }
	
	private Document getDocument(String apnFilePath) {
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			document = saxReader.read(new File(apnFilePath));
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return document;
	}

}
