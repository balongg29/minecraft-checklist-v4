package com.farmlist;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Icon buttons (item icons with a hover tip) that use the empty space at the sides of the panel.
 * When the screen is too narrow for side columns, the icons go in one row inside the panel instead.
 * "Left" tools go in the left column, "right" tools in the right column.
 */
final class ToolBar {

    static final int S = 22;
    private static final int GAP = 2;
    private static final int SIDE_MARGIN = 5;

    private record Tool(ItemStack icon, String tip, Runnable action, boolean right, int tint) {
    }

    private final List<Tool> tools = new ArrayList<>();
    private int[] xs = new int[0];
    private int[] ys = new int[0];
    private boolean side = true;

    /** tint: 0 normal, otherwise an ARGB color drawn behind the icon (used for the red confirm-delete state). */
    ToolBar add(Item icon, String tip, boolean right, int tint, Runnable action) {
        tools.add(new Tool(new ItemStack(icon), tip, action, right, tint));
        return this;
    }

    ToolBar add(Item icon, String tip, boolean right, Runnable action) {
        return add(icon, tip, right, 0, action);
    }

    boolean isSide() {
        return side;
    }

    /** Height the inline row needs inside the panel (0 when the icons sit at the sides). */
    int inlineHeight() {
        return side ? 0 : S + 4;
    }

    /** Works out where every icon goes. Call from init() before using isSide(). */
    void layout(int px, int py, int pw, int screenW) {
        boolean anyRight = tools.stream().anyMatch(Tool::right);
        int need = S + SIDE_MARGIN + 4;
        side = px >= need && (!anyRight || screenW - (px + pw) >= need);
        xs = new int[tools.size()];
        ys = new int[tools.size()];
        if (side) {
            int l = 0;
            int r = 0;
            for (int i = 0; i < tools.size(); i++) {
                if (tools.get(i).right()) {
                    xs[i] = px + pw + SIDE_MARGIN;
                    ys[i] = py + 4 + r++ * (S + GAP);
                } else {
                    xs[i] = px - SIDE_MARGIN - S;
                    ys[i] = py + 4 + l++ * (S + GAP);
                }
            }
        } else {
            int total = tools.size() * (S + GAP) - GAP;
            int x = px + (pw - total) / 2;
            for (int i = 0; i < tools.size(); i++) {
                xs[i] = x + i * (S + GAP);
                ys[i] = py + 18;
            }
        }
    }

    boolean click(int mx, int my) {
        for (int i = 0; i < tools.size(); i++) {
            if (Ui.in(mx, my, xs[i], ys[i], S, S)) {
                tools.get(i).action().run();
                return true;
            }
        }
        return false;
    }

    void draw(GuiGraphicsExtractor g, Font font, int mx, int my, int screenW, int screenH) {
        int hovered = -1;
        for (int i = 0; i < tools.size(); i++) {
            Tool t = tools.get(i);
            boolean hover = Ui.in(mx, my, xs[i], ys[i], S, S);
            if (hover) hovered = i;
            Ui.iconButton(g, xs[i], ys[i], S, hover, t.tint());
            Ui.icon(g, t.icon(), xs[i] + (S - 16) / 2, ys[i] + (S - 16) / 2);
        }
        if (hovered >= 0) {
            Ui.tip(g, font, tools.get(hovered).tip(), mx, my, screenW, screenH);
        }
    }
}
