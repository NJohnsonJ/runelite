/*
 * Copyright (c) 2020 Jordan <nightfirecat@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.itemstats;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import java.awt.Color;

import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.Text;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;
import org.apache.commons.lang3.StringUtils;
import static net.runelite.client.plugins.itemstats.ItemStatOverlay.UNARMED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ItemStatOverlayTest
{
	// Weapon definitions
	private static final ItemStats ABYSSAL_DAGGER = new ItemStats(false, true, 0.453, 8,
		ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.isTwoHanded(false)
			.astab(75)
			.aslash(40)
			.acrush(-4)
			.amagic(1)
			.dmagic(1)
			.str(75)
			.aspeed(4)
			.build());
	private static final ItemStats KATANA = new ItemStats(false, true, 0, 8,
		ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.isTwoHanded(true)
			.astab(7)
			.aslash(45)
			.dstab(3)
			.dslash(7)
			.dcrush(7)
			.drange(-3)
			.str(40)
			.aspeed(4)
			.build());
	private static final ItemStats BLOWPIPE = new ItemStats(false, true, 0, 0,
		ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.isTwoHanded(true)
			.arange(60)
			.rstr(40)
			.aspeed(3)
			.build());
	private static final ItemStats HEAVY_BALLISTA = new ItemStats(false, true, 4, 8,
		ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.isTwoHanded(true)
			.arange(110)
			.aspeed(7)
			.build());
	private static final ItemStats DFS = new ItemStats(false, true, 7.257, 8,
			ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.SHIELD.getSlotIdx())
			.amagic(-10)
			.arange(-5)
			.dstab(20)
			.dslash(25)
			.dcrush(22)
			.dmagic(10)
			.drange(22)
			.str(7)
			.build());
	private static final ItemStats GRACEFUL_CAPE = new ItemStats(false, true, -4, 0,
		ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.CAPE.getSlotIdx())
			.build());

	@Inject
	ItemStatOverlay overlay;

	@Mock
	@Bind
	Client client;

	@Mock
	@Bind
	ItemStatConfig config;

	@Mock
	@Bind
	ItemManager itemManager;

	@Before
	public void before()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);

		when(config.colorBetterUncapped()).thenReturn(new Color(0));
		when(config.colorWorse()).thenReturn(new Color(0));
	}

	@Test
	public void testUnarmedAttackSpeed()
	{
		assertEquals(ItemStatOverlay.UNARMED.getEquipment().getAspeed(), ABYSSAL_DAGGER.getEquipment().getAspeed());
		assertEquals(ItemStatOverlay.UNARMED.getEquipment().getAspeed(), KATANA.getEquipment().getAspeed());
		assertEquals(-1, BLOWPIPE.getEquipment().getAspeed() - ItemStatOverlay.UNARMED.getEquipment().getAspeed());
		assertEquals(3, HEAVY_BALLISTA.getEquipment().getAspeed() - ItemStatOverlay.UNARMED.getEquipment().getAspeed());
	}

	@Test
	public void testBuildStatBonusString()
	{
		// Empty equipment (fully unarmed)
		final ItemContainer equipment = mock(ItemContainer.class);
		when(client.getItemContainer(InventoryID.EQUIPMENT)).thenReturn(equipment);

		String tooltip;
		String sanitizedTooltip;

		tooltip = overlay.equipItemTooltip(ABYSSAL_DAGGER);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Stab: +75"));
		assertTrue(sanitizedTooltip.contains("Slash: +40"));
		assertTrue(sanitizedTooltip.contains("Crush: -4"));
		assertEquals(2, StringUtils.countMatches(sanitizedTooltip, "Magic: +1")); // Attack and defense
		assertTrue(sanitizedTooltip.contains("Melee Str: +75"));
		assertFalse(sanitizedTooltip.contains("Speed:"));

		tooltip = overlay.equipItemTooltip(KATANA);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Stab: +7"));
		assertTrue(sanitizedTooltip.contains("Slash: +45"));
		assertTrue(sanitizedTooltip.contains("Stab: +3")); // Defense
		assertTrue(sanitizedTooltip.contains("Slash: +7")); // Defense
		assertTrue(sanitizedTooltip.contains("Crush: +7")); // Defense
		assertTrue(sanitizedTooltip.contains("Range: -3")); // Defense
		assertTrue(sanitizedTooltip.contains("Melee Str: +40"));
		assertFalse(sanitizedTooltip.contains("Speed:"));

		tooltip = overlay.equipItemTooltip(BLOWPIPE);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Range: +60"));
		assertTrue(sanitizedTooltip.contains("Range Str: +40"));
		assertTrue(sanitizedTooltip.contains("Speed: -1"));
		assertFalse(sanitizedTooltip.contains("Stab:"));

		tooltip = overlay.equipItemTooltip(HEAVY_BALLISTA);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Range: +110"));
		assertTrue(sanitizedTooltip.contains("Speed: +3"));
		assertFalse(sanitizedTooltip.contains("Stab:"));
	}

	@Test
	public void testEquipItemTooltip_2HBothHandsFull()
	{
		// Empty equipment (fully unarmed)
		final ItemContainer equipment = mock(ItemContainer.class);
		when(client.getItemContainer(InventoryID.EQUIPMENT)).thenReturn(equipment);

		when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()))
				.thenReturn(new Item(0, 0));
		when(itemManager.getItemStats(0, false))
				.thenReturn(ABYSSAL_DAGGER);

		when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx()))
				.thenReturn(new Item(1, 0));
		when(itemManager.getItemStats(1, false))
				.thenReturn(DFS);

		final String tooltip = overlay.equipItemTooltip(BLOWPIPE);
		final String sanitizedTooltip = Text.sanitizeMultilineText(tooltip);

		// Attack Bonuses
		assertTrue(sanitizedTooltip.contains("Speed: -1"));
		assertTrue(sanitizedTooltip.contains("Melee Str: -82"));
		assertTrue(sanitizedTooltip.contains("Range Str: +40"));
		assertTrue(sanitizedTooltip.contains("Stab: -75"));
		assertTrue(sanitizedTooltip.contains("Slash: -40"));
		assertTrue(sanitizedTooltip.contains("Crush: +4"));
		assertTrue(sanitizedTooltip.contains("Magic: +9"));
		assertTrue(sanitizedTooltip.contains("Range: +65"));
		// Defense Bonuses
		assertTrue(sanitizedTooltip.contains("Stab: -20"));
		assertTrue(sanitizedTooltip.contains("Slash: -25"));
		assertTrue(sanitizedTooltip.contains("Crush: -22"));
		assertTrue(sanitizedTooltip.contains("Magic: -11"));
		assertTrue(sanitizedTooltip.contains("Range: -22"));
	}

	@Test
	public void testEquipItemTooltip_2HOffHandFull()
	{
		// Empty equipment (fully unarmed)
		final ItemContainer equipment = mock(ItemContainer.class);
		when(client.getItemContainer(InventoryID.EQUIPMENT)).thenReturn(equipment);

		when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()))
				.thenReturn(null);

		when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx()))
				.thenReturn(new Item(1, 0));
		when(itemManager.getItemStats(1, false))
				.thenReturn(DFS);

		final String tooltip = overlay.equipItemTooltip(BLOWPIPE);
		final String sanitizedTooltip = Text.sanitizeMultilineText(tooltip);

		// Attack Bonuses
		assertTrue(sanitizedTooltip.contains("Speed: -1"));
		assertTrue(sanitizedTooltip.contains("Melee Str: -7"));
		assertTrue(sanitizedTooltip.contains("Range Str: +40"));
		assertTrue(sanitizedTooltip.contains("Range: +65"));
		// Defense Bonuses
		assertTrue(sanitizedTooltip.contains("Stab: -20"));
		assertTrue(sanitizedTooltip.contains("Slash: -25"));
		assertTrue(sanitizedTooltip.contains("Crush: -22"));
		assertTrue(sanitizedTooltip.contains("Magic: -10"));
		assertTrue(sanitizedTooltip.contains("Range: -22"));
	}

	@Test
	public void testEquipItemTooltip_ShowBaseStats()
	{
		when(config.alwaysShowBaseStats()).thenReturn(true);

		// Empty equipment (fully unarmed)
		final ItemContainer equipment = mock(ItemContainer.class);
		when(client.getItemContainer(InventoryID.EQUIPMENT)).thenReturn(equipment);

		String tooltip;
		String sanitizedTooltip;

		tooltip = overlay.equipItemTooltip(ABYSSAL_DAGGER);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Stab: 75 (+75)"));
		assertTrue(sanitizedTooltip.contains("Slash: 40 (+40)"));
		assertTrue(sanitizedTooltip.contains("Crush: -4 (-4)"));
		assertEquals(2, StringUtils.countMatches(sanitizedTooltip, "Magic: 1 (+1)")); // Attack and defense
		assertTrue(sanitizedTooltip.contains("Melee Str: 75 (+75)"));
		assertTrue(sanitizedTooltip.contains("Speed: 4"));

		tooltip = overlay.equipItemTooltip(KATANA);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Stab: 7 (+7)"));
		assertTrue(sanitizedTooltip.contains("Slash: 45 (+45)"));
		assertTrue(sanitizedTooltip.contains("Stab: 3 (+3)")); // Defense
		assertTrue(sanitizedTooltip.contains("Slash: 7 (+7)")); // Defense
		assertTrue(sanitizedTooltip.contains("Crush: 7 (+7)")); // Defense
		assertTrue(sanitizedTooltip.contains("Range: -3 (-3)")); // Defense
		assertTrue(sanitizedTooltip.contains("Melee Str: 40 (+40)"));
		assertTrue(sanitizedTooltip.contains("Speed: 4"));

		tooltip = overlay.equipItemTooltip(BLOWPIPE);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Range: 60 (+60)"));
		assertTrue(sanitizedTooltip.contains("Range Str: 40 (+40)"));
		assertTrue(sanitizedTooltip.contains("Speed: 3 (-1)"));
		assertFalse(sanitizedTooltip.contains("Stab:"));

		tooltip = overlay.equipItemTooltip(HEAVY_BALLISTA);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Range: 110 (+110)"));
		assertTrue(sanitizedTooltip.contains("Speed: 7 (+3)"));
		assertFalse(sanitizedTooltip.contains("Stab:"));
	}

	@Test
	public void testUnequipItemTooltip()
	{
		String tooltip = overlay.unequipItemTooltip(ABYSSAL_DAGGER);
		String sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Stab: -75"));
		assertTrue(sanitizedTooltip.contains("Slash: -40"));
		assertTrue(sanitizedTooltip.contains("Crush: +4"));
		assertEquals(2, StringUtils.countMatches(sanitizedTooltip, "Magic: -1")); // Attack and defense
		assertTrue(sanitizedTooltip.contains("Melee Str: -75"));
		assertFalse(sanitizedTooltip.contains("Speed:"));

		tooltip = overlay.unequipItemTooltip(KATANA);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Stab: -7"));
		assertTrue(sanitizedTooltip.contains("Slash: -45"));
		assertTrue(sanitizedTooltip.contains("Stab: -3")); // Defense
		assertTrue(sanitizedTooltip.contains("Slash: -7")); // Defense
		assertTrue(sanitizedTooltip.contains("Crush: -7")); // Defense
		assertTrue(sanitizedTooltip.contains("Range: +3")); // Defense
		assertTrue(sanitizedTooltip.contains("Melee Str: -40"));
		assertFalse(sanitizedTooltip.contains("Speed:"));

		tooltip = overlay.unequipItemTooltip(BLOWPIPE);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Range: -60"));
		assertTrue(sanitizedTooltip.contains("Range Str: -40"));
		assertTrue(sanitizedTooltip.contains("Speed: +1"));
		assertFalse(sanitizedTooltip.contains("Stab:"));

		tooltip = overlay.unequipItemTooltip(HEAVY_BALLISTA);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Range: -110"));
		assertTrue(sanitizedTooltip.contains("Speed: -3"));
		assertFalse(sanitizedTooltip.contains("Stab:"));
	}

	@Test
	public void testUnequipItemTooltip_Weapon()
	{
		final String tooltip = overlay.unequipItemTooltip(HEAVY_BALLISTA);
		final String sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Range: -110"));
		assertTrue(sanitizedTooltip.contains("Speed: -3"));

		assertFalse(sanitizedTooltip.contains("Range Str:"));
		assertFalse(sanitizedTooltip.contains("Stab:"));
		assertFalse(sanitizedTooltip.contains("Crush:"));
		assertFalse(sanitizedTooltip.contains("Slash:"));
		assertFalse(sanitizedTooltip.contains("Magic:"));
	}

	@Test
	public void testUnequipItemTooltip_ShowBaseStats()
	{
		when(config.alwaysShowBaseStats()).thenReturn(true);

		String tooltip = overlay.unequipItemTooltip(ABYSSAL_DAGGER);
		String sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Stab: 75 (-75)"));
		assertTrue(sanitizedTooltip.contains("Slash: 40 (-40)"));
		assertTrue(sanitizedTooltip.contains("Crush: -4 (+4)"));
		assertEquals(2, StringUtils.countMatches(sanitizedTooltip, "Magic: 1 (-1)")); // Attack and defense
		assertTrue(sanitizedTooltip.contains("Melee Str: 75 (-75)"));
		assertTrue(sanitizedTooltip.contains("Speed: 4"));

		tooltip = overlay.unequipItemTooltip(KATANA);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Stab: 7 (-7)"));
		assertTrue(sanitizedTooltip.contains("Slash: 45 (-45)"));
		assertTrue(sanitizedTooltip.contains("Stab: 3 (-3)")); // Defense
		assertTrue(sanitizedTooltip.contains("Slash: 7 (-7)")); // Defense
		assertTrue(sanitizedTooltip.contains("Crush: 7 (-7)")); // Defense
		assertTrue(sanitizedTooltip.contains("Range: -3 (+3)")); // Defense
		assertTrue(sanitizedTooltip.contains("Melee Str: 40 (-40)"));
		assertTrue(sanitizedTooltip.contains("Speed: 4"));

		tooltip = overlay.unequipItemTooltip(BLOWPIPE);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Range: 60 (-60)"));
		assertTrue(sanitizedTooltip.contains("Range Str: 40 (-40)"));
		assertTrue(sanitizedTooltip.contains("Speed: 3 (+1)"));
		assertFalse(sanitizedTooltip.contains("Stab:"));


		tooltip = overlay.unequipItemTooltip(HEAVY_BALLISTA);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Range: 110 (-110)"));
		assertTrue(sanitizedTooltip.contains("Speed: 7 (-3)"));

		assertFalse(sanitizedTooltip.contains("Range Str:"));
		assertFalse(sanitizedTooltip.contains("Stab:"));
		assertFalse(sanitizedTooltip.contains("Crush:"));
		assertFalse(sanitizedTooltip.contains("Slash:"));
		assertFalse(sanitizedTooltip.contains("Magic:"));
	}

	@Test
	public void testEquipItemTooltip_ShowWeight()
	{
		when(config.showWeight()).thenReturn(true);

		// Empty equipment (fully unarmed)
		final ItemContainer equipment = mock(ItemContainer.class);
		when(client.getItemContainer(InventoryID.EQUIPMENT)).thenReturn(equipment);

		String tooltip = overlay.buildWeightRow(ABYSSAL_DAGGER.getWeight(), ItemID.ABYSSAL_DAGGER, true);
		String sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Weight: 0.453"));

		tooltip = overlay.buildWeightRow(ABYSSAL_DAGGER.getWeight(), ItemID.ABYSSAL_DAGGER, false);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Weight: 0.453"));

		// Equip
		tooltip = overlay.buildWeightRow(GRACEFUL_CAPE.getWeight(), ItemID.GRACEFUL_CAPE, true);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Weight: -4"));

		//Unequip
		tooltip = overlay.buildWeightRow(GRACEFUL_CAPE.getWeight(), ItemID.GRACEFUL_CAPE, false);
		sanitizedTooltip = Text.sanitizeMultilineText(tooltip);
		assertTrue(sanitizedTooltip.contains("Weight: +4"));
	}
}
