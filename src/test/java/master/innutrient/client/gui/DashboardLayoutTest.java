package master.innutrient.client.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardLayoutTest {
    @Test
    void staysInsideCommonGuiResolutionsAndScales() {
        int[][] resolutions = {{320, 180}, {427, 240}, {640, 360}, {854, 480}, {1280, 720}, {1720, 720}};
        for (int[] resolution : resolutions) {
            DashboardLayout.Layout layout = DashboardLayout.calculate(resolution[0], resolution[1], 5, 6);
            assertTrue(layout.panel().x() >= 0);
            assertTrue(layout.panel().y() >= 0);
            assertTrue(layout.panel().right() <= resolution[0]);
            assertTrue(layout.panel().bottom() <= resolution[1]);
            assertTrue(layout.viewport().width() > 0);
            assertTrue(layout.viewport().height() > 0);
            assertEquals(5, layout.nutrientRows().size());
        }
    }

    @Test
    void switchesBetweenCompactAndWideWithoutDetachedPanels() {
        DashboardLayout.Layout compact = DashboardLayout.calculate(540, 360, 5, 4);
        DashboardLayout.Layout wide = DashboardLayout.calculate(854, 480, 5, 4);
        assertFalse(compact.wide());
        assertTrue(wide.wide());
        assertEquals(compact.summary().x(), compact.recentFoods().x());
        assertTrue(wide.recentFoods().x() > wide.nutrientTitle().x());
        assertTrue(wide.modifiers().y() > wide.recentFoods().y());
    }

    @Test
    void customGroupCountsRemainBoundedAndScrollable() {
        for (int groups : new int[] {0, 2, 5, 9, 18}) {
            DashboardLayout.Layout layout = DashboardLayout.calculate(427, 240, groups, 6);
            assertEquals(groups, layout.nutrientRows().size());
            for (DashboardLayout.Rect row : layout.nutrientRows()) {
                assertTrue(row.x() >= 0);
                assertTrue(row.right() <= layout.viewport().width());
                assertTrue(row.height() > 0);
            }
        }
        DashboardLayout.Layout crowded = DashboardLayout.calculate(320, 180, 18, 6);
        assertTrue(crowded.contentHeight() > crowded.viewport().height());
    }
}
