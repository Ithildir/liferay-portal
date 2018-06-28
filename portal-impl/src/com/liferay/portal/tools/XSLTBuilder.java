/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.tools;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.petra.xml.Dom4jUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.xml.SAXReaderFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXReader;

/**
 * @author Brian Wing Shun Chan
 */
public class XSLTBuilder {

	private static final String _FILE_NAME_XPATH_EXPRESSION = "//file-name";

	public static void main(String[] args) throws IOException {
		if (args.length == 2) {
			String xmls = null;

			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(System.in))) {

				xmls = bufferedReader.readLine();
			}

			new XSLTBuilder(StringUtil.split(xmls), args[0], args[1]);
		}
		else if (args.length == 3) {
			new XSLTBuilder(StringUtil.split(args[0]), args[1], args[2]);
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	public XSLTBuilder(String xml, String xsl, String html) {
		this(new String[] {xml}, xsl, html);
	}

	private Document _convertSPDXFile(String spdxFileName) throws Exception {
		Document document = DocumentHelper.createDocument();

		SAXReader saxReader = SAXReaderFactory.getSAXReader(null, false, false);

		Document spdxDocument = saxReader.read(new File(spdxFileName));

		Element rdfElement = spdxDocument.getRootElement();

		List<Element> packageElements = rdfElement.elements("package");

		for (Element packageElement : packageElements) {
			List<Element> fileElements = packageElement.elements("file");

			for (Element fileElement : fileElements) {
				String fileName = fileElement.elementText("filename");

				_getSPDXJarFileName(spdxFileName);
			}
		}

		return document;
	}

	private String _getSPDXJarFileName(String fileName) {
		int start = fileName.indexOf("/modules/");

		if (start == -1) {
			return null;
		}

		int end = fileName.indexOf("/src/main/");

		if (end == -1) {
			return null;
		}

		String moduleDirName = fileName.substring(start + 8, end);

		System.out.println(moduleDirName);

		return null;
	}

	private static final File _rootDir;

	static {
		File dir = new File(System.getProperty("user.dir"));

		while (dir != null) {
			File portalImplDir = new File(dir, "portal-impl");

			if (portalImplDir.isDirectory()) {
				break;
			}

			dir = dir.getParentFile();
		}

		_rootDir = dir;
	}

	public XSLTBuilder(String[] xmls, String xsl, String html) {
		try {
			System.setProperty("line.separator", StringPool.NEW_LINE);

			String prefix = html.substring(
				0, html.lastIndexOf(CharPool.PERIOD));

			List<Document> documents = new ArrayList<>();

			String spdxFileName = System.getProperty("spdx.file");

			if (Validator.isNotNull(spdxFileName)) {
				documents.add(_convertSPDXFile(spdxFileName));
			}

			Document document = _combineAndSortXMLs(
				xmls, documents, prefix + ".xsl");

			if (xmls.length > 1) {
				String completeXml = prefix + "-complete.xml";

				String completeContent = Dom4jUtil.toString(document);

				Files.write(
					Paths.get(completeXml),
					completeContent.getBytes(StandardCharsets.UTF_8));
			}

			TransformerFactory transformerFactory =
				TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer(
				new StreamSource(xsl));

			transformer.transform(
				new DocumentSource(document),
				new StreamResult(new FileOutputStream(html)));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Document _combineAndSortXMLs(
			String[] xmls, Iterable<Document> documents, String xsl)
		throws Exception {

		SAXReader saxReader = SAXReaderFactory.getSAXReader(null, false, false);

		Map<String, Element> elementMap = new TreeMap<>();

		for (String xml : xmls) {
			Document document = saxReader.read(new File(xml));

			_putElements(elementMap, document, _FILE_NAME_XPATH_EXPRESSION);
		}

		for (Document document : documents) {
			_putElements(elementMap, document, _FILE_NAME_XPATH_EXPRESSION);
		}

		Document document = DocumentHelper.createDocument();

		File xslFile = new File(xsl);

		if (xslFile.exists()) {
			Map<String, String> args = new HashMap<>();

			args.put("href", xslFile.getName());
			args.put("type", "text/xsl");

			document.addProcessingInstruction("xml-stylesheet", args);
		}

		Element versionsElement = document.addElement("versions");

		Element versionElement = versionsElement.addElement("version");

		Element librariesElement = versionElement.addElement("libraries");

		for (Element element : elementMap.values()) {
			librariesElement.add(element.detach());
		}

		return document;
	}

	private void _putElements(
		Map<String, Element> elementMap, Document document,
		String xpathExpression) {

		List<Node> nodes = document.selectNodes(xpathExpression);

		for (Node node : nodes) {
			elementMap.put(node.getText(), node.getParent());
		}
	}

}