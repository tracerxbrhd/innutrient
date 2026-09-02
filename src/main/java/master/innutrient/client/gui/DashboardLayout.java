package master.innutrient.client.gui;

import java.util.ArrayList;
import java.util.List;

/** Pure GUI-space layout model, deliberately independent from Minecraft rendering APIs. */
final class DashboardLayout {
    static final int MAX_RECENT_FOODS = 6;
    static final int NUTRIENT_ROW_HEIGHT = 38;
    static final int RECENT_ROW_HEIGHT = 25;
    private static final int GAP = 7;
    static final int WIDE_BREAKPOINT = 590;
    private static final int NUTRIENT_HEADER_HEIGHT = 28;
    private static final int NUTRIENT_LEGEND_HEIGHT = 25;

    private DashboardLayout() {}

    static Layout calculate(int screenWidth, int screenHeight, int groupCount, int recentFoodCount,
                            int activeModifierCount) {
        int panelWidth = Math.max(1, Math.min(780, screenWidth - 16));
        int panelHeight = Math.max(1, Math.min(520, screenHeight - 34));
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = Math.max(25, (screenHeight - panelHeight) / 2 + 7);
        if (panelY + panelHeight > screenHeight - 2) panelY = Math.max(1, screenHeight - panelHeight - 2);

        Rect panel = new Rect(panelX, panelY, panelWidth, panelHeight);
        Rect header = new Rect(panelX + 11, panelY + 8, Math.max(0, panelWidth - 22), 20);
        Rect viewport = new Rect(panelX + 8, panelY + 32, Math.max(0, panelWidth - 16),
            Math.max(0, panelHeight - 40));
        int contentWidth = Math.max(1, viewport.width() - 5);
        boolean wide = contentWidth >= WIDE_BREAKPOINT;
        int groups = Math.max(0, groupCount);
        int shownRecent = Math.min(MAX_RECENT_FOODS, Math.max(0, recentFoodCount));
        int shownModifiers = Math.min(3, Math.max(0, activeModifierCount));
        int summaryHeight = wide ? 78 : 112;
        int nutrientRowsHeight = groups == 0 ? 33 : groups * NUTRIENT_ROW_HEIGHT;
        int nutrientHeight = NUTRIENT_HEADER_HEIGHT + nutrientRowsHeight + NUTRIENT_LEGEND_HEIGHT;
        int insightHeight = 68;
        int recentHeight = shownRecent == 0 ? 82 : 34 + shownRecent * RECENT_ROW_HEIGHT;
        int modifiersHeight = shownModifiers == 0 ? 72 : 43 + shownModifiers * 21;

        int y = 1;
        Rect summary = new Rect(1, y, Math.max(0, contentWidth - 2), summaryHeight);
        y += summaryHeight + GAP;

        List<Rect> rows = new ArrayList<>(groups);
        Rect nutrientSection;
        Rect nutrientTitle;
        Rect guidance;
        Rect recentFoods;
        Rect modifiers;
        if (wide) {
            int leftWidth = Math.max(300, (contentWidth - GAP) * 67 / 100);
            int rightX = leftWidth + GAP;
            int rightWidth = Math.max(0, contentWidth - rightX);
            nutrientSection = new Rect(1, y, Math.max(0, leftWidth - 1), nutrientHeight);
            nutrientTitle = titleInside(nutrientSection);
            addRows(rows, nutrientSection, groups);

            guidance = new Rect(rightX, y, rightWidth, insightHeight);
            recentFoods = new Rect(rightX, guidance.bottom() + GAP, rightWidth, recentHeight);
            modifiers = new Rect(rightX, recentFoods.bottom() + GAP, rightWidth, modifiersHeight);
            y = Math.max(nutrientSection.bottom(), modifiers.bottom()) + GAP;
        } else {
            nutrientSection = new Rect(1, y, Math.max(0, contentWidth - 2), nutrientHeight);
            nutrientTitle = titleInside(nutrientSection);
            addRows(rows, nutrientSection, groups);
            y = nutrientSection.bottom() + GAP;

            guidance = new Rect(1, y, Math.max(0, contentWidth - 2), insightHeight);
            y = guidance.bottom() + GAP;
            recentFoods = new Rect(1, y, Math.max(0, contentWidth - 2), recentHeight);
            y = recentFoods.bottom() + GAP;
            modifiers = new Rect(1, y, Math.max(0, contentWidth - 2), modifiersHeight);
            y = modifiers.bottom() + GAP;
        }
        return new Layout(panel, header, viewport, summary, nutrientSection, guidance, nutrientTitle,
            List.copyOf(rows), recentFoods, modifiers, Math.max(1, y), wide);
    }

    private static Rect titleInside(Rect section) {
        return new Rect(section.x() + 9, section.y() + 7, Math.max(0, section.width() - 18), 16);
    }

    private static void addRows(List<Rect> rows, Rect section, int count) {
        int rowY = section.y() + NUTRIENT_HEADER_HEIGHT;
        for (int index = 0; index < count; index++) {
            rows.add(new Rect(section.x() + 7, rowY, Math.max(0, section.width() - 14), NUTRIENT_ROW_HEIGHT));
            rowY += NUTRIENT_ROW_HEIGHT;
        }
    }

    record Rect(int x, int y, int width, int height) {
        Rect {
            width = Math.max(0, width);
            height = Math.max(0, height);
        }

        int right() { return x + width; }
        int bottom() { return y + height; }
    }

    record Layout(Rect panel, Rect header, Rect viewport, Rect summary, Rect nutrientSection, Rect guidance,
                  Rect nutrientTitle, List<Rect> nutrientRows, Rect recentFoods, Rect modifiers,
                  int contentHeight, boolean wide) {
        Layout {
            nutrientRows = List.copyOf(nutrientRows);
            contentHeight = Math.max(1, contentHeight);
        }
    }
}
