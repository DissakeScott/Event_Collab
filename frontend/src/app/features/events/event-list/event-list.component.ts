import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../../core/services/event.service';
import { Event } from '../../../core/models/event.model';

@Component({
  selector: 'app-event-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="container page">

      <!-- Header -->
      <div style="display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:32px;flex-wrap:gap;gap:16px">
        <div class="page-header" style="margin:0">
          <h1>Evenements</h1>
          <p>{{ events.length }} evenement(s) a decouvrir</p>
        </div>
        <div style="position:relative">
          <input class="form-control" style="width:280px;padding-left:40px"
                 placeholder="Rechercher un evenement..."
                 [(ngModel)]="keyword" (ngModelChange)="onSearch($event)">
          <span style="position:absolute;left:14px;top:50%;transform:translateY(-50%);color:var(--text-dim)">🔍</span>
        </div>
      </div>

      @if (loading) {
        <div class="spinner-wrap"><div class="spinner"></div></div>
      } @else if (events.length === 0) {
        <div class="empty-state">
          <div class="icon">🎭</div>
          <h3>Aucun evenement</h3>
          <p>Revenez bientot ou modifiez votre recherche</p>
        </div>
      } @else {
        <div class="grid-3">
          @for (e of events; track e.id) {
            <div class="card" style="display:flex;flex-direction:column;gap:0;padding:0;overflow:hidden;cursor:pointer"
                 [routerLink]="['/events', e.id]">

              <!-- Couleur haut de carte -->
              <div [style.background]="cardGradient(e.status)"
                   style="height:6px;width:100%"></div>

              <div style="padding:20px;flex:1;display:flex;flex-direction:column;gap:14px">
                <!-- Status + full -->
                <div style="display:flex;justify-content:space-between;align-items:center">
                  <span class="badge" [class]="statusBadge(e.status)">{{ e.status }}</span>
                  @if (e.isFull) { <span class="badge badge-danger">Complet</span> }
                </div>

                <!-- Titre -->
                <div>
                  <h3 style="font-size:17px;font-weight:700;margin-bottom:6px;line-height:1.3">{{ e.title }}</h3>
                  @if (e.description) {
                    <p style="color:var(--text-muted);font-size:13px;line-height:1.5;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden">
                      {{ e.description }}
                    </p>
                  }
                </div>

                <!-- Infos -->
                <div style="display:flex;flex-direction:column;gap:6px;font-size:13px;color:var(--text-muted)">
                  <div style="display:flex;align-items:center;gap:8px">
                    <span>📍</span><span>{{ e.location }}</span>
                  </div>
                  <div style="display:flex;align-items:center;gap:8px">
                    <span>📅</span><span>{{ e.startDate | date:"dd MMM yyyy, HH:mm" }}</span>
                  </div>
                </div>

                <!-- Capacite -->
                <div>
                  <div style="display:flex;justify-content:space-between;font-size:12px;color:var(--text-dim);margin-bottom:6px">
                    <span>{{ e.currentCapacity }} inscrits</span>
                    <span>{{ e.availableSpots }} places restantes</span>
                  </div>
                  <div class="progress">
                    <div class="progress-bar"
                         [style.width.%]="pct(e)"
                         [style.background]="e.isFull ? 'var(--danger)' : 'linear-gradient(90deg,var(--primary),var(--accent))'">
                    </div>
                  </div>
                </div>

                <div style="margin-top:auto">
                  <span class="btn btn-primary btn-sm" style="width:100%;justify-content:center">
                    Voir les details →
                  </span>
                </div>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `
})
export class EventListComponent implements OnInit {
  events: Event[] = [];
  loading = true;
  keyword = '';
  private t: any;

  constructor(private svc: EventService) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.svc.getAll().subscribe({
      next: r => { this.events = r.data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  onSearch(kw: string) {
    clearTimeout(this.t);
    this.t = setTimeout(() => {
      if (kw.trim().length >= 2) {
        this.svc.search(kw).subscribe({ next: r => this.events = r.data, error: () => {} });
      } else { this.load(); }
    }, 300);
  }

  pct(e: Event) { return Math.round((e.currentCapacity / e.maxCapacity) * 100); }

  cardGradient(s: string): string {
    const m: Record<string,string> = {
      PUBLISHED: 'linear-gradient(90deg,var(--primary),var(--accent))',
      DRAFT:     'linear-gradient(90deg,var(--warning),#f97316)',
      CANCELLED: 'linear-gradient(90deg,var(--danger),#ec4899)',
      COMPLETED: 'linear-gradient(90deg,var(--text-dim),var(--text-muted))'
    };
    return m[s] || 'var(--border)';
  }

  statusBadge(s: string): string {
    const m: Record<string,string> = {
      PUBLISHED: 'badge badge-success', DRAFT: 'badge badge-warning',
      CANCELLED: 'badge badge-danger',  COMPLETED: 'badge badge-muted'
    };
    return m[s] || 'badge';
  }
}