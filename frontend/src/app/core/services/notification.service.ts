import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Notification, ChatMessage } from '../models/notification.model';
import { ApiResponse } from '../models/api.model';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Observable, Subject } from 'rxjs';
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly API = '/api/v1/notifications';
  private readonly CHAT = '/api/v1/chat';
  
  unreadCount = signal<number>(0);
  public refreshList$ = new Subject<void>();
  private stompClient: Client | null = null;

  constructor(private http: HttpClient) {}

  // --- REST HTTP CLASSIQUE ---
  getAll(): Observable<ApiResponse<Notification[]>> { 
    return this.http.get<ApiResponse<Notification[]>>(this.API); 
  }
  
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

  // --- WEBSOCKET / TEMPS RÉEL ---
  connectWebSocket() {
    const token = localStorage.getItem('ec_token');
    if (!token || this.stompClient?.active) return;

    this.stompClient = new Client({
      // On passe par SockJS pour assurer la compatibilité (pointé vers ton API Gateway)
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => {
        // Décommente pour voir les logs websocket dans la console
        // console.log(str); 
      },
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('🔌 Connecté au serveur de notifications temps réel');
        
        // On s'abonne à notre file d'attente personnelle
       this.stompClient?.subscribe('/user/queue/notifications', (message) => {
          if (message.body) {
            console.log('🔔 Nouvelle notification reçue en direct !');
            this.unreadCount.update(count => count + 1);
            this.refreshList$.next(); // <-- NOUVEAU : On donne l'ordre de rafraîchir la page
          }
        });
      }
    });

    this.stompClient.activate();
  }

  disconnectWebSocket() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      console.log('🔌 Déconnecté du serveur temps réel');
    }
  }
}