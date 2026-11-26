package com.mafioso.util;

import com.mafioso.service.ValidationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link DocumentStorageUtil} validating magic bytes, executable rejection,
 * size limits, random server filenames, and path traversal defenses.
 */
public class DocumentStorageUtilTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void storesValidPdfWithRandomServerFilenameAndSha256() throws Exception {
        Path root = tempFolder.newFolder("docs").toPath();
        byte[] pdfBytes = "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF".getBytes(StandardCharsets.US_ASCII);

        DocumentStorageUtil.StoredFileResult result = DocumentStorageUtil.storeValidatedDocument(
                42L,
                "../../etc/My Transcript.pdf",
                new ByteArrayInputStream(pdfBytes),
                root,
                1024L * 1024L
        );

        assertEquals("My Transcript.pdf", result.getSanitizedOriginalFilename());
        assertNotEquals("My Transcript.pdf", result.getStoredFilename());
        assertTrue(result.getStoredFilename().endsWith(".pdf"));
        assertTrue(result.getStorageRelativePath().startsWith("student_42/"));
        assertEquals("application/pdf", result.getDetectedContentType());
        assertEquals(pdfBytes.length, result.getFileSizeBytes());
        assertEquals(64, result.getSha256Checksum().length());
        assertTrue(Files.exists(result.getAbsolutePath()));
    }

    @Test
    public void storesValidDocxArchive() throws Exception {
        Path root = tempFolder.newFolder("docs-docx").toPath();
        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
            zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zos.write("<Types/>".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("word/document.xml"));
            zos.write("<w:document/>".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        DocumentStorageUtil.StoredFileResult result = DocumentStorageUtil.storeValidatedDocument(
                7L,
                "cv.docx",
                new ByteArrayInputStream(zipOut.toByteArray()),
                root,
                1024L * 1024L
        );
        assertEquals(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                result.getDetectedContentType()
        );
    }

    @Test(expected = ValidationException.class)
    public void rejectsExecutableExtension() throws Exception {
        Path root = tempFolder.newFolder("docs-exe").toPath();
        DocumentStorageUtil.storeValidatedDocument(
                1L,
                "payload.exe",
                new ByteArrayInputStream(new byte[]{0x4D, 0x5A, 0x00, 0x00}),
                root,
                1024L * 1024L
        );
    }

    @Test(expected = ValidationException.class)
    public void rejectsMzExecutableDisguisedAsPdf() throws Exception {
        Path root = tempFolder.newFolder("docs-spoof").toPath();
        DocumentStorageUtil.storeValidatedDocument(
                1L,
                "passport.pdf",
                new ByteArrayInputStream(new byte[]{0x4D, 0x5A, 0x00, 0x00, 0x01, 0x02}),
                root,
                1024L * 1024L
        );
    }

    @Test(expected = ValidationException.class)
    public void rejectsJarDisguisedAsDocx() throws Exception {
        Path root = tempFolder.newFolder("docs-jar").toPath();
        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
            zos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            zos.write("Manifest-Version: 1.0\n".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("com/evil/Main.class"));
            zos.write(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE});
            zos.closeEntry();
        }

        DocumentStorageUtil.storeValidatedDocument(
                1L,
                "evil.docx",
                new ByteArrayInputStream(zipOut.toByteArray()),
                root,
                1024L * 1024L
        );
    }

    @Test(expected = ValidationException.class)
    public void enforcesMaxFileSizeAndCleansUpTempFile() throws Exception {
        Path root = tempFolder.newFolder("docs-limit").toPath();
        byte[] largePdf = new byte[2048];
        largePdf[0] = '%';
        largePdf[1] = 'P';
        largePdf[2] = 'D';
        largePdf[3] = 'F';
        largePdf[4] = '-';

        try {
            DocumentStorageUtil.storeValidatedDocument(
                    9L,
                    "big.pdf",
                    new ByteArrayInputStream(largePdf),
                    root,
                    512L
            );
        } finally {
            assertFalse(Files.list(root.resolve("student_9")).findAny().isPresent());
        }
    }

    @Test(expected = SecurityException.class)
    public void rejectsPathTraversalOnResolve() throws Exception {
        Path root = tempFolder.newFolder("docs-trav").toPath();
        DocumentStorageUtil.resolveExistingDocumentPath("../../etc/passwd", root);
    }
}
