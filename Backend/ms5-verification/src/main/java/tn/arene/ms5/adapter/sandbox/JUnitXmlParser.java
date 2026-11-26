package tn.arene.ms5.adapter.sandbox;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tn.arene.ms5.domain.TestResult;
import tn.arene.ms5.domain.VerdictResult;

/** Parseur pur (aucune I/O réseau) d'un rapport JUnit XML standard. */
public final class JUnitXmlParser {

	private JUnitXmlParser() {
	}

	public static List<TestResult> parse(String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
			NodeList cases = document.getElementsByTagName("testcase");
			List<TestResult> results = new ArrayList<>();
			for (int i = 0; i < cases.getLength(); i++) {
				Element testcase = (Element) cases.item(i);
				String name = testcase.getAttribute("name");
				if (name.isBlank()) name = "test-" + i;
				long durationMs = Math.round(Double.parseDouble(
						testcase.getAttribute("time").isBlank() ? "0" : testcase.getAttribute("time")) * 1000);
				NodeList children = testcase.getChildNodes();
				String logs = "";
				boolean failed = false;
				for (int j = 0; j < children.getLength(); j++) {
					Node node = children.item(j);
					if (node.getNodeType() == Node.ELEMENT_NODE
							&& ("failure".equals(node.getNodeName()) || "error".equals(node.getNodeName()))) {
						failed = true;
						logs = node.getTextContent().trim();
					}
				}
				VerdictResult outcome = failed ? VerdictResult.FAIL : VerdictResult.PASS;
				results.add(new TestResult(name, outcome, durationMs, logs));
			}
			return results;
		} catch (Exception exception) {
			throw new IllegalArgumentException("Rapport JUnit XML invalide", exception);
		}
	}
}
