package master.innutrient.client.gui;

import dev.uapi.client.ui.core.UIBounds;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Locale;

final class DashboardRender {
    static final int CARD_BACKGROUND = 0xC9181223;
    static final int CARD_HOVER = 0xE0261A35;
    static final int CARD_BORDER = 0xFF513267;
    static final int DIVIDER = 0xFF352441;
    static final int LOW = 0xFFFF7777;
    static final int BELOW = 0xFFFFB65C;
    static final int HEALTHY = 0xFF79D995;
    static final int ABOVE = 0xFFFFD76A;
    static final int EXCESSIVE = 0xFFD66BFF;

    private DashboardRender() {}

    static void card(GuiGraphicsExtractor graphics, UIBounds bounds, boolean hovered, int accent) {
        if (bounds.width() <= 0 || bounds.height() <= 0) return;
        graphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), hovered ? CARD_HOVER : CARD_BACKGROUND);
        border(graphics, bounds, CARD_BORDER);
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + 2, bounds.bottom(), accent);
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

    static String percent(double value) {
        return String.format(Locale.ROOT, "%.0f%%", value);
    }

    static String signedPercent(double multiplier, boolean inverse) {
        double change = (multiplier - 1.0) * 100.0 * (inverse ? -1 : 1);
        return String.format(Locale.ROOT, "%+.0f%%", change);
    }
}

