import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EventService } from '../../core/services/event.service';
import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';
import { Event } from '../../core/models/event.model';
import { Ticket } from '../../core/models/ticket.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="container page">

      <div class="page-header">
        <h1>Dashboard Organisateur</h1>
        <p>Bonjour {{ auth.currentUser()?.firstName }} — gerez vos evenements</p>
      </div>

      <!-- Stats -->
      <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:32px">
        <div class="stat-card">
          <div class="value">{{ events.length }}</div>
          <div class="label">Evenements</div>
        </div>
        <div class="stat-card">
          <div class="value" style="background:linear-gradient(135deg,var(--success),var(--accent));-webkit-background-clip:text;-webkit-text-fill-color:transparent">
            {{ publishedCount }}
          </div>
          <div class="label">Publies</div>
        </div>
        <div class="stat-card">
          <div class="value" style="background:linear-gradient(135deg,var(--warning),var(--danger));-webkit-background-clip:text;-webkit-text-fill-color:transparent">
            {{ totalParticipants }}
          </div>
          <div class="label">Participants</div>
        </div>
      </div>

      <div style="display:grid;grid-template-columns:1fr 1.2fr;gap:24px;align-items:start">

        <!-- Formulaire -->
        <div class="card">
          <h3 style="font-size:16px;font-weight:700;margin-bottom:20px">Creer un evenement</h3>

          @if (ok) { <div class="alert alert-success">{{ ok }}</div> }
          @if (err) { <div class="alert alert-error">{{ err }}</div> }

          <form [formGroup]="form" (ngSubmit)="create()">
            <div class="form-group">
              <label>Titre</label>
              <input class="form-control" formControlName="title" placeholder="Titre de l evenement">
            </div>
            <div class="form-group">
              <label>Description</label>
              <textarea class="form-control" formControlName="description" rows="2" placeholder="Description..."></textarea>
            </div>
            <div class="form-group">
              <label>Lieu</label>
              <input class="form-control" formControlName="location" placeholder="Laval, France">
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
              <div class="form-group">
                <label>Debut</label>
                <input class="form-control" type="datetime-local" formControlName="startDate">
              </div>
              <div class="form-group">
                <label>Fin</label>
                <input class="form-control" type="datetime-local" formControlName="endDate">
              </div>
            </div>
            <div class="form-group">
              <label>Capacite maximale</label>
              <input class="form-control" type="number" formControlName="maxCapacity" placeholder="100">
            </div>
            <button class="btn btn-primary" style="width:100%" type="submit" [disabled]="creating">
              {{ creating ? "Creation..." : "Creer l evenement" }}
            </button>
          </form>
        </div>

        <!-- Liste events -->
        <div class="card">
          <h3 style="font-size:16px;font-weight:700;margin-bottom:20px">Mes evenements</h3>

          @if (loading) {
            <div class="spinner-wrap" style="padding:40px"><div class="spinner"></div></div>
          } @else if (events.length === 0) {
            <div class="empty-state" style="padding:40px 0">
              <p>Aucun evenement pour le moment</p>
            </div>
          } @else {
            <div style="display:flex;flex-direction:column;gap:12px">
              @for (e of events; track e.id) {
                <div style="background:var(--bg2);border:1px solid var(--border);border-radius:var(--radius-sm);padding:16px">
                  <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:10px">
                    <div>
                      <div style="font-weight:600;margin-bottom:4px">{{ e.title }}</div>
                      <div style="font-size:13px;color:var(--text-muted)">{{ e.location }}</div>
                    </div>
                    <span class="badge" [class]="sb(e.status)">{{ e.status }}</span>
                  </div>

                  <div style="font-size:13px;color:var(--text-muted);margin-bottom:12px">
                    👥 {{ e.currentCapacity }} / {{ e.maxCapacity }} participants
                  </div>

                  <div class="progress" style="margin-bottom:12px">
                    <div class="progress-bar" [style.width.%]="pct(e)"
                         [style.background]="e.isFull?'var(--danger)':'linear-gradient(90deg,var(--primary),var(--accent))'"></div>
                  </div>

                  <div style="display:flex;gap:8px;flex-wrap:wrap">
                    @if (e.status === "DRAFT") {
                      <button class="btn btn-success btn-sm" (click)="publish(e)">Publier</button>
                    }
                    @if (e.status === "PUBLISHED") {
                      <button class="btn btn-outline btn-sm" (click)="showTickets(e)">
                        {{ selectedId===e.id ? "Masquer" : "Participants" }}
                      </button>
                      <button class="btn btn-danger btn-sm" (click)="cancelEvent(e)">Annuler</button>
                    }
                  </div>

                  @if (selectedId === e.id) {
                    <div style="margin-top:14px;padding-top:14px;border-top:1px solid var(--border)">
                      @if (tickets.length === 0) {
                        <p style="color:var(--text-dim);font-size:13px">Aucun participant</p>
                      } @else {
                        @for (t of tickets; track t.id) {
                          <div style="display:flex;justify-content:space-between;align-items:center;padding:6px 0;font-size:13px;border-bottom:1px solid var(--border)">
                            <span style="color:var(--text-muted)">{{ t.userEmail }}</span>
                            <span class="badge" [class]="sb(t.status)">{{ t.status }}</span>
                          </div>
                        }
                      }
                    </div>
                  }
                </div>
              }
            </div>
          }
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  events: Event[] = [];
  tickets: Ticket[] = [];
  selectedId = '';
  loading = true; creating = false;
  ok = ''; err = '';
  form: FormGroup;

  constructor(
    private eventSvc: EventService,
    private ticketSvc: TicketService,
    public  auth: AuthService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      title:       ['', Validators.required],
      description: [''],
      location:    ['', Validators.required],
      startDate:   ['', Validators.required],
      endDate:     ['', Validators.required],
      maxCapacity: [100, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit() {
    this.eventSvc.getMyEvents().subscribe({
      next: r => { this.events = r.data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get publishedCount() { return this.events.filter(e => e.status==='PUBLISHED').length; }
  get totalParticipants() { return this.events.reduce((s,e) => s+e.currentCapacity, 0); }
  pct(e: Event) { return Math.round((e.currentCapacity/e.maxCapacity)*100); }

  create() {
    if (this.form.invalid) return;
    this.creating = true; this.ok = ''; this.err = '';
    const v = this.form.value;
    const req = { ...v,
      startDate: new Date(v.startDate).toISOString().slice(0,19),
      endDate:   new Date(v.endDate).toISOString().slice(0,19)
    };
    this.eventSvc.create(req).subscribe({
      next: r => { this.events.unshift(r.data); this.ok = "Evenement cree !"; this.form.reset({maxCapacity:100}); this.creating = false; },
      error: e => { this.err = e.error?.message || "Erreur"; this.creating = false; }
    });
  }

  publish(e: Event) {
    this.eventSvc.publish(e.id).subscribe({
      next: r => { const i=this.events.findIndex(x=>x.id===e.id); if(i!==-1) this.events[i]=r.data; },
      error: () => {}
    });
  }

  cancelEvent(e: Event) {
    if (!confirm('Annuler cet evenement ?')) return;
    this.eventSvc.cancel(e.id).subscribe({
      next: r => { const i=this.events.findIndex(x=>x.id===e.id); if(i!==-1) this.events[i]=r.data; },
      error: () => {}
    });
  }

  showTickets(e: Event) {
    if (this.selectedId === e.id) { this.selectedId = ''; return; }
    this.selectedId = e.id;
    this.ticketSvc.getByEvent(e.id).subscribe({
      next: r => { this.tickets = r.data; },
      error: () => {}
    });
  }

  sb(s: string): string {
    return {PUBLISHED:'badge badge-success',DRAFT:'badge badge-warning',CANCELLED:'badge badge-danger',
            COMPLETED:'badge badge-muted',ACTIVE:'badge badge-success',USED:'badge badge-info'}[s] || 'badge';
  }
}