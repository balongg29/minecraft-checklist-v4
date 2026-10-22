package com.farmlist;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Share one checklist: choose "list only" or "with progress", then either copy a code
 * or whisper it to a player (they click a chat message to add it).
 */
public class ShareScreen extends Screen {

    private static final int PANEL_W = 240;
    private static final int PANEL_H = 148;

    private final String fileName;
    private boolean withProgress = false;
    private String playerText = "";

    private Button modeButton;
    private EditBox nameBox;
    private int px, py;

    public ShareScreen(String fileName) {
        super(Component.literal("Share"));
        this.fileName = fileName;
    }

    private Component modeLabel() {
        return Component.literal(withProgress ? "Sharing: list + progress" : "Sharing: list only");
    }

    @Override
    protected void init() {
        px = (this.width - PANEL_W) / 2;
        py = (this.height - PANEL_H) / 2;
        int inner = PANEL_W - 20;

        modeButton = Button.builder(modeLabel(), b -> {
            withProgress = !withProgress;
            b.setMessage(modeLabel());
        }).bounds(px + 10, py + 22, inner, 20).build();
        addRenderableWidget(modeButton);

        addRenderableWidget(Button.builder(Component.literal("Copy code"), b ->
                Share.copyCode(fileName, false, withProgress)
        ).bounds(px + 10, py + 46, inner, 20).build());

        nameBox = new EditBox(this.font, px + 10, py + 84, inner, 20, Component.literal("Player names"));
        nameBox.setMaxLength(120);
        nameBox.setValue(playerText);
        nameBox.setResponder(t -> playerText = t);
        addRenderableWidget(nameBox);

        int bw = (inner - 8) / 3;
        addRenderableWidget(Button.builder(Component.literal("Send"), b ->
                ChatShare.send(fileName, withProgress, names())
        ).bounds(px + 10, py + 108, bw, 20).build());

        addRenderableWidget(Button.builder(Component.literal("All online"), b -> {
            List<String> online = ChatShare.onlineNames();
            if (online.isEmpty()) {
                FarmListClient.message("No other players are online.");
            } else {
                ChatShare.send(fileName, withProgress, online);
            }
        }).bounds(px + 10 + bw + 4, py + 108, bw, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Back"), b ->
                this.minecraft.gui.setScreen(new ChecklistScreen(fileName))
        ).bounds(px + 10 + 2 * (bw + 4), py + 108, bw, 20).build());
    }

    /** Names typed in the box, separated by commas or spaces. */
    private List<String> names() {
        List<String> out = new ArrayList<>();
        for (String n : playerText.trim().split("[,\\s]+")) {
            if (!n.isEmpty() && !out.contains(n)) out.add(n);
        }
        return out;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        Ui.panel(g, px, py, PANEL_W, PANEL_H);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        Ui.textCenter(g, this.font, "Share \"" + Ui.fit(this.font, FarmListClient.baseName(fileName), PANEL_W - 70) + "\"",
                px + PANEL_W / 2, py + 8, Ui.TEXT_DARK, false);
        Ui.text(g, this.font, "Players, separated by commas (they need this mod):", px + 10, py + 72, Ui.TEXT_MUTED, false);
        if (!FarmListClient.status.isEmpty()) {
            Ui.text(g, this.font, Ui.fit(this.font, FarmListClient.status, PANEL_W - 20),
                    px + 10, py + PANEL_H - 14, Ui.TEXT_DARK, false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
