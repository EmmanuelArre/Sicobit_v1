package utng.gtid234.jeae.modelo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageIO;

public final class PdfSimple {

    private static final float ANCHO_PAGINA = 792f;   // horizontal (11in)
    private static final float ALTO_PAGINA = 612f;    // horizontal (8.5in)
    private static final float MARGEN = 30f;
    private static final float ANCHO_TABLA = ANCHO_PAGINA - (2 * MARGEN);

    private static final float ALTO_FILA = 16f;
    private static final float ALTO_ENCABEZADO_TABLA = 18f;
    private static final float TAMANO_FUENTE = 8.5f;
    private static final float TAMANO_FUENTE_ENCABEZADO = 9f;
    private static final float ANCHO_CARACTER_APROX = TAMANO_FUENTE * 0.56f;

    // Alto reservado en la primera página para el logo + título + línea divisoria
    private static final float ALTO_BLOQUE_TITULO = 92f;
    private static final float ALTO_LOGO = 46f;

    // Azul corporativo #1A56E8 y gris de banda alterna, en escala 0-1
    private static final String COLOR_ENCABEZADO = "0.102 0.337 0.910";
    private static final String COLOR_BANDA = "0.953 0.965 0.992";
    private static final String COLOR_BORDE = "0.851 0.863 0.882";
    private static final String COLOR_TEXTO_OSCURO = "0.102 0.102 0.102";

    private static final String RUTA_LOGO = "/utng/gtid234/jeae/images/logoutng.png";

    private PdfSimple() {
    }

  
    public static void generarTabla(File destino, String titulo, String subtitulo,
                                     String[] encabezados, List<String[]> filas) throws IOException {

        float[] anchos = calcularAnchosColumnas(encabezados, filas);

        List<List<String[]>> paginas = paginar(filas, calcularFilasPorPagina());

        if (paginas.isEmpty()) {
            paginas.add(new ArrayList<>());
        }

        LogoPdf logo = cargarLogo();

        List<byte[]> streams = new ArrayList<>();

        for (int p = 0; p < paginas.size(); p++) {

            boolean primeraPagina = (p == 0);
            String contenido = construirContenidoPagina(
                    primeraPagina ? titulo : null,
                    primeraPagina ? subtitulo : null,
                    primeraPagina ? logo : null,
                    encabezados, anchos, paginas.get(p),
                    p + 1, paginas.size(), filas.size()
            );

            streams.add(contenido.getBytes(StandardCharsets.ISO_8859_1));
        }

        escribirPdf(destino, streams, logo);
    }

    private static int calcularFilasPorPagina() {

        float alturaDisponible = ALTO_PAGINA - (2 * MARGEN) - ALTO_BLOQUE_TITULO
                - ALTO_ENCABEZADO_TABLA - 10f;

        return Math.max(5, (int) (alturaDisponible / ALTO_FILA));
    }

    private static List<List<String[]>> paginar(List<String[]> filas, int filasPorPagina) {

        List<List<String[]>> paginas = new ArrayList<>();
        List<String[]> actual = new ArrayList<>();

        for (String[] fila : filas) {

            actual.add(fila);

            if (actual.size() >= filasPorPagina) {
                paginas.add(actual);
                actual = new ArrayList<>();
            }
        }

        if (!actual.isEmpty()) {
            paginas.add(actual);
        }

        return paginas;
    }


    private static float[] calcularAnchosColumnas(String[] encabezados, List<String[]> filas) {

        int n = encabezados.length;
        float[] anchoMaxContenido = new float[n];

        for (int i = 0; i < n; i++) {
            anchoMaxContenido[i] = encabezados[i].length();
        }

        int muestra = Math.min(filas.size(), 300);

        for (int f = 0; f < muestra; f++) {

            String[] fila = filas.get(f);

            for (int i = 0; i < n && i < fila.length; i++) {

                String valor = fila[i] == null ? "" : fila[i];

                if (valor.length() > anchoMaxContenido[i]) {
                    anchoMaxContenido[i] = valor.length();
                }
            }
        }

        float sumaPesos = 0;

        for (float peso : anchoMaxContenido) {
            sumaPesos += Math.min(peso, 55);
        }

        float[] anchos = new float[n];

        for (int i = 0; i < n; i++) {

            float peso = Math.min(anchoMaxContenido[i], 55);
            anchos[i] = (peso / sumaPesos) * ANCHO_TABLA;

            if (anchos[i] < 48f) {
                anchos[i] = 48f;
            }
        }

        float sumaFinal = 0;
        for (float a : anchos) {
            sumaFinal += a;
        }

        float factor = ANCHO_TABLA / sumaFinal;

        for (int i = 0; i < n; i++) {
            anchos[i] *= factor;
        }

        return anchos;
    }

    private static String construirContenidoPagina(String titulo, String subtitulo, LogoPdf logo,
                                                    String[] encabezados, float[] anchos,
                                                    List<String[]> filas, int numPagina, int totalPaginas,
                                                    int totalRegistros) {

        StringBuilder c = new StringBuilder();
        float y = ALTO_PAGINA - MARGEN;

        if (titulo != null) {

            float xTexto = MARGEN;

            if (logo != null) {

                float anchoLogo = ALTO_LOGO * (logo.ancho / (float) logo.alto);
                float yLogo = y - ALTO_LOGO;

                c.append("q ").append(fmt(anchoLogo)).append(" 0 0 ").append(fmt(ALTO_LOGO))
                        .append(" ").append(fmt(MARGEN)).append(" ").append(fmt(yLogo))
                        .append(" cm /Im1 Do Q\n");

                xTexto = MARGEN + anchoLogo + 16;
            }

            c.append(rg(COLOR_ENCABEZADO));
            c.append(textoEn(xTexto, y - 20, 17, escaparPdf(titulo)));

            c.append(rg("0.42 0.46 0.51"));
            c.append(textoEn(xTexto, y - 38, 9.5f, escaparPdf(subtitulo)));

            c.append(rg("0.42 0.46 0.51"));
            c.append(textoEn(xTexto, y - 52, 8.5f, "Universidad Tecnologica del Norte de Guanajuato"));

            y -= (ALTO_LOGO + 8);

            c.append(RG(COLOR_BORDE));
            c.append("1 w\n");
            c.append(linea(MARGEN, y, MARGEN + ANCHO_TABLA, y));

            y -= 14;
        }

        float xTabla = MARGEN;
        float yTablaTop = y;

        c.append(rg(COLOR_ENCABEZADO));
        c.append(rect(xTabla, yTablaTop - ALTO_ENCABEZADO_TABLA, ANCHO_TABLA, ALTO_ENCABEZADO_TABLA, true, false));

        c.append(rg("1 1 1"));
        float xCursor = xTabla;

        for (int i = 0; i < encabezados.length; i++) {

            String texto = truncar(encabezados[i], anchos[i]);
            c.append(textoEn(xCursor + 4, yTablaTop - ALTO_ENCABEZADO_TABLA + 5, TAMANO_FUENTE_ENCABEZADO, escaparPdf(texto)));
            xCursor += anchos[i];
        }

        float yFila = yTablaTop - ALTO_ENCABEZADO_TABLA;

        int indice = 0;

        for (String[] fila : filas) {

            float yFilaBottom = yFila - ALTO_FILA;

            if (indice % 2 == 1) {
                c.append(rg(COLOR_BANDA));
                c.append(rect(xTabla, yFilaBottom, ANCHO_TABLA, ALTO_FILA, true, false));
            }

            c.append(rg(COLOR_TEXTO_OSCURO));
            xCursor = xTabla;

            for (int i = 0; i < anchos.length; i++) {

                String valor = (i < fila.length && fila[i] != null) ? fila[i] : "";
                String texto = truncar(valor, anchos[i]);
                c.append(textoEn(xCursor + 4, yFilaBottom + 5, TAMANO_FUENTE, escaparPdf(texto)));
                xCursor += anchos[i];
            }

            yFila = yFilaBottom;
            indice++;
        }

        float yTablaBottom = yFila;

        c.append(RG(COLOR_BORDE));
        c.append("0.6 w\n");

        c.append(linea(xTabla, yTablaTop, xTabla + ANCHO_TABLA, yTablaTop));
        c.append(linea(xTabla, yTablaTop - ALTO_ENCABEZADO_TABLA, xTabla + ANCHO_TABLA, yTablaTop - ALTO_ENCABEZADO_TABLA));

        float yLinea = yTablaTop - ALTO_ENCABEZADO_TABLA;
        for (int i = 0; i < filas.size(); i++) {
            yLinea -= ALTO_FILA;
            c.append(linea(xTabla, yLinea, xTabla + ANCHO_TABLA, yLinea));
        }

        xCursor = xTabla;
        c.append(linea(xCursor, yTablaTop, xCursor, yTablaBottom));

        for (float ancho : anchos) {
            xCursor += ancho;
            c.append(linea(xCursor, yTablaTop, xCursor, yTablaBottom));
        }

        c.append(rg("0.42 0.46 0.51"));
        String pie = "Sistema de Control de Laboratorios Extraclase   |   Página " + numPagina
                + " de " + totalPaginas + "       " + totalRegistros + " registro(s) en total";
        c.append(textoEn(MARGEN, MARGEN - 10, 8, escaparPdf(pie)));

        return c.toString();
    }

    private static String truncar(String texto, float anchoDisponible) {

        if (texto == null) {
            return "";
        }

        int maxChars = Math.max(1, (int) ((anchoDisponible - 8) / ANCHO_CARACTER_APROX));

        if (texto.length() <= maxChars) {
            return texto;
        }

        if (maxChars <= 3) {
            return texto.substring(0, maxChars);
        }

        return texto.substring(0, maxChars - 1) + "...";
    }

    private static String textoEn(float x, float y, float tamanoFuente, String textoEscapado) {
        return "BT /F1 " + fmt(tamanoFuente) + " Tf " + fmt(x) + " " + fmt(y) + " Td (" + textoEscapado + ") Tj ET\n";
    }

    private static String rect(float x, float y, float ancho, float alto, boolean relleno, boolean borde) {

        StringBuilder r = new StringBuilder();
        r.append(fmt(x)).append(" ").append(fmt(y)).append(" ").append(fmt(ancho)).append(" ").append(fmt(alto)).append(" re ");

        if (relleno && borde) {
            r.append("B\n");
        } else if (relleno) {
            r.append("f\n");
        } else {
            r.append("S\n");
        }

        return r.toString();
    }

    private static String linea(float x1, float y1, float x2, float y2) {
        return fmt(x1) + " " + fmt(y1) + " m " + fmt(x2) + " " + fmt(y2) + " l S\n";
    }

    private static String rg(String colorRgb) {
        return colorRgb + " rg\n";
    }

    private static String RG(String colorRgb) {
        return colorRgb + " RG\n";
    }

    private static String fmt(float valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    private static String escaparPdf(String texto) {

        if (texto == null) {
            return "";
        }

        StringBuilder limpio = new StringBuilder();

        for (char ch : texto.toCharArray()) {

            if (ch == '(' || ch == ')' || ch == '\\') {
                limpio.append('\\').append(ch);
            } else if (ch < 32 || ch > 255) {
                limpio.append('?');
            } else {
                limpio.append(ch);
            }
        }

        return limpio.toString();
    }

    private static final class LogoPdf {
        byte[] datosComprimidos;
        int ancho;
        int alto;
    }

    private static LogoPdf cargarLogo() {

        try (InputStream is = PdfSimple.class.getResourceAsStream(RUTA_LOGO)) {

            if (is == null) {
                return null;
            }

            BufferedImage imagen = ImageIO.read(is);

            if (imagen == null) {
                return null;
            }

            int ancho = imagen.getWidth();
            int alto = imagen.getHeight();

            byte[] rgb = new byte[ancho * alto * 3];
            int idx = 0;

            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {

                    int argb = imagen.getRGB(x, y);
                    int alfa = (argb >>> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;

                    // Aplana la transparencia sobre fondo blanco (el PDF no
                    // maneja aquí canal alfa para mantenerlo simple).
                    r = (r * alfa + 255 * (255 - alfa)) / 255;
                    g = (g * alfa + 255 * (255 - alfa)) / 255;
                    b = (b * alfa + 255 * (255 - alfa)) / 255;

                    rgb[idx++] = (byte) r;
                    rgb[idx++] = (byte) g;
                    rgb[idx++] = (byte) b;
                }
            }

            ByteArrayOutputStream comprimido = new ByteArrayOutputStream();
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);

            try (DeflaterOutputStream dos = new DeflaterOutputStream(comprimido, deflater)) {
                dos.write(rgb);
            }

            deflater.end();

            LogoPdf logo = new LogoPdf();
            logo.datosComprimidos = comprimido.toByteArray();
            logo.ancho = ancho;
            logo.alto = alto;

            return logo;

        } catch (Exception e) {

            // Si algo falla al leer/decodificar el logo, el PDF se genera
            // igual mostrando solo el texto del encabezado (sin tronar).
            return null;
        }
    }

    private static void escribirPdf(File destino, List<byte[]> streamsPorPagina, LogoPdf logo) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();

        escribir(buffer, "%PDF-1.4\n");

        offsets.add(buffer.size());
        escribir(buffer, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        int numPaginas = streamsPorPagina.size();
        StringBuilder kids = new StringBuilder();

        boolean hayLogo = logo != null;
        int idImagen = 4;
        int idObjeto = hayLogo ? 5 : 4;

        for (int i = 0; i < numPaginas; i++) {
            kids.append(idObjeto + i * 2).append(" 0 R ");
        }

        offsets.add(buffer.size());
        escribir(buffer, "2 0 obj\n<< /Type /Pages /Kids [" + kids.toString().trim()
                + "] /Count " + numPaginas + " >>\nendobj\n");

        // /Encoding /WinAnsiEncoding es indispensable: sin ella, un lector
        // de PDF interpreta los bytes 128-255 de la fuente base Helvetica
        // con StandardEncoding (no con Latin-1/CP1252), así que las
        // vocales acentuadas y la ñ (que aquí se escriben como bytes
        // ISO-8859-1, muy cercano a WinAnsi en ese rango) se ven como
        // símbolos aunque el PDF se genere sin ningún error.
        offsets.add(buffer.size());
        escribir(buffer, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n");

        if (hayLogo) {
            offsets.add(buffer.size());
            escribir(buffer, idImagen + " 0 obj\n<< /Type /XObject /Subtype /Image /Width " + logo.ancho
                    + " /Height " + logo.alto + " /ColorSpace /DeviceRGB /BitsPerComponent 8 "
                    + "/Filter /FlateDecode /Length " + logo.datosComprimidos.length + " >>\nstream\n");
            buffer.write(logo.datosComprimidos);
            escribir(buffer, "\nendstream\nendobj\n");
        }

        String recursosConLogo = hayLogo
                ? "<< /Font << /F1 3 0 R >> /XObject << /Im1 " + idImagen + " 0 R >> >>"
                : "<< /Font << /F1 3 0 R >> >>";

        String recursosSinLogo = "<< /Font << /F1 3 0 R >> >>";

        for (int p = 0; p < streamsPorPagina.size(); p++) {

            byte[] contenidoBytes = streamsPorPagina.get(p);

            int idPagina = idObjeto;
            int idContenido = idObjeto + 1;

            // El logo solo se dibuja en la primera página, así que solo esa
            // página necesita el XObject en sus recursos.
            String recursos = (p == 0 && hayLogo) ? recursosConLogo : recursosSinLogo;

            offsets.add(buffer.size());
            escribir(buffer, idPagina + " 0 obj\n<< /Type /Page /Parent 2 0 R /Resources "
                    + recursos + " /MediaBox [0 0 " + (int) ANCHO_PAGINA + " " + (int) ALTO_PAGINA
                    + "] /Contents " + idContenido + " 0 R >>\nendobj\n");

            offsets.add(buffer.size());
            escribir(buffer, idContenido + " 0 obj\n<< /Length " + contenidoBytes.length + " >>\nstream\n");
            buffer.write(contenidoBytes);
            escribir(buffer, "\nendstream\nendobj\n");

            idObjeto += 2;
        }

        int xrefStart = buffer.size();
        int totalObjetos = offsets.size() + 1;

        escribir(buffer, "xref\n0 " + totalObjetos + "\n0000000000 65535 f \n");

        for (int offset : offsets) {
            escribir(buffer, String.format(Locale.US, "%010d 00000 n \n", offset));
        }

        escribir(buffer, "trailer\n<< /Size " + totalObjetos + " /Root 1 0 R >>\nstartxref\n"
                + xrefStart + "\n%%EOF");

        try (FileOutputStream fos = new FileOutputStream(destino)) {
            buffer.writeTo(fos);
        }
    }

    private static void escribir(ByteArrayOutputStream buffer, String texto) throws IOException {
        buffer.write(texto.getBytes(StandardCharsets.ISO_8859_1));
    }
}
