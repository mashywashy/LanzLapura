package org.yohi.lapura_evaluation_system;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CurriculumParser {
    public static List<String> parseSubjects(String program) throws Exception {
        List<String> subjects = new ArrayList<>();
        String resourcePath = "/org/yohi/lapura_evaluation_system/" + program.toLowerCase() + "_curriculum.xml";

        try (InputStream inputStream = CurriculumParser.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Curriculum file not found: " + resourcePath);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            NodeList subjectNodes = doc.getElementsByTagName("subject");
            for (int i = 0; i < subjectNodes.getLength(); i++) {
                Node node = subjectNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    subjects.add(element.getAttribute("subjectCode"));
                }
            }
        } catch (IOException | SAXException e) {
            throw new Exception("Error parsing curriculum: " + e.getMessage());
        }
        return subjects;
    }
}
