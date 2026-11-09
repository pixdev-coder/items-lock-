package com.itemlock.client;

import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.sdl.SDLScancode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Item Lock, versi client-only: kunci disimpan per slot hotbar di komputer sendiri,
 * jadi mod ini jalan di server mana pun tanpa perlu dipasang di server.
 */
public class ItemLockClient implements ClientModInitializer {
	public static final String MOD_ID = "itemlock";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final int GOLD = 0xFFFFAA00;
	private static final int BLACK = 0xFF000000;
	private static final int SHADOW = 0xAA000000;

	private static KeyMapping toggleKey;

	@Override
	public void onInitializeClient() {
		LockState.load();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

		// Tombol bawaan: K (bisa diubah di Options > Controls > Key Binds).
		toggleKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.itemlock.toggle", InputConstants.Type.KEYBOARD, SDLScancode.SDL_SCANCODE_K, category)
		);

		// Awal tick: kalau slot yang sedang dipilih terkunci, buang penekanan tombol drop (Q / Ctrl+Q)
		// sebelum Minecraft sempat memprosesnya. Tidak ada paket yang dikirim ke server.
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			LocalPlayer player = client.player;

			if (player == null) {
				return;
			}

			int selected = player.getInventory().getSelectedSlot();

			if (LockState.isLocked(selected)) {
				boolean pressed = false;

				while (client.options.keyDrop.consumeClick()) {
					pressed = true;
				}

				if (pressed && !player.getInventory().getSelectedItem().isEmpty()) {
					notifyBlocked(selected);
				}
			}
		});

		// Akhir tick: tombol kunci ditekan -> kunci / buka kunci slot yang sedang dipilih.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				if (client.player != null) {
					toggleSelected(client.player.getInventory());
				}
			}
		});

		// Gambar gembok kecil di slot hotbar yang terkunci.
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.HOTBAR,
				Identifier.fromNamespaceAndPath(MOD_ID, "hotbar_locks"),
				ItemLockClient::extractLocks
		);
	}

	private static void toggleSelected(Inventory inventory) {
		int slot = inventory.getSelectedSlot();
		ItemStack held = inventory.getSelectedItem();

		if (LockState.isLocked(slot)) {
			LockState.setLocked(slot, false);
			showMessage(Component.translatable("message.itemlock.unlocked", slot + 1));
		} else if (held.isEmpty()) {
			showMessage(Component.translatable("message.itemlock.nothing_held"));
		} else {
			LockState.setLocked(slot, true);
			showMessage(Component.translatable("message.itemlock.locked", slot + 1, held.getHoverName()));
		}
	}

	/** Memberi tahu pemain bahwa sebuah aksi dibatalkan karena slotnya terkunci. */
	public static void notifyBlocked(int hotbarSlot) {
		showMessage(Component.translatable("message.itemlock.blocked", hotbarSlot + 1));
	}

	private static void showMessage(Component message) {
		Minecraft.getInstance().gui.hud.setOverlayMessage(message, false);
	}

	private static void extractLocks(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null) {
			return;
		}

		Inventory inventory = player.getInventory();
		int left = graphics.guiWidth() / 2 - 90;
		int top = graphics.guiHeight() - 19;

		for (int i = 0; i < 9; i++) {
			if (LockState.isLocked(i) && !inventory.getItem(i).isEmpty()) {
				drawPadlock(graphics, left + i * 20 + 2 + 9, top - 1);
			}
		}
	}

	/** Gembok kecil 7x8 piksel, digambar dari beberapa persegi panjang. */
	private static void drawPadlock(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.fill(x, y, x + 7, y + 8, SHADOW);
		// busur gembok
		graphics.fill(x + 2, y + 1, x + 5, y + 2, GOLD);
		graphics.fill(x + 2, y + 2, x + 3, y + 4, GOLD);
		graphics.fill(x + 4, y + 2, x + 5, y + 4, GOLD);
		// badan gembok dan lubang kunci
		graphics.fill(x + 1, y + 4, x + 6, y + 7, GOLD);
		graphics.fill(x + 3, y + 5, x + 4, y + 6, BLACK);
	}
}
