import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Notification, ChatMessage } from '../models/notification.model';
import { ApiResponse } from '../models/api.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly API = '/api/v1/notifications';
  private readonly CHAT = '/api/v1/chat';
  unreadCount = signal<number>(0);
  constructor(private http: HttpClient) {}
  getAll(): Observable<ApiResponse<Notification[]>> { return this.http.get<ApiResponse<Notification[]>>(this.API); }
  getUnreadCount(): Observable<ApiResponse<number>> {
    return this.http.get<ApiResponse<number>>(`${this.API}/unread/count`).pipe(
      tap(res => this.unreadCount.set(res.data))
    );
  }
  markAsRead(id: string): Observable<ApiResponse<Notification>> {
    return this.http.patch<ApiResponse<Notification>>(`${this.API}/${id}/read`, {});
  }
  markAllAsRead(): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.API}/read-all`, {}).pipe(
      tap(() => this.unreadCount.set(0))
    );
  }
  getChatHistory(eventId: string): Observable<ApiResponse<ChatMessage[]>> {
    return this.http.get<ApiResponse<ChatMessage[]>>(`${this.CHAT}/${eventId}/history`);
  }
  sendChatMessage(eventId: string, content: string): Observable<ApiResponse<ChatMessage>> {
    return this.http.post<ApiResponse<ChatMessage>>(`${this.CHAT}/send`, { eventId, content });
  }
}