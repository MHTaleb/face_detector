/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converter;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author taleb
 */
public class ImageConverter {

    public static File toFile(String imageName ,Long id, byte[] data, String extension) throws IOException {
        if (!Files.exists(Paths.get("c:/trainingDir"))) {
            Files.createDirectory(Paths.get("c:/trainingDir"));
        }
        if (!Files.exists(Paths.get("c:/trainingDir/" + id + "-"+imageName + "." + extension))) {
            Files.createFile(Paths.get("/out/" + extension + "/data" + id + "." + extension));
        }
        File outfile = new File("/out/" + extension + "/data" + id + "." + extension);
        if (!outfile.exists()) {
            outfile.createNewFile();
        }
        writeFile(outfile, data);
        return outfile;

    }

    public static String getExtension(String path) {
        return path.substring(path.length() - 3, path.length());
    }

    private static void writeFile(File file, byte[] data) throws IOException {

        // Write the data
        try (OutputStream fo = new FileOutputStream(file)) {
            // Write the data
            fo.write(data);
            // flush the file (down the toilet)
            fo.flush();
            // Close the door to keep the smell in.
        }

    }

    public static byte[] toByteArray(File file) throws FileNotFoundException, FileNotFoundException, IOException {
        InputStream is = new FileInputStream(file);
        // Get the size of the file
        long length = file.length();
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}