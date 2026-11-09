package com.itemlock.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menyimpan slot hotbar mana yang dikunci (0-8) dan menentukan klik mana yang harus diblokir.
 * Semuanya berjalan di sisi client, jadi server tidak perlu tahu apa-apa.
 */
public final class LockState {
	private static final boolean[] LOCKED = new boolean[9];

	private LockState() {
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("itemlock.txt");
	}

	/** Membaca daftar slot terkunci dari config/itemlock.txt (contoh isi: 0,3). */
	public static void load() {
		try {
			Path file = file();

			if (!Files.exists(file)) {
				return;
			}

			String text = Files.readString(file, StandardCharsets.UTF_8).trim();

			for (String part : text.split(",")) {
				try {
					int slot = Integer.parseInt(part.trim());

					if (slot >= 0 && slot < LOCKED.length) {
						LOCKED[slot] = true;
					}
				} catch (NumberFormatException ignored) {
					// bagian yang rusak dilewati saja
				}
			}
		} catch (IOException | RuntimeException e) {
			ItemLockClient.LOGGER.warn("Gagal membaca config Item Lock", e);
		}
	}

	private static void save() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < LOCKED.length; i++) {
			if (LOCKED[i]) {
				if (!sb.isEmpty()) {
					sb.append(',');
				}

				sb.append(i);
			}
		}

		try {
			Files.writeString(file(), sb.toString(), StandardCharsets.UTF_8);
		} catch (IOException | RuntimeException e) {
			ItemLockClient.LOGGER.warn("Gagal menyimpan config Item Lock", e);
		}
	}

	public static boolean isLocked(int hotbarSlot) {
		return hotbarSlot >= 0 && hotbarSlot < LOCKED.length && LOCKED[hotbarSlot];
	}

	public static void setLocked(int hotbarSlot, boolean locked) {
		if (hotbarSlot >= 0 && hotbarSlot < LOCKED.length) {
			LOCKED[hotbarSlot] = locked;
			save();
		}
	}

	/** Nomor slot hotbar (0-8) milik slot layar ini, atau -1 kalau bukan slot hotbar yang berisi item. */
	private static int hotbarIndexOf(Slot slot, Inventory inventory) {
		if (slot == null || slot.container != inventory) {
			return -1;
		}

		ItemStack stack = slot.getItem();

		if (stack.isEmpty()) {
			return -1;
		}

		for (int i = 0; i < LOCKED.length; i++) {
			if (inventory.getItem(i) == stack) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Dipanggil sebelum Minecraft memproses klik di layar inventory.
	 *
	 * @return nomor slot hotbar yang menyebabkan klik ini harus dibatalkan, atau -1 kalau klik boleh lanjut
	 */
	public static int blockedHotbarSlot(Slot slot, int button, ContainerInput input, Inventory inventory) {
		// Menyalin item (middle-click di Creative) dan menyebar item dari kursor tidak mengambil apa pun dari slot.
		String name = input.name();

		if (input == ContainerInput.CLONE || name.equals("QUICK_CRAFT")) {
			return -1;
		}

		// Klik dua kali mengumpulkan item sejenis dari SEMUA slot, termasuk yang terkunci.
		if (name.equals("PICKUP_ALL")) {
			for (int i = 0; i < LOCKED.length; i++) {
				if (LOCKED[i] && !inventory.getItem(i).isEmpty()) {
					return i;
				}
			}

			return -1;
		}

		// Klik, shift-click, Q, atau Ctrl+Q langsung pada slot terkunci.
		int index = hotbarIndexOf(slot, inventory);

		if (index >= 0 && LOCKED[index]) {
			return index;
		}

		// Tombol angka 1-9 menukar slot yang diklik dengan hotbar; jangan sampai menarik item terkunci keluar.
		if (input == ContainerInput.SWAP && button >= 0 && button < LOCKED.length && LOCKED[button] && !inventory.getItem(button).isEmpty()) {
			return button;
		}

		return -1;
	}
}
