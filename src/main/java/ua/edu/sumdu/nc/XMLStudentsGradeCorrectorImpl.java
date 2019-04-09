package ua.edu.sumdu.nc;

import com.sun.org.apache.xpath.internal.NodeSet;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

public class XMLStudentsGradeCorrectorImpl implements XMLCorrector {

    private static final Logger LOGGER = Logger.getLogger(XMLStudentsGradeCorrectorImpl.class.getSimpleName());
    private final File file;
    private final File correctedFile;
    private final Document document;
    private boolean hasBeenChanged;

    public XMLStudentsGradeCorrectorImpl(File file, File correctedFile) {
        this.file = file;
        this.correctedFile = correctedFile;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            document = dbf.newDocumentBuilder().parse(file);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void correct() {
        NodeList students = getAllStudents();
        double tagAvg;
        double actualAvg;
        Element student;
        for (int i = 0; i < students.getLength(); i++) {
            student = (Element) students.item(i);
            tagAvg = getStudentAvg(student);
            actualAvg = countStudentActualAvg((Element) students.item(i));
            System.err.println("tag value = " + tagAvg + " actualAvg = " + actualAvg);
            if (Math.abs(tagAvg - actualAvg) >= 0.1) {
                System.err.println("i am going to set new value");
                setStudentAvg(student, actualAvg);
                hasBeenChanged = true;
            }
        }
        if (hasBeenChanged) {
            System.err.println("i am going to save");
            save();
        }
    }

    private NodeList getAllStudents() {
        try {
            return (NodeList) XPathFactory.newInstance()
                    .newXPath().evaluate("/group/student", document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private double countStudentActualAvg(Element student) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList subjects;
        try {
            subjects = (NodeList) xpath.evaluate(
                    "/group/student[@firstname = '" + student.getAttribute("firstname")
                            + "'][@lastname = '"+ student.getAttribute("lastname")
                            +"']/subject", document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        int total = 0;
        System.err.println("Amount of subjects = " + subjects.getLength());
        for (int i = 0; i < subjects.getLength(); i++) {
            try {
                System.err.println("subject mark = " + Integer.parseInt(subjects.item(i).getAttributes().getNamedItem("mark").getTextContent()));
                total += Integer.parseInt(subjects.item(i).getAttributes().getNamedItem("mark").getTextContent());
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid mark value", e);
            }
        }
        System.err.println("total = " + total);
        return ((double) total) / subjects.getLength();
    }

    private void setStudentAvg(Element student, double newAvg) {
        if (student.getElementsByTagName("average").getLength() == 0) {
            student.appendChild(document.createElement("average"));
        }
        (student.getElementsByTagName("average").item(0)).setTextContent(String.valueOf(newAvg));
    }

    private void save() {
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "group.dtd");
            StreamResult streamResult = new StreamResult(correctedFile);
            tr.transform(new DOMSource(document.getDocumentElement()), streamResult);
        } catch (TransformerException e) {
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                LOGGER.error(e);
            }
        }
    }

    private double getStudentAvg(Element student) {
        if (student.getElementsByTagName("average").getLength() == 0) {
            Element average =  document.createElement("average");
            average.setTextContent("-1");
            student.appendChild(average);
        }
        return Double.parseDouble(student.getElementsByTagName("average").item(0).getTextContent());
    }
}