import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event, CreateEventRequest } from '../models/event.model';
import { ApiResponse } from '../models/api.model';

@Injectable({ providedIn: 'root' })
export class EventService {
  private readonly API = '/api/v1/events';
  constructor(private http: HttpClient) {}
  getAll(): Observable<ApiResponse<Event[]>> { return this.http.get<ApiResponse<Event[]>>(this.API); }
  getById(id: string): Observable<ApiResponse<Event>> { return this.http.get<ApiResponse<Event>>(`${this.API}/${id}`); }
  getMyEvents(): Observable<ApiResponse<Event[]>> { return this.http.get<ApiResponse<Event[]>>(`${this.API}/mine`); }
  search(kw: string): Observable<ApiResponse<Event[]>> { return this.http.get<ApiResponse<Event[]>>(`${this.API}/search?keyword=${kw}`); }
  create(req: CreateEventRequest): Observable<ApiResponse<Event>> { return this.http.post<ApiResponse<Event>>(this.API, req); }
  update(id: string, req: any): Observable<ApiResponse<Event>> { return this.http.patch<ApiResponse<Event>>(`${this.API}/${id}`, req); }
  publish(id: string): Observable<ApiResponse<Event>> { return this.http.post<ApiResponse<Event>>(`${this.API}/${id}/publish`, {}); }
  cancel(id: string): Observable<ApiResponse<Event>> { return this.http.post<ApiResponse<Event>>(`${this.API}/${id}/cancel`, {}); }
  delete(id: string): Observable<void> { return this.http.delete<void>(`${this.API}/${id}`); }
}