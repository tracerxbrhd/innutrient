package master.innutrient.client.gui;

import dev.uapi.client.ui.core.UIBounds;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Locale;

final class DashboardRender {
    static final int SECTION_BACKGROUND = 0xEB110D18;
    static final int SUBTLE_BACKGROUND = 0xD9181320;
    static final int METRIC_BACKGROUND = 0xB91D1726;
    static final int ROW_HOVER = 0xB5261D31;
    static final int SECTION_BORDER = 0xFF30243A;
    static final int DIVIDER = 0xFF2A2133;
    static final int TRACK_EMPTY = 0xFF231C2B;
    static final int LOW = 0xFFFF7777;
    static final int BELOW = 0xFFFFB65C;
    static final int HEALTHY = 0xFF79D995;
    static final int ABOVE = 0xFFFFD76A;
    static final int EXCESSIVE = 0xFFD66BFF;

    private DashboardRender() {}

    static void section(GuiGraphicsExtractor graphics, UIBounds bounds) {
        if (bounds.width() <= 0 || bounds.height() <= 0) return;
        graphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), SECTION_BACKGROUND);
        border(graphics, bounds, SECTION_BORDER);
    }

    static void accentedSection(GuiGraphicsExtractor graphics, UIBounds bounds, int accent) {
        section(graphics, bounds);
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + 2, bounds.bottom(), accent);
    }

    static void subtleCard(GuiGraphicsExtractor graphics, UIBounds bounds) {
        if (bounds.width() <= 0 || bounds.height() <= 0) return;
        graphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), SUBTLE_BACKGROUND);
    }

    static void interactiveRow(GuiGraphicsExtractor graphics, UIBounds bounds, boolean hovered, int accent) {
        if (!hovered || bounds.width() <= 0 || bounds.height() <= 0) return;
        graphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), ROW_HOVER);
        graphics.fill(bounds.x(), bounds.y() + 3, bounds.x() + 1, bounds.bottom() - 3, accent);
    }

    static void divider(GuiGraphicsExtractor graphics, int left, int right, int y) {
        if (right > left) graphics.fill(left, y, right, y + 1, DIVIDER);
    }

    static void border(GuiGraphicsExtractor graphics, UIBounds bounds, int color) {
        graphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.y() + 1, color);
        graphics.fill(bounds.x(), bounds.bottom() - 1, bounds.right(), bounds.bottom(), color);
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + 1, bounds.bottom(), color);
        graphics.fill(bounds.right() - 1, bounds.y(), bounds.right(), bounds.bottom(), color);
    }

    static void drawTrimmed(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int maxWidth,
                            int color, boolean shadow) {
        if (maxWidth <= 0) return;
        List<FormattedCharSequence> parts = font.split(text, maxWidth);
        graphics.text(font, parts.isEmpty() ? FormattedCharSequence.EMPTY : parts.getFirst(),
            x, y, color, shadow);
    }

    static void drawWrapped(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int maxWidth,
                            int maxLines, int color, boolean shadow) {
        if (maxWidth <= 0 || maxLines <= 0) return;
        List<FormattedCharSequence> parts = font.split(text, maxWidth);
        int count = Math.min(maxLines, parts.size());
        for (int line = 0; line < count; line++)
            graphics.text(font, parts.get(line), x, y + line * (font.lineHeight + 1), color, shadow);
    }

    static String percent(double value) {
        return String.format(Locale.ROOT, "%.0f%%", value);
    }

    static String signedPercent(double multiplier, boolean inverse) {
        double change = (multiplier - 1.0) * 100.0 * (inverse ? -1 : 1);
        return String.format(Locale.ROOT, "%+.0f%%", change);
    }

    static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }
}
