export interface Ticket {
  id: string; eventId: string; eventTitle?: string; userId: string; userEmail: string;
  status: 'ACTIVE'|'CANCELLED'|'USED'; qrCode?: string;
  bookedAt: string; cancelledAt?: string;
}