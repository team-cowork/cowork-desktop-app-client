import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import javax.imageio.ImageIO;

public final class GenerateWindowsIcon {
    private static final int[] ICON_SIZES = {16, 24, 32, 48, 64, 128, 256};

    public static void main(String[] args) throws Exception {
        File root = new File(".").getCanonicalFile();
        File iconFile = new File(root, "composeApp/icons/AppIcon_windows.ico");
        BufferedImage source = readLargestIconPng(iconFile);

        byte[][] pngEntries = new byte[ICON_SIZES.length][];
        for (int index = 0; index < ICON_SIZES.length; index++) {
            BufferedImage resized = resize(source, ICON_SIZES[index]);
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            ImageIO.write(resized, "png", png);
            pngEntries[index] = png.toByteArray();
        }

        writeIco(iconFile, pngEntries);
    }

    private static BufferedImage readLargestIconPng(File iconFile) throws Exception {
        byte[] ico = Files.readAllBytes(iconFile.toPath());
        int count = readUInt16Le(ico, 4);
        if (count <= 0) {
            throw new IllegalStateException("ICO file has no entries: " + iconFile);
        }

        int bestOffset = -1;
        int bestSize = -1;
        int bestWidth = -1;
        for (int index = 0; index < count; index++) {
            int entry = 6 + index * 16;
            int width = Byte.toUnsignedInt(ico[entry]);
            if (width == 0) width = 256;
            int imageSize = readInt32Le(ico, entry + 8);
            int imageOffset = readInt32Le(ico, entry + 12);
            if (width > bestWidth) {
                bestWidth = width;
                bestOffset = imageOffset;
                bestSize = imageSize;
            }
        }

        if (bestOffset < 0 || bestSize <= 0 || bestOffset + bestSize > ico.length) {
            throw new IllegalStateException("Invalid ICO image entry: " + iconFile);
        }

        byte[] png = new byte[bestSize];
        System.arraycopy(ico, bestOffset, png, 0, bestSize);
        BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(png));
        if (image == null) {
            throw new IllegalStateException("Largest ICO entry is not a PNG image: " + iconFile);
        }
        return image;
    }

    private static BufferedImage resize(BufferedImage source, int size) {
        BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, size, size, null);
        } finally {
            graphics.dispose();
        }
        return canvas;
    }

    private static void writeIco(File iconFile, byte[][] pngEntries) throws Exception {
        try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(iconFile.toPath()))) {
            output.writeShort(Short.reverseBytes((short) 0));
            output.writeShort(Short.reverseBytes((short) 1));
            output.writeShort(Short.reverseBytes((short) ICON_SIZES.length));

            int offset = 6 + ICON_SIZES.length * 16;
            for (int index = 0; index < ICON_SIZES.length; index++) {
                int size = ICON_SIZES[index];
                byte[] png = pngEntries[index];
                output.writeByte(size == 256 ? 0 : size);
                output.writeByte(size == 256 ? 0 : size);
                output.writeByte(0);
                output.writeByte(0);
                output.writeShort(Short.reverseBytes((short) 1));
                output.writeShort(Short.reverseBytes((short) 32));
                output.writeInt(Integer.reverseBytes(png.length));
                output.writeInt(Integer.reverseBytes(offset));
                offset += png.length;
            }

            for (byte[] png : pngEntries) {
                output.write(png);
            }
        }
    }

    private static int readUInt16Le(byte[] bytes, int offset) {
        return Byte.toUnsignedInt(bytes[offset]) | (Byte.toUnsignedInt(bytes[offset + 1]) << 8);
    }

    private static int readInt32Le(byte[] bytes, int offset) {
        return Byte.toUnsignedInt(bytes[offset])
            | (Byte.toUnsignedInt(bytes[offset + 1]) << 8)
            | (Byte.toUnsignedInt(bytes[offset + 2]) << 16)
            | (Byte.toUnsignedInt(bytes[offset + 3]) << 24);
    }
}
