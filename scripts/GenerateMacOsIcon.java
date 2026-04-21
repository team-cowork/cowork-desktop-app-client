import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import javax.imageio.ImageIO;

public final class GenerateMacOsIcon {
    private static final double CANVAS_SIZE = 1024.0;
    private static final double CONTENT_MIN_X = 1386.57;
    private static final double CONTENT_MIN_Y = 1178.14;
    private static final double CONTENT_WIDTH = 3846.0;
    private static final double CONTENT_HEIGHT = 2838.72;
    private static final double LOGO_WIDTH = 660.0;

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
        File iconsetDir = new File(root, "composeApp/icons/AppIcon.iconset");

        if (!iconsetDir.exists() && !iconsetDir.mkdirs()) {
            throw new IllegalStateException("Failed to create " + iconsetDir);
        }

        for (IconSize iconSize : ICON_SIZES) {
            BufferedImage canvas = renderIcon(iconSize.size);
            ImageIO.write(canvas, "png", new File(iconsetDir, iconSize.fileName));
        }

        ImageIO.write(renderIcon(1024), "png", new File(root, "composeApp/icons/AppIcon_macOS_1024.png"));
        writeIcns(iconsetDir, new File(root, "composeApp/icons/AppIcon.icns"));
    }

    private static BufferedImage renderIcon(int size) {
        BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double scaleToSize = size / CANVAS_SIZE;
            graphics.scale(scaleToSize, scaleToSize);

            Shape squircle = new RoundRectangle2D.Double(0, 0, CANVAS_SIZE, CANVAS_SIZE, 224, 224);
            graphics.setClip(squircle);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, (int) CANVAS_SIZE, (int) CANVAS_SIZE);

            double logoScale = LOGO_WIDTH / CONTENT_WIDTH;
            double logoHeight = CONTENT_HEIGHT * logoScale;
            double logoX = (CANVAS_SIZE - LOGO_WIDTH) / 2.0;
            double logoY = (CANVAS_SIZE - logoHeight) / 2.0;
            double translateX = logoX / logoScale - CONTENT_MIN_X;
            double translateY = logoY / logoScale - CONTENT_MIN_Y;

            graphics.scale(logoScale, logoScale);
            graphics.translate(translateX, translateY);
            graphics.setColor(new Color(0xEF4444));
            graphics.setStroke(new BasicStroke(91.5714f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            drawLogo(graphics);
        } finally {
            graphics.dispose();
        }
        return canvas;
    }

    private static void drawLogo(Graphics2D graphics) {
        drawPath(graphics, 3584.29, 2918, 4133.71, 2643.29, 5232.57, 2643.29, 4683.14, 2918);
        drawPath(graphics, 5232.57, 2643.29, 5232.57, 3742.14, 4683.14, 4016.86, 4683.14, 2918);
        drawPath(graphics, 3584.29, 2918, 3584.29, 4016.86, 4683.14, 4016.86, 4683.14, 2918);
        drawPath(graphics, 2485.43, 2185.43, 3034.86, 1910.71, 4133.71, 1910.71, 3584.29, 2185.43);
        drawPath(graphics, 4133.71, 1910.71, 4133.71, 3009.57, 3584.29, 3284.29, 3584.29, 2185.43);
        drawPath(graphics, 2485.43, 2185.43, 2485.43, 3284.29, 3584.29, 3284.29, 3584.29, 2185.43);
        drawPath(graphics, 1386.57, 1452.86, 1936, 1178.14, 3034.86, 1178.14, 2485.43, 1452.86);
        drawPath(graphics, 3034.86, 1178.14, 3034.86, 2277, 2485.43, 2551.71, 2485.43, 1452.86);
        drawPath(graphics, 1386.57, 1452.86, 1386.57, 2551.71, 2485.43, 2551.71, 2485.43, 1452.86);
    }

    private static void drawPath(Graphics2D graphics, double... points) {
        Path2D path = new Path2D.Double();
        path.moveTo(points[0], points[1]);
        for (int index = 2; index < points.length; index += 2) {
            path.lineTo(points[index], points[index + 1]);
        }
        path.closePath();
        graphics.draw(path);
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
