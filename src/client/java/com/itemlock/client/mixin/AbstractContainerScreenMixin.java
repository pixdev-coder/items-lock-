package com.itemlock.client.mixin;

import com.itemlock.client.ItemLockClient;
import com.itemlock.client.LockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

/**
 * Semua klik di layar inventory/chest lewat slotClicked() sebelum dikirim ke server.
 * Kalau klik itu akan mengeluarkan item dari slot hotbar yang terkunci, klik dibatalkan di sini
 * dan server tidak pernah menerima apa-apa.
 *
 * Di Minecraft 26.3 ada DUA versi slotClicked, jadi masing-masing dapat handler sendiri
 * dengan deskriptor yang ditulis lengkap:
 *  - versi int          : dipanggil oleh tombol keyboard (Q, angka 1-9, F)
 *  - versi MouseButtonEvent : dipanggil oleh klik mouse
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
	@Inject(
		method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void itemlock$blockKeyboardClicks(Slot slot, int slotId, int button, ContainerInput containerInput, CallbackInfo ci) {
		itemlock$check(slot, button, containerInput, ci);
	}

	@Inject(
		method = "slotClicked(Lnet/minecraft/world/inventory/Slot;ILnet/minecraft/client/input/MouseButtonEvent;Lnet/minecraft/world/inventory/ContainerInput;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void itemlock$blockMouseClicks(Slot slot, int slotId, MouseButtonEvent event, ContainerInput containerInput, CallbackInfo ci) {
		itemlock$check(slot, event.button(), containerInput, ci);
	}

	private static void itemlock$check(Slot slot, int button, ContainerInput containerInput, CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null) {
			return;
		}

		int blocked = LockState.blockedHotbarSlot(slot, button, containerInput, player.getInventory());

		if (blocked >= 0) {
			ItemLockClient.notifyBlocked(blocked);
			ci.cancel();
		}
	}
}
