package com.publiccms.common.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileLock;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.function.BiPredicate;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.publiccms.common.constants.Constants;

/**
 * 压缩/解压缩zip包处理类
 *
 * ZipUtils
 *
 */
public class ZipUtils {
    private ZipUtils() {
    }

    protected static final Log log = LogFactory.getLog(ZipUtils.class);

    /**
     * @param sourceFilePath
     * @param zipFilePath
     * @return whether the compression is successful
     * @throws IOException
     */
    public static boolean zip(String sourceFilePath, String zipFilePath) throws IOException {
        return zip(sourceFilePath, zipFilePath, true);
    }

    /**
     * @param sourceFilePath
     * @param zipFilePath
     * @param overwrite
     * @return whether the compression is successful
     * @throws IOException
     */
    public static boolean zip(String sourceFilePath, String zipFilePath, boolean overwrite) throws IOException {
        if (CommonUtils.notEmpty(sourceFilePath)) {
            File zipFile = new File(zipFilePath);
            if (zipFile.exists() && !overwrite) {
                return false;
            } else {
                zipFile.getParentFile().mkdirs();
                try (FileOutputStream outputStream = new FileOutputStream(zipFile);
                        ArchiveOutputStream<ZipArchiveEntry> zipOutputStream = new ZipArchiveOutputStream(outputStream);
                        FileLock fileLock = outputStream.getChannel().tryLock()) {
                    if (null != fileLock) {
                        compress(Paths.get(sourceFilePath), zipOutputStream, Constants.BLANK);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param sourceFilePath
     * @param out
     * @param basedir
     * @throws IOException
     */
    public static void compress(Path sourceFilePath, ArchiveOutputStream<ZipArchiveEntry> out, String basedir)
            throws IOException {
        if (Files.isDirectory(sourceFilePath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceFilePath)) {
                for (Path entry : stream) {
                    BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                    String fullName = Constants.BLANK.equals(basedir) ? entry.toFile().getName()
                            : CommonUtils.joinString(basedir, Constants.SEPARATOR, entry.toFile().getName());
                    if (attrs.isDirectory()) {
                        ZipArchiveEntry zipEntry = new ZipArchiveEntry(CommonUtils.joinString(fullName, Constants.SEPARATOR));
                        out.putArchiveEntry(zipEntry);
                        compress(entry, out, fullName);
                    } else if (!fullName.equalsIgnoreCase("files.zip")) {
                        compressFile(entry.toFile(), out, fullName);
                    }
                }
            } catch (IOException e) {
            }
        } else {
            File file = sourceFilePath.toFile();
            compressFile(file, out, file.getName());
        }
    }

    /**
     * <pre>
     * &#64;RequestMapping("export")
     * public ResponseEntity&lt;StreamingResponseBody&gt; export() {
     *     HttpHeaders headers = new HttpHeaders();
     *     headers.setContentDisposition(
     *             ContentDisposition.attachment().filename("filename.zip", StandardCharsets.UTF_8).build());
     *     StreamingResponseBody body = new StreamingResponseBody() {
     *         &#64;Override
     *         public void writeTo(OutputStream outputStream) throws IOException {
     *             try (ArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(outputStream)) {
     *                 ZipUtils.compressFile(new File("filename.txt"), zipOutputStream, "dir/filename.txt");
     *             }
     *         }
     *     };
     *     return ResponseEntity.ok().headers(headers).body(body);
     * }
     * </pre>
     *
     * @param file
     * @param out
     * @param fullName
     * @throws IOException
     */
    public static void compressFile(File file, ArchiveOutputStream<ZipArchiveEntry> out, String fullName) throws IOException {
        if (CommonUtils.notEmpty(file) && file.isFile()) {
            ZipArchiveEntry entry = new ZipArchiveEntry(fullName);
            entry.setTime(file.lastModified());
            out.putArchiveEntry(entry);
            try (FileInputStream fis = new FileInputStream(file)) {
                StreamUtils.copy(fis, out);
            }
            out.closeArchiveEntry();
        }
    }

    public static void compressFile(InputStream inputStream, ArchiveOutputStream<ZipArchiveEntry> out, String fullName)
            throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(fullName);
        entry.setTime(System.currentTimeMillis());
        out.putArchiveEntry(entry);
        StreamUtils.copy(inputStream, out);
        out.closeArchiveEntry();
    }

    /**
     * @param zipFilePath
     * @param encoding
     * @param overwrite
     *            if true then overwrite the file
     * @param overwriteFunction
     *            if overwrite is true and this function return true or this
     *            function is null then overwrite the file
     * @throws IOException
     */
    public static void unzipHere(String zipFilePath, String encoding, boolean overwrite,
            BiPredicate<ZipFile, ZipArchiveEntry> overwriteFunction) throws IOException {
        int index = zipFilePath.lastIndexOf(Constants.SEPARATOR);
        if (0 > index) {
            index = zipFilePath.lastIndexOf('\\');
        }
        unzip(zipFilePath, zipFilePath.substring(0, index), encoding, overwrite, overwriteFunction);
    }

    /**
     * @param zipFilePath
     * @param encoding
     * @param overwrite
     * @param overwriteFunction
     * @throws IOException
     */
    public static void unzip(String zipFilePath, String encoding, boolean overwrite,
            BiPredicate<ZipFile, ZipArchiveEntry> overwriteFunction) throws IOException {
        unzip(zipFilePath, zipFilePath.substring(0, zipFilePath.lastIndexOf(Constants.DOT)), encoding, overwrite,
                overwriteFunction);
    }

    /**
     * @param zipFilePath
     * @param targetPath
     * @param encoding
     * @param overwrite
     * @param overwriteFunction
     * @throws IOException
     */
    public static void unzip(String zipFilePath, String targetPath, String encoding, boolean overwrite,
            BiPredicate<ZipFile, ZipArchiveEntry> overwriteFunction) throws IOException {
        ZipFile zipFile = ZipFile.builder().setFile(zipFilePath).setCharset(encoding).get();
        Enumeration<ZipArchiveEntry> entryEnum = zipFile.getEntries();
        if (!targetPath.endsWith(Constants.SEPARATOR) && !targetPath.endsWith("\\")) {
            targetPath = CommonUtils.joinString(targetPath, File.separator);
        }
        while (entryEnum.hasMoreElements()) {
            ZipArchiveEntry zipEntry = entryEnum.nextElement();
            unzip(zipFile, zipEntry, targetPath, zipEntry.getName(), overwrite, overwriteFunction);
        }
        zipFile.close();
    }

    private static void unzip(ZipFile zipFile, ZipArchiveEntry zipEntry, String targetPath, String filePath, boolean overwrite,
            BiPredicate<ZipFile, ZipArchiveEntry> overwriteFunction) {
        if (filePath.contains("..")) {
            filePath = filePath.replace("..", Constants.BLANK);
        }
        if (zipEntry.isDirectory()) {
            File dir = new File(CommonUtils.joinString(targetPath, filePath));
            dir.mkdirs();
        } else {
            File targetFile = new File(CommonUtils.joinString(targetPath, filePath));
            if (!targetFile.exists() || overwrite) {
                targetFile.getParentFile().mkdirs();
                if (null == overwriteFunction || overwriteFunction.test(zipFile, zipEntry)) {
                    try (InputStream inputStream = zipFile.getInputStream(zipEntry);
                            FileOutputStream outputStream = new FileOutputStream(targetFile);
                            FileLock fileLock = outputStream.getChannel().tryLock()) {
                        if (null != fileLock) {
                            StreamUtils.copy(inputStream, outputStream);
                        }
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * @param zipFile
     * @param directory
     * @param targetPath
     * @param overwrite
     * @param overwriteFunction
     */
    public static void unzip(ZipFile zipFile, String directory, String targetPath, boolean overwrite,
            BiPredicate<ZipFile, ZipArchiveEntry> overwriteFunction) {
        Enumeration<ZipArchiveEntry> entryEnum = zipFile.getEntries();
        if (!targetPath.endsWith(Constants.SEPARATOR) && !targetPath.endsWith("\\")) {
            targetPath = CommonUtils.joinString(targetPath, File.separator);
        }
        if (null != directory && !directory.endsWith(Constants.SEPARATOR)) {
            directory = CommonUtils.joinString(directory, Constants.SEPARATOR);
        }
        while (entryEnum.hasMoreElements()) {
            ZipArchiveEntry zipEntry = entryEnum.nextElement();
            if (null == directory || zipEntry.getName().startsWith(directory)) {
                unzip(zipFile, zipEntry, targetPath, Strings.CS.removeStart(zipEntry.getName(), directory), overwrite,
                        overwriteFunction);
            }
        }
    }
}
