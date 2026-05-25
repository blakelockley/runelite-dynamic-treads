package com.dynamictreads;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(DynamicTreadsConfig.GROUP)
public interface DynamicTreadsConfig extends Config
{
	String GROUP = "dynamictreads";

	enum DefaultBoot
	{
		AVERNIC_TREADS_MAX("Avernic treads (max)", 31097),
		AVERNIC_TREADS("Avernic treads", 31088),
		SPIKED_MANACLES("Spiked manacles", 23389);

		private final String displayName;
		private final int itemId;

		DefaultBoot(String displayName, int itemId)
		{
			this.displayName = displayName;
			this.itemId = itemId;
		}

		public int getItemId()
		{
			return itemId;
		}

		@Override
		public String toString()
		{
			return displayName;
		}
	}

	enum MeleeBoot
	{
		PRIMORDIAL_BOOTS("Primordial boots", 13239),
		DRAGON_BOOTS("Dragon boots", 11840),
		SPIKED_MANACLES("Spiked manacles", 23389);

		private final String displayName;
		private final int itemId;

		MeleeBoot(String displayName, int itemId)
		{
			this.displayName = displayName;
			this.itemId = itemId;
		}

		public int getItemId()
		{
			return itemId;
		}

		@Override
		public String toString()
		{
			return displayName;
		}
	}

	enum RangeBoot
	{
		PEGASIAN_BOOTS("Pegasian boots", 13237),
		RANGER_BOOTS("Ranger boots", 2577);

		private final String displayName;
		private final int itemId;

		RangeBoot(String displayName, int itemId)
		{
			this.displayName = displayName;
			this.itemId = itemId;
		}

		public int getItemId()
		{
			return itemId;
		}

		@Override
		public String toString()
		{
			return displayName;
		}
	}

	enum MageBoot
	{
		ETERNAL_BOOTS("Eternal boots", 13235),
		INFINITY_BOOTS("Infinity boots", 6920);

		private final String displayName;
		private final int itemId;

		MageBoot(String displayName, int itemId)
		{
			this.displayName = displayName;
			this.itemId = itemId;
		}

		public int getItemId()
		{
			return itemId;
		}

		@Override
		public String toString()
		{
			return displayName;
		}
	}

	@ConfigItem(
		keyName = "defaultBoot",
		name = "Default boot",
		description = "Shown when no weapon is equipped, or when the equipped Avernic Treads variant doesn't carry the wielded weapon's style.",
		position = 1
	)
	default DefaultBoot defaultBoot()
	{
		return DefaultBoot.AVERNIC_TREADS_MAX;
	}

	@ConfigItem(
		keyName = "meleeBoot",
		name = "Melee boot",
		description = "Shown when the equipped weapon is a melee weapon and the Avernic Treads variant permits a melee transmog.",
		position = 2
	)
	default MeleeBoot meleeBoot()
	{
		return MeleeBoot.PRIMORDIAL_BOOTS;
	}

	@ConfigItem(
		keyName = "rangeBoot",
		name = "Ranged boot",
		description = "Shown when the equipped weapon is a ranged weapon and the Avernic Treads variant permits a ranged transmog.",
		position = 3
	)
	default RangeBoot rangeBoot()
	{
		return RangeBoot.PEGASIAN_BOOTS;
	}

	@ConfigItem(
		keyName = "mageBoot",
		name = "Magic boot",
		description = "Shown when the equipped weapon is a magic weapon and the Avernic Treads variant permits a magic transmog.",
		position = 4
	)
	default MageBoot mageBoot()
	{
		return MageBoot.ETERNAL_BOOTS;
	}
}
