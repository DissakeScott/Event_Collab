package com.eventcollab.notification.domain;

public enum NotificationType {
    TICKET_BOOKED,      // billet reserve
    TICKET_CANCELLED,   // billet annule
    EVENT_PUBLISHED,    // evenement publie
    EVENT_CANCELLED,    // evenement annule
    EVENT_REMINDER,     // rappel avant l evenement
    CHAT_MESSAGE        // nouveau message dans le chat
}