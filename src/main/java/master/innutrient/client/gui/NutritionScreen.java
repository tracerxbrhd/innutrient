package master.innutrient.client.gui;

import dev.uapi.client.UApiTabHost;
import dev.uapi.client.ui.components.UILabel;
import dev.uapi.client.ui.components.UIPanel;
import dev.uapi.client.ui.components.UIScrollContainer;
import dev.uapi.client.ui.core.UIBounds;
import dev.uapi.client.ui.core.UIComponent;
import dev.uapi.client.ui.core.UIContainer;
import dev.uapi.client.ui.core.UIScreen;
import dev.uapi.client.ui.theme.UITheme.ColorToken;
import dev.uapi.client.ui.theme.UIThemes;
import master.innutrient.Innutrient;
import master.innutrient.client.ClientNutritionCatalog;
import master.innutrient.network.NutritionRequestPayload;
import master.innutrient.nutrition.MealQuality;
import master.innutrient.nutrition.NutritionService;
import master.innutrient.player.NutritionAttachments;
import master.innutrient.player.NutritionState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/** One cohesive, responsive U-API Nutrition Dashboard. */
public final class NutritionScreen extends UIScreen implements UApiTabHost {
    private final boolean requestCatalog;
    private final long catalogRevision;
    private final List<NutrientRow> rows = new ArrayList<>();
    private final List<DashboardTooltipSource> tooltipSources = new ArrayList<>();
    private UIPanel panel;
    private UILabel titleLabel;
    private UIScrollContainer scroll;
    private NutritionSummaryCard summary;
    private NutrientBalanceSection nutrientSection;
    private DashboardGuidanceCard guidance;
    private DashboardSectionLabel nutrientTitle;
    private UILabel emptyGroupsLabel;
    private RecentFoodsCard recentFoods;
    private NutritionModifiersCard modifiers;
    private DashboardScrollBar scrollBar;
    private DashboardLayout.Layout currentLayout;

    public NutritionScreen() {
        this(true);
    }

    private NutritionScreen(boolean requestCatalog) {
        super(Component.translatable("screen.innutrient.title"));
        this.requestCatalog = requestCatalog;
        this.catalogRevision = ClientNutritionCatalog.revision();
    }

    @Override
    protected void buildUi(UIContainer root) {
        root.setTheme(UIThemes.ARCANE);
        panel = root.add(new UIPanel());
        titleLabel = panel.add(new UILabel(Component.translatable("screen.innutrient.dashboard.subtitle"),
            ColorToken.TEXT_PRIMARY));
        titleLabel.setShadow(true);
        scroll = panel.add(new UIScrollContainer());
        scroll.setWheelStep(DashboardLayout.NUTRIENT_ROW_HEIGHT);
        summary = scroll.add(new NutritionSummaryCard());
        nutrientSection = scroll.add(new NutrientBalanceSection());
        guidance = scroll.add(new DashboardGuidanceCard());
        nutrientTitle = scroll.add(new DashboardSectionLabel(
            Component.translatable("screen.innutrient.dashboard.nutrient_balance")));
        emptyGroupsLabel = scroll.add(new UILabel(Component.translatable("screen.innutrient.no_groups"),
            ColorToken.TEXT_MUTED));
        for (var group : ClientNutritionCatalog.groups()) rows.add(scroll.add(new NutrientRow(group)));
        recentFoods = scroll.add(new RecentFoodsCard());
        modifiers = scroll.add(new NutritionModifiersCard());
        scrollBar = panel.add(new DashboardScrollBar(scroll));

        tooltipSources.add(summary);
        tooltipSources.add(nutrientSection);
        tooltipSources.add(guidance);
        tooltipSources.addAll(rows);
        tooltipSources.add(recentFoods);
        tooltipSources.add(modifiers);
        DashboardTooltipLayer tooltips = root.add(new DashboardTooltipLayer(
            () -> List.copyOf(tooltipSources), () -> scroll == null ? UIBounds.EMPTY : scroll.bounds()));
        tooltips.setBounds(0, 0, 0, 0);
    }

    @Override
    protected void layoutUi(UIContainer root) {
        currentLayout = DashboardLayout.calculate(width, height, rows.size(), recentFoods.visibleFoodCount(),
            modifiers.visibleModifierCount());
        setBounds(panel, currentLayout.panel());
        DashboardLayout.Rect header = currentLayout.header();
        titleLabel.setBounds(header.x(), header.y() + 2, header.width(), font.lineHeight);
        setBounds(scroll, currentLayout.viewport());
        scroll.setContentHeight(currentLayout.contentHeight());
        scrollBar.setBounds(currentLayout.viewport().right() - 3, currentLayout.viewport().y(), 3,
            currentLayout.viewport().height());

        int offset = scroll.scrollOffset();
        setContentBounds(summary, currentLayout.summary(), offset);
        setContentBounds(nutrientSection, currentLayout.nutrientSection(), offset);
        setContentBounds(guidance, currentLayout.guidance(), offset);
        setContentBounds(nutrientTitle, currentLayout.nutrientTitle(), offset);
        emptyGroupsLabel.setVisible(rows.isEmpty());
        if (rows.isEmpty()) emptyGroupsLabel.setBounds(
            currentLayout.viewport().x() + currentLayout.nutrientSection().x() + 12,
            currentLayout.viewport().y() + currentLayout.nutrientSection().y() + 38 - offset,
            Math.max(0, currentLayout.nutrientSection().width() - 24), font.lineHeight);
        for (int index = 0; index < rows.size(); index++)
            setContentBounds(rows.get(index), currentLayout.nutrientRows().get(index), offset);
        setContentBounds(recentFoods, currentLayout.recentFoods(), offset);
        setContentBounds(modifiers, currentLayout.modifiers(), offset);
        root.children().getLast().setBounds(0, 0, width, height);
    }

    @Override
    protected void initScreen() {
        if (requestCatalog && minecraft != null && minecraft.getConnection() != null)
            PacketDistributor.sendToServer(NutritionRequestPayload.INSTANCE);
    }

    @Override
    protected void tickScreen() {
        if (minecraft == null) return;
        if (ClientNutritionCatalog.revision() != catalogRevision) {
            minecraft.setScreen(new NutritionScreen(false));
            return;
        }
        NutritionState state = minecraft.player == null ? NutritionState.empty()
            : minecraft.player.getData(NutritionAttachments.STATE);
        for (NutrientRow row : rows) row.setValue(state.get(row.group()));
        long gameTime = minecraft.level == null ? 0 : minecraft.level.getGameTime();
        var settings = ClientNutritionCatalog.dashboardSettings();
        var variety = NutritionService.variety(state, gameTime, ClientNutritionCatalog.groups(),
            settings.varietyWindowTicks(), settings.varietySampleTarget());
        double balance = NutritionService.balanceScore(state, ClientNutritionCatalog.groups());
        MealQuality lastMeal = state.dietMemory().isEmpty() ? null : state.dietMemory().getLast().mealQuality();
        summary.update(balance, state.dietQuality(), variety, lastMeal, settings);
        DashboardGuidance.Result context = DashboardGuidance.select(state, ClientNutritionCatalog.groups(), variety);
        guidance.update(context.message(), context.warning());
        int previousFoodCount = recentFoods.visibleFoodCount();
        int previousModifierCount = modifiers.visibleModifierCount();
        recentFoods.update(state.dietMemory(), gameTime);
        modifiers.update(state.dietQuality(), settings);
        if (previousFoodCount != recentFoods.visibleFoodCount()
            || previousModifierCount != modifiers.visibleModifierCount()) panel.invalidateLayout();
    }

    private void setContentBounds(UIComponent component, DashboardLayout.Rect rect, int offset) {
        component.setBounds(currentLayout.viewport().x() + rect.x(),
            currentLayout.viewport().y() + rect.y() - offset, rect.width(), rect.height());
    }

    private static void setBounds(UIComponent component, DashboardLayout.Rect rect) {
        component.setBounds(rect.x(), rect.y(), rect.width(), rect.height());
    }

    private DashboardLayout.Layout layoutSnapshot() {
        return currentLayout == null
            ? DashboardLayout.calculate(width, height, rows.size(),
                recentFoods == null ? 0 : recentFoods.visibleFoodCount(),
                modifiers == null ? 0 : modifiers.visibleModifierCount())
            : currentLayout;
    }

    @Override public ResourceLocation uApiTabId() { return Innutrient.id("nutrition"); }
    @Override public int uApiTabLeft() { return layoutSnapshot().panel().x(); }
    @Override public int uApiTabTop() { return Math.max(1, layoutSnapshot().panel().y() - 25); }
}
