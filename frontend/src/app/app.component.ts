import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { AuthService } from './core/services/auth.service';
import { NotificationService } from './core/services/notification.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent],
  template: `
    <app-navbar />
    <main><router-outlet /></main>
  `
})
export class AppComponent implements OnInit {
  constructor(
    private authService: AuthService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    // Si l'utilisateur est déjà connecté quand il ouvre l'app, on lance le WebSocket
    if (this.authService.isLoggedIn()) {
      this.notificationService.connectWebSocket();
      this.notificationService.getUnreadCount().subscribe();
    }
  }
}