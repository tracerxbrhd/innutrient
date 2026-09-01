package master.innutrient.client.gui;

import dev.uapi.client.UApiTabHost;
import dev.uapi.client.ui.components.UILabel;
import dev.uapi.client.ui.components.UIPanel;
import dev.uapi.client.ui.components.UIScrollContainer;
import dev.uapi.client.ui.core.UIContainer;
import dev.uapi.client.ui.core.UIScreen;
import dev.uapi.client.ui.theme.UITheme.ColorToken;
import master.innutrient.Innutrient;
import master.innutrient.client.ClientNutritionCatalog;
import master.innutrient.network.NutritionRequestPayload;
import master.innutrient.nutrition.NutritionService;
import master.innutrient.nutrition.NutrientStatus;
import master.innutrient.player.NutritionAttachments;
import master.innutrient.player.NutritionState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class NutritionScreen extends UIScreen implements UApiTabHost {
    private static final int ROW_HEIGHT = 40;
    private static final int ROW_GAP = 5;
    private final boolean requestCatalog;
    private final long catalogRevision;
    private final List<NutrientRow> rows = new ArrayList<>();
    private UIPanel panel;
    private UILabel titleLabel;
    private UILabel balanceLabel;
    private UILabel qualityLabel;
    private UILabel varietyLabel;
    private UILabel contextLabel;
    private UIScrollContainer scroll;

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
        panel = root.add(new UIPanel());
        titleLabel = panel.add(new UILabel(Component.translatable("screen.innutrient.title"), ColorToken.TEXT_PRIMARY));
        titleLabel.setShadow(true);
        balanceLabel = panel.add(new UILabel(Component.empty(), ColorToken.ACCENT_SUCCESS));
        qualityLabel = panel.add(new UILabel(Component.empty(), ColorToken.TEXT_PRIMARY));
        varietyLabel = panel.add(new UILabel(Component.empty(), ColorToken.TEXT_PRIMARY));
        contextLabel = panel.add(new UILabel(Component.empty(), ColorToken.TEXT_MUTED));
        scroll = panel.add(new UIScrollContainer());
        scroll.setWheelStep(ROW_HEIGHT + ROW_GAP);
        for (var group : ClientNutritionCatalog.groups()) rows.add(scroll.add(new NutrientRow(group)));
    }

    @Override
    protected void layoutUi(UIContainer root) {
        int panelWidth = Math.min(360, Math.max(220, width - 32));
        int panelHeight = Math.min(346, Math.max(186, height - 48));
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        panel.setBounds(left, top, panelWidth, panelHeight);
        titleLabel.setBounds(left + 12, top + 12, panelWidth - 24, 12);
        balanceLabel.setBounds(left + 12, top + 30, panelWidth - 24, 12);
        qualityLabel.setBounds(left + 12, top + 46, panelWidth - 24, 12);
        varietyLabel.setBounds(left + 12, top + 62, panelWidth - 24, 12);
        contextLabel.setBounds(left + 12, top + 78, panelWidth - 24, 12);
        scroll.setBounds(left + 10, top + 98, panelWidth - 20, panelHeight - 108);
        int contentHeight = rows.isEmpty() ? 24 : rows.size() * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;
        scroll.setContentHeight(contentHeight);
        int rowY = scroll.bounds().y() - scroll.scrollOffset();
        for (NutrientRow row : rows) {
            row.setBounds(scroll.bounds().x(), rowY, scroll.bounds().width(), ROW_HEIGHT);
            rowY += ROW_HEIGHT + ROW_GAP;
        }
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
        balanceLabel.setText(Component.translatable("screen.innutrient.balance",
            String.format(Locale.ROOT, "%.0f%%",
                NutritionService.balanceScore(state, ClientNutritionCatalog.groups()))));
        qualityLabel.setText(Component.translatable("screen.innutrient.diet_quality",
            Component.translatable(state.dietQuality().translationKey())));
        long gameTime = minecraft.level == null ? 0 : minecraft.level.getGameTime();
        var variety = NutritionService.variety(state, gameTime, ClientNutritionCatalog.groups());
        varietyLabel.setText(Component.translatable("screen.innutrient.variety",
            String.format(Locale.ROOT, "%.0f%%", variety.value()),
            Component.translatable(variety.tier().translationKey())));
        var low = ClientNutritionCatalog.groups().stream()
            .filter(group -> group.penalizeLow() && (group.status(state.get(group)) == NutrientStatus.DEFICIENT
                || group.status(state.get(group)) == NutrientStatus.BELOW_TARGET)).findFirst();
        var high = ClientNutritionCatalog.groups().stream()
            .filter(group -> group.penalizeHigh() && group.status(state.get(group)) == NutrientStatus.EXCESSIVE)
            .findFirst();
        contextLabel.setText(low.map(group -> Component.translatable("screen.innutrient.context.low",
                Component.translatable(group.translationKey())))
            .orElseGet(() -> high.map(group -> Component.translatable("screen.innutrient.context.high",
                    Component.translatable(group.translationKey())))
                .orElseGet(() -> Component.translatable("screen.innutrient.context.ok"))));
    }

    @Override
    protected void renderScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (NutrientRow row : rows) {
            if (row.bounds().contains(mouseX, mouseY) && row.bounds().intersects(scroll.bounds())) {
                graphics.renderTooltip(font, row.tooltip(), Optional.empty(), mouseX, mouseY);
                break;
            }
        }
        if (rows.isEmpty()) graphics.drawCenteredString(font,
            Component.translatable("screen.innutrient.no_groups"), width / 2, height / 2, 0xFFE0E0E0);
    }

    private int panelLeft() {
        int panelWidth = Math.min(360, Math.max(220, width - 32));
        return (width - panelWidth) / 2;
    }

    private int panelTop() {
        int panelHeight = Math.min(346, Math.max(186, height - 48));
        return (height - panelHeight) / 2;
    }

    @Override public ResourceLocation uApiTabId() { return Innutrient.id("nutrition"); }
    @Override public int uApiTabLeft() { return panelLeft(); }
    @Override public int uApiTabTop() { return Math.max(1, panelTop() - 25); }
}
