package com.mafioso.util;

import com.mafioso.config.AppConfig;
import com.mafioso.service.ValidationException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Secure document storage utility handling magic-byte content inspection,
 * executable rejection, random server-side filename generation, path traversal defense,
 * and SHA-256 integrity hashing.
 */
public final class DocumentStorageUtil {

    private static final Set<String> ALLOWED_EXTENSIONS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList("pdf", "png", "jpg", "jpeg", "docx"))
    );

    private static final Set<String> FORBIDDEN_EXECUTABLE_EXTENSIONS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                    "exe", "bat", "cmd", "sh", "msi", "dll", "so", "jar", "war", "ear", "class",
                    "js", "mjs", "cjs", "jsp", "jspx", "php", "py", "pl", "rb", "ps1", "vbs",
                    "com", "scr", "elf", "html", "htm", "svg", "hta", "cpl"
            ))
    );

    private static final Map<String, String> EXTENSION_CONTENT_TYPES = createContentTypes();

    private DocumentStorageUtil() {
    }

    private static Map<String, String> createContentTypes() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("pdf", "application/pdf");
        map.put("png", "image/png");
        map.put("jpg", "image/jpeg");
        map.put("jpeg", "image/jpeg");
        map.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        return Collections.unmodifiableMap(map);
    }

    public static final class StoredFileResult {
        private final String sanitizedOriginalFilename;
        private final String storedFilename;
        private final String storageRelativePath;
        private final String detectedContentType;
        private final long fileSizeBytes;
        private final String sha256Checksum;
        private final Path absolutePath;

        public StoredFileResult(String sanitizedOriginalFilename,
                                String storedFilename,
                                String storageRelativePath,
                                String detectedContentType,
                                long fileSizeBytes,
                                String sha256Checksum,
                                Path absolutePath) {
            this.sanitizedOriginalFilename = sanitizedOriginalFilename;
            this.storedFilename = storedFilename;
            this.storageRelativePath = storageRelativePath;
            this.detectedContentType = detectedContentType;
            this.fileSizeBytes = fileSizeBytes;
            this.sha256Checksum = sha256Checksum;
            this.absolutePath = absolutePath;
        }

        public String getSanitizedOriginalFilename() {
            return sanitizedOriginalFilename;
        }

        public String getStoredFilename() {
            return storedFilename;
        }

        public String getStorageRelativePath() {
            return storageRelativePath;
        }

        public String getDetectedContentType() {
            return detectedContentType;
        }

        public long getFileSizeBytes() {
            return fileSizeBytes;
        }

        public String getSha256Checksum() {
            return sha256Checksum;
        }

        public Path getAbsolutePath() {
            return absolutePath;
        }
    }

    public static StoredFileResult storeValidatedDocument(long studentId,
                                                        String rawFilename,
                                                        InputStream rawInputStream) throws IOException {
        return storeValidatedDocument(
                studentId,
                rawFilename,
                rawInputStream,
                Paths.get(AppConfig.getDocumentsDirectory()),
                AppConfig.getMaxDocumentSizeBytes()
        );
    }

    public static StoredFileResult storeValidatedDocument(long studentId,
                                                        String rawFilename,
                                                        InputStream rawInputStream,
                                                        Path baseDirectory,
                                                        long maxFileBytes) throws IOException {
        if (rawInputStream == null) {
            throw new ValidationException("Document file is required.",
                    Collections.singletonMap("file", "Please select a document file to upload."));
        }

        String sanitizedOriginal = sanitizeOriginalFilename(rawFilename);
        String ext = extractExtension(sanitizedOriginal);

        if (ext == null || FORBIDDEN_EXECUTABLE_EXTENSIONS.contains(ext) || !ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ValidationException(
                    "Unsupported or unsafe file extension. Allowed types: PDF, PNG, JPG, JPEG, DOCX.",
                    Collections.singletonMap("file", "Allowed file types: PDF, PNG, JPG, JPEG, DOCX.")
            );
        }

        Path normalizedBase = baseDirectory.toAbsolutePath().normalize();
        Files.createDirectories(normalizedBase);

        String studentFolder = "student_" + studentId;
        String storedFilename = UUID.randomUUID().toString() + "." + ext;
        String relativePath = studentFolder + "/" + storedFilename;

        Path targetDir = normalizedBase.resolve(studentFolder).normalize();
        if (!targetDir.startsWith(normalizedBase)) {
            throw new SecurityException("Invalid storage directory path.");
        }
        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(storedFilename).normalize();
        if (!targetPath.startsWith(targetDir)) {
            throw new SecurityException("Path traversal detected in storage target.");
        }

        byte[] headerBytes = new byte[16];
        int headerRead;
        BufferedInputStream bufferedIn = new BufferedInputStream(rawInputStream);
        bufferedIn.mark(32);
        headerRead = readFullyUpTo(bufferedIn, headerBytes);
        if (headerRead <= 0) {
            throw new ValidationException("Uploaded file is empty.",
                    Collections.singletonMap("file", "Uploaded file must not be empty."));
        }

        rejectExecutableMagicBytes(headerBytes, headerRead);
        String detectedContentType = verifyMagicBytesForExtension(ext, headerBytes, headerRead);

        bufferedIn.reset();

        long totalBytes = 0L;
        MessageDigest digest = sha256Digest();
        boolean committed = false;
        try {
            try (OutputStream out = Files.newOutputStream(targetPath)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = bufferedIn.read(buf)) != -1) {
                    totalBytes += n;
                    if (totalBytes > maxFileBytes) {
                        throw new ValidationException(
                                "File exceeds maximum allowed size of " + (maxFileBytes / (1024L * 1024L)) + " MB.",
                                Collections.singletonMap("file", "File exceeds maximum allowed size.")
                        );
                    }
                    digest.update(buf, 0, n);
                    out.write(buf, 0, n);
                }
            }

            if (totalBytes == 0L) {
                throw new ValidationException("Uploaded file is empty.",
                        Collections.singletonMap("file", "Uploaded file must not be empty."));
            }

            if ("docx".equals(ext)) {
                verifyValidDocxArchive(targetPath);
            }

            String checksum = toHex(digest.digest());
            committed = true;
            return new StoredFileResult(
                    sanitizedOriginal,
                    storedFilename,
                    relativePath,
                    detectedContentType,
                    totalBytes,
                    checksum,
                    targetPath
            );
        } finally {
            if (!committed) {
                deleteQuietly(targetPath);
            }
        }
    }

    public static Path resolveExistingDocumentPath(String storageRelativePath) {
        return resolveExistingDocumentPath(storageRelativePath, Paths.get(AppConfig.getDocumentsDirectory()));
    }

    public static Path resolveExistingDocumentPath(String storageRelativePath, Path baseDirectory) {
        if (storageRelativePath == null || storageRelativePath.trim().isEmpty()) {
            throw new SecurityException("Missing document storage path.");
        }
        String cleaned = storageRelativePath.replace('\\', '/').trim();
        if (cleaned.startsWith("/") || cleaned.contains("..")) {
            throw new SecurityException("Path traversal attempt rejected.");
        }
        Path normalizedBase = baseDirectory.toAbsolutePath().normalize();
        Path resolved = normalizedBase.resolve(cleaned).normalize();
        if (!resolved.startsWith(normalizedBase)) {
            throw new SecurityException("Resolved path escapes storage root.");
        }
        return resolved;
    }

    public static void deleteQuietly(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // Best effort cleanup
            }
        }
    }

    public static String sanitizeOriginalFilename(String rawFilename) {
        if (rawFilename == null) {
            return "document";
        }
        String normalized = rawFilename.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        String base = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        base = base.replaceAll("[\\p{Cntrl}\"]", "_").trim();
        if (base.isEmpty() || ".".equals(base) || "..".equals(base)) {
            return "document";
        }
        if (base.length() > 180) {
            int dot = base.lastIndexOf('.');
            if (dot > 0 && dot > base.length() - 15) {
                String ext = base.substring(dot);
                base = base.substring(0, 180 - ext.length()) + ext;
            } else {
                base = base.substring(0, 180);
            }
        }
        return base;
    }

    public static String sanitizeDownloadHeaderFilename(String filename) {
        String clean = sanitizeOriginalFilename(filename);
        StringBuilder sb = new StringBuilder(clean.length());
        for (int i = 0; i < clean.length(); i++) {
            char ch = clean.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')
                    || ch == '.' || ch == '-' || ch == '_' || ch == ' ') {
                sb.append(ch);
            } else {
                sb.append('_');
            }
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? "document" : result;
    }

    private static String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT).trim();
    }

    private static int readFullyUpTo(InputStream in, byte[] buffer) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            int n = in.read(buffer, offset, buffer.length - offset);
            if (n == -1) {
                break;
            }
            offset += n;
        }
        return offset;
    }

    private static void rejectExecutableMagicBytes(byte[] header, int len) {
        // PE/MZ executable
        if (len >= 2 && header[0] == 0x4D && header[1] == 0x5A) {
            throw executableRejected();
        }
        // ELF binary
        if (len >= 4 && header[0] == 0x7F && header[1] == 0x45 && header[2] == 0x4C && header[3] == 0x46) {
            throw executableRejected();
        }
        // Shebang script
        if (len >= 2 && header[0] == 0x23 && header[1] == 0x21) {
            throw executableRejected();
        }
        // Mach-O / Java class file (CAFEBABE / FEEDFACE)
        if (len >= 4) {
            int magic = ((header[0] & 0xFF) << 24)
                    | ((header[1] & 0xFF) << 16)
                    | ((header[2] & 0xFF) << 8)
                    | (header[3] & 0xFF);
            if (magic == 0xCAFEBABE || magic == 0xFEEDFACE || magic == 0xFEEDFACF
                    || magic == 0xCEFAEDFE || magic == 0xCFFAEDFE) {
                throw executableRejected();
            }
        }
    }

    private static ValidationException executableRejected() {
        return new ValidationException(
                "Executable or script files are strictly prohibited.",
                Collections.singletonMap("file", "Executable or script files are not permitted.")
        );
    }

    private static String verifyMagicBytesForExtension(String ext, byte[] header, int len) {
        if ("pdf".equals(ext)) {
            if (len >= 5 && header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F' && header[4] == '-') {
                return EXTENSION_CONTENT_TYPES.get("pdf");
            }
            throw mismatchedContent("File content does not match a valid PDF document.");
        }
        if ("png".equals(ext)) {
            if (len >= 8
                    && (header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47
                    && header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A) {
                return EXTENSION_CONTENT_TYPES.get("png");
            }
            throw mismatchedContent("File content does not match a valid PNG image.");
        }
        if ("jpg".equals(ext) || "jpeg".equals(ext)) {
            if (len >= 3 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return EXTENSION_CONTENT_TYPES.get("jpg");
            }
            throw mismatchedContent("File content does not match a valid JPEG image.");
        }
        if ("docx".equals(ext)) {
            if (len >= 4 && header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04) {
                return EXTENSION_CONTENT_TYPES.get("docx");
            }
            throw mismatchedContent("File content does not match a valid DOCX document.");
        }
        throw mismatchedContent("Unsupported file format.");
    }

    private static void verifyValidDocxArchive(Path docxPath) throws IOException {
        boolean hasContentTypes = false;
        boolean hasWordDocument = false;
        int entryCount = 0;

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(docxPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > 2000) {
                    throw mismatchedContent("DOCX archive structure exceeds safe limits.");
                }
                String name = entry.getName();
                if (name == null) {
                    continue;
                }
                if (name.endsWith(".class") || name.equalsIgnoreCase("META-INF/MANIFEST.MF")) {
                    throw executableRejected();
                }
                if ("[Content_Types].xml".equals(name)) {
                    hasContentTypes = true;
                } else if ("word/document.xml".equals(name)) {
                    hasWordDocument = true;
                }
            }
        } catch (IllegalArgumentException ex) {
            throw mismatchedContent("Invalid DOCX archive content.");
        }

        if (!hasContentTypes || !hasWordDocument) {
            throw mismatchedContent("File is not a valid Microsoft Word (.docx) document.");
        }
    }

    private static ValidationException mismatchedContent(String msg) {
        return new ValidationException(msg, Collections.singletonMap("file", msg));
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static String toHex(byte[] bytes) {
        ByteArrayOutputStream ignored = null;
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
