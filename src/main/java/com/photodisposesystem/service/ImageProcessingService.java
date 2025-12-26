package com.photodisposesystem.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Service;

@Service
public class ImageProcessingService {

    public Path compressImage(Path inputPath, float quality) throws IOException {
        BufferedImage image = ImageIO.read(inputPath.toFile());
        if (image == null) {
            throw new IllegalArgumentException("Unsupported image format");
        }
        String format = getFormatName(inputPath.getFileName().toString());
        Path outputPath = buildOutputPath(inputPath, "compressed");
        writeImage(image, format, outputPath, quality);
        return outputPath;
    }

    public Path convertImage(Path inputPath, String targetFormat) throws IOException {
        BufferedImage image = ImageIO.read(inputPath.toFile());
        if (image == null) {
            throw new IllegalArgumentException("Unsupported image format");
        }
        String safeFormat = targetFormat.toLowerCase();
        Path outputPath = inputPath.getParent().resolve(stripExtension(inputPath.getFileName().toString())
                + "_converted." + safeFormat);
        ImageIO.write(image, safeFormat, outputPath.toFile());
        return outputPath;
    }

    private void writeImage(BufferedImage image, String format, Path outputPath, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            throw new IllegalArgumentException("No writer available for format: " + format);
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(Math.min(Math.max(quality, 0.1f), 1.0f));
        }
        Files.createDirectories(outputPath.getParent());
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputPath.toFile())) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    private String getFormatName(String filename) {
        int index = filename.lastIndexOf('.');
        if (index == -1) {
            return "jpg";
        }
        return filename.substring(index + 1).toLowerCase();
    }

    private String stripExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index == -1) {
            return filename;
        }
        return filename.substring(0, index);
    }

    private Path buildOutputPath(Path inputPath, String suffix) {
        String filename = stripExtension(inputPath.getFileName().toString());
        String ext = getFormatName(inputPath.getFileName().toString());
        return inputPath.getParent().resolve(filename + "_" + suffix + "." + ext);
    }
}
