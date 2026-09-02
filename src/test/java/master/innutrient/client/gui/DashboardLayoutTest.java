package master.innutrient.client.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardLayoutTest {
    @Test
    void staysInsideCommonGuiResolutionsAndScales() {
        int[][] resolutions = {
            {320, 180}, {427, 240}, {640, 360}, {854, 480}, {960, 540}, {1280, 720}, {1720, 720}
        };
        for (int[] resolution : resolutions) {
            DashboardLayout.Layout layout = DashboardLayout.calculate(resolution[0], resolution[1], 5, 6, 3);
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
    void wideLayoutUsesCohesiveTwoColumnHierarchy() {
        DashboardLayout.Layout layout = DashboardLayout.calculate(854, 480, 5, 4, 3);
        assertTrue(layout.wide());
        double leftShare = layout.nutrientSection().width()
            / (double) (layout.nutrientSection().width() + layout.recentFoods().width());
        assertTrue(leftShare >= 0.65 && leftShare <= 0.69);
        assertEquals(layout.nutrientSection().y(), layout.guidance().y());
        assertTrue(layout.guidance().bottom() < layout.recentFoods().y());
        assertTrue(layout.recentFoods().bottom() < layout.modifiers().y());
        assertTrue(layout.summary().bottom() < layout.nutrientSection().y());
    }

    @Test
    void compactLayoutStacksEverySectionInsideOneScrollFlow() {
        DashboardLayout.Layout compact = DashboardLayout.calculate(540, 360, 5, 4, 2);
        assertFalse(compact.wide());
        assertEquals(compact.nutrientSection().x(), compact.guidance().x());
        assertEquals(compact.guidance().x(), compact.recentFoods().x());
        assertEquals(compact.recentFoods().x(), compact.modifiers().x());
        assertTrue(compact.nutrientSection().bottom() < compact.guidance().y());
        assertTrue(compact.guidance().bottom() < compact.recentFoods().y());
        assertTrue(compact.recentFoods().bottom() < compact.modifiers().y());
    }

    @Test
    void nutrientRowsAreCompactAlignedAndContainedBySingleSection() {
        for (int groups : new int[] {0, 3, 5, 8, 18}) {
            DashboardLayout.Layout layout = DashboardLayout.calculate(427, 240, groups, 6, 3);
            assertEquals(groups, layout.nutrientRows().size());
            DashboardLayout.Rect previous = null;
            for (DashboardLayout.Rect row : layout.nutrientRows()) {
                assertEquals(DashboardLayout.NUTRIENT_ROW_HEIGHT, row.height());
                assertTrue(row.x() > layout.nutrientSection().x());
                assertTrue(row.right() < layout.nutrientSection().right());
                assertTrue(row.y() >= layout.nutrientSection().y());
                assertTrue(row.bottom() < layout.nutrientSection().bottom());
                if (previous != null) assertEquals(previous.bottom(), row.y());
                previous = row;
            }
        }
        DashboardLayout.Layout crowded = DashboardLayout.calculate(320, 180, 18, 6, 3);
        assertTrue(crowded.contentHeight() > crowded.viewport().height());
    }

    @Test
    void recentFoodsAndModifierStatesUseOnlyTheHeightTheyNeed() {
        DashboardLayout.Layout emptyNeutral = DashboardLayout.calculate(1280, 720, 5, 0, 0);
        DashboardLayout.Layout populatedActive = DashboardLayout.calculate(1280, 720, 5, 6, 3);
        assertTrue(populatedActive.recentFoods().height() > emptyNeutral.recentFoods().height());
        assertTrue(populatedActive.modifiers().height() > emptyNeutral.modifiers().height());
        assertTrue(emptyNeutral.contentHeight() <= emptyNeutral.viewport().height());
        assertTrue(populatedActive.contentHeight() <= populatedActive.viewport().height());
    }
}
