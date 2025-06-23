package com.lessonsync.app.util

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class MusicXmlEditor(xmlString: String) {

    private val document: Document

    init {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        document = builder.parse(InputSource(StringReader(xmlString)))
        document.documentElement.normalize()
    }

    fun addTextAnnotation(
        measureNumber: Int,
        text: String,
        color: String = "#D83F31", // 기본 붉은색
        placement: String = "above"
    ) {
        val measures = document.getElementsByTagName("measure")
        for (i in 0 until measures.length) {
            val measureNode = measures.item(i)
            if (measureNode is Element && measureNode.getAttribute("number") == measureNumber.toString()) {
                val directionElement = document.createElement("direction").apply {
                    setAttribute("placement", placement)
                }
                val directionTypeElement = document.createElement("direction-type")
                val wordsElement = document.createElement("words").apply {
                    textContent = text
                    setAttribute("font-weight", "bold")
                    setAttribute("font-size", "10")
                    setAttribute("color", color)
                }
                directionTypeElement.appendChild(wordsElement)
                directionElement.appendChild(directionTypeElement)
                measureNode.insertBefore(directionElement, measureNode.firstChild)
                break
            }
        }
    }

    fun getUpdatedXml(): String {
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        val writer = StringWriter()
        transformer.transform(DOMSource(document), StreamResult(writer))
        return writer.toString()
    }
}