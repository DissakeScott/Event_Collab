import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../models/user.model';
import { ApiResponse } from '../models/api.model';
import { NotificationService } from './notification.service'; // <-- NOUVEAU : Importation du service

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly API = '/api/v1/auth';

  // Signal initialisé depuis localStorage au démarrage
  currentUser = signal<User | null>(this.loadUser());

  constructor(
    private http: HttpClient, 
    private router: Router,
    private notificationService: NotificationService // <-- NOUVEAU : Injection du service
  ) {}

  login(req: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.API}/login`, req).pipe(
      tap(res => this.saveSession(res.data))
    );
  }

  register(req: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.API}/register`, req).pipe(
      tap(res => this.saveSession(res.data))
    );
  }

  logout(): void {
    // --- NOUVEAU : Couper le WebSocket à la déconnexion ---
    this.notificationService.disconnectWebSocket();
    // ------------------------------------------------------

    localStorage.removeItem('ec_token');
    localStorage.removeItem('ec_refresh');
    localStorage.removeItem('ec_user');
    this.currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null { return localStorage.getItem('ec_token'); }
  isLoggedIn(): boolean { return !!this.getToken(); }
  isOrganizer(): boolean {
    const r = this.currentUser()?.role;
    return r === 'ORGANIZER' || r === 'ADMIN';
  }
  isAdmin(): boolean { return this.currentUser()?.role === 'ADMIN'; }

  private saveSession(auth: AuthResponse): void {
    localStorage.setItem('ec_token',   auth.accessToken);
    localStorage.setItem('ec_refresh', auth.refreshToken);
    localStorage.setItem('ec_user',    JSON.stringify(auth.user));
    this.currentUser.set(auth.user);

    // --- NOUVEAU : Allumer le WebSocket une fois connecté ---
    this.notificationService.connectWebSocket();
    // --------------------------------------------------------
  }

  private loadUser(): User | null {
    try {
      const raw = localStorage.getItem('ec_user');
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }
}