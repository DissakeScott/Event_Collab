import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';
import { Notification } from '../../../core/models/notification.model';

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container page">
      <div style="display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:32px">
        <div class="page-header" style="margin:0">
          <h1>Notifications</h1>
          <p>{{ unread }} non lue(s)</p>
        </div>
        @if (unread > 0) {
          <button class="btn btn-outline btn-sm" (click)="markAll()">
            Tout marquer comme lu
          </button>
        }
      </div>

      @if (loading) {
        <div class="spinner-wrap"><div class="spinner"></div></div>
      } @else if (notifs.length === 0) {
        <div class="empty-state">
          <div class="icon">🔔</div>
          <h3>Aucune notification</h3>
          <p>Vous etes a jour !</p>
        </div>
      } @else {
        <div style="display:flex;flex-direction:column;gap:10px">
          @for (n of notifs; track n.id) {
            <div class="card" (click)="read(n)" [style.cursor]="n.read?'default':'pointer'"
                 [style.border-left]="n.read?'3px solid var(--border)':'3px solid var(--primary)'"
                 [style.opacity]="n.read?'0.65':'1'">
              <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:16px">
                <div style="flex:1">
                  <div style="display:flex;align-items:center;gap:10px;margin-bottom:8px">
                    <span class="badge badge-info">{{ n.type }}</span>
                    @if (!n.read) { <span style="width:8px;height:8px;background:var(--primary);border-radius:50%;display:inline-block"></span> }
                  </div>
                  <div style="font-weight:600;margin-bottom:4px">{{ n.title }}</div>
                  <div style="color:var(--text-muted);font-size:14px">{{ n.message }}</div>
                </div>
                <div style="font-size:12px;color:var(--text-dim);white-space:nowrap">
                  {{ n.createdAt | date:"dd/MM HH:mm" }}
                </div>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `
})
export class NotificationListComponent implements OnInit {
  notifs: Notification[] = [];
  loading = true;
  unread = 0;

  constructor(private svc: NotificationService) {}

  ngOnInit() {
    this.svc.getAll().subscribe({
      next: r => { this.notifs = r.data; this.unread = r.data.filter(n=>!n.read).length; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  read(n: Notification) {
    if (n.read) return;
    this.svc.markAsRead(n.id).subscribe(() => {
      n.read = true; this.unread--;
      this.svc.unreadCount.update(c => Math.max(0, c-1));
    });
  }

  markAll() {
    this.svc.markAllAsRead().subscribe(() => {
      this.notifs.forEach(n => n.read = true);
      this.unread = 0;
    });
  }
}