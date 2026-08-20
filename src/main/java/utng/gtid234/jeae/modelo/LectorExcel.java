package utng.gtid234.jeae.modelo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Lee la PRIMERA hoja de un archivo .xlsx y regresa su contenido como
 * filas de texto, sin depender de ninguna librería externa (Apache POI,
 * etc.): un .xlsx es en realidad un .zip que contiene XML, así que basta
 * con leer "xl/sharedStrings.xml" (los textos, guardados una sola vez
 * y referenciados por índice) y "xl/worksheets/sheet1.xml" (las celdas).
 *
 * No soporta fórmulas, formatos de fecha con estilo, ni fórmulas con
 * caché de resultado en celdas de tipo distinto a texto/número: para el
 * caso de uso (columnas simples de matrícula, nombre y grupo) es
 * suficiente.
 */
public final class LectorExcel {

    private LectorExcel() {
    }

    public static List<String[]> leerPrimeraHoja(File archivo) throws IOException {

        try (ZipFile zip = new ZipFile(archivo)) {

            List<String> textosCompartidos = leerSharedStrings(zip);

            ZipEntry hoja = obtenerPrimeraHoja(zip);

            if (hoja == null) {
                throw new IOException("El archivo no contiene ninguna hoja de cálculo reconocible (xl/worksheets/sheetN.xml).");
            }

            return leerHoja(zip, hoja, textosCompartidos);

        } catch (Exception e) {

            if (e instanceof IOException io) {
                throw io;
            }

            throw new IOException("No se pudo leer el archivo .xlsx: " + e.getMessage(), e);
        }
    }

    private static ZipEntry obtenerPrimeraHoja(ZipFile zip) {

        // Primero intenta sheet1.xml (el caso normal); si no existe, toma
        // la primera hoja que encuentre en la carpeta de hojas.
        ZipEntry directa = zip.getEntry("xl/worksheets/sheet1.xml");

        if (directa != null) {
            return directa;
        }

        java.util.Enumeration<? extends ZipEntry> entradas = zip.entries();
        ZipEntry primera = null;

        while (entradas.hasMoreElements()) {

            ZipEntry e = entradas.nextElement();

            if (e.getName().matches("xl/worksheets/sheet\\d+\\.xml")) {
                if (primera == null || e.getName().compareTo(primera.getName()) < 0) {
                    primera = e;
                }
            }
        }

        return primera;
    }

    private static List<String> leerSharedStrings(ZipFile zip) throws Exception {

        List<String> lista = new ArrayList<>();

        ZipEntry entrada = zip.getEntry("xl/sharedStrings.xml");

        if (entrada == null) {
            return lista; // el archivo puede no tener textos compartidos (solo números)
        }

        Document doc = parsearXml(zip.getInputStream(entrada));

        NodeList nodosSi = doc.getElementsByTagName("si");

        for (int i = 0; i < nodosSi.getLength(); i++) {

            Element si = (Element) nodosSi.item(i);

            // Un <si> puede tener un <t> directo, o varios <r><t>...</t></r>
            // (texto con formato mixto, "rich text"): se concatenan todos.
            StringBuilder texto = new StringBuilder();

            NodeList nodosT = si.getElementsByTagName("t");

            for (int j = 0; j < nodosT.getLength(); j++) {
                texto.append(nodosT.item(j).getTextContent());
            }

            lista.add(texto.toString());
        }

        return lista;
    }

    private static List<String[]> leerHoja(ZipFile zip, ZipEntry hoja, List<String> textosCompartidos) throws Exception {

        Document doc = parsearXml(zip.getInputStream(hoja));

        NodeList nodosFila = doc.getElementsByTagName("row");

        // Mapa ordenado por número de fila real del Excel, para respetar
        // filas vacías intermedias tal como las vería un humano.
        Map<Integer, Map<Integer, String>> filas = new TreeMap<>();
        int maxColumna = 0;

        for (int i = 0; i < nodosFila.getLength(); i++) {

            Element fila = (Element) nodosFila.item(i);

            int numFila;
            try {
                numFila = Integer.parseInt(fila.getAttribute("r"));
            } catch (NumberFormatException e) {
                numFila = i + 1; // respaldo si la fila no trae el atributo "r"
            }

            Map<Integer, String> celdas = new HashMap<>();

            NodeList nodosCelda = fila.getElementsByTagName("c");

            for (int j = 0; j < nodosCelda.getLength(); j++) {

                Element celda = (Element) nodosCelda.item(j);

                String refCelda = celda.getAttribute("r"); // ej. "B7"
                int columna = columnaDesdeReferencia(refCelda);

                if (columna < 0) {
                    continue; // celda sin referencia de columna reconocible; se ignora
                }

                String tipo = celda.getAttribute("t"); // "s" = shared string, "" = número, "str" = fórmula-texto, "inlineStr"
                String valor = valorDeCelda(celda, tipo, textosCompartidos);

                celdas.put(columna, valor);
                maxColumna = Math.max(maxColumna, columna);
            }

            filas.put(numFila, celdas);
        }

        List<String[]> resultado = new ArrayList<>();

        for (Map<Integer, String> celdas : filas.values()) {

            String[] arregloFila = new String[maxColumna + 1];

            for (Map.Entry<Integer, String> e : celdas.entrySet()) {
                arregloFila[e.getKey()] = e.getValue();
            }

            resultado.add(arregloFila);
        }

        return resultado;
    }

    private static String valorDeCelda(Element celda, String tipo, List<String> textosCompartidos) {

        if ("inlineStr".equals(tipo)) {

            NodeList nodosT = celda.getElementsByTagName("t");
            StringBuilder texto = new StringBuilder();

            for (int i = 0; i < nodosT.getLength(); i++) {
                texto.append(nodosT.item(i).getTextContent());
            }

            return texto.toString();
        }

        NodeList nodosV = celda.getElementsByTagName("v");

        if (nodosV.getLength() == 0) {
            return "";
        }

        String valorCrudo = nodosV.item(0).getTextContent();

        if ("s".equals(tipo)) {

            try {

                int indice = Integer.parseInt(valorCrudo);

                if (indice >= 0 && indice < textosCompartidos.size()) {
                    return textosCompartidos.get(indice);
                }

            } catch (NumberFormatException ignorado) {
                // se cae al valor crudo si el índice no es numérico
            }
        }

        return valorCrudo;
    }

    // Convierte una referencia de celda tipo "C7" en el índice de columna
    // (A=0, B=1, C=2, ..., Z=25, AA=26, ...).
    private static int columnaDesdeReferencia(String referencia) {

        int columna = 0;

        for (char ch : referencia.toCharArray()) {

            if (!Character.isLetter(ch)) {
                break;
            }

            columna = columna * 26 + (Character.toUpperCase(ch) - 'A' + 1);
        }

        return columna - 1;
    }

    private static Document parsearXml(InputStream is) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Configuración segura: no resuelve entidades externas ni DTDs
        // (evita ataques XXE al abrir un .xlsx manipulado).
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(is);
    }
}
