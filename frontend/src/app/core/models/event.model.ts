export interface Event {
  id: string; title: string; description: string; location: string;
  startDate: string; endDate: string; maxCapacity: number;
  currentCapacity: number; availableSpots: number;
  isFull: boolean; status: 'DRAFT'|'PUBLISHED'|'CANCELLED'|'COMPLETED';
  organizerId: string; organizerEmail: string; createdAt: string;
}
export interface CreateEventRequest {
  title: string; description?: string; location: string;
  startDate: string; endDate: string; maxCapacity: number;
}