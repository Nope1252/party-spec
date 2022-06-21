package com.partyspec;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("partyspec")
public interface PartySpecConfig extends Config
{
	@ConfigItem(
			position = 1,
			keyName = "whitelist",
			name = "Whitelist",
			description = "Only names listed will have their spec% shown. If the list is empty, all connected party members will show up")
	default String getWhitelist()
	{
		return "";
	}

	@Range(max=16, min=8)
	@ConfigItem(
			position=6,
			keyName="fontSize",
			name="Font Size",
			description="Font size")
	default int fontSize() {
		return 12;
	}

	@ConfigItem(
			position = 7,
			keyName = "offSetTextHorizontal",
			name = "OffSet Text Horizontal",
			description = "Used to offset the text horizontally")
	default int offSetTextHorizontal() { return 0; }

	@ConfigItem(
			position = 8,
			keyName = "offSetTextVertical",
			name = "OffSet Text Vertical",
			description = "Used to offset the text vertically")
	default int offSetTextVertical() { return 0; }

	@ConfigItem(
			position = 9,
			keyName = "offSetTextZ",
			name = "OffSet Text Z",
			description = "Used to offset the text on the z-axis")
	default int offSetTextZ() { return 200; }

	@ConfigItem(
			position = 110,
			keyName = "specColor",
			name = "Spec Color",
			description = "The default color for spec energy")
	default Color getSpecColor()
	{
		return new Color(255,255,0,100);
	}

}
