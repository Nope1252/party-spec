package com.partyspec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@Setter
class PartySpecMember
{
    private final String name;
    private long memberID;
    private int spec;
}
