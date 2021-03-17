package com.victory.scrapy4j.core.component.selectors;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.util.List;

public class XMLSelector implements Selector {
    private Document document;
    private String xpath;

    public XMLSelector(Object body, String xpath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(false);
            factory.setCoalescing(false);
            factory.setExpandEntityReferences(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            this.xpath = xpath;
            document = builder.parse(new InputSource(new ByteArrayInputStream(body.toString().getBytes("utf-8"))));
        } catch (Exception ex) {

        }
    }

    @Override
    public Object get() {
        Object res = null;
        try {
            res = XPathFactory.newInstance().newXPath().evaluate(this.xpath, document, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
        }
        return res;
    }

    @Override
    public List<Object> getAll() {
        return null;
    }
}
