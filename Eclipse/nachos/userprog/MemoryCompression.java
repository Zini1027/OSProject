package nachos.userprog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

public class MemoryCompression {

    public static byte[] compressZLIB(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        System.out.println("Original: " + data.length / 1024 + " Kb");
        System.out.println("Compressed: " + output.length / 1024 + " Kb");
        return output;
    }

    public static byte[] decompressZLIB(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        System.out.println("Original: " + data.length);
        System.out.println("Compressed: " + output.length);
        return output;
    }

    public static byte[] compressGZIP(byte[] data) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GZIPOutputStream zos = new GZIPOutputStream(baos);
        zos.write(data);
        zos.close();

        return baos.toByteArray();
    }

    public static byte[] uncompressGZIP(byte[] data) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        GZIPInputStream zis = new GZIPInputStream(bais);
        byte[] tmpBuffer = new byte[4096];
        int n;
        while ((n = zis.read(tmpBuffer)) >= 0)
            baos.write(tmpBuffer, 0, n);
        zis.close();

        return baos.toByteArray();
    }

}
