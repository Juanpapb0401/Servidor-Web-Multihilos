import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public final class ServidorWeb {
    public static void main(String argv[]) throws Exception {
        int puerto = 6789;

        ServerSocket socketEscucha = new ServerSocket(puerto);

        while (true) {
            Socket socketConexion = socketEscucha.accept();
            SolicitudHttp solicitud = new SolicitudHttp(socketConexion);
            Thread hilo = new Thread(solicitud);
            hilo.start();
        }
    }
}

final class SolicitudHttp implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public SolicitudHttp(Socket socket) throws Exception {
        this.socket = socket;
    }

    public void run() {
        try {
            proceseSolicitud();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void proceseSolicitud() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String linea;

        String nombreArchivo = "";

        String method = "";

        while ((linea = in.readLine()) != null && !linea.isEmpty()) {

            System.out.println(linea);

            StringTokenizer partesLinea = new StringTokenizer(linea);

            method = partesLinea.nextToken();
            if (method.equals("GET")) {
                nombreArchivo = "." + partesLinea.nextToken();
                System.out.println("Nombre del archivo: " + nombreArchivo);
                break;
            }
        }

        InputStream inputStream = ClassLoader.getSystemResourceAsStream(nombreArchivo);
        var out = new BufferedOutputStream(socket.getOutputStream());

        if (inputStream != null) {
            File file = new File(ClassLoader.getSystemResource(nombreArchivo).toURI());
            long filesize = file.length();

            String contentType = contentType(nombreArchivo);
            String lineaDeEstado = "HTTP/1.0 200 OK" + CRLF;
            String lineaHeader = "Content-Type: " + contentType + "; charset=UTF-8" + CRLF +
                    "Content-Length: " + filesize + CRLF + CRLF;

            enviarString(lineaDeEstado, out);
            enviarString(lineaHeader, out);
            enviarBytes(inputStream, out);
        } else {
            InputStream errorStream = ClassLoader.getSystemResourceAsStream("404.html");
            String lineaDeEstado = "HTTP/1.0 404 Not Found" + CRLF;
            String lineaHeader = "Content-Type: text/html; charset=UTF-8" + CRLF;
            enviarString(lineaDeEstado, out);
            enviarString(lineaHeader, out);
            enviarString(CRLF, out);

            if (errorStream != null) {
                File file = new File(ClassLoader.getSystemResource("404.html").toURI());
                long filesize = file.length();
                enviarString("Content-Length: " + filesize + CRLF + CRLF, out);
                enviarBytes(errorStream, out);
            } else {
                String cuerpoMensaje = "<html><body><h1>404 Not Found sin Archivo</h1></body></html>";
                enviarString("Content-Length: " + cuerpoMensaje.length() + CRLF + CRLF, out);
                enviarString(cuerpoMensaje, out);
            }
        }
        out.flush();
        in.close();
        out.close();
        socket.close();
    }

    private static void enviarString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private static void enviarBytes(InputStream fis, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes;
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String nombreArchivo) {
        if (nombreArchivo.endsWith(".htm") || nombreArchivo.endsWith(".html")) {
            return "text/html";
        }
        if (nombreArchivo.endsWith(".gif")) {
            return "image/gif";
        }
        if (nombreArchivo.endsWith(".jpeg") || nombreArchivo.endsWith(".jpg")) {
            return "image/jpeg";
        }
        if (nombreArchivo.endsWith(".png")) {
            return "image/png";
        }
        if (nombreArchivo.endsWith(".css")) {
            return "text/css";
        }
        if (nombreArchivo.endsWith(".js")) {
            return "application/javascript";
        }
        return "application/octet-stream";
    }
}