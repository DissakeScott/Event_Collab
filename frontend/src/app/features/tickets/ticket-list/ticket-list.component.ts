import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TicketService } from '../../../core/services/ticket.service';
import { Ticket } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container page">
      <div class="page-header">
        <h1>Mes billets</h1>
        <p>{{ tickets.length }} billet(s) au total</p>
      </div>

      @if (loading) {
        <div class="spinner-wrap"><div class="spinner"></div></div>
      } @else if (tickets.length === 0) {
        <div class="empty-state">
          <div class="icon">🎟️</div>
          <h3>Aucun billet</h3>
          <p>Vous n avez pas encore reserve de billet</p>
          <a routerLink="/events" class="btn btn-primary" style="margin-top:20px">
            Explorer les evenements
          </a>
        </div>
      } @else {
        <div style="display:flex;flex-direction:column;gap:16px">
          @for (t of tickets; track t.id) {
            <div class="card" style="display:grid;grid-template-columns:1fr auto;gap:20px;align-items:center">
              <div style="display:flex;flex-direction:column;gap:10px">
                
                <div style="font-size:18px;font-weight:800;color:var(--primary);margin-bottom:4px;">
                  {{ t.eventTitle || 'Événement non spécifié' }}
                </div>
                <div style="display:flex;align-items:center;gap:12px">
                  <span class="badge" [class]="statusBadge(t.status)">{{ t.status }}</span>
                  <span style="font-size:13px;color:var(--text-dim)">#{{ t.id.slice(0,8).toUpperCase() }}</span>
                </div>
                <div style="font-size:13px;color:var(--text-muted)">
                  Reserve le {{ t.bookedAt | date:"dd MMM yyyy" + " a " + "HH:mm" }}
                </div>
                @if (t.cancelledAt) {
                  <div style="font-size:13px;color:var(--danger)">
                    Annule le {{ t.cancelledAt | date:"dd MMM yyyy" }}
                  </div>
                }
              </div>

              <div style="display:flex;flex-direction:column;align-items:center;gap:12px">
                @if (t.status === "ACTIVE" && t.qrCode) {
                  <img [src]="t.qrCode" alt="QR" style="width:120px;height:120px;border-radius:8px;border:2px solid var(--border)">
                  <span style="font-size:11px;color:var(--text-dim)">Scan au check-in</span>
                }
                @if (t.status === "ACTIVE") {
                  <button class="btn btn-danger btn-sm" (click)="cancel(t)" [disabled]="cancelling===t.id">
                    {{ cancelling===t.id ? "..." : "Annuler" }}
                  </button>
                }
              </div>
            </div>
          }
        </div>
      }
    </div>
  `
})
export class TicketListComponent implements OnInit {
  tickets: Ticket[] = [];
  loading = true;
  cancelling = '';

  constructor(private svc: TicketService) {}

  ngOnInit() {
    this.svc.myTickets().subscribe({
      next: r => { this.tickets = r.data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  cancel(t: Ticket) {
    if (!confirm('Annuler ce billet ?')) return;
    this.cancelling = t.id;
    this.svc.cancel(t.id).subscribe({
      next: r => { const i = this.tickets.findIndex(x => x.id===t.id); if(i!==-1) this.tickets[i]=r.data; this.cancelling=''; },
      error: () => { this.cancelling = ''; }
    });
  }

  statusBadge(s: string): string {
    return {ACTIVE:'badge badge-success',CANCELLED:'badge badge-danger',USED:'badge badge-info'}[s] || 'badge';
  }
}