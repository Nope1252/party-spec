package com.partyspec;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = true)
public class PartySpecUpdate extends PartyMemberMessage
{
    public PartySpecUpdate(int spec, long memberID){
        this.spec = spec;
        this.setMemberId(memberID);
    }
    private int spec;
}
