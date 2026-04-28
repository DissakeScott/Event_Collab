import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  template: `
    <nav class="navbar">
      <div class="container navbar-inner">

        <a routerLink="/events" class="navbar-brand">
          ⚡ <span>EventCollab</span>
        </a>

        <div class="navbar-nav">
          <a routerLink="/events" routerLinkActive="active"
             [routerLinkActiveOptions]="{exact:false}" class="nav-link">
            Evenements
          </a>

          @if (auth.isLoggedIn()) {
            <a routerLink="/tickets" routerLinkActive="active" class="nav-link">Billets</a>

            @if (auth.isOrganizer()) {
              <a routerLink="/dashboard" routerLinkActive="active" class="nav-link">Dashboard</a>
            }

            <a routerLink="/notifications" routerLinkActive="active" class="nav-link">
              Notifications
              @if (ns.unreadCount() > 0) {
                <span class="unread-dot">{{ ns.unreadCount() }}</span>
              }
            </a>

            <div class="nav-divider"></div>

            <span style="font-size:13px;color:var(--text-muted);padding:0 8px">
              {{ auth.currentUser()?.firstName }}
            </span>

            <button class="btn btn-outline btn-sm" (click)="auth.logout()">
              Deconnexion
            </button>
          } @else {
            <div class="nav-divider"></div>
            <a routerLink="/auth/login"    class="btn btn-ghost btn-sm">Connexion</a>
            <a routerLink="/auth/register" class="btn btn-primary btn-sm">Inscription</a>
          }
        </div>
      </div>
    </nav>
  `
})
export class NavbarComponent implements OnInit {
  constructor(public auth: AuthService, public ns: NotificationService) {}
  ngOnInit() {
    if (this.auth.isLoggedIn()) {
      this.ns.getUnreadCount().subscribe({ error: () => {} });
    }
  }
}