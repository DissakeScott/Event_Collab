package com.eventcollab.ticket.domain;

public enum TicketStatus {
    ACTIVE,     // billet valide
    CANCELLED,  // annulé par l utilisateur
    USED        // scanné au check-in
}