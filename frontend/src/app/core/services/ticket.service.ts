import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ticket } from '../models/ticket.model';
import { ApiResponse } from '../models/api.model';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private readonly API = '/api/v1/tickets';
  constructor(private http: HttpClient) {}
  book(eventId: string): Observable<ApiResponse<Ticket>> { return this.http.post<ApiResponse<Ticket>>(this.API, { eventId }); }
  myTickets(): Observable<ApiResponse<Ticket[]>> { return this.http.get<ApiResponse<Ticket[]>>(`${this.API}/me`); }
  getById(id: string): Observable<ApiResponse<Ticket>> { return this.http.get<ApiResponse<Ticket>>(`${this.API}/${id}`); }
  cancel(id: string): Observable<ApiResponse<Ticket>> { return this.http.delete<ApiResponse<Ticket>>(`${this.API}/${id}`); }
  getByEvent(eventId: string): Observable<ApiResponse<Ticket[]>> { return this.http.get<ApiResponse<Ticket[]>>(`${this.API}/event/${eventId}`); }
  checkIn(id: string): Observable<ApiResponse<Ticket>> { return this.http.post<ApiResponse<Ticket>>(`${this.API}/${id}/checkin`, {}); }
}