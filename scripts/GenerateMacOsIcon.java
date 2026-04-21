import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import javax.imageio.ImageIO;

public final class GenerateMacOsIcon {
    private static final double LOGO_SCALE = 0.745;

    private static final IconSize[] ICON_SIZES = {
        new IconSize("icon_16x16.png", 16, "icp4"),
        new IconSize("icon_16x16@2x.png", 32, "ic11"),
        new IconSize("icon_32x32.png", 32, "icp5"),
        new IconSize("icon_32x32@2x.png", 64, "ic12"),
        new IconSize("icon_128x128.png", 128, "ic07"),
        new IconSize("icon_128x128@2x.png", 256, "ic13"),
        new IconSize("icon_256x256.png", 256, "ic08"),
        new IconSize("icon_256x256@2x.png", 512, "ic14"),
        new IconSize("icon_512x512.png", 512, "ic09"),
        new IconSize("icon_512x512@2x.png", 1024, "ic10"),
    };

    public static void main(String[] args) throws Exception {
        File root = new File(".").getCanonicalFile();
        File sourceFile = new File(root, "composeApp/icons/AppIcon_macOS_1024.png");
        File iconsetDir = new File(root, "composeApp/icons/AppIcon.iconset");

        BufferedImage source = ImageIO.read(sourceFile);
        if (source == null) {
            throw new IllegalStateException("Failed to read " + sourceFile);
        }

        if (!iconsetDir.exists() && !iconsetDir.mkdirs()) {
            throw new IllegalStateException("Failed to create " + iconsetDir);
        }

        for (IconSize iconSize : ICON_SIZES) {
            BufferedImage canvas = new BufferedImage(iconSize.size, iconSize.size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = canvas.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int logoSize = (int) Math.round(iconSize.size * LOGO_SCALE);
                int offset = (iconSize.size - logoSize) / 2;
                graphics.drawImage(source, offset, offset, logoSize, logoSize, null);
            } finally {
                graphics.dispose();
            }

            ImageIO.write(canvas, "png", new File(iconsetDir, iconSize.fileName));
        }

        writeIcns(iconsetDir, new File(root, "composeApp/icons/AppIcon.icns"));
    }

    private static void writeIcns(File iconsetDir, File outputFile) throws Exception {
        ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream();
        DataOutputStream body = new DataOutputStream(bodyBytes);

        for (IconSize iconSize : ICON_SIZES) {
            byte[] png = Files.readAllBytes(new File(iconsetDir, iconSize.fileName).toPath());
            body.writeBytes(iconSize.icnsType);
            body.writeInt(png.length + 8);
            body.write(png);
        }

        byte[] bodyData = bodyBytes.toByteArray();
        try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(outputFile.toPath()))) {
            output.writeBytes("icns");
            output.writeInt(bodyData.length + 8);
            output.write(bodyData);
        }
    }

    private record IconSize(String fileName, int size, String icnsType) {
    }
}
