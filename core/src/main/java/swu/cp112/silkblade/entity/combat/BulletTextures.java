package swu.cp112.silkblade.entity.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Class to manage bullet textures.
 * Generates and caches textures for different bullet shapes.
 */
public class BulletTextures implements Disposable {
    private static BulletTextures instance;

    // Cache textures for reuse
    private final ObjectMap<Bullet.Shape, Texture> bulletTextures = new ObjectMap<>();
    private final ObjectMap<Bullet.Shape, Texture> glowTextures = new ObjectMap<>();

    // Default size for texture generation
    private static final int DEFAULT_TEXTURE_SIZE = 64;

    private BulletTextures() {
        generateBulletTextures();
    }

    public static BulletTextures getInstance() {
        if (instance == null) {
            instance = new BulletTextures();
        }
        return instance;
    }

    /**
     * Get the appropriate bullet texture for the given shape
     */
    public Texture getBulletTexture(Bullet.Shape shape) {
        if (!bulletTextures.containsKey(shape)) {
            generateTextureForShape(shape);
        }
        return bulletTextures.get(shape);
    }

    /**
     * Get the appropriate glow texture for the given shape
     */
    public Texture getGlowTexture(Bullet.Shape shape) {
        if (!glowTextures.containsKey(shape)) {
            generateGlowTextureForShape(shape);
        }
        return glowTextures.get(shape);
    }

    /**
     * Generate all textures for all bullet shapes
     */
    private void generateBulletTextures() {
        for (Bullet.Shape shape : Bullet.Shape.values()) {
            generateTextureForShape(shape);
            generateGlowTextureForShape(shape);
        }
    }

    /**
     * Generate texture for a specific bullet shape
     */
    private void generateTextureForShape(Bullet.Shape shape) {
        try {
            Pixmap pixmap = new Pixmap(DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);

            drawShapeOnPixmap(pixmap, shape);

            Texture texture = new Texture(pixmap);
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            bulletTextures.put(shape, texture);

            pixmap.dispose();
        } catch (Exception e) {
            GameLogger.logError("Failed to generate bullet texture for shape: " + shape, e);
        }
    }

    /**
     * Generate glow texture for a specific bullet shape
     */
    private void generateGlowTextureForShape(Bullet.Shape shape) {
        try {
            Pixmap pixmap = new Pixmap(DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE, Pixmap.Format.RGBA8888);

            // Create a larger, softer version of the shape for the glow effect
            Color glowColor = new Color(1, 1, 1, 0.7f);
            pixmap.setColor(glowColor);

            // Draw the glow (slightly larger than the regular shape)
            drawShapeOnPixmap(pixmap, shape, 0.2f);

            // Apply a soft gradient to the edges
            applyGlowGradient(pixmap);

            Texture texture = new Texture(pixmap);
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            glowTextures.put(shape, texture);

            pixmap.dispose();
        } catch (Exception e) {
            GameLogger.logError("Failed to generate glow texture for shape: " + shape, e);
        }
    }

    /**
     * Apply a soft gradient to create a glow effect
     */
    private void applyGlowGradient(Pixmap pixmap) {
        int width = pixmap.getWidth();
        int height = pixmap.getHeight();

        // Create a new pixmap for the result
        Pixmap result = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        int radius = width / 6;  // Blur radius

        // Simple blur implementation
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float r = 0, g = 0, b = 0, a = 0;
                int count = 0;

                // Sample pixels in radius
                for (int kx = -radius; kx <= radius; kx++) {
                    for (int ky = -radius; ky <= radius; ky++) {
                        int sampleX = x + kx;
                        int sampleY = y + ky;

                        if (sampleX >= 0 && sampleX < width && sampleY >= 0 && sampleY < height) {
                            Color color = new Color(pixmap.getPixel(sampleX, sampleY));
                            r += color.r;
                            g += color.g;
                            b += color.b;
                            a += color.a;
                            count++;
                        }
                    }
                }

                // Average values
                if (count > 0) {
                    r /= count;
                    g /= count;
                    b /= count;
                    a /= count;

                    // Set the pixel in the result pixmap
                    result.setColor(r, g, b, a);
                    result.drawPixel(x, y);
                }
            }
        }

        // Copy the result back to the original pixmap
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixmap.drawPixel(x, y, result.getPixel(x, y));
            }
        }

        result.dispose();
    }

    /**
     * Draw the specified shape on a pixmap
     */
    private void drawShapeOnPixmap(Pixmap pixmap, Bullet.Shape shape) {
        drawShapeOnPixmap(pixmap, shape, 0);
    }

    /**
     * Draw the specified shape on a pixmap with padding
     */
    private void drawShapeOnPixmap(Pixmap pixmap, Bullet.Shape shape, float padding) {
        int width = pixmap.getWidth();
        int height = pixmap.getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = (int) (Math.min(width, height) / 2 * (1 - padding));

        pixmap.setBlending(Pixmap.Blending.SourceOver);

        switch (shape) {
            case CIRCLE:
                pixmap.fillCircle(centerX, centerY, radius);
                break;

            case STAR:
                drawStar(pixmap, centerX, centerY, radius, 5);
                break;

            case TRIANGLE:
                drawTriangle(pixmap, centerX, centerY, radius);
                break;

            case SQUARE:
                int halfSize = radius;
                pixmap.fillRectangle(centerX - halfSize, centerY - halfSize, halfSize * 2, halfSize * 2);
                break;

            case HEXAGON:
                drawPolygon(pixmap, centerX, centerY, radius, 6);
                break;

            case DIAMOND:
                drawDiamond(pixmap, centerX, centerY, radius);
                break;

            case HEART:
                drawHeart(pixmap, centerX, centerY, radius);
                break;

            case CRESCENT:
                drawCrescent(pixmap, centerX, centerY, radius);
                break;
        }
    }

    private void drawStar(Pixmap pixmap, int centerX, int centerY, int radius, int points) {
        // Create a smoother, more balanced star shape with clear edges
        // This will produce better trail effects

        // Use two radii for inner and outer points
        float outerRadius = radius;
        float innerRadius = radius * 0.4f; // 40% of outer radius for a nice star shape

        // Pre-calculate coordinates for all points
        float[][] coords = new float[points * 2][2];

        // Generate star points
        for (int i = 0; i < points * 2; i++) {
            // Alternating between outer and inner points
            float currentRadius = (i % 2 == 0) ? outerRadius : innerRadius;

            // Calculate angle (starting from top)
            float angle = (float) (i * Math.PI / points - Math.PI / 2);

            // Store coordinates
            coords[i][0] = centerX + currentRadius * MathUtils.cos(angle);
            coords[i][1] = centerY + currentRadius * MathUtils.sin(angle);
        }

        // Draw triangles to form the star
        for (int i = 0; i < points * 2; i++) {
            int nextIndex = (i + 1) % (points * 2);

            // Draw triangle from center to current point to next point
            drawFilledTriangle(
                pixmap,
                centerX, centerY,
                (int)coords[i][0], (int)coords[i][1],
                (int)coords[nextIndex][0], (int)coords[nextIndex][1]
            );
        }
    }

    private void drawTriangle(Pixmap pixmap, int centerX, int centerY, int radius) {
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        for (int i = 0; i < 3; i++) {
            float angle = (float) (i * 2 * Math.PI / 3 - Math.PI / 2);
            xPoints[i] = centerX + (int) (radius * MathUtils.cos(angle));
            yPoints[i] = centerY + (int) (radius * MathUtils.sin(angle));
        }

        drawFilledTriangle(pixmap, xPoints[0], yPoints[0], xPoints[1], yPoints[1], xPoints[2], yPoints[2]);
    }

    private void drawPolygon(Pixmap pixmap, int centerX, int centerY, int radius, int sides) {
        int[] xPoints = new int[sides];
        int[] yPoints = new int[sides];

        for (int i = 0; i < sides; i++) {
            float angle = (float) (i * 2 * Math.PI / sides - Math.PI / 2);
            xPoints[i] = centerX + (int) (radius * MathUtils.cos(angle));
            yPoints[i] = centerY + (int) (radius * MathUtils.sin(angle));
        }

        // Draw filled triangles to create the polygon
        for (int i = 1; i < sides - 1; i++) {
            drawFilledTriangle(pixmap, xPoints[0], yPoints[0], xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }
    }

    private void drawDiamond(Pixmap pixmap, int centerX, int centerY, int radius) {
        // Create a diamond with smooth edges using four triangle draws

        // Calculate the diamond points
        int top = centerY - radius;
        int bottom = centerY + radius;
        int left = centerX - radius;
        int right = centerX + radius;

        // Draw the four triangles that make up the diamond
        // Top triangle
        drawFilledTriangle(pixmap, centerX, top, left, centerY, right, centerY);

        // Bottom triangle
        drawFilledTriangle(pixmap, centerX, bottom, left, centerY, right, centerY);

        // This approach creates a more balanced, symmetric diamond shape
        // that works better with the trail rendering system
    }

    private void drawHeart(Pixmap pixmap, int centerX, int centerY, int radius) {
        // Create a heart shape that works well with trails
        // Scale the heart to fit in the radius
        int heartHeight = radius * 2;
        int heartWidth = heartHeight;

        // Use a more simplified approach that creates a smoother heart
        // This will result in better trail rendering

        // First, draw the two circles for the top lobes of the heart
        int lobeRadius = (int)(radius * 0.5f);
        int lobeOffset = (int)(radius * 0.4f);

        // Draw the left and right circles (lobes)
        pixmap.fillCircle(centerX - lobeOffset, centerY - lobeOffset, lobeRadius);
        pixmap.fillCircle(centerX + lobeOffset, centerY - lobeOffset, lobeRadius);

        // Now draw a triangle for the bottom part of the heart
        // Define triangle points
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        // Left point (base of left lobe)
        xPoints[0] = centerX - lobeOffset - lobeRadius/2;
        yPoints[0] = centerY - lobeOffset + lobeRadius/2;

        // Right point (base of right lobe)
        xPoints[1] = centerX + lobeOffset + lobeRadius/2;
        yPoints[1] = centerY - lobeOffset + lobeRadius/2;

        // Bottom point
        xPoints[2] = centerX;
        yPoints[2] = centerY + radius;

        // Draw the triangle to form the bottom of the heart
        drawFilledTriangle(pixmap, xPoints[0], yPoints[0], xPoints[1], yPoints[1], xPoints[2], yPoints[2]);
    }

    private void drawCrescent(Pixmap pixmap, int centerX, int centerY, int radius) {
        // Remember the current color
        Color currentColor = new Color(1, 1, 1, 1); // We're using white by default

        // Create a more defined crescent with better texture properties

        // Draw a filled circle first
        pixmap.fillCircle(centerX, centerY, radius);

        // Set blending mode to erase for clean edges
        pixmap.setBlending(Pixmap.Blending.None);

        // Set color to fully transparent for erasing
        pixmap.setColor(0, 0, 0, 0);

        // Create a crescent by offsetting the second circle to the right
        // Use a more appropriate offset and size ratio for better visual appearance
        int offset = (int)(radius * 0.4f); // Less offset than before for a thicker crescent
        int secondRadius = (int)(radius * 0.85f); // Slightly smaller than original

        // Position the second circle slightly to the right and higher
        // This creates a more attractive crescent shape
        pixmap.fillCircle(centerX + offset, (int) (centerY - radius * 0.05f), secondRadius);

        // Restore original color and blending
        pixmap.setColor(currentColor);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
    }

    /**
     * Helper method to draw a filled triangle
     */
    private void drawFilledTriangle(Pixmap pixmap, int x1, int y1, int x2, int y2, int x3, int y3) {
        // Find the bounding box of the triangle
        int minX = Math.min(x1, Math.min(x2, x3));
        int maxX = Math.max(x1, Math.max(x2, x3));
        int minY = Math.min(y1, Math.min(y2, y3));
        int maxY = Math.max(y1, Math.max(y2, y3));

        // Scan through the bounding box and check if each point is inside the triangle
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (isPointInTriangle(x, y, x1, y1, x2, y2, x3, y3)) {
                    pixmap.drawPixel(x, y);
                }
            }
        }
    }

    /**
     * Helper method to check if a point is inside a triangle using barycentric coordinates
     */
    private boolean isPointInTriangle(int px, int py, int x1, int y1, int x2, int y2, int x3, int y3) {
        float area = 0.5f * (-y2 * x3 + y1 * (-x2 + x3) + x1 * (y2 - y3) + x2 * y3);
        float s = 1 / (2 * area) * (y1 * x3 - x1 * y3 + (y3 - y1) * px + (x1 - x3) * py);
        float t = 1 / (2 * area) * (x1 * y2 - y1 * x2 + (y1 - y2) * px + (x2 - x1) * py);

        return s >= 0 && t >= 0 && (s + t) <= 1;
    }

    @Override
    public void dispose() {
        for (Texture texture : bulletTextures.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }

        for (Texture texture : glowTextures.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }

        bulletTextures.clear();
        glowTextures.clear();

        instance = null;
    }
}
