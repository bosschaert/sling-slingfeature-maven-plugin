package org.apache.sling.feature.maven.mojos;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JarDecompressorTest {
    @Test
    public void testDecompress() throws Exception {
        File cj = new File(getClass().getResource("/repository/compressed.jar").getFile());
        File uj = Files.createTempFile(getClass().getSimpleName(), "uncompressed.jar").toFile();

        try {
            JarDecompressor.decompress(cj, uj);

            assertJarsEqual(cj, uj);
            assertTrue("Decompressed jar should be bigger", uj.length() > cj.length());
        } finally {
            assertTrue(uj.delete());
        }
    }

    private void assertJarsEqual(File cj, File uj) throws IOException {
        Map<String, byte[]> expectedBytes = readJar(cj);
        Map<String, byte[]> actualBytes = readJar(uj);
        assertEquals(expectedBytes, actualBytes);
    }

    private Map<String, byte[]> readJar(File jar) throws IOException {
        byte[] buffer = new byte[16384];
        Map<String, byte[]> map = new HashMap<>();

        try (JarInputStream jis = new JarInputStream(new FileInputStream(jar))) {
            JarEntry je = null;
            while ((je = jis.getNextJarEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                JarDecompressor.drainStream(jis, baos, buffer);
                map.put(je.getName(), baos.toByteArray());
            }
        }
        return map;
    }
}
