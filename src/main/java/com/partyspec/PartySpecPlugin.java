package com.partyspec;

import com.google.inject.Provides;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;

import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PartyChanged;

import net.runelite.client.party.events.UserJoin;
import net.runelite.client.party.events.UserPart;
import net.runelite.client.party.messages.PartyMemberMessage;
import net.runelite.client.party.messages.UserSync;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.util.Text;

@PluginDescriptor(
		name = "Party Special Attack",
		description = "Displays remaining special attack energy percentage for your party"
)
@Slf4j
public class PartySpecPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PartyService partyService;

	@Inject
	private WSClient wsClient;

	@Inject
	private PartySpecOverlay partySpecOverlay;

	@Inject
	private PartySpecConfig config;

	@Getter(AccessLevel.PACKAGE)
	private final Map<String, PartySpecMember> members = new ConcurrentHashMap<>();

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private int lastKnownHP = -1;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private boolean queuedUpdate = false;

	@Getter(AccessLevel.PACKAGE)
	private int lastUpdatedSpec = -1;

	/**
	 * Visible players from the configuration (Strings)
	 */
	private List<String> visiblePlayers = new ArrayList<>();

	private final String DEFAULT_MEMBER_NAME = "<unknown>";


	@Provides
	PartySpecConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartySpecConfig.class);
	}

	@Override
	protected void startUp()
	{
		visiblePlayers = getVisiblePlayers();
		overlayManager.add(partySpecOverlay);
		wsClient.registerMessage(PartySpecUpdate.class);
	}

	@Override
	protected void shutDown()
	{
		wsClient.unregisterMessage(PartySpecUpdate.class);
		overlayManager.remove(partySpecOverlay);
		members.clear();
	}

	@Subscribe
	public void onPartyChanged(PartyChanged partyChanged)
	{
		members.clear();
	}

	@Subscribe(priority = 1)
	public void onUserJoin(final UserJoin message)
	{
		queuedUpdate = true;
	}

	@Subscribe(priority = 1000)
	public void onUserPart(final UserPart message)
	{
		members.remove(partyService.getMemberById(message.getMemberId()).getDisplayName());
	}

	@Subscribe
	public void onGameTick(GameTick event){
		int currentSpec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
		if((currentSpec != lastUpdatedSpec || queuedUpdate) && client.getLocalPlayer() != null && partyService.isInParty()){
			String name = partyService.getMemberById(partyService.getLocalMember().getMemberId()).getDisplayName();
			if(!name.equals(DEFAULT_MEMBER_NAME)){
				SendUpdate(currentSpec);
				lastUpdatedSpec = currentSpec;
				queuedUpdate = false;
			}
		}
	}

	public boolean LocalMemberIsValid(){
		PartyMember localMember = partyService.getLocalMember();
		return (localMember != null);
	}

	public boolean MemberIsValid(PartyMemberMessage message, boolean allowSelf){

		if(!allowSelf) {
			if(partyService.getLocalMember().getMemberId() == message.getMemberId()){
				return false;
			}
		}

		String name = partyService.getMemberById(message.getMemberId()).getDisplayName();
		if (name == null)
		{
			return false;
		}

		return true;
	}

	public void SendUpdate(int spec){
		if(LocalMemberIsValid()){
			long localID = partyService.getLocalMember().getMemberId();
			String name = partyService.getMemberById(partyService.getLocalMember().getMemberId()).getDisplayName();

			partyService.send(new PartySpecUpdate(spec, localID));
			//handle self locally.
			PartySpecMember partySpecMember = members.computeIfAbsent(name, PartySpecMember::new);
			partySpecMember.setSpec(spec);
		}
	}

	public List<String> getVisiblePlayers()
	{
		final String configPlayers = config.getWhitelist().toLowerCase();

		if (configPlayers.isEmpty())
		{
			return Collections.emptyList();
		}

		return Text.fromCSV(configPlayers);
	}

	@Subscribe
	public void onPartySpecUpdate(PartySpecUpdate partySpecUpdate)
	{
		if(!MemberIsValid(partySpecUpdate,false)){
			return;
		}

		PartySpecMember partySpecMember = members.computeIfAbsent(partyService.getMemberById(partySpecUpdate.getMemberId()).getDisplayName(), PartySpecMember::new);
		partySpecMember.setSpec(partySpecUpdate.getSpec());
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals("partyspec"))
		{
			return;
		}

		visiblePlayers = getVisiblePlayers();
	}


}
