package master.innutrient.client.gui;

import java.util.ArrayList;
import java.util.List;

/** Pure GUI-space layout model, deliberately independent from Minecraft rendering APIs. */
final class DashboardLayout {
    static final int MAX_RECENT_FOODS = 6;
    private static final int GAP = 7;
    private static final int ROW_HEIGHT = 50;
    private static final int ROW_GAP = 4;

    private DashboardLayout() {}

    static Layout calculate(int screenWidth, int screenHeight, int groupCount, int recentFoodCount) {
        int panelWidth = Math.max(1, Math.min(780, screenWidth - 16));
        int panelHeight = Math.max(1, Math.min(520, screenHeight - 34));
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = Math.max(25, (screenHeight - panelHeight) / 2 + 7);
        if (panelY + panelHeight > screenHeight - 2) panelY = Math.max(1, screenHeight - panelHeight - 2);

        Rect panel = new Rect(panelX, panelY, panelWidth, panelHeight);
        Rect header = new Rect(panelX + 11, panelY + 8, Math.max(0, panelWidth - 22), 24);
        Rect viewport = new Rect(panelX + 8, panelY + 37, Math.max(0, panelWidth - 16),
            Math.max(0, panelHeight - 45));
        int contentWidth = Math.max(1, viewport.width() - 5);
        boolean wide = contentWidth >= 590;
        int summaryHeight = wide ? 82 : 94;
        int guidanceHeight = wide ? 36 : 45;
        int groups = Math.max(0, groupCount);
        int shownRecent = Math.min(MAX_RECENT_FOODS, Math.max(0, recentFoodCount));

        int y = 1;
        Rect summary = new Rect(1, y, Math.max(0, contentWidth - 2), summaryHeight);
        y += summaryHeight + GAP;
        Rect guidance = new Rect(1, y, Math.max(0, contentWidth - 2), guidanceHeight);
        y += guidanceHeight + GAP;

        List<Rect> rows = new ArrayList<>(groups);
        Rect nutrientTitle;
        Rect recentFoods;
        Rect modifiers;
        if (wide) {
            int leftWidth = Math.max(280, (contentWidth - GAP) * 3 / 5);
            int rightX = leftWidth + GAP;
            int rightWidth = Math.max(0, contentWidth - rightX);
            nutrientTitle = new Rect(2, y, Math.max(0, leftWidth - 3), 17);
            int rowY = y + 20;
            for (int index = 0; index < groups; index++) {
                rows.add(new Rect(1, rowY, Math.max(0, leftWidth - 2), ROW_HEIGHT));
                rowY += ROW_HEIGHT + ROW_GAP;
            }
            int nutrientBottom = groups == 0 ? rowY + 31 : rowY - ROW_GAP;
            int recentHeight = 36 + Math.max(1, shownRecent) * 27;
            recentFoods = new Rect(rightX, y, rightWidth, recentHeight);
            modifiers = new Rect(rightX, y + recentHeight + GAP, rightWidth, 111);
            y = Math.max(nutrientBottom, modifiers.bottom()) + GAP;
        } else {
            nutrientTitle = new Rect(2, y, Math.max(0, contentWidth - 3), 17);
            int rowY = y + 20;
            for (int index = 0; index < groups; index++) {
                rows.add(new Rect(1, rowY, Math.max(0, contentWidth - 2), ROW_HEIGHT));
                rowY += ROW_HEIGHT + ROW_GAP;
            }
            if (groups == 0) rowY += 31;
            y = rowY + GAP;
            int recentHeight = 36 + Math.max(1, shownRecent) * 27;
            recentFoods = new Rect(1, y, Math.max(0, contentWidth - 2), recentHeight);
            y += recentHeight + GAP;
            modifiers = new Rect(1, y, Math.max(0, contentWidth - 2), 111);
            y += modifiers.height() + GAP;
        }
        return new Layout(panel, header, viewport, summary, guidance, nutrientTitle, List.copyOf(rows),
            recentFoods, modifiers, Math.max(1, y), wide);
    }

    record Rect(int x, int y, int width, int height) {
        Rect {
            width = Math.max(0, width);
            height = Math.max(0, height);
        }

        int right() { return x + width; }
        int bottom() { return y + height; }
    }

    record Layout(Rect panel, Rect header, Rect viewport, Rect summary, Rect guidance,
                  Rect nutrientTitle, List<Rect> nutrientRows, Rect recentFoods, Rect modifiers,
                  int contentHeight, boolean wide) {
        Layout {
            nutrientRows = List.copyOf(nutrientRows);
            contentHeight = Math.max(1, contentHeight);
        }
    }
}
