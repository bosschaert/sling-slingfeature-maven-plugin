/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import static org.junit.Assert.assertArrayEquals;
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

    @Test
    public void testSignedJar() {

    }

    @Test
    public void testJarWithEmbeddedJar() {

    }

    private void assertJarsEqual(File cj, File uj) throws IOException {
        Map<String, byte[]> expectedJar = readJar(cj);
        Map<String, byte[]> actualJar = readJar(uj);

        assertEquals(expectedJar.size(), actualJar.size());
        for (Map.Entry<String, byte[]> entry : expectedJar.entrySet()) {
            byte[] actual = actualJar.get(entry.getKey());
            assertArrayEquals(entry.getValue(), actual);
        }
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
